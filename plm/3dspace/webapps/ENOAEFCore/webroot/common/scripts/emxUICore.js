var Browser = (function () {
	/*
	 * User Agent String for all the supported browser
	 *
	 * Just for reference about 2015x User agent Strings of all the supported browsers
		String ieUserAgentForIE11 = "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; .NET CLR 1.1.4322; rv:11.0) like Gecko";
		String ffUserAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:24.0) Gecko/20100101 Firefox/24.0";
		String chromeUserAgent  = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.107 Safari/537.36";
		String safariUserAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50";
	*/
	var browsers = {
				"IE" : false,
				"FIREFOX" : false,
				"CHROME" : false,
				"MOZILLA_FAMILY": false,
				"SAFARI" : false,
				"OPERA" : false,
				"MOBILE" : false,
				"EDGE" : false
 			};


	detectBrowser = function(ua){
		var ie = /(msie|trident)/i.test(ua)
	  , edge = /Edge/i.test(ua) 
      , chrome = /chrome|crios/i.test(ua) && !edge
      , safari = /safari/i.test(ua) && !chrome && !edge
      , firefox = /firefox/i.test(ua)
      , mobile = /(Mobile)/i.test(ua) || /(Touch|Tablet PC*)/.test(ua);
		if(ie){
			browsers.IE=true;
		}else if (chrome){
			browsers.CHROME=true;
		}else if(safari){
			browsers.SAFARI=true;
		}else if(firefox){
			browsers.FIREFOX=true;
		}
		if(mobile){
			browsers.MOBILE = true;
		}

		if(chrome || safari || firefox){
			browsers.MOZILLA_FAMILY=true;
		}
		if(edge){
		  browsers.EDGE=true;
		}
	};
	detectBrowser(navigator.userAgent);
	return browsers;
}());

/* Stub out ElapsedTimer.  This allows leaving timing checks in JS code */
/* even if emxUIElapsedTimer.js is not actually loaded */
if (self.ElapsedTimer == null) {
     ElapsedTimer = {
		reset: function () {},
		setThreshold: function() {},

		log: function() {},
		info: function() {},
		warn: function() {},

		timeCheck: function() {},
		enter: function() {},
		exit: function() {},

		fn: function() {},
		stack: function() {}
	}
}

function cancelEvent() {
        return false;
}
var strUserAgent = navigator.userAgent.toLowerCase();
var isIE		= Browser.IE;
var isMoz		= Browser.MOZILLA_FAMILY;
var isChrome	= Browser.CHROME;
var isSafari	= Browser.SAFARI;
var isEdge  = Browser.EDGE;
var isMac = navigator.platform.indexOf("Mac") > -1;
var isUnix = strUserAgent.indexOf("x11") > -1;
var isHPUX = strUserAgent.indexOf("hp-ux") > -1;
var isSunOS = strUserAgent.indexOf("sunos") > -1;
var isWin = navigator.platform.indexOf("Win") > -1;
var STR_CHECKBOX_NAME = "emxTableRowId";


//To disable the default browser drop behavior
window.addEventListener("dragover",function(e){
  e = e || event;
  e.preventDefault();
},false);
window.addEventListener("drop",function(e){
  e = e || event;
  e.preventDefault();
},false);

var emxUICore = new Object;
emxUICore.objElem = null;
emxUICore.DIR_IMAGES = "../common/images/";
emxUICore.DIR_STYLES = "../common/styles/";
emxUICore.DIR_SMALL_ICONS = emxUICore.DIR_IMAGES + "icons/small/";
emxUICore.DIR_BIG_ICONS = emxUICore.DIR_IMAGES + "icons/big/";
emxUICore.DIR_UTIL = emxUICore.DIR_IMAGES + "util/";
emxUICore.DIR_BUTTONS = emxUICore.DIR_IMAGES + "buttons/";
emxUICore.DIR_TREE = emxUICore.DIR_IMAGES + "tree/";
emxUICore.DIR_NAVBAR = emxUICore.DIR_IMAGES + "navbar/";
emxUICore.DIR_SEARCHPANE = emxUICore.DIR_IMAGES + "search/";
emxUICore.DIR_DISC = emxUICore.DIR_IMAGES + "discussion/";
emxUICore.IMG_SPACER = emxUICore.DIR_IMAGES + "utilSpacer.gif";
emxUICore.IMG_LOADING = emxUICore.DIR_SMALL_ICONS + "iconStatusLoading.gif";
emxUICore.IMG_PAGE_HEAD_ARROW = emxUICore.DIR_IMAGES + "utilPageHeadArrow.gif";
emxUICore.UI_LEVEL = 5;
emxUICore.CALENDAR_START_DOW = 0;

/*
 * Below vars can be removed after the discussion with application teams
 *
 * */
var isMinIE5 = false,isMinIE55 = false,isMinIE6 = false,isMinIE9 = false,isMaxIE8 = false, isMaxIE7 = false; isIE11 = false;
if (isIE) {
        var reIE = new RegExp("msie (\\S*);");
        var regResult = reIE.exec(strUserAgent);
		var fVer;
		if(!regResult){
			reIE = new RegExp("rv:(\\d*)");	
			fVer = parseFloat(reIE.exec(strUserAgent)[1]);
		}
		else{
			fVer = parseFloat(regResult[1]);
		}
        isMinIE5 = fVer >= 5;
        isMinIE55 = fVer >= 5.5;
        isMinIE6 = fVer >= 6;
        isMinIE9 = fVer >= 9;
        isMaxIE8 = fVer <= 8;
        isMaxIE7 = fVer <= 7;
        isIE11 = fVer == 11;
}
var isGecko = strUserAgent.indexOf("gecko") > -1;
var isMinMoz092 = false,isMinMoz094 = false,isMinMoz098 = false,isMinMoz1 = false,isMinFF3 = false,isMinFF4 = false;
if (isMoz) {
        if (strUserAgent.indexOf("rv:") > -1) {
                var reMoz = new RegExp("rv:([\\d\\]*.\\d)([\\.\\d]*)");
                reMoz.test(strUserAgent);
                var fMajorVer = RegExp["$1"];
                var fMinorVer = RegExp["$2"];
                isMinMoz092 = (fMajorVer == 0.9 && fMinorVer >= 0.2) || (fMajorVer > 0.9);
                isMinMoz094 = (fMajorVer == 0.9 && fMinorVer >= 0.4) || (fMajorVer > 0.9);
                isMinMoz098 = (fMajorVer == 0.9 && fMinorVer >= 0.8) || (fMajorVer > 0.9);
                isMinMoz1 = (fMajorVer >= 1.0);
        }
        var reFF = new RegExp("firefox[\/\s]([\\d\\]*.\\d)([\\.\\d]*)");
        reFF.test(strUserAgent);
        var ffVer = parseFloat(RegExp["$1"]);
        isMinFF3 = ffVer >= 3;
        isMinFF4 = ffVer >= 4;
}

var isWinNT = false,isWin95 = false,isWin98 = false,isWin2000 = false,isWinME = false,isWinXP = false;
if (isWin) {
        if (isIE) {
                isWinNT = strUserAgent.indexOf("windows nt 4.0") > -1;
                isWin95 = strUserAgent.indexOf("windows 95") > -1;
                isWin98 = strUserAgent.indexOf("windows 98") > -1;
                isWin2000 = strUserAgent.indexOf("windows nt 5.0") > -1;
                isWinME = strUserAgent.indexOf("win 9x 4.90") > -1;
                isWinXP = strUserAgent.indexOf("windows nt 5.1") > -1;
        } else if (isMoz) {
                isWinNT = strUserAgent.indexOf("winnt4.0") > -1;
                isWin95 = strUserAgent.indexOf("win95") > -1;
                isWin98 = strUserAgent.indexOf("win98") > -1;
                isWin2000 = strUserAgent.indexOf("windows nt 5.0") > -1;
                isWinME = strUserAgent.indexOf("win 9x 4.90") > -1;
                isWinXP = strUserAgent.indexOf("windows nt 5.1") > -1;
        }
}
/*
 * should be removed till here after discussion with app teams
 * */

//! Public Method Date.addDays()
//!     This function adds a specified number of days to the current date.
Date.prototype.addDays = function _Date_addDays(intDays) {
        this.setDate(this.getDate() + intDays);
};
//! Public Method Date.addMonths()
//!     This function adds a specified number of months to the current date.
Date.prototype.addMonths = function(intMonths) {
        this.setMonth(this.getMonth() + intMonths);
};
//! Public Method Date.addYears()
//!     This function adds a specified number of years to the current date.
Date.prototype.addYears = function(intYears) {
        this.setYear(this.getFullYear() + intYears);
};
//! Public Method Date.isDateEqual()
//!     This function determines if two dates are equal.
Date.prototype.isDateEqual = function _Date_isDateEqual(objDate) {
        if (objDate == null){
                return false;
        } else {
                return (this.getMonth() == objDate.getMonth()) && (this.getDate() == objDate.getDate()) && (this.getFullYear() == objDate.getFullYear());
        }
};
//! Public Method Date.isDayBefore()
//!     This method determines whether, given two days, the first date
//!     occurs earlier in the calendar week than the second date. This is
//!     needed when the calendar week begins on any day other than Sunday.
Date.isDayBefore = function _Date_isDayBefore(intTestDay, intStartDay) {
        var intTestDayLoc, intStartDayLoc;
        for (var i=0; i < 7; i++) {
                var iTestValue = (i + emxUIConstants.CALENDAR_START_DOW) % 7;
                if (iTestValue == intTestDay) intTestDayLoc = i;
                if (iTestValue == intStartDay) intStartDayLoc = i;
        }
        return intTestDayLoc < intStartDayLoc;
}
Array.prototype.find = function _Array_find(varItem) {
	 if(typeof varItem === 'function'){
         if (this === null) {
             throw new TypeError('Array.prototype.find called on null or undefined');
         } else if (typeof varItem !== 'function') {
             throw new TypeError('callback must be a function');
         }
         var list = Object(this);
         // Makes sures is always has an positive integer as length.
         var length = list.length >>> 0;
         var thisArg = arguments[1];
         for (var i = 0; i < length; i++) {
             var element = list[i];
             if ( varItem.call(thisArg, element, i, list) ) {
                   return element;
             }
        }
	 }else{
        var blnFound = false;
        for (var i=0; i  < this.length && !blnFound; i++) {
                if (this[i] == varItem) {
                        blnFound = true;
                }
        }
        return (blnFound ? i-1 : -1);
	 }
};
Array.prototype.remove = function _Array_remove(varItem) {
        var intPos = this.find(varItem);
        if (intPos > -1) {
                for (var i= intPos + 1; i < this.length; i++) {
                        this[i-1] = this[i];
                }
                this.length--;
        }
};
if (!Array.prototype.pop) {
    Array.prototype.pop = function () {
        var objItem = this[this.length-1];
        this.length--;
        return objItem;
    };
}
if (!Array.prototype.push) {
    Array.prototype.push = function (objItem) {
        this[this.length] = objItem;
    };
}

if (!Array.prototype.filter) {
	Array.prototype.filter = function(func) {
		var res = [];
		for (var i = 0; i < this.length; i++) {
			if (func(this[i], i, this)) {
				res.push(this[i]);
			}
		}
		return res;
	}
}

Array.prototype.apply = function(func, data) {
	var res = [];
	for (var i = 0; i < this.length; i++) {
		res.push(func(this[i], i, this, data));
	}
	return res;
}

