package io.straas.android.media.demo.widget.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableWrapper;
import android.support.v4.util.LruCache;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.Gravity;

import io.straas.android.sdk.demo.R;


/**
 * Lots of codes modified from Support library
 */
public class SeekBar extends AppCompatSeekBar {
    private static final int[] TINT_ATTRS = {
            android.R.attr.progressDrawable
    };
    private Bitmap mSampleTile;
    private static final ColorFilterLruCache COLOR_FILTER_CACHE = new ColorFilterLruCache(6);

    public SeekBar(Context context) {
        this(context, null);
    }

    public SeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarStyle);
    }

    public SeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (Build.VERSION.SDK_INT < 21) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, TINT_ATTRS, defStyleAttr, 0);
            int resId = typedArray.getResourceId(0, 0);
            if (resId == R.drawable.seekbar_track_material) {
                LayerDrawable ld = (LayerDrawable) ContextCompat.getDrawable(context, resId);
                setPorterDuffColorFilter(ld.findDrawableByLayerId(android.R.id.background),
                        ThemeUtils.getDisabledThemeAttrColor(context, R.attr.colorControlNormal), PorterDuff.Mode.SRC_IN);
                setPorterDuffColorFilter(ld.findDrawableByLayerId(android.R.id.secondaryProgress),
                        ThemeUtils.getThemeAttrColor(context, R.attr.colorControlNormal), PorterDuff.Mode.SRC_IN);
                setPorterDuffColorFilter(ld.findDrawableByLayerId(android.R.id.progress),
                        ThemeUtils.getThemeAttrColor(context, R.attr.colorControlActivated), PorterDuff.Mode.SRC_IN);
                setProgressDrawable(tileify(ld, false));
            }
            typedArray.recycle();
        }
    }


    /**
     * Converts a drawable to a tiled version of itself. It will recursively
     * traverse layer and state list drawables.
     */
    private Drawable tileify(Drawable drawable, boolean clip) {
        if (drawable instanceof DrawableWrapper) {
            Drawable inner = ((DrawableWrapper) drawable).getWrappedDrawable();
            if (inner != null) {
                inner = tileify(inner, clip);
                ((DrawableWrapper) drawable).setWrappedDrawable(inner);
            }
        } else if (drawable instanceof LayerDrawable) {
            LayerDrawable background = (LayerDrawable) drawable;
            final int N = background.getNumberOfLayers();
            Drawable[] outDrawables = new Drawable[N];

            for (int i = 0; i < N; i++) {
                int id = background.getId(i);
                outDrawables[i] = tileify(background.getDrawable(i),
                        (id == android.R.id.progress || id == android.R.id.secondaryProgress));
            }
            LayerDrawable newBg = new LayerDrawable(outDrawables);

            for (int i = 0; i < N; i++) {
                newBg.setId(i, background.getId(i));
            }

            return newBg;

        } else if (drawable instanceof BitmapDrawable) {
            final Bitmap tileBitmap = ((BitmapDrawable) drawable).getBitmap();
            if (mSampleTile == null) {
                mSampleTile = tileBitmap;
            }

            final ShapeDrawable shapeDrawable = new ShapeDrawable(getDrawableShape());
            final BitmapShader bitmapShader = new BitmapShader(tileBitmap,
                    Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
            shapeDrawable.getPaint().setShader(bitmapShader);
            return (clip) ? new ClipDrawable(shapeDrawable, Gravity.LEFT,
                    ClipDrawable.HORIZONTAL) : shapeDrawable;
        }

        return drawable;
    }

    private static PorterDuffColorFilter getPorterDuffColorFilter(int color, PorterDuff.Mode mode) {
        // First, lets see if the cache already contains the color filter
        PorterDuffColorFilter filter = COLOR_FILTER_CACHE.get(color, mode);

        if (filter == null) {
            // Cache miss, so create a color filter and add it to the cache
            filter = new PorterDuffColorFilter(color, mode);
            COLOR_FILTER_CACHE.put(color, mode, filter);
        }

        return filter;
    }

    private static void setPorterDuffColorFilter(Drawable d, int color, PorterDuff.Mode mode) {
        d.setColorFilter(getPorterDuffColorFilter(color, mode == null ? PorterDuff.Mode.SRC_IN : mode));
    }

    private Shape getDrawableShape() {
        final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
        return new RoundRectShape(roundedCorners, null, null);
    }


    private static class ColorFilterLruCache extends LruCache<Integer, PorterDuffColorFilter> {

        public ColorFilterLruCache(int maxSize) {
            super(maxSize);
        }

        PorterDuffColorFilter get(int color, PorterDuff.Mode mode) {
            return get(generateCacheKey(color, mode));
        }

        PorterDuffColorFilter put(int color, PorterDuff.Mode mode, PorterDuffColorFilter filter) {
            return put(generateCacheKey(color, mode), filter);
        }

        private static int generateCacheKey(int color, PorterDuff.Mode mode) {
            int hashCode = 1;
            hashCode = 31 * hashCode + color;
            hashCode = 31 * hashCode + mode.hashCode();
            return hashCode;
        }
    }
}