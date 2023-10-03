package org.baratinage.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONcomparator {

    private static final String depthStr = ">  ";

    private static List<String> logs = new ArrayList<>();

    public static boolean areMatchingIncluding(String jsonAstr, String jsonBstr, String... keysToInclude) {
        return areMatchingIncluding(new JSONObject(jsonAstr), new JSONObject(jsonBstr), keysToInclude);
    }

    public static boolean areMatchingIncluding(String jsonAstr, JSONObject jsonB, String... keysToInclude) {
        return areMatchingIncluding(new JSONObject(jsonAstr), jsonB, keysToInclude);
    }

    public static boolean areMatchingIncluding(JSONObject jsonA, String jsonBstr, String... keysToInclude) {
        return areMatchingIncluding(jsonA, new JSONObject(jsonBstr), keysToInclude);
    }

    public static boolean areMatchingIncluding(JSONObject jsonA, JSONObject jsonB, String... keysToInclude) {

        JSONObject filteredJsonA = new JSONObject();
        JSONObject filteredJsonB = new JSONObject();
        for (String key : keysToInclude) {
            if (jsonA.has(key)) {
                filteredJsonA.put(key, jsonA.get(key));
            }
            if (jsonB.has(key)) {
                filteredJsonB.put(key, jsonB.get(key));
            }
        }
        jsonA = filteredJsonA;
        jsonB = filteredJsonB;

        return areMatching(jsonA, jsonB);
    }

    public static boolean areMatchingExcluding(String jsonAstr, String jsonBstr, String... keysToExclude) {
        return areMatchingExcluding(new JSONObject(jsonAstr), new JSONObject(jsonBstr), keysToExclude);
    }

    public static boolean areMatchingExcluding(String jsonAstr, JSONObject jsonB, String... keysToExclude) {
        return areMatchingExcluding(new JSONObject(jsonAstr), jsonB, keysToExclude);
    }

    public static boolean areMatchingExcluding(JSONObject jsonA, String jsonBstr, String... keysToExclude) {
        return areMatchingExcluding(jsonA, new JSONObject(jsonBstr), keysToExclude);
    }

    public static boolean areMatchingExcluding(JSONObject jsonA, JSONObject jsonB, String... keysToExclude) {
        // create shallow copies (see: https://stackoverflow.com/a/12809884)
        jsonA = new JSONObject(jsonA, JSONObject.getNames(jsonA));
        jsonB = new JSONObject(jsonB, JSONObject.getNames(jsonB));
        for (String key : keysToExclude) {
            if (jsonA.has(key)) {
                jsonA.remove(key);
            }
            if (jsonB.has(key)) {
                jsonB.remove(key);
            }
        }
        return areMatching(jsonA, jsonB);
    }

    public static boolean areMatching(JSONObject a, JSONObject b) {
        logs.clear();
        boolean matching = areMatching(a, b, 0);
        if (!matching) {
            Collections.reverse(logs);
            for (String s : logs) {
                System.out.println(s);
            }
            printJsonStrings(a, b, 0);
        }
        return matching;
    }

    private static boolean areMatching(JSONObject a, JSONObject b, int depth) {

        Set<String> keysA = a.keySet();
        Set<String> keysB = b.keySet();

        if (!keysA.containsAll(keysB) || !keysB.containsAll(keysA)) {
            logs.add(depthStr.repeat(depth) + "NOT MATCHING > keys not matching");
            return false;
        }

        for (String key : keysA) {
            // NULL HANDLING
            if (a.isNull(key) && !b.isNull(key)) {
                logs.add(depthStr.repeat(depth) + "NOT MATCHING > KEY = '" + key
                        + "' > A is null while B is not for key.");
                return false;
            } else if (!a.isNull(key) && b.isNull(key)) {
                logs.add(depthStr.repeat(depth) + "NOT MATCHING > KEY = '" + key + "' > A is not null while B is.");
                return false;
            } else if (a.isNull(key) && b.isNull(key)) {
                continue;
            }

            Object oA = a.opt(key);
            Object oB = b.opt(key);
            boolean matching = areJSONObjectsMatching(oA, oB, depth + 1);
            if (!matching) {
                logs.add(depthStr.repeat(depth) + "NOT MATCHING > KEY = '" + key
                        + "' > The two JSONObject are not matching.");
                return false;
            }
        }
        return true;
    }

    private static boolean areJSONArrayMatching(JSONArray a, JSONArray b, int depth) {
        int n = a.length();
        int m = b.length();
        if (n != m) {
            logs.add(depthStr.repeat(depth) + "NOT MATCHING > A and B aredepthStr JSONArray with different length.");
            return false;
        }
        for (int k = 0; k < n; k++) {

            // NULL HANDLING
            if (a.isNull(k) && !b.isNull(k)) {
                logs.add(depthStr.repeat(depth) + "NOT MATCHING > INDEX = '" + k + "' > A is null while B is not.");
                return false;
            } else if (!a.isNull(k) && b.isNull(k)) {
                logs.add(depthStr.repeat(depth) + "NOT MATCHING > INDEX = '" + k + "' > A is not null while B is.");
                return false;
            } else if (a.isNull(k) && b.isNull(k)) {
                continue;
            }

            boolean matching = areJSONObjectsMatching(a.opt(k), b.opt(k), depth + 1);
            if (!matching) {
                logs.add(depthStr.repeat(depth) + "NOT MATCHING > INDEX = '" + k
                        + "' > The two JSONArrays are not matching.");
                return false;
            }
        }
        return true;

    }

    private static boolean areJSONObjectsMatching(Object a, Object b, int depth) {

        // JSONObject
        if (a instanceof JSONObject) {
            if (!(b instanceof JSONObject)) {
                logs.add(depthStr.repeat(depth) + "NOT MATCHING > A is JSONObject while B is not.");
                return false;
            }
            return areMatching((JSONObject) a, (JSONObject) b, depth);

            // JSONArray
        } else if (a instanceof JSONArray) {
            if (!(b instanceof JSONArray)) {
                logs.add(depthStr.repeat(depth) + "NOT MATCHING > A is JSONArray while B is not.");
                return false;
            }
            JSONArray aArr = (JSONArray) a;
            JSONArray bArr = (JSONArray) b;
            return areJSONArrayMatching(aArr, bArr, depth);

            // String
        } else if (a instanceof String) {
            if (!(b instanceof String)) {
                logs.add(depthStr.repeat(depth) + "NOT MATCHING > A is String while B is not.");
                return false;
            }
            return ((String) a).equals((String) b);

            // Double or Integer
        } else if (a instanceof Number) {
            if (!(b instanceof Number)) {
                logs.add(depthStr.repeat(depth) + "NOT MATCHING > A is Number while B is not.");
                return false;
            }
            Double aDouble = Double.valueOf(((Number) a).doubleValue());
            Double bDouble = Double.valueOf(((Number) b).doubleValue());
            return aDouble.equals(bDouble);
        } else if (a instanceof Boolean) {
            if (!(b instanceof Boolean)) {
                logs.add(depthStr.repeat(depth) + "NOT MATCHING > A is Boolean while B is not.");
                return false;
            }
            return ((Boolean) a).equals((Boolean) b);
        } else {
            logs.add(depthStr.repeat(depth) + "WARNING > UNHANDLED CASE! SHOULD NOT HAPPEND!");
            return a.toString().equals(b.toString());
        }
    }

    public static void printJsonStrings(JSONObject jsonA, JSONObject jsonB, int indent) {
        printJsonStrings(jsonA.toString(indent), jsonB.toString(indent));
    }

    public static void printJsonStrings(String jsonAstr, String jsonBstr) {
        System.out.println("> ----------------------------------------------");
        System.out.println("> A  ===========================================");
        System.out.println(jsonAstr);
        System.out.println("> B  ===========================================");
        System.out.println(jsonBstr);
        System.out.println("> ----------------------------------------------");
    }

}
