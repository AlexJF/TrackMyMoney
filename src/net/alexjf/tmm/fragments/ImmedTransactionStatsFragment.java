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
import net.alexjf.tmm.adapters.CategoryPercentageAdapter;
import net.alexjf.tmm.adapters.CategoryPercentageAdapter.CategoryPercentageInfo;
import net.alexjf.tmm.adapters.ImmediateTransactionAdapter;
import net.alexjf.tmm.domain.Category;
import net.alexjf.tmm.domain.ImmediateTransaction;
import net.alexjf.tmm.domain.MoneyNode;
import net.alexjf.tmm.exceptions.DatabaseException;
import net.alexjf.tmm.interfaces.IWithAdapter;
import net.alexjf.tmm.utils.AsyncTaskWithProgressDialog;
import net.alexjf.tmm.utils.Filter;
import net.alexjf.tmm.utils.Utils;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.content.res.Configuration;
import android.database.DataSetObserver;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class ImmedTransactionStatsFragment extends Fragment 
    implements IWithAdapter {
    private final static String KEY_SPINNERSELECTION = "spinnerSelection";

    private static int[] colors = null;

    private ImmediateTransactionAdapter adapter;
    private CategoryPercentageAdapter catPercentageAdapter;
    private CategorySeries dataSet;
    private DataSetObserver observer;
    private Filter<ImmediateTransaction> currentFilter;
    private GraphicalView chartView;
    private DefaultRenderer renderer = new DefaultRenderer();
    private ListView percentagesListView;
    private Spinner transactionTypeSpinner;

    private static Filter<ImmediateTransaction> incomeFilter = 
        new Filter<ImmediateTransaction>(new Filter.Condition<ImmediateTransaction>() {
            public boolean applies(ImmediateTransaction transaction) {
                try {
                    transaction.load();
                } catch (DatabaseException e) {
                    Log.e("TMM", e.getMessage(), e);
                }
                return transaction.getValue().signum() > 0;
            }
        });

    private static Filter<ImmediateTransaction> expenseFilter = 
        new Filter<ImmediateTransaction>(new Filter.Condition<ImmediateTransaction>() {
            public boolean applies(ImmediateTransaction transaction) {
                try {
                    transaction.load();
                } catch (DatabaseException e) {
                    Log.e("TMM", e.getMessage(), e);
                }
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
        if (colors == null) {
            colors = new int[] {
                getResources().getColor(R.color.pie1),
                getResources().getColor(R.color.pie2),
                getResources().getColor(R.color.pie3),
                getResources().getColor(R.color.pie4),
                getResources().getColor(R.color.pie5),
                getResources().getColor(R.color.pie6),
                getResources().getColor(R.color.pie7),
                getResources().getColor(R.color.pie8),
                getResources().getColor(R.color.pie9),
                getResources().getColor(R.color.pie10)
            };
        }

        View v = inflater.inflate(R.layout.fragment_immedtransaction_stats, 
                container, false);
        transactionTypeSpinner = (Spinner) 
            v.findViewById(R.id.transaction_type_spinner);
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.stats);

        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layout.setOrientation(LinearLayout.HORIZONTAL);
        }

        renderer.setZoomEnabled(false);
        renderer.setLabelsTextSize(Utils.displayPixelsToPixels(getActivity(), 12));
        renderer.setZoomButtonsVisible(false);
        renderer.setStartAngle(90);
        renderer.setInScroll(true);
        renderer.setPanEnabled(false);
        renderer.setScale(0.8f);
        renderer.setShowLabels(true);
        renderer.setShowLegend(false);
        chartView = ChartFactory.getPieChartView(
                this.getActivity(), dataSet, renderer);
        chartView.setLayoutParams(new ListView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            Utils.displayPixelsToPixels(getActivity(), 250)));

        percentagesListView = new ListView(getActivity());

        String currency = null;

        Bundle bundle = getArguments();
        if (bundle != null) {
            currency = bundle.getString(MoneyNode.KEY_CURRENCY);
        }

        catPercentageAdapter = new CategoryPercentageAdapter(getActivity(), 
                currency);
        percentagesListView.addHeaderView(chartView);
        percentagesListView.setAdapter(catPercentageAdapter);
        layout.addView(percentagesListView, new LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));

        if (savedInstanceState != null) {
            int selectedSpinnerPosition = 
                savedInstanceState.getInt(KEY_SPINNERSELECTION);
            transactionTypeSpinner.setOnItemSelectedListener(null);
            transactionTypeSpinner.setSelection(selectedSpinnerPosition);
        }

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

        return v;
    }

    private void updateCurrentTransactionSet() {
        if (adapter == null || catPercentageAdapter == null) {
            return;
        }

        if (adapter.getCount() == 0 && catPercentageAdapter.getCount() == 0) {
            return;
        }

        AsyncTaskWithProgressDialog<ImmediateTransactionAdapter, Void, Map<Category, Double>> asyncTask = 
            new AsyncTaskWithProgressDialog<ImmediateTransactionAdapter, Void, Map<Category, Double>> 
            (getActivity(), "Analyzing stats...") {
                // TODO: Make this more efficient by already getting all the
                // data from the database
                @Override
                protected Map<Category, Double> doInBackground(ImmediateTransactionAdapter... args) {
                    List<ImmediateTransaction> currentTransactionSet = 
                        new LinkedList<ImmediateTransaction>();
                    Utils.fromAdapterToList(adapter, currentTransactionSet);
                    currentFilter.applyInPlace(currentTransactionSet);

                    Map<Category, Double> perCategoryValues = new HashMap<Category, Double>();

                    for (ImmediateTransaction transaction : currentTransactionSet) {
                        try {
                            transaction.load();
                            Category cat = transaction.getCategory();
                            cat.load();

                            double transactionValue = 
                                transaction.getValue().abs().doubleValue();
                            Double existingValue = perCategoryValues.get(cat);

                            if (existingValue != null) {
                                transactionValue += existingValue;
                            }

                            perCategoryValues.put(cat, transactionValue);
                        } catch (DatabaseException e) {
                            setThrowable(e);
                        }
                    }

                    return perCategoryValues;
                };

                @Override
                protected void onPostExecuteSuccess(Map<Category, Double> perCategoryValues) {
                    for (SimpleSeriesRenderer simpleRenderer : renderer.getSeriesRenderers()) {
                        renderer.removeSeriesRenderer(simpleRenderer);
                    }

                    dataSet.clear();

                    int totalValue = 0;
                    for (Entry<Category, Double> catValue : perCategoryValues.entrySet()) {
                        totalValue += catValue.getValue();
                    }

                    catPercentageAdapter.clear();
                    catPercentageAdapter.setNotifyOnChange(false);
                    for (Entry<Category, Double> catValue : perCategoryValues.entrySet()) {
                        Category category = catValue.getKey();
                        int color = colors[dataSet.getItemCount() % colors.length];
                        double categoryTotalValue = catValue.getValue();
                        dataSet.add(category.getName(), catValue.getValue());
                        catPercentageAdapter.add(new CategoryPercentageInfo(category, 
                                    categoryTotalValue, categoryTotalValue / totalValue,
                                    color));
                        SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
                        seriesRenderer.setColor(color);
                        renderer.addSeriesRenderer(seriesRenderer);
                    }

                    catPercentageAdapter.sort(
                            new CategoryPercentageInfo.PercentageComparator(true));
                    catPercentageAdapter.notifyDataSetChanged();

                    if (chartView != null) {
                        chartView.repaint();
                    }
                }

                @Override
                protected void onPostExecuteFail(Throwable e) {
                    Toast.makeText(getContext(), 
                        "Stat analysis error! (" + e.getMessage() + ")", 3).show();
                    Log.e("TMM", e.getMessage(), e);
                }
            };

        asyncTask.execute(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public BaseAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void setAdapter(BaseAdapter adapter) {
        if (this.adapter == adapter) return;

        if (this.adapter != null) {
            this.adapter.unregisterDataSetObserver(observer);
        }

        this.adapter = (ImmediateTransactionAdapter) adapter;

        this.adapter.registerDataSetObserver(observer);
        updateCurrentTransactionSet();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SPINNERSELECTION, 
                transactionTypeSpinner.getSelectedItemPosition());
        super.onSaveInstanceState(outState);
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
        }
    }
}

