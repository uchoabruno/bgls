package com.bgls.web.rest;

import static com.bgls.domain.GameAsserts.*;
import static com.bgls.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.bgls.IntegrationTest;
import com.bgls.domain.Console;
import com.bgls.domain.Game;
import com.bgls.repository.ConsoleRepository;
import com.bgls.repository.EntityManager;
import com.bgls.repository.GameRepository;
import com.bgls.service.GameService;
import com.bgls.service.dto.GameDTO;
import com.bgls.service.mapper.GameMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

/**
 * Integration tests for the {@link GameResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class GameResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final byte[] DEFAULT_COVER = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_COVER = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_COVER_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_COVER_CONTENT_TYPE = "image/png";

    private static final String ENTITY_API_URL = "/api/games";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private GameRepository gameRepository;

    @Mock
    private GameRepository gameRepositoryMock;

    @Autowired
    private GameMapper gameMapper;

    @Mock
    private GameService gameServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Game game;

    private Game insertedGame;

    @Autowired
    private ConsoleRepository consoleRepository;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Game createEntity(EntityManager em) {
        Game game = new Game().name(DEFAULT_NAME).cover(DEFAULT_COVER).coverContentType(DEFAULT_COVER_CONTENT_TYPE);
        return game;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Game createUpdatedEntity(EntityManager em) {
        Game game = new Game().name(UPDATED_NAME).cover(UPDATED_COVER).coverContentType(UPDATED_COVER_CONTENT_TYPE);
        return game;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Game.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    public void initTest() {
        game = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedGame != null) {
            gameRepository.delete(insertedGame).block();
            insertedGame = null;
        }
        deleteEntities(em);
    }

    @Test
    void createGame() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Game
        GameDTO gameDTO = gameMapper.toDto(game);
        var returnedGameDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(gameDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(GameDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the Game in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedGame = gameMapper.toEntity(returnedGameDTO);
        assertGameUpdatableFieldsEquals(returnedGame, getPersistedGame(returnedGame));

        insertedGame = returnedGame;
    }

    @Test
    void createGameWithExistingId() throws Exception {
        // Create the Game with an existing ID
        game.setId(1L);
        GameDTO gameDTO = gameMapper.toDto(game);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(gameDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Game in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        game.setName(null);

        // Create the Game, which fails.
        GameDTO gameDTO = gameMapper.toDto(game);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(gameDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllGames() {
        // Initialize the database
        insertedGame = gameRepository.save(game).block();

        // Get all the gameList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(game.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].coverContentType")
            .value(hasItem(DEFAULT_COVER_CONTENT_TYPE))
            .jsonPath("$.[*].cover")
            .value(hasItem(Base64.getEncoder().encodeToString(DEFAULT_COVER)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllGamesWithEagerRelationshipsIsEnabled() {
        when(gameServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(gameServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllGamesWithEagerRelationshipsIsNotEnabled() {
        when(gameServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(gameRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getGame() {
        // Initialize the database
        insertedGame = gameRepository.save(game).block();

        // Get the game
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, game.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(game.getId().intValue()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.coverContentType")
            .value(is(DEFAULT_COVER_CONTENT_TYPE))
            .jsonPath("$.cover")
            .value(is(Base64.getEncoder().encodeToString(DEFAULT_COVER)));
    }

    @Test
    void getGamesByIdFiltering() {
        // Initialize the database
        insertedGame = gameRepository.save(game).block();

        Long id = game.getId();

        defaultGameFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultGameFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultGameFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    void getAllGamesByNameIsEqualToSomething() {
        // Initialize the database
        insertedGame = gameRepository.save(game).block();

        // Get all the gameList where name equals to
        defaultGameFiltering("name.equals=" + DEFAULT_NAME, "name.equals=" + UPDATED_NAME);
    }

    @Test
    void getAllGamesByNameIsInShouldWork() {
        // Initialize the database
        insertedGame = gameRepository.save(game).block();

        // Get all the gameList where name in
        defaultGameFiltering("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME, "name.in=" + UPDATED_NAME);
    }

    @Test
    void getAllGamesByNameIsNullOrNotNull() {
        // Initialize the database
        insertedGame = gameRepository.save(game).block();

        // Get all the gameList where name is not null
        defaultGameFiltering("name.specified=true", "name.specified=false");
    }

    @Test
    void getAllGamesByNameContainsSomething() {
        // Initialize the database
        insertedGame = gameRepository.save(game).block();

        // Get all the gameList where name contains
        defaultGameFiltering("name.contains=" + DEFAULT_NAME, "name.contains=" + UPDATED_NAME);
    }

    @Test
    void getAllGamesByNameNotContainsSomething() {
        // Initialize the database
        insertedGame = gameRepository.save(game).block();

        // Get all the gameList where name does not contain
        defaultGameFiltering("name.doesNotContain=" + UPDATED_NAME, "name.doesNotContain=" + DEFAULT_NAME);
    }

    @Test
    void getAllGamesByConsoleIsEqualToSomething() {
        Console console = ConsoleResourceIT.createEntity(em);
        consoleRepository.save(console).block();
        Long consoleId = console.getId();
        game.setConsoleId(consoleId);
        insertedGame = gameRepository.save(game).block();
        // Get all the gameList where console equals to consoleId
        defaultGameShouldBeFound("consoleId.equals=" + consoleId);

        // Get all the gameList where console equals to (consoleId + 1)
        defaultGameShouldNotBeFound("consoleId.equals=" + (consoleId + 1));
    }

    private void defaultGameFiltering(String shouldBeFound, String shouldNotBeFound) {
        defaultGameShouldBeFound(shouldBeFound);
        defaultGameShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultGameShouldBeFound(String filter) {
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc&" + filter)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(game.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].coverContentType")
            .value(hasItem(DEFAULT_COVER_CONTENT_TYPE))
            .jsonPath("$.[*].cover")
            .value(hasItem(Base64.getEncoder().encodeToString(DEFAULT_COVER)));

        // Check, that the count call also returns 1
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "/count?sort=id,desc&" + filter)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$")
            .value(is(1));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultGameShouldNotBeFound(String filter) {
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc&" + filter)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$")
            .isArray()
            .jsonPath("$")
            .isEmpty();

        // Check, that the count call also returns 0
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "/count?sort=id,desc&" + filter)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$")
            .value(is(0));
    }

    @Test
    void getNonExistingGame() {
        // Get the game
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingGame() throws Exception {
        // Initialize the database
        insertedGame = gameRepository.save(game).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the game
        Game updatedGame = gameRepository.findById(game.getId()).block();
        updatedGame.name(UPDATED_NAME).cover(UPDATED_COVER).coverContentType(UPDATED_COVER_CONTENT_TYPE);
        GameDTO gameDTO = gameMapper.toDto(updatedGame);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, gameDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(gameDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Game in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedGameToMatchAllProperties(updatedGame);
    }

    @Test
    void putNonExistingGame() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        game.setId(longCount.incrementAndGet());

        // Create the Game
        GameDTO gameDTO = gameMapper.toDto(game);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, gameDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(gameDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Game in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchGame() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        game.setId(longCount.incrementAndGet());

        // Create the Game
        GameDTO gameDTO = gameMapper.toDto(game);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(gameDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Game in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamGame() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        game.setId(longCount.incrementAndGet());

        // Create the Game
        GameDTO gameDTO = gameMapper.toDto(game);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(gameDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Game in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateGameWithPatch() throws Exception {
        // Initialize the database
        insertedGame = gameRepository.save(game).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the game using partial update
        Game partialUpdatedGame = new Game();
        partialUpdatedGame.setId(game.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedGame.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedGame))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Game in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertGameUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedGame, game), getPersistedGame(game));
    }

    @Test
    void fullUpdateGameWithPatch() throws Exception {
        // Initialize the database
        insertedGame = gameRepository.save(game).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the game using partial update
        Game partialUpdatedGame = new Game();
        partialUpdatedGame.setId(game.getId());

        partialUpdatedGame.name(UPDATED_NAME).cover(UPDATED_COVER).coverContentType(UPDATED_COVER_CONTENT_TYPE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedGame.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedGame))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Game in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertGameUpdatableFieldsEquals(partialUpdatedGame, getPersistedGame(partialUpdatedGame));
    }

    @Test
    void patchNonExistingGame() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        game.setId(longCount.incrementAndGet());

        // Create the Game
        GameDTO gameDTO = gameMapper.toDto(game);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, gameDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(gameDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Game in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchGame() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        game.setId(longCount.incrementAndGet());

        // Create the Game
        GameDTO gameDTO = gameMapper.toDto(game);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(gameDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Game in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamGame() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        game.setId(longCount.incrementAndGet());

        // Create the Game
        GameDTO gameDTO = gameMapper.toDto(game);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(gameDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Game in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteGame() {
        // Initialize the database
        insertedGame = gameRepository.save(game).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the game
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, game.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return gameRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Game getPersistedGame(Game game) {
        return gameRepository.findById(game.getId()).block();
    }

    protected void assertPersistedGameToMatchAllProperties(Game expectedGame) {
        // Test fails because reactive api returns an empty object instead of null
        // assertGameAllPropertiesEquals(expectedGame, getPersistedGame(expectedGame));
        assertGameUpdatableFieldsEquals(expectedGame, getPersistedGame(expectedGame));
    }

    protected void assertPersistedGameToMatchUpdatableProperties(Game expectedGame) {
        // Test fails because reactive api returns an empty object instead of null
        // assertGameAllUpdatablePropertiesEquals(expectedGame, getPersistedGame(expectedGame));
        assertGameUpdatableFieldsEquals(expectedGame, getPersistedGame(expectedGame));
    }
}
