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
        
        //�ٶ�
        locationClient = new LocationClient(this);
        //���ö�λ����
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);		//�Ƿ��GPS
        option.setLocationMode(LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");		//���÷���ֵ���������͡�
        //option.setPriority(LocationClientOption.NetWorkFirst);	//���ö�λ���ȼ�
        //option.setProdName("LocationDemo");	//���ò�Ʒ�����ơ�ǿ�ҽ�����ʹ���Զ���Ĳ�Ʒ�����ƣ����������Ժ�Ϊ���ṩ����Ч׼ȷ�Ķ�λ����
        option.setScanSpan(UPDATE_TIME);    //���ö�ʱ��λ��ʱ��������λ����
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
        
        //ע��λ�ü�����
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
					//��Ӫ����Ϣ
			        sb.append("\noperationers : ");
			        sb.append(location.getOperators());
				}
				LOCATION_COUTNS ++;
				sb.append("\n���λ�ø��´�����");
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
					startButton.setText("�ٶ�API Start");
					locationClient.stop();
				}else {
					startButton.setText("Stop");
					locationClient.start();
					/*
					 *�����������ֵ���ڵ���1000��ms��ʱ����λSDK�ڲ�ʹ�ö�ʱ��λģʽ��
					 *����requestLocation( )��ÿ���趨��ʱ�䣬��λSDK�ͻ����һ�ζ�λ��
					 *�����λSDK���ݶ�λ���ݷ���λ��û�з����仯���Ͳ��ᷢ����������
					 *������һ�ζ�λ�Ľ�����������λ�øı䣬�ͽ�������������ж�λ���õ��µĶ�λ�����
					 *��ʱ��λʱ������һ��requestLocation���ᶨʱ��������λ�����
					 */
					locationClient.requestLocation();
				}
			}
		});
        
        //ϵͳapi
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// ��GPS��ȡ����Ķ�λ��Ϣ
		location = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				2000, 8, new LocationListener() {

					@Override
					public void onLocationChanged(Location location) {
						// ��GPS��λ��Ϣ�����ı�ʱ������λ��
						updateView(location);
					}

					@Override
					public void onProviderDisabled(String provider) {
						updateView(null);
					}

					@Override
					public void onProviderEnabled(String provider) {
						// ��GPS LocationProvider����ʱ������λ��
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
			sb.append("ʵʱ��λ����Ϣ��\n���ȣ�");
			sb.append(location.getLongitude());
			sb.append("\nγ�ȣ�");
			sb.append(location.getLatitude());
			sb.append("\n�߶ȣ�");
			sb.append(location.getAltitude());
			sb.append("\n�ٶȣ�");
			sb.append(location.getSpeed());
			sb.append("\n����");
			sb.append(location.getBearing());
			sb.append("\n���ȣ�");
			sb.append(location.getAccuracy());
			locationInfoTextView1.setText(sb.toString());
		} else {
			// ��������Location����Ϊ�������EditText
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
