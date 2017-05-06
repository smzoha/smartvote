/*************************************************************
 * NewVoter Class
 * The back-end implementation of the activity represented by the NewVoter.xml file.
 * Provides the user with a form to enter the voter information and add it to the database.
 * Includes provision for enrolling fingerprint and taking picture of the user.
 * A part of the SmartVote application, developed by ZedApps.
 * 
 * @author Shamah M Zoha
 * @email shamah1992@gmail.com
 * @github bitbucket.org/smzoha/
 ************************************************************/

package com.zedapps.smartvote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.io.FileUtils;

import com.zedapps.smartvote.modules.User;
import com.zedapps.smartvote.modules.VoteManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NewVoter extends Activity {
	private static String app_data_loc = Environment.getDataDirectory().getAbsolutePath() +	// constant that defines
			"/data/com.zedapps.smartvote/databases/";										// the location of the database files
	
	
	private final int CAMERA_REQ = 1;  // request code for capturing image
	private final int FIN_REQ = 2;	   // request code for saving image
		
	File tmpFP = new File(app_data_loc + "tmp.dat");	// file object representing temporary fingerprint template file
	File tmpImg = new File(app_data_loc + "tmp.jpg");	// file object representing temporary image file of user
	
	VoteManager vManager; // instance of the VoteManager class
	User currentUser;     // User object representing the current user (administrator)
	SVMain svmainObj;     // instance of SVMain class, which houses all the modules

	EditText txtNID;	// text field for the user to input NID value
	EditText txtFName;	// text field for the user to input their first name
	EditText txtMName;	// text field for the user to input their middle name
	EditText txtLName;	// text field for the user to input their last name
	EditText txtAddress; // text field for the user to input their address
	EditText txtDOB;	// text field for the user to input their date of birth in dd/mm/yyyy format
	EditText txtFAName;	// text field for the user to input their father's name
	EditText txtMOName;	// text field for the user to input their mother's name
	
	Button btnEnroll;	// button that triggers enroll action
	Button btnTP;		// button that triggers capturing image action
	Button btnDiscard;	// button that discards the progress made
	Button btnSave;		// button that saves the new voter's informations into the system
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_voter);
		
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
		
		txtNID = (EditText) findViewById(R.id.txtNID);
		txtFName = (EditText) findViewById(R.id.txtFName);
		txtMName = (EditText) findViewById(R.id.txtMName);
		txtLName = (EditText) findViewById(R.id.txtLName);
		txtAddress = (EditText) findViewById(R.id.txtAddress);
		txtDOB = (EditText) findViewById(R.id.txtDOB);
		txtFAName = (EditText) findViewById(R.id.txtFAName);
		txtMOName = (EditText) findViewById(R.id.txtMAName);
		
		btnEnroll = (Button) findViewById(R.id.btnEnroll);
		btnTP = (Button) findViewById(R.id.btnImage);
		btnDiscard = (Button) findViewById(R.id.btnDiscard);
		btnSave = (Button) findViewById(R.id.btnSave);
		
		// disable the take picture, save and discard buttons initially
		
		btnTP.setEnabled(false);
		btnSave.setEnabled(false);
		btnDiscard.setEnabled(false);
		
		// should the user press the "Enroll" button, the enroll process will begin,
		// followed up by the storing of the temporary template file in the device.
		// if both the process is a success, the "Take Picture" button is enabled, as well
		// as the "Discard" button.
		
		btnEnroll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				svmainObj.fpEnroll();
				boolean result = svmainObj.fpGetTemplate();
				
				if(result) {
					btnEnroll.setEnabled(false);
					btnTP.setEnabled(true);
					btnDiscard.setEnabled(true);
				}
			}
		});
		
		// should the user press the "Take Picture" button, the startCamera method is
		// started, where the image is captured and processed for storing
		
		btnTP.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startCamera();
			}
		});
		
		// should the user press the "Save" button, the information on the fields
		// are taken into a string array, which is passed in for storing in the database.
		// the information provided for DOB is broken down to millisecond with the help
		// of SimpleDateFormat object.
		
		btnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String [] data = new String[8];
				
				data[0] = txtNID.getText().toString();
				data[1] = txtFName.getText().toString();
				data[2] = txtMName.getText().toString();
				data[3] = txtLName.getText().toString();
				data[4] = txtAddress.getText().toString();
				
				String tmpDateStr = txtDOB.getText().toString();
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
				Date tmpDateObj = new Date();
				
				try {
					tmpDateObj = sdf.parse(tmpDateStr);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				data[5] = Long.toString(tmpDateObj.getTime());
				
				data[6] = txtFAName.getText().toString();
				data[7] = txtMOName.getText().toString();
				
				svmainObj.addNewVoter(data);
				
				// finally, the temporary files are copied to the designated directory, 
				// with them being renamed accordingly with the NID value. 
				
				File voterFP = new File(app_data_loc + "fpFiles/" + data[0]+".dat");
				File voterImg = new File(app_data_loc + "images/" + data[0]+".jpg");
				
				
				try {
					FileUtils.moveFile(tmpFP, voterFP);
					FileUtils.moveFile(tmpImg, voterImg);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				Toast.makeText(NewVoter.this, "Voter successfully added to database!", Toast.LENGTH_SHORT).show();
				
				// the user is then taken back to the Administrative panel, with the
				// VoteManager object passed back to maintain concurrency and the User object
				// for maintaing the session. the serial connection is closed in the end to
				// ensure smooth transition.
				
				Intent toAdminPanel = new Intent(NewVoter.this, AdminPanel.class);
				toAdminPanel.putExtra("vManager", vManager);
				toAdminPanel.putExtra("usr", currentUser);
				startActivity(toAdminPanel);
				finish();
				svmainObj.fpClose();
			}
		});
		
		// should the user press the "Discard" button, the temporary files are going to be
		// removed, before the clearTextViews method is called to reset the form in the
		// interface. finally, the buttons are disabled, except the "Enroll" button, to
		// allow for a second go.
		
		btnDiscard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(tmpFP.exists()) {
					tmpFP.delete();
				}
							
				if(tmpImg.exists()) {
					tmpImg.delete();
				}
				
				clearTextViews();
				
				btnDiscard.setEnabled(false);
				btnEnroll.setEnabled(true);
				btnTP.setEnabled(false);
				btnSave.setEnabled(false);
			}
		});
	}
	
	// the startCamera method sets the location of the temporary image to be the storage
	// folder of the device, before starting the camera activity, which should stop upon
	// receiving the CAMERA_REQ as the result code
	
	public void startCamera() {
		Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, 
				Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "tmp.jpg")));
		startActivityForResult(captureIntent, CAMERA_REQ);
	}
	
	// onActivityResult is overrode, to handle the situation where the requestCode is either
	// CAMERA_REQ or FIN_REQ. for the first scenario, the application moves to the crop image
	// intent, while for the other, the image is saved.
	
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
			
		if(requestCode == CAMERA_REQ) {
			crop(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "tmp.jpg")));
		} 
			
		if (requestCode == FIN_REQ) {
			if(intent != null) {
				saveImage(intent);
			}
		}
	}
	
	// the crop intent is triggered, with the captured image being sent to be cropped
	// to a square image of 300 x 300 pixels
	
	public void crop(Uri path) {
		Intent cropIntent = new Intent("com.android.camera.action.CROP");
		cropIntent.setDataAndType(path, "image/*");
		cropIntent.putExtra("crop", "true");
		cropIntent.putExtra("aspectX", 1);
		cropIntent.putExtra("aspectY", 1);
		cropIntent.putExtra("outputX", 300);
		cropIntent.putExtra("outputY", 300);
		cropIntent.putExtra("return-data", true);
		startActivityForResult(cropIntent, FIN_REQ);
	}
	
	// the cropped image is saved to the external storage at first, as a jpg file,
	// before the file is moved to the database directory as temporary file. the
	// "Take Image" button is disabled and the "Save" button is enabled to ensure
	// the next step to be carried out.
	
	public void saveImage(Intent intent) {
		Bundle data = intent.getExtras();
		if(data != null) {
			Bitmap photo = data.getParcelable("data");
			File tmp = new File(Environment.getExternalStorageDirectory(), "tmp.jpg");
			
			try {
				tmp.createNewFile();
				FileOutputStream fos = new FileOutputStream(tmp);
				photo.compress(Bitmap.CompressFormat.JPEG, 100, fos);
				
				fos.flush();
				fos.close();
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			} finally {
				try {
					FileUtils.moveFile(new File(Environment.getExternalStorageDirectory(), "tmp.jpg"),
							tmpImg);
					Toast.makeText(NewVoter.this, "Image capture successful!", Toast.LENGTH_LONG).show();
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
				
				btnTP.setEnabled(false);
				btnSave.setEnabled(true);
			}
		}
	}
	
	// clears all the text fields to reset the form
	
	public void clearTextViews() {
		txtNID.setText("");
		txtFName.setText("");
		txtMName.setText("");
		txtLName.setText("");
		txtAddress.setText("");
		txtDOB.setText("");
		txtFAName.setText("");
		txtMOName.setText("");
	}
}