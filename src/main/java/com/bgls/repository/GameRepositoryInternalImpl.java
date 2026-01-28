package com.bgls.repository;

import com.bgls.domain.Game;
import com.bgls.domain.criteria.GameCriteria;
import com.bgls.repository.rowmapper.ColumnConverter;
import com.bgls.repository.rowmapper.ConsoleRowMapper;
import com.bgls.repository.rowmapper.GameRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.service.ConditionBuilder;

/**
 * Spring Data R2DBC custom repository implementation for the Game entity.
 */
@SuppressWarnings("unused")
class GameRepositoryInternalImpl extends SimpleR2dbcRepository<Game, Long> implements GameRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final ConsoleRowMapper consoleMapper;
    private final GameRowMapper gameMapper;
    private final ColumnConverter columnConverter;

    private static final Table entityTable = Table.aliased("game", EntityManager.ENTITY_ALIAS);
    private static final Table consoleTable = Table.aliased("console", "console");

    public GameRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        ConsoleRowMapper consoleMapper,
        GameRowMapper gameMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter,
        ColumnConverter columnConverter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Game.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.consoleMapper = consoleMapper;
        this.gameMapper = gameMapper;
        this.columnConverter = columnConverter;
    }

    @Override
    public Flux<Game> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Game> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = GameSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(ConsoleSqlHelper.getColumns(consoleTable, "console"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(consoleTable)
            .on(Column.create("console_id", entityTable))
            .equals(Column.create("id", consoleTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Game.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Game> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Game> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Mono<Game> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<Game> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<Game> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private Game process(Row row, RowMetadata metadata) {
        Game entity = gameMapper.apply(row, "e");
        entity.setConsole(consoleMapper.apply(row, "console"));
        return entity;
    }

    @Override
    public <S extends Game> Mono<S> save(S entity) {
        return super.save(entity);
    }

    @Override
    public Flux<Game> findByCriteria(GameCriteria gameCriteria, Pageable page) {
        return createQuery(page, buildConditions(gameCriteria)).all();
    }

    @Override
    public Mono<Long> countByCriteria(GameCriteria criteria) {
        return findByCriteria(criteria, null)
            .collectList()
            .map(collectedList -> collectedList != null ? (long) collectedList.size() : (long) 0);
    }

    private Condition buildConditions(GameCriteria criteria) {
        ConditionBuilder builder = new ConditionBuilder(this.columnConverter);
        List<Condition> allConditions = new ArrayList<Condition>();
        if (criteria != null) {
            if (criteria.getId() != null) {
                builder.buildFilterConditionForField(criteria.getId(), entityTable.column("id"));
            }
            if (criteria.getName() != null) {
                builder.buildFilterConditionForField(criteria.getName(), entityTable.column("name"));
            }
            if (criteria.getConsoleId() != null) {
                builder.buildFilterConditionForField(criteria.getConsoleId(), consoleTable.column("id"));
            }
        }
        return builder.buildConditions();
    }
}
