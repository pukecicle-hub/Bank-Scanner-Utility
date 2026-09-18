# Bank Scanner

A RuneLite plugin that scans your bank when you open it and displays every item stack in a searchable, filterable, and sortable side panel.

## Features

- **Automatic bank scan** – when you open the bank (or deposit/withdraw while it is open) the plugin reads every stack.
- **Item list** with:
  - Name
  - Quantity
  - Grand Exchange unit price (optional)
  - Total GE value of the stack (optional)
  - High Alchemy value (optional)
- **Filters**
  - Free-text name search
  - Minimum quantity
  - Minimum GE unit price
  - Members-only items
  - Tradeable-only items
  - Hide bank placeholders
- **Sorting** – by Name, Quantity, GE Price, Total Value, or High Alch, ascending or descending.
- **Right-click any item** in the panel:
  - **Open Wiki** – opens the OSRS Wiki page for that item (ID-based lookup)
  - Copy name / Copy item ID
- **Rescan Bank** button to force a refresh.
- Optional auto-open of the side panel when the bank opens (config).

## How to use

1. Install / load the plugin in RuneLite (developer mode or via Plugin Hub once published).
2. Open your bank in-game.
3. Click the **Bank Scanner** icon on the RuneLite sidebar.
4. Use the search box, min-qty / min-price fields, sort dropdown, and checkboxes to narrow the list.
5. Right-click any row → **Open Wiki**.

## Configuration

| Setting              | Description                                      |
|----------------------|--------------------------------------------------|
| Show GE price        | Show unit Grand Exchange price                   |
| Show total value     | Show quantity × GE price                         |
| Show High Alch       | Show high alchemy value                          |
| Hide placeholders    | Exclude empty placeholder slots                  |
| Open panel on bank open | Automatically focus the panel when bank opens |

## Building & running locally

Requires Java 11+ and a GitHub account (for the template workflow).

```bash
# From the plugin directory
./gradlew run
```

This launches RuneLite in developer mode with the plugin loaded.

## Notes

- Prices come from RuneLite’s `ItemManager` (GE / wiki prices).
- “Rarity” is not a first-class game field; use the GE-price and members/tradeable filters as proxies.
- The plugin is purely observational – it never moves items or sends packets.

## License

BSD 2-Clause (same as the RuneLite example plugin).
