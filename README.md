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

Before installing the application, make sure to have following components:

- [Android SDK](https://developer.android.com/sdk/index.html)
- [Android NDK](https://developer.android.com/tools/sdk/ndk/index.html)
- [OpenCV for Android](http://opencv.org/downloads.html)

To start, it is required to set the variables **OPENCV_SDK_ROOT** and
**OPENCVSDK**. The first variable is used as a project property. Proceed to
create a file called *local.properties* in the project directory and set the 
variable with the path to your installation of the OpenCV SDK directiry. For 
example:

```
OPENCV_SDK_ROOT=../../OpenCV-2.4.10-android-sdk/sdk
```

The second variable is a environment variable and in case of using Linux can be
set trough a console as follows:

```bash
$ export OPENCVSDK=/path/to/OpenCV-2.4.10-android-sdk/sdk
```

After setting the two variable, the next step to build the
native code using the *ndk-build* executable in the NDK installation.

```bash
$ /path/to/ndk-build
```

Then build the application using the *ant* command in the project directory

```bash
$ ant debug
```

Instead of debug, it can also be build as a *release*. Finally, install the
application on your device with the *adb* command:

```bash
$ adb install -r ./bin/Andrive-debug.apk
```

**NOTE**: Any application using the OpenCV library requires you to install also
on the device the [Android OpenCV Manager](http://docs.opencv.org/platforms/android/service/doc/index.html).
