// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi;

import org.opentcs.components.Lifecycle;
import spark.Service;

/**
 * A request handler.
 */
public interface RequestHandler
    extends
      Lifecycle {

  /**
   * Registers the handler's routes with the given service.
   *
   * @param service The service to register the routes with.
   */
  void addRoutes(Service service);
}
