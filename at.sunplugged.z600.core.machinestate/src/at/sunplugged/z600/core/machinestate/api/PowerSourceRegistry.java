package at.sunplugged.z600.core.machinestate.api;

/**
 * Interface for accessing the power sources.
 * 
 * 
 * @author Jascha Riedel
 *
 */
public interface PowerSourceRegistry {

    public enum PowerSourceId {
        SSV1, SSV2, MDX, PINNACLE;
    }

    /**
     * To access power sources.
     * 
     * @param id of the power source.
     * @return the {@linkplain PowerSource} corresponding to the id.
     */
    public PowerSource getPowerSource(PowerSourceId id);

}
