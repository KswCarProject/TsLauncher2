package com.android.launcher2;

import android.view.KeyEvent;
import android.view.View;

/* compiled from: FocusHelper */
class AppsCustomizeTabKeyEventListener implements View.OnKeyListener {
    AppsCustomizeTabKeyEventListener() {
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return FocusHelper.handleAppsCustomizeTabKeyEvent(v, keyCode, event);
    }
}
