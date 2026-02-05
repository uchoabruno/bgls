package com.bgls.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.bgls.domain.Game} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class GameDTO implements Serializable {

    private Long id;

    @NotNull(message = "must not be null")
    private String name;

    @Lob
    private byte[] cover;

    private String coverContentType;

    @NotNull(message = "must not be null")
    private ConsoleDTO console;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getCover() {
        return cover;
    }

    public void setCover(byte[] cover) {
        this.cover = cover;
    }

    public String getCoverContentType() {
        return coverContentType;
    }

    public void setCoverContentType(String coverContentType) {
        this.coverContentType = coverContentType;
    }

    public ConsoleDTO getConsole() {
        return console;
    }

    public void setConsole(ConsoleDTO console) {
        this.console = console;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GameDTO)) {
            return false;
        }

        GameDTO gameDTO = (GameDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, gameDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "GameDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", cover='" + getCover() + "'" +
            ", console=" + getConsole() +
            "}";
    }
}
