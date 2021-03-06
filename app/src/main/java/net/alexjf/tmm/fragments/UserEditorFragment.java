/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.fragments;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import net.alexjf.tmm.R;
import net.alexjf.tmm.database.DatabaseManager;
import net.alexjf.tmm.domain.User;
import net.alexjf.tmm.domain.UserList;
import net.alexjf.tmm.utils.Utils;

public class UserEditorFragment extends Fragment {
	private static final String KEY_CURRENTUSER = "username";

	private User user;
	private OnUserEditListener listener;
	private UserList userList;

	private EditText usernameText;
	private TextView passwordLabel;
	private EditText passwordText;
	private EditText passwordConfirmText;
	private TextView oldPasswordLabel;
	private EditText oldPasswordText;
	private Button addButton;

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		User prevUser = this.user;
		this.user = user;

		if (prevUser != user) {
			updateUserFields();
		}
	}

	/**
	 * @return the listener
	 */
	public OnUserEditListener getListener() {
		return listener;
	}

	/**
	 * @param listener the listener to set
	 */
	public void setListener(OnUserEditListener listener) {
		this.listener = listener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_user_editor,
				container, false);

		userList = new UserList();

		usernameText = (EditText) v.findViewById(R.id.username_text);
		passwordLabel = (TextView) v.findViewById(R.id.password_label);
		passwordText = (EditText) v.findViewById(R.id.password_text);
		passwordConfirmText = (EditText) v.findViewById(R.id.password_confirm_text);
		oldPasswordLabel = (TextView) v.findViewById(R.id.password_old_label);
		oldPasswordText = (EditText) v.findViewById(R.id.password_old_text);
		addButton = (Button) v.findViewById(R.id.add_button);

		addButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (!validateInputFields()) {
					return;
				}

				String username = usernameText.getText().toString();
				// TODO: This needs some salt
				String password = Utils.sha1(passwordText.getText().toString());

				if (user == null) {
					User newUser = userList.addUser(username);
					DatabaseManager.getInstance().login(username, password);
					listener.onUserCreated(newUser);
				} else {
					String oldPassword = Utils.sha1(oldPasswordText.getText().toString());
					if (!DatabaseManager.getInstance().changePassword(username, oldPassword, password)) {
						Toast.makeText(getActivity(), R.string.error_password_change, Toast.LENGTH_LONG).show();
						return;
					}
					listener.onUserEdited(user);
				}
			}
		});

		if (savedInstanceState != null) {
			user = savedInstanceState.getParcelable(KEY_CURRENTUSER);
		}

		updateUserFields();

		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_CURRENTUSER, user);
		super.onSaveInstanceState(outState);
	}

	private void updateUserFields() {
		passwordText.setText("");
		passwordConfirmText.setText("");
		oldPasswordText.setText("");

		if (user == null) {
			usernameText.setText("");
			usernameText.setEnabled(true);
			passwordLabel.setText(R.string.password);
			oldPasswordLabel.setVisibility(View.GONE);
			oldPasswordText.setVisibility(View.GONE);
			addButton.setText(R.string.add);
		} else {
			usernameText.setText(user.getName());
			usernameText.setEnabled(false);
			passwordLabel.setText(R.string.password_new);
			oldPasswordLabel.setVisibility(View.VISIBLE);
			oldPasswordText.setVisibility(View.VISIBLE);
			addButton.setText(R.string.edit);
		}
	}

	private boolean validateInputFields() {
		boolean error = false;

		Resources res = getResources();
		Drawable errorDrawable =
				res.getDrawable(R.drawable.indicator_input_error);
		errorDrawable.setBounds(0, 0,
				errorDrawable.getIntrinsicWidth(),
				errorDrawable.getIntrinsicHeight());

		String name = usernameText.getText().toString();

		String nameError = null;
		if (TextUtils.isEmpty(name)) {
			nameError = res.getString(R.string.error_username_not_empty);
		} else {
			UserList userList = new UserList();
			if (user == null && userList.getUser(name) != null) {
				nameError =
						res.getString(R.string.error_username_already_exists);
			}
		}

		if (nameError != null) {
			usernameText.setError(nameError, errorDrawable);
			error = true;
		}

		if (!passwordConfirmText.getText().toString().equals(
				passwordText.getText().toString())) {
			passwordConfirmText.setError(
					res.getString(R.string.error_password_confirmation),
					errorDrawable);
			error = true;
		}

		// If we are editing a user...
		if (user != null) {
			String username = user.getName();
			String passwordHash = Utils.sha1(oldPasswordText.getText().toString());
			if (!DatabaseManager.getInstance().login(username, passwordHash)) {
				oldPasswordText.setError(
						res.getString(R.string.error_wrong_password),
						errorDrawable);
				error = true;
			}
		}

		return !error;
	}

	public interface OnUserEditListener {
		public void onUserCreated(User user);

		public void onUserEdited(User user);
	}

}
