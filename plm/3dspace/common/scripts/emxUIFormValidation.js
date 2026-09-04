//=================================================================
// JavaScript emxUIFormValidation.js
//
// Copyright (c) 1992-2020 Dassault Systemes.
// All Rights Reserved.
// This program contains proprietary and trade secret information of MatrixOne,Inc.
// Copyright notice is precautionary only
// and does not evidence any actual or intended publication of such program
//=================================================================
// emxUIFormValidation.js
// This file is used to add any validation routines to be used by the UIForm component
//-----------------------------------------------------------------

function reloadAccessColumn() {
    emxEditableTable.reloadCell("Access");
}

// functions for "Drop files here or click to select", the selected files will be stored in getTopWindow().files_to_checkin
function filesDragOver(div) {
    div.className = "dropAreaHover";
    document.getElementById("dropAreaIcon").src = "images/iconActionAddHover.png";
}

function filesDragLeave(div) {
    div.className = "dropArea";
    document.getElementById("dropAreaIcon").src = "images/iconActionAdd.png";
}

function filesDrop(div, e) {
    filesDragLeave(div);
    var files = e.dataTransfer.files;
    filesSelected(div, files);
}

function fileInputChanged(div, e) {
    filesSelected(div.parentElement, div.files);
}

function mouseoutDropArea(div) {
    div.className = 'dropArea';
}

function mouseoverDropArea(div) {
    div.className = 'dropAreaMouseOver';
}

function isFileAlreadySelected(file, selectedFiles) {
    var n = selectedFiles.length;
    for (var i = 0; i < n; i++) {
        var selectedFile = selectedFiles[i];
        if (file.name.toUpperCase() === selectedFile.name.toUpperCase()) { // check file name
            return true;
        }
    }
    return false;
}

function filterOutAlreadySelectedFiles(newSelectFiles, selectedFiles) { // return filtered files
    var filteredFiles = [];
    for (var i = 0; i < newSelectFiles.length; i++) {
        var file = newSelectFiles[i];
        if (isFileAlreadySelected(file, selectedFiles) === false) {
            filteredFiles.push(file);
        }
    }
    return filteredFiles;
}

function removeFile(removeButton) {
    var divDropArea = removeButton.divDropArea;
    var divFileInfo = removeButton.divFileInfo;
    var n = divDropArea.files_to_checkin.length;
    for (var i = 0; i < n; i++) {
        var file = divDropArea.files_to_checkin[i];
        if (file === divFileInfo.file) {
            divDropArea.files_to_checkin.splice(i, 1);
            break;
        }
    }
    onFileSelectionChange(divDropArea.files_to_checkin);
    divFileInfo.parentElement.removeChild(divFileInfo);
}

function onFileSelectionChange(selectedFiles) {
    if (selectedFiles == null || selectedFiles.length == 0) {
        return;
    }

    getTopWindow().files_to_checkin = selectedFiles;
    var titleDiv = document.getElementById('Title');
    if(titleDiv){
	    var currentTitle = titleDiv.value;
	    var autoTitle = titleDiv.autoTitle;
	    if (autoTitle === undefined) {
	        autoTitle = "";
	    }
	    if (autoTitle === currentTitle) {
	        titleDiv.value = selectedFiles[0].name;
	        titleDiv.autoTitle = titleDiv.value;
	    }
    }
}

function filesSelected(div, files) {
    if (files == null || files.length == 0) {
        return;
    }
    if (div.files_to_checkin === undefined || div.files_to_checkin === null) {
        div.files_to_checkin = [];
    }
    files = filterOutAlreadySelectedFiles(files, div.files_to_checkin);
    for (var i = 0; i < files.length; i++) {
        div.files_to_checkin.push(files[i]);
    }
    onFileSelectionChange(div.files_to_checkin);

    for (var i = 0; i < files.length; i++) {
        var file = files[i];
        var divFileInfo = document.createElement('div');
        var parent = div.parentElement.parentElement;
        if (parent.childNodes.length > 1) {
            parent.insertBefore(divFileInfo, parent.childNodes[1])
        }
        else {
            parent.appendChild(divFileInfo);
        }
        divFileInfo.file = file;

        var divFileName = document.createElement('div');
        divFileName.className = 'fileName';
        divFileName.textContent = file.name;
        var removeButton = document.createElement('img');
        removeButton.src = 'images/iconActionClosePanel.png';
        removeButton.className = 'removeButton';
        removeButton.onmouseover = function () { this.className = 'removeButtonMouseOver'; }
        removeButton.onmouseout = function () { this.className = 'removeButton'; }
        removeButton.divDropArea = div;
        removeButton.divFileInfo = divFileInfo;
        removeButton.onclick = function () { removeFile(this); }
        divFileName.appendChild(removeButton);
        divFileInfo.appendChild(divFileName);

        var commentTextArea = document.createElement('textarea');
        divFileInfo.file.comment = "";
        commentTextArea.onchange = function () {
            this.parentElement.file.comment = this.value;
        }
        commentTextArea.onfocus = function () {
            if (this.className == 'fileCommentInit') {
                this.className = 'fileCommentEdit';
                this.value = '';
            }
        }
        commentTextArea.rows = "1";
        commentTextArea.className = "fileCommentInit";
        commentTextArea.textContent = MSG_ADD_COMMENT; // "Click to add comment" - emxComponents.CommonDocument.UploadFiles.AddComment
        divFileInfo.appendChild(commentTextArea);
    }
}
// ~ end of functions for "Drop files here or click to select"

function emxCreateRemoveExtraRowAboveType() { // Remove extra row above Type on emxCreate.jsp table
    var extraRowInnerHtml = '<td align="center">&nbsp;</td>';
    var table = document.getElementsByClassName('form')[0];
    for (var i = 0; i < table.rows.length; i++) {
        if (table.rows.item(i).innerHTML === extraRowInnerHtml) {
            table.deleteRow(i); break;
        }
    }
}

