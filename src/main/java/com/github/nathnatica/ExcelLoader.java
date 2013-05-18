package com.github.nathnatica;

import com.github.nathnatica.model.ColumnEntity;
import com.github.nathnatica.model.RecordEntity;
import com.github.nathnatica.model.TableDefEntity;
import com.github.nathnatica.model.TableEntity;
import com.github.nathnatica.validator.Argument;
import com.github.nathnatica.validator.InputData;
import com.google.common.base.CaseFormat;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class ExcelLoader {
    final static Logger logger = LoggerFactory.getLogger(ExcelLoader.class);

    static Map<String, TableDefEntity> tableDef;
    
    public static void main(String[] args) throws Exception {

        if (!Argument.validate(args)) return;


        String timestamp = getTimestamp();

        String file = args[0];
        MDC.put("logname", timestamp + "_" + file.substring(file.lastIndexOf("\\") + 1, file.length() - 1) + "_input");

        Files.copy(new File(file), new File(file.replace(".xls", "_backup_" + timestamp + ".xls")));

        tableDef = loadTableDef();

        XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(new File(file)));

        int sheets = wb.getNumberOfSheets();
        List<TableEntity> tables = null;
        for (int i = 0; i < sheets; i++) {
            if (wb.getSheetAt(i).getSheetName().contains("input")) {
                logger.debug("read input sheet");
                tables = readInputSheet(wb.getSheetAt(i));
            } else if (wb.getSheetAt(i).getSheetName().contains("check")) {
                // TODO
            }
        }

        if (InputData.validateInputData(tables)) return;

        DAO dao = new DAO();
        dao.execute(tables, Argument.action);
        
    }

    private static Map loadTableDef() throws Exception {
        System.out.println(PropertyUtil.getProperty("use.table.def.file"));
        if (StringUtils.equalsIgnoreCase(PropertyUtil.getProperty("use.table.def.file"), "true")) {
            String file = PropertyUtil.getProperty("table.def.file.path");
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(new File(file)));

            Sheet sheet = wb.getSheet(PropertyUtil.getProperty("table.def.sheet.name"));

            Map<String, TableDefEntity> tableDefMap = new HashMap<String, TableDefEntity>();
            
            int first = sheet.getFirstRowNum();
            int last = sheet.getLastRowNum();
            for (int i=first; i<=last; i++) {
                Row row = sheet.getRow(i);
                String tableName = row.getCell(0).getStringCellValue().replace("'", "").trim();
                String columnName = row.getCell(4).getStringCellValue().replace("'", "").trim();
                String typeName = row.getCell(9).getStringCellValue().replace("'", "").trim();
                String pkName = row.getCell(14).getStringCellValue().replace("'", "").trim();
                if (i == first) {

                    if (!StringUtils.contains(tableName, PropertyUtil.getProperty("table.def.sheet.index.table.name"))) {
                        logger.error("wrong table definition file coundn't find column name contains [{}]", PropertyUtil.getProperty("table.def.sheet.index.table.name"));
                        throw new Exception();
                    }
                    if (!StringUtils.contains(columnName, PropertyUtil.getProperty("table.def.sheet.index.column.name"))) {
                        logger.error("wrong table definition file coundn't find column name contains [{}]", PropertyUtil.getProperty("table.def.sheet.index.column.name"));
                        throw new Exception();
                    }
                    if (!StringUtils.contains(typeName, PropertyUtil.getProperty("table.def.sheet.index.type.name"))) {
                        logger.error("wrong table definition file coundn't find column name contains [{}]", PropertyUtil.getProperty("table.def.sheet.index.type.name"));
                        throw new Exception();
                    }
                    if (!StringUtils.contains(pkName, PropertyUtil.getProperty("table.def.sheet.index.pk.name"))) {
                        logger.error("wrong table definition file coundn't find column name contains [{}]", PropertyUtil.getProperty("table.def.sheet.index.pk.name"));
                        throw new Exception();
                    }
                }

                TableDefEntity entity = new TableDefEntity();
                entity.setType(typeName);
                entity.setPk(StringUtils.equalsIgnoreCase("Yes", pkName));

                String key = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_UNDERSCORE, tableName) +
                    "" +  CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_UNDERSCORE, columnName);      
                tableDefMap.put(key, entity);
            }
            return tableDefMap;
        }
        return Collections.EMPTY_MAP;
    }

    private static String getTimestamp() {
        Calendar c = GregorianCalendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int date = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        StringBuilder sb = new StringBuilder();
        sb.append(year).append(month < 10 ? "0" + month : month).append(date < 10 ? "0" + date : date)
                .append(hour < 10 ? "0" + hour : hour).append(minute < 10 ? "0" + minute : minute).append(second < 10 ? "0" + second : second);
        return sb.toString();
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
