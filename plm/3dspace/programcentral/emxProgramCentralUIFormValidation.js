//=================================================================
// JavaScript emxUIFormValidation.js
//
// Copyright (c) 1992-2020 Dassault Systemes.
// All Rights Reserved.
// This program contains proprietary and trade secret information of MatrixOne,Inc.
// Copyright notice is precautionary only
// and does not evidence any actual or intended publication of such program
//=================================================================
// emxProgramCentralUIFormValidation.js
// This file is used to add any validation routines to be used by the UIForm component
//-----------------------------------------------------------------

// [Added::Jan 19, 2011:S4E:R211:TypeAhead::Start]
//This functions will be executed when user changes Resource Pool field,
//Preferred person and Business skill will become blank when Resource pool is changed
//Added to Change the fields of form according to selected Quality data types..
function ChangeQualityType() {

    var objectId = document.forms[0].objectId.value;
    var timeStamp = document.forms[0].timeStamp.value;
    var DataTypeElement = document.getElementById("DataTypeId").value;

    if ("Continuous" == DataTypeElement) {
        var href = "../common/emxCreate.jsp?type=type_Quality&relationship=relationship_Quality&form=PMCProjectQualityCreateForm&policy=policy_Quality&header=emxProgramCentral.Common.Quality&postProcessJPO=emxQuality:createQualityProcess&isContinuous=true&nameField=both&autoNameChecked=true&suiteKey=ProgramCentral&StringResourceFileId=emxProgramCentralStringResource&SuiteDirectory=programcentral&submitAction=doNothing&postProcessURL=../programcentral/emxProgramCentralUtil.jsp?mode=refreshQualityPage&targetLocation=slidein&openerFrame=detailsDisplay&objectId=" + objectId + "&timeStamp=" + timeStamp + "";

        window.location.href = href;
    }
    else if ("Discrete" == DataTypeElement) {
        var href = "../common/emxCreate.jsp?type=type_Quality&relationship=relationship_Quality&form=PMCProjectQualityCreateForm&policy=policy_Quality&header=emxProgramCentral.Common.Quality&postProcessJPO=emxQuality:createQualityProcess&isContinuous=false&nameField=both&autoNameChecked=true&suiteKey=ProgramCentral&StringResourceFileId=emxProgramCentralStringResource&SuiteDirectory=programcentral&submitAction=doNothing&postProcessURL=../programcentral/emxProgramCentralUtil.jsp?mode=refreshQualityPage&targetLocation=slidein&openerFrame=detailsDisplay&objectId=" + objectId + "&timeStamp=" + timeStamp + "";

        window.location.href = href;

    }
}
//Added to validate field DPU of Quality form
function validateDPU() {
    var DPU = document.forms[0].DPU;
    if (isNaN(DPU.value)) {
        var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessageWithPlaceHolder&msgValue=DPU&errorKey=emxProgramCentral.Validate.ValidateValues";
        var errorMessage = emxUICore.getData(errorUrl);
        alert(errorMessage);
        return false;
    }
    else if (DPU.value < 0) {
        var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.Validate.ValidateDPU";
        var errorMessage = emxUICore.getData(errorUrl);
        alert(errorMessage);
        return false;
    }
    else if (DPU.value > 2147483647) {
        var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessageWithPlaceHolder&msgValue=DPU&errorKey=emxProgramCentral.Validate.ValueLessThan";
        var errorMessage = emxUICore.getData(errorUrl);
        alert(errorMessage);
        return false;
    } else {
        return true;
    }
}
//Added to validate field DPMO of Quality form
function validateDPMO() {
    var DPMO = document.forms[0].DPMO;

    if (isNaN(DPMO.value)) {
        var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessageWithPlaceHolder&msgValue=DPMO&errorKey=emxProgramCentral.Validate.ValidateValues";
        var errorMessage = emxUICore.getData(errorUrl);
        alert(errorMessage);
        return false;
    }
    else if (DPMO.value > 2147483647) {
        var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessageWithPlaceHolder&msgValue=DPMO&errorKey=emxProgramCentral.Validate.ValueLessThan";
        var errorMessage = emxUICore.getData(errorUrl);
        alert(errorMessage);
        return false;
    } else {
        return true;
    }
}
//Added to validate field Sigma of Quality form
function validateSigma() {
    var Sigma = document.forms[0].Sigma;
    if (isNaN(Sigma.value)) {
        var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessageWithPlaceHolder&msgValue=Sigma&errorKey=emxProgramCentral.Validate.ValidateValues";
        var errorMessage = emxUICore.getData(errorUrl);
        alert(errorMessage);
        return false;
    }
    else if (Sigma.value > 2147483647) {
        var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessageWithPlaceHolder&msgValue=Sigma&errorKey=emxProgramCentral.Validate.ValueLessThan";
        var errorMessage = emxUICore.getData(errorUrl);
        alert(errorMessage);
        return false;
    } else {
        return true;
    }
}

//Added to validate fields Mean,StandardDeviation,UpperSpecificationLimit, LowerSpecificationLimit,of Quality form
function validateContinuousFields() {
    var Mean = document.forms[0].Mean.value;
    var StandardDeviation = document.forms[0].StandardDeviation.value;
    var UpperSpecificationLimit = document.forms[0].UpperSpecificationLimit.value;
    var LowerSpecificationLimit = document.forms[0].LowerSpecificationLimit.value;
    var continuousFieldvalues = new Array(Mean, StandardDeviation, UpperSpecificationLimit, LowerSpecificationLimit);
    var continuousFieldNames = new Array("Mean", "Standard Deviation", "Upper Spec Limit", "Lower Spec Limit");
    for (var i = 0; i < continuousFieldvalues.length; i++) {
        var value = continuousFieldvalues[i];

        if (isNaN(value)) {
            alert(continuousFieldNames[i] + " Value must be a Real Number.");
            return false;
        }
        else if (value > 2147483647) {
            alert(continuousFieldNames[i] + " Value must be smaller than 2^31 - 1");
            return false;
        }
    }
    return true;
}

function clearPreferredPersons() {
    document.forms[0].PreferredPersonDisplay.value = "";
    document.forms[0].PreferredPersonOID.value = "";
    document.forms[0].PreferredPerson.value = "";
}
//[Added::Jan 19, 2011:S4E:R211:TypeAhead::End]


//added for Risk Configurable component V6R2008-1

function modifyRPNValueForRisk() {
    var Impact = document.forms[0].Impact.value;
    var Probability = document.forms[0].Probability.value;
    document.forms[0].RPN.value = Impact * Probability;
}


function getSearchData() {
    var objectId = document.getElementById('myParentId').value;
    var href = "../programcentral/emxProgramCentralAddProjectContentPreProcess.jsp?objectId=" + objectId + "&back=true";

    for (var i = 0; i < document.forms[0].elements.length; i++) {
        //Added one more condition to cut short length of url as it is constraint
        if (document.forms[0].elements[i].name != null && document.forms[0].elements[i].name != "" && document.forms[0].elements[i].name != "emxTableRowIdActual" && document.forms[0].elements[i].name != "RequestValuesMap") {
            href = href + "&" + document.forms[0].elements[i].name + "=" + document.forms[0].elements[i].value;
        }
    }
    //added for the bug 340320
    if (isIE) {
        window.open('', '_parent', '');
    }
    //till here
    getTopWindow().location.href = href;

}

// added for to nullify the "ProgramName" field.
function removeProgram() {
    document.forms[0].ProgramId.value = null;
    document.forms[0].ProgramName.value = "";
    //return true;    /* removed return as firefox was showing 'true' on UI after using this function*/
}

// added for to nullify the "BusinessUnitName" field.
function removeBusinessUnit() {
    document.forms[0].BusinessUnitId.value = null;
    document.forms[0].BusinessUnitName.value = "";
    //return true;   /* removed return as firefox was showing 'true' on UI after using this function*/
}

function validateStartEndDate() {

    var strEstimatedEndDate = document.forms[0].EstimatedEndDate.value;
    var fieldEstimatedEndDate = new Date(strEstimatedEndDate);

    var strEstimatedStartDate = document.forms[0].EstimatedStartDate.value;
    var fieldEstimatedStartDate = new Date(strEstimatedStartDate);

    if (fieldEstimatedStartDate.getTime() > fieldEstimatedEndDate.getTime()) {
        var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.Import.EndDateBeforeStartDate";
        var errorMessage = emxUICore.getData(errorUrl);
        alert(errorMessage);
        document.forms[0].EstimatedEndDate.value = "";
        document.forms[0].EstimatedStartDate.value = "";
        return false;
    }
    return true;
}

// Added:24-Mar-109:nzf:R207:PRG:Bug:366902
function isBadNameChars() {
    var nameField = this;
    if (arguments[0]) {
        nameField = arguments[0];
    }
    var isBadNameChar = checkForNameBadCharsList(nameField);
    if (isBadNameChar.length > 0) {
        alert(BAD_NAME_CHARS + isBadNameChar);
        return false;
    }
    var fieldValue = nameField.value;
    var hastabs = hasTabSpace(fieldValue);
    if (hastabs) {
        alert(BAD_NAME_CHARS + "\n" + " tab space");
        return false;
    }
    return true;
}
// End:R207:PRG:Bug:366902


function validateNameField() {
    var validName = isBadNameChars(this);
    if (validName) {
        var fieldValue = this.value;
        var lengthCheck = checkValidLength(fieldValue); //emxJSValidationUtil.js : function checkValidLength(str,type)
        if (!(lengthCheck)) {
            alert(emxUIConstants.STR_NAMECOLUMN);
            return false;
        }
    }
    else {
        return false;
    }
    return true;
}


function hasTabSpace(nameString) {
    var regExp = /\t/;
    return regExp.test(nameString);
}

function selectAssociatedPlanOnDeliverableTemplate() {
    emxFormReloadField('Plan');
}

function selectResourcePoolPeople() {//Modified function :24-Mar-109:di1:R210:ARP feature
    var ResourcePoolId = document.forms[0].ResourcePoolOID.value;
    if (null == ResourcePoolId || "" == ResourcePoolId) {
        if (ResourcePoolId.length == 0) {
            alert("Please select Resource Pool");
            //Added:PRG:I16:R213:IR-131272V6R2013 Start
            //var errBegin = "<emxUtil:i18nScript localize="i18nId">emxProgramCentral.ResourcePool.SelectPerson</emxUtil:i18nScript>";
            //alert(errBegin);
            //Added:PRG:I16:R213:IR-131272V6R2013 End
            return;
        }
    }

    if (ResourcePoolId == null || ResourcePoolId == "" || ResourcePoolId == "null") {
        alert("Please select Resource Pool");
        //Added:PRG:I16:R213:IR-131272V6R2013 Start
        //var errBegin = "<emxUtil:i18nScript localize="i18nId">emxProgramCentral.ResourcePool.SelectPerson</emxUtil:i18nScript>";
        //alert(errBegin);
        //Added:PRG:I16:R213:IR-131272V6R2013 End
        return;
    }

    var href = "../common/emxFullSearch.jsp?field=TYPES=type_Person&table=AEFGeneralSearchResults&cancelLabel=emxProgramCentral.Common.Close&selection=multiple&objectId=" + ResourcePoolId + "&excludeOIDprogram=emxResourceRequest:getExcludeOIDforResourcePoolPerson&submitURL=../programcentral/emxProgramCentralResourceRequestAutonomySearchSelect.jsp&fieldNameActual=PreferredPerson&fieldNameDisplay=PreferredPersonDisplay&fieldNameOID=PreferredPersonOID&suiteKey=ProgramCentral";
    showChooser(href, 700, 500);

}

