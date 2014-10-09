'use strict';

angular.module('ice.search.controller', [])
    .controller('SearchController', function ($scope, $http, $cookieStore, $location, Entry, Search) {
        $scope.$on("RunSearch", function (event, filters) {
            $scope.searchResults = undefined;
            $scope.searchFilters = filters;
            runAdvancedSearch(filters);
        });

        var runAdvancedSearch = function (filters) {
            $scope.loadingSearchResults = true;

            var webSearch = {};
            if (filters.webSearch)
                webSearch.w = true;

            Search().runAdvancedSearch(webSearch, filters,
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

        var noFilters = (!$scope.searchFilters || Object.keys($scope.searchFilters).length === 0);

        // todo : $location.search('q', $scope.queryText);
        if (noFilters) {
            $scope.searchFilters = {entryTypes:[], parameters:{}, blastQuery:{}, queryString:""};
            var queryString = $location.search().q;
            if (!queryString === undefined) {
                $scope.searchFilters.queryString = queryString;
            }
        }

//        if (noFilters) {
        // no filters keyword search only
//            var queryString = $location.search().q;
//            if (queryString === undefined)
//                queryString = "";

        // todo :
//            if ($scope.queryText !== queryString)
//                $scope.queryText = queryString; // update input box
//
        // filters run advanced search
        $scope.searchFilters.parameters.start = 0;
        $scope.searchFilters.parameters.retrieveCount = 15;
        $scope.searchFilters.parameters.sortField = "RELEVANCE";
        runAdvancedSearch($scope.searchFilters);

        // TODO : sort ?
        $scope.maxSize = 5;  // number of clickable pages to show in pagination
        $scope.currentPage = 1;

        $scope.setSearchResultPage = function (pageNo) {
            $scope.searchFilters.parameters.start = (pageNo - 1) * 15;
            runAdvancedSearch($scope.searchFilters);
        };

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

        $scope.tooltipDetails = function (entry) {
            $scope.searchResultToolTip = undefined;
            var sessionId = $cookieStore.get("sessionId");

            Entry(sessionId).tooltip({partId:entry.id},
                function (result) {
                    $scope.searchResultToolTip = result;
                }, function (error) {
                    console.error(error);
                });
        }
    })
    .controller('SearchInputController', function ($scope, $rootScope, $http, $cookieStore, $location) {
        $scope.searchTypes = {all:true, strain:true, plasmid:true, part:true, arabidopsis:true};

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
            var searchQuery = {entryTypes:[], parameters:{}, blastQuery:{}};

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

            // biosafety
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
            // todo : or
//            var queryString = $location.search().q;
//            if (queryString === undefined)
//                queryString = "";
            return searchQuery;
        };

        $scope.search = function () {
            $scope.searchFilters = defineQuery();

            var searchUrl = "/search";
            if ($location.path().slice(0, searchUrl.length) != searchUrl) {
                $location.path(searchUrl, false);
            } else {
                $scope.searchFilters = defineQuery();
                $scope.searchFilters.parameters.start = 0;
                $scope.searchFilters.parameters.retrieveCount = 15;
                $scope.searchFilters.parameters.sortField = "RELEVANCE";
                $scope.$broadcast("RunSearch", $scope.searchFilters);
//                var noFilters = (!$scope.searchFilters || Object.keys($scope.searchFilters).length === 0);
//                console.log("no filters", noFilters);

                // todo : $location.search('q', $scope.queryText);

//                if (noFilters) {
                // no filters keyword search only
//                    var queryString = $location.search().q;
//                    if (queryString === undefined)
//                        queryString = "";

                // todo :
//            if ($scope.queryText !== queryString)
//                $scope.queryText = queryString; // update input box
//
//                    console.log("url query parameter", queryString);
//                    $scope.searchFilters = {q:queryString, sort:'relevance', asc:false, limit:15};
//                    runSearch($scope.searchFilters);
//                } else {
//                }
            }
        };

        $scope.isWebSearch = function () {
            return $scope.searchFilters.webSearch === true;
        };

        $scope.searchWebOfRegistries = function () {
            $scope.searchFilters = defineQuery();
            $scope.searchFilters.webSearch = true;

            var searchUrl = "/search";
            if ($location.path().slice(0, searchUrl.length) != searchUrl) {
                $location.path(searchUrl, false);
            } else {
//                $scope.searchFilters = defineQuery();
                $scope.searchFilters.parameters.start = 0;
                $scope.searchFilters.parameters.retrieveCount = 15;
                $scope.searchFilters.parameters.sortField = "RELEVANCE";
                $scope.$broadcast("RunSearch", $scope.searchFilters);
            }
        };

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
    });