package com.example.firsttest;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class LiuLiangActivity extends Activity implements OnSharedPreferenceChangeListener{
	private long lastTotalRxBytes = 0;
	private long lastTotalTxBytes = 0;
	private long lastTimeStamp = 0;
	private TextView upload;
	private TextView download;
	private TextView today3G;
	private TextView todayWIFI;
	private TextView monthtotal;
	private TextView monthusetotal;
	private SharedPreferences settings;
	private String[] l = new String[4];
	private long totalflow;
	@SuppressWarnings("unused")
	private long chushiflow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.liuliang);
		upload = (TextView) findViewById(R.id.updatespeed);
		download = (TextView) findViewById(R.id.downloadspeed);
		today3G = (TextView) findViewById(R.id.today_3Gliuliang);
		todayWIFI = (TextView) findViewById(R.id.today_wifiliuliang);
		monthtotal = (TextView) findViewById(R.id.month_totalliuliang);
		monthusetotal = (TextView) findViewById(R.id.month_3Gliuliang);
		Button titleBack = (Button) findViewById(R.id.liuliang_back);
		Button titleEdit = (Button) findViewById(R.id.liuliang_list);
		Button titleSet = (Button) findViewById(R.id.liuliang_setting);
		titleBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				LiuLiangActivity.this.finish();
			}
		});
		titleEdit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent1 = new Intent(LiuLiangActivity.this,
						LiuLiangListActivity.class);
				startActivity(intent1); // ��ʾ����

			}
		});
		titleSet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent1 = new Intent(LiuLiangActivity.this,
						SettingActivity.class);
				startActivity(intent1); // ��ʾ����

			}
		});
		settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		monthtotal.setText("��������2G/3G/4G������"
				+ settings.getString("netflow", "") + "MB");
//		String statday=settings.getString("startday", "");
//		
//		Time t=new Time(); 
//		if(statday.equals(t.monthDay+"")&&t.hour==0&&t.minute==0&&t.second==0){
//			Editor editor=settings.edit();
//            editor.putLong("nowtotalflow",0);
//            editor.commit();
//		}
		totalflow=settings.getLong("nowtotalflow",0);
		monthusetotal.setText("��������2G/3G/4G������"
				+ new TextFormat().formatByte(totalflow));
		
		settings.registerOnSharedPreferenceChangeListener(this);//ע�ᶯ̬�仯����

		Intent intent = new Intent(this, StartService.class);
		startService(intent);
		lastTotalRxBytes = getTotalRxBytes();
		lastTotalTxBytes = getTotalTxBytes();
		chushiflow=TrafficStats.getMobileRxBytes()
				+ TrafficStats.getMobileTxBytes();
		lastTimeStamp = System.currentTimeMillis();
		new Timer().schedule(task, 1000, 2000); // 1s����������ÿ2sִ��һ��
	}

	private void showNetSpeed() {
		long nowTotalRxBytes = getTotalRxBytes();
		long nowTimeStamp = System.currentTimeMillis();
		long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));// ����ת��

		long nowTotalTxBytes = getTotalTxBytes();
		long speed1 = ((nowTotalTxBytes - lastTotalTxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));// ����ת��

		lastTimeStamp = nowTimeStamp;
		lastTotalRxBytes = nowTotalRxBytes;
		lastTotalTxBytes = nowTotalTxBytes;

		l[0] = String.valueOf(speed) + " kb/s";
		l[1] = String.valueOf(speed1) + " kb/s";
		long shoujiliuliang = TrafficStats.getMobileRxBytes()
				+ TrafficStats.getMobileTxBytes();
		l[2] = new TextFormat().formatByte(shoujiliuliang);
		long wifiliuliang = getTotalRxBytes() + getTotalTxBytes() - shoujiliuliang/1024;
		if (wifiliuliang < 0)
			l[3] = String.valueOf(0) + "bytes";
		else
			l[3] = new TextFormat().formatByte(wifiliuliang* 1024);

		Message msg = mHandler.obtainMessage();
		msg.what = 100;
		// msg.obj = String.valueOf(speed) + " kb/s";
		// msg.obj = l;

		mHandler.sendMessage(msg);// ���½���
	}

	private long getTotalRxBytes() {
		/*return TrafficStats.getUidRxBytes(getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0
				: (TrafficStats.getTotalRxBytes() / 1024);// תΪKB
*/		return (TrafficStats.getTotalRxBytes() / 1024);// תΪKB
	}

	private long getTotalTxBytes() {
		/*return TrafficStats.getUidTxBytes(getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0
				: (TrafficStats.getTotalTxBytes() / 1024);// תΪKB
*/		return (TrafficStats.getTotalTxBytes() / 1024);// תΪKB
		}

	TimerTask task = new TimerTask() {
		@Override
		public void run() {
			showNetSpeed();
		}
	};

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {// handler���յ���Ϣ��ͻ�ִ�д˷���
			/*
			 * TextView upload = (TextView)findViewById(R.id.updatespeed);
			 * TextView download = (TextView)findViewById(R.id.downloadspeed);
			 */
			upload.setText("�ϴ��ٶ�:" + l[1]);
			download.setText("�����ٶ�:" + l[0]);
			today3G.setText("��������2G/3G/4G������" + l[2]);
			todayWIFI.setText("��������WIFI������" + l[3]);
		}
	};

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

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		if (key.equals("netflow")) {
			settings = PreferenceManager
					.getDefaultSharedPreferences(this);
			monthtotal.setText("��������2G/3G/4G������"
					+ settings.getString("netflow", "") + "MB");
        }
		if (key.equals("nowtotalflow")) {
			settings = PreferenceManager
					.getDefaultSharedPreferences(this);
			monthusetotal.setText("��������2G/3G/4G������"
					+ new TextFormat().formatByte(settings.getLong("nowtotalflow",0)));
		}
		/*if (key.equals("jiaozheng")) {
			System.out.println("1111111111111111111111111");
			settings = PreferenceManager
					.getDefaultSharedPreferences(this);
			String flow=settings.getString(key, "");
			long time=Long.parseLong(flow)*1024*1024;
			System.out.println(time);
			System.out.println(111+settings.getLong("nowtotalflow",0));
			Editor editor=settings.edit();
            editor.putLong("nowtotalflow",time);
            editor.commit();
            System.out.println(222+settings.getLong("nowtotalflow",0));
		}*/
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		settings.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}
}
