/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import net.alexjf.tmm.R;
import net.alexjf.tmm.adapters.SelectedAdapter;
import net.alexjf.tmm.database.DatabaseManager;
import net.alexjf.tmm.domain.User;
import net.alexjf.tmm.domain.UserList;
import net.alexjf.tmm.utils.PreferenceManager;
import net.alexjf.tmm.utils.Utils;
import net.alexjf.tmm.utils.Utils.RememberedLoginData;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class UserListActivity extends ActionBarActivity {
    private static final String KEY_CURUSERINDEX = "curUserIdx";

    private static final int REQCODE_ADD = 0;
    private static final int REQCODE_EDIT = 1;

    private SelectedAdapter<User> adapter;
    private View userPasswordLayout;
    private EditText userPasswordText;
    private CheckBox rememberLoginCheck;

    public UserListActivity() {
        adapter = null;
        userPasswordLayout = null;
        userPasswordText = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        adapter = new SelectedAdapter<User>(this,
                R.layout.list_row_user, R.id.user_label,
                R.color.user_bg_normal, R.color.user_bg_selected);

        userPasswordLayout = findViewById(R.id.userpassword_layout);
        userPasswordText = (EditText) findViewById(R.id.userpassword_text);
        userPasswordText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(v, 0);
                }
            }
        });

        rememberLoginCheck = (CheckBox) findViewById(R.id.remember_login);

        ListView userListView = (ListView) findViewById(R.id.user_list);

        View footer = (View) getLayoutInflater().inflate(
                R.layout.list_footer_user, userListView, false);
        footer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(UserListActivity.this,
                    UserEditActivity.class);
                startActivityForResult(intent, REQCODE_ADD);
            };
        });

        userListView.addFooterView(footer);
        userListView.setAdapter(adapter);
        userListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d("TTM", "Item selected at position " + position);
                userPasswordLayout.setLayoutParams(
                    new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                                  LayoutParams.WRAP_CONTENT));
                userPasswordText.setText("");
                selectUser(position);
            }
        });

        Button loginButton = (Button) findViewById(R.id.userpassword_login);
        loginButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                User selectedUser = adapter.getItem(
                    adapter.getSelectedPosition());

                String password = userPasswordText.getText().toString();
                userPasswordText.setText("");

                String passwordHash = Utils.sha1(password);

                navigateToMoneyNodeList(selectedUser.getName(), passwordHash, false);
            };
        });

        if (savedInstanceState != null) {
            int selectedPosition = savedInstanceState.getInt(KEY_CURUSERINDEX);
            selectUser(selectedPosition);
        }

        registerForContextMenu(userListView);
        refreshUserList();

        RememberedLoginData loginData = Utils.getRememberedLogin();

        if (loginData != null) {
	        // We need to do this to ensure that login data is not cleared when doing this automatic login.
	        rememberLoginCheck.setChecked(true);
        	navigateToMoneyNodeList(loginData.username, loginData.passwordHash, true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQCODE_ADD:
                    refreshUserList();
                    selectUser(-1);
                    break;
                default:
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_CURUSERINDEX, adapter.getSelectedPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_user_list, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        User user = adapter.getItem(info.position);
        switch (item.getItemId()) {
            case R.id.menu_remove:
                UserList userList = new UserList();
                userList.removeUser(user);
	            DatabaseManager.getInstance().deleteDatabase(user.getName());
                PreferenceManager prefManager = PreferenceManager.getInstance();
                SharedPreferences.Editor editor =
                    prefManager.getUserPreferences(user).edit();
                editor.clear();
                editor.commit();
                refreshUserList();
                return true;
            case R.id.menu_edit:
                Intent intent = new Intent(UserListActivity.this,
                    UserEditActivity.class);
                intent.putExtra(User.KEY_USER, user);
                startActivityForResult(intent, REQCODE_EDIT);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void refreshUserList() {
        UserList userList = new UserList();
        adapter.clear();
        for (User user : userList.getUsers()) {
            adapter.add(user);
        }
        adapter.sort(new User.Comparator());
        selectUser(-1);
    }

    private void selectUser(int position) {
        adapter.setSelectedPosition(position);

        if (position >= 0) {
            userPasswordLayout.setVisibility(View.VISIBLE);
            userPasswordText.requestFocus();
        } else {
            userPasswordLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void navigateToMoneyNodeList(String username, String passwordHash, boolean finish) {
        DatabaseManager dbManager = DatabaseManager.getInstance();

        if (dbManager.login(username, passwordHash)) {
            if (rememberLoginCheck.isChecked()) {
            	Utils.setRememberedLogin(username, passwordHash);
                finish = true;
            } else {
            	Utils.clearRememberedLogin();
            }

            Utils.setCurrentUser(username);
            dbManager.getDatabase(username, passwordHash);

            Intent intent = new Intent(UserListActivity.this,
                MoneyNodeListActivity.class);
            startActivity(intent);

            if (finish) {
                finish();
            }
        } else {
            userPasswordText.setText("");
            String strError = getResources().getString(R.string.error_login_failed);
            Toast.makeText(UserListActivity.this,
                String.format(strError, username),
                Toast.LENGTH_LONG).show();

            // If there's a remembered login, it's clearly wrong so remove it
            Utils.clearRememberedLogin();
        }
    }
}
