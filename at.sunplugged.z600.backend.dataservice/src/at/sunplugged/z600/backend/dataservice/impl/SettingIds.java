package at.sunplugged.z600.backend.dataservice.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class SettingIds {

    /** ID. */
    public final static String BELT_POSITION = "BeltPosition";

    /** ID. */
    public final static String BELT_CORRECTION_RUNTIME_LEFT = "BeltPositionHorizontalLeft";

    /** ID. */
    public final static String BELT_CORRECTION_RUNTIME_RIGHT = "BeltPositionHorizontalRight";

    public static List<String> getAllIds() {
        List<String> returnList = new ArrayList<>();
        Field[] fields = SettingIds.class.getFields();
        for (Field field : fields) {
            int modifier = field.getModifiers();
            if (Modifier.isPublic(modifier) && Modifier.isFinal(modifier) && Modifier.isStatic(modifier)) {
                if (field.getType().isAssignableFrom(String.class)) {
                    try {
                        returnList.add((String) field.get(null));
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return returnList;
    }

    private SettingIds() {

    }
}
