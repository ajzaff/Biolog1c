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

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public interface Attacker {

  /**
   * Attack the given map location `loc'.
   * @param loc a valid map location to attack
   */

  boolean attack(MapLocation loc) throws GameActionException;
}
