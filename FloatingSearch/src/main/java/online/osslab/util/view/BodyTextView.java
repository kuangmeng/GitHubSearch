package online.osslab.util.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 悬浮搜索
 * http://floatingsearch.osslab.online
 */

public class BodyTextView extends TextView {

    private boolean isLocked;

    public BodyTextView(Context context) {
        super(context);
    }

    public BodyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BodyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setHeight(int pixels) {

        if(!isLocked)
           super.setHeight(pixels);
    }

    @Override
    public void setWidth(int pixels) {

        if(!isLocked)
           super.setWidth(pixels);
    }

    @Override
    public void setTextSize(float size) {
        if(!isLocked)
           super.setTextSize(size);
    }

    @Override
    public void setTextSize(int unit, float size) {
        if(!isLocked)
          super.setTextSize(unit, size);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {

        if(!isLocked)
           super.setPadding(left, top, right, bottom);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {

        if(!isLocked)
           super.setText(text, type);
    }

    @Override
    public void setTextScaleX(float size) {

        if(!isLocked)
           super.setTextScaleX(size);
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
        else return new ViewGroup.LayoutParams(0,0);
    }

    public void lock(){
        this.isLocked = true;
    }

    public void unlock(){
        this.isLocked = false;
    }
}
