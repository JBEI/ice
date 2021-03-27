import {Component, OnInit} from '@angular/core';
import {Paging} from "../../../models/paging";
import {Part} from "../../../models/Part";
import {HttpService} from "../../../services/http.service";
import {Folder} from "../../../models/folder";
import {User} from "../../../models/User";

declare var require: any;

@Component({
    selector: 'app-collection-contents',
    templateUrl: './contents.component.html',
    styleUrls: ['./contents.component.css']
})
export class ContentsComponent implements OnInit {

     dnaLogo = require("./assets/dna.svg");

    resource = "collections";
    selectedCollection: string = undefined;
    paging: Paging;
    folder: Folder;
    selectedFolder: Folder;
    header: any;
    entry: Part;
    user: User;
    params: { folderId?: number, showFilter: boolean, filter?: string }
    loadingPage: boolean;
    entryHeaders: any;

    constructor(private http: HttpService) {
    }

    ngOnInit(): void {
        this.paging = new Paging();
        this.paging.limit = 30;
        this.paging.sort = 'created';
        this.params = {showFilter: false};

        this.entryHeaders = {
            status: {field: "status", display: "Status", selected: true},
            hasSample: {field: "hasSample", display: "Has Sample", selected: true},
            hasSequence: {field: "hasSequence", display: "Has Sequence", selected: true},
            alias: {field: "alias", display: "Alias"},
            created: {field: "creationTime", display: "Created", selected: true},
            links: {field: "links", display: "Links"}
        };

        this.folderPageChange();
    }

    pageParts(): void {
        this.http.get(this.resource + "/PERSONAL/entries", this.paging).subscribe((result: any) => {
            if (this.resource === "collections") {
                this.folder = new Folder();
                this.folder.entries = result.data;
                this.folder.count = result.resultCount;
                this.paging.available = result.resultCount; // used in context display
            } else {
                // retrieved folders
                this.folder = result;
                this.paging.available = result.count;
                // if (result.canEdit)
                //     folderNameTooltip = "Click to rename";
            }
        }, (err) => {
        });
    }

    pageCounts(currentPage, resultCount, maxPageCount = 15): string {
        const pageNum = ((currentPage - 1) * maxPageCount) + 1;

        // number on this page
        const pageCount = (currentPage * maxPageCount) > resultCount ? resultCount : (currentPage * maxPageCount);
        return pageNum + " - " + (pageCount) + " of " + (resultCount);
    };

    selectedHeaderField(header: any, a): void {

    }

    tooltipDetails(part: Part): void {

    }

    select(entry: Part): void {

    }

    selectAll(): void {

    }

    setType(type: string): void {

    }

    showEntryDetails(a, b, c): void {
        // if (!this.paging.start) {
        //     params.offset = index;
        // }
        //
        // let offset = ((params.currentPage - 1) * params.limit) + index;
        // EntryContextUtil.setContextCallback(function (offset, callback) {
        //     params.offset = offset;
        //     params.limit = 1;
        //
        //     Util.get("rest/" + resource + "/" + params.folderId + "/entries", function (result) {
        //         if (resource === "collections") {
        //             callback(result.data[0].id);
        //         } else {
        //             callback(result.entries[0].id);
        //         }
        //         loadingPage = false;
        //     }, params);
        // }, params.count, offset, "folders/" + params.folderId, params.sort);
        //
        // //$location.search("fid", folder.id);
        // let url = "entry/" + entry.id;
        // if (sub)
        //     url += '/' + sub;
        //
        // $location.path(url);
        // $location.search({});
        //
        // if (folder && folder.type === 'REMOTE') {
        //     $location.search("folderId", folder.id);
        //     $location.search("remote", true);
        // }
    }

