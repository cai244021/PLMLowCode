//=================================================================
// JavaScript emxJSValidationUtil.js
//
// Copyright (c) 1992-2020 Dassault Systemes.
// All Rights Reserved.
// This program contains proprietary and trade secret information of MatrixOne,Inc.
// Copyright notice is precautionary only
// and does not evidence any actual or intended publication of such program
//=================================================================

var emxJSValidationUtil = {};
// Check that the number of characters in a string is between a max and a min
function isValidLength(string, min, max) {
  if (string.length < min || string.length > max) return false;
  else return true;
}

// Check that a credit card number is valid based using the LUHN formula (mod10 is 0)
function isValidCreditCard(number) {
  number = '' + number;

  if (number.length > 16 || number.length < 13 ) return false;
  else if (getMod10(number) != 0) return false;
  else if (arguments[1]) {
    var type = arguments[1];
    var first2digits = number.substring(0, 2);
    var first4digits = number.substring(0, 4);

    if (type.toLowerCase() == 'visa' && number.substring(0, 1) == 4 &&
      (number.length == 16 || number.length == 13 )) return true;
    else if (type.toLowerCase() == 'mastercard' && number.length == 16 &&
      (first2digits == '51' || first2digits == '52' || first2digits == '53' || first2digits == '54' || first2digits == '55')) return true;
    else if (type.toLowerCase() == 'american express' && number.length == 15 &&
      (first2digits == '34' || first2digits == '37')) return true;
    else if (type.toLowerCase() == 'diners club' && number.length == 14 &&
      (first2digits == '30' || first2digits == '36' || first2digits == '38')) return true;
    else if (type.toLowerCase() == 'discover' && number.length == 16 && first4digits == '6011') return true;
    else if (type.toLowerCase() == 'enroute' && number.length == 15 &&
      (first4digits == '2014' || first4digits == '2149')) return true;
    else if (type.toLowerCase() == 'jcb' && number.length == 16 &&
      (first4digits == '3088' || first4digits == '3096' || first4digits == '3112' || first4digits == '3158' || first4digits == '3337' || first4digits == '3528')) return true;
    else return true;
  }
  else return true;
}

// Check that an email address is valid based on RFC 821 (?)
function isValidEmail(address) {
  if (address.indexOf('@') < 1) return false;
  var name = address.substring(0, address.indexOf('@'));
  var domain = address.substring(address.indexOf('@') + 1);
  if (name.indexOf('(') != -1 || name.indexOf(')') != -1 || name.indexOf('<') != -1 || name.indexOf('>') != -1 || name.indexOf(',') != -1 || name.indexOf(';') != -1 || name.indexOf(':') != -1 || name.indexOf('\\') != -1 || name.indexOf('"') != -1 || name.indexOf('[') != -1 || name.indexOf(']') != -1 || name.indexOf(' ') != -1) return false;
  if (domain.indexOf('(') != -1 || domain.indexOf(')') != -1 || domain.indexOf('<') != -1 || domain.indexOf('>') != -1 || domain.indexOf(',') != -1 || domain.indexOf(';') != -1 || domain.indexOf(':') != -1 || domain.indexOf('\\') != -1 || domain.indexOf('"') != -1 || domain.indexOf('[') != -1 || domain.indexOf(']') != -1 || domain.indexOf(' ') != -1) return false;
  return true;
}


// Check that a US zip code is valid
function isValidZipcode(zipcode) {
  zipcode = removeSpaces(zipcode);
  if (!(zipcode.length == 5) || !isNumeric(zipcode)) return false;
  return true;
}


// Check that a Canadian postal code is valid
function isValidPostalcode(postalcode) {
  if (postalcode.search) {
    postalcode = removeSpaces(postalcode);
    if (postalcode.length == 6 && postalcode.search(/^\w\d\w\d\w\d$/) != -1) return true;
    else if (postalcode.length == 7 && postalcode.search(/^\w\d\w\-d\w\d$/) != -1) return true;
    else return false;
  }
  return true;
}

// Check that a string contains only letters and numbers
function isAlphanumeric(strVal, ignoreWhiteSpace) {
  /*
  if (string.search) {
    if ((ignoreWhiteSpace && string.search(/[^\w\s]/) != -1) || (!ignoreWhiteSpace && string.search(/\W/) != -1)) {
      return false;
    }
  }
  */
  return checkForNameBadChars(strVal,false);
}

