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
package io.github.lordakkarin.beacon.control.cell;

import io.github.lordakkarin.beacon.control.ServiceView;
import io.github.lordakkarin.beacon.upnp.Service;
import java.io.IOException;
import javafx.scene.control.ListCell;

/**
 * <strong>Service Cell</strong>
 *
 * Provides a specialized cell implementation which is capable of displaying services.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class ServiceCell extends ListCell<Service> {

  /**
   * {@inheritDoc}
   */
  @Override
  protected void updateItem(Service item, boolean empty) {
    super.updateItem(item, empty);

    if (!empty) {
      try {
        ServiceView serviceView = new ServiceView();
        serviceView.setImageProperty(item.getLogo());
        serviceView.setTitleProperty(item.getDisplayName());
        serviceView.setDetailsProperty(String.format("%d (%s)", item.getPort(), item.getType()));
        this.setGraphic(serviceView);
      } catch (IOException ex) {
        throw new IllegalStateException("Could not construct service view: " + ex.getMessage(), ex);
      }
    }
  }
}
