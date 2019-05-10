package net.crytec.wgflaggui;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.crytec.api.itemstack.ItemBuilder;
import net.crytec.api.smartInv.ClickableItem;
import net.crytec.api.smartInv.content.InventoryContents;
import net.crytec.api.smartInv.content.InventoryProvider;
import net.crytec.api.smartInv.content.Pagination;
import net.crytec.api.smartInv.content.SlotIterator;
import net.crytec.api.smartInv.content.SlotIterator.Type;
import net.crytec.wgflaggui.flags.FlagManager;

public class FlagMenuGUI implements InventoryProvider {

	private static final ItemStack fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
	private final FlagManager flagManager;
	private final ProtectedRegion region;
	
	public FlagMenuGUI(ProtectedRegion region, FlagManager manager) {
		this.region = region;
		this.flagManager = manager;
	}

	@Override
	public void init(Player player, InventoryContents contents) {

		contents.fillRow(0, ClickableItem.empty(fill));
		contents.fillRow(4, ClickableItem.empty(fill));

		Pagination pagination = contents.pagination();
		LinkedList<ClickableItem> items = new LinkedList<ClickableItem>();

		//Add Buttons for all enabled flags
		this.flagManager.getFlagMap().forEach(fs -> items.add(fs.getButton(player, region, contents)));

		ClickableItem[] c = new ClickableItem[items.size()];
		c = items.toArray(c);

		SlotIterator slotIterator = contents.newIterator(Type.HORIZONTAL, 1, 0);
		slotIterator = slotIterator.allowOverride(false);

		pagination.setItems(c);
		pagination.setItemsPerPage(27);
		pagination.addToIterator(slotIterator);

		if (!pagination.isLast()) {
			contents.set(4, 6, ClickableItem.of(new ItemBuilder(Material.MAP).name(Language.INTERFACE_NEXT_PAGE.toString()).build(), e -> {
				contents.inventory().open(player, pagination.next().getPage(), new String[]{"region"}, new Object[]{region});
			}));
		}

		if (!pagination.isFirst()) {
			contents.set(4, 2, ClickableItem.of(new ItemBuilder(Material.MAP).name(Language.INTERFACE_PREVIOUS_PAGE.toString()).build(), e -> {
				contents.inventory().open(player, pagination.previous().getPage(), new String[]{"region"}, new Object[]{region});
			}));
		}

		pagination.addToIterator(contents.newIterator(Type.HORIZONTAL, 1, 0));
	}

	@Override
	public void update(Player player, InventoryContents contents) {
	}
}