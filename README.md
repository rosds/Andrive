Andrive
=======

![alt tag](https://raw.githubusercontent.com/alfonsoros88/Andrive/master/Andrive/doc/imgs/ic_launcher.png)

Andrive is an application to measure the Time to Headway (THW) for driving
evaluation. It was develop under the supervision of [Michael Krause](http://www.ergonomie.tum.de/author/krause/) 
from the Institute of Ergonomics at the [Technische Universität Müenchen (TUM)](http://www.tum.de/).

This application is mainly implemented using the 
[OpenCV library](http://opencv.org). The current version performs
- Rear view vehicle detection.
- Distance calculation using classification region size.
- Speed calculation using GPS.
- THW calculation.


Installation
------------

Before installing the application, please make sure to have previously
installed the following components:

    * [Android SDK](https://developer.android.com/sdk/index.html)
    * [Android NDK](https://developer.android.com/tools/sdk/ndk/index.html)
    * [OpenCV for Android](http://opencv.org/downloads.html)

Once installed it is required to set two variables, **OPENCV_SDK_ROOT** and
**OPENCVSDK**. The first variable is used as a project property. Proceed to
create a file called *local.properties* in the project directory and set the 
variable with the path to your installation of the OpenCV SDK directiry. For 
example:

'''
OPENCV_SDK_ROOT=../../OpenCV-2.4.10-android-sdk/sdk
'''

The second variable is a environment variable and in case of using Linux can be
set for example as follows:

'''bash
$ export OPENCVSDK=/path/to/OpenCV-2.4.10-android-sdk/sdk
'''

After setting the two variable, the next step would be to build the
native code using the *ndk-build* executable included in the NDK installation.

'''bash
$ /path/to/ndk-buld
'''

After the native code is build, the application can be finally build using the
*ant* command in the main directory as:

'''bash
$ ant debug
'''

Instead of debug, it can also be build as a *release*. Finally, install the
application on your device with the *adb* command:

'''bash
$ adb install -r ./bin/Andrive-debug.apk
'''

done :D
