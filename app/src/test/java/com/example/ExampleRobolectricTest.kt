package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.data.SupabaseConfig
import io.github.jan.supabase.gotrue.auth
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MOVIE DB", appName)
  }

  @Test
  fun `supabase client initializes successfully under android runtime`() {
    val client = SupabaseConfig.client
    org.junit.Assert.assertNotNull("Supabase client must not be null", client)
    org.junit.Assert.assertNotNull("Supabase Auth plugin must be present", SupabaseConfig.auth)
  }
}
