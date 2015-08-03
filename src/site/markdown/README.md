# RundeckMonitor

[![Build Status](https://travis-ci.org/Sylvain-Bugat/RundeckMonitor.svg?branch=master)](https://travis-ci.org/Sylvain-Bugat/RundeckMonitor)

Rundeck Monitor is a system tray icon tools that indicates failed and long execution on a Rundeck instance.

![RundeckMonitor screenshot](https://raw.githubusercontent.com/Sylvain-Bugat/RundeckMonitor/master/Screenshot.png)

***

## Installation

Download the latest jar release at this URL: https://github.com/Sylvain-Bugat/RundeckMonitor/releases/latest

## Launch and configuration

Just execute the downloaded jar file: `rundeck-monitor-2.0.jar`.

And if no configuration file is found, a basic configuration wizard is launched.

After configuration, the tray icon can be in these colors:

| color | description |
| ---------- | ---------- |
| ![RundeckMonitor OK](https://raw.githubusercontent.com/Sylvain-Bugat/RundeckMonitor/master/src/main/resources/OK.png) | no failed jobs has failed since the launch or the last alert reset and no long execution is detected |
| ![RundeckMonitor KO](https://raw.githubusercontent.com/Sylvain-Bugat/RundeckMonitor/master/src/main/resources/KO.png) | a new failed job is detected since the launch or the last alert reset |
| ![RundeckMonitor late](https://raw.githubusercontent.com/Sylvain-Bugat/RundeckMonitor/master/src/main/resources/LATE.png) | a job is running for too long |
| ![RundeckMonitor KO and late](https://raw.githubusercontent.com/Sylvain-Bugat/RundeckMonitor/master/src/main/resources/KO_LATE.png) | a new failed job is detected since the launch or the last alert reset and another job is running for too long |
| ![RundeckMonitor disconnected](https://raw.githubusercontent.com/Sylvain-Bugat/RundeckMonitor/master/src/main/resources/DISCONNECTED.png) | the connection with Rundeck is lost |

When the tray icon is marqued with red, this alert can be reset to get back to green/black.

Failed and long  jobs list can be clicked in order to see the Rundeck execution detail. This is done by opening a default browser tab/window with the execution URL.

***

## Manual/custom configuration

Edit the configuration file creatd by the wizard or copy and edit the sample configuration file `rundeckMonitor.properties` from the master `target\` directory and change these parameters:  

### Rundeck project parameters

	rundeck.monitor.url=
	
URL with the http/https protocol and with only the domain and the port like: `http://rundeck.domain.com:4440`

	rundeck.monitor.project=
	
Rundeck project containing jobs to scan

### Authentication parameters

	rundeck.monitor.api.key=

Rundeck API key can be used instead of login/password

	rundeck.monitor.login=
	
Login with access to the Rundeck REST API if no API key is defined

	rundeck.monitor.password=
	
Password associated with this login if no API key is defined


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

	rundeck.monitor.api.version=12

Optional Rundeck rest API version to use (minimum version 5)

	rundeck.monitor.job.tab.redirection=SUMMARY
	
Default opened page when opening a failed/late job. Possible values: SUMMARY or DOWNLOAD_OUTPUT and if version is at least 2.0 RENDER_OUTPUT_TXT or RENDER_OUTPUT_HTML can be used.

	rundeck.monitor.disable.version.checker=false

Enable or disable the automatic version checker (true to disable)

	rundeck.monitor.interface.type=SWING

Java interface type to use: SWING for Windows or AWT for other OS

***

## Compile and build

**The minimum required version maven is 3.0.1**

Clone the master repository with this command:

	git clone https://github.com/Sylvain-Bugat/RundeckMonitor.git

Build target jars with this command:

	mvn clean install

