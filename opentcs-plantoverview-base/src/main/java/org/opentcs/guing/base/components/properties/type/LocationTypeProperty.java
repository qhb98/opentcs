// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.components.properties.type;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * A property that can take a value from a given set of location types.
 */
public class LocationTypeProperty
    extends
      AbstractProperty
    implements
      Selectable<String> {

  private List<String> fPossibleValues;

  public LocationTypeProperty(ModelComponent model) {
    this(model, new ArrayList<>(), "");
  }

  public LocationTypeProperty(ModelComponent model, List<String> possibleValues, Object value) {
    super(model);
    this.fPossibleValues = requireNonNull(possibleValues, "possibleValues");
    fValue = value;
  }

  @Override
  public Object getComparableValue() {
    return fValue;
  }

  @Override
  public void setValue(Object value) {
    if (fPossibleValues.contains(value)
        || value instanceof AcceptableInvalidValue) {
      super.setValue(value);
    }
  }

  @Override
  public List<String> getPossibleValues() {
    return fPossibleValues;
  }

  @Override
  public void setPossibleValues(List<String> possibleValues) {
    fPossibleValues = requireNonNull(possibleValues, "possibleValues");
  }

  @Override
  public void copyFrom(Property property) {
    LocationTypeProperty locTypeProperty = (LocationTypeProperty) property;
    setValue(locTypeProperty.getValue());
  }

  @Override
  public String toString() {
    return getValue().toString();
  }
}
