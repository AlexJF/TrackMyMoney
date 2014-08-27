/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import net.alexjf.tmm.R;
import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.fragments.MoneyNodeEditorFragment;
import net.alexjf.tmm.fragments.MoneyNodeEditorFragment.OnMoneyNodeEditListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

public class MoneyNodeEditActivity extends ActionBarActivity
    implements OnMoneyNodeEditListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moneynode_edit);

        Intent intent = getIntent();
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
        DatabaseHelper.getInstance().close();
    }

    public void onMoneyNodeEdited(MoneyNode node) {
        try {
            Intent data = new Intent();
            Log.d("TMM", "Editing moneynode " + node.getName());
            node.save();
            setResult(ActionBarActivity.RESULT_OK, data);
            finish();
        } catch (DatabaseException e) {
            Log.e("TMM", "Failure editing money node", e);
            String strError = getResources().getString(
                    R.string.error_moneynode_edit);
            Toast.makeText(MoneyNodeEditActivity.this,
                String.format(strError, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    public void onMoneyNodeCreated(MoneyNode node) {
        try {
            Intent data = new Intent();
            Log.d("TMM", "Adding moneynode " + node.getName());
            node.save();
            data.putExtra(MoneyNode.KEY_MONEYNODE, node);
            setResult(ActionBarActivity.RESULT_OK, data);
            finish();
        } catch (DatabaseException e) {
            Log.e("TMM", "Failure adding money node", e);
            String strError = getResources().getString(
                    R.string.error_moneynode_add);
            Toast.makeText(MoneyNodeEditActivity.this,
                String.format(strError, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }
}
