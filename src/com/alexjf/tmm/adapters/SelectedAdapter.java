package com.alexjf.tmm.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SelectedAdapter<T> extends ArrayAdapter<T> {

    // used to keep selected position in ListView
    private int selectedPos = -1;	// init value for not-selected
    private int rowViewResourceId;
    private int textViewResourceId;
    private int normalColorResourceId;
    private int selectedColorResourceId;

    public SelectedAdapter(Context context, int rowViewResourceId, 
            int textViewResourceId, List<T> objects, int normalColorResourceId,
            int selectedColorResourceId) {
        super(context, rowViewResourceId, textViewResourceId, objects);
        this.rowViewResourceId = rowViewResourceId;
        this.textViewResourceId = textViewResourceId;
        this.normalColorResourceId = normalColorResourceId;
        this.selectedColorResourceId = selectedColorResourceId;
    }

    public void setSelectedPosition(int pos) {
        selectedPos = pos;
        // inform the view of this change
        notifyDataSetChanged();
    }

    public int getSelectedPosition(){
        return selectedPos;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // only inflate the view if it's null
        if (view == null) {
            LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(rowViewResourceId, null);
        }

        // get text view
        TextView label = (TextView) view.findViewById(textViewResourceId);
        label.setText(this.getItem(position).toString());

        // change the row color based on selected state
        if (selectedPos == position) {
            view.setBackgroundResource(selectedColorResourceId);
        } else {
            view.setBackgroundResource(normalColorResourceId);
        }

        return view;
    }
}
