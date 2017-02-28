package com.ancientloreapps.barcodescanner;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

import java.lang.ref.WeakReference;


class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode>
{
	private WeakReference<ScannerActivity> activity;

	BarcodeTrackerFactory(final ScannerActivity activity)
	{
		this.activity = new WeakReference<>(activity);
	}

	@Override
	public Tracker<Barcode> create(Barcode barcode)
	{
		return new BarcodeTracker();
	}

	private class BarcodeTracker extends Tracker<Barcode>
	{
		@Override
		public void onNewItem(int id, Barcode item)
		{
			if (activity != null)
			{
				activity.get().manageResult(item);
			}
		}
	}
}

