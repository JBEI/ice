'use strict';

angular.module('ice.entry.export.controller', [])
    .controller('CustomExportController', function ($scope, $uibModalInstance, selectedTypes, selection, ExportFields,
                                                    Util) {
            $scope.processingDownload = false;
        $scope.selected = {all: selection.all, types: []};
            for (const key in selectedTypes) {
                if (selectedTypes.hasOwnProperty(key)) {
                    $scope.selected.types.push(key);
                }
            }

            // get the fields for display for user
            $scope.fields = ExportFields.fields();
            $scope.sequence = {format: "FASTA"};
            $scope.general = {};

            $scope.canDisplayFieldSet = function (key) {
                return (key === 'general' || selection.all || $scope.selected.types.indexOf(key.toUpperCase()) !== -1);
            };

            $scope.customExport = function () {
                $scope.processingDownload = false;
                $scope.errorSubmitting = false;
                selection.fields = [];

                // get fields user wishes to export
                for (const subFields in $scope.fields) {
                    if (!$scope.fields.hasOwnProperty(subFields))
                        continue;

                    if (subFields !== "general" && !selection.all && $scope.selected.types.indexOf(subFields.toUpperCase()) === -1)
                        continue;

                    for (const field in $scope.fields[subFields]) {
                        if (!$scope.fields[subFields].hasOwnProperty(field))
                            continue;

                        if ($scope.fields[subFields][field].omit)
                            continue;

                        selection.fields.push(field);
                    }
                }

                Util.post("rest/parts/custom?sequenceFormat=" + $scope.sequence.format, selection, function (success) {
                    $scope.processingDownload = true;
                    $scope.errorSubmitting = false;
                }, {}, function (error) {
                    console.error(error);
                    $scope.processingDownload = false;
                    $scope.errorSubmitting = true;
                });
            };
        }
    );
