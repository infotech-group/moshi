package com.squareup.moshi

import okio.Buffer
import okio.Okio
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
    val reader  = JsonUtf8Reader.of(input.byteInputStream().run(Okio::source).run(Okio::buffer))
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
}
