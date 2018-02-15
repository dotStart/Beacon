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
package io.github.lordakkarin.beacon.inject;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import javafx.fxml.FXMLLoader;
import javafx.util.Callback;

/**
 * <strong>FXML Loader Provider</strong>
 *
 * A provider implementation which will automatically inject new provider instances using Guice.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class FXMLLoaderProvider implements Provider<FXMLLoader> {

  private final Injector injector;

  @Inject
  public FXMLLoaderProvider(Injector injector) {
    this.injector = injector;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FXMLLoader get() {
    FXMLLoader loader = new FXMLLoader();
    loader.setControllerFactory(new GuiceControllerFactory());
    return loader;
  }

  /**
   * <strong>Guice Controller Factory</strong>
   *
   * Provides a simple controller factory which constructs new controller instances using Guice's
   * injector.
   */
  private class GuiceControllerFactory implements Callback<Class<?>, Object> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Object call(Class<?> param) {
      return FXMLLoaderProvider.this.injector.getInstance(param);
    }
  }
}
