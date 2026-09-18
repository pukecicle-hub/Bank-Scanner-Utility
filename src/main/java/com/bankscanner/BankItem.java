package com.bankscanner;

import lombok.Data;

/**
 * Immutable snapshot of a single bank stack used for display and sorting.
 */
@Data
public class BankItem
{
	private final int id;
	private final String name;
	private final int quantity;
	private final int gePrice;      // unit GE price
	private final int haPrice;      // unit high alch price
	private final long totalValue;  // quantity * gePrice
	private final boolean members;
	private final boolean tradeable;
	private final boolean placeholder;

	public BankItem(int id, String name, int quantity, int gePrice, int haPrice,
		boolean members, boolean tradeable, boolean placeholder)
	{
		this.id = id;
		this.name = name;
		this.quantity = quantity;
		this.gePrice = gePrice;
		this.haPrice = haPrice;
		this.totalValue = (long) quantity * gePrice;
		this.members = members;
		this.tradeable = tradeable;
		this.placeholder = placeholder;
	}
}
