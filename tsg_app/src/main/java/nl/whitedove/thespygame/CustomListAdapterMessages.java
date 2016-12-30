package nl.whitedove.thespygame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.List;

import nl.whitedove.thespygame.backend.tsgApi.model.TsgMessage;

class CustomListAdapterMessages extends BaseAdapter {

    private List<TsgMessage> listData;
    private LayoutInflater layoutInflater;

    CustomListAdapterMessages(Context context, List<TsgMessage> listData) {

        this.listData = listData;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listData.size();
    }


    @Override
    public TsgMessage getItem(int position) {
        return listData.get(getCount() - position - 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.messages_list_layout, parent, false);

            holder = new ViewHolder();
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            holder.tvMessageDt = (TextView) convertView.findViewById(R.id.tvMessageDt);
            holder.tvMessageText = (TextView) convertView.findViewById(R.id.tvMessageText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TsgMessage mess = getItem(position);

        DateTime datum = new DateTime(mess.getMessageDt().getMillis());
        holder.tvMessageDt.setText(Helper.tFormat.print(datum));
        holder.tvTitle.setText(mess.getTitle());
        holder.tvMessageText.setText(mess.getMessageTxt());

        return convertView;
    }

    private static class ViewHolder {
        TextView tvTitle;
        TextView tvMessageDt;
        TextView tvMessageText;
    }
}