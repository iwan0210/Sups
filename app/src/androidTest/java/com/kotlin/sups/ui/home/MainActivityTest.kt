package com.kotlin.sups.ui.home

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.NoActivityResumedException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.kotlin.sups.R
import com.kotlin.sups.di.datastore
import com.kotlin.sups.helper.EspressoIdlingResource
import com.kotlin.sups.ui.login.LoginActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {
    @get:Rule
    val activity = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var context: Context

    @Before
    fun setUp(): Unit = runBlocking {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        context = ApplicationProvider.getApplicationContext()
        context.datastore.edit { pref ->
            pref[AUTH_KEY] = true
        }
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun logout(): Unit = runBlocking {
        Intents.init()
        onView(withId(R.id.action_logout)).perform(click())
        onView(withText(R.string.confirm)).inRoot(isDialog()).perform(click())
        intended(hasComponent(LoginActivity::class.java.name))
        onView(withId(R.id.ed_login_email)).check(matches(isDisplayed()))
        pressBack()
        try {
            pressBack()
            onView(withId(R.id.action_logout)).check(doesNotExist())
        } catch (e: NoActivityResumedException) {
            assertTrue("App is closed", true)
        }

    }

    private companion object {
        private val AUTH_KEY = booleanPreferencesKey("isLoggedIn")
    }
}