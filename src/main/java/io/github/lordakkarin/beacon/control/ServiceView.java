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

import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * <strong>Service View</strong>
 *
 * Provides a view which is designed to be part of a list cell.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class ServiceView extends HBox implements Initializable {
        @FXML
        private Label details;
        private StringProperty detailsProperty = new SimpleStringProperty();
        private final ObjectProperty<Image> imageProperty = new SimpleObjectProperty<>();
        @FXML
        private ImageView imageView;
        @FXML
        private Label titleLabel;
        private StringProperty titleProperty = new SimpleStringProperty();

        public ServiceView() throws IOException {
                super();

                FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/ServiceView.fxml"));
                loader.setRoot(this);
                loader.setController(this);
                loader.load();
        }

        @Nonnull
        public StringProperty detailsPropertyProperty() {
                return detailsProperty;
        }

        @Nullable
        public String getDetailsProperty() {
                return detailsProperty.get();
        }

        @Nullable
        public Image getImageProperty() {
                return this.imageProperty.get();
        }

        @Nullable
        public String getTitleProperty() {
                return this.titleProperty.get();
        }

        @Nonnull
        public ObjectProperty<Image> imagePropertyProperty() {
                return this.imageProperty;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void initialize(URL location, ResourceBundle resources) {
                this.imageView.imageProperty().bind(this.imageProperty);
                this.titleLabel.textProperty().bind(this.titleProperty);
                this.details.textProperty().bind(this.detailsProperty);
        }

        public void setDetailsProperty(@Nullable String detailsProperty) {
                this.detailsProperty.set(detailsProperty);
        }

        public void setImageProperty(@Nullable Image imageProperty) {
                this.imageProperty.set(imageProperty);
        }

        public void setTitleProperty(@Nullable String titleProperty) {
                this.titleProperty.set(titleProperty);
        }

        @Nonnull
        public Property<String> titlePropertyProperty() {
                return this.titleProperty;
        }
}
