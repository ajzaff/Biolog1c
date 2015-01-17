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

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;

import java.util.Random;

import static team309.RobotPlayer.*;
import static team309.Signal.SIG_DEPOT_RATE;
import static team309.Signal.SIG_MINER_FACTORY_RATE;
import static team309.Navigation.directions;

/*
 * Beavers are the only units capable of building structures.
 * They also have a limited ability to mine, and attack if needed.
 */
public class Beaver extends BaseMiner {

  private final Random rand;

  /**
   * The color complex to build on.
   */
  private final int color;

  /**
   * Creates a new beaver.
   */
  public Beaver() {
    super(5);
    rand = new Random();
    color = (myHQ.x % 2) ^ (myHQ.y % 2);
  }

  /**
   * Performs actions for this beaver.
   * @throws GameActionException
   */
  @Override
  public void act() throws GameActionException {

    // Update the current location of this beaver.
    myLocation = rc.getLocation();

    transferSupply();

    int minerFactoryRate = rc.readBroadcast(SIG_MINER_FACTORY_RATE);
    int depotRate = rc.readBroadcast(SIG_DEPOT_RATE);

    // Build something;
    // Coincidentally the order here corresponds to priority.
    if(rc.isCoreReady()) {

      // Build a miner factory.
      if(minerFactoryRate > 0) {
        if(build(RobotType.MINERFACTORY)) {
          rc.broadcast(SIG_MINER_FACTORY_RATE, minerFactoryRate - 1);
        }
        else if(rc.getTeamOre() >= RobotType.MINERFACTORY.oreCost) {
          diffuseRandomly(rand);
        }
        return;
      }

      // Build a supply depot.
      if(depotRate > 0) {
        if(build(RobotType.SUPPLYDEPOT)) {
          rc.broadcast(SIG_DEPOT_RATE, depotRate - 1);
        }
        else if(rc.getTeamOre() >= RobotType.SUPPLYDEPOT.oreCost) {
          diffuseRandomly(rand);
        }
        return;
      }

      // Try mining as a default action.
      thresholdMine();
    }
  }

  /**
   * Tries to build a structure at a location around the beaver.
   * Buildings are only built in locations where their component positions
   * equal the HQ's component positions (mod 2).
   * In this way, no building is ever next to another.
   * @param structure a valid structure.
   * @return `true' if it built something; `false' otherwise.
   * @throws GameActionException
   */
  private boolean build(RobotType structure) throws GameActionException {

    for(Direction d : directions) {
      MapLocation loc = myLocation.add(d);
      if (rc.canBuild(d, structure) && isRightColor(loc)) {
        rc.build(d, structure);
        return true;
      }
    }

    return false;
  }

  /**
   * Tests if the map location `loc' is on the right HQ color.
   * @param loc a valid map location.
   * @return `true' if on the right color; `false' otherwise.
   */
  private boolean isRightColor(MapLocation loc) {
    int pointColor = (loc.x % 2) ^ (loc.y % 2);
    return pointColor == color;
  }
}