package net.nemerosa.ontrack.extension.neo4j;

import net.nemerosa.ontrack.common.Document;
import net.nemerosa.ontrack.extension.neo4j.export.Neo4JExportInput;
import net.nemerosa.ontrack.extension.neo4j.export.Neo4JExportOutput;
import net.nemerosa.ontrack.extension.neo4j.export.Neo4JExportService;
import net.nemerosa.ontrack.model.form.Form;
import net.nemerosa.ontrack.model.form.YesNo;
import net.nemerosa.ontrack.ui.controller.AbstractResourceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Callable;

@RestController
@RequestMapping("/extension/neo4j/export")
public class Neo4JExportController extends AbstractResourceController {

    private final Neo4JExportService exportService;

    @Autowired
    public Neo4JExportController(Neo4JExportService exportService) {
        this.exportService = exportService;
    }

    /**
     * Export form
     */
    @GetMapping
    public Form getExportForm() {
        Neo4JExportInput defaults = new Neo4JExportInput();
        return Form.create()
                .with(
                        YesNo.of("exportACL")
                                .label("Exporting ACL data")
                                .value(defaults.isExportACL())
                )
                ;
    }

    /**
     * Launching the export in async mode
     */
    @PostMapping
    public Callable<Neo4JExportOutput> launchExport(@RequestBody Neo4JExportInput input) {
        // Launching export
        return () -> exportService.export(input);
    }

    /**
     * Download
     */
    @GetMapping("{uuid}")
    public void download(@PathVariable String uuid, HttpServletResponse response) throws IOException {
        Document document = exportService.download(uuid);
        response.setContentType(document.getType());
        byte[] bytes = document.getContent();
        response.setContentLength(bytes.length);
        response.setHeader("Content-Disposition", "attachment; filename=neo4j.zip");
        response.getOutputStream().write(bytes);
    }

}
