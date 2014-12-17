package com.github.tester;

import com.github.tester.model.TableEntity;
import com.github.tester.util.ExcelUtil;
import com.github.tester.util.ResourceBundleUtil;
import com.github.tester.util.TimeUtil;
import com.github.tester.validator.Argument;
import com.github.tester.validator.InputData;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class TestRunner {
    final static Logger logger = LoggerFactory.getLogger(TestRunner.class);
    
    public static void main(String[] args) throws Exception {

        String file = null;
        if (!Argument.isValid(args)) {
            if (StringUtils.equals(args[0], "gui")) {
                file = JOptionPane.showInputDialog(null, "Input file name : ");
                String action = JOptionPane.showInputDialog(null, "Action : ins[ert], che[ck], del[ete]");
                if (Argument.decideAction(action)) {
                    return;
                }
            } else {
                return;
            }
        } else {
             file = args[0];
        }
        
        ResourceBundleUtil.init("app");
        
        setLog(file);

        TableDefinitionLoader.loadTableDef();
        
        boolean result = false;
        for (int i = Argument.action.getStart(); i <= Argument.action.getEnd(); i++) {
            Argument.action.setCurrent(i);
            Workbook wb = ExcelUtil.getWorkbook(file);
            List<TableEntity> tables = ExcelReader.getTableData(wb);

            if (!InputData.isValid(tables)) return;

            DAO dao = new DAO();
            result = dao.execute(tables, Argument.action);

            if (Argument.isCheckAction()) {
                InputData.check(tables);
                ExcelWriter.writeActuals(tables, wb);
                ExcelWriter.writeCheckResults(tables, wb);
                ExcelUtil.writeFile(wb, file);
            }
            if (i < Argument.action.getEnd()) {
                Thread.sleep(1000*60*3);    
            }
        }
         
        if (result) {
            logger.info("TestRunner ended successfully");
            logger.info("##############################################");
            logger.info("####### {} work ended", Argument.action.getValue());
            logger.info("##############################################");
        } else {
            logger.error(">>>>> ERROR <<<<<");
            logger.error(">>>>> TestRunner ended with Errors, check the log file");
        }
    }

    private static void setLog(String file) throws IOException {
        String timestamp = TimeUtil.getYYYYMMDDHH24MISS();
        MDC.put("logname", timestamp + "_" + file.substring(file.lastIndexOf("\\") + 1, file.length() - 1) + "_" + Argument.action.getValue());
//        Files.copy(new File(file), new File(file.replace(".xls", "_backup_" + timestamp + ".xls")));
    }
    
}
