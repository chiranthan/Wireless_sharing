package com.example.blucon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class Help extends Activity {

	WebView helpWebView;
	String helpHtml = "";
	BufferedReader br;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);

		helpWebView = (WebView) findViewById(R.id.webViewHelp);
				
		try {
			
			// Fetch the html file from assets folder
	        br = new BufferedReader(new InputStreamReader(getAssets().open("help.html")));
	        String word;
	        while((word=br.readLine()) != null)
	        helpHtml += word; //break txt file into different words, add to wordList
	    	}
	        catch(IOException e) {
	            e.printStackTrace();
	        }
		helpWebView.loadData(helpHtml, "text/html", "UTF-8");
	}
}