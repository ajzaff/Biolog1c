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
 * Encapsulates all game logic.
 */
public class RobotPlayer {

  /**
   * The maximum health an HQ can sense to cause a
   * cutoff in the target-selection routine.
   */
  private static final double HQ_AUTO_ENGAGE_CUTOFF = HQ.attackPower;

  /**
   * The minimum amount of ore on a given tile for
   * a beaver to justify trying to mine it.
   */
  private static final double BEAVER_MINING_CUTOFF = 5;

  /**
   * The beaver's cutoff for selecting a suitable target.
   */
  private static final double BEAVER_AUTO_ENGAGE_CUTOFF = BEAVER.attackPower;

  /**
   * The maximum health a tower can sense to cause a
   * cutoff in the target-selection routine.
   */
  private static final double TOWER_AUTO_ENGAGE_CUTOFF = 8;

  /**
   * The soldier's cutoff for selecting a suitable target.
   */
  private static final double SOLDIER_AUTO_ENGAGE_CUTOFF = SOLDIER.attackPower;

  /**
   * The minimum amount of ore on a given tile for
   * a beaver to justify trying to mine it.
   */
  private static final double MINER_MINING_CUTOFF = 1;

  /**
   * The beaver spawn rate signal channel.
   */
  private static final int SIG_BEAVER_RATE = 0;

  /**
   * The barracks build rate signal channel.
   */
  private static final int SIG_BARRACKS_RATE = 1;

  /**
   * The factory rate signal channel.
   */
  private static final int SIG_MINE_FACTORY_RATE = 2;

  /**
   * The depot rate signal channel.
   */
  private static final int SIG_DEPOT_RATE = 3;

  /**
   * The soldier rate signal channel.
   */
  private static final int SIG_SOLDIER_RATE = 4;

  /**
   * The soldier rate signal channel.
   */
  private static final int SIG_BASHER_RATE = 5;

  /**
   * The miner rate signal channel.
   */
  private static final int SIG_MINER_RATE = 6;

  /**
   * The helipad rate signal channel.
   */
  private static final int SIG_HELIPAD_RATE = 7;

  /**
   * The drone rate signalling channel.
   */
  private static final int SIG_DRONE_RATE = 8;

  /**
   * The aerospace lab rate signalling channel.
   */
  private static final int SIG_AERO_LAB_RATE = 9;

  /**
   * The launcher rate signalling channel.
   */
  private static final int SIG_LAUNCHER_RATE = 10;

  /**
   * The launcher rate signalling channel.
   */
  private static final int SIG_HANDWASH_RATE = 11;

  /**
   * The tank factory rate signalling channel.
   */
  private static final int SIG_TANK_FACTORY_RATE = 12;

  /**
   * The tank rate signalling channel.
   */
  private static final int SIG_TANK_RATE = 13;

  /**
   * Signal channel used to general unit micro.
   */
  private static final int SIG_SWARM = 14;

  /**
   * Rally around the HQ.
   */
  private static final int SWARM_NEAR_HQ = 0x1;

  /**
   * Rally around the middle of the map.
   */
  private static final int SWARM_NEAR_MID = 0x2;

  /**
   * Signal channel used for aggressive unit micro.
   */
  private static final int SIG_PUSH = 15;

  /**
   * Push toward near the enemy's HQ.
   */
  private static final int PUSH_NEAR_HQ = 0x1;

  /**
   * Signal space allocated to resupply map locations.
   */
  private static final int SIG_RESUPPLY = 16;

  /**
   * The resupply size (actual size, i.e. number of x+y components).
   */
  private static final int RESUPPLY_CHANNELS = 20;

  /**
   * The cutoff for sending a resupply signal.
   */
  private static final int RESUPPLY_CUTOFF = 100;

  /**
   * The minimum supply needed to cause a cutoff in supply transfer search.
   */
  private static final double SUPPLY_TRANSFER_CUTOFF = 500;

  /**
   * A static copy of the array of directional constants.
   */
  private static final Direction[] directions = Direction.values();

  /**
   * The robot controller instance.
   */
  private static RobotController rc;

  /**
   * A copy of my team.
   */
  private static Team myTeam;

  /**
   * A copy of the enemy team.
   */
  private static Team otherTeam;

  /**
   * The enemy HQ location.
   */
  private static MapLocation myHQ;

  /**
   * The enemy HQ location.
   */
  private static MapLocation enemyHQ;

  /**
   * The enemy HQ rush location.
   * It's a position a few blocks off the actual
   * HQ to prevent clustering of launchers.
   */
  private static MapLocation rushHQ;

  /**
   * The locations of enemy towers.
   */
  private static MapLocation[] enemyTowers;

