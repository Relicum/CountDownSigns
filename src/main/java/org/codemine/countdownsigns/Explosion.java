package org.codemine.countdownsigns;

import org.bukkit.*;
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
import org.codemine.schedule.TimeUnit;

import java.util.*;
import java.util.logging.Level;

/**
 * Explosion provides the effect once the count down has reached zero.
 *
 * @author Relicum
 * @version 0.0.1
 */
public class Explosion extends BukkitRun {

    private final CDS plugin;

    private final List<Entity> chicks = Collections.synchronizedList(new ArrayList<Entity>());

    private final Location location;

    private final PotionEffect potionEffect = PotionEffectType.BLINDNESS.createEffect(380, 1);

    protected BukkitTask innerRun;

    protected boolean hasRun = false;

    protected boolean mainRunning = false;

    protected boolean innerRunning = false;

    private BlockState sign;

    private BlockState block;

    private boolean sound;

    private boolean explosion;

    private boolean effect;

    private Random random = new Random(1635787324);

    private FireworkEffects fw = new FireworkEffects();


    public Explosion(CDS pl, BlockState sign, BlockState block, boolean sound, boolean explosion, boolean effect) {
        plugin = pl;
        this.sign = sign;
        this.block = block;
        this.sound = sound;
        this.explosion = explosion;
        this.effect = effect;
        this.location = new Location(sign.getWorld(), sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ(), Direction.NORTH.asFloat(), 0);
        this.runTaskLater(plugin, 1l, TimeUnit.TICKS);
    }


