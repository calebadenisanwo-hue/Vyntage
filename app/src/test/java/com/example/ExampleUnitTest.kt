package com.example

import com.example.data.model.AudioTrack
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun duration_formatting_isCorrect() {
    assertEquals("00:00", AudioTrack.formatDuration(0L))
    assertEquals("01:05", AudioTrack.formatDuration(65000L))
    assertEquals("03:45", AudioTrack.formatDuration(225000L))
  }
}

