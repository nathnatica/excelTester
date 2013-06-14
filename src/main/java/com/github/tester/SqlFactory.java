package com.github.tester;

import com.github.tester.sql.DeleteSql;
import com.github.tester.sql.ISql;
import com.github.tester.sql.InsertSql;
import com.github.tester.sql.SelectSql;
import com.github.tester.validator.Argument;

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
