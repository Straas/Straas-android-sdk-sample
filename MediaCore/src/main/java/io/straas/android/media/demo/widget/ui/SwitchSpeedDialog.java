package io.straas.android.media.demo.widget.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.Collections;

import io.straas.android.sdk.demo.R;
import io.straas.android.sdk.media.StraasMediaCore;

import static android.support.v7.appcompat.R.attr.alertDialogStyle;

public class SwitchSpeedDialog extends DialogFragment {

    private ArrayList<Float> speedOption = new ArrayList<>();
    private float currentSpeed = 1f;

    public SwitchSpeedDialog setCurrentSpeed(float currentSpeed) {
        this.currentSpeed = currentSpeed;
        return this;
    }

    public SwitchSpeedDialog setSpeedOption(ArrayList<Float> speedOption) {
        this.speedOption = speedOption;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.SwitchDialogTheme);

        builder.setTitle(R.string.speed_select);

        builder.setNegativeButton(R.string.common_cancel, null);

        String[] optionList = initOptionList();

        TypedArray a = builder.getContext().obtainStyledAttributes(null, android.support.v7.appcompat.R.styleable.AlertDialog,
                alertDialogStyle, 0);

        a.recycle();

        builder.setSingleChoiceItems(optionList, speedOption.indexOf(currentSpeed), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onSelectSpeed(dialog, which);
            }
        });

        return builder.create();
    }

    private void onSelectSpeed(DialogInterface dialog, int which) {
        if (speedOption.indexOf(currentSpeed) == which) {
            dialog.dismiss();
            return;
        }

        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());

        if (controller != null) {
            Bundle bundle = new Bundle();
            bundle.putFloat(StraasMediaCore.KEY_PLAYBACK_SPEED, speedOption.get(which));

            controller.getTransportControls().sendCustomAction(StraasMediaCore.COMMAND_SET_PLAYBACK_SPEED, bundle);
        }

        dialog.dismiss();
    }

    private String[] initOptionList() {
        ArrayList<String> stringOptions = new ArrayList<>();

        if (!speedOption.contains(currentSpeed)) {
            speedOption.add(currentSpeed);
        }

        Collections.sort(speedOption);

        for (Float speed : speedOption) {
            stringOptions.add(String.valueOf(speed) + "x");
        }

        return stringOptions.toArray(new String[0]);
    }
}
