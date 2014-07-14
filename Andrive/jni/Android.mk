LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#OpenCv
OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on

include $(OPENCVSDK)/native/jni/OpenCV.mk

LOCAL_MODULE    := AndriveNative
LOCAL_SRC_FILES := detector_native_source.cpp
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)
