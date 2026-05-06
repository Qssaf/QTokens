package dev.parrotstudios.qTokens;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class EventManager implements Listener {

    @EventHandler
    public void onPlace(PlayerInteractEvent event){
        if(!event.getAction().isRightClick()) return;
        if(event.getItem() == null) return;
        if(!QTokens.isCoin(event.getItem())) return;
        event.setCancelled(true);
    }
}
