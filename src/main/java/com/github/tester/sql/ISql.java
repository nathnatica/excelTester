package com.github.tester.sql;

import com.github.tester.model.RecordEntity;
import com.github.tester.model.TableEntity;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ISql {
    String getSqlFor(TableEntity table);
    boolean fillSql(PreparedStatement preparedStatement, RecordEntity r) throws Exception;
    void excuteSqlsForTable(PreparedStatement preparedStatement) throws SQLException;
    void postProcess(RecordEntity r);
    void validateExecutionCount() throws Exception;
}
