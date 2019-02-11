package prj.ccalvario.electricitycalculator.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import prj.ccalvario.electricitycalculator.R;
import prj.ccalvario.electricitycalculator.model.Rate;

public class RateAdapter extends RecyclerView.Adapter<RateAdapter.ViewHolder> {

    private Rate[] mDataset;
    private CustomItemClickListener mListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextViewName;
        public TextView mTextViewKwh;
        public TextView mTextViewCost;
        public ViewHolder(View v) {
            super(v);
            mTextViewName = (TextView) v.findViewById(R.id.textview_rates_name);
            mTextViewKwh = (TextView) v.findViewById(R.id.textview_rates_kwh);
            mTextViewCost = (TextView) v.findViewById(R.id.textview_rates_cost);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RateAdapter(Rate[] myDataset, CustomItemClickListener listener) {
        mDataset = myDataset;
        mListener = listener;
    }

    public void setListContent(Rate[] myDataset){
        mDataset = myDataset;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RateAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_rate, parent, false);
        final ViewHolder vh = new ViewHolder(itemView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(v, vh.getAdapterPosition());
            }
        });

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextViewName.setText(mDataset[position].getName());
        holder.mTextViewKwh.setText(mDataset[position].getKwhStr());
        holder.mTextViewCost.setText(String.valueOf(mDataset[position].getCost()));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}
