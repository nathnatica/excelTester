package com.github.nathnatica.sql;

import com.github.nathnatica.model.RecordEntity;
import com.github.nathnatica.model.TableEntity;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ISql {
    String getSqlFor(TableEntity table);
    boolean fillSql(PreparedStatement preparedStatement, RecordEntity r) throws Exception;
    void excuteSqlsForTable(PreparedStatement preparedStatement) throws SQLException;
    void postProcess(RecordEntity r);
    void validateExecutionCount() throws Exception;
}
