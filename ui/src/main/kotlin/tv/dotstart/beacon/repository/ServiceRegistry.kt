/*
 * Copyright (C) 2019 Johannes Donath <johannesd@torchmind.com>
 * and other copyright owners as documented in the project's IP log.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tv.dotstart.beacon.repository

import org.apache.commons.compress.compressors.CompressorStreamFactory
import tv.dotstart.beacon.BeaconCli
import tv.dotstart.beacon.config.Configuration
import tv.dotstart.beacon.preload.Loader
import tv.dotstart.beacon.repository.error.*
import tv.dotstart.beacon.repository.loader.RepositoryLoader
import tv.dotstart.beacon.repository.model.Service
import tv.dotstart.beacon.util.Cache
import tv.dotstart.beacon.util.logger
import java.io.BufferedInputStream
import java.net.URI
import java.nio.file.Files

/**
 * Manages the listing of available service definitions as well as the download of remote service
 * repositories.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
object ServiceRegistry : Iterable<Service> {

  private val logger = ServiceRegistry::class.logger

  private val services = mutableMapOf<URI, Service>()

  /**
   * Refreshes all services within the given list at once.
   */
  fun refresh(repositories: List<URI>) {
    if (repositories.isEmpty()) {
      logger.info("Nothing to refresh - Skipping")
      return
    }

    repositories.forEach {
      try {
        this.refresh(it)
      } catch (ex: IllegalRepositorySchemeException) {
        logger.warn("""Failed to refresh repository "$it": Unknown repository scheme""")
      } catch (ex: IllegalRepositorySpecificationException) {
        logger.warn("""Failed to refresh repository "$it": Malformed provider URI""")
      } catch (ex: MalformedRepositoryException) {
        logger.warn("""Failed to parse repository "$it"""", ex)
      } catch (ex: NoSuchRepositoryException) {
        logger.warn("""Failed to refresh repository "$it": No such repository""")
      } catch (ex: RepositoryAvailabilityException) {
        logger.warn("""Failed to refresh repository "$it": Temporarily unavailable""", ex)
      } catch (ex: IllegalRepositoryException) {
        logger.warn("""Failed to refresh repository "$it": Unknown Error""", ex)
      }
    }
  }

  /**
   * Refresh the repository at the given location.
   */
  fun refresh(location: URI) {
    logger.info("Loading repository $location")

    val path = Cache(location.toString()) {
      logger.info("Retrieving updated version of $location")
      RepositoryLoader(location, it)
    }

    val repository = Files.newInputStream(path).use {
      BufferedInputStream(it).use { buffered ->
        val compressor = CompressorStreamFactory.detect(buffered)
            ?: throw MalformedRepositoryException(
                "Cannot detect repository compression type")
        logger.debug("Detected $compressor compression")

        CompressorStreamFactory().createCompressorInputStream(compressor, buffered)
            .use { compressed ->
              Model.Repository.parseFrom(compressed)
            }
      }
    }

    this.refresh(repository)
  }

  /**
   * Refreshes a single repository model.
   */
  private fun refresh(repository: Model.Repository) {
    if (repository.displayName != null) {
      logger.debug("""Repository identifies itself as "${repository.displayName}"""")
    } else {
      logger.debug("""Repository does not include identification metadata""")
    }
    logger.debug("Parsing ${repository.serviceCount} services")

    repository.serviceList
        .mapNotNull {
          try {
            Service(it)
          } catch (ex: IllegalArgumentException) {
            logger.warn("""Failed to parse service "${it.id}"""", ex)
            null
          }
        }
        .forEach { this += it }
  }

  /**
   * Registers a new service with this registry.
   */
  operator fun plusAssign(service: Service) {
    logger.info("""Registered service "$service.id"""")
    this.services[service.id] = service
  }

  /**
   * Deletes a service from this registry.
   */
  operator fun minusAssign(service: Service) {
    this.services.remove(service.id)
  }

  override fun iterator(): Iterator<Service> = this.services.values.iterator()

  /**
   * Performs a refresh of all system repositories.
   */
  object SystemRepositoryLoader : Loader {

    override val description = "preloader.service.system"

    override fun load() {
      refresh(BeaconCli.systemRepositories)
    }
  }

  /**
   * Performs a refresh of all user repositories.
   */
  object UserRepositoryLoader : Loader {

    override val description = "preloader.service.user"

    override fun load() {
      refresh(Configuration.userRepositoryIndex)
    }
  }
}
