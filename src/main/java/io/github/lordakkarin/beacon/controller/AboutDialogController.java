/*
 * Copyright 2016 Johannes Donath <johannesd@torchmind.com>
 * and other copyright owners as documented in the project's IP log.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.lordakkarin.beacon.controller;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * <strong>About Window Controller</strong>
 *
 * Provides a window which denotes all important copyrights.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class AboutDialogController {

  @FXML
  private VBox root;

  /**
   * Handles clicks on the main window.
   */
  @FXML
  private void onClick() {
    ((Stage) this.root.getScene().getWindow()).close();
  }

  /**
   * Handles clicks on the open source button.
   *
   * @throws IOException when constructing the URL or opening the browser fails.
   * @throws URISyntaxException when the hardcoded URL is malformed.
   */
  @FXML
  private void onThirdpartyClick() throws IOException, URISyntaxException {
    Desktop desktop = (Desktop.isDesktopSupported() ? Desktop.getDesktop() : null);

    if (desktop == null || !desktop.isSupported(Desktop.Action.BROWSE)) {
      Alert alert = new Alert(Alert.AlertType.ERROR,
          "Could not open URL in browser. Please go to https://github.com/LordAkkarin/Beacon/wiki/Thirdparty-Acknowledgements to view a full list of included libraries.");
      alert.showAndWait();
      return;
    }

    desktop.browse(
        new URL("https://github.com/LordAkkarin/Beacon/wiki/Thirdparty-Acknowledgements").toURI());
  }
}