  /**
   * The locations of my towers.
   */
  private static MapLocation[] myTowers;

  /**
   * The space between both teams' HQs
   */
  private static MapLocation mid;

  /**
   * A pseudo-random number generator instance.
   */
  private static final Random rand = new Random();

  /**
   * Runs the `RobotPlayer', i.e. all game logic.
   * @param controller the robot controller instance.
   */
  public static void run(RobotController controller) {

    rc = controller; // Set the robot.
    myTeam = rc.getTeam(); // Set my team.
    otherTeam = myTeam.opponent(); // Set the enemy team.
    myHQ = rc.senseHQLocation();
    myTowers = rc.senseTowerLocations();
    enemyHQ = rc.senseEnemyHQLocation();
    rushHQ = enemyHQ.add(enemyHQ.directionTo(myHQ), LAUNCHER.attackRadiusSquared-1);
    enemyTowers = rc.senseEnemyTowerLocations();
    mid = new MapLocation((myHQ.x+enemyHQ.x)/2, (myHQ.y+enemyHQ.y)/2);

    Direction facing = null;

    /*
     * The method is encapsulated in a forever-loop.
     * It should run for as long as the match running.
     */
    // TODO: keep an ArrayList of dependency expectations. (see DependencyProgress). If one hasn't been met, and it should be, we KNOW its been destroyed and we can rebuild.
    while(true) {

      // Get the type of the active robot.
      RobotType t = rc.getType();


      if(t == HQ) {
        /*
         * The `HQ' can spawn beavers, and attack things, if needed.
         * I also use it as a central message service.
         */
        /**
         * Although we do not want it to come down to this,
         * the HQ is capable of doing large amounts of damage
         * to targets.
         *
         * This routine seeks out the best such target to hit, if any.
         */

        safeTransferSupply(); // sharing is caring.

        int round = Clock.getRoundNum();
        if(round == 1500) {
          rc.setIndicatorString(0, "Phase 9");
          safeBroadcastSetFlag(SIG_PUSH, PUSH_NEAR_HQ);
        }
        if(round == 1400) {
          rc.setIndicatorString(0, "Phase 8");
          safeBroadcast(SIG_DRONE_RATE, 20);
        }
        if(round == 1000) {
          rc.setIndicatorString(0, "Phase 8");
          safeBroadcast(SIG_HELIPAD_RATE, 4);
          safeBroadcast(SIG_DRONE_RATE, 20);
        }
        if(round == 804) {
          rc.setIndicatorString(0, "Phase 8");
          safeBroadcast(SIG_LAUNCHER_RATE, 20);
        }
        if(round == 644) {
          rc.setIndicatorString(0, "Phase 7");
          safeBroadcast(SIG_AERO_LAB_RATE, 2);
          safeBroadcast(SIG_DEPOT_RATE, 1);
        }
        else if(round == 493) {
          rc.setIndicatorString(0, "Phase 6");
          safeBroadcast(SIG_MINER_RATE, 20);
          safeBroadcast(SIG_DRONE_RATE, 4);
          safeBroadcast(SIG_TANK_RATE, 4);
        }
        else if(round == 393) {
          rc.setIndicatorString(0, "Phase 5");
          safeBroadcast(SIG_TANK_FACTORY_RATE, 1);
          safeBroadcast(SIG_LAUNCHER_RATE, 1);
          safeBroadcast(SIG_DEPOT_RATE, 5);
        }
        else if(round == 292) {
          rc.setIndicatorString(0, "Phase 4");
          safeBroadcast(SIG_AERO_LAB_RATE, 1);
        }
        else if(round == 241) {
          rc.setIndicatorString(0, "Phase 3");
          safeBroadcast(SIG_MINER_RATE, 3);
          safeBroadcast(SIG_BARRACKS_RATE, 1);
        }
        else if(round == 143) {
          rc.setIndicatorString(0, "Phase 2");
          safeBroadcast(SIG_MINE_FACTORY_RATE, 1);
        }
        else if(round == 90) {
          rc.setIndicatorString(0, "Phase 1");
          safeBroadcast(SIG_HELIPAD_RATE, 1);
        }
        else if(round == 0) {
          rc.setIndicatorString(0, "Phase 0");
          safeBroadcast(SIG_BEAVER_RATE, 4);
          safeBroadcastSetFlag(SIG_SWARM, SWARM_NEAR_MID);
        }

        if(rc.isWeaponReady() && unsafeSelectTargetAndEngage(rc.getLocation(), t.attackRadiusSquared, HQ_AUTO_ENGAGE_CUTOFF)) {
          rc.yield();
          continue;
        }

        if(rc.isCoreReady()) {
          unsafeSpawn(BEAVER, SIG_BEAVER_RATE);
        }
      }
      else if(t == TOWER) {
        /*
         * The `tower' is a powerful unit which attacks things.
         * It has an effective attack range of 24 units^2 (see TOWER_ATTACK_RANGE).
         */
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

        if(rc.isWeaponReady()) {
          unsafeSelectTargetAndEngage(rc.getLocation(), TOWER.attackRadiusSquared, TOWER_AUTO_ENGAGE_CUTOFF);
        }
      }
      else if(t == BASHER) {
        /*
         * Bashers walk around and attack adjacent enemies automatically.
         */

        safeTransferSupply(); // sharing is caring.

        if(rc.isCoreReady()) {
          if(!unsafeSwarm(SWARM_NEAR_MID)) {
            unsafeDiffuseRandomly();
          }
        }
      }
      else if(t == SOLDIER) {
        // Soldiers walk around and attack things.
        /*
         * The `tower' is a powerful unit which attacks things.
         * It has an effective attack range of 24 units^2 (see TOWER_ATTACK_RANGE).
         */
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

        safeTransferSupply(); // sharing is caring.

        if(rc.isWeaponReady() && unsafeSelectTargetAndEngage(rc.getLocation(), SOLDIER.attackRadiusSquared, SOLDIER_AUTO_ENGAGE_CUTOFF)) {
          rc.yield();
          continue;
        }
        if(rc.isCoreReady()) {
          if(!unsafeSwarm(SWARM_NEAR_MID)) {
            unsafeDiffuseRandomly();
          }
        }
      }
      else if(t == BEAVER) {
        /*
         * Beavers are the only units capable of building structures.
         * They also have a limited ability to mine, and attack if needed.
         */

        safeTransferSupply(); // sharing is caring.

        if(rc.isWeaponReady() && unsafeSelectTargetAndEngage(rc.getLocation(), BEAVER.attackRadiusSquared, BEAVER_AUTO_ENGAGE_CUTOFF)) {
          rc.yield();
          continue;
        }
        if(rc.isCoreReady()) {
          if(!unsafeBuild(BARRACKS, SIG_BARRACKS_RATE))
            if(!unsafeBuild(SUPPLYDEPOT, SIG_DEPOT_RATE))
              if(!unsafeBuild(MINERFACTORY, SIG_MINE_FACTORY_RATE))
                if(!unsafeBuild(HELIPAD, SIG_HELIPAD_RATE))
                  if(!unsafeBuild(AEROSPACELAB, SIG_AERO_LAB_RATE))
                    if(!unsafeBuild(TANKFACTORY, SIG_TANK_FACTORY_RATE)) {
                      RobotInfo[] enemies = rc.senseNearbyRobots(BEAVER.sensorRadiusSquared, otherTeam);
                      if (enemies.length == 0 && unsafeMine(BEAVER_MINING_CUTOFF)) {
                        rc.yield();
                        continue;
                      }

                      unsafeDiffuseSafely(enemies, null);
                    }
        }
      }
      else if(t == BARRACKS) {
        /*
         * Spawn bashers and soldiers.
         */

        if(rc.isCoreReady()) {
          if(!unsafeSpawn(SOLDIER, SIG_SOLDIER_RATE)) {
            unsafeSpawn(BASHER, SIG_BASHER_RATE);
          }
        }
      }
      else if(t == MINERFACTORY) {
        /*
         * Has the sole responsibility of producing miners.
         */

        if(rc.isCoreReady()) {
          unsafeSpawn(MINER, SIG_MINER_RATE);
        }
      }
      else if(t == MINER) {
        /**
         * Responsible for mining.
         */

        safeTransferSupply(); // sharing is caring.

        if(rc.isCoreReady()) {
          RobotInfo[] enemies = rc.senseNearbyRobots(MINER.sensorRadiusSquared, otherTeam);
          if(enemies.length > 0 || !unsafeMine(MINER_MINING_CUTOFF)) {
            Direction optimalDirection = safeOptimalOreDirection();
            unsafeDiffuseSafely(enemies, optimalDirection);
          }
        }
      }
      else if(t == HELIPAD) {
        /**
         * Spawns drones.
         */

        if(rc.isCoreReady()) {
          unsafeSpawn(DRONE, SIG_DRONE_RATE);
        }
      }
      else if(t == AEROSPACELAB) {
        /**
         * Spawns launchers.
         */

        if(rc.isCoreReady()) {
          unsafeSpawn(LAUNCHER, SIG_LAUNCHER_RATE);
        }
      }
      else if(t == TANKFACTORY) {
        /**
         * Spawns launchers.
         */

        if(rc.isCoreReady()) {
          unsafeSpawn(TANK, SIG_TANK_RATE);
        }
      }
      else if(t == TANK) {
        /**
         * Tanks are powerful long-range units.
         */

        safeTransferSupply(); // sharing is caring.

        if(rc.isWeaponReady() && unsafeSelectTargetAndEngage(rc.getLocation(), TANK.attackRadiusSquared, TANK.attackPower)) {
          rc.yield();
          continue;
        }
        if(rc.isCoreReady()) {
          if(!unsafeSwarm(SWARM_NEAR_MID)) {
            unsafeDiffuseRandomly();
          }
        }
      }
      else if(t == DRONE) {
        /**
         * Drones can fly over shit.
         */

        if(rc.isWeaponReady() && unsafeSelectTargetAndEngage(rc.getLocation(), DRONE.attackRadiusSquared, DRONE.attackPower)) {
          rc.yield();
          continue;
        }
        if(rc.isCoreReady()) {
          if(!unsafeRouteResupply(DRONE)) {
            RobotInfo[] enemies = rc.senseNearbyRobots(DRONE.sensorRadiusSquared, otherTeam);
            unsafeDiffuseSafely(enemies, null);
          }
        }
      }
      else if(t == LAUNCHER) {
        /**
         * Launchers stage and launch missiles but cannot themselves attack.
         */

        safeTransferSupply(); // sharing is caring.

        RobotInfo[] targets = rc.senseNearbyRobots(LAUNCHER.sensorRadiusSquared, otherTeam);

        if(rc.isWeaponReady()) {

          RobotInfo r_info = null;

          if(targets.length > 0) {

            for(RobotInfo r : targets) {

              if(r.type == TOWER) {
                r_info = r;
                break;
              }

              RobotInfo[] friends = rc.senseNearbyRobots(r.location, MISSILE.attackRadiusSquared, myTeam);

              if(friends.length == 0) {
                r_info = r;
                break;
              }

            }

            if(r_info != null) {
              facing = rc.getLocation().directionTo(r_info.location);
              unsafeLaunchMissile(facing);
            }

            rc.yield();
            continue;
          }
        }

        if(rc.isCoreReady() && targets.length == 0) {
          if(!unsafeSwarm(SWARM_NEAR_MID)) {
            unsafeDiffuseRandomly();
          }
        }
      }
      else if(t == MISSILE) {
        /**
         * Kaboom!
         */

        if(rc.isCoreReady()) {

          if(facing == null) {
            MapLocation myLocation = rc.getLocation();
            Direction d = Direction.NORTH;
            try {
              RobotInfo r_info = rc.senseRobotAtLocation(myLocation.add(d));
              int i = 0;
              while(i < 8 && (r_info == null || r_info.type != LAUNCHER || r_info.team != myTeam)) {
                d = d.rotateLeft();
                r_info = rc.senseRobotAtLocation(myLocation.add(d));
                i++;
              }
              facing = (i == 8? null : d.opposite());

              if(facing == null) {
                rc.yield();
                continue;
              }
            }
            catch (GameActionException e) {
              e.printStackTrace();
            }
          }

          try {
            RobotInfo[] targets = rc.senseNearbyRobots(MISSILE.attackRadiusSquared, otherTeam);
            RobotInfo[] friends = rc.senseNearbyRobots(MISSILE.attackRadiusSquared, myTeam);

            if(targets.length > 0 && friends.length == 0) {
              rc.explode();
            }

            else if(rc.canMove(facing)) {
              rc.move(facing);
            }
          }
          catch(GameActionException e) {
            e.printStackTrace();
          }
        }
      }

      // finish this bot's turn early and save the extra byte code.
      rc.yield();
    }
  }

