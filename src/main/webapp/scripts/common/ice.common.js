'use strict';

// dependencies for the ice application
angular.module('ice.dependencies', ['ngRoute',
    'ngCookies',
    'iceApp.filters',
    'iceApp.services',
    'iceApp.directives',
    'iceApp.controllers',
    'ui.router',
    'ice.search',
    'ice.upload',
    'ice.entry',
    'ice.collection',
    'ice.wor',
    'ice.admin',
    'ice.common.service',
    'ice.profile',
    'LocalStorageModule'
]);