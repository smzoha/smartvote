/*************************************************************
 * LoadData Class
 * The back-end implementation of the activity represented by the LoadData.xml file.
 * Provides the super administrator with the provision to load the database file.
 * A part of the SmartVote application, developed by ZedApps.
 * 
 * @author Shamah M Zoha
 * @email shamah1992@gmail.com
 * @github bitbucket.org/smzoha/
 ************************************************************/

package com.zedapps.smartvote;

import java.io.File;

import com.zedapps.smartvote.modules.User;
import com.zedapps.smartvote.modules.VoteManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoadData extends Activity {
	
	Button btnLoadDB; // button that triggers the load database process
	EditText txtPath; // text field that takes the file path as input
	EditText txtPass; // text field that takes the file password as input
	
	VoteManager vManager; // instance of the VoteManager class
	SVMain svmainObj;     // instance of SVMain class, which houses all the modules
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load_data);
		
		// receiving parcel transfered by the previous activity
		// and store them in the designated variable
		
		vManager = (VoteManager) getIntent().getParcelableExtra("vManager");
		
		// instantiate object of the SVMain class and bind the vManager object to it.
		// next, initiate the serial connection with the Arduino		
		
		svmainObj = new SVMain(getApplicationContext());
		svmainObj.setVManager(vManager);
		svmainObj.fpOpen();
		
		// instantiate the UI components
		
		btnLoadDB = (Button) findViewById(R.id.btnLoadDBAPanel);
		txtPath = (EditText) findViewById(R.id.txtPath);
		txtPass = (EditText) findViewById(R.id.txtPass);
		
		// should the user press the "Load Database" button, the load method will begin
		
		btnLoadDB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				load();
			}
		});
	}
	
	
	// obtains the input from the text field and assigns them to file and string objects
	// respectively. the SVMain object is used to unzip the file and the resulting boolean
	// value is stored in the result variable. if the operation was a success, the identify
	// method is called for verification. if not, a message is displayed.
	
	private void load() {
		File zipFile = new File(Environment.getExternalStorageDirectory().getPath() +
				"/" + txtPath.getText().toString());
		String pass = txtPass.getText().toString();
		boolean result = svmainObj.unzipFiles(zipFile, pass);
		
		if(result) {
			identify();
		} else {
			Toast.makeText(this, "File not loaded. Check path and password.", Toast.LENGTH_SHORT).show();
		}
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
		// after to avoid memory leakage. if no match is found, a message is displayed and database is removed.
		
		if(fpNID != null) {
				
			svmainObj.dbOpen();
			User currentUser = svmainObj.getSpecificUser(fpNID);
			svmainObj.dbClose();
			
			// if the user exists in the database, it is checked whether the user is a super administrator or not.
			// depending the status of the user, the next actions are taken. if the database yields that is null, 
			// an invalid fingerprint data message is displayed, followed by the removal of the database files.
			
			if(currentUser != null) {
				
				// if the user is a super administrator, the user is referred directly to the administrative panel.
				// the VoteManager object is passed for concurrency and the current User object is also passed
				// to start an active session. the serial connection is closed to ensure smooth transition.
				
				if(currentUser.isSAdmin()) {
					Intent toAdminPanel = new Intent(getApplicationContext(), AdminPanel.class);
					toAdminPanel.putExtra("vManager", vManager);
					toAdminPanel.putExtra("usr", currentUser);
					startActivity(toAdminPanel);
					finish();
					svmainObj.fpClose();
					
					// if the user not a super administrator, the database loaded is regarded void.
					// the files that are extracted are removed and appropriate messages are displayed.
					
				} else {
					Toast.makeText(LoadData.this, "Access Denied. Removing Database...", Toast.LENGTH_SHORT).show();
					svmainObj.removeDB();
					Toast.makeText(LoadData.this, "Database removed. Try again.", Toast.LENGTH_SHORT).show();
				}
				
				// message displayed if the fingerprint data is invalid and database is removed.
				
			} else {
				Toast.makeText(LoadData.this, "Invalid fingerprint data. Removing Database...", Toast.LENGTH_SHORT).show();
				svmainObj.removeDB();
				Toast.makeText(LoadData.this, "Database removed. Try again.", Toast.LENGTH_SHORT).show();
			}
			
			// message displayed if no match is found and the database is removed.
			
		} else {
			Toast.makeText(LoadData.this, "Match not found. Removing Database...", Toast.LENGTH_SHORT).show();
			svmainObj.removeDB();
			Toast.makeText(LoadData.this, "Database removed. Try again.", Toast.LENGTH_SHORT).show();
		}
	}
}