//Added function :26-Oct-10:s2e:R210  IR: IR-070493V6R2012
function selectResourcePoolPreferredPerson() {
    var ResourcePoolId = document.forms[0].ResourcePoolOID.value;
    if (null == ResourcePoolId || "" == ResourcePoolId) {
        if (ResourcePoolId.length == 0) {
            alert("Please select Resource Pool");
            //Added:PRG:I16:R213:IR-131272V6R2013 Start
            //var errBegin = "<emxUtil:i18nScript localize="i18nId">emxProgramCentral.ResourcePool.SelectPerson</emxUtil:i18nScript>";
            //alert(errBegin);
            //Added:PRG:I16:R213:IR-131272V6R2013 End
            return;
        }
    }
    if (ResourcePoolId == null || ResourcePoolId == "" || ResourcePoolId == "null") {
        alert("Please select Resource Pool ");
        //Added:PRG:I16:R213:IR-131272V6R2013 Start
        //var errBegin = "<emxUtil:i18nScript localize="i18nId">emxProgramCentral.ResourcePool.SelectPerson</emxUtil:i18nScript>";
        //alert(errBegin);
        //Added:PRG:I16:R213:IR-131272V6R2013 End
        return;
    }
    //var href = "../common/emxFullSearch.jsp?field=TYPES=type_Person&table=PMCCommonPersonSearchTable&cancelLabel=emxProgramCentral.Common.Close&selection=multiple&objectId="+ResourcePoolId+"&excludeOIDprogram=emxResourceRequest:getExcludeOIDforResourcePoolPerson&form=PMCCommonPersonSearchForm&submitURL=../programcentral/emxProgramCentralResourceRequestAutonomySearchSelect.jsp&fieldNameActual=PreferredPerson&fieldNameDisplay=PreferredPersonDisplay&fieldNameOID=PreferredPersonOID&suiteKey=ProgramCentral";
    var href = "../common/emxFullSearch.jsp?field=TYPES=type_Person:POLICY=policy_Person:RESOURCE_POOL_ID=" + ResourcePoolId + "&table=PMCCommonPersonSearchTable&cancelLabel=emxProgramCentral.Common.Close&selection=multiple&objectId=" + ResourcePoolId + "&excludeOIDprogram=emxResourcePool:getExcludeOIDForAddResourcesSearch&form=PMCCommonPersonSearchForm&submitURL=../programcentral/emxProgramCentralResourceRequestAutonomySearchSelect.jsp&fieldNameActual=PreferredPerson&fieldNameDisplay=PreferredPersonDisplay&fieldNameOID=PreferredPersonOID&suiteKey=ProgramCentral";
    showChooser(href, 700, 500);
}
//End :26-Oct-10:s2e:R210  IR: IR-070493V6R2012

function selectResourcePoolSkill(businessSkill1, businessSkill2, businessSkill3, resourcePool) {
    var href = "../common/emxFullSearch.jsp?field=TYPES=type_BusinessSkill&table=PMCBusinessSkillSearchSummary&cancelLabel=emxProgramCentral.Common.Close&selection=single&submitURL=../programcentral/emxProgramCentralResourceRequestAutonomySearchSelect.jsp&fieldNameActual=" + businessSkill1 + "&fieldNameDisplay=" + businessSkill2 + "&fieldNameOID=" + businessSkill3 + "&suiteKey=ProgramCentral";
    showChooser(href, 700, 500);
    // End:27-Apr-R212:MS9:PRG:IR-103943V6R2012x
    //End:04-Feb-R211:hp5:PRG:IR-046641V6R2012
}



//Added For IR-033739V6R2011
//Added:4-Oct-2010:vf2:R2012
//document.write("<script src='../common/scripts/emxUICore.js' type='text/javascript'></script>");
//End:4-Oct-2010:vf2:R2012
var CHANGE_PROJECT = "Change Project";
function validationForAddProgram() {
    var document = getTopWindow().document;
    //if(null!=frameobj.document.forms[0].ActualType && frameobj.document.forms[0].ProgramId.value =="" && frameobj.document.forms[0].ActualType.value == CHANGE_PROJECT){
    if (null != document.forms[0].ActualType && document.forms[0].ProgramId.value == "" && document.forms[0].ActualType.value == CHANGE_PROJECT) {
        alert("Please enter a Program");
        return false;
    } else {
        return true;
    }
}
//End For IR-033739V6R2011
//Added:28-May-2010:s4e:R210 PRG:ARP
function validateStandardCost() {
    var standardCostValue = document.forms[0].StandardCost.value;
    if (isNaN(standardCostValue)) {
        alert("Please enter numeric value for Standard Cost");
        //Added:PRG:I16:R213:IR-131272V6R2013 Start
        //var errBegin = "<emxUtil:i18nScript localize="i18nId">emxProgramCentral.StandardCost.EnterNumericValue</emxUtil:i18nScript>";
        //alert(errBegin);
        //Added:PRG:I16:R213:IR-131272V6R2013 End
        return false;
    }
    else if (parseInt(standardCostValue, 10) < 0) {
        alert("Please enter positive value for Standard Cost");
        //Added:PRG:I16:R213:IR-131272V6R2013 Start
        //var errBegin = "<emxUtil:i18nScript localize="i18nId">emxProgramCentral.StandardCost.EnterPositiveValue</emxUtil:i18nScript>";
        //alert(errBegin);
        //Added:PRG:I16:R213:IR-131272V6R2013 End
        return false;
    }
    else {
        return true;
    }

}
//End:28-May-2010:s4e:R210 PRG:ARP
//Added:13-Aug-2010:rz1:R210 PRG for External IR-063823V6R2011x bug
function validateNumberofPerson() {
    var NumberofPersonvalue = document.forms[0].NumberOfPeople.value;
    //MODIFIED:PA4:25-Aug-2011:IR-123555V6R2012x
    if (isNaN(NumberofPersonvalue) || (parseFloat(NumberofPersonvalue, 10) < 1)) {
        var key = "emxProgramCentral.Validate.ValidatePeople";
        var strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=translateErrorMsg&key=" + key;
        var responseText = emxUICore.getData(strURL);
        var responseJSONObject = emxUICore.parseJSON(responseText);
        var errorMsg = responseJSONObject["Error"];
        alert(errorMsg);
        return false;
    }
    else {
        return true;
    }
}

//END:13-Aug-2010:rz1:R210 PRG External IR-063823V6R2011x


function validateResourceRequestName() {
    var RequestName = document.forms[0].Name.value;
    var badChars = checkForNameBadCharsList(document.getElementById("Name"));
    if (badChars.length > 0) {
        alert("The following characters are invalid:\n" + badChars + "\nPlease remove the invalid character from Name Field");
        //Added:PRG:I16:R213:IR-131272V6R2013 Start
        //var errBegin = "<emxUtil:i18nScript localize="i18nId">emxProgramCentral.Common.InvalidCharacters</emxUtil:i18nScript>";
        //var errEnd = "<emxUtil:i18nScript localize="i18nId">emxProgramCentral.Common.RemoveInvalidCharacter</emxUtil:i18nScript>";
        //alert(errBegin+" "+badChars+" "+errEnd);
        //Added:PRG:I16:R213:IR-131272V6R2013 End
        return false;
    }
    return true;
}

// [ADDED::PRG:RG6:Feb 25, 2011:IR-094808V6R2012x :R211::Start]
function validateWBSTaskName() {
    var taskNameField = document.forms[0].Name;
    var badChars = checkForNameBadCharsList(taskNameField);
    if (badChars.length > 0) {
        alert("The following characters are invalid:\n" + badChars + "\nPlease remove the invalid character from Name Field");
        //Added:PRG:I16:R213:IR-131272V6R2013 Start
        //var errBegin = "<emxUtil:i18nScript localize="i18nId">emxProgramCentral.Common.InvalidCharacters</emxUtil:i18nScript>";
        //var errEnd = "<emxUtil:i18nScript localize="i18nId">emxProgramCentral.Common.RemoveInvalidCharacter</emxUtil:i18nScript>";
        //alert(errBegin+" "+badChars+" "+errEnd);
        //Added:PRG:I16:R213:IR-131272V6R2013 End
        return false;
    }
    return true;
}
//[ADDED::PRG:RG6:Feb 25, 2011:IR-094808V6R2012x :R211::End]

//ADDED for IR-030459V6R2013x
function validateWBSTaskConstraintDate() {

    var taskTypeElement = document.getElementById("Task Constraint Type").value;
    //IR-196805V6R2014: Date value is coming as NaN in FF for non English language. Manual entry is not allowed so entered value will be always date string,
    //so NaN check is not required. Need to check constaint date value is set or not by comparing with "".
    //var taskDateElement = new Date(document.getElementById("TaskConstraintDate").value);

    //if(taskTypeElement!= "As Soon As Possible" && taskTypeElement != "As Late As Possible" && isNaN(taskDateElement.getTime()))
    if (taskTypeElement != "As Soon As Possible" && taskTypeElement != "As Late As Possible" && document.getElementById("TaskConstraintDate").value == "") {
        alert("Please select the Task Constraint Date");
        return false;
    }
    return true;
}

//Added for the IR-221588V6R2014 to check both Badchars and characters length.
function checkFormFieldLength(fieldObj) {
    var charset = document.charset;
    var chars = fieldObj.value;
    var fieldLengthMax = 127;
    var i;
    var asciiCount = 0;
    var unicodeCount = 0;
    for (i = 0; i < chars.length; i++) {
        var charValue = chars.charCodeAt(i);
        if (charValue < 256) {
            asciiCount++;
        } else {
            unicodeCount++;
        }
    }

    var byteCount = (unicodeCount * 3) + asciiCount;
    if ((charset != null || charset != undefined) && charset == 'Shift_JIS') {
        byteCount = (unicodeCount * 2) + asciiCount;
    }

    if (byteCount > fieldLengthMax) {
        return "Fail";
    } else {
        return "Pass";
    }
}

