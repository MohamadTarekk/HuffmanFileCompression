package model.huffman;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import model.utility.NodeComparator;

public class Huffman {

	private Map<Character, String> codes;
	private Map<String, Character> decompressingCodes;

	private double compressedFileSize;

	private static String HEADER_BODY_SEPARATOR = "!@";

	private boolean isCompressable;

	int numberOfZerosToPad;

	public Huffman() {
		codes = new HashMap<Character, String>();
		decompressingCodes = new HashMap<String, Character>();
		isCompressable = true;
	}

	public void compress(HashMap<Character, Integer> frequencies) {
		buildHuffmanTree(frequencies);
		printTable();
	}

	private void buildHuffmanTree(HashMap<Character, Integer> frequencies) {
		PriorityQueue<Node> queue = new PriorityQueue<Node>(frequencies.size(), new NodeComparator());

		// create the leafs
		for (Map.Entry<Character, Integer> entry : frequencies.entrySet()) {
			queue.add(new Node(entry.getKey(), entry.getValue()));
		}

		// merge the leafs
		while (queue.size() > 1) {
			// Remove the 2 smallest nodes
			Node left = queue.poll();
			Node right = queue.poll();

			// Create an internal node
			Node internalNode = new Node(left.getFrequency() + right.getFrequency(), left, right);

			queue.add(internalNode);
		}

		assignCodes(queue.poll(), "");

		return;
	}

	private void assignCodes(Node root, String code) {

		if (root == null)
			return;

		if (root.isLeaf())
			codes.put(root.getCharacter(), code);

		assignCodes(root.getLeft(), code + "0");
		assignCodes(root.getRight(), code + "1");
	}

