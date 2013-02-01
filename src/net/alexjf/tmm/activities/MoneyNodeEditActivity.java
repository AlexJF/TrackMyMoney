/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import net.alexjf.tmm.R;
import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.domain.User;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.fragments.MoneyNodeEditorFragment;
import net.alexjf.tmm.fragments.MoneyNodeEditorFragment.OnMoneyNodeEditListener;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class MoneyNodeEditActivity extends SherlockFragmentActivity 
    implements OnMoneyNodeEditListener {
    private DatabaseHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moneynode_edit);

        Intent intent = getIntent();
        User currentUser = (User) intent.getParcelableExtra(
                User.KEY_USER);
        dbHelper = new DatabaseHelper(getApplicationContext(), 
                currentUser);
        MoneyNode node = (MoneyNode) intent.getParcelableExtra(
                MoneyNode.KEY_MONEYNODE);

        MoneyNodeEditorFragment editor = (MoneyNodeEditorFragment)
            getSupportFragmentManager().findFragmentById(R.id.moneynode_editor);
        editor.setNode(node);

        if (node == null) {
            setTitle(R.string.title_activity_moneynode_add);
        } else {
            setTitle(R.string.title_activity_moneynode_edit);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbHelper.close();
    }

    public void onMoneyNodeEdited(MoneyNode node) {
        try {
            Intent data = new Intent();
            Log.d("TMM", "Editing moneynode " + node.getName());
            node.save(dbHelper.getWritableDatabase());
            setResult(SherlockFragmentActivity.RESULT_OK, data);
            finish();
        } catch (DatabaseException e) {
            Log.e("TMM", "Failure editing money node", e);
            Toast.makeText(MoneyNodeEditActivity.this,
                "Error editing money node: " + e.getMessage(), 3).show();
        }
    }

    public void onMoneyNodeCreated(MoneyNode node) {
        try {
            Intent data = new Intent();
            Log.d("TMM", "Adding moneynode " + node.getName());
            node.save(dbHelper.getWritableDatabase());
            data.putExtra(MoneyNode.KEY_MONEYNODE, node);
            setResult(SherlockFragmentActivity.RESULT_OK, data);
            finish();
        } catch (DatabaseException e) {
            Log.e("TMM", "Failure adding money node", e);
            Toast.makeText(MoneyNodeEditActivity.this,
                "Error adding money node: " + e.getMessage(), 3).show();
        }
    }
}
