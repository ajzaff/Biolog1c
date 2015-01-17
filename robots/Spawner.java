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

package team309.robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;

public interface Spawner {

  /**
   * Tries to spawn a unit in the given direction `d'.
   * @param d a valid direction to spawn.
   * @param unit a valid unit to spawn.
   */
  boolean spawn(Direction d, RobotType unit) throws GameActionException;
}