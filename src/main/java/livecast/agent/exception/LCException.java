package livecast.agent.exception;

public class LCException extends RuntimeException {
    private ErrorCode code;

    public LCException(ErrorCode code, String message){
        super(message);

        this.code = code;
    }

    public ErrorCode getErrorCode() {
        return this.code;
    }
}
