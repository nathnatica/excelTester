package com.github.nathnatica;

import com.github.nathnatica.model.ColumnEntity;
import com.github.nathnatica.model.RecordEntity;
import com.github.nathnatica.model.TableDefEntity;
import com.github.nathnatica.model.TableEntity;
import com.github.nathnatica.util.ExcelUtil;
import com.github.nathnatica.util.TimeUtil;
import com.github.nathnatica.validator.Argument;
import com.github.nathnatica.validator.InputData;
import com.google.common.base.CaseFormat;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.File;
import java.util.*;

public class ExcelLoader {
    final static Logger logger = LoggerFactory.getLogger(ExcelLoader.class);

    static Map<String, TableDefEntity> tableDef;
    
    public static void main(String[] args) throws Exception {

        if (!Argument.isValid(args)) return;

        String file = args[0];
        String timestamp = TimeUtil.getYYYYMMDDHH24MISS();
        MDC.put("logname", timestamp + "_" + file.substring(file.lastIndexOf("\\") + 1, file.length() - 1) + "_" + Argument.action.getValue());

        Files.copy(new File(file), new File(file.replace(".xls", "_backup_" + timestamp + ".xls")));

        tableDef = TableDefinitionLoader.loadTableDef();

        Workbook wb = ExcelUtil.getWorkbook(file);

        int sheets = wb.getNumberOfSheets();
        List<TableEntity> tables = null;
        for (int i = 0; i < sheets; i++) {
            if ((Argument.isInsertAction() || Argument.isDeleteAction()) && wb.getSheetAt(i).getSheetName().contains("input")) {
                logger.debug("read input sheet");
                tables = readInputSheet(wb.getSheetAt(i));
            } else if (Argument.isCheckAction() && wb.getSheetAt(i).getSheetName().contains("check")) {
                logger.debug("read check sheet");
                tables = readCheckSheet(wb.getSheetAt(i));
            }
        }

        if (!InputData.isValid(tables)) return;

        DAO dao = new DAO();
        dao.execute(tables, Argument.action);
        
        if (Argument.isCheckAction()) {
            check(tables);
        }
    }

    private static void check(List<TableEntity> tables) {
        for (TableEntity table : tables) {
            for (RecordEntity record : table.records) {
                List<String> e = record.expecteds;
                List<String> a = record.actuals;
                if (record.isExisingRecord && (e.size() != a.size())) {
                    logger.error("expected record size and actual size of record of table {} is not maching", table.name);    
                }
                for (int i=0; i<e.size(); i++) {
                    if (StringUtils.equals(e.get(i), a.get(i))) {
                        logger.debug("expected [{}] and actual [{}]", e.get(i), a.get(i)) ;
                    } else {
                        logger.error("expected [{}] but actual [{}]", e.get(i), a.get(i));
                    }
                }
            }
        }
    }

    private static List<TableEntity> readCheckSheet(Sheet sheet) {
        int first = sheet.getFirstRowNum();
        int last = sheet.getLastRowNum();

        List<TableEntity> tables = new ArrayList<TableEntity>();
        TableEntity table = null;
        List<ColumnEntity> columns = null;
        List<RecordEntity> records = null;
        int start = -1;
        int end = -1;
        boolean isTargetTable = false;
        for (int i=first; i<=last; i++) {
            Row row = sheet.getRow(i);
            if (RowUtil.isTableRow(row)) {
                table = new TableEntity();
                columns = new ArrayList<ColumnEntity>();
                records = new ArrayList<RecordEntity>();
                table.name = capitalize(RowUtil.getTableName(row));
                isTargetTable = true;
            } else if (RowUtil.isColumnRow(row) && isTargetTable) {
                start = RowUtil.DATA_START_COLUMN_INDEX;
                end = row.getLastCellNum()-1;
                for (int j=start; j<=end; j++) {
                    ColumnEntity column = new ColumnEntity(columns.size());
                    column.name = capitalize(row.getCell(j).getStringCellValue().trim());
                    columns.add(column);
                }
                table.columns = columns;

                // fill type and condtion info from talbe def excel file
                if (StringUtils.equalsIgnoreCase(PropertyUtil.getProperty("use.table.def.file"), "true")) {
                    for (ColumnEntity column : columns) {
                        String key = table.name + "" + column.name;
                        if (!tableDef.containsKey(key)) {
                            logger.error("{} is not existing in table definition map", key);
                        }
                        if (tableDef.get(key) == null) {
                            logger.error("value of {} is null in table definition map", key);
                        }
                        TableDefEntity def = tableDef.get(key);
                        if (def.isAccectableType()) {
                            column.type = def.getType();
                        }
                        if (def.isPk()) {
                            column.condition = "W";
                        }
                    }
                }

            } else if (RowUtil.isTypesRow(row) && isTargetTable) {
                for (int j=0; j<columns.size(); j++) {
                    columns.get(j).type = row.getCell(j+start).getStringCellValue().trim();
                }
            } else if (RowUtil.isConditionsRow(row) && isTargetTable) {
                for (int j=0; j<columns.size(); j++) {
                    Cell c = row.getCell(j+start);
                    if (c != null) {
                        columns.get(j).condition = c.getStringCellValue().trim();
                    }
                }
            // add for check feature   
            } else if (RowUtil.isCheckRow(row) && isTargetTable) {
                for (int j=0; j<columns.size(); j++) {
                    Cell c = row.getCell(j+start);
                    if (c != null) {
                        columns.get(j).check = c.getStringCellValue().trim();
                    }
                }
            } else if (RowUtil.isExpectRow(row) && isTargetTable) {
                RecordEntity record = new RecordEntity();
                record.columns = columns;
                record.type = RowUtil.getRowType(row);
                List<String> values = new ArrayList<String>();
                for (int j=0; j<columns.size(); j++) {
                    Cell c = row.getCell(j+start);
                    if (c != null) {
                        values.add(c.getStringCellValue().trim());
                    } else {
                        values.add(null);
                    }
                }
                record.expecteds= values;
                records.add(record);
            // add for check feature   
            } else if (RowUtil.isRecordRow(row) && isTargetTable) {
                RecordEntity record = new RecordEntity();
                record.columns = columns;
                record.type = RowUtil.getRowType(row);
                List<String> values = new ArrayList<String>();
                for (int j=0; j<columns.size(); j++) {
                    Cell c = row.getCell(j+start);
                    if (c != null) {
                        values.add(c.getStringCellValue().trim());
                    } else {
                        values.add(null);
                    }
                }
                record.values = values;
                records.add(record);
            } else if (RowUtil.isCountRow(row) && isTargetTable) {
                table.count = Integer.parseInt(row.getCell(RowUtil.DATA_START_COLUMN_INDEX).getStringCellValue().trim());
                table.records = records;
                tables.add(table);
                table = null;
                columns = null;
                records = null;
                isTargetTable = false;
            }
            
        }
        return tables;
    }


