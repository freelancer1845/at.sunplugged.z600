package at.sunplugged.z600.core.machinestate.api;

public class WagoAddresses {

    public static final int DIGITAL_OUTPUT_MAX_ADDRESS = 57;

    public static final int DIGITAL_INPUT_MAX_ADDRESS = 55;

    public static final int ANALOG_INPUT_MAX_ADDRESS = 12;

    public static final int ANALOG_OUTPUT_MAX_ADDRESS = 6;

    /**
     * Keep in mind that Outlet Seven and Eight are not controlled by modbus!
     * 
     * @author Jascha Riedel
     *
     */
    public enum DigitalOutput {
        OUTLET_ONE(0),
        OUTLET_TWO(1),
        OUTLET_THREE(2),
        OUTLET_FOUR(5),
        OUTLET_FIVE(3),
        OUTLET_SIX(4),
        PRE_PUMP_ONE(24),
        PRE_PUMP_ROOTS(25),
        TUROBO_PUMP(26),
        PRE_PUMP_TWO(27),
        WATER_TURBO_PUMP(31),
        WATER_KATH_ONE(32),
        WATER_KATH_TWO(33),
        WATER_KATH_THREE(34),
        OUTLET_NINE(35),
        WATER_SHIELD(36),
        WATERPUMP(37),
        SSV_ONE_START(39),
        SSV_TWO_START(40),
        PINNACLE_REG_ONE(41),
        PINNACLE_REG_TWO(42),
        PINNACLE_START(43),
        PINNACLE_INTERLOCK(44),
        MDX_START(45),
        MDX_OFF(46),
        MDX_INTERLOCK(47),
        PINNACLE_OFF(49);

        private final int address;

        private DigitalOutput(int address) {
            this.address = address;

        }

        public int getAddress() {
            return this.address;
        }

        /** Returns null if there is no output registered under that address. */
        public static DigitalOutput getByAddress(int address) {
            DigitalOutput[] values = DigitalOutput.values();

            for (int i = 0; i < values.length; i++) {
                if (values[i].getAddress() == address) {
                    return values[i];
                }
            }
            return null;
        }

    }

    public enum DigitalInput {

        OUTLET_ONE_CLOSED(0),
        OUTLET_TWO_OPEN(1),
        OUTLET_TWO_CLOSED(2),
        OUTLET_THREE_OPEN(3),
        PRE_PUMP_ONE_OK(12),
        PRE_PUMP_ROOTS_OK(13),
        TURBO_PUMP_OK(14),
        TURBO_PUMP_HIGH_SPEED(15),
        PRE_PUMP_TWO_OK(16),
        WATER_KATH_ONE_ON(23),
        WATER_KATH_TWO_ON(24),
        WATER_KATH_THREE_ON(25),
        WATER_KATH_FOUR_ON(26),
        PINNACLE_OUT(34),
        P_120_MBAR(38),
        LEFT_SPEED_TRIGGER(39),
        RIGHT_SPEED_TRIGGER(40),
        COOLING_PUMP_OK(41),
        SSV_TWO_ERROR(42),
        SSV_ONE_ERROR(43);

        private final int address;

        private DigitalInput(int address) {
            this.address = address;
        }

        public int getAddress() {
            return address;
        }

        /**
         * Returns null if there is no DigitalInput registered under that
         * address.
         */
        public static DigitalInput getByAddress(int address) {
            DigitalInput[] digitalInputs = DigitalInput.values();
            for (int i = 0; i < digitalInputs.length; i++) {
                if (digitalInputs[i].getAddress() == address) {
                    return digitalInputs[i];
                }
            }

            return null;
        }

    }

    public enum AnalogInput {

        VOLTAGE_KATHODE_TWO(0),
        CURRENT_KATHODE_TWO(1),
        GAS_FLOW(3),
        PREASURE_TURBO_PUMP(4),
        CONVEYOR_POSITION_L_R(5),
        PREASURE_CRYO_ONE(6),
        PREASURE_CRYO_TWO(7),
        VOLTAGE_KATHODE_ONE(8),
        POWER_KATHODE_ONE(9),
        PREASURE_CHAMBER(10),
        VOLTAGE_KATHODE_THREE(11),
        CURRENT_KATHODE_THREE(12);

        private final int address;

        private AnalogInput(int address) {
            this.address = address;
        }

        public int getAddress() {
            return address;
        }

        public static AnalogInput getByAddress(int address) {
            AnalogInput[] analogInputs = AnalogInput.values();
            for (int i = 0; i < analogInputs.length; i++) {
                if (analogInputs[i].getAddress() == address) {
                    return analogInputs[i];
                }
            }
            return null;
        }

    }

    public enum AnalogOutput {
        GAS_FLOW_SETPOINT(2), KATHODE_TWO_SETPOINT(3), KATHODE_THREE_SETPOINT(4), KATHODE_ONE_SETPOINT(5);

        private final int address;

        private AnalogOutput(int address) {
            this.address = address;
        }

        public int getAddress() {
            return address;
        }

        public static AnalogOutput getByAddress(int address) {
            AnalogOutput[] analogOutputs = AnalogOutput.values();
            for (int i = 0; i < analogOutputs.length; i++) {
                if (analogOutputs[i].getAddress() == address) {
                    return analogOutputs[i];
                }
            }
            return null;
        }
    }

    private WagoAddresses() {
    }

}
