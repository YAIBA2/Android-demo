package com.example.firsttest;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug.MemoryInfo;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class ProcessManagerActivity extends Activity {
	private TextView tv_avail_process;
	private TextView tv_avail_memory;
	private ListView lv_processmanger;
	private View ll_loading;
	private List<AppInfo> userAppInfos;
	private List<AppInfo> systemAppInfos;
	private AppManagerAdapter adapter;
	// ���������Ŀ ������Ķ���
	private static AppInfo appinfo;
	private TextView tv_process_manager_status;
	private LayoutInflater infater; 
	private static ActivityManager activityManager;  
    private static List<RunningAppProcessInfo> runningAppProcessInfos; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.processmanage);
		
		ll_loading = findViewById(R.id.ll_loading1);
		tv_avail_process = (TextView) findViewById(R.id.avail_process);
		tv_avail_memory = (TextView) findViewById(R.id.avail_memory);
		lv_processmanger = (ListView) findViewById(R.id.processmanger);
		tv_avail_process.setText("��������:" + getRunningAppCount());
		tv_avail_memory.setText("ʣ���ڴ�:" + getAvailMemory());
		tv_process_manager_status = (TextView) findViewById(R.id.process_manager_status);
		fillData();

		lv_processmanger.setOnScrollListener(new OnScrollListener() {

			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			/**
			 * 
			 * @param view
			 * @param firstVisibleItem��һ���û��ɼ�����Ŀ��λ��
			 *            .
			 * @param visibleItemCount
			 * @param totalItemCount
			 */
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				int position = lv_processmanger.getFirstVisiblePosition();
				if (userAppInfos != null&& systemAppInfos!=null) {
					if (position < userAppInfos.size()) {
						tv_process_manager_status.setText("�û�����("
								+ userAppInfos.size() + "��)");
					} else {
						tv_process_manager_status.setText("ϵͳ����("
								+ systemAppInfos.size() + "��)");
					}
				}
			}
		});
		
		lv_processmanger.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Object obj = lv_processmanger.getItemAtPosition(position);//����� ������� getItem
				if (obj != null) {
					appinfo = (AppInfo) obj;
					appHandle();
					
				}
			}
		});
		
	}
	
	/**
	 * ���ã�AsyncTask�̴߳���listview����
	 */
	private void fillData() {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				List<AppInfo> appinfos = getAppInfos(getApplicationContext());
				userAppInfos = new ArrayList<AppInfo>();
				systemAppInfos = new ArrayList<AppInfo>();
				for (AppInfo info : appinfos) {
					if (info.isUserapp()) {
						userAppInfos.add(info);
					} else {
						systemAppInfos.add(info);
					}
				}
				return null;
			}

			@Override
			protected void onPreExecute() {
				ll_loading.setVisibility(View.VISIBLE);
				super.onPreExecute();
			}

			@Override
			protected void onPostExecute(Void result) {
				ll_loading.setVisibility(View.INVISIBLE);
				adapter = new AppManagerAdapter();
				lv_processmanger.setAdapter(adapter);
				super.onPostExecute(result);
			}

		}.execute();

	}
	/**
	 * ���ã���ȡ���еİ�װ���ֻ��ϵĳ��򣬲�����ϵͳ�������
	 * @param context
	 * @return
	 */
	public static List<AppInfo> getAppInfos(Context context){
		//1.��ȡ���еİ�װ���ֻ��ϵĳ���.
		activityManager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		runningAppProcessInfos=activityManager.getRunningAppProcesses();
		PackageManager pm = context.getPackageManager();
		List<PackageInfo>  packinfos =	pm.getInstalledPackages(0);
		List<AppInfo> appInfos = new ArrayList<AppInfo>();
		//activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE); 
		 for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcessInfos)  
	        {
			AppInfo appinfo = new AppInfo();
			String packname = runningAppProcessInfo.processName;
			appinfo.setPackname(packname);
			int id = runningAppProcessInfo.pid;  
			appinfo.setId(id);
			try  
            {  
                // ApplicationInfo��AndroidMainfest�ļ���������Application�ڵ�ķ�װ  
                ApplicationInfo applicationInfo = pm
                        .getPackageInfo(packname, 0).applicationInfo;  
                // Ӧ�õ�ͼ��  
                Drawable icon = applicationInfo.loadIcon(pm);  
                appinfo.setIcon(icon);  
                // Ӧ�õ�����  
                String name = applicationInfo.loadLabel(pm)  
                        .toString();  
                appinfo.setName(name);  
  
                // �����Ƿ�ΪϵͳӦ��  
                appinfo.setUserapp((filterApp(applicationInfo)));  
            }  
            catch (Exception e)  
            {  
                e.printStackTrace();  
                  
                //������û�н���ĺ�ͼ���һЩ����ʱ��Ĵ���ʽ  
                appinfo.setName(packname);  
                appinfo.setUserapp(false);  
                appinfo.setIcon(context.getResources().getDrawable(R.drawable.ic_launcher));  
            }  
			MemoryInfo[] memoryInfos = activityManager  
                    .getProcessMemoryInfo(new int[] {id});  
            // �õ�ռ�õ��ڴ�ռ�  
            long memory = memoryInfos[0].getTotalPss(); 
			appinfo.setMemory(memory*1024);
			appInfos.add(appinfo);
			appinfo=null;
		}
		return appInfos;
	}
	
	 public static boolean filterApp(ApplicationInfo info)  
	    {  
	        // ��ЩϵͳӦ���ǿ��Ը��µģ�����û��Լ�������һ��ϵͳ��Ӧ����������ԭ���ģ�  
	        // ���Ͳ���ϵͳӦ��������������ж����������  
	        if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)  
	        {  
	            return true;  
	        }  
	        else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0)// �ж��ǲ���ϵͳӦ��  
	        {  
	            return true;  
	        }  
	        return false;  
	    }  
	  
	 private int getRunningAppCount()  
	    {  	activityManager=(ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
	        runningAppProcessInfos = activityManager.getRunningAppProcesses();  
	        return runningAppProcessInfos.size();  
	    } 
	 
	 private String getAvailMemory()  
	    {  
	        // newһ���ڴ�Ķ���  
	        android.app.ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();  
	        // �õ�����ϵͳ������ڴ���Ϣ  
	        activityManager.getMemoryInfo(memoryInfo);  
	        // �õ���Ч���ڴ�ռ�  
	        long size = memoryInfo.availMem;  
	        return new TextFormat().formatByte(size);  
	    } 
	
	/**
	 * ���ã�listview��������
	 */
	@SuppressLint("ResourceAsColor")
	private class AppManagerAdapter extends BaseAdapter {

		public int getCount() {
			return userAppInfos.size() + 1 + systemAppInfos.size() + 1;
		}

		public Object getItem(int position) {
			AppInfo appinfo;
			if (position == 0 || position == (userAppInfos.size() + 1)) {
				return null;
			} else if (position <= userAppInfos.size()) {
				// �û�����
				int newpostion = position - 1;
				appinfo = userAppInfos.get(newpostion);
			} else {
				// ϵͳ����
				int newposition = position - 1 - userAppInfos.size() - 1;
				appinfo = systemAppInfos.get(newposition);
			}
			return appinfo;
		}

		public long getItemId(int position) {
			return 0;
		}

		@Override
		public boolean isEnabled(int position) {
			if (position == 0 || position == (userAppInfos.size() + 1))
				return false;

			return super.isEnabled(position);
		}
		
		

		public View getView(int position, View convertView, ViewGroup parent) {
			AppInfo appinfo;
			if (position == 0) {
				TextView tv = new TextView(getApplicationContext());
				tv.setText("�û�����(" + userAppInfos.size() + "��)");
				tv.setTextColor(Color.BLACK);
				tv.setBackgroundColor(R.color.gray);
				return tv;
			} else if (position == (userAppInfos.size() + 1)) {
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.BLACK);
				tv.setBackgroundColor(R.color.gray);
				tv.setText("ϵͳ����(" + systemAppInfos.size() + "��)");
				return tv;
			} else if (position <= userAppInfos.size()) {
				// �û�����
				int newpostion = position - 1;
				appinfo = userAppInfos.get(newpostion);
			} else {
				// ϵͳ����
				int newposition = position - 1 - userAppInfos.size() - 1;
				appinfo = systemAppInfos.get(newposition);
			}

			View view;
			ViewHolder holder;
			if (convertView != null && convertView instanceof RelativeLayout) {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			} else {
				holder = new ViewHolder();
				view = View.inflate(getApplicationContext(),
						R.layout.processmanage_item, null);
				holder.iv = (ImageView) view
						.findViewById(R.id.process_item_icon);
				holder.tv_location = (TextView) view
						.findViewById(R.id.process_item_rongliang);
				holder.tv_name = (TextView) view
						.findViewById(R.id.process_item_name);
//				holder.tv_version = (TextView) view
//						.findViewById(R.id.app_item_version);
//				holder.tv_time= (TextView) view
//						.findViewById(R.id.app_item_time);
				view.setTag(holder);

			}

			holder.iv.setImageDrawable(appinfo.getIcon());
			holder.tv_location.setText("ռ���ڴ棺"+new TextFormat().formatByte(appinfo.getMemory()));
//			if (appinfo.isInrom()) {
//				holder.tv_location.setText("�ֻ��ڴ�");
//			} else {
//				holder.tv_location.setText("SD��");
//			}
			holder.tv_name.setText(appinfo.getName());
//			holder.tv_version.setText(appinfo.getVersion());
//			holder.tv_time.setText(longToDate(appinfo.getTime()));
//			
			
			return view;
		}

	}

	static class ViewHolder {
		ImageView iv;
		TextView tv_name;
		TextView tv_location;
//		TextView tv_version;
//		TextView tv_time;
	}
	/**
	 * ���ã�����listviewÿһ��ʱ����ʾAlertDialog��������AlertDialog��ÿһ���
	 */
	private void appHandle() {
		OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// ���ļ�
				if (which == 0) {
					startApk();
				}
				// �޸��ļ���
				else if (which == 1) {
					uninstallApk();
				}
				// ɾ���ļ�
				else if (which==2){
					detailApk();
				}
				else if (which==3){
					turnmarketApk();
				}
				else{
					killTask();
				}
			}
		};
		// ѡ���ļ�ʱ��������ɾ�ò���ѡ��Ի���
		String[] menu = { "��", "ж��", "����","��ת�г�","ɱ������"};
		new AlertDialog.Builder(ProcessManagerActivity.this).setTitle("��ѡ��Ҫ���еĲ���")
				.setItems(menu, listener)
				.setPositiveButton("ȡ��", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();
	}
	
	private void startApk() {
		// ��ȡ��ǰӦ�ó���ĵ�һ��activity.
		try {
			PackageInfo packinfo = getPackageManager().getPackageInfo(
					appinfo.getPackname(), PackageManager.GET_ACTIVITIES);//��ȡACTIVITIES����Ϣ
			ActivityInfo[] infos = packinfo.activities;
			if (infos != null && infos.length > 0) {
				ActivityInfo activityinfo = infos[0];
				String classname = activityinfo.name;
				Intent intent = new Intent();
				intent.setClassName(appinfo.getPackname(), classname);//һ��Ӧ�� ������һ��Ӧ�� 
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//���� Ҫ���� ���flag
				startActivity(intent);
			} else {
				Toast.makeText(this, "�޷�������ǰӦ��", 0).show();
			}

		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "�޷�������ǰӦ��", 0).show();
		}

	}
	private void detailApk() {
		//��ȡ��ǰӦ�ó����Ӧ����Ϣ.
				Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				Uri uri = Uri.fromParts("package", appinfo.getPackname(), null);
				intent.setData(uri);
				startActivity(intent);
	}
	/**
	 * ���ã�ɾ��Ӧ�ó���
	 */
	private void uninstallApk() {
		//ɾ��Ӧ�ó���
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		intent.setAction("android.intent.action.DELETE");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setData(Uri.parse("package:" + appinfo.getPackname()));
		startActivityForResult(intent, 0);//1ж�� �� ˢ�µ�ǰ�б�  2��д onActivityResult���� ���°� ����
	}
	/**
	 * ���ã���ת���ֻ��г�����������ʾToast
	 */
	private void turnmarketApk() {
		//��ת�ֻ��г�
		try{
		    Uri uri = Uri.parse("market://details?id="+appinfo.getPackname());  
		    Intent intent = new Intent(Intent.ACTION_VIEW,uri);  
		    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);          
		    startActivity(intent);
		}catch(ActivityNotFoundException e){
		    Toast.makeText(getBaseContext(), "�޷����г�!", Toast.LENGTH_SHORT).show();
		}
	}
	 
	 private void killTask()
	 {  
	     long memorySize = appinfo.getMemory();  
	     // ɱ������  
	     activityManager.killBackgroundProcesses(appinfo.getPackname()); 
//			Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
//			method.invoke(activityManager,appinfo.getPackname());
	     if(appinfo.isUserapp())
	    	 userAppInfos.remove(appinfo); 
	     else
	    	 systemAppInfos.remove(appinfo);        
	     Toast.makeText(  
	             this,  
	             "�ͷ���"  
	                     + new TextFormat().formatByte(memorySize) + "�ռ�",  
	             Toast.LENGTH_SHORT).show();  
	       
	     //���¼��ؽ���  
	     adapter = new AppManagerAdapter();  
	     lv_processmanger.setAdapter(adapter);  
	     tv_avail_process.setText("��������:" + getRunningAppCount());
		 tv_avail_memory.setText("ʣ���ڴ�:" + getAvailMemory());
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
	 
	 public static String longToDate(long lo){
		 //longת����
	        Date date = new Date(lo);
	        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
	        return sd.format(date);
	    }
}
