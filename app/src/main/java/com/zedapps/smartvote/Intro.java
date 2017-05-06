/*************************************************************
 * Intro Class
 * The back-end implementation of the activity represented by the Intro.xml file.
 * Provides the users with a welcome screen, along with certain instructions.
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
import android.view.View;
import android.widget.Button;

public class Intro extends Activity {
	Button btnNext; 		// button to proceed to the next step
	
	VoteManager vManager; // instance of the VoteManager class
	SVMain svmainObj;     // instance of SVMain class, which houses all the modules

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);
		
		// receiving parcel transferred by the previous activity
		// and store them in the designated variable
				
		vManager = (VoteManager) getIntent().getParcelableExtra("vManager");
	
		// instantiate object of the SVMain class and bind the vManager object to it.
			
		svmainObj = new SVMain(getApplicationContext());
		svmainObj.setVManager(vManager);
		
		// instantiate the UI components
		
		btnNext = (Button) findViewById(R.id.btnNext);
		
		// if the user clicks on the "Next" button, the toFPVerify() method is run
		
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toFPVerify();
			}
		});
	}
	
	// method executed upon click on the "Next" button.
	// redirects user to the FPVerify activity to start the voting process.
	// passes the VoteManager object as parcel for concurrency.
	
	public void toFPVerify() {
		Intent toFPIntent = new Intent(this, FPVerify.class);
		toFPIntent.putExtra("vManager", vManager);
		startActivity(toFPIntent);
		finish();
	}
}
