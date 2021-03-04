/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing;

import com.google.common.collect.HashBasedTable;
import java.util.LinkedList;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.StaticRoute;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds routing tables using a depth-first-search implementation.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RoutingTableBuilderDfs
    extends RoutingTableBuilderAbstract
    implements RoutingTableBuilder {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RoutingTableBuilderDfs.class);
  /**
   * Whether to terminate early.
   */
  private final boolean terminateEarly;
  /**
   * The maximum search depth.
   */
  private final int maxDepth;

  /**
   * Creates a new instance.
   *
   * @param kernel The kernel providing the model data.
   * @param routeEvaluator The evaluator to be used to compute costs for routes.
   * @param configuration This class's configuration.
   */
  @Inject
  RoutingTableBuilderDfs(LocalKernel kernel,
                         RouteEvaluator routeEvaluator,
                         DefaultRouterConfiguration configuration) {
    super(kernel, routeEvaluator);
    requireNonNull(configuration, "configuration");
    this.terminateEarly = configuration.terminateSearchEarly();
    this.maxDepth = configuration.dfsMaxDepth();
  }

  @Override
  public RoutingTable computeTable(Vehicle vehicle) {
    this.vehicle = requireNonNull(vehicle, "vehicle");

    routingTable = HashBasedTable.create();
    long timeStampBefore = System.currentTimeMillis();
    for (Point curPoint : kernel.getTCSObjectsOriginal(Point.class)) {
      updateTableEntry(curPoint.getReference(),
                       curPoint.getReference(),
                       new LinkedList<>(),
                       0);
      descendSuccessors(curPoint, curPoint, new LinkedList<>());
    }
    double timePassed = (System.currentTimeMillis() - timeStampBefore) / 1000.0;
    LOG.debug("Computed routing table for {} in {} seconds.", vehicle.getName(), timePassed);
    for (StaticRoute staticRoute
             : kernel.getTCSObjectsOriginal(StaticRoute.class)) {
      integrateStaticRoute(staticRoute);
    }
    return new RoutingTable(routingTable);
  }

  /**
   *
   * @param startPoint The point we started from.
   * @param curPoint The point we're currently looking at.
   * @param steps The steps we travelled to get here.
   */
  private void computeTableEntries(Point startPoint,
                                   Point curPoint,
                                   LinkedList<Route.Step> steps) {
    requireNonNull(startPoint, "startPoint");
    requireNonNull(curPoint, "curPoint");
    requireNonNull(steps, "steps");

    long costs = routeEvaluator.computeCosts(vehicle, startPoint, steps);
    RoutingTable.Entry tableEntry
        = routingTable.get(startPoint.getReference(),
                           curPoint.getReference());
    // If we found a better route than any known one, update the table entry.
    if (tableEntry == null || costs < tableEntry.getCosts()) {
      updateTableEntry(startPoint.getReference(),
                       curPoint.getReference(),
                       new LinkedList<>(steps),
                       costs);
    }
    // If the route found is not better than an existing one and we should
    // terminate early, do so.
    // (Not knowing the cost function applied to the route, terminating here
    // might mean that a shorter route to one of the successors will not be
    // found. An exhaustive search might take much longer, however.)
    else if (terminateEarly) {
      return;
    }
    // If we have reached the maximum search depth, terminate the recursion.
    if (steps.size() > maxDepth) {
      return;
    }
    descendSuccessors(startPoint, curPoint, steps);
  }

  private void descendSuccessors(Point startPoint,
                                 Point curPoint,
                                 LinkedList<Route.Step> steps) {
    descendSuccessorsForward(startPoint, curPoint, steps);
    descendSuccessorsBackwards(startPoint, curPoint, steps);
  }

  private void descendSuccessorsForward(Point startPoint,
                                        Point curPoint,
                                        LinkedList<Route.Step> steps) {
    // Check all outgoing paths for more points to visit.
    for (TCSObjectReference<Path> outPathRef : curPoint.getOutgoingPaths()) {
      Path outPath = kernel.getTCSObjectOriginal(Path.class, outPathRef);
      Point nextPoint = kernel.getTCSObjectOriginal(Point.class,
                                                    outPath.getDestinationPoint());
      if (!visitedPointOnRoute(nextPoint, steps)
          && outPath.isNavigableForward()) {
        steps.addLast(
            new Route.Step(outPath,
                           kernel.getTCSObjectOriginal(Point.class, outPath.getSourcePoint()),
                           nextPoint,
                           Vehicle.Orientation.FORWARD,
                           steps.size()));
        computeTableEntries(startPoint, nextPoint, steps);
        steps.removeLast();
      }
    }
  }

  private void descendSuccessorsBackwards(Point startPoint,
                                          Point curPoint,
                                          LinkedList<Route.Step> steps) {
    // Check all incoming paths for more points to visit.
    for (TCSObjectReference<Path> inPathRef : curPoint.getIncomingPaths()) {
      Path inPath = kernel.getTCSObjectOriginal(Path.class, inPathRef);
      Point nextPoint = kernel.getTCSObjectOriginal(Point.class,
                                                    inPath.getSourcePoint());
      if (!visitedPointOnRoute(nextPoint, steps)
          && inPath.isNavigableReverse()) {
        steps.addLast(
            new Route.Step(inPath,
                           kernel.getTCSObjectOriginal(Point.class, inPath.getDestinationPoint()),
                           nextPoint,
                           Vehicle.Orientation.BACKWARD,
                           steps.size()));
        computeTableEntries(startPoint, nextPoint, steps);
        steps.removeLast();
      }
    }
  }
}