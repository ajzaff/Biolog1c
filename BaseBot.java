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

package team309;

import battlecode.common.Clock;
import battlecode.common.RobotType;
import battlecode.common.Team;

public abstract class BaseBot {

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
  protected final double sensorRadiusSquared;

  /**
   * The health of this robot set from the last round;
   * Useful to see if we've taken damage.
   * Used by towers to report changes to their HP status.
   * Generally useful for vulnerables, such as structures.
   *  !! Warning: not set by all bots. !!
   */
  protected double health;

  public BaseBot() {
    this.type = RobotPlayer.rc.getType();
    this.birthday = Clock.getRoundNum();
    this.myTeam = RobotPlayer.rc.getTeam();
    this.otherTeam = myTeam.opponent();
    this.sensorRadiusSquared = type.sensorRadiusSquared;
  }

  public abstract void act();

  public final void run() {
    while(true) {
      act();
      RobotPlayer.rc.yield();
    }
  }
}