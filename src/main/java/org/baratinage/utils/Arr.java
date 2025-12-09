package org.baratinage.utils;

import java.time.LocalDateTime;
import java.util.List;

public class Arr {

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

  public static double[] push(double[] array, double newValue) {
    double[] newArray = new double[array.length + 1];
    for (int k = 0; k < array.length; k++) {
      newArray[k] = array[k];
    }
    newArray[array.length] = newValue;
    return newArray;
  }

  public static LocalDateTime[] push(LocalDateTime[] array, LocalDateTime newValue) {
    LocalDateTime[] newArray = new LocalDateTime[array.length + 1];
    for (int k = 0; k < array.length; k++) {
      newArray[k] = array[k];
    }
    newArray[array.length] = newValue;
    return newArray;
  }

  public static boolean[] push(boolean[] array, boolean newValue) {
    boolean[] newArray = new boolean[array.length + 1];
    for (int k = 0; k < array.length; k++) {
      newArray[k] = array[k];
    }
    newArray[array.length] = newValue;
    return newArray;
  }

  public static double[] ones(int n) {
    double[] d = new double[n];
    for (int k = 0; k < n; k++) {
      d[k] = 1;
    }
    return d;
  }

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

  public static double[] toDouble(boolean[] src) {
    int n = src.length;
    double[] tgt = new double[n];
    for (int k = 0; k < n; k++) {
      tgt[k] = src[k] ? 1d : 0d;
    }
    return tgt;
  }

  public static boolean[] toBoolean(double[] src) {
    int n = src.length;
    boolean[] tgt = new boolean[n];
    for (int k = 0; k < n; k++) {
      tgt[k] = src[k] == 1d;
    }
    return tgt;
  }

  public static double[] toDouble(int[] src) {
    int n = src.length;
    double[] tgt = new double[n];
    for (int k = 0; k < n; k++) {
      tgt[k] = (double) src[k];
    }
    return tgt;
  }

  public static int[] toInt(double[] src) {
    int n = src.length;
    int[] tgt = new int[n];
    for (int k = 0; k < n; k++) {
      tgt[k] = ((Double) src[k]).intValue();
    }
    return tgt;
  }

}
