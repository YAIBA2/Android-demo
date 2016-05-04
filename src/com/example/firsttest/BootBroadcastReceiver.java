package com.example.firsttest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver{  
       
    //private static final String ACTION = "Android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {   
     /*   Log.e("TAG", "开机自动服务自动启动.....");   */
          
      //后边的XXX.class就是要启动的服务   
    	if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
    		   /*Intent newIntent = new Intent(context,StartService.class);
    		   newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
    		   context.startService(newIntent);*/
    		Intent i= new Intent(Intent.ACTION_RUN);
            i.setClass(context, StartService.class);
            context.startService(i);
    		  }           
    }    
}  
