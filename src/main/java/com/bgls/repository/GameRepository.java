package com.bgls.repository;

import com.bgls.domain.Game;
import com.bgls.domain.criteria.GameCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Game entity.
 */
@SuppressWarnings("unused")
@Repository
public interface GameRepository extends ReactiveCrudRepository<Game, Long>, GameRepositoryInternal {
    Flux<Game> findAllBy(Pageable pageable);

    @Override
    Mono<Game> findOneWithEagerRelationships(Long id);

    @Override
    Flux<Game> findAllWithEagerRelationships();

    @Override
    Flux<Game> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM game entity WHERE entity.console_id = :id")
    Flux<Game> findByConsole(Long id);

    @Query("SELECT * FROM game entity WHERE entity.console_id IS NULL")
    Flux<Game> findAllWhereConsoleIsNull();

    @Override
    <S extends Game> Mono<S> save(S entity);

    @Override
    Flux<Game> findAll();

    @Override
    Mono<Game> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface GameRepositoryInternal {
    <S extends Game> Mono<S> save(S entity);

    Flux<Game> findAllBy(Pageable pageable);

    Flux<Game> findAll();

    Mono<Game> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Game> findAllBy(Pageable pageable, Criteria criteria);
    Flux<Game> findByCriteria(GameCriteria criteria, Pageable pageable);

    Mono<Long> countByCriteria(GameCriteria criteria);

    Mono<Game> findOneWithEagerRelationships(Long id);

    Flux<Game> findAllWithEagerRelationships();

    Flux<Game> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
