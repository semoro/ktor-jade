package me.semoro.ktor.jade


import de.neuland.jade4j.JadeConfiguration
import de.neuland.jade4j.template.ClasspathTemplateLoader
import org.jetbrains.ktor.application.Application
import org.jetbrains.ktor.application.call
import org.jetbrains.ktor.content.templating
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.http.HttpHeaders
import org.jetbrains.ktor.http.HttpMethod
import org.jetbrains.ktor.routing.get
import org.jetbrains.ktor.routing.routing
import org.jetbrains.ktor.testing.handleRequest
import org.jetbrains.ktor.tests.withTestApplication
import org.junit.Test
import kotlin.test.assert
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.shouldBe

class JadeTemplateTest {
    @Test
    fun testName() {
        withTestApplication {
            application.setUpTestTemplates()

            application.routing {
                val model = mapOf("id" to 1, "title" to "Hello, World!")

                get("/") {
                    call.respond(JadeContent("test.jade", model, "e"))
                }
            }

            handleRequest(HttpMethod.Get, "/").response.let { response ->
                assertNotNull(response.content)

                @Suppress("DEPRECATION")
                assert(response.content!!.lines()) {
                    shouldBe(listOf("<p>Hello, 1</p><h1>Hello, World!</h1>"))
                }
                assertEquals(ContentType.Text.Html, ContentType.Companion.parse(assertNotNull(response.headers[HttpHeaders.ContentType])))
                assertEquals("e", response.headers[HttpHeaders.ETag])
            }
        }
    }

    private fun Application.setUpTestTemplates() {

        templating(jadeTemplate {
            val config = JadeConfiguration()
            config.templateLoader = ClasspathTemplateLoader()
            config
        })
    }
}