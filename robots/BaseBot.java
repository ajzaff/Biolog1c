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

import battlecode.common.*;
import team309.robots.units.BaseUnit;

import static team309.RobotPlayer.rc;

public abstract class BaseBot {

  /**
   * The supply transfer minimum.
   */
  protected static double SUPPLY_TRANSFER_MINIMUM = 500;

  /**
   * A copy of my team.
   */
  protected final Team myTeam;
  /**
   * A copy of the enemy team.
   */
  protected final Team otherTeam;

  /**
   * Used to keep track of the round this unit was born.
   */
  protected final int birthday;

  /**
   * This robot's type and other metrics.
   */
  protected final RobotType type;

  /**
   * Sensor radius squared.
   */
  protected final int sensorRadiusSquared;

  /**
   * Constructs a new `BaseBot'.
   */

  public BaseBot() {
    this.type = rc.getType();
    this.birthday = Clock.getRoundNum();
    this.myTeam = rc.getTeam();
    this.otherTeam = myTeam.opponent();
    this.sensorRadiusSquared = type.sensorRadiusSquared;
  }

  public abstract void act() throws GameActionException;

  /**
   * Runs this bot by executing its `act' method,
   * yielding the extra byte-code, and repeating.
   */

  public final void run() {
    while(true) {
      try {
        act();
        rc.yield();
      }
      catch (GameActionException e) {
        e.printStackTrace();
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Transfers supply between robots..
   * It is not guaranteed to transfer a supply every time.
   * @return `true' if supply was transferred; `false' otherwise.
   */
  public boolean transferSupply() throws GameActionException {

    double mySupply = rc.getSupplyLevel();
    if(mySupply < 2*SUPPLY_TRANSFER_MINIMUM) return false;

    RobotInfo[] units = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, myTeam);
    RobotInfo r_info = null;
    double minSupply = mySupply;

    for(RobotInfo unit : units) {
      if(BaseUnit.isUnit(unit.type) && unit.supplyLevel < minSupply) {
        r_info = unit;
        minSupply = unit.supplyLevel;
      }
    }

    if(r_info == null) return false;

    double transferAmount = (mySupply - minSupply) / 2;
    rc.transferSupplies((int) transferAmount, r_info.location);

    return true;
  }
}