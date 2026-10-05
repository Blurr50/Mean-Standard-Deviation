package testing;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import myhre.Main;

class MainTest {

	private static final double DELTA = 1e-9;

	@TempDir
	Path tempDir;

	/** Helper: writes text to a temp file and returns it. */
	private File writeFile(String name, String content) throws IOException {
		Path path = tempDir.resolve(name);
		Files.writeString(path, content);
		return path.toFile();
	}

	/** Helper: builds a LinkedList<Integer> from the given values. */
	private static LinkedList<Integer> list(Integer... values) {
		return new LinkedList<>(Arrays.asList(values));
	}

	// ------------------------------------------------------------------
	// calculateMean
	// ------------------------------------------------------------------
	@Nested
	@DisplayName("calculateMean")
	class MeanTests {

		@Test
		void emptyListReturnsZero() {
			assertEquals(0.0, Main.calculateMean(new LinkedList<>()), DELTA);
		}

		@Test
		void singleValueIsItsOwnMean() {
			assertEquals(7.0, Main.calculateMean(list(7)), DELTA);
		}

		@Test
		void meanOfSeveralValues() {
			assertEquals(3.0, Main.calculateMean(list(1, 2, 3, 4, 5)), DELTA);
		}

		@Test
		void meanIsNotTruncatedToInteger() {
			// (1 + 2) / 2 = 1.5, not 1
			assertEquals(1.5, Main.calculateMean(list(1, 2)), DELTA);
		}

		@Test
		void handlesNegativeNumbers() {
			assertEquals(0.0, Main.calculateMean(list(-5, 5)), DELTA);
			assertEquals(-2.0, Main.calculateMean(list(-1, -2, -3)), DELTA);
		}

		@Test
		void largeValuesDoNotOverflow() {
			// Sum exceeds Integer.MAX_VALUE, but the double accumulator handles it
			LinkedList<Integer> big = list(Integer.MAX_VALUE, Integer.MAX_VALUE);
			assertEquals((double) Integer.MAX_VALUE, Main.calculateMean(big), DELTA);
		}
	}

	// ------------------------------------------------------------------
	// calculateStandardDeviation (population: divides by n)
	// ------------------------------------------------------------------
	@Nested
	@DisplayName("calculateStandardDeviation")
	class StdDevTests {

		@Test
		void emptyListReturnsZero() {
			assertEquals(0.0, Main.calculateStandardDeviation(new LinkedList<>(), 0), DELTA);
		}

		@Test
		void singleValueHasZeroDeviation() {
			assertEquals(0.0, Main.calculateStandardDeviation(list(42), 42), DELTA);
		}

		@Test
		void identicalValuesHaveZeroDeviation() {
			assertEquals(0.0, Main.calculateStandardDeviation(list(5, 5, 5, 5), 5), DELTA);
		}

		@Test
		void classicSampleExample() {
			// mean = 5, sum of squared deviations = 32, sample variance = 32 / 7
			LinkedList<Integer> data = list(2, 4, 4, 4, 5, 5, 7, 9);
			double mean = Main.calculateMean(data);
			assertEquals(5.0, mean, DELTA);
			assertEquals(Math.sqrt(32.0 / 7), Main.calculateStandardDeviation(data, mean), DELTA);
		}

		@Test
		void simpleKnownValue() {
			// {1, 3}: mean = 2, sum of squares = 2, sample variance = 2 / 1
			assertEquals(Math.sqrt(2), Main.calculateStandardDeviation(list(1, 3), 2.0), DELTA);
		}

		@Test
		void resultIsNeverNegative() {
			LinkedList<Integer> data = list(-10, 0, 10, 100);
			double mean = Main.calculateMean(data);
			assertTrue(Main.calculateStandardDeviation(data, mean) >= 0);
		}

		@Test
		void orderOfValuesDoesNotMatter() {
			LinkedList<Integer> a = list(1, 2, 3, 4, 10);
			LinkedList<Integer> b = list(10, 4, 3, 2, 1);
			assertEquals(Main.calculateStandardDeviation(a, Main.calculateMean(a)),
					Main.calculateStandardDeviation(b, Main.calculateMean(b)), DELTA);
		}
	}

