package com.android.launcher2;

import android.app.SearchManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.launcher.R;
import com.android.launcher2.DefaultWorkspace;
import com.android.launcher2.LauncherSettings;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

public class LauncherProvider extends ContentProvider {
    private static final String ACTION_APPWIDGET_DEFAULT_WORKSPACE_CONFIGURE = "com.android.launcher.action.APPWIDGET_DEFAULT_WORKSPACE_CONFIGURE";
    static final String AUTHORITY = "com.android.launcher2.settings";
    static final Uri CONTENT_APPWIDGET_RESET_URI = Uri.parse("content://com.android.launcher2.settings/appWidgetReset");
    private static final String DATABASE_NAME = "launcher.db";
    private static final int DATABASE_VERSION = 12;
    static final String DB_CREATED_BUT_DEFAULT_WORKSPACE_NOT_LOADED = "DB_CREATED_BUT_DEFAULT_WORKSPACE_NOT_LOADED";
    static final String DEFAULT_WORKSPACE_RESOURCE_ID = "DEFAULT_WORKSPACE_RESOURCE_ID";
    private static final boolean LOGD = false;
    static final String PARAMETER_NOTIFY = "notify";
    static final String TABLE_FAVORITES = "favorites";
    private static final String TAG = "Launcher.LauncherProvider";
    public static int isBoot = 0;
    private DatabaseHelper mOpenHelper;

