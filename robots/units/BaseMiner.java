package team309.robots.units;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import team309.Navigation;

import static team309.RobotPlayer.rc;

public abstract class BaseMiner extends BaseUnit {

  private final double mineThreshold;

  /**
   * Builds a new miner with the given threshold.
   * @param mineThreshold minimum amount of ore to mine.
   */
  public BaseMiner(double mineThreshold) {
    this.mineThreshold = mineThreshold;
  }

  /**
   * Gets the mine threshold.
   * @return the mine threshold.
   */
  public final double getMineThreshold() {
    return mineThreshold;
  }

  /**
   * Senses the best ore within the given radius, divided by distance squared.
   * @param rangeSquared a valid range within the bots sensor range.
   * @return the map location of the best ore yield, possibly the current location.
   */
  public MapLocation senseBestOreLocation(int rangeSquared) {
    MapLocation[] ls = MapLocation.getAllMapLocationsWithinRadiusSq(myLocation, rangeSquared);
    MapLocation bestOreLocation = myLocation;
    double bestOreValue = rc.senseOre(myLocation);
    double minDistance = Double.MAX_VALUE;

    // Find the location.
    for(MapLocation loc : ls) {
      double oreValue = rc.senseOre(loc);
      double distance = loc.distanceSquaredTo(myLocation);
      boolean closer = distance < minDistance;
      if((oreValue > bestOreValue || closer) && rc.isPathable(RobotType.MINER, loc)) {
        if(closer) {
          minDistance = distance;
        }
        bestOreValue = oreValue;
        bestOreLocation = loc;
      }
    }

    return bestOreLocation;
  }

  /**
   * Checks the core delay and mines, if under threshold.
   *  !! Requires updated `myLocation' variable.
   */
  public boolean thresholdMine() throws GameActionException {
    if(rc.isCoreReady() && rc.canMine() && rc.senseOre(myLocation) >= getMineThreshold()) {
      rc.mine();
      return true;
    }
    return false;
  }

  /**
   * Checks the core delay and mines.
   * Mines even if yield is less than threshold.
   */
  public boolean mine() throws GameActionException {
    if(rc.isCoreReady() && rc.canMine()) {
      rc.mine();
      return true;
    }
    return false;
  }
}
