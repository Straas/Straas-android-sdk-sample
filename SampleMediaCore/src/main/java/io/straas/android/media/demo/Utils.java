package io.straas.android.media.demo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.view.View;
import android.view.ViewPropertyAnimator;

import java.util.List;

import io.straas.android.sdk.media.StraasMediaCore;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class Utils {
    private static final int NUMBER_THRESHOLD_THOUSAND = 999;
    private static final int NUMBER_THRESHOLD_MILLION = 999999;
    private static final int NUMBER_DIVIDER_THOUSAND = 1000;
    private static final int NUMBER_DIVIDER_MILLION = 1000000;
    private static final String THOUSAND = "K";
    private static final String MILLION = "M";
    private static final String NUMBER_INT_FORMAT = "%d";

    public static Bundle setNewFormatIndex(int index) {
        Bundle bundle = new Bundle();
        bundle.putInt(StraasMediaCore.KEY_CURRENT_VIDEO_FORMAT_INDEX, index);
        return bundle;
    }

    public static String extractPageToken(@NonNull List<MediaItem> items) {
        return items.get(items.size() - 1)
                .getDescription().getExtras().getString(StraasMediaCore.KEY_PAGE_TOKEN);
    }

    public static void toggleViewVisibilityWithAnimation(long autoHideDelayMs, View... views) {
        for (View view: views) {
            if (view.getVisibility() == VISIBLE) {
                setVisibilityWithAnimation(view, GONE);
            } else {
                showWithDuration(view, autoHideDelayMs);
            }
        }
    }

    public static void toggleViewVisibility(View... views) {
        for (View view: views) {
            if (view.getVisibility() == VISIBLE) {
                setVisibilityWithAnimation(view, GONE);
            } else {
                setVisibilityWithAnimation(view, VISIBLE);
            }
        }
    }

    public static void stopAnimation(View... views) {
        for (View view: views) {
            view.animate().setListener(null).cancel();
        }
    }

    public static void resetAlpha(View... views) {
        for (View view: views) {
            view.setAlpha(1f);
        }
    }

    public static void setVisibilityWithAnimation(View view, int visibility) {
        if (view.getVisibility() == visibility) {
            return;
        }
        view.animate().setListener(null).cancel();
        switch (visibility) {
            case VISIBLE:
                fadeInView(view, android.R.integer.config_longAnimTime, null);
                break;
            case INVISIBLE:
            case GONE:
                fadeOutView(view, android.R.integer.config_longAnimTime, null);
                break;
        }
    }

    public static void showWithDuration(final View view, final long delay) {
        setVisibilityWithAnimation(view, VISIBLE);
        view.animate().setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fadeOutView(view, android.R.integer.config_longAnimTime, null, delay);
            }
        });
    }

    public static void fadeOutView(@NonNull final View view, @IntegerRes int durationRes, Animator.AnimatorListener listener) {
        fadeOutView(view, durationRes, listener, 0);
    }

    public static void fadeOutView(@NonNull final View view, @IntegerRes int durationRes, Animator.AnimatorListener listener, long startDelay) {
        long duration = view.getResources().getInteger(durationRes);
        fadeOutView(view, duration, listener, startDelay);
    }

    public static void fadeOutView(@NonNull final View view, long duration, Animator.AnimatorListener listener, long startDelay) {
        if(view == null) return;
        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        ViewPropertyAnimator animator = view.animate();
        if (startDelay > 0) {
            animator.setStartDelay(startDelay);
        } else if (animator.getStartDelay() > 0) {
            animator.setStartDelay(0);
        }
        animator.alpha(0f)
                .setDuration(duration)
                .setListener((listener != null) ? listener : new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                });
    }

    public static void fadeInView(@NonNull final View view, @IntegerRes int durationRes, Animator.AnimatorListener listener) {
        long duration = view.getResources().getInteger(durationRes);
        fadeInView(view, duration, listener);
    }

    public static void fadeInView(@NonNull final View view, long duration, Animator.AnimatorListener listener) {
        if(view == null) return;
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        view.setAlpha(0f);
        if(view.getVisibility() != View.VISIBLE) view.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        ViewPropertyAnimator animator = view.animate();
        if (animator.getStartDelay() > 0) {
            animator.setStartDelay(0);
        }
        animator.alpha(1f)
                .setDuration(duration)
                .setListener((listener != null) ? listener : null);
    }

    public static Activity getActivity(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    public static String convertLong(long count) {
        String retString;

        if (count > NUMBER_THRESHOLD_MILLION) {
            count /= NUMBER_DIVIDER_MILLION;
            retString = String.format(NUMBER_INT_FORMAT, count) + MILLION;
        } else if (count > NUMBER_THRESHOLD_THOUSAND) {
            count /= NUMBER_DIVIDER_THOUSAND;
            retString = String.format(NUMBER_INT_FORMAT, count) + THOUSAND;
        } else {
            retString = String.format(NUMBER_INT_FORMAT, count);
        }

        return retString;
    }
}
