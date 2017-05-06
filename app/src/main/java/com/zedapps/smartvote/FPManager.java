/*************************************************************
 * FPManager Class
 * Handles the connectivity between Arduino and Android device.
 * Mainly features the methods associated with the fingerprint scanner,
 * with one being the exception that refers to the Thermal Printer.
 * Uses the Physicaloid Library by ksksue at GitHub.
 * A part of the SmartVote application, developed by ZedApps.
 * 
 * @author Shamah M Zoha
 * @email shamah1992@gmail.com
 * @github bitbucket.org/smzoha/
 ************************************************************/

package com.zedapps.smartvote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.ReadLisener;

public class FPManager {
	static final String ACTION_USB_PERMISSION = "com.zedapps.ACTION_USB_PERM"; // constant value for usb permission
	static final String app_data_loc = Environment.getDataDirectory() +        // constant value containing the database location
			"/data/com.zedapps.smartvote/databases/";
	
	Context appContext; // the application context
	
	UsbManager usbManager; // the UsbManager object
	UsbDevice targetDevice; // the UsbDevice object that refers to the Arduino
	UsbDeviceConnection usbDeviceConn; // the UsbDeviceConnection that connects to the Arduino
	
	Physicaloid serialDevice; // instance of the Physicaloid library that allows serial communication
	
	// constants to send command to the fingerprint scanner
	
	private final String ENROLL_CMD = "!";
	private final String GT_CMD = "#";
	private final String SCAN_CMD = "$";
	private final String ST_CMD = "%";
	private final String VERIFY_CMD = "&";
	
	// constants to print using the thermal printer
	
	private final String PRINT_BALLOT = Character.toString((char)0x6A);
	
	// constants defining the acknowledgement and the error bytes
	
	private final String ACK_MSG = "*";
	private final String ERR_MSG = "+";
	
	// flags that help in fingerprint process
	
	private boolean deviceExists = false;
	private boolean eStart, e1, e2, e3;
	private boolean enrollStatus, enrollError, gtStatus, scanStatus, stStatus;
	private int verifyStatus = Integer.MIN_VALUE; // max value - fail, min value - idle
	
	// the string object that contains the command of the current activity
	
	private String currActivity = "";
	
	// constructor that takes in the application context and assigns it to the local variable.
	// the Physicaloid object is created using the context object and the UsbManager object
	// is instantiated as well.
	
	public FPManager(Context context) {
		appContext = context;
		serialDevice = new Physicaloid(appContext);
		usbManager = (UsbManager) appContext.getSystemService(Context.USB_SERVICE);
	}
	
	// method to check if the Arduino is connected to the Android device.
	// returns boolean value depending on the status.
	
	public boolean fpDeviceExists() {
		return deviceExists;
	}
	
	// method to open serial connection with the Arduino.
	// if the connection is closed, only then is the connection opened.
	// the baud rate is set to 9600 and the fpDevRead method is called to attach
	// the readListener to the connection - to receive data from Arduino.
	
	public void fpOpen() {
		if(!serialDevice.isOpened()) {
			serialDevice.setBaudrate(9600);
			serialDevice.open();
			fpDevRead();
		}
	}
	
	// method to close serial connection with the Arduino.
	// if the connection is open with the Arduino, it is closed.
	
	public void fpClose() {
		if(serialDevice.isOpened()) {
			serialDevice.close();
		}
	}
	
	// method to obtain USB permission for the Arduino device.
	// initially, a list of connected USB devices is obtained.
	// the list is skimmed through to find the Arduino device. if it exists, the flag is set to true.
	// a pending intent is sent to the broadcast receiver to handle the permission action.
	
