package utils;

public class Vector {
    public static String[] push(String[] vector, String element) {
        String[] newVector = new String[vector.length + 1];
        for (int k = 0; k < vector.length; k++) {
            newVector[k] = vector[k];
        }
        newVector[vector.length] = element;
        return newVector;
    }
}
