package ch.minifig.monitoring.jmx;

public class JmxException extends RuntimeException {
    public JmxException(String message) {
        super(message);
    }

    public JmxException(String message, Throwable cause) {
        super(message, cause);
    }
}
