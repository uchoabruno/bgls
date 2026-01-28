package com.bgls.service.mapper;

import com.bgls.domain.Console;
import com.bgls.domain.Game;
import com.bgls.service.dto.ConsoleDTO;
import com.bgls.service.dto.GameDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Game} and its DTO {@link GameDTO}.
 */
@Mapper(componentModel = "spring")
public interface GameMapper extends EntityMapper<GameDTO, Game> {
    @Mapping(target = "console", source = "console", qualifiedByName = "consoleName")
    GameDTO toDto(Game s);

    @Named("consoleName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ConsoleDTO toDtoConsoleName(Console console);
}
