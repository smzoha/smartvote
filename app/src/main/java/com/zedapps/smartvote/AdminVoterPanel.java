/*************************************************************
 * AdminVoterPanel Class
 * The back-end implementation of the activity represented by the AdminVoterPanel.xml file.
 * In a scenario where the user is both a voter and an administrator, he/she can choose
 * which panel to access. This class serves to enable such provision.
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

public class AdminVoterPanel extends Activity {

	VoteManager vManager; // instance of the VoteManager class
	User currentUser;     // User object representing the current user (administrator/voter)
	SVMain svmainObj;     // instance of SVMain class, which houses all the modules
	
	Button btnToAdminPanel; // button that allows the user to access the admin panel
	Button btnToVotingPanel; // button that allows the user to access the voting panel
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin_voter_panel);
		
		// receiving parcel transfered by the previous activity
		// and store them in the designated variable
		
		vManager = (VoteManager) getIntent().getParcelableExtra("vManager");
		currentUser = (User) getIntent().getParcelableExtra("usr");
		
		// instantiate object of the SVMain class and bind the vManager object to it.
		
		svmainObj = new SVMain(getApplicationContext());
		svmainObj.setVManager(vManager);
		
		// instantiate the UI components
		
		btnToAdminPanel = (Button) findViewById(R.id.btnToAdminPanel);
		btnToVotingPanel = (Button) findViewById(R.id.btnToVotingInfo);
		
		// if the user in context has already voted, disable the provision to
		// access the voting panel
		
		if(currentUser.isHasVoted()) {
			btnToVotingPanel.setEnabled(false);
		}
		
		// should the user click the "Go To Administrative Panel" button, the application
		// would redirect to the user to the Administrative panel activity. The VoteManager
		// object is passed for concurrency, while the user object is passed to keep
		// the current session open. the serial connection is closed for smooth transition.
		
		btnToAdminPanel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent toAdminPanel = new Intent(AdminVoterPanel.this, AdminPanel.class);
				toAdminPanel.putExtra("vManager", vManager);
				toAdminPanel.putExtra("usr", currentUser);
				startActivity(toAdminPanel);
				finish();
			}
		});
		
		// should the user click the "Go To Voter Panel" button, the application
		// would redirect to the user to the EBallot activity, through the UserInfo
		// activity. The VoteManager object is passed for concurrency, while the user 
		// object is passed to keep the current session open. the serial connection 
		// is closed for smooth transition.
		
		btnToVotingPanel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(vManager.isVotingStatus()) {
					Intent toVInfoPanel = new Intent(AdminVoterPanel.this, UserInfo.class);
					toVInfoPanel.putExtra("vManager", vManager);
					toVInfoPanel.putExtra("usr", currentUser);
					startActivity(toVInfoPanel);
					finish();
				} else {
					Toast.makeText(AdminVoterPanel.this, "Voting process has not started yet. Try again later.", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}
