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
  private static Team myTeam;

  /**
   * A static copy of the enemy team.
   */
  private static Team otherTeam;

  /**
   * The location of my towers.
   */
  private static MapLocation[] myTowers;

  /**
   * The location of enemy towers.
   */
  private static MapLocation[] enemyTowers;

  /**
   * The location of my HQ.
   */
  private static MapLocation myHQ;

  /**
   * The location of the enemy HQ.
   */
  private static MapLocation enemyHQ;

  /**
   * A pseudo-random number generator instance.
   */
  private static final Random rand = new Random();

  /**
   * The maximum health an HQ can sense to cause a
   * cutoff in the target-selection routine.
   */
  private static final double HQ_AUTO_ENGAGE_CUTOFF = 24;

  /**
   * The tower's squared-range is constant.
   */
  private static final int TOWER_ATTACK_RANGE = 24;

  /**
   * The maximum health a tower can sense to cause a
   * cutoff in the target-selection routine.
   */
  private static final double TOWER_AUTO_ENGAGE_CUTOFF = 8;

  /**
   * The cost of a beaver.
   */
  private static final int BEAVER_COST = 100;

  /**
   * The minimum amount of ore on a given tile for
   * a beaver to justify trying to mine it.
   */
  private static final double BEAVER_MINE_CUTOFF = 1;

  /**
   * The beaver's squared-range constant.
   */
  private static final int BEAVER_ATTACK_RANGE = 5;

  /**
   * The beaver's cutoff for selecting a suitable target.
   */
  private static final int BEAVER_AUTO_ENGAGE_CUTOFF = 4;

  /**
   * The rate at which beavers decay (per round).
   */
  private static final double BEAVER_DECAY_RATE = 0.01;

  /**
   * The initial beaver seed population
   */
  private static final double BEAVER_SEED_POPULATION = 0;

  /**
   * The bare minimum allowable population of beavers.
   */
  private static final double BEAVER_BASE_POPULATION = 3;

  /**
   * The cost of a `barracks'.
   */
  private static final int BARRACKS_COST = 300;

  /**
   *The barracks decay rate.
   */
  private static final double BARRACKS_DECAY_RATE = 0.01;

  /**
   * The initial barracks population
   */
  private static final double BARRACKS_SEED_POPULATION = -2;

  /**
   * The bare minimum allowable barracks.
   */
  private static final double BARRACKS_BASE_POPULATION = 1;

  /**
   * The cost of a `basher'.
   */
  private static final int BASHER_COST = 80;

  /**
   *The basher decay rate.
   */
  private static final double BASHER_DECAY_RATE = 0.007;

  /**
   * The initial basher population
   */
  private static final double BASHER_SEED_POPULATION = 0;

  /**
   * The bare minimum allowable bashers.
   */
  private static final double BASHER_BASE_POPULATION = 10;

  /**
   * The cost of a `soldier'.
   */
  private static final int SOLDIER_COST = 60;

  /**
   *The soldier decay rate.
   */
  private static final double SOLDIER_DECAY_RATE = 0.001;

  /**
   * The initial soldier population
   */
  private static final double SOLDIER_SEED_POPULATION = 0;

  /**
   * The bare minimum allowable soldiers.
   */
  private static final double SOLDIER_BASE_POPULATION = 10;

  /**
   * The cost of a `miner factory'.
   */
  private static final int MINER_FACTORY_COST = 500;

  /**
   *The miner factory decay rate.
   */
  private static final double MINER_FACTORY_DECAY_RATE = 0.01;

  /**
   * The initial miner factory population
   */
  private static final double MINER_FACTORY_SEED_POPULATION = -1;

  /**
   * The bare minimum allowable miner factories.
   */
  private static final double MINER_FACTORY_BASE_POPULATION = 1;

  /**
   * The cost of a `miner'.
   */
  private static final int MINER_COST = 50;

  /**
   *The barracks decay rate.
   */
  private static final double MINER_DECAY_RATE = 0.005;

  /**
   * The initial miner population
   */
  private static final double MINER_SEED_POPULATION = 0;

  /**
   * The bare minimum allowable miners.
   */
  private static final double MINER_BASE_POPULATION = 5;

  /**
   * The signal channel used to communicate to all beavers.
   */
  private static final int SIGNAL_BEAVER = 0;

  /**
   * A signal flag raised by the HQ to force the building of a barracks.
   */
  private static final int FLAG_FORCE_BARRACKS = 0x1;

  /**
   * Forces a beaver to build a miner factory.
   */
  private static final int FLAG_FORCE_MINER_FACTORY = 0x2;

  /**
   * The signal channel used to communicate to all barracks.
   */
  private static final int SIGNAL_BARRACKS = 1;

  /**
   * A signal flag raised by the HQ to force the spawning of bashers.
   */
  private static final int FLAG_FORCE_BASHER = 0x1;

  /**
   * A signal flag raised by the HQ to force the spawning of soldier.
   */
  private static final int FLAG_FORCE_SOLDIER = 0x2;

  /**
   * The signal channel used to communicate to all miner factories.
   */
  private static final int SIGNAL_MINER_FACTORY = 0;

  /**
   * A signal flag raised by the HQ to force the production of a miner.
   */
  private static final int FLAG_FORCE_MINER = 0x1;

  /**
   * A static copy of the array of directional constants.
   */
  private static final Direction[] directions = Direction.values();

  /**
   * Runs the `RobotPlayer', i.e. all game logic.
   * @param rc the robot controller instance.
   */
  public static void run(RobotController rc) {

    myTeam = rc.getTeam(); // Assign my team instance.
    otherTeam = myTeam.opponent(); // Assign the enemy team instance.
    myTowers = rc.senseTowerLocations(); // Sense my tower locations.
    enemyTowers = rc.senseEnemyTowerLocations(); // Sense enemy towers.
    myHQ = rc.senseHQLocation(); // Sense my headquarters.
    enemyHQ = rc.senseEnemyHQLocation(); // Sense enemy headquarters.


    double beaverPopulation = BEAVER_SEED_POPULATION; // the estimated number of beavers alive.
    double barracksPopulation = BARRACKS_SEED_POPULATION; // the estimated number of barracks.
    double basherPopulation = BASHER_SEED_POPULATION; // the estimated number of bashers alive.
    double soldierPopulation = SOLDIER_SEED_POPULATION; // the estimated number of soldiers alive.
    double minerFactoryPopulation = MINER_FACTORY_SEED_POPULATION; // the estimated number of miner factories.
    double minerPopulation = MINER_SEED_POPULATION; // the estimated number of miners alive.

    /*
     * The method is encapsulated in a forever-loop.
     * It should run for as long as the match running.
     */
    while(true) {

      beaverPopulation -= BEAVER_DECAY_RATE; // decay our beaver population.
      barracksPopulation -= BARRACKS_DECAY_RATE; // decay the barracks population.
      basherPopulation -= BASHER_DECAY_RATE; // decay the basher population.
      soldierPopulation -= SOLDIER_DECAY_RATE; // decay the soldier population.
      minerFactoryPopulation -= MINER_FACTORY_DECAY_RATE; // decay the factories.
      minerPopulation -= MINER_DECAY_RATE; // decay the miner population.

      // Get the type of the active robot.
      RobotType t = rc.getType();

      if(t == HQ) {
        /*
         * The `HQ' can spawn beavers, and attack things, if needed.
         * I also use it as a central message service.
         */

        // If barracks have hit their base population, signal to build another.
        if(barracksPopulation < BARRACKS_BASE_POPULATION) {
          try {
            rc.broadcast(SIGNAL_BEAVER, rc.readBroadcast(SIGNAL_BEAVER) | FLAG_FORCE_BARRACKS);
          }
          catch(GameActionException e) {
            e.printStackTrace();
          }
        }

        // If bashers have hit their base population, signal to build another.
        if(basherPopulation < BASHER_BASE_POPULATION) {
          try {
            rc.broadcast(SIGNAL_BARRACKS, rc.readBroadcast(SIGNAL_BARRACKS) | FLAG_FORCE_BASHER);
          }
          catch(GameActionException e) {
            e.printStackTrace();
          }
        }

        // If soldiers have hit their base population, signal to build another.
        if(soldierPopulation < SOLDIER_BASE_POPULATION) {
          try {
            rc.broadcast(SIGNAL_BARRACKS, rc.readBroadcast(SIGNAL_BARRACKS) | FLAG_FORCE_SOLDIER);
          }
          catch(GameActionException e) {
            e.printStackTrace();
          }
        }

        // If miner factories have hit their base population, signal to build another.
        if(minerFactoryPopulation < MINER_FACTORY_BASE_POPULATION) {
          try {
            rc.broadcast(SIGNAL_BEAVER, rc.readBroadcast(SIGNAL_BEAVER) | FLAG_FORCE_MINER_FACTORY);
          }
          catch(GameActionException e) {
            e.printStackTrace();
          }
        }

        // If miners have hit their base population, signal to build another.
        if(minerPopulation < MINER_BASE_POPULATION) {
          try {
            rc.broadcast(SIGNAL_MINER_FACTORY, rc.readBroadcast(SIGNAL_MINER_FACTORY) | FLAG_FORCE_MINER);
          }
          catch(GameActionException e) {
            e.printStackTrace();
          }
        }

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

              // The turn is over
              rc.yield();
              continue;

            } catch (GameActionException e) {
              e.printStackTrace();
            }
          }
        }

        // If the HQ is ready to act.
        if(rc.isCoreReady()) {

          // Test the beaver population -- could we use more?
          if(beaverPopulation < BEAVER_BASE_POPULATION && rc.getTeamOre() >= BEAVER_COST) {
            Direction d = directions[rand.nextInt(8)]; // pick a random direction.
            if(rc.canSpawn(d, BEAVER)) { // test the spawn validity.
              try {
                rc.spawn(d, BEAVER); // spawn a new beaver.
                beaverPopulation++; // increment the beaver population.
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
         * It has an effective attack range of 24 units^2 (see TOWER_ATTACK_RANGE).
         */

        // Test if the tower's weapon system is online.
        if(rc.isWeaponReady()) {

          // Get an array of nearby robots within this towers attacking range.
          RobotInfo[] targets = rc.senseNearbyRobots(rc.getLocation(), TOWER_ATTACK_RANGE, otherTeam);

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
        /*
         * Bashers walk around and attack adjacent enemies automatically.
         */

        // Test if it's ready to perform an action.
        if(rc.isCoreReady()) {
          // Just walk randomly for now.
          Direction d = directions[rand.nextInt(8)];
          if(rc.canMove(d)) {
            try {
              rc.move(d);
            } catch (GameActionException e) {
              e.printStackTrace();
            }
          }
        }
      }
      else if(t == SOLDIER) {
        // Soldiers walk around and attack things.

        // Test if it's ready to perform an action.
        if(rc.isCoreReady()) {
          // Just walk randomly for now.
          Direction d = directions[rand.nextInt(8)];
          if(rc.canMove(d)) {
            try {
              rc.move(d);
            } catch (GameActionException e) {
              e.printStackTrace();
            }
          }
        }
      }
      else if(t == BEAVER) {
        /*
         * Beavers are the only units capable of building structures.
         * They also have a limited ability to mine, and attack if needed.
         */

        // Test if attacking is viable
        if(rc.isWeaponReady()) {
          RobotInfo[] targets = rc.senseNearbyRobots(rc.getLocation(), BEAVER_ATTACK_RANGE, otherTeam);

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
                  if (r.health <= BEAVER_AUTO_ENGAGE_CUTOFF) {
                    t_info = r;
                    break;
                  }

                  t_info = r; // Update the target info variable.
                }
              }

              rc.attackLocation(t_info.location); // Attack it!

              // The turn is over.
              rc.yield();
              continue;

            } catch (GameActionException e) {
              e.printStackTrace();
            }
          }
        }

        // Test if it's ready to perform an action.
        if(rc.isCoreReady()) {

          MapLocation myLocation = rc.getLocation(); // Get the beaver's map location.

          int signal = 0; // read the general beaver channel for any outstanding messages.
          try {
            signal = rc.readBroadcast(SIGNAL_BEAVER);
          }
          catch (GameActionException e) {
            e.printStackTrace();
          }

          int flag = 0;
          int cost = 0;
          RobotType type = null;

          rc.setIndicatorString(0, "" + ((BARRACKS_BASE_POPULATION-barracksPopulation)) + " > " + (MINER_FACTORY_BASE_POPULATION-minerFactoryPopulation));

          boolean build = false;
          if((BARRACKS_BASE_POPULATION-barracksPopulation) > (MINER_FACTORY_BASE_POPULATION-minerFactoryPopulation)) {
            flag = FLAG_FORCE_BARRACKS;;
            cost = BARRACKS_COST;
            type = BARRACKS;
            build = BARRACKS_BASE_POPULATION-barracksPopulation > 0;
          }
          else {
            flag = FLAG_FORCE_MINER_FACTORY;
            cost = MINER_FACTORY_COST;
            type = MINERFACTORY;
            build = MINER_FACTORY_BASE_POPULATION-minerFactoryPopulation > 0;
          }

          // If barracks are low -- build one.
          if(build && (signal & flag) != 0 && rc.getTeamOre() >= cost) {
            Direction d = directions[rand.nextInt(8)]; // get a random direction.
            if(rc.canBuild(d, type)) { // try building in this direction.
              try {
                rc.build(d, type);
                rc.broadcast(SIGNAL_BEAVER, signal ^ flag);

                if(type == BARRACKS) barracksPopulation++;
                else if(type == MINERFACTORY) minerFactoryPopulation++;

                // The beaver has used up its turn.
                rc.yield();
                continue;
              }
              catch(GameActionException e) {
                e.printStackTrace();
              }
            }
          }

          double oreAmount = rc.senseOre(myLocation); // Get the ore amount at my location.

          // Mine if it is "worth it" to do so.
          if(rand.nextBoolean() && oreAmount >= BEAVER_MINE_CUTOFF) {
            try {
              rc.mine();

              // The beaver has used up its turn.
              rc.yield();
              continue;
            }
            catch(GameActionException e) {
              e.printStackTrace();
            }
          }

          Direction d = directions[rand.nextInt(8)];
          if(rc.canMove(d)) {
            try {
              rc.move(d);

              // The beaver has used up its turn.
              rc.yield();
              continue;
            } catch (GameActionException e) {
              e.printStackTrace();
            }
          }
        }
      }
      else if(t == BARRACKS) {
        /*
         * Spawn bashers and soldiers.
         */

        // Test if it's ready to perform an action.
        if(rc.isCoreReady()) {

          int signal = 0; // read the general barracks channel for any outstanding messages.
          try {
            signal = rc.readBroadcast(SIGNAL_BARRACKS);
          }
          catch (GameActionException e) {
            e.printStackTrace();
          }

          // If bashers are low -- spawn one.
          if((signal & FLAG_FORCE_BASHER) != 0 && rc.getTeamOre() >= BASHER_COST) {
            Direction d = directions[rand.nextInt(8)]; // get a random direction.
            if(rc.canSpawn(d, BASHER)) { // try spawning in this direction.
              try {
                rc.spawn(d, BASHER);
                rc.broadcast(SIGNAL_BARRACKS, signal ^ FLAG_FORCE_BASHER);
                basherPopulation++;

                // The player has used up its turn.
                rc.yield();
                continue;
              }
              catch(GameActionException e) {
                e.printStackTrace();
              }
            }
          }

          // If soldiers are low -- spawn one.
          if((signal & FLAG_FORCE_SOLDIER) != 0 && rc.getTeamOre() >= SOLDIER_COST) {
            Direction d = directions[rand.nextInt(8)]; // get a random direction.
            if(rc.canSpawn(d, SOLDIER)) { // try building in this direction.
              try {
                rc.spawn(d, SOLDIER);
                rc.broadcast(SIGNAL_BARRACKS, signal ^ FLAG_FORCE_SOLDIER);
                soldierPopulation++;

                // The player has used up its turn.
                rc.yield();
                continue;
              }
              catch(GameActionException e) {
                e.printStackTrace();
              }
            }
          }
        }
      }
      else if(t == MINERFACTORY) {
        /*
         * Has the sole responsibility of producing miners.
         */

        // Test if it's ready to perform an action.
        if (rc.isCoreReady()) {

          int signal = 0; // read the general barracks channel for any outstanding messages.
          try {
            signal = rc.readBroadcast(SIGNAL_MINER_FACTORY);
          } catch (GameActionException e) {
            e.printStackTrace();
          }

          // If bashers are low -- spawn one.
          if ((signal & FLAG_FORCE_MINER) != 0 && rc.getTeamOre() >= MINER_COST) {
            Direction d = directions[rand.nextInt(8)]; // get a random direction.
            if (rc.canSpawn(d, MINER)) { // try spawning in this direction.
              try {
                rc.spawn(d, MINER);
                rc.broadcast(SIGNAL_MINER_FACTORY, signal ^ FLAG_FORCE_BASHER);
                minerPopulation++;

                // The player has used up its turn.
                rc.yield();
                continue;
              } catch (GameActionException e) {
                e.printStackTrace();
              }
            }
          }
        }
      }
      else if(t == MINER) {

        // Test if it's ready to perform an action.
        if(rc.isCoreReady()) {

          MapLocation myLocation = rc.getLocation();

          Direction d_max = directions[0];
          double oreMax = rc.senseOre(myLocation.add(d_max));

          // Sense the ore amounts at each location and go to the location.
          for(int i=0; i < 8; i++) {
            double ore = rc.senseOre(myLocation.add(directions[i]));
            if(ore > oreMax) {
              oreMax = ore;
              d_max = directions[i];
            }
          }

          // If I'd rather stay put.
          if(rc.senseOre(myLocation) > oreMax) {

            try {
              rc.mine();
            }
            catch(GameActionException e) {
              e.printStackTrace();
            }

            rc.yield();
            continue;
          }
          else {
            int i = 0;
            for( ; i < 8 && !rc.canMove(d_max); i++) {
              d_max = d_max.rotateLeft();
            }
            if(i < 8) {
              try {
                rc.move(d_max);
              }
              catch (GameActionException e) {
                e.printStackTrace();
              }
            }

          }
        }
      }

      // finish this bot's turn early and save the extra byte code.
      rc.yield();
    }
  }
}
