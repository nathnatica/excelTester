package com.github.nathnatica;

import com.github.nathnatica.model.ColumnEntity;
import com.github.nathnatica.model.RecordEntity;
import com.github.nathnatica.model.TableDefEntity;
import com.github.nathnatica.model.TableEntity;
import com.github.nathnatica.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.ArrayList;
import java.util.List;

public class RowUtil {

    public static final int CONTROL_COLUMN_INDEX = 1; // in excel file
    public static final int DATA_START_COLUMN_INDEX = 3; // in excel file
    public static final String CONTROL_COLUMN_DELEMITER = ",";
    public static final String COLUMN_CONDITON_WHERE = "W";
    public static final String COLUMN_CHECK_TYPE_EQUAL = "e";
    
    
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

    public static boolean isInertRow(Row row) {
        return matchesRowType("i", row);
    }

    public static boolean isCheckRow(Row row) {
        return matchesRowType("c", row);
    }
    public static boolean isExpectRow(Row row) {
        return matchesRowType("e", row);
    }
    public static boolean isActualRow(Row row) {
        return matchesRowType("a", row);
    }
    public static boolean isResultRow(Row row) {
        return matchesRowType("r", row);
    }
    public static boolean isCountRow(Row row) {
        return matchesRowType("count", row);
    }
    public static boolean isDeleteRow(Row row) {
        return matchesRowType("d", row);
    }
    
    private static boolean matchesRowType(String type, Row row) {
        if (row != null && row.getCell(CONTROL_COLUMN_INDEX) != null && StringUtils.isNotEmpty(row.getCell(CONTROL_COLUMN_INDEX).getStringCellValue())) {
            String temp = row.getCell(CONTROL_COLUMN_INDEX).getStringCellValue().trim();
            String[] arr = temp.split(CONTROL_COLUMN_DELEMITER);
            for (String e : arr) {
                if (StringUtils.equalsIgnoreCase(e, type)) {
                    return true;    
                }
            }
        }
        return false;
    }

    public static String getRowType(Row row) {
        if (row != null && row.getCell(CONTROL_COLUMN_INDEX) != null) {
            return row.getCell(CONTROL_COLUMN_INDEX).getStringCellValue();
        } else {
            return null;
        }
    }
    
    
    public static TableEntity makeTableEntity(Row row) {
        TableEntity t = new TableEntity();
        t.columns = new ArrayList<ColumnEntity>();
        t.records = new ArrayList<RecordEntity>();
        t.name = StrUtil.capitalize(RowUtil.getTableName(row));
        return t;
    }
 
    public static void fillColumnInfo(Row row, TableEntity table) {
        int start = RowUtil.DATA_START_COLUMN_INDEX;
        int end = row.getLastCellNum()-1;
        for (int j=start; j<=end; j++) {
            ColumnEntity column = new ColumnEntity(table.columns.size());
            column.name = StrUtil.capitalize(row.getCell(j).getStringCellValue().trim());
            table.columns.add(column);
        }

        // fill type and condition info from table def excel file
        if (TableDefinitionLoader.isAvailable()) {
            for (ColumnEntity column : table.columns) {
                TableDefEntity def = TableDefinitionLoader.get(table.name, column.name);
                column.type = def.getType();
                if (def.isPk()) {
                    column.condition = COLUMN_CONDITON_WHERE;
                }
            }
        }
    }
    
    public static void fillTypeInfo(Row row, TableEntity table) {
        int start = RowUtil.DATA_START_COLUMN_INDEX;
        for (int j=0; j<table.columns.size(); j++) {
            IColumnTypeFactory factory = (IColumnTypeFactory) BeanConfigurator.getBean("columnTypeFactory");
            table.columns.get(j).type = factory.getColumnType(row.getCell(j+start).getStringCellValue());
        }
    }
   
    
    public static void fillConditionInfo(Row row, TableEntity table) {
        int start = RowUtil.DATA_START_COLUMN_INDEX;
        for (int j=0; j<table.columns.size(); j++) {
            Cell c = row.getCell(j+start);
            if (c != null) {
                table.columns.get(j).condition = c.getStringCellValue().trim();
            }
        }
    }
    
    public static void fillCheckInfo(Row row, TableEntity table) {
        int start = RowUtil.DATA_START_COLUMN_INDEX;
        for (int j=0; j<table.columns.size(); j++) {
            Cell c = row.getCell(j+start);
            if (c != null) {
                table.columns.get(j).check = c.getStringCellValue().trim();
            }
        }
    }
    
    private static List<String> getValues(Row row, List<ColumnEntity> columns) {
        int start = RowUtil.DATA_START_COLUMN_INDEX;
        List<String> values = new ArrayList<String>();
        for (int j=0; j<columns.size(); j++) {
            Cell c = row.getCell(j+start);
            if (c != null) {
                values.add(c.getStringCellValue().trim());
            } else {
                values.add(null);
            }
        }
        return values;
    }
    
    public static void fillExpectInfo(Row row, TableEntity table) {
        RecordEntity record = new RecordEntity();
        record.columns = table.columns;
        record.type = RowUtil.getRowType(row);
        record.expecteds = getValues(row, table.columns);
        table.records.add(record);
    }

    public static void fillRecordInfo(Row row, TableEntity table) {
        RecordEntity record = new RecordEntity();
        record.columns = table.columns;
        record.type = RowUtil.getRowType(row);
        record.values = getValues(row, table.columns);
        table.records.add(record);
    }
    
    public static void fillCountInfo(Row row, TableEntity table) {
        table.count = Integer.parseInt(row.getCell(RowUtil.DATA_START_COLUMN_INDEX).getStringCellValue().trim());
    }
}
