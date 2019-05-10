package net.crytec.wgflaggui.flags;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.DoubleFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import lombok.Getter;
import net.crytec.api.itemstack.ItemBuilder;
import net.crytec.api.smartInv.ClickableItem;
import net.crytec.api.smartInv.content.InventoryContents;
import net.crytec.api.util.ChatStringInput;
import net.crytec.api.util.F;
import net.crytec.wgflaggui.Language;
import net.crytec.wgflaggui.WorldGuardFlagGUI;


public class FlagSetting implements Comparable<FlagSetting> {

	private Flag<?> flag;
	private String id;
	private List<String> description;
	private Material icon = Material.PAPER;
	private FlagInputType inputType;
	@Getter
	private final Permission permission;
	private final String displayname;

	public FlagSetting(String id, Flag<?> flag, Material icon, String displayname, List<String> description) {
		this.flag = flag;
		this.id = id;
		this.displayname = displayname;
		this.description = description.stream().map( line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
		this.icon = icon;
		
		this.permission = new Permission("flagmenu." + this.id, "Enables the use of the " + id + " flag.", PermissionDefault.TRUE);
		try {
		Bukkit.getPluginManager().addPermission(this.permission);
		} catch (IllegalArgumentException ex) { /*Permission is already defined, but we can safely ignore that. */ }
		
		if (flag instanceof StringFlag) {
			this.inputType = FlagInputType.STRING;
		} else if (flag instanceof StateFlag) {
			this.inputType = FlagInputType.STATE;
		} else if (flag instanceof SetFlag) {
			this.inputType = FlagInputType.SET;
		} else if (flag instanceof IntegerFlag) {
			this.inputType = FlagInputType.INTEGER;
		} else if (flag instanceof BooleanFlag) {
			this.inputType = FlagInputType.BOOLEAN;
		} else {
			this.inputType = FlagInputType.UNKNOWN;
		}
	}

	public ClickableItem getButton(Player player, ProtectedRegion region, InventoryContents contents) {
		
		ItemBuilder builder = new ItemBuilder(this.icon).name("§7" + this.getName());
		builder.setItemFlag(ItemFlag.HIDE_ATTRIBUTES);
		builder.setItemFlag(ItemFlag.HIDE_ENCHANTS);
		
		if (region.getFlags().containsKey(this.getFlag())) {
			if (this.inputType == FlagInputType.STATE) {
				StateFlag sf = (StateFlag) this.getFlag();
				String name = (region.getFlag(sf) == StateFlag.State.DENY) ? "§c" + ChatColor.stripColor(this.getName()) : "§a" + ChatColor.stripColor(this.getName());
				builder.name(name);
			}
			builder.enchantment(Enchantment.ARROW_DAMAGE);
		}
		builder.lore(this.description);
		builder.lore(Language.FLAG_CURRENT_VALUE.toString().replace("%value%", this.getCurrentValue(region)));
		builder.lore("");
		builder.lore(Language.FLAG_GUI_LEFTCLICK.toString());
		builder.lore(Language.FLAG_GUI_RIGHTCLICK.toString());
		
		return new ClickableItem(builder.build(), e -> {
			if (e.getClick() == ClickType.RIGHT && region.getFlags().containsKey(this.getFlag())) {
				region.setFlag(this.getFlag(), null);
				player.sendMessage(Language.FLAG_CLEARED.toString().replace("%flag%", this.getName()));
				contents.inventory().getProvider().reOpen(player, contents);
			return;
		}
			
			//Boolean or state
			if (this.inputType == FlagInputType.STATE || this.inputType == FlagInputType.BOOLEAN) {
				this.switchState(player, region);
				contents.inventory().getProvider().reOpen(player, contents);
				
			} else {
				// Everything else requires user input
				player.closeInventory();
				player.sendMessage(Language.FLAG_INPUT_CHAT.toChatString().replace("%flag%", this.getName()));
				ChatStringInput.addPlayer(player, input -> {
					try {
						setFlag(region, flag, BukkitAdapter.adapt(player), input);
						Bukkit.getScheduler().runTaskLater(WorldGuardFlagGUI.getInstance(), () -> contents.inventory().getProvider().reOpen(player, contents) , 1L);
					} catch (InvalidFlagFormat e1) {
						player.sendMessage(e1.getMessage());
					}
					
				});
				
			}
		});
	}
	
	
	private void switchState(Player player, ProtectedRegion region) {

		if (this.inputType == FlagInputType.STATE) {
			StateFlag sf = (StateFlag) this.getFlag();

			if (region.getFlags().containsKey(this.getFlag())) {
				if (region.getFlag(sf) == StateFlag.State.DENY) {
					region.setFlag(sf, StateFlag.State.ALLOW);
					player.sendMessage(Language.FLAG_ALLOWED.toString().replace("%flag%", this.getName()));
				} else {
					region.setFlag(sf, StateFlag.State.DENY);
					player.sendMessage(Language.FLAG_DENIED.toString().replace("%flag%", this.getName()));
				}
			} else {
				region.setFlag(sf, StateFlag.State.ALLOW);
				player.sendMessage(Language.FLAG_ALLOWED.toString().replace("%flag%", this.getName()));
			}
		} else {
			BooleanFlag bf = (BooleanFlag) this.getFlag();

			if (region.getFlags().containsKey(this.getFlag())) {
				if (!region.getFlag(bf).booleanValue()) {
					region.setFlag(bf, true);
					player.sendMessage(Language.FLAG_ALLOWED.toString().replace("%flag%", this.getName()));
				} else {
					region.setFlag(bf, false);
					player.sendMessage(Language.FLAG_DENIED.toString().replace("%flag%", this.getName()));
				}
			} else {
				region.setFlag(bf, true);
				player.sendMessage(Language.FLAG_ALLOWED.toString().replace("%flag%", this.getName()));
			}

		}
	}
	
	@SuppressWarnings("unchecked")
	public String getCurrentValue(ProtectedRegion region) {
		
		if (!region.getFlags().containsKey(this.getFlag())) {
			return "§cFlag not set";
		}
		
		switch (this.inputType) {
			case BOOLEAN : return F.tf(region.getFlag(((BooleanFlag) this.getFlag())).booleanValue());
			case DOUBLE : return F.name("" + region.getFlag(((DoubleFlag) this.getFlag())).doubleValue());
			case INTEGER : return F.name("" + region.getFlag(((IntegerFlag) this.getFlag())).intValue());
			case SET : {
				SetFlag<String> setflag = (SetFlag<String>) this.getFlag();
				return F.format ( region.getFlag(setflag), ",", "none");
			}
			case STATE : return F.tf( (region.getFlag(((StateFlag) this.getFlag())) == StateFlag.State.DENY) ? false : true  );
			case STRING : return F.name(region.getFlag(((StringFlag) this.getFlag())).toString());
			case UNKNOWN: return "§7Unknown";
			default : return  "Unable to query flag value";
			
		}
	}
	
    
	public void setIcon(Material mat) {
		this.icon = mat;
	}

	public Material getIcon() {
		return this.icon;
	}

	public String getId() {
		return this.id;
	}

	public Flag<?> getFlag() {
		return this.flag;
	}
	
    protected static <V> void setFlag(ProtectedRegion region, Flag<V> flag, Actor sender, String value) throws InvalidFlagFormat {
        region.setFlag(flag, flag.parseInput(FlagContext.create().setSender(sender).setInput(value).setObject("region", region).build()));
    }

	public String getName() {
		return this.displayname;
	}

	@Override
	public int compareTo(FlagSetting other) {
		return this.getId().compareTo(other.getId());
	}
}