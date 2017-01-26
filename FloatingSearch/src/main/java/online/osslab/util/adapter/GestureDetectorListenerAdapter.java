package online.osslab.util.adapter;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * 悬浮搜索
 * http://floatingsearch.osslab.online
 */

public abstract class GestureDetectorListenerAdapter implements
        GestureDetector.OnGestureListener{

    private static final String TAG = "GestureDetectorListenerAdapter";

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
