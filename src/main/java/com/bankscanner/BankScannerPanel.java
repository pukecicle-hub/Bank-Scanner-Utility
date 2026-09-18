package com.bankscanner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.QuantityFormatter;

public class BankScannerPanel extends PluginPanel
{
	private static final String WIKI_BASE = "https://oldschool.runescape.wiki/w/";
	private static final int ICON_SIZE = 32;

	private final BankScannerConfig config;
	private final ItemManager itemManager;
	private Runnable onRescan;

	private final JTextField searchField = new JTextField();
	private final JTextField minQtyField = new JTextField("0");
	private final JTextField minPriceField = new JTextField("0");
	private final JComboBox<SortMode> sortCombo = new JComboBox<>(SortMode.values());
	private final JCheckBox changeOrderCheck = new JCheckBox("Change order", true);
	private final JCheckBox membersOnlyCheck = new JCheckBox("Members only", false);
	private final JCheckBox tradeableOnlyCheck = new JCheckBox("Tradeable only", false);
	private final JCheckBox showHaCheck = new JCheckBox("Show High Alch", false);
	private final JLabel statusLabel = new JLabel("Open your bank to scan items.");
	private final JLabel totalLabel = new JLabel("");
	private final JPanel itemListPanel = new JPanel();

	private List<BankItem> allItems = new ArrayList<>();

	public BankScannerPanel(BankScannerConfig config, ItemManager itemManager)
	{
		super(false);
		this.config = config;
		this.itemManager = itemManager;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// ===== Settings / filters header =====
		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		header.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		header.setBorder(new EmptyBorder(8, 8, 8, 8));

		JLabel title = new JLabel("Bank Scanner");
		title.setForeground(Color.WHITE);
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.add(title);
		header.add(Box.createVerticalStrut(6));

		statusLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.add(statusLabel);

		totalLabel.setForeground(ColorScheme.BRAND_ORANGE);
		totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.add(totalLabel);

		header.add(Box.createVerticalStrut(8));

		// Search
		JLabel searchLabel = new JLabel("Search name:");
		searchLabel.setForeground(Color.LIGHT_GRAY);
		searchLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.add(searchLabel);
		searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
		searchField.setAlignmentX(Component.LEFT_ALIGNMENT);
		searchField.getDocument().addDocumentListener(simpleListener(this::refreshList));
		header.add(searchField);
		header.add(Box.createVerticalStrut(4));

		// Filters row
		JPanel filterRow = new JPanel(new GridLayout(2, 2, 4, 4));
		filterRow.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		filterRow.setAlignmentX(Component.LEFT_ALIGNMENT);
		filterRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

		JLabel minQtyLbl = new JLabel("Min qty:");
		minQtyLbl.setForeground(Color.LIGHT_GRAY);
		filterRow.add(minQtyLbl);
		minQtyField.getDocument().addDocumentListener(simpleListener(this::refreshList));
		filterRow.add(minQtyField);

		JLabel minPriceLbl = new JLabel("Min GE price:");
		minPriceLbl.setForeground(Color.LIGHT_GRAY);
		filterRow.add(minPriceLbl);
		minPriceField.getDocument().addDocumentListener(simpleListener(this::refreshList));
		filterRow.add(minPriceField);

		header.add(filterRow);
		header.add(Box.createVerticalStrut(4));

		// Sort
		JPanel sortRow = new JPanel(new BorderLayout(4, 0));
		sortRow.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		sortRow.setAlignmentX(Component.LEFT_ALIGNMENT);
		sortRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		sortCombo.addActionListener(e -> refreshList());
		sortRow.add(sortCombo, BorderLayout.CENTER);
		changeOrderCheck.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		changeOrderCheck.setForeground(Color.LIGHT_GRAY);
		changeOrderCheck.setToolTipText("Checked = ascending, unchecked = descending");
		changeOrderCheck.addActionListener(e -> refreshList());
		sortRow.add(changeOrderCheck, BorderLayout.EAST);
		header.add(sortRow);
		header.add(Box.createVerticalStrut(4));

		// Checkboxes
		styleCheck(membersOnlyCheck);
		membersOnlyCheck.addActionListener(e -> refreshList());
		header.add(membersOnlyCheck);

		styleCheck(tradeableOnlyCheck);
		tradeableOnlyCheck.addActionListener(e -> refreshList());
		header.add(tradeableOnlyCheck);

		styleCheck(showHaCheck);
		showHaCheck.setSelected(config.showHaPrice());
		showHaCheck.addActionListener(e -> refreshList());
		header.add(showHaCheck);

		header.add(Box.createVerticalStrut(6));

		JButton refreshBtn = new JButton("Rescan Bank");
		refreshBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
		refreshBtn.addActionListener(e ->
		{
			if (onRescan != null)
			{
				onRescan.run();
			}
		});
		header.add(refreshBtn);

		// ===== Divider between settings and list =====
		JPanel northWrapper = new JPanel(new BorderLayout());
		northWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		northWrapper.add(header, BorderLayout.CENTER);

		JSeparator divider = new JSeparator(SwingConstants.HORIZONTAL);
		divider.setForeground(new Color(60, 60, 60));
		divider.setBackground(new Color(60, 60, 60));
		divider.setBorder(new EmptyBorder(4, 0, 4, 0));
		northWrapper.add(divider, BorderLayout.SOUTH);

		add(northWrapper, BorderLayout.NORTH);

		// ===== Item list =====
		itemListPanel.setLayout(new BoxLayout(itemListPanel, BoxLayout.Y_AXIS));
		itemListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JScrollPane scroll = new JScrollPane(itemListPanel);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		add(scroll, BorderLayout.CENTER);
	}

