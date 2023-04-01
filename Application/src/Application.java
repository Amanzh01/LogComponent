// TODO: write unit the cases in unit test format
public class Application {
    public static void main(String [] args) throws InterruptedException {
        ILog log1 = new LogComponent(".", "logs");
        int n = 1000;
        for(int i = 0; i < n;++i){
            log1.write("message #" + Integer.toString(i + 1));
        }
        log1.stopNow();



        ILog log2 = new LogComponent(".", "logs2");
        for(int i = 0; i < n;++i){
            log2.write("message #" + Integer.toString(i + 1));
        }
        log2.stopAndWait();
    }
}