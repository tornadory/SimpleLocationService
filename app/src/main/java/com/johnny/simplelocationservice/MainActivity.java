package com.johnny.simplelocationservice;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private  String TAG = "SimpleLocationService";

    private ProgressDialog progress;


    private  SimpleLocationService mSimpleLocationService;
    Intent mServiceIntent;

    String deviceid = "";
    String imei = "";
    String imsi = "";

    String username = "";
    String email = "";

    TextView tvUserName;
    TextView tvEmail;

    TelephonyManager tm;

    boolean registed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tm = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        deviceid = tm.getDeviceId();
        imei = tm.getSimSerialNumber();
        imsi = tm.getSubscriberId();
        System.out.println("deviceID " + deviceid + "  imei " + imei + "  imsi " + imsi);

        tvUserName = (TextView)findViewById(R.id.txUsername);
        tvEmail = (TextView)findViewById(R.id.txEmail);

        SharedPreferences sharedPre=getSharedPreferences("config", MODE_PRIVATE);

        username=sharedPre.getString("username", "");
        email=sharedPre.getString("email", "");
        tvUserName.setText(username);
        tvEmail.setText(email);

        if(username != ""){
            registed = true;
        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                System.out.println(TAG + "true");
                return true;
            }
        }
        System.out.println (TAG + "false");
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void sendPostRequest(View View) {
        new PostClass(this).execute();
    }


    private class PostClass extends AsyncTask<String, Void, Void> {

        private final Context context;

        public PostClass(Context c){
            this.context = c;
        }

        protected void onPreExecute(){
            progress= new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {

                JSONObject jsonData = new JSONObject();
                jsonData.put("username", username);
                jsonData.put("email", email);
                jsonData.put("deviceID", deviceid);
                jsonData.put("imei", imei);
                jsonData.put("imsi", imsi);

                URL url = new URL("https://simple-location-demo.herokuapp.com/newuser");


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

                if(responseCode == 200){
                    //
                    SharedPreferences sharedPre= getSharedPreferences("config", MODE_PRIVATE);
                    SharedPreferences.Editor editor=sharedPre.edit();
                    editor.putString("username", username);
                    editor.putString("email", email);
                    editor.commit();
                    registed = true;
                }

                System.out.println("\nSending 'POST' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);

//                final StringBuilder output = new StringBuilder("Request URL " + url);
//                output.append(System.getProperty("line.separator")  + "Response Code " + responseCode);
//                output.append(System.getProperty("line.separator")  + "Type " + "POST");
//                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                String line = "";
//                StringBuilder responseOutput = new StringBuilder();
//                System.out.println("output===============" + br);
//                while((line = br.readLine()) != null ) {
//                    responseOutput.append(line);
//                }
//                br.close();
//
//                output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        progress.dismiss();
                    }
                });


            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute() {
            progress.dismiss();
        }

    }


    public void regUser(View view){
        if(tvUserName.getText().length() > 0 && tvEmail.getText().length()> 0){
            username = tvUserName.getText().toString();
            email = tvEmail.getText().toString();

            sendPostRequest(view);
        }
    }

    public void hideMe(View View){
        System.out.println(TAG + "hideMe called p");
        if(registed){
            System.out.println(TAG + "try to start service");


            mSimpleLocationService = new SimpleLocationService(this);
            mServiceIntent = new Intent(this, mSimpleLocationService.getClass());

            if (!isMyServiceRunning(mSimpleLocationService.getClass())) {
                startService(mServiceIntent);
            }

            finish();
        }else {
            Toast.makeText(getApplicationContext(), "Please register first!", Toast.LENGTH_LONG).show();
            System.out.println("have not been registered");
        }

    }
}
