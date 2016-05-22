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
package io.github.lordakkarin.beacon.upnp;

import javafx.scene.image.Image;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * <strong>Service</strong>
 *
 * Represents a custom or predefined service which can be published.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public interface Service {

        /**
         * Retrieves the service display name.
         *
         * @return a display name.
         */
        @Nonnull
        String getDisplayName();

        /**
         * Retrieves a logo for this service.
         *
         * @return a logo.
         */
        @Nonnull
        Image getLogo();

        /**
         * Retrieves the service port.
         *
         * @return a port.
         */
        @Nonnegative
        int getPort();

        /**
         * Retrieves the service protocol type.
         *
         * @return a type.
         */
        @Nonnull
        ProtocolType getType();
}
