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

public final class Swarm {
  /**
   * Rally around our HQ.
   */
  static final int SWARM_HOLD_OUR_HQ = 0x1;
  /**
   * Rally around our most vulnerable tower.
   */
  static final int SWARM_HOLD_OUR_VULNERABLE_TOWERS = 0x2;
  /**
   * Rally around their most vulnerable tower.
   */
  static final int SWARM_RALLY_THEIR_VULNERABLE_TOWERS = 0x4;
  /**
   * Rally around their HQ.
   */
  static final int SWARM_RALLY_THEIR_HQ = 0x8;
  /**
   * Engage their most vulnerable tower.
   */
  static final int SWARM_ENGAGE_THEIR_VULNERABLE_TOWERS = 0x10;
  /**
   * Engage their HQ.
   */
  static final int SWARM_ENGAGE_THEIR_HQ = 0x20;
}
