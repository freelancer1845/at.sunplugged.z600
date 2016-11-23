package at.sunplugged.z600.utils;

/**
 * Helper class for typical conversions.
 * 
 * @author Jascha Riedel
 *
 */
public class Conversion {

    public static double radianToDegree(double radianValue) {
        return radianValue * 180 / Math.PI;
    }

    private Conversion() {
    }

}
