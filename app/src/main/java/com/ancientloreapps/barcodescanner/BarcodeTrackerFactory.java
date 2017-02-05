package com.ancientloreapps.barcodescanner;

import android.content.Intent;
import android.net.Uri;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;


class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode>
{
	private ScannerActivity activity;

	BarcodeTrackerFactory(final ScannerActivity activity)
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
				String url = item.displayValue;
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				activity.startActivity(i);
			}
		}
	}
}

