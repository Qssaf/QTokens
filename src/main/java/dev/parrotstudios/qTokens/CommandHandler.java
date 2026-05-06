package dev.parrotstudios.qTokens;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {
    List<String> commandList = List.of("give", "take");


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) return true;
        if (args.length < 1 || args.length > 3) {
            player.sendMessage(ChatColor.RED + "Usage: /qtokens [give/take] <player> <amount>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "give": {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /qtokens give <player> <amount>");
                    return true;
                }
                Player target = QTokens.getInstance().getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "That player is not online.");
                    return true;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                    if(amount <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid amount.");
                    return true;
                }

               ItemStack stack = QTokens.getItem();
                stack.setAmount(amount);
                player.getInventory().addItem(stack);
                break;
            }
            case "take": {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /qtokens take <player> <amount>");
                    return true;
                }
                Player target = QTokens.getInstance().getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "That player is not online.");
                    return true;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                    if(amount <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid amount.");
                    return true;
                }

                for (ItemStack item : target.getInventory().getContents()) {
                    if (item == null || item.getType() == Material.AIR) continue;
                    if (QTokens.isCoin(item)) {
                        int currentAmount = item.getAmount();
                        if (currentAmount <= amount) {
                            amount -= currentAmount;
                            item.setAmount(0);
                        } else {
                            item.setAmount(currentAmount - amount);
                            amount = 0;
                        }
                    }
                    if (amount <= 0) break;
                }

                if (amount > 0) {
                    player.sendMessage(ChatColor.YELLOW + "Note: Could not take all tokens; player didn't have enough.");
                } else {
                    player.sendMessage(ChatColor.GREEN + "Successfully took tokens.");
                }
                break;
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length == 1){
            return commandList.stream()
                    .filter(value -> value.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if(args.length == 2){
            if(commandList.contains(args[0].toLowerCase())){
                return QTokens.getInstance().getServer()
                        .getOnlinePlayers()
                        .stream().map(Player::getName)
                        .filter(playerName -> playerName.startsWith(args[1].toLowerCase()))
                        .toList();
            }
        }
        if(args.length == 3){
            if(commandList.contains(args[0].toLowerCase())){
                return List.of("amount");
            }
        }
        return List.of();
    }


}
