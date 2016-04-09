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
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ContactsActivity extends Activity {
	private List<Addresss> AddressList = new ArrayList<Addresss>();
	private static final int ITEM_CALL = 1;  
	private static final int ITEM_DEL = 2;  
	private static final int ITEM_UPDATE = 3;  
	private AddressAdapter adapter;  
	

protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_NO_TITLE);
	setContentView(R.layout.address);

	initAddress();
	adapter = new AddressAdapter(ContactsActivity.this,
			R.layout.address_item, AddressList);
	ListView listView = (ListView) findViewById(R.id.listView2);
	listView.setAdapter(adapter);
	registerForContextMenu(listView);//设置长按菜单

	Button titleBack = (Button) findViewById(R.id.title_back1);
	Button titleEdit = (Button) findViewById(R.id.call);
	Button titleAdd = (Button) findViewById(R.id.contact_add);
	titleBack.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			ContactsActivity.this.finish();
		}
	});
	titleEdit.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
			/*Toast.makeText(getBaseContext(), "You clicked Edit button",
					Toast.LENGTH_SHORT).show();*/
			Intent intent1=new Intent(ContactsActivity.this,CalllogActivity.class);
			startActivity(intent1); //显示调用
		}
	});
	
	titleAdd.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
			/*Toast.makeText(getBaseContext(), "You clicked Edit button",
					Toast.LENGTH_SHORT).show();*/
			Intent intent1=new Intent(ContactsActivity.this,AddressAddActivity.class);
			startActivity(intent1); //显示调用
		}
	});
	
}
	
	private void initAddress() {
		Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, 
                null, null, null, null);
       int contactIdIndex = 0;
       int nameIndex = 0;
       
       if(cursor.getCount() > 0) {
           contactIdIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
           nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
       }
       while(cursor.moveToNext()) {
           String contactId = cursor.getString(contactIdIndex);
           String name = cursor.getString(nameIndex);
           /*
            * 查找该联系人的phone信息
            */
           Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
                   null, 
                   ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, 
                   null, null);
           int phoneIndex = 0;
           if(phones.getCount() > 0) {
               phoneIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
           }
           while(phones.moveToNext()) {
               String phoneNumber = phones.getString(phoneIndex);
               AddressList.add(new Addresss(name,phoneNumber));
               Log.i("TAG", phoneNumber);
           }
           
       }
    }
	
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		//super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("请选择操作");
		menu.add(0, ITEM_CALL, 0, "呼叫");
		menu.add(0, ITEM_DEL, 0, "删除");
		menu.add(0, ITEM_UPDATE, 0, "更新");

	}
	
	@Override//处理呼叫按钮
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();  // info.targetView得到list.xml中的LinearLayout对象.  
		int stuId = (int) info.position;
		//System.out.println(stuId+"mmmmmmm");
		Addresss Address=AddressList.get(stuId);
		//System.out.println(Address.getcontent()+"11111");
		if(item.getItemId()==ITEM_CALL){
		Intent intent =new Intent(Intent.ACTION_DIAL);
		Uri data=Uri.parse("tel:"+Address.getphone());
		intent.setData(data);
		startActivity(intent);
		}
		if(item.getItemId()==ITEM_DEL){
			String name=Address.getName();
			Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");  
	        ContentResolver resolver = getContentResolver();  
	        Cursor cursor = resolver.query(uri, new String[]{Data._ID},"display_name=?", new String[]{name}, null);  
	        if(cursor.moveToFirst()){  
	            int id = cursor.getInt(0);  
	            //根据id删除data中的相应数据  
	            resolver.delete(uri, "display_name=?", new String[]{name});  
	            uri = Uri.parse("content://com.android.contacts/data");  
	            resolver.delete(uri, "raw_contact_id=?", new String[]{id+""});  
	        }  
	        AddressList.remove(stuId); 
			adapter.notifyDataSetChanged();
		}
		if(item.getItemId()==ITEM_UPDATE){
			String addressphone=Address.getphone();
			String addressname=Address.getName();
			Intent intent1=new Intent(ContactsActivity.this,AddressAddActivity.class);
			intent1.putExtra("phone", addressphone);
			intent1.putExtra("name", addressname);
			startActivity(intent1);
		}
		return true;
}

}

