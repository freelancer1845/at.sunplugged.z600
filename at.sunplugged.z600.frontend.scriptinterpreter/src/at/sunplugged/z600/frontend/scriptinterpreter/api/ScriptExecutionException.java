package at.sunplugged.z600.frontend.scriptinterpreter.api;

public class ScriptExecutionException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 8229922046922455847L;

    public ScriptExecutionException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public ScriptExecutionException(String arg0) {
        super(arg0);
    }

}
