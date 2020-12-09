/*
 * Copyright 2019 Johannes Donath <johannesd@torchmind.com>
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
package tv.dotstart.beacon.ui.util

import com.jfoenix.controls.JFXAlert
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialogLayout
import com.jfoenix.controls.JFXTextArea
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import tv.dotstart.beacon.ui.BeaconUiMetadata
import java.io.PrintWriter
import java.io.StringWriter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * @author [Johannes Donath](mailto:johannesd@torchmind.com)
 */

/**
 * Displays a standard error dialog with the given title and description.
 */
fun dialog(title: String, description: String) {
  val alert = JFXAlert<Any>()
  alert.title = title

  val closeButton = JFXButton(Localization("action.close"))
  closeButton.onAction = EventHandler { alert.close() }

  val layout = JFXDialogLayout()
  layout.setHeading(Label(title))
  layout.setBody(Label(description))
  layout.setActions(closeButton)
  alert.setContent(layout)

  alert.showAndWait()
}

fun detailedErrorDialog(title: String, description: String, ex: Throwable) {
  val alert = JFXAlert<Any>()
  alert.title = title

  val reportButton = JFXButton(Localization("action.report"))
  reportButton.isDisable = !ErrorReporter.available
  reportButton.onAction = EventHandler {
    reportButton.isDisable = true
    ErrorReporter(ex)
  }

  val closeButton = JFXButton(Localization("action.close"))
  closeButton.onAction = EventHandler { alert.close() }

  val body = VBox(
      Label(description),
      JFXTextArea(errorReport(ex))
  )
  body.spacing = 5.0

  val layout = JFXDialogLayout()
  layout.setHeading(Label(title))
  layout.setBody(body)
  layout.setActions(reportButton, closeButton)
  alert.setContent(layout)

  alert.showAndWait()
}

fun errorReport(ex: Throwable) = buildString {
  append("Beacon Error Report\r\n")
  append("===================\r\n")
  append("Version: ${BeaconUiMetadata.version}\r\n")
  append("Date: ${DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())}\r\n")
  append("""Java Version: ${System.getProperty("java.version")}""").append("\r\n")
  append("""Java Architecture: ${System.getProperty("os.arch")}""").append("\r\n")
  append("""Operating System: ${System.getProperty("os.name")} (${
    System.getProperty(
        "os.version")
  })""").append("\r\n")
  append("\r\n")
  append("Stack Trace:\r\n")
  append("------------\r\n")

  StringWriter().use { str ->
    PrintWriter(str).use(ex::printStackTrace)
    append(str.toString())
  }
}
