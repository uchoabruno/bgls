package com.bgls.web.rest;

import com.bgls.domain.criteria.GameCriteria;
import com.bgls.repository.GameRepository;
import com.bgls.service.GameService;
import com.bgls.service.dto.GameDTO;
import com.bgls.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ForwardedHeaderUtils;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.bgls.domain.Game}.
 */
@RestController
@RequestMapping("/api/games")
public class GameResource {

    private static final Logger log = LoggerFactory.getLogger(GameResource.class);

    private static final String ENTITY_NAME = "game";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final GameService gameService;

    private final GameRepository gameRepository;

    public GameResource(GameService gameService, GameRepository gameRepository) {
        this.gameService = gameService;
        this.gameRepository = gameRepository;
    }

    /**
     * {@code POST  /games} : Create a new game.
     *
     * @param gameDTO the gameDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new gameDTO, or with status {@code 400 (Bad Request)} if the game has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<GameDTO>> createGame(@Valid @RequestBody GameDTO gameDTO) throws URISyntaxException {
        log.debug("REST request to save Game : {}", gameDTO);
        if (gameDTO.getId() != null) {
            throw new BadRequestAlertException("A new game cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return gameService
            .save(gameDTO)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/games/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /games/:id} : Updates an existing game.
     *
     * @param id the id of the gameDTO to save.
     * @param gameDTO the gameDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated gameDTO,
     * or with status {@code 400 (Bad Request)} if the gameDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the gameDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<GameDTO>> updateGame(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody GameDTO gameDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Game : {}, {}", id, gameDTO);
        if (gameDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, gameDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return gameRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return gameService
                    .update(gameDTO)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(
                        result ->
                            ResponseEntity.ok()
                                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                                .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /games/:id} : Partial updates given fields of an existing game, field will ignore if it is null
     *
     * @param id the id of the gameDTO to save.
     * @param gameDTO the gameDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated gameDTO,
     * or with status {@code 400 (Bad Request)} if the gameDTO is not valid,
     * or with status {@code 404 (Not Found)} if the gameDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the gameDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<GameDTO>> partialUpdateGame(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody GameDTO gameDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Game partially : {}, {}", id, gameDTO);
        if (gameDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, gameDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return gameRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<GameDTO> result = gameService.partialUpdate(gameDTO);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(
                        res ->
                            ResponseEntity.ok()
                                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                                .body(res)
                    );
            });
    }

    /**
     * {@code GET  /games} : get all the games.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of games in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<GameDTO>>> getAllGames(
        GameCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to get Games by criteria: {}", criteria);
        return gameService
            .countByCriteria(criteria)
            .zipWith(gameService.findByCriteria(criteria, pageable).collectList())
            .map(
                countWithEntities ->
                    ResponseEntity.ok()
                        .headers(
                            PaginationUtil.generatePaginationHttpHeaders(
                                ForwardedHeaderUtils.adaptFromForwardedHeaders(request.getURI(), request.getHeaders()),
                                new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                            )
                        )
                        .body(countWithEntities.getT2())
            );
    }

    /**
     * {@code GET  /games/count} : count all the games.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public Mono<ResponseEntity<Long>> countGames(GameCriteria criteria) {
        log.debug("REST request to count Games by criteria: {}", criteria);
        return gameService.countByCriteria(criteria).map(count -> ResponseEntity.status(HttpStatus.OK).body(count));
    }

    /**
     * {@code GET  /games/:id} : get the "id" game.
     *
     * @param id the id of the gameDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the gameDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<GameDTO>> getGame(@PathVariable("id") Long id) {
        log.debug("REST request to get Game : {}", id);
        Mono<GameDTO> gameDTO = gameService.findOne(id);
        return ResponseUtil.wrapOrNotFound(gameDTO);
    }

    /**
     * {@code DELETE  /games/:id} : delete the "id" game.
     *
     * @param id the id of the gameDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<Void>> deleteGame(@PathVariable("id") Long id) {
        log.debug("REST request to delete Game : {}", id);
        return gameService
            .delete(id)
            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }
}
