package com.example.firsttest;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends Activity {

	private List<Fruit> fruitList = new ArrayList<Fruit>();
	private long exitTime = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		initFruits();
		FruitAdapter adapter = new FruitAdapter(MainActivity.this,
				R.layout.fruit_item, fruitList);
		ListView listView = (ListView) findViewById(R.id.list_view);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Fruit fruit = fruitList.get(position);
				/*Toast.makeText(MainActivity.this, fruit.getName(),
						Toast.LENGTH_SHORT).show();*/
				switch (fruit.getImageId()) {
				case R.drawable.sms:
					Intent intent=new Intent(MainActivity.this,SmsActivity.class);
					startActivity(intent); //显示调用
					break;
				case R.drawable.contact:
					Intent intent1=new Intent(MainActivity.this,ContactsActivity.class);
					startActivity(intent1); //显示调用
					break;
				case R.drawable.file:
					Intent intent2=new Intent(MainActivity.this,FileActivity.class);
					startActivity(intent2); //显示调用
					break;
				default:
					break;
				}
				
			}
		});
	}

	private void initFruits() {
		Fruit sms = new Fruit("短信", R.drawable.sms);
		fruitList.add(sms);
		Fruit contact = new Fruit("通讯录", R.drawable.contact);
		fruitList.add(contact);
		Fruit file = new Fruit("文件管理器", R.drawable.file);
		fruitList.add(file);
		/*Fruit orange = new Fruit("Orange", R.drawable.orange_pic);
		fruitList.add(orange);
		Fruit watermelon = new Fruit("Watermelon", R.drawable.watermelon_pic);
		fruitList.add(watermelon);
		Fruit pear = new Fruit("Pear", R.drawable.pear_pic);
		fruitList.add(pear);
		Fruit grape = new Fruit("Grape", R.drawable.grape_pic);
		fruitList.add(grape);
		Fruit pineapple = new Fruit("Pineapple", R.drawable.pineapple_pic);
		fruitList.add(pineapple);
		Fruit strawberry = new Fruit("Strawberry", R.drawable.strawberry_pic);
		fruitList.add(strawberry);
		Fruit cherry = new Fruit("Cherry", R.drawable.cherry_pic);
		fruitList.add(cherry);
		Fruit mango = new Fruit("Mango", R.drawable.mango_pic);
		fruitList.add(mango);*/
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
	        if((System.currentTimeMillis()-exitTime) > 2000){  
	            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();                                
	            exitTime = System.currentTimeMillis();   
	        } else {
	            finish();
	            System.exit(0);
	        }
	        return true;   
	    }
	    return super.onKeyDown(keyCode, event);
	}
}