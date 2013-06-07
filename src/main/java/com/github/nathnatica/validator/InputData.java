package com.github.nathnatica.validator;

import org.apache.commons.lang3.StringUtils;
import com.github.nathnatica.model.ColumnEntity;
import com.github.nathnatica.model.RecordEntity;
import com.github.nathnatica.model.TableEntity;
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
            if (table.records.size() == 0) {
                logger.error("{} table's record size is 0", table.name);
                return false;
            }
            if (table.count == 0) {
                logger.error("{} table's count is 0", table.name);
                return false;
            }
            if (table.records.size() != table.count) {
                logger.error("{} table's size and count are not matching", table.name);
                return false;
            }
            logger.info("[{}] is target table with {} records to work", table.name, table.count);
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
    
}
