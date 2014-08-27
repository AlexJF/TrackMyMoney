/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.domain;

import net.alexjf.tmm.utils.Utils;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class represents a single user of the application.
 */
public class User implements Parcelable {
    // Intent keys
    public static final String KEY_USER = "curUser";

    private String name;
    private String password;

    User(String name) {
        this.name = name;
    }

    User(String name, String password) {
        this(name);
        setPassword(password);
    }

    User(User user) {
    	this(user.getName());
    	setPasswordHash(user.getPassword());
    }

    User(Parcel in) {
        readFromParcel(in);
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
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the password for this instance.
     *
     * @param password The password.
     */
    public void setPassword(String password) {
        this.password = Utils.sha1(password);
    }

    public void setPasswordHash(String passwordHash) {
        this.password = passwordHash;
    }

    /**
     * Gets the sha-1 hash of the password for this instance.
     *
     * @return The password.
     */
    public String getPassword() {
        return this.password;
    }

    @Override
	public boolean equals(Object o) {
		if (!(o instanceof User))
            return false;

        if (o == this)
            return true;

        User rhs = (User) o;
        return getName().equals(rhs.getName()) &&
        	   getPassword().equals(rhs.getPassword());
	}

	@Override
	public int hashCode() {
		int hash = 7;
        hash = 31 * hash + name.hashCode();
        hash = 31 * hash + password.hashCode();

        return hash;
	}

	public String toString() {
        return getName();
    };

    public void readFromParcel(Parcel in) {
        name = in.readString();
        password = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(password);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<User> CREATOR =
        new Parcelable.Creator<User>() {
            public User createFromParcel(Parcel in) {
                return new User(in);
            }

            public User[] newArray(int size) {
                return new User[size];
            }
        };

    public static class Comparator implements java.util.Comparator<User> {
        public int compare(User lhs, User rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }
}