    public boolean onCreate() {
        Resources res = ((LauncherApplication) getContext()).getResources();
        DefaultWorkspace.cell_count_x = res.getInteger(R.integer.cell_count_x);
        DefaultWorkspace.cell_count_y = res.getInteger(R.integer.cell_count_y);
        DefaultWorkspace.hotseat_cell_count_x = res.getInteger(R.integer.hotseat_cell_count_x);
        DefaultWorkspace.hotseat_cell_count_y = res.getInteger(R.integer.hotseat_cell_count_y);
        DefaultWorkspace.hotseat_all_apps_index = res.getInteger(R.integer.hotseat_all_apps_index);
        this.mOpenHelper = new DatabaseHelper(getContext());
        ((LauncherApplication) getContext()).setLauncherProvider(this);
        return true;
    }

    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, (String) null, (String[]) null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        }
        return "vnd.android.cursor.item/" + args.table;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);
        Cursor result = qb.query(this.mOpenHelper.getWritableDatabase(), projection, args.where, args.args, (String) null, (String) null, sortOrder);
        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    /* access modifiers changed from: private */
    public static long dbInsertAndCheck(DatabaseHelper helper, SQLiteDatabase db, String table, String nullColumnHack, ContentValues values) {
        if (values.containsKey("_id")) {
            return db.insert(table, nullColumnHack, values);
        }
        throw new RuntimeException("Error: attempting to add item without specifying an id");
    }

    /* access modifiers changed from: private */
    public static void deleteId(SQLiteDatabase db, long id) {
        SqlArguments args = new SqlArguments(LauncherSettings.Favorites.getContentUri(id, LOGD), (String) null, (String[]) null);
        db.delete(args.table, args.where, args.args);
    }

    public Uri insert(Uri uri, ContentValues initialValues) {
        SqlArguments args = new SqlArguments(uri);
        long rowId = dbInsertAndCheck(this.mOpenHelper, this.mOpenHelper.getWritableDatabase(), args.table, (String) null, initialValues);
        if (rowId <= 0) {
            return null;
        }
        Uri uri2 = ContentUris.withAppendedId(uri, rowId);
        sendNotify(uri2);
        return uri2;
    }

    /* JADX INFO: finally extract failed */
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SqlArguments args = new SqlArguments(uri);
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (ContentValues dbInsertAndCheck : values) {
                if (dbInsertAndCheck(this.mOpenHelper, db, args.table, (String) null, dbInsertAndCheck) < 0) {
                    db.endTransaction();
                    return 0;
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            sendNotify(uri);
            return values.length;
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        int count = this.mOpenHelper.getWritableDatabase().delete(args.table, args.where, args.args);
        if (count > 0) {
            sendNotify(uri);
        }
        return count;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        int count = this.mOpenHelper.getWritableDatabase().update(args.table, values, args.where, args.args);
        if (count > 0) {
            sendNotify(uri);
        }
        return count;
    }

    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, (ContentObserver) null);
        }
    }

    public long generateNewId() {
        return this.mOpenHelper.generateNewId();
    }

    public synchronized void loadDefaultFavoritesIfNecessary(int origWorkspaceResId) {
        SharedPreferences sp = getContext().getSharedPreferences(LauncherApplication.getSharedPreferencesKey(), 0);
        if (sp.getBoolean(DB_CREATED_BUT_DEFAULT_WORKSPACE_NOT_LOADED, LOGD)) {
            SharedPreferences.Editor editor = sp.edit();
            if (getContext().getResources().getBoolean(R.bool.use_default_workspace)) {
                int unused = this.mOpenHelper.loadFavorites(this.mOpenHelper.getWritableDatabase(), R.xml.default_workspace);
            }
            int unused2 = this.mOpenHelper.loadFavoritesOwn(this.mOpenHelper.getWritableDatabase());
            editor.remove(DB_CREATED_BUT_DEFAULT_WORKSPACE_NOT_LOADED);
            editor.commit();
        }
    }

    public void loadDefaultForce() {
        File dbFile = new File(this.mOpenHelper.getWritableDatabase().getPath());
        this.mOpenHelper.close();
        if (dbFile.exists()) {
            SQLiteDatabase.deleteDatabase(dbFile);
        }
        this.mOpenHelper = new DatabaseHelper(getContext());
        int unused = this.mOpenHelper.loadFavoritesOwn(this.mOpenHelper.getWritableDatabase());
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String TAG_APPWIDGET = "appwidget";
        private static final String TAG_CLOCK = "clock";
        private static final String TAG_EXTRA = "extra";
        private static final String TAG_FAVORITE = "favorite";
        private static final String TAG_FAVORITES = "favorites";
        private static final String TAG_FOLDER = "folder";
        private static final String TAG_SEARCH = "search";
        private static final String TAG_SHORTCUT = "shortcut";
        private final AppWidgetHost mAppWidgetHost;
        private final Context mContext;
        private long mMaxId = -1;

        DatabaseHelper(Context context) {
            super(context, LauncherProvider.DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 12);
            this.mContext = context;
            this.mAppWidgetHost = new AppWidgetHost(context, 1024);
            if (MyWorkspace.GetInstance().bLoadDefault(context).booleanValue()) {
                context.deleteDatabase(LauncherProvider.DATABASE_NAME);
            }
            MyWorkspace.GetInstance().initView();
            if (this.mMaxId == -1) {
                this.mMaxId = initializeMaxId(getWritableDatabase());
            }
        }

        private void sendAppWidgetResetNotify() {
            this.mContext.getContentResolver().notifyChange(LauncherProvider.CONTENT_APPWIDGET_RESET_URI, (ContentObserver) null);
        }

        public void onCreate(SQLiteDatabase db) {
            this.mMaxId = 1;
            db.execSQL("CREATE TABLE favorites (_id INTEGER PRIMARY KEY,title TEXT,intent TEXT,container INTEGER,screen INTEGER,cellX INTEGER,cellY INTEGER,spanX INTEGER,spanY INTEGER,itemType INTEGER,appWidgetId INTEGER NOT NULL DEFAULT -1,isShortcut INTEGER,iconType INTEGER,iconPackage TEXT,iconResource TEXT,icon BLOB,uri TEXT,displayMode INTEGER);");
            if (this.mAppWidgetHost != null) {
                this.mAppWidgetHost.deleteHost();
                sendAppWidgetResetNotify();
            }
            if (!convertDatabase(db)) {
                setFlagToLoadDefaultWorkspaceLater();
            }
        }

        private void setFlagToLoadDefaultWorkspaceLater() {
            SharedPreferences.Editor editor = this.mContext.getSharedPreferences(LauncherApplication.getSharedPreferencesKey(), 0).edit();
            editor.putBoolean(LauncherProvider.DB_CREATED_BUT_DEFAULT_WORKSPACE_NOT_LOADED, true);
            editor.commit();
        }

        private boolean convertDatabase(SQLiteDatabase db) {
            boolean converted = LauncherProvider.LOGD;
            Uri uri = Uri.parse("content://settings/old_favorites?notify=true");
            ContentResolver resolver = this.mContext.getContentResolver();
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, (String[]) null, (String) null, (String[]) null, (String) null);
            } catch (Exception e) {
            }
            if (cursor != null && cursor.getCount() > 0) {
                try {
                    converted = copyFromCursor(db, cursor) > 0 ? true : LauncherProvider.LOGD;
                    if (converted) {
                        resolver.delete(uri, (String) null, (String[]) null);
                    }
                } finally {
                    cursor.close();
                }
            }
            if (converted) {
                convertWidgets(db);
            }
            return converted;
        }

        /* JADX INFO: finally extract failed */
        private int copyFromCursor(SQLiteDatabase db, Cursor c) {
            int idIndex = c.getColumnIndexOrThrow("_id");
            int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.BaseLauncherColumns.INTENT);
            int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.BaseLauncherColumns.TITLE);
            int iconTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.BaseLauncherColumns.ICON_TYPE);
            int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.BaseLauncherColumns.ICON);
            int iconPackageIndex = c.getColumnIndexOrThrow(LauncherSettings.BaseLauncherColumns.ICON_PACKAGE);
            int iconResourceIndex = c.getColumnIndexOrThrow(LauncherSettings.BaseLauncherColumns.ICON_RESOURCE);
            int containerIndex = c.getColumnIndexOrThrow("container");
            int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.BaseLauncherColumns.ITEM_TYPE);
            int screenIndex = c.getColumnIndexOrThrow("screen");
            int cellXIndex = c.getColumnIndexOrThrow("cellX");
            int cellYIndex = c.getColumnIndexOrThrow("cellY");
            int uriIndex = c.getColumnIndexOrThrow("uri");
            int displayModeIndex = c.getColumnIndexOrThrow("displayMode");
            ContentValues[] rows = new ContentValues[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                ContentValues values = new ContentValues(c.getColumnCount());
                values.put("_id", Long.valueOf(c.getLong(idIndex)));
                values.put(LauncherSettings.BaseLauncherColumns.INTENT, c.getString(intentIndex));
                values.put(LauncherSettings.BaseLauncherColumns.TITLE, c.getString(titleIndex));
                values.put(LauncherSettings.BaseLauncherColumns.ICON_TYPE, Integer.valueOf(c.getInt(iconTypeIndex)));
                values.put(LauncherSettings.BaseLauncherColumns.ICON, c.getBlob(iconIndex));
                values.put(LauncherSettings.BaseLauncherColumns.ICON_PACKAGE, c.getString(iconPackageIndex));
                values.put(LauncherSettings.BaseLauncherColumns.ICON_RESOURCE, c.getString(iconResourceIndex));
                values.put("container", Integer.valueOf(c.getInt(containerIndex)));
                values.put(LauncherSettings.BaseLauncherColumns.ITEM_TYPE, Integer.valueOf(c.getInt(itemTypeIndex)));
                values.put("appWidgetId", -1);
                values.put("screen", Integer.valueOf(c.getInt(screenIndex)));
                values.put("cellX", Integer.valueOf(c.getInt(cellXIndex)));
                values.put("cellY", Integer.valueOf(c.getInt(cellYIndex)));
                values.put("uri", c.getString(uriIndex));
                values.put("displayMode", Integer.valueOf(c.getInt(displayModeIndex)));
                rows[i] = values;
                i++;
            }
            db.beginTransaction();
            int total = 0;
            try {
                for (ContentValues access$0 : rows) {
                    if (LauncherProvider.dbInsertAndCheck(this, db, TAG_FAVORITES, (String) null, access$0) < 0) {
                        db.endTransaction();
                        return 0;
                    }
                    total++;
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                return total;
            } catch (Throwable th) {
                db.endTransaction();
                throw th;
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            int version = oldVersion;
            if (version < 3) {
                db.beginTransaction();
                try {
                    db.execSQL("ALTER TABLE favorites ADD COLUMN appWidgetId INTEGER NOT NULL DEFAULT -1;");
                    db.setTransactionSuccessful();
                    version = 3;
                } catch (SQLException ex) {
                    Log.e(LauncherProvider.TAG, ex.getMessage(), ex);
                } finally {
                    db.endTransaction();
                }
                if (version == 3) {
                    convertWidgets(db);
                }
            }
            if (version < 4) {
                version = 4;
            }
            if (version < 6) {
                db.beginTransaction();
                try {
                    db.execSQL("UPDATE favorites SET screen=(screen + 1);");
                    db.setTransactionSuccessful();
                } catch (SQLException ex2) {
                    Log.e(LauncherProvider.TAG, ex2.getMessage(), ex2);
                } finally {
                    db.endTransaction();
                }
                if (updateContactsShortcuts(db)) {
                    version = 6;
                }
            }
            if (version < 7) {
                convertWidgets(db);
                version = 7;
            }
            if (version < 8) {
                normalizeIcons(db);
                version = 8;
            }
            if (version < 9) {
                if (this.mMaxId == -1) {
                    this.mMaxId = initializeMaxId(db);
                }
                loadFavorites(db, R.xml.update_workspace);
                version = 9;
            }
            if (version < 12) {
                updateContactsShortcuts(db);
                version = 12;
            }
            if (version != 12) {
                Log.w(LauncherProvider.TAG, "Destroying all old data.");
                db.execSQL("DROP TABLE IF EXISTS favorites");
                onCreate(db);
            }
        }

        private boolean updateContactsShortcuts(SQLiteDatabase db) {
            String selectWhere = LauncherProvider.buildOrWhereString(LauncherSettings.BaseLauncherColumns.ITEM_TYPE, new int[]{1});
            Cursor c = null;
            db.beginTransaction();
            try {
                Cursor c2 = db.query(TAG_FAVORITES, new String[]{"_id", LauncherSettings.BaseLauncherColumns.INTENT}, selectWhere, (String[]) null, (String) null, (String) null, (String) null);
                if (c2 == null) {
                    db.endTransaction();
                    if (c2 != null) {
                        c2.close();
                    }
                    return LauncherProvider.LOGD;
                }
                int idIndex = c2.getColumnIndex("_id");
                int intentIndex = c2.getColumnIndex(LauncherSettings.BaseLauncherColumns.INTENT);
                while (c2.moveToNext()) {
                    long favoriteId = c2.getLong(idIndex);
                    String intentUri = c2.getString(intentIndex);
                    if (intentUri != null) {
                        try {
                            Intent intent = Intent.parseUri(intentUri, 0);
                            Log.d("Home", intent.toString());
                            Uri uri = intent.getData();
                            if (uri != null) {
                                String data = uri.toString();
                                if (("android.intent.action.VIEW".equals(intent.getAction()) || "com.android.contacts.action.QUICK_CONTACT".equals(intent.getAction())) && (data.startsWith("content://contacts/people/") || data.startsWith("content://com.android.contacts/contacts/lookup/"))) {
                                    Intent intent2 = new Intent("com.android.contacts.action.QUICK_CONTACT");
                                    intent2.addFlags(268468224);
                                    intent2.putExtra("com.android.launcher.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION", true);
                                    intent2.setData(uri);
                                    intent2.setDataAndType(uri, intent2.resolveType(this.mContext));
                                    ContentValues values = new ContentValues();
                                    values.put(LauncherSettings.BaseLauncherColumns.INTENT, intent2.toUri(0));
                                    db.update(TAG_FAVORITES, values, "_id=" + favoriteId, (String[]) null);
                                }
                            }
                        } catch (RuntimeException ex) {
                            Log.e(LauncherProvider.TAG, "Problem upgrading shortcut", ex);
                        } catch (URISyntaxException e) {
                            Log.e(LauncherProvider.TAG, "Problem upgrading shortcut", e);
                        }
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                if (c2 != null) {
                    c2.close();
                }
                return true;
            } catch (SQLException ex2) {
                Log.w(LauncherProvider.TAG, "Problem while upgrading contacts", ex2);
                db.endTransaction();
                if (c != null) {
                    c.close();
                }
                return LauncherProvider.LOGD;
            } catch (Throwable th) {
                db.endTransaction();
                if (c != null) {
                    c.close();
                }
                throw th;
            }
        }

        private void normalizeIcons(SQLiteDatabase db) {
            Log.d(LauncherProvider.TAG, "normalizing icons");
            db.beginTransaction();
            Cursor c = null;
            SQLiteStatement update = null;
            boolean logged = LauncherProvider.LOGD;
            try {
                update = db.compileStatement("UPDATE favorites SET icon=? WHERE _id=?");
                c = db.rawQuery("SELECT _id, icon FROM favorites WHERE iconType=1", (String[]) null);
                int idIndex = c.getColumnIndexOrThrow("_id");
                int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.BaseLauncherColumns.ICON);
                while (c.moveToNext()) {
                    long id = c.getLong(idIndex);
                    byte[] data = c.getBlob(iconIndex);
                    try {
                        Bitmap bitmap = Utilities.resampleIconBitmap(BitmapFactory.decodeByteArray(data, 0, data.length), this.mContext);
                        if (bitmap != null) {
                            update.bindLong(1, id);
                            byte[] data2 = ItemInfo.flattenBitmap(bitmap);
                            if (data2 != null) {
                                update.bindBlob(2, data2);
                                update.execute();
                            }
                            bitmap.recycle();
                        }
                    } catch (Exception e) {
                        if (!logged) {
                            Log.e(LauncherProvider.TAG, "Failed normalizing icon " + id, e);
                        } else {
                            Log.e(LauncherProvider.TAG, "Also failed normalizing icon " + id);
                        }
                        logged = true;
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                if (update != null) {
                    update.close();
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLException ex) {
                Log.w(LauncherProvider.TAG, "Problem while allocating appWidgetIds for existing widgets", ex);
                db.endTransaction();
                if (update != null) {
                    update.close();
                }
                if (c != null) {
                    c.close();
                }
            } catch (Throwable th) {
                db.endTransaction();
                if (update != null) {
                    update.close();
                }
                if (c != null) {
                    c.close();
                }
                throw th;
            }
        }

        public long generateNewId() {
            if (this.mMaxId < 0) {
                throw new RuntimeException("Error: max id was not initialized");
            }
            this.mMaxId++;
            return this.mMaxId;
        }

        private long initializeMaxId(SQLiteDatabase db) {
            Cursor c = db.rawQuery("SELECT MAX(_id) FROM favorites", (String[]) null);
            long id = -1;
            if (c != null && c.moveToNext()) {
                id = c.getLong(0);
            }
            if (c != null) {
                c.close();
            }
            if (id != -1) {
                return id;
            }
            throw new RuntimeException("Error: could not query max id");
        }

        private void convertWidgets(SQLiteDatabase db) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.mContext);
            String selectWhere = LauncherProvider.buildOrWhereString(LauncherSettings.BaseLauncherColumns.ITEM_TYPE, new int[]{1000, 1002, 1001});
            Cursor c = null;
            db.beginTransaction();
            try {
                c = db.query(TAG_FAVORITES, new String[]{"_id", LauncherSettings.BaseLauncherColumns.ITEM_TYPE}, selectWhere, (String[]) null, (String) null, (String) null, (String) null);
                ContentValues values = new ContentValues();
                while (c != null && c.moveToNext()) {
                    long favoriteId = c.getLong(0);
                    int favoriteType = c.getInt(1);
                    try {
                        int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
                        values.clear();
                        values.put(LauncherSettings.BaseLauncherColumns.ITEM_TYPE, 4);
                        values.put("appWidgetId", Integer.valueOf(appWidgetId));
                        if (favoriteType == 1001) {
                            values.put("spanX", 4);
                            values.put("spanY", 1);
                        } else {
                            values.put("spanX", 2);
                            values.put("spanY", 2);
                        }
                        db.update(TAG_FAVORITES, values, "_id=" + favoriteId, (String[]) null);
                        if (favoriteType == 1000) {
                            appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, new ComponentName("com.android.alarmclock", "com.android.alarmclock.AnalogAppWidgetProvider"));
                        } else if (favoriteType == 1002) {
                            appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, new ComponentName("com.android.camera", "com.android.camera.PhotoAppWidgetProvider"));
                        } else if (favoriteType == 1001) {
                            appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, getSearchWidgetProvider());
                        }
                    } catch (RuntimeException ex) {
                        Log.e(LauncherProvider.TAG, "Problem allocating appWidgetId", ex);
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                if (c != null) {
                    c.close();
                }
            } catch (SQLException ex2) {
                Log.w(LauncherProvider.TAG, "Problem while allocating appWidgetIds for existing widgets", ex2);
                db.endTransaction();
                if (c != null) {
                    c.close();
                }
            } catch (Throwable th) {
                db.endTransaction();
                if (c != null) {
                    c.close();
                }
                throw th;
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:6:0x000c  */
        /* JADX WARNING: Removed duplicated region for block: B:8:0x0014  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private static final void beginDocument(org.xmlpull.v1.XmlPullParser r4, java.lang.String r5) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
            /*
                r2 = 2
            L_0x0001:
                int r0 = r4.next()
                if (r0 == r2) goto L_0x000a
                r1 = 1
                if (r0 != r1) goto L_0x0001
            L_0x000a:
                if (r0 == r2) goto L_0x0014
                org.xmlpull.v1.XmlPullParserException r1 = new org.xmlpull.v1.XmlPullParserException
                java.lang.String r2 = "No start tag found"
                r1.<init>(r2)
                throw r1
            L_0x0014:
                java.lang.String r1 = r4.getName()
                boolean r1 = r1.equals(r5)
                if (r1 != 0) goto L_0x0041
                org.xmlpull.v1.XmlPullParserException r1 = new org.xmlpull.v1.XmlPullParserException
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                java.lang.String r3 = "Unexpected start tag: found "
                r2.<init>(r3)
                java.lang.String r3 = r4.getName()
                java.lang.StringBuilder r2 = r2.append(r3)
                java.lang.String r3 = ", expected "
                java.lang.StringBuilder r2 = r2.append(r3)
                java.lang.StringBuilder r2 = r2.append(r5)
                java.lang.String r2 = r2.toString()
                r1.<init>(r2)
                throw r1
            L_0x0041:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.launcher2.LauncherProvider.DatabaseHelper.beginDocument(org.xmlpull.v1.XmlPullParser, java.lang.String):void");
        }

        /* access modifiers changed from: private */
        public int loadFavoritesOwn(SQLiteDatabase db) {
            Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
            intent.addCategory("android.intent.category.LAUNCHER");
            ContentValues values = new ContentValues();
            PackageManager packageManager = this.mContext.getPackageManager();
            int allAppsButtonRank = DefaultWorkspace.hotseat_all_apps_index;
            int i = 0;
            Iterator<DefaultWorkspace.Favorite> it = DefaultWorkspace.mFavo.iterator();
            while (it.hasNext()) {
                DefaultWorkspace.Favorite fa = it.next();
                boolean added = LauncherProvider.LOGD;
                String name = fa.name;
                if (fa.container == -101 && fa.mS == allAppsButtonRank) {
                    throw new RuntimeException("Invalid screen position for hotseat item");
                }
                values.clear();
                values.put("container", Integer.valueOf(fa.container));
                values.put("screen", Integer.valueOf(fa.mS));
                values.put("cellX", Integer.valueOf(fa.mX));
                values.put("cellY", Integer.valueOf(fa.mY));
                if (TAG_FAVORITE.equals(name)) {
                    added = addAppShortcutOwn(db, fa, values, packageManager, intent) >= 0 ? true : LauncherProvider.LOGD;
                } else if (TAG_APPWIDGET.equals(name)) {
                    added = addAppWidgetOwn(db, fa, values, packageManager);
                }
                if (added) {
                    i++;
                }
            }
            return i;
        }

        private long addAppShortcutOwn(SQLiteDatabase db, DefaultWorkspace.Favorite fa, ContentValues values, PackageManager packageManager, Intent intent) {
            ComponentName cn;
            ActivityInfo info;
            long id = -1;
            String packageName = fa.packageName;
            String className = fa.className;
            try {
                cn = new ComponentName(packageName, className);
                info = packageManager.getActivityInfo(cn, 0);
                try {
                    id = generateNewId();
                    intent.setComponent(cn);
                    intent.setFlags(270532608);
                    values.put(LauncherSettings.BaseLauncherColumns.INTENT, intent.toUri(0));
                    values.put(LauncherSettings.BaseLauncherColumns.TITLE, info.loadLabel(packageManager).toString());
                    values.put(LauncherSettings.BaseLauncherColumns.ITEM_TYPE, 0);
                    values.put("spanX", 1);
                    values.put("spanY", 1);
                    values.put("_id", Long.valueOf(generateNewId()));
                    if (LauncherProvider.dbInsertAndCheck(this, db, TAG_FAVORITES, (String) null, values) < 0) {
                        return -1;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w(LauncherProvider.TAG, "Unable to add favorite: " + packageName + "/" + className, e);
                }
            } catch (PackageManager.NameNotFoundException e2) {
                cn = new ComponentName(packageManager.currentToCanonicalPackageNames(new String[]{packageName})[0], className);
                info = packageManager.getActivityInfo(cn, 0);
            }
            return id;
        }

        private boolean addAppWidgetOwn(SQLiteDatabase db, DefaultWorkspace.Favorite fa, ContentValues values, PackageManager packageManager) {
            String packageName = fa.packageName;
            String className = fa.className;
            if (packageName == null || className == null) {
                values.put(LauncherSettings.BaseLauncherColumns.ITEM_TYPE, 4);
                values.put("spanX", Integer.valueOf(fa.spanX));
                values.put("spanY", Integer.valueOf(fa.spanY));
                values.put("appWidgetId", -1);
                values.put("_id", Long.valueOf(generateNewId()));
                long unused = LauncherProvider.dbInsertAndCheck(this, db, TAG_FAVORITES, (String) null, values);
                return true;
            }
            boolean hasPackage = true;
            ComponentName cn = new ComponentName(packageName, className);
            try {
                packageManager.getReceiverInfo(cn, 0);
            } catch (Exception e) {
                cn = new ComponentName(packageManager.currentToCanonicalPackageNames(new String[]{packageName})[0], className);
                try {
                    packageManager.getReceiverInfo(cn, 0);
                } catch (Exception e2) {
                    hasPackage = LauncherProvider.LOGD;
                }
            }
            if (!hasPackage) {
                return LauncherProvider.LOGD;
            }
            return addAppWidget(db, values, cn, fa.spanX, fa.spanY, (Bundle) null);
        }

        /* access modifiers changed from: private */
        public int loadFavorites(SQLiteDatabase db, int workspaceResourceId) {
            String title;
            Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
            intent.addCategory("android.intent.category.LAUNCHER");
            ContentValues values = new ContentValues();
            PackageManager packageManager = this.mContext.getPackageManager();
            int allAppsButtonRank = this.mContext.getResources().getInteger(R.integer.hotseat_all_apps_index);
            int i = 0;
            try {
                XmlResourceParser parser = this.mContext.getResources().getXml(workspaceResourceId);
                AttributeSet attrs = Xml.asAttributeSet(parser);
                beginDocument(parser, TAG_FAVORITES);
                int depth = parser.getDepth();
                loop0:
                while (true) {
                    int type = parser.next();
                    if ((type == 3 && parser.getDepth() <= depth) || type == 1) {
                        break;
                    } else if (type == 2) {
                        boolean added = LauncherProvider.LOGD;
                        String name = parser.getName();
                        TypedArray a = this.mContext.obtainStyledAttributes(attrs, R.styleable.Favorite);
                        long container = -100;
                        if (a.hasValue(2)) {
                            container = Long.valueOf(a.getString(2)).longValue();
                        }
                        String screen = a.getString(3);
                        String x = a.getString(4);
                        String y = a.getString(5);
                        if (container == -101 && Integer.valueOf(screen).intValue() == allAppsButtonRank) {
                            throw new RuntimeException("Invalid screen position for hotseat item");
                        }
                        values.clear();
                        values.put("container", Long.valueOf(container));
                        values.put("screen", screen);
                        values.put("cellX", x);
                        values.put("cellY", y);
                        if (TAG_FAVORITE.equals(name)) {
                            added = addAppShortcut(db, values, a, packageManager, intent) >= 0 ? true : LauncherProvider.LOGD;
                        } else if (TAG_SEARCH.equals(name)) {
                            added = addSearchWidget(db, values);
                        } else if (TAG_CLOCK.equals(name)) {
                            added = addClockWidget(db, values);
                        } else if (TAG_APPWIDGET.equals(name)) {
                            added = addAppWidget(parser, attrs, type, db, values, a, packageManager);
                        } else if (TAG_SHORTCUT.equals(name)) {
                            added = addUriShortcut(db, values, a) >= 0 ? true : LauncherProvider.LOGD;
                        } else if (TAG_FOLDER.equals(name)) {
                            int titleResId = a.getResourceId(9, -1);
                            if (titleResId != -1) {
                                title = this.mContext.getResources().getString(titleResId);
                            } else {
                                title = this.mContext.getResources().getString(R.string.folder_name);
                            }
                            values.put(LauncherSettings.BaseLauncherColumns.TITLE, title);
                            long folderId = addFolder(db, values);
                            added = folderId >= 0 ? true : LauncherProvider.LOGD;
                            ArrayList<Long> folderItems = new ArrayList<>();
                            int folderDepth = parser.getDepth();
                            while (true) {
                                int type2 = parser.next();
                                if (type2 != 3 || parser.getDepth() > folderDepth) {
                                    if (type2 == 2) {
                                        String folder_item_name = parser.getName();
                                        TypedArray ar = this.mContext.obtainStyledAttributes(attrs, R.styleable.Favorite);
                                        values.clear();
                                        values.put("container", Long.valueOf(folderId));
                                        if (TAG_FAVORITE.equals(folder_item_name) && folderId >= 0) {
                                            long id = addAppShortcut(db, values, ar, packageManager, intent);
                                            if (id >= 0) {
                                                folderItems.add(Long.valueOf(id));
                                            }
                                        } else if (TAG_SHORTCUT.equals(folder_item_name) && folderId >= 0) {
                                            long id2 = addUriShortcut(db, values, ar);
                                            if (id2 >= 0) {
                                                folderItems.add(Long.valueOf(id2));
                                            }
                                        }
                                        ar.recycle();
                                    }
                                } else if (folderItems.size() < 2 && folderId >= 0) {
                                    LauncherProvider.deleteId(db, folderId);
                                    if (folderItems.size() > 0) {
                                        LauncherProvider.deleteId(db, folderItems.get(0).longValue());
                                    }
                                    added = LauncherProvider.LOGD;
                                }
                            }
                        }
                        if (added) {
                            i++;
                        }
                        a.recycle();
                    }
                }
                throw new RuntimeException("Folders can contain only shortcuts");
            } catch (XmlPullParserException e) {
                Log.w(LauncherProvider.TAG, "Got exception parsing favorites.", e);
            } catch (IOException e2) {
                Log.w(LauncherProvider.TAG, "Got exception parsing favorites.", e2);
            } catch (RuntimeException e3) {
                Log.w(LauncherProvider.TAG, "Got exception parsing favorites.", e3);
            }
            return i;
        }

        private long addAppShortcut(SQLiteDatabase db, ContentValues values, TypedArray a, PackageManager packageManager, Intent intent) {
            ComponentName cn;
            ActivityInfo info;
            long id = -1;
            String packageName = a.getString(1);
            String className = a.getString(0);
            try {
                cn = new ComponentName(packageName, className);
                info = packageManager.getActivityInfo(cn, 0);
            } catch (PackageManager.NameNotFoundException e) {
                cn = new ComponentName(packageManager.currentToCanonicalPackageNames(new String[]{packageName})[0], className);
                info = packageManager.getActivityInfo(cn, 0);
            }
            try {
                id = generateNewId();
                intent.setComponent(cn);
                intent.setFlags(270532608);
                values.put(LauncherSettings.BaseLauncherColumns.INTENT, intent.toUri(0));
                values.put(LauncherSettings.BaseLauncherColumns.TITLE, info.loadLabel(packageManager).toString());
                values.put(LauncherSettings.BaseLauncherColumns.ITEM_TYPE, 0);
                values.put("spanX", 1);
                values.put("spanY", 1);
                values.put("_id", Long.valueOf(generateNewId()));
                if (LauncherProvider.dbInsertAndCheck(this, db, TAG_FAVORITES, (String) null, values) < 0) {
                    return -1;
                }
            } catch (PackageManager.NameNotFoundException e2) {
                Log.w(LauncherProvider.TAG, "Unable to add favorite: " + packageName + "/" + className, e2);
            }
            return id;
        }

        private long addFolder(SQLiteDatabase db, ContentValues values) {
            values.put(LauncherSettings.BaseLauncherColumns.ITEM_TYPE, 2);
            values.put("spanX", 1);
            values.put("spanY", 1);
            long id = generateNewId();
            values.put("_id", Long.valueOf(id));
            if (LauncherProvider.dbInsertAndCheck(this, db, TAG_FAVORITES, (String) null, values) <= 0) {
                return -1;
            }
            return id;
        }

        private ComponentName getSearchWidgetProvider() {
            ComponentName searchComponent = ((SearchManager) this.mContext.getSystemService(TAG_SEARCH)).getGlobalSearchActivity();
            if (searchComponent == null) {
                return null;
            }
            return getProviderInPackage(searchComponent.getPackageName());
        }

        private ComponentName getProviderInPackage(String packageName) {
            List<AppWidgetProviderInfo> providers = AppWidgetManager.getInstance(this.mContext).getInstalledProviders();
            if (providers == null) {
                return null;
            }
            int providerCount = providers.size();
            for (int i = 0; i < providerCount; i++) {
                ComponentName provider = providers.get(i).provider;
                if (provider != null && provider.getPackageName().equals(packageName)) {
                    return provider;
                }
            }
            return null;
        }

        private boolean addSearchWidget(SQLiteDatabase db, ContentValues values) {
            return addAppWidget(db, values, getSearchWidgetProvider(), 4, 1, (Bundle) null);
        }

        private boolean addClockWidget(SQLiteDatabase db, ContentValues values) {
            return addAppWidget(db, values, new ComponentName("com.android.alarmclock", "com.android.alarmclock.AnalogAppWidgetProvider"), 2, 2, (Bundle) null);
        }

        private boolean addAppWidget(XmlResourceParser parser, AttributeSet attrs, int type, SQLiteDatabase db, ContentValues values, TypedArray a, PackageManager packageManager) throws XmlPullParserException, IOException {
            String packageName = a.getString(1);
            String className = a.getString(0);
            if (packageName == null || className == null) {
                return LauncherProvider.LOGD;
            }
            boolean hasPackage = true;
            ComponentName cn = new ComponentName(packageName, className);
            try {
                packageManager.getReceiverInfo(cn, 0);
            } catch (Exception e) {
                cn = new ComponentName(packageManager.currentToCanonicalPackageNames(new String[]{packageName})[0], className);
                try {
                    packageManager.getReceiverInfo(cn, 0);
                } catch (Exception e2) {
                    hasPackage = LauncherProvider.LOGD;
                }
            }
            if (!hasPackage) {
                return LauncherProvider.LOGD;
            }
            int spanX = a.getInt(6, 0);
            int spanY = a.getInt(7, 0);
            Bundle extras = new Bundle();
            int widgetDepth = parser.getDepth();
            while (true) {
                int type2 = parser.next();
                if (type2 == 3 && parser.getDepth() <= widgetDepth) {
                    return addAppWidget(db, values, cn, spanX, spanY, extras);
                }
                if (type2 == 2) {
                    TypedArray ar = this.mContext.obtainStyledAttributes(attrs, R.styleable.Extra);
                    if (TAG_EXTRA.equals(parser.getName())) {
                        String key = ar.getString(0);
                        String value = ar.getString(1);
                        if (key != null && value != null) {
                            extras.putString(key, value);
                            ar.recycle();
                        }
                    } else {
                        throw new RuntimeException("Widgets can contain only extras");
                    }
                }
            }
            throw new RuntimeException("Widget extras must have a key and value");
        }

        private boolean addAppWidget(SQLiteDatabase db, ContentValues values, ComponentName cn, int spanX, int spanY, Bundle extras) {
            boolean allocatedAppWidgets = LauncherProvider.LOGD;
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.mContext);
            try {
                int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
                values.put(LauncherSettings.BaseLauncherColumns.ITEM_TYPE, 4);
                values.put("spanX", Integer.valueOf(spanX));
                values.put("spanY", Integer.valueOf(spanY));
                values.put("appWidgetId", Integer.valueOf(appWidgetId));
                values.put("_id", Long.valueOf(generateNewId()));
                long unused = LauncherProvider.dbInsertAndCheck(this, db, TAG_FAVORITES, (String) null, values);
                allocatedAppWidgets = true;
                appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, cn);
                if (extras != null && !extras.isEmpty()) {
                    Intent intent = new Intent(LauncherProvider.ACTION_APPWIDGET_DEFAULT_WORKSPACE_CONFIGURE);
                    intent.setComponent(cn);
                    intent.putExtras(extras);
                    intent.putExtra("appWidgetId", appWidgetId);
                    this.mContext.sendBroadcast(intent);
                }
            } catch (RuntimeException ex) {
                Log.e(LauncherProvider.TAG, "Problem allocating appWidgetId", ex);
            }
            return allocatedAppWidgets;
        }

        private long addUriShortcut(SQLiteDatabase db, ContentValues values, TypedArray a) {
            Resources r = this.mContext.getResources();
            int iconResId = a.getResourceId(8, 0);
            int titleResId = a.getResourceId(9, 0);
            try {
                String uri = a.getString(10);
                if (uri == null) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(a.getString(1), a.getString(0)));
                    uri = intent.toUri(0);
                }
                Intent intent2 = Intent.parseUri(uri, 0);
                if (iconResId == 0 || titleResId == 0) {
                    Log.w(LauncherProvider.TAG, "Shortcut is missing title or icon resource ID");
                    return -1;
                }
                long id = generateNewId();
                intent2.setFlags(268435456);
                values.put(LauncherSettings.BaseLauncherColumns.INTENT, intent2.toUri(0));
                values.put(LauncherSettings.BaseLauncherColumns.TITLE, r.getString(titleResId));
                values.put(LauncherSettings.BaseLauncherColumns.ITEM_TYPE, 1);
                values.put("spanX", 1);
                values.put("spanY", 1);
                values.put(LauncherSettings.BaseLauncherColumns.ICON_TYPE, 0);
                values.put(LauncherSettings.BaseLauncherColumns.ICON_PACKAGE, this.mContext.getPackageName());
                values.put(LauncherSettings.BaseLauncherColumns.ICON_RESOURCE, r.getResourceName(iconResId));
                values.put("_id", Long.valueOf(id));
                if (LauncherProvider.dbInsertAndCheck(this, db, TAG_FAVORITES, (String) null, values) < 0) {
                    return -1;
                }
                return id;
            } catch (URISyntaxException e) {
                Log.w(LauncherProvider.TAG, "Shortcut has malformed uri: " + null);
                return -1;
            }
        }
    }

    static String buildOrWhereString(String column, int[] values) {
        StringBuilder selectWhere = new StringBuilder();
        for (int i = values.length - 1; i >= 0; i--) {
            selectWhere.append(column).append("=").append(values[i]);
            if (i > 0) {
                selectWhere.append(" OR ");
            }
        }
        return selectWhere.toString();
    }

    static class SqlArguments {
        public final String[] args;
        public final String table;
        public final String where;

        SqlArguments(Uri url, String where2, String[] args2) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where2;
                this.args = args2;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where2)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            } else {
                this.table = url.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(url);
                this.args = null;
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = null;
                this.args = null;
                return;
            }
            throw new IllegalArgumentException("Invalid URI: " + url);
        }
    }
}
