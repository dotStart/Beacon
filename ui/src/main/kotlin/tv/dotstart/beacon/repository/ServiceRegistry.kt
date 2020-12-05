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
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream
import tv.dotstart.beacon.BeaconCli
import tv.dotstart.beacon.config.Configuration
import tv.dotstart.beacon.core.artifact.ArtifactProvider
import tv.dotstart.beacon.preload.Loader
import tv.dotstart.beacon.core.artifact.error.*
import tv.dotstart.beacon.repository.model.Service
import tv.dotstart.beacon.repository.error.MalformedRepositoryException
import tv.dotstart.beacon.util.Cache
import tv.dotstart.beacon.core.util.OperatingSystem
import tv.dotstart.beacon.util.logger
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Manages the listing of available service definitions as well as the download of remote service
 * repositories.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
object ServiceRegistry : Iterable<Service> {

  private val logger = ServiceRegistry::class.logger

  private val artifactProvider = ArtifactProvider.forDiscoveredLoaders()

  private val customPath = OperatingSystem.current.storageDirectory
      .resolve("custom.dat")
  private val customBackupPath = OperatingSystem.current.storageDirectory
      .resolve("custom.dat.bak")

  private val services = mutableMapOf<URI, Service>()
  private val customServices = mutableMapOf<URI, Service>()

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
      } catch (ex: ArtifactSchemeException) {
        logger.warn("""Failed to refresh repository "$it": Unknown repository scheme""")
      } catch (ex: ArtifactSpecificationException) {
        logger.warn("""Failed to refresh repository "$it": Malformed provider URI""")
      } catch (ex: MalformedRepositoryException) {
        logger.warn("""Failed to parse repository "$it"""", ex)
      } catch (ex: NoSuchArtifactException) {
        logger.warn("""Failed to refresh repository "$it": No such repository""")
      } catch (ex: ArtifactAvailabilityException) {
        logger.warn("""Failed to refresh repository "$it": Temporarily unavailable""", ex)
      } catch (ex: ArtifactException) {
        logger.warn("""Failed to refresh repository "$it": Unknown Error""", ex)
      }
    }
  }

  /**
   * Refresh the repository at the given location.
   */
  fun refresh(location: URI) {
    logger.info("Loading repository $location")

    val repository = Cache.getOrPopulate(location.toString()) {
      this.artifactProvider.retrieve(location)
    }

    this.refresh(repository)
  }

  /**
   * Refreshes all custom service definitions (if any).
   */
  fun refreshCustom() {
    logger.info("Loading custom services from $customPath")

    if (!Files.exists(this.customPath)) {
      logger.warn("No custom service definitions defined")
      return
    }

    try {
      this.refresh(this.customPath)
    } catch (ex: Throwable) {
      logger.error("Failed to decode custom service definitions - Removing file", ex)

      try {
        Files.delete(this.customPath)

        if (Files.exists(this.customBackupPath)) {
          logger.info(
              "Attempting to restore custom definitions from prior backup file at $customBackupPath")

          try {
            this.refresh(this.customBackupPath)
            this.persist()
          } catch (ex: Throwable) {
            logger.error("Failed to persist restored custom service definitions", ex)
          }
        }
      } catch (e: IOException) {
        logger.error("Failed to replace corrupted service definition file", e)
        return
      }
    }
  }

  /**
   * Refreshes a repository which has previously been stored as a local file.
   */
  fun refresh(path: Path) {
    Files.newInputStream(path).use<InputStream, Unit>(this::refresh)
  }

  private fun refresh(payload: ByteArray) {
    ByteArrayInputStream(payload).use(this::refresh)
  }

  private fun refresh(stream: InputStream) {
    val repository = BufferedInputStream(stream).use { buffered ->
      val compressor = CompressorStreamFactory.detect(buffered)
          ?: throw MalformedRepositoryException(
              "Cannot detect repository compression type")
      logger.trace("Detected $compressor compression")

      CompressorStreamFactory().createCompressorInputStream(compressor, buffered)
          .use { compressed ->
            Model.Repository.parseFrom(compressed)
          }
    }

    this.refresh(repository)
  }

  /**
   * Refreshes a single repository model.
   */
  private fun refresh(repository: Model.Repository) {
    if (repository.displayName != null) {
      logger.debug("""Display Name: "${repository.displayName}"""")
    } else {
      logger.debug("""Display Name: <unset>""")
    }
    logger.debug("Revision: ${repository.revision}")
    logger.trace("Parsing ${repository.serviceCount} services")

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
   * Persists all custom service definitions.
   */
  fun persist() {
    if (Files.exists(this.customPath)) {
      logger.info("Moving prior version of custom service definitions to $customBackupPath")
      Files.move(this.customPath, this.customBackupPath, StandardCopyOption.REPLACE_EXISTING)
    }

    logger.info("Persisting custom service definitions to $customPath")

    val repository = Model.Repository.newBuilder()
        .setRevision(0)
        .setDisplayName("Custom Services")
        .addAllService(
            this.customServices.values
                .map(Service::toRepositoryDefinition))
        .build()

    Files.newOutputStream(this.customPath)
        .let(::XZCompressorOutputStream)
        .use(repository::writeTo)
  }

  /**
   * Registers a new service with this registry.
   */
  operator fun plusAssign(service: Service) {
    logger.info("""Registered service "${service.id}"""")
    logger.debug(service)
    this.services[service.id] = service

    if (service.category == Model.Category.CUSTOM) {
      this.customServices[service.id] = service
    }
  }

  /**
   * Deletes a service from this registry.
   */
  operator fun minusAssign(service: Service) {
    this.services.remove(service.id)
    this.customServices.remove(service.id)
  }

  override fun iterator(): Iterator<Service> = this.services.values.iterator()

  /**
   * Performs a refresh of all system repositories.
   */
  object SystemRepositoryLoader : Loader {

    override val description = "service.system"

    override fun load() {
      logger.info("Refreshing system repositories")
      refresh(BeaconCli.systemRepositories)
    }
  }

  /**
   * Performs a refresh of all user repositories.
   */
  object UserRepositoryLoader : Loader {

    override val description = "service.user"

    override fun load() {
      logger.info("Refreshing user repositories")
      refresh(Configuration.userRepositoryIndex)
    }
  }

  object CustomRepositoryLoader : Loader {

    override val description = "service.custom"

    override fun load() {
      logger.info("Refreshing custom repository")
      refreshCustom()
    }
  }
}
