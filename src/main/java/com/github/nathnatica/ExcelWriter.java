package com.github.nathnatica;

import com.github.nathnatica.model.ColumnEntity;
import com.github.nathnatica.model.RecordEntity;
import com.github.nathnatica.model.TableEntity;
import com.github.nathnatica.util.ExcelUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExcelWriter {
    final static Logger logger = LoggerFactory.getLogger(ExcelWriter.class);


    static void writeActuals(List<TableEntity> tables, Workbook wb) {
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

    static void writeCheckResults(List<TableEntity> tables, Workbook wb) {
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
                            if (column.isCheckTypeEqual()) {
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