// Check that a string contains only letters
function isAlphabetic(string, ignoreWhiteSpace) {
  if (string.search) {
    if ((ignoreWhiteSpace && string.search(/[^a-zA-Z\s]/) != -1) || (!ignoreWhiteSpace && string.search(/[^a-zA-Z]/) != -1)) return false;
  }
  return true;
}


// Check that a string contains only numbers and periods and commas (1)
function isNumeric(numString,ignoreWhiteSpace) {
  var newString = "";
  var commaCount = 0;

  newString = toNumeric(numString);
  return !isNaN(newString);
}


// Return a string that contains only numbers and periods (1)
function toNumeric(numString,ignoreWhiteSpace) {
  var newString = "";
  numString = trimWhitespace(numString);
  var commaCount = 0;
  if("true" == hasDigitSeparator && preferredDecimalSymbol == ",")
  {
    preferredDecimalSymbol = ".";
  }
  //remove any commas
  for (var i = numString.length; i > -1; i--) {
    //Assume the decimal value is rounded off to two digits
    if (numString.charAt(i) == preferredDecimalSymbol && (parseInt(i) == parseInt(numString.length-3) || parseInt(i) == parseInt(numString.length-2))){
      if (commaCount == 0){
        newString = "." + newString;
        commaCount++;
      }
    }
    else if("true" == hasDigitSeparator && (numString.charAt(i) == "," || numString.charAt(i) == "."))
    {
      continue;
    }
    else{
      newString = numString.charAt(i) + newString;
    }
  }
  return newString;
}

// Return a Formated string according to the DegitalSeparator and DecimalSymbol
function formatNumber(numString) {
  numString = numString.toString();
  if("false" == hasDigitSeparator && preferredDecimalSymbol == ".")
    return numString;

  var newString = "";
  numString = trimWhitespace(numString);
  var commaCount = 0;

  var decPart = "";
  var remPart = numString;
  var decIndex = numString.indexOf(".");
  if(decIndex < 0 )
    decIndex = numString.indexOf(",");


  if(decIndex >= 0 )
  {
    decPart = numString.substr(decIndex+1);
    remPart = numString.substring(0, decIndex);
  }

  var decSym = preferredDecimalSymbol;
  var thSym = "";

  if("true" == hasDigitSeparator)
  {
    if(preferredDecimalSymbol == ",")
      thSym = ".";
    else
      thSym = ",";
  }


  if(thSym != "")
  {
    var j = 1;
    for (var i = remPart.length-1; i > -1; i--)
    {
      if(remPart.charAt(i) == "," || remPart.charAt(i) == ".")
        continue;

      if(j%4 == 0)
      {
        newString = remPart.charAt(i) + thSym + newString;
        j == 1;
      }
      else
        newString = remPart.charAt(i) + newString;
      j++;
    }
  }
  else
    newString = remPart;

  if(decPart != "")
    newString += decSym + decPart;
  return newString;
}


function formatNumeric(doc,form,textbox,numString) {
  eval(doc + "." + form + "." + textbox + ".value = \"" + toNumeric(numString)+"\"");
}


// Remove all spaces from a string
function removeSpaces(string) {
  var newString = '';
  for (var i = 0; i < string.length; i++) {
    if (string.charAt(i) != ' ') newString += string.charAt(i);
  }
  return newString;
}



function trimWhitespace(strString) {
    if(strString == null || strString == 'undefined') {
        return '';
    } else {
        strString = strString.replace(/^[\s\u3000]*/g, "");
        return strString.replace(/[\s\u3000]+$/g, "");
    }
}


function replace(string,text,by) {
// Replaces text within a string
    var strLength = string.length, txtLength = text.length;
    if ((strLength == 0) || (txtLength == 0)) return string;

    var i = string.indexOf(text);
    if ((!i) && (text != string.substring(0,txtLength))) return string;
    if (i == -1) return string;

    var newstr = string.substring(0,i) + by;

    if (i+txtLength < strLength)
        newstr += replace(string.substring(i+txtLength,strLength),text,by);

    return newstr;
}

