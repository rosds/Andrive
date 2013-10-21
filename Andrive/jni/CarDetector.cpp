#include <jni.h>
#include "tum_andrive_Andrive.h"
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>

using namespace std;
using namespace cv;

JNIEXPORT void JNICALL Java_tum_andrive_Andrive_nativeThreshold
  (JNIEnv *env, jobject obj, jlong in, jlong out)
{
    Mat& frame = *(Mat*)in;
    Mat gray_scale = Mat(frame.rows, frame.cols, CV_8UC4);
    Mat& output = *(Mat*)out;
    cvtColor(frame, gray_scale, CV_RGB2GRAY);
    threshold(gray_scale, output, 128, 255, 4);
}
