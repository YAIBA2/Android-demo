package com.example.firsttest;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CalllogAdapter extends ArrayAdapter<Calllog> {
	private int resourceId;

	public CalllogAdapter(Context context, int textViewResourceId, List<Calllog> objects) {
		super(context, textViewResourceId, objects);
		resourceId = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Calllog calllog = getItem(position);
		View view;
		ViewHolder viewHolder;
		if (convertView == null) {
			view = LayoutInflater.from(getContext()).inflate(resourceId, null);
			viewHolder = new ViewHolder();
			viewHolder.phone = (TextView) view.findViewById(R.id.calllogphone);
			viewHolder.type = (TextView) view.findViewById(R.id.calllogtype);
			viewHolder.time = (TextView) view.findViewById(R.id.calllogtime);
			viewHolder.date = (TextView) view
					.findViewById(R.id.calllogcalltime);
			viewHolder.name = (TextView) view.findViewById(R.id.calllogname);
			view.setTag(viewHolder);
		} else {
			view = convertView;
			viewHolder = (ViewHolder) view.getTag();
		}
		viewHolder.phone.setText(calllog.getphone());
		viewHolder.type.setText(calllog.gettype());
		viewHolder.time.setText(calllog.gettime());
		viewHolder.date.setText(calllog.getdate());
		viewHolder.name.setText(calllog.getname());
		return view;
	}

	class ViewHolder {
		TextView phone;
		TextView type;
		TextView time;
		TextView date;
		TextView name;

	}
}
