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

import com.google.inject.Inject;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.annotation.Nonnull;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.support.igd.PortMappingListener;
import org.fourthline.cling.support.model.PortMapping;

/**
 * <strong>Port Forwarding Service</strong>
 *
 * Provides a simplified service which globally handles UPnP registrations.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
@Singleton
public class ServiceManager {

  private static final Logger logger = LogManager.getFormatterLogger(ServiceManager.class);
  private final ObservableList<NetworkInterface> interfaceList = FXCollections
      .observableList(new CopyOnWriteArrayList<>());
  private final Map<Service, UpnpService> serviceMap = new ConcurrentHashMap<>();

  @Inject
  public ServiceManager() {
    this.refreshInterfaceList();
  }

  /**
   * Retrieves the first compatible address of a network interface on this machine.
   *
   * @param networkInterface an interface.
   * @return an address or, if no address was found, an empty optional.
   */
  @Nonnull
  public Optional<InetAddress> findCompatibleAddress(@Nonnull NetworkInterface networkInterface) {
    Enumeration<InetAddress> addressEnumeration = networkInterface.getInetAddresses();

    if (!addressEnumeration.hasMoreElements()) {
      return Optional.empty();
    }

    return Optional.of(addressEnumeration.nextElement());
  }

  /**
   * Retrieves a list of compatible network interfaces on this machine.
   *
   * @return a list of interfaces.
   */
  @Nonnull
  public ObservableList<NetworkInterface> getInterfaceList() {
    return FXCollections.unmodifiableObservableList(this.interfaceList);
  }

  /**
   * Publishes a service for a specified IP address.
   *
   * @param address an address.
   * @param service a service.
   */
  public void publishService(@Nonnull InetAddress address, @Nonnull Service service) {
    PortMapping mapping = new PortMapping(
        service.getPort(),
        address.getHostAddress(),
        (service.getType() == ProtocolType.TCP ? PortMapping.Protocol.TCP
            : PortMapping.Protocol.UDP),
        "Beacon Service (" + service.getDisplayName() + ")"
    );

    // ensure our lease is refreshed every ~30 seconds so we do not end up with zombie leases in our UPnP
    // configuration even when the application or PC crashes
    // FIXME: This causes the lease to expire entirely even while the application is running so we can't quite rely on it at the moment
    // mapping.setLeaseDurationSeconds(new UnsignedIntegerFourBytes(30));

    logger.info("Publishing service %s (using %s on port %d) on %s.", service.getDisplayName(),
        service.getType(), service.getPort(), address.getAddress());
    UpnpService upnpService = new UpnpServiceImpl(new PortMappingListener(mapping));
    upnpService.getControlPoint().search();
    this.serviceMap.put(service, upnpService);
  }

  /**
   * Refreshes the list of compatible interfaces.
   */
  public void refreshInterfaceList() {
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      this.interfaceList.clear();

      while (interfaces.hasMoreElements()) {
        NetworkInterface currentInterface = interfaces.nextElement();

        try {
          logger.info("Inspecting %s (interface %s).", currentInterface.getDisplayName(),
              currentInterface.getName());

          if (!currentInterface.isUp()) {
            throw new IllegalStateException("Device is offline");
          }

          if (!currentInterface.supportsMulticast()) {
            throw new IllegalStateException("Device does not support multicast");
          }

          if (currentInterface.isLoopback()) {
            logger.debug("Skipping loopback device %s.", currentInterface.getDisplayName());
            continue;
          }

          if (currentInterface.isVirtual()) {
            throw new IllegalStateException("Device is virtual");
          }

          if (currentInterface.isPointToPoint()) {
            throw new IllegalStateException("Device is a Point-to-Point device");
          }

          this.interfaceList.add(currentInterface);
        } catch (IllegalStateException ex) {
          logger.info("Skipping %s: %s", currentInterface.getDisplayName(), ex.getMessage());
        } catch (SocketException ex) {
          logger.warn("Skipping " + currentInterface.getDisplayName() + ": " + ex.getMessage(), ex);
        }
      }
    } catch (SocketException ex) {
      logger.error("Could not retrieve list of devices: " + ex.getMessage(), ex);
    }
  }

  /**
   * Un-publishes all active services.
   */
  public void shutdown() {
    logger.info("Cleaning up all remaining services ...");
    this.serviceMap.keySet().forEach(this::unpublishService);
    logger.info("All active services were un-published.");
  }

  /**
   * Un-Publishes a service.
   *
   * @param service a service.
   */
  public void unpublishService(@Nonnull Service service) {
    UpnpService upnpService = this.serviceMap.get(service);

    if (upnpService == null) {
      throw new NoSuchElementException("No such service: " + service);
    }

    logger.info("Un-publishing service %s (using %s on port %d).", service.getDisplayName(),
        service.getType(), service.getPort());
    upnpService.shutdown();
    this.serviceMap.remove(service);
  }
}
