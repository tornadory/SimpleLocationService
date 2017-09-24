package com.johnny.simplelocationservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.LocationSource;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by admin on 9/22/2017.
 */

public class SimpleLocationService extends Service implements LocationSource, AMapLocationListener {
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private OnLocationChangedListener mListener = null;

    String username = "";
    String email = "";

    Double lastLat = 0.0;
    Double lastLon = 0.0;
    long lastTime = 0;
    int scheduleTime = 60 * 1000;

    AMapLocation location;

    public SimpleLocationService(Context applicationContext) {
        super();
        System.out.println("SimpleLocationService " + " start simplelocaitonservice constructor");
    }

    public SimpleLocationService(){

    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("SimpleLocationService " + " start onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        System.out.println("SimpleLocationService" + " start simplelocaitonservice ");

        SharedPreferences sharedPre=getSharedPreferences("config", MODE_PRIVATE);
        username=sharedPre.getString("username", "");
        email=sharedPre.getString("email", "");

        initLoc();
        return START_STICKY;
    }

    private void initLoc() {
        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationClient.setLocationListener(this);
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setNeedAddress(true);
        mLocationOption.setInterval(10000);
//        mLocationOption.setGpsFirst(true);
//        mLocationOption.setOffset(true);
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        System.out.println("simple location service " + "onLocationChanged - AMapLocation" + aMapLocation.toString());
        if(aMapLocation != null){

            System.out.println("aMapLocation is not null");

            if(aMapLocation.getErrorCode() == 0){
                location = aMapLocation;
                if(location.getLatitude() != lastLat || location.getLongitude() != lastLon || (System.currentTimeMillis() - lastTime > scheduleTime)){
                    lastLat = location.getLatitude();
                    lastLon = location.getLongitude();
                    lastTime = System.currentTimeMillis();
                    sendPostRequest();
                }

            }else {
                System.out.println("aMapLocation getErrorCode is not equal to 0");
            }
        }
    }


    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {

    }


    public void sendPostRequest() {
        new SimpleLocationService.PostClass(this).execute();
    }


    private class PostClass extends AsyncTask<String, Void, Void> {

        private final Context context;

        public PostClass(Context c){
            this.context = c;
        }

        protected void onPreExecute(){
        }

        @Override
        protected Void doInBackground(String... params) {
            try {

                JSONObject jsonData = new JSONObject();
                jsonData.put("username", username);
                jsonData.put("latitude", location.getLatitude());
                jsonData.put("longitude", location.getLongitude());
                jsonData.put("address",location.getAddress());
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(location.getTime());
                String time = df.format(date);
                jsonData.put("time", time);

                URL url = new URL("https://simple-location-demo.herokuapp.com/addlocation");

                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                connection.setRequestProperty("Content-Type","application/json");
                connection.setDoOutput(true);
                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.write(jsonData.toString().getBytes("UTF-8"));
                dStream.flush();
                dStream.close();
                int responseCode = connection.getResponseCode();

                System.out.println("\nSending 'POST' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);

                final StringBuilder output = new StringBuilder("Request URL " + url);
                output.append(System.getProperty("line.separator")  + "Response Code " + responseCode);
                output.append(System.getProperty("line.separator")  + "Type " + "POST");
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                while((line = br.readLine()) != null ) {
                    responseOutput.append(line);
                }
                br.close();

                output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator")
                        + System.getProperty("line.separator") + responseOutput.toString());
                System.out.println(output.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute() {
        }

    }
}