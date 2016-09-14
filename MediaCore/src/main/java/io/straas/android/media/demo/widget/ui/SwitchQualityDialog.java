package io.straas.android.media.demo.widget.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.google.android.exoplayer.MediaFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import io.straas.android.sdk.media.StraasMediaCore;
import io.straas.android.sdk.mediacore.demo.R;
import io.straas.android.media.demo.Utils;

import static android.support.v7.appcompat.R.attr.alertDialogStyle;
import static android.support.v7.appcompat.R.styleable.AlertDialog;

public class SwitchQualityDialog extends DialogFragment implements
        DialogInterface.OnClickListener {

    private static final String AUTO_WITH_POSTFIX_FORMAT = "%s [%s]";

    private static final String KEY_STREAM_QUALITY_LIST = "key_stream_quality_list";
    private static final String KEY_SELECT_STREAM_QUALITY = "key_select_stream_quality";

    private int mSelectIndex;
    private ArrayList<MediaFormat> mMediaFormats;
    private MediaFormat[] mIncreasingBandwidthMediaFormats;

    private String mappingVariantString(MediaFormat mediaFormat) {
        String mapping;
        if (mediaFormat.adaptive) {
            mapping = getString(R.string.quality_auto);
        } else if (mediaFormat.trackId.contains("240")) {
            mapping = getString(R.string.quality_low);
        } else if (mediaFormat.trackId.contains("360")) {
            mapping = getString(R.string.quality_medium);
        } else if (mediaFormat.trackId.contains("720")) {
            mapping = getString(R.string.quality_high);
        } else if (mediaFormat.trackId.contains("1080")) {
            mapping = getString(R.string.quality_source);
        } else {
            mapping = mediaFormat.height + "p";
        }
        return mapping;
    }

    private String getAutoItemText(){
        MediaFormat selectedMediaFormat = mIncreasingBandwidthMediaFormats[mSelectIndex];
        if (!selectedMediaFormat.adaptive) {
            return getString(R.string.quality_auto);
        }
        for (MediaFormat mediaFormat : mIncreasingBandwidthMediaFormats) {
            if (mediaFormat.adaptive) {
                continue;
            }
            if (mediaFormat.height == selectedMediaFormat.height &&
                    mediaFormat.width == selectedMediaFormat.width) {
                return String.format(AUTO_WITH_POSTFIX_FORMAT, getString(R.string.quality_auto),
                        mappingVariantString(mediaFormat));
            }
        }
        return "";
    }

    public static SwitchQualityDialog newInstance(ArrayList<MediaFormat> variantList,
                                                  int currentVariantIndex) {
        SwitchQualityDialog dialog = new SwitchQualityDialog();

        Bundle bundle = new Bundle();

        bundle.putParcelableArrayList(KEY_STREAM_QUALITY_LIST, variantList);
        bundle.putInt(KEY_SELECT_STREAM_QUALITY, currentVariantIndex);

        dialog.setArguments(bundle);

        return dialog;
    }

    public static SwitchQualityDialog newInstance(@NonNull Bundle resultData) {
        resultData.setClassLoader(MediaFormat.class.getClassLoader());
        if (resultData.containsKey(StraasMediaCore.KEY_CURRENT_VIDEO_FORMAT_INDEX)) {
            ArrayList<MediaFormat> mediaFormats = resultData.getParcelableArrayList(StraasMediaCore.KEY_ALL_VIDEO_FORMATS);
            int selectedIndex = resultData.getInt(StraasMediaCore.KEY_CURRENT_VIDEO_FORMAT_INDEX);
            return newInstance(mediaFormats, selectedIndex);
        }
        return new SwitchQualityDialog();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParameter();
    }

    public void getParameter() {
        Bundle bundle = getArguments();
        mMediaFormats = bundle.getParcelableArrayList(KEY_STREAM_QUALITY_LIST);
        int selectIndex = bundle.getInt(KEY_SELECT_STREAM_QUALITY);
        if (mMediaFormats != null && mMediaFormats.size() > 0) {
            ArrayList<MediaFormat> mediaFormats = new ArrayList<>();
            MediaFormat selectedMediaFormat = mMediaFormats.get(selectIndex);
            for (MediaFormat mediaFormat : mMediaFormats) {
                if (!TextUtils.equals(mediaFormat.mimeType, selectedMediaFormat.mimeType)) {
                    continue;
                }
                mediaFormats.add(mediaFormat);
            }
            mIncreasingBandwidthMediaFormats = mediaFormats.toArray(new MediaFormat[0]);
            Arrays.sort(mIncreasingBandwidthMediaFormats, new Comparator<MediaFormat>() {
                @Override
                public int compare(MediaFormat lhs, MediaFormat rhs) {
                    return lhs.bitrate - rhs.bitrate;
                }
            });
            for (int i = 0, size = mIncreasingBandwidthMediaFormats.length; i < size; i++) {
                if (mIncreasingBandwidthMediaFormats[i].equals(selectedMediaFormat)) {
                    mSelectIndex = i;
                    break;
                }
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.SwitchQualityDialogTheme);

        builder.setTitle(R.string.quality_select);

        builder.setNegativeButton(R.string.common_cancel, null);

        if (mIncreasingBandwidthMediaFormats != null && mIncreasingBandwidthMediaFormats.length > 0) {
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

        if (getActivity().getSupportMediaController() != null) {
            getActivity().getSupportMediaController().getTransportControls().sendCustomAction(
                    StraasMediaCore.COMMAND_SET_FORMAT_INDEX, Utils.setNewFormatIndex(
                            mMediaFormats.indexOf(mIncreasingBandwidthMediaFormats[i])));
        }

        dialog.dismiss();
    }


    private String[] initOptionList() {
        ArrayList<String> strings = new ArrayList<>();
        String selectedMimeType = mIncreasingBandwidthMediaFormats[mSelectIndex].mimeType;

        for (MediaFormat mediaFormat : mIncreasingBandwidthMediaFormats) {
            String quality;
            if (!TextUtils.equals(mediaFormat.mimeType, selectedMimeType)) {
                continue;
            }
            if (mediaFormat.adaptive) {
                quality = getAutoItemText();
            } else {
                quality = mappingVariantString(mediaFormat);
            }
            strings.add(quality);
        }

        return strings.toArray(new String[0]);
    }
}