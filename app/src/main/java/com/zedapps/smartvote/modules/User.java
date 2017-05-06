/*************************************************************
 * User Class
 * The class that helps in mapping the values stored in the User table of the database.
 * Implements the Parcelable interface to allow the object to be passed between activities.
 * A part of the SmartVote application, developed by ZedApps.
 * 
 * @author Shamah M Zoha
 * @email shamah1992@gmail.com
 * @github bitbucket.org/smzoha/
 ************************************************************/

package com.zedapps.smartvote.modules;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
	private String nID;		// the national ID value of the user
	private String name;	// the name of the user (a concatenation of the first, middle and last name fields) 
	private String address;	// the address of the user
	private long dob;		// the date of birth of the user, represented in milliseconds
	private String fatherName;	// the father's name of user
	private String motherName;	// the mother's name of user
	private boolean hasVoted;	// the flag which indicates if the user has voted or not
	private boolean isVoter;	// the flag which indicates if the user is a voter
	private boolean isAdmin;	// the flag which indicates if the user is an administrator
	private boolean isSAdmin;	// the flag which indicates if the user is a super administrator
	
	
	// default constructor for the class, which takes in the information and store them in the variables.
	// the values usually come from the result of a query that is run on the database.
	// additionally, it resolves the integer flags stored in the table to boolean values.
	
	public User(String nID, String fname, String mName, String lName,
			String address, long dob, String faName, String maName,
			int hasVoted, int isVoter, int isAdmin, int isSAdmin) {
		setnID(nID);
		setName(fname + " " + mName + " " + lName);
		setAddress(address);
		setDob(dob);
		setFatherName(faName);
		setMotherName(maName);
		
		if(hasVoted == 1) {
			setHasVoted(true);
		} else {
			setHasVoted(false);
		}
		
		if(isVoter == 1) {
			setVoter(true);
		} else {
			setVoter(false);
		}
		
		if(isAdmin == 1) {
			setAdmin(true);
		} else {
			setAdmin(false);
		}
		
		if(isSAdmin == 1) {
			setSAdmin(true);
		} else {
			setSAdmin(false);
		}
	}
	
	/******************************************************
	 * 				GETTER & SETTER METHODS				  *
	 ******************************************************/	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getnID() {
		return nID;
	}
	
	public void setnID(String nID) {
		this.nID = nID;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public long getDob() {
		return dob;
	}
	
	public void setDob(long dob) {
		this.dob = dob;
	}
	
	public String getFatherName() {
		return fatherName;
	}
	
	public void setFatherName(String fatherName) {
		this.fatherName = fatherName;
	}
	
	public String getMotherName() {
		return motherName;
	}
	
	public void setMotherName(String motherName) {
		this.motherName = motherName;
	}
	public boolean isHasVoted() {
		return hasVoted;
	}
	
	public void setHasVoted(boolean hasVoted) {
		this.hasVoted = hasVoted;
	}
	
	public boolean isVoter() {
		return isVoter;
	}
	
	public void setVoter(boolean isVoter) {
		this.isVoter = isVoter;
	}
	
	public boolean isAdmin() {
		return isAdmin;
	}
	
	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	
	public boolean isSAdmin() {
		return isSAdmin;
	}
	
	public void setSAdmin(boolean isSAdmin) {
		this.isSAdmin = isSAdmin;
	}

	/******************************************************
	 * 			END OF GETTER & SETTER METHODS			  *
	 ******************************************************/	
	
	// constructor that helps in creating an Entity object from an incoming parcel
	
    protected User(Parcel in) {
        nID = in.readString();
        name = in.readString();
        address = in.readString();
        dob = in.readLong();
        fatherName = in.readString();
        motherName = in.readString();
        hasVoted = in.readByte() != 0x00;
        isVoter = in.readByte() != 0x00;
        isAdmin = in.readByte() != 0x00;
        isSAdmin = in.readByte() != 0x00;
    }
    
    // overriding the describeContents method of the Parcelable interface

    @Override
    public int describeContents() {
        return 0;
    }

    // overriding the writeToParcel method of the Parcelable interface to write object to parcel
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nID);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeLong(dob);
        dest.writeString(fatherName);
        dest.writeString(motherName);
        dest.writeByte((byte) (hasVoted ? 0x01 : 0x00));
        dest.writeByte((byte) (isVoter ? 0x01 : 0x00));
        dest.writeByte((byte) (isAdmin ? 0x01 : 0x00));
        dest.writeByte((byte) (isSAdmin ? 0x01 : 0x00));
    }

    // method to create creator object which helps in creating the parcel object upon transfer
    
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}