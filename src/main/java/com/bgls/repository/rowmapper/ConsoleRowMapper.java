package com.bgls.repository.rowmapper;

import com.bgls.domain.Console;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Console}, with proper type conversions.
 */
@Service
public class ConsoleRowMapper implements BiFunction<Row, String, Console> {

    private final ColumnConverter converter;

    public ConsoleRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Console} stored in the database.
     */
    @Override
    public Console apply(Row row, String prefix) {
        Console entity = new Console();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        entity.setImageContentType(converter.fromRow(row, prefix + "_image_content_type", String.class));
        entity.setImage(converter.fromRow(row, prefix + "_image", byte[].class));
        return entity;
    }
}
