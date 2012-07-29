package net.alexjf.tmm.domain;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class represents a list of users of the application.
 */
public class UserList {
    public final static String USERDATA_PREFERENCES_NAME = "userData";
    public final static String USERDATA_USERNAMES_KEY = "userNames";
    private Context context;
    private List<User> users;

    public UserList(Context context) {
        this.context = context;
        SharedPreferences preferences = getPreferences();
        String[] userNames = preferences.getString(USERDATA_USERNAMES_KEY, "").split(":");

        this.users = new ArrayList<User>(userNames.length);

        for (String username : userNames) {
            if (!username.equals("")) {
                this.users.add(new User(username, context));
            }
        }
    }

    public User addUser(String name) {
        User newUser = new User(name, context);
        this.users.add(newUser);
        saveData();
        return newUser;
    }

    public List<User> getUsers() {
        return this.users;
    }

    protected void saveData() {
        String userNames = "";

        for (User user : this.users) {
            userNames += user.getName() + ":";
        }

        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(USERDATA_USERNAMES_KEY, userNames);
        editor.commit();
    }

    protected SharedPreferences getPreferences() {
        return this.context.getSharedPreferences(USERDATA_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
}
