'use strict';

angular.module('ice.search.service', [])
    .factory('Search', function ($resource, $cookieStore) {
        return function () {

            var sessionId = $cookieStore.get("sessionId");

            return $resource('/rest/search', {}, {
                runSearch:{
                    method:'GET',
                    responseType:"json",
                    headers:{'X-ICE-Authentication-SessionId':sessionId}
                },

                runAdvancedSearch: {
                    method:'POST',
                    responseType:"json",
                    headers:{'X-ICE-Authentication-SessionId':sessionId}
                }
            });
        }
    });