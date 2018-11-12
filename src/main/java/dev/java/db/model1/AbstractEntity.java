package dev.java.db.model1;

import javax.persistence.Embeddable;

@Embeddable
public abstract class AbstractEntity {
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
