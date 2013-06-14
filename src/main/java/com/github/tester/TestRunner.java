package com.github.tester;

import com.github.tester.model.TableEntity;
import com.github.tester.util.ExcelUtil;
import com.github.tester.util.TimeUtil;
import com.github.tester.validator.Argument;
import com.github.tester.validator.InputData;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.List;

public class TestRunner {
    final static Logger logger = LoggerFactory.getLogger(TestRunner.class);
    
    public static void main(String[] args) throws Exception {

        if (!Argument.isValid(args)) return;

        String file = args[0];
        setLog(file);

        Workbook wb = ExcelUtil.getWorkbook(file);
        List<TableEntity> tables = ExcelReader.getTableData(wb);

        if (!InputData.isValid(tables)) return;

        DAO dao = new DAO();
        dao.execute(tables, Argument.action);
        
        if (Argument.isCheckAction()) {
            InputData.check(tables);
            ExcelWriter.writeActuals(tables, wb);
            ExcelWriter.writeCheckResults(tables, wb);
            ExcelUtil.writeFile(wb, file);
        }
        
        logger.info("TestRunner ended successfully");
    }

    private static void setLog(String file) throws IOException {
        String timestamp = TimeUtil.getYYYYMMDDHH24MISS();
        MDC.put("logname", timestamp + "_" + file.substring(file.lastIndexOf("\\") + 1, file.length() - 1) + "_" + Argument.action.getValue());
//        Files.copy(new File(file), new File(file.replace(".xls", "_backup_" + timestamp + ".xls")));
    }
    
}