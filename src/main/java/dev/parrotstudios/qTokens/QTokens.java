package dev.parrotstudios.qTokens;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    private static final Map<Character, String> LEGACY_TO_MINI = new HashMap<>();
    private static final Cache<String, String> COMPONENT_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();


    static {
        LEGACY_TO_MINI.put('0', "<black>");
        LEGACY_TO_MINI.put('1', "<dark_blue>");
        LEGACY_TO_MINI.put('2', "<dark_green>");
        LEGACY_TO_MINI.put('3', "<dark_aqua>");
        LEGACY_TO_MINI.put('4', "<dark_red>");
        LEGACY_TO_MINI.put('5', "<dark_purple>");
        LEGACY_TO_MINI.put('6', "<gold>");
        LEGACY_TO_MINI.put('7', "<gray>");
        LEGACY_TO_MINI.put('8', "<dark_gray>");
        LEGACY_TO_MINI.put('9', "<blue>");
        LEGACY_TO_MINI.put('a', "<green>");
        LEGACY_TO_MINI.put('b', "<aqua>");
        LEGACY_TO_MINI.put('c', "<red>");
        LEGACY_TO_MINI.put('d', "<light_purple>");
        LEGACY_TO_MINI.put('e', "<yellow>");
        LEGACY_TO_MINI.put('f', "<white>");
        LEGACY_TO_MINI.put('k', "<obfuscated>");
        LEGACY_TO_MINI.put('l', "<bold>");
        LEGACY_TO_MINI.put('m', "<strikethrough>");
        LEGACY_TO_MINI.put('n', "<underlined>");
        LEGACY_TO_MINI.put('o', "<italic>");
        LEGACY_TO_MINI.put('r', "<reset>");
    }

    public static String convertLegacyToMiniMessage(String input) {
        final StringBuilder sb = new StringBuilder(input.length());

        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);


            if (c == '#' && i + 6 < input.length()) {
                final char prev = i > 0 ? input.charAt(i - 1) : 0;
                if (prev != '<' && prev != ':') {
                    final String hex = input.substring(i + 1, i + 7);
                    if (hex.matches("[0-9a-fA-F]{6}")) {
                        sb.append('<').append('#').append(hex).append('>');
                        i += 6;
                        continue;
                    }
                }
            }

            if (c == '&' || c == '§') {

                if (i + 7 < input.length() && input.charAt(i + 1) == '#') {
                    final String hex = input.substring(i + 2, i + 8);
                    if (hex.matches("[0-9a-fA-F]{6}")) {
                        sb.append('<').append('#').append(hex).append('>');
                        i += 7;
                        continue;
                    }
                }


                if (i + 1 < input.length()) {
                    final char code = Character.toLowerCase(input.charAt(i + 1));
                    final String replacement = LEGACY_TO_MINI.get(code);

                    if (replacement != null) {
                        sb.append(replacement);
                        i++;
                        continue;
                    }
                }
            }

            sb.append(c);
        }

        return sb.toString();
    }

    public static Component format(String message) {
        if (message == null || message.isEmpty()) return Component.empty();

        try {
            return MiniMessage.miniMessage().deserialize(COMPONENT_CACHE.get(message, () -> {

                return convertLegacyToMiniMessage(ConfigManager.getPrefix() + message);}));
        } catch (Exception e) {

            return MiniMessage.miniMessage().deserialize(convertLegacyToMiniMessage(message));
        }
    }

    public static Component formatNoPrefix(String message) {
        if (message == null || message.isEmpty()) return Component.empty();

        try {
            return MiniMessage.miniMessage().deserialize(COMPONENT_CACHE.get(message, () -> {

                return convertLegacyToMiniMessage(ConfigManager.getPrefix() + message);}).replace(ConfigManager.getPrefix(),""));
        } catch (Exception e) {

            return MiniMessage.miniMessage().deserialize(convertLegacyToMiniMessage(message));
        }
    }



    public static boolean isCoin(ItemStack stack){
        return stack.getPersistentDataContainer().has(tokenKey);
    }



    public static QTokens getInstance() {
        return JavaPlugin.getPlugin(QTokens.class);
    }

    @Override
    public void onEnable() {
        ConfigManager.init(this);
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

    public static void prepareItem(){
        tokenStack = ItemStack.of(Material.SUNFLOWER);
        ItemMeta tokenMeta =  tokenStack.getItemMeta();
        tokenMeta.displayName(formatNoPrefix(ConfigManager.getString("token.name")));
        tokenMeta.lore(ConfigManager.getStringList("token.lore").stream().map(QTokens::formatNoPrefix).toList());
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

    public static String getFormattedBalance(Player player) {
        int balance = getPlayerBalance(player);
        return NumberFormat.getNumberInstance().format(balance);
    }

}
