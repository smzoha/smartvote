/*************************************************************
 * SVMain Class
 * The class that houses all the methods of the modules, such that it can be freely used
 * by using a single instance of this class. Houses all methods of the FPManager and the
 * DatabaseManager classes, along with a few methods of it's own.
 * A part of the SmartVote application, developed by ZedApps.
 * 
 * @author Shamah M Zoha
 * @email shamah1992@gmail.com
 * @github bitbucket.org/smzoha/
 ************************************************************/

package com.zedapps.smartvote;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.zedapps.smartvote.modules.Entity;
import com.zedapps.smartvote.modules.User;
import com.zedapps.smartvote.modules.VoteManager;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.Toast;

public class SVMain {
	
	FPManager fpManager; // instance of the FPManager class, which ensures serial communication
	DatabaseManager dbManager; // instance of the DatabaseManager class, which connects to the database
	VoteManager vManager; // instance of the VoteManager class, which manages concurrency
	
	
	private static String db_loc = Environment.getDataDirectory().getAbsolutePath() +	// constant that defines
			"/data/com.zedapps.smartvote/databases/";									// the location of the database files
	private static String db_name = "svdatabase.db"; // the database file name
	protected String NID_VAL; // the string object that holds the NID value of the current user
	
	Context appContext; // context object that holds the application context
	
	
	// default constructor which takes in the application context and instantiates the
	// FPManager and DatabaseManager objects
	
	public SVMain(Context context) {
		appContext = context;
		fpManager = new FPManager(appContext);
		dbManager = new DatabaseManager(appContext);
	}
	
	
	/******************************************************
	 * 			METHODS OF FPMANAGER CLASS				  *
	 ******************************************************/
	
	// checks if the Arduino is connected to the system
	
	public boolean deviceExists() {
		return fpManager.fpDeviceExists();
	}
	
	// opens serial connection with the Arduino
	
	public void fpOpen() {
		fpManager.fpOpen();
	}
	
	// closes serial connection with the Arduino
	
	public void fpClose() {
		fpManager.fpClose();
	}
	
	// requests USB Permission to allow serial connectivity
	
	public void fpPerm() {
		fpManager.fpPerm();
	}
	
	// begins the fingerprint enroll process, with proper handling for error and success
	// messages
	
	public void fpEnroll() {
		fpManager.fpEnroll();
			
		while(true) {
			if(fpManager.getEnrollStatus()) {
				break;
			}
			
			if(fpManager.getEnrollError()) {
				break;
			}
		}
	}
	
	// obtains the binary template file of the enrolled fingerprint as a temporary file
	// that is created in the database directory of the application. only performs the task
	// if enroll has been completed successfully, i.e. no error occurred.
	
