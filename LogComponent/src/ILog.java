public interface ILog {
    public void stopNow();
    public void stopAndWait();
    public void write(String message);
}
