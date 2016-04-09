package com.example.firsttest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class CalllogActivity extends Activity {
	private List<Calllog> calllogList = new ArrayList<Calllog>();
	private static final int ITEM_DELETE = 1;
	private static final int ITEM_ADDRESS = 2;
    private final static Uri uriCall = Uri.parse("content://call_log/calls");  
    private CalllogAdapter adapter;  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.calllog);
		initCalllog();
		adapter = new CalllogAdapter(CalllogActivity.this,
				R.layout.calllog_item, calllogList);
		ListView listView = (ListView) findViewById(R.id.listView3);
		listView.setAdapter(adapter);
		registerForContextMenu(listView);
	}

	private void initCalllog() {
		// TODO Auto-generated method stub
		Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI,
				null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				// 号码
				String number = cursor.getString(cursor
						.getColumnIndex(Calls.NUMBER));
				// 呼叫类型
				String type;
				switch (Integer.parseInt(cursor.getString(cursor
						.getColumnIndex(Calls.TYPE)))) {
				case Calls.INCOMING_TYPE:
					type = "呼入";
					break;
				case Calls.OUTGOING_TYPE:
					type = "呼出";
					break;
				case Calls.MISSED_TYPE:
					type = "未接";
					break;
				default:
					type = "挂断";// 应该是挂断.根据我手机类型判断出的
					break;
				}
				SimpleDateFormat sfd = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				Date date = new Date(Long.parseLong(cursor.getString(cursor
						.getColumnIndexOrThrow(Calls.DATE))));
				// 呼叫时间
				String time = sfd.format(date);
				// 联系人
				String name = cursor.getString(cursor
						.getColumnIndexOrThrow(Calls.CACHED_NAME));
				// 通话时间,单位:s
				String duration = cursor.getString(cursor
						.getColumnIndexOrThrow(Calls.DURATION));
				calllogList.add(new Calllog(number, type, duration + "s", time,
						name));
			} while (cursor.moveToNext());

		}
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		// super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("请选择操作");
		menu.add(0, ITEM_DELETE, 0, "删除");
		menu.add(0, ITEM_ADDRESS, 0, "新建联系人");
	}

	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo(); // info.targetView得到list.xml中的LinearLayout对象.
		int stuId = (int) info.position;
		//System.out.println(stuId + "mmmmmmm");
		Calllog sms2 = calllogList.get(stuId);
		//System.out.println(sms2.getcontent() + "11111");
		if (item.getItemId() == ITEM_DELETE) {
			String callphone = sms2.getphone();
			String calltime = sms2.gettime();
			calltime=calltime.substring(0,calltime.length()-1);
			String calltype = sms2.gettype();
			if(calltype.equals("呼入")){
				calltype="1";
			}
			if(calltype.equals("呼出")){
				calltype="2";
			}
			if(calltype.equals("未接")){
				calltype="3";
			}
			//System.out.println(comconStr + "555555");
			ContentResolver cr = getContentResolver();
			/*Cursor cursor = cr.query(uriCall, new String[] { "address",
					"_id", "body" }, null, null, null);
			while (cursor.moveToNext()) {
				String mbody = cursor.getString(cursor.getColumnIndex("body"));// 得到数据库中的数据
				String mphone = cursor.getString(cursor
						.getColumnIndex("address"));
				String id = cursor.getString(cursor.getColumnIndex("_id"));
				System.out.println(mbody + "33333");
				System.out.println(id + "ppppp");
				// 通过主键值来判断点中的listview中的Item所对应的数据库表中的_id
				if (comconStr.equals(mbody)) {
					System.out.println("2222-----------------");

					cursor.close();
					ContentResolver cr1 = getContentResolver();
					cr1.delete(Uri.parse("content://sms"), "_id=" + id, null);// 删除匹配的数据库里记录，_id为主键
					break;*/
				cr.delete(CallLog.Calls.CONTENT_URI, CallLog.Calls.NUMBER+"=? and "+CallLog.Calls.TYPE+"=? and "+CallLog.Calls.DURATION+"=?", new String[]{callphone,calltype,calltime});
				calllogList.remove(stuId);
				adapter.notifyDataSetChanged();

			}
		if(item.getItemId()==ITEM_ADDRESS){
			String addressphone=sms2.getphone();
			Intent intent1=new Intent(CalllogActivity.this,AddressAddActivity.class);
			intent1.putExtra("phone", addressphone);
			startActivity(intent1);
		}

		return true;
	}

}
