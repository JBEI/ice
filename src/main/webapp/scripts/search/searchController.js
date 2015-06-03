'use strict';

angular.module('ice.search.controller', [])
    .controller('SearchController', function ($scope, $http, $cookieStore, $location, Entry, Search, EntryContextUtil,
                                              Selection, WebOfRegistries) {

        $scope.params = {'asc': false, 'sort': 'created', currentPage: 1};
        $scope.maxSize = 5;  // number of clickable pages to show in pagination

        $scope.$on("RunSearch", function (event, filters) {
            $scope.searchResults = undefined;
            $scope.searchFilters = filters;
            $scope.params.currentPage = 1;
            runAdvancedSearch(filters);
        });

        var runAdvancedSearch = function (filters) {
            $scope.loadingSearchResults = true;

            Search().runAdvancedSearch({webSearch: filters.webSearch}, filters,
                function (result) {
                    $scope.searchResults = result;
                    $scope.loadingSearchResults = false;
                },
                function (error) {
                    $scope.loadingSearchResults = false;
                    $scope.searchResults = undefined;
                    console.log(error);
                }
            );
        };

        $scope.searchResultPageChanged = function () {
            $scope.searchFilters.parameters.start = ($scope.params.currentPage - 1) * 15;
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
            $scope.params.currentPage = (Math.floor(context.offset / 15)) + 1;
            $scope.searchResultPageChanged();
        } else {
            $scope.searchFilters.parameters.start = 0;
            $scope.searchFilters.parameters.retrieveCount = 15;
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

        $scope.searchResultPopupTemplate = "/scripts/search/tooltip.html";

        $scope.tooltipDetails = function (entry) {
            $scope.searchResultToolTip = undefined;
            var sessionId = $cookieStore.get("sessionId");

            Entry(sessionId).tooltip({partId: entry.id},
                function (result) {
                    $scope.searchResultToolTip = result;
                }, function (error) {
                    console.error(error);
                });
        };

        $scope.remoteTooltipDetails = function (result) {
            $scope.searchResultToolTip = undefined;
            WebOfRegistries().getToolTip({partnerId: result.partner.id, entryId: result.entryInfo.id},
                function (result) {
                    $scope.searchResultToolTip = result;
                }, function (error) {
                    console.error(error);
                });
        };

        $scope.goToEntryDetails = function (entry, index) {
            if (!$scope.searchFilters.parameters) {
                $scope.searchFilters.parameters = {start: index}
            }

            var offset = (($scope.params.currentPage - 1) * 15) + index;
            EntryContextUtil.setContextCallback(function (offset, callback) {
                $scope.searchFilters.parameters.start = offset;
                $scope.searchFilters.parameters.retrieveCount = 1;

                Search().runAdvancedSearch({webSearch: $scope.searchFilters.webSearch}, $scope.searchFilters,
                    function (result) {
                        callback(result.results[0].entryInfo.id);
                    }
                );
            }, $scope.searchResults.resultCount, offset, "/search", $scope.searchResults.sortField);

            $location.path("/entry/" + entry.id);
        };

        //
        // select result entry
        //
        $scope.selectSearchResult = function (entry) {
            Selection.selectEntry(entry);
        };

        $scope.searchEntrySelected = function (entry) {
            return Selection.searchEntrySelected(entry);
        }
    })
    .controller('SearchInputController', function ($scope, $rootScope, $http, $cookieStore, $location, Search) {
        $scope.searchTypes = {all: true, strain: true, plasmid: true, part: true, arabidopsis: true};

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
                parameters: {start: 0, retrieveCount: 15, sortField: "RELEVANCE"},
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
                // triggers search controller which uses searchfilters to perform search
                $location.path(searchUrl, false);
            } else {
                $scope.$broadcast("RunSearch", $scope.searchFilters);
            }
        };

        $scope.isWebSearch = function () {
            return $scope.searchFilters.webSearch === true;
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

            return false;
        };

        //
        // resets the search filters to the defaults setting
        //
        $scope.reset = function () {
            $scope.sequenceText = "";
            $scope.queryText = "";
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

            Search().runAdvancedSearch({webSearch: $scope.searchFilters.webSearch}, $scope.searchFilters,
                function (result) {
                    $scope.searchResults = result;
                    $scope.loadingSearchResults = false;
                },
                function (error) {
                    $scope.loadingSearchResults = false;
                    $scope.searchResults = undefined;
                    console.log(error);
                }
            );
        };
    });