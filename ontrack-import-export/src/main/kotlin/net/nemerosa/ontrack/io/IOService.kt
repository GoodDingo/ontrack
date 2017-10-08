package net.nemerosa.ontrack.io

import java.io.OutputStream

interface IOService {
    // TODO Completable future
    fun exportData(io: IOImportProperties, outputStream: OutputStream)
}