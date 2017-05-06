/*************************************************************
 * Report Class
 * The back-end implementation of the activity represented by the Report.xml file.
 * Allows the administrators to view the current report of the voting process - the vote tally.
 * A part of the SmartVote application, developed by ZedApps.
 * 
 * @author Shamah M Zoha
 * @email shamah1992@gmail.com
 * @github bitbucket.org/smzoha/
 ************************************************************/

package com.zedapps.smartvote;

import java.util.ArrayList;

import com.zedapps.smartvote.modules.Entity;
import com.zedapps.smartvote.modules.User;
import com.zedapps.smartvote.modules.VoteManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Report extends Activity {
	
	VoteManager vManager; // instance of the VoteManager class
	User currentUser;     // User object representing the current user (administrator)
	SVMain svmainObj;     // instance of SVMain class, which houses all the modules
	
	Button btnRepExit;	// button that takes user back to the administrative panel
	TextView txtRep;	// text view that displays the voting report

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report);
		
		// receiving parcel transfered by the previous activity
		// and store them in the designated variable
								
		vManager = (VoteManager) getIntent().getParcelableExtra("vManager");
		currentUser = (User) getIntent().getParcelableExtra("usr");
								
		// instantiate object of the SVMain class and bind the vManager object to it.
								
		svmainObj = new SVMain(getApplicationContext());
		svmainObj.setVManager(vManager);
							
		// instantiate the UI components
		
		btnRepExit = (Button) findViewById(R.id.btnRepExit);
		txtRep = (TextView) findViewById(R.id.txtRep);
		
		// create an array list which encapsulates the list of entity obtained from
		// the database
		
		ArrayList<Entity> eList = svmainObj.getEntityData();
		
		// the data retrieved from the database is then retrieved in the text view
		// placed in the interface. the entity name, the candidate name, and the vote
		// count is displayed as the report.
		
		for(int i = 0; i < eList.size(); i++) {
			Entity tmpEn = eList.get(i);
			txtRep.append(tmpEn.geteName() + " " + tmpEn.getcName() + " " + tmpEn.getvCount()
					+ "\n");
		}
		
		// should the user press the "Exit" button, the user is then taken back to the 
		// Administrative panel, with the VoteManager object passed back to maintain 
		// concurrency and the User object for maintaining the session. the serial connection 
		// is closed in the end to ensure smooth transition.
		
		btnRepExit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent toAdminPanel = new Intent(Report.this, AdminPanel.class);
				toAdminPanel.putExtra("vManager", vManager);
				toAdminPanel.putExtra("usr", currentUser);
				startActivity(toAdminPanel);
			}
		});
	}
}
