package com.example.firsttest;

import java.util.List;

import com.example.firsttest.FruitAdapter.ViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

public class SmsAdapter extends ArrayAdapter<Sms> {
	private int resourceId;
	public SmsAdapter(Context context, int textViewResourceId,
			List<Sms> objects) {
		super(context, textViewResourceId, objects);
		resourceId = textViewResourceId;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Sms sms = getItem(position);
		View view;
		ViewHolder viewHolder;
		if (convertView == null) {
			view = LayoutInflater.from(getContext()).inflate(resourceId, null);
			viewHolder = new ViewHolder();
			viewHolder.lianxiren = (TextView) view.findViewById(R.id.lianxiren);
			viewHolder.shijian = (TextView) view.findViewById(R.id.shijian);
			viewHolder.neirong = (TextView) view.findViewById(R.id.neirong);
			view.setTag(viewHolder);
		} else {
			view = convertView;
			viewHolder = (ViewHolder) view.getTag();
		}
		viewHolder.lianxiren.setText(sms.getphone());
		viewHolder.shijian.setText(sms.getdate());
		viewHolder.neirong.setText(sms.getcontent());
		return view;
	}
	
	class ViewHolder {
		TextView lianxiren;
		TextView shijian;
		TextView neirong;
		
	}
}