	public void fpPerm() {
		HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

		if(!deviceList.isEmpty()) {

			for(Iterator<UsbDevice> i = deviceList.values().iterator(); i.hasNext();) {
				targetDevice = i.next();
				int dVendorID = targetDevice.getVendorId();

				if(dVendorID == 9025) {
					deviceExists = true;
					PendingIntent pIntent = PendingIntent.getBroadcast(appContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
					usbManager.requestPermission(targetDevice, pIntent);
				} else {
					usbDeviceConn = null;
					targetDevice = null;
				}
			}
		}
	}
	
	// method to enroll fingerprint.
	// the currActivity variable is set to the ENROLL_CMD, before the command is passed to the Arduino.
	
	public void fpEnroll() {
		currActivity = ENROLL_CMD;
		fpDevWrite(ENROLL_CMD.getBytes());
	}
	
	// method to obtain the enrolled template.
	// the currActivity variable is set to the GT_CMD, before the flag is set to true (to indicate first run) 
	// and the command is passed to the Arduino.
	
	public void fpGetTemplate() {
		currActivity = GT_CMD;
		gtStatus = true;
		fpDevWrite(GT_CMD.getBytes());
	}
	
	// method to scan live fingerprint image.
	// the currActivity variable is set to the SCAN_CMD, before the command is passed to the Arduino.
	
	public void fpScanVoter() {
		currActivity = SCAN_CMD;
		fpDevWrite(SCAN_CMD.getBytes());
	}
	
	// method to set binary template to the fingerprint scanner.
	// the template file is provided, as well as the id value - the slot to store the file in.
	// the currActivity variable is set to the ST_CMD, before the command is passed to the Arduino.
	// the template file data is then converted to a byte array, before it is sent over a buffer of size 10.
	// once the transfer is complete, the id is sent to the scanner to complete the process.
	
	public void fpSetTemplate(File templateFile, int id) {
		if(templateFile.exists()) {	
			currActivity = ST_CMD;
			fpDevWrite(ST_CMD.getBytes());
			byte[] fpData = convertToByteArray(templateFile);
			
			int bCount = 0;
			while(bCount < fpData.length) {
				if((fpData.length - bCount) < 10) {
					int size = fpData.length - bCount;
					
					byte [] tmp = new byte[size];
					for(int i = 0; i < size; i++) {
						tmp[i] = fpData[bCount];
						bCount++;
					}
					
					fpDevWrite(tmp);
				} else {
					byte [] tmp = new byte[10];
					for(int i = 0; i < 10; i++) {
						tmp[i] = fpData[bCount];
						bCount++;
					}
					
					fpDevWrite(tmp);
				}
				
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			byte [] fileID = {(byte) id};
			fpDevWrite(fileID);
		}
	}
	
	// method to verify fingerprint - the live impression against the templates in the database.
	// the currActivity variable is set to the VERIFY_CMD, before the command is passed to the Arduino.
	
	public void fpVerify() {
		currActivity = VERIFY_CMD;
		fpDevWrite(VERIFY_CMD.getBytes());
	}
	
	// method to getEnrollStatus flag
	
	public boolean getEnrollStatus() {
		return enrollStatus;
	}
	
	// method to getEnrollError flag
	
	public boolean getEnrollError() {
		return enrollError;
	}
	
	// method to getGTStatus flag
	
	public boolean getGTStatus() {
		return gtStatus;
	}
	
	// method to getSTStatus flag
	
	public boolean getSTStatus() {
		return stStatus;
	}
	
	// method to getScanStatus flag
	
	public boolean getScanStatus() {
		return scanStatus;
	}
	
	// method to getVerifyStatus flag
	
	public int getVerifyStatus() {
		return verifyStatus;
	}
	
	// method to print the physical ballot.
	// the candidate name, the file bearing the symbol, the current system time, and the session id is passed.
	// initially, the PRINT_BALLOT constant is passed to start the process. the currAcitivty variable is used to hold the command.
	// the candidate name is then sent over to the printer, before the symbol file is broken down to a byte array
	// and passed through a buffer of size 10. Once the transfer is complete, the current time and the session id is sent,
	// which completes the whole process.
	
	public void printBallot(String cName, File symFile, String date_time, String randNum) {
		currActivity = PRINT_BALLOT;
		fpDevWrite(PRINT_BALLOT.getBytes());
		byte [] cNameLen = {(byte)cName.length()};
		fpDevWrite(cNameLen);
		fpDevWrite(cName.getBytes());
		
		if(symFile.exists()) {
			byte[] symData = convertToByteArray(symFile);
			
			int bCount = 0;
			while(bCount < symData.length) {
				if((symData.length - bCount) < 10) {
					int size = symData.length - bCount;
					
					byte [] tmp = new byte[size];
					for(int i = 0; i < size; i++) {
						tmp[i] = symData[bCount];
						bCount++;
					}
					
					fpDevWrite(tmp);
				} else {
					byte [] tmp = new byte[10];
					for(int i = 0; i < 10; i++) {
						tmp[i] = symData[bCount];
						bCount++;
					}
					
					fpDevWrite(tmp);
				}
				
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		byte [] DTLen = {(byte)date_time.length()};
		fpDevWrite(DTLen);
		fpDevWrite(date_time.getBytes());
		
		byte [] RNLen = {(byte)randNum.length()};
		fpDevWrite(RNLen);
		fpDevWrite(randNum.getBytes());
	}
	
	// the broadcast receiver which helps in seeking permission to establish connection with the Arduino.
	// the pending intent sends the constant for USB permission to the receiver, which calls the UsbManager
	// object to grant the permission. if the permission is not granted, the application closes.
	
	final BroadcastReceiver br = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(ACTION_USB_PERMISSION)) {
				boolean permStatus = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
				if(!permStatus) {
					toastMsg("Failed to connect. Application will close.");
					System.exit(0);
				}
			}
		}
	};
	
	
	// fpDevRead method which attaches a ReadListener to the Physicaloid serial connection object.
	// this includes an onRead method, which reads response from the serial connection and handles task based
	// on the command sent.
	
	private void fpDevRead() {
		if(serialDevice.isOpened()) {
			serialDevice.addReadListener(new ReadLisener() {
				FileOutputStream fos;

				@Override
				public void onRead(int size) {
					byte [] buffer = new byte[size];
					serialDevice.read(buffer, size);
					
					// if the currActivity holds ENROLL_CMD, it means that enroll has started. multiple flags are
					// used to ensure that all three enroll steps are completed. if there is an error, a message is displayed.
					
					if(currActivity.equals(ENROLL_CMD)) {
						if(buffer[0] == ACK_MSG.getBytes()[0]) {
							if(!eStart) {
								eStart = true;	
							} else if(eStart && !e1) {
								e1 = true;
							} else if(eStart && e1 && !e2) {
								e2 = true;
							} else if(eStart && e1 && e2 && !e3) {
								eStart = e1 = e2 = e3 = false;
								enrollStatus = true;
							}
						} else if (buffer[0] == ERR_MSG.getBytes()[0]) {
							toastMsg("Error in Enrollment!");
							enrollError = true;
						}
					}
					
					// if the currActivity variable holds GT_CMD, it means that the Android device is to receive template
					// file from the scanner. a temporary file is created in the database directory, before a FileOutputStream
					// object is instantiated to write the data received from the scanner.
					
					if(currActivity.equals(GT_CMD)) {
						
						if(buffer[0] == ERR_MSG.getBytes()[0]) {
							toastMsg("Something went wrong. Try again.");
							currActivity = "";
						} else {
							File fpDatFile = new File(app_data_loc + "tmp.dat");
							
							if(gtStatus) {
								if(fpDatFile.exists()) {
									fpDatFile.delete();
									
									if(fos != null) {
										try {
											fos.close();
										} catch (IOException e) {
											e.printStackTrace();
										}
										fos = null;
									}
								}
								
								gtStatus = false;
							}
							
							
							if(fos == null) {
								try {
									fos = new FileOutputStream(fpDatFile);
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								} 
							}
							
							try {
								fos.write(buffer);
								fos.flush();
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					
					// if the currActivity holds SCAN_CMD, it means that the scanner is intended to scan the live fingerprint
					// impression and store it in the Arduino's buffer. if an acknowledgement message is received, the process
					// is a success. if the message received is an error, the scanning process has failed.
					
					if(currActivity.equals(SCAN_CMD)){
						if(buffer[0] == ACK_MSG.getBytes()[0]) {
							scanStatus = true;
						} else if (buffer[0] == ERR_MSG.getBytes()[0]) {
							scanStatus = false;
						}
					}
					
					// if the currActivity holds ST_CMD, it means that a previously enrolled template file has been sent to the 
					// scanner for verification. if an acknowledgement message is received, the process was a success. 
					// if the message received is an error, the set template process has failed.
					
					if(currActivity.equals(ST_CMD)) {
						if(buffer[0] == ACK_MSG.getBytes()[0]) {
							stStatus = true;
						} else if(buffer[0] == ERR_MSG.getBytes()[0]) {
							stStatus = false;
						}
					}
					
					
					// if the currActivity holds VERIFY_CMD, it means that a request for verification is sent to the scanner.
					// if the first byte received is an acknowledgement byte, the next byte is checked for the id for which
					// the live fingerprint impression has matched, before it is stored in the verifyStatus flag. if the
					// first byte yields an error code, the verifyStatus flag is set to the maximum value of integer, to
					// indicate that there was no match.
					
					if(currActivity.equals(VERIFY_CMD)){
						byte [] statusByte = {buffer[0]};
						
						if(statusByte[0] == ACK_MSG.getBytes()[0]) {
							verifyStatus = (int)buffer[1];
						} else if (statusByte[0] == ERR_MSG.getBytes()[0]) {
							verifyStatus = Integer.MAX_VALUE;
						}
					}
				}
			});
		} else {
			toastMsg("Device is not connected to Arduino.");
		}
	}
	
	// fpDevWrite method takes in a byte array and writes it over the serial connection with the help of the Physicaloid
	// object.
	
	public void fpDevWrite(byte [] data) {
		serialDevice.write(data);
	}
	
	
	// the convertToByteArray helps in creating a byte array from an file object that is provided to it.
	// it utilizes a file input stream to read the byte and append them to an array that is returned at the end of the process.
	
	private FileInputStream fis;
	public byte[] convertToByteArray(File f) {
		byte [] data = null;
		
		try {
			fis = new FileInputStream(f);
			data = new byte[(int)f.length()];
			
			fis.read(data, 0, data.length);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return data;
	}
	
	// a simple handler thread which displays toast messages based on the string passed to the inner method that it holds
	
	Handler mHander = new Handler();
	public void toastMsg(String str) {
		final String appendStr = str;

		mHander.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(appContext, appendStr, Toast.LENGTH_SHORT).show();
			}
		});
	}
}