  /**
   * Selects and engages a target within the given `range'.
   *  !! This method is UNSAFE and requires `isWeaponReady' before using. !!
   * @param from a valid location of my attacking robot.
   * @param range a valid range within which to get candidate targets.
   * @param cutoff the maximum health a target must have to consider
   *               a full selection cutoff. When this happens, the
   *               target's location is attacked and selection stops.
   *               This mostly saves having to search through many targets.
   * @return `true' if a target was attacked; `false' otherwise.
   */
  // TODO: replace cutoff with signal channel, or data structure containing preferences.
  private static boolean unsafeSelectTargetAndEngage(MapLocation from, int range, double cutoff) {
    RobotInfo[] targets = rc.senseNearbyRobots(from, range, otherTeam);
    if(targets.length == 0) return false;

    RobotInfo r_info = targets[0]; // select the first target.
    for (int i=1; i < targets.length; i++) { // Loop through all enemy units
      RobotInfo r = targets[i];
      if(!rc.canAttackLocation(r.location)) continue;
      if (r.type == TOWER) { // If the target is a tower, cause a cutoff.
        r_info = r; // return this target.
        break;
      }
      if (r.health < r_info.health) { // If the new target is less healthy -- update it.
        if (r.health <= cutoff) {
          r_info = r; // If the health is "low enough" -- cutoff.
          break;
        }
        r_info = r; // Update the target info variable.
      }
    }
    try {
      rc.attackLocation(r_info.location); // Attack it!
      return true;
    }
    catch (GameActionException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Gets a valid spawn direction, or `null' from the given location for a robot.
   * It does not actually spawn anything.
   *  !! This method is SAFE to use without any checks. !!
   * @param from a valid location from which to initiate the spawn.
   * @param robot a valid robot to try to spawn.
   * @return a valid direction or `null'.
   */
  private static Direction getValidSpawnDirection(MapLocation from, RobotType robot) {
    Direction d = from.directionTo(enemyHQ);
    for(int i=0; i < 8; i++) {
      if(rc.canSpawn(d, robot)) {
        return d;
      }
      d = d.rotateLeft();
    }
    return null;
  }

  /**
   * Gets a valid build direction, or `null' from the given location for a robot.
   * It does not actually build anything.
   *  !! This method is SAFE to use without any checks. !!
   * @param from a valid location in which to initiate the build.
   * @param robot a valid robot to try to spawn.
   * @return a valid direction or `null'.
   */
  private static Direction getValidBuildDirection(MapLocation from, RobotType robot) {
    Direction d = from.directionTo(enemyHQ);
    for(int i=0; i < 8; i++) {
      if (rc.canBuild(d, robot)) {
        return d;
      }
      d = d.rotateLeft();
    }
    return null;
  }

  /**
   * Tries to build a robot of the given type in a valid build direction
   * If ore costs are satisfied, and an order was found in the broadcasts.
   * If built, it will signal to decrement the order count for this robot.
   *  !! This method is UNSAFE and requires `isCoreReady' before use. !!
   * @param robot a valid robot type to build.
   * @param signal the channel containing the order rate for this robot.
   * @return `true' if a robot was built; `false' otherwise.
   */
  private static boolean unsafeBuild(RobotType robot, final int signal) {
    try {
      int rate = rc.readBroadcast(signal);
      if(rate <= 0) return false;
      if(rc.getTeamOre() >= robot.oreCost) {
        Direction d = getValidBuildDirection(rc.getLocation(), robot);
        if (d != null) {
          rc.build(d, robot);
          rc.broadcast(signal, rate-1);
          return true;
        }
      }
    }
    catch(GameActionException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Tries to spawn a robot of the given type in a valid spawn direction
   * If ore costs are satisfied, and an order was found in the broadcasts.
   * If spawned, it will signal to decrement the order count for this robot.
   *  !! This method is UNSAFE and requires `isCoreReady' before use. !!
   * @param robot a valid robot type to spawn.
   * @param signal a valid channel containing the order rate for this robot.
   * @return `true' if a robot was spawned; `false' otherwise.
   */
  private static boolean unsafeSpawn(RobotType robot, final int signal) {
    try {
      int rate = rc.readBroadcast(signal);
      if(rate <= 0) return false;
      if(rc.getTeamOre() >= robot.oreCost) {
        Direction d = getValidSpawnDirection(rc.getLocation(), robot);
        if (d != null) {
          rc.spawn(d, robot);
          rc.broadcast(signal, rate-1);
          return true;
        }
      }
    }
    catch(GameActionException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * A customized supply transfer procedure for the HQ.
   * The HQ requires special privileges with donating its supply,
   * namely, there is no requirement that
   */

  /**
   * A pretty generic method which shares supply between nearby bots.
   * It should also post a message when a non-supply duty bot is low on supply.
   *  !! This method is SAFE to use without any checks. !!
   *  !! Though it may be inappropriate for some to use. !!
   */
  private static void safeTransferSupply() {
    try {
      try {
        RobotInfo[] targets = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, myTeam);
        if(targets.length == 0) return;
        MapLocation myLocation = rc.getLocation();
        double transferAmount = 0;
        double mySupply = rc.getSupplyLevel();
        double minSupply = mySupply;

        // Low supply messaging
        if(mySupply <= RESUPPLY_CUTOFF) {
          int signal = SIG_RESUPPLY;
          double minDistance = Double.MAX_VALUE;
          int offset = 0;
          for(int i=0; i < RESUPPLY_CHANNELS; i += 2) {
            if(rc.readBroadcast(signal+i) == 0) {
              offset = i;
              break;
            }
            MapLocation loc = safeReadBroadcastMapLocation(signal+i);
            double dist = myLocation.distanceSquaredTo(loc);
            if(dist < GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
              return;
            }
            if(dist < minDistance) {
              minDistance = dist;
              offset = i;
            }
          }
          safeBroadcastMapLocation(signal+offset, rc.getLocation());
          return;
        }

        // Transfer of supplies.
        RobotInfo r_info = null;
        for(RobotInfo r : targets) {

          // Structures are ineligible to receive supply.
          if(r.type == HQ || r.type == TOWER || r.type == SUPPLYDEPOT || r.type == TECHNOLOGYINSTITUTE || r.type == TRAININGFIELD || r.type == BARRACKS ||
            r.type == TANKFACTORY || r.type == HELIPAD || r.type == AEROSPACELAB || r.type == HANDWASHSTATION || r.type == MINERFACTORY)
            continue;
          if(rc.getType() == HQ && r.type == DRONE) {
            r_info = r;
            transferAmount = mySupply / 2;
            break;
          }
          if(r.supplyLevel < minSupply) {
            minSupply = r.supplyLevel;
            r_info = r;
            transferAmount = (mySupply - minSupply) / 2;
            if(rc.getType() == HQ && minSupply < RESUPPLY_CUTOFF) {
              transferAmount = Math.min(RESUPPLY_CUTOFF - minSupply, transferAmount);
            }
            if(transferAmount >= SUPPLY_TRANSFER_CUTOFF) {
              break;
            }
          }
        }
        if(r_info != null) {
          rc.transferSupplies((int) transferAmount, r_info.location);
        }
      }
      catch (GameActionException e) {
        e.printStackTrace();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Calculates the vector sum of the directions of nearby enemies.
   *  !! This method is SAFE to use without checks. !!
   * @param enemies an array of nearby enemies.
   * @return a direction representing the direction of enemies around you.
   */
  // TODO: consider ways to make this more customizable;
  // TODO: make it a true vector sum; this way additional meaning is carried in the vector's magnitude.
  private static Direction compositeEnemyDirection(RobotInfo[] enemies) {
    if(enemies.length == 0) return Direction.NONE;

    MapLocation myLocation = rc.getLocation();
    MapLocation loc = myLocation;

    for(RobotInfo r : enemies) {
      loc = loc.add(myLocation.directionTo(r.location));
    }

    return myLocation.directionTo(loc);
  }

  /**
   * Gets the direction of optimal ore yields.
   * It ignores the safety of the direction, but does check `canMove'.
   * This method only checks, and does not actually mine ore or move.
   *  !! This method is SAFE to use without checks. !!
   * @return an optimal mining direction; or `null'.
   */
  private static Direction safeOptimalOreDirection() {
    MapLocation myLocation = rc.getLocation();

    Direction best = null;
    double ore_best = rc.senseOre(myLocation);

    for(Direction d : directions) {
      if (rc.canMove(d)) {
        double ore = rc.senseOre(myLocation.add(d));
        if (ore > ore_best) {
          best = d;
          ore_best = ore;
        }
      }
    }

    return best;
  }


  /**
   * A really basic random move algorithm.
   * This probably shouldn't ever be used.
   *  !! This method is UNSAFE and requires `isCoreReady' before use.
   * @return `true' if a move happened; `false' otherwise.
   */
  private static boolean unsafeDiffuseRandomly() {
    Direction d = directions[rand.nextInt(8)];
    if(rc.canMove(d)) {
      try {
        rc.move(d);
        return true;
      } catch (GameActionException e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  /**
   * An improvement upon random diffusing motion.
   * It checks the enemy vector sum and moves in the direction opposite to that.
   *  !! This method is UNSAFE and requires `isCoreReady' before use.
   * @param enemies an array of nearby enemies.
   * @return `true' if a move happened; `false' otherwise.
   */
  // TODO: it may be wise to add an upper threshold on magnitude to cancel the move (engagement).
  private static boolean unsafeDiffuseSafely(RobotInfo[] enemies, Direction preferred) {
    Direction ced = compositeEnemyDirection(enemies).opposite();
    if(ced == Direction.NONE) ced = (preferred == null? directions[rand.nextInt(8)] : preferred);
    Direction[] ds = getDirectionsLike( ced );
    for(Direction d : ds) {
      if(rc.canMove(d)) {
        try {
          rc.move(d);
          return true;
        } catch (GameActionException e) {
          e.printStackTrace();
        }
      }
    }
    return false;
  }

  /**
   * Tries to mine at the given location, if there's more than `cutoff' ore.
   *  !! This method is UNSAFE and requires `isCoreReady' before use.
   * @param cutoff the minimum amount of ore to mine, useful for beavers.
   * @return `true' if mining happened; `false' otherwise.
   */
  private static boolean unsafeMine(double cutoff) {
    if(rc.senseOre(rc.getLocation()) >= cutoff) {
      try {
        rc.mine();
        return true;
      }
      catch (GameActionException e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  /**
   * Reads a broadcast from the given `signal' channel.
   *  !! This method is SAFE to use without checks. !!
   * @param signal a valid signalling channel.
   * @return the integer value read; or `0'.
   */
  private static int safeReadBroadcast(final int signal) {
    try {
      return rc.readBroadcast(signal);
    }
    catch(GameActionException e) {
      e.printStackTrace();
    }
    return 0;
  }

  /**
   * Broadcasts the value over the given `signal' channel.
   *  !! This method is SAFE to use without checks. !!
   * @param signal a valid signalling channel.
   * @param value an integer value to broadcast.
   */
  private static void safeBroadcast(final int signal, int value) {
    try {
      rc.broadcast(signal, value);
    }
    catch(GameActionException e) {
      e.printStackTrace();
    }
  }

  /**
   * Broadcasts a map location to the given `signal' channel index.
   * Using the current system, map locations are broadcast pairwise
   * And at adjacent indexes. (So the x-component is at `signal' and
   * The y-component is at `signal'+1).
   *  !! This method is SAFE to use without any checks. !!
   * @param signal a valid signal channel for this map location.
   * @param loc a valid map location.
   */
  private static void safeBroadcastMapLocation(final int signal, MapLocation loc) {
    if(loc == null) return;
    try {
      rc.broadcast(signal, loc.x);
      rc.broadcast(signal+1, loc.y);
    }
    catch (GameActionException e) {
      e.printStackTrace();
    }
  }

  /**
   * Sets the `flag' on the given `signal' channel.
   *  !! This method is SAFE to use without any checks. !!
   * @param signal a valid signal channel.
   * @param flag a valid integer flag.
   */
  private static void safeBroadcastSetFlag(final int signal, final int flag) {
    int value = 0;
    try {
      value = rc.readBroadcast(signal);
      rc.broadcast(signal, value | flag);
    }
    catch (GameActionException e) {
      e.printStackTrace();
    }
  }

  /**
   * Unsets the `flag' on the given `signal' channel.
   *  !! This method is SAFE to use without any checks. !!
   * @param signal a valid signal channel.
   * @param flag a valid integer flag.
   */
  private static void safeBroadcastUnsetFlag(final int signal, final int flag) {
    int value = 0;
    try {
      value = rc.readBroadcast(signal);
      rc.broadcast(signal, value & ~flag);
    }
    catch (GameActionException e) {
      e.printStackTrace();
    }
  }

  /**
   * Tests whether `loc' is not attackable from any of the enemies structures.
   * @param loc a valid map location.
   * @return `true' if `loc' is safe; `false' otherwise.
   */
  // TODO: add sister method `hiddenGround(boolean strict)'
  private static boolean safeGround(MapLocation loc) {
    if(loc.distanceSquaredTo(enemyHQ) <= HQ.attackRadiusSquared) return false;
    for(MapLocation tower : enemyTowers) {
      if(tower.distanceSquaredTo(loc) <= TOWER.attackRadiusSquared) {
        return false;
      }
    }
    return true;
  }

  /**
   * Tries to launch a missile in the given direction `d'.
   *  !! This method is UNSAFE and requires `isWeaponReady' before use. !!
   * @param d a launch direction.
   */
  private static void unsafeLaunchMissile(Direction d) {
    if(rc.getMissileCount() <= 0) return;
    if(d == null) return;
    try {
      if(rc.canLaunch(d)) {
        rc.launchMissile(d);
      }
    }
    catch (GameActionException e) {
      e.printStackTrace();
    }
  }

  /**
   * A convenience method for pathing; gets all directions which
   * make progress toward the given map location `loc'.
   *  !! This method is SAFE to use without any checks. !!
   * @param loc a valid map location.
   * @return an array of valid directions; or `null'.
   */
  private static Direction[] getDirectionsTo(MapLocation loc) {
    if(loc == null) return null;
    Direction d = rc.getLocation().directionTo(loc);
    return getDirectionsLike(d);
  }

  /**
   * Gets all directions "in the same pathing order" as `d'.
   *   !! This method is SAFE to use without any checks. !!
   * @param d a valid direction; or `Direction.NONE'.
   * @return an array of directions "like" `d'.
   */
  private static Direction[] getDirectionsLike(Direction d) {
    if(d == null) return null;
    return new Direction[] {
      d, d.rotateLeft(), d.rotateRight(),
      d.rotateLeft().rotateLeft(),
      d.rotateRight().rotateRight()
    };
  }

  /**
   * Reads a map location from the given `signal' channel index.
   * Using the current system, map locations are broadcast pairwise
   * And at adjacent indexes. (So the x-component is at `signal' and
   * The y-component is at `signal'+1).
   * @param signal a valid signal channel to read from.
   * @return a new map location.
   */
  private static MapLocation safeReadBroadcastMapLocation(final int signal) {

    int x = 0, y = 0;
    MapLocation loc = null;

    try {
      x = rc.readBroadcast(signal);
      y = rc.readBroadcast(signal+1);

      loc = new MapLocation(x,y);
    }
    catch (GameActionException e) {
      e.printStackTrace();
    }

    return loc;
  }

  /**
   * Performs the swam rally action if the `flag' is set.
   * @param flag a rally flag on the swarm signal channel.
   * @return `true' if the rally action was performed; `false' otherwise.
   */
  private static boolean unsafeSwarm(final int flag) {
    int value = 0;
    try {
      value = rc.readBroadcast(SIG_SWARM) & flag;
      if(value != 0) {

        MapLocation dest = null;

        if(flag == SWARM_NEAR_HQ) {
          dest = myHQ;
        }
        else if(flag == SWARM_NEAR_MID) {
          dest = mid;
        }
        else {
          return false;
        }

        Direction[] ds = getDirectionsTo(dest);
        for(Direction d : ds) {
          if(rc.canMove(d)) {
            rc.move(d);
            break;
          }
        }

        return true;
      }
    }
    catch (GameActionException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * An algorithm for routing robots to resupply, and filling resupply orders.
   *  !! This method is UNSAFE and requires `isCoreReady' before use. !!
   *  @param who a valid robot type representing the donor robot to be routed.
   *  @return `true' if the robot was moved; `false' otherwise.
   */
  private static boolean unsafeRouteResupply(RobotType who) {
    try {

      double mySupply = rc.getSupplyLevel();

      // If not enough supply, route to HQ.
      if(mySupply <= RESUPPLY_CUTOFF) {
        Direction[] ds = getDirectionsTo(myHQ);

        for(Direction d : ds) {
          if(rc.canMove(d)) {
            rc.move(d);
            return true;
          }
        }

        return true;
      }

      MapLocation[] locations = new MapLocation[RESUPPLY_CHANNELS/2];

      // Get an array of locations
      for(int i=SIG_RESUPPLY, j=0; i < SIG_RESUPPLY + RESUPPLY_CHANNELS; i += 2, j++) {
        if(rc.readBroadcast(i) == 0) continue;
        locations[j] = safeReadBroadcastMapLocation(i);
      }

      MapLocation myLocation = rc.getLocation();
      MapLocation closestSupply = null;
      double closestDistance = Double.MAX_VALUE;

      // Try to find the closest resupply order.
      for(MapLocation loc : locations) {
        if(loc == null) continue;
        double dist = myLocation.distanceSquaredTo(loc);
        if(dist < closestDistance) {
          closestDistance = dist;
          closestSupply = loc;
        }
      }

      // If one is found route to it.
      if(closestSupply != null) {

        // If within resupply radius, fill the order and reset the message channels.
        if(closestDistance < GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
          RobotInfo[] targets = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, myTeam);

          double health = 0;
          RobotInfo healthiest = null;

          // Just give all our supply to the healthiest eligible bot.
          for(RobotInfo r : targets) {
            if(r.type == TOWER || r.type == HQ || r.type == SUPPLYDEPOT || r.type == TECHNOLOGYINSTITUTE || r.type == TRAININGFIELD || r.type == BARRACKS ||
              r.type == TANKFACTORY || r.type == HELIPAD || r.type == AEROSPACELAB || r.type == HANDWASHSTATION || r.type == MINERFACTORY)
              continue;
            if(r.health > health) {
              health = r.health;
              healthiest = r;
            }
          }

          if(healthiest != null) {
            rc.transferSupplies((int) (mySupply / 2), healthiest.location);
          }

          int i=SIG_RESUPPLY;
          for(MapLocation loc : locations) {
            if(loc == null) continue;
            double distance = myLocation.distanceSquaredTo(loc);
            if(distance <= who.sensorRadiusSquared) {
              rc.broadcast(i, 0);
            }
            i+=2;
          }

          return false;
        }

        // Otherwise, route to the location.
        else {

          Direction[] ds = getDirectionsTo(closestSupply);

          for (Direction d : ds) {
            if (rc.canMove(d)) {
              rc.move(d);
              return true;
            }
          }
        }
      }

    }
    catch (GameActionException e) {
      e.printStackTrace();
    }

    return false;
  }
}
