/*
 * Copyright (c) 2017.
 * 个人版权所有
 * kuangmeng.net
 */

package hitamigos.githubsearch;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class PageAdapter extends PagerAdapter {
    List<View> views;
    public PageAdapter(List<View> views) {
        this.views = views;
    }
    @Override
    public int getCount(){
        return views.size();
    }
    @Override
    public boolean isViewFromObject(View view, Object object){
        return view==object;
    }
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view=views.get(position);
        container.addView(view);
        return  view;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));
    }
}
