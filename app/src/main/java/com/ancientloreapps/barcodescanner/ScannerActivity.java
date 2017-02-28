package com.ancientloreapps.barcodescanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;


public class ScannerActivity extends AppCompatActivity implements CameraSourcePreview.CameraPreviewListener
{
	private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
	private static final int CODE_REQ_CAMERA = 1001;
	public static final int CODE_RC_GMS = 1002;

	private static final String TAG = "Barcode-reader";

	private CameraSource mCameraSource;
	private CameraSourcePreview mPreview;

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);

		if (ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[] { CAMERA_PERMISSION }, CODE_REQ_CAMERA);
		}
		else
		{
			initialize();
		}
	}

	private void initialize()
	{
		setContentView(R.layout.barcode_capture);
		mPreview = (CameraSourcePreview) findViewById(R.id.preview);
		createCameraSource(true);
	}

	public void manageResult(Barcode barcode)
	{
		ListFragment listFragment = new ListFragment();
		Bundle arguments = new Bundle();
		arguments.putParcelable(ListFragment.ARGUMENT,barcode);
		listFragment.setArguments(arguments);
		getSupportFragmentManager()
				.beginTransaction()
				.add(listFragment,"list")
				.commitNowAllowingStateLoss();
	}

	@SuppressLint("InlinedApi")
	private void createCameraSource(final boolean useOnlyQRCode)
	{
		Context context = getApplicationContext();

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
			Log.w(TAG, "Detector dependencies are not yet available.");

			IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
			boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

			if (hasLowStorage)
			{
				Log.w(TAG, getString(R.string.low_storage_error));
			}
		}

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
			case CODE_REQ_CAMERA:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					initialize();
				}
				else
				{
					finish();
				}

				break;

			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		startCameraSource();
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		if (mPreview != null)
		{
			mPreview.stop();
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if (mPreview != null)
		{
			mPreview.release();
		}
	}

	private void startCameraSource()
	{
		int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());

		if (code != ConnectionResult.SUCCESS)
		{
			GoogleApiAvailability.getInstance().getErrorDialog(this, code, CODE_RC_GMS).show();
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
		finish();
	}
}
