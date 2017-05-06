/*************************************************************
 * Candidate Class
 * The class that helps in mapping the values stored in the Entity table of the database.
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

public class Entity implements Parcelable {
	private int eID;		// the id of the entity
	private String eName;	// the name of the entity
	private String cName;	// the name of the candidate
	private String eSym;	// the file name of the entity symbol
	private int vCount;		// the current vote count for the entity
	
	
	// default constructor for the class, which takes in the information and store them in the variables.
	// the values usually come from the result of a query that is run on the database.
	
	public Entity(int eID, String eName, String cName, String eSym, int vCount) {
		this.eID = eID;
		this.eName = eName;
		this.cName = cName;
		this.eSym = eSym;
		this.vCount = vCount;
	}

	/******************************************************
	 * 				GETTER & SETTER METHODS				  *
	 ******************************************************/	
	
    public int geteID() {
		return eID;
	}

	public void seteID(int eID) {
		this.eID = eID;
	}
	
	public String getcName() {
		return cName;
	}

	public void setcName(String cName) {
		this.cName = cName;
	}

	public String geteName() {
		return eName;
	}

	public void seteName(String eName) {
		this.eName = eName;
	}

	public String geteSym() {
		return eSym;
	}

	public void seteSym(String eSym) {
		this.eSym = eSym;
	}

	public int getvCount() {
		return vCount;
	}

	public void setvCount(int vCount) {
		this.vCount = vCount;
	}
	
	/******************************************************
	 * 			END OF GETTER & SETTER METHODS			  *
	 ******************************************************/

	
	// constructor that helps in creating an Entity object from an incoming parcel
	
    protected Entity(Parcel in) {
        eName = in.readString();
        cName = in.readString();
        eSym = in.readString();
        vCount = in.readInt();
    }

    
    // overriding the describeContents method of the Parcelable interface
    
    @Override
    public int describeContents() {
        return 0;
    }

    // overriding the writeToParcel method of the Parcelable interface to write object to parcel    
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(eName);
        dest.writeString(cName);
        dest.writeString(eSym);
        dest.writeInt(vCount);
    }

    // method to create creator object which helps in creating the parcel object upon transfer
    
	public static final Parcelable.Creator<Entity> CREATOR = new Parcelable.Creator<Entity>() {
        @Override
        public Entity createFromParcel(Parcel in) {
            return new Entity(in);
        }

        @Override
        public Entity[] newArray(int size) {
            return new Entity[size];
        }
    };
}