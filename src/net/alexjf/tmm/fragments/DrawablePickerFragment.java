/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.fragments;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import net.alexjf.tmm.R;
import net.alexjf.tmm.adapters.DrawableAdapter;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class DrawablePickerFragment extends DialogFragment {
    private OnDrawablePickedListener listener;
    private String filter;

    public interface OnDrawablePickedListener {
        public void onDrawablePicked(int drawableId, String drawableName);
    }

    public DrawablePickerFragment(OnDrawablePickedListener listener, String filter) {
        this.listener = listener;
        this.filter = filter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_drawable_picker, container, false);
        GridView drawableGrid = (GridView) v.findViewById(R.id.drawable_grid);
        List<Integer> drawableIds = new LinkedList<Integer>();

        for (Field field : R.drawable.class.getFields()) {
            String fieldName = field.getName();

            if (fieldName.startsWith(filter)) {
                try {
                    drawableIds.add(field.getInt(R.drawable.class));
                } catch (IllegalAccessException e) {
                    Log.e("TMM", "Failure during drawable listing", e);
                }
            }
        }

        final DrawableAdapter adapter = new DrawableAdapter(getActivity(), drawableIds);

        drawableGrid.setAdapter(adapter);
        drawableGrid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, 
                int position, long id) {
                Integer drawableId = adapter.getItem(position);
                String drawableName = getResources().getResourceEntryName(drawableId);
                dismiss();
                listener.onDrawablePicked(drawableId, drawableName);
            }
        });
        return v;
    }
}

