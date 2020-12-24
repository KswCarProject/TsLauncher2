package com.android.launcher2;

import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.SearchManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Advanceable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.launcher.R;
import com.android.launcher2.CellLayout;
import com.android.launcher2.DragLayer;
import com.android.launcher2.DropTarget;
import com.android.launcher2.LauncherModel;
import com.android.launcher2.SmoothPagedView;
import com.android.launcher2.Workspace;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class Launcher extends Activity implements View.OnClickListener, View.OnLongClickListener, LauncherModel.Callbacks, View.OnTouchListener {
    static final int APPWIDGET_HOST_ID = 1024;
    static final boolean DEBUG_STRICT_MODE = false;
    static final boolean DEBUG_WIDGETS = false;
    static final int DEFAULT_SCREEN = 2;
    private static final int DISMISS_CLING_DURATION = 250;
    static final String DUMP_STATE_PROPERTY = "launcher_dump_state";
    private static final int EXIT_SPRINGLOADED_MODE_LONG_TIMEOUT = 600;
    private static final int EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT = 300;
    static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";
    static final String FORCE_ENABLE_ROTATION_PROPERTY = "launcher_force_rotate";
    static final String INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION = "com.android.launcher.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION";
    static final boolean LOGD = false;
    private static final int MENU_GROUP_WALLPAPER = 1;
    private static final int MENU_HELP = 5;
    private static final int MENU_MANAGE_APPS = 3;
    private static final int MENU_SYSTEM_SETTINGS = 4;
    private static final int MENU_WALLPAPER_SETTINGS = 2;
    private static int NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS = 10;
    private static final String PREFERENCES = "launcher.preferences";
    static final boolean PROFILE_STARTUP = false;
    private static final int REQUEST_BIND_APPWIDGET = 11;
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    private static final int REQUEST_CREATE_SHORTCUT = 1;
    private static final int REQUEST_PICK_APPLICATION = 6;
    private static final int REQUEST_PICK_APPWIDGET = 9;
    private static final int REQUEST_PICK_SHORTCUT = 7;
    private static final int REQUEST_PICK_WALLPAPER = 10;
    private static final String RUNTIME_STATE = "launcher.state";
    private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cell_x";
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cell_y";
    private static final String RUNTIME_STATE_PENDING_ADD_CONTAINER = "launcher.add_container";
    private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_span_x";
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_span_y";
    private static final String RUNTIME_STATE_PENDING_ADD_WIDGET_INFO = "launcher.add_widget_info";
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";
    static int SCREEN_COUNT = 5;
    private static final int SHOW_CLING_DURATION = 550;
    static final String TAG = "Launcher";
    private static final String TOOLBAR_ICON_METADATA_NAME = "com.android.launcher.toolbar_icon";
    private static final String TOOLBAR_SEARCH_ICON_METADATA_NAME = "com.android.launcher.toolbar_search_icon";
    private static final String TOOLBAR_VOICE_SEARCH_ICON_METADATA_NAME = "com.android.launcher.toolbar_voice_search_icon";
    private static Drawable.ConstantState[] sAppMarketIcon = new Drawable.ConstantState[2];
    static final ArrayList<String> sDumpLogs = new ArrayList<>();
    private static HashMap<Long, FolderInfo> sFolders = new HashMap<>();
    private static boolean sForceEnableRotation = isPropertyEnabled(FORCE_ENABLE_ROTATION_PROPERTY);
    private static Drawable.ConstantState[] sGlobalSearchIcon = new Drawable.ConstantState[2];
    /* access modifiers changed from: private */
    public static LocaleConfiguration sLocaleConfiguration = null;
    private static final Object sLock = new Object();
    private static boolean sPausedFromUserAction = false;
    private static ArrayList<PendingAddArguments> sPendingAddList = new ArrayList<>();
    private static int sScreen = 2;
    private static Drawable.ConstantState[] sVoiceSearchIcon = new Drawable.ConstantState[2];
    private final int ADVANCE_MSG = 1;
    private final int mAdvanceInterval = 20000;
    private final int mAdvanceStagger = DISMISS_CLING_DURATION;
    private View mAllAppsButton;
    private Intent mAppMarketIntent = null;
    /* access modifiers changed from: private */
    public LauncherAppWidgetHost mAppWidgetHost;
    private AppWidgetManager mAppWidgetManager;
    /* access modifiers changed from: private */
    public AppsCustomizePagedView mAppsCustomizeContent;
    /* access modifiers changed from: private */
    public AppsCustomizeTabHost mAppsCustomizeTabHost;
    private boolean mAttached = false;
    private boolean mAutoAdvanceRunning = false;
    private long mAutoAdvanceSentTime;
    private long mAutoAdvanceTimeLeft = -1;
    private Drawable mBlackBackgroundDrawable;
    /* access modifiers changed from: private */
    public Runnable mBuildLayersRunnable = new Runnable() {
        public void run() {
            if (Launcher.this.mWorkspace != null) {
                Launcher.this.mWorkspace.buildPageHardwareLayers();
            }
        }
    };
    private final BroadcastReceiver mCloseSystemDialogsReceiver = new CloseSystemDialogsIntentReceiver(this, (CloseSystemDialogsIntentReceiver) null);
    private SpannableStringBuilder mDefaultKeySsb = null;
    private AnimatorSet mDividerAnimator;
    private View mDockDivider;
    private DragController mDragController;
    /* access modifiers changed from: private */
    public DragLayer mDragLayer;
    private Bitmap mFolderIconBitmap;
    private Canvas mFolderIconCanvas;
    /* access modifiers changed from: private */
    public ImageView mFolderIconImageView;
    private FolderInfo mFolderInfo;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                int i = 0;
                for (View key : Launcher.this.mWidgetsToAdvance.keySet()) {
                    final View v = key.findViewById(((AppWidgetProviderInfo) Launcher.this.mWidgetsToAdvance.get(key)).autoAdvanceViewId);
                    int delay = i * Launcher.DISMISS_CLING_DURATION;
                    if (v instanceof Advanceable) {
                        postDelayed(new Runnable() {
                            public void run() {
                                ((Advanceable) v).advance();
                            }
                        }, (long) delay);
                    }
                    i++;
                }
                Launcher.this.sendAdvanceMessage(20000);
            }
        }
    };
    private HideFromAccessibilityHelper mHideFromAccessibilityHelper = new HideFromAccessibilityHelper();
    private Hotseat mHotseat;
    private IconCache mIconCache;
    private LayoutInflater mInflater;
    private View mLauncherView;
    private LauncherModel mModel;
    private int mNewShortcutAnimatePage = -1;
    private ArrayList<View> mNewShortcutAnimateViews = new ArrayList<>();
    private boolean mOnResumeNeedsLoad;
    /* access modifiers changed from: private */
    public State mOnResumeState = State.NONE;
    private boolean mPaused = true;
    /* access modifiers changed from: private */
    public ItemInfo mPendingAddInfo = new ItemInfo();
    private AppWidgetProviderInfo mPendingAddWidgetInfo;
    private View mQsbDivider;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.SCREEN_OFF".equals(action)) {
                Launcher.this.mUserPresent = false;
                Launcher.this.mDragLayer.clearAllResizeFrames();
                Launcher.this.updateRunning();
                if (Launcher.this.mAppsCustomizeTabHost != null && Launcher.this.mPendingAddInfo.container == -1) {
                    Launcher.this.mAppsCustomizeTabHost.reset();
                    Launcher.this.showWorkspace(false);
                }
            } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                Launcher.this.mUserPresent = true;
                Launcher.this.updateRunning();
            }
        }
    };
    private Rect mRectForFolderAnimation = new Rect();
    private final int mRestoreScreenOrientationDelay = 500;
    private boolean mRestoring;
    private Bundle mSavedInstanceState;
    private Bundle mSavedState;
    /* access modifiers changed from: private */
    public SearchDropTargetBar mSearchDropTargetBar;
    /* access modifiers changed from: private */
    public SharedPreferences mSharedPrefs;
    /* access modifiers changed from: private */
    public State mState = State.WORKSPACE;
    /* access modifiers changed from: private */
    public AnimatorSet mStateAnimation;
    private final ArrayList<Integer> mSynchronouslyBoundPages = new ArrayList<>();
    private int[] mTmpAddItemCellCoordinates = new int[2];
    /* access modifiers changed from: private */
    public boolean mUserPresent = true;
    private boolean mVisible = false;
    private boolean mWaitingForResult;
    private BubbleTextView mWaitingForResume;
    private PowerManager.WakeLock mWakeLock;
    private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();
    /* access modifiers changed from: private */
    public HashMap<View, AppWidgetProviderInfo> mWidgetsToAdvance = new HashMap<>();
    /* access modifiers changed from: private */
    public Workspace mWorkspace;
    private Drawable mWorkspaceBackgroundDrawable;
    private boolean mWorkspaceLoading = true;

    private enum State {
        NONE,
        WORKSPACE,
        APPS_CUSTOMIZE,
        APPS_CUSTOMIZE_SPRING_LOADED
    }

    private static class PendingAddArguments {
        int cellX;
        int cellY;
        long container;
        Intent intent;
        int requestCode;
        int screen;

        private PendingAddArguments() {
        }

        /* synthetic */ PendingAddArguments(PendingAddArguments pendingAddArguments) {
            this();
        }
    }

    private static boolean isPropertyEnabled(String propertyName) {
        return Log.isLoggable(propertyName, 2);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        Log.w("###", "onConfigurationChanged###" + newConfig);
        super.onConfigurationChanged(newConfig);
    }

    private static int getStatusBarHeight(Context context) {
        return context.getResources().getDimensionPixelSize(context.getResources().getIdentifier("status_bar_height", "dimen", "android"));
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        this.mWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(1, getClass().getName());
        this.mWakeLock.acquire();
        Log.w("###", "onCreate###" + savedInstanceState);
        super.onCreate(savedInstanceState);
        LauncherApplication app = (LauncherApplication) getApplication();
        this.mSharedPrefs = getSharedPreferences(LauncherApplication.getSharedPreferencesKey(), 0);
        this.mModel = app.setLauncher(this);
        this.mIconCache = app.getIconCache();
        this.mDragController = new DragController(this);
        this.mInflater = getLayoutInflater();
        this.mAppWidgetManager = AppWidgetManager.getInstance(this);
        this.mAppWidgetHost = new LauncherAppWidgetHost(this, 1024);
        this.mAppWidgetHost.startListening();
        this.mPaused = false;
        checkForLocaleChange();
        setContentView(R.layout.launcher);
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().addFlags(67108864);
            getWindow().addFlags(134217728);
            ViewGroup contentView = (ViewGroup) findViewById(16908290);
            View childAt = contentView.getChildAt(0);
            if (childAt != null) {
                childAt.setFitsSystemWindows(true);
            }
            View view = new View(this);
            view.setLayoutParams(new ViewGroup.LayoutParams(-1, getStatusBarHeight(this)));
            view.setBackgroundColor(Color.parseColor("#00000000"));
            contentView.addView(view);
        }
        setupViews();
        showFirstRunWorkspaceCling();
        registerContentObservers();
        lockAllApps();
        this.mSavedState = savedInstanceState;
        restoreState(this.mSavedState);
        if (this.mAppsCustomizeContent != null) {
            this.mAppsCustomizeContent.onPackagesUpdated();
        }
        if (!this.mRestoring) {
            if (sPausedFromUserAction) {
                this.mModel.startLoader(true, -1);
            } else {
                this.mModel.startLoader(true, this.mWorkspace.getCurrentPage());
            }
        }
        if (!this.mModel.isAllAppsLoaded()) {
            this.mInflater.inflate(R.layout.apps_customize_progressbar, (ViewGroup) this.mAppsCustomizeContent.getParent());
        }
        this.mDefaultKeySsb = new SpannableStringBuilder();
        Selection.setSelection(this.mDefaultKeySsb, 0);
        registerReceiver(this.mCloseSystemDialogsReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        updateGlobalIcons();
        unlockScreenOrientation(true);
    }

    /* access modifiers changed from: protected */
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        sPausedFromUserAction = true;
    }

    private void updateGlobalIcons() {
        boolean searchVisible = false;
        boolean voiceVisible = false;
        int coi = getCurrentOrientationIndexForGlobalIcons();
        if (sGlobalSearchIcon[coi] == null || sVoiceSearchIcon[coi] == null || sAppMarketIcon[coi] == null) {
            updateAppMarketIcon();
            searchVisible = updateGlobalSearchIcon();
            voiceVisible = updateVoiceSearchIcon(searchVisible);
        }
        if (sGlobalSearchIcon[coi] != null) {
            updateGlobalSearchIcon(sGlobalSearchIcon[coi]);
            searchVisible = true;
        }
        if (sVoiceSearchIcon[coi] != null) {
            updateVoiceSearchIcon(sVoiceSearchIcon[coi]);
            voiceVisible = true;
        }
        if (sAppMarketIcon[coi] != null) {
            updateAppMarketIcon(sAppMarketIcon[coi]);
        }
        if (this.mSearchDropTargetBar != null) {
            this.mSearchDropTargetBar.onSearchPackagesChanged(searchVisible, voiceVisible);
        }
    }

    /* access modifiers changed from: private */
    public void checkForLocaleChange() {
        boolean localeChanged = false;
        if (sLocaleConfiguration == null) {
            new AsyncTask<Void, Void, LocaleConfiguration>() {
                /* access modifiers changed from: protected */
                public LocaleConfiguration doInBackground(Void... unused) {
                    LocaleConfiguration localeConfiguration = new LocaleConfiguration((LocaleConfiguration) null);
                    Launcher.readConfiguration(Launcher.this, localeConfiguration);
                    return localeConfiguration;
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(LocaleConfiguration result) {
                    Launcher.sLocaleConfiguration = result;
                    Launcher.this.checkForLocaleChange();
                }
            }.execute(new Void[0]);
            return;
        }
        Configuration configuration = getResources().getConfiguration();
        String previousLocale = sLocaleConfiguration.locale;
        String locale = configuration.locale.toString();
        int previousMcc = sLocaleConfiguration.mcc;
        int mcc = configuration.mcc;
        int previousMnc = sLocaleConfiguration.mnc;
        int mnc = configuration.mnc;
        if (!(locale.equals(previousLocale) && mcc == previousMcc && mnc == previousMnc)) {
            localeChanged = true;
        }
        if (localeChanged) {
            sLocaleConfiguration.locale = locale;
            sLocaleConfiguration.mcc = mcc;
            sLocaleConfiguration.mnc = mnc;
            this.mIconCache.flush();
            final LocaleConfiguration localeConfiguration = sLocaleConfiguration;
            new Thread("WriteLocaleConfiguration") {
                public void run() {
                    Launcher.writeConfiguration(Launcher.this, localeConfiguration);
                }
            }.start();
        }
    }

    private static class LocaleConfiguration {
        public String locale;
        public int mcc;
        public int mnc;

        private LocaleConfiguration() {
            this.mcc = -1;
            this.mnc = -1;
        }

        /* synthetic */ LocaleConfiguration(LocaleConfiguration localeConfiguration) {
            this();
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0028 A[SYNTHETIC, Splitter:B:11:0x0028] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0031 A[SYNTHETIC, Splitter:B:16:0x0031] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x003a A[SYNTHETIC, Splitter:B:21:0x003a] */
    /* JADX WARNING: Removed duplicated region for block: B:35:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:36:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void readConfiguration(android.content.Context r4, com.android.launcher2.Launcher.LocaleConfiguration r5) {
        /*
            r0 = 0
            java.io.DataInputStream r1 = new java.io.DataInputStream     // Catch:{ FileNotFoundException -> 0x0025, IOException -> 0x002e, all -> 0x0037 }
            java.lang.String r2 = "launcher.preferences"
            java.io.FileInputStream r2 = r4.openFileInput(r2)     // Catch:{ FileNotFoundException -> 0x0025, IOException -> 0x002e, all -> 0x0037 }
            r1.<init>(r2)     // Catch:{ FileNotFoundException -> 0x0025, IOException -> 0x002e, all -> 0x0037 }
            java.lang.String r2 = r1.readUTF()     // Catch:{ FileNotFoundException -> 0x0049, IOException -> 0x0046, all -> 0x0043 }
            r5.locale = r2     // Catch:{ FileNotFoundException -> 0x0049, IOException -> 0x0046, all -> 0x0043 }
            int r2 = r1.readInt()     // Catch:{ FileNotFoundException -> 0x0049, IOException -> 0x0046, all -> 0x0043 }
            r5.mcc = r2     // Catch:{ FileNotFoundException -> 0x0049, IOException -> 0x0046, all -> 0x0043 }
            int r2 = r1.readInt()     // Catch:{ FileNotFoundException -> 0x0049, IOException -> 0x0046, all -> 0x0043 }
            r5.mnc = r2     // Catch:{ FileNotFoundException -> 0x0049, IOException -> 0x0046, all -> 0x0043 }
            if (r1 == 0) goto L_0x004c
            r1.close()     // Catch:{ IOException -> 0x003e }
            r0 = r1
        L_0x0024:
            return
        L_0x0025:
            r2 = move-exception
        L_0x0026:
            if (r0 == 0) goto L_0x0024
            r0.close()     // Catch:{ IOException -> 0x002c }
            goto L_0x0024
        L_0x002c:
            r2 = move-exception
            goto L_0x0024
        L_0x002e:
            r2 = move-exception
        L_0x002f:
            if (r0 == 0) goto L_0x0024
            r0.close()     // Catch:{ IOException -> 0x0035 }
            goto L_0x0024
        L_0x0035:
            r2 = move-exception
            goto L_0x0024
        L_0x0037:
            r2 = move-exception
        L_0x0038:
            if (r0 == 0) goto L_0x003d
            r0.close()     // Catch:{ IOException -> 0x0041 }
        L_0x003d:
            throw r2
        L_0x003e:
            r2 = move-exception
            r0 = r1
            goto L_0x0024
        L_0x0041:
            r3 = move-exception
            goto L_0x003d
        L_0x0043:
            r2 = move-exception
            r0 = r1
            goto L_0x0038
        L_0x0046:
            r2 = move-exception
            r0 = r1
            goto L_0x002f
        L_0x0049:
            r2 = move-exception
            r0 = r1
            goto L_0x0026
        L_0x004c:
            r0 = r1
            goto L_0x0024
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.launcher2.Launcher.readConfiguration(android.content.Context, com.android.launcher2.Launcher$LocaleConfiguration):void");
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0029 A[SYNTHETIC, Splitter:B:11:0x0029] */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003b A[SYNTHETIC, Splitter:B:19:0x003b] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0044 A[SYNTHETIC, Splitter:B:24:0x0044] */
    /* JADX WARNING: Removed duplicated region for block: B:38:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:39:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void writeConfiguration(android.content.Context r5, com.android.launcher2.Launcher.LocaleConfiguration r6) {
        /*
            r1 = 0
            java.io.DataOutputStream r2 = new java.io.DataOutputStream     // Catch:{ FileNotFoundException -> 0x0026, IOException -> 0x002f }
            java.lang.String r3 = "launcher.preferences"
            r4 = 0
            java.io.FileOutputStream r3 = r5.openFileOutput(r3, r4)     // Catch:{ FileNotFoundException -> 0x0026, IOException -> 0x002f }
            r2.<init>(r3)     // Catch:{ FileNotFoundException -> 0x0026, IOException -> 0x002f }
            java.lang.String r3 = r6.locale     // Catch:{ FileNotFoundException -> 0x0053, IOException -> 0x0050, all -> 0x004d }
            r2.writeUTF(r3)     // Catch:{ FileNotFoundException -> 0x0053, IOException -> 0x0050, all -> 0x004d }
            int r3 = r6.mcc     // Catch:{ FileNotFoundException -> 0x0053, IOException -> 0x0050, all -> 0x004d }
            r2.writeInt(r3)     // Catch:{ FileNotFoundException -> 0x0053, IOException -> 0x0050, all -> 0x004d }
            int r3 = r6.mnc     // Catch:{ FileNotFoundException -> 0x0053, IOException -> 0x0050, all -> 0x004d }
            r2.writeInt(r3)     // Catch:{ FileNotFoundException -> 0x0053, IOException -> 0x0050, all -> 0x004d }
            r2.flush()     // Catch:{ FileNotFoundException -> 0x0053, IOException -> 0x0050, all -> 0x004d }
            if (r2 == 0) goto L_0x0056
            r2.close()     // Catch:{ IOException -> 0x0048 }
            r1 = r2
        L_0x0025:
            return
        L_0x0026:
            r3 = move-exception
        L_0x0027:
            if (r1 == 0) goto L_0x0025
            r1.close()     // Catch:{ IOException -> 0x002d }
            goto L_0x0025
        L_0x002d:
            r3 = move-exception
            goto L_0x0025
        L_0x002f:
            r0 = move-exception
        L_0x0030:
            java.lang.String r3 = "launcher.preferences"
            java.io.File r3 = r5.getFileStreamPath(r3)     // Catch:{ all -> 0x0041 }
            r3.delete()     // Catch:{ all -> 0x0041 }
            if (r1 == 0) goto L_0x0025
            r1.close()     // Catch:{ IOException -> 0x003f }
            goto L_0x0025
        L_0x003f:
            r3 = move-exception
            goto L_0x0025
        L_0x0041:
            r3 = move-exception
        L_0x0042:
            if (r1 == 0) goto L_0x0047
            r1.close()     // Catch:{ IOException -> 0x004b }
        L_0x0047:
            throw r3
        L_0x0048:
            r3 = move-exception
            r1 = r2
            goto L_0x0025
        L_0x004b:
            r4 = move-exception
            goto L_0x0047
        L_0x004d:
            r3 = move-exception
            r1 = r2
            goto L_0x0042
        L_0x0050:
            r0 = move-exception
            r1 = r2
            goto L_0x0030
        L_0x0053:
            r3 = move-exception
            r1 = r2
            goto L_0x0027
        L_0x0056:
            r1 = r2
            goto L_0x0025
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.launcher2.Launcher.writeConfiguration(android.content.Context, com.android.launcher2.Launcher$LocaleConfiguration):void");
    }

    public DragLayer getDragLayer() {
        return this.mDragLayer;
    }

    /* access modifiers changed from: package-private */
    public boolean isDraggingEnabled() {
        return !this.mModel.isLoadingWorkspace();
    }

    static int getScreen() {
        int i;
        synchronized (sLock) {
            i = sScreen;
        }
        return i;
    }

    static void setScreen(int screen) {
        synchronized (sLock) {
            sScreen = screen;
        }
    }

    private boolean completeAdd(PendingAddArguments args) {
        boolean result = false;
        switch (args.requestCode) {
            case 1:
                completeAddShortcut(args.intent, args.container, args.screen, args.cellX, args.cellY);
                result = true;
                break;
            case 5:
                completeAddAppWidget(args.intent.getIntExtra("appWidgetId", -1), args.container, args.screen, (AppWidgetHostView) null, (AppWidgetProviderInfo) null);
                result = true;
                break;
            case 6:
                completeAddApplication(args.intent, args.container, args.screen, args.cellX, args.cellY);
                break;
            case 7:
                processShortcut(args.intent);
                break;
        }
        resetAddInfo();
        return result;
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean isWidgetDrop;
        int appWidgetId;
        boolean z = true;
        if (requestCode == 11) {
            int appWidgetId2 = data != null ? data.getIntExtra("appWidgetId", -1) : -1;
            if (resultCode == 0) {
                completeTwoStageWidgetDrop(0, appWidgetId2);
            } else if (resultCode == -1) {
                addAppWidgetImpl(appWidgetId2, this.mPendingAddInfo, (AppWidgetHostView) null, this.mPendingAddWidgetInfo);
            }
        } else {
            boolean delayExitSpringLoadedMode = false;
            if (requestCode == 9 || requestCode == 5) {
                isWidgetDrop = true;
            } else {
                isWidgetDrop = false;
            }
            this.mWaitingForResult = false;
            if (isWidgetDrop) {
                if (data != null) {
                    appWidgetId = data.getIntExtra("appWidgetId", -1);
                } else {
                    appWidgetId = -1;
                }
                if (appWidgetId < 0) {
                    Log.e(TAG, "Error: appWidgetId (EXTRA_APPWIDGET_ID) was not returned from the \\widget configuration activity.");
                    completeTwoStageWidgetDrop(0, appWidgetId);
                    return;
                }
                completeTwoStageWidgetDrop(resultCode, appWidgetId);
                return;
            }
            if (resultCode == -1 && this.mPendingAddInfo.container != -1) {
                PendingAddArguments args = new PendingAddArguments((PendingAddArguments) null);
                args.requestCode = requestCode;
                args.intent = data;
                args.container = this.mPendingAddInfo.container;
                args.screen = this.mPendingAddInfo.screen;
                args.cellX = this.mPendingAddInfo.cellX;
                args.cellY = this.mPendingAddInfo.cellY;
                if (isWorkspaceLocked()) {
                    sPendingAddList.add(args);
                } else {
                    delayExitSpringLoadedMode = completeAdd(args);
                }
            }
            this.mDragLayer.clearAnimatedView();
            if (resultCode == 0) {
                z = false;
            }
            exitSpringLoadedDragModeDelayed(z, delayExitSpringLoadedMode, (Runnable) null);
        }
    }

    private void completeTwoStageWidgetDrop(final int resultCode, final int appWidgetId) {
        CellLayout cellLayout = (CellLayout) this.mWorkspace.getChildAt(this.mPendingAddInfo.screen);
        Runnable onCompleteRunnable = null;
        int animationType = 0;
        AppWidgetHostView boundWidget = null;
        if (resultCode == -1) {
            animationType = 3;
            final AppWidgetHostView layout = this.mAppWidgetHost.createView(this, appWidgetId, this.mPendingAddWidgetInfo);
            boundWidget = layout;
            onCompleteRunnable = new Runnable() {
                public void run() {
                    boolean z;
                    Launcher.this.completeAddAppWidget(appWidgetId, Launcher.this.mPendingAddInfo.container, Launcher.this.mPendingAddInfo.screen, layout, (AppWidgetProviderInfo) null);
                    Launcher launcher = Launcher.this;
                    if (resultCode != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    launcher.exitSpringLoadedDragModeDelayed(z, false, (Runnable) null);
                }
            };
        } else if (resultCode == 0) {
            animationType = 4;
            onCompleteRunnable = new Runnable() {
                public void run() {
                    boolean z;
                    Launcher launcher = Launcher.this;
                    if (resultCode != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    launcher.exitSpringLoadedDragModeDelayed(z, false, (Runnable) null);
                }
            };
        }
        if (this.mDragLayer.getAnimatedView() != null) {
            this.mWorkspace.animateWidgetDrop(this.mPendingAddInfo, cellLayout, (DragView) this.mDragLayer.getAnimatedView(), onCompleteRunnable, animationType, boundWidget, true);
        } else {
            onCompleteRunnable.run();
        }
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        boolean z;
        super.onResume();
        if (LauncherProvider.isBoot == 1) {
            LauncherProvider.isBoot = 0;
        }
        if (DefaultWorkspace.mOnResumeAllapp.booleanValue()) {
            this.mOnResumeState = State.APPS_CUSTOMIZE;
            DefaultWorkspace.mOnResumeAllapp = false;
        }
        Log.w("###", "onResume###" + this.mOnResumeState);
        if (this.mOnResumeState == State.WORKSPACE) {
            showWorkspace(false);
        } else if (this.mOnResumeState == State.APPS_CUSTOMIZE) {
            showAllApps(false);
        }
        this.mOnResumeState = State.NONE;
        if (this.mState == State.WORKSPACE) {
            z = true;
        } else {
            z = false;
        }
        setWorkspaceBackground(z);
        InstallShortcutReceiver.flushInstallQueue(this);
        this.mPaused = false;
        sPausedFromUserAction = false;
        if (this.mRestoring || this.mOnResumeNeedsLoad) {
            this.mWorkspaceLoading = true;
            this.mModel.startLoader(true, -1);
            this.mRestoring = false;
            this.mOnResumeNeedsLoad = false;
        }
        if (this.mWaitingForResume != null) {
            this.mWaitingForResume.setStayPressed(false);
        }
        if (this.mAppsCustomizeContent != null) {
            this.mAppsCustomizeContent.resetDrawableState();
        }
        getWorkspace().reinflateWidgetsIfNecessary();
        updateGlobalIcons();
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        updateWallpaperVisibility(true);
        super.onPause();
        this.mPaused = true;
        this.mDragController.cancelDrag();
        this.mDragController.resetLastGestureUpTime();
    }

    public Object onRetainNonConfigurationInstance() {
        this.mModel.stopLoader();
        if (this.mAppsCustomizeContent != null) {
            this.mAppsCustomizeContent.surrender();
        }
        return Boolean.TRUE;
    }

    private boolean acceptFilter() {
        return !((InputMethodManager) getSystemService("input_method")).isFullscreenMode();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int uniChar = event.getUnicodeChar();
        boolean handled = super.onKeyDown(keyCode, event);
        boolean isKeyNotWhitespace = uniChar > 0 && !Character.isWhitespace(uniChar);
        if (!handled && acceptFilter() && isKeyNotWhitespace && TextKeyListener.getInstance().onKeyDown(this.mWorkspace, this.mDefaultKeySsb, keyCode, event) && this.mDefaultKeySsb != null && this.mDefaultKeySsb.length() > 0) {
            return onSearchRequested();
        }
        if (keyCode != 82 || !event.isLongPress()) {
            return handled;
        }
        return true;
    }

    private String getTypedText() {
        return this.mDefaultKeySsb.toString();
    }

    private void clearTypedText() {
        this.mDefaultKeySsb.clear();
        this.mDefaultKeySsb.clearSpans();
        Selection.setSelection(this.mDefaultKeySsb, 0);
    }

    private static State intToState(int stateOrdinal) {
        State state = State.WORKSPACE;
        State[] stateValues = State.values();
        for (int i = 0; i < stateValues.length; i++) {
            if (stateValues[i].ordinal() == stateOrdinal) {
                return stateValues[i];
            }
        }
        return state;
    }

    private void restoreState(Bundle savedState) {
        if (DefaultWorkspace.mOnResumeAllapp.booleanValue()) {
            this.mOnResumeState = State.APPS_CUSTOMIZE;
            DefaultWorkspace.mOnResumeAllapp = false;
        }
        Log.w("###", "restoreState###" + this.mOnResumeState);
        if (savedState != null) {
            if (intToState(savedState.getInt(RUNTIME_STATE, State.WORKSPACE.ordinal())) == State.APPS_CUSTOMIZE) {
                this.mOnResumeState = State.APPS_CUSTOMIZE;
            }
            int currentScreen = savedState.getInt(RUNTIME_STATE_CURRENT_SCREEN, -1);
            if (currentScreen > -1) {
                this.mWorkspace.setCurrentPage(currentScreen);
            }
            long pendingAddContainer = savedState.getLong(RUNTIME_STATE_PENDING_ADD_CONTAINER, -1);
            int pendingAddScreen = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SCREEN, -1);
            if (pendingAddContainer != -1 && pendingAddScreen > -1) {
                this.mPendingAddInfo.container = pendingAddContainer;
                this.mPendingAddInfo.screen = pendingAddScreen;
                this.mPendingAddInfo.cellX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
                this.mPendingAddInfo.cellY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
                this.mPendingAddInfo.spanX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
                this.mPendingAddInfo.spanY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
                this.mPendingAddWidgetInfo = (AppWidgetProviderInfo) savedState.getParcelable(RUNTIME_STATE_PENDING_ADD_WIDGET_INFO);
                this.mWaitingForResult = true;
                this.mRestoring = true;
            }
            if (savedState.getBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, false)) {
                this.mFolderInfo = this.mModel.getFolderById(this, sFolders, savedState.getLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID));
                this.mRestoring = true;
            }
            if (this.mAppsCustomizeTabHost != null) {
                String curTab = savedState.getString("apps_customize_currentTab");
                if (curTab != null) {
                    this.mAppsCustomizeTabHost.setContentTypeImmediate(this.mAppsCustomizeTabHost.getContentTypeForTabTag(curTab));
                    this.mAppsCustomizeContent.loadAssociatedPages(this.mAppsCustomizeContent.getCurrentPage());
                }
                this.mAppsCustomizeContent.restorePageForIndex(savedState.getInt("apps_customize_currentIndex"));
            }
        }
    }

    private void setupViews() {
        DragController dragController = this.mDragController;
        this.mLauncherView = findViewById(R.id.launcher);
        this.mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        this.mWorkspace = (Workspace) this.mDragLayer.findViewById(R.id.workspace);
        this.mLauncherView.setSystemUiVisibility(1024);
        this.mWorkspaceBackgroundDrawable = getResources().getDrawable(R.drawable.workspace_bg);
        this.mBlackBackgroundDrawable = new ColorDrawable(ViewCompat.MEASURED_STATE_MASK);
        this.mDragLayer.setup(this, dragController);
        this.mHotseat = (Hotseat) findViewById(R.id.hotseat);
        if (this.mHotseat != null) {
            this.mHotseat.setup(this);
        }
        MyWorkspace.GetInstance().setupViews(this);
        this.mWorkspace.setHapticFeedbackEnabled(false);
        this.mWorkspace.setOnLongClickListener(this);
        this.mWorkspace.setup(dragController);
        dragController.addDragListener(this.mWorkspace);
        this.mSearchDropTargetBar = (SearchDropTargetBar) this.mDragLayer.findViewById(R.id.qsb_bar);
        this.mAppsCustomizeTabHost = (AppsCustomizeTabHost) findViewById(R.id.apps_customize_pane);
        this.mAppsCustomizeContent = (AppsCustomizePagedView) this.mAppsCustomizeTabHost.findViewById(R.id.apps_customize_pane_content);
        this.mAppsCustomizeContent.setup(this, dragController);
        dragController.setDragScoller(this.mWorkspace);
        dragController.setScrollView(this.mDragLayer);
        dragController.setMoveTarget(this.mWorkspace);
        dragController.addDropTarget(this.mWorkspace);
        if (this.mSearchDropTargetBar != null) {
            this.mSearchDropTargetBar.setup(this, dragController);
        }
    }

    /* access modifiers changed from: package-private */
    public View createShortcut(ShortcutInfo info) {
        return createShortcut(R.layout.application, (ViewGroup) this.mWorkspace.getChildAt(this.mWorkspace.getCurrentPage()), info);
    }

    /* access modifiers changed from: package-private */
    public View createShortcut(int layoutResId, ViewGroup parent, ShortcutInfo info) {
        BubbleTextView favorite = (BubbleTextView) this.mInflater.inflate(layoutResId, parent, false);
        favorite.applyFromShortcutInfo(info, this.mIconCache);
        favorite.setOnClickListener(this);
        return favorite;
    }

    /* access modifiers changed from: package-private */
    public void completeAddApplication(Intent data, long container, int screen, int cellX, int cellY) {
        int[] cellXY = this.mTmpAddItemCellCoordinates;
        CellLayout layout = getCellLayout(container, screen);
        if (cellX >= 0 && cellY >= 0) {
            cellXY[0] = cellX;
            cellXY[1] = cellY;
        } else if (!layout.findCellForSpan(cellXY, 1, 1)) {
            showOutOfSpaceMessage(isHotseatLayout(layout));
            return;
        }
        ShortcutInfo info = this.mModel.getShortcutInfo(getPackageManager(), data, this);
        if (info != null) {
            info.setActivity(data.getComponent(), 270532608);
            info.container = -1;
            this.mWorkspace.addApplicationShortcut(info, layout, container, screen, cellXY[0], cellXY[1], isWorkspaceLocked(), cellX, cellY);
            return;
        }
        Log.e(TAG, "Couldn't find ActivityInfo for selected application: " + data);
    }

    private void completeAddShortcut(Intent data, long container, int screen, int cellX, int cellY) {
        boolean foundCellSpan;
        int[] cellXY = this.mTmpAddItemCellCoordinates;
        int[] touchXY = this.mPendingAddInfo.dropPos;
        CellLayout layout = getCellLayout(container, screen);
        ShortcutInfo info = this.mModel.infoFromShortcutIntent(this, data, (Bitmap) null);
        if (info != null) {
            View view = createShortcut(info);
            if (cellX >= 0 && cellY >= 0) {
                cellXY[0] = cellX;
                cellXY[1] = cellY;
                foundCellSpan = true;
                if (!this.mWorkspace.createUserFolderIfNecessary(view, container, layout, cellXY, 0.0f, true, (DragView) null, (Runnable) null)) {
                    DropTarget.DragObject dragObject = new DropTarget.DragObject();
                    dragObject.dragInfo = info;
                    if (this.mWorkspace.addToExistingFolderIfNecessary(view, layout, cellXY, 0.0f, dragObject, true)) {
                        return;
                    }
                } else {
                    return;
                }
            } else if (touchXY != null) {
                foundCellSpan = layout.findNearestVacantArea(touchXY[0], touchXY[1], 1, 1, cellXY) != null;
            } else {
                foundCellSpan = layout.findCellForSpan(cellXY, 1, 1);
            }
            if (!foundCellSpan) {
                showOutOfSpaceMessage(isHotseatLayout(layout));
                return;
            }
            LauncherModel.addItemToDatabase(this, info, container, screen, cellXY[0], cellXY[1], false);
            if (!this.mRestoring) {
                this.mWorkspace.addInScreen(view, container, screen, cellXY[0], cellXY[1], 1, 1, isWorkspaceLocked());
            }
        }
    }

    static int[] getSpanForWidget(Context context, ComponentName component, int minWidth, int minHeight) {
        Rect padding = AppWidgetHostView.getDefaultPaddingForWidget(context, component, (Rect) null);
        if (context.getResources().getInteger(R.integer.myiconback) != 0) {
            return CellLayout.rectToCell(context.getResources(), minWidth, minHeight, (int[]) null);
        }
        return CellLayout.rectToCell(context.getResources(), padding.left + minWidth + padding.right, padding.top + minHeight + padding.bottom, (int[]) null);
    }

    static int[] getSpanForWidget(Context context, AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minWidth, info.minHeight);
    }

    static int[] getMinSpanForWidget(Context context, AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minResizeWidth, info.minResizeHeight);
    }

    static int[] getSpanForWidget(Context context, PendingAddWidgetInfo info) {
        return getSpanForWidget(context, info.componentName, info.minWidth, info.minHeight);
    }

    static int[] getMinSpanForWidget(Context context, PendingAddWidgetInfo info) {
        return getSpanForWidget(context, info.componentName, info.minResizeWidth, info.minResizeHeight);
    }

    /* access modifiers changed from: private */
    public void completeAddAppWidget(int appWidgetId, long container, int screen, AppWidgetHostView hostView, AppWidgetProviderInfo appWidgetInfo) {
        boolean foundCellSpan;
        if (appWidgetInfo == null) {
            appWidgetInfo = this.mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        }
        CellLayout layout = getCellLayout(container, screen);
        int[] minSpanXY = getMinSpanForWidget((Context) this, appWidgetInfo);
        int[] spanXY = getSpanForWidget((Context) this, appWidgetInfo);
        int[] cellXY = this.mTmpAddItemCellCoordinates;
        int[] touchXY = this.mPendingAddInfo.dropPos;
        int[] finalSpan = new int[2];
        if (this.mPendingAddInfo.cellX >= 0 && this.mPendingAddInfo.cellY >= 0) {
            cellXY[0] = this.mPendingAddInfo.cellX;
            cellXY[1] = this.mPendingAddInfo.cellY;
            spanXY[0] = this.mPendingAddInfo.spanX;
            spanXY[1] = this.mPendingAddInfo.spanY;
            foundCellSpan = true;
        } else if (touchXY != null) {
            int[] result = layout.findNearestVacantArea(touchXY[0], touchXY[1], minSpanXY[0], minSpanXY[1], spanXY[0], spanXY[1], cellXY, finalSpan);
            spanXY[0] = finalSpan[0];
            spanXY[1] = finalSpan[1];
            foundCellSpan = result != null;
        } else {
            foundCellSpan = layout.findCellForSpan(cellXY, minSpanXY[0], minSpanXY[1]);
        }
        if (!foundCellSpan) {
            if (appWidgetId != -1) {
                final int i = appWidgetId;
                new Thread("deleteAppWidgetId") {
                    public void run() {
                        Launcher.this.mAppWidgetHost.deleteAppWidgetId(i);
                    }
                }.start();
            }
            showOutOfSpaceMessage(isHotseatLayout(layout));
            return;
        }
        LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(appWidgetId, appWidgetInfo.provider);
        launcherInfo.spanX = spanXY[0];
        launcherInfo.spanY = spanXY[1];
        launcherInfo.minSpanX = this.mPendingAddInfo.minSpanX;
        launcherInfo.minSpanY = this.mPendingAddInfo.minSpanY;
        LauncherModel.addItemToDatabase(this, launcherInfo, container, screen, cellXY[0], cellXY[1], false);
        if (!this.mRestoring) {
            if (hostView == null) {
                launcherInfo.hostView = this.mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
                launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
            } else {
                launcherInfo.hostView = hostView;
            }
            launcherInfo.hostView.setTag(launcherInfo);
            launcherInfo.hostView.setVisibility(0);
            launcherInfo.notifyWidgetSizeChanged(this);
            this.mWorkspace.addInScreen(launcherInfo.hostView, container, screen, cellXY[0], cellXY[1], launcherInfo.spanX, launcherInfo.spanY, isWorkspaceLocked());
            addWidgetToAutoAdvanceIfNeeded(launcherInfo.hostView, appWidgetInfo);
        }
        resetAddInfo();
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.USER_PRESENT");
        registerReceiver(this.mReceiver, filter);
        this.mAttached = true;
        this.mVisible = true;
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mVisible = false;
        if (this.mAttached) {
            unregisterReceiver(this.mReceiver);
            this.mAttached = false;
        }
        updateRunning();
    }

    public void onWindowVisibilityChanged(int visibility) {
        this.mVisible = visibility == 0;
        updateRunning();
        if (this.mVisible) {
            this.mAppsCustomizeTabHost.onWindowVisible();
            if (!this.mWorkspaceLoading) {
                final ViewTreeObserver observer = this.mWorkspace.getViewTreeObserver();
                observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        Launcher.this.mWorkspace.postDelayed(Launcher.this.mBuildLayersRunnable, 500);
                        observer.removeOnPreDrawListener(this);
                        return true;
                    }
                });
            }
            updateAppMarketIcon();
            clearTypedText();
        }
    }

    /* access modifiers changed from: private */
    public void sendAdvanceMessage(long delay) {
        this.mHandler.removeMessages(1);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), delay);
        this.mAutoAdvanceSentTime = System.currentTimeMillis();
    }

    /* access modifiers changed from: private */
    public void updateRunning() {
        boolean autoAdvanceRunning;
        long delay = 20000;
        if (!this.mVisible || !this.mUserPresent || this.mWidgetsToAdvance.isEmpty()) {
            autoAdvanceRunning = false;
        } else {
            autoAdvanceRunning = true;
        }
        if (autoAdvanceRunning != this.mAutoAdvanceRunning) {
            this.mAutoAdvanceRunning = autoAdvanceRunning;
            if (autoAdvanceRunning) {
                if (this.mAutoAdvanceTimeLeft != -1) {
                    delay = this.mAutoAdvanceTimeLeft;
                }
                sendAdvanceMessage(delay);
                return;
            }
            if (!this.mWidgetsToAdvance.isEmpty()) {
                this.mAutoAdvanceTimeLeft = Math.max(0, 20000 - (System.currentTimeMillis() - this.mAutoAdvanceSentTime));
            }
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(0);
        }
    }

    /* access modifiers changed from: package-private */
    public void addWidgetToAutoAdvanceIfNeeded(View hostView, AppWidgetProviderInfo appWidgetInfo) {
        if (appWidgetInfo != null && appWidgetInfo.autoAdvanceViewId != -1) {
            View v = hostView.findViewById(appWidgetInfo.autoAdvanceViewId);
            if (v instanceof Advanceable) {
                this.mWidgetsToAdvance.put(hostView, appWidgetInfo);
                ((Advanceable) v).fyiWillBeAdvancedByHostKThx();
                updateRunning();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeWidgetToAutoAdvance(View hostView) {
        if (this.mWidgetsToAdvance.containsKey(hostView)) {
            this.mWidgetsToAdvance.remove(hostView);
            updateRunning();
        }
    }

    public void removeAppWidget(LauncherAppWidgetInfo launcherInfo) {
        removeWidgetToAutoAdvance(launcherInfo.hostView);
        launcherInfo.hostView = null;
    }

    /* access modifiers changed from: package-private */
    public void showOutOfSpaceMessage(boolean isHotseatLayout) {
        Toast.makeText(this, getString(isHotseatLayout ? R.string.hotseat_out_of_space : R.string.out_of_space), 0).show();
    }

    public LauncherAppWidgetHost getAppWidgetHost() {
        return this.mAppWidgetHost;
    }

    public LauncherModel getModel() {
        return this.mModel;
    }

    /* access modifiers changed from: package-private */
    public void closeSystemDialogs() {
        getWindow().closeAllPanels();
        this.mWaitingForResult = false;
    }

    /* access modifiers changed from: protected */
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if ("android.intent.action.MAIN".equals(intent.getAction())) {
            closeSystemDialogs();
            final boolean alreadyOnHome = (intent.getFlags() & 4194304) != 4194304;
            Runnable processIntent = new Runnable() {
                public void run() {
                    if (Launcher.this.mWorkspace != null) {
                        Folder openFolder = Launcher.this.mWorkspace.getOpenFolder();
                        Launcher.this.mWorkspace.exitWidgetResizeMode();
                        if (alreadyOnHome && Launcher.this.mState == State.WORKSPACE && !Launcher.this.mWorkspace.isTouchActive() && openFolder == null) {
                            Launcher.this.mWorkspace.moveToDefaultScreen(true);
                        }
                        Launcher.this.closeFolder();
                        Launcher.this.exitSpringLoadedDragMode();
                        if (alreadyOnHome) {
                            if (DefaultWorkspace.mOnResumeAllapp.booleanValue()) {
                                Launcher.this.showAllApps(true);
                            } else {
                                Launcher.this.showWorkspace(true);
                            }
                        } else if (DefaultWorkspace.mOnResumeAllapp.booleanValue()) {
                            Launcher.this.mOnResumeState = State.APPS_CUSTOMIZE;
                            DefaultWorkspace.mOnResumeAllapp = false;
                        } else {
                            Launcher.this.mOnResumeState = State.WORKSPACE;
                        }
                        View v = Launcher.this.getWindow().peekDecorView();
                        if (!(v == null || v.getWindowToken() == null)) {
                            ((InputMethodManager) Launcher.this.getSystemService("input_method")).hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                        if (!alreadyOnHome && Launcher.this.mAppsCustomizeTabHost != null) {
                            Launcher.this.mAppsCustomizeTabHost.reset();
                        }
                    }
                }
            };
            if (!alreadyOnHome || this.mWorkspace.hasWindowFocus()) {
                processIntent.run();
            } else {
                this.mWorkspace.postDelayed(processIntent, 350);
            }
        }
    }

    public void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        Iterator<Integer> it = this.mSynchronouslyBoundPages.iterator();
        while (it.hasNext()) {
            this.mWorkspace.restoreInstanceStateForChild(it.next().intValue());
        }
    }

    /* access modifiers changed from: protected */
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(RUNTIME_STATE_CURRENT_SCREEN, this.mWorkspace.getNextPage());
        super.onSaveInstanceState(outState);
        outState.putInt(RUNTIME_STATE, this.mState.ordinal());
        closeFolder();
        if (this.mPendingAddInfo.container != -1 && this.mPendingAddInfo.screen > -1 && this.mWaitingForResult) {
            outState.putLong(RUNTIME_STATE_PENDING_ADD_CONTAINER, this.mPendingAddInfo.container);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SCREEN, this.mPendingAddInfo.screen);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_X, this.mPendingAddInfo.cellX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_Y, this.mPendingAddInfo.cellY);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_X, this.mPendingAddInfo.spanX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y, this.mPendingAddInfo.spanY);
            outState.putParcelable(RUNTIME_STATE_PENDING_ADD_WIDGET_INFO, this.mPendingAddWidgetInfo);
        }
        if (this.mFolderInfo != null && this.mWaitingForResult) {
            outState.putBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, true);
            outState.putLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID, this.mFolderInfo.id);
        }
        if (this.mAppsCustomizeTabHost != null) {
            String currentTabTag = this.mAppsCustomizeTabHost.getCurrentTabTag();
            if (currentTabTag != null) {
                outState.putString("apps_customize_currentTab", currentTabTag);
            }
            outState.putInt("apps_customize_currentIndex", this.mAppsCustomizeContent.getSaveInstanceStateIndex());
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(0);
        this.mWorkspace.removeCallbacks(this.mBuildLayersRunnable);
        this.mModel.stopLoader();
        ((LauncherApplication) getApplication()).setLauncher((Launcher) null);
        try {
            this.mAppWidgetHost.stopListening();
        } catch (NullPointerException ex) {
            Log.w(TAG, "problem while stopping AppWidgetHost during Launcher destruction", ex);
        }
        this.mAppWidgetHost = null;
        this.mWidgetsToAdvance.clear();
        TextKeyListener.getInstance().release();
        if (this.mModel != null) {
            this.mModel.unbindItemInfosAndClearQueuedBindRunnables();
        }
        getContentResolver().unregisterContentObserver(this.mWidgetObserver);
        unregisterReceiver(this.mCloseSystemDialogsReceiver);
        this.mDragLayer.clearAllResizeFrames();
        ((ViewGroup) this.mWorkspace.getParent()).removeAllViews();
        this.mWorkspace.removeAllViews();
        this.mWorkspace = null;
        this.mDragController = null;
        LauncherAnimUtils.onDestroyActivity();
    }

    public DragController getDragController() {
        return this.mDragController;
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode >= 0) {
            this.mWaitingForResult = true;
        }
        super.startActivityForResult(intent, requestCode);
    }

    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData, boolean globalSearch) {
        showWorkspace(true);
        if (initialQuery == null) {
            initialQuery = getTypedText();
        }
        if (appSearchData == null) {
            appSearchData = new Bundle();
            appSearchData.putString("source", "launcher-search");
        }
        Rect sourceBounds = new Rect();
        if (this.mSearchDropTargetBar != null) {
            sourceBounds = this.mSearchDropTargetBar.getSearchBarBounds();
        }
        startGlobalSearch(initialQuery, selectInitialQuery, appSearchData, sourceBounds);
    }

    public void startGlobalSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData, Rect sourceBounds) {
        Bundle appSearchData2;
        ComponentName globalSearchActivity = ((SearchManager) getSystemService("search")).getGlobalSearchActivity();
        if (globalSearchActivity == null) {
            Log.w(TAG, "No global search activity found.");
            return;
        }
        Intent intent = new Intent("android.search.action.GLOBAL_SEARCH");
        intent.addFlags(268435456);
        intent.setComponent(globalSearchActivity);
        if (appSearchData == null) {
            appSearchData2 = new Bundle();
        } else {
            appSearchData2 = new Bundle(appSearchData);
        }
        if (!appSearchData2.containsKey("source")) {
            appSearchData2.putString("source", getPackageName());
        }
        intent.putExtra("app_data", appSearchData2);
        if (!TextUtils.isEmpty(initialQuery)) {
            intent.putExtra("query", initialQuery);
        }
        if (selectInitialQuery) {
            intent.putExtra("select_query", selectInitialQuery);
        }
        intent.setSourceBounds(sourceBounds);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Global search activity not found: " + globalSearchActivity);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (isWorkspaceLocked()) {
            return false;
        }
        super.onCreateOptionsMenu(menu);
        Intent manageApps = new Intent("android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS");
        manageApps.setFlags(276824064);
        Intent settings = new Intent("android.settings.SETTINGS");
        settings.setFlags(270532608);
        String helpUrl = getString(R.string.help_url);
        Intent help = new Intent("android.intent.action.VIEW", Uri.parse(helpUrl));
        help.setFlags(276824064);
        menu.add(1, 2, 0, R.string.menu_wallpaper).setIcon(17301567).setAlphabeticShortcut('W');
        menu.add(0, 3, 0, R.string.menu_manage_apps).setIcon(17301570).setIntent(manageApps).setAlphabeticShortcut('M');
        menu.add(0, 4, 0, R.string.menu_settings).setIcon(17301577).setIntent(settings).setAlphabeticShortcut('P');
        if (!helpUrl.isEmpty()) {
            menu.add(0, 5, 0, R.string.menu_help).setIcon(17301568).setIntent(help).setAlphabeticShortcut('H');
        }
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean allAppsVisible;
        boolean z = false;
        super.onPrepareOptionsMenu(menu);
        if (this.mAppsCustomizeTabHost.isTransitioning()) {
            return false;
        }
        if (this.mAppsCustomizeTabHost.getVisibility() == 0) {
            allAppsVisible = true;
        } else {
            allAppsVisible = false;
        }
        if (!allAppsVisible) {
            z = true;
        }
        menu.setGroupVisible(1, z);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 2:
                startWallpaper();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onSearchRequested() {
        startSearch((String) null, false, (Bundle) null, true);
        return true;
    }

    public boolean isWorkspaceLocked() {
        return this.mWorkspaceLoading || this.mWaitingForResult;
    }

    private void resetAddInfo() {
        this.mPendingAddInfo.container = -1;
        this.mPendingAddInfo.screen = -1;
        ItemInfo itemInfo = this.mPendingAddInfo;
        this.mPendingAddInfo.cellY = -1;
        itemInfo.cellX = -1;
        ItemInfo itemInfo2 = this.mPendingAddInfo;
        this.mPendingAddInfo.spanY = -1;
        itemInfo2.spanX = -1;
        ItemInfo itemInfo3 = this.mPendingAddInfo;
        this.mPendingAddInfo.minSpanY = -1;
        itemInfo3.minSpanX = -1;
        this.mPendingAddInfo.dropPos = null;
    }

    /* access modifiers changed from: package-private */
    public void addAppWidgetImpl(int appWidgetId, ItemInfo info, AppWidgetHostView boundWidget, AppWidgetProviderInfo appWidgetInfo) {
        if (appWidgetInfo.configure != null) {
            this.mPendingAddWidgetInfo = appWidgetInfo;
            Intent intent = new Intent("android.appwidget.action.APPWIDGET_CONFIGURE");
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra("appWidgetId", appWidgetId);
            startActivityForResultSafely(intent, 5);
            return;
        }
        completeAddAppWidget(appWidgetId, info.container, info.screen, boundWidget, appWidgetInfo);
        exitSpringLoadedDragModeDelayed(true, false, (Runnable) null);
    }

    /* access modifiers changed from: package-private */
    public void processShortcutFromDrop(ComponentName componentName, long container, int screen, int[] cell, int[] loc) {
        resetAddInfo();
        this.mPendingAddInfo.container = container;
        this.mPendingAddInfo.screen = screen;
        this.mPendingAddInfo.dropPos = loc;
        if (cell != null) {
            this.mPendingAddInfo.cellX = cell[0];
            this.mPendingAddInfo.cellY = cell[1];
        }
        Intent createShortcutIntent = new Intent("android.intent.action.CREATE_SHORTCUT");
        createShortcutIntent.setComponent(componentName);
        processShortcut(createShortcutIntent);
    }

    /* access modifiers changed from: package-private */
    public void addAppWidgetFromDrop(PendingAddWidgetInfo info, long container, int screen, int[] cell, int[] span, int[] loc) {
        boolean success;
        resetAddInfo();
        ItemInfo itemInfo = this.mPendingAddInfo;
        info.container = container;
        itemInfo.container = container;
        ItemInfo itemInfo2 = this.mPendingAddInfo;
        info.screen = screen;
        itemInfo2.screen = screen;
        this.mPendingAddInfo.dropPos = loc;
        this.mPendingAddInfo.minSpanX = info.minSpanX;
        this.mPendingAddInfo.minSpanY = info.minSpanY;
        if (cell != null) {
            this.mPendingAddInfo.cellX = cell[0];
            this.mPendingAddInfo.cellY = cell[1];
        }
        if (span != null) {
            this.mPendingAddInfo.spanX = span[0];
            this.mPendingAddInfo.spanY = span[1];
        }
        AppWidgetHostView hostView = info.boundWidget;
        if (hostView != null) {
            addAppWidgetImpl(hostView.getAppWidgetId(), info, hostView, info.info);
            return;
        }
        int appWidgetId = getAppWidgetHost().allocateAppWidgetId();
        Bundle options = info.bindOptions;
        if (options != null) {
            success = this.mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.componentName, options);
        } else {
            success = this.mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.componentName);
        }
        if (success) {
            addAppWidgetImpl(appWidgetId, info, (AppWidgetHostView) null, info.info);
            return;
        }
        this.mPendingAddWidgetInfo = info.info;
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_BIND");
        intent.putExtra("appWidgetId", appWidgetId);
        intent.putExtra("appWidgetProvider", info.componentName);
        startActivityForResult(intent, 11);
    }

    /* access modifiers changed from: package-private */
    public void processShortcut(Intent intent) {
        String applicationName = getResources().getString(R.string.group_applications);
        String shortcutName = intent.getStringExtra("android.intent.extra.shortcut.NAME");
        if (applicationName == null || !applicationName.equals(shortcutName)) {
            startActivityForResultSafely(intent, 1);
            return;
        }
        Intent mainIntent = new Intent("android.intent.action.MAIN", (Uri) null);
        mainIntent.addCategory("android.intent.category.LAUNCHER");
        Intent pickIntent = new Intent("android.intent.action.PICK_ACTIVITY");
        pickIntent.putExtra("android.intent.extra.INTENT", mainIntent);
        pickIntent.putExtra("android.intent.extra.TITLE", getText(R.string.title_select_application));
        startActivityForResultSafely(pickIntent, 6);
    }

    /* access modifiers changed from: package-private */
    public void processWallpaper(Intent intent) {
        startActivityForResult(intent, 10);
    }

    /* access modifiers changed from: package-private */
    public FolderIcon addFolder(CellLayout layout, long container, int screen, int cellX, int cellY) {
        FolderInfo folderInfo = new FolderInfo();
        folderInfo.title = getText(R.string.folder_name);
        LauncherModel.addItemToDatabase(this, folderInfo, container, screen, cellX, cellY, false);
        sFolders.put(Long.valueOf(folderInfo.id), folderInfo);
        FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, this, layout, folderInfo, this.mIconCache);
        this.mWorkspace.addInScreen(newFolder, container, screen, cellX, cellY, 1, 1, isWorkspaceLocked());
        return newFolder;
    }

    /* access modifiers changed from: package-private */
    public void removeFolder(FolderInfo folder) {
        sFolders.remove(Long.valueOf(folder.id));
    }

    private void startWallpaper() {
        showWorkspace(true);
        startActivityForResult(Intent.createChooser(new Intent("android.intent.action.SET_WALLPAPER"), getText(R.string.chooser_wallpaper)), 10);
    }

    private void registerContentObservers() {
        getContentResolver().registerContentObserver(LauncherProvider.CONTENT_APPWIDGET_RESET_URI, true, this.mWidgetObserver);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == 0) {
            switch (event.getKeyCode()) {
                case 3:
                    return true;
                case 25:
                    if (isPropertyEnabled(DUMP_STATE_PROPERTY)) {
                        dumpState();
                        return true;
                    }
                    break;
            }
        } else if (event.getAction() == 1) {
            switch (event.getKeyCode()) {
                case 3:
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void onBackPressed() {
        if (isAllAppsVisible()) {
            showWorkspace(true);
        } else if (this.mWorkspace.getOpenFolder() != null) {
            Folder openFolder = this.mWorkspace.getOpenFolder();
            if (openFolder.isEditingName()) {
                openFolder.dismissEditingName();
            } else {
                closeFolder();
            }
        } else {
            this.mWorkspace.exitWidgetResizeMode();
            this.mWorkspace.showOutlinesTemporarily();
        }
    }

    /* access modifiers changed from: private */
    public void onAppWidgetReset() {
        if (this.mAppWidgetHost != null) {
            this.mAppWidgetHost.startListening();
        }
    }

    public void onClick(View v) {
        if (v.getWindowToken() != null && this.mWorkspace.isFinishedSwitchingState()) {
            Object tag = v.getTag();
            if (tag instanceof ShortcutInfo) {
                Intent intent = ((ShortcutInfo) tag).intent;
                int[] pos = new int[2];
                v.getLocationOnScreen(pos);
                intent.setSourceBounds(new Rect(pos[0], pos[1], pos[0] + v.getWidth(), pos[1] + v.getHeight()));
                if (startActivitySafely(v, intent, tag) && (v instanceof BubbleTextView)) {
                    this.mWaitingForResume = (BubbleTextView) v;
                    this.mWaitingForResume.setStayPressed(true);
                }
            } else if (tag instanceof FolderInfo) {
                if (v instanceof FolderIcon) {
                    handleFolderClick((FolderIcon) v);
                }
            } else if (v != this.mAllAppsButton) {
            } else {
                if (isAllAppsVisible()) {
                    showWorkspace(true);
                } else {
                    onClickAllAppsButton(v);
                }
            }
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        showWorkspace(true);
        return false;
    }

    public void onClickSearchButton(View v) {
        v.performHapticFeedback(1);
        onSearchRequested();
    }

    public void onClickVoiceButton(View v) {
        v.performHapticFeedback(1);
        try {
            ComponentName activityName = ((SearchManager) getSystemService("search")).getGlobalSearchActivity();
            Intent intent = new Intent("android.speech.action.WEB_SEARCH");
            intent.setFlags(268435456);
            if (activityName != null) {
                intent.setPackage(activityName.getPackageName());
            }
            startActivity((View) null, intent, "onClickVoiceButton");
        } catch (ActivityNotFoundException e) {
            Intent intent2 = new Intent("android.speech.action.WEB_SEARCH");
            intent2.setFlags(268435456);
            startActivitySafely((View) null, intent2, "onClickVoiceButton");
        }
    }

    public void onClickAllAppsButton(View v) {
        showAllApps(true);
    }

    public void onTouchDownAllAppsButton(View v) {
        v.performHapticFeedback(1);
    }

    public void onClickAppMarketButton(View v) {
        if (this.mAppMarketIntent != null) {
            startActivitySafely(v, this.mAppMarketIntent, "app market");
        } else {
            Log.e(TAG, "Invalid app market intent.");
        }
    }

    /* access modifiers changed from: package-private */
    public void startApplicationDetailsActivity(ComponentName componentName) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.fromParts("package", componentName.getPackageName(), (String) null));
        intent.setFlags(276824064);
        startActivitySafely((View) null, intent, "startApplicationDetailsActivity");
    }

    /* access modifiers changed from: package-private */
    public void startApplicationUninstallActivity(ApplicationInfo appInfo) {
        if ((appInfo.flags & 1) == 0) {
            Toast.makeText(this, R.string.uninstall_system_app_text, 0).show();
            return;
        }
        Intent intent = new Intent("android.intent.action.DELETE", Uri.fromParts("package", appInfo.componentName.getPackageName(), appInfo.componentName.getClassName()));
        intent.setFlags(276824064);
        startActivity(intent);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x002d A[Catch:{ SecurityException -> 0x0031 }] */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0014 A[Catch:{ SecurityException -> 0x0031 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean startActivity(android.view.View r10, android.content.Intent r11, java.lang.Object r12) {
        /*
            r9 = this;
            r4 = 1
            r3 = 0
            r5 = 268435456(0x10000000, float:2.5243549E-29)
            r11.addFlags(r5)
            if (r10 == 0) goto L_0x002b
            java.lang.String r5 = "com.android.launcher.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION"
            boolean r5 = r11.hasExtra(r5)     // Catch:{ SecurityException -> 0x0031 }
            if (r5 != 0) goto L_0x002b
            r2 = r4
        L_0x0012:
            if (r2 == 0) goto L_0x002d
            r5 = 0
            r6 = 0
            int r7 = r10.getMeasuredWidth()     // Catch:{ SecurityException -> 0x0031 }
            int r8 = r10.getMeasuredHeight()     // Catch:{ SecurityException -> 0x0031 }
            android.app.ActivityOptions r1 = android.app.ActivityOptions.makeScaleUpAnimation(r10, r5, r6, r7, r8)     // Catch:{ SecurityException -> 0x0031 }
            android.os.Bundle r5 = r1.toBundle()     // Catch:{ SecurityException -> 0x0031 }
            r9.startActivity(r11, r5)     // Catch:{ SecurityException -> 0x0031 }
        L_0x0029:
            r3 = r4
        L_0x002a:
            return r3
        L_0x002b:
            r2 = r3
            goto L_0x0012
        L_0x002d:
            r9.startActivity(r11)     // Catch:{ SecurityException -> 0x0031 }
            goto L_0x0029
        L_0x0031:
            r0 = move-exception
            int r4 = com.android.launcher.R.string.activity_not_found
            android.widget.Toast r4 = android.widget.Toast.makeText(r9, r4, r3)
            r4.show()
            java.lang.String r4 = "Launcher"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            java.lang.String r6 = "Launcher does not have the permission to launch "
            r5.<init>(r6)
            java.lang.StringBuilder r5 = r5.append(r11)
            java.lang.String r6 = ". Make sure to create a MAIN intent-filter for the corresponding activity "
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.String r6 = "or use the exported attribute for this activity. "
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.String r6 = "tag="
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.StringBuilder r5 = r5.append(r12)
            java.lang.String r6 = " intent="
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.StringBuilder r5 = r5.append(r11)
            java.lang.String r5 = r5.toString()
            android.util.Log.e(r4, r5, r0)
            goto L_0x002a
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.launcher2.Launcher.startActivity(android.view.View, android.content.Intent, java.lang.Object):boolean");
    }

    /* access modifiers changed from: package-private */
    public boolean startActivitySafely(View v, Intent intent, Object tag) {
        try {
            return startActivity(v, intent, tag);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, 0).show();
            Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void startActivityForResultSafely(Intent intent, int requestCode) {
        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, 0).show();
        } catch (SecurityException e2) {
            Toast.makeText(this, R.string.activity_not_found, 0).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent + ". Make sure to create a MAIN intent-filter for the corresponding activity " + "or use the exported attribute for this activity.", e2);
        }
    }

    private void handleFolderClick(FolderIcon folderIcon) {
        FolderInfo info = folderIcon.getFolderInfo();
        Folder openFolder = this.mWorkspace.getFolderForTag(info);
        if (info.opened && openFolder == null) {
            Log.d(TAG, "Folder info marked as open, but associated folder is not open. Screen: " + info.screen + " (" + info.cellX + ", " + info.cellY + ")");
            info.opened = false;
        }
        if (!info.opened && !folderIcon.getFolder().isDestroyed()) {
            closeFolder();
            openFolder(folderIcon);
        } else if (openFolder != null) {
            int folderScreen = this.mWorkspace.getPageForView(openFolder);
            closeFolder(openFolder);
            if (folderScreen != this.mWorkspace.getCurrentPage()) {
                closeFolder();
                openFolder(folderIcon);
            }
        }
    }

    private void copyFolderIconToImage(FolderIcon fi) {
        DragLayer.LayoutParams lp;
        int width = fi.getMeasuredWidth();
        int height = fi.getMeasuredHeight();
        if (this.mFolderIconImageView == null) {
            this.mFolderIconImageView = new ImageView(this);
        }
        if (!(this.mFolderIconBitmap != null && this.mFolderIconBitmap.getWidth() == width && this.mFolderIconBitmap.getHeight() == height)) {
            this.mFolderIconBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            this.mFolderIconCanvas = new Canvas(this.mFolderIconBitmap);
        }
        if (this.mFolderIconImageView.getLayoutParams() instanceof DragLayer.LayoutParams) {
            lp = (DragLayer.LayoutParams) this.mFolderIconImageView.getLayoutParams();
        } else {
            lp = new DragLayer.LayoutParams(width, height);
        }
        float scale = this.mDragLayer.getDescendantRectRelativeToSelf(fi, this.mRectForFolderAnimation);
        lp.customPosition = true;
        lp.x = this.mRectForFolderAnimation.left;
        lp.y = this.mRectForFolderAnimation.top;
        lp.width = (int) (((float) width) * scale);
        lp.height = (int) (((float) height) * scale);
        this.mFolderIconCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        fi.draw(this.mFolderIconCanvas);
        this.mFolderIconImageView.setImageBitmap(this.mFolderIconBitmap);
        if (fi.getFolder() != null) {
            this.mFolderIconImageView.setPivotX(fi.getFolder().getPivotXForIconAnimation());
            this.mFolderIconImageView.setPivotY(fi.getFolder().getPivotYForIconAnimation());
        }
        if (this.mDragLayer.indexOfChild(this.mFolderIconImageView) != -1) {
            this.mDragLayer.removeView(this.mFolderIconImageView);
        }
        this.mDragLayer.addView(this.mFolderIconImageView, lp);
        if (fi.getFolder() != null) {
            fi.getFolder().bringToFront();
        }
    }

    private void growAndFadeOutFolderIcon(FolderIcon fi) {
        if (fi != null) {
            PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", new float[]{0.0f});
            PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", new float[]{1.5f});
            PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", new float[]{1.5f});
            if (((FolderInfo) fi.getTag()).container == -101) {
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) fi.getLayoutParams();
                ((CellLayout) fi.getParent().getParent()).setFolderLeaveBehindCell(lp.cellX, lp.cellY);
            }
            copyFolderIconToImage(fi);
            fi.setVisibility(4);
            ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(this.mFolderIconImageView, alpha, scaleX, scaleY);
            oa.setDuration((long) getResources().getInteger(R.integer.config_folderAnimDuration));
            oa.start();
        }
    }

    private void shrinkAndFadeInFolderIcon(final FolderIcon fi) {
        if (fi != null) {
            PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", new float[]{1.0f});
            PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", new float[]{1.0f});
            PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", new float[]{1.0f});
            final CellLayout cl = (CellLayout) fi.getParent().getParent();
            this.mDragLayer.removeView(this.mFolderIconImageView);
            copyFolderIconToImage(fi);
            ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(this.mFolderIconImageView, alpha, scaleX, scaleY);
            oa.setDuration((long) getResources().getInteger(R.integer.config_folderAnimDuration));
            oa.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    if (cl != null) {
                        cl.clearFolderLeaveBehind();
                        Launcher.this.mDragLayer.removeView(Launcher.this.mFolderIconImageView);
                        fi.setVisibility(0);
                    }
                }
            });
            oa.start();
        }
    }

    public void openFolder(FolderIcon folderIcon) {
        Folder folder = folderIcon.getFolder();
        folder.mInfo.opened = true;
        if (folder.getParent() == null) {
            this.mDragLayer.addView(folder);
            this.mDragController.addDropTarget(folder);
        } else {
            Log.w(TAG, "Opening folder (" + folder + ") which already has a parent (" + folder.getParent() + ").");
        }
        folder.animateOpen();
        growAndFadeOutFolderIcon(folderIcon);
    }

    public void closeFolder() {
        Folder folder = this.mWorkspace.getOpenFolder();
        if (folder != null) {
            if (folder.isEditingName()) {
                folder.dismissEditingName();
            }
            closeFolder(folder);
            dismissFolderCling((View) null);
        }
    }

    /* access modifiers changed from: package-private */
    public void closeFolder(Folder folder) {
        folder.getInfo().opened = false;
        if (((ViewGroup) folder.getParent().getParent()) != null) {
            shrinkAndFadeInFolderIcon((FolderIcon) this.mWorkspace.getViewForTag(folder.mInfo));
        }
        folder.animateClosed();
    }

    public boolean onLongClick(View v) {
        boolean allowLongPress;
        if (!isDraggingEnabled() || isWorkspaceLocked() || this.mState != State.WORKSPACE) {
            return false;
        }
        if (!(v instanceof CellLayout)) {
            v = (View) v.getParent().getParent();
        }
        resetAddInfo();
        CellLayout.CellInfo longClickCellInfo = (CellLayout.CellInfo) v.getTag();
        if (longClickCellInfo == null) {
            return true;
        }
        View itemUnderLongClick = longClickCellInfo.cell;
        if (isHotseatLayout(v) || this.mWorkspace.allowLongPress()) {
            allowLongPress = true;
        } else {
            allowLongPress = false;
        }
        if (allowLongPress && !this.mDragController.isDragging()) {
            if (itemUnderLongClick == null || (itemUnderLongClick instanceof FirstView)) {
                this.mWorkspace.performHapticFeedback(0, 1);
                startWallpaper();
            } else if (!(itemUnderLongClick instanceof Folder)) {
                this.mWorkspace.startDrag(longClickCellInfo);
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isHotseatLayout(View layout) {
        return this.mHotseat != null && layout != null && (layout instanceof CellLayout) && layout == this.mHotseat.getLayout();
    }

    /* access modifiers changed from: package-private */
    public Hotseat getHotseat() {
        return this.mHotseat;
    }

    /* access modifiers changed from: package-private */
    public SearchDropTargetBar getSearchBar() {
        return this.mSearchDropTargetBar;
    }

    /* access modifiers changed from: package-private */
    public CellLayout getCellLayout(long container, int screen) {
        if (container != -101) {
            return (CellLayout) this.mWorkspace.getChildAt(screen);
        }
        if (this.mHotseat != null) {
            return this.mHotseat.getLayout();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public Workspace getWorkspace() {
        return this.mWorkspace;
    }

    public boolean isAllAppsVisible() {
        return this.mState == State.APPS_CUSTOMIZE || this.mOnResumeState == State.APPS_CUSTOMIZE;
    }

    public boolean isAllAppsButtonRank(int rank) {
        return this.mHotseat.isAllAppsButtonRank(rank);
    }

    /* access modifiers changed from: private */
    public void setPivotsForZoom(View view, float scaleFactor) {
        view.setPivotX(((float) view.getWidth()) / 2.0f);
        view.setPivotY(((float) view.getHeight()) / 2.0f);
    }

    /* access modifiers changed from: package-private */
    public void disableWallpaperIfInAllApps() {
        if (isAllAppsVisible() && this.mAppsCustomizeTabHost != null && !this.mAppsCustomizeTabHost.isTransitioning()) {
            updateWallpaperVisibility(true);
        }
    }

    private void setWorkspaceBackground(boolean workspace) {
        this.mLauncherView.setBackground(this.mWorkspaceBackgroundDrawable);
    }

    /* access modifiers changed from: package-private */
    public void updateWallpaperVisibility(boolean visible) {
        int wpflags = visible ? 1048576 : 0;
        if (wpflags != (getWindow().getAttributes().flags & AccessibilityEventCompat.TYPE_TOUCH_INTERACTION_START)) {
            getWindow().setFlags(wpflags, AccessibilityEventCompat.TYPE_TOUCH_INTERACTION_START);
        }
        setWorkspaceBackground(visible);
    }

    private void dispatchOnLauncherTransitionPrepare(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionPrepare(this, animated, toWorkspace);
        }
    }

    /* access modifiers changed from: private */
    public void dispatchOnLauncherTransitionStart(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionStart(this, animated, toWorkspace);
        }
        dispatchOnLauncherTransitionStep(v, 0.0f);
    }

    /* access modifiers changed from: private */
    public void dispatchOnLauncherTransitionStep(View v, float t) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionStep(this, t);
        }
    }

    /* access modifiers changed from: private */
    public void dispatchOnLauncherTransitionEnd(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionEnd(this, animated, toWorkspace);
        }
        dispatchOnLauncherTransitionStep(v, 1.0f);
    }

    private void showAppsCustomizeHelper(boolean animated, boolean springLoaded) {
        ViewTreeObserver observer;
        if (this.mStateAnimation != null) {
            this.mStateAnimation.cancel();
            this.mStateAnimation = null;
        }
        Resources res = getResources();
        int duration = res.getInteger(R.integer.config_appsCustomizeZoomInTime);
        int fadeDuration = res.getInteger(R.integer.config_appsCustomizeFadeInTime);
        final float scale = (float) res.getInteger(R.integer.config_appsCustomizeZoomScaleFactor);
        final View fromView = this.mWorkspace;
        final AppsCustomizeTabHost toView = this.mAppsCustomizeTabHost;
        int startDelay = res.getInteger(R.integer.config_workspaceAppsCustomizeAnimationStagger);
        setPivotsForZoom(toView, scale);
        Animator workspaceAnim = this.mWorkspace.getChangeStateAnimation(Workspace.State.SMALL, animated);
        if (animated) {
            toView.setScaleX(scale);
            toView.setScaleY(scale);
            LauncherViewPropertyAnimator launcherViewPropertyAnimator = new LauncherViewPropertyAnimator(toView);
            launcherViewPropertyAnimator.scaleX(1.0f).scaleY(1.0f).setDuration((long) duration).setInterpolator(new Workspace.ZoomOutInterpolator());
            toView.setVisibility(0);
            toView.setAlpha(0.0f);
            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(toView, "alpha", new float[]{0.0f, 1.0f}).setDuration((long) fadeDuration);
            alphaAnim.setInterpolator(new DecelerateInterpolator(1.5f));
            alphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation == null) {
                        throw new RuntimeException("animation is null");
                    }
                    float t = ((Float) animation.getAnimatedValue()).floatValue();
                    Launcher.this.dispatchOnLauncherTransitionStep(fromView, t);
                    Launcher.this.dispatchOnLauncherTransitionStep(toView, t);
                    Launcher.this.hideHotseat(false);
                    if (Launcher.this.mSearchDropTargetBar != null) {
                        Launcher.this.mSearchDropTargetBar.hideSearchBar(false);
                    }
                }
            });
            this.mStateAnimation = LauncherAnimUtils.createAnimatorSet();
            this.mStateAnimation.play(launcherViewPropertyAnimator).after((long) startDelay);
            this.mStateAnimation.play(alphaAnim).after((long) startDelay);
            final boolean z = animated;
            final boolean z2 = springLoaded;
            this.mStateAnimation.addListener(new AnimatorListenerAdapter() {
                boolean animationCancelled = false;

                public void onAnimationStart(Animator animation) {
                    Launcher.this.updateWallpaperVisibility(true);
                    toView.setTranslationX(0.0f);
                    toView.setTranslationY(0.0f);
                    toView.setVisibility(0);
                    toView.bringToFront();
                }

                public void onAnimationEnd(Animator animation) {
                    Launcher.this.dispatchOnLauncherTransitionEnd(fromView, z, false);
                    Launcher.this.dispatchOnLauncherTransitionEnd(toView, z, false);
                    if (Launcher.this.mWorkspace != null && !z2 && !LauncherApplication.isScreenLarge()) {
                        Launcher.this.mWorkspace.hideScrollingIndicator(true);
                        Launcher.this.hideDockDivider();
                    }
                    if (!this.animationCancelled) {
                        Launcher.this.updateWallpaperVisibility(true);
                    }
                    if (Launcher.this.mSearchDropTargetBar != null) {
                        Launcher.this.mSearchDropTargetBar.hideSearchBar(false);
                    }
                }

                public void onAnimationCancel(Animator animation) {
                    this.animationCancelled = true;
                }
            });
            if (workspaceAnim != null) {
                this.mStateAnimation.play(workspaceAnim);
            }
            boolean delayAnim = false;
            dispatchOnLauncherTransitionPrepare(fromView, animated, false);
            dispatchOnLauncherTransitionPrepare(toView, animated, false);
            if (toView.getContent().getMeasuredWidth() == 0 || this.mWorkspace.getMeasuredWidth() == 0 || toView.getMeasuredWidth() == 0) {
                observer = this.mWorkspace.getViewTreeObserver();
                delayAnim = true;
            } else {
                observer = null;
            }
            final AnimatorSet stateAnimation = this.mStateAnimation;
            final AppsCustomizeTabHost appsCustomizeTabHost = toView;
            final View view = fromView;
            final boolean z3 = animated;
            final Runnable startAnimRunnable = new Runnable() {
                public void run() {
                    if (Launcher.this.mStateAnimation == stateAnimation) {
                        Launcher.this.setPivotsForZoom(appsCustomizeTabHost, scale);
                        Launcher.this.dispatchOnLauncherTransitionStart(view, z3, false);
                        Launcher.this.dispatchOnLauncherTransitionStart(appsCustomizeTabHost, z3, false);
                        AppsCustomizeTabHost appsCustomizeTabHost = appsCustomizeTabHost;
                        final AnimatorSet animatorSet = stateAnimation;
                        appsCustomizeTabHost.post(new Runnable() {
                            public void run() {
                                if (Launcher.this.mStateAnimation == animatorSet) {
                                    Launcher.this.mStateAnimation.start();
                                }
                            }
                        });
                    }
                }
            };
            if (delayAnim) {
                final ViewTreeObserver viewTreeObserver = observer;
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        toView.post(startAnimRunnable);
                        viewTreeObserver.removeOnGlobalLayoutListener(this);
                    }
                });
                return;
            }
            startAnimRunnable.run();
            return;
        }
        toView.setTranslationX(0.0f);
        toView.setTranslationY(0.0f);
        toView.setScaleX(1.0f);
        toView.setScaleY(1.0f);
        toView.setAlpha(1.0f);
        toView.setVisibility(0);
        toView.bringToFront();
        if (!springLoaded && !LauncherApplication.isScreenLarge()) {
            this.mWorkspace.hideScrollingIndicator(true);
            hideDockDivider();
            if (this.mSearchDropTargetBar != null) {
                this.mSearchDropTargetBar.hideSearchBar(false);
            }
        }
        dispatchOnLauncherTransitionPrepare(fromView, animated, false);
        dispatchOnLauncherTransitionStart(fromView, animated, false);
        dispatchOnLauncherTransitionEnd(fromView, animated, false);
        dispatchOnLauncherTransitionPrepare(toView, animated, false);
        dispatchOnLauncherTransitionStart(toView, animated, false);
        dispatchOnLauncherTransitionEnd(toView, animated, false);
        updateWallpaperVisibility(true);
    }

    private void hideAppsCustomizeHelper(State toState, boolean animated, boolean springLoaded, Runnable onCompleteRunnable) {
        sendBroadcast(new Intent("Allapp_backto_workspace"));
        if (this.mStateAnimation != null) {
            this.mStateAnimation.cancel();
            this.mStateAnimation = null;
        }
        Resources res = getResources();
        int duration = res.getInteger(R.integer.config_appsCustomizeZoomOutTime);
        int fadeOutDuration = res.getInteger(R.integer.config_appsCustomizeFadeOutTime);
        float scaleFactor = (float) res.getInteger(R.integer.config_appsCustomizeZoomScaleFactor);
        final View fromView = this.mAppsCustomizeTabHost;
        final View toView = this.mWorkspace;
        Animator workspaceAnim = null;
        if (toState == State.WORKSPACE) {
            workspaceAnim = this.mWorkspace.getChangeStateAnimation(Workspace.State.NORMAL, animated, res.getInteger(R.integer.config_appsCustomizeWorkspaceAnimationStagger));
        } else if (toState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
            workspaceAnim = this.mWorkspace.getChangeStateAnimation(Workspace.State.SPRING_LOADED, animated);
        }
        setPivotsForZoom(fromView, scaleFactor);
        updateWallpaperVisibility(true);
        showHotseat(animated);
        if (animated) {
            LauncherViewPropertyAnimator scaleAnim = new LauncherViewPropertyAnimator(fromView);
            scaleAnim.scaleX(scaleFactor).scaleY(scaleFactor).setDuration((long) duration).setInterpolator(new Workspace.ZoomInInterpolator());
            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(fromView, "alpha", new float[]{1.0f, 0.0f}).setDuration((long) fadeOutDuration);
            alphaAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            alphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    float t = 1.0f - ((Float) animation.getAnimatedValue()).floatValue();
                    Launcher.this.dispatchOnLauncherTransitionStep(fromView, t);
                    Launcher.this.dispatchOnLauncherTransitionStep(toView, t);
                }
            });
            this.mStateAnimation = LauncherAnimUtils.createAnimatorSet();
            dispatchOnLauncherTransitionPrepare(fromView, animated, true);
            dispatchOnLauncherTransitionPrepare(toView, animated, true);
            this.mAppsCustomizeContent.pauseScrolling();
            final boolean z = animated;
            final Runnable runnable = onCompleteRunnable;
            this.mStateAnimation.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    Launcher.this.updateWallpaperVisibility(true);
                    fromView.setVisibility(8);
                    Launcher.this.dispatchOnLauncherTransitionEnd(fromView, z, true);
                    Launcher.this.dispatchOnLauncherTransitionEnd(toView, z, true);
                    if (Launcher.this.mWorkspace != null) {
                        Launcher.this.mWorkspace.hideScrollingIndicator(false);
                    }
                    if (runnable != null) {
                        runnable.run();
                    }
                    Launcher.this.mAppsCustomizeContent.updateCurrentPageScroll();
                    Launcher.this.mAppsCustomizeContent.resumeScrolling();
                }
            });
            this.mStateAnimation.playTogether(new Animator[]{scaleAnim, alphaAnim});
            if (workspaceAnim != null) {
                this.mStateAnimation.play(workspaceAnim);
            }
            dispatchOnLauncherTransitionStart(fromView, animated, true);
            dispatchOnLauncherTransitionStart(toView, animated, true);
            final Animator animator = this.mStateAnimation;
            this.mWorkspace.post(new Runnable() {
                public void run() {
                    if (animator == Launcher.this.mStateAnimation) {
                        Launcher.this.mStateAnimation.start();
                    }
                }
            });
            return;
        }
        fromView.setVisibility(8);
        dispatchOnLauncherTransitionPrepare(fromView, animated, true);
        dispatchOnLauncherTransitionStart(fromView, animated, true);
        dispatchOnLauncherTransitionEnd(fromView, animated, true);
        dispatchOnLauncherTransitionPrepare(toView, animated, true);
        dispatchOnLauncherTransitionStart(toView, animated, true);
        dispatchOnLauncherTransitionEnd(toView, animated, true);
        this.mWorkspace.hideScrollingIndicator(false);
    }

    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= 60) {
            this.mAppsCustomizeTabHost.onTrimMemory();
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            updateWallpaperVisibility(true);
        } else {
            this.mWorkspace.postDelayed(new Runnable() {
                public void run() {
                    Launcher.this.disableWallpaperIfInAllApps();
                }
            }, 500);
        }
    }

    /* access modifiers changed from: package-private */
    public void showWorkspace(boolean animated) {
        showWorkspace(animated, (Runnable) null);
    }

    /* access modifiers changed from: package-private */
    public void showWorkspace(boolean animated, Runnable onCompleteRunnable) {
        boolean z = false;
        if (this.mState != State.WORKSPACE) {
            boolean wasInSpringLoadedMode = this.mState == State.APPS_CUSTOMIZE_SPRING_LOADED;
            this.mWorkspace.setVisibility(0);
            hideAppsCustomizeHelper(State.WORKSPACE, false, false, onCompleteRunnable);
            if (this.mSearchDropTargetBar != null) {
                this.mSearchDropTargetBar.showSearchBar(wasInSpringLoadedMode);
            }
            if (animated && wasInSpringLoadedMode) {
                z = true;
            }
            showDockDivider(z);
            if (this.mAllAppsButton != null) {
                this.mAllAppsButton.requestFocus();
            }
        }
        this.mWorkspace.flashScrollingIndicator(animated);
        this.mState = State.WORKSPACE;
        this.mUserPresent = true;
        updateRunning();
        getWindow().getDecorView().sendAccessibilityEvent(32);
    }

    /* access modifiers changed from: package-private */
    public void showAllApps(boolean animated) {
        if (this.mState == State.WORKSPACE) {
            showAppsCustomizeHelper(false, false);
            this.mAppsCustomizeTabHost.requestFocus();
            this.mAppsCustomizeContent.requestFocus();
            this.mState = State.APPS_CUSTOMIZE;
            this.mUserPresent = false;
            updateRunning();
            closeFolder();
            getWindow().getDecorView().sendAccessibilityEvent(32);
        }
    }

    /* access modifiers changed from: package-private */
    public void enterSpringLoadedDragMode() {
        if (isAllAppsVisible()) {
            hideAppsCustomizeHelper(State.APPS_CUSTOMIZE_SPRING_LOADED, true, true, (Runnable) null);
            hideDockDivider();
            this.mState = State.APPS_CUSTOMIZE_SPRING_LOADED;
        }
    }

    /* access modifiers changed from: package-private */
    public void exitSpringLoadedDragModeDelayed(final boolean successfulDrop, boolean extendedDelay, final Runnable onCompleteRunnable) {
        int i;
        if (this.mState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
            Handler handler = this.mHandler;
            AnonymousClass20 r2 = new Runnable() {
                public void run() {
                    if (successfulDrop) {
                        Launcher.this.mAppsCustomizeTabHost.setVisibility(8);
                        Launcher.this.showWorkspace(true, onCompleteRunnable);
                        return;
                    }
                    Launcher.this.exitSpringLoadedDragMode();
                }
            };
            if (extendedDelay) {
                i = EXIT_SPRINGLOADED_MODE_LONG_TIMEOUT;
            } else {
                i = 300;
            }
            handler.postDelayed(r2, (long) i);
        }
    }

    /* access modifiers changed from: package-private */
    public void exitSpringLoadedDragMode() {
        if (this.mState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
            showAppsCustomizeHelper(true, true);
            this.mState = State.APPS_CUSTOMIZE;
        }
    }

    /* access modifiers changed from: package-private */
    public void hideDockDivider() {
        if (this.mQsbDivider != null && this.mDockDivider != null) {
            this.mQsbDivider.setVisibility(4);
            this.mDockDivider.setVisibility(4);
        }
    }

    /* access modifiers changed from: package-private */
    public void showDockDivider(boolean animated) {
        if (this.mQsbDivider != null && this.mDockDivider != null) {
            this.mQsbDivider.setVisibility(0);
            this.mDockDivider.setVisibility(0);
            if (this.mDividerAnimator != null) {
                this.mDividerAnimator.cancel();
                this.mQsbDivider.setAlpha(1.0f);
                this.mDockDivider.setAlpha(1.0f);
                this.mDividerAnimator = null;
            }
            if (animated) {
                this.mDividerAnimator = LauncherAnimUtils.createAnimatorSet();
                this.mDividerAnimator.playTogether(new Animator[]{LauncherAnimUtils.ofFloat(this.mQsbDivider, "alpha", 1.0f), LauncherAnimUtils.ofFloat(this.mDockDivider, "alpha", 1.0f)});
                int duration = 0;
                if (this.mSearchDropTargetBar != null) {
                    duration = this.mSearchDropTargetBar.getTransitionInDuration();
                }
                this.mDividerAnimator.setDuration((long) duration);
                this.mDividerAnimator.start();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void lockAllApps() {
    }

    /* access modifiers changed from: package-private */
    public void unlockAllApps() {
    }

    /* access modifiers changed from: package-private */
    public void showHotseat(boolean animated) {
        if (LauncherApplication.isScreenLarge()) {
            return;
        }
        if (!animated) {
            this.mHotseat.setAlpha(1.0f);
        } else if (this.mHotseat.getAlpha() != 1.0f) {
            int duration = 0;
            if (this.mSearchDropTargetBar != null) {
                duration = this.mSearchDropTargetBar.getTransitionInDuration();
            }
            this.mHotseat.animate().alpha(1.0f).setDuration((long) duration);
        }
    }

    /* access modifiers changed from: package-private */
    public void hideHotseat(boolean animated) {
        if (LauncherApplication.isScreenLarge()) {
            return;
        }
        if (!animated) {
            this.mHotseat.setAlpha(0.0f);
        } else if (this.mHotseat.getAlpha() != 0.0f) {
            int duration = 0;
            if (this.mSearchDropTargetBar != null) {
                duration = this.mSearchDropTargetBar.getTransitionOutDuration();
            }
            this.mHotseat.animate().alpha(0.0f).setDuration((long) duration);
        }
    }

    /* access modifiers changed from: package-private */
    public void addExternalItemToScreen(ItemInfo itemInfo, CellLayout layout) {
        if (!this.mWorkspace.addExternalItemToScreen(itemInfo, layout)) {
            showOutOfSpaceMessage(isHotseatLayout(layout));
        }
    }

    private int getCurrentOrientationIndexForGlobalIcons() {
        switch (getResources().getConfiguration().orientation) {
            case 2:
                return 1;
            default:
                return 0;
        }
    }

    private Drawable getExternalPackageToolbarIcon(ComponentName activityName, String resourceName) {
        int iconResId;
        try {
            PackageManager packageManager = getPackageManager();
            Bundle metaData = packageManager.getActivityInfo(activityName, 128).metaData;
            if (!(metaData == null || (iconResId = metaData.getInt(resourceName)) == 0)) {
                return packageManager.getResourcesForActivity(activityName).getDrawable(iconResId);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Failed to load toolbar icon; " + activityName.flattenToShortString() + " not found", e);
        } catch (Resources.NotFoundException nfe) {
            Log.w(TAG, "Failed to load toolbar icon from " + activityName.flattenToShortString(), nfe);
        }
        return null;
    }

    private Drawable.ConstantState updateTextButtonWithIconFromExternalActivity(int buttonId, ComponentName activityName, int fallbackDrawableId, String toolbarResourceName) {
        Drawable toolbarIcon = getExternalPackageToolbarIcon(activityName, toolbarResourceName);
        Resources r = getResources();
        int w = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_width);
        int h = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_height);
        TextView button = (TextView) findViewById(buttonId);
        if (toolbarIcon == null) {
            Drawable toolbarIcon2 = r.getDrawable(fallbackDrawableId);
            toolbarIcon2.setBounds(0, 0, w, h);
            if (button == null) {
                return null;
            }
            button.setCompoundDrawables(toolbarIcon2, (Drawable) null, (Drawable) null, (Drawable) null);
            return null;
        }
        toolbarIcon.setBounds(0, 0, w, h);
        if (button != null) {
            button.setCompoundDrawables(toolbarIcon, (Drawable) null, (Drawable) null, (Drawable) null);
        }
        return toolbarIcon.getConstantState();
    }

    private Drawable.ConstantState updateButtonWithIconFromExternalActivity(int buttonId, ComponentName activityName, int fallbackDrawableId, String toolbarResourceName) {
        ImageView button = (ImageView) findViewById(buttonId);
        Drawable toolbarIcon = getExternalPackageToolbarIcon(activityName, toolbarResourceName);
        if (button != null) {
            if (toolbarIcon == null) {
                button.setImageResource(fallbackDrawableId);
            } else {
                button.setImageDrawable(toolbarIcon);
            }
        }
        if (toolbarIcon != null) {
            return toolbarIcon.getConstantState();
        }
        return null;
    }

    private void updateTextButtonWithDrawable(int buttonId, Drawable d) {
        ((TextView) findViewById(buttonId)).setCompoundDrawables(d, (Drawable) null, (Drawable) null, (Drawable) null);
    }

    private void updateButtonWithDrawable(int buttonId, Drawable.ConstantState d) {
        ((ImageView) findViewById(buttonId)).setImageDrawable(d.newDrawable(getResources()));
    }

    private void invalidatePressedFocusedStates(View container, View button) {
        if (container instanceof HolographicLinearLayout) {
            ((HolographicLinearLayout) container).invalidatePressedFocusedStates();
        } else if (button instanceof HolographicImageView) {
            ((HolographicImageView) button).invalidatePressedFocusedStates();
        }
    }

    private boolean updateGlobalSearchIcon() {
        View searchButtonContainer = findViewById(R.id.search_button_container);
        ImageView searchButton = (ImageView) findViewById(R.id.search_button);
        View voiceButtonContainer = findViewById(R.id.voice_button_container);
        View voiceButton = findViewById(R.id.voice_button);
        View voiceButtonProxy = findViewById(R.id.voice_button_proxy);
        ComponentName activityName = ((SearchManager) getSystemService("search")).getGlobalSearchActivity();
        if (activityName != null) {
            int coi = getCurrentOrientationIndexForGlobalIcons();
            sGlobalSearchIcon[coi] = updateButtonWithIconFromExternalActivity(R.id.search_button, activityName, R.drawable.ic_home_search_normal_holo, TOOLBAR_SEARCH_ICON_METADATA_NAME);
            if (sGlobalSearchIcon[coi] == null) {
                sGlobalSearchIcon[coi] = updateButtonWithIconFromExternalActivity(R.id.search_button, activityName, R.drawable.ic_home_search_normal_holo, TOOLBAR_ICON_METADATA_NAME);
            }
            if (searchButtonContainer != null) {
                searchButtonContainer.setVisibility(0);
            }
            searchButton.setVisibility(0);
            invalidatePressedFocusedStates(searchButtonContainer, searchButton);
            return true;
        }
        if (searchButtonContainer != null) {
            searchButtonContainer.setVisibility(8);
        }
        if (voiceButtonContainer != null) {
            voiceButtonContainer.setVisibility(8);
        }
        searchButton.setVisibility(8);
        voiceButton.setVisibility(8);
        if (voiceButtonProxy == null) {
            return false;
        }
        voiceButtonProxy.setVisibility(8);
        return false;
    }

    private void updateGlobalSearchIcon(Drawable.ConstantState d) {
        updateButtonWithDrawable(R.id.search_button, d);
        invalidatePressedFocusedStates(findViewById(R.id.search_button_container), (ImageView) findViewById(R.id.search_button));
    }

    private boolean updateVoiceSearchIcon(boolean searchVisible) {
        View voiceButtonContainer = findViewById(R.id.voice_button_container);
        View voiceButton = findViewById(R.id.voice_button);
        View voiceButtonProxy = findViewById(R.id.voice_button_proxy);
        ComponentName globalSearchActivity = ((SearchManager) getSystemService("search")).getGlobalSearchActivity();
        ComponentName activityName = null;
        if (globalSearchActivity != null) {
            Intent intent = new Intent("android.speech.action.WEB_SEARCH");
            intent.setPackage(globalSearchActivity.getPackageName());
            activityName = intent.resolveActivity(getPackageManager());
        }
        if (activityName == null) {
            activityName = new Intent("android.speech.action.WEB_SEARCH").resolveActivity(getPackageManager());
        }
        if (!searchVisible || activityName == null) {
            if (voiceButtonContainer != null) {
                voiceButtonContainer.setVisibility(8);
            }
            voiceButton.setVisibility(8);
            if (voiceButtonProxy == null) {
                return false;
            }
            voiceButtonProxy.setVisibility(8);
            return false;
        }
        int coi = getCurrentOrientationIndexForGlobalIcons();
        sVoiceSearchIcon[coi] = updateButtonWithIconFromExternalActivity(R.id.voice_button, activityName, R.drawable.ic_home_voice_search_holo, TOOLBAR_VOICE_SEARCH_ICON_METADATA_NAME);
        if (sVoiceSearchIcon[coi] == null) {
            sVoiceSearchIcon[coi] = updateButtonWithIconFromExternalActivity(R.id.voice_button, activityName, R.drawable.ic_home_voice_search_holo, TOOLBAR_ICON_METADATA_NAME);
        }
        if (voiceButtonContainer != null) {
            voiceButtonContainer.setVisibility(0);
        }
        voiceButton.setVisibility(0);
        if (voiceButtonProxy != null) {
            voiceButtonProxy.setVisibility(0);
        }
        invalidatePressedFocusedStates(voiceButtonContainer, voiceButton);
        return true;
    }

    private void updateVoiceSearchIcon(Drawable.ConstantState d) {
        View voiceButtonContainer = findViewById(R.id.voice_button_container);
        View voiceButton = findViewById(R.id.voice_button);
        updateButtonWithDrawable(R.id.voice_button, d);
        invalidatePressedFocusedStates(voiceButtonContainer, voiceButton);
    }

    private void updateAppMarketIcon() {
        View marketButton = findViewById(R.id.market_button);
        Intent intent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.APP_MARKET");
        ComponentName activityName = intent.resolveActivity(getPackageManager());
        if (activityName != null) {
            int coi = getCurrentOrientationIndexForGlobalIcons();
            this.mAppMarketIntent = intent;
            sAppMarketIcon[coi] = updateTextButtonWithIconFromExternalActivity(R.id.market_button, activityName, R.drawable.ic_launcher_market_holo, TOOLBAR_ICON_METADATA_NAME);
            marketButton.setVisibility(0);
            return;
        }
        marketButton.setVisibility(8);
        marketButton.setEnabled(false);
    }

    private void updateAppMarketIcon(Drawable.ConstantState d) {
        Resources r = getResources();
        Drawable marketIconDrawable = d.newDrawable(r);
        marketIconDrawable.setBounds(0, 0, r.getDimensionPixelSize(R.dimen.toolbar_external_icon_width), r.getDimensionPixelSize(R.dimen.toolbar_external_icon_height));
        updateTextButtonWithDrawable(R.id.market_button, marketIconDrawable);
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        boolean result = super.dispatchPopulateAccessibilityEvent(event);
        List<CharSequence> text = event.getText();
        text.clear();
        if (this.mState == State.APPS_CUSTOMIZE) {
            text.add(getString(R.string.all_apps_button_label));
        } else {
            text.add(getString(R.string.all_apps_home_button_label));
        }
        return result;
    }

    private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
        private CloseSystemDialogsIntentReceiver() {
        }

        /* synthetic */ CloseSystemDialogsIntentReceiver(Launcher launcher, CloseSystemDialogsIntentReceiver closeSystemDialogsIntentReceiver) {
            this();
        }

        public void onReceive(Context context, Intent intent) {
            Launcher.this.closeSystemDialogs();
        }
    }

    private class AppWidgetResetObserver extends ContentObserver {
        public AppWidgetResetObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            Launcher.this.onAppWidgetReset();
        }
    }

    public boolean setLoadOnResume() {
        if (!this.mPaused) {
            return false;
        }
        Log.i(TAG, "setLoadOnResume");
        this.mOnResumeNeedsLoad = true;
        return true;
    }

    public int getCurrentWorkspaceScreen() {
        if (this.mWorkspace != null) {
            return this.mWorkspace.getCurrentPage();
        }
        return SCREEN_COUNT / 2;
    }

    public void startBinding() {
        Workspace workspace = this.mWorkspace;
        this.mNewShortcutAnimatePage = -1;
        this.mNewShortcutAnimateViews.clear();
        this.mWorkspace.clearDropTargets();
        int count = workspace.getChildCount();
        for (int i = 0; i < count; i++) {
            ((CellLayout) workspace.getChildAt(i)).removeAllViewsInLayout();
        }
        this.mWidgetsToAdvance.clear();
        if (this.mHotseat != null) {
            this.mHotseat.resetLayout();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x003a, code lost:
        continue;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void bindItems(java.util.ArrayList<com.android.launcher2.ItemInfo> r21, int r22, int r23) {
        /*
            r20 = this;
            r20.setLoadOnResume()
            java.util.HashSet r18 = new java.util.HashSet
            r18.<init>()
            r0 = r20
            android.content.SharedPreferences r4 = r0.mSharedPrefs
            java.lang.String r6 = "apps.new.list"
            r0 = r18
            java.util.Set r18 = r4.getStringSet(r6, r0)
            r0 = r20
            com.android.launcher2.Workspace r2 = r0.mWorkspace
            r15 = r22
        L_0x001a:
            r0 = r23
            if (r15 < r0) goto L_0x0022
            r2.requestLayout()
            return
        L_0x0022:
            r0 = r21
            java.lang.Object r17 = r0.get(r15)
            com.android.launcher2.ItemInfo r17 = (com.android.launcher2.ItemInfo) r17
            r0 = r17
            long r6 = r0.container
            r8 = -101(0xffffffffffffff9b, double:NaN)
            int r4 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1))
            if (r4 != 0) goto L_0x003d
            r0 = r20
            com.android.launcher2.Hotseat r4 = r0.mHotseat
            if (r4 != 0) goto L_0x003d
        L_0x003a:
            int r15 = r15 + 1
            goto L_0x001a
        L_0x003d:
            r0 = r17
            int r4 = r0.itemType
            switch(r4) {
                case 0: goto L_0x0045;
                case 1: goto L_0x0045;
                case 2: goto L_0x00ac;
                default: goto L_0x0044;
            }
        L_0x0044:
            goto L_0x003a
        L_0x0045:
            r16 = r17
            com.android.launcher2.ShortcutInfo r16 = (com.android.launcher2.ShortcutInfo) r16
            r0 = r16
            android.content.Intent r4 = r0.intent
            r6 = 0
            java.lang.String r4 = r4.toUri(r6)
            java.lang.String r19 = r4.toString()
            r0 = r20
            r1 = r16
            android.view.View r3 = r0.createShortcut(r1)
            r0 = r17
            long r4 = r0.container
            r0 = r17
            int r6 = r0.screen
            r0 = r17
            int r7 = r0.cellX
            r0 = r17
            int r8 = r0.cellY
            r9 = 1
            r10 = 1
            r11 = 0
            r2.addInScreen(r3, r4, r6, r7, r8, r9, r10, r11)
            r14 = 0
            monitor-enter(r18)
            boolean r4 = r18.contains(r19)     // Catch:{ all -> 0x00a9 }
            if (r4 == 0) goto L_0x0080
            boolean r14 = r18.remove(r19)     // Catch:{ all -> 0x00a9 }
        L_0x0080:
            monitor-exit(r18)     // Catch:{ all -> 0x00a9 }
            if (r14 == 0) goto L_0x003a
            r4 = 0
            r3.setAlpha(r4)
            r4 = 0
            r3.setScaleX(r4)
            r4 = 0
            r3.setScaleY(r4)
            r0 = r17
            int r4 = r0.screen
            r0 = r20
            r0.mNewShortcutAnimatePage = r4
            r0 = r20
            java.util.ArrayList<android.view.View> r4 = r0.mNewShortcutAnimateViews
            boolean r4 = r4.contains(r3)
            if (r4 != 0) goto L_0x003a
            r0 = r20
            java.util.ArrayList<android.view.View> r4 = r0.mNewShortcutAnimateViews
            r4.add(r3)
            goto L_0x003a
        L_0x00a9:
            r4 = move-exception
            monitor-exit(r18)     // Catch:{ all -> 0x00a9 }
            throw r4
        L_0x00ac:
            int r7 = com.android.launcher.R.layout.folder_icon
            int r4 = r2.getCurrentPage()
            android.view.View r4 = r2.getChildAt(r4)
            android.view.ViewGroup r4 = (android.view.ViewGroup) r4
            r6 = r17
            com.android.launcher2.FolderInfo r6 = (com.android.launcher2.FolderInfo) r6
            r0 = r20
            com.android.launcher2.IconCache r8 = r0.mIconCache
            r0 = r20
            com.android.launcher2.FolderIcon r5 = com.android.launcher2.FolderIcon.fromXml(r7, r0, r4, r6, r8)
            r0 = r17
            long r6 = r0.container
            r0 = r17
            int r8 = r0.screen
            r0 = r17
            int r9 = r0.cellX
            r0 = r17
            int r10 = r0.cellY
            r11 = 1
            r12 = 1
            r13 = 0
            r4 = r2
            r4.addInScreen(r5, r6, r8, r9, r10, r11, r12, r13)
            goto L_0x003a
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.launcher2.Launcher.bindItems(java.util.ArrayList, int, int):void");
    }

    public void bindFolders(HashMap<Long, FolderInfo> folders) {
        setLoadOnResume();
        sFolders.clear();
        sFolders.putAll(folders);
    }

    public void bindAppWidget(LauncherAppWidgetInfo item) {
        setLoadOnResume();
        Workspace workspace = this.mWorkspace;
        int appWidgetId = item.appWidgetId;
        if (appWidgetId == -1) {
            DefaultWorkspace.myfirstview = LayoutInflater.from(this).inflate(R.layout.firstownview, (ViewGroup) null);
            DefaultWorkspace.myfirstview.setTag(item);
            workspace.addInScreen(DefaultWorkspace.myfirstview, item.container, item.screen, item.cellX, item.cellY, item.spanX, item.spanY, false);
        } else {
            AppWidgetProviderInfo appWidgetInfo = this.mAppWidgetManager.getAppWidgetInfo(appWidgetId);
            item.hostView = this.mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
            item.hostView.setTag(item);
            item.onBindAppWidget(this);
            workspace.addInScreen(item.hostView, item.container, item.screen, item.cellX, item.cellY, item.spanX, item.spanY, false);
            addWidgetToAutoAdvanceIfNeeded(item.hostView, appWidgetInfo);
        }
        workspace.requestLayout();
    }

    public void onPageBoundSynchronously(int page) {
        this.mSynchronouslyBoundPages.add(Integer.valueOf(page));
    }

    public void finishBindingItems() {
        boolean willSnapPage;
        setLoadOnResume();
        if (this.mSavedState != null) {
            if (!this.mWorkspace.hasFocus()) {
                this.mWorkspace.getChildAt(this.mWorkspace.getCurrentPage()).requestFocus();
            }
            this.mSavedState = null;
        }
        this.mWorkspace.restoreInstanceStateForRemainingPages();
        for (int i = 0; i < sPendingAddList.size(); i++) {
            completeAdd(sPendingAddList.get(i));
        }
        sPendingAddList.clear();
        updateAppMarketIcon();
        if (this.mVisible || this.mWorkspaceLoading) {
            Runnable newAppsRunnable = new Runnable() {
                public void run() {
                    Launcher.this.runNewAppsAnimation(false);
                }
            };
            if (this.mNewShortcutAnimatePage <= -1 || this.mNewShortcutAnimatePage == this.mWorkspace.getCurrentPage()) {
                willSnapPage = false;
            } else {
                willSnapPage = true;
            }
            if (!canRunNewAppsAnimation()) {
                runNewAppsAnimation(willSnapPage);
            } else if (willSnapPage) {
                this.mWorkspace.snapToPage(this.mNewShortcutAnimatePage, newAppsRunnable);
            } else {
                runNewAppsAnimation(false);
            }
        }
        this.mWorkspaceLoading = false;
    }

    private boolean canRunNewAppsAnimation() {
        return this.mNewShortcutAnimatePage < this.mWorkspace.getChildCount() && System.currentTimeMillis() - this.mDragController.getLastGestureUpTime() > ((long) (NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS * 1000));
    }

    /* access modifiers changed from: private */
    public void runNewAppsAnimation(boolean immediate) {
        AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
        Collection<Animator> bounceAnims = new ArrayList<>();
        Collections.sort(this.mNewShortcutAnimateViews, new Comparator<View>() {
            public int compare(View a, View b) {
                CellLayout.LayoutParams alp = (CellLayout.LayoutParams) a.getLayoutParams();
                CellLayout.LayoutParams blp = (CellLayout.LayoutParams) b.getLayoutParams();
                int cellCountX = LauncherModel.getCellCountX();
                return ((alp.cellY * cellCountX) + alp.cellX) - ((blp.cellY * cellCountX) + blp.cellX);
            }
        });
        if (immediate) {
            Iterator<View> it = this.mNewShortcutAnimateViews.iterator();
            while (it.hasNext()) {
                View v = it.next();
                v.setAlpha(1.0f);
                v.setScaleX(1.0f);
                v.setScaleY(1.0f);
            }
        } else {
            for (int i = 0; i < this.mNewShortcutAnimateViews.size(); i++) {
                ValueAnimator bounceAnim = LauncherAnimUtils.ofPropertyValuesHolder(this.mNewShortcutAnimateViews.get(i), PropertyValuesHolder.ofFloat("alpha", new float[]{1.0f}), PropertyValuesHolder.ofFloat("scaleX", new float[]{1.0f}), PropertyValuesHolder.ofFloat("scaleY", new float[]{1.0f}));
                bounceAnim.setDuration(450);
                bounceAnim.setStartDelay((long) (i * 75));
                bounceAnim.setInterpolator(new SmoothPagedView.OvershootInterpolator());
                bounceAnims.add(bounceAnim);
            }
            anim.playTogether(bounceAnims);
            anim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    if (Launcher.this.mWorkspace != null) {
                        Launcher.this.mWorkspace.postDelayed(Launcher.this.mBuildLayersRunnable, 500);
                    }
                }
            });
            anim.start();
        }
        this.mNewShortcutAnimatePage = -1;
        this.mNewShortcutAnimateViews.clear();
        new Thread("clearNewAppsThread") {
            public void run() {
                Launcher.this.mSharedPrefs.edit().putInt(InstallShortcutReceiver.NEW_APPS_PAGE_KEY, -1).putStringSet(InstallShortcutReceiver.NEW_APPS_LIST_KEY, (Set) null).commit();
            }
        }.start();
    }

    public void bindSearchablesChanged() {
        boolean searchVisible = updateGlobalSearchIcon();
        boolean voiceVisible = updateVoiceSearchIcon(searchVisible);
        if (this.mSearchDropTargetBar != null) {
            this.mSearchDropTargetBar.onSearchPackagesChanged(searchVisible, voiceVisible);
        }
    }

    public void bindAllApplications(final ArrayList<ApplicationInfo> apps) {
        Runnable setAllAppsRunnable = new Runnable() {
            public void run() {
                if (Launcher.this.mAppsCustomizeContent != null) {
                    Launcher.this.mAppsCustomizeContent.setApps(apps);
                }
            }
        };
        View progressBar = this.mAppsCustomizeTabHost.findViewById(R.id.apps_customize_progress_bar);
        if (progressBar != null) {
            ((ViewGroup) progressBar.getParent()).removeView(progressBar);
            this.mAppsCustomizeTabHost.post(setAllAppsRunnable);
            return;
        }
        setAllAppsRunnable.run();
    }

    public void bindAppsAdded(ArrayList<ApplicationInfo> apps) {
        setLoadOnResume();
        if (this.mAppsCustomizeContent != null) {
            this.mAppsCustomizeContent.addApps(apps);
        }
    }

    public void bindAppsUpdated(ArrayList<ApplicationInfo> apps) {
        setLoadOnResume();
        if (this.mWorkspace != null) {
            this.mWorkspace.updateShortcuts(apps);
        }
        if (this.mAppsCustomizeContent != null) {
            this.mAppsCustomizeContent.updateApps(apps);
        }
    }

    public void bindAppsRemoved(ArrayList<String> packageNames, boolean permanent) {
        if (permanent) {
            this.mWorkspace.removeItems(packageNames);
        }
        if (this.mAppsCustomizeContent != null) {
            this.mAppsCustomizeContent.removeApps(packageNames);
        }
        this.mDragController.onAppsRemoved(packageNames, this);
    }

    public void bindPackagesUpdated() {
        if (this.mAppsCustomizeContent != null) {
            this.mAppsCustomizeContent.onPackagesUpdated();
        }
    }

    private int mapConfigurationOriActivityInfoOri(int configOri) {
        Display d = getWindowManager().getDefaultDisplay();
        int naturalOri = 2;
        switch (d.getRotation()) {
            case 0:
            case 2:
                naturalOri = configOri;
                break;
            case 1:
            case 3:
                if (configOri != 2) {
                    naturalOri = 2;
                    break;
                } else {
                    naturalOri = 1;
                    break;
                }
        }
        int[] oriMap = new int[4];
        oriMap[0] = 1;
        oriMap[2] = 9;
        oriMap[3] = 8;
        int indexOffset = 0;
        if (naturalOri == 2) {
            indexOffset = 1;
        }
        return oriMap[(d.getRotation() + indexOffset) % 4];
    }

    public boolean isRotationEnabled() {
        return sForceEnableRotation || getResources().getBoolean(R.bool.allow_rotation);
    }

    public void lockScreenOrientation() {
        if (isRotationEnabled()) {
            setRequestedOrientation(mapConfigurationOriActivityInfoOri(getResources().getConfiguration().orientation));
        }
    }

    public void unlockScreenOrientation(boolean immediate) {
        if (!isRotationEnabled()) {
            return;
        }
        if (immediate) {
            setRequestedOrientation(-1);
        } else {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    Launcher.this.setRequestedOrientation(-1);
                }
            }, 500);
        }
    }

    private boolean isClingsEnabled() {
        return false;
    }

    private Cling initCling(int clingId, int[] positionData, boolean animate, int delay) {
        boolean z = true;
        final Cling cling = (Cling) findViewById(clingId);
        if (cling != null) {
            cling.init(this, positionData);
            cling.setVisibility(0);
            cling.setLayerType(2, (Paint) null);
            if (animate) {
                cling.buildLayer();
                cling.setAlpha(0.0f);
                cling.animate().alpha(1.0f).setInterpolator(new AccelerateInterpolator()).setDuration(550).setStartDelay((long) delay).start();
            } else {
                cling.setAlpha(1.0f);
            }
            cling.setFocusableInTouchMode(true);
            cling.post(new Runnable() {
                public void run() {
                    cling.setFocusable(true);
                    cling.requestFocus();
                }
            });
            HideFromAccessibilityHelper hideFromAccessibilityHelper = this.mHideFromAccessibilityHelper;
            DragLayer dragLayer = this.mDragLayer;
            if (clingId != R.id.all_apps_cling) {
                z = false;
            }
            hideFromAccessibilityHelper.setImportantForAccessibilityToNo(dragLayer, z);
        }
        return cling;
    }

    private void dismissCling(final Cling cling, final String flag, int duration) {
        if (cling != null && cling.getVisibility() != 8) {
            ObjectAnimator anim = LauncherAnimUtils.ofFloat(cling, "alpha", 0.0f);
            anim.setDuration((long) duration);
            anim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    cling.setVisibility(8);
                    cling.cleanup();
                    final String str = flag;
                    new Thread("dismissClingThread") {
                        public void run() {
                            SharedPreferences.Editor editor = Launcher.this.mSharedPrefs.edit();
                            editor.putBoolean(str, true);
                            editor.commit();
                        }
                    }.start();
                }
            });
            anim.start();
            this.mHideFromAccessibilityHelper.restoreImportantForAccessibility(this.mDragLayer);
        }
    }

    private void removeCling(int id) {
        final View cling = findViewById(id);
        if (cling != null) {
            final ViewGroup parent = (ViewGroup) cling.getParent();
            parent.post(new Runnable() {
                public void run() {
                    parent.removeView(cling);
                }
            });
            this.mHideFromAccessibilityHelper.restoreImportantForAccessibility(this.mDragLayer);
        }
    }

    private boolean skipCustomClingIfNoAccounts() {
        if (!((Cling) findViewById(R.id.workspace_cling)).getDrawIdentifier().equals("workspace_custom") || AccountManager.get(this).getAccountsByType("com.google").length != 0) {
            return false;
        }
        return true;
    }

    public void showFirstRunWorkspaceCling() {
        if (!isClingsEnabled() || this.mSharedPrefs.getBoolean("cling.workspace.dismissed", false) || skipCustomClingIfNoAccounts()) {
            removeCling(R.id.workspace_cling);
            return;
        }
        if (this.mSharedPrefs.getInt("DEFAULT_WORKSPACE_RESOURCE_ID", 0) != 0 && getResources().getBoolean(R.bool.config_useCustomClings)) {
            View cling = findViewById(R.id.workspace_cling);
            ViewGroup clingParent = (ViewGroup) cling.getParent();
            int clingIndex = clingParent.indexOfChild(cling);
            clingParent.removeViewAt(clingIndex);
            View customCling = this.mInflater.inflate(R.layout.custom_workspace_cling, clingParent, false);
            clingParent.addView(customCling, clingIndex);
            customCling.setId(R.id.workspace_cling);
        }
        initCling(R.id.workspace_cling, (int[]) null, false, 0);
    }

    public void showFirstRunAllAppsCling(int[] position) {
        if (!isClingsEnabled() || this.mSharedPrefs.getBoolean("cling.allapps.dismissed", false)) {
            removeCling(R.id.all_apps_cling);
        } else {
            initCling(R.id.all_apps_cling, position, true, 0);
        }
    }

    public Cling showFirstRunFoldersCling() {
        if (isClingsEnabled() && !this.mSharedPrefs.getBoolean("cling.folder.dismissed", false)) {
            return initCling(R.id.folder_cling, (int[]) null, true, 0);
        }
        removeCling(R.id.folder_cling);
        return null;
    }

    public boolean isFolderClingVisible() {
        Cling cling = (Cling) findViewById(R.id.folder_cling);
        if (cling == null || cling.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    public void dismissWorkspaceCling(View v) {
        dismissCling((Cling) findViewById(R.id.workspace_cling), "cling.workspace.dismissed", DISMISS_CLING_DURATION);
    }

    public void dismissAllAppsCling(View v) {
        dismissCling((Cling) findViewById(R.id.all_apps_cling), "cling.allapps.dismissed", DISMISS_CLING_DURATION);
    }

    public void dismissFolderCling(View v) {
        dismissCling((Cling) findViewById(R.id.folder_cling), "cling.folder.dismissed", DISMISS_CLING_DURATION);
    }

    public void dumpState() {
        Log.d(TAG, "BEGIN launcher2 dump state for launcher " + this);
        Log.d(TAG, "mSavedState=" + this.mSavedState);
        Log.d(TAG, "mWorkspaceLoading=" + this.mWorkspaceLoading);
        Log.d(TAG, "mRestoring=" + this.mRestoring);
        Log.d(TAG, "mWaitingForResult=" + this.mWaitingForResult);
        Log.d(TAG, "mSavedInstanceState=" + this.mSavedInstanceState);
        Log.d(TAG, "sFolders.size=" + sFolders.size());
        this.mModel.dumpState();
        if (this.mAppsCustomizeContent != null) {
            this.mAppsCustomizeContent.dumpState();
        }
        Log.d(TAG, "END launcher2 dump state");
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.println(" ");
        writer.println("Debug logs: ");
        for (int i = 0; i < sDumpLogs.size(); i++) {
            writer.println("  " + sDumpLogs.get(i));
        }
    }

    public static void dumpDebugLogsToConsole() {
        Log.d(TAG, "");
        Log.d(TAG, "*********************");
        Log.d(TAG, "Launcher debug logs: ");
        for (int i = 0; i < sDumpLogs.size(); i++) {
            Log.d(TAG, "  " + sDumpLogs.get(i));
        }
        Log.d(TAG, "*********************");
        Log.d(TAG, "");
    }
}
