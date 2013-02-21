/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import net.alexjf.tmm.R;
import net.alexjf.tmm.domain.UserList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;

public class UserAddActivity extends SherlockActivity {
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";

    private EditText usernameText;
    private EditText passwordText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_add);

        Button addButton = (Button) findViewById(R.id.add_button);
        usernameText = (EditText) findViewById(R.id.username_text);
        passwordText = (EditText) findViewById(R.id.password_text);

        addButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (!validateInputFields()) {
                    return;
                }

                Intent data = new Intent();
                data.putExtra(KEY_USERNAME, 
                    usernameText.getText().toString().trim());
                data.putExtra(KEY_PASSWORD, 
                    passwordText.getText().toString().trim());
                Log.d("TMM", "Adding user " + usernameText.getText().toString());
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });
    }

    private boolean validateInputFields() {
        boolean error = false;

        Drawable errorDrawable = 
            getResources().getDrawable(R.drawable.indicator_input_error);
        errorDrawable.setBounds(0, 0, 
                errorDrawable.getIntrinsicWidth(), 
                errorDrawable.getIntrinsicHeight());

        String name = usernameText.getText().toString();

        // TODO move error strings to resources
        String nameError = null;
        if (TextUtils.isEmpty(name)) {
            nameError = "Username cannot be empty.";
        }
        else {
            UserList userList = new UserList(this);
            if (userList.getUser(name) != null) {
                nameError = "Username already exists";
            }
        }

        if (nameError != null) {
            usernameText.setError(nameError, errorDrawable);
            error = true;
        }

        return !error;
    }

}
