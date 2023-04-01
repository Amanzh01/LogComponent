public class Application {
    public static void main(String [] args){
        ILog log = new LogComponent(".", "logs");
        int n = 1000;
        for(int i = 0; i < n;++i){
            log.write("message #" + Integer.toString(i + 1));
        }
        log.stopAndWait();
    }
}