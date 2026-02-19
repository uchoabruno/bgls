package com.bgls.repository;

import com.bgls.domain.Console;
import com.bgls.domain.Game;
import com.bgls.domain.Item;
import com.bgls.repository.rowmapper.GameRowMapper;
import com.bgls.repository.rowmapper.ItemRowMapper;
import com.bgls.repository.rowmapper.UserRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.sql.*;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC custom repository implementation for the Item entity.
 */
@SuppressWarnings("unused")
class ItemRepositoryInternalImpl extends SimpleR2dbcRepository<Item, Long> implements ItemRepositoryInternal {

    public static final String CONSOLE = "console";
    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final UserRowMapper userMapper;
    private final GameRowMapper gameMapper;
    private final ItemRowMapper itemMapper;

    private static final Table entityTable = Table.aliased("item", EntityManager.ENTITY_ALIAS);
    public static final String OWNER = "owner";
    private static final Table ownerTable = Table.aliased("jhi_user", OWNER);
    public static final String LENDED_TO = "lendedTo";
    private static final Table lendedToTable = Table.aliased("jhi_user", LENDED_TO);
    public static final String GAME = "game";
    private static final Table gameTable = Table.aliased(GAME, GAME);
    private final Table consoleTable = Table.aliased(CONSOLE, CONSOLE);

    public ItemRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        UserRowMapper userMapper,
        GameRowMapper gameMapper,
        ItemRowMapper itemMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation<>(
                (RelationalPersistentEntity<Item>) converter.getMappingContext().getRequiredPersistentEntity(Item.class)
            ),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.userMapper = userMapper;
        this.gameMapper = gameMapper;
        this.itemMapper = itemMapper;
    }

    @Override
    public Flux<Item> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Item> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = ItemSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(UserSqlHelper.getColumns(ownerTable, OWNER));
        columns.addAll(UserSqlHelper.getColumns(lendedToTable, LENDED_TO));
        columns.addAll(GameSqlHelper.getColumns(gameTable, GAME));
        columns.addAll(ConsoleSqlHelper.getColumns(consoleTable, CONSOLE));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(ownerTable)
            .on(Column.create("owner_id", entityTable))
            .equals(Column.create("id", ownerTable))
            .leftOuterJoin(lendedToTable)
            .on(Column.create("lended_to_id", entityTable))
            .equals(Column.create("id", lendedToTable))
            .leftOuterJoin(gameTable)
            .on(Column.create("game_id", entityTable))
            .equals(Column.create("id", gameTable))
            .leftOuterJoin(consoleTable)
            .on(Column.create("console_id", gameTable))
            .equals(Column.create("id", consoleTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269

        Sort originalSort = pageable.getSort();
        List<Sort.Order> newOrders = new ArrayList<>();

        for (Sort.Order order : originalSort) {
            switch (order.getProperty()) {
                case "game.console.name" -> newOrders.add(new Sort.Order(order.getDirection(), "console.name"));
                case "game.name" -> newOrders.add(new Sort.Order(order.getDirection(), "game.name"));
                case "owner.login" -> newOrders.add(new Sort.Order(order.getDirection(), "owner.login"));
                case "lendedTo.login" -> newOrders.add(new Sort.Order(order.getDirection(), "lendedTo.login"));
                default -> newOrders.add(order);
            }
        }
        Sort modifiedSort = Sort.by(newOrders);
        Pageable modifiedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), modifiedSort);

        String select = entityManager.createSelect(selectFrom, Item.class, modifiedPageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Item> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Item> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Mono<Item> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<Item> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<Item> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private Item process(Row row, RowMetadata metadata) {
        Item entity = itemMapper.apply(row, "e");
        entity.setOwner(userMapper.apply(row, OWNER));
        entity.setLendedTo(userMapper.apply(row, LENDED_TO));

        Game game = GameSqlHelper.extract(row, GAME);
        if (game != null) {
            Console console = ConsoleSqlHelper.extract(row, CONSOLE);
            game.setConsole(console);
        }
        entity.setGame(game);

        return entity;
    }
}
