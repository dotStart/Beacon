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
 * <strong>Custom Service</strong>
 *
 * Provides a customized service representation.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 * TODO: In the future we may want to use this to store custom configurations permanently.
 */
public class CustomService implements Service {
        private static final Image STANDARD_IMAGE = new Image(CustomService.class.getResource("/image/logo.png").toExternalForm());
        private final int port;
        private final ProtocolType type;

        public CustomService(@Nonnull ProtocolType type, @Nonnegative int port) {
                this.type = type;
                this.port = port;
        }

        /**
         * {@inheritDoc}
         */
        @Nonnull
        @Override
        public String getDisplayName() {
                return "Custom"; // TODO: Proper naming for storage
        }

        /**
         * {@inheritDoc}
         */
        @Nonnull
        @Override
        public Image getLogo() {
                return STANDARD_IMAGE;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getPort() {
                return this.port;
        }

        /**
         * {@inheritDoc}
         */
        @Nonnull
        @Override
        public ProtocolType getType() {
                return this.type;
        }
}
