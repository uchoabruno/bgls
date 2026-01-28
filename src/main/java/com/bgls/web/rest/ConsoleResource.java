package com.bgls.web.rest;

import com.bgls.repository.ConsoleRepository;
import com.bgls.service.ConsoleService;
import com.bgls.service.dto.ConsoleDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.bgls.domain.Console}.
 */
@RestController
@RequestMapping("/api/consoles")
public class ConsoleResource {

    private static final Logger log = LoggerFactory.getLogger(ConsoleResource.class);

    private static final String ENTITY_NAME = "console";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ConsoleService consoleService;

    private final ConsoleRepository consoleRepository;

    public ConsoleResource(ConsoleService consoleService, ConsoleRepository consoleRepository) {
        this.consoleService = consoleService;
        this.consoleRepository = consoleRepository;
    }

    /**
     * {@code POST  /consoles} : Create a new console.
     *
     * @param consoleDTO the consoleDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new consoleDTO, or with status {@code 400 (Bad Request)} if the console has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<ConsoleDTO>> createConsole(@Valid @RequestBody ConsoleDTO consoleDTO) throws URISyntaxException {
        log.debug("REST request to save Console : {}", consoleDTO);
        if (consoleDTO.getId() != null) {
            throw new BadRequestAlertException("A new console cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return consoleService
            .save(consoleDTO)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/consoles/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /consoles/:id} : Updates an existing console.
     *
     * @param id the id of the consoleDTO to save.
     * @param consoleDTO the consoleDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated consoleDTO,
     * or with status {@code 400 (Bad Request)} if the consoleDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the consoleDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<ConsoleDTO>> updateConsole(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ConsoleDTO consoleDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Console : {}, {}", id, consoleDTO);
        if (consoleDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, consoleDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return consoleRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return consoleService
                    .update(consoleDTO)
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
     * {@code PATCH  /consoles/:id} : Partial updates given fields of an existing console, field will ignore if it is null
     *
     * @param id the id of the consoleDTO to save.
     * @param consoleDTO the consoleDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated consoleDTO,
     * or with status {@code 400 (Bad Request)} if the consoleDTO is not valid,
     * or with status {@code 404 (Not Found)} if the consoleDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the consoleDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<ConsoleDTO>> partialUpdateConsole(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ConsoleDTO consoleDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Console partially : {}, {}", id, consoleDTO);
        if (consoleDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, consoleDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return consoleRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<ConsoleDTO> result = consoleService.partialUpdate(consoleDTO);

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
     * {@code GET  /consoles} : get all the consoles.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of consoles in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<ConsoleDTO>> getAllConsoles() {
        log.debug("REST request to get all Consoles");
        return consoleService.findAll().collectList();
    }

    /**
     * {@code GET  /consoles} : get all the consoles as a stream.
     * @return the {@link Flux} of consoles.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<ConsoleDTO> getAllConsolesAsStream() {
        log.debug("REST request to get all Consoles as a stream");
        return consoleService.findAll();
    }

    /**
     * {@code GET  /consoles/:id} : get the "id" console.
     *
     * @param id the id of the consoleDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the consoleDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ConsoleDTO>> getConsole(@PathVariable("id") Long id) {
        log.debug("REST request to get Console : {}", id);
        Mono<ConsoleDTO> consoleDTO = consoleService.findOne(id);
        return ResponseUtil.wrapOrNotFound(consoleDTO);
    }

    /**
     * {@code DELETE  /consoles/:id} : delete the "id" console.
     *
     * @param id the id of the consoleDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteConsole(@PathVariable("id") Long id) {
        log.debug("REST request to delete Console : {}", id);
        return consoleService
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
