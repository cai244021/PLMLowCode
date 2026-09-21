(function (global) {
    'use strict';

    var SUPPORTED_FORMAT_VERSION = 1;
    var lastNoticeKey = '';
    var lastNoticeAt = 0;

    function clone(value) {
        return JSON.parse(JSON.stringify(value));
    }

    function readMappingPath(source, path) {
        var value = source;
        var segments = String(path || '').replace(/\[(\d+)\]/g, '.$1').split('.').filter(Boolean);
        var blocked = {'__proto__': true, 'prototype': true, 'constructor': true};
        var i;
        for (i = 0; i < segments.length; i += 1) {
            if (blocked[segments[i]] || value === null || typeof value === 'undefined') {
                return undefined;
            }
            value = value[segments[i]];
        }
        return value;
    }

    function resolveMapping(template, source) {
        var exact;
        var result;
        var key;
        if (typeof template === 'string') {
            exact = template.match(/^\$\{([^{}]+)\}$/);
            if (exact) {
                return readMappingPath(source, exact[1]);
            }
            return template.replace(/\$\{([^{}]+)\}/g, function (match, path) {
                var value = readMappingPath(source, path);
                return value === null || typeof value === 'undefined' ? '' : String(value);
            });
        }
        if (Array.isArray(template)) {
            return template.map(function (item) { return resolveMapping(item, source); });
        }
        if (template && typeof template === 'object') {
            result = {};
            for (key in template) {
                if (Object.prototype.hasOwnProperty.call(template, key)) {
                    result[key] = resolveMapping(template[key], source);
                }
            }
            return result;
        }
        return template;
    }

    function notify(level, message) {
        var type = level === 'danger' ? 'error' : (level === 'warn' ? 'warning' : level);
        var text = String(message || '');
        var noticeKey;
        var now;
        type = ['success', 'info', 'warning', 'error'].indexOf(type) >= 0 ? type : 'info';
        noticeKey = type + '\n' + text;
        now = Date.now();
        //20260921 update by caipan AMIS默认错误通知和页面Failure Effect共用出口，同一提示只展示一次
        if (noticeKey === lastNoticeKey && now - lastNoticeAt < 1500) {
            return;
        }
        lastNoticeKey = noticeKey;
        lastNoticeAt = now;
        var ui;
        try {
            ui = global.amisRequire && global.amisRequire('amis-ui');
            if (ui && ui.toast && typeof ui.toast[type] === 'function') {
                ui.toast[type](text);
                return;
            }
        } catch (ignore) {}
        if (type === 'error' && global.alert) {
            global.alert(text);
        }
    }

    function walk(value, visitor) {
        var i;
        var key;
        if (Array.isArray(value)) {
            for (i = 0; i < value.length; i += 1) {
                walk(value[i], visitor);
            }
            return;
        }
        if (!value || typeof value !== 'object') {
            return;
        }
        visitor(value);
        for (key in value) {
            if (Object.prototype.hasOwnProperty.call(value, key)) {
                walk(value[key], visitor);
            }
        }
    }

    function findById(schema, componentId) {
        var found = null;
        walk(schema, function (node) {
            if (!found && node.id === componentId) {
                found = node;
            }
        });
        return found;
    }

    function enableDraggableDialogs(schema) {
        walk(schema, function (node) {
            if (node.type === 'dialog') {
                node.draggable = true;
            }
        });
    }

    function dropTargetClass(componentId) {
        return 'jf-lowcode-drop-' + String(componentId || '').replace(/[^A-Za-z0-9_-]/g, '-');
    }

    function parseDroppedItems(data) {
        var payload = typeof data === 'string' ? JSON.parse(data) : (data || {});
        var items = payload && payload.data && Array.isArray(payload.data.items)
            ? payload.data.items : [];
        return items.map(function (item) {
            return {
                objectId: String(item.objectId || item.physicalId || item.resourceid || '').trim(),
                objectType: String(item.objectType || item.displayType || '').trim(),
                objectTaxonomies: Array.isArray(item.objectTaxonomies) ? item.objectTaxonomies : []
            };
        }).filter(function (item) { return !!item.objectId; });
    }

    function normalizedDropType(value) {
        return String(value || '').replace(/^type_/i, '').toLowerCase();
    }

    function validateDroppedItems(items, acceptedTypes) {
        var accepted = (acceptedTypes || []).map(normalizedDropType).filter(Boolean);
        var invalid;
        if (!items.length) {
            throw new Error('\u62d6\u62fd\u6570\u636e\u4e2d\u672a\u627e\u5230objectId');
        }
        if (items.length > 50) {
            throw new Error('\u4e00\u6b21\u6700\u591a\u62d6\u516550\u4e2a\u5bf9\u8c61');
        }
        if (!accepted.length) {
            return;
        }
        invalid = items.find(function (item) {
            var candidates = [item.objectType].concat(item.objectTaxonomies || [])
                .map(normalizedDropType).filter(Boolean);
            return candidates.length && !candidates.some(function (candidate) {
                return accepted.indexOf(candidate) !== -1;
            });
        });
        if (invalid) {
            throw new Error('\u5f53\u524dTable\u4e0d\u652f\u6301\u62d6\u5165\u8be5\u5bf9\u8c61\u7c7b\u578b: ' + invalid.objectType);
        }
    }

    function mergeDroppedRows(scoped, binding, rows) {
        var table = scoped && scoped.getComponentById(binding.componentId);
        var data;
        var currentItems;
        var objectIdField = binding.objectIdField || 'id';
        var droppedItems = [];
        var droppedIds = {};
        if (!table || typeof table.getData !== 'function' || typeof table.setData !== 'function') {
            throw new Error('\u672a\u627e\u5230\u53ef\u66f4\u65b0\u7684Table: ' + binding.componentId);
        }
        data = table.getData() || {};
        currentItems = Array.isArray(data.items) ? data.items.slice() : [];
        rows.forEach(function (row) {
            var objectId = String(row && row[objectIdField] || '');
            if (!objectId || droppedIds[objectId]) {
                return;
            }
            droppedIds[objectId] = true;
            droppedItems.push(row);
        });
        currentItems = droppedItems.concat(currentItems.filter(function (item) {
            return !droppedIds[String(item && item[objectIdField] || '')];
        }));
        return Promise.resolve(table.setData({items: currentItems, total: currentItems.length}, false));
    }

    function waitForDropTarget(componentId) {
        return new Promise(function (resolve, reject) {
            var attempts = 0;
            function bindWhenReady() {
                var target = global.document.querySelector('.' + dropTargetClass(componentId));
                attempts += 1;
                if (target) {
                    resolve(target);
                } else if (attempts >= 100) {
                    reject(new Error('\u672a\u627e\u5230\u53ef\u62d6\u5165Table: ' + componentId));
                } else {
                    global.setTimeout(bindWhenReady, 50);
                }
            }
            bindWhenReady();
        });
    }

    //20260917 update by caipan V2拖拽事件统一通过事件动作和Effect执行，不再依赖Table专用动作分支
    function bindEventDrops(pagePackage, runtime) {
        var adapter = runtime.adapter || {};
        var bindings = (pagePackage.plmConfig && pagePackage.plmConfig.eventBindings) || [];
        var grouped = {};
        if (!adapter.bindTableDrop) return Promise.resolve();
        bindings.forEach(function (binding) {
            var source = binding.source || {};
            if (source.event !== 'drop' || !source.componentId) return;
            grouped[source.componentId] = grouped[source.componentId] || [];
            grouped[source.componentId].push(binding);
        });
        return Promise.all(Object.keys(grouped).map(function (componentId) {
            return waitForDropTarget(componentId).then(function (target) {
                return adapter.bindTableDrop(target, function (rawData) {
                    var items;
                    var task = Promise.resolve();
                    try {
                        items = parseDroppedItems(rawData);
                        validateDroppedItems(items, []);
                    } catch (error) {
                        if (adapter.notifyError) adapter.notifyError(error);
                        return;
                    }
                    grouped[componentId].forEach(function (binding) {
                        try {
                            validateDroppedItems(items, (binding.source || {}).acceptedTypes || []);
                        } catch (error) {
                            if (adapter.notifyError) adapter.notifyError(error);
                            return;
                        }
                        items.forEach(function (item) {
                            task = task.then(function () {
                                return executeEventBinding(binding, {
                                    objectId: item.objectId,
                                    objectType: item.objectType,
                                    objectTaxonomies: item.objectTaxonomies,
                                    item: item,
                                    droppedItems: items,
                                    objectIds: items.map(function (entry) { return entry.objectId; })
                                }, runtime);
                            });
                        });
                    });
                    task.catch(function (error) {
                        if (adapter.notifyError) adapter.notifyError(error);
                    });
                });
            }).catch(function (error) {
                if (adapter.notifyError) adapter.notifyError(error);
            });
        }));
    }

    function bindTableDrops(pagePackage, scoped, adapter) {
        var bindings = (pagePackage.plmConfig && pagePackage.plmConfig.tableBindings) || [];
        var eventBindings = (pagePackage.plmConfig && pagePackage.plmConfig.eventBindings) || [];
        var v2DropComponents = {};
        eventBindings.forEach(function (binding) {
            var source = binding.source || {};
            if (source.event === 'drop' && source.componentId) v2DropComponents[source.componentId] = true;
        });
        if (!adapter.bindTableDrop) {
            return Promise.resolve();
        }
        return Promise.all(bindings.map(function (binding) {
            var drop = binding.drop;
            if (!drop || !drop.actionCode || v2DropComponents[binding.componentId]) {
                return Promise.resolve();
            }
            return waitForDropTarget(binding.componentId).then(function (target) {
                return adapter.bindTableDrop(target, function (rawData) {
                    var items;
                    try {
                        items = parseDroppedItems(rawData);
                        validateDroppedItems(items, drop.acceptedTypes);
                    } catch (error) {
                        if (adapter.notifyError) adapter.notifyError(error);
                        return;
                    }
                    Promise.all(items.map(function (item) {
                        return Promise.resolve(adapter.executeAction(
                            drop.actionCode, {objectId: item.objectId}, adapter.context || {}
                        )).then(function (response) {
                            if (!response || Number(response.status) !== 0) {
                                throw new Error(response && response.msg ? response.msg : '\u62d6\u62fd\u5bf9\u8c61\u52a0\u8f7d\u5931\u8d25');
                            }
                            return response.data || {};
                        });
                    })).then(function (rows) {
                        return mergeDroppedRows(scoped, binding, rows);
                    }).catch(function (error) {
                        if (adapter.notifyError) adapter.notifyError(error);
                    });
                });
            }).catch(function (error) {
                if (adapter.notifyError) adapter.notifyError(error);
            });
        }));
    }

    function actionUrl(actionCode, componentId, bindingId) {
        var url = 'plm://' + encodeURIComponent(actionCode) + '?componentId=' + encodeURIComponent(componentId);
        if (bindingId) {
            url += '&bindingId=' + encodeURIComponent(bindingId);
        }
        return url;
    }

    //20260917 update by caipan 将低代码选择变化事件映射为AMIS原生事件名
    function runtimeEventName(eventName) {
        return eventName === 'selectionChange' ? 'selectedChange' : eventName;
    }

    function findFieldDefinition(pagePackage, fieldCode) {
        var fields = (pagePackage.resources && pagePackage.resources.fields) || [];
        var i;
        for (i = 0; i < fields.length; i += 1) {
            if (fields[i].fieldCode === fieldCode) {
                return fields[i];
            }
        }
        return null;
    }

    function fieldMetadata(metadata, fieldCode) {
        return metadata && metadata.fields && metadata.fields[fieldCode]
            ? metadata.fields[fieldCode] : null;
    }

    function optionsMap(options) {
        var result = {};
        (options || []).forEach(function (option) {
            result[String(option.value)] = option.label;
        });
        return result;
    }

    function compilePackage(pagePackage, metadata) {
        var schema;
        var config;
        var dataBindings;
        var fieldBindings;
        var actionBindings;
        var eventBindings;
        var tableBindings;
        var searchBindings;

        if (!pagePackage || typeof pagePackage !== 'object' || !pagePackage.schema) {
            throw new Error('\u9875\u9762\u914d\u7f6e\u5305\u7f3a\u5c11schema');
        }
        if (pagePackage.formatVersion && pagePackage.formatVersion !== SUPPORTED_FORMAT_VERSION) {
            throw new Error('\u4e0d\u652f\u6301\u7684\u9875\u9762\u5305\u7248\u672c: ' + pagePackage.formatVersion);
        }

        schema = clone(pagePackage.schema);
        //20260921 update by caipan 运行端统一启用AMIS弹窗标题栏拖拽，页面JSON无需逐个配置
        enableDraggableDialogs(schema);
        config = pagePackage.plmConfig || {};
        dataBindings = config.dataBindings || [];
        fieldBindings = config.fieldBindings || [];
        actionBindings = config.actionBindings || [];
        eventBindings = config.eventBindings || [];
        tableBindings = config.tableBindings || [];
        searchBindings = config.searchBindings || [];

        dataBindings.forEach(function (binding) {
            var component = findById(schema, binding.componentId);
            var apiData;
            var apiTrackExpression;
            if (component && component.type === 'service' && binding.trigger === 'INIT') {
                apiData = component.api && typeof component.api === 'object' ? component.api.data : null;
                apiTrackExpression = component.api && typeof component.api === 'object'
                    ? component.api.trackExpression : null;
                component.api = {
                    method: 'post',
                    url: actionUrl(binding.actionCode, binding.componentId)
                };
                //20260910 update by caipan 保留页面配置的报表编码等静态参数，供通用查询动作分派
                if (apiData) {
                    component.api.data = apiData;
                }
                //20260912 update by caipan 保留动态请求跟踪条件，数据域变化时重新加载Service
                if (apiTrackExpression) {
                    component.api.trackExpression = apiTrackExpression;
                }
            }
        });

        fieldBindings.forEach(function (binding) {
            var component = findById(schema, binding.componentId);
            var field = findFieldDefinition(pagePackage, binding.fieldCode);
            var translated = fieldMetadata(metadata, binding.fieldCode);
            var rangeConfig;
            if (component && binding.valueKey) {
                component.name = binding.valueKey;
            }
            if (component && translated && translated.label) {
                component.label = translated.label;
            }
            if (component && field && (field.rangeSource === 'PLM_RANGE' || field.rangeSource === 'PLM_STATE')) {
                rangeConfig = field.rangeConfig || {};
                if (field.rangeSource === 'PLM_RANGE' && !rangeConfig.attributeName) {
                    throw new Error('\u5b57\u6bb5' + binding.fieldCode + '\u7f3a\u5c11PLM Range\u5c5e\u6027\u540d\u914d\u7f6e');
                }
                if (translated && translated.options) {
                    //20260911 update by caipan 统一通过AMIS动态数据域加载PLM选项，避免表单初始化覆盖options
                    delete component.options;
                    component.source = '${__plmFieldOptions.' + binding.fieldCode + '}';
                } else if (field.rangeSource === 'PLM_RANGE') {
                    delete component.options;
                    component.source = {
                        method: 'post',
                        url: actionUrl('QUERY_ATTRIBUTE_RANGE', binding.componentId),
                        data: {
                            fieldCode: binding.fieldCode,
                            attributeName: rangeConfig.attributeName
                        }
                    };
                } else {
                    throw new Error('\u5b57\u6bb5' + binding.fieldCode + '\u7684PLM\u72b6\u6001\u5143\u6570\u636e\u672a\u52a0\u8f7d');
                }
            }
        });

        actionBindings.forEach(function (binding) {
            var component = findById(schema, binding.componentId);
            var api;
            if (!component) {
                return;
            }
            api = {
                method: 'post',
                url: actionUrl(binding.actionCode, binding.componentId)
            };
            if (binding.event === 'submit' && component.type === 'form') {
                component.api = api;
            } else if (binding.event === 'click') {
                component.actionType = 'ajax';
                component.api = api;
            } else if (binding.event === 'change') {
                component.onEvent = component.onEvent || {};
                component.onEvent.change = component.onEvent.change || { actions: [] };
                component.onEvent.change.actions.push({ actionType: 'ajax', args: { api: api } });
            }
        });

        eventBindings.forEach(function (binding) {
            var source = binding.source || {};
            var component = findById(schema, source.componentId);
            var api;
            if (!component || !binding.action || !binding.action.actionCode) {
                return;
            }
            api = {
                method: 'post',
                url: actionUrl(binding.action.actionCode, source.componentId, binding.id)
            };
            if (source.event === 'init' && component.type === 'service') {
                component.api = api;
            } else if (source.event === 'submit' && component.type === 'form') {
                component.api = api;
            } else if (source.event === 'click') {
                component.actionType = 'ajax';
                component.api = api;
            } else if (source.event === 'drop') {
                component.className = ((component.className || '') + ' '
                    + dropTargetClass(source.componentId)).trim();
            } else {
                var runtimeEvent = runtimeEventName(source.event);
                component.onEvent = component.onEvent || {};
                component.onEvent[runtimeEvent] = component.onEvent[runtimeEvent] || {actions: []};
                component.onEvent[runtimeEvent].actions.push({actionType: 'ajax', args: {api: api}});
            }
        });

        tableBindings.forEach(function (binding) {
            var component = findById(schema, binding.componentId);
            var apiData;
            var columns;
            if (component) {
                if (binding.drop && binding.drop.actionCode) {
                    component.className = ((component.className || '') + ' '
                        + dropTargetClass(binding.componentId)).trim();
                }
                apiData = component.api && typeof component.api === 'object' ? component.api.data : null;
                component.api = {
                    method: 'post',
                    url: actionUrl(binding.queryActionCode, binding.componentId)
                };
                if (component.loadDataOnce) {
                    component.api.data = Object.assign({}, apiData || {}, {clientSide: true});
                    if (typeof component.alwaysShowPagination === 'undefined') {
                        component.alwaysShowPagination = true;
                    }
                } else if (apiData) {
                    component.api.data = apiData;
                }
                columns = component.columns || [];
                (binding.columnBindings || []).forEach(function (columnBinding) {
                    var column = columns.find(function (item) { return item.name === columnBinding.columnName; });
                    var translated = fieldMetadata(metadata, columnBinding.fieldCode);
                    if (!column || !translated) return;
                    if (translated.label) column.label = translated.label;
                    if (translated.options && translated.options.length) {
                        column.type = 'mapping';
                        column.map = optionsMap(translated.options);
                    }
                });
            }
        });

        searchBindings.forEach(function (binding) {
            var component = findById(schema, binding.componentId);
            if (component && component.type === 'button') {
                component.actionType = 'url';
                component.url = 'plm://search?componentId=' + encodeURIComponent(binding.componentId)
                    + '&searchType=' + encodeURIComponent(binding.searchType || '')
                    + '&formId=' + encodeURIComponent(binding.formId || '')
                    + '&valueField=' + encodeURIComponent(binding.valueField || '')
                    + '&labelField=' + encodeURIComponent(binding.labelField || '')
                    + '&searchParams=' + encodeURIComponent(binding.searchParams || '');
            }
        });

        return schema;
    }

    function parsePlmUrl(url) {
        var raw = String(url || '').substring(6);
        var parts = raw.split('?');
        var params = new URLSearchParams(parts[1] || '');
        return {
            actionCode: decodeURIComponent(parts[0] || ''),
            componentId: params.get('componentId') || '',
            bindingId: params.get('bindingId') || ''
        };
    }

    function findEventBinding(pagePackage, bindingId) {
        var bindings = (pagePackage.plmConfig && pagePackage.plmConfig.eventBindings) || [];
        var i;
        for (i = 0; i < bindings.length; i += 1) {
            if (bindings[i].id === bindingId) {
                return bindings[i];
            }
        }
        return null;
    }

    function parseSearchTarget(url) {
        var raw = String(url || '');
        var queryIndex = raw.indexOf('?');
        var params = new URLSearchParams(queryIndex >= 0 ? raw.substring(queryIndex + 1) : '');
        return {
            searchType: params.get('searchType') || '',
            componentId: params.get('componentId') || '',
            formId: params.get('formId') || '',
            valueField: params.get('valueField') || '',
            labelField: params.get('labelField') || '',
            searchParams: params.get('searchParams') || ''
        };
    }

    function findSearchBinding(pagePackage, componentId) {
        var bindings = (pagePackage.plmConfig && pagePackage.plmConfig.searchBindings) || [];
        var i;
        for (i = 0; i < bindings.length; i += 1) {
            if (bindings[i].componentId === componentId) {
                return bindings[i];
            }
        }
        return null;
    }

    function applySearchResult(scoped, target, result) {
        var form;
        var values = {};
        if (!scoped || !target.formId || !target.valueField || !target.labelField) {
            throw new Error('\u641c\u7d22\u56de\u586b\u914d\u7f6e\u4e0d\u5b8c\u6574');
        }
        form = scoped.getComponentById(target.formId);
        if (!form || typeof form.setValues !== 'function') {
            throw new Error('\u672a\u627e\u5230\u641c\u7d22\u56de\u586b\u8868\u5355: ' + target.formId);
        }
        values[target.valueField] = result.objectId || '';
        values[target.labelField] = result.displayName || result.name || '';
        form.setValues(values);
    }

    function findActionBinding(pagePackage, componentId, actionCode) {
        var bindings = (pagePackage.plmConfig && pagePackage.plmConfig.actionBindings) || [];
        var i;
        for (i = 0; i < bindings.length; i += 1) {
            if (bindings[i].componentId === componentId && bindings[i].actionCode === actionCode) {
                return bindings[i];
            }
        }
        return null;
    }

    function runSuccessAction(adapter, binding, responseData) {
        if (!binding || !binding.successAction || binding.successAction === 'NONE') {
            return;
        }
        if (binding.successAction === 'REFRESH' && adapter.refresh) {
            adapter.refresh();
        } else if (binding.successAction === 'CLOSE' && adapter.close) {
            adapter.close();
        } else if (binding.successAction === 'OPEN_DETAIL' && adapter.openDetail) {
            adapter.openDetail(responseData && responseData.data && responseData.data.objectId);
        }
    }

    function normalizeFetcherResult(result) {
        if (result && Number(result.status) >= 100 && result.headers) {
            return result;
        }
        return { data: result, status: 200, headers: {} };
    }

    function componentById(runtime, componentId) {
        if (!runtime || !runtime.scoped || !componentId
                || typeof runtime.scoped.getComponentById !== 'function') {
            return null;
        }
        return runtime.scoped.getComponentById(componentId);
    }

    function replaceComponentData(component, data) {
        if (!component) return Promise.resolve();
        if (typeof component.setData === 'function') {
            return Promise.resolve(component.setData(data, false));
        }
        if (typeof component.setValues === 'function') {
            return Promise.resolve(component.setValues(data));
        }
        throw new Error('\u76ee\u6807\u7ec4\u4ef6\u4e0d\u652f\u6301\u5199\u5165\u6570\u636e');
    }

    function rowsFromMappedValue(value) {
        if (Array.isArray(value)) return value;
        return value && Array.isArray(value.items) ? value.items : [];
    }

    function executeEffect(effect, source, runtime) {
        var adapter = runtime.adapter || {};
        var target = resolveMapping(effect.target || '', source);
        var component = componentById(runtime, target);
        var mapped = resolveMapping(typeof effect.mapping === 'undefined' ? {} : effect.mapping, source);
        var current;
        var rows;
        var ids;
        var key;
        if (effect.type === 'SET_DATA') {
            return replaceComponentData(component, mapped);
        }
        if (effect.type === 'RELOAD' || effect.type === 'REFRESH_ROW') {
            if (component && typeof component.reload === 'function') {
                return Promise.resolve(component.reload());
            }
            if (!target && adapter.refresh) return Promise.resolve(adapter.refresh());
            throw new Error('\u672a\u627e\u5230\u53ef\u5237\u65b0\u7684\u76ee\u6807\u7ec4\u4ef6: ' + target);
        }
        if (effect.type === 'APPEND_ROWS') {
            current = component && typeof component.getData === 'function' ? component.getData() || {} : {};
            rows = rowsFromMappedValue(mapped);
            if (effect.position === 'first') rows = rows.concat(current.items || []);
            else rows = (current.items || []).concat(rows);
            key = effect.deduplicateBy;
            if (key) {
                ids = {};
                rows = rows.filter(function (row) {
                    var id = String(row && row[key] || '');
                    if (!id || ids[id]) return false;
                    ids[id] = true;
                    return true;
                });
            }
            return replaceComponentData(component, {items: rows, total: rows.length});
        }
        if (effect.type === 'REMOVE_ROWS') {
            current = component && typeof component.getData === 'function' ? component.getData() || {} : {};
            key = effect.deduplicateBy || 'id';
            ids = {};
            rowsFromMappedValue(mapped).forEach(function (row) {
                ids[String(row && typeof row === 'object' ? row[key] : row)] = true;
            });
            rows = (current.items || []).filter(function (row) { return !ids[String(row && row[key])]; });
            return replaceComponentData(component, {items: rows, total: rows.length});
        }
        if (effect.type === 'RESET') {
            if (component && typeof component.reset === 'function') return Promise.resolve(component.reset());
            return replaceComponentData(component, {});
        }
        if (effect.type === 'OPEN_DIALOG' || effect.type === 'OPEN_DRAWER') {
            if (runtime.scoped && typeof runtime.scoped.doAction === 'function') {
                var overlayAction = {actionType: effect.type === 'OPEN_DIALOG' ? 'dialog' : 'drawer'};
                //20260921 update by caipan 动态打开的弹窗同样启用拖拽，保持与Schema静态弹窗一致
                if (effect.type === 'OPEN_DIALOG' && mapped && typeof mapped === 'object') {
                    mapped.draggable = true;
                }
                overlayAction[effect.type === 'OPEN_DIALOG' ? 'dialog' : 'drawer'] = mapped;
                return Promise.resolve(runtime.scoped.doAction(overlayAction, source));
            }
            if (adapter.openOverlay) return Promise.resolve(adapter.openOverlay(effect.type, target, mapped));
            throw new Error('\u5f53\u524d\u8fd0\u884c\u7aef\u4e0d\u652f\u6301\u6253\u5f00\u5f39\u5c42');
        }
        if (effect.type === 'CLOSE') {
            if (target && runtime.scoped && typeof runtime.scoped.closeById === 'function') {
                return Promise.resolve(runtime.scoped.closeById(target));
            }
            if (component && typeof component.doAction === 'function') {
                return Promise.resolve(component.doAction({actionType: 'close'}, source, true));
            }
            if (!target && adapter.close) return Promise.resolve(adapter.close());
            throw new Error('\u672a\u627e\u5230\u53ef\u5173\u95ed\u7684\u76ee\u6807: ' + target);
        }
        if (effect.type === 'OPEN_DETAIL') {
            if (adapter.openDetail) {
                return Promise.resolve(adapter.openDetail(
                    mapped && typeof mapped === 'object' ? mapped.objectId || mapped.id || target : mapped || target
                ));
            }
        }
        if (effect.type === 'NAVIGATE') {
            if (adapter.navigate) {
                return Promise.resolve(adapter.navigate(
                    mapped && typeof mapped === 'object' ? mapped.url || target : mapped || target
                ));
            }
        }
        if (effect.type === 'NOTIFY') {
            var message = resolveMapping(effect.message || '${response.msg}', source);
            if (adapter.notify) return Promise.resolve(adapter.notify(effect.level || 'info', message));
            if (effect.level === 'error' && adapter.notifyError) {
                return Promise.resolve(adapter.notifyError(new Error(message)));
            }
            notify(effect.level || 'info', message);
            return Promise.resolve();
        }
        if (effect.type === 'CHAIN_ACTION' && effect.action && effect.action.actionCode) {
            return Promise.resolve(adapter.executeAction(
                effect.action.actionCode,
                resolveMapping(effect.action.inputMapping || {}, source),
                adapter.context || {}
            )).then(function (response) {
                if (!response || Number(response.status) !== 0) {
                    throw new Error(response && response.msg ? response.msg : '\u94fe\u5f0f\u52a8\u4f5c\u6267\u884c\u5931\u8d25');
                }
            });
        }
        return Promise.resolve();
    }

    function executeEffects(effects, source, runtime) {
        return (effects || []).reduce(function (task, effect) {
            return task.then(function () { return executeEffect(effect, source, runtime); });
        }, Promise.resolve());
    }

    function executeEventBinding(binding, eventData, runtime) {
        var source = Object.assign({}, eventData || {}, {
            event: eventData || {},
            plmContext: runtime.adapter.context || {}
        });
        var condition = typeof binding.when === 'undefined' || resolveMapping(binding.when, source);
        if (!condition) return Promise.resolve({status: 0, msg: '', data: {skipped: true}});
        var inputMapping = binding.action.inputMapping || {};
        var actionInput = inputMapping && typeof inputMapping === 'object'
                && !Array.isArray(inputMapping) && !Object.keys(inputMapping).length
            ? eventData || {} : resolveMapping(inputMapping, source);
        return Promise.resolve(runtime.adapter.executeAction(
            binding.action.actionCode,
            actionInput,
            runtime.adapter.context || {}
        )).then(function (response) {
            var effectSource = Object.assign({}, source, {response: response || {}, data: response && response.data});
            var effects = response && Number(response.status) === 0 ? binding.success : binding.failure;
            return executeEffects(effects, effectSource, runtime).then(function () { return response; });
        }, function (error) {
            var response = {status: 1, msg: error && error.message ? error.message : String(error), data: {}};
            var effectSource = Object.assign({}, source, {response: response, data: response.data});
            return executeEffects(binding.failure, effectSource, runtime).then(function () { throw error; });
        });
    }

    function createFetcher(pagePackage, adapter, runtime) {
        runtime = runtime || {adapter: adapter, scoped: null};
        return function (api) {
            var url = typeof api === 'string' ? api : api.url;
            var requestData = typeof api === 'string' ? {} : (api.data || {});
            var sourceComponent;
            var configuredData;
            var target;
            if (String(url).indexOf('plm://') !== 0) {
                if (!adapter.fetch) {
                    return Promise.reject(new Error('\u4e0d\u5141\u8bb8\u8bbf\u95ee\u975ePLM\u63a5\u53e3'));
                }
                return adapter.fetch(api);
            }
            target = parsePlmUrl(url);
            var eventBinding = target.bindingId ? findEventBinding(pagePackage, target.bindingId) : null;
            if (eventBinding) {
                return executeEventBinding(eventBinding, requestData, runtime)
                    .then(normalizeFetcherResult);
            }
            sourceComponent = findById(pagePackage.schema, target.componentId);
            configuredData = sourceComponent && sourceComponent.api
                && typeof sourceComponent.api === 'object' ? sourceComponent.api.data : null;
            //20260910 update by caipan 只恢复原始JSON中的静态参数，动态模板必须使用AMIS计算后的值
            requestData = Object.assign({}, requestData || {});
            Object.keys(configuredData || {}).forEach(function (key) {
                var value = configuredData[key];
                if (requestData[key] === undefined
                    && !(typeof value === 'string' && value.indexOf('${') !== -1)) {
                    requestData[key] = value;
                }
            });
            if (configuredData && configuredData.reportCode) {
                requestData.reportCode = configuredData.reportCode;
            }
            return Promise.resolve(adapter.executeAction(target.actionCode, requestData, adapter.context || {}))
                .then(normalizeFetcherResult)
                .then(function (result) {
                    var data = result.data || {};
                    if (Number(data.status) === 0) {
                        runSuccessAction(adapter, findActionBinding(pagePackage, target.componentId, target.actionCode), data);
                    }
                    return result;
                });
        };
    }

    function parseJson(text) {
        var source = String(text || '').trim();
        var start;
        var depth = 0;
        var inString = false;
        var escaped = false;
        var i;
        var ch;
        try {
            return JSON.parse(source);
        } catch (ignore) {
            start = source.indexOf('{');
            for (i = start; i >= 0 && i < source.length; i += 1) {
                ch = source.charAt(i);
                if (inString) {
                    if (escaped) {
                        escaped = false;
                    } else if (ch === '\\') {
                        escaped = true;
                    } else if (ch === '"') {
                        inString = false;
                    }
                } else if (ch === '"') {
                    inString = true;
                } else if (ch === '{') {
                    depth += 1;
                } else if (ch === '}') {
                    depth -= 1;
                    if (depth === 0) {
                        return JSON.parse(source.substring(start, i + 1));
                    }
                }
            }
            throw new Error('\u670d\u52a1\u7aef\u672a\u8fd4\u56de\u6709\u6548JSON');
        }
    }

    function embed(options) {
        if (!options.adapter || options.adapter.protocolVersion !== 1) {
            throw new Error('Adapter协议不兼容：需要版本1，请配套更新Widget或Space入口');
        }
        var pagePackage = options.pagePackage;
        var adapter = options.adapter;
        var amis = global.amisRequire('amis/embed');
        var fields = (pagePackage.resources && pagePackage.resources.fields) || [];
        var scoped;
        var runtime = {adapter: adapter, scoped: null};
        //20260912 update by caipan 浮层挂到不滚动的body，并补齐AMIS作用域，兼顾定位和样式
        global.document.body.classList.add('amis-scope');
        var env = {
            fetcher: createFetcher(pagePackage, adapter, runtime),
            notify: function (level, message) {
                if (adapter.notify) {
                    adapter.notify(level, message);
                } else {
                    notify(level, message);
                }
            },
            getModalContainer: function () {
                return global.document.body;
            },
            jumpTo: function (target) {
                if (String(target).indexOf('plm://search?') === 0) {
                    if (!adapter.openSearch) {
                        throw new Error('\u5f53\u524d\u8fd0\u884c\u7aef\u4e0d\u652f\u6301PLM\u539f\u751f\u641c\u7d22');
                    }
                    var searchTarget = parseSearchTarget(target);
                    var searchBinding = findSearchBinding(pagePackage, searchTarget.componentId);
                    if (searchBinding) {
                        searchTarget = {
                            searchType: searchBinding.searchType || '',
                            componentId: searchBinding.componentId,
                            formId: searchBinding.formId,
                            valueField: searchBinding.valueField,
                            labelField: searchBinding.labelField,
                            searchParams: searchBinding.searchParams || ''
                        };
                    }
                    Promise.resolve(adapter.openSearch(searchTarget)).then(function (result) {
                        //20260909 update by caipan Widget手动关闭搜索时不清空表单，也不显示错误提示
                        if (result && result.cancelled) {
                            return;
                        }
                        applySearchResult(scoped, searchTarget, result || {});
                    }).catch(function (error) {
                        if (adapter.notifyError) {
                            adapter.notifyError(error);
                        }
                    });
                    return;
                }
                if (target === 'plm://cancel' && adapter.close) {
                    adapter.close();
                    return;
                }
                if (adapter.navigate) {
                    adapter.navigate(target);
                }
            },
            updateLocation: function () {},
            isCancel: function () { return false; }
        };
        return Promise.resolve(fields.length
            ? adapter.executeAction('QUERY_PAGE_FIELD_METADATA', {fields: fields}, adapter.context || {})
            : {status: 0, data: {fields: {}}})
            .then(function (response) {
                var initialData = { plmContext: adapter.context || {}, __plmFieldOptions: {} };
                var metadataFields;
                if (!response || Number(response.status) !== 0) {
                    throw new Error(response && response.msg ? response.msg : '\u9875\u9762PLM\u56fd\u9645\u5316\u5143\u6570\u636e\u52a0\u8f7d\u5931\u8d25');
                }
                metadataFields = response.data && response.data.fields ? response.data.fields : {};
                Object.keys(metadataFields).forEach(function (fieldCode) {
                    initialData.__plmFieldOptions[fieldCode] = metadataFields[fieldCode].options || [];
                });
                var schema = compilePackage(pagePackage, response.data || {});
                scoped = amis.embed(options.container, schema, { data: initialData }, env);
                runtime.scoped = scoped;
                return bindEventDrops(pagePackage, runtime).then(function () {
                    return bindTableDrops(pagePackage, scoped, adapter);
                }).then(function () {
                    return scoped;
                });
            });
    }

    global.JFLowCodeRuntime = {
        protocolVersion: 1,
        formatVersion: SUPPORTED_FORMAT_VERSION,
        compilePackage: compilePackage,
        createFetcher: createFetcher,
        parseSearchTarget: parseSearchTarget,
        findSearchBinding: findSearchBinding,
        applySearchResult: applySearchResult,
        parseDroppedItems: parseDroppedItems,
        validateDroppedItems: validateDroppedItems,
        mergeDroppedRows: mergeDroppedRows,
        resolveMapping: resolveMapping,
        notify: notify,
        executeEffects: executeEffects,
        executeEventBinding: executeEventBinding,
        embed: embed,
        parseJson: parseJson
    };
}(window));
