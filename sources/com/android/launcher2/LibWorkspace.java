package com.android.launcher2;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;
import com.android.launcher.R;
import com.android.launcher2.DefaultWorkspace;
import com.yyw.ts90xhw.IconCfg;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LibWorkspace extends DefaultWorkspace {
    protected static final String TAG = "Workspace";
    public static String WEATHER_SERVICE = "com.forfan.carassist.WeatherService";
    protected static boolean firstview_ignore = false;
    public static ArrayList<DefaultWorkspace.Favorite> mAllAppsFavo = new ArrayList<>();
    public static Context mContext;
    private static LibWorkspace myWorkspace = new LibWorkspace();
    protected static int userdef_widget_sx = 0;
    protected static int userdef_widget_sy = 0;
    protected static int userdef_widget_x = 0;
    protected static int userdef_widget_y = 0;
    protected boolean addWorkspaceIcon = true;
    int freeCnt = -1;
    List<Integer> freeList = null;
    protected int[] iconBuf = new int[50];
    protected View mAllAppBtn = null;
    protected Launcher mLauncher = null;
    int pageNo = 0;
    private SparseArray<List<Integer>> positionMap = new SparseArray<>();
    private SharedPreferences sp;

    public static LibWorkspace GetInstance() {
        return myWorkspace;
    }

    public int getCurrentPage() {
        return mWorkspace.getCurrentPage();
    }

    public int getPageCount() {
        return mWorkspace.getPageCount();
    }

    public void setupViews(Launcher launcher) {
        this.mLauncher = launcher;
    }

    public void setupAllapp(BubbleTextView allapp, int pos) {
        if (allapp != null && pos >= 0) {
            this.mAllAppBtn = allapp;
            if (this.mLauncher != null) {
                View qsbbar = this.mLauncher.findViewById(R.id.qsb_bar);
                if (qsbbar != null) {
                    View apps = qsbbar.findViewById(R.id.allapps_button);
                    int allapptext = this.mLauncher.getResources().getInteger(R.integer.allapptext);
                    if (!(apps == null || allapptext == 0)) {
                        ((TextView) apps).setText(R.string.all_apps_button_label);
                    }
                    int[] position = new int[2];
                    CellLayout layout = this.mLauncher.getHotseat().getLayout();
                    layout.getLocationInWindow(position);
                    Log.d(TAG, "position x = " + position[0] + " y = " + position[1]);
                    int[] tmp = new int[2];
                    layout.cellToPoint(pos, 0, tmp);
                    qsbbar.setX((float) (position[0] + tmp[0]));
                }
                allapp.setText("");
            }
        }
    }

    public int[] getIconBuf() {
        return this.iconBuf;
    }

    public boolean isHaveIcon(int iconId) {
        for (int i : this.iconBuf) {
            if (iconId == i) {
                return true;
            }
        }
        return false;
    }

    public void SetContext(Context context) {
        mContext = context;
        startCommService(mContext);
    }

    private void startCommService(Context context) {
        Intent intent = new Intent();
        intent.setPackage("com.ts.MainUI");
        intent.setAction("android.intent.action.MAIN_UI");
        context.startService(intent);
    }

    public String getIconName(int iconId) throws PackageManager.NameNotFoundException {
        if (iconId < 0 || iconId >= MainIconDef.packageStr.length) {
            return null;
        }
        String packageName = MainIconDef.packageStr[iconId][0];
        String className = MainIconDef.packageStr[iconId][1];
        PackageManager packageManager = mContext.getPackageManager();
        return packageManager.getActivityInfo(new ComponentName(packageName, className), 0).loadLabel(packageManager).toString();
    }

    public void loadDefaultWallPaper(Context context) {
        if (context != null) {
            Log.d("####", "loadDefaultWallPaper in");
            WallpaperManager wpm = WallpaperManager.getInstance(context);
            try {
                wpm.setResource(context.getResources().getIdentifier("wallpaper_01", "drawable", context.getResources().getResourcePackageName(R.array.wallpapers)));
                Log.d("####", "loadDefaultWallPaper wallpaper_01");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "context == null");
        }
    }

    private boolean checkDefault(Context context) {
        File file = new File(context.getFilesDir() + "/forfandefault.bin");
        Log.d("####", "checkDefault file.exists() = " + file.exists());
        try {
            if (!file.exists()) {
                file.createNewFile();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Boolean bLoadDefault(Context context) {
        boolean bLoadDefault = false;
        int nResult = IconCfg.GetIconCfg(this.iconBuf);
        if (context != null) {
            hotseat_all_apps_index = context.getResources().getInteger(R.integer.hotseat_all_apps_index);
            cell_count_x = context.getResources().getInteger(R.integer.cell_count_x);
            cell_count_y = context.getResources().getInteger(R.integer.cell_count_y);
            hotseat_cell_count_x = context.getResources().getInteger(R.integer.hotseat_cell_count_x);
            hotseat_cell_count_y = context.getResources().getInteger(R.integer.hotseat_cell_count_y);
        }
        Log.d(TAG, "IconCfg.GetIconCfg = " + nResult);
        for (int i = 0; i < this.iconBuf.length; i++) {
            Log.d(TAG, "IconCfg[" + i + "] = " + this.iconBuf[i]);
        }
        if (nResult == 2) {
            loadDefaultWallPaper(context);
            bLoadDefault = true;
        }
        if (checkDefault(context)) {
            Log.d("####", "need to load default");
            loadDefaultWallPaper(context);
            bLoadDefault = true;
        }
        return Boolean.valueOf(bLoadDefault);
    }

    private void clearPositionMap() {
        this.pageNo = 0;
        this.freeCnt = -1;
        this.freeList = null;
        mAllAppsFavo.clear();
        this.positionMap.clear();
    }

    public void initView() {
        mFavo.clear();
        clearPositionMap();
        placeDefaultView();
        calculateOccupied();
        placeHotseat();
        placeWorkspace();
        mAllAppsFavo.addAll(mFavo);
    }

    private void calculateOccupied() {
        Iterator it = mFavo.iterator();
        while (it.hasNext()) {
            DefaultWorkspace.Favorite favorite = (DefaultWorkspace.Favorite) it.next();
            int screen = favorite.mS;
            int sizeX = favorite.spanX;
            int sizeY = favorite.spanY;
            if (!(sizeX == 0 || sizeY == 0)) {
                int posX = favorite.mX;
                int posY = favorite.mY;
                if (posX >= 0 && posY >= 0) {
                    List<Integer> freePosition = this.positionMap.get(screen);
                    if (freePosition == null) {
                        freePosition = new ArrayList<>();
                        for (int i = 0; i < cell_count_y; i++) {
                            for (int j = 0; j < cell_count_x; j++) {
                                freePosition.add(Integer.valueOf((j << 4) | i));
                            }
                        }
                        this.positionMap.put(screen, freePosition);
                    }
                    if (posX + sizeX > cell_count_x || posY + sizeY > cell_count_y) {
                        throw new IllegalArgumentException("favorite position error, x = " + posX + " y = " + posY + " SizeX = " + sizeX + " SizeY = " + sizeY);
                    }
                    for (int i2 = posX; i2 < posX + sizeX; i2++) {
                        int j2 = posY;
                        while (j2 < posY + sizeY) {
                            int pos = (i2 << 4) | j2;
                            if (freePosition.contains(Integer.valueOf(pos))) {
                                freePosition.remove(Integer.valueOf(pos));
                                j2++;
                            } else {
                                throw new IllegalArgumentException("favorite position error, position occupied by other widget " + i2 + " " + j2);
                            }
                        }
                    }
                    continue;
                }
            }
        }
    }

    public void placeDefaultView() {
        if (!firstview_ignore) {
            mFavo.add(new DefaultWorkspace.Favorite((String) null, (String) null, userdef_widget_x, userdef_widget_y, userdef_widget_sx, userdef_widget_sy));
        }
    }

    private int[] getEmptyPosition() {
        if (this.freeCnt == 0) {
            this.pageNo++;
        }
        if (this.freeCnt <= 0) {
            do {
                this.freeList = this.positionMap.get(this.pageNo);
                if (this.freeList == null) {
                    this.freeCnt = cell_count_x * cell_count_y;
                } else {
                    this.freeCnt = this.freeList.size();
                    if (this.freeCnt == 0) {
                        this.pageNo++;
                    }
                }
            } while (this.freeCnt == 0);
        }
        int posX = ((cell_count_x * cell_count_y) - this.freeCnt) % cell_count_x;
        int posY = (((cell_count_x * cell_count_y) - this.freeCnt) / cell_count_x) % cell_count_y;
        if (this.freeList != null) {
            int positon = this.freeList.get(0).intValue();
            posX = positon >> 4;
            posY = positon & 15;
            this.freeList.remove(0);
        }
        this.freeCnt--;
        return new int[]{posX, posY};
    }

    private void placeWorkspace() {
        int hotseatNum = hotseat_cell_count_y * hotseat_cell_count_x;
        if (hotseat_all_apps_index >= 0 && hotseat_all_apps_index < hotseatNum) {
            hotseatNum--;
        }
        if (hotseatNum <= 0) {
            hotseatNum = 0;
        }
        int pageNo2 = 0;
        int freeCnt2 = 0;
        List<Integer> freeList2 = null;
        for (int iconCnt = hotseatNum; iconCnt < MainIconDef.packageStr.length; iconCnt++) {
            int icon = this.iconBuf[iconCnt] - 1;
            if (icon >= 0 && icon < MainIconDef.packageStr.length) {
                if (freeCnt2 <= 0) {
                    do {
                        freeList2 = this.positionMap.get(pageNo2);
                        if (freeList2 == null) {
                            freeCnt2 = cell_count_x * cell_count_y;
                            continue;
                        } else {
                            freeCnt2 = freeList2.size();
                            if (freeCnt2 == 0) {
                                pageNo2++;
                                continue;
                            } else {
                                continue;
                            }
                        }
                    } while (freeCnt2 == 0);
                }
                int posX = ((cell_count_x * cell_count_y) - freeCnt2) % cell_count_x;
                int posY = (((cell_count_x * cell_count_y) - freeCnt2) / cell_count_x) % cell_count_y;
                if (freeList2 != null) {
                    int positon = freeList2.get(0).intValue();
                    posX = positon >> 4;
                    posY = positon & 15;
                    freeList2.remove(0);
                }
                if (this.addWorkspaceIcon) {
                    mFavo.add(new DefaultWorkspace.Favorite(MainIconDef.packageStr[icon][0], MainIconDef.packageStr[icon][1], pageNo2, posX, posY));
                } else {
                    mAllAppsFavo.add(new DefaultWorkspace.Favorite(MainIconDef.packageStr[icon][0], MainIconDef.packageStr[icon][1], pageNo2, posX, posY));
                }
                freeCnt2--;
                if (freeCnt2 == 0) {
                    pageNo2++;
                }
            }
        }
    }

    public void placeHotseat() {
        int hotseatCnt;
        int hotseatNum = hotseat_cell_count_y * hotseat_cell_count_x;
        if (hotseatNum > 0) {
            int nIconCnt = 0;
            int hotseatCnt2 = 0;
            while (hotseatCnt2 < hotseat_all_apps_index && nIconCnt < this.iconBuf.length) {
                int iconId = this.iconBuf[nIconCnt] - 1;
                if (iconId >= 0 && iconId < MainIconDef.packageStr.length) {
                    mFavo.add(new DefaultWorkspace.Favorite(MainIconDef.packageStr[iconId][0], MainIconDef.packageStr[iconId][1], hotseatCnt2 % hotseat_cell_count_x, hotseatCnt2 / hotseat_cell_count_x));
                }
                hotseatCnt2++;
                nIconCnt++;
            }
            if (hotseatCnt2 <= hotseatNum) {
                if (hotseat_all_apps_index < 0) {
                    hotseatCnt = 0;
                } else {
                    hotseatCnt = hotseat_all_apps_index + 1;
                }
                while (hotseatCnt < hotseatNum && nIconCnt < this.iconBuf.length) {
                    int iconId2 = this.iconBuf[nIconCnt] - 1;
                    if (iconId2 >= 0 && iconId2 < MainIconDef.packageStr.length) {
                        mFavo.add(new DefaultWorkspace.Favorite(MainIconDef.packageStr[iconId2][0], MainIconDef.packageStr[iconId2][1], hotseatCnt % hotseat_cell_count_x, hotseatCnt / hotseat_cell_count_x));
                    }
                    hotseatCnt++;
                    nIconCnt++;
                }
            }
        }
    }

    public void startIconActivity(int iconId) {
        if (iconId >= 0 && iconId < MainIconDef.packageStr.length) {
            try {
                String packageName = MainIconDef.packageStr[iconId][0];
                String className = MainIconDef.packageStr[iconId][1];
                Intent intent = new Intent();
                intent.addFlags(268435456);
                intent.setComponent(new ComponentName(packageName, className));
                mContext.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
