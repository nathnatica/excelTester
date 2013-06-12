package com.github.tester.validator;

import com.github.tester.model.ColumnEntity;
import com.github.tester.model.RecordEntity;
import com.github.tester.model.TableEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class InputData {
    final static Logger logger = LoggerFactory.getLogger(InputData.class);
    
    public static boolean isValid(List<TableEntity> tables) {
        if (tables == null || tables.size() == 0) {
            logger.error("input table information is empty");
            return false;
        }
        for (TableEntity table : tables) {
            if (!(Argument.isDeleteAction() && table.isOfCheckSheet())) {
                if (table.records.size() == 0) {
                    logger.error("[{}][{}] table's record size is 0", table.sheetName, table.name);
                    return false;
                }
                if (table.count == 0) {
                    logger.error("[{}][{}] table's count is 0", table.sheetName, table.name);
                    return false;
                }
                if (table.records.size() != table.count) {
                    logger.error("[{}][{}] table's size and count are not matching", table.sheetName, table.name);
                    return false;
                }
            } else {
                if (table.records.size() != table.deleteRowCount) {
                    logger.error("[{}][{}] table's size and deleteRowcount are not matching", table.sheetName, table.name);
                    return false;
                }
            }
            logger.info("[{}][{}] is target table with {} records to work", new Object[] {table.sheetName, table.name, table.count});
        }
        return true;
    }

    public static void check(List<TableEntity> tables) {
        for (TableEntity table : tables) {
            List<ColumnEntity> c = table.columns;
            for (RecordEntity record : table.records) {
                List<String> e = record.expecteds;
                List<String> a = record.actuals;
                if (c.size() != e.size()) {
                    logger.error("expected record size and column size of table [{}][{}] are not matching", table.sheetName, table.name);
                    logger.error("expected record size [{}], column size [{}]", e.size(), c.size());
                }
                if (record.isExisingRecord && (e.size() != a.size())) {
                    logger.error("expected record size and actual record size of table [{}][{}] are not matching", table.sheetName, table.name);
                    logger.error("expected record size [{}], actual record size [{}]", e.size(), a.size());
                }
                for (int i=0; i<e.size(); i++) {
                    if (StringUtils.equals(e.get(i), a.get(i)) || (StringUtils.isEmpty(e.get(i)) && StringUtils.isEmpty(a.get(i)))) {
                        logger.debug("expected [{}] and actual [{}]", e.get(i), a.get(i)) ;
                    } else {
                        logger.error("expected [{}] but actual [{}]", e.get(i), a.get(i));
                    }
                }
            }
        }
    }
    
}
