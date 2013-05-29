package com.github.nathnatica.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ExcelUtil {
    
    public static Workbook getWorkbook(String file) throws IOException {
        Workbook wb = null;
        if (StringUtils.endsWith(file, ".xlsx")) {
            wb = new XSSFWorkbook(new FileInputStream(new File(file)));
        } else if (StringUtils.endsWith(file, ".xls")) {
            wb = new HSSFWorkbook(new FileInputStream(new File(file)));
        }
        return wb;
    }
    
}
