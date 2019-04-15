package fan.mingwei.com.auto.AppHelper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import fan.mingwei.com.auto.Service.AccessibilityOperator;

public class AppApi {
    private static final String TAG="AppApi";
    private static final String force="强制停止";
    public static void runPackage(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        context.startActivity(intent);
    }
    public static void MySleep(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch(Exception io)
        {
            Log.e(TAG, io.toString());
        }
    }
    public static boolean cleanProcess(Context context,String pack){
        boolean result=false;
        AccessibilityOperator accessibilityOperator=AccessibilityOperator.getInstance();
        Log.e(TAG,"the service"+accessibilityOperator.isServiceRunning());
        if(accessibilityOperator.isServiceRunning()){
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package",pack, null);
            intent.setData(uri);
           context.startActivity(intent);
           MySleep(1000);
           if(accessibilityOperator.clickForText(force,0)){
               MySleep(1000);
               result=accessibilityOperator.clickForText(force,1);
           }
        }
         if(result){
             MySleep(500);
             accessibilityOperator.exeBack();
         }else {
             accessibilityOperator.exeBack();
             MySleep(500);
             accessibilityOperator.exeBack();
         }
        return result;
    }
    public static boolean clean_multi_Process(Context context,String[] pack){
        boolean result=false;
        int back_time=0;
        AccessibilityOperator accessibilityOperator=AccessibilityOperator.getInstance();
        Log.e(TAG,"the service"+accessibilityOperator.isServiceRunning());
        if(accessibilityOperator.isServiceRunning()){
            for (String mpack :pack) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package",mpack, null);
                intent.setData(uri);
                context.startActivity(intent);
                MySleep(1000);
                if(accessibilityOperator.clickForText(force,0)){
                    MySleep(1000);
                    result=accessibilityOperator.clickForText(force,1);
                    if(!result){
                        break;
                    }else {
                        ++back_time;
                    }

                }else {
                    break;
                }
            }
            }
            for(int i=0;i<back_time;++i){
                if(result){
                    MySleep(500);
                    accessibilityOperator.exeBack();
                }else {
                    accessibilityOperator.exeBack();
                    MySleep(500);
                    accessibilityOperator.exeBack();
                }
            }
        return result;
    }

}
