/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import net.alexjf.tmm.R;
import net.alexjf.tmm.domain.User;
import net.alexjf.tmm.fragments.UserEditorFragment;
import net.alexjf.tmm.fragments.UserEditorFragment.OnUserEditListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

public class UserEditActivity extends ActionBarActivity
    implements OnUserEditListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit);

        Intent intent = getIntent();
        User user = (User) intent.getParcelableExtra(
                User.KEY_USER);

        UserEditorFragment editor = (UserEditorFragment)
            getSupportFragmentManager().findFragmentById(R.id.user_editor);
        editor.setUser(user);
        editor.setListener(this);

        if (user == null) {
            setTitle(R.string.title_activity_user_add);
        } else {
            setTitle(R.string.title_activity_user_edit);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onUserEdited(User user) {
        Intent data = new Intent();
        Log.d("TMM", "Editing user " + user.getName());
        setResult(ActionBarActivity.RESULT_OK, data);
        finish();
    }

    public void onUserCreated(User user) {
        Intent data = new Intent();
        Log.d("TMM", "Adding user " + user.getName());
        data.putExtra(User.KEY_USER, user);
        setResult(ActionBarActivity.RESULT_OK, data);
        finish();
    }
}

