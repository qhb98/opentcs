// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v6;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.access.to.model.BoundingBoxCreationTO;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.visualization.Layer;
import org.opentcs.data.model.visualization.LayerGroup;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.util.Colors;

/**
 * Provides methods for mapping {@link PlantModelCreationTO} to {@link V6PlantModelTO} and
 * vice versa.
 */
public class V6TOMapper {

  /**
   * Creates a new instance.
   */
  public V6TOMapper() {
  }

  /**
   * Maps the given model to a {@link PlantModelCreationTO} instance.
   *
   * @param model The model to map.
   * @return The mapped {@link PlantModelCreationTO} instance.
   */
  public PlantModelCreationTO map(V6PlantModelTO model) {
    return new PlantModelCreationTO(model.getName())
        .withPoints(toPointCreationTO(model.getPoints()))
        .withVehicles(toVehicleCreationTO(model.getVehicles()))
        .withPaths(toPathCreationTO(model.getPaths()))
        .withLocationTypes(toLocationTypeCreationTO(model.getLocationTypes()))
        .withLocations(toLocationCreationTO(model.getLocations()))
        .withBlocks(toBlockCreationTO(model.getBlocks()))
        .withVisualLayout(toVisualLayoutCreationTO(model.getVisualLayout()))
        .withProperties(convertProperties(model.getProperties()));
  }

  /**
   * Maps the given model to a {@link V6PlantModelTO} instance.
   *
   * @param model The model to map.
   * @return The mapped {@link V6PlantModelTO} instance.
   */
  public V6PlantModelTO map(PlantModelCreationTO model) {
    V6PlantModelTO result = new V6PlantModelTO();

    result.setName(model.getName());
    result.setVersion(V6PlantModelTO.VERSION_STRING);
    result.getPoints().addAll(toPointTO(model.getPoints(), model.getPaths()));
    result.getVehicles().addAll(toVehicleTO(model.getVehicles()));
    result.getPaths().addAll(toPathTO(model.getPaths()));
    result.getLocationTypes().addAll(toLocationTypeTO(model.getLocationTypes()));
    result.getLocations().addAll(toLocationTO(model.getLocations()));
    result.getBlocks().addAll(toBlockTO(model.getBlocks()));
    result.setVisualLayout(toVisualLayoutTO(model.getVisualLayout()));
    result.getProperties().addAll(convertProperties(model.getProperties()));

    return result;
  }

  //Methods for mapping from PlantModelElementTO to CreationTO start here.
  private List<PointCreationTO> toPointCreationTO(List<PointTO> points) {
    List<PointCreationTO> result = new ArrayList<>();

    for (PointTO point : points) {
      result.add(
          new PointCreationTO(point.getName())
              .withPose(
                  new Pose(
                      new Triple(point.getPositionX(), point.getPositionY(), point.getPositionZ()),
                      point.getVehicleOrientationAngle().doubleValue()
                  )
              )
              .withType(Point.Type.valueOf(point.getType()))
              .withVehicleEnvelopes(toEnvelopeMap(point.getVehicleEnvelopes()))
              .withMaxVehicleBoundingBox(toBoundingBoxCreationTO(point.getMaxVehicleBoundingBox()))
              .withProperties(convertProperties(point.getProperties()))
              .withLayout(
                  new PointCreationTO.Layout(
                      new Couple(
                          point.getPointLayout().getPositionX(),
                          point.getPointLayout().getPositionY()
                      ),
                      new Couple(
                          point.getPointLayout().getLabelOffsetX(),
                          point.getPointLayout().getLabelOffsetY()
                      ),
                      point.getPointLayout().getLayerId()
                  )
              )
      );
    }

    return result;
  }

  private List<VehicleCreationTO> toVehicleCreationTO(List<VehicleTO> vehicles) {
    List<VehicleCreationTO> result = new ArrayList<>();

    for (VehicleTO vehicle : vehicles) {
      result.add(
          new VehicleCreationTO(vehicle.getName())
              .withBoundingBox(toBoundingBoxCreationTO(vehicle.getBoundingBox()))
              .withEnergyLevelThresholdSet(toEnergyLevelThresholdSetCreationTO(vehicle))
              .withMaxReverseVelocity(vehicle.getMaxReverseVelocity())
              .withMaxVelocity(vehicle.getMaxVelocity())
              .withEnvelopeKey(vehicle.getEnvelopeKey())
              .withProperties(convertProperties(vehicle.getProperties()))
              .withLayout(
                  new VehicleCreationTO.Layout(
                      Colors.decodeFromHexRGB(vehicle.getVehicleLayout().getColor())
                  )
              )
      );
    }

    return result;
  }

