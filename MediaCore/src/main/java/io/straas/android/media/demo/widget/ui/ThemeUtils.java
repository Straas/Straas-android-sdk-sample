package io.straas.android.media.demo.widget.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.v4.graphics.ColorUtils;
import android.util.TypedValue;

public class ThemeUtils {
    private static final ThreadLocal<TypedValue> TL_TYPED_VALUE = new ThreadLocal<>();

    static final int[] DISABLED_STATE_SET = new int[]{-android.R.attr.state_enabled};
    static final int[] FOCUSED_STATE_SET = new int[]{android.R.attr.state_focused};
    static final int[] ACTIVATED_STATE_SET = new int[]{android.R.attr.state_activated};
    static final int[] PRESSED_STATE_SET = new int[]{android.R.attr.state_pressed};
    static final int[] CHECKED_STATE_SET = new int[]{android.R.attr.state_checked};
    static final int[] SELECTED_STATE_SET = new int[]{android.R.attr.state_selected};
    static final int[] EMPTY_STATE_SET = new int[0];

    private static final int[] TEMP_ARRAY = new int[1];

    public static ColorStateList getDefaultColorStateList(Context context) {
        return getDefaultColorStateList(context, android.support.v7.appcompat.R.attr.colorControlNormal);
    }

    public static ColorStateList getDefaultColorStateList(Context context, @AttrRes int baseColorThemeAttr) {
        /**
         * Generate the default color state list which uses the colorControl attributes.
         * Order is important here. The default enabled state needs to go at the bottom.
         */

        final int colorControlNormal = getThemeAttrColor(context, baseColorThemeAttr);
        final int colorControlActivated = getThemeAttrColor(context,
                android.support.v7.appcompat.R.attr.colorControlActivated);

        return getColorStateList(context, colorControlNormal, colorControlActivated);
    }

    private static ColorStateList getColorStateList(Context context, int colorControlNormal, int colorControlActivated) {

        final int[][] states = new int[7][];
        final int[] colors = new int[7];
        int i = 0;

        // Disabled state
        states[i] = DISABLED_STATE_SET;
        colors[i] = getDisabledThemeAttrColor(context, android.support.v7.appcompat.R.attr.colorControlNormal);
        i++;

        states[i] = FOCUSED_STATE_SET;
        colors[i] = colorControlActivated;
        i++;

        states[i] = ACTIVATED_STATE_SET;
        colors[i] = colorControlActivated;
        i++;

        states[i] = PRESSED_STATE_SET;
        colors[i] = colorControlActivated;
        i++;

        states[i] = CHECKED_STATE_SET;
        colors[i] = colorControlActivated;
        i++;

        states[i] = SELECTED_STATE_SET;
        colors[i] = colorControlActivated;
        i++;

        // Default enabled state
        states[i] = EMPTY_STATE_SET;
        colors[i] = colorControlNormal;

        return new ColorStateList(states, colors);
    }

    public static int getDisabledThemeAttrColor(Context context, int attr) {
        final ColorStateList csl = getThemeAttrColorStateList(context, attr);
        if (csl != null && csl.isStateful()) {
            // If the CSL is stateful, we'll assume it has a disabled state and use it
            return csl.getColorForState(DISABLED_STATE_SET, csl.getDefaultColor());
        } else {
            // Else, we'll generate the color using disabledAlpha from the theme

            final TypedValue tv = getTypedValue();
            // Now retrieve the disabledAlpha value from the theme
            context.getTheme().resolveAttribute(android.R.attr.disabledAlpha, tv, true);
            final float disabledAlpha = tv.getFloat();

            return getThemeAttrColor(context, attr, disabledAlpha);
        }
    }

    private static TypedValue getTypedValue() {
        TypedValue typedValue = TL_TYPED_VALUE.get();
        if (typedValue == null) {
            typedValue = new TypedValue();
            TL_TYPED_VALUE.set(typedValue);
        }
        return typedValue;
    }

    public static int getThemeAttrColor(Context context, int attr) {
        TEMP_ARRAY[0] = attr;
        TypedArray a = context.obtainStyledAttributes(null, TEMP_ARRAY);
        try {
            return a.getColor(0, 0);
        } finally {
            a.recycle();
        }
    }

    static int getThemeAttrColor(Context context, int attr, float alpha) {
        final int color = getThemeAttrColor(context, attr);
        final int originalAlpha = Color.alpha(color);
        return ColorUtils.setAlphaComponent(color, Math.round(originalAlpha * alpha));
    }

    public static ColorStateList getThemeAttrColorStateList(Context context, int attr) {
        TEMP_ARRAY[0] = attr;
        TypedArray a = context.obtainStyledAttributes(null, TEMP_ARRAY);
        try {
            return a.getColorStateList(0);
        } finally {
            a.recycle();
        }
    }
}
