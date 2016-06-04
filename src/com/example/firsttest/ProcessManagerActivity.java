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
	// 被点击的条目 所代表的对象
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
		tv_avail_process.setText("进程总数:" + getRunningAppCount());
		tv_avail_memory.setText("剩余内存:" + getAvailMemory());
		tv_process_manager_status = (TextView) findViewById(R.id.process_manager_status);
		fillData();

		lv_processmanger.setOnScrollListener(new OnScrollListener() {

			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			/**
			 * 
			 * @param view
			 * @param firstVisibleItem第一个用户可见的条目的位置
			 *            .
			 * @param visibleItemCount
			 * @param totalItemCount
			 */
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				int position = lv_processmanger.getFirstVisiblePosition();
				if (userAppInfos != null&& systemAppInfos!=null) {
					if (position < userAppInfos.size()) {
						tv_process_manager_status.setText("用户程序("
								+ userAppInfos.size() + "个)");
					} else {
						tv_process_manager_status.setText("系统程序("
								+ systemAppInfos.size() + "个)");
					}
				}
			}
		});
		
		lv_processmanger.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Object obj = lv_processmanger.getItemAtPosition(position);//会调用 这个方法 getItem
				if (obj != null) {
					appinfo = (AppInfo) obj;
					appHandle();
					
				}
			}
		});
		
	}
	
	/**
	 * 作用：AsyncTask线程处理listview加载
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
	 * 作用：获取所有的安装在手机上的程序，并区分系统或第三方
	 * @param context
	 * @return
	 */
	public static List<AppInfo> getAppInfos(Context context){
		//1.获取所有的安装在手机上的程序.
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
                // ApplicationInfo是AndroidMainfest文件里面整个Application节点的封装  
                ApplicationInfo applicationInfo = pm
                        .getPackageInfo(packname, 0).applicationInfo;  
                // 应用的图标  
                Drawable icon = applicationInfo.loadIcon(pm);  
                appinfo.setIcon(icon);  
                // 应用的名字  
                String name = applicationInfo.loadLabel(pm)  
                        .toString();  
                appinfo.setName(name);  
  
                // 设置是否为系统应用  
                appinfo.setUserapp((filterApp(applicationInfo)));  
            }  
            catch (Exception e)  
            {  
                e.printStackTrace();  
                  
                //当遇到没有界面的和图标的一些进程时候的处理方式  
                appinfo.setName(packname);  
                appinfo.setUserapp(false);  
                appinfo.setIcon(context.getResources().getDrawable(R.drawable.ic_launcher));  
            }  
			MemoryInfo[] memoryInfos = activityManager  
                    .getProcessMemoryInfo(new int[] {id});  
            // 拿到占用的内存空间  
            long memory = memoryInfos[0].getTotalPss(); 
			appinfo.setMemory(memory*1024);
			appInfos.add(appinfo);
			appinfo=null;
		}
		return appInfos;
	}
	
	 public static boolean filterApp(ApplicationInfo info)  
	    {  
	        // 有些系统应用是可以更新的，如果用户自己下载了一个系统的应用来更新了原来的，  
	        // 它就不是系统应用啦，这个就是判断这种情况的  
	        if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)  
	        {  
	            return true;  
	        }  
	        else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0)// 判断是不是系统应用  
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
	        // new一个内存的对象  
	        android.app.ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();  
	        // 拿到现在系统里面的内存信息  
	        activityManager.getMemoryInfo(memoryInfo);  
	        // 拿到有效的内存空间  
	        long size = memoryInfo.availMem;  
	        return new TextFormat().formatByte(size);  
	    } 
	
	/**
	 * 作用：listview的适配器
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
				// 用户程序
				int newpostion = position - 1;
				appinfo = userAppInfos.get(newpostion);
			} else {
				// 系统程序
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
				tv.setText("用户程序(" + userAppInfos.size() + "个)");
				tv.setTextColor(Color.BLACK);
				tv.setBackgroundColor(R.color.gray);
				return tv;
			} else if (position == (userAppInfos.size() + 1)) {
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.BLACK);
				tv.setBackgroundColor(R.color.gray);
				tv.setText("系统程序(" + systemAppInfos.size() + "个)");
				return tv;
			} else if (position <= userAppInfos.size()) {
				// 用户程序
				int newpostion = position - 1;
				appinfo = userAppInfos.get(newpostion);
			} else {
				// 系统程序
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
			holder.tv_location.setText("占用内存："+new TextFormat().formatByte(appinfo.getMemory()));
//			if (appinfo.isInrom()) {
//				holder.tv_location.setText("手机内存");
//			} else {
//				holder.tv_location.setText("SD卡");
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
	 * 作用：单击listview每一项时候显示AlertDialog，并定义AlertDialog中每一项动作
	 */
	private void appHandle() {
		OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 打开文件
				if (which == 0) {
					startApk();
				}
				// 修改文件名
				else if (which == 1) {
					uninstallApk();
				}
				// 删除文件
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
		// 选择文件时，弹出增删该操作选项对话框
		String[] menu = { "打开", "卸载", "详情","跳转市场","杀死进程"};
		new AlertDialog.Builder(ProcessManagerActivity.this).setTitle("请选择要进行的操作")
				.setItems(menu, listener)
				.setPositiveButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();
	}
	
	private void startApk() {
		// 获取当前应用程序的第一个activity.
		try {
			PackageInfo packinfo = getPackageManager().getPackageInfo(
					appinfo.getPackname(), PackageManager.GET_ACTIVITIES);//获取ACTIVITIES的信息
			ActivityInfo[] infos = packinfo.activities;
			if (infos != null && infos.length > 0) {
				ActivityInfo activityinfo = infos[0];
				String classname = activityinfo.name;
				Intent intent = new Intent();
				intent.setClassName(appinfo.getPackname(), classname);//一个应用 激活另一个应用 
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//必须 要设置 这个flag
				startActivity(intent);
			} else {
				Toast.makeText(this, "无法开启当前应用", 0).show();
			}

		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "无法开启当前应用", 0).show();
		}

	}
	private void detailApk() {
		//获取当前应用程序的应用信息.
				Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				Uri uri = Uri.fromParts("package", appinfo.getPackname(), null);
				intent.setData(uri);
				startActivity(intent);
	}
	/**
	 * 作用：删除应用程序
	 */
	private void uninstallApk() {
		//删除应用程序
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		intent.setAction("android.intent.action.DELETE");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setData(Uri.parse("package:" + appinfo.getPackname()));
		startActivityForResult(intent, 0);//1卸载 后 刷新当前列表  2重写 onActivityResult方法 重新绑定 数据
	}
	/**
	 * 作用：跳转到手机市场，若无则提示Toast
	 */
	private void turnmarketApk() {
		//跳转手机市场
		try{
		    Uri uri = Uri.parse("market://details?id="+appinfo.getPackname());  
		    Intent intent = new Intent(Intent.ACTION_VIEW,uri);  
		    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);          
		    startActivity(intent);
		}catch(ActivityNotFoundException e){
		    Toast.makeText(getBaseContext(), "无法打开市场!", Toast.LENGTH_SHORT).show();
		}
	}
	 
	 private void killTask()
	 {  
	     long memorySize = appinfo.getMemory();  
	     // 杀死进程  
	     activityManager.killBackgroundProcesses(appinfo.getPackname()); 
//			Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
//			method.invoke(activityManager,appinfo.getPackname());
	     if(appinfo.isUserapp())
	    	 userAppInfos.remove(appinfo); 
	     else
	    	 systemAppInfos.remove(appinfo);        
	     Toast.makeText(  
	             this,  
	             "释放了"  
	                     + new TextFormat().formatByte(memorySize) + "空间",  
	             Toast.LENGTH_SHORT).show();  
	       
	     //重新加载界面  
	     adapter = new AppManagerAdapter();  
	     lv_processmanger.setAdapter(adapter);  
	     tv_avail_process.setText("进程总数:" + getRunningAppCount());
		 tv_avail_memory.setText("剩余内存:" + getAvailMemory());
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
	 
	 public static String longToDate(long lo){
		 //long转日期
	        Date date = new Date(lo);
	        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
	        return sd.format(date);
	    }
}
