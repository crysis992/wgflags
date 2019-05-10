package net.crytec.wgflaggui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.crytec.api.itemstack.ItemBuilder;
import net.crytec.api.smartInv.ClickableItem;
import net.crytec.api.smartInv.SmartInventory;
import net.crytec.api.smartInv.content.InventoryContents;
import net.crytec.api.smartInv.content.InventoryProvider;
import net.crytec.api.smartInv.content.Pagination;
import net.crytec.api.smartInv.content.SlotIterator.Type;
import net.crytec.wgflaggui.flags.FlagManager;

public class RegionSelectGUI implements InventoryProvider {

	private final List<ProtectedRegion> regions;
	private final FlagManager flagManager;

	public RegionSelectGUI(List<ProtectedRegion> regions, FlagManager flagManager) {
		this.regions = regions;
		this.flagManager = flagManager;
	}

	@Override
	public void init(Player player, InventoryContents contents) {

		Pagination pagination = contents.pagination();
		ArrayList<ClickableItem> items = new ArrayList<ClickableItem>();

		for (ProtectedRegion region : this.regions) {
			String id = ChatColor.GREEN + region.getId();

			ItemStack item = new ItemBuilder(Material.BOOK).name(id).build();
			items.add(ClickableItem.of(item, e -> {
				SmartInventory.builder().provider(new FlagMenuGUI(region, this.flagManager)).title(region.getId()).size(5).build().open(player);
			}));
		}

		ClickableItem[] c = new ClickableItem[items.size()];
		c = items.toArray(c);

		pagination.setItems(c);
		pagination.setItemsPerPage(18);
		pagination.addToIterator(contents.newIterator(Type.HORIZONTAL, 1, 0));

	}
}