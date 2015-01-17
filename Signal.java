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

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

import static team309.RobotPlayer.rc;

public final class Signal {

  /**
   * The beaver spawn rate signal channel.
   */
  public static final int SIG_BEAVER_RATE = 0;

  /**
   * The barracks build rate signal channel.
   */
  public static final int SIG_BARRACKS_RATE = 1;

  /**
   * The factory rate signal channel.
   */
  public static final int SIG_MINER_FACTORY_RATE = 2;

  /**
   * The depot rate signal channel.
   */
  public static final int SIG_DEPOT_RATE = 3;

  /**
   * The soldier rate signal channel.
   */
  public static final int SIG_SOLDIER_RATE = 4;

  /**
   * The soldier rate signal channel.
   */
  public static final int SIG_BASHER_RATE = 5;

  /**
   * The miner rate signal channel.
   */
  public static final int SIG_MINER_RATE = 6;

  /**
   * The helipad rate signal channel.
   */
  public static final int SIG_HELIPAD_RATE = 7;

  /**
   * The drone rate signalling channel.
   */
  public static final int SIG_DRONE_RATE = 8;

  /**
   * The aerospace lab rate signalling channel.
   */
  public static final int SIG_AERO_LAB_RATE = 9;

  /**
   * The launcher rate signalling channel.
   */
  public static final int SIG_LAUNCHER_RATE = 10;

  /**
   * The launcher rate signalling channel.
   */
  public static final int SIG_HANDWASH_RATE = 11;

  /**
   * The tank factory rate signalling channel.
   */
  public static final int SIG_TANK_FACTORY_RATE = 12;

  /**
   * The tank rate signalling channel.
   */
  public static final int SIG_TANK_RATE = 13;

  /**
   * Broadcasts a map location to the given `signal' channel index.
   * Using the current system, map locations are broadcast pairwise
   * And at adjacent indexes. (So the x-component is at `signal' and
   * The y-component is at `signal'+1).
   *  !! This method is SAFE to use without any checks. !!
   * @param signal a valid signal channel for this map location.
   * @param loc a valid map location.
   */
  public static void broadcastMapLocation(final int signal, MapLocation loc) throws GameActionException {
    rc.broadcast(signal, loc.x);
    rc.broadcast(signal+1, loc.y);
  }

  /**
   * Sets the `flag' on the given `signal' channel.
   *  !! This method is SAFE to use without any checks. !!
   * @param signal a valid signal channel.
   * @param flag a valid integer flag.
   */
  public void broadcastSetFlag(final int signal, final int flag) throws GameActionException {
    int value = rc.readBroadcast(signal);
    rc.broadcast(signal, value | flag);
  }

  /**
   * Unsets the `flag' on the given `signal' channel.
   *  !! This method is SAFE to use without any checks. !!
   * @param signal a valid signal channel.
   * @param flag a valid integer flag.
   */
  public static void broadcastUnsetFlag(final int signal, final int flag) throws GameActionException {
    int value = rc.readBroadcast(signal);
    rc.broadcast(signal, value & ~flag);
  }

  /**
   * Update the value of a signal, if value has changed.
   * @param signal a valid signalling channel.
   * @param newValue the new value.
   * @return `true' if the value was updated; `false' otherwise.
   * @throws GameActionException
   */
  public static boolean broadcastUpdate(final int signal, int newValue) throws GameActionException {
    int value = rc.readBroadcast(signal);
    if(value != newValue) {
      rc.broadcast(signal, newValue);
      return true;
    }
    return false;
  }
}
