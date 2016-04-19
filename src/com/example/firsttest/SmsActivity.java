package com.example.firsttest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.firsttest.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SmsActivity extends Activity {
	private List<Sms> SmsList = new ArrayList<Sms>();
	private Uri SMS_INBOX = Uri.parse("content://sms/inbox");
	private static final int ITEM_DELETE = 1;  
	private static final int ITEM_ADDRESS = 2;  
	private SmsAdapter adapter;  
	
	
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sms);

		initSms();
		adapter = new SmsAdapter(SmsActivity.this,
				R.layout.sms_item, SmsList);
		ListView listView = (ListView) findViewById(R.id.listView1);
		listView.setAdapter(adapter);
		/*listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Sms sms = SmsList.get(position);
				
				 * Toast.makeText(MainActivity.this, fruit.getName(),
				 * Toast.LENGTH_SHORT).show();
				 

			}
		});*/
		registerForContextMenu(listView);//���ó����˵�

		Button titleBack = (Button) findViewById(R.id.title_back);
		Button titleEdit = (Button) findViewById(R.id.title_edit);
		titleBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SmsActivity.this.finish();
			}
		});
		titleEdit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/*Toast.makeText(getBaseContext(), "You clicked Edit button",
						Toast.LENGTH_SHORT).show();*/
				Intent intent1=new Intent(SmsActivity.this,SmsSendActivity.class);
				startActivity(intent1); //��ʾ����
			}
		});
		
		

	}

	@SuppressWarnings("deprecation")
	private void initSms() {
		/*
		 * Sms sms = new Sms("123", "1651", "515"); SmsList.add(sms); Sms sms1 =
		 * new Sms("123", "15616", "799522"); SmsList.add(sms1);
		 */
		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(SMS_INBOX, new String[] { "address", "date",
				"body" }, null, null, "date desc");//�����α��ȡ
		/*
		 * Cursor cursor = managedQuery(SMS_INBOX, new String[] { "address",
		 * "date", "body" }, null, null, null);
		 */// �����÷���
		if (cursor.moveToFirst()) {
			int addrIdx = cursor.getColumnIndex("address");
			int dateIdx = cursor.getColumnIndex("date");
			int bodyIdx = cursor.getColumnIndex("body");
			do {
				String addr = cursor.getString(addrIdx);
				String body = cursor.getString(bodyIdx);
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd hh:mm:ss");
				Date d = new Date(Long.parseLong(cursor.getString(dateIdx)));
				String date = dateFormat.format(d);
				SmsList.add(new Sms(addr,body, date));
			} while (cursor.moveToNext());
		}
		cursor.close();

	}

	@Override//������
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		//super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("��ѡ�����");
		menu.add(0, ITEM_DELETE, 0, "ɾ��");
		menu.add(0, ITEM_ADDRESS, 0, "�½���ϵ��");

	}
	
	@Override//����ɾ����ť
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();  // info.targetView�õ�list.xml�е�LinearLayout����.  
		int stuId = (int) info.position;
		//System.out.println(stuId+"mmmmmmm");
		Sms sms2=SmsList.get(stuId);
		System.out.println(sms2.getcontent()+"11111");
		if(item.getItemId()==ITEM_DELETE){
			String comconStr=sms2.getcontent();
			String comlianxirenStr=sms2.getphone();
			//System.out.println(comconStr+"555555");
			ContentResolver cr = getContentResolver();
			Cursor cursor = cr.query(SMS_INBOX, new String[] { "address", "_id",
					"body" }, null, null, null);
			while (cursor.moveToNext()) {
				String mbody = cursor.getString(cursor.getColumnIndex("body"));//�õ����ݿ��е�����
				String mphone = cursor.getString(cursor.getColumnIndex("address"));
				String id=cursor.getString(cursor.getColumnIndex("_id"));
				/*System.out.println(mbody+"33333");
				System.out.println(id+"ppppp");*/
				// ͨ������ֵ���жϵ��е�listview�е�Item����Ӧ�����ݿ���е�_id
				if (comconStr.equals(mbody)) {
					//System.out.println("2222-----------------");
					
					cursor.close();
					ContentResolver cr1 = getContentResolver();
					cr1.delete(Uri.parse("content://sms"), "_id="+id,null);//ɾ��ƥ������ݿ����¼��_idΪ����
					break;
				}
			SmsList.remove(stuId); 
			adapter.notifyDataSetChanged();
			
		}
		
	}
		if(item.getItemId()==ITEM_ADDRESS){
			String addressphone=sms2.getphone();
			Intent intent1=new Intent(SmsActivity.this,AddressAddActivity.class);
			intent1.putExtra("phone", addressphone);
			startActivity(intent1);
		}
		return true;
}
	
}