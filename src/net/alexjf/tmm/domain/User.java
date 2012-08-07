package net.alexjf.tmm.domain;

import java.io.Serializable;

/**
 * This class represents a single user of the application.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1;

    public static final String EXTRA_CURRENTUSER = "currentUser";

    private String name;
    private String password;

    User(String name) {
        this.name = name;
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
     * Sets the password for this instance.
     *
     * @param password The password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the password for this instance.
     *
     * @return The password.
     */
    public String getPassword() {
        return this.password;
    }

    public String toString() {
        return getName();
    };
}
