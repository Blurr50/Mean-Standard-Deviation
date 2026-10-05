package myhre;

import java.awt.EventQueue;
import javax.swing.JFileChooser;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;

public class Main {

	private JFrame frame;
	private JButton fileButton;
	private JLabel mean;
	private JLabel stdDeviation;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize();
		createEvents();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblNewLabel = new JLabel("Mean and Standard Deviation Calculator");
		lblNewLabel.setBounds(75, 11, 286, 14);
		frame.getContentPane().add(lblNewLabel);

		fileButton = new JButton("Pick File");
		fileButton.setBounds(154, 36, 122, 22);
		frame.getContentPane().add(fileButton);

		JLabel lblNewLabel_1 = new JLabel("Standard Deviation: ");
		lblNewLabel_1.setBounds(29, 165, 173, 14);
		frame.getContentPane().add(lblNewLabel_1);

		JLabel lblNewLabel_2 = new JLabel("Mean: ");
		lblNewLabel_2.setBounds(29, 111, 93, 14);
		frame.getContentPane().add(lblNewLabel_2);

		mean = new JLabel("");
		mean.setBounds(212, 111, 192, 14);
		frame.getContentPane().add(mean);

		stdDeviation = new JLabel("");
		stdDeviation.setBounds(212, 165, 192, 14);
		frame.getContentPane().add(stdDeviation);
	}

	private void createEvents() {

		fileButton.addActionListener(e -> {
			// get file from user
			JFileChooser chooser = new JFileChooser();
			if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
				mean.setText("No file selected.");
				stdDeviation.setText("");
				return;
			}

			try {
				LinkedList<Integer> events = readNumbers(chooser.getSelectedFile());
				if (events.isEmpty()) {
					mean.setText("No numbers found in file.");
					stdDeviation.setText("");
					return;
				}
				double meanValue = calculateMean(events);
				double stdDevValue = calculateStandardDeviation(events, meanValue);
				mean.setText(String.format("%.2f", meanValue));
				stdDeviation.setText(String.format("%.2f", stdDevValue));
			} catch (FileNotFoundException f) {
				mean.setText("Could not read file.");
				stdDeviation.setText("");
			}
		});

	}

	public static LinkedList<Integer> readNumbers(File file) throws FileNotFoundException {
		LinkedList<Integer> numbers = new LinkedList<>();
		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNext()) {
				if (scanner.hasNextInt()) {
					numbers.add(scanner.nextInt());
				} else {
					scanner.next();
				}
			}
		}
		return numbers;
	}

	public static double calculateMean(LinkedList<Integer> events) {
		if (events.isEmpty()) {
			return 0;
		}
		double sum = 0;
		for (int event : events) {
			sum += event;
		}
		return sum / events.size();
	}

	public static double calculateStandardDeviation(LinkedList<Integer> events, double mean) {
		if (events.size() < 2) {
			return 0;
		}
		double sum = 0;
		for (int event : events) {
			sum += (event - mean) * (event - mean);
		}
		return Math.sqrt(sum / (events.size() - 1));
	}
}
