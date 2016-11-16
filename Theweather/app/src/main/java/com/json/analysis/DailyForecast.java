package com.json.analysis;

public class DailyForecast {
	private Cond cond;
	private String date;
	private Tmp tmp;
	private Wind wind;
	public Cond getCond(){
		return cond;
	}
	public void setConde(Cond cond){
		this.cond=cond;
	}
	public String getDate(){
		return date;
	}
	public void setDate(String date){
		this.date=date;
	}
	public Tmp getTmp(){
		return tmp;
	}
	public void setTmp(Tmp tmp){
		this.tmp=tmp;
	}
	public Wind getWind(){return wind;}
	public void setWind(Wind wind){this.wind=wind;}
}