	public void compressToFile(String fileContent, String outputFileName) {

		try {

			StringBuilder encoded = new StringBuilder();
			// append all the codes to encoded
			for (char c : fileContent.toCharArray()) {
				encoded.append(codes.get(c));
			}

			// calculate zero padding
			encoded = calculateZeroPadding(encoded);

			int numberOfBytes = encoded.length() / 8;

			int headerSize = calculateHeaderSize(numberOfBytes);

			int lengthOfInputFile = fileContent.length();
			if (headerSize + numberOfBytes > lengthOfInputFile) {
				isCompressable = false;
			}

			if (isCompressable) {
				File outputFile = new File(outputFileName);
				FileWriter fileWriter = new FileWriter(outputFile);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				FileOutputStream fout = new FileOutputStream(outputFile, true);

				bufferedWriter.write(numberOfBytes + " " + numberOfZerosToPad + System.lineSeparator());
				writeLineSeparatorCodes(bufferedWriter);
				// Write header
				writeHeader(bufferedWriter);
				// Write body
				writeBody(fout, encoded);
				compressedFileSize = outputFile.length();
				codes.clear();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private int calculateHeaderSize(int numberOfBytes) {
		int headerSize = 0;
		int numberOfDigits = String.valueOf(numberOfBytes).length();
		// First line in the header contains numberOfbytes + space + numberOfPaddedZeros
		// + \r\n
		headerSize = headerSize + numberOfDigits + 1 + 1 + 2;

		// Second line in the header contains the code of the new line + space +
		// carriage return + \r\n
		if (codes.containsKey('\n'))
			headerSize += codes.get('\n').length();
		if (codes.containsKey('\r'))
			headerSize = headerSize + 1 + codes.get('\r').length();

		headerSize += 2;

		// Character + : + space + code

		for (Entry<Character, String> entry : codes.entrySet()) {
			if (entry.getKey() == '\n' || entry.getKey() == '\r')
				continue;
			headerSize = headerSize + 5 + entry.getValue().length();
		}

		// Header_body separator
		headerSize += 4;

		return headerSize;
	}

	private StringBuilder calculateZeroPadding(StringBuilder encoded) {
		int remainder = (encoded.length()) % 8;
		if (remainder == 0) {
			numberOfZerosToPad = 0;
			return encoded;
		}

		numberOfZerosToPad = 8 - remainder;

		for (int i = 0; i < numberOfZerosToPad; i++)
			encoded.append('0');

		return encoded;
	}

	private void writeHeader(BufferedWriter bufferedWriter) {
		try {

			for (Entry<Character, String> entry : codes.entrySet()) {
				if (entry.getKey() == '\n' || entry.getKey() == '\r')
					continue;
				bufferedWriter.write(entry.getKey() + ": " + entry.getValue() + System.lineSeparator());
			}

			bufferedWriter.write(HEADER_BODY_SEPARATOR + System.lineSeparator());

			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeBody(FileOutputStream fout, StringBuilder encoded) {
		try {
			int startIndex = 0;
			int endIndex = 8;
			int length = encoded.length() / 8;

			for (int i = 0; i < length; i++) {
				byte binary = (byte) Integer.parseInt(encoded.substring(startIndex, endIndex), 2);
				startIndex = endIndex;
				endIndex += 8;
				// System.out.println(binary);
				fout.write((char) binary);
			}
			// System.out.println("-----");
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeLineSeparatorCodes(BufferedWriter bufferedWriter) {
		try {
			if (codes.containsKey('\n')) {
				bufferedWriter.write(codes.get('\n'));
			}
			if (codes.containsKey('\r')) {
				bufferedWriter.write(" " + codes.get('\r'));
			}

			bufferedWriter.write(System.lineSeparator());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void decompress(String inputFileName, String outputFileName) {

		try {

			File inputFile = new File(inputFileName);
			File outputFile = new File(outputFileName);

			FileReader fileReader = new FileReader(inputFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			FileWriter fileWriter = new FileWriter(outputFile);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			int numberOfBytes = readHeader(bufferedReader);
			byte[] fileContent = Files.readAllBytes(Paths.get(inputFileName));
			String fileAsBits = readBody(fileContent, numberOfBytes);
			// Node rootNode = buildDecodingTree();
			String decoded = decode(fileAsBits);

			decompressToFile(decoded, bufferedWriter);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int readHeader(BufferedReader bufferedReader) {
		int numberOfBytes = 0;
		numberOfZerosToPad = 0;
		try {
			String[] data = bufferedReader.readLine().split(" ");
			numberOfBytes = Integer.parseInt(data[0]);
			numberOfZerosToPad = Integer.parseInt(data[1]);

			String[] lineSeparators = bufferedReader.readLine().split(" ");
			if (lineSeparators.length == 2) {
				decompressingCodes.put(lineSeparators[0], '\n');
				decompressingCodes.put(lineSeparators[1], '\r');
			} else if (lineSeparators.length == 1) {
				decompressingCodes.put(lineSeparators[0], '\n');
			}

			// System.out.println(numberOfBytes + " " + numberOfZerosToPad);
			String line;
			while (!((line = bufferedReader.readLine()).equals(HEADER_BODY_SEPARATOR))) {
				String[] currentLine = line.split(": ");
				if (currentLine.length == 2) {
					decompressingCodes.put(currentLine[1], currentLine[0].charAt(0));
				} else
					continue;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return numberOfBytes;

	}

	private String readBody(byte[] fileContent, int bytesLength) {
		StringBuilder content = new StringBuilder();
		int fileSize = fileContent.length;
		// scan from the beginning of the body till the second to last byte (fileSize -
		// 1)
		for (int i = (fileSize - bytesLength); i < fileSize - 1; i++) {
			// Convert the byte to an unsigned integer
			int temp = fileContent[i] & 0xFF;
			// Convert the integer into an 8 bit string and append it to the string builder
			// I want it to take 8 place hence %8s
			// Use .replace to ensure that it takes the 8 bits
			content.append(String.format("%8s", Integer.toBinaryString(temp)).replace(' ', '0'));
		}

		// Handle the last byte
		int lastByte = fileContent[fileContent.length - 1] & 0xFF;
		content.append(String.format("%8s", Integer.toBinaryString(lastByte)).replace(' ', '0'));
		int length = content.length();
		content.delete(length - numberOfZerosToPad, length);
		// System.out.println(content);
		return content.toString();
	}

	private String decode(String fileAsBits) {
		StringBuilder currentSequenceOfBits = new StringBuilder();
		StringBuilder decoded = new StringBuilder();
		int length = fileAsBits.length();
		for (int i = 0; i < length; i++) {
			currentSequenceOfBits.append(fileAsBits.charAt(i));

			if (decompressingCodes.containsKey(currentSequenceOfBits.toString())) {
				decoded.append(decompressingCodes.get(currentSequenceOfBits.toString()));
				currentSequenceOfBits.setLength(0);
			}

		}
		return decoded.toString();
	}

	private void decompressToFile(String decoded, BufferedWriter bufferedWriter) {
		try {
			bufferedWriter.write(decoded);
			bufferedWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

	private void printTable() {
		for (Entry<Character, String> entry : codes.entrySet()) {
			char character = entry.getKey();
			String code = entry.getValue();
			String binaryRepresentation = Integer.toBinaryString((int) character);
			System.out.println("Byte: " + (int) character + " Code: " + binaryRepresentation + " New Code: " + code);
		}
	}

	public Map<Character, String> getCodes() {
		return codes;
	}

	public double getCompressedFileSize() {
		return compressedFileSize;
	}

	public boolean isCompressable() {
		return isCompressable;
	}

}
