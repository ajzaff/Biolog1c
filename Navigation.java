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

import static battlecode.common.RobotType.HQ;
import static battlecode.common.RobotType.TOWER;

public class Navigation {
  /**
   * A static copy of the array of directional constants.
   */
  public static final Direction[] directions = Direction.values();

  /**
   * Tests if a direction is NONE or OMNI
   * @return `true' if a direction is NONE or OMNI; `false' otherwise.
   */
  static boolean hasNoDirection(Direction d) {
    return d == Direction.NONE || d == Direction.OMNI;
  }

  /**
   * Returns the direction to an enemy tower in range.
   * @return a direction to an enemy tower or NONE, OMNI
   */
  private static Direction compositeEnemyStructureDirection(MapLocation myLocation) {

    MapLocation loc = myLocation;

    if(myLocation.distanceSquaredTo(RobotPlayer.enemyHQ) < HQ.sensorRadiusSquared + 4) {
      loc = loc.add(myLocation.directionTo(RobotPlayer.enemyHQ));
    }

    for(MapLocation tower : RobotPlayer.enemyTowers) {
      if(myLocation.distanceSquaredTo(tower) < TOWER.sensorRadiusSquared + 4) {
        loc = loc.add(myLocation.directionTo(tower));
      }
    }

    return myLocation.directionTo(loc);
  }

  /**
   * A convenience method for pathing; gets all directions which
   * make progress toward the given map location `loc'.
   *  !! This method is SAFE to use without any checks. !!
   * @param from a valid map location.
   * @param to a valid map location.
   * @return an array of valid directions; or `null'.
   */
  static Direction[] getDirectionsTo(MapLocation from, MapLocation to) {
    if(from == null || to == null) return null;
    Direction d = from.directionTo(to);
    return getDirectionsLike(d);
  }

  /**
   * Gets all directions "in the same pathing order" as `d'.
   *   !! This method is SAFE to use without any checks. !!
   * @param d a valid direction; or `Direction.NONE'.
   * @return an array of directions "like" `d'.
   */
  static Direction[] getDirectionsLike(Direction d) {
    if(d == null) return null;
    return new Direction[] {
      d, d.rotateLeft(), d.rotateRight(),
      d.rotateLeft().rotateLeft(),
      d.rotateRight().rotateRight()
    };
  }

  /**
   * Tests whether `loc' is not attackable from any of the enemies structures.
   * @param loc a valid map location.
   * @return `true' if `loc' is safe; `false' otherwise.
   */
  // TODO: add sister method `hiddenGround(boolean strict)'
  private static boolean safeGround(MapLocation loc) {
    if(loc.distanceSquaredTo(RobotPlayer.enemyHQ) <= HQ.attackRadiusSquared) return false;
    for(MapLocation tower : RobotPlayer.enemyTowers) {
      if(tower.distanceSquaredTo(loc) <= TOWER.attackRadiusSquared) {
        return false;
      }
    }
    return true;
  }
}
