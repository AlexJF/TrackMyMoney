package net.alexjf.tmm;

import net.alexjf.tmm.adapters.SelectedAdapter;
import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.domain.User;
import net.alexjf.tmm.domain.UserList;
import net.alexjf.tmm.R;

import android.content.Intent;

import android.os.Bundle;
import android.app.Activity;

import android.util.Log;
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

public class UserListActivity extends Activity {
    private UserList userList;
    private SelectedAdapter<User> adapter;
    private View userPasswordLayout;
    private EditText userPasswordText;

    public UserListActivity() {
        userList = null;
        adapter = null;
        userPasswordLayout = null;
        userPasswordText = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        userList = new UserList(getApplicationContext());

        adapter = new SelectedAdapter<User>(this, 
                R.layout.user_list_row, R.id.user_label, userList.getUsers(),
                R.color.user_bg_normal, R.color.user_bg_selected);

        View footer = (View) getLayoutInflater().inflate(
                R.layout.user_list_footer, null);
        footer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(UserListActivity.this, 
                    UserAddActivity.class);
                startActivityForResult(intent, 0);
            };
        });

        userPasswordLayout = findViewById(R.id.userpassword_layout);
        userPasswordText = (EditText) findViewById(R.id.userpassword_text);

        ListView userListView = (ListView) findViewById(R.id.user_list);

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
                adapter.setSelectedPosition(position);
            }
        });

        final EditText passwordText = (EditText) 
            findViewById(R.id.userpassword_text);

        Button loginButton = (Button) findViewById(R.id.userpassword_login);
        loginButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                User selectedUser = adapter.getItem(
                    adapter.getSelectedPosition());
                DatabaseHelper dbHelper = new DatabaseHelper(
                    getApplicationContext(),
                    selectedUser);
                String password = passwordText.getText().toString();
                if (dbHelper.login(password)) {
                    userPasswordText.setText("");
                    selectedUser.setPassword(password);

                    Intent intent = new Intent(UserListActivity.this,
                        MoneyNodeListActivity.class);
                    intent.putExtra(User.EXTRA_CURRENTUSER, selectedUser);
                    startActivity(intent);
                } else {
                    userPasswordText.setText("");
                    Toast.makeText(UserListActivity.this, 
                        "Login Failure!", 3).show();
                }
            };
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, 
            Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String username = data.getStringExtra(UserAddActivity.EXTRA_USERNAME);
            String password = data.getStringExtra(UserAddActivity.EXTRA_PASSWORD);

            // Add user and login to initiate the database
            User newUser = userList.addUser(username);
            DatabaseHelper dbHelper = new DatabaseHelper(
                    getApplicationContext(), newUser);
            dbHelper.login(password);
            adapter.notifyDataSetChanged();
        }
    }
}
