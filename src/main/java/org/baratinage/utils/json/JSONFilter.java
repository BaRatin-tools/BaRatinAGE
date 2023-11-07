package org.baratinage.utils.json;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONFilter {

    public static JSONObject filter(JSONObject json,
            boolean recursive,
            boolean excludeKeys,
            String... keys) {
        return filter(0, json, recursive, excludeKeys, keys);
    }

    private static JSONObject filter(
            int depth,
            JSONObject json,
            boolean recursive,
            boolean excludeKeys,
            String... keys) {
        if (!recursive && depth > 0) {
            return json;
        }

        if (JSONObject.getNames(json) == null) {
            return json;
        }

        JSONObject filteredJSON = null;
        if (excludeKeys) {
            // create shallow copy (see: https://stackoverflow.com/a/12809884)
            JSONObject jsonExclude = new JSONObject(json, JSONObject.getNames(json));
            for (String key : keys) {
                if (json.has(key)) {
                    jsonExclude.remove(key);

                }
            }
            filteredJSON = jsonExclude;
        } else {
            JSONObject jsonInclude = new JSONObject();
            for (String key : keys) {
                if (json.has(key)) {
                    jsonInclude.put(key, json.get(key));
                }
            }
            filteredJSON = jsonInclude;
        }

        if (!recursive) {
            return filteredJSON;
        }

        for (String key : filteredJSON.keySet()) {
            if (filteredJSON.isNull(key)) {
                continue;
            }
            Object o = filteredJSON.opt(key);
            if (o instanceof JSONObject) {
                filteredJSON.put(
                        key,
                        filter(
                                depth + 1,
                                (JSONObject) o,
                                recursive, excludeKeys, keys));
            } else if (o instanceof JSONArray) {
                filteredJSON.put(
                        key,
                        filter(
                                depth + 1,
                                (JSONArray) o,
                                recursive, excludeKeys, keys));
            }
        }

        return filteredJSON;
    }

    private static JSONArray filter(
            int depth,
            JSONArray json,
            boolean recursive,
            boolean excludeKeys,
            String... keys) {

        JSONArray jsonCopy = new JSONArray(json);
        for (int k = 0; k < jsonCopy.length(); k++) {
            if (jsonCopy.isNull(k)) {
                continue;
            }
            Object o = jsonCopy.get(k);
            if (o instanceof JSONObject) {
                JSONObject f = filter(
                        depth + 1,
                        (JSONObject) o,
                        recursive,
                        excludeKeys,
                        keys

                );
                jsonCopy.put(k, f);
            } else if (o instanceof JSONArray) {
                JSONArray f = filter(
                        depth + 1,
                        (JSONArray) o,
                        recursive,
                        excludeKeys,
                        keys

                );
                jsonCopy.put(k, f);
            } else {
                jsonCopy.put(k, o);
            }
        }
        return jsonCopy;
    }

}
