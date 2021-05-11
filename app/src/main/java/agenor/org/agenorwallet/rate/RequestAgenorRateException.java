package agenor.org.agenorwallet.rate;

/**
 * Created by furszy on 7/5/17.
 */
public class RequestAgenorRateException extends Exception {
    public RequestAgenorRateException(String message) {
        super(message);
    }

    public RequestAgenorRateException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestAgenorRateException(Exception e) {
        super(e);
    }
}
