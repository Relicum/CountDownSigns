package org.codemine.countdownsigns;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * SignTimer is a Object model representing the Countdown and Effects.
 *
 * @author Relicum
 * @version 0.0.1
 */
@AllArgsConstructor
@SerializableAs("SignTimer")
public class SignTimer implements ConfigurationSerializable {

    @Getter
    @Setter
    private String[] lines = new String[4];
    @Getter
    @Setter
    private String identifier;
    @Getter
    @Setter
    private long updatePeriod;
    @Getter
    @Setter
    private long timeLeft;
    @Getter
    @Setter
    private boolean paused;
    @Getter
    @Setter
    private boolean completed;
    @Getter
    @Setter
    private boolean explosion;
    @Getter
    @Setter
    private boolean useSound;
    @Getter
    @Setter
    private boolean useEffect;
    @Getter
    @Setter
    private BlockLoc signLocation;
    @Getter
    @Setter
    private BlockLoc explodingLocation;

    public SignTimer() {
        paused = true;
        explosion = true;
        useSound = true;
        useEffect = true;
        completed = false;
    }

    // deserialize the object
    public static SignTimer deserialize(Map<String, Object> map) {
        Object objLines = map.get("lines"),
                objId = map.get("identifier"),
                objUpdate = map.get("updatePeriod"),
                objLeft = map.get("timeLeft"),
                objPause = map.get("paused"),
                objComp = map.get("completed"),
                objExp = map.get("explosion"),
                objSound = map.get("useSound"),
                objEffect = map.get("useEffect"),
                objSign = map.get("signLocation"),
                objExpLoc = map.get("explodingLocation");
        if (objLines == null || objId == null || objUpdate == null || objLeft == null || objPause == null || objComp == null
                || objExp == null || objSound == null || objEffect == null || objSign == null || objExpLoc == null) {
            throw new NullPointerException("One of the values in SignTimer is null while trying to deserialize the class");
        }
        @SuppressWarnings("unchecked")
        ArrayList<String> li = (ArrayList<String>) objLines;
        return new SignTimer(li.toArray(new String[4]), (String) objId, (Integer) objUpdate, (Integer) objLeft,
                (boolean) objPause, (boolean) objComp, (boolean) objExp, (boolean) objSound, (boolean) objEffect, (BlockLoc) objSign, (BlockLoc) objExpLoc);
    }

    public void setLine(int index, String value) {
        if (index < 0 || index > 4) {
            throw new IllegalArgumentException("Index just be between 0-3");
        }
        if (value.length() > 16) {
            this.lines[index] = value.substring(0, 15);
        } else {
            this.lines[index] = value;
        }
    }

    public String getLine(int index) {
        if (index < 0 || index > 4) {
            throw new IllegalArgumentException("Index just be between 0-3");
        }
        return ChatColor.translateAlternateColorCodes('&', this.lines[index]);
    }

    //Subtracts the update period from the time remaining
    public void decrementTimeLeft() {
        timeLeft -= updatePeriod;
    }

    //serialize the object
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>(11);
        map.put("lines", getLines());
        map.put("identifier", getIdentifier());
        map.put("updatePeriod", getUpdatePeriod());
        map.put("timeLeft", getTimeLeft());
        map.put("paused", isPaused());
        map.put("completed", isCompleted());
        map.put("explosion", isExplosion());
        map.put("useSound", isUseSound());
        map.put("useEffect", isUseEffect());
        map.put("signLocation", getSignLocation());
        map.put("explodingLocation", getExplodingLocation());
        return map;
    }

    @Override
    public String toString() {
        return "SignTimer{" +
                "lines=" + Arrays.toString(lines) +
                ", identifier='" + identifier + '\'' +
                ", updatePeriod=" + updatePeriod +
                ", timeLeft=" + timeLeft +
                ", paused=" + paused +
                ", completed=" + completed +
                ", explosion=" + explosion +
                ", useSound=" + useSound +
                ", useEffect=" + useEffect +
                ", signLocation=" + signLocation +
                ", explodingLocation=" + explodingLocation +
                '}';
    }


}
