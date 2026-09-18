package com.bankscanner;

public enum SortMode
{
	NAME("Name"),
	QUANTITY("Quantity"),
	GE_PRICE("GE Price"),
	TOTAL_VALUE("Total Value"),
	HA_PRICE("High Alch");

	private final String displayName;

	SortMode(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