//determine if certain characters exist in a string
//return true/false
function charExists(str,ch){

  chIndex = str.indexOf(ch);
  if (chIndex == -1){
    return false;
    }else{
    return true;
    }
}

// Bad Character Validation Methods
// this function checks a string for bad characters
// Parameters:
//      strText (String) - the text to check.
//      arrBadChars (Array) - array of bad characters to check for.
//      blnReturnAll (boolean) - determines if the function should
//              return all bad characters or just the ones that were
//              found. True for all, false for found.
function checkStringForChars(strText, arrBadChars, blnReturnAll){
        var strBadChars = "";

        for (var i=0; i < arrBadChars.length; i++) {
                if (strText.indexOf(arrBadChars[i]) > -1) {
                        strBadChars += arrBadChars[i] + " ";
                }
        }

        if (strBadChars.length > 0) {
                if (blnReturnAll) {
                        return arrBadChars.join(" ");
                } else {
                        return strBadChars;
                }
        } else {
                return "";
        }
}

// this function checks a text field for bad characters
// Parameters:
//      objField (HTMLInputElement) - the text field to check.
//      arrBadChars (Array) - array of bad characters to check for.
//      blnReturnAll (boolean) - determines if the function should
//              return all bad characters or just the ones that were
//              found. True for all, false for found.
function checkFieldForChars(objField, arrBadChars, blnReturnAll){
        var strResult = checkStringForChars(objField.value, arrBadChars, blnReturnAll);
        if (strResult.length > 0) {
                objField.focus();
        }
        return strResult;
}

//check for the characters specified above by property file
function checkForBadChars(objField){
        return checkFieldForChars(objField, ARR_BAD_CHARS, false);
}

function checkForBadCharsCheckinDesc(objField){        
        return checkFieldForChars(objField, ARR_FILE_NAME_BAD_CHARS_CHECKIN_DESC, false);
}


function getAllBadChars(objField){
        return ARR_BAD_CHARS;
}

function getAllBadCharsForCheckinDesc(objField){
        return ARR_FILE_NAME_BAD_CHARS_CHECKIN_DESC;
}


//check for the characters specified above by property file
//true signifies passing in a string to check so return a boolean true/false
//else return the actual string of badchars.
function checkForNameBadChars(objField, blnIsObject){

        //if we pass in an object use it's 'Value' else use the string passed in
        if (blnIsObject){
                return checkFieldForChars(objField, ARR_NAME_BAD_CHARS, false);
        } else {
                var strFieldValue = objField;
                var strExists = checkStringForChars(strFieldValue, ARR_NAME_BAD_CHARS, false);

                if (strExists.length > 0 )
                {
                  return false;
                } else {
                    return true
                }
        }
}

//method to check if the string length has 128 bytes
function checkForNameLength128(objField, blnIsObject){

    //if we pass in an object use it's 'Value' else use the string passed in
	var strFieldValue = "";
    if (blnIsObject){
    	strFieldValue = objField.value;
    } else {
        strFieldValue = objField;
    }
    
    if (byteCount(strFieldValue) > 128 )
    {
    	return false;
    } else {
        return true;
    }
}

//method to get the byte count of a UTF-8 string
function byteCount(str) {
	var strArr = str.split("");
	var len = 0;
	for(var i = 0; i< strArr.length ; i++){
		var strChar = strArr[i];
		var encodeChar = encodeURIComponent(strChar);
		if(encodeChar.length == 1){//single byte char
			len++;
		}else{//multi byte char
			len = len + (encodeChar.split("%").length - 1); 
		}
	}
	//alert(len);
	return len;
}
//method to validate the length of user name lesser than 124
function checkValidUserNameLength(str)
{
	var len=byteCount(str);
	return len<124;
	}
////method to validate the length of all name fields
function checkValidLength(str,type)
{
	if(type=="Organization" && type)
		{
		   var len=byteCount(str);
		   return len<116;
		}
	else
		{
			var len=byteCount(str);
			return len<128;
		}
	}

//check for the characters specified above by property file
function checkForNameBadCharsList(objField){
        return checkFieldForChars(objField, ARR_NAME_BAD_CHARS, true);
}

