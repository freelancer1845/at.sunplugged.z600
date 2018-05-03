package at.sunplugged.z600.backend.dataservice.impl.model;

public class Z600Setting {

    private String key;

    private String value;

    public Z600Setting() {

    }

    public Z600Setting(String key, String value) {
        super();
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
