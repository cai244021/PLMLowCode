define(
    "DS/ENONewWidget/ENONew/Controller/ENONewController",
    [
        "UWA/Controls/Abstract",
        "DS/ENONewWidget/ENONew/Controller/ENONewDialogController",
        "DS/ENONewWidget/ENONew/Events",
        "DS/Controls/Button",
        "DS/ENONewWidget/ajaxRequests/ENONewServices",
        "DS/ENONewWidget/utils/Notification",
        "DS/ENOCollabToolsUI/tracker/ENOCollabTracker",
        "i18n!DS/ENONewWidget/assets/nls/ENONewWidgetNls",
    ],
    function (e, t, n, i, o, a, r, s) {
        return e.extend({
            skeleton: null,
            defaultAttributes: {},
            init: function (e) {
                console.log("ENONew Controller init");
                (this.id = e.id),
                    (this.context = e.context),
                    (this.labelTopBar = e.labelTopBar),
                    (this.commandName = e.commandName),
                    (this.overlay = e.overlay),
                    (this.responseFormat = e.responseFormat),
                    (this.facets = e.facets),
                    (this.viewType = e.viewType),
                    (this.activatelock = e.activatelock),
                    (this.activateDraftMode = e.activateDraftMode),
                    (this.activateCreateAnother = e.activateCreateAnother),
                    this._parent(e),
                    (this.objInfo = {
                        type: e.type,
                        options: e.options,
                        events: e.events,
                    }),
                    (this.silentMode = e.silentMode),
                    (this.notification = e.notification),
                    (this.Services = new o(e.context, { parsedResult: e.parsed })),
                    (this.buttons = {});
                var t = this.objInfo.events;
                for (eventID in t) {
                    var n = t[eventID];
                    this.addEvent(eventID, n);
                }
                this.silentMode ? this.silentCreation() : this._setup();
            },
            getUI: function (e) {
                var n = this;
                return new UWA.Promise(function (e) {
                    n.dialog || (n.dialog = new t()), e(n.dialog);
                });
            },
            _setup: function () {
                var e = this,
                    t = e.getUI(),
                    n = {
                        type: this.objInfo.type,
                        preferedType: this.objInfo.options.preferedType,
                        subTypes: !0,
                    },
                    i = e._getTypeInfo(n),
                    o = this.Services.getSecurityContextList();
                UWA.Promise.all([o, i, t])
                    .then(function (t) {
                        console.log("ENONew PromiseAll SecurityContext, typeInfo"),
                            e.postFetchAttributes(t[1], "launch"),
                            (e.defaultAttributes.SecurityContext = t[0]),
                            (e.defaultAttributes.Type = t[1].Type),
                            (e.attributes = t[1].attributes),
                            (e.interfaces = t[1].interfaces),
                        t[1].reInitOnSecCtxChange &&
                        e.defaultAttributes.SecurityContext &&
                        (e.defaultAttributes.SecurityContext.reInitOnSecCtxChange = !0),
                        t[1].prefs && (e.prefs = t[1].prefs),
                            e.displayDialog();
                        var n = {
                            UXName: "New",
                            UXSub: "Generic",
                            numberObjects: 1,
                            actionStatus: r.success,
                            pd16: e.options.options.typeName,
                        };
                        (e.lastTrackerEvent = r.createInitActionEvent(n)),
                            r.checkAndTrackPageEvent(e.lastTrackerEvent);
                    })
                    .catch(function (t) {
                        e.handleError(t), e.destroyComponent();
                        var n = {
                            UXName: "New",
                            UXSub: "Generic",
                            numberObjects: 1,
                            actionStatus: r.failure,
                            pd16: e.options.options.typeName,
                        };
                        (e.lastTrackerEvent = r.createInitActionEvent(n)),
                            r.checkAndTrackPageEvent(e.lastTrackerEvent);
                    });
            },
            silentCreation: function () {
                var e = this,
                    t = { type: this.objInfo.type, subTypes: !1 },
                    n = e._getTypeInfo(t, !0),
                    i = this.Services.getPreferedSecurityContext();
                UWA.Promise.all([i, n])
                    .then(function (t) {
                        var n = t[0],
                            i = t[1],
                            o = { eventID: "createClose", securityContext: n };
                        e._create(i, o);
                    })
                    .catch(function (t) {
                        e.handleError(t), e.destroyComponent();
                    });
            },
            displayDialog: function () {
                var e = this,
                    t = {
                        defaultAttributes: this.defaultAttributes,
                        attributes: this.attributes,
                        interfaces: this.interfaces,
                        prefs: this.prefs,
                    },
                    n = {
                        overlay: this.overlay,
                        activatelock: this.activatelock,
                        activateDraftMode: this.activateDraftMode,
                        activateCreateAnother: this.activateCreateAnother,
                        title: this.labelTopBar
                            ? this.labelTopBar
                            : s.get("new") + " " + e.getCurrentTypeNLS(),
                        persistId: "ENONew_" + (this.id ? this.id : this.buildID()),
                        facets: this.facets,
                        viewType: this.viewType,
                    };
                this.defaultAttributes &&
                this.defaultAttributes.SecurityContext &&
                (n.securityContext = this.defaultAttributes.SecurityContext.value);
                var i = {
                    onChangeAttribute: function (t) {
                        console.log("ENONew Controller onchnage"), e._onChangeAttribute(t);
                    },
                    onClose: function () {
                        e.destroyComponent();
                    },
                };
                this.dialog.setup(t, n, this.buttons, i, function (t, n, i) {
                    e.onSubmit(t, n, i);
                });
            },
            refreshDialog: function () {
                var e = {
                    defaultAttributes: this.defaultAttributes,
                    attributes: this.attributes,
                    interfaces: this.interfaces,
                };
                this.dialog.reloadContent(e), this.dialog.resetButtons();
            },
            reloadDialog: function (e) {
                var t = this,
                    n = { type: e };
                this._getTypeInfo(n)
                    .then(function (e) {
                        (t.attributes = e.attributes),
                            (t.interfaces = e.interfaces),
                            t.refreshDialog();
                    })
                    .catch(function (e) {
                        t.handleError(e), t.destroyComponent();
                    });
            },
            onSubmit: function (e, t, n) {
                this.defaultAttributes = e.defaultAttributes;
                var i,
                    o,
                    a = e.defaultAttributes;
                for (var r in a) {
                    var s = a[r],
                        c = s.name;
                    if ("Type" === c) {
                        i = s.value;
                        var l = s.persistData;
                        for (var d in l) {
                            var u = l[d];
                            u.name === i && (e.subTypes = [u]);
                        }
                    }else "SecurityContext" === c && (o = s.value);
                }
                (t.securityContext = o),
                    (e.type = i),
                    delete e.defaultAttributes,
                    this._create(e, t, n);
            },
            _getTypeInfo: function (e, t) {
                var n = e.type ? e.type : this.objInfo.type,
                    i = e.preferedType
                        ? e.preferedType
                        : this.objInfo.options.preferedType,
                    o = !!e.subTypes && e.subTypes,
                    a = e.securityContext ? e.securityContext : void 0,
                    r = {
                        type: n,
                        preferedType: i,
                        typeName: this.objInfo.options.typeName,
                        parentOrigin: this.objInfo.options.parentOrigin,
                        domainAttributes: this.objInfo.options.domainAttributes,
                        subTypes: o,
                        activatelock:
                            !(!this.activatelock || !this.activatelock.show) || void 0,
                        activateDraftMode:
                            !(!this.activateDraftMode || !this.activateDraftMode.show) ||
                            void 0,
                        initAttributes: this.objInfo.options.attributes
                            ? this.objInfo.options.attributes
                            : void 0,
                    };
                return (
                    (this.objInfo.options.preferedType = i || n),
                        this.Services.getTypeInfo1(r, a, t)
                );
            },
            _onChangeAttribute: function (e) {
                var t = this;
                console.log("args:", e);
                if (e)
                    for (var n in (console.log(e), e)) {
                        if ("New-Type" === n) {
                            var i = e[n].valueDB,
                                o = { preferedType: i };
                            t._getTypeInfo(o)
                                .then(function (e) {
                                    (t.defaultAttributes.Type.value = i),
                                        (t.attributes = e.attributes),
                                        (t.interfaces = e.interfaces),
                                        t.postFetchAttributes(e, "onChangeType"),
                                        t.refreshDialog();
                                })
                                .catch(function (e) {
                                    t.dispatchEvent("onFailure", [e]), t.destroyComponent();
                                });
                        }
                        if ("V_Name" === n) {
                            console.log("V_Name", n);
                            var i = e[n].valueDB;
                            for (var attr = 0; attr < this.attributes.publicAttributes.length; attr++) {
                                if (this.attributes.publicAttributes[attr].name === "V_Name") {
                                    this.attributes.publicAttributes[attr].value = i;
                                    this.attributes.publicAttributes[attr].ismode = true;
                                    break;
                                }
                            }
                            console.log("@@@@@@@@@@@@@@@@@@@@this:", this);
                        }

                        //add by ljr 根据零类型  展示其子类型
                        if ("JF_VPMReference-JF_PartType" === n) {
                            console.log("@@@@@@@@@@@@@@@@@@@@this:", this);
                            var i = e[n].valueDB,
                                o = { JF_PartType: i };

                                var V_NameValue = "attr";
                                console.log("this.attributes.publicAttributes:", this.attributes.publicAttributes);
                                console.log("JF_VPMReference-JF_PartType:", i);
                                for (var attr = 0; attr < this.attributes.publicAttributes.length; attr++) {
                                    if (this.attributes.publicAttributes[attr].name === "V_Name" && this.attributes.publicAttributes[attr].ismode === true) {
                                        V_NameValue = this.attributes.publicAttributes[attr].value;
                                        break;
                                    }
                                }
                                t._getTypeInfo(o)
                                    .then(function (e) {
                                        console.log("args2:", e);
                                        var nn = 0;
                                        if (V_NameValue !== "attr") {
                                            for (var attrIndex = 0; attrIndex < e.attributes.publicAttributes.length; attrIndex++) {
                                                if (e.attributes.publicAttributes[attrIndex].name === "V_Name") {
                                                    e.attributes.publicAttributes[attrIndex].value = V_NameValue;
                                                    e.attributes.publicAttributes[attrIndex].ismode = true;
                                                }
                                            }
                                        }
                                        for (var zz = 0; zz < e.attributes.extensionAttributes.length; zz++) {
                                            if (e.attributes.extensionAttributes[zz].name === 'JF_PartType') {
                                                e.attributes.extensionAttributes[zz].value = i;
                                            }
                                            if (e.attributes.extensionAttributes[zz].name === 'JF_PartSubType') {
                                                if ("T" === i || "E" == i) {
                                                    e.attributes.extensionAttributes[zz]['mandatory'] = true;
                                                    nn = zz;
                                                    for (var ui = 0; ui < e.attributes.extensionAttributes[zz].range.length; ui++) {
                                                        var range = e.attributes.extensionAttributes[zz].range[ui];
                                                        if (!range.includes(i) && range !== '') {
                                                            e.attributes.extensionAttributes[zz].range.splice(ui, 1);
                                                            e.attributes.extensionAttributes[zz].rangeNLS.splice(ui, 1);
                                                            ui--;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if ("T" === i || "E" === i) {
                                            var partType = e.attributes.extensionAttributes[1];
                                            e.attributes.extensionAttributes[1] = e.attributes.extensionAttributes[nn];
                                            e.attributes.extensionAttributes[nn] = partType;
                                        }
                                        (t.attributes = e.attributes),
                                            (t.interfaces = e.interfaces),
                                            t.postFetchAttributes(e, "onChangeJF_PartType"),
                                            t.refreshDialog();
                                    })
                                    .catch(function (e) {
                                        t.dispatchEvent("onFailure", [e]), t.destroyComponent();
                                    });

                        }
                        //判断子类型是否为面料件
                        if ("JF_VPMReference-JF_PartSubType" === n) {
                            console.log("@@@@@@@@@@@@@@@@@@@@this:", this);
                            var i = e[n].valueDB,
                                o = { JF_PartSubType: i };

                                var V_NameValue = "attr";
                                var strPartType = "";
                                console.log("this.attributes.publicAttributes:", this.attributes.publicAttributes);
                                for (var attr = 0; attr < this.attributes.publicAttributes.length; attr++) {
                                    if (this.attributes.publicAttributes[attr].name === "V_Name" && this.attributes.publicAttributes[attr].ismode === true) {
                                        V_NameValue = this.attributes.publicAttributes[attr].value;
                                        break;
                                    }
                                }
                                console.log("this.attributes.extensionAttributes:", this.attributes.extensionAttributes);
                                for (var attr = 0; attr < this.attributes.extensionAttributes.length; attr++) {
                                    if (this.attributes.extensionAttributes[attr].name === "JF_PartType") {
                                        strPartType = this.attributes.extensionAttributes[attr].value;
                                        break;
                                    }
                                }
                                console.log("V_NameValue:", V_NameValue);
                                console.log("strPartType:", strPartType);
                                console.log("i:", i);
                                t._getTypeInfo(o)
                                    .then(function (e) {
                                        if (V_NameValue !== "attr") {
                                            for (var attrIndex = 0; attrIndex < e.attributes.publicAttributes.length; attrIndex++) {
                                                if (e.attributes.publicAttributes[attrIndex].name === "V_Name") {
                                                    e.attributes.publicAttributes[attrIndex].value = V_NameValue;
                                                    e.attributes.publicAttributes[attrIndex].ismode = true;
                                                }
                                            }
                                        }
                                        for (var zz = 0; zz < e.attributes.extensionAttributes.length; zz++) {
                                            if (e.attributes.extensionAttributes[zz].name === 'JF_PartSubType') {
                                                e.attributes.extensionAttributes[zz]['mandatory'] = true;
                                                //modified必须为true,
                                                e.attributes.extensionAttributes[zz]['modified'] = true;
                                                e.attributes.extensionAttributes[zz].value = i;
                                                for (var ui = 0; ui < e.attributes.extensionAttributes[zz].range.length; ui++) {
                                                    var range = e.attributes.extensionAttributes[zz].range[ui];
                                                    if (!range.includes(strPartType) && range !== '') {
                                                        e.attributes.extensionAttributes[zz].range.splice(ui, 1);
                                                        e.attributes.extensionAttributes[zz].rangeNLS.splice(ui, 1);
                                                        ui--;
                                                    }
                                                }
                                            }
                                            if (e.attributes.extensionAttributes[zz].name === 'JF_Fabric') {
                                                if ("T03" === i) {
                                                    e.attributes.extensionAttributes[zz]['mandatory'] = true;
                                                }
                                            }
                                            if (e.attributes.extensionAttributes[zz].name === 'JF_PartType') {
                                                e.attributes.extensionAttributes[zz]['mandatory'] = true;
                                                e.attributes.extensionAttributes[zz].value = strPartType;
                                            }
                                        }
                                        (t.attributes = e.attributes),
                                            (t.interfaces = e.interfaces),
                                            t.postFetchAttributes(e, "onChangeJF_PartSubType"),
                                            t.refreshDialog();
                                    })
                                    .catch(function (e) {
                                        t.dispatchEvent("onFailure", [e]), t.destroyComponent();
                                    });
                        }
                        //end
                        if ("New-SecurityContext" === n) {
                            if (t.defaultAttributes.SecurityContext.reInitOnSecCtxChange) {
                                var a = e[n].valueDB;
                                t._getTypeInfo({ securityContext: a })
                                    .then(function (e) {
                                        (t.defaultAttributes.SecurityContext.value = a),
                                            (t.attributes = e.attributes),
                                            (t.interfaces = e.interfaces),
                                            t.postFetchAttributes(e, "onChangeSecurityContext"),
                                            t.refreshDialog();
                                    })
                                    .catch(function (e) {
                                        t.dispatchEvent("onFailure", [e]), t.destroyComponent();
                                    });
                            }
                        } else if ("3DPartTemplate" === n) {
                            var r = e[n],
                                s = r.valueDB,
                                c = r.authorizedValues.includes(s);
                            t.PartTemplate3D = c ? s : "";
                        }
                    }
            },
            lockUnlockCreatedObject: function (e, t, n) {
                return UWA.Promise((e) => {
                    e();
                });
            },
                /**
                 * add by chenyan 新增校验分类不能为空
                 */
            checkClassIsNotNull(){
                var res = true;
                try {
                    var classFacet = this.dialog.View._getFacetFromName("classificationFacet");
                    //获取选中的分类id 备注 直接获取对象上面数组拿不到需要转换
                    const selectClass = classFacet.content.initUI.plmNewLibraryActive.autoComplete.selectedItems;
                    const selectClassIdArr = selectClass.map(function (e) {
                        return e.id
                    })
                    console.log("selectClassIdArr:{}",selectClassIdArr);
                    // 提示库信息为必填
                    if ((!selectClassIdArr) || selectClassIdArr.length  < 1){
                        this.dialog._addNotif({msg :s.get("CLASS_IS_NOT_NULL"), eventID: "warning"});
                        res = false;
                    }
                }catch (e){
                    this.dialog._addNotif({msg :s.get("CLASS_GETID_FAIL"), eventID: "error"});
                    console.log(e);
                }
                return res;
            },
            _create: function (e, t, n) {
                // add by chenyan 校验分类库不能为空
                let isXENAPP = false ;
                if (widget){
                    const  appID = widget.data.appId;
                    console.log("appID:",appID)
                    if ("ENXENG_AP" == appID){
                        isXENAPP = true ;
                    }
                }
                if (isXENAPP){
                    const  checkRes = this.checkClassIsNotNull();
                    if (!checkRes){
                        this.dialog.resetButtons();
                        return ;
                    }
                }
                console.log("this：",this);
                var i = this;
                if (e) {
                    t = t || {};
                    var o = {};
                    (o.typeName = this.objInfo.options.typeName),
                        (o.aggregating_context = this.objInfo.options.parentPIDs),
                        (o.select = this.objInfo.options.select),
                        (o.folderID = this.objInfo.options.folderID),
                    this.objInfo.options.selectedIDs &&
                    (o.selectedIDs = this.objInfo.options.selectedIDs),
                        (o.activeFolder = this.objInfo.options.activeFolder);
                    var a = { queryParams: o, securityContext: t.securityContext };
                    i.activateDraftMode &&
                    i.activateDraftMode.show &&
                    n &&
                    n.footerCreateBtnState &&
                    ((e.prefs = {
                        createOption: n.footerCreateBtnState,
                        lockOption: n.toolbarIsLockEnabled,
                    }),
                        (i.activateDraftMode.preference = n.footerCreateBtnState)),
                        i.Services.createContent(
                            e,
                            a,
                            function (o, a) {
                                setTimeout(function () {
                                    var s = i.lockUnlockCreatedObject(n, o, t);
                                    UWA.Promise.all([s])
                                        .then((n) => {
                                            i.onSuccessOfCreation(o, a, function () {
                                                "create" === t.eventID
                                                    ? i.reloadDialog(e.type)
                                                    : "createClose" === t.eventID && i.destroyComponent();
                                                var n = {
                                                    typeObjects: e.type,
                                                    numberObjects: 1,
                                                    actionStatus: r.success,
                                                    pd16: i.options.options.typeName,
                                                };
                                                (i.lastTrackerEvent = r.createValidationEventFromEvent(
                                                    n,
                                                    i.lastTrackerEvent
                                                )),
                                                    r.checkAndTrackPageEvent(i.lastTrackerEvent);
                                            });
                                        })
                                        .catch(function (e) {
                                            console.log("Lock/Unlock has failed");
                                        });
                                }, 0);
                            },
                            function (n) {
                                i.handleError(n),
                                    "create" === t.eventID
                                        ? i.reloadDialog(e.type)
                                        : "createClose" === t.eventID && i.destroyComponent();
                                var o = {
                                    typeObjects: e.type,
                                    actionStatus: r.failure,
                                    pd16: i.options.options.typeName,
                                };
                                (i.lastTrackerEvent = r.createValidationEventFromEvent(
                                    o,
                                    i.lastTrackerEvent
                                )),
                                    r.checkAndTrackPageEvent(i.lastTrackerEvent);
                            }
                        );
                }
            },
            onSuccessOfCreation: function (e, t, n) {
                var i = this;
                if ("success" === e.status) {
                    if (i.responseFormat) {
                        if (
                            2 == i.responseFormat &&
                            ((e.parsedResult = JSON.parse(e.parsedResult)),
                            "FAILURE" !== e.parsedResult.result &&
                            ((e.parsedResult.rawAttr = JSON.parse(e.parsedResult.rawAttr)),
                                e.parsedResult.instances))
                        ) {
                            var o = e.parsedResult.instances;
                            for (var a in o) {
                                var r = o[a];
                                r.hasOwnProperty("to[CAD SubComponent].physicalid")
                                    ? ((r.relationphysicalid =
                                        r["to[CAD SubComponent].physicalid"]),
                                        delete r["to[CAD SubComponent].physicalid"])
                                    : r.hasOwnProperty("to[VPMInstance].physicalid") &&
                                    ((r.relationphysicalid = r["to[VPMInstance].physicalid"]),
                                        delete r["to[VPMInstance].physicalid"]),
                                r.hasOwnProperty("instanceName") &&
                                ((r.relationname = r.instanceName), delete r.instanceName);
                            }
                        }
                    } else {
                        var s = e.result,
                            c = [];
                        for (var l in s) {
                            var d = s[l],
                                u = {
                                    identifier: d.identifier ? d.identifier : d.physicalid,
                                    source: t,
                                    type: d.type,
                                };
                            d.relations && (u.relations = d.relations), c.push(u);
                        }
                        e.result = c;
                    }
                    i.dialog
                        ? i.dialog.postCreation(e, function (t) {
                            t && (e.facetData = t),
                                i.handleFacetErrors(e),
                                i.dispatchEvent("onSuccess", [e]),
                                n();
                        })
                        : (i.dispatchEvent("onSuccess", [e]), n());
                } else
                    i.dialog
                        ? i.dialog.postCreation(e, function () {
                            i.handleError(e), n();
                        })
                        : (i.handleError(e), n());
            },
            handleError: function (e) {
                var t = e.result;
                if (void 0 !== t && this.notification)
                    for (var n in t) {
                        var i = t[n];
                        if (i.errors)
                            for (ind1 in i.errors) {
                                var o = i.errors[ind1];
                                this.setNotification("error", o);
                            }
                    }
                this.dispatchEvent("onFailure", [e]);
            },
            handleFacetErrors: function (e) {
                var t = e.facetData;
                if (t && this.notification) {
                    var n,
                        i = { errorCode: "ENONEW_ERROR_8001" };
                    if (
                        (Object.keys(t).forEach(function (e) {
                            var i = t[e],
                                o = i.info;
                            if (o && o.errors && o.errors.length > 0) {
                                var a = o.errors[0];
                                "ENONEW_FACETS_POSTCREATION" === a.errorCode &&
                                a.errorMessages &&
                                (n || (n = []),
                                    n.push(i.facetNLS + " : " + a.errorMessages[0]));
                            }
                        }),
                            n)
                    ) {
                        i.errorMessages = n;
                        var o = "error";
                        "success" === e.status && (o = "error"), this.setNotification(o, i);
                    }
                }
            },
            getCurrentTypeNLS: function () {
                var e = "";
                if (this.commandName) e = this.commandName;
                else if (this.defaultAttributes.Type) {
                    var t = this.defaultAttributes.Type.value,
                        n = this.defaultAttributes.Type.range,
                        i = this.defaultAttributes.Type.rangeNLS,
                        o = n.indexOf(t);
                    o >= 0 && o < i.length && (e = i[o]);
                }
                return e;
            },
            setNotification: function (e, t) {
                var n = t.errorCode,
                    i = t.errorTitle ? t.errorTitle : s[n + "_Title"],
                    o = t.errorSubtitle ? t.errorSubtitle : s[n + "_Subtitle"],
                    a = "";
                if (t.errorMessages && 0 != t.errorMessages.length)
                    for (var r in t.errorMessages) {
                        a && (a += "<br>"), (a += t.errorMessages[r]);
                    }
                else a = s[n + "_Msg"];
                ("ENONEW_ERROR_5001" !== n && "InvalidAccess" !== n) ||
                t.errorTitle ||
                (i = s.cantCreateObject + ": " + this.getCurrentTypeNLS()),
                ("ENONEW_ERROR_0003" !== n &&
                    "ENONEW_ERROR_1003" !== n &&
                    "ENONEW_ERROR_1002" !== n) ||
                (i = s.ObjCreationFailed),
                ("ENONEW_ERROR_4005" !== n && "ENONEW_ERROR_0002" !== n) ||
                (i += ": " + this.getCurrentTypeNLS()),
                "ENONEW_ERROR_0007" === n &&
                (i += this.commandName ? ": " + this.commandName : "."),
                "ENONEW_ERROR_1004" === n &&
                ((i = s.cantCreateObject1 + ": " + this.getCurrentTypeNLS()),
                    (o = s.contactYourAdmin)),
                "ENONEW_ERROR_8001" === n &&
                (i = s.get(n + "_Title", { type: this.getCurrentTypeNLS() })),
                i || "error" !== e || (i = i = s.ObjCreationFailed);
                var c = { level: e, title: i, subtitle: o, message: a };
                this._setNotification(c);
            },
            _setNotification: function (e) {
                a.display(e);
            },
            update: function (e) {
                this.dialogView.updateView(e);
            },
            destroyComponent: function () {
                var e = {
                    newType: this.objInfo.options.typeName,
                    preferedType: this.objInfo.options.preferedType,
                    lockStatus: this.activatelock.preference,
                    draftStatus: this.activateDraftMode.preference,
                };
                this.dispatchEvent("onCancel", [e]),
                    n.dispatchEvent("onNewInfraExit", [e]),
                this.dialog && this.dialog.destroy(),
                    this.destroy();
            },
            _raiseNotif: function (e, t) {
                this.dispatchEvent("onNotification", { msg: e, eventID: t });
            },
            getSaveBtn: function () {
                var e = this,
                    t = new i({
                        label: s.saveBtn,
                        onClick: function (n) {
                            if (!e.dialog.onEscKeyDown) {
                                var i = e.objInfo.options.preferedType;
                                (i && "" != i && null != i && null != i) ||
                                (i = e.objInfo.type);
                                var o = i;
                                if (i) {
                                    var a = e.defaultAttributes.Type.range,
                                        r = e.defaultAttributes.Type.rangeNLS,
                                        c = a.indexOf(i);
                                    c >= 0 && c < r.length && (o = r[c]);
                                }
                                o &&
                                require(["DS/UIKIT/SuperModal"], function (n) {
                                    new n({ closable: !0, animate: !1, overlay: !0 }).confirm({
                                        title: s.saveSettingsTitle,
                                        message: s.get("confirmsaveSettingsMsg", { type: o }),
                                        callback: function (n) {
                                            if (((t.label = s.saveBtnLoading), n)) {
                                                var a = e.PartTemplate3D;
                                                if (a) {
                                                    var r = { type: i, UUID: a };
                                                    e.Services.set3DPartTemplate(
                                                        r,
                                                        void 0,
                                                        function (t) {
                                                            e.dialog._addNotif({
                                                                msg: s.get("confirmsaveSuccessMsg", {
                                                                    type: o,
                                                                }),
                                                                eventID: "success",
                                                            });
                                                        },
                                                        function (t) {
                                                            e.dialog._addNotif({
                                                                msg: s.get("confirmsaveErrorMsg", {
                                                                    type: o,
                                                                }),
                                                                eventID: "error",
                                                            });
                                                        }
                                                    );
                                                } else
                                                    e.dialog._addNotif({
                                                        msg: s.get("confirmsaveErrorMsg", { type: o }),
                                                        eventID: "error",
                                                    });
                                            }
                                            t.label = s.saveBtn;
                                        },
                                    });
                                });
                            }
                        },
                    });
                return t;
            },
            postFetchAttributes: function (e, t) {
                if (e && e.attributes.volatileAttributes) {
                    var n = e.attributes.volatileAttributes;
                    for (var i in n) {
                        var o = n[i];
                        if (
                            "3DPartTemplate" === o.name &&
                            ((this.PartTemplate3D = o.value), "launch" === t)
                        ) {
                            var a = this.getSaveBtn();
                            (a.visibleFlag = !1), (this.buttons.Close = a);
                        }
                    }
                }
            },
            buildID: function () {
                var e = "ID_";
                return (
                    this.objInfo.options.typeName && (e += this.objInfo.options.typeName),
                    this.objInfo.type && (e += this.objInfo.type),
                    this.Services && (e += this.Services.tenant),
                        e.replaceAll(" ", "_")
                );
            },
        });
    }
),
    define(
        "DS/ENONewWidget/ENONew/View/Facet/ENONewFacetBase",
        ["DS/EditPropWidget/facets/Common/FacetsBase"],
        function (e) {
            return e.extend({
                init: function (e, t) {
                    (this.sharedOptions = t),
                        (this.facetValidation = !0),
                        this._parent(e);
                },
                updateFacet: function () {
                    console.log("ENONewFacetBase : updateFacet() " + this.options.tabId);
                },
                setFacetValidation: function (e) {
                    (this.facetValidation = e), this.getOccurrence().setValidation(e);
                },
                getFacetvalidation: function () {
                    return this.facetValidation;
                },
                publishInfo: function (e, t) {
                    e && t && this.getOccurrence().addInfo(this.options.tabId, e, t);
                },
                fetchInfo: function (e, t) {
                    if (e) return this.getOccurrence().getInfo(e, t);
                },
                listenToPublishedInfo: function (e, t) {
                    var n = this;
                    setTimeout(() => {
                        n.getOccurrence().subscribeInfo(e, t);
                    });
                },
                postCreation: function (e) {
                    var t = this;
                    return (
                        console.log("onComplete : " + this.options.tabId),
                            new UWA.Promise(function (e) {
                                e({ name: t.options.tabId });
                            })
                    );
                },
                destroy: function () {
                    this._parent();
                },
            });
        }
    ),
    define("DS/ENONewWidget/utils/ENONewConstants", [], function () {
        "use strict";
        return {
            ATTRIBUTES: "attributes",
            INTERFACES: "interfaces",
            PUBLIC_ATTRIBUTES: "publicAttributes",
            EXTN_ATTRIBUTES: "extensionAttributes",
            VOLATILE_ATTRIBUTES: "volatileAttributes",
            TYPES: {
                INTEGER: "integer",
                REAL: "real",
                TIMESTAMP: "timestamp",
                PERSON: "person",
                BOOLEAN: "boolean",
                ENUM: "enum",
            },
            DIALOG: {
                TAB_VIEW: "tabView",
                WIZARD_VIEW: "wizardView",
                LIST_VIEW: "listView",
            },
            DOMAIN_ATTRIBUTES: "domainAttributes",
        };
    }),
    define(
        "DS/ENONewWidget/command/NewContentDialog",
        [
            "UWA/Class",
            "DS/Windows/ImmersiveFrame",
            "DS/Windows/Panel",
            "DS/Controls/Expander",
            "DS/Controls/Button",
            "DS/Controls/LineEditor",
            "i18n!DS/ENONewWidget/assets/nls/ENONewWidgetNls",
            "css!DS/ENONewWidget/ENONewWidget.css",
        ],
        function (e, t, n, i, o, a, r) {
            var s,
                c = r.newContentLabel;
            return e.extend({
                init: function () {
                    s = new t({ identifier: "ENONew_NewContentDialog_Frame" });
                },
                injectImmersiveFrame(e) {
                    e || (e = new UWA.Element("div")).inject(widget.body), s.inject(e);
                },
                display(e, t, n) {
                    var i,
                        o = new UWA.Element("div");
                    if ((o.inject(widget.body), s.inject(o), e)) {
                        var a = 0;
                        Object.keys(e).forEach(function (t) {
                            e[t] && (a += e[t].length);
                        }),
                            console.log("ENONewInfra NewContentDialog: noOfTypesToList=" + a);
                    }
                    if (e && 0 != a) {
                        console.log("ENONew : Not prepared");
                        var c = {
                            label: r.get("Type"),
                            UIType: "Expander",
                            items: [],
                            expanded: !0,
                        };
                        if (e) {
                            var l = {};
                            for (var d in e) {
                                var u = e[d];
                                u &&
                                u.length > 0 &&
                                u.forEach(function (e) {
                                    if (e) {
                                        var n = {
                                                UIType: "Command",
                                                label: e.nls,
                                                icon: t.iconBaseURL ? t.iconBaseURL + e.icon : e.icon,
                                                newType: d,
                                                objType: e.name,
                                            },
                                            i = e.package,
                                            o = l[i];
                                        o ||
                                        ((o = {
                                            UIType: "Expander",
                                            label: i,
                                            items: [],
                                            expanded: !0,
                                        }),
                                            (l[i] = o),
                                            c.items.push(o)),
                                            o.items.push(n);
                                    }
                                });
                            }
                        }
                        for (var f in l) l[f];
                        i = this.displayDialog([c], t, function (e) {
                            var t = e.dsModel.value;
                            t &&
                            n({
                                type: t.objType,
                                newType: t.newType,
                                commandName: t.label,
                            });
                        });
                    } else i = this.buildInputTypeField(t, n);
                    return i;
                },
                displayDialog: function (e, t, a) {
                    var r = new UWA.Element("div");
                    function l(e) {
                        var t = new UWA.Element("div", {
                                class: "expander_container grid-cell-expander",
                            }),
                            n = e.label ? e.label : "",
                            o = !!e.expanded && e.expanded,
                            a = new UWA.Element("div", {
                                class: "expander_content grid-container-content",
                            }),
                            r = e.items;
                        f(r).inject(a);
                        var s = new i({
                            header: n,
                            body: a,
                            style: "simple",
                            expandedFlag: o,
                        });
                        return s.inject(t), s;
                    }
                    r.inject(widget.body);
                    var d,
                        u,
                        f = function (e) {
                            var t, n, i, r, s, c;
                            if (e)
                                for (var d in ((t = new UWA.Element("div")), e)) {
                                    var u,
                                        f = e[d];
                                    "Expander" === f.UIType
                                        ? (u = l(f))
                                        : "Command" === f.UIType
                                            ? ((n = f),
                                                (i = void 0),
                                                (r = void 0),
                                                (s = void 0),
                                                (c = void 0),
                                                (i = new UWA.Element("div", {
                                                    class: "command_content grid-cell-btn",
                                                })),
                                                (r = n.label ? n.label : ""),
                                                (s = n.icon ? n.icon : ""),
                                            (c = n.launcherOptions ? n.launcherOptions : "") ||
                                            (c = n),
                                                new o({
                                                    value: c,
                                                    label: r,
                                                    icon: s,
                                                    displayStyle: "lite",
                                                    onClick: a,
                                                    styles: { display: "block", width: "fit-content" },
                                                }).inject(i),
                                                (u = i))
                                            : (console.log("NewContent : Incomplete data"),
                                                console.log(f)),
                                    u && u.inject(t);
                                }
                            else console.log("NewContent : No data");
                            return t;
                        },
                        p = f(e),
                        h = t.labelTopBar ? t.labelTopBar : c;
                    t.dimension &&
                    ((d = void 0 !== t.dimension.height ? t.dimension.height : -1),
                        (u = void 0 !== t.dimension.width ? t.dimension.width : -1));
                    var v = void 0 === t.resizable || t.resizable,
                        m = new n({
                            allowedDockAreas: WUXDockAreaEnum.NoneDockArea,
                            immersiveFrame: s,
                            title: h,
                            content: p,
                            height: d,
                            width: u,
                            resizableFlag: v,
                            position: { my: "center", at: "center", of: s },
                            identifier: "ENONew_NewContentDialog_Dialog",
                        });
                    return s.inject(r), m;
                },
                buildDialog: function (e) {
                    var t,
                        i,
                        o = e.identifier ? e.identifier : "ENONew_NewContentDialog",
                        a = e.labelTopBar ? e.labelTopBar : c;
                    e.dimension &&
                    ((t = void 0 !== e.dimension.height ? e.dimension.height : -1),
                        (i = void 0 !== e.dimension.width ? e.dimension.width : -1));
                    var r = void 0 === e.resizable || e.resizable,
                        l = e.content ? e.content : void 0;
                    return new n({
                        identifier: o,
                        allowedDockAreas: WUXDockAreaEnum.NoneDockArea,
                        immersiveFrame: s,
                        title: a,
                        content: l,
                        height: t,
                        width: i,
                        resizableFlag: r,
                        position: { my: "center", at: "center", of: s },
                    });
                },
                buildInputTypeField(e, t) {
                    var n = new UWA.Element("div", {
                            class: "command_content_vma12 input_type",
                        }),
                        i = new UWA.Element("div", { class: "lineContainer input_type" });
                    i.inject(n),
                        new UWA.Element("span", { text: "Dictionary Type" }).inject(i);
                    var r = new a({
                        placeholder: "Enter a value",
                        value: "VPMReference",
                    });
                    r.inject(i);
                    var s = new UWA.Element("div", {
                        class: "lineContainer button_create",
                    });
                    s.inject(n);
                    var c = function () {
                            t({ type: r.value, newType: "Default" });
                        },
                        l =
                            (new o({
                                value: "Create",
                                label: "Create",
                                onClick: c,
                                emphasize: "primary",
                                styles: { display: "block", width: "fit-content" },
                            }).inject(s),
                                this.buildDialog({ content: n }));
                    return (
                        l.addEventListener("keydown", function (e) {
                            13 == e.keyCode && c();
                        }),
                            l
                    );
                },
                destroyDialog: function () {
                    s.destroyAllWindows();
                },
            });
        }
    ),
    define(
        "DS/ENONewWidget/utils/Notification",
        [
            "UWA/Core",
            "DS/WebappsUtils/Console",
            "DS/Notifications/NotificationsManagerUXMessages",
            "DS/Notifications/NotificationsManagerViewOnScreen",
        ],
        function (e, t, n, i) {
            "use strict";
            var o = {
                notificationsManager: null,
                display: function (e) {
                    null === o.notificationsManager &&
                    ((o.notificationsManager = n),
                        i.setNotificationManager(o.notificationsManager),
                        i.setStackingPolicy(9));
                    var t = {
                        level: e.level,
                        title: e.title,
                        subtitle: e.subtitle,
                        message: e.message ? e.message : "",
                        sticky: "error" === e.level,
                    };
                    o.notificationsManager.addNotif(t);
                },
            };
            return o;
        }
    ),
    define(
        "DS/ENONewWidget/command/NewContentOptions",
        ["UWA/Class"],
        function (e) {
            return e.extend({
                init() {
                    this.prefType;
                },
                getNewTypeList: function () {
                    return { typeList: [], showSubtype: !1 };
                },
                getCreateOptions: function (e) {
                    return (
                        this.launcherOptions ||
                        (this.launcherOptions = {
                            type: e.type,
                            context: { service: "3DSpace" },
                            silentMode: !1,
                            notification: !0,
                            options: {
                                typeName: e.newType,
                                parentPIDs: [],
                                selectedIDs: [],
                                domainAttributes: [],
                            },
                            events: {
                                onSuccess: function () {
                                    console.log("Creation Successful");
                                },
                                onFailure: function () {
                                    console.log("Creation Error");
                                },
                                onCancel: function (e) {
                                    console.log("Creation dialog closed");
                                },
                            },
                        }),
                            this.launcherOptions
                    );
                },
                getLoginContext: () => ({
                    header: { SecurityContext: "" },
                    tenant: "",
                    service: "",
                    serviceBaseUrl: "",
                }),
            });
        }
    ),
    define(
        "DS/ENONewWidget/command/NewContentCmdMap",
        ["UWA/Class"],
        function (e) {
            var t = {
                MockDocument: {
                    cmdRequire:
                        "DS/ENOCollabSharingCmds/commands/Sample/ENONewMockDocumentCmd",
                },
                PhysicalProductCBDAssembly: {
                    cmdRequire:
                        "DS/ENOCollabSharingCmds/commands/ENONew/NewECcompCBDAssembly",
                },
                PhysicalProductCBDBoard: {
                    cmdRequire:
                        "DS/ENOCollabSharingCmds/commands/ENONew/NewECcompCBDBoard",
                },
                PhysicalProductPLMPCBPhysicalComponent: {
                    cmdRequire:
                        "DS/ENOCollabSharingCmds/commands/ENONew/NewECcompPLMPCBPhysicalComponent",
                },
                assembly: {
                    cmdRequire: "DS/ENOCollabSharingCmds/commands/ENONew/NewProduct",
                },
            };
            return e.singleton({ getCmdDetails: (e) => t[e], getRegisteredCmd() {} });
        }
    ),
    define("DS/ENONewWidget/utils/ENONewFacetConstants", [], function () {
        return {
            ENONewFACET_CLASSIFICATION: "classificationFacet",
            ENONewFACET_REVISION: "revisionFacet",
        };
    }),
    define(
        "DS/ENONewWidget/ajaxRequests/LifecycleServicesSettings",
        [
            "UWA/Class",
            "UWA/Class/Options",
            "DS/i3DXCompassPlatformServices/i3DXCompassPlatformServices",
        ],
        function (e, t, n) {
            "use strict";
            return e.singleton(t, {
                folder_json: null,
                tenant: "",
                options: { wintop: !1 },
                init: function (e) {
                    this._parent();
                },
                getTenant: function () {
                    return this.tenant;
                },
                setTenant: function (e) {
                    this.tenant = e;
                },
                getInstance: function (e) {
                    try {
                        this.init(e);
                    } catch (e) {}
                    for (var t in e) this.options[t] = e[t];
                    return this;
                },
                get3DSpaceUrl: function (e) {
                    var t = location.protocol + "//" + location.hostname;
                    return (
                        e && ((e.serverUrl = t), (e.serverUri = "3DSpace")), t + "/3DSpace"
                    );
                },
                get3DSpaceBaseUrl: function (e) {
                    if (!UWA.is(this.getOption("platform_services"), "array"))
                        throw new Error("Platform services not initialize yet");
                    var t;
                    try {
                        for (var n = 0; n < this.getOption("platform_services").length; n++)
                            if (this.getOption("platform_services")[n].platformId == e) {
                                t = this.getOption("platform_services")[n]["3DSpace"];
                                break;
                            }
                    } catch (e) {
                        t = this.get3DSpaceUrl();
                    }
                    return t;
                },
                get3DSpaceWSUrl: function (e, t, n) {
                    (e && "" != e) ||
                    (e = widget.getValue("x3dPlatformId") || "OnPremise");
                    var i =
                        this.get3DSpaceBaseUrl(e) +
                        t +
                        (t.indexOf("?") < 0 ? "?" : "&") +
                        "tenant=" +
                        e;
                    return null != n && null != n && (i = i + "&" + n), i;
                },
                getServerURL: function () {
                    var e = "";
                    return (
                        n.getPlatformServices({
                            onComplete: function (t) {
                                e = t[0]["3DSpace"];
                            },
                        }),
                            e
                    );
                },
                getPlatformUrl: function (e, t) {
                    var n = location.protocol + "//" + location.hostname,
                        i = e;
                    return t && ((t.serverUrl = n), (t.serverUri = i)), n + "/" + i;
                },
                getPlatformBaseUrl: function (e, t) {
                    if (!UWA.is(this.getOption("platform_services"), "array"))
                        throw new Error("Platform services not initialize yet");
                    var n;
                    try {
                        for (var i = 0; i < this.getOption("platform_services").length; i++)
                            if (this.getOption("platform_services")[i].platformId == t) {
                                n = this.getOption("platform_services")[i][e];
                                break;
                            }
                    } catch (e) {
                        n = this.getPlatformUrl();
                    }
                    return n;
                },
                getPlatformWSUrl: function (e, t, n, i) {
                    (null != t && null != t && "" != t) || (t = "OnPremise");
                    var o = this.getPlatformBaseUrl(e, t) + n + "?tenant=" + t;
                    return null != i && null != i && (o = o + "&" + i), o;
                },
                service_initialization: function (e, t) {
                    var i = this,
                        o = !1;
                    try {
                        UWA.is(this.getOption("platform_services"), "array") || (o = !0);
                    } catch (e) {
                        o = !0;
                    }
                    o
                        ? t
                            ? (i.setOption("platform_services", t), e())
                            : n.getPlatformServices({
                                onComplete: function (t) {
                                    i.setOption("platform_services", t),
                                        void 0 !== window.widget &&
                                        window.widget &&
                                        void 0 !== window.widget.getValue("x3dPlatformId") &&
                                        window.widget.getValue("x3dPlatformId") &&
                                        window.widget.getValue("x3dPlatformId").trim().length > 0
                                            ? (i.tenant = window.widget.getValue("x3dPlatformId"))
                                            : (i.tenant = t[0].platformId),
                                        e();
                                },
                                onFailure: function (t) {
                                    e();
                                },
                            })
                        : e();
                },
                getHeaders: function (e) {
                    var t = {
                        Accept: "application/json",
                        "Content-Type": "application/json",
                        "Accept-Language": widget.lang,
                    };
                    return e && (t.SecurityContext = e), t;
                },
            });
        }
    ),
    define(
        "DS/ENONewWidget/ajaxRequests/ENONewAjaxRequests",
        ["DS/WAFData/WAFData"],
        function (e) {
            return {
                _get: function (t, n, i) {
                    var o = Date.now().toString(),
                        a = "timestamp" + o;
                    t.params[a] = o;
                    var r = t.service + "?tenant=" + t.tenant;
                    -1 != r.indexOf("?") ? (r += "&") : (r += "?");
                    var s = t.params;
                    for (var c in s) {
                        var l = s[c];
                        l instanceof Array
                            ? l.forEach(function (e) {
                                r += c + "=" + e + "&";
                            })
                            : (r += c + "=" + l + "&");
                    }
                    var d = this.getHeaders(t.header);
                    e.authenticatedRequest(r, {
                        method: "GET",
                        headers: d,
                        timeout: 864e5,
                        type: "json",
                        onComplete: function (e) {
                            n(e);
                        },
                        onFailure: function (e, t) {
                            var n = e.message;
                            n.startsWith("NetworkError:") && n.contains("401")
                                ? i({ result: [{ errors: [{ errorCode: "InvalidAccess" }] }] })
                                : i({ result: [{ errors: [{ errorCode: "NetworkError" }] }] });
                        },
                        onTimeout: function () {
                            i({ result: [{ errors: [{ errorCode: "NetworkError" }] }] });
                        },
                    });
                },
                _post: function (t, n, i, o) {
                    var a = JSON.stringify(t),
                        r = n.service + "?tenant=" + n.tenant;
                    for (var s in (-1 != r.indexOf("?") ? (r += "&") : (r += "?"),
                        n.params)) {
                        var c = n.params[s];
                        c instanceof Array
                            ? c.forEach(function (e) {
                                r += s + "=" + e + "&";
                            })
                            : (r += s + "=" + c + "&");
                    }
                    var l = this.getHeaders(n.header);
                    e.authenticatedRequest(r, {
                        method: "POST",
                        headers: l,
                        timeout: 864e5,
                        data: a,
                        type: "json",
                        onComplete: function (e) {
                            i(e);
                        },
                        onFailure: function (e, t) {
                            var n = e.message;
                            n.startsWith("NetworkError:") && n.contains("401")
                                ? o({ result: [{ errors: [{ errorCode: "InvalidAccess" }] }] })
                                : o({ result: [{ errors: [{ errorCode: "NetworkError" }] }] });
                        },
                        onTimeout: function () {
                            o({ result: [{ errors: [{ errorCode: "NetworkError" }] }] });
                        },
                    });
                },
                getHeaders: function (e) {
                    var t = {
                        Accept: "application/json",
                        "Content-Type": "application/json",
                        "Accept-Language": widget.lang,
                    };
                    for (var n in e) {
                        var i = e[n];
                        "SecurityContext" === n && (i = encodeURIComponent(i)), (t[n] = i);
                    }
                    return t;
                },
            };
        }
    ),
    define("DS/ENONewWidget/validation/FileName", ["UWA/Class"], function (e) {
        "use strict";
        var t = {
            name: "CATIAV5",
            require: "DS/ENOXCADCATIAV5UI/ENOXCADFileNameValidation",
        };
        return {
            validate: function (e, n) {
                var i = e.value,
                    o = t.require;
                console.log("require " + o),
                    require([o], function (e) {
                        e.checkfilenameValidity(i, function (e) {
                            var t;
                            null != e &&
                            (console.log("ENOXCADFileNameValidation : " + e),
                                (t = { message: e, eventID: "warning" })),
                                n(t);
                        });
                    });
            },
        };
    }),
    define("DS/ENONewWidget/utils/Utils", [], function () {
        "use strict";
        function e() {}
        return (
            (e.prototype = {
                isArray: function (e) {
                    return "[object Array]" === Object.prototype.toString.call(e);
                },
                forEach: function (e, t) {
                    if (null != e)
                        if (("object" != typeof e && (e = [e]), this.isArray(e)))
                            for (var n = 0, i = e.length; n < i; n++)
                                t.call(null, e[n], n, e);
                        else
                            for (var o in e)
                                Object.prototype.hasOwnProperty.call(e, o) &&
                                t.call(null, e[o], o, e);
                },
            }),
                new e()
        );
    }),
    define(
        "DS/ENONewWidget/utils/ENONewAttributeUtil",
        [
            "DS/EditPropWidget/models/EditPropAttributeModel",
            "i18n!DS/ENONewWidget/assets/nls/ENONewWidgetNls",
        ],
        function (e, t) {
            "use strict";
            function n(e, t) {
                if (e) {
                    console.log("vma save local pref : " + e);
                    var n = "ENONew";
                    t && (n += "_" + t), (n += "_" + e);
                    var i = localStorage.getItem(n);
                    return console.log(n), i;
                }
            }
            return {
                _buildAttributesModel: function (t, i, o) {
                    var a = [];
                    if (UWA.is(t, "array"))
                        for (var r in t) {
                            var s = t[r],
                                c = s.name,
                                l = s.nls,
                                d = s.type,
                                u = s.value,
                                f = s.value,
                                p = s.isReadOnly,
                                h = s.visible,
                                v = s.extension,
                                m = s.range,
                                g = s.authorizedValuesRequired,
                                N = s.rangeNLS,
                                w = s.rangeGroup,
                                E = s.icons,
                                C = s.forbiddenChar,
                                y = s.dimension,
                                b = s.unitName,
                                D = s.dimensionType,
                                O = s.maxlength,
                                S = s.multiline,
                                I = s.multivalued,
                                A = s.positionUI,
                                T = s.positionGroup,
                                x = s.modified,
                                _ = s.mandatory,
                                W = s.visibility,
                                F = s.defaultAttribute,
                                U = s.storeLocalPreference,
                                k = s.validationService,
                                L = s.sortedOptions,
                                M = s.dropdownMode,
                                P = s.applysort;
                            if (h) {
                                d || (d = "string");
                                var V = d;
                                if ("string" === (d = d.toLowerCase())) V = "text";
                                else if ("integer" === d) V = "integer";
                                else if ("real" === d) V = "real";
                                else if ("boolean" === d) V = "boolean";
                                else if ("date" === d || "timestamp" === d) {
                                    if ("" != u) {
                                        u.contains("GMT") || (u = parseInt(u));
                                        var B = new Date(u);
                                        f = this._convertDateToStandard(B, !0).valueDB;
                                    }
                                    V = "timestamp";
                                }
                                var j = c;
                                (j = v ? v + "-" + j : j), F && (j = "New-" + c);
                                var R = new e({
                                    path: j,
                                    name: c,
                                    label: l,
                                    value: u,
                                    valueDB: f,
                                    type: V,
                                    readOnly: p,
                                });
                                "3DPartTemplate" == c && R.set("autocomplete", !0),
                                "boolean" !== V &&
                                m &&
                                (m && R.set("authorizedValues", m),
                                N && R.set("authorizedValuesNLS", N),
                                w && R.set("authorizedValuesGroup", w),
                                E && R.set("authorizedValuesIcon", E),
                                g && R.set("authorizedValuesRequired", g),
                                    R.set("customSelect", !0)),
                                h && R.set("visible", h),
                                C && R.set("forbiddenChar", C),
                                L && R.set("sortedOptions", L),
                                k && R.set("validationService", k),
                                O > 0 && R.set("maxlength", O),
                                W && R.set("visibility", W),
                                S && R.set("multiline", S),
                                x && R.set("modified", x),
                                I && R.set("multivalue", I),
                                _ && R.set("mandatory", _),
                                F && R.set("defaultAttribute", F),
                                M && R.set("dropdownMode", M),
                                P && R.set("applySort", P),
                                _ || R.set("facet", "ExtensionsFacet");
                                var z = 2;
                                F ? (z = 6) : o && (z = 9), T && (z = T);
                                var q = 1;
                                v && (z = "4." + v),
                                A || 0 === A || ((q = 2), (A = 0), R.set("masked", !1));
                                var K = z + "-" + q + "-" + A;
                                if ((R.set("positionUI", K), U)) {
                                    R.set("storeLocalPreference", U);
                                    var H = n(j, "attribute");
                                    H &&
                                    ((m && m.includes(H)) || !m) &&
                                    H != f &&
                                    (R.set("valueDB", H), R.set("isModified", !0));
                                }
                                null != y &&
                                (R.set("dimension", y),
                                    R.set("unitName", b),
                                    R.set("dimensionType", D));
                            }
                            null != R && a.push(R);
                        }
                    return a;
                },
                _parseAttributeModels: function (e, t) {
                    if (t && e) {
                        if (e.hasOwnProperty("value") && e.value !== t.valueDB) {
                            var i = t.valueDB;
                            ("number_wux" != t.tw_type && "number" != t.tw_type) ||
                            !UWA.is(i, "number") ||
                            (i = "" + i),
                                (e.value = i),
                                (e.modified = !0);
                        }
                        if (e.storeLocalPreference)
                            !(function (e, t, i) {
                                if (e && t) {
                                    console.log("vma save local pref : " + e + " = " + t);
                                    var o = "ENONew";
                                    i && (o += "_" + i),
                                        (o += "_" + e),
                                        console.log(o),
                                        localStorage.setItem(o, t),
                                        console.log(n(e, i));
                                }
                            })(t.getIdentifier(), t.valueDB, "attribute");
                    }
                },
                _convertDateToStandard: function (e, t) {
                    if (!e) return { value: "", valueDB: "" };
                    var n = t ? e : new Date(),
                        i = e.getUTCFullYear(),
                        o = e.getUTCMonth() + 1,
                        a = e.getUTCDate(),
                        r = n.getUTCHours(),
                        s = n.getUTCMinutes(),
                        c = n.getUTCSeconds();
                    return (
                        o < 10 && (o = "0" + o),
                        a < 10 && (a = "0" + a),
                        r < 10 && (r = "0" + r),
                        s < 10 && (s = "0" + s),
                        c < 10 && (c = "0" + c),
                            n.setYear(i),
                            n.setMonth(e.getMonth()),
                            n.setDate(a),
                            {
                                value: n.toLocaleString(widget.lang),
                                valueDB:
                                    i + "/" + o + "/" + a + "@" + r + ":" + s + ":" + c + ":GMT",
                            }
                    );
                },
            };
        }
    ),
    define(
        "DS/ENONewWidget/ENONew/Events",
        ["UWA/Class", "UWA/Class/Events"],
        function (e, t) {
            return e.singleton(t, {
                init() {
                    this._parent(), console.log("ENONew Event init");
                },
                dispatchEvent() {
                    this._parent.apply(this, arguments);
                },
                addEvent() {
                    this._parent.apply(this, arguments);
                },
            });
        }
    ),
    define("DS/ENONewWidget/ENONewWidget", [], function () {}),
    define(
        "DS/ENONewWidget/command/NewContentAppOptions",
        ["UWA/Class"],
        function (e) {
            return e.singleton({
                setAppContext(e) {
                    e
                        ? ((this.AppContext = e), (this.isAppContextSet = !0))
                        : (isAppContextSet = !1);
                },
                getAppContext() {
                    return this.AppContext;
                },
                isAppContextSet: !1,
            });
        }
    ),
    define(
        "DS/ENONewWidget/command/sample/ENONewDummyCmdOptions",
        ["DS/ENONewWidget/command/NewContentOptions"],
        function (e) {
            return e.singleton({
                getCreateOptions: function (e) {
                    return (
                        this.launcherOptions ||
                        ((this.launcherOptions = {
                            context: { service: "3DSpace" },
                            silentMode: !1,
                            notification: !0,
                            options: {
                                parentPIDs: e.parentPIDs,
                                selectedIDs: [],
                                domainAttributes: [{ name: "DummyBookmark" }],
                            },
                        }),
                        this.events && (this.launcherOptions.events = this.events)),
                            this.launcherOptions
                    );
                },
            });
        }
    ),
    //初始化新建页面
    define(
        "DS/ENONewWidget/ENONew/ENONewEngine",
        [
            "UWA/Core",
            "UWA/Controls/Abstract",
            "DS/WidgetServices/WidgetServices",
            "DS/ENONewWidget/ENONew/Controller/ENONewController",
            "DS/ENONewWidget/utils/ENONewConstants",
        ],
        function (e, t, n, i, o) {
            var a = ["DummyBookmark"];
            return t.extend({
                objInfo: {
                    id: "",
                    context: {
                        header: { SecurityContext: "" },
                        tenant: "",
                        service: "",
                        serviceBaseUrl: "",
                    },
                    labelTopBar: "",
                    commandName: "",
                    overlay: !0,
                    type: "",
                    silentMode: !1,
                    responseFormat: 0,
                    notification: !0,
                    activatelock: { show: !1, preference: "" },
                    activateDraftMode: { show: !1, preference: "" },
                    activateCreateAnother: !1,
                    options: {
                        preferedType: "",
                        typeName: "",
                        select: [],
                        parentPIDs: [],
                        selectedIDs: [],
                        parentOrigin: [],
                        folderID: [],
                        activeFolder: !1,
                        attributes: {},
                        domainAttributes: [{ name: "", value: [] }],
                    },
                    facets: [],
                    viewType: "listView",
                    events: {
                        onSuccess: function () {},
                        onFailure: function () {},
                        onCancel: function () {},
                    },
                },
                _getController: function (e) {
                    return this.controller ? this.controller : new i(e);
                },
                init: function (e, t) {
                    if (
                        (console.log("ENONew Engine init"),
                        t && n.setWidgetObject(t),
                            this._parent(e),
                            e)
                    ) {
                        (this.context = e.context),
                            (this.objInfo.id = void 0 !== e.id ? e.id : ""),
                            (this.objInfo.context = e.context ? e.context : {}),
                        e.options || (e.options = {}),
                            (this.objInfo.type = e.type),
                            (this.objInfo.silentMode = !0 === e.silentMode),
                            (this.objInfo.responseFormat = e.responseFormat
                                ? e.responseFormat
                                : 0),
                        this.objInfo.responseFormat && (this.objInfo.parsed = !0),
                            (this.objInfo.labelTopBar = e.labelTopBar),
                            (this.objInfo.commandName = e.commandName),
                            (this.objInfo.overlay = 0 != e.overlay),
                            (this.objInfo.notification = 0 != e.notification),
                            (this.objInfo.activateCreateAnother =
                                !0 === e.activateCreateAnother),
                            (this.objInfo.facets = e.facets ? e.facets : []),
                            (this.objInfo.viewType = e.viewType
                                ? e.viewType
                                : o.DIALOG.LIST_VIEW),
                        e.activatelock || (e.activatelock = {}),
                            (this.objInfo.activatelock = {
                                show: 1 == e.activatelock.show,
                                preference: e.activatelock.preference,
                            }),
                        e.activateDraftMode || (e.activateDraftMode = {}),
                            (this.objInfo.activateDraftMode = {
                                show: 1 == e.activateDraftMode.show,
                                preference: e.activateDraftMode.preference,
                            });
                        var i = this.objInfo.options;
                        for (var r in i) {
                            if (r === o.DOMAIN_ATTRIBUTES) {
                                var s = e.options[r],
                                    c = [];
                                s &&
                                (s.forEach(function (e) {
                                    if (e && e.name && a.includes(e.name)) {
                                        var t = { name: e.name };
                                        e.value && (t.value = e.value), c.push(t);
                                    }
                                }),
                                    (e.options[r] = c));
                            }
                            i[r] = e.options[r];
                        }
                        this.objInfo.type || (this.objInfo.type = i.typeName);
                        var l = this.objInfo.events;
                        if (e.events) for (var d in l) l[d] = e.events[d];
                        this.controller = this._getController(this.objInfo);
                    }
                },
            });
        }
    ),
    define(
        "DS/ENONewWidget/ENONew/View/Facet/ENONewFacetHelper",
        [
            "DS/ENONewWidget/utils/ENONewFacetConstants",
            "i18n!DS/ENONewWidget/assets/nls/ENONewWidgetNls",
            "i18n!DS/ENONewWidget/assets/nls/ENONewFacetNls",
        ],
        function (e, t, n) {
            var i = {
                facetObject: {
                    require: "",
                    alwaysCalled: !0,
                    facet: { name: "", text: "", icon: "" },
                    options: { data: {} },
                },
                getFacetDefinition: function (t) {
                    return ((i = {}),
                        (i[e.ENONewFACET_CLASSIFICATION] = {
                            require: "DS/PLMNewCmp/PLMNewClassificationFacet",
                            alwaysCalled: !0,
                            facet: {
                                name: e.ENONewFACET_CLASSIFICATION,
                                text: n.get(e.ENONewFACET_CLASSIFICATION),
                                icon: "classification-book",
                            },
                            dependee: [],
                        }),
                        (i[e.ENONewFACET_REVISION] = {
                            require: "DS/CfgWebAppXRevisionCommands/facets/PLMNewRevisionFacet",
                            alwaysCalled: !1,
                            facet: {
                                name: e.ENONewFACET_REVISION,
                                text: n.get(e.ENONewFACET_REVISION),
                                icon: "flow-branch-list",
                            },
                            dependee: [],
                        }),
                        i)[t];
                    var i;
                },
                getFacetList: function (e, t, n) {
                    var o = [],
                        a = this.getDefaultFacet();
                    return (
                        (a.options.data = e),
                        n.buttons && (a.options.buttons = n.buttons),
                            o.push(a),
                        t &&
                        t.forEach(function (e) {
                            var t = i.getFacetDefinition(e.name);
                            t &&
                            ((t.options = null != e.facetoptions ? e.facetoptions : {}),
                            n.securityContext &&
                            (t.options.securityContext = n.securityContext),
                                o.push(t));
                        }),
                            o
                    );
                },
                getDefaultFacet: function () {
                    return {
                        require: "DS/ENONewWidget/ENONew/View/Facet/ENOListViewFacet",
                        facet: {
                            name: this.getDefaultFacetName(),
                            text: n.get("defaultFacetCreate"),
                            icon: "attributes",
                        },
                        options: {},
                    };
                },
                getDefaultFacetName: function () {
                    return "ENOCreate";
                },
            };
            return i;
        }
    ),
    define(
        "DS/ENONewWidget/ENONewLauncher",
        [
            "UWA/Controls/Abstract",
            "DS/ENONewWidget/ajaxRequests/LifecycleServicesSettings",
        ],
        function (e, t) {
            return (
                t.service_initialization(function () {}),
                    e.extend({
                        CreateNew: function (e) {
                            e &&
                            require(["DS/ENONewWidget/ENONew/ENONewEngine"], function (t) {
                                new t(e);
                            });
                        },
                    })
            );
        }
    ),
    define(
        "DS/ENONewWidget/command/NewContentCommandBase",
        [
            "DS/ApplicationFrame/Command",
            "DS/ENONewWidget/ENONewLauncher",
            "DS/ENONewWidget/command/NewContentAppOptions",
            "i18n!DS/ENONewWidget/assets/nls/ENONewWidgetNls",
        ],
        function (e, t, n, i) {
            "use strict";
            return e.extend({
                init: function (e) {
                    if ((this._parent(e), n.isAppContextSet))
                        (this.ENONewContext = n.getAppContext()),
                            console.log(
                                "NewContentCommandBase init NewContentAppOptions : " +
                                e.ID +
                                " " +
                                e.className
                            );
                    else if (e.context) {
                        var i = e.context.getENONewContext;
                        UWA.is(i, "function") &&
                        ((this.ENONewContext = e.context.getENONewContext()),
                            console.log(
                                "NewContentCommandBase init options.context : " +
                                e.ID +
                                " " +
                                e.className
                            ));
                    }
                    this.launcher = new t();
                },
                launchCreateNew(e) {
                    this.launcher && this.launcher.CreateNew(e);
                },
                execute() {
                    var e = this.getCreateOptions({ newType: this.getType() });
                    this.launchCreateNew(e);
                },
                getCreateOptions(e) {
                    var t,
                        n,
                        i = this.ENONewContext;
                    if (
                        (i &&
                        i.getCreateOptions &&
                        UWA.is(i.getCreateOptions, "function") &&
                        (n = i.getCreateOptions(e)),
                        this.getOptions &&
                        UWA.is(this.getOptions, "function") &&
                        (t = this.getOptions(e)),
                            t)
                    ) {
                        if (n)
                            for (var o in n) {
                                var a = n[o];
                                if ("events" === o) t[o] || (t[o] = {}), Object.assign(t[o], a);
                                else if (null != a || null != a)
                                    if (UWA.is(a, "object") && "options" === o)
                                        if (t[o])
                                            for (var r in a) {
                                                var s = a[r];
                                                s && (t[o][r] = s);
                                            }
                                        else t[o] = a;
                                    else t[o] = a;
                            }
                    } else n && (t = n);
                    return (
                        this.getType &&
                        UWA.is(this.getType, "function") &&
                        (t || (t = {}),
                        t.options || (t.options = {}),
                            (t.options.typeName = this.getType())),
                            t
                    );
                },
                getCommandLabel: () => i.get("new"),
            });
        }
    ),
    define(
        "DS/ENONewWidget/command/sample/ENONewDummyCmd",
        ["DS/ENONewWidget/command/NewContentCommandBase"],
        function (e) {
            "use strict";
            return e.extend({
                defaults: { type: "VMADocument" },
                getType() {
                    return this.defaults.type;
                },
            });
        }
    ),
    define(
        "DS/ENONewWidget/command/sample/ENONewMockDocumentCmd",
        ["DS/ENONewWidget/command/NewContentCommandBase"],
        function (e) {
            "use strict";
            return e.extend({
                defaults: { type: "MockDocument" },
                getType() {
                    return this.defaults.type;
                },
            });
        }
    ),
    define(
        "DS/ENONewWidget/ajaxRequests/WebReq",
        [
            "UWA/Class",
            "DS/WAFData/WAFData",
            "DS/ENONewWidget/utils/Utils",
            "DS/ENONewWidget/ajaxRequests/LifecycleServicesSettings",
        ],
        function (e, t, n, i) {
            var o = e.singleton({
                defaults: { type: "json", timeout: 6e4 },
                init: function () {
                    ["delete", "get", "head", "options"].forEach(function (e) {
                        this[e] = function (t, n) {
                            return this.request(UWA.merge(n || {}, { url: t, method: e }));
                        };
                    }, this),
                        ["post", "put", "patch"].forEach(function (e) {
                            this[e] = function (t, n, i) {
                                let o = { ...(i || {}) };
                                return this.request(
                                    UWA.merge(o, { url: t, method: e, data: n })
                                );
                            };
                        }, this);
                },
                request: function (e) {
                    return (
                        "string" == typeof e
                            ? (e = { url: arguments[0] })
                            : (e = e || {}).data &&
                            "string" != typeof e.data &&
                            (e.data = JSON.stringify(e.data)),
                        UWA.Utils.parseUrl(e.url).protocol ||
                        (e.url = i.get3DSpaceWSUrl(e.tenant, e.url, null)),
                            ((e = UWA.merge(e, this.defaults)).method = e.method
                                ? e.method.toLowerCase()
                                : "get"),
                            (e.headers = e.headers || i.getHeaders(e.securityContext)),
                        e.securityContext || delete e.headers.SecurityContext,
                            this._dispatchPromise(e)
                    );
                },
                _dispatchPromise: function (e) {
                    var n = this;
                    return new UWA.Promise(function (i, o) {
                        t.authenticatedRequest(n._createURL(e), {
                            method: e.method,
                            headers: e.headers,
                            timeout: e.timeout,
                            data: e.data,
                            type: e.type,
                            onComplete: function (e) {
                                i(e);
                            },
                            onFailure: function (e, t, i) {
                                o({
                                    error: e,
                                    response: t,
                                    headers: i,
                                    errorMessage: n._parseError(e, t),
                                });
                            },
                            onTimeout: function () {
                                o({ isTimeout: !0 });
                            },
                        });
                    });
                },
                onWebServiceError: function (e, t, n, i, o) {
                    UWA.is(t, "string") &&
                    t.startsWith("NetworkError:") &&
                    e({ result: [{ errors: [{ errorCode: "NetworkError" }] }] });
                },
                _parseError: function (e, t) {
                    if (t && t.error) return JSON.stringify(t.error);
                },
                _createURL: function (e) {
                    var t = e.url;
                    return (
                        n.forEach(e.params, function (e, n) {
                            t += (-1 === t.indexOf("?") ? "?" : "&") + n + "=" + e;
                        }),
                            t
                    );
                },
            });
            return o.init(), o;
        }
    ),
    define(
        "DS/ENONewWidget/ENONew/View/ENOListView",
        [
            "UWA/Core",
            "UWA/Controls/Abstract",
            "DS/EditPropWidget/facets/Properties/Properties",
            "DS/EditPropWidget/models/EditPropAttributeModel",
            "DS/EditPropWidget/models/EditPropModel",
            "DS/EditPropWidget/utils/AttributesUtils",
            "DS/ENONewWidget/utils/ENONewAttributeUtil",
            "DS/ENONewWidget/utils/ENONewConstants",
            "i18n!DS/ENONewWidget/assets/nls/ENONewWidgetNls",
            "css!DS/ENONewWidget/ENONewWidget.css",
        ],
        function (e, t, n, i, o, a, r, s, c) {
            return t.extend({
                init: function (t) {
                    (this.customEvents = []),
                        (this.floatingAttributes = {}),
                        (this.attributesForValidationService = []),
                        (this.persistAttribute = {}),
                        (this.enableNotif = !1),
                        (this.identifier = t.identifier),
                        (this.skeleton = e.createElement("div", {
                            class: "Attributes_container",
                            "data-rec-id": "Attributes_container_rec-id",
                            styles: {
                                width: "100%",
                                height: "100%",
                                display: "flex",
                                "flex-direction": "column",
                            },
                        }));
                    var i = t.events;
                    for (eventID in i) {
                        var o = i[eventID];
                        this.addEvent(eventID, o);
                    }
                    (this.onComplete = t.onComplete), (this.buttons = t.buttons);
                    var a = new n({
                        setCloseButton: !1,
                        editMode: !0,
                        readOnly: !1,
                        scroller: !1,
                        identifier: this.identifier,
                        webUxTweakers: !0,
                        events: {
                            onCancel: function () {},
                            onNotification: function (e) {},
                        },
                    }).inject(this.skeleton);
                    this.propUI = a;
                    a.setAccordionProperties({
                        ExtensionsFacet: {
                            name: "ExtensionsFacet",
                            nameNLS: "Extensions",
                            weight: -10,
                        },
                    });
                },
                buildModel: function (e) {
                    (this.data = e), (this.propModel = new o());
                    var t = this._buildAttributesModel(e);
                    // add by chenyan 重排序
                    let isXENAPP = false ;
                    if (widget){
                        const  appID = widget.data.appId;
                        console.log("appID:",appID)
                        if ("ENXENG_AP" == appID){
                            isXENAPP = true ;
                        }
                    }
                    if (isXENAPP){
                        //必填顺序
                        //20260720 update by ljr XEN新建零件界面不再显示DirectBuy属性。
                        const orderAttrName = ["JF_VPMReference-JF_PartNameCN","JF_VPMReference-JF_PartNameEN","JF_VPMReference-JF_PartType","JF_VPMReference-JF_Detail_CN","JF_VPMReference-JF_Detail_EN","JF_VPMReference-JF_ProcurementType"] ;
                        //重排顺序属性 模态框渲染属性是按 push属性的先后
                        const arrAttrNameObj = new Array();
                        var tEmpty = {};
                        for (let j = 0; j < orderAttrName.length; j++) {
                            const attrName = orderAttrName[j];
                            arrAttrNameObj.push(t[attrName]);
                            //移除改属性
                            Reflect.deleteProperty(t, attrName);
                        }
                        for (let j = 0; j < orderAttrName.length; j++) {
                            t[orderAttrName[j]] = arrAttrNameObj[j];
                        }
                    }
                    this.removeJFXENUnusedAttributesFromPropData(t);
                    this.setPropModel(t);
                },
                isJFXENApp: function () {
                    return typeof widget !== "undefined" && widget && widget.data && "ENXENG_AP" == widget.data.appId;
                },
                getJFXENHiddenAttrNames: function () {
                    //20260720 update by ljr XEN创建零件选择库后不再显示DirectBuy、客户零件号、客户零件版本。
                    return [
                        "DirectBuy",
                        "JF_DirectBuy",
                        "attribute_JF_DirectBuy",
                        "JF_VPMReference.JF_DirectBuy",
                        "JF_VPMReference-JF_DirectBuy",
                        "attribute_JF_VPMReference.JF_DirectBuy",
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
                        "客户零件号",
                        "客户零件版本",
                        "客户零件版本号"
                    ];
                },
                isJFXENHiddenAttrName: function (e) {
                    if (!e) return false;
                    var t = String(e),
                        n = this.getJFXENHiddenAttrNames();
                    return n.some(function (e) {
                        return t === e || t.endsWith("-" + e) || t.indexOf("attribute[" + e + "]") >= 0;
                    });
                },
                isJFXENHiddenAttrModel: function (e, t) {
                    if (!this.isJFXENApp()) return false;
                    if (this.isJFXENHiddenAttrName(e)) return true;
                    var n = [];
                    if (t) {
                        t.getIdentifier && n.push(t.getIdentifier());
                        n.push(t.name, t.path, t.dbName, t.dataIndex, t.nls, t.label, t.displayName);
                        if (t.get) {
                            ["name", "path", "dbName", "dataIndex", "nls", "label", "displayName"].forEach(function (e) {
                                n.push(t.get(e));
                            });
                        }
                    }
                    return n.some(this.isJFXENHiddenAttrName.bind(this));
                },
                removeJFXENUnusedAttributesFromPropData: function (e) {
                    if (!this.isJFXENApp() || !e) return false;
                    var t = this,
                        n = false;
                    Object.keys(e).forEach(function (i) {
                        if (t.isJFXENHiddenAttrModel(i, e[i])) {
                            delete e[i];
                            n = true;
                        }
                    });
                    if (this.attributesForValidationService) {
                        this.attributesForValidationService = this.attributesForValidationService.filter(function (e) {
                            return !t.isJFXENHiddenAttrName(e);
                        });
                    }
                    if (this.persistAttribute) {
                        Object.keys(this.persistAttribute).forEach(function (e) {
                            t.isJFXENHiddenAttrName(e) && delete t.persistAttribute[e];
                        });
                    }
                    return n;
                },
                removeJFXENUnusedAttributesFromPropModel: function (e) {
                    if (!this.isJFXENApp() || !e) return false;
                    var t = this,
                        n = e.getAllAttributes ? e.getAllAttributes() : e._attributes,
                        o = false;
                    n && Object.keys(n).forEach(function (a) {
                        var r = n[a];
                        if (t.isJFXENHiddenAttrModel(a, r)) {
                            r && (r.visible = false);
                            try {
                                t.propUI && t.propUI.elements && t.propUI.elements.formSection && t.propUI.elements.formSection.toggleFormOrgLine(a, false);
                            } catch (e) {
                                console.log("toggle hidden XEN attribute failed", e);
                            }
                            delete n[a];
                            e.changedValues && delete e.changedValues[a];
                            t.persistAttribute && delete t.persistAttribute[a];
                            o = true;
                        }
                    });
                    if (this.attributesForValidationService) {
                        this.attributesForValidationService = this.attributesForValidationService.filter(function (e) {
                            return !t.isJFXENHiddenAttrName(e);
                        });
                    }
                    return o;
                },
                hideJFXENUnusedAttributeLines: function () {
                    if (!this.isJFXENApp() || !this.skeleton || !this.skeleton.querySelectorAll) return;
                    //20260720 update by ljr XEN选择库后库属性异步追加时，按行隐藏DirectBuy、客户零件号、客户零件版本号。
                    var e = this,
                        t = function (t) {
                            return e.isJFXENHiddenAttrName(String(t || "").replace(/\s|\*/g, ""));
                        },
                        n = function (e) {
                            var t = e;
                            while (t && t !== document && !(t.classList && t.classList.contains("formLine"))) {
                                t = t.parentNode;
                            }
                            return t && t.classList && t.classList.contains("formLine") ? t : null;
                        };
                    Array.prototype.forEach.call(this.skeleton.querySelectorAll(".textContainer"), function (e) {
                        var i = e.textContent || e.innerText || "";
                        if (t(i)) {
                            var o = n(e);
                            o && (o.style.display = "none", o.setAttribute("data-jf-xen-hidden", "true"));
                        }
                    });
                    Array.prototype.forEach.call(this.skeleton.querySelectorAll(".formTweakerContainer"), function (i) {
                        var o = i.getAttribute("id");
                        if (t(o)) {
                            var a = n(i);
                            a && (a.style.display = "none", a.setAttribute("data-jf-xen-hidden", "true"));
                        }
                    });
                },
                scheduleHideJFXENUnusedAttributeLines: function () {
                    if (!this.isJFXENApp()) return;
                    var e = this;
                    [0, 100, 500].forEach(function (t) {
                        setTimeout(function () {
                            e.removeJFXENUnusedAttributesFromPropModel(e.propModel);
                            e.hideJFXENUnusedAttributeLines();
                        }, t);
                    });
                },
                startJFXENUnusedAttributeObserver: function () {
                    if (!this.isJFXENApp() || this.jfXENUnusedAttrObserver || !this.skeleton || typeof MutationObserver === "undefined") return;
                    var e = this;
                    //20260720 update by ljr 监听库属性异步渲染，确保废弃属性行不再显示。
                    this.jfXENUnusedAttrObserver = new MutationObserver(function () {
                        e.hideJFXENUnusedAttributeLines();
                    });
                    this.jfXENUnusedAttrObserver.observe(this.skeleton, {
                        childList: true,
                        subtree: true
                    });
                },
                setPropModel: function (e) {
                    var t,
                        n = this,
                        o = this.propModel;
                    if (e) {
                        for (var a in this.customEvents) {
                            var r = this.customEvents[a];
                            o.addEvent(r.eventID, r.callback.bind(r));
                        }
                        t = o.set(e);
                    }
                    if (
                        (o.addEvent("onEnterKeyDown", function (e) {
                            n.dispatchEvent("onEnterKeyDown", [e]);
                        }),
                            t)
                    ) {
                        o.addEvent("onChange", function (e) {
                            console.log("ENONew onChange");
                            var t = e._changed;
                            for (var a in (console.log(t), t)) {
                                var i = t[a];
                                i.isModified || (i.isModified = !0), (i.isUIModified = !0);
                            }
                            n.removeJFXENUnusedAttributesFromPropModel(o) && setTimeout(function () {
                                n.refresh();
                            }, 0);
                            n.scheduleHideJFXENUnusedAttributeLines();
                        }),
                            o.addEvent("onChange:New-Type", function () {
                                console.log("ENONew Type Changed"),
                                    n.dispatchEvent("onChangeAttribute", [this._changed]);
                            }),
                            //add by ljr
                            // o.addEvent("onChange:JF_VPMReference-JF_PartType", function () {
                            //     console.log("ENONew JF_PartType  Changed"),
                            //         n.dispatchEvent("onChangeAttribute", [this._changed]);
                            // }),
                            o.addEvent("onChange:V_Name", function () {
                                console.log("ENONew V_Name  Changed"),
                                    n.dispatchEvent("onChangeAttribute", [this._changed]);
                            }),
                            o.addEvent("onChange:JF_VPMReference-JF_PartSubType", function () {
                                console.log("ENONew JF_PartSubType  Changed"),
                                    n.dispatchEvent("onChangeAttribute", [this._changed]);
                            }),
                            //end
                            o.addEvent("onChange:New-SecurityContext", function () {
                                console.log("ENONew SecurityContext Changed"),
                                    n.dispatchEvent("onChangeAttribute", [this._changed]);
                            }),
                        o.get("3DPartTemplate") &&
                        o.addEvent("onChange:3DPartTemplate", function (e, t) {
                            console.log("ENONew 3DPartTemplate Changed"),
                                n.dispatchEvent("onChangeAttribute", [this._changed]);
                        });
                        //add by chenyan 更新属性
                        widget.addEvent("JFUpdatePartType",function (args) {
                            let rangeIndex = -1 ;
                            var partType = o.get("JF_VPMReference-JF_PartType");
                            var detail_CN = o.get("JF_VPMReference-JF_Detail_CN");
                            var detail_EN = o.get("JF_VPMReference-JF_Detail_EN");
                            var v_Name = o.get("ExtensionsFacet-V_Name");
                            var fullV_Name  = "";
                            if ( args.DetailCN){
                                fullV_Name = args.DetailCN ;
                            }
                            if (args.DetailEN){
                                if (fullV_Name){
                                    fullV_Name = fullV_Name + "|" + args.DetailEN ;
                                }else {
                                    fullV_Name = args.DetailEN ;
                                }
                            }
                            //更新标题
                            if (fullV_Name){
                                //var newV_Name = new i(v_Name);
                                //newV_Name.set("value", fullV_Name);
                                //newV_Name.set("valueDB", fullV_Name);
                                //o.set(newV_Name.getIdentifier(), newV_Name);
                            }
                            //设置 详细类别-EN 详细类别-CN
                            var newDetailCN = new i(detail_CN);
                            var newDetailEN = new i(detail_EN);

                            newDetailCN.set("value", args.DetailCN);
                            newDetailCN.set("valueDB", args.DetailCN);
                            newDetailEN.set("value", args.DetailEN);
                            newDetailEN.set("valueDB", args.DetailEN);

                            o.set(newDetailCN.getIdentifier(), newDetailCN);
                            o.set(newDetailEN.getIdentifier(), newDetailEN);
                            // 设置零件类型
                            for (let j = 0; j < partType.authorizedValues.length; j++) {
                                if ( args.range == partType.authorizedValues[j]){
                                    rangeIndex = j ;
                                    break;
                                }
                            }
                            //只有配置range时才会更新
                            if (rangeIndex >= 0){
                                var newPartType = new i(partType);
                                let rangeNls = partType.authorizedValuesNLS[rangeIndex];
                                newPartType.set("value", rangeNls);
                                newPartType.set("valueDB", args.range);
                                o.set(newPartType.getIdentifier(), newPartType);
                            }
                            n.removeJFXENUnusedAttributesFromPropModel(o) && setTimeout(function () {
                                n.refresh();
                            }, 0);
                            n.scheduleHideJFXENUnusedAttributeLines();

                        });
                        var s = o.get("ExtensionsFacet-Filename")
                            ? "ExtensionsFacet-Filename"
                            : !!o.get("Filename") && "Filename";
                        if (s) {
                            var c = o.get("PLMEntity.V_Name")
                                ? "PLMEntity.V_Name"
                                : !!o.get("ExtensionsFacet-PLMEntity.V_Name") &&
                                "ExtensionsFacet-PLMEntity.V_Name";
                            c ||
                            (c = o.get("V_Name")
                                ? "V_Name"
                                : !!o.get("ExtensionsFacet-V_Name") &&
                                "ExtensionsFacet-V_Name");
                            var l = !1;
                            o.addEvent("onChange:" + c, function () {
                                if (!l) {
                                    var e = o.get(s),
                                        t = o.get(c);
                                    if (e && e.visible) {
                                        // add by chenyan 文件名和标题不联动 2025/07/17
                                        var n = new i(e);
                                        // n.set("value", t.get("value")),
                                        //     n.set("valueDB", t.get("valueDB")),
                                            o.set(n.getIdentifier(), n),
                                            (l = !1);
                                    }
                                }
                            }),
                                o.addEvent("onChange:" + s, function () {
                                    l = !0;
                                });
                        }
                    }
                    return t;
                },
                loadView: function (e) {
                    this.removeJFXENUnusedAttributesFromPropModel(this.propModel),
                    this.propUI.initData([this.propModel]),
                        this.startJFXENUnusedAttributeObserver(),
                        this.scheduleHideJFXENUnusedAttributeLines(),
                        this.propModel.clearChangedValue(),
                        this._resizeHandler(),
                        this.manageAccordion(this.propUI),
                        (this.enableNotif = !0),
                        this.focusFirstLine();
                },
                refresh: function () {
                    (this.enableNotif = !1),
                        this.removeJFXENUnusedAttributesFromPropModel(this.propModel),
                        this.propUI.addData([this.propModel]),
                        this.scheduleHideJFXENUnusedAttributeLines(),
                        this.manageAccordion(this.propUI),
                        (this.enableNotif = !0);
                },
                focusFirstLine: function () {
                    var e = this;
                    setTimeout(function () {
                        var t =
                            e.propUI.elements.formSection.elements.accordion.items[0].name;
                        e.propUI.elements.formSection.elements._organizers[t].focusLine(
                            0,
                            0
                        );
                    });
                },
                updateResizeHandler: function () {
                    var e = this.propUI.elements.formSection.elements.accordion.items[0];
                    e &&
                    this.propUI.elements.formSection.elements._organizers[
                        e.name
                        ]._forms[0].dispatchEvent("resetDisplay1");
                },
                manageAccordion: function (e) {
                    var t = e.elements.formSection.elements.accordion;
                    if (t) {
                        var n;
                        t._expanderItems[0] && t._expanderItems[0].elements.header.hide();
                        t._expanderItems[1] && (n = t._expanderItems[1]),
                        n &&
                        (!0 === n.expandedFlag
                            ? (n.header = c.collapseExtension)
                            : (n.header = c.expandExtension),
                            t.getContent().addEventListener("expand", function (e) {
                                if ("ExtensionsFacet" === e.options.expanderTarget.domId) {
                                    var t = c.collapseExtension;
                                    e.dsModel._expanderItems[e.options.expanderIndex].header =
                                        t;
                                }
                            }),
                            t.getContent().addEventListener("collapse", function (e) {
                                if ("ExtensionsFacet" === e.options.expanderTarget.domId) {
                                    var t = c.expandExtension;
                                    e.dsModel._expanderItems[e.options.expanderIndex].header =
                                        t;
                                }
                            }));
                    }
                },
                destroy: function () {
                    this.jfXENUnusedAttrObserver && (this.jfXENUnusedAttrObserver.disconnect(), this.jfXENUnusedAttrObserver = null);
                    console.log("ENONew destroy");
                },
                _addNotif: function (e) {
                    console.log(e),
                    this.enableNotif && this.dispatchEvent("onFacetNotification", [e]);
                },
                onSubmit: function (t) {
                    var n = this,
                        i = function (i) {
                            !0 === e.is(i, "array") &&
                            i.forEach(function (e) {
                                n._addNotif({
                                    customClass: e.customClass,
                                    eventID: e.eventID,
                                    msg: e.message,
                                });
                            }),
                                t(!1);
                        },
                        o = ((n = this), []);
                    this.propModel.getAttributeFromField().forEach(function (e) {
                        e.visible && o.push(e);
                    }),
                        this.propModel.lockModel(!0);
                    var r = a.validateAttributes(this.propModel, o);
                    r
                        ? i(r)
                        : this.validationFromService(function () {
                            var e = n._parseModel(n.data, n.propModel);
                            t(e);
                        }, i);
                },
                validationFromService: function (t, n) {
                    var i = this.attributesForValidationService,
                        o = [];
                    for (var a in i) {
                        var r = i[a],
                            s = this.propModel.get(r);
                        if (s && s.visible) {
                            var c = new e.Promise(function (e, t) {
                                var n = s.validationService,
                                    i = { name: s.name, value: s.valueDB };
                                require([n], function (t) {
                                    t.validate(i, function (t) {
                                        e(t);
                                    });
                                });
                            });
                            o.push(c);
                        }
                    }
                    e.Promise.all(o)
                        .then(function (e) {
                            console.log(e);
                            var i = [];
                            for (var o in e) {
                                var a = e[o];
                                a && i.push(a);
                            }
                            0 == i.length ? t() : n(i);
                        })
                        .catch(function (e) {
                            console.log(
                                "vma validation service failed, proceeding for object creation"
                            ),
                                t();
                        });
                },
                _buildAttributesModel: function (e) {
                    var t = this,
                        n = {},
                        i = [],
                        o = e.defaultAttributes,
                        a = [];
                    for (var c in o) {
                        var l = o[c];
                        a.push(l);
                    }
                    if (a.length > 0) {
                        var d = r._buildAttributesModel(a);
                        i = i.concat(d);
                    }
                    var u = e[s.ATTRIBUTES],
                        f = e[s.INTERFACES];
                    if (u) {
                        var p = u[s.PUBLIC_ATTRIBUTES];
                        if (p) {
                            d = r._buildAttributesModel(p, f);
                            i = i.concat(d);
                        }
                        var h = u[s.EXTN_ATTRIBUTES];
                        if (h) {
                            d = r._buildAttributesModel(h, f);
                            i = i.concat(d);
                        }
                        var v = u[s.VOLATILE_ATTRIBUTES];
                        if (v) {
                            d = r._buildAttributesModel(v, f, !0);
                            i = i.concat(d);
                        }
                        for (var m in i) {
                            var g = i[m];
                            if (this.persistAttribute[g.getIdentifier()]) {
                                var N = this.persistAttribute[g.getIdentifier()];
                                g.set("value", N.value),
                                    g.set("valueDB", N.valueDB),
                                    g.set("isModified", !0);
                            }
                            var w = g.visibility;
                            if (w)
                                for (var c in w) {
                                    var E = {
                                        attrID: g.getIdentifier(),
                                        list: w[c],
                                        eventID: "onChange:" + c,
                                        callback: function (e, n) {
                                            var i = this;
                                            console.log("onchange " + n.valueDB);
                                            var o = n.valueDB,
                                                a = !1;
                                            i.list.includes(o) && (a = !0),
                                                setTimeout(function () {
                                                    t.addAttributeOnUI(i.attrID, a);
                                                }, 0);
                                        },
                                    };
                                    this.customEvents.push(E);
                                }
                            (n[g.getIdentifier()] = g),
                            g.validationService &&
                            this.attributesForValidationService.push(g.getIdentifier());
                        }
                    }
                    return n;
                },
                addAttributeOnUI: function (e, t) {
                    var n = this.propModel;
                    if (
                        n &&
                        n.get(e) &&
                        ((n.get(e).visible = t),
                            this.propUI.elements.formSection.toggleFormOrgLine(e, t),
                        "3DPartTemplate" === e)
                    ) {
                        var i = this.buttons.Close;
                        i && (i.visibleFlag = t);
                    }
                },
                _parseModel: function (e, t) {
                    if (t && e) {
                        var n = {},
                            i = t.getAttributeFromField(),
                            o = e.attributes,
                            a = e.defaultAttributes;
                        for (attrId in i)
                            if (((attrModel = i[attrId]), attrModel.isModified)) {
                                var s = attrModel.name,
                                    c = null,
                                    l = !1;
                                if (attrModel.defaultAttribute)
                                    for (var d in a)
                                        (c = a[d]).name === s &&
                                        r._parseAttributeModels(c, attrModel);
                                else if (o) {
                                    var u = function (e, t) {
                                        for (ind in t)
                                            if (((c = t[ind]).name === e && (l = !0), l)) break;
                                    };
                                    for (AttrGrp in o) {
                                        if ((u(s, o[AttrGrp]), l)) break;
                                        c = null;
                                    }
                                    null != c && r._parseAttributeModels(c, attrModel);
                                }
                                if (attrModel.isUIModified) {
                                    var f = {
                                        name: attrModel.name,
                                        value: attrModel.value,
                                        valueDB: attrModel.valueDB,
                                    };
                                    this.persistAttribute[attrModel.getIdentifier()] = f;
                                }
                            }
                        (n.attributes = o), (n.defaultAttributes = a);
                        var p = e.interfaces;
                        return p && (n.interfaces = p), n;
                    }
                },
                getUI: function () {
                    return this.propUI;
                },
                getContentSize: function () {
                    var e = this.propUI.getContent();
                    if (e) return e.getSize();
                },
                _resizeHandler: function () {
                    var e = this;
                    this.propUI.elements.formSection.elements.container.addEvent(
                        "resize",
                        function () {
                            (e.isLoaded = !0), e.updateResizeHandler();
                            var t = e.getContentSize();
                            e.dispatchEvent("onResize", [t]);
                        }
                    );
                },
            });
        }
    ),
    define(
        "DS/ENONewWidget/ENONew/View/Facet/ENOListViewFacet",
        [
            "UWA/Core",
            "DS/UIKIT/Scroller",
            "DS/ENONewWidget/ENONew/View/Facet/ENONewFacetBase",
            "DS/ENONewWidget/ENONew/View/ENOListView",
            "DS/ENONewWidget/ENONew/View/Facet/ENONewFacetHelper",
        ],
        function (e, t, n, i, o) {
            return n.extend({
                init: function (t) {
                    var n = this;
                    this._parent(t),
                        console.log("vma12 : Create facet init"),
                        (this.skeleton = e.createElement("div", {
                            class: "CreateFacet_container",
                        }));
                    var o = t.events.onResize;
                    t.events.onResize = function (e) {
                        o("ENOCreate", e);
                    };
                    var a = t.events.onChangeAttribute;
                    (t.events.onChangeAttribute = (e) => {
                        for (var t in e)
                            if ("New-SecurityContext" === t) {
                                var i = e[t];
                                n.publishInfo("currentSecurityContext", {
                                    value: i.valueDB,
                                    valueNLS: i.value,
                                });
                            }
                        a(e);
                    }),
                        (this.listview = new i(t)),
                        this.listview.propUI.inject(this.skeleton),
                        this.listview.buildModel(t.data),
                        this.listview.loadView();
                },
                updateFacet: function (e) {
                    this.listview.buildModel(e.data), this.listview.loadView();
                },
                _getUI: function () {
                    return this.skeleton;
                },
                getUI: function () {
                    return this.skeleton;
                },
                onSubmit: function (e, t) {
                    this.listview.onSubmit(t);
                },
                onComplete: function (t) {
                    return new e.Promise(function (e) {
                        e({ name: o.getDefaultFacetName() });
                    });
                },
                destroy: function () {
                    this.listview.destroy(), this._parent();
                },
            });
        }
    ),
    define(
        "DS/ENONewWidget/utils/CredentialList",
        ["DS/ENONewWidget/ajaxRequests/WebReq"],
        function (e) {
            "use strict";
            const t = (e) =>
                e.reduce(function (e, n) {
                    return e.concat(Array.isArray(n) ? t(n) : n);
                }, []);
            return {
                loadCredentials: (n) =>
                    new UWA.Class.Promise((i, o) => {
                        e.get("/resources/modeler/pno/person", {
                            tenant: n,
                            params: {
                                current: "true",
                                select: "collabspaces&select=preferredcredentials",
                            },
                        }).then((e) =>
                            i(
                                ((e) => {
                                    let n = [];
                                    if (!UWA.is(e.collabspaces, "array")) return n;
                                    let i =
                                        [
                                            ...new Set(
                                                t(
                                                    e.collabspaces.map((e) =>
                                                        e.couples.map((e) => e.organization.name)
                                                    )
                                                )
                                            ),
                                        ].length > 1;
                                    return (
                                        e.collabspaces.forEach((e) => {
                                            e.couples.forEach((t) => {
                                                var o =
                                                        null == t.role.nls || "" == t.role.nls
                                                            ? t.role.name
                                                            : t.role.nls,
                                                    a = t.role.name,
                                                    r = t.organization.name,
                                                    s = t.organization.title,
                                                    c = e.name,
                                                    l = e.title ? e.title : e.name;
                                                let d = i ? l + " ● " + s + " ● " + o : l + " ● " + o,
                                                    u = "ctx::" + a + "." + r + "." + c;
                                                n.push({ displayName: d, ctx: u });
                                            });
                                        }),
                                            n.sort((e, t) =>
                                                e.displayName.localeCompare(t.displayName, void 0, {
                                                    numeric: !0,
                                                    sensitivity: "base",
                                                })
                                            )
                                    );
                                })(e)
                            )
                        );
                    }),
                filterCredentials: (t, n, i) =>
                    new UWA.Class.Promise((o, a) => {
                        e.post(
                            "/resources/lifecycle/util/filterValidSCs",
                            ((e, t) => {
                                let n = { Command: t, SecurityContexts: e.map((e) => e.ctx) };
                                return JSON.stringify(n);
                            })(n, i),
                            { tenant: t }
                        )
                            .then((e) => {
                                e.status && "success" === e.status
                                    ? o(
                                        ((e) =>
                                            e.filter(
                                                (e) =>
                                                    !((e) => {
                                                        let t = e.match(/(ctx::)([^.]+)/);
                                                        return t.length > 2 && "VPLMAdmin" === t[2];
                                                    })(e)
                                            ))(e.data)
                                    )
                                    : a(
                                        e.error
                                            ? e.error
                                            : "/resources/lifecycle/util/filterValidSCs - failed!"
                                    );
                            })
                            .catch(a);
                    }),
            };
        }
    ),
    define(
        "DS/ENONewWidget/ENONew/View/ENOWizardView",
        [
            "UWA/Class/Events",
            "UWA/Class/Promise",
            "UWA/Controls/Abstract",
            "UWA/Core",
            "DS/EditPropWidget/models/OccurrenceModel",
            "DS/ENONewWidget/ENONew/View/Facet/ENONewFacetHelper",
        ],
        function (e, t, n, i, o, a) {
            return n.extend(e, {
                name: "DS/ENONewWidget/ENONew/View/facet/wizardView",
                isLoaded: null,
                init: function (e) {
                    var t = this;
                    for (var n in ((this.options = e),
                        t._parent(e),
                        (t.navigator = { container: null, toggler: null }),
                        (t.content = { container: null }),
                        (t.activeView = ""),
                        (t.activeMode = "multi-step"),
                        (this._sharedOptions = {}),
                        (this.facets = e.facets),
                        e.events))
                        this.addEvent(n, e.events[n]);
                    var r = new o();
                    this.setContext(r),
                        (r.subscribeInfo = function (e, t) {
                            var n = t === a.getDefaultFacetName() ? "" : t || "";
                            r.addEvent("publishinfo." + n, e);
                        }),
                        (r.setValidation = function () {
                            t.dispatchEvent("refreshValidationStatus");
                        }),
                        t.setupDefaultFacet(),
                        t.addEvent("onTabChange", e.events.onTabChange),
                        (this.skeleton = i.createElement("div", {
                            class: "create-view-body multi-step",
                            styles: { width: "100%", height: "100%" },
                        }));
                },
                setupDefaultFacet: function () {
                    var e = this,
                        t = this._getFacetFromName(a.getDefaultFacetName());
                    t.options.events = {};
                    var n = !0;
                    (t.options.events.onResize = function (t) {
                        if (n) {
                            n = !1;
                            var i = e.navigator.container.getSize(),
                                o = { height: t.height + i.height + 8, width: i.width };
                            e.dispatchEvent("onResize", [o]), e.setRopeLength(e.activeView);
                        }
                        e.isLoaded = !0;
                    }),
                        (t.options.events.onChangeAttribute = function (t) {
                            e.dispatchEvent("onChangeAttribute", [t]);
                        }),
                        (t.options.events.onEnterKeyDown = function () {
                            e.dispatchEvent("onEnterKeyDown");
                        }),
                        (t.options.events.onFacetNotification = function (t) {
                            e.dispatchEvent("onFacetNotification", [t]);
                        });
                },
                loadView: function (e) {
                    var t = this;
                    (t.navigator.container = i
                        .createElement("nav", { class: "create-view-navigator-container" })
                        .inject(t.skeleton)),
                        i
                            .createElement("div", {
                                class: "evolve-container",
                                html: [
                                    i.createElement("div", { class: "evolve-rope-placeholder" }),
                                    i.createElement("div", { class: "evolve-rope" }),
                                ],
                            })
                            .inject(t.navigator.container);
                    var n = i
                        .createElement("nav", { class: "create-view-navigator" })
                        .inject(t.navigator.container);
                    t.facets.forEach(function (e) {
                        if (e.facet) {
                            var i = t._tabItem(e.facet);
                            (e.navItem = i), i.container.inject(n);
                        }
                    }),
                        (t.content.container = i
                            .createElement("div", { class: "create-view-content" })
                            .inject(t.skeleton)),
                        (t.facetContainer = i
                            .createElement("div", {
                                class: "wizardFacetContainer",
                                styles: { width: "100%", height: "100%" },
                            })
                            .inject(t.content.container)),
                        t.addEvent("facetLoaded", (t) => {
                            t && a.getDefaultFacetName() === t.facet.name && e();
                        }),
                        t.switchView(a.getDefaultFacetName());
                },
                getContext: function () {
                    return this._sharedContext;
                },
                setContext: function (e) {
                    this._sharedContext = e;
                },
                refreshView: function () {
                    this.switchView(a.getDefaultFacetName());
                },
                _tabItem: function (e) {
                    var t = this,
                        n = {};
                    return (
                        (n.container = i.createElement("a", {
                            id: e.name,
                            class: "nav-item",
                        })),
                            n.container.addEventListener("dragover", function () {
                                t.switchView(e.name);
                            }),
                            (n.activeIndicator = i
                                .createElement("div", { class: "nav-item-active-indicator" })
                                .inject(n.container)),
                            i
                                .createElement("span", { class: "fonticon fonticon-expand-down" })
                                .inject(n.activeIndicator),
                            (n.circle = i
                                .createElement("div", { class: "nav-item-circle" })
                                .inject(n.container)),
                            i
                                .createElement("span", { class: "fonticon fonticon-" + e.icon })
                                .inject(n.circle),
                            (n.label = i
                                .createElement("div", { class: "nav-item-label", html: e.text })
                                .inject(n.container)),
                            n
                    );
                },
                switchView: function (e) {
                    var t = this,
                        n = this.activeView;
                    if (t.navigator && t.navigator.container && e) {
                        var i = t.navigator.container.getElements("a"),
                            o = !1;
                        i.forEach(function (i) {
                            !1 === o
                                ? i.removeClassName("filled").addClassName("filled")
                                : i.removeClassName("filled"),
                            i.id === n && i.removeClassName("active"),
                            i.id === e &&
                            (i.addClassName("active"), t.setRopeLength(e), (o = !0));
                        });
                    }
                    var a = this._getFacetFromName(n);
                    a && a.content && a.content.hide();
                    var r = this._getFacetFromName(e);
                    r && r.content
                        ? r.content.show()
                        : t._loadFacet(r, !0, function () {
                            r.content && r.content.setContext(this._sharedContext),
                                console.log("Done loading facet");
                        }),
                        (t.activeView = e),
                        t.dispatchEvent("onTabChange", [r]);
                },
                setRopeLength: function (e) {
                    if (e) {
                        var t = this._getFacetFromName(e).navItem.circle;
                        if (t) {
                            var n = t.getDimensions().outerWidth,
                                i =
                                    ((t.getPosition().x - 10 + n) /
                                        this.navigator.container.getSize().width) *
                                    100;
                            this.navigator.container
                                .getElement(".evolve-rope")
                                .setStyles({ width: i + "%", height: "2px" });
                        }
                    }
                },
                loadFacet: function (e, t, n) {
                    this._loadFacet(e, t, n);
                },
                _loadFacet: function (e, t, n) {
                    var i = this;
                    if (e && "loading" !== e.content)
                        if (e.content) n.call(i);
                        else {
                            e.content = "loading";
                            var o = ++window.top.perfoIncrem;
                            window.top.performance.mark("start_prereqFacet:" + o),
                                require([e.require], function (a) {
                                    window.top.performance.mark("end_prereqFacet:" + o),
                                        (e.content = null);
                                    var r = i.activeView,
                                        s = e.facet.name === r;
                                    if (!t || s) {
                                        var c = e.options ? e.options : {},
                                            l = c.events ? c.events : {};
                                        (c.events = l),
                                            (e.options = c),
                                            (e.options.tabId = e.facet.name),
                                            (e.options.eventThread =
                                                i.options.eventThread + "." + e.facet.name),
                                            (e.content = new a(e.options, i._sharedOptions));
                                        var d = e.content.getUI(),
                                            u =
                                                "function" == typeof d.addClassName
                                                    ? d
                                                    : d.container
                                                        ? d.container
                                                        : d.elements
                                                            ? d.elements.container
                                                            : null;
                                        u &&
                                        u.addClassName(
                                            "ENONew_wizard_Container ENONew_wizard_" + e.facet.name
                                        ),
                                            e.content.getUI().inject(i.facetContainer),
                                        s || e.content.hide(),
                                            i.dispatchEvent("facetLoaded", e),
                                            n.call(i);
                                    }
                                });
                        }
                },
                _getFacetFromName: function (e) {
                    return this.facets.find((t) => t.facet.name === e);
                },
                _getFacetIndexFromName: function (e) {
                    return this.facets.findIndex((t) => t.facet.name === e);
                },
                getActiveFacet: function () {
                    return this._getFacetFromName(this.activeView);
                },
                isFirstFacet: function (e) {
                    return 0 === this._getFacetIndexFromName(e);
                },
                isLastFacet: function (e) {
                    return this._getFacetIndexFromName(e) === this.facets.length - 1;
                },
                switchToRelativeFacet: function (e) {
                    var t = this.activeView,
                        n = this._getFacetIndexFromName(t),
                        i = this.facets[n + e];
                    this.switchView(i.facet.name);
                },
            });
        }
    ),
    define(
        "DS/ENONewWidget/ENONew/View/ENOTabView",
        [
            "UWA/Core",
            "UWA/Controls/Abstract",
            "DS/EditPropWidget/constants/EditPropConstants",
            "DS/EditPropWidget/UI/IDCardSkeleton",
            "DS/EditPropWidget/models/OccurrenceModel",
            "DS/ENONewWidget/ENONew/View/Facet/ENONewFacetHelper",
        ],
        function (e, t, n, i, o, a) {
            return t.extend({
                init: function (t) {
                    (this.opts = t),
                        this._parent(t),
                        (this.sharedInfo = {}),
                        (this.skeleton = e.createElement("div", {
                            class: "ENONewTab_container",
                            styles: { width: "100%", height: "100%" },
                        })),
                        (this.facets = t.facets),
                        this.setupDefaultFacet();
                },
                setupDefaultFacet: function () {
                    var e = this,
                        t = this._getFacetFromName(a.getDefaultFacetName());
                    t.options.events = {};
                    (t.options.events.onResize = function (t, n) {
                        if (
                            a.getDefaultFacetName() === t &&
                            e.idCardSkeleton.currentFacetName === t
                        ) {
                            var i = {
                                height:
                                    n.height +
                                    e.idCardSkeleton.elements.tabContainer.getSize().height +
                                    14,
                                width: n.width,
                            };
                            e.dispatchEvent("onResize", [i]), (e.isLoaded = !0);
                        }
                    }),
                        (t.options.events.onChangeAttribute = function (t) {
                            e.dispatchEvent("onChangeAttribute", [t]);
                        }),
                        (t.options.events.onEnterKeyDown = function () {
                            e.dispatchEvent("onEnterKeyDown");
                        }),
                        (t.options.events.onFacetNotification = function (t) {
                            e.dispatchEvent("onFacetNotification", [t]);
                        });
                },
                _getFacetFromName: function (e) {
                    return this.facets.find((t) => t.facet.name === e);
                },
                buildModel: function (e) {
                    console.log(e);
                },
                loadView: function (e) {
                    var t = this,
                        r = {
                            name: "ENONew tab_content",
                            className: "ENONew_TabView_MasterContain",
                            typeOfDisplay: n.ONLY_EDIT_PROPERTIES,
                            selectionType: n.NO_SELECTION,
                            readOnly: !1,
                            extraNotif: !1,
                            showIdCard: !1,
                            closeButton: !1,
                            showTopBar: !1,
                        };
                    (r.sharedContext = new o()),
                        (r.sharedContext.addInfo = function (e, n, i) {
                            if (e && n) {
                                t.sharedInfo[e] || (t.sharedInfo[e] = {}),
                                    (t.sharedInfo[e][n] = i);
                                var o = e === a.getDefaultFacetName() ? "" : e;
                                r.sharedContext.dispatchEvent("publishinfo." + o, [n, i]);
                            }
                        }),
                        (r.sharedContext.getInfo = function (e, n) {
                            if (e)
                                return n || (n = a.getDefaultFacetName()), t.sharedInfo[n][e];
                        }),
                        (r.sharedContext.subscribeInfo = function (e, t) {
                            var n = t === a.getDefaultFacetName() ? "" : t || "";
                            r.sharedContext.addEvent("publishinfo." + n, e);
                        }),
                        (r.sharedContext.setValidation = function () {
                            t.dispatchEvent("refreshValidationStatus");
                        }),
                        (this.idCardSkeleton = new i(this.facets, r)),
                        this.idCardSkeleton.elements.container.inject(this.skeleton),
                        this.idCardSkeleton.subscribeEvt("facetLoaded", (t) => {
                            t && a.getDefaultFacetName() === t.facet.name && e();
                        }),
                        this.idCardSkeleton._manageFacetVisibility(),
                        this.idCardSkeleton.elements.tabs.addEventListener(
                            "tabButtonClick",
                            function (e) {
                                var n = e.options.value,
                                    i = t._getFacetFromName(n);
                                t.dispatchEvent("onTabChange", [i]);
                            }
                        );
                },
                loadFacet: function (e, t, n) {
                    this.idCardSkeleton._loadFacet(e, t, n);
                },
                refreshView: function () {
                    this.idCardSkeleton.selectFacet(a.getDefaultFacetName());
                },
                getUI: function () {
                    return this._getUI();
                },
                _getUI: function () {
                    return this.skeleton;
                },
            });
        }
    ),
    define(
        "DS/ENONewWidget/ENONew/Controller/ENONewDialogController",
        [
            "UWA/Controls/Abstract",
            "DS/Windows/ImmersiveFrame",
            "DS/Controls/ComboBox",
            "DS/Controls/Button",
            "DS/Windows/Dialog",
            "DS/Controls/ModalLoader",
            "DS/ENONewWidget/ENONew/Events",
            "DS/ENONewWidget/ENONew/View/ENOListView",
            "DS/ENONewWidget/ENONew/View/ENOTabView",
            "DS/ENONewWidget/ENONew/View/ENOWizardView",
            "DS/ENONewWidget/ENONew/View/Facet/ENONewFacetHelper",
            "i18n!DS/ENONewWidget/assets/nls/ENONewWidgetNls",
        ],
        function (e, t, n, i, o, a, r, s, c, l, d, u) {
            return e.extend({
                init: function (e) {
                    this._parent(e),
                        (this.immersiveFrame = new t({})),
                        this.immersiveFrame.inject(widget.body),
                        (this.userPrefs = {}),
                        (this.footerButtons = {});
                },
                setup: function (e, t, n, i, o) {
                    var a = this;
                    for (eventID in ((a.dialogContainer = UWA.createElement("div", {
                        class: "ENONewInfra DialogContainer",
                    })),
                        (a.elements.dialogContainer = a.dialogContainer),
                        (i = i || {}))) {
                        var r = i[eventID];
                        this.addEvent(eventID, r);
                    }
                    if (((a.ServiceCallback = o), (this.viewType = "listView"), t)) {
                        (a.prefs = e.prefs),
                        t.facets &&
                        t.facets.length &&
                        t.viewType &&
                        ((this.viewType = t.viewType),
                            (this.facets = d.getFacetList(e, t.facets, {
                                securityContext: t.securityContext,
                                buttons: a.footerButtons,
                            }))),
                            (this.identifier = t.persistId ? t.persistId : "ENONew_Id");
                        var s = a.getDefaultButtons(this.viewType, t);
                        s && Object.assign(a.footerButtons, s),
                        n && Object.assign(a.footerButtons, n);
                        var c = a.buildContent(e, t),
                            l = a.buildToolbar(t, e.prefs);
                        UWA.Promise.all([l, c])
                            .then(function (e) {
                                var n = UWA.createElement("div", {
                                    class: "ENONewInfra ToolbarContainer",
                                });
                                n.inject(a.dialogContainer),
                                    (a.elements.toolbarContainer = n),
                                    (a.toolbar = e[0]),
                                a.toolbar && a.toolbar.inject(n);
                                var i = UWA.createElement("div", {
                                    class: "ENONewInfra ContentContainer",
                                });
                                i.inject(a.dialogContainer),
                                    (a.elements.contentContainer = i),
                                    e[1].skeleton.inject(i);
                                var o = {
                                    identifier: a.identifier,
                                    title: t.title ? t.title : u.CreateNew,
                                    modalFlag: t.overlay,
                                    content: a.dialogContainer,
                                    width: 580,
                                    position: {
                                        my: "top",
                                        at: "top",
                                        offsetY: 90,
                                        collision: "fit",
                                    },
                                    checkboxInfos: {
                                        label: u.get("repeatCheckbox"),
                                        checkFlag: t.activateCreateAnother,
                                    },
                                    buttons: a.footerButtons,
                                    escapeButton: a.footerButtons.Discard,
                                };
                                (a.myDialog = a.buildDialog(o)),
                                    a.manageButtons(),
                                t.activateDraftMode &&
                                t.activateDraftMode.show &&
                                ((a.footerButtons.createBtnCombo = a.buildDraftModeButton({
                                    preference: t.activateDraftMode.preference,
                                    optionsList: a.prefs ? a.prefs.createBtn : [],
                                })),
                                a.footerButtons.createBtnCombo &&
                                (a.footerButtons.Ok.hide(),
                                    a.footerButtons.createBtnCombo.inject(
                                        a.myDialog.elements._buttonsDiv,
                                        "top"
                                    )));
                            })
                            .catch(function (e) {
                                console.log(e);
                            });
                    }
                },
                buildDialog: function (e) {
                    var t = this;
                    (e.resizableFlag = !0),
                        (e.immersiveFrame = this.immersiveFrame),
                        (e.activeFlag = !0);
                    var n = new o(e);
                    return (
                        n.addEventListener("close", function () {
                            t.dispatchEvent("onClose");
                        }),
                            (this.disableBtnClickOnEnter = !1),
                            n.addEventListener("keydown", function (e) {
                                13 === e.keyCode
                                    ? (t.disableBtnClickOnEnter = !0)
                                    : 27 === e.keyCode && (t.onEscKeyDown = !0);
                            }),
                            n.addEventListener("keyup", function (e) {
                                13 === e.keyCode
                                    ? ((t.onEnterKeyDown = !1), (t.disableBtnClickOnEnter = !1))
                                    : 27 === e.keyCode &&
                                    ((t.onEscKeyDown = !1), t.dispatchEvent("onClose"));
                            }),
                            n
                    );
                },
                buildContent: function (e, t, n) {
                    var i = this,
                        o = !0;
                    return new UWA.Promise(function (t) {
                        if ("listView" === i.viewType)
                            (i.View = new s({
                                identifier: i.identifier,
                                events: {
                                    onChangeAttribute: function (e) {
                                        i.dispatchEvent("onChangeAttribute", [e]);
                                    },
                                    onFacetNotification: function (e) {
                                        i._addNotif(e);
                                    },
                                    onEnterKeyDown: function () {
                                        i.onEnterKeyDown = !0;
                                    },
                                    onResize: function (e) {
                                        o &&
                                        ((o = !1),
                                            i.View.focusFirstLine(),
                                            setTimeout(function () {
                                                (i.myDialog.minHeight =
                                                    i.myDialog.elements._footerDiv.offsetHeight + 80),
                                                    r.dispatchEvent("onCreationDialogAction", [
                                                        { action: "ready" },
                                                    ]);
                                            }));
                                    },
                                },
                                buttons: i.footerButtons,
                            })),
                                i.View.buildModel(e),
                                i.View.loadView(),
                                t(i.View);
                        else {
                            var n = {
                                identifier: i.identifier,
                                facets: i.facets,
                                buttons: i.footerButtons,
                                events: {
                                    onTabChange: function (e) {
                                        i.manageButtons(e);
                                    },
                                    onChangeAttribute: function (e) {
                                        i.dispatchEvent("onChangeAttribute", [e]);
                                    },
                                    onFacetNotification: function (e) {
                                        i._addNotif(e);
                                    },
                                    onEnterKeyDown: function () {
                                        i.onEnterKeyDown = !0;
                                    },
                                    onResize: function (e) {
                                        o &&
                                        ((o = !1),
                                            setTimeout(function () {
                                                (i.myDialog.minHeight =
                                                    i.myDialog.elements._footerDiv.offsetHeight + 80),
                                                    r.dispatchEvent("onCreationDialogAction", [
                                                        { action: "ready" },
                                                    ]);
                                            })),
                                            i.myDialog._getDimensionsFromPreferences();
                                    },
                                    refreshValidationStatus: function () {
                                        i.evaluateValidationStatus();
                                    },
                                },
                            };
                            "tabView" === i.viewType
                                ? (i.View = new c(n))
                                : "wizardView" === i.viewType && (i.View = new l(n)),
                                i.View.loadView(() => {
                                    t(i.View);
                                });
                        }
                    });
                },
                buildToolbar: function (e, t) {
                    var n = this;
                    return new UWA.Promise(function (i) {
                        if (e && e.activatelock && e.activatelock.show) {
                            var o = !1;
                            function a(e) {
                                return "LOCK" === e
                                    ? {
                                        class: "ENONew-lock-class",
                                        icon: "key",
                                        label: u.get("locked"),
                                    }
                                    : {
                                        class: "ENONew-lock-open-class",
                                        icon: "lock-open",
                                        label: u.get("unlocked"),
                                    };
                            }
                            e.activatelock.preference
                                ? (o = e.activatelock.preference)
                                : t && t.lockState && (o = t.lockState),
                                (n.userPrefs.isLock = o),
                                require(["DS/Tweakers/GeneratedToolbar"], function (e) {
                                    let t = a(o);
                                    var r = {
                                            entries: [
                                                {
                                                    id: "toolbarLock_Btn",
                                                    onNodeUpdate: function () {},
                                                    dataElements: {
                                                        typeRepresentation: "function",
                                                        icon: {
                                                            iconName: t.icon,
                                                            fontIconFamily: 1,
                                                            className: t.class,
                                                        },
                                                        position: "far",
                                                        category: "2",
                                                        label: t.label,
                                                        value: function (e) {
                                                            let t =
                                                                    "key" ===
                                                                    e.context.nodeModel.getAttributeValue(
                                                                        "semantics"
                                                                    ).icon.iconName
                                                                        ? "UNLOCK"
                                                                        : "LOCK",
                                                                i = a(t);
                                                            (n.userPrefs.isLock = t),
                                                                e.context.nodeModel.setLabel(i.label),
                                                                e.context.toolbar.updateNodeModel(
                                                                    "toolbarLock_Btn",
                                                                    {
                                                                        grid: {
                                                                            semantics: {
                                                                                icon: {
                                                                                    iconName: i.icon,
                                                                                    fontIconFamily: 1,
                                                                                    className: i.class,
                                                                                },
                                                                            },
                                                                        },
                                                                    }
                                                                );
                                                        },
                                                    },
                                                },
                                            ],
                                        },
                                        s = e.prototype.createTreeDocument(r);
                                    s.onNodeModelUpdate(function (e) {
                                        if (
                                            "lock_btn" === e.data.nodeModel._ID &&
                                            e.data.attributes.hasOwnProperty("data")
                                        ) {
                                            var t = a(e.data.nodeModel.getAttributeValue("data"));
                                            e.target.setLabel(t.label);
                                        }
                                    });
                                    var c = new e({
                                        executionContext: s,
                                        overflowManagement: "dropdown",
                                        direction: "horizontal",
                                        items: s,
                                    });
                                    i(c);
                                });
                        } else i();
                    });
                },
                showDialog: function () {
                    this.myDialog.show();
                },
                addLoadingOverlay(e) {
                    this.myDialog &&
                    a.displayModalLoader(this.myDialog.elements._contentDiv, e);
                },
                removeLoadingOverlay(e) {
                    this.myDialog &&
                    a.removeModalLoader(this.myDialog.elements._contentDiv, e);
                },
                reloadContent: function (e) {
                    if ("listView" === this.viewType)
                        this.View.buildModel(e), this.View.loadView();
                    else {
                        this.View.refreshView();
                        var t = d.getDefaultFacetName();
                        this.View.facets.forEach(function (n) {
                            if (n.content && UWA.is(n.content.updateFacet, "function")) {
                                var i = n.options;
                                e && n.facet.name === t && (i.data = e),
                                    n.content.updateFacet(i);
                            }
                        });
                    }
                },
                onSubmitDialog: function (e) {
                    var t = this,
                        n = function (n) {
                            n
                                ? ("create" !== e && "createClose" !== e) ||
                                (t.ServiceCallback &&
                                    "function" == typeof t.ServiceCallback &&
                                    t.ServiceCallback(
                                        n,
                                        { eventID: e },
                                        t.getUserPreferences()
                                    ))
                                : t.resetButtons();
                        };
                    "listView" === this.viewType
                        ? this.View.onSubmit(n)
                        : this.onSubmit(e, n);
                },
                getUserPreferences: function () {
                    return {
                        toolbarIsLockEnabled: this.userPrefs
                            ? this.userPrefs.isLock
                            : void 0,
                        footerCreateBtnState: this.userPrefs
                            ? this.userPrefs.createBtn
                            : void 0,
                    };
                },
                _addNotif: function (e) {
                    var t = e.msg,
                        n = e.eventID,
                        i = e.eventID;
                    switch (n) {
                        case "error":
                            (t = t || e.json), this._addError(t, i);
                            break;
                        case "warning":
                            this._addWarning(t, i);
                            break;
                        case "success":
                            this._addSuccess(t, i);
                            break;
                        default:
                            this._addInfo(t, i);
                    }
                },
                _addInfo: function (e, t) {
                    var n = this;
                    this._getErrorManager(function (i) {
                        var o = i.addInfo(e, t);
                        n.dispatchEvent("onNotification", o);
                    });
                },
                _addWarning: function (e, t) {
                    var n = this;
                    this._getErrorManager(function (i) {
                        var o = i.addWarning(e, t);
                        n.dispatchEvent("onNotification", o);
                    });
                },
                _addSuccess: function (e, t) {
                    var n = this;
                    this._getErrorManager(function (i) {
                        var o = i.addSuccess(e, t);
                        n.dispatchEvent("onNotification", o);
                    });
                },
                _addError: function (e, t) {
                    var n = this;
                    this._getErrorManager(function (i) {
                        var o = i.addError(e, t);
                        n.dispatchEvent("onNotification", o);
                    });
                },
                _getErrorManager: function (e) {
                    var t = this;
                    this.errorOverlay
                        ? e.call(null, this.errorOverlay)
                        : require(["DS/ErrorWidget/ErrorWidget"], function (n) {
                            t.errorOverlay ||
                            (t.errorOverlay = new n(
                                widget,
                                t.myDialog.elements.container,
                                { className: "newcontent_errorManager" }
                            )),
                                e.call(null, t.errorOverlay);
                        });
                },
                getDefaultButtons: function (e, t) {
                    var n = this,
                        i = {},
                        o = [];
                    return (
                        "listView" === e || "tabView" === e
                            ? (o = ["Ok", "Discard"])
                            : "wizardView" === e &&
                            (o = ["Ok", "Reset", "RestoreDefaults", "Discard"]),
                            o.forEach(function (e) {
                                i[e] = n.buildButton(e, t);
                            }),
                            i
                    );
                },
                buildDraftModeButton(e) {
                    var t,
                        i = this;
                    if (e && e.optionsList && e.optionsList.length) {
                        var o = [],
                            a = "";
                        e.optionsList.forEach((t) => {
                            o.push(t.nls), t.name === e.preference && (a = t.nls);
                        }),
                            (i.userPrefs.createBtn = e.preference),
                            (t = new n({
                                name: "createClose",
                                emphasize: "primary",
                                enableSearchFlag: !1,
                                actionOnClickFlag: !0,
                                elementsList: o,
                                value: a,
                            })).addEventListener("click", function (n) {
                                if (
                                    !i.disableBtnClickOnEnter ||
                                    (i.disableBtnClickOnEnter && i.onEnterKeyDown)
                                ) {
                                    if (
                                        i.disableBtnClickOnEnter &&
                                        i.onEnterKeyDown &&
                                        "wizardView" === i.viewType
                                    )
                                        return void i.footerButtons.RestoreDefaults.onClick();
                                    (i.userPrefs.createBtn = e.optionsList[t.selectedIndex].name),
                                        (this.permanentDisable = !0),
                                        i.disableFormSubmit(!0),
                                        (n.dsModel.label = u.creatingButton),
                                        i.myDialog.checkboxInfos &&
                                        i.myDialog.checkboxInfos.checkFlag
                                            ? i.onSubmitDialog("create")
                                            : i.onSubmitDialog("createClose");
                                }
                            }),
                            t.elements.mainElement.addClassName("wux-ui-state-primary"),
                            t.elements.expander.addClassName("wux-ui-state-primary"),
                            t.elements.expander.setStyle("margin-left", 0);
                    }
                    return t;
                },
                manageButtons: function (e) {
                    if ("wizardView" === this.viewType && this.View) {
                        var t = this.View.getActiveFacet(),
                            n = this.View.isFirstFacet(t.facet.name),
                            i = this.View.isLastFacet(t.facet.name);
                        (this.footerButtons.Reset.visibleFlag = !n),
                            (this.footerButtons.RestoreDefaults.visibleFlag = !i),
                            (this.footerButtons.Ok.visibleFlag = i);
                    }
                },
                evaluateValidationStatus: function () {
                    for (var e = !0, t = 0; t < this.View.facets.length; t++) {
                        var n = this.View.facets[t];
                        if (n.content && !n.content.facetValidation) {
                            e = !1;
                            break;
                        }
                    }
                    e
                        ? this.resetButtons()
                        : ((this.permanentDisable = !0), this.disableFormSubmit(!0));
                },
                buildButton: function (e, t) {
                    var n,
                        o = this;
                    switch (e) {
                        case "Apply":
                            n = new i({
                                id: "create",
                                label: u.createBtn,
                                onClick: function (e) {
                                    (this.permanentDisable = !0),
                                        o.disableFormSubmit(!0),
                                        (e.dsModel.label = u.creatingButton),
                                        o.onSubmitDialog("create");
                                },
                            });
                            break;
                        case "Ok":
                            n = new i({
                                name: "createClose",
                                label: u.createCloseBtn,
                                onClick: function (e) {
                                    if (
                                        !o.disableBtnClickOnEnter ||
                                        (o.disableBtnClickOnEnter && o.onEnterKeyDown)
                                    ) {
                                        if (
                                            o.disableBtnClickOnEnter &&
                                            o.onEnterKeyDown &&
                                            "wizardView" === o.viewType
                                        )
                                            return void o.footerButtons.RestoreDefaults.onClick();
                                        (this.permanentDisable = !0),
                                            o.disableFormSubmit(!0),
                                            (e.dsModel.label = u.creatingButton),
                                            o.myDialog.checkboxInfos &&
                                            o.myDialog.checkboxInfos.checkFlag
                                                ? o.onSubmitDialog("create")
                                                : o.onSubmitDialog("createClose");
                                    }
                                },
                            });
                            break;
                        case "draftModeButton":
                            n = o.buildDraftModeButton(t.activateDraftMode);
                            break;
                        case "Discard":
                            n = new i({
                                name: "close",
                                label: u.closeBtn,
                                onClick: function (e) {
                                    o.dispatchEvent("onClose");
                                },
                            });
                            break;
                        case "Reset":
                            n = new i({
                                name: "previous",
                                label: u.previousBtn,
                                onClick: function (e) {
                                    UWA.is(o.View.switchToRelativeFacet, "function") &&
                                    o.View.switchToRelativeFacet(-1);
                                },
                            });
                            break;
                        case "RestoreDefaults":
                            n = new i({
                                name: "next",
                                label: u.nextBtn,
                                onClick: function (e) {
                                    UWA.is(o.View.switchToRelativeFacet, "function") &&
                                    o.View.switchToRelativeFacet(1);
                                },
                            });
                    }
                    return n;
                },
                disableFormSubmit: function (e) {
                    if (this.footerButtons)
                        for (var t in (this.permanentDisable && (e = !0),
                            this.footerButtons)) {
                            var n = this.footerButtons[t];
                            ("Ok" != n.value &&
                                "Apply" != n.value &&
                                "createBtnCombo" !== t) ||
                            (n.disabled = e);
                        }
                },
                resetButtons: function () {
                    if (this.footerButtons)
                        for (var e in this.footerButtons) {
                            var t = this.footerButtons[e];
                            (t.disabled = !1),
                                "Ok" === t.value
                                    ? (t.label = u.get("createCloseBtn"))
                                    : "Apply" === t.value && (t.label = u.get("createBtn"));
                        }
                },
                onSubmit: function (e, t) {
                    this.View.facets[0].content.onSubmit(e, t);
                },
                postCreation: function (e, t) {
                    var n = this;
                    if ("listView" === this.viewType) t();
                    else {
                        var i = e.result,
                            o = {},
                            a = {},
                            r = {},
                            s = new Set();
                        this.facetDependency
                            ? (r = this.facetDependency)
                            : (this.View.facets.forEach(function (e) {
                                e.dependee && e.dependee.length > 0
                                    ? (r[e.facet.name] || (r[e.facet.name] = {}),
                                        (r[e.facet.name].to = new Set(e.dependee)),
                                        e.dependee.forEach(function (t) {
                                            r[t] || (r[t] = {});
                                            var n = r[t];
                                            n.from || (n.from = new Set()),
                                                n.from.add(e.facet.name);
                                        }))
                                    : s.add(e.facet.name);
                            }),
                                (this.facetDependency = r)),
                            this.independentFacetList
                                ? (s = this.independentFacetList)
                                : (this.independentFacetList = s),
                            (function e() {
                                var c = [];
                                s.forEach(function (e) {
                                    var t = n.View._getFacetFromName(e);
                                    if (t.content && UWA.is(t.content.postCreation, "function")) {
                                        var r = JSON.parse(JSON.stringify(i)),
                                            s = t.content.postCreation(r, a);
                                        s &&
                                        (s.then(function (e) {
                                            n.markComplete(t.facet, e, o, a);
                                        }),
                                            c.push(s));
                                    }
                                }),
                                    UWA.Promise.allSettled(c)
                                        .then(function (i) {
                                            var a = new Set();
                                            s.forEach((e) => {
                                                if (r[e]) {
                                                    var t = r[e].from;
                                                    t &&
                                                    t.forEach((t) => {
                                                        var n = r[t].to;
                                                        n && (n.delete(e), 0 == n.size && a.add(t));
                                                    });
                                                }
                                            }),
                                                (s = a).size > 0
                                                    ? e()
                                                    : (n.facetDependency &&
                                                    UWA.Object.keys(n.facetDependency).length > 0 &&
                                                    console.error(
                                                        "ENONew Facet dependency not resolved completely"
                                                    ),
                                                        n.closeView(o),
                                                        t(o));
                                        })
                                        .catch(function (e) {
                                            console.log("ENONew : postCreation failed"), t(o);
                                        });
                            })();
                    }
                },
                markComplete: function (e, t, n, i) {
                    var o = e.name;
                    if ((console.log("markComplete:" + o), console.log(t), t)) {
                        var a = { facetNLS: e.text, info: t };
                        (n[o] = a),
                        t.errors &&
                        ((i[o] = {}),
                            Object.assign(i[o], JSON.parse(JSON.stringify(t.errors))));
                    }
                },
                closeView: function (e) {
                    console.log("closeView"), console.log(e);
                },
                destroy: function () {
                    this.myDialog && this.myDialog.destroy(),
                    this.immersiveFrame && this.immersiveFrame.destroy(),
                        this._parent();
                },
            });
        }
    ),
    define(
        "DS/ENONewWidget/utils/DefaultCredential",
        [
            "DS/ENONewWidget/ajaxRequests/WebReq",
            "DS/ENONewWidget/ajaxRequests/LifecycleServicesSettings",
            "DS/WAFData/WAFData",
        ],
        function (e, t, n) {
            "use strict";
            let i = (i, o, a) => {
                    var r = t.get3DSpaceWSUrl(
                        i,
                        "/resources/pno/person/getsecuritycontext",
                        null
                    );
                    n.authenticatedRequest(r, {
                        type: "json",
                        method: "GET",
                        proxy: "passport",
                        headers: t.getHeaders(),
                        timeout: 6e5,
                        onComplete: function (e) {
                            var t = "";
                            if (
                                null != e &&
                                "" != e &&
                                e.hasOwnProperty("SecurityContext") &&
                                null != e.SecurityContext
                            ) {
                                (t = e.SecurityContext.replace("ctx::", "")).trim().length >
                                0 && (t = "ctx::" + e.SecurityContext),
                                    o(t);
                            } else a(lifecycleWidgetNls.ErrorSecurityContextEmpty);
                        },
                        onFailure: function (t, n) {
                            console.log("getSecurityContext:Failure..." + t),
                                e.onWebServiceError(a, t, n);
                        },
                        ontimeout: function () {
                            console.log("getSecurityContext: A connection timeout occurred."),
                                e.onWebServiceError(
                                    a,
                                    null,
                                    lifecycleWidgetNls.connectionTimeout
                                );
                        },
                    });
                },
                o = (e) => {
                    if (
                        "function" == typeof widget.hasPreference &&
                        widget.hasPreference("xPref_CREDENTIAL")
                    ) {
                        var t = widget.getPreference("xPref_CREDENTIAL");
                        if (
                            void 0 !== t.value &&
                            null !== t.value &&
                            t.hasOwnProperty("value")
                        )
                            return "ctx::" + t.value;
                    }
                    return "function" == typeof widget.hasPreference &&
                    null != widget.getPreference("pad_security_ctx")
                        ? widget.getPreference("pad_security_ctx").value
                        : null;
                };
            return {
                loadDefaultContexCredential: (e) =>
                    ((e, t) => {
                        var n = o(e);
                        return n
                            ? new UWA.Class.Promise.resolve(n)
                            : new UWA.Class.Promise(function (t, n) {
                                i(
                                    e,
                                    function (e) {
                                        t(e);
                                    },
                                    function (e) {
                                        n(e);
                                    }
                                );
                            });
                    })(e.tenant, e.context).then((e) =>
                        UWA.Promise.resolve(
                            ((e) =>
                                e.startsWith && e.startsWith("ctx::") ? e : "ctx::" + e)(e)
                        )
                    ),
            };
        }
    ),
    //初始化新建页面
    define(
        "DS/ENONewWidget/ajaxRequests/ENONewServices",
        [
            "UWA/Controls/Abstract",
            "DS/ENONewWidget/utils/CredentialList",
            "DS/ENONewWidget/utils/DefaultCredential",
            "DS/ENONewWidget/ajaxRequests/LifecycleServicesSettings",
            "DS/ENONewWidget/ajaxRequests/ENONewAjaxRequests",
            "DS/ENOCollabToolsUI/tracker/ENOCollabTrackerBackEnd",
            "i18n!DS/ENONewWidget/assets/nls/ENONewWidgetNls",
        ],
        function (e, t, n, i, o, a, r) {
            return (
                (Services = e.extend({
                    init: function (e, t) {
                        this._parent(e),
                        e &&
                        (e.tenant
                            ? (this.tenant = e.tenant)
                            : (this.tenant = i.getTenant()),
                            e.serviceBaseUrl
                                ? (this.service = e.serviceBaseUrl)
                                : e.service &&
                                (this.service = i.getPlatformBaseUrl(
                                    e.service,
                                    this.tenant
                                )),
                        this.service ||
                        (this.service = i.getPlatformBaseUrl("3DSpace", this.tenant)),
                        e.header &&
                        ((this.header = e.header),
                        e.header.SecurityContext &&
                        (this.prefSecCont = e.header.SecurityContext)),
                        t && (this.parsedResult = !!t.parsedResult));
                    },
                    getTypeInfo1: function (e, t, n) {
                        // add by chenyan 新增标识创建零件还是总成
                        widget.JFTypeName = e.typeName;
                        var i = this;
                        t && (this.prefSecCont = t);
                        var a = this.header ? this.header : {},
                            r = {
                                service:
                                    this.service +
                                    "/resources/v1/collabServices/authoring/createContent/typeInfo",
                                tenant: this.tenant,
                            };
                        return (
                            Object.keys(e).forEach(
                                (t) => (void 0 === e[t] || null === e[t]) && delete e[t]
                            ),
                                new UWA.Promise(function (t, s) {
                                    i.getPreferedSecurityContext()
                                        .then(function (c) {
                                            (i.prefSecCont = c),
                                                (a.SecurityContext = i.prefSecCont),
                                                (r.header = a),
                                                o._post(
                                                    e,
                                                    r,
                                                    function (e) {
                                                        e && "success" === e.status
                                                            ? t(i.parseTypeInfo(e, n))
                                                            : s(e);
                                                    },
                                                    function (e) {
                                                        console.log("vma rejected"), s(e);
                                                    }
                                                );
                                        })
                                        .catch(function (e) {
                                            console.log("vma security context failure"), console.log(e);
                                        });
                                })
                        );
                    },
                    //初始化新建页面
                    getCurrentUserHasStandardRoleAndHandle : async function (jsonData) {
                        var hasRole = false;
                        const  r = {
                            service:
                                this.service +
                                "/TWXPublicRest/TWXTicketService",
                            params :{
                                JPOName : "JF_ECRRESTService",
                                FuncName : "getCurrentUserHasStandardRole"
                            }
                        };
                        o._post(
                            {},
                            r,
                            function (e) {
                                if (e && "success" === e.status){
                                    //只有属性有值才移除
                                    if (e.attributes){
                                        var removeAttributes = e.attributes;
                                        Object.keys(removeAttributes).forEach( strAttrName => {
                                            const attrRanges = removeAttributes[strAttrName];
                                            jsonData.result.forEach(result => {
                                                const attributes = result.attributes;
                                                if (attributes.extensionAttributes){
                                                    attributes.extensionAttributes.forEach(attr => {
                                                        if (attr.name === strAttrName) {
                                                            var removeIndex  = [];
                                                            //找到需要移除的
                                                            attr.range.forEach((strRange,index) =>{
                                                                console.log("strRange",strRange);
                                                                console.log("index",index);
                                                                //如果移除的range值中包含此range记录此下标
                                                                if(attrRanges.includes(strRange)){
                                                                    removeIndex.push(index);
                                                                }
                                                            });
                                                            if (removeIndex.length > 0){
                                                                console.log(removeIndex);
                                                                for (let j = 0; j < removeIndex.length; j++) {
                                                                    const index = removeIndex[j];
                                                                    attr.range.splice(index, 1);
                                                                    attr.rangeNLS.splice(index, 1);
                                                                }
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        });
                                    }
                                }
                            },
                            function (e) {
                                console.log("request fail",e);
                            }
                        );
                        console.log("jsonData:",jsonData)
                        console.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                        return jsonData;
                    },
                    // add by chenyan 0829  替换物理产品为 Physical Product
                    replacePhysicalProduct :function (jsonData) {
                        jsonData.result.forEach(result => {
                            const attributes = result.attributes;
                            if (attributes.publicAttributes){
                                attributes.publicAttributes.forEach(attr => {
                                    if (attr.name === 'V_Name' ) {
                                        if (attr.value.includes('物理产品')){
                                            attr.value = attr.value.replace('物理产品', 'Physical Product');
                                        }
                                        attr['dropdownMode']='click';
                                        // add by chenyan 将标题设置为不必填放到下面 2023/07/22
                                        attr['modified']= true;
                                        attr['mandatory']= false;
                                        attr['isReadOnly']= true;
                                        // attr.ismode = false;
                                    }
                                });
                            }
                            if (attributes.volatileAttributes){
                                attributes.volatileAttributes.forEach(volatileAttr => {
                                    if (volatileAttr.name === 'Filename' && volatileAttr.value.includes('物理产品')) {
                                        volatileAttr.value = volatileAttr.value.replace('物理产品', 'Physical Product');
                                    }
                                    if(volatileAttr.name === 'Filename'){
                                        volatileAttr.isReadOnly=true;
                                    }
                                });
                            }
                            // ad by caipan JF_PartSubType 增加子类型
                            //var JF_PartType="";
                            if (attributes.extensionAttributes){
                                //20260720 update by ljr XEN新建零件界面不再显示DirectBuy、客户零件号、客户零件版本。
                                const jfXENHiddenAttrNames = [
                                    "JF_DirectBuy",
                                    "JF_VPMReference.JF_DirectBuy",
                                    "JFCustomerPartNumber",
                                    "JFCustomerPartRevision",
                                    "CustomerPartNumber",
                                    "CustomerPartRevision",
                                    "JF_CustomerPartNumber",
                                    "JF_CustomerPartRevision"
                                ];
                                attributes.extensionAttributes = attributes.extensionAttributes.filter(attr => !jfXENHiddenAttrNames.includes(attr.name));
                                attributes.extensionAttributes.forEach(volatileAttr => {
                                    if (volatileAttr.name === 'JF_PartType') {
                                        volatileAttr['mandatory']=true;
                                        volatileAttr['dropdownMode']='click';
                                        volatileAttr['modified']=true;
                                    }
                                    // add by chenyan 新增详细类别CN 详细类别设置为必填 中文名称英文名称 2025/07/21
                                    if (volatileAttr.name === 'JF_Detail_CN' || volatileAttr.name === 'JF_Detail_EN' || volatileAttr.name === 'JF_PartNameCN' || volatileAttr.name === 'JF_PartNameEN') {
                                        volatileAttr['mandatory'] = true;
                                    }

                                });
                            }

                        });

                        return jsonData;
                    },
                    //加载界面元素
                    parseTypeInfo: async function (e, t) {
                        var n = {};
                        // add by 2024/10/30 chenyan 新增只有XEN才走修改逻辑，其他APP不走
                        let isXENAPP = false ;
                        if (widget){
                            const  appID = widget.data.appId;
                            console.log("appID:",appID)
                            if ("ENXENG_AP" == appID){
                                isXENAPP = true ;
                            }
                        }
                        if (isXENAPP){
                            // add by chenyan 2024 09/04 新增发送请求获取当前登录人是否包含标注件库role 不包含需要去掉子类型中的标准件
                            e = await this.getCurrentUserHasStandardRoleAndHandle(e);
                            // add by chenyan 2024 08/29 将请求数据的V_Name 中的中文改为英文 防止Catia V5 创建报错
                            e = this.replacePhysicalProduct(e);
                        }
                        console.log(e.result);
                        this.rawTypeInfo = e.result;
                        var i = e.result[0];
                        if (t) return i;
                        var o = i.type,
                            a = i.subTypes;
                        if ((a = UWA.is(a, "array") ? a : [])) {
                            var s = [],
                                c = [];
                            for (var l in a) {
                                var d = a[l];
                                s.push(d.name), c.push(d.nls);
                            }
                            var u = s.length > 1,
                                f = {
                                    name: "Type",
                                    nls: r.Type,
                                    type: "String",
                                    value: o,
                                    range: s,
                                    rangeNLS: c,
                                    positionUI: 1,
                                    positionGroup: 6,
                                    visible: u,
                                    defaultAttribute: !0,
                                    sortedOptions: !0,
                                    mandatory: !0,
                                    persistData: a,
                                    dropdownMode: "click",
                                };
                            n.Type = f;
                        }
                        if (isXENAPP){
                            //add by ljr
                            var jfPartType = 0;
                            for (var m = 0; m < i.attributes.extensionAttributes.length; m++) {
                                if ("JF_PartType" === i.attributes.extensionAttributes[m].name) {
                                    jfPartType = m;
                                    break;
                                }
                            }
                            var oneAttr = i.attributes.extensionAttributes[0];
                            i.attributes.extensionAttributes[0] = i.attributes.extensionAttributes[jfPartType];
                            i.attributes.extensionAttributes[jfPartType] = oneAttr;
                            //end
                        }
                        return (
                            (n.attributes = i.attributes),
                                (n.interfaces = i.interfaces),
                                (n.reInitOnSecCtxChange = 1 == i.reInitOnSecCtxChange),
                                (n.prefs = i.prefs),
                                n
                        );
                    },
                    createContent: function (e, t, n, i) {
                        var r = this,
                            s =
                                this.service +
                                "/resources/v1/collabServices/authoring/createContent/Create",
                            c = { tenant: this.tenant, service: s },
                            l = this.header ? this.header : {};
                        for (var d in ((l.SecurityContext = t.securityContext),
                            (c.header = l),
                            e)) {
                            var u = e[d];
                            this.rawTypeInfo[0][d] = u;
                        }
                        var f = { create: this.rawTypeInfo };
                        a.enrichRequestWithMetrics(f, { UXName: "New" });
                        var p = {
                            typeName: "",
                            select: [],
                            aggregating_context: [],
                            parsed: this.parsedResult,
                            folderID: [],
                            selectedIDs: [],
                            activeFolder: !1,
                        };
                        if (e) {
                            if (t.queryParams) {
                                for (var d in p) (val = t.queryParams[d]), val && (p[d] = val);
                                c.params = p;
                            }
                            o._post(
                                f,
                                c,
                                function (e) {
                                    n(e, r.service);
                                },
                                i
                            );
                        }
                    },
                    lockUnlockObject(e, t, n, i) {
                        var a =
                                "/resources/v1/collabServices/reservation/op/" +
                                (e && "LOCK" === e ? "reserve" : "unreserve"),
                            r = this.header ? this.header : {},
                            s = this.service + a,
                            c = {
                                tenant: this.tenant,
                                service: s,
                                params: { isMultiSel: 0, select: "physicalid" },
                                header: r,
                            },
                            l = { branchorcontent: "type" };
                        if (t && t.result) {
                            var d = [];
                            t.result.forEach(function (e) {
                                e.type && (d.push("model/bus/" + e.physicalid), 0);
                            }),
                                (l.urls = d);
                        }
                        (r.SecurityContext = n.securityContext
                            ? n.securityContext
                            : this.prefSecCont),
                            o._post(
                                l,
                                c,
                                function (e) {
                                    i(e);
                                },
                                function (e) {
                                    i(e);
                                }
                            );
                    },
                    getSecurityContextList: function () {
                        var e = this,
                            n = this.getPreferedSecurityContext(),
                            i = t.loadCredentials(e.tenant);
                        return new UWA.Promise(function (t) {
                            UWA.Promise.all([n, i]).then(function (n) {
                                n.length < 2 || !UWA.is(n[1], "array")
                                    ? console.error("Missing credentials")
                                    : t(
                                        (function (t) {
                                            var n = this.prefSecCont;
                                            (n && "" != n) || (n = t[0]), (e.preferedSecCtx = n);
                                            var i = t[1],
                                                o = [],
                                                a = [];
                                            for (var s in i) {
                                                var c = i[s];
                                                o.push(c.ctx), a.push(c.displayName);
                                            }
                                            var l = o.length > 1;
                                            return {
                                                name: "SecurityContext",
                                                nls: r.SecurityContext,
                                                type: "String",
                                                value: n,
                                                range: o,
                                                rangeNLS: a,
                                                visible: l,
                                                positionUI: 1,
                                                positionGroup: 8,
                                                defaultAttribute: !0,
                                                sortedOptions: !0,
                                                mandatory: !0,
                                                dropdownMode: "click",
                                            };
                                        })(n)
                                    );
                            });
                        });
                    },
                    getPreferedSecurityContext: function () {
                        var e = this;
                        return this.prefSecCont
                            ? new UWA.Promise(function (t) {
                                t(e.prefSecCont);
                            })
                            : n.loadDefaultContexCredential({
                                tenant: this.tenant,
                                context: this.context,
                            });
                    },
                    set3DPartTemplate: function (e, t, n, i) {
                        var a =
                                this.service +
                                "/resources/v1/modelerServices/authoring/op/set3DPartTemplateExpression",
                            r = {
                                securityContext: this.prefSecCont,
                                tenant: this.tenant,
                                service: a,
                            },
                            s = { data: [e] };
                        e && o._post(s, r, n, i);
                    },
                    getListofTypes: function (e, t, n) {
                        var i = this.header ? this.header : {},
                            a =
                                this.service +
                                "/resources/v1/collabServices/authoring/createContent/listOfTypes",
                            r = { tenant: this.tenant, service: a };
                        e &&
                        (this.prefSecCont
                            ? ((i.SecurityContext = this.prefSecCont),
                                (r.header = i),
                                o._post(e, r, t, n))
                            : this.getPreferedSecurityContext()
                                .then(function (a) {
                                    (this.prefSecCont = a),
                                        (i.SecurityContext = this.prefSecCont),
                                        (r.header = i),
                                        o._post(e, r, t, n);
                                })
                                .catch(function (e) {
                                    console.log("vma security context failure"),
                                        console.log(e);
                                }));
                    },
                })),
                    Services
            );
        }
    ),
    //初始化新建页面
    define(
        "DS/ENONewWidget/command/NewContentCmd",
        [
            "DS/ENONewWidget/command/NewContentCmdMap",
            "DS/ENONewWidget/command/NewContentCommandBase",
            "DS/ENONewWidget/ENONewLauncher",
            "DS/ENONewWidget/command/NewContentDialog",
            "DS/ENONewWidget/ajaxRequests/ENONewServices",
            "DS/ENONewWidget/ajaxRequests/LifecycleServicesSettings",
        ],
        function (e, t, n, i, o, a) {
            "use strict";
            return t.extend({
                init: function (e) {
                    this._parent(e);
                    var t,
                        a = this.ENONewContext;
                    a
                        ? (a.getNewTypeList ||
                        ((this.existingImpl = !0),
                            a.updatedListOfTypes && UWA.is(a.updatedListOfTypes, "function")
                                ? (this.updatedListOfTypes = a.updatedListOfTypes)
                                : console.log("updatedListOfTypes function is invalid"),
                            a.getLaunchOptions && UWA.is(a.getLaunchOptions, "function")
                                ? (this.getLaunchOptions = a.getLaunchOptions)
                                : console.log("getLaunchOptions function is invalid")),
                        a.getLoginContext &&
                        UWA.is(a.getLoginContext, "function") &&
                        (t = a.getLoginContext()))
                        : console.log("ENONewContext object is empty"),
                        (this.dialog = new i()),
                        (this.launcher = new n()),
                        (this.services = new o(t)),
                        (this.isCreateDialogOpen = !1),
                        this.enable(),
                        (this.specializedCommands = {});
                },
                execute: function () {
                    var e,
                        t = this;
                    if (!this.isCreateDialogOpen)
                        if ((this.disable(), this.existingImpl)) {
                            var n = this.updatedListOfTypes();
                            n = n || this.ENONewContext.panelContent.data;
                            var i = this.ENONewContext.options
                                ? this.ENONewContext.options
                                : {};
                            (e = this.dialog.displayDialog(n, i, function (e) {
                                var n = e.dsModel;
                                if ((console.log("btn clicked : " + n.label), n)) {
                                    var i = n.value;
                                    i ? t.launchPanel(i) : console.log("Failed to get type Info");
                                } else console.log("Failed to get view Model");
                            })) &&
                            (e.addEventListener("close", function (e) {
                                (t.isCreateDialogOpen = !1),
                                    t.dialog.destroyDialog(),
                                    t.enable();
                            }),
                                (t.isCreateDialogOpen = !0));
                        } else {
                            var o = this.getNewTypeList();
                            this.getTypeList(o, function (n) {
                                var i =
                                    a.get3DSpaceBaseUrl(a.getTenant()) +
                                    "/snresources/images/icons/small/";
                                (e = t.dialog.display(n, { iconBaseURL: i }, function (e) {
                                    t.launchTypeCmd(e), t.closeNewContentPanel();
                                })) &&
                                (e.addEventListener("close", function (e) {
                                    t.closeNewContentPanel();
                                }),
                                    (t.isCreateDialogOpen = !0));
                            });
                        }
                },
                getType() {
                    return this._type;
                },
                getNewTypeList() {
                    var e = this.ENONewContext;
                    if (e && e.getNewTypeList && UWA.is(e.getNewTypeList, "function"))
                        return e.getNewTypeList(this.options);
                },
                getTypeList(e, t) {
                    e && e.typeList
                        ? this.services.getListofTypes(
                            e,
                            function (e) {
                                console.log("vma12:Success:getTypeList"), t(e.result);
                            },
                            function () {
                                console.log("vma12:Fail:getTypeList");
                            }
                        )
                        : t();
                },
                launchTypeCmd(t) {
                    var n = this;
                    if (((this._type = t.type), (this.typeDetails = t), t.newType))
                        if (this.specializedCommands[t.newType]) {
                            this.specializedCommands[t.newType].commandInstance.execute(t);
                        } else {
                            var i = e.getCmdDetails(t.newType);
                            if (i)
                                (n.specializedCommands[t.newType] = i),
                                    require([i.cmdRequire], function (e) {
                                        var i = new e(n.options);
                                        (n.specializedCommands[t.newType].commandInstance = i),
                                            i.execute(t);
                                    });
                            else {
                                this._type = "Default";
                                var o = this.getCreateOptions({
                                    type: t.type,
                                    newType: "Default",
                                });
                                this.launchCreateNew(o);
                            }
                        }
                    else console.error("Command launch failed");
                },
                addObjectInLifecycleWidget(e) {
                    var t;
                    e &&
                    ((t = []),
                        e.forEach(function (e) {
                            var n = {
                                objectId: e.identifier,
                                displayName: e.V_name,
                                objectType: e.type,
                            };
                            t.push(n);
                        }),
                        this.options.context.addNewObject1({ data: { items: t } }));
                },
                getOptions(e) {
                    var t = this;
                    return {
                        context: {},
                        type: e.type,
                        options: { typeName: e.newType },
                        events: {
                            onSuccess: function (e) {
                                t.addObjectInLifecycleWidget(e.result);
                            },
                            onFailure: function (e) {},
                            onCancel: function (e) {},
                        },
                    };
                },
                closeNewContentPanel() {
                    (this.isCreateDialogOpen = !1),
                    this.dialog && this.dialog.destroyDialog(),
                        this.enable();
                },
                launchPanel: function (e) {
                    var t = this.getLaunchOptions(e);
                    this.launcher.CreateNew(t),
                        (this.isCreateDialogOpen = !1),
                        this.dialog.destroyDialog(),
                        this.enable();
                },
            });
        }
    ),
    define(
        "DS/ENONewWidget/command/sample/NewContentInputTypeCmd",
        ["DS/ENONewWidget/command/NewContentCmd"],
        function (e) {
            "use strict";
            return e.extend({
                getNewTypeList: () => [],
                getTypeList(e, t) {
                    t();
                },
                launchTypeCmd(e) {
                    this._type = e.newType;
                    var t = this.getCreateOptions(e);
                    this.launchCreateNew(t);
                },
            });
        }
    );
