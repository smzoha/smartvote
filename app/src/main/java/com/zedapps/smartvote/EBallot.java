/*************************************************************
 * EBallot Class
 * The back-end implementation of the activity represented by the EBallot.xml file.
 * Provides the user (voter) with an electronic ballot to cast their vote.
 * A part of the SmartVote application, developed by ZedApps.
 * 
 * @author Shamah M Zoha
 * @email shamah1992@gmail.com
 * @github bitbucket.org/smzoha/
 ************************************************************/

package com.zedapps.smartvote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import com.zedapps.smartvote.modules.Candidate;
import com.zedapps.smartvote.modules.Entity;
import com.zedapps.smartvote.modules.User;
import com.zedapps.smartvote.modules.VoteManager;

public class EBallot extends Activity {
	
	VoteManager vManager; // instance of the VoteManager class
	User currentUser;     // User object representing the current user (voter)
	SVMain svmainObj;     // instance of SVMain class, which houses all the modules
	
	ListView lstView;		// ListView that holds the candidate information
	Candidate[] candidateArr; // an array to contain the Candidate objects
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_eballot);
		
		// receiving parcel transfered by the previous activity
		// and store them in the designated variable
		
		vManager = (VoteManager) getIntent().getParcelableExtra("vManager");
		currentUser = (User) getIntent().getParcelableExtra("usr");
		
		// instantiate object of the SVMain class and bind the vManager object to it.
		// next, initiate the serial connection with the Arduino
		
		svmainObj = new SVMain(getApplicationContext());
		svmainObj.setVManager(vManager);
		
		svmainObj.fpOpen();
		
		// obtain all entity from the database, and instantiate the array to hold
		// the Candidate objects. a loop is run to include the relevant informations
		// of the Entity objects into the array as Candidate objects.
		
		final ArrayList<Entity> eData = svmainObj.getEntityData();
		candidateArr = new Candidate[eData.size()];
		
		for(int i = 0; i < eData.size(); i++) {
			Entity tmp = eData.get(i);
			candidateArr[i] = new Candidate(tmp.getcName(), tmp.geteSym()+".bmp");
		}
		
		// instantiate the UI component
		
		lstView = (ListView) findViewById(R.id.lstBallot);
		
		// attach the BallotArrayAdapter to the ListView, following instantiation.
		
		BallotArrayAdapter baAdapter = new BallotArrayAdapter(EBallot.this, R.layout.ballot_layout, candidateArr);
		lstView.setAdapter(baAdapter);
		
		// attach an OnItemLongClickListener to the ListView to ensure that it listens for
		// long press on the object that are held by the ListView. Upon long click, a temporary
		// Entity object is created, based on the click on the list. A dialog is created
		// which prompts the user to confirm their vote. if the user confirms, the application
		// moves to the confirmVote method. else, it returns focus to the EBallot activity once
		// again.
		
		lstView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				final Entity tmpEn = eData.get(position);
				
				AlertDialog.Builder diagBuilder = new AlertDialog.Builder(EBallot.this);
				diagBuilder.setTitle("Confirmation".toString());
				diagBuilder.setMessage("Are you sure you wish to cast your vote to " + tmpEn.getcName() + "?" +
						"\nYour vote will be casted beyond this point and you cannot change your option!");
				diagBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						confirmVote(tmpEn);
					}
				});
				
				diagBuilder.setNegativeButton("No", null);
				
				diagBuilder.show();
				
				return true;
			}
		});
	}
	
	// the confirmVote method finalizes the voting process.
	// the temporary Entity object is passed in and a session ID is created using
	// the VoteManager object. this session ID, along with the Entity object is passed
	// to another method called print, which handles the printing process of the physical
	// ballot. the vote count is incremented and the voting status is set with the help of
	// the SVMain object, before the user is redirected to the Thank You activity. the
	// VoteManager object is passed to maintain concurrency and the serial connection is
	// closed for smooth transition.
	
	public void confirmVote(Entity tmpEn) {
		String sID = vManager.generateSessionID();
		vManager.addSID(sID);
			
		print(tmpEn, sID);
			
		svmainObj.incrementVCount(tmpEn.geteID(), tmpEn.getvCount());
		svmainObj.sethasVotedForUser(currentUser.getnID());
			
		Intent toThankMsg = new Intent(EBallot.this, ThankUser.class);
		toThankMsg.putExtra("vManager", vManager);
		startActivity(toThankMsg);
		finish();
		svmainObj.fpClose();
	}
	
	// the print method allows the user to obtain the physical ballot.
	// the Entity object is passed, as well as the session ID.
	// the current system date is obtained, before the printBallot method in the
	// SVMain class is called, with the candidate name being passed, along with the R file
	// of the entity symbol, the current date, and the session ID.
	
	public void print(Entity tmpEn, String sID) {
		Date currDate = new Date(System.currentTimeMillis());
		svmainObj.printBallot(tmpEn.getcName(), new File(tmpEn.geteSym()+".R"), currDate.toString(),
				sID);
	}
}
