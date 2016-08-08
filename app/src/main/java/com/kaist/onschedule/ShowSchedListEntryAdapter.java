package com.kaist.onschedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by user on 2016-05-25.
 */
/**
 * Adapts NewsEntry objects onto views for lists
 */
public final class ShowSchedListEntryAdapter extends ArrayAdapter<SchedInfo> {

    private final int newsItemLayoutResource;

    public ShowSchedListEntryAdapter(final Context context, final int newsItemLayoutResource) {
        super(context, 0);
        this.newsItemLayoutResource = newsItemLayoutResource;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {

        // We need to get the best view (re-used if possible) and then
        // retrieve its corresponding ViewHolder, which optimizes lookup efficiency
        final View view = getWorkingView(convertView);
        final ViewHolder viewHolder = getViewHolder(view);
        final SchedInfo entry = getItem(position);

        // Setting the title view is straightforward
        viewHolder.titleView.setText(entry.get_date());

        // Setting the subTitle view requires a tiny bit of formatting
        final String formattedSubTitle = String.format("By %s From %s To %s",
                entry.get_time(), entry.get_departure(), entry.get_destination()
        );

        viewHolder.subTitleView.setText(formattedSubTitle);

        return view;
    }

    private View getWorkingView(final View convertView) {
        // The workingView is basically just the convertView re-used if possible
        // or inflated new if not possible
        View workingView = null;

        if(null == convertView) {
            final Context context = getContext();
            final LayoutInflater inflater = (LayoutInflater)context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);

            workingView = inflater.inflate(newsItemLayoutResource, null);
        } else {
            workingView = convertView;
        }

        return workingView;
    }

    private ViewHolder getViewHolder(final View workingView) {
        // The viewHolder allows us to avoid re-looking up view references
        // Since views are recycled, these references will never change
        final Object tag = workingView.getTag();
        ViewHolder viewHolder = null;


        if(null == tag || !(tag instanceof ViewHolder)) {
            viewHolder = new ViewHolder();

            viewHolder.titleView = (TextView) workingView.findViewById(R.id.news_entry_title);
            viewHolder.subTitleView = (TextView) workingView.findViewById(R.id.news_entry_subtitle);

            workingView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) tag;
        }

        return viewHolder;
    }

    /**
     * ViewHolder allows us to avoid re-looking up view references
     * Since views are recycled, these references will never change
     */
    private static class ViewHolder {
        public TextView titleView;
        public TextView subTitleView;
    }
}
