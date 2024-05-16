/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.persistence.unified;

import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import javax.swing.filechooser.FileFilter;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.guing.common.persistence.ModelFileReader;
import org.opentcs.guing.common.persistence.unified.UnifiedModelConstants;
import org.opentcs.util.persistence.ModelParser;

/**
 * Implementation of [@link ModelFileReader} to deserialize a {@link PlantModelCreationTO} from a
 * xml file.
 */
public class UnifiedModelReader
    implements ModelFileReader {

  /**
   * The model parser.
   */
  private final ModelParser modelParser;

  @Inject
  public UnifiedModelReader(ModelParser modelParser) {
    this.modelParser = requireNonNull(modelParser, "modelParser");
  }

  @Override
  public Optional<PlantModelCreationTO> deserialize(File file)
      throws IOException {
    requireNonNull(file, "file");

    PlantModelCreationTO plantModel = modelParser.readModel(file);
    return Optional.of(plantModel);
  }

  @Override
  public FileFilter getDialogFileFilter() {
    return UnifiedModelConstants.DIALOG_FILE_FILTER;
  }
}