function validateBadCharsNlength() {
    var isBadNameChar = checkForNameBadCharsList(this);
    if (isBadNameChar.length > 0) {
        alert(BAD_NAME_CHARS + isBadNameChar);
        return false;
    } else {
        var charLength = checkFormFieldLength(this);
        if (charLength == "Fail") {
            var timestamp = new Date().getTime();
            var url = "../programcentral/emxProgramCentralUIFormValidation.jsp?strmode=maxLengthError&timestamp=" + timestamp;
            var strMaxLength = emxUICore.getData(url);
            alert(strMaxLength);
            return false;
        } else
            return true;
    }
}
function validateExpIssueDate() {
    var isValidDate = true;
    var formName = document.emxCreateForm;

    if ((formName.EstimatedStartDate.value != "" && formName.EstimatedStartDate.value != null) && (formName.EstimatedFinishDate.value != "" && formName.EstimatedFinishDate.value != null)) {
        if (formName.EstimatedStartDate_msvalue.value > formName.EstimatedFinishDate_msvalue.value) {
            alert("Invalid Date:\n Estimated Start Date should not be later than Estimated Finish Date.\n Please enter valid dates.");
            isValidDate = false;
        } else {
            isValidDate = true;
        }
    } else if (null == formName.EstimatedFinishDate.value || "" == formName.EstimatedFinishDate.value) {
        var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramcentral.ProjectIssue.EnterFinishDate";
        var errorMessage = emxUICore.getData(errorUrl);
        alert(errorMessage);
        //alert("Must enter a valid value for Estimated Finish Date");
        isValidDate = false;
    } else {
        isValidDate = false;
    }

    if (isValidDate) {
        return true;
    } else {
        return false;
    }
}

//Synchronization
function syncRows(dir) {

    if (portalMode == "true") {
        toggleProgress("visible");
    } else {
        turnOnProgress();
    }

    setTimeout(function () {
        syncRowsTimeout(dir);
    }, 20);
}

function pastedAboveAlready() {
    if (typeof dataGridEnabled != 'undefined' && dataGridEnabled == true) {
        editableTable.pasteAbove();
    }
    else {
        if ("cut" === lastOperation) {
            var copiedRows = new Array();
            for (var i = 0; i < aCopiedRowsChecked.length; i++) {
                var copiedId = aCopiedRowsChecked[i].getAttribute("o");
                copiedRows.push(copiedId);
            }
            var pastedAlready = false;
            for (var i = 0; i < aPastedRowsChecked.length; i++) {
                var rowId = aPastedRowsChecked[i].getAttribute("id");
                var parentRowId = rowId.substr(0, rowId.lastIndexOf(","));
                var parentRow = emxUICore.selectSingleNode(oXML, "/mxRoot/rows//r[@id='" + parentRowId + "']");
                var subTasks = emxUICore.selectNodes(parentRow, "r");
                for (var i = 0; i < subTasks.length; i++) {
                    var subTaskId = subTasks[i].getAttribute("o");
                    if (copiedRows.indexOf(subTaskId) > -1) {
                        pastedAlready = true;
                        break;
                    }
                }
                if (pastedAlready) {
                    return 1;
                }
            }
        }
        return 0;
    }

}

function pastedBelowAlready() {
    if (typeof dataGridEnabled != 'undefined' && dataGridEnabled == true) {
        editableTable.pasteRowsBelow();
    }
    else {

        if ("cut" === lastOperation) {
            var copiedRows = new Array();
            for (var i = 0; i < aCopiedRowsChecked.length; i++) {
                var copiedId = aCopiedRowsChecked[i].getAttribute("o");
                copiedRows.push(copiedId);
            }
            var pastedAlready = false;
            for (var i = 0; i < aPastedRowsChecked.length; i++) {
                var subTasks = emxUICore.selectNodes(aPastedRowsChecked[i], "r");
                for (var i = 0; i < subTasks.length; i++) {
                    var subTaskId = subTasks[i].getAttribute("o");
                    if (copiedRows.indexOf(subTaskId) > -1) {
                        pastedAlready = true;
                        break;
                    }
                }
                if (pastedAlready) {
                    return 1;
                }
            }
        }
        return 0;
    }

}

function pastePreProcess() {
    if ("pasteTaskAbove" === arguments[0] && pastedAboveAlready() === 1) {
        return 1;
    } else if (pastedBelowAlready() === 1) {
        return 1;
    }

    var objIds = new Array();
    var rowIds = new Array();
    var checkboxes = getCheckedCheckboxes();
    var objIds = new Array();
    var isValidOperation = false;
    for (var e in checkboxes) {
        var aId = e.split("|");
        var objId = aId[1];
        objIds.push(objId);
        isValidOperation = true;
    }
    if (!isValidOperation) {
        alert(emxUIConstants.STR_SBEDIT_NO_SELECT_NODES);
        return true;
    }
    var strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=pastePreProcess&objIds=" + objIds;
    if ("pasteTaskAbove" === arguments[0]) {
        strURL += "&pasteTaskAbove=true";
    }
    var responseText = emxUICore.getData(strURL);
    if (responseText !== "{}") {
        var responseJSONObject = emxUICore.parseJSON(responseText);
        var message = responseJSONObject["error"];
        var sInvalidTask = responseJSONObject["invalidTasks"];
        alert(message);
        return 1;
    }
    return 0;
}

function pasteTaskAbove() {
    if (typeof dataGridEnabled != 'undefined' && dataGridEnabled == true) {
        editableTable.pasteAbove();
    }
    else {
        if (pastePreProcess("pasteTaskAbove") === 1) return;

        // RPL5-added for performance to avoid additional rebuild view in BPS function
        var rebuildViewInProcess = true;
        var syncSBInProcess = true;

        pasteAbove();

        var aRows = emxUICore.selectNodes(oXML, "/mxRoot/rows//r[@status ='cut' or @status='add' or @status='resequence'or @status='changed']");

        // RPL5- Logic to make newly added row non-editable
        for (var i = 0; i < aRows.length; i++) {
            var row = aRows[i];
            row.setAttribute("display", "none");
            //MSE13-to disable selection of newly added row before saving
            if ("cut" != row.getAttribute("status"))
                row.setAttribute("disableSelection", "true");
            // RPL5-added to disable expand in for newly added row
            row.setAttribute("disableExpand", "true");
            var nColumns = row.getElementsByTagName("c");
            for (var itr = 0; itr < nColumns.length; itr++) {
                var columnInfo = nColumns[itr];
                columnInfo.setAttribute("editMask", "false");
            }
        }
        var rebuildViewInProcess = false;
        var syncSBInProcess = false;
        rebuildView();
    }

}

function pasteTaskBelow() {
    if (typeof dataGridEnabled != 'undefined' && dataGridEnabled == true) {
        editableTable.pasteAsChild();
    }
    else {
        if (pastePreProcess() === 1) return;

        // RPL5-added for performance to avoid additional rebuild view in BPS function
        var rebuildViewInProcess = true;
        var syncSBInProcess = true;

        pasteAsChild();

        var aRows = emxUICore.selectNodes(oXML, "/mxRoot/rows//r[@status ='cut' or @status='add' or @status='resequence'or @status='changed']");

        // RPL5- Logic to make newly added row non-editable
        for (var i = 0; i < aRows.length; i++) {
            var row = aRows[i];
            row.setAttribute("display", "none");
            //MSE13-to disable selection of newly added row before saving
            if ("cut" != row.getAttribute("status"))
                row.setAttribute("disableSelection", "true");
            // RPL5-added to disable expand in for newly added row
            row.setAttribute("disableExpand", "true");
            var nColumns = row.getElementsByTagName("c");
            for (var itr = 0; itr < nColumns.length; itr++) {
                var columnInfo = nColumns[itr];
                columnInfo.setAttribute("editMask", "false");
            }
        }
        var rebuildViewInProcess = false;
        var syncSBInProcess = false;
        rebuildView();
    }

}

function cutPreProcess() {
    var isValidOperation = false;
    var checkboxes = getCheckedCheckboxes();
    var objIds = new Array();
    var rowIds = new Array();
    for (var e in checkboxes) {
        var aId = e.split("|");
        var objId = aId[1];
        var rowId = aId[3];
        objIds.push(objId);
        rowIds.push(rowId);
        isValidOperation = true;
    }

    if (!isValidOperation) {
        alert(emxUIConstants.STR_SBEDIT_NO_SELECT_NODES);
        return true;
    }

    var strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=cutPreProcess&objIds=" + objIds;
	if (typeof dataGridEnabled != 'undefined' && dataGridEnabled == true) {
        strURL = "../../programcentral/emxProgramCentralUtil.jsp?mode=cutPreProcess&objIds=" + objIds;
    }
    var responseText = emxUICore.getData(strURL);
    if (responseText !== "{}") {
        var responseJSONObject = emxUICore.parseJSON(responseText);
        var message = responseJSONObject["error"];
        var sInvalidTasks = responseJSONObject["invalidTasks"];
        var invalidTasks = sInvalidTasks.split(",");
        for (var index = 0; index < invalidTasks.length; index++) {
            var invalidTaskId = invalidTasks[index];
            var idx = objIds.indexOf(invalidTaskId);
            var invalidRowId = rowIds[idx];
            if (typeof dataGridEnabled == 'undefined') {
                var rowObj = getNode(oXML.documentElement, "/mxRoot/rows//r[@id='" + invalidRowId + "']");
                rowObj.setAttribute("checked", "");
            }
            else if (typeof dataGridEnabled != 'undefined' && dataGridEnabled == true) {
                var rowObj = emxEditableTable.getNodeFromAttribute("rowId", invalidRowId);
                emxEditableTable.setAttribute(rowObj, "checked", "");
            }
        }
        alert(message);
        rebuildView();
    }
}

function cutTask() {
    cutPreProcess();
    if (typeof dataGridEnabled != 'undefined' && dataGridEnabled == true) {
        var selectedNodes = emxEditableTable.getSelectedNode();
        for (var i = 0; i < selectedNodes.length; i++) {
            if (emxEditableTable.getAttribute(selectedNodes[i], "checked") == "") {
                emxEditableTable.unselectNode(selectedNodes[i]);
            }
        }
        editableTable.cut();
    }
    else {
        var aCheckedRows = emxUICore.selectNodes(oXML, "/mxRoot/rows//r[@checked='checked']");
        if (aCheckedRows.length > 0) {
            cut();
        }
    }
}

