<!doctype html>
<html lang="en" data-ng-app="iceApp">
<head>
    <base href="<%=request.getContextPath()%>/">

    <meta charset="utf-8">
    <title>ICE - Inventory of Composable Elements</title>
    <link href="css/font-awesome.min.css" rel="stylesheet">

    <link href="css/ice.css" rel="stylesheet">
    <script src="scripts/lib/jquery/jquery-1.11.3.min.js"></script>
    <link href="css/bootstrap.min.css" rel="stylesheet">

    <script src="scripts/lib/angular/angular.min.js"></script>
    <script src="scripts/lib/angular/angular-route.min.js"></script>
    <script src="scripts/lib/angular/angular-resource.min.js"></script>
    <script src="scripts/lib/angular/angular-cookies.min.js"></script>
    <script src="scripts/lib/angular/angular-animate.min.js"></script>
    <script src="scripts/lib/angular/angular-touch.min.js"></script>

    <script src="scripts/lib/angular-ui/angular-ui-router.min-0.2.15.js"></script>
    <script src="scripts/lib/angular-ui/ui-bootstrap-tpls-0.13.0.min.js"></script>

    <script src="scripts/lib/angular-file-upload/angular-file-upload.min.js"></script>

    <script src="scripts/lib/handsontable-0.14.1/handsontable.full.min.js"></script>
    <script src="scripts/lib/handsontable-0.14.1/pikaday.js"></script>
    <link rel="stylesheet" href="scripts/lib/handsontable-0.14.1/pikaday.css">
    <link rel="stylesheet" media="screen" href="scripts/lib/handsontable-0.14.1/handsontable.full.min.css">

    <script src="scripts/lib/angular-slider/angular-slider.min.js"></script>

    <script src="scripts/lib/momentjs/moment.min.js"></script>
    <script src="scripts/lib/angular-moment/angular-moment.min.js"></script>

    <!-- GRUNT START -->
    <script src="scripts/ice.app.js"></script>

    <script src="scripts/common/ice.common.js"></script>
    <script src="scripts/common/commonService.js"></script>
    <script src="scripts/search/ice.search.js"></script>
    <script src="scripts/upload/ice.upload.js"></script>
    <script src="scripts/entry/ice.entry.js"></script>
    <script src="scripts/collection/ice.collection.js"></script>
    <script src="scripts/wor/ice.wor.js"></script>
    <script src="scripts/admin/ice.admin.js"></script>

    <script src="scripts/services.js"></script>

    <script src="scripts/search/searchService.js"></script>
    <script src="scripts/upload/uploadService.js"></script>
    <script src="scripts/wor/worService.js"></script>

    <script src="scripts/controllers.js"></script>
    <script src="scripts/search/searchController.js"></script>
    <script src="scripts/upload/uploadController.js"></script>
    <script src="scripts/entry/entryController.js"></script>
    <script src="scripts/entry/sample/sampleController.js"></script>
    <script src="scripts/entry/entryService.js"></script>
    <script src="scripts/entry/entryDirectives.js"></script>
    <script src="scripts/collection/collectionController.js"></script>
    <script src="scripts/collection/collectionService.js"></script>
    <script src="scripts/collection/collectionDirectives.js"></script>
    <script src="scripts/wor/worController.js"></script>
    <script src="scripts/admin/adminController.js"></script>
    <script src="scripts/admin/adminDirectives.js"></script>

    <script src="scripts/filters.js"></script>
    <script src="scripts/directives.js"></script>

    <script src="scripts/profile/ice.profile.js"></script>
    <script src="scripts/profile/profileController.js"></script>
    <script src="scripts/profile/profileService.js"></script>

    <!-- GRUNT END -->
</head>

<body>
<div id="container">
    <div id="body" ui-view></div>
    <div class="navbar-fixed-bottom">
        <table cellpadding="0" cellspacing="0" style="width: 100%; background-color: #fff">
            <tr>
                <td colspan="2" class="footer_line"></td>
            </tr>
            <tr>
                <td>
                    <table cellspacing="0" cellpadding="0">
                        <tr>
                            <td align="left" style="vertical-align: top;"><img
                                    src="img/doe-bioenergy-research-cent.gif" height="60px"></td>
                            <td align="left" style="vertical-align: top;"><img src="img/lbnl-logo.gif" height="60px">
                            </td>
                            <td align="left" style="vertical-align: top;"><img src="img/sandia-lab-logo.gif"
                                                                               height="60px"></td>
                            <td align="left" style="vertical-align: top;"><img src="img/ucb-logo.gif" height="60px">
                            </td>
                            <td align="left" style="vertical-align: top;"><img src="img/ucdavis-logo.gif" height="60px">
                            </td>
                            <td align="left" style="vertical-align: top;"><img
                                    src="img/carnegie-institution-logo.gif" height="60px"></td>
                            <td align="left" style="vertical-align: top;"><img src="img/llnl-logo.gif" height="60px">
                            </td>
                            <td align="left" style="vertical-align: top;"><img src="img/pnw.png" height="60px"></td>
                        </tr>
                    </table>
                </td>
                <td align="right">
                    <table cellspacing="0" cellpadding="0" class="font-90em">
                        <tr>
                            <td align="right" style="vertical-align: top;">
                                <table cellspacing="0" cellpadding="0">
                                    <tr>
                                        <td align="left" style="vertical-align: top;">
                                            <div>©&nbsp;</div>
                                        </td>
                                        <td align="left" style="vertical-align: top;">
                                            <a href="https://github.com/JBEI/ice">JBEI ICE Registry</a></td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td align="right" style="vertical-align: top;">
                                <div>All rights reserved.</div>
                            </td>
                        </tr>
                        <tr>
                            <td align="right" style="vertical-align: top;">
                                <table cellspacing="0" cellpadding="0">
                                    <tr>
                                        <td align="left" style="vertical-align: top;"><a
                                                href="https://github.com/JBEI/ice/issues/new">Submit an Issue</a></td>
                                        <td align="left" style="vertical-align: top;">
                                            <div>&nbsp; | &nbsp;</div>
                                        </td>
                                        <td align="left" style="vertical-align: top;">
                                            <a href="http://public-registry.jbei.org/manual">Help</a>
                                        </td>
                                        <td align="left" style="vertical-align: top;">
                                            <div>&nbsp; | &nbsp; <span class="label label-primary">v4.3.2</span></div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </div>
</div>

</body>
</html>
