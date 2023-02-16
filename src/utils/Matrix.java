package utils;

// import java.lang.reflect.Array;
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.List;

public class Matrix {
    private boolean storeAsColumn;
    private double[][] rows;
    private double[][] columns;

    public Matrix() {
        this.rows = new double[0][0];
        this.columns = new double[0][0];
        this.storeAsColumn = false;
    }

    public Matrix(boolean providedAsColumn, boolean storeAsColumn, double[]... columnsOrRows) {
        this.storeAsColumn = storeAsColumn;
        if (providedAsColumn && storeAsColumn) {
            this.columns = columnsOrRows;
        } else if (providedAsColumn && !storeAsColumn) {
            this.rows = Matrix.transpose(columnsOrRows);
        } else if (!providedAsColumn && storeAsColumn) {
            this.columns = Matrix.transpose(columnsOrRows);
        }

    }

    public static double[][] transpose(double[][] matrix) {
        int n = matrix.length;
        if (n == 0)
            return matrix;
        int m = matrix[0].length;
        double[][] tMatrix = new double[m][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                tMatrix[j][i] = matrix[i][j];
            }
        }
        return tMatrix;
    }

    public static double[][] concat(double[]... vectors) {
        if (vectors.length > 0) {
            int n = vectors[0].length;
            for (double[] v : vectors) {
                if (n != v.length) {
                    String msg = String.format("Vectors have different number of elements: %d != %d",
                            n, v.length);
                    System.out.println(msg);
                }
            }
        }
        return vectors;
    }

    public static double[][] concat(double[][] matrix, double[]... vectors) {
        int nVectInMatrix = matrix.length;
        int nVectInVectors = vectors.length;
        int nElement;
        int nElementMax;
        if (nVectInMatrix > 0) {
            nElement = matrix[0].length;
        } else {
            if (nVectInVectors > 0) {
                nElement = vectors[0].length;
            } else {
                return new double[0][0];
            }
        }
        nElementMax = nElement;
        for (double[] v : matrix) {
            if (nElement != v.length) {
                String msg = String.format("The input matrix has vectors with different number of elements: %d != %d",
                        nElement, v.length);
                System.out.println(msg);
                if (v.length > nElementMax) {
                    nElementMax = v.length;
                }
            }
        }
        for (double[] v : vectors) {
            if (nElement != v.length) {
                String msg = String.format("The input vectors have different number of elements: %d != %d",
                        nElement, v.length);
                System.out.println(msg);
                if (v.length > nElementMax) {
                    nElementMax = v.length;
                }
            }
        }
        double[][] newMatrix = new double[nVectInMatrix + nVectInVectors][nElementMax];
        for (int k = 0; k < nVectInMatrix; k++) {
            newMatrix[k] = matrix[k];
        }
        for (int k = 0; k < nVectInVectors; k++) {
            newMatrix[k + nVectInMatrix] = vectors[k];
        }
        return newMatrix;
    }

    // source: https://stackoverflow.com/a/69157437
    // public static <T> T[] concat(T[] array1, T[] array2) {
    // List<T> resultList = new ArrayList<>(array1.length + array2.length);
    // Collections.addAll(resultList, array1);
    // Collections.addAll(resultList, array2);
    // @SuppressWarnings("unchecked") // the type cast is safe as the array1 has the
    // type T[]
    // T[] resultArray = (T[])
    // Array.newInstance(array1.getClass().getComponentType(), 0);
    // return resultList.toArray(resultArray);
    // }

    // public static String[] concat(String[] vectorA, String[] vectorB) {
    // if (vectorA == null && vectorB == null)
    // return null;
    // if (vectorA == null)
    // return vectorB;
    // if (vectorB == null)
    // return vectorA;
    // int nA = vectorA.length;
    // int nB = vectorA.length;
    // if (nA == 0)
    // return vectorB;
    // if (nB == 0)
    // return vectorA;
    // String[] result = new String[nA + nB];
    // for (int k = 0; k < nA; k++) {
    // result[k] = vectorA[k];
    // }
    // for (int k = 0; k < nA; k++) {
    // result[k + nA] = vectorB[k];
    // }
    // return result;
    // }

    // public static <T> T[] concat(T[] vectorA, T[] vectorB) {
    // if (vectorA == null && vectorB == null)
    // return null;
    // if (vectorA == null)
    // return vectorB;
    // if (vectorB == null)
    // return vectorA;
    // int nA = vectorA.length;
    // int nB = vectorA.length;
    // if (nA == 0)
    // return vectorB;
    // if (nB == 0)
    // return vectorA;
    // if (T == String) {

    // }
    // T[] result;
    // // T[] result = new T[nA + nB];
    // // for (int k = 0; k < nA; k++) {
    // // result[k] = vectorA[k];
    // // }
    // // for (int k = 0; k < nA; k++) {
    // // result[k + nA] = vectorB[k];
    // // }
    // return result;
    // }

    public static void prettyPrint(double[][] matrix) {
        int nVect = matrix.length;
        if (nVect == 0) {
            System.out.println("\nmatrix | 0 x 0 | empty matrix\n");
        }
        int[] nElements = new int[nVect];
        int nElement = matrix[0].length;
        int nElementMax = nElement;
        for (int k = 0; k < nVect; k++) {
            if (nElement != matrix[k].length) {
                nElement = -1;
                if (matrix[k].length > nElementMax) {
                    nElementMax = matrix[k].length;
                }
            }

            nElements[k] = matrix[k].length;
        }
        System.out.printf("\nmatrix | %d x ", nVect);
        if (nElement == -1) {
            System.out.print("[");
            for (int i : nElements) {
                System.out.printf("%d, ", i);

            }
            System.out.print("]\n\n");
        } else {
            System.out.printf("%d\n\n", nElement);
        }
        for (int j = 0; j < nVect; j++) {
            String s = String.format("Vector_%d", j);
            System.out.printf("%15s", s);
            if (j >= 5) {
                System.out.print(" ...");
                break;
            }
        }
        System.out.print("\n");
        for (int i = 0; i < nElementMax; i++) {
            for (int j = 0; j < nVect; j++) {
                if (matrix[j].length > i) {
                    // System.out.print(String.format("%f", matrix[j][i]));
                    System.out.printf("%15f", matrix[j][i]);
                } else {
                    System.out.printf("%15s", ".....");
                }
                if (j >= 5) {
                    System.out.print(" ...");
                    break;
                }
            }

            System.out.print("\n");
            if (i >= 5) {
                System.out.printf("%15s\n", "...");
                break;
            }
        }
        System.out.print("\n");
    }

    public int nrow() {
        if (this.storeAsColumn) {
            if (this.columns.length > 0) {
                return this.columns[0].length;
            }
            return 0;
        } else {
            return this.rows.length;
        }
    }

    public int ncol() {
        if (this.storeAsColumn) {
            return this.columns.length;
        } else {
            if (this.rows.length > 0) {
                return this.rows[0].length;
            }
            return 0;
        }
    }

    public double getValue(int rowIndex, int colIndex) {
        if (this.storeAsColumn) {
            return this.columns[colIndex][rowIndex];
        } else {
            return this.rows[rowIndex][colIndex];
        }
    }

    public double[] getColumn(int colIndex) {
        if (this.storeAsColumn) {
            return this.columns[colIndex];
        } else {
            if (this.rows.length == 0) {
                return new double[0];
            }
            double[] column = new double[this.rows[0].length];
            for (int k = 0; k < this.rows.length; k++) {
                column[k] = this.rows[k][colIndex];
            }
            return column;
        }
    }

    public double[] getRow(int rowIndex) {
        if (!this.storeAsColumn) {
            return this.rows[rowIndex];
        } else {
            if (this.columns.length == 0) {
                return new double[0];
            }
            double[] row = new double[this.columns[0].length];
            for (int k = 0; k < this.columns.length; k++) {
                row[k] = this.columns[k][rowIndex];
            }
            return row;
        }
    }

    public double[][] get(boolean asColumn) {
        if (this.storeAsColumn && asColumn) {
            return this.columns;
        } else if (this.storeAsColumn && !asColumn) {
            return Matrix.transpose(this.columns);
        } else if (!this.storeAsColumn && asColumn) {
            return Matrix.transpose(this.rows);
        } else if (!this.storeAsColumn && !asColumn) {
            return this.rows;
        }
        return this.columns;
    }

}
