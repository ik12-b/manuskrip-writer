package com.example

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.DocumentEntity
import com.example.data.model.FolioEntity
import com.example.ui.components.ManuscriptHeader
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleDoc = DocumentEntity(
      id = "test_doc",
      title = "Naskah Uji Screenshot",
      repository = "Test Repository",
      datePeriod = "1400 H",
      language = "Arabic (العربية)",
      scriptType = "Naskh",
      totalFolios = 1
    )
    val sampleFolio = FolioEntity(
      id = "test_folio",
      documentId = "test_doc",
      folioNumber = "1r",
      title = "Folio 1r"
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        ManuscriptHeader(
          document = sampleDoc,
          folio = sampleFolio,
          totalLines = 8,
          currentLineNumber = 1,
          pendingSyncCount = 0,
          isSyncing = false,
          onOpenDocPicker = {},
          onOpenExport = {},
          onExportPdf = {},
          onOpenAddDoc = {},
          onSyncNow = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

