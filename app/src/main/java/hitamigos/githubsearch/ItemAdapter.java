/*
 * Copyright (c) 2017.
 * 个人版权所有
 * kuangmeng.net
 */

package hitamigos.githubsearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.List;

import krelve.app.kuaihu.model.StoriesEntity;
import krelve.app.kuaihu.util.PreUtils;

/**
 * Created by wwjun.wang on 2015/8/14.
 */
public class ItemAdapter extends BaseAdapter {
    private List<HashMap> entities;
    private Context context;
    public ItemAdapter(Context context, List<HashMap> items) {
        this.context = context;
        entities = items;
    }
    @Override
    public int getCount() {
        return entities.size();
    }

    @Override
    public Object getItem(int position) {
        return entities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.list, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.info = (TextView) convertView.findViewById(R.id.info);
            viewHolder.link = (TextView) convertView.findViewById(R.id.link);
            viewHolder.star = (TextView) convertView.findViewById(R.id.star);
            viewHolder.fork = (TextView) convertView.findViewById(R.id.fork);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        HashMap entity = entities.get(position);
        viewHolder.name.setText(entity.get("name"));

        return convertView;
    }

    public static class ViewHolder {
        TextView name;
        TextView info;
        TextView link;
        TextView star;
        TextView fork;
    }

}
