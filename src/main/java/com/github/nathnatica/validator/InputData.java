package com.github.nathnatica.validator;

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
}
