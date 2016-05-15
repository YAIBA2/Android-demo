package com.example.firsttest;
import android.graphics.drawable.Drawable;

public class AppInfo {

	private Drawable icon;
	private String name;
	private String packname;
	private String version;
	private long time;
	public long cachesize;          // �����С
    public long codesize;           //Ӧ�ó����С
    public long datasize;    		//Ӧ�ô�С
    public long appsize;    		//Ӧ���ܴ�С
	private boolean inrom;//��װλ�� �ֻ� ���ڻ��� sd��
	private boolean userapp;//�Ƿ�ΪϵͳӦ�� 
	
	private String receivername;
	public String getReceivername() {
		return receivername;
	}
	public void setReceivername(String receivername) {
		this.receivername = receivername;
	}
	@Override
	public String toString() {
		return "AppInfo [name=" + name + ", packname=" + packname
				+ ", version=" + version + ", inrom=" + inrom + ", userapp="
				+ userapp + "]";
	}
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	public long getcachesize() {
		return cachesize;
	}
	public void setCachesize(long cachesize) {
		this.cachesize=cachesize;
	}
	public long getcodesize() {
		return codesize;
	}
	public void setCodesize(long codesize) {
		this.codesize=codesize;
	}
	public long getdatasize() {
		return datasize;
	}
	public void setDatasize(long datasize) {
		this.datasize=datasize;
	}
	public long getappsize() {
		return appsize;
	}
	public void setAppsize(long appsize) {
		this.appsize=appsize;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time=time;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPackname() {
		return packname;
	}
	public void setPackname(String packname) {
		this.packname = packname;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public boolean isInrom() {
		return inrom;
	}
	public void setInrom(boolean inrom) {
		this.inrom = inrom;
	}
	public boolean isUserapp() {
		return userapp;
	}
	public void setUserapp(boolean userapp) {
		this.userapp = userapp;
	}
	
	
	
}
