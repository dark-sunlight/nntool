## Building ##

### Prerequisites ###
* Android Studio >= 3
* Windows, macOS, Linux (any Version)

### Build ###
With fullfilled prerequisites perform the following steps:

1. Open *android-demo* in Android Studio and build target *android-demo*

---------------

### Demo Parameters ###

Modify *src/main/java/com/zafaco/DemoApplicationBerec/fragments/SpeedFragment.java* according to Code-Documentation to edit measurement parameters before *Build*

---------------

### Demo Execution ###
Launch the App *android-demo* in android-simulator or on an android-device and select *Start Test*. Perform another measurement by clicking *Start* again.

---------------

## Port Blocking ##

For the port blocking module a specified set of ports is tested against a defined test-service.

### Settings ###

Available settings are located in the src/main/res/values/defaults.xml file (of the android-demo project).
They are:

* default\_qos\_control\_host: which defines the location of the qos-service to test against
* default\_qos\_control\_port: the port to be used when connecting to that qos-service (which is not the same as the ports to be tested)
* qos\_tcp\_test\_port\_list: which is a list of TCP ports to be tested against during the execution of the port-blocking module
* qos\_udp\_test\_port\_list: which is a list of UDP ports to be tested against during the execution of the port-blocking module
(Note that all UDP ports that shall be successfully tested against need be enabled in the qos-service config.properties file. 
The corresponding setting is "server.udp.ports", which takes a list of ports to be enabled for UDP blocking tests)

### Results ###

The results in the demo-application of the port-blocking module are only displayed as Json array 
(for more information see [the JSON standard](http://www.ecma-international.org/publications/files/ECMA-ST/ECMA-404.pdf)). 
This will not be the case for later versions of the app.

In case of a failed connection to the qos-service (as defined in the corresponding Settings) the result
is a simple text providing information about the currently input configuration (if no echo protocol tests are executed), or 
just the results for the echo protocol tests (if echo-protocol tests are enabled in the defaults.xml).

The test type (e.g: UDP) can be found under the "test\_type" key.
For TCP tests, the port is reachable if the result under "tcp\_result\_out" reads "OK".
For udp results, the port is reachable if the value under "udp\_objective\_out\_num\_packets" (the number of UDP packets expected)
 equals the value under "udp\_result\_out\_num\_packets" (the number of UDP packets actually received).
 UDP tests additionally carry information about the round-trip-time under the "udp\_result\_out\_rtts\_ns" key, 
 wherein the entry after "0" contains the round-trip-time for that UDP packet in nano-seconds. 
 Note that the round-trip-time is not set for blocked ports.

---------------

## Echo Protocol ##

For the echo protocol test a given payload is sent to a specified server and the returned payload is checked for equality with the originally sent one.

### Settings

Available settings are located in the src/main/res/values/defaults.xml file (of the android-demo project).
They are:

* qos\_echo\_service\_host: which defines the location of the echo-service provider to test against
* qos\_echo\_service\_tcp\_ports: which is a list of TCP ports to test the echo service on (Note: the default echo-service port is 7)
* qos\_echo\_service\_udp\_ports: which is a list of UDP ports to test the echo service on (Note: the default echo-service port is 7)

### Results ###

The result of the echo-protocol test is found under the "echo\_protocol\_status" key. It has one of the following 3 values:

* OK: the test succeeded (the payload that was sent to the echo-service host was returned exactly as sent)
* TIMEOUT: the test ran into a timeout during the execution
* ERROR: an error occurred during the echo-protocol test

---------------

## License ##

ias-android-demo is released under the AGPLv3 <https://www.gnu.org/licenses/agpl-3.0.txt>

Copyright (C) 2016-2019 zafaco GmbH

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3 
as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
