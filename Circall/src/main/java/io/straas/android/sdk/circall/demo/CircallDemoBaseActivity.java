package io.straas.android.sdk.circall.demo;

import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.support.v7.app.AppCompatActivity;

@BindingMethods({
        @BindingMethod(type = android.widget.ImageView.class,
                attribute = "app:srcCompat",
                method = "setImageDrawable")
})
public abstract class CircallDemoBaseActivity extends AppCompatActivity {
}
