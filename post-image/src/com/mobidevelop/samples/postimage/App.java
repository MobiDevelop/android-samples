package com.mobidevelop.samples.postimage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class App extends Activity {
	public static final int REQUEST_GET = 1337;
	
	PostResponseReceiver receiver;
	ProgressDialog dialog;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.app);
        
        // Set up a broadcast receiver to receive results        
        IntentFilter filter = new IntentFilter();
        filter.addAction(PostResponseReceiver.ACTION_PROGRESS);
        filter.addAction(PostResponseReceiver.ACTION_RESULT);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver = new PostResponseReceiver();
		registerReceiver(receiver, filter);
		
        Intent intent = getIntent();
        String action = intent.getAction();
        
        // If we are handling a share intent, pass along the intent
		if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
			doPost(intent);
		}
    }
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		if (dialog != null) {
			// Kill our dialog if it exists
			dialog.dismiss();
			dialog = null;
		}
		if (receiver != null) {
			// Unregister our receiver
			unregisterReceiver(receiver);	
		}		
	}
    
    // Handle button click.
    public void getContent(View v) {
    	startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), REQUEST_GET);
    }
    
    // Copy the intent and direct to our service
    public void doPost(Intent intent) {
		TextView text = (TextView) findViewById(R.id.text);
		text.setText(null);
		dialog = new ProgressDialog(this);
		dialog.setMessage(getString(R.string.label_dialog_uploading));
		dialog.show();
    	startService(new Intent(intent).setClass(this, PostIntentService.class));
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_GET) {
				// We got an image from... somewhere, we don't care where, pass it along.
				doPost(new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, data.getData()));
			}
		}
	}
	
	/**
	 * A simple broadcast receiver for receiving progress and result broadcasts
	 * from {@link PostIntentService}.
	 * @author jshapcot
	 */
	public class PostResponseReceiver extends BroadcastReceiver {		
		public static final String ACTION_PROGRESS = "com.mobidevelop.samples.postimage.intent.action.PROGRESS";
		public static final String ACTION_RESULT = "com.mobidevelop.samples.postimage.intent.action.RESULT";
		
		public static final String EXTRA_RESULT = "com.mobidevelop.samples.postimage.intent.extra.RESULT";
		public static final String EXTRA_TEXT = "com.mobidevelop.samples.postimage.intent.extra.TEXT";
		
    	@Override
		public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		if (ACTION_PROGRESS.equals(action)) {
    			
    		}
    		else
    		if (ACTION_RESULT.equals(action)) {
    			if (dialog != null) {
    				dialog.dismiss();
    				dialog = null;
    			}
    			TextView text = (TextView) findViewById(R.id.text);
    			text.setText(intent.getBooleanExtra(EXTRA_RESULT, false) + ": " + intent.getStringExtra(EXTRA_TEXT));
    		}
		}
    }
}
