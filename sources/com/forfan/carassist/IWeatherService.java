package com.forfan.carassist;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;
import java.util.Map;

public interface IWeatherService extends IInterface {
    Map getIndexMap(String str, String str2) throws RemoteException;

    String getLMCityId() throws RemoteException;

    void getWeatherByCityId(String str) throws RemoteException;

    List<WeatherData> getWeatherList(String str) throws RemoteException;

    public static abstract class Stub extends Binder implements IWeatherService {
        private static final String DESCRIPTOR = "com.forfan.carassist.IWeatherService";
        static final int TRANSACTION_getIndexMap = 2;
        static final int TRANSACTION_getLMCityId = 4;
        static final int TRANSACTION_getWeatherByCityId = 1;
        static final int TRANSACTION_getWeatherList = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWeatherService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWeatherService)) {
                return new Proxy(obj);
            }
            return (IWeatherService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    getWeatherByCityId(data.readString());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    Map _result = getIndexMap(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeMap(_result);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    List<WeatherData> _result2 = getWeatherList(data.readString());
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    String _result3 = getLMCityId();
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        private static class Proxy implements IWeatherService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void getWeatherByCityId(String id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Map getIndexMap(String cityId, String date) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cityId);
                    _data.writeString(date);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readHashMap(getClass().getClassLoader());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<WeatherData> getWeatherList(String cityId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cityId);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(WeatherData.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getLMCityId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
