package net.crytec.wgflaggui.flags;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.google.common.collect.Lists;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.LocationFlag;

import net.crytec.api.config.PluginConfig;
import net.crytec.shaded.org.apache.lang3.EnumUtils;
import net.crytec.wgflaggui.WorldGuardFlagGUI;

public class FlagManager {
	
	private static final Permission allPerm = new Permission("flagmenu.all", "Allow the useage of all flags", PermissionDefault.FALSE);
	
	private LinkedList<FlagSetting> settings = Lists.newLinkedList();
	
	private LinkedList<FlagSetting>  flags = Lists.newLinkedList();
	private final PluginConfig flagConfig;
	
	private final List<String> forbiddenFlags;
	
	public FlagManager(WorldGuardFlagGUI plugin, List<String> blacklist) {
		
		this.forbiddenFlags = blacklist;
		plugin.getLogger().info("Blacklisted " + this.forbiddenFlags.size() + " flags.");
		
		this.flagConfig = new PluginConfig(plugin, plugin.getDataFolder(), "flags.yml");
		boolean needSave = false;
		
		
		for (Flag<?> flag : WorldGuard.getInstance().getFlagRegistry().getAll()) {
			if (this.forbiddenFlags.contains(flag.getName()) || flag instanceof LocationFlag) {
				continue;
			}
			
			String path = "flags." + flag.getName() + ".";
			if (!flagConfig.isSet("flags." + flag.getName() + ".name")) {
				flagConfig.set(path + "name", "&7" + flag.getName());
				flagConfig.set(path + "enabled", true);
				flagConfig.set(path + "icon", Material.LIGHT_GRAY_DYE.toString());
				flagConfig.set(path + "description", Arrays.asList("&7This changes " + flag.getName()));
				needSave = true;
			}
			
			Material icon = Material.BARRIER;
			
			if (EnumUtils.isValidEnum(Material.class, flagConfig.getString(path + "icon"))) {
				icon = Material.valueOf(flagConfig.getString(path + "icon"));
			}
			
			if (flagConfig.getBoolean(path + "enabled")) {
				this.addFlags(flag.getName(), flag, icon, ChatColor.translateAlternateColorCodes('&', flagConfig.getString(path + "name")), flagConfig.getStringList(path + "description"));
			}
		}
		if (needSave) {
			this.flagConfig.saveConfig();
		}
		this.settings.sort(Comparator.comparing(FlagSetting::getId));
		
		this.getFlagMap().stream().map(FlagSetting::getPermission).collect(Collectors.toList()).forEach(perm -> {
			allPerm.getChildren().put(perm.getName(), perm.getDefault().getValue(false));
		});
	}
	
	
	public void addFlags(String idenfifier, Flag<?> flag, Material icon, String displayname, List<String> description) {
		this.flags.add(new FlagSetting(idenfifier, flag, icon, displayname, description));
	}
	
	public LinkedList<FlagSetting> getFlagMap() {
		return this.flags;
	}
}