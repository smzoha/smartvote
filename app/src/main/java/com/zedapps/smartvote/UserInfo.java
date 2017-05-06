/*************************************************************
 * UserInfo Class
 * The back-end implementation of the activity represented by the UserInfo.xml file.
 * Displays the information of the voter on the screen for verification, for 5 seconds.
 * A part of the SmartVote application, developed by ZedApps.
 * 
 * @author Shamah M Zoha
 * @email shamah1992@gmail.com
 * @github bitbucket.org/smzoha/
 ************************************************************/

package com.zedapps.smartvote;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.zedapps.smartvote.modules.User;
import com.zedapps.smartvote.modules.VoteManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UserInfo extends Activity {

	VoteManager vManager; // instance of the VoteManager class
	User currentUser;     // User object representing the current user (voter)
	
	TextView txtVInfo;	 // the text view that displays the user's information
	ImageView imgVPhoto; // the image view that displays the user's image
	
	SVMain svmainObj;     // instance of SVMain class, which houses all the modules
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_info);
		
		// receiving parcel transfered by the previous activity
		// and store them in the designated variable
		
		vManager = (VoteManager) getIntent().getParcelableExtra("vManager");
		currentUser = (User) getIntent().getParcelableExtra("usr");
		
		// instantiate object of the SVMain class and bind the vManager object to it.
		
		svmainObj = new SVMain(getApplicationContext());
		svmainObj.setVManager(vManager);
		
		// instantiate the UI components
		
		txtVInfo = (TextView) findViewById(R.id.txtVinfo);
		imgVPhoto = (ImageView) findViewById(R.id.imgVPhoto);
		
		// obtain a bitmap object from the image path provided in the user object
		
		Bitmap voterImg = svmainObj.getImage(currentUser.getnID());
		
		// if the image file exists, set it as a parameter for the image view object to display it in the screen.
		// also, set the dimension of the image to 250 x 250 pixels.
		
		if(voterImg != null) {
			imgVPhoto.setImageBitmap(voterImg);
			imgVPhoto.getLayoutParams().height=250;
			imgVPhoto.getLayoutParams().width=250;
		}
		
		// a simple date format object is instantiated to convert the millisecond data of the DOB field
		// from the database to a legible format - dd-MMM-yyy
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
		
		// the information of the user is appended in the text view for viewing
		
		txtVInfo.append("NID: " + currentUser.getnID()+ "\n");
		txtVInfo.append("Name: " + currentUser.getName() + "\n");
		txtVInfo.append("Address: " + currentUser.getAddress() + "\n");
		txtVInfo.append("Date of Birth: " + sdf.format(new Date(currentUser.getDob())) + "\n");
		txtVInfo.append("Father's Name: " + currentUser.getFatherName() + "\n");
		txtVInfo.append("Mother's Name: " + currentUser.getMotherName() + "\n");
		
		// start the wait method where the background thread is run to carry out the process
		
		waitForIntent();
	}
	
	
	// the method that helps in keeping the activity alive for 5 seconds, before moving to the EBallot screen

	public void waitForIntent() {
		Thread waitThread = new Thread() {
			
			// halts the main thread for 5 seconds, before the user is redirected to the EBallot activity,
			// which enables the voter to cast their vote and obtain the physical ballot. the VoteManager
			// object is passed through to ensure concurrency throughout the system, while the User object
			// is passed to continue to the session in the next activity. a message is displayed
			// if the process runs into an error.
			
			public void run() {
				try {
					Thread.sleep(5000);
					Intent toEBallot = new Intent(UserInfo.this, EBallot.class);
					toEBallot.putExtra("vManager", vManager);
					toEBallot.putExtra("usr", currentUser);
					startActivity(toEBallot);
					finish();
				} catch (InterruptedException e) {
					Toast.makeText(UserInfo.this, e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		};
		
		// starts the thread implemented and instantiated above
		
		waitThread.start();
	}
}
