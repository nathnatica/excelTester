package com.github.nathnatica;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;

public class RowUtil {

    public static final int CONTROL_COLUMN_INDEX = 1; // in excel file
    public static final int DATA_START_COLUMN_INDEX = 3; // in excel file

    public static boolean isTableRow(Row row) {
        return matchesRowType("table", row);
    }

    public static String getTableName(Row row) {
        return row.getCell(DATA_START_COLUMN_INDEX).getStringCellValue();
    }

    public static boolean isColumnRow(Row row) {
        return matchesRowType("column", row);
    }

    public static boolean isTypesRow(Row row) {
        return matchesRowType("type", row);
    }

    public static boolean isConditionsRow(Row row) {
        return matchesRowType("condition", row);
    }

    public static boolean isRecordRow(Row row) {
        return matchesRowType("i", row);
    }

    public static boolean isCountRow(Row row) {
        return matchesRowType("count", row);
    }

    private static boolean matchesRowType(String type, Row row) {
        return row != null && row.getCell(CONTROL_COLUMN_INDEX) != null
                && StringUtils.equalsIgnoreCase(row.getCell(CONTROL_COLUMN_INDEX).getStringCellValue(), type);
    }

    public static String getRowType(Row row) {
        if (row != null && row.getCell(CONTROL_COLUMN_INDEX) != null) {
            return row.getCell(CONTROL_COLUMN_INDEX).getStringCellValue();
        } else {
            return null;
        }
    }
}
