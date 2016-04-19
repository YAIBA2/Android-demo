package com.example.firsttest;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	//private Bitmap directory, file;
	// 存储文件名称
	private ArrayList<String> names = null;
	// 存储文件路径
	private ArrayList<String> paths = null;

	// 参数初始化
	public FileAdapter(Context context, ArrayList<String> na,
			ArrayList<String> pa) {
		names = na;
		paths = pa;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return names.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return names.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (null == convertView) {
			convertView = inflater.inflate(R.layout.filemanage_item, null);
			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.filename);
			holder.time = (TextView) convertView.findViewById(R.id.filedate);
			holder.image = (ImageView) convertView.findViewById(R.id.fileimage);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		File f = new File(paths.get(position).toString());
		if (names.get(position).equals("@1")) {
			holder.text.setText("/");
			holder.time.setText("顶层文件夹");
			holder.image.setImageResource(R.drawable.file1);
		} else if (names.get(position).equals("@2")) {
			holder.text.setText("..");
			holder.time.setText("上层文件夹");
			holder.image.setImageResource(R.drawable.file1);
		} else {
			holder.text.setText(f.getName());
			holder.time.setText(new Timestamp(f.lastModified()).toString());
			if (f.isDirectory()) {
				holder.image.setImageResource(R.drawable.file1);
			} else if (f.isFile()) {
				String _FileName = paths.get(position).toLowerCase();
				 if(_FileName.endsWith(".txt")){  //文本显示t
					  holder.image.setImageResource(R.drawable.file3) ;
					  //holder.text.setText(paths.get(position)) ; 
	                 // mViewHolder.mFileTime.setText(new Date(System.currentTimeMillis()).toLocaleString());
	                       
	             }else if(_FileName.endsWith(".png") || _FileName.endsWith(".jpg") ||_FileName.endsWith(".jpeg") || _FileName.endsWith(".gif")){
	            	 holder.image.setImageResource(R.drawable.music1);
	            	 //holder.text.setText(paths.get(position)) ;  
	                 //mViewHolder.mFileTime.setText(new Date(System.currentTimeMillis()).toLocaleString());
	                  
	             }else if(_FileName.endsWith(".mp4")|| _FileName.endsWith(".avi")|| _FileName.endsWith(".3gp") || _FileName.endsWith(".rmvb")){
	            	 holder.image.setImageResource(R.drawable.media1) ;
	            	 //holder.text.setText(paths.get(position)) ; 
	                 // mViewHolder.mFileTime.setText(new Date(System.currentTimeMillis()).toLocaleString());
	                   
	             }else if(_FileName.endsWith("mp3")){
	            	  holder.image.setImageResource(R.drawable.mp3) ;
	             }else if(_FileName.endsWith("doc")||_FileName.endsWith("docx")){
	            	 holder.image.setImageResource(R.drawable.doc) ;
	             }else if(_FileName.endsWith("zip")||_FileName.endsWith("rar")||_FileName.endsWith("gz")||_FileName.endsWith("jar")){
	            	 holder.image.setImageResource(R.drawable.yasuo) ;
	             }else if(_FileName.endsWith("pdf")){
	            	 holder.image.setImageResource(R.drawable.pdf) ;
	             }else if(_FileName.endsWith("apk")){
	            	 holder.image.setImageResource(R.drawable.apk) ;
	            	 // holder.text.setText(paths.get(position)) ; 
	                 // mViewHolder.mFileTime.setText(new Date(System.currentTimeMillis()).toLocaleString());
	             }else{
	            	 holder.image.setImageResource(R.drawable.text1) ;
	            	 //holder.text.setText(paths.get(position)) ; 
	                // mViewHolder.mFileTime.setText(new Date(System.currentTimeMillis()).toLocaleString());
	             }
			} else {
				System.out.println(f.getName());
			}
		}
		return convertView;
	}

	private class ViewHolder {
		private TextView text;
		private TextView time;
		private ImageView image;
	}

}
