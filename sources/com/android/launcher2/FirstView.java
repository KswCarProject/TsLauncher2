package com.android.launcher2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.launcher.R;
import com.yyw.ts90xhw.KeyDef;
import java.util.ArrayList;
import java.util.List;

public class FirstView extends FrameLayout implements View.OnClickListener, View.OnLongClickListener {
    private boolean isHasCarPlay;
    private boolean isHasRadio;
    /* access modifiers changed from: private */
    public boolean isScrollToLeft;
    private int[] mIvIconDns;
    private int[] mIvIconUps;
    private ImageView[] mIvItems;
    private int[] mIvLtIconDns;
    private int[] mIvLtIconUps;
    private int[] mIvRtIconDns;
    private int[] mIvRtIconUps;
    /* access modifiers changed from: private */
    public int mLastPage;
    /* access modifiers changed from: private */
    public List<View> mPageList;
    private int mSelectedIndex;
    private ViewPager mViewPager;

    public FirstView(Context context) {
        this(context, (AttributeSet) null);
    }

    public FirstView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FirstView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mPageList = new ArrayList();
        this.mIvIconUps = new int[]{R.drawable.mainmenu_icon_radio_up, R.drawable.mainmenu_icon_music_up, R.drawable.mainmenu_icon_navi_up, R.drawable.mainmenu_icon_bluetooth_up, R.drawable.mainmenu_icon_set_up, R.drawable.mainmenu_icon_carplay_up, R.drawable.mainmenu_icon_app_up, R.drawable.mainmenu_icon_guan_up, R.drawable.mainmenu_icon_cheliang_up, R.drawable.mainmenu_icon_xingchediannao_up};
        this.mIvIconDns = new int[]{R.drawable.mainmenu_icon_radio_dn, R.drawable.mainmenu_icon_music_dn, R.drawable.mainmenu_icon_navi_dn, R.drawable.mainmenu_icon_bluetooth_dn, R.drawable.mainmenu_icon_set_dn, R.drawable.mainmenu_icon_carplay_dn, R.drawable.mainmenu_icon_app_dn, R.drawable.mainmenu_icon_guan_dn, R.drawable.mainmenu_icon_cheliang_dn, R.drawable.mainmenu_icon_xingchediannao_dn};
        this.mIvLtIconUps = new int[]{R.drawable.main_bottom_play_up, R.drawable.main_bottom_prev_up, R.drawable.main_bottom_ding_up, R.drawable.main_bottom_bt_up, R.drawable.main_bottom_wifi_up, R.drawable.main_bottom_iphone_up, R.drawable.main_bottom_w_up, R.drawable.main_bottom_cha_up, R.drawable.main_bottom_set_up, R.drawable.main_bottom_eq_up};
        this.mIvLtIconDns = new int[]{R.drawable.main_bottom_play_dn, R.drawable.main_bottom_prev_dn, R.drawable.main_bottom_ding_dn, R.drawable.main_bottom_bt_dn, R.drawable.main_bottom_wifi_dn, R.drawable.main_bottom_iphone_dn, R.drawable.main_bottom_w_dn, R.drawable.main_bottom_cha_dn, R.drawable.main_bottom_set_dn, R.drawable.main_bottom_eq_dn};
        this.mIvRtIconUps = new int[]{R.drawable.main_bottom_vol_up, R.drawable.main_bottom_next_up, R.drawable.main_bottom_jia_up, R.drawable.main_bottom_book_up, R.drawable.main_bottom_i01_up, R.drawable.main_bottom_anzhuo_up, R.drawable.main_bottom_ie_up, R.drawable.main_bottom_close_up, R.drawable.main_bottom_i02_up, R.drawable.main_bottom_pan_up};
        this.mIvRtIconDns = new int[]{R.drawable.main_bottom_vol_dn, R.drawable.main_bottom_next_dn, R.drawable.main_bottom_jia_dn, R.drawable.main_bottom_book_dn, R.drawable.main_bottom_i01_dn, R.drawable.main_bottom_anzhuo_dn, R.drawable.main_bottom_ie_dn, R.drawable.main_bottom_close_dn, R.drawable.main_bottom_i02_dn, R.drawable.main_bottom_pan_dn};
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.isHasCarPlay = MyWorkspace.GetInstance().isHaveIcon(30);
        this.isHasRadio = MyWorkspace.GetInstance().isHaveIcon(15);
        Log.d("HAHA", "isHasRadio = " + this.isHasRadio);
        int count = 10;
        if (!this.isHasCarPlay && !this.isHasRadio) {
            count = 8;
        } else if (!this.isHasRadio || !this.isHasCarPlay) {
            count = 9;
        }
        this.mIvItems = new ImageView[count];
        initData();
        initUI();
    }

    public void cancelSelectedIndex() {
        if (this.mSelectedIndex >= 0 && this.mSelectedIndex < this.mIvItems.length) {
            this.mIvItems[this.mSelectedIndex].setSelected(false);
        }
    }

    private void selectCurrentIndex(int index) {
        if (index >= 0 && index < this.mIvItems.length) {
            cancelSelectedIndex();
            this.mIvItems[index].setSelected(true);
            this.mIvItems[index].requestFocus();
            this.mSelectedIndex = index;
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int iconIndex;
        int page = this.mViewPager.getCurrentItem();
        switch (event.getKeyCode()) {
            case 19:
            case 21:
                if (event.getAction() == 0) {
                    return true;
                }
                if (page == 0 && this.mSelectedIndex % 5 == 0) {
                    return true;
                }
                if (this.mSelectedIndex % 5 == 0) {
                    this.isScrollToLeft = true;
                    this.mViewPager.setCurrentItem(page - 1, true);
                    return true;
                }
                selectCurrentIndex(this.mSelectedIndex - 1);
                return true;
            case 20:
            case 22:
                if (event.getAction() == 0) {
                    return true;
                }
                if (page == this.mPageList.size() - 1 && this.mSelectedIndex == this.mIvItems.length - 1) {
                    return true;
                }
                if (this.mSelectedIndex % 5 == 4) {
                    this.mViewPager.setCurrentItem(page + 1, true);
                    return true;
                }
                selectCurrentIndex(this.mSelectedIndex + 1);
                return true;
            case 23:
            case KeyDef.PKEY_UP /*66*/:
                if (event.getAction() == 0) {
                    return true;
                }
                if (this.isHasRadio || this.isHasCarPlay) {
                    iconIndex = !this.isHasRadio ? this.mSelectedIndex + 1 : !this.isHasCarPlay ? this.mSelectedIndex < 5 ? this.mSelectedIndex : this.mSelectedIndex + 1 : this.mSelectedIndex;
                } else {
                    iconIndex = this.mSelectedIndex < 4 ? this.mSelectedIndex + 1 : this.mSelectedIndex + 2;
                }
                clickIndexItem(iconIndex);
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    private void initData() {
        addPageView(0);
        addPageView(1);
    }

    private void addPageView(int pageIndex) {
        LinearLayout pageView = (LinearLayout) View.inflate(getContext(), R.layout.page_item, (ViewGroup) null);
        int i = 0;
        while (i < 5) {
            View item = View.inflate(getContext(), R.layout.item_layout, (ViewGroup) null);
            item.setLayoutParams(new LinearLayout.LayoutParams(-2, -1, 1.0f));
            item.setOnClickListener(this);
            item.setOnLongClickListener(this);
            pageView.addView(item);
            if ((pageIndex * 5) + i < this.mIvItems.length) {
                ImageView ivLtIcon = (ImageView) item.findViewById(R.id.iv_lt_icon);
                ImageView ivRtIcon = (ImageView) item.findViewById(R.id.iv_rt_icon);
                TextView tvName = (TextView) item.findViewById(R.id.tv_name);
                int index = (pageIndex * 5) + i;
                if (index < this.mIvItems.length) {
                    this.mIvItems[index] = (ImageView) item.findViewById(R.id.iv_icon);
                }
                int iconIndex = index;
                if (pageIndex == 0) {
                    if (this.isHasRadio || this.isHasCarPlay) {
                        iconIndex = !this.isHasRadio ? index + 1 : index;
                    } else {
                        iconIndex = i == 4 ? index + 2 : index + 1;
                    }
                }
                if (pageIndex == 1) {
                    if (!this.isHasRadio && !this.isHasCarPlay) {
                        iconIndex = index + 2;
                    } else if (!this.isHasRadio || !this.isHasCarPlay) {
                        iconIndex = index + 1;
                    } else {
                        iconIndex = index;
                    }
                }
                item.setTag(Integer.valueOf(iconIndex));
                if (index < this.mIvItems.length) {
                    this.mIvItems[index].setImageDrawable(getStateDrawable(this.mIvIconUps[iconIndex], this.mIvIconDns[iconIndex]));
                    ivLtIcon.setImageDrawable(getStateDrawable(this.mIvLtIconUps[iconIndex], this.mIvLtIconDns[iconIndex]));
                    ivRtIcon.setImageDrawable(getStateDrawable(this.mIvRtIconUps[iconIndex], this.mIvRtIconDns[iconIndex]));
                }
                switch (iconIndex) {
                    case 0:
                        setSystemName(tvName, 14);
                        break;
                    case 1:
                        setSystemName(tvName, 5);
                        break;
                    case 2:
                        setSystemName(tvName, 0);
                        break;
                    case 3:
                        setSystemName(tvName, 17);
                        break;
                    case 4:
                        setAppName(tvName, "com.android.settings");
                        break;
                    case 5:
                        tvName.setText(R.string.car_play);
                        break;
                    case 6:
                        tvName.setText(R.string.all_apps_button_label);
                        break;
                    case 7:
                        tvName.setText(R.string.off_screen);
                        break;
                    case 8:
                        setSystemName(tvName, 21);
                        break;
                    case 9:
                        tvName.setText(R.string.driving_computer);
                        break;
                }
            } else {
                item.setVisibility(4);
            }
            i++;
        }
        pageView.setOnLongClickListener(this);
        this.mPageList.add(pageView);
    }

    private void setAppName(TextView tvName, String packageName) {
        try {
            PackageManager pm = getContext().getPackageManager();
            tvName.setText((String) pm.getPackageInfo(packageName, 0).applicationInfo.loadLabel(pm));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setSystemName(TextView tvName, int index) {
        try {
            ComponentName cn = new ComponentName(MainIconDef.packageStr[index][0], MainIconDef.packageStr[index][1]);
            PackageManager packageManager = getContext().getPackageManager();
            tvName.setText(packageManager.getActivityInfo(cn, 0).loadLabel(packageManager).toString());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Drawable getStateDrawable(int upId, int dnId) {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{16842919}, ContextCompat.getDrawable(getContext(), dnId));
        drawable.addState(new int[]{16842913}, ContextCompat.getDrawable(getContext(), dnId));
        drawable.addState(new int[]{16842908}, ContextCompat.getDrawable(getContext(), dnId));
        drawable.addState(new int[0], ContextCompat.getDrawable(getContext(), upId));
        return drawable;
    }

    public void initUI() {
        this.mViewPager = (ViewPager) findViewById(R.id.vp_show);
        PagerAdapter mPagerAdapter = new PagerAdapter() {
            public int getCount() {
                return FirstView.this.mPageList.size();
            }

            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            public void destroyItem(View arg0, int arg1, Object arg2) {
                ((ViewPager) arg0).removeView((View) FirstView.this.mPageList.get(arg1));
            }

            public Object instantiateItem(View arg0, int arg1) {
                ((ViewPager) arg0).addView((View) FirstView.this.mPageList.get(arg1));
                return FirstView.this.mPageList.get(arg1);
            }
        };
        ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
            public void onPageSelected(int position) {
                if (position == FirstView.this.mLastPage) {
                    FirstView.this.isScrollToLeft = true;
                }
                FirstView.this.updateSelectedPage(position);
            }

            public void onPageScrolled(int position, float arg1, int arg2) {
                FirstView.this.mLastPage = position;
            }

            public void onPageScrollStateChanged(int arg0) {
            }
        };
        this.mViewPager.setAdapter(mPagerAdapter);
        this.mViewPager.setOnPageChangeListener(pageChangeListener);
        selectCurrentIndex(0);
    }

    /* access modifiers changed from: private */
    public void updateSelectedPage(int position) {
        cancelSelectedIndex();
        if (this.isScrollToLeft) {
            selectCurrentIndex((position * 5) + 4);
        } else {
            selectCurrentIndex((position * 5) + 0);
        }
        this.isScrollToLeft = false;
    }

    private void setWallpaper() {
        getContext().startActivity(Intent.createChooser(new Intent("android.intent.action.SET_WALLPAPER"), getResources().getText(R.string.chooser_wallpaper)));
    }

    public boolean onLongClick(View v) {
        setWallpaper();
        return true;
    }

    public void onClick(View v) {
        clickIndexItem(((Integer) v.getTag()).intValue());
    }

    private void clickIndexItem(int iconIndex) {
        switch (iconIndex) {
            case 0:
                MyWorkspace.GetInstance().startIconActivity(14);
                break;
            case 1:
                MyWorkspace.GetInstance().startIconActivity(5);
                break;
            case 2:
                MyWorkspace.GetInstance().startIconActivity(0);
                break;
            case 3:
                MyWorkspace.GetInstance().startIconActivity(17);
                break;
            case 4:
                startActivity("com.android.settings", "com.android.settings.Settings");
                break;
            case 5:
                if (!startActivity("cn.manstep.phonemirrorBox", "cn.manstep.phonemirrorBox.MainActivity")) {
                    startActivity("cn.manstep.phonemirrorBox.CarPlay.carplay", "cn.manstep.phonemirrorBox.MainActivity");
                    break;
                }
                break;
            case 6:
                MyWorkspace.GetInstance().onBtnAllAppClick();
                break;
            case 7:
                MyWorkspace.GetInstance().sendPowerKey();
                break;
            case 8:
                MyWorkspace.GetInstance().startIconActivity(21);
                break;
            case 9:
                startActivity("com.ts.MainUI", "com.ts.main.benz.DrivingComputeActivity");
                break;
        }
        int index = iconIndex;
        if (!this.isHasRadio && !this.isHasCarPlay) {
            index = iconIndex < 5 ? iconIndex - 1 : iconIndex - 2;
        } else if (!this.isHasRadio) {
            index = iconIndex - 1;
        } else if (!this.isHasCarPlay) {
            index = iconIndex > 5 ? iconIndex - 1 : iconIndex;
        }
        selectCurrentIndex(index);
    }

    private boolean startActivity(String packageName, String className) {
        try {
            Intent intent = new Intent();
            intent.addFlags(335544320);
            intent.setComponent(new ComponentName(packageName, className));
            getContext().startActivity(intent);
            return true;
        } catch (Exception e) {
            Log.d("HAHA", "exception = " + e.getMessage());
            return false;
        }
    }
}
