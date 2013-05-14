package com.github.nathnatica;

import com.github.nathnatica.model.ColumnEntity;
import com.github.nathnatica.model.RecordEntity;
import com.github.nathnatica.model.TableEntity;
import com.google.common.io.Files;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ExcelLoader {
    final static Logger logger = LoggerFactory.getLogger(ExcelLoader.class);

    public static void main(String[] args) throws IOException {

//        String path = PropertyUtil.getProperty("file.path");
        String file = null;
        if (args == null || args.length == 0) {
            System.out.println("needs argument[0] - excel file name");
            return;
        } else {
            file = args[0];
        }
//        String action = args[1];

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
        String timestamp = sb.toString();

        MDC.put("logname", timestamp + "_" + file.substring(file.lastIndexOf("\\") + 1, file.length() - 1) + "_input");

        Files.copy(new File(file), new File(file.replace(".xls", "_backup_" + timestamp + ".xls")));


        XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(new File(file)));

        int sheets = wb.getNumberOfSheets();
        List<TableEntity> tables = null;
        for (int i = 0; i < sheets; i++) {
            if (wb.getSheetAt(i).getSheetName().contains("input")) {
                logger.debug("read input sheet");
                tables = readInputSheet(wb.getSheetAt(i));
            } else if (wb.getSheetAt(i).getSheetName().contains("check")) {

            }
        }


        for (TableEntity table : tables) {
            if (table.records.size() != table.count) {
                logger.error("count not matching");
            }
            table.print();
            table.getInsertSQL();
            table.getDeleteSQL();
        }
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
        for (int i=first; i<=last; i++) {
            System.out.println(i);
            Row row = sheet.getRow(i);
            if (RowUtil.isTableRow(row)) {
                logger.debug("table row");
                table = new TableEntity();
                columns = new ArrayList<ColumnEntity>();
                records = new ArrayList<RecordEntity>();
                table.name = RowUtil.getTableName(row);
            } else if (RowUtil.isColumnRow(row)) {
                logger.debug("column row");
                start = RowUtil.DATA_START_COLUMN_INDEX;
                end = row.getLastCellNum()-1;
                for (int j=start; j<=end; j++) {
                    ColumnEntity column = new ColumnEntity(columns.size());
                    column.name = row.getCell(j).getStringCellValue().trim();
                    columns.add(column);
                }
                table.columns = columns;
            } else if (RowUtil.isTypesRow(row)) {
                logger.debug("type row");
                for (int j=0; j<columns.size(); j++) {
                    columns.get(j).type = row.getCell(j+start).getStringCellValue().trim();
                }
            } else if (RowUtil.isConditionsRow(row)) {
                logger.debug("condition row");
                for (int j=0; j<columns.size(); j++) {
                    Cell c = row.getCell(j+start);
                    if (c != null) {
                        columns.get(j).condition = c.getStringCellValue().trim();
                    }
                }
            } else if (RowUtil.isRecordRow(row)) {
                RecordEntity record = new RecordEntity();
                record.columns = columns;
                record.type = RowUtil.getRowType(row);
                List<String> values = new ArrayList<String>();
                logger.debug("data row");
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
            } else if (RowUtil.isCountRow(row)) {
                logger.debug("count row");
                table.count = Integer.parseInt(row.getCell(RowUtil.DATA_START_COLUMN_INDEX).getStringCellValue().trim());
                table.records = records;
                tables.add(table);
                table = null;
                columns = null;
                records = null;
            }

        }
    return tables;
    }
}
