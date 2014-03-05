#include <jni.h>
#include "tum_andrive_Andrive.h"
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/objdetect/objdetect.hpp>
#include <vector>

using namespace std;
using namespace cv;

static CascadeClassifier classifier;

/**
 *  This function loads the xml file for the classifier.
 *  @param xmlPath is the path for the file given from the java method.
 */
JNIEXPORT void JNICALL Java_tum_andrive_Andrive_loadClassifierXml
  (JNIEnv *env, jobject obj, jstring xmlPath)
{
    // Convert the JNI string to a C-string
    const char *path = env -> GetStringUTFChars(xmlPath, NULL);
    if (path == NULL) return;

    // Load the cascade
    classifier.load(path);

    // Release string resource
    env -> ReleaseStringUTFChars(xmlPath, path);
}

JNIEXPORT void JNICALL Java_tum_andrive_Andrive_nativeThreshold
  (JNIEnv *env, jobject obj, jlong in, jlong out)
{
    Mat& frame = *(Mat*)in;
    Mat gray_scale = Mat(frame.rows, frame.cols, CV_8UC4);
    Mat& output = *(Mat*)out;
    cvtColor(frame, gray_scale, CV_RGB2GRAY);
    frame.copyTo(output);
    // threshold(gray_scale, output, 128, 255, 4);
}

JNIEXPORT void JNICALL Java_tum_andrive_Andrive_faceDetect
  (JNIEnv *env, jobject obj, jlong in, jlong out)
{
    Mat& frame = *(Mat*)in;
    Mat gray_scale = Mat(frame.rows, frame.cols, CV_8UC4);
    Mat& output = *(Mat*)out;
    cvtColor(frame, gray_scale, CV_RGB2GRAY);
    threshold(gray_scale, output, 128, 255, 4);
}
