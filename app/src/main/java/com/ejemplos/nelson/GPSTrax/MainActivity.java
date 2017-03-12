package com.ejemplos.nelson.GPSTrax;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.UUID;

//import static com.ejemplos.nelson.GPSTrax.R.id.map;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "GPSTrax";
    private String defaultUploadWebsite;
    private Button trackingButton;
    private boolean currentlyTracking;
    private int intervalInMinutes = 1;
    private AlarmManager alarmManager;
    private Intent gpsTrackerIntent;
    private PendingIntent pendingIntent;
    public String imei=null;
    public static Context contextOfApplication;
    public boolean registroHecho=false;
    public String strNombreEequipo=null;
    public String strCorreoCuenta=null;
    public String strUsuario=null;
    public String strClave=null;
    TextView textView_nombreEquipo;
    TextView textView_imei;
    TextView textView_longitud;
    TextView textView_latitud;
    TextView textView_exactitud;
    TextView textView_fechaHora;
    int prescaler=0;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contextOfApplication = getApplicationContext();



        Log.d(TAG,"Starting Aplication GPSTrax.....");
        Log.d(TAG,"Created by Nelson Rodriguez 01/02/2017");
        Log.d(TAG,"Revisando si esta habilitado el posicionamiento...");

        textView_nombreEquipo=(TextView)findViewById(R.id.textView_nombreEquipo);
        textView_imei=(TextView)findViewById(R.id.textView_imei);
        textView_longitud=(TextView)findViewById(R.id.textView_longitud);
        textView_latitud=(TextView)findViewById(R.id.textView_latitud);
        textView_exactitud=(TextView)findViewById(R.id.textView_exactitud);
        textView_fechaHora=(TextView)findViewById(R.id.textView_fechaHora);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);


        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Log.d(TAG,"GPS esta activo!!!");
            //Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
        }else{
            Log.d(TAG,"GPS no esta activo!!!");
            Toast.makeText(getApplicationContext(),"Debe habilitar el GPS!!!",Toast.LENGTH_SHORT).show();
            showGPSDisabledAlertToUser();
        }
        defaultUploadWebsite = getString(R.string.default_upload_website);
        trackingButton = (Button)findViewById(R.id.trackingButton);
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
        currentlyTracking = sharedPreferences.getBoolean("currentlyTracking", false);
        Log.d(TAG,"currentlyTracking(1): "+currentlyTracking);
        registroHecho=sharedPreferences.getBoolean("registroHecho", false);
        Log.d(TAG,"registroHecho inicio: "+registroHecho);

        //registroHecho=false;
        if (!registroHecho){
            Log.d(TAG,"Abriendo activity de registro");
            Intent i=new Intent(this,Register.class);
            startActivity(i);
        }else {
            Log.d(TAG,"Leyendo datos de usuario e imei:");
            strNombreEequipo=sharedPreferences.getString("nombreEquipo",null);
            strCorreoCuenta=sharedPreferences.getString("correoCuenta",null);
            strUsuario=sharedPreferences.getString("usuario",null);
            strClave=sharedPreferences.getString("clave",null);
            imei=sharedPreferences.getString("imei",null);
            Log.d(TAG,"strNombreEequipo: "+strNombreEequipo);
            Log.d(TAG,"strCorreoCuenta: "+strCorreoCuenta);
            Log.d(TAG,"strUsuario: "+strUsuario);
            Log.d(TAG,"strClave: "+strClave);
            Log.d(TAG,"imei: "+imei);
        }






        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d(TAG,"Guardando alarmState false");
        editor.putBoolean("alarmState",false);
        Log.d(TAG,"Guardando intervalo fijo de 1 minuto...");
        editor.putInt("intervalInMinutes", 1);
        editor.apply();
        Log.d(TAG,"currentlyTracking 1: "+currentlyTracking);
        boolean firstTimeLoadingApp = sharedPreferences.getBoolean("firstTimeLoadingApp", true);
        Log.d(TAG,"firstTimeLoadingApp 1: "+firstTimeLoadingApp);
        if (firstTimeLoadingApp) {
            editor = sharedPreferences.edit();
            editor.putBoolean("firstTimeLoadingApp", false);
            editor.putString("appID",  UUID.randomUUID().toString());
            editor.apply();
        }

        trackingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                trackLocation(view);
            }
        });
        Log.d(TAG,"Activando countdown timer");


        new CountDownTimer(30000, 5000) {

            public void onTick(long millisUntilFinished) {
                double latitud;
                double longitud;
                String nombreEquipo;
                //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                //here you can have your logic to set text to edittext
                Log.d(TAG,"onTick hecho");
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
                textView_nombreEquipo.setText("Nombre Equipo: "+sharedPreferences.getString("nombreEquipo",null));
                textView_imei.setText("IMEI: "+sharedPreferences.getString("imei",null));
                textView_latitud.setText("Latitud: "+sharedPreferences.getFloat("latitud",0));
                textView_longitud.setText("Longitud: "+sharedPreferences.getFloat("longitude",0));
                textView_exactitud.setText("Exactitud: "+sharedPreferences.getFloat("accuracy",0)+" m");
                textView_fechaHora.setText("Fecha y Hora ultimo reporte: "+sharedPreferences.getString("fechaHora","NA"));
                prescaler=0;
                latitud=sharedPreferences.getFloat("latitud",0);
                longitud=sharedPreferences.getFloat("longitude",0);
                nombreEquipo=sharedPreferences.getString("nombreEquipo",null);

                LatLng posicion = new LatLng(latitud, longitud);
                mMap.addMarker(new MarkerOptions().position(posicion).title(nombreEquipo));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
            }

            public void onFinish() {
                Log.d(TAG,"onFinish hecho");
                this.start();
            }

        }.start();



    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS esta deshabilitado, para el funcionamiento de esta aplicacion debe habilitarlo!")
                .setCancelable(false)
                .setPositiveButton("Habilitar el GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancelar",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    public void startAlarmManager() {
        Log.d(TAG, "startAlarmManager");

        Context context = getBaseContext();
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        gpsTrackerIntent = new Intent(context, GpsTrackerAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
        intervalInMinutes = sharedPreferences.getInt("intervalInMinutes", 1);

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                intervalInMinutes * 60000, // 60000 = 1 minute
                pendingIntent);
    }

    private void cancelAlarmManager() {
        Log.d(TAG, "cancelAlarmManager");

        Context context = getBaseContext();
        Intent gpsTrackerIntent = new Intent(context, GpsTrackerAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    // called when trackingButton is tapped
    protected void trackLocation(View v) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!checkIfGooglePlayEnabled()) {
            return;
        }
        if (currentlyTracking) {
            Log.d(TAG,"currentlyTracking:"+currentlyTracking);
            cancelAlarmManager();

            currentlyTracking = false;
            editor.putBoolean("currentlyTracking", false);
            editor.putString("sessionID", "");
        } else {
            Log.d(TAG,"currentlyTracking:"+currentlyTracking);
            startAlarmManager();

            currentlyTracking = true;
            editor.putBoolean("currentlyTracking", true);
            editor.putFloat("totalDistanceInMeters", 0f);
            editor.putBoolean("firstTimeGettingPosition", true);
            editor.putString("sessionID",  UUID.randomUUID().toString());
        }
        editor.apply();
        setTrackingButtonState();
    }
    private boolean hasSpaces(String str) {
        return ((str.split(" ").length > 1) ? true : false);
    }
    private boolean checkIfGooglePlayEnabled() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            return true;
        } else {
            Log.e(TAG, "unable to connect to google play services.");
            //Toast.makeText(getApplicationContext(), R.string.google_play_services_unavailable, Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(), "Por favor habilite Google Play Services!!", Toast.LENGTH_LONG).show();
            return false;
        }
    }
    private void setTrackingButtonState() {
        if (currentlyTracking) {
            trackingButton.setBackgroundResource(R.drawable.green_tracking_button);
            trackingButton.setTextColor(Color.BLACK);
            trackingButton.setText("Rastreando equipo movil!");
        } else {
            trackingButton.setBackgroundResource(R.drawable.red_tracking_button);
            trackingButton.setTextColor(Color.WHITE);
            trackingButton.setText("Rastreo Apagado!");
        }
    }
    public String getImei(){
        return imei;
    }
    @Override
    public void onResume() {
        Log.d(TAG, "onResume MainActivity");
        super.onResume();
        Log.d(TAG,"Leyendo datos de usuario e imei:");
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
        strNombreEequipo=sharedPreferences.getString("nombreEquipo",null);
        strCorreoCuenta=sharedPreferences.getString("correoCuenta",null);
        strUsuario=sharedPreferences.getString("usuario",null);
        strClave=sharedPreferences.getString("clave",null);
        imei=sharedPreferences.getString("imei",null);
        Log.d(TAG,"strNombreEequipo: "+strNombreEequipo);
        Log.d(TAG,"strCorreoCuenta: "+strCorreoCuenta);
        Log.d(TAG,"strUsuario: "+strUsuario);
        Log.d(TAG,"strClave: "+strClave);
        Log.d(TAG,"imei: "+imei);
        registroHecho=sharedPreferences.getBoolean("registroHecho", false);

        boolean estadoAlarma=sharedPreferences.getBoolean("alarmState",false);
        Log.d(TAG,"alarmState: "+estadoAlarma);
        if (!estadoAlarma && registroHecho){
            Log.d(TAG,"Arrancando alarmanager(1)...");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            startAlarmManager();
            editor.putBoolean("alarmState",true);
            editor.apply();
            currentlyTracking=true;
        }
        if (registroHecho && currentlyTracking){
            Log.d(TAG,"Arrancando intent GpsTrackerAlarmReceiver");
            this.startService(new Intent(this, LocationService.class));
        }




        //displayUserSettings();
        setTrackingButtonState();
    }
    public static Context getContextOfApplication(){
        return contextOfApplication;
    }

    @Override
    protected void onStop() {
        Log.d(TAG,"onStop MainActivity");

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy MainActivity");
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("alarmState",false);
        editor.apply();
        Log.d(TAG,"chao!!!!");
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        UiSettings uiSettings=mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        LatLng sydney = new LatLng(0, 0);
        mMap.addMarker(new MarkerOptions().position(sydney).title("UTC"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}