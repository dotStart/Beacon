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

import com.google.inject.Singleton;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <strong>Localization Service</strong>
 *
 * Provides a service which automatically manages the selection of translations.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
@Singleton
public class LocalizationService {
        public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

        /**
         * Attempts to load the most fitting resource bundle.
         *
         * @param name a bundle name.
         * @return a resource bundle.
         */
        @Nonnull
        public ResourceBundle load(@Nonnull String name) {
                try {
                        return ResourceBundle.getBundle("localization/" + name);
                } catch (MissingResourceException ex) {
                        return ResourceBundle.getBundle("localization/" + name, DEFAULT_LOCALE);
                }
        }
}
