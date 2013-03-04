/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.alexjf.tmm.utils.PreferenceManager;

import android.content.Context;

/**
 * This class represents a list of users of the application.
 */
public class UserList {
    public final static String USERDATA_PREFERENCES_NAME = "userData";
    public final static String USERDATA_USERNAMES_KEY = "userNames";
    public final static String USERDATA_REMEMBEREDLOGIN = "rememberedLogin";

    private Context context;
    private Map<String, User> users;

    public static User getRememberedLogin(Context context) {
        PreferenceManager prefManager = PreferenceManager.getInstance();
        String[] rememberedLogin = prefManager.readGlobalStringPreference(
                USERDATA_REMEMBEREDLOGIN, "").split(":");

        if (rememberedLogin.length != 2) {
            return null;
        } else {
            User user = new User(rememberedLogin[0]);
            user.setPasswordHash(rememberedLogin[1]);
            return user;
        }
    }

    public static void setRememberedLogin(Context context, User user) {
        PreferenceManager prefManager = PreferenceManager.getInstance();
        
        if (user != null) {
            prefManager.writeGlobalStringPreference(USERDATA_REMEMBEREDLOGIN, 
                    user.getName() + ":" + user.getPassword());
        } else {
            prefManager.removeGlobalPreference(USERDATA_REMEMBEREDLOGIN);
        }
    }

    public UserList(Context context) {
        this.context = context;
        PreferenceManager prefManager = PreferenceManager.getInstance();
        String[] userNames = prefManager.readGlobalStringPreference(
                    USERDATA_USERNAMES_KEY, "").split(":");

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

        PreferenceManager.getInstance().writeGlobalStringPreference(
                USERDATA_USERNAMES_KEY, userNames);
    }
}
