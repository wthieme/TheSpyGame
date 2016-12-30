package nl.whitedove.thespygame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

class CustomListAdapterLocations extends BaseAdapter {

    private List<String> listData;
    private LayoutInflater layoutInflater;

    CustomListAdapterLocations(Context context, List<String> listData) {

        this.listData = listData;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public String getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.locations_list_layout, parent, false);

            holder = new ViewHolder();
            holder.tvPossibleLocation = (TextView) convertView.findViewById(R.id.tvPossibleLocation);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String location = getItem(position);
        holder.tvPossibleLocation.setText(location);

        return convertView;
    }

    private static class ViewHolder {
        TextView tvPossibleLocation;
    }
}