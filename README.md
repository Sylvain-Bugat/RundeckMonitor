# RundeckMonitor

System tray icon tools that indicates only failed rundeck jobs.

![RundeckMonitor screenshot](Screenshot.png)

***

## Installation

To install RundeckMonitor you just have to clone the repository:

	git clone https://github.com/Sylvain-Bugat/RundeckMonitor.git

## Configuration

Edit the sample configuration file `rundeckMonitor.properties` in the 'target\' directory and configure these parameters:  

	rundeck.monitor.url=
	
URL with the http protocol and with only the domain and the port like: `http:\\rundeck.domain.com:4444`

	rundeck.monitor.login=
	
Login with access to the Rundeck REST API

	rundeck.monitor.password=
	
Password associated with this login

	rundeck.monitor.project=
	
Rundeck project containing jobs to scan

## Additionnal Configuration

These parameters in the `rundeckMonitor.properties` file can also be changed:

	rundeck.monitor.name=Rundeck monitor
	
Set the name of the application

	rundeck.monitor.refresh.delay=60
	
Delay between 2 scans of failed jobs

	rundeck.monitor.failed.job.number=10
	
Number of failed jobs to see in the popup menu

	rundeck.monitor.date.format=dd/MM/yyyy HH:mm:ss
	
Date format of the failed jobs in the popup menu

***

## Launch RundeckMonitor

Just execute this jar file: 'target\RundeckMonitor-1.0-SNAPSHOT-jar-with-dependencies.jar'.

The tray icon can be in 3 colors:  
![RundeckMonitor OK](src/main/resources/OK.png) green when no failed jobs has failed since the launch or the last alert reset  
![RundeckMonitor KO](src/main/resources/KO.png) red when a new failed jobs is detected  
![RundeckMonitor disconnected](src/main/resources/DISCONNECTED.png) yellow if the connection with rundeck is lost  

When the tray icon is red, it can be reset to get back to green.

Failed jobs list can be clicked in order to see the rundeck execution detail. This is done by opening a default browser tab/window with the execution URL.
