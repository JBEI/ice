'use strict';

/* Controllers */
var app = angular.module('iceApp.controllers', []);

// register controller for iceApp
app.controller('EntryController', function ($scope, $http) {

    var data = '{action:retrieve, entity:entry}';

    $http.post('/ice', data)
        .success(function(result) {
             $scope.result = result;
        })
        .error(function(data) {
            alert('Error');
        });

//    $scope.orderProp = 'id';
});
