/*************************************************************
 * ThankUser Class
 * The back-end implementation of the activity represented by the ThankUser.xml file.
 * Thanks the voter after the successful cast of the vote, before moving back to the introduction
 * screen to enable the next voter to log in to the system.
 * A part of the SmartVote application, developed by ZedApps.
 * 
 * @author Shamah M Zoha
 * @email shamah1992@gmail.com
 * @github bitbucket.org/smzoha/
 ************************************************************/

package com.zedapps.smartvote;

import com.zedapps.smartvote.modules.VoteManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class ThankUser extends Activity {
	
	VoteManager vManager; // instance of the VoteManager class
	SVMain svmainObj;     // instance of SVMain class, which houses all the modules

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_thank_user);
		
		// receiving parcel transfered by the previous activity
		// and store them in the designated variable
		
		vManager = (VoteManager) getIntent().getParcelableExtra("vManager");
			
		// instantiate object of the SVMain class and bind the vManager object to it.
		
		svmainObj = new SVMain(getApplicationContext());
		svmainObj.setVManager(vManager);
		
		// start the wait method where the background thread is run to carry out the process
		
		waitForIntent();
	}
	
	// the method that helps in keeping the activity alive for 10 seconds, before moving back to the introduction screen
	
	public void waitForIntent() {
		Thread waitThread = new Thread() {
			
			// halts the main thread for 10 seconds, before the user is redirected to the Intro activity,
			// which essentially displays the introduction screen, with a few instructions. the VoteManager
			// object is passed through to ensure concurrency throughout the system. a message is displayed
			// if the process runs into an error.
			
			public void run() {
				try {
					Thread.sleep(10000);
					Intent toIntro = new Intent(ThankUser.this, Intro.class);
					toIntro.putExtra("vManager", vManager);
					startActivity(toIntro);
					finish();
				} catch (InterruptedException e) {
					Toast.makeText(ThankUser.this, e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		};
		
		// starts the thread implemented and instantiated above
		
		waitThread.start();
	}
}
