var localEdmDocument = {
    id: "1ecabcaca5e51752184db6b072cd4a30",
    edmNodeType: "DOCUMENT",
    parentId: "AU616Sa7hz1onBRyPC8p",
    name: "impots_accuseReception",
    fileExtension: "pdf",
    fileContentType: "application/pdf",
    temporaryFileToken: null,
    fileDate: 1431006862000,
    nodePath: "/tmp/impots_accuseReception.pdf"
};

var remoteEdmDocument = {
    id: "1ecabcaca5e51752184db6b072cd4a31",
    edmNodeType: "DOCUMENT",
    parentId: "AU616Sa7hz1onBRyPC8p",
    name: "impots_accuseReception",
    fileExtension: "pdf",
    fileContentType: "application/pdf",
    temporaryFileToken: null,
    fileDate: 1431006862000,
    nodePath: "http://mydocs.lol/tmp/impots_accuseReception.pdf"
};

describe('DocumentSearchController', function() {

    // compatibility : http://www.w3schools.com/jsref/jsref_trim_string.asp
    if (typeof String.prototype.trim !== 'function') {
        String.prototype.trim = function() {
            return this.replace(/^\s+|\s+$/g, '');
        };
    }
    if (typeof(console) === 'undefined' || console === null) {
        console = {
            debug: function() {},
            log: function() {},
            warn: function() {},
            error: function() {}
        };
    }
    // --

    var $scope;
    beforeEach(module('edmApp'));
    beforeEach(inject(function($rootScope, $controller) {
        $scope = $rootScope.$new();
        $controller('DocumentSearchController', {
            $scope: $scope
        });
    }));

    it('should get the correct icon', function() {
        var iconName = $scope.getDocumentNodeIcon(localEdmDocument);
        expect(iconName).toBe('pdf');
    });

    it('should local file be accessible by link', function() {
        var documentPath = $scope.linkToDocument(localEdmDocument);
        expect(documentPath).toBe('/files?docId=1ecabcaca5e51752184db6b072cd4a30');
    });

    it('should remote document be accessible by his path', function() {
        var documentPath = $scope.linkToDocument(remoteEdmDocument);
        expect(documentPath).toBe('http://mydocs.lol/tmp/impots_accuseReception.pdf');
    });

    it('should conserve previous search when updating pattern', function() {
        $scope.searchedPattern = 'previous search';
        $scope.addWordAndSubmitSearch('new search element');
        expect($scope.searchedPattern).toBe('previous search AND new search element');
    });

    it('should add top word in empty search', function() {
        $scope.addWordAndSubmitSearch('keyword');
        expect($scope.searchedPattern).toBe('keyword');
    });

    it('should well format extension filter', function() {
        $scope.searchedPattern = "search pattern must not be empty";
        $scope.aggregations.fileExtension = [{
            key: "pdf",
            isChecked: true
        }, {
            key: "png",
            isChecked: true
        }, {
            key: "doc",
            isChecked: false
        }];
        var filter = $scope._getQueryFilters();
        console.debug("Filter : " + filter);
        expect(filter).toBe(' AND (fileExtension:pdf OR fileExtension:png)');
    });

    it('should well format date filter', function() {
        $scope.searchedPattern = "search pattern must not be empty";
        $scope.dateAggregationFilter = "(date:[2015-08-01 TO 2015-08-31])";
        var filter = $scope._getQueryFilters();
        expect(filter).toBe(' AND (date:[2015-08-01 TO 2015-08-31])');
    });

    it('should not include date filter on empty date', function() {
        $scope.searchedPattern = "search pattern must not be empty";
        var filter = $scope._getQueryFilters();
        expect(filter).toBe('');
    });

    it('should well format category filter', function() {
        $scope.searchedPattern = "search pattern must not be empty";
        $scope.categories = [{
            id: "c1",
            key: "category 1",
            isChecked: true
        }, {
            id: "c2",
            key: "category 2",
            isChecked: true
        }, {
            id: "c3",
            key: "category 3",
            isChecked: false
        }];
        var filter = $scope._getQueryFilters();
        console.debug("Filter : " + filter);
        expect(filter).toBe(' AND (categoryId:c1 OR categoryId:c2)');
    });

    it('should combine filters', function() {
        $scope.searchedPattern = "search pattern must not be empty";

        $scope.aggregations.fileExtension = [{
            key: "pdf",
            isChecked: true
        }, {
            key: "png",
            isChecked: true
        }, {
            key: "doc",
            isChecked: false
        }];

        $scope.dateAggregationFilter = "(date:[2015-08-01 TO 2015-08-31])";

        $scope.categories = [{
            id: "c1",
            key: "category 1",
            isChecked: true
        }, {
            id: "c2",
            key: "category 2",
            isChecked: true
        }, {
            id: "c3",
            key: "category 3",
            isChecked: false
        }];

        var filter = $scope._getQueryFilters();
        expect(filter).toBe(' AND (fileExtension:pdf OR fileExtension:png) AND (date:[2015-08-01 TO 2015-08-31]) AND (categoryId:c1 OR categoryId:c2)');
    });
});
