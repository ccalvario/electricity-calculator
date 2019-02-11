package prj.ccalvario.electricitycalculator.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import prj.ccalvario.electricitycalculator.model.Item;
import prj.ccalvario.electricitycalculator.R;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private Item[] mDataset;
    private CustomItemClickListener mListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextViewName;
        public TextView mTextViewWatts;
        public TextView mTextViewTime;
        public TextView mTextViewDays;
        public ViewHolder(View v) {
            super(v);
            mTextViewName = (TextView) v.findViewById(R.id.item_name);
            mTextViewWatts = (TextView) v.findViewById(R.id.item_watts);
            mTextViewTime = (TextView) v.findViewById(R.id.item_time);
            mTextViewDays = (TextView) v.findViewById(R.id.item_days);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CustomAdapter(Item[] myDataset, CustomItemClickListener listener) {
        mDataset = myDataset;
        mListener = listener;
    }

    public void setListContent(Item[] myDataset){
        mDataset = myDataset;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_item, parent, false);
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
        int days = 0;
        holder.mTextViewName.setText(mDataset[position].getName());
        holder.mTextViewWatts.setText(mDataset[position].getWattsStr());
        holder.mTextViewTime.setText(mDataset[position].getTimeStr());
        holder.mTextViewDays.setText(String.valueOf(mDataset[position].getDays()));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}
