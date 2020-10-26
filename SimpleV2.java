import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;

public class SimpleV2 {
	public void readFile(String path) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line = null;

		while ((line = reader.readLine()) != null) {
			String lineNumberStr = line.substring(0, 6);
			int lineNumber = Integer.parseInt(lineNumberStr.trim());

			String lineString = line.substring(8);

			System.out.println("Line Num:" + lineNumber + " Line:" + lineString);
		}
		reader.close();
	}

	public static void main(String[] args) {
		try {
			new SimpleV2().readFile(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
