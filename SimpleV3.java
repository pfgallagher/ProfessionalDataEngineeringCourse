import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.io.FileReader;

public class SimpleV3 {
	HashMap<String, Integer> wordCountMap = new HashMap<String, Integer>();

	public void readFile(String path) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line = null;

		while ((line = reader.readLine()) != null) {
			String lineString = line.substring(8);
			lineString = lineString.toLowerCase();

			String[] lineWords = lineString.split("\\W");

			for (String word : lineWords) {
				int curCount = wordCountMap.getOrDefault(word, 0);
				curCount++;
				wordCountMap.put(word, curCount);
			}
		}
		reader.close();
		wordCountMap.forEach((word, count) -> {
			System.out.println("Word:" + word + " Count:" + count);
		});
	}

	public static void main(String[] args) {
		try {
			new SimpleV3().readFile(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
