angular.module('ontrack.extension.neo4j', [
    'ot.service.core'
])
// Routing
    .config(function ($stateProvider) {
        $stateProvider.state('neo4j-export', {
            url: '/extension/neo4j/export',
            templateUrl: 'extension/neo4j/export.tpl.html',
            controller: 'Neo4JExportActionCtrl'
        });
    })
    // Controller
    .controller('Neo4JExportActionCtrl', function ($scope, ot) {
        // View definition
        var view = ot.view();
        view.title = "Export to Neo4J";
        view.commands = [
            // Closing to home page
            ot.viewCloseCommand('/home')
        ];
        // TODO Loads and displays the form
    })
;