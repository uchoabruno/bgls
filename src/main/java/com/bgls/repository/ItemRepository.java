package com.bgls.repository;

import com.bgls.domain.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Item entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ItemRepository extends ReactiveCrudRepository<Item, Long>, ItemRepositoryInternal {
    Flux<Item> findAllBy(Pageable pageable);

    @Override
    Mono<Item> findOneWithEagerRelationships(Long id);

    @Override
    Flux<Item> findAllWithEagerRelationships();

    @Override
    Flux<Item> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM item entity WHERE entity.owner_id = :id")
    Flux<Item> findByOwner(Long id);

    @Query("SELECT * FROM item entity WHERE entity.owner_id IS NULL")
    Flux<Item> findAllWhereOwnerIsNull();

    @Query("SELECT * FROM item entity WHERE entity.lended_to_id = :id")
    Flux<Item> findByLendedTo(Long id);

    @Query("SELECT * FROM item entity WHERE entity.lended_to_id IS NULL")
    Flux<Item> findAllWhereLendedToIsNull();

    @Query("SELECT * FROM item entity WHERE entity.game_id = :id")
    Flux<Item> findByGame(Long id);

    @Query("SELECT * FROM item entity WHERE entity.game_id IS NULL")
    Flux<Item> findAllWhereGameIsNull();

    @Query(
        "SELECT COUNT(i.id) FROM item i " +
        "LEFT JOIN jhi_user owner ON i.owner_id = owner.id " +
        "LEFT JOIN jhi_user lended_to ON i.lended_to_id = lended_to.id " +
        "LEFT JOIN game g ON i.game_id = g.id " +
        "LEFT JOIN console c ON g.console_id = c.id " +
        "WHERE (:ownerId IS NULL OR i.owner_id = :ownerId) " +
        "AND (:lendedToId IS NULL OR i.lended_to_id = :lendedToId) " +
        "AND (:lendedToLogin IS NULL OR LOWER(lended_to.login) LIKE LOWER(CONCAT('%', :lendedToLogin, '%'))) " +
        "AND (:gameId IS NULL OR i.game_id = :gameId) " +
        "AND (:gameName IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', :gameName, '%'))) " +
        "AND (:consoleId IS NULL OR g.console_id = :consoleId)"
    )
    Mono<Long> countWithFilters(
        @Param("ownerId") Long ownerId,
        @Param("lendedToId") Long lendedToId,
        @Param("lendedToLogin") String lendedToLogin,
        @Param("gameId") Long gameId,
        @Param("gameName") String gameName,
        @Param("consoleId") Long consoleId
    );

    @Query(
        "SELECT i.id FROM item i " +
        "LEFT JOIN jhi_user owner ON i.owner_id = owner.id " +
        "LEFT JOIN jhi_user lended_to ON i.lended_to_id = lended_to.id " +
        "LEFT JOIN game g ON i.game_id = g.id " +
        "LEFT JOIN console c ON g.console_id = c.id " +
        "WHERE (:ownerId IS NULL OR i.owner_id = :ownerId) " +
        "AND (:lendedToId IS NULL OR i.lended_to_id = :lendedToId) " +
        "AND (:lendedToLogin IS NULL OR LOWER(lended_to.login) LIKE LOWER(CONCAT('%', :lendedToLogin, '%'))) " +
        "AND (:gameId IS NULL OR i.game_id = :gameId) " +
        "AND (:gameName IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', :gameName, '%'))) " +
        "AND (:consoleId IS NULL OR g.console_id = :consoleId) " +
        "ORDER BY " +
        "CASE WHEN :sortField = 'id' AND :sortDirection = 'ASC' THEN i.id END ASC, " +
        "CASE WHEN :sortField = 'id' AND :sortDirection = 'DESC' THEN i.id END DESC, " +
        "CASE WHEN :sortField = 'owner.login' AND :sortDirection = 'ASC' THEN owner.login END ASC, " +
        "CASE WHEN :sortField = 'owner.login' AND :sortDirection = 'DESC' THEN owner.login END DESC, " +
        "CASE WHEN :sortField = 'lendedTo.login' AND :sortDirection = 'ASC' THEN lended_to.login END ASC, " +
        "CASE WHEN :sortField = 'lendedTo.login' AND :sortDirection = 'DESC' THEN lended_to.login END DESC, " +
        "CASE WHEN :sortField = 'game.name' AND :sortDirection = 'ASC' THEN g.name END ASC, " +
        "CASE WHEN :sortField = 'game.name' AND :sortDirection = 'DESC' THEN g.name END DESC, " +
        "CASE WHEN :sortField = 'game.console.name' AND :sortDirection = 'ASC' THEN c.name END ASC, " +
        "CASE WHEN :sortField = 'game.console.name' AND :sortDirection = 'DESC' THEN c.name END DESC, " +
        "i.id ASC " +
        "LIMIT :limit OFFSET :offset"
    )
    Flux<Long> findIdsWithFilters(
        @Param("ownerId") Long ownerId,
        @Param("lendedToId") Long lendedToId,
        @Param("lendedToLogin") String lendedToLogin,
        @Param("gameId") Long gameId,
        @Param("gameName") String gameName,
        @Param("consoleId") Long consoleId,
        @Param("sortField") String sortField,
        @Param("sortDirection") String sortDirection,
        @Param("limit") int limit,
        @Param("offset") long offset
    );

    @Override
    <S extends Item> Mono<S> save(S entity);

    @Override
    Flux<Item> findAll();

    @Override
    Mono<Item> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface ItemRepositoryInternal {
    <S extends Item> Mono<S> save(S entity);

    Flux<Item> findAllBy(Pageable pageable);

    Flux<Item> findAll();

    Mono<Item> findById(Long id);

    Mono<Item> findOneWithEagerRelationships(Long id);

    Flux<Item> findAllWithEagerRelationships();

    Flux<Item> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);

    Flux<Item> findAllWithEagerRelationshipsByIds(Flux<Long> ids);
}
