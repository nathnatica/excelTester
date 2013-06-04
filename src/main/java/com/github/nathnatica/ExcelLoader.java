package com.github.nathnatica;

import com.github.nathnatica.model.ColumnEntity;
import com.github.nathnatica.model.RecordEntity;
import com.github.nathnatica.model.TableDefEntity;
import com.github.nathnatica.model.TableEntity;
import com.github.nathnatica.model.column.IColumnType;
import com.github.nathnatica.util.ExcelUtil;
import com.github.nathnatica.util.StrUtil;
import com.github.nathnatica.util.TimeUtil;
import com.github.nathnatica.validator.Argument;
import com.github.nathnatica.validator.InputData;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.File;
import java.util.*;

public class ExcelLoader {
    final static Logger logger = LoggerFactory.getLogger(ExcelLoader.class);
    
    public static void main(String[] args) throws Exception {

        if (!Argument.isValid(args)) return;

        String file = args[0];
        String timestamp = TimeUtil.getYYYYMMDDHH24MISS();
        MDC.put("logname", timestamp + "_" + file.substring(file.lastIndexOf("\\") + 1, file.length() - 1) + "_" + Argument.action.getValue());

        Files.copy(new File(file), new File(file.replace(".xls", "_backup_" + timestamp + ".xls")));

        TableDefinitionLoader.loadTableDef();

        Workbook wb = ExcelUtil.getWorkbook(file);

        int sheets = wb.getNumberOfSheets();
        List<TableEntity> tables = null;
        for (int i = 0; i < sheets; i++) {
            if ((Argument.isInsertAction() || Argument.isDeleteAction()) && wb.getSheetAt(i).getSheetName().contains("input")) {
                logger.debug("read input sheet");
                tables = readSheet(wb.getSheetAt(i));
            } else if ((Argument.isCheckAction() || Argument.isDeleteAction()) && wb.getSheetAt(i).getSheetName().contains("check")) {
                logger.debug("read check sheet");
                tables = readSheet(wb.getSheetAt(i));
            }
        }

        if (!InputData.isValid(tables)) return;

        DAO dao = new DAO();
        dao.execute(tables, Argument.action);
        
        if (Argument.isCheckAction()) {
            check(tables);
            writeActuals(tables, wb);
            writeCheckResults(tables, wb);
            ExcelUtil.writeFile(wb, file);
        }
    }


