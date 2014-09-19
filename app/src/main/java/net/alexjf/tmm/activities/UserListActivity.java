/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import net.alexjf.tmm.R;
import net.alexjf.tmm.adapters.SelectedAdapter;
import net.alexjf.tmm.database.DatabaseManager;
import net.alexjf.tmm.domain.User;
import net.alexjf.tmm.domain.UserList;
import net.alexjf.tmm.exceptions.ExitingException;
import net.alexjf.tmm.utils.PreferenceManager;
import net.alexjf.tmm.utils.Utils;
import net.alexjf.tmm.utils.Utils.RememberedLoginData;

import java.util.Arrays;
import java.util.Collection;

public class UserListActivity extends BaseActionBarActivity {
	private static final String KEY_CURUSERINDEX = "curUserIdx";

	private static final int REQCODE_ADD = 0;
	private static final int REQCODE_EDIT = 1;

	public static final String KEY_INTENTION = "intention";
	public static final String INTENTION_LOGIN = "login";
	public static final String INTENTION_RELOGIN = "relogin";

	private String intention;

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
	protected void onCreateInternal(Bundle savedInstanceState) throws ExitingException {
		// We don't need a working database at this stage.
		setRequiresDatabase(false);
		super.onCreateInternal(savedInstanceState);
		setContentView(R.layout.activity_user_list);

		Intent intent = getIntent();

		intention = intent.getStringExtra(KEY_INTENTION);

		if (intention == null) {
			intention = INTENTION_LOGIN;
		}

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
			}
		});

		// Only put 'Add user' option in list if we are logging, not relogging.
		if (intention.equals(INTENTION_LOGIN)) {
			userListView.addFooterView(footer);
		}

		userListView.setAdapter(adapter);
		userListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d("TTM", "Item selected at position " + position);
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

				login(selectedUser.getName(), passwordHash, false);
			}

			;
		});

		if (savedInstanceState != null) {
			int selectedPosition = savedInstanceState.getInt(KEY_CURUSERINDEX);
			selectUser(selectedPosition);
		}

		registerForContextMenu(userListView);

		RememberedLoginData loginData = Utils.getRememberedLogin();

		if (loginData != null) {
			// We need to do this to ensure that login data is not cleared when doing this automatic login.
			rememberLoginCheck.setChecked(true);
			login(loginData.username, loginData.passwordHash, true);
		}

		User currentUser = null;

		// If we are relogging only add the current user to the list
		if (intention.equals(INTENTION_RELOGIN)) {
			currentUser = Utils.getCurrentUser();
		}

		if (currentUser != null) {
			refreshUserList(currentUser);
		} else {
			refreshUserList();
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

		super.onActivityResult(requestCode, resultCode, data);
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

		// Only create the menu if we are logging in, not relogging
		if (intention.equals(INTENTION_LOGIN)) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.context_user_list, menu);
		}
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

	private void refreshUserList(Collection<User> users) {
		adapter.setNotifyOnChange(false);
		adapter.clear();
		for (User user : users) {
			adapter.add(user);
		}
		adapter.sort(new User.Comparator());
		adapter.notifyDataSetChanged();
	}

	private void refreshUserList() {
		UserList userList = new UserList();
		refreshUserList(userList.getUsers());
		selectUser(-1);
	}

	private void refreshUserList(User user) {
		refreshUserList(Arrays.asList(user));
		selectUser(0);
	}

	private void selectUser(int position) {
		adapter.setSelectedPosition(position);

		if (position >= 0) {
			/*userPasswordLayout.setLayoutParams(
					new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT));*/
			userPasswordText.setText("");
			userPasswordLayout.setVisibility(View.VISIBLE);
			userPasswordText.requestFocus();
		} else {
			userPasswordLayout.setVisibility(View.GONE);
		}
	}

	private void login(String username, String passwordHash, boolean finish) {
		DatabaseManager dbManager = DatabaseManager.getInstance();

		if (dbManager.login(username, passwordHash)) {
			if (rememberLoginCheck.isChecked()) {
				Utils.setRememberedLogin(username, passwordHash);
				finish = true;
			} else {
				Utils.clearRememberedLogin();
			}

			Utils.setCurrentUser(username);

			// Open database. Force clear cache except if this is a relogin.
			dbManager.getDatabase(username, passwordHash, !intention.equals(INTENTION_RELOGIN));

			// If doing a normal login or relogging as a different user, start from scratch
			if (!intention.equals(INTENTION_RELOGIN)) {
				Intent intent = new Intent(UserListActivity.this,
						MoneyNodeListActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}

			// If set to finish or relogging, finish this activity
			if (finish || intention.equals(INTENTION_RELOGIN)) {
				setResult(RESULT_OK);
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

	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		// If we were supposed to relogin but pressed back button instead
		// then exit application
		if (intention.equals(INTENTION_RELOGIN)) {
			Utils.exitApplication(this);
		}
		// Else business as usual
		else {
			super.onBackPressed();
		}
	}
}
