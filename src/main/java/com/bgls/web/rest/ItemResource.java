package com.bgls.web.rest;

import com.bgls.domain.Item;
import com.bgls.repository.ItemRepository;
import com.bgls.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ForwardedHeaderUtils;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.bgls.domain.Item}.
 */
@RestController
@RequestMapping("/api/items")
@Transactional
public class ItemResource {

    private static final Logger log = LoggerFactory.getLogger(ItemResource.class);

    private static final String ENTITY_NAME = "item";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ItemRepository itemRepository;

    public ItemResource(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /**
     * {@code POST  /items} : Create a new item.
     *
     * @param item the item to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new item, or with status {@code 400 (Bad Request)} if the item has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<Item>> createItem(@RequestBody Item item) throws URISyntaxException {
        log.debug("REST request to save Item : {}", item);
        if (item.getId() != null) {
            throw new BadRequestAlertException("A new item cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return itemRepository
            .save(item)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/items/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /items/:id} : Updates an existing item.
     *
     * @param id the id of the item to save.
     * @param item the item to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated item,
     * or with status {@code 400 (Bad Request)} if the item is not valid,
     * or with status {@code 500 (Internal Server Error)} if the item couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Item>> updateItem(@PathVariable(value = "id", required = false) final Long id, @RequestBody Item item)
        throws URISyntaxException {
        log.debug("REST request to update Item : {}, {}", id, item);
        if (item.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, item.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return itemRepository
            .existsById(id)
            .flatMap(exists -> {
                if (Boolean.FALSE.equals(exists)) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return itemRepository
                    .save(item)
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
     * {@code PATCH  /items/:id} : Partial updates given fields of an existing item, field will ignore if it is null
     *
     * @param id the id of the item to save.
     * @param item the item to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated item,
     * or with status {@code 400 (Bad Request)} if the item is not valid,
     * or with status {@code 404 (Not Found)} if the item is not found,
     * or with status {@code 500 (Internal Server Error)} if the item couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Item>> partialUpdateItem(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Item item
    ) throws URISyntaxException {
        log.debug("REST request to partial update Item partially : {}, {}", id, item);
        if (item.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, item.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return itemRepository
            .existsById(id)
            .flatMap(exists -> {
                if (Boolean.FALSE.equals(exists)) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Item> result = itemRepository.findById(item.getId()).flatMap(itemRepository::save);

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
     * {@code GET  /items} : get all the items.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of items in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<Item>>> getAllItems(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload,
        @RequestParam(name = "ownerId", required = false) Long ownerId,
        @RequestParam(name = "lendedToId", required = false) Long lendedToId,
        @RequestParam(name = "lendedTo", required = false) String lendedToLogin,
        @RequestParam(name = "gameId", required = false) Long gameId,
        @RequestParam(name = "game", required = false) String gameName,
        @RequestParam(name = "consoleId", required = false) Long consoleId
    ) {
        log.debug(
            "REST request to get a page of Items with filters: ownerId={}, lendedToId={}, lendedToLogin={}, gameId={}, gameName={}, consoleId={}",
            ownerId,
            lendedToId,
            lendedToLogin,
            gameId,
            gameName,
            consoleId
        );

        boolean hasFilters =
            ownerId != null || lendedToId != null || lendedToLogin != null || gameId != null || gameName != null || consoleId != null;

        if (!hasFilters) {
            if (eagerload) {
                return itemRepository
                    .count()
                    .zipWith(itemRepository.findAllWithEagerRelationships(pageable).collectList())
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
            } else {
                return itemRepository
                    .count()
                    .zipWith(itemRepository.findAllBy(pageable).collectList())
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
        }

        String sortField = "id";
        String sortDirection = "ASC";

        if (pageable.getSort().isSorted()) {
            Sort.Order order = pageable.getSort().iterator().next();
            sortField = order.getProperty();
            sortDirection = order.getDirection().name();
        }

        long offset = pageable.getOffset();
        int limit = pageable.getPageSize();

        return itemRepository
            .countWithFilters(ownerId, lendedToId, lendedToLogin, gameId, gameName, consoleId)
            .zipWith(
                itemRepository
                    .findIdsWithFilters(
                        ownerId,
                        lendedToId,
                        lendedToLogin,
                        gameId,
                        gameName,
                        consoleId,
                        sortField,
                        sortDirection,
                        limit,
                        offset
                    )
                    .flatMap(id -> eagerload ? itemRepository.findOneWithEagerRelationships(id) : itemRepository.findById(id))
                    .collectList()
            )
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
     * {@code GET  /items/:id} : get the "id" item.
     *
     * @param id the id of the item to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the item, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Item>> getItem(@PathVariable("id") Long id) {
        log.debug("REST request to get Item : {}", id);
        Mono<Item> item = itemRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(item);
    }

    /**
     * {@code DELETE  /items/:id} : delete the "id" item.
     *
     * @param id the id of the item to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteItem(@PathVariable("id") Long id) {
        log.debug("REST request to delete Item : {}", id);
        return itemRepository
            .deleteById(id)
            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }
}
