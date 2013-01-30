/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class represents a list of users of the application.
 */
public class UserList {
    public final static String USERDATA_PREFERENCES_NAME = "userData";
    public final static String USERDATA_USERNAMES_KEY = "userNames";
    private Context context;
    private Map<String, User> users;

    public UserList(Context context) {
        this.context = context;
        SharedPreferences preferences = getPreferences();
        String[] userNames = preferences.getString(USERDATA_USERNAMES_KEY, "").split(":");

        this.users = new HashMap<String, User>();

        for (String username : userNames) {
            if (!username.equals("")) {
                this.users.put(username, new User(username));
            }
        }
    }

    public User addUser(String name) {
        User newUser = new User(name);
        this.users.put(name, newUser);
        saveData();
        return newUser;
    }

    public void removeUser(User user) {
        if (user != null) {
            DatabaseHelper.deleteDatabase(context, user);
            this.users.remove(user.getName());
            saveData();
        }
    }

    public void removeUser(String name) {
        User user = getUser(name);
        removeUser(user);
    }

    public Collection<User> getUsers() {
        return Collections.unmodifiableCollection(this.users.values());
    }

    public User getUser(String name) {
        return users.get(name);
    }

    protected void saveData() {
        String userNames = "";

        for (String userName : this.users.keySet()) {
            userNames += userName + ":";
        }

        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(USERDATA_USERNAMES_KEY, userNames);
        editor.commit();
    }

    protected SharedPreferences getPreferences() {
        return this.context.getSharedPreferences(USERDATA_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
}
