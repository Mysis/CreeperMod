package io.github.mysis.creepermod;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public final class CreeperMod extends JavaPlugin implements Listener {

    public static List<UUID> creeperFallDamageResist = new CopyOnWriteArrayList<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof Creeper) {
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            final Player player = (Player) entity; //declared as final so that it can be used in anonymous BukkitRunnable
            //getLogger().info("player damaged!");
            if (event.getDamager() instanceof Creeper) {
                getLogger().info("player damaged by creeper!");

                getLogger().info("player health: " + player.getHealth());
                getLogger().info("event damage: " + event.getDamage());
                getLogger().info("final damage: " + event.getFinalDamage());
                getLogger().info("damage reduction: " + (1 - event.getFinalDamage() / event.getDamage()));

                getLogger().info("armor: " + player.getAttribute(Attribute.GENERIC_ARMOR).getValue());
                //getLogger().info("armor toughness: " + player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue());
                //getLogger().info("knockback resistance: " + player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).getValue());

                if (player.getHealth() >= 19 && player.getAttribute(Attribute.GENERIC_ARMOR).getValue() >= 15) {
                    getLogger().info("player had 19 or more health and 15 or more armor");

                    if (player.getHealth() - event.getFinalDamage() < 1) {
                        event.setDamage(0);
                        player.setHealth(1);
                        getLogger().info("saved player from death");
                    }

                    creeperFallDamageResist.add(player.getUniqueId());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            creeperFallDamageResist.remove(player.getUniqueId());
                            getLogger().info("player is no longer resistant to fall damage");
                        }
                    }.runTaskLater(this, 60);
                    getLogger().info("player is resistant to fall damage for 60 ticks");
                } else {
                    getLogger().info("player did not have 19 or more health or did not have 15 armor");
                }
            }
        } else if (entity instanceof AbstractHorse) {
            AbstractHorse horse = (AbstractHorse) entity;
            /*
            getLogger().info("speed base value: " + horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue());
            getLogger().info("speed value: " + horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue());
            getLogger().info("jump base value: " + horse.getAttribute(Attribute.HORSE_JUMP_STRENGTH).getBaseValue());
            getLogger().info("jump value: " + horse.getAttribute(Attribute.HORSE_JUMP_STRENGTH).getValue());
            getLogger().info("getJumpStrength value: " + horse.getJumpStrength());
            getLogger().info("health base value: " + horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            getLogger().info("health value: " + horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            */

            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                    if (horse.isTamed()) {
                        double health = horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 2;
                        double speed = Math.round(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue() * 430) / 10.0;
                        double jumpRaw = horse.getAttribute(Attribute.HORSE_JUMP_STRENGTH).getValue();
                        double jump = Math.round((-0.1817584952 * Math.pow(jumpRaw, 3) + 3.689713992 * Math.pow(jumpRaw, 2) + 2.128599134 * jumpRaw - 0.343930367) * 10) / 10.0;
                        horse.setCustomName("H:" + health + " S:" + speed + " J:" + jump);
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        //getLogger().info("onEntityDamageEvent called!");
        if (entity instanceof Player) {
            Player player = (Player) event.getEntity();
            //getLogger().info("entity is player");
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                //getLogger().info("damage caused by fall");
                for (UUID id : creeperFallDamageResist) {
                    if (id.compareTo(event.getEntity().getUniqueId()) == 0) {
                        getLogger().info("player is resistant to fall");
                        if (event.getDamage() <= 10) {
                            if (player.getHealth() - event.getFinalDamage() < 1) {
                                event.setDamage(0);
                                player.setHealth(1);
                                getLogger().info("saved player from death");
                            }
                        } else {
                            getLogger().info("damage too high to protect player");
                        }
                        /*
                        if (event.getDamage() > 10) {
                            event.setDamage(event.getDamage() - 10);
                        } else {
                            event.setDamage(0);
                        }
                        return;
                        */
                    }
                }
            }
        }
    }
}
