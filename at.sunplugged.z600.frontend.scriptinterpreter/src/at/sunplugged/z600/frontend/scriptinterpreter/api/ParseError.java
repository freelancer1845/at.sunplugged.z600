package at.sunplugged.z600.frontend.scriptinterpreter.api;

public class ParseError extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1978641387310843832L;

    private final int line;

    public ParseError(String error, int line) {
        super(error);
        this.line = line;
    }

    public ParseError(int line, Throwable previousError) {
        super(previousError.getMessage(), previousError);
        this.line = line;
    }

    public ParseError(String error) {
        super(error);
        this.line = -1;
    }

    public int getLine() {
        return line;
    }

}
