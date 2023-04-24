import org.gradle.api.internal.file.archive.compression.URIBuilder
import org.gradle.api.internal.resources.DefaultResourceHandler
import org.gradle.api.internal.resources.ResourceResolver
import org.gradle.api.resources.ReadableResource
import org.gradle.api.resources.ResourceHandler
import org.gradle.api.resources.internal.ReadableResourceInternal
import java.io.InputStream
import java.net.URI
import org.tukaani.xz.SeekableFileInputStream
import org.tukaani.xz.SeekableXZInputStream

// Solution copied from https://github.com/gradle/gradle/issues/15065#issuecomment-1413699551

class XZArchiver(private val resource: ReadableResourceInternal) : ReadableResource {
    private val uri: URI = URIBuilder(resource.uri).schemePrefix("xz:").build()
    override fun getDisplayName() = resource.displayName
    override fun getURI() = uri
    override fun getBaseName() = displayName
    override fun read(): InputStream {
        val file = resource.backingFile
        val seekable = SeekableFileInputStream(file)
        return SeekableXZInputStream(seekable)
    }
    override fun toString() = displayName
}

fun ResourceHandler.xz(path: Any): XZArchiver {
    val resourceResolverField = DefaultResourceHandler::class.java.getDeclaredField("resourceResolver")
    resourceResolverField.isAccessible = true
    val resourceResolver = resourceResolverField.get(this) as ResourceResolver
    val resource = resourceResolver.resolveResource(path)
    resourceResolverField.isAccessible = false
    return XZArchiver(resource)
}