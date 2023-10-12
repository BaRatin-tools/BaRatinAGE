package org.baratinage.utils.json;

import java.util.Map;

public record JSONCompareResult(
        boolean matching,
        String type,
        String message,
        Map<String, JSONCompareResult> children) {
};