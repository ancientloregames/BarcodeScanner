package com.ancientloreapps.barcodescanner;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.io.IOException;


public class CameraSourcePreview extends ViewGroup
{
	private static final String TAG = "CameraSourcePreview";

	private SurfaceView mSurfaceView;
	private boolean mStartRequested;
	private boolean mSurfaceAvailable;
	private CameraSource mCameraSource;

	private CameraPreviewListener listener;
	public interface CameraPreviewListener
	{
		void onPreviewError(String error);
	}

	public CameraSourcePreview(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		if(context instanceof CameraPreviewListener)
			this.listener = (CameraPreviewListener)context;
		mStartRequested = false;
		mSurfaceAvailable = false;

		mSurfaceView = new SurfaceView(context);
		mSurfaceView.getHolder().addCallback(new SurfaceCallback());
		addView(mSurfaceView);
	}

	public void start(CameraSource cameraSource) throws IOException
	{
		if (cameraSource == null)
		{
			stop();
		}

		mCameraSource = cameraSource;

		if (mCameraSource != null)
		{
			mStartRequested = true;
			startIfReady();
		}
	}

	public void stop()
	{
		if (mCameraSource != null)
		{
			mCameraSource.stop();
		}
	}

	public void release()
	{
		if (mCameraSource != null)
		{
			mCameraSource.release();
			mCameraSource = null;
		}
	}

	private void startIfReady() throws IOException
	{
		if (mStartRequested && mSurfaceAvailable)
		{
			try
			{
				mCameraSource.start(mSurfaceView.getHolder());
			}catch (RuntimeException e){
				e.printStackTrace();
				if(listener != null)
					listener.onPreviewError(e.getMessage());
			}
			mStartRequested = false;
		}
	}

	private class SurfaceCallback implements SurfaceHolder.Callback
	{
		@Override
		public void surfaceCreated(SurfaceHolder surface)
		{
			mSurfaceAvailable = true;
			try
			{
				startIfReady();
			}
			catch (SecurityException se)
			{
				Log.e(TAG, "Do not have permission to start the camera", se);
			}
			catch (IOException e)
			{
				Log.e(TAG, "Could not start camera source.", e);
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder surface)
		{
			mSurfaceAvailable = false;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
		{
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		for (int i = 0; i < getChildCount(); ++i)
		{
			getChildAt(i).layout(0, 0, right, bottom);
		}

		try
		{
			startIfReady();
		}
		catch (SecurityException se)
		{
			Log.e(TAG, "Do not have permission to start the camera", se);
		}
		catch (IOException e)
		{
			Log.e(TAG, "Could not start camera source.", e);
		}
	}
}
