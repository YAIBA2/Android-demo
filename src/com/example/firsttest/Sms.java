package com.example.firsttest;

public class Sms {

	private String phone;
	private String content;
	private String date;
	public Sms(String phone, String content,String date) {
		this.phone = phone;
		this.content = content;
		this.date = date;
	}

	public String getphone() {
		return phone;
	}

	public String getcontent() {
		return content;
	}
	
	public String getdate() {
		return date;
	}

}
