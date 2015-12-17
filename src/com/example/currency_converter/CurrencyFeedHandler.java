package com.example.currency_converter;


import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.*;
import android.util.Log;




public class CurrencyFeedHandler extends DefaultHandler{
	 private CurrencyFeed feed;
	 
	 public CurrencyFeed getFeed() {
		 return feed;
	 }
	 
	 public void startDocument() throws SAXException {
	        feed = new CurrencyFeed();
	 }
	 
	 public void startElement(String namespaceURI, String localName, 
	            String qName, Attributes atts) throws SAXException {
	        
		 if (localName.equals("Cube")) {
             String date = atts.getValue("time");
             if (date != null) {
                 feed.setDate(date);
             }
             String currency = atts.getValue("currency");
             String rate = atts.getValue("rate");
             if (rate != null) {
                 try {
                	 double double_rate = Double.parseDouble(rate);
                     feed.addRate(currency, double_rate);
                 } catch (Exception e) {
                     Log.d("Parser","Cannot parse exchange rate:");
                 }
             }
         }
     }
 	        	
	          
	    
	     
	   
}
