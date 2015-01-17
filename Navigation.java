/*
Copyright (C) 2015  Alan J. Zaffetti

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
*/

package team309;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

import java.util.Stack;

import static team309.RobotPlayer.rc;

/**
 * Basic navigation operations for unit movement.
 */
public final class Navigation {

  /**
   * A static copy of the array of directional constants.
   */
  public static final Direction[] directions = Direction.values();

  /**
   * Tests if a direction is NONE or OMNI
   * @return `true' if a direction is NONE or OMNI; `false' otherwise.
   */
  public static boolean hasNoDirection(Direction d) {
    return d == null || d == Direction.NONE || d == Direction.OMNI;
  }

  /**
   * A convenience method for pathing; gets all directions which
   * make progress toward the given map location `loc'.
   *  !! This method is SAFE to use without any checks. !!
   * @param target a valid map location.
   * @return an array of valid directions; or `null'.
   */
  public static Direction[] getDirectionsTo(MapLocation target) {
    Direction d = rc.getLocation().directionTo(target);
    return getDirectionsLike(d);
  }

  /**
   * Gets all directions "in the same pathing order" as `d'.
   *   !! This method is SAFE to use without any checks. !!
   * @param d a valid direction; or `Direction.NONE'.
   * @return an array of directions "like" `d'.
   */
  public static Direction[] getDirectionsLike(Direction d) {
    return new Direction[] {
      d, d.rotateLeft(), d.rotateRight(),
      d.rotateLeft().rotateLeft(),
      d.rotateRight().rotateRight()
    };
  }

  public static double squareDistance(MapLocation loc1, MapLocation loc2) {
    return (loc1.x-loc2.x)*(loc1.x-loc2.x) + (loc1.y-loc2.y)*(loc1.y-loc2.y);
  }
}
