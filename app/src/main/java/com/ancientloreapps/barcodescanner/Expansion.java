package com.ancientloreapps.barcodescanner;


/**
 * ${PACKAGE_NAME}. Created by ${USER} on ${DATE}.
 */

class Expansion
{
	private final String name;
	private final String value;

	Expansion(String name, String value)
	{
		this.name = name;
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	public String getName()
	{
		return name;
	}
}
