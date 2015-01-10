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
   * Rally around our HQ.
   */
  private static final int SWARM_HOLD_OUR_HQ = 0x1;

  /**
   * Rally around our most vulnerable tower.
   */
  private static final int SWARM_HOLD_OUR_VULNERABLE_TOWERS = 0x2;

  /**
   * Rally around their most vulnerable tower.
   */
  private static final int SWARM_RALLY_THEIR_VULNERABLE_TOWERS = 0x4;

  /**
   * Rally around their HQ.
   */
  private static final int SWARM_RALLY_THEIR_HQ = 0x8;

  /**
   * The length of a typical rally before signalling an engage.
   */
  private static final int RALLY_LENGTH = 200;

  /**
   * Engage their most vulnerable tower.
   */
  private static final int SWARM_ENGAGE_THEIR_VULNERABLE_TOWERS = 0x10;

  /**
   * Engage their HQ.
   */
  private static final int SWARM_ENGAGE_THEIR_HQ = 0x20;

  /**
   * The tech. institute rate channel
   */
  private static final int SIG_TECH_INSTITUTE_RATE = 15;

  /**
   * Signal space allocated to resupply map locations.
   */
  private static final int SIG_RESUPPLY = 16;

  /**
   * The resupply size (actual size, i.e. number of x+y components).
   */
  private static final int RESUPPLY_CHANNELS = 20;

  /**
   * The training field rate channel.
   */
  private static final int SIG_TRAINING_FIELD_RATE = 37;

  /**
   * The commander rate channel.
   */
  private static final int SIG_COMMANDER_RATE = 38;

  /**
   * My towers hit points signal channel (takes up 6 adjacent channels).
   */
  private static final int SIG_OUR_TOWER_HP = 39;

  /**
   * Enemy tower hit points signal channel (takes up 6 adjacent channels).
   */
  private static final int SIG_THEIR_TOWER_HP = 45;

  /**
   * Our most vulnerable tower's map location (51 & 52).
   */
  private static final int SIG_OUR_VULNERABLE_TOWER = 51;

  /**
   * Their most vulnerable tower's map location ( 53 & 54).
   */
  private static final int SIG_THEIR_VULNERABLE_TOWER = 53;

  /**
   * The "birthday" of a rally lets us see how long we've been at it.
   */
  private static final int SIG_RALLY_CLOCK = 55;

  /**
   * A constant for undiscovered enemy towers.
   */

  private static final int TOWER_HP_UNKNOWN = -1;

  /**
   * The cutoff for sending a resupply signal.
   */
  private static final int RESUPPLY_CUTOFF = 500;

  /**
   * The minimum supply needed to cause a cutoff in supply transfer search.
   */
  private static final double SUPPLY_TRANSFER_CUTOFF = 500;

  /**
   * The percent of a units maximum HP needed to be under to consider all threats in safe diffuse.
   */
  private static final double CHICKEN_CUTOFF = 0.2;

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
  private static MapLocation[] ourTowers;

  /**
   * The space between both teams' HQs
   */
  private static MapLocation mid;

  /**
   * A pseudo-random number generator instance.
   */
  private static final Random rand = new Random();

  /**
   * Can be used to keep track of a special map location;
   * Used by drones to keep track of supply order jobs.
   */
  private static MapLocation destination = null;

  /**
   * Used to keep track of the round this unit was born.
   */
  private static int birthday;

  /**
   * The health of this robot set from the last round;
   * Useful to see if we've taken damage.
   * Used by towers to report changes to their HP status.
   * Generally useful for vulnerables, such as structures.
   *  !! Warning: not set by all bots. !!
   */
  private static double health;

  /**
   * The index of a tower as it corresponds to the array.
   */
  private static Integer towerIndex = null;

  /**
   * Runs the `RobotPlayer', i.e. all game logic.
   * @param controller the robot controller instance.
   */
  public static void run(RobotController controller) {

    rc = controller; // Set the robot.
    myTeam = rc.getTeam(); // Set my team.
    otherTeam = myTeam.opponent(); // Set the enemy team.
    myHQ = rc.senseHQLocation();
    ourTowers = rc.senseTowerLocations();
    enemyHQ = rc.senseEnemyHQLocation();
    rushHQ = enemyHQ.add(enemyHQ.directionTo(myHQ), LAUNCHER.attackRadiusSquared-1);
    enemyTowers = rc.senseEnemyTowerLocations();
    birthday = Clock.getRoundNum();

    /**
     * Used by robots who have an associated direction;
     * Currently used by missiles to decide which way to travel.
     */
    Direction facing = null;

    /*
     * The method is encapsulated in a forever-loop.
     * It should run for as long as the match running.
     */
    // TODO: keep an ArrayList of dependency expectations. (see DependencyProgress). If one hasn't been met, and it should be, we KNOW its been destroyed and we can rebuild.
    while(true) {

      // Get the type of the active robot.
      RobotType t = rc.getType();

      if(Clock.getRoundNum() % 50 == 0) {
        safeBroadcastNearbyTowers();
      }


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

        safeTransferHQSupply(); // sharing is caring.

        // get round number
        int round = Clock.getRoundNum();

        // Re evaluate endgame strategy every 50 turns.
        if(round > 1200 && round % 50 == 0) {
          int value = safeReadBroadcast(SIG_SWARM);

          boolean HQSwarm = (value & SWARM_RALLY_THEIR_HQ) != 0;
          boolean towerSwarm = (value & SWARM_RALLY_THEIR_VULNERABLE_TOWERS) != 0;

          // When rallying becomes an engagement.
          if(HQSwarm || towerSwarm) {

            int rallyLength = round - safeReadBroadcast(SIG_RALLY_CLOCK);
            boolean isCrunch = GameConstants.ROUND_MAX_LIMIT - round < RALLY_LENGTH - rallyLength;

            // test if the rally should end.
            if (rallyLength >= RALLY_LENGTH || isCrunch) {

              if (towerSwarm) {
                safeBroadcastSetFlag(SIG_SWARM, SWARM_ENGAGE_THEIR_VULNERABLE_TOWERS);
              }
              else if (HQSwarm) {
                safeBroadcastSetFlag(SIG_SWARM, SWARM_ENGAGE_THEIR_HQ);
              }
            }
          }

          // Recalculate an endgame strategy
          safeBroadcastEndgameStrategy();
        }

        if(round == 1500) {
          rc.setIndicatorString(0, "Phase 9");
          safeBroadcast(SIG_LAUNCHER_RATE, 10);
        }
        if(round == 1000) {
          rc.setIndicatorString(0, "Phase 8");
          safeBroadcast(SIG_HELIPAD_RATE, 2);
          safeBroadcast(SIG_DRONE_RATE, 10);
          safeBroadcast(SIG_LAUNCHER_RATE, 20);
        }
        if(round == 804) {
          rc.setIndicatorString(0, "Phase 8");
          safeBroadcast(SIG_LAUNCHER_RATE, 10);
        }
        if(round == 644) {
          rc.setIndicatorString(0, "Phase 7");
          safeBroadcast(SIG_AERO_LAB_RATE, 2);
          safeBroadcast(SIG_DEPOT_RATE, 1);
          safeBroadcast(SIG_LAUNCHER_RATE, 5);
        }
        else if(round == 493) {
          rc.setIndicatorString(0, "Phase 6");
          safeBroadcast(SIG_MINER_RATE, 20);
          safeBroadcast(SIG_DRONE_RATE, 4);
          safeBroadcast(SIG_LAUNCHER_RATE, 4);
        }
        else if(round == 393) {
          rc.setIndicatorString(0, "Phase 5");
          safeBroadcast(SIG_TANK_FACTORY_RATE, 1);
          safeBroadcast(SIG_LAUNCHER_RATE, 5);
          safeBroadcast(SIG_DEPOT_RATE, 5);
        }
        else if(round == 292) {
          rc.setIndicatorString(0, "Phase 4");
          safeBroadcast(SIG_AERO_LAB_RATE, 1);
          safeBroadcast(SIG_MINER_RATE, 2);
        }
        else if(round == 241) {
          rc.setIndicatorString(0, "Phase 3");
          safeBroadcast(SIG_MINER_RATE, 3);
          safeBroadcast(SIG_BARRACKS_RATE, 1);
        }
        else if(round == 143) {
          rc.setIndicatorString(0, "Phase 2");
          safeBroadcast(SIG_MINE_FACTORY_RATE, 1);
          safeBroadcast(SIG_DRONE_RATE, 1);
        }
        else if(round == 90) {
          rc.setIndicatorString(0, "Phase 1");
          safeBroadcast(SIG_BEAVER_RATE, 1);
          safeBroadcast(SIG_DRONE_RATE, 1);
          safeBroadcast(SIG_MINE_FACTORY_RATE, 1);
        }
        else if(round == 0) {
          rc.setIndicatorString(0, "Phase 0");
          initializeTowerHP();
          safeBroadcast(SIG_BEAVER_RATE, 3);
          safeBroadcast(SIG_HELIPAD_RATE, 1);
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

        // One-time setting of tower index.
        if(towerIndex == null) {
          int i=0;
          for(MapLocation tower : ourTowers) {
            if(tower != rc.getLocation()) {
              i++;
            }
          }
          towerIndex = i;
        }

        // Check if the tower has taken hits.
        // If it has, broadcast the update.
        // TODO: it may be prudent to send other signals here.
        if(rc.getHealth() < health) {
          safeBroadcast(SIG_OUR_TOWER_HP + towerIndex, (int) health);
        }
      }
      else if(t == BASHER) {
        /*
         * Bashers walk around and attack adjacent enemies automatically.
         */

        safeTransferSupply(); // sharing is caring.

        if(rc.isCoreReady()) {
          RobotInfo[] enemies = rc.senseNearbyRobots(BASHER.sensorRadiusSquared, otherTeam);
          unsafeDiffuseSafely(enemies, rc.getLocation().directionTo(enemyHQ), BASHER.attackPower, true);
        }
      }
      else if(t == SOLDIER) {
        // Soldiers walk around and attack things.

        safeTransferSupply(); // sharing is caring.

        if(rc.isWeaponReady() && unsafeSelectTargetAndEngage(rc.getLocation(), SOLDIER.attackRadiusSquared, SOLDIER_AUTO_ENGAGE_CUTOFF)) {
          rc.yield();
          continue;
        }
        if(rc.isCoreReady()) {
          RobotInfo[] enemies = rc.senseNearbyRobots(SOLDIER.sensorRadiusSquared, otherTeam);
          unsafeDiffuseSafely(enemies, rc.getLocation().directionTo(enemyHQ), BASHER.attackPower, true);
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
                    if(!unsafeBuild(TECHNOLOGYINSTITUTE, SIG_TECH_INSTITUTE_RATE))
                      if(!unsafeBuild(TRAININGFIELD, SIG_TRAINING_FIELD_RATE))
                        if(!unsafeBuild(TANKFACTORY, SIG_TANK_FACTORY_RATE)) {
                          RobotInfo[] enemies = rc.senseNearbyRobots(BEAVER.sensorRadiusSquared, otherTeam);
                          if (enemies.length == 0 && unsafeMine(BEAVER_MINING_CUTOFF)) {
                            rc.yield();
                            continue;
                          }
                          unsafeDiffuseSafely(enemies, null, BEAVER.attackPower, true);
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

        if(rc.isWeaponReady() && unsafeSelectTargetAndEngage(rc.getLocation(), MINER.attackRadiusSquared, MINER.attackPower)) {
          rc.yield();
          continue;
        }

        if(rc.isCoreReady()) {
          RobotInfo[] enemies = rc.senseNearbyRobots(MINER.sensorRadiusSquared, otherTeam);
          if(enemies.length > 0 || !unsafeMine(MINER_MINING_CUTOFF)) {
            Direction optimalDirection = safeOptimalOreDirection();
            unsafeDiffuseSafely(enemies, optimalDirection, MINER.attackPower, true);
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
          RobotInfo[] enemies = rc.senseNearbyRobots(TANK.sensorRadiusSquared, otherTeam);
          if(!unsafeSwarm(enemies)) {
            unsafeDiffuseSafely(enemies, rc.getLocation().directionTo(enemyHQ), TANK.attackPower, true);
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
          boolean isChickenCutoff = rc.getHealth() < CHICKEN_CUTOFF * t.maxHealth;
          if(isChickenCutoff || !unsafeRouteResupply(DRONE)) {
            RobotInfo[] enemies = rc.senseNearbyRobots(DRONE.sensorRadiusSquared, otherTeam);
            unsafeDiffuseSafely(enemies, null, COMMANDER.attackPower, true);
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
          RobotInfo[] enemies = rc.senseNearbyRobots(LAUNCHER.sensorRadiusSquared, otherTeam);
          if(!unsafeSwarm(enemies)) {
            unsafeDiffuseSafely(enemies, rc.getLocation().directionTo(enemyHQ), MISSILE.attackPower, true);
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

            else if(rc.canMove(facing) && rc.isPathable(MISSILE, rc.getLocation().add(facing))) {
              rc.move(facing);
            }
          }
          catch(GameActionException e) {
            e.printStackTrace();
          }
        }
      }
      else if(t == TECHNOLOGYINSTITUTE) {
        /**
         * Requirement for computer, training field, commander.
         */
      }
      else if(t == TRAININGFIELD) {
        /**
         * Builds the commander.
         */

        if(rc.isCoreReady()) {
          unsafeSpawn(COMMANDER, SIG_COMMANDER_RATE);
        }
      }
      else if(t == COMMANDER) {

        if(rc.isWeaponReady() && unsafeSelectTargetAndEngage(rc.getLocation(), COMMANDER.attackRadiusSquared, COMMANDER.attackPower)) {
          rc.yield();
          continue;
        }
        if(rc.isCoreReady()) {
          rc.setIndicatorString(0, "XP "+rc.getXP());

          RobotInfo[] enemies = rc.senseNearbyRobots(COMMANDER.sensorRadiusSquared, otherTeam);
          unsafeDiffuseSafely(enemies, rc.getLocation().directionTo(enemyHQ), COMMANDER.attackPower, true);
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

    if(!hasNoDirection(compositeEnemyStructureDirection())) return false;

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
   * The method has been updated to introduce an anti-clotting mechanism.
   * (Basically, sometimes beavers build things too close together.)
   *  !! This method is SAFE to use without any checks. !!
   * @param from a valid location in which to initiate the build.
   * @param robot a valid robot to try to spawn.
   * @return a valid direction or `null'.
   */
  private static Direction getValidBuildDirection(MapLocation from, RobotType robot) {
    try {
      Direction d = from.directionTo(enemyHQ);
      for(int i=0; i < 8; i++) {
        if (rc.canBuild(d, robot)) {

          MapLocation loc = from.add(d);
          boolean foundStructure = false;

          // check all locations for clotting,
          // If a structure is found, the direction `d' is not valid.
          for(Direction dir : directions) {
            RobotInfo r = rc.senseRobotAtLocation(loc.add(dir));
            if(r != null && isStructure(r.type)) {
              foundStructure = true;
              break;
            }
          }
          if(!foundStructure) {
            return d;
          }
        }
        d = d.rotateLeft();
      }
    }
    catch(GameActionException e) {
      e.printStackTrace();
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
   * namely, there is no requirement that HQ's share supply fairly, except with the drone.
   */
  private static void safeTransferHQSupply() {
    try {
      double mySupply = rc.getSupplyLevel();
      RobotInfo[] targets = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, myTeam);
      if(targets.length == 0) return;

      boolean isDrone = false;
      double minSupply = mySupply;
      double transferAmount = 0;
      RobotInfo r_info = null;

      // transfer supplies.
      for(RobotInfo r : targets) {
        if(r.supplyLevel < minSupply) {
          if(r.type == DRONE) {
            isDrone = true;
            transferAmount = mySupply / 2;
            minSupply = r.supplyLevel;
            r_info = r;
          }
          else if(! isDrone && !isStructure(r.type)) {
            minSupply = r.supplyLevel;
            transferAmount = Math.min(100, (mySupply - minSupply) / 2);
            r_info = r;
          }
        }
      }

      if(r_info != null) {
        rc.transferSupplies((int) transferAmount, r_info.location);
      }
    }
    catch(GameActionException e) {
      e.printStackTrace();
    }
  }

  /**
   * Sends a low-supply distress signal which will hopefully be picked up by a drone.
   *  !! This method is SAFE to use without checks. !!
   *  @param location a valid location of distress.
   */
  private static void safeBroadcastLowSupply(MapLocation location) {
    try {
      int signal = SIG_RESUPPLY;
      double minDistance = Double.MAX_VALUE;
      int offset = 0;
      for(int i=0; i < RESUPPLY_CHANNELS; i += 2) {
        if(rc.readBroadcast(signal+i) == 0) {
          offset = i;
          break;
        }
        MapLocation loc = safeReadBroadcastMapLocation(signal+i);
        double dist = location.distanceSquaredTo(loc);
        if(dist < GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
          return;
        }
        if(dist < minDistance) {
          minDistance = dist;
          offset = i;
        }
      }

      safeBroadcastMapLocation(signal+offset, rc.getLocation());
    }
    catch(GameActionException e) {
      e.printStackTrace();
    }
  }


  /**
   * A pretty generic method which shares supply between nearby bots.
   * It should also post a message when a non-supply duty bot is low on supply.
   *  !! This method is SAFE to use without any checks. !!
   *  !! Though it may be inappropriate for some to use. !!
   */
  private static void safeTransferSupply() {
    try {
      try {
        MapLocation myLocation = rc.getLocation();
        double mySupply = rc.getSupplyLevel();

        // Low supply messaging
        if(mySupply <= RESUPPLY_CUTOFF) {
          safeBroadcastLowSupply(myLocation);
        }

        RobotInfo[] targets = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, myTeam);
        if(targets.length == 0) return;

        // Don't donate if you don't have enough.
        if(mySupply < SUPPLY_TRANSFER_CUTOFF) return;

        RobotInfo r_info = null;
        double minSupply = mySupply;

        // Transfer of supplies.
        for(RobotInfo r : targets) {

          // Structures are ineligible to receive supply.
          // Also, sending supply back to drones should be guarded.
          if(isStructure(r.type) || r.type == DRONE) continue;

          if(r.supplyLevel < minSupply) {
            minSupply = r.supplyLevel;
            r_info = r;
          }
        }
        if(r_info != null) {
          rc.transferSupplies((int) ((mySupply - minSupply) / 2), r_info.location);
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
   * Tests if the robot is a structure.
   * @return `true' if the robot is a structure; `false' otherwise.
   */
  private static boolean isStructure(RobotType rt) {
    return rt == HQ || rt == TOWER || rt == SUPPLYDEPOT || rt == TECHNOLOGYINSTITUTE || rt == TRAININGFIELD || rt == BARRACKS ||
      rt == TANKFACTORY || rt == HELIPAD || rt == AEROSPACELAB || rt == HANDWASHSTATION || rt == MINERFACTORY;
  }

  /**
   * Calculates the vector sum of the directions of nearby enemies.
   *  !! This method is SAFE to use without checks. !!
   * @param enemies an array of nearby enemies.
   * @param threatAttackPowerThreshold the minimum amount of attack something needs to have to be considered a threat.
   * @return a direction representing the direction of enemies around you.
   */
  // TODO: make it a true vector sum; this way additional meaning is carried in the vector's magnitude.
  private static Direction compositeEnemyDirection(RobotInfo[] enemies, double threatAttackPowerThreshold, boolean includeStructures) {

    Direction towerDirection = Direction.NONE;

    // check structures.
    if(includeStructures) {
      towerDirection = compositeEnemyStructureDirection();
      if(!hasNoDirection(towerDirection)) return towerDirection;
    }

    if(enemies.length == 0) return Direction.NONE;

    MapLocation myLocation = rc.getLocation();
    MapLocation loc = myLocation.add(towerDirection);

    double attackSum = 0;
    boolean isChickenCutoff = rc.getHealth() >= CHICKEN_CUTOFF * rc.getType().maxHealth;

    // Check all eligible bots.
    for(RobotInfo r : enemies) {
      boolean inRange = myLocation.distanceSquaredTo(r.location) <= r.type.attackRadiusSquared;
      boolean launcherInRange = r.type == LAUNCHER && myLocation.distanceSquaredTo(r.location) <= r.type.sensorRadiusSquared + 4;
      if(isChickenCutoff || inRange || launcherInRange) {
        loc = loc.add(myLocation.directionTo(r.location));
        attackSum += (r.type == LAUNCHER? MISSILE.attackPower : r.type.attackPower);
      }
    }

    // If the numbers don't add up, return NONE.
    if(!isChickenCutoff && attackSum <= threatAttackPowerThreshold) {
      return Direction.NONE;
    }

    return myLocation.directionTo(loc);
  }

  /**
   * Tests if a direction is NONE or OMNI
   * @return `true' if a direction is NONE or OMNI; `false' otherwise.
   */
  private static boolean hasNoDirection(Direction d) {
    return d == Direction.NONE || d == Direction.OMNI;
  }

  /**
   * Returns the direction to an enemy tower in range.
   * @return a direction to an enemy tower or NONE, OMNI
   */
  private static Direction compositeEnemyStructureDirection() {

    MapLocation myLocation = rc.getLocation();
    MapLocation loc = myLocation;

    if(myLocation.distanceSquaredTo(enemyHQ) < HQ.sensorRadiusSquared + 4) {
      loc = loc.add(myLocation.directionTo(enemyHQ));
    }

    for(MapLocation tower : enemyTowers) {
      if(myLocation.distanceSquaredTo(tower) < TOWER.sensorRadiusSquared + 4) {
        loc = loc.add(myLocation.directionTo(tower));
      }
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
    Direction toHQ = myLocation.directionTo(enemyHQ);

    Direction best = null;
    double ore_best = rc.senseOre(myLocation);

    if (rc.canMove(toHQ)) {
      double ore = rc.senseOre(myLocation.add(toHQ));
      if (ore >= ore_best) {
        best = toHQ;
        ore_best = ore;
      }
    }

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
   * @param threatAttackPowerThreshold the minimum amount of attack something needs to have to be considered a threat.
   * @return `true' if a move happened; `false' otherwise.
   */
  // TODO: it may be wise to add an upper threshold on magnitude to cancel the move (engagement).
  private static boolean unsafeDiffuseSafely(RobotInfo[] enemies, Direction preferred, double threatAttackPowerThreshold, boolean avoidStructures) {
    Direction ced = compositeEnemyDirection(enemies, threatAttackPowerThreshold, avoidStructures).opposite();
    if(hasNoDirection(ced)) ced = (preferred == null? directions[rand.nextInt(8)] : preferred);
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
   * @param enemies nearby enemies.
   * @return `true' if the rally action was performed; `false' otherwise.
   */
  private static boolean unsafeSwarm(RobotInfo[] enemies) {

    int value = 0;
    try {
      value = rc.readBroadcast(SIG_SWARM);

      if(value != 0) {

        if((value & SWARM_ENGAGE_THEIR_VULNERABLE_TOWERS) != 0) {
          MapLocation vulnerableTower = safeReadBroadcastMapLocation(SIG_THEIR_VULNERABLE_TOWER);

          rc.setIndicatorString(1, "swarming: "+vulnerableTower);

          Direction[] ds = getDirectionsTo(vulnerableTower);

          for(Direction d : ds) {
            if(rc.canMove(d)) {
              rc.move(d);
              return true;
            }
          }
        }
        else if((value & SWARM_ENGAGE_THEIR_HQ) != 0) {

          Direction[] ds = getDirectionsTo(enemyHQ);

          for(Direction d : ds) {
            if(rc.canMove(d)) {
              rc.move(d);
              return true;
            }
          }
        }
        else if((value & SWARM_HOLD_OUR_HQ) != 0) {
          unsafeDiffuseSafely(enemies, rc.getLocation().directionTo(myHQ), 0, true);
          return true;
        }
        else if((value & SWARM_HOLD_OUR_VULNERABLE_TOWERS) != 0) {
          MapLocation vulnerableTower = safeReadBroadcastMapLocation(SIG_OUR_VULNERABLE_TOWER);
          unsafeDiffuseSafely(enemies, rc.getLocation().directionTo(vulnerableTower), 0, true);
          return true;
        }
        else if((value & SWARM_RALLY_THEIR_HQ) != 0) {
          unsafeDiffuseSafely(enemies, rc.getLocation().directionTo(enemyHQ), 0, true);
          return true;
        }
        else if((value & SWARM_RALLY_THEIR_VULNERABLE_TOWERS) != 0) {
          MapLocation vulnerableTower = safeReadBroadcastMapLocation(SIG_THEIR_VULNERABLE_TOWER);

          rc.setIndicatorString(0, "rallying: "+vulnerableTower);

          unsafeDiffuseSafely(enemies, rc.getLocation().directionTo(vulnerableTower), 0, true);
          return true;
        }
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
        RobotInfo[] nearby = rc.senseNearbyRobots(who.sensorRadiusSquared, otherTeam);
        return unsafeDiffuseSafely(nearby, rc.getLocation().directionTo(myHQ), 0, true);
      }

      double closestDistance = Double.MAX_VALUE;
      MapLocation myLocation = rc.getLocation();
      MapLocation[] locations = new MapLocation[RESUPPLY_CHANNELS/2];

      // Get an array of locations
      for(int i=SIG_RESUPPLY, j=0; i < SIG_RESUPPLY + RESUPPLY_CHANNELS; i += 2, j++) {
        if(rc.readBroadcast(i) == 0) continue;
        locations[j] = safeReadBroadcastMapLocation(i);
      }

      // Try to set a resupply destination.
      if(destination == null) {

        // Try to find the closest resupply order.
        for(MapLocation loc : locations) {
          if(loc == null) continue;
          double dist = myLocation.distanceSquaredTo(loc);
          if(dist < closestDistance) {
            closestDistance = dist;
            destination = loc;
          }
        }
      }
      else {
        closestDistance = myLocation.distanceSquaredTo(destination);
      }

      // If a destination is set, either move to the location or resupply the location.
      if(destination != null) {

        // If within resupply radius, fill the order and reset the message channels.
        if(closestDistance < GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
          RobotInfo[] targets = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, myTeam);

          double supply = 0;
          RobotInfo best = null;

          // Just give all our supply to the most eligible bot.
          for(RobotInfo r : targets) {
            if(isStructure(r.type) | r.type == DRONE) continue;
            if(r.supplyLevel >  supply && r.health > r.type.maxHealth * 0.1) {
              supply = r.supplyLevel;
              best = r;
            }
          }

          if(best != null) {
            rc.transferSupplies((int) (mySupply / 2), best.location);
          }

          int i=SIG_RESUPPLY;

          // Reset the resupply distress channels close to this location.
          for(MapLocation loc : locations) {
            if(loc == null) continue;
            double distance = myLocation.distanceSquaredTo(loc);
            if(distance <= GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
              rc.broadcast(i, 0);
            }
            i+=2;
          }

          // Reset the destination.
          destination = null;

          return false;
        }

        // Otherwise, we haven't reached the destination yet, so route to it.
        else {

          RobotInfo[] nearby = rc.senseNearbyRobots(who.sensorRadiusSquared, otherTeam);
          return unsafeDiffuseSafely(nearby, rc.getLocation().directionTo(destination), 0, true);
        }
      }

    }
    catch (GameActionException e) {
      e.printStackTrace();
    }

    return false;
  }

  /**
   * A one-time call to initialize tower HP.
   */
  private static void initializeTowerHP() {
    for(int i=0; i < ourTowers.length; i++) {
      safeBroadcast(SIG_OUR_TOWER_HP+i, (int) TOWER.maxHealth);
    }
    for(int i=0; i < ourTowers.length; i++) {
      safeBroadcast(SIG_THEIR_TOWER_HP +i, TOWER_HP_UNKNOWN);
    }
  }

  /**
   * Calculates the theoretical most vulnerable tower
   * based on the given array of tower locations.
   * It ends up returning the closest tower which is most deviated from the average tower location.
   * @param towerLocations an array of valid tower locations.
   * @param towerHP although tower HP is not factored into the calculation of
   *                theoretical tower weakness, this is used to detect if a tower is alive.
   * @return the map location of the theoretically "most vulnerable" tower.
   */
  private static MapLocation getMostTheoreticVulnerableTower(MapLocation[] towerLocations, double[] towerHP) {

    int x = 0;
    int y = 0;
    int i = 0;

    double[] distances = new double[towerLocations.length];

    for(MapLocation tower : towerLocations) {
      if(towerHP[i] == 0) {
        distances[i++] = Double.MAX_VALUE;
      }
      else {
        distances[i++] = myHQ.distanceSquaredTo(tower);
        x += tower.x;
        y += tower.y;
      }
    }

    MapLocation average = new MapLocation(x / towerLocations.length, y / towerLocations.length);


    double closest = distances[0];
    MapLocation best = towerLocations[0];

    for(i=1; i < towerLocations.length; i++) {
      double value = distances[i] - towerLocations[i].distanceSquaredTo(average);
      if(value < closest) {
        closest = value;
        best = towerLocations[i];
      }
    }

    return best;

  }


  /**
   * A call to calculate an endgame strategy.
   * Ideally, the method could be re called at any time to refresh the strategy.
   */
  private static void safeBroadcastEndgameStrategy() {
    try {

      boolean isOurTowerDamaged = false;
      boolean isTheirTowerDamaged = false;

      double[] ourTowerHP = new double[ourTowers.length];
      double[] theirTowerHP = new double[ourTowers.length];
      double ourMinHP = TOWER.maxHealth;
      double theirMinHP = TOWER.maxHealth;
      MapLocation theirMinTower = null;
      MapLocation ourMinTower = null;

      int ourDestroyedCount = 0;
      int theirDestroyedCount = 0;

      // Loop through all tower channels and collect all info.
      // This loop is for both our towers and theirs.
      for (int i = 0; i < ourTowers.length; i++) {
        int ourTower = rc.readBroadcast(SIG_OUR_TOWER_HP + i);
        int theirTower = rc.readBroadcast(SIG_THEIR_TOWER_HP + i);

        ourTowerHP[i] = ourTower;
        theirTowerHP[i] = theirTower;

        if(ourTower == 0) {
          ourDestroyedCount++;
        }

        if(theirTower == 0) {
          theirDestroyedCount++;
        }

        // Calculates minimums.
        if(ourTower != 0 && ourTower < ourMinHP) {
          ourMinHP = ourTower;
          ourMinTower = ourTowers[i];
        }
        if(theirTower != TOWER_HP_UNKNOWN && theirTower != 0 && theirTower < theirMinHP) {
          theirMinHP = theirTower;
          theirMinTower = ourTowers[i];
        }

        // Sense damage.
        if(ourTower != 0 && ourTower < TOWER.maxHealth) {
          isOurTowerDamaged = true;
        }
        if(theirTower != 0 && theirTower != TOWER_HP_UNKNOWN && theirTower < TOWER.maxHealth) {
          isTheirTowerDamaged = true;
        }
      }

      // Calculate the theoretical weakest tower, if none could be found.
      /**
       * So, towers are more powerful if they are clumped together.
       * Obviously, since their fire is concentrated they can do more damage.
       * However, sometimes a tower is quite far from the average "an outlier."
       * Such a tower is weak and can be targeted.
       */
      if(ourMinHP == TOWER.maxHealth) {
        ourMinTower = getMostTheoreticVulnerableTower(ourTowers, ourTowerHP);
      }
      if(theirMinHP == TOWER.maxHealth) {
        theirMinTower = getMostTheoreticVulnerableTower(enemyTowers, theirTowerHP);
      }

      // broadcast vulnerable tower locations.
      safeBroadcastMapLocation(SIG_OUR_VULNERABLE_TOWER, ourMinTower);
      safeBroadcastMapLocation(SIG_THEIR_VULNERABLE_TOWER, theirMinTower);


      int ourTowersLeft = ourTowers.length - ourDestroyedCount;
      int theirTowersLeft = enemyTowers.length - theirDestroyedCount;
      int clock = safeReadBroadcast(SIG_RALLY_CLOCK);

      // Are we winning (or tied)?
      if(ourTowersLeft >= theirTowersLeft) {
        /**
         * Employing a conservative strategy, if winning, and no
         * damaged enemy towers exist, be defensive.
         * Rally all troops to towers and weaker troops to defend buildings @ HQ.
         *
         * If our damaged towers exist, rally all capable troops to this tower.
         *
         * If we are tied and no damaged towers exist, its high time to create one.
         * Form a rally group outside the "most vulnerable" tower and wait to attack.
         *
         * If damaged enemy towers exist, and it is possible to destroy one, go for it.
         *
         * Otherwise, if no towers exist, rush the HQ.
         */

        // all tied up. rally up! should attack soon.
        if(ourTowersLeft == theirTowersLeft || isTheirTowerDamaged) {
          safeBroadcastSetFlag(SIG_SWARM, SWARM_RALLY_THEIR_VULNERABLE_TOWERS);
          if(clock == 0) safeBroadcast(SIG_RALLY_CLOCK, Clock.getRoundNum());
        }

        // ok, not terrible but be defensive.
        else if(isOurTowerDamaged) {
          safeBroadcastSetFlag(SIG_SWARM, SWARM_HOLD_OUR_VULNERABLE_TOWERS);
          safeBroadcastSetFlag(SIG_SWARM, SWARM_HOLD_OUR_HQ);
        }
      }

      // Are we losing?
      else if(theirTowersLeft > ourTowersLeft) {
        /**
         * Damn, we hoped it wouldn't come to this.
         * If we are losing and no damaged enemy towers exist, its high time to create one.
         * Form a rally group outside the "most vulnerable" tower and wait to attack.
         *
         * Even if one of our towers is critically damaged, at this stage we are losing and
         * its a bad idea to get into a defensive mode.
         */

        safeBroadcastSetFlag(SIG_SWARM, SWARM_RALLY_THEIR_VULNERABLE_TOWERS);
        safeBroadcastSetFlag(SIG_SWARM, SWARM_HOLD_OUR_HQ);
        if(clock == 0) safeBroadcast(SIG_RALLY_CLOCK, Clock.getRoundNum());
      }

    }
    catch (GameActionException e) {
      e.printStackTrace();
    }
  }

  /**
   * Update this tower.
   * @param tower a valid tower.
   */
  private static void broadcastTowerUpdate(RobotInfo tower) {

    try {
      int offset = 0;
      int health = 0;
      boolean found = false;
      int signal = 0;

      // if my team
      if (tower.team == myTeam) {
        for (MapLocation t : ourTowers) {
          if (t == tower.location) {
            found = true;
            break;
          }
          offset++;
        }
        signal = SIG_OUR_TOWER_HP;
      }

      // otherwise,
      else {
        for (MapLocation t : enemyTowers) {
          if (t == tower.location) {
            found = true;
            break;
          }
          offset++;
        }
        signal = SIG_THEIR_TOWER_HP;
      }

      health = rc.readBroadcast(signal + offset);

      rc.setIndicatorString(2, "" + found + " && " + tower.health + " < " + health + " || " + health + " == " + TOWER_HP_UNKNOWN);

      if(found && (int) tower.health < health || health == TOWER_HP_UNKNOWN) {

        if(health == 0) {
          rc.setIndicatorString(2, "tower @ "+tower.location + " has " + health + " health.");
        }

        rc.broadcast(signal + offset, (int) Math.round(tower.health));
      }
    }
    catch (GameActionException e ) {
      e.printStackTrace();
    }
  }

  /**
   * Broadcasts periodic updates on tower sightings.
   */

  private static void safeBroadcastNearbyTowers() {

    MapLocation myLocation = rc.getLocation();

    boolean inRangeOfOurTower = false;
    boolean inRangeOfTheirTower = false;
    int i = 0;

    // search through our towers to see if we can see any.
    for(MapLocation tower : ourTowers) {
      if(myLocation.distanceSquaredTo(tower) <= rc.getType().sensorRadiusSquared) {
        inRangeOfOurTower = true;
        break;
      }
      i++;
    }

    if(!inRangeOfOurTower) {
      // search through our enemy's towers to see if we can see any.
      for(MapLocation tower : enemyTowers) {
        if(myLocation.distanceSquaredTo(tower) <= rc.getType().sensorRadiusSquared) {
          inRangeOfTheirTower = true;
          break;
        }
        i++;
      }

      // give up.
      if(!inRangeOfTheirTower) return;
    }

    RobotInfo[] nearbyRobots = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared);

    for(RobotInfo r : nearbyRobots) {
      if(r.type == TOWER) {
        broadcastTowerUpdate(r);
      }
    }
  }
}
