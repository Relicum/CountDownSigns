package org.codemine.countdownsigns;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.codemine.countdownsigns.Effects.FireworkEffects;
import org.codemine.countdownsigns.Effects.ParticleEffect;
import org.codemine.schedule.BukkitRun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.logging.Level;

/**
 * Name: Explosion.java Created: 20 June 2014
 *
 * @author Relicum
 * @version 0.0.1
 */
public class Explosion extends BukkitRun {

    private final CDS plugin;
    private final List<Entity> chicks=Collections.synchronizedList(new ArrayList<Entity>());
    protected BukkitTask innerRun;
    protected boolean hasRun=false;
    private BlockState sign;
    private BlockState block;
    private boolean sound;
    private boolean explosion;
    private boolean effect;
    private Random random=new Random(1635787324);
    private boolean isOdd;
    private FireworkEffects fw=new FireworkEffects();
    private final Location location;

    public Explosion(CDS pl, BlockState sign, BlockState block, boolean sound, boolean explosion, boolean effect) {
        plugin=pl;
        this.sign=sign;
        this.block=block;
        this.sound=sound;
        this.explosion=explosion;
        this.effect=effect;
        this.location=new Location(sign.getWorld(), sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ(), Direction.NORTH.asFloat(), 0);
    }
    /**
     * It will return players in a radius, from location.
     *
     * @param location Initial location
     * @param radius distance from the "location" that will return all the entities from each block;
     * @return HashSet(Player) credits skore87
     */
    public static HashSet<Player> getNearbyEntities(Location location, int radius) {

        int chunkRadius=radius < 16 ? 1 : (radius - (radius % 16)) / 16;
        HashSet<Player> radiusEntities=new HashSet<>();

        for(int chX=0 - chunkRadius ; chX <= chunkRadius ; chX++) {
            for(int chZ=0 - chunkRadius ; chZ <= chunkRadius ; chZ++) {
                int x=(int) location.getX(), y=(int) location.getY(), z=(int) location.getZ();
                for(Entity e : new Location(location.getWorld(), x + (chX * 16), y, z + (chZ * 16)).getChunk().getEntities()) {
                    if(e.getLocation().distance(location) <= radius && e.getLocation().getBlock() != location.getBlock()) {
                        if(e instanceof LivingEntity) {
                            if(e instanceof Player) {
                                radiusEntities.add((Player) e);
                            }

                        }
                    }
                }
            }
        }
        return radiusEntities;
    }
    @Override
    public void run() {
        sign.getWorld().setStorm(true);
        sign.getWorld().setThundering(true);
        sign.getWorld().setThunderDuration(160);
        ParticleEffect.WITCH_MAGIC.display(sign.getLocation(), 0.0f, 1.0f, 0.0f, 1.9f, 40);
        //ParticleEffect.RED_DUST.display(sign.getLocation().add(-1, 2, -1), 0.0f, 0.0f, 0.0f, 0.9f, 40);
        // ParticleEffect.RED_DUST.display(sign.getLocation().add(1, 2, -1), 0.0f, 0.0f, 0.0f, 0.9f, 40);
        //ParticleEffect.RED_DUST.display(sign.getLocation().add(-1, 2, 1), 0.0f, 0.0f, 0.0f, 0.9f, 40);
        PotionEffect potionEffect=PotionEffectType.BLINDNESS.createEffect(100, 1);

        for(Player player : Bukkit.getOnlinePlayers()) {

            player.playSound(player.getLocation(), Sound.ZOMBIE_METAL, 20.0f, 1.0f);
            player.addPotionEffect(potionEffect, true);
        }
        //sign.getLocation().getWorld().playSound(sign.getLocation(), Sound.ZOMBIE_METAL, 20.0f, 1.0f);
        innerRun=new BukkitRun() {

            int count=400;
            boolean isNeeded=true;

            @Override
            public void run() {
                if(count < 0) {
                    hasRun=true;
                }
                if(!hasRun) {

                    int rand=random.nextInt(100) + 1;
                    if(rand == 101) rand=100;
                    int min=random.nextInt(4) + 1;
                    System.out.println("Min is set to " + min);
                    int x=1;
                    int y=1;
                    int z=1;
                    if(min == 1) {
                        x=random.nextInt(6);
                        if(x == 0) {
                            x=1;
                        }
                        z=random.nextInt(6);
                        y=random.nextInt(6);
                        location.setYaw(Direction.NORTH.asFloat());
                    }
                    if(min == 2) {
                        x=random.nextInt(6);
                        if(x == 0) {
                            x=1;
                        }
                        z=random.nextInt(6);
                        if(z == 0) {
                            z=1;
                        }
                        y=random.nextInt(6);
                        location.setYaw(Direction.EAST.asFloat());
                    }
                    if(min == 3) {
                        x=random.nextInt(6);
                        if(x == 0) {
                            x=1;
                        }
                        z=random.nextInt(6);
                        if(z == 0) {
                            z=1;
                        }
                        y=random.nextInt(6);

                        location.setYaw(Direction.SOUTH.asFloat());
                    }
                    if(min == 4) {
                        x=random.nextInt(6);
                        if(x == 0) {
                            x=1;
                        }
                        z=random.nextInt(6);
                        y=random.nextInt(6);
                        location.setYaw(Direction.WEST.asFloat());
                    }
                    //launch a chicken 30% of the time
                    if((100 % rand) < 40) {
                        Entity entity=sign.getWorld().spawnEntity(sign.getLocation().add(x, y, z), EntityType.CHICKEN);
                        entity.setVelocity(entity.getLocation().getDirection().multiply(1.1d).setY(1.8d));
                        System.out.println("Chick is spawned with a yaw of " + entity.getLocation().getYaw() + " ID: " + entity.getUniqueId().toString());
                        chicks.add(entity);
                    }
                    //Launch firework the other 70%
                    else {

                        try {
                            fw.playFirework(sign.getWorld(), sign.getLocation().add(x, y + 15d, z), getFireworkEffect());
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }

                    sign.getWorld().strikeLightningEffect(sign.getLocation());
                    ParticleEffect.HUGE_EXPLOSION.display(sign.getLocation().add(x, y, z), 0.0f, 0.5f, 0.0f, 1.5f, 10);
                    ParticleEffect.ANGRY_VILLAGER.display(sign.getLocation().add(x, y, z), 0.0f, 5.0f, 0.0f, 1.0f, 50);

                    count-=5;

                }

                else {

                    for(Player player : Bukkit.getOnlinePlayers()) {

                        player.playSound(player.getLocation(), Sound.WITHER_DEATH, 20.0f, 1.0f);
                    }
                    // sign.getLocation().getWorld().playSound(sign.getLocation(), Sound.WITHER_DEATH, 20.0f, 1.0f);
                    MessageUtil.logInfoFormatted("Should now be running the end of inner loop code as count is less than -1");
                    sign.setType(Material.AIR);
                    sign.update(true);
                    block.setType(Material.AIR);
                    block.update(true);
                    //block.getWorld().createExplosion(block.getLocation().getX(), block.getLocation().getY(), block.getLocation().getZ(), 2.0f, false, false);
                    ParticleEffect.CRIT.display(block.getLocation(), 0, 1, 0, 1.2f, 50);

                    if(!chicks.isEmpty()) {
                        //Remove all the chicken entities
                        synchronized(chicks) {
                            ListIterator<Entity> iterator=chicks.listIterator();

                            try {
                                while(iterator.hasNext()) {
                                    removeChick(iterator.next());
                                    iterator.remove();

                                }
                            }
                            catch(Exception e) {
                                MessageUtil.logServereFormatted("Exception thrown while trying to remove chickens");
                                e.printStackTrace();
                                return;
                            }

                            MessageUtil.logDebug(Level.INFO, "Entities marked for removal");

                        }
                        MessageUtil.logDebug(Level.INFO, "Seems that there are still entities lin the list, trying to remove them now");

                    }

                    cancelInnerRun();
                }
                isNeeded=true;

            }

        }.runTaskTimer(plugin, 100l, 5l);

        sign.getLocation().getWorld().playSound(sign.getLocation(), Sound.WITHER_SHOOT, 20.0f, 1.0f);
        sign.getWorld().setStorm(false);
        sign.getWorld().setThundering(false);
        sign.getWorld().setThunderDuration(0);

        try {
            plugin.getConfig().getConfigurationSection("sign").set("signtimer", null);
            plugin.saveConfig();
            plugin.reloadConfig();
        }
        catch(Exception e) {
            MessageUtil.logDebug(Level.SEVERE, "Error thrown when trying to delete settings from Explosion BukRun");
            e.printStackTrace();
        }

        MessageUtil.logInfoFormatted("Now testing of Running sign is running it couldn't be but the task will be");
        if(!plugin.getTask().isRunning()) {
            plugin.getTask().cancelTheTask();
            MessageUtil.logInfoFormatted("RunningSign task should now of stopped");
        }
        plugin.setTaskToNull();

    }

    public void removeChick(Entity chicky) {

        if(chicky.getType().equals(EntityType.CHICKEN)) {
            if(chicky.isValid()) {
                chicky.remove();
            }
            else {
                MessageUtil.logWarningFormatted("Chicken has died with id of " + chicky.getUniqueId().toString());
                MessageUtil.logWarningFormatted("Tried to mark same entity for remove LET RELICUM KNOW");
            }

        }
    }
    public void cancelInnerRun() {
        if(innerRun != null) {
            innerRun.cancel();
            MessageUtil.logDebug(Level.INFO, "The inner runnable in explosion should now be canceled");
            innerRun=null;

        }

        else {
            MessageUtil.logDebug(Level.SEVERE, "Unable to stop the inner runnable");

        }
    }

    public FireworkEffect getFireworkEffect() {

        // Get the type
        int rt=random.nextInt(5) + 1;
        FireworkEffect.Type type=FireworkEffect.Type.BALL;
        if(rt == 1) {
            type=FireworkEffect.Type.BALL;
        }
        if(rt == 2) {
            type=FireworkEffect.Type.BALL_LARGE;
        }
        if(rt == 3) {
            type=FireworkEffect.Type.BURST;
        }
        if(rt == 4) {
            type=FireworkEffect.Type.CREEPER;
        }
        if(rt == 5) {
            type=FireworkEffect.Type.STAR;
        }

        // Get our random colors
        int r1i=random.nextInt(255) + 1;
        int r2i=random.nextInt(255) + 1;
        int r3i=random.nextInt(255) + 1;
        Color c1=Color.fromRGB(r1i, r2i, r3i);
        Color c2=Color.fromRGB(r1i, r2i, r3i);

        // Create our effect with this
        return FireworkEffect.builder().flicker(random.nextBoolean())
                             .withColor(c1).withFade(c2).with(type)
                             .trail(random.nextBoolean()).build();

    }

}
