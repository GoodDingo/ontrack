package net.nemerosa.ontrack.io

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/io")
class IOController(
        private val ioService: IOService
) {

    @GetMapping
    fun exportData(io: IOImportProperties, httpServletResponse: HttpServletResponse) {
        val output = httpServletResponse.outputStream
        try {
            httpServletResponse.contentType = "application/zip"
            httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"ontrack.zip\"")
            ioService.exportData(io, output)
        } finally {
            output.flush()
        }
    }

}