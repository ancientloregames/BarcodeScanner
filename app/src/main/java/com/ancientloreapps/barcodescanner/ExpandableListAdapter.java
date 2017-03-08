package com.ancientloreapps.barcodescanner;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;


/**
 * ${PACKAGE_NAME}. Created by ${USER} on ${DATE}.
 */

class ExpandableListAdapter extends BaseExpandableListAdapter
{
	private Activity context;
	private List<Item> items;
	private List<List<Expansion>> expansions;

	ExpandableListAdapter(Activity context, List<Item> items, List<List<Expansion>> expansions)
	{
		this.context = context;
		this.items = items;
		this.expansions = expansions;
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		return items.get(groupPosition);
	}

	@Override
	public long getGroupId(int groupPosition)
	{
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View layout, ViewGroup parent)
	{
		Item item = (Item) getGroup(groupPosition);
		if (layout == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			layout = inflater.inflate(R.layout.list_item, parent, false);
		}

		TextView title = (TextView) layout.findViewById(R.id.item);
		ImageButton button = (ImageButton) layout.findViewById(R.id.button);

		title.setText(context.getResources().getString(item.getTitleId()));
		button.setImageDrawable(context.getResources().getDrawable(item.getImageId()));
		button.setTag(item.getTitleId());

		return layout;
	}

	@Override
	public int getGroupCount()
	{
		return items.size();
	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		return expansions.get(groupPosition).size();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		return expansions.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition)
	{
		return childPosition;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition,
							 boolean isLastChild, View layout, ViewGroup parent)
	{
		Expansion item = (Expansion) getChild(groupPosition, childPosition);

		if (layout == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			layout = inflater.inflate(R.layout.list_item_expansion, parent, false);
		}

		TextView title = (TextView) layout.findViewById(R.id.title);
		TextView value = (TextView) layout.findViewById(R.id.value);

		title.setText(item.getName());
		value.setText(item.getValue());

		return layout;
	}

	@Override
	public boolean hasStableIds()
	{
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return true;
	}
}