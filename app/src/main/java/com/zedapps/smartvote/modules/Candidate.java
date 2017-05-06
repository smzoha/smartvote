/*************************************************************
 * Candidate Class
 * The class that stores the basic information about the candidate, to show them in the EBallot list.
 * Basically, a stripped down version of the Entity class.
 * A part of the SmartVote application, developed by ZedApps.
 * 
 * @author Shamah M Zoha
 * @email shamah1992@gmail.com
 * @github bitbucket.org/smzoha/
 ************************************************************/

package com.zedapps.smartvote.modules;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Candidate {
	
	private String candidate_name; // string object that holds the candidate name
	private Bitmap entity_symbol;  // bitmap object that holds the data for the entity symbol

	
	// default constructor which takes in a candidate name field, as well as the path for the image file of symbol.
	// this path is used to create bitmap object and assign it to the local bitmap variable.
	// the bitmap object is created using the createBMP method, which can be found below. 
	
	public Candidate(String cn, String es_path) {
		candidate_name = cn;
		entity_symbol = createBMP(es_path);
	}
	
	
	/******************************************************
	 * 				GETTER & SETTER METHODS				  *
	 ******************************************************/
	
	public String getCandidate_name() {
		return candidate_name;
	}
	public void setCandidate_name(String candidate_name) {
		this.candidate_name = candidate_name;
	}
	
	public Bitmap getEntity_symbol() {
		return entity_symbol;
	}
	public void setEntity_symbol(String es_path) {
		this.entity_symbol = createBMP(es_path);
	}
	
	/******************************************************
	 * 			END OF GETTER & SETTER METHODS			  *
	 ******************************************************/
	
	// method to create bitmap object from path passed to it.
	// a file object is created using the string path provided, and should the file exist in the system,
	// a bitmap object is created using the BitmapFactory class and the resulting bitmap object is returned
	// as output.
	
	public Bitmap createBMP(String es_path) {
		Bitmap retVal = null;
		File imgFile = new File(es_path);
		
		if(imgFile.exists()) {
			retVal = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		}
		
		return retVal;
	}
	
}
