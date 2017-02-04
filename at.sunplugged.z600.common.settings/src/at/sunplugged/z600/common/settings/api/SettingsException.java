package at.sunplugged.z600.common.settings.api;

public class SettingsException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 3257601473135946760L;

    public SettingsException() {
        super();
    }

    public SettingsException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
        super(arg0, arg1, arg2, arg3);
    }

    public SettingsException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public SettingsException(String arg0) {
        super(arg0);
    }

    public SettingsException(Throwable arg0) {
        super(arg0);
    }

}
