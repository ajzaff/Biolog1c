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
  private static final double MINER_MINING_CUTOFF = 20;

  /**
   * The minimum supply to consider transferring some.
   */
  private static final double SUPPLY_TRANSFER_CUTOFF = 1000;

  /**
   * The minimum supply worth transferring.
   */
  private static final double SUPPLY_TRANSFER_QUANTUM = 500;

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
   * Units of this type will not be constructed -- the default initial state.
   */
  private static final int RATE_HALT = 0;

  /**
   * Units of this type are constructed very slowly.
   */
  private static final int RATE_MINIMAL = 1;

  /**
   * Units of this type are constructed slower than normal.
   */
  private static final int RATE_SLOW = 2;

  /**
   * Units of this type are constructed normally.
   */
  private static final int RATE_NORMAL = 3;

  /**
   * Units of this type are constructed rapidly.
   */
  private static final int RATE_RAPID = 4;

  /**
   * Units of this type are constructed immediately.
   */
  private static final int RATE_IMMEDIATE = 5;

  /**
   * The swarm signal
   */
  private static final int SIG_SWARM = 14;

  /**
   * All units behave normally.
   */
  private static final int SWARM_DISABLED = 0;

  /**
   * All units observe their respective rally points
   */
  private static final int SWARM_RALLY = 1;

  /**
   * All units rush the HQ.
   */
  private static final int SWARM_RUSH = 2;

  /**
   * The channel for soldier units observing a rally.
   */
  private static final int SIG_RALLY_A = 15;

  /**
   * The channel for basher units observing a rally.
   */
  private static final int SIG_RALLY_B = 17;

  /**
   * The channel for basher units observing a rally.
   */
  private static final int SIG_RALLY_C = 19;

  /**
   * The effective starting round for each "phase" of the game starting from phase 0.
   */
  private static final int[] PHASES = new int[] {
    0,   /* phase 0: spawn a beaver and rush barracks. */
    30,  /* phase 1: build a few soldiers & bashers, and a few beavers. */
    80,  /* phase 2: halt all unit production to build a helipad. */
    130,  /* phase 3: spawn a few drones and save up. */
    220, /* phase 4: produce a miner factory, supply depots, and a miner. */
    240, /* phase 5: calculate rally points for all units, spawn more miners. */
    300, /* phase 6: purchase rally point barracks/helipad, depots at HQ. */
    400, /* phase 7: construct the aerospace lab. */
    450, /* phase 8: build launchers and target towers -- from here on out expect continuous missile attacks. */
    720, /* phase 9: augment soldiers, bashers, and drones, at rally points. */
    900, /* phase 10: build more depots, handwash station. */
    1000,/* phase 11: invest in tank factory and more miners. */
    1200,/* phase 12: spam and rally tanks. */
    1400,/* phase 13: augment drones at rally point. */
    1500,/* phase 14: augment bashers & soldiers. */
    1600,/* phase 15: rush the HQ. */
  };

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
   * The locations of enemy towers.
   */
  private static MapLocation[] enemyTowers;

  /**
   * The locations of my towers.
   */
  private static MapLocation[] myTowers;

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
    enemyTowers = rc.senseEnemyTowerLocations();

    Direction facing = null;

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
         * I also use it as a central message service.
         */
        /**
         * Although we do not want it to come down to this,
         * the HQ is capable of doing large amounts of damage
         * to targets.
         *
         * This routine seeks out the best such target to hit, if any.
         */

        rc.setIndicatorString(0, ""+rc.hasSpawnRequirements(LAUNCHER));

        transferSupply(); // sharing is caring.

        int round = Clock.getRoundNum();
        if(round == PHASES[15]) {
          rc.setIndicatorString(0, "Phase 15");
          safeBroadcast(SIG_SWARM, SWARM_RUSH);
        }
        else if(round == PHASES[14]) {
          rc.setIndicatorString(0, "Phase 14");
          safeBroadcast(SIG_LAUNCHER_RATE, RATE_IMMEDIATE);
        }
        else if(round == PHASES[13]) {
          rc.setIndicatorString(0, "Phase 13");
          safeBroadcast(SIG_LAUNCHER_RATE, RATE_IMMEDIATE);
        }
        else if(round == PHASES[12]) {
          rc.setIndicatorString(0, "Phase 12");
          safeBroadcast(SIG_TANK_RATE, RATE_HALT);
          safeBroadcast(SIG_LAUNCHER_RATE, RATE_IMMEDIATE);
        }
        else if(round == PHASES[11]) {
          rc.setIndicatorString(0, "Phase 11");
          safeBroadcast(SIG_TANK_FACTORY_RATE, RATE_HALT);
          safeBroadcast(SIG_TANK_RATE, RATE_IMMEDIATE);
        }
        else if(round == PHASES[10]) {
          rc.setIndicatorString(0, "Phase 10");
          safeBroadcast(SIG_LAUNCHER_RATE, RATE_HALT);
          safeBroadcast(SIG_TANK_FACTORY_RATE, RATE_IMMEDIATE);
        }
        else if(round == PHASES[9]) {
          rc.setIndicatorString(0, "Phase 9");
          safeBroadcast(SIG_LAUNCHER_RATE, RATE_IMMEDIATE);
        }
        else if(round == PHASES[8]) {
          rc.setIndicatorString(0, "Phase 8");
          safeBroadcast(SIG_MINER_RATE, RATE_HALT);
          safeBroadcast(SIG_LAUNCHER_RATE, RATE_IMMEDIATE);
        }
        else if(round == PHASES[7]) {
          rc.setIndicatorString(0, "Phase 7");
          safeBroadcast(SIG_AERO_LAB_RATE, RATE_HALT);
          safeBroadcast(SIG_LAUNCHER_RATE,RATE_IMMEDIATE);
        }
        else if(round == PHASES[6]) {
          rc.setIndicatorString(0, "Phase 6");
          safeBroadcast(SIG_AERO_LAB_RATE,RATE_HALT);
          safeBroadcast(SIG_MINER_RATE, RATE_RAPID);
        }
        else if(round == PHASES[5]) {
          rc.setIndicatorString(0, "Phase 5");
          safeBroadcast(SIG_MINER_RATE, RATE_HALT);
          safeBroadcast(SIG_AERO_LAB_RATE,RATE_IMMEDIATE);
          safeBroadcastRallyPoints();
        }
        else if(round == PHASES[4]) {
          rc.setIndicatorString(0, "Phase 4");
          safeBroadcast(SIG_MINER_RATE, RATE_RAPID);
        }
        else if(round == PHASES[3]) {
          rc.setIndicatorString(0, "Phase 3");
          safeBroadcast(SIG_MINE_FACTORY_RATE, RATE_HALT);
          safeBroadcast(SIG_MINER_RATE, RATE_IMMEDIATE);
        }
        else if(round == PHASES[2]) {
          rc.setIndicatorString(0, "Phase 2");
          safeBroadcast(SIG_DRONE_RATE, RATE_HALT);
          safeBroadcast(SIG_HELIPAD_RATE, RATE_HALT);
          safeBroadcast(SIG_MINE_FACTORY_RATE, RATE_IMMEDIATE);
        }
        else if(round == PHASES[1]) {
          rc.setIndicatorString(0, "Phase 1");
          safeBroadcast(SIG_BEAVER_RATE, RATE_HALT);
          safeBroadcast(SIG_HELIPAD_RATE, RATE_HALT);
          safeBroadcast(SIG_DRONE_RATE, RATE_IMMEDIATE);
        }
        else if(round == PHASES[0]) {
          rc.setIndicatorString(0, "Phase 0");
          safeBroadcast(SIG_BEAVER_RATE, RATE_IMMEDIATE);
          safeBroadcast(SIG_HELIPAD_RATE, RATE_IMMEDIATE);
        }

        if(rc.isWeaponReady() && unsafeSelectTargetAndEngage(rc.getLocation(), t.attackRadiusSquared, HQ_AUTO_ENGAGE_CUTOFF)) {
          rc.yield();
          continue;
        }

        if(rc.isCoreReady()) {
          int beaverRate = safeReadBroadcast(SIG_BEAVER_RATE);

          unsafeSpawn(BEAVER, beaverRate);
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

        transferSupply(); // sharing is caring.

        if(rc.isWeaponReady()) {
          unsafeSelectTargetAndEngage(rc.getLocation(), TOWER.attackRadiusSquared, TOWER_AUTO_ENGAGE_CUTOFF);
        }
      }
      else if(t == BASHER) {
        /*
         * Bashers walk around and attack adjacent enemies automatically.
         */

        transferSupply(); // sharing is caring.

        if(rc.isCoreReady()) {
          if(!unsafeSwarm(SIG_RALLY_C)) {
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

        transferSupply(); // sharing is caring.

        if(rc.isWeaponReady() && unsafeSelectTargetAndEngage(rc.getLocation(), SOLDIER.attackRadiusSquared, SOLDIER_AUTO_ENGAGE_CUTOFF)) {
          rc.yield();
          continue;
        }
        if(rc.isCoreReady()) {
          if(!unsafeSwarm(SIG_RALLY_C)) {
            unsafeDiffuseRandomly();
          }
        }
      }
      else if(t == BEAVER) {
        /*
         * Beavers are the only units capable of building structures.
         * They also have a limited ability to mine, and attack if needed.
         */

        transferSupply(); // sharing is caring.

        if(rc.isWeaponReady() && unsafeSelectTargetAndEngage(rc.getLocation(), BEAVER.attackRadiusSquared, BEAVER_AUTO_ENGAGE_CUTOFF)) {
          rc.yield();
          continue;
        }
        if(rc.isCoreReady()) {
          int barracksRate = safeReadBroadcast(SIG_BARRACKS_RATE);
          int depotRate = safeReadBroadcast(SIG_DEPOT_RATE);
          int mineFactoryRate = safeReadBroadcast(SIG_MINE_FACTORY_RATE);
          int helipadRate = safeReadBroadcast(SIG_HELIPAD_RATE);
          int aeroLabRate = safeReadBroadcast(SIG_AERO_LAB_RATE);
          int tankFactoryRate = safeReadBroadcast(SIG_TANK_FACTORY_RATE);

          if(!unsafeBuild(BARRACKS, barracksRate))
            if(!unsafeBuild(SUPPLYDEPOT, depotRate))
              if(!unsafeBuild(MINERFACTORY, mineFactoryRate))
                if(!unsafeBuild(HELIPAD, helipadRate))
                  if(!unsafeBuild(AEROSPACELAB, aeroLabRate))
                    if(!unsafeBuild(TANKFACTORY, tankFactoryRate)) {
                      if (chance(90) && unsafeMine(BEAVER_MINING_CUTOFF)) {
                        rc.yield();
                        continue;
                      }

                      unsafeDiffuseRandomly();
                    }
        }
      }
      else if(t == BARRACKS) {
        /*
         * Spawn bashers and soldiers.
         */

        transferSupply(); // sharing is caring.

        if(rc.isCoreReady()) {
          int soldierRate = safeReadBroadcast(SIG_SOLDIER_RATE);
          int basherRate = safeReadBroadcast(SIG_BASHER_RATE);

          if(!unsafeSpawn(SOLDIER, soldierRate)) {
            unsafeSpawn(BASHER, basherRate);
          }
        }
      }
      else if(t == MINERFACTORY) {
        /*
         * Has the sole responsibility of producing miners.
         */

        transferSupply(); // sharing is caring.

        if(rc.isCoreReady()) {
          int minerRate = safeReadBroadcast(SIG_MINER_RATE);

          unsafeSpawn(MINER, minerRate);
        }
      }
      else if(t == MINER) {
        /**
         * Responsible for mining.
         */

        transferSupply(); // sharing is caring.

        if(rc.isCoreReady()) {
          if(!unsafeMine(MINER_MINING_CUTOFF)) {
            unsafeDiffuseRandomly();
          }
        }
      }
      else if(t == HELIPAD) {
        /**
         * Spawns drones.
         */

        transferSupply(); // sharing is caring.

        if(rc.isCoreReady()) {
          int droneRate = safeReadBroadcast(SIG_DRONE_RATE);

          unsafeSpawn(DRONE, droneRate);
        }
      }
      else if(t == AEROSPACELAB) {
        /**
         * Spawns launchers.
         */

        transferSupply(); // sharing is caring.

        if(rc.isCoreReady()) {
          int rate = safeReadBroadcast(SIG_LAUNCHER_RATE);

          unsafeSpawn(LAUNCHER, rate);
        }
      }
      else if(t == TANKFACTORY) {
        /**
         * Spawns launchers.
         */

        transferSupply(); // sharing is caring.

        if(rc.isCoreReady()) {
          int rate = safeReadBroadcast(SIG_TANK_RATE);

          unsafeSpawn(TANK, rate);
        }
      }
      else if(t == TANK) {
        /**
         * Tanks are powerful long-range units.
         */

        transferSupply(); // sharing is caring.

        if(rc.isWeaponReady() && unsafeSelectTargetAndEngage(rc.getLocation(), TANK.attackRadiusSquared, TANK.attackPower)) {
          rc.yield();
          continue;
        }
        if(rc.isCoreReady()) {
          if(!unsafeSwarm(SIG_RALLY_A)) {
            unsafeDiffuseRandomly();
          }
        }
      }
      else if(t == DRONE) {
        /**
         * Drones can fly over shit.
         */

        transferSupply(); // sharing is caring.

        if(rc.isWeaponReady() && unsafeSelectTargetAndEngage(rc.getLocation(), DRONE.attackRadiusSquared, DRONE.attackPower)) {
          rc.yield();
          continue;
        }
        if(rc.isCoreReady()) {
          if(!unsafeSwarm(SIG_RALLY_B)) {
            unsafeDiffuseRandomly();
          }
        }
      }
      else if(t == LAUNCHER) {
        /**
         * Launchers stage and launch missiles but cannot themselves attack.
         */

        transferSupply(); // sharing is caring.

        if(rc.isWeaponReady()) {

          RobotInfo[] targets = rc.senseNearbyRobots(LAUNCHER.sensorRadiusSquared, otherTeam);
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

        if(rc.isCoreReady()) {
          if(!unsafeSwarm(SIG_RALLY_A)) {
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

            if(rc.isPathable(MISSILE, rc.getLocation().add(facing)) && rc.canMove(facing)) {
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

  private static boolean unsafeSelectTargetAndEngage(MapLocation from, int range, double cutoff) {
    RobotInfo[] targets = rc.senseNearbyRobots(from, range, otherTeam);
    if(targets.length == 0) return false;

    RobotInfo r_info = targets[0]; // select the first target.
    for (int i=1; i < targets.length; i++) { // Loop through all enemy units
      RobotInfo r = targets[i];
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

  private static boolean unsafeBuild(RobotType robot, final int rate) {
    if(rate == RATE_HALT) return false;
    double cutoff = robot.oreCost * (RATE_IMMEDIATE - rate + 1);
    if(rc.getTeamOre() >= cutoff) {
      Direction d = getValidBuildDirection(rc.getLocation(), robot);
      if (d != null) {
        try {
          rc.build(d, robot);
          return true;
        }
        catch(GameActionException e) {
          e.printStackTrace();
        }
      }
    }
    return false;
  }

  private static boolean unsafeSpawn(RobotType robot, final int rate) {
    if(rate == RATE_HALT) return false;
    double cutoff = robot.oreCost * (RATE_IMMEDIATE - rate + 1);
    if(rc.getTeamOre() >= cutoff) {
      Direction d = getValidSpawnDirection(rc.getLocation(), robot);
      if(d != null) {
        try {
          rc.spawn(d, robot);
          return true;
        }
        catch(GameActionException e) {
          e.printStackTrace();
        }
      }
    }
    return false;
  }

  private static void transferSupply() {
    RobotInfo[] targets = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, myTeam);
    if(targets.length == 0) return;
    double transferAmount = 0;
    double mySupply = rc.getSupplyLevel();
    double minSupply = mySupply;
    if(mySupply < SUPPLY_TRANSFER_CUTOFF) return;
    RobotInfo r_info = null;
    for(RobotInfo r : targets) {
      if(r.supplyLevel < minSupply) {
        transferAmount = (mySupply - r.supplyLevel) / 2;
        if(transferAmount >= SUPPLY_TRANSFER_QUANTUM) {
          r_info = r;
          break;
        }
        minSupply = r.supplyLevel;
      }
    }
    if(r_info != null) {
      try {
        rc.transferSupplies((int) transferAmount, r_info.location);
      }
      catch (GameActionException e) {
        e.printStackTrace();
      }
    }
  }

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

  private static boolean chance(double percent) {
    return rand.nextInt(10000) < 10000*percent;
  }

  private static int safeReadBroadcast(int signal) {
    try {
      return rc.readBroadcast(signal);
    }
    catch(GameActionException e) {
      e.printStackTrace();
    }
    return 0;
  }

  private static void safeBroadcast(int signal, int value) {
    try {
      rc.broadcast(signal, value);
    }
    catch(GameActionException e) {
      e.printStackTrace();
    }
  }

  private static void safeBroadcastRallyPoints() {

    MapLocation otherTower = myTowers[0];
    MapLocation remoteTower = myTowers[0];
    MapLocation nearbyTower = myTowers[0];
    double maxDistance = Double.MIN_VALUE;
    double minDistance = Double.MAX_VALUE;

    for(MapLocation tower : myTowers) {
      double distance = tower.distanceSquaredTo(enemyHQ);
      if(distance < maxDistance) {
        nearbyTower = tower;
        maxDistance = distance;
      }
      else if(distance < minDistance) {
        otherTower = remoteTower;
        remoteTower = tower;
        minDistance = distance;
      }
    }

    safeBroadcastMapLocation(SIG_RALLY_A, remoteTower);
    safeBroadcastMapLocation(SIG_RALLY_B, otherTower);
    safeBroadcastMapLocation(SIG_RALLY_C, nearbyTower);
    safeBroadcast(SIG_SWARM, SWARM_RALLY);
  }

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

  private static Direction[] getDirectionsTo(MapLocation loc) {
    if(loc == null) return null;
    Direction d = rc.getLocation().directionTo(loc);
    return new Direction[] {
      d, d.rotateLeft(), d.rotateRight(),
      d.rotateLeft().rotateLeft(),
      d.rotateRight().rotateRight()
    };
  }

  private static void safeBroadcastMapLocation(int signal, MapLocation loc) {

    try {
      rc.broadcast(signal, loc.x);
      rc.broadcast(signal+1, loc.y);
    }
    catch (GameActionException e) {
      e.printStackTrace();
    }
  }

  private static MapLocation safeReadBroadcastMapLocation(int signal) {

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

  private static boolean unsafeSwarm(int signal) {
    int swarm = safeReadBroadcast(SIG_SWARM);
    if(swarm == SWARM_DISABLED) return false;
    if(swarm == SWARM_RALLY) {
      MapLocation rallyPoint = safeReadBroadcastMapLocation(signal);
      if(rallyPoint == null) return false;
      Direction[] ds = getDirectionsTo(rallyPoint);
      for(Direction d : ds) {
        if(rc.canMove(d)) {
          try {
            rc.move(d);
            return true;
          }
          catch (GameActionException e) {
            e.printStackTrace();
          }
        }
      }
    }
    else if(swarm == SWARM_RUSH) {
      Direction[] ds = getDirectionsTo(enemyHQ);
      for(Direction d : ds) {
        if(rc.canMove(d)) {
          try {
            rc.move(d);
            return true;
          }
          catch (GameActionException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return false;
  }
}
