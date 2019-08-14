package com.knowology.km.dto;

public class CombData {
	private String text;
	private Object value;
	
	
	public CombData() {
	}
	public CombData(String text, Object value) {
		super();
		this.text = text;
		this.value = value;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
}
