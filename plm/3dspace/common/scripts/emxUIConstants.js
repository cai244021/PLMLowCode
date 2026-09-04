 /*
 *  JavaScript Constants
 *  emxUIConstants.js
 *  Version 1.8
 *  UI Level 3
 *  Requires: (nothing)
 *  Last Updated: 11-Apr-03, Nicholas C. Zakas (NCZ)
 *
 *  This file contains the class definition of the actionbar.
 *
 *  Copyright (c) 1992-2020 Dassault Systemes. All Rights Reserved.
 *  This program contains proprietary and trade secret information
 *  of MatrixOne,Inc. Copyright notice is precautionary only
 *  and does not evidence any actual or intended publication of such program
 *
 *  static const char RCSID[] = $Id: emxUIConstants.js.rca 1.37.2.1 Tue Dec 16 04:55:36 2008 ds-smahapatra Experimental $
 *=================================================================
 */

// Try to consolidate browser detection at on place. Use emxUICore.js
var Browser = (function () {
	/*
	 * User Agent String for all the supported browser
	 *
	 *
	 * */
	var browsers = {
				"IE" : false,
				"FIREFOX" : false,
				"CHROME" : false,
				"MOZILLA_FAMILY": false,
				"SAFARI" : false,
				"MOBILE" : false
 			};


	detectBrowser = function(ua){
		var ie = /(msie|trident)/i.test(ua)
      , chrome = /chrome|crios/i.test(ua)
      , safari = /safari/i.test(ua) && !chrome
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
	};
	detectBrowser(navigator.userAgent);
	return browsers;
}());

var DIR_IMAGES = "../common/images/";
var DIR_STYLES = "../common/styles/";
var DIR_APPLEVEL_STYLES = "../common/styles/";
var DIR_APPLEVEL_IMAGES = "../common/images/";
var DIR_TREE = DIR_IMAGES;
var DIR_NAVBAR = DIR_IMAGES;
var DIR_SEARCHPANE = DIR_IMAGES;
var DIR_BUTTONS = DIR_APPLEVEL_IMAGES + "";
var DIR_SMALL_ICONS = DIR_IMAGES+"";
var DIR_BIG_ICONS = DIR_IMAGES + "";
var DIR_UTIL = DIR_APPLEVEL_IMAGES;
var IMG_BULLET = DIR_IMAGES + "yellowbullet.gif";
var IMG_SPACER = DIR_IMAGES + "utilSpacer.gif";
var IMG_LOADING = DIR_IMAGES + "iconStatusLoading.gif";
var URL_MAIN = "../common/emxMainFrame.asp";
var URL_SHRUNK = "../common/emxShrunkFrame.asp";
var UI_LEVEL = 3;
var CALENDAR_START_DOW = 0;
var HIDDEN_FRAME_LIST = ["listHidden","postHidden","formViewHidden","formEditHidden","formCreateHidden","hiddenFrame","pagehidden","imageHidden","searchHidden"];


var strUserAgent = navigator.userAgent.toLowerCase();
var isKHTML = Browser.SAFARI || Browser.CHROME ;

var isIE = Browser.IE;
var isMoz = Browser.FIREFOX;
var isChrome = Browser.CHROME;

var isMac = navigator.platform.indexOf("Mac") > -1;
var isLinux = navigator.platform.indexOf("Linux") > -1;
var isUnix = strUserAgent.indexOf("x11") > -1;
var isHPUX = strUserAgent.indexOf("hp-ux") > -1;
var isSunOS = strUserAgent.indexOf("sunos") > -1;


//no more supported, should be removed after discussion with application team
var isNS4 = false;
var isMinIE5 = isMinIE51 = isMinIE52 = isMinIE55 = isMinIE6 = false;
var isMinMoz092 = isMinMoz094 = isMinMoz098 = isMinMoz1 = isMinMoz14 = isNS6 = isMinNS6 = isMinNS61 = isMinNS62 = false;
var isMinNS4 = isMinNS45 = isMinNS47 = isMinNS475 = isMinNS479 = isMinNS48 = false;
var isMaxMoz178 = false;
var isMinMoz17 = false;
var isMinMoz16 = false;
var isMinMoz18 = false;
var isMinMoz19 = false;
var isMinMoz1907 = false;

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
        isMinIE51 = fVer >= 5.1;
        isMinIE52 = fVer >= 5.2;
        isMinIE55 = fVer >= 5.5;
        isMinIE6 = fVer >= 6;
        isMaxIE7 = fVer < 8;
        isMinIE9 = fVer > 8;
        isMinIE10 = fVer > 9;
} else if (isMoz) {
        isNS6 = isMinNS6 = true;
        if (strUserAgent.indexOf("rv:") > -1) {
                (new RegExp("rv:(\\d\\.\\d)([\\.\\d]*)")).test(strUserAgent);
                var fMajorVer = RegExp["$1"];
                var fMinorVer = RegExp["$2"];
				isMinMoz092 = isMinNS61 = (fMajorVer == 0.9 && fMinorVer >= 0.2) || (fMajorVer > 0.9);
                isMinMoz094 = isMinNS62 = (fMajorVer == 0.9 && fMinorVer >= 0.4) || (fMajorVer > 0.9);
                isMinMoz098 = (fMajorVer == 0.9 && fMinorVer >= 0.8) || (fMajorVer > 0.9);
                isMinMoz1 = (fMajorVer >= 1.0);
                isMinMoz14 = (fMajorVer >= 1.4);
                isMinMoz16 = (fMajorVer >= 1.6);
                isMinMoz17 = (fMajorVer >= 1.7);
                isMinMoz18 = (fMajorVer >= 1.8);
				isMinMoz19 = (fMajorVer >= 1.9);
                if (fMinorVer != null)
                {
					if (isMinMoz18)
					{
							var minorVerNum = fMinorVer.substring(1);
		                	isMaxMoz18 = ((isMinMoz14 || isMinMoz16) && !(isMinMoz17))|| (isMinMoz17 && minorVerNum <= 8);
					}
					else{
                     	var minorVerNum = fMinorVer.substring(1);
	                		isMaxMoz178 = ((isMinMoz14 || isMinMoz16) && !(isMinMoz17))|| (isMinMoz17 && minorVerNum <= 8);
                    }

                  	if (isMinMoz19)
					{
							var minorVerNum1 = fMinorVer.substring(1,2);
							var minorVerNum2 = fMinorVer.substring(3);
							isMinMoz1907 = ( minorVerNum2 >= 7 || minorVerNum1 >=1  );

					}
                 }
        }
} else if (isNS4) {
        var fVer = parseFloat(navigator.appVersion);
        isMinNS4 = true;
        isMinNS45 = fVer >= 4.5;
        isMinNS46 = fVer >= 4.6;
        isMinNS47 = fVer >= 4.7;
        isMinNS479 = fVer >= 4.79;
        isMinNS48 = fVer >= 4.8;
}
var isWin = navigator.platform.indexOf("Win") > -1;
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
        } else if (isNS4) {
                isWinNT = strUserAgent.indexOf("winnt") > -1;
                isWin95 = strUserAgent.indexOf("win95") > -1;
                isWin98 = strUserAgent.indexOf("win98") > -1;
                isWin2000 = strUserAgent.indexOf("windows nt 5.0") > -1;
                isWinME = strUserAgent.indexOf("win 9x 4.90") > -1;
        }
}
//no more supported, should be removed after discussion with application team




if(typeof emxUIConstants == "undefined"){

	var uiconstantscache = typeof sessionStorage != "undefined" ? sessionStorage.getItem('uiConstantsCache') : null;
	if(uiconstantscache){
		uiconstantscache= JSON.parse(uiconstantscache);
		var emxUIConstants = uiconstantscache;
	}else{
		document.write("<scri" + "pt language=\"JavaScript\" type=\"text/javascript\" src=\"../common/emxUIConstantsJavaScriptInclude.jsp\"></scr" + "ipt>");
	}
}

