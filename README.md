# RundeckMonitor

System tray icon tools that indicates failed rundeck jobs and jobs running for a too long time.

![RundeckMonitor screenshot](Screenshot.png)

***

## Installation

Download the master repository at this URL: https://github.com/Sylvain-Bugat/RundeckMonitor/archive/master.zip

Or clone the master repository with this command:

	git clone https://github.com/Sylvain-Bugat/RundeckMonitor.git

## Configuration

Edit the sample configuration file `rundeckMonitor.properties` in the `target\` directory and configure these parameters:  

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
	
Delay between 2 scans of failed jobs(unit: seconds)

	rundeck.monitor.execution.late.threshold=1800
	
Delay after a running jobs is flagged as late(unit: seconds)

	rundeck.monitor.failed.job.number=10
	
Number of failed jobs to see in the popup menu

	rundeck.monitor.date.format=dd/MM/yyyy HH:mm:ss
	
Date format of the failed jobs in the popup menu

	rundeck.monitor.api.version=10

Optional rundeck rest API version to use (11 cannot be used yet because of https://github.com/rundeck/rundeck-api-java-client/pull/12)

***

## Launch RundeckMonitor

Just execute this jar file: `target/RundeckMonitor-1.2-SNAPSHOT-jar-with-dependencies.jar`.

The tray icon can be in these colors:

| color | description |
| ---------- | ---------- |
| ![RundeckMonitor OK](src/main/resources/OK.png) | no failed jobs has failed since the launch or the last alert reset and no long execution is detected |
| ![RundeckMonitor KO](src/main/resources/KO.png) | a new failed job is detected since the launch or the last alert reset |
| ![RundeckMonitor late](src/main/resources/LATE.png) | a job is running for too long |
| ![RundeckMonitor KO and late](src/main/resources/KO_LATE.png) | a new failed job is detected since the launch or the last alert reset and another job is running for too long |
| ![RundeckMonitor disconnected](src/main/resources/DISCONNECTED.png) | the connection with rundeck is lost |

When the tray icon is marqued with red, this alert can be reset to get back to green/black.

Failed and long  jobs list can be clicked in order to see the rundeck execution detail. This is done by opening a default browser tab/window with the execution URL.
