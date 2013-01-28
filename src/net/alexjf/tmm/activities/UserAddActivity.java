/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import net.alexjf.tmm.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;

public class UserAddActivity extends SherlockActivity {
    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_PASSWORD = "password";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_add);

        Button addButton = (Button) findViewById(R.id.add_button);
        final EditText usernameText = (EditText) findViewById(R.id.username_text);
        final EditText passwordText = (EditText) findViewById(R.id.password_text);

        addButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent data = new Intent();
                data.putExtra(EXTRA_USERNAME, 
                    usernameText.getText().toString().trim());
                data.putExtra(EXTRA_PASSWORD, 
                    passwordText.getText().toString().trim());
                Log.d("TMM", "Adding user " + usernameText.getText().toString());
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });
    }
}
