import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// TODO: write UNIT tests
public class LogComponent implements ILog {

    private static final int MAX_QUEUE_SIZE = 1000;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String logDirectory;
    private final String logFileNamePrefix;
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
    private volatile boolean isRunning = true;
    private Thread writerThread;

    public LogComponent(String logDirectory, String logFileNamePrefix) {
        this.logDirectory = logDirectory;
        this.logFileNamePrefix = logFileNamePrefix;
        writerThread = new Thread(this::writeLogs);
        writerThread.start();
    }

    public void write(String message) {
        if (!isRunning) {
            return;
        }
        try {
            logQueue.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error writing log message: " + e.getMessage());
        }
    }

    public void stop() {
        isRunning = false;
    }

    public void stopAndWait() {
        try {
            isRunning = false;
            while(!logQueue.isEmpty()){
                // wait
            }
            writerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error stopping log component: " + e.getMessage());
        }
    }
    private void writeLogs() {
        LocalDateTime lastDate = null;
        FileOutputStream fos = null;
        FileChannel fileChannel = null;
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            while (isRunning) {
                String message = logQueue.take();
                if (fileChannel == null || lastDate == null || !lastDate.toLocalDate().isEqual(LocalDateTime.now().toLocalDate())) {
                    if (fileChannel != null) {
                        fileChannel.close();
                    }
                    lastDate = LocalDateTime.now();
                    String fileName = String.format("%s/%s_%s.log", logDirectory, logFileNamePrefix, DATE_TIME_FORMATTER.format(lastDate));
                    File file = new File(fileName);
                    fos = new FileOutputStream(file, true);
                    fileChannel = fos.getChannel();

                }
                try{
                    buffer.clear();
                    buffer.put((message + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        fileChannel.write(buffer);
                    }
                }
                catch (Exception ex){
                    System.err.println("Could not write from buffer: " + ex.getMessage());
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error writing log message: " + e.getMessage());
        } finally {
            if (fileChannel != null) {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    System.err.println("Error closing log file channel: " + e.getMessage());
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    System.err.println("Error closing log file output stream: " + e.getMessage());
                }
            }
        }
    }

}




