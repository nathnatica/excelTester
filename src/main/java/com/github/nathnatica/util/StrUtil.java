package com.github.nathnatica.util;

import com.google.common.base.CaseFormat;

public class StrUtil {
    public static String capitalize(String input) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_UNDERSCORE, input);
    }
}
