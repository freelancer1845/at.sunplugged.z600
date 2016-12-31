package at.sunplugged.z600.core.machinestate.api.exceptions;

/**
 * Base class for exceptions thrown by parts of the machineStateService.
 * 
 * @author Jascha Riedel
 *
 */
public abstract class MachineStateException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -3237885702301917671L;

    public MachineStateException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public MachineStateException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
        super(arg0, arg1, arg2, arg3);
        // TODO Auto-generated constructor stub
    }

    public MachineStateException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    public MachineStateException(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public MachineStateException(Throwable arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

}
