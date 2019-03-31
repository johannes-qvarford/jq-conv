package com.johannesqvarford

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.html.*
import kotlinx.html.*
import io.ktor.http.content.PartData
import io.ktor.http.content.streamProvider
import org.apache.commons.codec.binary.Hex
import java.nio.charset.Charset

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {

    routing {
        get("/") {
            call.respondHtml {
                head {
                    meta { charset="UTF-8" }
                }
                body {
                    h1 {
                        +"Johannes Qvarford Encoding Converter"
                    }
                    p {
                        +"Converts the beginning of a file from multiple different encoding into UTF-8."
                        +" Useful if you want to figure out the original encoding of a file."
                    }
                    form(encType = FormEncType.multipartFormData, method = FormMethod.post) {
                        acceptCharset = "UTF-8"
                        p {
                            label { +"File to convert: " }
                            fileInput { name="file" }
                        }
                        button (type=ButtonType.submit) {
                            + "Submit"
                        }
                    }
                }
            }
        }

        post("/") {
            val multipart = call.receiveMultipart()
            val filePart = multipart.readPart() as PartData.FileItem

            data class EncodingItem(val title: String, val content: String)

            val beginning = filePart.streamProvider().use { input -> input.readNBytes(20) }

            val charsetItems = sequenceOf(
                EncodingItem(
                    title = "Bytes",
                    content = Hex.encodeHexString(beginning)
                        .chunked(2)
                        .joinToString(" "))
                ) +
                Charset.availableCharsets()
                    .map { cs -> EncodingItem(cs.key, String(beginning, cs.value)) }

            call.respondHtml {
                head {
                    meta { charset = "UTF-8" }
                }
                body {
                    h1 {
                        +"Johannes Qvarford Converter"
                    }
                    p { +"Result for file ${filePart.originalFileName}"}
                    for (cs in charsetItems) {
                        h2 { +cs.title }
                        p { +cs.content }
                    }
                }
            }
        }
    }
}
