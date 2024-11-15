// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase.recharging;

import jakarta.annotation.Nonnull;
import java.util.List;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;

/**
 * A strategy for finding locations suitable for recharging vehicles.
 */
public interface RechargePositionSupplier
    extends
      Lifecycle {

  /**
   * Returns a sequence of destinations for recharging the given vehicle.
   * In most cases, the sequence will probably consist of only one destination. Some vehicles may
   * require to visit more than one destination for recharging, though, e.g. to drop off a battery
   * pack at one location and get a fresh one at another location.
   *
   * @param vehicle The vehicle to be recharged.
   * @return A sequence of destinations including operations for recharging the given vehicle. If
   * no suitable sequence was found, the returned sequence will be empty.
   */
  @Nonnull
  List<DriveOrder.Destination> findRechargeSequence(
      @Nonnull
      Vehicle vehicle
  );
}
