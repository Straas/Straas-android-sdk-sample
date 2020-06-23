package io.straas.android.sdk.streaming.demo.filter.beauty.internal;

import android.content.*;
import android.content.res.*;
import android.graphics.*;

import java.io.*;

public class BitmapUtils {

    public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap bitmap = null;
        AssetManager manager = context.getResources().getAssets();
        try {
            InputStream is = manager.open(fileName);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
