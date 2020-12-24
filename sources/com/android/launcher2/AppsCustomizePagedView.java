package com.android.launcher2;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.ExploreByTouchHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.launcher.R;
import com.android.launcher2.AsyncTaskPageData;
import com.android.launcher2.DragLayer;
import com.android.launcher2.DropTarget;
import com.android.launcher2.LauncherModel;
import com.android.launcher2.PagedViewCellLayout;
import com.android.launcher2.PagedViewIcon;
import com.android.launcher2.PagedViewWidget;
import com.android.launcher2.Workspace;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AppsCustomizePagedView extends PagedViewWithDraggableItems implements View.OnClickListener, View.OnKeyListener, DragSource, PagedViewIcon.PressedCallback, PagedViewWidget.ShortPressListener, LauncherTransitionable {
    private static float CAMERA_DISTANCE = 6500.0f;
    private static final boolean PERFORM_OVERSCROLL_ROTATION = true;
    static final String TAG = "AppsCustomizePagedView";
    private static float TRANSITION_MAX_ROTATION = 22.0f;
    private static float TRANSITION_PIVOT = 0.65f;
    private static float TRANSITION_SCALE_FACTOR = 0.74f;
    static final int WIDGET_BOUND = 1;
    static final int WIDGET_INFLATED = 2;
    static final int WIDGET_NO_CLEANUP_REQUIRED = -1;
    static final int WIDGET_PRELOAD_PENDING = 0;
    static final int sLookAheadPageCount = 2;
    static final int sLookBehindPageCount = 2;
    private static final int sPageSleepDelay = 200;
    private AccelerateInterpolator mAlphaInterpolator = new AccelerateInterpolator(0.9f);
    private int mAppIconSize;
    private ArrayList<ApplicationInfo> mApps;
    private Runnable mBindWidgetRunnable = null;
    CanvasCache mCachedAppWidgetPreviewCanvas = new CanvasCache();
    RectCache mCachedAppWidgetPreviewDestRect = new RectCache();
    PaintCache mCachedAppWidgetPreviewPaint = new PaintCache();
    RectCache mCachedAppWidgetPreviewSrcRect = new RectCache();
    BitmapCache mCachedShortcutPreviewBitmap = new BitmapCache();
    CanvasCache mCachedShortcutPreviewCanvas = new CanvasCache();
    PaintCache mCachedShortcutPreviewPaint = new PaintCache();
    private Canvas mCanvas;
    private int mClingFocusedX;
    private int mClingFocusedY;
    private int mContentWidth;
    PendingAddWidgetInfo mCreateWidgetInfo = null;
    /* access modifiers changed from: private */
    public ArrayList<Runnable> mDeferredPrepareLoadWidgetPreviewsTasks = new ArrayList<>();
    private ArrayList<AsyncTaskPageData> mDeferredSyncWidgetPageItems = new ArrayList<>();
    private DragController mDragController;
    private boolean mDraggingWidget = false;
    private boolean mHasShownAllAppsCling;
    private IconCache mIconCache;
    /* access modifiers changed from: private */
    public boolean mInTransition;
    private Runnable mInflateWidgetRunnable = null;
    /* access modifiers changed from: private */
    public Launcher mLauncher;
    private final LayoutInflater mLayoutInflater;
    private DecelerateInterpolator mLeftScreenAlphaInterpolator = new DecelerateInterpolator(4.0f);
    private int mMaxAppCellCountX;
    private int mMaxAppCellCountY;
    private int mNumAppsPages = 0;
    private int mNumWidgetPages = 0;
    private final PackageManager mPackageManager;
    private PagedViewIcon mPressedIcon;
    ArrayList<AppsCustomizeAsyncTask> mRunningTasks;
    private int mSaveInstanceStateItemIndex = -1;
    private Rect mTmpRect = new Rect();
    int mWidgetCleanupState = -1;
    /* access modifiers changed from: private */
    public int mWidgetCountX;
    private int mWidgetCountY;
    private int mWidgetHeightGap;
    private Toast mWidgetInstructionToast;
    int mWidgetLoadingId = -1;
    private PagedViewCellLayout mWidgetSpacingLayout;
    private int mWidgetWidthGap;
    private ArrayList<Object> mWidgets;
    Workspace.ZInterpolator mZInterpolator = new Workspace.ZInterpolator(0.5f);
    private final float sWidgetPreviewIconPaddingPercentage = 0.25f;

    public enum ContentType {
        Applications,
        Widgets
    }

    @SuppressLint({"NewApi"})
    public AppsCustomizePagedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mPackageManager = context.getPackageManager();
        this.mApps = new ArrayList<>();
        this.mWidgets = new ArrayList<>();
        this.mIconCache = ((LauncherApplication) context.getApplicationContext()).getIconCache();
        this.mCanvas = new Canvas();
        this.mRunningTasks = new ArrayList<>();
        this.mAppIconSize = context.getResources().getDimensionPixelSize(R.dimen.app_icon_size);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AppsCustomizePagedView, 0, 0);
        this.mMaxAppCellCountX = a.getInt(0, -1);
        this.mMaxAppCellCountY = a.getInt(1, -1);
        this.mWidgetWidthGap = a.getDimensionPixelSize(2, 0);
        this.mWidgetHeightGap = a.getDimensionPixelSize(3, 0);
        this.mWidgetCountX = a.getInt(4, 2);
        this.mWidgetCountY = a.getInt(5, 2);
        this.mClingFocusedX = a.getInt(6, 0);
        this.mClingFocusedY = a.getInt(7, 0);
        a.recycle();
        this.mWidgetSpacingLayout = new PagedViewCellLayout(getContext());
        this.mFadeInAdjacentScreens = false;
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
    }

    /* access modifiers changed from: protected */
    public void init() {
        super.init();
        this.mCenterPagesVertically = false;
        setDragSlopeThreshold(((float) getContext().getResources().getInteger(R.integer.config_appsCustomizeDragSlopeThreshold)) / 100.0f);
    }

    private int getMiddleComponentIndexOnCurrentPage() {
        if (getPageCount() <= 0) {
            return -1;
        }
        int currentPage = getCurrentPage();
        if (currentPage < this.mNumAppsPages) {
            PagedViewCellLayoutChildren childrenLayout = ((PagedViewCellLayout) getPageAt(currentPage)).getChildrenLayout();
            int numItemsPerPage = this.mCellCountX * this.mCellCountY;
            int childCount = childrenLayout.getChildCount();
            if (childCount > 0) {
                return (currentPage * numItemsPerPage) + (childCount / 2);
            }
            return -1;
        }
        int numApps = this.mApps.size();
        int numItemsPerPage2 = this.mWidgetCountX * this.mWidgetCountY;
        int childCount2 = ((PagedViewGridLayout) getPageAt(currentPage)).getChildCount();
        if (childCount2 > 0) {
            return ((currentPage - this.mNumAppsPages) * numItemsPerPage2) + numApps + (childCount2 / 2);
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public int getSaveInstanceStateIndex() {
        if (this.mSaveInstanceStateItemIndex == -1) {
            this.mSaveInstanceStateItemIndex = getMiddleComponentIndexOnCurrentPage();
        }
        return this.mSaveInstanceStateItemIndex;
    }

    /* access modifiers changed from: package-private */
    public int getPageForComponent(int index) {
        if (index < 0) {
            return 0;
        }
        if (index < this.mApps.size()) {
            return index / (this.mCellCountX * this.mCellCountY);
        }
        return this.mNumAppsPages + ((index - this.mApps.size()) / (this.mWidgetCountX * this.mWidgetCountY));
    }

    /* access modifiers changed from: package-private */
    public void restorePageForIndex(int index) {
        if (index >= 0) {
            this.mSaveInstanceStateItemIndex = index;
        }
    }

    private void updatePageCounts() {
        int nAllAppPages = getResources().getInteger(R.integer.allAppPages);
        if (nAllAppPages != 1) {
            this.mNumAppsPages = (int) Math.ceil((double) (((float) this.mApps.size()) / ((float) (this.mCellCountX * this.mCellCountY))));
        }
        if (nAllAppPages != 2) {
            this.mNumWidgetPages = (int) Math.ceil((double) (((float) this.mWidgets.size()) / ((float) (this.mWidgetCountX * this.mWidgetCountY))));
        }
    }

    /* access modifiers changed from: protected */
    public void onDataReady(int width, int height) {
        boolean isLandscape = getResources().getConfiguration().orientation == 2;
        int maxCellCountX = Integer.MAX_VALUE;
        int maxCellCountY = Integer.MAX_VALUE;
        if (LauncherApplication.isScreenLarge()) {
            if (isLandscape) {
                maxCellCountX = LauncherModel.getCellCountX();
            } else {
                maxCellCountX = LauncherModel.getCellCountY();
            }
            if (isLandscape) {
                maxCellCountY = LauncherModel.getCellCountY();
            } else {
                maxCellCountY = LauncherModel.getCellCountX();
            }
        }
        if (this.mMaxAppCellCountX > -1) {
            maxCellCountX = Math.min(maxCellCountX, this.mMaxAppCellCountX);
        }
        int maxWidgetCellCountY = maxCellCountY;
        if (this.mMaxAppCellCountY > -1) {
            maxWidgetCellCountY = Math.min(maxWidgetCellCountY, this.mMaxAppCellCountY);
        }
        this.mWidgetSpacingLayout.setGap(this.mPageLayoutWidthGap, this.mPageLayoutHeightGap);
        this.mWidgetSpacingLayout.setPadding(this.mPageLayoutPaddingLeft, this.mPageLayoutPaddingTop, this.mPageLayoutPaddingRight, this.mPageLayoutPaddingBottom);
        this.mWidgetSpacingLayout.calculateCellCount(width, height, maxCellCountX, maxCellCountY);
        this.mCellCountX = this.mWidgetSpacingLayout.getCellCountX();
        this.mCellCountY = this.mWidgetSpacingLayout.getCellCountY();
        updatePageCounts();
        int widthSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), ExploreByTouchHelper.INVALID_ID);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), ExploreByTouchHelper.INVALID_ID);
        this.mWidgetSpacingLayout.calculateCellCount(width, height, maxCellCountX, maxWidgetCellCountY);
        this.mWidgetSpacingLayout.measure(widthSpec, heightSpec);
        this.mContentWidth = this.mWidgetSpacingLayout.getContentWidth();
        boolean hostIsTransitioning = getTabHost().isTransitioning();
        invalidatePageData(Math.max(0, getPageForComponent(this.mSaveInstanceStateItemIndex)), hostIsTransitioning);
        if (!hostIsTransitioning) {
            post(new Runnable() {
                public void run() {
                    AppsCustomizePagedView.this.showAllAppsCling();
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void showAllAppsCling() {
        if (!this.mHasShownAllAppsCling && isDataReady()) {
            this.mHasShownAllAppsCling = true;
            int[] offset = new int[2];
            int[] pos = this.mWidgetSpacingLayout.estimateCellPosition(this.mClingFocusedX, this.mClingFocusedY);
            this.mLauncher.getDragLayer().getLocationInDragLayer(this, offset);
            pos[0] = pos[0] + ((getMeasuredWidth() - this.mWidgetSpacingLayout.getMeasuredWidth()) / 2) + offset[0];
            pos[1] = pos[1] + (offset[1] - this.mLauncher.getDragLayer().getPaddingTop());
            this.mLauncher.showFirstRunAllAppsCling(pos);
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        if (!isDataReady() && !this.mApps.isEmpty() && !this.mWidgets.isEmpty()) {
            setDataIsReady();
            setMeasuredDimension(width, height);
            onDataReady(width, height);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void onPackagesUpdated() {
        this.mWidgets.clear();
        List<AppWidgetProviderInfo> widgets = AppWidgetManager.getInstance(this.mLauncher).getInstalledProviders();
        List<ResolveInfo> shortcuts = this.mPackageManager.queryIntentActivities(new Intent("android.intent.action.CREATE_SHORTCUT"), 0);
        for (AppWidgetProviderInfo widget : widgets) {
            if (widget.minWidth <= 0 || widget.minHeight <= 0) {
                Log.e(TAG, "Widget " + widget.provider + " has invalid dimensions (" + widget.minWidth + ", " + widget.minHeight + ")");
            } else {
                int[] spanXY = Launcher.getSpanForWidget((Context) this.mLauncher, widget);
                int[] minSpanXY = Launcher.getMinSpanForWidget((Context) this.mLauncher, widget);
                int minSpanX = Math.min(spanXY[0], minSpanXY[0]);
                int minSpanY = Math.min(spanXY[1], minSpanXY[1]);
                if (minSpanX > LauncherModel.getCellCountX() || minSpanY > LauncherModel.getCellCountY()) {
                    Log.e(TAG, "Widget " + widget.provider + " can not fit on this device (" + widget.minWidth + ", " + widget.minHeight + ")");
                } else {
                    this.mWidgets.add(widget);
                }
            }
        }
        this.mWidgets.addAll(shortcuts);
        Collections.sort(this.mWidgets, new LauncherModel.WidgetAndShortcutNameComparator(this.mPackageManager));
        updatePageCounts();
        invalidateOnDataChange();
    }

    public void onClick(View v) {
        if (this.mLauncher.isAllAppsVisible() && !this.mLauncher.getWorkspace().isSwitchingState()) {
            if (v instanceof PagedViewIcon) {
                ApplicationInfo appInfo = (ApplicationInfo) v.getTag();
                if (this.mPressedIcon != null) {
                    this.mPressedIcon.lockDrawableState();
                }
                this.mLauncher.updateWallpaperVisibility(true);
                this.mLauncher.startActivitySafely(v, appInfo.intent, appInfo);
            } else if (v instanceof PagedViewWidget) {
                if (this.mWidgetInstructionToast != null) {
                    this.mWidgetInstructionToast.cancel();
                }
                this.mWidgetInstructionToast = Toast.makeText(getContext(), R.string.long_press_widget_to_add, 0);
                this.mWidgetInstructionToast.show();
                ImageView p = (ImageView) v.findViewById(R.id.widget_preview);
                AnimatorSet bounce = LauncherAnimUtils.createAnimatorSet();
                ValueAnimator tyuAnim = LauncherAnimUtils.ofFloat(p, "translationY", (float) getResources().getDimensionPixelSize(R.dimen.dragViewOffsetY));
                tyuAnim.setDuration(125);
                ValueAnimator tydAnim = LauncherAnimUtils.ofFloat(p, "translationY", 0.0f);
                tydAnim.setDuration(100);
                bounce.play(tyuAnim).before(tydAnim);
                bounce.setInterpolator(new AccelerateInterpolator());
                bounce.start();
            }
        }
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return FocusHelper.handleAppsCustomizeKeyEvent(v, keyCode, event);
    }

    /* access modifiers changed from: protected */
    public void determineDraggingStart(MotionEvent ev) {
    }

    private void beginDraggingApplication(View v) {
        this.mLauncher.getWorkspace().onDragStartedWithItem(v);
        this.mLauncher.getWorkspace().beginDragShared(v, this);
    }

    /* access modifiers changed from: package-private */
    public Bundle getDefaultOptionsForWidget(Launcher launcher, PendingAddWidgetInfo info) {
        if (Build.VERSION.SDK_INT < 17) {
            return null;
        }
        AppWidgetResizeFrame.getWidgetSizeRanges(this.mLauncher, info.spanX, info.spanY, this.mTmpRect);
        Rect padding = AppWidgetHostView.getDefaultPaddingForWidget(this.mLauncher, info.componentName, (Rect) null);
        float density = getResources().getDisplayMetrics().density;
        int xPaddingDips = (int) (((float) (padding.left + padding.right)) / density);
        int yPaddingDips = (int) (((float) (padding.top + padding.bottom)) / density);
        Bundle options = new Bundle();
        options.putInt("appWidgetMinWidth", this.mTmpRect.left - xPaddingDips);
        options.putInt("appWidgetMinHeight", this.mTmpRect.top - yPaddingDips);
        options.putInt("appWidgetMaxWidth", this.mTmpRect.right - xPaddingDips);
        options.putInt("appWidgetMaxHeight", this.mTmpRect.bottom - yPaddingDips);
        return options;
    }

    private void preloadWidget(final PendingAddWidgetInfo info) {
        final AppWidgetProviderInfo pInfo = info.info;
        final Bundle options = getDefaultOptionsForWidget(this.mLauncher, info);
        if (pInfo.configure != null) {
            info.bindOptions = options;
            return;
        }
        this.mWidgetCleanupState = 0;
        this.mBindWidgetRunnable = new Runnable() {
            @SuppressLint({"NewApi"})
            public void run() {
                AppsCustomizePagedView.this.mWidgetLoadingId = AppsCustomizePagedView.this.mLauncher.getAppWidgetHost().allocateAppWidgetId();
                if (options == null) {
                    if (AppWidgetManager.getInstance(AppsCustomizePagedView.this.mLauncher).bindAppWidgetIdIfAllowed(AppsCustomizePagedView.this.mWidgetLoadingId, info.componentName)) {
                        AppsCustomizePagedView.this.mWidgetCleanupState = 1;
                    }
                } else if (AppWidgetManager.getInstance(AppsCustomizePagedView.this.mLauncher).bindAppWidgetIdIfAllowed(AppsCustomizePagedView.this.mWidgetLoadingId, info.componentName, options)) {
                    AppsCustomizePagedView.this.mWidgetCleanupState = 1;
                }
            }
        };
        post(this.mBindWidgetRunnable);
        this.mInflateWidgetRunnable = new Runnable() {
            public void run() {
                if (AppsCustomizePagedView.this.mWidgetCleanupState == 1) {
                    AppWidgetHostView hostView = AppsCustomizePagedView.this.mLauncher.getAppWidgetHost().createView(AppsCustomizePagedView.this.getContext(), AppsCustomizePagedView.this.mWidgetLoadingId, pInfo);
                    info.boundWidget = hostView;
                    AppsCustomizePagedView.this.mWidgetCleanupState = 2;
                    hostView.setVisibility(4);
                    int[] unScaledSize = AppsCustomizePagedView.this.mLauncher.getWorkspace().estimateItemSize(info.spanX, info.spanY, info, false);
                    DragLayer.LayoutParams lp = new DragLayer.LayoutParams(unScaledSize[0], unScaledSize[1]);
                    lp.y = 0;
                    lp.x = 0;
                    lp.customPosition = true;
                    hostView.setLayoutParams(lp);
                    AppsCustomizePagedView.this.mLauncher.getDragLayer().addView(hostView);
                }
            }
        };
        post(this.mInflateWidgetRunnable);
    }

    public void onShortPress(View v) {
        if (this.mCreateWidgetInfo != null) {
            cleanupWidgetPreloading(false);
        }
        this.mCreateWidgetInfo = new PendingAddWidgetInfo((PendingAddWidgetInfo) v.getTag());
        preloadWidget(this.mCreateWidgetInfo);
    }

    private void cleanupWidgetPreloading(boolean widgetWasAdded) {
        if (!widgetWasAdded) {
            PendingAddWidgetInfo info = this.mCreateWidgetInfo;
            this.mCreateWidgetInfo = null;
            if (this.mWidgetCleanupState == 0) {
                removeCallbacks(this.mBindWidgetRunnable);
                removeCallbacks(this.mInflateWidgetRunnable);
            } else if (this.mWidgetCleanupState == 1) {
                if (this.mWidgetLoadingId != -1) {
                    this.mLauncher.getAppWidgetHost().deleteAppWidgetId(this.mWidgetLoadingId);
                }
                removeCallbacks(this.mInflateWidgetRunnable);
            } else if (this.mWidgetCleanupState == 2) {
                if (this.mWidgetLoadingId != -1) {
                    this.mLauncher.getAppWidgetHost().deleteAppWidgetId(this.mWidgetLoadingId);
                }
                this.mLauncher.getDragLayer().removeView(info.boundWidget);
            }
        }
        this.mWidgetCleanupState = -1;
        this.mWidgetLoadingId = -1;
        this.mCreateWidgetInfo = null;
        PagedViewWidget.resetShortPressTarget();
    }

    public void cleanUpShortPress(View v) {
        if (!this.mDraggingWidget) {
            cleanupWidgetPreloading(false);
        }
    }

    private boolean beginDraggingWidget(View v) {
        Bitmap preview;
        this.mDraggingWidget = true;
        ImageView image = (ImageView) v.findViewById(R.id.widget_preview);
        PendingAddItemInfo createItemInfo = (PendingAddItemInfo) v.getTag();
        if (image.getDrawable() == null) {
            this.mDraggingWidget = false;
            return false;
        }
        float scale = 1.0f;
        if (!(createItemInfo instanceof PendingAddWidgetInfo)) {
            Drawable icon = this.mIconCache.getFullResIcon(((PendingAddShortcutInfo) v.getTag()).shortcutActivityInfo);
            preview = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            this.mCanvas.setBitmap(preview);
            this.mCanvas.save();
            renderDrawableToBitmap(icon, preview, 0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            this.mCanvas.restore();
            this.mCanvas.setBitmap((Bitmap) null);
            createItemInfo.spanY = 1;
            createItemInfo.spanX = 1;
        } else if (this.mCreateWidgetInfo == null) {
            return false;
        } else {
            PendingAddWidgetInfo createWidgetInfo = this.mCreateWidgetInfo;
            createItemInfo = createWidgetInfo;
            int spanX = createItemInfo.spanX;
            int spanY = createItemInfo.spanY;
            int[] size = this.mLauncher.getWorkspace().estimateItemSize(spanX, spanY, createWidgetInfo, true);
            FastBitmapDrawable previewDrawable = (FastBitmapDrawable) image.getDrawable();
            preview = getWidgetPreview(createWidgetInfo.componentName, createWidgetInfo.previewImage, createWidgetInfo.icon, spanX, spanY, Math.min((int) (((float) previewDrawable.getIntrinsicWidth()) * 1.25f), size[0]), Math.min((int) (((float) previewDrawable.getIntrinsicHeight()) * 1.25f), size[1]));
            float[] mv = new float[9];
            Matrix m = new Matrix();
            m.setRectToRect(new RectF(0.0f, 0.0f, (float) preview.getWidth(), (float) preview.getHeight()), new RectF(0.0f, 0.0f, (float) previewDrawable.getIntrinsicWidth(), (float) previewDrawable.getIntrinsicHeight()), Matrix.ScaleToFit.START);
            m.getValues(mv);
            scale = mv[0];
        }
        boolean clipAlpha = !(createItemInfo instanceof PendingAddWidgetInfo) || ((PendingAddWidgetInfo) createItemInfo).previewImage != 0;
        Bitmap outline = Bitmap.createScaledBitmap(preview, preview.getWidth(), preview.getHeight(), false);
        this.mLauncher.lockScreenOrientation();
        this.mLauncher.getWorkspace().onDragStartedWithItem(createItemInfo, outline, clipAlpha);
        this.mDragController.startDrag(image, preview, this, createItemInfo, DragController.DRAG_ACTION_COPY, (Rect) null, scale);
        outline.recycle();
        preview.recycle();
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean beginDragging(View v) {
        if (!super.beginDragging(v)) {
            return false;
        }
        if (v instanceof PagedViewIcon) {
            beginDraggingApplication(v);
        } else if ((v instanceof PagedViewWidget) && !beginDraggingWidget(v)) {
            return false;
        }
        postDelayed(new Runnable() {
            public void run() {
                if (AppsCustomizePagedView.this.mLauncher.getDragController().isDragging()) {
                    AppsCustomizePagedView.this.mLauncher.dismissAllAppsCling((View) null);
                    AppsCustomizePagedView.this.resetDrawableState();
                    AppsCustomizePagedView.this.mLauncher.enterSpringLoadedDragMode();
                }
            }
        }, 150);
        return true;
    }

    private void endDragging(View target, boolean isFlingToDelete, boolean success) {
        if (isFlingToDelete || !success || (target != this.mLauncher.getWorkspace() && !(target instanceof DeleteDropTarget))) {
            this.mLauncher.exitSpringLoadedDragMode();
        }
        this.mLauncher.unlockScreenOrientation(false);
    }

    public View getContent() {
        return null;
    }

    public void onLauncherTransitionPrepare(Launcher l, boolean animated, boolean toWorkspace) {
        this.mInTransition = true;
        if (toWorkspace) {
            cancelAllTasks();
        }
    }

    public void onLauncherTransitionStart(Launcher l, boolean animated, boolean toWorkspace) {
    }

    public void onLauncherTransitionStep(Launcher l, float t) {
    }

    public void onLauncherTransitionEnd(Launcher l, boolean animated, boolean toWorkspace) {
        boolean z = false;
        this.mInTransition = false;
        Iterator<AsyncTaskPageData> it = this.mDeferredSyncWidgetPageItems.iterator();
        while (it.hasNext()) {
            onSyncWidgetPageItems(it.next());
        }
        this.mDeferredSyncWidgetPageItems.clear();
        Iterator<Runnable> it2 = this.mDeferredPrepareLoadWidgetPreviewsTasks.iterator();
        while (it2.hasNext()) {
            it2.next().run();
        }
        this.mDeferredPrepareLoadWidgetPreviewsTasks.clear();
        if (!toWorkspace) {
            z = true;
        }
        this.mForceDrawAllChildrenNextFrame = z;
    }

    public void onDropCompleted(View target, DropTarget.DragObject d, boolean isFlingToDelete, boolean success) {
        if (!isFlingToDelete) {
            endDragging(target, false, success);
            if (!success) {
                boolean showOutOfSpaceMessage = false;
                if (target instanceof Workspace) {
                    CellLayout layout = (CellLayout) ((Workspace) target).getChildAt(this.mLauncher.getCurrentWorkspaceScreen());
                    ItemInfo itemInfo = (ItemInfo) d.dragInfo;
                    if (layout != null) {
                        layout.calculateSpans(itemInfo);
                        showOutOfSpaceMessage = !layout.findCellForSpan((int[]) null, itemInfo.spanX, itemInfo.spanY);
                    }
                }
                if (showOutOfSpaceMessage) {
                    this.mLauncher.showOutOfSpaceMessage(false);
                }
                d.deferDragViewCleanupPostAnimation = false;
            }
            cleanupWidgetPreloading(success);
            this.mDraggingWidget = false;
        }
    }

    public void onFlingToDeleteCompleted() {
        endDragging((View) null, true, true);
        cleanupWidgetPreloading(false);
        this.mDraggingWidget = false;
    }

    public boolean supportsFlingToDelete() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelAllTasks();
    }

    public void clearAllWidgetPages() {
        cancelAllTasks();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View v = getPageAt(i);
            if (v instanceof PagedViewGridLayout) {
                ((PagedViewGridLayout) v).removeAllViewsOnPage();
                this.mDirtyPageContent.set(i, true);
            }
        }
    }

    private void cancelAllTasks() {
        Iterator<AppsCustomizeAsyncTask> iter = this.mRunningTasks.iterator();
        while (iter.hasNext()) {
            AppsCustomizeAsyncTask task = iter.next();
            task.cancel(false);
            iter.remove();
            this.mDirtyPageContent.set(task.page, true);
            View v = getPageAt(task.page);
            if (v instanceof PagedViewGridLayout) {
                ((PagedViewGridLayout) v).removeAllViewsOnPage();
            }
        }
        this.mDeferredSyncWidgetPageItems.clear();
        this.mDeferredPrepareLoadWidgetPreviewsTasks.clear();
    }

    public void setContentType(ContentType type) {
        if (type == ContentType.Widgets) {
            invalidatePageData(this.mNumAppsPages, true);
        } else if (type == ContentType.Applications) {
            invalidatePageData(0, true);
        }
    }

    /* access modifiers changed from: protected */
    public void snapToPage(int whichPage, int delta, int duration) {
        super.snapToPage(whichPage, delta, duration);
        updateCurrentTab(whichPage);
        Iterator<AppsCustomizeAsyncTask> iter = this.mRunningTasks.iterator();
        while (iter.hasNext()) {
            AppsCustomizeAsyncTask task = iter.next();
            int pageIndex = task.page;
            if ((this.mNextPage <= this.mCurrentPage || pageIndex < this.mCurrentPage) && (this.mNextPage >= this.mCurrentPage || pageIndex > this.mCurrentPage)) {
                task.setThreadPriority(19);
            } else {
                task.setThreadPriority(getThreadPriorityForPage(pageIndex));
            }
        }
    }

    private void updateCurrentTab(int currentPage) {
        String tag;
        AppsCustomizeTabHost tabHost = getTabHost();
        if (tabHost != null && (tag = tabHost.getCurrentTabTag()) != null) {
            if (currentPage >= this.mNumAppsPages && !tag.equals(tabHost.getTabTagForContentType(ContentType.Widgets))) {
                tabHost.setCurrentTabFromContent(ContentType.Widgets);
            } else if (currentPage < this.mNumAppsPages && !tag.equals(tabHost.getTabTagForContentType(ContentType.Applications))) {
                tabHost.setCurrentTabFromContent(ContentType.Applications);
            }
        }
    }

    private void setVisibilityOnChildren(ViewGroup layout, int visibility) {
        int childCount = layout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            layout.getChildAt(i).setVisibility(visibility);
        }
    }

    private void setupPage(PagedViewCellLayout layout) {
        layout.setCellCount(this.mCellCountX, this.mCellCountY);
        layout.setGap(this.mPageLayoutWidthGap, this.mPageLayoutHeightGap);
        layout.setPadding(this.mPageLayoutPaddingLeft, this.mPageLayoutPaddingTop, this.mPageLayoutPaddingRight, this.mPageLayoutPaddingBottom);
        setVisibilityOnChildren(layout, 8);
        int widthSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), ExploreByTouchHelper.INVALID_ID);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), ExploreByTouchHelper.INVALID_ID);
        layout.setMinimumWidth(getPageContentWidth());
        layout.measure(widthSpec, heightSpec);
        setVisibilityOnChildren(layout, 0);
    }

    public void syncAppsPageItems(int page, boolean immediate) {
        int numCells = this.mCellCountX * this.mCellCountY;
        int startIndex = page * numCells;
        int endIndex = Math.min(startIndex + numCells, this.mApps.size());
        PagedViewCellLayout layout = (PagedViewCellLayout) getPageAt(page);
        layout.removeAllViewsOnPage();
        ArrayList<Object> items = new ArrayList<>();
        ArrayList<Bitmap> images = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i++) {
            ApplicationInfo info = this.mApps.get(i);
            PagedViewIcon icon = (PagedViewIcon) this.mLayoutInflater.inflate(R.layout.apps_customize_application, layout, false);
            icon.applyFromApplicationInfo(info, true, this);
            icon.setOnClickListener(this);
            icon.setOnLongClickListener(this);
            icon.setOnTouchListener(this);
            icon.setOnKeyListener(this);
            int index = i - startIndex;
            layout.addViewToCellLayout(icon, -1, i, new PagedViewCellLayout.LayoutParams(index % this.mCellCountX, index / this.mCellCountX, 1, 1));
            items.add(info);
            images.add(info.iconBitmap);
            if (i == 0) {
                icon.requestFocus();
            }
        }
        layout.createHardwareLayers();
    }

    private int getWidgetPageLoadPriority(int page) {
        int toPage = this.mCurrentPage;
        if (this.mNextPage > -1) {
            toPage = this.mNextPage;
        }
        Iterator<AppsCustomizeAsyncTask> iter = this.mRunningTasks.iterator();
        int minPageDiff = Integer.MAX_VALUE;
        while (iter.hasNext()) {
            minPageDiff = Math.abs(iter.next().page - toPage);
        }
        int rawPageDiff = Math.abs(page - toPage);
        return rawPageDiff - Math.min(rawPageDiff, minPageDiff);
    }

    private int getThreadPriorityForPage(int page) {
        int pageDiff = getWidgetPageLoadPriority(page);
        if (pageDiff <= 0) {
            return 1;
        }
        if (pageDiff <= 1) {
            return 19;
        }
        return 19;
    }

    private int getSleepForPage(int page) {
        return Math.max(0, getWidgetPageLoadPriority(page) * sPageSleepDelay);
    }

    /* access modifiers changed from: private */
    public void prepareLoadWidgetPreviewsTask(int page, ArrayList<Object> widgets, int cellWidth, int cellHeight, int cellCountX) {
        Iterator<AppsCustomizeAsyncTask> iter = this.mRunningTasks.iterator();
        while (iter.hasNext()) {
            AppsCustomizeAsyncTask task = iter.next();
            int taskPage = task.page;
            if (taskPage < getAssociatedLowerPageBound(this.mCurrentPage) || taskPage > getAssociatedUpperPageBound(this.mCurrentPage)) {
                task.cancel(false);
                iter.remove();
            } else {
                task.setThreadPriority(getThreadPriorityForPage(taskPage));
            }
        }
        final int sleepMs = getSleepForPage(page);
        AsyncTaskPageData pageData = new AsyncTaskPageData(page, widgets, cellWidth, cellHeight, new AsyncTaskCallback() {
            public void run(AppsCustomizeAsyncTask task, AsyncTaskPageData data) {
                try {
                    Thread.sleep((long) sleepMs);
                } catch (Exception e) {
                }
                try {
                    AppsCustomizePagedView.this.loadWidgetPreviewsInBackground(task, data);
                } finally {
                    if (task.isCancelled()) {
                        data.cleanup(true);
                    }
                }
            }
        }, new AsyncTaskCallback() {
            public void run(AppsCustomizeAsyncTask task, AsyncTaskPageData data) {
                AppsCustomizePagedView.this.mRunningTasks.remove(task);
                if (!task.isCancelled()) {
                    AppsCustomizePagedView.this.onSyncWidgetPageItems(data);
                }
            }
        });
        AppsCustomizeAsyncTask t = new AppsCustomizeAsyncTask(page, AsyncTaskPageData.Type.LoadWidgetPreviewData);
        t.setThreadPriority(getThreadPriorityForPage(page));
        t.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new AsyncTaskPageData[]{pageData});
        this.mRunningTasks.add(t);
    }

    private void setupPage(PagedViewGridLayout layout) {
        layout.setPadding(this.mPageLayoutPaddingLeft, this.mPageLayoutPaddingTop, this.mPageLayoutPaddingRight, this.mPageLayoutPaddingBottom);
        int widthSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), ExploreByTouchHelper.INVALID_ID);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), ExploreByTouchHelper.INVALID_ID);
        layout.setMinimumWidth(getPageContentWidth());
        layout.measure(widthSpec, heightSpec);
    }

    private void renderDrawableToBitmap(Drawable d, Bitmap bitmap, int x, int y, int w, int h) {
        renderDrawableToBitmap(d, bitmap, x, y, w, h, 1.0f);
    }

    private void renderDrawableToBitmap(Drawable d, Bitmap bitmap, int x, int y, int w, int h, float scale) {
        if (bitmap != null) {
            Canvas c = new Canvas(bitmap);
            c.scale(scale, scale);
            Rect oldBounds = d.copyBounds();
            d.setBounds(x, y, x + w, y + h);
            d.draw(c);
            d.setBounds(oldBounds);
            c.setBitmap((Bitmap) null);
        }
    }

    private Bitmap getShortcutPreview(ResolveInfo info, int maxWidth, int maxHeight) {
        Bitmap tempBitmap = (Bitmap) this.mCachedShortcutPreviewBitmap.get();
        Canvas c = (Canvas) this.mCachedShortcutPreviewCanvas.get();
        if (tempBitmap != null && tempBitmap.getWidth() == maxWidth && tempBitmap.getHeight() == maxHeight) {
            c.setBitmap(tempBitmap);
            c.drawColor(0, PorterDuff.Mode.CLEAR);
            c.setBitmap((Bitmap) null);
        } else {
            tempBitmap = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888);
            this.mCachedShortcutPreviewBitmap.set(tempBitmap);
        }
        Drawable icon = this.mIconCache.getFullResIcon(info);
        int paddingTop = getResources().getDimensionPixelOffset(R.dimen.shortcut_preview_padding_top);
        int paddingLeft = getResources().getDimensionPixelOffset(R.dimen.shortcut_preview_padding_left);
        int scaledIconWidth = (maxWidth - paddingLeft) - getResources().getDimensionPixelOffset(R.dimen.shortcut_preview_padding_right);
        renderDrawableToBitmap(icon, tempBitmap, paddingLeft, paddingTop, scaledIconWidth, scaledIconWidth);
        Bitmap preview = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888);
        c.setBitmap(preview);
        Paint p = (Paint) this.mCachedShortcutPreviewPaint.get();
        if (p == null) {
            p = new Paint();
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0.0f);
            p.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            p.setAlpha(15);
            this.mCachedShortcutPreviewPaint.set(p);
        }
        c.drawBitmap(tempBitmap, 0.0f, 0.0f, p);
        c.setBitmap((Bitmap) null);
        renderDrawableToBitmap(icon, preview, 0, 0, this.mAppIconSize, this.mAppIconSize);
        return preview;
    }

    private Bitmap getWidgetPreview(ComponentName provider, int previewImage, int iconId, int cellHSpan, int cellVSpan, int maxWidth, int maxHeight) {
        int bitmapWidth;
        int bitmapHeight;
        String packageName = provider.getPackageName();
        if (maxWidth < 0) {
            maxWidth = Integer.MAX_VALUE;
        }
        if (maxHeight < 0) {
        }
        Drawable drawable = null;
        if (previewImage != 0 && (drawable = this.mPackageManager.getDrawable(packageName, previewImage, (ApplicationInfo) null)) == null) {
            Log.w(TAG, "Can't load widget preview drawable 0x" + Integer.toHexString(previewImage) + " for provider: " + provider);
        }
        Bitmap defaultPreview = null;
        boolean widgetPreviewExists = drawable != null;
        if (widgetPreviewExists) {
            bitmapWidth = drawable.getIntrinsicWidth();
            bitmapHeight = drawable.getIntrinsicHeight();
        } else {
            if (cellHSpan < 1) {
                cellHSpan = 1;
            }
            if (cellVSpan < 1) {
                cellVSpan = 1;
            }
            BitmapDrawable previewDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.widget_preview_tile);
            int previewDrawableWidth = previewDrawable.getIntrinsicWidth();
            int previewDrawableHeight = previewDrawable.getIntrinsicHeight();
            bitmapWidth = previewDrawableWidth * cellHSpan;
            bitmapHeight = previewDrawableHeight * cellVSpan;
            defaultPreview = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas c = (Canvas) this.mCachedAppWidgetPreviewCanvas.get();
            c.setBitmap(defaultPreview);
            previewDrawable.setBounds(0, 0, bitmapWidth, bitmapHeight);
            previewDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            previewDrawable.draw(c);
            c.setBitmap((Bitmap) null);
            float iconScale = Math.min(((float) Math.min(bitmapWidth, bitmapHeight)) / ((float) (this.mAppIconSize + (((int) (((float) this.mAppIconSize) * 0.25f)) * 2))), 1.0f);
            Drawable icon = null;
            try {
                int hoffset = (int) ((((float) previewDrawableWidth) - (((float) this.mAppIconSize) * iconScale)) / 2.0f);
                int yoffset = (int) ((((float) previewDrawableHeight) - (((float) this.mAppIconSize) * iconScale)) / 2.0f);
                if (iconId > 0) {
                    icon = this.mIconCache.getFullResIcon(packageName, iconId);
                }
                if (icon != null) {
                    renderDrawableToBitmap(icon, defaultPreview, hoffset, yoffset, (int) (((float) this.mAppIconSize) * iconScale), (int) (((float) this.mAppIconSize) * iconScale));
                }
            } catch (Resources.NotFoundException e) {
            }
        }
        float scale = 1.0f;
        if (bitmapWidth > maxWidth) {
            scale = ((float) maxWidth) / ((float) bitmapWidth);
        }
        if (scale != 1.0f) {
            bitmapWidth = (int) (((float) bitmapWidth) * scale);
            bitmapHeight = (int) (((float) bitmapHeight) * scale);
        }
        Bitmap preview = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        if (widgetPreviewExists) {
            renderDrawableToBitmap(drawable, preview, 0, 0, bitmapWidth, bitmapHeight);
        } else {
            Canvas c2 = (Canvas) this.mCachedAppWidgetPreviewCanvas.get();
            Rect src = (Rect) this.mCachedAppWidgetPreviewSrcRect.get();
            Rect dest = (Rect) this.mCachedAppWidgetPreviewDestRect.get();
            c2.setBitmap(preview);
            src.set(0, 0, defaultPreview.getWidth(), defaultPreview.getHeight());
            dest.set(0, 0, preview.getWidth(), preview.getHeight());
            Paint p = (Paint) this.mCachedAppWidgetPreviewPaint.get();
            if (p == null) {
                p = new Paint();
                p.setFilterBitmap(true);
                this.mCachedAppWidgetPreviewPaint.set(p);
            }
            c2.drawBitmap(defaultPreview, src, dest, p);
            c2.setBitmap((Bitmap) null);
        }
        return preview;
    }

    public void syncWidgetPageItems(int page, boolean immediate) {
        int numItemsPerPage = this.mWidgetCountX * this.mWidgetCountY;
        final ArrayList<Object> items = new ArrayList<>();
        final int cellWidth = (((this.mWidgetSpacingLayout.getContentWidth() - this.mPageLayoutPaddingLeft) - this.mPageLayoutPaddingRight) - ((this.mWidgetCountX - 1) * this.mWidgetWidthGap)) / this.mWidgetCountX;
        final int cellHeight = (((this.mWidgetSpacingLayout.getContentHeight() - this.mPageLayoutPaddingTop) - this.mPageLayoutPaddingBottom) - ((this.mWidgetCountY - 1) * this.mWidgetHeightGap)) / this.mWidgetCountY;
        int offset = (page - this.mNumAppsPages) * numItemsPerPage;
        for (int i = offset; i < Math.min(offset + numItemsPerPage, this.mWidgets.size()); i++) {
            items.add(this.mWidgets.get(i));
        }
        final PagedViewGridLayout layout = (PagedViewGridLayout) getPageAt(page);
        layout.setColumnCount(layout.getCellCountX());
        for (int i2 = 0; i2 < items.size(); i2++) {
            Object rawInfo = items.get(i2);
            PagedViewWidget widget = (PagedViewWidget) this.mLayoutInflater.inflate(R.layout.apps_customize_widget, layout, false);
            if (rawInfo instanceof AppWidgetProviderInfo) {
                AppWidgetProviderInfo info = (AppWidgetProviderInfo) rawInfo;
                PendingAddItemInfo createItemInfo = new PendingAddWidgetInfo(info, (String) null, (Parcelable) null);
                int[] spanXY = Launcher.getSpanForWidget((Context) this.mLauncher, info);
                createItemInfo.spanX = spanXY[0];
                createItemInfo.spanY = spanXY[1];
                int[] minSpanXY = Launcher.getMinSpanForWidget((Context) this.mLauncher, info);
                createItemInfo.minSpanX = minSpanXY[0];
                createItemInfo.minSpanY = minSpanXY[1];
                widget.applyFromAppWidgetProviderInfo(info, -1, spanXY);
                widget.setTag(createItemInfo);
                widget.setShortPressListener(this);
            } else if (rawInfo instanceof ResolveInfo) {
                ResolveInfo info2 = (ResolveInfo) rawInfo;
                PendingAddItemInfo createItemInfo2 = new PendingAddShortcutInfo(info2.activityInfo);
                createItemInfo2.itemType = 1;
                createItemInfo2.componentName = new ComponentName(info2.activityInfo.packageName, info2.activityInfo.name);
                widget.applyFromResolveInfo(this.mPackageManager, info2);
                widget.setTag(createItemInfo2);
            }
            widget.setOnClickListener(this);
            widget.setOnLongClickListener(this);
            widget.setOnTouchListener(this);
            widget.setOnKeyListener(this);
            int ix = i2 % this.mWidgetCountX;
            int iy = i2 / this.mWidgetCountX;
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(GridLayout.spec(iy, GridLayout.LEFT), GridLayout.spec(ix, GridLayout.TOP));
            layoutParams.width = cellWidth;
            layoutParams.height = cellHeight;
            layoutParams.setGravity(51);
            if (ix > 0) {
                layoutParams.leftMargin = this.mWidgetWidthGap;
            }
            if (iy > 0) {
                layoutParams.topMargin = this.mWidgetHeightGap;
            }
            layout.addView(widget, layoutParams);
        }
        final boolean z = immediate;
        final int i3 = page;
        layout.setOnLayoutListener(new Runnable() {
            public void run() {
                int maxPreviewWidth = cellWidth;
                int maxPreviewHeight = cellHeight;
                if (layout.getChildCount() > 0) {
                    int[] maxSize = ((PagedViewWidget) layout.getChildAt(0)).getPreviewSize();
                    maxPreviewWidth = maxSize[0];
                    maxPreviewHeight = maxSize[1];
                }
                if (z) {
                    AsyncTaskPageData data = new AsyncTaskPageData(i3, items, maxPreviewWidth, maxPreviewHeight, (AsyncTaskCallback) null, (AsyncTaskCallback) null);
                    AppsCustomizePagedView.this.loadWidgetPreviewsInBackground((AppsCustomizeAsyncTask) null, data);
                    AppsCustomizePagedView.this.onSyncWidgetPageItems(data);
                } else if (AppsCustomizePagedView.this.mInTransition) {
                    AppsCustomizePagedView.this.mDeferredPrepareLoadWidgetPreviewsTasks.add(this);
                } else {
                    AppsCustomizePagedView.this.prepareLoadWidgetPreviewsTask(i3, items, maxPreviewWidth, maxPreviewHeight, AppsCustomizePagedView.this.mWidgetCountX);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void loadWidgetPreviewsInBackground(AppsCustomizeAsyncTask task, AsyncTaskPageData data) {
        if (task != null) {
            task.syncThreadPriority();
        }
        ArrayList<Object> items = data.items;
        ArrayList<Bitmap> images = data.generatedImages;
        int count = items.size();
        for (int i = 0; i < count; i++) {
            if (task != null) {
                if (!task.isCancelled()) {
                    task.syncThreadPriority();
                } else {
                    return;
                }
            }
            Object rawInfo = items.get(i);
            if (rawInfo instanceof AppWidgetProviderInfo) {
                AppWidgetProviderInfo info = (AppWidgetProviderInfo) rawInfo;
                int[] cellSpans = Launcher.getSpanForWidget((Context) this.mLauncher, info);
                images.add(getWidgetPreview(info.provider, info.previewImage, info.icon, cellSpans[0], cellSpans[1], Math.min(data.maxImageWidth, this.mWidgetSpacingLayout.estimateCellWidth(cellSpans[0])), Math.min(data.maxImageHeight, this.mWidgetSpacingLayout.estimateCellHeight(cellSpans[1]))));
            } else if (rawInfo instanceof ResolveInfo) {
                images.add(getShortcutPreview((ResolveInfo) rawInfo, data.maxImageWidth, data.maxImageHeight));
            }
        }
    }

    /* access modifiers changed from: private */
    public void onSyncWidgetPageItems(AsyncTaskPageData data) {
        if (this.mInTransition) {
            this.mDeferredSyncWidgetPageItems.add(data);
            return;
        }
        try {
            PagedViewGridLayout layout = (PagedViewGridLayout) getPageAt(data.page);
            int count = data.items.size();
            for (int i = 0; i < count; i++) {
                PagedViewWidget widget = (PagedViewWidget) layout.getChildAt(i);
                if (widget != null) {
                    widget.applyPreview(new FastBitmapDrawable(data.generatedImages.get(i)), i);
                }
            }
            layout.createHardwareLayer();
            invalidate();
            Iterator<AppsCustomizeAsyncTask> iter = this.mRunningTasks.iterator();
            while (iter.hasNext()) {
                AppsCustomizeAsyncTask task = iter.next();
                task.setThreadPriority(getThreadPriorityForPage(task.page));
            }
        } finally {
            data.cleanup(false);
        }
    }

    public void syncPages() {
        removeAllViews();
        cancelAllTasks();
        Context context = getContext();
        for (int j = 0; j < this.mNumWidgetPages; j++) {
            PagedViewGridLayout layout = new PagedViewGridLayout(context, this.mWidgetCountX, this.mWidgetCountY);
            setupPage(layout);
            addView(layout, new ViewGroup.LayoutParams(-1, -1));
        }
        for (int i = 0; i < this.mNumAppsPages; i++) {
            PagedViewCellLayout layout2 = new PagedViewCellLayout(context);
            setupPage(layout2);
            addView(layout2);
        }
    }

    public void syncPageItems(int page, boolean immediate) {
        if (page < this.mNumAppsPages) {
            syncAppsPageItems(page, immediate);
        } else {
            syncWidgetPageItems(page, immediate);
        }
    }

    /* access modifiers changed from: package-private */
    public View getPageAt(int index) {
        return getChildAt(indexToPage(index));
    }

    /* access modifiers changed from: protected */
    public int indexToPage(int index) {
        return (getChildCount() - index) - 1;
    }

    /* access modifiers changed from: protected */
    public void screenScrolled(int screenCenter) {
        float alpha;
        super.screenScrolled(screenCenter);
        for (int i = 0; i < getChildCount(); i++) {
            View v = getPageAt(i);
            if (v != null) {
                float scrollProgress = getScrollProgress(screenCenter, v, i);
                float interpolatedProgress = this.mZInterpolator.getInterpolation(Math.abs(Math.min(scrollProgress, 0.0f)));
                float scale = (1.0f - interpolatedProgress) + (TRANSITION_SCALE_FACTOR * interpolatedProgress);
                float translationX = Math.min(0.0f, scrollProgress) * ((float) v.getMeasuredWidth());
                if (scrollProgress >= 0.0f) {
                    alpha = this.mLeftScreenAlphaInterpolator.getInterpolation(1.0f - scrollProgress);
                } else if (scrollProgress < 0.0f) {
                    alpha = this.mAlphaInterpolator.getInterpolation(1.0f - Math.abs(scrollProgress));
                } else {
                    alpha = 1.0f;
                }
                v.setCameraDistance(this.mDensity * CAMERA_DISTANCE);
                int pageWidth = v.getMeasuredWidth();
                int pageHeight = v.getMeasuredHeight();
                if (i == 0 && scrollProgress < 0.0f) {
                    v.setPivotX(TRANSITION_PIVOT * ((float) pageWidth));
                    v.setRotationY((-TRANSITION_MAX_ROTATION) * scrollProgress);
                    scale = 1.0f;
                    alpha = 1.0f;
                    translationX = 0.0f;
                } else if (i != getChildCount() - 1 || scrollProgress <= 0.0f) {
                    v.setPivotY(((float) pageHeight) / 2.0f);
                    v.setPivotX(((float) pageWidth) / 2.0f);
                    v.setRotationY(0.0f);
                } else {
                    v.setPivotX((1.0f - TRANSITION_PIVOT) * ((float) pageWidth));
                    v.setRotationY((-TRANSITION_MAX_ROTATION) * scrollProgress);
                    scale = 1.0f;
                    alpha = 1.0f;
                    translationX = 0.0f;
                }
                v.setTranslationX(translationX);
                v.setScaleX(scale);
                v.setScaleY(scale);
                v.setAlpha(alpha);
                if (alpha == 0.0f) {
                    v.setVisibility(4);
                } else if (v.getVisibility() != 0) {
                    v.setVisibility(0);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void overScroll(float amount) {
        acceleratedOverScroll(amount);
    }

    public int getPageContentWidth() {
        return this.mContentWidth;
    }

    /* access modifiers changed from: protected */
    public void onPageEndMoving() {
        super.onPageEndMoving();
        this.mForceDrawAllChildrenNextFrame = true;
        this.mSaveInstanceStateItemIndex = -1;
    }

    public void setup(Launcher launcher, DragController dragController) {
        this.mLauncher = launcher;
        this.mDragController = dragController;
    }

    private void invalidateOnDataChange() {
        if (!isDataReady()) {
            requestLayout();
            return;
        }
        cancelAllTasks();
        invalidatePageData();
    }

    public void setApps(ArrayList<ApplicationInfo> list) {
        this.mApps = list;
        updatePageCounts();
        invalidateOnDataChange();
    }

    private void addAppsWithoutInvalidate(ArrayList<ApplicationInfo> list) {
        int count = list.size();
        for (int i = 0; i < count; i++) {
            ApplicationInfo info = list.get(i);
            int index = Collections.binarySearch(this.mApps, info, LauncherModel.getAppNameComparator());
            if (index < 0) {
                this.mApps.add(-(index + 1), info);
            }
        }
    }

    public void addApps(ArrayList<ApplicationInfo> list) {
        addAppsWithoutInvalidate(list);
        updatePageCounts();
        invalidateOnDataChange();
    }

    private int findAppByComponent(List<ApplicationInfo> list, ApplicationInfo item) {
        ComponentName removeComponent = item.intent.getComponent();
        int length = list.size();
        for (int i = 0; i < length; i++) {
            if (list.get(i).intent.getComponent().equals(removeComponent)) {
                return i;
            }
        }
        return -1;
    }

    private int findAppByPackage(List<ApplicationInfo> list, String packageName) {
        int length = list.size();
        for (int i = 0; i < length; i++) {
            if (ItemInfo.getPackageName(list.get(i).intent).equals(packageName)) {
                return i;
            }
        }
        return -1;
    }

    private void removeAppsWithoutInvalidate(ArrayList<ApplicationInfo> list) {
        int length = list.size();
        for (int i = 0; i < length; i++) {
            int removeIndex = findAppByComponent(this.mApps, list.get(i));
            if (removeIndex > -1) {
                this.mApps.remove(removeIndex);
            }
        }
    }

    private void removeAppsWithPackageNameWithoutInvalidate(ArrayList<String> packageNames) {
        Iterator<String> it = packageNames.iterator();
        while (it.hasNext()) {
            String pn = it.next();
            int removeIndex = findAppByPackage(this.mApps, pn);
            while (removeIndex > -1) {
                this.mApps.remove(removeIndex);
                removeIndex = findAppByPackage(this.mApps, pn);
            }
        }
    }

    public void removeApps(ArrayList<String> packageNames) {
        removeAppsWithPackageNameWithoutInvalidate(packageNames);
        updatePageCounts();
        invalidateOnDataChange();
    }

    public void updateApps(ArrayList<ApplicationInfo> list) {
        removeAppsWithoutInvalidate(list);
        addAppsWithoutInvalidate(list);
        updatePageCounts();
        invalidateOnDataChange();
    }

    public void reset() {
        this.mSaveInstanceStateItemIndex = -1;
        AppsCustomizeTabHost tabHost = getTabHost();
        String tag = tabHost.getCurrentTabTag();
        if (tag != null && !tag.equals(tabHost.getTabTagForContentType(ContentType.Applications))) {
            tabHost.setCurrentTabFromContent(ContentType.Applications);
        }
        if (this.mCurrentPage != 0) {
            invalidatePageData(0);
        }
    }

    private AppsCustomizeTabHost getTabHost() {
        return (AppsCustomizeTabHost) this.mLauncher.findViewById(R.id.apps_customize_pane);
    }

    public void dumpState() {
        ApplicationInfo.dumpApplicationInfoList(TAG, "mApps", this.mApps);
        dumpAppWidgetProviderInfoList(TAG, "mWidgets", this.mWidgets);
    }

    private void dumpAppWidgetProviderInfoList(String tag, String label, ArrayList<Object> list) {
        Log.d(tag, String.valueOf(label) + " size=" + list.size());
        Iterator<Object> it = list.iterator();
        while (it.hasNext()) {
            Object i = it.next();
            if (i instanceof AppWidgetProviderInfo) {
                AppWidgetProviderInfo info = (AppWidgetProviderInfo) i;
                Log.d(tag, "   label=\"" + info.label + "\" previewImage=" + info.previewImage + " resizeMode=" + info.resizeMode + " configure=" + info.configure + " initialLayout=" + info.initialLayout + " minWidth=" + info.minWidth + " minHeight=" + info.minHeight);
            } else if (i instanceof ResolveInfo) {
                ResolveInfo info2 = (ResolveInfo) i;
                Log.d(tag, "   label=\"" + info2.loadLabel(this.mPackageManager) + "\" icon=" + info2.icon);
            }
        }
    }

    public void surrender() {
        cancelAllTasks();
    }

    public void iconPressed(PagedViewIcon icon) {
        if (this.mPressedIcon != null) {
            this.mPressedIcon.resetDrawableState();
        }
        this.mPressedIcon = icon;
    }

    public void resetDrawableState() {
        if (this.mPressedIcon != null) {
            this.mPressedIcon.resetDrawableState();
            this.mPressedIcon = null;
        }
    }

    /* access modifiers changed from: protected */
    public int getAssociatedLowerPageBound(int page) {
        int count = getChildCount();
        return Math.max(Math.min(page - 2, count - Math.min(count, 5)), 0);
    }

    /* access modifiers changed from: protected */
    public int getAssociatedUpperPageBound(int page) {
        int count = getChildCount();
        return Math.min(Math.max(page + 2, Math.min(count, 5) - 1), count - 1);
    }

    /* access modifiers changed from: protected */
    public String getCurrentPageDescription() {
        int stringId;
        int count;
        int page = this.mNextPage != -1 ? this.mNextPage : this.mCurrentPage;
        if (page < this.mNumAppsPages) {
            stringId = R.string.apps_customize_apps_scroll_format;
            count = this.mNumAppsPages;
        } else {
            page -= this.mNumAppsPages;
            stringId = R.string.apps_customize_widgets_scroll_format;
            count = this.mNumWidgetPages;
        }
        return String.format(getContext().getString(stringId), new Object[]{Integer.valueOf(page + 1), Integer.valueOf(count)});
    }
}
