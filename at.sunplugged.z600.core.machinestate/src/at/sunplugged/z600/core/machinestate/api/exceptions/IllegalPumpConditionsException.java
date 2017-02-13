package at.sunplugged.z600.core.machinestate.api.exceptions;

/**
 * Exception thrown if a pump is started or stopped at an illegal time.
 * 
 * @author Jascha Riedel
 *
 */
public class IllegalPumpConditionsException extends IllegalStateException {

    /**
     * 
     */
    private static final long serialVersionUID = -370509502879492546L;

    public IllegalPumpConditionsException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public IllegalPumpConditionsException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    public IllegalPumpConditionsException(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public IllegalPumpConditionsException(Throwable arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

}