  private List<PathCreationTO> toPathCreationTO(List<PathTO> paths) {
    List<PathCreationTO> result = new ArrayList<>();

    for (PathTO path : paths) {
      result.add(
          new PathCreationTO(
              path.getName(),
              path.getSourcePoint(),
              path.getDestinationPoint()
          )
              .withLength(path.getLength())
              .withLocked(path.isLocked())
              .withMaxVelocity(path.getMaxVelocity().intValue())
              .withMaxReverseVelocity(path.getMaxReverseVelocity().intValue())
              .withPeripheralOperations(
                  toPeripheralOperationCreationTOs(path.getPeripheralOperations())
              )
              .withVehicleEnvelopes(toEnvelopeMap(path.getVehicleEnvelopes()))
              .withProperties(convertProperties(path.getProperties()))
              .withLayout(
                  new PathCreationTO.Layout(
                      Path.Layout.ConnectionType.valueOf(path.getPathLayout().getConnectionType()),
                      path.getPathLayout().getControlPoints().stream()
                          .map(controlPoint -> new Couple(controlPoint.getX(), controlPoint.getY()))
                          .toList(),
                      path.getPathLayout().getLayerId()
                  )
              )
      );
    }

    return result;
  }

  private List<PeripheralOperationCreationTO> toPeripheralOperationCreationTOs(
      List<PeripheralOperationTO> tos
  ) {
    return tos.stream()
        .map(
            to -> new PeripheralOperationCreationTO(to.getName(), to.getLocationName())
                .withExecutionTrigger(
                    PeripheralOperation.ExecutionTrigger.valueOf(to.getExecutionTrigger())
                )
                .withCompletionRequired(to.isCompletionRequired())
        )
        .toList();
  }

  private List<LocationTypeCreationTO> toLocationTypeCreationTO(
      List<LocationTypeTO> locationTypes
  ) {
    List<LocationTypeCreationTO> result = new ArrayList<>();

    for (LocationTypeTO locationType : locationTypes) {
      result.add(
          new LocationTypeCreationTO(locationType.getName())
              .withAllowedOperations(getOperationNames(locationType.getAllowedOperations()))
              .withAllowedPeripheralOperations(
                  getPeripheralOperationNames(
                      locationType.getAllowedPeripheralOperations()
                  )
              )
              .withProperties(convertProperties(locationType.getProperties()))
              .withLayout(
                  new LocationTypeCreationTO.Layout(
                      LocationRepresentation.valueOf(
                          locationType.getLocationTypeLayout().getLocationRepresentation()
                      )
                  )
              )
      );
    }

    return result;
  }

  private List<LocationCreationTO> toLocationCreationTO(List<LocationTO> locations) {
    List<LocationCreationTO> result = new ArrayList<>();

    for (LocationTO location : locations) {
      result.add(
          new LocationCreationTO(
              location.getName(),
              location.getType(),
              new Triple(
                  location.getPositionX(),
                  location.getPositionY(),
                  location.getPositionZ()
              )
          )
              .withLinks(getLinks(location))
              .withLocked(location.isLocked())
              .withProperties(convertProperties(location.getProperties()))
              .withLayout(
                  new LocationCreationTO.Layout(
                      new Couple(
                          location.getLocationLayout().getPositionX(),
                          location.getLocationLayout().getPositionY()
                      ),
                      new Couple(
                          location.getLocationLayout().getLabelOffsetX(),
                          location.getLocationLayout().getLabelOffsetY()
                      ),
                      LocationRepresentation.valueOf(
                          location.getLocationLayout().getLocationRepresentation()
                      ),
                      location.getLocationLayout().getLayerId()
                  )
              )
      );
    }

    return result;
  }

