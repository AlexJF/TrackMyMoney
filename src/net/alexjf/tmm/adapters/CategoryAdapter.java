/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.adapters;

import java.util.List;

import net.alexjf.tmm.R;
import net.alexjf.tmm.domain.Category;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.utils.DrawableResolver;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CategoryAdapter extends ArrayAdapter<Category> {
    private static final int ROW_VIEW_RESID = R.layout.list_row_category;
    private static final int ROW_VIEW_NAMETEXTID = R.id.category_name;

    public CategoryAdapter(Context context) {
        super(context, ROW_VIEW_RESID, ROW_VIEW_NAMETEXTID);
    }

    public CategoryAdapter(Context context, List<Category> objects) {
        super(context, ROW_VIEW_RESID, ROW_VIEW_NAMETEXTID, objects);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        return getCustomView(position, view, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View view, ViewGroup parent) {
        // only inflate the view if it's null
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(ROW_VIEW_RESID, null);
        }

        Category cat = getItem(position);
        try {
            cat.load();

            ImageView iconImageView = (ImageView) view.findViewById(R.id.category_icon);
            String iconName = cat.getIcon();
            int iconId = DrawableResolver.getInstance().getDrawableId(iconName);
            if (iconId != 0) {
                iconImageView.setImageResource(iconId);
            }

            TextView nameLabel = (TextView) view.findViewById(R.id.category_name);
            nameLabel.setText(cat.getName());
        } catch (DatabaseException e) {
            Log.e("TMM", e.getMessage(), e);
        }

        return view;
    }

}
