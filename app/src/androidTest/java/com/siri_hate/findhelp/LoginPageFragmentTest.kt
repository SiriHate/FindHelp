package com.siri_hate.findhelp

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.siri_hate.findhelp.ui.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginPageFragmentTest {

    @Rule
    @JvmField
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testManualLogin() {
        val email = "user@mail.ru"
        val password = "admin200"

        // Type in email and password
        onView(withId(R.id.login_fragment_login_input)).perform(typeText(email))
        onView(withId(R.id.login_fragment_password_input)).perform(typeText(password))

        // Close the keyboard
        closeSoftKeyboard()

        // Click the login button
        onView(withId(R.id.login_fragment_login_button)).perform(click())

        // Check if the correct fragment is opened
        onView(withId(R.id.userPageFragment)).check(matches(isDisplayed()))
    }

    @Test
    fun testAutoLogin() {

        // Check if the correct fragment is opened
        onView(withId(R.id.moderatorPageFragment)).check(matches(isDisplayed()))

    }

}