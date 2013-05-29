package com.github.nathnatica;

import com.github.nathnatica.model.TableDefEntity;
import com.github.nathnatica.util.ExcelUtil;
import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TableDefinitionLoader {
    final static Logger logger = LoggerFactory.getLogger(TableDefinitionLoader.class);
    
    public static Map<String, TableDefEntity> loadTableDef() throws Exception {
        if (StringUtils.equalsIgnoreCase(PropertyUtil.getProperty("use.table.def.file"), "true")) {
            String file = PropertyUtil.getProperty("table.def.file.path");
            Workbook wb = ExcelUtil.getWorkbook(file);

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
}
