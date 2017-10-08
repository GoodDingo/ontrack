package net.nemerosa.ontrack.io

import net.nemerosa.ontrack.model.security.SecurityRole
import net.nemerosa.ontrack.model.security.SecurityService
import net.nemerosa.ontrack.model.support.EnvService
import net.nemerosa.ontrack.repository.support.AbstractJdbcRepository
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.sql.DataSource


@Service
@Transactional
class IOServiceImpl
@Autowired
constructor(
        private val securityService: SecurityService,
        private val envService: EnvService,
        dataSource: DataSource
) : IOService, AbstractJdbcRepository(dataSource) {

    override fun exportData(io: IOImportProperties, outputStream: OutputStream) {
        // Unique ID for this export
        val uuid = UUID.randomUUID().toString()
        // Creates a working directory
        val workingDir = envService.getWorkingDir("io", uuid)
        try {
            // Export in files
            export(workingDir)
            // Zips all files
            val bout = BufferedOutputStream(outputStream)
            try {
                ZipOutputStream(bout).use { zout ->
                    workingDir.listFiles()?.forEach { file ->
                        zout.putNextEntry(ZipEntry(file.name))
                        BufferedInputStream(FileInputStream(file)).use { inp ->
                            IOUtils.copy(inp, zout)
                        }
                    }
                }
            } finally {
                bout.flush()
            }
        } finally {
            FileUtils.forceDelete(workingDir)
        }
    }

    override fun export(workingDir: File) {
        // Checking rights
        if (securityService.currentAccount?.role != SecurityRole.ADMINISTRATOR) {
            throw AccessDeniedException("Export only allowed for administrators.")
        }
        /**
         * Global data
         */
        export(workingDir, "CONFIGURATIONS", "ID", "TYPE", "NAME", "CONTENT")
        export(workingDir, "SETTINGS", "CATEGORY", "NAME", "VALUE")
        export(workingDir, "PREDEFINED_PROMOTION_LEVELS", "ID", "ORDERNB", "NAME", "DESCRIPTION", "IMAGETYPE", "IMAGEBYTES");
        export(workingDir, "PREDEFINED_VALIDATION_STAMPS", "ID", "NAME", "DESCRIPTION", "IMAGETYPE", "IMAGEBYTES");
        export(workingDir, "STORAGE", "STORE", "NAME", "DATA");
    }

    private fun export(workingDir: File, table: String, vararg columns: String) {
        val query = "SELECT * FROM $table"

        PrintWriter(File(workingDir, table)).use {
            it.println(columns.joinToString(",", "\"", "\""))
            jdbcTemplate.query(
                    query,
                    { rs ->
                        val row: List<String?> = columns.map { column ->
                            rs.getObject(column).toCsv()
                        }
                        it.println(row.joinToString(","))
                    }
            )
        }
    }

    private fun Any?.toCsv(): String? =
            if (this == null) {
                null
            } else {
                when (this) {
                    is String -> "\"${this}\""
                    else -> toString()
                }
            }

}