//check for the characters specified above by property file
//and boolean value should be passed as true for unified bad chars
//and boolean value should be passed as false for NonUnified bad chars (i.e. All List of Bad Chars)

function checkForUnifiedNameBadChars(objField,blnIsUnified){
	return checkFieldForChars(objField, ARR_NAME_BAD_CHARS, !blnIsUnified);
}

//Getter method for List of Name bad characters 
function getAllNameBadChars(objField){
	return ARR_NAME_BAD_CHARS;
}

//check for the characters specified above by property file
function checkForRestrictedBadChars(objField){
        return checkFieldForChars(objField, ARR_BAD_CHARS, false);
}

//check for the characters specified above by property file
function checkForCommonIllegalCharacters(objField){
        return checkFieldForChars(objField, ARR_BAD_CHARS, true);
}


//check for the characters specified above by property file for FileNames
function checkForFileNameBadChars(objField){
        return checkForFileNameBadChars(objField, false);
}

function getAllFileBadChars(){
	return ARR_FILE_NAME_BAD_CHARS;
}


//check for the characters specified above by property file
//return the actual string of badchars.
function checkForFileNameBadChars(objField, blnIsObject){

        //if we pass in an object use it's 'Value' else use the string passed in
        if (blnIsObject){
                return checkFieldForChars(objField, ARR_FILE_NAME_BAD_CHARS, false);
        } else {
                var strFieldValue = objField;
                return strExists = checkStringForChars(strFieldValue, ARR_FILE_NAME_BAD_CHARS, false);
        }
}

function allowCustomInput(objSel, strName, strTextId, isFieldRequired, fromEditTable) {
        var objText = document.getElementById(strTextId);
        objText.disabled = !(objSel.options[objSel.selectedIndex].value == "```manualEntryOptionDisplay```");
        objText.style.backgroundColor = objText.disabled ? "#dedede" : "";

		var fieldLabel = "";
		if(objText.fieldLabel)
		{
			fieldLabel = objText.fieldLabel;
		} else {
			fieldLabel = objSel.fieldLabel;
		}
		objText.fieldLabel = fieldLabel;
        objSel.fieldLabel = fieldLabel;

        if (objText.disabled) {
                objSel.name = strName;
                objText.name = strName + "_tmp";
                if(isFieldRequired) {
                        objSel.requiredValidate = isZeroLength;
                        objText.requiredValidate = '';
                }
                if(fromEditTable) {
                        saveFieldObj(objSel);
                }
        } else {
                objText.name = strName;
                objSel.name = strName + "_tmp";
                if(isFieldRequired) {
					if(fromEditTable) {
						fieldObjs[fieldObjs.length] = objText;
					}
                        objText.requiredValidate = isZeroLength;
                        objSel.requiredValidate = '';
                }
                if(fromEditTable) {
                        saveFieldObj(objText);
                }
        }
}

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

// function onchangeJFProjectPhase(value) {
//     var selectValue = this.options[this.selectedIndex].value;
//     // console.log("onchangeJFProjectPhase:", selectValue);
//     var fieldName= value;
//     if(fieldName=='JFProjectPhase') {
//         fieldName1 = 'JFIsLastQuote';
//         fieldName2 = 'JFIsTKOData';
//         var fieldId = "calc_" + fieldName1;
//         var tr = document.getElementById(fieldId);
//         console.log("onchangeJFProjectPhase:", tr);
//         console.log("onchangeJFProjectPhase0:", tr.children[0]);
//         console.log("onchangeJFProjectPhase1:", tr.children[3]);
//
//         if (selectValue == 'phase1') {
//
//             tr.children[3].className = "createLabel";
//             tr.children[0].className = "createLabelRequired";
//             validateRequiredField(fieldName1);
//             clearValidateRequiredField(fieldName2);
//         } else {
//             tr.children[0].className = "createLabel";
//             tr.children[3].className = "createLabelRequired";
//             validateRequiredField(fieldName2);
//             clearValidateRequiredField(fieldName1);
//         }
//     }
// }