function checkRangeValue(fieldObj){
	if(!fieldObj){
		fieldObj = this;
	}
	var attributeName = fieldObj.name;
	attributeName = attributeName.substring(attributeName.indexOf('|')+1);
	var attributeValue = fieldObj.value;
	if(attributeValue != ''){
		
		attributeValue = attributeValue.replace(/\[/g, '');
		attributeValue = attributeValue.replace(/\]/g, '');

		if(/::/g.test(attributeValue)){
			var rangeValues = attributeValue.split(/::/g);
			var minVal = rangeValues[0];
			var maxVal = rangeValues[1];
			if(isNaN(minVal) || isNaN(maxVal) || (attributeValue.match(/::/g)).length>1){
				var stralert = emxUIConstants.INVALID_RANGE_FORMAT;
				stralert = stralert.replace(/\{0}/, attributeName);
				alert(stralert);
				return false;
			} else if(Number(minVal) >= Number(maxVal)){
				var stralert = emxUIConstants.MINIMUM_NOT_LESS_THAN_MAXIMUM;
				stralert = stralert.replace(/\{0}/, attributeName);
				alert(stralert);
				return false;
			}
		} else if(isNaN(attributeValue) || Number(attributeValue) <= 0){
			var stralert = emxUIConstants.INVALID_RANGE_VALUE;
			stralert = stralert.replace(/\{0}/, attributeName);
			alert(stralert);
			return false;
		}
	}
	return true;
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
// add by chenyan 2024/07/22  是否平台件选择是时，影响工厂可填写且必填
function checkAffectsFactoryInputAccess(){
	//获取form中元素实际保存值
	var strIsPlatformPart = emxFormGetValue("JFIsPlatformPart").current.actual;
    const affectsFactory =  FormHandler.GetField("JFAffectsFactory");
    const  domAffectsFactory = document.getElementById('calc_JFAffectsFactory');
    affectsFactory.HandlerField[0].customValidate = checkInputIsNull;
    console.log(strIsPlatformPart);
    if ("No" === strIsPlatformPart){
        //禁用 影响工厂 输入框
        emxFormSetFieldEditable("JFAffectsFactory",false);
        //清空原输入
        const  options = FormHandler.GetField("JFAffectsFactory").HandlerField;
        let isAlert = false;
        for (let i = 0; i < options.length; i++) {
            const option = options[i];
            if (option.checked){
                option.checked =  false
            }
        }
        //移除必填校验
        //移除必填样式
        if (domAffectsFactory){
            domAffectsFactory.querySelector('td:first-child').className = "createLabel"
        }
    }else {
        emxFormSetFieldEditable("JFAffectsFactory",true);
        //添加必填校验 isZeroLength（DS校验非空JS）
        //添加必填样式
        if (domAffectsFactory){
            domAffectsFactory.querySelector('td:first-child').className = "createLabelRequired"
        }
    }
}

// add by chenyan 2024/07/22
//
// 开发费工时变更（小时）、试验开发费变更（元） 变更来源选择Both，内部
// 开发费工时变更（小时）- 外部、试验费变更（元）-外部 变更来源选择Both，外部
function checkDevAndTrialInputAccess(){
    //获取form中元素实际保存值
    var strChangeSource = emxFormGetValue("JFChangeSource").current.actual;
    console.log(strChangeSource);
    //表示ECR QQ 样式
    var ECRQQIsRequire = false;
    if ("External Changes" === strChangeSource){
        ECRQQIsRequire = true;
        //
        //禁用 影响工厂 输入框
        //清空原输入
        //外部变更   外部可填 并校验必填；内部不可填 去掉校验
        //将内部相关的属性的值置为空  并且不可编辑
        emxFormSetValue("JFChangesDeveExpensesManHours","","");
        emxFormSetFieldEditable("JFChangesDeveExpensesManHours",false);
        //必填校验去除
        const JFChangesDeveExpensesManHours =  FormHandler.GetField("JFChangesDeveExpensesManHours");
        const JFChangesDeveExpensesManHoursExternal =  FormHandler.GetField("JFChangesDeveExpensesManHoursExternal");
        JFChangesDeveExpensesManHours.HandlerField.requiredValidate = "";
        //将外部相关的属性设置为可编辑
        emxFormSetFieldEditable("JFChangesDeveExpensesManHoursExternal",true);
        if (!JFChangesDeveExpensesManHours.HandlerField.requiredValidate){
            JFChangesDeveExpensesManHoursExternal.HandlerField.requiredValidate = isZeroLength;
            const  dom = document.getElementById('calc_JFChangesDeveExpensesManHours');
            if (dom){
                dom.querySelector('td:first-child').className = "createLabel";
                dom.querySelector('td:nth-child(3)').className = "createLabelRequired";
            }
        }

    }else if ("Internal Changes" === strChangeSource){
        ECRQQIsRequire = false;
        //禁用 影响工厂 输入框
        //清空原输入
        //内部变更   内部可填 并校验必填；外部不可填 去掉校验
        //将外部相关的属性的值置为空  并且不可编辑
        emxFormSetValue("JFChangesDeveExpensesManHoursExternal","","");
        emxFormSetFieldEditable("JFChangesDeveExpensesManHoursExternal",false);
        //去掉必填校验
        const JFChangesDeveExpensesManHoursExternal =  FormHandler.GetField("JFChangesDeveExpensesManHoursExternal");
        JFChangesDeveExpensesManHoursExternal.HandlerField.requiredValidate = "";
        //内部设置为可编辑
        emxFormSetFieldEditable("JFChangesDeveExpensesManHours",true);
        const JFChangesDeveExpensesManHours =  FormHandler.GetField("JFChangesDeveExpensesManHours");
        if (!JFChangesDeveExpensesManHours.HandlerField.requiredValidate){
            JFChangesDeveExpensesManHours.HandlerField.requiredValidate = isZeroLength;
            const  dom = document.getElementById('calc_JFChangesDeveExpensesManHours');
            if (dom){
                dom.querySelector('td:first-child').className = "createLabelRequired";
                dom.querySelector('td:nth-child(3)').className = "createLabel";
            }
        }

    }else if ("Both" === strChangeSource){
        ECRQQIsRequire = true;
        emxFormSetFieldEditable("JFChangesDeveExpensesManHours",true);
        emxFormSetFieldEditable("JFChangesDeveExpensesManHoursExternal",true);
        //判断必填校验是否为空 为空就添加必填
        const JFChangesDeveExpensesManHours =  FormHandler.GetField("JFChangesDeveExpensesManHours");
        const JFChangesDeveExpensesManHoursExternal =  FormHandler.GetField("JFChangesDeveExpensesManHoursExternal");

        if (!JFChangesDeveExpensesManHours.HandlerField.requiredValidate){
            JFChangesDeveExpensesManHours.HandlerField.requiredValidate = isZeroLength;
            const  dom = document.getElementById('calc_JFChangesDeveExpensesManHours');
            if (dom){
                dom.querySelector('td:first-child').className = "createLabelRequired";
            }
        }
        if (!JFChangesDeveExpensesManHoursExternal.HandlerField.requiredValidate){
            JFChangesDeveExpensesManHoursExternal.HandlerField.requiredValidate = isZeroLength;
            const  dom = document.getElementById('calc_JFChangesDeveExpensesManHours');
            if (dom){
                dom.querySelector('td:nth-child(3)').className = "createLabelRequired";
            }
        }
    }

    //切换ECRQQ编号样式
    switchECRQQStyle(ECRQQIsRequire,null);
}

/**
 * ECR Form编辑界面
 */
function checkAffectsFactoryInputAccessInFormEdit(){
    //获取form中元素实际保存值
    var strIsPlatformPart = emxFormGetValue("JFIsPlatformPart").current.actual;
    // const affectsFactory =  FormHandler.GetField("JFAffectsFactory");
    const  domAffectsFactory = document.getElementById('calc_JFAffectsFactory');
    const  JFAffectsFactory = document.getElementsByName("JFAffectsFactory")
    // JFAffectsFactory[0].customValidate = checkInputIsNull;
    console.log(strIsPlatformPart);
    if ("No" === strIsPlatformPart||"N" === strIsPlatformPart){
        //禁用 影响工厂 输入框
        emxFormSetFieldEditable("JFAffectsFactory",false);
        //清空原输入 该API不适用于构造HTML
        //移除必填校验
        // JFAffectsFactory.requiredValidate = "";
        //移除必填样式
        if (domAffectsFactory){
            domAffectsFactory.querySelector('td:first-child').className = "label"
        }
        for (let i = 0; i < JFAffectsFactory.length; i++) {
            const option = JFAffectsFactory[i];
            if (option.checked){
                option.checked = false;
            }
        }
    }else {
        emxFormSetFieldEditable("JFAffectsFactory",true);
        //添加必填校验
        // JFAffectsFactory.requiredValidate = checkInputIsNull  ;
        //添加必填样式
        if (domAffectsFactory){
            domAffectsFactory.querySelector('td:first-child').className = "createLabelRequired"
        }
    }
}

/**
 * 校验如果变更来源为Both和外部时QQ编号和附件是必填
 */
function  checkJFQQ(){
    var strChangeSource = emxFormGetValue("JFChangeSource").current.actual;
    if (strChangeSource === "Both" || strChangeSource === "External Changes"){
        const  domJFQQ = document.getElementById('JFQQ');
        const domJFQQFileId=  document.getElementById("JFECRQQFileId");
        if (!(domJFQQ.value && domJFQQFileId.value)){
            sendMess("\u5fc5\u987b\u8f93\u5165\u6709\u6548\u503c\uff1a QQ\u7f16\u53f7\u53ca\u9644\u4ef6","Valid values must be entered: QQ ID and attachment");
        }
    }
}
function switchECRQQStyle(isRequire,cusValidate){
    const  domQQ = document.getElementById('calc_JFQQ');
    if (isRequire){
        domQQ.querySelector('td:first-child').className = "createLabelRequired";
    }else {

        domQQ.querySelector('td:first-child').className = "label";
    }
}
function checkDevAndTrialInputAccessInFormEdit(){
    //获取form中元素实际保存值
    var strChangeSource = emxFormGetValue("JFChangeSource").current.actual;
    const JFChangesDeveExpensesManHours=  document.getElementById("JFChangesDeveExpensesManHours");
    const JFChangesDeveExpensesManHoursExternal=  document.getElementById("JFChangesDeveExpensesManHoursExternal");
    var ECRQQIsRequire = false;
    if ("External Changes" === strChangeSource){
        ECRQQIsRequire = true;
        //禁用 影响工厂 输入框
        //清空原输入
        JFChangesDeveExpensesManHours.value = "";
        emxFormSetFieldEditable("JFChangesDeveExpensesManHours",false);
        //添加禁用样式
        JFChangesDeveExpensesManHours.style.backgroundColor = "#ebebe4";
        //设置readonly
        JFChangesDeveExpensesManHours.setAttribute("readonly","readonly");
        //清除readonly
        JFChangesDeveExpensesManHoursExternal.removeAttribute("readonly");
        //清除禁用样式
        JFChangesDeveExpensesManHoursExternal.style = "";
        emxFormSetFieldEditable("JFChangesDeveExpensesManHoursExternal",true);
        //去掉必填样式
        const  dom = document.getElementById('calc_JFChangesDeveExpensesManHours');
        //去掉内部必填样式
        //添加外部必填样式
        if (dom){
            dom.querySelector('td:first-child').className = "label"
            dom.querySelector('td:nth-child(3)').className = "createLabelRequired"
        }
    }else if ("Internal Changes" === strChangeSource){
        //禁用 影响工厂 输入框
        //清空原输入
        JFChangesDeveExpensesManHoursExternal.value = "";
        emxFormSetFieldEditable("JFChangesDeveExpensesManHoursExternal",false);

        //添加禁用样式
        JFChangesDeveExpensesManHoursExternal.style.backgroundColor = "#ebebe4";
        //设置readonly
        JFChangesDeveExpensesManHoursExternal.setAttribute("readonly","readonly");
        //清除readonly
        JFChangesDeveExpensesManHours.removeAttribute("readonly");
        //清除禁用样式
        JFChangesDeveExpensesManHours.style = "";
        emxFormSetFieldEditable("JFChangesDeveExpensesManHours",true);
        const  dom = document.getElementById('calc_JFChangesDeveExpensesManHours');
        if (dom){
            dom.querySelector('td:first-child').className = "createLabelRequired"
            dom.querySelector('td:nth-child(3)').className = "label"
        }
    }else if ("Both" === strChangeSource){
        ECRQQIsRequire = true;
        //清除readonly
        JFChangesDeveExpensesManHours.removeAttribute("readonly")
        //清除禁用样式
        JFChangesDeveExpensesManHours.style = "";
        //清除readonly
        JFChangesDeveExpensesManHoursExternal.removeAttribute("readonly")
        //清除禁用样式
        JFChangesDeveExpensesManHoursExternal.style = "";
        emxFormSetFieldEditable("JFChangesDeveExpensesManHours",true);
        emxFormSetFieldEditable("JFChangesDeveExpensesManHoursExternal",true);
        const  dom = document.getElementById('calc_JFChangesDeveExpensesManHours');
        if (dom){
            dom.querySelector('td:first-child').className = "createLabelRequired"
            dom.querySelector('td:nth-child(3)').className = "createLabelRequired"
        }
    }
    //切换ECRQQ编号样式
    switchECRQQStyle(ECRQQIsRequire,null);
}

function  checkInputIsNumber(){
    const regex = /^[+]?(0|[1-9]\d*)(\.\d+)?([eE][+]?\d+)?$/;
    const strInputValue = this.value;
    const strFieldTitle = this.title;
    const  strName = this.name ;
    console.log(this);
    var isRequire = false;
    var strChangeSource = emxFormGetValue("JFChangeSource").current.actual;
    //必须是必填
    if ("Both" == strChangeSource){
        isRequire = true;
    }else if ("Internal Changes" == strChangeSource){
        //内部必填
        if (strName == "JFChangesDeveExpensesManHours" || strName == "JFChangesTrialExpensesManHours"){
            isRequire = true;
        }
    }else if ("External Changes" == strChangeSource){
        //外部必填
        if (strName == "JFChangesDeveExpensesManHoursExternal" || strName == "JFChangesTrialExpensesManHoursExternal"){
            isRequire = true;
        }
    }
    if (isRequire){
        if (!strInputValue){
            var language = navigator.language || navigator.userLanguage;
            if (language.includes("zh") || language.includes("zh-CN")) {
                strMess = `\u5fc5\u987b\u8f93\u5165\u6709\u6548\u503c\uff1a ${strFieldTitle}`;
            }else {
                strMess = `A valid value must be entered: ${strFieldTitle}`;
            }
            alert(strMess);
            return false;
        }
    }
    //可以为空但是如果有值必须是数值
     if (strInputValue && !regex.test(strInputValue)){
         //拿到浏览器语言
         var language = navigator.language || navigator.userLanguage;

         if (language.includes("zh") || language.includes("zh-CN")) {
             strMess = `\u5fc5\u987b\u8f93\u5165\u6709\u6548\u7684\u6570\u503c\uff1a${strFieldTitle} : \u5f53\u524d\u7684\u5c0f\u6570\u70b9\u7b26\u53f7\u4e3a ' . '`;
         }else {
             strMess = `A valid numerical value must be entered: ${strFieldTitle}: The current decimal point symbol is'. '`;
         }
         alert(strMess);
         return false;
     }
     return true;
}



function checkInputIsNull (){
    const strInputValue = this.value;
    const strFieldTitle = this.title;
    var strIsPlatformPart = emxFormGetValue("JFIsPlatformPart").current.actual;
    //是否平台间为 否直接通过
    if (strIsPlatformPart == "No"||strIsPlatformPart == "N"){
        return true;
    }
    const  options = FormHandler.GetField("JFAffectsFactory").HandlerField;
    let isAlert = false;
    for (let i = 0; i < options.length; i++) {
        const option = options[i];
        console.log("option.checked:{}",option.checked)
        if (option.checked){
            isAlert = true;
            break
        }
    }
    if (!isAlert){
        //拿到浏览器语言
        var language = navigator.language || navigator.userLanguage;

        if (language.includes("zh") || language.includes("zh-CN")) {
            strMess = `\u5fc5\u987b\u8f93\u5165\u6709\u6548\u503c\uff1a \u5f71\u54cd\u5de5\u5382`;
        }else {
            strMess = `A valid value must be entered: Affects the Factory`;
        }
        alert(strMess);
        return false;
    }
    return true;
}

function checkInputIsNullEdit (){
    console.log("--checkInputIsNullEdit-->")
    console.log("--checkInputIsNullEdit-->")
    console.log("--checkInputIsNullEdit-->")
    const strInputValue = this.value;
    const strFieldTitle = this.title;
    var strIsPlatformPart = emxFormGetValue("JFIsPlatformPart").current.actual;
    //是否平台间为 否直接通过
    if (strIsPlatformPart == "No"||strIsPlatformPart == "N"){
        return true;
    }
    const  options = document.getElementsByName("JFAffectsFactory");
    let isAlert = false;
    for (let i = 0; i < options.length; i++) {
        const option = options[i];
        console.log("option.checked:{}",option.checked)
        if (option.checked){
            isAlert = true;
            break
        }
    }
    if (!isAlert){
        //拿到浏览器语言
        var language = navigator.language || navigator.userLanguage;

        if (language.includes("zh") || language.includes("zh-CN")) {
            strMess = `\u5fc5\u987b\u8f93\u5165\u6709\u6548\u503c\uff1a \u5f71\u54cd\u5de5\u5382`;
        }else {
            strMess = `A valid value must be entered: Affects the Factory`;
        }
        alert(strMess);
        return false;
    }
    return true;
}
function checkAffectedProject(){
    //获取form中元素实际保存值
    var strIsAssociatedOtherProject = emxFormGetValue("JFAssociatedOtherProjects").current.actual;
    const  domAffectsFactory = document.getElementById('calc_JFAffectedProject');
    console.log(strIsAssociatedOtherProject);
    var strClassName = "label";
    if ("Y" === strIsAssociatedOtherProject){
        strClassName = "createLabelRequired" ;
    }
    if (domAffectsFactory){
        domAffectsFactory.querySelector('td:first-child').className = strClassName;
    }
}

/**
 * 发送提示消息
 */
function sendMess(strCNMess,strENMess){
    var language = navigator.language || navigator.userLanguage;
    var strMess = "";
    if (language.includes("zh") || language.includes("zh-CN")) {
        strMess = strCNMess;
    }else {
        strMess = strENMess;
    }
    alert(strMess);
}

function ECOShowModalDialog(event,strUrl){
    const  strMode = this.editableTable.mode ;
    //判判断表格是否处于编辑模式，不处于需要手动设置为编辑模式
    if (strMode != "edit"){
        document.getElementById("editButttonId").click();
    }
    var currentDom = event.target ;
    console.log(currentDom);
    let td = $(currentDom).parents('td');
    console.log("td",td)
    let id = $(td[0]).attr("rmbrow");
    strUrl+="&rowId=";
    strUrl+=id;
    showModalDialog(strUrl);
}


function PCRShowModalDialog(event,strUrl){
    // const  strMode = this.editableTable.mode ;
    // //判判断表格是否处于编辑模式，不处于需要手动设置为编辑模式
    // if (strMode != "edit"){
    //     document.getElementById("editButttonId").click();
    // }
    var currentDom = event.target ;
    console.log(currentDom);
    let td = $(currentDom).parents('td');
    console.log("td",td)
    let id = $(td[0]).attr("rmbrow");
    strUrl+="&rowId=";
    strUrl+=id;
    showModalDialog(strUrl);
}


//add buy ljr  20241126
function JFCheckColorCode(newValue) {
    console.log("value:", newValue);
    var value = -1;
    //table校验时使用this.value
    //拿到浏览器语言
    var language = navigator.language || navigator.userLanguage;
    // 使用正则表达式匹配
    var strMess = "";
    var regex = /^[A-Z0-9]{3}$/;
    if (!newValue) {
        value = this.value;
    } else {
        value = newValue;
    }
    if (language.includes("zh") || language.includes("zh-CN")) {
        strMess = "\u989c\u8272\u4ee3\u7801\u4ec5\u652f\u6301A,B,C...\u4ee5\u53ca1\u30012\u3001...0, \u603b\u957f\u5ea6\u53ea\u5141\u8bb83\u4e2a\u5b57\u7b26!";
    } else {
        strMess = "Color codes only support A, B, C... and 1、 2、... 0; The total length only allows 3 characters!";
    }
    console.log("value:", value);
    if (regex.test(value)) {
        return true;
    } else {
        alert(strMess);
        return false;
    }
}

/**
 * 校验颜色分组中颜色分组编码在颜色矩阵中是否唯一 且必须为两位英文大写
 * @param newValue
 */
function checkColorGroupNameIsUniqueOnColorMatrix(newValue){
    let strGroupName ;

    if (newValue){
        strGroupName = newValue;
    }else {
        const strTitle = emxFormGetValue("Title").current.actual;
        strGroupName = strTitle;
    }
    if (strGroupName){
        const regex = /^[A-Z]{2}$/;
        checkRes = regex.test(strGroupName);
        var language = navigator.language || navigator.userLanguage;
        if (!checkRes){
            if (language.includes("zh") || language.includes("zh-CN")) {
                strMess = "\u5206\u7ec4\u7f16\u7801\u5fc5\u987b\u4e3a\u4e24\u4f4d\u82f1\u6587\u5927\u5199\uff0c\u8bf7\u91cd\u65b0\u8f93\u5165\uff01";
            } else {
                strMess = "The grouping code must be two uppercase English characters, please re-enter!";
            }
            alert(strMess);
            return checkRes;
        }
        // 判断颜色分组编码的填写范围只能是在UB-ZZ分范围  update by ljr 20260601 HA-ZZ
        const codeNum = strGroupName.charCodeAt(0) * 100 + strGroupName.charCodeAt(1);
        check = codeNum >= ('H'.charCodeAt(0) * 100 + 'A'.charCodeAt(0)) &&
            codeNum <= ('Z'.charCodeAt(0) * 100 + 'Z'.charCodeAt(0))
        if (!check) {
            if (language.includes("zh") || language.includes("zh-CN")) {
                mess = "\u989c\u8272\u5206\u7ec4\u7f16\u7801\u7684\u586b\u5199\u8303\u56f4\u53ea\u80fd\u662f\u5728HA-ZZ\u5206\u8303\u56f4\uff01!";
            } else {
                mess = "The range for filling in color group coding can only be within the HA-ZZ sub range!";
            }
            alert(mess);
            return check;
        }
        const urlParameters = location.search.substr(1);
        const parameters = new URLSearchParams(urlParameters);
        //ecr Id
        const strObjectId = parameters.get("objectId");
        const args = { objectId: strObjectId, groupNumber: strGroupName}
         $.ajax({
            async :false ,
            url: '../TWXPublicRest/TWXTicketService',
            type: 'GET',
            data: {
                JPOName : "JF_ECRRESTService",
                FuncName : "checkColorGroupNameIsUniqueOnColorMatrix",
                Params : JSON.stringify(args)
            },
            success: function(res) {
                if (res.code == "200"){
                    checkRes =  true;
                }else {
                    alert(res.mess);
                    checkRes = false;
                }

            },
            error: function(res) {
                checkRes = false ;
            },
            timeout: 3000 // 设置超时时间为3秒
        });

    }else {
        checkRes = true;
    }
    return checkRes ;
}

/**
 *
 * 校验表格中客户内部颜色编码
 * @param newValue
 * @returns {boolean}
 */
function checkJFInternalColorCode(newValue){
    let checkFlag = true ;
    let checkFlagForm = true ;
    //正则校验 输入必须为三位数且只能为字母数字！
    const regex = /^[a-zA-Z0-9]{3}$/;
    if (newValue){
        checkFlag = regex.test(newValue);
    }else {
        checkFlagForm = regex.test(document.getElementById("JF_InternalColorCode").value );
    }
    console.log("checkFlag::",checkFlag)
    console.log("checkFlagForm::",checkFlagForm)
    if (!checkFlag){
        //拿到浏览器语言
        const language = navigator.language || navigator.userLanguage;
        // 使用正则表达式匹配
        let strMess = "";
        if (language.includes("zh") || language.includes("zh-CN")) {
            //非法输入！请输入三位字符，字符只能是字母或数字！
            strMess = "\u975e\u6cd5\u8f93\u5165\uff01\u8bf7\u8f93\u5165\u4e09\u4f4d\u5b57\u7b26\uff0c\u5b57\u7b26\u53ea\u80fd\u662f\u5b57\u6bcd\u6216\u6570\u5b57\uff01";
        } else {
            strMess = "Illegal input! Please enter three characters, characters can only be letters or numbers!";
        }
        alert(strMess)

    }
    //form表单界面校验
    if (!checkFlagForm){
        //拿到浏览器语言
        const language = navigator.language || navigator.userLanguage;
        // 使用正则表达式匹配
        let strMess = "";
        if (language.includes("zh") || language.includes("zh-CN")) {
            //整椅内部颜色码输入有误！请输入三位字符，字符只能是字母或数字！
            strMess = "\u6574\u6905\u5185\u90e8\u989c\u8272\u7801\u8f93\u5165\u6709\u8bef\uff01\u8bf7\u8f93\u5165\u4e09\u4f4d\u5b57\u7b26\uff0c\u5b57\u7b26\u53ea\u80fd\u662f\u5b57\u6bcd\u6216\u6570\u5b57\uff01";
        } else {
            strMess = "Internal ColorCode Illegal input! Please enter three characters, characters can only be letters or numbers!";
        }
        alert(strMess)
    }
    if (newValue){
        return checkFlag;
    }else {
        return checkFlagForm;
    }
}

/**
 * 添加参考项目
 */
function addCostReferConst(){
    const  checkRes = checkSelectSingle();
    const  table = this.emxEditableTable;
    if (checkRes){
        const  selectRow = table.getCheckedRows();
        const  row = selectRow[0];
        const  strType = row.getAttribute("type");
        const  strOid = row.getAttribute("o");
        const  strLevel = row.getAttribute("id");
        let selectId = `${strOid}`;
        if ("JFModules" == strType){
            let urlParameters = location.search.substr(1);
            let parameters = new URLSearchParams(urlParameters);
            let objectId = parameters.get("objectId");
            let parentOID = parameters.get("parentOID");
            // let searchUrl = "../common/emxFullSearch.jsp?field=TYPES=type_JFCost&showInitialResults=true&table=AEFGeneralSearchResults&selection=multiple";
            searchUrl = "../common/emxIndentedTable.jsp?table=AEFGeneralSearchResults&program=JF_Cost:getCostTemplate";
            searchUrl+="&selection=multiple&sortColumnName=Name&sortDirection=ascending";
            searchUrl+="&header=emxFramework.Common.SelectReferenceProject";
            searchUrl+="&submitLabel=emxFramework.Common.ok&cancelButton=emxFramework.Button.Cancel&cancelLabel=emxFramework.Command.JFCostQuotationCancel";
            searchUrl+="&submitAction=refreshCaller";
            searchUrl += "&cancelLabel=emxFramework.Common.Close";
            searchUrl += "&submitAction=refreshCaller";
            searchUrl += "&HelpMarker=emxhelpfullsearch";
            searchUrl += "&submitURL=../common/JF_CostProcess.jsp?";
            searchUrl += "&rel=" + "relationship_JFModule2ReferConst";
            searchUrl += "&Type=" + "type_JFCostReferConst";
            searchUrl += "&policy=" + "type_JFCostReferConst";
            searchUrl += "&objectId=" + objectId;
            searchUrl += "&parentOID=" + parentOID;
            searchUrl += "&selectId=" + selectId;
            showModalDialog(searchUrl);
        }else {
            const language = navigator.language || navigator.userLanguage;
            // 使用正则表达式匹配
            let strMess = "";
            if (language.includes("zh") || language.includes("zh-CN")) {
                //非法输入！请输入三位字符，字符只能是字母或数字，且首字母不能为数字！
                strMess = "\u8bf7\u9009\u62e9\u62a5\u4ef7\u6574\u6905\uff01";
            } else {
                strMess = "Please choose a quote for the entire chair!";
            }
            alert(strMess)
            return;
        }
    }

}

/**
 *
 * 校验只能选择一个
 */
function checkSelectSingle(){
    let checkRes = true ;
    const  table = this.emxEditableTable;
    if (table){
        const  selectRows = table.getCheckedRows().length;
        if (selectRows > 1){
            checkRes = false;
            const language = navigator.language || navigator.userLanguage;
            let strMess = "";
            if (language.includes("zh") || language.includes("zh-CN")) {
                strMess = "\u53ea\u80fd\u9009\u62e9\u4e00\u9879\uff01";
            } else {
                strMess = "Only one option can be selected!";
            }
            alert(strMess)
        }
    }
    return checkRes;
}

/**
 * 打开对象编辑框
 * @param strObjId 对象id
 * @param isEditFlag 编辑和视图模式
 */
function openEditObj(strObjId,isEditFlag){
    let strMode = "view";
    if (isEditFlag == "1"){
        isEditFlag = "edit"
    }
    getTopWindow().showSlideInDialog(`../common/emxForm.jsp?form=JFFunctionModuleEditForm&mode=${isEditFlag}&submitAction=doNothing&objectId=${strObjId}&postProcessURL=../common/JF_RefreshJFFunctionModuleProcess.jsp`, null, null, "right", 450);
}


/**
 * 打开对象编辑框
 * @param strObjId 对象id
 * @param isEditFlag 编辑和视图模式
 */
function openCostAnalysisEditObj(strObjId,isEditFlag,strParentId){
    let strMode = "view";
    if (isEditFlag == "1"){
        isEditFlag = "edit"
    }
    //refreshCaller
    getTopWindow().showSlideInDialog(`../common/emxForm.jsp?form=JFCostAnalysisEditForm&mode=${isEditFlag}&submitAction=doNothing&objectId=${strObjId}&partId=${strParentId}&postProcessURL=../common/JF_CloseSlidein.jsp&isForm=true`, null, null, "right", 450);
}

function delColorGroup(){

    const  checkRes = checkSelectSingle();
    const  table = this.emxEditableTable;
    if (checkRes){
        const  selectRow = table.getCheckedRows();
        const  row = selectRow[0];
        const  strOid = row.getAttribute("o");
        const  strRelId = row.getAttribute("r");
        const  strParentId = row.getAttribute("p");
        const  strId = row.getAttribute("id");
        const groupCodeObj = this.emxEditableTable.getCellValueByRowId(strId,"Title");
        if (groupCodeObj){
            const  strGroupCode = groupCodeObj.value.current.actual;
            if ("NA" == strGroupCode || "UA" == strGroupCode){
                const language = navigator.language || navigator.userLanguage;
                let strMess = "";
                if (language.includes("zh") || language.includes("zh-CN")) {
                    strMess = "\u989c\u8272\u5206\u7ec4\uff1a\u5ea7\u6905\u3001\u65e0\u8272\u4ef6\u4e0d\u5141\u8bb8\u88ab\u5220\u9664\uff01";
                } else {
                    strMess = "Color grouping: Seats and colorless parts are not allowed to be deleted!";
                }
                alert(strMess);
                return;
            }
        }
        const args = { objectId: strOid}
        $.ajax({
            async :false ,
            url: '../TWXPublicRest/TWXTicketService',
            type: 'GET',
            data: {
                JPOName : "JF_ECRRESTService",
                FuncName : "checkColorGroupNameHasQuoteBOM",
                Params : JSON.stringify(args)
            },
            success: function(res) {
                //无色件和座椅不允许被删除
                if ("404" == res.code){
                    alert(res.mess);
                    return;
                }
                const isConfirm = confirm(res.mess);
                if (isConfirm){
                    $.ajax({
                        async :false ,
                        url: '../TWXPublicRest/TWXTicketService',
                        type: 'GET',
                        data: {
                            JPOName : "JF_ECRRESTService",
                            FuncName : "deleteColorGroupByGroupId",
                            Params : JSON.stringify(args)
                        },
                        success: function(res) {
                            alert(res.mess);
                            if (res.code == "200"){
                                window.emxEditableTable.removeRowsSelected([`${strParentId}|${strOid}|${strParentId}|${strId}`]);
                            }
                        },
                        error: function(res) {
                            alert("删除失败，请联系管理员！")
                        },
                        timeout: 3000 // 设置超时时间为3秒
                    });
                }

            },
            error: function(res) {
                alert("校验失败，请联系管理员！")
            },
            timeout: 3000 // 设置超时时间为3秒
        });
    }
}

function checkColorStyleTitleIsUniqueOnColorMatrix(newValue){
    let strGroupName ;

    if (newValue){
        strGroupName = newValue;
    }else {
        const strTitle = emxFormGetValue("Title").current.actual;
        strGroupName = strTitle;
    }
    let  checkRes ;
    if (strGroupName){
        const urlParameters = location.search.substr(1);
        const parameters = new URLSearchParams(urlParameters);
//ecr Id
        const strObjectId = parameters.get("objectId");
        const args = { objectId: strObjectId, styleName: strGroupName}
        $.ajax({
            async :false ,
            url: '../TWXPublicRest/TWXTicketService',
            type: 'GET',
            data: {
                JPOName : "JF_ECRRESTService",
                FuncName : "checkColorStyleTitleIsUniqueOnColorMatrix",
                Params : JSON.stringify(args)
            },
            success: function(res) {
                if (res.code == "200"){
                    checkRes =  true;
                }else {
                    alert(res.mess);
                    checkRes = false;
                }

            },
            error: function(res) {
                checkRes = false ;
            },
            timeout: 3000 // 设置超时时间为3秒
        });

    }else {
        checkRes = true;
    }
    return checkRes ;
}

/**
 * 校验输入是否为非负实数
 * @param newValue
 * @returns {boolean}
 */
function isNonNegativeRealNumber(newValue){
    let strGroupName ;
    let checkRes = false;
    if (newValue){
        // 正实数或0的正则表达式：可以是整数或小数，包括0和0.0
        const regex = /^\s*(0(\.\d+)?|([1-9]\d*(\.\d+)?))\s*$/;
        checkRes  = regex.test(newValue);
    }
    if (!checkRes){
        const language = navigator.language || navigator.userLanguage;
        let strMess = "";
        if (language.includes("zh") || language.includes("zh-CN")) {
            strMess = "\u8f93\u5165\u9519\u8bef\uff01\u8bf7\u8f93\u5165\u5927\u4e8e\u7b49\u4e8e0\u7684\u6570";
        } else {
            strMess = "Input error! Please enter a number greater than or equal to 0!";
        }
        alert(strMess);
    }
    return checkRes ;
}

/**
 * 当报价用量变动是计算报价价格
 * add by chenyan 2025/03/23 功能选项和报价选项不一致时,报价用量修改,评估价格不联动
 */
function calcOfferOptionPrice(newValue){
    const currCell = this.emxEditableTable.getCurrentCell();
    const actualValue = this.emxEditableTable.getCellValueByRowId(currCell.rowID,"JF_ReferPrice").value.current.actual;
    const strFunctionOption = this.emxEditableTable.getCellValueByRowId(currCell.rowID,"JF_FuntionOption").value.current.actual;
    const strOfficeOption = this.emxEditableTable.getCellValueByRowId(currCell.rowID,"JF_OfferOption").value.current.actual;
    if (strFunctionOption == strOfficeOption){
        // 使用 Number() 转换字符串为数字
        let iNewValue = Number(newValue);
        let iActualValue = Number(actualValue);
        let result = iNewValue * iActualValue;
        const  strResult = result.toFixed(2);
        //联动评估价格
        this.emxEditableTable.setCellValueByRowId(currCell.rowID,"JF_EvaluatePrice",strResult,strResult,true);
    }
}
function checkSubmitLibTable(){
    console.log(this);
    const  table = this.emxEditableTable;
    if (table) {
        const selectRows = table.getCheckedRows();
        const selectRowsLen = selectRows.length;
        console.log(selectRows)
        if (selectRowsLen <= 0){
            if (language.includes("zh") || language.includes("zh-CN")) {
                strMess = "\u8bf7\u9009\u62e9\u5206\u7c7b\u5e93!";
            } else {
                strMess = "Please select a classification library!";
            }
            alert(strMess);
            return;
        }
        const  rowInfoArr = new Array();
        //重新构造选中行
        for (let i = 0; i < selectRows.length; i++) {
            const  row = selectRows[i];
            const  strOid = row.getAttribute("o");
            const  strId = row.getAttribute("id");
            const  strLevel = row.getAttribute("level");
            const rowInfo = {};
            rowInfo.objectId = strOid;
            rowInfo.id = strId;
            if (strLevel == "0"){
                rowInfo.isOneLevelClass = "1";
            }else {
                rowInfo.isOneLevelClass = "0";
            }
            rowInfoArr.push(rowInfo);
        }
        //过滤选中行
        for (let i = 0; i < selectRows.length; i++) {
            const  row = selectRows[i];
            const  strId = row.getAttribute("id");
            const  strLevel = row.getAttribute("level");
            //根节点不需要
            if (strLevel != "0"){
                let parentIdArr = getSpecificParents(strId,2);
                //不是本身且有父存在选中里面则移除该节点
                if (parentIdArr[0] != strId) {
                    if (parentIdArr.some(strParentId => rowInfoArr.some(rowObj => rowObj["id"] == strParentId))) {
                        const index = rowInfoArr.findIndex(obj => obj.id === strId);
                        if (index !== -1) {
                            rowInfoArr.splice(index, 1);
                        }
                    }
                }
            }
        }
        const urlParameters = location.search.substr(1);
        const parameters = new URLSearchParams(urlParameters);
        const strSelectIds = parameters.get("selectIds");
        const refresh = parameters.get("refresh");  //add  by ljr
        const args = { selectPartList: strSelectIds, selectLibList: rowInfoArr}
        $.ajax({
            async :false ,
            url: '../TWXPublicRest/TWXTicketService?JPOName=JF_ECRRESTService&FuncName=createCostAnalysisByBOM',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(args),
            success: function(res) {
                alert(res.mess);
                if (res.code == "200"){
                    //关闭窗口
                    getTopWindow().close();
                    //add by ljr ebom界面不需要刷新  菜单成本分析需要刷新
                    if ("true" === refresh) {
                        //需要刷新
                        window.top.getWindowOpener().top.refreshTablePage();
                        // getTopWindow().openerFindFrame(getTopWindow(),"detailsDisplay").emxEditableTable.refreshSelectedRows();
                    }
                }
            },
            error: function(res) {
            },
            timeout: 3000 // 设置超时时间为3秒
        });
    }
}

/**
 * 获取指定层级id 和起始父lvel 之间的所有父级
 * @param hierarchyStr
 * @param parentLevel
 * @returns {*[]}
 */
function getSpecificParents(hierarchyStr, parentLevel) {
    // 将字符串按逗号分割并转换成数组
    const levels = hierarchyStr.split(',');
    const totalLevels = levels.length;

    // 检查无效的 parentLevel
    if (parentLevel < 0 || parentLevel >= totalLevels) {
        return [hierarchyStr];
    }

    const parents = [];

    // 从指定的父级层数开始，逐级添加到最底层
    for (let i = parentLevel; i < totalLevels; i++) {
        const parent = levels.slice(0, i).join(',');
        parents.push(parent);
    }
    return parents;
}

function costQuotationSaveAndFlush(){
    console.log(this);
    if (this.emxEditableTable.checkDataModified()){
        this.applyEdits();
    }
    // todo 怎么判断已经完成计算
    setTimeout(async () =>{

    },500);
}


/**
 * 计算评估价格
 */
function calcEvaluatePrice(){
    //获取form表单的属性
    const formFiled = FormHandler.Fields._container;
    const argsJson = {objectId :getParameters("objectId")} ;
    const arrFields = new Array();
    argsJson.arrFields = arrFields ;
    let isValid = true; // 用于标记表单是否有效
    const requiredFields = ['JF_FucFoamCostPerMold', 'JF_FucFoamNumberMoldFrames','JF_FucFoamUnitPricePerKilogram','JF_FucFoamTotalWeight','JF_FucComPlasticNumber','JF_FucComPlasticWeight','JF_FucComPlasticAverageUnitPrice','JF_FucComPlasticCompensationCoefficient','JF_FucOthMetalWeight','JF_FucOthMetalUnitPricePerKilogram','JF_FucCoverUnitPrice','JF_FucCoverUnitPriceLabor','JF_FucCoverNetarea','JF_FucCoverUtilization','JF_FucSewingHours','JF_FucCoverPatternNB','JF_FucCoverAxuPrices','JF_FucHarnessLoopsNB','JF_FucHarnessLoopUnitPrice' ]; // 必填字段的名称数组
    for (let field in formFiled) {
        if (formFiled.hasOwnProperty(field)) { // 确保只遍历对象自身的属性
            const fieldJson = {} ;
            const fieldObj = formFiled[field];
            const  fieldType = fieldObj.FieldType;
            const  fieldName = fieldObj.Name;
            // 校验必填项
            if (requiredFields.includes(fieldName)) {
                const fileValueObj = FormHandler.GetFieldValue(field);
                const actualValue = fileValueObj.current.actual;

                // 创建一个错误提示的 div
                const errorMessage = document.createElement('div');
                errorMessage.innerText = `是必填项且为实数`;
                errorMessage.style.color = 'red'; // 设置提示颜色
                // 获取输入框元素
                const inputElement = document.getElementById(field);
                if (inputElement) {
                    // 获取输入框的父级元素
                    const parentElement = inputElement.parentNode;
                    // 校验输入值
                    if (!actualValue || actualValue.trim() === "") {
                        isValid = false; // 标记为无效
                        // 在父级元素下添加提示信息
                        parentElement.appendChild(errorMessage);
                        // 高亮边框
                        inputElement.style.border = '1px solid red';
                        // 添加输入事件监听器
                        inputElement.addEventListener('input', function() {
                            const currentValue = inputElement.value;
                            if (currentValue && currentValue.trim() !== "") {
                                // 移除错误提示
                                if (parentElement.contains(errorMessage)) {
                                    parentElement.removeChild(errorMessage);
                                }
                                // 重置边框样式
                                inputElement.style.border = ''; // 或者设置为默认边框
                            }
                        });
                    } else {
                        const isNumber = /^-?\d+(\.\d+)?$/.test(actualValue.trim());
                        if (!isNumber){
                            isValid = false; // 标记为无效
                            // 在父级元素下添加提示信息
                            parentElement.appendChild(errorMessage);
                            // 高亮边框
                            inputElement.style.border = '1px solid red';
                            // 添加输入事件监听器
                            inputElement.addEventListener('input', function() {
                                const currentValue = inputElement.value;
                                if (currentValue && currentValue.trim() !== "") {
                                    // 移除错误提示
                                    if (parentElement.contains(errorMessage)) {
                                        parentElement.removeChild(errorMessage);
                                    }
                                    // 重置边框样式
                                    inputElement.style.border = ''; // 或者设置为默认边框
                                }
                            });
                        }else{
                            // 如果有有效值，移除错误提示（如果存在）
                            if (parentElement.contains(errorMessage)) {
                                parentElement.removeChild(errorMessage);
                            }
                            // 重置边框样式
                            inputElement.style.border = '';
                        }
                    }
                } else {
                    console.error(`Element with ID ${field} not found.`);
                }
            }
            //html单独处理
            if ( "attribute" === fieldType || "program" === fieldType){
                const  domField = fieldObj.ActualField;
                //是否可以编辑
                const  canEdit = fieldObj.CanEdit();
                const  strFieldType = fieldObj.FieldType;
                fieldJson.canEdit = canEdit ;
                fieldJson.fieldName = field ;
                if (domField){
                    //获取属性值
                    const fileValueObj = FormHandler.GetFieldValue(field);
                    const actualValue = fileValueObj.current.actual;
                    console.log("fieldName:",fieldName,"actualValue:",actualValue);
                    fieldJson.fieldValue = actualValue ;
                }
                // todo 获取不可编辑属性值
                if (!canEdit){
                    const  calcFileName = "calc_"+fieldName;
                    const  domCalcField = document.getElementById(calcFileName);
                    const notEditValue = domCalcField.getElementsByClassName ("inputField")[0].innerText ;
                    fieldJson.fieldValue = notEditValue ;
                }
                //获取属性的interface
                const strInterfaceName = fieldObj.GetSettingValue("interfaceName");
                fieldJson.interfaceName = strInterfaceName ;
                arrFields.push(fieldJson);
            }else {
                // if ( "JF_EvaluatePrice" == fieldName){
                //     document.getElementById("JF_EvaluatePrice");
                //     console.log();
                // }
            }
        }
    }
    if (!isValid) {
        alert('请填写必填项或者填写实数用于计算评估价格')
        console.log("表单校验未通过，请检查必填项");
        return; // 退出函数
    }
    $.ajax({
        url: '../TWXPublicRest/TWXTicketService?JPOName=JF_ECRRESTService&FuncName=calcEvaluatePriceProcessNewFunction',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(argsJson),
        success: function(res) {
            // alert(res.mess);
            if (res.code == "200"){
                if (res.calcAttrMapList){
                    for (let i = 0; i < res.calcAttrMapList.length; i++) {
                        const calcAttr = res.calcAttrMapList[i];
                        if (calcAttr){
                            //找到对应的field对象
                            const fieldObj = formFiled[calcAttr.attrName];
                            if (fieldObj){
                                const  fieldType = fieldObj.FieldType;
                                const strCalcValue = calcAttr.calcValue;
                                if ( "attribute" == fieldType){
                                    FormHandler.SetFieldValue(field.Name,strCalcValue,strCalcValue);
                                }else if (calcAttr.attrName == "JF_EvaluatePrice"){
                                    const domEvaluatePrice = document.getElementById("JF_EvaluatePrice");
                                    const JF_EvaluatePricefieldValue = document.getElementById("JF_EvaluatePricefieldValue");
                                    //显示结果
                                    if (strCalcValue){
                                        domEvaluatePrice.value = strCalcValue ;
                                        JF_EvaluatePricefieldValue.value = strCalcValue ;
                                    }
                                }
                            }

                        }
                        for (let calcAttrKey in calcAttr) {
                            if ("JF_EvaluatePrice" == calcAttrKey){
                                //关闭窗口
                            }
                        }
                    }
                }

                // todo 刷新表格
            }
        },
        error: function(res) {
        },
        timeout: 3000 // 设置超时时间为3秒
    });
}
function getParameters(strKey){
    const urlParameters = location.search.substr(1);
    const parameters = new URLSearchParams(urlParameters);
    const strKeyValue = parameters.get(strKey);
    return strKeyValue ;
}

function reloadSubTypeRange(){
    if(null !=emxEditableTable && "null" != emxEditableTable){
        const strRowId = emxEditableTable.getCurrentCell().rowID;
        emxEditableTable.reloadCell("JF_PartSubType",strRowId);
    }
    return true;
}

/**
 * 根据零件类型设置零件子类型是否可编辑
 * @returns {boolean}
 */
function switchEditBySelectPartType_old(){
    if(null !=emxEditableTable && "null" != emxEditableTable){
        const  strPartType = emxEditableTable.getCurrentCell().value.current.actual;
        const strRowId = emxEditableTable.getCurrentCell().rowID;
        //清空原来值
        emxEditableTable.setCellValueByRowId(strRowId,"JF_PartSubType","","",true);
        if ("T" == strPartType || "E" == strPartType){
            //设置为编辑模式
            emxEditableTable.setCellEditableByRowId(strRowId,"JF_PartSubType",true,true);
        }else {
            //设置为不可编辑
            emxEditableTable.setCellEditableByRowId(strRowId,"JF_PartSubType",false,true);
        }
    }
    return true;
}


function switchEditBySelectPartType(newVal, rowId, attrName) {
    /*var targetWindow = null;
    targetWindow = window;
    if (targetWindow) {
        targetWindow = findFrame(parent, "PMCWBS");
    }
    if(targetWindow==null){
        targetWindow =window.frames;
    }*/
    var strPartType = getValueForCell(rowId, "PartType", "actual");
    //清空原来值
    emxEditableTable.setCellValueByRowId(rowId,"JF_PartSubType","","",true);
    if ("T" == strPartType || "E" == strPartType){
        //设置为编辑模式
        emxEditableTable.setCellEditableByRowId(rowId,"JF_PartSubType",true,true);
    }else {
        //设置为不可编辑
        emxEditableTable.setCellEditableByRowId(rowId,"JF_PartSubType",false,true);
    }
    return true;

}

function getValueForCell(rowId,cellName,valueType) {
    var cellContent    = emxEditableTable.getCellValueByRowId(rowId,cellName);
    return cellContent ?  valueType == "actual" ? cellContent.value.current.actual : cellContent.value.current.display : "";
}

function switchEditBySelectDoase(){
    if(null !=emxEditableTable && "null" != emxEditableTable){
        const  strPartType = emxEditableTable.getCurrentCell().value.current.actual;
        const strRowId = emxEditableTable.getCurrentCell().rowID;
        const relid = emxEditableTable.getCurrentCell().relid;
        //清空原来值
        if ("PC" == strPartType && relid!='' ){
            //设置为不可编辑
            emxEditableTable.setCellEditableByRowId(strRowId,"Dosage",false,true);
            emxEditableTable.setCellValueByRowId(strRowId,"Dosage","1","1",true);
        }else if (relid==''){
            emxEditableTable.setCellEditableByRowId(strRowId,"Dosage",false,true);
        }else {
            //设置为编辑模式
            emxEditableTable.setCellEditableByRowId(strRowId,"Dosage",true,true);
        }
    }
    return true;
}

/**
 * 校验输入是否是实数
 */
function  validateInputIsRealNumber(newValue) {
    if (newValue){
        // 正实数或0的正则表达式：可以是整数或小数，包括0和0.0
        const regex = /^[+-]?\d+(\.\d+)?$/;
        checkRes  = regex.test(newValue);
    }

    if (!checkRes){
        const language = navigator.language || navigator.userLanguage;
        let strMess = "";
        if (language.includes("zh") || language.includes("zh-CN")) {
            strMess = "\u8f93\u5165\u9519\u8bef\uff01\u8bf7\u8f93\u5165\u5b9e\u6570";
        } else {
            strMess = "Input error! Please enter a  real number";
        }
        alert(strMess);
    }
    return checkRes ;
}

/**
 * 校验Form输入是否是实数
 * @returns {boolean}
 */
function validateFormInputIsRealNumber() {
    const newValue = this.value;
    if (!newValue) {
        return true;
    }
    return validateInputIsRealNumber(newValue);
}

/**
 * 校验成本核算实际成本
 * @returns {*|boolean|boolean}
 */
function validateInputCostAnalysis(newValue){
    const strHref = location.href;
    const urlParameters = location.search.substr(1);
    const parameters = new URLSearchParams(urlParameters);
    let checkRes = false;
    // add by chenyan 优化成本核算逻辑
    if (strHref.includes("/emxIndentedTable.jsp")) {
        const  strTableName = parameters.get("table");
        if (strTableName.includes("JFBOMCostAnalysisEditTable")){
            if (!newValue){
                return true;
            }
        }
    } else if (strHref.includes("/emxForm.jsp")){
        const  strFormName = parameters.get("form");
        if ( "JFCostAnalysisEditForm" == strFormName){
            newValue = emxFormGetValue("JFActualcCost").current.actual;
            if (!newValue){
                return true;
            }
        }

    }
    return validateInputIsRealNumber(newValue) ;
}
function calcJF_assemblyManufacturingPrice(newValue){
    const currCell = this.emxEditableTable.getCurrentCell();
    const JF_assemblyTimePrice = this.emxEditableTable.getCellValueByRowId(currCell.rowID,"JF_assemblyTimePrice").value.current.actual;
    const JF_AssemblyPriceUnitPrice = this.emxEditableTable.getCellValueByRowId(currCell.rowID,"JF_AssemblyPriceUnitPrice").value.current.actual;
    console.log('calcJF_assemblyManufacturingPrice::JF_assemblyTimePrice:',JF_assemblyTimePrice)
    console.log('calcJF_assemblyManufacturingPrice::JF_AssemblyPriceUnitPrice:',JF_AssemblyPriceUnitPrice)
        let iNewValue = Number(JF_assemblyTimePrice);
        let iActualValue = Number(JF_AssemblyPriceUnitPrice);
        let result = iNewValue * iActualValue;
        const  strResult = result.toFixed(2);
        //联动价格
        this.emxEditableTable.setCellValueByRowId(currCell.rowID,"JF_assemblyManufacturingPrice",strResult,strResult,true);
}

function calcJF_FoamManufacturingPrice(newValue){
    const currCell = this.emxEditableTable.getCurrentCell();
    const JF_FoamModesNumber = this.emxEditableTable.getCellValueByRowId(currCell.rowID,"JF_FoamModesNumber").value.current.actual;
    const JF_FoamingUnitPrice = this.emxEditableTable.getCellValueByRowId(currCell.rowID,"JF_FoamingUnitPrice").value.current.actual;
    let iNewValue = Number(JF_FoamModesNumber);
    let iActualValue = Number(JF_FoamingUnitPrice);
    let result = iNewValue * iActualValue;
    const  strResult = result.toFixed(2);
    //联动价格
    this.emxEditableTable.setCellValueByRowId(currCell.rowID,"JF_FoamManufacturingPrice",strResult,strResult,true);
}

function calcJF_TrimTotalTimePrice(newValue){
    const currCell = this.emxEditableTable.getCurrentCell();
    const JF_TrimTotalTimePrice = this.emxEditableTable.getCellValueByRowId(currCell.rowID,"JF_TrimTotalTimePrice").value.current.actual;
    const JF_TrimUnitPrice = this.emxEditableTable.getCellValueByRowId(currCell.rowID,"JF_TrimUnitPrice").value.current.actual;
    let iNewValue = Number(JF_TrimTotalTimePrice);
    let iActualValue = Number(JF_TrimUnitPrice);
    let result = iNewValue * iActualValue;
    const  strResult = result.toFixed(2);
    //联动价格
    this.emxEditableTable.setCellValueByRowId(currCell.rowID,"JF_TrimTotalManufacturingPrice",strResult,strResult,true);
}

function updateJFAffectedProject(strNewECRId){
    let argsJson = {objectId : strNewECRId};
    $.ajax({
        url: '../TWXPublicRest/TWXTicketService?JPOName=JF_ECRRESTService&FuncName=updateJFAffectedProject',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(argsJson),
        success: function(res) {
            if (res.code == "200"){
                alert(res.mess);
                // 更新html
                if (!res.html){
                    res.html = "";
                }
                const  projectDom = document.getElementById("JFProjectReplace");
                if (projectDom){
                    projectDom.innerHTML = res.html;
                }
            }
        },
        error: function(res) {
        },
        timeout: 3000 // 设置超时时间为3秒
    });
}

//校验只能输入正整数不能包含小数点
function isPositiveIntegerNumber(newValue) {
    let strGroupName;
    let checkRes = false;
    if (newValue) {
        // 非负整数的正则表达式：可以是0或正整数
        const regex = /^\s*(0|[1-9]\d*)\s*$/;
        checkRes = regex.test(newValue);
    }
    if (!checkRes) {
        const language = navigator.language || navigator.userLanguage;
        let strMess = "";
        if (language.includes("zh") || language.includes("zh-CN")) {
            strMess = "\u8f93\u5165\u9519\u8bef\uff01\u8bf7\u8f93\u5165\u6b63\u6574\u6570";
        } else {
            strMess = "Input error! Please enter a non-negative integer!";
        }
        alert(strMess);
    }
    return checkRes;
}

/**
 * 自动必填验证
 * @param value
 */
function onchangeJF_PCRIsAProductChar(value) {
    var selectValue = this.options[this.selectedIndex].value;
    console.log("JF_PCRIsAProductChar:", selectValue);
    var fieldName= value;
    if(fieldName=='JF_PCRIsAProductChar') {
        fieldName = 'JF_PCRProductSAChar';
        var fieldId = "calc_" + fieldName;
        if (selectValue == 'N') {
            var tr = document.getElementById(fieldId);
            tr.children[0].className = "createLabel";
            clearValidateRequiredField(fieldName);
        } else {
            var tr = document.getElementById(fieldId);
            tr.children[0].className = "createLabelRequired";
            validateRequiredField(fieldName);
        }
    }
   else if(fieldName=='JF_PCRIsAffectFKPI'){
        fieldName='JF_PCRKPI';
        var fieldId = "calc_" + fieldName;
        var labels = document.getElementsByTagName('label');
        for (var i = 0; i < labels.length; i++) {
            if (labels[i].getAttribute('for') === 'JF_PCRKPI') {
                if (selectValue == 'N') {
                    console.log(labels[i].textContent); // 输出: KPI
                    labels[i].className = "createLabel";
                    clearValidateRequiredField(fieldName);
                    break;
                } else {
                    labels[i].className = "createLabelRequired";
                    validateRequiredField(fieldName);
                    break;
                }
            }
        }
    }
}

/**
 * 清除必填数组
 * @param fieldName
 */
function clearValidateRequiredField(fieldName) {
    const fieldNameObj =  FormHandler.GetField(fieldName);
    fieldNameObj.HandlerField.requiredValidate = "";
}

/**
 * PCR选择多工程
 */
function checkPCRAffectsFactoryInputAccess(){
    //获取form中元素实际保存值
    var strIsPlatformPart = emxFormGetValue("JFIsPlatformPart").current.actual;
    const affectsFactory =  FormHandler.GetField("JFAffectsFactory");
    const  domAffectsFactory = document.getElementById('calc_JFAffectsFactory');
    affectsFactory.HandlerField[0].customValidate = checkInputIsNull;
    console.log(strIsPlatformPart);
    if ("N" === strIsPlatformPart){
        //禁用 影响工厂 输入框
        emxFormSetFieldEditable("JFAffectsFactory",false);
        //清空原输入
        const  options = FormHandler.GetField("JFAffectsFactory").HandlerField;
        let isAlert = false;
        for (let i = 0; i < options.length; i++) {
            const option = options[i];
            if (option.checked){
                option.checked =  false
            }
        }
        //移除必填校验
        //移除必填样式
        if (domAffectsFactory){
            domAffectsFactory.querySelector('td:first-child').className = "createLabel"
        }
    }else {
        emxFormSetFieldEditable("JFAffectsFactory",true);
        //添加必填校验 isZeroLength（DS校验非空JS）
        //添加必填样式
        if (domAffectsFactory){
            domAffectsFactory.querySelector('td:first-child').className = "createLabelRequired"
        }
    }
}

/**
 * 打开创建PCR界面之后执行禁用编辑适用工厂
 */
function disableJFAffectsFactory(){
        const  options = FormHandler.GetField("JFAffectsFactory").HandlerField;
        let isAlert = false;
        for (let i = 0; i < options.length; i++) {
            const option = options[i];
            if (option.checked){
                option.checked =  false
            }
        }
    emxFormSetFieldEditable("JFAffectsFactory",false);
    }


function onchangeEditJFProjectPhase(value) {
    //【ECR 变更类型】增加" QBOM 发放" (仅项目阶段为Phase 1时可选）
    emxFormReloadField("JFECRChangeType");
}

function onchangeJFProjectPhase(value) {
    var selectValue = this.options[this.selectedIndex].value;

    console.log("emxUIFormValidation-->onchangeJFProjectPhase:", selectValue);
    var fieldName= value;
    if(fieldName=='JFProjectPhase') {
        //【ECR 变更类型】增加" QBOM 发放" (仅项目阶段为Phase 1时可选）
        emxFormReloadField("JFECRChangeType");

        var fieldName1 = 'JFIsLastQuote';
        var fieldName2 = 'JFIsTKOData';
        var fieldId = "calc_" + fieldName1;
        var tr = document.getElementById(fieldId);
        var JFProjectLevel = document.getElementById("calc_JFProjectLevel");

        let content = JFProjectLevel.children[1].innerText;
        let flag=true;
        if (content.includes('A')||content.includes('B')||content.trim() === ""||content.includes('NA')){
            flag=false;
        }else {
            //C or 衍生项目
            flag=true;
        }

        const LJFIsLastQuoteId = document.getElementById('JFIsLastQuoteId');
        const JFIsTKODataId = document.getElementById('JFIsTKODataId');


        if (selectValue == 'phase1'&&flag) {
            //JFIsLastQuote必填
            tr.children[0].className = "labelRequired";
            validateRequiredField(fieldName1);
            LJFIsLastQuoteId.disabled = false;
            LJFIsLastQuoteId.classList.remove('disabled');
        }else {
            //JFIsLastQuote不可填，数据清空
            LJFIsLastQuoteId.value = '';
            LJFIsLastQuoteId.disabled = true;
            LJFIsLastQuoteId.classList.add('disabled');
            tr.children[0].className = "label";
            clearValidateRequiredField(fieldName1);
        }
        // if(selectValue == 'phase2'||selectValue == 'phase3'){
            //20260519 ljr 项目阶段的onchange handler js 新增phase 2+3
        if(selectValue == 'phase2'||selectValue == 'phase3' || selectValue == 'phase2+3'){
            //JFIsTKOData必填
            JFIsTKODataId.disabled = false;
            JFIsTKODataId.classList.remove('disabled');
            const JFIsTKODataClassName=tr.children[3].width;
            console.log("emxUIFormValidation-->JFIsTKODataClassName:", JFIsTKODataClassName);
            if(JFIsTKODataClassName){
                tr.children[3].className = "labelRequired";
            }else {
                tr.children[2].className = "labelRequired";
            }

            validateRequiredField(fieldName2);
        }else {
            //JFIsTKOData不可填写，数据清空
            JFIsTKODataId.value = '';
            JFIsTKODataId.disabled = true;
            JFIsTKODataId.classList.add('disabled');
            // tr.children[3].className = "label";
            const JFIsTKODataClassName=tr.children[3].width;
            console.log("emxUIFormValidation-->JFIsTKODataClassName:", JFIsTKODataClassName);
            if(JFIsTKODataClassName){
                tr.children[3].className = "label";
            }else {
                tr.children[2].className = "label";
            }

            clearValidateRequiredField(fieldName2);
            const calc_JFQuestionsList = document.getElementById('calc_JFQuestionsList');
            calc_JFQuestionsList.children[0].className = "label";
        }
    }
}

function onchangeJFIsTKODataForECR(value){
    var fieldName= value;
    if(fieldName=='JFIsTKOData') {
        var fieldId = "calc_" + fieldName;

        const JFIsTKODataId = document.getElementById('JFIsTKODataId');
        const calc_JFQuestionsList = document.getElementById('calc_JFQuestionsList');
        var selectValue = this.options[this.selectedIndex].value;

        if(selectValue=='Yes'){
            calc_JFQuestionsList.children[0].className = "labelRequired";
        }else if(selectValue=='No'){
            calc_JFQuestionsList.children[0].className = "label";
        }else {

        }

    }
}


function checkECRField() {

    fieldName1 = 'JFIsLastQuote';
    fieldName2 = 'JFIsTKOData';
    var fieldId = "calc_" + fieldName1;
    var tr = document.getElementById(fieldId);


    const LJFIsLastQuoteId = document.getElementById('JFIsLastQuoteId');
    const JFIsTKODataId = document.getElementById('JFIsTKODataId');
    console.log("LJFIsLastQuoteId:", LJFIsLastQuoteId);
    console.log("JFIsTKODataId:", JFIsTKODataId);


    //JFIsLastQuote不可填，数据清空
    LJFIsLastQuoteId.value = '';
    LJFIsLastQuoteId.disabled = true;
    LJFIsLastQuoteId.classList.add('disabled');
    tr.children[0].className = "createLabel";
    clearValidateRequiredField(fieldName1);


    //JFIsTKOData不可填写，数据清空
    JFIsTKODataId.value = '';
    JFIsTKODataId.disabled = true;
    JFIsTKODataId.classList.add('disabled');
    tr.children[2].className = "createLabel";
    clearValidateRequiredField(fieldName2);


}

function onchangeJFProjectLevel() {
    var JFProjectLevel = document.getElementById("calc_JFProjectLevel");

    var JFProjectPhaseId= document.getElementById("JFProjectPhaseId");
    var JFProjectName= document.getElementsByName("JFProjectName");
    var selectValue=JFProjectPhaseId.options[JFProjectPhaseId.selectedIndex].value;



    let content = JFProjectLevel.children[1].innerText;
    let flag=true;
    if (content.includes('A')||content.includes('B')||content.trim() === ""||content.includes('NA')){
        flag=false;
    }else {
        //C or 衍生项目
        flag=true;
    }

    const LJFIsLastQuoteId = document.getElementById('JFIsLastQuoteId');
    const JFIsTKODataId = document.getElementById('JFIsTKODataId');
    console.log("LJFIsLastQuoteId:", LJFIsLastQuoteId);
    console.log("JFIsTKODataId:", JFIsTKODataId);
    var fieldName1 = 'JFIsLastQuote';
    var tr = document.getElementById('calc_'+fieldName1);
    if (selectValue == 'phase1'&&flag) {
        //JFIsLastQuote必填
        tr.children[0].className = "createLabelRequired";
        validateRequiredField(fieldName1);
        LJFIsLastQuoteId.disabled = false;
        LJFIsLastQuoteId.classList.remove('disabled');
    }else {
        //JFIsLastQuote不可填，数据清空
        LJFIsLastQuoteId.value = '';
        LJFIsLastQuoteId.disabled = true;
        LJFIsLastQuoteId.classList.add('disabled');
        tr.children[0].className = "createLabel";
        clearValidateRequiredField(fieldName1);
    }
    //20260519 ljr 新增刷新项目阶段   需要判断选择项目是无DV/DV 然后动态匹配Range
    emxFormReloadField("JFProjectPhase");
}

/**
 * 现在输入字符的大小
 * @param newValue
 * @returns {*}
 */
function  validateInputIsStringLength(newValue) {
    let length = newValue.length;
    let checkRes = true;
    if (length>30){
        checkRes = false;
        const language = navigator.language || navigator.userLanguage;
        let strMess = "";
        if (language.includes("zh") || language.includes("zh-CN")) {
            strMess = "\u8F93\u5165\u9519\u8BEF\uFF01\u96F6\u4EF6\u4E2D\u6587\u540D\u79F0\u6700\u5927\u8F93\u516530\u4E2A\u5B57\u7B26";
        } else {
            strMess = "Input error! The maximum length of the Chinese name of the part is 30 characters.";
        }
        alert(strMess);
    }
    return checkRes ;
}
function  validateInputIsStringLengthForColor(newValue) {
    let length = newValue.length;
    let checkRes = true;
    if (length>9){
        checkRes = false;
        const language = navigator.language || navigator.userLanguage;
        let strMess = "";
        if (language.includes("zh") || language.includes("zh-CN")) {
            strMess = "\u8F93\u5165\u9519\u8BEF\uFF01\u6574\u6905\u989C\u8272\u540D\u79F0\u6700\u5927\u8F93\u51659\u4E2A\u5B57\u7B26";
        } else {
            strMess = "Input error! The maximum length for entering the color name of the entire chair is 9 characters.";
        }
        alert(strMess);
    }
    return checkRes ;
}


/**
 * MBOM编辑 用量单位为个时，MBOM的用量必须为整数
 */
function  validateJFDosageOfJFUtil(newValue) {
    const currCell = this.emxEditableTable.getCurrentCell();
    const actualValue = this.emxEditableTable.getCellValueByRowId(currCell.rowID,"JF_Unit").value.current.actual;
    const regex1 = /^(?!0+(\.0+)?$)\d+(\.\d+)?$/;  //正实数 不包含0.0 0
    const regex2 = /^[1-9]\d*$/;  //正整数
    const language = navigator.language || navigator.userLanguage;
    if ("PC" === actualValue) {
        if (!regex2.test(newValue)) {
            if (language.includes("zh") || language.includes("zh-CN")) {
                strMess = "\u7528\u91cf\u53ea\u80fd\u8f93\u5165\u6b63\u5b9e\u6570,\u4e0d\u5305\u542b0,0.0";
            } else {
                strMess = "Usage can only be entered as positive real numbers, excluding 0,0.0";
            }
            alert(strMess);
            return false;
        }
    } else {
        if (!regex1.test(newValue)) {
            if (language.includes("zh") || language.includes("zh-CN")) {
                strMess = "\u9009\u62e9\u8f85\u6599\u7684\u5355\u4f4d\u662f\u4e2a,\u7528\u91cf\u53ea\u80fd\u8f93\u5165\u6b63\u6574\u6570";
            } else {
                strMess = "The unit for selecting auxiliary materials is PC, and the dosage can only be entered as a positive integer";
            }
            alert(strMess);
            return false;
        }
    }
    return true ;
}

/**
 *
 * @param strObjectId
 * @param strTableName
 * @param strRelId
 */
function openVueModel(strObjectId,strTableName,strRelId){
    //创建channel通信
    const channel = new BroadcastChannel("JF_OPEN_VUE_MODEL");
    // let currentCell = emxEditableTable.getCurrentCell();
    //获取rowId
    let currentCell = emxEditableTable.getCellValueByObjectRelId(strRelId,strObjectId,"JFBOMIncrement");
    const messObj = {
        objectId:strObjectId,
        tableName:strTableName,
        relId:strRelId,
        rowId:currentCell.rowID
    };
    channel.postMessage(messObj);
}


function removePCRExecuteTaskDoc(PCRExecuteTaskDocid){
    let argsJson = {objectId : PCRExecuteTaskDocid};
    const isConfirm = confirm("确认删除");
    if (isConfirm){
        console.log("----2000>");
        // const  projectDom = document.getElementById(PCRExecuteTaskDocid);
        // console.log(projectDom);
        //
        // if (projectDom){
        //     projectDom.innerHTML = '';
        // }

        $.ajax({
            url: '../TWXPublicRest/TWXTicketService?JPOName=JF_DataInterface&FuncName=removePCRExecuteTaskDoc',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(argsJson),
            success: function(res) {
                if (res.code == "200"){
                    // alert(res.mess);
                    // 更新html
                    console.log("----2000>");
                    const  projectDom = document.getElementById(PCRExecuteTaskDocid);

                    console.log(projectDom);
                    if (projectDom){
                        projectDom.innerHTML = '';
                    }
                }
            },
            error: function(res) {
            },
            timeout: 3000 // 设置超时时间为3秒
        });
    }

}

/**
 * 更新BOM增量成本
 * @param strNewValue 新值
 * @param strRowId 行id
 * @param strFieldName 字段名
 * @returns {boolean}
 * @author CHENYAN
 * @DATE 2026-01-27
 */
function updateBOMIncrementPriceCol(strNewValue,strRowId,strFieldName) {
    //内外部的另一个属性值
    let strOtherFieldValue = "";
    if ("JF_ChangeUnitPriceCostExternal" == strFieldName){
        const otherColObj = emxEditableTable.getCellValueByRowId(strRowId,"JF_ChangeUnitPriceCost");
        strOtherFieldValue = otherColObj.value.current.display;
    }else if ("JF_ChangeUnitPriceCost" == strFieldName){
        const otherColObj = emxEditableTable.getCellValueByRowId(strRowId,"JF_ChangeUnitPriceCostExternal");
        strOtherFieldValue = otherColObj.value.current.display;
    }
    let otherNum = Number(0);
    //转换为Number对象
    if (strOtherFieldValue){
        otherNum = Number(strOtherFieldValue);
    }
    let newNum = Number(0);
    //转换为Number对象
    if (strNewValue){
        newNum = Number(strNewValue);
    }
    //差异值
    let diffNum = otherNum + newNum;

    //BOM增量
    const bomIncrementCellValue = emxEditableTable.getCellValueByRowId(strRowId,"JFBOMIncrement");
    //旧的BOM增量html
    let oldHtmlCellValue = bomIncrementCellValue.value.old.actual  ;
    //旧的BOM增量值
    let oldCellValue = bomIncrementCellValue.value.old.display  ;
    //旧的BOM增量值
    let incrementOldNum = Number(oldCellValue)  ;
    //保存最原始的value对象
    //表格数据
    var pTable = editableTable.tblListBody;
    if(!pTable){
        pTable = editableTable.tblTreeBody;
    }
    //行对象
    let  objTR = null ;
    for(var i=0;i<pTable.rows.length;i++){
        var rowObj = pTable.rows[i];
        var rowID = rowObj.getAttribute("id");
        if(rowID == strRowId){
            objTR = rowObj;
            break;
        }
    }
    if (objTR){
        let colObj = colMap.getColumnByName("JFBOMIncrement");
        let colIndex = -1 ;
        if (colObj){
            colIndex = colObj.index;
        }
        let objTD = null;
        //找到td
        if (colIndex != -1 ){
            for(var i = 0; i < objTR.cells.length; i++){
                var cell = objTR.cells[i];
                var pos = parseInt(cell.getAttribute("position"),10);
                if(pos && pos == colIndex){
                    objTD = cell;
                    break;
                }
            }
            if(objTD == null){
                for(var i = 0; i < objTR.nextSibling.cells.length; i++){
                    var cell = objTR2.cells[i];
                    var pos = parseInt(cell.getAttribute("position"),10);
                    if(pos && pos == colIndex){
                        objTD = cell;
                        break;
                    }
                }
            }
        }
        console.log("objTD:",objTD)
        if (objTD){
            // objTD.innerHTML 直接单元格里面的html
            console.log(objTD.innerHTML);
            let objHTML = emxUICore.createXMLDOM();
            objHTML.loadXML(objTD.innerHTML);
            let strCurrentPriceQuantity = objHTML.documentElement.getAttribute("currentpricequantity");
            let strIsFirstRevision = objHTML.documentElement.getAttribute("isfirstrevision");
            let strBeforePrice = objHTML.documentElement.getAttribute("beforeprice");
            let strBeforePriceQuantity = objHTML.documentElement.getAttribute("beforepricequantity");
            //  首版BOM增量成本等于原始成本 不需要联动更新
            if ("true" == strIsFirstRevision){
                return;
            }
            //  （（前一个版本的单价成本 + 当前版本增量单价成本（内外部之和））* 当前版本的数量）- 前一个版本的单件成本 * 前一个版本的数量
            let currentPriceQuantity = Number(strCurrentPriceQuantity);
            let beforeUnitPrice = Number(strBeforePrice);
            let beforePriceQuantity = Number(strBeforePriceQuantity);
            let currentPrice = ((beforeUnitPrice + diffNum) * currentPriceQuantity) ;
            let beforePrice = (beforeUnitPrice * beforePriceQuantity) ;
            let newValue = (currentPrice - beforePrice).toFixed(2);
            for (let i = 0; i < objHTML.documentElement.childNodes.length; i++) {
                let childNode = objHTML.documentElement.childNodes[i];
                if (childNode.nodeName === "span") {
                    childNode.innerHTML = newValue;
                }
            }
            //必须使用c标签包裹xml
            newHtmlXml = `<c>${objHTML.xml}</c>`;
            emxEditableTable.setCellHTMLValueByRowId(strRowId, "JFBOMIncrement", newHtmlXml, false,true);
        }
    }
}

function  flushChangedRow(strRows){
    console.log(strRows);
    let rowIds = strRows.split("@");
    //可以刷新html列
    emxEditableTable.refreshRowByRowId(rowIds);
}


/**
 * add by ljr ECO执行任务的计划完成时间不能早于创建时间
 * @param newVal
 * @param rowId
 * @param attrName
 */
function validateJFTaskEstimatedFinishDate(newVal) {
    // 1. 计划完成时间 处理时间戳（毫秒） 格式化为天
    const ymd1 = timestampToYMD(newVal);       // 20260202
    //2.找到iframe 拿取计划完成时间动态列中配置的ECP任务时间
    var m = findFrame(getTopWindow(),'detailsDisplay');
    const currCell = m.emxEditableTable.getCurrentCell();
    // var taskTime = m.colMap.getColumnByName('JFTaskEstimatedFinishDate').settings['TaskOriginated']; //拿取ECO任务时间
    //拿取ECP的创建时间
    //2.将ECP的创建时间格式化为天
    var originated = m.emxEditableTable.getCellValueByRowId(currCell.rowID,"Originated").value.current.actual;
    var originatedDisPlay = m.emxEditableTable.getCellValueByRowId(currCell.rowID,"Originated").value.current.display;
    const ymd2 = usDateTimeToYMD(originated); // 20240822
    //3.拿取当前修改行的ECP任务name
    const taskName = m.emxEditableTable.getCellValueByRowId(currCell.rowID,"TaskName").value.current.actual;
    //4.开始判断 时间
    let message = "";
    const browserLanguage = navigator.language;
    // if(daysBetween(day1, day2)) {
    if (ymd2 > ymd1) {
        if (browserLanguage.startsWith('zh')) {
            message ="ECO执行任务:"+ taskName + "的计划完成时间不能早于ECP任务的创建时间("+ originatedDisPlay +")";
        } else {
            message="The completion time of the ECO Implementation Plan: " + taskName +  " cannot be earlier than the creation time of the ECP task("+ originatedDisPlay +")";
        }
    }
    if (""!==message) {
        alert(message);
        return false;
    } else {
        return true;
    }
}

/**
 * 将时间戳转换为 YYMMDDD 格式 精确到天
 * @param timestamp
 * @returns {number}
 */
function timestampToYMD(timestamp) {
    const numTimestamp = Number(timestamp);
    const d = new Date(numTimestamp);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0'); // 月份从0开始
    const day = String(d.getDate()).padStart(2, '0');
    return Number(`${y}${m}${day}`);
}

/**
 * 将系统时间转换为YYMMDD格式 精确到天
 * @param str
 * @returns {number}
 */
function usDateTimeToYMD(str) {
    // 示例输入: "8/22/2024 10:37:20 AM"
    const datePart = str.split(' ')[0]; // "8/22/2024"
    const [month, day, year] = datePart.split('/').map(Number);
    const y = year;
    const m = String(month).padStart(2, '0');
    const d = String(day).padStart(2, '0');
    return Number(`${y}${m}${d}`);
}

/**
 * 职能列的 OnFocus Handler 方法
 * ESO table表中 联动刷新职能，这里刷新的时候需要判断当前节点的上级节点是什么
 * @User liujr
 * @returns {boolean}
 */
function reloadJFFunctionOnFocus(){
    if(null !=emxEditableTable && "null" != emxEditableTable){
        const strRowId = emxEditableTable.getCurrentCell().rowID;
        emxEditableTable.reloadCell("functional",strRowId);
    }
    return true;
}

/**
 * 职能列的 On Change Handler方法
 * WBS中ESO Table中职能刷新模块的js方法 On Change Handler
 * */
function changeESOTableFunctionModulesHandler(){
    if(null !=emxEditableTable && "null" != emxEditableTable){
        const strRowId = emxEditableTable.getCurrentCell().rowID;
        var functional = this.emxEditableTable.getCellValueByRowId(strRowId, "functional").value.current.actual;
        if (null == functional || typeof functional === "undefined" || "" === functional) {
            return true;
        }
        var range;
        if ("CS" === functional) {
            range = "CS";
        } else if ("CS_JIT" === functional) {
            range = "CS_JIT";
        } else if ("EE" === functional) {
            range = "E1";
        } else if ("Structure" === functional) {
            range = "Structure";
        } else if ("Subsystem" === functional) {
            range = "Trim";
        }
        this.emxEditableTable.setCellValueByRowId(strRowId, "JF_Modules", range, range, true);
        emxEditableTable.reloadCell("JF_Modules", strRowId);
        return true;
    }
}

/**
 * 模块列的 On OnFocus Handler 方法
 * WBS中ESO Table中模块刷新的js方法 On OnFocus Handler
 * */
function reloadModulesOnFocus(){
    if(null !=emxEditableTable && "null" != emxEditableTable){
        const strRowId = emxEditableTable.getCurrentCell().rowID;
        emxEditableTable.reloadCell("JF_Modules",strRowId);
    }
}

/**
 * 模块列的 On Change Handler 方法
 * WBS中ESO Table中模块刷新地点的js方法 On Change Handler
 * */
function changeESOTableJFModulesJFLocations(){
    if(null !=emxEditableTable && "null" != emxEditableTable){
        const strRowId = emxEditableTable.getCurrentCell().rowID;
        var modules = this.emxEditableTable.getCellValueByRowId(strRowId, "JF_Modules").value.current.actual;
        if (modules == null || typeof modules === "undefined" || modules === "") {
            return true;
        }
        var range;
        var range1;
        if ("CS,CS_JIT".includes(modules)) {
            range = "SH";
            range1 = "D1";
        } else if ("Trim,Plastic,Foam".includes(modules)) {
            range = "SH";
            range1 = "1";
        } else if ("Desk,Foot,Leg,E2,HR,AR".includes(modules)) {
            range = "SH";
            range1 = "NA";
        } else if ("E1".includes(modules)) {
            range = "SH";
            range1 = "NA";
        } else if ("Structure".includes(modules)) {
            range = "SH";
            range1 = "Portfolio";
        }
        this.emxEditableTable.setCellValueByRowId(strRowId, "JF_Locations", range, range, true);
        this.emxEditableTable.setCellValueByRowId(strRowId, "JF_Department", range1, range1, true);
        emxEditableTable.reloadCell("JF_Locations", strRowId);
        emxEditableTable.reloadCell("JF_Department", strRowId);
        return true;
    }

}

/**
 * 地点列 On OnFocus Handler 方法
 * WBS中ESO Table中地点刷新的js方法 On OnFocus Handler
 * */
function reloadJFLocationsOnFocus(){
    if(null !=emxEditableTable && "null" != emxEditableTable){
        const strRowId = emxEditableTable.getCurrentCell().rowID;
        emxEditableTable.reloadCell("JF_Locations",strRowId);
    }
}

/**
 * 地点列 On Change Handler 方法
 * WBS中ESO Table中地点刷新部门的js方法 On Change Handler
 * */
function changeESOTableJFLocationsJFDepartment(){
    if(null !=emxEditableTable && "null" != emxEditableTable){
        const strRowId = emxEditableTable.getCurrentCell().rowID;
        var locations = this.emxEditableTable.getCellValueByRowId(strRowId, "JF_Locations").value.current.actual;
        var modules = this.emxEditableTable.getCellValueByRowId(strRowId, "JF_Modules").value.current.actual;
        if (modules == null || typeof modules === "undefined" || modules === "") {
            return true;
        }
        if (locations == null || typeof locations === "undefined" || locations === "") {
            return true;
        }
        var range;
        if ("CS,CS_JIT".includes(modules) && "SH".includes(locations)) {
            range = "D1";
        } else if ("CS,CS_JIT".includes(modules) && "Beijing,Changchun,Changzhou,Chongqing,Hefei,Wuhu,Ningbo".includes(locations)) {
            range = "NA";
        } else if ("Structure".includes(modules) && "SH".includes(locations)) {
            range = "Portfolio";
        } else if ("Structure".includes(modules) && "CQ,BJ,CZ,HF,WH".includes(locations)) {
            range = "Metal";
        } else if ("Trim,Plastic,Foam".includes(modules) && "SH".includes(locations)) {
            range = "1";
        } else if ("Trim,Plastic,Foam".includes(modules) && "BJ,CC,CZ,CQ,HF,WH,NB".includes(locations)) {
            range = "NA";
        } else if ("Desk,Foot,Leg,E1,E2,HR,AR".includes(modules) && "SH,BJ,CC,CZ,CQ,HF,WH,NB".includes(locations)) {
            range = "NA";
        }
        this.emxEditableTable.setCellValueByRowId(strRowId, "JF_Department", range, range, true);
        emxEditableTable.reloadCell("JF_Department", strRowId);
        return true;
    }
}

/**
 * 部门列  On OnFocus Handler 方法
 * WBS中ESO Table中部门刷新的js方法 On OnFocus Handler
 * */
function reloadJFDepartmentOnFocus(){
    if(null !=emxEditableTable && "null" != emxEditableTable){
        const strRowId = emxEditableTable.getCurrentCell().rowID;
        emxEditableTable.reloadCell("JF_Department",strRowId);
    }
}


function markEditSelectedLine(){
    if(null !=emxEditableTable && "null" != emxEditableTable){
        //1.先找到固定列前的部分
        const strRowId = emxEditableTable.getCurrentCell().rowID;
        // 2. 获取 固定列后的部分 bodyTable 下所有的行 (tr 标签)
        var bodyTable = document.getElementById("bodyTable");
        var allRows = bodyTable.getElementsByTagName('tr');
        var targetRow = null;
        // 3. 遍历查找 ID 完全匹配的那一行
        for (var i = 0; i < allRows.length; i++) {
            if (allRows[i].id === strRowId) {
                targetRow = allRows[i];
                break; // 找到了就跳出循环
            }
        }
        // 4. 将改行设置为蓝色底纹
        document.getElementById(strRowId).className = document.getElementById(strRowId).className + " mx_rowSelected";
        if (targetRow) {
            targetRow.className = targetRow.className + " mx_rowSelected";
        }

    }
}

//DA /PartList  选择项目 自动联动阶段 add ljr 20260519
function onchangeDaPartListJFProject() {
    emxFormReloadField("JFProjectPhase");
    var relatedFieldNames = [
        "RelatedDR", "RelatedDRDisplay", "RelatedDROID",
        "RelatedECR", "RelatedECRDisplay", "RelatedECROID"
    ];
    for (var i = 0; i < relatedFieldNames.length; i++) {
        var field = document.getElementById(relatedFieldNames[i]);
        if (!field && document.forms.length > 0) {
            field = document.forms[0][relatedFieldNames[i]];
        }
        if (field) {
            field.value = "";
        }
    }
}

/**
 * DA编辑页面保存前校验启动时间和关闭时间。
 * 启动时间必须早于关闭时间，且时间间隔不能超过30天。
 * @returns {boolean}
 */
function validateDADateRange() {
    var startTimeField = document.getElementById("JFDAStartTime_msvalue");
    var closeTimeField = document.getElementById("JFDACloseTime_msvalue");
    if (!startTimeField && document.forms.length > 0) {
        startTimeField = document.forms[0]["JFDAStartTime_msvalue"];
    }
    if (!closeTimeField && document.forms.length > 0) {
        closeTimeField = document.forms[0]["JFDACloseTime_msvalue"];
    }
    if (!startTimeField || !closeTimeField || !startTimeField.value || !closeTimeField.value) {
        return true;
    }

    var startTime = Number(startTimeField.value);
    var closeTime = Number(closeTimeField.value);
    if (isNaN(startTime) || isNaN(closeTime)) {
        return true;
    }
    if (startTime >= closeTime) {
        sendMess("DA\u542f\u52a8\u65f6\u95f4\u5fc5\u987b\u65e9\u4e8eDA\u5173\u95ed\u65f6\u95f4\u3002",
                "The DA start time must be earlier than the DA close time.");
        return false;
    }
    if (closeTime - startTime > 30 * 24 * 60 * 60 * 1000) {
        sendMess("DA\u542f\u52a8\u65f6\u95f4\u548cDA\u5173\u95ed\u65f6\u95f4\u7684\u95f4\u9694\u4e0d\u80fd\u8d85\u8fc730\u5929\u3002",
                "The interval between the DA start time and close time cannot exceed 30 days.");
        return false;
    }
    return true;
}

/**
 * DA创建页面选择关联DR或正式ECR。
 * @param changeType JFDR或JFFormalECR
 * @author caipan
 * @date 2026/7/24
 */
function showDARelatedChangeSelector(changeType) {
    var projectField = document.getElementById("JFProjectNameOID");
    if (!projectField && document.forms.length > 0) {
        projectField = document.forms[0]["JFProjectNameOID"];
    }
    var projectId = projectField ? projectField.value : "";
    if (!projectId) {
        sendMess("\u8bf7\u5148\u9009\u62e9\u9879\u76ee\u3002", "Please select a project first.");
        return;
    }

    var isFormalECR = "JFFormalECR" == changeType;
    var fieldName = isFormalECR ? "RelatedECR" : "RelatedDR";
    var symbolicType = isFormalECR ? "type_JFFormalECR" : "type_JFDR";
    var searchUrl = "../common/emxFullSearch.jsp?field=TYPES=" + symbolicType
            + "&table=AEFGeneralSearchResults"
            + "&showInitialResults=true"
            + "&selection=multiple"
            + "&includeOIDprogram=JF_DeviationApplicationSource:getDAProjectRelatedChangeIds"
            + "&projectId=" + encodeURIComponent(projectId)
            + "&changeType=" + encodeURIComponent(isFormalECR ? "JFFormalECR" : "JFDR")
            + "&fieldNameActual=" + fieldName
            + "&fieldNameDisplay=" + fieldName + "Display"
            + "&fieldNameOID=" + fieldName + "OID"
            + "&submitURL=./JF_AEFSearchUtil.jsp";
    showModalDialog(searchUrl, 850, 630, true, "Large");
}

/**
 * 判断创建快照时是否必须关联产品配置表。
 * @param snapshotSubType 快照子类型
 * @returns {boolean}
 */
function isSnapshotProductConfigRequired(snapshotSubType) {
    return ["Quote", "DV SOURCING", "PV SOURCING", "OTS", "SOP"].indexOf(snapshotSubType) >= 0;
}

/**
 * 获取创建快照页面的快照子类型实际值。
 * @returns {string}
 */
function getSnapshotSubTypeValue() {
    try {
        var formValue = emxFormGetValue("JFSnapshotSubType");
        if (formValue && formValue.current) {
            return formValue.current.actual || "";
        }
    } catch (e) {
        // Fall back to the form element.
    }
    var field = document.getElementById("JFSnapshotSubType");
    if (!field && document.forms.length > 0) {
        field = document.forms[0]["JFSnapshotSubType"];
    }
    return field ? field.value : "";
}

/**
 * 清空创建快照页面已选择的产品配置表。
 */
function clearSnapshotProductConfigSelection() {
    var actualField = document.getElementById("JFSnapshotProductConfig");
    var displayField = document.getElementById("JFSnapshotProductConfigDisplay");
    if (actualField) {
        actualField.value = "";
    }
    if (displayField) {
        displayField.value = "";
    }
}

/**
 * 根据快照子类型控制产品配置表及最终报价版本字段显示和默认值。
 */
function updateSnapshotProductConfigField() {
    var snapshotSubType = getSnapshotSubTypeValue();
    //20260806 update by caipan 最终报价版本字段仅在Quote类型下显示
    var finalQuotationRow = document.getElementById("calc_JF_IsTheFinalQuotationVersionAvailable");
    if (finalQuotationRow) {
        finalQuotationRow.style.display = snapshotSubType === "Quote" ? "" : "none";
    }
    var row = document.getElementById("calc_ProductConfigTable");
    if (!row) {
        return;
    }
    var required = isSnapshotProductConfigRequired(snapshotSubType);
    row.style.display = required ? "" : "none";
    var labelCell = row.querySelector("td:first-child");
    if (labelCell) {
        labelCell.className = required ? "createLabelRequired" : "createLabel";
    }
    if (!required) {
        clearSnapshotProductConfigSelection();
        return;
    }
    var actualField = document.getElementById("JFSnapshotProductConfig");
    var displayField = document.getElementById("JFSnapshotProductConfigDisplay");
    var defaultField = document.getElementById("JFSnapshotDefaultProductConfig");
    var defaultDisplayField = document.getElementById("JFSnapshotDefaultProductConfigDisplay");
    if (actualField && !actualField.value && defaultField) {
        actualField.value = defaultField.value;
        displayField.value = defaultDisplayField ? defaultDisplayField.value : "";
    }
}

/**
 * 快照子类型变更处理。
 */
function onchangeSnapshotProductConfig() {
    updateSnapshotProductConfigField();
}

/**
 * 打开当前项目产品配置表单选搜索。
 */
function showSnapshotProductConfigSelector() {
    var projectField = document.getElementById("JFSnapshotProductConfigProjectId");
    var projectId = projectField ? projectField.value : "";
    if (!projectId) {
        return;
    }
    var searchUrl = "../common/emxFullSearch.jsp?field=TYPES=type_JFProductConfigTable"
            + "&table=AEFGeneralSearchResults"
            + "&showInitialResults=true"
            + "&selection=single"
            + "&submitAction=refreshCaller"
            + "&includeOIDprogram=JF_ProductConfig:getConfigTableRouteSearchObjectIds"
            + "&projectId=" + encodeURIComponent(projectId)
            + "&projectRelationship=JFProject2ProductConfigTable"
            + "&submitURL=../common/JF_SnapshotProductConfigSearchSubmit.jsp";
    showModalDialog(searchUrl, 850, 630, true, "Large");
}

/**
 * 创建或编辑快照提交前校验产品配置表必填。
 * @returns {boolean}
 */
function validateSnapshotProductConfig() {
    if (!isSnapshotProductConfigRequired(getSnapshotSubTypeValue())) {
        return true;
    }
    var actualField = document.getElementById("JFSnapshotProductConfig");
    if (!actualField) {
        return true;
    }
    if (actualField.value) {
        return true;
    }
    var messageField = document.getElementById("JFSnapshotProductConfigRequiredMessage");
    alert(messageField ? messageField.value : "Please select a product configuration table.");
    return false;
}

//PCR  选择主项目 自动联动阶段 add ljr 20260520
function onchangePCRmainProject() {
    emxFormReloadField("JF_PCRProjectPhase");
}
/**
 * JF_isKeyTask列 On Change Handler。
 * 1. 用于WBS任务排程视图和ESO视图；
 * 2. 从当前编辑行读取JF_isKeyTask列的实际值，值为Y时提示用户完成关键任务必须上传交付物文档；
 * 3. 仅做提示，不阻断表格保存。
 * @author LIUJR
 * @date 2026/7/7
 * @returns {boolean}
 */
function validateJFIsKeyTaskTableField(){
    if(null != emxEditableTable && "null" != emxEditableTable){
        const strRowId = emxEditableTable.getCurrentCell().rowID;
        var isKeyTask = "";
        try {
            var tableObj = this && this.emxEditableTable ? this.emxEditableTable : emxEditableTable;
            var isKeyTaskCell = tableObj.getCellValueByRowId(strRowId, "JF_isKeyTask");
            if (isKeyTaskCell && isKeyTaskCell.value && isKeyTaskCell.value.current) {
                isKeyTask = isKeyTaskCell.value.current.actual;
                if (isKeyTask == null || typeof isKeyTask === "undefined" || isKeyTask === "") {
                    isKeyTask = isKeyTaskCell.value.current.display;
                }
            }
        } catch (e) {
            isKeyTask = "";
        }
        if ("Y" === isKeyTask || "Yes" === isKeyTask || "true" === isKeyTask) {
            var deliverableAlertUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.JFIsKeyTask.MandatoryDeliverableAlert";
            var deliverableAlertMessage = emxUICore.getData(deliverableAlertUrl);
            alert(deliverableAlertMessage);
        }
        return true;
    }
    return true;
}

/**
 * JF_isKeyTask字段 OnChange Handler。
 * 1. 用于任务特性页面等通用Form编辑场景；
 * 2. 页面编辑态可能只加载通用Form校验JS，需在此处定义方法，避免提示Change Handler未定义；
 * 3. 当“是否关键任务”修改为Y时，读取国际化提示并提醒用户完成关键任务必须上传交付物文档；
 * 4. 仅做提示，不阻断保存。
 * @author LIUJR
 * @date 2026/7/8
 * @returns {boolean}
 */
function validateJFIsKeyTaskFormField() {
    var isKeyTaskObj = document.getElementById("JF_isKeyTaskId");
    if (isKeyTaskObj == null) {
        isKeyTaskObj = document.getElementById("JF_isKeyTask");
    }
    if (isKeyTaskObj == null && document.forms[0]) {
        isKeyTaskObj = document.forms[0].elements["JF_isKeyTask"];
    }
    if (isKeyTaskObj == null) {
        return true;
    }
    var isKeyTask = isKeyTaskObj.value;
    if ("Y" == isKeyTask || "Yes" == isKeyTask || "true" == isKeyTask) {
        var deliverableAlertUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.JFIsKeyTask.MandatoryDeliverableAlert";
        var deliverableAlertMessage = emxUICore.getData(deliverableAlertUrl);
        alert(deliverableAlertMessage);
    }
    return true;
}

/**
 * 获取项目国际化提示。
 * @param errorKey 国际化Key
 * @param defaultMessage 默认提示
 * @author LIUJR
 * @date 2026/7/17
 * @returns {string}
 */
function getJFCustomerPartsDBMessage(errorKey, defaultMessage) {
    try {
        var messageUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=" + errorKey;
        var message = emxUICore.getData(messageUrl);
        if (message && message.indexOf(errorKey) < 0) {
            return message;
        }
    } catch (e) {
    }
    return defaultMessage;
}

/**
 * 获取EBOM行上的所属项目值。
 * @param rowId 当前行ID
 * @author LIUJR
 * @date 2026/7/17
 * @returns {string}
 */
function getEBOMCustomerPartsDBProjectValue(rowId) {
    var projectValue = getValueForCell(rowId, "ProjectRel", "actual");
    if (!projectValue) {
        projectValue = getValueForCell(rowId, "ProjectRel", "display");
    }
    return projectValue;
}

/**
 * EBOM客户零件号/DB列编辑前校验所属项目。
 * @author LIUJR
 * @date 2026/7/17
 * @returns {boolean}
 */
function checkEBOMCustomerPartsDBProject() {
    if (null == emxEditableTable || "null" == emxEditableTable) {
        return true;
    }
    var currentCell = emxEditableTable.getCurrentCell();
    var rowId = currentCell ? currentCell.rowID : "";
    var projectValue = getEBOMCustomerPartsDBProjectValue(rowId);
    if (!projectValue) {
        alert(getJFCustomerPartsDBMessage(
                "emxProgramCentral.JFCustomerPartsDB.ProjectRequired",
                "所属项目维护后才能编辑【客户零件号/DB】属性信息"));
        return false;
    }
    return true;
}

/**
 * EBOM客户零件号/DB列保存必填校验。
 * @param newValue 当前单元格新值
 * @param rowId 当前行ID
 * @param attrName 当前列名
 * @author LIUJR
 * @date 2026/7/17
 * @returns {boolean}
 */
function validateEBOMCustomerPartsDBRequired(newValue) {
    if (null == emxEditableTable || "null" == emxEditableTable) {
        return true;
    }
    var currentCell = emxEditableTable.getCurrentCell();
    var rowId = currentCell ? currentCell.rowID : "";
    var attrName = currentCell ? (currentCell.columnName || currentCell.name || currentCell.attrName) : "";
    var projectValue = getEBOMCustomerPartsDBProjectValue(rowId);
    if (!projectValue) {
        alert(getJFCustomerPartsDBMessage(
                "emxProgramCentral.JFCustomerPartsDB.ProjectRequired",
                "所属项目维护后才能编辑【客户零件号/DB】属性信息"));
        return false;
    }

    var directBuy = getValueForCell(rowId, "CustomerPartsDBRelDirectBuy", "actual");
    var customerPartNumber = getValueForCell(rowId, "CustomerPartsDBRelNumber", "actual");
    var customerPartName = getValueForCell(rowId, "CustomerPartsDBRelName", "actual");
    var customerPartRevision = getValueForCell(rowId, "CustomerPartsDBRelRevision", "actual");
    if ("CustomerPartsDBRelDirectBuy" == attrName || "CustomerPartsDBDirectBuy" == attrName || "attribute_JF_DirectBuy" == attrName) {
        directBuy = newValue;
    } else if ("CustomerPartsDBRelNumber" == attrName || "CustomerPartsDBNumber" == attrName || "attribute_JFCustomerPartNumber" == attrName) {
        customerPartNumber = newValue;
    } else if ("CustomerPartsDBRelName" == attrName || "CustomerPartsDBName" == attrName || "attribute_JFCustomerPartName" == attrName) {
        customerPartName = newValue;
    } else if ("CustomerPartsDBRelRevision" == attrName || "CustomerPartsDBRevision" == attrName || "attribute_JFCustomerPartRevision" == attrName) {
        customerPartRevision = newValue;
    }

    if ("consignment" == directBuy || "direct-buy" == directBuy) {
        if (!customerPartNumber || !customerPartName || !customerPartRevision) {
            alert(getJFCustomerPartsDBMessage(
                    "emxProgramCentral.JFCustomerPartsDB.RequiredWhenDirectBuy",
                    "DirectBuy为consignment、direct-buy时，客户零件号、客户零件名称、客户零件版本必填。"));
            return true;
        }
    }
    return true;
}

/**
 * 获取EBOM客户零件号/DB单元格保存时的最终值。
 * @param rowId 当前行ID
 * @param columnName 列名
 * @author LIUJR
 * @date 2026/7/20
 * @returns {string}
 */
function getEBOMCustomerPartsDBApplyCellValue(rowId, columnName) {
    var value = getValueForCell(rowId, columnName, "actual");
    if (null == value || "undefined" == typeof value) {
        value = getValueForCell(rowId, columnName, "display");
    }
    return null == value || "undefined" == typeof value
            ? ""
            : String(value).replace(/^\s+|\s+$/g, "");
}

var jfCustomerPartsDBApplyValidationMessageReturned = false;

/**
 * 返回EBOM客户零件号/DB保存校验提示。
 * @param message 国际化提示
 * @author LIUJR
 * @date 2026/7/20
 * @returns {string}
 * @description 四列共用ValidateOnApply，单次保存只返回一次提示，避免同一错误重复弹出。
 */
function getEBOMCustomerPartsDBApplyValidationMessage(message) {
    if (jfCustomerPartsDBApplyValidationMessageReturned) {
        return "";
    }
    jfCustomerPartsDBApplyValidationMessageReturned = true;
    window.setTimeout(function () {
        jfCustomerPartsDBApplyValidationMessageReturned = false;
    }, 0);
    return message;
}

/**
 * EBOM客户零件号/DB整表保存必填校验。
 * @param newValue 当前校验列的新值
 * @param rowId 当前行ID
 * @author LIUJR
 * @date 2026/7/20
 * @returns {string} 校验通过返回空字符串，校验失败返回国际化提示
 * @description ValidateOnApply在用户点击保存时执行。
 *              DirectBuy为consignment、direct-buy时，客户零件号、客户零件名称、客户零件版本必填；
 *              DirectBuy为non-DB时允许以上三个字段为空。
 */
function validateEBOMCustomerPartsDBRequiredOnApply(newValue, rowId) {
    if (!rowId) {
        return "";
    }

    var projectValue = getEBOMCustomerPartsDBProjectValue(rowId);
    if (!projectValue) {
        return getEBOMCustomerPartsDBApplyValidationMessage(getJFCustomerPartsDBMessage(
                "emxProgramCentral.JFCustomerPartsDB.ProjectRequired",
                "所属项目维护后才能编辑【客户零件号/DB】属性信息"));
    }

    var directBuy = getEBOMCustomerPartsDBApplyCellValue(rowId, "CustomerPartsDBRelDirectBuy");
    var customerPartNumber = getEBOMCustomerPartsDBApplyCellValue(rowId, "CustomerPartsDBRelNumber");
    var customerPartName = getEBOMCustomerPartsDBApplyCellValue(rowId, "CustomerPartsDBRelName");
    var customerPartRevision = getEBOMCustomerPartsDBApplyCellValue(rowId, "CustomerPartsDBRelRevision");

    // ValidateOnApply会按当前列传入新值，这里覆盖对应字段，确保使用本次保存的最终数据。
    var columnName = "";
    if ("undefined" != typeof colMap && "undefined" != typeof currentColumnPosition) {
        var currentColumn = colMap.getColumnByIndex(currentColumnPosition - 1);
        columnName = currentColumn ? currentColumn.name : "";
    }
    var currentValue = null == newValue ? "" : String(newValue).replace(/^\s+|\s+$/g, "");
    if ("CustomerPartsDBRelDirectBuy" == columnName) {
        directBuy = currentValue;
    } else if ("CustomerPartsDBRelNumber" == columnName) {
        customerPartNumber = currentValue;
    } else if ("CustomerPartsDBRelName" == columnName) {
        customerPartName = currentValue;
    } else if ("CustomerPartsDBRelRevision" == columnName) {
        customerPartRevision = currentValue;
    }

    if (("consignment" == directBuy || "direct-buy" == directBuy)
            && (!customerPartNumber || !customerPartName || !customerPartRevision)) {
        return getEBOMCustomerPartsDBApplyValidationMessage(getJFCustomerPartsDBMessage(
                "emxProgramCentral.JFCustomerPartsDB.RequiredWhenDirectBuy",
                "DirectBuy为consignment、direct-buy时，客户零件号、客户零件名称、客户零件版本必填。"));
    }
    return "";
}

