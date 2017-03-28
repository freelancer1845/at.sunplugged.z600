package at.sunplugged.z600.common.settings.api;

/**
 * Holds the IDs of parameters of the machine.
 * 
 * @author Jascha Riedel
 *
 */
public class ParameterIds {

    /** SafetyProtcols Outlets. */
    public static final String SAFETY_PROTOCOLS_OUTLETS = "Safety_Protocols_Outlets";

    /** Initial desired pressure gas flow. */
    public static final String INITIAL_DESIRED_PRESSURE_GAS_FLOW = "Initial_Desired_Pressure_Gas_Flow";

    /** Initial gas flow parameter. */
    public static final String INITIAL_GAS_FLOW_PARAMETER = "Initial_Gas_Flow_Parameter";

    /** Gas flow control parameter. */
    public static final String GAS_FLOW_CONTROL_PARAMETER = "Gas_Flow_Control_Parameter";

    /** Gas flow hysteresis control parameter. */
    public static final String GAS_FLOW_HYSTERESIS_CONTROL_PARAMETER = "Gas_Flow_Hysteresis_Control_Parameter";

    /** Limit for starting the pressure control. */
    public static final String PRESSURE_CONTROL_LOWER_LIMIT = "Pressure_Control_Lower_Limit";

    /** Vacuum lower limit. */
    public static final String VACUUM_LOWER_LIMIT_MBAR = "Vacuum_Lower_Limit_mbar";

    /** Vacuum upper limit. */
    public static final String VACUUM_UPPER_LIMIT_MBAR = "Vacuum_Upper_Limit_mbar";

    /** Vacuum trigger tubopump. */
    public static final String START_TRIGGER_TURBO_PUMP = "Start_Trigger_Turbo_Pump";

    /** Cryo Pump Trigger Pressure. */
    public static final String CRYO_PUMP_PRESSURE_TRIGGER = "Cryo_Pump_Pressure_Trigger";

    /** Maximum Speed Setting. */
    public static final String ENGINE_MAXIMUM_SPEED = "Engine_Maximum_Speed";

    /** Lower Safety Limit PowerSource in kW. */
    public static final String LOWER_SAFETY_LIMIT_POWER_AT_POWER_SORUCE = "Lower_Safety_Limit_Power_At_Power_Source";

    /** Initial Power Pinnacle. */
    public static final String INITIAL_POWER_PINNACLE = "Initial_Current_Pinnacle";

    /** Max Power Pinnacle. */
    public static final String MAX_POWER = "Max_Power";

    /** Power Change per 0.1s pinnacle. */
    public static final String POWER_CHANGE_PINNACLE = "Power_Change_Pinnacle_per_0.1s";

    /** Initial Current SSV. */
    public static final String INITIAL_CURRENT_SSV = "Initial_Current_SSV";

    /** Current change per 0.1s SSV. */
    public static final String CURRENT_CHANGE_SSV = "Current_Change_SSV_per_0.1s";

}
