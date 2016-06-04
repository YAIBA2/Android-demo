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
	// ���������Ŀ ������Ķ���
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
		tv_avail_rom.setText("�����ڴ�:" + getAvailRom());
		tv_avail_sd.setText("����SD��:" + getAvailSD());
		tv_app_manager_status = (TextView) findViewById(R.id.app_manager_status);
		fillData();

		lv_appmanger.setOnScrollListener(new OnScrollListener() {

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
				int position = lv_appmanger.getFirstVisiblePosition();
				if (userAppInfos != null&& systemAppInfos!=null) {
					if (position < userAppInfos.size()) {
						tv_app_manager_status.setText("�û�����("
								+ userAppInfos.size() + "��)");
					} else {
						tv_app_manager_status.setText("ϵͳ����("
								+ systemAppInfos.size() + "��)");
					}
				}
			}
		});
		
		lv_appmanger.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Object obj = lv_appmanger.getItemAtPosition(position);//����� ������� getItem
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
	 * ��ȡ�ֻ�Rom���õĿռ�
	 * 
	 * @return
	 */
	private String getAvailRom() {
		File path = Environment.getDataDirectory();//Rom����
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		long size = blockSize * availableBlocks;
		return Formatter.formatFileSize(this, size);
	}
	
	/**
	 * ��ȡ�ֻ�sd�����õĿռ�
	 * 
	 * @return
	 */
	private String getAvailSD() {
		File path = Environment.getExternalStorageDirectory();//sd��
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize(); // ��ȡ��ÿһ��ռ�洢���ݵĴ�С
		long availableBlocks = stat.getAvailableBlocks();// �õ����õ�sd�ռ�ĸ���

		long size = blockSize * availableBlocks;
		return Formatter.formatFileSize(this, size);
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
				lv_appmanger.setAdapter(adapter);
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
				//��װ��sd��
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
				holder.tv_location.setText("�ֻ��ڴ�");
			} else {
				holder.tv_location.setText("SD��");
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
				else if (which==4){
					getdetail();
				}
				else{
					getauthority();
				}
			}
		};
		// ѡ���ļ�ʱ��������ɾ�ò���ѡ��Ի���
		String[] menu = { "��", "ж��", "����","��ת�г�","��С��Ϣ","Ȩ����Ϣ"};
		new AlertDialog.Builder(AppManagerActivity.this).setTitle("��ѡ��Ҫ���еĲ���")
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
	/**
	 * ����:������ʾ��С��Ϣ�Ի���
	 */
	private void getdetail(){
		infater = (LayoutInflater) AppManagerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialog = infater.inflate(R.layout.appinfo, null) ;
		TextView tvcachesize =(TextView) dialog.findViewById(R.id.tvcachesize) ; //�����С
		TextView tvdatasize = (TextView) dialog.findViewById(R.id.tvdatasize)  ; //���ݴ�С
		TextView tvcodesize = (TextView) dialog.findViewById(R.id.tvcodesize) ; // Ӧ�ó����С
		TextView tvtotalsize = (TextView) dialog.findViewById(R.id.tvtotalsize) ; //�ܴ�С
		//����ת������ֵ
		tvcachesize.setText(new TextFormat().formatByte(appinfo.getcachesize()));
		tvdatasize.setText(new TextFormat().formatByte(appinfo.getdatasize())) ;
		tvcodesize.setText(new TextFormat().formatByte(appinfo.getcodesize())) ;
		tvtotalsize.setText(new TextFormat().formatByte(appinfo.getappsize())) ;
		//��ʾ�Զ���Ի���
		AlertDialog.Builder builder =new AlertDialog.Builder(AppManagerActivity.this) ;
		builder.setView(dialog) ;
		builder.setTitle(appinfo.getName()+"�Ĵ�С��ϢΪ��") ;
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel() ;  // ȡ����ʾ�Ի���
			}
			
		});
		builder.create().show() ;
	}
	/**
	 * @throws NameNotFoundException 
	 * ���ã���ȡӦ��Ȩ��
	 */
	private void getauthority(){
		infater = (LayoutInflater) AppManagerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialog = infater.inflate(R.layout.appauthority, null) ;
		TextView tvauthority =(TextView) dialog.findViewById(R.id.authority) ; //�����С
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
                   tmpPermInfo.group, 0);//Ȩ�޷�Ϊ��ͬ��Ⱥ�飬ͨ��Ȩ���������ǵõ���Ȩ������ʲô���͵�Ȩ�ޡ�         
             tvauthority.append(i + "-" + pgi.loadLabel(pm).toString() + "\n");  
             tvauthority.append(i + "-" + tmpPermInfo.loadLabel(pm).toString()+ "\n");  
             tvauthority.append(i + "-" + tmpPermInfo.loadDescription(pm).toString()+ "\n");  
             tvauthority.append("\n");  
             }
             catch(NameNotFoundException e){
            	 tvauthority.append("\n");
            	 Log.e("##ddd", "Could'nt retrieve permissions for package");  
            	 //Toast.makeText(getBaseContext(), "����δ֪Ȩ����Ϣ���޷���ȡ˵��!", Toast.LENGTH_SHORT).show();
             }
		 }//ͨ��permName�õ���Ȩ�޵���ϸ��Ϣ  
         }  
	catch (NullPointerException e){
		 tvauthority.setText("��Ȩ������");
		 //Toast.makeText(getBaseContext(), "û������Ȩ��", Toast.LENGTH_SHORT).show();
	}
		//��ʾ�Զ���Ի���
		AlertDialog.Builder builder =new AlertDialog.Builder(AppManagerActivity.this) ;
		builder.setView(dialog) ;
		builder.setTitle(appinfo.getName()+"��Ȩ����ϢΪ��") ;
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel() ;  // ȡ����ʾ�Ի���
			}
			
		});
		builder.create().show() ;
		//BtnSend_Click();����socket����Ȩ����ʾ
	}
	
	/**
	  * ���ã�-----��ȡ���Ĵ�С-----
	  * @param context ������
	  * @param pkgName app�İ���
	  * @param appInfo ʵ���࣬���ڴ��App��ĳЩ��Ϣ
	  */
	 public static void getPkgSize(final Context context, String pkgName, final AppInfo appinfo) {
	  // getPackageSizeInfo��PackageManager�е�һ��private������������Ҫͨ������Ļ���������
	  Method method;
	  try {
	   method = PackageManager.class.getMethod("getPackageSizeInfo",
	     new Class[]{String.class, IPackageStatsObserver.class});
	   // ���� getPackageSizeInfo ��������Ҫ����������1����Ҫ����Ӧ�ð�����2���ص�
	   method.invoke(context.getPackageManager(), pkgName,
	     new IPackageStatsObserver.Stub() {
	      @Override
	      public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
	       if (succeeded && pStats != null) {
	        synchronized (AppInfo.class) {
	         appinfo.setCachesize(pStats.cacheSize);//�����С
	         appinfo.setDatasize(pStats.dataSize); //���ݴ�С
	         appinfo.setCodesize(pStats.codeSize); //Ӧ�ô�С
	         appinfo.setAppsize(pStats.cacheSize + pStats.codeSize + pStats.dataSize);//Ӧ�õ��ܴ�С
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
	 
//	 private void runTcpClient(String str) {  
//	        try {  
//	        	System.out.println("123333333333333333333");
//	            Socket s = new Socket();//ע��host�ĳ����������hostname��IP��ַ
//	            s.connect(new InetSocketAddress("10.0.2.2", 8777));
//	            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));  
//	            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream())); 
//	            //send output msg  
//	            //String outMsg = "TCP connecting to " + 8777 + System.getProperty("line.separator");   
//	            String outMsg = str+System.getProperty("line.separator");  
//	            out.write(outMsg);//��������  
//	            out.flush();  
//	            Log.i("TcpClient", "sent: " + outMsg);  
//	            //accept server response  
//	            //String inMsg = in.readLine() + System.getProperty("line.separator");//�õ����������ص�����  
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
			 sb.append(appinfo.getPackname()+"��Ȩ����\n");
				try {
					PackageManager pm = getApplicationContext().getPackageManager(); 
				 for (int i = 0; i < sharedPkgList.length; i++) {  
		             String permName = sharedPkgList[i];  
		             sb.append(i + "-" + permName + "\n");  
		             try{
		             PermissionInfo tmpPermInfo;
						tmpPermInfo = pm.getPermissionInfo(permName, 0);
		             PermissionGroupInfo pgi = pm.getPermissionGroupInfo(  
		                   tmpPermInfo.group, 0);//Ȩ�޷�Ϊ��ͬ��Ⱥ�飬ͨ��Ȩ���������ǵõ���Ȩ������ʲô���͵�Ȩ�ޡ�         
		             sb.append(i + "-" + pgi.loadLabel(pm).toString() + "\n");  
		             sb.append(i + "-" + tmpPermInfo.loadLabel(pm).toString()+ "\n");  
		             sb.append(i + "-" + tmpPermInfo.loadDescription(pm).toString()+ "\n");  
		             sb.append("\n");  
		             }
		             catch(NameNotFoundException e){
		            	 sb.append("\n");
		            	 Log.e("##ddd", "Could'nt retrieve permissions for package");  
		            	 //Toast.makeText(getBaseContext(), "����δ֪Ȩ����Ϣ���޷���ȡ˵��!", Toast.LENGTH_SHORT).show();
		             }
				 }//ͨ��permName�õ���Ȩ�޵���ϸ��Ϣ  
		         }  
			catch (NullPointerException e){
				sb.append("��Ȩ������");
				 //Toast.makeText(getBaseContext(), "û������Ȩ��", Toast.LENGTH_SHORT).show();
			}
		try {
			 s = new Socket();//ע��host�ĳ����������hostname��IP��ַ
			 s.connect(new InetSocketAddress("10.0.2.2", 8777));
			 //BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));  
			 out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream())); 
			 //send output msg  
			 //String outMsg = "TCP connecting to " + 8777 + System.getProperty("line.separator");   
			 String outMsg = sb.toString();  
			 out.write(outMsg);//��������  
			 out.flush();  
			 Log.i("TcpClient", "sent: " + outMsg);  
			 s.shutdownOutput();  
			 //accept server response  
			 //String inMsg = in.readLine() + System.getProperty("line.separator");//�õ����������ص�����  
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
