package com.android.launcher2;

import android.app.Activity;
import android.os.Bundle;
import com.android.launcher.R;

public class WallpaperChooser extends Activity {
    private static final String TAG = "Launcher.WallpaperChooser";

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.wallpaper_chooser_base);
        if (getFragmentManager().findFragmentById(R.id.wallpaper_chooser_fragment) == null) {
            WallpaperChooserDialogFragment.newInstance().show(getFragmentManager(), "dialog");
        }
    }
}
