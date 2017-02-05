package com.ancientloreapps.barcodescanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;


public class ScannerActivity extends AppCompatActivity implements CameraSourcePreview.CameraPreviewListener
{
	private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
	private static final int REQ_CODE_CAMERA = 140;

	private static final String TAG = "Barcode-reader";
	public static final String USE_ONLY_QR_CODE = "use_only_qr_code";

	// intent request code to handle updating play services if needed.
	public static final int RC_HANDLE_GMS = 9009;
	public static final int RC_CLIENTS = 0xc0de;

	// constants used to pass extra data in the intent
	public static final String BarcodeObject = "Barcode";

	private CameraSource mCameraSource;
	private CameraSourcePreview mPreview;

	/**
	 * Initializes the UI and creates the detector pipeline.
	 */
	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);

		if (ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[] { CAMERA_PERMISSION }, REQ_CODE_CAMERA);
		}
		else
		{
			initActivity();
		}
	}

	private void initActivity()
	{
		setContentView(R.layout.barcode_capture);
		mPreview = (CameraSourcePreview) findViewById(R.id.preview);
		createCameraSource(getIntent().getBooleanExtra(USE_ONLY_QR_CODE, false));
	}

	public void returnResult(final Barcode barcode)
	{
		Intent intent = new Intent().putExtra(BarcodeObject, barcode);
		setResult(barcode != null ? CommonStatusCodes.SUCCESS : CommonStatusCodes.RESOLUTION_REQUIRED,
				barcode != null ? intent : null);

		finish();
	}

	/**
	 * Creates and starts the camera.  Note that this uses a higher resolution in comparison
	 * to other detection examples to enable the barcode detector to detect small barcodes
	 * at long distances.
	 * <p>
	 * Suppressing InlinedApi since there is a check that the minimum version is met before using
	 * the constant.
	 */
	@SuppressLint("InlinedApi")
	private void createCameraSource(final boolean useOnlyQRCode)
	{
		Context context = getApplicationContext();

		// A barcode detector is created to track barcodes.  An associated multi-processor instance
		// is set to receive the barcode detection results, track the barcodes, and maintain
		// graphics for each barcode on screen.  The factory is used by the multi-processor to
		// create a separate tracker instance for each barcode.
		BarcodeDetector.Builder builder = new BarcodeDetector.Builder(context);

		if (useOnlyQRCode)
		{
			builder.setBarcodeFormats(Barcode.QR_CODE);
		}

		BarcodeDetector barcodeDetector = builder.build();
		BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(this);
		barcodeDetector.setProcessor(new MultiProcessor.Builder<>(barcodeFactory).build());

		if (!barcodeDetector.isOperational())
		{
			// Note: The first time that an app using the barcode or face API is installed on a
			// device, GMS will download a native libraries to the device in order to do detection.
			// Usually this completes before the app is run for the first time.  But if that
			// download has not yet completed, then the above call will not detect any barcodes
			// and/or faces.
			//
			// isOperational() can be used to check if the required native libraries are currently
			// available.  The detectors will automatically become operational once the library
			// downloads complete on device.
			Log.w(TAG, "Detector dependencies are not yet available.");

			// Check for low storage.  If there is low storage, the native library will not be
			// downloaded, so detection will not become operational.
			IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
			boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

			if (hasLowStorage)
			{
				Log.w(TAG, getString(R.string.low_storage_error));
			}
		}

		// Creates and starts the camera.  Note that this uses a higher resolution in comparison
		// to other detection examples to enable the barcode detector to detect small barcodes
		// at long distances.
		mCameraSource = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
				.setFacing(isBackCameraAvailable() ? CameraSource.CAMERA_FACING_BACK : CameraSource.CAMERA_FACING_FRONT)
				.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
				.setRequestedPreviewSize(1600, 1024)
				.setRequestedFps(15.0f).build();
	}

	private boolean isBackCameraAvailable()
	{
		return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		switch (requestCode)
		{
			case REQ_CODE_CAMERA:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					initActivity();
				}
				else
				{
					returnResult(null);
				}

				break;

			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	/**
	 * Restarts the camera.
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		startCameraSource();
	}

	/**
	 * Stops the camera.
	 */
	@Override
	protected void onPause()
	{
		super.onPause();

		if (mPreview != null)
		{
			mPreview.stop();
		}
	}

	/**
	 * Releases the resources associated with the camera source, the associated detectors, and the
	 * rest of the processing pipeline.
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if (mPreview != null)
		{
			mPreview.release();
		}
	}

	/**
	 * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
	 * (e.g., because onResume was called before the camera source was created), this will be called
	 * again when the camera source is created.
	 */
	private void startCameraSource()
	{
		// check that the device has play services available.
		int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());

		if (code != ConnectionResult.SUCCESS)
		{
			GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS).show();
		}

		if (mCameraSource != null)
		{
			try
			{
				mPreview.start(mCameraSource);
			}
			catch (IOException e)
			{
				Log.e(TAG, "Unable to start camera source.", e);
				mCameraSource.release();
				mCameraSource = null;
			}
		}
	}

	@Override
	public void onPreviewError(String error)
	{
		returnResult(null);
	}
}
