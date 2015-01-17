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

import battlecode.common.*;
import team309.Signal;
import team309.robots.Attacker;
import team309.robots.BaseSentry;
import team309.robots.Spawner;

import static team309.RobotPlayer.rc;
import static team309.Signal.*;

/*
 * The `HQ' can spawn beavers, and attack things, if needed.
 * I also use it as a central message service.
 *
 * Although we do not really want it to come down to this,
 * the HQ is capable of doing large amounts of damage
 * to targets.
 *
 * This routine seeks out the best such target to hit, if any.
 */
public class HQ extends BaseSpawner implements Attacker {

  @Override
  public void act() throws GameActionException {

    transferSupply(); // sharing is caring.

    // On the first round, spawn a few beavers.
    if(Clock.getRoundNum() == 0) {
      rc.broadcast(SIG_BEAVER_RATE, 1);
      rc.broadcast(SIG_MINER_FACTORY_RATE, 1);
    }
    else if(Clock.getRoundNum() == 100) {
      rc.broadcast(SIG_MINER_RATE, 10);
    }
    else if(Clock.getRoundNum() == 1000) {
      rc.broadcast(SIG_DEPOT_RATE, 1);
    }

    int beaverRate = rc.readBroadcast(SIG_BEAVER_RATE);

    // Spawn some units.
    if(rc.isCoreReady() && beaverRate > 0 && spawn(RobotType.BEAVER)) {
      rc.broadcast(SIG_BEAVER_RATE, beaverRate - 1);
    }
  }

  @Override
  public boolean attack(MapLocation loc) {
    return false;
  }
}