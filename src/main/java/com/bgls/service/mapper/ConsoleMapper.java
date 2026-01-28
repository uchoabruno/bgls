package com.bgls.service.mapper;

import com.bgls.domain.Console;
import com.bgls.service.dto.ConsoleDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Console} and its DTO {@link ConsoleDTO}.
 */
@Mapper(componentModel = "spring")
public interface ConsoleMapper extends EntityMapper<ConsoleDTO, Console> {}
