define("DS/SearchLocalActionsForTWXOpen/SearchLocalActionsForTWXOpen", [
    "UWA/Class",
    "UWA/Class/Debug",
    "UWA/Class/Events",
], function (e, n, t,s) {
    //     "DS/i3DXCompass/UsageTracker" 核心方法 clickApp 
    return e.singleton(t, n, {
        executeAction: function (e) {
            console.log(e);
            e.object_id &&
            e.actionsHelper.getServiceURL({
                onComplete: function (n) {
                    var paramap = {
                        objectId: e.object_id,
                        relationId: "",
                    }
                    console.log(s);
                    let str3DspaceUrl = n;
                    // str3DspaceUrl = str3DspaceUrl + "/emxLogin.jsp?appName=ENOBUPS_AP&SecurityContext=&objectId=" + e.object_id;
                    str3DspaceUrl = str3DspaceUrl + "/emxLogin.jsp?&SecurityContext=&objectId=" + e.object_id;
                    window.open(str3DspaceUrl,'about:blank');
                },
                id: e.object_id,
            });
        },
    });
});

function openPostWindow(url, map, name) {
    var tempForm = document.createElement("form");
    tempForm.id = "tempForm1";
    tempForm.method = "post";
    tempForm.action = url;
    tempForm.target = name;

    var hiddenInput = "";
    for (var prop in map) {
        if (map.hasOwnProperty(prop)) {
            hiddenInput = document.createElement("input");
            hiddenInput.type = "hidden";
            hiddenInput.name = prop;
            hiddenInput.value = map[prop];
            //console.log('key is ' + prop +' and value is ' + map[prop]);
            tempForm.appendChild(hiddenInput);
        }
    }
    tempForm.addEventListener("onsubmit", function () {
        window.open(url,'about:blank');
        // window.open(url,'about:blank', name, "height=700, width=1000");
    }, true);
    document.body.appendChild(tempForm);
    var evt = document.createEvent("HTMLEvents");
    evt.initEvent("onsubmit", true, true);
    tempForm.dispatchEvent(evt);
    tempForm.submit();
    document.body.removeChild(tempForm);
}
