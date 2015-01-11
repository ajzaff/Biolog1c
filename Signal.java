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

public class Signal {

  /**
   * Reads a broadcast from the given `signal' channel.
   *  !! This method is SAFE to use without checks. !!
   * @param signal a valid signalling channel.
   * @return the integer value read; or `0'.
   */
  static int safeReadBroadcast(final int signal) {
    try {
      return RobotPlayer.rc.readBroadcast(signal);
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
  static void safeBroadcast(final int signal, int value) {
    try {
      RobotPlayer.rc.broadcast(signal, value);
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
      RobotPlayer.rc.broadcast(signal, loc.x);
      RobotPlayer.rc.broadcast(signal+1, loc.y);
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
  static void safeBroadcastSetFlag(final int signal, final int flag) {
    int value = 0;
    try {
      value = RobotPlayer.rc.readBroadcast(signal);
      RobotPlayer.rc.broadcast(signal, value | flag);
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
      value = RobotPlayer.rc.readBroadcast(signal);
      RobotPlayer.rc.broadcast(signal, value & ~flag);
    }
    catch (GameActionException e) {
      e.printStackTrace();
    }
  }
}
