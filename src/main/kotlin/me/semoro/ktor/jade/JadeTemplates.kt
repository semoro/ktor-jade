package me.semoro.ktor.jade


import de.neuland.jade4j.Jade4J
import de.neuland.jade4j.JadeConfiguration
import de.neuland.jade4j.template.JadeTemplate
import org.jetbrains.ktor.content.*
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.http.charset
import org.jetbrains.ktor.util.Attributes
import org.jetbrains.ktor.util.ValuesMap
import java.io.OutputStream
import kotlin.reflect.KClass


class JadeTemplateResource internal constructor(val configuration: JadeConfiguration, val template: JadeTemplate, val model: Map<String, Any>, val etag: String) : FinalContent.StreamConsumer(), Resource {
    override val contentType: ContentType
        get() = when (configuration.mode) {Jade4J.Mode.HTML -> ContentType.Text.Html
            Jade4J.Mode.XML -> ContentType.Text.Xml
            Jade4J.Mode.XHTML -> ContentType("text", "xhtml")
        }
    override val attributes = Attributes()

    override fun stream(out: OutputStream) {
        with(out.bufferedWriter(contentType.charset() ?: Charsets.UTF_8)) {
            configuration.renderTemplate(template, model, this)
            flush()
        }
    }

    override val versions: List<Version>
        get() = listOf(EntityTagVersion(etag))

    override val expires = null

    override val cacheControl = null

    override val contentLength = null

    override val headers: ValuesMap
        get() = super.headers
}

class JadeContent(val templateName: String, val model: Map<String, Any>, val etag: String)


fun jadeTemplate(block: () -> JadeConfiguration): TemplateEngine<JadeContent, JadeTemplateResource> =
        JadeTemplateEngine(block())

private class JadeTemplateEngine(val configuration: JadeConfiguration) : TemplateEngine<JadeContent, JadeTemplateResource> {

    override val contentClass: KClass<JadeContent>
        get() = JadeContent::class

    override fun process(content: JadeContent) = JadeTemplateResource(configuration, configuration.getTemplate(content.templateName), content.model, content.etag)
}