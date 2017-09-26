package com.johnny.simplelocationservice;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by admin on 9/22/2017.
 */

public class SimpleLocationServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        System.out.println(SimpleLocationServiceReceiver.class.getSimpleName() + "onRecevie..." + intent.getAction());
        if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)){
            Intent serviceIntent = new Intent(context, SimpleLocationService.class);
            context.startService(serviceIntent);
        }
//        Intent serviceIntent = new Intent(context, SimpleLocationService.class);
//        context.startService(serviceIntent);
    }

//    private boolean isMyServiceRunning(Class<?> serviceClass) {
//        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if (serviceClass.getName().equals(service.service.getClassName())) {
//                System.out.println(TAG + "true");
//                return true;
//            }
//        }
//        System.out.println (TAG + "false");
//        return false;
//    }
}