  private List<BlockCreationTO> toBlockCreationTO(List<BlockTO> blocks) {
    List<BlockCreationTO> result = new ArrayList<>();

    for (BlockTO block : blocks) {
      result.add(
          new BlockCreationTO(block.getName())
              .withType(Block.Type.valueOf(block.getType()))
              .withMemberNames(
                  block.getMembers().stream()
                      .map(member -> member.getName())
                      .collect(Collectors.toSet())
              )
              .withProperties(convertProperties(block.getProperties()))
              .withLayout(
                  new BlockCreationTO.Layout(
                      Colors.decodeFromHexRGB(block.getBlockLayout().getColor())
                  )
              )
      );
    }

    return result;
  }

  private VisualLayoutCreationTO toVisualLayoutCreationTO(VisualLayoutTO visualLayout) {
    return new VisualLayoutCreationTO(visualLayout.getName())
        .withScaleX(visualLayout.getScaleX())
        .withScaleY(visualLayout.getScaleY())
        .withLayers(convertLayers(visualLayout.getLayers()))
        .withLayerGroups(convertLayerGroups(visualLayout.getLayerGroups()))
        .withProperties(convertProperties(visualLayout.getProperties()));
  }

  private List<Layer> convertLayers(List<VisualLayoutTO.Layer> layers) {
    List<Layer> result = new ArrayList<>();

    for (VisualLayoutTO.Layer layer : layers) {
      result.add(
          new Layer(
              layer.getId(),
              layer.getOrdinal(),
              layer.isVisible(),
              layer.getName(),
              layer.getGroupId()
          )
      );
    }

    return result;
  }

  private List<LayerGroup> convertLayerGroups(List<VisualLayoutTO.LayerGroup> layerGroups) {
    List<LayerGroup> result = new ArrayList<>();

    for (VisualLayoutTO.LayerGroup layerGroup : layerGroups) {
      result.add(
          new LayerGroup(
              layerGroup.getId(),
              layerGroup.getName(),
              layerGroup.isVisible()
          )
      );
    }

    return result;
  }

  private Map<String, String> convertProperties(List<PropertyTO> propsList) {
    Map<String, String> result = new HashMap<>();
    for (PropertyTO property : propsList) {
      String propName
          = isNullOrEmpty(property.getName()) ? "Property unknown" : property.getName();
      String propValue
          = isNullOrEmpty(property.getValue()) ? "Value unknown" : property.getValue();

      result.put(propName, propValue);
    }

    return result;
  }

  private List<String> getOperationNames(List<AllowedOperationTO> ops) {
    List<String> result = new ArrayList<>(ops.size());
    for (AllowedOperationTO operation : ops) {
      result.add(operation.getName());
    }
    return result;
  }

  private List<String> getPeripheralOperationNames(List<AllowedPeripheralOperationTO> ops) {
    List<String> result = new ArrayList<>(ops.size());
    for (AllowedPeripheralOperationTO operation : ops) {
      result.add(operation.getName());
    }
    return result;
  }

  private Map<String, Set<String>> getLinks(LocationTO to) {
    Map<String, Set<String>> result = new HashMap<>();
    for (LocationTO.Link linkTO : to.getLinks()) {
      result.put(
          linkTO.getPoint(),
          new HashSet<>(getOperationNames(linkTO.getAllowedOperations()))
      );
    }

    return result;
  }

