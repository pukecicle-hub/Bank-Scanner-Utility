package com.bankscanner;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Bank Scanner",
	description = "Scans your bank when opened and lists items with quantities, prices, filtering and sorting. Right-click any item to open its OSRS Wiki page.",
	tags = {"bank", "items", "scanner", "filter", "sort", "wiki", "price", "quantity"}
)
public class BankScannerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ItemManager itemManager;

	@Inject
	private BankScannerConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	private BankScannerPanel panel;
	private NavigationButton navButton;

	@Override
	protected void startUp() throws Exception
	{
		panel = new BankScannerPanel(config);
		panel.setOnRescan(this::scanBank);

		BufferedImage icon = loadIconSafely();

		navButton = NavigationButton.builder()
			.tooltip("Bank Scanner")
			.icon(icon)
			.priority(5)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
		log.info("Bank Scanner started");
	}

	/**
	 * Never throws — returns a blank image if the resource is missing.
	 */
	private BufferedImage loadIconSafely()
	{
		// Paths relative to the class package and absolute classpath root
		String[] paths = {
			"icon.png",                 // com/bankscanner/icon.png
			"/icon.png",                // root of jar
			"/com/bankscanner/icon.png"
		};

		for (String path : paths)
		{
			try
			{
				BufferedImage img = ImageUtil.loadImageResource(BankScannerPlugin.class, path);
				if (img != null)
				{
					return img;
				}
			}
			catch (Throwable t)
			{
				// ImageUtil throws IllegalArgumentException when the resource is missing
			}
		}

		log.warn("Plugin icon not found — using blank icon");
		return new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
	}

	@Override
	protected void shutDown() throws Exception
	{
		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
		}
		if (panel != null)
		{
			panel.updateItems(null);
		}
		log.info("Bank Scanner stopped");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN
			|| event.getGameState() == GameState.HOPPING)
		{
			if (panel != null)
			{
				panel.updateItems(null);
			}
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.BANK.getId())
		{
			if (isBankOpen())
			{
				scanBank();
			}
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == 12)
		{
			clientThread.invokeLater(() ->
			{
				if (isBankOpen())
				{
					scanBank();
				}
			});
		}
	}

	public void scanBank()
	{
		clientThread.invoke(() ->
		{
			ItemContainer bank = client.getItemContainer(InventoryID.BANK);
			if (bank == null)
			{
				return;
			}

			List<BankItem> items = new ArrayList<>();
			Item[] containerItems = bank.getItems();
			if (containerItems == null)
			{
				return;
			}

			for (Item item : containerItems)
			{
				if (item == null)
				{
					continue;
				}
				int id = item.getId();
				int qty = item.getQuantity();
				if (id <= 0 || qty <= 0)
				{
					continue;
				}

				ItemComposition comp = itemManager.getItemComposition(id);
				if (comp == null)
				{
					continue;
				}

				int canonicalId = itemManager.canonicalize(id);
				int gePrice = itemManager.getItemPrice(canonicalId);
				int haPrice = comp.getHaPrice();

				boolean placeholder = comp.getPlaceholderTemplateId() == 14401 || qty == 0;

				String name = comp.getMembersName();
				if (name == null || name.isEmpty())
				{
					name = comp.getName();
				}

				items.add(new BankItem(
					id,
					name,
					qty,
					gePrice,
					haPrice,
					comp.isMembers(),
					comp.isTradeable(),
					placeholder
				));
			}

			if (panel != null)
			{
				panel.updateItems(items);
			}
			log.debug("Bank scanned: {} stacks", items.size());
		});
	}

	private boolean isBankOpen()
	{
		Widget bankContainer = client.getWidget(ComponentID.BANK_ITEM_CONTAINER);
		if (bankContainer == null)
		{
			bankContainer = client.getWidget(12, 12);
		}
		return bankContainer != null && !bankContainer.isHidden();
	}

	@Provides
	BankScannerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BankScannerConfig.class);
	}
}
