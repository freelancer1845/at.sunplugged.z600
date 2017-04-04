package at.sunplugged.z600.backend.dataservice.api;

/**
 * An Exception thrown by the {@linkplain DataService}.
 * 
 * @author Jascha Riedel
 *
 */
public class DataServiceException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -6752717347895916545L;

    public DataServiceException() {
        super();
    }

    public DataServiceException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
        super(arg0, arg1, arg2, arg3);
    }

    public DataServiceException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public DataServiceException(String arg0) {
        super(arg0);
    }

    public DataServiceException(Throwable arg0) {
        super(arg0);
    }

}
