package com.android.launcher2;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import com.android.launcher2.CellLayout;

public class ShortcutAndWidgetContainer extends ViewGroup {
    static final String TAG = "CellLayoutChildren";
    private int mCellHeight;
    private int mCellWidth;
    private int mHeightGap;
    private final int[] mTmpCellXY = new int[2];
    private final WallpaperManager mWallpaperManager;
    private int mWidthGap;

    public ShortcutAndWidgetContainer(Context context) {
        super(context);
        this.mWallpaperManager = WallpaperManager.getInstance(context);
    }

    public void setCellDimensions(int cellWidth, int cellHeight, int widthGap, int heightGap) {
        this.mCellWidth = cellWidth;
        this.mCellHeight = cellHeight;
        this.mWidthGap = widthGap;
        this.mHeightGap = heightGap;
    }

    public View getChildAt(int x, int y) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
            if (lp.cellX <= x && x < lp.cellX + lp.cellHSpan && lp.cellY <= y && y < lp.cellY + lp.cellVSpan) {
                return child;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            measureChild(getChildAt(i));
        }
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
    }

    public void setupLp(CellLayout.LayoutParams lp) {
        lp.setup(this.mCellWidth, this.mCellHeight, this.mWidthGap, this.mHeightGap);
    }

    public void measureChild(View child) {
        int cellWidth = this.mCellWidth;
        int cellHeight = this.mCellHeight;
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
        lp.setup(cellWidth, cellHeight, this.mWidthGap, this.mHeightGap);
        child.measure(View.MeasureSpec.makeMeasureSpec(lp.width, 1073741824), View.MeasureSpec.makeMeasureSpec(lp.height, 1073741824));
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
                int childLeft = lp.x;
                int childTop = lp.y;
                child.layout(childLeft, childTop, lp.width + childLeft, lp.height + childTop);
                if (lp.dropped) {
                    lp.dropped = false;
                    int[] cellXY = this.mTmpCellXY;
                    getLocationOnScreen(cellXY);
                    this.mWallpaperManager.sendWallpaperCommand(getWindowToken(), "android.home.drop", cellXY[0] + childLeft + (lp.width / 2), cellXY[1] + childTop + (lp.height / 2), 0, (Bundle) null);
                }
            }
        }
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        if (child != null) {
            Rect r = new Rect();
            child.getDrawingRect(r);
            requestRectangleOnScreen(r);
        }
    }

    public void cancelLongPress() {
        super.cancelLongPress();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).cancelLongPress();
        }
    }

    /* access modifiers changed from: protected */
    public void setChildrenDrawingCacheEnabled(boolean enabled) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            view.setDrawingCacheEnabled(enabled);
            if (!view.isHardwareAccelerated() && enabled) {
                view.buildDrawingCache(true);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setChildrenDrawnWithCacheEnabled(boolean enabled) {
        super.setChildrenDrawnWithCacheEnabled(enabled);
    }
}
