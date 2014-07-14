package tum.andrive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.core.Core;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
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

    private static final Scalar colors[] = new Scalar[] {
		new Scalar(255, 128, 128, 255),
		new Scalar(255, 160, 128, 255),
		new Scalar(255, 255, 152, 255),
		new Scalar(152, 255, 152, 255),
		new Scalar(128, 255, 208, 255),
		new Scalar(128, 255, 255, 255),
		new Scalar(128, 160, 255, 255),
		new Scalar(128, 128, 255, 255),
		new Scalar(192, 128, 255, 255),
		new Scalar(255, 128, 255, 255)
    };
	private static final String TAG = "OCVSample::Activity";
    private static final int JAVA_DETECTOR = 0;
    private static final int NATIVE_DETECTOR = 1;
    
    private MenuItem mItemType;
    private int mDetectorType = JAVA_DETECTOR;
    private String[] mDetectorName;
    
    /** Size of the frame window used to false positive filtering **/
    private static final int N = 3;
    
    private int v_id = 1;
    private LinkedList<Rect> vehicles = new LinkedList<Rect>();
    private LinkedList<Integer> vids = new LinkedList<Integer>();
    
	private Mat mRgba;
	private Mat mGray;
    private File mCascadeFile;
	private CameraBridgeViewBase mOpenCvCameraView;
	private Queue<MatOfRect> detection_window;

    private CascadeClassifier javaClassifier;
    private DetectionBasedTracker mNativeDetector;

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

                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);
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
	
	public Andrive() {
		mDetectorName = new String[2];
		mDetectorName[JAVA_DETECTOR] = "Java Detector";
		mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";
	}
	
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
        detection_window = new LinkedList<MatOfRect>();
        		
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
        int width = mGray.cols();
        int height = mGray.rows();
        relativeObjSize = Math.round(height * 0.12f);
        
        mNativeDetector.setMinDetectionSize(relativeObjSize);
        
        if (mDetectorType == JAVA_DETECTOR) {
        	javaClassifier.detectMultiScale(mGray, objs, 1.1, 4, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
        			new Size(relativeObjSize, relativeObjSize), new Size());
        }
        else {
        	mNativeDetector.detect(mGray, objs);
        }

        track_vehicles(objs);
        
        /** Draw the final classification **/
        Rect[] objArray = vehicles.toArray(new Rect[0]);
        for (int i = 0; i < objArray.length; i++) {
            String distance = String.format("%.2fm", pixels_to_meters((double)objArray[i].width / (double)width));
            Scalar color = colors[vids.get(i) % colors.length];
            Core.rectangle(mRgba, objArray[i].tl(), objArray[i].br(), color, 3);
            Core.putText(mRgba, distance, objArray[i].tl(), Core.FONT_HERSHEY_SIMPLEX, 1.5, color, 4);
        }

        return mRgba;

    	// nativeThreshold(inputFrame.rgba().getNativeObjAddr(), mRgba.getNativeObjAddr());
    	// faceDetect(inputFrame.rgba().getNativeObjAddr(), mRgba.getNativeObjAddr());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.andrive, menu);
        mItemType = menu.add(mDetectorName[mDetectorType]);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemType) {
        	int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;
        	item.setTitle(mDetectorName[tmpDetectorType]);
        	setDetectorType(tmpDetectorType);
        }
        return true;
    }
    
    private void setDetectorType(int type) {
    	if (mDetectorType != type) {
    		mDetectorType = type;
    		
    		if (type == NATIVE_DETECTOR) {
    			Log.i(TAG, "Native Detector");
    			mNativeDetector.start();
    		}
    		else {
        		Log.i(TAG, "Cascade Java Detector enabled");
        		mNativeDetector.stop();
        	}
    	}
    }
    
    /**
     * \brief 
     * This function converts the normalized width in pixels of 
     * the classified bounding box containing the car to a distance 
     * in meters. This is done with a fitted exponential model of the 
     * form:
     * M(x) = a * exp(b * x) + c * exp(d * x)
     * @param[in] x width in pixels of the classification rectangle
     * @return distance in meters to the vehicle
     */
    public static double pixels_to_meters(double x) {
        double a = 286.2713350528750;
        double b = -75.44461973326037;
        double c = 36.00759908705765;
        double d = -8.988638172976206;
        return a * Math.exp(b * x) + c * Math.exp(d * x); 
    }

    
    /**
     * This function groups the persistent classifications among N
     * frames in the detection_window.
     * @param frame is the actual frame classification result
     * @return The consistent classified rectangles.
     */
    private List<Rect> filter_detection_window(MatOfRect frame){
    	MatOfRect group = new MatOfRect();
    	List<Rect> ls = new ArrayList<Rect>();
    	
    	detection_window.add(frame);
    	if (detection_window.size() == N) {
    		
    		/** Join the detection_window in one MatOfRect **/
    		Iterator<MatOfRect> it = detection_window.iterator();
    		while (it.hasNext()) {
    			ls.addAll(it.next().toList());
    		}
    		group.fromList(ls);
            Objdetect.groupRectangles(group, new MatOfInt(), N - 1, 0.7);
            detection_window.poll();
    	}
    	
    	return group.toList();
    }
    
    
    /**
     * This function keeps track of the vehicles being detected on each
     * frame. The final output is keep in the lists vehicles and vids.
     * @param frame
     */
    private void track_vehicles(MatOfRect frame) {
    	List<Rect> current_frame = filter_detection_window(frame);
    	LinkedList<Rect> final_vehicles = new LinkedList<Rect>();
    	LinkedList<Integer> final_ids = new LinkedList<Integer>();
    	
    	Iterator<Rect> it = current_frame.iterator();

    	while (it.hasNext()) {
    		Rect r1 = it.next();
    		Point c1 = new Point((r1.x + r1.width) / 2.0f, (r1.y + r1.height) / 2.0f);
    			
    		int id = v_id;	
    		for (int j = 0; j < vehicles.size(); j++) {
    			Rect r2 = vehicles.get(j);
    			Point c2 = new Point((r2.x + r2.width) / 2.0f, (r2.y + r2.height) / 2.0f);
    			Point c3 = new Point(c1.x - c2.x, c1.y - c2.y);
    			
    			double norm = Math.sqrt(Math.pow(c3.x, 2) + Math.pow(c3.y, 2));
    			if (norm < 20.0f) {
    				id = vids.get(j);
    				break;
    			}
    		}
    		
			final_vehicles.add(r1);
			final_ids.add(id);
			
    		if (id == v_id) { 
    			v_id++; 
    		}
    	}
    	
    	vehicles = final_vehicles;
    	vids = final_ids;
    }
}
