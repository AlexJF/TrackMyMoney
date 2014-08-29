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
import net.alexjf.tmm.exceptions.DatabaseException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class CategoryListActivity extends ActionBarActivity {
    public static final String KEY_INTENTION = "intention";
    public static final String INTENTION_MANAGE = "manage";
    public static final String INTENTION_SELECT = "select";

    private static final int REQCODE_ADD = 0;
    private static final int REQCODE_EDIT = 1;

    private CategoryAdapter adapter;
    private String intention;

    public CategoryListActivity() {
        adapter = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        Intent intent = getIntent();

        intention = intent.getStringExtra(KEY_INTENTION);

        if (intention == null) {
            intention = INTENTION_MANAGE;
        }

        adapter = new CategoryAdapter(this);

        ListView categoriesListView = (ListView) findViewById(
                R.id.category_list);

        View emptyView = findViewById(R.id.category_list_empty);

        categoriesListView.setEmptyView(emptyView);
        categoriesListView.setAdapter(adapter);

        categoriesListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
                Category selectedCategory = adapter.getItem(position);
                Log.d("TMM", "Selected category " + selectedCategory.getName());

                if (intention.equals(INTENTION_SELECT)) {
                    Intent data = new Intent();
                    data.putExtra(Category.KEY_CATEGORY, selectedCategory);
                    setResult(ActionBarActivity.RESULT_OK, data);
                    finish();
                }
                else if (intention.equals(INTENTION_MANAGE)) {
                    // TODO: Browse by category on all money nodes
                }
            }
        });

        registerForContextMenu(categoriesListView);
        updateData();
    }

    private void updateData() {
        List<Category> categories;

        try {
            categories = Category.getCategories();
        } catch (Exception e) {
            Log.e("TMM", "Failed to get categories: " + e.getMessage() +
                    "\n" + e.getStackTrace());
            categories = new ArrayList<Category>();
        }

        adapter.clear();
        for (Category cat : categories) {
            adapter.add(cat);
        }
    }

    private void updateGui() {
        adapter.sort(new Category.Comparator());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_category_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                Intent intent = new Intent(this,
                    CategoryEditActivity.class);
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
                try {
                    Category.deleteCategory(category);
                    adapter.remove(category);
                } catch (DatabaseException e) {
                    Log.e("TMM", "Unable to delete category", e);
                    String strError = getResources().getString(
                            R.string.error_cat_delete);
                    Toast.makeText(this,
                        strError, Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.menu_edit:
                Intent intent = new Intent(this,
                    CategoryEditActivity.class);
                intent.putExtra(Category.KEY_CATEGORY, category);
                startActivityForResult(intent, REQCODE_EDIT);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGui();
    }
}
