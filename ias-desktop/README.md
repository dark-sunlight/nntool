## Building ##

### Prerequisites ###
* Node.js >= 10
* npm >= 6
* uglify-es >= 3
* Linux or macOS (any Version, required for uglify)

### Build ###
1. Build *ias*, see instructions in *ias/README.md*
2. In dir *ias-desktop*, run *npm i* to install dependencies
3. Put Public keys of TLS-certificates which should be connected to in *certificates/*
3.1 If TLS-certificate check should be deactivated, modify function verifyCertificate() in main.js according to the Code-Documentation

There are two build options available:
1. For uglifying, run *npm run build* on Linux or macOS with fullfilled prerequisites. The output in *build/* will be usable in other electron projects.
2. For creating an executable, run *npm run dist:{mac|linux|win}* with fullfilled prerequisites. The executable will be build in os-specific formats (see *package.json*) and placed in *dist/*. Currently, there is *no* cross-compiling available, i.e., every :dist has to be build on the target OS.

---------------

### Demo Parameters ###

Modify *src/web_desktop/index.html* according to Code-Documentation to edit measurement parameters before *Build*

---------------

### Demo Execution ###
There are three execution options available:
1. Run *npm run electron:{mac|linux|win}* with fullfilled prerequisites without building. The debug UI will be shown.
2. After building, run the build executable. The default UI will be shown.
3. After building, run the build executable from command-line with the argument *--cli*. The default measurement configuration will be used. there are several cli options available:
	- *--debug* debug UI will be shown
	- *--verbose* verbose measurement output on cli
	- *--rtt={true|false}* perform rtt measurement
	- *--download={true|false}* perform download measurement
	- *--upload={true|false}* perform upload measurement

To perform rtt, download, or upload measurements, the *ias-server* module has to be deployed on the measurement peer.

---------------

## License ##

ias-desktop is released under the AGPLv3 <https://www.gnu.org/licenses/agpl-3.0.txt>

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