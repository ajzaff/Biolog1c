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

package team309.robots.units;

import battlecode.common.*;
import team309.Navigation;
import team309.robots.BaseSentry;

import java.util.Random;

import static team309.Navigation.directions;
import static team309.RobotPlayer.rc;
import static battlecode.common.RobotType.*;

public abstract class BaseUnit extends BaseSentry {

  /**
   * The robots current location.
   * The implementation is responsible for updating this.
   */
  protected MapLocation myLocation;

  /**
   * Routes a unit to the given location.
   * @param destination a valid map location.
   */
  public boolean routeTo(MapLocation destination) throws GameActionException {
    if(myLocation == destination) return false;

    Direction[] ds = Navigation.getDirectionsTo(destination);

    for(Direction d : ds) {
      if(rc.canMove(d)) {
        rc.move(d);
        return true;
      }
    }

    return false;
  }

  /**
   * Diffuses randomly.
   * @param rand a random number generator.
   */
  public boolean diffuseRandomly(Random rand) throws GameActionException {
    Direction d = directions[rand.nextInt(8)];
    for(int i=0; i < 8; i++) {
      if(rc.canMove(d)) {
        rc.move(d);
        return true;
      }
      d = d.rotateLeft();
    }
    return false;
  }

  /**
   * Tests whether the robot type is a unit.
   * @param type a robot type.
   * @return `true' if the robot is a unit; `false' otherwise.
   */
  public static boolean isUnit(RobotType type) {
    return type == MINER || type == BASHER || type == BEAVER || type == COMMANDER || type == COMPUTER ||
      type == DRONE || type == LAUNCHER || type == MINER || type == SOLDIER || type == TANK;
  }
}
