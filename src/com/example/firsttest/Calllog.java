package com.example.firsttest;

public class Calllog {
	private String phone;
	private String type;
	private String time;
	private String date;
	private String name;
	public Calllog(String phone, String type,String time,String date,String name) {
		this.phone = phone;
		this.type = type;
		this.date = date;
		this.time = time;
		this.name = name;
	}

	public String getphone() {
		return phone;
	}

	public String getname() {
		return name;
	}
	
	public String getdate() {
		return date;
	}
	public String gettype() {
		return type;
	}
	public String gettime() {
		return time;
	}

}
