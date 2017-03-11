package at.sunplugged.z600.common.utils;

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

    public static double clipConversionOut(double value, double min, double max) {
        if (value < min) {
            value = min;
        }
        if (value > max) {
            value = max;
        }
        return (value * (4095) / (max - min) + min);
    }

    public static double clipConversionIn(double value, double min, double max) {
        return (value * (max - min) / 4095) + min;
    }

    private Conversion() {
    }

}
