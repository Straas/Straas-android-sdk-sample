package io.straas.android.media.demo.widget.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.google.android.exoplayer2.Format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.straas.android.media.demo.R;
import io.straas.android.media.demo.Utils;
import io.straas.android.sdk.media.StraasMediaCore;

import static android.support.v7.appcompat.R.attr.alertDialogStyle;
import static android.support.v7.appcompat.R.styleable.AlertDialog;

public class SwitchQualityDialog extends DialogFragment implements
        DialogInterface.OnClickListener {

    private static final String AUTO_WITH_POSTFIX_FORMAT = "%s [%s]";

    private static final String KEY_STREAM_QUALITY_LIST = "key_stream_quality_list";
    private static final String KEY_SELECT_STREAM_QUALITY = "key_select_stream_quality";

    private static final Pattern REGEX_VIDEO_DASH = Pattern.compile("^video-(\\d+p)$");

    private int mSelectIndex;
    private ArrayList<Format> mFormats;
    private Format[] mIncreasingBandwidthFormats;

    private String mappingVariantString(Format format) {
        String mapping;
        Matcher videoDashMatcher = REGEX_VIDEO_DASH.matcher(format.id);
        if (isFormatAdaptive(format)) {
            mapping = getString(R.string.quality_auto);
        } else if (format.id.equals("video-source") || format.id.equals("video-1080p")) {
            mapping = getString(R.string.quality_source);
        } else if (format.id.equals("video-1080p-transcoded")) {
            mapping = "1080p";
        } else if (videoDashMatcher.find()) {
            mapping = videoDashMatcher.group(1);
        } else {
            mapping = format.height == Format.NO_VALUE ? "" : format.height + "p";
        }
        return mapping;
    }

    private boolean isFormatAdaptive(Format format) {
        return format.id == null && format.bitrate == Format.NO_VALUE;
    }

    private String getAutoItemText(){
        Format selectedFormat = mIncreasingBandwidthFormats[mSelectIndex];
        if (!isFormatAdaptive(selectedFormat)) {
            return getString(R.string.quality_auto);
        }
        for (Format format : mIncreasingBandwidthFormats) {
            if (isFormatAdaptive(format)) {
                continue;
            }
            if (format.height == selectedFormat.height &&
                    format.width == selectedFormat.width) {
                String quality = mappingVariantString(format);
                if (TextUtils.isEmpty(quality)) {
                    continue;
                }
                return String.format(AUTO_WITH_POSTFIX_FORMAT, getString(R.string.quality_auto),
                        quality);
            }
        }
        return getString(R.string.quality_auto);
    }

    public static SwitchQualityDialog newInstance(ArrayList<Format> variantList,
                                                  int currentVariantIndex) {
        SwitchQualityDialog dialog = new SwitchQualityDialog();

        Bundle bundle = new Bundle();

        bundle.putParcelableArrayList(KEY_STREAM_QUALITY_LIST, variantList);
        bundle.putInt(KEY_SELECT_STREAM_QUALITY, currentVariantIndex);

        dialog.setArguments(bundle);

        return dialog;
    }

    public static SwitchQualityDialog newInstance(@NonNull Bundle resultData) {
        resultData.setClassLoader(Format.class.getClassLoader());
        if (resultData.containsKey(StraasMediaCore.KEY_CURRENT_VIDEO_FORMAT_INDEX)) {
            ArrayList<Format> Formats = resultData.getParcelableArrayList(StraasMediaCore.KEY_ALL_VIDEO_FORMATS);
            int selectedIndex = resultData.getInt(StraasMediaCore.KEY_CURRENT_VIDEO_FORMAT_INDEX);
            return newInstance(Formats, selectedIndex);
        }
        return new SwitchQualityDialog();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParameter();
    }

    public void getParameter() {
        Bundle bundle = getArguments();
        mFormats = bundle.getParcelableArrayList(KEY_STREAM_QUALITY_LIST);
        int selectIndex = bundle.getInt(KEY_SELECT_STREAM_QUALITY);
        if (mFormats != null && mFormats.size() > 0) {
            ArrayList<Format> formats = new ArrayList<>();
            Format selectedFormat = mFormats.get(selectIndex);
            for (Format format : mFormats) {
                if (!TextUtils.equals(format.sampleMimeType, selectedFormat.sampleMimeType)) {
                    continue;
                }
                formats.add(format);
            }
            mIncreasingBandwidthFormats = formats.toArray(new Format[0]);
            Arrays.sort(mIncreasingBandwidthFormats, new Comparator<Format>() {
                @Override
                public int compare(Format lhs, Format rhs) {
                    return lhs.bitrate - rhs.bitrate;
                }
            });
            for (int i = 0, size = mIncreasingBandwidthFormats.length; i < size; i++) {
                if (mIncreasingBandwidthFormats[i].equals(selectedFormat)) {
                    mSelectIndex = i;
                    break;
                }
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.SwitchDialogTheme);

        builder.setTitle(R.string.quality_select);

        builder.setNegativeButton(R.string.common_cancel, null);

        if (mIncreasingBandwidthFormats != null && mIncreasingBandwidthFormats.length > 0) {
            String[] optionList = initOptionList();

            TypedArray a = builder.getContext().obtainStyledAttributes(null, AlertDialog,
                    alertDialogStyle, 0);

            a.recycle();

            builder.setSingleChoiceItems(optionList, mSelectIndex, this);

        }

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int i) {

        if (mSelectIndex == i) {
            dialog.dismiss();
            return;
        }
        mSelectIndex = i;

        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());

        if (controller != null) {
            controller.getTransportControls().sendCustomAction(
                    StraasMediaCore.COMMAND_SET_FORMAT_INDEX, Utils.setNewFormatIndex(
                            mFormats.indexOf(mIncreasingBandwidthFormats[i])));
        }

        dialog.dismiss();
    }


    private String[] initOptionList() {
        ArrayList<String> strings = new ArrayList<>();
        String selectedMimeType = mIncreasingBandwidthFormats[mSelectIndex].sampleMimeType;

        for (Format format : mIncreasingBandwidthFormats) {
            String quality;
            if (!TextUtils.equals(format.sampleMimeType, selectedMimeType)) {
                continue;
            }
            if (isFormatAdaptive(format)) {
                quality = getAutoItemText();
            } else {
                quality = mappingVariantString(format);
            }
            if (!TextUtils.isEmpty(quality)) {
                strings.add(quality);
            }
        }

        return strings.toArray(new String[0]);
    }
}
