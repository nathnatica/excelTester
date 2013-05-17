package com.github.nathnatica.validator;

import com.github.nathnatica.model.ColumnEntity;
import com.github.nathnatica.model.RecordEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.List;

public class Fill {
    final static Logger logger = LoggerFactory.getLogger(Fill.class);
    
    public static boolean fillInsetSQL(PreparedStatement preparedStatement, RecordEntity r) throws Exception {
        List<ColumnEntity> cList = r.columns;
        List<String> vList = r.values;
        logger.error("column list size {}, value list size {} ", cList.size(), vList.size());

        try {
            for (int i=0; i<cList.size(); i++) {
                ColumnEntity c = cList.get(i);
                logger.debug(i + "(" + c.name + ") = \"" + vList.get(i) + "\"");
                if (StringUtils.equalsIgnoreCase("VARCHAR2", c.type)) {
                    preparedStatement.setString(i+1, vList.get(i));
                } else if (StringUtils.equalsIgnoreCase("NUMBER", c.type)) {
                    if (StringUtils.isNotEmpty(vList.get(i))) {
                        preparedStatement.setBigDecimal(i+1, new BigDecimal(vList.get(i)));
                    } else {
                        preparedStatement.setBigDecimal(i+1, null);
                    }
                } else if (StringUtils.equalsIgnoreCase("RAW", c.type)) {
                    preparedStatement.setString(i+1, vList.get(i));
                } else if (StringUtils.equalsIgnoreCase("DATE", c.type)) {
//                        preparedStatement.setTimestamp(i+1, Timestamp.valueOf(vList.get(i).replace("/", "-")));
                    preparedStatement.setString(i+1, vList.get(i));
                } else {
                    throw new Exception("wrong column type");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return true;
    }
    
    
    
}
