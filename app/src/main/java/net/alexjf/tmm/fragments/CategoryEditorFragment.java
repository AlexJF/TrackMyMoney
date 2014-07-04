/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.fragments;

import net.alexjf.tmm.R;
import net.alexjf.tmm.domain.Category;
import net.alexjf.tmm.domain.DatabaseHelper;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.fragments.IconPickerFragment.OnIconPickedListener;
import net.alexjf.tmm.utils.DrawableResolver;
import net.alexjf.tmm.views.SelectorButton;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class CategoryEditorFragment extends Fragment 
    implements OnIconPickedListener {
    private static final String KEY_CURRENTCATEGORY = "currentCategory";
    private static final String KEY_SELECTEDICON = "selectedIcon";
    
    private static final String TAG_DRAWABLEPICKER = "drawablePicker";

    private OnCategoryEditListener listener;

    private Category category;
    private String selectedDrawableName;

    private IconPickerFragment iconPicker;

    private EditText nameText;
    private SelectorButton iconImageButton;
    private Button addButton;

    public interface OnCategoryEditListener {
        public void onCategoryCreated(Category category);
        public void onCategoryEdited(Category category);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_category_editor, container, false);

        nameText = (EditText) v.findViewById(R.id.name_text);
        iconImageButton = (SelectorButton) v.findViewById(R.id.icon_button);
        addButton = (Button) v.findViewById(R.id.add_button);

        iconPicker = (IconPickerFragment) 
            getFragmentManager().findFragmentByTag(TAG_DRAWABLEPICKER);

        if (iconPicker == null) {
            iconPicker = new IconPickerFragment();
        }

        iconPicker.setListener(this);

        iconImageButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                iconPicker.show(getFragmentManager(), TAG_DRAWABLEPICKER);
            }
        });

        addButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (!validateInputFields()) {
                    return;
                }

                String name = nameText.getText().toString().trim();

                if (category == null) {
                    Category newCategory = new Category(name, selectedDrawableName);
                    listener.onCategoryCreated(newCategory);
                } else {
                    category.setName(name);
                    category.setIcon(selectedDrawableName);
                    listener.onCategoryEdited(category);
                }

            }
        });

        if (savedInstanceState != null) {
            category = savedInstanceState.getParcelable(KEY_CURRENTCATEGORY);
        }
        
        updateCategoryFields();

        if (savedInstanceState != null) {
            selectedDrawableName = savedInstanceState.getString(KEY_SELECTEDICON);
            int iconId = DrawableResolver.getInstance().getDrawableId(selectedDrawableName);
            iconImageButton.setDrawableId(iconId);
        }

        return v;
    }

    public void onIconPicked(int drawableId, String drawableName) {
        iconImageButton.setDrawableId(drawableId);
        iconImageButton.setError(false);
        selectedDrawableName = drawableName;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_SELECTEDICON, selectedDrawableName);
        outState.putParcelable(KEY_CURRENTCATEGORY, category);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnCategoryEditListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + 
                    " must implement OnCategoryEditListener");
        }
    }

    /**
     * @return the category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(Category category) {
        Category prevCategory = this.category;
        this.category = category;

        if (prevCategory != category) {
            updateCategoryFields();
        }
    }

    private void updateCategoryFields() {
        // If we are adding a new category, reset all fields
        if (category == null) {
            nameText.setText("");
            iconImageButton.setDrawableId(0);
            addButton.setText(R.string.add);
        // If we are editing a category, fill fields with current information
        } else {
            nameText.setText(category.getName());
            selectedDrawableName = category.getIcon();
            int iconId = DrawableResolver.getInstance().getDrawableId(selectedDrawableName);
            iconImageButton.setDrawableId(iconId);
            addButton.setText(R.string.edit);
        }
    }

    private boolean validateInputFields() {
        boolean error = false;

        Resources res = getResources();
        Drawable errorDrawable = 
            res.getDrawable(R.drawable.indicator_input_error);
        errorDrawable.setBounds(0, 0, 
                errorDrawable.getIntrinsicWidth(), 
                errorDrawable.getIntrinsicHeight());
        String name = nameText.getText().toString();

        String nameError = null;
        if (TextUtils.isEmpty(name)) {
            nameError = res.getString(R.string.error_name_not_empty);
        }
        else {
            try {
                // If we are adding a new category and the name already exists
                if (category == null && 
                    DatabaseHelper.getInstance().hasCategoryWithName(name)) {
                    nameError = 
                        res.getString(R.string.error_cat_name_already_exists);
                }
            } catch (DatabaseException e) {
                nameError = res.getString(R.string.error_cat_determine_exists);
            }
        }

        if (nameError != null) {
            nameText.setError(nameError, errorDrawable);
            error = true;
        }

        if (TextUtils.isEmpty(selectedDrawableName)) {
            iconImageButton.setError(true);
            error = true;
        }

        return !error;
    }
}

