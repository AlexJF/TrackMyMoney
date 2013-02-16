/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.fragments;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.alexjf.tmm.R;
import net.alexjf.tmm.adapters.ImmediateTransactionAdapter;
import net.alexjf.tmm.domain.Category;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.interfaces.IWithAdapter;
import net.alexjf.tmm.utils.Filter;
import net.alexjf.tmm.utils.Utils;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;

public class ImmedTransactionStatsFragment extends Fragment 
    implements IWithAdapter {
    private static int[] COLORS = new int[] { Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN };

    private ImmediateTransactionAdapter adapter;
    private CategorySeries dataSet;
    private DataSetObserver observer;
    private Filter<ImmediateTransaction> currentFilter;
    private GraphicalView chartView;
    private DefaultRenderer renderer = new DefaultRenderer();

    private static Filter<ImmediateTransaction> incomeFilter = 
        new Filter<ImmediateTransaction>(new Filter.Condition<ImmediateTransaction>() {
            public boolean applies(ImmediateTransaction transaction) {
                return transaction.getValue().signum() > 0;
            }
        });

    private static Filter<ImmediateTransaction> expenseFilter = 
        new Filter<ImmediateTransaction>(new Filter.Condition<ImmediateTransaction>() {
            public boolean applies(ImmediateTransaction transaction) {
                return transaction.getValue().signum() < 0;
            }
        });

    public ImmedTransactionStatsFragment() {
        observer = new ImmedTransactionDataSetObserver();
        currentFilter = incomeFilter;
        dataSet = new CategorySeries("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_immedtransaction_stats, 
                container, false);
        Spinner transactionTypeSpinner = (Spinner) 
            v.findViewById(R.id.transaction_type_spinner);
        transactionTypeSpinner.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, 
                        int position, long id) {
                        switch (position) {
                            case 0:
                                currentFilter = incomeFilter;
                                break;
                            case 1:
                                currentFilter = expenseFilter;
                                break;
                        }
                        updateCurrentTransactionSet();
                    };

                    public void onNothingSelected(AdapterView<?> arg0) {
                        currentFilter = incomeFilter;
                    };
        });

        LinearLayout layout = (LinearLayout) v.findViewById(R.id.stats);
        renderer.setZoomEnabled(false);
        renderer.setZoomButtonsVisible(false);
        renderer.setStartAngle(90);
        renderer.setInScroll(true);
        renderer.setPanEnabled(false);
        renderer.setScale(0.75f);
        renderer.setShowLabels(true);
        renderer.setShowLegend(false);
        chartView = ChartFactory.getPieChartView(
                this.getActivity(), dataSet, renderer);
        layout.addView(chartView, new LayoutParams(LayoutParams.MATCH_PARENT,
          0, 1));
        return v;
    }

    private void updateCurrentTransactionSet() {
        if (adapter == null) {
            return;
        }

        List<ImmediateTransaction> currentTransactionSet = 
            new LinkedList<ImmediateTransaction>();
        Utils.fromAdapterToList(adapter, currentTransactionSet);
        currentFilter.applyInPlace(currentTransactionSet);

        Map<String, Double> perCategoryValues = new HashMap<String, Double>();

        for (SimpleSeriesRenderer simpleRenderer : renderer.getSeriesRenderers()) {
            renderer.removeSeriesRenderer(simpleRenderer);
        }

        dataSet.clear();
        for (ImmediateTransaction transaction : currentTransactionSet) {
            try {
                transaction.load();
                Category cat = transaction.getCategory();
                cat.load();

                perCategoryValues.put(cat.getName(), 
                        transaction.getValue().doubleValue());
            } catch (DatabaseException e) {
                Log.e("TMM", "Failed to load transaction", e);
            }
        }

        for (Entry<String, Double> catValue : perCategoryValues.entrySet()) {
            dataSet.add(catValue.getKey(), catValue.getValue());
            SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
            seriesRenderer.setColor(COLORS[(dataSet.getItemCount() - 1) % COLORS.length]);
            renderer.addSeriesRenderer(seriesRenderer);
        }

        if (chartView != null) {
            chartView.repaint();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (chartView != null) {
            chartView.repaint();
        }
    }

    @Override
    public BaseAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void setAdapter(BaseAdapter adapter) {
        if (this.adapter != null) {
            this.adapter.unregisterDataSetObserver(observer);
        }

        this.adapter = (ImmediateTransactionAdapter) adapter;

        this.adapter.registerDataSetObserver(observer);
        updateCurrentTransactionSet();
    }

    /**
     * This class is responsible for monitoring changes to the complete
     * transaction adapter and update the local transaction subsets when such a
     * change occurs.
     */
    private class ImmedTransactionDataSetObserver extends DataSetObserver {

        @Override
        public void onChanged() {
            updateCurrentTransactionSet();
        }

        @Override
        public void onInvalidated() {
            updateCurrentTransactionSet();
        }
    }
}

