package com.bgls.repository;

import com.bgls.domain.Console;
import io.r2dbc.spi.Row;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class ConsoleSqlHelper {

    private ConsoleSqlHelper() {}

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("name", table, columnPrefix + "_name"));
        columns.add(Column.aliased("image", table, columnPrefix + "_image"));
        columns.add(Column.aliased("image_content_type", table, columnPrefix + "_image_content_type"));

        return columns;
    }

    public static Console extract(Row row, String columnPrefix) {
        if (row.get(columnPrefix + "_id", Long.class) == null) {
            return null;
        }

        Console console = new Console();
        console.setId(row.get(columnPrefix + "_id", Long.class));
        console.setName(row.get(columnPrefix + "_name", String.class));
        console.setImage(row.get(columnPrefix + "_image", byte[].class));
        console.setImageContentType(row.get(columnPrefix + "_image_content_type", String.class));
        return console;
    }
}
