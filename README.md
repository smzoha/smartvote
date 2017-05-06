![alt text](app/src/main/res/drawable-xxhdpi/ic_launcher.png "SmartVote Logo") 
# SmartVote

An Android application that is developed as a part of the SVS (Smart Voting System) project. The Android application is connected with an Arduino Mega 2560, which, in turn, is connected to other peripherals, such as the fingerprint scanner and the thermal printer. The application offers three levels of user access - voter, administrators and super administrators, with the last having complete control over the system. The voters can only cast their vote, while the administrators have privilege of enabling or disabling the voting process, as well as generate reports based on the vote casted.

The following are the dependecies (libraries) that are required to compile and run the application. The .jar files that could not be referenced through a Gradle repository is included in the lib folder of the project.

* **Phyiscaloid Library** by *ksksue*, which was used to establish the serial communication with Arduino. The project and related documentations can be found [here](http://github.com/ksksue/PhysicaloidLibrary).

The following dependencies were referenced in build.gradle and should be fetched upon build.

* **Zip4J Library** by *Srikanth Reddy Lingala*, which was used for loading the encrypted zipped database from the internal storage. The project and related documentations can be found [here](http://www.lingala.net/zip4j/).
* **Apache Commons IO** by *Apache*, which was used for file mainpulation, that is, moving, copying, deleting and renaming of files. The library and the related documentations can be found [here](https://commons.apache.org/proper/commons-io/).

The dependencies (libraries) listed above are licensed under Apache License, version 2.0, which can be found [here](http://www.apache.org/licenses/LICENSE-2.0).

The application itself, however, is licensed under GNU General Public License, version 3.0. A copy of the license can be found in the repository itself and [here](https://www.gnu.org/licenses/gpl-3.0-standalone.html).

The application was coded in Eclipse, later migrated to Android Studio to enable Gradle building.

![alt text](https://www.gnu.org/graphics/gplv3-127x51.png "GPL License V3.0")