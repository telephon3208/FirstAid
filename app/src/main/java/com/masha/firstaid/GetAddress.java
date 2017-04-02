package com.masha.firstaid;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;



//этот класс будет получать текущие координаты и превращать их в строку адреса
class GetAddress {

    private LocationManager locationManager;
    private Geocoder geocoder;
    private Context context;
    private StringBuffer addressText;

    GetAddress(Context context) {
        this.context = context;
        onCreate();
    }

    private void onCreate() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        addressText = new StringBuffer();
        geocoder = new Geocoder(context, Locale.getDefault());

        //явно проверяем, что нужное разрешение прописано в манифесте
        //может надо еще проверить ACCESS_FINE_LOCATION ??
        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            //вешаем слушателя
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    //время обновления координат в миллисекундах
                    1000 * 10,
                    //расстояние обновления координат в метрах
                    10,
                    locationListener);

            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                    locationListener);

        }

        checkEnabled();
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
      //      showLocation(location);
            getAddress(location);

        }

        @Override
        public void onProviderDisabled(String provider) {
            //выводит на экран работает ли сервис
      //      checkEnabled();
        }

        @Override
        public void onProviderEnabled(String provider) {
            //выводит на экран работает ли сервис
         //   checkEnabled();
            //явно проверяем, что нужное разрешение прописано в манифесте
            //может надо еще проверить ACCESS_FINE_LOCATION ??
            if (ContextCompat.checkSelfPermission(context,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                showLocation(locationManager.getLastKnownLocation(provider));
        }

        //int: OUT_OF_SERVICE if the provider is out of service, and this is not expected
        // to change in the near future; TEMPORARILY_UNAVAILABLE if the provider is
        // temporarily unavailable but is expected to be available shortly;
        // and AVAILABLE if the provider is currently available.
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
/*            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                tvStatusGPS.setText("Status: " + String.valueOf(status));
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                tvStatusNet.setText("Status: " + String.valueOf(status));
            }*/
        }
    };

    private void showLocation(Location location) {
       /* if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            tvLocationGPS.setText(formatLocation(location));
        } else if (location.getProvider().equals(
                LocationManager.NETWORK_PROVIDER)) {
            tvLocationNet.setText(formatLocation(location));
        }*/
    }

    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()));
    }

    private void checkEnabled() {
        /*tvEnabledGPS.setText("Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER));
        tvEnabledNet.setText("Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER));*/
    }

    private void getAddress(Location location){
        double currentLat = location.getLatitude();
        double currentLong = location.getLongitude();

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
        }
        catch (Exception ex) {
            Log.d("MyTags", "Что-то пошло не так...");
            ex.printStackTrace();
        }



    }

    public String address() {
        return addressText.toString();
    }

    public void close() {
        //отключаем слушателя
        locationManager.removeUpdates(locationListener);
    }

}
