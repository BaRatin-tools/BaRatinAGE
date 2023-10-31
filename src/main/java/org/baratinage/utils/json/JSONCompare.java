package org.baratinage.utils.json;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.baratinage.utils.ConsoleLogger;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONCompare {

    public static JSONCompareResult compare(JSONObject a, JSONObject b) {

        Set<String> keys = new HashSet<>();
        keys.addAll(a.keySet());
        keys.addAll(b.keySet());

        Map<String, JSONCompareResult> children = new HashMap<>();
        boolean matching = true;
        String type = "";
        String message = "";

        for (String key : keys) {

            if (!a.has(key) || !b.has(key)) {
                children.put(key, new JSONCompareResult(
                        false,
                        "key",
                        "Either a or b is missing key '" + key + "'",
                        null));
                matching = false;
                continue;
            }

            // NULL HANDLING
            if ((a.isNull(key) && !b.isNull(key)) || (!a.isNull(key) && b.isNull(key))) {
                children.put(key, new JSONCompareResult(
                        false,
                        "null",
                        "Either a or b is null",
                        null));
                matching = false;
                continue;
            }

            Object oA = a.opt(key);
            Object oB = b.opt(key);

            JSONCompareResult res = compare(oA, oB);
            if (!res.matching()) {
                matching = false;
            }
            children.put(key, res);
        }

        if (!matching) {
            type = "map";
            message = "At least one of the children is not matching";
        }
        return new JSONCompareResult(matching, type, message, children);
    }

    public static JSONCompareResult compare(JSONArray a, JSONArray b) {
        int n = a.length();
        int m = b.length();
        if (n != m) {
            return new JSONCompareResult(
                    false,
                    "length",
                    "a and b are of different length",
                    null);
        }

        Map<String, JSONCompareResult> children = new HashMap<>();
        boolean matching = true;
        String type = "";
        String message = "";

        for (int k = 0; k < n; k++) {

            // NULL HANDLING
            if (a.isNull(k) && !b.isNull(k)) {
                children.put(String.valueOf(k),
                        new JSONCompareResult(
                                false,
                                "null",
                                "a is null and b is not null",
                                null));
                matching = false;
                continue;
            } else if (!a.isNull(k) && b.isNull(k)) {
                children.put(String.valueOf(k),
                        new JSONCompareResult(
                                false,
                                "null",
                                "a is not null and b is null",
                                null));
                matching = false;
                continue;
            } else if (a.isNull(k) && b.isNull(k)) {
                children.put(String.valueOf(k),
                        new JSONCompareResult(
                                true,
                                "null",
                                "a and b are both null",
                                null));
                continue;
            }

            // boolean matching = areJSONObjectsMatching(a.opt(k), b.opt(k), depth + 1);
            JSONCompareResult res = compare(a.opt(k), b.opt(k));
            if (!res.matching()) {
                matching = false;
            }
            children.put(String.valueOf(k), res);

        }
        if (!matching) {
            type = "array";
            message = "At least one of the children is not matching";
        }
        return new JSONCompareResult(matching, type, message, children);

    }

    private static JSONCompareResult compare(Object a, Object b) {

        // JSONObject
        if (a instanceof JSONObject) {
            if (!(b instanceof JSONObject)) {
                return new JSONCompareResult(
                        false,
                        "type",
                        "b should be a JSONObject",
                        null);
            }
            return compare((JSONObject) a, (JSONObject) b);

            // JSONArray
        } else if (a instanceof JSONArray) {
            if (!(b instanceof JSONArray)) {
                return new JSONCompareResult(
                        false,
                        "type",
                        "b should be a JSONArray",
                        null);
            }
            JSONArray aArr = (JSONArray) a;
            JSONArray bArr = (JSONArray) b;
            return compare(aArr, bArr);

            // String
        } else if (a instanceof String) {
            if (!(b instanceof String)) {
                return new JSONCompareResult(
                        false,
                        "type",
                        "b should be a String",
                        null);
            }
            return new JSONCompareResult(
                    ((String) a).equals((String) b),
                    "string",
                    "",
                    null);

            // Double or Integer
        } else if (a instanceof Number) {
            if (!(b instanceof Number)) {
                return new JSONCompareResult(
                        false,
                        "type",
                        "b should be a Number",
                        null);
            }
            Double aDouble = Double.valueOf(((Number) a).doubleValue());
            Double bDouble = Double.valueOf(((Number) b).doubleValue());

            return new JSONCompareResult(
                    aDouble.equals(bDouble),
                    "number",
                    "",
                    null);

        } else if (a instanceof Boolean) {
            if (!(b instanceof Boolean)) {
                return new JSONCompareResult(
                        false,
                        "type",
                        "b should be a Boolean",
                        null);
            }
            return new JSONCompareResult(
                    ((Boolean) a).equals((Boolean) b),
                    "boolean",
                    "",
                    null);
        } else {
            ConsoleLogger.error("unhandle comparison case! Should not happend!");

            return new JSONCompareResult(
                    a.toString().equals(b.toString()),
                    "unknown",
                    "",
                    null);
        }
    }

}
