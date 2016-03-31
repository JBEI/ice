angular.module('ice.wor.service', [])
    .factory('WorService', function () {
        var selectedPartner = undefined;
        var context;
        var menuDetails = [
            {
                url: 'scripts/wor/entry/general-information.html',
                display: 'General Information',
                isPrivileged: false,
                icon: 'fa-exclamation-circle'
            },
            {
                id: 'sequences',
                url: 'scripts/wor/entry/sequence-analysis.html',
                display: 'Sequence Analysis',
                isPrivileged: false,
                countName: 'sequenceCount',
                icon: 'fa-search-plus'
            },
            {
                id: 'comments',
                url: 'scripts/wor/entry/comments.html',
                display: 'Comments',
                isPrivileged: false,
                countName: 'commentCount',
                icon: 'fa-comments-o'
            },
            {
                id: 'samples',
                url: 'scripts/wor/entry/samples.html',
                display: 'Samples',
                isPrivileged: false,
                countName: 'sampleCount',
                icon: 'fa-flask'
            }
        ];

        return {
            setSelectedPartner: function (partner) {
                selectedPartner = partner;
            },

            getSelectedPartner: function () {
                return selectedPartner;
            },

            setContextCallback: function (callback, available, offset, back) {
                context = {};
                context.callback = callback;
                context.available = available;
                context.offset = offset;
                context.back = back;
            },

            getContext: function () {
                return context;
            },

            getMenu: function () {
                return menuDetails;
            }
        }
    });