//! Public Method Array.pop()
//!     This methods pops an item off the back of the array. This
//!     only is used in IE 5.0 because it doesn
//!     pop() method.
if (!Array.prototype.pop) {
        Array.prototype.pop = function () {
                var objItem = this[this.length-1];
                this.length--;
                return objItem;
        }
}
//! Public Method Array.push()
//!     This methods pushes an item onto the back of the array. This
//!     only is used in IE 5.0 because it doesn
//!     push() method.
if (!Array.prototype.push) {
        Array.prototype.push = function (objItem) {
                this[this.length] = objItem;
        }
}
//! Public Method Array.find()
//!     This methods finds the given item and returns its index in
//!     the array.
Array.prototype.find = function (objItem) {
        var bFound = false;
        for (var i=0; i  < this.length && !bFound; i++) {
                if (this[i] == objItem) {
                        bFound = true;
                }
        }
        if (bFound) {
                return i-1;
        } else {
                return -1;
        }
}
//! Public Method Array.remove()
//!     This methods finds the given item and removes it from the array.
//!     It does nothing if the item is not found.
Array.prototype.remove = function (objItem) {
        var iPos = this.find(objItem);
        if (iPos > -1) {
                for (var i= iPos + 1; i < this.length; i++) {
                        this[i-1] = this[i];
                }
                this.length--;
        }
}
//! Public Method String.htmlEncode()
//!     This method converts a string into an HTML string by replacing
//!     all illegal characters with appropriate entities.
String.prototype.htmlEncode = function (blnAll) {
        var strTemp = this;
        if (blnAll) {
                strTemp = strTemp.replace(new RegExp("&", "g"), "&amp;");
                strTemp = strTemp.replace(new RegExp("\\\"", "g"), "&quot;");
                strTemp = strTemp.replace(new RegExp("\\\'", "g"), "&apos;");
        }
        strTemp = strTemp.replace(new RegExp("<", "g"), "&lt;");
        strTemp = strTemp.replace(new RegExp(">'", "g"), "&gt;");
        return strTemp;
}
function jsDocument() {
        this.text = new Array;
        for (var i=0; i < arguments.length; i++) {
                this.text[i] = arguments[i];
        }
        this.write = function (str) { this.text[this.text.length] = str; };
        this.writeln = function (str) { this.text[this.text.length] = str + "\n"; };
        this.toString = function () { return this.text.join(""); };
        this.clear = function () { delete this.text; this.text = new Array; };
        this.writeHTMLHeader = function (strStylesheet) {
                this.write("<!DOCTYPE html><html><head>");
                this.write("<link rel=\"stylesheet\" href=\"");
                this.write(getStyleSheet("emxUIDefault"));
                this.write("\">");
                this.write("<link rel=\"stylesheet\" href=\"");
                this.write(strStylesheet);
                this.write("\">");
                //Modified For Bug : 348007
                this.write("<link rel=\"stylesheet\" href=\"");
                this.write(getStyleSheet("emxUIMenu"));
                this.write("\">");
				this.write("<script type=\"text/javascript\" src=\"../common/scripts/emxUICore.js\"></script>");
                this.write("</head>");
        };
        this.writeBody = function (style) { this.writeln("<body" + (style ? " class=\"" + style + "\"" : "") + ">"); };
        this.writeHTMLFooter = function () { this.writeln("</body></html>"); };
}
//! Public Function findFrame()
//!     This function finds a frame with a given name.
function findFrame(objWindow, strName) {
        if (strName == "_top") {
                return getTopWindow();
        } else if (strName == "_self") {
                return self;
        } else if (strName == "_parent") {
                return parent;
        } else {
                var objFrame = null;
                try
                {
                	if(objWindow && objWindow.frames){
                		for (var i = 0; i < objWindow.frames.length && !objFrame; i++) {
							try {
								if (objWindow.frames[i] && objWindow.frames[i].name && objWindow.frames[i].name == strName) {
									objFrame = objWindow.frames[i];
								}
							} catch(ex){
							}
                          }
                	}

                }
                catch(e) {
					if(e.description && e.description.search(/Denied/i) == -1)
					{
						  if(-2146828218 != e.number && -2147418094 != e.number)
						  {
							if(e.description == ""){
								alert(emxUIConstants.STR_JS_AnExceptionOccurred + " " + emxUIConstants.STR_JS_ErrorName + " " + e.name
					    				+ emxUIConstants.STR_JS_ErrorDescription + " " + e.description
					    				+ emxUIConstants.STR_JS_ErrorNumber + " " + e.number
					    				+ emxUIConstants.STR_JS_ErrorMessage + " " + e.message);
							} else if(-2147024891!= e.number){
								alert(e.description);
						    }
	                   }
	                }
                }

                if (!objFrame) {
                	try
                	{
                		if(objWindow && objWindow.frames){
                			for (var i=0; i < objWindow.frames.length && !objFrame; i++) {
                                objFrame = findFrame(objWindow.frames[i], strName);
                			}
                		}
                  }
					catch(e)
					{
						if(e.description && e.description.search(/Denied/i) == -1)
						{
						  if(-2146828218 != e.number && -2147418094 != e.number)
						  {
							if(e.description == ""){
								alert(emxUIConstants.STR_JS_AnExceptionOccurred + " " + emxUIConstants.STR_JS_ErrorName + " " + e.name
					    				+ emxUIConstants.STR_JS_ErrorDescription + " " + e.description
					    				+ emxUIConstants.STR_JS_ErrorNumber + " " + e.number
					    				+ emxUIConstants.STR_JS_ErrorMessage + " " + e.message);
							} else if(-2147024891!= e.number){
								alert(e.description);
							}
						}
                  }
                }
                }
            	return objFrame;
        }
}
//! Public Function openerFindFrame()
//!     This function finds a frame from current window or its
//!     getWindowOpener() with a given name.
function openerFindFrame(objWindow, strName) {

		var objFrame = null;
		try{
			objFrame = findFrame(objWindow, strName);
	        var objOpenerWindow = null;
	        if (!objFrame){
	                if (objWindow && objWindow.getWindowOpener() && !objWindow.getWindowOpener().closed){
	                        objOpenerWindow = objWindow.getWindowOpener().getTopWindow();
	                        if (objOpenerWindow) {
	                                objFrame = openerFindFrame(objOpenerWindow, strName)
	                        }
	                }
	        }
	    }
	    catch(e){
		// ignore any error (function returns null)
	    }
        return objFrame;
}
//! Public Function addURLParam()
//!     This function adds a parameter to a URL.
function addURLParam(strURL, strParam) {
        var strNewURL = strURL;
        if (strNewURL.indexOf(strParam) == -1){
                strNewURL += (strNewURL.indexOf('?') > -1 ? '&' : '?') + strParam;
        }
        return strNewURL;
}
//! Public Function addUniqueURLParam()
//!     This function adds a parameter to a URL if not existing.
function addUniqueURLParam(strURL, strParam) {
        var strNewURL = strURL;
        var arrayParamNameValue = new Array;
        if (strParam && strParam.indexOf("=") != -1){
                arrayParamNameValue = strParam.split('=');
                strParamName = arrayParamNameValue[0];
                if (strNewURL.indexOf(strParamName + "=") == -1){
                        strNewURL += (strNewURL.indexOf('?') > -1 ? '&' : '?') + strParam;
                }
        }
        return strNewURL;
}
//! Public Function showError()
//!     This function displays an alert with error text.
function showError(strError) {
        alert(emxUIConstants.STR_ERROR_HEADER + strError);
}
//! Public Function showConfirmation()
//!     This function displays a confirmation box with confirmation text.
function showConfirmation(strConfirm) {
        return confirm(STR_CONFIRM_HEADER + strConfirm);
}
//! Public Function doNothing()
//!     This function is assigned to an event in order to block its occurance.
function doNothing() {
        return false;
}
//! Private Function getStyleSheet()
//!     This function creates the style sheet string for a given style sheet prefix.
function getStyleSheet(strCSSPrefix, cssPath) {
        var strCSSFile = "";
        if(cssPath){
        	strCSSFile = cssPath + strCSSPrefix;
        }else{
        	strCSSFile = DIR_STYLES + strCSSPrefix;
        }
        if (isUnix) {
                strCSSFile += "_Unix.css";
        } else {
                strCSSFile += ".css";
        }
        return strCSSFile;
}
//! Public Function addStyleSheet()
//!     This function adds a style sheet to the given document.
//! we have to refactor this method as not to take cssPath once we move all the CSS from main folder into common/styles folder
function addStyleSheet(strCSSPrefix, cssPath) {
        var strCSSFile = getStyleSheet(strCSSPrefix, cssPath);
        document.write("<link rel=\"stylesheet\" type=\"text/css\" ");
        document.write("href=\"" + strCSSFile + "\">");
}

//! Public Function appendStyleSheet()
//!     This function appends a style sheet to the given document.
//! we have to refactor this method as not to take cssPath once we move all the CSS from main folder into common/styles folder
function appendStyleSheet(strCSSPrefix, cssPath) {
	var link = document.createElement('link');
	var strCSSFile = getStyleSheet(strCSSPrefix, cssPath);
    link.setAttribute('rel', 'stylesheet');
    link.setAttribute('type', 'text/css');
    link.setAttribute('href', strCSSFile);
    document.getElementsByTagName('head')[0].appendChild(link);
}

