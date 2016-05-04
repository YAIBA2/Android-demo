package com.example.firsttest;

import java.text.DecimalFormat;
import java.util.List;


import android.content.Context;
import android.net.TrafficStats;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LiuLiangListAdapter  extends ArrayAdapter<Liuliang> {
	private int resourceId;
	public LiuLiangListAdapter(Context context, int textViewResourceId,
			List<Liuliang> objects) {
		super(context, textViewResourceId, objects);
		resourceId = textViewResourceId;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Liuliang sms = getItem(position);
		View view;
		ViewHolder viewHolder;
		if (convertView == null) {
			view = LayoutInflater.from(getContext()).inflate(resourceId, null);
			viewHolder = new ViewHolder();
			viewHolder.appname = (TextView) view.findViewById(R.id.appname);
			viewHolder.image = (ImageView) view.findViewById(R.id.appimage);
			viewHolder.upload = (TextView) view.findViewById(R.id.update);
			viewHolder.download = (TextView) view.findViewById(R.id.download);
			viewHolder.total = (TextView) view.findViewById(R.id.totalliuliang);
			view.setTag(viewHolder);
		} else {
			view = convertView;
			viewHolder = (ViewHolder) view.getTag();
		}
		viewHolder.image.setImageDrawable(sms.getIcon());  
        String name = sms.getAppname();  
        /*if(name.length() > 8){  
            name = name.substring(0, 7)+"...";  
        }*/  
        viewHolder.appname.setText(name);  
        int uid = sms.getUid();  
        long tx = TrafficStats.getUidTxBytes(uid);  
        if(tx < 0){  
            tx = 0;  
        }  
        long rx = TrafficStats.getUidRxBytes(uid);  
        if(rx < 0){  
            rx = 0;  
        }  
        long total = tx + rx;  
        viewHolder.total.setText("总计:"+new TextFormat().formatByte(total));  
        viewHolder.upload.setText("上传:"+new TextFormat().formatByte(tx));  
        viewHolder.download.setText("下载:"+new TextFormat().formatByte(rx)); 
		return view;
	}
	
	class ViewHolder {
		TextView appname;
		TextView upload;
		TextView download;
		TextView total;
		ImageView image;
		
	}
	class TextFormat {
		
		/**
		 * 格式化数据
		 * @param data
		 * @return
		 */
		String formatByte(long data){
			DecimalFormat format = new DecimalFormat("##.##");
			if(data < 1024){
				return data+"bytes";
			}else if(data < 1024 * 1024){
				return format.format(data/1024f) +"KB";
			}else if(data < 1024 * 1024 * 1024){
				return format.format(data/1024f/1024f) +"MB";
			}else if(data < 1024 * 1024 * 1024 * 1024){
				return format.format(data/1024f/1024f/1024f) +"GB";
			}else{
				return "超出统计范围";
			}
		}
	}
}
