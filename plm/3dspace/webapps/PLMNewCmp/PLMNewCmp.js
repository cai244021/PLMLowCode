define('DS/PLMNewCmp/InitClassificationCmpUI', [
  'DS/i3DXCompassPlatformServices/i3DXCompassPlatformServices',
  'UWA/Class',
  'text!DS/PLMNewCmp/assets/classificationAppsRequire_setting.json',
], function (e, t, i) {
  'use strict'
  return t.extend({
    init: function () {
      ;(this.container = UWA.createElement('div', {
        class: 'classificationPLMNewContainer',
      })),
        (this.controller = {})
    },
    setObjectId: function (e) {
      this.objectId = e
    },
    getUI: function (t) {
      var n = this
      return (
        (function (e) {
          var t = Promise.resolve()
          e.forEach(function (e) {
            t = t.then(e)
          })
        })([
          function () {
            if (t.isBookmark)
              return new Promise(function (e, o) {
                var s = JSON.parse(i).facets.bookmarks.require
                require([s], function (e) {
                  n.plmNewFolderActive = new e({
                    dialogButtons: !0,
                    selectionMode: 'ONLYSingle',
                    favoriteButton: !0,
                    appId: 'Bookmarks',
                    handleClassification: !0,
                    OKButtonLabel: 'OK',
                    componentTitle: 'OK',
                    isRootSelectable: !0,
                    isClassifyMultiEveDispatch: !1,
                    isonSetActiveEveDispatch: !0,
                    container: n.container,
                    getClassificationAttributes: !1,
                    isInformationPanel: t.isInformationPanel,
                    objectId: n.objectId,
                    securityContext: t.securityContext,
                  })
                })
                const a = setInterval(function () {
                  n.plmNewFolderActive && (e(), clearInterval(a))
                }, 100)
              })
          },
          function () {
            if (t.isLibrary)
              return new Promise(function (e, o) {
                var s = JSON.parse(i).facets.library.require
                require([s], function (e) {
                  n.plmNewLibraryActive = new e({
                    dialogButtons: !0,
                    selectionMode: 'ONLYSingle',
                    favoriteButton: !0,
                    appId: 'Libraries',
                    handleClassification: !0,
                    OKButtonLabel: 'OK',
                    componentTitle: 'OK',
                    isRootSelectable: !0,
                    isClassifyMultiEveDispatch: !1,
                    isonSetActiveEveDispatch: !0,
                    container: n.container,
                    getClassificationAttributes: !0,
                    attributesReadOnly: !1,
                    isInformationPanel: t.isInformationPanel,
                    objectId: n.objectId,
                    securityContext: t.securityContext,
                  })
                })
                const a = setInterval(function () {
                  n.plmNewLibraryActive && (e(), clearInterval(a))
                }, 100)
              })
          },
          function () {
            if (t.isExportControl)
              return new Promise(function (o, s) {
                e.getGrantedRoles(function (e) {
                  for (var o = 0; o < e.length; o++)
                    if (e[o] && ('IPS' === e[o].id || 'IPA' === e[o].id)) {
                      var s = JSON.parse(i).facets.ipexportControl.require
                      require([s], function (e) {
                        n.plmNewIPProtectionActive = new e({
                          dialogButtons: !0,
                          selectionMode: 'ONLYSingle',
                          favoriteButton: !0,
                          appId: 'IPExportControl',
                          handleClassification: !0,
                          OKButtonLabel: 'OK',
                          componentTitle: 'OK',
                          isRootSelectable: !0,
                          isClassifyMultiEveDispatch: !1,
                          isonSetActiveEveDispatch: !0,
                          container: n.container,
                          getClassificationAttributes: !0,
                          attributesReadOnly: !0,
                          isInformationPanel: t.isInformationPanel,
                          objectId: n.objectId,
                          securityContext: t.securityContext,
                        })
                      })
                      break
                    }
                })
                const a = setInterval(function () {
                  n.plmNewIPProtectionActive && (o(), clearInterval(a))
                }, 100)
              })
          },
        ]),
        this.container
      )
    },
    getController: function () {
      return (
        (this.controller.plmNewFolderActive = this.plmNewFolderActive),
        (this.controller.plmNewLibraryActive = this.plmNewLibraryActive),
        (this.controller.plmNewIPProtectionActive = this.plmNewIPProtectionActive),
        this.controller
      )
    },
  })
}),
  define('DS/PLMNewCmp/service/PLMNewCommonWebService', [
    'UWA/Core',
    'DS/WAFData/WAFData',
    'i18n!DS/IPClassifyActive/assets/nls/IPClassifyActive',
    'DS/PADUtils/PADSettingsMgt',
    'DS/PADUtils/PADUtilsServices',
    'DS/WidgetServices/WidgetServices',
    'DS/IPClassifyUtil/IPClassifyUtility',
    'DS/IPClassifyActive/utils/IPClassifyActiveUtil',
    'DS/IPClassifyUXUtil/CredentialsView',
  ], function (e, t, i, n, o, s, a, r, l) {
    'use strict'
    return {
      _orderField: 'ds6w:label',
      _orderBy: 'asc',
      getSetting: function () {
        return r.getSetting()
      },
      get3DSpaceUrl: function () {
        return new Promise(function (e, t) {
          s.get3DSpaceUrlAsync({
            onComplete: function (t) {
              e(t)
            },
            onFailure: function (e) {
              t(e)
            },
          })
        })
      },
      getSearchInfo: function () {
        return {
          with_nls: !1,
          with_indexing_date: !0,
          select_predicate: [
            'ds6w:label',
            'ds6w:created',
            'ds6w:reservedBy',
            'relcount',
            'taxonomies',
            'ds6w:classification',
            'icon_2ddefaultthb.subtype',
            'ds6w:type',
            'physicalid',
            'ds6w:classificationUsage',
          ],
          select_file: ['icon', 'thumbnail_2d'],
          order_field: 'ds6w:modified',
          order_by: 'desc',
          nresults: 25,
        }
      },
      getShowContentInViewSetting: function () {
        return void 0 !== window.top.showContentInView
          ? window.top.showContentInView
          : this.getSetting().showContentInView
      },
      setOrder: function (e, t) {
        ;(this._orderField = e || this._orderField),
          (this._orderBy = t || this._orderBy)
      },
      getTenantID: function () {
        return window.top.tenantId || s.getTenantID()
      },
      getSecurityContext: function () {
        var e = n.getSetting('pad_security_ctx')
        if (!e && widget.hasPreference('Current_security_credentials')) {
          var t = widget.getPreference('Current_security_credentials')
          void 0 !== t.defaultValueNonNls &&
            null !== t.defaultValueNonNls &&
            t.hasOwnProperty('defaultValueNonNls') &&
            (e = 'ctx::' + t.defaultValueNonNls)
        }
        return window.top.securityContext || e || this._securityContext
      },
      getSelect_bo: function () {
        return [
          'ds6w:label',
          'ds6w:what/ds6w:type',
          'physicalid',
          'ds6w:when/ds6w:created',
          'ds6w:when/ds6w:modified',
          'ds6w:how/ds6w:classification',
          'icon_2ddefaultthb.subtype',
        ]
      },
      _getSecurityContext: function (e) {
        var t = this,
          i = e,
          n =
            '/resources/modeler/pno/person?current=true&select=preferredcredentials&select=collabspaces',
          o = 'GET',
          s = i.tenant ? i.tenant : this.getTenantID()
        t.get3DSpaceUrl().then(function (e) {
          var a = e + n + '&tenant=' + s,
            r = widget.lang ? widget.lang : widget.getValue('lang')
          return (
            (i.method = o),
            (i.headers = {
              Accept: 'application/json',
              'Accept-Language': r,
              'Content-Type': 'application/json',
            }),
            (i.cache = -1),
            (i.withoutSC = !0),
            t._executeRequest(a, i, !0)
          )
        })
      },
      fetchContent: function (e, t) {
        return (
          (t.payload.specific_source_parameter = this.getSpecific_source_parameter_content(
            t.pid,
          )),
          (t.payload.label = this.getLabel()),
          (t.data = UWA.Json.encode(t.payload)),
          this._executeRequest(e, t, 'searchInFolder')
        )
      },
      fetch2: function (e) {
        var t = [
            'type',
            'label',
            'ds6w:classification',
            'icon_2ddefaultthb.subtype',
          ],
          i = {}
        UWA.extend(i, e)
        var n = e.ids,
          o = this
        n.length &&
          o.get3DSpaceUrl().then(function (s) {
            console.log(s)
            var a = s + '/cvservlet/fetch/v2',
              r = l.getCredential(),
              c = widget.lang ? widget.lang : widget.getValue('lang')
            ;(i.payload = { physicalid: n, label: e.label }),
              (i.payload.select_predicate = t),
              (i.query = { SecurityContext: r }),
              (i.method = 'POST'),
              (i.headers = {
                SecurityToken: window.top.dsUserLogin + '|' + r + '|preferred',
                Accept: 'application/json',
                'content-type': 'application/json',
                'Accept-Language': c,
              }),
              (i.type = 'json'),
              (i.data = UWA.Json.encode(i.payload))
            o._executeRequest(a, i, 'fetch')
          })
      },
      flattenedSearch: function (e) {
        var t = {}
        if ((UWA.extend(t, e), !UWA.is(t, 'object')))
          throw new Error(
            'Invalid inputs for FolderActiveWebService.searchInActiveBookmark',
          )
        var i = e.searchKey
            ? e.searchKey
            : this.generateSearchQuery(e.searchKey, e.pid, e.type),
          n = this
        new Promise(function (e, t) {
          s.get3DSearchUrlAsync({
            onComplete: function (t) {
              e(t)
            },
            onFailure: function (e) {
              t(e)
            },
          })
        }).then(function (o) {
          var s = o + '/search',
            a = widget.lang ? widget.lang : widget.getValue('lang')
          ;(t.payload = n.getSearchInfo()),
            (t.payload.locale = a),
            (t.payload.order_by = n._orderBy),
            (t.payload.order_field = n._orderField),
            (t.payload.query = '[ds6w:label]:("*' + i + '*")'),
            (t.payload.tenant = n.getTenantID()),
            (t.payload.resourceid_not_in = e.alreadyPresentIDs),
            (t.payload.next_start = void 0),
            (t.payload.nresults =
              void 0 !== e.nresults
                ? e.nresults
                : parseInt(nresults) || t.payload.nresults),
            (t.payload.start = void 0),
            (t.payload.source = ['3dspace']),
            (t.payload.login = {
              '3dspace': { SecurityContext: n.getSecurityContext() },
            }),
            (t.payload.label = n.getLabel()),
            delete t.payload.specific_source_parameter,
            e.pid &&
              (t.payload.specific_source_parameter = n.getSpecific_source_parameter(
                e.pid,
                !0,
              )),
            (t.payload.specific_source_parameter = {
              '3dspace': { additional_query: 'AND ' + n.getSearchQuery() },
            }),
            n.getRefineSearch && (t.payload.refine = n.getRefineSearch()),
            console.log(t.payload.specific_source_parameter)
          null != e.next_start
            ? (t.payload.next_start = e.next_start)
            : (t.payload.start = 0),
            (t.method = 'POST'),
            (t.headers = {
              Accept: 'application/json',
              'content-type': 'application/json',
              'Accept-Language': a,
            }),
            (t.type = 'json'),
            (t.data = UWA.Json.encode(t.payload)),
            (t.withoutSC = !0),
            (t.timeout = 3e6),
            n._executeRequest(s, t, 'searchInFolder')
        })
      },
      getLabel: function (e) {
        return (
          'IPNewPLMComponent-' +
          (window.top.dsUserLogin ? window.top.dsUserLogin : 'unknown') +
          '-' +
          new Date().getTime()
        )
      },
      getClassPaths: function (e) {
        var t = {}
        if ((UWA.extend(t, e), !UWA.is(t, 'object')))
          throw new Error(
            'Invalid inputs for FolderActiveWebService.searchInActiveBookmark',
          )
        var i = this
        i.get3DSpaceUrl().then(function (s) {
          console.log(s)
          var a = s + '/cvservlet/navigate',
            r = widget.lang ? widget.lang : widget.getValue('lang'),
            l = null
          e.plmNewContextProvider && (l = e.plmNewContextProvider),
            (t.payload = {}),
            (t.payload.locale = r),
            (t.payload.tenant = o.getPlatformId()),
            (t.query = {
              SecurityContext: l || n.getSetting('pad_security_ctx'),
              tenant: o.getPlatformId(),
            }),
            (t.payload.input_physical_ids = e.id),
            (t.payload.attributes = [
              'ds6w:label',
              'physicalid',
              'ds6w:type',
              'ds6w:classification',
            ]),
            (t.payload.version = 3),
            (t.payload.primitives = [
              { navigate_to_rel: { id: 1 } },
              { navigate_from_rel: { id: 2 } },
            ]),
            (t.payload.patterns = { GET_PARENT: [{ id: 2, iter: -1 }] }),
            (t.payload.patternFlags = {
              GET_PARENT: { flags: ['returnPaths'] },
            }),
            (t.payload.source = ['3dspace'])
          var c = new Date()
          ;(t.payload.label = (
            'PlmNewClassificationFacetNavigate' + c.getTime()
          )
            .replace(new RegExp(' ', 'g'), '_')
            .replace(new RegExp(',', 'g'), '')
            .replace(new RegExp('/', 'g'), '-')),
            (t.timeout = 12e4),
            (t.payload.start = 0),
            (t.method = 'POST'),
            (t.headers = {
              Accept: 'application/json',
              'content-type': 'application/json',
              'Accept-Language': r,
            }),
            (t.type = 'json'),
            (t.data = UWA.Json.encode(t.payload))
          i._executeRequest(a, t, 'getClassPaths')
        })
      },
      constructPath: function (e, t = 'idPathMap') {
        var i = []
        const n = new Map(),
          o = new Map()
        var s
        return (
          e.result.forEach((e) => {
            var t,
              a = {
                label: (t = e.outputs.GET_PARENT
                  ? e.outputs.GET_PARENT[0]
                  : e.input_object)['ds6w:label'],
                physicalid: t.physicalid,
                type: t['ds6w:type'],
              }
            ;(i = this.constructPathChain(a, t.paths ? t.paths[0] : void 0)),
              (s = this.extractPath(i)),
              o.set(i[i.length - 1].physicalid, i),
              n.set(i[i.length - 1].physicalid, s)
          }),
          'idPathChainMap' === t ? o : n
        )
      },
      constructPathChain: function (e, t) {
        var i = []
        if ((i.push(e), null == t || null == t || 0 == t.length)) return i
        for (let e = t.length - 1; e >= 0; e--) {
          var n = {
            label: t[e]['ds6w:label'],
            physicalid: t[e].physicalid,
            type: t[e]['ds6w:type'],
          }
          i.push(n)
        }
        return i
      },
      extractPath: function (e) {
        var t = e[0].label
        for (let i = 1; i < e.length; i++) t = t + ' > ' + e[i].label
        return t
      },
      _executeRequest: function (e, n, o) {
        this.requestList || (this.requestList = {}),
          ('getClassPaths' != o && 'getClassPaths' != o) ||
            (this.requestList[o] && this.requestList[o].cancel(),
            (this.requestList[o] = null))
        var s = Math.round(1e3 * Math.random()).toString(),
          a = widget.id + '_' + widget.getTitle() + '_'
        window.top.performance.mark('folderRequest_begin' + s)
        var r = {}
        UWA.extend(r, n),
          UWA.is(r.onTimeout, 'function') ||
            ((r.onTimeout = function () {
              var e = {
                type: 'timeout',
                toString: function () {
                  return i.msg_InfoAction_Timeout
                },
              }
              r.onFailure(e)
            }),
            (r.onComplete = function (e) {
              window.top.performance.mark('folderRequest_end' + s)
              var t = window.top.performance.getEntriesByName(
                'folderRequest_begin' + s,
              )
              t &&
                1 === t.length &&
                (window.top.performance.measure(
                  a + '_Request' + o,
                  'folderRequest_begin' + s,
                  'folderRequest_end' + s,
                ),
                window.top.performance.clearMarks('folderRequest_end' + s),
                window.top.performance.clearMarks('folderRequest_begin' + s)),
                n.onComplete(e)
            })),
          UWA.is(r.query) || (r.query = {}),
          r.query.tenant ||
            !0 === n.withoutTenant ||
            (r.query.tenant = this.getTenantID())
        var l = e
        return (
          0 !== Object.keys(r.query).length &&
            (l +=
              (e.indexOf('?') < 0 ? '?' : '&') +
              UWA.Utils.toQueryString(r.query)),
          (this.requestList[o] = t.authenticatedRequest(l, r)),
          this.requestList[o]
        )
      },
    }
  }),
  define('DS/PLMNewCmp/utils/PLMNewClassificationFacetData', [
    'UWA/Core',
    'DS/TreeModel/TreeDocument',
    'DS/TreeModel/TreeNodeModel',
    'DS/Controls/TooltipModel',
    'i18n!DS/PLMNewCmp/assets/nls/PLMNewCommCmpSel',
  ], function (e, t, i, n, o) {
    return e.Class.extend({
      init: function (e) {},
      buildSkeleton: function (e) {},
      _generateSearchData: function (e, i, n) {
        var s = this,
          a = { 0: new t() }
        a[0].onPreExpand(function (e) {
          e.data.onlyUI && e.target.preExpandDone()
        })
        var r = {
          'ds6w:label': 'label',
          'ds6w:what/ds6w:type': 'type',
          type_icon_url: 'icon',
          resourceid: 'id',
          'ds6w:created': 'created',
          'ds6w:modified': 'modified',
          'ds6w:when/ds6w:created': 'created',
          'ds6w:when/ds6w:modified': 'modified',
          'ds6w:how/ds6w:classification': 'folderClassification',
          'icon_2ddefaultthb.subtype': 'iconType',
          mxid: 'mxid',
          'ds6w:classificationUsage': 'classUsage',
        }
        return (
          e.forEach(function (e) {
            var t = e.attributes,
              l = {}
            t.forEach(function (e) {
              var t = r[e.name]
              t && (l[t] = e.value),
                'type' === t && (l.displayType = e.dispValue),
                'type_icon_url' === t && (t.icon = e.value)
            })
            var c = { useAsyncPreExpand: !0 }
            ;(c.value = {
              label: l.label,
              subLabel: o.loading,
              icon: [l.icon],
            }),
              (c.label = l.label),
              (c.id = l.id),
              (c.plmType = l.type),
              (c.created = l.created),
              (c.modified = l.modified),
              (c.folderClassification = l.folderClassification),
              (c.iconType = l.iconType),
              (c.mxid = l.mxid),
              (c.classUsage = l.classUsage)
            var p = e && e.level ? parseInt(e.level) : 1
            ;(c.icons = [i(l)]),
              a[0].addChild(s._createTreeNodeModel(c, a[p - 1], n))
          }),
          a[0]
        )
      },
      createAutocompleteDataTree: function (e) {
        var i = new t({
          shouldBeSelected: function (e) {
            return e.isSelectable
          },
        })
        return (
          i.getRoots().forEach(function (e, t) {
            e.sortChildren({
              isRecursive: !0,
              sortFunction: function (e, t) {
                return e.options.label.localeCompare(t.options.label) >= 1
                  ? 1
                  : -1
              },
            })
          }),
          i.pushUpdate(),
          i
        )
      },
      _createTreeNodeModel: function (e, o, s, a) {
        var r = o instanceof t
        e && e.publishOnConnect && delete e.publishOnConnect,
          e && e.folderClassification && delete e.folderClassification,
          e && e.children && (e.children = []),
          (e.useAsyncPreExpand = !0)
        var l = new i(e)
        return (
          (l.id = e.id),
          (l.plmType = e.plmType),
          e.isFavorite && (l.isFavorite = !0),
          (e.isPersonal || (!r && o.isPersonal)) && (l.isPersonal = !0),
          e.isExisting && (l.isExisting = e.isExisting),
          e.created && (l.created = e.created),
          (l.getID = function () {
            var e = this.id
            return (e && '' !== e) || (e = this.plmType), e
          }),
          (l.getLabel = function () {
            return this.options.label
          }),
          (l.getType = function () {
            return this.plmType
          }),
          (l.mxid = e.mxid),
          (l.label = e.label),
          (l.options.oldLabel = e.label),
          (l.pathId = (r ? 'root' : o.pathId) + ',' + l.getID()),
          (l.isSelectable = s(e.plmType)),
          o.prepareUpdate(),
          r ? o.addRoot(l) : o.addChild(l, a),
          o.pushUpdate(),
          l.updateOptions({
            tooltip: new n({
              title: l.options.value ? l.options.value.label : l.options.label,
              shortHelp: l.options.value
                ? l.options.value.subLabel
                : l.options.label,
              mouseRelativePosition: !0,
            }),
          }),
          l
        )
      },
    })
  }),
  define('DS/PLMNewCmp/PLMNewClassificationFacet', [
    'DS/ENONewWidget/ENONew/View/Facet/ENONewFacetBase',
    'UWA/Class/Options',
    'UWA/Class/Events',
    'DS/PADUtils/PADSettingsMgt',
    'DS/ClassificationProp/models/ClassifPropModel',
    'i18n!DS/PLMNewCmp/assets/nls/PLMNewCommCmpSel',
    'DS/PLMNewCmp/InitClassificationCmpUI',
    'css!DS/PLMNewCmp/PLMNewCmp.css',
  ], function (e, t, i, n, o, s, a) {
    return e.extend(t, i, {
      name: 'PLMNewClassificationFacet',
      securityContext: null,
      init: function (e) {
        var t = e
        this._parent(e),
          (this.initUI = new a()),
          (this.elements.container = this.initUI.getUI(e)),
          t.securityContext && (securityContext = t.securityContext),
          this.listenToPublishedInfo(function (e, t) {
            if ('currentSecurityContext' === e) {
              let e = t.value
              securityContext = e
            }
          })
      },
      plmNewContextProvider: function () {
        return securityContext
      },
      getUI: function (e) {
        return this.elements.container
      },
      postCreation: function (e) {
        var t = this
        let i,
          n,
          a,
          r = e[0].identifier,
          l = new Map()
        var c
        function p(e, t, i) {
          e.length > 0 &&
            e.forEach(function (e) {
              function n(e) {
                let n = e.slice(4),
                  o = t.get(n)
                l.has(i) || l.set(i.toString(), new Set()), l.get(i).add(o)
              }
              'string' == typeof e && e.startsWith('fail')
                ? n(e)
                : e && e.value && e.value.startsWith('fail') && n(e.value)
            })
        }
        if (
          ((t.IDsToClassify = []),
          (t.IDsToBookmark = []),
          (t.IDsToProtect = []),
          (t.controller = this.initUI.getController()),
          (t.plmNewFolderActive = t.controller.plmNewFolderActive),
          (t.plmNewIPProtectionActive = t.controller.plmNewIPProtectionActive),
          (t.plmNewLibraryActive = t.controller.plmNewLibraryActive),
          t.plmNewIPProtectionActive &&
            (a = new Promise(function (e, t) {
              ;(c = e), t
            })),
          t.plmNewFolderActive &&
            t.plmNewFolderActive.saveRespectiveAssociations)
        ) {
          t.plmNewFolderActive.folderActiveActionBar &&
            t.plmNewFolderActive.folderActiveActionBar.nestedMenuInContext
              .popover &&
            t.plmNewFolderActive.folderActiveActionBar.nestedMenuInContext.popover.dispatchEvent(
              'close',
            ),
            t.plmNewFolderActive.autoComplete &&
              t.plmNewFolderActive.autoComplete.selectedItems &&
              t.plmNewFolderActive.autoComplete.selectedItems.length > 0 &&
              (t.IDsToBookmark = t.plmNewFolderActive.autoComplete.selectedItems.map(
                function (e) {
                  return e.id
                },
              ))
          let e = t.plmNewFolderActive.classIdAndTitleMap
          n = new Promise(function (i) {
            t.IDsToBookmark.length > 0
              ? t.plmNewFolderActive
                  .saveRespectiveAssociations(
                    r,
                    t.IDsToBookmark,
                    t.plmNewContextProvider,
                  )
                  .then(function (t) {
                    p(t, e, 'BookmarksApp'), i()
                  })
              : i()
          }).then(function (e) {})
        }
        if (
          t.plmNewLibraryActive &&
          t.plmNewLibraryActive.saveRespectiveAssociations
        ) {
          t.plmNewLibraryActive.libraryActiveActionBar &&
            t.plmNewLibraryActive.libraryActiveActionBar.nestedMenuInContext
              .popover &&
            t.plmNewLibraryActive.libraryActiveActionBar.nestedMenuInContext.popover.dispatchEvent(
              'close',
            ),
            t.plmNewLibraryActive.autoComplete &&
              t.plmNewLibraryActive.autoComplete.selectedItems &&
              t.plmNewLibraryActive.autoComplete.selectedItems.length > 0 &&
              (t.IDsToClassify = t.plmNewLibraryActive.autoComplete.selectedItems.map(
                function (e) {
                  return e.id
                },
              ))
          let e = t.plmNewLibraryActive.classIdAndTitleMap
          i = new Promise(function (i) {
            t.IDsToClassify.length > 0
              ? t.plmNewLibraryActive.saveRespectiveAssociations
                  .call(null, r, t.IDsToClassify, t.plmNewContextProvider)
                  .then(function (t) {
                    p(t, e, 'LibraryApp'), i()
                  })
                  .catch(() => {
                    console.log('Some save operation failed')
                  })
              : i()
          })
            .then(function (e) {
              var i = new o({ objectId: r })
              ;[].push(i),
                t.plmNewLibraryActive.classifPropertiesView.controller.skeleton.saveClassificationFacet(
                  r,
                  t.plmNewLibraryActive.classifPropModels,
                  i,
                  t.plmNewContextProvider,
                )
            })
            .catch((e) => {
              console.log('data issue')
            })
        }
        return (
          Promise.allSettled([[], i, n]).then(() => {
            if (
              t.plmNewIPProtectionActive &&
              t.plmNewIPProtectionActive.saveRespectiveAssociations
            ) {
              t.plmNewIPProtectionActive.plmNewIPActionBar &&
                t.plmNewIPProtectionActive.plmNewIPActionBar.nestedMenuInContext
                  .popover &&
                t.plmNewIPProtectionActive.plmNewIPActionBar.nestedMenuInContext.popover.dispatchEvent(
                  'close',
                ),
                t.plmNewIPProtectionActive.autoComplete &&
                  t.plmNewIPProtectionActive.autoComplete.selectedItems &&
                  t.plmNewIPProtectionActive.autoComplete.selectedItems.length >
                    0 &&
                  (t.IDsToProtect = t.plmNewIPProtectionActive.autoComplete.selectedItems.map(
                    function (e) {
                      return e.id
                    },
                  ))
              let e = t.plmNewIPProtectionActive.classIdAndTitleMap
              t.IDsToProtect.length > 0
                ? t.plmNewIPProtectionActive.saveRespectiveAssociations
                    .call(null, r, t.IDsToProtect)
                    .then(function (t) {
                      p(t, e, 'ExportControlApp'), c()
                    })
                : c()
            }
          }),
          new UWA.Promise(function (o) {
            Promise.allSettled([n, i, a]).then(() => {
              var i = {
                level: 'error',
                title: s.replace(s.get('classificationFailure_title'), {
                  plmnewtype: e[0].type,
                }),
                subtitle: s.replace(s.get('classificationFailure_subtitle'), {
                  plmnewtype: e[0].type,
                }),
                message: [s.get('classificationFailure_message')],
                allowUnsafeHTML: !1,
                code: 'error',
              }
              if (0 == l.size) {
                var n = s.replace(s.get('classificationSuccessMessage'), {
                  plmnewtype: e[0].type,
                })
                ;((i = {}).code = '200'), (i.message = [n])
              }
              0 == t.IDsToBookmark.length &&
                0 == t.IDsToClassify.length &&
                0 == t.IDsToProtect.length &&
                (i = null),
                o(i)
            })
          })
        )
      },
      destroy: function () {
        this.options.onDestroyCallback(this.options.tabId), this._parent()
      },
    })
  }),
  define('DS/PLMNewCmp/ClassificationFacet', [
    'DS/EditPropWidget/facets/Common/FacetsBase',
    'DS/PLMNewCmp/InitClassificationCmpUI',
    'DS/ClassificationProp/utils/ClassifPropControl',
  ], function (e, t, i) {
    'use strict'
    return e.extend({
      init: function (e) {
        var t = this
        this._parent(e)
        var n = {}
        for (var o in e) n[o] = e[o]
        ;(n.events = n.events ? n.events : {}),
          (n.events.refreshActions = function (e) {
            t.refreshActions()
          }),
          (this.securityContext = widget.data.pad_security_ctx),
          i.setContext(this.securityContext),
          (this.tenant = widget.data.x3dPlatformId),
          (this.options = e),
          (this.options.isBookmark = !0),
          (this.options.isLibrary = !0),
          (this.options.isExportControl = !0),
          (this.options.isInformationPanel = !0),
          i.setInformationPanel(!0),
          (this.options.securityContext = this.securityContext),
          (this.elements.container = UWA.createElement('div', {
            class: 'classificationInformationContainer',
          }))
      },
      updateFacet: function () {
        this.elements &&
          this.elements.plmContainer &&
          this.elements.plmContainer.destroy()
        var e = this.getModel()
        ;((e && e._attributes && 'relationship' == !e._attributes.metatype) ||
          (e && !e._innerModels)) &&
          (e && e._attributes && e._attributes.selReferenceID
            ? (this.objectId = e._attributes.selReferenceID)
            : e && e._attributes && (this.objectId = e._attributes.objectId)),
          (this.initUI = new t()),
          this.initUI.setObjectId(this.objectId),
          i.setObjectId(this.objectId),
          (this.elements.plmContainer = this.initUI.getUI(this.options)),
          this.elements.plmContainer.inject(this.elements.container)
      },
    })
  }),
  define('DS/PLMNewCmp/PLMNewCommomCmpUXController', [
    'UWA/Controls/Abstract',
    'UWA/Class/Options',
    'UWA/Class/Events',
    'DS/WUXAutoComplete/AutoComplete',
    'DS/Controls/Button',
    'DS/Controls/TooltipModel',
    'DS/Windows/ImmersiveFrame',
    'DS/Windows/Dialog',
    'DS/Controls/Loader',
    'DS/Controls/ModalContainer',
    'DS/Controls/Accordeon',
    'DS/PLMNewCmp/utils/PLMNewClassificationFacetData',
    'DS/TreeModel/TreeDocument',
    'DS/Controls/Popup',
    'DS/ClassificationProp/facets/Properties/Properties',
    'DS/ClassificationProp/models/ClassifPropModel',
    'DS/ClassificationProp/ClassifPropWidget',
    'DS/ClassificationProp/constants/ClassifPropConstants',
    'DS/ClassificationProp/comm/EventManager',
    'DS/PADUtils/PADSettingsMgt',
    'DS/ClassificationProp/utils/ClassifPropControl',
    'i18n!DS/PLMNewCmp/assets/nls/PLMNewCommCmpSel',
    'DS/IPClassifyUtil/IPClassifyPreference',
    'DS/IPClassifyUtil/IPClassifyAlert',
    'css!DS/PLMNewCmp/PLMNewCmp',
  ], function (
    e,
    t,
    i,
    n,
    o,
    s,
    a,
    r,
    l,
    c,
    p,
    d,
    u,
    f,
    m,
    h,
    w,
    C,
    g,
    v,
    y,
    P,
    I,
    A,
  ) {
    return e.extend(t, i, {
      name: 'PLMNewCommomCmpUXController',
      classIdAndTitleMap: new Map(),
      apiAttributesCall: [],
      _isProcessing: !1,
      securityContextMethod: null,
      isJFXENLibraryAttributeFilterEnabled: function () {
        return (
          this.options &&
          'Libraries' === this.options.appId &&
          'undefined' != typeof widget &&
          widget &&
          widget.data &&
          'ENXENG_AP' === widget.data.appId
        )
      },
      isJFXENUnusedLibraryAttribute: function (e, t) {
        if (!this.isJFXENLibraryAttributeFilterEnabled()) return !1
        var i = [
            'CustomerPartNumber',
            'CustomerPartRevision',
            'JF_CustomerPartNumber',
            'JF_CustomerPartRevision',
            'JFCustomerPartNumber',
            'JFCustomerPartRevision',
            '客户零件号',
            '客户零件版本',
            '客户零件版本号',
          ],
          n = [e]
        t &&
          'function' == typeof t.getIdentifier &&
          n.push(t.getIdentifier())
        t &&
          'function' == typeof t.get &&
          ['name', 'label', 'path', 'select', 'attributeID', 'nls'].forEach(
            function (e) {
              n.push(t.get(e))
            },
          )
        t &&
          'object' == typeof t &&
          n.push(
            t.name,
            t.label,
            t.path,
            t.select,
            t.attributeID,
            t.nls,
          )
        return n.some(function (e) {
          if (!e) return !1
          var t = String(e).replace(/\s|\*/g, '')
          return i.some(function (e) {
            return (
              t === e ||
              t === 'attribute[' + e + ']' ||
              t.endsWith('.' + e) ||
              t.endsWith('-' + e)
            )
          })
        })
      },
      installJFXENLibraryAttributeFilter: function (e) {
        if (
          !this.isJFXENLibraryAttributeFilterEnabled() ||
          !e ||
          'function' != typeof e.set ||
          e.jfXENLibraryAttributeFilterInstalled
        )
          return
        var t = this,
          i = e.set
        //20260720 update by ljr XEN创建界面选择库后，在库属性返回模型写入前过滤客户零件号、客户零件版本号。
        e.set = function (e, n) {
          if (e && 'object' == typeof e && !Array.isArray(e)) {
            var o = {}
            return (
              Object.keys(e).forEach(function (i) {
                t.isJFXENUnusedLibraryAttribute(i, e[i]) || (o[i] = e[i])
              }),
              i.call(this, o, n)
            )
          }
          return t.isJFXENUnusedLibraryAttribute(e, n)
            ? this
            : i.apply(this, arguments)
        }
        e.jfXENLibraryAttributeFilterInstalled = !0
      },
      init: function (e) {
        this._parent(e)
        var t = this
        ;(this.elements.masterContainer = UWA.createElement('div', {
          class: 'plmnew-masterContainer',
          id: e.appId,
        })),
          (t.counter = 0),
          (t.alreadySelectedNodes = []),
          (t.alreadyPresentIDs = []),
          t.nodeRemoved,
          y.setProvider(t),
          'function' == typeof t.updateAttribute &&
            y &&
            y.setIPClassifyAlertProvider(A),
          (this.domaincontainer = UWA.createElement('div', {
            class: 'plmnew-domain-container',
          }).inject(this.elements.masterContainer)),
          (this.container = UWA.createElement('div', {
            class: 'plmnew-container',
            id: e.appId + 'plmnew-container',
          }).inject(this.domaincontainer))
        let i = e.container
        this.elements.masterContainer.inject(i),
          (this.isInformationPanel = e.isInformationPanel),
          (this.loader = new l()),
          (t.plmNewClassificationFacetData = new d()),
          (t.accordeonCon = UWA.createElement('div', {
            class: 'plmnew-accordeon-container',
          })),
          this.isInformationPanel ||
            t.accordeonCon.inject(t.elements.masterContainer)
        var n = { className: 'classification_attributes' }
        t.classifPropertiesUI = new m(n, { readOnly: !0 }).inject(
          this.elements.masterContainer,
        )
        var o = null
        e.securityContext &&
          'function' == typeof e.securityContext &&
          ((o = e.securityContext.call(null)),
          (securityContextMethod = e.securityContext)),
          (o = o || v.getSetting('pad_security_ctx')),
          (t.classifPropertiesView = new w({
            typeOfDisplay: C.ALL,
            selectionType: C.NO_SELECTION,
            readOnly: e.attributesReadOnly,
            setEditButton: !1,
            context: {
              getSecurityContext: function () {
                return { SecurityContext: o }
              },
            },
          })),
          t.classifPropertiesView.inject(t.accordeonCon),
          (t.classifPropModels = []),
          (this.storageContainer = new u()),
          (t.previousPresentIds = [])
        var s = t._getComponentName()
        if (!0 === e.isRefreshContainer && this.isInformationPanel) {
          document
            .getElementById(t.options.appId + '-autocomplete-container')
            .remove()
          let i = document.getElementById(e.appId + 'plmnew-container')
          i.removeChild(i.firstChild), (t.container = i)
        }
        if (this.isInformationPanel) {
          var a
          let e = !0
          I.get(s + '_accordeonPreference', (i) => {
            ;(e = !i || 'collapse' != i.type),
              (a = new p({
                items: [
                  {
                    header: s,
                    content: this.createPLMNewManagement(t.accordeonCon),
                    expandedFlag: e,
                  },
                ],
                style: 'filled-separate',
              }).inject(this.container)).addEventListener('expand', function (
                e,
              ) {
                I.set(
                  e.options.expanderTarget.header + '_accordeonPreference',
                  { type: e.type },
                  !0,
                )
              }),
              a.addEventListener('collapse', function (e) {
                I.set(
                  e.options.expanderTarget.header + '_accordeonPreference',
                  { type: e.type },
                  !0,
                )
              })
          })
        } else this.createPLMNewManagement().inject(this.container)
      },
      _closePanel: function () {
        this.dialog && this.dialog.close()
      },
      createAutocompleteComponent: function (e, t, i, a) {
        var r = this,
          l = UWA.createElement('div', {
            class: 'plmnew-autocomplete-container',
          })
        if (!r.isInformationPanel)
          UWA.createElement('div', {
            class: 'plmnew-autocomplete-label',
            html: '<label id="plmnewLabel">' + e + '</label>',
          }).inject(l)
        var c = UWA.createElement('div', {
            class: 'plmnew-autocomplete-line',
          }).inject(l),
          p = UWA.createElement('div', { class: 'plmnew-autocomplete' }).inject(
            c,
          ),
          d = UWA.createElement('div', {
            class: 'plmnew-button-browse',
          }).inject(c)
        r = this
        function m() {
          if (
            !r.autoComplete.selectedItems ||
            !r.autoComplete.selectedItems.length
          )
            return []
          var e = []
          return (
            r.autoComplete.selectedItems.forEach((t) => {
              e.push(t.id)
            }),
            e
          )
        };
        // add by chenyan 限定搜索只能一个
        let w = new u()
        if (
          ((r.autoComplete = new n({
            elementsTree: w,
            placeholder: 'Search',
            typeDelayBeforeSearch: 400,
            keepSearchResultsFlag: !1,
            multiSearchMode: !0,
            minLengthBeforeSearch: 0,
            allowFreeInputFlag: !1,
            selectedItems: [],
            popupSizeAdjustPolicy: f.SizeAdjustPolicyEnum.NoAdjust,
            columnOptions: {
              autoRowHeightFlag: !0,
              typeRepresentation: 'detailedUser',
            },
          }).inject(p)),
          (r.autoComplete.elementsTree = function (e) {
            return (
              (e = e.trim().replace(/^\s+/g, '')),
              (r.autoComplete.customFilterMessage =
                P.autoCompleteFilterMessage.no_objects_found),
              (function (e) {
                return (
                  !!/[*+?()[\]"]/g.test(e) &&
                  ((r.autoComplete.customFilterMessage =
                    'Special Characters * ( ) [ ] +  ? " are not allowed'),
                  !0)
                )
              })(e)
                ? new Promise((e) => {
                    ;(r.autoComplete.customFilterMessage =
                      'Special Characters * ( ) [ ] +  ? " are not allowed'),
                      e(new u())
                  })
                : e.length >= 3
                ? new Promise((t) => {
                    r.storageContainer.removeRoots(),
                      r.PLMNewCommonWebService.flattenedSearch({
                        searchKey: e,
                        alreadyPresentIDs: m(),
                        nresults: 5, //!
                        onComplete: function (i) {
                          if (i.results) {
                            if (
                              (r.storageContainer.prepareUpdate(),
                              r.createAndInsert(r.storageContainer, i.results),
                              i.infos.nmatches > 5)
                            ) {
                              var n = r.enableBrowseOptionInList(e)
                              r.storageContainer.addRoot(n)
                            }
                            r.storageContainer.pushUpdate()
                            var o = []
                            i.results.forEach((e) => {
                              const t = e.attributes.find(
                                (e) => 'physicalid' === e.name,
                              ).value
                              o.push(t)
                            }),
                              r.PLMNewCommonWebService.getClassPaths({
                                id: o,
                                plmNewContextProvider:
                                  r.options.securityContext,
                                onComplete: function (e, t) {
                                  const n = r.PLMNewCommonWebService.constructPath(
                                    e,
                                    'idPathMap',
                                  )
                                  r.storageContainer.prepareUpdate(),
                                    r.storageContainer
                                      .getAllDescendants()
                                      .forEach((e) => {
                                        n.has(e.id) &&
                                          e.updateOptions({
                                            tooltip: new s({
                                              title: e.options.value.label,
                                              shortHelp: n.get(e.id),
                                              mouseRelativePosition: !0,
                                            }),
                                            value: { subLabel: n.get(e.id) },
                                          })
                                      }),
                                    'function' ==
                                      typeof r.setClassUsageForSearchData &&
                                      (r.setClassPathForSearchData(i),
                                      r.setClassUsageForSearchData(n),
                                      r.classifPropertiesUI.setClassPath(
                                        r.getClassPath(),
                                      ),
                                      r.classifPropertiesUI.setClassUsage(
                                        r.getClassUsage(),
                                      )),
                                    r.storageContainer.pushUpdate()
                                },
                                onFailure: function (e) {
                                  console.error(e)
                                },
                              })
                          }
                          t(r.storageContainer)
                        },
                        onFailure: function (e) {
                          console.error(e)
                        },
                      })
                  })
                : new Promise((e) => {
                    ;(r.autoComplete.customFilterMessage =
                      P.autoCompleteFilterMessage.minimum_3_characters_search),
                      e(new u())
                  })
            )
          }),
          'function' == typeof r.getAdditionalNodesAtInit)
        ) {
          let e = r.options.isInformationPanel
          r.getAdditionalNodesAtInit(w, e, r.options.objectId).then((e) => {
            e &&
              e.forEach((e) => {
                'function' == typeof r.getInitIcon &&
                  (e.icons = [r.getInitIcon(e)]),
                  (e.isExisting = !0),
                  (function (e) {
                    let t = r.plmNewClassificationFacetData._createTreeNodeModel(
                      e,
                      w,
                      r.couldBeSelected,
                    )
                    w.pushUpdate(), (w.value = t)
                    var i = w.getChildren().length
                    w.getNthRoot(i - 1).select()
                  })(e)
              })
          }),
            'function' ==
              typeof r.getAttributesandClassPathsForExistingClasses &&
              r.options.isInformationPanel &&
              (r.classifPropertiesUI.setClassPath(r.getClassPath()),
              r.classifPropertiesUI.setClassUsage(r.getClassUsage()),
              r.classifPropertiesUI.setClassPath(r.getAttribute()))
        }
        async function C(e, t, i) {
          await e.forEach((e) => {
            r.addToProcessingCall(v.bind(this, e, t, i.get(e)))
          })
        }
        var v = function (e, t, i) {
          r.autoComplete.selectedItems
          if (t)
            return new Promise((t) => {
              if (r.options.getClassificationAttributes) {
                var n = new h({ objectId: e })
                r.installJFXENLibraryAttributeFilter(n)
                r.classifPropModels.push(n),
                  g.publish('getnoClassificationAttribute', e, !0),
                  r.classifPropertiesUI.setClassIDandTitle(e, i),
                  g.subscribe('getAttributesDone', function (e) {
                    t()
                  }),
                  r.classifPropModels.length > 0 &&
                    r.classifPropertiesView.initDatas(
                      r.classifPropModels,
                      'classificationAttiProp',
                    )
              } else t()
            })
          var n = r.classifPropertiesView.controller._getModel(e)
          g.publish('getnoClassificationAttribute', e, !1)
          var o = r.classifPropModels.indexOf(n)
          ;-1 != o && r.classifPropModels.splice(o, 1)
          var s = r.classifPropertiesUI.getAccordeonFromID(e)
          r.classifPropModels.length > 0 &&
            r.classifPropertiesView.initDatas(
              r.classifPropModels,
              'classificationAttiProp',
            ),
            s && s._removeSection(e)
        }
        return (
          r.autoComplete.addEventListener('change', function (e) {
            let t,
              i,
              n = r.isInformationPanel
            var o = e.dsModel._model.getSelectedNodes(),
              s = o[o.length - 1]
            if (s) {
              const e = s.getID()
              if (null != e && 'Browse' === e)
                return (
                  (s._isSelected = !1),
                  (r.autoComplete.selectedItems = r.autoComplete.selectedItems.filter(
                    (e) => 'Browse' != e.getID(),
                  )),
                  void r.browseAssociations()
                )
              t = !!s.isExisting && s.isExisting
            }
            var a = m(),
              l = (function () {
                if (
                  !r.autoComplete.selectedItems ||
                  !r.autoComplete.selectedItems.length
                )
                  return new Map()
                var e = new Map()
                return (
                  r.autoComplete.selectedItems.forEach((t) => {
                    e.set(t.getID(), t.getLabel())
                  }),
                  e
                )
              })(),
              c = !1
            function p(e, t) {
              return e.filter((e) => !t.includes(e))
            }
            if (r.previousPresentIds.length < a.length) {
              c = !0
              var d = p(a, r.previousPresentIds)
              if (((i = d), !d)) return
              1 == d.length
                ? r.addToProcessingCall(v.bind(this, d[0], c, l.get(d[0])))
                : C(d, c, l)
            } else if (a.length < r.previousPresentIds.length) {
              !0
              var u = p(r.previousPresentIds, a)
              if (((i = u), !u)) return
              1 == u.length ? v(u[0], c, l.get(u[0])) : C(u, c, l)
            }
            !t && n && i
              ? (function (e, t) {
                  let i = {}
                  ;(i.classId = e),
                    (i.IDs = [r.options.objectId]),
                    t
                      ? 'function' == typeof r.addContents && r.addContents(i)
                      : 'function' == typeof r.removeContents &&
                        r.removeContents(i)
                })(i, c)
              : s && (s.isExisting = !1),
              (r.previousPresentIds = m())
          }),
          (new o({
            displayStyle: 'lite',
            label: P.tooltip_browse,
            onClick: a,
            icon: { iconName: 'open-in-a-new-window', fontIconFamily: 1 },
            showLabelFlag: !1,
          }).inject(d).tooltipInfos = new s({ shortHelp: P.tooltip_browse })),
          l.inject(i),
          r.autoComplete
        )
      },
      createAndInsert: function (e, t) {
        var i = this
        result = i.plmNewClassificationFacetData._generateSearchData(
          t,
          function (e) {
            return i._getIcon(e)
          },
          function (e) {
            return i.couldBeSelected(e)
          },
        )
        let n = []
        Object.assign(n, result.getChildren()),
          n.forEach(function (t) {
            e.prepareUpdate(), e.addChild(t), e.pushUpdate()
          })
      },
      _processHandler: async function (e) {
        await e.call(), console.log('api call success')
      },
      addToProcessingCall: function (e) {
        this.apiAttributesCall.push(e),
          0 == this._isProcessing &&
            ((this._isProcessing = !0), this.processCalls())
      },
      processCalls: async function () {
        if (0 != this.apiAttributesCall.length)
          try {
            for (this._isProcessing = !0; this.apiAttributesCall.length > 0; ) {
              let e = this.apiAttributesCall.shift()
              await this._processHandler(e)
            }
          } finally {
            this._isProcessing = !1
          }
        else this._isProcessing = !1
      },
      createAutocompleteDataTree: function (e) {
        return this.plmNewClassificationFacetData.createAutocompleteDataTree(e)
      },
      insertInAutocomplete: function (e, t) {
        ;(t.isSelectable = this.couldBeSelected(t.plmType)),
          this.autoComplete.selectedItems.every(function (e) {
            if (e.id != t.id) return !0
          }) &&
            this.plmNewClassificationFacetData._createTreeNodeModel(
              t.options,
              e._model,
              this.couldBeSelected,
            )
      },
      setPossibleDatasAutocomplete: function (e, t) {
        e.elementsTree = t
      },
      setPossibleDatasAutocompleteSer: function (e, t) {
        e.elementsTree.addRoot(t)
      },
      setValuesAutocomplete: function (e, t) {
        e.value = t
      },
      _setNodesIdTobeHighLighted: function (e) {
        this._IdsTobeHighLighted = e
      },
      enableBrowseOptionInList: function (e) {
        var t = { useAsyncPreExpand: !0 }
        return (
          (t.label = P.browse_more + ' ' + e),
          (t.value = {
            label: P.browse_more,
            subLabel: 'There are some more results containing "' + e + '"',
            icon: 'open-in-a-new-window',
          }),
          (t.id = 'Browse'),
          (t.plmType = 'Browse'),
          (t.enableBrowse = !0),
          (t._isSelected = !0),
          (t.getLabel = function () {
            return 'Browse'
          }),
          this.plmNewClassificationFacetData._createTreeNodeModel(
            t,
            new u(),
            function (e) {
              return !0
            },
          )
        )
      },
      createPLMNewManagement: function (e) {
        var t = UWA.createElement('div', {
          class: 'plmnew-autocompletes-container',
          id: this.options.appId + '-autocomplete-container',
        })
        this.autoCompleteName = this._autoCompleteName()
        this._autocompleteComponent(t)
        return (
          void 0 !== e && e.inject(t),
          this.loader.off(),
          c.removeModal(this.elements.masterContainer),
          t
        )
      },
      browseAssociations: function (e, t, i) {
        // add by chenyan 只有XEN是才有修改逻辑 限制库只能选择一个
        let isXENAPP = false ;
        if (widget){
          const  appID = widget.data.appId;
          console.log("appID:",appID)
          if ("ENXENG_AP" == appID){
            isXENAPP = true ;
          }
        }
        if (isXENAPP){
          if (this.previousPresentIds && this.previousPresentIds.length>=1){
              A.displayNotification({
                message: P.jf_classificationSelectOne,
                className: "error"
              });
            return;
          }

        }

        this.onClickSelector()
      },
    })
  })