function syncRowsTimeout(dir) {
    var cBoxArray = new Array();
    var checkboxes = getCheckedCheckboxes();

    for (var e in checkboxes) {
        var aId = e.split("|");
        var rowid = aId[3];
        var rowObject = getSelectedRowObjects([rowid])[0];
        var nRows = emxUICore.selectNodes(rowObject, ".//r");
        var matchresult = rowObject.getAttribute("matchresult");
        if (matchresult != 'common' && dir == matchresult) {
            var chkLen = nRows.length;
            for (var j = 0; j < chkLen; j++) {
                var id = nRows[j].getAttribute("id");
                var oid = nRows[j].getAttribute("o");
                var relid = nRows[j].getAttribute("r");
                if (relid == null || relid == "null") {
                    relid = "";
                }
                var parentId = nRows[j].getAttribute("p");
                if (parentId == null || parentId == "null") {
                    parentId = "";
                }
                var totalRowInfo = relid + "|" + oid + "|" + parentId + "|" + id;
                FreezePaneregisterByRowId(id);
                checkboxes[totalRowInfo] = totalRowInfo;
            }
        }
    }

    for (var e in checkboxes) {
        var aId = e.split("|");
        var rowid = aId[3];
        cBoxArray[cBoxArray.length] = rowid;
    }
    var invalidSyncArray = [];
    for (var e in checkboxes) {

        var aId = e.split("|");
        var rowid = aId[3];
        var rowObject = getSelectedRowObjects([rowid])[0];

        var matchResult = rowObject.getAttribute("matchresult");
        var taskName = "";
        if ("common" != parentMatchResult && "parent" != parentMatchResult) {
            var rowColumns = emxUICore.selectNodes(oXML, "/mxRoot/rows//r[@id = '" + rowid + "']/c");
            var columnCount = rowColumns.length;
            var columnHalf = columnCount / 2;

            taskName = trim(emxUICore.getText(rowColumns[0]));
            if (taskName == "" || taskName == null) {
                taskName = trim(emxUICore.getText(rowColumns[columnHalf]));
            }
        }

        var parentMatchResult = rowObject.parentNode.getAttribute("matchresult");
        var parentRowId = rowObject.parentNode.getAttribute("id");
        var isSelected = false;

        if (dir == matchResult) {
            //Do nothing
        } else {
            //#1 use case
            for (var itr = 0; itr < cBoxArray.length; itr++) {
                if (cBoxArray[itr] == parentRowId) {
                    isSelected = true;
                }
            }

            //#2
            if (!isSelected && parentRowId != "0") {
                if ("common" != parentMatchResult && "parent" != parentMatchResult) {
                    var parentRow = emxUICore.selectSingleNode(syncDataXML, "/mxRoot/object/object[@rowId = '" + parentRowId + "']");
                    if (parentRow != null) {
                        var markup = parentRow.getAttribute("markup");
                        if ("cut" == markup) {
                            invalidSyncArray.push(taskName);
                        }
                    } else {
                        invalidSyncArray.push(taskName);
                    }
                }
            }
        }
    }

    if (invalidSyncArray.length > 0) {
        var invalidTaskName = "";
        for (var i = 0; i < invalidSyncArray.length; i++) {
            if (i == 0) {
                invalidTaskName = invalidSyncArray[i];
            } else {
                invalidTaskName += ", " + invalidSyncArray[i];
            }
        }
        var strURL = "../programcentral/emxProgramCentralWhatIfAnalysis.jsp?mode=errorMsg";
        var responseText = emxUICore.getData(strURL);
        var responseJSONObject = emxUICore.parseJSON(responseText);
        var errorMsg = responseJSONObject["Error"];
        alert(errorMsg + " " + invalidTaskName);
    } else {
        if (dir == "left") {
            javascript: synchronize('left');
        } else if (dir == "right") {
            javascript: synchronize('right');
        }

        if (portalMode == "true") {
            setTimeout('toggleProgress("hidden")', 10);
        } else {
            setTimeout("turnOffProgress()", 10);
        }
    }
}

/***************New Project UI scripts**************/
function getFileDetails(mode) {
    var errorMassage;
    var fileInput = document.getElementById('FileFullPath');
    var fileInputName = document.getElementById('FileFullPath').value;
    fileInputName = encodeURIComponent(fileInputName);
    var file = fileInput.files[0];
    var url = "../programcentral/emxProgramCentralUtil.jsp?mode=importProjectProcess&fileName=" + fileInputName;
    if("ProjectTemplateSchedule"==mode){
		url = url +"&importSubProject=false";
		mode = "Schedule";
	}    
turnOnProgress();
    var hasError = false;
    var fileExtension = fileInputName.split(".")[fileInputName.split(".").length - 1];
    if (fileInputName == "") {
        hasError = true;
        turnOffProgress();
        disablePreviewButton();
        var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.Project.NoFileSelected";
        errorMassage = emxUICore.getData(errorUrl);
        alert(errorMassage);
        createImportedFileStatusField(hasError, mode, errorMassage);
    }
    else if (fileExtension.toUpperCase() == "CSV" || fileExtension.toUpperCase() == "TXT" || fileExtension.toUpperCase() == "TEXT") {
        var responseText = getFilePostData(url, file);
        var responseJSONObject = emxUICore.parseJSON(responseText);
        for (var key in responseJSONObject) {

            if (key != "error" && "Schedule" != mode) {
                if (key == "title") {
                    errorMassage = responseJSONObject[key];
                } else {
                    document.getElementsByName(key)[0].value = responseJSONObject[key];
                }
            } else if (key != "error" && "Schedule" == mode && key == "title") {
                errorMassage = responseJSONObject[key];
            } else if (key == "error") {
                errorMassage = responseJSONObject[key];
                alert(errorMassage);
                hasError = true;
                break;
            }

        }
        // Fix for IR-606843 - Added To uncheck the already checked autoname checkbox
        var autoName = document.getElementsByName("autoNameCheck")[0];
        if (autoName && autoName.checked) {
            autoName.click();
        }

        turnOffProgress();
        enablePreviewButton();
        createImportedFileStatusField(hasError, mode, errorMassage);
    }
    else {
        hasError = true;
        turnOffProgress();
        disablePreviewButton();
        var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.Project.FileExtensionNotSupported&fileExtension=" + fileExtension;
        errorMassage = emxUICore.getData(errorUrl);
        alert(errorMassage);
        createImportedFileStatusField(hasError, mode, errorMassage);
    }
}

