package com.bgls.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Item.
 */
@Table("item")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @Transient
    private User owner;

    @Transient
    private User lendedTo;

    @Transient
    @JsonIgnoreProperties(value = { "console", "items" }, allowSetters = true)
    private Game game;

    @Column("owner_id")
    private Long ownerId;

    @Column("lended_to_id")
    private Long lendedToId;

    @Column("game_id")
    private Long gameId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Item id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return this.owner;
    }

    public void setOwner(User user) {
        this.owner = user;
        this.ownerId = user != null ? user.getId() : null;
    }

    public Item owner(User user) {
        this.setOwner(user);
        return this;
    }

    public User getLendedTo() {
        return this.lendedTo;
    }

    public void setLendedTo(User user) {
        this.lendedTo = user;
        this.lendedToId = user != null ? user.getId() : null;
    }

    public Item lendedTo(User user) {
        this.setLendedTo(user);
        return this;
    }

    public Game getGame() {
        return this.game;
    }

    public void setGame(Game game) {
        this.game = game;
        this.gameId = game != null ? game.getId() : null;
    }

    public Item game(Game game) {
        this.setGame(game);
        return this;
    }

    public Long getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(Long user) {
        this.ownerId = user;
    }

    public Long getLendedToId() {
        return this.lendedToId;
    }

    public void setLendedToId(Long user) {
        this.lendedToId = user;
    }

    public Long getGameId() {
        return this.gameId;
    }

    public void setGameId(Long game) {
        this.gameId = game;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Item)) {
            return false;
        }
        return getId() != null && getId().equals(((Item) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Item{" +
            "id=" + getId() +
            "}";
    }
}
