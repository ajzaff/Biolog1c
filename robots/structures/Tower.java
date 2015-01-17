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

import battlecode.common.MapLocation;
import team309.robots.BaseSentry;


/**
 * The `tower' is a powerful unit which attacks things.
 * It has an effective attack range of 24 units^2.
 *
 * The goal is to find a suitable enemy robot to attack.
 * The strategy is generally, to find the robot
 * with the minimum remaining health and attack it.
 *
 * To save compute time, if a target with less than a given
 * cutoff is found, it is selected, and target search stops.
 */
public class Tower extends BaseSentry {

  @Override
  public void act() {


  }

  @Override
  public boolean attack(MapLocation loc) {
    return false;
  }
}
