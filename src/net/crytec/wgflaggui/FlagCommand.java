package net.crytec.wgflaggui;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.crytec.acf.BaseCommand;
import net.crytec.acf.annotation.CommandAlias;
import net.crytec.acf.annotation.CommandPermission;
import net.crytec.acf.annotation.Default;
import net.crytec.api.smartInv.SmartInventory;
import net.crytec.wgflaggui.flags.FlagManager;

@CommandAlias("regionoptions")
@CommandPermission("wggui.options")
public class FlagCommand extends BaseCommand {
	
	private final FlagManager manager;
	
	public FlagCommand(FlagManager manager) {
		this.manager = manager;
	}

	@Default
	public void openInterface(Player player) {
		
		RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()));
		
		List<ProtectedRegion> regions = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation())).getRegions()
		.stream()
		.filter(pr -> pr.getOwners().contains(player.getUniqueId()))
		.collect(Collectors.toList());
		
		if (regions.isEmpty()) {
			player.sendMessage(Language.ERROR_NO_REGION.toChatString());
			return;
		}
		
		if (regions.size() > 1) {
			SmartInventory.builder().provider(new RegionSelectGUI(regions, this.manager)).title("Select region").size(5).build().open(player);
			return;
		}
		
		ProtectedRegion region = regions.get(0);
		SmartInventory.builder().provider(new FlagMenuGUI(region, this.manager)).title(region.getId()).size(5).build().open(player);
	}
	
}