    private static void check(List<TableEntity> tables) {
        for (TableEntity table : tables) {
            List<ColumnEntity> c = table.columns;   
            for (RecordEntity record : table.records) {
                List<String> e = record.expecteds;
                List<String> a = record.actuals;
                if (c.size() != e.size()) {
                    logger.error("expected record size and column size of table {} are not matching", table.name);
                    logger.error("expected record size [{}], column size [{}]", e.size(), c.size());
                }
                if (record.isExisingRecord && (e.size() != a.size())) {
                    logger.error("expected record size and actual record size of table {} are not matching", table.name);
                    logger.error("expected record size [{}], actual record size [{}]", e.size(), a.size());
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

    private static List<TableEntity> readSheet(Sheet sheet) {
        int first = sheet.getFirstRowNum();
        int last = sheet.getLastRowNum();

        List<TableEntity> tables = new ArrayList<TableEntity>();
        TableEntity table = null;
//        List<ColumnEntity> columns = null;
//        List<RecordEntity> records = null;
//        int start = RowUtil.DATA_START_COLUMN_INDEX;
        boolean isTargetTable = false;
        for (int i=first; i<=last; i++) {
            Row row = sheet.getRow(i);
            if (RowUtil.isTableRow(row)) {
                table = RowUtil.makeTableEntity(row);
//                columns = new ArrayList<ColumnEntity>();
//                records = new ArrayList<RecordEntity>();
//                table.name = StrUtil.capitalize(RowUtil.getTableName(row));
                isTargetTable = true;
            } else if (RowUtil.isColumnRow(row) && isTargetTable) {
                RowUtil.fillColumnInfo(row, table);
//                int end = row.getLastCellNum()-1;
//                for (int j=start; j<=end; j++) {
//                    ColumnEntity column = new ColumnEntity(columns.size());
//                    column.name = StrUtil.capitalize(row.getCell(j).getStringCellValue().trim());
//                    columns.add(column);
//                }
//                table.columns = columns;
//
//                // fill type and condtion info from talbe def excel file
//                if (TableDefinitionLoader.isAvailable()) {
//                    for (ColumnEntity column : columns) {
//                        TableDefEntity def = TableDefinitionLoader.get(table.name, column.name);
//                        column.type = def.getType();
//                        
//                        if (def.isPk()) {
//                            column.condition = "W";
//                        }
//                    }
//                }

            } else if (RowUtil.isTypesRow(row) && isTargetTable) {
                RowUtil.fillTypeInfo(row, table);
//                for (int j=0; j<columns.size(); j++) {
//                    columns.get(j).type = ColumnTypeFactory.getColumnType(row.getCell(j+start).getStringCellValue());
//                }
            } else if (RowUtil.isConditionsRow(row) && isTargetTable) {
                RowUtil.fillConditionInfo(row, table);
//                for (int j=0; j<columns.size(); j++) {
//                    Cell c = row.getCell(j+start);
//                    if (c != null) {
//                        columns.get(j).condition = c.getStringCellValue().trim();
//                    }
//                }
            // add for check feature   
            } else if (RowUtil.isCheckRow(row) && isTargetTable) {
                RowUtil.fillCheckInfo(row, table);
//                for (int j=0; j<columns.size(); j++) {
//                    Cell c = row.getCell(j+start);
//                    if (c != null) {
//                        columns.get(j).check = c.getStringCellValue().trim();
//                    }
//                }
            } else if (RowUtil.isExpectRow(row) && isTargetTable) {
                RowUtil.fillExpectInfo(row, table);
//                RecordEntity record = new RecordEntity();
//                record.columns = columns;
//                record.type = RowUtil.getRowType(row);
//                List<String> values = new ArrayList<String>();
//                for (int j=0; j<columns.size(); j++) {
//                    Cell c = row.getCell(j+start);
//                    if (c != null) {
//                        values.add(c.getStringCellValue().trim());
//                    } else {
//                        values.add(null);
//                    }
//                }
//                record.expecteds= values;
//                records.add(record);
            // add for check feature   
            } else if (RowUtil.isRecordRow(row) && isTargetTable) {
                RowUtil.fillRecordInfo(row, table);
//                RecordEntity record = new RecordEntity();
//                record.columns = columns;
//                record.type = RowUtil.getRowType(row);
//                List<String> values = new ArrayList<String>();
//                for (int j=0; j<columns.size(); j++) {
//                    Cell c = row.getCell(j+start);
//                    if (c != null) {
//                        values.add(c.getStringCellValue().trim());
//                    } else {
//                        values.add(null);
//                    }
//                }
//                record.values = values;
//                records.add(record);
            } else if (RowUtil.isCountRow(row) && isTargetTable) {
                RowUtil.fillCountInfo(row, table);
//                table.count = Integer.parseInt(row.getCell(RowUtil.DATA_START_COLUMN_INDEX).getStringCellValue().trim());
//                table.records = records;
                tables.add(table);
                table = null;
//                columns = null;
//                records = null;
                isTargetTable = false;
            }
            
        }
        return tables;
    }


//    private static List<TableEntity> readInputSheet(Sheet sheet) {
//        int first = sheet.getFirstRowNum();
//        int last = sheet.getLastRowNum();
//
//        List<TableEntity> tables = new ArrayList<TableEntity>();
//        TableEntity table = null;
//        List<ColumnEntity> columns = null;
//        List<RecordEntity> records = null;
//        int start = RowUtil.DATA_START_COLUMN_INDEX;
//        boolean isTargetTable = false;
//        for (int i=first; i<=last; i++) {
//            Row row = sheet.getRow(i);
//            if (RowUtil.isTableRow(row)) {
//                table = new TableEntity();
//                columns = new ArrayList<ColumnEntity>();
//                records = new ArrayList<RecordEntity>();
//                table.name = StrUtil.capitalize(RowUtil.getTableName(row));
//                isTargetTable = true;
//            } else if (RowUtil.isColumnRow(row) && isTargetTable) {
//                int end = row.getLastCellNum()-1;
//                for (int j=start; j<=end; j++) {
//                    ColumnEntity column = new ColumnEntity(columns.size());
//                    column.name = StrUtil.capitalize(row.getCell(j).getStringCellValue().trim());
//                    columns.add(column);
//                }
//                table.columns = columns;
//                
//                // fill type and condtion info from talbe def excel file
//                if (TableDefinitionLoader.isAvailable()) {
//                    for (ColumnEntity column : columns) {
//                        TableDefEntity def = TableDefinitionLoader.get(table.name, column.name);
//                        column.type = def.getType();
//                        
//                        if (def.isPk()) {
//                            column.condition = "W";
//                        }
//                    }
//                }
//                
//            } else if (RowUtil.isTypesRow(row) && isTargetTable) {
//                for (int j=0; j<columns.size(); j++) {
//                    columns.get(j).type = ColumnTypeFactory.getColumnType(row.getCell(j+start).getStringCellValue());
//                }
//            } else if (RowUtil.isConditionsRow(row) && isTargetTable) {
//                for (int j=0; j<columns.size(); j++) {
//                    Cell c = row.getCell(j+start);
//                    if (c != null) {
//                        columns.get(j).condition = c.getStringCellValue().trim();
//                    }
//                }
//            } else if (RowUtil.isRecordRow(row) && isTargetTable) {
//                RecordEntity record = new RecordEntity();
//                record.columns = columns;
//                record.type = RowUtil.getRowType(row);
//                List<String> values = new ArrayList<String>();
//                for (int j=0; j<columns.size(); j++) {
//                    Cell c = row.getCell(j+start);
//                    if (c != null) {
//                        values.add(c.getStringCellValue().trim());
//                    } else {
//                        values.add(null);
//                    }
//                }
//                record.values = values;
//                records.add(record);
//            } else if (RowUtil.isCountRow(row) && isTargetTable) {
//                table.count = Integer.parseInt(row.getCell(RowUtil.DATA_START_COLUMN_INDEX).getStringCellValue().trim());
//                table.records = records;
//                tables.add(table);
//                table = null;
//                columns = null;
//                records = null;
//                isTargetTable = false;
//            }
//
//        }
//    return tables;
//    }

    
    private static void writeActuals(List<TableEntity> tables, Workbook wb) {
        int sheets = wb.getNumberOfSheets();
        for (int i = 0; i < sheets; i++) {
            if (wb.getSheetAt(i).getSheetName().contains("check")) {
                Sheet sheet = wb.getSheetAt(i);
                int first = sheet.getFirstRowNum();
                int last = sheet.getLastRowNum();
                boolean isTargetTable = false;
                TableEntity targetTable = null;
                int tableIndex = 0;
                int actualRecordIndex = 0;
                for (int j=first; j<=last; j++) {
                    Row row = sheet.getRow(j);

                    if (RowUtil.isTableRow(row)) {
                        targetTable = tables.get(tableIndex++);
                        isTargetTable = true;
                    } else if (RowUtil.isActualRow(row) && isTargetTable) {
                        RecordEntity r = targetTable.records.get(actualRecordIndex++);
                        int start = RowUtil.DATA_START_COLUMN_INDEX;
                        for (int k=0; k<r.actuals.size(); k++) {
                            Cell c = row.getCell(k + start);
                            c.setCellValue(r.actuals.get(k));
                        }
                        actualRecordIndex++;
                    } else if (RowUtil.isCountRow(row) && isTargetTable) {
                        isTargetTable = false;
                    }
                }
            }
        }
    }
    
    private static void writeCheckResults(List<TableEntity> tables, Workbook wb) {
        int sheets = wb.getNumberOfSheets();
        for (int i = 0; i < sheets; i++) {
            if (wb.getSheetAt(i).getSheetName().contains("check")) {
                Sheet sheet = wb.getSheetAt(i);
                int first = sheet.getFirstRowNum();
                int last = sheet.getLastRowNum();
                boolean isTargetTable = false;
                TableEntity targetTable = null;
                int tableIndex = 0;
                int actualRecordIndex = 0;
                for (int j=first; j<=last; j++) {
                    Row row = sheet.getRow(j);

                    if (RowUtil.isTableRow(row)) {
                        targetTable = tables.get(tableIndex++);
                        isTargetTable = true;
                    } else if (RowUtil.isResultRow(row) && isTargetTable) {
                        RecordEntity r = targetTable.records.get(actualRecordIndex++);
                        int start = RowUtil.DATA_START_COLUMN_INDEX;
                        for (int k=0; k<r.actuals.size(); k++) {
                            
                            ColumnEntity column = targetTable.columns.get(k);
                            
                            Cell c = row.getCell(k + start);
                            if (StringUtils.equalsIgnoreCase("e", column.check)) { // if check condition is equal
                                if (column.type.isSame(r.expecteds.get(k), r.actuals.get(k))) {
                                    ExcelUtil.setOK(c, wb);
                                } else {
                                    ExcelUtil.setNG(c, wb);
                                }
                            }
                        }
                        actualRecordIndex++;
                    } else if (RowUtil.isCountRow(row) && isTargetTable) {
                        isTargetTable = false;
                    }
                }
            }
        }
    }

}
