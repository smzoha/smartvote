/*************************************************************
 * DatabaseManager Class
 * Bridges the application with the database containing the user and the entity information.
 * A part of the SmartVote application, developed by ZedApps.
 * 
 * @author Shamah M Zoha
 * @email shamah1992@gmail.com
 * @github bitbucket.org/smzoha/
 ************************************************************/

package com.zedapps.smartvote;

import java.sql.SQLException;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.zedapps.smartvote.modules.Entity;
import com.zedapps.smartvote.modules.User;

public class DatabaseManager extends SQLiteOpenHelper {

	// initializing constants which contains the location of the database file
	// and the database file name.
	
	private static String db_loc = Environment.getDataDirectory().getAbsolutePath() +
			"/data/com.zedapps.smartvote/databases/";
	private static String db_name = "svdatabase.db";
	
	// creating the SQLiteDatabase object, as well as a Context object to hold
	// the application context passed to it.
	
	SQLiteDatabase svdb;
	Context appContext;
	
	// constructor with application context passed in. the super constructor is called
	// to create the object, with the database file name being provided.
	
	public DatabaseManager(Context context) {
		super(context, db_name, null, 1);
		appContext = context;
	}

	// the override methods are kept empty, since the application do not need database
	// to be created when the file does not exist, or is updated.
	
	@Override
	public void onCreate(SQLiteDatabase db) {}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
	
	// the connection with the database file is opened. the file name, along with
	// the directory, is provided, along with the constant that allows for read/
	// write access.
	
	public void openDatabase() throws SQLException {
		svdb = SQLiteDatabase.openDatabase(db_loc+db_name, null, 
				SQLiteDatabase.OPEN_READWRITE);
	}
	
	// method to obtain information of all the user stored in the database.
	// a temporary object for the database is created, which is used to store
	// a copy of the database, to ensure that the original object is not
	// overwritten. next, an array list is created to hold User objects.
	// the query is then executed and the data is stored in a Cursor object.
	// this Cursor object is used to retrieve data from the result of the query,
	// and the information is stored in the list. finally, the list is returned.
	// finally, the temporary database object is closed to avoid memory leakage.
	
