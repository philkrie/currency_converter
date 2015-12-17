package com.example.currency_converter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener, OnItemSelectedListener, TextWatcher {

	private Spinner currencySpinner1;
	private Spinner currencySpinner2;
	private EditText currencyEditText1;
	private EditText currencyEditText2;
	private TextView internetNotificationTextView;
	private ImageButton swapImageButton;
	private ImageView currencyImageView1;
	private ImageView currencyImageView2;
	private String currency1;
	private String currency2;
	private int pos1;
	private int pos2;
	private String imgName;
	private CurrencyFeed feed;
    private final String URL_STRING = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
    private final String FILENAME = "rate_feed.xml";
    private Double output;
    private SharedPreferences savedValues;
    private boolean rounding;
    private boolean nativelang;
    private boolean resuming;
    private boolean fileexists;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		currencySpinner1 = (Spinner) findViewById(R.id.currencySpinner1);
		currencySpinner2 = (Spinner) findViewById(R.id.currencySpinner2);
		currencyEditText1 = (EditText) findViewById(R.id.currencyEditText1);
		currencyEditText2 = (EditText) findViewById(R.id.currencyEditText2);
		swapImageButton = (ImageButton) findViewById(R.id.swapImageButton);
		currencyImageView1 = (ImageView) findViewById(R.id.currencyImageView1);
		currencyImageView2 = (ImageView) findViewById(R.id.currencyImageView2);
		internetNotificationTextView = (TextView) findViewById(R.id.internetNotificationTextView);
		
		currencySpinner1.setOnItemSelectedListener(this);
		currencySpinner2.setOnItemSelectedListener(this);
		swapImageButton.setOnClickListener(this);
		currencyEditText1.addTextChangedListener(this);
		
		currencyEditText2.setEnabled(false);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		savedValues = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (isOnline()) {
			new DownloadFeed().execute();
	    	Log.v("MainActivity", "Starting download");
		} else {
			new ReadFeed().execute();
		}
        
 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onPause() {
		
		//Saves values into editor when paused
		
		Editor editor = savedValues.edit();
		editor.putInt("flag1", pos1);
		editor.putInt("flag2", pos2);
		editor.putString("currency1", currency1);
		editor.putString("currency2", currency2);
		editor.putString("userinput", currencyEditText1.getText().toString());

		editor.commit();
		super.onPause();
	}
	
	@Override
	public void onResume() {
		
		super.onResume();
		
		// get preferences
		rounding = savedValues.getBoolean("pref_rounding", true);
		nativelang = savedValues.getBoolean("pref_native", false);
		
		int spinnerarray;
		if (nativelang) {
			spinnerarray = R.array.currency_array_native;
		} else {
			spinnerarray = R.array.currency_array;
		}
		
		resuming = true;
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        spinnerarray, android.R.layout.simple_spinner_item);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		currencySpinner1.setAdapter(adapter);
		currencySpinner2.setAdapter(adapter);
		
		//Reset data
		new DownloadFeed().execute();
	
        pos1 = savedValues.getInt("flag1", 0);
        currencySpinner1.setSelection(pos1);
		pos2 = savedValues.getInt("flag2", 0);
        currencySpinner2.setSelection(pos2);
		currency1 = savedValues.getString("currency1", "USD"); 
		currency2 = savedValues.getString("currency2", "USD"); 
		currencyEditText1.setText(savedValues.getString("userinput", "0"));
		
		if (isOnline()) {
    		internetNotificationTextView.setText("");
    	}
		
	}
		
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
			return true;
		}
		if (id == R.id.action_about){
			startActivity(new Intent(getApplicationContext(), AboutActivity.class));
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

	    return cm.getActiveNetworkInfo() != null && 
	       cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}
	
	class DownloadFeed extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
        
            try{
                // get the URL
                URL url = new URL(URL_STRING);

                // get the input stream
                InputStream in = url.openStream();

                // get the output stream
                Context context = MainActivity.this;
                FileOutputStream out = 
                    context.openFileOutput(FILENAME, Context.MODE_PRIVATE);

                // read input and write output
                byte[] buffer = new byte[1024];
                int bytesRead = in.read(buffer);
                while (bytesRead != -1)
                {
                    out.write(buffer, 0, bytesRead);
                    bytesRead = in.read(buffer);
                }
                out.close();
                in.close();
            } 
            catch (IOException e) {
                Log.e("News reader", e.toString());
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
            Log.d("Currency converter", "Feed downloaded: " + new Date());
            new ReadFeed().execute();
        }
    }
	
	class ReadFeed extends AsyncTask<Void, Void, Void> {
        
        @Override
        protected Void doInBackground(Void... params) {
            try {
                // get the XML reader
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                XMLReader xmlreader = parser.getXMLReader();
    
                // set content handler
                CurrencyFeedHandler theCurrencyFeedHandler = new CurrencyFeedHandler();
                xmlreader.setContentHandler(theCurrencyFeedHandler);
    
                // read the file from internal storage
                try {
                	FileInputStream in = openFileInput(FILENAME);
                    InputSource is = new InputSource(in);
                    xmlreader.parse(is);
                } catch (FileNotFoundException e) {
                	e.printStackTrace();
                	fileexists = false;
                	return null;
                }
                
                fileexists = true;
    
                // set the feed in the activity
                MainActivity.this.feed = theCurrencyFeedHandler.getFeed();
                resuming = false;
            } 
            catch (Exception e) {
                Log.e("Rates feed", e.toString());
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
            if (fileexists) {
        	Log.d("Currency converter", "Feed read: " + new Date());
            Log.d("Currency converter", feed.getDate());  
            	if (!isOnline()){
            		internetNotificationTextView.setText("No internet connection. Loading currency rates from " 
            				+ feed.getDate());
            	}
            calculate();
            } else {
            	internetNotificationTextView.setText("No internet connection. There are no previously "
            			+ "downloaded conversion rates. Please check internet connection");
            	currencySpinner1.setEnabled(false);
            	currencySpinner2.setEnabled(false);
            	currencyEditText1.setEnabled(false);

            }
        }
    }
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.swapImageButton:
			String text1 = currencyEditText1.getText().toString();
			String text2 = currencyEditText2.getText().toString();
			int position1 = currencySpinner1.getSelectedItemPosition();
			int position2 = currencySpinner2.getSelectedItemPosition();
			currencySpinner2.setSelection(position1);
			currencySpinner1.setSelection(position2);
			currencyEditText1.setText(text2);
			currencyEditText2.setText(text1);
		}
		
	}
	
	private void displayFlags(int pos, ImageView currencyImageView, int num) {
		String currency = null;
		switch (pos) {
		case 0:
			imgName = "usa";
			currency = "USD";
			break;
		case 1:
			imgName = "europeanunion";
			currency = "EUR";
			break;
		case 2:
			imgName = "canada";
			currency = "CAD";
			break;
		case 3:
			imgName = "unitedkingdom";
			currency = "GBP";
			break;
		case 4:
			imgName = "australia";
			currency = "AUD";
			break;
		case 5:
			imgName = "brazil";
			currency = "BRL";
			break;
		case 6:
			imgName = "bulgaria";
			currency = "BGN";
			break;
		case 7:
			imgName = "china";
			currency = "CNY";
			break;
		case 8:
			imgName = "croatia";
			currency = "HRK";
			break;
		case 9:
			imgName = "czechrepublic";
			currency = "CZK";
			break;
		case 10:
			imgName = "denmark";
			currency = "DKK";
			break;
		case 11:
			imgName = "hongkong";
			currency = "HKD";
			break;
		case 12:
			imgName = "hungary";
			currency = "HUF";
			break;
		case 13:
			imgName = "india";
			currency = "INR";
			break;
		case 14:
			imgName = "indonesia";
			currency = "IDR";
			break;
		case 15:
			imgName = "israel";
			currency = "ILS";
			break;
		case 16:
			imgName = "japan";
			currency = "JPY";
			break;
		case 17:
			imgName = "lithuania";
			currency = "LTL";
			break;
		case 18:
			imgName = "malaysia";
			currency = "MYR";
			break;
		case 19:
			imgName = "mexico";
			currency = "MXN";
			break;
		case 20:
			imgName = "newzealand";
			currency = "NZD";
			break;
		case 21:
			imgName = "norway";
			currency = "NOK";
			break;
		case 22:
			imgName = "philippines";
			currency = "PHP";
			break;
		case 23:
			imgName = "poland";
			currency = "PLN";
			break;
		case 24:
			imgName = "romania";
			currency = "RON";
			break;
		case 25:
			imgName = "russianfederation";
			currency = "RUB";
			break;
		case 26:
			imgName = "singapore";
			currency = "SGD";
			break;
		case 27:
			imgName = "southafrica";
			currency = "ZAR";
			break;
		case 28:
			imgName = "southkorea";
			currency = "KRW";
			break;
		case 29:
			imgName = "sweden";
			currency = "SEK";
			break;
		case 30:
			imgName = "switzerland";
			currency = "CHF";
			break;
		case 31:
			imgName = "thailand";
			currency = "THB";
			break;
		case 32:
			imgName = "turkey";
			currency = "TRY";
			break;
		}
		
		if (num == 1){
			currency1 = currency;
		} else {
			currency2 = currency;
		}
		
		currencyImageView.setImageResource(getResources().getIdentifier(imgName, "drawable",getPackageName()));
}

	private void calculate() {
		if (resuming){
			return;
		}
		else if (!currencyEditText1.getText().toString().equals("")) {
			Double userinput = Double.parseDouble(currencyEditText1.getText().toString());
		
			if (pos1 == pos2) {
				if (rounding)
					currencyEditText2.setText(String.format("%.2f",userinput));
				else
					currencyEditText2.setText(userinput.toString());
			} else if (pos1 == 1) {
				output = userinput * feed.getRate(currency2);
				if (rounding)
					currencyEditText2.setText(String.format("%.2f", output));
				else
					currencyEditText2.setText(output.toString());
			} else if (pos2 == 1) {
				output = userinput/feed.getRate(currency1);
				if (rounding)
					currencyEditText2.setText(String.format("%.2f", output));
				else
					currencyEditText2.setText(output.toString());
			} else {
				output = userinput/feed.getRate(currency1)*feed.getRate(currency2);
				if (rounding)
					currencyEditText2.setText(String.format("%.2f", output));
				else
					currencyEditText2.setText(output.toString());
			}
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
		if (parent.getId() == R.id.currencySpinner1) {
			pos1 = position;
			displayFlags(pos1, currencyImageView1, 1);
			calculate();
		} else {
			pos2 = position;
			displayFlags(pos2, currencyImageView2, 2);
			calculate();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}

	@Override
	public void afterTextChanged(Editable s) {
		calculate();
	}

}

