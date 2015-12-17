package com.example.currency_converter;


import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends Activity {

	TextView textView3;
	CurrencyFeed feed;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rule);
		
		textView3 = (TextView) findViewById(R.id.textView3);
		
		feed = new CurrencyFeed();
		
		textView3.setText("Last Updated: " + feed.getDate());
	}
	
}
