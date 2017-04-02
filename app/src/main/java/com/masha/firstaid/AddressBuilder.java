package com.masha.firstaid;


import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//этот класс будет получать текущие координаты и превращать их в строку адреса
public class AddressBuilder {

    private Context context;
    public StringBuffer addressText = new StringBuffer();
    LocationManager locationManager;

    AddressBuilder(Context context) {
        this.context = context;
        onCreate();
    }

    private void onCreate() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //явно проверяем, что нужное разрешение прописано в манифесте
        //может надо еще проверить ACCESS_FINE_LOCATION ??
        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            //вешаем слушателя
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    //время обновления координат в миллисекундах
                    100,
                    //расстояние обновления координат в метрах
                    10,
                    locationListener);

            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 100, 10,
                    locationListener);

        }
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            getAddress(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private void getAddress(Location location) {
        Log.d("MyTags", "метод getAdress");
        double currentLat = location.getLatitude();
        double currentLong = location.getLongitude();

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        //этот метод лучше сделать в отдельном потоке
        try {
            List<Address> list = (ArrayList<Address>) geocoder.getFromLocation(currentLat, currentLong, 1);
            Address ad;
            if (!list.isEmpty()) {
                ad = list.get(0);
                //очищаем и заполняем новым адресом
                addressText.delete(0, addressText.capacity() - 1);
                addressText.append(ad.getLocality());
                addressText.append(", ");
                addressText.append(ad.getAddressLine(0));
                //   address.setText(addressText);
            } else {
                Log.d("MyTags", "Адрес не определен");
            }
        }
        //if latitude is less than -90 or greater than 90
        // OR if longitude is less than -180 or greater than 180
        catch (IllegalArgumentException illEx) {
            Log.d("MyTags", "Получены некорректные координаты");
            illEx.printStackTrace();
        }
        //if the network is unavailable or any other I/O problem occurs
        catch (IOException IOEx) {
            Log.d("MyTags", "Нет доступа к интернету");
            IOEx.printStackTrace();
        } catch (Exception ex) {
            Log.d("MyTags", "Что-то пошло не так...");
            ex.printStackTrace();
        }
    }

    public void close() {
        //отключаем слушателя
        locationManager.removeUpdates(locationListener);
    }

}


