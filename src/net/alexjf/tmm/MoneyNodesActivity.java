package net.alexjf.tmm;

import net.alexjf.tmm.adapters.MoneyNodeAdapter;
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

public class MoneyNodesActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moneynodes);

        Intent intent = getIntent();

        User currentUser = intent.getSerializableExtra(User.EXTRA_CURRENT_USER);

        final MoneyNodeAdapter adapter = new MoneyNodeAdapter(this, currentUser.getMoneyNodes());

        ListView moneyNodesListView = (ListView) findViewById(R.id.moneynode_list);

        moneyNodesListView.setAdapter(adapter);
        moneyNodesListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("TTM", "Item selected at position " + position);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_money_nodes, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }
}
