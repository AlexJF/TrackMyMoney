package com.alexjf.tmm.domain;

/**
 * This class represents a single user of the application.
 */
public class User {
    private String name;
    private String passHash;

    public User(String name, String passHash) {
        this.name = name;
        this.passHash = passHash;
    }

    /**
     * Gets the name for this instance.
     *
     * @return The name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name for this instance.
     *
     * @param name The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the passHash for this instance.
     *
     * @param passHash The passHash.
     */
    public void setPassHash(String passHash) {
        this.passHash = passHash;
    }

    /**
     * Gets the passHash for this instance.
     *
     * @return The passHash.
     */
    public String getPassHash() {
        return this.passHash;
    }

    public String toString() {
        return getName();
    };
}
