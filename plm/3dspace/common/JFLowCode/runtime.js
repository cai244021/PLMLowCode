(function (global) {
    'use strict';

    var SUPPORTED_FORMAT_VERSION = 1;

    function clone(value) {
        return JSON.parse(JSON.stringify(value));
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

    function actionUrl(actionCode, componentId) {
        return 'plm://' + encodeURIComponent(actionCode) + '?componentId=' + encodeURIComponent(componentId);
    }

    function compilePackage(pagePackage) {
        var schema;
        var config;
        var fieldBindings;
        var actionBindings;
        var tableBindings;

        if (!pagePackage || typeof pagePackage !== 'object' || !pagePackage.schema) {
            throw new Error('\u9875\u9762\u914d\u7f6e\u5305\u7f3a\u5c11schema');
        }
        if (pagePackage.formatVersion && pagePackage.formatVersion !== SUPPORTED_FORMAT_VERSION) {
            throw new Error('\u4e0d\u652f\u6301\u7684\u9875\u9762\u5305\u7248\u672c: ' + pagePackage.formatVersion);
        }

        schema = clone(pagePackage.schema);
        config = pagePackage.plmConfig || {};
        fieldBindings = config.fieldBindings || [];
        actionBindings = config.actionBindings || [];
        tableBindings = config.tableBindings || [];

        fieldBindings.forEach(function (binding) {
            var component = findById(schema, binding.componentId);
            if (component && binding.valueKey) {
                component.name = binding.valueKey;
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

        tableBindings.forEach(function (binding) {
            var component = findById(schema, binding.componentId);
            if (component) {
                component.api = {
                    method: 'post',
                    url: actionUrl(binding.queryActionCode, binding.componentId)
                };
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
            componentId: params.get('componentId') || ''
        };
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

    function createFetcher(pagePackage, adapter) {
        return function (api) {
            var url = typeof api === 'string' ? api : api.url;
            var requestData = typeof api === 'string' ? {} : (api.data || {});
            var target;
            if (String(url).indexOf('plm://') !== 0) {
                if (!adapter.fetch) {
                    return Promise.reject(new Error('\u4e0d\u5141\u8bb8\u8bbf\u95ee\u975ePLM\u63a5\u53e3'));
                }
                return adapter.fetch(api);
            }
            target = parsePlmUrl(url);
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
        var pagePackage = options.pagePackage;
        var adapter = options.adapter;
        var schema = compilePackage(pagePackage);
        var amis = global.amisRequire('amis/embed');
        var env = {
            fetcher: createFetcher(pagePackage, adapter),
            jumpTo: function (target) {
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
        return amis.embed(options.container, schema, { data: { plmContext: adapter.context || {} } }, env);
    }

    global.JFLowCodeRuntime = {
        formatVersion: SUPPORTED_FORMAT_VERSION,
        compilePackage: compilePackage,
        createFetcher: createFetcher,
        embed: embed,
        parseJson: parseJson
    };
}(window));
