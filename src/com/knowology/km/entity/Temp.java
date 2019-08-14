/*
 * KBDataAction中AddChildren方法里需要的临时类，目的为实现多个返回值
 */
package com.knowology.km.entity;

import java.util.List;

public class Temp {
	private List<String> ser;
	private List<String> repeat;
	private List<String> abs;
	
	public List<String> getSer() {
		return ser;
	}
	public void setSer(List<String> ser) {
		this.ser = ser;
	}
	public List<String> getRepeat() {
		return repeat;
	}
	public void setRepeat(List<String> repeat) {
		this.repeat = repeat;
	}
	public List<String> getAbs() {
		return abs;
	}
	public void setAbs(List<String> abs) {
		this.abs = abs;
	}
}
