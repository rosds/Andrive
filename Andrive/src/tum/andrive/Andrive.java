package tum.andrive;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.WindowManager;

public class Andrive extends Activity implements CvCameraViewListener2 {

	private CameraBridgeViewBase mOpenCvCameraView;
	private static final String TAG = "OCVSample::Activity";
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					Log.i(TAG, "OpenCV loaded successfully");
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
    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_andrive);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.AndriveView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
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
    public void onCameraViewStarted(int width, int height) {};
    
    @Override
    public void onCameraViewStopped() {};
    
    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	return inputFrame.rgba();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.andrive, menu);
        return true;
    }
    
}
