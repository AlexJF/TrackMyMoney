/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import net.alexjf.tmm.R;
import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.domain.Category;
import net.alexjf.tmm.domain.User;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.fragments.CategoryEditorFragment;
import net.alexjf.tmm.fragments.CategoryEditorFragment.OnCategoryEditListener;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class CategoryEditActivity extends SherlockFragmentActivity 
    implements OnCategoryEditListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_edit);

        Intent intent = getIntent();
        Category category = (Category) intent.getParcelableExtra(
                Category.KEY_CATEGORY);

        CategoryEditorFragment editor = (CategoryEditorFragment)
            getSupportFragmentManager().findFragmentById(R.id.category_editor);
        editor.setCategory(category);

        if (category == null) {
            setTitle(R.string.title_activity_category_add);
        } else {
            setTitle(R.string.title_activity_category_edit);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DatabaseHelper.getInstance().close();
    }

    public void onCategoryEdited(Category category) {
        try {
            Intent data = new Intent();
            Log.d("TMM", "Editing category " + category.getName());
            category.save();
            setResult(SherlockFragmentActivity.RESULT_OK, data);
            finish();
        } catch (DatabaseException e) {
            Log.e("TMM", "Failure editing category", e);
            Toast.makeText(CategoryEditActivity.this,
                "Error editing category: " + e.getMessage(), 3).show();
        }
    }

    public void onCategoryCreated(Category category) {
        try {
            Intent data = new Intent();
            Log.d("TMM", "Adding category " + category.getName());
            category.save();
            data.putExtra(Category.KEY_CATEGORY, category);
            setResult(SherlockFragmentActivity.RESULT_OK, data);
            finish();
        } catch (DatabaseException e) {
            Log.e("TMM", "Failure adding category", e);
            Toast.makeText(CategoryEditActivity.this,
                "Error adding category: " + e.getMessage(), 3).show();
        }
    }
}