	public ArrayList<User> getAllUser() {
		SQLiteDatabase tmpDB = this.getWritableDatabase();
		ArrayList <User> allUsers = new ArrayList<User>();
		Cursor data;
		
		try {
			data = tmpDB.rawQuery("SELECT * FROM user;", null);
			if(data == null) {
				return null;
			}
			
			data.moveToFirst();
			
			while(!data.isAfterLast()) {
				allUsers.add(new User(data.getString(0),
						data.getString(1),
						data.getString(2),
						data.getString(3),
						data.getString(4),
						data.getLong(5),
						data.getString(6),
						data.getString(7),
						data.getInt(8),
						data.getInt(9),
						data.getInt(10),
						data.getInt(11)));
				data.moveToNext();
			}
			
			data.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		tmpDB.close();
		
		return allUsers;
	}
	
	
	// method to obtain information of one specific user stored in the database.
	// a temporary object for the database is created, which is used to store
	// a copy of the database, to ensure that the original object is not
	// overwritten. next, a single User object is created to hold User data.
	// the query is then executed and the data is stored in a Cursor object.
	// the Cursor object is then checked to find if it holds a single information
	// or not. if there are multiple data, a 'null' object is returned. otherwise,
	// the valid user information is passed as the User object.
	// finally, the temporary database object is closed to avoid memory leakage.
	
	public User getSpecificUser(String NID_VAL) {
		SQLiteDatabase tmpDB = this.getWritableDatabase();
		Cursor data;
		User contextUser = null;
		
		try {
			data = tmpDB.rawQuery("SELECT * FROM user WHERE n_id = ?;", new String[]{NID_VAL});
			if(data == null) {
				return null;
			}
			
			data.moveToFirst();
			
			if(data.getCount() == 1) {
				contextUser = new User(data.getString(0),
						data.getString(1),
						data.getString(2),
						data.getString(3),
						data.getString(4),
						data.getLong(5),
						data.getString(6),
						data.getString(7),
						data.getInt(8),
						data.getInt(9),
						data.getInt(10),
						data.getInt(11));
			} else {
				return null;
			}
			
			data.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		tmpDB.close();
		
		return contextUser;
	}
	
	// method to insert voter information in the database.
	// a string array with all the data of the tuple is passed in.
	// a temporary database is created to hold the copy of the original object.
	// a ContentValues object is created to hold the information passed,
	// before an insert operation is performed. the status flags are
	// hard-coded to ensure that the data of the user inserted is a voter.
	// finally, the temporary database object is closed to avoid memory leakage.
	
	public void insertVoter(String [] data) {
		SQLiteDatabase tmpDB = this.getWritableDatabase();
		
		ContentValues voterValues = new ContentValues();
		voterValues.put("n_ID", data[0]);
		voterValues.put("first_name", data[1]);
		voterValues.put("middle_name", data[2]);
		voterValues.put("last_name", data[3]);
		voterValues.put("address", data[4]);
		voterValues.put("dob", Long.parseLong(data[5]));
		voterValues.put("father_name", data[6]);
		voterValues.put("mother_name", data[7]);
		voterValues.put("hasVoted", 0);
		voterValues.put("isVoter", 1);
		voterValues.put("isAdmin", 0);
		voterValues.put("isSAdmin", 0);
		
		tmpDB.insert("user", null, voterValues);
		tmpDB.close();
	}
	
	
	// method to delete a tuple of data from the user table.
	// the NID value is passed to execute the query.
	// based on the NID value, the data of the user is removed from the database.
	
	public void deleteSpecificUser(String NID_VAL) {
		SQLiteDatabase tmpDB = this.getWritableDatabase();
		tmpDB.delete("user", "n_id = " + NID_VAL, null);
		tmpDB.close();
	}
	
	// method to set hasVoted flag to true for a specific user.
	// the NID value is passed to execute the query.
	// based on the NID value, the flag is set to 1.
	
	public void sethasVotedForUser(String NID_VAL) {
		SQLiteDatabase tmpDB = this.getWritableDatabase();
		tmpDB.execSQL("UPDATE user SET hasVoted = 1 WHERE n_id = "+NID_VAL+";");
		tmpDB.close();
	}
	
		// method to obtain information of all the entity stored in the database.
		// a temporary object for the database is created, which is used to store
		// a copy of the database, to ensure that the original object is not
		// overwritten. next, an array list is created to hold Entity objects.
		// the query is then executed and the data is stored in a Cursor object.
		// this Cursor object is used to retrieve data from the result of the query,
		// and the information is stored in the list. finally, the list is returned.
		// finally, the temporary database object is closed to avoid memory leakage.
	
	public ArrayList<Entity> getAllEntity() {
		SQLiteDatabase tmpDB = this.getWritableDatabase();
		ArrayList <Entity> allEntity = new ArrayList<Entity>();
		Cursor data;
		
		try {
			data = tmpDB.rawQuery("SELECT * FROM entity;", null);
			if(data == null) {
				return null;
			}
			
			data.moveToFirst();
			
			while(!data.isAfterLast()) {
				allEntity.add(new Entity(data.getInt(0),
						data.getString(1),
						data.getString(2),
						db_loc + "eImages/" + data.getString(3),
						data.getInt(4)));
				data.moveToNext();
			}
			
			data.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		tmpDB.close();
		
		return allEntity;
	}
	
	// method to increment the vote_count field of a specific entity.
	// the entity is defined by the id passed, and the previous count is
	// passed into the method. the query is executed to add '1' to the 
	// existing count. used after the vote has been casted.
	
	public void incrementVoteCount(int id, int vCount) {
		SQLiteDatabase tmpDB = this.getWritableDatabase();
		tmpDB.execSQL("UPDATE entity SET vote_count = " + (vCount+1) 
				+ " WHERE e_id =" + id + ";" );
		tmpDB.close();
	}
	
	
	// a synchronized method that closes the database at the end of operation.
	
	public synchronized void close() {
		if(svdb != null) {
			svdb.close();
		}
		super.close();
	}
	
}
