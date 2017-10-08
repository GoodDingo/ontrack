package net.nemerosa.ontrack.io

import java.io.File
import java.io.OutputStream

interface IOService {
    fun exportData(io: IOImportProperties, outputStream: OutputStream)
    fun export(workingDir: File)
}