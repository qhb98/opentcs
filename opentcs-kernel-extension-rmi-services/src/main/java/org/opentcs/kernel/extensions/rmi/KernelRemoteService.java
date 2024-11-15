// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.rmi;

import java.util.concurrent.ExecutionException;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.Lifecycle;

/**
 * A base class for kernel-side implementations of remote services.
 */
public abstract class KernelRemoteService
    implements
      Lifecycle {

  /**
   * The message to log when a service method execution failed.
   */
  private static final String EXECUTION_FAILED_MESSAGE = "Failed to execute service method";

  /**
   * Wraps the given exception into a suitable {@link RuntimeException}.
   *
   * @param exc The exception to find a runtime exception for.
   * @return The runtime exception.
   */
  protected RuntimeException findSuitableExceptionFor(Exception exc) {
    if (exc instanceof InterruptedException) {
      return new IllegalStateException("Unexpectedly interrupted");
    }
    if (exc instanceof ExecutionException
        && exc.getCause() instanceof RuntimeException) {
      return (RuntimeException) exc.getCause();
    }
    return new KernelRuntimeException(EXECUTION_FAILED_MESSAGE, exc.getCause());
  }
}
