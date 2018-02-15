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
 * <strong>Stock Service</strong>
 *
 * Provides a list of known service types which can be configured with a single click.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public enum StockService implements Service {
  MINECRAFT(
      "Minecraft",
      new Image(StockService.class.getResource("/image/service/minecraft.png").toExternalForm()),
      25565, ProtocolType.TCP
  ),
  CSGO(
      "Counter-Strike: Global Offensive",
      new Image(StockService.class.getResource("/image/service/csgo.png").toExternalForm()),
      27015, ProtocolType.UDP
  ),
  KILLING_FLOOR_2(
      "Killing Floor 2",
      new Image(StockService.class.getResource("/image/service/kf2.png").toExternalForm()),
      7777, ProtocolType.UDP
  ),
  RUST(
      "Rust",
      new Image(StockService.class.getResource("/image/service/rust.png").toExternalForm()),
      28015, ProtocolType.UDP
  ),
  STARBOUND(
      "Starbound",
      new Image(StockService.class.getResource("/image/service/starbound.png").toExternalForm()),
      21025, ProtocolType.TCP
  );

  private final String displayName;
  private final Image logo;
  private final int port;
  private final ProtocolType type;

  StockService(@Nonnull String displayName, @Nonnull Image logo, @Nonnegative int port,
      @Nonnull ProtocolType type) {
    this.displayName = displayName;
    this.logo = logo;
    this.port = port;
    this.type = type;
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public String getDisplayName() {
    return this.displayName;
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public Image getLogo() {
    return this.logo;
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
