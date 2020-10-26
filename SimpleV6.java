import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.FileReader;

public class SimpleV7 {
	public static final int READER_THREADS = 5;
	public static final int COUNTER_THREADS = 5;

	ConcurrentHashMap<String, AtomicInteger> wordCountMap = new ConcurrentHashMap<String, AtomicInteger>();

	ExecutorService fileReadersPool = Executors.newFixedThreadPool(READER_THREADS);
	ExecutorService countersPool = Executors.newFixedThreadPool(COUNTER_THREADS);

	ArrayList<LinkedBlockingQueue<String>> wordsQueues = new ArrayList<LinkedBlockingQueue<String>>(COUNTER_THREADS);

	AtomicBoolean readingFinished = new AtomicBoolean(false);

	AtomicInteger finalCount = new AtomicInteger(0);

	private void readDirectory(String path) throws IOException, InterruptedException {
		for (int i = 0; i < COUNTER_THREADS; i++) {
			wordsQueues.add(i, new LinkedBlockingQueue<String>());
		}
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
		System.out.println(
				String.format("There are %s unique words and %s processed.", wordCountMap.size(), finalCount.get()));
	}

	private void readFile(final File file) {
		fileReadersPool.submit(new Runnable() {
			public void run() {
				try {
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String line = null;
					MessageDigest digest = MessageDigest.getInstance("MD5");
					while ((line = reader.readLine()) != null) {
						String[] words = map(line);
						for (String word : words) {
							byte[] md5Digest = digest.digest(word.getBytes());
							byte firstByte = md5Digest[0];
							int index = -1;
							if (firstByte < -77) {
								index = 0;
							} else if (firstByte < -26) {
								index = 1;
							} else if (firstByte < 25) {
								index = 2;
							} else if (firstByte < 76) {
								index = 3;
							} else if (firstByte <= 127) {
								index = 4;
							} else {
								index = 0;
							}
							wordsQueues.get(index).add(word);
						}
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
		for (int i = 0; i < COUNTER_THREADS; i++) {
			final int counterQueueIndex = i;
			countersPool.submit(new Runnable() {
				public void run() {
					int amountProcessed = 0;
					LinkedBlockingQueue<String> myQueue = wordsQueues.get(counterQueueIndex);
					try {
						while (!readingFinished.get() || !myQueue.isEmpty()) {
							String words = myQueue.poll(1, TimeUnit.SECONDS);
							if (words != null) {
								reduce(words);
								amountProcessed++;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.out.println(
							String.format("Counter thread %s - processed %s", counterQueueIndex, amountProcessed));
					finalCount.addAndGet(amountProcessed);
				}
			});
		}
	}

	private String[] map(String line) {
		String lineString = line.substring(8);
		lineString = lineString.toLowerCase();
		return lineString.split("\\W");
	}

	private void reduce(String word) {
		AtomicInteger count = wordCountMap.putIfAbsent(word, new AtomicInteger(0));
		if (count == null) {
			count = wordCountMap.get(word);
		}
		count.incrementAndGet();
	}

	public static void main(String[] args) {
		try {
			new SimpleV7().readDirectory(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
