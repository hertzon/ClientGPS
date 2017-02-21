package com.ejemplos.nelson.clientgps;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

//import com.google.android.gms.common.GooglePlayServicesUtil;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "GPSTrax";
    public static TcpClient mTcpClient;
    public static final String SERVER_IP = "104.236.203.72"; //your computer IP address
    public static final int SERVER_PORT = 31272;
    private PrintWriter mBufferOut;

    // use the websmithing defaultUploadWebsite for testing and then check your
    // location with your browser here: https://www.websmithing.com/gpstracker/displaymap.php
    private String defaultUploadWebsite;

    private boolean currentlyProcessingLocation = false;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        defaultUploadWebsite = getString(R.string.default_upload_website);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // if we are currently trying to get a location and the alarm manager has called this again,
        // no need to start processing a new location.
        if (!currentlyProcessingLocation) {
            currentlyProcessingLocation = true;
            startTracking();
        }

        return START_NOT_STICKY;
    }

    private void startTracking() {
        Log.d(TAG, "startTracking");

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {

            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {
            Log.e(TAG, "unable to connect to google play services.");
        }
    }

    protected void sendLocationDataToWebsite(Location location) {

        String provider=location.getProvider();
        float bearing=location.getBearing();
        float accuary=location.getAccuracy();
        double altitude=location.getAltitude();
        double latitude=location.getLatitude();
        double longitude=location.getLongitude();
        float velocidadmps=location.getSpeed();
        float velocidadkph=velocidadmps*3.6f;
        Log.i(TAG,"provider: "+provider);
        Log.i(TAG,"bearing: "+bearing);
        Log.i(TAG,"accuary: "+accuary);
        Log.i(TAG,"altitude: "+altitude);
        Log.i(TAG,"latitude: "+latitude);
        Log.i(TAG,"longitude: "+longitude);
        Log.i(TAG,"velocidadmps: "+velocidadmps);
        Log.i(TAG,"velocidadkph: "+velocidadkph);


        SharedPreferences sharedPreferences = this.getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
        String imei=sharedPreferences.getString("imei",null);
        Log.i(TAG,"imei from shared preferences: "+imei);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //String timestamp = sdf.format(new Date());

        String coordenadas=locacion_formateada(latitude,longitude);
        //Date d=new Date(System.currentTimeMillis());
        String  s= null;
        s=GetUTCdatetimeAsString();
        String ano=s.substring(2 ,4);
        String dia=s.substring(8,10);
        String mes=s.substring(5, 7);
        String hora=s.substring(11, 13);
        String minuto=s.substring(14, 16);
        String segundo=s.substring(17, 19);

        //String trama_pos="imei:"+imei+','+"tracker"+','+ano+mes+dia+hora+minuto+','+','+'F'+','+hora+minuto+segundo+".000"+','+'A'+','+coordenadas+','+velocidadkph+','+"0"+';';
        String trama_pos="imei:"+imei+','+"tracker"+','+ano+mes+dia+hora+minuto+','+','+'F'+','+hora+minuto+segundo+".000"+','+'A'+','+coordenadas+','+velocidadkph+','+bearing+';';
        Log.i(TAG,"Trama pos: "+trama_pos);


        if (true){
            Log.i(TAG,"Hay red!!!");
            new SendServer().execute("##"+"imei:"+imei+','+"A;");
            new SendServer().execute(trama_pos);
        }else {
            Log.i(TAG,"No Hay red!!!");
        }





        ///////////********************************************************************


        // formatted for mysql datetime format
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date date = new Date(location.getTime());

        sharedPreferences = this.getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        float totalDistanceInMeters = sharedPreferences.getFloat("totalDistanceInMeters", 0f);

        boolean firstTimeGettingPosition = sharedPreferences.getBoolean("firstTimeGettingPosition", true);

        if (firstTimeGettingPosition) {
            editor.putBoolean("firstTimeGettingPosition", false);
        } else {
            Location previousLocation = new Location("");
            previousLocation.setLatitude(sharedPreferences.getFloat("previousLatitude", 0f));
            previousLocation.setLongitude(sharedPreferences.getFloat("previousLongitude", 0f));

            float distance = location.distanceTo(previousLocation);
            totalDistanceInMeters += distance;
            editor.putFloat("totalDistanceInMeters", totalDistanceInMeters);
        }

        editor.putFloat("previousLatitude", (float)location.getLatitude());
        editor.putFloat("previousLongitude", (float)location.getLongitude());
        editor.apply();

        final RequestParams requestParams = new RequestParams();
        requestParams.put("latitude", Double.toString(location.getLatitude()));
        requestParams.put("longitude", Double.toString(location.getLongitude()));

        Double speedInMilesPerHour = location.getSpeed()* 2.2369;
        requestParams.put("speed",  Integer.toString(speedInMilesPerHour.intValue()));

        try {
            requestParams.put("date", URLEncoder.encode(dateFormat.format(date), "UTF-8"));
        } catch (UnsupportedEncodingException e) {}

        requestParams.put("locationmethod", location.getProvider());

        if (totalDistanceInMeters > 0) {
            requestParams.put("distance", String.format("%.1f", totalDistanceInMeters / 1609)); // in miles,
        } else {
            requestParams.put("distance", "0.0"); // in miles
        }

        requestParams.put("username", sharedPreferences.getString("userName", ""));
        requestParams.put("phonenumber", sharedPreferences.getString("appID", "")); // uuid
        requestParams.put("sessionid", sharedPreferences.getString("sessionID", "")); // uuid

        Double accuracyInFeet = location.getAccuracy()* 3.28;
        requestParams.put("accuracy",  Integer.toString(accuracyInFeet.intValue()));

        Double altitudeInFeet = location.getAltitude() * 3.28;
        requestParams.put("extrainfo",  Integer.toString(altitudeInFeet.intValue()));

        requestParams.put("eventtype", "android");

        Float direction = location.getBearing();
        requestParams.put("direction",  Integer.toString(direction.intValue()));

        final String uploadWebsite = sharedPreferences.getString("defaultUploadWebsite", defaultUploadWebsite);
        stopSelf();
//
//        LoopjHttpClient.get(uploadWebsite, requestParams, new AsyncHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
//                LoopjHttpClient.debugLoopJ(TAG, "sendLocationDataToWebsite - success", uploadWebsite, requestParams, responseBody, headers, statusCode, null);
//                stopSelf();
//            }
//            @Override
//            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] errorResponse, Throwable e) {
//                LoopjHttpClient.debugLoopJ(TAG, "sendLocationDataToWebsite - failure", uploadWebsite, requestParams, errorResponse, headers, statusCode, e);
//                stopSelf();
//            }
//        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.e(TAG, "position: " + location.getLatitude() + ", " + location.getLongitude() + " accuracy: " + location.getAccuracy());

            // we have our desired accuracy of 500 meters so lets quit this service,
            // onDestroy will be called and stop our location uodates
            if (location.getAccuracy() < 500.0f) {
                stopLocationUpdates();
                sendLocationDataToWebsite(location);
            }
        }
    }

    private void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    /**
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000); // milliseconds
        locationRequest.setFastestInterval(1000); // the fastest rate in milliseconds at which your app can handle location updates
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");

        stopLocationUpdates();
        stopSelf();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "GoogleApiClient connection has been suspend");
    }
    public static String locacion_formateada(double latitude, double longitude) {

        try {
            //float latSeconds = (float) Math.round(latitude * 3600);
            float latSeconds = (float) latitude * 3600;
            int latDegrees = (int) (latSeconds / 3600);
            latSeconds = Math.abs(latSeconds % 3600);
            int latMinutes = (int) (latSeconds / 60);
            latSeconds %= 60;
            float longSeconds = (float) longitude * 3600;
            int longDegrees = (int) (longSeconds / 3600);
            longSeconds = Math.abs(longSeconds % 3600);
            int longMinutes = (int) (longSeconds / 60);
            longSeconds %= 60;

            //velocidad=(int) (velocidad/1.852);
            String latDegree = latDegrees >= 0 ? "N" : "S";
            //String lonDegrees = latDegrees >= 0 ? "E" : "W";
            String longDegree = longDegrees >= 0 ? "E" : "W";
            float lati = Math.abs(latDegrees) * 100 + latMinutes + latSeconds / 60;
            float longi=Math.abs(longDegrees)*100+longMinutes+longSeconds/60;
            String latiString=String.format("%02.4f", lati);
            String longiString=String.format("%02.4f", longi);
            if (Math.abs(latDegrees)<10){
                latiString="0"+latiString;
            }
            if (Math.abs(longDegrees)<100){
                longiString="0"+longiString;
            }
            latiString=latiString.replace(',','.')+','+latDegree;
            longiString=longiString.replace(',','.')+','+longDegree;

            return latiString+','+longiString;
        } catch (Exception e) {
            Log.i("Debug","Error....");

            return ""+ String.format("%8.5f", latitude) + "  "
                    + String.format("%8.5f", longitude) ;
        }
    }
    public static String GetUTCdatetimeAsString()
    {
        String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new Date());

        return utcTime;
    }
//    public  boolean isNetwork(){
//        //Determinamos si hay conectividad de red....
//        boolean isConnected=false;
//        Context applicationContext=MainActivity.getContextOfApplication();
//        ConnectivityManager cm =
//                (ConnectivityManager)applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//        if (activeNetwork!=null){
//            isConnected = activeNetwork.isConnected();
//        }else {
//            isConnected=false;
//        }
//        return isConnected;
//    }

    public class SendServer extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            Log.i(TAG,"mensaje pasado: "+params[0]);
            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                Socket socket = new Socket(serverAddr, SERVER_PORT);
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                mBufferOut.println(params[0]);
                mBufferOut.flush();
                socket.close();

            } catch (UnknownHostException e) {
                e.printStackTrace();
                Log.i(TAG,"Error: "+e.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }


            return false;
        }
    }
}