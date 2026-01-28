package com.bgls.service.impl;

import com.bgls.repository.ConsoleRepository;
import com.bgls.service.ConsoleService;
import com.bgls.service.dto.ConsoleDTO;
import com.bgls.service.mapper.ConsoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.bgls.domain.Console}.
 */
@Service
@Transactional
public class ConsoleServiceImpl implements ConsoleService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleServiceImpl.class);

    private final ConsoleRepository consoleRepository;

    private final ConsoleMapper consoleMapper;

    public ConsoleServiceImpl(ConsoleRepository consoleRepository, ConsoleMapper consoleMapper) {
        this.consoleRepository = consoleRepository;
        this.consoleMapper = consoleMapper;
    }

    @Override
    public Mono<ConsoleDTO> save(ConsoleDTO consoleDTO) {
        log.debug("Request to save Console : {}", consoleDTO);
        return consoleRepository.save(consoleMapper.toEntity(consoleDTO)).map(consoleMapper::toDto);
    }

    @Override
    public Mono<ConsoleDTO> update(ConsoleDTO consoleDTO) {
        log.debug("Request to update Console : {}", consoleDTO);
        return consoleRepository.save(consoleMapper.toEntity(consoleDTO)).map(consoleMapper::toDto);
    }

    @Override
    public Mono<ConsoleDTO> partialUpdate(ConsoleDTO consoleDTO) {
        log.debug("Request to partially update Console : {}", consoleDTO);

        return consoleRepository
            .findById(consoleDTO.getId())
            .map(existingConsole -> {
                consoleMapper.partialUpdate(existingConsole, consoleDTO);

                return existingConsole;
            })
            .flatMap(consoleRepository::save)
            .map(consoleMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<ConsoleDTO> findAll() {
        log.debug("Request to get all Consoles");
        return consoleRepository.findAll().map(consoleMapper::toDto);
    }

    public Mono<Long> countAll() {
        return consoleRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ConsoleDTO> findOne(Long id) {
        log.debug("Request to get Console : {}", id);
        return consoleRepository.findById(id).map(consoleMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete Console : {}", id);
        return consoleRepository.deleteById(id);
    }
}
