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
package tv.dotstart.beacon.repository.compiler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.logging.log4j.LogManager
import tv.dotstart.beacon.repository.compiler.model.Repository
import java.awt.image.BufferedImage
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO

/**
 * Provides a command line utility which converts JSON definitions into a serialized repository
 * definition.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */
object Compiler {

  private const val ICON_SIZE = 24

  private val logger = LogManager.getLogger(Compiler::class.java)

  private val mapper = jacksonObjectMapper()

  val client = OkHttpClient.Builder() // TODO: User Agent
    .build()

  /**
   * JVM Entry Point
   *
   * This method is used when directly invoking this library from the command line (e.g. for oneshot
   * repository creation).
   *
   * If you wish to embed this compiler, please refer to [compile] for more information.
   */
  @JvmStatic
  fun main(args: Array<String>) {
    if (args.size != 2) {
      println("Usage: java -jar repository.jar <repository>")
      println()
      println("Compiles a JSON document into a binary repository file")
      println("which may be used with Beacon:")
      println()
      println("  $ java -jar repository.jar definition.json repository.dat")
      println()
      println("Generally, the JSON file is expected to be formatted as")
      println("follows:")
      println()
      println("  {")
      println("""    "displayName": "My Repository",""")
      println("""    "revision": 42,""")
      println("""    "services": [""")
      println("      {")
      println("""        "id": "game://valvesoftware.com/csgo",""")
      println("""        "title": "Counter Strike: Global Offensive",""")
      println("""        "icon": "file://icon/csgo.png",""")
      println("""        "category": "GAME_SERVER",""")
      println("""        "ports": [""")
      println("""          {""")
      println("""            "protocol": "TCP",""")
      println("""            "port": 27015""")
      println("""          },""")
      println("""          {""")
      println("""            "protocol": "UDP",""")
      println("""            "port": 27015""")
      println("""          }""")
      println("""        ]""")
      println("      }")
      println("    }")
      println("  ]")
      println()
      println("Note that the icon field may be omitted if none is available. If given, it is")
      println(
        "expected to to be a file, http or https URL which points to the desired image. When"
      )
      println("an icon is given, it will be converted into the PNG format and resized to the")
      println("default icon size (currently ${ICON_SIZE}x$ICON_SIZE).")
      println()
      println("In addition, the revision and displayName fields in the repository definition may")
      println("be omitted. The revision is always assumed to be zero in this case.")
      System.exit(1)
    }

    val input = Paths.get(args[0])
    val output = Paths.get(args[1])

    try {
      this.compile(input, output)
    } catch (ex: Throwable) {
      println("Failed to compile repository: ${ex.message}")
      ex.printStackTrace()
      System.exit(2)
    }
  }

  /**
   * Compiles the given JSON definition into a compressed repository definition.
   */
  fun compile(input: Path, output: Path) {
    logger.info("Compiling $input into $output")

    val repository = mapper.readValue<Repository>(input.toFile())
    RepositoryBuilder {
      displayName = repository.displayName
      repository.revision?.let { revision = it }

      repository.services.forEach { service ->
        logger.debug("Compiling service ${service.id} (\"${service.title}\")")

        withService(service.id, service.title) {
          category = service.category

          service.icon?.let { iconUrl ->
            withTemporaryFile {
              fetchIcon(iconUrl, it)
              icon = it
            }
          }

          service.ports.forEach { (protocol, number) ->
            number via protocol
          }
        }
      }

      writeTo(output)
    }
  }

  /**
   * Converts an icon URL into a real world path variable.
   */
  private fun fetchIcon(url: URL, target: Path) {
    logger.debug("Fetching icon from $url")

    if (url.protocol == "file") {
      logger.debug("Already local - Skipping download")
      convertImage(Paths.get(url.toURI()), target)
      return
    }

    withTemporaryFile { path ->
      logger.debug("Storing image $url in temporary file $path")

      val request = Request.Builder()
        .url(url)
        .build()

      this.client.newCall(request).execute()
        .takeIf(Response::isSuccessful)
        ?.body
        ?.bytes()
        ?.let { Files.write(path, it) }

      try {
        convertImage(path, target)
      } catch (ex: IllegalArgumentException) {
        throw IllegalArgumentException("Invalid image file: $url", ex)
      }
    }
  }

  /**
   * Converts and resizes candidate images to fit the UI.
   *
   * The output file will be encoded as a PNG with the dimensions of 32x32.
   */
  private fun convertImage(input: Path, output: Path) {
    logger.debug("Converting image $input to $output")

    val img = ImageIO.read(input.toFile())
      ?: throw IllegalArgumentException("Invalid input image format")

    val targetImg = if (img.width != ICON_SIZE || img.height != ICON_SIZE) {
      logger.debug("Image size mismatch - Resizing to ${ICON_SIZE}x${ICON_SIZE}")
      val outImg = BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_4BYTE_ABGR)

      val g = outImg.graphics
      g.drawImage(img, 0, 0, ICON_SIZE, ICON_SIZE, null)
      g.dispose()

      outImg
    } else {
      img
    }

    ImageIO.write(targetImg, "PNG", output.toFile())
  }

  private fun <R> withTemporaryFile(block: (Path) -> R): R {
    val tmp = Files.createTempFile("beacon_", ".tmp")
      ?: throw IllegalStateException("Failed to allocate temporary file")

    logger.debug("Allocated temporary file $tmp")
    try {
      return block(tmp)
    } finally {
      logger.debug("Freeing temporary file $tmp")
      Files.deleteIfExists(tmp)
    }
  }
}
