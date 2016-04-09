package com.example.firsttest;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddressAddActivity extends Activity {
	private EditText name;
	private EditText phone;
	private EditText email;
	private Button newaddress;
	private Button updateaddress;
	private String addressphone;
	private String addressname;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.address_add);

		name = (EditText) findViewById(R.id.address_name_add);
		phone = (EditText) findViewById(R.id.address_phone_add);
		email = (EditText) findViewById(R.id.address_email_add);

		Intent intent = getIntent();
		if (intent.hasExtra("phone")) {
			addressphone = intent.getStringExtra("phone");

			if (addressphone.length() != 0) {
				phone.setText(addressphone);
			}
		}
		if (intent.hasExtra("name")) {
			addressname = intent.getStringExtra("name");

			if (addressname.length() != 0) {
				name.setText(addressname);
			}
		}
		newaddress = (Button) findViewById(R.id.address_button);
		newaddress.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String addname = name.getText().toString().trim();
				String addphone = phone.getText().toString().trim();
				String addemail = email.getText().toString().trim();
				if (!PhoneNumberUtils.isGlobalPhoneNumber(addphone)) {
					Toast.makeText(AddressAddActivity.this, "电话不合法，请重新输入",
							Toast.LENGTH_LONG).show();
				} else if (!isName(addname)) {
					Toast.makeText(AddressAddActivity.this, "姓名不合法，请重新输入",
							Toast.LENGTH_LONG).show();
				} else if (!isEmail(addemail)) {
					Toast.makeText(AddressAddActivity.this, "Email不合法，请重新输入",
							Toast.LENGTH_LONG).show();
				} else {
						boolean addresult = Insertadress(addname, addphone,
								addemail);
						if (addresult) {
							Toast.makeText(AddressAddActivity.this, "新建成功",
									Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(AddressAddActivity.this, "新建失败",
									Toast.LENGTH_LONG).show();
						}
					}
			}
		});
		updateaddress = (Button) findViewById(R.id.update_button);
		updateaddress.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String addname = name.getText().toString().trim();
				String addphone = phone.getText().toString().trim();
				String addemail = email.getText().toString().trim();
				if (!PhoneNumberUtils.isGlobalPhoneNumber(addphone)) {
					Toast.makeText(AddressAddActivity.this, "电话不合法，请重新输入",
							Toast.LENGTH_LONG).show();
				} else if (!isName(addname)) {
					Toast.makeText(AddressAddActivity.this, "姓名不合法，请重新输入",
							Toast.LENGTH_LONG).show();
				} else if (!isEmail(addemail)) {
					Toast.makeText(AddressAddActivity.this, "Email不合法，请重新输入",
							Toast.LENGTH_LONG).show();
				} else {
						boolean addresult = Updateadress(addname, addphone,
								addemail);
						if (addresult) {
							Toast.makeText(AddressAddActivity.this, "更新成功",
									Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(AddressAddActivity.this, "更新失败",
									Toast.LENGTH_LONG).show();
					} 
				
				}
			}
		});
	}

	protected boolean isEmail(String email) {
		String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);
		return m.matches();

	}

	protected boolean isName(String name1) {
		String str = "^(([\u4e00-\u9fa5]{2,8})|([a-zA-Z]{2,16}))$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(name1);
		return m.matches();

	}

	protected boolean Insertadress(String name, String phone, String Email) {
		boolean result = true;
		Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
		ContentResolver resolver = getContentResolver();
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		ContentProviderOperation op1 = ContentProviderOperation.newInsert(uri)
				.withValue("account_name", null).build();
		operations.add(op1);

		uri = Uri.parse("content://com.android.contacts/data");
		ContentProviderOperation op2 = ContentProviderOperation.newInsert(uri)
				.withValueBackReference("raw_contact_id", 0)
				.withValue("mimetype", "vnd.android.cursor.item/name")
				.withValue("data2", name).build();
		operations.add(op2);

		ContentProviderOperation op3 = ContentProviderOperation.newInsert(uri)
				.withValueBackReference("raw_contact_id", 0)
				.withValue("mimetype", "vnd.android.cursor.item/phone_v2")
				.withValue("data1", phone).withValue("data2", "2").build();
		operations.add(op3);

		ContentProviderOperation op4 = ContentProviderOperation.newInsert(uri)
				.withValueBackReference("raw_contact_id", 0)
				.withValue("mimetype", "vnd.android.cursor.item/email_v2")
				.withValue("data1", Email).withValue("data2", "2").build();
		operations.add(op4);

		try {
			resolver.applyBatch("com.android.contacts", operations);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = false;
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = false;
		}

		return result;

	}

	protected boolean Updateadress(String name, String phone, String Email) {
		boolean result = true;
		String contactId="";
		Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, 
                null, null, null, null);
       int contactIdIndex = 0;
       int nameIndex = 0;
       
       if(cursor.getCount() > 0) {
           contactIdIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
           nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
       }
       a:while(cursor.moveToNext()) {
           contactId = cursor.getString(contactIdIndex);
           String name1 = cursor.getString(nameIndex);
          /* if(name1.equals(addressname)){
        	   cursor.close();
        	   break;
           }*/
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
               if(phoneNumber.equals(addressphone)&&name1.equals(addressname)){
            	   phones.close();
            	   cursor.close();
            	   break a;
               }
           }
           }
       
		 Uri uri = Uri.parse("content://com.android.contacts/data");//对data表的所有数据操作 
		    ContentResolver resolver = getContentResolver(); 
		    ContentValues values = new ContentValues(); 
		    values.put("data1", phone); 
		    resolver.update(uri, values, "mimetype=? and raw_contact_id=?", new String[]{"vnd.android.cursor.item/phone_v2",contactId});
		    values.clear();
		    values.put("data1", name); 
		    resolver.update(uri, values, "mimetype=? and raw_contact_id=?", new String[]{"vnd.android.cursor.item/name",contactId});  
		    values.clear();
		    values.put("data2", "2");   //单位 
	        values.put("data1", Email);
		    resolver.update(uri, values, "mimetype=? and raw_contact_id=?", new String[]{"vnd.android.cursor.item/email_v2",contactId});  

		return result;
	}
}
