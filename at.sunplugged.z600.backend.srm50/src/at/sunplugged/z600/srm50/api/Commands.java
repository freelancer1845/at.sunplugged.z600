package at.sunplugged.z600.srm50.api;

public class Commands {

    /** Calls the "READ" command witch returns the values of all channels. */
    public static final String MEASUREMENT = "READ";

    /** Returns the Current measurement settings. */
    public static final String TODO = "TODO";

    /** Sets the measurement method via append one of the "TODO_X" ids. */
    public static final String SET_TODO = "TODO,";

    /** Measurement off. */
    public static final String TODO_OFF = "0";

    /** Measurement Resistence (SRM). */
    public static final String TODO_SRM = "1";

    /** Measurement to Optical Density (OD). */
    public static final String TODO_OD = "2";

    /** Measurement to SRM and OD (CRT). */
    public static final String TODO_CRT = "3";

    /** Returns the active measurement channels. */
    public static final String ACTI = "ACTI";

    /** Sets active measurement channels appending the count. */
    public static final String ACTI_X = "ACTI,";

    /** Returns the current OD Measurement Scale. */
    public static final String MBOD = "MBOD";

    /** Sets the current OD Measurement Scale append one of the "MBOD_X" ids. */
    public static final String SET_MBOD = "MBOD,";

    /** OD Measurement Scale 0 - 0.25. */
    public static final String MBOD_ZERO_ZERO_DOT_TWO_FIVE = "0";

    /** OD Measurement Scale 2.5 - 4. */
    public static final String MBOD_TWO_POINT_FIVE_FOUR = "1";

    /** OD Measurement Scale Automatic. */
    public static final String MBOD_AUTOMATIC = "2";

    /** Returns measurement Unit. */
    public static final String UNIT = "UNIT";

    /** Sets the measurement Unit by appending one of the "UNIT_X" ids. */
    public static final String SET_UNIT = "UNIT,";

    /** Measurement Value Siemens. */
    public static final String UNIT_SIEMANS = "0";

    /** Measurement Value Ohm. */
    public static final String UNIT_OHM = "1";

    /** Returns the output format. */
    public static final String FORM = "FORM";

    /** Sets the output format by appending one of the "FORM_X" ids. */
    public static final String SET_FORM = "FOMR,x";

    /** Line format. */
    public static final String FORM_LINE = "0";

    /** Row format. */
    public static final String FORM_ROW = "0";

    /** Returns the current Setting for the distancencompensation. */
    public static final String COMP = "COMP";

    /** Activates distancecompensation. */
    public static final String COMP_ON = "COMP,1";

    /** Deactivates distancecompensation. */
    public static final String COMP_OFF = "COMP,0";

    /** Returns all the settings. */
    public static final String GTCF = "GTCF";

    /** Returns the value of the distance sensor in mm. */
    public static final String RDMM = "RDMM";

    /** By append the channel number, this sets that channel to zero. */
    public static final String ZERO_X = "ZERO,";

    /**
     * Calibrates the given channel x to a given value y. Append like that
     * (UCAL, + "1,8888.2").
     */
    public static final String UCAL_X_Y = "UCAL,";

    /** Resets calibration of the appended channel. */
    public static final String UCLR_X = "ULCR,";

    private Commands() {
    }
}
