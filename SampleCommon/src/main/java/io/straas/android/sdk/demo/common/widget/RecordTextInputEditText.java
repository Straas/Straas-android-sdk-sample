package io.straas.android.sdk.demo.common.widget;

import android.content.*;
import android.util.*;

import com.google.android.material.textfield.*;

import io.straas.android.sdk.demo.common.*;

public class RecordTextInputEditText extends TextInputEditText {

    private static final String SP_NAME = "SP_RECORD_TEXT_INPUT_EDIT_TEXT";
    private static final String DEFAULT_SP_KEY = "DEFAULT_SP_KEY";

    private boolean mListening;

    public RecordTextInputEditText(Context context) {
        this(context, null);
    }

    public RecordTextInputEditText(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }

    public RecordTextInputEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setText(getRecord());
        mListening = true;
    }

    private SharedPreferences getSharedPreferences() {
        return getContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    private String getSharedPreferencesKey() {
        if (getTag() != null) {
            return getTag().toString();
        }
        return DEFAULT_SP_KEY;
    }

    private String getRecord() {
        return getSharedPreferences().getString(getSharedPreferencesKey(), null);
    }

    private void recordCurrentText() {
        if (getText() == null) {
            return;
        }
        String text = getText().toString();
        getSharedPreferences().edit().putString(getSharedPreferencesKey(), text).apply();
    }

    //============================================================================
    // override methods
    //============================================================================
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (mListening) {
            recordCurrentText();
        }
    }
}
