/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.activities;

import java.util.ArrayList;
import java.util.List;

import net.alexjf.tmm.R;
import net.alexjf.tmm.adapters.CategoryAdapter;
import net.alexjf.tmm.domain.Category;
import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.domain.User;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class CategoryListActivity extends SherlockActivity {
    private static final int REQCODE_ADD = 0;
    private static final int REQCODE_EDIT = 1;

    private DatabaseHelper dbHelper;
    private User currentUser;
    private CategoryAdapter adapter;

    public CategoryListActivity() {
        dbHelper = null;
        currentUser = null;
        adapter = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        Intent intent = getIntent();
        currentUser = (User) intent.getParcelableExtra(
                User.KEY_USER);
        dbHelper = new DatabaseHelper(getApplicationContext(), 
                currentUser);

        List<Category> categories;

        try {
            categories = dbHelper.getCategories();
        } catch (Exception e) {
            Log.e("TMM", "Failed to get categories: " + e.getMessage() + 
                    "\n" + e.getStackTrace());
            categories = new ArrayList<Category>();
        }

        adapter = new CategoryAdapter(this, dbHelper, categories);

        ListView categoriesListView = (ListView) findViewById(
                R.id.category_list);

        View emptyView = findViewById(R.id.category_list_empty);

        categoriesListView.setEmptyView(emptyView);
        categoriesListView.setAdapter(adapter);

        registerForContextMenu(categoriesListView);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_category_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                Intent intent = new Intent(this, 
                    CategoryEditActivity.class);
                intent.putExtra(User.KEY_USER, currentUser);
                startActivityForResult(intent, REQCODE_ADD);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, 
            Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQCODE_ADD:
                    Category category = (Category) data.getParcelableExtra(
                        Category.KEY_CATEGORY);
                    adapter.add(category);
                    break;
                case REQCODE_EDIT:
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        android.view.MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_category_list, menu);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        Category category = adapter.getItem(info.position);
        switch (item.getItemId()) {
            case R.id.menu_remove:
                dbHelper.deleteCategory(category);
                adapter.remove(category);
                return true;
            case R.id.menu_edit:
                Intent intent = new Intent(this, 
                    CategoryEditActivity.class);
                intent.putExtra(User.KEY_USER, currentUser);
                intent.putExtra(Category.KEY_CATEGORY, category);
                startActivityForResult(intent, REQCODE_EDIT);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
