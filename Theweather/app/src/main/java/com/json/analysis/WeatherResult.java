package com.json.analysis;

import java.util.List;

public class WeatherResult {
	private Basic basic;
	private List<DailyForecast> daily_forecast;
	private Now now;
	
	public Basic getBasic(){
		return basic;
	}
	public void setBasic(Basic basic){
		this.basic=basic;
	}
	public List<DailyForecast> getForecast(){
		return daily_forecast;
	}
	public void setForecast(List<DailyForecast> daily_forecast){
		this.daily_forecast=daily_forecast;
	}
	public Now getNow(){return now;}
	public void setNow(Now now){this.now=now;}
}
