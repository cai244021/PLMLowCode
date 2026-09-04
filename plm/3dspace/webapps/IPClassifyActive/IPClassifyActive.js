define(
  "DS/IPClassifyActive/utils/IPClassifyActiveLogger",
  ["UWA/Class", "DS/Logger/Logger"],
  function (e, t) {
    "use strict";
    return e.singleton({
      init: function () {
        this._logger = t.getLogger("DS/IPClassifyActive");
      },
      getLogger: function () {
        return this._logger;
      },
    });
  }
),
  define(
    "DS/IPClassifyActive/utils/IPClassifyActiveCtxSelectionPanel",
    [
      "UWA/Class",
      "DS/Controls/Button",
      "DS/Windows/Dialog",
      "DS/Controls/ComboBox",
      "DS/Windows/ImmersiveFrame",
      "DS/IPClassifyUXUtil/service/IPClassifyUXUtilWebService",
      "DS/IPClassifyUXUtil/private/CredentialsConverter",
      "DS/Controls/LineEditor",
      "DS/IPClassifyUtil/IPClassifyAlert",
      "i18n!DS/IPClassifyActive/assets/nls/IPClassifyActive",
    ],
    function (e, t, i, n, o, a, s, r, l, d) {
      "use strict";
      return e.extend({
        init: function (e) {
          this._parent(e), this.buildSkeleton(e);
        },
        buildSkeleton: function (e) {
          var a = this;
          this.populateComboBox(e.tenant)
            .then(function (s) {
              var l = new o(),
                c = UWA.createElement("div", { class: "context-selector" }),
                u = UWA.createElement("div", {
                  class: "text1-context-selector",
                }).inject(c);
              a._createLabel(d.IPClassify_panel_title, !0, u);
              var h = new r().inject(c),
                p = UWA.createElement("div", {
                  class: "text2-context-selector",
                }).inject(c);
              a._createLabel(d.IPClassify_panel_credentials, !0, p);
              var f = new n({
                  value: s.preferrredctx,
                  elementsList: s.elementsList,
                }).inject(c),
                _ = new i({
                  immersiveFrame: l,
                  title: e.title,
                  content: c,
                  activeFlag: !1,
                  modalFlag: !0,
                  resizableFlag: !0,
                  height: 180,
                  width: 300,
                  resizableFlag: "horizontal",
                  buttons: {
                    Ok: new t({
                      onClick: function (t) {
                        t.preventDefault(), t.stopPropagation();
                        var i = { title: h.value, ctx: f.value };
                        e.callback(i),
                          _.immersiveFrame.getContent().destroy(),
                          _.destroy();
                      },
                    }),
                    Cancel: new t({
                      onClick: function () {
                        e.callback("none"),
                          _.immersiveFrame.getContent().destroy(),
                          _.destroy();
                      },
                    }),
                  },
                });
              (_.buttons.Ok.disabled = !0),
                (h._myInput.oninput = function () {
                  _.buttons.Ok.disabled = 0 == this.value.length;
                }),
                _.getContent().addClassName("ctx-chooser-active-component"),
                _.addEventListener("close", function (t) {
                  t.stopPropagation(),
                    e.callback("none"),
                    _.immersiveFrame.getContent().destroy(),
                    _.destroy();
                }),
                l.inject(e.parentPanel);
            })
            .catch(function (e) {
              e && l.displayNotification({ message: e, className: "error" });
            });
        },
        checkForIfOrgisSame: function (e) {
          var t = [];
          return (
            e.forEach(function (e) {
              t.push(e.org);
            }),
            1 === new Set(t).size
          );
        },
        populateComboBox: function (e) {
          var t = this;
          return new Promise(function (i, n) {
            a.getscandcollab({ platformID: e }).then((e) => {
              var o = UWA.is(e, "string") ? JSON.parse(e) : e,
                a = s.computeCredentials(o),
                r = a.credential;
              if (a.credentials && a.credentials.length > 0) {
                var l = t.checkForIfOrgisSame(a.credentials),
                  c = [],
                  u = ["VPLMProjectAdministrator", "VPLMAdmin"];
                if (
                  (a.credentials.forEach(function (e) {
                    if (-1 == u.indexOf(e.role)) {
                      var t = "ctx::" + e.role + "." + e.org + "." + e.cs;
                      if (l) var i = e.csNLS + " ● " + e.roleNls;
                      else i = e.csNLS + " ● " + e.orgNls + " ● " + e.roleNls;
                      c.push({ label: i, value: t });
                    }
                  }),
                  c.length > 0)
                )
                  i({ elementsList: c, preferrredctx: r });
                else {
                  var h = d.IPClassify_error_bookmakCannotBeCreated;
                  n(h);
                }
              } else n();
            });
          });
        },
        _createLabel: function (e, t, i) {
          var n = UWA.createElement("span", {
            class: "label",
            styles: { color: "black", fontSize: 12, margin: -4 },
          }).inject(i);
          n = UWA.createElement("span", { text: e }).inject(n);
          t &&
            UWA.createElement("div", {
              class: "attrMand fonticon fonticon-asterisk-alt",
              styles: {
                fontSize: 7,
                position: "relative",
                color: "orange",
                "vertical-align": "super",
              },
            }).inject(i);
        },
      });
    }
  ),
  define(
    "DS/IPClassifyActive/InputFieldAbstract",
    [
      "UWA/Controls/Abstract",
      "DS/Windows/ImmersiveFrame",
      "DS/Controls/LineEditor",
      "DS/Controls/Button",
      "DS/Utilities/Dom",
      "i18n!DS/IPClassifyActive/assets/nls/IPClassifyActive",
    ],
    function (e, t, i, n, o, a) {
      function s() {
        setTimeout(() => {
          this._immersiveFrame && this._immersiveFrame.hide();
        });
      }
      return e.extend({
        name: "tweakerClassif",
        _popover: null,
        values: [],
        init: function (e) {
          this._parent(e), this._buildInlineEditor(), this._applyTouchMode();
        },
        destroy: function () {
          this._parent.apply(this, arguments),
            this._popover &&
              (this._popover.nestedMenuInContext.destroy(),
              (this._popover = null),
              this._immersiveFrame.destroy(),
              (this._immersiveFrame = null));
        },
        _setValue: function (e) {
          var t = "";
          e.forEach((e, i) => {
            t += (0 === i ? "" : ",") + e.label;
          }),
            (this._internalTweakerString.value = t),
            (this.values = e),
            this.dispatchEvent("change");
        },
        _applyTouchMode: function () {
          this.elements._editBtn &&
            (this.elements._editBtn.touchMode = this.options.touchMode),
            this._internalTweakerString &&
              (this._internalTweakerString.touchMode = this.options.touchMode);
        },
        getValues: function () {
          var e = [];
          return this._internalTweakerString.value && (e = this.values), e;
        },
        _buildInlineEditor: function () {
          var e = this;
          (this.elements.container = UWA.createElement("div", {
            class: "wux-controls-abstract",
          })),
            (this.elements._internalView = UWA.createElement("div")),
            this.elements._internalView.addClassName(
              "IPClass-multirep-inline-editor"
            ),
            (this._internalTweakerString = new i({
              displayClearFieldButtonFlag: !0,
            })),
            this._internalTweakerString.addEventListener(
              "change",
              function (t) {
                this.value ||
                  (t.dsModel && t.dsModel.valueToCommit) ||
                  (e._setValue([]),
                  e._popover &&
                    e._popover.nestedMenuInContext.setActiveNode(null, null));
              }
            ),
            this._internalTweakerString.inject(this.elements._internalView),
            this.elements._internalView.inject(this.elements.container),
            this._internalTweakerString
              .getContent()
              .addClassName("IPClass-multirep-tweakerstring"),
            (this._internalTweakerString.elements.inputField.onkeydown = (
              e
            ) => {
              ("c" === e.key && e.ctrlKey) || e.preventDefault();
            }),
            (this._internalTweakerString.getContent().oncut = (e) =>
              e.preventDefault()),
            (this._internalTweakerString.getContent().onpaste = (e) =>
              e.preventDefault()),
            (this.elements._editBtn = new n({ label: a.btn_browse })),
            this.elements._editBtn
              .getContent()
              .addClassName("IPClass-multirep-button"),
            this.elements._editBtn.inject(this.elements._internalView),
            this._applyAugmentedDisplay();
        },
        _applyAugmentedDisplay: function () {
          this.elements._editBtn &&
            o.addEventOnElement(this, this.elements._editBtn, "click", (e) => {
              e.stopPropagation(),
                require([this.options.panelRequire], (e) => {
                  if (!this._popover) {
                    if (!this._immersiveFrame) {
                      this._immersiveFrame = new t({
                        identifier: "ClassifFrame",
                      });
                      var i = 0;
                      widget.body.getChildren().forEach((e) => {
                        var t = e.getStyle("z-index");
                        !isNaN(t) && t > i && (i = t);
                      }),
                        this._immersiveFrame
                          .getContent()
                          .setStyle("z-index", i),
                        this._immersiveFrame.inject(widget.body);
                    }
                    (this._popover = new e({
                      _immersiveFrame: this._immersiveFrame,
                    })),
                      this._popover.addEvent("onSetActive", (e) => {
                        var t = [];
                        e.getID &&
                          (t = [{ label: e.getLabel(), id: e.getID() }]),
                          this._setValue(t),
                          s.call(this);
                      }),
                      this._popover.addEvent("onCancel", s.bind(this));
                  }
                  var n = this.getValues();
                  n.length > 0 &&
                    this._popover.nestedMenuInContext._activeBookmarkId !==
                      n[0].id &&
                    this._popover.nestedMenuInContext.setActiveNode(
                      n[0].id,
                      n[0].label
                    ),
                    this._popover.showPopover(),
                    this._immersiveFrame.show();
                });
            });
        },
      });
    }
  ),
  define(
    "DS/IPClassifyActive/dataProvider/IPClassifyActiveFindInProvider",
    [
      "UWA/Class",
      "DS/Core/PointerEvents",
      "DS/IPClassifyUtil/IPClassifyAlert",
      "i18n!DS/IPClassifyActive/assets/nls/IPClassifyActive",
    ],
    function (e, t, i, n) {
      "use strict";
      return e.singleton({
        NlsTree: {},
        autoCompleteKey: "IPClassifyActive_FindIn_savedQueries",
        supportedSection: "",
        unSupportedSection: "",
        getSupportedTypesForDrop: function () {
          return this.supportedTypesForDrop || [];
        },
        setTreeSections: function (e) {
          (this.supportedSection = e.allSection),
            (this.unSupportedSection = e.favoriteSection),
            (this.supportedTypesForDrop = e.supportedTypesForDrop);
        },
        setFindInTreeNLS: function (e) {
          this.NlsTree = e;
        },
        setTreeGridView: function (e) {
          this._treeView = e;
        },
        getTreeGridView: function () {
          return this._treeView;
        },
        getTreeVisibleNodesyId: function (e) {
          return this._getVisibleNodeById(e);
        },
        getSupportedSections: function (e) {
          let t = this._treeView.getTreeDocument().getRoots(),
            i = [];
          return (
            t.forEach((t) => {
              t.getID() === this.unSupportedSection ||
                (e && e !== t.plmType) ||
                i.push(t);
            }),
            i
          );
        },
        getTreeSectionNodeForFind: function (e) {
          let t = this.getSupportedSections(e);
          return t.length > 0 ? t[0] : null;
        },
        setTreeElementToAddCommand: function (e) {
          return (this._ipClassifyActiveContainer = e);
        },
        getTreeElementToAddCommand: function () {
          return this._ipClassifyActiveContainer;
        },
        getTreeSelectedNode: function () {
          var e = this._treeView.getTreeDocument().getSelectedNodes();
          return (
            (this._currentSelectionNode = 1 == e.length ? e[0] : void 0),
            this._currentSelectionNode
          );
        },
        setWebServiceRef: function (e) {
          this._webServiceRef = e;
        },
        expandTreeAndSelectNode: function (e) {
          this.expandTreeAndSelectFolder(e);
        },
        findTreeNodes: function (e) {
          this._webServiceRef.fetchObjectsForFindInTree(e);
        },
        setSortFun: function (e) {
          this._applySort = e;
        },
        setModelEvents: function (e) {
          this._modelEvents = e;
        },
        onCurrentSelectionChange: function (e) {
          this._modelEvents.subscribe(
            { event: "IPActive/NodeSelection/OnChange" },
            e
          );
        },
        onTreeFilterChange: function (e) {},
        expandTreeAndSelectFolder: function (e) {
          var o = this,
            a =
              e && e.folderToFindAndSelect
                ? e.folderToFindAndSelect.ids
                : void 0,
            s = [],
            r = !0;
          if (
            (e.findInTree &&
              r &&
              ((this.findInTree = !0),
              e.findInTreeLoader.setMessage(o.NlsTree.loader.formulatingPaths),
              e.findInTreeLoader.loader.addEventListener(
                t.POINTERHIT,
                function (t) {
                  if (((r = !1), e.onComplete)) {
                    var i = { folderToFindAndSelect: e.folderToFindAndSelect };
                    e.onComplete(i);
                  }
                }
              )),
            a && r)
          ) {
            s = JSON.parse(JSON.stringify(a));
            for (var l = 0; l < a.length; l++) {
              if (o._getVisibleNodeById(a[l])) {
                const e = s.indexOf(a[l]);
                if ((e > -1 && s.splice(e, 1), !r)) break;
              }
            }
            if (s.length > 0 && r) {
              o._webServiceRef.pathInActiveBookmark({
                ids: a,
                onComplete: function (t) {
                  var i = t.result,
                    n = {},
                    s = {};
                  i.forEach(function (e) {
                    var t =
                        e.outputs &&
                        e.outputs.GET_PARENT &&
                        e.outputs.GET_PARENT[0],
                      i = e.input_object,
                      o = i.physicalid;
                    if (((s[o] = {}), UWA.extend(s[o], i), t)) {
                      var a = t.physicalid;
                      if (t.paths) {
                        var r = t.paths.filter(function (e) {
                          return e[0].physicalid === o;
                        })[0];
                        if (r && r.length > 1) {
                          a = r[1].physicalid;
                          for (var l = r.length - 1; l >= 0; l--) {
                            a = t;
                            l !== r.length - 1 && (a = r[l + 1]),
                              (n[r[l].physicalid] = {}),
                              UWA.extend(n[r[l].physicalid], a);
                          }
                        } else (n[o] = {}), UWA.extend(n[o], t);
                      }
                    }
                  });
                  var l = [],
                    d = function (e) {
                      var t = e.physicalid;
                      if (n[t]) {
                        var i = o.getNoFavoriteNodeModelById(t),
                          a = n[t].physicalid,
                          s = o.getNoFavoriteNodeModelById(a);
                        if (i) {
                          if (!i.isVisible())
                            for (l.push(s); s && !s.isVisible(); )
                              (s = s.getParent()) && l.push(s);
                          return i;
                        }
                        if (!s && !(s = d(n[t]))) return;
                        var r = o.addNewNode(e, s);
                        for (l.push(s); s && !s.isVisible(); )
                          (s = s.getParent()) && l.push(s);
                        return r;
                      }
                    };
                  if (
                    (a.forEach(function (e) {
                      s[e] && r && d(s[e]);
                    }),
                    e.onComplete && r)
                  ) {
                    o._applySort(),
                      l.forEach(function (e) {
                        e.expand({ onlyUI: !0 }),
                          e.setState({ state: "partiallyExpanded" });
                      });
                    var c = { folderToFindAndSelect: e.folderToFindAndSelect };
                    e.onComplete(c);
                  }
                },
                onFailure: function (t) {
                  i.displayNotification({
                    message: o.NlsTree.msg_error_while_formulating_paths
                      ? o.NlsTree.msg_error_while_formulating_paths
                      : n.msg_error_while_formulating_paths,
                    className: "error",
                  }),
                    e.onFailure && e.onFailure();
                },
              });
            } else if (e.onComplete) {
              var d = { folderToFindAndSelect: e.folderToFindAndSelect };
              e.onComplete(d);
            }
          } else
            console.error(
              "No input given to expandTreeAndSelectFolder: 'options.folderToFindAndSelect' is undefined !"
            ),
              e.onFailure && e.onFailure();
        },
        _getVisibleNodeById: function (e, t) {
          var i = this,
            n = t ? [t] : i.getSupportedSections(),
            o = null;
          return (
            n.every((t) => {
              if (t && t.isVisible())
                if (t.getID() === e) o = t;
                else if (t.isExpanded()) {
                  var n = t.getChildren();
                  if (Array.isArray(n) && n.length >= 1)
                    for (var a = 0; a < n.length; a++) {
                      var s = n[a];
                      if (s) {
                        if (s.id == e) {
                          o = s;
                          break;
                        }
                        var r = i._getVisibleNodeById(e, s);
                        if (r && r.isVisible()) {
                          o = r;
                          break;
                        }
                      }
                    }
                }
              return !o;
            }),
            o
          );
        },
      });
    }
  ),
  define(
    "DS/IPClassifyActive/utils/IPClassifyActiveCommunication",
    ["DS/PlatformAPI/PlatformAPI"],
    function (e) {
      "use strict";
      return {
        publish: function (t, i, n) {
          e.publish(t, i),
            !1 !== n &&
              (e.publish(
                "webinwin:web:notification",
                JSON.stringify({ event: t, content: i })
              ),
              "undefined" != typeof dscef &&
                dscef.sendString(JSON.stringify({ event: t, content: i })));
        },
        subscribe: function (t, i, n) {
          var o = window,
            a = function () {
              if (o === top || o.parent) return i.apply(this, arguments);
              throw new Exception("widget has been destroyed");
            };
          e.subscribe(t, a),
            !1 !== n &&
              e.subscribe("webinwin:win:notification", function (e) {
                "string" == typeof e && (e = JSON.parse(e)),
                  e && e.event === t && a(e.content);
              });
        },
      };
    }
  ),
  define(
    "DS/IPClassifyActive/utils/WidgetDataProvider",
    ["UWA/Controls/Abstract"],
    function (e) {
      return e.singleton({
        _eventNameProvider: null,
        _DataProvider: null,
        setDataProvider: function (e) {
          e.widgetEventsProvider &&
            (this._eventNameProvider = e.widgetEventsProvider),
            e.widgetDataProvider && (this._DataProvider = e.widgetDataProvider);
        },
        getEventNameProvider: function () {
          return this._eventNameProvider;
        },
        getDataProvider: function () {
          return this._DataProvider;
        },
      });
    }
  ),
  define(
    "DS/IPClassifyActive/NestedMenu/NestedMenu",
    [
      "UWA/Controls/Abstract",
      "UWA/Class/Options",
      "UWA/Class/Events",
      "DS/Tree/Tree",
      "DS/TreeModel/TreeDocument",
      "DS/TreeModel/TreeNodeModel",
      "DS/Tree/TreeNodeView",
      "DS/UIKIT/Spinner",
      "DS/UIKIT/Mask",
      "DS/DataGridView/DataGridView",
      "DS/Core/PointerEvents",
      "DS/PADUtils/Configuration",
      "DS/DataGridView/DataGridViewLayout",
      "DS/WidgetServices/WidgetServices",
      "DS/IPClassifyUtil/IPClassifyAlert",
      "i18n!DS/IPClassifyActive/assets/nls/IPClassifyActive",
    ],
    function (e, t, i, n, o, a, s, r, l, d, c, u, h, p, f, _) {
      "use strict";
      return e.extend(t, i, {
        name: "NestedMenu",
        _historyStore: {},
        _historyCursor: [],
        _historyNodeModel: {},
        _searchKeys: {},
        _inSearchPanel: !1,
        _sortField: "alpha",
        _sortBy: "desc",
        _parentMap: {},
        _highlightId: "",
        _expandedNodes: [],
        init: function (e) {
          (this._master = e.master),
            (this._onPreExpandTreeNode = e.onPreExpandTreeNode),
            (this._onPreCollapseTreeNode = e.onPreCollapseTreeNode),
            (this._onInfiniteScrollAsync = e.onInfiniteScrollAsync),
            (this._firstScreenFetch = e.firstScreenFetch),
            (this._populateFavoriteSection = e.populateFavoriteSection),
            (this._onContextualMenuReady = e.onContextualMenuReady),
            (this._fetchData = e.fetchData),
            (this._generateParentChain = e.generateParentChain),
            (this._getPath = e.getPath),
            (this._sortByReload = e.sortByReload),
            (this._searchData = e.searchData),
            (this._sortFunction = e.sortFunction),
            (this._shouldDisplayTooltip = e.shouldDisplayTooltip),
            (this._couldBeSelected = e.couldBeSelected),
            (this._msg_empty = e.showMessageWhileEmpty),
            (this._msg_resultEmpty = e.showMessageWhileResultEmpty),
            (this._allSection = e.allSection),
            (this.nestedMenuContainer = e.container),
            (this.multiSelectionComponent = e.multiSelectionComponent),
            (this._modelEvents = e.modelEvents),
            (this.selectionMode = e.selectionMode
              ? e.selectionMode
              : "DefaultMulti"),
            (this.isRootSelectable = e.isRootSelectable),
            (this.hideFavorite = e.hideFavorite);
        },
        render: function () {
          this._buildSkeleton();
        },
        initData: function () {
          this._initPromise = new Promise((e) => {
            (this._isInitializedData = !0),
              this.initNavigator("", []),
              this._maskIt("init", this.nestedMenuContainer),
              this.hideEmptyView(),
              this._firstScreenFetch().then((t, i) => {
                this.NavigatorTo(i && i.options.id, t),
                  this.multiSelectionComponent && this.expandMainSection(),
                  window.jasmine ||
                    this.hideFavorite ||
                    this._populateFavoriteSection(),
                  this._unmaskIt("init", this.nestedMenuContainer),
                  e();
              });
          });
        },
        _maskIt: function (e, t, i) {
          t.nmMasks || (t.nmMasks = { keys: {} }),
            (t.nmMasks.keys[e] = i),
            this._maskNext(t);
        },
        _unmaskIt: function (e, t) {
          t.nmMasks &&
            t.nmMasks.keys.hasOwnProperty(e) &&
            (delete t.nmMasks.keys[e], this._maskNext(t));
        },
        _maskNext: function (e) {
          let t = e.nmMasks.inUse;
          if (!t || !e.nmMasks.keys.hasOwnProperty(t)) {
            let t = Object.keys(e.nmMasks.keys);
            if (t.length > 0) {
              let i = t[0];
              (e.nmMasks.inUse = i), l.mask(e, e.nmMasks.keys[i]);
            } else l.unmask(e), delete e.nmMasks;
          }
        },
        expandMainSection: async function () {
          return (
            await this._initPromise,
            new Promise((e) => {
              var t = this.getNoFavoriteNodeModelById(this._allSection);
              t.isExpanded()
                ? e()
                : (this._modelEvents.subscribeOnce(
                    { event: "onPostExpand:" + this._allSection },
                    e
                  ),
                  t.expand());
            })
          );
        },
        updateView: function () {
          var e = this;
          setTimeout(function () {
            e._getCurrentCursor() || e._treeView.setScrollerPosition({});
          }, 10);
        },
        updateColumnToFit: function (e) {
          this._treeView && this._treeView.sizeColumnToFit("tree", e);
        },
        setActiveNode: function (e) {
          (this._activeBookmarkId = e),
            this._treeView.updateView({
              updateCellContent: !0,
              updateCellLayout: !0,
              updateCellAttributes: !0,
            });
        },
        selectHighlight: function () {
          if (this._highlightId)
            this.getNoFavoriteNodeModelById(this._highlightId) ||
              this.getNodeModelById(this._highlightId);
        },
        focusToActiveView: function (e) {
          var t = this;
          this._getCurrentCursor() &&
            0 === this._getCurrentCursor().indexOf("search") &&
            this.endSearch(),
            setTimeout(function () {
              if (t.getNoFavoriteNodeModelById(e)) {
                l.mask(t.nestedMenuContainer.getParent());
                var i = t.getNoFavoriteNodeModelById(e);
                i.isVisible() ||
                  (function (e) {
                    for (
                      var i = e.getParent(), n = i.plmType;
                      n !== t._allSection;

                    ) {
                      if (i.isVisible())
                        return void (
                          i.isExpanded() || i.expand({ onlyUI: !0 })
                        );
                      i.isExpanded() || i.expand({ onlyUI: !0 }),
                        (n = (i = i.getParent()).plmType);
                    }
                    n === t._allSection &&
                      (i.isExpanded() || i.expand({ onlyUI: !0 }));
                  })(i),
                  i
                    ? (t._treeView.unselectAll(),
                      t
                        ._getPath(e)
                        .then(function () {
                          i.select();
                          var e = t._treeView.layout.getRowPosition(i._rowID);
                          t._treeView.setScrollerPosition({ y: e }),
                            l.unmask(t.nestedMenuContainer.getParent());
                        })
                        .catch(function () {
                          console.log("Active bookmark is not accessible"),
                            l.unmask(t.nestedMenuContainer.getParent()),
                            t.isRootSelectable ? i.select() : i.remove();
                          var e = i.pathId.split(","),
                            n = e[e.length - 2];
                          t.getNoFavoriteNodeModelById(n) &&
                            t.getNoFavoriteNodeModelById(n).select();
                        }))
                    : (t._highlightId = e);
              } else
                t._getCurrentCursor() ||
                  (l.mask(t.nestedMenuContainer.getParent()),
                  Promise.all([t._getPath(e)])
                    .then(function (i) {
                      var n = i[0];
                      if (
                        (t._updateParentMap(n),
                        t._parentMap && Object.keys(t._parentMap).length > 1)
                      ) {
                        var o = t._parentMap.current.parentId;
                        o = t._parentMap[o];
                      }
                      var a = t.getNoFavoriteNodeModelById(e);
                      if (o && !a) {
                        for (
                          var s, r = t._parentMap[e].parentId, d = o.parentId;
                          !s && d;

                        )
                          (s = t.getNoFavoriteNodeModelById(d)) ||
                            (d = t._parentMap[d].parentId);
                        Promise.all([
                          t._fetchData(r),
                          t._generateParentChain(e, s, t._parentMap),
                        ]).then(function (i) {
                          var n = i[0],
                            o = i[1],
                            a = t._treeView.getTreeDocument();
                          a.prepareUpdate(),
                            o.expand({ onlyUI: !0 }),
                            [].concat(n).forEach(function (e) {
                              o.addChild(e);
                            }),
                            a.pushUpdate();
                          var s = t.getNoFavoriteNodeModelById(e);
                          t._treeView.unselectAll(), s.select();
                          var r = t._treeView.layout.getRowPosition(s._rowID);
                          t._treeView.setScrollerPosition({ y: r }),
                            l.unmask(t.nestedMenuContainer.getParent());
                        });
                      } else {
                        if (
                          (a && o && o.parentId === t._getCurrentCursor()
                            ? t._treeView.unselectAll()
                            : 1 === t._historyCursor.length &&
                              (t._historyCursor = []),
                          a)
                        ) {
                          a.select();
                          var c = t._treeView.layout.getRowPosition(a._rowID);
                          t._treeView.setScrollerPosition({ y: c });
                        } else t._highlightId = e;
                        l.unmask(t.nestedMenuContainer.getParent());
                      }
                    })
                    .catch(function () {
                      l.unmask(t.nestedMenuContainer.getParent());
                    }));
            }, 50);
        },
        _updateActiveInfo: function (e, t, i) {
          this.dispatchEvent("updateActiveInfo", { id: e, label: t, type: i });
        },
        _updateParentMap: function (e) {
          for (var t in e) this._parentMap[t] = e[t];
          for (var t in (this._parentMap.current &&
            this._activeBookmarkId === this._parentMap.current.parentId &&
            this._updateActiveInfo(
              this._parentMap.current.parentId,
              this._parentMap.current.parentName,
              this._parentMap.current.parentType
            ),
          this._parentMap)) {
            var i = this._parentMap[t];
            this._historyNodeModel[i.parentId] = {
              id: i.parentId,
              label: i.parentName,
              type: i.parentType,
              getID: function () {
                return this.id;
              },
              getLabel: function () {
                return this.label;
              },
              getType: function () {
                return this.type;
              },
            };
          }
        },
        getNodeModelById: function (e) {
          return (
            e &&
            this._treeView
              .getTreeDocument()
              .getAllDescendants()
              .filter(function (t) {
                return t.getID && t.getID() === e;
              })[0]
          );
        },
        getNoFavoriteNodeModelById: function (e) {
          return (
            e &&
            this._treeView
              .getTreeDocument()
              .getAllDescendants()
              .filter(function (t) {
                return t.getID && t.getID() === e && !t.isFavorite;
              })[0]
          );
        },
        getDocument: function () {
          return this._treeView && this._treeView.getTreeDocument();
        },
        getTreeView: function () {
          return this._treeView;
        },
        initNavigator: function (e, t) {
          (t = [].concat(t)),
            (this._historyCursor && this._historyStore && this._treeView) ||
              ((this._historyStore = this._historyStore || {}),
              (this._historyStore[e] = t),
              (this._historyCursor = this._historyCursor || [e]));
        },
        clearNavigator: function () {
          this._historyCursor = [];
        },
        NavigatorBack: function () {
          var e = this._getCurrentCursor(),
            t = this._parentMap[e],
            i =
              (t && this._historyStore[t.parentId]) ||
              (e && -1 !== e.indexOf("search"));
          if (this._historyCursor.length > 1 || i) {
            var n = this._historyCursor.pop();
            (this._previousSearch = null),
              n &&
                0 === n.indexOf("search") &&
                (this._previousSearch = { id: n, key: this._searchKeys[n] }),
              (this._highlightId = n);
            var o = this._historyCursor[this._historyCursor.length - 1],
              a = this._historyStore[o],
              s = this._treeView.getTreeDocument();
            s.prepareUpdate(),
              s.removeRoots(),
              this._checkViewDisplay(o, a),
              a.forEach(function (e) {
                s.addRoot(e);
              }),
              this._treeView.updateView(),
              s.pushUpdate(),
              this.updateView(),
              this.dispatchEvent("Navi_Back", {
                nodeModel: this._historyNodeModel[o],
                nodeId: o,
                path: this._getFullPath(this._historyNodeModel[o]),
              }),
              this.selectHighlight();
          }
        },
        _getFullPath: function (e) {
          var t = this;
          if (!e || !e.id) return [];
          var i = [{ id: e.getID(), label: e.getLabel() }];
          return (
            (function e(n) {
              var o = t._parentMap[n.parentId];
              o &&
                o.parentId &&
                (i.unshift({
                  id: o.parentId,
                  label: o.parentName,
                  type: o.parentType,
                }),
                e(o));
            })({ parentId: e.getID(), parentName: e.getLabel() }),
            i
          );
        },
        addMoreModelInExisted: function (e, t) {
          var i = this._treeView.getTreeDocument();
          (t = [].concat(t)),
            (this._historyStore[e] = this._historyStore[e].concat(t)),
            e === this._getCurrentCursor() &&
              (i.prepareUpdate(),
              t.forEach(function (e) {
                e && i.addRoot(e);
              }),
              this._treeView.updateView({
                updateCellContent: !0,
                updateCellLayout: !0,
                updateCellAttributes: !0,
              }),
              i.pushUpdate(),
              this.selectHighlight());
        },
        NavigatorTo: function (e, t) {
          var i = this;
          e !== this._getCurrentCursor() && (this._previousSearch = null),
            (i._historyNodeModel[e] =
              this.getNodeModelById(e) || i._historyNodeModel[e]);
          var n = this._treeView.getTreeDocument();
          (t = t || this._historyStore[e]),
            n.prepareUpdate(),
            n.removeRoots(),
            t &&
              ((t = [].concat(t)),
              this._checkViewDisplay(e, t),
              t.forEach(function (e) {
                e && n.addRoot(e),
                  e.getID &&
                    e.getID() &&
                    e.getID() === i._activeBookmarkId &&
                    i._updateActiveInfo(e.getID(), e.getLabel(), e.getType());
              }),
              (i._historyStore[e] = t)),
            i._treeView.pushUpdateView(),
            n.pushUpdate(),
            this.updateView(),
            (e &&
              i._getCurrentCursor() === e &&
              -1 !== i._historyCursor.indexOf(e)) ||
              (i._historyCursor && i._historyCursor.push(e)),
            i.dispatchEvent("Navi_To", {
              nodeModel: this._historyNodeModel[e],
              nodeId: e,
              path: this._getFullPath(this._historyNodeModel[e]),
            }),
            i.selectHighlight();
        },
        _checkViewDisplay: function (e, t) {
          if (t && 0 !== t.length) this.hideEmptyView();
          else {
            var i =
              e && 0 === e.indexOf("search")
                ? this._msg_resultEmpty
                : this._msg_empty;
            this.showEmptyView(i);
          }
        },
        applySort: function (e, t, i) {
          (this._sortField = e || this._sortField),
            (this._sortBy = t || this._sortBy);
          var n = this,
            o =
              this._getCurrentCursor() && "date" === this._sortField
                ? "asc"
                : this._sortBy;
          this._sortField;
          this._treeView &&
            ((this._treeView.getColumnOrGroup("tree").compareFunction =
              function (e, t, o, a, s) {
                return n._sortFunction
                  ? n._sortFunction(n._sortField, n._sortBy, o, a)
                  : i(o, a);
              }),
            (this._treeView.sortModel = [{ dataIndex: "tree", sort: o }]),
            this._treeView.reapplySortModel());
        },
        applySortByReload: function (e, t) {
          var i = this,
            n = this._getCurrentCursor();
          this._sortByReload &&
            (l.mask(this.nestedMenuContainer),
            this._sortByReload(n, e, t).then(function (o) {
              o && i.NavigatorTo(n, o),
                i.applySort(e, t),
                l.unmask(i.nestedMenuContainer);
            }));
        },
        _getCurrentCursor: function () {
          return this._historyCursor && this._historyCursor.slice(-1)[0];
        },
        _getPidOfSearch: function (e) {
          var t = this._historyCursor.indexOf(e);
          return t < 1 ? void 0 : this._historyCursor[t - 1];
        },
        startSearch: function () {
          (this._inSearchPanel = !0),
            document.body.addEventListener(
              "keydown",
              this.escKeyEvent.bind(this)
            );
          var e = "search" + Date.now();
          this._previousSearch && (e = this._previousSearch.id),
            this.switchColumn(!0),
            this.NavigatorTo(e);
        },
        endSearch: function () {
          (this._inSearchPanel = !1),
            document.body.removeEventListener(
              "keydown",
              this.escKeyEvent.bind(this)
            );
          var e = this._getCurrentCursor();
          e && 0 === e.indexOf("search") && this.NavigatorBack(),
            this.switchColumn(!1);
        },
        escKeyEvent: function (e) {
          "27" == (e.keyCode || e.which) && this.endSearch();
        },
        switchColumn: function (e) {
          var t = this;
          e
            ? (this._treeView.layout.setColumnVisibleFlag("tree", !1),
              this.nestedMenuContainer.addClassName("searchPage"),
              this._treeView.getColumnOrGroup("label")
                ? (this._treeView.layout.setColumnVisibleFlag("label", !0),
                  this._treeView.layout.setColumnVisibleFlag("tooltip", !0))
                : (this._treeView.addColumnOrGroup({
                    text: "label",
                    dataIndex: "label",
                    width: "auto",
                    minWidth: 0,
                    editableFlag: !1,
                    onCellRequest: function (e) {
                      t.onCellRequest(e);
                      var i = UWA.createElement("div", {
                          class: "search-node",
                        }),
                        n = e.nodeModel.getIcons()[0].iconName;
                      UWA.createElement("span", {
                        class: n,
                        styles: {
                          width: "20px",
                          "min-width": "20px",
                          margin: "auto 10px",
                        },
                      }).inject(i),
                        UWA.createElement("div", {
                          text: e.nodeModel.getLabel(),
                          class: "wux-tweakers-string-label",
                        }).inject(i);
                      e.cellView.getContent().setContent(i);
                    },
                  }),
                  this._treeView.addColumnOrGroup({
                    text: "tooltip",
                    dataIndex: "tooltip",
                    width: 30,
                    minWidth: 30,
                    editableFlag: !1,
                    resizableFlag: !1,
                    onCellRequest: function (e) {
                      t.onCellRequest(e);
                      var i = UWA.createElement("i", {
                        class: "wux-ui-3ds wux-ui-3ds-1x wux-ui-3ds-help",
                        styles: { color: "#b4b6ba", margin: "auto" },
                      });
                      t._treeView.layout.setColumnWidth("tooltip", 30),
                        e.cellView.getContent().setContent(i),
                        (e.dataIndex = "tooltip"),
                        this.defaultOnCellRequest(e);
                    },
                  })))
            : (this._treeView.layout.setColumnVisibleFlag("tree", !0),
              this._treeView.layout.setColumnVisibleFlag("label", !1),
              this._treeView.layout.setColumnVisibleFlag("tooltip", !1),
              this.nestedMenuContainer.removeClassName("searchPage"));
        },
        search: function (e) {
          var t = this;
          return new Promise(function (i, n) {
            l.mask(t.nestedMenuContainer),
              t.hideEmptyView(),
              setTimeout(function () {
                t._treeView.setScrollerPosition({});
              }, 10);
            var o = t._getCurrentCursor();
            (o && 0 === o.indexOf("search")) || (o = "search" + Date.now());
            var a = t._getPidOfSearch(o);
            t._searchData(e, o, a).then(
              function (n) {
                (t._searchKeys[o] = e),
                  n && t.NavigatorTo(o, n),
                  l.unmask(t.nestedMenuContainer),
                  i();
              },
              function (e) {
                let i = _.msg_error_search;
                e &&
                  e.error &&
                  e.error.errorCode &&
                  "FS_INVALID_QUERY__NOT_AUTHORIZED_QUERY__WILDCARD" ===
                    e.error.errorCode &&
                  (i = _.msg_error_invalid_wildcard),
                  f.displayNotification({ message: i, className: "error" }),
                  l.unmask(t.nestedMenuContainer),
                  n();
              }
            );
          });
        },
        onInfiniteScrollAsync: function (e) {
          var t = this,
            i = t._getCurrentCursor();
          this._onInfiniteScrollAsync(i, e).then(function (e) {
            var n = e.children,
              o = e.sort;
            t.addMoreModelInExisted(i, n),
              i === t._getCurrentCursor() &&
                o &&
                t.applySort(o.sortField, o.sortBy),
              t._treeView.infiniteScrollProcessDone();
          });
        },
        getSearchKeyById: function (e) {
          return this._searchKeys[e];
        },
        showEmptyView: function (e) {
          this.emptyView.show(),
            (this.emptyView.innerText = e || this._msg_empty);
        },
        hideEmptyView: function () {
          this.emptyView.hide();
        },
        _onPreCollapse: function (e) {
          var t = this._getCurrentCursor();
          e.target.id;
          return this._onPreCollapseTreeNode(e, t);
        },
        _onPreExpand: function (e) {
          if (!e.data.onlyUI) {
            var t = this._getCurrentCursor();
            return this._onPreExpandTreeNode(e, t);
          }
          e.target.preExpandDone();
        },
        _onPostCollapse: function (e) {
          var t = e.target.id;
          this.removeExpandedNodes(t), this.updateColumnToFit();
        },
        _onPostExpand: function (e) {
          var t = e.target.id;
          this.addExpandedNodes(t),
            this.attachExpandedNodes(t),
            this.updateColumnToFit({ computeAllRows: !0 }),
            this._modelEvents.publish({ event: "onPostExpand:" + t, data: e });
        },
        addExpandedNodes: function (e) {
          let t = this.getNoFavoriteNodeModelById(e);
          this._expandedNodes[e] = t ? t.getChildren() : void 0;
        },
        removeExpandedNodes: function (e) {
          delete this._expandedNodes[e];
        },
        attachExpandedNodes: function (e) {
          var t = this.getNoFavoriteNodeModelById(e).getChildren(),
            i = this;
          t.forEach(function (e) {
            var t = i._expandedNodes[e.id];
            t &&
              ([].concat(t).forEach(function (t) {
                i.getNoFavoriteNodeModelById(t.id) || e.addChild(t);
              }),
              "partiallyExpanded" !== e.getState() && e.expand({ onlyUI: !0 }));
          });
        },
        _buildSkeleton: function () {
          var e = this,
            t = UWA.clone(u.getConfig("dgvOptions")),
            i = t && t.dgv ? t.dgv : {},
            n = UWA.extend(
              { identifier: "DataGridView-" + e.name + "-" + widget.id },
              i
            );
          (n.treeDocument = new o()),
            (n.columns = [
              {
                dataIndex: "tree",
                autoRowHeightFlag: !0,
                allowUnsafeHTMLContent: !0,
                editableFlag: !1,
                compareFunction: function (t, i, n, o, a) {
                  return e._sortFunction(e._sortField, e._sortBy, n, o);
                },
                onCellRequest: function (t) {
                  e.onCellRequest(t), this.defaultOnCellRequest(t);
                },
              },
            ]);
          var a = UWA.clone(t.dgvLayout);
          e.multiSelectionComponent && (a = {}),
            (n.layout = new h(a)),
            (n.layout.columnsHeader = !1),
            (n.layout.cellHeight = 38),
            (n.layout.useLastColumnWidthAutoFlag = !0),
            (n.treeNodeCellOptions = n.treeNodeCellOptions || {}),
            (n.treeNodeCellOptions.expanderStyle = "triangle"),
            (n.treeNodeCellOptions.depthIncrement = 14),
            (n.useAsyncPreExpand = !0),
            e.multiSelectionComponent
              ? ((n.selectionBehavior = {
                  canInteractiveMultiselectWithCheckboxClick: !0,
                  canMultiSelect: !0,
                  enableFeedbackForActiveCell: !0,
                  enableShiftSelection: !0,
                  toggle: !1,
                  unselectAllOnEmptyArea: !0,
                }),
                (n.rowSelection =
                  "ONLYSingle" == this.selectionMode ||
                  "DefaultSingle" == this.selectionMode
                    ? "single"
                    : "multiple"),
                "DefaultMulti" == this.selectionMode &&
                  ((n.selectionBehavior.toggle = !0),
                  (n.showSelectionCheckBoxesFlag = !0)))
              : ((n.selectionBehavior = n.selectionBehavior || {}),
                (n.selectionBehavior.unselectAllOnEmptyArea = !1),
                (n.selectionBehavior.toggle = p.isTouchDevice()),
                (n.selectionBehavior.selectNodeModelOnCellSelection = !0),
                (n.cellPreselectionFeedback = !1),
                (n.cellDragEnabledFlag = !1),
                (n.cellActivationFeedback = "none"),
                (n.rowSelection = "single"),
                (n.showSelectionCheckBoxesFlag = !1)),
            (n.cellSelection = !1),
            (n.rowDragEnabledFlag = !1),
            (n.showRowIndexFlag = !1),
            (n.showOutlineFlag = !1),
            (n.showRowHeaderBackgroundFlag = !1),
            (n.showAlternateBackgroundFlag = !1),
            (n.showRowBorderFlag = !1),
            (n.getCellTooltip = function (t) {
              return e._shouldDisplayTooltip && e._shouldDisplayTooltip(t);
            }),
            (n.onContextualEvent = function (t) {
              return !e._inSearchPanel && e._onContextualMenuReady(t);
            }),
            (n.onDragStartCell = function () {}),
            (n.onDragEndCell = function () {}),
            (n.onDragLeaveCell = function () {}),
            (n.onDragOverCell = function () {}),
            (n.onDropCell = function () {}),
            (n.onDragEnterBlank = function () {}),
            (n.onDragLeaveBlank = function () {}),
            (n.onDragOverBlank = function () {}),
            (n.onDropBlank = function () {}),
            (n.shouldAcceptDrag = function () {
              return !1;
            }),
            (n.shouldAcceptDrop = function () {
              return !1;
            });
          var s = new d(n).inject(this.nestedMenuContainer);
          (this._treeView = s), (this._manager = this._manager || s);
          this._treeView.onInfiniteScroll(function (t) {
            !(function (t) {
              t.reachBottom &&
              e._onInfiniteScrollAsync &&
              "function" == typeof e._onInfiniteScrollAsync
                ? e.onInfiniteScrollAsync(t)
                : e._treeView.infiniteScrollProcessDone();
            })(t);
          }),
            s
              .getTreeDocument()
              .getXSO()
              .onAdd(function (t) {
                e.dispatchEvent("select", t);
              }),
            e.multiSelectionComponent &&
              (s
                .getTreeDocument()
                .getXSO()
                .onRemove(function (t) {
                  e.dispatchEvent("unSelect", t);
                }),
              s
                .getTreeDocument()
                .getXSO()
                .onChange(function (t) {
                  0 == s.getTreeDocument().getSelectedNodes().length &&
                    e.dispatchEvent("clearSelection");
                })),
            s
              .getTreeDocument()
              .getXSO()
              .onChange(function (t) {
                e._modelEvents.publish({
                  event: "IPActive/NodeSelection/OnChange",
                });
              }),
            this._onPreExpandTreeNode &&
              "function" == typeof this._onPreExpandTreeNode &&
              s.getTreeDocument().onPreExpand(this._onPreExpand.bind(this)),
            this._onPreCollapseTreeNode &&
              "function" == typeof this._onPreCollapseTreeNode &&
              s.getTreeDocument().onPreCollapse(this._onPreCollapse.bind(this)),
            s.getTreeDocument().onPostCollapse(this._onPostCollapse.bind(this)),
            s.getTreeDocument().onPostExpand(this._onPostExpand.bind(this)),
            (s.getTreeDocument().options.shouldBeSelected = function (t) {
              return !e._couldBeSelected || e._couldBeSelected(t);
            }),
            s.addEventListener(c.POINTERDOWN, function (t, i) {
              2 === t.multipleHitCount &&
                i &&
                -1 !== i.rowID &&
                e._switchExpandStatus(i);
            }),
            (this.emptyView = UWA.createElement("div", {
              class: "emptyView",
              styles: {
                height: "100%",
                position: "absolute",
                top: 0,
                left: 0,
                width: "100%",
                "text-align": "center",
                "padding-top": "5px",
              },
            }).inject(this.nestedMenuContainer)),
            this.hideEmptyView();
        },
        _switchExpandStatus: function (e) {
          "access" === e.dataIndex ||
            that._inSearchPanel ||
            (!0 === e.nodeModel._isExpanded
              ? e.nodeModel.collapse()
              : e.nodeModel.expand());
        },
        onCellRequest: function (e) {
          e.isHeader ||
            (e.nodeModel.options.isBeingCut
              ? e.cellView.getContent().addClassName("state-is-being-cut")
              : e.cellView.getContent().removeClassName("state-is-being-cut"));
          var t = e.nodeModel;
          (t.getID =
            t.getID ||
            function () {
              return this.options.id;
            }),
            (t.getType =
              t.getType ||
              function () {
                return this.options.plmType;
              }),
            (t.getLabel =
              t.getLabel ||
              function () {
                return this.options.label;
              }),
            -1 !== ["Content"].indexOf(e.nodeModel.getType())
              ? e.cellView.getContent().addClassName("couldNotBeSelected")
              : e.cellView.getContent().removeClassName("couldNotBeSelected"),
            "Path" === e.nodeModel.getType()
              ? e.cellView.getContent().addClassName("resultPath")
              : e.cellView.getContent().removeClassName("resultPath"),
            t.id === this._activeBookmarkId
              ? e.cellView.getContent().addClassName("isActivated")
              : e.cellView.getContent().removeClassName("isActivated");
        },
      });
    }
  ),
  define(
    "DS/IPClassifyActive/utils/IPClassifyActiveUtil",
    [
      "UWA/Utils",
      "DS/IPClassifyActive/utils/WidgetDataProvider",
      "DS/ZipJS/deflate",
      "DS/ZipJS/inflate",
    ],
    function (e, t) {
      return {
        getSetting: function () {
          return t.getDataProvider().getSettings();
        },
        jsonZip: function (t) {
          if (t.length > 0 && "{" !== t[0] && "[" !== t[0]) return t;
          for (
            var i = new ArrayBuffer(t.length), n = new Uint8Array(i), o = 0;
            o < t.length;
            o++
          )
            n[o] = t.charCodeAt(o);
          var a = new Deflater();
          a.append(n);
          var s = a.flush(),
            r = String.fromCharCode.apply(null, s);
          return e.base64Encode(r);
        },
        jsonUnzip: function (t) {
          if (t.length > 0 && ("{" === t[0] || "[" === t[0])) return t;
          if (t.length > 0 && 0 === t.indexOf("root,")) return "{}";
          var i = t;
          try {
            i = e.base64Decode(t);
          } catch (e) {}
          for (
            var n = new ArrayBuffer(i.length), o = new Uint8Array(n), a = 0;
            a < i.length;
            a++
          )
            o[a] = i.charCodeAt(a);
          var s = new Inflater(),
            r = s.append(o);
          return s.flush(), String.fromCharCode.apply(null, r);
        },
      };
    }
  ),
  define(
    "DS/IPClassifyActive/utils/EventsManager",
    [
      "DS/IPClassifyActive/utils/IPClassifyActiveCommunication",
      "DS/IPClassifyActive/utils/WidgetDataProvider",
    ],
    function (e, t) {
      return {
        notifyObjectSelection: function (i) {
          var n = t.getDataProvider(),
            o = t.getEventNameProvider();
          if (i && n && n.getObjectForPublication && o && o.selection) {
            var a = n.getObjectForPublication(i);
            a && e.publish(o.selection, { content: { current: a } });
          }
        },
      };
    }
  ),
  define(
    "DS/IPClassifyActive/NestedMenu/NestedMenuInContext",
    [
      "UWA/Controls/Abstract",
      "UWA/Class/Options",
      "UWA/Class/Events",
      "DS/WebappsUtils/WebappsUtils",
      "DS/WidgetServices/WidgetServices",
      "i18n!DS/IPClassifyActive/assets/nls/IPClassifyActive",
      "DS/IPClassifyActive/NestedMenu/NestedMenu",
      "DS/UIKIT/Input/Text",
      "DS/Windows/ImmersiveFrame",
      "DS/Windows/Dialog",
      "DS/Controls/Button",
      "DS/IPClassifyActive/utils/WidgetDataProvider",
      "DS/Windows/DockingElement",
      "DS/IPClassifyFindInContext/FindInTree",
      "DS/CoreEvents/ModelEvents",
      "DS/UIKIT/Mask",
      "DS/IPClassifyUtil/IPClassifyAlert",
      "DS/IPClassifyActive/dataProvider/IPClassifyActiveFindInProvider",
      "DS/IPClassifyActive/utils/IPClassifyActiveCtxSelectionPanel",
      "DS/Controls/Loader",
      "DS/Controls/ModalContainer",
    ],
    function (e, t, i, n, o, a, s, r, l, d, c, u, h, p, f, _, v, m, g, y, C) {
      "use strict";
      return e.extend(t, i, {
        name: "NestedMenuInContext",
        _selectBookmarkId: "",
        _selectBookmarkLabel: "",
        _currentBookmarkId: "",
        _currentBookmarkLabel: "",
        _selectBookmarkNodeModel: "",
        _activeBookmarkId: "",
        _activeBookmarkLabel: "",
        _isInitializedData: !1,
        _sortField: "alpha",
        _sortBy: "asc",
        _historySort: {},
        _currentCursor: "",
        _enableActive: !0,
        _modalMode: !1,
        isSearchActivated: !1,
        init: function (e) {
          (this.options = e),
            (this.container = this.options.container),
            (this._selectBtn = this.options.selectBtnLabel),
            (this._selectHereBtn = this.options.selectHereBtnLabel),
            (this._unselectBtn = this.options.unselectBtnLabel),
            (this._cancelBtn = this.options.cancelBtnLabel),
            (this._focusBtnTooltip = this.options.focusBtnTooltip),
            (this._deactivatedBtnTooltip = this.options.deactivatedBtnTooltip),
            (this._playBtnTooltip = this.options.playBtnTooltip),
            (this._pauseBtnTooltip = this.options.pauseBtnTooltip),
            (this.couldBeValid = this.options.couldBeValid),
            (this.getActiveIcon = this.options.getActiveIcon),
            (this.enableModalView = this.options.enableModalView),
            (this.enablePlayPause = this.options.enablePlayPause),
            (this.isChooserPanel = this.options.isChooserPanel),
            (this.multiSelectionComponent = e.multiSelectionComponent),
            (this.authoring = "boolean" == typeof e.authoring && e.authoring),
            (this.favoriteButton =
              "boolean" == typeof e.favoriteButton && e.favoriteButton),
            (this.selectionMode = e.selectionMode
              ? e.selectionMode
              : "DefaultMulti"),
            (this.dialogButtons =
              "boolean" == typeof e.dialogButtons && e.dialogButtons),
            (this.webServiceRef = e.webServiceRef),
            (this.handleClassification = e.handleClassification),
            (this._modelEvents = new f()),
            (this.options.modelEvents = this._modelEvents),
            this.isChooserPanel && (this._unselectBtn = this._selectBtn),
            (this.popOverDialogProperties = e.popOverDialogProperties),
            m.setModelEvents(this._modelEvents),
            m.setFindInTreeNLS(e.master._findInTreeNLS),
            m.setWebServiceRef(e.webServiceRef),
            m.setTreeSections({
              allSection: e.master._allSection,
              favoriteSection: e.master._favoriteSection,
              supportedTypesForDrop: e.master._supportedTypesForDropAtFindIn,
            }),
            (m.addNewNode = e.master.addNewNode.bind(e.master)),
            this._buildPopover(),
            (this.isSearchInitialized = !1);
        },
        _initNestedMenu: function () {
          var e = this;
          (this.nestedMenu = new s(
            Object.assign(this.options, { container: this.nestedMenuContainer })
          )),
            this.nestedMenu.addEvent("updateActiveInfo", function (t) {
              (e._activeBookmarkId = t.id),
                (e._activeBookmarkLabel = t.label),
                (e._activebookmarkSet = {
                  getID: function () {
                    return t.id;
                  },
                  getLabel: function () {
                    return t.label;
                  },
                  getType: function () {
                    return t.type;
                  },
                }),
                e.dispatchEvent("updateActiveInfo", e._activebookmarkSet);
            }),
            this.nestedMenu.addEvent("Navi_Back", function (t) {
              var i = t.nodeModel,
                n = t.nodeId;
              t.path;
              (e._currentBookmarkId = i && i.getID()),
                (e._currentBookmarkLabel = i && i.getLabel()),
                (e._currentCursor = n),
                (e._selectBookmarkId = ""),
                (e._selectBookmarkLabel = ""),
                (e._selectBookmarkNodeModel = ""),
                (e._currentBookmarkNodeModel = i),
                e._checkSortPanel(n),
                e._onSwitchLevel(i, n);
            }),
            this.nestedMenu.addEvent("Navi_To", function (t) {
              var i = t.nodeModel,
                n = t.nodeId;
              t.path;
              (e._currentBookmarkId = i && i.getID()),
                (e._currentBookmarkLabel = i && i.getLabel()),
                (e._currentCursor = n),
                (e._selectBookmarkId = ""),
                (e._selectBookmarkLabel = ""),
                (e._selectBookmarkNodeModel = ""),
                (e._currentBookmarkNodeModel = i),
                e._checkSortPanel(n),
                e._onSwitchLevel(i, n);
            }),
            this.nestedMenu.addEvent("select", function (t) {
              if (
                ((e._selectBookmarkId = t && t.getID()),
                (e._selectBookmarkLabel = t && t.getLabel()),
                (e._selectBookmarkNodeModel = t),
                e._checkValidBtn(t),
                e.multiSelectionComponent && e._propertiesWdg)
              ) {
                (e.informationContainer.innerText = ""),
                  e._propertiesWdg.inject(e.informationContainer);
                require(["DS/EditPropWidget/utils/EditPropUtils"], function (
                  i
                ) {
                  var n = [],
                    o = i.getModelFromSelectedNode(t);
                  o && n.push(o), e._propertiesWdg.initDatas(n);
                });
              }
              e.dispatchEvent("onSetActive", t), e.dispatchEvent("select", t);
            }),
            e.multiSelectionComponent &&
              (this.nestedMenu.addEvent("clearSelection", function (t) {
                e._checkValidBtn(t),
                  e._propertiesWdg && e._propertiesWdg.initDatas([]);
              }),
              this.nestedMenu.addEvent("unSelect", function (t) {
                e._checkValidBtn(t),
                  e._propertiesWdg && e._propertiesWdg.initDatas([]),
                  (e.informationContainer.innerText = ""),
                  e.infoNoSelection.inject(e.informationContainer);
              }));
        },
        _onSwitchLevel: function (e, t) {
          e ? this._notInRootLevel(e) : this._inRootLevel(),
            this._checkSearchPanel(t),
            this._updateActivePanel();
        },
        setActiveNode: function (e, t) {
          e === this._activeBookmarkId
            ? ((this._activeBookmarkId = ""),
              (this._activeBookmarkLabel = ""),
              (this._activebookmarkSet = {}))
            : ((this._activeBookmarkId = e),
              (this._activeBookmarkLabel = t),
              (this._activebookmarkSet = {
                getID: function () {
                  return e;
                },
                getLabel: function () {
                  return t;
                },
              }),
              this.updatePlayPause()),
            this.nestedMenu.setActiveNode(e, t),
            this._updateActivePanel();
        },
        setActiveNode_pause: function (e, t) {
          (this._activeBookmarkId = e),
            (this._activeBookmarkLabel = t),
            (this._activebookmarkSet = {
              getID: function () {
                return e;
              },
              getLabel: function () {
                return t;
              },
            }),
            (this._enableActive = !1),
            this.activePanelTitle.addClassName("disable"),
            (this.activePanelPlayPause.title = this._getPlayBtnTooltip()),
            this.activePanelPlayPause.addClassName("fonticon-play"),
            this.activePanelPlayPause.removeClassName("fonticon-pause"),
            this.nestedMenu.setActiveNode("");
        },
        _applySort: function (e, t) {
          (this._sortField =
            e ||
            (this._historySort[this._currentCursor] &&
              this._historySort[this._currentCursor].sortField) ||
            this._sortField),
            (this._sortBy =
              t ||
              (this._historySort[this._currentCursor] &&
                this._historySort[this._currentCursor].sortBy) ||
              this._sortBy),
            "alpha" === this._sortField
              ? ((this._modalHeaderSortPanel_alpha.className =
                  "fonticon fonticon-sort-alpha-" + this._sortBy),
                this._modalHeaderSortPanel_alpha.addClassName("active"),
                this._modalHeaderSortPanel_date.removeClassName("active"))
              : ((this._modalHeaderSortPanel_date.className =
                  "fonticon fonticon-sort-num-" + this._sortBy),
                this._modalHeaderSortPanel_date.addClassName("active"),
                this._modalHeaderSortPanel_alpha.removeClassName("active")),
            this.nestedMenu.applySort(
              this._sortField,
              this._sortBy,
              this._sortFun.bind(this)
            );
        },
        _applySortByReload: function (e, t) {
          (this._sortField = e || this._sortField),
            (this._sortBy = t || this._sortBy),
            (this._historySort[this._currentCursor] =
              this._historySort[this._currentCursor] || {}),
            (this._historySort[this._currentCursor].sortBy = this._sortBy),
            (this._historySort[this._currentCursor].sortField =
              this._sortField),
            this.nestedMenu.applySortByReload(this._sortField, this._sortBy);
        },
        getSortByCursor: function (e) {
          return (
            (e = void 0 === e ? this._currentCursor : e),
            this._historySort[e] || { sortField: "alpha", sortBy: "asc" }
          );
        },
        _sortFun: function (e, t) {
          return e > t;
        },
        _inRootLevel: function () {
          this._modalHeaderNavPanel_BackBtn.hide(),
            (this._modalHeaderNavPanel_Title.innerText = ""),
            this._checkValidBtn();
        },
        _notInRootLevel: function (e) {
          this._modalHeaderNavPanel_BackBtn.show(),
            (this._modalHeaderNavPanel_Title.innerText = e.getLabel()),
            (this._modalHeaderNavPanel_Title.title = e.getLabel()),
            this._checkValidBtn(e);
        },
        _getSelectBtnLabel: function () {
          return this._selectBtn || a.IPClassifyActive_btn_add;
        },
        _getSelectHereBtnLabel: function () {
          return this._selectHereBtn || a.IPClassifyActive_btn_addhere;
        },
        _getUnselectBtnLabel: function () {
          return this._unselectBtn || a.IPClassifyActive_btn_remove;
        },
        _getCancelBtnLabel: function () {
          return this._cancelBtn || a.IPClassifyActive_btn_cancel;
        },
        _getFocusBtnTooltip: function () {
          return this._focusBtnTooltip || a.IPClassifyActive_btn_focus;
        },
        _getDeactivatedBtnTooltip: function () {
          return (
            this._deactivatedBtnTooltip || a.IPClassifyActive_btn_deactivated
          );
        },
        _getPauseBtnTooltip: function () {
          return this._pauseBtnTooltip || a.IPClassifyActive_btn_pause;
        },
        _getPlayBtnTooltip: function () {
          return this._playBtnTooltip || a.IPClassifyActive_btn_play;
        },
        _updateActivePanel: function () {
          this._activeBookmarkId && !this.isChooserPanel
            ? (this.activePanel.show(),
              (this.activePanelTitleText.innerText = this._activeBookmarkLabel))
            : this.activePanel.hide();
        },
        _checkValidBtn: function (e) {
          if (this.multiSelectionComponent) {
            this._couldBeValid(e)
              ? this._classifyButton && (this._classifyButton.disabled = !1)
              : this._classifyButton && (this._classifyButton.disabled = !0);
            let t = !!e && e.getID();
            this._folders_FindInTree &&
              e &&
              ("LibrarySection_favorite" == e.getType() ||
                (t && t.endsWith("_favorite")) ||
                e.isFavorite) &&
              this._folders_FindInTree.hideUI(),
              this._folders_FindInTree &&
                e &&
                e.isFavorite &&
                this._folders_FindInTree.hideUI();
          } else {
            var t = e && e.getID && e.getID();
            this._couldBeValid(e)
              ? ((this.popover.buttons.Ok.disabled = !1),
                this._activeBookmarkId === t && this._enableActive
                  ? (this.popover.buttons.Ok.label =
                      this._getUnselectBtnLabel())
                  : this._couldBeSet(t)
                  ? (this.popover.buttons.Ok.label = this._getSelectBtnLabel())
                  : (this.popover.buttons.Ok.label =
                      this._getSelectHereBtnLabel()))
              : ((this.popover.buttons.Ok.label = this._getSelectBtnLabel()),
                (this.popover.buttons.Ok.disabled = !0));
          }
        },
        _couldBeValid: function (e, t) {
          if ("function" == typeof this.couldBeValid) {
            var i = this.nestedMenu ? this.nestedMenu._treeView : null,
              n = i ? i.getTreeDocument() : null;
            return this.couldBeValid(e, n);
          }
          return !0;
        },
        _getActiveIcon: function (e) {
          return "function" == typeof this.getActiveIcon
            ? this.getActiveIcon(e)
            : e
            ? "wux-ui-3ds wux-ui-3ds-wux-ui-3ds wux-ui-3ds-1x wux-ui-3ds-bookmark"
            : "bookmark";
        },
        _couldBeSet: function (e) {
          return this._currentBookmarkId !== e && this.getNodeModelById(e);
        },
        getNodeModelById: function (e) {
          return this.nestedMenu.getNodeModelById(e);
        },
        searchInContext: function (e) {
          this.nestedMenu
            .search(e)
            .then(() => {
              this.isSearchInitialized = !1;
            })
            .catch(() => {
              this.isSearchInitialized = !1;
            });
        },
        enterSearchPanel: function () {
          this.nestedMenu.startSearch();
        },
        exitSearchPanel: function () {
          this.nestedMenu.endSearch();
        },
        _checkSortPanel: function (e) {
          this._historySort[e] = this._historySort[e] || {};
          var t = this._historySort[this._currentCursor].sortBy || this._sortBy,
            i =
              this._historySort[this._currentCursor].sortField ||
              this._sortField;
          "alpha" === i
            ? ((this._modalHeaderSortPanel_alpha.className =
                "active fonticon fonticon-sort-alpha-" + t),
              this._modalHeaderSortPanel_date.removeClassName("active"))
            : ((this._modalHeaderSortPanel_date.className =
                "active fonticon fonticon-sort-num-" + t),
              this._modalHeaderSortPanel_alpha.removeClassName("active")),
            (this._historySort[this._currentCursor].sortBy = t),
            (this._historySort[this._currentCursor].sortField = i);
        },
        _checkSearchPanel: function (e) {
          if (!this.multiSelectionComponent) {
            e && 0 === e.indexOf("search")
              ? (this._modalHeaderNavPanel.hide(),
                this._modalHeaderSearchPanel.removeClassName("InputHidden"),
                this._modalHeaderSearchPanel_searchBtn.addClassName("active"),
                this._modalHeaderSearchPanel_searchInput.focus())
              : (this._modalHeaderNavPanel.show(),
                this._modalHeaderSearchPanel.addClassName("InputHidden"),
                this._modalHeaderSearchPanel_searchBtn.removeClassName(
                  "active"
                ));
            var t = this.nestedMenu.getSearchKeyById(e);
            (t = t || ""),
              this._modalHeaderSearchPanel_searchInput.setValue(t),
              "" === t
                ? this._modalHeaderSearchPanel_clearBtn.addClassName("hidden")
                : this._modalHeaderSearchPanel_clearBtn.removeClassName(
                    "hidden"
                  );
          }
        },
        initData: function () {
          this.nestedMenu.render(),
            this.nestedMenu.initData(),
            this._inRootLevel(),
            (this._isInitializedData = !0),
            m.setTreeGridView(this.nestedMenu.getTreeView()),
            m.setTreeElementToAddCommand(this._modalContainer),
            (m.getNoFavoriteNodeModelById =
              this.nestedMenu.getNoFavoriteNodeModelById),
            m.setSortFun(this._applySort.bind(this));
        },
        updateView: function () {
          this._activeBookmarkId
            ? this._currentCursor !== this._activeBookmarkId &&
              this.nestedMenu &&
              this.nestedMenu.focusToActiveView(this._activeBookmarkId)
            : this.nestedMenu && this.nestedMenu.updateView();
          var e =
            this._selectBookmarkNodeModel || this._currentBookmarkNodeModel;
          this._checkValidBtn(e);
        },
        getContainer: function () {
          return this._modalContainer;
        },
        _buildPopover: function (e) {
          var t = this;
          if (
            ((this._maxHeight = 350),
            (this._maxWidth = 280),
            widget.addEvent("onResize", function (e) {}),
            (this._modalContainer = UWA.createElement("div", {
              class: "modalContainer",
            })),
            (this._modalHeader = UWA.createElement("div", {
              class: "modalHeader",
              styles: { height: "38px", width: "100%" },
            }).inject(this._modalContainer)),
            (this._modalHeaderNavPanel = UWA.createElement("div", {
              class: "modalHeaderNavPanel",
              styles: {},
            }).inject(this._modalHeader)),
            (this._modalHeaderNavPanel_BackBtn = UWA.createElement("i", {
              class: "wux-ui-3ds wux-ui-3ds-2x wux-ui-3ds-chevron-left",
              title: a.IPClassifyActive_hover_back,
              styles: {
                color: "#b4b6ba",
                cursor: "pointer",
                width: "38px",
                "text-align": "center",
              },
              events: {
                click: function () {
                  t.nestedMenu.NavigatorBack();
                },
              },
            }).inject(this._modalHeaderNavPanel)),
            this._modalHeaderNavPanel_BackBtn.hide(),
            (this._modalHeaderNavPanel_Title = UWA.createElement("span", {
              class: "modalHeaderNavPanel_Title",
              styles: { "font-weight": "bold" },
            }).inject(this._modalHeaderNavPanel)),
            (this._modalHeaderNavPanel_Title.innerText = ""),
            this.authoring &&
              (this._modalHeaderCreateNew = UWA.createElement("div", {
                class: "modalHeaderCreateNew",
              }).inject(this._modalHeader)),
            this.favoriteButton &&
              (this._modalHeaderFavorite = UWA.createElement("div", {
                class: "modalHeaderFavorite",
              }).inject(this._modalHeader)),
            (this._modalHeaderSelectOn = UWA.createElement("div", {
              class: "modalHeaderSelectOn",
            }).inject(this._modalHeader)),
            (this._modalHeaderExpandAll = UWA.createElement("div", {
              class: "modalHeaderExpandAll",
            }).inject(this._modalHeader)),
            this.multiSelectionComponent)
          ) {
            (this._modalHeaderFindInContainer = UWA.createElement("div", {
              class: "modalHeader_findInContainer",
              styles: { width: "100%" },
            }).inject(this._modalContainer)),
              this._modalHeaderFindInContainer.hide(),
              (this._modalHeaderFindInIcon = UWA.createElement("div", {
                class: "modalHeaderFindInIcon",
              }).inject(this._modalHeader)),
              (this._folders_FindInTree = new p({
                dataProvider: m,
                findInContainerDiv: this._modalHeaderFindInContainer,
                disableClickEvent: !0,
              })),
              this._folders_FindInTree.elements.container.inject(
                this._modalHeaderFindInIcon
              ),
              (this._folders_FindInTree.isCurrentFolderValidForFindIn =
                function () {
                  return !0;
                });
            this._folders_FindInTree._findInTreeIcon.addEventListener(
              "click",
              function () {
                !(function () {
                  var e = t.nestedMenu ? t.nestedMenu._treeView : null;
                  if (0 == !!e) return !1;
                  var i = !!e && e.getTreeDocument().getSelectedNodes();
                  return !(
                    0 == i ||
                    0 == i.length ||
                    (i && i.length > 1) ||
                    t._folders_FindInTree.dataProvider.unSupportedSection ==
                      i[0].plmType ||
                    i[0].isFavorite
                  );
                })()
                  ? v.displayNotification({
                      subtitle: a.IPClassify_msg_selection_Incorrect,
                      message: a.IPClassify_msg_selection_Incorrect_Advice,
                      className: "warning",
                    })
                  : t._folders_FindInTree.commonFindinTreeUI.visibleFlag
                  ? t._folders_FindInTree.hideUI()
                  : t._folders_FindInTree.showUI();
              }.bind(t)
            );
          }
          if (
            ((this._modalHeaderSortPanel = UWA.createElement("div", {
              class: "modalHeaderSortPanel",
            }).inject(this._modalHeader)),
            (this._modalHeaderSortPanel_alpha = UWA.createElement("span", {
              class: "fonticon fonticon-sort-alpha-asc",
              title: a.IPClassifyActive_hover_alphaOrder,
              events: {
                click: function (e) {
                  var i = this.hasClassName("fonticon-sort-alpha-desc")
                    ? "desc"
                    : "asc";
                  this.hasClassName("active")
                    ? (this.removeClassName("fonticon-sort-alpha-" + i),
                      (i = "asc" === i ? "desc" : "asc"),
                      this.addClassName("fonticon-sort-alpha-" + i))
                    : (this.addClassName("active"),
                      t._modalHeaderSortPanel_date.removeClassName("active")),
                    t._applySortByReload("alpha", i);
                },
              },
            }).inject(this._modalHeaderSortPanel)),
            (this._modalHeaderSortPanel_date = UWA.createElement("span", {
              class: "fonticon fonticon-sort-num-asc ",
              title: a.IPClassifyActive_hover_dateOrder,
              events: {
                click: function (e) {
                  var i = this.hasClassName("fonticon-sort-num-desc")
                    ? "desc"
                    : "asc";
                  this.hasClassName("active")
                    ? (this.removeClassName("fonticon-sort-num-" + i),
                      (i = "asc" === i ? "desc" : "asc"),
                      this.addClassName("fonticon-sort-num-" + i))
                    : (this.addClassName("active"),
                      t._modalHeaderSortPanel_date.removeClassName("active")),
                    t._applySortByReload("date", i);
                },
              },
            }).inject(this._modalHeaderSortPanel)),
            this.authoring &&
              (this.createNew = UWA.createElement("span", {
                id: "createNewIcon",
                title: a.IPClassifyActive_btn_createBookmark,
                class: "fonticon fonticon-bookmark-add",
                events: {
                  click: function () {
                    var e = t.nestedMenu ? t.nestedMenu._treeView : null,
                      i = e ? e.getTreeDocument() : null;
                    if (
                      t.couldBeValid(void 0, i, !1, 0) &&
                      !i.getSelectedNodes()[0].isFavorite
                    ) {
                      var n = i.getSelectedNodes()[0];
                      t.options.createObject(n, t.getActivePanelLoader.bind(t));
                    } else
                      v.displayNotification({
                        message: a.IPClassify_msg_selection_Incorrect,
                        className: "warning",
                      });
                  },
                },
              }).inject(this._modalHeaderCreateNew)),
            this.favoriteButton &&
              (this.favoriteAddRemove = UWA.createElement("span", {
                id: "favoriteIcon",
                title: a.IPClassifyActive_btn_favorite,
                class: "fonticon fonticon-favorite-on",
                events: {
                  click: function () {
                    var e = this,
                      i = t.nestedMenu ? t.nestedMenu._treeView : null,
                      n = i ? i.getTreeDocument() : null,
                      o = n.getSelectedNodes()[0];
                    if (t.couldBeValid(void 0, n, !1))
                      if (this.hasClassName("active"))
                        t.options.removeFavorite(o, function () {
                          e.removeClassName("active");
                        });
                      else {
                        var s = {
                          id: o.id,
                          name: o.getLabel(),
                          type: o.getType(),
                          mxid: o.mxid,
                        };
                        0 == t.options.isItAmongFavorites(o) &&
                          t.options.addFavorite(s, function () {
                            e.addClassName("active");
                          });
                      }
                    else
                      v.displayNotification({
                        message: a.IPClassify_msg_selection_Incorrect,
                        className: "warning",
                      });
                  },
                },
              }).inject(this._modalHeaderFavorite)),
            (this.expandAll = UWA.createElement("span", {
              id: "expandIcon",
              title: a.IPClassifyActive_btn_expand,
              class: "fonticon fonticon-flow-tree-expand-down",
              events: {
                click: function () {
                  var e = t.nestedMenu ? t.nestedMenu._treeView : null,
                    i = e ? e.getTreeDocument() : null,
                    n =
                      1 == i.getSelectedNodes().length
                        ? i.getSelectedNodes()[0]
                        : void 0;
                  t.couldBeValid(n, i, !1)
                    ? i.getSelectedNodes()[0].expandAll()
                    : v.displayNotification({
                        message: a.IPClassify_msg_selection_Incorrect,
                        className: "warning",
                      });
                },
              },
            }).inject(this._modalHeaderExpandAll)),
            "ONLYSingle" !== this.selectionMode &&
              this.multiSelectionComponent &&
              ((this.selectOn = UWA.createElement("span", {
                id: "selectOnIcon",
                title: a.IPClassifyActive_btn_selectOn,
                class: "fonticon fonticon-select-on",
                events: {
                  click: function () {
                    var e = t.nestedMenu._treeView;
                    if (t.selectOn.isSelectOn) {
                      (e.selectionBehavior.toggle = !1),
                        (e.showSelectionCheckBoxesFlag = "OnOver"),
                        (e.rowSelection = "single");
                      var i = e.getTreeDocument().getSelectedNodes();
                      i && i.length > 1 && e.getTreeDocument().unselectAll(),
                        (t.selectOn.isSelectOn = !1),
                        this.removeClassName("active");
                    } else
                      (e.rowSelection = "multiple"),
                        (e.showSelectionCheckBoxesFlag = !0),
                        (e.selectionBehavior.toggle = !0),
                        (t.selectOn.isSelectOn = !0),
                        this.addClassName("active");
                  },
                },
              }).inject(t._modalHeaderSelectOn)),
              "DefaultSingle" == this.selectionMode
                ? (this.selectOn.isSelectOn = !1)
                : ((this.selectOn.isSelectOn = !0),
                  this.selectOn.addClassName("active"))),
            this._modelEvents.subscribe(
              { event: "IPActive/NodeSelection/OnChange" },
              function () {
                if (t.favoriteButton) {
                  var e = t.nestedMenu ? t.nestedMenu._treeView : null,
                    i = e ? e.getTreeDocument() : null;
                  if (t.couldBeValid(void 0, i, !1)) {
                    var n = i.getSelectedNodes()[0];
                    t.options.isItAmongFavorites(n)
                      ? t.favoriteAddRemove.addClassName("active")
                      : t.favoriteAddRemove.removeClassName("active");
                  } else t.favoriteAddRemove.removeClassName("active");
                }
              }
            ),
            this.multiSelectionComponent)
          ) {
            this.nestedMenuContainer = UWA.createElement("div", {
              class: "nestedMenuContainer",
              styles: { height: "100%", "flex-grow": "1", width: "100%" },
            }).inject(this._modalContainer);
            this._modalHeaderInfoPanel = UWA.createElement("div", {
              class: "modelHeaderInfoPanel",
            }).inject(this._modalHeader);
            var i = UWA.createElement("span", {
              id: "InfoPanel",
              title: a.IPClassifyActive_hover_info,
              class: "fonticon fonticon-info",
            }).inject(t._modalHeaderInfoPanel);
            (t.infoHidden = !0),
              i.addEvent("click", function (e) {
                t.infoHidden
                  ? (this.addClassName("active"),
                    (t._rightDockingElement.visibleDockingZoneFlag = !0),
                    (t.infoHidden = !1))
                  : (this.removeClassName("active"),
                    (t._rightDockingElement.visibleDockingZoneFlag = !1),
                    (t.infoHidden = !0)),
                  e.stopPropagation();
              }),
              (this.informationContainer = UWA.createElement("div", {
                class: "informationContainer",
                styles: { height: "100%", width: "100%" },
              }).inject(this._modalContainer)),
              (this.infoNoSelection = UWA.createElement("div", {
                class: "infoNoSelection",
              })),
              UWA.createElement("div", {
                text:
                  t.multiSelectionComponent.NLS.msg_selectNodeToSeeInfo ||
                  a.IPClassifyActive_msg_selectToSeeInfo,
              }).inject(this.infoNoSelection),
              (t._rightDockingElement = new h({
                side: WUXDockAreaEnum.RightDockArea,
                dockingZoneSize: 200,
                minSize: 100,
                maxSize: 400,
                useBordersFlag: !0,
              })),
              (t._rightDockingElement.visibleCollapserFlag = !1),
              (t._rightDockingElement.collapsibleFlag = !1),
              (t._rightDockingElement.freeZoneContent =
                this.nestedMenuContainer),
              t._rightDockingElement.inject(this._modalContainer),
              (t._rightDockingElement.dockingZoneContent =
                this.informationContainer),
              (t._rightDockingElement.visibleDockingZoneFlag = !1),
              require([
                "DS/EditPropWidget/EditPropWidget",
                "DS/EditPropWidget/constants/EditPropConstants",
              ], function (e, i) {
                (t._propertiesWdg = new e({
                  className: "enxpropertiestab",
                  setEditButton: !1,
                  readOnly: !0,
                  typeOfDisplay: i.FORCE_READONLY,
                  selectionType: i.RESTRICTED_SELECTION,
                  multiselection: !1,
                  collapseTabs: !1,
                  context:
                    t.webServiceRef &&
                    t.webServiceRef.getSecurityContext &&
                    t.webServiceRef.getTenantID
                      ? {
                          getSecurityContext: function () {
                            return {
                              SecurityContext:
                                t.webServiceRef.getSecurityContext(),
                              tenant: t.webServiceRef.getTenantID(),
                            };
                          },
                        }
                      : void 0,
                })),
                  t._propertiesWdg.inject(t.informationContainer),
                  t._propertiesWdg.stopSelection(!0),
                  t._propertiesWdg.initDatas([]);
              }),
              t._folders_FindInTree
                .getUI()
                .inject(this._modalHeaderFindInContainer);
          } else
            (this.nestedMenuContainer = UWA.createElement("div", {
              class: "nestedMenuContainer",
              styles: {
                height: "calc(100% - 90px)",
                "flex-grow": "1",
                width: "100%",
              },
            }).inject(this._modalContainer)),
              (this._modalHeaderSearchPanel = UWA.createElement("div", {
                class: "modalHeaderSearchPanel",
              }).inject(this._modalHeader)),
              (this._modalHeaderSearchPanel_searchInput = new r({
                className: "searchInput",
                placeholder: a.search_placeholder,
                events: {
                  onKeyDown: function (e) {
                    this.getValue()
                      ? (t._modalHeaderSearchPanel_clearBtn.removeClassName(
                          "hidden"
                        ),
                        13 === e.keyCode &&
                          "" != this.getValue().trim() &&
                          (this.getValue().trim().length > 1
                            ? (t.searchInContext(this.getValue().trim()),
                              (t.isSearchInitialized = !0))
                            : v.displayNotification({
                                message: a.msg_warning_notenoughcharforsearch,
                                className: "warning",
                              })))
                      : t._modalHeaderSearchPanel_clearBtn.addClassName(
                          "hidden"
                        );
                  },
                },
              }).inject(this._modalHeaderSearchPanel)),
              (this._modalHeaderSearchPanel_clearBtn = UWA.createElement(
                "span",
                {
                  class: "clearBtn fonticon fonticon-cancel-circled",
                  events: {
                    click: function (e) {
                      t._modalHeaderSearchPanel_searchInput.setValue(""),
                        t._modalHeaderSearchPanel_clearBtn.addClassName(
                          "hidden"
                        );
                    },
                  },
                }
              ).inject(this._modalHeaderSearchPanel)),
              this._modalHeaderSearchPanel_clearBtn.addClassName("hidden"),
              this._modalHeaderSearchPanel.addClassName("InputHidden"),
              (this._modalHeaderSearchPanel_searchBtn = UWA.createElement(
                "span",
                {
                  class: "fonticon fonticon-search",
                  title: a.IPClassifyActive_hover_search,
                  events: {
                    click: function (e) {
                      t._modalHeaderSearchPanel.hasClassName("InputHidden")
                        ? (t._checkSearchPanel("search"), t.enterSearchPanel())
                        : (t._checkSearchPanel(""), t.exitSearchPanel());
                    },
                  },
                }
              ).inject(this._modalHeaderSearchPanel));
          (this.activePanel = UWA.createElement("div", {
            class: "activePanel",
            styles: {
              height: "30px",
              width: "100%",
              bottom: "0",
              border: "1px solid #b4b6ba",
            },
          }).inject(this._modalContainer)),
            this.activePanel.hide(),
            (this.activePanelTitle = UWA.createElement("div", {
              class: "activePanelTitle",
              title: this._getFocusBtnTooltip(),
              styles: { maxWidth: "calc(100% - 20px)", width: "100%" },
              events: {
                click: function (e) {
                  t.updateView();
                },
              },
            }).inject(this.activePanel)),
            (this.activePanelTitleIcon = UWA.createElement("span", {
              class: this._getActiveIcon(!0),
            }).inject(this.activePanelTitle)),
            (this.activePanelTitleText = UWA.createElement("div", {
              class: "activePanelTitleText",
            }).inject(this.activePanelTitle)),
            this.enablePlayPause &&
              (this.activePanelPlayPause = UWA.createElement("span", {
                class: "activePanelDeactivate fonticon fonticon-pause",
                title: this._getPauseBtnTooltip(),
                events: {
                  click: function (e) {
                    t._enableActive = !t._enableActive;
                    var i = t._activeBookmarkId,
                      n = {};
                    t._enableActive
                      ? ((n = t._activebookmarkSet || n),
                        t.playOrPauseActive(!0))
                      : ((i = ""),
                        (n = {
                          isPause: {
                            id: t._activeBookmarkId,
                            label: t._activeBookmarkLabel,
                          },
                        }),
                        t.playOrPauseActive(!1)),
                      t.dispatchEvent("setActive", n),
                      t.nestedMenu.setActiveNode(i),
                      t._updateActivePanel();
                    var o =
                      t._selectBookmarkNodeModel || t._currentBookmarkNodeModel;
                    t._checkValidBtn(o);
                  },
                },
              }).inject(this.activePanel)),
            (this.activePanelDeactivate = UWA.createElement("span", {
              class:
                "activePanelDeactivate clearBtn fonticon fonticon-cancel-circled",
              title: this._getDeactivatedBtnTooltip(),
              events: {
                click: function (e) {
                  (t._activeBookmarkId = ""),
                    (t._activeBookmarkLabel = ""),
                    t.dispatchEvent("setActive", {}),
                    t.nestedMenu.setActiveNode(""),
                    t._updateActivePanel();
                  var i =
                    t._selectBookmarkNodeModel || t._currentBookmarkNodeModel;
                  t._checkValidBtn(i);
                },
              },
            }).inject(this.activePanel)),
            this._initNestedMenu(),
            t.multiSelectionComponent
              ? (this.initMultiSelector(),
                this._modalContainer.inject(this._multiSelectorContentDiv))
              : (this.initPopover(),
                this.popover.setBody(this._modalContainer));
        },
        selectObjects: async function (e) {
          e.length > 0
            ? ("ONLYSingle" === this.selectionMode && (e = [e[0]]),
              this.nestedMenu._maskIt(
                "sel",
                this.nestedMenuContainer,
                a.replace(a.SelectionInProgress, { nb: e.length })
              ),
              await this.nestedMenu.expandMainSection(),
              m.expandTreeAndSelectFolder({
                folderToFindAndSelect: { ids: e },
                onComplete: () => {
                  this._selectVisibleNodes(e),
                    this.nestedMenu._unmaskIt("sel", this.nestedMenuContainer);
                },
                onFailure: () => {
                  this.nestedMenu._unmaskIt("sel", this.nestedMenuContainer);
                },
              }))
            : this._selectVisibleNodes([]);
        },
        _selectVisibleNodes: function (e) {
          this.nestedMenu._treeView.getTreeDocument().unselectAll(),
            e.length > 0 &&
              e.forEach((e) => {
                let t = m._getVisibleNodeById(e);
                t && t.select();
              });
        },
        playOrPauseActive: function (e) {
          this.playOrPauseActive && e
            ? ((this._enableActive = !0),
              (this.activePanelPlayPause.title = this._getPauseBtnTooltip()),
              this.activePanelTitle.removeClassName("disable"),
              this.activePanelPlayPause.removeClassName("fonticon-play"),
              this.activePanelPlayPause.addClassName("fonticon-pause"))
            : this.playOrPauseActive &&
              ((this._enableActive = !1),
              (this.activePanelPlayPause.title = this._getPlayBtnTooltip()),
              this.activePanelTitle.addClassName("disable"),
              this.activePanelPlayPause.addClassName("fonticon-play"),
              this.activePanelPlayPause.removeClassName("fonticon-pause"));
        },
        updatePlayPause: function () {
          this._activeBookmarkId &&
            this.activePanelPlayPause &&
            this.playOrPauseActive(!0);
        },
        /**
         * 根据物理id判断该结点是否是叶子结点
         * @param strJson
         * @private
         */
        _jfCheckClassIsLeafNode : async function (strJson) {
            let postParameter = {
              method : "POST" ,
              headers : {
                Accept: "application/json",
                "content-type": "application/json",
              },
              type : "json",
              data : strJson,
              timeout : 12e4 ,
              withoutSC : 1 ,
              query :{
                JPOName : "JF_ECRRESTService",
                FuncName : "checkClassIsLeafNodeById",
              },
              onFailure :  function (e){
                console.log("error",e);
                return {"isLeaf":false};
              },
            };
            //获取请求对象
            const t = this.webServiceRef;
          let  strRequestUrl = await t.get3DSpaceUrl();
          strRequestUrl += "/TWXPublicRest/TWXTicketService";
          return new Promise(function (resolve,reject){
            postParameter.onComplete =  function (e){
              resolve(e);
            },
             t._executeRequest(strRequestUrl, postParameter, 1);
          }) ;
        },
        getMultiSelectorContainer: function () {
          return this._multiSelectorBody;
        },
        initMultiSelector: function () {
          var e = this,
            t = UWA.createElement("div", {
              class: "ip-selectorComponent-body",
            }),
            i = UWA.createElement("div", {
              class: "ip-selectorComponent-content",
            }).inject(t);
          if (this.dialogButtons) {
            var n = UWA.createElement("div", {
                class: "ip-selectorComponent-footer",
              }).inject(t),
              o = new c({
                label:
                  e.multiSelectionComponent.NLS.button_OK ||
                  a.IPClassifyActive_btn_classify,
                emphasize: "primary",
              });
            o.addEventListener("buttonclick", async function () {
              let isXENAPP = false ;
              if (widget){
                const  appID = widget.data.appId;
                console.log("appID:",appID)
                if ("ENXENG_AP" == appID){
                  isXENAPP = true ;
                }
              }
              if (isXENAPP){
                // add by chenyan 2024/12/05  新增判断是书签还是库 只能请求判断该节点是否是叶子结点
                let typeFlag = e.getActiveIcon();
                if (typeFlag == "library-class" && e._selectBookmarkNodeModel.plmType == "General Class"){
                  //是否为叶子结点
                  let isChild = false;
                  const classLabel = e._selectBookmarkLabel;
                  var res = await  e._jfCheckClassIsLeafNode(JSON.stringify({"objectId":e._selectBookmarkId,"createType":widget.JFTypeName})).then(function (res){
                    return res ;
                  })
                  console.log("res",res);
                  if (res.isLeaf){
                    isChild = true;
                  }
                  // 是否为库结点
                  if (!isChild){
                    //提示只能选择叶子结点
                    let strMess = a.msg_warning_place_leaf_node;
                    strMess = strMess.replace('{}', classLabel);
                    v.displayNotification({
                      message: strMess,
                      className: "warning",
                    })
                    return ;
                    //新增 创建零件时只能选择单件 创建 总成时只能选择组合件 component(零件) assembly(装配) 2025/07/22
                  }
                  // else if (!res.selectClassTypeIsRight) {
                  //   v.displayNotification({
                  //     message: res.mess,
                  //     className: "warning",
                  //   })
                  //   return;
                  // }
                  //调用更新零件类型事件
                  widget.dispatchEvent("JFUpdatePartType",{"range":res.rootRange,"DetailEN":res.DetailEN,"DetailCN":res.DetailCN});
                }
              }

              if (e.handleClassification) {
                let o = new Map();
                var i = [];
                (n = e.nestedMenu.getDocument().getSelectedNodes()).forEach(
                  function (e) {
                    o.set(e.getID(), e.getLabel()), i.push(e.getID());
                  }
                ),
                  e.multiSelectionComponent &&
                  e.multiSelectionComponent.withClassify
                    ? (_.mask(
                        t,
                        e.multiSelectionComponent.NLS.msg_ClassifyInProgress
                      ),
                      e.dispatchEvent("onClassifyMultiple", {
                        classIDandTitleMap: o,
                        objectId: e.options.selectedObjectId,
                        nodeData: n,
                        handleUI: function () {
                          _.unmask(t),
                            e.popover && e.popover.destroy(),
                            (e.immersiveFrame.visibleFlag = !1);
                        },
                      }))
                    : e.dispatchEvent("onClassifyMultiple", {
                        chosenClasses: i,
                        nodeData: n,
                      });
              } else {
                var n;
                i = [];
                (n = e.nestedMenu.getDocument().getSelectedNodes()).forEach(
                  function (e) {
                    i.push(e.getID());
                  }
                ),
                  e.multiSelectionComponent &&
                  e.multiSelectionComponent.withClassify
                    ? (_.mask(
                        t,
                        e.multiSelectionComponent.NLS.msg_ClassifyInProgress
                      ),
                      e.webServiceRef.performMultiClassify({
                        data: {
                          Classifications: i,
                          ItemsToClassify:
                            e.multiSelectionComponent.inputObjects,
                        },
                        onComplete: function (n) {
                          v.displayNotification({
                            message:
                              e.multiSelectionComponent.NLS
                                .msg_ClassifySuccessful,
                            className: "success",
                          }),
                            _.unmask(t),
                            e.popover && e.popover.destroy(),
                            e.dispatchEvent("onClassifyMultiple", {
                              chosenClasses: i,
                              result: "success",
                            });
                        },
                        onFailure: function (i, n, o) {
                          v.displayNotification({
                            message:
                              e.multiSelectionComponent.NLS.msg_ClassifyFailed,
                            className: "error",
                          }),
                            _.unmask(t),
                            e.popover && e.popover.destroy();
                        },
                      }))
                    : e.dispatchEvent("onClassifyMultiple", {
                        chosenClasses: i,
                      });
              }
            }),
              o.inject(n),
              (o.disabled = !0);
            var s = new c({
              label:
                e.multiSelectionComponent.NLS.button_Cancel ||
                a.IPClassifyActive_btn_cancel,
              emphasize: "secondary",
            });
            s.addEventListener("buttonclick", function () {
              e.popover && e.popover.destroy(),
                e.immersiveFrame && (e.immersiveFrame.visibleFlag = !1),
                e.dispatchEvent("onSelectorCancel");
            }),
              s.inject(n),
              (e._classifyButton = o),
              (e._closeButton = s);
          } else t.setAttribute("noButton", !0), i.setAttribute("noButton", !0);
          (e._multiSelectorBody = t),
            (e._multiSelectorContentDiv = i),
            e.initData();
          var r = u.getDataProvider();
          if (e.multiSelectionComponent.launchDialog) {
            if (
              ((e.immersiveFrame = e.multiSelectionComponent.immersiveFrame),
              !e.immersiveFrame)
            ) {
              e.immersiveFrame = new l({
                identifier: "WebUIPanelDocumentation_IconsTitleWindows",
              });
              var h = document.querySelectorAll(
                ".IP_SS_MainIPProtectionContainer"
              )[0];
              (e.iFrameContainer = h || document.body),
                e.immersiveFrame.inject(e.iFrameContainer);
            }
            if (e.popOverDialogProperties)
              var { height: p } = e.popOverDialogProperties,
                { width: f } = e.popOverDialogProperties,
                { icon: m } = e.popOverDialogProperties,
                { minHeight: g } = e.popOverDialogProperties,
                { minWidth: y } = e.popOverDialogProperties,
                { modalFlag: C } = e.popOverDialogProperties,
                { autoCloseFlag: S } = e.popOverDialogProperties;
            (e.popover = new d({
              immersiveFrame: e.immersiveFrame,
              title: e.multiSelectionComponent.NLS.widget_name,
              autoCloseFlag: S || !1,
              content: e._multiSelectorBody,
              height: p || 350,
              width: f || 350,
              minHeight: g || 200,
              minWidth: y || 200,
              resizableFlag: !0,
              modalFlag: C || !1,
              identifier: "WebUIPanelDocumentation_FontAwesomeIconDialog",
              icon: m || {
                iconName: r.getWidgetIcon(),
                fontIconFamily: WUXManagedFontIcons.Font3DS,
              },
            })),
              e.popover.addEventListener("close", function (t) {
                t.stopPropagation(),
                  e.popover.destroy(),
                  (e.immersiveFrame.visibleFlag = !1),
                  e.dispatchEvent("onSelectorCancel");
              });
          }
        },
        initPopover: function () {
          var e = this,
            t = u.getDataProvider();
          if (t && t.getWidgetName && t.getWidgetIcon) {
            var i = this.options.immersiveFrame;
            if (!i) {
              i = new l({
                identifier: "WebUIPanelDocumentation_IconsTitleWindows",
              });
              var n = document.body;
              i.inject(n);
            }
            (this.popover = new d({
              immersiveFrame: i,
              title: t.getWidgetName(),
              autoCloseFlag: !1,
              height: 250,
              width: 250,
              minHeight: 200,
              minWidth: 200,
              resizableFlag: !0,
              modalFlag: !!e.isChooserPanel,
              identifier: "WebUIPanelDocumentation_FontAwesomeIconDialog",
              icon: {
                iconName: t.getWidgetIcon(),
                fontIconFamily: WUXManagedFontIcons.Font3DS,
              },
            })),
              this.popover.elements.container.addClassName("IPClassifyActive"),
              this.initPopoverButtons();
            var o = function () {
              (e.__startTime = Date.now()),
                e._isInitializedData
                  ? e.nestedMenu && e.updateView()
                  : e.initData();
            };
            o(),
              this.popover.addEventListener(
                "contentVisibleStateChange",
                function (t) {
                  e.popover.visibleFlag && o();
                }
              ),
              this.popover.addEventListener("close", function () {
                (e.popover.visibleFlag = !1), e.dispatchEvent("onCancel");
              }),
              (this.popover.setBody = function (t) {
                e.popover.content = t;
              });
          }
        },
        initPopoverButtons: function () {
          var e = this;
          this.popover &&
            (this.popover.buttons = {
              Ok: new c({
                label: this._getSelectBtnLabel(),
                onClick: function (t) {
                  if (e.isSearchInitialized) t.stopPropagation();
                  else {
                    var i = e._selectBookmarkId || e._currentBookmarkId,
                      n = e._selectBookmarkLabel || e._currentBookmarkLabel;
                    i === e._activeBookmarkId &&
                    e._enableActive &&
                    !e.isChooserPanel
                      ? ((e._activeBookmarkId = ""),
                        (e._activeBookmarkLabel = ""))
                      : ((e._activeBookmarkId = i),
                        (e._activeBookmarkLabel = n));
                    var o = {};
                    e._activeBookmarkId &&
                      ((o =
                        e._selectBookmarkNodeModel ||
                        e._currentBookmarkNodeModel),
                      e.updatePlayPause()),
                      (e._activebookmarkSet = o),
                      e.dispatchEvent("setActive", o),
                      e._checkValidBtn(o),
                      e.nestedMenu.setActiveNode(
                        e._activeBookmarkId,
                        e._activeBookmarkLabel
                      ),
                      e._updateActivePanel(),
                      e.closePopoverOrModal();
                  }
                },
              }),
              Cancel: new c({
                label: this._getCancelBtnLabel(),
                onClick: function (t) {
                  e.closePopoverOrModal(), e.dispatchEvent("onCancel");
                },
              }),
            });
        },
        closePopoverOrModal: function () {
          this._modalMode &&
            (this.initPopover(), this.popover.setBody(this._modalContainer)),
            this.popover.hide(),
            (this._modalMode = !1),
            this.modal && this.modal.hide();
        },
        getActivePanelLoader: function () {
          var e = this;
          return {
            createLoader: function () {
              (this.container = UWA.createElement("div", {
                class: "FolderEditor_createNew_loader",
              }).inject(e.getContainer())),
                (this.loader = new y({
                  showButtonFlag: !1,
                  fadeDuration: 500,
                }).inject(this.container)),
                this.loader.on(a.IPClassify_loader_msg_creation);
            },
            removeLoader: function () {
              this.loader &&
                (this.loader.off(),
                this.container.destroy(),
                delete this.loader,
                delete this.container);
            },
          };
        },
        destroy: function () {
          this.popover && this.popover.destroy(),
            this.immersiveFrame && (this.immersiveFrame.visibleFlag = !1);
        },
      });
    }
  ),
  define(
    "DS/IPClassifyActive/service/IPClassifyActiveWebService",
    [
      "UWA/Core",
      "DS/WAFData/WAFData",
      "i18n!DS/IPClassifyActive/assets/nls/IPClassifyActive",
      "DS/PADUtils/PADSettingsMgt",
      "DS/PADUtils/PADUtilsServices",
      "DS/WidgetServices/WidgetServices",
      "DS/IPClassifyActive/utils/IPClassifyActiveUtil",
    ],
    function (e, t, i, n, o, a, s) {
      "use strict";
      function r() {
        return new Promise(function (e, t) {
          a.get3DSearchUrlAsync({
            onComplete: function (t) {
              e(t);
            },
            onFailure: function (e) {
              t(e);
            },
          });
        });
      }
      function l(e) {
        let t = "";
        return (
          e.length > 0 &&
            e.forEach((e, i) => {
              i > 0 && (t += " OR "), (t += "(taxonomies:" + e + ")");
            }),
          t
        );
      }
      return {
        _orderField: "ds6w:label",
        _orderBy: "asc",
        getSetting: function () {
          return s.getSetting();
        },
        get3DSpaceUrl: function () {
          return new Promise(function (e, t) {
            a.get3DSpaceUrlAsync({
              onComplete: function (t) {
                e(t);
              },
              onFailure: function (e) {
                t(e);
              },
            });
          });
        },
        getShowContentInViewSetting: function () {
          return void 0 !== window.top.showContentInView
            ? window.top.showContentInView
            : this.getSetting().showContentInView;
        },
        setOrder: function (e, t) {
          (this._orderField = e || this._orderField),
            (this._orderBy = t || this._orderBy);
        },
        getTenantID: function () {
          return window.top.tenantId || a.getTenantID();
        },
        getSecurityContext: function () {
          var e = n.getSetting("pad_security_ctx");
          if (!e && widget.hasPreference("Current_security_credentials")) {
            var t = widget.getPreference("Current_security_credentials");
            void 0 !== t.defaultValueNonNls &&
              null !== t.defaultValueNonNls &&
              t.hasOwnProperty("defaultValueNonNls") &&
              (e = "ctx::" + t.defaultValueNonNls);
          }
          return (
            !e &&
              widget.getValue("xPref_CREDENTIAL") &&
              (e = "ctx::" + widget.getValue("xPref_CREDENTIAL")),
            window.top.securityContext || e || this._securityContext
          );
        },
        getSelect_bo: function () {
          return [
            "ds6w:label",
            "ds6w:type",
            "physicalid",
            "ds6w:created",
            "ds6w:modified",
            "ds6w:classification",
            "mxid",
            "icon_2ddefaultthb.subtype",
            "ds6w:classificationUsage",
          ];
        },
        _getSecurityContext: function (e) {
          var t = this,
            i = e,
            n =
              "/resources/modeler/pno/person?current=true&select=preferredcredentials&select=collabspaces",
            o = "GET",
            a = i.tenant ? i.tenant : this.getTenantID();
          t.get3DSpaceUrl().then(function (e) {
            var s = e + n + "&tenant=" + a,
              r = widget.lang ? widget.lang : widget.getValue("lang");
            return (
              (i.method = o),
              (i.headers = {
                Accept: "application/json",
                "Accept-Language": r,
                "Content-Type": "application/json",
              }),
              (i.cache = -1),
              (i.withoutSC = !0),
              t._executeRequest(s, i, !0)
            );
          });
        },
        fetchInActiveBookmark: function (e) {
          var t = {};
          if ((UWA.extend(t, e), !UWA.is(t, "object")))
            throw new Error(
              "Invalid inputs for FolderActiveWebService.fetchInActiveBookmark"
            );
          var i = this;
          r().then(function (n) {
            var o = n + "/search",
              a = widget.lang ? widget.lang : widget.getValue("lang");
            (t.payload = {
              with_nls: !1,
              with_indexing_date: !0,
              select_predicate: [
                "ds6w:label",
                "ds6w:created",
                "ds6w:type",
                "ds6w:reservedBy",
                "relcount",
                "taxonomies",
                "ds6w:classification",
                "icon_2ddefaultthb.subtype",
                "mxid",
                "predicatename",
                "ds6w:classificationUsage",
              ],
              select_file: ["icon", "thumbnail_2d"],
              order_field: "ds6w:modified",
              order_by: "desc",
              nresults: 25,
            }),
              i.getRefineFilter && (t.payload.refine = i.getRefineFilter()),
              (t.payload.order_by = i._orderBy),
              (t.payload.order_field = i._orderField),
              (t.payload.locale = a),
              (t.payload.query = 'NOT physicalid: "' + e.pid + '"'),
              (t.payload.tenant = i.getTenantID());
            var s = i.getSetting().numberOfResultsDisplay;
            (t.payload.nresults =
              e.nresults || parseInt(s) || t.payload.nresults),
              (t.payload.next_start = void 0),
              (t.payload.start = void 0),
              (t.payload.source = ["3dspace"]),
              (t.payload.label = i.getLabel()),
              delete t.payload.specific_source_parameter;
            var r = i.getShowContentInViewSetting();
            s = i.getSetting().numberOfResultsDisplay;
            t.payload.specific_source_parameter =
              i.getSpecific_source_parameter(e.pid);
            null != e.next_start
              ? (t.payload.next_start = e.next_start)
              : (t.payload.start = 0),
              (t.method = "POST"),
              (t.headers = {
                Accept: "application/json",
                "content-type": "application/json",
                "Accept-Language": a,
              }),
              (t.type = "json"),
              (t.data = UWA.Json.encode(t.payload)),
              (t.withoutSC = !0),
              (t.timeout = 12e4);
            var l = t.onComplete,
              d = t.onFailure;
            let c = null;
            e.isContentSearch
              ? ((t.onComplete = function (e) {
                  var t = e.infos.next_start.split(" ")[0];
                  (e.isEnd = t === e.infos.nhits + ""),
                    (e.isContentSearch = !0),
                    l && l(e);
                }),
                (t.onFailure = function (e) {
                  d && d(e);
                }),
                (c = i.fetchContent(o, t)))
              : ((t.onComplete = function (n) {
                  n.results = n.results || [];
                  var a = n.results.length,
                    u = s - a;
                  u > 0 && r && !e.hideContent
                    ? ((t.payload.nresults = u),
                      (t.payload.specific_source_parameter = {
                        "3dspace": {
                          search_in_context: {
                            expand: {
                              root_physicalid: e.pid,
                              expand_iter: "1",
                              type_filter_bo: [],
                            },
                          },
                        },
                      }),
                      (t.onComplete = function (e) {
                        (e.results = e.results || []),
                          (e.results = n.results.concat(e.results));
                        var t = n.infos.nhits + e.infos.nhits;
                        (e.isEnd = e.results.length === t),
                          (e.isContentSearch = !0),
                          l && l(e);
                      }),
                      (t.onFailure = function (e) {
                        d && d(e);
                      }),
                      (c = i.fetchContent(o, t)))
                    : ((n.isEnd =
                        n.results && n.results.length === n.infos.nhits),
                      l && l(n));
                }),
                (t.onFailure = function (e) {
                  d && d(e);
                }),
                (c = i._executeRequest(o, t, "searchInFolder")));
          });
        },
        fetchContent: function (e, t) {
          return (
            (t.payload.specific_source_parameter =
              this.getSpecific_source_parameter_content(t.pid)),
            (t.payload.label = this.getLabel()),
            (t.data = UWA.Json.encode(t.payload)),
            this._executeRequest(e, t, "searchInFolder")
          );
        },
        initSecurityContext: function () {
          var e = this;
          return new Promise(function (t, i) {
            e._getSecurityContext({
              onComplete: function (i) {
                var n = JSON.parse(i);
                (e._securityContext =
                  "ctx::" +
                  n.preferredcredentials.role.name +
                  "." +
                  n.preferredcredentials.organization.name +
                  "." +
                  n.preferredcredentials.collabspace.name),
                  t(e._securityContext);
              },
              onFailure: function (e) {
                console.log(e), i(e);
              },
            });
          });
        },
        pathInActiveBookmark: function (e) {
          var t = this;
          this.getSecurityContext()
            ? this._pathInActiveBookmark(e)
            : this.initSecurityContext().then(function (i) {
                t._pathInActiveBookmark(e);
              });
        },
        _pathInActiveBookmark: function (e) {
          var t = {};
          if ((UWA.extend(t, e), !UWA.is(t, "object")))
            throw new Error(
              "Invalid inputs for FolderActiveWebService.pathInActiveBookmark"
            );
          var i = this;
          i.get3DSpaceUrl().then(function (n) {
            var o = n + "/cvservlet/navigate",
              a = widget.lang ? widget.lang : widget.getValue("lang");
            (t.payload = {}),
              (t.payload.locale = a),
              (t.payload.order_by = i._orderBy),
              (t.payload.order_field = i._orderField),
              (t.payload.tenant = i.getTenantID()),
              (t.payload.input_physical_ids = e.ids),
              (t.payload.version = 3),
              i.navigatePathRelArray &&
              UWA.is(i.navigatePathRelArray(), "array")
                ? (t.payload.primitives = [
                    {
                      navigate_from_rel: {
                        id: 1,
                        filter: { rel_type: i.navigatePathRelArray() },
                      },
                    },
                  ])
                : (t.payload.primitives = [{ navigate_from_rel: { id: 1 } }]),
              (t.payload.patterns = { GET_PARENT: [{ id: 1, iter: -1 }] }),
              (t.payload.patternFlags = {
                GET_PARENT: { flags: ["returnPaths"] },
              }),
              (t.payload.attributes = [
                "ds6w:label",
                "ds6w:type",
                "ds6w:created",
                "ds6w:modified",
                "ds6w:classification",
                "icon_2ddefaultthb.subtype",
                "mxid",
              ]),
              (t.payload.next_start = void 0),
              (t.payload.start = void 0),
              (t.payload.source = ["3dspace"]),
              (t.payload.label = i.getLabel()),
              delete t.payload.specific_source_parameter;
            null != e.next_start
              ? (t.payload.next_start = e.next_start)
              : (t.payload.start = 0),
              (t.method = "POST"),
              (t.headers = {
                Accept: "application/json",
                "content-type": "application/json",
                "Accept-Language": a,
              }),
              (t.type = "json"),
              (t.data = UWA.Json.encode(t.payload)),
              (t.query = { SecurityContext: i.getSecurityContext() }),
              (t.timeout = 12e4),
              i._executeRequest(o, t, "searchInFolder");
          });
        },
        searchInActiveBookmark: function (e) {
          var t = this;
          this.getSecurityContext()
            ? this._searchInActiveBookmark(e)
            : this.initSecurityContext().then(function (i) {
                t._searchInActiveBookmark(e);
              });
        },
        _searchInActiveBookmark: function (e) {
          // add by chenyan 只有XEN是才有修改逻辑
          let isXENAPP = false ;
          if (widget){
            const  appID = widget.data.appId;
            console.log("appID:",appID)
            if ("ENXENG_AP" == appID){
              isXENAPP = true ;
            }
          }
          var t = {};
          if ((UWA.extend(t, e), !UWA.is(t, "object")))
            throw new Error(
              "Invalid inputs for FolderActiveWebService.searchInActiveBookmark"
            );
          let strBaseUrl  = a.getServerUrl();
          var i = e.bookmarkID
              ? this.generateLabelQuery(e.bookmarkID)
              : e.searchKey
              ? this.generateSearchQuery(e.searchKey, e.pid)
              : this.getSearchQuery(),
            n = this;
          r().then(function (o) {
            var a = o + "/search",
              s = widget.lang ? widget.lang : widget.getValue("lang");
            (t.payload = {
              with_nls: !1,
              with_indexing_date: !0,
              select_predicate: [
                "ds6w:label",
                "ds6w:created",
                "ds6w:type",
                "ds6w:reservedBy",
                "relcount",
                "taxonomies",
                "ds6w:classification",
                "icon_2ddefaultthb.subtype",
                "mxid",
                "predicatename",
                "ds6w:classificationUsage",
              ],
              select_file: ["icon", "thumbnail_2d"],
              order_field: "ds6w:modified",
              order_by: "desc",
              nresults: 25,
            }),
              n.getRefineFilter && (t.payload.refine = n.getRefineFilter()),
              (t.payload.locale = s),
              (t.payload.order_by = n._orderBy),
              (t.payload.order_field = n._orderField),
              (t.payload.query = i),
              (t.payload.tenant = n.getTenantID()),
              (t.payload.next_start = void 0);
            var r = n.getSetting().numberOfResultsDisplay;
            (t.payload.nresults =
              void 0 !== e.nresults
                ? e.nresults
                : parseInt(r) || t.payload.nresults),
              (t.payload.start = void 0),
              (t.payload.source = ["3dspace"]),
              (t.payload.login = {
                "3dspace": { SecurityContext: n.getSecurityContext() },
              }),
              (t.payload.label = n.getLabel()),
              delete t.payload.specific_source_parameter,
              e.pid &&
                (t.payload.specific_source_parameter =
                  n.getSpecific_source_parameter(e.pid, !0));
            null != e.next_start
              ? (t.payload.next_start = e.next_start)
              : (t.payload.start = 0),
              (t.method = "POST"),
              (t.headers = {
                Accept: "application/json",
                "content-type": "application/json",
                "Accept-Language": s,
              }),
              (t.type = "json"),
              (t.data = UWA.Json.encode(t.payload)),
              (t.withoutSC = !0),
              (t.timeout = 12e4);
            console.log("button:",i)
            if (isXENAPP && i.includes("Library")){
              let strQueryUrl = strBaseUrl + "/3dspace/TWXPublicRest/TWXTicketService?JPOName=JF_ECRRESTService&FuncName=getLibQueryByXENApp&Params=%7B%7D"
              const queryArgs = {
                  method : "GET",
                  headers : {
                    "Accept": "application/json",
                    "content-type": "application/json",
                    "Accept-Language": s,
                  },
                  type : "json",
                onComplete: function (res){
                    if (res.code == "200"){
                      if (res.query){
                        t.payload.query = res.query ;
                        t.data = UWA.Json.encode(t.payload);
                      }
                      n._executeRequest(a, t, "searchInFolder");
                    }else {
                      n._executeRequest(a, t, "searchInFolder");
                    }
                },
                onFailure:function (res){
                  n._executeRequest(a, t, "searchInFolder");
                }
              }
              n._executeRequest(strQueryUrl,queryArgs,"getLibQueryByXENApp");
            }else if (isXENAPP && i.includes("Workspace")){
              let strQueryUrl = strBaseUrl + "/3dspace/TWXPublicRest/TWXTicketService?JPOName=JF_ECRRESTService&FuncName=getWorkspaceQueryByXENApp&Params=%7B%7D"
              const queryArgs = {
                method : "GET",
                headers : {
                  "Accept": "application/json",
                  "content-type": "application/json",
                  "Accept-Language": s,
                },
                type : "json",
                onComplete: function (res){
                  if (res.code == "200"){
                    if (res.query){
                      t.payload.query = res.query ;
                      t.data = UWA.Json.encode(t.payload);
                    }
                    n._executeRequest(a, t, "searchInFolder");
                  }else {
                    n._executeRequest(a, t, "searchInFolder");
                  }
                },
                onFailure:function (res){
                  n._executeRequest(a, t, "searchInFolder");
                }
              }
              n._executeRequest(strQueryUrl,queryArgs,"getWorkspaceQueryByXENApp");
            }else {
              n._executeRequest(a, t, "searchInFolder");
            }
          });
        },
        //展开的请求 add by caipan 20260612
        expandInActiveBookmark: async function (e) {
          if (e.isRoot)
            return (e.nresults = 1e3), this.searchInActiveBookmark(e);
          var d = await this.get3DSpaceUrl(),
            t =
              d + "/cvservlet/progressiveexpand/v2?output_format=cvjson",
            i = {};
          UWA.extend(i, e);
          let n = {
            label: this.getLabel(),
            root: { physical_id: e.pid },
            parameters: { graph_advise: 1 },
            graph: {},
            filter: { all: 1 },
          };
          if ("function" == typeof this.getRel_filter_bo)
            n.graph.descending_condition_relation = {
              uql: l(this.getRel_filter_bo()),
            };
          else if ("function" == typeof this.getType_filter_bo) {
            var o = l(this.getType_filter_bo());
            this.getActiveMaturityFilter &&
              (o = this.getActiveMaturityFilter(o)),
              (n.graph.descending_condition_object = { uql: o });
          }
          "All" !== i.level &&
            (n.aggregation_processors = [
              {
                truncate: {
                  max_distance_from_prefix: 1,
                  prefix_filter: {
                    prefix_path: [{ physical_id_path: [e.pid] }],
                  },
                },
              },
            ]);
          i.payload = {
            batch: { expands: [n] },
            outputs: {
              select_object: this.getSelect_bo(),
              hits: { predefined_computation: ["icons"] },
              format: "entity_relation_occurrence",
            },
          };
          await new Promise(
            function (e) {
              var t =
                  d +
                  "/TWXPublicRest/TWXTicketService?JPOName=JF_ECRRESTService&FuncName=getExpandQueryByRole&Params=%7B%7D",
                o = widget.lang ? widget.lang : widget.getValue("lang"),
                a = {
                  method: "GET",
                  headers: {
                    Accept: "application/json",
                    "content-type": "application/json",
                    "Accept-Language": o,
                  },
                  type: "json",
                  onComplete: function (t) {
                    "200" == t.code &&
                      t.query &&
                      (n.graph.descending_condition_object = {
                        uql: t.query,
                      }),
                      e();
                  },
                  onFailure: function () {
                    e();
                  },
                };
              try {
                this._executeRequest(t, a, "getExpandQueryByRole");
              } catch (t) {
                e();
              }
            }.bind(this)
          );
          return (
            (i.headers = {
              "Content-Type": "application/json",
              SecurityToken:
                window.top.dsUserLogin +
                "|" +
                this.getSecurityContext() +
                "|preferred",
            }),
            (i.type = "json"),
            (i.data = UWA.Json.encode(i.payload)),
            (i.method = "POST"),
            (i.timeout = 12e4),
            this._executeRequest(t, i, "expandFolder")
          );
        },
        getUserPreferences: function (e) {
          var t = e;
          if (!UWA.is(t, "object"))
            throw new Error(
              "Invalid inputs for FolderActiveWebService.getUserPreferences"
            );
          var i = this,
            n = null;
          return (n = a.getCompassUrlAsync({
            onComplete: function (e) {
              var o = e + "/resources/AppsMngt/user/getPreferences";
              (t.query = { name: t.name }),
                (t.method = "GET"),
                (t.headers = {
                  Accept: "application/json",
                  SecurityContext: void 0,
                }),
                (t.withoutTenant = !0);
              var a = i._executeRequest(o, t, "getUserPreferences");
              n ? (n.cancel = a.cancel) : (n = a);
            },
          }));
        },
        _executeRequest: function (e, n, o) {
          var a = Math.round(1e3 * Math.random()).toString(),
            s = widget.id + "_" + widget.getTitle() + "_";
          window.top.performance.mark("folderRequest_begin" + a);
          var r = {};
          UWA.extend(r, n),
            UWA.is(r.onTimeout, "function") ||
              ((r.onTimeout = function () {
                var e = {
                  type: "timeout",
                  toString: function () {
                    return i.msg_InfoAction_Timeout;
                  },
                };
                r.onFailure(e);
              }),
              (r.onComplete = function (e) {
                window.top.performance.mark("folderRequest_end" + a);
                var t = window.top.performance.getEntriesByName(
                  "folderRequest_begin" + a
                );
                t &&
                  1 === t.length &&
                  (window.top.performance.measure(
                    s + "_Request" + o,
                    "folderRequest_begin" + a,
                    "folderRequest_end" + a
                  ),
                  window.top.performance.clearMarks("folderRequest_end" + a),
                  window.top.performance.clearMarks("folderRequest_begin" + a)),
                  n.onComplete(e);
              }));
          var l = {
            Accept: "application/json",
            "Accept-Language": widget.lang
              ? widget.lang
              : widget.getValue("lang"),
          };
          n.withoutSC ||
            (l.SecurityContext = encodeURIComponent(this.getSecurityContext())),
            n.headers
              ? (r.headers = UWA.extend(l, n.headers))
              : (r.headers = l),
            UWA.is(r.query) || (r.query = {}),
            r.query.tenant ||
              !0 === n.withoutTenant ||
              (r.query.tenant = this.getTenantID());
          var d = e;
          return (
            0 !== Object.keys(r.query).length &&
              (d +=
                (e.indexOf("?") < 0 ? "?" : "&") +
                UWA.Utils.toQueryString(r.query)),
            t.authenticatedRequest(d, r)
          );
        },
      };
    }
  ),
  define(
    "DS/IPClassifyActive/IPClassifyActive",
    [
      "UWA/Utils",
      "UWA/Controls/Abstract",
      "UWA/Class/Options",
      "UWA/Class/Events",
      "UWA/Promise",
      "DS/TreeModel/TreeDocument",
      "DS/TreeModel/TreeNodeModel",
      "DS/WebappsUtils/WebappsUtils",
      "DS/IPClassifyActive/service/IPClassifyActiveWebService",
      "DS/IPClassifyActive/utils/IPClassifyActiveUtil",
      "i18n!DS/IPClassifyActive/assets/nls/IPClassifyActive",
      "DS/PADUtils/PADUtilsServices",
      "DS/IPClassifyActive/NestedMenu/NestedMenuInContext",
      "DS/WidgetServices/WidgetServices",
      "DS/IPClassifyActive/utils/EventsManager",
      "css!DS/IPClassifyActive/IPClassifyActive.css",
      "DS/IPClassifyActive/utils/WidgetDataProvider",
      "DS/IPClassifyActive/utils/IPClassifyActiveLogger",
      "DS/IPClassifyActive/utils/IPClassifyActiveCtxSelectionPanel",
      "DS/PADUtils/PADSettingsMgt",
      "DS/IPClassifyUtil/IPClassifyAlert",
    ],
    function (e, t, i, n, o, a, s, r, l, d, c, u, h, p, f, _, v, m, g, y, C) {
      "use strict";
      return t.extend(i, n, {
        name: "IPClassifyActive",
        _logger: null,
        _rootNodeFavorite: null,
        _rootNodeFolders: null,
        _rootNodePersonalSection: null,
        _rootNodeTrash: null,
        _requestInfo: {},
        _searchInfo: {},
        _pathInfo: {},
        _subBookmarkExpand: {},
        _getSetting: function () {
          return d.getSetting();
        },
        init: function (e) {
          // add by 2024/12/03 chenyan 新增只有XEN才走单选逻辑，其他APP不走
          let isXENAPP = false ;
          if (widget){
            const  appID = widget.data.appId;
            console.log("appID:",appID)
            if ("ENXENG_AP" == appID){
              isXENAPP = true ;
            }
          }
          if (isXENAPP){
           //设置XEN创建选择库时只能单选
            e.selectionMode = "ONLYSingle";
          }
          var t = this;
          (this._logger = m.getLogger()),
            (this.options = e),
            v.setDataProvider(e.dataProvider),
            (this.nestedMenuInContext =
              this.nestedMenuInContext ||
              new h({
                master: this,
                title: e.title,
                container: e.container,
                immersiveFrame: e.immersiveFrame,
                toggleManual: e.toggleManual,
                fixPosition: e.fixPosition,
                className: e.className,
                selectBtnLabel: e.selectBtnLabel,
                selectHereBtnLabel: e.selectHereBtnLabel,
                unselectBtnLabel: e.unselectBtnLabel,
                cancelBtnLabel: e.cancelBtnLabel,
                onPreExpandTreeNode: this._onPreExpandTreeNode.bind(this),
                onPreCollapseTreeNode: this._onPreCollapseTreeNode.bind(this),
                onInfiniteScrollAsync: this._onInfiniteScrollAsync.bind(this),
                onContextualMenuReady: this.__onContextualMenuReady.bind(this),
                firstScreenFetch: this.firstScreenFetch.bind(this),
                populateFavoriteSection:
                  this._populateFavoriteSection.bind(this),
                searchData: this._searchData.bind(this),
                fetchData: this.fetchData.bind(this),
                generateParentChain: this.generateParentChain.bind(this),
                getPath: this.getPathOfActiveBookmark.bind(this),
                sortByReload: this.sortByReload.bind(this),
                sortFunction: this.sortFunction.bind(this),
                shouldDisplayTooltip: this.shouldDisplayTooltip.bind(this),
                focusBtnTooltip: this._getFocusBtnTooltip(),
                couldBeSelected: e.couldBeSelected
                  ? e.couldBeSelected.bind(this)
                  : this.couldBeSelected.bind(this),
                couldBeValid: e.couldBeValid
                  ? e.couldBeValid.bind(this)
                  : this.couldBeValid.bind(this),
                getActiveIcon: this._getActiveIcon.bind(this),
                enableModalView: this._getSetting().enableModalView,
                enablePlayPause: this._getSetting().enablePlayPause,
                showMessageWhileEmpty: this._showMessageWhileEmpty(
                  this._getSetting().showContentInView
                ),
                showMessageWhileResultEmpty:
                  c.IPClassifyActive_msg_noResultFound,
                allSection: this._allSection,
                isChooserPanel: e.isChooserPanel,
                multiSelectionComponent: e.multiSelectionComponent,
                authoring: e.authoring,
                dialogButtons: e.dialogButtons,
                favoriteButton: e.favoriteButton,
                selectionMode: e.selectionMode,
                isItAmongFavorites: this.isItAmongFavorites.bind(this),
                addFavorite: this.addFavorite.bind(this),
                removeFavorite: this.removeFavorite.bind(this),
                webServiceRef: t._getWebServiceRef(),
                createObject: this._createObject.bind(this),
                handleClassification: e.handleClassification,
                isRootSelectable: e.isRootSelectable,
                isClassifyMultiEveDispatch: e.isClassifyMultiEveDispatch,
                isonSetActiveEveDispatch: e.isonSetActiveEveDispatch,
                popOverDialogProperties: e.popOverDialogProperties,
                selectedObjectId: e.objectId,
                hideFavorite: e.hideFavorite,
              })),
            this.nestedMenuInContext.addEvent("setActive", function (e) {
              t.options.onSetActive && t.options.onSetActive(e),
                t.dispatchEvent("onSetActive", e);
            }),
            this.nestedMenuInContext.addEvent("onCancel", function (e) {
              t.dispatchEvent("onCancel");
            }),
            this.nestedMenuInContext.addEvent("updateActiveInfo", function (e) {
              t.dispatchEvent("updateActiveInfo", e);
            }),
            this.nestedMenuInContext.addEvent("select", function (e) {
              f.notifyObjectSelection(e);
            }),
            e.isClassifyMultiEveDispatch &&
              this.nestedMenuInContext.addEvent(
                "onClassifyMultiple",
                function (e) {
                  t.dispatchEvent("onClassifyMultiple", e);
                }
              ),
            e.isonSetActiveEveDispatch &&
              this.nestedMenuInContext.addEvent("onSetActive", function (e) {
                t.dispatchEvent("onSetActive", e);
              }),
            p.isReplayODT() && (window.FolderActive = this);
        },
        selectObjects: function (e) {
          this.nestedMenuInContext.selectObjects(e);
        },
        showPopover: function () {
          this.nestedMenuInContext.popover.visibleFlag = !0;
        },
        hidePopover: function () {
          this.nestedMenuInContext.popover.visibleFlag = !1;
        },
        togglePopover: function () {
          this.nestedMenuInContext.popover.visibleFlag =
            !this.nestedMenuInContext.popover.visibleFlag;
        },
        getModalContainer: function () {
          return (
            this.nestedMenuInContext && this.nestedMenuInContext.getContainer()
          );
        },
        _generateExpandData: function (e, t, i) {
          t.prepareUpdate();
          let n = null,
            o = null;
          i
            ? t.removeChildren()
            : ((n = {}),
              (o = {}),
              t.getChildren().length > 0 &&
                t.getChildren().forEach((e, t) => {
                  n[e.id] = t;
                }),
              (o = Object.keys(n)));
          let a = [],
            r = [],
            l = {};
          if (
            (e.forEach((e) => {
              if (e.from || e.Path) e.Path && r.push(e.Path);
              else {
                let t = e.resourceid,
                  i = o ? o.indexOf(t) : -1;
                if (-1 === i) {
                  a.push(e);
                  let t = { useAsyncPreExpand: !0 },
                    i =
                      "Shared" === e["ds6w:classification"]
                        ? "Shared"
                        : "Bookmark";
                  (e.iconType = e["icon_2ddefaultthb.subtype"]),
                    (t.label = e["ds6w:label"]),
                    (t.icons = [e.icon]),
                    (t.id = e.resourceid),
                    (t.plmType = this._getPlmType(e["ds6w:type"])),
                    (t.created = e["ds6w:created"]),
                    (t.modified = e["ds6w:modified"]),
                    (t.folderClassification = i),
                    (t.iconType = e.iconType),
                    (t.mxid = e.mxid),
                    (t.classUsage = e["ds6w:classificationUsage"]),
                    (t.icons = [this._getIcon(e)]),
                    this.couldBeExpand(t) && (t.children = []),
                    (l[t.id] = t);
                } else o.splice(i, 1), delete n[t];
              }
            }),
            n)
          ) {
            let e = Object.values(n).sort();
            for (let i = e.length - 1; i >= 0; i--)
              t.getChildren().splice(e[i], 1);
          }
          if (
            (r.forEach((e) => {
              var i = t;
              for (let t = 2; t < e.length; t += 2) {
                let n = e[t],
                  o = l[n];
                o &&
                  (o instanceof s
                    ? i.addChild(o)
                    : (l[n] = this._createTreeNodeModel(o, i)),
                  (i = l[n]));
              }
            }),
            t.pushUpdate(),
            i)
          )
            for (var d in l)
              l[d].id !== t.id &&
                l[d].expand &&
                !l[d].isExpanded() &&
                l[d].expand({ onlyUI: !0 });
          this.nestedMenuInContext._applySort();
        },
        _generateSearchData: function (e) {
          var t = this,
            i = { 0: new a() };
          i[0].onPreExpand(function (e) {
            e.data.onlyUI && e.target.preExpandDone();
          });
          var n = {
              "ds6w:label": "label",
              "ds6w:what/ds6w:type": "type",
              type_icon_url: "icon",
              resourceid: "id",
              "ds6w:created": "created",
              "ds6w:modified": "modified",
              "ds6w:when/ds6w:created": "created",
              "ds6w:when/ds6w:modified": "modified",
              "ds6w:how/ds6w:classification": "folderClassification",
              "icon_2ddefaultthb.subtype": "iconType",
              mxid: "mxid",
              "ds6w:type": "type",
              "ds6w:what/ds6w:classificationUsage": "classUsage",
            },
            o = [],
            s = v.getDataProvider();
          s &&
            s.getAdditionalSearchAttributes &&
            (o = s.getAdditionalSearchAttributes()),
            o.forEach((e, t) => {
              n[e] = e;
            }),
            e.forEach(function (e) {
              var a = e.attributes,
                s = {};
              a.forEach(function (e) {
                var t = n[e.name];
                t && (s[t] = e.value),
                  "type" === t && (s.displayType = e.dispValue);
              });
              var r = { useAsyncPreExpand: !0 };
              (r.label = s.label),
                (r.icons = [s.icon]),
                (r.id = s.id),
                (r.plmType = t._getPlmType(s.type)),
                (r.created = s.created),
                (r.modified = s.modified),
                (r.folderClassification = s.folderClassification),
                (r.iconType = s.iconType),
                (r.mxid = s.mxid),
                (r.classUsage = s.classUsage),
                o.forEach((e, t) => {
                  r[e] = s[e];
                });
              var l = e && e.level ? parseInt(e.level) : 1;
              (r.icons = [t._getIcon(s)]),
                t.couldBeExpand(r) && (r.children = []),
                (i[l] = t._createTreeNodeModel(r, i[l - 1]));
            });
          var r = i[0].getRoots();
          return [].concat(r);
        },
        generateData: function (e) {
          var t = this,
            i = { 0: new a() };
          return (
            i[0].onPreExpand(function (e) {
              e.data.onlyUI && e.target.preExpandDone();
            }),
            e.forEach(function (e) {
              var n = { useAsyncPreExpand: !0 };
              (n.label = e.label || e["ds6w:label"]),
                (n.icons = (e.icon && [e.icon]) || [e.type_icon_url]),
                (n.id = e.id || e.resourceid),
                (n.plmType = t._getPlmType(e["ds6w:what/ds6w:type"])),
                (n.created = e.created || e["ds6w:when/ds6w:created"]),
                UWA.is(e.publishOnConnect) &&
                  (n.publishOnConnect = e.publishOnConnect),
                (n.folderClassification = e["ds6w:how/ds6w:classification"]),
                (n.iconType = e["icon_2ddefaultthb.subtype"]),
                (n.mxid = e.mxid);
              var o = e && e.level ? parseInt(e.level) : 1;
              t.couldBeExpand(n) &&
                ((n.children = []), (n.icons = [t._getIcon(n)])),
                (i[o] = t._createTreeNodeModel(n, i[o - 1]));
            }),
            i[0].getRoots()
          );
        },
        _pathData: function (e, t) {
          var i = this;
          return new o(function (n, o) {
            i._getWebServiceRef().pathInActiveBookmark({
              ids: e.map(function (e) {
                return e.id;
              }),
              onComplete: function (o) {
                var a = o.result,
                  s = {},
                  r = {},
                  l = [];
                a.forEach(function (e) {
                  var t =
                      e.outputs &&
                      e.outputs.GET_PARENT &&
                      e.outputs.GET_PARENT[0],
                    i = "",
                    n = e.input_object.physicalid,
                    o = e.input_object["ds6w:type"],
                    a = "",
                    l = "",
                    d = "root";
                  if (
                    t &&
                    ((i = t["ds6w:label"]),
                    (d = t.physicalid),
                    (a = t["ds6w:label"]),
                    (l = t["ds6w:type"]),
                    t.paths)
                  ) {
                    var c = t.paths.filter(function (e) {
                      return e[0].physicalid === n;
                    })[0];
                    if (c && c.length > 1) {
                      (d = c[1].physicalid), (a = c[1]["ds6w:label"]);
                      for (var u = c.length - 1; u > 1; u--)
                        i += " > " + c[u]["ds6w:label"];
                    } else a = "";
                  }
                  ("Workspace Vault" !== o || t) &&
                    ((s[n] = s[n] || {}),
                    (s[n].pathName = i),
                    (s[n].parentName = a),
                    (s[n].parentType = l),
                    (s[n].parent = d)),
                    (r[n] = r[n] || {}),
                    (r[n].type = o);
                });
                var d = i._pathInfo[t],
                  c = "";
                e.forEach(function (e) {
                  if (
                    !s[e.id] ||
                    ("Folder" === e.getType() &&
                      "Workspace" != s[e.id].parentType)
                  )
                    ("Folder" === e.getType() && "Workspace" != r[e.id].type) ||
                      l.push(e);
                  else {
                    if ((l.push(e), d.parent && d.parent === s[e.id].parent))
                      e.path = c;
                    else {
                      var n = i._getRootName();
                      s[e.id].pathName && (n += " > " + s[e.id].pathName);
                      var o = "";
                      s[e.id].parentName &&
                        (" > " + s[e.id].parentName,
                        (o += " > " + s[e.id].parentName));
                      e.path = n + o;
                    }
                    (d = s[e.id]),
                      (c = e.path),
                      (i._pathInfo[t] = s[e.id].parent);
                  }
                }),
                  n(l);
              },
              onFailure: function (e, t, i) {
                o(e, t, i);
              },
            });
          });
        },
        _searchData: function (e, t, i, n) {
          var a = this;
          return (
            (i =
              i === this._allSection || i === this._favoriteSection
                ? void 0
                : i),
            new o(function (o, s) {
              a._getWebServiceRef().searchInActiveBookmark({
                content: "All",
                filterChildren: "",
                pid: i,
                searchKey: e,
                onComplete: function (r) {
                  var l = r.results || [],
                    d = a._generateSearchData(l),
                    c = r.infos.next_start.split(" ")[0] === r.infos.nhits + "";
                  0 === l.length || c
                    ? delete a._requestInfo[t]
                    : ((a._requestInfo[t] = a._requestInfo[t] || {}),
                      (a._requestInfo[t].key = e),
                      (a._requestInfo[t].pid = i),
                      (a._requestInfo[t].isSearch = !0),
                      (a._requestInfo[t].next_start = r.infos.next_start)),
                    (a._pathInfo[t] = ""),
                    (a._searchInfo[t] = { key: e, pid: i }),
                    d.length > 0
                      ? a
                          ._pathData(d, t)
                          .then(function (e) {
                            n || delete a._subBookmarkExpand[t],
                              a.attachExpandChild(t, e),
                              o(e);
                          })
                          .catch(function (e) {
                            s(e);
                          })
                      : (n || delete a._subBookmarkExpand[t], o(d));
                },
                onFailure: function (e, t, i) {
                  s(t),
                    a._logger.error(
                      "_searchData - searchInActiveBookmark Failed: ",
                      e
                    );
                },
              });
            })
          );
        },
        shouldDisplayTooltip: function (e) {
          if (-1 != e.rowID) {
            var t = e.nodeModel;
            return (
              (t.getID =
                t.getID ||
                function () {
                  return this.options.id;
                }),
              (t.getType =
                t.getType ||
                function () {
                  return this.options.plmType;
                }),
              (t.getLabel =
                t.getLabel ||
                function () {
                  return this.options.label;
                }),
              ("tree" !== e.dataIndex && "label" !== e.dataIndex) ||
              "Path" === e.nodeModel.getType()
                ? "Path" === e.nodeModel.getType()
                  ? { shortHelp: "" }
                  : "tooltip" === e.dataIndex
                  ? { shortHelp: e.nodeModel.path }
                  : void 0
                : { shortHelp: e.nodeModel._options.label }
            );
          }
        },
        couldBeSelected: function (e) {
          return (
            !e.getType || ("Content" !== e.getType() && "Path" !== e.getType())
          );
        },
        sortFunction: function (e, t, i, n) {
          var o;
          if (i.getParent().plmType === this._favoriteSection) return null;
          if (i.isRoot() || n.isRoot()) return null;
          if (
            i.plmType === this._favoriteSection ||
            i.plmType === this._allSection
          )
            return null;
          if ("date" !== e) {
            var a = i.options.label ? i.options.label : "",
              s = n.options.label ? n.options.label : "";
            return p.compareValue(s, a);
          }
          try {
            o =
              i.options.created > n.options.created
                ? 1
                : i.options.created < n.options.created
                ? -1
                : 0;
          } catch (e) {
            o = i.options.created < n.options.created ? -1 : 1;
          } finally {
            return o;
          }
        },
        sortByReload: function (e, t, i) {
          var n = this;
          return (
            (t = "alpha" === t ? "ds6w:label" : "ds6w:created"),
            n._getWebServiceRef().setOrder(t, i),
            new o(function (t, i) {
              if (e)
                if (0 !== e.indexOf("search")) t(n.fetchData(e, !0));
                else {
                  var o = n._searchInfo[e] || {};
                  t(n._searchData(o.key, e, o.pid, !0));
                }
              else n.nestedMenuInContext._applySort(), t(!1);
            })
          );
        },
        getPathOfActiveBookmark: function (e) {
          var t = this;
          return new o(function (i, n) {
            t._getWebServiceRef().pathInActiveBookmark({
              ids: [e],
              onComplete: function (o) {
                var a = o.result,
                  s = {},
                  r = !1,
                  l = c.IPClassifyActive_msg_pathNotAccesible;
                if (a.length > 0 && a[0].input_object["ds6w:label"]) {
                  var d =
                    "Personal Workspace" == a[0].input_object["ds6w:type"];
                  if (a[0].outputs)
                    if (a[0].outputs.GET_PARENT && a[0].outputs.GET_PARENT[0])
                      d =
                        "Personal Workspace" ==
                        a[0].outputs.GET_PARENT[0]["ds6w:type"];
                  r =
                    !!d ||
                    !UWA.is(t._getRootVisbility, "function") ||
                    t._getRootVisbility(a);
                } else {
                  (l = c.IPClassifyActive_msg_objectNotAccesible),
                    (t.nestedMenuInContext._activeBookmarkId = ""),
                    (t.nestedMenuInContext._activeBookmarkLabel = ""),
                    t.nestedMenuInContext.dispatchEvent("setActive", {}),
                    t.nestedMenuInContext.nestedMenu.setActiveNode(""),
                    t.nestedMenuInContext._updateActivePanel();
                  var u =
                    t.nestedMenuInContext._selectBookmarkNodeModel ||
                    t.nestedMenuInContext._currentBookmarkNodeModel;
                  t.nestedMenuInContext._checkValidBtn(u);
                }
                r
                  ? (a.forEach(function (e) {
                      var i =
                          e.outputs &&
                          e.outputs.GET_PARENT &&
                          e.outputs.GET_PARENT[0],
                        n = e.input_object.physicalid,
                        o = "root";
                      if (i) {
                        if (
                          ((o = i.physicalid),
                          i["ds6w:label"],
                          i["ds6w:type"],
                          i["ds6w:created"],
                          i["ds6w:modified"],
                          i["ds6w:classification"],
                          i["icon_2ddefaultthb.subtype"],
                          i.paths)
                        ) {
                          var a = i.paths.filter(function (e) {
                            return e[0].physicalid === n;
                          })[0];
                          if (a && a.length > 1) {
                            (o = a[1].physicalid),
                              a[1]["ds6w:label"],
                              a[1]["ds6w:type"],
                              a[1]["ds6w:created"],
                              a[1]["ds6w:modified"],
                              a[1]["ds6w:classification"],
                              a[1]["icon_2ddefaultthb.subtype"];
                            for (var r = a.length - 1; r >= 0; r--) {
                              o = i;
                              r !== a.length - 1 && (o = a[r + 1]),
                                (s[a[r].physicalid] = {
                                  parentId: o.physicalid,
                                  parentName: o["ds6w:label"],
                                  parentType: o["ds6w:type"],
                                  parentCreated: o["ds6w:created"],
                                  parentModified: o["ds6w:modified"],
                                  parentClassification:
                                    o["ds6w:classification"],
                                  parentIcon: o["icon_2ddefaultthb.subtype"],
                                });
                            }
                          } else
                            s[n] = {
                              parentId: i.physicalid,
                              parentName: i["ds6w:label"],
                              parentType: i["ds6w:type"],
                              parentCreated: i["ds6w:created"],
                              parentModified: i["ds6w:modified"],
                              parentClassification: i["ds6w:classification"],
                              parentIcon: i["icon_2ddefaultthb.subtype"],
                            };
                        }
                        s[i.physicalid] = {
                          parentId: t._allSection,
                          parentName: t._getRootName(),
                          parentType: "",
                        };
                      } else s[n] = { parentId: t._allSection, parentName: t._getRootName(), parentType: "" };
                    }),
                    (e !== t._allSection && e !== t._favoriteSection) ||
                      (s[e] = {
                        parentId: void 0,
                        parentName: "",
                        parentType: "",
                      }),
                    a[0] &&
                      (s.current = {
                        parentId: a[0].input_object.physicalid,
                        parentName: a[0].input_object["ds6w:label"],
                        parentType: a[0].input_object["ds6w:type"],
                        parentCreated: a[0].input_object["ds6w:created"],
                        parentModified: a[0].input_object["ds6w:modified"],
                        parentClassification:
                          a[0].input_object["ds6w:classification"],
                        parentIcon:
                          a[0].input_object["icon_2ddefaultthb.subtype"],
                      }),
                    i(s))
                  : (n(),
                    t.options.isRootSelectable ||
                      C.displayNotification({
                        message: l,
                        className: "warning",
                      }));
              },
              onFailure: function (e, t, i) {
                n(e, t, i);
              },
            });
          });
        },
        generateParentChain: function (e, t, i) {
          var n = this;
          return new o(function (o, a) {
            for (var s = []; e !== t.id; ) {
              var r = i[e],
                l = { useAsyncPreExpand: !0 };
              (l.label = r.parentName),
                (l.icons = r.parentName),
                (l.id = r.parentId),
                (l.plmType = n._getPlmType(r.parentType)),
                (l.created = r.parentCreated),
                (l.modified = r.parentModified),
                (l.folderClassification = r.parentClassification),
                (l.iconType = r.parentIcon),
                (l.mxid = r.mxid),
                (l.icons = [
                  n._getIcon({ type: l.plmType, iconType: l.iconType }),
                ]),
                (l.children = []),
                (e = r.parentId),
                s.push(l);
            }
            s.reverse();
            var d = t,
              c = [];
            s.forEach(function (e, t) {
              d.id !== e.id &&
                (d.expand({ onlyUI: !0 }),
                (d = n._createTreeNodeModel(e, d)),
                c.push(d));
            }),
              t.setState({ state: "partiallyExpanded" }),
              c.forEach(function (e, t) {
                t !== c.length - 1 &&
                  e.setState({ state: "partiallyExpanded" });
              }),
              o(d);
          });
        },
        fetchData: function (e, t) {
          if (!e)
            return new o(function (e, t) {
              e([]);
            });
          var i = this,
            n = this.nestedMenuInContext.getSortByCursor(e),
            a = "alpha" === n.sortField ? "ds6w:label" : "ds6w:created";
          return (
            i._getWebServiceRef().setOrder(a, n.sortBy),
            new o(function (n, o) {
              e === i._allSection
                ? i._getWebServiceRef().searchInActiveBookmark({
                    nresults: 1e3,
                    content: "All",
                    onComplete: function (o) {
                      var a = o.results || [],
                        s =
                          o.infos.next_start.split(" ")[0] ===
                          o.infos.nhits + "";
                      0 === a.length || s
                        ? delete i._requestInfo[e]
                        : ((i._requestInfo[e] = i._requestInfo[e] || {}),
                          (i._requestInfo[e].isFetch = !0),
                          (i._requestInfo[e].next_start = o.infos.next_start),
                          (i._requestInfo[e].isContentSearch =
                            o.isContentSearch));
                      var r = i._generateSearchData(a);
                      t || delete i._subBookmarkExpand[e],
                        i.attachExpandChild(e, r),
                        n(r);
                    },
                    onFailure: function (e, t, n) {
                      o(e),
                        i._logger.error(
                          "fetchData - searchInActiveBookmark Failed: ",
                          e
                        );
                    },
                  })
                : e === i._favoriteSection ||
                  i._getWebServiceRef().fetchInActiveBookmark({
                    pid: e,
                    onComplete: function (o) {
                      var a = o.results || [];
                      0 ===
                        (a = a.map(function (e) {
                          var t = {};
                          return (
                            e.attributes.forEach(function (e) {
                              t[e.name] = e.value;
                            }),
                            t
                          );
                        })).length || o.isEnd
                        ? delete i._requestInfo[e]
                        : ((i._requestInfo[e] = i._requestInfo[e] || {}),
                          (i._requestInfo[e].isFetch = !0),
                          (i._requestInfo[e].pid = e),
                          (i._requestInfo[e].next_start = o.infos.next_start),
                          (i._requestInfo[e].isContentSearch =
                            o.isContentSearch));
                      var s = i.generateData(a);
                      t || delete i._subBookmarkExpand[e],
                        i.attachExpandChild(e, s),
                        n(s);
                    },
                    onFailure: function (e, t, n) {
                      o(e),
                        i._logger.error(
                          "fetchData - fetchInActiveBookmark Failed: ",
                          e
                        );
                    },
                  });
            })
          );
        },
        attachExpandChild: function (e, t) {
          var i = this._subBookmarkExpand[e],
            n = {};
          for (var o in i) {
            var a = i[o];
            a && a.isExpanded() && (n[a.id] = a);
          }
          var s = function (e) {
            e.expand({ onlyUI: !0 }),
              e.getChildren() &&
                e.getChildren().forEach(function (e) {
                  e.isExpanded() && s(e);
                });
          };
          t &&
            t.forEach(function (e) {
              n[e.id] &&
                ([].concat(n[e.id].getChildren()).forEach(function (t) {
                  e.addChild(t);
                }),
                (i[e.id] = e),
                s(e));
            });
        },
        fetchPersonalBookmark: function () {
          var e =
            null == window.activatePersonalBookmark ||
            window.activatePersonalBookmark;
          return this.getPersonalBookmark && e
            ? this.getPersonalBookmark()
            : o.resolve();
        },
        firstScreenFetch: function () {
          var e = this,
            t = new a();
          return (
            t.onPreExpand(function (e) {
              e.data.onlyUI && e.target.preExpandDone();
            }),
            new o(function (i, n) {
              e.fetchPersonalBookmark().then(function (n) {
                n &&
                  (e._rootNodePersonalSection = e._createTreeNodeModel(n, t));
                var o = e._getFavoritesIcon(),
                  a = { useAsyncPreExpand: !0 };
                (a.label = e._getFavoritesName()),
                  (a.icons = [o]),
                  (a.children = []),
                  (a.id = e._favoriteSection),
                  (a.plmType = e._favoriteSection),
                  (e._rootNodeFavorite = e._createTreeNodeModel(a, t));
                var s = e._getRootIcon(),
                  r = { useAsyncPreExpand: !0 };
                (r.label = e._getRootName()),
                  (r.icons = [s]),
                  (r.children = []),
                  (r.id = e._allSection),
                  (r.plmType = e._allSection),
                  (e._rootNodeFolders = e._createTreeNodeModel(r, t)),
                  e.options.hideFavorite && e._rootNodeFavorite.hide(),
                  i(t.getChildren());
              });
            })
          );
        },
        _populateFavoriteSection: function (e) {
          var t = this;
          t._rootNodeFavorite
            ? t._rootNodeFavorite._isPopulated
              ? e && e.onComplete && e.onComplete()
              : t.getFavorites({
                  onComplete: function (i) {
                    var n = { useAsyncPreExpand: !1 };
                    i.forEach(function (e) {
                      (n.label = e.name), (n.isPersonal = e.isPersonal);
                      var i = t._getIcon(e, "favorites");
                      (n.icons = [i]),
                        (n.children = null),
                        (n.id = e.id),
                        (n.mxid = e.mxid),
                        e.type ? (n.plmType = e.type) : (n.plmType = "Folder"),
                        (n.isFavorite = !0),
                        t
                          ._createTreeNodeModel(n, t._rootNodeFavorite)
                          .setState({ state: "noChildren" });
                    }),
                      (t._rootNodeFavorite._isPopulated = !0),
                      e && e.onComplete && e.onComplete();
                  },
                  onFailure: function (i) {
                    t._logger.error(
                      "FolderTreeCont : unable to initialize list of Favorite Folders ",
                      i
                    ),
                      e && e.onFailure && e.onFailure(i);
                  },
                })
            : e &&
              e.onFailure &&
              e.onFailure(
                new Error("FolderSection_favorite node does not yet exist")
              );
        },
        _refreshFavoriteSection: function (e) {
          if (this._rootNodeFavorite) {
            this._rootNodeFavorite.removeChildren(),
              (this._rootNodeFavorite._isPopulated = !1);
            var t = v.getDataProvider();
            t && t.onFavoriteSectionRefresh && t.onFavoriteSectionRefresh(),
              this._populateFavoriteSection(e);
          } else
            this._logger.log(
              "FolderTreeCont : FolderSection_favorite node does not yet exist"
            ),
              e &&
                e.onFailure &&
                e.onFailure(
                  new Error("FolderSection_favorite node does not yet exist")
                );
        },
        _onInfiniteScrollAsync: function (e, t) {
          var i = this,
            n = this.nestedMenuInContext.getSortByCursor(e),
            a = "alpha" === n.sortField ? "ds6w:label" : "ds6w:created";
          return (
            i._getWebServiceRef().setOrder(a, n.sortBy),
            new o(function (t, o) {
              var a = i._requestInfo[e];
              a
                ? a.isSearch &&
                  i._getWebServiceRef().searchInActiveBookmark({
                    next_start: a.next_start,
                    searchKey: a.key,
                    pid: a.pid,
                    onComplete: function (s) {
                      var r = s.results || [],
                        l = i._generateSearchData(r),
                        d =
                          s.infos.next_start.split(" ")[0] ===
                          s.infos.nhits + "";
                      0 === r.length || d
                        ? delete i._requestInfo[e]
                        : ((i._requestInfo[e] = i._requestInfo[e] || {}),
                          (i._requestInfo[e].key = a.key),
                          (i._requestInfo[e].pid = a.pid),
                          (i._requestInfo[e].isSearch = !0),
                          (i._requestInfo[e].next_start = s.infos.next_start)),
                        l.length > 0
                          ? i
                              ._pathData(l, e)
                              .then(function (o) {
                                i.attachExpandChild(e, o),
                                  t({ children: o, sort: n });
                              })
                              .catch(function (e) {
                                o(e);
                              })
                          : t({ children: l, sort: n });
                    },
                    onFailure: function (e, t, n) {
                      i._logger.error(
                        "_onInfiniteScrollAsync : searchInActiveBookmark failed ",
                        e
                      ),
                        o(e);
                    },
                  })
                : t({ children: [] });
            })
          );
        },
        _onPreCollapseTreeNode: function (e, t) {
          var i = e.data.nodeModel;
          if (i.isRoot()) {
            var n = i.getID();
            this._subBookmarkExpand[t] && delete this._subBookmarkExpand[t][n];
          }
        },
        _onPreExpandTreeNode: function (e, t) {
          var i = e.data.nodeModel;
          if (e.data.onlyUI) i.preExpandDone();
          else {
            var n = this;
            if (i.plmType !== n._favoriteSection) {
              var o = i.id;
              i.plmType == n._allSection &&
                (this._rootServiceHandler && this._rootServiceHandler.cancel(),
                (o = void 0));
              var a = i.plmType === n._allSection;
              this._getWebServiceRef().expandInActiveBookmark({
                pid: o,
                level: e.data.expandLevel,
                isRoot: a,
                onComplete: function (o) {
                  var s = o.results || [];
                  if (a) {
                    if (!i.isExpanded()) {
                      var r = [];
                      i.getChildren().length > 0 &&
                        (r = i.getChildren().map((e) => e.id));
                      var l = n._generateSearchData(s);
                      if (
                        (i.prepareUpdate(),
                        l.forEach(function (e) {
                          if (r.length > 0)
                            if (r.indexOf(e.id) > -1) {
                              var t = r.indexOf(e.id);
                              t > -1 && r.splice(t, 1);
                            } else i.addChild(e);
                          else i.addChild(e);
                        }),
                        r.length > 0)
                      )
                        for (var d = 0; d < i.getChildren().length; d++) {
                          var c = i.getChildren()[d].id;
                          r.indexOf(c) > -1 &&
                            (i.getChildren().splice(d, 1), (d -= 1));
                        }
                      i.pushUpdate(), n.nestedMenuInContext._applySort();
                    }
                  } else
                    n._generateExpandData(s, i, "All" === e.data.expandLevel);
                  if (
                    (null === i.options.children && (i.options.children = []),
                    i.preExpandDone(),
                    i.isRoot())
                  ) {
                    var u = i.getID();
                    (n._subBookmarkExpand[t] = n._subBookmarkExpand[t] || {}),
                      (n._subBookmarkExpand[t][u] = i);
                  }
                },
                onFailure: function (e) {
                  (i.loadHasFailed = !0), i.preExpandDone();
                },
              });
            } else
              i.plmType === n._favoriteSection
                ? n._refreshFavoriteSection({
                    onComplete: function () {
                      i.preExpandDone();
                    },
                  })
                : i.preExpandDone();
          }
        },
        _createTreeNodeModel: function (e, t, i) {
          var n = t instanceof a,
            o = (e.publishOnConnect, e.folderClassification, null),
            r = v.getDataProvider();
          r && r.getBadge && (o = r.getBadge(e)),
            delete e.publishOnConnect,
            delete e.folderClassification;
          var l = new s(e);
          return (
            (l.id = e.id),
            (l.plmType = e.plmType),
            e.isFavorite && (l.isFavorite = !0),
            t.isPersonal && (l.isPersonal = !0),
            e.created && (l.created = e.created),
            (l._instanceType = "FolderEditor/TreeNodeModel"),
            (l._loadingcounter = 0),
            (l.getID = function () {
              var e = this.id;
              return (e && "" !== e) || (e = this.plmType), e;
            }),
            (l.getLabel = function () {
              return this.options.label;
            }),
            (l.getType = function () {
              return this.plmType;
            }),
            (l.mxid = e.mxid),
            (l.options.oldLabel = e.label),
            (l.pathId = (n ? "root" : t.pathId) + "," + l.getID()),
            o && l.setBadges(o),
            n ? t.addRoot(l) : t.addChild(l, i),
            l
          );
        },
        addNewNode: function (e, t) {
          return (
            (e.useAsyncPreExpand = !0),
            (e.label = e["ds6w:label"]),
            (e.id = e.physicalid),
            (e.plmType = this._getPlmType(e["ds6w:type"])),
            (e.created = e["ds6w:created"]),
            (e.modified = e["ds6w:modified"]),
            (e.folderClassification = e["ds6w:classification"]),
            (e.iconType = e.type_icon_url || e["icon_2ddefaultthb.subtype"]),
            (e.icons = [
              this._getIcon({ type: e.plmType, iconType: e.iconType }),
            ]),
            this.couldBeExpand(e) && (e.children = []),
            this._createTreeNodeModel(e, t)
          );
        },
        __onContextualMenuReady: function (e) {
          var t,
            i = e.cellInfos || e.treeview;
          return (
            (i.nodeModel ? i.nodeModel : i.cellModel) &&
              (!1 === i.cellModel._isSelected
                ? ((i.cellModel._isSelected = !0), i.nodeModel.select(!0))
                : !1 === i.nodeModel.isSelected() && i.nodeModel.select(!0),
              (t = this._getContextualMenu(i, "TREE")),
              -1 !==
                [this._allSection, this._favoriteSection].indexOf(
                  i.nodeModel.getID()
                ) && (t = t.slice(-1)),
              t &&
                t.forEach(function (e) {
                  "separator" === e.type || "SeparatorItem" === e.type
                    ? (e.type = "SeparatorItem")
                    : ((e.title = e.label),
                      (e.action = { callback: e.callback }),
                      (e.type = "PushItem"));
                })),
            t
          );
        },
        isItAmongFavorites: function (e) {
          var t = !1;
          if (e && e.id) {
            var i = this._rootNodeFavorite.getChildren();
            if (i)
              for (var n = 0; n < i.length; n++) {
                var o = i[n];
                if (e.id == o.id) {
                  t = !0;
                  break;
                }
              }
          }
          return t;
        },
        getFavorites: function (t) {
          return this._getWebServiceRef().getUserPreferences({
            name: this.getFavoriteKey() + u.getPlatformId(),
            onComplete: function (i) {
              var n = [];
              if (i.length > 0) {
                var o = e.base64Decode(i);
                (o = JSON.parse(o)).data &&
                  o.data.data &&
                  o.data.data.favorite_folders &&
                  (n = o.data.data.favorite_folders);
              }
              t.onComplete(n);
            },
            onFailure: function (e) {
              t.onFailure && t.onFailure(e);
            },
          });
        },
        _setFavorites: function (t) {
          var i = t.favoriteFolders;
          if (Array.isArray(i)) {
            var n = {};
            n.data = this.getAppSpecificFavJson();
            for (var o = 0; o < i.length; o++) {
              var a = i[o];
              if (a.id && a.name) {
                var s = { id: a.id, name: a.name };
                (s.type = a.type),
                  (s.mxid = a.mxid ? a.mxid : void 0),
                  a.inherit && (s.inherit = !0),
                  n.data.data.favorite_folders.push(s);
              }
            }
            this._getWebServiceRef().setUserPreferences({
              name: this.getFavoriteKey() + u.getPlatformId(),
              value: e.base64Encode(JSON.stringify(n)),
              onComplete: function (e) {
                t.onComplete && t.onComplete(n.data.data.favorite_folders);
              },
              onFailure: function (e) {
                C.displayNotification({
                  message: c.msg_error_preferencessnotsaved,
                  className: "error",
                }),
                  t.onFailure && t.onFailure(e);
              },
            });
          } else
            t.onFailure &&
              t.onFailure(new Error("options.favoriteFolders is not an array"));
        },
        addFavorite: function (e, t) {
          if (e) {
            var i = this,
              n = Array.isArray(e) ? e : [e],
              o = [];
            n.forEach(function (e) {
              o.push(e.id);
            }),
              this.getFavorites({
                onComplete: function (e) {
                  var a,
                    s,
                    r,
                    l = e;
                  for (a = 0; a < n.length; ++a) {
                    r = !1;
                    var d = n[a];
                    for (s = 0; s < l.length && !r; ++s) {
                      var u = l[s];
                      d.id === u.id && (delete n[a], (r = !0));
                    }
                  }
                  for (a = 0; a < n.length; ++a) n[a] && l.push(n[a]);
                  i._setFavorites({
                    favoriteFolders: l,
                    onComplete: function (e) {
                      e.forEach(function (e) {
                        if (!1 === i.isItAmongFavorites(e)) {
                          var n = { useAsyncPreExpand: !1 };
                          (n.label = e.name),
                            (n.isPersonal = e.isPersonal),
                            (n.children = null),
                            (n.id = e.id),
                            (n.mxid = e.mxid),
                            e.inherit && (n.inherit = 1);
                          var a = i._getIcon(e, "favorites");
                          (n.icons = [a]),
                            e.type
                              ? (n.plmType = e.type)
                              : (n.plmType = "Folder"),
                            (n.isFavorite = !0),
                            i
                              ._createTreeNodeModel(n, i._rootNodeFavorite)
                              .setState({ state: "noChildren" }),
                            t(),
                            o.length > 0 &&
                              o.indexOf(e.id) > -1 &&
                              C.displayNotification({
                                message: c.replace(
                                  c.IPClassifyActive_msg_info_favoriteadded,
                                  { name1: e.name }
                                ),
                                className: "success",
                              });
                        }
                      });
                    },
                    onFailure: function (e) {
                      console.log("WebService _setFavorites Err : " + e);
                    },
                  });
                },
                onFailure: function (e) {
                  console.log("WebService getFavorites Err : " + e);
                },
              });
          }
        },
        removeFavorite: function (e, t) {
          for (
            var i = this,
              n = !1,
              o = i._rootNodeFavorite.getChildren(),
              a = Array.isArray(o) ? o.length : 0,
              s = 0;
            s < a;
            s++
          ) {
            var r = o[s];
            if (e.id == r.id) {
              (n = r.isSelected()),
                i._rootNodeFavorite.removeChild(r),
                !1 === i._rootNodeFavorite.hasChildren() &&
                  i._rootNodeFavorite.withTransactionUpdate(function () {
                    i._rootNodeFavorite.removeChildren(),
                      i._rootNodeFavorite.setState({ state: "collapsed" });
                  });
              break;
            }
          }
          i.getFavorites({
            onComplete: function (o) {
              for (var a = o, s = 0, r = 0; r < a.length; r++) {
                var l = a[r];
                if (e.id == l.id) {
                  a.splice(r, 1), (s = 1);
                  break;
                }
              }
              s
                ? i._setFavorites({
                    favoriteFolders: a,
                    onComplete: function (o) {
                      var a = e.getLabel() ? e.getLabel() : "";
                      t(),
                        C.displayNotification({
                          message: c.replace(
                            c.IPClassifyActive_msg_info_favoriteremoved,
                            { name1: a }
                          ),
                          className: "success",
                        }),
                        n && i._rootNodeFavorite.select();
                    },
                    onFailure: function (e) {},
                  })
                : console.log(
                    "FolderTreeCont favorite folder to remove, not found in list"
                  );
            },
            onFailure: function (e) {
              console.log("WebService getFavorites Err : " + e);
            },
          });
        },
        _createObject: function (e, t) {
          var i = this,
            n = t;
          e = e;
          new g({
            title: c.IPClassifyActive_panel_newBookmark,
            tenant: y.getSetting("x3dPlatformId"),
            parentPanel: i.nestedMenuInContext._modalContainer,
            callback: function (t) {
              if ("object" == typeof t && null !== t) {
                var o = n();
                o.createLoader();
                i.executeServiceToCreate(
                  { ctx: t.ctx, title: t.title, nodeId: e.id },
                  function (t) {
                    var n = { useAsyncPreExpand: !0 };
                    (n.label = t.title),
                      (n.id = t.id),
                      (n.plmType = i._getPlmType(t.type)),
                      (n.created = t.created),
                      (n.modified = t.modified),
                      (n.folderClassification = "Bookmark"),
                      (n.icons = [i._getIcon(n)]),
                      (n.children = []);
                    t = i._createTreeNodeModel(n, e);
                    e.expand({ onlyUI: !0 }),
                      e.setState({ state: "partiallyExpanded" }),
                      o.removeLoader();
                  }.bind(i),
                  function (e) {
                    C.displayNotification({
                      message: c.IPClassify_error_bookmakCannotBeCreated,
                      className: "error",
                    }),
                      o.removeLoader();
                  }.bind(i)
                );
              }
            },
          });
        },
      });
    }
  ),
  define(
    "DS/IPClassifyActive/IPClassifyActiveHTML",
    [
      "DS/IPClassifyActive/IPClassifyActive",
      "DS/IPClassifyActive/utils/IPClassifyActiveCommunication",
      "DS/WidgetServices/WidgetServices",
      "DS/WidgetServices/securityContextServices/SecurityContextServices",
      "DS/IPClassifyActive/utils/WidgetDataProvider",
    ],
    function (e, t, i, n, o) {
      window.top.performance.mark("start_EP_HTMLIPClassifyActive"),
        window.top.performance.mark("start_widgetLoading"),
        window.currentPropertiesOptions ||
          (window.currentPropertiesOptions = {});
      var a = null,
        s = null,
        r = {
          onLoad: function () {
            window.top.performance.mark("end_widgetLoading"),
              window.top.performance.mark("start_managePref");
            i.setWidgetValue("x3dAppId", "ENOLCMT_AP"),
              document.body.addClassName("IPClassifyActiveHTML"),
              window.top.performance.mark("end_managePref"),
              window.top.performance.mark("start_createProp"),
              (a = new e({})),
              window.top.performance.mark("end_createProp"),
              widget.body.setContent(a.getModalContainer()),
              a.nestedMenuInContext.initData(),
              a.nestedMenuInContext.updateView(),
              a.addEvent("onCancel", function () {
                console.log("Active classify event : onCancel");
                var e = o.getEventNameProvider();
                e && e.cancel && t.publish(e.cancel, !0, !0);
              }),
              a.addEvent("onSetActive", function (e) {
                console.log("Active classify event : onSetActive");
                var i = e.getID ? e.getID() : "",
                  n = e.getLabel ? e.getLabel() : "",
                  a = {
                    origin: widget.id,
                    id: i,
                    label: n,
                    sc:
                      widget.getPreference("pad_security_ctx") &&
                      widget.getPreference("pad_security_ctx").value,
                  };
                r._activeClassify = a;
                var s = o.getEventNameProvider();
                s && s.set && t.publish(s.set, r._activeClassify, !0);
              });
            var l = widget.getView().type;
            "fullscreen" != l && "maximized" != l && r._setBodyHeight("600"),
              (s = i.getWidgetPreference("securityContext")) &&
                s.value &&
                n
                  .getInstance({
                    context: {
                      getSecurityContext: function () {
                        return { SecurityContext: s.value };
                      },
                    },
                  })
                  .getSecurityContext();
            var d = i.getWidgetPreference("isTransient");
            d && "false" === d.value && i.deleteWidgetValue("SelectedItem"),
              i.setWidgetValue("isTransient", "false"),
              i.deleteWidgetValue("securityContext");
            var c = widget.body.getParent().getSize();
            r._setBodyHeight(c.height);
            currentPropertiesOptions.selected &&
              currentPropertiesOptions.selected;
            window.top.performance.mark("start_initdata"),
              window.top.performance.mark("end_initdata");
          },
          onRefresh: function () {},
          onResize: function (e) {
            var t = widget.body.getParent().getSize();
            r._setBodyHeight(t.height);
          },
          onDestroy: function (e) {},
          onViewChange: function (e) {
            var t = e.height;
            "fullscreen" !== e.type && "maximized" !== e.type && (t = "600"),
              t && r._setBodyHeight("600");
          },
          _setBodyHeight: function (e) {
            e &&
              ("auto" !== e && (e += "px"), widget.body.setStyle("height", e));
          },
        };
      i.isReplayedODT()
        ? setTimeout(function () {
            r.onLoad();
          })
        : widget.addEvent("onLoad", r.onLoad),
        widget.addEvent("onRefresh", r.onRefresh),
        widget.addEvent("onResize", r.onResize),
        widget.addEvent("onViewChange", r.onViewChange),
        window.addEventListener("unload", r.onDestroy),
        widget.setAutoRefresh(1e4);
    }
  );
