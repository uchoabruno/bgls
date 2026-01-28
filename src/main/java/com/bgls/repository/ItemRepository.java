package com.bgls.repository;

import com.bgls.domain.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
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
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Item> findAllBy(Pageable pageable, Criteria criteria);

    Mono<Item> findOneWithEagerRelationships(Long id);

    Flux<Item> findAllWithEagerRelationships();

    Flux<Item> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
