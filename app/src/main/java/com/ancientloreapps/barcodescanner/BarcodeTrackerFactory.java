package com.ancientloreapps.barcodescanner;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;


public class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode>
{
	private BarcodeCaptureActivity activity;

	BarcodeTrackerFactory(final BarcodeCaptureActivity activity)
	{
		this.activity = activity;
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
				activity.returnResult(item);
			}
		}
	}
}

