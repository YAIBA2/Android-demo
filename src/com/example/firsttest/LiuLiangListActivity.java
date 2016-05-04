package com.example.firsttest;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.ListView;

public class LiuLiangListActivity extends Activity {
	private List<Liuliang> lingliangList = new ArrayList<Liuliang>();
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.liulianglist);

		dialog = ProgressDialog.show(LiuLiangListActivity.this, "������...",
				"������������......", true);
		/*
		 * Intent intent1=new
		 * Intent(LiuLiangActivity.this,LiuLiangListActivity.class);
		 * startActivity(intent1); //��ʾ����
		 */new Thread(new Runnable() {
			@Override
			public void run() {
				initLiuliang();

				handler.sendEmptyMessage(0);// ִ�к�ʱ�ķ���֮��������handler
			}
		}).start();

		//

	}

	private void initLiuliang() {
		// TODO Auto-generated method stub
		PackageManager pm = getPackageManager();
		List<PackageInfo> installedPackages = pm
				.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES
						| PackageManager.GET_PERMISSIONS);
		for (PackageInfo info : installedPackages) {
			// ��ȡȨ������
			String[] permissions = info.requestedPermissions;
			if (permissions != null && permissions.length > 0) {
				for (String permission : permissions) {
					if (permission.equals(Manifest.permission.INTERNET)) {
						ApplicationInfo applicationInfo = info.applicationInfo;
						Drawable icon = applicationInfo.loadIcon(pm);
						String appname = applicationInfo.loadLabel(pm)
								.toString();
						String packagename = applicationInfo.packageName;
						int uid = applicationInfo.uid;
						Liuliang trafficInfo = new Liuliang(icon, appname,
								packagename, uid);
						lingliangList.add(trafficInfo);
					}
				}
			}
		}
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {// handler���յ���Ϣ��ͻ�ִ�д˷���
			LiuLiangListAdapter adapter = new LiuLiangListAdapter(
					LiuLiangListActivity.this, R.layout.liulianglist_item,
					lingliangList);
			ListView listView = (ListView) findViewById(R.id.liulianglistview);
			listView.setAdapter(adapter);
			dialog.dismiss();// �ر�ProgressDialog
		}
	};
}
