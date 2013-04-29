package com.github.nathnatica;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;

public class RowUtil {

    public static final int CONTROL_COLUMN_INDEX = 1;
    public static final int DATA_START_COLUMN_INDEX = 3;

    public static boolean isTableRow(Row row) {
        return StringUtils.equalsIgnoreCase(row.getCell(CONTROL_COLUMN_INDEX).getStringCellValue(), "table");
    }

    public static String getTableName(Row row) {
        return row.getCell(DATA_START_COLUMN_INDEX).getStringCellValue();
    }

    public static boolean isColumnRow(Row row) {
        return StringUtils.equalsIgnoreCase(row.getCell(CONTROL_COLUMN_INDEX).getStringCellValue(), "column");
    }

    public static boolean isTypesRow(Row row) {
        return StringUtils.equalsIgnoreCase(row.getCell(CONTROL_COLUMN_INDEX).getStringCellValue(), "type");
    }

    public static boolean isConditionsRow(Row row) {
        return StringUtils.equalsIgnoreCase(row.getCell(CONTROL_COLUMN_INDEX).getStringCellValue(), "condition");
    }

    public static boolean isDataRow(Row row) {
        return StringUtils.equalsIgnoreCase(row.getCell(CONTROL_COLUMN_INDEX).getStringCellValue(), "i");
    }

    public static boolean isCountRow(Row row) {
        return StringUtils.equalsIgnoreCase(row.getCell(CONTROL_COLUMN_INDEX).getStringCellValue(), "count");
    }

}
