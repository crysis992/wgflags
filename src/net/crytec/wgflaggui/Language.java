package net.crytec.wgflaggui;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
 
/**
* An enum for requesting strings from the language file.
*/

public enum Language {
    TITLE("title-name", "&2[&fRegionOptions&2]"),
	
    ERROR_NO_REGION("error.noRegionFound", "&cThere is no region on your current position"),
    
	FLAG_TITLE("flag.menu_title" , "§lFlag Menu"),
	FLAG_EMPTY_MESSAGE("flag.message.empty" , "§cSorry, but you currently cannot set any flags!"),
	FLAG_CLEARED("flag.menu.cleared", "§7Flag (&6%flag%&7) has been cleared."),
	FLAG_ALLOWED("flag.menu.allowed", "§7Flag (&6%flag%&7) is now &2allowed!"),
	FLAG_DENIED("flag.menu.denied", "§7Flag (&6%flag%&7) is now &4forbidden"),
	FLAG_INPUT_CHAT("flag.message.chatinput", "&7Please enter the new value for flag %flag%:"),
	FLAG_GUI_LEFTCLICK("flag.menu.leftclick", "&2Left click &7to change"),
	FLAG_GUI_RIGHTCLICK("flag.menu.rightclick", "&2Right click &7to reset"),
	FLAG_CURRENT_VALUE("flag.menu.currentValue", "&7Current Value: %value%"),
	
	INTERFACE_NEXT_PAGE("interface.general.nextpage", "&fNext Page"),
	INTERFACE_PREVIOUS_PAGE("interface.general.previous", "&fPrevious Page"),
	INTERFACE_BACK("interface.general.back", "&fBack"),
	
	;
	
	
    private String path;
    private String def;
    private boolean isArray = false;
    
    private List<String> defArray;
    private static YamlConfiguration LANG;
 
    /**
    * Lang enum constructor.
    * @param path The string path.
    * @param start The default string.
    */
    private Language(String path, String start) {
        this.path = path;
        this.def = start;
    }
    
    private Language(String path, List<String> start) {
        this.path = path;
        this.defArray = start;
        this.isArray = true;
    }
    
    /**
    * Set the {@code YamlConfiguration} to use.
    * @param config The config to set.
    */
    public static void setFile(YamlConfiguration config) {
        LANG = config;
    }
    
    public static YamlConfiguration getFile() {
    	return LANG;
    }
    
    @Override
    public String toString() {
        if (this == TITLE) return ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, def)) + " ";
        return ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, def));
    }
    
    /**
     * Get the String with the TITLE
     * @return
     */
    public String toChatString() {
    	return TITLE.toString() + ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, def));
    }
    
    public List<String> getDescriptionArray() {
    	return LANG.getStringList(this.path).stream().map(x -> ChatColor.translateAlternateColorCodes('&', x)).collect(Collectors.toList());
    }
    
    public boolean isArray() {
    	return this.isArray;
    }
    
    public List<String> getDefArray() {
    	return this.defArray;
    }
     
    /**
    * Get the default value of the path.
    * @return The default value of the path.
    */
    public String getDefault() {
        return this.def;
    }
 
    /**
    * Get the path to the string.
    * @return The path to the string.
    */
    public String getPath() {
        return this.path;
    }
}
