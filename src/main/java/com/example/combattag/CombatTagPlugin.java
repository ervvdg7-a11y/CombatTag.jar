package com.example.combattag;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CombatTagPlugin extends JavaPlugin implements Listener {
    private static final long COMBAT_MILLIS = 20_000L;
    private final Map<UUID, Long> combatExpiry = new HashMap<>();
    private BukkitTask timerTask;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        timerTask = Bukkit.getScheduler().runTaskTimer(this, this::updateTimers, 0L, 20L);
        getLogger().info("CombatTag enabled: 20-second combat timer.");
    }

    @Override
    public void onDisable() {
        if (timerTask != null) timerTask.cancel();
        combatExpiry.clear();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Player attacker = getPlayerAttacker(event.getDamager());
        if (attacker == null || attacker.equals(victim) || event.getFinalDamage() <= 0) return;
        tag(attacker);
        tag(victim);
    }

    private Player getPlayerAttacker(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile
                && projectile.getShooter() instanceof Player player) return player;
        return null;
    }

    private void tag(Player player) {
        combatExpiry.put(player.getUniqueId(), System.currentTimeMillis() + COMBAT_MILLIS);
    }

    private void updateTimers() {
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, Long> entry : new HashMap<>(combatExpiry).entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                combatExpiry.remove(entry.getKey());
                continue;
            }
            long remaining = entry.getValue() - now;
            if (remaining <= 0) {
                combatExpiry.remove(entry.getKey());
                player.sendActionBar(Component.text("Вы вышли из боя", NamedTextColor.GREEN));
            } else {
                long seconds = (remaining + 999L) / 1000L;
                player.sendActionBar(Component.text("⚔ В бою: " + seconds + " сек.", NamedTextColor.RED));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Long expiry = combatExpiry.remove(player.getUniqueId());
        if (expiry == null || expiry <= System.currentTimeMillis()) return;

        Location location = player.getLocation();
        PlayerInventory inventory = player.getInventory();
        dropItems(location, inventory.getStorageContents());
        dropItems(location, inventory.getArmorContents());

        ItemStack offhand = inventory.getItemInOffHand();
        if (offhand != null && !offhand.getType().isAir()) {
            location.getWorld().dropItemNaturally(location, offhand.clone());
        }

        inventory.clear();
        inventory.setArmorContents(new ItemStack[4]);
        inventory.setItemInOffHand(null);

        if (player.getHealth() > 0.0) player.setHealth(0.0);

        Bukkit.broadcast(Component.text(
                player.getName() + " вышел во время боя и потерял инвентарь!",
                NamedTextColor.RED));
    }

    private void dropItems(Location location, ItemStack[] items) {
        for (ItemStack item : items) {
            if (item != null && !item.getType().isAir()) {
                location.getWorld().dropItemNaturally(location, item.clone());
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        combatExpiry.remove(event.getEntity().getUniqueId());
    }
}