function createImportedFileStatusField(hasError, mode, errorMassage) {
    var image;
    var title;

    if (hasError) {
        image = "images/iconStatusComplaintUrgent.png";
        title = errorMassage;
        if ("Schedule" != mode) {
            disableImportFormFields();
        }
    } else {
        image = "images/iconStatusComplete.gif";
        title = errorMassage;
        if ("Schedule" != mode) {
            enableImportFormFields();
        }
    }
    var statusField = document.getElementById("status");
    if (statusField != null) {
        statusField.src = image;
        statusField.title = title;
    } else {
        var img = "<img id=\"status\" src=\"" + image + "\" title=\"" + title + "\"/>";
        statusField = document.createElement('td');
        statusField.innerHTML = img;
        var previewNode = document.getElementById("previewFile");
        var nodeToAppend = previewNode.parentNode.parentNode;
        nodeToAppend.appendChild(statusField);
    }

}
function disableAllFields() {
    document.getElementsByName("btnquestions")[0].disabled = true;
    document.getElementsByName("questionsDisplay")[0].value = "No question for respond..";
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

function getPreview() {
    var href = "../common/emxIndentedTable.jsp?table=PMCProjectImportFileViewTable&expandProgram=emxProjectSpace:getImportObjectList&suiteKey=ProgramCentral&SuiteDirectory=programcentral&StringResourceFileId=emxProgramCentralStringResource&emxSuiteDirectory=programcentral&freezePane=Name&hideHeader=true&customize=false&rowGrouping=false&showPageURLIcon=false&hideLaunchButton=true&export=false&displayView=details&export=false&hideRootSelection=true&objectCompare=false&showClipboard=false&multiColumnSort=false&findMxLink=false&showRMB=false&massPromoteDemote=false&expandLevelFilter=false&cellwrap=false&sortColumnName=Level&parallelLoading=true&maxCellsToDraw=5000&scrollPageSize=60&PrinterFriendly=false";
    showModalDialog(href);
}

function predictTemplateWBS() {
    var searchProjectTemplateId = document.getElementsByName("SeachProjectOID")[0].value;
    var href = "../common/emxIndentedTable.jsp?table=PMCProjectSummaryForProjects&sortColumnName=taskId&expandProgram=emxProjectSpace:getTemplateWBS&suiteKey=ProgramCentral&SuiteDirectory=programcentral&StringResourceFileId=emxProgramCentralStringResource&emxSuiteDirectory=programcentral&freezePane=Name&hideHeader=true&customize=false&rowGrouping=false&showPageURLIcon=false&hideLaunchButton=true&export=false&displayView=details&export=false&hideRootSelection=true&objectCompare=false&showClipboard=false&multiColumnSort=false&findMxLink=false&showRMB=false&massPromoteDemote=false&expandLevelFilter=false&objectId=" + searchProjectTemplateId;
    showModalDialog(href);
}
function previewTemplateTasksWBS() {
    var href = "../common/emxIndentedTable.jsp?table=PMCProjectSummaryForProjects&program=emxProjectTemplate:getTemplateTasksWBS&expandProgram=emxProjectSpace:getTemplateWBS&suiteKey=ProgramCentral&SuiteDirectory=programcentral&StringResourceFileId=emxProgramCentralStringResource&emxSuiteDirectory=programcentral&freezePane=Name&hideHeader=true&customize=false&rowGrouping=false&showPageURLIcon=false&hideLaunchButton=true&export=false&displayView=details&export=false&hideRootSelection=true&objectCompare=false&showClipboard=false&multiColumnSort=false&findMxLink=false&showRMB=false&massPromoteDemote=false&expandLevelFilter=false";
    showModalDialog(href);
}
function getExistingProjectInfo() {
    var searchProjectId = document.getElementsByName("SeachProjectOID")[0].value;

    if (searchProjectId != null && searchProjectId != "" && searchProjectId != "undefined") {
        var strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=searchProjectData&searchProjectId=" + searchProjectId;

        var responseText = emxUICore.getData(strURL);
        var responseJSONObject = emxUICore.parseJSON(responseText);

        for (var i = 0; i < responseJSONObject.length; i++) {

            for (var key in responseJSONObject[i]) {
                if (document.getElementsByName(key)[0] != null && document.getElementsByName(key)[0] != 'undefined') {
                    if (key == "description" && responseJSONObject[i][key] == null) {
                        document.getElementsByName(key)[0].value = "";
                    } 
                    else if ( key == "folders") {
						if(responseJSONObject[i][key] != ""){
							// document.getElementsByName(key)[0].disabled = true;
							document.getElementsByName(key)[0].checked = true;
						}
						else{
							// document.getElementsByName(key)[0].disabled = false;
							document.getElementsByName(key)[0].checked = true;
						}
					} 		
					else {
                        if (!(key == "Policy" && responseJSONObject[i][key] == "Project Space Hold Cancel")) {
                            document.getElementsByName(key)[0].value = responseJSONObject[i][key];
                        }
                    }
                }
            }
        }

    } else {
        setDefaultValue();
    }
}
function setFocusOnNameField() {
    document.getElementById("Name").focus();
document.getElementsByName("autoNameCheck")[0].disabled=true;// add by caipan
}
function setFocusOnSearchField() {
    document.getElementsByName("SeachProjectDisplay")[0].focus();
}
function setDefaultValue() {
    document.getElementById("Name").value = "";
    document.getElementById("descriptionId").value = "";

    var strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=getCurrentDate";
    var responseText = emxUICore.getData(strURL);
    var responseJSONObject = emxUICore.parseJSON(responseText);
    for (var key in responseJSONObject) {
        document.getElementsByName(key)[0].value = responseJSONObject[key];
    }
    //Program
    //Chnages for IR-681315 - To avoid the null pointer exception at runtime
    var programDisplay = document.getElementsByName("ProgramDisplay");
    if (programDisplay.length != 0) {
        programDisplay[0].value = "";
    }
    var program = document.getElementsByName("Program");
    if (program.length != 0) {
        program[0].value = "";
    }
    var programOID = document.getElementsByName("ProgramOID");
    if (programOID.length != 0) {
        programOID[0].value = "";
    }
    //BU
    document.getElementsByName("BusinessUnitDisplay")[0].value = "";
    document.getElementsByName("BusinessUnit")[0].value = "";
    document.getElementsByName("BusinessUnitOID")[0].value = "";

    //BG
    var bg = document.getElementsByName("BusinessGoal")[0];
    if (bg != null && bg != 'undefined') {
        document.getElementsByName("BusinessGoalDisplay")[0].value = "";
        document.getElementsByName("BusinessGoal")[0].value = "";
        document.getElementsByName("BusinessGoalOID")[0].value = "";
    }

    //Question
    var question = document.getElementsByName("questions")[0];
    if (question != null && question != 'undefined') {
        disableQuestionField();
    }

    //Predict WBS
    var preview = document.getElementsByName("predictWBS")[0];
    if (preview != null && preview != 'undefined') {
        disableTemplatePredictWBS();
    }

    //Disaable Resource Template Field
    ResourceTemplate = document.getElementsByName("ResourceTemplate")[0];
    if (ResourceTemplate != null && ResourceTemplate != 'undefined') {
        ResourceTemplate.options.length = 0;
        disableRT();
    }
    document.getElementsByName("SeachProjectDisplay")[0].focus();
}

function getSelectedProjectTemplateScheduleDetails(searchProjectTemplateId) {

    var strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=searchProjectData&searchProjectId=" + searchProjectTemplateId;

    var responseText = emxUICore.getData(encodeURI(strURL));
    var responseJSONObject = emxUICore.parseJSON(responseText);
    var selectedProjectTemplate = document.getElementsByName("SeachProjectDisplay")[0].value;
	if (responseJSONObject[0].state != "Released" && responseJSONObject[0].KindOfProjectTemplate=="TRUE") {
	
	var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessageWithPlaceHolder&msgValue="+selectedProjectTemplate+"&errorKey=emxProgramCentral.ProjectTemplate.SelectedTemplateStateValidation";
   	var errorMessage = emxUICore.getData(errorUrl);
    alert(errorMessage);

     basicClear('SeachProject');
     return;
	}
    for (var i = 0; i < responseJSONObject.length; i++) {

        for (var key in responseJSONObject[i]) {
            if (key == "Question" && responseJSONObject[i][key] == "true") {
                enableQuestionField();
                break;
            }
        }
    }

}
function getSelectedProjectScheduleDetails() {

    var searchProjectTemplateId = document.getElementsByName("SeachProjectOID")[0].value;

    if (searchProjectTemplateId != null && searchProjectTemplateId != "" && searchProjectTemplateId != "undefined") {
        var strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=getScheduleTasksPreview&searchProjectId=" + searchProjectTemplateId;
        emxUICore.getData(encodeURI(strURL));

        getSelectedProjectTemplateScheduleDetails(searchProjectTemplateId);

        enableTemplatePredictWBS();
    } else {
        disableQuestionField();
    }
}

function getSelectedProjectTemplateDetails() {
	enableNameField();
    var searchProjectTemplateId = document.getElementsByName("SeachProjectOID")[0].value;

    if (searchProjectTemplateId != null && searchProjectTemplateId != "" && searchProjectTemplateId != "undefined") {
        var strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=searchProjectData&searchProjectId=" + searchProjectTemplateId;

        var responseText = emxUICore.getData(strURL);
        var responseJSONObject = emxUICore.parseJSON(responseText);

        var selectedProjectTemplate = document.getElementsByName("SeachProjectDisplay")[0].value;
		if (responseJSONObject[0].state != "Released") {
		
		var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessageWithPlaceHolder&msgValue="+selectedProjectTemplate+"&errorKey=emxProgramCentral.ProjectTemplate.SelectedTemplateStateValidation";
        var errorMessage = emxUICore.getData(errorUrl);
        alert(errorMessage);
         basicClear('SeachProject');
         return;
 		}
		
		//IR-955371-3DEXPERIENCER2023x
		//below fields are not in documents. so, no need to check for these
		delete responseJSONObject[0].state;
		delete responseJSONObject[0].KindOfProjectTemplate;
		delete responseJSONObject[0].autoNameSeries;

                 
        for (var i = 0; i < responseJSONObject.length; i++) {

            for (var key in responseJSONObject[i]) {

                if (key == "Question" && responseJSONObject[i][key] == "true") {
                    enableQuestionField();
                } else if (key == "Question" && responseJSONObject[i][key] == "false") {
                    disableQuestionField();
                } else if (key == "RT" && responseJSONObject[i][key] == "true") {
                    enableRTF();
                    emxFormReloadField('ResourceTemplate');
                } else if (key == "RT" && responseJSONObject[i][key] == "false") {
                    emxFormReloadField('ResourceTemplate');
                    disableRT();
                } else if (key == "ProjectDate") {
                    // While creating the Project from "Project Template" default date in ProjectDate field should not be changed after selecting the "Project Template".
                } else if (key == "ScheduleFrom") {
                    emxFormReloadField('ScheduleFrom');
                } else if (key == "DefaultConstraintType") {
                    emxFormReloadField('DefaultConstraintType');
                } else if(key == 'nameFieldReadOnly' && responseJSONObject[i][key] == "Yes"){
					disableNameField();
				}else {
                    var value = responseJSONObject[i][key];
                    if (key != 'nameFieldReadOnly' && value != null && value != "") {
                        document.getElementsByName(key)[0].value = value;
                    }
                }
            }
        }
        enableTemplatePredictWBS();

    } else {
        setDefaultValue();
    }

}
function disableNameField() {
	document.getElementById("Name").readOnly=true;
	document.getElementsByName("autoNameCheck")[0].disabled = true;
}
function enableNameField() {
	document.getElementById("Name").readOnly = false;
	document.getElementsByName("autoNameCheck")[0].disabled = false;
}

function disablePreviewButton() {
    document.getElementById("previewFile").disabled = true;
    disableImportFormFields();
}

function disableSchedulePreviewButton() {
    document.getElementById("previewFile").disabled = true;
}

function enablePreviewButton() {
    document.getElementById("previewFile").disabled = false;
}

function disableRT() {
    document.getElementById("ResourceTemplateId").disabled = true;
}

function disableRTF() {
    setFocusOnSearchField();
    document.getElementById("ResourceTemplateId").disabled = true;
    //the project date field was appearing as disabled so we have set createInputField class to the project date field to appear as enabled
    if (document.getElementById('calc_TypeActual').childNodes[3]) {
        document.getElementById('calc_TypeActual').childNodes[3].setAttribute("class", "createInputField");
    }
    disableQuestionField();
}

function enableRTF() {
    document.getElementById("ResourceTemplateId").disabled = false;
}

function disableQuestionField() {
    var keyName = "questionsDisplay";
    var keyValue = "";
    var strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=QuestionTxt&subMode=NoQuestion";
    var responseTxt = emxUICore.getData(strURL);
    var responseJSONObj = emxUICore.parseJSON(responseTxt);

    keyValue = responseJSONObj[keyName];

    document.getElementsByName("btnquestions")[0].disabled = true;
    document.getElementsByName("questionsDisplay")[0].value = keyValue;
    disableTemplatePredictWBS();
}

function enableTemplatePredictWBS() {
    document.getElementById("predictWBS").disabled = false;
}

function disableTemplatePredictWBS() {
    document.getElementById("predictWBS").disabled = true;
}

function enableQuestionField() {
    var keyName = "questionsDisplay";
    var keyValue = "";
    var strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=QuestionTxt&subMode=ToRespond";
    var responseTxt = emxUICore.getData(strURL);
    var responseJSONObj = emxUICore.parseJSON(responseTxt);

    keyValue = responseJSONObj[keyName];
    //the question field was appearing as disabled so we have set createInputField class to the question field to appear as enabled
    if (document.getElementById('calc_questions').childNodes[1]) {
        document.getElementById('calc_questions').childNodes[1].setAttribute("class", "createInputField");
    }

    document.getElementsByName("btnquestions")[0].disabled = false;
    document.getElementsByName("questionsDisplay")[0].value = "";//Value is set to empty for IR-418483-3DEXPERIENCER2017x

    enableTemplatePredictWBS();
}

function disableImportFormFields() {
    document.getElementById("Name") && (document.getElementById("Name").value = "");
	document.getElementById("descriptionId") && (document.getElementById("descriptionId").value = "");

	document.getElementById("Name") && (document.getElementById("Name").disabled = true);
	document.getElementsByName("autoNameCheck") && (document.getElementsByName("autoNameCheck")[0]) && (document.getElementsByName("autoNameCheck")[0].disabled = true);
	document.getElementById("descriptionId") && (document.getElementById("descriptionId").disabled = true);
	document.getElementsByName("TypeActualDisplay") && (document.getElementsByName("TypeActualDisplay")[0]) && (document.getElementsByName("TypeActualDisplay")[0].disabled = true);
	document.getElementsByName("btnTypeActual") && (document.getElementsByName("btnTypeActual")[0]) && (document.getElementsByName("btnTypeActual")[0].disabled = true);
}
function enableImportFormFields() {
    document.getElementById("Name").disabled = false;
    document.getElementsByName("autoNameCheck")[0].disabled = false;
    document.getElementById("descriptionId").disabled = false;
    document.getElementsByName("TypeActualDisplay")[0].disabled = false;
    document.getElementsByName("btnTypeActual")[0].disabled = false;
}

//Task creation script
function reloadForm() {
    emxFormReloadField('Policy');
}
function reloadDuration() {
    var taskPolicy = document.getElementById("PolicyId").value;
    if ("Project Task" == taskPolicy) {
        document.getElementById("Duration").value = "1";
    } else {
        document.getElementById("Duration").value = "0";
    }
}

function changeDurationAndPolicy() {
    var selectedTaskType = document.getElementsByName("TypeActual")[0].value;
    var strURL = "../programcentral/emxProjectManagementUtil.jsp?mode=ChangePolicy&SelectedType=" + selectedTaskType;
    if (document.getElementsByName("policy")[0] != null) {
        var selectedPolicy = document.getElementsByName("policy")[0].value;
        strURL = strURL + "&selectedPolicy=" + selectedPolicy;
    }
    var responseTxt = emxUICore.getData(strURL);
    var responseJSONObj = emxUICore.parseJSON(responseTxt);
    var policy = responseJSONObj["Policy"];
    var isMilestone = responseJSONObj["isMilestone"];
    var isGate = responseJSONObj["isGate"];

    for (var key in responseJSONObj) {
        if (document.getElementsByName(key)[0] != null && document.getElementsByName(key)[0] != 'undefined') {
            document.getElementsByName(key)[0].value = responseJSONObj[key];
            if ((isMilestone || isGate) && "Duration" == key) {
                document.getElementById("Duration").disabled = true;
            } if ((isMilestone || isGate) && "EstimatedStartDate" == key) {
                document.getElementById("EstimatedStartDate").disabled = true;
                document.getElementsByName("EstimatedStartDate_date")[0].setAttribute('href', 'javascript:void(0)');
                document.getElementsByName("EstimatedStartDate_date")[0].setAttribute('disabled', 'true');
            } if ((isMilestone || isGate) && "EstimatedEndDate" == key) {
                document.getElementById("EstimatedEndDate").disabled = true;
                document.getElementsByName("EstimatedEndDate_date")[0].setAttribute('href', 'javascript:void(0)');
                document.getElementsByName("EstimatedEndDate_date")[0].setAttribute('disabled', 'true');
            }
        }
    }

    var defPolicy = document.getElementById("PolicyId");
    var index = 0
    for (index = 0; index < defPolicy.options.length; index++) {
        if (policy == defPolicy.options[index].text) {
            break;
        }
    }
    defPolicy.selectedIndex = index;

    var labels = document.getElementsByTagName("label");
    for (var i = 0; i < labels.length; i++) {
        var elem = document.getElementsByTagName("label")[i].htmlFor;
        if("NeedsReview" == elem){
        	var elemId = "NeedsReviewId";
        }else if("TaskCalculateDuration" == elem){
        	var elemId = "TaskCalculateDuration";
        }
        
        if ((isMilestone || isGate) && ("NeedsReview" == elem || "TaskCalculateDuration" == elem)) {
            document.getElementsByTagName("label")[i].style.visibility = "hidden";
            document.getElementById(elemId).style.visibility = "hidden";
            document.getElementsByTagName("label")[i].style.display = "none";
            document.getElementById(elemId).style.display = "none";
        } else if ("NeedsReview" == elem || "TaskCalculateDuration" == elem) {
            document.getElementsByTagName("label")[i].style.visibility = "visible";
            document.getElementById(elemId).style.visibility = "visible";
        }
    }
}

function setDefaultConstraint() {
    var defconstratinttype = "As Soon As Possible";
    if (null != document.getElementById("Schedule FromId")) {
        var scheduledfrom = document.getElementById("Schedule FromId").value;
    } else if (null != document.emxCreateForm.ScheduleFrom) {
        var scheduledfrom = document.emxCreateForm.ScheduleFrom.value;
    }
    var defconstratinttype = document.getElementById("DefaultConstraintTypeId");
    if ("Project Start Date" == scheduledfrom) {
        defconstratinttype.value = "As Soon As Possible";
    } else {
        defconstratinttype.value = "As Late As Possible";
    }
}

function validateWBSTaskConstraintDate1() {

    var taskTypeElement = document.getElementById("Task Constraint TypeId").value;

    if (taskTypeElement != "As Soon As Possible" && taskTypeElement != "As Late As Possible" && document.getElementById("TaskConstraintDate").value == "") {
        alert("Please select the Task Constraint Date");
        return false;
    }
    return true;
}

function setTaskDuration() {
    var taskDurationKeyword = document.getElementById("DurationKeywordsId");

    if (null != taskDurationKeyword && null != taskDurationKeyword.value) {
        var durationKeywordVal = taskDurationKeyword.value;
        if (durationKeywordVal != "") {
            var temp = new Array();
            temp = durationKeywordVal.split('|');
            document.getElementById("Duration").value = temp[1];
            setDurationKeyword();
            if (temp[1] == document.getElementById("Duration").value) {
                document.getElementById("DurationKeywordsId").value = durationKeywordVal;
                document.getElementById("Duration").value = temp[1];
            }
            else {
                document.getElementById("DurationKeywordsId").value = "";
                document.getElementById("Duration").value = "1";
            }
        }
        else {
            document.getElementById("Duration").value = "";
        }
    }
}

function setDurationKeyword() {
    var taskPolicy = document.getElementById("PolicyId").value;
    var taskDuration = document.getElementById("Duration");
    var secondCall = taskDuration.getAttribute("secondCall");

    if (taskDuration.value >= 10000) {
        var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.Common.PleaseEnterADurationValueLessThan";
        var errorMessage = emxUICore.getData(errorUrl);
        alert(errorMessage);
        taskDuration.value = "1";
        taskDuration.setAttribute("secondCall", "true");
    }

    if ("Project Task" == taskPolicy) {

        if (!secondCall && !isNaN(taskDuration.value) && taskDuration.value <= 0) {
            var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.Common.ValueMustBeAPositiveReal";
            var errorMessage = emxUICore.getData(errorUrl);
            alert(errorMessage);
            taskDuration.value = "1";
            taskDuration.setAttribute("secondCall", "true");

        }
    }
    else if ("Project Review" == taskPolicy) {
        if (!secondCall && !isNaN(taskDuration.value) && taskDuration.value < 0) {
            var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.Common.ValueMustBeNonNegavtive";
            var errorMessage = emxUICore.getData(errorUrl);
            alert(errorMessage);
            taskDuration.value = "0";
            taskDuration.setAttribute("secondCall", "true");

        }
    }
    if (secondCall) {
        taskDuration.removeAttribute("secondCall");
    }
    var taskDurationKeyword = document.getElementById("DurationKeywordsId");
    if (null != taskDurationKeyword) {
        taskDurationKeyword.value = "";
    }

}

function addApplicabilityContextsSelector() {
    var selectTag = document.getElementsByName("ApplicabilityContexts");
    var applicabilityContextsHidden = document.getElementById("ApplicabilityContextsHidden");
    var sURL = '../common/emxFullSearch.jsp?field=TYPES=type_Model&excludeOID=' + applicabilityContextsHidden.value + '&table=PMCGeneralSearchResults&selection=multiple&hideHeader=true&submitURL=../enterprisechange/emxEnterpriseChangeUtil.jsp?mode=searchUtil&targetTag=select&selectName=ApplicabilityContexts';
    showChooser(sURL, 850, 630);
}

//For ECH fields
function removeApplicabilityContexts() {
    var selectTag = document.getElementsByName("ApplicabilityContexts");
    var selectedOptionsValue = "";
    for (var i = selectTag[0].options.length - 1; i >= 0; i--) {
        if (selectTag[0].options[i].selected) {
            if (selectedOptionsValue != "") {
                selectedOptionsValue += ",";
            }
            selectedOptionsValue += selectTag[0].options[i].value;
            selectTag[0].remove(i);
        }
    }

    var applicabilityContextsHidden = document.getElementById("ApplicabilityContextsHidden");
    var applicabilityContextsHiddenValues = applicabilityContextsHidden.value.split(",");
    var selectedOptionsValues = selectedOptionsValue.split(",");
    var applicabilityContextsHiddenNewValue = "";
    for (var j = 0; j < applicabilityContextsHiddenValues.length; j++) {
        var applicabilityContextsHiddenValue = applicabilityContextsHiddenValues[j];
        var contains = "false";
        for (var k = 0; k < selectedOptionsValues.length; k++) {
            var selectedOptionValue = selectedOptionsValues[k];
            if (applicabilityContextsHiddenValue == selectedOptionValue) {
                contains = "true";
            }
        }
        if (contains == "false") {
            if (applicabilityContextsHiddenNewValue != "") {
                applicabilityContextsHiddenNewValue += ",";
            }
            applicabilityContextsHiddenNewValue += applicabilityContextsHiddenValue;
        }
    }
    applicabilityContextsHidden.value = applicabilityContextsHiddenNewValue;
}

//Drag & drop
function isValidDnDOperation(inputs) {
    var error = "";
    var success = "";

    if (inputs != "" && inputs != 'undefined') {
        var isDragParent = false;
        var targetObjectId = inputs.targetObjectId;
        var targetObjectLevel = inputs.targetObjectLevel;
        var targetObjectType = inputs.targetObjectType;

        var draggedObjectId = inputs.draggedObjectId;
        var draggedObjectLevel = inputs.draggedObjectLevel;
        var draggedObjectType = inputs.draggedObjectType;
        var parentDropObjectId = inputs.parentDropObjectId;
        var parentDragObjectId = inputs.parentDragObjectId;

        if (draggedObjectLevel != "") {
            isDragParent = targetObjectLevel.indexOf(draggedObjectLevel) == 0 ? true : false;
        }

        if (isDragParent && parentDragObjectId == parentDropObjectId) {
            var strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=DnDWarningMsg";
            var responseTxt = emxUICore.getData(strURL);
            var responseJSONObj = emxUICore.parseJSON(responseTxt);
            error = responseJSONObj["error"];
            success = "error";
        } else {
            var strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=DragAndDrop&targetObjectId=" + targetObjectId + "&targetObjectLevel=" + targetObjectLevel + "&targetObjectType=" + targetObjectType + "&draggedObjectId=" + draggedObjectId + "&draggedObjectLevel=" + draggedObjectLevel + "&draggedObjectType=" + draggedObjectType + "&parentDragObjectId=" + parentDragObjectId + "&parentDropObjectId=" + parentDropObjectId;
            var responseTxt = emxUICore.getData(strURL);
            var responseJSONObj = emxUICore.parseJSON(responseTxt);
            success = responseJSONObj["status"];
            error = responseJSONObj["error"];
        }
        return { status: success, error: error };
    } else {
        return true;
    }

}

function ChangeEventFrequncy() {
    var objectId = "";
    if (document.forms[0].parentOID) {
        objectId = document.forms[0].parentOID.value;
    }
    var EventFreqElelment = document.getElementById("FrequencyId").value;
    if ("Non-Recurrence" == EventFreqElelment) {
        var href = "../common/emxForm.jsp?form=PMCCalendarCreateEventForm&mode=edit&findMxLink=false&formHeader=emxProgramCentral.Common.CreateCalendar&isNonRecurrence=true&postProcessJPO=emxWorkCalendar:createCalendareEventProcess&suiteKey=ProgramCentral&StringResourceFileId=emxProgramCentralStringResource&SuiteDirectory=programcentral&submitAction=doNothing&postProcessURL=../programcentral/emxProgramCentralUtil.jsp?mode=refreshCalendarEventPage&targetLocation=slidein&openerFrame=PMCListCalendarEvents&HelpMarker=emxhelpcalendareventcreate&objectId=" + objectId + "&parentOID=" + objectId + "";

        window.location.href = href;
    }
    else if ("Weekly" == EventFreqElelment) {
        var href = "../common/emxForm.jsp?form=PMCCalendarCreateEventForm&mode=edit&findMxLink=false&formHeader=emxProgramCentral.Common.CreateCalendar&isNonRecurrence=false&postProcessJPO=emxWorkCalendar:createCalendareEventProcess&suiteKey=ProgramCentral&StringResourceFileId=emxProgramCentralStringResource&SuiteDirectory=programcentral&submitAction=doNothing&postProcessURL=../programcentral/emxProgramCentralUtil.jsp?mode=refreshCalendarEventPage&targetLocation=slidein&openerFrame=PMCListCalendarEvents&HelpMarker=emxhelpcalendareventcreate&objectId=" + objectId + "&parentOID=" + objectId + "";

        window.location.href = href;
    }
}

function ChangeReviewTaskField() {
    //Change Default Duration According to Policy selected.
    var taskType = document.getElementById("TypeActualId").value;
    var strURL = "../programcentral/emxProjectManagementUtil.jsp?mode=validateKindOf&objectType=" + taskType;
    var responseTxt = emxUICore.getData(strURL);
    var responseJSONObj = emxUICore.parseJSON(responseTxt);
    var isMilestone = responseJSONObj["isMilestone"];
    var isGate = responseJSONObj["isGate"];

    if (isMilestone || isGate) {
        document.getElementById("Duration").value = "0";
        document.getElementById("Duration").disabled = true;
    } else {
        document.getElementById("Duration").value = "1";
        document.getElementById("Duration").disabled = false;
    }

    var labels = document.getElementsByTagName("label");
    for (var i = 0; i < labels.length; i++) {
        var elem = document.getElementsByTagName("label")[i].htmlFor;
        if ((isMilestone || isGate) && "NeedsReview" == elem) {
            document.getElementsByTagName("label")[i].style.visibility = "hidden";
            document.getElementById("NeedsReviewId").style.visibility = "hidden";
            break;
        } else if ("NeedsReview" == elem) {
            document.getElementsByTagName("label")[i].style.visibility = "visible";
            document.getElementById("NeedsReviewId").style.visibility = "visible";
            break;
        }
    }
}

/**
 * Validate state of selected tasks.
 * @param frameXML -  structure browser object.
 * @returns {Boolean} - boolean value.
 */
function validateScheduleState(frameXML) {
    var isValidOperation = true;
    var checkboxes = frameXML.getCheckedCheckboxes();

    for (var e in checkboxes) {
        var aId = e.split("|");
        var rowid = aId[3];

        var rowObject = frameXML.getSelectedRowObjects([rowid])[0];
        var taskStateValue = frameXML.document.getElementById(rowObject.getAttribute("o")).value;

        if (taskStateValue != null && typeof taskStateValue != "undefined"
            && taskStateValue == "Review" && taskStateValue == "Complete") {
            alert("You cannot perform indentation operation beyond In Work state!");
            isValidOperation = false;
            break;
        }
    }

    return isValidOperation;
}

function removeLocation() {
    document.forms[0].LocationId.value = null;
    document.forms[0].LocationName.value = "";
}

function validateProjectName() {
    var projectName = this.value;
    if (projectName == " ") {
        var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.Validate.ProjectName";
        var errorMessage = emxUICore.getData(errorUrl);
        alert(errorMessage);
    }
    else {
        return true;
    }
}

function validateProjectOrProjectTemplateName() {
    var searchProjectTemplateId = document.getElementsByName("SeachProjectOID")[0].value;

    if ((searchProjectTemplateId == null || searchProjectTemplateId == "" || searchProjectTemplateId == "undefined")) {
        document.getElementsByName("SeachProjectDisplay")[0].value = "";
        var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&key=emxProgramCentral.Common.WrongProjectOrProjectTemplateName";
        var errorMessage = emxUICore.getData(errorUrl);
        alert(errorMessage);
        return false;
    } else {
        return true;
    }
}

function submitLaborReport() {
    var reportingYear = document.getElementById("ReportingYear");
    var reportingYearValue = reportingYear.value;
    if (!isNumeric(reportingYearValue)) {
        var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&key=emxProgramCentral.WeeklyTimeSheetReports.SelectNumericReportingYear";
        var errorMessage = emxUICore.getData(errorUrl);
        alert(errorMessage);
        reportingYear.value = "";
        reportingYear.focus();
        return;
    } else if (reportingYearValue == "") {
        var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&key=emxProgramCentral.WeeklyTimeSheetReports.PMCPlzEntValNo";
        var errorMessage = emxUICore.getData(errorUrl);
        alert(errorMessage);
        reportingYear.value = "";
        reportingYear.focus();
        return;
    } else {
       var parsedyear = parseInt(reportingYearValue);
       if(reportingYearValue.length!=4 || parsedyear <= 0) { 
            var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&key=emxProgramCentral.WeeklyTimeSheetReports.Select4DigitReportingYear";
            var errorMessage = emxUICore.getData(errorUrl);
            alert(errorMessage);
            reportingYear.value = "";
            reportingYear.focus();
            return;
        }
    }
}

function toggleRollupIcon(topFrame, oldImgName, newImgName, rowid, operation) {
    if ((typeof dataGridEnabled != 'undefined' && dataGridEnabled) || (typeof parent.dataGridEnabled != 'undefined' && parent.dataGridEnabled)) {
        var toolbar = (typeof dgView != 'undefined') ? dgView._dgViewToolbar : parent.dgView._dgViewToolbar;

        var obj = null;

        if (toolbar != undefined && toolbar.items != undefined) {
            for (var itemPos = 0; itemPos < toolbar.items.getChildren().length; itemPos++) {
                var item = toolbar.items.getChildren()[itemPos];
                if (item.options.grid.semantics.icon && item.options.grid.semantics.icon.indexOf(oldImgName) >= 0) {
                    var iconArray = item.options.grid.semantics.icon.split("/");
                    if (iconArray && iconArray[4] && iconArray[4] === oldImgName + ".png") {
                        obj = item;
                        break;
                    }
                }
            }

            if (obj) {
                var iconName = "../../common/images/" + newImgName + ".png";

                obj.updateOptions({ icon: iconName });
            }
        }
    }
    else {

        var toolbar = topFrame.objMainToolbar;
        var obj = null;
        var itemPos = 0
        for (itemPos = 0; itemPos < toolbar.items.length; itemPos++) {
            var icon = toolbar.items[itemPos].icon;
            if (icon && icon.indexOf(oldImgName) >= 0) {
                var iconArray = icon.split("/");
                if (iconArray && iconArray[3] && iconArray[3] === oldImgName + ".png") {
                    obj = toolbar.items[itemPos];
                    break;
                }
            }
        }

        if (obj) {
            if (obj.element) {
                strText = obj.element.innerHTML;
                if (strText.indexOf(">") > -1) {
                    var strText1 = [];
                    strText1 = strText.split(">");
                    var imageName = strText1[0];

                    imageName = imageName.replace(oldImgName, newImgName);

                    strText = imageName + ">";
                    obj.element.innerHTML = strText;

                    objIcon = obj.icon;
                    objIcon = objIcon.replace(oldImgName, newImgName);
                    obj.icon = objIcon;
                }
            }
        }
    }

}

function changeTimelineField() {

    var intervalValues = document.getElementsByName("CostInterval");
    for (var i = 0; i < intervalValues.length; i++) {
        var intervalPeriod = intervalValues[i].value;
        if ("Project Phase" == intervalPeriod) {

            if (true == intervalValues[i].checked) {
                document.getElementsByName("TimeLineIntervalFrom")[0].disabled = true;
                document.getElementsByName("TimeLineIntervalTo")[0].disabled = true;
                document.getElementsByName("timelineImg")[0].hidden = true
                document.getElementsByName("timelineImg")[1].hidden = true
            } else {
                document.getElementsByName("timelineImg")[0].hidden = false
                document.getElementsByName("timelineImg")[1].hidden = false
            }


        }
    }
}

function validateNumberOf() {
    var taskNumberOf = document.getElementById("NumberOf");
    var numberOfTask = parseInt(taskNumberOf.value);
    if ((isNaN(numberOfTask)) || (numberOfTask <= 0) || (numberOfTask > 100)) {
        var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.Common.CreateNumberOfTasks";
        var errorMessage = emxUICore.getData(errorUrl);
        alert(errorMessage);
        taskNumberOf.value = "1";
        document.getElementById("Name").requiredValidate = isZeroLength;
        document.getElementById("Name").disabled = false;
        document.getElementsByName("autoNameCheck")[0].checked = false;
        document.getElementsByName("autoNameCheck")[0].disabled = false;
        return false;
    }

    if ((numberOfTask > 1)) {
        document.getElementById("Name").requiredValidate = "";
        document.getElementById("Name").disabled = true;
        document.getElementById("Name").value = "";
        document.getElementsByName("autoNameCheck")[0].checked = true;
        document.getElementsByName("autoNameCheck")[0].disabled = true;
    }
    else if ((numberOfTask == 1)) {
        document.getElementById("Name").disabled = false;
        document.getElementById("Name").requiredValidate = isZeroLength;
        document.getElementsByName("autoNameCheck")[0].checked = false;
        document.getElementsByName("autoNameCheck")[0].disabled = false;
    }

    return true;
}


function validateNumberOfForFolder() {
    var folderNumberOf = document.getElementById("NumberOf");
    var numberOfFolder = parseInt(folderNumberOf.value);
    if ((isNaN(numberOfFolder)) || (numberOfFolder <= 0) || (numberOfFolder > 10)) {
        var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.Common.CreateNumberOfFolders";
        var errorMessage = emxUICore.getData(errorUrl);
        alert(errorMessage);
        folderNumberOf.value = "1";
        //document.getElementsByName("Name")[0].requiredValidate = isZeroLength; 
        //document.getElementsByName("Name")[0].disabled = false;
        //document.getElementsByName("autoNameCheck")[0].checked = false;
        //document.getElementsByName("autoNameCheck")[0].disabled	= false;
		
		document.getElementsByName("Title")[0].disabled =false;
		document.getElementsByName("Title")[0].value = "";
        return false;
    }

    if ((numberOfFolder > 1)) {
		var labelUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.Bookmark.CreateBookmarkAutoTitle";
        var label = emxUICore.getData(labelUrl);
        //document.getElementsByName("Name")[0].requiredValidate = ""; 
    	//document.getElementsByName("Name")[0].disabled=true;
        //document.getElementsByName("Name")[0].value = "";
        //document.getElementsByName("autoNameCheck")[0].checked = true;
        //document.getElementsByName("autoNameCheck")[0].disabled= true;
		
		document.getElementsByName("Title")[0].disabled =true;
		document.getElementsByName("Title")[0].value = label;
    }
    else if ((numberOfFolder == 1)) {
        //document.getElementsByName("Name")[0].disabled=false;
        //document.getElementsByName("Name")[0].requiredValidate = isZeroLength; 
        //document.getElementsByName("autoNameCheck")[0].checked = false;
        //document.getElementsByName("autoNameCheck")[0].disabled= false;
		
		document.getElementsByName("Title")[0].disabled =false;
		document.getElementsByName("Title")[0].value = "";
    }

    return true;
}

function reloadDefaultTabField() {
    emxFormReloadField('DefaultTab');
}

function validateDurationFormField() {
    var durationVal = document.getElementById("Duration").value;
    //Input unit can be "D" for day or "H" for hour, that needs to be converted to lowercase 
    durationVal = durationVal.toLowerCase();

    var errorUrl = "../programcentral/emxProjectManagementUtil.jsp?mode=errorMessageForDuration&errorKey=emxProgramCentral.Common.InvalidDurationErrorMessage&decimalkey=emxFramework.DecimalSymbol";
    var responseTxt = emxUICore.getData(errorUrl);
    var responseJSONObj = emxUICore.parseJSON(responseTxt);
    var errmsg = responseJSONObj["errorMessage"];
    var daysUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.DurationUnits.Days";
    var days = emxUICore.getData(daysUrl);
    var hoursUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.DurationUnits.Hours";
    var hours = emxUICore.getData(hoursUrl);
    days = days.toLowerCase();
    hours = hours.toLowerCase();

    if (durationVal.indexOf(days) != -1) {
        durationVal = durationVal.replace(days, "d");
    } else if (durationVal.indexOf(hours) != -1) {
        durationVal = durationVal.replace(hours, "h");
    }

    /* The below condition will check if the entered duration contains Days / Hours. If not Its not a valid duration*/
    if (!durationVal.endsWith(' h') && !durationVal.endsWith(' d') && isNaN(durationVal)) {
        alert(errmsg);
        document.getElementById("Duration").value = 1
        return false;
    }
    else {
        if (durationVal.lastIndexOf(' d') != -1) {
            durationVal = durationVal.substring(0, durationVal.lastIndexOf(' d'));
        } else if (durationVal.lastIndexOf(' h') != -1) {
            durationVal = durationVal.substring(0, durationVal.lastIndexOf(' h'));
        }

    }
    if (durationVal != null) {
        var decimalSymbol = responseJSONObj["decimalSymbol"];
        if (isNaN(durationVal)) {
            alert(errmsg);
            document.getElementById("Duration").value = 1
            return false;
        }
        if (durationVal.endsWith(' ')) {
            alert(errmsg);
            document.getElementById("Duration").value = 1
            return false;
        }
        if (durationVal.indexOf(decimalSymbol) != -1) {
            if (durationVal.split(decimalSymbol)[1].length >= 3) {
                errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.Common.InvalidDurationDecimalValue";
                var Error = emxUICore.getData(errorUrl);
                alert(Error);
                document.getElementById("Duration").value = 1
                return false;
            }
        }

        if (durationVal == " ") {
            alert(errmsg);
            document.getElementById("Duration").value = 1
            return false;
        }
        if (durationVal <= 0) {
            errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.Common.ValueMustBeAPositiveReal";
            var Error = emxUICore.getData(errorUrl);
            alert(Error);
            document.getElementById("Duration").value = 1
            return false;
        }
        if (durationVal >= 10000) {
            errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.Common.PleaseEnterADurationValueLessThan";
            var Error = emxUICore.getData(errorUrl);
            alert(Error);
            document.getElementById("Duration").value = 1
            return false;
        }
    }
    return true;
}

function validateTaskRequirementFormField() {
    var taskRequirement = document.getElementById("TaskRequirementId").value;
    var objectId = document.forms[0].parentOID.value;
    if ("Mandatory" == taskRequirement) {
        var errorUrl = "../programcentral/emxProjectManagementUtil.jsp?mode=taskRequirementAlert&objectId=" + objectId;
        var responseTxt = emxUICore.getData(errorUrl);
        var responseJSONObj = emxUICore.parseJSON(responseTxt);
        var showAlert = responseJSONObj["showAlert"];
        if (showAlert) {
            var errorUrl = "../programcentral/emxProgramCentralUtil.jsp?mode=errorMessage&errorKey=emxProgramCentral.TaskRequirement.OptionalToMandatory";
            var errorMessage = emxUICore.getData(errorUrl);
            alert(errorMessage);
            return false;
        }
    }
}

function getSubQuestionsInTemplate()
	{
                var xmlMessage = "<mxRoot><action fromRMB=\"\"><![CDATA[remove]]></action>";
                var strRowId = event.target.parentNode.getAttribute("rmbrow");
                var rmbId = event.target.parentNode.getAttribute("rmbid");
                var selectedResponseIndex = event.target.selectedIndex;
                var selectBoxId = event.target.id;
                var arrTemp = event.target.value.split("|");

                var tempSelectOptionArr = {};
                var allSelectElements = document.getElementsByClassName("temp-select-class");
                var allCommentInputElements = document.getElementsByClassName("temp-comment-input-class");
                
                for (let index = 0; index < allSelectElements.length && allCommentInputElements.length; index++) {
					 var dataToCache = {};
					 dataToCache["option"] = allSelectElements[index].selectedIndex;
					 dataToCache["comment"] = allCommentInputElements[index].value;
                     tempSelectOptionArr[allSelectElements[index].id.split("-")[0]] = dataToCache;
                }
                var selectedResponse = "";
                if(arrTemp.length>=2){
                    selectedResponse = arrTemp[1];
                    rmbId = arrTemp[0];
					console.log(selectedResponse);
                }
                var strUrlTemp = "../programcentral/emxProgramCentralUtil.jsp?mode=GetSubQuestions&objectId=" + rmbId+"&selectedResponse="+selectedResponse+"&selectedOptionArr="+JSON.stringify(tempSelectOptionArr);
                var responseTxt = emxUICore.getData(encodeURI(strUrlTemp));
				var topFrame = findFrame(getTopWindow(),"content");

               var getChildItems = topFrame.emxEditableTable.getChildrenRowIds(strRowId); 
                for (var item1 in getChildItems)
               {                   
                      if(!isNaN(item1))
                     {
                            var rowId = strRowId+","+item1;
                            xmlMessage += "<item id=\""+rowId+"\"/>";
                     }
               } 
                xmlMessage += "</mxRoot>";
				emxUICore.selectSingleNode(topFrame.oXML, "/mxRoot/rows//r[@id = '" + strRowId + "']").setAttribute("p",emxUICore.selectSingleNode(topFrame.oXML, "/mxRoot/rows//r[@id = '" + strRowId + "']").getAttribute("p").replaceAll("|","@"));
               if(getChildItems)
                   removedeletedRows(xmlMessage);
                topFrame.emxEditableTable.select(new Array(strRowId));

                var refreshComponentTemp = "<mxRoot><action><![CDATA[expand]]></action><data status=\"committed\" fromRMB=\"false\"></data></mxRoot>";
                topFrame.emxEditableTable.addToSelected(refreshComponentTemp);
                
                if(responseTxt != "" && responseTxt != null){
					var responseTxtArr = responseTxt.split("</mxRoot>");
					responseTxtArr.forEach(responseData=>{
                        if(responseData!=""){
							
                topFrame.emxEditableTable.select(new Array(strRowId));
    						topFrame.emxEditableTable.addToSelected(responseData+"</mxRoot>");
                        }
					});
    	           
                }
                topFrame.refreshStructureWithOutSort();

                for(var k in tempSelectOptionArr){
                    if(document.getElementById(k+"-response") != null && document.getElementById(k+"-comment-input") != null){
						document.getElementById(k+"-response").selectedIndex = tempSelectOptionArr[k]["option"];
						document.getElementById(k+"-comment-input").value = tempSelectOptionArr[k]["comment"];
					}
                }


                var checkedItems = getCheckedCheckboxes();
        
               for (var item in checkedItems)
               { 
                       strRowId = (item.substring(item.lastIndexOf("|")+1,item.length));
                      nRow = emxUICore.selectSingleNode(oXML, "/mxRoot/rows//r[@id = '" + strRowId + "']");
                      var arrItem=new Array(nRow);
                      unRegisterSelectedRows(arrItem);
               }
        
	            
	}
function checkroles(){
    let array = new Array();
    let errMsg = '';
    console.log("checkroles-5");
    var tds = document.querySelectorAll('[position="7"]');
    for(let tdd = 0 ; tdd<tds.length;tdd++){
        const element = tds[tdd];
        const childlen = element.childElementCount;
        let value = element.textContent;
        if(childlen>0){
            const childs =tds[tdd].children;
            for(let childindex = 0;childindex<childs.length;childindex++){
                const title = childs[childindex].textContent;
                value = value.replace(title, '');
            }

        }
        if(value !== ''){
            console.log(value);
            value = value.trim();
            if(!array.includes(value)){
                array.push(value);
            }else{
                errMsg += ','+value;
            }
        }

    }
    if(errMsg.length>0){
        const lanuage = navigator.language;
        let ms = 'role "{}" repet';
        if(lanuage.includes('zh')){
            ms = '角色"{}"重复';
        }
        alert(ms.replace('{}',errMsg.substring(1)));
    }else{
        emxEditableTable.performXMLDataPost();
    }
}
/**
 * JF_isKeyTask字段 OnChange Handler。
 * 1. 用于任务创建页面和任务特性页面；
 * 2. 当“是否关键任务”修改为Y时，读取国际化提示并提醒用户完成关键任务必须上传交付物文档；
 * 3. 仅做提示，不阻断保存和后续原有业务逻辑。
 * @author LIUJR
 * @date 2026/7/7
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
