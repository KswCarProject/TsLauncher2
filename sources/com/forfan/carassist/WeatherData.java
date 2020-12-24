package com.forfan.carassist;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;

public class WeatherData implements Serializable, Parcelable {
    public static final Parcelable.Creator<WeatherData> CREATOR = new Parcelable.Creator<WeatherData>() {
        public WeatherData createFromParcel(Parcel source) {
            return new WeatherData(source, (WeatherData) null);
        }

        public WeatherData[] newArray(int size) {
            return new WeatherData[size];
        }
    };
    private static final long serialVersionUID = 1;
    public String aqi;
    public String city;
    public String curTemp;
    public String date;
    public String fengli;
    public String fengxiang;
    public String hightemp;
    public String lowtemp;
    public String updateTime;
    public String weather;
    public String weatherStr;
    public String week;

    public WeatherData() {
    }

    public int describeContents() {
        return 0;
    }

    private WeatherData(Parcel source) {
        readFromParcel(source);
    }

    /* synthetic */ WeatherData(Parcel parcel, WeatherData weatherData) {
        this(parcel);
    }

    public void readFromParcel(Parcel source) {
        this.date = source.readString();
        this.week = source.readString();
        this.weather = source.readString();
        this.weatherStr = source.readString();
        this.fengxiang = source.readString();
        this.fengli = source.readString();
        this.hightemp = source.readString();
        this.lowtemp = source.readString();
        this.curTemp = source.readString();
        this.aqi = source.readString();
        this.updateTime = source.readString();
        this.city = source.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.date);
        dest.writeString(this.week);
        dest.writeString(this.weather);
        dest.writeString(this.weatherStr);
        dest.writeString(this.fengxiang);
        dest.writeString(this.fengli);
        dest.writeString(this.hightemp);
        dest.writeString(this.lowtemp);
        dest.writeString(this.curTemp);
        dest.writeString(this.aqi);
        dest.writeString(this.updateTime);
        dest.writeString(this.city);
    }
}
