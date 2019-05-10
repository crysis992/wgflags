package net.crytec.wgflaggui;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import net.crytec.acf.BukkitCommandManager;
import net.crytec.wgflaggui.flags.FlagManager;

public class WorldGuardFlagGUI extends JavaPlugin {

	@Getter
	private static WorldGuardFlagGUI instance;
	
	@Getter
	private FlagManager flagManager;

	@Override
	public void onLoad() {
		WorldGuardFlagGUI.instance = this;
	}

	@Override
	public void onEnable() {
		
		this.loadLanguage();
		
		File file = new File(this.getDataFolder(), "config.yml");
		if (!file.exists()) {
			this.saveResource("config.yml", true);
			this.reloadConfig();
		}
		
		this.flagManager = new FlagManager(this, this.getConfig().getStringList("forbiddenflags"));
		
		BukkitCommandManager manager = new BukkitCommandManager(this);
		manager.registerCommand(new FlagCommand(this.flagManager));
		
	}

	@Override
	public void onDisable() {

	}
	
	private void loadLanguage() {
		File lang = new File(this.getDataFolder(), "lang.yml");
		if (!lang.exists()) {
			try {
				this.getDataFolder().mkdir();
				lang.createNewFile();
				if (lang != null) {
					YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(lang);
					defConfig.save(lang);
					Language.setFile(defConfig);
				}
			} catch (IOException e) {
				this.getLogger().severe("Could not create language file!");
				Bukkit.getPluginManager().disablePlugin(this);
			}
		}

		YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
		for (Language item : Language.values()) {
			if (conf.getString(item.getPath()) == null) {
				if (item.isArray()) {
					conf.set(item.getPath(), item.getDefArray());
				} else {
					conf.set(item.getPath(), item.getDefault());
				}
			}
		}
		Language.setFile(conf);
		try {
			conf.save(lang);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}