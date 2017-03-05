package at.sunplugged.z600.common.settings.impl.defaultproperties;

import java.util.Properties;

import at.sunplugged.z600.common.settings.api.NetworkComIds;

public class NetworkComDefaultProperties extends Properties {

    /**
     * 
     */
    private static final long serialVersionUID = 860554967772843909L;

    public NetworkComDefaultProperties() {
        writeDefaultProperties();
    }

    private void writeDefaultProperties() {
        this.put(NetworkComIds.MBT_CONTROLLER_IP, "localhost");
        this.put(NetworkComIds.SQL_CONNECTION_STRING,
                "10.0.0.1\\SQLEXPRESS; Integrated Security=false;Initial Catalog=Z600_Datenerfassung");
        this.put(NetworkComIds.SQL_USERNAME, "Z600");
        this.put(NetworkComIds.SQL_PASSWORD, "alwhrh29035uafpue9ru3AWU");
        this.put(NetworkComIds.LEFT_ENGINE_COM_PORT, "COM6");
        this.put(NetworkComIds.RIGHT_ENGINE_COM_PORT, "COM5");
        this.put(NetworkComIds.VAT_SEVEN_COM_PORT, "COM3");
        this.put(NetworkComIds.VAT_EIGHT_COM_PORT, "COM4");
    }

}