  //Methods for mapping from CreationTO to PlantModelElementTO start here.
  private List<PointTO> toPointTO(List<PointCreationTO> points, List<PathCreationTO> paths) {
    List<PointTO> result = new ArrayList<>();

    for (PointCreationTO point : points) {
      PointTO pointTO = new PointTO();
      pointTO.setName(point.getName());
      pointTO.setPositionX(point.getPose().getPosition().getX())
          .setPositionY(point.getPose().getPosition().getY())
          .setVehicleOrientationAngle((float) point.getPose().getOrientationAngle())
          .setType(point.getType().name())
          .setOutgoingPaths(getOutgoingPaths(point, paths))
          .setVehicleEnvelopes(toVehicleEnvelopeTOs(point.getVehicleEnvelopes()))
          .setMaxVehicleBoundingBox(toBoundingBoxTO(point.getMaxVehicleBoundingBox()))
          .setPointLayout(
              new PointTO.PointLayout()
                  .setPositionX(point.getLayout().getPosition().getX())
                  .setPositionY(point.getLayout().getPosition().getY())
                  .setLabelOffsetX(point.getLayout().getLabelOffset().getX())
                  .setLabelOffsetY(point.getLayout().getLabelOffset().getY())
                  .setLayerId(point.getLayout().getLayerId())
          )
          .setProperties(convertProperties(point.getProperties()));

      result.add(pointTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private List<VehicleTO> toVehicleTO(List<VehicleCreationTO> vehicles) {
    List<VehicleTO> result = new ArrayList<>();

    for (VehicleCreationTO vehicle : vehicles) {
      VehicleTO vehicleTO = new VehicleTO();
      vehicleTO.setName(vehicle.getName());
      vehicleTO.setBoundingBox(toBoundingBoxTO(vehicle.getBoundingBox()))
          .setMaxVelocity(vehicle.getMaxVelocity())
          .setMaxReverseVelocity(vehicle.getMaxReverseVelocity())
          .setEnergyLevelGood((long) vehicle.getEnergyLevelThresholdSet().getEnergyLevelGood())
          .setEnergyLevelCritical(
              (long) vehicle.getEnergyLevelThresholdSet().getEnergyLevelCritical()
          )
          .setEnergyLevelFullyRecharged(
              (long) vehicle.getEnergyLevelThresholdSet().getEnergyLevelFullyRecharged()
          )
          .setEnergyLevelSufficientlyRecharged(
              (long) vehicle.getEnergyLevelThresholdSet().getEnergyLevelSufficientlyRecharged()
          )
          .setEnvelopeKey(vehicle.getEnvelopeKey())
          .setVehicleLayout(
              new VehicleTO.VehicleLayout()
                  .setColor(Colors.encodeToHexRGB(vehicle.getLayout().getRouteColor()))
          )
          .setProperties(convertProperties(vehicle.getProperties()));

      result.add(vehicleTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private List<PathTO> toPathTO(List<PathCreationTO> paths) {
    List<PathTO> result = new ArrayList<>();

    for (PathCreationTO path : paths) {
      PathTO pathTO = new PathTO();
      pathTO.setName(path.getName());
      pathTO.setSourcePoint(path.getSrcPointName())
          .setDestinationPoint(path.getDestPointName())
          .setLength(path.getLength())
          .setMaxVelocity((long) path.getMaxVelocity())
          .setMaxReverseVelocity((long) path.getMaxReverseVelocity())
          .setPeripheralOperations(toPeripheralOperationTOs(path.getPeripheralOperations()))
          .setLocked(path.isLocked())
          .setVehicleEnvelopes(toVehicleEnvelopeTOs(path.getVehicleEnvelopes()))
          .setPathLayout(
              new PathTO.PathLayout()
                  .setConnectionType(path.getLayout().getConnectionType().name())
                  .setControlPoints(
                      path.getLayout().getControlPoints().stream()
                          .map(controlPoint -> {
                            return new PathTO.ControlPoint()
                                .setX(controlPoint.getX())
                                .setY(controlPoint.getY());
                          })
                          .toList()
                  )
                  .setLayerId(path.getLayout().getLayerId())
          )
          .setProperties(convertProperties(path.getProperties()));

      result.add(pathTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private List<PeripheralOperationTO> toPeripheralOperationTOs(
      List<PeripheralOperationCreationTO> tos
  ) {
    return tos.stream()
        .map(
            to -> (PeripheralOperationTO) new PeripheralOperationTO()
                .setLocationName(to.getLocationName())
                .setExecutionTrigger(to.getExecutionTrigger().name())
                .setCompletionRequired(to.isCompletionRequired())
                .setName(to.getOperation())
        )
        .toList();
  }

  private List<LocationTypeTO> toLocationTypeTO(
      List<LocationTypeCreationTO> locationTypes
  ) {
    List<LocationTypeTO> result = new ArrayList<>();

    for (LocationTypeCreationTO locationType : locationTypes) {
      LocationTypeTO locationTypeTO = new LocationTypeTO();
      locationTypeTO.setName(locationType.getName());
      locationTypeTO.setAllowedOperations(
          toAllowedOperationTOs(locationType.getAllowedOperations())
      )
          .setAllowedPeripheralOperations(
              toAllowedPeripheralOperationTOs(locationType.getAllowedPeripheralOperations())
          )
          .setLocationTypeLayout(
              new LocationTypeTO.LocationTypeLayout()
                  .setLocationRepresentation(
                      locationType.getLayout().getLocationRepresentation().name()
                  )
          )
          .setProperties(convertProperties(locationType.getProperties()));

      result.add(locationTypeTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private List<LocationTO> toLocationTO(List<LocationCreationTO> locations) {
    List<LocationTO> result = new ArrayList<>();

    for (LocationCreationTO location : locations) {
      LocationTO locationTO = new LocationTO();
      locationTO.setName(location.getName());
      locationTO.setPositionX(location.getPosition().getX())
          .setPositionY(location.getPosition().getY())
          .setType(location.getTypeName())
          .setLinks(toLocationTOLinks(location.getLinks()))
          .setLocked(location.isLocked())
          .setLocationLayout(
              new LocationTO.LocationLayout()
                  .setPositionX(location.getLayout().getPosition().getX())
                  .setPositionY(location.getLayout().getPosition().getY())
                  .setLabelOffsetX(location.getLayout().getLabelOffset().getX())
                  .setLabelOffsetY(location.getLayout().getLabelOffset().getY())
                  .setLocationRepresentation(
                      location.getLayout().getLocationRepresentation().name()
                  )
                  .setLayerId(location.getLayout().getLayerId())
          )
          .setProperties(convertProperties(location.getProperties()));

      result.add(locationTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private List<BlockTO> toBlockTO(List<BlockCreationTO> blocks) {
    List<BlockTO> result = new ArrayList<>();

    for (BlockCreationTO block : blocks) {
      BlockTO blockTO = new BlockTO();
      blockTO.setName(block.getName());
      blockTO.setType(block.getType().name())
          .setMembers(toMemberTOs(block.getMemberNames()))
          .setBlockLayout(
              new BlockTO.BlockLayout()
                  .setColor(Colors.encodeToHexRGB(block.getLayout().getColor()))
          )
          .setProperties(convertProperties(block.getProperties()));

      result.add(blockTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private VisualLayoutTO toVisualLayoutTO(VisualLayoutCreationTO layout) {
    VisualLayoutTO result = new VisualLayoutTO();

    result.setName(layout.getName())
        .setProperties(convertProperties(layout.getProperties()));
    result.setScaleX((float) layout.getScaleX())
        .setScaleY((float) layout.getScaleY())
        .setLayers(toLayerTOs(layout.getLayers()))
        .setLayerGroups(toLayerGroupTOs(layout.getLayerGroups()));

    return result;
  }

  private List<VisualLayoutTO.Layer> toLayerTOs(List<Layer> layers) {
    List<VisualLayoutTO.Layer> result = new ArrayList<>();

    for (Layer layer : layers) {
      result.add(
          new VisualLayoutTO.Layer()
              .setId(layer.getId())
              .setOrdinal(layer.getOrdinal())
              .setVisible(layer.isVisible())
              .setName(layer.getName())
              .setGroupId(layer.getGroupId())
      );
    }

    return result;
  }

  private List<VisualLayoutTO.LayerGroup> toLayerGroupTOs(List<LayerGroup> layerGroups) {
    List<VisualLayoutTO.LayerGroup> result = new ArrayList<>();

    for (LayerGroup layerGroup : layerGroups) {
      result.add(
          new VisualLayoutTO.LayerGroup()
              .setId(layerGroup.getId())
              .setName(layerGroup.getName())
              .setVisible(layerGroup.isVisible())
      );
    }

    return result;
  }

  private List<PointTO.OutgoingPath> getOutgoingPaths(
      PointCreationTO point,
      List<PathCreationTO> paths
  ) {
    List<PointTO.OutgoingPath> result = new ArrayList<>();

    for (PathCreationTO path : paths) {
      if (Objects.equals(path.getSrcPointName(), point.getName())) {
        result.add(new PointTO.OutgoingPath().setName(path.getName()));
      }
    }

    Collections.sort(result, Comparators.outgoingPathsByName());

    return result;
  }

  private List<AllowedOperationTO> toAllowedOperationTOs(Collection<String> allowedOperations) {
    return allowedOperations.stream()
        .sorted()
        .map(allowedOperation -> {
          return (AllowedOperationTO) new AllowedOperationTO().setName(allowedOperation);
        })
        .toList();
  }

  private List<AllowedPeripheralOperationTO> toAllowedPeripheralOperationTOs(
      Collection<String> allowedOperations
  ) {
    return allowedOperations.stream()
        .sorted()
        .map(allowedOperation -> {
          return (AllowedPeripheralOperationTO) new AllowedPeripheralOperationTO()
              .setName(allowedOperation);
        })
        .toList();
  }

  private List<LocationTO.Link> toLocationTOLinks(Map<String, Set<String>> links) {
    List<LocationTO.Link> result = new ArrayList<>();

    links.forEach((key, value) -> {
      result.add(
          new LocationTO.Link()
              .setPoint(key)
              .setAllowedOperations(toAllowedOperationTOs(value))
      );
    });

    Collections.sort(result, Comparators.linksByPointName());

    return result;
  }

  private List<MemberTO> toMemberTOs(Collection<String> members) {
    return members.stream()
        .map(member -> (MemberTO) new MemberTO().setName(member))
        .sorted(Comparators.elementsByName())
        .toList();
  }

  private List<PropertyTO> convertProperties(Map<String, String> properties) {
    List<PropertyTO> result = new ArrayList<>();

    properties.forEach((key, value) -> {
      result.add(new PropertyTO().setName(key).setValue(value));
    });

    Collections.sort(result, Comparators.propertiesByName());

    return result;
  }

  private boolean isNullOrEmpty(String s) {
    return s == null || s.isEmpty();
  }

  private Map<String, Envelope> toEnvelopeMap(List<VehicleEnvelopeTO> envelopeTOs) {
    return envelopeTOs.stream()
        .collect(
            Collectors.toMap(
                VehicleEnvelopeTO::getKey,
                vehicleEnvelopeTO -> toEnvelope(vehicleEnvelopeTO)
            )
        );
  }

  private Envelope toEnvelope(VehicleEnvelopeTO vehicleEnvelopeTO) {
    return new Envelope(
        vehicleEnvelopeTO.getVertices().stream()
            .map(coupleTO -> new Couple(coupleTO.getX(), coupleTO.getY()))
            .toList()
    );
  }

  private List<VehicleEnvelopeTO> toVehicleEnvelopeTOs(Map<String, Envelope> envelopeMap) {
    return envelopeMap.entrySet()
        .stream()
        .sorted(Map.Entry.comparingByKey())
        .map(
            entry -> new VehicleEnvelopeTO()
                .setKey(entry.getKey())
                .setVertices(toCoupleTOs(entry.getValue().getVertices()))
        )
        .toList();
  }

  private List<CoupleTO> toCoupleTOs(List<Couple> couples) {
    return couples.stream()
        .map(
            couple -> new CoupleTO()
                .setX(couple.getX())
                .setY(couple.getY())
        )
        .toList();
  }

  private BoundingBoxCreationTO toBoundingBoxCreationTO(BoundingBoxTO boundingBox) {
    return new BoundingBoxCreationTO(
        boundingBox.getLength(),
        boundingBox.getWidth(),
        boundingBox.getHeight()
    )
        .withReferenceOffset(
            new CoupleCreationTO(
                boundingBox.getReferenceOffsetX(),
                boundingBox.getReferenceOffsetY()
            )
        );
  }

  private VehicleCreationTO.EnergyLevelThresholdSet toEnergyLevelThresholdSetCreationTO(
      VehicleTO vehicle
  ) {
    return new VehicleCreationTO.EnergyLevelThresholdSet(
        vehicle.getEnergyLevelCritical().intValue(),
        vehicle.getEnergyLevelGood().intValue(),
        vehicle.getEnergyLevelSufficientlyRecharged().intValue(),
        vehicle.getEnergyLevelFullyRecharged().intValue()
    );
  }

  private BoundingBoxTO toBoundingBoxTO(BoundingBoxCreationTO boundingBox) {
    return new BoundingBoxTO()
        .setLength(boundingBox.getLength())
        .setWidth(boundingBox.getWidth())
        .setHeight(boundingBox.getHeight())
        .setReferenceOffsetX(boundingBox.getReferenceOffset().getX())
        .setReferenceOffsetY(boundingBox.getReferenceOffset().getY());
  }
}
