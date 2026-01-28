package com.bgls.service;

import com.bgls.service.dto.ConsoleDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.bgls.domain.Console}.
 */
public interface ConsoleService {
    /**
     * Save a console.
     *
     * @param consoleDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<ConsoleDTO> save(ConsoleDTO consoleDTO);

    /**
     * Updates a console.
     *
     * @param consoleDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<ConsoleDTO> update(ConsoleDTO consoleDTO);

    /**
     * Partially updates a console.
     *
     * @param consoleDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<ConsoleDTO> partialUpdate(ConsoleDTO consoleDTO);

    /**
     * Get all the consoles.
     *
     * @return the list of entities.
     */
    Flux<ConsoleDTO> findAll();

    /**
     * Returns the number of consoles available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" console.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<ConsoleDTO> findOne(Long id);

    /**
     * Delete the "id" console.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);
}
