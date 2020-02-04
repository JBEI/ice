'use strict';

angular.module('ice.download.controller', [])
    .controller('DownloadFile', function ($scope, $stateParams, Util) {
        console.log("download", $stateParams.uuid);

        const clickEvent = new MouseEvent("click", {
            "view": window,
            "bubbles": true,
            "cancelable": false
        });

        Util.download("rest/file/exports/" + $stateParams.uuid).$promise.then(function (result) {
            let url = URL.createObjectURL(new Blob([result.data]));
            let a = document.createElement('a');
            a.href = url;
            a.download = result.filename();
            a.target = '_blank';
            a.dispatchEvent(clickEvent);
        });
    });