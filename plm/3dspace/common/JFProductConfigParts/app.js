(function () {
    function getQuery(name) {
        var match = new RegExp("[?&]" + name + "=([^&]*)").exec(window.location.search);
        return match ? decodeURIComponent(match[1].replace(/\+/g, " ")) : "";
    }

    function getInitialObjectId() {
        var objectId = getQuery("objectId") || getQuery("parentOID") || window.sessionStorage.getItem("JFProductConfigParts.objectId") || "";
        if (objectId) {
            window.sessionStorage.setItem("JFProductConfigParts.objectId", objectId);
            return objectId;
        }
        try {
            var topWindow = window.parent && window.parent.getTopWindow ? window.parent.getTopWindow() : null;
            if (topWindow && topWindow.getUWAPref) {
                objectId = topWindow.getUWAPref("objectId") || "";
                if (objectId) {
                    window.sessionStorage.setItem("JFProductConfigParts.objectId", objectId);
                }
                return objectId;
            }
        } catch (e) {
            return "";
        }
        return "";
    }

    function get3DSpaceBase() {
        var marker = "/common/JFProductConfigParts/";
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
        var url = get3DSpaceBase() + "/TWXPublicRest/TWXTicketService" + query;
        return fetch(url, {
            method: "GET",
            credentials: "same-origin",
            headers: {"Accept": "application/json"}
        }).then(parseJsonResponse);
    }

    function postProductConfigMatrixBatch(params, keepalive) {
        var request = fetch(get3DSpaceBase() + "/common/JF_ProductConfigMatrixBatchUpdate.jsp?timeStamp=" + new Date().getTime(), {
            method: "POST",
            credentials: "same-origin",
            keepalive: keepalive === true,
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/json"
            },
            body: JSON.stringify(params || {})
        });
        return keepalive === true ? request : request.then(parseJsonResponse);
    }

    function uploadProductConfigExcel(objectId, file) {
        var formData = new FormData();
        formData.append("objectId", objectId || "");
        formData.append("file", file);
        return fetch(get3DSpaceBase() + "/common/JF_ProductConfigImport.jsp?timeStamp=" + new Date().getTime(), {
            method: "POST",
            credentials: "same-origin",
            headers: {"Accept": "application/json"},
            body: formData
        }).then(parseJsonResponse);
    }

    var DEFAULT_I18N = {
        "emxFramework.JFProductConfigParts.Title": "Parts and Configuration",
        "emxFramework.JFProductConfigParts.CreatePositionProduct": "Create Position Product",
        "emxFramework.JFProductConfigParts.DeletePositionProduct": "Delete Position Product",
        "emxFramework.JFProductConfigParts.CreateVehicleConfig": "Create Vehicle Config",
        "emxFramework.JFProductConfigParts.DeleteVehicleConfig": "Delete Vehicle Config",
        "emxFramework.JFProductConfigParts.AddParts": "Add Parts",
        "emxFramework.JFProductConfigParts.RemoveParts": "Remove Parts",
        "emxFramework.JFProductConfigParts.ProductConfigTableIdEmpty": "Current product config table id is empty.",
        "emxFramework.JFProductConfigParts.NoPositionProducts": "No position products.",
        "emxFramework.JFProductConfigParts.SeatPosition": "Seat Position",
        "emxFramework.JFProductConfigParts.VehicleConfig1": "Vehicle Config 1",
        "emxFramework.JFProductConfigParts.VehicleConfig2": "Vehicle Config 2",
        "emxFramework.JFProductConfigParts.VehicleConfig3": "Vehicle Config 3",
        "emxFramework.JFProductConfigParts.VehicleConfig4": "Vehicle Config 4",
        "emxFramework.JFProductConfigParts.VehicleConfigName": "Vehicle Config Name",
        "emxFramework.JFProductConfigParts.VehicleConfigInfo": "Config Info",
        "emxFramework.JFProductConfigParts.CopyFromVehicleConfig": "Copy From Existing Config",
        "emxFramework.JFProductConfigParts.NoCopy": "No Copy",
        "emxFramework.JFProductConfigParts.VehicleConfigNameRequired": "Vehicle config name is required.",
        "emxFramework.JFProductConfigParts.VehicleConfigInfoRequired": "Config info is required.",
        "emxFramework.JFProductConfigParts.VehicleConfigNameExists": "Vehicle config name already exists.",
        "emxFramework.JFProductConfigParts.SelectVehicleConfigFirst": "Please select a vehicle config first.",
        "emxFramework.JFProductConfigParts.DeleteVehicleConfigConfirm": "Delete selected vehicle config?",
        "emxFramework.JFProductConfigParts.Quantity": "Quantity",
        "emxFramework.JFProductConfigParts.QuantityNumberOnly": "Quantity can only contain numbers.",
        "emxFramework.JFProductConfigParts.OptionalOrNot": "Optional",
        "emxFramework.JFProductConfigParts.PartNumber": "Part Number",
        "emxFramework.JFProductConfigParts.PartCNName": "Part CN Name",
        "emxFramework.JFProductConfigParts.CustomerPartNumber": "Customer Part Number",
        "emxFramework.Attribute.JFCustomerPartName": "Customer Part Name",
        "emxFramework.JFProductConfigParts.AssemblyLevel": "Assembly Level",
        "emxFramework.JFProductConfigParts.Domestic1Brazil": "Domestic 1/Brazil",
        "emxFramework.JFProductConfigParts.Domestic2Uzbekistan": "Domestic 2/Uzbekistan",
        "emxFramework.JFProductConfigParts.NoParts": "No parts.",
        "emxFramework.JFProductConfigParts.RequiredNote": "Red labels are required fields.",
        "emxFramework.JFProductConfigParts.PositionProductName": "Position Product Name",
        "emxFramework.JFProductConfigParts.Description": "Description",
        "emxFramework.JFProductConfigParts.OK": "OK",
        "emxFramework.JFProductConfigParts.Apply": "Apply",
        "emxFramework.JFProductConfigParts.Cancel": "Cancel",
        "emxFramework.JFProductConfigParts.Processing": "Processing, please wait...",
        "emxFramework.JFProductConfigParts.LoadFailed": "Load failed.",
        "emxFramework.JFProductConfigParts.CreateFailed": "Create failed.",
        "emxFramework.JFProductConfigParts.PositionProductNameRequired": "Position product name is required.",
        "emxFramework.JFProductConfigParts.SelectPositionProductFirst": "Please select a position product first.",
        "emxFramework.JFProductConfigParts.SelectPartsFirst": "Please select parts first.",
        "emxFramework.JFProductConfigParts.Type": "Type",
        "emxFramework.JFProductConfigParts.DeletePositionProductConfirm": "Delete selected position product?",
        "emxFramework.JFProductConfigParts.PositionProductNameExists": "Position product name already exists.",
        "emxFramework.JFProductConfigParts.PositionProductNameNotEditable": "Only non-whole-chair position product names can be edited.",
        "emxFramework.JFProductConfigParts.ObjectCountUnit": "objects",
        "emxFramework.JFProductConfigParts.BatchUpdate": "Batch Update",
        "emxFramework.JFProductConfigParts.BatchField": "Field",
        "emxFramework.JFProductConfigParts.BatchValue": "Value",
        "emxFramework.JFProductConfigParts.DragSort": "Drag to sort",
        "emxFramework.JFProductConfigParts.Import": "Import",
        "emxFramework.JFProductConfigParts.Export": "Export",
        "emxFramework.JFProductConfigParts.ImportFailed": "Import failed.",
        "emxFramework.JFProductConfigParts.DownloadErrorExcel": "Download error Excel",
        "emxFramework.JFProductConfigParts.NoEditAccess": "No edit access."
    };

    function loadI18n() {
        return fetch("./i18n.jsp?timeStamp=" + new Date().getTime(), {
            method: "GET",
            credentials: "same-origin",
            headers: {"Accept": "application/json"}
        }).then(parseJsonResponse);
    }

    function getFirstOptionValue(options) {
        return options && options.length > 0 ? options[0].value : "";
    }

    function getOptionValueOrFirst(options, preferredValue) {
        if (!options || options.length === 0) {
            return "";
        }
        return options.some(function (option) { return option.value === preferredValue; }) ? preferredValue : options[0].value;
    }

    function normalizePosition(raw) {
        var parts = raw.parts || [];
        var partCount = raw.partCount !== undefined && raw.partCount !== null ? parseInt(raw.partCount, 10) : parts.length;
        return {
            id: raw.id || raw["id"],
            name: raw.name || raw["name"],
            title: raw["attribute[Title]"] || raw.title || raw.name || "",
            ppType: raw["attribute[JFPPType]"] || raw.ppType || "",
            //20260826 update by caipan 保留位置产品原始key用于跨语言唯一性校验
            ppTitle: raw["attribute[JFPPTitle]"] || raw.ppTitle || "",
            description: raw.description || "",
            partCount: isNaN(partCount) ? parts.length : partCount,
            collapsed: raw.collapsed !== undefined ? raw.collapsed : true,
            partsLoaded: raw.partsLoaded === true,
            loadingParts: false,
            parts: parts.map(function (part) {
                var partNumber = part["attribute[EnterpriseExtension.V_PartNumber]"] || "";
                var partName = part.name || part["name"] || "";
                return {
                    id: part.id || part["id"],
                    relId: part["id[connection]"] || "",
                    name: partName,
                    partNumber: partNumber,
                    partDisplayNumber: partNumber || partName,
                    partNameCN: part["attribute[JF_VPMReference.JF_PartNameCN]"] || "",
                    customerPartNumber: part.customerPartNumber || "",
                    customerPartName: part.customerPartName || "",
                    partLevel: part.assemblyLevel || "",
                    order: part.order || "",
                    matrix: part.matrix || {}
                };
            })
        };
    }

    function normalizeVehicleConfig(raw) {
        return {
            id: raw.id || raw["id"],
            name: raw.name || raw["name"],
            title: raw.title || raw["attribute[Title]"] || "",
            vcInfo: raw.vcInfo || raw["attribute[JFVCInfo]"] || "",
            order: raw.order || raw["attribute[JFOrder]"] || ""
        };
    }

    function openPartSearchLauncher(objectId, positionIds) {
        var frameId = "JFProductConfigPartSearchLauncher";
        var frame = document.getElementById(frameId);
        if (!frame) {
            frame = document.createElement("iframe");
            frame.id = frameId;
            frame.name = frameId;
            frame.style.display = "none";
            document.body.appendChild(frame);
        }
        frame.src = get3DSpaceBase() + "/common/JF_ProductConfigPartSearchLauncher.jsp?objectId=" + encodeURIComponent(objectId) + "&positionIds=" + encodeURIComponent(positionIds.join(",")) + "&timeStamp=" + new Date().getTime();
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

    function getColumnWidthStorageKey(objectId) {
        return "JFProductConfigParts.columnWidths.v2." + (objectId || "default");
    }

    function loadStoredColumnWidths(objectId) {
        try {
            var text = window.localStorage.getItem(getColumnWidthStorageKey(objectId));
            return text ? JSON.parse(text) : {};
        } catch (e) {
            return {};
        }
    }

    function saveStoredColumnWidths(objectId, columnWidths) {
        try {
            window.localStorage.setItem(getColumnWidthStorageKey(objectId), JSON.stringify(columnWidths || {}));
        } catch (e) {
        }
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

    if (!window.Vue || !window.Vue.createApp) {
        throw new Error("Vue runtime is unavailable.");
    }

    window.Vue.createApp({
        data: function () {
            var objectId = getInitialObjectId();
            return {
                objectId: objectId,
                objectName: getQuery("objectName"),
                positions: [],
                vehicleConfigs: [],
                selectedPositionIds: [],
                selectedPartRelIds: [],
                selectedVehicleConfigId: "",
                selectedVehicleConfigIds: [],
                positionDialogVisible: false,
                positionForm: {ppType: "", ppTitle: "", customTitle: "", description: ""},
                positionMeta: {ppTypes: [], ppTitles: []},
                matrixMeta: {optionalOptions: [{value: "", label: ""}]},
                vehicleDialogVisible: false,
                vehicleForm: {title: "", vcInfo: "", copyFromId: ""},
                draggedVehicleConfigId: "",
                draggedPartRelId: "",
                draggedPositionId: "",
                dialogPosition: null,
                dragState: null,
                message: "",
                messageType: "info",
                messageLink: null,
                messageTimer: null,
                loading: false,
                canEdit: false,
                columnWidths: loadStoredColumnWidths(objectId),
                columnResizeState: null,
                pendingMatrixChanges: {},
                matrixSaveTimer: null,
                matrixSaving: false,
                inFlightMatrixBatch: [],
                matrixSavePromise: null,
                dataLoaded: false,
                contextUser: "",
                batchEditor: {positionId: "", configId: "", fieldName: "", value: ""},
                i18n: Object.assign({}, DEFAULT_I18N)
            };
        },
        mounted: function () {
            var self = this;
            var refreshHandler = function (event) {
                if (event.data && event.data.type === "JF_PRODUCT_CONFIG_PARTS_CHANGED") {
                    if (event.data.code !== "200" && event.data.message) {
                        self.showMessage(event.data.message, "error");
                        self.loadData();
                        return;
                    }
                    self.loadData(event.data.message || "");
                }
            };
            window.addEventListener("message", refreshHandler);
            if (window.top && window.top !== window && window.top.addEventListener) {
                window.top.addEventListener("message", refreshHandler);
            }
            window.addEventListener("beforeunload", function () {
                self.flushPendingMatrixChanges(true);
            });
            window.addEventListener("pagehide", function () {
                self.flushPendingMatrixChanges(true);
            });
            loadI18n()
                .then(function (data) {
                    self.i18n = Object.assign({}, DEFAULT_I18N, data || {});
                    document.title = self.t("emxFramework.JFProductConfigParts.Title");
                })
                .catch(function () {
                    document.title = self.t("emxFramework.JFProductConfigParts.Title");
                })
                .then(function () {
                    return callProductConfigRest("getPositionProductCreateMeta", {});
                })
                .then(function (data) {
                    if (String(data.code) === "200") {
                        self.positionMeta = {
                            ppTypes: data.ppTypes || [],
                            ppTitles: data.ppTitles || []
                        };
                        self.positionForm = self.getDefaultPositionForm();
                    }
                })
                .catch(function (error) {
                    self.showMessage(error.message, "error");
                })
                .then(function () {
                    return callProductConfigRest("getProductConfigMatrixMeta", {});
                })
                .then(function (data) {
                    if (String(data.code) === "200") {
                        self.matrixMeta = {
                            optionalOptions: data.optionalOptions || [{value: "", label: ""}]
                        };
                    }
                })
                .catch(function (error) {
                    self.showMessage(error.message, "error");
                })
                .then(function () {
                    return self.loadData();
                })
                .then(function () {
                    var restored = self.restorePendingMatrixChanges();
                    return self.submitPendingMatrixChanges().then(function () {
                        return restored ? self.loadData() : null;
                    });
                })
                .catch(function () {
                    // The save method already displays the server error and keeps the changes for retry.
                });
        },
        watch: {
            selectedPositionIds: function () {
                this.selectedPartRelIds = [];
            }
        },
        methods: {
            modalStyle: function () {
                if (!this.dialogPosition) {
                    return {};
                }
                return {
                    left: this.dialogPosition.left + "px",
                    top: this.dialogPosition.top + "px",
                    transform: "none"
                };
            },
            t: function (key) {
                return this.i18n[key] || DEFAULT_I18N[key] || key;
            },
            isCompactColumn: function (key) {
                return key === "assemblyLevel" || key.indexOf("quantity-") === 0 || key.indexOf("optional-") === 0;
            },
            getColumnMinWidth: function (key) {
                return this.isCompactColumn(key) ? 45 : 150;
            },
            getDefaultColumnWidth: function (key, headerText) {
                if (this.isCompactColumn(key)) {
                    return 60;
                }
                var text = String(headerText || "");
                var width = 36;
                for (var i = 0; i < text.length; i++) {
                    width += text.charCodeAt(i) > 255 ? 14 : 8;
                }
                return Math.max(this.getColumnMinWidth(key), width);
            },
            getColumnWidth: function (key, headerText) {
                if (!this.columnWidths[key]) {
                    this.columnWidths[key] = this.getDefaultColumnWidth(key, headerText);
                }
                return this.columnWidths[key];
            },
            startColumnResize: function (event, key) {
                var self = this;
                var startWidth = this.getColumnWidth(key);
                this.columnResizeState = {
                    key: key,
                    startX: event.clientX,
                    startWidth: startWidth
                };
                var moveHandler = function (moveEvent) {
                    if (!self.columnResizeState) {
                        return;
                    }
                    var nextWidth = self.columnResizeState.startWidth + moveEvent.clientX - self.columnResizeState.startX;
                    self.columnWidths[self.columnResizeState.key] = Math.max(self.getColumnMinWidth(self.columnResizeState.key), nextWidth);
                };
                var upHandler = function () {
                    saveStoredColumnWidths(self.objectId, self.columnWidths);
                    self.columnResizeState = null;
                    document.removeEventListener("mousemove", moveHandler);
                    document.removeEventListener("mouseup", upHandler);
                };
                document.addEventListener("mousemove", moveHandler);
                document.addEventListener("mouseup", upHandler);
            },
            getDefaultPositionForm: function () {
                return {
                    ppType: getOptionValueOrFirst(this.positionMeta.ppTypes, "wholeChair"),
                    ppTitle: getFirstOptionValue(this.positionMeta.ppTitles),
                    customTitle: "",
                    description: ""
                };
            },
            wholeChairTitleOptions: function () {
                return this.positionMeta.ppTitles;
            },
            isWholeChairPosition: function () {
                return this.positionForm.ppType === "wholeChair";
            },
            getPositionTitleValue: function () {
                if (!this.isWholeChairPosition()) {
                    return this.positionForm.customTitle;
                }
                var selectedOption = this.positionMeta.ppTitles.find(function (option) {
                    return option.value === this.positionForm.ppTitle;
                }, this);
                return selectedOption ? selectedOption.label : "";
            },
            getPositionTitleRangeValue: function () {
                return this.isWholeChairPosition() ? this.positionForm.ppTitle : this.positionForm.customTitle;
            },
            showMessage: function (message, type) {
                if (this.messageTimer) {
                    window.clearTimeout(this.messageTimer);
                    this.messageTimer = null;
                }
                this.message = message || "";
                this.messageType = type || "info";
                this.messageLink = null;
            },
            clearMessage: function () {
                if (this.messageTimer) {
                    window.clearTimeout(this.messageTimer);
                    this.messageTimer = null;
                }
                this.message = "";
                this.messageLink = null;
            },
            hasEditAccess: function () {
                if (!this.canEdit) {
                    this.showMessage(this.t("emxFramework.JFProductConfigParts.NoEditAccess"), "error");
                    return false;
                }
                return true;
            },
            isPositionSelected: function (positionId) {
                return this.selectedPositionIds.indexOf(positionId) > -1;
            },
            togglePosition: function (position) {
                position.collapsed = !position.collapsed;
                if (!position.collapsed && !position.partsLoaded) {
                    this.loadPositionParts(position);
                }
            },
            loadPositionParts: function (position, forceReload) {
                if (!position || position.loadingParts || (position.partsLoaded && forceReload !== true)) {
                    return;
                }
                var self = this;
                position.loadingParts = true;
                callProductConfigRest("getPositionPartsConfigData", {
                    objectId: this.objectId,
                    positionId: position.id
                }).then(function (data) {
                    if (String(data.code) !== "200") {
                        self.showMessage(data.mess || self.t("emxFramework.JFProductConfigParts.LoadFailed"), "error");
                        return;
                    }
                    var loadedPosition = normalizePosition(data.position || {});
                    loadedPosition.collapsed = position.collapsed;
                    loadedPosition.partsLoaded = true;
                    var index = self.positions.findIndex(function (item) { return item.id === position.id; });
                    if (index > -1) {
                        self.positions[index] = loadedPosition;
                    }
                }).catch(function (error) {
                    self.showMessage(error.message, "error");
                }).then(function () {
                    position.loadingParts = false;
                });
            },
            applyPositions: function (rawPositions, reloadLoadedParts, keepLoadedParts) {
                var previousPositions = this.positions || [];
                var loadedPositionIds = this.getLoadedPositionIds();
                var previousById = {};
                previousPositions.forEach(function (position) {
                    previousById[position.id] = position;
                });
                this.positions = (rawPositions || []).map(function (rawPosition) {
                    var position = normalizePosition(rawPosition);
                    var previous = previousById[position.id];
                    if (previous) {
                        position.collapsed = previous.collapsed;
                        position.partsLoaded = keepLoadedParts === false ? false : previous.partsLoaded;
                        position.loadingParts = false;
                        position.parts = keepLoadedParts === false ? position.parts : (previous.partsLoaded ? previous.parts : position.parts);
                    }
                    return position;
                });
                if (reloadLoadedParts === true) {
                    this.reloadLoadedPositions(loadedPositionIds);
                }
            },
            reloadLoadedPositions: function (positionIds) {
                (positionIds || []).forEach(function (positionId) {
                    var position = this.positions.find(function (item) { return item.id === positionId; });
                    if (position && position.collapsed) {
                        position.partsLoaded = false;
                    } else if (position) {
                        this.loadPositionParts(position, true);
                    }
                }, this);
            },
            isPositionTitleEditable: function (position) {
                return this.canEdit && position && position.ppType === "nonWholeChair";
            },
            loadData: function (successMessage, forceFullReload) {
                var self = this;
                if (!this.objectId) {
                    return Promise.resolve();
                }
                if (this.dataLoaded && (this.matrixSaving || Object.keys(this.pendingMatrixChanges).length > 0)) {
                    return this.submitPendingMatrixChanges().then(function () {
                        return self.loadData(successMessage, forceFullReload);
                    }).catch(function () {
                        // Keep the current page data when pending changes cannot be saved.
                    });
                }
                this.loading = true;
                return callProductConfigRest("getPartsConfigData", {objectId: this.objectId})
                    .then(function (data) {
                        if (String(data.code) !== "200") {
                            self.showMessage(data.mess || self.t("emxFramework.JFProductConfigParts.LoadFailed"), "error");
                            return;
                        }
                        self.contextUser = data.contextUser || "";
                        self.canEdit = data.canEdit === true || String(data.canEdit) === "true";
                        self.vehicleConfigs = (data.vehicleConfigs || []).map(normalizeVehicleConfig);
                        self.applyPositions(data.positions || [], true, forceFullReload === true ? false : true);
                        self.selectedPositionIds = self.selectedPositionIds.filter(function (positionId) {
                            return self.positions.some(function (position) { return position.id === positionId; });
                        });
                        self.selectedPartRelIds = self.selectedPartRelIds.filter(function (relId) {
                            return self.positions.some(function (position) {
                                return position.parts.some(function (part) { return part.relId === relId; });
                            });
                        });
                        if (self.vehicleConfigs.length === 0) {
                            self.selectedVehicleConfigId = "";
                        } else if (!self.selectedVehicleConfigId || !self.vehicleConfigs.some(function (config) { return config.id === self.selectedVehicleConfigId; })) {
                            self.selectedVehicleConfigId = self.vehicleConfigs[0].id;
                        }
                        self.selectedVehicleConfigIds = self.selectedVehicleConfigIds.filter(function (configId) {
                            return self.vehicleConfigs.some(function (config) { return config.id === configId; });
                        });
                        if (successMessage) {
                            self.showMessage(successMessage, "info");
                        }
                        self.dataLoaded = true;
                    })
                    .catch(function (error) {
                        self.showMessage(error.message, "error");
                    })
                    .then(function () {
                        self.loading = false;
                    });
            },
            openPositionDialog: function () {
                if (this.loading) {
                    return;
                }
                if (!this.hasEditAccess()) {
                    return;
                }
                this.positionForm = this.getDefaultPositionForm();
                this.dialogPosition = null;
                this.positionDialogVisible = true;
            },
            closePositionDialog: function () {
                this.positionDialogVisible = false;
                this.stopDragDialog();
            },
            openVehicleDialog: function () {
                if (this.loading) {
                    return;
                }
                if (!this.hasEditAccess()) {
                    return;
                }
                if (this.positions.length === 0) {
                    this.showMessage(this.t("emxFramework.JFProductConfigParts.SelectPositionProductFirst"), "error");
                    return;
                }
                var self = this;
                this.loading = true;
                callProductConfigRest("getVehicleConfigCreateMeta", {objectId: this.objectId})
                    .then(function (data) {
                        if (String(data.code) !== "200") {
                            self.showMessage(data.mess || self.t("emxFramework.JFProductConfigParts.LoadFailed"), "error");
                            return;
                        }
                        self.vehicleConfigs = (data.vehicleConfigs || []).map(normalizeVehicleConfig);
                        self.vehicleForm = {
                            title: data.defaultTitle || "",
                            vcInfo: "",
                            copyFromId: ""
                        };
                        self.dialogPosition = null;
                        self.vehicleDialogVisible = true;
                    })
                    .catch(function (error) {
                        self.showMessage(error.message, "error");
                    })
                    .then(function () {
                        self.loading = false;
                    });
            },
            closeVehicleDialog: function () {
                this.vehicleDialogVisible = false;
                this.stopDragDialog();
            },
            startDragDialog: function (event) {
                var modal = event.currentTarget.parentElement;
                var rect = modal.getBoundingClientRect();
                this.dialogPosition = {left: rect.left, top: rect.top};
                this.dragState = {
                    offsetX: event.clientX - rect.left,
                    offsetY: event.clientY - rect.top
                };
                document.addEventListener("mousemove", this.dragDialog);
                document.addEventListener("mouseup", this.stopDragDialog);
                event.preventDefault();
            },
            dragDialog: function (event) {
                if (!this.dragState) {
                    return;
                }
                var left = event.clientX - this.dragState.offsetX;
                var top = event.clientY - this.dragState.offsetY;
                this.dialogPosition = {
                    left: Math.max(0, Math.min(left, window.innerWidth - 120)),
                    top: Math.max(0, Math.min(top, window.innerHeight - 60))
                };
            },
            stopDragDialog: function () {
                this.dragState = null;
                document.removeEventListener("mousemove", this.dragDialog);
                document.removeEventListener("mouseup", this.stopDragDialog);
            },
            savePosition: function (closeAfterCreate) {
                if (this.loading) {
                    return;
                }
                if (!this.hasEditAccess()) {
                    return;
                }
                var self = this;
                var positionTitle = this.getPositionTitleValue();
                var positionKey = this.getPositionTitleRangeValue();
                if (!positionTitle) {
                    this.showMessage(this.t("emxFramework.JFProductConfigParts.PositionProductNameRequired"), "error");
                    return;
                }
                //20260826 update by caipan 按JFPPTitle原始key校验，避免中英文展示名称不同导致重复创建
                if (this.positions.some(function (position) { return position.ppTitle === positionKey; })) {
                    this.showMessage(this.t("emxFramework.JFProductConfigParts.PositionProductNameExists"), "error");
                    return;
                }
                var positionParams = {
                    objectId: this.objectId,
                    ppType: this.positionForm.ppType,
                    ppTitle: positionKey,
                    title: positionTitle,
                    description: this.positionForm.description || ""
                };
                this.loading = true;
                this.submitPendingMatrixChanges().then(function () {
                    return callProductConfigRest("createPositionProduct", positionParams);
                }).then(function (data) {
                    if (String(data.code) !== "200") {
                        self.showMessage(data.mess || self.t("emxFramework.JFProductConfigParts.CreateFailed"), "error");
                        return;
                    }
                    self.positionDialogVisible = !closeAfterCreate;
                    if (data.positionId && self.selectedPositionIds.indexOf(data.positionId) < 0) {
                        self.selectedPositionIds.push(data.positionId);
                    }
                    self.applyPositions(data.positions || [], false);
                    if (!closeAfterCreate) {
                        self.positionForm = self.getDefaultPositionForm();
                    }
                    self.showMessage("", "info");
                }).catch(function (error) {
                    self.showMessage(error.message, "error");
                }).then(function () {
                    self.loading = false;
                });
            },
            saveVehicleConfig: function (closeAfterCreate) {
                if (this.loading) {
                    return;
                }
                if (!this.hasEditAccess()) {
                    return;
                }
                var self = this;
                var title = (this.vehicleForm.title || "").trim();
                var vcInfo = (this.vehicleForm.vcInfo || "").trim();
                if (!title) {
                    this.showMessage(this.t("emxFramework.JFProductConfigParts.VehicleConfigNameRequired"), "error");
                    return;
                }
                if (!vcInfo) {
                    this.showMessage(this.t("emxFramework.JFProductConfigParts.VehicleConfigInfoRequired"), "error");
                    return;
                }
                if (this.vehicleConfigs.some(function (config) { return config.title === title; })) {
                    this.showMessage(this.t("emxFramework.JFProductConfigParts.VehicleConfigNameExists"), "error");
                    return;
                }
                var vehicleParams = {
                    objectId: this.objectId,
                    title: title,
                    vcInfo: vcInfo,
                    copyFromId: this.vehicleForm.copyFromId || ""
                };
                this.loading = true;
                this.submitPendingMatrixChanges().then(function () {
                    return callProductConfigRest("createVehicleConfig", vehicleParams);
                }).then(function (data) {
                    if (String(data.code) !== "200") {
                        self.showMessage(data.mess || self.t("emxFramework.JFProductConfigParts.CreateFailed"), "error");
                        return;
                    }
                    self.vehicleDialogVisible = !closeAfterCreate;
                    self.vehicleConfigs = (data.vehicleConfigs || []).map(normalizeVehicleConfig);
                    self.applyPositions(data.positions || [], true);
                    self.selectedVehicleConfigId = data.vehicleConfigId || self.selectedVehicleConfigId;
                    if (!closeAfterCreate) {
                        self.vehicleForm = {title: data.defaultTitle || "", vcInfo: "", copyFromId: ""};
                    }
                    self.showMessage("", "info");
                }).catch(function (error) {
                    self.showMessage(error.message, "error");
                }).then(function () {
                    self.loading = false;
                });
            },
            selectVehicleConfig: function (vehicleConfigId) {
                this.selectedVehicleConfigId = vehicleConfigId;
            },
            deletePositionProduct: function () {
                if (this.loading) {
                    return;
                }
                if (!this.hasEditAccess()) {
                    return;
                }
                if (this.selectedPositionIds.length === 0) {
                    this.showMessage(this.t("emxFramework.JFProductConfigParts.SelectPositionProductFirst"), "error");
                    return;
                }
                if (!window.confirm(this.t("emxFramework.JFProductConfigParts.DeletePositionProductConfirm"))) {
                    return;
                }
                var self = this;
                var positionIds = this.selectedPositionIds.slice();
                this.loading = true;
                this.submitPendingMatrixChanges().then(function () {
                    return callProductConfigRest("deletePositionProduct", {
                        objectId: self.objectId,
                        positionIds: positionIds
                    });
                }).then(function (data) {
                    if (String(data.code) !== "200") {
                        self.showMessage(data.mess || self.t("emxFramework.JFProductConfigParts.CreateFailed"), "error");
                        return;
                    }
                    self.applyPositions(data.positions || [], false);
                    self.vehicleConfigs = (data.vehicleConfigs || []).map(normalizeVehicleConfig);
                    self.selectedPositionIds = [];
                    self.selectedPartRelIds = [];
                    self.showMessage("", "info");
                }).catch(function (error) {
                    self.showMessage(error.message, "error");
                }).then(function () {
                    self.loading = false;
                });
            },
            updatePositionProduct: function (position) {
                if (this.loading) {
                    return;
                }
                if (!this.isPositionTitleEditable(position)) {
                    this.showMessage(this.t("emxFramework.JFProductConfigParts.PositionProductNameNotEditable"), "error");
                    this.loadData();
                    return;
                }
                if (!this.hasEditAccess()) {
                    this.loadData();
                    return;
                }
                var self = this;
                var title = (position.title || "").trim();
                if (!title) {
                    this.showMessage(this.t("emxFramework.JFProductConfigParts.PositionProductNameRequired"), "error");
                    this.loadData();
                    return;
                }
                if (this.positions.some(function (item) { return item.id !== position.id && item.title === title; })) {
                    this.showMessage(this.t("emxFramework.JFProductConfigParts.PositionProductNameExists"), "error");
                    this.loadData();
                    return;
                }
                this.loading = true;
                callProductConfigRest("updatePositionProduct", {
                    objectId: this.objectId,
                    positionId: position.id,
                    title: title
                }).then(function (data) {
                    if (String(data.code) !== "200") {
                        self.showMessage(data.mess || self.t("emxFramework.JFProductConfigParts.CreateFailed"), "error");
                        self.loadData();
                        return;
                    }
                    self.showMessage("", "info");
                }).catch(function (error) {
                    self.showMessage(error.message, "error");
                    self.loadData();
                }).then(function () {
                    self.loading = false;
                });
            },
            getMatrixValue: function (part, configId, fieldName) {
                var matrix = part.matrix[configId] || {};
                return matrix[fieldName] || "";
            },
            setMatrixValue: function (part, configId, fieldName, value) {
                if (!part.matrix[configId]) {
                    part.matrix[configId] = {};
                }
                part.matrix[configId][fieldName] = value;
            },
            getLoadedPositionIds: function () {
                return this.positions.filter(function (position) {
                    return position.partsLoaded;
                }).map(function (position) {
                    return position.id;
                });
            },
            mergeLoadedPositions: function (loadedPositions) {
                var self = this;
                (loadedPositions || []).map(normalizePosition).forEach(function (loadedPosition) {
                    var index = self.positions.findIndex(function (position) { return position.id === loadedPosition.id; });
                    if (index > -1) {
                        loadedPosition.collapsed = self.positions[index].collapsed;
                        loadedPosition.partsLoaded = true;
                        self.positions[index] = loadedPosition;
                    }
                });
            },
            moveItemBefore: function (items, idKey, draggedId, targetId, placeAfter) {
                var draggedIndex = items.findIndex(function (item) { return item[idKey] === draggedId; });
                var targetIndex = items.findIndex(function (item) { return item[idKey] === targetId; });
                if (draggedIndex < 0 || targetIndex < 0 || draggedIndex === targetIndex) {
                    return items;
                }
                var draggedItem = items.splice(draggedIndex, 1)[0];
                targetIndex = items.findIndex(function (item) { return item[idKey] === targetId; });
                items.splice(placeAfter ? targetIndex + 1 : targetIndex, 0, draggedItem);
                return items;
            },
            getAdjacentIds: function (items, idKey, movedId) {
                var index = items.findIndex(function (item) { return item[idKey] === movedId; });
                return {
                    previousId: index > 0 ? items[index - 1][idKey] : "",
                    nextId: index > -1 && index < items.length - 1 ? items[index + 1][idKey] : ""
                };
            },
            getMatrixBatchTargets: function (position, part) {
                if (this.selectedPartRelIds.length <= 1 || this.selectedPartRelIds.indexOf(part.relId) < 0) {
                    return [{position: position, part: part}];
                }
                var targets = [];
                this.positions.forEach(function (currentPosition) {
                    if (!currentPosition.partsLoaded) {
                        return;
                    }
                    currentPosition.parts.forEach(function (currentPart) {
                        if (this.selectedPartRelIds.indexOf(currentPart.relId) > -1) {
                            targets.push({position: currentPosition, part: currentPart});
                        }
                    }, this);
                }, this);
                return targets.length > 0 ? targets : [{position: position, part: part}];
            },
            sanitizeQuantityInput: function (event) {
                var value = String(event.target.value || "").replace(/\D/g, "");
                if (event.target.value !== value) {
                    event.target.value = value;
                }
                return value;
            },
            updateVehicleConfig: function (vehicleConfig) {
                if (this.loading) {
                    return;
                }
                if (!this.hasEditAccess()) {
                    this.loadData();
                    return;
                }
                var self = this;
                var title = (vehicleConfig.title || "").trim();
                if (!title) {
                    this.showMessage(this.t("emxFramework.JFProductConfigParts.VehicleConfigNameRequired"), "error");
                    this.loadData();
                    return;
                }
                this.loading = true;
                var vehicleConfigParams = {
                    objectId: this.objectId,
                    vehicleConfigId: vehicleConfig.id,
                    title: title,
                    vcInfo: vehicleConfig.vcInfo || ""
                };
                this.submitPendingMatrixChanges().then(function () {
                    return callProductConfigRest("updateVehicleConfig", vehicleConfigParams);
                }).then(function (data) {
                    if (String(data.code) !== "200") {
                        self.showMessage(data.mess || self.t("emxFramework.JFProductConfigParts.CreateFailed"), "error");
                        self.loadData();
                        return;
                    }
                    self.vehicleConfigs = (data.vehicleConfigs || []).map(normalizeVehicleConfig);
                    self.applyPositions(data.positions || [], false);
                    self.showMessage("", "info");
                }).catch(function (error) {
                    self.showMessage(error.message, "error");
                    self.loadData();
                }).then(function () {
                    self.loading = false;
                });
            },
            startVehicleConfigDrag: function (vehicleConfig) {
                if (!this.canEdit) {
                    return;
                }
                this.draggedVehicleConfigId = vehicleConfig.id;
            },
            dropVehicleConfigBefore: function (targetConfig) {
                if (!this.canEdit) {
                    return;
                }
                if (!this.draggedVehicleConfigId || this.draggedVehicleConfigId === targetConfig.id) {
                    return;
                }
                var configs = this.vehicleConfigs.filter(function (config) {
                    return config.id !== this.draggedVehicleConfigId;
                }, this);
                var targetIndex = configs.findIndex(function (config) {
                    return config.id === targetConfig.id;
                });
                if (targetIndex < 0) {
                    return;
                }
                this.updateVehicleConfigOrder(this.draggedVehicleConfigId, targetIndex > 0 ? configs[targetIndex - 1].id : "", targetConfig.id);
                this.draggedVehicleConfigId = "";
            },
            dropVehicleConfig: function (event, targetConfig) {
                if (!this.canEdit || !this.draggedVehicleConfigId || this.draggedVehicleConfigId === targetConfig.id) {
                    return;
                }
                var placeAfter = event.offsetX > event.currentTarget.offsetWidth / 2;
                this.moveItemBefore(this.vehicleConfigs, "id", this.draggedVehicleConfigId, targetConfig.id, placeAfter);
                var adjacent = this.getAdjacentIds(this.vehicleConfigs, "id", this.draggedVehicleConfigId);
                this.updateVehicleConfigOrder(this.draggedVehicleConfigId, adjacent.previousId, adjacent.nextId);
                this.draggedVehicleConfigId = "";
            },
            dropVehicleConfigToEnd: function () {
                if (!this.canEdit) {
                    return;
                }
                if (!this.draggedVehicleConfigId) {
                    return;
                }
                var configs = this.vehicleConfigs.filter(function (config) {
                    return config.id !== this.draggedVehicleConfigId;
                }, this);
                var previousVehicleConfigId = configs.length > 0 ? configs[configs.length - 1].id : "";
                this.updateVehicleConfigOrder(this.draggedVehicleConfigId, previousVehicleConfigId, "");
                this.draggedVehicleConfigId = "";
            },
            updateVehicleConfigOrder: function (vehicleConfigId, previousVehicleConfigId, nextVehicleConfigId) {
                if (this.loading) {
                    return;
                }
                if (!this.hasEditAccess()) {
                    return;
                }
                var self = this;
                callProductConfigRest("setVehicleConfigOrder", {
                    objectId: this.objectId,
                    vehicleConfigId: vehicleConfigId,
                    previousVehicleConfigId: previousVehicleConfigId,
                    nextVehicleConfigId: nextVehicleConfigId
                }).then(function (data) {
                    if (String(data.code) !== "200") {
                        self.showMessage(data.mess || self.t("emxFramework.JFProductConfigParts.LoadFailed"), "error");
                        return;
                    }
                    self.showMessage("", "info");
                }).catch(function (error) {
                    self.showMessage(error.message, "error");
                }).then(function () {
                    self.draggedVehicleConfigId = "";
                });
            },
            updateMatrixCell: function (position, part, config, fieldName) {
                if (this.loading) {
                    return;
                }
                if (!this.hasEditAccess()) {
                    this.loadData();
                    return;
                }
                var matrix = part.matrix[config.id] || {};
                if (matrix.quantity && !/^\d+$/.test(matrix.quantity)) {
                    this.showMessage(this.t("emxFramework.JFProductConfigParts.QuantityNumberOnly"), "error");
                    return;
                }
                var value = matrix[fieldName] || "";
                this.getMatrixBatchTargets(position, part).forEach(function (target) {
                    this.setMatrixValue(target.part, config.id, fieldName, value);
                    var targetMatrix = target.part.matrix[config.id] || {};
                    if (targetMatrix.quantity && !/^\d+$/.test(targetMatrix.quantity)) {
                        this.showMessage(this.t("emxFramework.JFProductConfigParts.QuantityNumberOnly"), "error");
                        return;
                    }
                    this.queueMatrixCellUpdate(target.position, target.part, config, targetMatrix);
                }, this);
            },
            handleMatrixQuantityInput: function (position, part, config, event) {
                this.setMatrixValue(part, config.id, "quantity", this.sanitizeQuantityInput(event));
                this.updateMatrixCell(position, part, config, "quantity");
            },
            getMatrixChangeKey: function (change) {
                return change.positionId + "|" + change.positionPartRelId + "|" + change.vehicleConfigId;
            },
            getMatrixBackupKey: function () {
                return "JFProductConfigParts.pendingMatrixChanges." + this.objectId + "." + this.contextUser;
            },
            persistPendingMatrixChanges: function () {
                if (!this.objectId || !this.contextUser) {
                    return;
                }
                var self = this;
                var changeMap = {};
                (this.inFlightMatrixBatch || []).forEach(function (change) {
                    changeMap[self.getMatrixChangeKey(change)] = change;
                });
                Object.keys(this.pendingMatrixChanges).forEach(function (key) {
                    changeMap[key] = self.pendingMatrixChanges[key];
                });
                try {
                    if (Object.keys(changeMap).length === 0) {
                        window.localStorage.removeItem(this.getMatrixBackupKey());
                    } else {
                        window.localStorage.setItem(this.getMatrixBackupKey(), JSON.stringify({
                            objectId: this.objectId,
                            contextUser: this.contextUser,
                            changes: changeMap
                        }));
                    }
                } catch (e) {
                    // Browser storage can be unavailable; the normal save queue remains active.
                }
            },
            restorePendingMatrixChanges: function () {
                if (!this.objectId || !this.contextUser) {
                    return false;
                }
                try {
                    var saved = JSON.parse(window.localStorage.getItem(this.getMatrixBackupKey()) || "{}");
                    if (saved.objectId !== this.objectId || !this.contextUser || saved.contextUser !== this.contextUser || !saved.changes) {
                        return false;
                    }
                    var self = this;
                    var restored = false;
                    Object.keys(saved.changes).forEach(function (key) {
                        var change = saved.changes[key];
                        if (change && change.positionId && change.positionPartRelId && change.vehicleConfigId) {
                            self.pendingMatrixChanges[key] = change;
                            restored = true;
                        }
                    });
                    return restored;
                } catch (e) {
                    try {
                        window.localStorage.removeItem(this.getMatrixBackupKey());
                    } catch (ignore) {
                        // Browser storage is unavailable.
                    }
                    return false;
                }
            },
            queueMatrixCellUpdate: function (position, part, config, matrix) {
                var self = this;
                var key = position.id + "|" + part.relId + "|" + config.id;
                this.pendingMatrixChanges[key] = {
                    positionId: position.id,
                    positionPartRelId: part.relId,
                    vehicleConfigId: config.id,
                    quantity: matrix.quantity || "",
                    optional: matrix.optional || ""
                };
                this.persistPendingMatrixChanges();
                if (this.matrixSaveTimer) {
                    window.clearTimeout(this.matrixSaveTimer);
                }
                this.matrixSaveTimer = window.setTimeout(function () {
                    self.submitPendingMatrixChanges().catch(function () {
                        // The save method already displays the server error and keeps the changes for retry.
                    });
                }, 3000);
            },
            submitPendingMatrixChanges: function () {
                return this.flushPendingMatrixChanges(false);
            },
            flushPendingMatrixChanges: function (keepalive) {
                var self = this;
                var changeMap = {};
                if (keepalive) {
                    (this.inFlightMatrixBatch || []).forEach(function (change) {
                        changeMap[self.getMatrixChangeKey(change)] = change;
                    });
                }
                Object.keys(this.pendingMatrixChanges).forEach(function (key) {
                    changeMap[key] = self.pendingMatrixChanges[key];
                });
                var changes = Object.keys(changeMap).map(function (key) {
                    return changeMap[key];
                });
                if (changes.length === 0) {
                    return this.matrixSavePromise || Promise.resolve();
                }
                if (this.matrixSaveTimer) {
                    window.clearTimeout(this.matrixSaveTimer);
                    this.matrixSaveTimer = null;
                }
                this.persistPendingMatrixChanges();
                var params = {
                    objectId: this.objectId,
                    changes: changes
                };
                if (keepalive) {
                    var keepaliveRequests = [];
                    for (var changeIndex = 0; changeIndex < changes.length; changeIndex += 100) {
                        keepaliveRequests.push(postProductConfigMatrixBatch({
                            objectId: this.objectId,
                            changes: changes.slice(changeIndex, changeIndex + 100)
                        }, true).then(parseJsonResponse));
                    }
                    Promise.all(keepaliveRequests).then(function (results) {
                        var allSaved = results.every(function (data) {
                            return String(data.code) === "200";
                        });
                        if (allSaved && !self.matrixSaving) {
                            changes.forEach(function (change) {
                                var key = self.getMatrixChangeKey(change);
                                var pending = self.pendingMatrixChanges[key];
                                if (pending && pending.quantity === change.quantity && pending.optional === change.optional) {
                                    delete self.pendingMatrixChanges[key];
                                }
                            });
                            self.persistPendingMatrixChanges();
                        }
                    }).catch(function () {
                        // The local backup is intentionally retained for the next page load.
                    });
                    return Promise.resolve();
                }
                if (this.matrixSaving) {
                    return this.matrixSavePromise.then(function () {
                        return self.submitPendingMatrixChanges();
                    });
                }
                this.pendingMatrixChanges = {};
                this.matrixSaving = true;
                this.inFlightMatrixBatch = changes;
                this.persistPendingMatrixChanges();
                this.matrixSavePromise = postProductConfigMatrixBatch(params, false).then(function (data) {
                    if (String(data.code) !== "200") {
                        throw new Error(data.mess || self.t("emxFramework.JFProductConfigParts.CreateFailed"));
                    }
                }).then(function () {
                    self.inFlightMatrixBatch = [];
                    self.matrixSaving = false;
                    self.matrixSavePromise = null;
                    self.persistPendingMatrixChanges();
                    self.showMessage("", "info");
                    return self.submitPendingMatrixChanges();
                }, function (error) {
                    changes.forEach(function (change) {
                        var key = self.getMatrixChangeKey(change);
                        if (!self.pendingMatrixChanges[key]) {
                            self.pendingMatrixChanges[key] = change;
                        }
                    });
                    self.inFlightMatrixBatch = [];
                    self.matrixSaving = false;
                    self.matrixSavePromise = null;
                    self.persistPendingMatrixChanges();
                    self.showMessage(error.message, "error");
                    throw error;
                });
                return this.matrixSavePromise;
            },
            openBatchEditor: function (position, config, fieldName) {
                if (!this.hasEditAccess()) {
                    return;
                }
                this.batchEditor = {
                    positionId: position.id,
                    configId: config.id,
                    fieldName: fieldName || "quantity",
                    value: ""
                };
            },
            getPartLink: function (part) {
                return get3DSpaceBase() + "/common/emxTree.jsp?objectId=" + encodeURIComponent(part.id || "");
            },
            openPartNavigator: function (part) {
                openPartNavigatorLauncher(part.id);
            },
            closeBatchEditor: function () {
                this.batchEditor = {positionId: "", configId: "", fieldName: "", value: ""};
            },
            isBatchEditorOpen: function (position) {
                return this.batchEditor.positionId === position.id && this.batchEditor.configId;
            },
            applyBatchEdit: function (position) {
                if (!this.batchEditor.fieldName || !this.batchEditor.configId) {
                    return;
                }
                var config = this.vehicleConfigs.find(function (item) { return item.id === this.batchEditor.configId; }, this);
                if (!config) {
                    return;
                }
                var value = this.batchEditor.value || "";
                if (this.batchEditor.fieldName === "quantity" && value && !/^\d+$/.test(value)) {
                    this.showMessage(this.t("emxFramework.JFProductConfigParts.QuantityNumberOnly"), "error");
                    return;
                }
                position.parts.forEach(function (part) {
                    this.setMatrixValue(part, config.id, this.batchEditor.fieldName, value);
                    this.queueMatrixCellUpdate(position, part, config, part.matrix[config.id] || {});
                }, this);
                this.closeBatchEditor();
                this.submitPendingMatrixChanges().catch(function () {
                    // The save method already displays the server error and keeps the changes for retry.
                });
            },
            deleteVehicleConfig: function () {
                if (this.loading) {
                    return;
                }
                if (!this.hasEditAccess()) {
                    return;
                }
                var self = this;
                var vehicleConfigIds = this.selectedVehicleConfigIds.slice();
                if (vehicleConfigIds.length === 0) {
                    this.showMessage(this.t("emxFramework.JFProductConfigParts.SelectVehicleConfigFirst"), "error");
                    return;
                }
                if (!window.confirm(this.t("emxFramework.JFProductConfigParts.DeleteVehicleConfigConfirm"))) {
                    return;
                }
                this.loading = true;
                this.submitPendingMatrixChanges().then(function () {
                    return callProductConfigRest("deleteVehicleConfig", {
                        objectId: self.objectId,
                        vehicleConfigIds: vehicleConfigIds
                    });
                }).then(function (data) {
                    if (String(data.code) !== "200") {
                        self.showMessage(data.mess || self.t("emxFramework.JFProductConfigParts.CreateFailed"), "error");
                        return;
                    }
                    self.vehicleConfigs = (data.vehicleConfigs || []).map(normalizeVehicleConfig);
                    self.applyPositions(data.positions || [], false);
                    self.selectedVehicleConfigIds = [];
                    self.selectedVehicleConfigId = self.vehicleConfigs.length > 0 ? self.vehicleConfigs[0].id : "";
                    self.showMessage("", "info");
                }).catch(function (error) {
                    self.showMessage(error.message, "error");
                }).then(function () {
                    self.loading = false;
                });
            },
            addParts: function () {
                if (this.loading) {
                    return;
                }
                if (!this.hasEditAccess()) {
                    return;
                }
                if (this.selectedPositionIds.length === 0) {
                    this.showMessage(this.t("emxFramework.JFProductConfigParts.SelectPositionProductFirst"), "error");
                    return;
                }
                var self = this;
                var selectedPositionIds = this.selectedPositionIds.slice();
                this.loading = true;
                this.submitPendingMatrixChanges().then(function () {
                    openPartSearchLauncher(self.objectId, selectedPositionIds);
                }).catch(function () {
                    // The save method already displays the server error.
                }).then(function () {
                    self.loading = false;
                });
            },
            exportExcel: function () {
                if (this.loading || !this.objectId) {
                    return;
                }
                var self = this;
                var cookieName = "JFProductConfigExportStatus";
                clearCookie(cookieName);
                this.loading = true;
                this.submitPendingMatrixChanges().then(function () {
                    var frameId = "JFProductConfigExportFrame";
                    var frame = document.getElementById(frameId);
                    if (!frame) {
                        frame = document.createElement("iframe");
                        frame.id = frameId;
                        frame.name = frameId;
                        frame.style.display = "none";
                        document.body.appendChild(frame);
                    }
                    frame.src = get3DSpaceBase() + "/common/JF_ProductConfigExport.jsp?objectId=" + encodeURIComponent(self.objectId) + "&timeStamp=" + new Date().getTime();
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
                }).catch(function () {
                    self.loading = false;
                });
            },
            openImportFile: function () {
                if (this.loading || !this.hasEditAccess()) {
                    return;
                }
                if (this.$refs.importFile) {
                    this.$refs.importFile.value = "";
                    this.$refs.importFile.click();
                }
            },
            importExcel: function (event) {
                var file = event.target.files && event.target.files.length > 0 ? event.target.files[0] : null;
                if (!file) {
                    return;
                }
                var self = this;
                this.loading = true;
                this.submitPendingMatrixChanges().then(function () {
                    return uploadProductConfigExcel(self.objectId, file);
                }).then(function (data) {
                    if (String(data.code) !== "200") {
                        var message = data.mess || self.t("emxFramework.JFProductConfigParts.ImportFailed");
                        if (data.errorToken) {
                            var url = get3DSpaceBase() + "/common/JF_ProductConfigImportErrorDownload.jsp?token=" + encodeURIComponent(data.errorToken);
                            self.messageLink = {url: url, text: self.t("emxFramework.JFProductConfigParts.DownloadErrorExcel")};
                        }
                        self.showMessage(message, "error");
                        if (data.errorToken) {
                            self.messageLink = {url: get3DSpaceBase() + "/common/JF_ProductConfigImportErrorDownload.jsp?token=" + encodeURIComponent(data.errorToken), text: self.t("emxFramework.JFProductConfigParts.DownloadErrorExcel")};
                        }
                        return;
                    }
                    self.showMessage(data.mess || "", "info");
                    self.loadData(null, true);
                }).catch(function (error) {
                    self.showMessage(error.message, "error");
                }).then(function () {
                    self.loading = false;
                    if (event.target) {
                        event.target.value = "";
                    }
                });
            },
            removeParts: function () {
                if (this.loading) {
                    return;
                }
                if (!this.hasEditAccess()) {
                    return;
                }
                var self = this;
                var partRelIds = this.selectedPartRelIds.slice();
                if (this.selectedPartRelIds.length === 0) {
                    this.showMessage(this.t("emxFramework.JFProductConfigParts.SelectPartsFirst"), "error");
                    return;
                }
                this.loading = true;
                this.submitPendingMatrixChanges().then(function () {
                    return callProductConfigRest("removePartsFromPosition", {
                        objectId: self.objectId,
                        relIds: partRelIds
                    });
                }).then(function (data) {
                    if (String(data.code) !== "200") {
                        self.showMessage(data.mess || self.t("emxFramework.JFProductConfigParts.CreateFailed"), "error");
                        return;
                    }
                    self.applyPositions(data.positions || [], true);
                    self.selectedPartRelIds = [];
                    self.showMessage("", "info");
                }).catch(function (error) {
                    self.showMessage(error.message, "error");
                }).then(function () {
                    self.loading = false;
                });
            },
            startPartDrag: function (position, part) {
                if (!this.canEdit) {
                    return;
                }
                this.draggedPositionId = position.id;
                this.draggedPartRelId = part.relId;
            },
            dropPartBefore: function (position, targetPart) {
                if (!this.canEdit) {
                    return;
                }
                if (!this.draggedPartRelId || this.draggedPartRelId === targetPart.relId || this.draggedPositionId !== position.id) {
                    return;
                }
                var parts = position.parts.filter(function (part) {
                    return part.relId !== this.draggedPartRelId;
                }, this);
                var targetIndex = parts.findIndex(function (part) {
                    return part.relId === targetPart.relId;
                });
                if (targetIndex < 0) {
                    return;
                }
                this.updatePartOrder(position.id, this.draggedPartRelId, targetIndex > 0 ? parts[targetIndex - 1].relId : "", targetPart.relId);
            },
            dropPart: function (event, position, targetPart) {
                if (!this.canEdit || !this.draggedPartRelId || this.draggedPartRelId === targetPart.relId || this.draggedPositionId !== position.id) {
                    return;
                }
                var placeAfter = event.offsetY > event.currentTarget.offsetHeight / 2;
                this.moveItemBefore(position.parts, "relId", this.draggedPartRelId, targetPart.relId, placeAfter);
                var adjacent = this.getAdjacentIds(position.parts, "relId", this.draggedPartRelId);
                this.updatePartOrder(position.id, this.draggedPartRelId, adjacent.previousId, adjacent.nextId);
            },
            dropPartToEnd: function (position) {
                if (!this.canEdit) {
                    return;
                }
                if (!this.draggedPartRelId || this.draggedPositionId !== position.id) {
                    return;
                }
                var parts = position.parts.filter(function (part) {
                    return part.relId !== this.draggedPartRelId;
                }, this);
                var previousRelId = parts.length > 0 ? parts[parts.length - 1].relId : "";
                this.updatePartOrder(position.id, this.draggedPartRelId, previousRelId, "");
            },
            updatePartOrder: function (positionId, relId, previousRelId, nextRelId) {
                if (this.loading) {
                    return;
                }
                if (!this.hasEditAccess()) {
                    return;
                }
                var self = this;
                callProductConfigRest("setPositionPartOrder", {
                    objectId: this.objectId,
                    positionId: positionId,
                    relId: relId,
                    previousRelId: previousRelId,
                    nextRelId: nextRelId
                }).then(function (data) {
                    if (String(data.code) !== "200") {
                        self.showMessage(data.mess || self.t("emxFramework.JFProductConfigParts.LoadFailed"), "error");
                        return;
                    }
                    self.showMessage("", "info");
                }).catch(function (error) {
                    self.showMessage(error.message, "error");
                }).then(function () {
                    self.draggedPartRelId = "";
                    self.draggedPositionId = "";
                });
            }
        }
    }).mount("#app");
}());
