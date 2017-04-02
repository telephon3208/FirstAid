package com.masha.firstaid;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private boolean isStartedGPSsettings = false;
    private AddressBuilder addressBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addressBuilder = new AddressBuilder(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Вызвать скорую помощь?", Snackbar.LENGTH_LONG)
                Snackbar.make(view, "Call an emergency?", Snackbar.LENGTH_LONG)
                        .setAction("Yes", snackbarOnClickListener).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isStartedGPSsettings) {
            isStartedGPSsettings = false;
            showAddress();
        }
    }

    View.OnClickListener snackbarOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Уточнить адрес?")
                 //   .setMessage("Покормите кота!")
                //    .setIcon(R.drawable.ic_android_cat)
                    .setCancelable(false);
            builder.setPositiveButton("Да",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    //если GPS отключен, то показываем диалог
                                    checkingGPS();
                                }
                            });
            builder.setNegativeButton("Нет",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    callAnEmergency();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();

        }
    };

    private void callAnEmergency() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:112"));
        startActivity(intent);
    }

    private void checkingGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showAddress();
        } else {
            showGPSTurnOnDialog();
        }
    }

    //показываем диалоговое окно с адресом
   private void showAddress() {

       AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
       builder.setTitle("Вы находитесь здесь")
               .setMessage(getAddress())
               //    .setIcon(R.drawable.ic_android_cat)
               .setCancelable(false);
       builder.setPositiveButton("OK",
               new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       dialog.cancel();
                       callAnEmergency();
                   }
               });

       AlertDialog alert = builder.create();
       alert.show();
   }

    //показываем диалоговое окно с вопросом включить ли GPS
   private void showGPSTurnOnDialog() {
       AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
       builder.setTitle("Включить GPS?")
                  .setMessage("Это позволит точнее определить адрес.")
               //    .setIcon(R.drawable.ic_android_cat)
               .setCancelable(false);
       builder.setPositiveButton("Да",
               new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       dialog.cancel();
                       isStartedGPSsettings = true;
                       startActivity(new Intent(
                               android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                   }
               });
       builder.setNegativeButton("Нет",
               new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       dialog.cancel();
                       showAddress();
                   }
               });
       AlertDialog alert = builder.create();
       alert.show();
   }

   private String getAddress() {

       StringBuffer s;
     //  do {
           s = addressBuilder.addressText;
     //  } while (s.toString().isEmpty());
       addressBuilder.close();
       return s.toString();
   }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
