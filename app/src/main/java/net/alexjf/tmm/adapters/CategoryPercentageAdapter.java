/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.adapters;

import java.text.NumberFormat;
import java.util.List;

import org.joda.money.Money;

import net.alexjf.tmm.R;
import net.alexjf.tmm.adapters.CategoryPercentageAdapter.CategoryPercentageInfo;
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
import android.widget.ProgressBar;
import android.widget.TextView;

public class CategoryPercentageAdapter extends ArrayAdapter<CategoryPercentageInfo> {
    private static final int ROW_VIEW_RESID = R.layout.list_row_category_percentage;
    private static final int ROW_VIEW_NAMETEXTID = R.id.category_name;

    private NumberFormat percentageFormat;

    public CategoryPercentageAdapter(Context context) {
        super(context, ROW_VIEW_RESID, ROW_VIEW_NAMETEXTID);
        init();
    }

    public CategoryPercentageAdapter(Context context,
            List<CategoryPercentageInfo> objects) {
        super(context, ROW_VIEW_RESID, ROW_VIEW_NAMETEXTID, objects);
        init();
    }

    private void init() {
        percentageFormat = NumberFormat.getPercentInstance();
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

        CategoryPercentageInfo catPercInfo = getItem(position);
        Category cat = catPercInfo.getCategory();
        try {
            cat.load();

            ImageView iconImageView = (ImageView) view.findViewById(R.id.category_icon);
            String iconName = cat.getIcon();
            int iconId = DrawableResolver.getInstance().getDrawableId(iconName);
            if (iconId != 0) {
                iconImageView.setImageResource(iconId);
            }

            iconImageView.setColorFilter(catPercInfo.getColor());

            TextView nameLabel = (TextView) view.findViewById(R.id.category_name);
            nameLabel.setText(cat.getName());

            TextView valueLabel = (TextView) view.findViewById(R.id.category_value);
            valueLabel.setText(catPercInfo.getValue().toString());

            TextView percentageLabel = (TextView) view.findViewById(R.id.category_percentage_text);
            percentageLabel.setText(percentageFormat.format(catPercInfo.getPercentage()));

            ProgressBar percentageBar = (ProgressBar) view.findViewById(R.id.category_percentage_bar);
            percentageBar.setProgress((int) Math.round(catPercInfo.getPercentage() * 100));
        } catch (DatabaseException e) {
            Log.e("TMM", e.getMessage(), e);
        }

        return view;
    }

    public static class CategoryPercentageInfo {
        private Category category;
        private double percentage;
        private Money value;
        private int color;

        public CategoryPercentageInfo(Category category, Money value,
                double percentage, int color) {
            this.category = category;
            this.percentage = percentage;
            this.value = value;
            this.color = color;
        }

        public Category getCategory() {
            return category;
        }

        public double getPercentage() {
            return percentage;
        }

        public Money getValue() {
            return value;
        }

        public int getColor() {
            return color;
        }

        public static class PercentageComparator
                implements java.util.Comparator<CategoryPercentageInfo> {
            int modifier;

            public PercentageComparator(boolean reverse) {
                modifier = reverse ? -1 : 1;
            }

            public int compare(CategoryPercentageInfo lhs,
                               CategoryPercentageInfo rhs) {
                return modifier *
                    Double.compare(lhs.getPercentage(), rhs.getPercentage());
            }
        }
    }
}
