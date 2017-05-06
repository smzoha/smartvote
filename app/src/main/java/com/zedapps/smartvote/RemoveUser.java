/*************************************************************
 * RemoveUser Class
 * The back-end implementation of the activity represented by the RemoveUser.xml file.
 * Allows the administrator to remove user from the database - both tuple and data files.
 * A part of the SmartVote application, developed by ZedApps.
 * 
 * @author Shamah M Zoha
 * @email shamah1992@gmail.com
 * @github bitbucket.org/smzoha/
 ************************************************************/

package com.zedapps.smartvote;

import java.util.ArrayList;

import com.zedapps.smartvote.modules.User;
import com.zedapps.smartvote.modules.VoteManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class RemoveUser extends Activity {

	VoteManager vManager; // instance of the VoteManager class
	User currentUser;     // User object representing the current user (administrator)
	SVMain svmainObj;     // instance of SVMain class, which houses all the modules
	
	ListView lvUsers; // the list displaying the NID values of all tuples of the user table
	
	Button btnBackAP; // the button that takes the user back to the administrative panel
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remove_user);
		
		// receiving parcel transfered by the previous activity
		// and store them in the designated variable
						
		vManager = (VoteManager) getIntent().getParcelableExtra("vManager");
		currentUser = (User) getIntent().getParcelableExtra("usr");
						
		// instantiate object of the SVMain class and bind the vManager object to it.
						
		svmainObj = new SVMain(getApplicationContext());
		svmainObj.setVManager(vManager);
						
		// instantiate the UI components
		
		lvUsers = (ListView) findViewById(R.id.lstUser);
		btnBackAP = (Button) findViewById(R.id.btnBackAP);
		
		// creating two array list, the first of which takes the User objects in
		// while the other is used to store only the NID values. the second list is
		// filled with a loop that strips the NID values from the first list.
		
		final ArrayList<User> userData = svmainObj.getUserData();
		final ArrayList<String> userNID = new ArrayList<String>();
		
		for(int i = 0; i < userData.size(); i++) {
			userNID.add(userData.get(i).getnID());
		}
		
		// an array adapter is created with the userNID list and attached to the list view
		
		final ArrayAdapter<String> userListAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, userNID);
		lvUsers.setAdapter(userListAdapter);
		
		// if an item in the list view is long pressed, the user object in the clicked position
		// is retrieved from the list. a confirmation dialog is displayed, which displays the name
		// of the user as well. upon clicking the "Yes" button on the dialog, the record is removed
		// from the list view, as well as the database and the file system. the "No" button dismiss
		// the dialog.
		
		lvUsers.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				final User tmpUser = userData.get(position);
				
				AlertDialog.Builder diagBuilder = new AlertDialog.Builder(RemoveUser.this);
				diagBuilder.setTitle("Confirmation".toString());
				diagBuilder.setMessage("Are you sure you want to remove the user - NID: " +
						tmpUser.getnID() + " Name: " + tmpUser.getName() + ".");
				diagBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						svmainObj.removeSpecificUser(tmpUser.getnID());
						userData.remove(position);
						userNID.remove(position);
						userListAdapter.notifyDataSetChanged();
						dialog.cancel();
						Toast.makeText(RemoveUser.this, "User has been removed from the system.",
								Toast.LENGTH_SHORT).show();
					}
				});
				
				diagBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				
				diagBuilder.show();
								
				return true;
			}
			
		});
		
		// should the user press the "Exit" button, the user is then taken back to the 
		// Administrative panel, with the VoteManager object passed back to maintain 
		// concurrency and the User object for maintaining the session. the serial connection 
		// is closed in the end to ensure smooth transition.
		
		btnBackAP.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent toAdminPanel = new Intent(RemoveUser.this, AdminPanel.class);
				toAdminPanel.putExtra("vManager", vManager);
				toAdminPanel.putExtra("usr", currentUser);
				startActivity(toAdminPanel);
				finish();
			}
		});
	}
}
