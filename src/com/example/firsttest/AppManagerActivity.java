package com.example.firsttest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.os.StatFs;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
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

public class AppManagerActivity extends Activity {
	private TextView tv_avail_rom;
	private TextView tv_avail_sd;
	private ListView lv_appmanger;
	private View ll_loading;
	private List<AppInfo> userAppInfos;
	private List<AppInfo> systemAppInfos;
	private AppManagerAdapter adapter;
	// 被点击的条目 所代表的对象
	private static AppInfo appinfo;
	private TextView tv_app_manager_status;
	private LayoutInflater infater; 
	private Socket s = null;  
	private BufferedWriter out=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.appmanage);	
		ll_loading = findViewById(R.id.ll_loading);
		tv_avail_rom = (TextView) findViewById(R.id.avail_rom);
		tv_avail_sd = (TextView) findViewById(R.id.avail_sd);
		lv_appmanger = (ListView) findViewById(R.id.appmanger);
		tv_avail_rom.setText("可用内存:" + getAvailRom());
		tv_avail_sd.setText("可用SD卡:" + getAvailSD());
		tv_app_manager_status = (TextView) findViewById(R.id.app_manager_status);
		fillData();

		lv_appmanger.setOnScrollListener(new OnScrollListener() {

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
				int position = lv_appmanger.getFirstVisiblePosition();
				if (userAppInfos != null&& systemAppInfos!=null) {
					if (position < userAppInfos.size()) {
						tv_app_manager_status.setText("用户程序("
								+ userAppInfos.size() + "个)");
					} else {
						tv_app_manager_status.setText("系统程序("
								+ systemAppInfos.size() + "个)");
					}
				}
			}
		});
		
		lv_appmanger.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Object obj = lv_appmanger.getItemAtPosition(position);//会调用 这个方法 getItem
				if (obj != null) {
					appinfo = (AppInfo) obj;
					appHandle();
					
				}
			}
		});
		