	public boolean fpGetTemplate() {
		if(!fpManager.getEnrollError()) {
			try {
				fpManager.fpGetTemplate();
			} catch(Exception e) {
				Toast.makeText(appContext, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
			
			Toast.makeText(appContext, "Temporary template file created!", Toast.LENGTH_SHORT).show();
			return true;
		} else {
			return false;
		}
	}
	
	// a single method that houses the scan voter, set template and the verification methods
	// that can be found in the FPManager class, along with proper pauses to ensure that a
	// synchronization between the Arduino and the Android device can be established.
	// at first, the live fingerprint impression is scanned, followed by the setting of
	// database into the scanner. the handling for the 200 fingerprint capacity is done here,
	// by calling the verifying method once the capacity of the scanner's buffer is full.
	// also, 10 bytes are sent over a buffer at a time to ensure perfect transmission of data.
	
	public void fpIdentify() {
		try {
			fpManager.fpScanVoter();
			Thread.sleep(7000);
			
			if(fpManager.getScanStatus()) {
				List<File> fpFiles = getFpFiles();
				
				int count = 0, prevBulkSize = 0;
				
				for(int i = 0; i < fpFiles.size(); i++) {
					fpManager.fpSetTemplate(fpFiles.get(i), i);
					Thread.sleep(2000);
					count++;
					
					if(count == 200) {
						fpManager.fpVerify();
						Thread.sleep(2000);
						
						int tmpStatus = fpManager.getVerifyStatus() & 0xFF;
						
						if(tmpStatus > -1 && tmpStatus != Integer.MAX_VALUE) {
							NID_VAL = FilenameUtils.removeExtension(fpFiles.get(prevBulkSize+tmpStatus).getName()); 
							count = 0;
							break;
						}
						
						count = 0;
						prevBulkSize = i;
					}
				}
				
				if(count != 0) {
					fpManager.fpVerify();
					Thread.sleep(2000);
					
					int tmpStatus = fpManager.getVerifyStatus() & 0xFF;
					
					if(tmpStatus > -1 && tmpStatus != Integer.MAX_VALUE) {
						NID_VAL = FilenameUtils.removeExtension(fpFiles.get(prevBulkSize+tmpStatus).getName());
					} else {
						NID_VAL = "";
					}
					
					count = 0;
				}
				
			} else {
				Toast.makeText(appContext, "Scanning Failed!", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Toast.makeText(appContext, "Something went wrong. Try again.", Toast.LENGTH_SHORT).show();
		}
	}
	
	// returns the NID value that is obtained after the verification process, which points
	// at the user in the context
	
	public String getNID() {
		return NID_VAL;
	}
	
	// prints the physical ballot, with the candidate name, file name of the symbol, the
	// current system date and the session ID being passed to the method
	
	public void printBallot(String cName, File symFile, String date_time, String randnum) {
		fpManager.printBallot(cName, symFile, date_time, randnum);
	}
	

	/******************************************************
	 * 			METHODS OF DATABASEMANAGER CLASS		  *
	 ******************************************************/
	
	// opens connection with the database
	
	public void dbOpen() {
		try {
			dbManager.openDatabase();
		} catch (SQLException e) {
			Toast.makeText(appContext, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	// obtains all data from the entity table and returns as an array list
	
	public ArrayList<Entity> getEntityData() {
		return dbManager.getAllEntity();
	}
	
	// increments the value of the vote count field, which bears the e_id that is provided
	// into the method. the previous count is provided as well to ensure proper increment.
	
	public void incrementVCount(int eID, int count) {
		dbManager.incrementVoteCount(eID, count);
	}
	
	// obtains all data from the user table and returns as an array list
	
	public ArrayList<User> getUserData() {
		return dbManager.getAllUser();
	}
	
	// returns the User object that contains the information of the specific user
	// bearing the NID passed into the method
	
	public User getSpecificUser(String NID_VAL) {
		return dbManager.getSpecificUser(NID_VAL);
	}
	
	// adds a new tuple to the user table, with informations being in the array of string
	// passed to it
	
	public void addNewVoter(String [] data) {
		dbManager.insertVoter(data);
	}
	
	// removes the tuple from the user table that is represented by the NID value passed
	// into the method. also removes the fingerprint template file, as well as the user 
	// photo, to ensure complete removal from the system.
	
	public void removeSpecificUser(String NID_VAL) {
		dbManager.deleteSpecificUser(NID_VAL);
		try {
			FileUtils.forceDelete(new File(db_loc + "fpFiles/" + NID_VAL + ".dat"));
			FileUtils.forceDelete(new File(db_loc + "images/" + NID_VAL + ".jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// set the hasVoted flag to true for a specific user in the user table, represented by
	// the NID value passed to the method
	
	public void sethasVotedForUser(String NID_VAL) {
		dbManager.sethasVotedForUser(NID_VAL);
	}
	
	// close connection with the database
	
	public void dbClose() {
		dbManager.close();
	}
	
	/******************************************************
	 * 					GENERIC METHODS					  *
	 ******************************************************/
	
	// binds the VoteManager object passed to maintain concurrency
	
	public void setVManager(VoteManager vm) {
		vManager = vm;
	}
	
	// checks if the files required for the application are in place. checks if the
	// database file exists, along with the folder for the fingerprint templates, the
	// user images, and the entity images. returns false if any one of the directory or
	// file does not exist. returns true if all exists.
	
	public boolean checkFiles() {
		boolean retVal = false;
		
		File dbLoc = new File(db_loc);
		File dbFile = new File(db_loc + db_name);
		File fpDatDir = new File(db_loc + "fpFiles/");
		File imgDir = new File(db_loc + "images/");
		File eImgDir = new File(db_loc + "eImages/");
		
		if(dbLoc.exists() && dbFile.exists() && fpDatDir.exists()
				&& imgDir.exists() && eImgDir.exists()) {
			retVal = true;
		}
		
		if(retVal == false && !dbLoc.exists()) {
			dbLoc.mkdir();
		}
		
		return retVal;
	}
	
	// extracts the files from a zip file which is passed into the method. the extracted
	// files are stored in the database directory to serve the purpose of the application.
	// utilizes Zip4J library over the native method to enable the use of encryption on
	// the data zip file, i.e. allow for password protected zip files to be used in the 
	// system.
	
	public boolean unzipFiles(File src, String password) {
		try {
			ZipFile zipSrc = new ZipFile(src);
			if(zipSrc.isEncrypted()) {
				zipSrc.setPassword(password);
			}
			zipSrc.extractAll(db_loc);
		} catch (ZipException ze) {
			return false;
		}
		
		return true;
	}
	
	// removes the database file completely, along with the directory where it is stored
	
	public void removeDB() {
		File dbLoc = new File(db_loc);
		try {
			FileUtils.deleteDirectory(dbLoc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// obtains a list of the fingerprint files and returns as output
	
	public List<File> getFpFiles() {
		File dir = new File(db_loc + "fpFiles/");
		File[] fpFilesArr = dir.listFiles();
		List<File> fpFilesList = Arrays.asList(fpFilesArr);
		return fpFilesList;
	}
	
	// converts user image files to bitmap object, using the NID value provided, and
	// return it for further use
	
	public Bitmap getImage(String NID) {
		File imgFile = new File(db_loc + "images/" + NID + ".jpg");
		
		if(imgFile.exists()) {
			Bitmap bmpRef = BitmapFactory.decodeFile(imgFile.getPath());
			return bmpRef;
		} else {
			return null;
		}
	}
	
	// END OF GENERIC FUNCTION //
	
	
}
