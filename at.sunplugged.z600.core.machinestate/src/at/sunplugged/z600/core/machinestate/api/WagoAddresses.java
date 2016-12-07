package at.sunplugged.z600.core.machinestate.api;

public class WagoAddresses {

    public enum Kind {
        OUTLET, PUMP, WATER_COOLING;
    }

    /**
     * Keep in mind that Outlet Seven and Eight are not controlled by modbus!
     * 
     * @author Jascha Riedel
     *
     */
    public enum DigitalOutput {
        OUTLET_ONE(0, Kind.OUTLET),
        OUTLET_TWO(1, Kind.OUTLET),
        OUTLET_THREE(2, Kind.OUTLET),
        OUTLET_FOUR(5, Kind.OUTLET),
        OUTLET_SIX(3, Kind.OUTLET),
        OUTLET_SEVEN(4, Kind.OUTLET),
        OUTLET_NINE(35, Kind.OUTLET),
        WATER_TURBO_PUMP(31, Kind.WATER_COOLING),
        WATER_KATH_ONE(32, Kind.WATER_COOLING),
        WATER_KATH_TWO(33, Kind.WATER_COOLING),
        WATER_KATH_THREE(34, Kind.WATER_COOLING),
        WATERPUMP(37, Kind.WATER_COOLING),
        WATER_SHIELD(36, Kind.WATER_COOLING),
        PRE_PUMP_ONE(32, Kind.PUMP),
        TUROBO_OUMP(34, Kind.PUMP),
        PRE_PUMP_TWO(35, Kind.PUMP);

        private final int address;

        private final Kind kind;

        private DigitalOutput(int address, Kind kind) {
            this.address = address;
            this.kind = kind;

        }

        public int getAddress() {
            return this.address;
        }

        public Kind getKind() {
            return this.kind;
        }
    }

    public enum DigitalInput {

        OUTLET_ONE_CLOSED(0, Kind.OUTLET),
        OUTLET_TWO_OPEN(1, Kind.OUTLET),
        OUTLET_TWO_CLOSED(2, Kind.OUTLET),
        OUTLET_THREE_OPEN(3, Kind.OUTLET),
        PRE_PUMP_ONE_OK(12, Kind.PUMP),
        TURBO_PUMP_OK(14, Kind.PUMP),
        TURBO_PUMP_HIGH_SPEED(15, Kind.PUMP),
        PRE_PUMP_TWO_OK(16, Kind.PUMP),
        WATER_KATH_ONE_ON(23, Kind.WATER_COOLING),
        WATER_KATH_TWO_ON(24, Kind.WATER_COOLING),
        WATER_KATH_THREE_ON(25, Kind.WATER_COOLING),
        WATHER_KATH_FOUR_ON(26, Kind.WATER_COOLING);

        private final int address;

        private final Kind kind;

        private DigitalInput(int address, Kind kind) {
            this.address = address;
            this.kind = kind;
        }

        public int getAddress() {
            return address;
        }

        public Kind getKind() {
            return kind;
        }

    }

    private WagoAddresses() {
    }

}
