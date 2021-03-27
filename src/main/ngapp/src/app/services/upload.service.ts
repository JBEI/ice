import {Injectable} from '@angular/core';
import {HttpClient, HttpEvent, HttpHeaders, HttpParams, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs';
import {UserService} from "./user.service";

@Injectable({
    providedIn: 'root'
})
export class UploadService {

    constructor(private http: HttpClient, private user: UserService) {
    }

    // file from event.target.files[0]
    uploadFile(url: string, list: FileList): Observable<HttpEvent<any>> {

        const formData = new FormData();
        for (let i = 0; i < list.length; i += 1) {
            const file: File = list.item(i);
            if (!file) {
                continue;
            }
            formData.append('file', file);
        }

        const params = new HttpParams();
        const headers = new HttpHeaders({'X-ICE-Authentication-SessionId': this.user.getUser().sessionId});

        const options = {
            headers,
            params,
            reportProgress: true,
        };

        const req = new HttpRequest('POST', url, formData, options);
        return this.http.request(req);
    }

    // let multipleSequencesUploader = $scope.multipleSequencesUploader = new FileUploader({
    //   scope: $scope,
    //   // url: 'rest/files/sequences',
    //   method: 'POST',
    //   removeAfterUpload: true,
    //   multiple: true,
    //   autoUpload: false,
    //   headers: {'X-ICE-Authentication-SessionId': Auth.getSessionId()},
    // });
    //
    // multipleSequencesUploader.onSuccessItem = function (fileItem, response, status, headers) {
    //   if (status !== 200 || !response)
    //     return;
    //
    //   for (let i = 0; i < response.data.length; i += 1) {
    //     $scope.multiSequenceFiles.push(response.data[i]);
    //   }
    // };
    //
    // multipleSequencesUploader.onAfterAddingAll = function (items) {
    //   for (let i = 0; i < items.length; i += 1)
    //     items[i].url = 'rest/files/sequences?designId=' + $scope.deDesign.id;
    //
    //   $scope.multiSequenceFiles = [];
    //   multipleSequencesUploader.uploadAll();
    // };
    //
    // // when all selected files have been uploaded and processed
    // multipleSequencesUploader.onCompleteAll = function () {
    //   if ($scope.multiSequenceFiles.length === 1) {
    //     addPartToDesign($scope.multiSequenceFiles[0]);
    //   } else {
    //     let modalInstance = $uibModal.open({
    //       templateUrl: 'js/design/deviceeditor/modal/modal-multi-sequence-file-import.html',
    //       backdrop: 'static',
    //       size: 'lg',
    //       resolve: {
    //         sequenceFiles: function () {
    //           return $scope.multiSequenceFiles;
    //         },
    //         openSpaceCount: function () {
    //           let position = undefined;
    //           for (let i = 0; i < $scope.selectedBin.parts.length; i += 1) {
    //             const part = $scope.selectedBin.parts[i];
    //             if (part.position > $scope.selectedPosition.row) {
    //               position = part.position;
    //               break;
    //             }
    //           }
    //
    //           if (position === undefined)
    //             return -1;
    //
    //           return (position - $scope.selectedPosition.row);
    //         },
    //         designId: function () {
    //           return $scope.deDesign.id;
    //         }
    //       },
    //       controller: 'MultiSequenceFileImport'
    //     });
    //
    //     modalInstance.result.then(function (result) {
    //       // todo : files to delete
    //       addPartsToDesign(result);
    //     });
    //   }
    // };

}
