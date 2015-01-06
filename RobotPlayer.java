/*
`Biolog1c' is my submission to the 2015 `Battlecode' competition run by MIT.
I've decided to release its source for all to review, modify, and play with.

I only ask that you please not use the source, or portions of the source
as your team's submission to this year's Battlecode competition.

You may reach me by email: alan.z@cox.net.
If I don't know you please put `Cold contact' as your subject line.

---------------------------------------------------------------------------

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

package biolog1c;

import battlecode.common.*;
import static battlecode.common.RobotType.*;

import java.util.Random;

/**
 *
 */
public class RobotPlayer {

  /**
   * A static copy of my team.
   */
  static Team myTeam;

  /**
   * A static copy of the enemy team.
   */
  static Team otherTeam;

  /**
   * A pseudo-random number generator instance.
   */
  static final Random rand = new Random();

  /**
   * The tower's squared-range constant.
   */
  static final int TOWER_RANGE = 24;

  /**
   * A static copy of the array of directional constants.
   */
  static final Direction[] directions = Direction.values();

  /**
   * Runs the `RobotPlayer', i.e. all game logic.
   * @param rc the robot controller instance.
   */
  public static void run(RobotController rc) {

    //Assign my team instance.
    myTeam = rc.getTeam();

    // Assign the enemy team instance.
    otherTeam = myTeam.opponent();

    /*
     * The method is encapsulated in a forever-loop.
     * It should run for as long as the match running.
     */
    while(true) {

      // Get the type of the active robot.
      RobotType t = rc.getType();

      if(t == HQ) {
        // The `HQ' can spawn beavers, and attack other things, if needed.

        Direction d = directions[rand.nextInt(8)];
        if(rc.isCoreReady() && rc.canSpawn(d, BEAVER)) {
          try {
            rc.spawn(d, BEAVER);
          }
          catch(GameActionException e) {
            e.printStackTrace();
          }
        }
      }
      else if(t == TOWER) {
        // The `tower' is a powerful unit which attacks things.
        // It has an effective attack range of 24 units^2.

        if(rc.isWeaponReady()) {
          RobotInfo[] robots = rc.senseNearbyRobots(rc.getLocation(), TOWER_RANGE, otherTeam);

          try {
            double minHealth = Integer.MAX_VALUE;
            double thresholdHealth = 5;
            RobotInfo r_info = null;
            for(RobotInfo r : robots) {
              if(r.health < minHealth) {
                if(r.health < thresholdHealth) {
                  r_info = r;
                  break;
                }
                r_info = r;
                minHealth = r.health;
              }
            }
            if(r_info != null)
              rc.attackLocation(robots[rand.nextInt(robots.length)].location);
          }
          catch(GameActionException e) {
            e.printStackTrace();
          }
        }
      }
      else if(t == BASHER) {
        // Bashers walk around and attack adjacent enemies.
      }
      else if(t == SOLDIER) {
        // Soldiers walk around and attack things.
      }
      else if(t == BEAVER) {
        // Beavers are units which BUILD structures (like BARRACKS).
        if(rc.isCoreReady()) {
          if(rand.nextBoolean()) {
            Direction d = directions[rand.nextInt(8)];
            if(rc.canMove(d)) {
              try {
                rc.move(d);
              } catch (GameActionException e) {
                e.printStackTrace();
              }
            }
            else {
              try {
                rc.mine();
              }
              catch(GameActionException e) {
                e.printStackTrace();
              }
            }
          }
          else if(rand.nextBoolean()) {
            try {
              rc.mine();
            }
            catch(GameActionException e) {
              e.printStackTrace();
            }
          }
          else {

          }
        }
      }
      else if(t == BARRACKS) {
        // Spawn bashers and soldiers.
      }

      // finish this bot's turn early and save the extra byte code.
      rc.yield();
    }
  }
}
