package com.bankscanner;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("bankscannerplugin")
public interface BankScannerConfig extends Config
{
	@ConfigSection(
		name = "Display",
		description = "How items are shown in the panel",
		position = 0
	)
	String displaySection = "displaySection";

	@ConfigItem(
		keyName = "showGePrice",
		name = "Show GE price",
		description = "Display Grand Exchange unit price for each item",
		section = displaySection,
		position = 0
	)
	default boolean showGePrice()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showTotalValue",
		name = "Show total value",
		description = "Display quantity × GE price for each stack",
		section = displaySection,
		position = 1
	)
	default boolean showTotalValue()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showHaPrice",
		name = "Show High Alch",
		description = "Display high alchemy value for each item",
		section = displaySection,
		position = 2
	)
	default boolean showHaPrice()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hidePlaceholders",
		name = "Hide placeholders",
		description = "Do not list bank placeholders (empty slots with placeholder icons)",
		section = displaySection,
		position = 3
	)
	default boolean hidePlaceholders()
	{
		return true;
	}

	@ConfigItem(
		keyName = "autoOpenPanel",
		name = "Open panel on bank open",
		description = "Automatically expand the Bank Scanner side panel when the bank is opened",
		section = displaySection,
		position = 4
	)
	default boolean autoOpenPanel()
	{
		return false;
	}
}
