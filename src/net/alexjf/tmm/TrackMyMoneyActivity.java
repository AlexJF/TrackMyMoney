package net.alexjf.tmm;

import net.alexjf.tmm.adapters.SelectedAdapter;
import net.alexjf.tmm.domain.User;
import net.alexjf.tmm.domain.UserList;
import net.alexjf.tmm.exceptions.LoginFailedException;

import com.alexjf.tmm.R;



import android.content.Intent;

import android.os.Bundle;
import android.app.Activity;

import android.util.Log;
import android.view.Menu;
import android.view.View;

import android.view.View.OnClickListener;

import android.view.ViewGroup.LayoutParams;

import android.widget.AdapterView;

import android.widget.AdapterView.OnItemClickListener;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class TrackMyMoneyActivity extends Activity {
    private UserList userList;
    private SelectedAdapter<User> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trackmymoney);

        userList = new UserList(getApplicationContext());

        adapter = new SelectedAdapter<User>(this, 
                R.layout.user_list_row, R.id.user_label, userList.getUsers(),
                R.color.user_bg_normal, R.color.user_bg_selected);

        View footer = (View) getLayoutInflater().inflate(R.layout.user_list_footer, null);
        footer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(TrackMyMoneyActivity.this, UserAddActivity.class);
                startActivityForResult(intent, 0);
            };
        });

        ListView userListView = (ListView) findViewById(R.id.user_list);

        userListView.addFooterView(footer);
        userListView.setAdapter(adapter);
        userListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("TTM", "Item selected at position " + position);
                View userPasswordLayout = findViewById(R.id.userpassword_layout);
                userPasswordLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                adapter.setSelectedPosition(position);
            }
        });

        final EditText passwordText = (EditText) findViewById(R.id.userpassword_text);

        Button loginButton = (Button) findViewById(R.id.userpassword_login);
        loginButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                User selectedUser = adapter.getItem(adapter.getSelectedPosition());
                try {
                    selectedUser.login(passwordText.getText().toString());
                    Toast.makeText(TrackMyMoneyActivity.this, "Successful!", 3).show();
                } catch (LoginFailedException e) {
                    Toast.makeText(TrackMyMoneyActivity.this, "Failure!", 3).show();
                }
            };
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_money_nodes, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String username = data.getStringExtra("username");
            String password = data.getStringExtra("password");

            // Add user and login to initiate the database
            try {
                userList.addUser(username).login(password);
                adapter.notifyDataSetChanged();
            } catch (LoginFailedException e) {
            }
        }
    }
}
