package io.straas.android.sdk.circall.demo;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.google.android.gms.tasks.Task;

//import io.straas.android.circall.licodemanager.LicodeManagerApi;
import io.straas.android.sdk.circall.CircallStatsReport;
import io.straas.android.sdk.circall.CircallToken;

//import static io.straas.android.sdk.circall.demo.BuildConfig.CMS_APP_TOKEN;
//import static io.straas.android.sdk.circall.demo.BuildConfig.LICODE_MANAGER_HOST;
import static io.straas.android.sdk.circall.demo.SingleVideoCallActivity.TARGET_ROOM_NAME;
import static io.straas.android.sdk.circall.demo.RoomTokenFetcher.obtainLicodeToken;
import io.straas.android.sdk.demo.R;

public final class Utils {

    static void requestFullscreenMode(AppCompatActivity activity) {
        // Set window styles for fullscreen-window size. Needs to be done before
        // adding content.
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity.getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_KEEP_SCREEN_ON
                | LayoutParams.FLAG_DISMISS_KEYGUARD | LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | LayoutParams.FLAG_TURN_SCREEN_ON);
        activity.getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
    }

    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }

    static CircallToken getCircallToken(SharedPreferences preferences, Context context) {
        return new CircallToken(preferences.getString(context.getString(R.string.pref_circall_token_key),
                context.getString(R.string.pref_circall_token_default)));
    }

    static String getUsername(SharedPreferences preferences, Context context) {
        return preferences.getString(context.getString(R.string.pref_circall_username_key),
                context.getString(R.string.pref_circall_username_default));
    }

    private static String getRoomServerUrl(SharedPreferences preferences, Context context) {
        return preferences.getString(context.getString(R.string.pref_room_server_url_key),
                context.getString(R.string.pref_room_server_url_default));
    }

    private static String getTokenFetchMethod(SharedPreferences preferences, Context context) {
        return preferences.getString(context.getString(R.string.pref_token_fetch_method_key),
                context.getString(R.string.pref_token_fetch_method_default));
    }

    static Task<String> getTokenFromChoosenMethod(SharedPreferences preferences, Context context) {
 //       if (TextUtils.equals(getTokenFetchMethod(preferences, context),
   //             context.getString(R.string.pref_token_fetch_method_default))) {
            // Method 1: token from licode basic example
            return method1(preferences, context);
//        } else {
            // Method 2: token from circall licode manager
    //        return method2(preferences, context);
     //   }
    }

    private static Task<String> method1(SharedPreferences preferences, Context context) {
        RoomTokenFetcher.RoomConnectionParameters roomConnectionParameters =
                new RoomTokenFetcher.RoomConnectionParameters(getRoomServerUrl(preferences, context),
                        getUsername(preferences, context));

        return obtainLicodeToken(roomConnectionParameters);
    }

//    private static Task<String> method2(SharedPreferences preferences, Context context) {
//        return LicodeManagerApi.INSTANCE.obtainLicodeManagerToken(LICODE_MANAGER_HOST, CMS_APP_TOKEN,
//                TARGET_ROOM_NAME, getUsername(preferences, context));
//    }

    static String toDisplayText(Context context, CircallStatsReport CircallStatsReport) {
        String bitrateString = formatBitrate(context, CircallStatsReport.getBitrate());
        return context.getResources().getString(R.string.stream_stats_format, bitrateString,
                CircallStatsReport.getFps());
    }

    @SuppressLint("DefaultLocale")
    private static String formatBitrate(Context context, int number) {
        float result = number;
        int suffix = R.string.bps;
        if (result > 900) {
            suffix = R.string.kilo_bps;
            result = result / 1024;
        }
        if (result > 900) {
            suffix = R.string.mega_bps;
            result = result / 1024;
        }
        if (result > 900) {
            suffix = R.string.giga_bps;
            result = result / 1024;
        }
        String value;
        if (result < 1) {
            value = String.format("%.2f", result);
        } else if (result < 10) {
            value = String.format("%.1f", result);
        } else if (result < 100) {
            value = String.format("%.0f", result);
        } else {
            value = String.format("%.0f", result);
        }
        return context.getResources().getString(R.string.bitrate_suffix, value,
                context.getString(suffix));
    }
}
