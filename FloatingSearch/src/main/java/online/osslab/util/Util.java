package online.osslab.util;

/**
 * 悬浮搜索
 * http://floatingsearch.osslab.online
 */

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class Util {

    private static final String TAG = "Util";

    public static void showSoftKeyboard(final Context context, final EditText editText){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }

    public static void closeSoftKeyboard(Activity activity){

        View currentFocusView = activity.getCurrentFocus();
        if (currentFocusView != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocusView.getWindowToken(), 0);
        }
    }

    public static int dpToPx(int dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return (int) (dp * metrics.density);
    }

    public static int pxToDp(int px){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return (int) (px /metrics.density);
    }

    public static int spToPx(int sp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics);
    }

    public static int pxToSp(int px) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return px/(int)metrics.scaledDensity;
    }

    public static int getScreenWidth(Activity activity){

        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        return outMetrics.widthPixels;
    }

    public static int getScreenHeight(Activity activity){

        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        return outMetrics.heightPixels;
    }

    public static Drawable setIconColor(Drawable icon, int color){
        DrawableCompat.wrap(icon);
        DrawableCompat.setTint(icon, color);
        return icon;
    }
}
