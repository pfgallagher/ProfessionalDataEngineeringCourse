import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;

public class SimpleV1 {
	public void readFile(String path) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line = null;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		reader.close();
	}

	public static void main(String[] args) {
		try {
			new SimpleV1().readFile(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
