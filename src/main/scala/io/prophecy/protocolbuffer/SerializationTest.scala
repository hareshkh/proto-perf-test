package io.prophecy.protocolbuffer

import play.api.libs.json._
import schema.{SubMessage, TopLevelMessage}

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream
import scala.util.Random

object SerializationTest {

  private def compress(data: Array[Byte]): Array[Byte] = {
    val outputStream = new ByteArrayOutputStream()
    val gzipOutputStream = new GZIPOutputStream(outputStream)
    gzipOutputStream.write(data)
    gzipOutputStream.close()
    outputStream.toByteArray
  }

  private def generateRandomString(length: Int): String = {
    val alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val random = new Random()
    val sb = new StringBuilder(length)

    for (_ <- 1 to length) {
      val randomIndex = random.nextInt(alphabet.length)
      sb.append(alphabet(randomIndex))
    }

    sb.toString()
  }

  def main(args: Array[String]): Unit = {

    implicit val sfmt: Format[SubMessage] = new Format[SubMessage] {
      override def reads(json: JsValue): JsResult[SubMessage] = ???

      override def writes(o: SubMessage): JsValue = JsObject(
        List(
          "str" -> JsString(o.str)
        )
      )
    }
    implicit val tfmt: Format[TopLevelMessage] = new Format[TopLevelMessage] {
      override def reads(json: JsValue): JsResult[TopLevelMessage] = ???

      override def writes(o: TopLevelMessage): JsValue = {
        JsObject(
          List(
            "stringField" -> JsString(o.stringField),
            "intField" -> JsNumber(o.intField),
            "longField" -> JsNumber(o.longField),
            "bigStringField" -> JsString(o.bigStringField),
            "mapField" -> Json.toJson(o.mapField)
          )
        )
      }
    }

    val sampleObject = TopLevelMessage(
      stringField = generateRandomString(length = 20),
      intField = Random.nextInt(),
      longField = Random.nextLong(),
      bigStringField = generateRandomString(length = 15000),
      mapField = (1 to 10)
        .map(_ =>
          generateRandomString(length = 20) -> SubMessage(str =
            generateRandomString(length = 30)
          )
        )
        .toMap
    )

    val json = Json.toJson(sampleObject).toString().getBytes()
    val compressedJson = compress(json)

    val proto = sampleObject.toByteArray
    val compressedProto = compress(proto)

    println(s"Original string = ${Json.toJson(sampleObject).toString()}")
    println(s"JSON size = ${json.length}")
    println(s"JSON size (compressed) = ${compressedJson.length}")
    println(s"Proto size = ${proto.length}")
    println(s"Proto size (compressed) = ${compressedProto.length}")
  }
}
