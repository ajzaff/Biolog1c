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
   * The maximum health an HQ can sense to cause a
   * cutoff in the target-selection routine.
   */
  static final double HQ_AUTO_ENGAGE_CUTOFF = 24;

  /**
   * The tower's squared-range constant.
   */
  static final int TOWER_RANGE = 24;

  /**
   * The maximum health a tower can sense to cause a
   * cutoff in the target-selection routine.
   */
  static final double TOWER_AUTO_ENGAGE_CUTOFF = 8;

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

    int i = 0;

    /*
     * The method is encapsulated in a forever-loop.
     * It should run for as long as the match running.
     */
    while(true) {

      // Get the type of the active robot.
      RobotType t = rc.getType();

      if(t == HQ) {
        /*
         * The `HQ' can spawn beavers, and attack things, if needed.
         */

        // Test if the HQ's weapon is ready.
        if(rc.isWeaponReady()) {
          /**
           * Although we do not want it to come down to this,
           * the HQ is capable of doing large amounts of damage
           * to targets.
           *
           * This routine seeks out the best such target to hit, if any.
           */

          RobotInfo[] targets = rc.senseNearbyRobots(rc.getLocation(), t.attackRadiusSquared, otherTeam);

          // If no nearby enemies, yield and continue.
          if(targets.length > 0) {
            try {
              RobotInfo t_info = targets[0]; // The "best target" -- let's initialize this to `target_0'.

              // Loop through all enemy units
              for (RobotInfo r : targets) {

                // If the target is a missile, cause a cutoff.
                if (r.type == MISSILE) {
                  t_info = r;
                  break;
                }

                // If the new target is less healthy -- update it.
                if (r.health < t_info.health) {

                  // If the health is "low enough" -- cutoff.
                  if (r.health <= HQ_AUTO_ENGAGE_CUTOFF) {
                    t_info = r;
                    break;
                  }

                  t_info = r; // Update the target info variable.
                }
              }

              rc.attackLocation(t_info.location); // Attack it!
            } catch (GameActionException e) {
              e.printStackTrace();
            }
          }
        }

        Direction d = directions[rand.nextInt(8)];
        if(rc.isCoreReady()) {
          if(i < 20) {
            if(rc.canSpawn(d, BEAVER)) {
              try {
                rc.spawn(d, BEAVER);
                i++;
              }
              catch(GameActionException e) {
                e.printStackTrace();
              }
            }
          }
        }
      }
      else if(t == TOWER) {
        /*
         * The `tower' is a powerful unit which attacks things.
         * It has an effective attack range of 24 units^2 (see TOWER_RANGE).
         */

        // Test if the tower's weapon system is online.
        if(rc.isWeaponReady()) {

          // Get an array of nearby robots within this towers attacking range.
          RobotInfo[] targets = rc.senseNearbyRobots(rc.getLocation(), TOWER_RANGE, otherTeam);

          // If no nearby enemies, yield and continue.
          if(targets.length == 0) {
            rc.yield();
            continue;
          }

          /*
           * The goal is to find a suitable enemy robot to attack.
           * The strategy is generally, to find the robot
           * with the minimum remaining health and attack it.
           *
           * To save compute time, if a target with less than a given
           * cutoff is found, it is selected, and target search stops.
           *
           * Missile targets prioritized over all others.
           */
          try {

            RobotInfo t_info = targets[0]; // The "best target" -- let's initialize this to `target_0'.

            // Loop through all enemy units
            for(RobotInfo r : targets) {

              // If the target is a missile, cause a cutoff.
              if(r.type == MISSILE) {
                t_info = r;
                break;
              }

              // If the new target is less healthy -- update it.
              if(r.health < t_info.health) {

                // If the health is "low enough" -- cutoff.
                if(r.health <= TOWER_AUTO_ENGAGE_CUTOFF) {
                  t_info = r;
                  break;
                }

                t_info = r; // Update the target info variable.
              }
            }

            rc.attackLocation(t_info.location); // Attack it!
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
        /*
         * Beavers are the only units capable of building structures.
         * They also have a limited ability to mine, and attack if needed.
         */

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
          }
          else if(rc.senseOre(rc.getLocation()) >= 1) {
            try {
              rc.mine();
            }
            catch(GameActionException e) {
              e.printStackTrace();
            }
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
