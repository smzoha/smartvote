/*************************************************************
 * FPVerify Class
 * The back-end implementation of the activity represented by the FPVerify.xml file.
 * Takes in the live fingerprint image of the user and refers them to appropriate panel.
 * A part of the SmartVote application, developed by ZedApps.
 * 
 * @author Shamah M Zoha
 * @email shamah1992@gmail.com
 * @github bitbucket.org/smzoha/
 ************************************************************/

package com.zedapps.smartvote;

import com.zedapps.smartvote.modules.User;
import com.zedapps.smartvote.modules.VoteManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class FPVerify extends Activity {

	VoteManager vManager; // instance of the VoteManager class
	SVMain svmainObj;     // instance of SVMain class, which houses all the modules
	
	Button btnFPVerifyStart; // button to start the verification process
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fpverify);
		
		// receiving parcel transfered by the previous activity
		// and store them in the designated variable
						
		vManager = (VoteManager) getIntent().getParcelableExtra("vManager");
			
		// instantiate object of the SVMain class and bind the vManager object to it.
		// next, open serial connection with the Arduino.
		
		svmainObj = new SVMain(getApplicationContext());
		svmainObj.setVManager(vManager);
		svmainObj.fpOpen();
		
		// instantiate the UI components
		
		btnFPVerifyStart = (Button) findViewById(R.id.btnFPVerifyStart);
		
		// if the user clicks on the "Start" button, the identify() method is run
		
		btnFPVerifyStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				identify();
			}
		});
	}
	
	// method to identify user using the live fingerprint image and the data stored in the database
	
	public void identify() {
		
		// fpIdentify method from the SVMain object is called to trigger the fingerprint scanning,
		// set template from the database and verification processes.
		
		svmainObj.fpIdentify();
		
		// the NID value attached to the fingerprint data is obtained.
		
		String fpNID = svmainObj.getNID();
		
		// if the NID value is not empty, i.e. the fingerprint impression provided is a match, a connection
		// with the database is established to obtain the user information. the connection is closed shortly
		// after to avoid memory leakage. if no match is found, a message is displayed.
		
		if(fpNID != "") {
			svmainObj.dbOpen();
			User currentUser = svmainObj.getSpecificUser(fpNID);
			svmainObj.dbClose();
			
			// if the user exists in the database, the classification between them is done. otherwise, a message is shown
			// which states that the user is not found. if the user does not belong to any class, i.e. voter, admin or super
			// admin, an invalid user status message is displayed.
			
			if(currentUser != null) {
				
				// if the user is a super admin, the user is referred directly to the administrative panel.
				// the VoteManager object is passed for concurrency and the current User object is also passed
				// to start an active session. the serial connection is closed to ensure smooth transition.
				
				if(currentUser.isSAdmin()) {
					Intent toAdminPanel = new Intent(FPVerify.this, AdminPanel.class);
					toAdminPanel.putExtra("vManager", vManager);
					toAdminPanel.putExtra("usr", currentUser);
					startActivity(toAdminPanel);
					finish();
					svmainObj.fpClose();
					
				// if the user is an admin, it is checked if the user is also a voter or not. if the user is a
			    // voter as well, they are referred to the AdminVoterPanel, where they have the provision to select
				// panels. if not, the user is referred to the Administrative panel.
				// the VoteManager object is passed for concurrency and the current User object is also passed
				// to start an active session. the serial connection is closed to ensure smooth transition.
					
				} else if(currentUser.isAdmin()) {
					if(currentUser.isVoter()) {
						Intent toAVSplit = new Intent(FPVerify.this, AdminVoterPanel.class);
						toAVSplit.putExtra("vManager", vManager);
						toAVSplit.putExtra("usr", currentUser);
						startActivity(toAVSplit);
						finish();
						svmainObj.fpClose();
						
					} else {
						Intent toAdminPanel = new Intent(FPVerify.this, AdminPanel.class);
						toAdminPanel.putExtra("vManager", vManager);
						toAdminPanel.putExtra("usr", currentUser);
						startActivity(toAdminPanel);
						finish();
						svmainObj.fpClose();
					}
				
				// if the user is found to be a voter, it is checked if they have already casted their vote.
				// if the condition is true, a message is displayed. else, they are redirected to the voting panel.
				// the VoteManager object is passed for concurrency and the current User object is also passed
				// to start an active session. the serial connection is closed to ensure smooth transition.
					
				} else if(currentUser.isVoter()) {
					if(currentUser.isHasVoted()) {
						Toast.makeText(FPVerify.this, "You have already casted your vote!", Toast.LENGTH_SHORT).show();
					} else {
						if(vManager.isVotingStatus()) {
							Intent toVInfoPanel = new Intent(FPVerify.this, UserInfo.class);
							toVInfoPanel.putExtra("vManager", vManager);
							toVInfoPanel.putExtra("usr", currentUser);
							startActivity(toVInfoPanel);
							finish();
							svmainObj.fpClose();
							
						} else {
							Toast.makeText(FPVerify.this, "Voting process has not started yet. Try again later.", Toast.LENGTH_SHORT).show();
						}
					}
				} else {
					Toast.makeText(FPVerify.this, "Invalid user status! Try again.", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(FPVerify.this, "User not found! Try again.", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(FPVerify.this, "Match not found! Try again.", Toast.LENGTH_SHORT).show();			
		}
	}
}
