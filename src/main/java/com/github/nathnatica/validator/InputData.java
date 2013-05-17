package com.github.nathnatica.validator;

import com.github.nathnatica.model.TableEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class InputData {
    final static Logger logger = LoggerFactory.getLogger(InputData.class);
    
    public static boolean validateInputData(List<TableEntity> tables) {
        if (tables == null || tables.size() == 0) {
            logger.error("input table information is empty");
            return true;
        }
        for (TableEntity table : tables) {
            if (table.records.size() == 0) {
                logger.error("{} table's record size is 0", table.name);
                return true;
            }
            if (table.count == 0) {
                logger.error("{} table's count is 0", table.name);
                return true;
            }
            if (table.records.size() != table.count) {
                logger.error("{} table's size and count are not matching", table.name);
                return true;
            }
            logger.info("[{}] is target table with {} records to work", table.name, table.count);
        }
        return false;
    }
}
