## Building ##

### Prerequisites ###
* XCode >= 10
* cocoapods
* NativeScript = 5.x
* macOS (any Version)

### Build ###
With fullfilled prerequisites perform the following steps:
1. Run *npm i* to install dependencies
2. Run *tns build ios*. If *tns build ios* fails, check if the following files have the executable bit set:
* ias-mobile/platforms/ios/internal/nativescript-post-build
* ias-mobile/platforms/ios/internal/nativescript-pre-build
* ias-mobile/platforms/ios/internal/nativescript-pre-link
* ias-mobile/platforms/ios/internal/strip-dynamic-framework-architectures.sh
* ias-mobile/platforms/ios/internal/metadata-generator/bin/build-step-metadata-generator.py
* ias-mobile/platforms/ios/internal/metadata-generator/bin/objc-metadata-generator

---------------

## License ##

ias-mobile is released under the AGPLv3 <https://www.gnu.org/licenses/agpl-3.0.txt>

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