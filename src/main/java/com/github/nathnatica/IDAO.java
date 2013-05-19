package com.github.nathnatica;

import com.github.nathnatica.model.RecordEntity;
import com.github.nathnatica.model.TableEntity;
import com.github.nathnatica.validator.Argument;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface IDAO {
    String getSqlFor(TableEntity table, Argument.Action action);
    boolean fillInsertSql(PreparedStatement preparedStatement, RecordEntity r) throws Exception;
    boolean fillDeleteSql(PreparedStatement preparedStatement, RecordEntity r) throws Exception;
    boolean fillSql(PreparedStatement preparedStatement, RecordEntity r, Argument.Action action) throws Exception;
}
