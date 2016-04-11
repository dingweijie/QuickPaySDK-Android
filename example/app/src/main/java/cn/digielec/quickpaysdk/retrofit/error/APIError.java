package cn.digielec.quickpaysdk.retrofit.error;

/**
 * Created by dw on 2016/4/1.
 */
public class APIError {
    private int statusCode;
    private String message;

    public APIError() {
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
