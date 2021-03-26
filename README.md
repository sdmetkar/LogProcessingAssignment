What does this application do?
------------------------------
This application accepts the path to a log file containing json string on each line , reads data , processes it and saves on HSQL database table EVENTS_TBL .


How to run?
------------------------------

1. Ensure that Java is installed and path environment variable is set to /bin folder of jre
2. Ensure that hsqldb-jdk8.jar is on the classpath
3. Copy LogFileProcessor.jar at folder of your choice. This is the runnable jar for the application.
4. Open command prompt at same location where you copied the LogFileProcessor.jar.
4. To start the database , run following command
java org.hsqldb.server.Server --database.0 file:./credDb --dbname.0 eventsDb

or  pass the value to classpath variable in the command itself

java -classpath replaceWithPathTo_hsqldb-jdk8.jar org.hsqldb.server.Server --database.0 file:./credDb --dbname.0 eventsDb

This will create few files with names starting with credDb in current location from where you ran above command

DO NOT close this command prompt else database will be stopped

4.  Open new command prompt at the same location and run following command to run the application

java -jar LogFileProcessor.jar

5. You will be prompted for path of the file you want to process. Enter the full path of log file.
For ex: C:\Users\sunny\logfile.txt

6. Once the program finishes, you can check the EVENTS_TBL table by connecting to database
using runManagerSwing.bat file in hsqldb\bin .
NOTE: You might need to update the jar file name in bat file to hsqldb-jdk8.jar for it work with jdk8.

