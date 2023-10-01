package org.baratinage.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONcomparator {

    private static List<String> logs = new ArrayList<>();

    public static boolean areMatching(JSONObject a, JSONObject b) {
        logs.clear();
        boolean matching = areMatching(a, b, 0);
        if (!matching) {
            for (String s : logs) {
                System.out.println(s);
            }
        }
        return matching;
    }

    private static boolean areMatching(JSONObject a, JSONObject b, int depth) {

        Set<String> keysA = a.keySet();
        Set<String> keysB = b.keySet();

        if (!keysA.containsAll(keysB) || !keysB.containsAll(keysA)) {
            logs.add("NOT MATCHING > keys not matching");
            return false;
        }

        for (String key : keysA) {
            logs.add(".".repeat(depth) + " > Comparing items for key '" + key + "'...");

            // NULL HANDLING
            if (a.isNull(key) && !b.isNull(key)) {
                logs.add("NOT MATCHING > A is null while B is not");
                return false;
            } else if (!a.isNull(key) && b.isNull(key)) {
                logs.add("NOT MATCHING > A is not null while B is");
                return false;
            } else if (a.isNull(key) && b.isNull(key)) {
                continue;
            }

            Object oA = a.opt(key);
            Object oB = b.opt(key);
            boolean matching = areJSONObjectsMatching(oA, oB, depth + 1);
            if (!matching) {
                logs.add("NOT MATCHING > Elements '" + key + "' of the two JSONObject are not matching");
                return false;
            }
        }
        return true;
    }

    private static boolean areJSONArrayMatching(JSONArray a, JSONArray b, int depth) {
        int n = a.length();
        int m = b.length();
        if (n != m) {
            logs.add("NOT MATCHING > A and B are JSONArray with different length");
            return false;
        }
        for (int k = 0; k < n; k++) {
            logs.add(".".repeat(depth) + " > Comparing items at index '" + k + "'...");

            // NULL HANDLING
            if (a.isNull(k) && !b.isNull(k)) {
                logs.add("NOT MATCHING > A is null while B is not");
                return false;
            } else if (!a.isNull(k) && b.isNull(k)) {
                logs.add("NOT MATCHING > A is not null while B is");
                return false;
            } else if (a.isNull(k) && b.isNull(k)) {
                continue;
            }

            boolean matching = areJSONObjectsMatching(a.opt(k), b.opt(k), depth + 1);
            if (!matching) {
                logs.add("NOT MATCHING > Elements " + k + " of the two JSONArrays are not matching");
                return false;
            }
        }
        return true;

    }

    private static boolean areJSONObjectsMatching(Object a, Object b, int depth) {

        // JSONObject
        if (a instanceof JSONObject) {
            if (!(b instanceof JSONObject)) {
                logs.add("NOT MATCHING > A is JSONObject while B is not");
                return false;
            }
            return areMatching((JSONObject) a, (JSONObject) b, depth);

            // JSONArray
        } else if (a instanceof JSONArray) {
            if (!(b instanceof JSONArray)) {
                logs.add("NOT MATCHING > A is JSONArray while B is not");
                return false;
            }
            JSONArray aArr = (JSONArray) a;
            JSONArray bArr = (JSONArray) b;
            return areJSONArrayMatching(aArr, bArr, depth);

            // String
        } else if (a instanceof String) {
            if (!(b instanceof String)) {
                logs.add("NOT MATCHING > A is String while B is not");
                return false;
            }
            return ((String) a).equals((String) b);

            // Double or Integer
        } else if (a instanceof Number) {
            if (!(b instanceof Number)) {
                logs.add("NOT MATCHING > A is Number while B is not");
                return false;
            }
            Double aDouble = Double.valueOf(((Number) a).doubleValue());
            Double bDouble = Double.valueOf(((Number) b).doubleValue());
            return aDouble.equals(bDouble);
        } else if (a instanceof Boolean) {
            if (!(b instanceof Boolean)) {
                logs.add("NOT MATCHING > A is Boolean while B is not");
                return false;
            }
            return ((Boolean) a).equals((Boolean) b);
        } else {
            logs.add("WARNING: UNHANDLED CASE!");
            return a.toString().equals(b.toString());
        }
    }

}