String.prototype.htmlEncode = function _String_htmlEncode() {
        var strTemp = this.toString();
        strTemp = strTemp.replace(/&/g, "&amp;");
        strTemp = strTemp.replace(/>/g, "&gt;");
        strTemp = strTemp.replace(/</g, "&lt;");
        strTemp = strTemp.replace(/\"/g, "&quot;");
        strTemp = strTemp.replace(/\'/g, "&#39;");
        return strTemp;
}

String.prototype.htmlDecode = function _String_htmlDecode() {
        var decode = this.toString();
        decode = decode.replace(/&amp;/g,"&");
        decode = decode.replace(/&lt;/g,"<");
        decode = decode.replace(/&gt;/g,">");
        decode = decode.replace(/&quot;/g,"\"");
        decode = decode.replace(/&#39;/g,"\'");
        return decode;
}

function emxUIStringBuffer() {
        this.strings = new Array;
}
emxUIStringBuffer.prototype.clear = function _emxUIStringBuffer_clear() {
        delete this.strings;
        this.strings = new Array;
};
emxUIStringBuffer.prototype.toString = function _emxUIStringBuffer_toString() {
        return this.strings.join("");
};
emxUIStringBuffer.prototype.write = function _emxUIStringBuffer_write(strText) {
        this.strings.push(strText);
};
emxUIStringBuffer.prototype.writeln = function _emxUIStringBuffer_writeln(strText) {
        this.strings.push(strText + "\n");
}

emxUICore.addClass = function _emxUICore_addClass(objElement, strClass) {
        if (!objElement || typeof objElement != "object") {
                emxUICore.throwError("Required argument objElement is null or not an object.");
        } else if (!strClass || typeof strClass != "string") {
                emxUICore.throwError("Required argument strClass is null or not a string.");
        }
        var strOriginal = objElement.className || "";
        var arrClasses = strOriginal.split(" ");
        arrClasses.push(strClass);
        objElement.className =  arrClasses.join(" ");
};
emxUICore.addToPageHistory = function _emxUICore_addToPageHistory(strSuite,strURL,strMenu,strCommand,strTarget,strCommandTitle,strLinkType,intWidth,intHeight) {
        if( getTopWindow().enablePageHistory == "true")
        {
            if(!isIE){
               strSuite=escape(strSuite);
            }
            strCommandTitle=encodeURIComponent(strCommandTitle);
	    if(strURL.indexOf('javascript:') < 0)
	    {
		var strFinalURL = "emxPageHistoryProcess.jsp?pageURL="+escape(strURL)+"&width="+intWidth+"&height="+intHeight+"&AppName="+strSuite+"&menuName="+strMenu+"&commandName="+strCommand+"&targetLocation="+strTarget+"&CommandTitle="+strCommandTitle+"&linkType="+strLinkType+"&suiteDir="+strSuite+"&myDeskSuiteDir="+strSuite;
                if(getTopWindow().hiddenFrame){
                    getTopWindow().hiddenFrame.location.href = strFinalURL;
		}
	    }
        }

};
emxUICore.addURLParam = function _emxUICore_addURLParam(strURL, strParam) {
        if (!strURL || typeof strURL != "string") {
                emxUICore.throwError("Required argument strURL is null or not a string.");
        } else if (!strParam || typeof strParam != "string") {
                emxUICore.throwError("Required argument strParam is null or not a string.");
        }
        var strNewURL = strURL;
        var strName = strParam.split("=")[0];
        if (strNewURL.indexOf(strName + "=") == -1) {
             strNewURL += (strNewURL.indexOf('?') > -1 ? '&' : '?') + strParam;
        }
        return strNewURL;
};
emxUICore.createImageButton = function _emxUICore_createImageButton(objDocument, strImage, fnOnClick, intWidth, intHeight) {
        if (!objDocument || typeof objDocument != "object") {
                emxUICore.throwError("Required argument objDocument is null or not an object.");
        } else if (!strImage || typeof strImage != "string") {
                emxUICore.throwError("Required argument strImage is null or not a string.");
        } else if (!fnOnClick || typeof fnOnClick != "function") {
                emxUICore.throwError("Required argument fnOnClick is null or not a function.");
        }
        var objImg = objDocument.createElement("img");
        objImg.src = strImage;
        objImg.onclick = fnOnClick;
        if (intWidth) {
                objImg.width = intWidth;
        }
        if (intHeight) {
                objImg.height = intHeight;
        }
        return objImg;
};
emxUICore.findFrame = function _emxUICore_findFrame(objWindow, strName) {
        if (!objWindow || typeof objWindow != "object") {
                emxUICore.throwError("Required argument objWindow is null or not an object.");
        } else if (!strName || typeof strName != "string") {
                emxUICore.throwError("Required argument strName is null or not a string.");
        }
        switch(strName) {
                case "_top":
                        return getTopWindow();
                case "_self":
                        return self;
                case "_parent":
                        return parent;
                default:
                     var objFrame = null;
		     try{
				for (var i = 0; i < objWindow.frames.length && !objFrame; i++) {
					try {
						if (objWindow.frames[i].name == strName) {
							objFrame = objWindow.frames[i];
						}
					} catch(ex){
					}
				}
				for (var i=0; i < objWindow.frames.length && !objFrame; i++) {
					objFrame = this.findFrame(objWindow.frames[i], strName);
				}
			}catch (e){}
                        return objFrame;
        }
};
//!     This function gets the height of the content inside
//!     a container (layer).
emxUICore.getContentHeight = function _emxUICore_getContentHeight(objDiv) {
        if (isIE) {
                return objDiv.clientHeight;
        } else {
                var intHeight = 0;
                for (var i=0; i < objDiv.childNodes.length; i++) {
                    if (objDiv.childNodes[i].nodeType == 1) {
                        if (objDiv.childNodes[i].offsetHeight == 0) {
                            for (var j=0; j < objDiv.childNodes[i].childNodes.length; j++) {
                                if (objDiv.childNodes[i].childNodes[j].nodeType == 1) {
                                    intHeight += objDiv.childNodes[i].childNodes[j].offsetHeight;
                                }
                            }
                        } else {
                            intHeight += objDiv.childNodes[i].offsetHeight;
                        }
                    }
                }
                return intHeight;
        }
};
//!     This function gets the width of the content inside
//!     a container (layer).
emxUICore.getContentWidth = function _emxUICore_getContentWidth(objDiv) {
        if (isIE) {
                return objDiv.clientWidth;
        } else {
                return (objDiv.childNodes.length > 0 ? objDiv.getElementsByTagName("*")[0].offsetWidth : objDiv.offsetWidth);
        }
};
emxUICore.getIcon = function _emxUICore_getIcon(strIcon) {
        if (typeof strIcon != "string") {
                emxUICore.throwError("Required argument strIcon is null or not a string.");
        }
        if ((strIcon.substring(1,8)) == "servlet" || strIcon.indexOf(emxUIConstants.DIR_SMALL_ICONS) > -1) {
                return strIcon;
        }
        else if (strIcon.indexOf("/") > -1) { // If the path of the image is specified then return as is
            return strIcon;
        }else { // If only image name is passed then append ../common/images to the image name to look for the image in common/images folder
                return emxUIConstants.DIR_SMALL_ICONS + strIcon;
        }
};
emxUICore.getNextElement = function _emxUICore_getNextElement(objElement) {
        if (typeof objElement != "object") {
                emxUICore.throwError("Required argument objElement is null or not an object.");
        }
        var objTemp = objElement.nextSibling;
        while(objTemp && objTemp.nodeType != 1) {
                objTemp = objTemp.nextSibling;
        }
        return objTemp;
};
emxUICore.getPreviousElement = function _emxUICore_getPreviousElement(objElement) {
        if (typeof objElement != "object") {
                emxUICore.throwError("Required argument objElement is null or not an object.");
        }
        var objTemp = objElement.previousSibling;
        while(objTemp && objTemp.nodeType != 1) {
            objTemp = objTemp.previousSibling;
        }
        return objTemp;
};
emxUICore.getParentObject = function _emxUICore_getParentObject(strObjectName) {
        if (typeof strObjectName != "string") {
                emxUICore.throwError("Required argument strObjectName is null or not a string.");
        }
        var objParent = parent;
        var objTemp = objParent[strObjectName];
        while(objTemp == null && objParent != getTopWindow()) {
                objParent = objParent.parent;
                objTemp = objParent[strObjectName];
        }
        return objTemp;
};
emxUICore.getUniqueID = function _emxUICore_getUniqueID_() {
        var strTemp = "emx";
        strTemp += ((new Date()).getTime() * Math.random());
        return strTemp;
};
emxUICore.iterateFrames = function _emxUICore_iterateFrames(fnExec, objRootWindow) {
        if (typeof fnExec != "function") {
                emxUICore.throwError("Required argument fnExec is null or not a function.");
        }
        function _emxUICore_iterateFramesEx(objWindow) {
                var execute = true;
        		try {
                	var temp = objWindow.name;
                } catch(ex){
                	execute = false;
                }
                if(execute) {
                	fnExec(objWindow);
                }

                if(objWindow && objWindow.frames){
                	for (var i=0; i < objWindow.frames.length; i++) {
                        _emxUICore_iterateFramesEx(objWindow.frames[i]);
                	}
                }
        }
        if(!objRootWindow) objRootWindow = getTopWindow();
        _emxUICore_iterateFramesEx(objRootWindow);
};
emxUICore.link = function _emxUICore_link(strURL, strTarget,typeOfEle) {
	if(emxUICore.objElem && !(typeOfEle=='undefined' && typeOfEle=="emxUIToolbarButton")){
		emxUICore.objElem.hide();
	}
	jQuery(typeOfEle).closest('.menu-panel').hide();

        if (typeof strURL === "string" && strURL.match(/^\s*$/)) {//<- just return if whitespace only strings
           return;
        } else if (typeof strURL != "string") {
                emxUICore.throwError("Required argument strURL is null or not a string.");
        } else if (strTarget && typeof strTarget != "string") {
                emxUICore.throwError("Required argument strTarget is null or not a string.");
        }
        if (strURL.indexOf("javascript:") == 0) {
        	 if (strURL.indexOf("javascript:getTopWindow().showSlideInDialog(") == 0) {
        		 //strURL = strURL.substring(0, strURL.lastIndexOf(")")) + ", \"" + window.name + "\")";
        		 strURL = strURL.replace('$OPENERFRAME$', window.name);
			}
			// ---------Start MSF Changes
			if(strURL.indexOf("msfBypass=true") >= 0) {
				getTopWindow().require(["DS/MSFDocumentManagement/MSFDocumentClient"], function(MSFDocumentClient) {				

					if(MSFDocumentClient.isConnectedWithMSF() === "true") {
						if(strURL.indexOf("isFromRMB=true")>=0){
							var arr = strURL.split('&');
							for (var i=0;i<arr.length;i++){
								if(arr[i].indexOf('emxTableRowId')>=0) {
									realId = arr[i].split('|')[1];
								}
							}
							strURL=''
							for (var i=0;i<arr.length;i++){
								if(arr[i].indexOf('objectId')>=0) {
									arr[i] = 'objectId='+realId;
								}
								strURL+=arr[i]+'&';
							}
							strURL = strURL.slice(0,-1);
						}

						var msfMessage = JSON.parse("{}");
						msfMessage["RequestType"] = "CheckIn";
						msfMessage["UrlParam"] = strURL;
						MSFDocumentClient.sendMessage(msfMessage);
						return;
					}
					eval(strURL);
 				},function() {
					eval(strURL);
 				});
			}
			else {
				eval(strURL);
			}	
			// ---------End MSF Changes
        } else {
                if (strTarget != null) {
                	/*if(strTarget == 'content') {
                		var objstf = emxUICore.findFrame(getTopWindow(), "emxUIStructureTree");
                		if(objstf){
                			strTarget = "detailsDisplay";
                		}
                	}*/
                        var objFrame = emxUICore.findFrame(window, strTarget);
                        if (objFrame == null) {
                        	objFrame = emxUICore.findFrame(getTopWindow(), strTarget);
                		}
                        if (objFrame) {
							if (jQuery.inArray(strTarget,HIDDEN_FRAME_LIST) != -1) {
								submitWithCSRF(strURL,objFrame);
							}else{
								if(!isChrome){
                                 objFrame.document.body.innerHTML="";
				                 objFrame.document.head.innerHTML="";
								}
                                 objFrame.location.href = strURL;
							}
                        } else {
                        	if(isComponentPage(strURL)){
                        		openNavigatorDialog(strURL);
                        	} else {
                        		window.open(strURL, strTarget);
                        	}
                        }
                }else if(strURL.indexOf("targetLocation")>=0){
                	var params = strURL.slice(window.location.href.indexOf('?') + 1).split('&');
                	var map = new Object();
                	for(var i=0; i< params.length;i++){
                		var param = params[i].split("=");
                		map[param[0]]=param[1];
					}
					if(map["targetLocation"] === "slidein"){
                			getTopWindow().showSlideInDialog(strURL , "true");
					}else if(map["targetLocation"] === "popup"){
                				if(map["isPopupModal"] == "true")
								{
									showModalDialog(strURL,"812","500",false,map["popupSize"]);
								}
								else
								{
									showNonModalDialog(strURL, "812","500", "true", '', map["popupSize"]);
								}
                			}else {                				
                				getTopWindow().location.href = strURL;
                	}
                } else {
                        document.location.href = strURL;
                }
        }
};
//! Public Method emxUICore.moveTo()
//!     This function moves an element to a specified pixel location.
emxUICore.moveTo = function _emxUICore_moveTo(objElement, intX, intY) {
        if (typeof objElement != "object") {
                emxUICore.throwError("Required argument objElement is null or not an object.");
        } else if (typeof intX != "number") {
                emxUICore.throwError("Required argument intX is null or not a number.");
        }
        objElement.style.left = intX + "px";
        if (intY != null && typeof intY == "number") {
                objElement.style.top = intY + "px";
        }
};
emxUICore.removeClass = function _emxUICore_removeClass(objElement, strClass) {
	if(objElement){
        var strOriginal = objElement.className || "";
        var arrClasses = strOriginal.split(" ");
        var arrNew = new Array;
        for (var i=0; i < arrClasses.length; i++) {
                if (arrClasses[i] != strClass) {
                        arrNew.push(arrClasses[i]);
                }
        }
        objElement.className =  arrNew.join(" ");
	}
};
emxUICore.throwError = function _emxUICore_throwError(strMessage) {
        if ((isMinIE55 && isWin) || isMoz || isEdge) {
                strMessage = strMessage.replace(/\n/g,"");
                eval("throw new Error(\""  + strMessage + "\")");
        } else {
                alert(emxUIConstants.STR_JS_Error + " " + strMessage);
        }
};
emxUICore.addEventHandler = function _emxUICore_addEventHandler(objElement, strType, fnHandler, blnCapture) {
                addEvent(objElement, strType, fnHandler)
	//getTopWindow().UWA.objElement.addEvent(strType,fnHandler);
};
//! Public Method emxUICore.addStyleSheet()
//!     This function adds a style sheet to the given document.
emxUICore.addStyleSheet = function _emxUICore_addStyleSheet(strCSSPrefix) {
        var strCSSFile = getStyleSheet(strCSSPrefix);
        document.write("<link rel=\"stylesheet\" type=\"text/css\" ");
        document.write("href=\"" + strCSSFile + "\">");
}
emxUICore.cancelUserSelect = function _emxUICore_cancelUserSelect_IE(objElement, objWin) {
        if (isIE) {
                objElement.onselectstart = function() { emxUICore.getEvent(objWin).preventDefault() };
        } else {
                objElement.style.mozUserSelect = "none";
        }
};
emxUICore.checkDOMError = function _emxUICore_checkDOMError_Moz(objXML, strFile) {
        if (isIE) {
                if (objXML.parseError != 0) {
                        emxUICore.throwError("XML file " + strFile + " could not be parsed. " + objXML.parseError.reason + "(Line: " + objXML.parseError.line + ", Char: " + objXML.parseError.linepos + ").");
                }
        } else {
                if (objXML.documentElement.tagName == "parsererror") {
                        emxUICore.throwError("XML file " + strFile + " could not be parsed. The file may either be missing or not well-formed.");
                }
        }
};
emxUICore.createXMLDOM = function _emxUICore_createXMLDOM(namespace) {

		/*
        if (isIE) {
                var obj = new ActiveXObject("MSXML2.DOMDocument");
                obj.setProperty("SelectionLanguage", "XPath");
  if (namespace != null) {
   obj.setProperty("SelectionNamespaces", namespace);
  } else {
   obj.setProperty("SelectionNamespaces", "xmlns:aef='http://www.matrixone.com/aef'");
  }
                return obj;
        } else {
                return document.implementation.createDocument("", "", null);
        }*/

       return XMLHelper.CreateXMLDocument(namespace);
};
emxUICore.transformToText = function _emxUICore_transformToText(objXML, objXSLT) {
	ElapsedTimer.enter();
    try {
        if (isIE) {
            var strText = objXML.transformNode(objXSLT);
            ElapsedTimer.exit("1");
            return strText;
        } else {
/*
                if (!isUnix) {
*/
                        var objProcessor = new XSLTProcessor();
                        objProcessor.importStylesheet(objXSLT);
                        var objResult = objProcessor.transformToDocument(objXML);
                        if (objResult.documentElement.tagName == "result" || objResult.documentElement.tagName == "transformiix:result") {
                                var strResult = objResult.documentElement.xml;
                        		ElapsedTimer.exit("2");
                                //return strResult.substring(strResult.indexOf(">")+1, strResult.indexOf("</transformiix:result>"));
                                strResult  = strResult.substring(strResult.indexOf(">")+1, strResult.length);
								strResult = strResult.replace(/<\/transformiix:result>/gi,"");
								strResult = strResult.replace(/<transformiix:result>/gi,"");
                                return strResult;
                        } else {
                        		ElapsedTimer.exit("3");
                                return objResult.xml;
                        }
/*
                } else {
                        var objBuf = new emxUIStringBuffer;
                        objBuf.write("xml=");
//added for 309856
                        //objBuf.write(escape(objXML.xml));
                        objBuf.write(encodeURI(objXML.xml));
// till here
                        objBuf.write("&xslt=");
// added for 309856
                        //objBuf.write(escape(objXSLT.xml));
                        objBuf.write(encodeURI(objXSLT.xml));
// till here
                        var strResult = emxUICore.getDataPost("../common/emxTransform.jsp", objBuf.toString());
                        return strResult;
                }
*/
        }
    } catch (objError) {
        alert(emxUIConstants.STR_JS_ErrorOWTTT + " " + objError.message);
    }
	ElapsedTimer.exit("4");
};
emxUICore.getElementsByTagName = function (objRefNode, strTagName) {
    if (isIE || isMinFF3 || isChrome || isSafari || strTagName.indexOf(":") == -1) {
        return objRefNode.getElementsByTagName(strTagName);
    } else {
        return objRefNode.getElementsByTagName(strTagName.substring(strTagName.indexOf(":")+1));
    }
};

emxUICore.selectNodes = function (objRefNode, strXPath) {
	if (objRefNode.nodeType == 9) {
		objRefNode = objRefNode.documentElement;
	}
    if (isIE) {
        var arrNodes = objRefNode.selectNodes(strXPath);
        var arrResult = new Array;
        for (var i=0; i < arrNodes.length; i++) {
            arrResult.push(arrNodes[i]);
        }
        return arrResult;
    } else {
        var objEvaluator = new XPathEvaluator();
        var objNSResolver = objEvaluator.createNSResolver(objRefNode.ownerDocument.documentElement);
        var objNodes = objEvaluator.evaluate(strXPath, objRefNode, objNSResolver, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
        var arrResult = new Array;
        var objNext;
        while (objNext = objNodes.iterateNext()) {
            arrResult.push(objNext);
        }
        return arrResult;
    }
};

emxUICore.selectSingleNode = function (objRefNode, strXPath) {
	if (objRefNode.nodeType == 9) {
		objRefNode = objRefNode.documentElement;
	}
    if (isIE) {
        return objRefNode.selectSingleNode(strXPath);
    } else {
        var objEvaluator = new XPathEvaluator();
        var objNSResolver = objEvaluator.createNSResolver(objRefNode.ownerDocument.documentElement);
        var objNodes = objEvaluator.evaluate(strXPath, objRefNode, objNSResolver, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
        return objNodes.iterateNext();
    }
};


emxUICore.createHttpRequest = function _emxUICore_createHttpRequest() {
        if (isIE) {
                return new ActiveXObject("Microsoft.XMLHTTP");
        } else {
                return new XMLHttpRequest;
        }
};
emxUICore.getActualLeft = function _emxUICore_getActualLeft(objElement) {
        var intLeft = objElement.offsetLeft;
        var objParent;
        if(isMaxIE8){
         objParent = objElement.parentNode;}
        else
       {  objParent = objElement.offsetParent;  }
        if (isIE) {
                while(objParent != null) {
                        if (objParent.tagName == "TD") {
                                intLeft += objParent.clientLeft;
                        }
                        intLeft += objParent.offsetLeft;
                        objParent = objParent.offsetParent;
                }
        } else {
                while(objParent != null) {
                        if (objParent.tagName == "TABLE") {
                                var intBorder = parseInt(objParent.border);
                                if (isNaN(intBorder)) {
                                        if (objParent.getAttribute("frame") != null) {
                                                intLeft += 1;
                                        }
                                } else {
                                        intLeft += intBorder;
                                }
                        }
                        intLeft += objParent.offsetLeft;
                        objParent = objParent.offsetParent;
                }
        }
        return intLeft;
};
emxUICore.getActualTop = function _emxUICore_getActualTop(objElement) {
        var intTop = objElement.offsetTop;
        var  objParent;
        if(isMaxIE8 && objElement.getAttribute("massUpdate") != "true"){
            objParent = objElement.parentNode;
        }else {
           objParent = objElement.offsetParent;}
        if (isIE) {
                while(objParent != null) {
                        if (objParent.tagName == "TD") {
                                intTop += objParent.clientTop;
                        }
                        intTop += objParent.offsetTop;
                        objParent = objParent.offsetParent;
						if(objParent != null && objParent.tagName == "TABLE")
						{
							if($(objParent).find('input[name="searchType"]').length>0)
							{
								intTop -= $(objParent).find('input[name="searchType"]').eq(0).parent().height();
							}
						}
                }
        } else {
                while(objParent != null) {
                        if (objParent.tagName == "TABLE") {
                                var intBorder = parseInt(objParent.border);
                                if (isNaN(intBorder)) {
                                        if (objParent.getAttribute("frame") != null) {
                                                intTop += 1;
                                        }
                                } else {
                                        intTop += intBorder;
                                }
								if($(objParent).find('input[name="searchType"]').length > 0)
								{
										intTop -= $(objParent).find('input[name="searchType"]').eq(0).parent().height();
								}		
                        }
                        intTop += objParent.offsetTop;
                        objParent = objParent.offsetParent;
                }
        }
      // objParent = objElement.offsetParent;
       while (objParent != null){
       		if ((objParent.tagName == "DIV") && (objParent.scrollTop != 0)){
       			intTop -= objParent.scrollTop;
       		}
       		objParent = objParent.offsetParent;
       }
        return intTop;
};

emxUICore.getScrollTop = function _emxUICore_getScrollTop(objElement) {
        var intTop = objElement.offsetTop;
        intTop -= objElement.scrollTop;
        var objParent = objElement.offsetParent;
        if (isIE) {
                while(objParent != null) {
                        if (objParent.tagName == "TD") {
                                intTop += objParent.clientTop;
                                intTop -= objParent.scrollTop;
                        }
                        intTop += objParent.offsetTop;
                        intTop -= objParent.scrollTop;
                        objParent = objParent.offsetParent;
                }
        } else {
                while(objParent != null) {
                        if (objParent.tagName == "TABLE") {
                                var intBorder = parseInt(objParent.border);
                                if (isNaN(intBorder)) {
                                        if (objParent.getAttribute("frame") != null) {
                                                intTop += 1;
                                        }
                                } else {
                                        intTop += intBorder;
                                }
}
                        intTop += objParent.offsetTop;
                        intTop -= objParent.scrollTop;
                        objParent = objParent.offsetParent;
                }
        }
        return intTop;
};

//! Public Method emxUICore.getCurrentStyle()
//!     This function gets the current style object for the given
//!     DOM object.
emxUICore.getCurrentStyle = function _emxUICore_getCurrentStyle(objElement, objWindow) {
        if (isIE) {
                return objElement.currentStyle;
        } else {
                return document.defaultView.getComputedStyle(objElement, "");
        }
}
//added for BUG: 347348
emxUICore.isValidPageURL = function _emxUICore_isValidPageURL(strURL) {
	var objHTTP = this.createHttpRequest();
	objHTTP.open("get", strURL, false);
	objHTTP.send(null);
	if (objHTTP.status == 404)
		return false;
	else
		return true;
};
//End for BUG: 347348



//------------------------------------------------------------------------------
//--------  Wrappers for XMLHttpRequest for Ajax style programming -------------

emxUICore.createHttpRequest = function _emxUICore_createHttpRequest() {
    if (isIE) {
		if (typeof XMLHttpRequest != "undefined") {
	        return new XMLHttpRequest();
	    } else {
            return new ActiveXObject("Microsoft.XMLHTTP");
        }
    } else {
        return new XMLHttpRequest;
    }
};

emxUICore.checkResponse = function _emxUICore_checkResponse(objHTTP) {
//	if (objHTTP.getResponseHeader("X-MX-LOGIN-HEADER-TODO") != "") {
//	  logged out
//	}
	if (objHTTP.status == 200) {
		return;
	} else if(objHTTP.status == 401){
		getTopWindow().location.href = "../common/emxNavigatorErrorPage.jsp?errorCode=401";
	} else{
		emxUICore.throwError("HTTP response code = " + objHTTP.status
							+ "\n" + objHTTP.statusText);
	}
}

//End for BUG: 347348
// STOP! are you sure you want to use getData and not getDataPost()?
// Using GET instead of POST is always going to get you in trouble
// with long URL's.
emxUICore.getData = function _emxUICore_getData(strURL, fnCallback, oClientData) {
	ElapsedTimer.enter(strURL + " " + fnCallback ? ("ASYNCH: ") : "synchronous");
	if (typeof strURL != "string") {
		ElapsedTimer.exit("FAIL");
		emxUICore.throwError("Required parameter strURL is null or not a string.");
	}

	var objHTTP = this.createHttpRequest();
	objHTTP.open("get", strURL, fnCallback != null);

	if (fnCallback) {
		if (typeof fnCallback != "function") {
			ElapsedTimer.exit("FAIL2");
			emxUICore.throwError("Optional parameter fnCallback is not null and not a function.");
		}
		objHTTP.onreadystatechange = function _getData_readyStateChange() {
			if (objHTTP.readyState == 4) {
				ElapsedTimer.info("getData Data callback: " + ElapsedTimer.fn(fnCallback));
				emxUICore.checkResponse(objHTTP);
				//ElapsedTimer.enter("onreadystatechange: ->" + objHTTP.readyState + ' ' + objHTTP.responseText.length + " chars");
				fnCallback(objHTTP.responseText, oClientData, objHTTP);
				objHTTP.onreadystatechange = null;
				//ElapsedTimer.exit("onreadystatechange");
			}
		};
		addSecureTokenHeader(objHTTP);
		objHTTP.send(null);
		ElapsedTimer.exit('submitted');
		return objHTTP;
	} else {
		objHTTP.send(null);
		emxUICore.checkResponse(objHTTP);
		ElapsedTimer.exit(objHTTP.responseText.length + ' chars');
		try {
			return objHTTP.responseText;
		} finally {
			objHTTP = null;
		}
	}
};

emxUICore.getEvent = function _emxUICore_getEvent_IE(objWin) {

        var objEvent;
        if (isIE) {
		try{
                	objWin = (objWin == null ? window : objWin);
                	objEvent = objWin.event;
		}catch(e) {}
                if (objEvent == null) {
                    emxUICore.throwError("No event object found.");
                }
                if (!objEvent.target) {
                    objEvent.target = objEvent.srcElement;
                }
                objEvent.currentTarget = objEvent.target;
                objEvent.charCode = objEvent.keyCode;
                if(!objEvent.preventDefault){
                	objEvent.preventDefault = function () { this.returnValue = false; };
                }
                if(!objEvent.stopPropagation){
                	objEvent.stopPropagation = function () { this.cancelBubble = true; };
                }

        } else {
                objEvent = arguments.callee.caller.arguments[0];
        }
        return objEvent;
};

emxUICore.getMouseButton = function _emxUICore_getMouseButton(objEvent) {
	var button;
    if (!objEvent.which)
        /* IE case */
        button= (objEvent.button < 2) ? "LEFT" :
                  ((objEvent.button == 4) ? "MIDDLE" : "RIGHT");
     else
        /* All others */
        button= (objEvent.which < 2) ? "LEFT" :
                  ((objEvent.which == 2) ? "MIDDLE" : "RIGHT");
	return button;
};

emxUICore.isLeftButton = function _emxUICore_isLeftButton(objEvent) {
	return this.getMouseButton(objEvent) == "LEFT";
};

//Added for Bug : 353307
emxUICore.clonedForm = function _emxUICore_clonedForm (srcForm,formParent)
{
	 var inputElements = srcForm.getElementsByTagName("input");
	 var outForm       = srcForm.cloneNode(false);

	 formParent.appendChild(outForm);
     var allowedElements = [];
     var rootFound = false;
     for(var i = 0 ; i < inputElements.length; i++)
     {
  	   if(inputElements[i].type == "hidden" && inputElements[i].name != "emxTableRowId" && inputElements[i].name != "emxTableRowIdActual")
  	   {
  		  var clonedItem = inputElements[i].cloneNode(true);
  		  if(!rootFound)
  		  {
		 	if(clonedItem.name == "objectId")
			{
				clonedItem.name = "rootObjectId";
				rootFound = true;
			}
  		  }
  		  allowedElements.push(clonedItem);
  		}
     }

     for(var j = 0 ; j < allowedElements.length; j++)
     {
       outForm.appendChild(allowedElements[j]);
     }

	 return outForm;
}


//! Private Function submitList()
//!     This function handles actions on a list of items. It takes a URL
//!     and assigns it to a form, submits the form, and displays the
//!     result in a specified location.
function submitList(strURL, strTarget, strRowSelect, bPopup, iWidth, iHeight, confirmationMessage, strMethod, strPopupSize,selectedCount,isModal, slideinWidth){
	var curFrameName;
	try {
		curFrameName = this.name;
	}
	catch(e)
    {
		curFrameName = "";
    }
	var objListWindow = findFrame(this, "listDisplay");

	if(!objListWindow){
		  objListWindow = findFrame(parent, curFrameName);
		  var objListChildWindow = findFrame(objListWindow, "listDisplay");
		  objListWindow = objListChildWindow ? objListChildWindow : objListWindow;
	}

	if (!objListWindow || objListWindow.noListSubmission){
		objListWindow = findFrame(parent, "listDisplay");

		if (!objListWindow || objListWindow.noListSubmission){
			objListWindow = this.frames['pagecontent'];
			if(objListWindow){
				curFrameName = 'pagecontent';
			}
		}

		if (!objListWindow || objListWindow.noListSubmission){
			objListWindow = this.frames[0];
		}
	}

	var objForm = null;
	if (objListWindow)
	{
		objForm= objListWindow.document.forms['emxSubmitForm'] ? objListWindow.document.forms['emxSubmitForm'] : objListWindow.document.forms['emxTableForm'];
		if (objForm == null)
		{
			objForm = objListWindow.document.forms[0];
		}
	}

	if (objForm == null)
	{
		objForm = document.forms[0];
	}

	var iChecks = 0;
if(selectedCount){
	iChecks = selectedCount;
}
if(iChecks == 0){
	var isOkToSubmit=true;
	var bResponse=false;

	var strIds = null;
	//Modified for Bug : 353307
	if(strRowSelect != "none" && strRowSelect != "rmb")
	{
		strIds = parent.ids;
		if(strIds == null || strIds == 'undefined' || strIds == '~')
		{
			if( ('ids' in window) && ids && ids != null && ids != 'undefined')
				strIds = ids;
			else
				strIds = "~";
		}
	}

	if (strIds != null && strIds != undefined && typeof strIds == "string") {
		var arrTemp = strIds.split("~");
		iChecks = arrTemp.length - 2;
	}
}
	if (strRowSelect == "single" && iChecks > 1) {
		showError(emxUIConstants.ERR_SELECT_ONLY_ONE);
		return;
	} else if ((iChecks == 0) && strRowSelect != "none" && strRowSelect != "rmb" ) {
		showError(emxUIConstants.ERR_NONE_SELECTED);
		return;
	}
	if(confirmationMessage!=null && confirmationMessage!="undefined" && confirmationMessage!="null" && confirmationMessage!=""){
		if (confirmationMessage.indexOf("${TABLE_SELECTED_COUNT}") > 0){
			var selectedCount = 0;
			if (strIds) {
				var arrayItems = strIds.match(new RegExp("~", "g"));
				if (arrayItems != null){
					selectedCount = arrayItems.length -1;
				}
			}else if(strRowSelect != "rmb")
			{
				selectedCount = 1;
			}
			confirmationMessage = confirmationMessage.replace(new RegExp("\\$\\{TABLE_SELECTED_COUNT\\}","g"), selectedCount);
		}
		var bResponse=window.confirm(confirmationMessage);
		if(bResponse){
			isOkToSubmit=true;
		} else {
			return;
		}
	}
	if(strRowSelect == "rmb")
	{
		strURL += "&isFromRMB=true";
	}
	if(isSnN() && isFullSearchPage(strURL)){
		var strIdsTable = null;
		for(var i=0; i<objForm.length; i++){
			if(objForm[i].name === 'uiType' && objForm[i].value === 'table'){
				strIdsTable = parent.ids;
				if(strIdsTable == null || strIdsTable == 'undefined' || strIdsTable == '~'){
					if( ('ids' in window) && ids && ids != null && ids != 'undefined')
						strIdsTable = ids;
					else
						strIdsTable = "~";
				}
			}
		}

		for(var i=0; i<objForm.length; i++){
			if(objForm[i].name !== 'uiType'){
				if(objForm[i].name === 'emxTableRowId' && strIdsTable !== null && strIdsTable === '~'){

				}else{
					strURL+="&"+objForm[i].name+"="+objForm[i].value;
				}
			}
		}
		showModalDialog(strURL);
		return;
	}
	if (bPopup) {
		if(isComponentPage(strURL)){
			objForm.action = strURL;
			var tempurl = "../common/emxNavigatorSubmitPopup.jsp?frameName=" + curFrameName + "&targetLocation="+strTarget+"&url=";
			if(isModal == "true"){
				showModalDialog(tempurl, iWidth, iHeight, "true", strPopupSize);
			}else{
				showNonModalDialog(tempurl, iWidth, iHeight, "true", '', strPopupSize);
			}
		}else {
			if(isModal == "true"){
				showModalDialog("../common/emxAEFSubmitPopupAction.jsp?frameName=" + curFrameName + "&url="+escape(strURL), iWidth, iHeight, "true", strPopupSize);
			}else{
				showNonModalDialog("../common/emxAEFSubmitPopupAction.jsp?frameName=" + curFrameName + "&url="+escape(strURL), iWidth, iHeight, "true", '', strPopupSize);
			}
		}
		return;
	} else if(strTarget == 'slidein'){
		//showModalDialog("../common/emxAEFSubmitPopupAction.jsp?url="+escape(strURL), iWidth, iHeight, "true", strPopupSize);
		strURL = encodeHREF(strURL+"&targetLocation=slidein");

		if(this.location.href.indexOf("portalMode=true")>-1){
			getTopWindow().showSlideInDialog("../common/emxAEFSubmitSlideInAction.jsp?portalMode=true&portalFrame=" + curFrameName  + "&url="+strURL , "true", window.name, "", slideinWidth);
		}
		else{
		    getTopWindow().showSlideInDialog("../common/emxAEFSubmitSlideInAction.jsp?portalMode=false&frameName=" + curFrameName + "+&url="+strURL , "true", window.name, "", slideinWidth);
		}
		return;
	}
	var rmbFormParent = null;
	var rmbSubmit     = false;
	if(strRowSelect == "rmb")
	{
		if (objListWindow)
		{
			rmbFormParent = objListWindow.document.body;
		}else
		{
			rmbFormParent = document.body;
		}

		rmbSubmit = true;
		objForm = emxUICore.clonedForm(objForm,rmbFormParent);
	}

	if (strTarget){
		objForm.target = strTarget;
	}
	if(isOkToSubmit) {
		addSecureToken(objForm);
		if(this.location.href.indexOf("portalMode=true")>-1){
			strURL += "&portalMode=true&frameName=" + curFrameName + "";
		}
		else{
			strURL += "&portalMode=false&frameName=" + curFrameName + "";
		}
		objForm.action = strURL;
		if(strMethod != null && strMethod != "") {
			objForm.method = strMethod;
		}else {
			objForm.method = "post";
		}
		objForm.submit();
		removeSecureToken(objForm);

	}

	//Added for Bug : 353307
	if(rmbSubmit)
	{
		rmbFormParent.removeChild(objForm);
	}
}

//! Private Method emxUICore.getStyleSheet()
//!     This function creates the style sheet string for a given style sheet prefix.
emxUICore.getStyleSheet = function _emxUICore_getStyleSheet(strCSSPrefix) {
        var strCSSFile = emxUIConstants.DIR_STYLES + strCSSPrefix;
        if (isUnix) {
                strCSSFile += "_Unix.css";
        } else {
                strCSSFile += ".css";
        }
        return strCSSFile;
}
emxUICore.getTagNameEMX = function _emxUICore_getTagNameEMX(objElement) {
        if (isIE) {
                return (objElement.tagName.indexOf("emx:") == -1 ? "emx:" + objElement.tagName : objElement.tagName);
        } else {
                return (objElement.tagName.toLowerCase().indexOf("emx:") == -1 ? "emx:" + objElement.tagName.toLowerCase() : objElement.tagName.toLowerCase());
        }
};
emxUICore.getWinHeight = function _emxUICore_getWinHeight(objWin) {
	objWin = (objWin == null ? window : objWin);
	if (isIE) {
		var doc = objWin.document;
		if (doc.documentElement && doc.documentElement.clientHeight)
		{
			return doc.documentElement.clientHeight;
		}
		else if (doc.body)
		{
			return doc.body.clientHeight;
		}
	}
	else
	{
		return objWin.innerHeight;
	}
};

emxUICore.getWinWidth = function _emxUICore_getWinWidth_IE(objWin) {
	objWin = (objWin == null ? window : objWin);
	if (isIE)
	{
		if (document.documentElement && document.documentElement.clientWidth)
		{
			return document.documentElement.clientWidth;
		}
		else if (document.body)
		{
			return document.body.clientWidth;
		}
	 }
	else
	{
         return objWin.innerWidth;
    }
};

//Depricated
emxUICore.getWindowHeight = function _emxUICore_getWindowHeight(objWin) {
        objWin = (objWin == null ? window : objWin);
        if (isIE) {
                return objWin.document.body.clientHeight;
        } else {
                return objWin.innerHeight;
        }
};
//Depricated
emxUICore.getWindowWidth = function _emxUICore_getWindowWidth_IE(objWin) {
        objWin = (objWin == null ? window : objWin);
        if (isIE) {
                return objWin.document.body.clientWidth;
        } else {
                return objWin.innerWidth;
        }
};
emxUICore.setText = function _emxUICore_setText(objXML, value) {
        if(objXML != null){
        	if(isIE) {
        		(objXML.text != null)? objXML.text = value : objXML.innerText = value;
        	} else {
            	var objDOM = this.createXMLDOM();
            	var nTextNode = objDOM.createTextNode(value);
            	while ( objXML.hasChildNodes() ) {
              		objXML.removeChild( objXML.firstChild );
            	}
               	objXML.appendChild(nTextNode);
            	}
         	}
};
emxUICore.getText = function _emxUICore_setText(objXML) {
        var value = null;
        if(isIE) {
            value = objXML.text;
        } else {
            value = objXML.textContent;
        }

        return value;
};
emxUICore.getXML = function _emxUICore_getXML(objXML) {
    var value = "";
    for(var i=0; i < objXML.childNodes.length; i++){
    	value = value + objXML.childNodes[i].xml;
	}
    return value;
};
emxUICore.getXMLData = function _emxUICore_getXMLData(strURL, namespace) {
        ElapsedTimer.enter('getXMLData');
        if(typeof strURL != "string") {
                emxUICore.throwError("Required parameter strURL is null or not a string.");
        }

		//IR-134058V6R2013
		var ts = new Date().getTime();
		if (strURL.indexOf("?") != -1)
		{
			strURL += "&ajax-no-cache=" + ts;
		}
		else
		{
			strURL += "?ajax-no-cache=" + ts;
		}

        var objHTTP = this.createHttpRequest();
        objHTTP.open("get", strURL, false);
        //objHTTP.setRequestHeader("Content-type","application/x-www-form-urlencoded; charset=UTF-8");
        objHTTP.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        objHTTP.setRequestHeader("charset", "UTF-8");
        addSecureTokenHeader(objHTTP);
        objHTTP.send(null);
		emxUICore.checkResponse(objHTTP);
		var responseText = "";
		try{
			responseText = objHTTP.responseText;
		}catch(e){
			//console.log(e);
			if(e.number === -2147024882 || e.name === "NS_ERROR_OUT_OF_MEMORY"){
				alert(emxUIConstants.STR_NotEnoughStorageMsg);
			}
			return;
		}

        var objDOM = this.createXMLDOM(namespace);
        objDOM.loadXML(responseText);
        this.checkDOMError(objDOM);
        ElapsedTimer.exit('getXMLData' + ' ' + responseText.length + ' chars');
        try {
        return objDOM;
        } finally {
        	objDOM = null;
        	objHTTP = null;
		    responseText = null;
        }

};

// POST a request and get back the resulting response as an XML DOM.
// The POST'ed data may either be XML (document or element) or url-encoded form data.
// The request may either be synchronous, or asynchronous with callback.
//
// NOTE: In synchronous (blocking) mode the entire browser locks up while the request
//       is processed.  Use asynch mode if possible.
//
// Arguments:
//   strURL              - This is passed as-is to the server along with the POST verb
//   objXmlOrStrFormData - This can be EITHER a string containing url-encoded form data, OR
//                         an XML object (Document or Element).  If it is XML, a string representation
//                         of the XML will be sent to the server with ContentType text/xml.
//                         CAREFUL! do not pass in oXML.xml expecting to have it treated as XML;
//                         that would be seen as a string, and thus treated as form data
//   fnCallback          - If this is non-null, it indicates that this will be an asynchronous
//                         (non-blocking) request.  fnCallback in this case must be a function,
//                         (not a string to be eval'ed), whose signature is expected to be:
//                         fnCallback(oXML, oClientData, oHTTP)
//                         When the request response is received, fnCallback is called back
//                         with an XML DOM constructed from from the responseText, and also with
//                         oClientData, and the the HTTPRequest itself (for further inspection if necessary)
//   oClientData         - This is arbitrary data that the caller may use to communicate down into the
//                         callback function
// Returns:
//   - In synchronous mode (fnCallback == null) this function returns an XML DOM created from
//     the responseText.
//   - In asynch mode, this function immediately returns the HTTP request object.

emxUICore.getXMLDataPost = function _emxUICore_getXMLDataPost(strURL, objXmlOrStrFormData, fnCallback, oClientData, namespace) {
	if (typeof strURL != "string") {
		ElapsedTimer.exit("FAIL");
		emxUICore.throwError("Required parameter strURL is null or not a string.");
	}

	var strData, contentType;
	if (objXmlOrStrFormData) {
		if (objXmlOrStrFormData.xml) {
			strData = objXmlOrStrFormData.xml;
			contentType = "text/xml";
		} else {
			strData = objXmlOrStrFormData;
			contentType = "application/x-www-form-urlencoded";
			if (strData.charAt(0) == "<") {
				throwError("Parameter objXmlOrStrFormData appears to be XML text, not an XML object");
			}
		}
	}
	ElapsedTimer.enter(strURL + " " + strData + " " + fnCallback ? ("ASYNCH: ") : "synchronous");

	var objHTTP = this.createHttpRequest();
	objHTTP.open("post", strURL, fnCallback != null);
	objHTTP.setRequestHeader("Content-Type", contentType);
	objHTTP.setRequestHeader("charset", "UTF-8");
	addSecureTokenHeader(objHTTP);
	if (fnCallback) {
		if (typeof fnCallback != "function") {
			ElapsedTimer.exit("FAIL2");
			emxUICore.throwError("Optional parameter fnCallback is not null and not a function.");
		}
		objHTTP.onreadystatechange = function getXMLDataPost_readyStateChange() {
			/*370484*/

			if (objHTTP.readyState == 4 && objHTTP.responseText != "") {
				//emxUICore.checkResponse(objHTTP);
				ElapsedTimer.info("getXMLDataPost: Data callback: " + ElapsedTimer.fn(fnCallback));
				ElapsedTimer.enter("onreadystatechange: ->" + objHTTP.readyState + ' ' + objHTTP.responseText.length + " chars");
				fnCallback(emxUICore.GetXMLDom(objHTTP.responseText), oClientData, objHTTP);
				objHTTP.onreadystatechange = null;
				ElapsedTimer.exit("onreadystatechange");
			}
		}
		objHTTP.send(strData);
		ElapsedTimer.exit();
		return objHTTP;
	} else {
		objHTTP.send(strData);
		emxUICore.checkResponse(objHTTP);

		var objDOM = this.createXMLDOM(namespace);
		objDOM.loadXML(objHTTP.responseText);
		this.checkDOMError(objDOM);
		ElapsedTimer.exit(objHTTP.responseText.length + ' chars');
		try {
        	return objDOM;
        } finally {
        	objDOM = null;
        	objHTTP = null;
        }
	}
};

/**
* This is a private method created for IR-IR-153125V6R2013x
* Search for referece of Error|:Session|:TimeOut in emxNavigatorBaseInclude.inc and emxUIFreezePane.js
*/
function loginchk(serverResponse) {
	if(serverResponse.indexOf("Error|:Session|:TimeOut") != -1){
		window[document.location.href = document.location.href];
		return true;
	}else{
		return false;
	}
};
emxUICore.getTextXMLDataPost = function _emxUICore_getTextXMLDataPost(strURL, strData) {
        ElapsedTimer.enter('getTextXMLDataPost' + ' ' + strURL + " " + strData);
        if(typeof strURL != "string") {
                emxUICore.throwError("Required parameter strURL is null or not a string.");
        }
        var objHTTP = this.createHttpRequest();
        objHTTP.open("post", strURL, false);
        objHTTP.setRequestHeader("Content-type","text/xml");
        addSecureTokenHeader(objHTTP);
        objHTTP.send(strData);
		emxUICore.checkResponse(objHTTP);

        var objDOM = this.createXMLDOM();
        objDOM.loadXML(objHTTP.responseText);
        if(loginchk(objHTTP.responseText)){
        	return objHTTP.responseText;
        }
        this.checkDOMError(objDOM);
        ElapsedTimer.exit('getTextXMLDataPost' + ' ' + objHTTP.responseText.length + ' chars');
        return objDOM;
};



emxUICore.downloadPDFPost = function _emxUICore_downloadPDFPost(strURL,strData) {

	if (isIE) {

		ElapsedTimer.enter('getTextXMLDataPost' + ' ' + strURL + " " + strData);

		if (typeof strURL != "string") {
			emxUICore.throwError("Required parameter strURL is null or not a string.");
		}

		var objHTTP = this.createHttpRequest();
		objHTTP.open("post", strURL, true);
		objHTTP.setRequestHeader("Content-type", "text/html");
		addSecureTokenHeader(objHTTP);
		objHTTP.responseType = 'blob';
		objHTTP.send(strData);

		objHTTP.onload = function(event) {
			if (objHTTP.status == 200) {
				var bb = new MSBlobBuilder();
				bb.append(objHTTP.response);
				var blob = bb.getBlob('application/pdf');
				var url = URL.createObjectURL(blob);
				var filename = objHTTP.getResponseHeader('Content-Disposition');
				filename = filename.substring(filename.indexOf("=") + 2,filename.lastIndexOf("\""));

				window.navigator.msSaveOrOpenBlob(blob, filename);

			} else {
				alert('Sorry, PDF was not generated');
			}
		};
	} else {
		var a = document.createElement('a');
		a.setAttribute('href', '#');
		a.setAttribute('id', 'sim');
		document.body.appendChild(a);

		ElapsedTimer.enter('getTextXMLDataPost' + ' ' + strURL + " " + strData);

		if (typeof strURL != "string") {
			emxUICore.throwError("Required parameter strURL is null or not a string.");
		}

		var objHTTP = this.createHttpRequest();
		objHTTP.open("post", strURL, true);
		objHTTP.setRequestHeader("Content-type", "text/html");
		addSecureTokenHeader(objHTTP);
		objHTTP.responseType = 'blob';

		objHTTP.onload = function(event) {
			if (objHTTP.status == 200) {
				var blob = new Blob([ objHTTP.response ], {
					type : 'application/pdf'
				});
				var url = URL.createObjectURL(blob);
				var link = document.querySelector('#sim');
				link.setAttribute('href', url);
				var filename = objHTTP.getResponseHeader('Content-Disposition');
				filename = filename.substring(filename.indexOf("=") + 2,filename.lastIndexOf("\""));
				filename = filename.replace("%c2%a0","");

				$('#sim').attr("download", filename);
				link.click();

			} else {
				alert('Sorry, PDF was not generated');
			}
		};
		objHTTP.send(strData);
		//emxUICore.checkResponse(objHTTP);
	}
};


// POST a request and get back the resulting response as straight text.
// The POST'ed data is expected to be url-encoded form data.
// The request may either be synchronous, or asynchronous with callback.
//
// NOTE: In synchronous (blocking) mode the entire browser locks up while the request
//       is processed.  Use asynch mode if possible.
//
// Arguments:
//   strURL          - This is passed as-is to the server along with the POST verb
//   strData         - This should be url-encoded form data.  No further encoding is done here.
//   fnCallback      - If this is non-null, it indicates that this will be an asynchronous
//                     (non-blocking) request.  fnCallback in this case must be a function,
//                     (not a string to be eval'ed), whose signature is expected to be:
//                     fnCallback(strResponseText, oClientData, oHTTP)
//                     When the request response is received, fnCallback is called back
//                     with theresponseText, and also with
//                     oClientData, and the the HTTPRequest itself (for further inspection if necessary)
//   oClientData     - This is arbitrary data that the caller may use to communicate down into the
//                     callback function
// Returns:
//   - In synchronous mode (fnCallback == null) this function returns the responseText.
//   - In asynch mode, this function immediately returns the HTTP request object.  The response
//     text is eventually passed to fnCallback.
emxUICore.getDataPost = function _emxUICore_getDataPost(strURL, strData, fnCallback, oClientData) {
	ElapsedTimer.enter(strURL + " " + strData + " " + fnCallback ? ("ASYNCH: ") : "synchronous");
	if (typeof strURL != "string") {
		ElapsedTimer.exit("FAIL");
		emxUICore.throwError("Required parameter strURL is null or not a string.");
	}

	var objHTTP = this.createHttpRequest();
	objHTTP.open("post", strURL, fnCallback != null);
	objHTTP.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	//	objHTTP.setRequestHeader("charset", "UTF-8");
	addSecureTokenHeader(objHTTP);
	if (fnCallback) {
		if (typeof fnCallback != "function") {
			ElapsedTimer.exit("FAIL2");
			emxUICore.throwError("Optional parameter fnCallback is not null and not a function.");
		}
		objHTTP.onreadystatechange = function getXMLDataPost_readyStateChange() {
			if (objHTTP.readyState == 4) {
				//emxUICore.checkResponse(objHTTP);
				ElapsedTimer.info("getDataPost: Data callback: " + ElapsedTimer.fn(fnCallback));
				ElapsedTimer.enter("onreadystatechange: ->" + objHTTP.readyState + ' ' + objHTTP.responseText.length + " chars");
				fnCallback(objHTTP.responseText, oClientData, objHTTP);
				objHTTP.onreadystatechange = null;
				ElapsedTimer.exit("onreadystatechange");
			}
		}
		objHTTP.send(strData);
		ElapsedTimer.exit();
		return objHTTP;
	} else {
		objHTTP.send(strData);
		emxUICore.checkResponse(objHTTP);
		ElapsedTimer.exit(objHTTP.responseText.length + ' chars');
		try {
			return objHTTP.responseText;
		} finally {
			objHTTP = null;
		}
	}
};

//! Public Method emxUICore.hide()
//!      This method hides an HTML element.
emxUICore.hide = function _emxUICore_hide(objElement) {
        objElement.style.visibility = "hidden";
};
emxUICore.removeEventHandler = function _emxUICore_removeEventHandler(objElement, strType, fnHandler, blnCapture) {
        if (typeof objElement != "object") {
                emxUICore.throwError("Required argument objElement is null or not an object.");
        } else if (typeof strType != "string") {
                emxUICore.throwError("Required argument strType is null or not a string.");
        } else if (typeof fnHandler != "function") {
        }
        if (objElement.removeEventListener) {
        	 objElement.removeEventListener(strType, fnHandler, !!blnCapture);
        }
};
//! Public Method emxUICore.show()
//!      This method shows an HTML element.
emxUICore.show = function _emxUICore_show(objElement) {
        objElement.style.visibility = "visible";
};
//! Public Method emxUICore.stopPropagation()
//!      This method stops the propagation of an event.
emxUICore.stopPropagation = function _emxUICore_stopPropagation() {
        this.getEvent().stopPropagation();
};

emxUICore.getNamedForm = function _getNamedForm(oWin, strName){
    if(oWin.document.forms[strName]){
        return oWin.document.forms[strName];
    }
    var formCollection = oWin.document.forms;
    var len = formCollection.length;
    for(var i = 0; i < len; i++){
        if(formCollection[i].name == strName){
            return formCollection[i];
        }
        if(formCollection[i].getAttribute("name") == strName){
            return formCollection[i];
        }
    }
    throw new Error("Can't locate dynamic form named "+strName);
}

function emxUIObject() {
        this.emxClassName = "emxUIObject";
        this.enabled = true;
        this.eventHandlers = new Object;
         //Mode setting on Commands and Menus[Added to support Structure Browser - Edit Mode Toolbar]
		this.Mode  = "";
		this.ViewMode = "";
		this.displayMode="";
		this.expanded="";
		//added for bug : 342600
		this.isRMB = "";
}
emxUIObject.prototype.disable = function _emxUIObject_disable() {
        this.enabled = false;
};
emxUIObject.prototype.enable = function _emxUIObject_enable() {
        this.enabled = true;
};
emxUIObject.prototype.setRMB = function _emxUIObject_setRMB(isrmb) {
        this.isRMB = isrmb;
};
emxUIObject.prototype.getRMB = function _emxUIObject_getRMB() {
        this.isRMB;
};
emxUIObject.prototype.fireEvent = function _emxUIObject_fireEvent(strType, objEvent) {
        if (typeof strType != "string") {
                emxUICore.throwError("Required argument strType is null or not a string.");
        }
        if (this.enabled) {
                if (typeof this.eventHandlers[strType] != "undefined") {
                        for (var i=0; i < this.eventHandlers[strType].length; i++) {
                                this.eventHandlers[strType][0](objEvent);
                        }
                }
                if (typeof this["on" + strType] == "function") {
                        this["on" + strType](objEvent);
                }
        }
}
emxUIObject.prototype.registerEventHandler = function _emxUIObject_registerEventHandler(strType, fnHandler) {
        if (typeof strType != "string") {
                emxUICore.throwError("Required argument strType is null or not a string.");
        } else if (typeof fnHandler != "function") {
                emxUICore.throwError("Required argument fnHandler is null or not a function.");
        }
        if (typeof this.eventHandlers[strType] == "undefined") {
                this.eventHandlers[strType] = new Array;
        }
        this.eventHandlers[strType].push(fnHandler);
}
emxUIObject.prototype.toString = function _emxUIObject_toString() {
        return "[object " + this.emxClassName + "]";
}
emxUIObject.prototype.unregisterEventHandler = function _emxUIObject_unregisterEventHandler(strType, fnHandler) {
        if (typeof strType != "string") {
                emxUICore.throwError("Required argument strType is null or not a string.");
        } else if (typeof fnHandler != "function") {
                emxUICore.throwError("Required argument fnHandler is null or not a function.");
        }
        if (typeof this.eventHandlers[strType] != "undefined") {
                this.eventHandlers[strType].remove(fnHandler);
        }
}

emxUIObject.prototype.clearEventHandlers = function _emxUIObject_clearEventHandlers(strType) {
    if (typeof strType != "string") {
            emxUICore.throwError("Required argument strType is null or not a string.");
    }
    if (typeof this.eventHandlers[strType] != "undefined") {
            this.eventHandlers[strType] = new Array;
    }
}

 //Sets the Mode [Added to support Structure Browser - Edit Mode Toolbar]
emxUIObject.prototype.setMode = function _emxUIObject_setMode(Mode) {
	if(Mode != 'view' && Mode != 'edit'){
		this.Mode="";
	}else{
        this.Mode = Mode;
	}
	if((typeof this.url != "undefined") &&
	   ((this.url.indexOf("editMode()") >= 0) || (this.url.indexOf("viewMode()") >= 0))){
		this.Mode="";
	}
};
//Gets the Mode [Added to support Structure Browser - Edit Mode Toolbar]
emxUIObject.prototype.getMode = function _emxUIObject_getMode() {
        return this.Mode;
};

//set displayMode

emxUIObject.prototype.setDisplayMode = function _emxUIObject_setDispayMode(DisplayMode) {
	
        this.displayMode = DisplayMode;
};
//Gets the DisplayMode [Added to support Structure Browser - DisplayMode Toolbar]
emxUIObject.prototype.getDisplayMode = function _emxUIObject_getDisplayMode() {
        return this.displayMode;
};

//Sets the Mode [Added to support Structure Browser - Display Mode Toolbar]
emxUIObject.prototype.setViewMode = function _emxUIObject_setViewMode(Mode) {
	this.ViewMode = Mode;
};
//Gets the Mode [Added to support Structure Browser - Display Mode Toolbar]
emxUIObject.prototype.getViewMode = function _emxUIObject_getViewMode() {
        return this.ViewMode;
};

//Sets the Expanded setting [Added to support Toolbar Expanded setting]
emxUIObject.prototype.setExpanded = function _emxUIObject_setExpanded(expanded) {
	this.expanded = expanded;
};
//Gets the Mode [Added to support Structure Browser - Display Mode Toolbar]
emxUIObject.prototype.getExpanded = function _emxUIObject_getExpanded() {
        return this.expanded;
};



function emxUIImageLoader() {
        this.superclass = emxUIObject;
        this.superclass();
        delete this.superclass;
        this.emxClassName = "emxUIImageLoader";
        this.images = new Array;
        this.loaded = 0;
}
emxUIImageLoader.prototype = new emxUIObject;
emxUIImageLoader.prototype.addImage = function _emxUIImageLoader_addImage(strImage, strID) {
        if (typeof strImage != "string") {
                emxUICore.throwError("Required argument strImage is null or not a string.");
        }
        var objImg = new Image;
        var objThis = this;
        this.images[this.images.length] = objImg;
        if (strID) {
                this.images[strID] = objImg;
        }
        objImg.onload = function () {
                objThis.handleImageLoad();
        };
        if(isIE){
            window.addEventListener("unload",function(){objImg.onload = null;}, false);
        }
        objImg.onerror = function () {
                emxUICore.throwError("The image " + this.src + "could not be found.");
        };
        objImg._src = strImage;
};
emxUIImageLoader.prototype.begin = function _emxUIImageLoader_begin() {
        for (var i=0; i < this.images.length; i++) {
                this.images[i].src = this.images[i]._src;
        }
};
emxUIImageLoader.prototype.clear = function _emxUIImageLoader_clear() {
        this.images = new Array;
        this.loaded = 0;
};
emxUIImageLoader.prototype.handleImageLoad = function _emxUIImageLoader_handleImageLoad() {
        this.loaded++;
        if (this.loaded == this.images.length) {
                this.fireEvent("load");
        }
};
if (!isIE) {
    Document.prototype.loadXML = function(strXML) {
        var objDOMParser = new DOMParser();
        var objDoc = objDOMParser.parseFromString(strXML, "text/xml");
  while (this.hasChildNodes())
   this.removeChild(this.lastChild);
        for (var i=0; i < objDoc.childNodes.length; i++) {
            var objImportedNode = this.importNode(objDoc.childNodes[i], true);
            this.appendChild(objImportedNode);
        }
    }
    if(!Element.prototype.getElementsByTagName){
    	Element.prototype.__getElementsByTagName__ = Element.prototype.getElementsByTagName;
    	Element.prototype.getElementsByTagName = function (strTagName) {
    		if (strTagName.indexOf("emx:") == 0) {
    			strTagName = strTagName.substring(4, strTagName.length);
    		}
    		return this.__getElementsByTagName__(strTagName);
    	}
    }
    Node.prototype.__defineGetter__("xml", function _Node_xml_getter () {
        var objXMLSerializer = new XMLSerializer;
        var strXML = objXMLSerializer.serializeToString(this);
        return strXML;
    });
}

/*
This function should be used to attach events
It attaches event and also register it to registeredEvents cache
On unload of the page; all the events are detached.
*/
function addEvent(oEventTarget, sEventType, fDest){
    if(oEventTarget.addEventListener){
    	 oEventTarget.addEventListener(sEventType, fDest, true);
    }else if(typeof oEventTarget[sEventType] == "function"){
        var fOld = oEventTarget[sEventType];
        oEventTarget[sEventType] = function(e){ fOld(e); fDest(e); };
    } else {
        oEventTarget[sEventType] = fDest;
    };

    /* Implementing registeredEvents for all event systems */
    registeredEvents.registerEvent(oEventTarget, sEventType, fDest, true);
};


/*
In IE browsers, there are memory leaks when function attached to event handler
is referencing back to HTML DOM elements. When page is unloaded, it is necessary
to detach all the event handlers to release memory.
Below function is used to avoid this memory leak.
The registeredEvents keeps information about all the events that has been
registered for page in scope. It stores following information
1. The HTML control where event is registtered
2. Name of the event like onclick,onmouseup...
3. The function that has been registered

It has got an array and two methods
1. array is used to store the information
2. method add is used to register event
3. method flush is used to unregister all the events in the array
*/
var registeredEvents = function(){
    // array holds information about all the events
    var lEvents = [];

    return {
        lEvents : lEvents,

        // function to register an event
        registerEvent : function(HTMLControl, sEventName, fEventFunction, bCapture){
            return;
        }
    };
}();

// on unload of page detach all the events.
//addEvent(window,"unload",registeredEvents.unregisterAllEvents);
//getTopWindow().UWA.window.addEvent("unload",registeredEvents.unregisterAllEvents);

emxUICore.sendXMLPost = function _emxUICore_sendXMLPost(strURL, strData) {
if(typeof strURL != "string") {
emxUICore.throwError("Required parameter strURL is null or not a string.");
}
var objHTTP = this.createHttpRequest();
objHTTP.open("post", strURL, false);
objHTTP.setRequestHeader("Content-type","text/xml");
objHTTP.send(strData);
	emxUICore.checkResponse(objHTTP);

return objHTTP.responseText;
};

emxUICore.getAsyncXMLData = function _emxUICore_getAsyncXMLData(oRequest, fnCallback, levelId, strURL, strData) {
    oRequest.open("post", strURL, true);
    oRequest.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    oRequest.setRequestHeader("charset", "UTF-8");
    oRequest.onreadystatechange = function() {
            fnCallback(levelId);
    }
    objHTTP.send(strData);
}

function verifyQueryLimit() {
    var footerFrame = findFrame(getTopWindow(), "searchFoot");
    if (footerFrame) {
        var sQueryLimit = footerFrame.document.getElementById("QueryLimit").value;
        if(isNaN(sQueryLimit) || sQueryLimit.length == 0){
            alert(footerFrame.QueryLimitNumberFormat);
            return false;
        }

        //does QLimit contain a decimal?
        if(sQueryLimit.indexOf(".") != -1) {
            alert(footerFrame.QueryLimitNumberFormat);
            return false;
        }

        //QLimit must be at least 1
        sQueryLimit = parseInt(sQueryLimit, 10);
        if(sQueryLimit < 1){
            alert(footerFrame.QueryLimitNumberFormat);
            return false;
        }
        //QLimit must not be greater than 32767
        if(sQueryLimit > footerFrame.upperQueryLimit) {
            alert(footerFrame.QueryLimitMaxAllowed);
            return false;
        }
    }
	return true;
}

emxUICore.submitLongURL = function _emxUICore_submitLongURL(strURL,objForm,strTarget) {
	if(typeof strURL != "string") {
		emxUICore.throwError("Required parameter strURL is null or not a string.");
	}

	var targetIFRAME = findFrame(getTopWindow(),strTarget);
	if(targetIFRAME == null){
		targetIFRAME = findFrame(getTopWindow(),"listHidden");
	}

	if(targetIFRAME == null){
		emxUICore.throwError("Cannot find a target IFRAME");
	}

	if(isIE && strURL.length > 2048){
		var items = new Array();
		var actionURL = strURL.replace(/[?].*/, "");
		var search = strURL.substr(actionURL.length + 1);
		if (search == strURL) {
			search = "";
		}
		var params = search.split("&");
		for (var i = 0; i < params.length; i++) {
			var param = params[i];
			if (param == '')
				continue;
			var name = decodeURIComponent(param.replace(/=.*/, ""));
			var value = decodeURIComponent(param.substr(name.length + 1));
			items.push({
				name:name,
				value:value
				});
		}
		var tmpForm = document.getElementById("emxPostSubmitForm");

		if(tmpForm != null){
	   		objForm = tmpForm;
		}

		if(objForm == null){
			objForm    = document.createElement('form');
			objForm.name   = "emxPostSubmitForm";
			objForm.id     = "emxPostSubmitForm";
			document.body.appendChild(objForm);
		}

		//clear old form elements
		for(var ix=0; ix < items.length; ix++)
		{
			for(var id=0; id < objForm.elements.length; id++)
			{
				if(items[ix].name == objForm.elements[id].name){
			  		objForm.removeChild(objForm.elements[id]);
			  		id--;
				}
			}
		}

		//adding hidden elements
		for(var index=0; index < items.length; index++)
		{
			var input   = document.createElement('input');
			input.type  = "hidden";
			input.name  = items[index].name;
			input.value = items[index].value;
			objForm.appendChild(input);
		}

		//submitting the form
		objForm.action = actionURL;
		objForm.method = "post";
		objForm.target = targetIFRAME.name;
		objForm.submit();
	}else{
		targetIFRAME.frameElement.src = strURL;
	}
}


var XMLHelper =
{
	GetXMLDocument : function() {
		if (isIE) {
			var Activex_Doms = [
				"Msxml2.DOMDocument.5.0",
				"Msxml2.DOMDocument.4.0",
				"Msxml2.DOMDocument.3.0",
				"MSXML2.DOMDocument",
				"MSXML.DOMDocument",
				"Microsoft.XMLDOM"
			];
			var Activex_Dom = null;
			for(var i = 0 ; i < Activex_Doms.length ; i++) {
				try{
					var success = new ActiveXObject(Activex_Doms[i]);
					if(success){
						Activex_Dom = Activex_Doms[i];
						break;
						//return testObj;
					}
				}catch(e){}
			}
			return function() {
				return new ActiveXObject(Activex_Dom);
			}
		}
	}()
	,
	GetXSLDocument : function() {
		if (isIE) {
			var Activex_FT_Doms = [
				"Msxml2.FreeThreadedDOMDocument.5.0",
				"MSXML2.FreeThreadedDOMDocument.4.0",
				"MSXML2.FreeThreadedDOMDocument.3.0"
			];
			var Activex_FT_Dom = null;
			for(var i = 0 ; i < Activex_FT_Doms.length ; i++) {
				try {
					var success = new ActiveXObject(Activex_FT_Doms[i]);
					if(success) {
					  Activex_FT_Dom = Activex_FT_Doms[i];
					  break;
					}
				} catch(e) { }
			}
			return function() {
				return new ActiveXObject(Activex_FT_Dom);
			}
		}
	}()
	,
	GetXSLTemplate : function() {
		if (isIE) {
			var Activex_Templates = [
				"Msxml2.XSLTemplate.5.0",
				"Msxml2.XSLTemplate.4.0",
				"MSXML2.XSLTemplate.3.0"
			];
			var Activex_Template = null;
			for(var i = 0 ; i < Activex_Templates.length ; i++) {
				try {
					var success = new ActiveXObject(Activex_Templates[i]);
					if(success) {
					  Activex_Template = Activex_Templates[i];
					  break;
					}
				} catch(e) { }
			}
			return function() {
				return new ActiveXObject(Activex_Template);
			}
		}
	}()
	,
	CreateXMLDocument : function _CreateXMLDocument(namespace) {
		if(isIE) {
			var doc = this.GetXMLDocument();
			doc.setProperty("SelectionLanguage", "XPath");
			if (namespace != null) {
			  doc.setProperty("SelectionNamespaces", namespace);
			} else {
			  doc.setProperty("SelectionNamespaces", "xmlns:aef='http://www.matrixone.com/aef'");
			}
			return doc;
		} else {
			return document.implementation.createDocument("", "", null);
		}
	}
};

emxUICore.GetXSLDom = function _GetXSLDom(xsl)
{
	var dom   = XMLHelper.GetXSLDocument();
	dom.async = false;
	dom.loadXML(xsl);
	this.checkDOMError(dom);
    return dom;
}

emxUICore.GetXMLDom = function _GetXMLDom(xml)
{
	var dom = XMLHelper.CreateXMLDocument();
    dom.loadXML(xml);
    this.checkDOMError(dom);
    return dom;
}

emxUICore.GetXSLRemote = function _GetXSL(xsl)
{
	var resp = this.getData(xsl);
	return isIE ? emxUICore.GetXSLDom(resp) : emxUICore.GetXMLDom(resp);
}

emxUICore.GetXMLRemote = function _GetXMLRemote(callback,url,postdata,custom)
{
	var http = this.createHttpRequest();
	http.open("post", url, true);
	http.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	http.setRequestHeader("charset", "UTF-8");
	http.onreadystatechange = function() {
		if(http.readyState == 4)  {
	       callback(emxUICore.GetXMLDom(http.responseText),custom);
	    }
	}
    http.send(postdata);
}

emxUICore.GetJsonRemote = function _GetJsonRemote(url, postdata, callback, custom)
{
	var http = this.createHttpRequest();
	http.open("post", url, false);
	http.setRequestHeader("Content-type", "text/plain");
	http.setRequestHeader("charset", "UTF-8");

	var getObject = function(text){
		try
		{
			text = text.replace(/^[\r\n]+/g, "").replace(/[\r\n]+$/g, "");
			return emxUICore.parseJSON(text);
		}catch(e)
		{
			return text;
		}
	}

	if(callback){
		http.onreadystatechange = function() {
			if(http.readyState == 4)  {
				callback(getObject(http.responseText), custom);
			}
		}
	}
	var text = this.toJSONString(postdata);
    http.send(text);
    return !callback ? getObject(http.responseText) : null;
}


emxUICore.EncodeUrlParam = function _EncodeUrlParam(param)
{
    if (param == null || param.length <= 0)
    {
        return param;
    }

    var encodedParam = [];
    for (var i = 0; i < param.length; i++)
    {
        encodedParam.push(param.charCodeAt(i));
        if (i < (param.length - 1))
        {
            encodedParam.push(".");
        }
    }

    return encodedParam.join("");
}

emxUICore.DecodeUrlParam = function _DecodeUrlParam(param)
{
    if (param == null || param.length <= 0)
    {
        return param;
    }

    var decodedParam = [];
    param = param.split(".");
    for (var i = 0; i < param.length; i++)
    {
        decodedParam.push(String.fromCharCode(param[i]));
    }

    return decodedParam.join("");
}
/**
 * This function is used by the command tip page ,
 * which opens the command details in a maximized window in a new pop up
 */
function openTipWindow(strURL){
	showAndGetNonModalDialog(strURL,'Max', 'Max', true);
}

emxUICore.arrayToJSONString = function(array, keylist){

	 var txtArray = [], count, len = array.length, val;
     for (count = 0; count < len; count += 1) {
         val = array[count];
         txtArray.push(this.toJSONString(val, keylist));
     }
     return '[' + txtArray.join(',') + ']';
}

emxUICore.booleanToJSONString = function(bool){
	 return String(bool);
}

emxUICore.numberToJSONString = function(number){
	return isFinite(number) ? String(number) : 'null';
}

emxUICore.stringToJSONString = function(string){

	var replaceList = {
            '\b': '\\b',
            '\t': '\\t',
            '\n': '\\n',
            '\f': '\\f',
            '\r': '\\r',
            '"' : '\\"',
            '\\': '\\\\'
        };

	 if (/["\\\x00-\x1f]/.test(string)) {
         return '"' + string.replace(/[\x00-\x1f\\"]/g, function (a) {
             var cCode = replaceList[a];
             if (cCode) {
                 return cCode;
             }
             cCode = txtArray.charCodeAt();
             return '\\u00' + Math.floor(cCode / 16).toString(16) + (cCode % 16).toString(16);
         }) + '"';
     }
     return '"' + string + '"';
}

emxUICore.dateToJSONString = function(date){

	  function func(num) {
          return num < 10 ? '0' + num : num;
      }
      return '"' + date.getUTCFullYear() + '-' +
              func(date.getUTCMonth() + 1)  + '-' +
              func(date.getUTCDate())       + 'T' +
              func(date.getUTCHours())      + ':' +
              func(date.getUTCMinutes())    + ':' +
              func(date.getUTCSeconds())    + 'Z"';
}

emxUICore.objectToJSONString = function(object, keylist){
	var txtArray = [], key, count, value;
	var target = keylist ? keylist : object;
	for (key in target) {
        if (typeof key === 'string' &&
                Object.prototype.hasOwnProperty.apply(object, [key])) {
            value = object[key];
            txtArray.push(this.stringToJSONString(key) + ':' + this.toJSONString(value, keylist));
        }
    }
	 return '{' + txtArray.join(',') + '}';
}

emxUICore.toJSONString = function(json, keylist) {

	var value;
	if (json && typeof json.toJSONString === 'function') {
		value = json.toJSONString(keylist);
	}else {
		switch (typeof json) {
		case 'object':
			if(json instanceof Array)
				value = this.arrayToJSONString(json, keylist);
			else
				value = this.objectToJSONString(json, keylist);
			break;
		case 'string':
			value = this.stringToJSONString(json);
			break;
		case 'number':
			value = this.numberToJSONString(json);
			break;
		case 'boolean':
			value = this.booleanToJSONString(json);
			break;
		}
	}
	return value;
}

emxUICore.parseJSON = function(json, func) {

	var replaceList = {
            '\b': '\\b',
            '\t': '\\t',
            '\n': '\\n',
            '\f': '\\f',
            '\r': '\\r',
            '"' : '\\"',
            '\\': '\\\\'
        };

	var tmp;
    function walk(key, val) {
        var itr;
        if (val && typeof val === 'object') {
            for (itr in val) {
                if (Object.prototype.hasOwnProperty.apply(val, [itr])) {
                    val[itr] = walk(itr, val[itr]);
                }
            }
        }
        return func(key, val);
    }
    if (/^[,:{}\[\]0-9.\-+Eaeflnr-u \n\r\t]*$/.test(json.
            replace(/\\./g, '@').
            replace(/"[^"\\\n\r]*"/g, ''))) {
        tmp = eval('(' + json + ')');
        return typeof func === 'function' ? walk('', tmp) : tmp;
    }
    throw new SyntaxError('parseJSON');
}

var HashMap = function () {
	this._container = {};
}

HashMap.prototype =  {

	Get : function (key) {
		return this.Contains(key) ? this._container[key] : null;
	}
	,
	Put : function(key,value) {
		this._container[key] = value;
	}
	,
	Contains : function(key) {
		return key in this._container;
	}
	,
	Remove : function(key) {
		return this.Contains(key) ? delete this._container[key] : false;
	}
	,
	ToString : function(sep) {
		var tostring = [];
		sep = sep || ",";
		for(var key in this._container) {
			tostring.push("[ " + key + " = " + this._container[key] + " ]");
		}
		return tostring.join(sep);
	}
	,
	IsValid : function(key) {
		return typeof this.Get(key) != "function";
	}
	,
	ToArray : function() {
		var arry = [];

		for(var key in this._container) {
			if(this.IsValid(key)) {
				arry.push(this.Get(key));
			}
		}
		return arry;
	}
}

function callCheckout(objectId, action, fileName, format, refresh, closeWindow, appName, appDir, partId, version,SB,customSortColumns, customSortDirections,uiType, table,getCheckoutMapFromSession ,fromDataSessionKey,parentOID, appProcessPage,versionId)
{
	// ---------Start MSF Changes
	var requireObj;
	try{
		if(require) {
			requireObj = require;
		}
	}
	catch(error){
		if(top.require) {
			requireObj = top.require;
		} else {
			requireObj = top.opener ? top.opener.require : undefined;
		}
	}	
	var Checkout = function() {
		var newForm = getTopWindow().document.getElementById("commonDocumentCheckout");
		if( newForm == null)
		{
			newForm = getTopWindow().getWindowOpener().getTopWindow().document.getElementById("commonDocumentCheckout");
		}
		var portalMode = false;
		var currentFrame = "";
		if(this != getTopWindow()){
			if(this.parent.location.href.indexOf("portalMode=true")>-1){
				portalMode = true;
			}
			currentFrame = this.parent.name;
		}

		newForm.objectId.value = objectId;
		newForm.action.value = action;
		newForm.fileName.value = fileName;
		newForm.format.value = format;
		newForm.refresh.value = refresh;
		newForm.closeWindow.value = closeWindow;
		newForm.appDir.value = appDir;
		newForm.appName.value = appName;
		newForm.trackUsagePartId.value = partId;
		newForm.version.value = version;
		newForm.versionId.value = versionId;
		newForm.customSortColumns.value = customSortColumns;
		newForm.customSortDirections.value = customSortDirections;
		newForm.uiType.value = uiType;
		newForm.table.value = table;
		newForm.getCheckoutMapFromSession.value = getCheckoutMapFromSession;
		newForm.fromDataSessionKey.value = fromDataSessionKey;
		newForm.parentOID.value = parentOID;
		newForm.frameName.value = currentFrame;
		newForm.portalMode.value = portalMode;
		try{
			newForm.action = "../components/emxCommonDocumentPreCheckout.jsp";
		}catch(e){
			newForm.setAttribute("action","../components/emxCommonDocumentPreCheckout.jsp");
		}

		newForm.appProcessPage.value = appProcessPage;


		var intWidth = "730";
		var intHeight = "450";
		var strFeatures = "width=" + intWidth + ",height=" + intHeight;
		var intLeft = parseInt((screen.width - intWidth) / 2);
		var intTop = parseInt((screen.height - intHeight) / 2);
		if (isIE_M) {
			strFeatures += ",left=" + intLeft + ",top=" + intTop;
		} else{
			strFeatures += ",screenX=" + intLeft + ",screenY=" + intTop;
		}
		if (isNS4_M) {
			strFeatures += ",resizable=no";
		} else {
			strFeatures += ",resizable=yes";
		}
		// Code added for the Bug 295197 Dt 8/11/2005
		var today=new Date();
		var suffix=today.getTime();
		var strFeatures = "";
		var winName = "_self";
		var win = "";

		if(SB == null && "view" == action)
		{
			//modified for Bug no : 317605. Dt 03/04/2006. Added scrollbars=yes
			strFeatures = "width=730,height=450,dependent=yes,resizable=yes,toolbar=yes,scrollbars=yes";
			winName="CheckoutWin"+suffix;
			// Till Here
			win = window.open('', winName, strFeatures);
			registerChildWindows(win, getTopWindow());
		}
		if("view" == action)
		{
			newForm.target =winName;
		} else {
			var appletFrameElem = getTopWindow().document.getElementById("appletFrame");
			if(appletFrameElem){
				appletFrameElem.style.width = "1px";
				appletFrameElem.style.height = "1px";
				appletFrameElem.style.visibility = "visible";
				newForm.target ="appletFrame";
			}else{
				newForm.target ="hiddenFrame";
			}
		}
		newForm.submit();
	}	
	requireObj(["DS/MSFDocumentManagement/MSFDocumentClient"], function(MSFDocumentClient) {
		
		if(MSFDocumentClient.isConnectedWithMSF() === "true") {
			MSFDocumentClient.MSFCallCheckout(action, objectId, partId, uiType, parentOID, versionId, fileName, format);
			return;
		}
		Checkout.call();
	}, function() {
		Checkout.call();
	});
	// ---------End MSF Changes
}

emxUICore.trimWhiteSpace = function(text) {
	return (text || "").replace( /^(\s|\u00A0)+|(\s|\u00A0)+$/g , "" );
}

emxUICore.trim = function (str)
{
    if(!str || typeof str != 'string')
        return null;

    return str.replace(/^[\s]+/,'').replace(/[\s]+$/,'').replace(/[\s]{2,}/,' ');
}

function getLocaleSeperators(){
	var nfLocalesLikeUS = [ 'ae','au','ca','cn','eg','gb','hk','il','in','jp','sk','th','tw','us', 'en-US', 'zh-CN', 'ja' ];
	var nfLocalesLikeDE = [ 'at','br','de','dk','es','gr','it','nl','pt','tr','vn' ];
	var nfLocalesLikeFR = [ 'cz','fi','fr','ru','se','pl' ];
	var nfLocalesLikeCH = [ 'ch' ];

	var strLan = emxUIConstants.BROWSER_LANGUAGE;
	if(nfLocalesLikeDE.find(strLan) != -1){
		return [",", "."];
	}else if(nfLocalesLikeFR.find(strLan) != -1){
		return [",", " "];
	}else if(nfLocalesLikeCH.find(strLan) != -1){
		return [".", "'"];
	}else{ //default locales like US
		return [".", ","];
	}
}

function getFormatExpression(strNumber){
	var browserLang =  emxUIConstants.BROWSER_LANGUAGE;
	var realExp = "";
	var y = browserLang == "zh-CN" ? 5 : 4;
	for(var i=1; i<=strNumber.length; i++){
		if(i == y ){
			realExp = "," + realExp;
			y = browserLang == "zh-CN" ? (y + 4) : (y + 3);
		}
		realExp = "#" + realExp;
	}
	return realExp;
}

function getFormattedNumber(strNumber){
	var browserLang = emxUIConstants.BROWSER_LANGUAGE;
	if(strNumber && strNumber != null && strNumber.length > 0){
		var decIndex = strNumber.indexOf(emxUIConstants.DECIMAL_QTY_SETTING);
		var formattedNumber = "";
		if(decIndex != -1){
			var integerPart = strNumber.substring(0,decIndex);
			var realPart = strNumber.substring(decIndex+1,strNumber.length);
			var localeSeperators = getLocaleSeperators();
			formattedNumber = jQuery.formatNumber(integerPart,{format:getFormatExpression(integerPart) ,locale:browserLang});
			formattedNumber = formattedNumber.length > 0 ? formattedNumber : "0";
			formattedNumber = formattedNumber + localeSeperators[0] + realPart;
		}else{
			formattedNumber = jQuery.formatNumber(strNumber,{format:getFormatExpression(strNumber) ,locale:browserLang});
			formattedNumber = formattedNumber.length > 0 ? formattedNumber : "0";
		}
		return formattedNumber;
	}else{
		return "";
	}
}

function getNumericCellValue(columnCell){
	var isEdited = "true" == columnCell.getAttribute("edited");
	return isEdited ? columnCell.getAttribute("newA") : columnCell.getAttribute("a");
}

/* V6R2014 - To support new Menu's for teh Compass apps Holder
 * We need to pass a command to this method;it wil return the top level menu for that*/



function strReplaceAll(str, from, to){
	if(from == to){
		return str;
	}
	if (str != null && str != "") {
		var ind = str.indexOf(from);
		while (ind > -1) {
			str = str.replace(from, to);
			ind = str.indexOf(from);
		}
	}
	return str;
}

function getHighLevelMenu(cmdName){
	cmdName = "Id_"+strReplaceAll(cmdName," ","_");
	var cmd = jQuery('#'+cmdName);
	var retnDiv = cmd.closest('.menu-panel');
	return retnDiv;
}

function setProductLabel(brandName, appName){
	var expLabel = "<strong>3D</strong>EXPERIENCE";
	var ablh = getTopWindow().jQuery('li[id="AEFBrandLogoHolder"]');
	ablh.find('span').html(expLabel);
	ablh.attr('title', '');
	var tempbrdlabel = ' | ' + brandName + ' ';
	var jqapplabel = getTopWindow().jQuery('li[id="AEFAppLabelHolder"]');
	jqapplabel.data("appName",tempbrdlabel + appName);
	jqapplabel.data("brandName", brandName);
	changeProductOnTopFrame(expLabel, tempbrdlabel, appName);
	var adjustedName = adjustAppName(tempbrdlabel + appName);
	setAdjustedAppLabel(brandName, adjustedName);
}

function changeProductOnTopFrame(expLabel, tempbrdlabel, appName){
	var ablh = getTopWindow().jQuery('.topbar-app');
	ablh.find('span[class="topbar-app-i3dx"]').html(expLabel);
	var topFrameAppBrand = getTopWindow().jQuery('.topbar-app-title');
	ablh.find('span[class="topbar-app-brand"]').html(tempbrdlabel);
	topFrameAppBrand.find('span[class="topbar-app-name"]').html(appName);
}

function setAdjustedAppLabel(brandName, adjustedName){
	var tempbrdlabel = ' | ' + brandName + ' ';
	if(tempbrdlabel.length < adjustedName.length){
		adjustedName = adjustedName.replace(brandName, "<strong>" + brandName + "</strong>");
	} else {
		adjustedName = ' | <strong>' + adjustedName.substring(3) + '</strong>';
	}
	var aalholder = getTopWindow().jQuery('li[id="AEFAppLabelHolder"]')
	aalholder.find('span').html(adjustedName);
	aalholder.attr('title', '');
}
function adjustAppName(appName){
	if(appName){
		var charwidth = 7.7;
		var clogoelem = getTopWindow().jQuery('li[id="AEFCompanyLogo"]');
		var totwidth = clogoelem.innerWidth() + getTopWindow().jQuery('li[id="AEFBrandLogoHolder"]').innerWidth() + getTopWindow().jQuery('li[id="compassHoldout"]').innerWidth();

		var winLeftwidth = (jQuery('#pageHeadDiv')[0]).offsetWidth;
		winLeftwidth = winLeftwidth * 0.35;

		var remwidth = winLeftwidth - totwidth;
		var maxcharsfit = remwidth/charwidth;
		maxcharsfit = Math.round(maxcharsfit);
		if(maxcharsfit < appName.length)
		{
			appName = appName.substr(0,maxcharsfit-2);
			appName += "..";
		}
		return appName;
	} else {
		return '';
	}
}

function launchAppMenu(){
	var menuDiv = jQuery('.my-desk');

	if(menuDiv.is(":visible")){
		menuDiv.hide();
	}else{
		menuDiv.css('display','block');
		menuDiv.css('left','20px');
		menuDiv.css('top',(jQuery('#pageHeadDiv').height() - jQuery('div#tabBar').height()));

		emxUICore.iterateFrames(function (objFrame) {
  	    	if(objFrame){
  	    		jQuery(objFrame).bind("mousedown",function(e){
  	    			var target= e.target;
  	    			if(target && menuDiv.has(target).length == 0 && target.className != 'btn app-menu'){
  	    				menuDiv.hide();
  	    				jQuery(objFrame).unbind("mousedown");
  	    			}
  	    		});
  	    	}
  	    });

		if(menuDiv[0].clientHeight > (emxUICore.getWinHeight() * 0.75)){
			menuDiv[0].style.height = (emxUICore.getWinHeight() * 0.75)+"px";
		}
	}
}

function launchEnoviaApp(appUrl){
	getTopWindow().bclist.clear();
	var appName = appUrl.substring(appUrl.indexOf('?'));
	var contextFrame = getTopWindow().findFrame(getTopWindow(),"content");
	contextFrame.location.href = "emxNavigatorContentLoad.jsp" + appName;
}
function disableMDGroups(){
	var mbbtn = jQuery('#menuButton');
	if(mbbtn.length != 0){
		mbbtn[0].style.display = "none";
	}
	var grp = jQuery('.my-desk').find('.group');
	for(var k=0;k < grp.length; k++){
		grp[k].style.display = "none";
	}
}
function toggleappGroups(appGroup){
	var selector = 'div[id="'+appGroup+'"]';
	var appGrpDiv = jQuery(selector);
	if(appGrpDiv.length != 0){
		appGrpDiv[0].style.display = "block";
	}
}

function changeProduct(brandName, appName, appTrigram, appMenuList, licensedAppList, isFromIFWE){
	if(getTopWindow().curProd == "" || licensedAppList.indexOf(getTopWindow().curProd) < 0  && !getTopWindow().isfromIFWE){
		getTopWindow().curProd = appTrigram;
		if(!isFromIFWE){
		getTopWindow().setProductLabel(brandName, appName);
		}
		getTopWindow().disableMDGroups();
		var mbbtn = jQuery('#menuButton');
		if(appMenuList.length != 0 && mbbtn){
			if(mbbtn[0]){
			mbbtn[0].style.display = "block";
		}
		}

		for(var i=0;i<appMenuList.length;i++){
			getTopWindow().toggleappGroups(appMenuList[i]);
		}
	}
}

function showMyDesk()
{
    // My Desk Changes START
    if(jQuery("#mydeskpanel").hasClass("appMenu")) {
        jQuery("#panelObjectHistory").remove();  
        jQuery("#content").css("width", "");  
        jQuery("#ExtpageHeadDiv").hide();
        jQuery("#resizerLeftPanelMenu").hide();
        jQuery("#panelToggle").css("top", "80px");

        jQuery("#mydeskpanel").css('left', jQuery("#leftPanelMenu").css("left"));
        jQuery("#mydeskpanel").css('top','80px');                                                                            
        jQuery("#mydeskpanel").css('max-height','');                                                                            
        jQuery("#mydeskpanel").css('display','block');                                                                                                                                                    
        jQuery("#mydeskpanel").unbind("mouseenter");
        jQuery("#mydeskpanel").unbind("mouseleave");                                                                           
        jQuery("#mydeskpanel").removeClass("appMenu");
    }
    
    jQuery("#pageContentDiv").css("top", "80px");
    if(jQuery("#panelToggle").hasClass("closed")) {
    	jQuery("#panelToggle").removeClass("closed");
		jQuery("#panelToggle").addClass("open");
		isPanelOpen = true;
        jQuery("#pageContentDiv").css("left", "20px");
    } else {
        jQuery("#pageContentDiv").css("left", "212px");
    }
    
    // My Desk Changes END
	jQuery("div#leftPanelMenu").css('display',"none");
	jQuery("div#mydeskpanel").css('display',"block");
	
	jQuery("div#mx_divGrabber").css('display',"none");
	jQuery("div#mydeskpanel").css('left', '16px');
	jQuery("div#pageContentDiv").css('left', jQuery("div#mydeskpanel").width() + jQuery("div#panelToggle").width() +'px');	
	
	var divph = getTopWindow().document.getElementById("ExtpageHeadDiv");
	divph.style.display='none';
	if(getTopWindow().isfromIFWE){
		getTopWindow().toppos = 0;
	} else {
		if(emxUIConstants.TOPFRAME_ENABLED){
			getTopWindow().toppos = jQuery('#topbar').height() + jQuery('#navBar').height();
		}else{
			getTopWindow().toppos = jQuery('#pageHeadDiv').height();
		}
	}
	jQuery("div#pageContentDiv").css('top',getTopWindow().toppos);
	jQuery("div#mydeskpanel").css('top',getTopWindow().toppos);
	jQuery("div#leftPanelMenu").css('top',getTopWindow().toppos);
	jQuery("div#panelToggle").css('top',getTopWindow().toppos);
	jQuery("div#panelToggle").attr('title',emxUIConstants.STR_HIDE);
	jQuery("div#GlobalMenuSlideIn").css('top',getTopWindow().toppos);
	jQuery("div#rightSlideIn").css('top',getTopWindow().toppos);
	jQuery("div#leftSlideIn").css('top',getTopWindow().toppos);
}

function showTreePanel(){
	jQuery("div#catMenu").hide();
	jQuery("div#leftPanelTree").show();
	jQuery("div#togglecat").show();
	//toggleTree();
 }

 function hideTreePanel(){
	jQuery("div#catMenu").show();
	jQuery("div#leftPanelTree").hide();
	jQuery("div#togglecat").hide();
	//toggleTree();
}

function setCollabSpace(collabSpace){
    var frameContent = getTopWindow().findFrame(getTopWindow(),"content");
    var objForm = getTopWindow().document.createElement("form");

    var cturl = document.createElement("input");
    cturl.type="hidden";cturl.id = "ContentURL";cturl.name="ContentURL";
    cturl.value = frameContent.document.location.href;
    objForm.appendChild(cturl);
    var vCollabSpace = document.createElement("input");
    vCollabSpace.type="hidden";vCollabSpace.id = "CollabSpace";vCollabSpace.name="CollabSpace" ;
    vCollabSpace.value = collabSpace;
    objForm.appendChild(vCollabSpace);
    objForm.action = "emxSecurityContextSelectionProcess.jsp";
    getTopWindow().document.body.appendChild(objForm);
    addSecureToken(objForm);
    objForm.submit();
    removeSecureToken(objForm);
}

function toggleTagger(){
	if(!isWindowShadeOpened()){
		if(emxUISlideIn.is_closed || showSlideInDialog.mode != "tag navigator"){
			if(getTopWindow().sb || getTopWindow().hasWidget){
				if(ds.isCompassOpen){
					ds.isCompassOpen = false;
					getTopWindow().jQuery("div#pageContentDiv").animate({'left' : '0px'}, 200);
					getTopWindow().jQuery('#appsPanel')[0].style.display = "none";
					ds.Compass.hideAccordion();
				}
				showSlideInDialog.mode = "tag navigator";
				showSlideInDialog("bpsTagNavLoader.html", null, null, "left");

				// set time out to be sure that animate has started brfore calling TagNavigator code
                setTimeout(function () {
                    require(["DS/TagNavigator/TagNavigator"], function (TagNavigator) {TagNavigator.toggle6WTaggerViewVisibility(true)});
                }, 10);
			}
		} else {
			closeSlideInDialog();
			 if(!isPanelOpen){
				 getTopWindow().jQuery("div#pageContentDiv").animate({'left' : '20px'}, 200);
			}
			showSlideInDialog.mode = "";
			require(["DS/TagNavigator/TagNavigator"], function (TagNavigator) {TagNavigator.toggle6WTaggerViewVisibility(false)});
		}
	}
}

function getPhyscicalId(objectId){
	var physcicalid = JSON.parse(emxUICore.getData('../resources/bps/physicalids?oid_list=' + objectId));
	return physcicalid[0];
}

function getPhyscicalIds(objectId){
	var physcicalid = JSON.parse(emxUICore.getData('../resources/bps/physicalids?oid_list=' + objectId));
	return physcicalid;
}

function getContextObjDetails() {
	var ctxId = emxUICore.getContextId(false);
	var objDetails ="";
	if(ctxId && ctxId != '' &&  typeof(ctxId) != "undefined") {
		var objDetails = JSON.parse(emxUICore.getData('../resources/bps/objDetails?oid_list=' + ctxId));
		
	}
	return objDetails;
}
 
function getContextObjDetailsFromPortal(){
	var ctxId = "";
	var contextFrame = getTopWindow().findFrame(getTopWindow(),"portalDisplay");
	if(contextFrame){
		ctxId = emxUICore.getContextIDFromFrameURL(contextFrame);
	}
	var objDetails ="";
	if(ctxId && ctxId != '' &&  typeof(ctxId) != "undefined") {
		objDetails = JSON.parse(emxUICore.getData('../resources/bps/objDetails?oid_list=' + ctxId));
		
	}
	return objDetails;
};

emxUICore.setIdtoCompass = function _setIdtoCompass(){
	if(typeof SnN != "undefined" && SnN.is3DSearchActive) {
		return;
	}
	
	if(getTopWindow().ds.resetObj != false) {
	    var objMap = getContextObjDetails();
		var ctxId = objMap.physId;//emxUICore.getContextId(true);
		var displayName = "";
		
		if(objMap.Title && objMap.Title != '' &&  typeof(objMap.Title) != "undefined") {
			displayName = objMap.Title;
		} else if(objMap.V_Name && objMap.V_Name != '' &&  typeof(objMap.V_Name) != "undefined") {
			displayName = objMap.V_Name;
		} else {
			displayName = objMap.name;
		}
		if(ctxId && ctxId != '' &&  ctxId != 'undfined'){
		    var envId = getTopWindow().curTenant;
			if(envId == '' ||  typeof(envId) == "undefined") {
				envId = "OnPremise";
			}
			var obj = {
				objectType: 'mime/type',
				objectId: ctxId,
				envId: getTopWindow().curTenant,
				contextId: getTopWindow().curSecCtx
			};
			// Set the object
			getTopWindow().ds.Compass.setObject(obj);
			
			 var x3dCont = {
                protocol: "3DXContent",
                version: "1.0",
                source: getTopWindow().currentApp,
                data: {
                  items: [{
                      envId: envId,
                      serviceId: "3DSpace",
                      contextId: getTopWindow().curSecCtx,
                      objectId: ctxId,
                      objectType: objMap.type,
                      displayName : displayName
                }]
             } };
			
			getTopWindow().ds.Compass.setX3DContent(x3dCont);
		} else {
			getTopWindow().ds.Compass.resetObject();
			getTopWindow().ds.Compass.resetX3DContent();
		}
	} else {
		getTopWindow().ds.resetObj = true;
	}
};

emxUICore.getContextId = function _getContextId(isPhyscicalId){
	var wsdiv = jQuery("div#windowshade");
	var contextID = "";
	if((typeof wsdiv[0] != "undefined" &&  wsdiv[0].style.display == 'block')){
		var searchsbframe = getTopWindow().findFrame(getTopWindow(),"windowShadeFrame");
		if(searchsbframe && searchsbframe.ids && searchsbframe.ids.split('~').length - 2 >= 1){
			contextID = searchsbframe.ids.split('~')[searchsbframe.ids.split('~').length - 2];
			if(contextID.indexOf('|') >= 0){
				contextID = contextID.split('|')[1];
			}
		}
	} else {
	var contextFrame = getTopWindow().findFrame(getTopWindow(),"content");
	if(contextFrame){
			var ddFrame = getTopWindow().findFrame(getTopWindow(),"detailsDisplay");
			var portalFrame = getTopWindow().findFrame(getTopWindow(),"portalDisplay");
			if(ddFrame){
				portalFrame = getTopWindow().findFrame(ddFrame,"portalDisplay");
			}

			if(portalFrame && portalFrame.ids && portalFrame.ids.split('~').length - 2 >= 1){
				contextID = portalFrame.ids.split('~')[portalFrame.ids.split('~').length - 2];
				if(contextID.indexOf('|') >= 0){
					contextID = contextID.split('|')[1];
				}
			} else if(ddFrame && ddFrame.ids && ddFrame.ids.split('~').length - 2 >= 1){
				contextID = ddFrame.ids.split('~')[ddFrame.ids.split('~').length - 2];
				if(contextID.indexOf('|') >= 0){
					contextID = contextID.split('|')[1];
				}
			} else {
				contextID = emxUICore.getContextIDFromFrameURL(contextFrame, ddFrame);
			}
		}
	}

		if(contextID != "" && isPhyscicalId)
		{
		    contextID = contextID.trim();
			contextID = getPhyscicalId(contextID);
		}
		return contextID;
};

emxUICore.getContextIDFromFrameURL = function _getContextIDFromFrameURL(contextFrame, ddFrame){
	var contextID = "";
			var locHref = contextFrame.location.href;
			var idStartIndex = locHref.indexOf('&objectId=');
					if(ddFrame && idStartIndex < 0){
						locHref = ddFrame.location.href;
						idStartIndex = locHref.indexOf('&objectId=');
					}

				if(contextFrame.ids && contextFrame.ids.split('~').length - 2 >= 1){
					contextID = contextFrame.ids.split('~')[contextFrame.ids.split('~').length - 2];
					if(contextID.indexOf('|') >= 0){
						contextID = contextID.split('|')[1];
					}
				}else if(contextFrame.parent.ids && contextFrame.parent.ids.split('~').length - 2 >= 1){
					contextID = contextFrame.parent.ids.split('~')[contextFrame.parent.ids.split('~').length - 2];
					if(contextID.indexOf('|') >= 0){
						contextID = contextID.split('|')[1];
					}
				}else if(contextFrame.arrObjectId && contextFrame.arrObjectId.length >= 1){
					contextID = contextFrame.arrObjectId[0];
				}else if(idStartIndex > -1){
			idStartIndex = idStartIndex + '&objectId='.length;
			var idEndIndex = locHref.indexOf('&', idStartIndex);
			if(idEndIndex > -1){
				contextID = locHref.substr(idStartIndex,idEndIndex-idStartIndex);
			}else{
				contextID = locHref.substr(idStartIndex);
			}
				}
				return contextID;
};

emxUICore.addToClientCache = function _addToClientCache(obj){
	if(emxUIConstants.STORAGE_SUPPORTED){
		var menucache = localStorage.getItem('menuCache');
		var json = jQuery.parseJSON(menucache);
		var expandedMenus = json.expanded;
		var selector = obj.tagName +'[id="'+ $(obj).attr('id')+'"]' ;
		var index = jQuery.inArray(selector,expandedMenus);
		if(index==-1)
		{
			expandedMenus.push(selector);
		}
		else{
			expandedMenus =emxUICore.removeFromArray(selector,expandedMenus);
		}
		json.expanded=expandedMenus;
		localStorage.setItem('menuCache',emxUICore.toJSONString(json));
	}
};

emxUICore.removeFromArray= function _removeFromArray(value, arr) {
    return jQuery.grep(arr, function(elem, index) {
        return elem !== value;
    })
};

var topcache = null;
function getTopWindow(){
    if (topcache) {
        return topcache;
    }
    if(typeof topMostEnoviaWindow  != "undefined" && topMostEnoviaWindow ){
    	topcache=this;
    	return this;
    }

	try{
        var tmp = top.test = "temp";
        if (top.test === "temp" && typeof top.topMostEnoviaWindow  != "undefined" && top.topMostEnoviaWindow ) {
            topcache = top;
            return top;
        }
    }catch(e){

    }


    var oTop = this;
    while(oTop && oTop.parent != oTop && oTop.name != "mxPortletContent" && typeof oTop.topMostEnoviaWindow  == "undefined"){
        try{
            var doc = oTop.parent.test = "temp";
            if (oTop.parent.test !== "temp") {
                break;
            }
        }catch(e){
            break;
        }
        oTop = oTop.parent;
    }
    topcache = oTop;
    return topcache;
}
function getCSRFToken(){
	var sTokenInjectorUrl= "../common/emxUICSRFTokenInjector.jsp?_="+Date.now();
	return emxUICore.getData(sTokenInjectorUrl);
}
function addSecureToken(form){
	if(typeof form.elements[emxUIConstants.CSRF_TOKEN_KEY] != "undefined"){
		removeSecureToken(form);
	}
	if(emxUIConstants.isCSRFEnabled){
		if(emxUIConstants.isPageTokenEnabled){
			var csrfToken = JSON.parse(getCSRFToken());
			emxUIConstants.CSRF_TOKEN_NAME = csrfToken[emxUIConstants.CSRF_TOKEN_KEY];
			emxUIConstants.CSRF_TOKEN_VALUE = csrfToken[emxUIConstants.CSRF_TOKEN_NAME];
		}
		var input1   = document.createElement('input');
		input1.type  = "hidden";
		input1.name  = emxUIConstants.CSRF_TOKEN_KEY;
		input1.value = emxUIConstants.CSRF_TOKEN_NAME;
		form.appendChild(input1);

		var input2   = document.createElement('input');
		input2.type  = "hidden";
		input2.name  = emxUIConstants.CSRF_TOKEN_NAME;
		input2.value = emxUIConstants.CSRF_TOKEN_VALUE;
		form.appendChild(input2);
	}
}
function removeSecureToken(form){

	if(emxUIConstants.isCSRFEnabled && form){
		var tokenkey = form[emxUIConstants.CSRF_TOKEN_KEY].value;
		if(typeof jQuery != "undefined"){
			jQuery(form[emxUIConstants.CSRF_TOKEN_KEY]).remove();
			jQuery(form[tokenkey]).remove();
		}else{
			form.removeChild(form[emxUIConstants.CSRF_TOKEN_KEY]);
			form.removeChild(form[tokenkey]);
		}
	}
}

function addSecureTokenHeader(objHTTP){

	if(emxUIConstants.isCSRFEnabled){
		if(emxUIConstants.isPageTokenEnabled){
			var csrfToken = JSON.parse(getCSRFToken());
			emxUIConstants.CSRF_TOKEN_NAME = csrfToken[emxUIConstants.CSRF_TOKEN_KEY];
			emxUIConstants.CSRF_TOKEN_VALUE = csrfToken[emxUIConstants.CSRF_TOKEN_NAME];
		}
		objHTTP.setRequestHeader(emxUIConstants.CSRF_TOKEN_KEY,emxUIConstants.CSRF_TOKEN_NAME);
		objHTTP.setRequestHeader(emxUIConstants.CSRF_TOKEN_NAME, emxUIConstants.CSRF_TOKEN_VALUE);
	}
}

function getSecureTokenJSON(){
	if(emxUIConstants.isCSRFEnabled){
		var csrfToken = {};
		if(emxUIConstants.isPageTokenEnabled){
			csrfToken = JSON.parse(getCSRFToken());
			return csrfToken;
		}
		csrfToken =  JSON.parse("{\""+emxUIConstants.CSRF_TOKEN_KEY+"\":\""+emxUIConstants.CSRF_TOKEN_NAME+"\",\""+
		emxUIConstants.CSRF_TOKEN_NAME +"\":\""+ emxUIConstants.CSRF_TOKEN_VALUE+"\"}");
		return csrfToken;
	}
}

function submitWithCSRF(url,target, parameters){
	if(url && typeof url != "undefined" && url.indexOf("|") > -1) {
        			url=url.replace(/\|/g, encodeURIComponent("|"));
    }

	var form    = document.createElement('form');
	form.name   = "emxHiddenCSRFForm";
	form.id     = "emxHiddenCSRFForm";
	addSecureToken(form);
	addAppName(form);
	if(typeof parameters != "undefined")
	{
		addPostParams(parameters,form);	
	}		
	document.body.appendChild(form);

	form.action = url;
	form.method = "post";
	form.target = target.name;
	form.submit();
	removeSecureToken(form);
	document.body.removeChild(form);
}


function addPostParams(parameters, form)
{
	if(parameters=='' || parameters==null)
		return;
	var paramArray = parameters.split('&');
	var input;
	var paramLength=paramArray.length;
	var keyValueArray;
	for(var count=0;count<paramLength;count++)
	{
		keyValueArray=paramArray[count].split('=');
		input   = document.createElement('input');
		input.type  = "hidden";
		input.name  = keyValueArray[0];
		input.value = keyValueArray[1];
		form.appendChild(input);
	}	
}

function addAppName(form)
{
	var currentApp = getTopWindow().currentApp;
	if(currentApp != null && currentApp != 'undefined' )
	{
		var input   = document.createElement('input');
		input.type  = "hidden";
		input.name  = "currentApp";
		input.value = currentApp;
		form.appendChild(input);
	}
}
function callViewer(objectId, action, fileName, format, viewerURL, partId, version)
{
    var newForm = getTopWindow().document.getElementById("commonDocumentCheckout");
    if( newForm == null)
    {
    	newForm = getTopWindow().getWindowOpener().getTopWindow().document.getElementById("commonDocumentCheckout");
    }

    newForm.objectId.value = objectId;
    newForm.id.value = objectId;
    newForm.fileName.value = fileName;
    newForm.file.value = fileName;
    newForm.format.value = format;
    newForm.fileAction.value = action;
    newForm.action = viewerURL;
    newForm.trackUsagePartId.value = partId;
    newForm.version.value = version;

    var intWidth = "730";
    var intHeight = "450";
    var strFeatures = "width=" + intWidth + ",height=" + intHeight;
    var intLeft = parseInt((screen.width - intWidth) / 2);
    var intTop = parseInt((screen.height - intHeight) / 2);
    if (isIE_M) {
            strFeatures += ",left=" + intLeft + ",top=" + intTop;
    } else{
            strFeatures += ",screenX=" + intLeft + ",screenY=" + intTop;
    }
    if (isNS4_M) {
            strFeatures += ",resizable=no";
    } else {
            strFeatures += ",resizable=yes";
    }

    //modified for Bug no : 317605. Dt 03/04/2006. Added scrollbars=yes
    var strFeatures = "width=730,height=450,dependent=yes,resizable=yes,toolbar=yes,scrollbars=yes";
    var win = window.open('', "CheckoutWin", strFeatures);
    newForm.target="CheckoutWin";
    newForm.submit();
}

function xssEncode(value, encodeType){
	return emxUICore.getData("../common/XSSUtil.jsp?paramValue="+ value +"&encodeType="+encodeType);
}

function encodeHREF(URL){
	var encodedURl="";
	var fileName = URL.split("?");
	var enocodeAnd = encodeURIComponent("&");
	var encodeQuestion = encodeURIComponent("?");
	var encodeEqual = encodeURIComponent("=");
	if(fileName.length > 1){
		var parameters = fileName[1].split("&");
		for(var i=0; i<parameters.length;i++){
			var singlekeyValue = parameters[i].split("=");
			var key = singlekeyValue[0];
			var value = singlekeyValue[1];
			var decoded = decodeURIComponent(value);
			if(value == decoded){
				value = encodeURIComponent(value);
			}

			encodedURl += key + encodeEqual + value;
			if(i != parameters.length-1){
				encodedURl += enocodeAnd;
			}
		}
		encodedURl = encodeURIComponent(fileName[0]) + encodeQuestion + encodedURl;
	}else{
		encodedURl = encodeURIComponent(fileName[0]);
	}
	return encodedURl;
}
function encodeAllHREFParameters(URL){
	var encodedURl="";
	var fileName = URL.split("?");
	var tempParameter = "";
	var separator = "";
	if(fileName.length > 1){
		for(var j=1;j<fileName.length;j++){
			tempParameter = tempParameter + separator + fileName[j];
			separator = "?"
		}
		var parameters = tempParameter.split("&");
		for(var i=0; i<parameters.length;i++){
			var singlekeyValue = parameters[i].split("=");
			var key = singlekeyValue[0];
			var value = singlekeyValue[1];
			if(singlekeyValue.length>2){
				value = encodeHREF(parameters[i].substring(key.length+1,parameters[i].length+1));
			} else {
			var decoded = decodeURIComponent(value);
			if(value == decoded){
				value = encodeURIComponent(value);
			}
			}
			encodedURl += key + "=" + value;
			if(i != parameters.length-1){
				encodedURl += "&";
			}
		}
		encodedURl = fileName[0] + "?" + encodedURl;
	}else{
		encodedURl = fileName[0];
	}
	return encodedURl;
}
function getDecodedURLParam(parameter, staticURL, decode){
	   var currLocation = (staticURL.length)? staticURL : window.location.search,
	       parArr = currLocation.split("?")[1].split("&"),
	       returnBool = true;

	   for(var i = 0; i < parArr.length; i++){
	        parr = parArr[i].split("=");
	        if(parr[0] == parameter){
				returnBool = true;
	            return (decode) ? decodeURIComponent(parr[1]) : parr[1];
	        }else{
	            returnBool = false;
	        }
	   }

	   if(!returnBool) return false;
	}
function getWindowOpener(){
	try{
		if(getTopWindow().SnN && getTopWindow().SnN.FROM_SNN){
			resetFromSnN();
			if(getTopWindow().SnN.FRAME_TO_BE_REFRESHED != null && typeof getTopWindow().SnN.FRAME_TO_BE_REFRESHED.name != 'undefined' && getTopWindow().SnN.FRAME_TO_BE_REFRESHED.name != ""){
				frameToRefresh = emxUICore.findFrame(this, getTopWindow().SnN.FRAME_TO_BE_REFRESHED.name);
				return frameToRefresh;
			}else{
				//OPENEROK
				return opener;
			}
		}else{
			//OPENEROK
			return opener;
		}
	}catch(e){}
}
 function getContentWindow(){
	 var contentWindow = getTopWindow();
	 while(contentWindow.getWindowOpener()){
		 contentWindow = contentWindow.getWindowOpener().getTopWindow();
	 }
	 return contentWindow;
 }

function resetFromSnN(){
	var refreshWindow = getTopWindow().SnN.FRAME_TO_BE_REFRESHED;
	refreshWindow.onbeforeunload = function () {
		getTopWindow().SnN.FROM_SNN = false;
	};
}
function closeWindow(isPopup){
	if((getTopWindow().getWindowOpener()|| isPopup) && (!getTopWindow().SnN || !getTopWindow().SnN.FROM_SNN)){
		if(typeof turnOffProgress == "function"){
			turnOffProgress();
		}
		return getTopWindow().close();
	}else if(getTopWindow().slideInFrame && (!getTopWindow().SnN || !getTopWindow().SnN.FROM_SNN))
	{
		getTopWindow().closeSlideInDialog();
	}
}

function getParamValuesByName(url, querystring) {
	  var qstring = url.slice(url.indexOf('?') + 1).split('&');
	  for (var i = 0; i < qstring.length; i++) {
	    var urlparam = qstring[i].split('=');
	    if (urlparam[0] == querystring) {
	       return urlparam[1];
	    }
	  }
	}
//Used to get the navigator window reference.
function getNavigatorWindowRef(){
	var currentTop = getTopWindow();
	var _navigatorWinRef = null;
	if(currentTop.location.href.indexOf("emxNavigator.jsp") >=0){
		_navigatorWinRef = currentTop;
	}else{
		var contentFrame = currentTop.openerFindFrame(currentTop, "content");
		_navigatorWinRef = contentFrame && typeof contentFrame.getTopWindow === "function"? contentFrame.getTopWindow() : null;
	}
	return _navigatorWinRef;
}

//To get XSS encoded value from esapi api
function getXSSEncodedValue(paramJSON){
    var encodedValue = paramJSON.value;
    var navigatorRef = getNavigatorWindowRef();
    if(navigatorRef && paramJSON.value){
    	switch(paramJSON.encodeType){
	    	case "html":
	    		encodedValue = navigatorRef.$ESAPI.encoder().encodeForHTML(paramJSON.value);
	    	    break;
	    	case "htmlattribute":
	    		encodedValue = navigatorRef.$ESAPI.encoder().encodeForHTMLAttribute(paramJSON.value);
	    	    break;
	    	case "javascript":
	    		encodedValue = navigatorRef.$ESAPI.encoder().encodeForJavaScript(paramJSON.value);
	    	    break;
	    	case "url":
	    		encodedValue = encodeURIComponent(paramJSON.value);
	    	    break;
	    	case "css":
	    		encodedValue = navigatorRef.$ESAPI.encoder().encodeForCSS(paramJSON.value);
	    	    break;
	    	default:
	    		encodedValue = paramJSON.value;
	    	    break;
    	}
    }
    return encodedValue;
}

function getXSSDecodedValue(paramJSON){
	var navigatorRef = getNavigatorWindowRef();
    var decodedValue = paramJSON.value;
    if(navigatorRef && paramJSON.value){
    	switch(paramJSON.decodeType){
    	    case "html":
    	    	decodedValue = navigatorRef.$ESAPI.encoder().decodeForHTML(paramJSON.value);
    	    	break;
    	    case "url":
    	    	decodedValue = decodeURIComponent(paramJSON.value);
    	    	break;
    	    default:
    	    	decodedValue = paramJSON.value;
	    	    break;
    	}
    }
    return decodedValue;
}

function updateURLParameter(url, param, paramVal){
    var tempArray = url.split(param+"=");
    var baseURL = tempArray[0];
	
	var finalURL = baseURL;
    for(i=1; i<tempArray.length; i++) {
    	finalURL += updateURLParameterRecur(tempArray[i],param,paramVal);
    }
    return finalURL;
}

function updateURLParameterRecur(url,param,paramVal){
	var position = url.indexOf("&");
    var subString;
    if(position == -1){
    	subString = url.substring(0,url.length);
    }else{
    	subString = url.substring(0,position);
    }
    url = url.replace(subString, paramVal);
    var finalURL = param+"="+url;
	return finalURL;
}


//This function is to remove the key and value of any param from the passed sourceURL
emxUICore.removeURLParam = function removeURLParam(strURL, strparam) {
  if (strURL) {
          var arrURLParts = strURL.split("?");
          var strQueryString = arrURLParts[1];
          var regexp = "(" + strparam + "=\[\\d\\.]*\\&?)";
          strQueryString = strQueryString.replace(new RegExp(regexp), "");
          //strQueryString = strQueryString.replace(new RegExp("(relId=\[\\d\\.]*\\&?)"), "");
          if (strQueryString.lastIndexOf("&") == strQueryString.length-1) {
                  strQueryString = strQueryString.substring(0, strQueryString.length-1);
          }
          arrURLParts[1] = strQueryString;
          return arrURLParts.join("?");
  } else {
          return "";
  }
}

function isSnN () {
	var retVal;
	if(typeof emxUIConstants != "undefined"){
		retVal = emxUIConstants.TOPFRAME_ENABLED;
	}else{
		var data = emxUICore.getData('../resources/bps/menu/topframe');
		retVal = JSON.parse(data).topFrameEnabled;
	}
	return retVal;

}
function isAnyRowSelectedInSB(oXML){
	if(oXML){
		return (emxUICore.selectNodes(oXML,"/mxRoot/rows//r[@checked='checked']").length > 0);
	}else{
		return false;
	}
}





//Function to refresh view in current channel
//frameLocation     Window in which frame exists
//frameName		 Frame name which has to be refreshed
//refreshURL		 URL to load in the frame
function refreshCurrentFrame(frameLocation,frameName,refreshURL){
	var frameContent = findFrame(frameLocation, frameName);
	if ( frameContent != null )	{
		frameContent.document.location.href = refreshURL;
		if((frameContent.getTopWindow().location.href).indexOf("emxNavigator.jsp")==-1){
			frameContent.getTopWindow().opener.location.href = refreshURL;
		}
	} else {
		getTopWindow().opener.getTopWindow().document.location.href = getTopWindow().opener.getTopWindow().document.location.href;
	}
}

/* Persistance API used for Local Storage */
 /**
	 * @author SNA4 Description - An API to store the persistant data into tne
	 *         local storage
	 */


// sets the local storage -key,value,namespace,scope 
//if no namespace is specified then it will be GLOBAL
function setPersistenceData(key, value, nameSpace, scope){
    var storage = jQuery.localStorage;
	if(storage && key != null && key != 'undefined' && key != '' && value != null && value != 'undefined' && value != '')
	{
		if(nameSpace != null && nameSpace != 'undefined' && nameSpace != '')
		{
		} 
		else {
			nameSpace = 'Global';
		}
		if(scope != null && scope != 'undefined' && scope != '')
		{
		}else {
			scope = 'local'; // possible values 'local', 'session' and 'none'
		}
		
		var JsonObj = {
				"value" : value,				
				"scope" : scope
		};
		
		storage.set(nameSpace, key, JsonObj);
	}
	
}

//retrieves the local storage -key,namespace 
//if no namespace is specified then it will be GLOBAL
function getPersistenceData(key, nameSpace ){
	var storage = jQuery.localStorage;	
	try {
		if(nameSpace == null || nameSpace == 'undefined' && nameSpace == '')
		{
			nameSpace = 'Global';
		}
        var val = storage.get(nameSpace,key);
        if(val && val.value)
        {
            return val.value
        }
		return null;
	} catch(e){
		return null;
	}
}

function removePersistenceData( key , nameSpace )
{
	var storage = jQuery.localStorage;
			
	try
	{
		if(key == null)
		{
			storage.remove(nameSpace);
		}
		if(nameSpace == null || nameSpace == 'undefined' && nameSpace == '')
		{
			nameSpace = 'Global';
		}
		else {
			try
			{
				storage.remove(nameSpace,key);
			}
			catch(e)
			{
				return null;
			}
		}
	}
	
	catch(e){
		return null;
	}
}



function getTablename()
{
	var configuredTableName = getRequestSetting('selectedTable');
    if(configuredTableName == null || configuredTableName == 'undefined' || configuredTableName == "")
    	configuredTableName = getTableNameFromXML();

    return configuredTableName;
}

//function to get the cookie value by name
function getCookie(name) {
  var value = "; " + document.cookie;
  var parts = value.split("; " + name + "=");
  if (parts.length == 2) return parts.pop().split(";").shift();
}

//function to set the cookie value by name
function setCookie(c_name, value, exdays) {
    var exdate = new Date();
    exdate.setDate(exdate.getDate() + exdays);
    var c_value = escape(value) + ((exdays == null) ? "" : "; expires=" + exdate.toUTCString());
    document.cookie = c_name + "=" + c_value;
}

//Method to check the session periodically
var timeoutBuffer = 5000;
function checkSession() {
    var sessionExpiry = Math.abs(getCookie(emxUIConstants.STR_AUTOLOGOUT_SERVEREXPIRYTIME));
    var timeOffset = Math.abs(getCookie(emxUIConstants.STR_AUTOLOGOUT_CLIENTTIMEOFFSET));
    var localTime = (new Date()).getTime();
	//console.log("sessionExpiry :: "+sessionExpiry);
	//console.log("timeOffset :: "+timeOffset);
	//console.log("localTime :: "+localTime);
	
    if (((localTime + timeOffset) > (sessionExpiry + timeoutBuffer)) || isNaN(sessionExpiry)) {
		//getTopWindow().closeAllChildWindows();
		//getTopWindow().location.href = getTopWindow().location.href;
		do3DSpaceLogout();
    } else {
        setTimeout('checkSession()', emxUIConstants.STR_AUTOLOGOUT_POLLTIMER);
    }
}



//function to calculate the offset time
function calcTimeoutOffset() {
    var serverTime = getCookie(emxUIConstants.STR_AUTOLOGOUT_SERVERTIME);
    serverTime = serverTime==null ? null : Math.abs(serverTime);
    var clientTimeOffset = (new Date()).getTime() - serverTime;
    setCookie(emxUIConstants.STR_AUTOLOGOUT_CLIENTTIMEOFFSET, clientTimeOffset);
	checkSession();
}

function do3DSpaceLogout(){
	getTopWindow().closeAllChildWindows();
	var url = window.document.location.href;
	var tenant = getParamValuesByName(url, "tenant");
	window.document.location.href = "../emxPreLogout.jsp?tenant="+tenant;
}

function isFullSearchPage(strURL) {
	var isFTS = false;
	if(strURL.indexOf("emxFullSearch.jsp") >= 0){
		isFTS = true;
	}
	return isFTS;
}
function setErrorHandlerWAFData(){
 // WAFData 401 / 403 Passport error : logout.
	require(["DS/WAFData/WAFData"], function (WAFData) {
		if (!WAFData.passportErrorHandler) {
                WAFData.setErrorHandler(function (errorMessage, authURL) {
                    var env = window === window.top ? window : window.top;
                   if (env) {
                        requestReload(emxUIConstants.sessionExpirationTitle, emxUIConstants.sessionExpirationMessage);
                   }
                });
            }
		});
}
function requestReload(title, message) {
		require(["DS/UIKIT/SuperModal"], function (SuperModal) {
                var env = window === window.top ? window : window.top,
                    superModal;
                superModal = new SuperModal({ renderTo: env.document.body });
                superModal.confirm({
                    title: title,
                    message: message.split('. ').join('.\n'),
                    callback: function (confirmed) {
                        if (confirmed) {
                           do3DSpaceLogout();
                        }else{
							//do nothing
						}
                    }
                });
                superModal.modals.current.elements.container.setStyle('white-space', 'pre-line');
		});
}


function encodeSpecialHREFParameters(URL){
	var encodedURl="";
	var fileName = URL.split("?");
	var tempParameter = "";
	var separator = "";
	if(fileName.length > 1){
		for(var j=1;j<fileName.length;j++){
			tempParameter = tempParameter + separator + fileName[j];
			separator = "?"
		}
		var parameters = tempParameter.split("&");
		for(var i=0; i<parameters.length;i++){
			var singlekeyValue = parameters[i].split("=");
			var key = singlekeyValue[0];
			var value = singlekeyValue[1];
			if(value.indexOf("|") > -1){	
				if(singlekeyValue.length>2){
					value = encodeHREF(parameters[i].substring(key.length+1,parameters[i].length+1));
				} else {
				var decoded = decodeURIComponent(value);
				if(value == decoded){
					value = encodeURIComponent(value);
				}
				}
			}
			
			encodedURl += key + "=" + value;
			if(i != parameters.length-1){
				encodedURl += "&";
			}
		}
		encodedURl = fileName[0] + "?" + encodedURl;
	}else{
		encodedURl = fileName[0];
	}
	return encodedURl;
}

	/**
     * 在线预览
     */
   function showDoc() {
    const table = this.emxEditableTable;
    if (table) {
        const  selectRow = table.getCheckedRows();
        const  row = selectRow[0];
        const  strType = row.getAttribute("type");
        const  strOid = row.getAttribute("o");
        if(strType==='Document'){
            let searchUrl = "../components/CUS_DocumentViewEditActionContent.jsp";
            searchUrl += "?objectId="+strOid;
            showModalDialog(searchUrl);
        }else{
            alert("选择的不是文档类型,请重新选择")
        }
    }
}

function checkJFPartRequirementCommitmentDate(newVal, rowId, attrName) {   
    var JFPartRequirementPlanDate = getValueForCell(rowId, "JFPartRequirementPlanDate", "actual");
	var JFPartRequirementCommitmentDate = getValueForCell(rowId, "JFPartRequirementCommitmentDate", "actual");

	if(daysBetween(JFPartRequirementCommitmentDate,JFPartRequirementPlanDate)){
	   return '';
	}else{
		const browserLanguage = navigator.language;
		let message = "";
		if (browserLanguage.startsWith('zh')) {
		message ="零件(T0)供应商承诺日期不可以晚于零件(T0)需求计划日期";
	} else {
		message="The commitment date of the parts (T0) supplier cannot be later than the demand planning date of the parts (T0).";
	}
		return message;
	}
    return '';

}

function daysBetween(date1, date2) {
    const oneDay = 24 * 60 * 60 * 1000; // 每天的毫秒数
    const firstDate = new Date(date1);
    const secondDate = new Date(date2);
    let diff = 0;
    if(secondDate<firstDate){
		return false;
    }
	return true;
  
}