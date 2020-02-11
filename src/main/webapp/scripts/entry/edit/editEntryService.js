'use strict';

angular.module('ice.entry.edit.service', [])
    .factory('StringArrayToObjectArray', function () {

        return {
            convert: function (baseObject, stringArray, objectArrayField) {
                let arrayLength = stringArray.length;
                if (!arrayLength) {
                    baseObject[objectArrayField] = [{}];
                    return;
                }

                let tmp = [];
                for (let i = 0; i < arrayLength; i++) {
                    tmp.push({value: stringArray[i]});
                }
                angular.copy(tmp, baseObject[objectArrayField]);
            }
        }
    });