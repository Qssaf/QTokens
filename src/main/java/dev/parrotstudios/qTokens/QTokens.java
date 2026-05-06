package dev.parrotstudios.qTokens;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

@SuppressWarnings("DataFlowIssue")
public class QTokens extends JavaPlugin {

    private static ItemStack tokenStack = null;
    private static NamespacedKey tokenKey;
    public static NamespacedKey getTokenKey() {
        return tokenKey;
    }

    public static ItemStack getItem(){
        return new ItemStack(tokenStack);
    }

    public static boolean isCoin(ItemStack stack){
        return stack.getPersistentDataContainer().has(tokenKey);
    }

    public static Component format(String message){
        return MiniMessage.miniMessage().deserialize(message);
    }

    public static QTokens getInstance() {
        return JavaPlugin.getPlugin(QTokens.class);
    }

    @Override
    public void onEnable() {
        prepareKey();
        prepareItem();
        if(getInstance().getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            PlaceholderParser.getInstance().register();
        }
        getServer().getPluginManager().registerEvents(new EventManager(), this);
        getCommand("qtokens").setExecutor(new CommandHandler());

        getLogger().info("QTokens has been enabled!");
    }

    @Override
    public void onDisable() {

        getLogger().info("QTokens has been disabled!");
    }

    private void prepareKey(){
       tokenKey = new NamespacedKey(QTokens.getInstance(), "qtoken");
    }

    private void prepareItem(){
        tokenStack = ItemStack.of(Material.SUNFLOWER);
        ItemMeta tokenMeta =  tokenStack.getItemMeta();
        tokenMeta.displayName(Component.text("Token"));
        tokenMeta.setEnchantmentGlintOverride(true);
        tokenMeta.getPersistentDataContainer().set(tokenKey, PersistentDataType.BOOLEAN, true);
        tokenStack.setItemMeta(tokenMeta);
    }

    public static int getPlayerBalance(Player player) {
        return Arrays.stream(player.getInventory().getContents())
                .filter(item -> item != null && item.getType() != Material.AIR)
                .filter(item -> item.getPersistentDataContainer().has(tokenKey, PersistentDataType.BOOLEAN))
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

}
