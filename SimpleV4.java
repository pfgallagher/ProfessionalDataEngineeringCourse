import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.io.FileReader;

public class SimpleV4 {
	HashMap<String, Integer> wordCountMap = new HashMap<String, Integer>();

	private void readFile(String path) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line = null;

		while ((line = reader.readLine()) != null) {
			String[] lineWords = map(line);
			reduce(lineWords);

		}
		reader.close();
		wordCountMap.forEach((word, count) -> {
			System.out.println("Word:" + word + " Count:" + count);
		});
		System.out.println(String.format("There are %s unique words", wordCountMap.size()));
	}

	private String[] map(String line) {
		String lineString = line.substring(8);
		lineString = lineString.toLowerCase();
		return lineString.split("\\W");
	}

	private void reduce(String[] lineWords) {
		for (String word : lineWords) {
			int curCount = wordCountMap.getOrDefault(word, 0);
			curCount++;
			wordCountMap.put(word, curCount);
		}
	}

	public static void main(String[] args) {
		try {
			new SimpleV4().readFile(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
