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

    static Map<String, TableDefEntity> tableDef;
    
    public static void loadTableDef() throws Exception {
        if (isAvailable() && tableDef == null) {
            String file = PropertyUtil.getProperty("table.def.file.path");
            Workbook wb = ExcelUtil.getWorkbook(file);

            Sheet sheet = wb.getSheet(PropertyUtil.getProperty("table.def.sheet.name"));

            tableDef = new HashMap<String, TableDefEntity>();

            int first = sheet.getFirstRowNum();
            int last = sheet.getLastRowNum();
            
            for (int i=first; i<=last; i++) {
                Row row = sheet.getRow(i);
                String tableName = row.getCell(0).getStringCellValue().replace("'", "").trim();
                String columnName = row.getCell(4).getStringCellValue().replace("'", "").trim();
                String typeName = row.getCell(9).getStringCellValue().replace("'", "").trim();
                String pkName = row.getCell(14).getStringCellValue().replace("'", "").trim();
                
                if (i == first) {
                    checkTableDefHeader(tableName, columnName, typeName, pkName);
                } else {
                    TableDefEntity entity = new TableDefEntity();
                    IColumnTypeFactory factory = (IColumnTypeFactory) BeanConfigurator.getBean("columnTypeFactory");
                    entity.setType(factory.getColumnType(typeName));
                    entity.setPk(StringUtils.equalsIgnoreCase("Yes", pkName));

                    String key = getKey(tableName, columnName);
                    tableDef.put(key, entity);
                }
            }
        } else {
            tableDef = Collections.EMPTY_MAP;
        }
    }

    private static void checkTableDefHeader(String tableName, String columnName, String typeName, String pkName) throws Exception {
        if (!StringUtils.contains(tableName, PropertyUtil.getProperty("table.def.sheet.index.table.name"))) {
            logger.error("wrong table definition file couldn't find column name contains [{}]", PropertyUtil.getProperty("table.def.sheet.index.table.name"));
            throw new Exception();
        }
        if (!StringUtils.contains(columnName, PropertyUtil.getProperty("table.def.sheet.index.column.name"))) {
            logger.error("wrong table definition file couldn't find column name contains [{}]", PropertyUtil.getProperty("table.def.sheet.index.column.name"));
            throw new Exception();
        }
        if (!StringUtils.contains(typeName, PropertyUtil.getProperty("table.def.sheet.index.type.name"))) {
            logger.error("wrong table definition file couldn't find column name contains [{}]", PropertyUtil.getProperty("table.def.sheet.index.type.name"));
            throw new Exception();
        }
        if (!StringUtils.contains(pkName, PropertyUtil.getProperty("table.def.sheet.index.pk.name"))) {
            logger.error("wrong table definition file couldn't find column name contains [{}]", PropertyUtil.getProperty("table.def.sheet.index.pk.name"));
            throw new Exception();
        }
    }

    private static String getKey(String tableName, String columnName) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_UNDERSCORE, tableName) + "" +  CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_UNDERSCORE, columnName);
    }
    
    public static TableDefEntity get(String tableName, String ColumnName) {
        String key = getKey(tableName, ColumnName);
        if (!tableDef.containsKey(key)) {
            logger.error("{} is not existing in table definition map", key);
        }
        if (tableDef.get(key) == null) {
            logger.error("value of {} is null in table definition map", key);
        }
        return tableDef.get(key);
    }

    public static boolean isAvailable() {
        return StringUtils.equalsIgnoreCase(PropertyUtil.getProperty("use.table.def.file"), "true");
    }
}
