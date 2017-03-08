package com.ancientloreapps.barcodescanner;


/**
 * ${PACKAGE_NAME}. Created by ${USER} on ${DATE}.
 */

class Item
{
	private final int titleId;
	private final int imageId;

	Item(int titleId, int imageId)
	{
		this.imageId = imageId;
		this.titleId = titleId;
	}

	public int getTitleId()
	{
		return titleId;
	}

	public int getImageId()
	{
		return imageId;
	}
}
