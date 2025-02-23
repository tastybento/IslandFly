package world.bentobox.islandfly.listeners;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.events.island.IslandEvent;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.islandfly.FlySettings;
import world.bentobox.islandfly.IslandFlyAddon;

public class FlyListener implements Listener {

    private final FlySettings settings;
    private final BentoBox plugin;

    public FlyListener(final IslandFlyAddon addon) {
        
        this.settings = addon.getSettings();
        this.plugin = addon.getPlugin();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onExitIsland(final IslandEvent.IslandExitEvent event) {
        
        // Check only when player exit is own island
        final UUID playerUUID = event.getPlayerUUID();
        
        if (!event.getIsland().getMembers().containsKey(playerUUID)) return;

        // Player already flying
        final User user = User.getInstance(playerUUID);
        
        if (!user.getPlayer().getAllowFlight()) return;
        // Bypass permission
        if (user.hasPermission(plugin.getIWM().getAddon(user.getWorld()).get().getPermissionPrefix() + "island.flybypass")) return;

        // Alert player fly will be disabled
        final int flyTimeout = settings.getFlyTimeout();
        
        user.sendMessage("islandfly.fly-outside-alert", TextVariables.NUMBER, String.valueOf(flyTimeout));
        
        // If timeout is 0 or less disable fly immediately
        if (flyTimeout <= 0) {
            disableFly(user);
            return;
        }
        
        // Else disable fly with a delay
        this.plugin.getServer().getScheduler().runTaskLater(plugin, ()-> {
            
            // Verify player is still online
            if(!user.isOnline()) return;
            
            final IslandsManager islands = plugin.getIslands();
            
            // Check player is not on his own island
            if (!(islands.userIsOnIsland(user.getWorld(), user)
            && islands.getIslandAt(user.getLocation()).get().getMembers().containsKey(playerUUID))) {
                disableFly(user);
            }
            else {
            	user.sendMessage("islandfly.cancel-disable");
            }
        }, 20L* settings.getFlyTimeout());
    }

    /**
     * Disable player fly and alert it
     * @param user
     */
    private void disableFly(final User user) {
        
        final Player player = user.getPlayer();
        
        player.setFlying(false);
        player.setAllowFlight(false);
        user.sendMessage("islandfly.disable-fly");
    }

}
