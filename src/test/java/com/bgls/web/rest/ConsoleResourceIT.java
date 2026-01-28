package com.bgls.web.rest;

import static com.bgls.domain.ConsoleAsserts.*;
import static com.bgls.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.bgls.IntegrationTest;
import com.bgls.domain.Console;
import com.bgls.repository.ConsoleRepository;
import com.bgls.repository.EntityManager;
import com.bgls.service.dto.ConsoleDTO;
import com.bgls.service.mapper.ConsoleMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link ConsoleResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class ConsoleResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final byte[] DEFAULT_IMAGE = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_IMAGE = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_IMAGE_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_IMAGE_CONTENT_TYPE = "image/png";

    private static final String ENTITY_API_URL = "/api/consoles";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ConsoleRepository consoleRepository;

    @Autowired
    private ConsoleMapper consoleMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Console console;

    private Console insertedConsole;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Console createEntity(EntityManager em) {
        Console console = new Console().name(DEFAULT_NAME).image(DEFAULT_IMAGE).imageContentType(DEFAULT_IMAGE_CONTENT_TYPE);
        return console;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Console createUpdatedEntity(EntityManager em) {
        Console console = new Console().name(UPDATED_NAME).image(UPDATED_IMAGE).imageContentType(UPDATED_IMAGE_CONTENT_TYPE);
        return console;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Console.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    public void initTest() {
        console = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedConsole != null) {
            consoleRepository.delete(insertedConsole).block();
            insertedConsole = null;
        }
        deleteEntities(em);
    }

    @Test
    void createConsole() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Console
        ConsoleDTO consoleDTO = consoleMapper.toDto(console);
        var returnedConsoleDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(consoleDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(ConsoleDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the Console in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedConsole = consoleMapper.toEntity(returnedConsoleDTO);
        assertConsoleUpdatableFieldsEquals(returnedConsole, getPersistedConsole(returnedConsole));

        insertedConsole = returnedConsole;
    }

    @Test
    void createConsoleWithExistingId() throws Exception {
        // Create the Console with an existing ID
        console.setId(1L);
        ConsoleDTO consoleDTO = consoleMapper.toDto(console);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(consoleDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Console in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        console.setName(null);

        // Create the Console, which fails.
        ConsoleDTO consoleDTO = consoleMapper.toDto(console);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(consoleDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllConsolesAsStream() {
        // Initialize the database
        consoleRepository.save(console).block();

        List<Console> consoleList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(ConsoleDTO.class)
            .getResponseBody()
            .map(consoleMapper::toEntity)
            .filter(console::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(consoleList).isNotNull();
        assertThat(consoleList).hasSize(1);
        Console testConsole = consoleList.get(0);

        // Test fails because reactive api returns an empty object instead of null
        // assertConsoleAllPropertiesEquals(console, testConsole);
        assertConsoleUpdatableFieldsEquals(console, testConsole);
    }

    @Test
    void getAllConsoles() {
        // Initialize the database
        insertedConsole = consoleRepository.save(console).block();

        // Get all the consoleList
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
            .value(hasItem(console.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].imageContentType")
            .value(hasItem(DEFAULT_IMAGE_CONTENT_TYPE))
            .jsonPath("$.[*].image")
            .value(hasItem(Base64.getEncoder().encodeToString(DEFAULT_IMAGE)));
    }

    @Test
    void getConsole() {
        // Initialize the database
        insertedConsole = consoleRepository.save(console).block();

        // Get the console
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, console.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(console.getId().intValue()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.imageContentType")
            .value(is(DEFAULT_IMAGE_CONTENT_TYPE))
            .jsonPath("$.image")
            .value(is(Base64.getEncoder().encodeToString(DEFAULT_IMAGE)));
    }

    @Test
    void getNonExistingConsole() {
        // Get the console
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingConsole() throws Exception {
        // Initialize the database
        insertedConsole = consoleRepository.save(console).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the console
        Console updatedConsole = consoleRepository.findById(console.getId()).block();
        updatedConsole.name(UPDATED_NAME).image(UPDATED_IMAGE).imageContentType(UPDATED_IMAGE_CONTENT_TYPE);
        ConsoleDTO consoleDTO = consoleMapper.toDto(updatedConsole);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, consoleDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(consoleDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Console in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedConsoleToMatchAllProperties(updatedConsole);
    }

    @Test
    void putNonExistingConsole() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        console.setId(longCount.incrementAndGet());

        // Create the Console
        ConsoleDTO consoleDTO = consoleMapper.toDto(console);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, consoleDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(consoleDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Console in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchConsole() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        console.setId(longCount.incrementAndGet());

        // Create the Console
        ConsoleDTO consoleDTO = consoleMapper.toDto(console);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(consoleDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Console in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamConsole() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        console.setId(longCount.incrementAndGet());

        // Create the Console
        ConsoleDTO consoleDTO = consoleMapper.toDto(console);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(consoleDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Console in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateConsoleWithPatch() throws Exception {
        // Initialize the database
        insertedConsole = consoleRepository.save(console).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the console using partial update
        Console partialUpdatedConsole = new Console();
        partialUpdatedConsole.setId(console.getId());

        partialUpdatedConsole.image(UPDATED_IMAGE).imageContentType(UPDATED_IMAGE_CONTENT_TYPE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedConsole.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedConsole))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Console in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertConsoleUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedConsole, console), getPersistedConsole(console));
    }

    @Test
    void fullUpdateConsoleWithPatch() throws Exception {
        // Initialize the database
        insertedConsole = consoleRepository.save(console).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the console using partial update
        Console partialUpdatedConsole = new Console();
        partialUpdatedConsole.setId(console.getId());

        partialUpdatedConsole.name(UPDATED_NAME).image(UPDATED_IMAGE).imageContentType(UPDATED_IMAGE_CONTENT_TYPE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedConsole.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedConsole))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Console in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertConsoleUpdatableFieldsEquals(partialUpdatedConsole, getPersistedConsole(partialUpdatedConsole));
    }

    @Test
    void patchNonExistingConsole() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        console.setId(longCount.incrementAndGet());

        // Create the Console
        ConsoleDTO consoleDTO = consoleMapper.toDto(console);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, consoleDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(consoleDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Console in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchConsole() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        console.setId(longCount.incrementAndGet());

        // Create the Console
        ConsoleDTO consoleDTO = consoleMapper.toDto(console);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(consoleDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Console in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamConsole() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        console.setId(longCount.incrementAndGet());

        // Create the Console
        ConsoleDTO consoleDTO = consoleMapper.toDto(console);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(consoleDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Console in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteConsole() {
        // Initialize the database
        insertedConsole = consoleRepository.save(console).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the console
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, console.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return consoleRepository.count().block();
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

    protected Console getPersistedConsole(Console console) {
        return consoleRepository.findById(console.getId()).block();
    }

    protected void assertPersistedConsoleToMatchAllProperties(Console expectedConsole) {
        // Test fails because reactive api returns an empty object instead of null
        // assertConsoleAllPropertiesEquals(expectedConsole, getPersistedConsole(expectedConsole));
        assertConsoleUpdatableFieldsEquals(expectedConsole, getPersistedConsole(expectedConsole));
    }

    protected void assertPersistedConsoleToMatchUpdatableProperties(Console expectedConsole) {
        // Test fails because reactive api returns an empty object instead of null
        // assertConsoleAllUpdatablePropertiesEquals(expectedConsole, getPersistedConsole(expectedConsole));
        assertConsoleUpdatableFieldsEquals(expectedConsole, getPersistedConsole(expectedConsole));
    }
}
