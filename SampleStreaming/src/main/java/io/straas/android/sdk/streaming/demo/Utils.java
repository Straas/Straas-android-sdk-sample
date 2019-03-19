package io.straas.android.sdk.streaming.demo;

import android.content.Context;
import android.os.Build;

import io.straas.android.sdk.streaming.StreamStatsReport;

public class Utils {

    public static String toDisplayText(Context context, StreamStatsReport streamStatsReport) {
        String bitrateString = formatBitrate(context, streamStatsReport.getBitrate());
        return context.getResources().getString(R.string.stream_stats_format, bitrateString,
                streamStatsReport.getFps());
    }

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

    public static boolean isAndroidOreoOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }
}
