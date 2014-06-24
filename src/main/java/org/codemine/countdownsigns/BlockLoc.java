package org.codemine.countdownsigns;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple Serialized Block Location Class
 *
 * @author Relicum
 * @version 0.0.1
 */
@EqualsAndHashCode
@SerializableAs("BlockLoc")
public class BlockLoc implements ConfigurationSerializable {

    @Getter
    private int X;
    @Getter
    @Setter
    private int Y;
    @Getter
    @Setter
    private int Z;
    @Getter
    @Setter
    private String world;

    private String name;

    /**
     * P Instantiates a new Block loc.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @param world the world
     */
    public BlockLoc(int x, int y, int z, String world) {
        Validate.notNull(x, "X Cord can not be empty when setting block loc");
        Validate.notNull(y, "Y Cord can not be empty when setting block loc");
        Validate.notNull(z, "Z Cord can not be empty when setting block loc");
        Validate.notNull(world, "World can not be null when setting block lock");
        this.X=x;
        this.Y=y;
        this.Z=z;
        this.world=world;
    }

    // deserialize the object
    public static BlockLoc deserialize(Map<String,Object> map) {
        Object objX=map.get("X"),
          objY=map.get("Y"),
          objZ=map.get("Z"),
          objW=map.get("world");
        if(objX == null || objY == null || objZ == null || objW == null) {
            throw new NullPointerException("One of the values in Block is null while trying to deserialize the class");
        }
        return new BlockLoc((Integer) objX, (Integer) objY, (Integer) objZ, (String) objW);

    }

    //Get full location
    public Location getLocation() {
        return new Location(Bukkit.getWorld(getWorld()), getX(), getY(), getZ());
    }

    //serialize the object
    @Override
    public Map<String,Object> serialize() {
        Map<String,Object> map=new HashMap<>(4);
        map.put("X", getX());
        map.put("Y", getY());
        map.put("Z", getZ());
        map.put("world", getWorld());
        return map;

    }

    @Override
    public String toString() {
        return "world:" + getWorld() + "," + "X:" + getX() + "," + "Y:" + getY() + "," + "Z:" + getZ();

    }
}