//! Public Function turnOffProgress()
//!     This function changes the progress clock so that it disappears.
function turnOffProgress() {
	try{
		var isfound = false;
		if(parent.turnOffProgressTop){
			parent.turnOffProgressTop();
		}else if(getTopWindow().turnOffProgressTop){
			getTopWindow().turnOffProgressTop();
			isfound = true;
		}
		if((document.getElementById('imgProgressDiv')) != null)
		{
			document.getElementById('imgProgressDiv').style.visibility = 'hidden';
			isfound = true;		
		}
		else if((document.getElementById('imgLoadingProgressDiv')) != null){
			document.getElementById('imgLoadingProgressDiv').style.visibility = 'hidden';
			isFound=true;
		}
		else if((parent.frames[0])!=null && (parent.frames[0].document.getElementById('imgProgressDiv')) != null)
		{
			parent.frames[0].document.getElementById('imgProgressDiv').style.visibility = 'hidden';
			isfound = true;		
		}
		else if ((parent.frames[1])!=null &&  (parent.frames[1].document.getElementById('imgProgressDiv')) != null)
		{
			parent.frames[1].document.getElementById('imgProgressDiv').style.visibility = 'hidden';
			isfound = true;		
		}
		else if((getTopWindow().document != null) && (getTopWindow().document.getElementById('imgProgressDiv')) != null)
		{
			getTopWindow().document.getElementById('imgProgressDiv').style.visibility = 'hidden';
			isfound = true;		
		}
		else if((parent.document != null)) {
			if(parent.document.getElementById('imgProgressDiv') != null){
			parent.document.getElementById('imgProgressDiv').style.visibility = 'hidden';
			}else if(parent.document.getElementById('imgProgressDivChannel')!=null){
				jQuery(parent.document).find('div#imgProgressDivChannel').each(function(elem, value){value.style.visibility = 'hidden';});
			}
			isfound = true;		
		}
		if((document.getElementById('imgProgressDivChannel')) != null)
		{
			document.getElementById('imgProgressDivChannel').style.visibility = 'hidden';
			document.getElementById("imgLoadingProgressDiv").style.visibility = 'hidden';
			isfound = true;		
	    }
		else if((parent.frames[0])!=null && (parent.frames[0].document.getElementById('imgProgressDivChannel')) != null)
		{
			parent.frames[0].document.getElementById('imgProgressDivChannel').style.visibility = 'hidden';
			parent.frames[0].document.getElementById("imgLoadingProgressDiv").style.visibility = 'hidden';
		    isfound = true;		
	    }
		else if ((parent.frames[1])!=null &&  (parent.frames[1].document.getElementById('imgProgressDivChannel')) != null)
		{
			parent.frames[1].document.getElementById('imgProgressDivChannel').style.visibility = 'hidden';
			parent.frames[1].document.getElementById('imgLoadingProgressDiv').style.visibility = 'hidden';
		    isfound = true;		
	    }
		else if((getTopWindow().document != null) && (getTopWindow().document.getElementById('imgProgressDivChannel')) != null)
		{
			getTopWindow().document.getElementById('imgProgressDivChannel').style.visibility = 'hidden';
			getTopWindow().document.getElementById('imgLoadingProgressDiv').style.visibility = 'hidden';
		    isfound = true;		
	    }
		else if((parent.document != null) && (parent.document.getElementById('imgProgressDivChannel')) != null) {
			parent.document.getElementById('imgProgressDivChannel').style.visibility = 'hidden';
			parent.document.getElementById('imgLoadingProgressDiv').style.visibility = 'hidden';
		    isfound = true;		
	    }
	    if(portalMode == "true"){
			toggleProgress('hidden');
		    isfound = true;		
	    }
		if(!isfound)
		{
			setTimeout("turnOffProgress()", 500);
		}
	}catch(e){
		//do nothing
	}
}

//! Public Function turnOnProgress()
//!     This function changes the progress clock so that it reappears.
function turnOnProgress(strImage) {
	
	try{
		var isfound=false;
		if(parent.turnOnProgressTop)
			parent.turnOnProgressTop();
		else if(getTopWindow().turnOnProgressTop)
			getTopWindow().turnOnProgressTop();

		if((document.getElementById('imgProgressDiv')) != null)
		{
			document.getElementById('imgProgressDiv').style.visibility = 'visible';
		    isfound = true;		
		}
		else if((document.getElementById('imgLoadingProgressDiv')) != null){
			document.getElementById('imgLoadingProgressDiv').style.visibility = 'visible';
			isFound=true;
		}
		else if ((parent.frames[0])!=null && (parent.frames[0].document.getElementById('imgProgressDiv')) != null)
		{
			parent.frames[0].document.getElementById('imgProgressDiv').style.visibility = 'visible';
		    isfound = true;		
		}
		else if ((parent.frames[1])!=null && parent.frames[1].document && (parent.frames[1].document.getElementById('imgProgressDiv')) != null)
		{
			parent.frames[1].document.getElementById('imgProgressDiv').style.visibility = 'visible';
		    isfound = true;		
		}
		else if((getTopWindow().document != null) && (getTopWindow().document.getElementById('imgProgressDiv')) != null)
		{
			getTopWindow().document.getElementById('imgProgressDiv').style.visibility = 'visible';
		    isfound = true;		
		}
		else if((parent.document != null)) {
			if(parent.document.getElementById('imgProgressDiv') != null){
			parent.document.getElementById('imgProgressDiv').style.visibility = 'visible';
			}else if(parent.document.getElementById('imgProgressDivChannel')!=null){
				parent.document.getElementById('imgProgressDivChannel').style.visibility = 'visible';
			}
		    isfound = true;		
	    }
		if((document.getElementById('imgProgressDivChannel')) != null)
		{
			document.getElementById('imgProgressDivChannel').style.visibility = 'visible';
			document.getElementById('imgLoadingProgressDiv').style.visibility = 'visible';
			document.getElementById('imgLoadingProgressDiv').innerHTML = emxUIConstants.STR_LOADING;
		    isfound = true;		
	    }
		else if ((parent.frames[0])!=null && (parent.frames[0].document.getElementById('imgProgressDivChannel')) != null)
		{
			parent.frames[0].document.getElementById('imgProgressDivChannel').style.visibility = 'visible';
			parent.frames[0].document.getElementById('imgLoadingProgressDiv').style.visibility = 'visible';
			parent.frames[0].document.getElementById('imgLoadingProgressDiv').innerHTML = emxUIConstants.STR_LOADING;
		    isfound = true;		
	    }
		else if ((parent.frames[1])!=null && parent.frames[1].document && (parent.frames[1].document.getElementById('imgProgressDivChannel')) != null)
		{
			parent.frames[1].document.getElementById('imgProgressDivChannel').style.visibility = 'visible';
			parent.frames[1].document.getElementById('imgLoadingProgressDiv').style.visibility = 'visible';
			parent.frames[1].document.getElementById('imgLoadingProgressDiv').innerHTML = emxUIConstants.STR_LOADING;
		    isfound = true;		
	    }
		else if((getTopWindow().document != null) && (getTopWindow().document.getElementById('imgProgressDivChannel')) != null)
		{
			getTopWindow().document.getElementById('imgProgressDivChannel').style.visibility = 'visible';
			getTopWindow().document.getElementById('imgLoadingProgressDiv').style.visibility = 'visible';
			getTopWindow().document.getElementById('imgLoadingProgressDiv').style.innerHTML = emxUIConstants.STR_LOADING;
		    isfound = true;		
	    }
		else if((parent.document != null) && (parent.document.getElementById('imgProgressDivChannel')) != null) {
			parent.document.getElementById('imgProgressDivChannel').style.visibility = 'visible';
			parent.document.getElementById('imgLoadingProgressDiv').style.visibility = 'visible';
			parent.document.getElementById('imgLoadingProgressDiv').innerHTML = emxUIConstants.STR_LOADING;
		    isfound = true;		
		}
		if(!isFound)
		{

			setTimeout("turnOnProgress()", 500);
		}
	}catch(e){
			//do nothing
	}
}


//��ȡ��ǰ��¼��
function getCurrentUser(callback) {
        var xhr = new XMLHttpRequest();
        xhr.open("GET", "https://"+window.location.hostname+"/3ddashboard/api/users/current", true);
        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    var data = JSON.parse(xhr.responseText);
                    callback(data);
                }
            }
        };
        xhr.send();
    }

    let watermark = null;
    let numOfWatermarks = 0;