	// ------------------------------------------------------------------
	// readNumbers (file parsing)
	// ------------------------------------------------------------------
	@Nested
	@DisplayName("readNumbers")
	class ReadNumbersTests {

		@Test
		void readsNumbersSeparatedBySpaces() throws IOException {
			File f = writeFile("spaces.txt", "1 2 3 4");
			assertEquals(List.of(1, 2, 3, 4), Main.readNumbers(f));
		}

		@Test
		void readsNumbersSeparatedByNewlines() throws IOException {
			File f = writeFile("lines.txt", "10\n20\n30\n");
			assertEquals(List.of(10, 20, 30), Main.readNumbers(f));
		}

		@Test
		void readsMixedWhitespace() throws IOException {
			File f = writeFile("mixed.txt", "1\t2\n  3   \r\n4");
			assertEquals(List.of(1, 2, 3, 4), Main.readNumbers(f));
		}

		@Test
		void readsNegativeNumbers() throws IOException {
			File f = writeFile("neg.txt", "-1 -20 5");
			assertEquals(List.of(-1, -20, 5), Main.readNumbers(f));
		}

		@Test
		void skipsNonIntegerTokens() throws IOException {
			File f = writeFile("junk.txt", "1 abc 2 hello 3");
			assertEquals(List.of(1, 2, 3), Main.readNumbers(f));
		}

		@Test
		void decimalsAreSkippedNotTruncated() throws IOException {
			// Documents current behavior: "3.5" is not an int, so it is dropped.
			File f = writeFile("decimals.txt", "1 3.5 2");
			assertEquals(List.of(1, 2), Main.readNumbers(f));
		}

		@Test
		void emptyFileGivesEmptyList() throws IOException {
			File f = writeFile("empty.txt", "");
			assertTrue(Main.readNumbers(f).isEmpty());
		}

		@Test
		void fileWithNoNumbersGivesEmptyList() throws IOException {
			File f = writeFile("words.txt", "just some words here");
			assertTrue(Main.readNumbers(f).isEmpty());
		}

		@Test
		void preservesOrderAndDuplicates() throws IOException {
			File f = writeFile("dupes.txt", "5 1 5 1 5");
			assertEquals(List.of(5, 1, 5, 1, 5), Main.readNumbers(f));
		}

		@Test
		void missingFileThrows() {
			File missing = tempDir.resolve("does_not_exist.txt").toFile();
			assertThrows(FileNotFoundException.class, () -> Main.readNumbers(missing));
		}

		@Test
		void outOfRangeIntegerIsSkipped() throws IOException {
			// Larger than Integer.MAX_VALUE: hasNextInt() is false, so it's skipped
			File f = writeFile("big.txt", "1 99999999999 2");
			assertEquals(List.of(1, 2), Main.readNumbers(f));
		}
	}

	// ------------------------------------------------------------------
	// Integration: file -> mean -> std dev
	// ------------------------------------------------------------------
	@Test
	@DisplayName("file contents flow through to correct statistics")
	void endToEndStatistics() throws IOException {
		File f = writeFile("data.txt", "2 4 4 4\n5 5 7 9");
		LinkedList<Integer> numbers = Main.readNumbers(f);

		double mean = Main.calculateMean(numbers);
		double sd = Main.calculateStandardDeviation(numbers, mean);

		assertEquals(5.0, mean, DELTA);
		assertEquals(Math.sqrt(32.0 / 7), sd, DELTA);	}

	@Test
	@DisplayName("empty file produces zeros without throwing")
	void endToEndEmptyFile() throws IOException {
		File f = writeFile("empty.txt", "");
		LinkedList<Integer> numbers = Main.readNumbers(f);

		assertEquals(0.0, Main.calculateMean(numbers), DELTA);
		assertEquals(0.0, Main.calculateStandardDeviation(numbers, 0.0), DELTA);
	}
}