import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.FileReader;

public class SimpleV5 {
	public static final int READER_THREADS = 5;
	public static final int COUNTER_THREADS = 5;

	ConcurrentHashMap<String, AtomicInteger> wordCountMap = new ConcurrentHashMap<String, AtomicInteger>();

	ExecutorService fileReadersPool = Executors.newFixedThreadPool(READER_THREADS);
	ExecutorService countersPool = Executors.newFixedThreadPool(COUNTER_THREADS);

	LinkedBlockingQueue<String[]> wordsQueue = new LinkedBlockingQueue<String[]>();
	AtomicBoolean readingFinished = new AtomicBoolean(false);

	private void readDirectory(String path) throws IOException, InterruptedException {
		File file = new File(path);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File fileInDirectory : files) {
				if (!fileInDirectory.isHidden()) {
					readFile(fileInDirectory);
				}

			}
		} else {
			readFile(file);
		}
		createCounterThreads();
		fileReadersPool.shutdown();
		fileReadersPool.awaitTermination(60, TimeUnit.SECONDS);
		System.out.println("Reader threads done.");
		readingFinished.set(true);
		countersPool.shutdown();
		countersPool.awaitTermination(60, TimeUnit.SECONDS);
		System.out.println("Counter threads done.");
		wordCountMap.forEach((word, count) -> {
			System.out.println(String.format("Word:%s Count:%s", word, count));
		});
		System.out.println(String.format("There are %s unique words.", wordCountMap.size()));
	}

	private void readFile(final File file) {
		fileReadersPool.submit(new Runnable() {
			public void run() {
				try {
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String line = null;
					while ((line = reader.readLine()) != null) {
						String[] words = map(line);
						wordsQueue.add(words);
					}
					reader.close();
					System.out.println(String.format("Done reading %s", file));
				} catch (Exception e) {
					System.out.println(String.format("Error processing file", file.toString()));
					e.printStackTrace();
				}
			}
		});
	}

	private void createCounterThreads() {
		Runnable counterRunnable = new Runnable() {
			public void run() {
				try {
					while (!readingFinished.get() || !wordsQueue.isEmpty()) {
						String[] words = wordsQueue.poll(1, TimeUnit.SECONDS);
						if (words != null) {
							reduce(words);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		for (int i = 0; i < COUNTER_THREADS; i++) {
			countersPool.submit(counterRunnable);
		}
	}

	private String[] map(String line) {
		String lineString = line.substring(8);
		lineString = lineString.toLowerCase();
		return lineString.split("\\W");
	}

	private void reduce(String[] lineWords) {
		for (String word : lineWords) {
			AtomicInteger count = wordCountMap.putIfAbsent(word, new AtomicInteger(0));
			if (count == null) {
				count = wordCountMap.get(word);
			}
			count.incrementAndGet();
		}
	}

	public static void main(String[] args) {
		try {
			new SimpleV5().readDirectory(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