//		Thread thread = new Thread(new MyThread());
//        thread.start();
		
	}
	
	/**
	 * 获取手机Rom可用的空间
	 * 
	 * @return
	 */
	private String getAvailRom() {
		File path = Environment.getDataDirectory();//Rom可用
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		long size = blockSize * availableBlocks;
		return Formatter.formatFileSize(this, size);
	}
	
	/**
	 * 获取手机sd卡可用的空间
	 * 
	 * @return
	 */
	private String getAvailSD() {
		File path = Environment.getExternalStorageDirectory();//sd卡
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize(); // 获取到每一块空间存储数据的大小
		long availableBlocks = stat.getAvailableBlocks();// 得到可用的sd空间的个数

		long size = blockSize * availableBlocks;
		return Formatter.formatFileSize(this, size);
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
				lv_appmanger.setAdapter(adapter);
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
		PackageManager pm = context.getPackageManager();
		List<PackageInfo>  packinfos =	pm.getInstalledPackages(0);
		List<AppInfo> appInfos = new ArrayList<AppInfo>();
		for(PackageInfo packinfo: packinfos){
			AppInfo appinfo = new AppInfo();
			String packname = packinfo.packageName;
			try {
				PackageInfo pkgInfo = pm.getPackageInfo(packname,  
				        PackageManager.GET_PERMISSIONS);
				String sharedPkgList[] = pkgInfo.requestedPermissions;
				appinfo.setAuthority(sharedPkgList);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			appinfo.setPackname(packname);
			String version = packinfo.versionName;
			appinfo.setVersion(version);
			long initialtime=packinfo.firstInstallTime;
			appinfo.setTime(initialtime);
			Drawable icon = packinfo.applicationInfo.loadIcon(pm);
			appinfo.setIcon(icon);
			String name = packinfo.applicationInfo.loadLabel(pm).toString();
			appinfo.setName(name);
			getPkgSize(context,packinfo.packageName,appinfo);
			if((packinfo.applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) !=0){
				//安装在sd卡
				appinfo.setInrom(false);
			}else{
				appinfo.setInrom(true);
			}
			if( (packinfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)==1 ){
				appinfo.setUserapp(false);
			}else{
				appinfo.setUserapp(true);
			}
			appInfos.add(appinfo);
		}
		return appInfos;
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
						R.layout.appmanage_item, null);
				holder.iv = (ImageView) view
						.findViewById(R.id.app_item_icon);
				holder.tv_location = (TextView) view
						.findViewById(R.id.app_item_location);
				holder.tv_name = (TextView) view
						.findViewById(R.id.app_item_name);
				holder.tv_version = (TextView) view
						.findViewById(R.id.app_item_version);
				holder.tv_time= (TextView) view
						.findViewById(R.id.app_item_time);
				view.setTag(holder);

			}

			holder.iv.setImageDrawable(appinfo.getIcon());
			if (appinfo.isInrom()) {
				holder.tv_location.setText("手机内存");
			} else {
				holder.tv_location.setText("SD卡");
			}
			holder.tv_name.setText(appinfo.getName());
			holder.tv_version.setText(appinfo.getVersion());
			holder.tv_time.setText(longToDate(appinfo.getTime()));
			
			
			return view;
		}

	}

	static class ViewHolder {
		ImageView iv;
		TextView tv_name;
		TextView tv_location;
		TextView tv_version;
		TextView tv_time;
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
				else if (which==4){
					getdetail();
				}
				else{
					getauthority();
				}
			}
		};
		// 选择文件时，弹出增删该操作选项对话框
		String[] menu = { "打开", "卸载", "详情","跳转市场","大小信息","权限信息"};
		new AlertDialog.Builder(AppManagerActivity.this).setTitle("请选择要进行的操作")
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
	/**
	 * 作用:用来显示大小信息对话框
	 */
	private void getdetail(){
		infater = (LayoutInflater) AppManagerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialog = infater.inflate(R.layout.appinfo, null) ;
		TextView tvcachesize =(TextView) dialog.findViewById(R.id.tvcachesize) ; //缓存大小
		TextView tvdatasize = (TextView) dialog.findViewById(R.id.tvdatasize)  ; //数据大小
		TextView tvcodesize = (TextView) dialog.findViewById(R.id.tvcodesize) ; // 应用程序大小
		TextView tvtotalsize = (TextView) dialog.findViewById(R.id.tvtotalsize) ; //总大小
		//类型转换并赋值
		tvcachesize.setText(new TextFormat().formatByte(appinfo.getcachesize()));
		tvdatasize.setText(new TextFormat().formatByte(appinfo.getdatasize())) ;
		tvcodesize.setText(new TextFormat().formatByte(appinfo.getcodesize())) ;
		tvtotalsize.setText(new TextFormat().formatByte(appinfo.getappsize())) ;
		//显示自定义对话框
		AlertDialog.Builder builder =new AlertDialog.Builder(AppManagerActivity.this) ;
		builder.setView(dialog) ;
		builder.setTitle(appinfo.getName()+"的大小信息为：") ;
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel() ;  // 取消显示对话框
			}
			
		});
		builder.create().show() ;
	}
	/**
	 * @throws NameNotFoundException 
	 * 作用：获取应用权限
	 */
	private void getauthority(){
		infater = (LayoutInflater) AppManagerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialog = infater.inflate(R.layout.appauthority, null) ;
		TextView tvauthority =(TextView) dialog.findViewById(R.id.authority) ; //缓存大小
		String sharedPkgList[]=appinfo.getAuthority();
		try {
			PackageManager pm = getApplicationContext().getPackageManager(); 
		 for (int i = 0; i < sharedPkgList.length; i++) {  
             String permName = sharedPkgList[i];  
             tvauthority.append(i + "-" + permName + "\n");  
             try{
             PermissionInfo tmpPermInfo;
				tmpPermInfo = pm.getPermissionInfo(permName, 0);
             PermissionGroupInfo pgi = pm.getPermissionGroupInfo(  
                   tmpPermInfo.group, 0);//权限分为不同的群组，通过权限名，我们得到该权限属于什么类型的权限。         
             tvauthority.append(i + "-" + pgi.loadLabel(pm).toString() + "\n");  
             tvauthority.append(i + "-" + tmpPermInfo.loadLabel(pm).toString()+ "\n");  
             tvauthority.append(i + "-" + tmpPermInfo.loadDescription(pm).toString()+ "\n");  
             tvauthority.append("\n");  
             }
             catch(NameNotFoundException e){
            	 tvauthority.append("\n");
            	 Log.e("##ddd", "Could'nt retrieve permissions for package");  
            	 //Toast.makeText(getBaseContext(), "存在未知权限信息，无法获取说明!", Toast.LENGTH_SHORT).show();
             }
		 }//通过permName得到该权限的详细信息  
         }  
	catch (NullPointerException e){
		 tvauthority.setText("无权限声明");
		 //Toast.makeText(getBaseContext(), "没有声明权限", Toast.LENGTH_SHORT).show();
	}
		//显示自定义对话框
		AlertDialog.Builder builder =new AlertDialog.Builder(AppManagerActivity.this) ;
		builder.setView(dialog) ;
		builder.setTitle(appinfo.getName()+"的权限信息为：") ;
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel() ;  // 取消显示对话框
			}
			
		});
		builder.create().show() ;
		//BtnSend_Click();用于socket传输权限演示
	}
	
	/**
	  * 作用：-----获取包的大小-----
	  * @param context 上下文
	  * @param pkgName app的包名
	  * @param appInfo 实体类，用于存放App的某些信息
	  */
	 public static void getPkgSize(final Context context, String pkgName, final AppInfo appinfo) {
	  // getPackageSizeInfo是PackageManager中的一个private方法，所以需要通过反射的机制来调用
	  Method method;
	  try {
	   method = PackageManager.class.getMethod("getPackageSizeInfo",
	     new Class[]{String.class, IPackageStatsObserver.class});
	   // 调用 getPackageSizeInfo 方法，需要两个参数：1、需要检测的应用包名；2、回调
	   method.invoke(context.getPackageManager(), pkgName,
	     new IPackageStatsObserver.Stub() {
	      @Override
	      public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
	       if (succeeded && pStats != null) {
	        synchronized (AppInfo.class) {
	         appinfo.setCachesize(pStats.cacheSize);//缓存大小
	         appinfo.setDatasize(pStats.dataSize); //数据大小
	         appinfo.setCodesize(pStats.codeSize); //应用大小
	         appinfo.setAppsize(pStats.cacheSize + pStats.codeSize + pStats.dataSize);//应用的总大小
//			 Log.d("asdasdxxca",appinfo.getcachesize()+"");
//	         Log.d("asdasdxxco",appinfo.getcodesize()+"");
//	         Log.d("asdasdxxda",appinfo.getdatasize()+"");
	         //System.out.println("11111111111111"+appinfo.getcachesize()+"");
	        }
	       }
	      }
	     });
	  } catch (Exception e) {
	   e.printStackTrace();
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
	 
	 public static String longToDate(long lo){
		 //long转日期
	        Date date = new Date(lo);
	        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
	        return sd.format(date);
	    }
	 
//	 private void runTcpClient(String str) {  
//	        try {  
//	        	System.out.println("123333333333333333333");
//	            Socket s = new Socket();//注意host改成你服务器的hostname或IP地址
//	            s.connect(new InetSocketAddress("10.0.2.2", 8777));
//	            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));  
//	            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream())); 
//	            //send output msg  
//	            //String outMsg = "TCP connecting to " + 8777 + System.getProperty("line.separator");   
//	            String outMsg = str+System.getProperty("line.separator");  
//	            out.write(outMsg);//发送数据  
//	            out.flush();  
//	            Log.i("TcpClient", "sent: " + outMsg);  
//	            //accept server response  
//	            //String inMsg = in.readLine() + System.getProperty("line.separator");//得到服务器返回的数据  
//	            //Log.i("TcpClient", "received: " + inMsg);  
//	            //close connection  
//	            s.close();  
//	        } catch (UnknownHostException e) {  
//	            e.printStackTrace();  
//	        } catch (IOException e) {  
//	            e.printStackTrace();  
//	        }   
//	    } 
	 private void runTcpClient() throws IOException {    
			 StringBuilder sb=new StringBuilder();
			 String sharedPkgList[]=appinfo.getAuthority();
			 sb.append(appinfo.getPackname()+"的权限是\n");
				try {
					PackageManager pm = getApplicationContext().getPackageManager(); 
				 for (int i = 0; i < sharedPkgList.length; i++) {  
		             String permName = sharedPkgList[i];  
		             sb.append(i + "-" + permName + "\n");  
		             try{
		             PermissionInfo tmpPermInfo;
						tmpPermInfo = pm.getPermissionInfo(permName, 0);
		             PermissionGroupInfo pgi = pm.getPermissionGroupInfo(  
		                   tmpPermInfo.group, 0);//权限分为不同的群组，通过权限名，我们得到该权限属于什么类型的权限。         
		             sb.append(i + "-" + pgi.loadLabel(pm).toString() + "\n");  
		             sb.append(i + "-" + tmpPermInfo.loadLabel(pm).toString()+ "\n");  
		             sb.append(i + "-" + tmpPermInfo.loadDescription(pm).toString()+ "\n");  
		             sb.append("\n");  
		             }
		             catch(NameNotFoundException e){
		            	 sb.append("\n");
		            	 Log.e("##ddd", "Could'nt retrieve permissions for package");  
		            	 //Toast.makeText(getBaseContext(), "存在未知权限信息，无法获取说明!", Toast.LENGTH_SHORT).show();
		             }
				 }//通过permName得到该权限的详细信息  
		         }  
			catch (NullPointerException e){
				sb.append("无权限声明");
				 //Toast.makeText(getBaseContext(), "没有声明权限", Toast.LENGTH_SHORT).show();
			}
		try {
			 s = new Socket();//注意host改成你服务器的hostname或IP地址
			 s.connect(new InetSocketAddress("10.0.2.2", 8777));
			 //BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));  
			 out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream())); 
			 //send output msg  
			 //String outMsg = "TCP connecting to " + 8777 + System.getProperty("line.separator");   
			 String outMsg = sb.toString();  
			 out.write(outMsg);//发送数据  
			 out.flush();  
			 Log.i("TcpClient", "sent: " + outMsg);  
			 s.shutdownOutput();  
			 //accept server response  
			 //String inMsg = in.readLine() + System.getProperty("line.separator");//得到服务器返回的数据  
			 //Log.i("TcpClient", "received: " + inMsg);  
			 //close connection  
			 //s.close();  
		 } catch (UnknownHostException e) {  
			 e.printStackTrace();  
		 } catch (IOException e) {  
			 e.printStackTrace();  
		 }finally{    
	            if (out != null)  
	            	out.close();   
	            if (s != null)  
	                s.close();      
	        } 
		 
	 } 
	 
//	 class MyThread implements Runnable{
//		    public void run(){
//		        try {
//		        	System.out.println("---------------9");
//		            Socket socket = new Socket("10.0.2.2",8777);
//		            System.out.println("---------------8");
//		        } catch (Exception e) {
//		            e.printStackTrace();
//		        }
//		    }
//		     
//		}
	 
	 public void BtnSend_Click() {
		    new Thread() {
		        @Override
		        public void run() {
		        	try {
						runTcpClient();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
		    }.start();
		}

}
