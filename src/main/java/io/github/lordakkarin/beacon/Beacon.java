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
package io.github.lordakkarin.beacon;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.github.lordakkarin.beacon.inject.FXMLLoaderProvider;
import io.github.lordakkarin.beacon.upnp.ServiceManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ResourceBundle;

/**
 * <strong>Beacon</strong>
 *
 * Provides a JavaFX application entry point which will initialize the entire GUI.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class Beacon extends Application implements Module {
        private final Injector injector;
        private static final Logger logger = LogManager.getFormatterLogger(Beacon.class);

        public Beacon() {
                super();

                this.injector = Guice.createInjector(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void configure(@Nonnull Binder binder) {
                binder.bind(FXMLLoader.class).toProvider(FXMLLoaderProvider.class);
        }

        /**
         * <strong>Main Entry Point</strong>
         *
         * Provides an entry point to the JVM which is automatically executed when double-clicking a jar (while Java is
         * associated with the jar file extension) as well as double-clicking or otherwise launching the wrapped
         * executable version of this program.
         *
         * @param arguments an array of command line arguments.
         */
        public static void main(@Nonnull String[] arguments) {
                launch(Beacon.class, arguments);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void start(@Nonnull Stage primaryStage) throws Exception {
                primaryStage.getIcons().add(new Image(Beacon.class.getResource("/image/logo.png").toExternalForm()));

                logger.info("Beacon UPnP Port Forwarding Assistant");
                logger.info("Copyright (C) 2016 Johannes Donath and other copyright owners as documented in the project's IP log.");
                logger.info("Licensed under the terms of the Apache License, Version 2.0");
                primaryStage.initStyle(StageStyle.UNDECORATED);

                FXMLLoader loader = this.injector.getInstance(FXMLLoader.class);
                loader.setLocation(Beacon.class.getResource("/fxml/BeaconWindow.fxml"));
                loader.setResources(ResourceBundle.getBundle("localization/BeaconWindow"));

                Scene scene = new Scene(loader.load(), 400, 500);
                primaryStage.setScene(scene);
                primaryStage.show();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void stop() throws Exception {
                logger.info("Shutting down beacon ...");
                this.injector.getInstance(ServiceManager.class).shutdown();
                logger.info("Good bye! :)");

                super.stop();
        }
}