// function  checkJFQuestionsList(){
//     var strJFProjectPhase = emxFormGetValue("JFProjectPhase").current.actual;
//     var strJFIsTKOData = emxFormGetValue("JFIsTKOData").current.actual;
//
//     console.log("checkJFQuestionsList:", strJFProjectPhase);
//     console.log("checkJFQuestionsList:", strJFIsTKOData);
//     if (strJFProjectPhase != "phase1" && strJFIsTKOData === "Yes"){
//         const  domJFQQ = document.getElementById('JFQuestionsList');
//         const domJFQQFileId=  document.getElementById("JFECRQQFileId2");
//         if (!(domJFQQFileId.value)){
//             sendMess("\u5fc5\u987b\u4e0a\u4f20\u95ee\u9898\u6e05\u5355\u9644\u4ef6","The attachment of the problem list must be uploaded");
//         }
//     }
// }

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

function clearValidateRequiredField(fieldName) {
    const fieldNameObj =  FormHandler.GetField(fieldName);
    fieldNameObj.HandlerField.requiredValidate = "";
}


function removeQuestionsListDocument() {
    const JFECRQQFileId2=  document.getElementById("JFECRQQFileId2");
    const JFReplace2=  document.getElementById("JFReplace2");
    JFECRQQFileId2.value="";
    JFReplace2.innerHTML = '';
}

function removeBeforeDocument() {
    const JFECRQQFileId2=  document.getElementById("JFPCRBeforeFileId");
    const JFReplace2=  document.getElementById("JFPCRBeforeReplace");
    JFECRQQFileId2.value="";
    JFReplace2.innerHTML = '';
}

function removeAfterDocument() {
    const JFECRQQFileId2=  document.getElementById("JFPCRAfterFileId");
    const JFReplace2=  document.getElementById("JFPCRAfterReplace");
    JFECRQQFileId2.value="";
    JFReplace2.innerHTML = '';
}

function checkJFProjectPhases() {
    var strJFProjectPhase = emxFormGetValue("JFProjectPhase").current.actual;
    // var strJFIsTKOData = emxFormGetValue("JFIsTKOData").current.actual;
    fieldName1 = 'JFIsLastQuote';
    fieldName2 = 'JFIsTKOData';
    var fieldId = "calc_" + fieldName1;
    var tr = document.getElementById(fieldId);
    console.log("checkJFIsLastQuote:", tr);
    console.log("checkJFIsLastQuote:", tr.children[0]);
    console.log("checkJFIsLastQuote:", tr.children[2]);

    if (strJFProjectPhase == 'phase1') {

        tr.children[2].className = "createLabel";
        tr.children[0].className = "createLabelRequired";
        validateRequiredField(fieldName1);
        clearValidateRequiredField(fieldName2);
    } else {
        tr.children[0].className = "createLabel";
        tr.children[2].className = "createLabelRequired";
        validateRequiredField(fieldName2);
        clearValidateRequiredField(fieldName1);
    }
}

function checkInputIsNull (){
    const strInputValue = this.value;
    const strFieldTitle = this.title;
    console.log("strFieldTitle:{}",strFieldTitle);
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

function checkCheckBoxIsNull (){
    const strInputValue = this.value;
    const strFieldTitle = this.title;
    console.log("strFieldTitle:{}",strFieldTitle);
    var strIsPlatformPart = emxFormGetValue("JFIsPlatformPart").current.actual;
    //是否平台间为 否直接通过
    // if (strIsPlatformPart == "No"||strIsPlatformPart == "N"){
    //     return true;
    // }
    // const  options = FormHandler.GetField("JFAffectsFactory").HandlerField;
    // let isAlert = false;
    // for (let i = 0; i < options.length; i++) {
    //     const option = options[i];
    //     console.log("option.checked:{}",option.checked)
    //     if (option.checked){
    //         isAlert = true;
    //         break
    //     }
    // }
    // if (!isAlert){
    //     //拿到浏览器语言
    //     var language = navigator.language || navigator.userLanguage;
    //
    //     if (language.includes("zh") || language.includes("zh-CN")) {
    //         strMess = `\u5fc5\u987b\u8f93\u5165\u6709\u6548\u503c\uff1a \u5f71\u54cd\u5de5\u5382`;
    //     }else {
    //         strMess = `A valid value must be entered: Affects the Factory`;
    //     }
    //     alert(strMess);
    //     return false;
    // }
    return true;
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

