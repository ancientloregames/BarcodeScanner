package com.ancientloreapps.barcodescanner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.gms.vision.barcode.Barcode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;


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
		View layout = inflater.inflate(R.layout.list_fragment,container,false);

		listView = (ExpandableListView) layout.findViewById(R.id.list);

		return layout;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
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

	private void parseBarcode(Barcode barcode) throws NoSuchMethodException
	{
		if(barcode == null)
			return;

		listItems = new HashMap<>();
		Method method;

		if(barcode.url != null && barcode.url.url != null)
		{
			Class[] parameterTypes = new Class[1];
			parameterTypes[0] = String.class;
			method = ListFragment.class.getMethod("onClickButtonUrl", parameterTypes);
			listItems.put("url", adjustItem(barcode.url.title, new String[] {barcode.url.url}, method));
		}
		if (barcode.email != null)
		{
		}
		if (barcode.wifi != null)
		{
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

	private View adjustItem(final String label, final String[] expansions, final Method method)
	{

		LayoutInflater inflater = getLayoutInflater(null);
		View listItemUrl = inflater.inflate(R.layout.list_item,listView,true);
		TextView textView = (TextView)listItemUrl.findViewById(R.id.item);
		ImageButton button = (ImageButton)listItemUrl.findViewById(R.id.button);
		textView.setText(label);
		button.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Object[] parameters = new Object[1];
				parameters[0] = expansions[1];
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
		return listItemUrl;
	}

	private void onClickButtonUrl(String url)
	{
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		getActivity().startActivity(i);
	}
}
