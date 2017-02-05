angular.module('ontrack.extension.neo4j', [
    'ot.service.core',
    'ot.service.form'
])
// Routing
    .config(function ($stateProvider) {
        $stateProvider.state('neo4j-export', {
            url: '/extension/neo4j/export',
            templateUrl: 'extension/neo4j/neo4j-export.tpl.html',
            controller: 'Neo4JExportActionCtrl'
        });
    })
    // Controller
    .controller('Neo4JExportActionCtrl', function ($http, $scope, ot, otFormService) {
        // View definition
        var view = ot.view();
        view.title = "Export to Neo4J";
        view.commands = [
            // Closing to home page
            ot.viewCloseCommand('/home')
        ];
        // Loads and displays the form
        ot.pageCall($http.get('extension/neo4j/export')).then(function (form) {
            $scope.exportForm = form;
            // Prepares the form for display
            $scope.exportData = otFormService.prepareForDisplay(form);
        });
    })
;