//����ˮӡ
    function createWatermark() {
        watermark = document.createElement('div');
        watermark.classList.add('watermark');
        getCurrentUser(function (data) {
            const screenWidth = window.innerWidth * 2;
            const screenHeight = window.innerHeight * 2;
            const padding = 50; // ����ˮӡ֮��ļ��
            numOfWatermarks = Math.floor(screenWidth / 800); // ����ÿ�е�ˮӡ����
            const watermarkWidth = (screenWidth - (padding * 2)) / numOfWatermarks;
            const watermarkHeight = screenHeight / 10;
            for (let i = 0; i < numOfWatermarks * 10; i++) {
                const span = document.createElement('span');
                span.innerText = data.login;
                span.style.width = watermarkWidth + "px";
                span.style.height = watermarkHeight + "px";
                span.style.position = "fixed";
                span.style.left = padding + (i % numOfWatermarks) * watermarkWidth + "px";
                span.style.top = Math.floor(i / numOfWatermarks) * watermarkHeight + "px";
                span.style.fontSize = "30px";
                span.style.color = "rgba(0, 0, 0, 0.2)";
                span.style.margin = "10px";
                watermark.appendChild(span);
            }
        });
        watermark.style.pointerEvents = "none";
        watermark.style.position = "fixed";
        watermark.style.top = 0;
        watermark.style.left = 0;
        watermark.style.width = "50vw";
        watermark.style.height = "200vh";
        watermark.style.zIndex = 9999;
        watermark.style.display = "flex";
        watermark.style.flexWrap = "wrap";
        watermark.style.opacity = 0.5;
        watermark.style.transform = "rotate(330deg)";
if(parent.document.documentElement === parent.parent.document.documentElement){
var watermark1 = parent.parent.document.getElementsByClassName("watermark");
if(watermark1.length === 0){
parent.document.documentElement.appendChild(watermark);
}
}
    }
    function updateWatermarkLayout() {
        if (watermark) {
            watermark.remove();
            createWatermark();
        }
    }
var resizeTimer;
function delayedUpdateWatermarkLayout() {
    clearTimeout(resizeTimer);
    resizeTimer = setTimeout(function() {
        updateWatermarkLayout();
    }, 200); // �����ӳ�ʱ�䣬��λΪ����
}
//����ҳ�����ţ����¼���ˮӡ
    window.addEventListener('resize', delayedUpdateWatermarkLayout);
    createWatermark();


/*
 *  add by ljr 2024/07/19
 *  ��ѯ���еĲ��ž���������Ա������
 *
 * */
function addJSApprovePersonContributor(){
	var contributorHidden = document.getElementById("JSApprovePerson");
	var sURL=	'../common/emxFullSearch.jsp?field=TYPES=type_Person:CURRENT=policy_Person.state_Active&txtExcludeOIDs='+contributorHidden.value+'&table=AEFGeneralSearchResults&selection=multiple&hideHeader=true&submitURL=../enterprisechangemgtapp/ECMUtil.jsp?mode=searchUtilPerson&targetTag=select&selectName=Contributor&inputFieldHidden=JSApprovePerson';
	showChooser(sURL, 850, 630);
}

 /*
  *  add by ljr 2025/04/21
  *  数据外发 审批人 抓取项目的id
  *
  * */
 function addJSApprovePerson(){
	 var contributorHidden = document.getElementsByName('JSConnectProjectOID')[0].value;
	 console.log("contributorHidden:", contributorHidden);
	 var sURL = '';
	 if (contributorHidden === '') {
		 // "/emxFullSearch.jsp?field=TYPES=type_Person&table=AEFGeneralSearchResults&selection=single&includeOIDprogram=JF_DataOutSource:getDataSourceApprovePersons&submitURL=./JF_AEFSearchUtil.jsp&fieldNameDisplay=ApprovePersonDisplay&fieldNameOID=ApprovePersonOID&fieldNameActual=ApprovePerson";
		 sURL=	'../common/emxFullSearch.jsp?field=TYPES=type_Person&table=AEFGeneralSearchResults&selection=single&includeOIDprogram=JF_DataOutSource:getDataSourceApprovePersons&submitURL=../common/JF_AEFSearchUtil.jsp&fieldNameDisplay=ApprovePersonDisplay&fieldNameOID=ApprovePersonOID&fieldNameActual=ApprovePerson';
	 } else {
		 sURL=	'../common/emxFullSearch.jsp?field=TYPES=type_Person&table=AEFGeneralSearchResults&selection=single&includeOIDprogram=JF_DataOutSource:getDataSourceApprovePersons&projectId='+ contributorHidden +'&submitURL=../common/JF_AEFSearchUtil.jsp&fieldNameDisplay=ApprovePersonDisplay&fieldNameOID=ApprovePersonOID&fieldNameActual=ApprovePerson';
	 }
	 showChooser(sURL, 850, 630);
 }

 /*
 *  add by ljr 2025/09/09
 *  数据外发 所属项目的选择
 *
 * */
 function viewProjectSpaceDesc() {
	 var sURL=	'../common/emxFullSearch.jsp?field=TYPES=type_ProjectSpace&cancelLabel=emxFramework.Common.Close&HelpMarker=emxhelpselectorganization&includeOIDprogram=JF_PublicMethodClass:getSystemAllProjectSpaces&table=AEFGeneralSearchResults&selection=single&showInitialResults=true&submitURL=./JF_AEFSearchUtil.jsp&fieldNameDisplay=JSConnectProjectDisplay&fieldNameOID=JSConnectProjectOID&fieldNameActual=JSConnectProject';
	 showChooser(sURL, 850, 630);
 }

 /**
  * add by chenyan 2024 0829
  *  使用channel的方式刷新数据 防止加载过多js 响应速度慢
  */
 function addJSApprovePersonDEV(){
	  const channel = new BroadcastChannel("JF_ECR_EDIT_DEV_NOTICE");
	  var contributorHidden = document.getElementById("JSApprovePerson");
      channel.addEventListener("message", (event) => {
        console.log(event.data)
	    var personList =  JSON.parse(event.data);
	    var contributor =  document.getElementsByName("Contributor");
		console.log("contributorHidden",contributorHidden);
	  for (let i = 0; i < personList.length; i++) {
		  var person = personList[i];
		  var option = document.createElement("option");
		  option.value = person['id'];
		  option.text = person['fullName'];
		  if (contributorHidden.value){
			  contributorHidden.value+=",";
		  }
		  contributorHidden.value+=person['id'];
		  contributor[0].add(option);
	  }
        channel.close();
      });
	var sURL=	'../common/emxFullSearch.jsp?field=TYPES=type_Person:CURRENT=policy_Person.state_Active&txtExcludeOIDs='+contributorHidden.value+'&table=AEFGeneralSearchResults&selection=multiple&hideHeader=true&submitURL=../common/JF_ECMUtil.jsp?mode=searchUtilPerson&targetTag=select&selectName=Contributor&inputFieldHidden=JSApprovePerson';
	showChooser(sURL, 850, 630);
}


 /**
  * 添加受影响的项目
  */
 function addJSAffectedProject(){
	 const channel = new BroadcastChannel("JF_ECR_EDIT_DEV_NOTICE");
	 var contributorHidden = document.getElementById("JFAffectedProject");
	 channel.addEventListener("message", (event) => {
		 console.log(event.data)
		 var projectList =  JSON.parse(event.data);
		 var contributor =  document.getElementsByName("JFAffectedProjectSelect");
		 console.log("contributorHidden",contributorHidden);
		 for (let i = 0; i < projectList.length; i++) {
			 var project = projectList[i];
			 var option = document.createElement("option");
			 option.value = project['id'];
			 option.text = project['name'];
			 if (contributorHidden.value){
				 contributorHidden.value+=",";
			 }
			 contributorHidden.value+=project['id'];
			 contributor[0].add(option);
		 }
		 channel.close();
	 });
	 var sURL=	'../common/emxFullSearch.jsp?field=TYPES=type_ProjectSpace&includeOIDprogram=JF_PublicMethodClass:getSystemAllProjectSpaces&txtExcludeOIDs='+contributorHidden.value+'&table=AEFGeneralSearchResults&selection=multiple&hideHeader=true&submitURL=../common/JF_ECMUtil.jsp?mode=searchUtilPerson&targetTag=select&selectName=JFAffectedProjectSelect&inputFieldHidden=JFAffectedProject';
	 showChooser(sURL, 850, 630);
 }

/*
 *  add by ljr 2024/07/19
 *  �Ƴ�ѡ���ž���������Ա
 *
 * */
