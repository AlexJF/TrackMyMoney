package com.alexjf.tmm;

import com.alexjf.tmm.adapters.SelectedAdapter;

import com.alexjf.tmm.domain.User;

import android.os.Bundle;
import android.app.Activity;

import android.util.Log;
import android.view.Menu;
import android.view.View;

import android.view.ViewGroup.LayoutParams;

import android.widget.AdapterView;

import android.widget.AdapterView.OnItemClickListener;

import android.widget.LinearLayout;
import android.widget.ListView;

public class TrackMyMoneyActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trackmymoney);

        User[] user_data = new User[] {
            new User("Alex", "12345"),
            new User("Sveta", "12345"),
            new User("Pedro", "12345"),
            new User("Diogo", "12345"),
            new User("Alex", "12345"),
            new User("Sveta", "12345"),
            new User("Pedro", "12345"),
            new User("Diogo", "12345"),
        };

        final SelectedAdapter<User> adapter = new SelectedAdapter<User>(this, 
                R.layout.user_list_row, R.id.user_label, user_data,
                R.color.user_bg_normal, R.color.user_bg_selected);

        View footer = (View) getLayoutInflater().inflate(R.layout.user_list_footer, null);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_money_nodes, menu);
        return true;
    }
}
