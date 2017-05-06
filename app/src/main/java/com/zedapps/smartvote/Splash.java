/*************************************************************
 * Splash Class
 * The back-end implementation of the activity represented by the Splash.xml file.
 * Provides the user with a splash screen bearing the application name, behind which the pre-processing is done.
 * A part of the SmartVote application, developed by ZedApps.
 * 
 * @author Shamah M Zoha
 * @email shamah1992@gmail.com
 * @github bitbucket.org/smzoha/
 ************************************************************/

package com.zedapps.smartvote;

import com.zedapps.smartvote.modules.VoteManager;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class Splash extends Activity {
	
	SVMain svmainObj;     // instance of SVMain class, which houses all the modules
	VoteManager vManager; // instance of the VoteManager class
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // the VoteManager object is instantiated on the first run
        
        vManager = new VoteManager();
        
        // instantiate object of the SVMain class and bind the vManager object to it
        
        svmainObj = new SVMain(getApplicationContext());
        svmainObj.setVManager(vManager);
        
        // register broadcast receiver to seek permission for USB connectivity from user
        
        registerReceiver(svmainObj.fpManager.br, 
        		new IntentFilter(FPManager.ACTION_USB_PERMISSION));
  
        // call method to request permission
        
        svmainObj.fpPerm();
        
        // start a thread to hold the splash screen for 2 seconds
        
        Thread start = new Thread() {
        	public void run() {
    	    	try {
    				Thread.sleep(2000);
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			} finally {
    				
    				// if the Arduino is not connected to the device, the application will close
    				
    				if(!svmainObj.deviceExists()) {
    					System.exit(0);
    				}
    				
    				// if the database file and the other additional data files are not available, the user is
    				// redirected to the LoadData activity, to ensure the load of the database and files. the vManager
    				// object is passed to maintain concurrency and the broadcast receiver is unregistered for smooth transition.
    				
    		        if(!svmainObj.checkFiles()) {
    		        	unregisterReceiver(svmainObj.fpManager.br);
    		        	svmainObj.fpClose();
    		        	Intent toLoadData = new Intent(Splash.this, LoadData.class);
    		        	toLoadData.putExtra("vManager", vManager);
    		        	startActivity(toLoadData);
    		        	finish();
    		        	
    		        	// if the database file and the other additional data files are available, the user is taken to the
    		        	// Intro activity, in which the instructions are provided. the vManager object is passed to maintain 
    		        	// concurrency and the broadcast receiver is unregistered for smooth transition.
    		        	
    		        } else {
    		        	unregisterReceiver(svmainObj.fpManager.br);
    		        	svmainObj.fpClose();
    			        Intent toIntro = new Intent(Splash.this, Intro.class);
    			        toIntro.putExtra("vManager", vManager);
    			        startActivity(toIntro);
    			        finish();
    		        }
    			}
        	}
        };
        
        // start the thread instantiated and designed above
        
        start.start();
    }
}