	private void styleCheck(JCheckBox box)
	{
		box.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		box.setForeground(Color.LIGHT_GRAY);
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
	}

	public void setOnRescan(Runnable onRescan)
	{
		this.onRescan = onRescan;
	}

	public void updateItems(List<BankItem> items)
	{
		this.allItems = items != null ? new ArrayList<>(items) : new ArrayList<>();
		SwingUtilities.invokeLater(this::refreshList);
	}

	private void refreshList()
	{
		itemListPanel.removeAll();

		String search = searchField.getText().trim().toLowerCase();
		int minQty = parseIntSafe(minQtyField.getText(), 0);
		int minPrice = parseIntSafe(minPriceField.getText(), 0);
		boolean asc = changeOrderCheck.isSelected();
		SortMode mode = (SortMode) sortCombo.getSelectedItem();
		boolean membersOnly = membersOnlyCheck.isSelected();
		boolean tradeableOnly = tradeableOnlyCheck.isSelected();
		boolean showHa = showHaCheck.isSelected();

		List<BankItem> filtered = allItems.stream()
			.filter(i -> !config.hidePlaceholders() || !i.isPlaceholder())
			.filter(i -> search.isEmpty() || i.getName().toLowerCase().contains(search))
			.filter(i -> i.getQuantity() >= minQty)
			.filter(i -> i.getGePrice() >= minPrice)
			.filter(i -> !membersOnly || i.isMembers())
			.filter(i -> !tradeableOnly || i.isTradeable())
			.collect(Collectors.toList());

		Comparator<BankItem> cmp;
		switch (mode)
		{
			case QUANTITY:
				cmp = Comparator.comparingInt(BankItem::getQuantity);
				break;
			case GE_PRICE:
				cmp = Comparator.comparingInt(BankItem::getGePrice);
				break;
			case TOTAL_VALUE:
				cmp = Comparator.comparingLong(BankItem::getTotalValue);
				break;
			case HA_PRICE:
				cmp = Comparator.comparingInt(BankItem::getHaPrice);
				break;
			case NAME:
			default:
				cmp = Comparator.comparing(BankItem::getName, String.CASE_INSENSITIVE_ORDER);
				break;
		}
		if (!asc)
		{
			cmp = cmp.reversed();
		}
		filtered.sort(cmp);

		long totalValue = 0;
		int totalStacks = filtered.size();
		long totalQty = 0;

		for (BankItem item : filtered)
		{
			totalValue += item.getTotalValue();
			totalQty += item.getQuantity();
			itemListPanel.add(createItemRow(item, showHa));
		}

		if (filtered.isEmpty())
		{
			JLabel empty = new JLabel(allItems.isEmpty()
				? "  No bank data yet — open your bank."
				: "  No items match the current filters.");
			empty.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			itemListPanel.add(empty);
		}

		statusLabel.setText(String.format("%d stacks  |  %,d total items", totalStacks, totalQty));
		totalLabel.setText("Bank GE value (filtered): " + QuantityFormatter.quantityToStackSize(totalValue) + " gp");

		itemListPanel.revalidate();
		itemListPanel.repaint();
	}