function removeJSApprovePerson(){
	var selectTag = document.getElementsByName("Contributor");
	var selectedOptionsValue = "";
	var bIsContributorFieldModified = "false";
	for (var i=selectTag[0].options.length-1;i>=0;i--) {
		if (selectTag[0].options[i].selected) {
			if (selectedOptionsValue!="") {
				selectedOptionsValue += ",";
			}
			selectedOptionsValue += selectTag[0].options[i].value;
			selectTag[0].remove(i);
			bIsContributorFieldModified = "true";
		}
	}
	//To make the decision of calling connect/disconnect method only on field modification.
	if(bIsContributorFieldModified==="true"){
		var isContributorFieldModified = document.getElementById("IsContributorFieldModified");
		isContributorFieldModified.value = "true";
	}
	var contributorHidden = document.getElementById("JSApprovePerson");
	var contributorHiddenValues = contributorHidden.value.split(",");
	var selectedOptionsValues = selectedOptionsValue.split(",");
	var contributorHiddenNewValue = "";
	for (var j=0;j<contributorHiddenValues.length;j++) {
		var contributorHiddenValue = contributorHiddenValues[j];
		var contains = "false";
		for (var k=0;k<selectedOptionsValues.length;k++) {
    		var selectedOptionValue = selectedOptionsValues[k];
    		if (contributorHiddenValue == selectedOptionValue) {
    			contains = "true";
    		}
    	}
		if (contains == "false") {
			if (contributorHiddenNewValue!="") {
				contributorHiddenNewValue += ",";
    		}
			contributorHiddenNewValue += contributorHiddenValue;
		}
	}
	contributorHidden.value = contributorHiddenNewValue;
}

 /**
  *
  * 移除选中的受影响项目
  */
 function removeJSAffectedProject(){
	 var selectTag = document.getElementsByName("JFAffectedProjectSelect");
	 var selectedOptionsValue = "";
	 var bIsContributorFieldModified = "false";
	 for (var i=selectTag[0].options.length-1;i>=0;i--) {
		 if (selectTag[0].options[i].selected) {
			 if (selectedOptionsValue!="") {
				 selectedOptionsValue += ",";
			 }
			 selectedOptionsValue += selectTag[0].options[i].value;
			 selectTag[0].remove(i);
			 bIsContributorFieldModified = "true";
		 }
	 }
	 //To make the decision of calling connect/disconnect method only on field modification.
	 if(bIsContributorFieldModified==="true"){
		 var isContributorFieldModified = document.getElementById("JFAffectedProjectFieldModified");
		 isContributorFieldModified.value = "true";
	 }
	 var contributorHidden = document.getElementById("JFAffectedProject");
	 var contributorHiddenValues = contributorHidden.value.split(",");
	 var selectedOptionsValues = selectedOptionsValue.split(",");
	 var contributorHiddenNewValue = "";
	 for (var j=0;j<contributorHiddenValues.length;j++) {
		 var contributorHiddenValue = contributorHiddenValues[j];
		 var contains = "false";
		 for (var k=0;k<selectedOptionsValues.length;k++) {
			 var selectedOptionValue = selectedOptionsValues[k];
			 if (contributorHiddenValue == selectedOptionValue) {
				 contains = "true";
			 }
		 }
		 if (contains == "false") {
			 if (contributorHiddenNewValue!="") {
				 contributorHiddenNewValue += ",";
			 }
			 contributorHiddenNewValue += contributorHiddenValue;
		 }
	 }
	 contributorHidden.value = contributorHiddenNewValue;
 }

 function openDownloadModel(mode){
	 require(["DS/Windows/Dialog",
		 "DS/Controls/Button",
		 "DS/UIKIT/Form",
		 "i18n!DS/ENONewWidget/assets/nls/ENONewWidgetNls"],
		 function (dialog,btn,form,i18n){
		 v = [
			 new btn({
				 label: i18n.get("Btn_OK"),
				 onClick: function () {
					 //关闭按钮
					 myDialog.buttons.Yes.disabled = true;
					 const exportTypeField = myDialog.content.getField("exportType");
					 const  strValue = exportTypeField.value;
					 console.log("strValue",strValue);
					 if (!strValue){
						 getTopWindow().showTransientMessage(i18n.get("Export_Type_IsRequire"), 'warning', 'alert-right-search')
						 myDialog.buttons.Yes.disabled = false;
						 return;
					 }
					 //获取表格的window
					 const  tableWin = findFrame(window,"detailsDisplay");
					 if (tableWin){
						 const urlParameters = tableWin.location.search.substr(1);
						 const parameters = new URLSearchParams(urlParameters);
						 const  strObjectId = parameters.get("objectId");
						 const  selectRowArr = tableWin.emxEditableTable.getCheckedRows();
						 let strArgsRowId = "" ;
						 for (let i = 0; i < selectRowArr.length; i++) {
							 const  row = selectRowArr[i];
							 const  strOid = row.getAttribute("o");
							 strArgsRowId +=strOid ;
							 if (i  < selectRowArr.length-1){
								 strArgsRowId +="," ;
							 }
						 }
						 console.log("strArgsRowId",strArgsRowId);
						 getTopWindow().showTransientMessage(i18n.get("Export_Export_Loading"), 'info', 'alert-right-search')
						 const strMode = mode ? mode : "";
						 const strUrl = `./JF_LastRevisionDownLoad.jsp?mode=${strMode}&downType=ajax&objectId=${strObjectId}&selectRowIds=${strArgsRowId}&exportType=${strValue}`;
						 let downIframe = tableWin.document.getElementById("downloadIframe");
						 if (!downIframe){
							 downIframe = tableWin.document.createElement('iframe');
							 downIframe.style.display = 'none'; // 隐藏iframe

							 downIframe.id = "downloadIframe"; // 设置iframe的src属性为文件下载地址
							 // 将iframe添加到DOM中
							 document.body.appendChild(downIframe);
						 }
						 //情况src
						 if (downIframe.src){
							 downIframe.src = "";
						 }
						 downIframe.src = strUrl; // 设置iframe的src属性为文件下载地址
						 myDialog.close();
					 }

				 },
				 className: "primary",
			 }),
			 new btn({
				 label: i18n.get("Btn_Cancel"),
				 onClick: function () {
					 myDialog.close();
				 },
				 className: "default",
			 })
		 ];

		 var newForm = new form({
			 // className:"form-horizontal",//设置水平
			 // grid:"4 8",//水平比例
			 fields: [
				 {
					 type: "select",
					 label: i18n.get("Export_Type_Label"),
					 required: !0,
					 name:"exportType",
					 placeholder: i18n.get("Export_Type_Placeholder"),
					 options: [
						 { "label": i18n.get("Export_Type_LastRevision"), "value": "LastRevision" },
						 { "label": i18n.get("Export_Type_LastRevisionRelease"), "value": "LastRevisionRelease" }
					 ]
				 }
			 ]

		 });
		 f = {
			 title: i18n.get("Export_Type_Title"),
			 content: newForm,
			 buttonArray: v
		 };

		 const  myDialog = new dialog({
			 id: "myinfo",
			 class: "myinfoclass",
			 title: f.title,
			 content: f.content,
			 activeFlag: !1,
			 resizableFlag: !0,
			 height: 250,
			 width: 400,
			 buttons: { Yes: f.buttonArray[0], No: f.buttonArray[1] },
		 });
		 myDialog.show();
	 });

 }

 function addJSSTDPerson(){
	 const channel = new BroadcastChannel("JF_ECR_EDIT_DEV_NOTICE");
	 var contributorHidden = document.getElementById("JFSTDPerson");
	 console.log("contributorHidden:",contributorHidden)
	 channel.addEventListener("message", (event) => {
		 console.log(event.data)
		 var personList =  JSON.parse(event.data);
		 var contributor =  document.getElementsByName("JFSTDPersonSelect");
		 console.log("contributorHidden",contributorHidden);
		 for (let i = 0; i < personList.length; i++) {
			 var person = personList[i];
			 var option = document.createElement("option");
			 option.value = person['id'];
			 option.text = person['fullName'];
			 if (contributorHidden.value){
				 contributorHidden.value+=",";
			 }
			 contributorHidden.value+=person['id'];
			 contributor[0].add(option);
		 }
		 channel.close();
	 });
	 var sURL=	'../common/emxFullSearch.jsp?field=TYPES=type_Person:CURRENT=policy_Person.state_Active&txtExcludeOIDs='+contributorHidden.value+'&table=AEFGeneralSearchResults&selection=multiple&hideHeader=true&submitURL=../common/JF_ECMUtil.jsp?mode=searchUtilPerson&targetTag=select&selectName=Contributor&inputFieldHidden=JFSTDPerson';
	 showChooser(sURL, 850, 630);
 }

 function removeJSSTDPerson(){
	 var selectTag = document.getElementsByName("JFSTDPersonSelect");
	 var selectedOptionsValue = "";
	 var bIsContributorFieldModified = "false";
	 for (var i=selectTag[0].options.length-1;i>=0;i--) {
		 if (selectTag[0].options[i].selected) {
			 if (selectedOptionsValue!="") {
				 selectedOptionsValue += ",";
			 }
			 selectedOptionsValue += selectTag[0].options[i].value;
			 selectTag[0].remove(i);
			 bIsContributorFieldModified = "true";
		 }
	 }
	 //To make the decision of calling connect/disconnect method only on field modification.
	 if(bIsContributorFieldModified==="true"){
		 var isContributorFieldModified = document.getElementById("JFSTDPersonFieldModified");
		 isContributorFieldModified.value = "true";
	 }
	 var contributorHidden = document.getElementById("JFSTDPerson");
	 var contributorHiddenValues = contributorHidden.value.split(",");
	 var selectedOptionsValues = selectedOptionsValue.split(",");
	 var contributorHiddenNewValue = "";
	 for (var j=0;j<contributorHiddenValues.length;j++) {
		 var contributorHiddenValue = contributorHiddenValues[j];
		 var contains = "false";
		 for (var k=0;k<selectedOptionsValues.length;k++) {
			 var selectedOptionValue = selectedOptionsValues[k];
			 if (contributorHiddenValue == selectedOptionValue) {
				 contains = "true";
			 }
		 }
		 if (contains == "false") {
			 if (contributorHiddenNewValue!="") {
				 contributorHiddenNewValue += ",";
			 }
			 contributorHiddenNewValue += contributorHiddenValue;
		 }
	 }
	 contributorHidden.value = contributorHiddenNewValue;
 }

 function addDocumentPerson(ContributorHidden,Contributor,groupName){
	 var contributorHidden = document.getElementById(ContributorHidden);
	 var sURL = "";
	 if (ContributorHidden==='ContributorHidden'){
		 sURL=	'../common/emxFullSearch.jsp?field=TYPES=type_Person:CURRENT=policy_Person.state_Active&txtExcludeOIDs='+contributorHidden.value+'&table=AEFGeneralSearchResults&selection=multiple&hideHeader=true&submitURL=../enterprisechangemgtapp/ECMUtil.jsp?mode=searchUtilPerson&targetTag=select&selectName='+Contributor+'&inputFieldHidden='+ContributorHidden;
	 }else {
		 sURL=	'../common/emxFullSearch.jsp?field=TYPES=type_Person:CURRENT=policy_Person.state_Active&groupName='+groupName+'&includeOIDprogram=JF_PublicMethodClass:getUserGroupPersons&txtExcludeOIDs='+contributorHidden.value+'&table=AEFGeneralSearchResults&selection=multiple&hideHeader=true&submitURL=../enterprisechangemgtapp/ECMUtil.jsp?mode=searchUtilPerson&targetTag=select&selectName='+Contributor+'&inputFieldHidden='+ContributorHidden;
	 }
	 showChooser(sURL, 850, 630);
 }

 function removeDocumentPerson(ContributorHidden,Contributor,IsContributorFieldModified){
	 var selectTag = document.getElementsByName(Contributor);
	 var selectedOptionsValue = "";
	 var bIsContributorFieldModified = "false";
	 for (var i=selectTag[0].options.length-1;i>=0;i--) {
		 if (selectTag[0].options[i].selected) {
			 if (selectedOptionsValue!="") {
				 selectedOptionsValue += ",";
			 }
			 selectedOptionsValue += selectTag[0].options[i].value;
			 selectTag[0].remove(i);
			 bIsContributorFieldModified = "true";
		 }
	 }
	 //To make the decision of calling connect/disconnect method only on field modification.
	 if(bIsContributorFieldModified==="true"){
		 var isContributorFieldModified = document.getElementById(IsContributorFieldModified);
		 isContributorFieldModified.value = "true";
	 }
	 var contributorHidden = document.getElementById(ContributorHidden);
	 var contributorHiddenValues = contributorHidden.value.split(",");
	 var selectedOptionsValues = selectedOptionsValue.split(",");
	 var contributorHiddenNewValue = "";
	 for (var j=0;j<contributorHiddenValues.length;j++) {
		 var contributorHiddenValue = contributorHiddenValues[j];
		 var contains = "false";
		 for (var k=0;k<selectedOptionsValues.length;k++) {
			 var selectedOptionValue = selectedOptionsValues[k];
			 if (contributorHiddenValue == selectedOptionValue) {
				 contains = "true";
			 }
		 }
		 if (contains == "false") {
			 if (contributorHiddenNewValue!="") {
				 contributorHiddenNewValue += ",";
			 }
			 contributorHiddenNewValue += contributorHiddenValue;
		 }
	 }
	 contributorHidden.value = contributorHiddenNewValue;
 }

 function checkInECRAttachment() {
	 var id=document.getElementsByName("objectId")[0].value;
	 console.log("----111--->"+id);
	 var fileInput = document.getElementById('FileFullPath');

	 var state;
	 console.log(fileInput);
	 console.log(fileInput.files.length);
	 const selectElement = document.getElementById('ReceiptDocSelect');
	 console.log(selectElement);

	 var badCharName;
	 var badCharinFile = "";
	 var badCharFileList = "\n";
	 var showBadFileAlert = false;
	 for (var i=0;i<fileInput.files.length;i++) {
		 var file = fileInput.files[i];
		 var fileName=file.name;
		 badCharName = getAllInvalidCharsInFileName(fileName);
		 if (badCharName.length != 0)
		 {
			 showBadFileAlert = true;
			 badCharinFile = badCharinFile + badCharName;
			 badCharFileList = badCharFileList + fileName +"\n";
		 }
	 }
	 if(showBadFileAlert) {
		 var uniqueList = badCharinFile.split(' ');
		 var result = [];
		 for (var i = 0; i < uniqueList.length; i++) {
			 if (result.indexOf(uniqueList[i]) == -1) {
				 result.push(uniqueList[i]);
			 }
		 }
		 badCharinFile = result.join(" ");
		 var alertMessage ="\u60a8\u8f93\u5165\u7684\u5185\u5bb9\u4e2d\u5305\u542b\u65e0\u6548\u5b57\u7b26"+":";

		 alert(alertMessage + badCharFileList + "\u4e0d\u5141\u8bb8\u8f93\u5165\u4e0b\u9762\u5217\u51fa\u7684\u5b57\u7b26"+"/n" + "',#,$,@,%" + "\u8bf7\u79fb\u9664");
		 return;
	 }else {
		 for (var i = 0; i < fileInput.files.length; i++) {
			 var file = fileInput.files[i];
			 var fileInputName = file.name;
			 var enfileInputName = encodeURIComponent(fileInputName);
			 console.log("----enfileInputName---" + enfileInputName);

			 var url = "../enterprisechangemgtapp/JF_ECMCheckInAttachment.jsp?fileName=" + enfileInputName + "&objectId=" + id;
			 var responseText = getFilePostData(url, file);
			 console.log("----responseJSONObject--->" + responseText);
			 var responseJSONObject = emxUICore.parseJSON(responseText);

			 console.log("----responseJSONObject--->" + responseJSONObject);
			 state = responseJSONObject["state"];
			 if (state == "1") {
				 var errorMassage = responseJSONObject["error"];
				 //alert("\u9644\u4ef6\u4e0a\u4f20\u5931\u8d25\uff1a"+errorMassage);
			 } else {
				 var optionid = responseJSONObject["id"];
				 var optionname = responseJSONObject["title"];


				 const newOption = document.createElement('option');
				 newOption.value = optionid;
				 newOption.textContent = optionname;
				 selectElement.appendChild(newOption);
			 }

		 }

		 if (state == "1") {
			 alert("\u9644\u4ef6\u4e0a\u4f20\u5931\u8d25");
		 } else {
			 alert("\u9644\u4ef6\u4e0a\u4f20\u6210\u529f\uff01");
		 }

	 }

 }


 function clearECMAttachment() {

	 const select = document.getElementById('ReceiptDocSelect');
	 console.log(select);
	 const values = [];
	 for (let i = 0; i < select.options.length; i++) {
		 if (select.options[i].selected) {
			 values.push(select.options[i].value);
		 }
	 }

	 var id=document.getElementsByName("objectId")[0].value;
	 console.log("----id=--!!!222->=="+id);
	 console.log(values);
	 var state;
	 for (let i = 0; i < values.length; i++) {
		 const valueid=values[i];
		 var url = "../enterprisechangemgtapp/JF_ECMDelectAttachment.jsp?objectId="+id+"&docid="+valueid;
		 var responseText = getFilePostData(url);
		 console.log("----responseJSONObject--->"+responseText);
		 var responseJSONObject = emxUICore.parseJSON(responseText);
		 console.log("----responseJSONObject--->"+responseJSONObject);
		 state = responseJSONObject["state"];
		 if(state == "1"){
			 var errorMassage = responseJSONObject["error"];
			 // alert("\u6e05\u9664\u5931\u8d25\uff1a"+errorMassage);
		 }else{
			 // alert("\u6e05\u9664\u6210\u529f\uff01");
		 }
	 }
	 if(state=="1"){

	 }else{
		 Array.from(select.selectedOptions).forEach(opt => {
			 opt.remove(); // 或使用select.remove(opt.index)
		 });
	 }



 }


 function checkInECRAttachmentSpecial() {
	 var id=document.getElementsByName("objectId")[0].value;
	 console.log("----111--->"+id);
	 var fileInput = document.getElementById('FileFullPath2');

	 var state;
	 console.log(fileInput);
	 console.log(fileInput.files.length);
	 const selectElement = document.getElementById('ReceiptDocSelect2');
	 console.log(selectElement);



	 var badCharName;
	 var badCharinFile = "";
	 var badCharFileList = "\n";
	 var showBadFileAlert = false;
	 for (var i=0;i<fileInput.files.length;i++) {
		 var file = fileInput.files[i];
		 var fileName=file.name;
		 badCharName = getAllInvalidCharsInFileName(fileName);
		 if (badCharName.length != 0)
		 {
			 showBadFileAlert = true;
			 badCharinFile = badCharinFile + badCharName;
			 badCharFileList = badCharFileList + fileName +"\n";
		 }
	 }
	 if(showBadFileAlert) {
		 var uniqueList = badCharinFile.split(' ');
		 var result = [];
		 for (var i = 0; i < uniqueList.length; i++) {
			 if (result.indexOf(uniqueList[i]) == -1) {
				 result.push(uniqueList[i]);
			 }
		 }
		 badCharinFile = result.join(" ");
		 var alertMessage ="\u60a8\u8f93\u5165\u7684\u5185\u5bb9\u4e2d\u5305\u542b\u65e0\u6548\u5b57\u7b26"+":";

		 alert(alertMessage + badCharFileList + "\u4e0d\u5141\u8bb8\u8f93\u5165\u4e0b\u9762\u5217\u51fa\u7684\u5b57\u7b26"+"/n" + "',#,$,@,%" + "\u8bf7\u79fb\u9664");
		 return;
	 }else {
		 for(var i=0;i<fileInput.files.length;i++){
			 var file = fileInput.files[i];
			 var fileInputName=file.name;
			 var enfileInputName = encodeURIComponent(fileInputName);
			 console.log("----enfileInputName---"+enfileInputName);

			 var url = "../enterprisechangemgtapp/JF_ECMCheckInAttachmentSpecia.jsp?fileName=" + enfileInputName+"&objectId="+id;
			 var responseText = getFilePostData(url, file);
			 console.log("----responseJSONObject--->"+responseText);
			 var responseJSONObject = emxUICore.parseJSON(responseText);

			 console.log("----responseJSONObject--->"+responseJSONObject);
			 state = responseJSONObject["state"];
			 if(state == "1"){
				 var errorMassage = responseJSONObject["error"];
				 alert(errorMassage);
			 }else {
				 var optionid = responseJSONObject["id"];
				 var optionname = responseJSONObject["title"];
				 const newOption = document.createElement('option');
				 newOption.value = optionid;
				 newOption.textContent = optionname;
				 selectElement.appendChild(newOption);
				 alert("\u9644\u4ef6\u4e0a\u4f20\u6210\u529f\uff01");
			 }

		 }

	 }


	 // if(state == "1"){
		//  alert("\u9644\u4ef6\u4e0a\u4f20\u5931\u8d25");
	 // }else {
		//  alert("\u9644\u4ef6\u4e0a\u4f20\u6210\u529f\uff01");
	 // }



 }


 function getAllInvalidCharsInFileName(fileName) {
	 if (!fileName || typeof fileName !== 'string') {
		 return [];
	 }
	 const invalidCharSet = new Set();
	 const forbiddenChars = '"\',#$@%'; // 所有不允许的字符

	 for (const char of fileName) {
		 if (forbiddenChars.includes(char)) {
			 invalidCharSet.add(char);
		 }
	 }

	 return Array.from(invalidCharSet); // 转为数组，保持顺序（按首次出现）
 }


 function clearECMAttachmentSpecial() {

	 const select = document.getElementById('ReceiptDocSelect2');
	 console.log(select);
	 const values = [];
	 for (let i = 0; i < select.options.length; i++) {
		 if (select.options[i].selected) {
			 values.push(select.options[i].value);
		 }
	 }

	 var id=document.getElementsByName("objectId")[0].value;
	 console.log("----id=--!!!222->=="+id);
	 console.log(values);
	 var state;
	 for (let i = 0; i < values.length; i++) {
		 const valueid=values[i];
		 var url = "../enterprisechangemgtapp/JF_ECMDelectAttachment.jsp?objectId="+id+"&docid="+valueid;
		 var responseText = getFilePostData(url);
		 console.log("----responseJSONObject--->"+responseText);
		 var responseJSONObject = emxUICore.parseJSON(responseText);
		 console.log("----responseJSONObject--->"+responseJSONObject);
		 state = responseJSONObject["state"];
		 if(state == "1"){
			 var errorMassage = responseJSONObject["error"];
			 // alert("\u6e05\u9664\u5931\u8d25\uff1a"+errorMassage);
		 }else{
			 // alert("\u6e05\u9664\u6210\u529f\uff01");
		 }
	 }
	 if(state=="1"){

	 }else{
		 Array.from(select.selectedOptions).forEach(opt => {
			 opt.remove(); // 或使用select.remove(opt.index)
		 });
	 }



 }

 function getFilePostData(strURL, strData, fnCallback, oClientData) {
	 ElapsedTimer.enter(strURL + " " + strData + " " + fnCallback ? ("ASYNCH: ") : "synchronous");
	 if (typeof strURL != "string") {
		 ElapsedTimer.exit("FAIL");
		 emxUICore.throwError("Required parameter strURL is null or not a string.");
	 }
	 var objHTTP = emxUICore.createHttpRequest();
	 objHTTP.open("post", strURL, fnCallback != null);
	 objHTTP.setRequestHeader("charset", "UTF-8");
	 objHTTP.setRequestHeader("Content-Type", "multipart/form-data");
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
		 };
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
 }

 function SyncMBOMToMDMModel(){
	 require(["DS/Windows/Dialog",
			 "DS/Controls/Button",
			 "DS/UIKIT/Form",
			 "i18n!DS/ENONewWidget/assets/nls/ENONewWidgetNls"],
		 function (dialog,btn,form,i18n){
			 v = [
				 new btn({
					 label: i18n.get("Btn_OK"),
					 onClick: function () {
						 //关闭按钮
						 myDialog.buttons.Yes.disabled = true;
						 const exportTypeField = myDialog.content.getField("exportType");
						 const  strValue = exportTypeField.value;

						 const selectedOptions = exportTypeField.selectedOptions;
						 const selectedValues = Array.from(selectedOptions).map(opt => opt.value);
						 console.log("多选值：", selectedValues); // 输出数组形式的多选值


						 console.log("selectedValues",selectedValues);
						 if (selectedValues.length<1){
							 getTopWindow().showTransientMessage(i18n.get("SyncMBOMToMDMModel_IsRequire"), 'warning', 'alert-right-search')
							 myDialog.buttons.Yes.disabled = false;
							 return;
						 }
						 //获取表格的window
						 const  tableWin = findFrame(window,"detailsDisplay");
						 if (tableWin){
							 const urlParameters = tableWin.location.search.substr(1);
							 const parameters = new URLSearchParams(urlParameters);
							 const  strObjectId = parameters.get("objectId");
							 const  selectRowArr = tableWin.emxEditableTable.getCheckedRows();
							 let strArgsRowId = "" ;
							 for (let i = 0; i < selectRowArr.length; i++) {
								 const  row = selectRowArr[i];
								 const  strOid = row.getAttribute("o");
								 strArgsRowId +=strOid ;
								 if (i  < selectRowArr.length-1){
									 strArgsRowId +="," ;
								 }
							 }
							 console.log("strArgsRowId",strArgsRowId);
							 getTopWindow().showTransientMessage(i18n.get("SyncMBOMToMDMModel_Loading"), 'info', 'alert-right-search')
							 const strUrl = `./JF_SyncMBOMToMDM.jsp?=ajax&objectId=${strObjectId}&&exportType=${selectedValues}`;
							 let downIframe = tableWin.document.getElementById("downloadIframe");
							 if (!downIframe){
								 downIframe = tableWin.document.createElement('iframe');
								 downIframe.style.display = 'none'; // 隐藏iframe

								 downIframe.id = "downloadIframe"; // 设置iframe的src属性为文件下载地址
								 // 将iframe添加到DOM中
								 document.body.appendChild(downIframe);
							 }
							 //情况src
							 if (downIframe.src){
								 downIframe.src = "";
							 }
							 downIframe.src = strUrl; // 设置iframe的src属性为文件下载地址
							 myDialog.close();
						 }

					 },
					 className: "primary",
				 }),
				 new btn({
					 label: i18n.get("Btn_Cancel"),
					 onClick: function () {
						 myDialog.close();
					 },
					 className: "default",
				 })
			 ];

			 var newForm = new form({
				 // className:"form-horizontal",//设置水平
				 // grid:"4 8",//水平比例
				 fields: [
					 {
						 type: "select",
						 multiple: true,
						 label: i18n.get("SyncMBOMToMDMModel_Label"),
						 required: !0,
						 name:"exportType",
						 placeholder: i18n.get("SyncMBOMToMDMModel_Placeholder"),
						 options: [
							 { "label": i18n.get("SyncMBOMToMDMModel_1021"), "value": "1021" },
							 { "label": i18n.get("SyncMBOMToMDMModel_1031"), "value": "1031" },
							 { "label": i18n.get("SyncMBOMToMDMModel_1071"), "value": "1071" },
							 { "label": i18n.get("SyncMBOMToMDMModel_1091"), "value": "1091" },
							 { "label": i18n.get("SyncMBOMToMDMModel_1121"), "value": "1121" },
							 { "label": i18n.get("SyncMBOMToMDMModel_1151"), "value": "1151" },
							 { "label": i18n.get("SyncMBOMToMDMModel_1161"), "value": "1161" },
							 { "label": i18n.get("SyncMBOMToMDMModel_9061"), "value": "9061" },
							 { "label": i18n.get("SyncMBOMToMDMModel_9071"), "value": "9071" },
							 { "label": i18n.get("SyncMBOMToMDMModel_2031"), "value": "2031" },
							 { "label": i18n.get("SyncMBOMToMDMModel_1211"), "value": "1211" },
							 { "label": i18n.get("SyncMBOMToMDMModel_2021"), "value": "2021" }

						 ]
					 }
				 ]

			 });
			 f = {
				 title: i18n.get("SyncMBOMToMDMModel_Tilte"),
				 content: newForm,
				 buttonArray: v
			 };

			 const  myDialog = new dialog({
				 id: "myinfo",
				 class: "myinfoclass",
				 title: f.title,
				 content: f.content,
				 activeFlag: !1,
				 resizableFlag: !0,
				 height: 250,
				 width: 800,
				 buttons: { Yes: f.buttonArray[0], No: f.buttonArray[1] },
			 });
			 myDialog.show();
		 });

 }

 // 公共方法 - 添加人员
 function addPersonContributor(fieldName){
	 var contributorHidden = document.getElementById(fieldName);
	 console.log("contributorHidden:{}", contributorHidden);
	 console.log("fieldName:{}", fieldName);
 // &txtExcludeOIDs='+contributorHidden.value+'
	 var sURL=	'../common/emxFullSearch.jsp?field=TYPES=type_Person&table=AEFGeneralSearchResults&groupName=department_manager&form=PMCCommonPersonSearchForm&showInitialResults=true&selection=multiple&selection=multiple&hideHeader=true&submitURL=../enterprisechangemgtapp/ECMUtil.jsp?mode=searchUtilPerson&targetTag=select&selectName=' + fieldName + 'Contributor&inputFieldHidden=' + fieldName;
	 console.log("fieldName:{}", fieldName);
	 showChooser(sURL, 850, 630);
 }

