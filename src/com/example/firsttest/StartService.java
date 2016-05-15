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
	  private Runnable mTasks = new Runnable() { //�ڶ��������ӳٵķ�ʽ 
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
		public void handleMessage(Message msg) {// handler���յ���Ϣ��ͻ�ִ�д˷���
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
		totalflow = sharedPreferences.getLong("nowtotalflow", 0);// ���������ļ��洢��ȷ��oncreat�õ�׼ȷ����ֵ
		chushiflow = TrafficStats.getMobileRxBytes()
				+ TrafficStats.getMobileTxBytes();
		System.out.println("��ʼʱ��������" + totalflow);
		System.out.println("��ʼʱ�ĳ�ʼ����" + chushiflow);
		new Timer().schedule(task, 1000, 5000);// 5sˢ��
		//objHandler.post(mTasks);//�ڶ��������ӳٵķ�ʽ 
		super.onCreate();
	}

	@SuppressWarnings("deprecation")
	public void onStart(Intent intent, int startId) {
		Log.i(TAG, "============> TService.onStart");
		// objHandler.postDelayed(mTasks, 1000); �ڶ���������ʱ�ķ�ʽ
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
		/*API11���ϲ�����ʹ��
		Notification notification = new Notification(R.drawable.network15,
				"���������", 0);
		Intent intent = new Intent(this, LiuLiangActivity.class);
		// intent.putExtra("FLG", 1);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, 0);
		notification.setLatestEventInfo(
				this,
				"�������",
				"ʣ��"
						+ new TextFormat().formatByte(totalflow
								+ shoujiliuliang - chushiflow) + "/"
						+ settings.getString("netflow", "") + "MB",
				contentIntent);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notificationManager.notify(1, notification);
		//*/
		//*API15-API11����
		 //NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE); 
		 NotificationCompat.Builder builder =
		 new NotificationCompat.Builder(StartService.this);
		 builder.setTicker("���������");
		 builder.setContentTitle("�������");
		 builder.setContentText("ʣ��"
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
		 //����֪ͨ��ʾ���� 
		 // Uri sound =Uri.fromFile(newFile("/system/media/audio/ringtones/Childhood.ogg")); //
		 //builder.setSound(sound); // //����֪ͨ��,��ʾ���ֻ�֪ͨ����ʱ��1�룬����ֹ1�룬����1�� //
		 //long[] vibrates = {0,1000,1000,1000}; //
		 //builder.setVibrate(vibrates); // //����֪ͨ��ʾLED�� //
		 //builder.setLights(Color.BLUE, 1000, 1000); // //����֪ͨ��ʾĬ��Ч��
		 //builder.setDefaults(Notification.DEFAULT_ALL);
		 
		 builder.setContentIntent(pendingIntent); 
		 Notification notification =builder.build(); 
		 notification.flags=Notification.FLAG_ONGOING_EVENT;
		 notificationManager.notify(1, notification);
		// */
		
		/*API15����
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
						totalflow + shoujiliuliang - chushiflow).commit();//�����������ʾ�ô洢��������

		SharedPreferences mySharedPreferences = getSharedPreferences("test1",//�ı��洢������������
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
		 * ��ʽ������
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
				return "����ͳ�Ʒ�Χ";
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

			alertDialog.setMessage("�ƶ������ѳ������Ƿ����ʹ��");

			alertDialog.setPositiveButton("����ѯ��",

			new DialogInterface.OnClickListener()

			{

				public void onClick(DialogInterface dialog, int which)

				{
					intCounter++;
				}

			});

			alertDialog.setNegativeButton("��������",

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

			ad.setCanceledOnTouchOutside(false);// ����������򲻻���dialog��ʧ

			ad.show();
		}
	}
	
	//�����ȡ���������ķ���
	private void toggleMobileData(Context context, boolean enabled) {
		  ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		  Class<?> conMgrClass = null; // ConnectivityManager��
		  Field iConMgrField = null; // ConnectivityManager���е��ֶ�
		  Object iConMgr = null; // IConnectivityManager�������
		  Class<?> iConMgrClass = null; // IConnectivityManager��
		  Method setMobileDataEnabledMethod = null; // setMobileDataEnabled����

		  try {
		   // ȡ��ConnectivityManager��
		   conMgrClass = Class.forName(conMgr.getClass().getName());
		   // ȡ��ConnectivityManager���еĶ���mService
		   iConMgrField = conMgrClass.getDeclaredField("mService");
		   // ����mService�ɷ���
		   iConMgrField.setAccessible(true);
		   // ȡ��mService��ʵ������IConnectivityManager
		   iConMgr = iConMgrField.get(conMgr);
		   // ȡ��IConnectivityManager��
		   iConMgrClass = Class.forName(iConMgr.getClass().getName());
		   // ȡ��IConnectivityManager���е�setMobileDataEnabled(boolean)����
		   setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
		   // ����setMobileDataEnabled�����ɷ���
		   setMobileDataEnabledMethod.setAccessible(true);
		   // ����setMobileDataEnabled����
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