package com.example.firsttest;

import android.graphics.drawable.Drawable;

public class Liuliang {
	// Ӧ��ͼ��
	private Drawable icon;
	// app����
	private String appname;
	// ����
	private String packname;
	// uid
	private int uid;

	public Liuliang(Drawable icon, String appname, String packname, int uid) {
		super();
		this.icon = icon;
		this.appname = appname;
		this.packname = packname;
		this.uid = uid;
	}
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	public String getAppname() {
		return appname;
	}
	public void setAppname(String appname) {
		this.appname = appname;
	}
	public String getPackname() {
		return packname;
	}
	public void setPackname(String packname) {
		this.packname = packname;
	}
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
}