    folderPageChange(): void {
        let notFound = undefined;
        let loadingPage = true;
//             params.offset = (params.currentPage - 1) * params.limit;
//
//             params.fields = [];
//             for (let key in entryHeaders) {
//                 if (!entryHeaders.hasOwnProperty(key))
//                     continue;
//
//                 let header = entryHeaders[key];
//                 if (header.selected) {
//                     params.fields.push(header.field);
//                 }
//             }
//

    };

    showFolderRenameModal(): void {
//             if (!folder.canEdit)
//                 return;
//
//             let modalInstance = $uibModal.open({
//                 templateUrl: 'scripts/folder/modal/rename-folder.html',
//                 controller: function ($scope, $uibModalInstance, folderName) {
//                     newFolderName = folderName;
//                 },
//                 backdrop: 'static',
//                 resolve: {
//                     folderName: function () {
//                         return folder.folderName;
//                     }
//                 },
//                 size: 'sm'
//             });
//
//             modalInstance.result.then(function (newName) {
//                 if (newName === folder.folderName)
//                     return;
//
//                 let tmp = {id: folder.id, folderName: newName};
//                 Util.update("rest/folders/" + tmp.id, tmp, {}, function (result) {
//                     folder.folderName = result.folderName;
//                 })
//             })
    }


// .controller('CollectionFolderController', function ($rootScope, $scope, $location, $uibModal, $stateParams,
//                                                     EntryContextUtil, Selection, Util, localStorageService) {
//         $rootScope.$emit("CollectionSelection", $stateParams.collection);
//         let resource = "collections";
//
//         //
//         // init
//         //
//
//         // default entry headers
//         entryHeaders = {
//             status: {field: "status", display: "Status", selected: true},
//             hasSample: {field: "hasSample", display: "Has Sample", selected: true},
//             hasSequence: {field: "hasSequence", display: "Has Sequence", selected: true},
//             alias: {field: "alias", display: "Alias"},
//             created: {field: "creationTime", display: "Created", selected: true},
//             links: {field: "links", display: "Links"}
//         };
//
//         // default init params
//         params = {
//             'asc': false,
//             'sort': 'created',
//             currentPage: 1,
//             hstep: [15, 30, 50, 100],
//             limit: 30
//         };
//
//         // get client stored headers
//         let storedFields = localStorageService.get('entryHeaderFields');
//         if (!storedFields) {
//             // set default headers
//             let entryHeaderFields = [];
//             for (let key in entryHeaders) {
//                 if (!entryHeaders.hasOwnProperty(key))
//                     continue;
//
//                 let header = entryHeaders[key];
//                 if (header.selected) {
//                     entryHeaderFields.push(header.field);
//                 }
//             }
//
//             // and store
//             localStorageService.set('entryHeaderFields', entryHeaderFields);
//         } else {
//             // set user selected
//             for (let key in entryHeaders) {
//                 if (!entryHeaders.hasOwnProperty(key))
//                     continue;
//
//                 let header = entryHeaders[key];
//                 header.selected = (storedFields.indexOf(header.field) !== -1);
//             }
//         }
//
//         maxSize = 5;  // number of clickable pages to show in pagination
//         let subCollection = $stateParams.collection;   // folder id or one of the defined collections (Shared etc)   ]
//         // retrieve folder contents. all folders are redirected to /folder/{id} which triggers this
//         if (subCollection !== undefined) {
//             folder = undefined;
//             params.folderId = subCollection;
//             if (isNaN(params.folderId))
//                 resource = "collections";
//             else
//                 resource = "folders";
//
//             let context = EntryContextUtil.getContext();
//             if (context) {
//                 let pageNum = (Math.floor(context.offset / params.limit)) + 1;
//                 params.sort = context.sort;
//                 params.currentPage = pageNum;
//             }
//
//             folderPageChange();
//         }
//
//         // custom header selection or de-selection by user
//         selectedHeaderField = function (header, $event) {
//             if ($event) {
//                 $event.preventDefault();
//                 $event.stopPropagation();
//             }
//             header.selected = !header.selected;
//             let storedFields = localStorageService.get('entryHeaderFields');
//
//             if (header.selected) {
//                 // selected by user, add to stored list
//                 storedFields.push(header.field);
//                 localStorageService.set('entryHeaderFields', storedFields);
//             } else {
//                 // not selected by user, remove from stored list
//                 let i = storedFields.indexOf(header.field);
//                 if (i !== -1) {
//                     storedFields.splice(i, 1);
//                     localStorageService.set('entryHeaderFields', storedFields);
//                 }
//             }
//
//             // refresh data
//             folderPageChange();
//         };
//
//         $rootScope.$on("RefreshAfterDeletion", function () {
//             params.currentPage = 1;
//             folderPageChange();
//         });
//
    sort(sortType: string): void {
        this.folder = null;
        this.paging.offset = 0;
        if (this.paging.sort === sortType)
            this.paging.asc = !this.paging.asc;
        else
            this.paging.asc = false;
        this.paging.sort = sortType;
        this.folderPageChange();
    };

//         hStepChanged = function () {
//             folderPageChange();
//         };
//
    selectAllClass(): string {
        // if (Selection.allSelected() || folder.entries.length === Selection.getSelectedEntries().length)
        //     return 'fa-check-square-o';
        //
        // if (Selection.hasSelection())
        //     return 'fa-minus-square';
        return 'fa-square-o';
    };

//
//         setType = function (type) {
//             Selection.setTypeSelection(type);
//         };
//
//         selectAll = function () {
//             if (Selection.allSelected())
//                 Selection.setTypeSelection('none');
//             else
//                 Selection.setTypeSelection('all');
//         };
//
    isSelected(entry): boolean {
        return false;
        // if (Selection.isSelected(entry))
        //     return true;
        //
        // return Selection.searchEntrySelected(entry);
    };

//
//         select = function (entry) {
//             Selection.selectEntry(entry);
//         };
//
//         showEntryDetails = function (entry, index, sub) {
//             if (!params.offset) {
//                 params.offset = index;
//             }
//
//             let offset = ((params.currentPage - 1) * params.limit) + index;
//             EntryContextUtil.setContextCallback(function (offset, callback) {
//                 params.offset = offset;
//                 params.limit = 1;
//
//                 Util.get("rest/" + resource + "/" + params.folderId + "/entries", function (result) {
//                     if (resource === "collections") {
//                         callback(result.data[0].id);
//                     } else {
//                         callback(result.entries[0].id);
//                     }
//                     loadingPage = false;
//                 }, params);
//             }, params.count, offset, "folders/" + params.folderId, params.sort);
//
//             //$location.search("fid", folder.id);
//             let url = "entry/" + entry.id;
//             if (sub)
//                 url += '/' + sub;
//
//             $location.path(url);
//             $location.search({});
//
//             if (folder && folder.type === 'REMOTE') {
//                 $location.search("folderId", folder.id);
//                 $location.search("remote", true);
//             }
//         };
//
//         tooltipDetails = function (e) {
//             currentTooltip = undefined;
//             let params = {};
//             if (folder && folder.type === 'REMOTE') {
//                 params.remote = true;
//                 params.folderId = folder.id;
//             }
//
//             Util.get("rest/parts/" + e.id + "/tooltip", function (result) {
//                 currentTooltip = result;
//             }, params);
//         };
//
//         folderPopupTemplateUrl = "scripts/folder/template.html";
//
//         // opens a modal that presents user with options to share selected folder
//         openFolderShareSettings = function () {
//             let modalInstance = $uibModal.open({
//                 templateUrl: 'scripts/folder/modal/folder-permissions.html',
//                 controller: "FolderPermissionsController",
//                 backdrop: "static",
//                 resolve: {
//                     folder: function () {
//                         return folder;
//                     }
//                 }
//             });
//
//             modalInstance.result.then(function (updatedPermissions) {
//                 if (updatedPermissions) {
//                     folder.accessPermissions = updatedPermissions;
//                 }
//             });
//         };
//
//         openPartsUploadDialog = function () {
//             let modalInstance = $uibModal.open({
//                 templateUrl: 'scripts/folder/modal/folder-upload-parts.html',
//                 controller: "FolderPartsUploadController",
//                 backdrop: "static",
//                 resolve: {
//                     folder: function () {
//                         return folder;
//                     }
//                 }
//             });
//
//             modalInstance.result.then(function (result) {
//                 if (!result)
//                     return;
//                 params.currentPage = 1;
//                 folderPageChange();
//             });
//         };
//
//         openSampleCreationDialog = function () {
//             let modalInstance = $uibModal.open({
//                 templateUrl: 'scripts/folder/modal/folder-create-samples.html',
//                 controller: "FolderCreateSamplesController",
//                 backdrop: "static",
//                 resolve: {
//                     folder: function () {
//                         return folder;
//                     }
//                 }
//             });
//
//             modalInstance.result.then(function () {
//                 params.currentPage = 1;
//                 folderPageChange();
//             });
//         };
//
//         const clickEvent = new MouseEvent("click", {
//             "view": window,
//             "bubbles": true,
//             "cancelable": false
//         });
//
//         exportSampleFolder = function () {
//             Util.download("/rest/folders/" + folder.id + "/file").$promise.then(function (result) {
//                 let url = URL.createObjectURL(new Blob([result.data]));
//                 let a = document.createElement('a');
//                 a.href = url;
//                 a.download = result.filename();
//                 a.target = '_blank';
//                 a.dispatchEvent(clickEvent);
//             });
//         };
//
//         markSampleFolder = function (approved) {
//             const newStatus = approved ? "FULFILLED" : "REJECTED";
//             Util.update("rest/samples/requests/" + folder.sampleRequest.id + "?status=" + newStatus, {}, {isFolder: true}, function (result) {
//                 if (result === undefined || result.id !== folder.sampleRequest.id)
//                     return;
//
//                 folder.sampleRequest.status = result.status;
//             });
//         };
//
//         getDisplay = function (permission) {
//             if (permission.article === 'ACCOUNT')
//                 return permission.display.replace(/[^A-Z]/g, '');
//
//             // group
//             if (permission.article === 'GROUP')
//                 return permission.display;
//
//             return "Remote";
//         };
//
//         shareText = function (permission) {
//             let display = "";
//             if (permission.article === 'GROUP')
//                 display = "Members of ";
//
//             display += permission.display;
//
//             if (permission.type.lastIndexOf("WRITE", 0) === 0)
//                 display += " can edit";
//             else
//                 display += " can read";
//             return display;
//         };
//
//         changeFolderType = function (newType) {
//             let tmp = {id: folder.id, type: newType};
//             Util.update('rest/folders/' + folder.id, tmp, {}, function (result) {
//                 folder.type = result.type;
//                 if (newType === 'PUBLIC')
//                     $location.path('folders/available');
//                 else
//                     $location.path('folders/personal');
//             });
//         };
//
//         showFolderRenameModal = function () {
//             if (!folder.canEdit)
//                 return;
//
//             let modalInstance = $uibModal.open({
//                 templateUrl: 'scripts/folder/modal/rename-folder.html',
//                 controller: function ($scope, $uibModalInstance, folderName) {
//                     newFolderName = folderName;
//                 },
//                 backdrop: 'static',
//                 resolve: {
//                     folderName: function () {
//                         return folder.folderName;
//                     }
//                 },
//                 size: 'sm'
//             });
//
//             modalInstance.result.then(function (newName) {
//                 if (newName === folder.folderName)
//                     return;
//
//                 let tmp = {id: folder.id, folderName: newName};
//                 Util.update("rest/folders/" + tmp.id, tmp, {}, function (result) {
//                     folder.folderName = result.folderName;
//                 })
//             })
//         }
//     })
// }
}
