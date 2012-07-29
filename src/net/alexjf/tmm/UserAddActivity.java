package net.alexjf.tmm;

import com.alexjf.tmm.R;

import android.content.Intent;

import android.os.Bundle;
import android.app.Activity;

import android.util.Log;

import android.view.View;

import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.EditText;

public class UserAddActivity extends Activity {
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
                data.putExtra("username", usernameText.getText().toString());
                data.putExtra("password", passwordText.getText().toString());
                Log.d("TMM", "Adding user " + usernameText.getText().toString());
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });
    }
}
