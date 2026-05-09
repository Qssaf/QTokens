package dev.parrotstudios.qTokens;

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
    List<String> commandList = List.of("give", "take","reload","balance");


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(QTokens.format(ConfigManager.getString("messages.console")));
            return true;
        }
        if (args.length < 1 || args.length > 3) {
            player.sendMessage(QTokens.format(ConfigManager.getString("messages.usage")));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload":{
                if(!player.hasPermission("qtokens.use.reload")){
                    player.sendMessage(QTokens.format(ConfigManager.getString("messages.noPermission")));
                    return true;
                }
                ConfigManager.reloadConfig();
                QTokens.prepareItem();
                player.sendMessage(QTokens.format(ConfigManager.getString("messages.reload")));
                return true;
            }
            case "balance": {
                if(!(args.length > 1)){
                    if(!player.hasPermission("qtokens.balance")){
                        player.sendMessage(QTokens.format(ConfigManager.getString("messages.noPermission")));
                        return true;
                    }
                    player.sendMessage(QTokens.format(ConfigManager.getString("messages.balance")
                            .replace("%amount%", QTokens.getFormattedBalance(player))));
                    return true;
                }
                Player target = QTokens.getInstance().getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(QTokens.format(ConfigManager.getString("messages.notOnline"
                            ,"<red>That player is not online.")));
                    return true;
                }
                if(!player.hasPermission("qtokens.use.balance.others")){
                    player.sendMessage(QTokens.format(ConfigManager.getString("messages.noPermission")));
                    return true;
                }
                player.sendMessage(QTokens.format(ConfigManager.getString("messages.balanceOther").replace("%player%",target.getName())
                        .replace("%amount%", QTokens.getFormattedBalance(target))));

                break;
            }
            case "give": {
                if (args.length < 3) {
                    player.sendMessage(QTokens.format(ConfigManager.getString("messages.usage")));
                    return true;
                }
                Player target = QTokens.getInstance().getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(QTokens.format(ConfigManager.getString("messages.notOnline"
                            ,"<red>That player is not online.")));
                    return true;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                    if(amount <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    player.sendMessage(QTokens.format(ConfigManager
                            .getString("messages.invalidAmount","<red>Invalid amount.")));
                    return true;
                }

               ItemStack stack = QTokens.getItem();
                stack.setAmount(amount);
                target.getInventory().addItem(stack);
                player.sendMessage(QTokens.format(ConfigManager
                        .getString("messages.gave","").replace("%amount%", String.valueOf(amount).replace("%player%",target.getName()))));
                break;
            }
            case "take": {
                if (args.length < 3) {
                    player.sendMessage(QTokens.format(ConfigManager.getString("messages.usage")));
                    return true;
                }
                Player target = QTokens.getInstance().getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(QTokens.format(ConfigManager.getString("messages.notOnline"
                            ,"<red>That player is not online.")));
                    return true;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                    if(amount <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    player.sendMessage(QTokens.format(ConfigManager
                            .getString("messages.invalidAmount","<red>Invalid amount.")));
                    return true;
                }
                final int amountStart = amount;
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
                if(amount == amountStart){
                    player.sendMessage(QTokens.format(ConfigManager.getString("messages.tookNone")));
                    return true;
                }
                if (amount > 0) {
                    player.sendMessage(QTokens.format(ConfigManager.getString("messages.tookNotEnough").replace("%player%", args[1]).replace("%amount%", String.valueOf(amount))));
                } else {
                    player.sendMessage(QTokens.format(ConfigManager.getString("messages.tookSuccess")));
                }
                return true;

            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length == 1){
            return commandList.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .filter(arg -> sender.hasPermission("qtokens.use."+arg)).toList();
        }
        if(args.length == 2){
            if(commandList.contains(args[0].toLowerCase())){
                if(args[0].contains("reload")) return List.of();
                if(sender.hasPermission("qtokens.use."+args[0].toLowerCase())){
                    return QTokens.getInstance().getServer()
                            .getOnlinePlayers()
                            .stream().map(Player::getName)
                            .filter(playerName -> playerName.toLowerCase().startsWith(args[1].toLowerCase()))
                            .toList();
                }
                return List.of();
            }
        }
        if(args.length == 3){
            if(args[0].contains("balance") || args[0].contains("reload")){
                return List.of();
            }
            if(commandList.contains(args[0].toLowerCase()) && sender.hasPermission("qtokens.use."+args[0].toLowerCase())){
                return List.of("amount");
            }
        }
        return List.of();
    }


}
