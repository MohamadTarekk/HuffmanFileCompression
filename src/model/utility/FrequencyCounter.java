package model.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class FrequencyCounter {

	private HashMap<Character, Integer> frequencies;
	private StringBuilder fileContent;
	private String filePath;
	private double fileSize;

	public FrequencyCounter(String path) {
		frequencies = new HashMap<Character, Integer>();
		fileContent = new StringBuilder();
		filePath = path;
	}

	public void readFile() {
		try {
			File file = new File(filePath);
			fileSize = file.length();
			FileReader fileReader = null;
			fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			int c;
			while ((c = bufferedReader.read()) != -1) {
				char character = (char) c;
				fileContent.append(character);
				if (!frequencies.containsKey(character)) {
					frequencies.put(character, 0);
				}
				frequencies.put(character, frequencies.get(character) + 1);
			}
			bufferedReader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

	public String getFileContent() {
		return fileContent.toString();
	}

	public HashMap<Character, Integer> getFrequencies() {
		return frequencies;
	}

	public double getFileSize() {
		return fileSize;
	}


}
