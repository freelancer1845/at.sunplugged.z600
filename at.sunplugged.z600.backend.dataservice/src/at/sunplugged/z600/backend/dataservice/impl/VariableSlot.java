package at.sunplugged.z600.backend.dataservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import at.sunplugged.z600.backend.dataservice.api.DataServiceException;

/**
 * A slot for a variable of the given type.
 * 
 * @author Jascha Riedel
 *
 * @param <T>
 */
public final class VariableSlot<T> {

    /** Maximum size of one slot. */
    public static final int MAXIMUM_ELEMENTS = 10000;

    /** Name of the variable. */
    private final String variableName;

    /** The underlying HashMap. */
    private final Map<Date, T> hashMap;

    public VariableSlot(String variableName) {
        this.variableName = variableName;
        this.hashMap = new LinkedHashMap<>();

    }

    @SuppressWarnings("unchecked")
    public void addData(Date date, Object data) throws DataServiceException {
        if (hashMap.size() == MAXIMUM_ELEMENTS) {
            hashMap.remove(hashMap.entrySet().iterator().next().getKey());
        }
        hashMap.put(date, (T) data);
        if (hashMap.size() == MAXIMUM_ELEMENTS) {
            throw new DataServiceException("Maximum Elements reached for \"" + variableName
                    + "\"! Either clear the whole slot or following data will replace oldest.");
        }
    }

    public List<T> getData() {
        List<T> returnList = new ArrayList<>();
        for (T element : hashMap.values()) {
            returnList.add(element);
        }
        return returnList;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((variableName == null) ? 0 : variableName.hashCode());
        return result;
    }

    public void clearData() {
        hashMap.clear();
    }

}