    /**
     * It will return players in a radius, from location. <p>This method returns a synchronized Iterator from a HashSet of {@link org.bukkit.entity.Player}. To avoid any {@link
     * java.util.ConcurrentModificationException} Exceptions.
     *
     * @param location Initial location
     * @param radius   distance from the "location" that will return all the entities from each block;
     * @return synchronizedSet HashSet(Player) credits skore87 and Relicum
     */
    public static Iterator<Player> getNearbyEntities(Location location, int radius) {

        int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16)) / 16;
        Set<Player> radiusEntities = new HashSet<>();

        for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++) {
            for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
                int x = (int) location.getX(), y = (int) location.getY(), z = (int) location.getZ();
                for (Entity e : new Location(location.getWorld(), x + (chX * 16), y, z + (chZ * 16)).getChunk().getEntities()) {
                    if (e.getLocation().distance(location) <= radius && e.getLocation().getBlock() != location.getBlock()) {
                        if (e instanceof LivingEntity) {
                            if (e instanceof Player) {
                                radiusEntities.add((Player) e);
                            }

                        }
                    }
                }
            }
        }
        return Collections.synchronizedSet(radiusEntities).iterator();
    }


    //This is the main Outer Loop. Any threads starting inside this loop should be cancel
    //Either from this loop or outside of the main loop.
    @Override
    public void run() {
        setMainRunning(true);
        //plugin.setCountdownTaskToNull();
        sign.getWorld().setStorm(true);
        sign.getWorld().setThundering(true);
        sign.getWorld().setThunderDuration(160);

        //ParticleEffect.WITCH_MAGIC.display(sign.getLocation(), 0.0f, 1.0f, 0.0f, 1.9f, 40);
        ParticleEffect.RED_DUST.display(sign.getLocation(), 1.4f, 3.0f, -1.4f, 2.9f, 40);
        //ParticleEffect.RED_DUST.display(sign.getLocation(), -3.0f, 6.0f, 3.0f, 3.9f, 40);
        //ParticleEffect.RED_DUST.display(sign.getLocation(), 1.0f, 8.0f, -1.0f, 4.9f, 40);

        final Iterator<Player> playerIterator = getNearbyEntities(sign.getLocation(), 16);

        while (playerIterator.hasNext()) {

            playerItRun(playerIterator.next());
        }

        //This is start of the repeating loop for the explosion effect.
        innerRun = new BukkitRun() {

            int count = 400;

            boolean isNeeded = true;


            @Override
            public void run() {
                if (count == 400) {
                    setInnerRunning(true);
                }
                if (count < -5) {
                    setInnerRunning(false);
                    hasRun = true;
                }
                setInnerRunning(true);
                if (!hasRun) {

                    int rand = random.nextInt(100) + 1;
                    if (rand == 101) rand = 100;
                    int min = random.nextInt(4) + 1;
                    //System.out.println("Min is set to " + min);
                    int x = 1;
                    int y = 1;
                    int z = 1;
                    if (min == 1) {
                        x = random.nextInt(6);
                        if (x == 0) {
                            x = 1;
                        }
                        z = random.nextInt(6);
                        y = random.nextInt(6);
                        location.setYaw(Direction.NORTH.asFloat());
                    }
                    if (min == 2) {
                        x = random.nextInt(6);
                        if (x == 0) {
                            x = 1;
                        }
                        z = random.nextInt(6);
                        if (z == 0) {
                            z = 1;
                        }
                        y = random.nextInt(6);
                        location.setYaw(Direction.EAST.asFloat());
                    }
                    if (min == 3) {
                        x = random.nextInt(6);
                        if (x == 0) {
                            x = 1;
                        }
                        z = random.nextInt(6);
                        if (z == 0) {
                            z = 1;
                        }
                        y = random.nextInt(6);

                        location.setYaw(Direction.SOUTH.asFloat());
                    }
                    if (min == 4) {
                        x = random.nextInt(6);
                        if (x == 0) {
                            x = 1;
                        }
                        z = random.nextInt(6);
                        y = random.nextInt(6);
                        location.setYaw(Direction.WEST.asFloat());
                    }
                    //launch a chicken 30% of the time
                    if ((100 % rand) < 20) {
                        Entity entity = sign.getWorld().spawnEntity(sign.getLocation().add(x, y, z), EntityType.CHICKEN);
                        entity.teleport(location.add(x, y, z));
                        entity.setVelocity(entity.getLocation().getDirection().setY(1.8d));
                        //System.out.println("Chick is spawned with a yaw of " + entity.getLocation().getYaw() + " ID: " + entity.getUniqueId().toString());
                        chicks.add(entity);
                    }
                    //Launch firework the other 70%
                    else {

                        try {
                            fw.playFirework(sign.getWorld(), sign.getLocation().add(x / 5.0f, y + 10.0d, z / 5.0f), getFireworkEffect());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    sign.getWorld().strikeLightningEffect(sign.getLocation().add(x / 20.0f, 0.5f, z / 20.0f));
                    ParticleEffect.LARGE_EXPLODE.display(sign.getLocation(), x / 15f, 1.5f, z / 15f, 1.5f, 40);
                    ParticleEffect.MOB_SPELL.display(sign.getLocation(), x / 10f, 5, z / 10f, 1.9f, 40);

                    count -= 5;

                } else {
                    while (playerIterator.hasNext()) {

                        playerItRun(playerIterator.next());
                    }
                    if (!chicks.isEmpty()) {
                        //Remove all the chicken entities
                        synchronized (chicks) {
                            ListIterator<Entity> iterator = chicks.listIterator();

                            try {
                                while (iterator.hasNext()) {
                                    removeChick(iterator.next());
                                    iterator.remove();

                                }
                            } catch (Exception e) {
                                MessageUtil.logServereFormatted("Exception thrown while trying to remove chickens");
                                e.printStackTrace();
                                return;
                            }
                            if (plugin.isDebug()) {
                                MessageUtil.logDebug(Level.INFO, "Entities successfully marked for removal");
                            }
                        }

                    }

                    for (Player player : plugin.getServer().getOnlinePlayers()) {

                        player.playSound(player.getLocation(), Sound.WITHER_DEATH, 20.0f, 1.0f);
                    }
                    // sign.getLocation().getWorld().playSound(sign.getLocation(), Sound.WITHER_DEATH, 20.0f, 1.0f);
                    if (plugin.isDebug()) {
                        MessageUtil.logDebug(Level.INFO, "Should now be running the end of inner loop code as count is less than -1");
                    }
                    sign.setType(Material.AIR);
                    sign.update(true);
                    block.setType(Material.AIR);
                    block.update(true);
                    plugin.unregisterEvent();
                    cancelInnerRun();
                }
                isNeeded = true;

            }

        }.runTaskTimer(plugin, 100l, 5l);
        if (hasRun) {

            sign.getLocation().getWorld().playSound(location, Sound.WITHER_SHOOT, 20.0f, 1.0f);
            ParticleEffect.HEART.display(location.add(0.3, 0.1, 0.3), 16.0d, 0.0f, 2.0f, 0.0f, 0.12f, 50);
            sign.getWorld().setStorm(false);
            sign.getWorld().setThundering(false);
            sign.getWorld().setThunderDuration(0);

            try {
                plugin.getConfig().getConfigurationSection("sign").set("signtimer", null);
                plugin.saveConfig();
                plugin.reloadConfig();
            } catch (Exception e) {
                MessageUtil.logDebug(Level.SEVERE, "Error thrown when trying to delete settings from Explosion BukRun");
                e.printStackTrace();
            }
        }
    }


    public void removeChick(Entity chicky) {

        if (chicky.getType().equals(EntityType.CHICKEN)) {
            if (chicky.isValid()) {
                chicky.remove();
            } else {
                MessageUtil.logWarningFormatted("Chicken has died with id of " + chicky.getUniqueId().toString());
                MessageUtil.logWarningFormatted("Tried to mark same entity for remove LET RELICUM KNOW");
            }

        }
    }


    public void cancelInnerRun() {
        if (innerRun != null) {
            innerRun.cancel();
            MessageUtil.logDebug(Level.INFO, "The inner runnable in explosion should now be canceled");
            innerRun = null;

        } else {
            MessageUtil.logDebug(Level.SEVERE, "Unable to stop the inner runnable");

        }
    }


    public FireworkEffect getFireworkEffect() {

        // Get the type
        int rt = random.nextInt(5) + 1;
        FireworkEffect.Type type = FireworkEffect.Type.BALL;
        if (rt == 1) {
            type = FireworkEffect.Type.BALL;
        }
        if (rt == 2) {
            type = FireworkEffect.Type.BALL_LARGE;
        }
        if (rt == 3) {
            type = FireworkEffect.Type.BURST;
        }
        if (rt == 4) {
            type = FireworkEffect.Type.CREEPER;
        }
        if (rt == 5) {
            type = FireworkEffect.Type.STAR;
        }

        // Get our random colors
        int r1i = random.nextInt(255) + 1;
        int r2i = random.nextInt(255) + 1;
        int r3i = random.nextInt(255) + 1;
        Color c1 = Color.fromRGB(r1i, r2i, r3i);
        Color c2 = Color.fromRGB(r1i, r2i, r3i);

        // Create our effect with this
        return FireworkEffect.builder().flicker(random.nextBoolean())
                .withColor(c1).withFade(c2).with(type)
                .trail(random.nextBoolean()).build();

    }


    public synchronized boolean playerItRun(Player player) {

        try {

            player.playSound(player.getLocation(), Sound.ZOMBIE_METAL, 20.0f, 1.0f);

            player.addPotionEffect(potionEffect, true);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Gets if the current task is running. <p>This method is synchronized to make it thread save. (Provided all access to the value is done using this method)
     *
     * @return Value of isRunning. True and the main task is currently running.
     */
    public synchronized boolean isMainRunning() {
        return mainRunning;
    }


    /**
     * Sets task status to running mainRunning. <p>This method is synchronized to make it thread save. (Provided all access to the value is done using this method)
     *
     * @param mainRunning the boolean value, true sets the my task status to running(It does not start the task just sets the status)
     */
    public synchronized void setMainRunning(boolean mainRunning) {
        this.mainRunning = mainRunning;
    }


    /**
     * Sets the inner task status to running. <p>This method is synchronized to make it thread save. (Provided all access to the value is done using this method)
     *
     * @param innerRunning the boolean value, true sets the my inner task status to running(It does not start the inner task just sets the status)
     */
    public synchronized void setInnerRunning(boolean innerRunning) {
        this.innerRunning = innerRunning;
    }


    /**
     * Gets if the inner task is running. <p>This method is synchronized to make it thread save. (Provided all access to the value is done using this method)
     *
     * @return Value of innerRunning. True and the inner task is currently running.
     */
    public synchronized boolean innerRunning() {
        return innerRunning;
    }


    public class ExplosionInner extends BukkitRun {

        @Override
        public void run() {

        }
    }
}
