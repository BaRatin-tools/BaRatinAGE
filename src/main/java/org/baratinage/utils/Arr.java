package org.baratinage.utils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lightweight helpers for manipulating primitive arrays and simple collections.
 */
public class Arr {

  /** Remove elements at the given indices from a double array. */
  public static double[] removeElements(double[] array, List<Integer> indicesToRemove) {
    double[] newArray = new double[array.length - indicesToRemove.size()];
    int newIndex = 0;
    for (int i = 0; i < array.length; i++) {
      if (!indicesToRemove.contains(i)) {
        newArray[newIndex++] = array[i];
      }
    }
    return newArray;
  }

  /** Remove elements at the given indices from a LocalDateTime array. */
  public static LocalDateTime[] removeElements(LocalDateTime[] array, List<Integer> indicesToRemove) {
    LocalDateTime[] newArray = new LocalDateTime[array.length - indicesToRemove.size()];
    int newIndex = 0;
    for (int i = 0; i < array.length; i++) {
      if (!indicesToRemove.contains(i)) {
        newArray[newIndex++] = array[i];
      }
    }
    return newArray;
  }

  /** Remove elements at the given indices from a boolean array. */
  public static boolean[] removeElements(boolean[] array, List<Integer> indicesToRemove) {
    boolean[] newArray = new boolean[array.length - indicesToRemove.size()];
    int newIndex = 0;
    for (int i = 0; i < array.length; i++) {
      if (!indicesToRemove.contains(i)) {
        newArray[newIndex++] = array[i];
      }
    }
    return newArray;
  }

  /** Append a new value to the end of a double array. */
  public static double[] push(double[] array, double newValue) {
    double[] newArray = new double[array.length + 1];
    for (int k = 0; k < array.length; k++) {
      newArray[k] = array[k];
    }
    newArray[array.length] = newValue;
    return newArray;
  }

  /** Append a new LocalDateTime to the end of the array. */
  public static LocalDateTime[] push(LocalDateTime[] array, LocalDateTime newValue) {
    LocalDateTime[] newArray = new LocalDateTime[array.length + 1];
    for (int k = 0; k < array.length; k++) {
      newArray[k] = array[k];
    }
    newArray[array.length] = newValue;
    return newArray;
  }

  /** Append a new boolean to the end of the array. */
  public static boolean[] push(boolean[] array, boolean newValue) {
    boolean[] newArray = new boolean[array.length + 1];
    for (int k = 0; k < array.length; k++) {
      newArray[k] = array[k];
    }
    newArray[array.length] = newValue;
    return newArray;
  }

  /** Create a double array of length n filled with defaultValue. */
  public static double[] makeDoubleArray(int n, double defaultValue) {
    double[] d = new double[n];
    for (int k = 0; k < n; k++) {
      d[k] = defaultValue;
    }
    return d;
  }

  /** Reorder an array using a given index mapping. */
  public static double[] reorderArray(Integer[] indices, double[] array) {
    if (array.length != indices.length) {
      ConsoleLogger.error("'array' and 'indices' must have the same length!");
      return null;
    }
    int n = array.length;
    double[] reordered = new double[n];
    for (int k = 0; k < n; k++) {
      reordered[k] = array[indices[k]];
    }
    return reordered;
  }

  /**
   * Create a linearly spaced array between low and high (inclusive) with n
   * points.
   */
  public static double[] makeArray(double low, double high, int n) {
    double step = (high - low) / ((double) n - 1);
    double[] grid = new double[n];
    for (int k = 0; k < n; k++) {
      grid[k] = low + step * k;
    }
    return grid;
  }

  /** Create a grid from low to high with a fixed step size. */
  public static double[] makeArray(double low, double high, double step) {
    int n = (int) ((high - low) / step + 1);
    double[] grid = new double[n];
    for (int k = 0; k < n; k++) {
      grid[k] = low + step * k;
    }
    return grid;
  }

  /** Convert a boolean array to double[] (true->1.0, false->0.0). */
  public static double[] toDouble(boolean[] src) {
    int n = src.length;
    double[] tgt = new double[n];
    for (int k = 0; k < n; k++) {
      tgt[k] = src[k] ? 1d : 0d;
    }
    return tgt;
  }

  /** Convert a double array to boolean[] (1.0 as true, otherwise false). */
  public static boolean[] toBoolean(double[] src) {
    int n = src.length;
    boolean[] tgt = new boolean[n];
    for (int k = 0; k < n; k++) {
      tgt[k] = src[k] == 1d;
    }
    return tgt;
  }

  /** Convert an int array to double[]. */
  public static double[] toDouble(int[] src) {
    int n = src.length;
    double[] tgt = new double[n];
    for (int k = 0; k < n; k++) {
      tgt[k] = (double) src[k];
    }
    return tgt;
  }

  /** Convert a double[] to int[] by truncating decimals. */
  public static int[] toInt(double[] src) {
    int n = src.length;
    int[] tgt = new int[n];
    for (int k = 0; k < n; k++) {
      tgt[k] = ((Double) src[k]).intValue();
    }
    return tgt;
  }

}
