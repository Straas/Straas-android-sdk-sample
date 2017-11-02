package io.straas.android.sdk.streaming.demo;

import android.content.Context;

import io.straas.android.sdk.demo.R;
import io.straas.android.sdk.streaming.StreamStatsReport;

public class Utils {

    public static String toDisplayText(Context context, StreamStatsReport streamStatsReport) {
        String bitrate = formatBitrate(context, streamStatsReport.getBitrate());
        return  String.format("%s\n%.1f fps", bitrate,
                streamStatsReport.getFps());
    }

    private static String formatBitrate(Context context, int number) {
        float result = number;
        int suffix = R.string.bps;
        if (result > 900) {
            suffix = R.string.kiloBps;
            result = result / 1024;
        }
        if (result > 900) {
            suffix = R.string.megaBps;
            result = result / 1024;
        }
        if (result > 900) {
            suffix = R.string.gigaBps;
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
        return context.getResources().getString(R.string.bitrateSuffix, value,
                context.getString(suffix));
    }
}
