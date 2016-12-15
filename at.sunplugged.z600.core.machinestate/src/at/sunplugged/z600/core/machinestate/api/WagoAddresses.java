package at.sunplugged.z600.core.machinestate.api;

public class WagoAddresses {

    public static final int DIGITAL_OUTPUT_MAX_ADDRESS = 57;

    public static final int DIGITAL_INPUT_MAX_ADDRESS = 55;

    public static final int ANALOG_INPUT_MAX_ADDRESS = 11;

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
        OUTLET_NINE(35),
        WATER_TURBO_PUMP(31),
        WATER_KATH_ONE(32),
        WATER_KATH_TWO(33),
        WATER_KATH_THREE(34),
        WATERPUMP(37),
        WATER_SHIELD(36),
        PRE_PUMP_ONE(32),
        TUROBO_OUMP(34),
        PRE_PUMP_TWO(35);

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
        TURBO_PUMP_OK(14),
        TURBO_PUMP_HIGH_SPEED(15),
        PRE_PUMP_TWO_OK(16),
        WATER_KATH_ONE_ON(23),
        WATER_KATH_TWO_ON(24),
        WATER_KATH_THREE_ON(25),
        WATHER_KATH_FOUR_ON(26),
        LEFT_SPEED_TRIGGER(39),
        RIGHT_SPEED_TRIGGER(40);

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

        PREASURE_TURBO_PUMP(3), PREASURE_CRYO_ONE(5), PREASURE_CRYO_TWO(6), PREASURE_CHAMBER(9);

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
        KATHODE_ONE_SETPOINT(15395);

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
