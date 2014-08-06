'use strict';

angular.module('ice.search.controller', [])
    .controller('SearchController', function ($scope, $http, $cookieStore, $location) {
        console.log("SearchController", $scope.searchFilters);
        var sessionId = $cookieStore.get("sessionId");
        var queryString = $location.search().q;
        $scope.queryString = queryString;

        // param defaults
        if (!$scope.loadingPage) {
            $scope.searchFilters.q = queryString;
            $scope.searchFilters.sort = 'relevance';
            $scope.searchFilters.asc = false;
            $scope.searchFilters.limit = 15;
            $scope.searchFilters.t = ['strain', 'plasmid', 'arabidopsis', 'part'];
            $scope.searchFilters.b = "BLAST_N";
            $scope.runUserSearch();
        }

        $scope.setSearchResultPage = function (pageNo) {
            $scope.loadingPage = true;
            $scope.searchFilters.offset = (pageNo - 1) * 15;
            $scope.runUserSearch();
        };

        // TODO : sort
        $scope.maxSize = 5;  // number of clickable pages to show in pagination
        $scope.currentPage = 1;

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
    })
    .controller('SearchInputController', function ($scope, $rootScope, $http, $cookieStore, $location, Search) {
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

        $scope.search = function (isAdvancedSearch) {
            $scope.searchResults = undefined;
            if (isAdvancedSearch) {
                $scope.loadingSearchResults = true;
                $location.path('/search');
                var search = Search();
                var searchQuery = {};
                searchQuery.queryString = $scope.queryText;
                var blastType = $scope.blastSearchType === undefined ? "BLAST_N" : $scope.blastSearchType.toUpperCase();
                searchQuery.blastQuery = {blastProgram:blastType, sequence:$scope.sequenceText};
                searchQuery.entryTypes = [];
                for (var searchType in $scope.searchTypes) {
                    if ($scope.searchTypes.hasOwnProperty(searchType) && searchType !== 'all') {
                        if ($scope.searchTypes[searchType])
                            searchQuery.entryTypes.push(searchType.toUpperCase());
                    }
                }

                search.runAdvancedSearch(searchQuery, function (result) {
                    $scope.searchFilters = searchQuery;
                    $scope.loadingSearchResults = false;
                    $scope.searchResults = result;
                }, function (error) {
                    $scope.loadingSearchResults = false;
                    $scope.searchResults = undefined;
                });
            } else {
                $scope.searchFilters.q = $scope.queryText;
                $scope.searchFilters.s = $scope.sequenceText;
                $scope.searchFilters.sort = 'relevance';
                $scope.searchFilters.asc = false;
                $scope.searchFilters.t = [];
                $scope.searchFilters.b = $scope.blastSearchType;
                $scope.searchFilters.hasSample = $scope.hasSample;
                $scope.searchFilters.hasSequence = $scope.hasSequence;
                $scope.searchFilters.hasAttachment = $scope.hasAttachment;

                for (var type in $scope.searchTypes) {
                    if ($scope.searchTypes.hasOwnProperty(type) && type !== 'all') {
                        if ($scope.searchTypes[type])
                            $scope.searchFilters.t.push(type);
                    }
                }

                $scope.loadingPage = true;
                $location.path('/search');
                $location.search('q', $scope.queryText);
                $scope.runUserSearch();
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