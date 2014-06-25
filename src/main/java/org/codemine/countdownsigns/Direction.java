package org.codemine.countdownsigns;

/**
 * Name: Direction.java Created: 23 June 2014
 *
 * @author Relicum
 * @version 0.0.1
 */
public enum Direction {

    NORTH(179.0f),
    EAST(90.0f),
    SOUTH(0.0f),
    WEST(88.0f);

    private final float yaw;

    private Direction(float paramYaw) {
        this.yaw = paramYaw;
    }

    public float asFloat() {
        return yaw;
    }
}
