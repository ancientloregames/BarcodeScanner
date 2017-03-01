package com.ancientloreapps.barcodescanner;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.vision.barcode.Barcode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import static com.google.android.gms.vision.barcode.Barcode.WiFi.OPEN;
import static com.google.android.gms.vision.barcode.Barcode.WiFi.WEP;
import static com.google.android.gms.vision.barcode.Barcode.WiFi.WPA;


/**
 * Created by Firefly on 2/28/2017.
 */

public class ListFragment extends Fragment
{
	static final String ARGUMENT = "barcode";

	ExpandableListView listView;
	private HashMap<String,View> listItems;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.list_fragment,container,false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view,savedInstanceState);

		listView = (ExpandableListView) view.findViewById(R.id.list);
		Bundle arguments = getArguments();
		if(arguments != null)
		{
			Barcode barcode = arguments.getParcelable(ARGUMENT);
			try
			{
				parseBarcode(barcode);
			}
			catch (NoSuchMethodException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDestroyView()
	{
		for(View item : listItems.values())
		{
			item.findViewById(R.id.button).setOnClickListener(null);
		}
		super.onDestroyView();
	}

	private void parseBarcode(Barcode barcode) throws NoSuchMethodException
	{
		if(barcode == null)
			return;

		listItems = new HashMap<>();
		Method method;

		if(URLUtil.isValidUrl(barcode.displayValue))//barcode.url is always empty
		{
			Class[] parameterTypes = new Class[1];
			parameterTypes[0] = String.class;
			method = ListFragment.class.getMethod("onClickButtonUrl", parameterTypes);
			listItems.put("url",
					adjustItem(getString(R.string.label_link),
							method,
							new Object[] {barcode.displayValue}));
		}
		if (barcode.wifi != null)
		{
			Class[] parameterTypes = new Class[3];
			parameterTypes[0] = int.class;
			parameterTypes[1] = String.class;
			parameterTypes[2] = String.class;
			method = ListFragment.class.getMethod("onClickButtonWifi", parameterTypes);
			listItems.put("wifi",
					adjustItem(getString(R.string.label_wifi),
							method,
							new Object[] {barcode.wifi.encryptionType,
									barcode.wifi.ssid,
									barcode.wifi.password}));
		}
		if (barcode.email != null)
		{
			Class[] parameterTypes = new Class[3];
			parameterTypes[0] = String.class;
			parameterTypes[1] = String.class;
			parameterTypes[2] = String.class;
			method = ListFragment.class.getMethod("onClickButtonEmail", parameterTypes);
			listItems.put("email",
					adjustItem(getString(R.string.label_email),
							method,
							new Object[] {barcode.email.address,
									barcode.email.subject,
									barcode.email.body}));
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
	}

	private View adjustItem(final String label, final Method method, final Object[] parameters)
	{

		LayoutInflater inflater = getLayoutInflater(null);
		View listItemUrl = inflater.inflate(R.layout.list_item,listView,false);
		TextView textView = (TextView)listItemUrl.findViewById(R.id.item);
		Button button = (Button)listItemUrl.findViewById(R.id.button);
		textView.setText(label);
		button.setText(label);
		button.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				try
				{
					method.invoke(this, parameters);
				}
				catch (IllegalAccessException e)
				{
					e.printStackTrace();
				}
				catch (InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}
		});
		listView.addView(listItemUrl);
		return listItemUrl;
	}

	private void onClickButtonUrl(String url)
	{
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		if(intent.resolveActivity(getContext().getPackageManager()) != null)
			startActivity(intent);
		else
			Toast.makeText(getContext(), getString(R.string.message_noapp),Toast.LENGTH_SHORT).show();

	}

	private void onClickButtonWifi(int type, String ssid, String password)
	{
		WifiConfiguration conf = new WifiConfiguration();

		conf.SSID = "\"" + ssid + "\"";
		String pass = "\"" + password + "\"";
		switch(type)
		{
			case OPEN:
				conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
				break;
			case WEP:
				conf.wepKeys[0] = pass;
				conf.wepTxKeyIndex = 0;
				conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
				conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
				break;
			case WPA:
				conf.preSharedKey = pass;
				break;
		}
		WifiManager wifiManager = (WifiManager)getContext().getSystemService(Context.WIFI_SERVICE);
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
		if(intent.resolveActivity(getContext().getPackageManager()) != null)
			startActivity(Intent.createChooser(intent, getString(R.string.message_email)));
		else
			Toast.makeText(getContext(), getString(R.string.message_noapp),Toast.LENGTH_SHORT).show();
	}
}
