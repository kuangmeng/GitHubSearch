package online.osslab.util.adapter;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * 悬浮搜索
 * http://floatingsearch.osslab.online
 */

public abstract class TextWatcherAdapter implements TextWatcher {

    private static final String TAG = "TextWatcherAdapter";

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
