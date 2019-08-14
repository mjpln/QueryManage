package com.knowology.km.dto;

public class Response {
	private Object body;
	private boolean success;
	private String message;
	
	public Response() {
	}
	
	public Response(Object body, boolean success, String message) {
		this.body = body;
		this.success = success;
		this.message = message;
	}
	
	public Response(Object body, boolean success) {
		this.body = body;
		this.success = success;
	}
	
	
	public Object getBody() {
		return body;
	}
	public void setBody(Object body) {
		this.body = body;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
