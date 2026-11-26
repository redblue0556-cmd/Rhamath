package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.RhajeshMatchTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Rhajesh Match", appName)
  }

  @Test
  fun `splash to lobby to quiz and lab flows render cleanly`() {
    composeTestRule.setContent {
      RhajeshMatchTheme {
        RhajeshMatchApp()
      }
    }

    composeTestRule.onNodeWithText("RHAJESH MATCH").assertIsDisplayed().performClick()
    composeTestRule.mainClock.advanceTimeBy(500)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("start_learning_button").performScrollTo().assertIsDisplayed().performClick()

    composeTestRule.onNodeWithText("PILIH BAB!").assertIsDisplayed()
    composeTestRule.onNodeWithTag("chapter_card_0").assertIsDisplayed().performClick()

    composeTestRule.onNodeWithTag("option_0").assertIsDisplayed().performClick()
    composeTestRule.mainClock.advanceTimeBy(400)
    composeTestRule.onNodeWithTag("feedback_banner").performScrollTo().assertIsDisplayed()

    composeTestRule.onNodeWithTag("quiz_home_button").performClick()
    composeTestRule.onNodeWithTag("open_lab_button").performScrollTo().assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("lab_back_button").assertIsDisplayed()
  }
}
