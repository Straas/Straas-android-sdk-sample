package io.straas.android.media.demo.widget.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.AttributeSet;

import io.straas.android.media.demo.*;

public class RecordTextInputEditText extends android.support.design.widget.TextInputEditText {

    private static final String KEY_RECORD = "RECORD";
    private static final String DEFAULT_SP_NAME = "SP_RECORD_TEXT_INPUT_EDIT_TEXT";

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
    }

    private String getRecord() {
        return getSharedPreferences().getString(KEY_RECORD, null);
    }

    private SharedPreferences getSharedPreferences() {
        return getContext().getSharedPreferences(getSharedPreferencesName(), Context.MODE_PRIVATE);
    }

    private String getSharedPreferencesName() {
        if (getTag() != null) {
            return getTag().toString();
        }
        return DEFAULT_SP_NAME;
    }

    public void recordCurrentText() {
        if (getText() == null) {
            return;
        }
        String text = getText().toString();
        getSharedPreferences().edit().putString(KEY_RECORD, text).apply();
    }
}
