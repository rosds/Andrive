package tum.andrive;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

public class DetectionBasedTracker
{
    public DetectionBasedTracker(String cascadeName, int minFaceSize) {
        mNativeObj = nativeCreateObject(cascadeName, minFaceSize);
    }

    public void start() {
        nativeStart(mNativeObj);
    }

    public void stop() {
        nativeStop(mNativeObj);
    }

    public void setMinDetectionSize(int size) {
        nativeSetDetectionSize(mNativeObj, size);
    }

    public void detect(Mat imageGray, MatOfRect objs) {
        nativeDetect(mNativeObj, imageGray.getNativeObjAddr(), objs.getNativeObjAddr());
    }

    public void release() {
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    private long mNativeObj = 0;

    private static native long nativeCreateObject(String cascadeName, int minFaceSize);
    private static native void nativeDestroyObject(long thiz);
    private static native void nativeStart(long thiz);
    private static native void nativeStop(long thiz);
    private static native void nativeSetDetectionSize(long thiz, int size);
    private static native void nativeDetect(long thiz, long inputImage, long faces);
}
