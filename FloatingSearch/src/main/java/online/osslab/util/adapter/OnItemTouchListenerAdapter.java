package online.osslab.util.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;

/**
 * 悬浮搜索
 * http://floatingsearch.osslab.online
 */

public abstract class OnItemTouchListenerAdapter implements RecyclerView.OnItemTouchListener {

    private static final String TAG = "OnItemTouchListenerAdapter";

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent e) {
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
