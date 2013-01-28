package net.alexjf.tmm.domain;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class represents a single user of the application.
 */
public class User implements Parcelable {
    public static final String EXTRA_CURRENTUSER = "currentUser";

    private String name;
    private String password;

    User(String name) {
        this.name = name;
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
}
