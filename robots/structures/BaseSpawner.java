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

package team309.robots.structures;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;
import team309.Navigation;
import team309.robots.Spawner;

import static team309.RobotPlayer.enemyHQ;
import static team309.RobotPlayer.rc;

public abstract class BaseSpawner extends BaseStructure implements Spawner {

  /**
   * Spawns a `unit' in the given direction `d'.
   * @param d a valid direction to spawn.
   * @param unit a valid unit to spawn.
   * @return `true' if a unit was spawned; `false' otherwise.
   * @throws GameActionException
   */
  @Override
  public boolean spawn(Direction d, RobotType unit) throws GameActionException {
    if(d == null) return false;
    if(rc.canSpawn(d, unit)) {
      rc.spawn(d, unit);
      return true;
    }
    return false;
  }

  /**
   * Spawns a `unit' in one of the priority directions.
   * @param ds an array of direction priorities.
   * @param unit a valid unit to spawn.
   * @return `true' if a unit was spawned; `false' otherwise.
   * @throws GameActionException
   */
  public boolean spawn(Direction[] ds, RobotType unit) throws GameActionException {
    return spawn(getValidSpawnDirection(ds, unit), unit);
  }

  /**
   * Spawns a `unit' towards the enemy's HQ.
   * @param unit a valid unit to spawn.
   * @return `true' if a unit was spawned; `false' otherwise.
   * @throws GameActionException
   */
  public boolean spawn(RobotType unit) throws GameActionException {
    return spawn(Navigation.directions, unit);
  }

  /**
   * Gets a valid spawn direction.
   * @param ds an array of direction priorities.
   * @param unit a valid unit type.
   * @return a valid spawn direction or `null'.
   */
  public Direction getValidSpawnDirection(Direction[] ds, RobotType unit) {
    for(Direction d : ds) {
      if(rc.canSpawn(d, unit)) {
        return d;
      }
    }
    return null;
  }
}