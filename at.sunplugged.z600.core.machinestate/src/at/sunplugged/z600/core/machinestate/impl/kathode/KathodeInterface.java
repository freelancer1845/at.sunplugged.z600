package at.sunplugged.z600.core.machinestate.impl.kathode;

import java.io.IOException;

import at.sunplugged.z600.core.machinestate.api.exceptions.InvalidKathodeStateException;

public interface KathodeInterface {

    public void startKathode() throws InvalidKathodeStateException;

    public void stopKathode();

    public void setPowerSetpoint(Double power);

    public double getPowerSetpoint();

    public double getPowerAtKathode();

    public double getCurrentAtKathode();

    public double getVoltageAtKathode();

    public void tickPowerControl() throws IOException;

}
