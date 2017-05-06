/*************************************************************
 * AdminPanel Class
 * The back-end implementation of the activity represented by the AdminPanel.xml file.
 * Provides administrative panel to both Administrators and the Super Administrators.
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
import android.widget.TextView;
import android.widget.Toast;

public class AdminPanel extends Activity {
	
	VoteManager vManager; // instance of the VoteManager class
	User currentUser;     // User object representing the current user (administrator)
	SVMain svmainObj;     // instance of SVMain class, which houses all the modules
	
	TextView txtWelcome;  // object representing the welcome message
	
	Button btnEnableVote;	// button that enables the voting process
	Button btnDisableVote;  // button that disables the voting process
	Button btnLoadDB;		// button that redirects to load database panel
	Button btnReport;		// button that redirects to the report panel
	Button btnAddVoter;		// button that enables addition of new voter
	Button btnRemoveVoter;  // button that redirects to the remove user panel
	Button btnExitPanel;    // button that redirects back to the introduction screen

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin_panel);
		
		// receiving parcel transfered by the previous activity
		// and store them in the designated variable
		
		vManager = (VoteManager) getIntent().getParcelableExtra("vManager");
		currentUser = (User) getIntent().getParcelableExtra("usr");
		
		// instantiate object of the SVMain class and bind the vManager object to it.
		// next, initiate the serial connection with the Arduino
		
		svmainObj = new SVMain(getApplicationContext());
		svmainObj.setVManager(vManager);
		svmainObj.fpOpen();
		
		// instantiate the UI components
		
		txtWelcome = (TextView) findViewById(R.id.txtWelcomeAdmin);
		btnAddVoter = (Button) findViewById(R.id.btnAddVoter);
		btnRemoveVoter = (Button) findViewById(R.id.btnRemoveVoter);
		btnEnableVote = (Button) findViewById(R.id.btnEnableVoting);
		btnLoadDB = (Button) findViewById(R.id.btnLoadDBAPanel);
		btnReport = (Button) findViewById(R.id.btnReport);
		btnDisableVote = (Button) findViewById(R.id.btnDisableVoting);
		btnExitPanel = (Button) findViewById(R.id.btnExitAdminPanel);
		
		// dynamically display the current administrator's name on the welcome message
		
		txtWelcome.append(" " + currentUser.getName() + "!");
		
		// if the current user is not a super administrator, and is a regular administrator,
		// strip certain functionality (such as add voter, remove user and load database, by
		// hiding the buttons
		
		if(!currentUser.isSAdmin()) {
			btnLoadDB.setVisibility(View.INVISIBLE);
			btnAddVoter.setVisibility(View.INVISIBLE);
			btnRemoveVoter.setVisibility(View.INVISIBLE);
		}
		
		// had the voting process been already started, disable the "Enable Vote" button
		// and show the "Disable Vote" button to allow for stopping the voting process.
		// the converse is allowed as well
		
		if(vManager.isVotingStatus()) {
			btnEnableVote.setEnabled(false);
		} else {
			btnDisableVote.setEnabled(false);
		}
		
		// if the user clicks on the "Enable Vote" button, the enableVote() method is run
		
		btnEnableVote.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				enableVote();
			}
		});

		// if the user clicks on the "Disable Vote" button, the disableVote() method is run
		
		btnDisableVote.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				disableVote();
			}
		});
		
		// if the user clicks on the "Load Database" button, the application is referred to
		// the LoadData activity. the VoteManager object is passed to the next activity
		// to maintain concurrency, and the serial connection is closed to allow for smooth
		// transition.
		
		btnLoadDB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent toLoadDB = new Intent(AdminPanel.this, LoadData.class);
				toLoadDB.putExtra("vManager", vManager);
				startActivity(toLoadDB);
				finish();
				svmainObj.fpClose();
			}
		});
		
		
		// if the user clicks on the Report button, the application is referred to
		// the Report activity. the VoteManager object is passed to the next activity
		// to maintain concurrency, and the serial connection is closed to allow for smooth
		// transition. the object bearing the information of the current user 
		// running the session is also passed.
		
		btnReport.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent toReport = new Intent(AdminPanel.this, Report.class);
				toReport.putExtra("vManager", vManager);
				toReport.putExtra("usr", currentUser);
				startActivity(toReport);
				finish();
				svmainObj.fpClose();
			}
		});
		
		// if the user clicks on the "Add New Voter" button, the application is referred to
		// the NewVoter activity. the VoteManager object is passed to the next activity
		// to maintain concurrency, and the serial connection is closed to allow for smooth
		// transition. the object bearing the information of the current user 
		// running the session is also passed.
		
		btnAddVoter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent toAddVoter= new Intent(AdminPanel.this, NewVoter.class);
				toAddVoter.putExtra("vManager", vManager);
				toAddVoter.putExtra("usr", currentUser);
				startActivity(toAddVoter);
				finish();
				svmainObj.fpClose();
			}
		});

		// if the user clicks on the "Remove Existing User" button, the application is 
		// referred to the RemoveUser activity. the VoteManager object is passed to the 
		// next activity to maintain concurrency, and the serial connection is closed to allow 
		// for smooth transition. the object bearing the information of the current user 
		// running the session is also passed.
		
		btnRemoveVoter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent toRemoveUser= new Intent(AdminPanel.this, RemoveUser.class);
				toRemoveUser.putExtra("vManager", vManager);
				toRemoveUser.putExtra("usr", currentUser);
				startActivity(toRemoveUser);
				finish();
				svmainObj.fpClose();
			}
		});
		
		
		// if the user clicks on the "Exit" button, the application is referred to
		// the Intro activity, rolling it back to the beginning.
		// the VoteManager object is passed to the next activity to maintain concurrency, 
		// and the serial connection is closed to allow for smooth transition.
		
		btnExitPanel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent toIntro= new Intent(AdminPanel.this, Intro.class);
				toIntro.putExtra("vManager", vManager);
				startActivity(toIntro);
				finish();
				svmainObj.fpClose();
			}
		});
	}
	
	// the enableVote method is invoked when the "Enable Vote" button is clicked in the
	// panel. the votingStatus field in the VoteManager object is set to true, while
	// the "Enable Vote" button is disabled and the "Disable Vote" button is activated
	// to allow for the process to be reversed.
	
	public void enableVote() {
		vManager.setVotingStatus(true);
		btnEnableVote.setEnabled(false);
		btnDisableVote.setEnabled(true);
		Toast.makeText(AdminPanel.this, "Voting process enabled."
				+ "Users may cast their votes from now.", Toast.LENGTH_SHORT).show();
	}
	
	// the disableVote method is invoked when the "Disable Vote" button is clicked in the
	// panel. the votingStatus field in the VoteManager object is set to false, while
	// the "Disable Vote" button is disabled and the "Enable Vote" button is activated
	// to allow for the process to be reversed.
	
	public void disableVote() {
		vManager.setVotingStatus(false);
		btnEnableVote.setEnabled(true);
		btnDisableVote.setEnabled(false);
		Toast.makeText(AdminPanel.this, "Voting process disabled.", Toast.LENGTH_SHORT).show();
	}
	
	
}