//部门经理用户组
 function addDepManagerPersonContributor(fieldName){
	 var contributorHidden = document.getElementById(fieldName);
	 console.log("contributorHidden:{}", contributorHidden);
	 console.log("fieldName:{}", fieldName);
	 // &txtExcludeOIDs='+contributorHidden.value+'
	 var sURL=	'../common/emxFullSearch.jsp?field=TYPES=type_Person&table=AEFGeneralSearchResults&groupName=department_manager&includeOIDprogram=JF_PublicMethodClass:getUserGroupPersons&form=PMCCommonPersonSearchForm&showInitialResults=true&selection=multiple&selection=multiple&hideHeader=true&submitURL=../enterprisechangemgtapp/ECMUtil.jsp?mode=searchUtilPerson&targetTag=select&selectName=' + fieldName + 'Contributor&inputFieldHidden=' + fieldName;
	 console.log("fieldName:{}", fieldName);
	 showChooser(sURL, 850, 630);
 }

 // 公共方法 - 移除人员
 function removePersonContributor(fieldName){
	 var selectTag = document.getElementsByName(fieldName + "Contributor");
	 var selectedOptionsValue = "";
	 var bIsContributorFieldModified = "false";
	 for (var i=selectTag[0].options.length-1;i>=0;i--) {
		 if (selectTag[0].options[i].selected) {
			 if (selectedOptionsValue!="") {
				 selectedOptionsValue += ",";
			 }
			 selectedOptionsValue += selectTag[0].options[i].value;
			 selectTag[0].remove(i);
			 bIsContributorFieldModified = "true";
		 }
	 }
	 //To make the decision of calling connect/disconnect method only on field modification.
	 if(bIsContributorFieldModified==="true"){
		 var isContributorFieldModified = document.getElementById("IsContributorFieldModified");
		 isContributorFieldModified.value = "true";
	 }
	 var contributorHidden = document.getElementById(fieldName);
	 var contributorHiddenValues = contributorHidden.value.split(",");
	 var selectedOptionsValues = selectedOptionsValue.split(",");
	 var contributorHiddenNewValue = "";
	 for (var j=0;j<contributorHiddenValues.length;j++) {
		 var contributorHiddenValue = contributorHiddenValues[j];
		 var contains = "false";
		 for (var k=0;k<selectedOptionsValues.length;k++) {
			 var selectedOptionValue = selectedOptionsValues[k];
			 if (contributorHiddenValue == selectedOptionValue) {
				 contains = "true";
			 }
		 }
		 if (contains == "false") {
			 if (contributorHiddenNewValue!="") {
				 contributorHiddenNewValue += ",";
			 }
			 contributorHiddenNewValue += contributorHiddenValue;
		 }
	 }
	 contributorHidden.value = contributorHiddenNewValue;
 }

 // add by liujr 20240709 Validation Email Address
 function IdmCheckEmailAddress(newValue) {
	 console.log("value:", newValue);
	 var value = -1;
	 //table校验时使用this.value
	 //拿到浏览器语言
	 var language = navigator.language || navigator.userLanguage;
	 // 使用正则表达式匹配
	 var strMess = "";
	 var regex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
	 if (!newValue) {
		 value = this.value;
	 } else {
		 value = newValue;
	 }
	 value = value.replace(",", ";");
	 var array = value.split(";"); // 将字符串拆分为字符数组

	 if (language.includes("zh") || language.includes("zh-CN")) {
		 strMess = "\u6536\u4ef6\u4eba\u90ae\u7bb1\u683c\u5f0f\u9519\u8bef,\u8bf7\u586b\u5199\u6b63\u786e\u7684\u90ae\u7bb1!";
	 } else {
		 strMess = "Note:Partner general manager email format is wrong, please fill in the correct email!";
	 }
	 console.log("value:", value);
	 console.log("array:", array);
	 var flag = 0;
	 for (var i = 0; i < array.length; i++) {
		 var arrValue = array[i];
		 if (regex.test(arrValue)) {
			 continue;
		 }
		 flag = 1;
		 break;
	 }
	 console.log("flag:", flag);

	 if (0 === flag) {
		 return true;
	 } else {
		 alert(strMess);
		 return false;
	 }

 }

 //check: where value is N of JFWaterMark,Approver is requie
 function isJFWaterMarkN(newValue) {
	 var JFWaterMark=  document.getElementById("JSWaterMarkId").value;
	 console.log(JFWaterMark);
	 console.log(newValue);
	 if(typeof newValue === 'undefined') {
		 newValue = document.getElementsByName("JSApprovePersonDisplay")[0].value;
	 }
	 console.log(newValue);
	 var strMess = '';
	 if(JFWaterMark == 'N'){
		 if(newValue == '') {
			 strMess = "\u662f\u5426\u6253\u6c34\u5370\u4e3a\u5426\u65f6\uff0c\u8bf7\u9009\u62e9\u90e8\u95e8\u7ecf\u7406\u5ba1\u6279\u4eba\uff01";
		 }
	 }
	 if (strMess == ''){
		 return true;
	 }else {
		 alert(strMess);
		 return false;
	 }
 }

 // add by liujr 20240709 WaterMark of Approver
 function checkJSWaterMark(value) {
	 var selectValue = this.options[this.selectedIndex].value;
	 console.log("checkJSWaterMark:", selectValue);
	 if(selectValue=='Y') {
		 document.getElementsByName("JSApprovePersonDisplay")[0].value=''
		 var tr = document.getElementById("calc_JSApprovePerson");
		 tr.children[0].className="createLabel";
	 } else {
		 var tr = document.getElementById("calc_JSApprovePerson");
		 tr.children[0].className="createLabelRequired";
	 }
 }