define(
  "DS/EditPropWidget/UI/EditPropSkeleton",
  [
    "UWA/Json",
    "DS/EditPropWidget/UI/IDCardManager",
    "DS/EditPropWidget/constants/EditPropConstants",
    "DS/WidgetServices/WidgetServices",
    "css!DS/EditPropWidget/EditPropWidget.css",
    "i18n!DS/EditPropWidget/assets/nls/langEditProp",
  ],
  function (e, t, i, n) {
    "use strict";
    return t.extend({
      defaultOptions: {
        className: "",
        typeOfDisplay: i.ALL,
        setEditButton: !1,
        displayType: i.STANDARD,
      },
      init: function (e, t) {
        var n = this;
        (t.typeOfDisplay = t.typeOfDisplay ? t.typeOfDisplay : i.ALL),
          (t.displayType = t.displayType ? t.displayType : i.STANDARD),
          (t.className = t.className ? t.className : ""),
          (t.className += " EditProp_MasterContain"),
          (t.className += t.typeOfDisplay ? " " + t.typeOfDisplay : ""),
          (t.name = "EditProp_MasterContain"),
          (t.showIdCard =
            void 0 !== t.showIdCard
              ? t.showIdCard
              : t.typeOfDisplay !== i.ONLY_EDIT_PROPERTIES),
          (t.showFacets = t.typeOfDisplay === i.ALL),
          (t.showExpander = t.typeOfDisplay === i.ALL),
          (t.closeButton = i.isCloseButton(t.displayType)),
          (t.defaultFacetName = t.defaultFacetName ? t.defaultFacetName : ""),
          (t.infoForFacet = t.infoForFacet ? t.infoForFacet : null),
          (t.indexModeParameter =
            !!t.indexModeParameter && t.indexModeParameter),
          (t.idCardOptions = {
            events: {
              thumbnailClick: function () {
                n._getFirstPropertiesModel().get("activeThumbnail")
                  ? (n.elements.postIdCard &&
                      n.elements.postIdCard.maskThumbnail(),
                    require([
                      "DS/ImageManager/ImageManager",
                      "DS/EditPropWidget/ajaxRequests/ServicesAjaxRequests",
                    ], function (e, t) {
                      n.elements.postIdCard &&
                        n.elements.postIdCard.unmaskThumbnail();
                      var i = n._getFirstPropertiesModel(),
                        s = i ? i.get("objectId") : null;
                      !s ||
                        (n.elements.imageManager &&
                          n.elements.imageManager.getModelId() === s) ||
                        (n.elements.imageManager &&
                          n.elements.imageManager.destroy(),
                        (n.elements.imageManager = new e({
                          modelId: s,
                          tenantId: t.getTenantFromModel(i),
                          events: {
                            onPrimaryChange: function (e) {
                              (i = n._getFirstPropertiesModel()).set(
                                "previewUrl",
                                e ? e.url : ""
                              );
                              var t = i ? i.get("objectId") : null;
                              t &&
                                n.dispatchEvent("onPrimaryChange", {
                                  id: t,
                                  image: e,
                                }),
                                n._updateThumbnailForModel(i);
                            },
                          },
                        }))),
                        n.elements.imageManager &&
                          n.elements.imageManager.execute();
                    }))
                  : n._isFacetAuthorized(i.FACET_VIEWS) &&
                    n.selectFacet(i.FACET_VIEWS);
              },
            },
          }),
          this._parent(e, t),
          (this._sharedOptions.readOnly = !1 !== t.readOnly),
          this.options.typeOfDisplay === i.ONLY_IDENTITY_CARD &&
            (this.options.setEditButton = !1),
          (this.editMode = !0 === t.editMode),
          this.options.typeOfDisplay === i.ALL &&
            (this.elements.containerError = this.elements.globalDiv),
          (this.elements.mainError = UWA.createElement("div", {
            class: "EditProp_MainError",
          })
            .inject(this.elements.globalDiv)
            .hide());
      },
      setReadonlyStatus: function (e) {
        (this._sharedOptions.readOnly = e), this.onRefresh();
      },
      setOccurrences: function (e) {
        this.elements.imageManager &&
          (this.elements.imageManager.destroy(),
          delete this.elements.imageManager),
          this._parent(e);
      },
      _manageEvent: function (e) {
        this._parent(e);
        var t = this._getCurrentSelectedFacet(),
          i = this._getFacetName(t);
        e.facet === i &&
          "switchMode" === e.eventID &&
          this.elements.postIdCard &&
          this._manageActionVisibility();
      },
      clearPanel: function () {
        this._parent(),
          this._getErrorManager(function (e) {
            e.clearAllMessages();
          });
      },
      _getErrorManager: function (e) {
        var t = this;
        this.errorOverlay
          ? e.call(null, this.errorOverlay)
          : require(["DS/ErrorWidget/ErrorWidget"], function (i) {
              t.errorOverlay ||
                (t.errorOverlay = new i(
                  n.getWidgetObject(),
                  t.elements.containerError,
                  { className: "editProperties_errorManager" }
                )),
                e.call(null, t.errorOverlay);
            });
      },
      _addNotif: function (e) {
        var t = e.msg,
          i = e.eventID,
          n = e.customClass;
        switch (i) {
          case "mainstaticerror":
            (t = t || e.json), this.showMainErrorArea(t);
            break;
          case "error":
            (t = t || e.json), this._addError(t, n);
            break;
          case "warning":
            this._addWarning(t, n);
            break;
          case "success":
            this._addSuccess(t, n);
            break;
          default:
            this._addInfo(t, n);
        }
      },
      _addInfo: function (e, t) {
        var i = this;
        this._getErrorManager(function (n) {
          var s = n.addInfo(e, t);
          i.dispatchEvent("onNotification", s);
        });
      },
      _addWarning: function (e, t) {
        var i = this;
        this._getErrorManager(function (n) {
          var s = n.addWarning(e, t);
          i.dispatchEvent("onNotification", s);
        });
      },
      _addSuccess: function (e, t) {
        var i = this;
        this._getErrorManager(function (n) {
          var s = n.addSuccess(e, t);
          i.dispatchEvent("onNotification", s);
        });
      },
      _addError: function (t, i) {
        var n = this;
        this._getErrorManager(function (s) {
          var a = t;
          if (
            null != a &&
            ("string" == typeof a || a instanceof String) &&
            a.includes("ErrorCode:1501009")
          ) {
            a = {
              level: "error",
              title: UWA.i18n("InvalidCSCreationfailed"),
              subtitle: UWA.i18n("InvalidCSCreationFailedSubtitle"),
              message: UWA.i18n("NoAccessToCS"),
            };
            var o = s.display(a);
          } else o = s.displayJSONError(e.encode(t), i);
          n.dispatchEvent("onNotification", o);
        });
      },
      hideMainErrorArea: function () {
        this.elements.mainError.hide();
      },
      showMainErrorArea: function (e) {
        null != e &&
          "" != e &&
          (this.clearAllMainError(), this.addMainError(e)),
          this.elements.mainError.show();
      },
      addMainError: function (e) {
        new UWA.createElement("div", { text: e }).inject(
          this.elements.mainError
        );
      },
      clearAllMainError: function () {
        this.elements.mainError.setContent("");
      },
      showMainComponent: function () {
        this._parent(), this.hideMainErrorArea();
      },
    });
  }
),
  define("DS/EditPropWidget/constants/EditPropConstants", [], function () {
    "use strict";
    return {
      ONLY_IDENTITY_CARD: "editProp_onlyIC",
      ONLY_EDIT_PROPERTIES: "editProp_onlyProperties",
      ALL: "editProp_all",
      ALL_SELECTION: "allSelection",
      RESTRICTED_SELECTION: "restrictSelection",
      NO_SELECTION: "noSelection",
      MVA_SEP: "|",
      POPUP: "editProp_popup",
      TRANSIENT: "editProp_transient",
      STANDARD: "editProp_standard",
      FORCE_READWRITE: "force_readWrite",
      FORCE_READONLY: "force_readonly",
      FORCE_DEFAULT: "force_default",
      NO_CHANGES: "no_changes",
      ACTION_SWITCH: "pencil",
      ACTION_REFRESH: "cw",
      ACTION_RESET: "reset",
      ACTION_SWYM_VIEW_CONTENT_IN_3DSWYM: "swym_view_content_in_3dswym",
      ACTION_SWYM_VIEW_COMMUNITY_IN_3DSWYM: "swym_view_community_in_3dswym",
      ACTION_SWYM_VIEW_USER_PROFILE: "swym_view_user_profile",
      ACTION_SWYM_VIEW_SURVEY_IN_3DSWYM: "swym_view_survey_in_3dswym",
      ACTION_SHARING_FILTER_VIEW: "filter",
      ACTION_SHARING_DELETE: "deleteUser",
      ACTION_SHARING_ADD: "addUser",
      ACTION_SHARING_LIST_VIEW: "listView",
      FACET_NONE: "none",
      FACET_VIEWS: "views",
      FACET_PROPERTIES: "properties",
      FACET_iPROPERTIES: "iproperties",
      FACET_INSTANCE: "instance",
      FACET_iINSTANCE: "indexInstance",
      FACET_ACTIVITY: "activity",
      FACET_GENERALHISTORY: "history",
      FACET_SWYMCOMMENTS: "social",
      FACET_EBOM: "EBOMPart",
      FACET_SWYM_CONTENT: "swym_content",
      FACET_SWYM_COMMUNITY_PROPERTIES: "swym_community_properties",
      FACET_SWYM_USER_PROPERTIES: "swym_user_properties",
      FACET_SWYM_SURVEY_PROPERTIES: "swym_survey_properties",
      FACET_SPECIFICATIONS: "Specifications",
      FACET_Variability: "Variability",
      FACET_EFFECTIVITY: "Effectivity",
      FACET_CHANGE: "Change",
      FACET_iCHANGE: "iChange",
      FACET_ALTERNATE: "Alternate",
      FACET_IPPROTECTION: "IPProtection",
      FACET_RICHCONTENT: "Rich Content Attr",
      FACET_DOCUMENTS: "documents",
      FACET_FILES: "Files",
      FACET_PROXY: "proxyFacet",
      FACET_ADMIN: "adminFacet",
      FACET_RELATIONS: "relations",
      FACET_DERIVEDOUTPUT: "derivedoutputs",
      FACET_REVISIONS: "revisions",
      FACET_SHARING: "sharing",
      FACET_PHYSICSSIMULATION: "physicssimulation",
      FACET_POSTERRORANALYSIS: "postErrorAnalysis",
      FACET_PC_CONTENT: "product_configuration_content",
      FACET_FL_SPECIFICATION: "catfl_specification_document",
      MODEL_INIT: "_Model_init",
      MODEL_FACET_INFO_PREFIX: "_DS_FACET_",
      MODEL_DS_ATTR: "_DS_ATTR_",
      METATYPE_BUS: "businessobject",
      METATYPE_REL: "relationship",
      METATYPE_PATH: "path",
      METATYPE_UNKNOWN: "unknown",
      MULTISEL_STD: "yes",
      MULTISEL_NO: "no",
      MULTISEL_MERGE: "merge",
      EP_RESPONSE_OK: "OK",
      EDITPROPINDEX_DIMENSION: "DSDim_",
      EDITPROPINDEX_DEFAULTTYPE: "TypeNotAvailable",
      COLLAPSE_IC_PREFERENCE: {
        type: "hidden",
        name: "editProperty_collapseIC",
        label: "is IC collapse",
        defaultValue: "false",
      },
      TYPES: {
        INTEGER: "integer",
        REAL: "real",
        TIMESTAMP: "timestamp",
        PERSON: "person",
        BOOLEAN: "boolean",
        ENUM: "enum",
      },
      MIN_WIDTH_FOR_TABLE: 200,
      WIDTH_SWITCH_MODE: 150,
      VARIABLES: { minWidthForTable: 200 },
      ACCORDION: { ATTRIBUTES: "Attributes" },
      isCloseButton: function (e) {
        return e !== this.STANDARD;
      },
    };
  }),
  define("DS/EditPropWidget/comm/CommManager", ["UWA/Class"], function (e) {
    "use strict";
    return e.extend({
      subscriptions: [],
      lCurrentListId: [],
      _nbOfCancelReceived: 0,
      _statusReady: "ready",
      _statusRunning: "running",
      _commName: null,
      _currentStatus: null,
      _currentListenStatus: null,
      init: function (e) {
        (this._currentStatus = null),
          (this._currentListenStatus = null),
          (this.subscriptions = []),
          (this.lCurrentListId = []),
          (this._nbOfCancelReceived = 0),
          e || (e = "default"),
          (this._commName = e);
      },
      getCommObject: function (e) {
        var t = window.top.EP_CommManager;
        return t || !0 === e || (t = window.top.EP_CommManager = {}), t;
      },
      getCurrentComm: function (e) {
        var t = this.getCommObject(e);
        return (
          (t = t || {})[this._commName] ||
            e ||
            (t[this._commName] = { callbacks: [] }),
          t[this._commName]
        );
      },
      _removeCurrentComm: function () {
        this.getCommObject(!0) &&
          delete window.top.EP_CommManager[this._commName];
      },
      createRunningEvent: function (e) {
        (this.lCurrentListId = e),
          (this._currentStatus = {
            status: this._statusRunning,
            answer: this.lCurrentListId,
          }),
          (this.getCurrentComm().status = this._currentStatus);
      },
      createReadyEvent: function (e) {
        this._currentStatus = { status: this._statusReady, answer: e };
        var t = this.getCurrentComm();
        (t.status = this._currentStatus),
          t.callbacks &&
            t.callbacks.length > 0 &&
            (t.callbacks.forEach(function (t) {
              t.call(null, e);
            }),
            this._removeCurrentComm());
      },
      createListenEvent: function (e) {
        var t = this.getCurrentComm(!0);
        t &&
          (t.status.status === this._statusReady
            ? (e.call(null, t.status.answer), this._removeCurrentComm())
            : t.callbacks.push(e));
      },
      getCurrentListenStatus: function () {
        var e = this.getCurrentComm(!0),
          t = null;
        return e && (t = e.status.status), t;
      },
      destroy: function () {
        this._removeCurrentComm(), this._parent();
      },
    });
  }),
  define(
    "DS/EditPropWidget/utils/AttributesUtils",
    [
      "UWA/Array",
      "DS/WidgetServices/WidgetServices",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i) {
      "use strict";
      return {
        defaultOptions: {},
        validateModels: function (e) {
          for (var t = null, i = e.length, n = 0; n < i; n++) {
            var s = e[n];
            t = this._validateAllAttributes(s);
          }
          return t;
        },
        validateAttributes: function (e, t) {
          var i = this,
            n = [],
            s = [];
          return (
            t.forEach(function (t) {
              if (t ? t.name : null) {
                !0 !== t.get("error").server && t.resetError();
                var n = t.valueDB,
                  a = null;
                if (e.isMergedModel()) {
                  var o = Object.keys(n);
                  o.length > 0 && (a = o[0]);
                }
                if (
                  (null != a && (n = n[a]),
                  null !==
                    (n =
                      n ||
                      (!0 === t.get("readOnly")
                        ? null
                        : null != a
                        ? t.value[a]
                        : t.value)))
                ) {
                  var r = i.checkValueForAttribute(t, n, s);
                  r && r.message && t.setError(r.message, r.type);
                }
              }
            }),
            s.length > 0
              ? s.forEach(function (e) {
                  var t = {
                    customClass:
                      e.attrName && e.attrName.length > 0
                        ? "EP_Warning_" + e.attrName
                        : "EP_Warning_Message",
                    message: e.message,
                    code: "ERROR",
                    eventID: "warning",
                  };
                  n.push(t);
                })
              : (n = null),
            n
          );
        },
        checkValueForAttribute: function (n, s, a) {
          var o = this,
            r = n ? n.name : null,
            l = { message: null, type: null };
          if (r && !0 !== n.get("readOnly")) {
            var c = n.label;
            return (
              (c = c ? c.toLowerCase() : ""),
              null !== s &&
                (Array.isArray(s) || (s = [s]),
                0 == s.length &&
                  !0 === n.mandatory &&
                  (l = o.addError(a, n, c, "MandatoryAttribute")),
                s.every(function (s) {
                  var r = null;
                  if (
                    ("function" == typeof n.validationFunction &&
                      (r = n.validationFunction(n)),
                    r &&
                      ((l = o.addError(a, n, n.label, r.message)).type =
                        r.type),
                    !0 === n.mandatory)
                  ) {
                    var d = s + "",
                      u = new RegExp(" ", "g");
                    "" === (d = d.replace(u, "")) &&
                      (l = o.addError(a, n, c, "MandatoryAttribute"));
                  }
                  if (
                    (n.maxlength > 0 &&
                      s.length > n.maxlength &&
                      (l = o.addErrorWithValue(
                        a,
                        n,
                        c,
                        n.maxlength,
                        "MaxLengthAttribute"
                      )),
                    "real" === n.type || "integer" === n.type)
                  ) {
                    d = s;
                    e.isArray(d)
                      ? d.forEach(function (e) {
                          isNaN(e) &&
                            (l = o.addErrorWithValue(
                              a,
                              n,
                              n.label,
                              e,
                              "NotaNumber"
                            ));
                        })
                      : isNaN(d)
                      ? (l = o.addErrorWithValue(
                          a,
                          n,
                          n.label,
                          d,
                          "NotaNumber"
                        ))
                      : "integer" === n.type &&
                        d % 1 != 0 &&
                        (l = o.addErrorWithValue(
                          a,
                          n,
                          n.label,
                          d,
                          "IntegerIsDecimal"
                        ));
                  }
                  if ("" !== n.minval) {
                    var h = parseFloat(n.minval),
                      p = n.value;
                    if (e.isArray(p))
                      p.forEach(function (e) {
                        var t = parseFloat(e);
                        t < h &&
                          (l = o.addErrorWithValue(
                            a,
                            n,
                            n.label,
                            e,
                            "OutOfMinVal"
                          )),
                          n.minvalincluded ||
                            t !== h ||
                            (l = o.addErrorWithValue(
                              a,
                              n,
                              n.label,
                              e,
                              "OutOfMinVal"
                            ));
                      });
                    else
                      (m = parseFloat(n.value)) < h &&
                        (l = o.addErrorWithValue(
                          a,
                          n,
                          n.label,
                          n.value,
                          "OutOfMinVal"
                        )),
                        n.minvalincluded ||
                          m !== h ||
                          (l = o.addErrorWithValue(
                            a,
                            n,
                            n.label,
                            n.value,
                            "OutOfMinVal"
                          ));
                  }
                  if ("" !== n.maxval) {
                    var m,
                      f = parseFloat(n.maxval);
                    p = n.value;
                    if (e.isArray(p))
                      p.forEach(function (e) {
                        var t = parseFloat(e);
                        t > f &&
                          (l = o.addErrorWithValue(
                            a,
                            n,
                            n.label,
                            e,
                            "OutOfMaxVal"
                          )),
                          n.maxvalincluded ||
                            t !== f ||
                            (l = o.addErrorWithValue(
                              a,
                              n,
                              n.label,
                              e,
                              "OutOfMaxVal"
                            ));
                      });
                    else
                      (m = parseFloat(n.value)) > f &&
                        (l = o.addErrorWithValue(
                          a,
                          n,
                          n.label,
                          n.value,
                          "OutOfMaxVal"
                        )),
                        n.maxvalincluded ||
                          m !== f ||
                          (l = o.addErrorWithValue(
                            a,
                            n,
                            n.label,
                            n.value,
                            "OutOfMaxVal"
                          ));
                  }
                  var g = n.get("forbiddenChar");
                  if (g) {
                    d = s;
                    if (t.commonChar(d, g, !0) > 0) {
                      var v = i.replace(i.get("ForbiddenCharacters"), {
                        label: n.label,
                        badChars: g,
                      });
                      a.push({ message: v, attrName: n.getIdentifier() }),
                        (l.message = v),
                        (l.type = "error");
                    }
                  }
                  return null === l.message;
                })),
              l
            );
          }
        },
        _validateAllAttributes: function (e) {
          var t = e.values();
          return this.validateAttributes(e, t);
        },
        addErrorWithValue: function (e, t, n, s, a) {
          i.get(a);
          var o = i.replace(i.get(a), { label: n, value: s });
          return (
            e.push({ message: o, attrName: t.getIdentifier() }),
            { message: o, type: "error" }
          );
        },
        addError: function (e, t, n, s) {
          i.get(s);
          var a = i.replace(i.get(s), { label: n });
          return (
            e.push({ message: a, attrName: t.getIdentifier() }),
            { message: a, type: "error" }
          );
        },
      };
    }
  ),
  define(
    "DS/EditPropWidget/buttonsBar/ButtonsBar",
    [
      "UWA/Controls/Abstract",
      "DS/Controls/TooltipModel",
      "DS/Controls/Button",
      "DS/UIKIT/DropdownMenu",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s) {
      return e.extend({
        defaultOptions: { vertical: !1, minimumSize: 0 },
        init: function (e, a) {
          this._parent(e),
            (this.elements.container = UWA.createElement("div", {
              class:
                "buttonsBar " +
                (e.classname ? e.classname : "") +
                (e.vertical ? " vertical" : ""),
            })),
            this.options.minimumSize < 0 && !a && (a = this.elements.container),
            (this.elements.previous = UWA.createElement("div", {
              class: "buttonsList",
            })),
            (this.elements.comboButton = new i({
              value: "dropdownAction",
              tooltipInfos: new t({
                shortHelp: s.dropActions,
                mouseRelativePosition: !0,
              }),
              icon: {
                iconName: "chevron-down",
                fontIconFamily: WUXManagedFontIcons.Font3DS,
              },
              displayStyle: "lite",
            })),
            (this.elements.dropdown = new n({
              target: this.elements.comboButton.getContent(),
              items: [],
            })),
            this.elements.comboButton.hide(),
            (this.elements.after = UWA.createElement("div", {
              class: "buttonsList",
            })),
            this.elements.previous.inject(this.elements.container),
            this.elements.comboButton.inject(this.elements.container),
            this.elements.after.inject(this.elements.container),
            (this._limitingDiv = a),
            (this._availableSize = 0),
            (this._sizeButtons = null),
            (this._buttons = {});
        },
        addButton: function (e, n) {
          var s = new i({
            value: e.name,
            tooltipInfos: new t({
              shortHelp: e.text,
              mouseRelativePosition: !0,
            }),
            icon: {
              iconName: e.icon,
              fontIconFamily: WUXManagedFontIcons.Font3DS,
            },
            displayStyle: "lite",
            type: e.type,
            onClick: function () {
              e.handler && e.handler();
            },
          });
          e.id && (s.domId = e.id);
          var a = this.elements.after;
          !0 !== e.after &&
            ((a = this.elements.previous),
            this.elements.dropdown.addItem({
              text: e.text,
              name: e.name,
              fonticon: e.icon,
              handler: e.handler,
            })),
            s.inject(a),
            (this._buttons[e.name] = { element: s, options: UWA.clone(e) }),
            (this._sizeButtons += 38),
            !1 !== n && this.refresh();
        },
        addButtons: function (e) {
          var t = this;
          e.forEach(function (e) {
            t.addButton(e, !1);
          }),
            this.refresh();
        },
        setButtons: function (e) {
          this.elements.previous.empty(),
            this.elements.after.empty(),
            this.clearButtons(),
            this.addButtons(e);
        },
        destroy: function () {
          this.clearButtons(), this._parent.apply(this, arguments);
        },
        clearButtons: function () {
          for (var e in this._buttons)
            this._buttons[e].element.destroy(),
              this.elements.dropdown.removeItem(e);
          (this._buttons = {}), (this._sizeButtons = 0);
        },
        hideButton: function (e) {
          this.elements.dropdown.disableItem(e);
          var t = this._buttons[e];
          t && (t.options._visible = !1),
            (this._sizeButtons -= 38),
            this.refresh();
        },
        showButton: function (e) {
          this.elements.dropdown.enableItem(e);
          var t = this._buttons[e];
          t && (t.options._visible = !0),
            (this._sizeButtons += 38),
            this.refresh();
        },
        updateButton: function (e, t) {
          (t.id = e), (t.name = e);
          var i = { id: e, name: e };
          t.text && (i.text = t.text),
            t.icon && (i.fonticon = t.icon),
            this.elements.dropdown.updateItem(i);
          var n = this._buttons[e];
          n &&
            (t.text && (n.element.getModel().tooltipInfos.shortHelp = t.text),
            t.icon &&
              (n.element.getModel().icon = {
                iconName: t.icon,
                fontIconFamily: WUXManagedFontIcons.Font3DS,
              }));
        },
        onResize: function () {
          if (this._limitingDiv && 0 !== this.options.minimumSize) {
            var e = this._limitingDiv;
            this.options.minimumSize;
            if (
              (this.options.minimumSize < 0 &&
                ((e = this._limitingDiv.getParent()), 0),
              e)
            ) {
              var t = e.getSize(),
                i = this.options.vertical ? t.height : t.width;
              this._availableSize = i - this.options.minimumSize;
            }
          }
          this.refresh();
        },
        refresh: function () {
          if (this._limitingDiv && this.options.minimumSize)
            if (
              0 !== this._availableSize &&
              this._availableSize < this._sizeButtons
            ) {
              var e = null;
              for (var t in this._buttons)
                !0 !== this._buttons[t].options.after &&
                  (null !== e
                    ? (!1 !== e &&
                        (e.element.hide(), this.elements.comboButton.show()),
                      (e = !1),
                      this._buttons[t].element.hide())
                    : (e = this._buttons[t]));
            } else
              for (var t in (this.elements.comboButton.hide(), this._buttons)) {
                var i = this._buttons[t];
                !1 !== i.options._visible && i.element.show();
              }
        },
      });
    }
  ),
  define("DS/EditPropWidget/changeAction/passThruJS", [], function () {
    "use strict";
    return {
      onReady: function (e) {
        require([
          "DS/CfgAuthoringContextUX/scripts/CfgAuthoringContext",
        ], function (t) {
          var i = {
              changeIsSetChecker: function (e) {
                return (
                  e &&
                  e.AuthoringContextHeader &&
                  Object.keys(e.AuthoringContextHeader).length > 0
                );
              },
            },
            n = [],
            s = [];
          for (var a in t)
            "function" == typeof t[a] && (n.push(t[a]), s.push(a));
          s.forEach(function (e, s) {
            i[e] = function (i, a) {
              if (
                "init" !== e &&
                "constructor" !== e &&
                "initialize" !== e &&
                "undefined" == typeof enoviaServerAuthoringWidget
              )
                return (
                  console.log("Authoring context not initialized"),
                  void ("function" == typeof i
                    ? i({})
                    : "function" == typeof a && a({}))
                );
              n[s].apply(t, arguments);
            };
          }),
            e.call(null, i);
        }, function () {
          e.call(null, null);
        });
      },
    };
  }),
  define("DS/EditPropWidget/models/HistoryObjectModel", [], function () {
    "use strict";
    return function (e) {
      for (var t in ((this.name = ""),
      (this.type = ""),
      (this.revision = ""),
      (this.id = ""),
      e))
        this[t] = e[t];
      (this.set = function (e, t) {
        void 0 !== t && (this[e] = t);
      }),
        (this.get = function (e) {
          return this[e];
        });
    };
  }),
  define("DS/EditPropWidget/models/EditPropAttributeModel", [], function () {
    "use strict";
    return function (e) {
      for (var t in ((this._identifier = null),
      (this.isTransient = !1),
      (this.name = ""),
      (this.label = ""),
      (this.select = ""),
      (this.path = ""),
      (this.value = ""),
      (this.valueDB = ""),
      (this.type = "text"),
      (this.tw_type = null),
      (this.authorizedValues = []),
      (this.authorizedValuesNLS = []),
      (this.authorizedValuesRequired = !0),
      (this.dimension = ""),
      (this.unitName = null),
      (this.dimensionType = null),
      (this.maxlength = -1),
      (this.positionUI = "0"),
      (this.positionIdentityCard = -1),
      (this.sectionIdentityCard = ""),
      (this.masked = !0),
      (this.forbiddenChar = null),
      (this.mandatory = !1),
      (this.modified = !1),
      (this.multiline = !1),
      (this.multivalue = !1),
      (this.multisel = !1),
      (this.readOnly = !0),
      (this.minval = ""),
      (this.maxval = ""),
      (this.minvalincluded = !1),
      (this.maxvalincluded = !1),
      (this.attributeID = ""),
      (this.facet = ""),
      (this.section = ""),
      (this.extension = ""),
      (this.group = "Default"),
      (this.urlIcon = null),
      (this.from = null),
      (this.navType = null),
      (this.relation = null),
      (this.dbValues = { value: "", valueDB: "" }),
      (this.initialsValues = { value: "", valueDB: "" }),
      (this.error = { message: null, type: "error" }),
      (this.validationFunction = function () {}),
      e))
        this[t] = e[t];
      (this.getIdentifier = function () {
        var e = this._identifier;
        return (
          e ||
            ((e = ""),
            (e += this.facet ? this.facet + "-" : ""),
            (e += this.path),
            (this._identifier = e)),
          e
        );
      }),
        (this.set = function (e, t) {
          void 0 !== t &&
            ((this[e] = t), "modified" !== e && (this.modified = !0));
        }),
        (this.saveInitialValues = function () {
          for (var e in this.initialsValues)
            (this.initialsValues[e] = UWA.clone(this[e])),
              (this.dbValues[e] = UWA.clone(this[e]));
        }),
        (this.resetError = function () {
          this.error = { message: null, type: "error" };
        }),
        (this.setError = function (e, t, i) {
          e &&
            (t || (t = "error"),
            (this.error = { message: e, type: t, server: !0 === i }));
        }),
        (this.resetInitialValues = function () {
          for (var e in this.initialsValues)
            this[e] = UWA.clone(this.initialsValues[e]);
        }),
        (this.get = function (e) {
          return this[e];
        }),
        (this.isSameValueSet = function (e) {
          var t = !0;
          if (
            (t = !(
              null === e.value ||
              null === this.value ||
              null === e.valueDB ||
              null === this.valueDB
            ))
          )
            if (this.multisel) {
              for (var i in this.value)
                if (
                  !(t =
                    t &&
                    this._isSameValueSet(
                      e.value[i],
                      this.value[i],
                      e.valueDB[i],
                      this.valueDB[i]
                    ))
                )
                  break;
            } else
              t = this._isSameValueSet(
                e.value,
                this.value,
                e.valueDB,
                this.valueDB
              );
          return !t;
        }),
        (this._isSameValueSet = function (e, t, i, n) {
          var s = !0;
          if ("boolean" === this.type)
            s = i === n || (i + "").toLowerCase() === (n + "").toLowerCase();
          else {
            var a = e.length;
            if ((s = null == t || a === t.length)) {
              if ("relation" !== this.type)
                for (var o = 0; o < a && (s = e[o] === t[o]); o++);
              if (s)
                for (
                  s = (a = i.length) === (n ? n.length : 0), o = 0;
                  o < a && (s = i[o] === n[o]);
                  o++
                );
            }
          }
          return s;
        }),
        (this.getChangedIds = function () {
          var e = {};
          if (!0 === this.multisel)
            for (var t in this.value) {
              var i = this.value[t],
                n = this.valueDB[t],
                s =
                  s &&
                  this._isSameValueSet(
                    this.initialsValues.value[t],
                    i,
                    this.initialsValues.valueDB[t],
                    n
                  );
              s || (e[t] = { value: i, valueDB: n });
            }
          else e.a = { value: this.value, valueDB: this.valueDB };
          return e;
        }),
        (this.hasChanged = function () {
          return this.isSameValueSet(this.initialsValues);
        }),
        (this.applySort = !1);
    };
  }),
  define(
    "DS/EditPropWidget/controllers/EditPropUIController",
    ["UWA/Class"],
    function (e) {
      "use strict";
      return e.extend({
        defaultOptions: {},
        init: function (e, t) {
          (this.editPropModelList = e), this._parent(t);
        },
        setModels: function (e) {
          this.editPropModelList = e;
        },
        getModels: function () {
          return this.editPropModelList;
        },
        getModel: function (e) {
          var t = null;
          return (
            this.editPropModelList.every(function (i) {
              var n = !0;
              return e == i.get("objectId") && ((t = i), (n = !1)), n;
            }),
            t
          );
        },
        getModifiedModels: function () {
          var e = [];
          return (
            this.editPropModelList.forEach(function (t) {
              if (!0 !== t.get("isTransient")) {
                var i = t.getChangedValues();
                Object.keys(i).length > 0 && e.push(t);
              }
            }),
            e
          );
        },
        getLabel: function () {
          var e = "";
          return (
            1 === this.editPropModelList.length &&
              (e = this.editPropModelList[0].get("label")),
            e
          );
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/ajaxRequests/MultipleAsynchronousRequests",
    [],
    function () {
      "use strict";
      return function (e, t) {
        var i = {},
          n = e.length,
          s = t;
        (this.setAnswer = function (t, a) {
          e.indexOf(t) >= 0 && (void 0 === i[t] && n--, (i[t] = a)),
            n <= 0 && null !== s && s.call(this);
        }),
          (this.getAnswer = function (e) {
            return i[e];
          });
      };
    }
  ),
  define("DS/EditPropWidget/comm/EventManager", ["UWA/Class"], function (e) {
    "use strict";
    var t = 0;
    return e.singleton({
      init: function () {
        this._parent.apply(this, arguments),
          (this._events = {}),
          (this._queueStack = []);
      },
      publish: function (e) {
        var i = this,
          n = { index: ++t },
          s = this._events[e];
        Array.prototype.shift.apply(arguments);
        var a = arguments;
        s &&
          s.forEach(function (e) {
            i._queueStack.push({ callback: e, context: n, args: a });
          });
        for (var o = null; (o = this._queueStack.shift()); )
          o.callback.apply(o.context, o.args);
      },
      subscribe: function (e, t) {
        var i = this._events[e];
        i || ((i = []), (this._events[e] = i)), i.push(t);
      },
      remove: function (e, t) {
        var i = this._events[e];
        if (i) {
          for (var n = -1; (n = i.indexOf(t)) >= 0; ) i.splice(n, 1);
          0 == i.length && delete this._events[e];
        }
      },
    });
  }),
  define(
    "DS/EditPropWidget/constants/ConstantsMapping",
    [
      "DS/EditPropWidget/constants/EditPropConstants",
      "DS/WidgetServices/WidgetServices",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i) {
      "use strict";
      var n = {
        "3dspace": {
          name: "3DSpace",
          fedPrefix: "physicalid",
          require: "DS/EditPropWidget/ajaxRequests/3DSpaceAjaxRequests",
          identifire: "pid:",
          path: "/resources",
        },
        "3dlifecycle": {
          name: "3dlifecycle",
          fedPrefix: "physicalid",
          require: "DS/EditPropWidget/ajaxRequests/3dlifecycleAjaxRequests",
          identifire: "pid:",
          path: "/resources",
        },
        "3ddrive": {
          name: "3DDrive",
          fedPrefix: "physicalid",
          isSource: !0,
          require: "DS/EditPropWidget/ajaxRequests/3DDriveAjaxRequest",
          identifire: "driveid:",
          path: "/resources",
        },
        "3dswym": {
          name: "3DSwym",
          fedPrefix: "resourceid",
          modelRequired: !0,
          modelNeeded: !0,
          require:
            "DS/SwymUnifiedSearch/script/customization/editprop-ajax-requests/swym-ajax-requests",
          identifire: "",
        },
        fedsearch: {
          name: "3DSearch",
          require: "DS/EditPropWidget/ajaxRequests/FederatedAjaxRequests",
        },
        www: {
          name: "www",
          require: "DS/FolderEditor/ajaxRequests/wwwAjaxRequests",
          extensionsSupported: !1,
        },
      };
      return {
        getSupportedServices: function () {
          return n;
        },
        getSupportedServicesArray: function () {
          var e = this.getSupportedServices(),
            t = [],
            i = [];
          for (var n in e) {
            var s = e[n];
            !0 !== s.modelRequired &&
              (s.isSource ? t.push(s.id ? s.id : n) : i.push(s.name));
          }
          return { sources: t, services: i };
        },
        getServiceBySource: function (e) {
          return n[e];
        },
        getServiceForModel: function (e) {
          var t = null,
            i = null;
          if (e) {
            if (((i = e.get("_sourceId")), !(t = n[i]))) {
              var s = e.get("sourceName");
              s && ((i = s.toLowerCase()), (t = n[i])),
                t ||
                  ((s = e.get("source")) &&
                    ((i = s.toLowerCase()), (t = n[i]))),
                e.set("_sourceId", i);
            }
            null != t &&
              ((e.identifire = t.identifire ? t.identifire : ""),
              (e.path = t.path ? t.path : ""));
          }
          return t;
        },
        getMapping: function (t) {
          var n = {};
          return (
            (n[e.FACET_ACTIVITY] = {
              require: "DS/EditPropWidget/facets/History/History",
              facet: {
                name: e.FACET_ACTIVITY,
                text: i.get("Activity"),
                icon: "clock",
              },
            }),
            (n[e.FACET_GENERALHISTORY] = {
              require: "DS/EditPropWidget/facets/GeneralHistory/History",
              services: ["3DSpace"],
              facet: {
                name: e.FACET_GENERALHISTORY,
                text: i.get("Activity"),
                icon: "clock",
              },
              options: t,
            }),
            (n[e.FACET_VIEWS] = {
              require: "DS/EditPropWidget/facets/3DPlay/3DPlayFacet",
              webInWin: !1,
              facet: { name: e.FACET_VIEWS, text: i.get("Views"), icon: "eye" },
              options: t,
            }),
            (n[e.FACET_PROPERTIES] = {
              alwaysCalled: !0,
              supportMerge: !0,
              require: "DS/EditPropWidget/facets/Properties/EditPropController",
              options: t,
              actions: [e.ACTION_SWITCH, e.ACTION_RESET],
              facet: {
                name: e.FACET_PROPERTIES,
                text: i.get("Properties"),
                icon: "attributes",
              },
              supportExtension: !0,
            }),
            (n[e.FACET_iPROPERTIES] = {
              alwaysVisible: !1,
              alwaysCalled: !0,
              require: "DS/EditPropWidget/facets/Properties/iPropertiesFacet",
              options: t,
              actions: [e.ACTION_RESET],
              facet: {
                name: e.FACET_iPROPERTIES,
                text: i.get("iProperties"),
                icon: "attributes",
              },
            }),
            (n[e.FACET_RICHCONTENT] = {
              require:
                "DS/ReqProperties/facets/RichAttribute/RichContentController",
              facet: {
                name: e.FACET_RICHCONTENT,
                text: i.get("RichContentTabName"),
                icon: "attributes",
              },
            }),
            (n[e.FACET_SWYMCOMMENTS] = {
              require: "DS/EditPropWidget/facets/SwymComments/SwymComments",
              services: ["comment"],
              content: null,
              facet: {
                name: e.FACET_SWYMCOMMENTS,
                text: i.get("Social"),
                icon: "comment",
              },
            }),
            (n[e.FACET_EBOM] = {
              supportMerge: !0,
              require: "DS/EditPropWidget/facets/EBOMPart/EBOMPart",
              options: t,
              actions: [e.ACTION_SWITCH, e.ACTION_RESET],
              facet: {
                name: e.FACET_EBOM,
                text: i.get("EBOMPart"),
                icon: "table",
              },
            }),
            (n[e.FACET_EFFECTIVITY] = {
              alwaysVisible: !1,
              roles: ["CSV", "InternalDS"],
              require: "DS/CfgWebAppEffectivityUX/facets/EffectivityFacet",
              options: t,
              facet: {
                name: e.FACET_EFFECTIVITY,
                text: i.Effectivity,
                icon: "survey",
              },
            }),
            (n[e.FACET_SPECIFICATIONS] = {
              alwaysVisible: !1,
              require:
                "DS/CfgWebAppSpecificationsUX/facets/CfgSpecificationsFacet",
              options: t,
              facet: {
                name: e.FACET_SPECIFICATIONS,
                text: i.Specifications,
                icon: "properties",
              },
            }),
            (n[e.FACET_Variability] = {
              alwaysVisible: !1,
              require:
                "DS/CfgWebAppXRevisionCommands/facets/CfgXRevisionVariabilityFacet",
              options: t,
              facet: {
                name: e.FACET_Variability,
                text: i.Variability,
                icon: "variants-and-options",
              },
            }),
            (n[e.FACET_CHANGE] = {
              alwaysVisible: !1,
              options: t,
              require: "DS/ENOChgActionProp/facets/ChangePropertiesFacet",
              facet: { name: e.FACET_CHANGE, text: i.Change, icon: "change" },
            }),
            (n[e.FACET_iCHANGE] = {
              alwaysVisible: !1,
              options: t,
              require: "DS/ENOChgActionProp/facets/iChangePropertiesFacet",
              facet: { name: e.FACET_iCHANGE, text: i.iChange, icon: "change" },
            }),
            (n[e.FACET_ALTERNATE] = {
              alwaysVisible: !1,
              options: t,
              require:
                "DS/ENOXAlternateSubstitute/components/properties/js/AlternatePropertiesFacet",
              facet: {
                name: e.FACET_ALTERNATE,
                text: i.get("Alternate"),
                icon: "exchange",
              },
            }),
            (n[e.FACET_IPPROTECTION] = {
              alwaysVisible: !1,
              supportMerge: !1,
              roles: ["IPA", "IPS", "InternalDS"],
              options: t,
              require: "DS/ENOIPProtection/IPProtection/IPProtectionController",
              facet: {
                name: e.FACET_IPPROTECTION,
                text: i.IPProtection,
                icon: "vault",
              },
            }),
            (n[e.FACET_PROXY] = {
              require: "DS/OSLCConsumer/facets/ProxyFacet",
              calledIfVisible: !0,
              facet: {
                name: e.FACET_PROXY,
                text: i.get("ProxyFacet"),
                icon: "link",
              },
            }),
            (n[e.FACET_INSTANCE] = {
              supportMerge: !0,
              require: "DS/EditPropWidget/facets/Instance/InstanceFacet",
              options: t,
              actions: [e.ACTION_SWITCH, e.ACTION_RESET],
              facet: {
                name: e.FACET_INSTANCE,
                text: i.get("EP_instance"),
                icon: "attributes",
              },
              supportExtension: !0,
            }),
            (n[e.FACET_iINSTANCE] = {
              require: "DS/EditPropWidget/facets/Instance/IndexInstanceFacet",
              options: t,
              actions: [e.ACTION_RESET],
              facet: {
                name: e.FACET_iINSTANCE,
                text: i.get("EPIndex_instance"),
                icon: "attributes",
              },
            }),
            (n[e.FACET_SWYM_CONTENT] = {
              alwaysVisible: !1,
              alwaysCalled: !1,
              require:
                "DS/SwymUnifiedSearch/script/customization/facets/edit-properties-facets/swym-content-facet",
              actions: [e.ACTION_SWYM_VIEW_CONTENT_IN_3DSWYM],
              facet: {
                name: e.FACET_SWYM_CONTENT,
                text: i.get("View"),
                icon: "eye",
              },
            }),
            (n[e.FACET_SWYM_SURVEY_PROPERTIES] = {
              alwaysVisible: !1,
              alwaysCalled: !1,
              require:
                "DS/SwymUnifiedSearch/script/customization/facets/edit-properties-facets/swym-survey-properties-facet",
              actions: [e.ACTION_SWYM_VIEW_SURVEY_IN_3DSWYM],
              facet: UWA.merge(
                { name: e.FACET_SWYM_SURVEY_PROPERTIES },
                n[e.FACET_PROPERTIES].facet
              ),
            }),
            (n[e.FACET_SWYM_COMMUNITY_PROPERTIES] = {
              alwaysCalled: !1,
              alwaysVisible: !1,
              require:
                "DS/SwymUnifiedSearch/script/customization/facets/edit-properties-facets/swym-community-properties-facet",
              actions: [e.ACTION_SWYM_VIEW_COMMUNITY_IN_3DSWYM],
              facet: UWA.merge(
                { name: e.FACET_SWYM_COMMUNITY_PROPERTIES },
                n[e.FACET_PROPERTIES].facet
              ),
            }),
            (n[e.FACET_SWYM_USER_PROPERTIES] = {
              alwaysCalled: !1,
              alwaysVisible: !1,
              require:
                "DS/SwymUnifiedSearch/script/customization/facets/edit-properties-facets/swym-user-properties-facet",
              actions: [e.ACTION_SWYM_VIEW_USER_PROFILE],
              facet: UWA.merge(
                { name: e.FACET_SWYM_USER_PROPERTIES },
                n[e.FACET_PROPERTIES].facet
              ),
            }),
            (n[e.FACET_ADMIN] = {
              require: "DS/PropertiesAdmin/PropertiesAdmin",
              facet: {
                name: e.FACET_ADMIN,
                text: i.get("AdminFacet"),
                icon: "cog",
              },
            }),
            (n[e.FACET_RELATIONS] = {
              require: "DS/ENORIPE_Relations/views/RelationsTab",
              options: t,
              facet: {
                name: e.FACET_RELATIONS,
                text: i.get("relations_tab"),
                icon: "object-related",
              },
            }),
            (n[e.FACET_DERIVEDOUTPUT] = {
              roles: ["CSV", "InternalDS"],
              supportMerge: !1,
              options: t,
              require: "DS/ENODODisplay/commands/facets/DerivedOutputFacet",
              facet: {
                name: e.FACET_DERIVEDOUTPUT,
                text: i.get("DerivedOutput"),
                icon: "derived-output",
              },
              actions: [
                {
                  name: "generateDeriveOutput",
                  text: i.get("generateDeriveOutputNLS"),
                  icon: "derived-output",
                },
                {
                  name: "refreshDOView",
                  text: i.get("refreshDOViewNLS"),
                  icon: "refresh",
                },
                {
                  name: "bulkDownloadDO",
                  text: i.get("bulkDownloadDONLS"),
                  icon: "folder-zipped-download",
                },
              ],
            }),
            (n[e.FACET_REVISIONS] = {
              blockFromSearch: !0,
              options: t,
              require: "DS/ENOXVersionExplorerUtils/RevisionsTabFacet",
              facet: {
                name: e.FACET_REVISIONS,
                text: i.get("revisions_tab"),
                icon: "flow-branch-list",
              },
            }),
            (n[e.FACET_DOCUMENTS] = {
              require: "DS/ReqProperties/facets/ReqDoc/ReqDocFacet",
              facet: {
                name: e.FACET_DOCUMENTS,
                text: i.get("documents_tab"),
                icon: "attach",
              },
            }),
            (n[e.FACET_FILES] = {
              alwaysVisible: !1,
              supportMerge: !1,
              blockFromSearch: !0,
              options: t,
              require: "DS/DocumentFiles/FilesFacet",
              facet: { name: e.FACET_FILES, text: i.Files, icon: "docs" },
            }),
            (n[e.FACET_SHARING] = {
              blockFromSearch: !0,
              options: t,
              require: "DS/ENOSharingWebUI/ENOSharingWebUI",
              actions: [],
              facet: {
                name: e.FACET_SHARING,
                text: i.get("sharing_tab"),
                icon: "forward",
              },
            }),
            (n[e.FACET_PHYSICSSIMULATION] = {
              options: t,
              visible: !0,
              alwaysVisible: !0,
              require: "DS/SPYUI/facets/PhysSimulationFacet",
              facet: {
                name: e.FACET_PHYSICSSIMULATION,
                text: i.get("physicssimulation_tab"),
                icon: "compass-simulation",
              },
            }),
            (n[e.FACET_POSTERRORANALYSIS] = {
              visible: !0,
              blockFromSearch: !0,
              options: t,
              require:
                "DS/ENOLifecyclePostErrorAnalysis/ENOLifecyclePostErrorAnalysisUI",
              facet: {
                name: e.FACET_POSTERRORANALYSIS,
                text: i.get("postErrorAnalysis_tab"),
                icon: "parameters",
              },
            }),
            (n[e.FACET_PC_CONTENT] = {
              require: "DS/PCOverview/js/PCOverviewFacet",
              supportMerge: !0,
              facet: {
                name: e.FACET_PC_CONTENT,
                text: i.get("pc_content_tab"),
                icon: "properties",
              },
            }),
            (n[e.FACET_FL_SPECIFICATION] = {
              options: t,
              supportMerge: !1,
              require:
                "DS/CATFLWidgetProperties/facets/CATFLSpecificationDocumentFacet",
              facet: {
                name: e.FACET_FL_SPECIFICATION,
                icon: "doc-log",
                text: i.get("catfl_specification_document_tab"),
              },
            }),
            n
          );
        },
        getDefaultFacets: function (i, n) {
          var s = this.getMapping(),
            a = [
              e.FACET_PROPERTIES,
              e.FACET_FILES,
              e.FACET_PC_CONTENT,
              e.FACET_iPROPERTIES,
              e.FACET_INSTANCE,
              e.FACET_iINSTANCE,
              e.FACET_SHARING,
              e.FACET_VIEWS,
              e.FACET_EBOM,
              e.FACET_ACTIVITY,
              e.FACET_GENERALHISTORY,
              e.FACET_EFFECTIVITY,
              e.FACET_SPECIFICATIONS,
              e.FACET_Variability,
              e.FACET_CHANGE,
              e.FACET_iCHANGE,
              e.FACET_ALTERNATE,
              e.FACET_IPPROTECTION,
              e.FACET_PROXY,
              e.FACET_SWYMCOMMENTS,
              e.FACET_SWYM_CONTENT,
              e.FACET_SWYM_COMMUNITY_PROPERTIES,
              e.FACET_SWYM_USER_PROPERTIES,
              e.FACET_SWYM_SURVEY_PROPERTIES,
              e.FACET_RELATIONS,
              e.FACET_DERIVEDOUTPUT,
              e.FACET_REVISIONS,
              e.FACET_DOCUMENTS,
              e.FACET_RICHCONTENT,
              e.FACET_FL_SPECIFICATION,
              e.FACET_ADMIN,
              e.FACET_POSTERRORANALYSIS,
            ],
            o = a,
            r = t.isWebInWinEnvironment();
          return (
            (!0 !== r && !0 !== i && !0 !== n) ||
              ((o = []),
              a.forEach(function (e) {
                var t = !0,
                  a = s[e];
                !0 === i && (t = !a || !0 === a.supportMerge),
                  t && !0 === r && (t = !1 !== a.webInWin),
                  t && (!0 === n ? o.push(a) : o.push(e));
              })),
            o
          );
        },
      };
    }
  ),
  define(
    "DS/EditPropWidget/models/EditPropModel",
    [
      "UWA/Class/Model",
      "UWA/Class/Debug",
      "DS/WidgetServices/WidgetServices",
      "DS/EditPropWidget/models/EditPropAttributeModel",
      "DS/EditPropWidget/constants/EditPropConstants",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s) {
      "use strict";
      return e.extend(t, {
        METATYPE_BUS: s.METATYPE_BUS,
        METATYPE_REL: s.METATYPE_REL,
        METATYPE_PATH: s.METATYPE_PATH,
        changedValues: {},
        _locked: !1,
        _lockedAttributes: [],
        _initProperties: {},
        defaults: {
          metatype: s.METATYPE_UNKNOWN,
          source: "3DSpace",
          sourceName: "",
          tenant: i.getTenantID(),
          objectId: "",
          label: "",
          labelAttribute: "",
          type_icon_url: "",
          previewUrl: "",
          isTransient: !1,
          _changeId: "",
          instrefid: "",
          indexModeParameter: "",
          i3dxId: "",
        },
        isMergedModel: function () {
          return !1;
        },
        getOptions: function () {
          var e = {};
          for (var t in this.defaults) e[t] = this.get(t);
          return e;
        },
        lockModel: function (e) {
          var t = this;
          (this._locked = !0 === e),
            this._locked ||
              (this._lockedAttributes.forEach(function (e) {
                var i = t.get(e);
                null != i && ((i = new n(i)), t.set(e, i));
              }),
              (this._lockedAttributes = []));
        },
        isLocked: function () {
          return !0 === this._locked;
        },
        setFloatingAttribute: function (e, t, i) {
          this.isLocked()
            ? ((this._attributes[e] = t),
              this._lockedAttributes.indexOf(e) < 0 &&
                this._lockedAttributes.push(e))
            : (!0 !== i && (t = new n(t)), this.set(e, t));
        },
        getComputedTitle: function () {
          return this.get("label");
        },
        setup: function () {
          for (var e in ((this._lockedAttributes = []),
          (this._locked = !1),
          (this.changedValues = {}),
          (this._initProperties = {}),
          this.defaults))
            this._initProperties[e] = this.get(e);
        },
        getChangeId: function () {
          return this.get("_changeId");
        },
        getTenant: function () {
          return this.get("tenant");
        },
        saveInitialValues: function (e, t) {
          var i = this,
            n = this.keys(),
            s = [];
          n.forEach(function (t) {
            var n = i.get(t);
            null !== n &&
              "object" == typeof n &&
              "function" == typeof n.saveInitialValues &&
              (null == e || e.indexOf(t) >= 0) &&
              (n.saveInitialValues(), s.push(t));
          }),
            !1 !== t && this._dispatchChangedAttributes(i, s, !1),
            this._clearChangedValue(e),
            this.modelSaved();
        },
        isTransientModel: function () {
          return this.get("isTransient");
        },
        getAttributeFromField: function (e, t, i) {
          var n = this;
          i = i || 0;
          var s = [];
          return (
            this.keys().every(function (a) {
              var o = n.get(a);
              return (
                null !== o &&
                  "object" == typeof o &&
                  "function" == typeof o.get &&
                  o.get(e) === t &&
                  s.push(o),
                0 === i || s.length < i
              );
            }),
            1 === i ? (s.length > 0 ? s[0] : null) : s
          );
        },
        resetInitialValues: function () {
          var e = this;
          this.keys().forEach(function (t) {
            var i = e.get(t);
            i &&
              "object" == typeof i &&
              "function" == typeof i.resetInitialValues &&
              (i.resetInitialValues(),
              e._dispatchChangedAttributes(e, [t], !0));
          }),
            this._clearChangedValue();
        },
        resetAllErrors: function (e) {
          var t = this;
          this.keys().forEach(function (i) {
            var n = t.get(i);
            n &&
              "object" == typeof n &&
              "function" == typeof n.resetError &&
              (n.resetError(),
              !1 !== e && t._dispatchChangedAttributes(t, [i], !0));
          });
        },
        clearChangedValue: function (e) {
          this._clearChangedValue(e);
        },
        _clearChangedValue: function (e) {
          var t = this;
          e
            ? e.forEach(function (e) {
                t._removeChangedValue(e);
              })
            : (this.changedValues = {});
        },
        removeChangedValue: function (e) {
          this._removeChangedValue(e);
        },
        _removeChangedValue: function (e) {
          this.changedValues[e] && delete this.changedValues[e];
        },
        attributeHasChanged: function (e) {
          return !!this.changedValues[e];
        },
        removeEvent: function (e, t) {
          this.removeEvent2("onChange:" + e, t);
        },
        removeEvent2: function (e, t) {
          var i = this._dispatchers[e];
          if (i)
            for (var n = i.bindings.length - 1; n >= 0; n--) {
              i.bindings[n].listener === t && i.bindings.splice(n, 1);
            }
        },
        getChangedValues: function () {
          return this.changedValues;
        },
        setChangedValues: function (e) {
          "object" == typeof e && (this.changedValues = e);
        },
        hasSameAttributesThan: function (e) {
          var t = Object.keys(this._attributes),
            i = Object.keys(e._attributes),
            n = !1;
          return (
            t.length === i.length &&
              ((n = !0),
              t.every(function (e) {
                return i.indexOf(e) < 0 && (n = !1), n;
              })),
            n
          );
        },
        onChange: function (e) {
          var t = this._getChangedAttributes(e);
          t.length > 0 && this._dispatchChangedAttributes(e, t, !1);
        },
        _getChangedAttributes: function (e) {
          var t = e.changedAttributes(),
            i = [];
          for (var s in t) {
            var a = t[s],
              o = !1;
            a instanceof n &&
              ((o = a.hasChanged())
                ? (e.changedValues[s] = a)
                : ((o = void 0 !== e.changedValues[s]) &&
                    delete e.changedValues[s],
                  (o = o || !this._previousAttributes[s])),
              o && i.push(s));
          }
          return i;
        },
        _dispatchChangedAttributes: function (e, t, i) {
          var n = this,
            s = [];
          t.forEach(function (t) {
            i && n.dispatchEvent("onChange:" + t), s.push(e.get(t));
          }),
            this.dispatchEvent("onChangeAttributes", [e, s]);
        },
        modelSaved: function () {
          this.dispatchEvent("onSave");
        },
        getAllAttributes: function () {
          return this._attributes;
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/models/CommonAbstract",
    ["DS/EditPropWidget/comm/EventManager", "UWA/Controls/Abstract"],
    function (e, t) {
      var i = 0;
      return t.extend({
        _callbacks: {},
        init: function (e) {
          i++,
            e && !e.eventThread && (e.eventThread = "CT" + i),
            this._parent.apply(this, arguments),
            (this._callbacks = {});
        },
        publishEvt: function (t) {
          var i = this._getEventThread(t);
          (arguments[0] = i), e.publish.apply(e, arguments);
        },
        subscribeEvt: function (t, i, n) {
          var s = this._getEventThread(t);
          (arguments[0] = s),
            e.subscribe.apply(e, arguments),
            (this._callbacks[s] = i);
        },
        destroy: function () {
          for (var t in this._callbacks) e.remove(t, this._callbacks[t]);
          this._parent.apply(this, arguments);
        },
        _getEventThread: function (e) {
          return (
            (this.options.eventThread ? this.options.eventThread + "." : "") + e
          );
        },
        _callMethodOnEach: function (e, t) {
          var i = e,
            n = t;
          for (var s in (Array.prototype.shift.apply(arguments),
          Array.prototype.shift.apply(arguments),
          i))
            i[s][n].apply(this, arguments);
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/ajaxRequests/ServicesAjaxRequests",
    [
      "DS/WidgetServices/WidgetServices",
      "DS/WidgetServices/securityContextServices/SecurityContextServices",
      "DS/EditPropWidget/ajaxRequests/MultipleAsynchronousRequests",
      "DS/EditPropBaseUX/scripts/EditPropUtility",
      "DS/WAFData/WAFData",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s, a) {
      "use strict";
      return {
        createRequest: function (e, t, i, n) {
          var s = this;
          e && !1 === e.withChange
            ? s._createRequestWithAuthoringContext(e, t, i, n)
            : require(["DS/EditPropWidget/changeAction/passThruJS"], function (
                a
              ) {
                a.onReady(function (a) {
                  s._createRequestWithAuthoringContext(e, t, i, n, a);
                });
              });
        },
        _createRequestWithAuthoringContext: function (n, a, o, r, l) {
          var c = this;
          if (n.handleRequest) {
            if (
              n.handleRequest.handle &&
              "function" == typeof n.handleRequest.handle.isCancelled &&
              n.handleRequest.handle.isCancelled()
            )
              return;
          } else n.handleRequest = {};
          var d = ["A"];
          e.isChangeAvailable() && l && d.push("B"),
            !0 === n.withSC && d.push("C");
          var u = new i(d, function () {
            var t = this.getAnswer("A"),
              i = this.getAnswer("B"),
              l = this.getAnswer("C");
            l = l || n.securityContext;
            var d = t + o,
              u = {
                Accept: "application/json",
                "content-type": "application/json",
              };
            if (((u["Accept-Language"] = e.getLanguage()), i)) {
              var h = "silent" === n.withChange && "object" == typeof r;
              for (var p in i)
                h ? void 0 === r[p] && (r[p] = i[p]) : (u[p] = i[p]);
            }
            l && (u.SecurityContext = encodeURIComponent(l));
            var m = n.contentType ? n.contentType : "json";
            if (n.headers) for (var p in n.headers) u[p] = n.headers[p];
            var f = {
              type: m,
              method: a,
              timeout: 36e5,
              proxy: "passport",
              headers: u,
              cache: -1,
              onComplete: function (e) {
                n.onCompleteWS(e, n);
              },
              onFailure: function (e, t, i) {
                c._onFailure(e, t, i, n);
              },
              onTimeout: function (e) {
                c._onTimeout(n);
              },
            };
            e.getWidgetValue("isStandalone") || (f.authentication = "passport"),
              r && (f.data = "json" !== m ? r : UWA.Json.encode(r)),
              (n.handleRequest.handle = s.handleRequest(
                c._formatUrl(n.model, d),
                f
              ));
          });
          (n.handleRequest.handle = e.getSourceUrlAsync({
            platformId: this.getTenantFromModel(n.model),
            source: n.source ? n.source : this.getSourceFromModel(n.model),
            onComplete: function (e) {
              u.setAnswer("A", e);
            },
          })),
            !0 === n.withSC &&
              t
                .getInstance({ tenant: this.getTenantFromModel(n.model) })
                .getValidSecurityContext({
                  onSuccess: function (e) {
                    u.setAnswer("C", e.SecurityContext);
                  },
                  onFailure: function (e) {
                    console.log(" -- >> Can't get the security context"),
                      u.setAnswer("C", null);
                  },
                }),
            e.isChangeAvailable() &&
              l &&
              l.get2(function (e) {
                var t = null;
                l.changeIsSetChecker(e) && (t = e.AuthoringContextHeader),
                  e.errorMessage && console.log("Change : " + e.errorMessage),
                  u.setAnswer("B", t);
              });
        },
        getTenantFromModel: function (t) {
          var i = t ? t.get("tenant") : null;
          return (i = i || e.getTenantID());
        },
        getSourceFromModel: function (e) {
          return e ? e.get("source") : null;
        },
        _formatUrl: function (e, t) {
          var i = this.getTenantFromModel(e);
          return this._formatUrl2(i, t);
        },
        _formatUrl2: function (e, t) {
          if (e) {
            var i = t.indexOf("?");
            i < 0 && (t += "?"),
              i > 0 && i < t.length - 1 && (t += "&"),
              (t = t += "tenant=" + e);
          }
          return t;
        },
        getExtraValues: function (e, t) {
          var i = {},
            n = [];
          return (
            e.forEach(function (e) {
              i[e] = !0;
            }),
            t.forEach(function (e) {
              i[e] || n.push(e);
            }),
            n
          );
        },
        _createJSONModifiedAttribute: function (e) {
          var t = [],
            i = ["physicalid", "modified"],
            n = {},
            s = {},
            a = e.getChangedValues(),
            o = {},
            r = {};
          for (var l in a) {
            var c = e.get(l),
              d = c.facet;
            if ("object" == typeof c && !c.isTransient) {
              var u = c.attributeID;
              if (null != u && u.length > 0) this._addPLMParameterQuery(c, t);
              else {
                var h = c.getChangedIds();
                for (var p in h) {
                  var m = h[p];
                  "a" === p && (p = e.get("objectId"));
                  var f = o[p];
                  f || ((f = []), (o[p] = f)),
                    (P = r[p]) || ((P = [].concat(i)), (r[p] = P)),
                    P.push(c.select);
                  var g = m.valueDB;
                  !1 !== g && null == g && (g = m.value);
                  var v = null;
                  Array.isArray(g) ||
                    "string" == typeof g ||
                    (v = g.toString()),
                    null === v && (v = g);
                  var _ = null;
                  if (null !== c.from) {
                    var E = null,
                      C = null;
                    if (
                      (c.multivalue
                        ? ((E = this.getExtraValues(
                            c.valueDB,
                            c.initialsValues.valueDB
                          )),
                          (C = this.getExtraValues(
                            c.initialsValues.valueDB,
                            c.valueDB
                          )))
                        : c.valueDB !== c.initialsValues.valueDB &&
                          ((E = c.initialsValues.valueDB
                            ? [c.initialsValues.valueDB]
                            : null),
                          (C = c.valueDB ? [c.valueDB] : null)),
                      C &&
                        C.length > 0 &&
                        (_ = {
                          op: "connect",
                          path: c.path,
                          relation: c.relation,
                          name: c.name,
                          from: c.from,
                          value: C,
                        }),
                      E && E.length > 0)
                    ) {
                      var S = {
                        op: "disconnect",
                        path: c.path,
                        relation: c.relation,
                        from: c.from,
                        name: c.name,
                        value: E,
                      };
                      _ ? f.push(S) : (_ = S);
                    }
                  } else _ = { op: "replace", path: c.path, value: v };
                  d && (_.facet = d);
                  var y = c.get("unitName");
                  y &&
                    ((_.unitName = y),
                    (_.dimensionType = c.get("dimensionType"))),
                    _ && f.push(_);
                }
              }
            }
          }
          var A = e.get("metatype"),
            I = "model/";
          I +=
            "businessobject" == A || "bus" == A
              ? "bus/"
              : "EBOM" == A
              ? "EBOM/"
              : "rel/";
          var b = [];
          for (var p in o) {
            var P;
            s = { path: I + p, method: "PATCH", body: o[p] };
            (P = r[p]) && ((n = { select: P }), (s.queryParams = n)), b.push(s);
          }
          return { requests: b, plmParametersRequests: t };
        },
        _addPLMParameterQuery: function (e, t) {
          var i = e.valueDB;
          !1 === i || i || (i = e.value);
          var n = [],
            s = [],
            a = {},
            o = null;
          if (
            (Array.isArray(i) || "string" == typeof i || (o = i.toString()),
            null === o && (o = i),
            n.push(e.name),
            s.push({ op: "replace", path: e.path, value: o }),
            null != n)
          ) {
            var r = { select: n };
            a.queryParams = r;
          }
          a.body = s;
          var l = "plmparameter/" + e.attributeID;
          a.path = "model/" + l;
          (a.method = "PATCH"), t.push(a);
        },
        _concatRequestListComposite: function (e, t) {
          t.forEach(function (t, i) {
            var n = !1;
            e.every(function (i, s) {
              return (
                t.path != i.path ||
                t.method != i.method ||
                ((e[s].body = e[s].body.concat(t.body)),
                (e[s].queryParams.select = e[s].queryParams.select.concat(
                  t.queryParams.select
                )),
                (n = !0),
                !1)
              );
            }),
              !1 === n && e.push(t);
          });
        },
        securityContextDispatcher: function (e, i) {
          var n = this;
          if (e.securityContext) i.call(this, e);
          else {
            var s = null;
            (s = t.getInstance({ tenant: e.tenant }).getValidSecurityContext({
              context: e.context,
              onSuccess: function (t) {
                (e.securityContext = t.SecurityContext), i.call(n, e);
              },
              onFailure: function (t) {
                n._onFailure(null, { error: t }, null, e);
              },
            })),
              e.handleRequest && s && (e.handleRequest.handle = s);
          }
        },
        _onCompleteAttributes: function (e, t) {
          t.onComplete.call(null, e);
        },
        _onCompleteCommit: function (e, t) {
          t.onComplete.call(null, e);
        },
        _onFailure: function (e, t, i, s) {
          var o = {};
          if (
            null != t &&
            t.hasOwnProperty("error") &&
            "EDITPROP_ERROR_0001" === t.error
          )
            (o = {
              message: a.get("UpdateFailedDueToFrozenModelConnected"),
              code: "ERROR",
            }),
              this._raiseError(o, s);
          else if (
            null != t &&
            t.hasOwnProperty("error") &&
            "ErrorCode:1500572" === t.error
          ) {
            var r = {
              level: "error",
              title: UWA.i18n("InvalidCSCreationfailed"),
              subtitle: UWA.i18n("InvalidCSCreationFailedSubtitle"),
              message: UWA.i18n("NoAccessToCS"),
            };
            n.showMessageOnUI(r.level, r.title, r.subtitle, r.message, !1);
          } else if (
            null != t &&
            t.hasOwnProperty("error") &&
            "INF102" === t.error.code
          ) {
            r = {
              level: "error",
              title: UWA.i18n("NoaccesstoApptitle"),
              subtitle: UWA.i18n("NoaccesstoAppDesc"),
              message: "",
            };
            n.showMessageOnUI(r.level, r.title, r.subtitle, r.message, !1);
          } else
            (o =
              null != t && t.hasOwnProperty("error")
                ? t.error
                : { message: a.get("Error Server"), code: "ERROR" }),
              this._raiseError(o, s);
        },
        _raiseError: function (e, t) {
          "object" != typeof e && (e = { message: e }),
            t.onComplete.call(null, { success: !1, error: e });
        },
        _onTimeout: function (e) {
          var t = { message: a.get("ErrorTimeOut"), code: "TO" };
          this._raiseError(t, e);
        },
      };
    }
  ),
  define(
    "DS/EditPropWidget/models/EditPropModelMerge",
    [
      "DS/EditPropWidget/models/EditPropModel",
      "DS/EditPropWidget/models/EditPropAttributeModel",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i) {
      return e.extend({
        _innerModels: {},
        setup: function () {
          this._parent(), (this._innerModels = {});
        },
        getMergingModels: function () {
          return this._innerModels;
        },
        getMergedModelIds: function () {
          var e = this.getMergingModels();
          return Object.keys(e);
        },
        setMergingModels: function (e) {
          var t = this;
          if (this._innerModels)
            for (var i in this._innerModels) {
              (n = this._innerModels[i])._callbackMerge &&
                n.removeEvent(
                  "onChangeAttributes",
                  n._callbackMerge[t.get("objectId")]
                );
            }
          for (var i in ((this._innerModels = e), this._innerModels)) {
            var n;
            ((n = this._innerModels[i])._callbackMerge = n._callbackMerge
              ? n._callbackMerge
              : {}),
              (n._callbackMerge[t.get("objectId")] = function (e, i) {
                var n = e.get("objectId");
                i.forEach(function (e) {
                  var i = e.getIdentifier(),
                    s = t.get(i);
                  s &&
                    ((s.get("value")[n] = e.get("value")),
                    (s.get("valueDB")[n] = e.get("valueDB")),
                    t.setFloatingAttribute(i, s));
                });
              }),
              n.addEvent(
                "onChangeAttributes",
                n._callbackMerge[t.get("objectId")]
              );
          }
        },
        isMergedModel: function () {
          return !0;
        },
        set: function (e, i) {
          i instanceof t && i.set("multisel", !0), this._parent(e, i);
        },
        getComputedTitle: function () {
          var e = this.getMergedModelIds().length;
          return (
            (newLabel = i.replace(i.get("{0} objects selected"), { 0: e })),
            newLabel
          );
        },
        resetAllErrors: function () {
          var e = this;
          this.lockModel(!0);
          var t = this.getMergingModels();
          for (var i in t) t[i].resetAllErrors();
          setTimeout(function () {
            e.lockModel(!1);
          }),
            e._parent(!1);
        },
        clearChangedValue: function (e) {
          var t = this;
          this.lockModel(!0);
          var i = this.getMergingModels();
          for (var n in i) i[n].clearChangedValue(e);
          setTimeout(function () {
            t.lockModel(!1);
          });
        },
        resetInitialValues: function () {
          this._parent.apply(this, arguments);
          var e = this.getMergingModels();
          for (var t in e) {
            var i = e[t];
            i.resetInitialValues.apply(i, arguments);
          }
        },
        getChangedValues: function () {
          var e = {},
            t = this.getMergingModels();
          for (var i in t) {
            var n = t[i];
            if (n) {
              var s = n.getChangedValues();
              if (s)
                for (var a in s) {
                  var o = this.get(a);
                  o && (e[a] = o);
                }
            }
          }
          return e;
        },
        _getChangedAttributes: function (e) {
          var t = e.changedAttributes(),
            i = e.getChangedValues(),
            n = [];
          for (var s in t) null !== i[s] && n.push(s);
          return n;
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/comm/DistantAjax",
    [
      "UWA/Class",
      "DS/EditPropWidget/comm/CommManager",
      "DS/WidgetServices/WidgetServices",
      "DS/EditPropWidget/constants/EditPropConstants",
    ],
    function (e, t, i, n) {
      "use strict";
      return (
        (window.top.perfoIncrem = window.top.perfoIncrem
          ? window.top.perfoIncrem
          : 0),
        e.singleton({
          _isAsynchronous: !1,
          _valuesAsync: {},
          init: function () {
            (this._isAsynchronous =
              !0 === widget.getValue("asynchronousRequest") ||
              "true" === widget.getValue("asynchronousRequest")),
              widget.setValue("asynchronousRequest", "false"),
              (this._valuesAsync = {});
          },
          _getCommManager: function (e) {
            e.call(this, t);
          },
          getAsyncRead: function (e) {
            this._getAsync("read", e);
          },
          getAsyncFacetCheck: function (e) {
            this._getAsync("facetCheck", e);
          },
          _getAsync: function (e, t) {
            this._isAsynchronous
              ? this._getCommManager(function (i) {
                  var n = new i(e);
                  n.createListenEvent(function (e) {
                    t(e), n.destroy();
                  }),
                    setTimeout(function () {
                      n.getCurrentListenStatus() || t(null);
                    }, 100);
                })
              : t(null);
          },
          _distantCall: function (e, t, i, n) {
            this._getCommManager(function (s) {
              var a = new s(e),
                o = !1,
                r = ++window.top.perfoIncrem;
              window.top.performance.mark("start_getAsync:" + r),
                this._getAsync(e, function (e) {
                  e && window.top.performance.mark("end_getAsync:" + r),
                    !1 === o &&
                      e &&
                      e.discriminant === t &&
                      ((o = !0), i && i(e ? e.value : e));
                }),
                setTimeout(function () {
                  !1 === o &&
                    (a.createRunningEvent(),
                    n.call(this, function (e) {
                      !1 === o &&
                        ((o = !0),
                        a.createReadyEvent({ value: e, discriminant: t }),
                        i && i(e));
                    }));
                }),
                setTimeout(function () {
                  a && a.destroy();
                }, 12e4);
            });
          },
          getRoles: function (e, t) {
            var n = this;
            this._distantCall("role", e, t, function (t) {
              i.getGrantedRoles(function (e) {
                t.call(n, e);
              }, e);
            });
          },
          getServices: function (e, t) {
            var n = this;
            this._distantCall("services", e, t, function (e) {
              i.getServicesUrl({
                onComplete: function (t) {
                  e.call(n, t);
                },
              });
            });
          },
          launchMainRead: function (e, i) {
            require([
              "DS/EditPropWidget/ajaxRequests/EditPropAjaxRequests",
              "DS/EditPropWidget/ajaxRequests/ServicesAjaxRequests",
            ], function (n, s) {
              var a = new t("read");
              a.createRunningEvent(e),
                n.getListOfAttributes({
                  modelList: e,
                  tenant: s.getTenantFromModel(e && e.length > 0 ? e[0] : null),
                  handleRequest: {},
                  navigateToMain: !0,
                  securityContext: i.securityContext,
                  index_mode: i.index_mode,
                  ignoreKernelAccessChecks:
                    "true" === i.ignoreKernelAccessChecks,
                  onComplete: function (e) {
                    a.createReadyEvent(e);
                  },
                }),
                setTimeout(function () {
                  a && a.destroy();
                }, 12e4);
            });
          },
          launchFacetsVisibility: function (e, t, n, s, a) {
            var o = this,
              r = [
                "DS/EditPropWidget/ajaxRequests/EditPropAjaxRequests",
                "DS/EditPropWidget/ajaxRequests/ServicesAjaxRequests",
              ];
            void 0 === this._valuesAsync.facetCheck &&
              (this._valuesAsync.facetCheck = this._isAsynchronous);
            var l = this._valuesAsync.facetCheck && !s,
              c = !1,
              d = 0;
            (n.handleRequest = n.handleRequest ? n.handleRequest : {}),
              n.handleRequest && (n.handleRequest.handle = i.getCancelObject());
            var u = n.handleRequest.handle;
            s
              ? r.push("DS/EditPropWidget/comm/CommManager")
              : l &&
                ((d = 200),
                this.getAsyncFacetCheck(function (e) {
                  1 == c ||
                    u.isCancelled() ||
                    (e &&
                      ((c = !0),
                      (o._valuesAsync.facetCheck = !1),
                      n.onComplete(e)));
                })),
              require(r, function (i, s, r) {
                setTimeout(function () {
                  if (!0 !== c && !u.isCancelled()) {
                    var l = null;
                    r &&
                      ((l = new r("facetCheck")).createRunningEvent(e),
                      setTimeout(function () {
                        l && l.destroy();
                      }, 12e4)),
                      i.checkFacetsVisibility({
                        handleRequest: n.handleRequest,
                        context: n.context,
                        tenant: s.getTenantFromModel(e),
                        model: e,
                        facets: t,
                        sourceAppId: a,
                        onComplete: function (e) {
                          l && l.createReadyEvent(e),
                            "function" == typeof n.onComplete &&
                              !0 !== c &&
                              n.onComplete(e),
                            (c = !0),
                            (o._valuesAsync.facetCheck = !1);
                        },
                      });
                  }
                }, d);
              });
          },
          launchMainReadFromIndexService: function (e, i) {
            require([
              "DS/EditPropWidget/ajaxRequests/EditPropAjaxRequests",
            ], function (n) {
              var s = new t("read");
              s.createRunningEvent({ model: e, options: i });
              var a = {
                modelList: [e],
                tenant: e.getTenant(),
                handleRequest: i.handleRequest,
                select_predicates: i.predicates,
                defService: i.service,
                onComplete: function (e) {
                  var t = s.getCurrentComm().status.answer.options;
                  s.createReadyEvent({ response: e, options: t });
                },
              };
              i.getPLMParameter && (a.getPLMParameter = i.getPLMParameter),
                n.getListOfAttributes(a),
                setTimeout(function () {
                  s && s.destroy();
                }, 12e4);
            });
          },
          launchFacetsVisibilityFromIndexService: function (e, s, a) {
            var o = this,
              r = new t("facetCheck");
            r.createRunningEvent({ model: e, facets: s, options: a });
            var l = {};
            void 0 === o._valuesAsync.facetCheck &&
              (o._valuesAsync.facetCheck = o._isAsynchronous);
            var c = o._valuesAsync.facetCheck,
              d = !1;
            (a.handleRequest = a.handleRequest ? a.handleRequest : {}),
              a.handleRequest && (a.handleRequest.handle = i.getCancelObject());
            var u = a.handleRequest.handle;
            if (
              (c &&
                (200,
                o.getAsyncFacetCheck(function (e) {
                  1 == d ||
                    u.isCancelled() ||
                    (e &&
                      ((d = !0),
                      (o._valuesAsync.facetCheck = !1),
                      a.onComplete(e)));
                })),
              e.get("metatype") === n.METATYPE_BUS)
            ) {
              (l.id = e.get("objectId")),
                (l.mains = {}),
                (l.mains[e.get("objectId")] = {
                  id: e.get("objectId"),
                  type: e.get("type"),
                  metatype: "bus",
                });
              var h = {};
              s.forEach(function (e) {
                e.name && e.name === n.FACET_iPROPERTIES
                  ? (h[e.name] = { available: !0 })
                  : (h[e.name] = { available: !1 });
              }),
                (l.facets = h),
                r && r.createReadyEvent(l),
                "function" == typeof a.onComplete &&
                  !0 !== d &&
                  a.onComplete(l),
                (d = !0),
                (o._valuesAsync.facetCheck = !1);
            } else
              e.get("metatype") === n.METATYPE_REL &&
                require([
                  "DS/EditPropWidget/ajaxRequests/EditPropAjaxRequests",
                  "DS/EditPropWidget/constants/ConstantsMapping",
                ], function (t, i) {
                  var c = i.getServiceBySource("3dspace");
                  t.getInstanceDetails({
                    modelList: [e],
                    defService: c,
                    onComplete: function (t) {
                      !(function (t) {
                        if (
                          t &&
                          t.infos &&
                          t.infos.status &&
                          t.infos.status === n.EP_RESPONSE_OK
                        ) {
                          var i = null,
                            c = null,
                            u = null;
                          t &&
                            t.result &&
                            t.result.length > 0 &&
                            (t.result.forEach(function (e) {
                              for (var t in e)
                                "outputs" === t &&
                                  e[t].RELATIONS &&
                                  e[t].RELATIONS &&
                                  e[t].RELATIONS.length > 0 &&
                                  e[t].RELATIONS.forEach(function (e) {
                                    e.physicalid && (i = e.physicalid),
                                      e["ds6w:type"] && e["ds6w:type"],
                                      e.to &&
                                        e.to.physicalid &&
                                        e.to["ds6w:type"] &&
                                        ((c = e.to.physicalid),
                                        (u = e.to["ds6w:type"]));
                                  });
                            }),
                            (l.mains = {}),
                            (l.mains[i] = { id: c, type: u, metatype: "bus" }));
                          var h = {};
                          s.forEach(function (e) {
                            e.name && e.name === n.FACET_iPROPERTIES
                              ? (h[e.name] = { available: !0 })
                              : (h[e.name] = { available: !1 });
                          }),
                            (l.facets = h),
                            (l.id = e.get("objectId")),
                            r && r.createReadyEvent(l),
                            "function" == typeof a.onComplete &&
                              !0 !== d &&
                              a.onComplete(l),
                            (d = !0),
                            (o._valuesAsync.facetCheck = !1);
                        }
                      })(t);
                    },
                    primitives: [
                      { navigate_to_rel: { id: 1, mode: "return_rel" } },
                    ],
                    patterns: { RELATIONS: [{ id: 1 }] },
                    attributes: ["ds6w:type"],
                  });
                });
          },
        })
      );
    }
  ),
  define(
    "DS/EditPropWidget/utils/EditPropUtils",
    [
      "DS/EditPropWidget/models/EditPropModel",
      "DS/EditPropWidget/constants/EditPropConstants",
      "DS/WidgetServices/WidgetServices",
      "DS/EditPropWidget/models/EditPropAttributeModel",
      "DS/EditPropWidget/constants/EditPropConstants",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s, a) {
      "use strict";
      var o = a.get("accordion_parameters"),
        r = function (e, t, i) {
          var s = [];
          if (t) {
            t.forEach(function (t, a) {
              var r = (function (e, t, i) {
                  var s = new n(),
                    a = null;
                  i && t.extension && (a = i[t.extension]), (a = a || {});
                  var r = 1;
                  s.set("multisel", e.isMergedModel()),
                    s.set("name", t.name),
                    s.set("label", t.name + " (NLS not defined)"),
                    s.set("path", t.selectable),
                    s.set("select", t.selectable),
                    s.set("select", t.select),
                    s.set("type", t.type),
                    t.speType && s.set("type", t.speType),
                    s.set(
                      "multiline",
                      null != t.multiline && !1 !== t.multiline
                    );
                  var l = null != t.multivalue && !1 !== t.multivalue;
                  s.set("multivalue", l);
                  var c = t.value,
                    d = t.valueDB;
                  if (
                    (e.isMergedModel() || ((c = { r: c }), d && (d = { r: d })),
                    c)
                  )
                    for (var u in c) {
                      var h = c[u],
                        p = d ? d[u] : p;
                      !1 === l
                        ? ((h = null === h ? "" : h),
                          (p = null === p ? "" : p),
                          Array.isArray(h) &&
                            ((h = h.length > 0 ? h[0] : ""),
                            p && (p = p.length > 0 ? p[0] : "")),
                          (h = void 0 === h ? "" : h))
                        : ("" !== h && null != h) || ((h = []), (p = [])),
                        (c[u] = h),
                        d && (d[u] = p);
                    }
                  else if (e.isMergedModel()) {
                    var m = e.getMergingModels();
                    for (var f in ((c = {}), (d = {}), m)) {
                      var g = m[f],
                        v = !1 === l ? "" : [];
                      (c[g.get("objectId")] = v), (d[g.get("objectId")] = v);
                    }
                  }
                  e.isMergedModel() || ((c = c.r), d && (d = d.r)),
                    t.range &&
                      ("string" == typeof t.range &&
                        (t.range = t.range.split(",")),
                      s.set("authorizedValues", t.range)),
                    t.rangeNLS &&
                      ("string" == typeof t.rangeNLS &&
                        (t.rangeNLS = t.rangeNLS.split(",")),
                      s.set("authorizedValuesNLS", t.rangeNLS)),
                    void 0 !== t.range_required &&
                      s.set(
                        "authorizedValuesRequired",
                        !0 === t.range_required
                      );
                  var _ = !0;
                  t.readOnly || "owner" !== t.selectable || (_ = !1),
                    ("No" !== t.readOnly && !1 !== t.readOnly) || (_ = !1),
                    s.set("readOnly", _),
                    s.set("value", c),
                    d ? s.set("valueDB", d) : s.set("valueDB", c),
                    s.set(
                      "mandatory",
                      "Yes" === t.mandatory || !0 === t.mandatory
                    ),
                    s.set("label", t.nls),
                    s.set("path", t.path),
                    s.set("dimension", t.dimension),
                    s.set("unitName", t.unitName),
                    s.set("dimensionType", t.dimensionType),
                    s.set("maxlength", t.maxlength),
                    s.set("forbiddenChar", t.forbiddenChar),
                    t.minval &&
                      (s.set("minval", t.minval),
                      t.minvalincluded &&
                        s.set("minvalincluded", t.minvalincluded)),
                    t.maxval &&
                      (s.set("maxval", t.maxval),
                      t.maxvalincluded &&
                        s.set("maxvalincluded", t.maxvalincluded)),
                    t.attrID &&
                      (s.set("attributeID", t.attrID),
                      s.set("facet", o),
                      s.set("section", o)),
                    t.facet
                      ? (s.set("facet", t.facet), s.set("section", t.facet))
                      : a.name &&
                        !0 !== a.deploymentExtension &&
                        (s.set("facet", a.name),
                        s.set("section", a.nameNLS ? a.nameNLS : a.name)),
                    t.facetName && s.set("section", t.facetName),
                    t.extension && s.set("extension", t.extension),
                    void 0 !== t.from && s.set("from", t.from),
                    t.relation && s.set("relation", t.relation),
                    t.rules && s.set("rules", t.rules),
                    t.applysort && s.set("applySort", t.applysort);
                  var E = t.UIPosition,
                    C = ((r = 1), 1);
                  a.deploymentExtension && !0 !== t.maskedByType && (r = 2),
                    E || 0 === E || ((C = 2), (E = 0), s.set("masked", !1));
                  var S = r + "-" + C + "-" + E;
                  return (
                    s.set("positionUI", S),
                    t.group &&
                      "Default" !== t.group &&
                      (s.set("group", t.group),
                      s.set("groupNLS", t.groupNLS),
                      s.set("groupWeight", t.groupWeight)),
                    s.saveInitialValues(),
                    s
                  );
                })(e, t, i),
                l = r.get("positionUI");
              (l += "-" + a), r.set("positionUI", l), s.push(r);
            });
          }
          return s;
        };
      return {
        getModelFromSelectedNode: function (n) {
          var s,
            a = null;
          null !=
            (s =
              !(s =
                n && "function" == typeof n.getInstID ? n.getInstID() : null) &&
              n &&
              "function" == typeof n.getRelationID
                ? n.getRelationID()
                : s) &&
            Array.isArray(s) &&
            (s = s.length > 0 ? s[0] : null);
          var o = !!s,
            r = o ? "relationship" : "businessobject";
          if (
            (s = (s = s || n.getID()) || n.id) ===
            (n.plmType || (n.getType && n.getType()))
          )
            return null;
          var l = !o && n.metatype ? n.metatype : r,
            c = n.source ? n.source : null,
            d = n.tenant ? n.tenant : null,
            u = !o && n.type ? n.type : null;
          let h = n.sourceName ? n.sourceName : null,
            p = n.indexModeParameter ? n.indexModeParameter : null;
          if (
            (null === u &&
              !o &&
              n.options &&
              n.options.type &&
              (u = n.options.type),
            s)
          ) {
            var m = { metatype: l, objectId: s };
            d = d || i.getTenantID();
            if (n.getAttributeValue) {
              let e = n.getAttributeValue("i3dx");
              e && (m.i3dxId = e);
            }
            d && (m.tenant = d),
              c && (m.source = c),
              h && (m.sourceName = h),
              void 0 !== p && (m.indexModeParameter = p),
              u && ((m.type = u), (m[t.MODEL_DS_ATTR + "type"] = u)),
              (a = new e(m));
          }
          return a;
        },
        getModelFromPath: function (i, n, s, a) {
          var o = i.length,
            r = 1 == o || s || o % 2 == 0,
            l = r ? o - 1 : o - 2,
            c = i[l],
            d = n ? n[c] : null,
            u = "";
          n && !r && (d = n[(u = i[l + 1])]);
          var h = {
            objectId: c,
            metatype: (g = s || !r ? "relationship" : "businessobject"),
          };
          if (
            (a &&
              (a.source && (h.source = a.source),
              a.sourceName && (h.sourceName = a.sourceName),
              void 0 !== a.indexModeParameter &&
                (h.indexModeParameter = a.indexModeParameter)),
            d)
          ) {
            var p = d.source ? d.source : null,
              m = d.type ? d.type : null,
              f = d.tenant ? d.tenant : null,
              g = r && d.metatype ? d.metatype : g;
            f && (h.tenant = f),
              p && (h.source = p),
              m && (h[t.MODEL_DS_ATTR + "type"] = m),
              g && (h.metatype = g),
              u && (h.selReferenceID = u);
          }
          return new e(h);
        },
        getPreviewUrl: function (e) {
          var t = e.preview_url;
          return t || "";
        },
        fillFromServer: function (e, t, a) {
          var o = {
            modifyAccess: !0,
            success: !1,
            allAttributeAreRO: !0,
            plmparams: !1,
          };
          if (!1 !== e.success) {
            (o.success = !0), (o.modifyAccess = !1 !== e.modifyAccess);
            var l = this.getPreviewUrl(e),
              c = e.type_icon_url || "",
              d = (function (e) {
                var t = e.type_icon_large_url;
                return t || "";
              })(e);
            c === d && (d = "");
            var u = t.getChangedValues(),
              h = {};
            l && (h.previewUrl = l),
              d && (h.type_icon_url = d),
              c && (h.type_small_icon = c),
              e.thumbnailsRW && (h.activeThumbnail = !0),
              (o.allAttributeAreRO = (function (e, t, n) {
                var s = !0;
                n || (n = {});
                var a = [
                  "owner",
                  "modified",
                  "branch.owner",
                  "branch.modified",
                ];
                //20260903 update by liujr XEN详细属性面板继续隐藏旧客户零件字段，DirectBuy改为显示、只读且非必填。
                var jfXENHiddenAttrs = [
                  "JFCustomerPartNumber",
                  "JFCustomerPartRevision",
                  "CustomerPartNumber",
                  "CustomerPartRevision",
                  "JF_CustomerPartNumber",
                  "JF_CustomerPartRevision",
                  "attribute_JFCustomerPartNumber",
                  "attribute_JFCustomerPartRevision",
                  "attribute_CustomerPartNumber",
                  "attribute_CustomerPartRevision",
                  "attribute_JF_CustomerPartNumber",
                  "attribute_JF_CustomerPartRevision",
                  "JF_VPMReference.JF_CustomerPartNumber",
                  "JF_VPMReference-JF_CustomerPartNumber",
                  "JF_VPMReference.JF_CustomerPartRevision",
                  "JF_VPMReference-JF_CustomerPartRevision",
                  "客户零件号",
                  "客户零件版本",
                  "客户零件版本号"
                ];
                var jfXENReadOnlyAttrs = [
                  "DirectBuy",
                  "JF_DirectBuy",
                  "attribute_JF_DirectBuy",
                  "JF_VPMReference.JF_DirectBuy",
                  "JF_VPMReference-JF_DirectBuy",
                  "ds6wg:JF_VPMReference.JF_DirectBuy",
                  "attribute_JF_VPMReference.JF_DirectBuy"
                ];
                var jfIsXENApp = false;
                if (typeof widget !== "undefined" && widget && widget.data && "ENXENG_AP" == widget.data.appId) {
                  jfIsXENApp = true;
                }
                var jfIsHiddenXENAttr = function (e) {
                  if (!jfIsXENApp || !e) {
                    return false;
                  }
                  var i = e.getIdentifier ? e.getIdentifier() : "";
                  var n = e.name || "";
                  var s = e.path || "";
                  var a = e.dbName || "";
                  var o = e.dataIndex || "";
                  var r = e.nls || e.label || e.displayName || "";
                  return jfXENHiddenAttrs.some(function (e) {
                    return i === e || n === e || s === e || a === e || o === e || r === e || i.endsWith("-" + e) || i.indexOf("attribute[" + e + "]") >= 0 || s.indexOf("attribute[" + e + "]") >= 0;
                  });
                };
                var jfIsReadOnlyXENAttr = function (e) {
                  if (!jfIsXENApp || !e) {
                    return false;
                  }
                  var i = e.getIdentifier ? e.getIdentifier() : "";
                  var n = e.name || "";
                  var s = e.path || "";
                  var a = e.dbName || "";
                  var o = e.dataIndex || "";
                  var r = e.nls || e.label || e.displayName || "";
                  return jfXENReadOnlyAttrs.some(function (e) {
                    return i === e || n === e || s === e || a === e || o === e || r === e || i.endsWith("-" + e) || i.indexOf("attribute[" + e + "]") >= 0 || s.indexOf("attribute[" + e + "]") >= 0;
                  });
                };
                (i.getWidgetValue("index_mode") || "Federated" === t.source) &&
                  (a = ["ds6w:responsible", "ds6w:modified"]);
                var o = n.type_small_icon,
                  l = r(e, t.data, t.extensions),
                  c = r(e, t.basicData, t.extensions);
                return (
                  r(e, t.groupData, t.extensions).forEach(function (t) {
                    if (jfIsHiddenXENAttr(t)) {
                      return;
                    }
                    jfIsReadOnlyXENAttr(t) && (t.set("readOnly", true), t.set("mandatory", false));
                    (s = s && !0 === t.get("readOnly")),
                      (t.facet = t.group),
                      (t.extension = t.group),
                      "type_icon_url" === t.name &&
                        o !== t.value &&
                        (n.type_icon_url = t.value),
                      "type" === t.name && t.set("urlIcon", o);
                    var i = 0,
                      r = a.indexOf(t.name);
                    t.get("positionUI") && (i = t.get("positionUI")),
                      !e.isMergedModel() &&
                        r >= 0 &&
                        (0 === r || 2 === r
                          ? t.set("sectionIdentityCard", "ownerName")
                          : 1 === r || 3 === r
                          ? t.set("sectionIdentityCard", "date")
                          : t.set("positionIdentityCard", r)),
                      !0 === t.get("masked") &&
                        (t.set("positionUI", i), (n[t.getIdentifier()] = t));
                  }),
                  c.forEach(function (t) {
                    if (jfIsHiddenXENAttr(t)) {
                      return;
                    }
                    jfIsReadOnlyXENAttr(t) && (t.set("readOnly", true), t.set("mandatory", false));
                    (s = s && !0 === t.get("readOnly")),
                      "type_icon_url" === t.name &&
                        o !== t.value &&
                        (n.type_icon_url = t.value),
                      "type" === t.name && t.set("urlIcon", o);
                    var i = 0,
                      r = a.indexOf(t.name);
                    t.get("positionUI") && (i = t.get("positionUI")),
                      !e.isMergedModel() &&
                        r >= 0 &&
                        (0 === r || 2 === r
                          ? t.set("sectionIdentityCard", "ownerName")
                          : 1 === r || 3 === r
                          ? t.set("sectionIdentityCard", "date")
                          : t.set("positionIdentityCard", r)),
                      !0 === t.get("masked") &&
                        (t.set("positionUI", i), (n[t.getIdentifier()] = t));
                  }),
                  l.forEach(function (t, i) {
                    if (jfIsHiddenXENAttr(t)) {
                      return;
                    }
                    jfIsReadOnlyXENAttr(t) && (t.set("readOnly", true), t.set("mandatory", false));
                    (s = s && !0 === t.get("readOnly")),
                      "ds6w:type" === t.name && t.set("urlIcon", o);
                    var r = 0,
                      l = a.indexOf(t.name);
                    t.get("positionUI") && (r = t.get("positionUI")),
                      !e.isMergedModel() &&
                        l >= 0 &&
                        (0 === l || 2 === l
                          ? t.set("sectionIdentityCard", "ownerName")
                          : 1 === l || 3 === l
                          ? t.set("sectionIdentityCard", "date")
                          : t.set("positionIdentityCard", l)),
                      t.set("positionUI", r),
                      (n[t.getIdentifier()] = t);
                  }),
                  s
                );
              })(t, e, h));
            var p = (function (e) {
                var t = {},
                  i = e.computed;
                if (i && i.label) {
                  var n = i.label;
                  if (n.value && "object" == typeof n.value)
                    for (var s in n.value) {
                      var a = n.value[s];
                      n.value[s] = Array.isArray(a) && a.length > 0 ? a[0] : a;
                    }
                  (t.label = n.value),
                    n.path && (t.attributeId = n.path),
                    !n.path && n.name && (t.attributeId = n.name);
                }
                return t;
              })(e),
              m = p.label;
            m ||
              (t.isMergedModel()
                ? ((m = {}),
                  t.getMergedModelIds().forEach(function (e) {
                    m[e] = "";
                  }))
                : (m = "")),
              m && (h.label = Array.isArray(m) ? m[0] : m),
              p.attributeId && h[p.attributeId]
                ? ((h.labelAttribute = p.attributeId),
                  h[p.attributeId].set("sectionIdentityCard", "title"),
                  t.addEvent("onChange:" + p.attributeId, function () {
                    var e = t.get(p.attributeId);
                    e && t.set("label", e.valueDB);
                  }))
                : (h[s.MODEL_DS_ATTR + "label"] = new n({
                    sectionIdentityCard: "title",
                    value: m,
                    valueDB: m,
                    multisel: t.isMergedModel(),
                    isTransient: !0,
                    path: s.MODEL_DS_ATTR + "label",
                  }));
            var f = {};
            for (var g in u) h[g] || (f[g] = u[g]);
            e.type &&
              (h.type ? h.type.set("valueDB", e.type) : (h.type = e.type));
            var v = t.getAllAttributes();
            for (var g in v) {
              var _ = v[g];
              "object" == typeof _ &&
                _ instanceof n &&
                !_.isTransient &&
                (a.initTable || _.attributeID) &&
                delete v[g];
            }
            if ((t.set(h), t && t.isMergedModel())) {
              var E = t.getMergingModels();
              for (var C in E) {
                var S = E[C];
                S.clearChangedValue();
                var y = UWA.clone(h);
                for (var g in h) {
                  var A = h[g];
                  if (A instanceof n) {
                    var I = new n(y[g]);
                    I.set("value", A.get("value")[C]),
                      I.set("valueDB", A.get("valueDB")[C]),
                      I.set("multisel", !1),
                      I.saveInitialValues(),
                      (y[g] = I);
                  }
                }
                S.set(y), S.setChangedValues(f);
              }
            }
            t.clearChangedValue(),
              t.setChangedValues(f),
              (function (e) {
                !e.get("type") ||
                  "true" !== i.getWidgetValue("admin") ||
                  ("CATProduct" !== e.get("type").value &&
                    "CATPart" !== e.get("type").value) ||
                  (e.get("project") && e.get("project").set("readOnly", !1));
              })(t),
              (o.plmparams =
                !0 ===
                (function (e) {
                  var t = e.PLMParametersAvailable;
                  return (t = t || !1);
                })(e));
          }
          return o;
        },
        getModelFromPathInIndexMode: function (i, n, s, a) {
          let o = "";
          i && i.length > 0 && (o = i[0]);
          let r = "";
          n && n.length > 0 && (r = n[0]);
          let l = o;
          r && (l = r);
          let c = s ? s[l] : null,
            d = { metatype: "businessobject" };
          if (
            (a &&
              (a.source && (d.source = a.source),
              a.sourceName && (d.sourceName = a.sourceName),
              void 0 !== a.indexModeParameter &&
                (d.indexModeParameter = a.indexModeParameter)),
            o && (d.objectId = o),
            r && (d.i3dxId = r),
            c)
          ) {
            let e = c.type ? c.type : null;
            e && ((d[t.MODEL_DS_ATTR + "type"] = e), (d.type = e));
          }
          return new e(d);
        },
      };
    }
  ),
  define(
    "DS/EditPropWidget/ajaxRequests/3DDriveAjaxRequest",
    [
      "DS/WidgetServices/WidgetServices",
      "DS/EditPropWidget/ajaxRequests/ServicesAjaxRequests",
      "DS/EditPropWidget/constants/EditPropConstants",
      "DS/EditPropWidget/constants/ConstantsMapping",
      "DS/EditPropWidget/utils/EditPropUtils",
      "DS/WAFData/WAFData",
      "DS/FedDictionaryAccess/FedDictionaryAccessAPI",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s, a, o, r) {
      "use strict";
      var l = {},
        c = {},
        d = {},
        u = function (e, t, i) {
          var n = e[t];
          n
            ? (!0 == !n.multivalue &&
                ((n.value = [n.value]),
                (n.valueDB = [n.valueDB]),
                (n.multivalue = !0)),
              n.value.push(i.value),
              n.valueDB.push(i.valueDB))
            : (e[t] = i);
        };
      return {
        checkFacetsVisibility: function (e) {
          var t = {};
          if (null != e.facets && e.facets.length > 0)
            for (var n = 0; n < e.facets.length; n++) {
              var s = e.facets[n].name;
              "properties" === s
                ? (t[i.FACET_PROPERTIES] = !0)
                : "social" === s
                ? (t[i.FACET_SWYMCOMMENTS] = !0)
                : (t[i.FACET_PROPERTIES] = !0);
            }
          else t[i.FACET_PROPERTIES] = !0;
          e.onComplete.call(this, { id: e.model.get("objectId"), facets: t });
        },
        getSourceId: function (e) {
          var t = n.getSupportedServices(),
            i = void 0,
            s = "",
            a = e.get("sourceName");
          return (
            a && (i = t[a.toLowerCase()]),
            i || ((a = e.get("source")) && (i = t[a.toLowerCase()])),
            i && i.fedPrefix && (s = i.fedPrefix),
            s
          );
        },
        fillModel: function (e, t) {
          var i = e.onComplete;
          return (
            (e.onComplete = function (e) {
              e &&
                e.results &&
                e.results.length > 0 &&
                s.fillFromServer(e.results[0], t, {}),
                i.apply(this, [t]);
            }),
            (e.modelList = [t]),
            this.getListOfAttributes(e)
          );
        },
        getIdFromModel: function (e) {
          var t = this.getSourceId(e),
            i = e.get("objectId");
          return (
            (i = i || ""), t && 0 !== i.indexOf(t) ? t + ':"' + i + '"' : i
          );
        },
        _getModelType: function (e) {
          var t,
            i = e.get("ds6w:type");
          t = "string" == typeof i ? i : i ? i.get("valueDB") : "";
          var n = e.get("source");
          t ||
            "3DSwym" !== n ||
            (0 == e.get("objectId").indexOf("iam") && (t = "pno:Person"));
          return t;
        },
        getListOfAttributes: function (i) {
          var n = i.modelList;
          i.securityContext;
          if (n.length) {
            var s = n[0],
              a = s.get("objectId"),
              o = "";
            if (a) {
              o = "/search";
              var r = [];
              s.get("sourceName") &&
                ("3DDrive" === s.get("sourceName")
                  ? r.push("drive")
                  : r.push(s.get("sourceName"))),
                (i.model = s),
                (i.idModel = a),
                (i.onCompleteWS = this._onCompleteFederated.bind(this));
              var l = e.getLanguage(),
                c = e.getCancelObject();
              this.getMask(
                { "ds6w:type": { valueDB: this._getModelType(s) } },
                function (e) {
                  var n = [];
                  e.Write.attributes.forEach(function (e) {
                    var t = e.name;
                    n.push(t);
                  });
                  var a = this.getIdFromModel(s),
                    d = {
                      with_indexing_date: !1,
                      with_synthesis: !1,
                      with_synthesis_attribute: !0,
                      with_nls: !1,
                      locale: l,
                      query: a,
                      tenant: t.getTenantFromModel(s),
                      source: r,
                      select_predicate: n,
                      start: 0,
                      select_file: ["icon", "thumbnail_2d"],
                    },
                    u =
                      "PropertiesPanel-" +
                      (window.top.dsUserLogin
                        ? window.top.dsUserLogin
                        : "unknown") +
                      "-" +
                      new Date().getTime();
                  (d.label = u),
                    (i.source = "3DSearch"),
                    (c.handler = t.createRequest(i, "POST", o, d));
                }
              );
              return c;
            }
          }
        },
        getElementsNls: function (t, i) {
          t && t.length > 0
            ? o.getResourcesInfo(t.join(","), {
                tenantId: i.tenant,
                lang: e.getLanguage(),
                apiVersion: "R2018x",
                onComplete: function (e) {
                  i.onComplete.call(this, e);
                },
                onFailure: function (e) {
                  console.log(" --\x3e> Can't get ElementsNls " + e),
                    i.onComplete.call(this, { success: !1 });
                },
              })
            : i.onComplete.call(this, null);
        },
        getValuesNls: function (t, i) {
          t
            ? o.getNlsOfPropertiesValues(JSON.stringify(t), {
                tenantId: i.tenant,
                lang: e.getLanguage(),
                apiVersion: "R2019x",
                onComplete: function (e) {
                  i.onComplete.call(this, e);
                },
                onFailure: function (e) {
                  console.log(" --\x3e> Can't get ValuesNls " + e),
                    i.onComplete.call(this, { success: !1 });
                },
              })
            : i.onComplete.call(this, null);
        },
        _callMask: function (e, t, i) {
          var n = this;
          d[e]
            ? i.call(n, d[e])
            : require([t], function (t) {
                (t = JSON.parse(t))[e] ? (d[e] = t[e]) : (d[e] = t),
                  i.call(n, d[e]);
              });
        },
        getMask: function (e, t) {
          var i = e["ds6w:type"];
          i && "pno:Person" === i.valueDB
            ? this._callMask(
                "Person",
                "text!DS/EditPropWidget/assets/masks/3DSwym/Person.json",
                t
              )
            : this._callMask(
                "Default",
                "text!DS/EditPropWidget/assets/masks/Federated/default.json",
                t
              );
        },
        _onCompleteFederated: function (i, n) {
          var s = this,
            a = i.results,
            o = { source: "Federated" },
            d = [],
            h = {};
          if (((o.data = d), a && a.length > 0)) {
            var p = {},
              m = {},
              f = {},
              g = {},
              v = null,
              _ = null,
              E = a[0],
              C = {};
            for (var S in (E.attributes.forEach(function (t) {
              var i = t.name,
                n = i,
                s = t.value;
              s = void 0 === s ? "" : s;
              var a = "ds6w_facet" === t.format,
                r = a;
              if (a) {
                var d = i.split("/");
                n = d[d.length - 1];
              }
              if ((a = a || "attribute" === t.format)) {
                (v = !0 === r ? g : m), (_ = !0 === r ? f : p);
                var E = t.type,
                  S = !1;
                if ("date" === E) {
                  var y = Date.parse(s),
                    A = new Date(y);
                  A &&
                    ((s = (s = e.convertDateToStandard(A, !0)).valueDB),
                    ("ds6w:modified" !== n && "ds6w:created" !== n) ||
                      (E = "datetime"));
                } else S = "string" === E;
                0 === s.indexOf(n) && (s = s.replace(n + ".", ""));
                var I = void 0 !== t.dispValue,
                  b = I ? t.dispValue : s;
                if (S && !I) {
                  var P = c[n];
                  b = s;
                  var T = P ? P[s] : null;
                  if (T) b = T;
                  else {
                    var D = C[n];
                    D || ((D = []), (C[n] = D)), D.push(s);
                  }
                }
                var M = {
                  name: n,
                  selectable: n,
                  path: i,
                  type: E,
                  value: b,
                  valueDB: s,
                  readOnly: !0,
                  nls: n,
                };
                "ds6w:label" === n && (h = M);
                var w = l[n];
                w ? ((M.nls = w.nlsName), u(v, n, M)) : u(_, n, M);
              } else "resourceid" === n ? (o.physicalID = s) : (o[i] = s);
            }),
            f))
              p[S] || (p[S] = f[S]);
            for (var S in g) m[S] || (m[S] = g[S]);
            n.idModel && (o.physicalID = n.idModel),
              (o.computed = { label: h });
            var y = Object.keys(p),
              A = new Promise(function (e) {
                s.getElementsNls(y, {
                  tenant: t.getTenantFromModel(n.model),
                  onComplete: function (t) {
                    e(t);
                  },
                });
              }),
              I = new Promise(function (e) {
                s.getValuesNls(C, {
                  tenant: t.getTenantFromModel(n.model),
                  onComplete: function (t) {
                    e(t);
                  },
                });
              });
            Promise.all([A, I]).then(function (e) {
              var i = e[0],
                a = e[1];
              if (i)
                if (!1 !== i.success) {
                  var r = i || {};
                  r.vocabularyElementNLSInfo &&
                    r.vocabularyElementNLSInfo.forEach(function (e) {
                      var t = p[e.uri];
                      "Predicate" === e.type ? (l[e.uri] = e) : (c[e.uri] = e),
                        t && e.nlsName && ((t.nls = e.nlsName), (m[e.uri] = t));
                    });
                } else t._raiseError(i.error ? i.error.message : "Error", n);
              for (var u in a) {
                var h = m[u];
                if (h) {
                  var f = a[u];
                  if (h.multivalue) {
                    var g = [];
                    h.value.forEach(function (e) {
                      var t = f[e];
                      g.push(t || e);
                    }),
                      (h.value = g);
                  } else h.value = f[h.value] ? f[h.value] : h.value;
                }
              }
              s.getMask(m, function (e) {
                e.Write.attributes.forEach(function (e) {
                  var t = e.name,
                    i = m[t];
                  i && d.push(i);
                }),
                  n.onComplete.call(null, { results: [o] });
              });
            });
          } else t._raiseError(r.get("FedAjax_NotFound"), n);
        },
      };
    }
  ),
  define(
    "DS/EditPropWidget/facets/Properties/TweakerManager",
    [
      "UWA/Core",
      "UWA/Class",
      "DS/WidgetServicesUI/helpers/Helpers",
      "DS/WidgetServicesUI/helpers/Constants",
      "DS/EditPropWidget/models/EditPropAttributeModel",
      "DS/WidgetServices/WidgetServices",
      "DS/WebappsUtils/WebappsUtils",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s, a, o, r) {
      var l = {};
      (l.DEFAULT = {
        type: "DEFAULT",
        from: function (e) {
          return e.valueDB;
        },
        to: function (e) {
          return {
            valueDB: e.value,
            value: e.displayValue ? e.displayValue : e.value,
          };
        },
        extraOptions: function (e, t) {
          var i = t.authorizedValuesNLS,
            n = t.authorizedValues,
            s = t.authorizedValuesGroup,
            a = t.authorizedValuesIcon,
            r = t.customSelect,
            c = t.sortedOptions,
            d = !0 === t.autocomplete,
            u = i.length;
          if (u > 0) {
            var h = !1,
              p = t.get("tw_type"),
              m = l[p];
            (m && m.from) || (m = l.DEFAULT);
            for (var f = [], g = [], v = 0; v < u; v++) {
              var _ = i[v],
                E = n[v],
                C = { label: _, value: E, group: s ? s[v] : null };
              if (a && a[v]) {
                var S = "icons/16/" + a[v] + ".png",
                  y = o.getWebappsAssetUrl("ENOCollabSharingCmds", S);
                C.icon = y;
              }
              g.push(C),
                h || i[v] !== n[v]
                  ? (h = !0)
                  : ((E = m.from({ value: _, valueDB: E })),
                    (_ = null),
                    f.push({ label: _, value: E }));
            }
            return {
              autocomplete: d,
              sortedOptions: c,
              customSelect: r,
              trueEnum: h,
              data: {
                list: h ? g : f,
                range_required: t.authorizedValuesRequired,
              },
            };
          }
        },
      }),
        (l[n.TYPES.BOOLEAN] = {
          type: n.TYPES.BOOLEAN,
          from: function (e) {
            return (
              !0 === e.valueDB ||
              ("string" == typeof e.valueDB &&
                "true" === e.valueDB.toLowerCase())
            );
          },
        }),
        (l[n.TYPES.RELATION] = {
          type: n.TYPES.RELATION,
          from: function (e) {
            return {
              id: e.valueDB,
              label: e.value && e.value.label ? e.value.label : "",
            };
          },
          to: function (e) {
            return {
              valueDB: e.value ? e.value.id : "",
              value: e.value ? e.value : "",
            };
          },
          extraOptions: function (e, t) {
            return { data: { rules: t.get("rules") } };
          },
        }),
        (l[n.TYPES.PERSON] = {
          type: n.TYPES.PERSON,
          from: function (e) {
            return { personID: e.valueDB, fullName: e.value };
          },
          to: function (e) {
            return { valueDB: e.value.personID, value: e.value.fullName };
          },
          extraOptions: function (e) {
            return {
              data: {
                objectIDs: e.isMergedModel()
                  ? e.getMergedModelIds()
                  : [e.get("objectId")],
              },
            };
          },
          check: function (e, t) {
            if (null === t)
              return { message: r.UnvalidAttribute, type: "error" };
          },
        }),
        (l[n.TYPES.DATE] = {
          type: n.TYPES.DATE,
          from: function (e) {
            return a.formatDateObj(e.valueDB);
          },
          to: function (e) {
            return {
              valueDB: a.convertDateToStandard(e.value, !0).valueDB,
              value: e.displayValue,
            };
          },
        }),
        (l[n.TYPES.DATETIME] = l[n.TYPES.DATE]),
        (l[n.TYPES.BOOLEAN_WUX] = l[n.TYPES.BOOLEAN]),
        (l[n.TYPES.DATE_WUX] = l[n.TYPES.DATE]),
        (l[n.TYPES.DATETIME_WUX] = l[n.TYPES.DATE]),
        (l[n.TYPES.PERSON_WUX] = l[n.TYPES.PERSON]);
      var c = function (e) {
        var t = l[e.get("tw_type")],
          i = null;
        if (t && t.check) {
          var n = e.get("valueDB");
          e.multisel && (n = n[Object.keys(n)[0]]),
            Array.isArray(n) || (n = [n]),
            n.every(function (n) {
              return null === (i = t.check(e, n));
            });
        }
        return i;
      };
      return t.extend({
        _sharedOptions: null,
        init: function (e, t) {
          this._parent(e),
            (this.elements = {}),
            (this.elements.fields = {}),
            (this._sharedOptions = t),
            (this.onNotification = e.onNotification
              ? e.onNotification
              : function () {}),
            (this.options = this.options ? this.options : {}),
            this._setEditable(e && !0 === e.editable);
        },
        isReadOnly: function () {
          var e = this._sharedOptions;
          return !e || !0 === e.readOnly;
        },
        clear: function () {
          for (var e in this.elements.fields) this._removeField(e);
          this.elements.fields = {};
        },
        actionOnScroll: function () {
          for (var e in this.elements.fields) {
            var t = this.elements.fields[e];
            t.field &&
              "function" == typeof t.field.actionOnScroll &&
              t.field.actionOnScroll();
          }
        },
        getValidationFunction: function (e) {
          var t = l[e];
          if (t && t.check) return c;
        },
        _getValueForTweaker: function (e, t) {
          var i = e,
            n = l[i];
          (n && n.from) || (n = l[(i = "DEFAULT")]);
          return "boolean_wux" == e && "" == t.value && "" == t.valueDB
            ? null
            : t.multisel
            ? this.__getValueFromMultiselForConvert(n, t)
            : this.__getValueFromStandardForConvert(n, t);
        },
        __getValueFromStandardForConvert: function (e, t) {
          var i = !0 === t.multivalue,
            n = e.type,
            s = t.value,
            a = Array.isArray(s) ? s : [s],
            o = s,
            r = a;
          ("DEFAULT" === n && t.readOnly) ||
            ((o = t.valueDB), (r = Array.isArray(o) ? o : [o]));
          for (var l = a.length, c = [], d = 0; d < l; d++) {
            var u = e.from({ value: a[d], valueDB: r[d] });
            c.push(u);
          }
          return i ? c : l > 0 ? c[0] : null;
        },
        __getValueFromMultiselForConvert: function (e, t) {
          var i = t.value,
            n = t.valueDB,
            s = {};
          for (var a in i) {
            var o = {
                value: i[a],
                valueDB: n[a],
                multivalue: t.multivalue,
                readOnly: t.readOnly,
              },
              r = this.__getValueFromStandardForConvert(e, o);
            s[a] = r;
          }
          return s;
        },
        __getValueFromStandardFromTweaker: function (e, t) {
          var i = t.value,
            n = t.displayValue,
            s = {},
            a = {};
          if (Array.isArray(i)) {
            (s = []), (a = []);
            for (var o = i.length, r = 0; r < o; r++) {
              var l = e.to({ value: i[r], displayValue: n[r] });
              s.push(l.value), a.push(l.valueDB);
            }
          } else
            (s = (l = e.to({ value: i, displayValue: n })).value),
              (a = l.valueDB);
          return { value: s, valueDB: a };
        },
        __getValueFromMultiselFromTweaker: function (e, t, i) {
          var n = t.value,
            s = t.displayValue;
          for (var a in ((valueAttr = {}), (valueDBAttr = {}), n)) {
            i &&
              i.multivalue &&
              ((n[a] = Array.isArray(n[a]) ? n[a] : [n[a]]),
              (s[a] = Array.isArray(s[a]) ? s[a] : [s[a]]));
            var o = this.__getValueFromStandardFromTweaker(e, {
              value: n[a],
              displayValue: s[a],
            });
            (valueAttr[a] = o.value), (valueDBAttr[a] = o.valueDB);
          }
          return { value: valueAttr, valueDB: valueDBAttr };
        },
        _getValueFromTweaker: function (e, t, i) {
          var n = l[e];
          (n && n.to) || (n = l.DEFAULT);
          i.multivalue;
          var s = { value: t.getValue(), displayValue: t.getDisplayValues() };
          return !0 === i.multisel
            ? this.__getValueFromMultiselFromTweaker(n, s, i)
            : this.__getValueFromStandardFromTweaker(n, s);
        },
        _generateOptions: function (e, t, i) {
          var n = {
            units: i.dimension,
            minValue: i.minval,
            maxValue: i.maxval,
            minValueIncluded: i.minvalincluded,
            maxValueIncluded: i.maxvalincluded,
            multiline: i.multiline,
            multisel: i.multisel,
            icon: i.urlIcon,
            parameter: !!i.attributeID,
            multivalued: i.multivalue,
            mandatory: i.mandatory,
            readOnly: i.readOnly,
            applySort: i.applySort,
            type: i.type,
          };
          this.isReadOnly() && (n.editable = !1),
            "prop" === t.facet && (n.prop = !0);
          var s = l[e];
          if (((s && s.extraOptions) || (s = l.DEFAULT), s && s.extraOptions)) {
            var a = s.extraOptions(t, i);
            for (var o in a) n[o] = a[o];
          }
          return i.dropdownMode && (n.dropdownMode = i.dropdownMode), n;
        },
        _updateValue: function (e, t, i, n) {
          if (e.isMergedModel()) {
            var s = e.getMergingModels();
            for (var a in (e.lockModel(!0), s))
              (innerModel = s[a]),
                this.__updateValue(
                  innerModel,
                  innerModel.get(t.getIdentifier()),
                  i[a],
                  n[a]
                );
            setTimeout(function () {
              e.lockModel(!1);
            });
          } else this.__updateValue(e, t, i, n);
        },
        __updateValue: function (e, t, i, n) {
          var s = e.getChangedValues(),
            a = t.getIdentifier(),
            o = (t.value, t.valueDB),
            r = s[a];
          if (r) r.value, (o = r.valueDB);
          var l = !1;
          if (
            !(l =
              (null == n || null == n
                ? ""
                : Array.isArray(n)
                ? n.sort().toString()
                : n) ===
              (null == o || null == o
                ? ""
                : Array.isArray(o)
                ? o.sort().toString()
                : o)) &&
            e.isMergedModel()
          ) {
            l = !0;
            var c = e.getMergingModels();
            for (var d in c) l = l && c[d] === o[d];
          }
          l || this._modelChangeValue(e, e.get(a), i, n);
        },
        _modelChangeValue: function (t, i, n, a) {
          var o = new s(e.clone(i));
          return (
            o.set("value", n),
            o.set("valueDB", a),
            t.set(i.getIdentifier(), o),
            o
          );
        },
        _getOrCreateField: function (e) {
          var t = this.elements.fields[e];
          return t || (t = this.elements.fields[e] = {}), t;
        },
        _addEventToField: function (e, t, i) {
          var n = this._getOrCreateField(t);
          n.events || (n.events = []), n.events.push(i), (n.model = e);
        },
        _addTweakerToField: function (e, t) {
          this._getOrCreateField(e).field = t;
        },
        _removeField: function (e) {
          var t = this.elements.fields[e];
          t.field &&
            ("function" == typeof t.destroy
              ? t.destroy()
              : "function" == typeof t.field.destroy && t.field.destroy()),
            t.events &&
              t.model &&
              t.events.forEach(function (i) {
                t.model.removeEvent(e, i);
              }),
            delete this.elements.fields[e];
        },
        setError: function (e) {},
        onNotification: function (e) {},
        _manageOnChangeEvent: function (e, t, i, n) {
          var s = function () {
            setTimeout(function () {
              i.elements.container ? n.call() : e.removeEvent(t, s);
            });
          };
          this._addEventToField(e, t, s), e.addEvent("onChange:" + t, s);
        },
        _setEditable: function (e) {
          this.editable = e && !this.isReadOnly();
        },
        underEdition: function () {
          return this.editable && !this.isReadOnly();
        },
        switchMode: function (e) {
          for (var t in (this._setEditable(e), this.elements.fields)) {
            var i = this.elements.fields[t];
            i && i.field && i.field.switchMode(this.underEdition());
          }
        },
        getTweakerFromId: function (e) {
          return this.elements.fields[e];
        },
        getTweaker: function (e, t, s, a, o) {
          var r = this,
            l = e.get(t),
            c = this._generateOptions(s, e, l);
          (e.webUxTweakers || e.propertiesWidgetUI) && !0 === c.trueEnum
            ? (s = n.TYPES.ENUM_WUX)
            : e.propertiesWidgetUI ||
              e.webUxTweakers ||
              !0 !== c.trueEnum ||
              (s = n.TYPES.ENUM);
          var d = this._getValueForTweaker(s, l);
          if (
            ((c.value = d),
            (c.events = {
              changes: function () {
                var i = e.get(t);
                if (i) {
                  var n = r._getValueFromTweaker(s, this, i);
                  r._updateValue(e, i, n.value, n.valueDB);
                }
              },
              notification: function (e) {
                r.onNotification(e);
              },
            }),
            a)
          )
            for (var u in a) c[u] = a[u];
          i.getTweaker(s, c, function (i) {
            r._addTweakerToField(t, i),
              r._manageOnChangeEvent(e, t, i, function () {
                var n = e.get(t),
                  a = r._getValueForTweaker(s, n);
                i.setValue(a);
              }),
              i.switchMode(r.underEdition()),
              o.call(this, i);
          });
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/ajaxRequests/FederatedAjaxRequests",
    [
      "DS/WidgetServices/WidgetServices",
      "DS/EditPropWidget/ajaxRequests/ServicesAjaxRequests",
      "DS/EditPropWidget/constants/EditPropConstants",
      "DS/EditPropWidget/constants/ConstantsMapping",
      "DS/EditPropWidget/utils/EditPropUtils",
      "DS/WAFData/WAFData",
      "DS/FedDictionaryAccess/FedDictionaryAccessAPI",
      "text!DS/EditPropWidget/assets/settings/SettingsForServices.json",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s, a, o, r, l) {
      "use strict";
      var c = {},
        d = {},
        u = {};
      r = JSON.parse(r);
      var h = function (e, t, i) {
        var n = e[t];
        n
          ? (!0 == !n.multivalue &&
              ((n.value = [n.value]),
              (n.valueDB = [n.valueDB]),
              (n.multivalue = !0)),
            n.value.push(i.value),
            n.valueDB.push(i.valueDB))
          : (e[t] = i);
      };
      return {
        getPreviewUrl: function (e) {
          if (!e.ids && e.modelList) {
            var t = this;
            (e.ids = []),
              e.modelList.forEach(function (i) {
                var n = t.getIdFromModel(i);
                n && e.ids.push(n);
              });
          }
          this._getPreviewUrl(e);
        },
        _getPreviewUrl: function (i) {
          var n = this,
            s = i.ids,
            o = s.length;
          o &&
            (i.handleRequest.handle = e.get3DSearchUrlAsync({
              platformId: i.tenant,
              onComplete: function (r) {
                var l = r + "/search",
                  c = e.getLanguage(),
                  d = "",
                  u = "";
                s.forEach(function (e) {
                  (d += u + e), (u = " OR ");
                });
                var h = {
                    with_indexing_date: !1,
                    with_synthesis: !1,
                    with_synthesis_attribute: !1,
                    with_nls: !1,
                    nresults: o,
                    locale: c,
                    query: d,
                    tenant: i.tenant,
                    start: 0,
                    select_file: ["icon", "thumbnail_2d"],
                  },
                  p =
                    "PropertiesPanel-" +
                    (window.top.dsUserLogin
                      ? window.top.dsUserLogin
                      : "unknown") +
                    "-" +
                    new Date().getTime();
                h.label = p;
                var m = {
                  Accept: "application/json",
                  "content-type": "application/json",
                };
                m["Accept-Language"] = e.getLanguage();
                var f = {
                  type: "json",
                  method: "POST",
                  timeout: 24e4,
                  data: UWA.Json.encode(h),
                  proxy: "passport",
                  headers: m,
                  cache: -1,
                  onComplete: function (e) {
                    n.onCompletePreview(e, i);
                  },
                  onFailure: function (e, n, s) {
                    t._onFailure(e, n, s, i);
                  },
                  onTimeout: function (e) {
                    t._onTimeout(i);
                  },
                };
                e.getWidgetValue("isStandalone") ||
                  (f.authentication = "passport"),
                  (i.handleRequest.handle = a.handleRequest(
                    t._formatUrl2(i.tenant, l),
                    f
                  ));
              },
            }));
        },
        onCompletePreview: function (e, t) {
          var i = [];
          e &&
            e.results &&
            e.results.forEach(function (e) {
              var t = null,
                n = null,
                s = !0;
              e.attributes &&
                e.attributes.every(function (e) {
                  return (
                    "resourceid" === e.name && (t = e.value),
                    "preview_url" === e.name && (n = e.value),
                    (s = !(null != t && null != n))
                  );
                }),
                s || i.push({ id: t, preview: n });
            }),
            t.onComplete(i);
        },
        checkFacetsVisibility: function (e) {
          var t = {};
          (t[i.FACET_PROPERTIES] = !0),
            e.onComplete.call(this, { id: e.model.get("objectId"), facets: t });
        },
        getSourceId: function (e) {
          var t = e.get("sourceName");
          t = t || e.get("source");
          var i = "",
            s = n.getSupportedServices()[t.toLowerCase()];
          return s && s.fedPrefix && (i = s.fedPrefix), i;
        },
        fillModel: function (e, t) {
          var i = e.onComplete;
          return (
            (e.onComplete = function (e) {
              e &&
                e.results &&
                e.results.length > 0 &&
                s.fillFromServer(e.results[0], t, {}),
                i.apply(this, [t]);
            }),
            (e.modelList = [t]),
            this.getListOfAttributes(e)
          );
        },
        getIdFromModel: function (e) {
          var t = this.getSourceId(e),
            n = e.get("objectId");
          return (
            e.get("metatype") === i.METATYPE_REL &&
              e.get("selReferenceID") &&
              (n = e.get("selReferenceID")),
            (n = n || ""),
            t && 0 !== n.indexOf(t) ? t + ':"' + n + '"' : n
          );
        },
        getQueryForIndexService: function (e) {
          let t = e.get("objectId"),
            n = e.get("i3dxId");
          e.get("metatype") === i.METATYPE_REL &&
            e.get("selReferenceID") &&
            (t = e.get("selReferenceID"));
          let s = "";
          return (s = n ? `[ds6w:i3dx]:"${n}"` : `[ds6w:resourceUid]:"${t}"`);
        },
        _getModelType: function (e) {
          var t,
            i = e.get("ds6w:type");
          t = "string" == typeof i ? i : i ? i.get("valueDB") : "";
          var n = e.get("source");
          t ||
            "3DSwym" !== n ||
            (0 == e.get("objectId").indexOf("iam") && (t = "pno:Person"));
          return t;
        },
        getListOfAttributes: function (n) {
          var s = n.modelList;
          n.securityContext;
          if (s.length) {
            var a = s[0],
              o = a.get("objectId");
            a.get("metatype") === i.METATYPE_REL &&
              a.get("selReferenceID") &&
              (o = a.get("selReferenceID")),
              a.get("i3dxId") && (o = a.get("i3dxId"));
            var l = "";
            if (o) {
              l = "/search";
              (n.model = a),
                (n.idModel = o),
                (n.onCompleteWS = this._onCompleteFederated.bind(this));
              var c = e.getLanguage(),
                d = e.getCancelObject();
              if (
                a.get("indexModeParameter") &&
                n.select_predicates &&
                Array.isArray(n.select_predicates) &&
                n.select_predicates.length > 0
              ) {
                let e = this.getQueryForIndexService(a),
                  i = [],
                  s = a.get("source");
                if (s) {
                  let e = r[s.toUpperCase()];
                  e && e.keyOnFedSearch && i.push(e.keyOnFedSearch);
                }
                var u = {
                  with_indexing_date: !1,
                  with_synthesis: !0,
                  with_synthesis_attribute: !0,
                  with_nls: !1,
                  locale: c,
                  query: e,
                  tenant: t.getTenantFromModel(a),
                  source: i,
                  select_predicate: n.select_predicates,
                  start: 0,
                  select_file: ["icon", "thumbnail_2d"],
                };
                n.getPLMParameter &&
                  ((u.source = ["3dspace"]),
                  (u.specific_source_parameter = {
                    "3dspace": { plmparam: { retrieve_plmparam: !0 } },
                  }));
                var h =
                  "PropertiesPanel-" +
                  (window.top.dsUserLogin
                    ? window.top.dsUserLogin
                    : "unknown") +
                  "-" +
                  new Date().getTime();
                (u.label = h),
                  (n.source = "3DSearch"),
                  (d.handler = t.createRequest(n, "POST", l, u));
              } else
                this.getMask(
                  { "ds6w:type": { valueDB: this._getModelType(a) } },
                  function (e) {
                    var i = [];
                    e.Write.attributes.forEach(function (e) {
                      var t = e.name;
                      i.push(t);
                    });
                    var s = this.getIdFromModel(a),
                      o = [];
                    a.get("indexSource")
                      ? o.push(a.get("indexSource").toLowerCase())
                      : a.get("sourceName") &&
                        o.push(a.get("sourceName").toLowerCase());
                    var r = {
                      with_indexing_date: !1,
                      with_synthesis: !0,
                      with_synthesis_attribute: !0,
                      with_nls: !1,
                      locale: c,
                      query: s,
                      tenant: t.getTenantFromModel(a),
                      source: o,
                      select_predicate: i,
                      start: 0,
                      select_file: ["icon", "thumbnail_2d"],
                    };
                    i && (n.select_predicates = i),
                      n.getPLMParameter &&
                        ((r.source = ["3dspace"]),
                        (r.specific_source_parameter = {
                          "3dspace": { plmparam: { retrieve_plmparam: !0 } },
                        }));
                    var u =
                      "PropertiesPanel-" +
                      (window.top.dsUserLogin
                        ? window.top.dsUserLogin
                        : "unknown") +
                      "-" +
                      new Date().getTime();
                    (r.label = u),
                      (n.source = "3DSearch"),
                      (d.handler = t.createRequest(n, "POST", l, r));
                  }
                );
              return d;
            }
          }
        },
        getElementsNls: function (t, i) {
          t && t.length > 0
            ? o.getResourcesInfo(t.join(","), {
                tenantId: i.tenant,
                lang: e.getLanguage(),
                apiVersion: "R2018x",
                onComplete: function (e) {
                  i.onComplete.call(this, e);
                },
                onFailure: function (e) {
                  console.log(" --\x3e> Can't get ElementsNls " + e),
                    i.onComplete.call(this, { success: !1 });
                },
              })
            : i.onComplete.call(this, null);
        },
        getValuesNls: function (t, i) {
          if (t) {
            var n = {};
            for (var s in t)
              Array.isArray(t[s]) &&
                ((n[s] = []),
                t[s].forEach(function (e) {
                  n[s].push({ value: t[s][0], source: [i.service] });
                }));
            o.getNlsOfPropertiesValuesBySource(n, {
              tenantId: i.tenant,
              serviceName: i.service,
              lang: e.getLanguage(),
              apiVersion: "R2019x",
              onComplete: function (e) {
                i.onComplete.call(this, e);
              },
              onFailure: function (e) {
                console.log(" --\x3e> Can't get ValuesNls " + e),
                  i.onComplete.call(this, e);
              },
            }),
              o.getPropertiesForClass(JSON.stringify(t), {
                tenantId: i.tenant,
                serviceName: i.service,
                lang: e.getLanguage(),
                apiVersion: "R2019x",
                onComplete: function (e) {
                  i.onComplete.call(this, e);
                },
                onFailure: function (e) {
                  console.log(" --\x3e> Can't get ValuesNls " + e),
                    i.onComplete.call(this, e);
                },
              });
          } else i.onComplete.call(this, null);
        },
        _callMask: function (e, t, i) {
          var n = this;
          u[e]
            ? i.call(n, u[e])
            : require([t], function (t) {
                (t = JSON.parse(t))[e] ? (u[e] = t[e]) : (u[e] = t),
                  i.call(n, u[e]);
              }, function (t) {
                require([
                  "text!DS/EditPropWidget/assets/masks/Federated/default.json",
                ], function (t) {
                  (t = JSON.parse(t))[e] ? (u[e] = t[e]) : (u[e] = t),
                    i.call(n, u[e]);
                });
              });
        },
        getMask: function (e, t) {
          var i = e["ds6w:type"];
          if (i && "pno:Person" === i.valueDB)
            this._callMask(
              "Person",
              "text!DS/EditPropWidget/assets/masks/3DSwym/Person.json",
              t
            );
          else {
            var n = i.valueDB;
            if (null != n && "" != n) {
              var s = n.split(":")[1];
              if (null != s && null != s && "" != s) {
                var a =
                  "text!DS/EditPropWidget/assets/masks/Federated/" +
                  (s + ".json");
                this._callMask(i && i.valueDB, a, t);
              } else
                this._callMask(
                  "Default",
                  "text!DS/EditPropWidget/assets/masks/Federated/default.json",
                  t
                );
            } else
              this._callMask(
                "Default",
                "text!DS/EditPropWidget/assets/masks/Federated/default.json",
                t
              );
          }
        },
        _onCompleteFederated: function (i, n) {
          var s = this,
            a = i.results,
            o = { source: "Federated" },
            r = [],
            u = {},
            p = {};
          if (((o.data = r), a && a.length > 0)) {
            var m = {},
              f = {},
              g = {},
              v = {},
              _ = null,
              E = null,
              C = a[0],
              S = {};
            for (var y in (C.attributes.forEach(function (t) {
              var i = t.name,
                a = i,
                r = t.value;
              r = void 0 === r ? "" : r;
              var l = "ds6w_facet" === t.format,
                C = l;
              if (l) {
                var y = i.split("/");
                a = y[y.length - 1];
              }
              if ((l = l || "attribute" === t.format)) {
                (_ = !0 === C ? v : f), (E = !0 === C ? g : m);
                var A = t.type,
                  I = !1;
                if ("date" === A) {
                  var b = Date.parse(r),
                    P = new Date(b);
                  P &&
                    ((r = (r = e.convertDateToStandard(P, !0)).valueDB),
                    ("ds6w:modified" !== a && "ds6w:created" !== a) ||
                      (A = "date"));
                } else
                  "double" === A.toLowerCase()
                    ? (A = "real")
                    : (I = "STRING" === A.toUpperCase());
                0 === r.indexOf(a) && (r = r.replace(a + ".", ""));
                var T = void 0 !== t.dispValue,
                  D = T ? t.dispValue : r;
                if (I && !T) {
                  var M = d[a];
                  D = r;
                  var w = M ? M[r] : null;
                  if (w) D = w;
                  else {
                    var O = S[a];
                    O || ((O = []), (S[a] = O)), O.push(r);
                  }
                }
                var F = {
                  name: a,
                  selectable: a,
                  path: i,
                  type: A,
                  value: D,
                  valueDB: r,
                  readOnly: !0,
                  nls: a,
                };
                "ds6w:label" === a && (u = F);
                var x = c[a];
                x ? ((F.nls = x.nlsName), h(_, a, F)) : h(E, a, F);
              } else "resourceid" === a ? (o.physicalID = r) : t.format.startsWith("PLMPARAM") || (o[i] = r);
              n.getPLMParameter && (p = s.parsePLMParamater(t, p));
            }),
            Object.keys(p).length > 0 &&
              (o.PLMParametersAvailableForIndexMode = !0),
            g))
              m[y] || (m[y] = g[y]);
            for (var y in v) f[y] || (f[y] = v[y]);
            n.idModel && (o.physicalID = n.idModel),
              (o.computed = { label: u });
            var A = Object.keys(m);
            if (C.attributes.length > 0 && n.select_predicates)
              for (var I = 0; I < n.select_predicates.length; I++)
                A.includes(n.select_predicates[I]) ||
                  A.push(n.select_predicates[I]);
            var b = new Promise(function (i) {
                e.isReplayedODT() &&
                  (s.nlsNameApiCalledCounter || (s.nlsNameApiCalledCounter = 0),
                  void 0 !== s.nlsNameApiCalledCounter &&
                    s.nlsNameApiCalledCounter++),
                  s.getElementsNls(A, {
                    tenant: t.getTenantFromModel(n.model),
                    onComplete: function (e) {
                      i(e);
                    },
                    apiCalled: s.nlsNameApiCalledCounter,
                  });
              }),
              P = new Promise(function (i) {
                e.isReplayedODT() &&
                  (s.nlsValueApiCalledCounter ||
                    (s.nlsValueApiCalledCounter = 0),
                  void 0 !== s.nlsValueApiCalledCounter &&
                    s.nlsValueApiCalledCounter++);
                var a = n.model.get("source") ? n.model.get("source") : "";
                a &&
                  s.getValuesNls(S, {
                    tenant: t.getTenantFromModel(n.model),
                    service: a,
                    onComplete: function (e) {
                      i(e);
                    },
                    apiCalled: s.nlsValueApiCalledCounter,
                  });
              });
            Promise.all([b, P]).then(function (e) {
              var i = e[0],
                s = e[1];
              if (i)
                if (!1 !== i.success) {
                  var a = i || {};
                  a.vocabularyElementNLSInfo &&
                    a.vocabularyElementNLSInfo.forEach(function (e) {
                      var t = m[e.uri];
                      "Predicate" === e.type ? (c[e.uri] = e) : (d[e.uri] = e),
                        t && e.nlsName && ((t.nls = e.nlsName), (f[e.uri] = t));
                    });
                } else t._raiseError(i.error ? i.error.message : "Error", n);
              for (var u in s) {
                var h = f[u];
                if (h) {
                  var g = s[u];
                  if (h.multivalue) {
                    var v = [];
                    h.value.forEach(function (e) {
                      var t = g[e];
                      v.push(t || e);
                    }),
                      (h.value = v);
                  } else h.value = g[h.value] ? g[h.value] : h.value;
                }
              }
              n.select_predicates.forEach(function (e) {
                var t = f[e];
                if (t) r.push(t);
                else {
                  var i = d[e];
                  if ((i || (i = c[e]), i && i.nlsName && i.uri)) {
                    var n = {
                      name: i.uri,
                      nls: i.nlsName,
                      path: i.uri,
                      readOnly: !0,
                      selectable: i.uri,
                      type: "STRING",
                      value: "",
                      valueDB: "",
                    };
                    r.push(n);
                  }
                }
              });
              for (var _ = Object.keys(p), E = [], C = 0; C < _.length; C++)
                (p[_[C]].groupNLS = l.get("accordion_parameters")),
                  (p[_[C]].group = "PLMParametersGroup"),
                  (p[_[C]].groupWeight = 0),
                  E.push(p[_[C]]);
              (o.groupData = E), n.onComplete.call(null, { results: [o] });
            });
          } else t._raiseError(l.get("FedAjax_NotFound"), n);
        },
        getListOfExtensions: function (e) {
          var i = e.modelList[0],
            n = i.get("objectId"),
            s = "",
            a = {};
          if ((i.get("i3dxId") && (n = i.get("i3dxId")), n)) {
            s = "/search";
            let c = [],
              d = i.get("source");
            if (d) {
              let e = r[d.toUpperCase()];
              e && e.keyOnFedSearch && c.push(e.keyOnFedSearch);
            }
            (a.model = i),
              (a.objectId = n),
              (a.onCompleteWS = e.onComplete),
              (a.onComplete = function (t) {
                var i =
                  "Exception in '/search' ws when looking for List of Extensions";
                t &&
                  t.error &&
                  t.error.Message &&
                  t.error.MessageException &&
                  (i += ` -> error Message = ${t.error.Message} MessageException = ${t.error.MessageException}`),
                  console.error(i),
                  e.onComplete.call([], e);
              });
            var o = {
                with_indexing_date: !1,
                with_synthesis: !0,
                with_synthesis_attribute: !0,
                with_nls: !1,
                locale: e.lang,
                query: this.getQueryForIndexService(i),
                tenant: e.tenantId,
                source: c,
                select_predicate: e.select_predicates,
                start: 0,
                select_file: ["icon", "thumbnail_2d"],
              },
              l =
                "PropertiesPanel-" +
                (window.top.dsUserLogin ? window.top.dsUserLogin : "unknown") +
                "-" +
                new Date().getTime();
            (o.label = l),
              (a.source = "3DSearch"),
              t.createRequest(a, "POST", s, o);
          }
        },
        parsePLMParamater: function (e, t) {
          return (
            e.format.startsWith("PLMPARAM") &&
              (t[e.format] || (t[e.format] = {}),
              e.name && !t[e.format].name && (t[e.format].name = e.format),
              e.name && !t[e.format].nls && (t[e.format].nls = e.name),
              e.name &&
                !t[e.format].selectable &&
                (t[e.format].selectable = e.format),
              e.name && !t[e.format].name && (t[e.format].name = e.name),
              "unit" === e.type && (t[e.format].dimension = e.value),
              "minRangeProp" === e.type &&
                ("none" !== e.value
                  ? (t[e.format].minval = e.value)
                  : (t[e.format].minval = "")),
              "maxRangeProp" === e.type &&
                ("none" !== e.value
                  ? (t[e.format].maxval = e.value)
                  : (t[e.format].maxval = "")),
              "value" === e.type && (t[e.format].value = e.value),
              t[e.format].value || (t[e.format].value = ""),
              t[e.format].type || (t[e.format].type = "string"),
              t[e.format].readOnly || (t[e.format].readOnly = !0)),
            t
          );
        },
      };
    }
  ),
  define(
    "DS/EditPropWidget/ajaxRequests/3dlifecycleAjaxRequests",
    [
      "DS/WidgetServices/WidgetServices",
      "DS/EditPropWidget/ajaxRequests/ServicesAjaxRequests",
      "DS/EditPropWidget/ajaxRequests/FederatedAjaxRequests",
      "DS/EditPropWidget/constants/EditPropConstants",
      "DS/WAFData/WAFData",
    ],
    function (e, t, i, n, s) {
      "use strict";
      return {
        checkFacetsVisibility: function (e) {
          var t = {};
          if (null != e.facets && e.facets.length > 0)
            for (var i = 0; i < e.facets.length; i++) {
              var s = e.facets[i].name;
              "properties" === s
                ? (t[n.FACET_PROPERTIES] = !0)
                : "social" === s
                ? (t[n.FACET_SWYMCOMMENTS] = !0)
                : (t[n.FACET_PROPERTIES] = !0);
            }
          else t[n.FACET_PROPERTIES] = !0;
          e.onComplete.call(this, { id: e.model.get("objectId"), facets: t });
        },
        getListOfAttributes: function (i) {
          var n = i.modelList;
          i.navigateToMain;
          i.securityContext = "ctx::Kanban User.Company Name.Kanban_PRJ";
          e.getWidgetValue("index_mode");
          if (n.length) {
            var s = t.getTenantFromModel(r),
              a =
                "/resources/v1/modeler/kanban/boards/" +
                (r = n[0]).get("objectId") +
                "?tenant=" +
                s +
                "&mask=properties",
              o = e.isDebug(),
              r =
                (e.getWidgetValue("debug_properties"),
                n.length > 0 ? n[0] : null);
            (i.model = r),
              (i.withSC = !1),
              (i.withChange = !1),
              (i.onCompleteWS = t._onCompleteAttributes),
              t.createRequest(i, "GET", a, null);
          }
        },
        commitModels: function (e) {
          var i = e.modelList;
          e.securityContext = "ctx::Kanban User.Company Name.Kanban_PRJ";
          var n = t.getTenantFromModel(r),
            s =
              "/resources/v1/modeler/kanban/boards/" +
              (r = i[0]).get("objectId") +
              "?tenant=" +
              n,
            a = [],
            o = !1;
          i.forEach(function (e, i) {
            var n = t._createJSONModifiedAttribute(e);
            (o = o || n.requests.length > 0), (a = a.concat(n.requests));
            var s = n.plmParametersRequests;
            s.length > 0 && t._concatRequestListComposite(a, s);
          });
          var r = i.length > 0 ? i[0] : null,
            l = { requests: a };
          o &&
            null != r &&
            r.isMergedModel() &&
            (l.transactionid = r.get("objectId")),
            (e.model = r),
            (e.onCompleteWS = t._onCompleteCommit),
            t.createRequest(e, "PATCH", s, l);
        },
      };
    }
  ),
  define(
    "DS/EditPropWidget/models/EditPropHistoryModel",
    ["DS/EditPropWidget/models/HistoryObjectModel"],
    function (e) {
      "use strict";
      return function (t) {
        for (var i in ((this.user = ""),
        (this.date = ""),
        (this.event = ""),
        (this.eventNls = ""),
        (this.objects = []),
        (this._generationObjectList = function () {
          if (this.objects && this.objects.length > 0) {
            var t = [];
            this.objects.forEach(function (i) {
              var n = new e(i);
              t.push(n);
            }),
              (this.objects = t);
          }
        }),
        t))
          this[i] = t[i];
        (this.set = function (e, t) {
          void 0 !== t && (this[e] = t),
            "objects" === e && this._generationObjectList();
        }),
          (this.get = function (e) {
            return this[e];
          }),
          this._generationObjectList();
      };
    }
  ),
  define(
    "DS/EditPropWidget/ICard/IdCard",
    [
      "UWA/Controls/Abstract",
      "DS/Controls/Button",
      "DS/Controls/TooltipModel",
      "DS/UIKIT/Mask",
      "DS/WidgetServicesUI/helpers/Helpers",
      "DS/WidgetServicesUI/helpers/Constants",
      "DS/WidgetServices/WidgetServices",
      "DS/EditPropWidget/facets/Properties/TweakerManager",
      "DS/EditPropWidget/constants/EditPropConstants",
      "DS/EditPropWidget/models/EditPropAttributeModel",
      "DS/EditPropWidget/buttonsBar/ButtonsBar",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s, a, o, r, l, c, d, u) {
      return e.extend({
        init: function (e) {
          this._parent.apply(this, arguments),
            (this._showThumbnailsCondition = {}),
            (this._currentModel = null),
            (this._titleAttr = null),
            (this.elements.container = UWA.createElement("div", {
              class: "idCardContainer",
            })),
            (this.elements.thumbnail = UWA.createElement("div", {
              class: "thumbnail",
            })),
            (this.elements.attrContainer = UWA.createElement("div", {
              class: "attributes",
            })),
            this.elements.thumbnail.inject(this.elements.container),
            this.elements.attrContainer.inject(this.elements.container),
            (this.elements.headerContainer = UWA.createElement("div", {
              class: "header-section",
            })),
            (this.elements.titleContainer = UWA.createElement("div", {
              class: "title-section",
            })),
            (this.elements.title = UWA.createElement("div", {
              class: "title",
            })),
            (this.elements.revision = UWA.createElement("div", {
              class: "revision",
            })),
            (this.elements.multititles = UWA.createElement("div", {
              class: "multititles",
            })),
            this.elements.title.inject(this.elements.titleContainer),
            this.elements.revision.inject(this.elements.titleContainer),
            this.elements.multititles.inject(this.elements.titleContainer),
            (this.elements.actions = new d(
              { classname: "actions-section", minimumSize: 200 },
              this.elements.titleContainer
            )),
            this.elements.actions.inject(this.elements.headerContainer),
            this.elements.titleContainer.inject(this.elements.headerContainer),
            (this.elements.detailsContainer = UWA.createElement("div", {
              class: "details-section",
            })),
            this.elements.headerContainer.inject(this.elements.attrContainer),
            this.elements.detailsContainer.inject(this.elements.attrContainer),
            (this.elements.owner = UWA.createElement("span", {
              class: "owner",
            })),
            (this.elements.date = UWA.createElement("span", {
              class: "lastModified text-muted",
            })),
            this.elements.owner.inject(this.elements.detailsContainer),
            this.elements.date.inject(this.elements.detailsContainer),
            (this.tweakerManager = new r({}, { readOnly: !1 })),
            (this._handlerThumbnailClick = function () {
              this.dispatchEvent("thumbnailClick");
            }.bind(this));
          var t = this,
            i = [
              { icon: "expand-up", text: u.get("hideIDCard"), method: "show" },
              {
                icon: "expand-down",
                text: u.get("showIDCard"),
                method: "hide",
              },
            ],
            n = o.getWidgetValue(l.COLLAPSE_IC_PREFERENCE.name),
            s = 0 === n || 1 === n ? n : 0;
          (this.defaultAction = {
            name: "actionHide",
            text: i[s].text,
            icon: i[s].icon,
            handler: function () {
              var e = i[(s = s ? 0 : 1)];
              (t.defaultAction.text = i[s].text),
                (t.defaultAction.icon = i[s].icon),
                o.setWidgetValue(l.COLLAPSE_IC_PREFERENCE.name, s),
                t._updateFromExpandButton(e);
            },
          }),
            this.setActions(e.actions);
          e = i[s];
          this._updateFromExpandButton(e);
        },
        _updateFromExpandButton: function (e) {
          this.showThumbnail("collapse", "show" === e.method),
            this.elements.detailsContainer[e.method](),
            this.elements.multititles[e.method](),
            this.elements.actions.updateButton("actionHide", e);
        },
        setActions: function (e) {
          this.elements.actions.setButtons(e),
            this.elements.actions.addButton(this.defaultAction);
        },
        fillFromModel: function (e) {
          this.clearValues();
          var t = e.getAllAttributes();
          if (t)
            for (var i in t) {
              var n = t[i];
              n instanceof c && this.setValueForSection(e, n);
            }
          if (!e.get("labelAttribute")) {
            var s = e.get(l.MODEL_DS_ATTR + "label");
            s && this.setValueForSection(e, s);
          }
        },
        clearValues: function () {
          this.elements.container &&
            (this.elements.owner.empty(),
            this.elements.date.empty(),
            this.elements.title.empty(),
            this.elements.multititles.empty(),
            this.elements.revision.empty());
        },
        onResize: function () {
          this._manageThumbnailDisplay(),
            this.elements.actions.onResize(),
            this._computeTitle();
        },
        maskThumbnail: function () {
          var e = this.getThumbnailElement();
          e && n.mask(e);
        },
        unmaskThumbnail: function () {
          var e = this.getThumbnailElement();
          e && n.unmask(e);
        },
        changeThumbnail: function (e, t) {
          this.elements.thumbnail.setStyle(
            "background-image",
            "url(" + e + ")"
          );
          var i = this.getThumbnailElement();
          i &&
            (this.showThumbnail("url", !!e),
            !0 === t && i.addClassName("clickable"),
            i.removeEvent("click", this._handlerThumbnailClick),
            i.addEvent("click", this._handlerThumbnailClick));
        },
        showThumbnail: function (e, t) {
          this._showThumbnailsCondition[e] !== t &&
            ((this._showThumbnailsCondition[e] = t),
            this.__manageThumbnailDisplay());
        },
        _manageThumbnailDisplay: function () {
          var e = this.elements.container.getSize();
          (showThumbnails = e.width >= 350),
            this.showThumbnail("width", showThumbnails);
        },
        __manageThumbnailDisplay: function () {
          var e = !0;
          for (var t in this._showThumbnailsCondition)
            if (!(e = this._showThumbnailsCondition[t])) break;
          e
            ? this.elements.container.removeClassName("noThumbnail")
            : this.elements.container.addClassName("noThumbnail");
        },
        getThumbnailElement: function () {
          return this.elements.thumbnail;
        },
        injectValue: function (e, t, i, n) {
          this.elements[i] && this._injectValue(e, t, this.elements[i], n);
        },
        _injectValue: function (e, t, i, n) {
          var a = { readOnly: !0, value: e, postBuild: !1 };
          if (n) for (var o in n) a[o] = n[o];
          i.empty(),
            s.getTweaker(t, a, function (e) {
              i.empty(), e.inject(i), n && n.callback && n.callback();
            });
        },
        addRevision: function () {
          if (this.elements.title) {
            var e =
              this.elements.title.getElementsByClassName("tw_ROValue_content");
            e && e.length > 0 && this.elements.revision.inject(e[0]);
          }
        },
        _computeTitle: function (e) {
          if (this._currentModel) {
            var t = this,
              i = this._currentModel.getComputedTitle(),
              n = e;
            e || (n = this._currentModel.get(this._titleAttr)),
              n &&
                ((this._titleAttr = n.getIdentifier()),
                i || (i = n.get("value")));
            var s = !!(this.elements.container.getSize().width > 380);
            s
              ? this.elements.container.removeClassName("smallSize")
              : this.elements.container.addClassName("smallSize");
            var o = this.elements.attrContainer.getSize().width / 4,
              r = i.length > o,
              l = s,
              c = s;
            this.injectValue(i, a.TYPES.STRING, "title", {
              postBuild: r,
              maxCharacters: o,
              multiline: l,
              forHTML: c,
              callback: function () {
                t.addRevision();
              },
            });
          }
        },
        setValueForSection: function (e, t) {
          var i = t.get("sectionIdentityCard"),
            n = !!i;
          if (n) {
            this._currentModel = e;
            var s = "title" === i;
            if (
              (s &&
                (this._computeTitle(t),
                !0 === e.isMergedModel()
                  ? this.elements.title.addClassName("EP_MergedTitle")
                  : this.elements.title.removeClassName("EP_MergedTitle")),
              !0 !== e.isMergedModel())
            ) {
              if (s) {
                var r = e.get("revision");
                r &&
                  this.injectValue(
                    " " + r.get("value"),
                    a.TYPES.STRING,
                    "revision"
                  );
              } else if ("ownerName" === i)
                this.injectValue(t.get("value") + " ", a.TYPES.STRING, "owner");
              else if ("date" === i) {
                var l = t.get("value");
                (l = l || t.get("valueDB")),
                  (computedDate = o.formatDateObj(l)),
                  this._injectValue(
                    computedDate,
                    a.TYPES.DATETIME,
                    this.elements.date
                  );
              }
            } else
              this.injectValue(t.get("value"), a.TYPES.STRING, "multititles", {
                multisel: !0,
              });
          }
          return n;
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/ajaxRequests/3DSpaceAjaxRequests",
    [
      "DS/WidgetServices/WidgetServices",
      "DS/EditPropWidget/ajaxRequests/ServicesAjaxRequests",
      "DS/EditPropWidget/ajaxRequests/FederatedAjaxRequests",
      "DS/EditPropWidget/constants/EditPropConstants",
      "DS/WAFData/WAFData",
      "DS/ENOCollabToolsUI/tracker/ENOCollabTrackerBackEnd",
    ],
    function (e, t, i, n, s, a) {
      "use strict";
      return {
        commitModels: function (e) {
          var i = e.modelList,
            n = (e.securityContext, []),
            s = !1;
          i.forEach(function (e, i) {
            var a = t._createJSONModifiedAttribute(e);
            (s = s || a.requests.length > 0), (n = n.concat(a.requests));
            var o = a.plmParametersRequests;
            o.length > 0 && t._concatRequestListComposite(n, o);
          });
          var o = i.length > 0 ? i[0] : null,
            r = { requests: n };
          s &&
            null != o &&
            o.isMergedModel() &&
            (r.transactionid = o.get("objectId")),
            a.enrichRequestWithMetrics(r, { UXName: "Properties" }),
            (e.model = o),
            (e.onCompleteWS = t._onCompleteCommit),
            t.createRequest(
              e,
              "PUT",
              "/resources/v1/collabServices/attributes/op/update?propertiesFacet=true",
              r
            );
        },
        /** add by chenyan  新增获取必填属性提示 **/
        getNotWriteAttrByPart : function (e) {
              t.createRequest(
                  e,
                  "POST",
                  "/TWXPublicRest/TWXTicketService?JPOName=JF_ECRRESTService&FuncName=getNotWriteAttrByPart",
                  {
                    "partIdList":e.partIdList,
                    "ignoreXENUnusedAttr":!0 === e.ignoreXENUnusedAttr
                  }
              );
        },
        getPreviewUrl: function (e) {
          var t = e.modelList,
            n = (t.length, []),
            s = "";
          t.forEach(function (e) {
            var t = e.get("objectId");
            (s = s || e.get("tenant")),
              t && n.push("physicalid:" + e.get("objectId"));
          }),
            n.length > 0 && ((e.ids = n), (e.tenant = s), i.getPreviewUrl(e));
        },
        getHistory: function (e) {
          t.securityContextDispatcher(e, this._getHistory);
        },
        _getHistory: function (i) {
          var n = i.modelList,
            a = i.nbResult,
            o = i.lastDate,
            r = i.securityContext;
          i.handleRequest || (i.handleRequest = {}), (a = a || 20);
          if (n.length) {
            var l = "",
              c = "";
            n.forEach(function (e) {
              var t = e.get("objectId");
              (l += c + "id=" + t), (c = "&");
            });
            var d = n.length > 0 ? n[0] : null;
            i.handleRequest.handle = e.getSourceUrlAsync({
              platformId: t.getTenantFromModel(d),
              source: t.getSourceFromModel(d),
              onComplete: function (c) {
                var d = "",
                  u = "";
                (d = "passport"),
                  (u =
                    c + "/resources/v1/collabServices/attributes/op/history"),
                  (u += "?" + l),
                  (u += "&nbResults=" + a),
                  o && (u += "&lastDate=" + o);
                var h = {
                  Accept: "application/json",
                  "content-type": "application/json",
                };
                (h["Accept-Language"] = e.getLanguage()),
                  r && (h.SecurityContext = encodeURIComponent(r));
                var p = {
                  type: "json",
                  method: "GET",
                  timeout: 36e5,
                  proxy: d,
                  headers: h,
                  cache: -1,
                  onComplete: function (e) {
                    t._onCompleteAttributes(e, i);
                  },
                  onFailure: function (e, n, s) {
                    t._onFailure(e, n, s, i);
                  },
                  onTimeout: function (e) {
                    t._onTimeout(i);
                  },
                };
                e.getWidgetValue("isStandalone") ||
                  (p.authentication = "passport");
                var m = n.length > 0 ? n[0] : null;
                i.handleRequest.handle = s.handleRequest(t._formatUrl(m, u), p);
              },
            });
          }
        },
        checkFacetsVisibility: function (e) {
          t.securityContextDispatcher(e, this._checkFacetsVisibility);
        },
        _checkFacetsVisibility: function (i) {
          var n = i.model,
            a = n.get("objectId"),
            o = i.facets,
            r = i.sourceAppId,
            l = i.securityContext;
          i.handleRequest || (i.handleRequest = {});
          var c = { id: a, facets: o, sourceAppId: r };
          n.get("instrefid") &&
            ((c.mains = {}), (c.mains[a] = { id: n.get("instrefid") })),
            n.isMergedModel() && (c.ids = n.getMergedModelIds());
          i.handleRequest.handle = e.getSourceUrlAsync({
            platformId: t.getTenantFromModel(n),
            source: t.getSourceFromModel(n),
            onComplete: function (a) {
              var o = "",
                r = "";
              (o = "passport"),
                (r =
                  a +
                  "/resources/v1/collabServices/attributes/op/facets/available");
              var d = {
                Accept: "application/json",
                "content-type": "application/json",
              };
              (d["Accept-Language"] = e.getLanguage()),
                l && (d.SecurityContext = encodeURIComponent(l));
              var u = {
                type: "json",
                method: "POST",
                timeout: 36e5,
                proxy: o,
                headers: d,
                data: UWA.Json.encode(c),
                onComplete: function (e) {
                  t._onCompleteAttributes(e, i);
                },
                onFailure: function (e, n, s) {
                  t._onFailure(e, n, s, i);
                },
                onTimeout: function (e) {
                  t._onTimeout(i);
                },
              };
              e.getWidgetValue("isStandalone") ||
                (u.authentication = "passport"),
                (i.handleRequest.handle = s.handleRequest(
                  t._formatUrl(n, r),
                  u
                ));
            },
          });
        },
        getListOfAttributes: function (i) {
          var s = i.modelList,
            a = !0 === i.navigateToMain ? "true" : "false",
            o = (i.securityContext, e.getWidgetValue("index_mode")),
            r = s.length,
            l = !1,
            c = !1;
          if (r) {
            for (
              var d = [],
                u = [],
                h = [],
                p = {},
                m = (T = s[0]).get("objectId"),
                f = 0;
              f < r;
              f++
            ) {
              var g = s[f],
                v = { a: g };
              for (var _ in ((c = c || g.isMergedModel()),
              g.isMergedModel() && (v = g.getMergingModels()),
              v)) {
                var E = v[_],
                  C = E.get("metatype"),
                  S = E.get("objectId"),
                  y = E.get("instrefid");
                (l = l || "EBOM" == C),
                  o || (C != n.METATYPE_BUS && "bus" != C)
                    ? o || C != n.METATYPE_REL
                      ? d.push(S)
                      : ("undefined" !== y &&
                          "" !== y &&
                          u.length === h.length &&
                          (h.push(y), (p[S] = { id: y })),
                        u.push(S))
                    : h.push(S);
              }
            }
            var A = "";
            (A = "/resources/v1/proto/attributes/op/read"),
              !1 !== l || o
                ? o &&
                  (A = "/resources/v1/collabServices/attributes/op/readIndex")
                : (A = "/resources/v1/collabServices/attributes/op/read");
            var I = e.isDebug(),
              b = e.getWidgetValue("debug_properties") || I ? "true" : null,
              P = null;
            o
              ? (P = {
                  lIds: d,
                  indexattributes: "true",
                  debug_properties: b || "",
                })
              : ((P = {
                  lIds: d,
                  relIDs: u,
                  busIDs: h,
                  plmparameters: i.plmParams ? "true" : "false",
                  attributes: !1 !== i.attributes ? "true" : "false",
                  navigateToMain: a,
                  readonly: !0 === i.readonly ? "true" : "false",
                  debug_properties: b || "",
                  ignoreKernelAccessChecks: i.ignoreKernelAccessChecks
                    ? "true"
                    : "false",
                }),
                Object.keys(p).length > 0 && (P.mains = p),
                c && (P.format = "merge"),
                c && (P.transactionid = m));
            var T = s.length > 0 ? s[0] : null;
            (i.model = T),
              (i.withSC = !0),
              (i.withChange = "silent"),
              (i.onCompleteWS = t._onCompleteAttributes),
              t.createRequest(i, "POST", A, P);
          }
        },
        getListOfExtensions: function (e) {
          var i = "";
          if (e.modelList.length) {
            var n = e.tenantId,
              s = { classes: [e.type] };
            i = `/resources/dictionary/class/interfaces?tenant=${n}&xrequestedwith=xmlhttprequest`;
            var a = {
              onCompleteWS: e.onComplete,
              onComplete: function (t) {
                var i =
                  "Exception in '/resources/dictionary/class/interfaces' ws";
                t &&
                  t.error &&
                  t.error.Message &&
                  t.error.MessageException &&
                  (i += ` -> error Message = ${t.error.Message} MessageException = ${t.error.MessageException}`),
                  console.error(i),
                  e.onComplete.call([], e);
              },
            };
            t.createRequest(a, "POST", i, s);
          }
        },
        getInstanceDetails: function (e) {
          t.securityContextDispatcher(e, this._getInstanceDetails);
        },
        _getInstanceDetails: function (e) {
          var i = e.securityContext;
          if (e.modelList && e.modelList.length > 0) {
            var n = e.modelList[0].get("objectId"),
              s = {};
            (s.input_physical_ids = [n]),
              (s.primitives = e.primitives),
              (s.patterns = e.patterns),
              (s.attributes = e.attributes),
              (s.version = 3);
            var a =
              "EP-" +
              (window.top.dsUserLogin ? window.top.dsUserLogin : "unknown") +
              "-" +
              new Date().getTime();
            s.label = a;
            var o = {
                onCompleteWS: e.onComplete,
                onComplete: function (e) {
                  var t = "Exception in '/cvservlet/navigate' ws";
                  e &&
                    e.error &&
                    e.error.Message &&
                    e.error.MessageException &&
                    (t += ` -> error Message = ${e.error.Message} MessageException = ${e.error.MessageException}`),
                    console.error(t);
                },
              },
              r = `/cvservlet/navigate?SecurityContext=${i}`;
            t.createRequest(o, "POST", r, s);
          }
        },
        fetchMask: function (e) {
          t.securityContextDispatcher(e, this._fetchMask);
        },
        _fetchMask: function (e) {
          var i = {
              type: e.type,
              name: e.name,
              indexMode: e.indexMode ? "true" : "false",
            },
            n = {
              onCompleteWS: e.onComplete,
              onComplete: function (e) {
                var t =
                  "Exception '/resources/collabCommon/settings/getmask' ws";
                e &&
                  e.error &&
                  e.error.Message &&
                  e.error.MessageException &&
                  (t += ` -> error Message = ${e.error.Message} MessageException = ${e.error.MessageException}`),
                  console.error(t);
              },
              securityContext: e.securityContext,
              type: e.type,
              name: e.name,
            };
          t.createRequest(
            n,
            "POST",
            "/resources/collabCommon/settings/getmask",
            i
          );
        },
        getListOfDimensions: function (e) {
          t.securityContextDispatcher(e, this._getListOfDimensions);
        },
        _getListOfDimensions: function (e) {
          var i = {
            onCompleteWS: e.onComplete,
            onComplete: function (e) {
              var t = "Exception in '/resources/dictionary/dimensions' ws";
              e &&
                e.error &&
                e.error.Message &&
                e.error.MessageException &&
                (t += ` -> error Message = ${e.error.Message} MessageException = ${e.error.MessageException}`),
                console.error(t);
            },
          };
          t.createRequest(i, "GET", "/resources/dictionary/dimensions");
        },
        getConvertedMKSValue: function (e) {
          t.securityContextDispatcher(e, this._getConvertedMKSValue);
        },
        _getConvertedMKSValue: function (e) {
          var i = { Data: e.data, tenantId: e.tenantId },
            n = {
              onCompleteWS: e.onComplete,
              onComplete: function (e) {
                var t =
                  "Exception in '/resources/dictionary/dimensions/convertValues' ws";
                e &&
                  e.error &&
                  e.error.Message &&
                  e.error.MessageException &&
                  (t += ` -> error Message = ${e.error.Message} MessageException = ${e.error.MessageException}`),
                  console.error(t);
              },
              securityContext: e.securityContext,
            };
          t.createRequest(
            n,
            "POST",
            "/resources/dictionary/dimensions/convertValues",
            i
          );
        },
      };
    }
  ),
  define(
    "DS/EditPropWidget/ajaxRequests/EditPropAjaxRequests",
    [
      "DS/EditPropWidget/ajaxRequests/3DSpaceAjaxRequests",
      "DS/EditPropWidget/ajaxRequests/FederatedAjaxRequests",
      "DS/EditPropWidget/constants/ConstantsMapping",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n) {
      "use strict";
      var s = { checkFacetsVisibility: "_getDefaultFacetsVisibility" },
        a = i.getSupportedServices(),
        o = a["3dspace"];
      o && (o.service = e);
      var r = a.fedsearch;
      return (
        o && (r.service = t),
        {
          _getAjaxService: function (e, t, s) {
            var a = null;
            i.getSupportedServices();
            if (s) a = s;
            else if (e && e.length > 0) {
              var o = e[0];
              a = i.getServiceForModel(o);
            }
            a
              ? a.service
                ? t.call(null, a, e)
                : require([a.require], function (i) {
                    (a.service = i), t.call(null, a, e);
                  })
              : t.call(null, { name: n.get("Unknown") }, e);
          },
          _onSourceUnavailable: function (e, t) {
            var i = {
              message: n.replace(n.get("sourceUnavailable"), {
                label: "SourceName",
                value: t,
              }),
              code: "SU",
            };
            e.onComplete.call(null, { success: !1, error: i });
          },
          _callMethodOnSource: function (e, t) {
            var i = this,
              n = e.modelList;
            !Array.isArray(n) && e.model && (n = [e.model]),
              this._getAjaxService(
                n,
                function (n, a) {
                  var o = n.service;
                  if (o && "function" == typeof o[t]) o[t].call(o, e);
                  else {
                    var r = !0;
                    a.forEach(function (e) {
                      r = r && e.get("isTransient");
                    });
                    var l = s[t];
                    r && l
                      ? i[l].call(i, e)
                      : i._onSourceUnavailable(e, n.name);
                  }
                },
                e.defService
              );
          },
          commitModels: function (e) {
            this._callMethodOnSource(e, "commitModels");
          },
          getNotWriteAttrByPart: function (e) {
            this._callMethodOnSource(e, "getNotWriteAttrByPart");
          },
          getPreviewUrl: function (e) {
            this._callMethodOnSource(e, "getPreviewUrl");
          },
          getHistory: function (e) {
            this._callMethodOnSource(e, "getHistory");
          },
          checkFacetsVisibility: function (e) {
            this._callMethodOnSource(e, "checkFacetsVisibility");
          },
          getListOfAttributes: function (e) {
            this._callMethodOnSource(e, "getListOfAttributes");
          },
          _getDefaultFacetsVisibility: function (e) {
            var t = e.model.get("objectId");
            e.onComplete.call(null, { id: t, facets: { properties: !0 } });
          },
          getListOfExtensions: function (e) {
            this._callMethodOnSource(e, "getListOfExtensions");
          },
          getInstanceDetails: function (e) {
            this._callMethodOnSource(e, "getInstanceDetails");
          },
          fetchMask: function (e) {
            this._callMethodOnSource(e, "fetchMask");
          },
          getListOfDimensions: function (e) {
            this._callMethodOnSource(e, "getListOfDimensions");
          },
          getConvertedMKSValue: function (e) {
            this._callMethodOnSource(e, "getConvertedMKSValue");
          },
        }
      );
    }
  ),
  define(
    "DS/EditPropWidget/facets/Properties/EditPropBase",
    [
      "UWA/Class",
      "DS/UIKIT/Tooltip",
      "DS/WidgetServicesUI/helpers/Constants",
      "DS/EditPropWidget/facets/Properties/TweakerManager",
    ],
    function (e, t, i, n) {
      "use strict";
      return e.extend({
        lTooltip: [],
        elements: {},
        _sharedOptions: null,
        init: function (e, t) {
          var i = this;
          (this._sharedOptions = t || {}),
            this._parent(e),
            (this.tweakerManager = new n(
              {
                editable: !0 === e.editMode,
                onNotification: function (e) {
                  i._manageError(e);
                },
              },
              t
            ));
        },
        getSharedOptions: function () {
          return this._sharedOptions;
        },
        isReadOnly: function () {
          var e = this.getSharedOptions();
          return !e || !0 === e.readOnly;
        },
        switchTweakersStatus: function (e) {
          this.tweakerManager.switchMode(e);
        },
        _clearAllFields: function () {
          this.tweakerManager.clear();
        },
        _actionOnScroll: function () {
          this.tweakerManager.actionOnScroll();
        },
        _valueIsChanging: function (e, t) {},
        setError: function (e) {},
        _manageError: function (e, t) {},
      });
    }
  ),
  define(
    "DS/EditPropWidget/facets/Properties/PropertiesUIController",
    [
      "DS/EditPropWidget/controllers/EditPropUIController",
      "DS/EditPropWidget/utils/AttributesUtils",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i) {
      "use strict";
      return e.extend({
        defaultOptions: {},
        validateModels: function (e) {
          return t.validateModels(e);
        },
        validateAttributes: function (e, i) {
          return t.validateAttributes(e, i);
        },
        resetValueOfModels: function (e) {
          e || (e = this.getModels()),
            e.forEach(function (e) {
              e.resetInitialValues();
            });
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/utils/EditPropIndexUtils",
    [
      "DS/EditPropWidget/ajaxRequests/EditPropAjaxRequests",
      "DS/WidgetServices/WidgetServices",
      "DS/EditPropWidget/constants/ConstantsMapping",
      "DS/FedDictionaryAccess/FedDictionaryAccessAPI",
      "DS/EditPropWidget/constants/EditPropConstants",
    ],
    function (e, t, i, n, s) {
      "use strict";
      return {
        getListOfExtensions: function (n, a) {
          var o = this;
          if (n.type !== s.EDITPROPINDEX_DEFAULTTYPE) {
            var r = t.getCancelObject(),
              l = i.getServiceBySource("3dspace"),
              c = i.getServiceBySource("fedsearch"),
              d = n.type,
              u = n.model,
              h = t.getLanguage(),
              p = u.getTenant();
            (o.promiseForAutoExt = new Promise(function (t, i) {
              e.getListOfExtensions({
                type: d,
                tenantId: p,
                modelList: [u],
                lang: h,
                apiVersion: "R2018x",
                handleRequest: { handle: r },
                defService: l,
                onComplete: function (e, n) {
                  try {
                    var s = null;
                    e && (s = e), t(s);
                  } catch (e) {
                    i(e);
                  }
                },
              });
            })),
              (o.promiseForNonAutoAndCustoExt = new Promise(function (t) {
                e.getListOfExtensions({
                  type: d,
                  tenantId: p,
                  modelList: [u],
                  lang: h,
                  apiVersion: "R2018x",
                  handleRequest: { handle: r },
                  defService: c,
                  select_predicates: ["ds6w:kind"],
                  onComplete: function (e, i) {
                    var n = null;
                    e && (n = e), t(n);
                  },
                });
              })),
              Promise.all([
                o.promiseForAutoExt,
                o.promiseForNonAutoAndCustoExt,
              ]).then(function (e) {
                o.parseListOfExtensions(e, a, n);
              });
          } else a([], n);
        },
        parseListOfExtensions: function (e, t, i) {
          var n = e[0],
            s = e[1],
            a = [];
          n &&
            n.results &&
            n.results.length > 0 &&
            n.results[0] &&
            n.results[0].interfaces &&
            n.results[0].interfaces.map(function (e) {
              if (
                e.hasOwnProperty("automatic") &&
                "yes" === e.automatic.toLowerCase() &&
                e.name
              ) {
                var t = { isExt: !0, isAutomaticExt: !0, name: e.name };
                a.push(t);
              }
            }),
            s &&
              s.results &&
              s.results.length > 0 &&
              s.results[0] &&
              s.results[0].attributes &&
              s.results[0].attributes.map(function (e) {
                if (e.name.includes("ds6w:kind") && e.value) {
                  var t = { isExt: !0, isAutomaticExt: !1, name: e.value };
                  a.push(t);
                }
              }),
            t(a, i);
        },
        getMaskAttributes: function (n, s) {
          var a = this,
            o = new Set(),
            r = new Map(),
            l = 0,
            c = [],
            d = {},
            u = {},
            h = {};
          n.forEach(function (e) {
            e.name && r.set(e.name, e);
          });
          var p = function (e, t) {
              if (e.success) {
                var i = t.name;
                t.type;
                if (e.mask) {
                  var p = e.mask;
                  if (i && i.length > 0) {
                    var m = r.get(i);
                    m || "default" !== i || (m = { name: "default" }),
                      d[m.name] || (d[m.name] = { attributes: [] }),
                      m.isBus && (d[m.name].isType = !0),
                      m.isRel && (d[m.name].isType = !0),
                      "default" === m.name && (d[m.name].isType = !0),
                      m.isExt &&
                        ((d[m.name].isExtension = !0),
                        m.isAutomaticExt
                          ? (d[m.name].isAutomaticExt = !0)
                          : (d[m.name].isAutomaticExt = !1));
                  }
                  if (
                    (p &&
                      p.indexmode_write &&
                      p.indexmode_write.attributes &&
                      p.indexmode_write.attributes.forEach(function (e) {
                        (o.add(e.name),
                        i &&
                          i.length > 0 &&
                          (m && m.isExt
                            ? (u[e.name] = { extension: m.name, isExt: !0 })
                            : (u[e.name] = { isExt: !1 }),
                          m && !d[m.name] && (d[m.name] = { attributes: [] }),
                          m && d[m.name].attributes.push(e.name)),
                        e.group) &&
                          ((u[e.name].group = e.group),
                          void 0 !== h[e.group] &&
                          void 0 !== h[e.group].attributes
                            ? h[e.group].attributes.push(e.name)
                            : ((h[e.group] = { attributes: [] }),
                              h[e.group].attributes.push(e.name)));
                      }),
                    p && p.indexmode_groups)
                  )
                    for (var f in p.indexmode_groups)
                      h[f] &&
                        ((h[f].NLS = p.indexmode_groups[f].NLS
                          ? p.indexmode_groups[f].NLS
                          : f),
                        (h[f].weight = p.indexmode_groups[f].weight
                          ? p.indexmode_groups[f].weight
                          : 0));
                }
              }
              if ((l += 1) === n.length) {
                for (var g of o) c.push(g);
                a.getPredicates(n[0].name, function (e) {
                  var t = new Map();
                  e &&
                    Array.isArray(e) &&
                    e.length > 0 &&
                    e.forEach(function (e) {
                      c.includes(e.uri) && e.dimension && t.set(e.uri, e);
                    });
                  var i = {
                    maskAttributeDetails: u,
                    listOfPredicates: c,
                    maskGroupInfo: h,
                    groupAttributesInfoAsPerMask: d,
                  };
                  t.size > 0 && (i.dimensionAttributes = t), s(i);
                });
              }
              i &&
                "default" === i &&
                s({
                  maskAttributeDetails: u,
                  listOfPredicates: c,
                  maskGroupInfo: h,
                  groupAttributesInfoAsPerMask: d,
                });
            },
            m = i.getServiceBySource("3dspace");
          if (r.size > 0) {
            t.getCancelObject();
            for (var f of r.keys()) {
              var g = r.get(f),
                v = null;
              g.isBus
                ? (v = "bus")
                : g.isRel
                ? (v = "rel")
                : g.isExt && (v = "itf");
              var _ = {
                defService: m,
                name: f,
                type: v,
                indexMode: !0,
                onComplete: p,
              };
              e.fetchMask(_);
            }
          } else {
            _ = {
              defService: m,
              name: "default",
              type: "bus",
              indexMode: !0,
              onComplete: p,
            };
            e.fetchMask(_);
          }
        },
        getPredicates: function (e, i) {
          var s = {
            apiVersion: "R2018x",
            lang: t.getLanguage(),
            tenantId: t.getTenantID(),
            onComplete: function (e) {
              e &&
              e.classPredicates &&
              e.classPredicates.length > 0 &&
              Array.isArray(e.classPredicates) &&
              e.classPredicates[0] &&
              e.classPredicates[0] &&
              "object" == typeof e.classPredicates[0] &&
              e.classPredicates[0].vocabularyPredicateInfo
                ? i(e.classPredicates[0].vocabularyPredicateInfo)
                : i([]);
            },
            onFailure: function (e) {
              console.log(e), i([]);
            },
          };
          n.getPropertiesForClass(e, s);
        },
      };
    }
  ),
  define(
    "DS/EditPropWidget/models/OccurrenceModel",
    [
      "UWA/Class/Model",
      "DS/EditPropWidget/models/EditPropModel",
      "UWA/Class/Debug",
    ],
    function (e, t, i) {
      "use strict";
      return e.extend(i, {
        defaults: {
          isMerging: !1,
          modelPrefix: "00",
          secondaryOptionsForFacet: [],
        },
        isMergedOccurrence: function () {
          return !0 === this.get("isMerging");
        },
        getMergePrefixe: function () {
          return this.get("modelPrefix");
        },
        getAllModels: function (e, i) {
          var n = this._attributes,
            s = !0 === e ? [] : {};
          for (var a in n)
            if (
              n[a].model instanceof t &&
              (!0 === e ? s.push(n[a].model) : (s[a] = n[a].model), !0 === i)
            )
              break;
          return s;
        },
        _getModelElement: function (e) {
          var t = this.get(e);
          return t || (t = { model: null, options: null }), t;
        },
        setOptions: function (e, t) {
          var i = this._getModelElement();
          (i.options = t), this.set(e, i);
        },
        getOptions: function (e) {
          var t = this.get(e);
          return t ? t.options : null;
        },
        setModel: function (e, t) {
          var i = this._getModelElement();
          (i.model = t), this.set(e, i);
        },
        getModel: function (e) {
          var t = this.get(e);
          return t ? t.model : null;
        },
        setSecondaryOptionsForFacet: function (e) {
          this.set("secondaryOptionsForFacet", e);
        },
        getSecondaryOptionsForFacet: function () {
          return this.get("secondaryOptionsForFacet");
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/controllers/EditPropMainController",
    [
      "UWA/Class/Model",
      "DS/EditPropWidget/models/OccurrenceModel",
      "DS/EditPropWidget/models/EditPropModel",
      "DS/EditPropWidget/models/EditPropModelMerge",
      "DS/EditPropWidget/models/EditPropAttributeModel",
      "DS/EditPropWidget/utils/EditPropUtils",
      "DS/EditPropWidget/UI/EditPropSkeleton",
      "DS/EditPropWidget/constants/EditPropConstants",
      "DS/EditPropWidget/constants/ConstantsMapping",
      "DS/WidgetServices/WidgetServices",
      "DS/WidgetServices/securityContextServices/SecurityContextServices",
      "DS/ENOCollabToolsUI/tracker/ENOCollabTracker",
    ],
    function (e, t, i, n, s, a, o, r, l, c, d, u) {
      "use strict";
      return UWA.Class.extend({
        skeleton: null,
        defaultOptions: {
          selectionType: r.ALL_SELECTION,
          displayType: r.STANDARD,
          context: null,
          odtUpTransient: !1,
        },
        init: function (e) {
          var t = this;
          for (var i in (this._parent(e),
          (this._selectionIsStopped = !1),
          (this._currentNewSelection = null),
          (this.options = this.options ? this.options : {}),
          this.defaultOptions))
            this.options[i] = e[i];
          (this.indexModeParameterFromApp = !1),
            (this.indexModeParameter = !1),
            (this.USE_APP_SECURTIYCONTEXT =
              "true" === e.USE_APP_SECURTIYCONTEXT),
            null != e.indexModeParameter &&
              (this.setIndexModeFlag(e.indexModeParameter),
              this.setIndexModeFlagFromApp(e.indexModeParameter)),
            d.getInstance({ context: this.options.context });
          var n = e.events.onCancel;
          (e.events.onCancel = function (e) {
            t._unsubscribeSelect(),
              t.skeleton.clearPanel(),
              t.skeleton.setOccurrences([]),
              t.skeleton.notifyClose,
              t.skeleton.notifyClose(),
              n && n.call(t, e);
          }),
            this._setup(e),
            e.selectionType !== r.NO_SELECTION &&
              ((this._commandProxy = null),
              require([
                "DS/WidgetServices/commandProxy/CommandProxy",
              ], function (e) {
                var i = !1,
                  n = 0,
                  s = function (e, s) {
                    if (
                      (e.paths && 0 === e.paths.length) ||
                      (e.pathsInst && 0 === e.pathsInst.length)
                    )
                      (i = !1),
                        setTimeout(function () {
                          !1 === i && t._onSelectionChange(e, s);
                        }, 500);
                    else {
                      i = !0;
                      var a = ++n;
                      setTimeout(function () {
                        a === n && t._onSelectionChange(e, s);
                      }, 50);
                    }
                  },
                  a = { events: { onSelect: s } };
                (t.options.displayType !== r.TRANSIENT &&
                  !0 !== t.options.odtUpTransient) ||
                  (a.events.onUpdateTransient = function (e) {
                    e.models;
                    var i = e.options;
                    i &&
                      i.securityContext &&
                      (t.USE_APP_SECURTIYCONTEXT
                        ? d
                            .getInstance({
                              context: {
                                getSecurityContext: function () {
                                  return { SecurityContext: i.securityContext };
                                },
                              },
                            })
                            .getValidSecurityContext()
                        : d
                            .getInstance({
                              context: {
                                getSecurityContext: function () {
                                  return { SecurityContext: i.securityContext };
                                },
                              },
                            })
                            .getSecurityContext()),
                      t._commandProxy.subscribe({ onSelect: s });
                  }),
                  (t._commandProxy = e.create(a));
              }));
        },
        getTopBar: function () {
          return this.skeleton.getTopBar();
        },
        _unsubscribeSelect: function () {
          this._commandProxy &&
            this._commandProxy.unsubscribe(this._commandProxy.choices.SELECT);
        },
        setModels: function (e, t, i, n) {
          var s = this;
          null != i && "" !== i
            ? ((this.skeleton.defaultFacetName = i),
              this.skeleton.selectFacet(i))
            : (this.skeleton.defaultFacetName = null),
            null != n && "" !== n && (this.skeleton.infoForFacet = n);
          var a = !!this._currentNewSelection;
          (this._currentNewSelection = { models: e, change: t }),
            a ||
              this.skeleton.checkChangeAuthorized(function (e) {
                var t = s._currentNewSelection;
                (s._currentNewSelection = null),
                  !0 === e && s._setModels(t.models, t.change);
              }, !0);
        },
        _setModels: function (a, o) {
          window.top.performance.mark("start_setModelController");
          var l = (v = a && a.length > 0 ? a[0] : null)
            ? v instanceof e
              ? v.get("tenant")
              : v.tenant
            : null;
          if (
            (v && l && c.setWidgetValue("x3dPlatformId", l),
            "ENOLCMT_AP" === c.getWidgetValue("x3dAppId") &&
              void 0 !== o &&
              c.isChangeAvailable())
          ) {
            require(["DS/EditPropWidget/changeAction/passThruJS"], function (
              e
            ) {
              e.onReady(function (e) {
                a.length > 0 &&
                (e.changeIsSetChecker(o) || (o && o.configurationRevision))
                  ? e.set(o, function (t) {
                      void 0 === t.Message && e.show();
                    })
                  : (e.cleanAuthDetails(), e.hide());
              });
            });
          }
          var d = this,
            h = a.length,
            p = [];
          if (h) {
            var m = {},
              f = {},
              g = null;
            a.forEach(function (n, o) {
              if (
                "function" != typeof n.isMergedModel ||
                !0 !== n.isMergedModel()
              ) {
                var l = a[o],
                  c = null;
                n instanceof e
                  ? (d._getModel(l.get("objectId")) || l.clearChangedValue(),
                    (c = l.getChangedValues()))
                  : ((l = new i(n)), (a[o] = l)),
                  d._getModel(l.get("objectId")) ||
                    ((d.editPropModelList = [l]), l.clearChangedValue()),
                  l.keys().forEach(function (e) {
                    var t = l.get(e),
                      i = t instanceof s;
                    if (
                      (!i &&
                        t &&
                        "object" == typeof t &&
                        "EditProp_listOfChange" !== e &&
                        "label" !== e &&
                        ((t = new s(t)), (i = !0), l.set(e, t)),
                      i && !0 === l.isTransientModel())
                    ) {
                      var n = t.getIdentifier();
                      if (n) {
                        var a = f[n];
                        if (a) {
                          var o = t.get("value"),
                            r = t.get("valueDB"),
                            c = a.get("value"),
                            d = a.get("valueDB");
                          (c[l.get("objectId")] = o),
                            (d[l.get("objectId")] = r),
                            "title" === a.get("sectionIdentityCard") &&
                              ((g.value[l.get("objectId")] = o),
                              (g.valueDB[l.get("objectId")] = r)),
                            a.__nbValues++;
                        } else {
                          (o = (a = new s(UWA.clone(t))).get("value")),
                            (r = a.get("valueDB")),
                            (d = {});
                          ((c = {})[l.get("objectId")] = o),
                            (d[l.get("objectId")] = r),
                            a.set("multisel", !0),
                            a.set("value", c),
                            a.set("valueDB", d),
                            (a.__nbValues = 1),
                            "title" === a.get("sectionIdentityCard") &&
                              (((g = { value: {}, valueDB: {} }).value[
                                l.get("objectId")
                              ] = o),
                              (g.valueDB[l.get("objectId")] = r)),
                            (f[n] = a);
                        }
                      }
                    }
                  }),
                  l.clearChangedValue();
                var u = l.get("EditProp_listOfChange");
                u && (l.setChangedValues(u), l.unset("EditProp_listOfChange")),
                  c && l.setChangedValues(c);
                var h = l;
                if (!m[h.get("objectId")]) {
                  m[h.get("objectId")] = h;
                  var v = new t();
                  v.setModel(r.MODEL_INIT, h), p.push(v);
                }
              }
            });
            var v,
              _ = Object.keys(m).length;
            if (_ > 1)
              if (null != (v = p[0].getModel(r.MODEL_INIT))) {
                var E = UWA.clone(v.getOptions());
                for (var C in f) {
                  f[C].__nbValues === _ && (E[C] = f[C]);
                }
                null != g &&
                  (E[r.MODEL_DS_ATTR + "label"] = new s({
                    sectionIdentityCard: "title",
                    value: g.value,
                    valueDB: g.valueDB,
                    isTransient: !0,
                    multisel: !0,
                    path: r.MODEL_DS_ATTR + "label",
                  }));
                var S =
                    (S = c.getWidgetPreference("transactionId")) && S.value
                      ? S.value
                      : d._generateMergeID(),
                  y = new t({ modelPrefix: S, isMerging: !0 });
                E.objectId = S;
                var A = new n(E);
                A.setMergingModels(m),
                  y.setModel(r.MODEL_INIT, A),
                  p.unshift(y);
              } else console.log("First model not found in occurrence");
          }
          var I = {
              UXName: "Information",
              numberObjects: h,
              UXContext:
                this.options.displayType === r.TRANSIENT ||
                this.options.displayType === r.POPUP
                  ? "App Popup"
                  : "App Side Panel",
            },
            b = u.createInitActionEvent(I);
          u.checkAndTrackPageEvent(b),
            window.top.performance.mark("end_setModelController"),
            this.skeleton.setOccurrences(p);
        },
        _generateMergeID: function () {
          return c.generateUniqueId();
        },
        _getModel: function (e) {
          return this.skeleton.getModel(e);
        },
        getModels: function () {
          return this.skeleton.getModels();
        },
        getTitle: function () {
          return this.skeleton.getTitle();
        },
        getOptimalSize: function () {
          return this.skeleton.getOptimalSize();
        },
        getUI: function () {
          return this.skeleton;
        },
        onRefresh: function () {
          var e = this.getModels();
          this.setModels([], void 0), this.setModels(e, void 0);
        },
        _getFacet: function (e, t) {
          var i = t;
          if ("string" == typeof t) i = e[t];
          else if ("object" == typeof t && t.id) {
            var n = e[t.id];
            if (n) {
              for (var s in t) n[s] = t[s];
              i = n;
            }
          }
          return i;
        },
        addFacets: function (e) {
          if (e) {
            var t = this,
              i = [],
              n = this.mapFacet ? this.mapFacet : l.getMapping();
            e.forEach(function (e) {
              i.push(t._getFacet(n, e));
            });
            var s = this.getUI();
            s && s.addFacets(i);
          }
        },
        _setup: function (e) {
          var t = this,
            i = l.getMapping(e);
          t.mapFacet = i;
          var n = l.getDefaultFacets();
          e.facets && ((n = e.facets), delete e.facets);
          var s = [],
            a = !1;
          n.forEach(function (n) {
            var o = t._getFacet(i, n);
            o &&
              (!a && o.facet && o.facet.name === r.FACET_PROPERTIES && (a = !0),
              (!0 !== o.blockFromSearch ||
                !e ||
                ("searchInformationProperties" !== e.className &&
                  "searchPreviewProperties" !== e.className)) &&
                s.push(o));
          }),
            a ||
              ((i[r.FACET_PROPERTIES].visible = !1),
              s.push(i[r.FACET_PROPERTIES])),
            (this.skeleton = new o(s, e));
        },
        destroyComponent: function () {
          this._commandProxy &&
            (this._commandProxy.unsubscribe("onUpdateTransient"),
            this._commandProxy.unsubscribe("onSelect"));
        },
        onResize: function () {
          this.getUI().onResize();
        },
        _raiseNotif: function (e, t) {
          this.dispatchEvent("onNotification", { msg: e, eventID: t });
        },
        stopSelection: function (e) {
          this._selectionIsStopped = !0 === e;
        },
        _onSelectionChange: function (e, t) {
          if (
            this.options.selectionType === r.NO_SELECTION ||
            !0 === this._selectionIsStopped
          )
            return;
          if (
            t &&
            t.metadata &&
            t.metadata.originWidgetId === c.getWidgetId() &&
            "ENORIPE_Relations" === t.metadata.origin
          )
            return;
          let i = null;
          e.hasOwnProperty("securityContext") &&
            ((i = e.securityContext),
            d.getInstance({
              context: {
                getSecurityContext: function () {
                  return { SecurityContext: i };
                },
              },
            }));
          var n = e.pathsInst,
            s = !!n,
            o = (n = n && n.length > 0 ? n : e.paths) && n.length,
            l = e.defaultFacetName,
            u = e.infoForFacet,
            h = e.attributes;
          e.appid;
          let p = e.source,
            m = e.sourceName,
            f = e.indexModeParameter,
            g = e.pathsI3dx,
            v = n;
          !0 === f &&
            g &&
            Array.isArray(g) &&
            g.length > 0 &&
            void 0 === o &&
            ((o = g.length), (n = g)),
            this.makeDecisionForIndexMode(f);
          let _ = { indexModeParameter: this.getIndexModeFlag() };
          p && (_.source = p), m && (_.sourceName = m);
          var E = this.getModels(),
            C = [];
          E.forEach(function (e) {
            C.push(e.get("objectId"));
          });
          if (
            this.options.selectionType !== r.RESTRICTED_SELECTION ||
            (t.metadata && t.metadata.originWidgetId === c.getWidgetId())
          ) {
            for (var S = [], y = 0; y < o; y++) {
              var A = n[y];
              let t = void 0;
              g && g.length === o && (t = g[y]);
              let i = void 0;
              if ((v && v.length === o && (i = v[y]), A)) {
                if (f) I = a.getModelFromPathInIndexMode(i, t, h, _);
                else var I = a.getModelFromPath(A, h, s, _);
                !I.get("tenant") && e.tenant && I.set("tenant", e.tenant),
                  S.push(I);
              }
            }
            var b = null;
            if (
              (c.isChangeAvailable() && (b = UWA.clone(e.changeControl)),
              this.setModels(S, b || null, l, u),
              c.isReplayedODT())
            )
              return S;
          }
        },
        changeAttributes: function (e) {
          this.skeleton.changeAttributes(e);
        },
        setIndexModeFlag: function (e) {
          void 0 !== e && (this.indexModeParameter = e);
        },
        getIndexModeFlag: function () {
          return !0 === this.indexModeParameter;
        },
        setIndexModeFlagFromApp: function (e) {
          void 0 !== e && (this.indexModeParameterFromApp = e);
        },
        getIndexModeFlagFromApp: function () {
          return !0 === this.indexModeParameterFromApp;
        },
        makeDecisionForIndexMode: function (e) {
          let t = this;
          if (!0 === e || !1 === e)
            t.setIndexModeFlag(e), t.skeleton.setIndexModeFlag(e);
          else {
            let e = t.getIndexModeFlagFromApp();
            t.setIndexModeFlag(e), t.skeleton.setIndexModeFlag(e);
          }
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/EditPropWidget",
    [
      "UWA/Core",
      "UWA/Controls/Abstract",
      "DS/WidgetServices/WidgetServices",
      "DS/EditPropWidget/comm/EventManager",
      "DS/EditPropWidget/controllers/EditPropMainController",
    ],
    function (e, t, i, n, s) {
      "use strict";
      return t.extend({
        defaultOptions: {},
        _getController: function (e) {
          return this.controller ? this.controller : new s(e);
        },
        init: function (e, t) {
          t && i.setWidgetObject(t),
            (this.clickOnAnnotationData = e.clickOnAnnotationData);
          var s = {};
          for (var a in e) s[a] = e[a];
          (s.propertiesWidgetUI = !0),
            (null !== s.securityContextFromAppPreference &&
              void 0 !== s.securityContextFromAppPreference) ||
              (s.context &&
              "function" == typeof s.context.getSecurityContext &&
              s.context.getSecurityContext() &&
              s.context.getSecurityContext().SecurityContext
                ? (s.securityContextFromAppPreference =
                    s.context.getSecurityContext().SecurityContext)
                : (s.securityContextFromAppPreference = null)),
            (null !== s.disableAddExtensionButton &&
              void 0 !== s.disableAddExtensionButton) ||
              (s.disableAddExtensionButton = !1),
            s.sourceAppId || (s.sourceAppId = s.appId);
          var o = this;
          this._parent(s),
            this.elements || (this.elements = {}),
            s.events || (s.events = {}),
            (s.events.onInnerCancel = function () {
              o.destroyComponent();
            }),
            (this.controller = this._getController(s)),
            (this.elements.UI = this.controller.getUI()),
            (this.elements.container = this.elements.UI),
            this.elements.container
              .getContent()
              .addEvent("resize", function () {
                o.onInnerResize();
              }),
            (i.isReplayedODT() ||
              !0 === i.getWidgetObject().getValue("proto")) &&
              ((this._handlerSwitch = function (e) {
                this.setReadonlyStatus(e);
              }.bind(this)),
              n.subscribe("switchMode", this._handlerSwitch));
        },
        addAnnotationData: function (e) {
          var t = this,
            i = function (t) {
              "social" === t.facet.name &&
                setTimeout(function () {
                  var i = t.content._socialView;
                  if (i && e && e.annotation) {
                    var n = {
                      media: [
                        {
                          type: "annotation",
                          file: e.screenshot,
                          data: { annotationFile: e.annotation },
                        },
                      ],
                      replace: !1,
                    };
                    i.setActiveFieldContent(n);
                  }
                });
            };
          t.elements.UI._lFacets &&
            t.elements.UI._lFacets.forEach(function (e) {
              e &&
                e.facet &&
                e.facet.name &&
                "social" === e.facet.name &&
                (e.content
                  ? i(e)
                  : t.elements.UI.subscribeEvt("facetLoaded", i),
                t.elements.UI.selectFacet(e.facet.name));
            });
        },
        initDatas: function (e, t, i) {
          void 0 !== this.clickOnAnnotationData &&
            null != this.clickOnAnnotationData &&
            (e[0].clickOnAnnotationData = this.clickOnAnnotationData);
          let n = void 0;
          e &&
            e[0] &&
            "function" == typeof e[0].get &&
            e[0].get("indexModeParameter") &&
            (n = e[0].get("indexModeParameter")),
            void 0 === n &&
              "object" == typeof e[0] &&
              e[0].hasOwnProperty("indexModeParameter") &&
              (n = e[0].indexModeParameter),
            this.controller.makeDecisionForIndexMode(n);
          let s = this.controller.getIndexModeFlag();
          e &&
            e[0] &&
            n !== s &&
            ("function" == typeof e[0].set
              ? e[0].set("indexModeParameter", s)
              : (e[0].indexModeParameter = s)),
            this.controller.setModels(e, void 0, t, i);
        },
        initData: function (e, t, i) {
          this.initDatas(e, t, i);
        },
        changeAttributes: function (e) {
          this.controller.changeAttributes(e);
        },
        show: function (e) {
          this.elements.popup &&
            (this.elements.popup.show(), this.initDatas(e));
        },
        centerIt: function (e) {
          this.elements.popup && this.elements.popup.centerIt();
        },
        onRefresh: function () {
          this.controller.onRefresh();
        },
        onResize: function () {},
        onInnerResize: function () {
          this.elements.popup
            ? this.elements.popup.onResize()
            : this.elements.UI && this.elements.UI.onResize();
        },
        setReadonlyStatus: function (e) {
          this.elements.UI.setReadonlyStatus(e);
        },
        selectFacet: function (e) {
          this.elements.UI.selectFacet(e);
        },
        removeFacets: function (e) {
          this.elements.UI.removeFacets(e);
        },
        stopSelection: function (e) {
          this.controller.stopSelection(e);
        },
        addFacets: function (e) {
          this.controller.addFacets(e);
        },
        getTitle: function () {
          return this.elements.UI.getTitle();
        },
        getIdentityCardUI: function () {
          return this.elements.UI.getIdentityCardUI();
        },
        getMainTable: function (e) {
          return this.elements.UI.getMainTable();
        },
        getListModel: function () {
          return this.controller.getModels();
        },
        destroyComponent: function () {
          this._handlerSwitch && n.remove("switchMode", this._handlerSwitch),
            this.controller.destroyComponent(),
            this.elements.popup
              ? this.elements.popup.destroyComponent()
              : this.elements.UI.destroyComponent();
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/EditPropWidgetHTML",
    [
      "DS/EditPropWidget/EditPropWidget",
      "DS/WidgetServices/WidgetServices",
      "DS/WidgetServices/securityContextServices/SecurityContextServices",
      "DS/EditPropWidget/constants/EditPropConstants",
      "DS/EditPropWidget/models/EditPropModel",
    ],
    function (e, t, i, n, s) {
      window.top.performance.mark("end_EP_HTMLProperties"),
        window.top.performance.mark("start_widgetLoading"),
        window.currentPropertiesOptions ||
          (window.currentPropertiesOptions = {});
      var a = null,
        o = !0,
        r = null,
        l = {
          onLoad: function () {
            window.top.performance.mark("end_widgetLoading"),
              window.top.performance.mark("start_managePref");
            var c,
              d,
              u = this;
            if (!currentPropertiesOptions.selected)
              if (t.getUrlParameter("selected")) {
                var h = t.getUrlParameter("meta")
                    ? t.getUrlParameter("meta")
                    : null,
                  p = t.getUrlParameter("tenant")
                    ? t.getUrlParameter("tenant")
                    : null,
                  m = t.getUrlParameter("source")
                    ? t.getUrlParameter("source")
                    : null,
                  f = t.getUrlParameter("sourceName")
                    ? t.getUrlParameter("sourceName")
                    : null,
                  g = { objectId: t.getUrlParameter("selected") };
                h && (g.metatype = h),
                  p && (g.tenant = p),
                  m && (g.source = m),
                  f && (g.sourceName = f),
                  (currentPropertiesOptions.selected = [new s(g)]);
              } else {
                var v = t.getWidgetPreference("SelectedItem");
                if (
                  null != v &&
                  "object" == typeof v &&
                  ((o =
                    "true" === t.getWidgetPreferenceValue("loadData") ||
                    !0 === t.getWidgetPreferenceValue("loadData")),
                  (currentPropertiesOptions.selected = Array.isArray(v.value)
                    ? v.value
                    : []),
                  "undefined" != typeof widget)
                ) {
                  var _ =
                    void 0 !== widget.getValue("X3DContentId")
                      ? JSON.parse(widget.getValue("X3DContentId"))
                      : null;
                  if (null !== _) {
                    var E = _.data;
                    if (null !== E) {
                      var C = [];
                      if (E.items.length > 0)
                        for (
                          null !== (S = E.items[0]).objectId &&
                            void 0 !== S.objectId &&
                            C.push(S),
                            itemsLength = C.length,
                            itemsIndex = 0;
                          itemsIndex < itemsLength;
                          itemsIndex++
                        ) {
                          var S;
                          ((g = {
                            objectId: (S = C[itemsIndex]).objectId,
                            metatype: "businessobject",
                          }).tenant = S.envId),
                            (g.source = S.serviceId),
                            (g.sourceName = S.serviceId),
                            (currentPropertiesOptions.selected = [new s(g)]);
                        }
                    }
                  }
                }
              }
            var y = !1;
            t.isWebInWinEnvironment()
              ? ((c = t.getUrlParameter("defaultFacetName")),
                (d = t.getUrlParameter("infoForFacet")),
                (y =
                  "true" === t.getUrlParameter("indexModeParameter") ||
                  !0 === widget.getValue("indexModeParameter")))
              : ((c = widget.getValue("defaultFacetName")),
                (d = widget.getValue("infoForFacet")),
                (y =
                  "true" === widget.getValue("indexModeParameter") ||
                  !0 === widget.getValue("indexModeParameter"))),
              t.setWidgetValue("x3dAppId", "ENOLCMT_AP"),
              document.body.addClassName("editPropWidgetHTML");
            var A = "true" === t.getUrlParameter("editMode");
            void 0 === A &&
              (A =
                "true" ===
                t.getWidgetPreferenceValue("editProperties_editMode"));
            var I = "true" === t.getUrlParameter("updateSession"),
              b = "true" === t.getWidgetPreferenceValue("isTransient"),
              P = n.STANDARD;
            b && (P = n.TRANSIENT);
            var T = "true" === t.getUrlParameter("readonly");
            (T =
              T ||
              ("false" !== widget.getBool("readOnly") &&
                !1 !== widget.getBool("readOnly"))),
              window.top.performance.mark("end_managePref");
            var D = t.getWidgetValue("securityContextFromAppPreference")
                ? t.getWidgetValue("securityContextFromAppPreference")
                : null,
              M = t.getWidgetValue("parent_x3dAppId")
                ? t.getWidgetValue("parent_x3dAppId")
                : null,
              w = !!t.getWidgetValue("disableAddExtensionButton");
            let O = t.getWidgetValue("USE_APP_SECURTIYCONTEXT");
            O = !!O;
            let F = t.getWidgetValue("ignoreKernelAccessChecks");
            (F = "true" === F),
              window.top.performance.mark("start_createProp"),
              (a = new e({
                readOnly: T,
                showTopBar: !0,
                setCloseButton: !0,
                setEditButton: !0,
                displayType: P,
                selectionType: n.ALL_SELECTION,
                editMode: A,
                defaultFacetName: c,
                infoForFacet: d,
                indexModeParameter: y,
                updateSession: I,
                topClassHeight: "modal-content",
                securityContextFromAppPreference: D,
                sourceAppId: M,
                disableAddExtensionButton: w,
                USE_APP_SECURTIYCONTEXT: O,
                ignoreKernelAccessChecks: F,
                events: {
                  onTitleChanged: function (e) {
                    var t = e || "" === e ? e : a.getTitle();
                    (t = t ? t.unescapeHTML() : ""), widget.setTitle(t);
                  },
                  onDataLoaded: function () {
                    a.onResize(),
                      setTimeout(function () {
                        "undefined" != typeof dscef &&
                          dscef.sendString("DataLoaded"),
                          !u.changeControlInitialized &&
                            t.isChangeAvailable() &&
                            ((u.changeControlInitialized = !0),
                            require([
                              "DS/EditPropWidget/changeAction/passThruJS",
                            ], function (e) {
                              e.onReady(function (e) {
                                var i = t.getWidgetPreference("changeControl"),
                                  n = !0;
                                if (i && i.value) {
                                  var s = i.value;
                                  n = !e.changeIsSetChecker(s);
                                }
                                e.initialize(
                                  a.elements.container.elements.container,
                                  null,
                                  n ? "hide" : "show",
                                  "bottom-left",
                                  { isEditable: !1 }
                                ),
                                  (enoviaServerAuthoringWidget.saveMode =
                                    "change"),
                                  (n &&
                                    (!s ||
                                      (null == s.configurationRevision &&
                                        null == s.configurationRevision))) ||
                                    e.set(s);
                              });
                            }));
                      });
                  },
                },
              })),
              window.top.performance.mark("end_createProp");
            var x = a.getTitle();
            widget.setTitle(x), widget.body.setContent(a);
            var W = widget.getView().type;
            "fullscreen" != W && "maximized" != W && l._setBodyHeight("600"),
              (r = t.getWidgetPreference("securityContext")) &&
                r.value &&
                (O
                  ? i
                      .getInstance({
                        context: {
                          getSecurityContext: function () {
                            return { SecurityContext: r.value };
                          },
                        },
                      })
                      .getValidSecurityContext()
                  : i
                      .getInstance({
                        context: {
                          getSecurityContext: function () {
                            return { SecurityContext: r.value };
                          },
                        },
                      })
                      .getSecurityContext());
            var R = t.getWidgetPreference("isTransient");
            R && "false" === R.value && t.deleteWidgetValue("SelectedItem"),
              t.setWidgetValue("isTransient", "false");
            var N = widget.body.getParent().getSize();
            l._setBodyHeight(N.height);
            var U = currentPropertiesOptions.selected
              ? currentPropertiesOptions.selected
              : [];
            window.top.performance.mark("start_initdata"),
              a.initDatas(U, void 0, void 0, { loadModel: o }),
              window.top.performance.mark("end_initdata"),
              (currentPropertiesOptions.component = a);
          },
          onRefresh: function () {
            a.onRefresh();
          },
          onResize: function (e) {
            var t = widget.body.getParent().getSize();
            l._setBodyHeight(t.height), a.onResize();
          },
          onDestroy: function (e) {
            a.destroyComponent();
          },
          onViewChange: function (e) {
            var t = e.height;
            "fullscreen" !== e.type && "maximized" !== e.type && (t = "600"),
              t && l._setBodyHeight("600");
          },
          _setBodyHeight: function (e) {
            e &&
              ("auto" !== e && (e += "px"), widget.body.setStyle("height", e));
          },
        };
      t.isReplayedODT()
        ? setTimeout(function () {
            l.onLoad();
          })
        : widget.addEvent("onLoad", l.onLoad),
        widget.addEvent("onRefresh", l.onRefresh),
        widget.addEvent("onResize", l.onResize),
        widget.addEvent("onViewChange", l.onViewChange),
        window.addEventListener("unload", l.onDestroy),
        widget.setAutoRefresh(1e4);
    }
  ),
  define(
    "DS/EditPropWidget/popup/EditPropPopup",
    [
      "UWA/Controls/Abstract",
      "DS/Logger/Logger",
      "DS/EditPropWidget/controllers/EditPropMainController",
      "DS/WidgetServices/WidgetServices",
      "DS/EditPropWidget/constants/EditPropConstants",
      "DS/UIKIT/Spinner",
      "DS/ENOFloatingPanel/ENOFloatingPanel",
      "css!DS/EditPropWidget/EditPropWidget.css",
      "i18n!DS/EditPropWidget/assets/nls/editPropPopup",
    ],
    function (e, t, i, n, s, a, o, r, l) {
      "use strict";
      return e.extend({
        defaultOptions: {
          className: "",
          readOnly: !1,
          serverName: "",
          serverUri: "",
          editMode: !1,
          container: null,
          context: null,
        },
        init: function (e) {
          (this.dataReady = !1), this._parent(e), this.initUI();
        },
        centerIt: function () {
          (this.centerAtNextResize = !1), this.container.centerIt();
        },
        setDefaultListOfModel: function (e) {
          this.defaultModels = e;
        },
        setDataReady: function (e) {
          e && (this.defaultModels = null), (this.dataReady = e);
        },
        getController: function () {
          return this.propController;
        },
        initUI: function () {
          var e = this;
          (this.centerAtNextResize = !1),
            (this.widgetPere = this.options.container),
            (this.widgetPere = this.widgetPere
              ? this.widgetPere
              : n.getWidgetBody());
          var t = {},
            r = this.options;
          for (var c in r) t[c] = r[c];
          (t.className = "editProp_innerComponent"),
            (t.showTopBar = !1),
            (t.popup = !0),
            (t.setCloseButton = !0),
            (t.setEditButton = !0),
            (t.events = {});
          var d = !1,
            u = {
              onDataLoaded: function () {
                if (d) {
                  var t = e.propController.getOptimalSize(),
                    i = t.width;
                  i < 600 && (i = 600),
                    e.container.setNewBodySize({ width: i, height: t.height }),
                    e.container.lockWidth(),
                    (e.centerAtNextResize = !0);
                }
                e.fakeFloatingPanel.hide();
              },
              onTitleChanged: function () {
                var t = e.propController.getTitle();
                t &&
                  (t =
                    " - " +
                    (t = (t = t.replace(/(&nbsp;)/g, " ")).replace(
                      /(<wbr>)/g,
                      ""
                    ))),
                  e.container.setTitle("ENOVIA - Information" + t);
              },
              onIdentityCardExpands: function () {
                e.onResize();
              },
            };
          if (r.events) for (var c in r.events) t.events[c] = r.events[c];
          for (var c in u) t.events[c] = u[c];
          e.propController = new i(t);
          e.propController.getTopBar();
          var h = UWA.createElement("table", { class: "EditProp_topBarPopup" }),
            p =
              (UWA.createElement("td", { class: "titleSpan" }).inject(h),
              UWA.createElement("td", { class: "right-actions" }).inject(h),
              null),
            m = null;
          n.isWebInWinEnvironment() ||
            n.isNatifEnvironnement() ||
            (n.getRealPath("EditPropWidget/EditPropWidget.html", widget, !1),
            (p = {
              custoClass: "properties_piner",
              widgetTitle: "",
              widgetAppId: "ENOLCMT_AP",
              widgetData: {
                title: "My widget",
                SelectedItem: [],
                loadData: !0,
                editProperty_collapseIC: n.getWidgetValue(
                  s.COLLAPSE_IC_PREFERENCE.name
                ),
                editProperties_editMode: n.getWidgetValue(
                  "editProperties_editMode"
                ),
              },
            }),
            (m = function (t) {
              var i = e.propController.getModels();
              0 === i.length && e.defaultModels && (i = e.defaultModels);
              i.forEach(function (e, t) {
                var n = UWA.clone(e.getChangedValues());
                i[t].set("EditProp_listOfChange", n);
              }),
                (t.widgetData.SelectedItem = i),
                (t.widgetData.loadData = !e.dataReady),
                (t.widgetData.editProperty_collapseIC = n.getWidgetValue(
                  s.COLLAPSE_IC_PREFERENCE.name
                )),
                (t.widgetData.editProperties_editMode = n.getWidgetValue(
                  "editProperties_editMode"
                ));
            }));
          var f = UWA.createElement("span");
          new a({ visible: !0 }).inject(f),
            UWA.createElement("span", {
              tag: "span",
              text: l.get("Loading"),
            }).inject(f),
            (e.fakeFloatingPanel = new o({
              className: "EPP_PopupDialFake",
              body: f,
              title: "ENOVIA - Information",
              pinOptions: p,
              pinCallback: m,
              overlay: !1,
              closable: !0,
              animate: !0,
              resizable: !1,
              events: {
                onClose: function () {
                  e._cancel();
                },
              },
            })),
            (e.container = new o({
              className: "EPP_PopupDial " + this.options.className,
              body: e.propController.getUI(),
              footer: e.propController.getTopBar(),
              title: "ENOVIA - Information",
              pinOptions: p,
              pinCallback: m,
              persistId: "floatProperties",
              overlay: !1,
              closable: !0,
              animate: !0,
              resizable: !0,
              autocenter: !1,
              events: {
                onResizeFloating: function () {
                  e.centerAtNextResize && e.centerIt();
                },
                onClose: function () {
                  e._cancel();
                },
              },
            })),
            (d = !e.container.isDimensionSaved()),
            e.container.elements.container.setStyle("left", -2e4),
            e.fakeFloatingPanel.inject(this.widgetPere),
            e.container.inject(this.widgetPere);
          var g = this.container.elements.header;
          this.container.elements.content;
          setTimeout(function () {
            var t = e.widgetPere.getSize(),
              i = g.getSize(),
              n = "1000";
            "1000" > t.height - i.height && (n = t.height - i.height),
              d && e.container.setNewSize({ width: 600, height: n }),
              e.fakeFloatingPanel.centerIt();
          }, 0);
        },
        show: function () {
          this.fakeFloatingPanel.show(), this.container.show();
        },
        onRefresh: function () {
          null != this.propController && this.propController.onRefresh();
        },
        onResize: function () {
          this.container.onResize(),
            this.propController && this.propController.onResize();
        },
        _cancel: function () {
          this.dispatchEvent("onCancel"), this.dispatchEvent("onInnerCancel");
        },
        destroyComponent: function () {
          this.propController.destroyComponent(),
            this.container.hide(),
            this.container.destroy(),
            this.fakeFloatingPanel.hide(),
            this.fakeFloatingPanel.destroy();
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/EditPropComponent",
    [
      "DS/EditPropWidget/EditPropWidget",
      "DS/EditPropWidget/popup/EditPropPopup",
    ],
    function (e, t) {
      "use strict";
      return e.extend({
        init: function (e) {
          this._parent(e);
        },
        _getController: function (e) {
          return (
            (this.elements.popup = this.elements.popup
              ? this.elements.popup
              : new t(e)),
            this.controller
              ? this.controller
              : this.elements.popup.getController()
          );
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/utils/EditPropIndexDictionaryForUnits",
    [
      "UWA/Core",
      "DS/EditPropWidget/ajaxRequests/EditPropAjaxRequests",
      "DS/WidgetServices/WidgetServices",
      "DS/EditPropWidget/constants/ConstantsMapping",
      "DS/EditPropWidget/constants/EditPropConstants",
    ],
    function (e, t, i, n, s) {
      "use strict";
      return e.Class.singleton({
        _dictionary: null,
        init: function () {
          this._dictionary = null;
        },
        setUpDimensionsDictionary: function () {
          var e = this,
            s = e._getDictionary();
          if (s) return (e._dictionary = s), void e._destoryDictionary();
          e._dictionary = new Map();
          var a = i.getCancelObject(),
            o = n.getServiceBySource("3dspace"),
            r = i.getLanguage(),
            l = {
              tenantId: i.getTenantID(),
              lang: r,
              handleRequest: { handle: a },
              defService: o,
              onComplete: function (t) {
                t &&
                  "object" == typeof t &&
                  t.results &&
                  t.results.length > 0 &&
                  (t.results.forEach(function (t) {
                    e._dictionary.set(t.Name, t);
                  }),
                  e._storeDictionary(e._dictionary));
              },
            };
          t.getListOfDimensions(l);
        },
        getDimension: async function (e, t, i) {
          var n = this;
          n._getDictionary() && n._destoryDictionary();
          var a = 0;
          e.results[0].data.forEach(async function (o) {
            if (t.has(o.name)) {
              var r = t.get(o.name),
                l = r.dimension,
                c = n.getMKSUnit(l),
                d = r.manipulationUnit,
                u = n.getNLSNameForDimension(l, d);
              if (
                ((o.dimensionType = s.EDITPROPINDEX_DIMENSION + l),
                (o.unitName = d),
                (o.dimension = u),
                d && d.length > 0 && c !== d)
              ) {
                a++;
                var h = {
                  UnitFrom: c,
                  UnitTo: d,
                  Dimension: l,
                  Values: o.valueDB,
                };
                n.getConvertedMKSValue([h], function (t) {
                  a--,
                    t &&
                      "object" == typeof t &&
                      t.ConvertedValues &&
                      ((o.value = t.ConvertedValues),
                      (o.valueDB = t.ConvertedValues)),
                    0 === a && i(e);
                });
              }
            }
          }),
            0 === a && i(e);
        },
        getMKSUnit: function (e) {
          return this._dictionary.get(e) ? this._dictionary.get(e).MKSUnit : "";
        },
        getNLSNameForDimension: function (e, t) {
          var i = this._dictionary.get(e);
          if (i) {
            if (
              (0 === t.length && (t = i.MKSUnit),
              i && i.Units && Array.isArray(i.Units))
            )
              for (var n = 0; n < i.Units.length; n++)
                if (i.Units[n].Name === t) return i.Units[n].NLSName;
            return "";
          }
        },
        getConvertedMKSValue: function (e, s) {
          var a = i.getCancelObject(),
            o = n.getServiceBySource("3dspace"),
            r = i.getLanguage(),
            l = {
              tenantId: i.getTenantID(),
              lang: r,
              data: e,
              handleRequest: { handle: a },
              defService: o,
              onComplete: function (e) {
                if (e && "object" == typeof e)
                  for (var t in e) {
                    var i = e[t];
                    Array.isArray(i) && i.length > 0 && i[0] && s(i[0]);
                  }
              },
            };
          t.getConvertedMKSValue(l);
        },
        _storeDictionary: function (e) {
          window.top.ENOEditPropIndex_DimensionDictionary = e;
        },
        _getDictionary: function () {
          return window.top.ENOEditPropIndex_DimensionDictionary
            ? window.top.ENOEditPropIndex_DimensionDictionary
            : null;
        },
        _destoryDictionary: function () {
          window.top.ENOEditPropIndex_DimensionDictionary &&
            delete window.top.ENOEditPropIndex_DimensionDictionary;
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/facets/Common/FacetsCore",
    ["DS/EditPropWidget/models/CommonAbstract"],
    function (e) {
      return e.extend({
        _sharedContext: null,
        destroyComponent: function () {},
        onResize: function () {},
        updateFacet: function () {},
        getButtons: function () {},
        onAction: function (e) {},
        onSave: function () {},
        getExtensions: function () {},
        nothingToSave: function () {
          return !0;
        },
        isActionAvailable: function (e) {
          return !0;
        },
        init: function (e) {
          this._parent(e), this.elements || (this.elements = {});
        },
        getTabId: function () {
          return this.options.tabId ? this.options.tabId : "";
        },
        onRefresh: function () {
          this.updateFacet();
        },
        getContext: function () {
          return this._sharedContext;
        },
        switchSaveButton: function (e) {
          this._raiseEvent("switchSave", e);
        },
        switchAddButton: function (e) {
          this._raiseEvent("switchAdd", e);
        },
        refreshActions: function () {
          this._raiseEvent("updateActions");
        },
        updateReadWriteAccess: function (e) {
          this._raiseEvent("updateRWA", e);
        },
        _raiseNotification: function (e, t, i) {
          (t = t || "error"),
            this.dispatchEvent("onNotification", {
              msg: e,
              eventID: t,
              customClass: i,
            });
        },
        _raiseEvent: function (e, t) {
          e &&
            this.dispatchEvent("onEvent", {
              facet: this.getTabId(),
              eventID: e,
              value: t,
            });
        },
        inject: function (e) {
          var t = this.getUI();
          t && (t.hide(), t.inject(e));
        },
        show: function () {
          return this.__callMethodOnUI("show");
        },
        hide: function () {
          return this.__callMethodOnUI("hide");
        },
        getOptimalSize: function () {
          return this.__callMethodOnUI("getSize");
        },
        __callMethodOnUI: function (e) {
          var t = this._getUI(),
            i = t[e];
          return (
            "function" != typeof i && (i = (t = this._getDOMUI())[e]),
            t ? i.call(t) : null
          );
        },
        getUI: function () {
          return this._getUI();
        },
        _getDOMUI: function () {
          for (var e = this._getUI(); "function" == typeof e.getContent; )
            e = e.getContent();
          return e;
        },
        _getUI: function () {
          return this.elements ? this.elements.container : null;
        },
        showLoader: function () {
          this._raiseEvent("switchLoader", !0);
        },
        hideLoader: function () {
          this._raiseEvent("switchLoader", !1);
        },
        display: function (e) {
          this.setContext(e), this.show();
        },
        setContext: function (e) {
          this._sharedContext !== e &&
            ((this._sharedContext = e), this.updateFacet());
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/facets/Common/FacetsBase",
    [
      "DS/EditPropWidget/facets/Common/FacetsCore",
      "DS/EditPropWidget/constants/EditPropConstants",
    ],
    function (e, t) {
      return e.extend({
        _lastUpdatedId: null,
        _sharedOptions: null,
        init: function (e, t) {
          (this._sharedOptions = t),
            this._parent.apply(this, arguments),
            this.elements || (this.elements = {});
        },
        getSharedOptions: function () {
          return this._sharedOptions;
        },
        isReadOnly: function () {
          var e = this.getSharedOptions();
          return !e || !0 === e.readOnly;
        },
        getOccurrence: function () {
          return this.getContext();
        },
        getModel: function (e) {
          var i = null,
            n = this.getOccurrence();
          if (
            (n && (model = this.getOccurrence().getModel(this.getTabId())), e)
          ) {
            if (
              (model && model.get("objectId") === e && (i = model), !model && n)
            ) {
              var s = n.getAllModels();
              for (var a in s) {
                var o = s[a];
                if (o && o.get("objectId") === e) {
                  i = o;
                  break;
                }
              }
            }
          } else
            n &&
              (model ||
                (model = this.getOccurrence().getModel(t.FACET_PROPERTIES)),
              model || (model = this.getOccurrence().getModel(t.MODEL_INIT)),
              (i = model));
          return i;
        },
        setContext: function (e) {
          this.setOccurrence(e);
        },
        setOccurrence: function (e) {
          var i;
          if ((i = this.getOccurrence()) && e) {
            var n = i.getModel(t.MODEL_INIT),
              s = i.getModel(t.FACET_PROPERTIES),
              a = e.getModel(t.MODEL_INIT),
              o = e.getModel(t.FACET_PROPERTIES);
            this.getModel();
            var r = e.getModel(this.getTabId());
          }
          if ((this._setOccurrence(e), (i = this.getOccurrence()))) {
            var l = this.getModel();
            if (!l) return;
            var c = l.get("objectId");
            "relationship" == l.get("metatype") &&
              (c = l.get("selReferenceID"));
            var d = !0;
            r && n.get("objectId") === a.get("objectId")
              ? ((d = !1), i.setModel(this.getTabId(), r))
              : s && o && s.get("objectId") === o.get("objectId")
              ? ((d = !1), i.setModel(t.FACET_PROPERTIES, s))
              : n &&
                n.get("objectId") === a.get("objectId") &&
                ((d = !1), i.setModel(t.MODEL_INIT, n)),
              (c != this._lastUpdatedId || d) &&
                ((this._lastUpdatedId = c), this.updateFacet());
          }
        },
        _setOccurrence: function (e) {
          this._sharedContext = e;
        },
        setModel: function (e) {
          this.getTabId() === t.FACET_iPROPERTIES
            ? this.getOccurrence() &&
              this.getOccurrence().setModel(t.FACET_PROPERTIES, e)
            : this.getOccurrence() &&
              this.getOccurrence().setModel(this.getTabId(), e);
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/facets/History/History",
    [
      "DS/EditPropWidget/facets/Common/FacetsBase",
      "UWA/Controls/Abstract",
      "UWA/Class",
      "DS/DataDragAndDrop/DataDragAndDrop",
      "UWA/Json",
      "DS/UIKIT/Tooltip",
      "DS/UIKIT/Scroller",
      "DS/EditPropWidget/ajaxRequests/EditPropAjaxRequests",
      "DS/WidgetServices/WidgetServices",
      "DS/WidgetServices/UIInfra/UIInfra",
      "DS/EditPropWidget/models/EditPropHistoryModel",
      "DS/UIKIT/Spinner",
      "css!DS/EditPropWidget/EditPropWidget.css",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s, a, o, r, l, c, d, u, h, p) {
      "use strict";
      return i.extend(t, e, {
        defaultOptions: { className: "", nbResults: 5 },
        pictureAPIConfig: {
          defaultSwym:
            "https://dsext001-eu1-215dsi0708-3dswym.3dexperience.3ds.com",
          pictureAPIPrefix: "/api/user/getpicture/login/",
          pictureAPISuffix: "/format/mini",
        },
        init: function (e) {
          this._parent(e), this._initValues(), this._initUI();
        },
        _initValues: function () {
          (this._mapItemToDrag = {}),
            (this.currentStatus = {
              noMoreHistory: !1,
              lastDate: null,
              model: null,
            }),
            (this.request = { getHistory: { handle: null } });
        },
        _initUI: function () {
          var e = this;
          (this.elements = {}),
            (this.elements.container = UWA.createElement("div", {
              class: "EP_MainHistoryContainer " + this.options.className,
            })),
            (this.elements.sub_container = UWA.createElement("div", {
              class: "EP_SS_MainHistoryContainer",
            }).inject(this.elements.container));
          var t = UWA.createElement("div", {
            class: "EP_HistoryFatherContainer",
          }).inject(this.elements.sub_container);
          this.elements.historyContainer = UWA.createElement("div", {
            class: "EP_HistoryContainer",
          }).inject(t);
          (this.elements.scroller = new o({
            element: this.elements.sub_container,
          }).inject(this.elements.container)),
            this.elements.scroller.elements.content.addEventListener(
              "scroll",
              function () {
                e._actionOnScroll();
              }
            ),
            (this.elements.sSpinner = new u({
              className: "hist_small_spinner",
            }).inject(
              UWA.createElement("div", {
                class: "hist_small_spinner_container",
              }).inject(t)
            )),
            (this.elements.spinner = new u({
              className: "hist_spinner",
            }).inject(this.elements.container));
        },
        _actionOnScroll: function () {
          var e = this.elements.historyContainer.getSize(),
            t = this.elements.scroller.elements.content.getSize(),
            i =
              this.elements.sSpinner.elements.container.parentElement.getSize();
          this.elements.historyContainer.getPosition().y <=
            t.height - e.height - i.height + 2 &&
            this.loadHistory(this.currentStatus.model);
        },
        _hideAll: function () {
          !(this._getAllUIElements().length > 0) &&
            this.elements.historyContainer.hide(),
            this.elements.spinner.hide(),
            this.elements.sSpinner.hide();
        },
        _showLoader: function () {
          this._getAllUIElements().length > 0
            ? this._showSmallSpinner()
            : this._showMainSpinner();
        },
        _showSmallSpinner: function () {
          this.elements.sSpinner.show();
        },
        _showMainSpinner: function () {
          this._hideAll(), this.elements.spinner.show();
        },
        _hideSpinner: function () {
          this._hideAll(), this.elements.historyContainer.show();
        },
        _createChangeNameUI: function (e, t) {
          var i = UWA.createElement("div", { class: "hist_modify_container" });
          if (t.info) {
            var n = t.info.name;
            n = n || t.info.value;
            var s = t.info.was;
            e &&
              e.iconType &&
              UWA.createElement("div", {
                class: "hist_modify_container_image",
                html: {
                  tag: "img",
                  class: "EditProp_undraggable",
                  width: "16px",
                  src: e.iconType,
                },
              }).inject(i);
            var o = UWA.createElement("div", {
                class: "hist_modify_container_txt",
              }).inject(i),
              r = UWA.createElement("span", { text: n || "" }).inject(
                UWA.createElement("div", { class: "hist_modify_name" }).inject(
                  o
                )
              ),
              l = UWA.createElement("span", { text: s || "" }).inject(
                UWA.createElement("div", {
                  class: "hist_modify_name hist_oldName",
                }).inject(o)
              );
            new a({
              delay: { show: 400 },
              position: "top left",
              target: l,
              body: s,
            }),
              new a({
                delay: { show: 400 },
                position: "top left",
                target: r,
                body: n,
              });
          }
          return i;
        },
        _addHistory: function (e, t) {
          var i = this,
            o = UWA.createElement("div", { class: "EP_HistoryElement" }).inject(
              this.elements.historyContainer
            ),
            r = t.get("userId");
          r &&
            this._userPictureURL(r, {
              onComplete: function (e) {
                var t = UWA.createElement("img", {
                  tag: "img",
                  class: "EditProp_undraggable img-circle",
                  width: "45px",
                  src: e,
                }).inject(
                  UWA.createElement("div", {
                    class: "EP_HistoryPicture",
                  }).inject(o)
                );
                t.hide(),
                  (t.onload = function (e) {
                    o.addClassName("hist_withPicture"), t.show();
                  });
              },
            });
          var d = UWA.createElement("div", { class: "EP_HistoryInfos" }).inject(
              o
            ),
            u = t.get("objects"),
            h = t.get("eventNls"),
            m = t.get("time"),
            f = t.get("user"),
            g = "";
          f || (g = "Someone ");
          var v = u ? u.length : 0,
            _ = v > 1 ? "s" : "",
            E = null,
            C = "";
          switch (h) {
            case "addFolder":
              C = p.replace(p.get(g + "added {0} folder" + _), { 0: v });
              break;
            case "addContent":
              C = p.replace(p.get(g + "added {0} content" + _), { 0: v });
              break;
            case "removeFolder":
              C = p.replace(p.get(g + "removed {0} folder" + _), { 0: v });
              break;
            case "removeContent":
              C = p.replace(p.get(g + "removed {0} content" + _), { 0: v });
              break;
            case "modify":
              var S = t.info.nameNls ? t.info.nameNls : t.info.name;
              delete t.info.name,
                (C = p.replace(p.get(g + "changed the attribute {0}"), {
                  0: S,
                })),
                (E = this._createChangeNameUI(e, t));
              break;
            case "changeName":
              (C = p.get(g + "changed the name")),
                (E = this._createChangeNameUI(e, t));
              break;
            case "create":
              C = p.get(g + "create the object");
              break;
            default:
              C = p.get(g + "made some changes");
          }
          UWA.createElement("div", {
            html: [
              { tag: "span", class: "EP_history_user", text: f },
              { tag: "span", text: " " + C },
            ],
          }).inject(d);
          var y = new Date(m).toLocaleString(l.getLanguage());
          UWA.createElement("div", {
            class: "EP_History_date",
            text: y,
          }).inject(d),
            null != E && E.inject(d);
          var A = UWA.createElement("div", {
              class: "EP_History_ObjectList",
            }).inject(d),
            I = ["Workspace", "Workspace Vault"];
          u.forEach(function (t) {
            var o = t.get("type");
            if (I.indexOf(o) < 0) {
              var r = t.get("name"),
                d = t.get("label");
              d = d || r;
              var u = t.get("id"),
                h = u || 0 === u,
                m = h ? "exist" : "hist_object_delete",
                f = t.get("iconType"),
                g = null;
              f &&
                (g = UWA.createElement("div", {
                  class: "EP_History_type_icon",
                  html: {
                    tag: "img",
                    width: "16px",
                    class: "EditProp_undraggable",
                    src: f,
                  },
                })),
                (m += null !== g ? "" : " withoutThumbnail");
              var v = UWA.createElement("div", {
                class: "EP_History_dragObj",
              }).inject(A);
              t = UWA.createElement("div", {
                class: "EP_History_ObjectElement " + m,
              }).inject(v);
              g && g.inject(t);
              var _ = UWA.createElement("span", {
                class: "EP_History_ObjectName",
                text: d,
              }).inject(t);
              if ((new a({ delay: { show: 400 }, target: _, body: d }), h)) {
                var E = {
                    objectId: u,
                    objectType: o,
                    serviceId: e.source,
                    envId: e.tenant,
                  },
                  C = function (e, t, n) {
                    if (!t.hasClassName(S) || !0 !== n) {
                      if (!0 !== e.ctrlKey)
                        i.elements.historyContainer
                          .getElements("." + S)
                          .forEach(function (e) {
                            e.removeClassName(S);
                          }),
                          (i._mapItemToDrag = {});
                      t.toggleClassName(S),
                        t.hasClassName(S)
                          ? (i._mapItemToDrag[u] = E)
                          : i._mapItemToDrag[u] && delete i._mapItemToDrag[u];
                    }
                  },
                  S = "hist_objectSelected";
                t.addEvents({
                  click: function (e) {
                    C(e, this);
                  },
                }),
                  n.draggable(t, {
                    protocol: "3DXContent",
                    version: "1.1",
                    source: "ENOLCMT_AP",
                    widget_id: l.getWidgetId(),
                    data: void 0,
                    start: function (e, n) {
                      C(n, t, !0), (i._mapItemToDrag[u] = E);
                      var a = [];
                      for (var o in i._mapItemToDrag)
                        a.push(i._mapItemToDrag[o]);
                      var r = { data: { items: a } },
                        l = s.encode(r);
                      n.dataTransfer.setData("text", l),
                        c.createGhostDnD(n, {
                          number: a.length,
                          className: "Hist_ghost",
                        });
                    },
                    stop: function () {},
                  });
              } else
                new a({
                  position: "bottom",
                  delay: { show: 400 },
                  target: t,
                  body: p.get("This object is not accessible"),
                });
            }
          });
        },
        launchHistory: function (e) {
          var t = e.length,
            i = !1;
          if (
            t ===
            (this.currentStatus.model ? this.currentStatus.model.length : 0)
          ) {
            i = !0;
            for (
              var n = 0;
              n < t &&
              !1 !==
                (i =
                  e[n].get("objectId") ===
                  this.currentStatus.model[n].get("objectId"));
              n++
            );
          }
          i || (this.clearHistory(), this.loadHistory(e));
        },
        clearHistory: function () {
          this._endOfRequest(),
            (this.currentStatus.lastDate = null),
            (this.currentStatus.noMoreHistory = !1),
            this._getAllUIElements().forEach(function (e) {
              e.remove();
            });
        },
        _getAllUIElements: function () {
          return this.elements.historyContainer.getElements(
            ".EP_HistoryElement"
          );
        },
        _userPictureURL: function (e, t) {
          var i = this;
          l.get3DSwymUrlAsync({
            onComplete: function (n) {
              var s = n || i.pictureAPIConfig.defaultSwym,
                a = s
                  ? s +
                    i.pictureAPIConfig.pictureAPIPrefix +
                    e +
                    i.pictureAPIConfig.pictureAPISuffix
                  : "";
              t.onComplete.call(null, a);
            },
          });
        },
        updateFacet: function (e) {
          this.clearHistory(), this.loadHistory(this.getModel());
        },
        loadHistory: function (e) {
          var t = this;
          null == e || Array.isArray(e) || (e = [e]),
            this.request.getHistory.handle &&
              e &&
              this.currentStatus.model &&
              (e.length != this.currentStatus.model.length ||
                e[0].get("objectId") !=
                  this.currentStatus.model[0].get("objectId")) &&
              (this.request.getHistory.handle.cancel(),
              (this.request.getHistory.handle = null));
          var i = null === this.currentStatus.lastDate;
          if (
            !1 === t.currentStatus.noMoreHistory &&
            !this.request.getHistory.handle &&
            e
          ) {
            (this.currentStatus.model = e), this._showLoader();
            var n = e[0].get("source"),
              s = {
                modelList: e,
                tenant: e[0].getTenant(),
                nbResult: this.options.nbResults,
                onComplete: function (e) {
                  i && t.clearHistory(),
                    t._endOfRequest(),
                    e.results
                      ? e.results.forEach(function (e) {
                          if (
                            (e.lastTime &&
                              (t.currentStatus.lastDate = e.lastTime),
                            e.history)
                          ) {
                            var i = {
                              id: e.id,
                              type: e.type,
                              iconType: e.iconType,
                              source: n,
                              tenant: e.tenant,
                            };
                            t.getModel(i.id) &&
                              (0 === e.history.length &&
                                (t.currentStatus.noMoreHistory = !0),
                              e.history.forEach(function (e) {
                                var n = new d(e);
                                null !== n.info &&
                                void 0 !== n.info.name &&
                                void 0 !== n.info.was
                                  ? n.info.name !== n.info.was &&
                                    t._addHistory(i, n)
                                  : t._addHistory(i, n);
                              }),
                              t._actionOnScroll());
                          }
                          e.error && t._setError({ message: e.error });
                        })
                      : t._setError({ message: p.get("can't find history") });
                },
                onFailure: function (e) {
                  t._endOfRequest(), t._setError(e);
                },
              };
            this.currentStatus.lastDate &&
              (s.lastDate = this.currentStatus.lastDate),
              (s.handleRequest = this.request.getHistory),
              (s.handleRequest.handle = l.getCancelObject().handler),
              r.getHistory(s);
          }
        },
        _endOfRequest: function () {
          (this.request.getHistory.handle = null), this._hideSpinner();
        },
        _setError: function (e, t, i) {
          this._raiseNotification(e, t, i);
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/facets/GeneralHistory/History",
    [
      "DS/EditPropWidget/facets/Common/FacetsBase",
      "DS/WidgetServices/WidgetServices",
      "DS/ENXHistoryUX/View/HistoryFacet",
      "css!DS/EditPropWidget/EditPropWidget.css",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s) {
      "use strict";
      return e.extend({
        defaultOptions: { className: "", nbResults: 5 },
        init: function (e) {
          this._parent(e), (this.options = e), this._initUI();
        },
        _initUI: function () {
          this.elements = {};
          this.getModel();
          var e = UWA.createElement("div", {
            class: "EP_HistoryFacet_Container",
          });
          this.elements.container = e;
        },
        updateFacet: function (e) {
          var n = this,
            s = n.getModel();
          let a = n.getOccurrence();
          if ("connection" === s.get("metatype") && a) {
            let e = a.get("properties");
            e && e.model && (s = e.model);
          }
          let o =
              "relationship" === s.get("metatype")
                ? s.get("selReferenceID")
                  ? s.get("selReferenceID")
                  : s.get("instrefid")
                : s.get("objectId"),
            r = "";
          if (a) {
            let e = a.getSecondaryOptionsForFacet();
            e &&
              Array.isArray(e) &&
              e.length > 0 &&
              e.forEach(function (e) {
                "rel" === e.metatype && (r = e.id);
              });
            let t = a.get("_Model_init");
            if (t && t.model) {
              let e = t.model;
              e &&
                "function" == typeof e.get &&
                "relationship" === e.get("metatype") &&
                (e.get("instrefid") || e.get("selReferenceID")) &&
                ((o = e.get("instrefid")
                  ? e.get("instrefid")
                  : e.get("selReferenceID")),
                (r = e.get("objectId")));
            }
          }
          t.get3DSpaceUrlAsync({
            onComplete: function (e) {
              var s = t.getTenantID();
              s = s && 0 != s.length ? "tenant=" + s : "tenant=OnPremise";
              let a = {
                id: [],
                connection: [],
                facetLang: t.getLanguage(),
                securityContext: n.options.securityContextFromAppPreference,
              };
              o && a.id.push(o),
                r && a.connection.push(r),
                t.isReplayedODT()
                  ? window.top && (window.top.EP_ODTData = { options: a })
                  : (0 === a.id.length && 0 === a.connection.length) ||
                    i.init(a, n.elements.container);
            },
          });
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/facets/3DPlay/3DPlayFacet",
    [
      "DS/EditPropWidget/facets/Common/FacetsBase",
      "UWA/Controls/Abstract",
      "DS/EditPropWidget/constants/EditPropConstants",
      "DS/EditPropWidget/ajaxRequests/ServicesAjaxRequests",
      "UWA/Class",
      "DS/WidgetServices/WidgetServices",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s, a, o) {
      return s.extend(t, e, {
        player3D: null,
        _3DViewLaunched: !1,
        init: function (e) {
          this._parent(e), this._initUI();
        },
        _initUI: function () {
          var e = this;
          (this.elements.container = UWA.createElement("div", {
            class: "container_3DPlay",
          })),
            (this.elements.containerHDThumbnail = UWA.createElement("div", {
              class: "editProp_HDImageContainer",
              events: {
                click: function () {
                  e._switchTo3DView(), (e._3DViewLaunched = !0);
                },
              },
            })),
            this.elements.containerHDThumbnail.inject(this.elements.container),
            this.elements.containerHDThumbnail.hide(),
            (this.elements.container3DPlay = UWA.createElement("div", {
              class: "editProp_3DPlayContainer",
            })),
            this.elements.container3DPlay.inject(this.elements.container),
            this.elements.container3DPlay.hide();
        },
        _switchTo3DView: function (e) {
          this.elements.containerHDThumbnail.hide(),
            this.elements.container3DPlay.show(),
            this._initPlayer(e);
        },
        _switchToHDThumbnail: function (e) {
          this.elements.containerHDThumbnail.show(),
            this.elements.container3DPlay.hide();
        },
        destroyComponent: function () {
          this._stop3DPlayer();
        },
        onResize: function () {},
        _stop3DPlayer: function () {
          this.player3D && (this.player3D.dispose(), (this.player3D = null));
        },
        getIdentityCardUI: function () {
          return this.elements.topTable;
        },
        _initPlayer: function (e) {
          var t = this.getModel();
          if (t) {
            var s = t.get("type");
            if ((s || (s = t.get(i.MODEL_DS_ATTR + "type")), s)) {
              "object" == typeof s && (s = (s = s.valueDB) || s.value);
              var r = t.get("objectId");
              if (this.currentId3DPlay === r && this.player3D) return;
              this.elements.container3DPlay.setContent(o.get("3DPlay loading")),
                this._stop3DPlayer();
              var l = {};
              !1 !== e && (l.loading = "autoplay");
              var c = this;
              a.getSourceUrlAsync({
                platformId: n.getTenantFromModel(t),
                source: n.getSourceFromModel(t),
                onComplete: function (e) {
                  var i = {
                    container: c.elements.container3DPlay,
                    input: {
                      asset: {
                        provider: "EV6",
                        serverurl: e,
                        physicalid: r,
                        dtype: s,
                        tenant: n.getTenantFromModel(t),
                        requiredAuth: "passport",
                        contextId: c.options.securityContextFromAppPreference,
                      },
                      usage: "preview",
                    },
                    options: l,
                  };
                  require(["DS/3DPlayHelper/3DPlayHelper"], function (e) {
                    c.elements.container3DPlay.empty(),
                      (c.player3D = new e(i)),
                      (c.currentId3DPlay = r);
                  });
                },
              });
            }
          }
        },
        _handlerUpdateThumbnail: function (e) {
          this._updateThumbnail(e);
        },
        updateFacet: function () {
          this._3DViewLaunched = !1;
          var e = this.getModel();
          e &&
            (e.removeEvent2(
              "onChange:previewUrl",
              this._handlerUpdateThumbnail
            ),
            e.removeEvent2(
              "onChange:type_icon_url",
              this._handlerUpdateThumbnail
            ),
            e.addEvent(
              "onChange:previewUrl",
              this._handlerUpdateThumbnail.bind(this, e)
            ),
            e.addEvent(
              "onChange:type_icon_url",
              this._handlerUpdateThumbnail.bind(this, e)
            ),
            this._updateThumbnail(e)),
            this._stop3DPlayer(),
            this.onRefresh();
        },
        onRefresh: function () {
          var e = null,
            t = this.getModel();
          t && (e = (e = t.get("previewUrl")) || t.get("type_icon_url"));
          e && !0 !== this._3DViewLaunched
            ? this._switchToHDThumbnail()
            : this._switchTo3DView(!1);
        },
        _updateThumbnail: function (e) {
          var t = this;
          this._switchToHDThumbnail();
          var i = e.get("previewUrl");
          if ((i = i || e.get("type_icon_url"))) {
            this.elements.HDThumbnail = UWA.createElement("div", {
              class: "editProp_BigThumbnail",
            });
            var n = new Image();
            (n.onload = function () {
              this.naturalHeight < 100
                ? t._switchTo3DView(!1)
                : t.elements.HDThumbnail.setStyle(
                    "background-image",
                    "url(" + this.src + ")"
                  );
            }),
              (n.src = i),
              this.elements.containerHDThumbnail.setContent(
                this.elements.HDThumbnail
              );
          }
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/facets/SwymComments/SwymComments",
    [
      "DS/EditPropWidget/facets/Common/FacetsBase",
      "UWA/Controls/Abstract",
      "UWA/Class",
      "DS/WidgetServices/WidgetServices",
      "DS/SocialToolbar/SocialToolbar",
      "DS/UIKIT/Scroller",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s, a, o) {
      return i.extend(t, e, {
        _socialView: null,
        init: function (e) {
          (this._socialView = null), this._parent(e), this._initUI();
        },
        _initUI: function () {
          (this.elements.container = UWA.createElement("div", {
            class: "container_SwymComment",
          })),
            (this.elements.content = UWA.createElement("div", {
              class: "content_SwymComment",
            })),
            (this.elements.contentScroll = UWA.createElement("div", {
              class: "contentScroll_SwymComment",
            })),
            this.elements.contentScroll.inject(this.elements.content),
            (this.elements.scroller = new a({
              element: this.elements.content,
            }).inject(this.elements.container));
        },
        updateFacet: function () {
          this._socialView && this._socialView.destroy();
          var e = this;
          this.elements.contentScroll.empty();
          var t = this.getModel();
          if (t) {
            var i = t.get("objectId");
            "relationship" == t.get("metatype") &&
              (i = t.get("selReferenceID")),
              n.getServicesUrl({
                onComplete: function (a) {
                  Array.isArray(a) && a.length > 0 && (a = a[0]);
                  var r = t.get("source") ? t.get("source") : "3DSpace",
                    l = a[r] ? a[r] : a["3DSpace"],
                    c = a["3DSwym"],
                    d = t.identifire ? t.identifire + i : i,
                    u = t.path ? l + t.path : l;
                  if (l) {
                    var h =
                        c ||
                        "https://dsext001-eu1-215dsi0708-3dswym.3dexperience.3ds.com",
                      p = widget.data.parent_x3dAppId;
                    p || (p = widget.data.appId);
                    var m = n.getTenantID(),
                      f = {
                        protocol: "3DXContent",
                        version: "",
                        source: "",
                        widgetId: "",
                        data: {
                          items: [
                            {
                              envId: (m = m || "OnPremise"),
                              serviceId: r,
                              objectId: i,
                              objectType: "",
                              facet: "isSocial",
                            },
                          ],
                        },
                      };
                    console.log(JSON.stringify(f)),
                      console.log(encodeURI(JSON.stringify(f))),
                      (e._socialView = new s({
                        commentProxyToken: { header: "X-DS-CSRFTOKEN" },
                        events: {
                          onMediaClick: function (e) {
                            var i = { annotation: e.data.annotationFile };
                            t.clickOnAnnotationData(i, { clean: !0 });
                          },
                        },
                        "3DSwymUrl": h,
                        subjectURI: d,
                        tenantId: m,
                        serviceURL: u,
                        enableShare: !1,
                        appForcedLinkUri:
                          "/#app:ENOLCMT_AP/content:X3DContentId=" +
                          encodeURIComponent(JSON.stringify(f)),
                      })),
                      e.elements.contentScroll.empty(),
                      e._socialView.inject(e.elements.contentScroll);
                  } else
                    e._raiseNotification(
                      o.get("CannotFindServerAdress"),
                      "error",
                      "SC_FindServer"
                    );
                },
              });
          } else
            UWA.createElement("div", {
              class: "SC_NoItems",
              text: o.get("NoItemSelected"),
            }).inject(e.elements.contentScroll);
        },
        destroyComponent: function () {
          this._socialView && this._socialView.destroy();
        },
        onResize: function () {},
        onRefresh: function () {
          this.updateFacet();
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/form/Form",
    [
      "DS/EditPropWidget/models/CommonAbstract",
      "DS/WidgetServicesUI/helpers/Constants",
      "DS/EditPropWidget/facets/Properties/TweakerManager",
      "DS/EditPropWidget/utils/AttributesUtils",
      "DS/WidgetServices/WidgetServices",
      "DS/WidgetServices/moveHelper/MoveHelper",
      "DS/UIKIT/Tooltip",
      "DS/Controls/TooltipModel",
      "DS/EditPropWidget/models/EditPropAttributeModel",
      "css!DS/EditPropWidget/EditPropWidget.css",
    ],
    function (e, t, i, n, s, a, o, r, l) {
      var c = function (e) {
          var i,
            n,
            s = e.get("tw_type");
          return (
            s ||
              ("owner" === e.get("name")
                ? (s = t.TYPES.PERSON_WUX)
                : "originated" === e.get("name") ||
                  "modified" === e.get("name") ||
                  "branch.originated" === e.get("name") ||
                  "branch.modified" === e.get("name")
                ? (s = t.TYPES.DATETIME_WUX)
                : ((i = e.get("type")),
                  (n = "string" === i || "text" === i ? "string_wux" : i),
                  "integer" === i
                    ? (n = t.TYPES.INTEGER_WUX)
                    : "real" === i
                    ? (n = t.TYPES.NUMBER_WUX)
                    : "timestamp" === i || "date" === i
                    ? (n = t.TYPES.DATE_WUX)
                    : "person" === i
                    ? (n = t.TYPES.PERSON_WUX)
                    : "boolean" === i || "radiobutton" === i || "checkbox" === i
                    ? (n = t.TYPES.BOOLEAN_WUX)
                    : ("enum" !== i &&
                        "listbox" !== i &&
                        "listboxmanual" !== i &&
                        "combobox" !== i) ||
                      (n = t.TYPES.ENUM_WUX),
                  (s = n)),
              e.set("tw_type", s)),
            s
          );
        },
        d = function (e) {
          var i,
            n,
            s = e.get("tw_type");
          return (
            s ||
              ("owner" === e.get("name")
                ? (s = t.TYPES.PERSON)
                : "originated" === e.get("name") ||
                  "modified" === e.get("name") ||
                  "branch.originated" === e.get("name") ||
                  "branch.modified" === e.get("name")
                ? (s = t.TYPES.DATETIME)
                : ((i = e.get("type")),
                  (n = i),
                  "integer" === i
                    ? (n = t.TYPES.INTEGER)
                    : "real" === i
                    ? (n = t.TYPES.NUMBER)
                    : "timestamp" === i || "date" === i
                    ? (n = t.TYPES.DATE)
                    : "person" === i
                    ? (n = t.TYPES.PERSON)
                    : "boolean" === i || "radiobutton" === i || "checkbox" === i
                    ? (n = t.TYPES.BOOLEAN)
                    : ("enum" !== i &&
                        "listbox" !== i &&
                        "listboxmanual" !== i &&
                        "combobox" !== i) ||
                      (n = t.TYPES.ENUM),
                  (s = n)),
              e.set("tw_type", s)),
            s
          );
        },
        u = {},
        h = function (e, t, i) {
          try {
            localStorage.setItem(
              e + "_style" + (i ? "_" + i : ""),
              JSON.stringify(t)
            );
          } catch (e) {
            console.log("Local storage full. Current form size can't be saved");
          }
        },
        p = function (e, t, i) {
          var n = f(e, u[t] ? u[t] : "200", t, i);
          return h(e, n, t), n;
        },
        m = function (e, t) {
          var i = localStorage.getItem(e + "_style" + (t ? "_" + t : "")),
            n = null;
          try {
            (n = JSON.parse(i)).width < 80 && (n.width = 80);
          } catch (e) {}
          return n || (n = p(e, t)), n;
        },
        f = function (e, t, i, n) {
          var s = {};
          return (
            (s["." + e + " .formLabel" + (i ? "." + i : "")] = {
              width: t + "px",
            }),
            (s["." + e + " .resizeHandler" + (i ? "." + i : "")] = {
              left: t - 10 + "px",
            }),
            { style: s, width: t, userAction: n }
          );
        },
        g = function (e, t, i) {
          var n = null;
          return (
            e.forEach(function (e) {
              var a = (function (e, t) {
                var i = m(e, t),
                  n = e + "_style" + (t ? "_" + t : "");
                return s.addNewStyles(n, i.style, !0), i;
              })(e, i);
              t === e && (n = a);
            }),
            n
          );
        },
        y = function (e) {
          if (!e || typeof widget === "undefined" || !widget || !widget.data || "ENXENG_AP" != widget.data.appId) return false;
          //20260720 update by ljr XEN创建页选择库后，库属性显示前跳过客户零件号、客户零件版本号。
          var t = [
              "JFCustomerPartNumber",
              "JFCustomerPartRevision",
              "CustomerPartNumber",
              "CustomerPartRevision",
              "JF_CustomerPartNumber",
              "JF_CustomerPartRevision",
              "客户零件号",
              "客户零件版本",
              "客户零件版本号"
            ],
            i = [];
          e.getIdentifier && i.push(e.getIdentifier());
          i.push(e.name, e.path, e.select, e.label, e.attributeID);
          e.get && ["name", "path", "select", "label", "attributeID"].forEach(function (t) {
            i.push(e.get(t));
          });
          return i.some(function (e) {
            if (!e) return false;
            var i = String(e).replace(/\s|\*/g, "");
            return t.some(function (e) {
              return i === e || i.endsWith("-" + e) || i.indexOf("attribute[" + e + "]") >= 0;
            });
          });
        },
        v = e.extend({
          postBuildTweaker: function () {
            this._tweaker && this._tweaker.postBuildUI(),
              (this._finalizeWhenReady = !0);
          },
          destroy: function () {
            this._parent.apply(this, arguments),
              this._model &&
                this._model.removeEvent(
                  this._idAttr,
                  this._manageMessageHandler
                );
          },
          init: function (e, t, i, n) {
            this._parent(n),
              (this._idAttr = i.getIdentifier()),
              (this._model = t),
              (this._form = e),
              (this._tweaker = null),
              (this._finalizeWhenReady = !1),
              (this._manageMessageHandler = this._manageMessage.bind(this));
            var s = this;
            !0 !== n.postBuild &&
              this.subscribeEvt("triggerPostBuild", function () {
                s.postBuildTweaker();
              }),
              (this.elements.container = UWA.createElement("div", {
                class:
                  "formLine s12 m12" +
                  (i.get("width") ? " l" + i.get("width") : " l12"),
              })),
              (this.elements.flexLine = UWA.createElement("div", {
                class: "flexLine",
              }).inject(this.elements.container));
            var a = this._form._sharedOptions.identifier
                ? "formLabel " + this._form._sharedOptions.identifier
                : "formLabel",
              o = UWA.createElement("div", { class: a + " s12" }).inject(
                this.elements.flexLine
              ),
              l = !0 !== this.options.readOnly && i.get("mandatory"),
              u = UWA.createElement("div", {
                class: "attrLabel" + (l ? " mandAttr" : ""),
              }).inject(o),
              h = UWA.createElement("span", { class: "textContainer" }).inject(
                u
              );
            (UWA.createElement("span", { text: i.get("label") }).inject(
              h
            ).tooltipInfos = new r({
              shortHelp: i.get("label"),
              mouseRelativePosition: !0,
            })),
              !0 === l &&
                UWA.createElement("div", {
                  class: "attrMand",
                  html: " *",
                }).inject(u);
            var p = this._idAttr,
              m = UWA.createElement("div", { class: "formValue s12" }).inject(
                this.elements.flexLine
              ),
              f = UWA.createElement("div", {
                class: "formTweakerContainer",
                id: p,
              }).inject(m),
              g = t.propertiesWidgetUI || t.webUxTweakers ? c(i) : d(i),
              v = {};
            this.options.readonly && (v.readOnly = this.options.readOnly),
              (v.postBuild = this.options.postBuild),
              !t.propertiesWidgetUI ||
              t.webUxTweakers ||
              ("string" !== g && "string_wux" !== g)
                ? (v.forClickable = !1)
                : (v.forClickable = !0),
              this._form.tweakerManager.getTweaker(
                t,
                this._idAttr,
                g,
                v,
                function (e) {
                  e.inject(f),
                    (s._tweaker = e),
                    s._finalizeWhenReady && s.postBuildTweaker(),
                    s.dispatchEvent("tweakerReady:" + s._idAttr, [e]),
                    ("string" !== i.tw_type && "string_wux" !== i.tw_type) ||
                      i.multiline ||
                      s._tweaker.addEvent("enterKeyDown", function () {
                        t.dispatchEvent("onEnterKeyDown", [i]);
                      });
                }
              ),
              t.addEvent(
                "onChange:" + this._idAttr,
                this._manageMessageHandler
              ),
              this._manageMessage();
          },
          showNotificationOnField: function (e) {
            if (
              (this.elements.errorContent &&
                this.elements.errorContent.setText(e.message ? e.message : ""),
              e.message)
            )
              e.type && e.type;
            else this.elements.flexLine.removeClassName("errorOnField");
          },
          _manageMessage: function () {
            var e = this._model.get(this._idAttr),
              t =
                this._model.webUxTweakers || this._model.propertiesWidgetUI
                  ? c(e)
                  : d(e);
            e.validationFunction =
              this._form.tweakerManager.getValidationFunction(t);
            var i = n.validateAttributes(this._model, [e]);
            e && e.error && this.showNotificationOnField(e.error),
              i &&
                i.length > 0 &&
                (!this._tweaker || !0 === this._tweaker.underEdition) &&
                this.publishEvt("onNotifications", i);
          },
          focusTweakerElement: function () {
            if (this._tweaker) {
              var e = this._tweaker;
              e && UWA.is(e.focus, "function") && e.focus();
            } else
              this.addEventOnce("tweakerReady:" + this._idAttr, function (e) {
                e && UWA.is(e.focus, "function") && e.focus();
              });
          },
        }),
        _ = null,
        E = null;
      return e.extend({
        defaultOptions: {
          width: 12,
          label: null,
          classname: "large",
          postBuild: !0,
        },
        _sharedOptions: null,
        isReadOnly: function () {
          var e = this._sharedOptions;
          return !e || !0 === e.readOnly;
        },
        init: function (e, t) {
          this._parent(e), (this._sharedOptions = t);
          var n = this;
          (this.elements.container = UWA.createElement("fieldset", {
            class: "formContainer s12 m12 l" + this.options.width,
          })),
            (this.fields = []),
            (this.resizeHandlerClass = this._sharedOptions.identifier
              ? "resizeHandler " + this._sharedOptions.identifier
              : "resizeHandler");
          var o = UWA.createElement("div", {
              class: this.resizeHandlerClass,
            }).inject(this.elements.container),
            r = null,
            l = null;
          (this.elements.moveHelpers = new a({
            element: o,
            events: {
              startMove: function () {
                l = null;
                var e = m(n.getClassResize(), n._sharedOptions.identifier);
                r = e.width;
              },
              move: function (e) {
                if (((l = null), e.diff.x)) {
                  var t = r - e.diff.x;
                  t < 80
                    ? (t = 80)
                    : n.maxColWidth && t > n.maxColWidth && (t = n.maxColWidth);
                  var i = n._sharedOptions.identifier;
                  (l = f(n.getClassResize(), t, i)),
                    s.addNewStyles(
                      n.getClassResize() + "_style" + (i ? "_" + i : ""),
                      l.style,
                      !0
                    );
                }
              },
              endMove: function () {
                if (l && l.width) {
                  l.userAction = !0;
                  h(n.getClassResize(), l, n._sharedOptions.identifier);
                  n._publishResizeCell(l);
                }
                l = null;
              },
            },
          })),
            e.label &&
              UWA.createElement("legend", { text: e.label }).inject(
                this.elements.container
              ),
            (this.elements.content = UWA.createElement("div", {
              class: "formContent",
            }).inject(this.elements.container)),
            this.subscribeEvt("editionSwitched", function (e) {
              n.switchTweakersStatus();
            }),
            this.subscribeEvt("innerSwitch", function (e) {
              if (((n.options.sizeClassname = e), E !== this.index)) {
                E = this.index;
                var t = m(e, n._sharedOptions.identifier);
                n._publishResizeCell(t);
              }
            }),
            this.subscribeEvt("resetDisplay", function () {
              _ !== this.index && ((_ = this.index), n.resetDisplay());
            }),
            this.addEvent("resetDisplay1", function () {
              var e = n.elements.content.getSize();
              if ((n.idealLabelWidth || n.updateResizeLimit(), e.width)) {
                n.maxColWidth = e.width - 287;
                var t = m(n.getClassResize(), n._sharedOptions.identifier);
                t && t.width && t.userAction
                  ? ((u[n._sharedOptions.identifier] = t.width),
                    n.maxColWidth < 80
                      ? ((u[n._sharedOptions.identifier] = 80),
                        n.resetDisplay(["medium", "large"], t.userAction))
                      : e.width - t.width < 287 &&
                        ((u[n._sharedOptions.identifier] = n.maxColWidth),
                        n.resetDisplay(["medium", "large"], t.userAction)))
                  : ((u[n._sharedOptions.identifier] = n.idealLabelWidth),
                    n.resetDisplay(["medium", "large"]));
              }
            }),
            (this.tweakerManager = new i(
              {
                editable: !0 === this._sharedOptions.editMode,
                onNotification: function (e) {
                  n.publishEvt("onNotifications", e);
                },
              },
              this._sharedOptions
            )),
            this._manageEditionStatus(),
            !0 === this.options.automaticCSS && this.updateCSS();
        },
        updateCSS: function () {
          var e = g(
            this.options.classnames,
            this.options.sizeClassname,
            this._sharedOptions.identifier
          );
          this._publishResizeCell(e);
        },
        _publishResizeCell: function (e) {
          var t = null;
          e &&
            ((t = (function (e, t, i) {
              var n = {};
              return (
                e.forEach(function (e) {
                  var s = m(e, i);
                  t === e && (s.current = !0), (n[e] = s);
                }),
                n
              );
            })(
              this.options.classnames,
              this.options.sizeClassname,
              this._sharedOptions.identifier
            )).userAction = e.userAction),
            this.publishEvt("onResizeCells", t);
        },
        updateResizeLimit: function () {
          var e = this;
          e.fields &&
            e.fields.length &&
            ((e.idealLabelWidth = 0),
            e.fields.forEach(function (t) {
              t.elements.flexLine
                .getElementsByClassName("formLabel")[0]
                .setStyles({ width: "auto" });
              var i = t.elements.flexLine
                .getElementsByClassName("formLabel")[0]
                .getSize();
              i &&
                i.width &&
                e.idealLabelWidth < i.width &&
                (e.idealLabelWidth = i.width),
                t.elements.flexLine
                  .getElementsByClassName("formLabel")[0]
                  .setStyles({ width: "" });
            }));
        },
        resetDisplay: function (e, t) {
          var i = e || this.options.classnames;
          i || (i = [this.getClassResize()]);
          var n = this;
          i.forEach(function (e) {
            var i = p(e, n._sharedOptions.identifier, t),
              a =
                e +
                "_style" +
                (n._sharedOptions.identifier
                  ? "_" + n._sharedOptions.identifier
                  : "");
            s.addNewStyles(a, i.style, !0);
          }),
            this._publishResizeCell(null);
        },
        getClassResize: function () {
          return this.options.sizeClassname;
        },
        _manageEditionStatus: function () {
          var e =
            !1 === this.isReadOnly() && !0 === this._sharedOptions.editMode;
          e
            ? this.elements.container.addClassName("underEdition")
            : this.elements.container.removeClassName("underEdition"),
            this.tweakerManager.switchMode(e);
        },
        destroy: function () {
          this.clear(),
            this.fields.forEach(function (e) {
              e.destroy();
            }),
            this._parent.apply(this, arguments);
        },
        switchTweakersStatus: function () {
          this._manageEditionStatus();
        },
        _clearAllFields: function () {
          this.tweakerManager.clear();
        },
        _actionOnScroll: function () {
          this.tweakerManager.actionOnScroll();
        },
        clear: function () {
          this._clearAllFields(), this.elements.content.empty();
        },
        setModel: function (e, t) {
          var i = this;
          this.clear();
          var n = null != t && t.attrs ? t.attrs : e.getAllAttributes(),
            a = [];
          for (var o in n) {
            var r = n[o];
            r instanceof l && a.push(r);
          }
          a.sort(function (e, t) {
            return s.compareValue(t.get("positionUI"), e.get("positionUI"));
          }),
            a.forEach(function (t) {
              if (y(t)) return;
              i._addLine(e, t);
            });
        },
        _addLine: function (e, t) {
          this.options.readOnly = this.isReadOnly();
          var i = new v(this, e, t, this.options);
          this.fields.push(i), i.inject(this.elements.content);
        },
        toggleLineById: function (e, t) {
          if (this.fields) {
            var i = this.fields.find((t) => t._idAttr === e);
            if (i) return i.getContent().toggle(t), !0;
          }
        },
        focusLine: function (e) {
          this.fields[e].focusTweakerElement();
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/form/FormOrganizer",
    [
      "DS/EditPropWidget/models/CommonAbstract",
      "DS/EditPropWidget/models/EditPropAttributeModel",
      "DS/WidgetServices/WidgetServices",
      "DS/EditPropWidget/form/Form",
    ],
    function (e, t, i, n) {
      return e.extend({
        _sharedOptions: null,
        init: function (e, t) {
          this._parent(e),
            (this._display = null),
            (this._sharedOptions = t),
            (this._forms = []),
            (this.elements.container = UWA.createElement("div", {
              class:
                "formOrganizer " +
                (this.options.className ? this.options.className : ""),
            }));
        },
        destroy: function () {
          this.clear(), this._parent.apply(this, arguments);
        },
        clear: function () {
          this._forms.forEach(function (e) {
            e.destroy();
          }),
            (this._forms = []);
        },
        _callOnForms: function (e, t) {
          this._forms.forEach(function (i) {
            i[e].apply(i, t);
          });
        },
        setModel: function (e, t) {
          var i = e.getAllAttributes();
          this.setAttributes(e, i, t);
        },
        setAttributes: function (e, s, a) {
          var o = this;
          this.clear();
          var r = {},
            l = [];
          for (var c in s) {
            var d = s[c];
            if (d instanceof t && !0 !== d.isTransient) {
              var u = d.get("form");
              u || (u = "DSDefault");
              var h = r[u];
              if (!h) {
                var p = a ? a[u] : null;
                (h = { list: [], name: u }),
                  p &&
                    ((h.group = p),
                    void 0 !== p.position && (h.position = p.position)),
                  l.push(h),
                  (r[u] = h);
              }
              h.list.push(d);
            }
          }
          l.sort(function (e, t) {
            n = null;
            var n = void 0 === e.position && void 0 !== t.position ? 1 : null;
            return (n =
              null ===
              (n =
                null ===
                  (n =
                    null === n && void 0 === t.position && void 0 !== e.position
                      ? -1
                      : n) &&
                void 0 !== t.position &&
                void 0 !== e.position
                  ? e.position - t.position
                  : n)
                ? i.compareValue(t.name, e.name)
                : n);
          });
          var m = ++window.top.perfoIncrem;
          window.top.performance.mark("start_buildForm:" + m),
            l.forEach(function (t) {
              var i = UWA.clone(o.options);
              if (t.group) for (var s in t.group) i[s] = t.group[s];
              i.automaticCSS = !1;
              var a = new n(i, o._sharedOptions);
              a.setModel(e, { attrs: t.list }),
                a.inject(o.elements.container),
                o._forms.push(a);
            }),
            o._forms.length > 0 && o._forms[0].updateCSS(),
            window.top.performance.mark("end_buildForm:" + m);
        },
        focusLine: function (e, t) {
          var i = this;
          setTimeout(function () {
            i._forms[e].focusLine(t);
          }, 0);
        },
        toggleFormLine: function (e, t) {
          this._forms &&
            this._forms.forEach(function (i) {
              i.toggleLineById(e, t);
            });
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/form/FormSections",
    [
      "DS/EditPropWidget/models/CommonAbstract",
      "DS/EditPropWidget/models/EditPropAttributeModel",
      "DS/EditPropWidget/form/FormOrganizer",
      "DS/EditPropWidget/comm/EventManager",
      "DS/WidgetServices/WidgetServices",
      "DS/UIKIT/Accordion",
      "DS/Controls/Accordeon",
      "DS/EditPropWidget/constants/EditPropConstants",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s, a, o, r, l) {
      var c = r.ACCORDION.ATTRIBUTES,
        d = l.get("accordion_attributes"),
        u = l.get("accordion_parameters");
      return e.extend({
        _sharedOptions: null,
        defaultOptions: { showSections: !0, sizeClassname: "large" },
        init: function (e, t) {
          var i = this;
          this._parent.apply(this, arguments),
            (this.options.classnames = ["large", "medium", "small"]),
            (this.openedExpanderItemIndex = []),
            (this._sharedOptions = t),
            (this.elements.accordion = new o({
              items: [],
              style: "simple",
              exclusive: !1,
            })),
            this.elements.accordion
              .getContent()
              .addEventListener("expand", function (e) {
                n.publish("onTableOpened", e.options.expanderTarget.domId);
                var t = { expanderItem: e, action: "opened" };
                i.getIndexesOfOpenedOrSelectedAccordeonItems(t);
              }),
            this.elements.accordion
              .getContent()
              .addEventListener("collapse", function (e) {
                var t = { expanderItem: e, action: "closed" };
                i.getIndexesOfOpenedOrSelectedAccordeonItems(t);
              }),
            (this.elements.container = UWA.createElement("div", {
              class:
                this._getDefaultClassname() + " " + this.options.sizeClassname,
            })),
            this.elements.container.addEvent("resize", function () {
              console.log("resize section");
            }),
            this.elements.accordion.inject(this.elements.container),
            this.subscribeEvt("switchDisplay", function (e) {
              i._switchDisplay(e) ||
                i.publishEvt("innerSwitch", i.options.sizeClassname);
            }),
            this.subscribeEvt("switchWrap", function () {
              i._switchWrap();
            }),
            (this.elements._organizers = {}),
            (this._accordionProperties = []),
            (this._openedSections = [c]);
        },
        _switchWrap: function () {
          this.options.titleWrap
            ? this.elements.container.removeClassName("formWrap")
            : this.elements.container.addClassName("formWrap"),
            (this.options.titleWrap = !this.options.titleWrap);
        },
        _getDefaultClassname: function () {
          var e = "";
          return (
            s.isTouchDevice() && (e = "touchDevice "),
            !0 === this.options.titleWrap && (e += "formWrap"),
            "formSections " + e
          );
        },
        _switchDisplay: function (e) {
          var t = this._display === e;
          return t || ((this._display = e), this._checkSize()), t;
        },
        _checkSize: function () {
          var e = this.elements.container.getSize().width,
            t = this._getDefaultClassname();
          ((null === this._display && e < 500) || "s" === this._display) &&
            (this.options.sizeClassname = "small"),
            ((null === this._display && e < 1e3 && e > 500) ||
              "m" === this._display) &&
              (this.options.sizeClassname = "medium"),
            ((null === this._display && e > 1e3) || "l" === this._display) &&
              (this.options.sizeClassname = "large"),
            (t += this.options.sizeClassname),
            (this.elements.container.className = t);
        },
        inject: function () {
          var e = this;
          this._parent.apply(this, arguments),
            setTimeout(function () {
              e._checkSize();
            });
        },
        getOrganizers: function () {
          return this.elements._organizers;
        },
        setAccordionProperties: function (e) {
          this._accordionProperties = e;
        },
        _callOnOrganizers: function (e, t) {
          for (var i in this.elements._organizers) {
            var n = this.elements._organizers[i];
            n[e].apply(n, t);
          }
        },
        openSections: function (e) {
          var t = this;
          e.forEach(function (e) {
            t.openSection(e);
          });
        },
        openSection: function (e) {
          var t = this.findExpanderIndexFromId(e);
          this.elements.accordion.expandItem(t);
        },
        destroy: function () {
          this.clear(), this._parent.apply(this, arguments);
        },
        clear: function () {
          this.reset();
        },
        setModel: function (e, i) {
          var n = this;
          this.clear(), (this._model = e), (this._groups = i);
          var a = e.getAllAttributes(),
            o = {},
            r = [];
          for (var l in a) {
            var h = a[l];
            if (h instanceof t && !0 !== h.isTransient) {
              var p = c,
                m = h.get("facet"),
                f = h.get("section"),
                g = h.get("extension");
              (m = m || f), (n.options.showSections || m === u) && (p = m || c);
              var v = o[p];
              if (!v) {
                var _ = n._accordionProperties[p];
                if (!((_ = _ || {}) && _.name && _.nameNLS && _.icon)) {
                  var E = n._accordionProperties[g];
                  E && (_.icon = E.icon),
                    (_.name = p),
                    (_.nameNLS = f || (p === c ? d : p));
                }
                (n._accordionProperties[p] = _),
                  "Default" !== h.group &&
                    ((_.name = h.group),
                    (_.nameNLS = h.groupNLS),
                    (_.weight = h.groupWeight)),
                  _.weight || (_.weight = 0),
                  (v = { list: [], name: p, section: _ }),
                  r.push(v),
                  (o[p] = v);
              }
              v.list.push(h);
            }
          }
          r.sort(function (e, t) {
            var i =
              e.section.weight === t.section.weight
                ? null
                : e.section.weight < t.section.weight
                ? 1
                : -1;
            return (i = null === i ? s.compareValue(t.name, e.name) : i);
          });
          var C = ++window.top.perfoIncrem;
          window.top.performance.mark("start_buildSections:" + C),
            r.forEach(function (e) {
              n.addSection(e);
            }),
            window.top.performance.mark("end_buildSections:" + C),
            this.openSections(this._openedSections);
        },
        addSection: function (e, t) {
          var i = e.section,
            n = i,
            s = i;
          if (("object" == typeof i && (n = i.name), t)) {
            var a = this.findExpanderIndexFromId(n);
            if (!!this.elements.accordion.getExpanderFromIndex(a)) return;
          }
          var o = this._accordionProperties[n],
            r = null;
          o &&
            (t || (o.nameNLS && (s = o.nameNLS)),
            o.icon &&
              (r =
                "classif" === o.icon
                  ? UWA.createElement("span", {
                      class: "fonticon fonticon-flow-tree",
                      title: l.get("EP_icon_classification"),
                    })
                  : UWA.createElement("img", { src: o.icon, width: "10px" })));
          var c = UWA.createElement("span", { text: s });
          r && r.inject(c);
          var d = s ? s.replace(/ /g, "_") : "";
          this._addSection({
            expandedFlag: s !== u && this.options.sectionAutoexpand,
            name: n,
            attrs: e.list,
            title: c,
            orgClassName: "formSection_" + d,
          });
        },
        reset: function () {
          for (var e in this.elements._organizers) {
            var t = this.elements._organizers[e];
            t.remove(), t.destroy(), delete this.elements._organizers[e];
            var i = this.findExpanderIndexFromId(e);
            this.elements.accordion.deleteItem(i);
          }
        },
        _addSection: function (e) {
          if (e && e.name) {
            var t = e.name,
              n = this.elements._organizers[t];
            if (!n) {
              var s = UWA.clone(this.options);
              (s.className = e.orgClassName),
                (n = new i(s, this._sharedOptions)),
                (this.elements._organizers[t] = n),
                (e.content = n.getContent());
              var a = this.elements.accordion.addItem(e),
                o = this.elements.accordion.getExpanderFromIndex(a);
              (o.domId = e.name),
                e.title &&
                  e.title.textContent &&
                  (o.header = e.title.textContent);
            }
            n.setAttributes(this._model, e.attrs, this._groups);
          }
          this._manageAccordionVisibility();
        },
        toggleFormOrgLine: function (e, t) {
          var i = this.elements._organizers;
          if (i)
            for (var n in i) {
              i[n].toggleFormLine(e, t);
            }
        },
        _manageAccordionVisibility: function () {
          this.elements.accordion.items.length <= 1
            ? this._hideAccordion()
            : 2 == this.elements.accordion.items.length &&
              this._showAccordion();
        },
        _hideAccordion: function () {
          var e = this.elements.accordion.getExpanderFromIndex(0);
          if (e) {
            var t = this.elements.accordion.elements.container.parentElement,
              i = this.elements._organizers[e.domId];
            t && i.inject(t);
          }
          this.elements.accordion.hide();
        },
        _showAccordion: function () {
          var e = this.elements.accordion.getExpanderFromIndex(0);
          e &&
            this.elements._organizers[e.domId].inject(e.elements.bodyContainer);
          this.elements.accordion.show();
        },
        findExpanderIndexFromId: function (e) {
          for (
            var t = 0;
            t < this.elements.accordion._expanderItems.length;
            t++
          )
            if (this.elements.accordion._expanderItems[t].domId === e) return t;
          return -1;
        },
        getIndexesOfOpenedOrSelectedAccordeonItems: function (e) {
          var t = e.expanderItem.options.expanderTarget.domId,
            i = e.action;
          if ("opened" === i) {
            for (var n = 0; n < this._openedSections.length; n++)
              if (t && this._openedSections[n] === t) return;
            t && this._openedSections.push(t);
          } else if ("closed" === i) {
            var s = this._openedSections.filter((e) => e !== t);
            this._openedSections = s;
          }
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/facets/Properties/Properties",
    [
      "UWA/Class",
      "DS/EditPropWidget/models/CommonAbstract",
      "UWA/Json",
      "UWA/Array",
      "DS/WidgetServices/WidgetServices",
      "DS/EditPropWidget/form/FormSections",
      "DS/EditPropWidget/comm/EventManager",
      "DS/EditPropWidget/facets/Properties/EditPropBase",
      "DS/EditPropWidget/facets/Properties/PropertiesUIController",
      "DS/EditPropWidget/constants/EditPropConstants",
      "DS/EditPropWidget/models/EditPropAttributeModel",
      "DS/UIKIT/Input/Button",
      "DS/UIKIT/Scroller",
      "DS/WidgetServicesUI/tweakers/TweakerWUXButton",
      "css!DS/EditPropWidget/EditPropWidget.css",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s, a, o, r, l, c, d, u, h, p, m, f) {
      "use strict";
      f.get("accordion_attributes"), f.get("accordion_parameters");
      window.top.perfoIncrem = window.top.perfoIncrem
        ? window.top.perfoIncrem
        : 0;
      return e.extend(t, r, {
        _previousScrollerPosition: null,
        defaultOptions: {
          setRefreshButton: !1,
          setValidButton: !0,
          setCloseButton: !0,
          setEditButton: !1,
          showTopBar: !0,
          editMode: !1,
          showSections: !0,
          sectionAutoexpand: !1,
          readOnly: !0,
          scroller: !0,
          displayType: c.STANDARD,
          updateSession: !1,
        },
        init: function (e) {
          require(["DS/TransientWidget/TransientWidget"]),
            this._parent.apply(this, arguments),
            (this.wiwListModels = null),
            (this.handlerTouchEnd = function (e) {
              Date.now();
            }.bind(this)),
            (this.handlerDblClick = null),
            (this.handlerAttributeChanged = function () {
              this._ManageValidButton();
            }.bind(this)),
            (this.handlerLabelChanged = function () {
              this._firstModel &&
                this.dispatchEvent(
                  "onTitleChanged",
                  this._firstModel.get("label")
                );
            }.bind(this)),
            (this._firstModel = null),
            (this._previousScrollerPosition = null),
            (this.lastPointedTableCellObj = null),
            (this._sharedOptions.editMode = !0 === e.editMode),
            (this._sharedOptions.identifier = e.identifier),
            e.webUxTweakers && (this.webUxTweakers = e.webUxTweakers),
            e.propertiesWidgetUI &&
              (this.propertiesWidgetUI = e.propertiesWidgetUI),
            (this.elements.UIController = new l([])),
            (this.isReadOnly = this.isReadOnly());
          var t = this;
          this.subscribeEvt("onResizeCells", this._onCellResized.bind(this)),
            this.subscribeEvt("onNotifications", function (e) {
              t.setError(e);
            }),
            this.initUI();
        },
        _onCellResized: function (e) {
          var t = null,
            i = !1;
          if (e) {
            var n = e.medium;
            i = !0 !== (t = n && !0 === n.current ? n : e.large).current;
          }
          var s = !1;
          if (!i) {
            var a = c.MIN_WIDTH_FOR_TABLE;
            e && (a = parseInt(t.width)),
              (s = c.VARIABLES.minWidthForTable !== a),
              (c.VARIABLES.minWidthForTable = a);
          }
          e && !0 === s && this.dispatchEvent("refreshActions"),
            this._updateUI();
        },
        setAccordionProperties: function (e) {
          this.elements.formSection.setAccordionProperties(e);
        },
        isReadonly: function () {
          return !1 !== this.isReadOnly;
        },
        clear: function () {
          this._clearAllFields();
        },
        _clearAllLine: function () {
          this.elements.formSection.clear();
        },
        _getModel: function (e) {
          return this.elements.UIController.getModel(e);
        },
        initData: function (e, t) {
          this.resetUIValues(),
            this.elements.UIController.setModels([]),
            this._clearAllLine();
          var i = this._sharedOptions.editMode;
          if (t)
            switch (t.edition) {
              case c.FORCE_READWRITE:
                i = !0;
                break;
              case c.FORCE_READONLY:
                i = !1;
                break;
              case c.FORCE_DEFAULT:
                i = !0 === this.options.editMode;
            }
          if (
            (this._sharedOptions.editMode !== i && this._switchEditionMode(),
            !0 === this.options.updateSession)
          ) {
            if ("undefined" != typeof dscef) {
              this.wiwListModels = e;
              var n = e[0]._attributes,
                s = [];
              for (var a in n) {
                if (null != n[a]) {
                  var o = n[a].value,
                    r = n[a].valueDB,
                    l = n[a].readOnly;
                  null != o &&
                    null != l &&
                    null != r &&
                    s.push({
                      attribute: a,
                      value: n[a].value,
                      valueDB: n[a].valueDB,
                      readOnly: n[a].readOnly,
                    });
                }
              }
              var d = JSON.stringify(s);
              dscef.sendString(
                '{"operation":"getData","operationVersion":"1.0","attributes":' +
                  d +
                  "}"
              );
            }
          } else this.addData(e);
        },
        addData: function (e) {
          this._addData(e);
        },
        changeAttributes: function (e) {
          var t = JSON.parse(e);
          if (null != this.wiwListModels && null != e) {
            for (var i = 0; i < t.attributes.length; i++) {
              var n = t.attributes[i],
                s = n.attribute,
                a = n.value,
                o = n.valueDB,
                r = n.readOnly;
              null != a &&
                null != o &&
                null != r &&
                (this.wiwListModels[0]._attributes[s].value != a &&
                  ((this.wiwListModels[0]._attributes[s].value = a),
                  (this.wiwListModels[0]._attributes[s].initialsValues.value =
                    a)),
                this.wiwListModels[0]._attributes[s].valueDB != o &&
                  ((this.wiwListModels[0]._attributes[s].valueDB = o),
                  (this.wiwListModels[0]._attributes[s].initialsValues.valueDB =
                    o)),
                this.wiwListModels[0]._attributes[s].readOnly != r &&
                  (this.wiwListModels[0]._attributes[s].readOnly = r));
            }
            this.addData(this.wiwListModels);
          }
        },
        _getScrollerContent: function () {
          return this.elements.scroller
            ? this.elements.scroller.elements.content
            : null;
        },
        _scrollIsBottom: function (e) {
          var t = !1;
          if (e) {
            var i = e.scrollHeight,
              n = e.getDimensions();
            t = 0 !== e.scrollTop && e.scrollTop > i - n.height;
          }
          return t;
        },
        _getScrollerPosition: function () {
          var e = this._getScrollerContent();
          return e ? (this._scrollIsBottom(e) ? -1 : e.scrollTop) : null;
        },
        _setScrollerPosition: function (e) {
          var t = this._getScrollerContent();
          t && (t.scrollTop = -1 === e ? t.scrollHeight : e);
        },
        _saveScrollerPosition: function () {
          this._previousScrollerPosition = this._getScrollerPosition();
        },
        _restoreScrollerPosition: function () {
          null !== this._previousScrollerPosition &&
            this._setScrollerPosition(this._previousScrollerPosition),
            (this._previousScrollerPosition = null);
        },
        onResize: function () {},
        _addData: function (e) {
          var t = ++window.top.perfoIncrem;
          window.top.performance.mark("start_LoadUI:" + t);
          var i = this;
          this._ManageValidButton(!1),
            null != e &&
              e.length > 0 &&
              this.elements.UIController.setModels([e[0]]);
          var n = this.elements.UIController.getModels();
          if (n.length <= 0) this._clearAllLine();
          else {
            var a = n[0];
            (this._firstModel = a),
              a.addEvent("onChange:label", this.handlerLabelChanged),
              a.addEvent("onChange", this.handlerAttributeChanged),
              this.updateTableManager(),
              this.onResize(),
              this._ManageValidButton(),
              this.endOfLoad();
          }
          window.top.performance.mark("end_LoadUI:" + t),
            this.dispatchEvent("onChangeSize"),
            this.dispatchEvent("onTitleChanged"),
            this.elements.UIController.validateModels(
              this.elements.UIController.getModels()
            ),
            (window.top.readPerfo = function (e, t) {
              console.log("computing..."),
                require([
                  "text!DS/EditPropWidget/assets/noNls/perfos.json",
                ], function (i) {
                  for (
                    var n = JSON.parse(i),
                      a = [],
                      o = window.top.performance.getEntriesByType("mark"),
                      r = {},
                      l = "",
                      c = 0;
                    c < o.length;
                    c++
                  ) {
                    var d = o[c].name,
                      u = o[c].startTime,
                      h = d.replace("start_", "").replace("end_", ""),
                      p = h,
                      m = null;
                    if (d !== h) {
                      a.push(d);
                      var f = h.split(":");
                      (p = f[0]), f.length > 0 && (m = f[1]);
                    }
                    if (r[h]) {
                      var g = u - r[h].startTime;
                      if (
                        ((r[h].endTime = u),
                        (r[h].duration = g),
                        window.console)
                      ) {
                        var v = (n[p] ? n[p] : p + " [ NO NLS ]").toUpperCase();
                        m && (v += " :increm: " + m),
                          !0 === e &&
                            (v +=
                              " -- specificName: " +
                              h +
                              " -- Entry Type: " +
                              o[c].entryType +
                              " -- Start Time: " +
                              r[h].startTime +
                              " -- End Time: " +
                              u),
                          (v += " -- Duration: " + g + "ms \n"),
                          console.log(v),
                          (l += v);
                      }
                    } else r[h] = { startTime: u };
                  }
                  a.forEach(function (e) {
                    window.top.performance.clearMarks(e);
                  }),
                    !0 !== t && s.download("perfo", l, "text", !1);
                });
            }),
            console.log(
              "%creadPerfo() to read Properties the performances",
              "color:blue;font-weight:bold;"
            ),
            s.isDebug() &&
              setTimeout(function () {
                window.top.readPerfo(!0, !0);
              }, 2e3),
            window.top.performance.mark("end_EP_DataDisplay"),
            setTimeout(function () {
              i._restoreScrollerPosition(),
                window.top.performance.mark("end_EP_FullTime");
            }),
            this.elements.formSection._manageAccordionVisibility();
        },
        endOfLoad: function () {
          var e = this;
          this.dataLoadedOnce ||
            setTimeout(function () {
              (e.dataLoadedOnce = !0), e.dispatchEvent("onDataLoaded");
            }, 0);
        },
        _isCloseButton: function () {
          return c.isCloseButton(this.options.displayType);
        },
        show: function () {
          this.getUI().show();
        },
        setOption: function (e, t) {
          this._parent.apply(this, arguments),
            (this.elements.formSection.options[e] = t);
        },
        resetDisplay: function () {
          this.publishEvt("resetDisplay");
        },
        initUI: function () {
          var e = ++window.top.perfoIncrem;
          window.top.performance.mark("start_EP_InitUI:" + e);
          var t = this;
          if (
            ((this.elements.mainDiv = this.elements.container =
              UWA.createElement("div", {
                class: "EditProp_MainContain includeMenuBar",
              })),
            (!0 !== this.isReadOnly && !0 === this._sharedOptions.editMode) ||
              this.elements.container.addClassName("EP_ReadOnly"),
            (this.ManageButtonDiv = UWA.createElement("div", {
              class: "EditProp_ManageButtonDiv",
            })),
            (t.ManageButtonTopRight = UWA.createElement("td", {
              class: "EditProp_ManageButtonTopRight",
            })),
            !this._isCloseButton())
          ) {
            var i = {
              label: f.get("EP_Cancel"),
              className: "EditProp_buttonTop EditProp_Cancel TopButtonCancel",
              setAttribute: { name: "TopButtonCancel" },
              clickCallback: function () {
                "undefined" != typeof dscef &&
                  dscef.sendString(
                    '{"operation":"onClose","operationVersion":"1.0"}'
                  ),
                  t.elements.UIController.resetValueOfModels(),
                  t.elements.UIController.validateModels(
                    t.elements.UIController.getModels()
                  ),
                  setTimeout(function () {
                    t._ManageValidButton();
                  }, 0);
              },
            };
            this.elements.boutonCancel = new p(i);
          }
          this.elements.boutonCancel &&
            !0 === t.options.setCloseButton &&
            this.elements.boutonCancel.inject(t.ManageButtonDiv),
            t.ManageButtonTopRight.inject(this.elements.container);
          var n = (this.elements.containerProperties = UWA.createElement(
              "div",
              { class: "EP_ContainerProperties" }
            )),
            r = (this.elements.containerAccordeon = UWA.createElement("div", {
              class: "EP_ScrollerProperties",
            }));
          o.subscribe("onTableOpened", function (e) {
            t._saveScrollerPosition();
          }),
            (this.elements.formSection = new a(
              {
                postBuild: !1,
                sectionAutoexpand: this.options.sectionAutoexpand,
                showSections: this.options.showSections,
                eventThread: this.options.eventThread,
              },
              this._sharedOptions
            )),
            this.elements.formSection.inject(r),
            r.setStyle("overflow-y", "auto"),
            r.inject(n);
          var l = (this.elements.bottomTable = UWA.createElement("div", {
              class: "EditProp_BottomTableContainer",
            })),
            d =
              "true" === s.getWidgetValue(c.COLLAPSE_IC_PREFERENCE.name) &&
              this.options.typeOfDisplay === c.ALL;
          (null !== d && !0 !== d) ||
            s.setWidgetValue(c.COLLAPSE_IC_PREFERENCE.name, "true");
          var u = (this.elements.containerMainTable = UWA.createElement("div", {
            class: "ICTableContainer ",
          }).inject(l));
          n.inject(u),
            l.inject(this.elements.container),
            !0 === this.options.showTopBar &&
              this.ManageButtonDiv.inject(t.elements.container),
            this.elements.formSection._manageAccordionVisibility(),
            window.top.performance.mark("end_EP_InitUI:" + e),
            this.elements.container.addEvent(
              "resize",
              this._updateUI.bind(this)
            );
        },
        _updateUI: function () {
          var e = this.elements.container.getSize();
          e.width && e.width < 381
            ? this.publishEvt("switchDisplay", "s")
            : this.publishEvt("switchDisplay", "m");
        },
        getUI: function () {
          return this.elements.container;
        },
        resetUIValues: function () {
          this.elements.formSection && this.elements.formSection.reset();
        },
        getComponentMode: function () {
          return !0 === this.options.updateSession || this.isReadOnly;
        },
        switchComponentMode: function (e) {
          (this.isReadOnly = e),
            this.handlerDblClick &&
              this.elements.container.removeEvent(
                "dblclick",
                this.handlerDblClick
              ),
            (this.handlerDblClick = null),
            !0 !== e &&
              ((this.handlerDblClick = function () {
                this._switchEditionMode(!0);
              }.bind(this)),
              this.elements.container.addEvent(
                "dblclick",
                this.handlerDblClick
              ));
        },
        addSection: function () {
          this.elements.formSection.addSection.apply(
            this.elements.formSection,
            arguments
          );
        },
        _switchEditionMode: function (e) {
          var t = !1;
          if (
            e !==
            (!1 === this.isReadOnly && !0 === this._sharedOptions.editMode)
          ) {
            if (!1 === this.isReadOnly)
              if (this._sharedOptions.editMode) {
                if (this.elements.UIController.getModifiedModels().length > 0) {
                  var i = f.get(
                    "Some attributes are in edition. Please valid or cancel the modifications to switch"
                  );
                  return (
                    this._isCloseButton() &&
                      (i = f.get(
                        "Some attributes are in edition. Please valid or refresh to switch"
                      )),
                    void this.addWarning(i)
                  );
                }
                (this._sharedOptions.editMode = !1),
                  this._activateAddingButton(!1);
              } else
                (this._sharedOptions.editMode = !0),
                  this._activateAddingButton(!0);
            else this._sharedOptions.editMode = !1;
            t = !0;
          }
          return (
            t &&
              this.publishEvt("editionSwitched", this._sharedOptions.editMode),
            !0 !== this._sharedOptions.editMode
              ? this.elements.container.addClassName("EP_ReadOnly")
              : this.elements.container.removeClassName("EP_ReadOnly"),
            s.setWidgetValue(
              "editProperties_editMode",
              this._sharedOptions.editMode
            ),
            t
          );
        },
        _switchEdition: function (e) {
          this._switchEditionMode(e) && this.onResize(),
            (this.lastPointedTableCellObj = null);
        },
        resizeActionActive: function () {
          return c.VARIABLES.minWidthForTable !== c.MIN_WIDTH_FOR_TABLE;
        },
        getOptimalSize: function () {
          var e = this.elements.masterTable.getSize(),
            t = this.elements.containerProperties.getSize(),
            i = e.height - t.height,
            n = this.elements.formSection.getContent().getSize();
          return (n.height += i + 60), (n.width += 30), n;
        },
        updateTableManager: function () {
          this._saveScrollerPosition(),
            this._clearAllLine(),
            this.updateTable();
        },
        updateTable: function () {
          var e = this;
          this._clearAllFields(),
            this._clearAllLine(),
            e.modelIdentityCard &&
              (e.modelIdentityCard.set("title", ""),
              e.modelIdentityCard.set("ownerName", ""),
              e.modelIdentityCard.set("date", ""),
              e.modelIdentityCard.set("description", ""));
          var t = this.elements.UIController.getModels(),
            i = t.length,
            n = ++window.top.perfoIncrem;
          window.top.performance.mark("start_addInForm:" + n);
          for (var s = 0; s < i; s++) {
            var a = t[s];
            this.webUxTweakers && (a.webUxTweakers = this.webUxTweakers),
              this.propertiesWidgetUI &&
                (a.propertiesWidgetUI = this.propertiesWidgetUI),
              this.elements.formSection.setModel(a);
          }
          window.top.performance.mark("end_addInForm:" + n),
            this._restoreScrollerPosition(),
            setTimeout(function () {
              e.publishEvt("triggerPostBuild");
            });
        },
        saveIsNeeded: function () {
          var e = !0;
          return (
            this.elements.UIController.getModifiedModels().every(function (t) {
              var i = t.previousAttributes();
              for (var n in ("function" == typeof t.getChangedValues &&
                (i = t.getChangedValues()),
              i)) {
                var s = i[n];
                if (s instanceof d && !1 === s.isTransient) {
                  e = !1;
                  break;
                }
              }
              return !1 !== e;
            }),
            !e
          );
        },
        validateModel: function () {
          var e = !1,
            t = this.elements.UIController.getModifiedModels();
          if (t.length > 0) {
            var i = this.elements.UIController.validateModels(t);
            i
              ? ((i.message = f.get("Modification failed") + ":" + i.message),
                this.setError(i))
              : (e = !0);
          }
          return this._ManageValidButton(), e;
        },
        _valueIsChanging: function (e, t) {
          this._ManageValidButton();
          var i = { value: t, valueDB: e.get("valueDB") };
          e.isSameValueSet(i) && this._activateValidButton();
        },
        _activateAddingButton: function (e) {
          this.dispatchEvent("onAddActivation", e);
        },
        _activateValidButton: function (e) {
          var t = !e;
          this.elements.boutonCancel &&
            (t
              ? this.elements.boutonCancel.show()
              : this.elements.boutonCancel.hide()),
            this.dispatchEvent("onValidActivation", t);
        },
        _ManageValidButton: function () {
          this._activateValidButton(!this.saveIsNeeded());
        },
        getButtons: function () {
          return this.ManageButtonDiv;
        },
        getTitle: function () {
          var e = "";
          return (
            this.elements.UIController &&
              (e = this.elements.UIController.getLabel()),
            e
          );
        },
        showNotification: function (e) {
          this.addSuccess(e);
        },
        setError: function (e) {
          var t = this;
          if (n.isArray(e))
            e.forEach(function (e) {
              var i = e && e.customClass ? e.customClass : null;
              t._manageError(e, i);
            });
          else {
            var i = e && e.customClass ? e.customClass : null;
            t._manageError(e, i);
          }
        },
        _manageError: function (e, t) {
          if (e)
            switch (e.eventID ? e.eventID : "error") {
              case "warning":
                this.addWarning(e.message ? e.message : e, t);
                break;
              case "success":
                this.addSuccess(e.message ? e.message : e, t);
                break;
              default:
                this.addError(e, t);
            }
        },
        addWarning: function (e, t) {
          this.dispatchEvent("onNotification", {
            msg: e,
            eventID: "warning",
            customClass: t,
          });
        },
        addSuccess: function (e, t) {
          this.dispatchEvent("onNotification", {
            msg: e,
            eventID: "success",
            customClass: t,
          });
        },
        addInfo: function (e, t) {
          this.dispatchEvent("onNotification", {
            msg: e,
            eventID: "info",
            customClass: t,
          });
        },
        addError: function (e, t) {
          this.dispatchEvent("onNotification", {
            msg: e.message ? e.message : i.encode(e),
            eventID: "error",
            customClass: t,
          });
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/facets/Properties/EditPropController",
    [
      "UWA/Class/Model",
      "UWA/Core",
      "DS/WidgetServices/securityContextServices/SecurityContextServices",
      "DS/WidgetServices/WidgetServices",
      "DS/EditPropWidget/comm/EventManager",
      "DS/EditPropWidget/facets/Common/FacetsBase",
      "DS/EditPropWidget/models/EditPropModel",
      "DS/EditPropWidget/models/EditPropModelMerge",
      "DS/EditPropWidget/models/EditPropAttributeModel",
      "DS/EditPropWidget/ajaxRequests/EditPropAjaxRequests",
      "DS/EditPropWidget/facets/Properties/Properties",
      "DS/EditPropWidget/constants/EditPropConstants",
      "DS/EditPropWidget/constants/ConstantsMapping",
      "DS/EditPropWidget/comm/DistantAjax",
      "DS/EditPropWidget/utils/EditPropUtils",
      "DS/EditPropBaseUX/scripts/EditPropUtility",
      "DS/ENOCollabToolsUI/tracker/ENOCollabTracker",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s, a, o, r, l, c, d, u, h, p, m, f, g, v) {
      "use strict";
      var _ = v.get("accordion_parameters");
      return a.extend({
        defaultOptions: {
          serverName: "",
          serverUri: "",
          displayType: u.STANDARD,
          extraNotif: !1,
          context: null,
        },
        getListHandle: null,
        commitHandle: null,
        previewUrlHandle: null,
        init: function (e) {
          var t = this;
          this._parent.apply(this, arguments),
            (this.getListHandle = { handle: null }),
            (this.commitHandle = { handle: null }),
            (this.previewUrlHandle = { handle: null });
          var i = {};
          for (var n in e) i[n] = e[n];
          (i.events = i.events ? i.events : {}),
            (i.events.onNotification = function (e) {
              t._setError(e.msg ? e.msg : e.json, e.eventID, e.customClass);
            }),
            (i.events.onValidActivation = function (e) {
              t.switchSaveButton(e);
            }),
            (i.events.onAddActivation = function (e) {
              t.switchAddButton(e);
            }),
            (t.parametersLoaded = !1),
            (t.indexModeParameter = !!e.indexModeParameter),
            s.subscribe("onTableOpened", function (e) {
              e != _ ||
                !1 !== t.parametersLoaded ||
                t.indexModeParameter ||
                ((t.parametersLoaded = !0), t._loadPLMParameters());
            }),
            (i.events.refreshActions = function (e) {
              t.refreshActions();
            }),
            this._createUI(i);
        },
        isUIReadonly: function () {
          return this.editPropertiesUI.isReadonly();
        },
        _createUI: function (e) {
          (e.className = this.getTabId()),
            (e.eventThread = this.options.eventThread),
            (this.editPropertiesUI = new d(e, this.getSharedOptions()));
        },
        nothingToSave: function () {
          return !this.editPropertiesUI.saveIsNeeded();
        },
        onSave: function () {
          var e = this.editPropertiesUI.validateModel(),
            t = this.getModel();
          if (e)
            if (!0 === this.options.updateSession) {
              var i = t.changedValues,
                n = [];
              for (var s in i)
                if (null != s) {
                  var a = i[s].value,
                    o = i[s].valueDB;
                  null != a &&
                    null != o &&
                    n.push({ attribute: s, value: a, valueDB: o });
                }
              var r = JSON.stringify(n);
              "undefined" != typeof dscef &&
                dscef.sendString(
                  '{"operation":"onOk","operationVersion":"1.0","attributes":' +
                    r +
                    "}"
                );
            } else this._commitModels([t]);
        },
        getExtensions: function () {
          var e = this,
            t = e.getModel();
          let n = function (i, n) {
            e.showLoader();
            new i(
              widget,
              e._parent,
              e.options,
              n,
              t,
              function () {
                e.hideLoader(), e.onRefresh();
              },
              function (t) {
                e.hideLoader(),
                  require(["DS/PADUtils/views/PADAlert"], function (e) {
                    e.displayNotification({
                      eventID: "string" == typeof type ? type : "info",
                      msg: t,
                    });
                  });
              }
            );
          };
          require(["DS/EditPropExtensions/UI/AddExtensionsWidget"], function (
            s
          ) {
            var a = t.getTenant();
            i.getInstance({ tenant: a }).getValidSecurityContext({
              context: e.options.context,
              onSuccess: function (e) {
                n(s, e);
              },
              onFailure: function (t) {
                e._setError(t);
              },
            });
          }, function () {
            e._setError("AddExtensionWidget do not exist");
          });
        },
        onAction: function (e) {
          e === u.ACTION_SWITCH
            ? this.switchEdition()
            : e === u.ACTION_RESET &&
              (this.editPropertiesUI.resetDisplay(), this.refreshActions());
        },
        isActionAvailable: function (e) {
          return e === u.ACTION_SWITCH
            ? !this.editPropertiesUI.getComponentMode()
            : e !== u.ACTION_RESET ||
                this.editPropertiesUI.resizeActionActive();
        },
        switchEdition: function () {
          this.editPropertiesUI._switchEdition();
        },
        getButtons: function () {
          return this.editPropertiesUI.getButtons();
        },
        _getUI: function () {
          return this.editPropertiesUI;
        },
        onLoad: function () {
          this.clearAllDatas();
        },
        updateFacet: function () {
          let e = this.getModel();
          (e && e.get("indexModeParameter")) || this.onRefresh();
        },
        onRefresh: function () {
          this.loadModels(this.getModel(), {
            parameters: !0 === this.parametersLoaded,
          });
        },
        _cancelListRequest: function () {
          this.getListHandle &&
            this.getListHandle.handle &&
            (this.getListHandle.handle.cancel(),
            (this.getListHandle.handle = null));
        },
        _cancelCommitHandle: function () {
          this.commitHandle &&
            this.commitHandle.handle &&
            (this.commitHandle.handle.cancel(), (this.commitHandle = null));
        },
        _cancelPreviewUrlHandle: function () {
          this.previewUrlHandle &&
            this.previewUrlHandle.handle &&
            (this.previewUrlHandle.handle.cancel(),
            (this.previewUrlHandle = null));
        },
        destroyComponent: function () {
          this._cancelCommitHandle(),
            this._cancelListRequest(),
            this._cancelPreviewUrlHandle(),
            this.clearAllDatas(),
            this._commandProxy && this._commandProxy.destroy();
        },
        clearAllDatas: function () {
          var e = this.getModel();
          e && e.destroy(), this.setModel(null);
        },
        _loadPLMParameters: function () {
          this.loadModels(this.getModel(), { parameters: !0, attributes: !1 });
        },
        loadModels: function (t, i) {
          window.top.performance.mark("start_PrepareAjax");
          var s = this;
          this.showLoader();
          var a = t;
          if (t) {
            var r = h.getServiceForModel(t);
            if (!0 === t.isTransientModel() || (r && !0 === r.modelNeeded))
              this.setModel(a),
                this.editPropertiesUI.switchComponentMode(!0),
                this.trackPropInitUXEvent(t, this.getTabId()),
                this.editPropertiesUI.initData([this.getModel()]),
                this.hideLoader();
            else {
              var d = null;
              t instanceof e
                ? (s.getModel(a.get("objectId")) || a.clearChangedValue(),
                  (d = a.getChangedValues()))
                : (a = new o(t)),
                s.getModel(a.get("objectId")) ||
                  (s.setModel(a), a.clearChangedValue()),
                a.keys().forEach(function (e) {
                  var t = a.get(e);
                  t instanceof l ||
                    "object" != typeof t ||
                    "EditProp_listOfChange" === e ||
                    "label" === e ||
                    e.startsWith(u.MODEL_FACET_INFO_PREFIX) ||
                    e.startsWith(u.MODEL_DS_ATTR) ||
                    a.set(e, new l(t));
                }),
                s._resetModifFlag(a);
              var m = a.get("EditProp_listOfChange");
              if (
                (m && (a.setChangedValues(m), a.unset("EditProp_listOfChange")),
                d && a.setChangedValues(d),
                window.top.performance.mark("end_PrepareAjax"),
                i && !1 === i.loadModel)
              )
                this.setModel(a),
                  this.trackPropInitUXEvent(this.getModel(), this.getTabId()),
                  this.editPropertiesUI.initData([this.getModel()], {
                    properties: !0,
                  });
              else {
                window.top.performance.mark("start_EP_AJAXRequest");
                var f = !1;
                window.top.performance.mark("start_EP_GetFromDistant"),
                  p.getAsyncRead(function (e) {
                    !1 === f &&
                      (e
                        ? (window.top.performance.mark("end_EP_GetFromDistant"),
                          (f = !0),
                          s._cancelListRequest(),
                          s._onCompleteLoadModel(e))
                        : (function () {
                            s._cancelListRequest();
                            var e = n.getCancelObject();
                            s.getListHandle.handle = e;
                            var t = i && !0 === i.parameters,
                              o = i && !1 !== i.attributes,
                              r = {
                                handleRequest: { handle: e },
                                modelList: [a],
                                tenant: a.getTenant(),
                                onComplete: function (i) {
                                  s.hideLoader(),
                                    !1 !== f ||
                                      e.isCancelled() ||
                                      ((f = !0),
                                      console.log("Ajax 2"),
                                      !0 === window.top.isDebug &&
                                        console.log(
                                          "Ajax 2 answered " +
                                            window.top.performance.now()
                                        ),
                                      (s.getListHandle.handle = null),
                                      s._onCompleteLoadModel(i, {
                                        initTable: !t,
                                        buildBehavior: o
                                          ? u.FORCE_DEFAULT
                                          : u.NO_CHANGES,
                                      }));
                                },
                              };
                            (r.plmParams = t),
                              (r.attributes = o),
                              (r.readonly = s.isUIReadonly()),
                              (r.navigateToMain =
                                s.getTabId() === u.FACET_PROPERTIES),
                              (r.ignoreKernelAccessChecks =
                                !!s.options.ignoreKernelAccessChecks),
                              c.getListOfAttributes(r);
                          })());
                  });
              }
            }
          } else
            this.editPropertiesUI.clear(),
              this._onCompleteLoadModel({ results: [] });
        },
        hide: function () {
          this._parent(), this.editPropertiesUI.clear();
        },
        _commitModels: function (e) {
          var t = this;
          //复制一下好调用
          const  jfE = e;
          t.commitHandle.handle = null;
          let n = function (i) {
            t.showLoader(),
              c.commitModels({
                handleRequest: t.commitHandle,
                serverName: t.options.serverName,
                serverUri: t.options.serverUri,
                modelList: e,
                tenant: e[0].getTenant(),
                securityContext: i.SecurityContext,
                onComplete: function (e) {
                  const partIdList = new Array();
                  if (e.results){
                    for (let j = 0; j < e.results.length; j++) {
                      const resObj = e.results[j];
                      if (resObj["status"] == 200){
                        const strPhysicalid =  resObj["body"]["physicalid"];
                        partIdList.push(strPhysicalid);
                      }

                    }
                  }
                  console.log("partIdList:",partIdList)
                  t.hideLoader(),
                    (t.commitHandle.handle = null),
                    t._onCompleteCommit(e);
                  // add by chenyan 新增获取请求必填提示
                  console.log("getNotWriteAttrByPart---------begin ")
                  let isXENAPP = false ;
                  if (widget){
                    const  appID = widget.data.appId;
                    console.log("appID:",appID)
                    if ("ENXENG_AP" == appID){
                      isXENAPP = true ;
                    }
                  }
                  if (isXENAPP){
                    c.getNotWriteAttrByPart({
                      handleRequest: t.commitHandle,
                      serverName: t.options.serverName,
                      serverUri: t.options.serverUri,
                      tenant: jfE[0].getTenant(),
                      securityContext: i.SecurityContext,
                      modelList: jfE,
                      partIdList: partIdList,
                      //20260720 update by ljr XEN必填校验不再校验DirectBuy、客户零件号、客户零件版本。
                      ignoreXENUnusedAttr: true,
                      onComplete: function (res) {
                        if (res.mess){
                          t.editPropertiesUI._manageError({eventID : "warning",message:res.mess});
                        }
                      },
                      onCompleteWS:function (res) {
                        if (res.mess){
                          t.editPropertiesUI._manageError({eventID : "warning",message:res.mess});
                        }
                      },
                    });
                  }
                  console.log("getNotWriteAttrByPart---------end ")

                },
              });
          };
          i.getInstance({ tenant: e[0].getTenant() }).getValidSecurityContext({
            context: this.options.context,
            onSuccess: function (e) {
              n(e);
            },
            onFailure: function (e) {
              t._setError(e, "error");
            },
          });
        },
        _manageNotificationFromAnswer: function (e, t) {
          var i = t,
            n = "error",
            s = "undefined";
          if (e)
            if (e.hasOwnProperty("error")) i = e.error;
            else if (e.hasOwnProperty("notification")) {
              (i = (a = e.notification).message),
                (n = a.type ? a.type : n),
                (s = a.code ? a.code : "LCERR400100") &&
                  (i = { type: "MainStaticError", message: a.message });
            } else if (e.hasOwnProperty("errors") && e.errors.length > 0) {
              var a;
              (i = (a = e.errors[e.errors.length - 1]).message),
                (n = a.type ? a.type : n);
            } else
              !1 === e.success &&
                (i = { message: v.get("Server error"), code: "ERROR" });
          if (i)
            if (
              void 0 !== e &&
              void 0 !== e.errors &&
              null != e.errors &&
              2 == e.errors.length &&
              void 0 !== e.errors[0].code &&
              "1900068" === e.errors[0].code &&
              void 0 !== e.errors[1] &&
              "1500188" != e.errors[1].code
            ) {
              var o = {
                level: "error",
                title: UWA.i18n("UpdateObjectfailed"),
                subtitle: UWA.i18n("UpdateObjectfailedSubtitle"),
                message: "",
              };
              f.showMessageOnUI(o.level, o.title, o.subtitle, o.message, !1);
            } else
              s && 0 == s.localeCompare("LCERR400100")
                ? this._setMainError(
                    i.message,
                    i.type.toLowerCase(),
                    i.customClass
                  )
                : this._setError(i, n.toLowerCase(), i.customClass);
          return { message: i, type: n.toLowerCase() };
        },
        _setError: function (e, t, i) {
          (t = t || "error"), this._raiseNotification(e, t, i);
        },
        _setMainError: function (e, t, i) {
          (t = t || "mainstaticerror"), this._raiseNotification(e, t, i);
        },
        _onCompleteCommit: function (e) {
          var i = this,
            s = "",
            a = !0,
            o = !1,
            r = !1,
            c = !1,
            d = null,
            u = null,
            h = e.transactionid;
          h &&
            (null !== (d = i.getModel(h)) && d.isMergedModel()
              ? (d.resetAllErrors(),
                d.lockModel(!0),
                (u = d.getMergingModels()))
              : console.log(
                  " --- >> The model retrieved from the update is not the same than the one which has been sent"
                ));
          var p = e.results,
            m = !1,
            _ = [],
            E = { created: [], deleted: [], modified: [], ownerChanged: [] };
          if (
            (e.modifiedObjectsBL &&
              e.modifiedObjectsBL.forEach((e) => {
                "Update" === e.action &&
                  E.modified.push({ physicalid: e.pid, operation: "update" });
              }),
            !1 === e.success)
          )
            i.editPropertiesUI.setError(e.error);
          else if (e.UXMessage) {
            var C = "";
            e.UXMessage.forEach((e) => {
              e && ("" != C && (C += "<br>"), (C += e));
            }),
              f.showMessageOnUI(
                "error",
                UWA.i18n("UpdateObjectfailed"),
                UWA.i18n("contactYourAdmin"),
                C,
                !1
              );
          } else {
            if (
              (p.forEach(function (e, t) {
                var i = e.status;
                i < 200 ||
                  i >= 300 ||
                  (e.body.oldInstancePhysicalid && !1 === c && (c = !0));
              }),
              p.forEach(function (e, n) {
                var d = !0,
                  h = e.status,
                  p = null;
                if (h < 200 || h >= 300)
                  p = {
                    message: v.get("Error during update"),
                    code: "ERROR",
                    customClass: "errorUpdate",
                  };
                else {
                  m = !0;
                  var f = null,
                    g = null,
                    C = null,
                    S = null;
                  e.body.physicalid &&
                    ((g =
                      "object" == typeof e.body.physicalid
                        ? e.body.physicalid.value
                        : e.body.physicalid),
                    (C = e.body.oldInstancePhysicalid),
                    (f = i.getModel(g)),
                    null !== u && null === f && (f = u[g]));
                  var y = null;
                  if (
                    (e.hasOwnProperty("RefreshUI") &&
                      (y = e.RefreshUI.results.attributes),
                    (S =
                      !1 === c
                        ? null == y || (null != y && 0 == y.length)
                          ? { physicalid: g, operation: "update" }
                          : {
                              physicalid: g,
                              operation: "update",
                              attributes: e.RefreshUI.results.attributes,
                            }
                        : null == y || (null != y && 0 == y.length)
                        ? {
                            physicalid: g,
                            oldInstancePhysicalid: C,
                            operation: "update",
                          }
                        : {
                            physicalid: g,
                            oldInstancePhysicalid: C,
                            operation: "update",
                            attributes: e.RefreshUI.results.attributes,
                          }),
                    !1 === c)
                  )
                    if (null == f) {
                      if (
                        null != e.body.attrID &&
                        null != e.body.name &&
                        null != e.body.value
                      ) {
                        d = !1;
                        var A = i.getModel();
                        if (A)
                          if (
                            (R = A.getAttributeFromField(
                              "attributeID",
                              e.body.attrID.value,
                              1
                            ))
                          ) {
                            var I = new l(t.clone(R));
                            I.set("value", e.body.value.value),
                              A.set(R.getIdentifier(), I),
                              i._resetModifFlag(A, [R.getIdentifier()]),
                              e.body.physicalid &&
                                e.body.physicalid.value &&
                                E.modified.push({
                                  physicalid: e.body.physicalid.value,
                                  operation: "update",
                                });
                          }
                      }
                    } else {
                      f.resetAllErrors(),
                        e.body && e.body.errors && e.body.errors.length > 0
                          ? (a = !1)
                          : ((s = v.get("Modification successful")),
                            (a = i.options.extraNotif));
                      e.body;
                      var b = f.values(),
                        P = [],
                        T = [],
                        D = !1;
                      for (var M in e.body) {
                        var w = e.body[M],
                          O = i._manageNotificationFromAnswer(w),
                          F = w ? ("object" == typeof w ? w.value : w) : null,
                          x = w ? ("object" == typeof w ? w.valueDB : F) : null,
                          W = f.get(M);
                        if (
                          (null == W &&
                            null != b &&
                            (b = f.values()).every(function (e) {
                              var t = !0;
                              return (
                                e instanceof l &&
                                  null != e.get("select") &&
                                  e.get("select") === M &&
                                  ((W = e), (M = e.getIdentifier()), (t = !1)),
                                t
                              );
                            }),
                          W)
                        ) {
                          var R,
                            N = !!W && !0 === W.get("multivalue");
                          (R = t.clone(W)).setError(O.message, O.type, !0);
                          var U = "current" != M && "owner" != M,
                            L =
                              null !== F && ("error" !== O.type || !O.message);
                          if (L) {
                            if (U) {
                              var k = F;
                              if (
                                (Array.isArray(F) &&
                                  (k = F.length > 0 ? F[0] : ""),
                                "#DENIED!" === k)
                              ) {
                                (o = !0), (D = !0);
                                break;
                              }
                              "description" == M && (F = k),
                                null == x && (x = F);
                              var V = F,
                                j = x;
                              if (N) {
                                var B = null != F;
                                (V = Array.isArray(F) ? F : B ? [F] : []),
                                  (j = Array.isArray(x) ? x : B ? [x] : []);
                              } else {
                                B = null != F;
                                (V = Array.isArray(F)
                                  ? F.length > 0
                                    ? F[0]
                                    : ""
                                  : F || ""),
                                  (j = Array.isArray(x)
                                    ? x.length > 0
                                      ? x[0]
                                      : ""
                                    : x || "");
                              }
                              R.set("value", V), R.set("valueDB", j);
                            }
                            T.push(M);
                          }
                          (f.attributeHasChanged(M) && !L) || P.push(M),
                            R instanceof l || (R = new l(R)),
                            f.set(M, R);
                        }
                      }
                      i._resetModifFlag(f, P), f.saveInitialValues(T);
                    }
                  else
                    (r = !0),
                      null == C && (C = g),
                      null == f &&
                        null == (f = i.getModel(C)) &&
                        null !== u &&
                        (f = u[C]),
                      null != f && f.clearChangedValue(),
                      _.push([g]);
                  !D &&
                    e.body &&
                    e.body.errors &&
                    e.body.errors.forEach(function (e) {
                      D = D || "unaccessible" === e.code;
                    }),
                    d &&
                      S &&
                      (D
                        ? E.deleted.push(S)
                        : e.body && e.body.owner
                        ? E.ownerChanged.push(S)
                        : E.modified.push(S));
                }
                i._manageNotificationFromAnswer(e.body, p);
              }),
              !0 === c)
            ) {
              var S = n.getWidgetPreference("changeControl"),
                y = null;
              null != S && "" !== S.value && S.value && (y = S.value),
                require([
                  "DS/WidgetServices/commandProxy/CommandProxy",
                ], function (e) {
                  e.publishSelect({
                    data: {
                      paths: _,
                      pathsInst: !0,
                      defaultFacetName: "instance",
                      changeControl: y,
                    },
                  });
                });
            }
            !0 === m &&
              require(["DS/PADUtils/PADCommandProxy"], function (e) {
                var t = [],
                  i = [],
                  n = [];
                E.modified.forEach(function (e) {
                  t.push(e.physicalid);
                  var i = null,
                    s = null;
                  if (!0 === c) {
                    (i = e.physicalid),
                      null == (s = e.oldInstancePhysicalid) &&
                        (s = e.physicalid);
                    var a = null;
                    null != i &&
                      null != s &&
                      null !== (a = { modified: i, previous: s }) &&
                      n.push(a);
                  }
                }),
                  E.ownerChanged.forEach(function (e) {
                    t.push({ physicalid: e.physicalid, key: "ownerChanged" });
                  }),
                  E.deleted.forEach(function (e) {
                    i.push(e.physicalid);
                  });
                var s = { modified: t };
                i.length > 0 && (s.deleted = i);
                var a = null;
                a = n.length > 0 ? { authored: n } : { authored: s };
                var o = e.create();
                o.authored({ authored: !0 }), o.refresh(a), o.destroy();
              });
          }
          if (
            (d &&
              setTimeout(function () {
                d.lockModel(!1);
              }),
            !0 === r)
          ) {
            C = v.get("instanceEvolved");
            i.editPropertiesUI.addInfo(C);
          }
          a && "" != s && i.editPropertiesUI.showNotification(s),
            !0 === o && this.onRefresh();
          var A = {
            numberObjects: p.length,
            actionStatus: !0 === m ? g.success : g.failure,
          };
          (i.lastTrackerEvent = g.createValidationEventFromEvent(
            A,
            i.lastTrackerEvent
          )),
            g.checkAndTrackPageEvent(i.lastTrackerEvent);
        },
        _buildFromResponse: function (e, t, i) {
          var n = this,
            s = {
              modifyAccess: !0,
              oneSuccess: !1,
              allAttributeAreRO: !0,
              plmparams: null,
            };
          if (!1 !== e.success) {
            for (
              var a = e.transactionid,
                l = e.results,
                d = l ? l.length : 0,
                u = 0;
              u < d;
              u++
            ) {
              var h = l[u];
              n._manageNotificationFromAnswer(h);
              var p = null;
              p = null != a ? a : n._getID(h);
              var f = n.getModel(p),
                g = n._getMetaType(h),
                v = null,
                _ = !1;
              if (f && f.isMergedModel()) {
                v = {};
                var E = h.ids,
                  C = f.getMergingModels();
                E.forEach(function (e) {
                  var t = C[e];
                  if (null == t) {
                    _ = !0;
                    var i = UWA.clone(f.getOptions());
                    (i.objectId = e), (i.metatype = g), (t = new o(i));
                  }
                  v[e] = t;
                });
              }
              if (_ || null == f) {
                if (!_) {
                  var S = n.getOccurrence();
                  if (null === S)
                    return console.log(" --- >> No current occurence"), {};
                  if (null !== S.getModel(this.getTabId()))
                    return (
                      console.log(
                        " --- >> A model already exist for properties tab"
                      ),
                      {}
                    );
                  if (!f && a)
                    return (
                      console.log(
                        " --- >> A merged model is returned for a single model"
                      ),
                      {}
                    );
                }
                var y = n.getOccurrence().getAllModels(!0, !0);
                t = { metatype: g, objectId: p };
                y &&
                  y.length > 0 &&
                  (((t = UWA.clone(y[0].getOptions())).objectId = p),
                  (t.metatype = g)),
                  null != v
                    ? (f = new r(t)).setMergingModels(v)
                    : (f = new o(t)),
                  f.clearChangedValue();
              }
              if ((g && f.set("metatype", g), n.setModel(f), h.extensions))
                for (var A in h.extensions) i[A] = h.extensions[A];
              var I = m.getPreviewUrl(h);
              n._cancelPreviewUrlHandle(),
                I ||
                  c.getPreviewUrl({
                    handleRequest: n.commitHandle,
                    modelList: [f],
                    tenant: f.getTenant(),
                    onComplete: function (e) {
                      (n.previewUrlHandle.handle = null),
                        e.forEach(function (e) {
                          var t = n.getModel(e.id);
                          null != t &&
                            e.preview &&
                            (e.preview.indexOf("icons") < 0 ||
                              !t.get("type_icon_url")) &&
                            t.set("previewUrl", e.preview);
                        });
                    },
                  });
              var b = m.fillFromServer(h, f, t);
              (s.modifyAccess = !1 !== b.modifyAccess && s.modifyAccess),
                (s.oneSuccess = s.oneSuccess || b.success),
                (s.allAttributeAreRO =
                  s.allAttributeAreRO && b.allAttributeAreRO),
                (s.plmparams = !1 !== s.plmparams && b.plmparams);
            }
            n.editPropertiesUI._ManageValidButton();
          }
          return s;
        },
        _onCompleteLoadModel: function (e, t) {
          this.hideLoader(),
            window.top.performance.mark("end_EP_AJAXRequest"),
            window.top.performance.mark("start_EP_DataTreatement"),
            t || (t = { initTable: !0, buildBehavior: u.FORCE_DEFAULT });
          var i = e.options;
          i &&
            (void 0 !== i.showSections &&
              this.editPropertiesUI.setOption(
                "showSections",
                !0 === i.showSections
              ),
            void 0 !== i.autoExpand &&
              this.editPropertiesUI.setOption(
                "sectionAutoexpand",
                !0 === i.autoExpand
              ));
          var n = {};
          this._manageNotificationFromAnswer(e);
          var s = this._buildFromResponse(e, t, n),
            a =
              s.modifyAccess &&
              !s.allAttributeAreRO &&
              (!this.isReadOnly() || "false" === this.isReadOnly());
          (!0 !== a && !0 !== t.initTable) ||
            (this.editPropertiesUI.switchComponentMode(!a),
            this.updateReadWriteAccess(a)),
            window.top.performance.mark("end_EP_DataTreatement"),
            window.top.performance.mark("start_EP_DataDisplay");
          var o = this.getModel(),
            r = s.oneSuccess && o ? [o] : [];
          this.editPropertiesUI.setAccordionProperties(n),
            r && r.length > 0 && (r[0].facet = "prop"),
            r &&
              r.length > 0 &&
              this.trackPropInitUXEvent(r[0], this.getTabId()),
            this.editPropertiesUI.initData(r, {
              edition: t.buildBehavior,
              properties: !0,
            }),
            s.plmparams && this.editPropertiesUI.addSection({ section: _ }, !0);
        },
        _getID: function (e) {
          return e.physicalID;
        },
        _getMetaType: function (e) {
          return e.metatype;
        },
        _resetModifFlag: function (e, t) {
          e.clearChangedValue(t), this.editPropertiesUI._ManageValidButton();
        },
        changeAttributes: function (e) {
          this.editPropertiesUI.changeAttributes(e);
        },
        trackPropInitUXEvent: function (e, t) {
          if (e && e instanceof o) {
            var i = e._innerModels ? Object.keys(e._innerModels).length : 1,
              n = null;
            e &&
              e._attributes &&
              e._attributes.source &&
              (n = e._attributes.source);
            var s = null;
            1 == i &&
              e._attributes &&
              e._attributes._DS_ATTR_type &&
              e._attributes._DS_ATTR_type.valueDB &&
              (s = e._attributes._DS_ATTR_type.valueDB),
              1 == i &&
                !s &&
                e &&
                e._attributes &&
                e._attributes.type &&
                e._attributes.type.valueDB &&
                (s = e._attributes.type.valueDB),
              1 == i &&
                !s &&
                e &&
                e._attributes &&
                e._attributes["ds6w:type"] &&
                (e._attributes["ds6w:type"].value ||
                  e._attributes["ds6w:type"].valueDB) &&
                (s =
                  e._attributes["ds6w:type"].value ||
                  e._attributes["ds6w:type"].valueDB);
            let o = "Information Properties Facet";
            var a = {
              UXName: "Properties",
              UXContext: (o =
                t && "instance" === t.toLowerCase()
                  ? "Information Instance Facet"
                  : "Information Properties Facet"),
              numberObjects: i,
              typeObjects: s,
            };
            n && (a.source = n),
              (this.lastTrackerEvent = g.createInitActionEvent(a)),
              g.checkAndTrackPageEvent(this.lastTrackerEvent);
          }
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/facets/Common/PropertiesDerived",
    [
      "DS/EditPropWidget/facets/Properties/EditPropController",
      "UWA/Controls/Abstract",
      "UWA/Class",
      "DS/EditPropWidget/models/EditPropModel",
      "DS/EditPropWidget/models/EditPropModelMerge",
      "DS/EditPropWidget/constants/EditPropConstants",
      "DS/WidgetServices/WidgetServices",
    ],
    function (e, t, i, n, s, a, o) {
      "use strict";
      return e.extend({
        metatype: a.METATYPE_UNKNOWN,
        setOccurrence: function (e) {
          var t = this,
            i = e ? e.getOptions(this.getTabId()) : null,
            a = e ? e.getModel(this.getTabId()) : null;
          if (i && !a) {
            if ("object" == typeof i) {
              var o = i.targets;
              if (null != o) {
                var r = [],
                  l = {};
                if (
                  (o.forEach(function (e) {
                    var i = new n({ objectId: e.id, metatype: t.metatype });
                    r.push(i), (l[e.id] = i);
                  }),
                  e.isMergedOccurrence())
                ) {
                  var c = e.getMergePrefixe();
                  (a = new s({
                    objectId: c,
                    metatype: t.metatype,
                  })).setMergingModels(l);
                } else if (r.length > 0) {
                  c = r[0].get("objectId");
                  a = new n({ objectId: c, metatype: t.metatype });
                }
                e.setModel(this.getTabId(), a);
              }
            }
          }
          (null == e || e.getModel(this.getTabId())) && this._parent(e);
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/facets/EBOMPart/EBOMPart",
    [
      "DS/EditPropWidget/facets/Common/PropertiesDerived",
      "DS/EditPropWidget/constants/EditPropConstants",
    ],
    function (e, t) {
      "use strict";
      return e.extend({
        metatype: t.BUS,
        init: function (e) {
          var t = {};
          for (var i in e) t[i] = e[i];
          (t.readOnly = !0), this._parent(t);
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/facets/Instance/InstanceFacet",
    [
      "DS/EditPropWidget/facets/Common/PropertiesDerived",
      "DS/EditPropWidget/constants/EditPropConstants",
    ],
    function (e, t) {
      "use strict";
      return e.extend({ metatype: t.REL });
    }
  ),
  define(
    "DS/EditPropWidget/facets/Properties/iPropertiesFacet",
    [
      "UWA/Core",
      "UWA/Class/Model",
      "DS/EditPropWidget/facets/Properties/EditPropController",
      "DS/EditPropWidget/facets/Properties/Properties",
      "DS/EditPropWidget/models/EditPropAttributeModel",
      "DS/EditPropWidget/models/EditPropModel",
      "DS/EditPropWidget/utils/AttributesUtils",
      "DS/EditPropWidget/constants/EditPropConstants",
      "DS/EditPropWidget/constants/ConstantsMapping",
      "DS/EditPropWidget/comm/DistantAjax",
      "DS/WidgetServices/WidgetServices",
      "DS/EditPropWidget/ajaxRequests/EditPropAjaxRequests",
      "DS/FedDictionaryAccess/FedDictionaryAccessAPI",
      "DS/EditPropWidget/utils/EditPropIndexUtils",
      "DS/UIKIT/Mask",
      "DS/EditPropWidget/utils/EditPropIndexDictionaryForUnits",
      "DS/EditPropWidget/ajaxRequests/ServicesAjaxRequests",
      "text!DS/EditPropWidget/assets/settings/SettingsForServices.json",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s, a, o, r, l, c, d, u, h, p, m, f, g, v, _) {
      "use strict";
      return (
        (v = JSON.parse(v)),
        i.extend({
          getListHandle: null,
          init: function () {
            this._parent.apply(this, arguments),
              (this.getListHandle = { handle: null }),
              this.elements.container ||
                (this.elements.container = e.createElement("div", {
                  class: "RPL30",
                  styles: { height: "100%" },
                })),
              (this.listOfExtensions = []),
              (this.listOfPredicates = []),
              (this.maskAttributeDetails = {}),
              (this.maskGroupInfo = {}),
              (this.groupAttributesInfoAsPerMask = {}),
              (this.answer = null),
              (this.dimensionAttributes = new Map()),
              f.setUpDimensionsDictionary();
          },
          updateFacet: function () {
            if (this.elements && this.elements.container) {
              var e = this.getModel();
              if (!e) return !1;
              if (!e.get("indexModeParameter")) return;
              var t = e.get("objectId");
              this.elements.container.setContent(""),
                (this.iPropUX = new n({
                  setCloseButton: !1,
                  editMode: !1,
                  readOnly: !0,
                  scroller: !1,
                  identifier: "EPIndex_" + t,
                  events: {},
                })),
                this.elements.container.setContent(this.iPropUX),
                this.loadModels(e, {});
            }
          },
          loadModels: function (e, t) {
            window.top.performance.mark("start_PrepareAjax");
            var i = this;
            this.showLoader();
            var n = e;
            if (e) {
              var s = l.getServiceBySource("fedsearch");
              if (!0 === e.isTransientModel() || (s && !0 === s.modelNeeded))
                this.setModel(n),
                  this.iPropUX.initData([this.getModel()]),
                  this.hideLoader();
              else if (
                (window.top.performance.mark("end_PrepareAjax"),
                t && !1 === t.loadModel)
              )
                this.setModel(n), this.iPropUX.initData([this.getModel()]);
              else {
                window.top.performance.mark("start_EP_AJAXRequest");
                var a = !1;
                window.top.performance.mark("start_EP_GetFromDistant"),
                  c.getAsyncRead(function (t) {
                    !1 === a &&
                      (t && t.response && t.options
                        ? (window.top.performance.mark("end_EP_GetFromDistant"),
                          (a = !0),
                          (i.answer = t.response),
                          i._cancelListRequest(),
                          t.options.maskAttributeDetails &&
                            (i.maskAttributeDetails =
                              t.options.maskAttributeDetails),
                          t.options.predicates &&
                            (i.listOfPredicates = t.options.predicates),
                          t.options.groupAttributesInfoAsPerMask &&
                            (i.groupAttributesInfoAsPerMask =
                              t.options.groupAttributesInfoAsPerMask),
                          t.options.listOfExtensions &&
                            (i.listOfExtensions = t.options.listOfExtensions),
                          t.options.maskGroupInfo &&
                            (i.maskGroupInfo = t.options.maskGroupInfo),
                          t.options.dimensionAttributes &&
                            (i.dimensionAttributes =
                              t.options.dimensionAttributes),
                          i.calculateUIPosition(),
                          i.getDimensionAttribute(
                            i.answer,
                            i.dimensionAttributes,
                            function (e) {
                              (i.answer = e),
                                i.parseResponse(),
                                i._onCompleteLoadModel(e);
                            }
                          ))
                        : (function () {
                            if (
                              ((i.launchPostRequest = function () {
                                i._cancelListRequest();
                                var e = d.getCancelObject();
                                i.getListHandle.handle = e;
                                var t = {
                                  handleRequest: { handle: e },
                                  modelList: [n],
                                  tenant: n.getTenant(),
                                  onComplete: function (t) {
                                    i.hideLoader(),
                                      !1 !== a ||
                                        e.isCancelled() ||
                                        ((a = !0),
                                        console.log("Ajax 2"),
                                        !0 === window.top.isDebug &&
                                          console.log(
                                            "Ajax 2 answered " +
                                              window.top.performance.now()
                                          ),
                                        (i.getListHandle.handle = null),
                                        t &&
                                        t.results &&
                                        t.results.length > 0 &&
                                        t.results[0] &&
                                        t.results[0].data &&
                                        Array.isArray(t.results[0].data) &&
                                        t.results[0].data.length > 0
                                          ? i.getDimensionAttribute(
                                              t,
                                              i.dimensionAttributes,
                                              function (e) {
                                                (i.answer = e),
                                                  i.calculateUIPosition(),
                                                  i.parseResponse(),
                                                  i._onCompleteLoadModel(e);
                                              }
                                            )
                                          : (i._setError(
                                              _.get("FedAjax_NotFound"),
                                              void 0,
                                              "rpl30_" + Date.now()
                                            ),
                                            i.hideMainComponent()));
                                  },
                                };
                                (t.defService = s),
                                  (t.select_predicates = i.listOfPredicates);
                                let o = n.get("source"),
                                  r = o && v[o.toUpperCase()],
                                  l = !1;
                                r && (l = r.plmParameterSupported),
                                  (t.getPLMParameter = l),
                                  (t.dimensionAttributes =
                                    i.dimensionAttributes),
                                  u.getListOfAttributes(t);
                              }),
                              (i.launchPostRequestForInstance = function () {
                                var t = l.getServiceBySource("3dspace");
                                u.getInstanceDetails({
                                  modelList: [e],
                                  defService: t,
                                  onComplete: function (t) {
                                    if (
                                      (i.hideLoader(),
                                      t &&
                                        t.infos &&
                                        t.infos.status &&
                                        "OK" === t.infos.status)
                                    )
                                      if (
                                        t.result &&
                                        t.result.length > 0 &&
                                        t.result[0] &&
                                        t.result[0].input_object &&
                                        "object" ==
                                          typeof t.result[0].input_object
                                      ) {
                                        var n = t.result[0].input_object;
                                        i.calculateUIPosition(),
                                          i.parseInstanceWSResponse(
                                            n,
                                            e,
                                            function (e) {
                                              e &&
                                              e.results &&
                                              e.results.length > 0 &&
                                              e.results[0] &&
                                              e.results[0].data &&
                                              Array.isArray(
                                                e.results[0].data
                                              ) &&
                                              e.results[0].data.length > 0
                                                ? i.getDimensionAttribute(
                                                    e,
                                                    i.dimensionAttributes,
                                                    function (e) {
                                                      (i.answer = e),
                                                        i.parseResponse(),
                                                        i._onCompleteLoadModel(
                                                          e
                                                        );
                                                    }
                                                  )
                                                : (i._setError(
                                                    _.get("FedAjax_NotFound"),
                                                    void 0,
                                                    "rpl30" + Date.now()
                                                  ),
                                                  i.hideMainComponent());
                                            }
                                          );
                                      } else
                                        i._setError(
                                          _.get("FedAjax_NotFound"),
                                          void 0,
                                          "rpl30" + Date.now()
                                        ),
                                          i.hideMainComponent();
                                  },
                                  primitives: [
                                    {
                                      navigate_to_rel: {
                                        id: 1,
                                        mode: "return_rel",
                                      },
                                    },
                                  ],
                                  patterns: { RELATIONS: [{ id: 1 }] },
                                  attributes: i.listOfPredicates,
                                });
                              }),
                              n.get("metatype") !== r.METATYPE_REL ||
                                n.get("selReferenceID"))
                            )
                              if (n.get("type")) i.getListOfAttributes(n);
                              else {
                                var t = d.getCancelObject();
                                i.getListHandle.handle = t;
                                var o = {
                                  handleRequest: { handle: t },
                                  modelList: [n],
                                  tenant: n.getTenant(),
                                  onComplete: function (e) {
                                    if (
                                      (i.hideLoader(),
                                      e &&
                                        e.results &&
                                        "object" == typeof e.results[0] &&
                                        e.results[0] &&
                                        e.results[0].data &&
                                        Array.isArray(e.results[0].data) &&
                                        e.results[0].data.length > 0)
                                    ) {
                                      if (
                                        "ds6w:type" ===
                                        e.results[0].data[0].name
                                      ) {
                                        var t = e.results[0].data[0].valueDB;
                                        t && n.set("type", t);
                                      }
                                    } else
                                      i._setError(
                                        _.get("FedAjax_NotFound"),
                                        void 0,
                                        "rpl30" + Date.now()
                                      ),
                                        i.hideMainComponent();
                                    i.getListOfAttributes(n);
                                  },
                                };
                                (o.defService = s),
                                  (o.select_predicates = ["ds6w:type"]),
                                  u.getListOfAttributes(o);
                              }
                            else i.getInstanceDetails(n);
                          })());
                  });
              }
            } else
              this.iPropUX &&
                "function" == typeof this.iPropUX.clear &&
                this.iPropUX.clear(),
                this._onCompleteLoadModel({ results: [] });
          },
          _getModelType: function (e) {
            var t = e.get("type"),
              i = null;
            return (
              (null != (i = "object" == typeof t ? t.get("valueDB") : t) &&
                null != i) ||
                (i = r.EDITPROPINDEX_DEFAULTTYPE),
              i
            );
          },
          destroyComponent: function () {
            e.is(this.destroy, "function") && this.destroy(),
              (this.listOfExtensions = null),
              (this.listOfPredicates = null),
              (this.maskAttributeDetails = null),
              (this.maskGroupInfo = null),
              (this.groupAttributesInfoAsPerMask = null),
              (this.answer = null),
              f._destoryDictionary();
          },
          calculateUIPosition: function () {
            var e = this,
              t = 1;
            for (var i in e.groupAttributesInfoAsPerMask)
              e.groupAttributesInfoAsPerMask[i] &&
                e.groupAttributesInfoAsPerMask[i].isType &&
                e.groupAttributesInfoAsPerMask[i].attributes &&
                e.groupAttributesInfoAsPerMask[i].attributes.forEach(function (
                  i
                ) {
                  (e.maskAttributeDetails[i].UIPosition = t), t++;
                });
            for (var i in e.groupAttributesInfoAsPerMask)
              e.groupAttributesInfoAsPerMask[i] &&
                e.groupAttributesInfoAsPerMask[i].isExtension &&
                e.groupAttributesInfoAsPerMask[i].isAutomaticExt &&
                e.groupAttributesInfoAsPerMask[i].attributes &&
                e.groupAttributesInfoAsPerMask[i].attributes.forEach(function (
                  i
                ) {
                  (e.maskAttributeDetails[i].UIPosition = t), t++;
                });
            for (var i in e.groupAttributesInfoAsPerMask)
              e.groupAttributesInfoAsPerMask[i] &&
                e.groupAttributesInfoAsPerMask[i].isExtension &&
                !1 === e.groupAttributesInfoAsPerMask[i].isAutomaticExt &&
                e.groupAttributesInfoAsPerMask[i].attributes &&
                e.groupAttributesInfoAsPerMask[i].attributes.forEach(function (
                  i
                ) {
                  (e.maskAttributeDetails[i].UIPosition = t), t++;
                });
            e.answer &&
              e.answer.results &&
              e.answer.results.length > 0 &&
              e.answer.results[0] &&
              e.answer.results[0].PLMParametersAvailableForIndexMode &&
              e.answer.results[0].groupData &&
              e.answer.results[0].groupData.forEach(function (e) {
                (e.UIPosition = t), t++;
              });
          },
          getListOfAttributes: async function (e) {
            var t = this,
              i = { model: e, type: t._getModelType(e) };
            p.getListOfExtensions(i, function (i) {
              (t.listOfExtensions = i),
                (t.listOfPredicates = []),
                (t.maskAttributeDetails = {}),
                (t.maskGroupInfo = {}),
                (t.groupAttributesInfoAsPerMask = {});
              var n = [],
                s = { isBus: !0, name: t._getModelType(e) };
              n.push(s);
              for (var a = 0; a < i.length; a++) n.push(i[a]);
              p.getMaskAttributes(n, function (e) {
                (t.maskAttributeDetails = e.maskAttributeDetails),
                  (t.listOfPredicates = e.listOfPredicates),
                  (t.maskGroupInfo = e.maskGroupInfo),
                  (t.groupAttributesInfoAsPerMask =
                    e.groupAttributesInfoAsPerMask),
                  (t.dimensionAttributes = e.dimensionAttributes),
                  t.launchPostRequest(t.listOfPredicates);
              });
            });
          },
          parseResponse: function () {
            var e = this,
              t = [],
              i = [],
              n = e.answer;
            n.results[0].data.forEach(function (n) {
              e.listOfPredicates.includes(n.name) &&
                (e.maskAttributeDetails &&
                  e.maskAttributeDetails.hasOwnProperty(n.name) &&
                  e.maskAttributeDetails[n.name].isExt &&
                  (n.extension = e.maskAttributeDetails[n.name].extension),
                e.maskAttributeDetails &&
                  e.maskAttributeDetails[n.name] &&
                  e.maskAttributeDetails[n.name].UIPosition &&
                  (n.UIPosition = e.maskAttributeDetails[n.name].UIPosition),
                e.maskAttributeDetails[n.name] &&
                e.maskAttributeDetails[n.name].group
                  ? ((n.group = e.maskAttributeDetails[n.name].group),
                    e.maskGroupInfo[n.group] &&
                      e.maskGroupInfo[n.group].NLS &&
                      e.maskGroupInfo[n.group].weight.toString().length > 0 &&
                      ((n.groupNLS = e.maskGroupInfo[n.group].NLS),
                      (n.groupWeight = e.maskGroupInfo[n.group].weight)),
                    i.push(n))
                  : t.push(n));
            }),
              (n.results[0].data = t),
              (n.results[0].groupData = n.results[0].groupData
                ? [...n.results[0].groupData, ...i]
                : i),
              e.listOfExtensions &&
                e.listOfExtensions.length > 0 &&
                ((n.results[0].extensions = {}),
                e.listOfExtensions.forEach(function (e) {
                  (n.results[0].extensions[e.name] = {
                    name: e.name,
                    nameNLS: e.name,
                  }),
                    e.isAutomaticExt &&
                      (n.results[0].extensions[e.name].deploymentExtension =
                        e.isAutomaticExt);
                })),
              (n.options = { showSections: !0, autoExpand: !1 });
          },
          getInstanceDetails: async function (e) {
            var t = this,
              i = t._getModelType(e);
            if (i !== r.EDITPROPINDEX_DEFAULTTYPE) {
              var n = { model: e, type: i };
              p.getListOfExtensions(n, function (e, n) {
                (t.listOfExtensions = e),
                  (t.listOfPredicates = []),
                  (t.maskAttributeDetails = {}),
                  (t.maskGroupInfo = {}),
                  (t.groupAttributesInfoAsPerMask = {});
                var s = [];
                if (i !== r.EDITPROPINDEX_DEFAULTTYPE) {
                  var a = { isRel: !0, name: i };
                  s.push(a);
                }
                for (var o = 0; o < e.length; o++) s.push(e[o]);
                p.getMaskAttributes(s, function (e) {
                  (t.maskAttributeDetails = e.maskAttributeDetails),
                    (t.listOfPredicates = e.listOfPredicates),
                    (t.maskGroupInfo = e.maskGroupInfo),
                    (t.groupAttributesInfoAsPerMask =
                      e.groupAttributesInfoAsPerMask),
                    (t.dimensionAttributes = e.dimensionAttributes),
                    t.launchPostRequestForInstance(t.listOfPredicates);
                });
              });
            }
          },
          parseInstanceWSResponse: function (e, t, i) {
            var n = this,
              s = t.get("source");
            s || (s = t.get("sourceName"));
            var a = { source: s },
              o = [],
              r = Object.keys(e),
              l = {};
            for (var c in e) l[c] = [e[c]];
            var u = new Promise(function (e) {
                d.isReplayedODT() &&
                  (n.nlsNameApiCalledCounter || (n.nlsNameApiCalledCounter = 0),
                  void 0 !== n.nlsNameApiCalledCounter &&
                    n.nlsNameApiCalledCounter++),
                  n.getElementsNls(r, {
                    tenant: g.getTenantFromModel(t),
                    onComplete: function (t) {
                      e(t);
                    },
                    apiCalled: n.nlsNameApiCalledCounter,
                  });
              }),
              h = new Promise(function (e) {
                d.isReplayedODT() &&
                  (n.nlsValueApiCalledCounter ||
                    (n.nlsValueApiCalledCounter = 0),
                  void 0 !== n.nlsValueApiCalledCounter &&
                    n.nlsValueApiCalledCounter++);
                var i = t.get("sourceName")
                  ? t.get("sourceName")
                  : t.get("source");
                n.getValuesNls(l, {
                  tenant: g.getTenantFromModel(t),
                  service: i,
                  onComplete: function (t) {
                    e(t);
                  },
                  apiCalled: n.nlsValueApiCalledCounter,
                });
              });
            Promise.all([u, h]).then(function (t) {
              if (t && "object" == typeof t) {
                var n = t[0],
                  s = t[1];
                n &&
                  n.vocabularyElementNLSInfo &&
                  n.vocabularyElementNLSInfo.forEach(function (t) {
                    if ("Predicate" === t.type) {
                      var i = t.uri,
                        n = t.dataType,
                        r = s[i] ? s[i][e[i]] : "",
                        l = e[i] ? e[i] : "",
                        c = {
                          name: i,
                          selectable: i,
                          path: i,
                          type: n,
                          readOnly: !0,
                          value: r,
                          valueDB: l,
                          nls: t.nlsName,
                        };
                      if ("dateTime" === n || "date" === n) {
                        var u = Date.parse(l),
                          h = new Date(u);
                        if (h) {
                          var p = d.convertDateToStandard(h, !0);
                          (l = p.valueDB),
                            (r = p.value),
                            (c.value = r),
                            (c.valueDB = l);
                        }
                        c.type = "date";
                      }
                      o.push(c),
                        "ds6w:label" === t.uri && (a.computed = { label: c });
                    } else if (t.nlsName) a[t.nlsName] = e[t.nlsName];
                    else {
                      var m = t.uri.split(":");
                      m.length > 1 && m[1] && (a[t.nlsName] = e[m[1]]);
                    }
                  }),
                  (a.data = o);
              }
              i({ results: [a] });
            });
          },
          getDimensionAttribute: function (e, t, i) {
            var n = !1;
            if (
              e &&
              e.results &&
              e.results.length > 0 &&
              e.results[0] &&
              e.results[0].data
            )
              for (var s = e.results[0].data, a = 0; a < s.length; a++)
                if (s[a].name && t && t.has(s[a].name)) {
                  n = !0;
                  break;
                }
            n ? f.getDimension(e, t, i) : i(e);
          },
          getElementsNls: function (e, t) {
            e && e.length > 0
              ? h.getResourcesInfo(e.join(","), {
                  tenantId: t.tenant,
                  lang: d.getLanguage(),
                  apiVersion: "R2018x",
                  onComplete: function (e) {
                    t.onComplete.call(this, e);
                  },
                  onFailure: function (e) {
                    console.log(" --\x3e> Can't get ElementsNls " + e),
                      t.onComplete.call(this, { success: !1 });
                  },
                })
              : t.onComplete.call(this, null);
          },
          getValuesNls: function (e, t) {
            if (e) {
              var i = {};
              for (var n in e)
                Array.isArray(e[n]) &&
                  ((i[n] = []),
                  e[n].forEach(function (s) {
                    i[n].push({ value: e[n][0], source: [t.service] });
                  }));
              h.getNlsOfPropertiesValuesBySource(i, {
                tenantId: t.tenant,
                serviceName: t.service,
                lang: d.getLanguage(),
                apiVersion: "R2019x",
                onComplete: function (e) {
                  t.onComplete.call(this, e);
                },
                onFailure: function (e) {
                  console.log(" --\x3e> Can't get ValuesNls " + e),
                    t.onComplete.call(this, e);
                },
              });
            }
          },
          hideMainComponent: function () {
            this.editPropertiesUI &&
              this.editPropertiesUI.elements &&
              this.editPropertiesUI.elements.mainDiv &&
              "function" ==
                typeof this.editPropertiesUI.elements.mainDiv.hide &&
              this.editPropertiesUI.elements.mainDiv.hide();
          },
        })
      );
    }
  ),
  define(
    "DS/EditPropWidget/facets/Instance/IndexInstanceFacet",
    [
      "UWA/Core",
      "DS/EditPropWidget/facets/Properties/iPropertiesFacet",
      "DS/EditPropWidget/constants/EditPropConstants",
      "DS/EditPropWidget/models/EditPropModel",
      "DS/EditPropWidget/models/EditPropModelMerge",
      "DS/WidgetServices/WidgetServices",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s, a, o) {
      "use strict";
      return t.extend({
        setOccurrence: function (e) {
          var t = this,
            s = e ? e.getOptions(this.getTabId()) : null,
            a = e ? e.getModel(this.getTabId()) : null;
          if (s && !a) {
            if (s && "object" == typeof s) {
              var r = s.targets;
              null != r &&
                (r.forEach(function (e) {
                  e.id
                    ? (a = new n({
                        objectId: e.id,
                        metatype: i.METATYPE_REL,
                        type: e.type ? e.type : "",
                      }))
                    : (t._setError(
                        o.get("FedAjax_NotFound"),
                        void 0,
                        "rpl30" + Date.now()
                      ),
                      t.hideMainComponent());
                }),
                e.setModel(this.getTabId(), a));
            }
          }
          (null == e || e.getModel(this.getTabId())) && this._parent(e);
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/UI/IDCardSkeleton",
    [
      "UWA/Class/Model",
      "DS/EditPropWidget/models/CommonAbstract",
      "DS/EditPropWidget/constants/EditPropConstants",
      "DS/EditPropWidget/ICard/IdCard",
      "DS/EditPropWidget/facets/Properties/EditPropController",
      "DS/WidgetServices/MultipleAsynchronousRequests",
      "DS/WidgetServices/WidgetServices",
      "DS/EditPropWidget/comm/DistantAjax",
      "DS/EditPropWidget/buttonsBar/ButtonsBar",
      "DS/EditPropWidget/constants/ConstantsMapping",
      "DS/UIKIT/Tooltip",
      "DS/UIKIT/Input/Button",
      "DS/UIKIT/SuperModal",
      "DS/UIKIT/Spinner",
      "DS/Controls/TabBar",
      "DS/Controls/TooltipModel",
      "DS/PlatformAPI/PlatformAPI",
      "DS/WidgetServicesUI/tweakers/TweakerWUXButton",
      "css!DS/EditPropWidget/EditPropWidget.css",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s, a, o, r, l, c, d, u, h, p, m, f, g, v, _, E) {
      "use strict";
      var C = { "DS/EditPropWidget/facets/Properties/EditPropController": s };
      window.top.perfoIncrem = window.top.perfoIncrem
        ? window.top.perfoIncrem
        : 0;
      var S = {};
      (S[i.ACTION_SWITCH] = {
        name: i.ACTION_SWITCH,
        text: E.get("EditProp_boutonSwitchtoEdit"),
        icon: i.ACTION_SWITCH,
        id: "EDITPROP_PENCIL_EDIT_ICON",
      }),
        (S[i.ACTION_RESET] = {
          name: i.ACTION_RESET,
          text: E.get("Reset display"),
          icon: i.ACTION_RESET,
        }),
        (S[i.ACTION_SWYM_VIEW_CONTENT_IN_3DSWYM] = {
          name: i.ACTION_SWYM_VIEW_CONTENT_IN_3DSWYM,
          text: E.get("ViewIn3DSwym"),
          icon: "level-down",
        }),
        (S[i.ACTION_SWYM_VIEW_COMMUNITY_IN_3DSWYM] = {
          name: i.ACTION_SWYM_VIEW_COMMUNITY_IN_3DSWYM,
          text: E.get("ViewIn3DSwym"),
          icon: "level-down",
        }),
        (S[i.ACTION_SWYM_VIEW_USER_PROFILE] = {
          name: i.ACTION_SWYM_VIEW_USER_PROFILE,
          text: E.get("ViewUserProfile"),
          icon: "level-down",
        }),
        (S[i.ACTION_SWYM_VIEW_SURVEY_IN_3DSWYM] = {
          name: i.ACTION_SWYM_VIEW_SURVEY_IN_3DSWYM,
          text: E.get("ViewIn3DSwym"),
          icon: "level-down",
        }),
        (S[i.ACTION_SHARING_FILTER_VIEW] = {
          name: i.ACTION_SHARING_FILTER_VIEW,
          text: E.get("filterMembers"),
          icon: "filter",
        }),
        (S[i.ACTION_SHARING_DELETE] = {
          name: i.ACTION_SHARING_DELETE,
          text: E.get("deleteSharingMembers"),
          icon: "trash",
        }),
        (S[i.ACTION_SHARING_ADD] = {
          name: i.ACTION_SHARING_ADD,
          text: E.get("addSharingMembers"),
          icon: "topbar-share",
        }),
        (S[i.ACTION_SHARING_LIST_VIEW] = {
          name: i.ACTION_SHARING_LIST_VIEW,
          text: E.get("sharingListView"),
          icon: "alpha-sorting-a-z",
        }),
        (S[i.ACTION_SHARING_LARGE_TILE_VIEW] = {
          name: i.ACTION_SHARING_LARGE_TILE_VIEW,
          text: E.get("sharingLargeTileView"),
          icon: "group",
        });
      i.FACET_NONE;
      return t.extend({
        _lFacets: [],
        _sharedContext: null,
        _sharedOptions: {},
        _RWAccess: !1,
        defaultOptions: {
          name: "",
          className: "",
          showIdCard: !0,
          showTopBar: !0,
          showFacets: !0,
          warningOnUnsaved: !0,
          closeButton: !0,
          noThumbnail: !1,
          facetLabel: !0,
          displayType: i.STANDARD,
        },
        init: function (e, t) {
          (this._sharedOptions = {}),
            (this.indexModeParameter = !!t.indexModeParameter),
            (this.disableAddExtensionButton = !!t.disableAddExtensionButton),
            this._parent(t),
            this.options.actions &&
              this.options.actions.forEach(function (e) {
                void 0 === e.after &&
                  "action_close_panel" === e.name &&
                  (e.after = !0);
              }),
            (this._sharedContext = t.sharedContext ? t.sharedContext : null),
            (this._lFacets = e),
            this.elements || (this.elements = {}),
            (this.loaderFacetList = []),
            this.idcardInit();
        },
        getTopBar: function () {
          return this.elements.manageButtonDiv;
        },
        idcardInit: function (e) {
          var t = this,
            s = (this.elements.container = UWA.createElement("div", {
              name: this.options.name,
              class: this.options.className,
            })),
            a = "";
          !0 === this.options.showTopBar && (a = "withMenuBar");
          this.elements.globalDiv = UWA.createElement("div", {
            class: "IDCardGlobalContain " + a,
          }).inject(s);
          var o = (t.elements.mainInterface = UWA.createElement("div", {
              class: "IDCardMainInterface",
            }).inject(this.elements.globalDiv)),
            r = (this.elements.masterTable = UWA.createElement("div", {
              class: "IDCardMasterTable ",
            }).inject(o)),
            c = (this.elements.topTable = UWA.createElement("div", {
              class: "IDCardTopTableContainer",
            }));
          if (
            ((this.elements.bottomMainArea = UWA.createElement("div", {
              class: "IDCardBottomArea",
            })),
            (this.elements.bottomInnerArea = UWA.createElement("div", {
              class: "IDCardInnerBottomArea",
            })),
            this.elements.bottomInnerArea.inject(this.elements.bottomMainArea),
            (this.elements.actionArea = UWA.createElement("div", {
              class: "actionArea",
            })),
            (this.elements.bottomArea = UWA.createElement("div", {
              class: "facetContainer",
            })),
            this.elements.actionArea.inject(this.elements.bottomInnerArea),
            this.elements.bottomArea.inject(this.elements.bottomInnerArea),
            (this.elements.tabContainer = UWA.createElement("div", {
              class: "tabMainContainer",
            })),
            c.inject(r),
            this.elements.tabContainer.inject(r),
            this.elements.bottomMainArea.inject(r),
            (this.elements.buttonsBar = new l({
              vertical: !1,
              minimumSize: -1,
            })),
            this.elements.buttonsBar.inject(this.elements.actionArea),
            delete this.elements.postIdCard,
            !0 === this.options.showIdCard)
          ) {
            var d = {};
            this.options.idCardOptions && (d = this.options.idCardOptions),
              (d.actions = []),
              (this.elements.postIdCard = new n(d)),
              (t.identityCardContainer = UWA.createElement("div", {
                class: "topTableElement identityCardCell infra",
              }).inject(c));
            var u = UWA.createElement("div", {
              class: "ICTableContainer ",
              events: {
                click: function () {
                  let e = { currentFacet: t.currentFacetName };
                  g.publish(`PropHeaderActivated:${widget.id}`, e);
                },
              },
            }).inject(this.identityCardContainer);
            this.elements.postIdCard.inject(u),
              (this.currentFacetName = null),
              (this.defaultFacetName = null),
              (this.infoForFacet = null),
              this._createTabs([]),
              (this.elements.superModal = new h({ renderTo: this.container }));
          }
          (this.elements.manageButtonDiv = UWA.createElement("div", {
            class: "skeleton_ManageButtonDiv",
          }).inject(r)),
            (this.elements.loadingArea = new p({ visible: !1 })),
            this.elements.loadingArea.inject(this.elements.manageButtonDiv);
          var m = {
            className: "TopButtonValid",
            emphasize: "primary",
            setAttribute: { name: "TopButtonValid" },
            clickCallback: function () {
              t._saveAllFacets();
            },
          };
          if (!0 === t.options.updateSession)
            (m.label = E.get("EP_OK")), (this.elements.boutonValid = v(m));
          else {
            (m.label = E.get("EP_Ok")), (this.elements.boutonValid = v(m));
          }
          this.elements.boutonValid.hide(),
            this.elements.boutonValid.inject(this.elements.manageButtonDiv),
            (this.elements.facetButtonDiv = UWA.createElement("div", {
              class: "skeleton_FacetButtonDiv",
              html: {},
            }).inject(this.elements.manageButtonDiv));
          if (
            (t.disableAddExtensionButton ||
              require([
                "DS/EditPropExtensions/UI/AddExtensionsWidget",
              ], function (e) {
                var i = {
                  className: "TopButtonAdd",
                  label: E.get("EP_Add"),
                  setAttribute: { name: "TopButtonAdd" },
                  clickCallback: function () {
                    t._loadExtensions();
                  },
                };
                (t.elements.boutonAdd = v(i)),
                  !1 === t.editMode && t.elements.boutonAdd.hide(),
                  !0 === t.options.updateSession && t.elements.boutonAdd.hide(),
                  t.elements.boutonAdd.inject(t.elements.manageButtonDiv);
              }),
            (this.elements.facetButtonDiv = UWA.createElement("div", {
              class: "skeleton_FacetButtonDiv",
              html: {},
            }).inject(this.elements.manageButtonDiv)),
            this.options.closeButton)
          ) {
            var f = {
              label: E.get("EP_Close"),
              className: "TopButtonClose",
              setAttribute: { name: "TopButtonClose" },
              clickCallback: function () {
                t.checkChangeAuthorized(function () {
                  t.dispatchEvent("onCancel"),
                    t.options.displayType === i.TRANSIENT
                      ? require([
                          "DS/TransientWidget/TransientWidget",
                        ], function (e) {
                          e.closeWidget();
                        })
                      : t.dispatchEvent("onInnerCancel");
                });
              },
            };
            (this.elements.boutonCancel = v(f)),
              this.elements.boutonCancel.inject(this.elements.manageButtonDiv);
          }
        },
        _switchFacet: function (e) {
          g.publish(`${e}:${widget.id}`, e);
          var t = e !== this.currentFacetName;
          (this.currentFacetName = e),
            t && this._manageFacetLaunching(),
            this._switchAdd(!0);
        },
        _createTabs: function (e) {
          var t = this;
          !0 === this.options.showFacets &&
            (this.elements.tabs &&
              this.elements.tabs.getTabCount() > 0 &&
              (this.elements.tabs.destroy(), (this.elements.tabs = null)),
            this.elements.tabs ||
              ((this.elements.tabs = new m({
                displayStyle: "strip",
                centeredFlag: !0,
                showComboBoxFlag: !0,
                editableFlag: !1,
                multiSelFlag: !1,
                reorderFlag: !1,
              })),
              (this.elements.tabs.touchMode = o.isTouchDevice()),
              this.elements.tabs.onSelectTab(function (e) {
                if (void 0 !== e && void 0 !== e.index && e.index >= 0) {
                  var i = t.elements.tabs.getValueByIndex(e.index);
                  t._switchFacet(i);
                }
              }),
              this.elements.tabs.inject(this.elements.tabContainer)),
            (e = e || this._lFacets).forEach(function (e) {
              var i = t.elements.tabs.add({
                label: !0 === t.options.facetLabel ? e.facet.text : void 0,
                value: e.facet.name,
                allowClosableFlag: !1,
                icon: { iconName: e.facet.icon, fontIconFamily: 1 },
              });
              (i.getModel().tooltipInfos = new f({
                shortHelp: e.facet.text,
                mouseRelativePosition: !1,
              })),
                i.getContent().setAttribute("tab_name", e.facet.name);
            }),
            e.length > 1
              ? this.elements.tabs.show()
              : this.elements.tabs.hide());
        },
        _saveAllFacets: function () {
          var e = this;
          this._lFacets.forEach(function (t) {
            var i = e._getFacetContent(t);
            i && !1 !== t._isVisible && i.onSave();
          });
        },
        _loadExtensions: function () {
          var e = this;
          this._lFacets.forEach(function (t) {
            var i = e._getFacetContent(t);
            i &&
              !1 !== t._isVisible &&
              i.getTabId() == e.currentFacetName &&
              i.getExtensions();
          });
        },
        checkChangeAuthorized: function (e, t) {
          var i = this;
          if (
            !0 === this.options.warningOnUnsaved &&
            this._checkSaveForAllFacets()
          ) {
            this.elements.superModal.confirm(
              E.get("EP_checkValidClose"),
              E.get("EP_Confirmation"),
              function (n) {
                (!0 !== n && !0 !== t) || e.call(i, n);
              }
            );
          } else e.call(this, !0);
        },
        _checkSaveForAllFacets: function () {
          var e = this,
            t = !1;
          return (
            this._lFacets.every(function (i) {
              var n = e._getFacetContent(i);
              return n && !1 !== i._isVisible && (t = !n.nothingToSave()), !t;
            }),
            t
          );
        },
        getSharedContext: function () {
          return this._sharedContext;
        },
        setSharedContext: function (e) {
          (this._sharedContext = e), this.onRefresh();
        },
        getContainer: function () {
          return this.elements.container;
        },
        getMainTable: function () {
          return this.elements.bottomArea;
        },
        getIdentityCardUI: function () {
          return this.elements.topTable;
        },
        getCollapseICPreferenceName: function () {
          return i.COLLAPSE_IC_PREFERENCE.name;
        },
        destroyComponent: function () {
          var e = this;
          this._lFacets.forEach(function (t) {
            var i = e._getFacetContent(t);
            i && i.destroyComponent();
          }),
            this.elements.tabs.destroy();
        },
        getOptimalSize: function () {
          var e = this._getCurrentFacetContent(),
            t = this._getSizeTopTable(),
            i = { width: 0, height: 0 };
          return e && ((i = e.getOptimalSize()).height += t.height), i;
        },
        _getSizeTopTable: function () {
          var e = this.elements.topTable.getSize();
          return (
            "none" === this.elements.topTable.getStyle("display") &&
              (e = { height: 0, width: 0 }),
            e
          );
        },
        _getThumbnailDiv: function () {
          return (
            this.thumbnail ||
              (this.thumbnail =
                this.elements.container.getElement(".thumbnail-section")),
            this.thumbnail
          );
        },
        _refreshCurrentFacet: function () {
          var e = this._getCurrentSelectedFacet(),
            t = this._getFacetContent(e);
          t && t.onRefresh();
        },
        onRefresh: function () {
          var e = this,
            t = this._getCurrentSelectedFacet();
          this._refreshCurrentFacet(),
            this._lFacets.forEach(function (i) {
              ((!0 === i.alwaysCalled && !1 !== i._isVisible) ||
                (!0 === i.calledIfVisible && !0 === i._isVisible)) &&
                e._getFacetName(i) !== e._getFacetName(t) &&
                e._loadFacet(i, !1, function () {
                  var t = e._getFacetContent(i);
                  t && t.onRefresh();
                });
            });
        },
        _manageThumbnailDisplay: function () {
          var e = this.elements.container.getSize(),
            t = !0 !== this.options.noThumbnail && e.height >= 300;
          this.elements.postIdCard &&
            this.elements.postIdCard.showThumbnail("height", t);
        },
        onResize: function (e) {
          var t = this._getCurrentFacetContent();
          t && t.onResize(),
            this._manageThumbnailDisplay(),
            this.elements.postIdCard && this.elements.postIdCard.onResize(),
            this.elements.buttonsBar.onResize();
        },
        _getFacetFromName: function (e) {
          var t = null;
          return (
            this._lFacets.every(function (i) {
              return i.facet && i.facet.name === e && (t = i), !t;
            }),
            t
          );
        },
        getFacetContent: function (e) {
          var t = this._getFacetFromName(e);
          return t ? this._getFacetContent(t) : null;
        },
        _getFacetContent: function (e) {
          return e && e.content && "object" == typeof e.content
            ? e.content
            : null;
        },
        _getCurrentFacetContent: function () {
          var e = this._getCurrentSelectedFacet();
          return e ? this._getFacetContent(e) : null;
        },
        _getCurrentSelectedFacet: function () {
          var e = null,
            t = null;
          if (this.currentFacetName && this.options.showFacets)
            (e = this.currentFacetName) &&
              this._lFacets.every(function (i) {
                return i.facet && i.facet.name === e && (t = i), !t;
              });
          else {
            var n = null;
            if (
              (this._lFacets.length > 0 &&
                ((n = this._lFacets[0]),
                this.indexModeParameter &&
                  this._lFacets.every(function (e) {
                    return (
                      !e.facet ||
                      !e.facet.name ||
                      e.facet.name !== i.FACET_iPROPERTIES ||
                      ((n = e), !1)
                    );
                  }),
                this._lFacets.every(function (e) {
                  var t = e.alwaysVisible;
                  return t && (n = e), !t;
                })),
              widget.getValue("X3DContentId") &&
                "" !== widget.getValue("X3DContentId"))
            ) {
              var s = JSON.parse(widget.getValue("X3DContentId"));
              if (
                s &&
                s.data &&
                Array.isArray(s.data.items) &&
                s.data.items.length > 0
              ) {
                var a = s.data.items[0];
                if (
                  a.facet &&
                  "isSocial" === a.facet &&
                  this._lFacets.length > 0
                ) {
                  n = this._lFacets[0];
                  for (var o = 0; o < this._lFacets.length; o++)
                    this._lFacets[o].facet.name == i.FACET_SWYMCOMMENTS &&
                      (n = this._lFacets[o]);
                }
              }
            }
            t = n;
          }
          return t;
        },
        _updateFacets: function () {
          var e = this._getCurrentFacetContent();
          e && e.display();
        },
        _hideAllFacetsContent: function (e) {
          var t = this,
            i = e ? this._getFacetName(e) : null;
          this._lFacets.forEach(function (e) {
            var n = t._getFacetContent(e);
            n && t._getFacetName(e) !== i && n.hide();
          });
        },
        _manageFacetLaunching: function (e) {
          var t = ++window.top.perfoIncrem;
          window.top.performance.mark("start_launchFacets:" + t);
          var i = this,
            n = this._getCurrentSelectedFacet(),
            s = this.getSharedContext();
          !0 !== e && this._launchFacet(),
            window.top.performance.mark("start_Iterate:" + t);
          var a = i._getFacetName(n);
          (this.currentFacetName = a),
            this._lFacets.forEach(function (e) {
              if (
                ((!0 === e.alwaysCalled &&
                  !1 !== e._isVisible &&
                  void 0 !== e._isVisible) ||
                  (!0 === e.calledIfVisible && !0 === e._isVisible)) &&
                i._getFacetName(e) !== a
              ) {
                var t = ++window.top.perfoIncrem;
                window.top.performance.mark("start_loadFacet:" + t),
                  i._loadFacet(e, !1, function () {
                    window.top.performance.mark("end_loadFacet:" + t);
                    var n = i._getFacetContent(e);
                    n && n.setContext(s);
                    var a = i._getCurrentSelectedFacet();
                    i._getFacetName(a) === i._getFacetName(e) &&
                      i.__launchFacet(a);
                  });
              }
            }),
            window.top.performance.mark("end_Iterate:" + t),
            window.top.performance.mark("end_launchFacets:" + t);
        },
        _launchFacet: function () {
          var e = this,
            t = this._getCurrentSelectedFacet();
          return (
            this._hideAllFacetsContent(t),
            this._loadFacet(t, !0, function () {
              e.__launchFacet(t);
            }),
            t
          );
        },
        _loadFacet: function (e, t, i) {
          var n = this;
          if (e && "loading" !== e.content)
            if (e.content) i.call(n);
            else {
              e.content = "loading";
              var s = ++window.top.perfoIncrem;
              window.top.performance.mark("start_prereqFacet:" + s),
                (function (e, t) {
                  var i = [],
                    n = [];
                  e.forEach(function (e) {
                    var t = C[e];
                    t ? n.push(t) : i.push(e);
                  }),
                    i.length > 0
                      ? require(i, function () {
                          for (var s = 0; s < arguments.length; s++)
                            (e[i[s]] = arguments[s]), n.push(arguments[s]);
                          t.apply(null, n);
                        })
                      : t.apply(null, n);
                })([e.require], function (a) {
                  window.top.performance.mark("end_prereqFacet:" + s),
                    (e.content = null);
                  var r = n._getCurrentSelectedFacet(),
                    l = n._getFacetName(e) === n._getFacetName(r);
                  if (!t || l) {
                    var c = e.options ? e.options : {};
                    if (Object.keys(c).length > 0) {
                      var d = n.infoForFacet;
                      if (
                        (c.infoForFacet,
                        null != d && (e.options.infoForFacet = d),
                        void 0 === c.tenant || null === c.tenant)
                      ) {
                        var u = n._getTenant();
                        null === u && (u = o.getTenantID()),
                          (e.options.tenant = u);
                      }
                    }
                    var h = c.events ? c.events : {};
                    (h.onNotification = function (e) {
                      n._addNotif(e);
                    }),
                      (h.onEvent = function (e) {
                        n._manageEvent(e);
                      }),
                      (c.events = h),
                      (e.options = c),
                      (e.options.tabId = e.facet.name),
                      (e.options.eventThread =
                        n.options.eventThread + "." + e.facet.name),
                      (e.content = new a(e.options, n._sharedOptions));
                    var p = e.content.getUI(),
                      m =
                        "function" == typeof p.addClassName
                          ? p
                          : p.container
                          ? p.container
                          : p.elements
                          ? p.elements.container
                          : null;
                    m &&
                      m.addClassName("EP_tab_Container EP_tab_" + e.facet.name),
                      e.content.getUI().inject(n.elements.bottomArea),
                      l || e.content.hide(),
                      n.publishEvt("facetLoaded", e),
                      i.call(n);
                  }
                });
            }
        },
        _supportExtension: function () {
          let e = !0,
            t = !1,
            n = null;
          if (
            ("function" == typeof this.getOccurrences &&
              (n = this.getOccurrences()),
            n && n.length > 0)
          ) {
            let t = n[0].getModel(i.MODEL_INIT);
            if (t) {
              let i = c.getServiceForModel(t);
              i && (e = !1 !== i.extensionsSupported);
            }
          }
          let s = c.getMapping();
          return (
            s &&
              s[this.currentFacetName] &&
              c.getMapping()[this.currentFacetName].supportExtension &&
              (t = !0),
            e && t
          );
        },
        _switchSave: function (e) {
          e
            ? (this.elements.boutonValid.show(),
              this.elements.boutonAdd &&
                (this.elements.boutonAdd.disabled = !0))
            : (this.elements.boutonValid.hide(),
              this.elements.boutonAdd &&
                (this.elements.boutonAdd.disabled = !1));
        },
        _switchAdd: function (e) {
          e && !0 !== this.options.updateSession && this._supportExtension()
            ? this.elements.boutonAdd &&
              this._sharedOptions.editMode &&
              this.elements.boutonAdd.show()
            : this.elements.boutonAdd && this.elements.boutonAdd.hide();
        },
        _manageActionVisibility: function () {
          var e = this,
            t = this._getCurrentSelectedFacet(),
            i = t ? t.actions : null,
            n = [];
          i &&
            i.forEach(function (i) {
              var s = i,
                a = i;
              "object" == typeof i ? (s = i.name) : (a = {});
              var o = !0,
                r = e._getFacetContent(t);
              if (
                (r &&
                  (o =
                    "pencil" !== s
                      ? r.isActionAvailable(s)
                      : "pencil" === s &&
                        !0 === e._RWAccess &&
                        r.isActionAvailable(s)),
                o)
              ) {
                var l = S[s];
                for (var c in l) a[c] || (a[c] = l[c]);
                (a.handler = function () {
                  e._getCurrentFacetContent().onAction(s);
                }),
                  n.push(a);
              }
            });
          i = this.options.actions;
          this.elements.postIdCard &&
            i &&
            i.length > 0 &&
            this.elements.postIdCard.setActions(i),
            this.elements.buttonsBar.setButtons(n),
            n.length > 0
              ? this.elements.buttonsBar.show()
              : this.elements.buttonsBar.hide();
        },
        _manageEvent: function (e) {
          var t = this._getCurrentSelectedFacet(),
            i = this._getFacetName(t);
          if ("switchSave" === e.eventID) {
            var n = e.value;
            !0 !== n && (n = this._checkSaveForAllFacets()),
              this._switchSave(n);
          } else if ("switchAdd" === e.eventID) {
            var s = e.value;
            this._switchAdd(s);
          } else if ("switchLoader" === e.eventID) {
            var a = e.value,
              o = this.loaderFacetList.indexOf(e.facet);
            !0 === a && o < 0
              ? this.loaderFacetList.push(e.facet)
              : !1 === a && o >= 0 && this.loaderFacetList.splice(o, 1),
              this.loaderFacetList.length > 0
                ? this._displayLoadingArea()
                : this._hideLoadingArea();
          } else
            e.facet === i && "updateActions" === e.eventID
              ? this._manageActionVisibility()
              : "updateRWA" === e.eventID &&
                (this._updateRWA(e.value), this._manageActionVisibility());
        },
        _updateRWA: function (e) {
          this._RWAccess = e;
        },
        _hideLoadingArea: function () {
          this.elements.loadingArea.hide();
        },
        _displayLoadingArea: function () {
          this.elements.loadingArea.show();
        },
        __launchFacet: function (e) {
          var t = ++window.top.perfoIncrem,
            i = this._getFacetContent(e);
          if (i) {
            !0 === this.options.showTopBar
              ? this.elements.globalDiv.addClassName("withMenuBar")
              : this.elements.globalDiv.removeClassName("withMenuBar");
            var n = i.getButtons();
            this.elements.facetButtonDiv.empty(),
              n && n.inject(this.elements.facetButtonDiv),
              window.top.performance.mark("start_displayFacets:" + t),
              i.display(this.getSharedContext()),
              window.top.performance.mark("end_displayFacets:" + t),
              this._manageActionVisibility();
          }
        },
        _getFacetName: function (e) {
          return e && e.facet ? e.facet.name : "";
        },
        _updateThumbnail: function (e, t) {
          this.elements.postIdCard &&
            this.elements.postIdCard.changeThumbnail(e, t);
        },
        clearIdentityCard: function () {
          this.elements.postIdCard && this.elements.postIdCard.clearValues();
        },
        getIdentityCard: function () {
          return this.elements.postIdCard;
        },
        selectFacet: function (e) {
          var t = this._getFacetFromName(e);
          if (this.elements.tabs && t && !1 !== t._isVisible) {
            var i = this.elements.tabs.getButtonFromValue(e);
            i && (i.setProperties({ checkFlag: !0 }), this._switchFacet(e));
          }
        },
        clearPanel: function () {
          this._hideAllFacetsContent();
        },
        hideMainComponent: function () {
          this.elements.mainInterface.hide();
        },
        showMainComponent: function () {
          this.elements.mainInterface.show();
        },
        _manageFacetVisibility: function () {
          var e = this;
          this._hideAllFacets();
          var t = ++window.top.perfoIncrem;
          window.top.performance.mark("start_facetVisibility:" + t);
          var i = new a(["B", "C"], function () {
            var i = this.getAnswer("B"),
              n = this.getAnswer("C"),
              s = e._facetCheck(this);
            if (!0 === s.success) {
              var a = e._getCurrentSelectedFacet();
              this.currentFacetName = e._getFacetName(a);
              var o = !0,
                r = null,
                l = !1;
              e._lFacets.forEach(function (t) {
                var c = t.facet.name,
                  d = a && e._getFacetName(a) === c,
                  u = t._isVisible || !0 === t.alwaysVisible;
                u &&
                  t.services &&
                  t.services.forEach(function (e) {
                    i[e] || (u = !1);
                  }),
                  u &&
                    t.roles &&
                    ((u = !1),
                    t.roles.forEach(function (e) {
                      n.indexOf(e) >= 0 && (u = !0);
                    })),
                  !r && u && (r = c),
                  u && d && (o = !1),
                  (t._isVisible = u),
                  !0 === e.indexModeParameter
                    ? "properties" === c
                      ? (t._isVisible = !1)
                      : "iproperties" === c && (t._isVisible = !0)
                    : "iproperties" === c && (t._isVisible = !1),
                  !0 === s.refreshCurrent && d && !o && (l = !0);
              }),
                e._facetVisibilityUpdate(),
                o && e.selectFacet(r),
                !l && s.refreshAction && e._manageActionVisibility(),
                window.top.performance.mark("end_facetVisibility:" + t),
                e._manageFacetLaunching(!l);
            }
          });
          if (!0 === e._queryFacets(i)) {
            var n = e._getTenant();
            window.top.performance.mark("start_visibilityUrl"),
              r.getServices(n, function (e) {
                window.top.performance.mark("end_visibilityUrl"),
                  i.setAnswer("B", e);
              }),
              window.top.performance.mark("start_visibilityRole"),
              r.getRoles(n, function (e) {
                window.top.performance.mark("end_visibilityRole"),
                  i.setAnswer("C", e);
              });
          }
        },
        _getTenant: function () {
          return o.getTenantID();
        },
        _queryFacets: function (e) {
          return !0;
        },
        _facetCheck: function (e) {
          return (
            this._lFacets.forEach(function (e) {
              e._isVisible = !0;
            }),
            { success: !0 }
          );
        },
        _getPriorityFacetName: function () {
          var e = null;
          null === e &&
            null != (t = this.defaultFacetName) &&
            "" !== t &&
            (e = t);
          if (
            (null === e &&
              this.currentFacetName &&
              this.options.showFacets &&
              (e = this.currentFacetName),
            null === e)
          ) {
            if (null !== this.options) var t = this.options.defaultFacetName;
            null != t && "" !== t && (e = t);
          }
          if (
            null === e &&
            widget.getValue("X3DContentId") &&
            "" !== widget.getValue("X3DContentId")
          ) {
            var n = JSON.parse(widget.getValue("X3DContentId"));
            if (
              n &&
              n.data &&
              Array.isArray(n.data.items) &&
              n.data.items.length > 0
            ) {
              var s = n.data.items[0];
              s.facet && "isSocial" === s.facet && (e = i.FACET_SWYMCOMMENTS);
            }
          }
          return null === e && (e = "properties"), e;
        },
        _facetVisibilityUpdate: function () {
          var e = this;
          if (this.elements.tabs) {
            var t = this._getPriorityFacetName(),
              i = e._getFacetFromName(t),
              n = this._getCurrentSelectedFacet();
            i !== n && (n = i);
            var s = null,
              a = !1,
              o = [];
            this._lFacets.forEach(function (t) {
              !1 !== t._isVisible &&
                !1 !== t.visible &&
                (!0 === e.options.updateSession
                  ? "properties" === t.facet.name && o.push(t)
                  : o.push(t),
                n === t && (null === s && (s = t), (a = !0)));
            }),
              null === s &&
                this._lFacets.forEach(function (e) {
                  !1 !== e._isVisible &&
                    !1 !== e.visible &&
                    null === s &&
                    (s = e);
                }),
              this._createTabs(o),
              a && n.content && n.content.display
                ? (this.selectFacet(n.facet.name),
                  n.content.display(this.getSharedContext()))
                : s && this.selectFacet(s.facet.name);
          }
        },
        removeFacets: function (e) {
          var t = [],
            i = this._getCurrentSelectedFacet(),
            n = !1;
          this._lFacets.forEach(function (s) {
            e.indexOf(s.facet.name) < 0
              ? t.push(s)
              : i && i.facet.name === s.facet.name && (n = !0);
          }),
            (this._lFacets = t),
            this._facetVisibilityUpdate(),
            n && this._manageFacetLaunching();
        },
        getOrAddFacets: function (e) {
          var t = this,
            i = [];
          e.forEach(function (e) {
            t._getFacetFromName(e.facet.name) || i.push(e);
          }),
            this.addFacets(i);
        },
        addFacets: function (e) {
          if (e && (!Array.isArray(e) || 0 !== e.length)) {
            var t = this,
              i = !0;
            if (e && Array.isArray(e)) {
              var n = {};
              t._lFacets.forEach(function (e) {
                e.facet && (n[e.facet.name] = !0);
              }),
                e.forEach(function (e) {
                  e.facet &&
                    !n[e.facet.name] &&
                    ((e._isVisible = !0 === e.alwaysVisible),
                    (i = i && e._isVisible),
                    void 0 !== e.position
                      ? t._lFacets.splice(
                          Math.min(
                            e.position,
                            Object.keys(t._lFacets).length - 1
                          ),
                          0,
                          e
                        )
                      : t._lFacets.push(e));
                });
            }
            i ? this._facetVisibilityUpdate() : this._manageFacetVisibility();
          }
        },
        _hideAllFacets: function () {
          this._createTabs([]);
        },
        _isFacetAuthorized: function (e) {
          this._getCurrentModelType();
          var t = this._getFacetFromName(e);
          return t && !1 !== t._isVisible;
        },
        changeAttributes: function (e) {
          var t = this._getCurrentFacetContent();
          null != t && t.changeAttributes(e);
        },
        setIndexModeFlag: function (e) {
          this.indexModeParameter = e;
        },
        getIndexModeFlag: function () {
          return !0 === this.indexModeParameter;
        },
      });
    }
  ),
  define(
    "DS/EditPropWidget/UI/IDCardManager",
    [
      "DS/EditPropWidget/UI/IDCardSkeleton",
      "DS/EditPropWidget/constants/EditPropConstants",
      "DS/EditPropWidget/constants/ConstantsMapping",
      "DS/EditPropWidget/comm/DistantAjax",
      "DS/EditPropWidget/models/EditPropModel",
      "DS/EditPropWidget/models/EditPropModelMerge",
      "DS/EditPropWidget/models/EditPropAttributeModel",
      "css!DS/EditPropWidget/EditPropWidget.css",
      "i18n!DS/EditPropWidget/assets/nls/langEditProp",
    ],
    function (e, t, i, n, s, a, o, r, l) {
      "use strict";
      return (
        (window.top.perfoIncrem = window.top.perfoIncrem
          ? window.top.perfoIncrem
          : 0),
        e.extend({
          _lOccurrences: [],
          _faceVisibilityHandler: null,
          defaultOptions: { multiselection: t.MULTISEL_MERGE },
          init: function (e, t) {
            (this._lOccurrences = []),
              (this._faceVisibilityHandler = { handle: null }),
              (this.sourceAppId = t.sourceAppId),
              this._parent(e, t);
          },
          getModels: function () {
            var e = this.getOccurrences(),
              i = [];
            return (
              e &&
                e.forEach(function (e) {
                  var n = e.getModel(t.MODEL_INIT);
                  n && i.push(n);
                }),
              i
            );
          },
          notifyClose: function () {
            var e = this;
            this._lFacets.forEach(function (t) {
              var i = e._getFacetContent(t);
              i && "function" == typeof i.onClose && i.onClose();
            });
          },
          getModel: function (e, t) {
            var i = null;
            return (
              (t ? t.getAllModels(!0) : this.getModels()).every(function (t) {
                return e !== t.get("objectId") || ((i = t), !1);
              }),
              i
            );
          },
          _getFirstFacetModel: function (e) {
            var t = null,
              i = this._getFirstOccurrence();
            return i && (t = i.getModel(e)), t;
          },
          _getFirstInitModel: function () {
            return this._getFirstFacetModel(t.MODEL_INIT);
          },
          _getFirstPropertiesModel: function () {
            return this._getFirstFacetModel(t.FACET_PROPERTIES);
          },
          getOccurrences: function () {
            return this._lOccurrences;
          },
          _getFirstOccurrence: function () {
            return this._lOccurrences.length > 0 ? this._lOccurrences[0] : null;
          },
          getSharedContext: function () {
            return this._getFirstOccurrence();
          },
          setOccurrences: function (e) {
            var i = window.top.perfoIncrem++;
            window.top.performance.mark("start_setModelSkeleton:" + i);
            var n = this;
            this._clearAllFacetsModels(), (this.modelHasChanged = !0);
            var s = e.length <= 0,
              a = null;
            if (
              s ||
              (this.options.multiselection === t.MULTISEL_NO && e.length > 1) ||
              (n.indexModeParameter &&
                this.options.multiselection &&
                e.length > 1)
            )
              a = l.get(s ? "NoItemSelected" : "TooManyObjectSelected");
            else if (
              e.length > 1 &&
              this.options.multiselection === t.MULTISEL_MERGE
            ) {
              var o,
                r = (o = e[0]).getModel(t.MODEL_INIT);
              if (r) {
                var c = r.getMergingModels(),
                  d = null,
                  u = null;
                for (var h in c) {
                  var p = c[h],
                    m = p.get("tenant"),
                    f = p.get("source");
                  if (
                    (null === d
                      ? (d = m)
                      : d !== m && (a = l.get("DifferentTenantsNotSupported")),
                    null === u
                      ? (u = f)
                      : u !== f && (a = l.get("DifferentSourcesNotSupported")),
                    null !== a)
                  )
                    break;
                }
              }
            }
            if (null !== a) {
              if (
                (this.clearIdentityCard(),
                this.showMainErrorArea(a),
                this.dispatchEvent("onTitleChanged", ""),
                this.hideMainComponent(),
                (this._lOccurrences = []),
                n.indexModeParameter)
              )
                return;
            } else
              this.showMainComponent(),
                (o = e[0]).addEvent(
                  "onChange:" + t.FACET_PROPERTIES,
                  function () {
                    n._initializeIDCard(o);
                  }
                ),
                this._initializeIDCard(o),
                (this._lOccurrences = e);
            window.top.performance.mark("end_setModelSkeleton:" + i),
              this._manageFacetVisibility(),
              this._manageFacetLaunching();
          },
          __queryFacets: function (e) {
            var t = this;
            this._faceVisibilityHandler.handle &&
              (this._faceVisibilityHandler.handle.cancel(),
              (this._faceVisibilityHandler.handle = null));
            var i = this._getFirstInitModel();
            if (i) {
              var s = [],
                a = !1;
              t._lFacets.forEach(function (e) {
                (i.isMergedModel() && !e.supportMerge) ||
                  (s.push({ name: e.facet.name, inheritance: e.inheritance }),
                  (a = a || (e.services && e.services.length > 0)));
              }),
                t.indexModeParameter
                  ? n.launchFacetsVisibilityFromIndexService(i, s, {
                      context: this.options.context,
                      handleRequest: this._faceVisibilityHandler,
                      onComplete: function (i) {
                        i && !1 !== i.success
                          ? e.setAnswer("A", i)
                          : t._addNotif({
                              msg: i ? i.error.message : l["Error Server"],
                              eventID: "error",
                            });
                      },
                    })
                  : n.launchFacetsVisibility(
                      i,
                      s,
                      {
                        context: this.options.context,
                        handleRequest: this._faceVisibilityHandler,
                        onComplete: function (i) {
                          i && !1 !== i.success
                            ? e.setAnswer("A", i)
                            : t._addNotif({
                                msg: i ? i.error.message : l["Error Server"],
                                eventID: "error",
                              });
                        },
                      },
                      void 0,
                      t.sourceAppId
                    );
            }
          },
          _queryFacets: function (e) {
            var t = this;
            e.addRequest("A");
            var n = this._getFirstInitModel();
            if (n) {
              var s = i.getServiceForModel(n);
              n.get("isTransient") || !0 !== s.modelNeeded
                ? t.__queryFacets(e)
                : (e.addRequest("Fed"),
                  require([
                    "DS/EditPropWidget/ajaxRequests/FederatedAjaxRequests",
                  ], function (i) {
                    i.fillModel(
                      {
                        onComplete: function (i) {
                          var n = i.get("ds6w:type");
                          if ((n = n || i.get("ds6w:what/ds6w:type"))) {
                            var s = n.get("valueDB");
                            i.set("ds6w:what/ds6w:type_value", s);
                          }
                          i.set("preview_url", i.get("previewUrl")),
                            t.__queryFacets(e),
                            e.setAnswer("Fed", !0);
                        },
                      },
                      n
                    );
                  }));
            }
            return !!n;
          },
          _facetCheck: function (e) {
            var i = this,
              n = e.getAnswer("A"),
              r = e.getAnswer("Fed"),
              l = n.id,
              c = n.facets,
              d = i._getFirstInitModel(),
              u = i._getCurrentSelectedFacet(),
              h = null !== d && d.get("objectId") === l,
              p = null,
              m = !!r,
              f = !1;
            if (h && n.notification)
              return (
                this.clearIdentityCard(),
                this.showMainErrorArea(n.notification.message),
                this.hideMainComponent(),
                { success: !1 }
              );
            if (h) {
              var g = i._getFirstOccurrence(),
                v = n.mains,
                _ = v ? v[l] : null,
                E = l,
                C = "empty",
                S = null;
              _ &&
                ((E = _.id),
                (C = _.type),
                (S = _.metatype
                  ? "bus" == _.metatype
                    ? t.METATYPE_BUS
                    : t.METATYPE_REL
                  : t.METATYPE_UNKNOWN));
              var y = !1,
                A = {};
              if (((p = g.getModel(t.FACET_PROPERTIES)), d.isMergedModel())) {
                (E = l), (C = "mergedObject");
                var I = d.getMergingModels(),
                  b = p && p.isMergedModel() ? p.getMergingModels() : null;
                for (var P in I) {
                  var T = I[P],
                    D = v[P],
                    M = null != D && D.id ? D.id : P,
                    w = t.METATYPE_UNKNOWN;
                  if (
                    ((w = D.metatype
                      ? "bus" == D.metatype
                        ? t.METATYPE_BUS
                        : t.METATYPE_REL
                      : w),
                    (C = D ? D.type : ""),
                    !(b ? b[M] : null) && M !== P)
                  )
                    (y = !0),
                      ((F = UWA.clone(T.getOptions())).objectId = M),
                      (T = new s(F));
                  T.set("metatype", w),
                    T.set(t.MODEL_DS_ATTR + "type", {
                      valueDB: C,
                      isTransient: !0,
                    });
                  var O =
                    w === t.METATYPE_REL ? n.hierarchiesRel : n.hierarchiesBus;
                  T.set(t.MODEL_DS_ATTR + "types", {
                    valueDB: O ? O[C] : null,
                    isTransient: !0,
                  }),
                    (A[M] = T);
                }
                p = g.getModel(t.FACET_PROPERTIES);
              } else null === p && E === l && (p = d);
              if (y || !p) {
                var F,
                  x = p || d,
                  W = x.getAllAttributes();
                !0 !== x.get("isTransient") && (W = x.getOptions()),
                  ((F = UWA.clone(W)).objectId = E),
                  S && (F.metatype = S),
                  (F[t.MODEL_DS_ATTR + "type"] = {
                    valueDB: C,
                    isTransient: !0,
                  });
                O = w === t.METATYPE_REL ? n.hierarchiesRel : n.hierarchiesBus;
                for (var R in ((F[t.MODEL_DS_ATTR + "types"] = {
                  valueDB: O ? O[C] : null,
                  isTransient: !0,
                }),
                F))
                  W[R] instanceof o && (F[R] = new o(F[R]));
                d.isMergedModel()
                  ? (p = new a(F)).setMergingModels(A)
                  : (p = new s(F));
              } else if (p) {
                p.set(t.MODEL_DS_ATTR + "type", {
                  valueDB: C,
                  isTransient: !0,
                });
                O = w === t.METATYPE_REL ? n.hierarchiesRel : n.hierarchiesBus;
                p.set(t.MODEL_DS_ATTR + "types", {
                  valueDB: O ? O[C] : null,
                  isTransient: !0,
                });
              }
              g.setModel(t.FACET_PROPERTIES, p);
              c = n.facets;
              i._lFacets.forEach(function (e) {
                var t = e.facet.name,
                  n = c[t],
                  s = n && (!0 === n || !0 === n.available);
                ((e._isVisible = s),
                !0 === e.inheritance && i._getFacetName(u) === t && (f = !0),
                n && n.information) &&
                  (g.setOptions(t, n.information),
                  i._getFacetName(u) === t && (m = !0));
              });
              let e = n.secondaryOptions;
              e &&
                Array.isArray(e) &&
                e.length > 0 &&
                g.setSecondaryOptionsForFacet(e);
            }
            return { success: !!p, refreshCurrent: m, refreshAction: f };
          },
          _getTenant: function () {
            var e = this._getFirstInitModel(),
              t = null;
            return e && (t = e.getTenant() ? e.getTenant() : this._parent()), t;
          },
          _clearAllFacetsModels: function () {
            var e = this;
            this._lFacets.forEach(function (t) {
              var i = e._getFacetContent(t);
              i && i.setOccurrence(null);
            });
          },
          _initializeIDCard: function (e) {
            var i = this;
            if (e) {
              var n = e.getModel(t.FACET_PROPERTIES);
              n &&
                (this._updateThumbnailForModel(n),
                this.elements.postIdCard &&
                  this.elements.postIdCard.fillFromModel(n),
                this.elements.postIdCard &&
                  n.addEvent("onChangeAttributes", function (e, t) {
                    var n = !1;
                    t.forEach(function (t) {
                      t &&
                        t instanceof o &&
                        (n =
                          i.elements.postIdCard.setValueForSection(e, t) || n);
                    });
                  }),
                n.addEvent("onChange:label", function () {
                  var e = n ? n.getComputedTitle() : "";
                  i.dispatchEvent("onTitleChanged", e),
                    n.get(t.MODEL_DS_ATTR + "label") &&
                      i.elements.postIdCard &&
                      i.elements.postIdCard.setValueForSection(
                        n,
                        n.get(t.MODEL_DS_ATTR + "label")
                      );
                }),
                n.addEvent("onChange:previewUrl", function () {
                  i._updateThumbnailForModel(this);
                }),
                n.addEvent("onChange:type_icon_url", function () {
                  i._updateThumbnailForModel(this);
                }));
            }
          },
          getTitle: function () {
            var e = this._getFirstOccurrence(),
              i = e ? e.getModel(t.FACET_PROPERTIES) : null;
            return i ? i.getComputedTitle() : "";
          },
          _updateThumbnailForModel: function (e) {
            if (!0 !== this.options.noThumbnail) {
              var i = e.get("previewUrl")
                ? e.get("previewUrl")
                : e.get("type_icon_url");
              i = i || "";
              var n =
                this._isFacetAuthorized(t.FACET_VIEWS) ||
                e.get("activeThumbnail");
              this._updateThumbnail(i, n);
            }
          },
          _getCurrentModelType: function () {
            var e = this.getModels(),
              t = e.length > 0 ? e[0].get("type") : "",
              i = t;
            return (
              t &&
                "object" == typeof t &&
                (i = (i = t.get("valueDB")) || t.get("value")),
              i
            );
          },
        })
      );
    }
  );
