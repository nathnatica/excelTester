package com.github.nathnatica;

import com.github.nathnatica.model.ColumnEntity;
import com.github.nathnatica.model.DataEntity;
import com.github.nathnatica.model.TableEntity;
import com.google.common.collect.RowSortedTable;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ExcelLoader {
    final static Logger logger = LoggerFactory.getLogger(ExcelLoader.class);

    public static void main(String[] args) throws IOException {
//        if (args.length < 2) {
//            System.out.println("wrong number of arguments");
//            return;
//        }

        String path = PropertyUtil.getProperty("file.path");
//        String path = args[0];
//        String action = args[1];

        Calendar c = GregorianCalendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH)+1;
        int date = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        StringBuilder sb = new StringBuilder();
        sb.append(year).append(month < 10?"0"+month:month).append(date<10?"0"+date:date)
                .append(hour<10?"0"+hour:hour).append(minute<10?"0"+minute:minute).append(second<10?"0"+second:second);

        Files.copy(new File(path), new File(path.replace(".xls","_backup_" + sb.toString() + ".xls")));


        XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(new File(path)));

        int sheets = wb.getNumberOfSheets();
        List<TableEntity> tables = null;
        for (int i=0; i<sheets; i++) {
            if (wb.getSheetAt(i).getSheetName().contains("input")) {
                logger.debug("read input sheet");
                tables = readInputSheet(wb.getSheetAt(i));
            } else if (wb.getSheetAt(i).getSheetName().contains("check")) {

            }
        }


        tables.get(0).getInsertSQL();

    }



    private static List<TableEntity> readInputSheet(Sheet sheet) {
        int first = sheet.getFirstRowNum();
        int last = sheet.getLastRowNum();


        List<TableEntity> tables = new ArrayList<TableEntity>();
        TableEntity table = null;
        List<ColumnEntity> columns = new ArrayList<ColumnEntity>();
        List<DataEntity> data = new ArrayList<DataEntity>();
        int start = -1;
        int end = -1;
        for (int i=first; i<=last; i++) {

            Row row = sheet.getRow(i);
            if (RowUtil.isTableRow(row)) {
                logger.debug("table row");
                table = new TableEntity();
                table.name = RowUtil.getTableName(row);
            } else if (RowUtil.isColumnRow(row)) {
                logger.debug("column row");
                start = RowUtil.DATA_START_COLUMN_INDEX;
                end = row.getLastCellNum()-1;
                for (int j=start; j<=end; j++) {
                    ColumnEntity column = new ColumnEntity();
                    column.name = row.getCell(j).getStringCellValue();
                    columns.add(column);
                }
                table.columns = columns;
            } else if (RowUtil.isTypesRow(row)) {
                logger.debug("type row");
                for (int j=0; j<columns.size(); j++) {
                    columns.get(j).type = row.getCell(j+start).getStringCellValue();
                }
            } else if (RowUtil.isConditionsRow(row)) {
                logger.debug("condition row");
                for (int j=0; j<columns.size(); j++) {
                    Cell c = row.getCell(j+start);
                    if (c != null) {
                        columns.get(j).condition = c.getStringCellValue();
                    }
                }
            } else if (RowUtil.isDataRow(row)) {
                logger.debug("data row");
                DataEntity entity = new DataEntity();
                for (int j=0; j<columns.size(); j++) {
                    Cell c = row.getCell(j+start);
                    if (c != null) {
                        entity.value = c.getStringCellValue();
                    }
                }
                data.add(entity);
                table.data = data;
            } else if (RowUtil.isCountRow(row)) {
                logger.debug("count row");
                table.count = Integer.parseInt(row.getCell(RowUtil.DATA_START_COLUMN_INDEX).getStringCellValue());
                tables.add(table);
                table = null;
                columns = null;
                data = null;
            }

        }

        for (TableEntity t: tables) {
            if (t.data.size() != t.count) {
                logger.error("count not matching");
            }
        }
    return tables;
    }
}
