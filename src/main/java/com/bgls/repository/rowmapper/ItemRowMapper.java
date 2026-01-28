package com.bgls.repository.rowmapper;

import com.bgls.domain.Item;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Item}, with proper type conversions.
 */
@Service
public class ItemRowMapper implements BiFunction<Row, String, Item> {

    private final ColumnConverter converter;

    public ItemRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Item} stored in the database.
     */
    @Override
    public Item apply(Row row, String prefix) {
        Item entity = new Item();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setOwnerId(converter.fromRow(row, prefix + "_owner_id", Long.class));
        entity.setLendedToId(converter.fromRow(row, prefix + "_lended_to_id", Long.class));
        entity.setGameId(converter.fromRow(row, prefix + "_game_id", Long.class));
        return entity;
    }
}
