package com.android.launcher2;

public interface DragScroller {
    boolean onEnterScrollArea(int i, int i2, int i3);

    boolean onExitScrollArea();

    void scrollLeft();

    void scrollRight();
}
