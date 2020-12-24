package com.android.launcher2;

import android.view.View;
import java.util.ArrayList;

public class DefaultWorkspace {
    public static int cell_count_x = 5;
    public static int cell_count_y = 2;
    public static int hotseat_all_apps_index = 0;
    public static int hotseat_cell_count_x = 5;
    public static int hotseat_cell_count_y = 1;
    public static ArrayList<Favorite> mFavo = new ArrayList<>();
    public static Boolean mOnResumeAllapp = false;
    public static Workspace mWorkspace = null;
    public static View myfirstview = null;
    public static int noscroll = 0;

    public static void SetWorkspace(Workspace wk) {
        mWorkspace = wk;
    }

    public void ScrollHome() {
    }

    public void Scroll(CellLayout cl, float scrollProgress) {
    }

    public class Favorite {
        String className;
        int container;
        int mS;
        int mX;
        int mY;
        String name;
        String packageName;
        int spanX;
        int spanY;

        public Favorite(String pkName, String csName, int x, int y, int sX, int sY) {
            this.name = "appwidget";
            this.packageName = pkName;
            this.className = csName;
            this.container = -100;
            this.mS = 0;
            this.mX = x;
            this.mY = y;
            this.spanX = sX;
            this.spanY = sY;
        }

        public Favorite(String pkName, String csName, int x, int y, int sX, int sY, int s) {
            this.name = "appwidget";
            this.packageName = pkName;
            this.className = csName;
            this.container = -100;
            this.mS = s;
            this.mX = x;
            this.mY = y;
            this.spanX = sX;
            this.spanY = sY;
        }

        public Favorite(String pkName, String csName, int s, int x, int y) {
            this.name = "favorite";
            this.packageName = pkName;
            this.className = csName;
            this.container = -100;
            this.mS = s;
            this.mX = x;
            this.mY = y;
        }

        public Favorite(String pkName, String csName, int x, int y) {
            this.name = "favorite";
            this.packageName = pkName;
            this.className = csName;
            this.container = -101;
            this.mS = x;
            this.mX = x;
            this.mY = y;
        }
    }
}
