package controller;

import java.util.HashMap;

import model.huffman.Huffman;
import model.utility.FrequencyCounter;

public class Controller {

	FrequencyCounter fc;
	Huffman huffman = new Huffman();
	HashMap<Character, Integer> frequencies;
	String fileContent;
	double inputFileSize;

	public boolean load(String path) {
		FrequencyCounter fc = new FrequencyCounter(path);
		fc.readFile();
		frequencies = fc.getFrequencies();
		if (frequencies.size() == 0)
			return false;
		fileContent = fc.getFileContent();
		inputFileSize = fc.getFileSize();
		return true;
	}

	public void compress() {
		huffman.compress(frequencies);
		frequencies.clear();
	}

	public void decompress(String inputFilePath, String outputFilePath) {
		huffman.decompress(inputFilePath, outputFilePath);
	}

	public double saveAs(String path) {
		huffman.compressToFile(fileContent, path);
		double compressionRatio;
		boolean isCompressable = huffman.isCompressable();
		if (!isCompressable) {
			compressionRatio = -1; // Compression failed
			return compressionRatio;
		}
		double compressedFileSize = huffman.getCompressedFileSize();
		compressionRatio = compressedFileSize / inputFileSize;
		return compressionRatio;
	}

}
