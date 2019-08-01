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
package tv.dotstart.beacon.util

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.RollingFileAppender
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy
import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.layout.PatternLayout
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.reflect.KClass

/**
 * Provides extension functions and properties which enhance logging within the application.
 *
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */

/**
 * Permits retrieving and altering of the root logger level.
 */
var rootLevel: Level
  get() = LogManager.getRootLogger().level
  set(value: Level) {
    Configurator.setRootLevel(value)
  }

/**
 * Retrieves the logger for a given type.
 *
 * Note that this field will be inlined when accessed (e.g. is equal to a direct call to
 * LogManager#getLogger()) and should thus be cached in a property or local variable if accessed
 * more frequently.
 */
inline val <T : Any> KClass<T>.logger: Logger
  get() = LogManager.getLogger(this.java)

/**
 * Configures the root logger to write all of its logs to a given storage location.
 */
fun configureLogStorage(target: Path) {
  Files.createDirectories(target)

  val ctx = LoggerContext.getContext(false)
  val root = ctx.rootLogger
  val cfg = ctx.configuration

  val layout = PatternLayout.newBuilder()
      .withPattern("[%d{HH:mm:ss}] [%25.25t] [%level]: %msg%n")
      .withCharset(StandardCharsets.UTF_8)
      .withAlwaysWriteExceptions(true)
      .withConfiguration(cfg)
      .build()

  val policy = OnStartupTriggeringPolicy.createPolicy(0)

  val strategy = DefaultRolloverStrategy.newBuilder()
      .withMax("10")
      .withMin("1")
      .withFileIndex("min")
      .withConfig(cfg)
      .build()

  val fileAppender = (RollingFileAppender.newBuilder<RollingFileAppenderBuilder>() as RollingFileAppender.Builder<RollingFileAppenderBuilder>).apply {
    name = "File"
    configuration = cfg
    setLayout(layout)

    withFileName(target.resolve("latest.log").toAbsolutePath().toString())
    withFilePattern(target.resolve("lantern.log").toAbsolutePath().toString() + ".%i")
    withAppend(true)
    withImmediateFlush(true)
    withPolicy(policy)
    withStrategy(strategy)
  }.build()

  fileAppender.start()

  cfg.addAppender(fileAppender)
  root.addAppender(fileAppender)
}

/**
 * Utility class which works around log4j's current API issues.
 *
 * See https://stackoverflow.com/questions/49601627/how-to-specify-recursive-generic-parameter-in-kotlin/50565929#50565929
 * for more information on the topic.
 */
private class RollingFileAppenderBuilder : RollingFileAppender.Builder<RollingFileAppenderBuilder>()
