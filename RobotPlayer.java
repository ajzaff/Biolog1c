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

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import team309.sentry.HQ;
import team309.sentry.Tower;
import team309.structures.*;
import team309.units.*;

/**
 * Provides a method `run' which creates a new robot instance and runs it.
 */
public class RobotPlayer {

  /**
   * The enemy HQ location.
   */
  public static MapLocation myHQ;
  /**
   * The enemy HQ location.
   */
  public static MapLocation enemyHQ;

  /**
   * The locations of enemy towers.
   */
  public static MapLocation[] enemyTowers;
  /**
   * The locations of my towers.
   */
  public static MapLocation[] ourTowers;

  /**
   * The robot controller instance.
   */
  public static RobotController rc;

  /**
   * Runs the `RobotPlayer', i.e. all game logic.
   * @param rc the robot controller instance.
   */
  public static void run(RobotController rc) {

    RobotType t = rc.getType();
    RobotPlayer.rc = rc;
    myHQ = rc.senseHQLocation();
    enemyHQ = rc.senseEnemyHQLocation();
    enemyTowers = rc.senseEnemyTowerLocations();
    ourTowers = rc.senseTowerLocations();

    if(t == RobotType.HQ) new HQ().run();
    else if(t == RobotType.TOWER) new Tower().run();
    else if(t == RobotType.BASHER) new Basher().run();
    else if(t == RobotType.SOLDIER) new Soldier().run();
    else if(t == RobotType.BEAVER) new Beaver().run();
    else if(t == RobotType.BARRACKS) new Barracks().run();
    else if(t == RobotType.MINERFACTORY) new MinerFactory().run();
    else if(t == RobotType.MINER) new Miner().run();
    else if(t == RobotType.HELIPAD) new Helipad().run();
    else if(t == RobotType.AEROSPACELAB) new AerospaceLab().run();
    else if(t == RobotType.TANKFACTORY) new TankFactory().run();
    else if(t == RobotType.TANK) new Tank().run();
    else if(t == RobotType.DRONE) new Drone().run();
    else if(t == RobotType.LAUNCHER) new Launcher().run();
    else if(t == RobotType.MISSILE) new Missile().run();
    else if(t == RobotType.TECHNOLOGYINSTITUTE) new TechnologyInstitute().run();
    else if(t == RobotType.TRAININGFIELD) new TrainingField().run();
    else if(t == RobotType.COMMANDER) new Commander().run();
    else if(t == RobotType.HANDWASHSTATION) new HandwashStation().run();
  }
}
