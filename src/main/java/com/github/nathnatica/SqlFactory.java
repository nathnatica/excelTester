package com.github.nathnatica;

import com.github.nathnatica.sql.DeleteSql;
import com.github.nathnatica.sql.ISql;
import com.github.nathnatica.sql.InsertSql;
import com.github.nathnatica.sql.SelectSql;
import com.github.nathnatica.validator.Argument;

public class SqlFactory {
    public ISql getSqlFor(Argument.Action action) {
        if (Argument.Action.INSERT == action) {
            return new InsertSql();
        } else if (Argument.Action.DELETE == action) {
            return new DeleteSql();
        } else if (Argument.Action.CHECK == action) {
            return new SelectSql();
        }
        return null;
    }
}
