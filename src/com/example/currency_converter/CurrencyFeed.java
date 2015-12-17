package com.example.currency_converter;

import java.util.HashMap;

public class CurrencyFeed {
	private HashMap<String, Double> rates;
	private static String date;
	
	public CurrencyFeed() {
		rates = new HashMap<String, Double>();
	}
	
	public double getRate(String currency) {
		return rates.get(currency);
	}
	
	public void addRate(String currency, double rate) {
		rates.put(currency, rate);
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		CurrencyFeed.date = date;
	}
}
