package controller;

import javafx.application.Application;
import javafx.stage.Stage;
import view.Compression;

public class Main extends Application {
	
	private Compression compression = new Compression();;

	public static void main(String[] args) {

		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		compression.initialize(primaryStage);
	}

	/*
	public static void main(String[] args) {
		FrequencyCounter fc = new FrequencyCounter("input.txt");
		fc.readFile();

		String fileContent = fc.getFileContent();
		HashMap<Character, Integer> frequencies = fc.getFrequencies();

		for (Entry<Character, Integer> entry : frequencies.entrySet()) {
			System.out.println("Character: " + entry.getKey() + " Frequency: " + entry.getValue());
		}

		Huffman huffman = new Huffman();

		huffman.compress(frequencies);
		for (Entry<Character, String> entry : huffman.getCodes().entrySet()) {
			System.out.println("Character: " + (int) entry.getKey() + " Code: " + entry.getValue());
		}

		huffman.compressToFile(fileContent, "output.txt");

		huffman.decompress("output.txt", "output2.txt");

		for (Entry<Character, String> entry : huffman.getCodes().entrySet()) {
			System.out.println("Character: " + (int) entry.getKey() + " Frequency: " + entry.getValue());
		}
		

	}
	*/

}
