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

import java.util.Stack;

import static team309.RobotPlayer.myHQ;
import static team309.robots.units.Miner.State.*;
import static team309.RobotPlayer.rc;

/**
 * Specialized unit for mining.
 */
public class Miner extends BaseMiner {

  /**
   * The routing destination for this miner.
   * If set, it means a miner is being routed to ore.
   */
  private MapLocation destination;

  /**
   * The states of a miner.
   */
  enum State {
    MINING,
    ROUTING,
    ATTACKING,
    IDLING
  }

  /**
   * This miner's current state
   */
  private State state;

  /**
   * Constructs a new miner
   */
  public Miner() {
    super(1);
    state = IDLING;
  }

  /**
   * Mine, evade.
   */
  public void act() throws GameActionException {

    myLocation = rc.getLocation(); // update location.
    transferSupply(); // share supply.

    rc.setIndicatorString(0, state.name());

    if(!rc.isCoreReady()) return;

    if(state == IDLING) {
      idlingState();
    }
    else if(state == ROUTING) {
      routingState();
    }
    else if(state == MINING) {
      miningState();
    }
  }

  /**
   * Handles the idling state transitions.
   */
  private void idlingState() throws GameActionException {
    destination = senseBestOreLocation(sensorRadiusSquared);
    if(destination == myLocation) {
      state = MINING;
    }
    else {
      state = ROUTING;
    }
  }

  /**
   * Handles the routing state transitions.
   */
  private void routingState() throws GameActionException {
    if(!routeTo(destination)) {
      state = MINING;
      miningState();
    }
  }

  /**
   * Handles the mining state transitions.
   */
  private void miningState() throws GameActionException {
    double oreValue = rc.senseOre(myLocation);
    if(oreValue < getMineThreshold()) {
      state = IDLING;
      idlingState();
    }
    if(state == MINING) {
      mine();
    }
  }
}