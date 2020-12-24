package com.android.launcher2;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.launcher.R;
import com.forfan.carassist.IWeatherService;
import com.forfan.carassist.WeatherData;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WeatherView extends RelativeLayout implements View.OnClickListener {
    private static final String TAG = "WeatherView";
    private boolean bUserdefBg = false;
    private Button mBtnRefresh;
    /* access modifiers changed from: private */
    public String mCityId;
    private TextView mCityTxt;
    private final int mCityTxtId;
    private final int mCurTempId;
    private TextView mCurrentTemp;
    private final int mIconId;
    private final int mRefreshId;
    private final int mTempId;
    private final int mTypeTextId;
    private TextView mUpdateTxt;
    private final int mUpdateTxtId;
    private WeatherData mWeatherData;
    private ImageView mWeatherImage;
    BroadcastReceiver mWeatherReciver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String type;
            Log.d(WeatherView.TAG, "mWeatherReciver recived!!");
            Bundle bundle = intent.getExtras();
            if (bundle != null && (type = bundle.getString("type")) != null) {
                if (type.equalsIgnoreCase("cityId")) {
                    String tmp = bundle.getString("value", (String) null);
                    if (tmp != null && !tmp.equalsIgnoreCase(WeatherView.this.mCityId)) {
                        WeatherView.this.mCityId = tmp;
                    }
                    WeatherView.this.updateWeatherDisplay();
                }
                if (type.equals("WeatherResult")) {
                    String cityId = bundle.getString("CityId");
                    if (WeatherView.this.mCityId != null && cityId.equals(WeatherView.this.mCityId) && bundle.getBoolean("Result", false)) {
                        WeatherView.this.updateWeatherDisplay();
                    }
                }
                if (type.equals("HistoryLoaded")) {
                    WeatherView.this.updateWeatherDisplay();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public IWeatherService mWeatherService;
    private TextView mWeatherTemp;
    private TextView mWeatherType;
    private Context m_Context;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction().toString();
            if (!action.equals("android.intent.action.SCREEN_OFF")) {
                WeatherView.this.bindWeatherService();
            }
            Log.i(WeatherView.TAG, "BroadcastReceiver recived " + action);
        }
    };
    private ServiceConnection weatherSconn = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(WeatherView.TAG, "onServiceDisconnected");
            WeatherView.this.mWeatherService = null;
        }

        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            Log.d(WeatherView.TAG, "onServiceConnected");
            WeatherView.this.mWeatherService = IWeatherService.Stub.asInterface(binder);
            try {
                WeatherView.this.mCityId = WeatherView.this.mWeatherService.getLMCityId();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            WeatherView.this.updateWeatherDisplay();
        }
    };

    private int getViewId(TypedArray a, int styleableId, String str) {
        int groupid = 0;
        if (isInEditMode() || (groupid = a.getResourceId(styleableId, 0)) != 0) {
            return groupid;
        }
        throw new IllegalArgumentException("The " + str + " attribute is required and must refer " + "to a valid child.");
    }

    public WeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.m_Context = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WeatherView);
        this.mIconId = getViewId(a, 1, "weather_image");
        this.mTypeTextId = getViewId(a, 3, "weather_txt");
        this.mTempId = getViewId(a, 4, "temperature_txt");
        this.mCurTempId = getViewId(a, 2, "current_temp");
        this.mCityTxtId = getViewId(a, 6, "location_txt");
        this.mUpdateTxtId = getViewId(a, 7, "update_time");
        this.mRefreshId = getViewId(a, 5, "btn_refresh");
        int resId = a.getResourceId(8, -1);
        if (!isInEditMode() && resId != R.drawable.main_weather_sunny_bg) {
            this.bUserdefBg = true;
        }
        a.recycle();
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        Log.d(TAG, "onFinishInflate");
        super.onFinishInflate();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            Log.d(TAG, "!isInEditMode()");
            registerScreenActionReceiver();
            registWeatherReciver();
            bindWeatherService();
            setupWeatherViews();
            updateWeatherDisplay();
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow");
        this.m_Context.unregisterReceiver(this.receiver);
        this.m_Context.unregisterReceiver(this.mWeatherReciver);
        this.m_Context.unbindService(this.weatherSconn);
        super.onDetachedFromWindow();
    }

    private void setupWeatherViews() {
        this.mWeatherImage = (ImageView) findViewById(this.mIconId);
        this.mWeatherType = (TextView) findViewById(this.mTypeTextId);
        this.mCurrentTemp = (TextView) findViewById(this.mCurTempId);
        this.mWeatherTemp = (TextView) findViewById(this.mTempId);
        this.mCityTxt = (TextView) findViewById(this.mCityTxtId);
        this.mUpdateTxt = (TextView) findViewById(this.mUpdateTxtId);
        this.mBtnRefresh = (Button) findViewById(this.mRefreshId);
        if (this.mBtnRefresh != null) {
            this.mBtnRefresh.setOnClickListener(this);
        }
    }

    /* access modifiers changed from: private */
    public void bindWeatherService() {
        Intent intent = new Intent();
        intent.setAction(LibWorkspace.WEATHER_SERVICE);
        this.m_Context.bindService(intent, this.weatherSconn, 1);
    }

    /* access modifiers changed from: package-private */
    public void registWeatherReciver() {
        IntentFilter filter = new IntentFilter("com.ts.weather.WEATHER_CHANGE");
        filter.addAction("com.ts.weather.GET_WEATHER_RESULT");
        this.m_Context.registerReceiver(this.mWeatherReciver, filter);
    }

    private void registerScreenActionReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.SCREEN_ON");
        this.m_Context.registerReceiver(this.receiver, filter);
    }

    public void onClick(View v) {
        if (v.getId() == this.mRefreshId && this.mWeatherService != null) {
            try {
                this.mWeatherService.getWeatherByCityId(this.mCityId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private int getWeatherIconByName(String weather) {
        return this.m_Context.getResources().getIdentifier("weather_icon_" + weather, "drawable", this.m_Context.getResources().getResourcePackageName(R.drawable.weather_icon_qing));
    }

    /* access modifiers changed from: protected */
    public void updateWeatherDisplay() {
        if (this.mWeatherService != null) {
            try {
                List<WeatherData> list = this.mWeatherService.getWeatherList(this.mCityId);
                if (list != null) {
                    int index = 0;
                    Time tmpTime = new Time();
                    tmpTime.setToNow();
                    Date now = new Date(tmpTime.year - 1900, tmpTime.month, tmpTime.monthDay);
                    long timeDistance = -1;
                    for (int i = 0; i < list.size(); i++) {
                        try {
                            long tmp = Math.abs(new SimpleDateFormat("yyyy-MM-dd").parse(list.get(i).date).getTime() - now.getTime());
                            if (i == 0) {
                                timeDistance = tmp;
                            }
                            if (tmp <= timeDistance) {
                                index = i;
                                timeDistance = tmp;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    this.mWeatherData = list.get(index);
                    this.mCityTxt.setText(this.mWeatherData.city);
                    if (this.mWeatherTemp != null) {
                        this.mWeatherTemp.setText(String.valueOf(this.mWeatherData.lowtemp) + "~" + this.mWeatherData.hightemp + "℃");
                    }
                    if (this.mWeatherType != null) {
                        this.mWeatherType.setText(this.mWeatherData.weatherStr);
                    }
                    if (!this.bUserdefBg) {
                        if (this.mWeatherData.weatherStr.contains("雪")) {
                            setBackgroundResource(R.drawable.main_weather_snowy_bg);
                        } else if (this.mWeatherData.weatherStr.contains("雨")) {
                            setBackgroundResource(R.drawable.main_weather_rainy_bg);
                        } else if (this.mWeatherData.weatherStr.contains("阴")) {
                            setBackgroundResource(R.drawable.main_weather_cloudy_bg);
                        } else {
                            setBackgroundResource(R.drawable.main_weather_sunny_bg);
                        }
                    }
                    if (this.mCurrentTemp != null) {
                        if (this.mWeatherData.curTemp == null || this.mWeatherData.curTemp.equals("null")) {
                            this.mCurrentTemp.setText("--℃");
                        } else {
                            this.mCurrentTemp.setText(String.valueOf(this.mWeatherData.curTemp) + "℃");
                        }
                    }
                    if (this.mUpdateTxt != null) {
                        this.mUpdateTxt.setText(this.mWeatherData.updateTime.substring(5));
                    }
                    if (this.mWeatherImage != null) {
                        this.mWeatherImage.setBackgroundResource(getWeatherIconByName(this.mWeatherData.weather));
                    }
                }
            } catch (RemoteException e2) {
                e2.printStackTrace();
            }
        }
    }
}
