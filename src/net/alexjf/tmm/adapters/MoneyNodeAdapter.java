package net.alexjf.tmm.adapters;

import java.util.List;

import com.alexjf.tmm.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MoneyNodeAdapter extends ArrayAdapter<MoneyNode> {
    private static final int ROW_VIEW_RESID = R.layout.moneynode_list_row;

    public MoneyNodeAdapter(Context context, List<MoneyNode> objects) {
        super(context, ROW_VIEW_RESID, objects);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // only inflate the view if it's null
        if (view == null) {
            LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(ROW_VIEW_RESID, null);
        }

        TextView nameLabel = (TextView) view.findViewById(R.id.moneynode_name);
        nameLabel.setText(this.getItem(position).getName());

        TextView balanceLabel = (TextView) view.findViewById(R.id.moneynode_balance);
        balanceLabel.setText(this.getItem(position).getBalance());

        return view;
    }
}
