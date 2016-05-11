'use strict';

angular.module('ice.search.controller', [])
    .controller('SearchController', function ($scope, $http, $cookieStore, $location, EntryContextUtil,
                                              Selection, Util, localStorageService) {

        $scope.params = {asc: false, sort: 'RELEVANCE', currentPage: 1, hstep: [15, 30, 50, 100], limit: 30};
        $scope.maxSize = 5;  // number of clickable pages to show in pagination
        var query = {entryTypes: ['STRAIN', 'PLASMID', 'PART', 'ARABIDOPSIS'], queryString: undefined};

        $scope.$on("RunSearch", function (event, filters) {
            query = filters;
            $scope.searchResults = undefined;
            $scope.searchFilters = filters;
            $scope.params.currentPage = 1;
            runAdvancedSearch(filters);
        });

        var runAdvancedSearch = function (filters) {
            $scope.loadingSearchResults = true;

            Util.post("rest/search", filters, function (result) {
                $scope.searchResults = result;
                $scope.loadingSearchResults = false;
            }, {webSearch: filters.webSearch}, function () {
                $scope.loadingSearchResults = false;
                $scope.searchResults = undefined;
            });
        };

        $scope.selectAllClass = function () {
            if (Selection.allSelected()) // || $scope.folder.entries.length === Selection.getSelectedEntries().length)
                return 'fa-check-square-o';

            if (Selection.hasSelection())
                return 'fa-minus-square';
            return 'fa-square-o';
        };

        $scope.selectAllSearchResults = function () {
            if (Selection.allSelected()) {
                Selection.setTypeSelection('none');
                Selection.setSearch(undefined);
            }
            else {
                Selection.setTypeSelection('all');
                Selection.setSearch(query);
                console.log(query);
            }
        };

        $scope.searchResultPageChanged = function () {
            $scope.searchFilters.parameters.start = ($scope.params.currentPage - 1) * $scope.params.limit;
            runAdvancedSearch($scope.searchFilters);
        };

        var noFilters = (!$scope.searchFilters || Object.keys($scope.searchFilters).length === 0);

        if (noFilters) {
            $scope.searchFilters = {entryTypes: [], parameters: {}, blastQuery: {}, queryString: ""};
            var queryString = $location.search().q;

            if (queryString !== undefined) {
                $scope.searchFilters.queryString = queryString;
            }
        }

        var context = EntryContextUtil.getContext();
        if (context) {
            $scope.params.currentPage = (Math.floor(context.offset / $scope.params.limit)) + 1;
            $scope.searchResultPageChanged();
        } else {
            $scope.searchFilters.parameters.start = 0;
            $scope.searchFilters.parameters.retrieveCount = $scope.params.limit;
            $scope.searchFilters.parameters.sortField = "RELEVANCE";
            $scope.params.currentPage = 1;
            runAdvancedSearch($scope.searchFilters);
        }

        $scope.getType = function (relScore) {
            if (relScore === undefined)
                return 'info';

            if (relScore >= 70)
                return 'success';
            if (relScore >= 30 && relScore < 70)
                return 'warning';
            if (relScore < 30)
                return 'danger';
            return 'info';
        };

        $scope.searchResultPopupTemplate = "scripts/folder/template.html";

        $scope.tooltipDetails = function (entry) {
            $scope.currentTooltip = undefined;
            Util.get("rest/parts/" + entry.id + "/tooltip", function (result) {
                $scope.currentTooltip = result;
            });
        };

        $scope.remoteTooltipDetails = function (result) {
            $scope.currentTooltip = undefined;
            Util.get("rest/partners/" + result.partner.id + "/entries/" + result.entryInfo.id + "/tooltip",
                function (result) {
                    $scope.currentTooltip = result;
                });
        };

        $scope.goToEntryDetails = function (entry, index) {
            if (!$scope.searchFilters.parameters) {
                $scope.searchFilters.parameters = {start: index}
            }

            var offset = (($scope.params.currentPage - 1) * $scope.params.limit) + index;
            EntryContextUtil.setContextCallback(function (offset, callback) {
                $scope.searchFilters.parameters.start = offset;
                $scope.searchFilters.parameters.retrieveCount = 1;
                Util.post("rest/search", $scope.searchFilters,
                    function (result) {
                        callback(result.results[0].entryInfo.id);
                    }, {webSearch: $scope.searchFilters.webSearch}
                );
            }, $scope.searchResults.resultCount, offset, "/search", $scope.searchResults.sortField);

            $location.path("entry/" + entry.id);
        };

        //
        // select result entry
        //
        $scope.selectSearchResult = function (entry) {
            Selection.selectEntry(entry);
        };

        $scope.searchEntrySelected = function (entry) {
            if (Selection.isSelected(entry))
                return true;

            return Selection.searchEntrySelected(entry);
        };

        $scope.hStepChanged = function () {
            $scope.params.currentPage = 1;
            //$scope.searchResultPageChanged();
            $scope.searchFilters.parameters.retrieveCount = $scope.params.limit;
            $scope.searchFilters.parameters.start = 0;

            //console.log($scope.searchFilters);
            //console.log($scope.params);
            //var offset = (($scope.params.currentPage - 1) * $scope.params.limit) + index;
            //EntryContextUtil.setContextCallback(function (offset, callback) {
            //    $scope.searchFilters.parameters.start = offset;
            //    $scope.searchFilters.parameters.retrieveCount = 1;

            runAdvancedSearch($scope.searchFilters);
        };

        $scope.resultsHeaders = {
            relevance: {field: "relevance", display: "Relevance", selected: true},
            hasSample: {field: "hasSample", display: "Has Sample", selected: true},
            hasSequence: {field: "hasSequence", display: "Has Sequence", selected: true},
            //alias: {field: "alias", display: "Alias"},
            created: {field: "creationTime", display: "Created", selected: true}
        };

        var storedFields = localStorageService.get('searchResultsHeaderFields');
        if (!storedFields) {
            // set default headers
            var searchResultsHeaderFields = [];
            for (var key in $scope.resultsHeaders) {
                if (!$scope.resultsHeaders.hasOwnProperty(key))
                    continue;

                var header = $scope.resultsHeaders[key];
                if (header.selected) {
                    searchResultsHeaderFields.push(header.field);
                }
            }

            // and store
            localStorageService.set('searchResultsHeaderFields', searchResultsHeaderFields);
        } else {
            console.log($scope.resultsHeaders);
            // set user selected
            for (var key in $scope.resultsHeaders) {
                if (!$scope.resultsHeaders.hasOwnProperty(key))
                    continue;

                var header = $scope.resultsHeaders[key];
                header.selected = (storedFields.indexOf(header.field) != -1);
            }
        }

        $scope.selectedHeaderField = function (header, $event) {
            if ($event) {
                $event.preventDefault();
                $event.stopPropagation();
            }
            header.selected = !header.selected;
            var storedFields = localStorageService.get('searchResultsHeaderFields');

            if (header.selected) {
                // selected by user, add to stored list
                storedFields.push(header.field);
                localStorageService.set('searchResultsHeaderFields', storedFields);
            } else {
                // not selected by user, remove from stored list
                var i = storedFields.indexOf(header.field);
                if (i != -1) {
                    storedFields.splice(i, 1);
                    localStorageService.set('searchResultsHeaderFields', storedFields);
                }
            }
        };
    })
    .controller('SearchInputController', function ($scope, $rootScope, $http, $cookieStore, $location) {
        $scope.searchTypes = {all: true, strain: true, plasmid: true, part: true, arabidopsis: true};
        $scope.fieldFilters = [];

        $scope.addFieldFilter = function () {
            $scope.fieldFilters.push({field: "", filter: ""});
        };

        $scope.removeFieldFilter = function (index) {
            $scope.fieldFilters.splice(1, index);
        };

        $scope.check = function (selection) {
            var allTrue = true;
            for (var type in $scope.searchTypes) {
                if ($scope.searchTypes.hasOwnProperty(type) && type !== 'all') {
                    if (selection === 'all')
                        $scope.searchTypes[type] = $scope.searchTypes.all;
                    allTrue = (allTrue && $scope.searchTypes[type] === true);
                }
            }
            $scope.searchTypes.all = allTrue;
        };

        var defineQuery = function () {
            var searchQuery = {
                entryTypes: [],
                parameters: {start: 0, retrieveCount: 30, sortField: "RELEVANCE"},
                blastQuery: {}
            };

            // check search types  : {all: false, strain: true, plasmid: false, part: true, arabidopsis: true}
            for (var type in $scope.searchTypes) {
                if ($scope.searchTypes.hasOwnProperty(type) && type !== 'all') {
                    if ($scope.searchTypes[type]) {
                        searchQuery.entryTypes.push(type.toUpperCase());
                    }
                }
            }

            // check blast search type
            if ($scope.blastSearchType) {
                searchQuery.blastQuery.blastProgram = $scope.blastSearchType;
            }

            // check "has ..."
            if ($scope.hasAttachment)
                searchQuery.parameters.hasAttachment = $scope.hasAttachment;

            if ($scope.hasSample)
                searchQuery.parameters.hasSample = $scope.hasSample;

            if ($scope.hasSequence)
                searchQuery.parameters.hasSequence = $scope.hasSequence;

            // bio safety
            if ($scope.bioSafetyLevelOption) {
                searchQuery.bioSafetyOption = $scope.bioSafetyLevelOption == "1" ? "LEVEL_ONE" : "LEVEL_TWO";
            }
            // todo include above with fieldFilters
            searchQuery.fieldFilters = $scope.fieldFilters;

            //sequence
            if ($scope.sequenceText) {
                searchQuery.blastQuery.sequence = $scope.sequenceText;
                if (!searchQuery.blastQuery.blastProgram)
                    searchQuery.blastQuery.blastProgram = "BLAST_N";
            }

            searchQuery.queryString = $scope.queryText;
            return searchQuery;
        };

        $scope.search = function (isWebSearch) {
            $scope.searchFilters = defineQuery();
            $scope.searchFilters.webSearch = isWebSearch;

            var searchUrl = "/search";
            if ($location.path().slice(0, searchUrl.length) != searchUrl) {
                // triggers search controller which uses search filters to perform search
                $location.path(searchUrl, false);
            } else {
                $scope.$broadcast("RunSearch", $scope.searchFilters);
            }
            $scope.advancedMenu.isOpen = false;
        };

        $scope.isWebSearch = function () {
            return $scope.searchFilters.webSearch === true;
        };

        $scope.advancedMenu = {
            isOpen: false
        };

        $scope.toggleAdvancedMenuDropdown = function ($event) {
            $event.preventDefault();
            $event.stopPropagation();
            $scope.advancedMenu.isOpen = !$scope.advancedMenu.isOpen;
        };

        $scope.canReset = function () {
            if ($scope.queryText || $scope.sequenceText || $scope.hasSample || $scope.hasSequence || $scope.hasAttachment)
                return true;

            if ($scope.blastSearchType || $scope.bioSafetyLevelOption)
                return true;

            for (var searchType in $scope.searchTypes) {
                if ($scope.searchTypes.hasOwnProperty(searchType)) {
                    if ($scope.searchTypes[searchType] != true)
                        return true;
                }
            }

            return $scope.fieldFilters.length;
        };

        //
        // resets the search filters to the defaults setting
        //
        $scope.reset = function () {
            $scope.sequenceText = "";
            $scope.queryText = "";
            $scope.fieldFilters = [];
            $location.url($location.path());
            $scope.blastSearchType = "";
            $scope.bioSafetyLevelOption = "";
            $scope.hasSample = false;
            $scope.hasSequence = false;
            $scope.hasAttachment = false;
            for (var searchType in $scope.searchTypes) {
                if ($scope.searchTypes.hasOwnProperty(searchType)) {
                    $scope.searchTypes[searchType] = true;
                }
            }
        };

        $scope.sortResults = function (sortType) {
            console.log("sort", sortType);
            sortType = sortType.toUpperCase();

            if (!$scope.searchFilters.parameters) {
                $scope.searchFilters.parameters = {sortAscending: false};
            } else {
                if (sortType === $scope.searchFilters.parameters.sortField) {
                    $scope.searchFilters.parameters.sortAscending = !$scope.searchFilters.parameters.sortAscending;
                } else
                    $scope.searchFilters.parameters.sortAscending = false;
            }

            $scope.searchFilters.parameters.sortField = sortType;
            $scope.searchFilters.parameters.start = 0;
            $scope.loadingSearchResults = true;
            $scope.$broadcast("RunSearch", $scope.searchFilters);
        };
    });