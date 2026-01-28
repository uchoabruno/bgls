package com.bgls.service;

import com.bgls.domain.criteria.GameCriteria;
import com.bgls.service.dto.GameDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.bgls.domain.Game}.
 */
public interface GameService {
    /**
     * Save a game.
     *
     * @param gameDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<GameDTO> save(GameDTO gameDTO);

    /**
     * Updates a game.
     *
     * @param gameDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<GameDTO> update(GameDTO gameDTO);

    /**
     * Partially updates a game.
     *
     * @param gameDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<GameDTO> partialUpdate(GameDTO gameDTO);
    /**
     * Find games by criteria.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<GameDTO> findByCriteria(GameCriteria criteria, Pageable pageable);

    /**
     * Find the count of games by criteria.
     * @param criteria filtering criteria
     * @return the count of games
     */
    public Mono<Long> countByCriteria(GameCriteria criteria);

    /**
     * Get all the games with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<GameDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Returns the number of games available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" game.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<GameDTO> findOne(Long id);

    /**
     * Delete the "id" game.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);
}
