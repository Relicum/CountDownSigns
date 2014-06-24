package org.codemine.countdownsigns.Effects;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.lang.reflect.Method;
import java.util.Random;

/**
 * FireworkEffects Created: 21 June 2014
 * <p/>
 * FireworkEffects provides a thread-safe and (reasonably) version independent way to instantly explode a FireworkEffect at a given location. You are welcome to use, redistribute,
 * modify and destroy your own copies of this source with the following conditions:
 * <p/>
 * 1. No warranty is given or implied. 2. All damage is your own responsibility. 3. You provide credit publicly to the original source should you release the plugin.
 *
 * @author codename_B and Relicum
 * @version 0.0.1
 */
public class FireworkEffects {

    private Method worldHandler=null;
    private Method nmsWorldBroadcastEntityEffect=null;
    private Method fireworkHandler=null;
    private Random random=new Random(6543647);
    /**
     * Internal method, used as shorthand to grab our method in a nice friendly manner
     *
     * @param clazz the class you want the method from
     * @param method the method you want in string format
     * @return Method (or null)
     */
    private static Method getMethod(Class<?> clazz, String method) {
        for(Method m : clazz.getMethods()) {
            if(m.getName().equals(method)) {
                return m;
            }
        }
        return null;
    }
    public void playFirework(World world, Location loc, FireworkEffect fe) throws Exception {

        Firework fw=world.spawn(loc, Firework.class);

        Object nmsWorld=null;
        Object nmsFirework=null;

        if(worldHandler == null) {

            worldHandler=getMethod(world.getClass(), "getHandle");
            fireworkHandler=getMethod(fw.getClass(), "getHandle");
        }

        nmsWorld=worldHandler.invoke(world, (Object[]) null);
        nmsFirework=fireworkHandler.invoke(fw, (Object[]) null);

        if(nmsWorldBroadcastEntityEffect == null) {

            nmsWorldBroadcastEntityEffect=getMethod(nmsWorld.getClass(), "broadcastEntityEffect");
        }

        //Load firework metadata
        FireworkMeta data=fw.getFireworkMeta();

        //clear firework effects to allow reuse
        data.clearEffects();
        //Set power of firework to 1
        data.setPower((random.nextInt(200) + 101) / 100);
        //add the effect
        data.addEffect(fe);

        fw.setFireworkMeta(data);

        /*
         * Now broadcast the firework and kill the firework object
         *
         */

        nmsWorldBroadcastEntityEffect.invoke(nmsWorld, new Object[]{nmsFirework , (byte) 17});

        //Remove the object from game
        fw.remove();
    }

}
