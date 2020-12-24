package com.android.launcher2;

import android.view.View;

/* compiled from: PagedViewCellLayout */
interface Page {
    View getChildOnPageAt(int i);

    int getPageChildCount();

    int indexOfChildOnPage(View view);

    void removeAllViewsOnPage();

    void removeViewOnPageAt(int i);
}
