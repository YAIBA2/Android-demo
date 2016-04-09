package com.example.firsttest;

import java.util.List;

import com.example.firsttest.SmsAdapter.ViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AddressAdapter extends ArrayAdapter<Addresss> {

	private int resourceId;

	public AddressAdapter(Context context, int textViewResourceId,
			List<Addresss> objects) {
		super(context, textViewResourceId, objects);
		resourceId = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Addresss address = getItem(position);
		View view;
		ViewHolder viewHolder;
		if (convertView == null) {
			view = LayoutInflater.from(getContext()).inflate(resourceId, null);
			viewHolder = new ViewHolder();
			viewHolder.lianxiren = (TextView) view
					.findViewById(R.id.addressname);
			viewHolder.phone = (TextView) view.findViewById(R.id.addressphone);
			view.setTag(viewHolder);
		} else {
			view = convertView;
			viewHolder = (ViewHolder) view.getTag();
		}
		viewHolder.lianxiren.setText(address.getName());
		viewHolder.phone.setText(address.getphone());
		return view;
	}

	class ViewHolder {
		TextView lianxiren;
		TextView phone;

	}

}
