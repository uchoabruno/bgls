package com.bgls.service.impl;

import com.bgls.domain.criteria.GameCriteria;
import com.bgls.repository.GameRepository;
import com.bgls.service.GameService;
import com.bgls.service.dto.GameDTO;
import com.bgls.service.mapper.GameMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.bgls.domain.Game}.
 */
@Service
@Transactional
public class GameServiceImpl implements GameService {

    private static final Logger log = LoggerFactory.getLogger(GameServiceImpl.class);

    private final GameRepository gameRepository;

    private final GameMapper gameMapper;

    public GameServiceImpl(GameRepository gameRepository, GameMapper gameMapper) {
        this.gameRepository = gameRepository;
        this.gameMapper = gameMapper;
    }

    @Override
    public Mono<GameDTO> save(GameDTO gameDTO) {
        log.debug("Request to save Game : {}", gameDTO);
        return gameRepository.save(gameMapper.toEntity(gameDTO)).map(gameMapper::toDto);
    }

    @Override
    public Mono<GameDTO> update(GameDTO gameDTO) {
        log.debug("Request to update Game : {}", gameDTO);
        return gameRepository.save(gameMapper.toEntity(gameDTO)).map(gameMapper::toDto);
    }

    @Override
    public Mono<GameDTO> partialUpdate(GameDTO gameDTO) {
        log.debug("Request to partially update Game : {}", gameDTO);

        return gameRepository
            .findById(gameDTO.getId())
            .map(existingGame -> {
                gameMapper.partialUpdate(existingGame, gameDTO);

                return existingGame;
            })
            .flatMap(gameRepository::save)
            .map(gameMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<GameDTO> findByCriteria(GameCriteria criteria, Pageable pageable) {
        log.debug("Request to get all Games by Criteria");
        return gameRepository.findByCriteria(criteria, pageable).map(gameMapper::toDto);
    }

    /**
     * Find the count of games by criteria.
     * @param criteria filtering criteria
     * @return the count of games
     */
    public Mono<Long> countByCriteria(GameCriteria criteria) {
        log.debug("Request to get the count of all Games by Criteria");
        return gameRepository.countByCriteria(criteria);
    }

    public Flux<GameDTO> findAllWithEagerRelationships(Pageable pageable) {
        return gameRepository.findAllWithEagerRelationships(pageable).map(gameMapper::toDto);
    }

    public Mono<Long> countAll() {
        return gameRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<GameDTO> findOne(Long id) {
        log.debug("Request to get Game : {}", id);
        return gameRepository.findOneWithEagerRelationships(id).map(gameMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete Game : {}", id);
        return gameRepository.deleteById(id);
    }
}
