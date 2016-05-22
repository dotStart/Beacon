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
package io.github.lordakkarin.beacon.control;

import javafx.scene.control.TextField;

import javax.annotation.Nonnull;

/**
 * <strong>Number Field</strong>
 *
 * Provides an extension to {@link TextField} which will only allow numbers to be part of its contents.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class NumberField extends TextField {

        /**
         * {@inheritDoc}
         */
        @Override
        public void replaceSelection(String replacement) {
                if (this.validate(replacement)) {
                        super.replaceSelection(replacement);
                }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void replaceText(int start, int end, String text) {
                if (this.validate(text)) {
                        super.replaceText(start, end, text);
                }
        }

        /**
         * Validates the text.
         *
         * @param text a text.
         * @return true if valid, false otherwise.
         */
        private boolean validate(@Nonnull String text) {
                return text.matches("^[0-9]*$");
        }
}
