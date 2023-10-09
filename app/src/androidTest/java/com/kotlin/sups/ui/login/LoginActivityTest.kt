package com.kotlin.sups.ui.login

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.NoActivityResumedException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.kotlin.sups.R
import com.kotlin.sups.data.remote.ApiConfig
import com.kotlin.sups.helper.EspressoIdlingResource
import com.kotlin.sups.ui.home.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest {

    private val mockWebServer = MockWebServer()

    @get:Rule
    val activity = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        mockWebServer.start(8080)
        ApiConfig.BASE_URL = "http://127.0.0.1:8080/"
    }

    @Test
    fun login_success(): Unit = runBlocking {
        val email = "gawekke@gmail.com"
        val password = "pekalongan10"
        val response = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"error\":false,\"message\":\"success\",\"loginResult\":{\"userId\":\"user-tm8zj0bE4o-9yXw6\",\"name\":\"gawekke\",\"token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLXRtOHpqMGJFNG8tOXlYdzYiLCJpYXQiOjE2OTU5MjE4ODh9.Mw48d_ajmvJ_8O7K0CSgSuQo2A6mEPg14SNtI3l7XR8\"}}")
        Intents.init()
        onView(
            allOf(
                isDescendantOfA(withId(R.id.ed_login_email)),
                withHint(R.string.email)
            )
        )
            .perform(typeText(email), closeSoftKeyboard())
        onView(
            allOf(
                isDescendantOfA(withId(R.id.ed_login_password)),
                withHint(R.string.password)
            )
        ).perform(typeText(password), closeSoftKeyboard())
        onView(withId(R.id.login_button)).perform(click())
        mockWebServer.enqueue(response)
        delay(10000)
        intended(hasComponent(MainActivity::class.java.name))
        onView(withId(R.id.action_logout)).check(matches(isDisplayed()))
        pressBack()
        try {
            pressBack()
            onView(withId(R.id.ed_login_email)).check(ViewAssertions.doesNotExist())
        } catch (e: NoActivityResumedException) {
            assertTrue("App is closed", true)
        }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }
}