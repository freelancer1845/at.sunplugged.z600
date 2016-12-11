package at.sunplugged.z600.mbt.impl;

import java.util.ArrayList;

public class ShiftedArrayList<T> extends ArrayList<T> {

    /**
     * 
     */
    private static final long serialVersionUID = -1996516345598873598L;

    private final int startAddress;

    public ShiftedArrayList(int startAddress) {
        this.startAddress = startAddress;
    }

    @Override
    public void add(int index, T element) {
        checkIndex(index);
        super.add(index - startAddress, element);
    }

    @Override
    public T get(int index) {
        checkIndex(index);
        return super.get(index - startAddress);
    }

    @Override
    public int indexOf(Object o) {

        return super.indexOf(o) + startAddress;
    }

    @Override
    public int lastIndexOf(Object o) {
        // TODO Auto-generated method stub
        return super.lastIndexOf(o) + startAddress;
    }

    private void checkIndex(int index) {
        if (index - startAddress < 0) {
            throw new IndexOutOfBoundsException(
                    "Array List is shifted by: " + startAddress + ". Tried to access " + index);
        }
    }

}
