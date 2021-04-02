package com.squareup.moshi

import okio.Buffer
import okio.Okio
import okio.buffer
import okio.source
import org.junit.Assert
import org.junit.Test

class StreamingTest {

  @Test fun escapedDoubleQuote() = testSpecialCharacter("""\"escaped\"""")
  @Test fun escapedSingleQuote() = testSpecialCharacter("""\'escaped\'""")
  @Test fun newLine1()           = testSpecialCharacter("""\n""")
  @Test fun newLine2()           = testSpecialCharacter("""\r\n""")
  @Test fun stringTerminals()    = testSpecialCharacter("""{}[]:, \n\t\r\f/\\;#=""")
  @Test fun backlash1()          = testSpecialCharacter("""\/""")
  @Test fun backlash2()          = testSpecialCharacter("""\\""")

  private fun testSpecialCharacter(value: String) {
    val input = """{"a":"$value"}"""
    val reader  = readerOf(input)
    val out = Buffer()
    val writer = JsonUtf8Writer.of(out)

    reader.beginObject()
    writer.beginObject()

    writer.name(reader.nextName())
    reader.streamValue(writer)

    reader.endObject()
    writer.endObject()

    writer.flush()

    val streamed = out.clone().readUtf8()

    JsonReader.of(out).readJsonValue()

    Assert.assertEquals(input, streamed)
  }

  @Test
  fun unescape() {
    val value = "a\\\"c\\\""
    val valueQuotedUnescaped = """"a"c"""""
    val input = """{"a":"$value"}"""
    val reader  = readerOf(input)
    val out = Buffer()

    reader.beginObject()
    reader.skipName()
    reader.streamDoubleQuotedStringUnescape(out)

    Assert.assertEquals(valueQuotedUnescaped, out.readUtf8())
  }

  private fun readerOf(value: String) = JsonUtf8Reader.of(value.byteInputStream().source().buffer())
}
