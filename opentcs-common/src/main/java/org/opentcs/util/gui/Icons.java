// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.gui;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static methods related to window icons.
 */
public final class Icons {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Icons.class);
  /**
   * Path to the openTCS window icons.
   */
  private static final String ICON_PATH = "/org/opentcs/util/gui/res/icons/";
  /**
   * File names of the openTCS window icons.
   */
  private static final String[] ICON_FILES = {"opentcs_icon_016.png",
      "opentcs_icon_032.png",
      "opentcs_icon_064.png",
      "opentcs_icon_128.png",
      "opentcs_icon_256.png"};

  /**
   * Prevents instantiation.
   */
  private Icons() {
    // Do nada.
  }

  /**
   * Get the icon for OpenTCS windows in different resolutions.
   *
   * @return List of icons
   */
  public static List<Image> getOpenTCSIcons() {
    try {
      List<Image> icons = new ArrayList<>();
      for (String iconFile : ICON_FILES) {
        String iconURL = ICON_PATH + iconFile;
        final Image icon = ImageIO.read(Icons.class.getResource(iconURL));
        icons.add(icon);
      }
      return icons;
    }
    catch (IOException | IllegalArgumentException exc) {
      LOG.warn("Couldn't load icon images from path {}", ICON_PATH, exc);
      return new ArrayList<>();
    }
  }
}
