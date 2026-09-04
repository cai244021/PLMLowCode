define("DS/ReviseWidget/ReviseWidget", [
  "css!DS/ReviseWidget/ReviseWidget",
  "UWA/Controls/Abstract",
  "DS/Controls/Editor",
  "i18n!DS/LifecycleWidget/assets/nls/LifecycleWidgetNls",
  "DS/LifecycleServices/LifecycleCommandManager",
  "DS/LifecycleServices/LifecycleServices",
  "DS/LifecycleServices/LifecycleServicesSettings",
  "DS/LifecycleServices/LifecycleAlert",
  "DS/LifecycleServices/HTMLTemplating",
  "DS/WAFData/WAFData",
  "DS/LifecycleServices/LifecycleObjectList",
  "DS/LifecycleServices/DictionaryServices",
  "DS/LifecycleControls/CommandDialog",
  "DS/LifecycleControls/DataGridViewContainer",
  "DS/LifecycleCmd/MessageMediator",
  "DS/LifecycleCmd/Dictionary",
  "DS/LifecycleCmd/SelectionUtil",
  "DS/LifecycleServices/IllegalCharMapping",
  "DS/WidgetServices/WidgetServices",
  "DS/WebToWinInfra/WebToWinCom",
  "DS/LifecycleServices/NewBranchObjectProp",
  "DS/Controls/ButtonGroup",
  "DS/Controls/Toggle",
  "DS/ErrorWidget/ErrorWidget",
  "DS/ENOCollabToolsUI/tracker/ENOCollabTracker",
  "DS/ENOCollabToolsUI/tracker/ENOCollabTrackerBackEnd",
  "i18n!DS/LifecycleServices/assets/nls/LifecycleServicesNls",
  "i18n!DS/WidgetServicesUI/assets/nls/i18nWidgetService",
], function (
  e,
  t,
  i,
  r,
  n,
  s,
  o,
  a,
  c,
  l,
  d,
  u,
  h,
  p,
  v,
  m,
  f,
  y,
  g,
  b,
  E,
  w,
  S,
  O,
  R,
  _,
  k,
  C,
) {
  "use strict";
  return t.extend({
    physicalIds: null,
    context: null,
    objectListDiv: null,
    objectsList: null,
    tenant: null,
    folderId: null,
    useNLV: !1,
    newRevisionFromOldRevisionAllowed: !1,
    commentInput: null,
    foreIntent: !1,
    init: function (e) {
      (this._parent(e),
        (this.physicalIds = []),
        (this._newTitles = new m()),
        (this.illegalChars = ""),
        (this.IllegalCharMapping = new y()),
        (this.commandManager = new n()),
        (this.selUtil = new f()),
        null != e &&
          e.hasOwnProperty("wintop") &&
          1 == e.wintop &&
          (this.container = UWA.createElement("div", {
            name: "new_revision",
            class: "revise_popup",
          })),
        (this.reviseOptions = []));
    },
    _getUniqueListBy: function (e, t) {
      return [...new Map(e.map((e) => [e[t], e])).values()];
    },
    executeCmd: function (e, t, i) {
      var n = this;
      ((e = this._getUniqueListBy(e, "physicalid")),
        this.addEvent(
          "onReady",
          function (t) {
            this._showCommandUI(e);
          },
          this,
        ),
        this.addEvent("onComplete", function (t) {
          e[0].useFIRST ? i(n.firstConfigRevisionObject) : i();
        }),
        (this.isMultipleSelection = e.length > 1),
        (this.context = t.context),
        (this.context.displayNotification = (e) => {
          this._displayNotification(e);
        }),
        (this.folderId = t.targetFolderId),
        (this.trackerEventBeginTime = Date.now()));
      try {
        if (void 0 !== o.getActiveFolder) {
          var a = o.getActiveFolder();
          void 0 !== a.id &&
            null !== a.id &&
            "" !== a.id &&
            ((this.folderId = a.id),
            (this.securityContex =
              "" !== a.sc && void 0 !== a.sc ? a.sc : this.securityContex),
            require(["DS/PlatformAPI/PlatformAPI"], function (e) {
              e.publish("FolderEditor.ActiveFolderCallBack", {
                id: a.id,
                label: a.label,
              });
            }));
        }
      } catch (e) {
        ((this.folderId = t.targetFolderId),
          (this.securityContex = o.getCurrentFolderSecurityContext()));
      }
      if (
        (this.isMultipleSelection && this._createObjectsList(), 0 === e.length)
      )
        throw r.noObject;
      (e.forEach(function (e) {
        if (
          e.hasOwnProperty("physicalid") &&
          ("" === e.physicalid || !UWA.is(e.physicalid))
        )
          throw r.objectNotFound;
        (n.physicalIds.push(e.physicalid),
          null === n.tenant &&
            ((n.tenant = e.tenant), n.commandManager.setTenant(n.tenant)));
      }),
        s.getAllowedSemanticsPromise(this.physicalIds, this.context).then(
          function (t) {
            if (s.isBaselineMode(void 0, t)) {
              if (e.length > 1)
                return void n.showError(r.multipleSelectionObjectsNotSupported);
              "ConfiguredBaseline" === e[0].type
                ? ((n.useFIRST = e[0].useFIRST),
                  n.setObjects(e, t.allowedSemantics))
                : require(["DS/HistoryExplorer/HistoryDialog"], function (r) {
                    new r().executeCmd(
                      e,
                      {
                        getAllowedSemanticsResponse: t,
                        context: n.context,
                        name: "history_command",
                      },
                      i,
                    );
                  });
            } else n.setObjects(e, t.allowedSemantics);
          },
          function (t) {
            let i = n.trackerEventBeginTime
                ? Date.now() - n.trackerEventBeginTime
                : 0,
              r = {
                UXName: "Revise",
                models: e,
                actionStatus: "failure",
                actionTime: i,
              };
            ((n.trackerEvent = R.createInitActionEvent(r)),
              R.checkAndTrackPageEvent(n.trackerEvent),
              n.showError(t));
          },
        ));
    },
    _getRevisionDetailsMultiSelect: function (e, t) {
      var i = null,
        n = [];
      (e.forEach(function (e) {
        var t = e.objectId || e.physicalid;
        n.push(t);
      }),
        s
          .getVersionNumberProposalPromise(n, "LAST", null, this.context)
          .then((e) => {
            if (e.errorID)
              throw (
                console.log(
                  "Versioning service returned error: " + JSON.stringify(e),
                ),
                e.errorMessage ? e.errorMessage : ((i = e), r.reviseFailed)
              );
            var t = new Map();
            (e &&
              Array.isArray(e.addRequests) &&
              e.addRequests.forEach((e) => {
                const str = e.code;//add by caipan 20260402
                const reg = /^[A-Z]{2}\.\d{1,2}-\d{3,4}$/;//符合数模的版本规则
                if(reg.test(str)) {
                  const result = str.includes('-') ? str.split('-')[0].split('.')[0] + '.' + (parseInt(str.split('-')[0].split('.')[1]) + 1) + '-000' : str + '-000';
                  t.set(e.copyId, result);
                }else{
                  t.set(e.copyId,  e.code);
                }
              }),
              this.preparedObjectsList.forEach((e) => {
                var i = e.objectId || e.physicalid,
                  r = t.get(i);
                r && (e.proposedRevision = r);
              }));
          })
          .then(() =>
            this._WafDataPromise(
              "/resources/lifecycle/revise/prepare_revise_maskattributes",
              { data: this.preparedObjectsList },
              t,
            ),
          )
          .then((e) => {
            if ("failure" === e.status && e.hasOwnProperty("report")) {
              var t = e.report;
              throw t.length > 0 && t[0].hasOwnProperty("message")
                ? t[0].message
                : ((i = e), r.reviseFailed);
            }
            var n = e.results;
            if (!Array.isArray(n) || 0 === n.length) throw r.objectNotFound;
            ((this.preparedObjectsList = n),
              this.dispatchEvent("displyaDataGrid", {}));
          })
          .catch((e) => {
            (console.log("Prepare revise request status is failure"),
              console.log(e),
              i ? this._showReviseFailure(i) : this.showError(e));
          }));
    },
    _createMultiSelContentProp: function (e, t) {
      var i = this;
      (s
        .getSecurityContextPromise(i.tenant, i.context)
        .then(function (e) {
          i._getRevisionDetailsMultiSelect(t, e);
        })
        .catch(function (e) {
          i.showError(e);
        }),
        this.addEvent(
          "displyaDataGrid",
          function (i) {
            this._createDataGridView(e, t);
          },
          this,
        ));
    },
    _createDataGridView(e, t) {
      ((this.dgvContainer = new p(
        { idName: "dgvContainer", clickToShowDGV: !1, showNewRevision: !0 },
        this.preparedObjectsList,
      )),
        this.dgvContainer.inject(e, "top"));
    },
    _createSingleSelContentProp: function (e) {
      ((this.objectProp = new E({
        className: "newrevision_prop",
        data_rec_id: "revise_prop_rec-id",
        showProposedRevision: !0,
        editable: !0,
      })),
        this.objectProp.inject(e));
      var t = this.preparedObjectsList[0];
      t.hasOwnProperty("attributes_mask") || (t.attributes_mask = []);
      var i = !1;
      t.attributes_mask.forEach(function (e) {
        "attribute[PLMReference.V_versionComment]" == e.selectable && (i = !0);
      });
      var r = !1,
        n = !1;
      if (
        (t.hasOwnProperty("type.kindof[PLMReference]") &&
          (r = "TRUE" === t["type.kindof[PLMReference]"]),
        t.hasOwnProperty("branch") && (n = !!t.branch),
        !i)
      ) {
        var o = {};
        n
          ? ((o.selectable = "attribute[description]"),
            (o.readOnly = !1),
            (o.path = "description"),
            (o.name = "V_versionComment"),
            (o.nls = k.LifecycleObjectList_versionComment),
            (o.multiline = !0),
            (o.maxlength = 256),
            t.attributes_mask.push(o))
          : r &&
            ((o.selectable = "attribute[PLMReference.V_versionComment]"),
            (o.readOnly = !1),
            (o.path = "PLMReference.V_versionComment"),
            (o.name = "V_versionComment"),
            (o.nls = k.LifecycleObjectList_versionComment),
            (o.multiline = !0),
            (o.maxlength = 256),
            t.attributes_mask.push(o));
      }
      if (
        (this.objectProp.setObject(this.preparedObjectsList[0]),
        t.availableSemantic &&
          t.availableSemantic.includes("FORE") &&
          "Released" !== t.current &&
          "Obsolete" !== t.current)
      ) {
        var a = this;
        s._getAncestorsPromise(t.physicalid).then(function (e) {
          if (e) {
            t.ancestors = e;
            var i = new w({ type: "checkbox" }),
              r = k.insertBefore + t.revision,
              n = new S({ type: "checkbox", label: r, value: 0 });
            i.addChild(n);
            var s = UWA.createElement("div", {
              name: "checkbox_container",
              styles: { bottom: "0px", position: "absolute" },
            });
            (i.inject(s),
              s.inject(a.objectProp.container),
              (a.foreIntentCheckbox = n));
          }
        });
      }
      this.objectProp.addEvent("onAttributeValidationError", (e) => {
        e.forEach((e) => {
          this.showNotificationInDialog(e);
        });
      });
    },
    _WafDataPromise: function (e, t, i) {
      return new Promise((n, a) => {
        var c = o.get3DSpaceWSUrl(this.tenant, e, null);
        l.authenticatedRequest(c, {
          method: "POST",
          type: "json",
          headers: o.getHeaders(i),
          timeout: 6e5,
          data: JSON.stringify(t),
          onComplete: function (e) {
            n(e);
          },
          onFailure: function (e, t) {
            (console.log("Failure..." + e), a(s.parseWebServiceError(e, t)));
          },
          ontimeout: function () {
            (console.log("A connection timeout occured."), a(r.timeout));
          },
        });
      });
    },
    _getRevisionDetails: function (e, t, i) {
      var n = this,
        o = new Map();
      t.forEach((e) => {
        o.set(e.id, e);
      });
      let a = !0;
      var c = null;
      this._WafDataPromise(
        "/resources/lifecycle/revise/prepare_revise_checkavailability",
        { data: e },
        i,
      )
        .then((e) => {
          if ("failure" === e.status && e.hasOwnProperty("report")) {
            var t = e.report;
            throw t.length > 0 && t[0].hasOwnProperty("message")
              ? t[0].message
              : ((c = e), r.reviseFailed);
          }
          a = !!e.NewRevisionFromOldRevisionAllowed;
        })
        .then(() => {
          return this._WafDataPromise(
            "/resources/lifecycle/product/attributeList",
            {
              data: e,
              attributes: [
                "current",
                "attribute[PLMReference.V_versionComment]",
                "type.kindof[PLMReference]",
                "displayType",
                "current",
              ],
            },
            i,
          );
        })
        .then((e) => {
          var t = e.results;
          if (!Array.isArray(t) || 0 === t.length) throw r.objectNotFound;
          this.preparedObjectsList = t;
        })
        .then(() => {
          this.preparedObjectsList.forEach((e) => {
            var t = e.objectId || e.physicalid,
              i = o.get(t);
            i &&
              Array.isArray(i.semantic) &&
              (e.availableSemantic = i.semantic);
          });
        })
        .then(() => {
          var t = [];
          return (
            e.forEach(function (e) {
              var i = e.objectId || e.physicalid;
              t.push(i);
            }),
            s.getVersionNumberProposalPromise(
              t,
              a ? "LAST" : "E",
              null,
              this.context,
            )
          );
        })
        .then((e) => {
          if (e.errorID)
            throw (
              console.log(
                "Versioning service returned error: " + JSON.stringify(e),
              ),
              e.errorMessage ? e.errorMessage : ((c = e), r.reviseFailed)
            );
          var t = new Map();
          (e &&
            Array.isArray(e.addRequests) &&
            e.addRequests.forEach((e) => {
                const str = e.code;//add by caipan 20260402
                const reg = /^[A-Z]{2}\.\d{1,2}-\d{3,4}$/;
                if(reg.test(str)) {
                  const result = str.includes('-') ? str.split('-')[0].split('.')[0] + '.' + (parseInt(str.split('-')[0].split('.')[1]) + 1) + '-000' : str + '-000';
                  t.set(e.copyId, result);
                }else{
                  t.set(e.copyId,  e.code);
                }
            }),
            this.preparedObjectsList.forEach((e) => {
              var i = e.objectId || e.physicalid,
                r = t.get(i);
              r && (e.proposedRevision = r);
            }));
        })
        .then(() =>
          this._WafDataPromise(
            "/resources/lifecycle/revise/prepare_revise_maskattributes",
            { data: this.preparedObjectsList },
            i,
          ),
        )
        .then((e) => {
          if ("failure" === e.status && e.hasOwnProperty("report")) {
            var t = e.report;
            throw t.length > 0 && t[0].hasOwnProperty("message")
              ? t[0].message
              : ((c = e), r.reviseFailed);
          }
          var i = e.results;
          if (!Array.isArray(i) || 0 === i.length) throw r.objectNotFound;
          ((this.preparedObjectsList = i), this.dispatchEvent("onReady", {}));
        })
        .catch((t) => {
          (console.log("Prepare revise request status is failure"),
            console.log(t),
            c ? this._showReviseFailure(c) : this.showError(t));
          let i = n.trackerEventBeginTime
              ? Date.now() - n.trackerEventBeginTime
              : 0,
            r = {
              UXName: "Revise",
              models: e,
              actionStatus: "failure",
              actionTime: i,
            };
          ((n.trackerEvent = R.createInitActionEvent(r)),
            R.checkAndTrackPageEvent(n.trackerEvent),
            this._onComplete());
        });
    },
    _getFirstVersionNumberProposal: function (e, t) {
      var i = this,
        n = {
          addRequests: [
            { copyId: e, ancestors: [{ id: e, semantic: "FIRST" }] },
          ],
        },
        a = JSON.stringify(n),
        c = o.get3DSpaceWSUrl(
          this.tenant,
          "/resources/v1/dslc/getversionnumberproposal",
          null,
        );
      l.authenticatedRequest(c, {
        method: "POST",
        type: "json",
        headers: o.getHeaders(t),
        timeout: 6e5,
        data: a,
        onComplete: function (e) {
          (console.log(e),
            Array.isArray(e.addRequests) &&
              e.addRequests.length > 0 &&
              (i.preparedObjectsList = [
                { proposedRevision: e.addRequests[0].code },
              ]),
            i.dispatchEvent("onReady", {}));
        },
        onFailure: function (e, t) {
          (console.log("Failure, object details: " + e),
            i.showError(s.parseWebServiceError(e, t)));
        },
        onTimeout: function () {
          (console.log("A connection timeout occured."),
            i.showError(r.timeout));
        },
      });
    },
    _showCommandUI: function (e) {
      var t = this,
        n = !1,
        s = document.createElement("div");
      s.className = "revise_popup dlg_cmd_content css_grid_container";
      var o = "",
        a = "";
      if (this.isMultipleSelection) {
        if (
          (this._createMultiSelContentProp(s, e),
          (a = " - " + e.length + " " + r.multipleSelectionObjects),
          (n = this.preparedObjectsList.some(function (e) {
            return !(
              !e.hasOwnProperty("type.kindof[PLMReference]") ||
              "TRUE" !== e["type.kindof[PLMReference]"]
            );
          }, this)))
        ) {
          (UWA.createElement("div", {
            class: "comment_label",
            text: k.LifecycleObjectList_versionComment,
          }).inject(s),
            (this.commentInput = new i({
              placeholder: C["Enter a text"],
              newLineMode: "enter",
            })),
            this.commentInput.addEventListener("change", function () {
              (t.preparedObjectsList.forEach(function (e) {
                (e.hasOwnProperty("modifiedAttributes") ||
                  (e.modifiedAttributes = {}),
                  e.hasOwnProperty("type.kindof[PLMReference]") &&
                    "TRUE" === e["type.kindof[PLMReference]"] &&
                    (e.modifiedAttributes["PLMReference.V_versionComment"] =
                      t.commentInput.value),
                  e.hasOwnProperty("branch") &&
                    e.branch &&
                    (e.modifiedAttributes.description = t.commentInput.value));
              }),
                t.commentInput
                  .getContent()
                  .setAttribute("title", t.commentInput.value));
            }),
            this.commentInput.addEventListener(
              "uncommittedChange",
              function () {
                t.commentInput
                  .getContent()
                  .setAttribute("title", t.commentInput.valueToCommit);
                var e = t._checkVersionCommentValue(
                  t.commentInput.valueToCommit,
                );
                e && t.showNotificationInDialog(e);
              },
            ),
            this.commentInput.getContent().addClassName("comment_input"),
            this.commentInput.inject(s));
        }
      } else if (
        (this._createSingleSelContentProp(s),
        Array.isArray(e) && e.length && (e[0].name || e[0].displayName))
      ) {
        o = e[0].name ? e[0].name : e[0].displayName;
        let t = e[0].revision ? " " + e[0].revision : "";
        ((a = " - " + o), o.endsWith(t) || (a += t));
      }
      let l = {
        UXName: "Revise",
        UXMode: "ObjectView",
        models: e,
        actionStatus: "success",
        actionTime: this.trackerEventBeginTime
          ? Date.now() - this.trackerEventBeginTime
          : 0,
      };
      (this.isMultipleSelection && (l.models = this.preparedObjectsList),
        this.isMultipleSelection && (l.UXMode = "ListView"),
        (this.trackerEvent = R.createInitActionEvent(l)),
        R.checkAndTrackPageEvent(this.trackerEvent));
      var d = this,
        u = new h(),
        p = u.create(s, {
          className: "ENO_Revise",
          closeButton: !0,
          title: r.newRevision + a,
          OKBtnText: r.ReviseOK,
          resizable: !0,
          imageUrl: e[0].imageUrl || "",
          onOk: function () {
            ((t.trackerEventBeginTime = Date.now()),
              d._executeRevise(function (e, i) {
                i
                  ? (u.destroy(),
                    d._onComplete(),
                    void 0 !== t.options &&
                      null !== t.options &&
                      t.options.hasOwnProperty("wintop") &&
                      1 == t.options.wintop &&
                      d._onReviseComplete(e),
                    d.dispatchEvent("onClose"))
                  : u.updateUI();
              }));
          },
          onClose: function () {
            (d._onComplete(),
              void 0 !== t.options &&
                null !== t.options &&
                t.options.hasOwnProperty("wintop") &&
                1 == t.options.wintop &&
                d._onCancel());
          },
          events: {
            onResizeFloating: function () {
              t.objectsList && t.objectsList.objectsTable.resizeTable();
            },
          },
        });
      (this.isMultipleSelection &&
        p.elements.container.addClassName("multiSel"),
        p.inject(widget.body));
      var v = p.getBody();
      (v || (v = widget.body),
        (this.errorOverlay = new O(this, v, {
          className: "revise_errorManager",
        })));
      var m = this.isMultipleSelection ? 600 : 530,
        f = this.isMultipleSelection && n ? 286 : 236,
        y = this.isMultipleSelection ? 400 : 330;
      ((f = this.isMultipleSelection ? f + 25 * (e.length - 2) : f),
        !this.isMultipleSelection && c.isTouchMode() && (f += 24));
      var g = f;
      (p.setNewSize({ width: m, height: f }),
        setTimeout(function () {
          p.setMinSize(y, g);
        }, 10));
    },
    _onComplete: function () {
      this.dispatchEvent("onComplete", {});
    },
    _onReviseComplete: function (e) {
      var t = [];
      if (
        null != e &&
        e.hasOwnProperty("results") &&
        e.results.hasOwnProperty("length") &&
        e.results.length > 0
      )
        for (var i = 0; i < e.results.length; i++)
          t.push({
            argname: "revised_physicalid" + i,
            argvalue: e.results[i].physicalid,
          });
      var r = b.createJSONArrayArg("NewVersionWebInWinResults", t);
      (r.length > 0 &&
        this.options.webToWinComSocket.dispatchEvent(
          "onDispatchToWin",
          { notif_name: "onCtxMenuClick", notif_parameters: r },
          "lf_web_in_win",
        ),
        this.options.webToWinComSocket.dispatchEvent(
          "onDispatchToWin",
          { notif_name: "ClosePanel", notif_parameters: "ClosePanel" },
          "lf_web_in_win",
        ));
    },
    _onCancel: function () {
      var e = b.createJSONArrayMessage("onDisplayErrorMessage", {
        severity: "warning",
        message: "Revise canceled",
      });
      e.length > 0 &&
        (this.options.webToWinComSocket.dispatchEvent(
          "onDispatchToWin",
          { notif_name: "onDisplayErrorMessage", notif_parameters: e },
          "lf_web_in_win",
        ),
        this.options.webToWinComSocket.dispatchEvent(
          "onDispatchToWin",
          { notif_name: "ClosePanel", notif_parameters: "ClosePanel" },
          "lf_web_in_win",
        ));
    },
    setObjects: function (e, t) {
      var i = this;
      this.commandManager.addEvent(
        "onCompleteGetOptions",
        function (r) {
          this.reviseOptions = r;
          for (var n = i.reviseOptions.objects, o = 0; o < n.length; ++o) {
            var a = n[o];
            a["type.property[IPML.NonLinearVersioningAvailability].value"] &&
              (this.useNLV = !0);
            var c = a.CADOrigin || "CATIAV5",
              l = "";
            ("CATIAV5" === c && (l = ":!"),
              i.IllegalCharMapping.addCADType(c, l));
          }
          if (i.reviseOptions.hasOwnProperty("options")) {
            var d = i.reviseOptions.options;
            for (o = 0; o < d.length; ++o) {
              var u = d[o];
              u.hasOwnProperty("newRevisionFromOldRevisionAllowed") &&
                (i.newRevisionFromOldRevisionAllowed =
                  u.newRevisionFromOldRevisionAllowed);
            }
          }
          i._verifyNewRevisionAllowed(e, t) &&
            this.IllegalCharMapping.getMappings().then(function (r) {
              ((i.illegalChars = r.find("CATIAV5") || ""),
                i.isMultipleSelection
                  ? (i._verifyMultipleSelection(e), i.objectsList.setObjects(e))
                  : s
                      .getSecurityContextPromise(i.tenant, i.context)
                      .then(function (r) {
                        i.useFIRST
                          ? i._getFirstVersionNumberProposal(e[0].physicalid, r)
                          : i._getRevisionDetails(e, t, r);
                      })
                      .catch(function (e) {
                        i.showError(e);
                      }));
            });
        },
        this,
      );
      try {
        var n = this;
        document.body.style.cursor = "wait";
        var o = {
          data: [{ physicalid: this.physicalIds[0] }],
          command: "revise",
        };
        this.commandManager.getOptions(
          o,
          function (t, i) {
            if (
              i &&
              i.report &&
              i.report[0] &&
              i.report[0].errorcode &&
              3 === i.report[0].errorcode
            ) {
              var s = e[0].typeDisplayName ? e[0].typeDisplayName : e[0].type,
                o = r.replace(r.get("noReviseOfAggType"), { dispType: s });
              n.isMultipleSelection && (o = r.noReviseOfAggMulti);
              var a = r.reviseParent;
              n.showError(o, { title: o, subtitle: a });
            } else n.showError(t);
            n._onComplete();
          },
          this.context,
        );
      } finally {
        document.body.style.cursor = "default";
      }
    },
    _createObjectsList: function () {
      ((this.objectListDiv = UWA.createElement("div", {
        class: "revise-objectList",
      })),
        (this.objectsList = new d({
          className: "objectlist-Revise",
          showDisplayNameColumn: !1,
          showNameColumn: !0,
          showRevisionColumn: !0,
        })),
        this.objectsList.inject(this.objectListDiv));
    },
    _verifyNewRevisionAllowed: function (e, t) {
      if (this.newRevisionFromOldRevisionAllowed || this.useFIRST) return !0;
      var i = [];
      if (
        (t &&
          Array.isArray(t) &&
          t.forEach(function (t) {
            if (Array.isArray(t.semantic) && !t.semantic.includes("E")) {
              var r = e.find((e) => e.physicalid == t.id);
              r && i.push(r);
            }
          }),
        0 == i.length)
      )
        return !0;
      var n = r.noReviseObjects,
        s = r.newRevisionFromAnyRevisionForbidden,
        o = "";
      return (
        i.forEach((e) => {
          var t = e.displayName ? e.displayName : e.name,
            i = e.revision ? " " + e.revision : "";
          ((o += t), e.revision && !t.endsWith(i) && (o += i), (o += "<br>"));
        }),
        this.showError(n, { title: n, subtitle: s, submessage: o }),
        this._onComplete(),
        !1
      );
    },
    _verifyMultipleSelection: function (e) {
      var t = this,
        i = function (e) {
          if (null === e || 0 === e.length)
            t.showNotification(r.objectNotFound);
          else {
            var i = e.filter(function (e, t, i) {
              return i.some(function (t) {
                return (
                  e.physicalid != t.physicalid && e.versionid == t.versionid
                );
              });
            });
            if (0 == i.length) t.dispatchEvent("onReady", {});
            else {
              i.sort(function (e, t) {
                return e.displayName < t.displayName
                  ? -1
                  : e.displayName > t.displayName
                    ? 1
                    : e.revision < t.revision
                      ? -1
                      : e.revision > t.revision
                        ? 1
                        : 0;
              });
              var n = r.multObjsSameFamily + "<br>";
              (i.forEach(function (e, t, i) {
                var r = e.displayName ? e.displayName : e.name,
                  s = e.revision ? " " + e.revision : "";
                ((n += r),
                  e.revision && !r.endsWith(s) && (n += s),
                  t < i.length - 1 && (n += "<br>"));
              }),
                t.showError(n),
                t._onComplete());
            }
          }
        };
      s.getSecurityContextPromise(t.tenant, t.context)
        .then(function (r) {
          t._attributeListRequest(e, r, i);
        })
        .catch(function (e) {
          t.showError(e);
        });
    },
    _attributeListRequest: function (e, t, i) {
      document.body.style.cursor = "wait";
      var n = this,
        a = {
          data: e,
          attributes: [
            "baseType",
            "logicalid",
            "versionid",
            "type.kindof[PLMReference]",
            "displayType",
          ],
        },
        c = o.get3DSpaceWSUrl(
          this.tenant,
          "/resources/lifecycle/product/attributeList",
          null,
        );
      l.authenticatedRequest(c, {
        method: "POST",
        type: "json",
        headers: o.getHeaders(t),
        timeout: 6e5,
        data: JSON.stringify(a),
        onComplete: function (e) {
          document.body.style.cursor = "default";
          var t = e.results;
          ((n.preparedObjectsList = t), i(t));
        },
        onFailure: function (t, i) {
          ((document.body.style.cursor = "default"),
            console.log("verifyMultipleSelection:Failure..." + t));
          let r = n.trackerEventBeginTime
              ? Date.now() - n.trackerEventBeginTime
              : 0,
            o = {
              UXName: "Revise",
              models: e,
              actionStatus: "failure",
              actionTime: r,
            };
          ((n.trackerEvent = R.createInitActionEvent(o)),
            R.checkAndTrackPageEvent(n.trackerEvent),
            n.showError(s.parseWebServiceError(t, i)),
            n._onComplete());
        },
        onTimeout: function () {
          ((document.body.style.cursor = "default"),
            console.log(
              "verifyMultipleSelection: A connection timeout occured.",
            ));
          let t = n.trackerEventBeginTime
              ? Date.now() - n.trackerEventBeginTime
              : 0,
            i = {
              UXName: "Revise",
              models: e,
              actionStatus: "failure",
              actionTime: t,
            };
          ((n.trackerEvent = R.createInitActionEvent(i)),
            R.checkAndTrackPageEvent(n.trackerEvent),
            n.showError(r.timeout),
            n._onComplete());
        },
      });
    },
    _strContainsIllegalChars: function (e, t) {
      return !!(e && g.commonChar(e, t, !0) > 0);
    },
    _strIsLegal: function (e) {
      return !this._strContainsIllegalChars(e, this.illegalChars);
    },
    _verifyTitles: function (e) {
      return (
        !e.some(function (e) {
          return !this._strIsLegal(e.title);
        }, this) ||
        (this.showError(
          r.replace(r.illegalTitle, { illegalChars: this.illegalChars }),
        ),
        !1)
      );
    },
    _checkVersionCommentValue: function (e) {
      if (e && e.length > 256) {
        var t = require("i18n!DS/EditPropWidget/assets/nls/langEditProp"),
          i = "checkVersionCommentValue error";
        return (
          t &&
            (i = t.replace(t.get("MaxLengthAttribute"), {
              label: k.LifecycleObjectList_versionComment,
              value: 256,
            })),
          { code: "ERROR", eventID: "warning", message: i }
        );
      }
      return null;
    },
    _executeRevise: function (e) {
      var t = this,
        i = { data: [] };
      if (
        ((this.foreIntent = !(
          !this.foreIntentCheckbox || !0 !== this.foreIntentCheckbox.checkFlag
        )),
        this.physicalIds.forEach(function (e) {
          var r = { physicalid: e };
          (t._newTitles.contains(e) && (r.title = t._newTitles.find(e)),
            t.preparedObjectsList &&
              t.preparedObjectsList.forEach(function (t) {
                (t.hasOwnProperty("physicalid") &&
                  t.hasOwnProperty("modifiedAttributes") &&
                  e == t.physicalid &&
                  (r.modifiedAttributes = t.modifiedAttributes),
                  t.hasOwnProperty("proposedRevision") &&
                    t.hasOwnProperty("physicalid") &&
                    e == t.physicalid &&
                    ((r.proposedRevision = t.proposedRevision),
                    r.modifiedAttributes || (r.modifiedAttributes = {}),
                    r.modifiedAttributes.revision ||
                      (r.modifiedAttributes.revision = t.proposedRevision),
                    console.log(
                      e + " : item.proposedRevision = " + r.proposedRevision,
                    )),
                  t.hasOwnProperty("ancestors") &&
                    t.hasOwnProperty("physicalid") &&
                    e == t.physicalid &&
                    (r.ancestors = t.ancestors));
              }),
            i.data.push(r));
        }),
        null != this.objectProp) &&
        null != (r = this.objectProp.getErrors()) &&
        r.length > 0
      )
        return (
          r.forEach((e) => {
            this.showNotificationInDialog(e);
          }),
          void e(null, !1)
        );
      if (t.preparedObjectsList) {
        var r = [];
        if (
          (t.preparedObjectsList.forEach(function (e) {
            if (
              e.modifiedAttributes &&
              e.modifiedAttributes["PLMReference.V_versionComment"]
            ) {
              var i = t._checkVersionCommentValue(
                e.modifiedAttributes["PLMReference.V_versionComment"],
              );
              i && r.push(i);
            }
          }),
          r.length > 0)
        )
          return (t.showNotificationInDialog(r[0]), void e(null, !1));
      }
      if (this._verifyTitles(i.data)) {
        JSON.stringify(i);
        i.folderid = this.folderId;
        var n = function (t) {
          ((document.body.style.cursor = "default"), e(t, !0));
        };
        t = this;
        s.getSecurityContextPromise(t.tenant, t.context)
          .then(function (e) {
            t.reviseServiceRequest(i, n, e);
          })
          .catch(function (e) {
            t.showError(e);
          });
      } else e(null, !1);
    },
    showNotificationInDialog: function (e) {
      "ERROR" === e.code
        ? null != this.errorOverlay
          ? this.errorOverlay.addError(e.message, e.customClass)
          : this.showError(e.message)
        : null != this.errorOverlay
          ? this.errorOverlay.addWarning(e.message, e.customClass)
          : this.showNotification(e.message);
    },
    showNotification: function (e, t) {
      if (
        ((t = t || {}), "function" == typeof this.context.displayNotification)
      ) {
        var i = {
          eventID: "string" == typeof t.type ? t.type : "primary",
          msg: e,
          sticky: t.sticky || !1,
        };
        this.context.displayNotification(i);
      } else alert(e);
    },
    showError: function (e, t) {
      if (
        ((t = t || {}), "function" == typeof this.context.displayNotification)
      ) {
        var i = {
          eventID: "error",
          msg: t.title || t.subtitle ? t.submessage : e,
          title: t.title,
          subtitle: t.subtitle,
          sticky: t.sticky || !1,
        };
        this.context.displayNotification(i);
      } else alert(e);
    },
    _displayNotification: function (e) {
      a.displayNotification(e);
    },
    _showReviseFailure: function (e) {
      var t = r.reviseFailed,
        i = null;
      if (0 !== e.report.length) {
        var n = e.report[0];
        if (
          ((t = (t = n.error).replace(/(\n)+/g, "<br>")),
          console.log(n.description),
          (i = { title: r.reviseFailed, submessage: t }),
          n.ENOVersioningErrorKey &&
            ("NonLinearVersioning.Error.NoReviseAccess" ===
              n.ENOVersioningErrorKey ||
              "NonLinearVersioning.Error.AccessRights" ===
                n.ENOVersioningErrorKey))
        ) {
          var s = r.noReviseObjects,
            o = this.preparedObjectsList[0]
              ? this.preparedObjectsList[0].displayName
              : null;
          if (!this.isMultipleSelection && o) {
            s = r.replace(r.noReviseOfName, { dispName: o });
            var a = r.verifyCredentials,
              c = r.lackingReviseAccessRole;
            ("NonLinearVersioning.Error.AccessRights" ===
              n.ENOVersioningErrorKey &&
              ((a = r.replace(r.get("noPermissionToLatestRevision"), {
                dispName: o,
              })),
              (c = r.createNewBranch)),
              (i = { title: s, subtitle: a, submessage: c }));
          }
        }
      }
      this.showError(t, i);
    },
    reviseServiceRequest: function (e, t, i) {
      var n = this;
      console.log("REVISE:");
      if (
        ((e.notificationTimeout = 600),
        (this.trackerNumberObjects = e.data ? e.data.length : 0),
        !this.trackerEvent)
      ) {
        let e = { UXName: "Revise", numberObjects: this.trackerNumberObjects };
        this.trackerEvent = R.createInitActionEvent(e);
      }
      var a = { UXName: "Revise", isWebInWin: !1 };
      (n.options && n.options.wintop && (a.isWebInWin = n.options.wintop),
        n.options &&
          n.options.client_app_domain &&
          (a.client_app_domain = n.options.client_app_domain),
        n.options &&
          n.options.client_app_name &&
          (a.client_app_name = n.options.client_app_name),
        _.enrichRequestWithMetricsWebInWin(e, a));
      var c = JSON.stringify(e),
        d = o.get3DSpaceWSUrl(
          this.tenant,
          "/resources/lifecycle/revise/major",
          null,
        );
      if (n.useNLV || n.useFIRST) {
        d = o.get3DSpaceWSUrl(
          this.tenant,
          "/resources/v1/dslc/addversions",
          null,
        );
        var u = "E";
        (n.newRevisionFromOldRevisionAllowed && (u = "LAST"),
          n.useFIRST && (u = "FIRST"));
        for (var h = [], p = 0, m = e.data.length; p < m; p++) {
          (S = []).push({ semantic: u, id: e.data[p].physicalid });
          var f = [];
          e.data[p].title &&
            f.push({ name: "PLMEntity.V_Name", value: e.data[p].title });
          var y = null;
          if (e.data[p].modifiedAttributes) {
            var g = e.data[p].modifiedAttributes,
              b = null;
            for (b in g)
              "revision" === b
                ? (y = g.revision)
                : f.push({ name: b, value: g[b] });
          }
          var E = { copyId: e.data[p].physicalid, ancestors: S, attributes: f };
          (y && (E.code = y), h.push(E));
        }
        c = JSON.stringify({ addRequests: h });
      }
      if (n.foreIntent) {
        d = o.get3DSpaceWSUrl(
          this.tenant,
          "/resources/v1/dslc/addversions",
          null,
        );
        u = "FORE";
        var w = [],
          S = [],
          O = null;
        f = [];
        if (e.data[0].modifiedAttributes) {
          ((g = e.data[0].modifiedAttributes), (b = null));
          for (b in g)
            "revision" === b
              ? (O = g.revision)
              : f.push({ name: b, value: g[b] });
        }
        (S.push({ semantic: u, id: e.data[0].physicalid }),
          ((E = {
            copyId: e.data[0].ancestors[0].id,
            ancestors: S,
            attributes: f,
          }).code = O || e.data[0].proposedRevision),
          w.push(E),
          (c = JSON.stringify({ addRequests: w })));
      }
      ((document.body.style.cursor = "wait"),
        l.authenticatedRequest(d, {
          method: "POST",
          headers: o.getHeaders(i),
          timeout: 6e5,
          onComplete: function (i) {
            void 0 !== n.options &&
            null !== n.options &&
            n.options.hasOwnProperty("wintop") &&
            1 == n.options.wintop
              ? t(i)
              : (n.useFIRST &&
                  null != i &&
                  i.hasOwnProperty("addRequests") &&
                  (n.firstConfigRevisionObject = {
                    id: i.addRequests[0].id,
                    revision: i.addRequests[0].code,
                  }),
                t());
            var s = { created: [], deleted: [], modified: [] };
            ((s.context = n.context), (s.refreshFolder = !!e.folderid));
            var o = e.data,
              a = n.selUtil.getAppSelectionRootPids(n.context),
              c = a.length > 0;
            if (
              null != i &&
              i.hasOwnProperty("status") &&
              i.hasOwnProperty("report")
            ) {
              if ("failure" === i.status) {
                let e = n.trackerEventBeginTime
                    ? Date.now() - n.trackerEventBeginTime
                    : 0,
                  t = {
                    numberObjects: n.trackerNumberObjects,
                    actionStatus: "failure",
                    actionTime: e,
                  };
                return (
                  (n.trackerEvent = R.createValidationEventFromEvent(
                    t,
                    n.trackerEvent,
                  )),
                  R.checkAndTrackPageEvent(n.trackerEvent),
                  void n._showReviseFailure(i)
                );
              }
              var l = i.results;
              (n.commandManager
                .findObjectDerivedFromSelection(l, o)
                .forEach(function (e) {
                  var t = !1;
                  e.hasOwnProperty("derivedfromphysicalid") && (t = !0);
                  var i =
                      e["attribute[PLMReference.V_DerivedFrom]"] ||
                      e.derivedfromphysicalid,
                    r = {
                      physicalid: e.physicalid,
                      sourceObjectId: i,
                      operation: "Revise",
                      refreshPSD: t,
                    };
                  if (
                    (e.hasOwnProperty("folders") &&
                      ((s.refreshFolder = !0), (r.folders = e.folders)),
                    !c || (c && -1 !== a.indexOf(r.sourceObjectId)))
                  )
                    s.created.push(r);
                  else if (c) {
                    var n = { physicalid: i, operation: "Revise" };
                    s.modified.push(n);
                  }
                }),
                l
                  .filter(function (e) {
                    var t =
                      e.derivedfromphysicalid ||
                      e["attribute[PLMReference.V_DerivedFrom]"];
                    return !!t && !o.find((e) => e.physicalid === t);
                  })
                  .forEach(function (e) {
                    var t = {
                      physicalid: e.derivedfromphysicalid
                        ? e.derivedfromphysicalid
                        : e["attribute[PLMReference.V_DerivedFrom]"],
                      operation: "Revise",
                    };
                    s.modified.push(t);
                  }));
            }
            if (
              (n.useNLV || n.useFIRST) &&
              null != i &&
              i.hasOwnProperty("addRequests")
            ) {
              if (i.errorID) {
                let e = n.trackerEventBeginTime
                    ? Date.now() - n.trackerEventBeginTime
                    : 0,
                  t = {
                    numberObjects: n.trackerNumberObjects,
                    actionStatus: "failure",
                    actionTime: e,
                  };
                return (
                  (n.trackerEvent = R.createValidationEventFromEvent(
                    t,
                    n.trackerEvent,
                  )),
                  R.checkAndTrackPageEvent(n.trackerEvent),
                  i.errorMessage
                    ? n.showError(i.errorMessage)
                    : n.showError(r.reviseFailed),
                  void console.log(
                    "Addversions service returned error (key):" + i.key,
                  )
                );
              }
              n.useNLV &&
                i.addRequests.forEach(function (e) {
                  var t = {
                    physicalid: e.id,
                    sourceObjectId: e.copyId,
                    operation: "Revise",
                    refreshPSD: !0,
                  };
                  s.created.push(t);
                });
            }
            if (n.foreIntent && null != i && i.hasOwnProperty("addRequests")) {
              if (i.errorID)
                return (
                  i.errorMessage
                    ? n.showError(i.errorMessage)
                    : n.showError(r.reviseFailed),
                  void console.log(
                    "Addversions service returned error (key):" + i.key,
                  )
                );
              i.addRequests.forEach(function (e) {
                var t = {
                  physicalid: e.id,
                  sourceObjectId: e.copyId,
                  operation: "Revise",
                  refreshPSD: !0,
                  forceLoad: !0,
                };
                s.created.push(t);
              });
            }
            (n.showNotification(r.reviseSuccessful, { type: "success" }),
              v.dispatchEvent("onModification", s));
            let d = n.trackerEventBeginTime
                ? Date.now() - n.trackerEventBeginTime
                : 0,
              u = {
                numberObjects: n.trackerNumberObjects,
                actionStatus: "success",
                actionTime: d,
              };
            (Array.isArray(i.results) && (u.pv16 = i.results.length),
              (n.trackerEvent = R.createValidationEventFromEvent(
                u,
                n.trackerEvent,
              )),
              R.checkAndTrackPageEvent(n.trackerEvent),
              "function" == typeof n.callback && n.callback(returnedData));
          },
          data: c,
          onTimeout: function (e) {
            t();
            let i = n.trackerEventBeginTime
                ? Date.now() - n.trackerEventBeginTime
                : 0,
              r = {
                numberObjects: n.trackerNumberObjects,
                actionStatus: "running_on_backend",
                actionTime: i,
              };
            ((n.trackerEvent = R.createValidationEventFromEvent(
              r,
              n.trackerEvent,
            )),
              R.checkAndTrackPageEvent(n.trackerEvent),
              n.showNotification(k.reviseTimeoutInfo, { sticky: !0 }),
              console.log("Timeout error"));
          },
          onFailure: function (e, i) {
            (t(),
              s.showWebServiceError(
                e,
                i,
                function (e, t, i, r, s) {
                  if (t) {
                    n.showNotification(k.reviseTimeoutInfo, { sticky: !0 });
                    let e = n.trackerEventBeginTime
                        ? Date.now() - n.trackerEventBeginTime
                        : 0,
                      t = {
                        numberObjects: n.trackerNumberObjects,
                        actionStatus: "running_on_backend",
                        actionTime: e,
                      };
                    ((n.trackerEvent = R.createValidationEventFromEvent(
                      t,
                      n.trackerEvent,
                    )),
                      R.checkAndTrackPageEvent(n.trackerEvent));
                  } else {
                    n.showError(e, { title: i, subtitle: r, submessage: s });
                    let t = n.trackerEventBeginTime
                        ? Date.now() - n.trackerEventBeginTime
                        : 0,
                      o = {
                        numberObjects: n.trackerNumberObjects,
                        actionStatus: "failure",
                        actionTime: t,
                      };
                    ((n.trackerEvent = R.createValidationEventFromEvent(
                      o,
                      n.trackerEvent,
                    )),
                      R.checkAndTrackPageEvent(n.trackerEvent));
                  }
                },
                !0,
              ),
              console.log("Failed to retrive data from the server"));
          },
          type: "json",
        }));
    },
  });
});
