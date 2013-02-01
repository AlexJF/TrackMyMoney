/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import net.alexjf.tmm.R;
import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.domain.User;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.fragments.ImmedTransactionEditorFragment;
import net.alexjf.tmm.fragments.ImmedTransactionEditorFragment.OnImmediateTransactionEditListener;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class ImmedTransactionEditActivity extends SherlockFragmentActivity 
    implements OnImmediateTransactionEditListener {
    private DatabaseHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_immedtransaction_edit);

        Intent intent = getIntent();
        User currentUser = (User) intent.getParcelableExtra(
                User.KEY_USER);
        dbHelper = new DatabaseHelper(getApplicationContext(), 
                currentUser);
        MoneyNode moneyNode = (MoneyNode) 
            intent.getParcelableExtra(MoneyNode.KEY_MONEYNODE);
        ImmediateTransaction transaction = (ImmediateTransaction) 
            intent.getParcelableExtra(ImmediateTransaction.KEY_TRANSACTION);

        ImmedTransactionEditorFragment editor = (ImmedTransactionEditorFragment)
            getSupportFragmentManager().findFragmentById(R.id.immedtransaction_editor);
        editor.setDbHelper(dbHelper);
        editor.setCurrentMoneyNode(moneyNode);
        editor.setTransaction(transaction);

        if (transaction == null) {
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

    public void onImmediateTransactionEdited(ImmediateTransaction trans) {
        try {
            Intent data = new Intent();
            Log.d("TMM", "Editing immediate transaction " + trans.getId());
            trans.save(dbHelper.getWritableDatabase());
            setResult(SherlockFragmentActivity.RESULT_OK, data);
            finish();
        } catch (DatabaseException e) {
            Log.e("TMM", "Failure editing immediate transaction", e);
            Toast.makeText(ImmedTransactionEditActivity.this,
                "Error editing transaction: " + e.getMessage(), 3).show();
        }
    }

    public void onImmediateTransactionCreated(ImmediateTransaction trans) {
        try {
            Intent data = new Intent();
            Log.d("TMM", "Adding immediate transaction " + trans.getId());
            trans.save(dbHelper.getWritableDatabase());
            data.putExtra(ImmediateTransaction.KEY_TRANSACTION, trans);
            setResult(SherlockFragmentActivity.RESULT_OK, data);
            finish();
        } catch (DatabaseException e) {
            Log.e("TMM", "Failure adding immediate transaction", e);
            Toast.makeText(ImmedTransactionEditActivity.this,
                "Error adding transaction: " + e.getMessage(), 3).show();
        }
    }
}
