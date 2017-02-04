package net.nemerosa.ontrack.extension.neo4j;

import net.nemerosa.ontrack.extension.neo4j.export.Neo4JExportInput;
import net.nemerosa.ontrack.extension.neo4j.export.Neo4JExportOutput;
import net.nemerosa.ontrack.extension.neo4j.export.Neo4JExportService;
import net.nemerosa.ontrack.model.form.Form;
import net.nemerosa.ontrack.model.form.YesNo;
import net.nemerosa.ontrack.ui.controller.AbstractResourceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        // FIXME Async export
        // Launching export
        return () -> exportService.export(input);
    }

}
