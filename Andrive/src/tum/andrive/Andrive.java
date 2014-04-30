package tum.andrive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Math;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.core.Core;
import org.opencv.core.Rect;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;


public class Andrive extends Activity implements CvCameraViewListener2 {

	private static final String TAG = "OCVSample::Activity";
    private static final Scalar RECT_COLOR = new Scalar(0, 255, 0, 255);

	private Mat mRgba;
	private Mat mGray;
    private File mCascadeFile;
	private CameraBridgeViewBase mOpenCvCameraView;

    private CascadeClassifier javaClassifier;

    private int relativeObjSize = 0;
    
    private GPSListener gps;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					Log.i(TAG, "OpenCV loaded successfully");

                    // load native library code
					System.loadLibrary("AndriveNative");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.cascade);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "cascade.xml");
                            FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        javaClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (javaClassifier.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            javaClassifier = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    
					mOpenCvCameraView.enableView();
				}break;
				
				default:
				{
					super.onManagerConnected(status);
				} break;
			}

		}
	};
	
	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        gps = new GPSListener(this);
        		
    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_andrive);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.AndriveView);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    protected void onPause() {
    	super.onPause();
    	if (mOpenCvCameraView != null) {
    		mOpenCvCameraView.disableView();
		}
    };
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if (mOpenCvCameraView != null) {
			mOpenCvCameraView.disableView();
		}
    };
    
    @Override
    public void onCameraViewStarted(int width, int height) {
    	mGray = new Mat();
    	mRgba = new Mat();
    };
    
    @Override
    public void onCameraViewStopped() {
    	mRgba.release();
        mGray.release();
    };
    
    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        MatOfRect objs = new MatOfRect();

        // Adjust minimun size for objects in the image
        int height = mGray.rows();
        relativeObjSize = Math.round(height * 0.2f);
        
        javaClassifier.detectMultiScale(mGray, objs, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
        new Size(relativeObjSize, relativeObjSize), new Size());

        Rect[] objArray = objs.toArray();
        for (int i = 0; i < objArray.length; i++) {
            String distance = String.format("%.2fm", pixels_to_meters(objArray[i].width));
            Core.rectangle(mRgba, objArray[i].tl(), objArray[i].br(), RECT_COLOR, 3);
            Core.putText(mRgba, distance, objArray[i].tl(), Core.FONT_HERSHEY_SIMPLEX, 1.5, RECT_COLOR, 4);
        }

        return mRgba;

    	// nativeThreshold(inputFrame.rgba().getNativeObjAddr(), mRgba.getNativeObjAddr());
    	// faceDetect(inputFrame.rgba().getNativeObjAddr(), mRgba.getNativeObjAddr());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.andrive, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        return true;
    }
    
    public native void nativeThreshold(long input, long output);
    public native void faceDetect(long input, long output);
    public native void loadClassifierXml(String path);

    
    /**
     * This function converts the width in pixels of the classified
     * bounding box containing the car to a distance in meters. This 
     * is done with a fitted exponential model of the form
     * M(x) = a * exp(b * x) + c * exp(d * x)
     * @param x width in pixels of the classification rectangle
     * @return distance in meters to the vehicle
     */
    public static double pixels_to_meters(int x) {
        double a = 162.220606414207;
        double b = -0.0136042729498191;
        double c = 15.4820029252565;
        double d = -0.00118712463148137;
        return a * Math.exp(b * x) + c * Math.exp(d * x); 
    }

}
