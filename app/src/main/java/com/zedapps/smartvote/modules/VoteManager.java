/*************************************************************
 * VoteManager Class
 * The class that helps in retaining concurrency throughout the application run time,
 * since the SVMain object could not be made parcelable, due to usage of external libraries.
 * Implements the Parcelable interface to allow the object to be passed between activities.
 * A part of the SmartVote application, developed by ZedApps.
 * 
 * @author Shamah M Zoha
 * @email shamah1992@gmail.com
 * @github bitbucket.org/smzoha/
 ************************************************************/

package com.zedapps.smartvote.modules;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

public class VoteManager implements Parcelable {
	private boolean votingStatus; 	// boolean value which indicates the status of the voting process - active/inactive
	private ArrayList<String> sessionIDList; // a list that stores all the session ID, to avoid duplication
	
	// a constant that contains the path to the file that stores the session ID.
	// a separate file is maintained to ensure that the IDs used are stored even after the application is closed. 
	
	private static final File listFile = new File(Environment.getDataDirectory().getPath() + 
			"/data/com.zedapps.smartvote/databases/sessionid.txt");
	
	
	// default constructor which sets the votingStatus flag to false and instantiates the
	// list object, before calling a method to populate it, namely populateList
	
	public VoteManager() {
		votingStatus = false;
		sessionIDList = new ArrayList<String>();
		populateList();
	}
	
	
	/******************************************************
	 * 				GETTER & SETTER METHODS				  *
	 ******************************************************/
	
	public boolean isVotingStatus() {
		return votingStatus;
	}
	
	
	public void setVotingStatus(boolean votingStatus) {
		this.votingStatus = votingStatus;
	}
	
	/******************************************************
	 * 			END OF GETTER & SETTER METHODS			  *
	 ******************************************************/
	
	
	// method used to populate the session ID list from file
	
	public void populateList() {
		try {
			
			// if the file exists in the system, a buffered reader is created to read
			// the file. else, a new empty file is created, in which the data will be 
			// stored.
			
			if(listFile.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(listFile));
				String sessionCode = "";
				
				// as long as the file has lines remaining to read, the loop will
				// continue and add them into the list
				
				while((sessionCode = br.readLine()) != null) {
					sessionIDList.add(sessionCode);
				}
				
				
				// finally, the reader is closed
				
				br.close();
			} else {
				listFile.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	// method to add a session ID to the list, that is passed in as a parameter
	
	public void addSID(String currSessionID) {
		try {
			
			// the session ID is first added to the list
			
			sessionIDList.add(currSessionID);
			
			// next, a buffered writer is created with the text file, and the
			// session ID is written to it, to ensure that it is stored for future
			// usage and to avoid duplication
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(listFile, true));
			bw.write(currSessionID);
			bw.flush();
			bw.newLine();
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// method that is used to generate a 15 digit unique session ID
	
	public String generateSessionID() {
		String retVal = "";
		boolean loop = true;
		
		// a loop is created until the flag is set to false - which is done when an
		// unique number has been generated
		
		while(loop) {
			
			// through concatenation of 15 random digits to a string object, the session
			// ID is created
			
			String tmp = "";
			for(int i = 0; i < 15; i++) {
				tmp = tmp + (int)(Math.random()*10);
			}
			
			// if the generated value already exists in the list, the code is regenerated.
			// however, if the value is unique, it is passed to the return variable,
			// and the loop flag is set to false, in order to break the loop.
			
			if(!sessionIDList.contains(tmp)) {
				retVal = tmp;
				loop = false;
			}
		}
		
		// the unique session ID is returned in the end
		
		return retVal;
	}

	
	// constructor that helps in creating an VoteManager object from an incoming parcel
	
    protected VoteManager(Parcel in) {
        votingStatus = in.readByte() != 0x00;
        if (in.readByte() == 0x01) {
            sessionIDList = new ArrayList<String>();
            in.readList(sessionIDList, String.class.getClassLoader());
        } else {
            sessionIDList = null;
        }
    }
    

    // overriding the describeContents method of the Parcelable interface
    
    @Override
    public int describeContents() {
        return 0;
    }

    
    // overriding the writeToParcel method of the Parcelable interface to write object to parcel
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (votingStatus ? 0x01 : 0x00));
        if (sessionIDList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(sessionIDList);
        }
    }

    
    // method to create creator object which helps in creating the parcel object upon transfer
    
    public static final Parcelable.Creator<VoteManager> CREATOR = new Parcelable.Creator<VoteManager>() {
        @Override
        public VoteManager createFromParcel(Parcel in) {
            return new VoteManager(in);
        }

        @Override
        public VoteManager[] newArray(int size) {
            return new VoteManager[size];
        }
    };
}
