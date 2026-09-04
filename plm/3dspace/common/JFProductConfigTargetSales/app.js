(function () {
    function getQuery(name) {
        var match = new RegExp("[?&]" + name + "=([^&]*)").exec(window.location.search);
        return match ? decodeURIComponent(match[1].replace(/\+/g, " ")) : "";
    }

    function isUsableObjectId(objectId) {
        return !!objectId && objectId !== "null" && objectId !== "undefined" && objectId !== "MyDesk" && objectId.indexOf("$") < 0 && objectId.indexOf("{") < 0 && objectId.indexOf("<") < 0;
    }

    function getQueryFromWindow(targetWindow, name) {
        try {
            var search = targetWindow && targetWindow.location ? targetWindow.location.search : "";
            var match = new RegExp("[?&]" + name + "=([^&]*)").exec(search);
            return match ? decodeURIComponent(match[1].replace(/\+/g, " ")) : "";
        } catch (e) {
            return "";
        }
    }

    function getObjectIdFromWindow(targetWindow) {
        if (!targetWindow) {
            return "";
        }
        var objectId = getQueryFromWindow(targetWindow, "objectId") || getQueryFromWindow(targetWindow, "parentOID");
        if (isUsableObjectId(objectId)) {
            return objectId;
        }
        try {
            if (targetWindow.getUWAPref) {
                objectId = targetWindow.getUWAPref("objectId") || "";
                if (isUsableObjectId(objectId)) {
                    return objectId;
                }
            }
            if (targetWindow.bclist && targetWindow.bclist.getCurrentBC && targetWindow.bclist.getCurrentBC()) {
                objectId = targetWindow.bclist.getCurrentBC().id || "";
                if (isUsableObjectId(objectId)) {
                    return objectId;
                }
            }
        } catch (e) {
            return "";
        }
        return "";
    }

    function getInitialObjectId() {
        var objectId = getQuery("objectId") || getQuery("parentOID") || "";
        if (isUsableObjectId(objectId)) {
            window.sessionStorage.setItem("JFProductConfigTargetSales.objectId", objectId);
            return objectId;
        }
        var windows = [window, window.parent, window.top];
        try {
            if (window.parent && window.parent.getTopWindow) {
                windows.push(window.parent.getTopWindow());
            }
            if (window.top && window.top.getTopWindow) {
                windows.push(window.top.getTopWindow());
            }
            if (window.opener) {
                windows.push(window.opener);
            }
        } catch (e) {
        }
        for (var i = 0; i < windows.length; i++) {
            objectId = getObjectIdFromWindow(windows[i]);
            if (isUsableObjectId(objectId)) {
                window.sessionStorage.setItem("JFProductConfigTargetSales.objectId", objectId);
                return objectId;
            }
        }
        try {
            objectId = window.sessionStorage.getItem("JFProductConfigTargetSales.objectId") || window.sessionStorage.getItem("JFProductConfigParts.objectId") || "";
            if (isUsableObjectId(objectId)) {
                window.sessionStorage.setItem("JFProductConfigTargetSales.objectId", objectId);
                return objectId;
            }
        } catch (e) {
        }
        try {
            if (window.parent && window.parent.sessionStorage) {
                objectId = window.parent.sessionStorage.getItem("JFProductConfigTargetSales.objectId") || window.parent.sessionStorage.getItem("JFProductConfigParts.objectId") || "";
                if (isUsableObjectId(objectId)) {
                    window.sessionStorage.setItem("JFProductConfigTargetSales.objectId", objectId);
                    return objectId;
                }
            }
        } catch (e) {
            return "";
        }
        return "";
    }

    function get3DSpaceBase() {
        var marker = "/common/JFProductConfigTargetSales/";
        var index = window.location.pathname.indexOf(marker);
        if (index > -1) {
            return window.location.pathname.substring(0, index);
        }
        return "/3dspace";
    }

    function parseJsonResponse(response) {
        return response.text().then(function (text) {
            try {
                return JSON.parse(text);
            } catch (e) {
                throw new Error("Invalid JSON response from " + response.url + ": " + text.replace(/\s+/g, " ").substring(0, 220));
            }
        });
    }

    function callProductConfigRest(functionName, params) {
        var query = "?JPOName=JF_ProductConfig&FuncName=" + encodeURIComponent(functionName) + "&Params=" + encodeURIComponent(JSON.stringify(params || {}));
        return fetch(get3DSpaceBase() + "/TWXPublicRest/TWXTicketService" + query, {
            method: "GET",
            credentials: "same-origin",
            headers: {"Accept": "application/json"}
        }).then(parseJsonResponse);
    }

    function uploadTargetSalesSummaryExcel(objectId, file) {
        var formData = new FormData();
        formData.append("objectId", objectId || "");
        formData.append("file", file);
        return fetch(get3DSpaceBase() + "/common/JF_ProductConfigTargetSalesImport.jsp?timeStamp=" + new Date().getTime(), {
            method: "POST",
            credentials: "same-origin",
            headers: {"Accept": "application/json"},
            body: formData
        }).then(parseJsonResponse);
    }

    function getCookieValue(name) {
        var cookies = document.cookie ? document.cookie.split(";") : [];
        for (var i = 0; i < cookies.length; i++) {
            var cookie = cookies[i].trim();
            if (cookie.indexOf(name + "=") === 0) {
                return decodeURIComponent(cookie.substring(name.length + 1));
            }
        }
        return "";
    }

    function clearCookie(name) {
        document.cookie = name + "=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/";
    }

    function openPartNavigatorLauncher(partId) {
        var frameId = "JFProductConfigPartNavigatorLauncher";
        var frame = document.getElementById(frameId);
        if (!frame) {
            frame = document.createElement("iframe");
            frame.id = frameId;
            frame.name = frameId;
            frame.style.display = "none";
            document.body.appendChild(frame);
        }
        frame.src = get3DSpaceBase() + "/common/JF_ProductConfigPartNavigatorLauncher.jsp?objectId=" + encodeURIComponent(partId || "") + "&timeStamp=" + new Date().getTime();
    }

    var DEFAULT_I18N = {
        "emxFramework.JFProductConfigTargetSales.Title": "Target Sales",
        "emxFramework.JFProductConfigTargetSales.AnnualTargetSales": "Annual Target Sales",
        "emxFramework.JFProductConfigTargetSales.ConfigurationRatio": "Configuration Ratio",
        "emxFramework.JFProductConfigTargetSales.Summary": "Summary",
        "emxFramework.JFProductConfigTargetSales.BasicInfo": "Basic Information",
        "emxFramework.Attribute.JFProjectLevelAPRDiscount": "Project Level APR Discount(%)",
        "emxFramework.JFProductConfigTargetSales.Save": "Save",
        "emxFramework.JFProductConfigTargetSales.AddYear": "Add Year",
        "emxFramework.JFProductConfigTargetSales.Delete": "Delete",
        "emxFramework.JFProductConfigTargetSales.CalculateSummary": "Automatic Calculation",
        "emxFramework.JFProductConfigTargetSales.ExportModify": "Export Modify",
        "emxFramework.JFProductConfigTargetSales.Import": "Import",
        "emxFramework.JFProductConfigTargetSales.ImportFailed": "Import failed.",
        "emxFramework.JFProductConfigTargetSales.Submit": "Submit",
        "emxFramework.JFProductConfigTargetSales.SubmitSuccess": "Submitted successfully.",
        "emxFramework.JFProductConfigTargetSales.ManagerTask": "Customer Manager Task",
        "emxFramework.JFProductConfigTargetSales.Sequence": "Sequence",
        "emxFramework.JFProductConfigTargetSales.TaskName": "Name",
        "emxFramework.JFProductConfigTargetSales.Status": "Status",
        "emxFramework.JFProductConfigTargetSales.CreateTime": "Created Time",
        "emxFramework.JFProductConfigTargetSales.CompleteTime": "Completion Time",
        "emxFramework.JFProductConfigTargetSales.Owner": "Owner",
        "emxFramework.JFProductConfigTargetSales.NoManagerTasks": "No customer manager tasks.",
        "emxFramework.JFProductConfigTargetSales.ApprovalDialogTitle": "Submit Approval",
        "emxFramework.JFProductConfigTargetSales.ApprovalComment": "Approval Comment",
        "emxFramework.JFProductConfigTargetSales.ApprovalCommentPlaceholder": "Enter an approval comment.",
        "emxFramework.JFProductConfigTargetSales.ApprovalCommentRequired": "Approval comment is required.",
        "emxFramework.JFProductConfigTargetSales.Agree": "Agree",
        "emxFramework.JFProductConfigTargetSales.CalculateModePrompt": "Automatic calculation will overwrite existing content. Please be aware.",
        "emxFramework.JFProductConfigTargetSales.Overwrite": "Overwrite",
        "emxFramework.JFProductConfigTargetSales.FillBlankOnly": "Fill blanks only",
        "emxFramework.JFProductConfigTargetSales.Confirm": "Confirm",
        "emxFramework.JFProductConfigTargetSales.Cancel": "Cancel",
        "emxFramework.JFProductConfigTargetSales.Year": "Year",
        "emxFramework.JFProductConfigTargetSales.Overseas": "Overseas",
        "emxFramework.JFProductConfigTargetSales.Domestic": "Domestic",
        "emxFramework.JFProductConfigTargetSales.Other": "Other",
        "emxFramework.JFProductConfigTargetSales.OverseasPercent": "Overseas(%)",
        "emxFramework.JFProductConfigTargetSales.DomesticPercent": "Domestic(%)",
        "emxFramework.JFProductConfigTargetSales.OtherPercent": "Other(%)",
        "emxFramework.JFProductConfigTargetSales.Configuration": "Configuration",
        "emxFramework.JFProductConfigTargetSales.PartNumber": "Part Number",
        "emxFramework.JFProductConfigTargetSales.PartName": "Part Name",
        "emxFramework.JFProductConfigTargetSales.CustomerPartNumber": "Customer Part Number",
        "emxFramework.Attribute.JFCustomerPartName": "Customer Part Name",
        "emxFramework.JFProductConfigTargetSales.PartInfo": "Part Info",
        "emxFramework.JFProductConfigTargetSales.Ratio": "Ratio",
        "emxFramework.JFProductConfigTargetSales.NoYears": "No target sales years.",
        "emxFramework.JFProductConfigTargetSales.NoVehicleConfigs": "No vehicle configurations.",
        "emxFramework.JFProductConfigTargetSales.NoParts": "No parts.",
        "emxFramework.JFProductConfigParts.ObjectCountUnit": "objects",
        "emxFramework.JFProductConfigTargetSales.ProductConfigTableIdEmpty": "Current product config table id is empty.",
        "emxFramework.JFProductConfigTargetSales.YearRequired": "Year is required.",
        "emxFramework.JFProductConfigTargetSales.YearInvalid": "Year must be a 4-digit number.",
        "emxFramework.JFProductConfigTargetSales.YearExists": "Year already exists.",
        "emxFramework.JFProductConfigTargetSales.SelectYearFirst": "Please select a target sales year first.",
        "emxFramework.JFProductConfigTargetSales.RatioRangeInvalid": "Configuration ratio must be a number from 0 to 100; decimals are allowed.",
        "emxFramework.JFProductConfigTargetSales.Processing": "Processing, please wait...",
        "emxFramework.JFProductConfigTargetSales.LoadFailed": "Load failed.",
        "emxFramework.JFProductConfigTargetSales.SaveSuccess": "Saved.",
        "emxFramework.JFProductConfigTargetSales.CreateFailed": "Create failed.",
        "emxFramework.JFProductConfigParts.QuantityNumberOnly": "Quantity can only contain numbers."
    };

    function loadI18n() {
        return fetch("./i18n.jsp?timeStamp=" + new Date().getTime(), {
            method: "GET",
            credentials: "same-origin",
            headers: {"Accept": "application/json"}
        }).then(parseJsonResponse);
    }

    if (!window.Vue || !window.Vue.createApp) {
        throw new Error("Vue runtime is unavailable.");
    }

    window.Vue.createApp({
        data: function () {
            return {
                objectId: getInitialObjectId(),
                targetSales: [],
                vehicleConfigs: [],
                parts: [],
                managerTasks: [],
                selectedPartIds: [],
                selectedTargetSalesIds: [],
                selectedManagerTaskIds: [],
                projectLevelAPRDiscount: "",
                lastSavedProjectLevelAPRDiscount: "",
                projectLevelAPRDiscountSaveTimer: null,
                targetSalesSaveTimers: {},
                vehicleRatioSaveTimers: {},
                sections: {
                    basicInfo: true,
                    targetSales: true,
                    vehicleRatio: true,
                    summary: true,
                    managerTasks: true
                },
                newYear: "",
                loading: false,
                calculateDialogVisible: false,
                submitDialogVisible: false,
                approvalComment: "",
                canEdit: false,
                message: "",
                messageType: "info",
                messageTimer: null,
                i18n: Object.assign({}, DEFAULT_I18N)
            };
        },
        mounted: function () {
            var self = this;
            loadI18n()
                .then(function (data) {
                    self.i18n = Object.assign({}, DEFAULT_I18N, data || {});
                    document.title = self.t("emxFramework.JFProductConfigTargetSales.Title");
                })
                .catch(function () {
                    document.title = self.t("emxFramework.JFProductConfigTargetSales.Title");
                })
                .then(function () {
                    self.loadData();
                });
        },
        beforeUnmount: function () {
            if (this.projectLevelAPRDiscountSaveTimer) {
                window.clearTimeout(this.projectLevelAPRDiscountSaveTimer);
                this.projectLevelAPRDiscountSaveTimer = null;
            }
            this.clearSaveTimers(this.targetSalesSaveTimers);
            this.clearSaveTimers(this.vehicleRatioSaveTimers);
        },
        methods: {
            t: function (key) {
                return this.i18n[key] || DEFAULT_I18N[key] || key;
            },
            sanitizeNumber: function (value) {
                return String(value || "").replace(/\D/g, "");
            },
            markTargetSalesSaved: function (item) {
                item._lastSaved = {
                    overseas: item.overseas || "",
                    domestic: item.domestic || "",
                    other: item.other || ""
                };
            },
            markVehicleRatioSaved: function (config) {
                config._lastSaved = {
                    overseas: config.overseas || "",
                    domestic: config.domestic || "",
                    other: config.other || ""
                };
                config._invalidFields = {};
            },
            clearSaveTimers: function (timers) {
                Object.keys(timers || {}).forEach(function (key) {
                    window.clearTimeout(timers[key]);
                    delete timers[key];
                });
            },
            loadData: function (successMessage) {
                var self = this;
                if (!isUsableObjectId(this.objectId)) {
                    this.objectId = getInitialObjectId();
                }
                if (!isUsableObjectId(this.objectId)) {
                    this.showMessage(this.t("emxFramework.JFProductConfigTargetSales.ProductConfigTableIdEmpty"), "error");
                    return;
                }
                this.loading = true;
                callProductConfigRest("getTargetSalesData", {objectId: this.objectId})
                    .then(function (data) {
                        if (String(data.code) !== "200") {
                            self.showMessage(data.mess || self.t("emxFramework.JFProductConfigTargetSales.LoadFailed"), "error");
                            return;
                        }
                        self.canEdit = data.canEdit === true || String(data.canEdit) === "true";
                        self.projectLevelAPRDiscount = data.projectLevelAPRDiscount || "";
                        self.lastSavedProjectLevelAPRDiscount = self.projectLevelAPRDiscount;
                        self.targetSales = data.targetSales || [];
                        self.vehicleConfigs = data.vehicleConfigs || [];
                        self.parts = data.parts || [];
                        self.managerTasks = data.managerTasks || [];
                        self.selectedTargetSalesIds = [];
                        self.selectedManagerTaskIds = [];
                        self.targetSales.forEach(self.markTargetSalesSaved);
                        self.vehicleConfigs.forEach(self.markVehicleRatioSaved);
                        if (successMessage) {
                            self.showMessage(successMessage, "info");
                        }
                    })
                    .catch(function (error) {
                        self.showMessage(error.message, "error");
                    })
                    .then(function () {
                        self.loading = false;
                    });
            },
            createYear: function () {
                var self = this;
                if (!this.canEdit || this.loading) {
                    return;
                }
                var year = this.sanitizeNumber(this.newYear);
                if (!year) {
                    this.showMessage(this.t("emxFramework.JFProductConfigTargetSales.YearRequired"), "error");
                    return;
                }
                if (!/^\d{4}$/.test(year)) {
                    this.showMessage(this.t("emxFramework.JFProductConfigTargetSales.YearInvalid"), "error");
                    return;
                }
                if (this.targetSales.some(function (item) { return item.year === year; })) {
                    this.showMessage(this.t("emxFramework.JFProductConfigTargetSales.YearExists"), "error");
                    return;
                }
                this.loading = true;
                callProductConfigRest("createTargetSalesYear", {objectId: this.objectId, year: year})
                    .then(function (data) {
                        if (String(data.code) !== "200") {
                            self.showMessage(data.mess || self.t("emxFramework.JFProductConfigTargetSales.CreateFailed"), "error");
                            return;
                        }
                        self.newYear = "";
                        self.targetSales = data.targetSales || [];
                        self.vehicleConfigs = data.vehicleConfigs || [];
                        self.parts = data.parts || [];
                        self.selectedTargetSalesIds = [];
                        self.targetSales.forEach(self.markTargetSalesSaved);
                        self.vehicleConfigs.forEach(self.markVehicleRatioSaved);
                    })
                    .catch(function (error) {
                        self.showMessage(error.message, "error");
                    })
                    .then(function () {
                        self.loading = false;
                    });
            },
            toggleSection: function (sectionName) {
                this.sections[sectionName] = !this.sections[sectionName];
            },
            toggleAllManagerTasks: function (event) {
                this.selectedManagerTaskIds = event.target.checked
                    ? this.managerTasks.map(function (task) { return task.id; })
                    : [];
            },
            deleteSelectedTargetSales: function () {
                var self = this;
                if (!this.canEdit || this.selectedTargetSalesIds.length === 0) {
                    return;
                }
                this.selectedTargetSalesIds.forEach(function (targetSalesId) {
                    if (self.targetSalesSaveTimers[targetSalesId]) {
                        window.clearTimeout(self.targetSalesSaveTimers[targetSalesId]);
                        delete self.targetSalesSaveTimers[targetSalesId];
                    }
                });
                this.loading = true;
                callProductConfigRest("deleteTargetSalesYears", {
                    objectId: this.objectId,
                    targetSalesIds: this.selectedTargetSalesIds.slice()
                }).then(function (data) {
                    if (String(data.code) !== "200") {
                        self.showMessage(data.mess || self.t("emxFramework.JFProductConfigTargetSales.LoadFailed"), "error");
                        return;
                    }
                    self.targetSales = data.targetSales || [];
                    self.vehicleConfigs = data.vehicleConfigs || [];
                    self.parts = data.parts || [];
                    self.selectedTargetSalesIds = [];
                    self.targetSales.forEach(self.markTargetSalesSaved);
                    self.vehicleConfigs.forEach(self.markVehicleRatioSaved);
                }).catch(function (error) {
                    self.showMessage(error.message, "error");
                }).then(function () {
                    self.loading = false;
                });
            },
            handleProjectLevelAPRDiscountInput: function (event) {
                var self = this;
                this.projectLevelAPRDiscount = this.sanitizeNumber(event.target.value);
                event.target.value = this.projectLevelAPRDiscount;
                if (!this.canEdit) {
                    return;
                }
                if (this.projectLevelAPRDiscountSaveTimer) {
                    window.clearTimeout(this.projectLevelAPRDiscountSaveTimer);
                }
                this.projectLevelAPRDiscountSaveTimer = window.setTimeout(function () {
                    self.saveProjectLevelAPRDiscount();
                }, 600);
            },
            flushProjectLevelAPRDiscountSave: function () {
                if (this.projectLevelAPRDiscountSaveTimer) {
                    window.clearTimeout(this.projectLevelAPRDiscountSaveTimer);
                    this.projectLevelAPRDiscountSaveTimer = null;
                }
                this.saveProjectLevelAPRDiscount();
            },
            saveProjectLevelAPRDiscount: function () {
                var self = this;
                this.projectLevelAPRDiscountSaveTimer = null;
                if (!this.canEdit || this.projectLevelAPRDiscount === this.lastSavedProjectLevelAPRDiscount) {
                    return;
                }
                var discount = this.projectLevelAPRDiscount;
                callProductConfigRest("updateProjectLevelAPRDiscount", {
                    objectId: this.objectId,
                    projectLevelAPRDiscount: discount
                }).then(function (data) {
                    if (String(data.code) !== "200") {
                        self.showMessage(data.mess || self.t("emxFramework.JFProductConfigTargetSales.LoadFailed"), "error");
                        return;
                    }
                    self.lastSavedProjectLevelAPRDiscount = discount;
                }).catch(function (error) {
                    self.showMessage(error.message, "error");
                });
            },
            handleTargetSalesInput: function (item, fieldName, event) {
                var self = this;
                item[fieldName] = this.sanitizeNumber(event.target.value);
                event.target.value = item[fieldName];
                if (!this.canEdit || !item || !item.id) {
                    return;
                }
                if (this.targetSalesSaveTimers[item.id]) {
                    window.clearTimeout(this.targetSalesSaveTimers[item.id]);
                }
                this.targetSalesSaveTimers[item.id] = window.setTimeout(function () {
                    self.saveTargetSalesItem(item);
                }, 600);
            },
            flushTargetSalesSave: function (item) {
                if (!item || !item.id) {
                    return;
                }
                if (this.targetSalesSaveTimers[item.id]) {
                    window.clearTimeout(this.targetSalesSaveTimers[item.id]);
                    delete this.targetSalesSaveTimers[item.id];
                }
                this.saveTargetSalesItem(item);
            },
            saveTargetSalesItem: function (item) {
                var self = this;
                if (!this.canEdit || !item || !item.id || this.isItemSaved(item)) {
                    return;
                }
                delete this.targetSalesSaveTimers[item.id];
                var savedValues = {
                    overseas: item.overseas || "",
                    domestic: item.domestic || "",
                    other: item.other || ""
                };
                callProductConfigRest("updateTargetSales", {
                    objectId: this.objectId,
                    targetSalesId: item.id,
                    overseas: savedValues.overseas,
                    domestic: savedValues.domestic,
                    other: savedValues.other
                }).then(function (data) {
                    if (String(data.code) !== "200") {
                        self.showMessage(data.mess || self.t("emxFramework.JFProductConfigTargetSales.LoadFailed"), "error");
                        return;
                    }
                    item._lastSaved = savedValues;
                }).catch(function (error) {
                    self.showMessage(error.message, "error");
                });
            },
            handleVehicleRatioInput: function (config, fieldName, event) {
                var self = this;
                //20260831 update by caipan 配置比例支持输入0-100范围内的小数
                var value = event.target.value.replace(/[^\d.\-]/g, "");
                if (value.charAt(0) === ".") {
                    value = "0" + value;
                }
                config[fieldName] = value;
                event.target.value = config[fieldName];
                this.validateVehicleRatioField(config, fieldName, true);
                if (!this.canEdit || !config || !config.id) {
                    return;
                }
                if (this.vehicleRatioSaveTimers[config.id]) {
                    window.clearTimeout(this.vehicleRatioSaveTimers[config.id]);
                }
                this.vehicleRatioSaveTimers[config.id] = window.setTimeout(function () {
                    self.saveVehicleRatio(config);
                }, 600);
            },
            flushVehicleRatioSave: function (config) {
                if (!config || !config.id) {
                    return;
                }
                if (this.vehicleRatioSaveTimers[config.id]) {
                    window.clearTimeout(this.vehicleRatioSaveTimers[config.id]);
                    delete this.vehicleRatioSaveTimers[config.id];
                }
                this.saveVehicleRatio(config);
            },
            saveVehicleRatio: function (config) {
                var self = this;
                if (!this.canEdit || !config || !config.id || this.isItemSaved(config)) {
                    return;
                }
                if (!this.validateVehicleRatio(config, true)) {
                    return;
                }
                delete this.vehicleRatioSaveTimers[config.id];
                var savedValues = {
                    overseas: config.overseas || "",
                    domestic: config.domestic || "",
                    other: config.other || ""
                };
                callProductConfigRest("updateVehicleConfigSalesRatio", {
                    objectId: this.objectId,
                    vehicleConfigId: config.id,
                    overseas: savedValues.overseas,
                    domestic: savedValues.domestic,
                    other: savedValues.other
                }).then(function (data) {
                    if (String(data.code) !== "200") {
                        self.showMessage(data.mess || self.t("emxFramework.JFProductConfigTargetSales.LoadFailed"), "error");
                        return;
                    }
                    config._lastSaved = savedValues;
                }).catch(function (error) {
                    self.showMessage(error.message, "error");
                });
            },
            validateVehicleRatio: function (config, showError) {
                return this.validateVehicleRatioField(config, "overseas", showError)
                    && this.validateVehicleRatioField(config, "domestic", showError)
                    && this.validateVehicleRatioField(config, "other", showError);
            },
            validateVehicleRatioField: function (config, fieldName, showError) {
                if (!config._invalidFields) {
                    config._invalidFields = {};
                }
                var value = config[fieldName] || "";
                var valid = value === "" || /^\d+(?:\.\d+)?$/.test(value) && parseFloat(value) >= 0 && parseFloat(value) <= 100;
                config._invalidFields[fieldName] = !valid;
                if (!valid && showError) {
                    this.showMessage(this.t("emxFramework.JFProductConfigTargetSales.RatioRangeInvalid"), "error");
                } else if (valid) {
                    this.clearRatioRangeMessageIfValid();
                }
                return valid;
            },
            clearRatioRangeMessageIfValid: function () {
                if (this.message !== this.t("emxFramework.JFProductConfigTargetSales.RatioRangeInvalid")) {
                    return;
                }
                var hasInvalid = this.vehicleConfigs.some(function (config) {
                    var invalidFields = config._invalidFields || {};
                    return invalidFields.overseas || invalidFields.domestic || invalidFields.other;
                });
                if (!hasInvalid) {
                    this.clearMessage();
                }
            },
            isVehicleRatioFieldInvalid: function (config, fieldName) {
                return !!(config && config._invalidFields && config._invalidFields[fieldName]);
            },
            isItemSaved: function (item) {
                var lastSaved = item._lastSaved || {};
                return (item.overseas || "") === (lastSaved.overseas || "")
                    && (item.domestic || "") === (lastSaved.domestic || "")
                    && (item.other || "") === (lastSaved.other || "");
            },
            getPartLink: function (part) {
                return get3DSpaceBase() + "/common/emxTree.jsp?objectId=" + encodeURIComponent(part.id || "");
            },
            openPartNavigator: function (part) {
                openPartNavigatorLauncher(part.id);
            },
            getAnnualQuantity: function (part, targetSalesId) {
                return part.annualQuantities && part.annualQuantities[targetSalesId] ? part.annualQuantities[targetSalesId] : "";
            },
            setAnnualQuantity: function (part, targetSalesId, value) {
                if (!part.annualQuantities) {
                    part.annualQuantities = {};
                }
                part.annualQuantities[targetSalesId] = this.sanitizeNumber(value);
            },
            updatePartAnnualQuantity: function (part, targetSales) {
                var self = this;
                if (!this.canEdit || !part || !targetSales) {
                    return;
                }
                callProductConfigRest("updateTargetSalesPartQuantity", {
                    objectId: this.objectId,
                    targetSalesId: targetSales.id,
                    partId: part.id,
                    annualQuantity: this.getAnnualQuantity(part, targetSales.id)
                }).then(function (data) {
                    if (String(data.code) !== "200") {
                        self.showMessage(data.mess || self.t("emxFramework.JFProductConfigTargetSales.LoadFailed"), "error");
                        return;
                    }
                    //20260831 update by caipan 手工修改汇总销量保存成功后刷新当前零件比例
                    part.ratio = data.ratio || "";
                }).catch(function (error) {
                    self.showMessage(error.message, "error");
                });
            },
            calculateSummary: function () {
                if (!this.canEdit || this.loading) {
                    return;
                }
                this.calculateDialogVisible = true;
            },
            closeCalculateSummaryDialog: function () {
                this.calculateDialogVisible = false;
            },
            runCalculateSummary: function (overwrite) {
                var self = this;
                if (!this.canEdit || this.loading) {
                    return;
                }
                this.calculateDialogVisible = false;
                this.loading = true;
                callProductConfigRest("calculateTargetSalesSummary", {
                    objectId: this.objectId,
                    overwrite: overwrite
                }).then(function (data) {
                    if (String(data.code) !== "200") {
                        self.showMessage(data.mess || self.t("emxFramework.JFProductConfigTargetSales.LoadFailed"), "error");
                        return;
                    }
                    self.canEdit = data.canEdit === true || String(data.canEdit) === "true";
                    self.projectLevelAPRDiscount = data.projectLevelAPRDiscount || "";
                    self.lastSavedProjectLevelAPRDiscount = self.projectLevelAPRDiscount;
                    self.targetSales = data.targetSales || [];
                    self.vehicleConfigs = data.vehicleConfigs || [];
                    self.parts = data.parts || [];
                    self.managerTasks = data.managerTasks || self.managerTasks;
                    self.selectedPartIds = [];
                    self.selectedTargetSalesIds = [];
                    self.selectedManagerTaskIds = [];
                    self.targetSales.forEach(self.markTargetSalesSaved);
                    self.vehicleConfigs.forEach(self.markVehicleRatioSaved);
                }).catch(function (error) {
                    self.showMessage(error.message, "error");
                }).then(function () {
                    self.loading = false;
                });
            },
            exportSummary: function () {
                if (this.loading || !this.canEdit || !this.objectId) {
                    return;
                }
                var self = this;
                var cookieName = "JFProductConfigTargetSalesExportStatus";
                clearCookie(cookieName);
                this.loading = true;
                var frameId = "JFProductConfigTargetSalesExportFrame";
                var frame = document.getElementById(frameId);
                if (!frame) {
                    frame = document.createElement("iframe");
                    frame.id = frameId;
                    frame.name = frameId;
                    frame.style.display = "none";
                    document.body.appendChild(frame);
                }
                frame.src = get3DSpaceBase() + "/common/JF_ProductConfigTargetSalesExport.jsp?objectId=" + encodeURIComponent(this.objectId) + "&timeStamp=" + new Date().getTime();
                var startedAt = new Date().getTime();
                var timer = window.setInterval(function () {
                    var status = getCookieValue(cookieName);
                    if (status) {
                        window.clearInterval(timer);
                        clearCookie(cookieName);
                        self.loading = false;
                        if (status.indexOf("error:") === 0) {
                            self.showMessage(status.substring(6), "error");
                        }
                    } else if (new Date().getTime() - startedAt > 120000) {
                        window.clearInterval(timer);
                        self.loading = false;
                    }
                }, 500);
            },
            openSummaryImportFile: function () {
                if (this.loading || !this.canEdit) {
                    return;
                }
                if (this.$refs.summaryImportFile) {
                    this.$refs.summaryImportFile.value = "";
                    this.$refs.summaryImportFile.click();
                }
            },
            importSummary: function (event) {
                var file = event.target.files && event.target.files.length > 0 ? event.target.files[0] : null;
                if (!this.canEdit || !file) {
                    return;
                }
                var self = this;
                this.loading = true;
                uploadTargetSalesSummaryExcel(this.objectId, file).then(function (data) {
                    if (String(data.code) !== "200") {
                        self.showMessage(data.mess || self.t("emxFramework.JFProductConfigTargetSales.ImportFailed"), "error");
                        return;
                    }
                    self.loadData();
                }).catch(function (error) {
                    self.showMessage(error.message, "error");
                }).then(function () {
                    self.loading = false;
                    if (event.target) {
                        event.target.value = "";
                    }
                });
            },
            openSubmitDialog: function () {
                if (this.loading || !this.canEdit || !this.objectId) {
                    return;
                }
                this.approvalComment = "";
                this.submitDialogVisible = true;
            },
            closeSubmitDialog: function () {
                if (this.loading) {
                    return;
                }
                this.submitDialogVisible = false;
                this.approvalComment = "";
            },
            submitTargetSales: function () {
                var self = this;
                var approvalComment = (this.approvalComment || "").trim();
                if (this.loading || !this.canEdit || !this.objectId || !this.submitDialogVisible) {
                    return;
                }
                if (!approvalComment) {
                    this.showMessage(this.t("emxFramework.JFProductConfigTargetSales.ApprovalCommentRequired"), "error");
                    return;
                }
                this.loading = true;
                callProductConfigRest("submitTargetSales", {objectId: this.objectId, approvalComment: approvalComment})
                    .then(function (data) {
                        if (String(data.code) !== "200") {
                            self.showMessage(data.mess || self.t("emxFramework.JFProductConfigTargetSales.LoadFailed"), "error");
                            return;
                        }
                        self.canEdit = false;
                        self.submitDialogVisible = false;
                        self.approvalComment = "";
                        self.managerTasks = data.managerTasks || self.managerTasks;
                        self.selectedManagerTaskIds = [];
                        self.showMessage(data.mess || self.t("emxFramework.JFProductConfigTargetSales.SubmitSuccess"), "info");
                    })
                    .catch(function (error) {
                        self.showMessage(error.message, "error");
                    })
                    .then(function () {
                        self.loading = false;
                    });
            },
            showMessage: function (message, type) {
                var self = this;
                this.message = message || "";
                this.messageType = type || "info";
                if (this.messageTimer) {
                    window.clearTimeout(this.messageTimer);
                }
                this.messageTimer = window.setTimeout(function () {
                    self.clearMessage();
                }, type === "error" ? 8000 : 3000);
            },
            clearMessage: function () {
                this.message = "";
                this.messageType = "info";
                if (this.messageTimer) {
                    window.clearTimeout(this.messageTimer);
                    this.messageTimer = null;
                }
            }
        }
    }).mount("#app");
})();
