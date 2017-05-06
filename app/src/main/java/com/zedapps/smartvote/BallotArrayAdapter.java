/*************************************************************
 * BallotArrayAdapter Class
 * An extension to the ArrayAdapter class which allows the ListView in the EBallot activity
 * to display entity image, as well as the name of the candidate.
 * A part of the SmartVote application, developed by ZedApps.
 * 
 * @author Shamah M Zoha
 * @email shamah1992@gmail.com
 * @github bitbucket.org/smzoha/
 ************************************************************/

package com.zedapps.smartvote;

import com.zedapps.smartvote.modules.Candidate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BallotArrayAdapter extends ArrayAdapter<Candidate>{
	
	Context appContext; // the context of the application
	int layoutResourceID; // the layout resource ID obtained
	Candidate[] candidateArray; // an array of candidate objects, holding the candidate name
								// and entity symbol path
	
	
	// constructor that assigns the values provided into the variables
	
	public BallotArrayAdapter(Context context, int rID, Candidate[] cArr) {
		super(context, rID, cArr);
		appContext = context;
		layoutResourceID = rID;
		candidateArray = cArr;
	}
	
	
	// overriding the getView method of the ArrayAdapter class, which returns a View object
	// obtains the LayoutInflator object from the Context object, before assigning the
	// "ballot_layout.xml" layout to it. the TextView and ImageView objects are obtained
	// from the layout, before the image and the text from the current candidate object is
	// assigned to it. finally, the image size is defined to 150x150 px, to allow for a
	// better viewing in the device screen, before the View object representing the row
	// is returned.
	
	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		LayoutInflater lInflater = (LayoutInflater) appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = lInflater.inflate(R.layout.ballot_layout, parent, false);
		TextView txtCName = (TextView) rowView.findViewById(R.id.cName);
		ImageView imgESym = (ImageView) rowView.findViewById(R.id.eSym);
		
		txtCName.setText(candidateArray[pos].getCandidate_name());
		imgESym.setImageBitmap(candidateArray[pos].getEntity_symbol());
		
		imgESym.getLayoutParams().height=150;
		imgESym.getLayoutParams().width=150;
		
		return rowView;
	}
}
