package online.osslab.util.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * 悬浮搜索
 * http://floatingsearch.osslab.online
 */

public class IconImageView extends ImageView {

    private boolean isLocked;

    public IconImageView(Context context) {
        super(context);
    }

    public IconImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IconImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {

        if(!isLocked)
            super.setPadding(left, top, right, bottom);
    }

    @Override
    public void setScaleX(float scaleX) {

        if(!isLocked)
            super.setScaleX(scaleX);
    }

    @Override
    public void setScaleY(float scaleY) {

        if(!isLocked)
            super.setScaleY(scaleY);
    }

    @Override
    public ViewGroup.LayoutParams getLayoutParams() {

        if(!isLocked)
            return super.getLayoutParams();
        else return null;
    }

    public void lock(){
        this.isLocked = true;
    }

    public void unlock(){
        this.isLocked = false;
    }
}
