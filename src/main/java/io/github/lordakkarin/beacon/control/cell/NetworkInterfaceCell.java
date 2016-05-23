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

import javafx.scene.control.ListCell;

import java.net.NetworkInterface;

/**
 * <strong>Network Interface Cell</strong>
 *
 * Provides a customized cell implementation which is able to properly display a network interface.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class NetworkInterfaceCell extends ListCell<NetworkInterface> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected void updateItem(NetworkInterface item, boolean empty) {
                super.updateItem(item, empty);

                if (!empty) {
                        this.setText(item.getDisplayName());
                }
        }
}
