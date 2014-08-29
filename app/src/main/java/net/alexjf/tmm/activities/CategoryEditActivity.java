/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import net.alexjf.tmm.R;
import net.alexjf.tmm.domain.Category;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.fragments.CategoryEditorFragment;
import net.alexjf.tmm.fragments.CategoryEditorFragment.OnCategoryEditListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

public class CategoryEditActivity extends ActionBarActivity
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

    public void onCategoryEdited(Category category) {
        try {
            Intent data = new Intent();
            Log.d("TMM", "Editing category " + category.getName());
            category.save();
            setResult(ActionBarActivity.RESULT_OK, data);
            finish();
        } catch (DatabaseException e) {
            Log.e("TMM", "Failure editing category", e);
            String strError = getResources().getString(R.string.error_cat_edit);
            Toast.makeText(CategoryEditActivity.this,
                String.format(strError, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    public void onCategoryCreated(Category category) {
        try {
            Intent data = new Intent();
            Log.d("TMM", "Adding category " + category.getName());
            category.save();
            data.putExtra(Category.KEY_CATEGORY, category);
            setResult(ActionBarActivity.RESULT_OK, data);
            finish();
        } catch (DatabaseException e) {
            Log.e("TMM", "Failure adding category", e);
            String strError = getResources().getString(R.string.error_cat_add);
            Toast.makeText(CategoryEditActivity.this,
                String.format(strError, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }
}
