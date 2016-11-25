package at.sunplugged.z600.main.controlling.variables;

public class SurfaceResistance extends AbstractMeasurementVariable {

    private final int channelNumber;

    public SurfaceResistance(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    @Override
    public double getCurrentValue() {
        return 0;
    }

}
