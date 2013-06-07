package com.github.nathnatica;

import com.github.nathnatica.model.TableEntity;
import com.github.nathnatica.util.ExcelUtil;
import com.github.nathnatica.validator.Argument;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class ExcelReader {
    final static Logger logger = LoggerFactory.getLogger(ExcelReader.class);
    
    public static List<TableEntity> getTableData(Workbook wb) throws Exception {
        
        TableDefinitionLoader.loadTableDef();

        int sheets = wb.getNumberOfSheets();
        List<TableEntity> tables = new ArrayList<TableEntity>();
        for (int i = 0; i < sheets; i++) {
            if (isActionTargetSheet(wb.getSheetAt(i))) {
                readSheet(tables, wb.getSheetAt(i));
            }
        }
        return tables;    
    }
    
    private static boolean isActionTargetSheet(Sheet sh) {
        return (((Argument.isInsertAction() || Argument.isDeleteAction()) && sh.getSheetName().contains("input")) ||
                ((Argument.isCheckAction() || Argument.isDeleteAction()) && sh.getSheetName().contains("check")));
    }
    
    private static void readSheet(List<TableEntity> tables, Sheet sheet) {
        logger.debug("read [{}] sheet", sheet.getSheetName());
        int first = sheet.getFirstRowNum();
        int last = sheet.getLastRowNum();

        TableEntity table = null;
        int deleteRowCount = 0;
        boolean isTargetTable = false;
        for (int i=first; i<=last; i++) {
            Row row = sheet.getRow(i);
            if (RowUtil.isTableRow(row)) {
                table = RowUtil.makeTableEntity(row);
                isTargetTable = true;
            } else if (RowUtil.isColumnRow(row) && isTargetTable) {
                RowUtil.fillColumnInfo(row, table);
            } else if (RowUtil.isTypesRow(row) && isTargetTable) {
                RowUtil.fillTypeInfo(row, table);
            } else if (RowUtil.isConditionsRow(row) && isTargetTable) {
                RowUtil.fillConditionInfo(row, table);
            } else if (RowUtil.isCheckRow(row) && isTargetTable) {
                RowUtil.fillCheckInfo(row, table);
            } else if (Argument.isCheckAction() && RowUtil.isExpectRow(row) && isTargetTable) {
                RowUtil.fillExpectInfo(row, table);
            } else if (RowUtil.isInertRow(row) && isTargetTable) {
                RowUtil.fillRecordInfo(row, table);
            } else if (Argument.isDeleteAction() && RowUtil.isDeleteRow(row) && isTargetTable) {
                RowUtil.fillRecordInfo(row, table);
                deleteRowCount++;
            } else if (RowUtil.isCountRow(row) && isTargetTable) {
                RowUtil.fillCountInfo(row, table);
                table.deleteRowCount = deleteRowCount;
                tables.add(table);
                table = null;
                deleteRowCount = 0;
                isTargetTable = false;
            }

        }
    }
}