	private JPanel createItemRow(BankItem item, boolean showHa)
	{
		JPanel row = new JPanel(new BorderLayout(8, 0));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(new EmptyBorder(4, 6, 4, 6));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

		// Item icon on the left
		JLabel iconLabel = new JLabel();
		iconLabel.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
		iconLabel.setMinimumSize(new Dimension(ICON_SIZE, ICON_SIZE));
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		try
		{
			AsyncBufferedImage img = itemManager.getImage(item.getId(), item.getQuantity(), false);
			if (img != null)
			{
				img.addTo(iconLabel);
			}
		}
		catch (Exception ignored)
		{
			// leave blank if image fails
		}
		row.add(iconLabel, BorderLayout.WEST);

		// Text details
		JPanel text = new JPanel();
		text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
		text.setOpaque(false);

		JLabel nameLabel = new JLabel(item.getName());
		nameLabel.setForeground(Color.WHITE);
		text.add(nameLabel);

		StringBuilder detail = new StringBuilder();
		detail.append("x").append(QuantityFormatter.formatNumber(item.getQuantity()));
		if (config.showGePrice() && item.getGePrice() > 0)
		{
			detail.append("  |  ").append(QuantityFormatter.quantityToStackSize(item.getGePrice())).append(" ea");
		}
		if (config.showTotalValue() && item.getTotalValue() > 0)
		{
			detail.append("  |  ").append(QuantityFormatter.quantityToStackSize(item.getTotalValue())).append(" total");
		}
		if (showHa && item.getHaPrice() > 0)
		{
			detail.append("  |  HA ").append(QuantityFormatter.quantityToStackSize(item.getHaPrice()));
		}

		JLabel detailLabel = new JLabel(detail.toString());
		detailLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		text.add(detailLabel);

		row.add(text, BorderLayout.CENTER);

		// Right-click menu
		JPopupMenu popup = new JPopupMenu();
		JMenuItem wikiItem = new JMenuItem("Open Wiki");
		wikiItem.addActionListener(e -> openWiki(item));
		popup.add(wikiItem);

		JMenuItem copyName = new JMenuItem("Copy name");
		copyName.addActionListener(e -> {
			java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(new java.awt.datatransfer.StringSelection(item.getName()), null);
		});
		popup.add(copyName);

		JMenuItem copyId = new JMenuItem("Copy item ID");
		copyId.addActionListener(e -> {
			java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(new java.awt.datatransfer.StringSelection(String.valueOf(item.getId())), null);
		});
		popup.add(copyId);

		row.setComponentPopupMenu(popup);
		row.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (SwingUtilities.isRightMouseButton(e))
				{
					popup.show(row, e.getX(), e.getY());
				}
			}
		});

		return row;
	}

	private void openWiki(BankItem item)
	{
		String url = WIKI_BASE + "Special:Lookup?type=item&id=" + item.getId();
		LinkBrowser.browse(url);
	}

	private static int parseIntSafe(String s, int def)
	{
		try
		{
			return Integer.parseInt(s.trim().replace(",", ""));
		}
		catch (NumberFormatException e)
		{
			return def;
		}
	}

	private static DocumentListener simpleListener(Runnable r)
	{
		return new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e) { r.run(); }
			@Override
			public void removeUpdate(DocumentEvent e) { r.run(); }
			@Override
			public void changedUpdate(DocumentEvent e) { r.run(); }
		};
	}
}
