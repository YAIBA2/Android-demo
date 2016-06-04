package com.example.firsttest;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class LocationActivity extends Activity {
	private TextView locationInfoTextView = null;
	private Button startButton = null;
	private TextView locationInfoTextView1 = null;
	private Button startButton1 = null;
	private Location location;
	private LocationManager locationManager;
	private LocationClient locationClient = null;
	private static final int UPDATE_TIME = 5000;
	private static int LOCATION_COUTNS = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.location);
        
        locationInfoTextView = (TextView) this.findViewById(R.id.tv_loc_info);
        startButton = (Button) this.findViewById(R.id.btn_start);
        locationInfoTextView1 = (TextView) this.findViewById(R.id.tv_loc_info1);
        startButton1 = (Button) this.findViewById(R.id.btn_start1);
        
        //百度
        locationClient = new LocationClient(this);
        //设置定位条件
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);		//是否打开GPS
        option.setLocationMode(LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");		//设置返回值的坐标类型。
        //option.setPriority(LocationClientOption.NetWorkFirst);	//设置定位优先级
        //option.setProdName("LocationDemo");	//设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
        option.setScanSpan(UPDATE_TIME);    //设置定时定位的时间间隔。单位毫秒
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
        
        //注册位置监听器
        locationClient.registerLocationListener(new BDLocationListener() {
			
			@Override
			public void onReceiveLocation(BDLocation location) {
				// TODO Auto-generated method stub
				if (location == null) {
					return;
				}
				StringBuffer sb = new StringBuffer(256);
				sb.append("Time : ");
				sb.append(location.getTime());
				sb.append("\nError code : ");
				sb.append(location.getLocType());
				sb.append("\nLatitude : ");
				sb.append(location.getLatitude());
				sb.append("\nLontitude : ");
				sb.append(location.getLongitude());
				sb.append("\nRadius : ");
				sb.append(location.getRadius());
				if (location.getLocType() == BDLocation.TypeGpsLocation){
					sb.append("\nSpeed : ");
					sb.append(location.getSpeed());
					sb.append("\nSatellite : ");
					sb.append(location.getSatelliteNumber());
					sb.append("\ndirection : ");
					sb.append(location.getDirection());
			        sb.append("\naddr : ");
			        sb.append(location.getAddrStr());
			        
				} else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
					sb.append("\nAddress : ");
					sb.append(location.getAddrStr());
					//运营商信息
			        sb.append("\noperationers : ");
			        sb.append(location.getOperators());
				}
				LOCATION_COUTNS ++;
				sb.append("\n检查位置更新次数：");
				sb.append(String.valueOf(LOCATION_COUTNS));
				locationInfoTextView.setText(sb.toString());
			}
			
			public void onReceivePoi(BDLocation location) {
			}
			
		});
        
        startButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (locationClient == null) {
					return;
				}
				if (locationClient.isStarted()) {
					startButton.setText("百度API Start");
					locationClient.stop();
				}else {
					startButton.setText("Stop");
					locationClient.start();
					/*
					 *当所设的整数值大于等于1000（ms）时，定位SDK内部使用定时定位模式。
					 *调用requestLocation( )后，每隔设定的时间，定位SDK就会进行一次定位。
					 *如果定位SDK根据定位依据发现位置没有发生变化，就不会发起网络请求，
					 *返回上一次定位的结果；如果发现位置改变，就进行网络请求进行定位，得到新的定位结果。
					 *定时定位时，调用一次requestLocation，会定时监听到定位结果。
					 */
					locationClient.requestLocation();
				}
			}
		});
        
        //系统api
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// 从GPS获取最近的定位信息
		location = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				2000, 8, new LocationListener() {

					@Override
					public void onLocationChanged(Location location) {
						// 当GPS定位信息发生改变时，更新位置
						updateView(location);
					}

					@Override
					public void onProviderDisabled(String provider) {
						updateView(null);
					}

					@Override
					public void onProviderEnabled(String provider) {
						// 当GPS LocationProvider可用时，更新位置
						updateView(locationManager
								.getLastKnownLocation(provider));

					}

					@Override
					public void onStatusChanged(String provider, int status,
							Bundle extras) {
					}
				});
	
		
        startButton1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				updateView(location);
			}
		});
        
    }
    
    private void updateView(Location location) {
		if (location != null) {
			StringBuffer sb = new StringBuffer();
			sb.append("实时的位置信息：\n经度：");
			sb.append(location.getLongitude());
			sb.append("\n纬度：");
			sb.append(location.getLatitude());
			sb.append("\n高度：");
			sb.append(location.getAltitude());
			sb.append("\n速度：");
			sb.append(location.getSpeed());
			sb.append("\n方向：");
			sb.append(location.getBearing());
			sb.append("\n精度：");
			sb.append(location.getAccuracy());
			locationInfoTextView1.setText(sb.toString());
		} else {
			// 如果传入的Location对象为空则清空EditText
			locationInfoTextView1.setText("");
		}
	}
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (locationClient != null && locationClient.isStarted()) {
			locationClient.stop();
			locationClient = null;
		}
	}
}
