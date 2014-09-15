/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import net.alexjf.tmm.R;

import java.util.List;

public abstract class TwoLineTextAdapter<T> extends ArrayAdapter<T> {
	private static final int ROW_VIEW_RESID = R.layout.list_row_2lines;

	private Integer defaultTextViewColor = null;

	public TwoLineTextAdapter(Context context) {
		super(context, ROW_VIEW_RESID);
	}

	public TwoLineTextAdapter(Context context,
			List<T> objects) {
		super(context, ROW_VIEW_RESID, objects);
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		// only inflate the view if it's null
		if (view == null) {
			LayoutInflater vi = (LayoutInflater) this.getContext().
					getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = vi.inflate(ROW_VIEW_RESID, null);
		}

		TextView titleTextView = (TextView) view.findViewById(R.id.item_title);
		TextView valueTextView = (TextView) view.findViewById(R.id.item_value);
		T t = (T) getItem(position);

		if (defaultTextViewColor == null) {
			defaultTextViewColor = valueTextView.getTextColors().getDefaultColor();
		}

		valueTextView.setTextColor(defaultTextViewColor);

		setTitleText(titleTextView, t);
		setValueText(valueTextView, t);

		return view;
	}

	public abstract void setTitleText(TextView titleTextView, T item);

	public abstract void setValueText(TextView titleTextView, T item);
}