    private static List<TableEntity> readInputSheet(Sheet sheet) {
        int first = sheet.getFirstRowNum();
        int last = sheet.getLastRowNum();

        List<TableEntity> tables = new ArrayList<TableEntity>();
        TableEntity table = null;
        List<ColumnEntity> columns = null;
        List<RecordEntity> records = null;
        int start = -1;
        int end = -1;
        boolean isTargetTable = false;
        for (int i=first; i<=last; i++) {
            Row row = sheet.getRow(i);
            if (RowUtil.isTableRow(row)) {
                table = new TableEntity();
                columns = new ArrayList<ColumnEntity>();
                records = new ArrayList<RecordEntity>();
                table.name = capitalize(RowUtil.getTableName(row));
                isTargetTable = true;
            } else if (RowUtil.isColumnRow(row) && isTargetTable) {
                start = RowUtil.DATA_START_COLUMN_INDEX;
                end = row.getLastCellNum()-1;
                for (int j=start; j<=end; j++) {
                    ColumnEntity column = new ColumnEntity(columns.size());
                    column.name = capitalize(row.getCell(j).getStringCellValue().trim());
                    columns.add(column);
                }
                table.columns = columns;
                
                // fill type and condtion info from talbe def excel file
                if (StringUtils.equalsIgnoreCase(PropertyUtil.getProperty("use.table.def.file"), "true")) {
                    for (ColumnEntity column : columns) {
                        String key = table.name + "" + column.name;
                        if (!tableDef.containsKey(key)) {
                            logger.error("{} is not existing in table definition map", key);
                        }
                        if (tableDef.get(key) == null) {
                            logger.error("value of {} is null in table definition map", key);
                        }
                        TableDefEntity def = tableDef.get(key);
                        if (def.isAccectableType()) {
                            column.type = def.getType();
                        }
                        if (def.isPk()) {
                            column.condition = "W";
                        }
                    }
                }
                
            } else if (RowUtil.isTypesRow(row) && isTargetTable) {
                for (int j=0; j<columns.size(); j++) {
                    columns.get(j).type = row.getCell(j+start).getStringCellValue().trim();
                }
            } else if (RowUtil.isConditionsRow(row) && isTargetTable) {
                for (int j=0; j<columns.size(); j++) {
                    Cell c = row.getCell(j+start);
                    if (c != null) {
                        columns.get(j).condition = c.getStringCellValue().trim();
                    }
                }
            } else if (RowUtil.isRecordRow(row) && isTargetTable) {
                RecordEntity record = new RecordEntity();
                record.columns = columns;
                record.type = RowUtil.getRowType(row);
                List<String> values = new ArrayList<String>();
                for (int j=0; j<columns.size(); j++) {
                    Cell c = row.getCell(j+start);
                    if (c != null) {
                        values.add(c.getStringCellValue().trim());
                    } else {
                        values.add(null);
                    }
                }
                record.values = values;
                records.add(record);
            } else if (RowUtil.isCountRow(row) && isTargetTable) {
                table.count = Integer.parseInt(row.getCell(RowUtil.DATA_START_COLUMN_INDEX).getStringCellValue().trim());
                table.records = records;
                tables.add(table);
                table = null;
                columns = null;
                records = null;
                isTargetTable = false;
            }

        }
    return tables;
    }
    
    private static String capitalize(String input) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_UNDERSCORE, input);
    }
    
}
