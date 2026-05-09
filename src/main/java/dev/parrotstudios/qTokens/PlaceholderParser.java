package dev.parrotstudios.qTokens;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class PlaceholderParser extends PlaceholderExpansion {

    private static final PlaceholderParser instance = new PlaceholderParser();

    public static PlaceholderParser getInstance() {
        return instance;
    }

    private PlaceholderParser() {}

    @Override
    public @NotNull String getIdentifier() {
        return "QTokens";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Qssaf";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        List<String> paramList = List.of(params.split("_"));
        if(paramList.isEmpty())  return "";

        return switch (paramList.getFirst()) {
            case "balance": {
                if(paramList.size() > 2){
                   if(paramList.get(2).equalsIgnoreCase("formatted")) yield QTokens.getFormattedBalance(player);
                   yield "";
                }
                yield String.valueOf(QTokens.getPlayerBalance(player));
            }
            case null, default: yield "";
        };
    }

    @Override
    public @NonNull List<String> getPlaceholders() {
        return List.of(
                "%qtokens_balance%",
                "%qtokens_balance_formatted%"
        );
    }
}
