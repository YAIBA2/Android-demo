package com.example.firsttest;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SmsSendActivity extends Activity {
	private EditText to;

	private EditText msgInput;

	private Button send;
	
	private IntentFilter sendFilter;

	private SendStatusReceiver sendStatusReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sms_send_item);
		to = (EditText) findViewById(R.id.to);
		msgInput = (EditText) findViewById(R.id.msg_input);
		send = (Button) findViewById(R.id.send);
		sendFilter = new IntentFilter();
		sendFilter.addAction("SENT_SMS_ACTION");
		sendStatusReceiver = new SendStatusReceiver();
		registerReceiver(sendStatusReceiver, sendFilter);
		send.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String smsto=to.getText().toString().trim();
				String smsmsgInput=msgInput.getText().toString().trim();
				if(!PhoneNumberUtils.isGlobalPhoneNumber(smsto)){
					Toast.makeText(SmsSendActivity.this, "电话不合法，请重新输入", Toast.LENGTH_LONG).show();
				}else{
				SmsManager smsManager = SmsManager.getDefault();
				Intent sentIntent = new Intent("SENT_SMS_ACTION");
				PendingIntent pi = PendingIntent.getBroadcast(
						SmsSendActivity.this, 0, sentIntent, 0);
				smsManager.sendTextMessage(to.getText().toString(), null,
						msgInput.getText().toString(), pi, null);
			}
			}
		});
	}
	
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(sendStatusReceiver);
	}
	
class SendStatusReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (getResultCode() == RESULT_OK) {
			Toast.makeText(context, "Send succeeded", Toast.LENGTH_LONG)
					.show();
		} else {
			Toast.makeText(context, "Send failed", Toast.LENGTH_LONG)
					.show();
		}
	}

}
}


