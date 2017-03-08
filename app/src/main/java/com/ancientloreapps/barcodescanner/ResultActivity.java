package com.ancientloreapps.barcodescanner;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ExpandableListView;
import android.widget.Toast;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.ArrayList;
import java.util.List;


/**
 * ${PACKAGE_NAME}. Created by ${USER} on ${DATE}.
 */

public class ResultActivity extends AppCompatActivity
{
	static final String ARGUMENT = "barcode";

	Barcode barcode;
	ExpandableListView listView;

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		initialize();
	}

	private void initialize()
	{
		setContentView(R.layout.list_fragment);
		listView = (ExpandableListView) findViewById(R.id.list);
		Intent intent = getIntent();
		Barcode barcode = intent.getParcelableExtra(ARGUMENT);
		if(barcode != null)
		{
			listView.setAdapter(getListAdapter(barcode));
		}
		else finish();
	}

	@Nullable
	private ExpandableListAdapter getListAdapter(Barcode barcode)
	{
		if(barcode == null)
			return null;
		this.barcode = barcode;

		ArrayList<Item> items = new ArrayList<>();
		ArrayList<List<Expansion>> groups = new ArrayList<>();

		if(URLUtil.isValidUrl(barcode.displayValue)) // barcode.url is always empty so far
		{
			ArrayList<Expansion> expansions = new ArrayList<>();
			if(barcode.url != null)
			{
				if(!"".equals(barcode.url.title))
					expansions.add(new Expansion(getString(R.string.exp_title), barcode.url.title));
				if(!"".equals(barcode.url.url))
					expansions.add(new Expansion(getString(R.string.exp_url), barcode.url.url));
				else expansions.add(new Expansion(getString(R.string.exp_url), barcode.displayValue));
			}
			else expansions.add(new Expansion(getString(R.string.exp_url), barcode.displayValue));

			items.add(new Item(R.string.label_link,R.drawable.common_full_open_on_phone));
			groups.add(expansions);
		}
		if (barcode.wifi != null && !"".equals(barcode.wifi.ssid))
		{
			ArrayList<Expansion> expansions = new ArrayList<>();

			String encription;
			switch (barcode.wifi.encryptionType)
			{
				default:
					encription = getString(R.string.misc_unknown);
					break;
				case Barcode.WiFi.OPEN:
					encription = getString(R.string.encription_open);
					break;
				case Barcode.WiFi.WEP:
					encription = getString(R.string.encription_wep);
					break;
				case Barcode.WiFi.WPA:
					encription = getString(R.string.encription_wpa);
					break;
			}
			expansions.add(new Expansion(getString(R.string.exp_encription_type), encription));
			expansions.add(new Expansion(getString(R.string.exp_title), barcode.wifi.ssid));
			expansions.add(new Expansion(getString(R.string.exp_url), barcode.wifi.password));

			items.add(new Item(R.string.label_wifi,R.drawable.common_full_open_on_phone));
			groups.add(expansions);
		}
		if (barcode.email != null)
		{
			ArrayList<Expansion> expansions = new ArrayList<>();

			String type;
			switch (barcode.email.type)
			{
				default:
					type = getString(R.string.misc_unknown);
					break;
				case Barcode.Email.HOME:
					type = getString(R.string.encription_open);
					break;
				case Barcode.Email.WORK:
					type = getString(R.string.encription_wep);
					break;
			}
			expansions.add(new Expansion(getString(R.string.exp_type), type));
			if(!"".equals(barcode.email.address))
				expansions.add(new Expansion(getString(R.string.exp_address), barcode.email.address));
			if(!"".equals(barcode.email.address))
				expansions.add(new Expansion(getString(R.string.exp_subject), barcode.email.subject));
			if(!"".equals(barcode.email.address))
				expansions.add(new Expansion(getString(R.string.exp_message), barcode.email.body));

			items.add(new Item(R.string.label_email,R.drawable.common_full_open_on_phone));
			groups.add(expansions);
		}
		if (barcode.contactInfo != null)
		{
		}
		if (barcode.calendarEvent != null)
		{
		}
		if (barcode.driverLicense != null)
		{
		}
		if (barcode.geoPoint != null)
		{
		}
		if (barcode.sms != null)
		{
		}
		if (barcode.phone != null)
		{
		}

		return new ExpandableListAdapter(this, items, groups);
	}

	public void onClickHandler(View view)
	{
		int item = (int) view.getTag();
		switch (item)
		{
			case R.string.label_link:
				onClickButtonUrl(barcode.displayValue);
				break;
			case R.string.label_wifi:
				onClickButtonWifi(barcode.wifi.encryptionType,barcode.wifi.ssid,barcode.wifi.password);
				break;
			case R.string.label_email:
				onClickButtonEmail(barcode.email.address,barcode.email.subject,barcode.email.body);
				break;
		}
	}

	private void onClickButtonUrl(String url)
	{
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		if(intent.resolveActivity(getPackageManager()) != null)
			startActivity(intent);
		else
			Toast.makeText(this, getString(R.string.message_noapp),Toast.LENGTH_SHORT).show();

	}

	private void onClickButtonWifi(int type, String ssid, String password)
	{
		WifiConfiguration conf = new WifiConfiguration();

		conf.SSID = "\"" + ssid + "\"";
		String pass = "\"" + password + "\"";
		switch(type)
		{
			case Barcode.WiFi.OPEN:
				conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
				break;
			case Barcode.WiFi.WEP:
				conf.wepKeys[0] = pass;
				conf.wepTxKeyIndex = 0;
				conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
				conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
				break;
			case Barcode.WiFi.WPA:
				conf.preSharedKey = pass;
				break;
		}
		WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		wifiManager.addNetwork(conf);

		List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
		for( WifiConfiguration i : list ) {
			if(conf.SSID.equals(i.SSID)) {
				wifiManager.disconnect();
				wifiManager.enableNetwork(i.networkId, true);
				wifiManager.reconnect();
				break;
			}
		}
	}

	private void onClickButtonEmail(String address, String subject, String message)
	{
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] {address});
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, message);
		if(intent.resolveActivity(getPackageManager()) != null)
			startActivity(Intent.createChooser(intent, getString(R.string.message_email)));
		else
			Toast.makeText(this, getString(R.string.message_noapp),Toast.LENGTH_SHORT).show();
	}
}
