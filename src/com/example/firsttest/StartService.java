package com.example.firsttest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

public class StartService extends Service implements
		OnSharedPreferenceChangeListener {

	// private Handler objHandler = new Handler();
	private int intCounter = 0;
	private static final String TAG = "TService";
	private NotificationManager notificationManager;
	private SharedPreferences settings;
	private long totalflow;
	private long chushiflow;
	private long limitflow;
	/*
	  private Runnable mTasks = new Runnable() { //第二中设置延迟的方式 
	  public void run()
	  { 
	  notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		showNotification();
		Message msg = objHandler.obtainMessage();
		msg.what = 100;
		objHandler.sendMessage(msg);
	  objHandler.postDelayed(mTasks, 5000); } 
	  };
	 */

	Handler objHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {// handler接收到消息后就会执行此方法
			if (msg.what == 100 && intCounter == 0) {
				createDialog();
			}
		}
	};

	TimerTask task = new TimerTask() {
		@Override
		public void run() {
			// intCounter++;
			// Looper.prepare();
			notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			showNotification();
			Message msg = objHandler.obtainMessage();
			msg.what = 100;
			objHandler.sendMessage(msg);
			// Looper.loop();
		}
	};

	public void onCreate() {
		// intCounter++;
		Log.d(TAG, "============> TService.onCreate");
		// notificationManager = (NotificationManager)
		// getSystemService(NOTIFICATION_SERVICE);
		// showNotification();

		settings = PreferenceManager.getDefaultSharedPreferences(this);
		settings.registerOnSharedPreferenceChangeListener(this);

		String statday = settings.getString("startday", "");
		limitflow = Long.parseLong(settings.getString("netflow", "300"));

		Time t = new Time();
		t.setToNow();
		System.out.println("statday"+statday);
		System.out.println("t.monthDay"+t.monthDay);
		System.out.println("t.monthDay"+t.hour);
		System.out.println("t.monthDay"+ t.minute);
		System.out.println("t.monthDay"+t.second);
		
		
		
		if (statday.equals(t.monthDay + "") && settings.getInt("updatecontrol",0)==0) {
			SharedPreferences mySharedPreferences = getSharedPreferences(
					"test1", MODE_PRIVATE);
			SharedPreferences.Editor editor = mySharedPreferences.edit();
			editor.putLong("nowtotalflow", 0);
			editor.commit();
			editor=settings.edit();
			editor.putInt("updatecontrol", 1);
			editor.commit();
		}

		// totalflow = settings.getLong("totalflow",0);
		// totalflow = settings.getLong("nowtotalflow", 0);
		SharedPreferences sharedPreferences = getSharedPreferences("test1",
				Activity.MODE_PRIVATE);
		totalflow = sharedPreferences.getLong("nowtotalflow", 0);// 用来处理文件存储，确保oncreat得到准确的数值
		chushiflow = TrafficStats.getMobileRxBytes()
				+ TrafficStats.getMobileTxBytes();
		System.out.println("初始时的总流量" + totalflow);
		System.out.println("初始时的初始流量" + chushiflow);
		new Timer().schedule(task, 1000, 5000);// 5s刷新
		//objHandler.post(mTasks);//第二中设置延迟的方式 
		super.onCreate();
	}

	@SuppressWarnings("deprecation")
	public void onStart(Intent intent, int startId) {
		Log.i(TAG, "============> TService.onStart");
		// objHandler.postDelayed(mTasks, 1000); 第二种设置延时的方式
		// new Timer().schedule(task, 1000, 5000);
		// totalflow = settings.getLong("totalflow",0);
		super.onStart(intent, startId);
	}

	public IBinder onBind(Intent intent) {
		Log.i(TAG, "============> TService.onBind");
		return null;
	}

	public class LocalBinder extends Binder {
		public StartService getService() {
			return StartService.this;
		}
	}

	public boolean onUnbind(Intent intent) {
		Log.i(TAG, "============> TService.onUnbind");
		return false;
	}

	public void onRebind(Intent intent) {
		Log.i(TAG, "============> TService.onRebind");
	}

	public void onDestroy() {
		Log.i(TAG, "============> TService.onDestroy");
		// notificationManager.cancel(1);
		// objHandler.removeCallbacks(task);
		long shoujiliuliang = TrafficStats.getMobileRxBytes()
				+ TrafficStats.getMobileTxBytes();
		Editor editor = settings.edit();
		// editor.putLong("totalflow", totalflow+shoujiliuliang-chushiflow);
		editor.putLong("nowtotalflow", totalflow + shoujiliuliang - chushiflow);
		editor.commit();
		settings.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}
	

	@SuppressWarnings("deprecation")
	private void showNotification() {
		long shoujiliuliang = TrafficStats.getMobileRxBytes()
				+ TrafficStats.getMobileTxBytes();
		/*API11以上不建议使用
		Notification notification = new Notification(R.drawable.network15,
				"流量监控中", 0);
		Intent intent = new Intent(this, LiuLiangActivity.class);
		// intent.putExtra("FLG", 1);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, 0);
		notification.setLatestEventInfo(
				this,
				"流量监控",
				"剩余"
						+ new TextFormat().formatByte(totalflow
								+ shoujiliuliang - chushiflow) + "/"
						+ settings.getString("netflow", "") + "MB",
				contentIntent);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notificationManager.notify(1, notification);
		//*/
		//*API15-API11可用
		 //NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE); 
		 NotificationCompat.Builder builder =
		 new NotificationCompat.Builder(StartService.this);
		 builder.setTicker("流量监控中");
		 builder.setContentTitle("流量监控");
		 builder.setContentText("剩余"
					+ new TextFormat().formatByte(totalflow
							+ shoujiliuliang - chushiflow) + "/"
					+ settings.getString("netflow", "") + "MB");
		 builder.setSmallIcon(R.drawable.network15);
		 builder.setLargeIcon(BitmapFactory.decodeResource(StartService.this.getResources(), R.drawable.network15));
		 builder.setWhen(0);
		 Intent intent = new Intent(this,LiuLiangActivity.class); PendingIntent
		 pendingIntent =
		 PendingIntent.getActivity(this,0,intent
		 ,PendingIntent.FLAG_CANCEL_CURRENT); 
		 //设置通知提示铃声 
		 // Uri sound =Uri.fromFile(newFile("/system/media/audio/ringtones/Childhood.ogg")); //
		 //builder.setSound(sound); // //设置通知振动,表示让手机通知到来时震动1秒，而后静止1秒，在震动1秒 //
		 //long[] vibrates = {0,1000,1000,1000}; //
		 //builder.setVibrate(vibrates); // //设置通知提示LED灯 //
		 //builder.setLights(Color.BLUE, 1000, 1000); // //设置通知提示默认效果
		 //builder.setDefaults(Notification.DEFAULT_ALL);
		 
		 builder.setContentIntent(pendingIntent); 
		 Notification notification =builder.build(); 
		 notification.flags=Notification.FLAG_ONGOING_EVENT;
		 notificationManager.notify(1, notification);
		// */
		
		/*API15以上
	    NotificationManager nm = (NotificationManager) context.getSystemService(
	            Context.NOTIFICATION_SERVICE);
	    Intent intent1 = new Intent(this, LiuLiangActivity.class); 
	    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
	 
	    Notification notification1 = new Notification.Builder(this)
	        .setAutoCancel(true)
	        .setContentTitle("title")
	        .setContentText("describe")
	        .setContentIntent(pendingIntent)
	        .setSmallIcon(R.drawable.ic_launcher)
	        .setWhen(System.currentTimeMillis())
	        .build();
	         
	    nm.notify(1, notification1);
	//*/
		
		settings.edit()
				.putLong("nowtotalflow",
						totalflow + shoujiliuliang - chushiflow).commit();//这里仅用做显示用存储的总流量

		SharedPreferences mySharedPreferences = getSharedPreferences("test1",//文本存储的总流量保存
				MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putLong("nowtotalflow", totalflow + shoujiliuliang - chushiflow);
		editor.commit();
		System.out.println("88888" + settings.getLong("nowtotalflow", 0));
		
		Time t = new Time();
		t.setToNow();
		String statday = settings.getString("startday", "");
		if (!statday.equals(t.monthDay + "") && settings.getInt("updatecontrol",0)==1) {
			editor=settings.edit();
			editor.putInt("updatecontrol", 0);
			editor.commit();
		}

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		if (key.equals("jiaozheng")) {
			System.out.println("1111111111111111111111111");
			settings = PreferenceManager.getDefaultSharedPreferences(this);
			String flow = settings.getString(key, "");
			long time = Long.parseLong(flow) * 1024 * 1024;
			System.out.println(time);
			System.out.println(111 + settings.getLong("nowtotalflow", 0));
			Editor editor = settings.edit();
			editor.putLong("nowtotalflow", time);
			editor.commit();
			totalflow = settings.getLong("nowtotalflow", 0);
			System.out.println(222 + settings.getLong("nowtotalflow", 0));
		}
	}

	class TextFormat {

		/**
		 * 格式化数据
		 * 
		 * @param data
		 * @return
		 */
		String formatByte(long data) {
			DecimalFormat format = new DecimalFormat("##.##");
			if (data < 1024) {
				return data + "bytes";
			} else if (data < 1024 * 1024) {
				return format.format(data / 1024f) + "KB";
			} else if (data < 1024 * 1024 * 1024) {
				return format.format(data / 1024f / 1024f) + "MB";
			} else if (data < 1024 * 1024 * 1024 * 1024) {
				return format.format(data / 1024f / 1024f / 1024f) + "GB";
			} else {
				return "超出统计范围";
			}
		}
	}

	@SuppressWarnings("unused")
	private void displayToast(String message) {
		Toast.makeText(StartService.this, message, Toast.LENGTH_SHORT).show();
	}

	public void createDialog() {
		long shoujiliuliang = TrafficStats.getMobileRxBytes()
				+ TrafficStats.getMobileTxBytes();
		limitflow = Long.parseLong(settings.getString("netflow", ""));
		if ((totalflow + shoujiliuliang - chushiflow) > limitflow * 1024 * 1024) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(
					StartService.this);

			alertDialog.setMessage("移动流量已超出，是否继续使用");

			alertDialog.setPositiveButton("不再询问",

			new DialogInterface.OnClickListener()

			{

				public void onClick(DialogInterface dialog, int which)

				{
					intCounter++;
				}

			});

			alertDialog.setNegativeButton("立即断网",

			new DialogInterface.OnClickListener()

			{

				public void onClick(DialogInterface dialog, int which)

				{
					toggleMobileData(getBaseContext(), false);
					intCounter++;
				}

			});

			AlertDialog ad = alertDialog.create();

			ad.getWindow()
					.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

			ad.setCanceledOnTouchOutside(false);// 点击外面区域不会让dialog消失

			ad.show();
		}
	}
	
	//反射获取控制流量的方法
	private void toggleMobileData(Context context, boolean enabled) {
		  ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		  Class<?> conMgrClass = null; // ConnectivityManager类
		  Field iConMgrField = null; // ConnectivityManager类中的字段
		  Object iConMgr = null; // IConnectivityManager类的引用
		  Class<?> iConMgrClass = null; // IConnectivityManager类
		  Method setMobileDataEnabledMethod = null; // setMobileDataEnabled方法

		  try {
		   // 取得ConnectivityManager类
		   conMgrClass = Class.forName(conMgr.getClass().getName());
		   // 取得ConnectivityManager类中的对象mService
		   iConMgrField = conMgrClass.getDeclaredField("mService");
		   // 设置mService可访问
		   iConMgrField.setAccessible(true);
		   // 取得mService的实例化类IConnectivityManager
		   iConMgr = iConMgrField.get(conMgr);
		   // 取得IConnectivityManager类
		   iConMgrClass = Class.forName(iConMgr.getClass().getName());
		   // 取得IConnectivityManager类中的setMobileDataEnabled(boolean)方法
		   setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
		   // 设置setMobileDataEnabled方法可访问
		   setMobileDataEnabledMethod.setAccessible(true);
		   // 调用setMobileDataEnabled方法
		   setMobileDataEnabledMethod.invoke(iConMgr, enabled);
		  } catch (ClassNotFoundException e) {
		   e.printStackTrace();
		  } catch (NoSuchFieldException e) {
		   e.printStackTrace();
		  } catch (SecurityException e) {
		   e.printStackTrace();
		  } catch (NoSuchMethodException e) {
		   e.printStackTrace();
		  } catch (IllegalArgumentException e) {
		   e.printStackTrace();
		  } catch (IllegalAccessException e) {
		   e.printStackTrace();
		  } catch (InvocationTargetException e) {
		   e.printStackTrace();
		  }
		 }
}