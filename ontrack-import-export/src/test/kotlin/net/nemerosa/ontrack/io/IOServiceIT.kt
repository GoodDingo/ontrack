package net.nemerosa.ontrack.io

import net.nemerosa.ontrack.it.AbstractServiceTestSupport
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import kotlin.test.assertFailsWith

class IOServiceIT : AbstractServiceTestSupport() {

    @Autowired
    private lateinit var ioService: IOService

    @Test
    fun `Only for administrators`() {
        val wd = createTempDir()
        assertFailsWith(AccessDeniedException::class) {
            ioService.export(wd)
        }
    }

    @Test
    fun `Exporting`() {
        val wd = createTempDir()
        asAdmin().execute {
            ioService.export(wd)
        }
    }

}