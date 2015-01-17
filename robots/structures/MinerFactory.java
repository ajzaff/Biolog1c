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

import battlecode.common.GameActionException;
import battlecode.common.RobotType;

import static team309.RobotPlayer.rc;
import static team309.Signal.SIG_MINER_FACTORY_RATE;
import static team309.Signal.SIG_MINER_RATE;

/**
 * Responsible for producing miners.
 */
public class MinerFactory extends BaseSpawner {

  /**
   * Spawns some miners.
   * @throws GameActionException
   */

  @Override
  public void act() throws GameActionException {
    int minerRate = rc.readBroadcast(SIG_MINER_RATE);

    if(rc.isCoreReady() && minerRate > 0 && spawn(RobotType.MINER)) {
      rc.broadcast(SIG_MINER_RATE, minerRate - 1);
    }
  }
}
