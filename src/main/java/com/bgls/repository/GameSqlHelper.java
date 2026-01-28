package com.bgls.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class GameSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("name", table, columnPrefix + "_name"));
        columns.add(Column.aliased("cover", table, columnPrefix + "_cover"));
        columns.add(Column.aliased("cover_content_type", table, columnPrefix + "_cover_content_type"));

        columns.add(Column.aliased("console_id", table, columnPrefix + "_console_id"));
        return columns;
    }
}
