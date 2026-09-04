# ENOVIA UI 配置本地帮助完整手册

本手册由 `D:\文档\本地帮助文档` 中的 SingleFile HTML 自动抽取生成，跳过 `index.html`。

- [返回索引](./ENOVIA_UI_Config_AI_Index.md)
- [查看配置字典](./ENOVIA_UI_Config_AI_Config_Dictionary.md)

## 来源页面

- [Adding Rows to Selected Rows](#adding-rows-to-selected-rows) (`Adding Rows to Selected Rows.html`)
- [Complete Cell Information](#complete-cell-information) (`Complete Cell Information.html`)
- [Deleting Rows](#deleting-rows) (`Deleting Rows.html`)
- [Displaying Validation Errors](#displaying-validation-errors) (`Displaying Validation Errors.html`)
- [URL Parameters Accepted by emxIndentedTable.jsp](#emxindentedtable-url) (`emxIndentedTable_URL.html`)
- [Enabling / Disabling Cell Edit by Object / Relationship ID](#enabling-disabling-cell-edit-by-object-relationship-id) (`Enabling _ Disabling Cell Edit by Object _ Relationship ID.html`)
- [Enabling / Disabling Cell Edit by Row ID](#enabling-disabling-cell-edit-by-row-id) (`Enabling _ Disabling Cell Edit by Row ID.html`)
- [Expanding Rows](#expanding-rows) (`Expanding Rows.html`)
- [Settings for Fields in Web Form Objects](#formsetting-url) (`FormSetting_URL.html`)
- [URL Parameters Accepted by emxForm.jsp](#formurlparameters) (`FormURLParameters.html`)
- [Getting Cell Values by Object ID](#getting-cell-values-by-object-id) (`Getting Cell Values by Object ID.html`)
- [Getting Cell Values by Row ID](#getting-cell-values-by-row-id) (`Getting Cell Values by Row ID.html`)
- [Getting Child Column Values](#getting-child-column-values) (`Getting Child Column Values.html`)
- [Getting Child IDs](#getting-child-ids) (`Getting Child IDs.html`)
- [Getting Current Cell Details](#getting-current-cell-details) (`Getting Current Cell Details.html`)
- [Getting Parent Column Values](#getting-parent-column-values) (`Getting Parent Column Values.html`)
- [Getting Parent ID](#getting-parent-id) (`Getting Parent ID.html`)
- [Load Mark Up](#load-mark-up) (`Load Mark Up.html`)
- [Appendix: MQL Parser Keywords](#mql-parser-keywords) (`MQL_Parser_Keywords.html`)
- [Refreshing Rows](#refreshing-rows) (`Refreshing Rows.html`)
- [Refreshing Table Columns](#refreshing-table-columns) (`Refreshing Table Columns.html`)
- [Refreshing the Header Subtitle](#refreshing-the-header-subtitle) (`Refreshing the Header Subtitle.html`)
- [Reloading Cell Values](#reloading-cell-values) (`Reloading Cell Values.html`)
- [Removing Selected Rows](#removing-selected-rows) (`Removing Selected Rows.html`)
- [About Selectables](#selectables) (`Selectables.html`)
- [Selecting Rows](#selecting-rows) (`Selecting Rows.html`)
- [Setting Cell Value by Row ID](#setting-cell-value-by-row-id) (`Setting Cell Value by Row ID.html`)
- [Setting Cell Values By Object / Relationship ID](#setting-cell-values-by-object-relationship-id) (`Setting Cell Values By Object _ Relationship ID.html`)
- [Setting the HTML Value of a Cell by Row ID](#setting-the-html-value-of-a-cell-by-row-id) (`Setting the HTML Value of a Cell by Row ID.html`)
- [Settings](#settings) (`Settings.html`)
- [Settings for Columns in a Structure Browser](#tablecolumns-setting) (`TableColumns_Setting.html`)
- [Settings for Toolbar Menu Objects](#toolbar-setting) (`Toolbar_Setting.html`)
- [URL Parameters](#urlparameters) (`URLParameters.html`)

<a id="adding-rows-to-selected-rows"></a>
## Adding Rows to Selected Rows

来源：`Adding Rows to Selected Rows.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

The `addToSelected` API allows you to paste a row either above or below the selected row. This API is supported for details and graphics view of the structure browser.

The `<data status>` element includes the `pasteBelowOrAbove="true"` attribute/ value, and the `<item>` element includes the data about the object to be pasted including the `pasteAboveToRow` or `pasteBelowToRow` attribute with the row data.

For example:

```
<mxRoot>
<action> <![CDATA[refresh]]> </action>

<data status="committed"  
pasteBelowOrAbove="true"
>
<item oid="30536.59499.40784.21329" 
relId="30536.59499.40784.14515" pid="30536.59499.60784.65279" 

pasteAboveToRow="0,0"
 />
</data>
</mxRoot>
```

The pasted row can be set as editable or non-editable by adding the `RowEditable` (values of true or false) attribute to the <item> element as shown here:

```
<mxRoot>
<action>
<![CDATA[refresh]]>
</action>
<data status=" committed "  p
pasteBelowOrAbove="true"
>
<item 
RowEditable="false"
 oid="30536.59499.40784.21329" 
relId="30536.59499.40784.14515" pid="30536.59499.60784.65279" 

pasteBelowToRow="0,0"
 />
</data>
</mxRoot>
</mxRoot>
```

You can paste a row as the child of the selected row in committed or pending mode (defined by the data status element):

```
<mxRoot>
<action>add</action>
    <
data status='pending'
 fromRMB='false'>
       <item oid='43472.32595.47845.16772'
       relType='relationship_SBOrdersToLineItems' relId=''
       pid='40712.19048.41410.33' direction='from'>
       <column name='Description'>Description 00</column>
       </item>
    </data>
</mxRoot>
```

Usage:

```
var addXML = "<mxRoot>";
addXML += "<action><![CDATA[add]]></action>";
addXML += "<data status=\"committed\" fromRMB=\"true\">";
addXML += "<item oid=\"" + dragInfo.objects[0].oid + "\" relId=\"" + dragInfo.objects[0].rid + 
"\" pid=\"" + dropInfo.object.oid + "\" direction=\"\" pasteBelowToRow=\"" + dropInfo.object.id + "\" />";
addXML += "</data>";
addXML += "</mxRoot>";
emxEditableTable.addToSelected(addXML);
```

<a id="complete-cell-information"></a>
## Complete Cell Information

来源：`Complete Cell Information.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

For some APIs, the method returns `CompleteCellInfo`.

The information contains this data:

```
Object.rowID
```

```
Object.columnName
```

```
Object.relid
```

```
Object.objectid
```

```
Object.type
```

```
Object.level
```

```
Object.direction
```

```
Object.value.current.actual
```

```
Object.value.current.display
```

```
Object.value.old.actual
```

```
Object.value.old.display
```

```
Object.editable
```

The object.direction is with respect to the object's parent.

<a id="deleting-rows"></a>
## Deleting Rows

来源：`Deleting Rows.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API removes objects from the structure. The hierarchy below the removed object is also removed.

```
removedeletedRows()
```

Usage:

```
<mxRoot>
    <action refresh=”true” fromRMB=”true”><![CDATA[remove]]></action>
        <item id=”<rowIdToBeRemoved>”/>
        <message><![CDATA[<Any Message>]]></message>
</mxRoot>
```

Example:

```
var removeXML = "<mxRoot>";
removeXML += "<action refresh=\"true\" fromRMB=\"true\"><![CDATA[remove]]></action>";
removeXML += "<item id=\"" + dragInfo.objects[0].id + "\" />";
removeXML += "<message><![CDATA[]]></message>";
removeXML += "</mxRoot>";
removedeletedRows(removeXML);
```

<a id="displaying-validation-errors"></a>
## Displaying Validation Errors

来源：`Displaying Validation Errors.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API displays validation errors from a custom JSP page. If a row Id does not exist, the API skips that row and does not throw an exception.

```
emxEditableTable.displayValidationMessags("XML");
```

This API requires this XML as the input argument:

```
<mxRoot>
```

```
<object rowId="0,5"> 
```

```
    <error>Error: Object with the same name already exists.</
error>
```

```
</object>
```

```
<object rowId="0,3,2"> 
```

```
    <error>Error: Multiple connections are not permitted.</
error>
```

```
</object>
```

```
</mxRoot>
```

The rowIDs and the text of the error messages, must be defined by the JSP.

<a id="emxindentedtable-url"></a>
## URL Parameters Accepted by emxIndentedTable.jsp

来源：`emxIndentedTable_URL.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This table lists the parameters that you can use with emxIndentedTable.jsp. You can add these parameters to the href parameter for the component that calls the structure browser. They provide the main configurability control to the structure browser component.

| URL Parameter | Description | Possible / Default Values |
| --- | --- | --- |
| appendColumns | The name of the table that defines columns that you want to include in the structure browser being defined. Any columns added using this parameter are not available to users for Custom Table Views. That is, the user cannot remove or reorder any of the columns added by this parameter. | Table nameuirey |
| applyURL | Specifies the URL string to be submitted when a user clicks the Apply button after making changes to the structure browser in edit mode. This parameter can also specify custom apply functionality as described in Customizing the Apply Action . | javascript:methodName |
| autoFilter | Determines whether the filter tool displays in the toolbar, and overrides the Auto Filter setting on a table or structure browser column. | true (default)--The Filter tool displays in the page toolbar. false--The Filer tool does not display in the page toolbar. |
| buffer | This parameter overrides the emxFramework.FrezePane.Buffer=true system property defined in emxSystem.properties. The default for this URL property is true : the structure browser starts to cache the data for all rows as the structure browser opens. If you change the value to false , the structure browser does not automatically buffer when the page initially loads. It does, however, buffer the data when the user selects the vertical scroll bar. For large structures, buffer=true can take more time to fetch and render the complete structure. The percentage of buffering displays next to the object count during the buffering process. | true (default) false |
| calculations | Controls the display of the toolbar button. When true, this icon only displays on the toolbar if the structure browser contains at least one column defined as numeric. | true--Sigma icon shows if the structure browser contains at least 1 numeric column false (default)--Sigma icon does not show on the toolbar. |
| cancelLabel | Used only when the structure browser is in a pop-up window and in View mode. It is ignored for all other conditions. Shows a Cancel button in the footer that closes the pop-up window without any other actions. Can be a text string or a string resource id. | Cancel emxFramework.FreezePane.Close |
| cellwrap | Wraps the contents of the cell in a structure browser automatically. When set to false, does not wrap the contents of the cell in a structure browser. You can also enable or disable content wrapping for a particular structure browser page by passing the following URL parameter: cellwrap = true or cellwrap = false . This parameter is valid for desktop as well as mobile apps and for the structure compare, grid table, and full search pages. | Default = true for Desktop apps Default = false for Mobile apps |
| connectionProgram | The JPO:method used for operations such as resequence, Copy, Paste, and Cut & Paste to make the needed connection between child and parent objects. This parameter is only used to implement custom logic. | emxPart:connectEBOMParts emxDocument:connectFolders |
| crossHighlight | When the user selects a check box or radio button associated with a 3DLive Examine or 3DPlay graphic, opens that graphic image. If a 3DLive Examine channel has been defined (within a Powerview), the 3D image opens in that channel, otherwise the 3D image opens in a popup window. If the selectHandler parameter is passed, that Javascript function overrides this parameter. | true |
| customize | Enables (true) or disables (false) the ability for users to create customized versions of the page. This parameter overrides the emxFramework.UITable.Customization system property, and has no effect on other table or structure browser pages. If not provided, the value specified for emxFramework.UITable.Customization is used. All access controls defined for the system table are retained in the customized table. If a column does not have a label or alt value defined (and is not a file, checkbox, icon, image, or separator), the column name shows in the user's selection list, however, this name cannot be internationalized. | true false |
| direction | Direction use for expanding the object. When one or more relationship names are passed in, the object is expanded for the given relationship(s) using the specified direction. Not supported for non-object based structure browsers. | to from both (default) |
| directionFilter | Used to turn ON or OFF the direction filter shown in the header. By default the parameter is false and the direction filter is hidden. The parameter must be passed explicitly as true to show the filter. Not supported for non-object based structure browsers. | true false (default) |
| displayView | A comma-separated list of available views (details,thumbnails,graph) for the structured data. The View menu only shows options included in this URL parameter; that is, if you do not include thumbnails and graph in the parameter value, then users cannot select the thumbnail and graph display mode. This URL parameter override the emxFramework.Freezepane.view property in emxSystem.properties. | details details,thumbnails details,thumbnails,graph |
| editLink | Specify whether the view table should display the Mode button toggle button. When true, the Mode menu and disabled Edit menu (AEFSBEditActions) is included in the toolbar. | true false (default) |
| editRelationship | Defines a comma-separated list of relationships that can be used in conjunction with the cut and paste edit commands. The user can only cut or paste rows that are connected to the parent object using one of the defined relationships. | relationship_AffectedItems, relationship_EBOM |
| editRootNode | Defines if the root node can be selected. The default is true; if you want to prevent users from selecting the root node, pass this parameter set to false. | true (default) false |
| emxExpandFilter | When the structure browser page opens, the value for this parameter determines how many levels to automatically expand the structure. | 1 3 All |
| expandLevelFilter | Enable or disable the Expand filter for the structure browser page. This parameter overrides the expandLevelFilterMenu URL parameter. | true (default false |
| expandLevelFilterMenu | Specify the menu used to show the Expand level filter. | Administrative menu name AEFFreezePaneExpandLevelFilter (default) |
| expandProgram | Used to pass the name of the program to use for expanding the object. This parameter value should be assigned to the JPO name and the method name separated by a colon. The program can use the hasChildren=true/false parameter. When false, the node shows expanded (- symbol) so the user knows that node has no children. If this parameter is not used, then the user needs to click the + symbol to determine if a node has children or not. | <JPO Name:method Name> emxPart:getEBOM emxDocument:getFolders |
| expandProgramMenu | Can be assigned to an administrative menu or a command name, which contains the definitions of the JPO, method name and label. When command is passed in: Command settings program and function are used for getting the JPO name and method name for expanding the objects. The List Filter is not shown because there is only one JPO available to expand. When Menu is passed in: Commands connected to this menu are used for listing the options in the List Filter combo box. The system obtains the label for each item from the command's label. The program and function settings on individual commands are used as the JPO method for expanding the objects, upon selecting the options from the List Filter. By default, the first command in the list is used for expanding the objects. The commands connected to the menu honor all the access settings supported by the configurable component (such as Access Mask, Access Expression, and Access Program). | Administrative menu name Administrative command name For example: ENCBOMLists (menu) PMCFolderLists(menu) ENCBOMList (command) |
| Export (view mode only) | Shows or hides the Export tool on the page toolbar. When users click the tool, the system exports the table data to a file in the user's preferred format in their preferred format: CSV, HTML, or Text. Users choose their preferred format using the Preferences tool in the global toolbar. For information on configuring the export preference, see Export Table Data Format . For instructions on using the preference page, click Help from the preference page. If the user chooses CSV or Text for their export format, the export does not include Icon column type and programHTMLOutput columns. To get the column values exported when the column type is "programHTMLOutput," set "Export= true" for the column. | true (Default)--The Export tool displays, allowing users to export the table data. false--The tool does not display. |
| findMxLink | When true, show the mxLink icon/command button on the toolbar, which opens a search dialog box. | true (default) false |
| freezePane | Used to configure which column(s) should display as the freeze pane column in the structure browser. If this parameter is not passed in, the first column in the table is used as Freeze Pane column. A comma-separated list can be provided, and the columns are displayed in the order listed. If all of the table columns are passed, the last column listed for this parameter displays in the scroll pane. | Column name in the table Name Title Name,Title,Description |
| header | The content of the heading that appears at the top of the table page. If the structure browser is called by a command in an object's tree (category list), the text specified by this parameter is not displayed. | Any alphanumeric text or a string resource ID. header=Buyer Desk header=emxQuoteCentral.AssignedPackages.AssignedPackages |
| HelpMarker | Specifies the name of the help marker to call for context-sensitive help. | String The naming convention for help markers "emxhelp" followed by the object or feature and then the action, for example, emxhelproutecreate and emxhelpprojectedit. The marker is all lowercase with no spaces. |
| hideLaunchButton | Used with the advanced structure compare tool, which uses a PowerView window. Channels and tabs in PowerView windows, that often show unrelated data, normally include a launch button that opens that tab in a maximized window. For the advanced structure compare, you may not want your users to open more windows. If true, the launch command is hidden on the toolbar when the page shows in a PowerView (portalMode=true). | true false (default) |
| hideRootSelection | Shows or hides the check box for the root node when the structure browser is configured for a single root node and the selection URL parameter is set to multiple (ignored if selection is set to single or none or the structure browser includes multiple root nodes). | true false (default) |
| inquiry | Used when there is no program parameter passed in to emxIndentedTable.jsp. Specifies the inquiry administrative object to retrieve the business objects to include in the table and any inquiries for the table page filter list. If multiple inquiries are passed with the ',' separator, emxIndentedTable.jsp uses the first inquiry object to display the list in the table on page load and the remaining inquiry objects to add to the filter list in the header. If there is only one inquiry, the filter list in the header is not displayed. To build the filter list, you must use either JPOs or Inquiry objects. You cannot use both. | Names of inquiry administrative objects separated by commas. Only one inquiry is active at a time. inquiry=SCSBuyerDesk,SCSBuyerDeskAssigned inquiry=ENCAllParts, ENCReleasedParts |
| inquiryLabel | Available for view mode only. Used with the inquiry parameter. Specifies the labels for each inquiry in the filter list in the table page header. Each label is associated to the corresponding inquiry administrative object name specified in the inquiry parameter. The label may have the actual text or the string resource id for internationalization. |  |
| insertNewRow | Adds to the toolbar in edit mode. When passed, the page adds these commands to the toolbar: Create New Create new after Create new before Remove inserted row(s) | true false |
| jpoAppServerParamList | Allows session data to be passed to a JPO and uses the format: scope:attributeName where scope can be one of these values: application session request and the attribute must be a valid attribute used within the specified scope. The parameter can pass a comma-separated list of scope:attributeName values. The attribute values must be serializable. | application:<attributeName>,session:<attributeName>,request:<attributeName> |
| lookupJPO | The JPO name and method name to execute the lookup function for a row added to the structure browser (using +). The JPO is invoked when the user clicks Lookup Entry . When passed, the page adds these commands to the toolbar: Add existing Add existing after Add existing before Remove inserted row(s) | JPOName:methodName |
| massPromoteDemote | Overrides the system property emxFramework.Lifecycle.MassPromoteDemote.Enable for the specific page. The default value for this parameter is the value for the above property. The page must also include the State and Type columns. See Collaboration and Approvals Administration Guide : Properties for Configuring Mass Promote and Demote for details on setting property values that control the mass promote/demote feature. | true false |
| massUpdate (edit mode only) | Use to turn on or off mass update controls on the editable table page. | true (default)--Mass update is available for the page. false--Mass update not is available for the page. |
| massUpdateTCL | Use if the Range Program setting is defined for any column in the structure browser and you want the column to be available in the mass update tool. If the structure browser does not contain any columns that use a Range Program, you do not need to pass this parameter. | true--Range values for the affected columns will show in the mass update tool. false (default)--Range values for the affected columns will not show in the mass update tool. |
| mode | Indicates what mode to display the structure browser. This parameter is optional. | edit view (default) |
| multiColumnSort | Enables or disables multiple column sorting on a page. If disabled, the user can still sort by a single column if one is specified in the sortColumnName parameter. When set to false, multiple column sorting is also disabled in any custom tables users' create based on this system table. | true (default) false |
| objectId | Required parameter for this component. It represents the object ID of the root object in the structure. For multiple root nodes, the program parameter is used instead, however, if the program is not available, this id is used to get the root object details. | <OID for the root object in the structure> |
| onReset | The name of a JavaScript function that is invoked when a user clicks the Reset button on an editable Structure Browser page.The function must be included in the file defined in emxSystem.properties: eServiceSuiteAPPLICATIONNAME.UIFreezePane.ValidationFile = VALIDATIONFILENAME (either .js or .jsp) For example: eServiceSuiteEngineeringCentral.UIFreezePane.ValidationFile = emxEngineeringCentralFormValidation.jsp | resetEverything |
| pageSize | Enables pagination for structure browsers in flat mode (no expansions). When this URL parameter is passed, the structure browser initially loads the first set of n rows (for example, 25), and includes a set of pagination tools at the bottom right of the page. Users can select a specific page, or use the back and forward buttons to move through the pages. Users can also click the page button to enable or disable pagination. When pagination is enabled, sort works over all rows, but all other structure browser tools (such as Row Grouping and Find-In) only work on the currently-displayed rows. | integer value |
| parallelLoading | Enables Java parallel processing to improve the loading of data Parallel loading of data improves performance and is used whenever this parameter is set to true and there are more than 500 rows of data. | true false (default) |
| PrinterFriendly (view mode only) | Specifies whether the table page should include the Printer Friendly tool. You can have the system pass this parameter automatically by entering the Printer Friendly setting for the command object that calls the table page. | true (default) false |
| program | Specifies the JPO and method to generate the list of objects. When this parameter is passed, the objectId URL parameter is ignored. The JPO returns one or more objects. | <JPO name:Method name> AEFSearch:getPartList |
| programLabel | Available for view mode of the structure browser only. When the program parameter is used, this parameter defines the labels for each program in the filter list in the table page header. You can assign one or more labels (equal to the number of program values) separated by a comma ",". Each label is associated to the corresponding JPO object and method name specified in the program parameter. The label can contain the actual text or the string resource id for internationalization. | programLabel =All,Assigned programLabel = emxEngineeringCentral.Common.All, emxEngineeringCentral.Common.Released |
| relationship | Relationship names to be used for expanding the root object to display the table structure view. The same relationships are used while expanding the structure by clicking on the plus on the nodes. The relationship name passed in can be the symbolic name or the actual relationship name. Using the symbolic names is recommended. A property setting can be used to pass the relationship names. The property must be assigned to one or more relationships to be used for expansion. When one or more relationships are passed in, the program uses the list of passed-in relationship(s) only, to expand the object. If no relationship parameter is passed in, then it assumes all. <all> (default) - The given object will be expanded to get all the connected objects, irrespective of what relationship is used, to be displayed as child/parent objects. Not supported for non-object based structure browsers. | <relationship name> <list of comma separated relationship names> <property key assigned to one or more relationships> For example: relationship_EBOM, relationship_PartSpecification relationship_Employee all (default) |
| relationshipFilter | Used to turn ON or OFF the relationship filter shown in the header. By default the parameter is false and the relationship filter is hidden. You must explicitly pass the parameter as true to show the filter. Not supported for non-object based structure browsers. | true false (default) |
| resequenceRelationship | Used when the order of child objects is significant. Defines a comma-separated list of relationships that determine if a paste operation is a resequencing. Resequencing means that the child object was moved from one place in the list of child objects to another (under the same parent object). | relationship_AffectedItems, relationship_EBOM |
| rowGrouping | When true, the is included on the toolbar, allowing the user to group rows by up to 3 columns. See 对结构化内容中的行进行分组 to see how an end user can use this feature. | true (default) false |
| rowGroupingColumnNames | When using row grouping, you can provide a comma-separated list of up to 3 column names so that when the page is opened, it automatically groups the page by the values in the specified columns. |  |
| selectHandler | The name of a Javascript function to invoke with a user selects/deselects a check box or radio button. This parameter overrides the deafult behavior that uses functions (FreezePaneregister(strID)) and (FeezePaneunregister(strID)) when users select or deselect check boxes or radio buttons. |  |
| selection (view mode only) | Controls whether the table page adds a column of check boxes or radio buttons in the left-most column of the table. When set to single or none , the parameter objectCompare is false and the object compare icon does not display. | multiple--Users can select more than one row in the table. A check box displays in the left column of each row. There are no access restriction for the check boxes. If you pass this parameter, it overrides any check box column added to the table administrative object. single--Users can select one row in the table. A radio button displays in the left column of each row. none--A selection column is not added to the table page. (A check box column can still be displayed by adding the column to the table administrative object.) |
| showApply | When set to true, shows the Apply button in Edit mode. When set to false, Edit mode does not have an Apply button. | true (default) false |
| showClipboard | This menu is enabled by default and adds the to the page toolbar. The icon acts as a pull-down menu. If the user clicks , selected objects are added to the clipboard collection for that user. If the user clicks the arrow, the user can select the New/Add to Collections or Add to Clipboard Collection command. Set the value for this parameter to false to disable this feature. For File Summary Pages for apps, the default value for this parameter is false. | true (default) false |
| showPageURLIcon | When true, shows in the toolbar. This tool lets users copy the URL to the specific app page. When false, does not show in the toolbar. The default value for this parameter is defined by the emxFramework.Toolbar.ShowPageURLIcon property in emxSystem.properties . | true false |
| showRMB | Enables or disables the right-click menus on the page. When set to false, all right-click menus for that page are disabled. | true (default) false |
| showTabHeader | Applies only in Portal mode (when the page is displayed within a PowerView), enables or disables the page header. If false, the header text is not displayed in the PowerView tab. If true, the header text shows in the tab. | true false (default) |
| sortColumnName | The column names to use for sorting the column data, or none if no initial sorting is required. When the node is expanded, the system uses the same parameter to sort the list and display the child objects. By default, the first columns in the table view (Freeze Pane column) are used to sort. The sorting is "alphanumeric" (sorttype=alpha). Can be a comma-separated list of up to 3 column names. The page sorts by the first provided column, then by the second, then by the third. Used as the default Sort by settings in the Customize Table View dialog box. Columns defined with sortType=other setting cannot be used with multiColumnSort. | <name of the column> column1,column2,column3 none |
| sortDirection | Defines the sort order for the columns specified in the sortColumnName parameter. If a single value is passed, it applies to all columns, or you can pass a comma-separated list of values matching the number of columns in the sortColumnName parameter. Used as the default sort directions in the Customize Table View dialog box. | ascending (default) descending ascending,descending,ascending |
| subHeader | Creates a subHeader below the main header in the table header frame. If the structure browser is called by a command in an object's tree (category list), the text specified by the subheader parameter is displayed at the bottom of the window instead of in the header area. | The value can be any static text or a string resource id. For example: subHeader= emxEngineeringCentral.Common.BOMLevel subHeader=Bill of Material Level 1 The value can also include macros such as $<type> $<revision>. |
| submitLabel | For View mode only, shows a Submit button in the footer. If this URL parameter is not passed and submitURL is passed, then the default "Submit" is used as the label. Can be a text string or a string resource id. | Submit emxFramework.Common.Submit |
| submitURL | The JSP called when a user clicks the Submit button in the structure browser footer. | emxProcess.jsp ${SUITE_DIR}/emxProcess.jsp |
| table | Passes the table definition for displaying the structured view. | <table name> - system table For example: ENCBOMSummary PMCAllProjects |
| tableHeadClass | Customizes the style for a column header. | Class defined in dsecUIType-Custom.css |
| tableMenu | Can be assigned to an administrative menu or command name that contains the table name definition and label. When you pass command: Command setting table gets the table name for the table definition. The Table Filter does not display (because there is only one table available to view). When you pass menu: The system uses commands connected to this menu for listing the options in the Table Filter combo box. The system obtains the label for each item from the command's label. The system uses the table setting on individual commands as the table definition for displaying the view, upon selecting the options from the Table Filter. By default, the system uses the first command for the table definition. The commands connected to the menu honor all the access settings supported by the configurable component (such as Access Mask, Access Expression, and Access Program). | Administrative menu name Administrative command name For example: ENCBOMViews (menu) PMCFolderViews(menu) ENCBOMView (command) |
| TipPage (view mode only) | Specifies whether the table page should include the Tip Page tool and call a specific html or jsp when a user clicks the tool. If this setting is not included, the Tip tool is not included on the table page. | Name of a custom html or JSP page, including any path. The starting point for the directory reference is the content directory. For example, if you want to call an html file in ematrix/doc/customcentral and the content directory is ematrix/customcentral, you would add this parameter to the table.jsp: TipPage=./doc/custom-tailored/tippage.html |
| toolbar | A comma-separated list of UI menu names that add custom filters to the toolbar. Menus specified here are added as a new toolbar row beneath the Action toolbar. | PMCWBSTaskToolbar ECEBOMToolbar,ECEBOMFilter |
| TransactionType | Controls whether the table query is run within an Update transaction or Read transaction. Use Update transactions whenever the system need to update the database while fetching the object list. | read (default)--The table query is not run within an Update transaction. update--The table query (inquiry or JPO) runs within an Update transaction. |
| triggerValidation | Controls the Validate command on the toolbar. If true or empty, the command shows on the toolbar. If set to false, the trigger validation icon does not display on the toolbar. | true (default) false |
| type | Used to pass the type name for filtering the expanded structure when the table displays and also when the structure expands. The type name can be the symbolic name or the actual type name. Using the symbolic name is recommended. You can use a property setting to pass the type names. The property must be assigned to one or more types for filtering. If you pass one or more types, the program uses the list of passed-in types to filter the object list. If you pass no parameter, the system uses all. all (default) - Displays all objects and performs no filtering. This is an optional parameter. Not supported for non-object based structure browsers. | <type name> <list of comma separated type names> <property key assigned to one or more types> For example: type_Part,type_ECO type_Folder all (default) |
| typeFilter | Used to turn ON or OFF the type filter shown in the header. By default the parameter is false and the type filter is hidden. You must explicitly pass the parameter as true to show the filter. Not supported for non-object based structure browsers. | true false (default) |

<a id="enabling-disabling-cell-edit-by-object-relationship-id"></a>
## Enabling / Disabling Cell Edit by Object / Relationship ID

来源：`Enabling _ Disabling Cell Edit by Object _ Relationship ID.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API enables or disables edit mode for a row identified using the RelId, ObjectId, and column name.

```
emxEditableTable.setCellEditableByObjectRelId
```

This API requires these input arguments:

- `relId`. The relationship ID of the needed cell.

- `objectId`. The object ID of the needed cell.

- `colName`. The column name of the needed cell.

- `flag`. If true, sets the editMask attribute of the row to false (allows to row to be edited; if false, removes the editMask attribute from the row (the row cannot be edited).

- `isRefreshView`. A boolean value that when true (the default), refreshes the structure browser page.

Example:

```
Var relId = [];
```

```
Var objectId =[];
```

```
Var colName = Units
```

```
Var isRefreshView = true;
```

```
emxEditableTable.setCellEditableByObjectRelId(relId,objectId, 
colName,true,isRefreshView)
```

In this example, row [0,1] is set to be editable.

<a id="enabling-disabling-cell-edit-by-row-id"></a>
## Enabling / Disabling Cell Edit by Row ID

来源：`Enabling _ Disabling Cell Edit by Row ID.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API enables or disables edit mode for a cell identified using the RowId and column name.

```
emxEditableTable.setCellEditableByRowId
```

This API requires these input arguments:

- `rowId`. The row ID of the needed cell.

- `colName`. The column name of the needed cell.

- `flag`. If true, sets the editMask attribute of the row to false (allows to row to be edited; if false, removes the editMask attribute from the row (the row cannot be edited).

- `isRefreshView`. A boolean value that when true (the default), refreshes the structure browser page.

Example:

```
Var rowId = [0,1];
```

```
Var colName = Units
```

```
Var isRefreshView = true;
```

```
emxEditableTable.setCellEditableByRowId(rowId,colName,true,isRefreshView)
```

In this example, the cell in the Units column of row [0,1] is made editable.

<a id="expanding-rows"></a>
## Expanding Rows

来源：`Expanding Rows.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API expands the rows to the specified level. If the structure does not have the number of levels, the API expands the entire structure. If a row Id does not exist, the API skips that row and does not throw an exception. This API is defined in emxUIFreezePane.js.

```
emxEditableTable.expand
```

This API requires this input argument:

- arrRowIds. An array of row IDs.

- level. The number of levels to expand; must be a positive integer value or the keyword `All`.

Example:

```
var arrRowIds = new Array (); 
var level = "1" ; 
arrRowIds.push( "0,0" ); 
arrRowIds.push( "0,0,0" ); 
arrRowIds.push( "0,1" ); 
emxEditableTable.expand(arrRowIds,level);
```

Any rowIds that to do not match rows in the context structure browser are ignored. If there are no matching rows, an error displays to the user.

<a id="formsetting-url"></a>
## Settings for Fields in Web Form Objects

来源：`FormSetting_URL.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This table lists and describes the settings for web form objects. The name and value for each setting are case sensitive.

| Setting | Description | Accepted Values/Examples |
| --- | --- | --- |
| Access Expression | Controls access to the field based on a valid expression. The system evaluates the expression at runtime. If the expression evaluates to True and no other access control prevents access, the component is shown. Depending on the type of expression defined, the program may or may not need a valid objectID. |  |
| Access Function | The name of the JPO method to invoke in the JPO specified for the Access Program setting. The Access function gets the input parameter as a HashMap that contains all the request parameters passed into the JSP page. The JPO method must return a Boolean object. If the returned value is true and no other access control prevents access, the component displays. If false, it is hidden. To see a sample JPO method to control access, see Sample JPO for Controlling Access . Use this setting to evaluate access independent of users' roles. To allow users to configure the visibility of fields, use setAccess as the default. For example, users can hide a specific field in view or edit mode as required. | The name of an access check method in the JPO specified in the Access Program setting, such as: emxAccessCheck() Or setAccess |
| Access Mask | Specifies the accesses the user must have for the current business object for the field to display. Make sure that an objectId is available before configuring this setting for configurable toolbar menus and commands. If the user does not have all the specified accesses in the business object's policy for the current state, the system hides the component. If the user has the access and no other access control prevents access, the system displays the component. You can specify multiple accesses by separating the accesses with a comma. | Modify Delete ToConnect ToDisconnect FromConnect FromDisconnect Modify,Delete FromConnect,FromDisconnect |
| Access Program | Controls access to the field based on the output from a method in the specified JPO program. You must define the program in MQL . This setting requires that you also specify the Access Function setting. If Access Function is not set, the system ignores the Access Program setting. The following input values are required for the program: A list of all of the request parameters in a HashMap Method name as a string JPO Program Name as a string Context The output must be a Boolean. To allow users to configure the visibility of fields, use emxGenericFields as the default. For example, users can hide a specific field in view or edit mode as required. | Name of a JPO defined as a program object, such as: emxAEFCollectionAccess Or emxGenericFields |
| Add Icon | If you enable users to add existing items, you can define the icon you want to use (such as default path /common/images, but no macros are supported). | ../images/iconActionAdd.png |
| Add Link | Enables users to add existing items by setting the appropriate URL or command. | APPMeetingAddAttendee (default) .../engineeringcentral/emx /emxEntgrDocumentAddExistingIntermediate.jsp? mode=PartRefDocAddExisting&suiteKey =EngineeringCentral&StringResourceFileID =emxEngineeringCentralStringResource& SuiteDirectory-engineeringcentral &targetLocation=popup |
| Add Text | Displays text for a link. |  |
| Add Tooltip | Defines a tooltip when you enable the AddExisting capability. | Add Existing (default) |
| Additional Query | When Type Ahead is configured on the field (by setting the RangeHref to emxFullSearch.jsp or using a predefined Type Ahead Chooser), this setting defines additional field/selection criteria used to restrict the selection list. See About Automatic Type Ahead . | <FieldName1>=<select expression1>:<FieldName2>=<select expression2>: |
| Admin Type | Use to translate fields whose values are names of administrative objects or ranges of attributes. For example, suppose you are configuring a field that shows an object's current state and you want the state name to be translated. You would add this setting and set the value to State. The translations for administrative object names are stored in the emxFrameworkStringResource.properties files, as described in Internationalizing Dynamic UI Components . | These keywords get the field values translated for the appropriate type name: Type State Role Relationship Policy Group Vault Attribute (for translating the attribute name, not the range values) To translate attribute and range values, specify the symbolic name of the attribute, which starts with "attribute_" and is followed by the attribute name with no spaces. For example: attribute_UnitOfMeasure attribute_PartClassification |
| Allow Guide Edit | When true, users can manually edit the form row for this field. Applicable only when the range parameter is set to a URL or when the setting format is assigned to date or for fields of type combobox. It is ignored in all other cases. When this setting is true, the Admin Type setting is ignored. | false (default)--Guide entry is not allowed. true--Guide entry is allowed. |
| Alternate OID expression | By default, when a field's data is configured to show as a hyperlink using the href parameter, the system passes the ID for the business object the form page applies to. Using this setting, you can have the system pass the ID(s) for a different object, namely, the ID(s) for the object(s) returned from the expression specified in this setting. | $<to[relationship_NewPartPartRevision].from.id> $<to[relationship_EBOM].from.id> To see an example of a field that uses an Alternate OID expression and an Alternate Type expression, see Field Values with Hyperlinked Data Using an Alternate OID and Alternate Type Icon . |
| Alternate Type expression | When the Show Alternate Icon setting is true, this expression is used to obtain the object type. Based on the obtained type, the corresponding icon is displayed. | $<to[relationship_NewPartPartRevision].from.type> $<to[relationship_EBOM].from.type> |
| Calendar Function | Use to specify the name of the method in the JPO specified in the Calendar Program setting that retrieves the non-working days based on the calendar defined for the location. | The name of a function in the Calendar Program JPO, such as: getNonWorkingDays |
| Calendar Program | Use to specify the name of a JPO that contains a method to get the non-working days for a calendar. | The name of a JPO, such as: emxWorkCalendar |
| Category | Used to select attributes with the same uiform_Category property setting. | D2MBusiness EC Technical |
| Cols | Used when the input type is set to textarea. This setting limits the length of the textarea on the form and specifies the visible width in average character widths. If not specified, it uses the HTML default, which is 25. | 25 40 50 |
| Column Count | The number of name/value columns to draw horizontally. | 1 (default) 2 3 n |
| Column Style | This setting is only valid for Edit mode; it does not work with View mode. Defines styles from the CSS for the specific field. To define alignment, use one of these values: left-align center-align right-align For numeric fields, the default is left-align. | left-align |
| Command | Defines the URL of a command that is executed on the inside panel. | APPMeetingAgenda |
| Counter Link | Defines the link that opens when you click the counter (enter URL or command). | APPReferenceDocumentsTreeCategory |
| Counter Style | Defines the CSS settings to format the counter. | font-weight:bold; |
| Counter Tooltip | Defines the tooltip for the counter. |  |
| Create Exclude | Comma-separated list of attributes that will not be included in the web form when opened in Create mode when a field on the web form has been defined as Field Type = Dynamic Attributes. | attribute_Cost |
| Create Link | Enables users to create related items by setting the appropriate URL or command. | ../components/emxCommonDocument PreCheckin.jsp?objectAction=create |
| Create Icon | If you enable users to create new items, you can define the icon to use, (default path /common/images is used, but no macros are supported). | iconActionCreate.gif (default) |
| Create Text | Displays text for a link. |  |
| Create Tooltip | Defines a tooltip when you enable users to create new items. | Create New Document (default) |
| Decimal Format | Defines the decimal precision (number of digits after the decimal point) for displaying values in numeric fields. | 5 2 |
| Decimal Precision | Defines the decimal precision for all calculations for this form. The system properties setting emxFramework.FormCalculations.DecimalPrecision defines the system-wide value; this setting overrides that value. You only need to use this setting if you want to use a value other than the system-wide value (defined in emxSystem.properties). | Any positive integer. |
| Default | If a field's value is empty or null and this setting is defined, the default value is displayed for the field. | The default value you want to display. This can be a string resource key or the actual characters you want to fill in as the default. The wildcard (*) can be used for search criteria fields. emxFramework.Common.default All * |
| Delimiter | When using Input Type = dynamictextarea setting, this setting defines the character that separates values when the field is in View mode. In Edit mode, each value shows on a separate line. A comma is the default delimiter. If you specify any of these characters as the delimiter, a comma is used instead: $ \ ' " * \| ? ( ) > | <any string value> , (default) |
| Display Format | Specifies the number of the date format for any field where the value of the format setting is "date." See Date/Time Fields in Forms and Tables . | These are Java standard values of Date Format to display a date in a specific format. 3 - SHORT (12/12/52) 2 - MEDIUM (Dec 12, 1952) 1 - LONG (December 12, 1952) 0 - FULL (Tuesday, December 12 1952 AD) Default is set in emxSystem.properties: emxFramework.DateTime.DisplayFormat=MEDIUM. emxSystem.properties uses words, but the Display Format setting uses numbers. |
| Display Time | Controls whether the time displays with the date for fields whose format is set to date. If no time zone preference is set, then the DateTime is shown in the browser's time zone. The time is shown in terms of GMT+/- hh:mm, (for example, Saturday, August 21, 2004 12:45:00 PM GMT-04:00). To get the time in a format like EST or PDT, set the time zone preference to a specific zone. See Date/Time Fields in Forms and Tables . | true false Default is set in emxSystem.properties for the property emxFramework.DateTime.DisplayTime = false |
| Dynamic Field Function | When the Field Type setting is set to Dynamic, this setting defines the method in the Dynamic Field Program used to retrieve attributes to add to the form. getDynamicAttributes is provided by BPS for this purpose. | getDynamicAttributes |
| Dynamic Field Program | When the Field Type setting is set to Dynamic, this setting defines the JPO that contains the method used for adding dynamic attributes to forms. emxUnifiedTyping is provided by BPS for this purpose. | emxUnifiedTyping |
| Editable | Use to indicate whether the field is displayed as editable or read only. Only applies for Edit mode. View mode ignores the setting. | true (default)--Users can edit the field when shown on the Edit mode form. false--Users cannot edit the field when shown on the Edit mode form. The field looks just like it does in View mode except it is never hyperlinked. |
| Edit Count | Restricts access to edit capabilities (Add Link, Create Link) by the number of related items. For example, you can restrict create commands only if there are no related items yet. | 0 |
| Edit Exclude | Comma-separated list of attributes that will not be included in the web form when opened in Edit mode when a field on the web form has been defined as Field Type = Dynamic Attributes. | attribute_Cost |
| Edit States | Restricts access to edit capabilities (Add Link, Create Link) by the state of an object. | Preliminary |
| Expand Level | Defines the expansion level. | 1 (default) 0 (for all levels) |
| Export | Specifies whether field data is exported to csv or not. Use this to change the export value on a field-by-field basis. | true false |
| Field Column Headers | Used with the Field Type=Table Holder setting. Specifies the labels for the column headings. The number of labels should be the same as the value for the Field Table Columns setting and should be separated by a comma. You can specify either a string resource ID for the text string or the actual text string that should appear. To internationalize the text, you must use a string resource ID. See Fields that are Grouped . | Comma-separated list of column heading labels: Min,Max,Avg |
| Field Row Headers | Used with the Field Type=Table Holder setting. Specifies the labels for the row headings. The number of labels should be the same as the value for the Field Table Rows setting and should be separated by a comma. You can specify either a string resource ID for the text string or the actual text string that should appear. To internationalize the text, you must use a string resource ID. See Fields that are Grouped . | Comma-separated list of row heading labels: Weight,Volume |
| Field Size | Determines the width of a textbox input type field. The width is given in pixels except when Input Type is textbox or not set. In that case, its value refers to the (integer) number of characters. | 30 20 is the default. |
| Field Table Columns | Used with the Field Type=Table Holder setting. Defines the number of columns for the table. See Fields that are Grouped . | 2, 3, ... |
| Field Table Rows | Used with the Field Type=Table Holder setting. Defines the number of rows in the table. See Fields that are Grouped . | 1, 2, ... |
| Field Type | This setting is used: To indicate that the field's data should be obtained from a program or image instead of an expression. When the field data is obtained from an expression, if the data is basic information or an attribute. The system needs to know whether a field's data is basic information or an attribute in order to update the information correctly. Specifying whether the field is basic or an attribute is only required for fields that will be editable. To indicate the field is a placeholder field that defines fields to display in a table or group. For more information about specifying field data, see Form Fields . The Group Holder setting has been deprecated. | program--The values for this field are obtained from a program (JPO). The program and function name are required as settings. programHTMLOutput--Same as the program setting, except the field Field Type value output is in HTML format. Field values are placed in the table cell between <td> and </td> tags. This setting ignores other field settings such as Show Type Icon, href, format, and Alternate OID expression. This field type does not support the Validate setting. The programHTMLOutput setting is mandatory for attachment and attendee fields. Dynamic Attributes--Displays all attribute/value pairs associated with the context object in the properties page. ClassificationPaths--Used only with IP Classification . Displays the paths where the object is classified, with a separator between hierarchies. This separator is configurable with the IP Classification property string emxLibraryCentral.ClassPathSeparatorString. The default separator is '-----'. ClassificationAttributes--Used only with IP Classification . Displays the attributes acquired via classification. If the object is classified, it displays the classification name as the heading, and then displays a subheading with the name of the attribute group, and then attributes and their values acquired from the attribute group. If the same attribute is repeated in another attribute group or in another classification, this field displays a message underneath the field: "This value also appears in another Attribute Group." If a user modifies one attribute and has other occurrences in different attribute groups, this automatically updates all other occurrences of the attribute. If the object is classified but no attributes are acquired via classification, this field does not display anything. image--The field's value is the primary image associated with the business object. basic--The field displays basic information for the business object. Basic information includes name, type, originated, policy, and so on. Specifying basic as the field type is only needed when the field is editable. The only editable basic information is: type, name, revision, current, policy, description, owner, vault. attribute--The field displays values for an attribute on the business object, such as Originator or Weight. Section Header--Adds a new section heading between the form fields. The setting Section Level determines the heading level. See Field as Section Header and Separator . Section Separator--Adds white space to separate fields and sections. Table Holder--Arranges the fields under the field in columns and rows. Table Holder fields serve as placeholder fields to define the fields to display in a table. Also see these settings: Field Column Headers, Field Row Headers, Field Table Columns, Field Table Rows. emxTable--Embeds a configurable table in the form. Used with the table setting and either the inquiry or program setting. See Field that Embeds a Configurable Table . Dynamic--adds any attributes created using the Parameterization Console to the form. You must also define the Dynamic Field Function and Dynamic Field Program settings. |
| Filter BUS | Defines the filter for related business objects during expansion. | current == 'Active' |
| Filter REL | Defines the filter for relationships. | attribute[Approved] == 'No' |
| findMxLink | When true, show the mxLink icon/command button on the toolbar, which opens a search dialog box. | true (default) false |
| format | Use to specify the type of data in the field. If the Editable setting is true and the field is on an Edit mode form, the system uses these format values to validate the field value. Validation takes place on the client side, before updating the object displayed in the form. For more information about validation, see Validating Form Field Data . If you set the format to numeric , you can also set the Number Format setting to true . That setting allows the value to be displayed using the thousands and decimal separators based on the user's browser locale. If you set the format to user , the field content should be a person, such as a task assignee. The field displays the person's name using the firstname lastname format. To support the date compare logic, whenever the field format is "date", an additional hidden parameter is added to the form with the suffix, _msvalue, assigned to the web form field name. If there is a valid display value for the date, the hidden parameter is assigned with the value that is the equivalent of displayed date in milliseconds (calculated from midnight, January 1, 1970). This hidden parameter value can be used by the validation methods to compare two dates. Note: The hidden parameter gets updated only when the out-of-the-box calendar component is used to change the date. So if the field type is changed to guide edit, the hidden parameter may not have the updated milliseconds value when the field is manually changed. In this case the validation method can simply ignore the date compare and must depend on the server side validation. The client side validation can be ignored by checking if the field is read-only. | date--Uses the tag lib to format the displayed field value based on the browser locale setting. To see an example of a field configured as a date field and that has date validation, see Field Value with Dates . currency numeric--Use if the value must be validated as a number before updating the values. Applicable only in Edit mode. email--Displays the column values as an email address. When a user clicks the email address, the email editor configured in the client is presented. user--Shows a person's name using the fullname format (that is, "John Smith", and not "Smith, John"). |
| From | Defines the direction for the expansion. | true (default) false |
| function | The name of the method to call within the JPO program specified in the program setting. This method within the JPO is used to get the field values if the setting "Field Type" is set to "program" or "programHTMLOutput". For more information, see Field Values Obtained from a Program . The fieldRelatedItems setting is mandatory for attachment and attendee fields. Set to fieldImages for Enhanced Images field. | The name of a function in the program JPO, such as: getAssignedBuyerDesk getPackageAccess getParentPart |
| Gallery | Defines if the preview image is available or not. | True (true) |
| Gallery Zoom | Defines if zoom is available in preview or not. | True (true) |
| Height | Defines either one of the following for the image field: attribute[Approved] == 'No' Height Defines the height of a panel in pixels. | 64 (default) or 250 (300) |
| Hide Label | Defines if the form field label appears or does not appear on the left side. | true false (default) |
| Group Name | Used for grouping fields in web forms. The consecutive fields with same group name are considered a group. See Fields that are Grouped . | The name of the group. |
| Help Marker | Specifies the name of the help marker to call for context-sensitive help. In the href URL called when the field data is clicked, the system passes a parameter called HelpMarker and includes the marker text specified for this setting. | The naming convention for help markers is the page title, as displayed at the top of the visual page, prefixed with "emxhelp". The marker is all lowercase with no spaces. |
| Hide Label | Displays or hides the label for a particular row on a form. | true false (default) |
| Hide Mode | A comma-separated list indicating that if the user is accessing the system via that mode, then this field will be hidden. Value Hide field if access mode is: Show field if access mode is: Desktop Desktop Cloud or Mobile Mobile Mobile Desktop or Cloud Cloud Cloud Desktop or Mobile !Desktop Mobile or Cloud Desktop !Mobile Desktop or Cloud Mobile !Cloud Mobile or Desktop Cloud If this setting is not defined, then the field is always visible as long as the user has access to it contextually. | Desktop Mobile,Cloud !Mobile |
| Value | Hide field if access mode is: | Show field if access mode is: |
| Desktop | Desktop | Cloud or Mobile |
| Mobile | Mobile | Desktop or Cloud |
| Cloud | Cloud | Desktop or Mobile |
| !Desktop | Mobile or Cloud | Desktop |
| !Mobile | Desktop or Cloud | Mobile |
| !Cloud | Mobile or Desktop | Cloud |
| Highlight Nonzero Icon | Defines the icon that appears if there are related items matching the given expand criteria. The path must be relative to /common/images . | iconStatusCheckmark.gif |
| Highlight Nonzero Link | Defines the link that opens if a user clicks a highlighted icon. |  |
| Highlight Nonzero States | Limits the highlighting of missing items to certain states. | Marketing,Review |
| Highlight Nonzero Style | Defines the style to use for the counter if there are related items that match the given expand criteria. | color:#009c00;font-style:italic; |
| Highlight Nonzero Tooltip | Defines the tooltip that appears when a user pauses over a highlighted icon. | There ARE related items Documents are checked in. |
| Highlight Zero Icon | Defines the icon that appears if there are no related items that match the given expand criteria. The path must be relative to /common/images . |  |
| Highlight Zero Link | Defines the link that opens if a user clicks the highlighted icon. |  |
| Highlight Zero States | Limits the highlighting of missing items to certain states. |  |
| Highlight Zero Style | Defines the style to use for the counter if there are no related items that match the given expand criteria. | color:#cc0000;font-style:italic; |
| Highlight Zero Tooltip | Defines the tooltip that appears when users pause over a highlighted icon. | There are NO related items. |
| Icon Link | Defines the command or URL that opens when you click a type icon. The path must be relative to \common directory . | emxTree.jsp?DefaultCategory= ContextIssueListPage download (to enable file download) |
| Icon Tooltip | Defines the data that appears in an icon tooltip. | type,name,revision |
| Image | Use to specify an image file when the field value should be an image only. This setting is required when the Field Type setting is set to image. This file must exist in the application server (not in the database). You can make the image a hyperlink by including a URL in the href parameter. To see an example of a field with an image, see Field Values as Hyperlinked Image . | images/newPart.gif images/EditItem.gif |
| Image Size | When the web form includes a Field Type of image, this setting defines which size of the primary image associated with the business object should display in the column. The pixel dimensions for these sizes are defined using the emxComponents.Image. Size ImageSize property (see Collaboration and Approvals Administration Guide : Properties for Configuring the Image Manager ). | format_mxSmallImage (default) format_mxLargeImage format_mxMediumImage format_mxThumbnailImage |
| Input Type | Only used for forms and table columns in edit mode. Specifies the type of HTML control to display for user input. Although multiple choices can be displayed in a web form, only one selection can be saved during edit. If you want to disable the ability to make multiple selections during view of web form, you need to use programHTMLoutput and the html tag that does that. Use the radiobutton or combobox input types for fields that require a single selection. If you want to save multiple selections, a custom JPO needs to be written using the check box or listbox input types which might delimit the choices in the attribute using the Studio Customization Toolkit. If you specify check box or radiobutton for an attribute with the String datatype, the attribute must be defined with a range or an error occurs when the form is in Edit mode. If using dynamictextarea, the Delimiter setting also needs to be defined if you want to use a delimiter other than the default (comma). | textbox-Default. Provides a single-line box for typing text. This graphic shows a field configured as a Text Box with the Required setting equal to true. If the attribute is configured with a dimension, a drop-down list of units displays after the test box textarea-Provides a multi-line box for typing text. check box-Provides a check box next to each range value. See Sample JPO for Web Form with Custom Combobox . listbox-Provides a list of range values. Although users can select more than one item in the list by holding the Ctrl key, only one value will be saved unless a custom JPO is implemented. radiobutton-Shows a radio button next to each range value. To see an example of a field configured with radio buttons, see Field for Attribute with Choices in Radio Buttons . combobox-Provides a drop-down list of options and users can only select one value. Use combo boxes for attributes that have defined ranges and for attributes whose ranges are determined with a Range Helper URL. To see an example of a field with a combo box, see Field for Attribute with Choices in Combo Box . dynamictextarea-Allows multiple values to be entered in Edit mode |
| inquiry | Used only for fields defined with Field Type=emxTable. Either this setting or the program setting must be used to define the objects to retrieve. Specifies the inquiry administrative object that should be used to retrieve the business objects to be included in the table. | Name of inquiry administrative object. inquiry=SCSBuyerDesk inquiry=ENCAllParts |
| Label | Use to enter the text that should appear as the label for the field. Make sure Custom Label is checked so the system gets the label you specify. If Custom Label is unchecked, the system uses the expression as the label. Either a string resource ID for the text string or the actual text string that should appear. To internationalize the text, you must use a string resource ID. See Internationalizing Dynamic UI Components . The system first looks for a string resource ID that matches the entered value. If it finds one, it uses the value for the ID. If it does not find one, it displays the entered text. When used with a field type of Dynamic Attributes, the Label becomes the section heading name, or if not provided or set to blank, indicates that no section heading will be displayed. | emxEngineeringCentral.common.Name emxTeam.Common.ProjectName Description |
| Latest Label | Defines the details that appear for the latest element. | type,revision,current type,name |
| Latest Link | Defines the link that opens when users click the latest type icon. Use a path relative to \common directory . | emxTree.jsp?DefaultCategory= ContextIssueListPage download (for file downloads) |
| Latest Threshold | Defines the number of previous days for which changes are highlighted in red. | 10 (default) |
| Latest Width | Defines the width allowed for the latest information. | 250 (default) |
| Link More | If Max Items are set and there are more related items, an additional link opens the entire list of items. | If counter link is specified, it is reused. Otherwise, the default is emxTree.jsp . emxTree.jsp?DefaultCategory=PLCProductReferenceDocumentTreeCategory |
| Max Items | Defines the maximum number of items that appear as type icons, thumbnails, or in a table. This does not impact the counter. | 10 3 |
| Maximum Length | Limits the number of characters that can be entered in a field with Input Type of textbox. If not specified, it uses the HTML default (unlimited). | 30 |
| No Frame | Defines if the panel is inside a frame or not. | False (default) |
| Nowrap | If the setting is true for a field, then the text displayed in that field will not wrap at any time. If the setting is false, then the text wraps at a convenient space between words, or in the middle of a text string (for long strings that exceed the column width). | true false |
| Number Format | This setting only affects how numbers are displayed. Numbers being entered or edited do not show any formatting. When true, values are displayed using the format for the thousands and decimal separators based on the user's browser locale. When false, values are displayed as they are stored in the database, without any formatting. This setting only applies to numeric attributes if the format setting is defined as numeric . | true false (default) |
| OnChange Handler | The name of the JavaScript function called when the value in the field is changed. You can provide a semicolon separated list of JavaScript functions. | <JavaScript function name> |
| OnFocus Handler | The name of the JavaScript function called when a field is selected for edit. You can provide a semicolon separated list of JavaScript functions. | <JavaScript function name> |
| Pattern Types | If the expansion is done on more than one level, defines the types that are returned. Hides intermediate objects. | - Hardware Product Software Product |
| Popup Modal | Specifies whether windows opened from a link are modal or non-modal. Used when the setting Target Location is set to popup. If a web form is opened in a dialog in edit mode, then the dialog must open as a modal dialog. For example, if a table column is designed to pop up the web form in edit mode, then that column must have a setting Popup Modal=true to open the dialog as modal dialog. | true--the popup window is modal false--the popup window is non-modal If the setting is not specified, the window is modal. |
| Popup Size | Defines the size of the popup window. The value can be one of the listed example sizes. The pixel width and height values for these sizes are defined in emxSystem.properties. See Collaboration and Approvals Administration Guide : Properties for Configuring Popup Windows . | Small Medium Large SmallTall MediumTall |
| Printer Friendly | Specifies whether the target page should include the Printer Friendly tool. In the href URL called when the field data is clicked, the system passes a parameter called PrinterFriendly and includes the value specified for this setting. If this setting is not included, the value is assumed to be false. | True False (default) |
| program | Use to specify the name of the JPO program to get the field's data. This program gets the field values when the Field Type setting is program or programHTMLOutput . Can be used for fields defined with Field Type=emxTable to define the objects in the table. Alternatively, an inquiry can be used. Using a JPO to populate field data is recommended only when a select expression cannot be used to obtain the field value. To see an example of a field that uses a program, see Field Values Obtained from a Program . The emxGenericFields setting is mandatory for attachment and attendee fields and for an enhanced images field. | The name of a JPO, such as: SCSBuyerDeskForm TMCPackagesForm ENCPartForm AEFUtilForm emxGenericFields |
| Range Function | Use to specify the name of the method in the JPO specified in the Range Program setting. See the Range Program setting below for more information. | The name of a function in the Range Program JPO, such as: getAssignedRange getClassificationRange getPartUOM |
| Range Program | Use to specify the name of a JPO that contains a method to get the field value ranges (choices). A range program is used only when the Input Type setting is combobox, radiobutton, or check box. If the choices need to be presented on a page in a popup window, for example in a chooser, use the RangeHref parameter instead. To see an example of a field configured with a range program, see Field for Attribute with Choices in Combo Box . Also see JPO for Getting Field Range Values (Choices) . | The name of a JPO, such as: SCSBuyerDeskForm TMCPackagesForm ENCPartForm AEFUtilForm |
| Read Only Check box | For a field defined with the Boolean data type, or as a string with Boolean range values, this setting shows a check box instead of the TRUE or FALSE text. In edit mode, the user can check/clear the check box unless the Editable=false setting is specified for the field. | true false (default) |
| *Registered Suite | The application the field belongs to. The system looks for files related to the field in the registered directory for that application, which is specified in emxSystem.properties. Based on the application name, the system passes the following parameters in the href URL: suiteKey emxSuiteDirectory StringResourceFileId | Set the value without any spaces, for example, EngineeringCentral or Framework. Set the value to the suite name as defined in the key eServiceSuites.DisplayedSuites within emxSystem.properties. If the suite name starts with eServiceSuite then you can skip this prefix and assign the remaining text to the setting. For example, if the suite name in emxSystem.properties is eServiceSuite EngineeringCentral , then the word EngineeringCentral , can be assigned as "Registered Suite". |
| Relationships | Defines the relationship types that a link definition retrieves and displays for related items. The Assigned Meetings setting is mandatory for attendee fields and the Meeting Attachments setting is mandatory for attachment fields. | * (default) Assigned Meetings Meeting attachments Reference Document Part Specification,Reference Document |
| Reload Function | The name of the method in the JPO specified by the Reload Program setting that executes a field reload. You also need to specify a value for the Reload Program setting. | <JPO method name> |
| Reload Program | The name of a JPO to invoke when this code in the form is called: emxFormReloadField("FieldName") You also need to specify a value for the Reload Function setting. | <JPO Name> |
| Remove Range Blank | Used when the input type is set to combobox to ensure that the field contains a value and is not left blank. If set to True, this setting removes the blank from the combo box list. If a default is not specified, the first value in the list is shown by default. | true false (default) |
| Required | Use to indicate the value for this field is required. This setting is applicable for editable fields displayed on the Editable mode form. | true--When completing the form, the user must enter a value for the field. The field label appears in red italic text. If the user does not enter a value and clicks Done, a JavaScript message appears that prompts the user to enter a value. false (default)--When completing the Edit form, the user can leave this field blank. |
| Rows | Used when the input type is set to textarea . This setting limits the height of the textarea on the form and specifies the number of visible text lines. If not specified, it uses the HTML default, which is 5. | 5 10 20 |
| Section Level | Used with the Field Type = Section Header setting to define the level of heading. To see an example, see Field as Section Header and Separator . | Two heading levels are available: 1 (default)--Font for heading label is large and a horizontal line is included above the label. 2--Font for heading label is smaller and the heading is in the same gray rectangle as standard fields. |
| Show All View | If this is set to true, then Show View and Show Printer Friendly settings are overwritten to true. | true false (default) |
| Show Alternate Icon | Set to true if the field value must display with an icon other than the current object type icon. To get the right alternate icon, you must define the Alternate Type expression with the expression to obtain the object type. | true false (default) |
| Show Clear Button | Adds a Clear hyperlink next to the field. This setting only applies to textarea/textbox input type fields. When the user clicks the Clear link, it clears the content of the textbox/textarea input type field. | true false |
| Show Counter | Counts related items. | true (default) |
| Show Edit | Specifies whether a field is visible in edit mode or not. | true false (default) |
| Show Icons | Displays the type icons of the related items for quick access. | true false (default) |
| Show Latest | Shows the recently-modified related items. | true false (default) |
| Show Printer Friendly | Specifies whether a field is visible in printer friendly mode or not. | true false (default) |
| Show Table | Shows the table with details about related items. | true false (default) |
| Show Thumbnails | Displays thumbnails of related items. | true false (default) |
| Show Type Icon | If set to true, the field value displays along with the type icon for the business object being displayed by the current form page. The icon is defined in the emxFramework.smallIcon property in emxSystem.properties. The icon displays to the left of the field data. If no icon is defined for this type, the system looks for a property defined for the parent type and so on up the hierarchy. If no property is defined for any type in the hierarchy, the system uses the default icon specified in the emxFramework.smallIcon.defaultType property. | true false (default) To see an example of a field with a type icon, see Field Values as a Hyperlink and Type Icon . |
| Show View | Specifies whether a field is visible in view mode or not. | true false (default) |
| Slidein Width | When the Target Location setting is set to slidein, this setting defines the width (in pixels) of the slidein window. If you do not specify this setting or you provide a non-numerical value, the default width (350) is used. The minimum value is 350. If you provide a smaller value, 350 is used. The maximum value is 900. If you provide a larger value, 900 is used. You can also set the value to wide to use the configured width as defined by the emxFramework.widerslideIn.size parameter in emxSystem.properties. | wide 500 800 |
| Sort Direction | Used only for fields of type combobox and listbox (attributes with defined ranges). The sort order for the option list. | ascending (default)--Sort a to z or 0 to n. descending--Sort z to a or n to 0. none--The option list is not sorted. |
| Sort Key | Defines the selectable to sort related items before displaying type icons, thumbnails, and table rows. | originated |
| Sort Range Values | Enables or disables the sorting of range values in combobox or listbox controls. When enabled, the list is sorted based on the datatype and using the direction defined by the Sort Direction setting. When disabled, no sorting is done on the list. In a drop-down, range values populated based on an attribute expression will be sorted based on the attribute's datatype. Range values populated using the Range Program and Range Function settings will be sorted alphanumerically. If the Range Program or Range Function returns numeric or date values, the alphanumeric sort will not be appropriate. The program or function should sort the values in the required order, and this setting should be disabled. This setting does not apply to fields or columns configured for attributes associated with a dimension. Dimension ranges are always sorted as alphanumeric in ascending order. This setting cannot be used in custom JSPs that use the editOptionList taglib. For this specific situation, you can use the sortType attribute for that tag. | enable (default disable |
| Sort Type | Defines the type of sorting. The default is retrieved automatically based on the sort key. | string integer real date |
| sortColumnName | Used only for fields defined with Field Type=emxTable. Specifies the column that the table sorts when the page is first loaded. If no column is specified, the rows are listed in the order they are retrieved from the database. | Name of column in the table specified in the table=TABLE_NAME setting. |
| table | Specifies the name of a table administrative object to embed in the form. Used only for fields defined with Field Type=emxTable. You must use the actual table name: symbolic names are not supported. See Field that Embeds a Configurable Table . | table=SCSBuyerDesk table=ENCParts |
| Table Actions | Enables standard capabilities for each item. Use commas to select multiple commands. | promote demote remove delete promote,demote |
| Table Cell Rows | Enables multiple values in a table row. | 1 3 |
| Table Columns | Uses selectables to determine the table details. Use 'Icon' and 'Image' to display the type icon/primary Image. | icon,type,name Image,type,name,description,attribute[Title] |
| Table Link | Defines the link that opens when a user clicks an item in a table. Enter a URL or command. | emxTree.jsp emxTree.jsp?DefaultCategory=AEFLifecycle |
| Table Styles | Defines CSS settings for each table column. Put the settings in the same sequence as Table Columns. | ,,font-weight:bold; color:#009c00,font-weight:bold;, |
| Target Location | Controls where the page specified in the href parameter appears when a user clicks the hyperlinked data. To specify a tab in a PowerView window, use the administrative command name that configures the tab in the PowerView page. If the specified command name is not defined within the current PowerView, the popup value is used. When using slidein as the Target Location and the Popup Modal setting is true, then the content window and the global toolbar are grayed out and disabled until the slidein window is closed. If you do not specify true for the Popup Modal setting, then users can click on buttons and menus to access other functions, but the slidein window remains open. | popup--Page appears in a new window. The window modality can be set with the Popup Modal setting. Popup is the default value and is used if the setting is not included or if the named frame cannot be found. content--The page replaces the content frame. mainFrame--Page appears in the frame that includes the content and menu frames. _top--Page replaces the entire body of the browser window. <command name>--The name of a command configured as the target tab in a PowerView page. slidein--Page appears in a slidein frame (slides in along the right side of the window). This value can be set to any valid frame name that is available to the form body frame. |
| Thumbnail Actions | Defines the following actions for items: promote, demote, remove, or delete. Use a comma to separate multiple commands. | promote demote remove delete |
| Thumbnail Details | Enter selectables to display details right beside thumbnails. | type,name,current |
| Thumbnail Link | Defines the link that opens when a user clicks a thumbnail. Provide a command name or a URL. The path must be relative to \common directory . | emxTree.jsp emxTree.jsp?DefaultCategory=AEFLifecycle |
| Thumbnail Size | Defines the image format for a thumbnail. | mxThumbnail Image mxSmall Image |
| Thumbnail Styles | Defines CSS settings for each thumbnail label. Put the settings in the same sequence as Thumbnail Details. | color:#009c00,font-weight:bold;, |
| Thumbnail Tooltip | Defines the tooltip that appears when a user pauses over a thumbnail. | type,name,revision type,name,owner |
| Tip Page | Specifies whether the target page should include the Tip Page tool (light bulb icon). In the href URL called when the field data is clicked, the system passes a parameter called TipPage and includes the URL specified for this setting. If this setting is not included, the value is assumed to be false and the target page does not display the Tip Page tool. | Name of a custom URL page. |
| Tooltip Separator | Defines a separator in all tooltips. | '-' |
| Type Ahead Validate | When Type Ahead is configured on the field, this setting determines if the application should restrict the user to selecting one of the suggested values (if true), or if the user can enter any data in the field without BPS validating that data (if false). | true false (default) |
| Type Icon Function | Specifies the method in the JPO specified in the Type Icon Program setting that retrieves an icon to show in addition to the Alternate Icon or Type Icon (enabled by Show Alternate Icon or Show Type Icon settings). If both the Show Alternate Icon and the Show Type Icon settings are set to false, any value for this setting is ignored. | Method Name |
| Type Icon Program | Defines the program that contains the function specified using the Type Icon Function setting. | JPO Name |
| TypeAhead | Enables type ahead. If not provided, automatically set to true. Typically used to disable type ahead for a field. | true false |
| TypeAhead Program | Name of the JPO called to retrieve a list of possible values. This JPO is called after the user types the specified number of characters. A JPO is typically used by fields that are normally populated using choosers. |  |
| TypeAhead Function | The name of the JPO method used in conjunction with the TypeAhead Program setting. |  |
| TypeAhead Character Count | Overrides the emxFramework.TypeAhead.RunProgram.CharacterCount system property. This setting may be useful for fields whose values may all contain the same prefix. | 2 (default) 5 |
| TypeAhead Saved Values Limit | Overrides the emxFramework.TypeAhead.SavedValuesLimit system property. | 10 (default) |
| Types | Defines types that are considered when retrieving related items. | Document Hardware Park,Software Part * |
| Update Function | Specifies a method name in the JPO given in the Update Program setting. See Update Program for more information. | The name of a function in the Update Program JPO, such as: setAssignedBuyerDesk setPackage Access setPartClassification |
| Update Program | Specifies a JPO that contains a method to set the field value when the field is displayed on an Edit mode form and when the user clicks Done. This program is used only when the Field Type setting is program or programHTMLOutput. Using an update program is recommended only when the Field Type is not attribute or basic. See JPO for Getting Field Values . | The name of a JPO, such as: SCSBuyerDeskForm TMCPackagesForm ENCPartForm AEFUtilForm |
| Url | Defines the URL that is executed on the inside panel. This setting overwrites the Command setting. Note: The Registered Suite setting resolves the URL path. | ${COMMON_DIR}/emxIndentedTable.jsp?table= APPMeetingAgendaSummary&sortColumnName= Sequence Number&program=emxMeeting: getMeetingAgendas&editLink=true&toolbar= APPAgendaItemToolbar&selection=multiple&freezePane= SequenceNumber,Topic&header= emxComponents.Heading.AgendaItems&customize= false&showClipboard= false&objectCompare=false&HelpMarker= emxhelpcreateagendaitem&HasValidateAll= true&preProcessJPO=emxMeeting: meetingAgendaItemEditPreProcess&postProcessJPO= emxMeeting:meetingAgendaItemEditPostProcess |
| Validate | Specifies the method to execute for validating any cell. The file containing this method must be specified in the emxAPPNAME.properties file. The Validate setting is not supported when the Field Type is programHTMLOutput. | The name of a method, such as: checkUniqueName |
| Validate Type | Specifies how field values should be validated. | Basic or Restricted--The field values are validated against the value of emxFramework.Javascript.BadChars. Name--The field values are validated against the value of emxFramework.Javascript.NameBadChars. |
| Vertical Group Name | All fields with the same Vertical Group Name will display in a column in the specified form location. | <text name of group> |
| View Exclude | Comma-separated list of attributes that will not be included in the web form when opened in View mode when a field on the web form has been defined as Field Type = Dynamic Attributes. | attribute_Cost |
| Window Height | Defines the height of a popup window opened by a link. | System Default 400 |
| Window Width | Defines the width of a popup window opened by a link. | System Default 1200 |
| Zoom | Defines whether zoom is available or not when a user pauses over an image. | true false (default) |
| Zoom Window | The height of div, that appears when a user pauses over an image. | 100 |
| Zoom Window Width | The width of div, that appear when a user pauses over an image. | 200 |
| *Required Setting |  |  |

<a id="formurlparameters"></a>
## URL Parameters Accepted by emxForm.jsp

来源：`FormURLParameters.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This table lists the parameters available to emxForm.jsp. You can add these parameters to the href parameter for the component that calls the form. For example, when you specify the emxForm.jsp to be called from a tree command, you can add these parameters to the href parameter for the menu object.

| Parameter | Description | Accepted Input Values |
| --- | --- | --- |
| appendFields | The name of the web form that defines fields that you want to include in the form being defined. |  |
| appendURL | Defines additional parameters to append to the URL string and is specified in this format: <KEY>\|SuiteKey Where the KEY is a value in the properties file of the specified SuiteKey . See Additional URL Parameters for Forms . | Effectivity\|EngineeringCentral |
| displayCDMFileSummary | Determines if the file summary list should display on the same page as the Properties for a CDM object. | true false (default) |
| editLink | Use to have the system include a default Edit toolbar item in the View Form header. | true false (default) emxForm.jsp?form=ENCPart&editLink=true |
| *form | Specifies the web form administrative object used for presenting the form page. You must use the actual form name: symbolic names are not supported. | Name of web form administrative object. emxForm.jsp?form=ENCPart |
| formFieldsOnly | By default, all fields in the defined form, dynamic attributes, and custom attributes show on form pages. If you pass formFieldsOnly=true in the href, then only the fields defined in the form object, not any dynamic or custom attributes, show in the form page. | true false (default) |
| HelpMarker | Specifies the name of the help marker to call for context-sensitive help. The Help link always displays for form pages. | The naming convention for help markers is the page title, as displayed at the top of the visual page, prefixed with "emxhelp". The marker is all lowercase with no spaces. |
| hideCancel | Used with the advanced structure compare tool. If true, the page does not have a Cancel button. | true false (default) |
| hideLaunchButton | Used with the advanced structure compare tool, which uses a PowerView window. Channels and tabs in PowerView windows, that often show unrelated data, normally include a launch button that opens that tab in a maximized window. For the advanced structure compare, you may not want your users to open more windows. If true, the launch command is hidden on the toolbar when the page shows in a PowerView (portalMode=true). | true false (default) |
| jpoAppServerParamList | Allows session data to be passed to a JPO and uses the format: scope:attributeName where scope can be one of these values: application session request and the attribute must be a valid attribute used within the specified scope. The parameter can pass a comma-separated list of scope:attributeName values. The attribute values must be serializable. | application:<attributeName>,session:<attributeName>,request:<attributeName> |
| launched | Specifies the behavior of the popup page. You can change the behavior of the popup page from the normal mode pages using this parameter. When the user clicks the Launch button from a channel tab, launched=true is passed to the new popup page which indicates that the popup is a result of clicking the Launch button. In normal mode, pages will not have the launched parameter and will default to launched=false . | true false (default) |
| mode | Specifies whether the form page is in view or edit mode. | view (default)--form is read only edit--form is editable emxForm.jsp?form=ENCPart&mode=edit |
| objectId | Specifies the business object that the properties need to be displayed for. | Valid business object ID. emxForm.jsp?objectId=3243.32424.232 |
| portalMode | Every page configured inside the PowerView includes the parameter portalMode=true , so that the page can differentiate between normal display and portal display. When the Launch button is clicked, it launches the currently displayed channel tab into a maximized popup window, passing the parameters launched=true and portalMode=false to the new window. | true false (default) |
| postProcessJPO | Used to specify the post processing JPO program name and the method name to invoke after form processing and database update. This parameter is applicable only for edit mode. | <JPO Name>:<Method Name> For example: emxPart:processECO |
| postProcessURL | Specifies the name of the JSP executed during edit form post processing. The JSP is executed only after the form processing and database update complete. This parameter is applicable only for edit mode. | ${SUITE_DIR}/emxCustomPostProcess.jsp <AppDirectory>/emxCustomPostProcess.jsp For example: engineeringcentral/emxCustomPostProcess.jsp |
| preProcessJavaScript | Defines a JavaScript function and is called after the form is loaded in edit mode to execute custom processing. Can be a single function, or a semi-colon separated list of functions. | JavaScript function name |
| PrinterFriendly | Specifies whether the form page should include the Printer Friendly tool. | true (default) false |
| RegisteredDirectory | Specifies the directory where the help files are located. | Directory name within ematrix. |
| relId | Specifies the relationship used to get the relationship attribute values for relationship fields. | Valid relationship ID. emxForm.jsp?relId=3243.32424.232 |
| renderPDF | Specifies whether the Render PDF icon displays in the form toolbar in View mode. If the parameter is not passed in, the default is false and the icon is not shown. | true--The Render PDF icon displays in the form toolbar. false--The Render PDF icon is not displayed in the form toolbar. |
| resetForm | Used with the advanced structure compare tool. If true, the page, includes a Reset link. | true false (default) |
| showPageURLIcon | When true, shows in the toolbar. This tool lets users copy the URL to the specific app page. When false, does not show in the toolbar. The default value for this parameter is defined by the emxFramework.Toolbar.ShowPageURLIcon property in emxSystem.properties . | true false |
| showTabHeader | Applies only in Portal mode (when the page is displayed within a PowerView), enables or disables the page header. If false, the header text is not displayed in the PowerView tab. If true, the header text shows in the tab. | true false (default) |
| submitAction | Determines the action to take after completing the form (user clicks Done or other action button other than Cancel, in addition to closing the form window). | refreshCaller: Reload the calling page. If called from a table or structure browser, then reloads the table or structure browser. doNothing: When called from a table or structure browser, does not refresh (including sorting) the calling page. treeContent: Load the newly-created object's tree in the main content frame. treePopup: Load the newly-created object's tree in a new pop-up window. |
| submitLabel | Used with the advanced structure compare tool. By default, a form includes a submit button with the default label of Done . This parameter changes the label of the button to Apply . | Any static text of string resource id. Apply SubmitLabel=emxFramkework.advstructurecompare.Apply |
| submitMultipleTimes | When used as part of the advanced structure compare tool, determines if the form is closed when the user clicks Apply , or if it remains open. | true false (default) |
| targetLocation | When a URL is defined to open in the slide-in frame (the Target Location setting for the component is set to slidein ), BPS appends this URL parameter with this value. This parameter supports a custom page where the html for a popup window needs to be different than the html for a slide-in window. | slidein |
| TipPage | Specifies the Web page (html or jsp) to launch when the user clicks one of the page toolbar buttons in the form header frame. | Name of a custom URL page. emxForm.jsp?form=ENCPart&TipPage=../myapplication/showMyTipPage.jsp? |
| toolbar | Specifies the menu administrative object used in the header frame that represents an action or filter toolbar. The value can be a comma-separated list of menus/toolbars. | PMCWBSTaskToolbar ECEBOMToolbar,ECEBOMFilter |
| *Required |  |  |

<a id="getting-cell-values-by-object-id"></a>
## Getting Cell Values by Object ID

来源：`Getting Cell Values by Object ID.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API returns `completeCellInfo` for the row ID and column name passed in.

See Complete Cell Information.

```
emxEditableTable.getCellValueByObjectRelId
```

This API requires these input arguments:

- `relId`. The relationship ID of the needed cell.

- `objectId`. The object ID of the needed object.

- `colName`. The column name of the needed cell.

Example:

```
Var relId = [];
```

```
Var objectId = [];
```

```
Var colName = FindNumber
```

```
emxEditableTable.getCellValueByObjectRelId(relId,objectId 
,colName)
```

<a id="getting-cell-values-by-row-id"></a>
## Getting Cell Values by Row ID

来源：`Getting Cell Values by Row ID.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API returns `completeCellInfo` for the row ID and column name passed in.

See Complete Cell Information.

```
emxEditableTable.getCellValueByRowId
```

This API requires these input arguments:

- `rowId`. The row ID of the needed cell.

- `colName`. The column name of the needed cell.

Example:

```
Var rowId = [0,1];
```

```
Var colName = FindNumber
```

```
emxEditableTable.getCellValueByRowId(rowId ,colName)
```

<a id="getting-child-column-values"></a>
## Getting Child Column Values

来源：`Getting Child Column Values.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API returns an array of column values of child cells based on the parent row ID and the specified column name.

```
emxEditableTable.getChildrenColumnValues
```

This API requires these input arguments:

- `rowId`. The row ID of the parent row.

- `colName`. The column name of the needed cell.

- `level`. The number of levels beneath that parent for which values should be returned.

Example:

```
Var rowId = [0,1];
```

```
Var colName = Units
```

```
Var level = 3;
```

```
emxEditableTable.getChildrenColumnValues(rowId,colName,level)
```

<a id="getting-child-ids"></a>
## Getting Child IDs

来源：`Getting Child IDs.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API returns an array of rowIDs of the first-level children for the current cell.

```
emxEditableTable.getChildrenRowIds
```

This API requires this input argument:

- `rowId`. The row ID of the needed cell.

Example:

```
Var rowId = [0,1];
```

```
emxEditableTable.getChildrenRowIds(rowId)
```

<a id="getting-current-cell-details"></a>
## Getting Current Cell Details

来源：`Getting Current Cell Details.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API returns `completeCellInfo` for the cell currently in focus.

See Complete Cell Information.

```
emxEditableTable.getCurrentCell
```

This API does not have any input arguments.

Example:

```
emxEditableTable.getCurrentCell()
```

<a id="getting-parent-column-values"></a>
## Getting Parent Column Values

来源：`Getting Parent Column Values.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API returns the value of the parent object's cell based on the specified row ID, column name and level.

```
emxEditableTable.getParentColumnValue
```

This API requires these input arguments:

- `rowId`. The row ID of the child cell.

- `colName`. The column name of the needed cell.

- `level`. The number of levels above that child for which values should be returned.

Example:

```
Var rowId = [0,3];
```

```
Var colName = Units
```

```
Var level = 1;
```

```
emxEditableTable.getParentColumnValue (rowId,colName,level)
```

<a id="getting-parent-id"></a>
## Getting Parent ID

来源：`Getting Parent ID.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API returns the row ID of the parent row for the current cell.

```
emxEditableTable.getParentRowId
```

This API requires this input argument:

- `rowId`. The row ID of the needed cell.

Example:

```
Var rowId = [0,1];
```

```
emxEditableTable.getParentRowId(rowId)
```

<a id="load-mark-up"></a>
## Load Mark Up

来源：`Load Mark Up.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API adds a new row to a structure browser. Applications must pass the object ID of the row to which the new row will be connected.

Within an XML <mxRoot> element, the markup value indicates the action to take, and can be any of these values:

- `add`. Adds a row for the object.

- `cut`. Removes the row for the object from the structure browser.

- . Updates column values.

- . Resequences rows.

This example adds the specified object as a child to the first object:

```
<mxRoot>
```

```
    <object objectId="19940.22650.58300.63486">
```

```
       <object objectId="19940.22650.28544.49549" relId="" 

       relType="relationship_EBOM" markup="add">   </object>
```

```
    </object>
```

```
</mxRoot>
```

This example removes the specified object:

```
<mxRoot>
```

```
    <object objectId="19940.22650.58300.63486" rowid="0">
```

```
       <object objectId="19940.22650.28544.49549" 
```

```
       relId="19940.22650.44544.64603"
```

```
       parentId="19940.22650.58300.63486"
```

```
       relType="relationship_EBOM" markup="cut"/>
```

```
    </object>
```

```
</mxRoot>
```

<a id="mql-parser-keywords"></a>
## Appendix: MQL Parser Keywords

来源：`MQL_Parser_Keywords.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This page lists keywords recognized by the MQL parser as hardcoded tokens.

!=

!access

!active

!add

!allowreuse

!allowusername

!application

!archive

!association

!attribute

!basic

!business

!captured

!changelattice

!changename

!changeowner

!changepolicy

!changetype

!changevault

!checkaccess

!checkin

!checkout

!client

!connect

!create

!creator

!default

!delete

!deletefiles

!demote

!derived

!disable

!disconnect

!downloadable

!enable

!enforce

!execute

!expandtype

!expires

!expression

!external

!file

!foreign

!form

!format

!freeze

!fromconnect

!fromdisconnect

!fromrelationship

!full

!global

!grant

!group

!hashed

!hidden

!history

!icon

!immediate

!inactive

!inquiry

!keepvid

!location

!lock

!lockout

!mail

!majorrevise

!match

!maxlength

!maxsize

!menu

!minorrevise

!minsize

!mixedalphanumeric

!modify

!modifyform

!monitor

!multiline

!multirange

!multivalue

!needsbusinessobject

!neverexpires

!override

!overwrite

!passwordexpired

!path

!person

!pipe

!policy

!pooled

!portal

!preserve

!preventduplicates

!privilegedbusinessadmin

!process

!program

!promote

!propagateconnection

!propagatemodify

!propagaterename

!property

!published

!rangevalue

!read

!relationship

!relationshiprule

!report

!required

!reserve

!resetonclone

!resetonrevision

!revise

!revoke

!role

!rule

!schedule

!server

!set

!show

!site

!smatch

!state

!status

!store

!structure

!substitute

!system

!table

!thaw

!toconnect

!todisconnect

!torelationship

!trusted

!type

!unique

!unlock

!usesexternalinterface

!vault

!viewform

!warnduplicates

!web

(

)

1

<

=

<=

>

>=

abort

abortprocess

abstract

acceptactivity

access

accesslist

action

activate

activateactivity

activateautoactivity

active

activefilters

actual

add

addinterface

addownership

address

admin

after

age

alias

aliasname

all

allowreuse

allowusername

allstate

alpha

alt

altowner1

altowner2

and

append

application

appliesto

approve

archive

argument

as

asanorg

asaproject

asarole

assign

assignee

assignment

association

att

attach

attribute

autoheight

automated

automatic

autostart

autowidth

average

background

basic

bcc

before

bestsofar

between

boolean

both

branch

branchid

branchtag

browse

bucketsync

businessobject

businessobjectlist

buslist

button

by

calculated

captured

capturedfile

cardinality

casesensitive

casualhour

cc

certificate

cestamp

chain

chainattribute

changelattice

changename

changeowner

changepolicy

changetype

changevault

channel

check

checkaccess

checkbox

checkin

checkintag

checkout

checkshowaccess

checksumoff

checksumon

checksumwarnonlyoff

checksumwarnonlyon

child

cipher

class

clear

client

clone

cluster

code

color

column

combobox

command

comment

commit

community

compact

compare

compile

compilerflags

complete

completeactivity

completeautoactivity

config

connect

connection

consecutive

constraint

cont

containing

content

context

continue

convert

copy

copyfromstore

copyonwrite

correct

corruptsummary

count

create

creator

csharp

cue

cumulative

current

custom

customevent

daily

data

dataobject

date

dbservertimezonefromdb

debug

decimal

default

defaultformat

deferred

definition

del

delegation

delete

deletefiles

deletetrigger

delimiter

demote

derivative

derived

description

designsync

detach

dimension

directory

directorytextbox

disable

disconnect

diskusage

dispatchtask

displayrule

download

downloadable

drawborder

dump

duration

dynamic

edit

ekl

element

email

emptyname

enable

encrypt

end

ending

enforce

environment

epilogue

error

escalation

escape

evaluate

event

eventmonitor

events

every

exception

exclude

exclusive

execute

exists

expand

expandtype

expires

export

expression

external

extract

fakeraw

false

family

familyid

fax

fcs

fcsbegin

fcsdbchecksum

fcsend

fcsextensions

fcssettings

field

file

filename

filetextbox

filter

finish

finishprocess

firstpage

fix

float

font

footer

for

force

foreign

form

format

frame

freeze

freezethaw

from

fromconnect

fromdisconnect

fromrel

fromtype

fromuser

full

fullname

gb

geometry

get

global

globaluniquetnr

grant

grantactivity

grantee

grantor

graphic

group

groupby

groupbyseparator

hashed

header

height

help

hidden

highlight

history

historyoff

historyon

host

href

icon

iconmail

ignore

image

immediate

immediateattribute

immediatederivative

import

in

inactive

inches

includemaxval

includemnval

inclusive

incomplete

incremental

index

indexport

indexspace

ingested

inivariable

input

inquiry

insert

instruction

integer

interactive

interface

interval

into

inventory

isassigned

isrelevant

java

justify

kb

keep

keepformat

keepvid

key

kill

label

language

last

lastpage

lattice

leaf

less

level

license

limit

linestyle

link

list

listbox

listnames

load

location

lock

locker

lockout

log

logicalid

login

lowerbound

lxfileunique

mail

major

majorid

majororder

majorrevise

majorrevision

majorsequence

many

map

margins

master

match

maturity

maxexclude

maxgroupings

maxinclude

maxlength

maxsize

maxval

mb

meaning

mem

member

members

memory

menu

message

metadata

method

mib

mime

minexclude

mininclude

minor

minororder

minorrevise

minorrevision

minorsequence

minsize

minval

mixedalphanumeric

mkbranch

mod

mode

modified

modify

modifyattribute

modifyconnection

modifydescription

modifyform

modifyfrom

modifyto

modifytype

monitor

monthly

move

movefrom

moveto

mql

msgtype

multiline

multipleinterfaces

multiplier

multivalue

n

name

needsbusinessobject

neverexpires

newpassword

newrow

no

node

nogroupbyheader

none

normal

notaccess

notactive

notallowreuse

notallowusername

notapplication

notarchive

notassociation

notattribute

notbasic

notbusiness

notcaptured

notchangelattice

notchangename

notchangeowner

notchangepolicy

notchangetype

notchangevault

notcheckaccess

notcheckin

notcheckout

notclient

notconnect

notcreate

notcreator

notdefault

notdelete

notdeletefiles

notdemote

notderived

notdisable

notdisconnect

notdownloadable

notenable

notenforce

notes

notexecute

notexpandtype

notexpires

notexpresion

notexternal

notfile

notforeign

notform

notformat

notfreeze

notfromconnect

notfromdisconnect

notfromrelationship

notfull

notglobal

notgrant

notgroup

nothashed

nothidden

nothistory

notice

notify

notimmediate

notinactive

notinquiry

notkeepvid

notlocation

notlock

notlockout

notmail

notmajorrevise

notmaxsize

notmenu

notminorrevise

notminsize

notmixedalphanumeric

notmodify

notmodifyform

notmonitor

notmultiline

notmultivalue

notneverexpires

notoverride

notoverwrite

notpasswordexpired

notpath

notperson

notpipe

notpolicy

notpooled

notportal

notpreserve

notpreventduplicates

notprivilegedbusinessadmin

notprocess

notprogram

notpromote

notpropagateconnection

notpropagatemodify

notpropagaterename

notproperty

notpublished

notrangevalue

notread

notrelationship

notrelationshiprule

notreport

notrequired

notresetonclone

notresetonrevision

notrevise

notrevoke

notrole

notrule

notschedule

notserver

notset

notshow

notsite

notstatus

notstore

notstructure

notsubstitute

notsystem

nottable

notthaw

nottoconnect

nottodisconnect

nottorelationship

nottrusted

nottype

notunique

notunlock

notusesexternalinterface

notvault

notviewform

notwarnduplicates

notweb

nowait

numeric

object

objects

observer

obsolete

off

offset

oid

oids

on

one

onlineinstance

onto

openedit

openmaxval

openminval

openview

or

oracleset

order

orderby

organization

originated

other

output

over

override

overrideactivity

overrideautoactivity

overwrite

owner

ownership

package

pageobject

parallelroute

params

parent

part

password

passwordexpired

path

pathtype

pattern

pause

peer

perline

permission

perpage

persistentforeignids

person

personal

pervalue

phone

physicalid

picas

pipe

place

points

policy

poll

pooled

pop

port

portal

prefix

preserve

preventduplicates

pri

primary

print

priority

private

privilegedbusinessadmin

process

product

program

project

prologue

promote

prompt

propagateconnection

propagatemodify

propagaterename

property

prot

protected

protocol

proxystamp

public

publish

published

pull

pure

purge

push

qstore

qtype

qualifier

query

querytrigger

queue

quit

quote

radiobutton

range

rangevalue

rate

raw

read

real

reassign

reassignactivity

reassignautoactivity

reassignprocess

receive

rechecksum

recordseparator

recurse

reference

refresh

register

rehash

reject

relationship

relationshiprule

rem

remaining

remove

removefile

removeinterface

removeownership

rename

renamefile

renumber

replace

replicate

replicationtrigger

replyq

report

reporttype

required

reserve

resetonclone

resetonrevision

resetperpage

resizeheight

resizewidth

resource

response

result

resume

resumeactivity

resumeautoactivity

resumeprocess

reversefilters

revise

revision

revoke

role

route

rule

rulefoot

rulehead

ruleleft

ruleright

run

save

savepoint

scale

schedule

schema

scroll

search

searchcriteria

searchindex

secondary

security

select

selectable

selected

selector

send

sendsyn

separator

sequence

server

service

sessions

set

setting

shell

show

signature

signer

site

size

skip

smatch

snmp

sort

sortattributes

sorttype

source

sourcefile

sparse

start

starting

startprocess

state

stateproperty

statistics

status

stop

stopprocess

store

string

structure

subject

subprocess

subscribe

subset

substitute

suffix

sum

summarize

summary

supportpage

suspend

suspendactivity

suspendautoactivity

suspendprocess

sync

synccksumchk

synchronized

syncmaxfilenum

syncmaxsize

system

table

tablespace

targetfile

taskmail

tcl

temp

template

temporary

tenant

tenantadmin

terse

test

text

textbox

thaw

thread

tidy

time

timeout

timestamp

timezone

tip

title

tnr

to

toconnect

todisconnect

toolset

topic

torel

totype

touser

trace

tracked

transaction

transition

trigger

triggeroff

true

truncatehistory

trusted

type

unique

uniquekey

unit

unitdescription

units

unlock

unreserve

unset

unsign

update

updatestamp

updatestate

upgrade

upload

upperbound

url

use

user

usesallpackage

usescustompackage

usesexternalinterface

usespackage

validate

value

vault

vcconnection

vcfile

vcfolder

vcjpo

vcmodule

verbose

version

versioned

versionid

versiontag

view

viewform

visible

wait

warnduplicates

warning

web

webpart

webreport

weekly

where

widget

width

with

workspace

write

xcoord

xml

ycoord

zip

zipsync

<a id="refreshing-rows"></a>
## Refreshing Rows

来源：`Refreshing Rows.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

These APIs let you refresh rows in the structure browser.

- Refresh Rows by ID

- Refresh Selected Rows

#### Refresh Rows by ID

Use this API to refresh a specific row or an array of rows. You can use this API in the details and graphic views of the structure browser.

```
emxEditableTable.refreshRowByRowId
```

This API requires one of these input arguments:

- `rowId`. The row ID of the needed row

- `arrRowIds`. An array of row IDs.

Example of using an array:

```
var arrRowIds = [];
 arrRowIds.push( "0,0");
 arrRowIds.push( "0,0,0");
 arrRowIds.push( "0,1");

 emxEditableTable.refreshRowByRowId(arrRowIds);
```

Example of using a specific row ID:

```
 rowId ="0,0";
 emxEditableTable.refreshRowByRowId(rowId);
```

#### Refresh Selected Rows

Use this API to refresh selected rows in the structure browser:

```
emxEditableTable.refreshSelectedRows()
```

This API can be used with mass update and the mass promote/demote functions. This API does not require any input arguments.

<a id="refreshing-table-columns"></a>
## Refreshing Table Columns

来源：`Refreshing Table Columns.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

These APIs let you refresh all columns in the structure browser.

Use the first one to refresh with sorting; use the second one to refresh without sorting:

```
emxEditableTable.refreshStructure
```

```
emxEditableTable.refreshStructureWithOutSort
```

When using the `refreshStructure` API, the structure browser is sorted based on the current values for sort parameters (`sortColumnName` and `sortDirection` URL parameters).

This API does not require any input arguments.

Example:

```
emxEditableTable.refreshStructure()
```

<a id="refreshing-the-header-subtitle"></a>
## Refreshing the Header Subtitle

来源：`Refreshing the Header Subtitle.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API updates the subtitle in the header without refreshing the structure browser page.

```
emxEditableTable.setSubHeader
```

This API requires this input argument:

- `newSubHeader`. The new text to use as the subtitle in the header. Use a text string; macros are not supported.

If the input parameter is empty (""), then any existing subtitle will be cleared. Long subtitles will be wrapped. If the subtitle includes special characters, encode it using the `encodeForJavaScript` API prior to calling this API.

Example:

```
emxEditableTable.setSubHeader("Filters for Project XYZ Applied")
```

<a id="reloading-cell-values"></a>
## Reloading Cell Values

来源：`Reloading Cell Values.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

If a column has the Reload Program and Reload Function settings defined, this API calls that method to reload the cell value. A single call can be used to update the values of multiple columns using a single AJAX call.

This API is often invoked from the JavaScript functions defined by the `onFocusHandler` or `onChangeHandler` settings for the structure browser column. Each column to be udpated has a Reload Program/Function defined.

This API only reloads data for the cell currently being modified.

```
emxEditableTable.reloadCell
```

This API requires this input argument:

- `colName1, colName2, colName3, ...`. A comma-separated list of the column name(s) of the cells to be updated.

Example:

```
reloadCell("state 1, state 2, state 3, state 4")
```

This example invokes the `reloadCell("state 1, state 2, state 3, state 4")` from `On Change Handler` of column "country". This function loads new range values for each of the listed columns.

<a id="removing-selected-rows"></a>
## Removing Selected Rows

来源：`Removing Selected Rows.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API removes rows from the view (the data is not deleted).

```
emxEditableTable.removeRowsSelected
```

This API does not require any input arguments.

Example:

```
emxEditableTable.removeRowsSelected()
```

<a id="selectables"></a>
## About Selectables

来源：`Selectables.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

The following topics are discussed:

- Select Expressions

- latest and islast Selectables

- Scenario for Selectables

#### Select Expressions

You can use select expressions in print and expand MQL statements, as well as queries.

The purpose of select expressions is to obtain or use information related to a business object. The system attempts to produce output for each select clause input, even if the object does not have a value for it. If this is the case, an empty field is output.

- In a query or a visual, the select expression value is used to qualify the search criteria (in a where clause) by comparing it with another (given) value.

- In a table, report, or form, the expression defines the information to present in a column or field.

- In a `print` or `expand` statement, the expression defines the information to output about the object(s).

You can obtain information which includes not only attribute values and other business object data, but also administrative object information, such as the governing policy, vault, and so on. The key property of a select expression is that it can access information related to an object.

In all cases, the expression is processed from the context of a starting object.

- In a query, the starting point is the business objects that meet other selection criteria (vault, type, and so on).

- In a table, the starting point is the business object in each row.

The phrase starting point is used because the select mechanism actually uses the same concept of navigation from one object to another that makes the rest of the system so flexible. Most information is actually represented internally by a small object and not by a text string or numeric value as it appears to the user.

These internal objects are all linked in the same way business objects are connected by relationships. The links can be navigated from one object to another. A period (.) indicates a link in the select expression. The entire list of selectable fields can be obtained via MQL.

If you type this MQL command:

```
print businessobject selectable;
```

The result is similar to:

```
name

description

revision

originated

modified

lattice.*

owner.*

grantor.*

grantee.*

granteeaccess

granteesignature

policy.*

type.*

attribute[].*

default.*

format[].*

current.*

state[].*

revisions[].*

previous.*

next.*

first.*

last.*

history

relationship[].*

to[].*

from[].*

exists

islockingenforced

vault.*

locked

locker.*

reserved

reservedby

reservedstart

reservedcomment

id

method

search*
```

The `.*` notation indicates the item is an object and you can navigate to other information from that object. Items without this notation are simple data types (`locked`, `id`, `method`, and so on) and cannot be used for further navigation.

The square bracket notation (for example, `attribute[].*`) indicates that there can be many linked attribute objects and that a specific name can be entered if it is known. By contrast, object notations without the square brackets (for example, `owner.*`), means there is only one Owner object linked to the starting object.

If you expand the information available from the Owner object by typing:

```
print businessobject selectable owner;
```

these options are listed:

```
owner.name

owner.description

owner.property[].*

owner.hidden

owner.id

owner.mask.*

owner.admin.*

owner.email

owner.isaperson

owner.isagroup

owner.isarole
```

`owner.name` indicates that you can find the name of the owner by navigating to the Owner object and then to the name data (string, in this case) in that object. Each successive expansion may reveal other objects that can yield more information.

A business object can only have a single governing policy, so the square bracket notation does not appear. If you expand the information available from the `policy.*` option by typing:

```
print businessobject selectable policy;
```

These options are listed:

```
policy.name

policy.description

policy.property[].*

policy.hidden

policy.id

policy.type[].*

policy.format[].*

policy.defaultformat.*

policy.state[].*

policy.revision

policy.store.*

policy.islockingenforced
```

If you expand the information further for the `policy.state` option by typing:

```
print businessobject selectable policy.state;
```

These options are listed:

```
policy.state.name

policy.state.publicaccess.*

policy.state.owneraccess.*

policy.state.notify

policy.state.route

policy.state.action

policy.state.check

policy.state.access[].*

policy.state.signature[].*

policy.state.revisionable

policy.state.versionable

policy.state.autopromote

policy.state.checkouthistory
```

You use the same process for expanding selectable fields as the process of expanding connected business objects in the Navigator browser. It involves following links from one item to another.

Programs should be explicit about the selectables desired and their output order. Dassault Systemes does not guarantee output will remain in the same order.

#### latest and islast Selectables

The `latest` and `islast` selectables are similar but could produce different results. The values for these selectables are not stored, but calculated at the time of the query.

`latest` operates on minor revisions only. A business object is the "latest" only if it is the last revision in the minor-revision family in the specified state.

`islast` is true for a business object if the revision is the last of its minor family. The query retrieves all objects with the same majorid value and sorts them based on minor order, with the last revision being true for `islast`.

#### Scenario for Selectables

This example shows a business object, Assembly 90000 A, with the following properties:

```
Assembly 900000 A

policy = Production

current = Released

description = Bed Frame

cost = 1000

weight = 100
```

Example

Suppose you want to use the information about the policy. The table below shows how this information is obtained with the MQL print command and also how it could be used in finds, tables, and so on.

|  | MQL print Command | Find or Visuals Where Clause | Table, Report, Form Field |
| --- | --- | --- | --- |
| Select Expression | print bus Assembly 90000 A select policy; | policy = production | policy |
| Result | MQL returns: policy = Production | Assembly 90000 A is found or the visuals are applied to it. | If Assembly 90000 A is in the table/report/form, Production is output in that column/field. |

In the where clause, a space must be included before and after all operators.

Example:

This example shows how to use the attribute "Cost" in Assembly 90000 A. The table below shows how this information is obtained with the MQL print command and also how it could be used in finds, tables, and so on.

|  | MQL print Command | Find or Visuals Where Clause | Table, Report, Form Field |
| --- | --- | --- | --- |
| Select Expression | print bus Assembly 90000 A select attribute[cost]; | type == Assembly && attribute[cost] == 1000 (or perhaps, attribute[cost] < 2500, and so on.) | attribute[cost] |
| Result | MQL returns: attribute[cost] = 1000 | Assembly 90000 A is found or the visuals are applied. | If Assembly 90000 A is in the table, report, or form, 1000 is output in that column/field. |

<a id="selecting-rows"></a>
## Selecting Rows

来源：`Selecting Rows.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API selects rows from the rows passed to the API. If a row Id does not exist, the API skips that row and does not throw an exception. This API is defined in emxUIFreezePane.js.

```
emxEditableTable.select
```

This API requires this input argument:

- arrRowIds. An array of row IDs.

Example:

```
var arrRowIds = new Array ();
 arrRowIds.push( "0,0" );
 arrRowIds.push( "0,0,0" );
 arrRowIds.push( "0,1" ); emxEditableTable.select(arrRowIds);
```

Any rowIds that do not have a match or that are already selected are ignored. If there are no matching rowIds, a message is displayed to the user.

The `select` URL parameter determines if the structure browser shows check boxes (`select=multiple`), radio buttons (`select=single`), or no select option (`select=none`). Depending on the value, the structure browser in view mode uses these visual cues to indicate which rows are selected:

- Check boxes: check boxes are selected and rows show with a blue background.

- Radio buttons: only the first returned row is selected and shown with a blue background.

- None: selected rows are indicated by a blue backround.

<a id="setting-cell-value-by-row-id"></a>
## Setting Cell Value by Row ID

来源：`Setting Cell Value by Row ID.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API sets the actual and display values of the cell identified by the row ID and column name.

```
emxEditableTable.setCellValueByRowId
```

This API requires these input arguments:

- `rowId`. The row ID of the needed cell.

- `colName`. The column name of the needed cell.

- `cellValue`. The actual value of the cell to be stored in the database.

- `cellDisplayValue`. The value to be shown in the UI for the cell.

- `isRefreshView`. A boolean value that when true (the default), refreshes the structure browser page.

Example:

```
Var rowId = [0,1];
```

```
Var colName = Units
```

```
Var cellValue = U1,U2,U3;
```

```
Var cellDisplayValue = "U1 - U3";
```

```
Var isRefreshView = true;
```

```
emxEditableTable.setCellValueByRowId(rowId,colName,cellValue, 
cellDisplayValue,isRefreshView)
```

This example sets the value for the Units column of row [0,1].

<a id="setting-cell-values-by-object-relationship-id"></a>
## Setting Cell Values By Object / Relationship ID

来源：`Setting Cell Values By Object _ Relationship ID.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API sets the actual and display values of the cell identified by the row ID and column name.

```
emxEditableTable.setCellValueByObjectRelId
```

This API requires these input arguments:

- `relId`. The relationship ID of the needed cell.

- `objectId`. The object ID of the needed cell.

- `colName`. The column name of the needed cell.

- `cellValue`. The actual value of the cell to be stored in the database.

- `cellDisplayValue`. The value to be shown in the UI for the cell.

- `isRefreshView`. A boolean value that when true (the default), refreshes the structure browser page.

Example:

```
Var relId = [];
```

```
Var objectId = [];
```

```
Var colName = Units
```

```
Var cellValue = U1,U2,U3;
```

```
Var cellDisplayValue = "U1 - U3";
```

```
Var isRefreshView = true;
```

```
emxEditableTable.setCellValueByRowId(relId,objectId,colName,cel
lValue,cellDisplayValue,isRefreshView)
```

<a id="setting-the-html-value-of-a-cell-by-row-id"></a>
## Setting the HTML Value of a Cell by Row ID

来源：`Setting the HTML Value of a Cell by Row ID.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This API replaces the HTML content of a cell with new HTML code. This API should only be used on cells in a column where the `Column Type` setting is set to `programHTMLOutput`.

For example, a column can be configured with an `On Change Handler` function so that when its value is updated, it can automatically update the HTML code in another cell.

```
emxEditableTable.setCellHTMLValueByRowId
```

This API requires these input arguments:

- `rowId`. The row ID of the needed cell.

- `colName`. The column name of the needed cell.

- `strHTML`. The HTML string to replace the current value. The string must be HTML5 compliant and enclosed within the <c> tag.

- `isRefreshView`. A boolean value that when true (the default), refreshes the structure browser page.

As an example, this function is invoked by the On Change Handler and uses this API to insert an image link in a cell:

```
function onDescriptionChanged(){
     var checkboxes = getCheckedCheckboxes();
     var cBoxArray = new Array(); 
     var strSelectedLevelIds = "";
     var id= "";
     for(var e in checkboxes){
          cBoxArray[cBoxArray.length] = e;
     }
     if (cBoxArray.length==0) {
          alert ("select a checkbox in Name Column before using this command" );
     }

     for (var itr = 0; itr < cBoxArray.length; itr++) {
          var aIds = cBoxArray[itr].split("|");

          id = aIds[3];

          var objectIds = "";
          objectIds=aIds[1];

          var relIds = "";
          relIds=aIds[0];

          var directions = "";
          var RowId= "";
          var RowId1= "";
          var RowId2= "";
          var strHTML = '<c><span><img height="16" border="0" align="middle"
               src="images/iconSmallDesignSpecification.gif" /></span></c>';

          emxEditableTable.setCellHTMLValueByRowId(id,'progHTMLColumn',strHTML,true);
     }
     return true;
     }
```

<a id="settings"></a>
## Settings

来源：`Settings.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This table lists and describes the settings for administrative objects that represent UI components. Note that the name and value for each setting are case sensitive.

| Setting | Description | Accepted Values/Examples |
| --- | --- | --- |
| UI Component |  |  |
| Access Expression | Controls access to the component based on a valid MQL expression. The expression gets evaluated at runtime. If the expression evaluates to True and no other access control prevents access, the component is shown, otherwise it is hidden. Depending on the type of expression defined, the program may or may not need a valid objectID. Administrators need to make sure that an objectId is available before configuring this setting for configurable toolbar menus and commands. When the setting is used for commands connected to the an app category menu, or the actions menu on the top bar, the expression is evaluated on the person business object. It does not depend on any objectId to be passed in as a URL parameter. In cases where no objectId is passed, such as a page originating from an app category menu, the Access Mask needs the URL to have the objectId available to validate. Similarly, the IconMail tree, which does not have an objectId, does not use this setting if defined. Generic Create Form: Do not use object-specific expressions. Access Expression is not supported for non-object based structure browsers. | attribute[$<attribute_Weight>].value > 100 The toolbar item displays only if the Weight attribute on the business object is greater than 100. attribute[$<attribute_Originator>].value == owner For menu command objects: context.user.name=="Test Everything" context.user.isassigned[Employee] == TRUE |
| command objects for menus and toolbar items, PowerView tabs, columns, form fields, generic create forms, configurable search form, tree commands, structure browser pages |  |  |
| Access Function | The name of the JPO method to invoke in the JPO specified for the Access Program setting. The Access function gets the input parameter as a HashMap, which contain all the request parameters that were passed into the Form page. The JPO method must return an object of class Boolean. If the returned value is true and no other access control prevents access, the component is displayed. If false, it is hidden. This is particularly useful if there is a criteria for access has nothing to do with users' roles. For example, suppose a link should only be shown to users who are employees of the host company. The JPO and method might check the user's company and display the link only if the user is from the host company. Generic Create Form: Do not use object-specific expressions. | The name of an access check method in the JPO specified in the Access Program setting, such as: emxAccessCheck() |
| command objects for menus and toolbar items, PowerView tabs, columns, form fields, generic create form, configurable search form, tree commands, structure browser pages |  |  |
| Access Mask | Specifies the accesses the user must have for the current business object in order for the component to be displayed. When the setting is used for commands connected to an app category menu or the Actions menu on the top bar, the expression is evaluated on the Person business object. It does not depend on any objectId to be passed in as a URL parameter. When the setting is used for commands in page toolbars, menus, trees, columns, and form fields, the access mask is evaluated on a specific business object. This business object is available only when the objectId is passed in as a URL parameter to the JSP. Administrators need to make sure that an objectId is available before configuring this setting for configurable toolbar menus and commands. In cases where no objectId is passed, such as a page originating from an app category menu, the Access Mask setting is ignored. The IconMail tree, which does not have an objectId, does not use this setting if defined. If the user does not have all the specified accesses in the business object's policy for the current state, the component is hidden. If the user has the access and no other access control prevents access, the component is displayed. You can specify multiple accesses by separating the accesses with a comma. Access Mask is not supported for non-object based structure browsers and toolbar custom filters. | Any set of accesses, separated by a comma. For example: Modify Delete ToConnect ToDisconnect FromConnect FromDisconnect Modify,Delete FromConnect,FromDisconnect |
| command objects for menus and toolbar items, PowerView tabs, columns, form fields, tree commands, structure browser pages, subscription commands |  |  |
| Access Program | Controls access to the UI component based on the output from a method in the specified JPO program. The program must be defined in 3DSpace . This setting requires that the Access Function setting also be specified. If Access Function is not set, the Access Program setting is ignored. The following input values are required for the program: A list of all of the request parameters in a HashMap Method name as a string JPO Program Name as a string Context The output must be a Boolean. Generic Create Form: Do not use object-specific expressions. | Name of a JPO defined as a program object in Live Collaboration, such as: emxAEFCollectionAccess |
| command objects for menus and toolbar items, PowerView tabs, columns, form fields, generic create form, configurable search form, tree commands, structure browser pages |  |  |
| Action Label | Only used with toolbar commands if Input Type is combobox or textbox. If used, a submit button shows after the combobox or textbox and uses the string specified by this setting as its label. The button refers to the JSON code/href specified on the textbox or combobox, so that a second command is not required to configure the button. | emxFramework.SampleFeature.Find Find emxFramework.Common.Filter Apply Filter |
| Toolbar textbox |  |  |
| Action Type | Tells the configurable toolbar that the command is a separator instead of a link. If the setting is not included, the toolbar treats the command as a standard link. | Separator--A line that separates one or more links. If the command is assigned to the top-level menu and is therefore on the toolbar itself, it is considered a vertical separator. Otherwise, it is considered a horizontal separator. |
| command objects for toolbar items |  |  |
| Add Input Type | Defines the input control used with the insert new row functionality when the structure browser is in edit mode. | textbox (default) combobox radiobutton listbox listboxmanual |
| columns for structure browsers |  |  |
| Additional Query | When Type Ahead is configured on the field/column (by setting the RangeHref to emxFullSearch.jsp or using a predefined Type Ahead Chooser), this setting defines additional field/selection critiera used to restrict the selection list. See About Automatic Type Ahead . | <FieldName1>=<select expression1>:<FieldName2>=<select expression2>: |
| form object field, column |  |  |
| Admin Type | Use to translate columns or fields whose values are administrative types or ranges of attributes. Based on the administrative type, the value is translated and presented. For attributes, it is necessary to provide the symbolic name of the attribute, which starts with "attribute_" and is followed by the attribute name with no spaces. For example, suppose you are configuring a field that shows an object's current state and you want the state name to be translated. You would add this setting and set the value to State. The translations for administrative object names are stored in the emxFrameworkStringResource.properties files, as described in the Internationalizing Dynamic UI Components . | Type State Role Relationship attribute_UnitOfMeasure These keywords get the field values translated for the appropriate type name: Type State Role Relationship Policy Group Vault Attribute (for translating the attribute name, not the range values) To translate an attribute value and range values, specify the symbolic name of the attribute. For example: attribute_UnitOfMeasure attribute_PartClassification |
| column form object field generic create form fields configurable search form structure browser pages |  |  |
| Allow Manual Edit | When true, users can manually edit the form row or column. Applicable only when the range parameter is set to a URL or when the setting format is assigned to date or for fields of type combobox. It is ignored in all other cases. When this setting is true, the Admin Type setting is ignored. For a toolbar link, only used when the format=chooser setting is defined, which is only used when the Input Control = textbox setting is defined. | false (default)--Manual entry is not allowed. true--Manual entry is allowed. |
| form object field generic create object form field column configurable search form structure browser pages toolbar link commands |  |  |
| Alternate OID expression | By default, when a column's data is configured to show as a hyperlink using the href parameter, the system passes the ID of the object for that row in the objectId parameter or the business object the form page applies to. Using this setting, you can configure the hyperlink so a different objectId is passed. The system passes the ID for the object returned from the expression defined in this setting. | $<to[relationship_NewPartPartRevision].from.id> $<to[relationship_EBOM].from.id> |
| structure browser pages, form object field |  |  |
| Alternate Policy expression | This setting is required to display the state of any connected objects and show the value translated. This setting is applicable only when the Admin Type setting is set to State. | Any select expression that evaluates to a policy of a connected object. |
| structure browser pages |  |  |
| Alternate Type expression | When the Show Alternate Icon setting is true, this expression is used to obtain the object type. Based on the obtained type, the corresponding icon is displayed. | $<to[relationship_NewPartPartRevision].from.type> $<to[relationship_EBOM].from.type> |
| column, form object field, structure browser pages |  |  |
| Auto Filter | Determines whether users can filter the rows based on data in the column. If at least one column is capable of autofiltering, the Filter tool displays in the page toolbar. This setting cannot be applied to any column that has multiple values for a single data column cell. Configuring many columns for auto filtering or applying auto filter to inappropriate columns can potentially have high performance impact when viewing the filtered list. For optimum performance, define a limited number of columns with Auto Filter. Also, choose columns that have a limited set of possible values on which the user may want to filter the data. For example, state and owner columns. Columns such as description and name are inappropriate because of the unlimited number of values that will result. If passed, the autoFilter URL Parameter overrides this setting. Filtering applies to root level objects only. | true--The Filter tool displays in the page toolbar. When clicked, the column is available on the Auto Filter Selection page. false (default)--The column is not available in the Auto Filter Selection page. |
| column structure browser column |  |  |
| Auto Fit Cell | When true, the textbox input control matches the cell width; it does not overlap the adjacent cell. This setting only applies when the Input Type setting is textbox . | true -- The width of the textbox input control is constrained to the width of the column. false(default) -- The width of the textbox input contol is not constrained and may overlap the adjacent column. |
| structure browser column |  |  |
| autoNumber | Used for Name columns in an editable structure browser. When autonaming is used for objects, this setting allows the user to choose the auto number series. The value for this setting is the symbolic name for the type. | autoNumber=type_HardwarePart |
| Name column in a structure browser |  |  |
| Access Behavior | This setting works with the results of the Access Expression, Access Mask, or Access Program/Access Function settings. Those settings return a true or false value. When true, the menu command shows in the list and can be selected by the user. When false, this setting defines whether a menu command is hidden from the user (hide; does not display in the menu) or dimmed (disable) so that it cannot be selected. This setting does not work with Role accesses. Commands not available to a user based on role are always hidden, regardless of the value of this setting. | hide (default) disable |
| Toolbars |  |  |
| Calculate Average | A setting of true shows the Average of the column values. This is calculated using the total of the values in the column divided by number of rows. Default Label is Average. | true false |
| columns |  |  |
| Calculate Sum | A setting of true shows the total of the column values. Default Label is Total. | true false |
| columns |  |  |
| Calculate Maximum | A setting of true shows the largest number of the column values. Default Label is Maximum. | true false |
| columns |  |  |
| Calculate Minimum | A setting of true shows the smallest number of the column values. Default Label is Minimum. | true false |
| columns |  |  |
| Calculate Median | A setting of true shows the middle number of the column values. If there is an even number of values, the average of the two middle values is shown. The median value for the odd number of rows is not rounded since no calculation is done. Default Label is Median. | true false |
| columns |  |  |
| Calculate Standard Deviation | A setting of true shows the standard deviation of the column values. The formula used for standard deviation is: sd = square root [sum( x - xbar)^2/ (N-1)] Where N is the total number of elements. xbar is the mean of the column values Default Label is Standard Deviation. | true false |
| columns |  |  |
| Calendar Function | Use to specify the name of the method in the JPO specified in the Calendar Program setting that retrieves the non-working days based on the calendar defined for the location. | The name of a function in the Calendar Program JPO, such as: getNonWorkingDays |
| columns, structure browser column objects, form field objects |  |  |
| Calendar Program | Use to specify the name of a JPO that contains a method to get the non-working days for a calendar. | The name of a JPO, such as: emxWorkCalendar |
| columns, structure browser column objects, form field objects |  |  |
| Color Cue Expression | See Collaboration and Approvals Administration Guide : Color-coded Refinements for a description of how to use the color cue settings. This setting defines the type of data in the Color Cue Expression Type setting. The Color Filter setting must be set to true if you include this setting. Cannot be used on columns where Column Type=ProgramHTMLOutput . | string numeric regex date |
| structure browser columns |  |  |
| Color Cue Expression Type | Semicolon-separated list expressions defining how to choose which colors should be shown for which values. When Color Cue Expression=string , this setting is defined in this format: <value1>,<value2>\|<color1>; <value1> and <value2> define text values that could show in the column and should both show the same color, defined by and <color1>. Use a comma to separate the values, and a pipe to separate the color. When Color Cue Expression=numeric , this setting is defined in this format: <value1>,<value2>\|<color1>; Where <value1> and <value2> define a range. If you define an overlapping range, no colors will be used in this column. The Color Filter setting must be set to true if you include this setting. Cannot be used on columns where Column Type=ProgramHTMLOutput. | Gate\|yellow,Milestone\|orange,Phase\|blue,Project Space\|gray |
| structure browser columns |  |  |
| Color Cue Label | A semicolon separated list of labels to use in the refinement pane for numeric, regex, and date expression types. You should define the same number of labels as colors defined in the cue expression. | Small Part;Medium Part;Large Part |
| structure browser columns |  |  |
| Color Filter | Enables the visual cue (color square) for the column. Only applies if the Auto Filter = true setting is also defined for the column. By default, the visual cues support 24 standard colors. If you want to control which colors are used for specific values, set values for the Color Cue Expression Type and Color Cue Expression settings. | true false |
| structure browser columns |  |  |
| Cols | Used when the input type is set to textarea. This setting limits the length of the textarea on the form and specifies the visible width in average character widths. If not specified, it uses the HTML default, which is 25. | 25 40 50 |
| form object field configurable search form |  |  |
| Column Count | The number of name/value columns to draw horizontally. For Create forms, this setting applies only to a field defined with Field Type = Dynamic Attributes. | 1 (default) 2 3 n |
| create form fields |  |  |
| Column Icon | Use to display an icon for the column's data instead of other data. Required when the setting "Column Type" is assigned to "icon". | Name of an image file such as: images/NewWindow.gif images/EditItem.gif |
| column, structure browser pages |  |  |
| Column Style | Defines styles from the CSS for the specific field. To define alignment, use one of these values: left-align center-align right-align For numeric fields, the default is left-align. | left-align |
| column, form field |  |  |
| Column Type | The setting "Column Type" is used when no expression is defined for the column data. | program--The values for this column are obtained from a program (JPO). With this setting, the program and function name are required as settings. programHTMLOutput--Same as the "program" setting above, except that the column value output is in HTML format. Column values are placed in a cell between <td> and </td> tags. This setting ignores other column settings such as Show Type Icon, href, format, and Alternate OID expression. icon--Used when the column values are shown as an icon. The setting "Column Icon" must be defined with the icon to be displayed. image--Used when the column values are images. When used, the column shows the primary image associated with the business object (cannot be used with Structure Browser). checkbox--Used when the column values are check boxes shown dimmed or not-dimmed based on the access to the object in that row. This access can be based on business logic and defined in a JPO or it can be role-based and defined by assigning roles to the column. If the checkboxes have no access restrictions, this setting is not required. separator--Used to define a column of white space between standard data columns. A separator is especially useful to separate two groups of columns. file--Shows which hyperlinks to a Quick File Access page listing files checked into or connected to the object. The optional Relationship Filter setting defines how to locate related files. |
| column structure browser pages |  |  |
| Comparable | Used only for showing a structure compare report. Defines whether the column can be used as a comparison criteria in a structure compare report. By default, all columns are comparable except for Column Types of: Image Icon Separator | true (default) false |
| column |  |  |
| Compare Report | Used only for showing a structure compare report. Defines whether to show the column in the structure compare report. By default, all columns are shown except for Column Types of: Image Icon Separator | hide show (default) |
| column |  |  |
| Confirm Message | Provides a JavaScript confirmation message when users click on the toolbar item. For example, a custom delete message can be configured for the onClick event to display a JavaScript confirm message to the user. The Confirmation dialog has the "OK" and "Cancel" button. Clicking OK proceeds with the processing and Cancel cancels the operation. | The actual text to display in the confirmation message or a string resource property key. To internationalize the message, a string resource key must be used: Are you sure you want to delete this object? emxFramework.common.alertMsg |
| command object for toolbar items |  |  |
| Create Exclude | Comma-separated list of attributes that will not be included in the webform when opened in Create mode when a field on the webform has been defined as Field Type = Dynamic Attributes. | For example: attribute_Cost |
| form object fields |  |  |
| Currency Converter | Specifies whether the target page should include the Currency Converter tool. In the href URL called when the UI component is clicked, the system passes a parameter called CurrencyConverter and includes the value specified for this setting. If the value is True, the target page includes the Currency Converter tool. If this setting is not included, the value is assumed to be false and the target page does not display the Currency Converter tool. This setting should only be used with Sourcing Central. | True False (default) |
| menu objects for navigation trees command objects for menu links and tools, tree commands, and toolbar items columns, form object fields |  |  |
| Currency Expression | The name of the currency to convert from. Required for columns whose values are monetary and that can be converted from one currency to another using the Conversion tool and defined exchange rates. The value should be a select clause that returns the value for the Currency attribute for a specific object or relationship, or the actual value. The currency to be converted from should be the currency in which the user entered the data in (the As Entered currency). This setting should only be used with Sourcing Central. | For example, to convert currency data for an RFQ Quotation, the following select clause returns the supplier's currency format. to[Supplier Line Item].attribute[Currency] |
| columns |  |  |
| Decimal Format | Defines the decimal precision (number of digits after the decimal point) for displaying values in numeric columns or fields. | 5 2 |
| structure browser columns, form fields |  |  |
| Default | If a field's value is empty or null and this setting is defined, the default value is displayed for the field. Specifies the default value for a toolbar custom filter, and can be a string resource or static text. If the control is configured as a combobox, the Default value must be one of the Range Display Values or Range Values (depending on which setting you used). | The default value you want to display. This can be a string resource key or the actual characters you want to fill in as the default. The wildcard (*) can be used for search criteria fields. emxFramework.Common.default All * emxFramework.CustomControl.Default |
| form object field generic create object fields configurable search form toolbar custom filter |  |  |
| Default Category | Specifies the category that should be selected when the tree first opens or is first inserted into another tree. By default the root node is selected, which means the object's Properties page displays. If the tree contains a category that users frequently want to see, you can use this setting to have it selected instead. Alternatively, you can pass the DefaultCategory parameter to emxTree.jsp. If both are defined, this URL parameter overrides the setting. A tree does not look for default categories defined for its sub-trees (assigned sub-menus). | The name of a tree command object that should be selected when the tree first opens. The command object must be assigned to the tree menu object and if it is not, the root node is selected. Default Category=PMCWBS |
| menu objects for navigation trees |  |  |
| Default Function | Specifies a JPO used to populate the default value when using the add existing row functionality. | JPO name |
| columns in structure browser |  |  |
| Default Program | Specifies the method within the JPO specified by the Default Program setting. | method name in the JPO |
| columns in structure browser |  |  |
| Delimiter | When using Input Type = dynamictextarea setting, this setting defines the character that separates values when the field is in View mode. In Edit mode, each value shows on a separate line. A comma is the default delimiter. If you specify any of these characters as the delimeter, a comma is used instead: $ \ ' " * \| ? ( ) > | <any string value> , (default) |
| form object fields |  |  |
| Diff Code | When multiple columns in a structure compare Complete Summary Report have different values, this setting defines what to show in the Diff Code column. The code value for the column with the lowest precedence shows in the Diff Code column. Multiple columns can have the same precedence, and if both columns have different values, both code values show in the Diff Code column. | <precedence:code value> 1:Attribute |
| structure browser columns |  |  |
| Display Format | Specifies the number of the date format for any non-editable column or field where the value of the format setting is "date." | 3 - SHORT (12/12/52) 2 - MEDIUM (Dec 12, 1952) 1 - LONG (December 12, 1952) 0 - FULL (Tuesday, December 12 1952 AD) Default is set in emxSystem.properties: emxFramework.DateTime.DisplayFormat=MEDIUM. emxSystem.properties uses words, but the Display Format setting uses numbers. |
| columns form object fields configurable search form structure browser pages |  |  |
| Display Time | Controls whether the time is displayed along with the date for columns and fields whose format is set to date. If no time zone preference is set, then the DateTime is shown in the browser's time zone. The time is shown in terms of GMT+/- hh:mm, (e.g., Saturday, August 21, 2004 12:45:00 PM GMT-04:00). To get the time in a format like EST or PDT, set the time zone preference to a specific zone. | true false Default is set in emxSystem.properties for the property emxFramework.DateTime.DisplayTime |
| columns form object fields configurable search form structure browser pages |  |  |
| Display View | When defining a menu or command for a page-level toolbar for a structure browser, defines whether that command shows in a specific view. The views are selectable from the menu. The value is a comma-separated list of these possible values: Detail (the Table view in the menu) Tree (the Graph View) Thumbnail For example, if the setting Display View=Detail,Tree is defined for a command, that command is only enabled when the user is viewing the structure browser in either the Table view or the Graph view. If viewing the Thumbnail view, that command is not available. | Detail Tree Thumbnail Detail,Tree Detail,Thumbnail Thumbnail,Tree Detail,Thumbnail,Tree |
| menu or command for a page-level toolbar for a structure browser |  |  |
| Draggable | Specifies that the item in this column can be dragged to another location within the structure browser. Default is false. | true false (default) |
| structure browser columns |  |  |
| Drop Actions | Specifies the drag-and-drop functions allowed on this column. Default is Copy. | Copy,Move Copy (default) Move |
| structure browser columns |  |  |
| Drop Directions | When connecting a dropped item to its parent (using a relationship specified by the Drop Relationships setting), specifies the end of the relationship used for the dropped item. | from to |
| structure browser columns |  |  |
| Drop Items | Indicates if one item or multiple items can be dropped into this column in a single operation. Default is Single. | Single Multiple |
| structure browser columns |  |  |
| Drop JPO | Defines a JPOName:methodName used to perform the required action, if that action is anything other than connecting the dragged and dropped on items. |  |
| structure browser columns |  |  |
| Drop Relationships | Comma-separated list to specify the relationships used to connect a dropped item to the context item. You can use the symbolic or actual name of the relationship. To define any other action than connecting the dragged and the dropped on item, use the Drop JPO setting. | relationship_EBOM |
| structure browser columns |  |  |
| Drop Types | Comma-separated list to specify the object types that can be dropped into this column. You can use the symbolic or actual name of the object type. | type_Part,Document |
| structure browser columns |  |  |
| Droppable | Specifies that objects can be dropped into this column. | true false (default) |
| structure browser columns |  |  |
| displayMode | Defines in which mode, Edit or View or Both, that this column is visible. When used, the visibility of the column is defined by the current mode and the value of this setting instead of any Access Expression. For example, if the table is in view mode and this setting is Edit, then the column is not visible. | Edit View Both (default) |
| columns used by emxIndentedTable.jsp |  |  |
| Dynamic Command Function | Defines the method in the JPO specified by the Dynamic Command Program setting that returns a list containing the data structure to build the right-click menu or the categories to add to a tree. In general, the list contains dynamic options based on the user context. This setting is only used with the right-click menu component and the toolbar component. | <JPO Method Name> |
| toolbar menu objects tree menu objects |  |  |
| Dynamic Command Program | Defines the JPO invoked from a right-click menu, navigation tree, or toolbar menu component. | <JPO Name> |
| toolbar menu objects |  |  |
| Dynamic Field Function | When the Field Type setting is set to Dynamic, this setting defines the method in the Dynamic Field Program used to retrieve attributes that have the IPML.CustoUserAccess property set to ReadWrite or ReadOnly. getDynamicAttributes is provided by BPS for this purpose. | getDynamicAttributes |
| emxCreate.jsp emxForm.jsp |  |  |
| Dynamic Field Program | When the Field Type setting is set to Dynamic, this setting defines the JPO that contains the method used for adding dynamic attributes to forms. emxUnifiedTyping is provided by BPS for this purpose. | emxUnifiedTyping |
| emxCreate.jsp emxForm.jsp |  |  |
| Dynamic URL | When enabled, users can enter URLs or mxLink values and the values will display and function as hyperlinks. | enable (default) disable |
| form and create form object fields columns structure browser columns |  |  |
| Edit Access Mask | Use to control cell-level access in an editable structure browser. This access check is done on all objects in one database call along with getting the column values. If any cell does not have the specified access, then the cell is shown as read only. | Any access mask, for example: modify, connect |
| columns structure browser |  |  |
| Edit Exclude | Comma-separated list of attributes that will not be included in the webform when opened in Edit mode when a field on the webform has been defined as Field Type = Dynamic Attributes. | For example: attribute_Cost |
| form object fields |  |  |
| Editable | Use to indicate whether the column or field is displayed as editable or read only. Only applies for Edit mode. View mode ignores the setting. For forms, default is true. For structure browsers, default is false. | true--Users can edit the column or field when shown on the Edit mode form. false--Users cannot edit the column or field when shown on the Edit mode form. The field looks just like it does in View mode except it is never hyperlinked. |
| columns, form object fields, generic create form fields, configurable search form, structure browser |  |  |
| Effective Date Expression | Used for columns whose values are monetary and that can be converted from one currency to another using defined exchange rates. The value should be a select clause that provides the value for the Effectivity Date attribute on a specific object or relationship. The system uses this date to get the currency conversion whose rate period falls within this date. If this setting is not added, the current date is used. | For example, for currency data for an RFQ Quotation, the following select clause returns the effectivity date. to[Supplier Line Item].attribute[Effectivity Date] |
| columns |  |  |
| Expand Function | Specifies the function in the JPO specified in the Expand Program setting that gets the list of child nodes for the tree category. | Name of a method in the Expand Program JPO. |
| tree command objects |  |  |
| Expand Inquiry | Use to configure the tree category name so a list of business objects is inserted under it automatically, without users having to first view details for objects. For example, Folder categories are often configured to display all folders without users having to view the folders first. Use this setting to define the list of business objects using an inquiry administrative object. You can also define the list using a JPO with the Expand Program and Expand Function settings. If both an inquiry and JPO are specified, the JPO takes precedence. | Name of an inquiry administrative object that retrieves the business objects to list for the category: TMCFolder TMCSubFolders ENCEBOMList |
| tree command objects |  |  |
| Expand Program | Use to configure the tree command so a list of business objects is inserted under it automatically, without users having to first view details for objects. For example, Folder categories are often configured to display all folders without users having to view the folders first. Use this setting to define the list of business objects using a JPO. You can also define the list using an inquiry with the Expand Inquiry setting. If both an inquiry and JPO are specified, the JPO takes precedence. This parameter specifies a JPO that contains a method to get the list of objects to insert under the category name. The method is specified using the Expand Function setting. Important Note: This feature is recommended primarily for hierarchically organized categories such as Folders and Subfolders. For best performance and consistency of behavior across applications, the preferred method of navigation is to use the standard tree behavior that does not use a dynamic expand. If conditions require the use of this setting, try to restrict its use to categories that will contain relatively few objects. | Name of a JPO program added to the database. |
| tree command object |  |  |
| Expanded | When defining a menu or command for a page-level toolbar or the RMB (right-mouse-button, also called the context menu), specifies if the menu should be automatically expanded when the user selects it. Possible values: When False and a user opens the menu, the triangle pointer shows to allow the user to expand and collapse submenus. Submenus are not expanded (unless the submenu has the Expanded=True setting defined). When the submenu is expanded, scrollbars appear if needed. When True and the user opens the menu, the menu shows all submenus fully expanded (unless a submenu has the Expanded=False setting defined). The user cannot collapse the menu (the triangle pointer does not display; an icon shows in its place) and no scrollbars appear. You cannot use this setting for the top bar or any My Desk menus (the menus that load categories into the navigation pane when a user opens an app from the compass). | True False (default) |
| menu or command for a page-level toolbar or the RMB |  |  |
| Export | Specifies whether or not column data is exported. Use this setting to change the export value on a column-by-column basis. By default, all column types except programHTMLOutput are exported. To include programHTMLOutput, or exclude other column types from the export, change this setting. | true false |
| columns structure browser |  |  |
| Field Column Headers | Used in conjunction with the Field Type=Table Holder setting. Specifies the labels for the column headings. The number of labels should be the same as the value for the Field Table Columns setting and should be separated by a comma. | Comma-separated list of column heading labels: Min,Max,Avg |
| form object field create form object field |  |  |
| Field Row Headers | Used in conjunction with the Field Type=Table Holder setting. Specifies the labels for the row headings. The number of labels should be the same as the value for the Field Table Rows setting and should be separated by a comma. | Comma-separated list of row heading labels: Weight,Volume |
| form object field create form object field |  |  |
| Field Size | Determines the width of a textbox input type field. The width is given in pixels except when Input Type is textbox or not set. In that case, its value refers to the (integer) number of characters. | Number of pixels, for example: 30 20 is the default. |
| form object field configurable search form create form object field |  |  |
| Field Table Columns | Used in conjunction with the Field Type=Table Holder setting. Defines the number of columns for the table. | 2, 3, ... |
| form object field create form object field |  |  |
| Field Table Rows | Used in conjunction with the Field Type=Table Holder setting. Defines the number of rows in the table. | 1, 2, ... |
| form object field create form object field |  |  |
| Field Type | Setting used only in edit mode while updating the table data. | basic--The column displays basic information for the business object. Basic information includes name, type, originated, policy, etc. Specifying basic as the field type is only needed when the column is editable. The only editable basic information is: type, name, revision, current, policy, description, owner, vault. attribute--The column displays values for an attribute on the business object, such as Originator or Weight. |
| columns |  |  |
| Field Type | This setting is used for several purposes: To indicate that the field's data should be obtained from a program or image instead of an expression. When the field data is obtained from an expression, if the data is basic information or an attribute. The system needs to know whether a field's data is basic information or an attribute in order to update the information correctly. Specifying whether the field is basic or an attribute is only required for fields that will be editable. To indicate the field is a placeholder field that defines fields to display in a table or group. These Field Type values that are supported by emxForm.jsp are NOT supported for emxCreate.jsp, Table Holder, Group Holder, ClassificationPath, ClassificationAttributes. | program--The values are obtained from a program (JPO). The program and function name are required as settings. programHTMLOutput--Same as program, except the field value output is in XHTML format. Field values are placed in the cell between <td> and </td> tags. This value ignores other field settings such as Show Type Icon, href, format, and Alternate OID expression. ClassificationPaths--Used only with Library Central. Displays the paths where the object is classified, with a separator between hierarchies. This separator is configurable with the Library Central property string emxLibraryCentral.ClassPathSeparatorString. The default separator is '-----'. ClassificationAttributes--Used only with Library Central and the Multiple Classification Module. Displays the attributes acquired via classification. If the object is classified, it displays the classification name as the heading, then displays a subheading with the name of the attribute group, then attributes and their values acquired from the attribute group. If the same attribute is repeated in another attribute group or in another classification, this field displays a message underneath the field: "This value also appears in another Attribute Group." If a user modifies one attribute that has other occurrences in different attribute groups, this automatically updates all other occurrences of the attribute. If the object is classified but no attributes are acquired via classification, this field does not display anything. Dynamic Attributes--Displays all attribute/value pairs associated with the context object in the properties page. image--The primary image associated with the business object. basic--Displays basic information for the business object: name, type, originated, policy, etc. Use only when the field is editable. The only editable basic information is: type, name, revision, current, policy, description, owner, vault. attribute--The field displays values for an attribute on the business object, such as Originator or Weight. Section Header--Adds a new section heading between the form fields. The setting Section Level determines the heading level. Section Separator--Adds white space to separate fields and sections. Group Holder--Groups the fields under the field in one row. Uses the Group Count setting to determine the number of fields to group. When a field has a Field Type of Group Holder, it serves as a placeholder field to define the group and does not appear on the form. Table Holder--Arranges the fields under the field in columns and rows. Table Holder fields serve as placeholder fields to define the fields to display in a table. emxTable--Embeds a configurable table in the form. Used in conjunction with the table setting and either the inquiry or program setting. Dynamic--adds any attributes created using the Parameterization Console to the form. You must also define the Dynamic Field Function and Dynamic Field Program settings. |
| form object field generic crete form object field configurable search form create form object field |  |  |
| format | Specifies the type of data in the field. If the Editable setting is true and the field is on an Edit mode form, the system uses these format values to validate the field value. Validation takes place on the client side, before updating the object displayed in the form. To support the date compare logic, whenever the field format is "date", an additional hidden parameter is added to the form with the name assigned to the web form field name suffixed by "_msvalue". If there is a valid display value for the date, the hidden parameter is assigned with the value that is the equivalent of displayed date in milliseconds (calculated from midnight, January 1, 1970). This hidden parameter value can be used by the validation methods to compare two dates. Note: The hidden parameter gets updated only when the out-of-the-box calendar component is used to change the date. If the field type is changed to manual edit, the hidden parameter may not have the updated milliseconds value when the field is manually changed. In this case the validation method can simply ignore the date compare and must depend on the server side validation. The client side validation can be ignored by checking if the field is readonly. | date--Uses the tag lib "emxUtil:lzDate" to format the displayed field value. currency numeric--Use if the value must be validated as a number before updating the values. Applicable only in Edit mode. email--Displays the column values as an email address. When a user clicks the email address, the email editor configured in the client is presented. user--Used for create forms (when Editable=false) to add read-only field to support using fullname chooser--Used only on a toolbar with Input Control = textbox when the entered text should be a date or a chooser. The browse button shows after the text box. |
| form object field create form object field configurable search form toolbar |  |  |
| format | For custom filter commands, date is the only supported format. When used, the calendar tool shows after the text box. | date--Displays the column values as a date. Uses the tag lib "emxUtil:lzDate" to format the display. |
| toolbar commands |  |  |
| format | Specifies the format to display the column data. If at least one column has the format set to currency or UOM, the Conversion tool displays in the page toolbar. When a user clicks the tool, the system opens a new window and displays all column data defined with format=currency and UOM to the currency and unit of measure selected in preferences. | date--Displays the column values as a date. Uses the tag lib "emxUtil:lzDate" to format the display. currency--Displays the column values as currency. UOM--Displays the column values as Unit of Measure and enables the Unit of Measure conversion interface. email--Displays the column values as an email address. When a user clicks the email address, the email editor configured in the client is presented. numeric--Displays the column values as numbers. To perform calculations or graphically analyze the data on a column of string attributes that have numerical values, numeric must be the column type |
| column structure browser |  |  |
| function | The name of the method to call within the JPO program specified in the program setting. This method within the JPO is used to get the field values if the setting "Field Type" is set to "program" or "programHTMLOutput". | The name of a function in the program JPO, such as: getAssignedBuyerDesk getPackageAccess getParentPart getCurrentState |
| form object field, generic create object field, configurable search form, column, structure browser |  |  |
| Group Count | Used in conjunction with the Field Type=Group Holder setting. Specifies the number of fields below the Group Holder field to place in the grouped row. | 2, 3, 4, ... |
| form object field |  |  |
| Group Header | Defines header text to display over several consecutive columns. For example, if you want a group header over three consecutive columns, add this setting to each column and assign the same value for each. To separate grouped columns using a column of white space, add a separator using Column Type=Separator. | Static text or string resource id. |
| column structure browser |  |  |
| Group Name | Used for grouping fields in web forms and structure browser. The consecutive fields with same group name are considered a group. | The name of the group. |
| form objects configurable search form create form object field structure browser |  |  |
| Help Marker | Specifies the name of the help marker to call for context-sensitive help. In the href URL called when the UI component is clicked, the system passes a parameter called HelpMarker and includes the marker text specified for this setting. | The naming convention for help markers "emxhelp" followed by the object or feature and then the action, for example, emxhelproutecreate and emxhelpprojectedit. The marker is all lowercase with no spaces. |
| menu objects for navigation trees, command objects for menu links and tools, tree commands, and toolbar items, columns, form object field, configurable search form |  |  |
| Hide Label | Displays or hides the label for a particular row on a form. | True False |
| form object fields generic create form fields configurable search form |  |  |
| Hide Mode | A comma-separated list indicating that if the user is accessing the system via that mode, then the component will be hidden. Value Hide component if access mode is: Show component if access mode is: Desktop Desktop Cloud or Mobile Mobile Mobile Desktop or Cloud Cloud Cloud Desktop or Mobile !Desktop Mobile or Cloud Desktop !Mobile Desktop or Cloud Mobile !Cloud Mobile or Desktop Cloud If this setting is not defined, then the component is always visible as long as the user has access to it contextually. This setting does not apply in these cases: global toolbar menu specified by the tableMenu URL parameter passed to emxIndentedTable.jsp menu for a tree menu for right mouse button (right-click menu) | Desktop Mobile,Cloud !Mobile |
| Value | Hide component if access mode is: | Show component if access mode is: |
| Desktop | Desktop | Cloud or Mobile |
| Mobile | Mobile | Desktop or Cloud |
| Cloud | Cloud | Desktop or Mobile |
| !Desktop | Mobile or Cloud | Desktop |
| !Mobile | Desktop or Cloud | Mobile |
| !Cloud | Mobile or Desktop | Cloud |
| toolbar menu object command objects form object fields command objects for toolbar tools |  |  |
| Image | Name of the image file to display on toolbar tool commands. The value can be one of the following: Any simple image (gif) file name without any prefix. The system assumes the image is referred from the current directory and prefixes it with "images/". An image file name prefixed with ${COMMON_DIR}. The system looks for the file in the images subdirectory of the directory designated as the common directory. The common directory is defined using the eServiceSuiteFramework.CommonDirectory property in emxSystem.properties. An image file name prefixed with ${SUITE_DIR}. The system looks for the image file in the images subdirectory of the application-specific directory which is defined using the eServiceSuiteSUITENAME.Directory property in emxSystem.properties. For example, the property for Engineering Central is eServiceSuiteEngineeringCentral.Directory = engineeringcentral. | iconPerson.gif buttonReports.gif ${COMMON_DIR}/buttonEdit.gif ${SUITE_DIR}/iconCreateECR.gif If no image is specified for a tree root node, the system displays the standard no image icon to indicate there is an error in the definition of the tree. |
| command objects for toolbar tools tree command objects configurable Preferences page |  |  |
| Image | Use to specify an image file when the field value should be an image only. This setting is required when the Field Type setting is set to image. This file must exist in the application server (not in the database). You can make the image a hyperlink by including a URL in the href parameter. | images/newPart.gif images/EditItem.gif |
| form object field generic create form fields |  |  |
| Image | Name of the image used for the menu. This is optional if a label has been defined but required if there is no label. | ${COMMON_DIR}/iconSmallOrganization.gif |
| toolbar menu object |  |  |
| Image Manager Toolbar | Defines a default toolbar to use for the Image category for an object type if a specific toolbar is not defined. For Image pages that require access or other settings different from the default, you can define a custom toolbar with a custom image upload command for that page. | <Toolbar Name> APPCompanyImageManagerToolBar |
| toolbar menu object |  |  |
| Image Size | When image is the Column Type of a column in a structure browser or the Field Type of a web form, this setting defines which size of the primary image associated with the business object should display in the column. The pixel dimensions for these sizes are defined using the emxComponents.Image. Size ImageSize property. | format_mxSmallImage (default) format_mxMediumImage format_mxLargeImage format_mxThumbnailImage |
| column Form object field |  |  |
| Image Upload Command | Defines a default command used for uploading images when a custom command is not required. | <Command Name> APPCompanyUploadImage |
| toolbar menu object |  |  |
| Input Type | Only used for structure browsers or forms in edit mode. Specifies the type of HTML control to display for user input. You can also designate the size of the input boxes to allow for appropriate spacing. This is important for short numeric entry fields. Although multiple choices can be displayed in a webform, only one selection can be saved during edit. To disable the ability to make multiple selections during view of webform, you need to use programHTMLoutput and the html tag that does that. Use the radiobutton or combobox input types for fields that require a single selection. To save multiple selections, a custom JPO needs to be written using the checkbox or listbox input types which might delimit the choices in the attribute using the Studio Customization Toolkit. For toolbar commands, textbox, combobox, and submit are supported. For controls other than toolbar commands, submit is not supported. If you specify checkbox or radiobutton for an attribute with the String datatype, the attribute must be defined with a range or an error occurs when the form is in Edit mode. If using dynamictextarea with a form field, the Delimiter setting also needs to be defined if you want to use a delimiter other than the default (comma). | The setting accepts the values listed below: textbox-This is the default. The field has a single-line box for typing text. textarea-The field has a multi-line box for typing text combobox-The field has a drop-down list of options and users can only select one value. Use comboboxes for attributes that have defined ranges and for attributes whose ranges are determined with a Range Helper URL. listbox-The field has a list of range values. Although users can select more than one item in the list by holding the Ctrl key, it's best to use check boxes for multiple selection fields. radiobutton-For forms only. Shows a radio button next to each range value. checkbox-For forms only. Shows a check box next to each range value. For fields that allow multiple selections, use check boxes instead of a list box. listbox-For forms and columns only. Provides a list of range values. Although users can select more than one item in the list by holding the Ctrl key, only one value will be saved unless a custom JPO is implemented. listboxmanual--For structure browser columns. Provides a list of range values and users can select a single value as with the listbox input type. In addition the user can manually type in a value. submit--Submit button configured to send contents of input control to a processing page. dynamictextarea-Allows multiple values to be entered in Edit mode |
| form object field configurable search form column structure browser toolbar textbox |  |  |
| inquiry | Used only for fields defined with Field Type=emxTable. Either this setting or the program setting must be used to define the objects to retrieve. Specifies the inquiry administrative object that should be used to retrieve the business objects to be included in the table. | Names of inquiry administrative object separated by a comma. inquiry=SCSBuyerDesk,SCSBuyerDeskAssigned inquiry=ENCAllParts, ENCReleasedParts |
| form object field |  |  |
| Label | Use to enter the text that should appear as the label for the field. | -- |
| configurable search form |  |  |
| Label Function | Specifies the function in the JPO specified in the Label Program setting that gets the value for the root node label. | The name of a method in the Label Program JPO. |
| menu objects for navigation trees tree command objects |  |  |
| Label Program | Use to define the label for the root node/tree command using a JPO. This setting specifies a JPO that contains a method to get the value for the label. The method is specified using the Label Function setting. The input for this JPO must include: A list of all of the Request Parameters in a HashMap Method Name as a string JPO Program Name as a string The output should be the string that is returned to the tree and used for the label. There are two other ways to define the label for a tree node: using the TreeLabel URL parameter passed to emxTree.jsp and using the Label parameter for the menu object. The URL parameter takes precedence over the other two methods, then the JPO, then the Label parameter. Tree command labels can also be defined using a Label parameter for the command object. The JPO takes precedence over the parameter. | The name of a JPO that has been added to the database. If a JPO is configured for a tree command or root node label and the JPO returns an empty string or null, an exception is thrown. |
| menu objects for navigation trees tree command objects |  |  |
| Level | Define the values to show in the Expand filter combo box. The values for this setting can be: comma-separated list of positive integers All Specify Text to be processed by the expandProgram JPO. You cannot use custom levels if the structure browser is filtered using relationship/type/direction. The All and Specify reserved keywords control specific levels of expansion. Use the Label setting to define the string resource or static text for the label that shows in front of the expand combo box; use the Registered Suite setting to define the application that defines the menu for the expand filter or the expand Program JPO. | 1,2,3,4,5 All Specify... Custom expand level (for example, End Item) |
| toolbar menu object for structure browser |  |  |
| Lookup Input Type | Defines the input control used with the add existing row functionality when the structure browser is in edit mode. | textbox (default) combobox radiobutton listbox listboxmanual |
| columns in structure browsers |  |  |
| Mass Update | Used only for Edit mode. If set to false for the column, the column is not available to the Mass Update feature, but can be edited using the inline editing (one cell at a time). If set to true, the column is used by the Mass Update feature. | true false |
| columns in structure browsers |  |  |
| Maximum Length | Limits the number of characters that can be entered in a field with Input Type of textbox. If not specified, it uses the HTML default (unlimited). | Number of characters, for example: 30 |
| form object field generic create form field configurable search form |  |  |
| Maximum Length | Limit the number of characters that are displayed on drop-down menu label. The Title property of the button is used as the Alt text to display the full label. | Any number of characters, such as 25 If this setting is not set, the toolbar displays the entire label. |
| toolbar menu object |  |  |
| Message URL Label | Specifies the label to use for URL links that call the tree. For example, notifications that users have new tasks include URL links that open the Inbox Task tree for the user's task. By default, the URL link label shows the name of the Inbox Task object, which is an auto generated name. But if the route creator entered a name for the task, it is better to show that entered name for the URL link. To have the URL link show the task's entered name, add the Message URL Label setting to the tree menu object for the Inbox Task. The value would be a select macro for the attribute that contains the entered name, in this case the Title attribute. When building the URL link, the system first looks for the Message URL Label setting on the application specific tree menu, for example, ENCtype_InboxTask. If not found there, the system looks for the setting in the common tree for that object type. If the common tree does not contain the setting, the system defaults to the object's Type,Name,Revision (for example, Inbox Task IT-0000101). | The value can be any select statement, plain text or a string resource ID. For example: $<attribute[attribute_Title].value> Task Title emxEngineeringCentral.TaskTitle The string resource Id can contain macros also, as in the following example: emxEngineeringCentral.TaskTitle= $<type> $<name> $<revision>: Urgent Task |
| menu objects for navigation trees |  |  |
| Mode | Used for structure browser only. If defined for a toolbar command, that command is only enabled on the toolbar in the indicated mode (Edit or View). If not defined, the command is enabled in both Edit and View mode. If defined for a toolbar top-level menu, the entire toolbar only shows in the indicated mode and is hidden in the other mode. | edit view |
| toolbar menu object |  |  |
| Mouse Over Popup | Enables or disables the mouse over popup DIV that displays the entire contents of the cell. When enabled, the popup shows in both View and Edit mode. | enable disable (default) |
| columns used in structure browser |  |  |
| Name Field | Defines how the name field for the object will display in the form: autoName-only the autoName option is available keyin-a textbox is provided for text entry both-both a textbox and the autoName checkbox shown for the name field | autoName keyin (default) both |
| generic create form field |  |  |
| Nowrap | If the setting is true for any field or column, then the text displayed in that field or column will not wrap at any time. The default is false. When wrapping is used, the text is wrapped at a convenient space between words, or in the middle of a text string (for long strings that exceed the column width). | true false |
| form fields columns |  |  |
| Number Format | This setting only affects how numbers are displayed. Numbers being entered or edited do not show any formatting. When true, values are displayed using the format for the thousands and decimal separators based on the user's browser locale. When false, values are displayed as they are stored in the database, without any formatting. This setting only applies to numeric attributes if the format setting is defined as numeric . | true false (default) |
| structure browser columns, form object fields |  |  |
| Popup Modal | If the setting Target Location is set to popup, the window can be configured as modal or non-modal. | true--the popup window is modal false--the popup window is non-modal If the setting is not specified, the window is modal. |
| command objects for menu links and tools, toolbar items, columns, structure browser, |  |  |
| Popup Modal | Use to configure whether the popup range helper window is model or non-modal. Only used when the setting Target Location is set to popup. If a form Component is opened in a dialog in edit mode then the dialog must open as a modal dialog. For example, if a column is designed to pop up the Form Component in edit mode, then that column must have a setting Popup Modal=true to open the dialog as modal dialog. | true--the popup window is modal false--the popup window is non-modal If the setting is not specified, the window is modal. |
| form object field configurable search form |  |  |
| Popup Size | Defines the size of the popup window. The value can be one of the listed example sizes. The pixel width and height values for these sizes are defined in emxSystem.properties. See Collaboration and Approvals Administration Guide : Properties for Popup Windows . | Small Medium Large SmallTall MediumTall |
| command objects for menu links and tools command objects for toolbar items, form object field, structure browser, configurable search form field |  |  |
| PreExpand | Determines whether to let the JPO program decide whether to add the [+] icon or to simply add the [+] without running the JPO. | true--The Structure Program and Function defined for the Structure command is run to get the list of objects. Then for each object, it runs the same program to determine whether a [+] icon is needed. If the list of objects is greater than 1, a plus [+] is added to the structure node. false (default)--The JPO Program and Function is run to get the list of objects, but the same program and function is not run again for each object. Instead, a plus icon [+] is automatically added for each structure node. |
| command objects for structure trees |  |  |
| PreExpand Category | Used in conjunction with the Expand Program and Expand Function settings or the Expand Inquiry setting, which inserts a list of objects under the tree category name without requiring users to first view details of those objects. Use this setting to determine whether the tree pre-processes the JPO or inquiry to determine if there are any objects to be listed. Pre-processing offers the benefit of determining in advance that there are no business objects in the list so the + sign for expansion is not shown. But pre-processing lowers performance. Administrators who configure this setting as true need to be aware of the performance implications to doing so. While it is nice to only display the + sign when data is present, the tree must actually go and get the data at the time the tree displays to do this. Therefore, if there are many tree commands configured with PreExpand Category set to true, each of those tabs will be evaluated when the tree is displayed regardless of whether the user actually intends on viewing data in those categories or not. This potentially unnecessary processing lowers performance. | true--Pre-processing is allowed so if there are no objects, the + sign for expanding the list is not shown. false (default)--Pre-processing is not allowed so the + sign is displayed even if there are no business objects in the list. The inquiry or JPO is not processes until the user clicks the + sign. |
| tree command objects |  |  |
| PreExpand Function | Specifies a method in the JPO defined in the Expand Program setting that configures the category for dynamic expand. This method determines whether there are any business objects in the business object list retrieved from the JPO, thereby determining whether the + sign should be displayed. Use of this setting requires a change in Live Collaboration that has not yet been implemented. | Name of a method in the Expand Program JPO. |
| tree command objects |  |  |
| Printer Friendly | Passes the PrinterFriendly parameter and the entered value to the JSP specified in the href URL. JSPs that use the PrinterFriendly parameter, such as emxForm.jsp, show the Printer Friendly tool when the setting is True and hide it when False. If the setting is not included, emxForm.jsp shows the tool by default. Users can click the tool to get a version of the current page that can be printed with the browser's Print button. Note that you can also specify the PrinterFriendly parameter in the href URL for JSPs that use it. | True (default) False |
| menu objects for navigation trees command objects for menu links and tools, tree commands, and toolbar items columns form object field |  |  |
| program | Use to specify the name of the JPO program to use to get the field's data when the Field Type setting is set to "program" or "programHTMLOutput". Also can be used for fields defined with Field Type=emxTable to define the objects in the table. Alternatively, inquiry can be used. Using a JPO to populate field data is recommended only when a select expression cannot be used to obtain the field value. | The name of a JPO, such as: SCSBuyerDeskForm TMCPackagesForm ENCPartForm AEFUtilForm |
| form object field generic create form field configurable search form |  |  |
| program | The name of the JPO program to use to get the column's data. This program is used to get the column values when the setting "Column Type" is set to "program" or "programHTMLOutput" or "checkbox". Using a JPO to populate column data is recommended only when a select expression cannot be used to obtain the values. | emxSCSBuyerDesk emxTMCPackages emxENCParentPart emxCommonTableUtil |
| column |  |  |
| Pull Right | Determines whether the drop-down links are displayed in the same level as the menu or pulled right to display the links in the next level. Applies only to second-level drop-down menus. | True (default) False |
| toolbar menu object |  |  |
| Range Display Values | Only used with toolbar commands if Input Type=combobox. Holds a comma-separated list of string resources or static text values used to populate the combobox. If this setting is not specified, the Range Values setting is used. | emxCustom.Combo.Value1, emxCustom.Combo.Value2, emxCustom.Combo.Value3 Value1,Value2,Value3 |
| toolbar custom filters |  |  |
| Range Function | Use to specify the name of the method in the JPO specified in the Range Program setting. See the Range Program setting below for more information. Only used with toolbar commands if Input Type=combobox. | The name of a function in the Range Program JPO, such as: getAssignedRange getClassificationRange getPartUOM |
| toolbar custom filters columns |  |  |
| Range Program | Use to specify the name of a JPO that contains a method to get the input control value ranges (choices). A range program is used only when the Input Type setting is combobox. If the column will be used in a structure browser and you want the mass update tool to allow editing of the column, you must also pass the massUpdateTCL=true URL parameter to the structure browser. | The name of a JPO, such as: ENCParts AEFUtils |
| toolbar custom filters columns |  |  |
| Range Values | Only used with toolbar commands if Input Type=combobox. Holds a comma-separated list of values used to populate the combobox. | Value1,Value2,Value3 |
| toolbar custom filters |  |  |
| Range Function | Use to specify the name of the method in the JPO specified in the Range Program setting. See the Range Program setting below for more information. | The name of a function in the Range Program JPO, such as: getAssignedRange getClassificationRange getPartUOM |
| form object field configurable search form columns structure browser |  |  |
| Range Program | Use to specify the name of a JPO that contains a method to get the field or column value ranges (choices). A range program is used only when the Input Type setting is combobox, radiobutton, or checkbox for forms, or combobox or popup for structure browsers. If the choices need to be presented on a page in a popup window, for example in a chooser, use the RangeHref parameter instead. The corresponding method which runs to get the values of the column should not have any HTML code. | The name of a JPO, such as: SCSBuyerDeskForm TMCPackagesForm ENCPartForm AEFUtilForm |
| form object field, generic create form field, configurable search form field, columns, structure browser |  |  |
| Read Only Checkbox | For a field defined with the Boolean data type, or as a string with Boolean range values, this setting shows a checkbox instead of the TRUE or FALSE text. In edit mode, the user can check/clear the checkbox unless the Editable=false setting is specified for the field. | true false (default) |
| form objects |  |  |
| Registered Suite | The application the UI component belongs to. The system looks for files related to the component in the registered directory for that application, which is specified in emxSystem.properties. Based on the application name, the system passes the following parameters in the href URL: suiteKey SuiteDirectory StringResourceFileId This setting is required for all dynamic UI components. | The value must be set without any spaces, for example, EngineeringCentral or Framework. The value must be set to the suite name as defined in the key eServiceSuites.DisplayedSuites within emxSystem.properties. If the suite name starts with "eServiceSuite" then this prefix can be skipped and assign the remaining text to the setting. For example, if the suite name in emxSystem.properties is "eServiceSuite EngineeringCentral ", then the word " EngineeringCentral ", can be assigned as "Registered Suite". In the href URL that is called when the item is clicked, the system passes a parameter called suiteKey and includes the property name that maps to the value specified for the setting. If the Column Type is file, this value must be Components. For subscription commands, use Components. |
| menu objects for app category menus and Actions submenus command objects for menu links and tools menu objects for navigation trees columns form objects menu objects for toolbars command objects for toolbar items and PowerView tabsstructure browser |  |  |
| Relationship Filter | Optional setting that defines a comma-separated list of relationship select expressions to get the file lists checked into or connected to the Type object. | from [<relationship_ReferenceDocument>].to.id, to [<relationship_ReferenceDocument>].from.id,..,.. |
| columns |  |  |
| Remove Range Blank | Used when the input type is set to combobox to ensure that the field contains a value and is not left blank. If set to True, this setting removes the blank from the combo box list. If a default is not specified, the first value in the list is shown by default. | true false (default) |
| form object fields configurable search form fields |  |  |
| Required | Use to indicate the value for this field is required. This setting is applicable for editable fields displayed on the Editable mode form. | true--When completing the form, the user must enter a value for the field. The field label appears in red italic text. If the user does not enter a value and clicks Done, a JavaScript message appears that prompts the user to enter a value. false (default)--When completing the Edit form, the user can leave this field blank. |
| form object field genericcreate form object field |  |  |
| Revision Filter | Enables or disables the display of the revision filter button. | Enable Disable (default) |
| tree command objects |  |  |
| RMB Menu | Specifies the name of the right-click menu to associate with the component. For a type-specific menu, such as type_Part, defines the type-specific right-click menu for that type. For a column, specifies the right-click menu for that specific column. | <admin menu name> |
| structure browsers objects toolbar menu objects |  |  |
| Root Label | Determines the label for the root node(s). You can use static text, such as Search Results, or a string resource id. When using a string resource id, you can use any valid MQL expression, such as: emxFramework.Common.RootL abel= $<type>$<name>:Root If the expression does not apply to the root object (such as an attribute not associated with that object), the expression is ignored and the default label is used. If this setting is configured for a column not in the freeze pane, it is ignored. | Search Results emxFramework.Common.RootLabel |
| structure browser freeze-pane column |  |  |
| Row Grouping | Enables or disables the column from being selected for row grouping. By default, all columns are enabled for grouping except file columns ( Column Type = File setting) and hard-coded icon columns. | true (default)--column can be used for grouping rows false--column can not be used for grouping roles |
| structure browser column setting |  |  |
| Row Grouping Range Function | Method in the JPO defined by the Row Grouping Range Program setting. The page will be grouped by the pre-defined row categories even if no rows in the structure browser have that value. |  |
| structure browser column setting |  |  |
| Row Number | If the column is defined with a Group Name setting, this setting defines whether this column displays in the first or second row of the group. Only 2 rows are permitted for a group. Used for merging cells as defined in Merged Cells . | 1 (default) 2 |
| structure browser |  |  |
| Row Select | Used only for links on pages. Specifies whether the JSP specified for the href expects one, at least one, or no rows in the table (Middle Frame) to be selected. If the Row Select setting is set to "single" or "multi", the setting Submit must be set to "true" in order to get the details of the selected item(s). If the setting is not specified, it is assumed to be none. A JavaScript alert message is displayed on error conditions when the onClick event occurs. The error conditions are explained in the column to the right. For subscriptions, this setting must be set to multi and the Submit setting must be set to true. | single-Appropriate only for links that appear on pages that have radio buttons or check boxes for each row. The JSP specified for the href expects exactly one row in the table (middle frame) to be selected. If more than one item is selected, a JavaScript alert message is displayed. The default message is set to the value of the following key in emxFrameworkStringResource.properties: emxFramework.Common.PleaseSelectOneItemOnly. Currently, the message is set to "Please Select One Item Only". If no item is selected, then the user will see the following message: "Please select an Item". This message corresponds to this key value in the emxFrameworkStringResource.properties file: emxFramework.Common.PleaseSelectitem multi-Appropriate only for links that appear on pages that have check boxes or radio buttons for each row. The JSP specified for the href expects at least one row in the table (middle frame) to be checked. If no item is selected, then the user will see the following message: "Please select an Item". This message corresponds to the following key value in the emxFrameworkStringResource.properties file: emxFramework.Common.PleaseSelectitem none (default)-The JSP specified for the href expects no row to be selected. If a row is selected, the JSP does not use it. |
| command objects for toolbar items |  |  |
| Row Span | If this column is defined with a Group Name setting, defines how many rows each cell in this column will span. If set to 1, no spanning is done. If set to 2, the column value spans 2 rows. You cannot span more than 2 rows. Used for merging cells as defined in Merged Cells . | 1 2 (default) |
| structure browser |  |  |
| Rows | Used when the input type is set to textarea. This setting limits the height of the textarea on the form and specifies the number of visible text lines. If not specified, it uses the HTML default, which is 5. | 5 10 s 20 |
| form object field configurable search form |  |  |
| Section Level | Used in conjunction with the Field Type: Section Header setting to define the level of heading. | Two heading levels are available: 1 (default)--Font for heading label is large and a horizontal line is included above the label. 2--Font for heading label is smaller and the heading is in the same gray rectangle as standard fields. |
| form object field generic create form field configurable search form |  |  |
| Selectable in Preferences | Only for commands in app category menus and submenus. Controls whether the link is available as a preferred Home page. If not set, the system assumes true and the command is listed as a Home page preference. Commands that call pages to display in a popup window, such as an object search command, should not be available as a Home page preference. | True False |
| command objects for menu links and tools |  |  |
| Show Alternate Icon | If the column value to be displayed with the icon is different from the current row's object icon, set this setting to true. ITo get the right alternate icon, the Alternate Type expression setting must be defined with the expression to obtain the object type. | true false |
| column structure browser |  |  |
| Show Alternate Icon | If the field value needs to be displayed with an icon that is different from the current object type icon, set this setting to true. To get the right alternate icon, the Alternate Type expression setting must be defined with the expression to obtain the object type. | true false (default) |
| form object field |  |  |
| Show Clear Button | Adds a Clear hyperlink next to the field. This setting only applies to textarea/textbox input type fields. When the Clear link is clicked, it clears the content of the textbox/textarea input type field. | True False |
| form object field generic create form field configurable search form structure browser columns |  |  |
| Show Type Icon | If set to true, the field or column value is displayed along with the object type icon as defined in the emxFramework.smallIcon property in emxSystem.properties. The icon is displayed to the left of the field or column data. If no property is defined for the type's icon, the system looks for a property defined for the parent type, then grandparent type and so on. If no property is defined for any type in the hierarchy, the system uses the default icon specified in the emxFramework.smallIcon.defaultType property. | true false (default) |
| column structure browser generic create form field |  |  |
| Slidein Width | When the Target Location setting is set to slidein, this setting defines the width in pixels of the slidein window. If you do not specify this setting or you provide a non-numerical value, the default width (350) is used. The minimum value is 350. If you provide a smaller value, 350 is used. The maximum value is 900. If you provide a larger value, 900 is used. You can also set the value to wide to use the configured width as defined by the emxFramework.widerslideIn.size parameter in emxSystem.properties. | wide 500 800 |
| web forms |  |  |
| Sort Direction | Used only for fields of type combobox and listbox. The direction to sort the option list. | ascending (default)--Sort a to z or 0 to n. descending--Sort z to a or n to 0. none--The option list is not sorted. |
| columns, structure browser, form object fields |  |  |
| Sort Program | Specifies the JPO program name that contains a custom sorting algorithm. This setting is used only when the column parameter sorttype is assigned to "other". | Name of a JPO added as a program in the database, such as: FindNumberSort CustomSort |
| column |  |  |
| Sort Range Values | Enables or disables the sorting of range values in combobox or listbox controls. When enabled, the list is sorted based on the datatype and using the direction defined by the Sort Direction setting. When disabled, no sorting is done on the list. In a drop-down, range values populated based on an attribute expression will be sorted based on the attribute's datatype. Range values populated using the Range Program and Range Function settings will be sorted alphanumerically. If the Range Program or Range Function returns numeric or date values, the alphanumeric sort will not be appropriate. The program or function should sort the values in the required order, and this setting should be disabled. This setting does not apply to fields or columns configured for attributes associated with a dimension. Dimension ranges are always sorted as alphanumeric in ascending order. This setting cannot be used in custom JSPs that use the editOptionList taglib. For this specific situation, you can use the sortType attribute for that tag. | enable (default disable |
| column, form field, structure broswer, configurable search form field |  |  |
| Sort Type | Determines how the column is sorted. The MQL sorttype subclause must also be included. Use on its own to sort numeric columns ( sorttype = numeric ) OR Use in conjunction with the Sort Type setting. Two examples: ( sorttype = other AND Sort Type = integer) ( sorttype = other AND Sort Type = real ) | date integer real AlphaNumericLarger--consider all numeric values as larger than the alphanumeric values AlphaNumericSmaller--consider all numeric values as smaller than the alphanumeric values |
| column structure browser |  |  |
| Sortable | Controls whether users can sort the structure browser based on data in the column. If a column is sortable, users can click the column heading to sort the structure browser based on that column. | true (default) false |
| column structure browser |  |  |
| sortColumnName | Used only for fields defined with Field Type=emxTable. Specifies the column by which the table should be sorted when the page is first loaded. If no column is specified, the rows are listed in the order they are retrieved from the database. | Name of column in the table specified in the table=TABLE_NAME setting. |
| form object field |  |  |
| sortDirection | Used only for fields defined with Field Type=emxTable. The direction to sort the rows by. | ascending (default)--Sort a to z or 0 to n. descending--Sort z to a or n to 0. |
| form object field |  |  |
| Structure Function | JPO method name to be used for getting the object list. | The name of a method in the JPO specified in the Structure Program setting, such as: getWorkspaceFolders getEBOMs |
| command objects for structure trees |  |  |
| Structure Inquiry | Inquiry administrative object name that gets the object list to display within the structure. This is an alternative to the JPO approach and the JPO approach takes precedence. This setting is not implemented. | The name of an inquiry administrative object: SCSWspFolderStructure ENCEBOMList |
| command objects for structure trees |  |  |
| Structure Menu | Specifies the menu administrative object that defines a structure tree for the object type. | The name of a menu administrative object. |
| menu object for navigation tree |  |  |
| Structure Program | JPO program that contains the method to be used for getting the object list to display within the structure. The method is specified in the Structure Function setting. | The name of a JPO program added to the database. SCSWorkspaceStructure ENCBOMStructure |
| command objects for structure trees |  |  |
| Style Column | Customizes the sytle for a column. The value must be a class defined in the dsecUITypeCustom.css. For the example style, the definition in the css file could be: ColumnBackGroundColor { background-color : rgb(252,186, 186); } To define alignment, use one of these values: left-align center-align right-align For numeric fields, the default is left-align. | ColumnBackGroundColor |
| column in a structure browser |  |  |
| Style Function | The method in the Style Program JPO that defines the style per cell in the column. | The name of a function in the Style Program JPO, such as: getStyleInfo |
| column in a structure browser |  |  |
| Style Program | The JPO that contains methods to defines the style per cell in the column. | The name of a JPO, such as: AEFUtilStyleJPO |
| column in a structure browser |  |  |
| Submit | Specifies whether the system should send the object ID(s) (and relationship ID, if applicable) for the current page to the JSP specified in the href parameter. For form pages, the submitted data is the object ID for the business object the page is about. For structure browser pages, the submitted data is the ID for each selected item in the table, which can include object and relationships. In either case, the IDs are sent using the relBusIdList parameter. If the setting is not specified, it is assumed to be false. | true--Submits the IDs to the URL specified in the href parameter. Should be used for actions that depend on selected rows. false--The URL specified in the href parameter is directly called from the toolbar item and no IDs from the current page are used. Use this value for links that do not require the business object IDs, such links for creating a new object. For toolbar commands in a structure browser, should be true so that the selected items in the structure are posted to the processing page. |
| command objects for toolbar items, including subscription commands |  |  |
| table | Specifies the name of a table administrative object to embed in the form. Used only for fields defined with Field Type=emxTable. | Name of table administrative object. For example: table=SCSBuyerDesk table=ENCParts |
| form object field |  |  |
| Target Location | Controls where the page specified in the href parameter appears. For columns, this value can be set to any valid frame name available to the body frame. For toolbar commands, must be listHidden or popup. To specify a tab in a PowerView window, use the administrative command name that configures the tab in the PowerView page. If the specified command name is not defined within the current PowerView, the popup value is used. Note: The Target Location setting is not available for commands in navigation trees. | content--The page replaces the tree frame. mainframe--The page appears in the main content frame, to the right of the tree frame and tab frames, replacing the page that is already there (not used for structure browser). popup--Page appears in new window. The window modality is set using the Popup Modal setting. _top--Page replaces the entire body of the browser window. topFrame--The page replaces the banner frame. (not used for structure browser) hiddenFrame--The frame called hiddenFrame is part of the top level Navigation window frameset and can be used to submit the Table frame and carry out background processing. This frame is not available for popup windows though so listHidden is a better frame to use. listHidden--A hidden frame within the table frameset. Use this frame for Target Location for any processing in the context of a table. <command name>--The name of a command configured as the target tab in a PowerView page. |
| command objects for menu and toolbar links and tools columns command objects for toolbar items subscription commands structure browser |  |  |
| Target Location | Controls where the page specified in the href parameter appears when a user clicks the hyperlinked data. This value can be set to any valid frame name that is available to the form body frame. | popup--Page appears in a new window. The window modality can be set with the Popup Modal setting. Popup is the default value and is used if the setting is not included or if the named frame cannot be found. content--The page replaces the content frame. mainFrame--Page appears in the frame that includes the content and menu frames. _top--Page replaces the entire body of the browser window. |
| form object field configurable search form |  |  |
| Target Location | This must be assigned to "searchContent." | searchContent |
| Search JSP page |  |  |
| Tip Page | Specifies whether the target page should include the Tip Page tool and the URL that should be called when the tool is clicked. In the href URL called when the UI component is clicked, the system passes a parameter called TipPage and includes the URL specified for this setting. If this setting is not included, the target page does not display the Tip Page tool. | Name of a custom html or JSP page, including any path. The starting point for the directory reference is the content directory. For example, if you want to call an html file in ematrix/doc/customcentral and the content directory is ematrix/customcentral, you would add this parameter to the href: TipPage=../doc/customcentral/tippage.html |
| menu objects for navigation trees command objects for menu links and tools, tree commands, and toolbar items columns form object field |  |  |
| Tree Scope ID | Passes the current tree's objectId to all its categories, to subtrees inserted into the tree, and to categories in the subtrees, to the nth level, in the parameter name specified as the value for this setting. This setting is valid only for tree menu objects and not for tree command objects. For example, suppose you want to pass the object ID for the current Bookmark Workspace to all tabs and subtrees in the Bookmark Workspace tree. You want to pass the object ID using a parameter called workspaceId. To do this, you would add the Tree Scope ID setting to the menu administrative object for Bookmark Workspace trees (which is called type_Project due to administrative object renaming) and enter "workspaceId" as the value. The parameter and value "workspace Id=OBJECTID OF THE WORKSPACE will be available to all the categories and subtrees of the current Bookmark Workspace to the nth level until another Bookmark Workspace object gets inserted or until any explicit URL parameter with the same name "workspaceId" is passed to the tree. | The name of the parameter you want to use to pass the object ID. For example: workspaceId projectId partId You can also pass specific parameters from the tree menu object to its categories using the AppendParameter parameter for the emxTree.jsp. |
| menu objects for tree nodes, menu objects for navigation trees |  |  |
| Type Ahead Mapping | When Type Ahead is configured on the field/column (by setting the RangeHref to emxFullSearch.jsp or using a predefined Type Ahead Chooser), this setting maps that field to the corresponding indexed field in config.xml. See About Automatic Type Ahead . | NAME LASTNAME,FIRSTNAME,USERNAME |
| form object field, column |  |  |
| Type Ahead Validate | When Type Ahead is configured on the field/column, this setting determines if the application should restrict the user to selecting one of the suggested values (if true), or if the user can enter any data in the field without BPS validating that data (if false). | true false (default) |
| form object field, column |  |  |
| Type Icon Function | Specifies the function in the JPO specified in the Type Icon Program setting that retrieves an icon to show in addition to the icon defined by the emxFramework.smallIcon.type system property. | Function Name |
| tree menu objects web forms structure browsers |  |  |
| Type Icon Program | Defines the program that contains the function specified using the Type Icon Function setting. | JPO Name |
| tree menu objects web forms structure browsers |  |  |
| TypeAhead | Enables type ahead. For mass update in a structure browser, default is true. | true false |
| configurable search form field, structure browser mass update |  |  |
| TypeAhead Program | Name of the JPO called to retrieve a list of possible values. | -- |
| configurable search form field, structure browser mass update |  |  |
| TypeAhead Function | The name of the JPO method. | -- |
| configurable search form field, structure browser mass update |  |  |
| TypeAhead Character Count | Overrides the emxFramework.TypeAhead.RunProgram.CharacterCount system property. | 2 (default) 5 |
| configurable search form field |  |  |
| TypeAhead Saved Values Limit | Overrides the emxFramework.TypeAhead.SavedValuesLimit system property. | 10 (default) |
| configurable search form field |  |  |
| UOM Expression | The name of the Unit of Measure to convert from. Required for columns whose values are measurements that can be converted from English units to Metric units or vice versa. The value can be a select clause that returns the value for the Unit of Measure attribute for a specific object or relationship or the actual value. The unit of measure to convert form should be the unit of measure in which the user entered the data (the As Entered unit of measure). This setting should only be used with Sourcing Central. | For example, for measurement data for RFQ Quotations, the following select clause returns the supplier's unit of measure format. to[Supplier Line Item].attribute[Unit of Measure] |
| columns |  |  |
| Update Function | Specifies a method name in the JPO given in the Update Program setting. Generic Create Form: This setting can not be used with the required fields: Type, Name, Policy, Vault, Owner. | The name of a function in the Update Program JPO, such as: setAssignedBuyerDesk setPackage Access setPartClassification |
| form fields,generic create form fields, columns, structure browser |  |  |
| Update Program | Specifies a JPO that contains a method to set the field value when the field is displayed on an Edit mode form and when the user clicks Done. This program is used only when the Field Type setting is program or programHTMLOutput. Using an update program is recommended only when the Field Type is not attribute or basic. Generic Create Form: This setting can not be used with the required fields: Type, Name, Policy, Vault, Owner. | The name of a JPO, such as: SCSBuyerDeskForm TMCPackagesForm ENCPartForm AEFUtilForm |
| form fields generic create formfields columns structure browser |  |  |
| Update Program Arguments | Comma-separated list of arguments to be passed to the JPO specified by the Update Program setting. | Relationship=relationship_EBOM,ConnectFrom=true |
| structure browser mass update |  |  |
| Validate | Specifies the method to be invoked for validating any cell. | The name of a method, such as: checkUniqueName |
| object, form object field, generic create form field, structure browser |  |  |
| Validate Type | This setting is used to validate any column or field value with any specific characters defined in emxSystem.properties. | Basic or Restricted--The column values are validated against the value of emxFramework.Javascript.BadChars. Name--The column values are validated against the value of emxFramework.Javascript.NameBadChars |
| column object, form field, generic create form field, structure browser |  |  |
| Vertical Group Name | Used for grouping fields vertically in create web forms. The fields with the same Vertical Group Name value will be are considered a group. | The name of the vertical group. |
| create form object field |  |  |
| View Exclude | Comma-separated list of attributes that will not be included in the webform when opened in View mode when a field on the webform has been defined as Field Type = Dynamic Attributes. | For example: attribute_Cost |
| form field object |  |  |
| Width | Number of pixels to define the column width. When this setting is not defined, by default the width is calculated based on the header text length, so that the header text is not truncated. | Column width in pixels: 150 |
| structure browser |  |  |
| width | Specifies the width of the filter toolbar control in pixels. Use this setting if you need to display many controls in a small area. | 100 |
| toolbar custom filter |  |  |

<a id="tablecolumns-setting"></a>
## Settings for Columns in a Structure Browser

来源：`TableColumns_Setting.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This table lists and describes the related column settings you can use to control the structure browser component behavior. The name and value for each setting is case sensitive.

Most of the settings supported by configurable tables are supported by the structure browser.

| Setting | Description | Accepted Values/Examples |
| --- | --- | --- |
| Access Expression | Controls access to the column based on a valid expression. The system evaluates the expression at runtime. If the expression evaluates to True and no other access control prevents access, the component is shown. Depending on the type of expression defined, the program may or may not need a valid objectID. Make sure that an objectID is available before configuring this setting for configurable toolbar menus and commands. Access Expression is not supported for non-object based structure browsers. | $<attribute[attribute_Weight].value>?> 100 $<attribute[attribute_Originator].value >== owner |
| Access Function | The name of the JPO method to invoke in the JPO specified for the Access Program setting. The Access function gets the input parameter as a HashMap that contains all the request parameters passed into the JSP page. The JPO method must return a Boolean object. If the returned value is true and no other access control prevents access, the component displays. If false, it is hidden. To see a sample JPO method to control access, see Sample JPO for Controlling Access . Use this setting to evaluate access independent of users' roles. | The name of an access check method in the JPO specified in the Access Program setting, such as: emxAccessCheck() |
| Access Mask | Specifies the accesses the user must have for the current business object for the column to display. When you use the setting for commands in page toolbars, menus, trees, table columns, and form fields, the system evaluates the access mask on a specific business object. This business object is available only when the objectId is passed in as a URL parameter to the JSP. Make sure that an objectId is available before configuring this setting for configurable toolbar menus and commands. If the user does not have all the specified accesses in the business object's policy for the current state, the system hides the component. If the user has the access and no other access control prevents access, the system displays the component. You can specify multiple accesses by separating the accesses with a comma. Access Mask is not supported for non-object based structure browsers. | Any set of accesses, separated by a comma. For example: Modify Delete ToConnect ToDisconnect FromConnect FromDisconnect Modify,Delete FromConnect,FromDisconnect |
| Access Program | Controls access to the column based on the output from a method in the specified JPO program. You must define the program in 1. This setting requires that you also specify the Access Function setting. If Access Function is not set, the system ignores the Access Program setting. The following input values are required for the program: A list of all of the request parameters in a HashMap Method name as a string JPO Program Name as a string Context The output must be a Boolean. | Name of a JPO defined as a program object, such as: emxAEFCollectionAccess |
| Add Input Type | Defines the input control used with the insert new row functionality when the structure browser is in edit mode. | textbox (default) combobox radiobutton listbox listboxmanual |
| Add Link | Displays a link to add more items to the object from existing items. Use a valid command that adds more items, such as: AddExistingIssueReportedAgainstLink | Command name |
| Additional Query | When Type Ahead is configured on the column (by setting the RangeHref to emxFullSearch.jsp or using a predefined Type Ahead Chooser), this setting defines additional field/selection critiera used to restrict the selection list. See About Automatic Type Ahead . | <FieldName1>=<select expression1>:<FieldName2>=<select expression2>: |
| Admin Type | Use to translate columns whose values are administrative types. The value is translated and presented. For attributes, provide the symbolic name of the attribute. Symbolic names start with "attribute_" and are followed by the attribute name with no spaces. Not supported for non-object based structure browsers. | Type State Role Relationship attribute_UnitOfMeasure |
| Allow Manual Edit | When true, users can manually edit the table column. Applicable only when you set the range parameter to a URL or when you assign the setting format to date or for fields of type combobox . The system ignores all other cases. When true, the system ignores the Admin Type setting. Not supported for non-object based structure browsers. | false (default)--Manual entry is not allowed. true--Manual entry is allowed. |
| Alternate OID expression | By default, when a column's data is configured to show as a hyperlink using the href parameter, the system passes the ID of the object for that row in the objectId parameter. Using this setting, you can configure the hyperlink so a different objectId is passed. The system passes the ID for the object returned from the expression defined in this setting. Not supported for non-object based structure browsers. | $<to[relationship_NewPartPartRevision].from.id> $<to[relationship_EBOM].from.id> For more information on configuring a column using an alternate object ID's expression, see Column Values Using Alternate OID in href and Select Expression and Column Values Using Alternate OID and Type Icon in href with Select Expression . |
| Alternate Policy expression | This setting is required to display the state of any connected objects and show the value translated. This setting is applicable only when the Admin Type setting is set to State. Not supported for non-object based structure browsers. | Any select expression that evaluates to a policy of a connected object. |
| Alternate Type expression | When the Show Alternate Icon setting is true, this expression is used to obtain the object type. Based on the obtained type, the corresponding icon is displayed. For more information on configuring a column using an alternate type expression, see Column Values Using Alternate OID in href and Select Expression . Not supported for non-object based structure browsers. | $<to[relationship_NewPartPartRevision].from.type> $<to[relationship_EBOM].from.type> |
| Arithmetic Expression | Comma-separated list of arithmetic expressions. Expressions can include cell variables. |  |
| Auto Filter | Determines whether users can filter the structure browser rows based on data in the column. If at least one column in the table is capable of autofiltering, the Filter tool displays in the page toolbar. If passed, the autoFilter URL Parameter overrides this setting. Filtering applies to root level objects only. See Filter for a Structure Browser Page . | true (default)--The Filter tool displays in the page toolbar. When clicked, the column is available on the Auto Filter Selection page. false--The column is not available in the Auto Filter Selection page. |
| autoNumber | Used for Name columns in an editable structure browser. When autonaming is used for objects, this setting allows the user to choose the auto number series. The value for this setting is the symbolic name for the type. | autoNumber=type_HardwarePart |
| Auto Fit Cell | When true, the textbox input control matches the cell width; it does not overlap the adjacent cell. This setting only applies when the Input Type setting is textbox . | true -- The width of the textbox input control is constrained to the width of the column. false(default) -- The width of the textbox input contol is not constrained and may overlap the adjacent column. |
| Calculate Average | When true, calculates the average of values in the column, using the defined decimal precision. By default, the calculation uses the system-wide precision (set in emxSystem.properties), or you can use the Decimal Precision setting to set a specific precision for this column calculation. | true false (default) |
| Calculate Average Label | When the Calculate Average setting is used, any value for this setting shows as the label for the column average. If not defined, "Average" is used as the label. | Average (default) <any string value> |
| Calculate Custom | When true, invokes the program defined by the Calculate Custom Program setting. | true false (default) |
| Calculate Custom Label | When the Calculate Custom and Calculate Custom Program settings are used, any value for this setting shows as the label for that calculated value. If not defined, "Custom Label" is used as the label. | Custom Label (default) <any string value> |
| Calculate Custom Program | If the Calculate Custom setting is true, this setting defines the program and function to invoke. The program can perform any calculations needed for the data in the column, with a single result. | program:function |
| Calculate Maximum | When true, displays the largest value of the column values. | true false (default) |
| Calculate Maximum Label | When the Calculate Maximum setting is used, any value for this setting shows as the label for the column maximum. If not defined, "Maximum" is used as the label. | Maximum (default) <any string value> |
| Calculate Median | When true, displays the middle value (the mean) of the column values. If the column contains in an even number of values, the average of the 2 middle numbers is used, using the defined Decimal Precision. By default, the calculation uses the system-wide precision (set in emxSystem.properties), or you can use the Decimal Precision setting to set a specific precision for this column calculation. | true false (default) |
| Calculate Median Label | When the Calculate Median setting is used, any value for this setting shows as the label for the column median. If not defined, "Median" is used as the label. | Median (default) <any string value> |
| Calculate Minimum | When true, displays the smallest value of the column values. | true false (default) |
| Calculate Minimum Label | When the Calculate Minimum setting is used, any value for this setting shows as the label for the column minimum. If not defined, "Minimum" is used as the label. | Minimum (default) <any string value> |
| Calculate Standard Deviation | When true, displays the standard deviation of the column values using this formula: where: N=total number of elements xbar=mean of the column values By default, the calculation uses the system-wide decimal precision (set in emxSystem.properties), or you can use the Decimal Precision setting to set a specific precision for this column calculation. | true false (default) |
| Calculate Standard Deviation Label | When the Calculate Standard Deviation setting is used, any value for this setting shows as the label for the column calculated value. If not defined, "Standard Deviation" is used as the label. | Standard Deviation (default) <any string value> |
| Calculate Sum | When true, displays the total of the column's values. | true false (default) |
| Calculate Sum Label | When the Calculate Sum setting is used, any value for this setting shows as the label for the column sum. If not defined, "Total" is used as the label. | Total (default) <any string value> |
| Calendar Function | Use to specify the name of the method in the JPO specified in the Calendar Program setting that retrieves the non-working days based on the calendar defined for the location. | The name of a function in the Calendar Program JPO, such as: getNonWorkingDays |
| Calendar Program | Use to specify the name of a JPO that contains a method to get the non-working days for a calendar. | The name of a JPO, such as: emxWorkCalendar |
| Color Cue Expression | See Collaboration and Approvals Administration Guide : Color-coded Refinements for a description of how to use the color cue settings. This setting defines the type of data in the Color Cue Expression Type setting. The Color Filter setting must be set to true if you include this setting. Cannot be used on columns where Column Type=ProgramHTMLOutput . | string numeric regex date |
| Color Cue Expression Type | Semicolon-separated list expressions defining how to choose which colors should be shown for which values. When Color Cue Expression=string , this setting is defined in this format: <value1>,<value2>\|<color1>; <value1> and <value2> define text values that could show in the column and should both show the same color, defined by and <color1>. Use a comma to separate the values, and a pipe to separate the color. When Color Cue Expression=numeric , this setting is defined in this format: <value1>,<value2>\|<color1>; Where <value1> and <value2> define a range. If you define an overlapping range, no colors will be used in this column. The Color Filter setting must be set to true if you include this setting. Cannot be used on columns where Column Type=ProgramHTMLOutput. | Gate\|yellow,Milestone\|orange,Phase\|blue,Project Space\|gray |
| Color Cue Label | A semicolon separated list of labels to use in the refinement pane for numeric, regex, and date expression types. You should define the same number of labels as colors defined in the cue expression. | Small Part;Medium Part;Large Part |
| Color Filter | Enables the visual cue (color square) for the column. Only applies if the Auto Filter = true setting is also defined for the column. By default, the visual cues support these 24 standard colors: blue green yellow red orange violet vermilion light-blue light-green light-yellow pink light-orange light-violet dark-blue dark-green light-brown maroon brown dark-violet dark-gray gray light-gray light-cyan bright-green Only the colors shown in bold can be discerned by color blind people. If you want to control which colors are used for specific values, set values for the Color Cue Expression Type and Color Cue Expression settings. | true false |
| Colors | Defines the color of the current state. Define this setting along with the States setting described in this table. | Ff7f00,009c00 |
| Column Icon | Use to display an icon for the column's data instead of other data. Required when the setting "Column Type" is assigned to "icon". Also see Column Values as Icons . Not supported for non-object based structure browsers. | Name of an image file such as: images/NewWindow.gif images/EditItem.gif |
| Column Type | The setting "Column Type" is used when no expression is defined for the column data. For recommendations on improving the performance of table columns whose data is generated with a JPO program (Column Type set to program or programHTMLOutput), see Improving Performance of Columns . If using programHTMLOutput to populate a column with an image or icon, make sure to include a height attribute to prevent column/row misalignments, for example: <img src="images/iconSmallDefault.gif" border="0" height="16"></img> For non-object based structure browsers, only these column types can be used: program progarmHTMLOutput separator | checkbox--Used when the column values are check boxes shown dimmed or not-dimmed based on the access to the object in that row. This access can be based on business logic and defined in a JPO or it can be role-based and defined by assigning roles to the column. If the check boxes have no access restrictions, this setting is not required and you can create check boxes by passing in the parameter selection=multiple to emxIndentedTable.jsp. Also see Check Box . file--Shows which hyperlinks to a Quick File Access page listing files checked into or connected to the object. The optional Relationship Filter setting defines how to locate related files. icon--Used when the column values are shown as an icon. The setting "Column Icon" must be defined with the icon to be displayed. For more information on configuring a column using a program, see Column Values as Icons . image--Used when the column values are images. When used, the column shows the small version of the primary image associated with the business object. program--The values for this column are obtained from a program (JPO). With this setting, the program and function name are required as settings. For more information on configuring a column using a program, see Column Values as Select Expressions . programHTMLOutput--Same as the "program" setting above, except the column value output is in HTML format. Column values are placed in the table cell between <td> and </td> tags. This setting ignores other column settings such as Show Type Icon, href, format, and Alternate OID expression. separator--Used to define a column of white space between standard data columns. A separator is especially useful to separate two groups of columns. |
| Comparable | When the structure browser includes a structure compare feature, this setting allows you to include (true), or exclude (false) the column from the list of columns a user can select to Compare By. By default, all columns are comparable except for Column Types of: Image Icon Separator | false true (default) |
| Compare Report | When the structure browser includes a structure compare feature, this setting allows you to show or hide the column from the report results. By default, all columns are comparable except for Column Types of: Image Icon Separator | hide show (default) |
| Complete State | Define this setting to view the icon Set to Final State . The value of this setting specifies the final state. | Release |
| Counter Link | Defines the URL or command that executes when you click the Counter link. For example, for URL: emxTree.jsp?DefaultCategory=IssueReportedAgainstTreeCategory For example, for command: IssueReportedAgainstTreeCategory | URL or command By default, the setting opens the Object Details page. |
| Currency Converter | Specifies that the currency shown in the column needs to be converted when the Currency Converter runs. When this setting is true for any column in a table, the table page automatically includes the Currency Converter tool. If this setting is not included, the value is assumed to be false and the target page does not display the Currency Converter tool. | true false (default) |
| Currency Expression | The name of the currency to convert from. Required for columns whose values are monetary and that can be converted from one currency to another using the Conversion tool and defined exchange rates. The value should be a select clause that returns the value for the Currency attribute for a specific object or relationship, or the actual value. The currency to be converted from should be the currency in which the user entered the data in (the As Entered currency). | For example, to convert currency data for an RFQ Quotation, the following select clause returns the supplier's currency format. to[Supplier Line Item].attribute[Currency] |
| Decimal Format | Defines the decimal precision (number of digits after the decimal point) for displaying values in numeric columns. | 5 2 |
| Decimal Precision | Defines the decimal precision for all calculations for this column. The system properties setting emxFramework.SBTableCalculations.DecimalPrecision defines the system-wide value; this setting overrides that value. You only need to use this setting if you want to use a value other than the system-wide value (defined in emxSystem.properties). | Any positive integer. |
| Default Function | Specifies a JPO used to populate the default value when using the add existing row functionality. | JPO name |
| Default Program | Specifies the method within the JPO specified by the Default Program setting. | method name in the JPO |
| Diff Code | When multiple columns in a structure compare Complete Summary Report have different values, this setting defines what to show in the Diff Code column. The code value for the column with the lowest precedence shows in the Diff Code column. Multiple columns can have the same precedence, and if both columns have different values, both code values show in the Diff Code column. | <precedence:code value> 1:Attribute |
| Directions | The Drag Types, Drop Types, Relationships, and the Direction settings are all interrelated. You can drop the object of the first type defined in the Drag Types setting on the object of the first type in the Drop Types setting. The relationship between these two objects is the first entry of the Relationships setting. The direction for the relationship is the first entry of the Directions setting. The same interrelationship applies to the second entry and so forth. | from,from,from |
| Display Format | Specifies the date format for any column where the value of the format setting is "date." See Date/Time Fields in Forms and Tables . Not supported for non-object based structure browsers. | These are Java standard values of Date Format to display a date in a specific format. 3 - SHORT (12/12/52) 2 - MEDIUM (Dec 12, 1952) 1 - LONG (December 12, 1952) 0 - FULL (Tuesday, December 12 1952 AD) Set the default in emxSystem.properties: emxFramework.DateTime.DisplayFormat=MEDIUM. emxSystem.properties uses words, but the Display Format setting uses numbers. |
| Display Time | Controls whether the time is displayed along with the date for columns whose format is set to date. If no time zone preference is set, then the DateTime is shown in the browser's time zone. The time is shown in terms of GMT+/- hh:mm, (e.g., Saturday, August 21, 2004 12:45:00 PM GMT-04:00). To get the time in a format like EST or PDT, set the time zone preference to a specific zone. See Date/Time Fields in Forms and Tables . Not supported for non-object based structure browsers. | true false Default is set in emxSystem.properties for the property emxFramework.DateTime.DisplayTime = false |
| Display View | When the column has the setting Display View=thumbnail defined, the data in this column displays beneath the thumbnail when the structure browser is shown in Thumbnail View. The number of lines of text shown beneath the thumbnail is controlled by the emxFramework.ThumbnailView.fieldcount property in emxSystem.properties. If the number of columns with Display View=thumbnail defined exceeds 3, then only the data for the first 3 columns with the Display View=thumbnail setting are shown beneath the thumbnail. If the number of columns with the setting is less than the property's value, then only those columns defined with the setting are shown. If this setting is not defined, then the structure browser displays the first 3 columns in the thumbnail view. | thumbnail |
| displayMode | Defines in which mode, Edit or View or Both, that this column is visible. When used, the visibility of the column is defined by the current mode and the value of this setting instead of an Access Expression. For example, if the structure browser is in view mode and this setting is Edit, then the column is not visible. | Edit View Both (default) |
| DocumentDrop Relationship | For a Drop Zone, specify the relationship you want to use to create a document. If you do not specify a relationship, then Reference Document is the default relationship. | relationship_VaultedDocumentsRev2 (relationship_ReferenceDocument) Meeting Attachment (Reference Document) |
| Drag Types | The Drag Types, Drop Types, Relationships, and the Direction settings are all interrelated. You can drop the object of the first type defined in the Drag Types setting on the object of the first type in the Drop Types setting. The relationship between these two objects is the first entry of the Relationships setting. The direction for the relationship is the first entry of the Directions setting. The same interrelationship applies to the second entry and so forth. | type_ProjectVault, type_DOCUMENTS,type_ProjectVault |
| Draggable | Specifies that the item in this column can be dragged to another location within the structure browser. Default is false, except for Name fields. By default, all Name fields are draggable. For a structure browser in a window shade (such as search results), the window shade fades out while the object is dragged. | true false (default) |
| Drop Actions | Specifies the drag-and-drop functions allowed on this column. Default is Copy. | Copy,Move Copy (default) Move |
| Drop Directions | When connecting a dropped item to its parent (using a relationship specified by the Drop Relationships setting), specifies the end of the relationship used for the dropped item. | from to |
| Drop Items | Indicates if one item or multiple items can be dropped into this column in a single operation. Default is Single. | Single Multiple |
| Drop JPO | Defines a JPOName:methodName used to perform the required action, if that action is anything other than connecting the dragged and dropped on items. |  |
| Drop Relationships | Comma-separated list to specify the relationships used to connect a dropped item to the context item. You can use the symbolic or actual name of the relationship. To define any other action than connecting the dragged and the dropped on item, use the Drop JPO setting. | relationship_EBOM |
| Drop Types | Comma-separated list to specify the object types that can be dropped into this column. You can use the symbolic or actual name of the object type. | type_Part,Document |
| Droppable | Specifies that objects can be dropped into this column. | true false (default) |
| Dynamic URL | When enabled, users can enter URLs or mxLink values and the values will display and function as hyperlinks. | enable (default) disable |
| Edit Access Function | Defines the method in the JPO specified by the Edit Access Program setting that determines access for each cell in the column. The method returns a StringList of boolean values for each cell (true/false) defining the edit access to that cell. | JPO method name |
| Edit Access Mask | Controls cell-level access in an editable table. The access check is done on all objects in one database call along with getting the column values. If any cell does not have the specified access, then the cell displays as read only. | Any access mask, for example: modify, connect |
| Edit Access Program | Defines the JPO to invoke that will determine whether the context user has edit access to individual cells in the column. | JPO name |
| Editable | Use to indicate whether the column is displayed as editable or read only. Only applies for Edit mode. View mode ignores the setting. | true--Users can edit the column data when shown on the Edit mode. false (default)--Users cannot edit the column data when shown in the Edit mode. The column looks just like it does in View mode except it is never hyperlinked. |
| Effective Date Expression | Use for columns whose values are monetary and that can be converted from one currency to another using defined exchange rates. Set the value to a select clause with a value for the Effectivity Date attribute on a specific object or relationship. The system uses this date to get the currency conversion whose rate period falls within this date. If this setting is not added, the current date is used. | For example, for currency data for an RFQ Quotation, the following select clause returns the effectivity date. to[Supplier Line Item].attribute[Effectivity Date] |
| Export | Specifies whether column data is exported. Use this to change the export value on a column-by-column basis. By default, all column types except programHTMLOutput are exported. If you want to additionally include programHTMLOutput, or exclude other column types from the export, change this setting. See Column Values as Program Output . | true false |
| format | Specifies the display format. If you set the format to numeric , you can also set the Number Format setting to true . That setting allows the value to be displayed using the thousands and decimal separators based on the user's browser locale. If at least one column has the format set to currency or UOM, the Conversion tool displays in the table page's page toolbar. When a user clicks the tool, the system opens a new window and displays all column data defined with format=currency and UOM to the currency and unit of measure selected in preferences. For information on the currency and unit of measure preferences, see About Unit of Measure Conversions and About Currency Conversions . Not supported for non-object based structure browsers. | date--Displays the column values as a date. Uses the tag lib "emxUtil:lzDate" to format the display. currency--Displays the column values as currency. UOM--Displays the column values as Unit of Measure and enables the Unit of Measure conversion interface. email--Displays the column values as an email address. When a user clicks the email address, the email editor configured in the client is presented. numeric--Displays the column values as numbers. To perform calculations or graphically analyze the data on a column of string attributes that have numerical values, numeric must be the column type user--Displays the user's name in the format "Lastname, Firstname". |
| function | The name of the method to call within the JPO program specified in the program setting. This method within the JPO is used to get the column values if the setting "Column Type" is set to "program" or "programHTMLOutput" or "checkbox". See Column Values as Program Output . | getAssignedBuyerDesk getPackageAccess getParentPart getCurrentState columnSubscriptionLink |
| Group Header | Defines header text to display over several consecutive columns. For example, if you want a group header over three consecutive columns, add this setting to each column and assign the same value for each. You may want to separate grouped columns using a separator column, which is just a column of white space. Add a separator using Column Type=Separator. To see an example, see Grouped Columns and Column Separator . | Static text or string resource id. |
| Group Name | Identifier used to collect columns into groups. All columns with the same Group Name will be grouped together. Used for merging cells as defined in Merged Cells . | Text String |
| Help Marker | Specifies the name of the help marker to call for context-sensitive help. In the href URL called when the column data is clicked, the system passes a parameter called HelpMarker and includes the marker text specified for this setting. | The naming convention for help markers "emxhelp" followed by the object or feature and then the action, for example, emxhelproutecreate and emxhelpprojectedit. The marker is all lowercase with no spaces. |
| Hide Mode | A comma-separated list indicating that if the user is accessing the system via that mode, then this column will be hidden. Value Hide column if access mode is: Show column if access mode is: Desktop Desktop Cloud or Mobile Mobile Mobile Desktop or Cloud Cloud Cloud Desktop or Mobile !Desktop Mobile or Cloud Desktop !Mobile Desktop or Cloud Mobile !Cloud Mobile or Desktop Cloud If this setting is not defined, then the column is always visible as long as the user has access to it contextually. | Desktop Mobile,Cloud !Mobile |
| Value | Hide column if access mode is: | Show column if access mode is: |
| Desktop | Desktop | Cloud or Mobile |
| Mobile | Mobile | Desktop or Cloud |
| Cloud | Cloud | Desktop or Mobile |
| !Desktop | Mobile or Cloud | Desktop |
| !Mobile | Desktop or Cloud | Mobile |
| !Cloud | Mobile or Desktop | Cloud |
| Image Size | For columns defined as Column type = Image , determines which which size of the primary image associated with the business object should display in the column and must use the symbolic name. The pixel dimensions for these sizes are defined using the emxComponents.Image. Size ImageSize property (see Collaboration and Approvals Administration Guide : Properties for Configuring the Image Manager ) where Size is Small, Medium, Large, or Thumbnail. When the context object is a Document, use Image Size = format_mxMediumImage . | format_mxSmallImage (default) format_mxMediumImage format_mxLargeImage format_mxThumbnailImage |
| Input Control Direction | Only used for structure browsers in Edit mode for columns that have an Input Type set to radiobutton or checkbox. Determines whether the field values display in a list (vertical) or on a single line (horizontal). | vertical (default) horizontal |
| Input Type | Only used for tables in Edit mode. Specifies the type of HTML control to display for user input. You can also designate the size of the input boxes to allow for appropriate spacing. This is important for short numeric entry fields. For fields that allow multiple selections, use checkbox instead of listbox (unless you implement a custom JPO). Multiple values are saved as a comma-separated list. | textbox--This is the default. The column has a single-line box for typing text. textarea--The column has a multi-line box for typing text. radiobutton--Shows a radio button next to each range value checkbox--Shows a checkbox next to each range value. listbox--Provides a list of range values and users can select a single value. Users can select multiple values, but only one value is saved. If you implement a custom JPO, users can select multiple values and those values are saved as a comma-separated list. listboxmanual--Provides a list of range values and users can select a single value as with the listbox input type. In addition the user can manually type in a value. combobox--The column has a drop-down list of options and users can only select one value. Use combo boxes for attributes that have defined ranges and for attributes whose ranges are determined with a Range Helper URL. |
| Level | Define the values to show in the Expand filter combo box. The values for this setting can be: comma-separated list of positive integers All Specify Text to be processed by the expandProgram JPO. You cannot use custom levels if the structure browser is filtered using relationship/type/direction. The All and Specify reserved keywords control specific levels of expansion. Use the Label setting to define the string resource or static text for the label that shows in front of the expand combo box; use the Registered Suite setting to define the application that defines the menu for the expand filter or the expand Program JPO. | 1,2,3,4,5 All Specify... Custom expand level (for example, End Item) |
| Link More | Defines the URL or command that executes when you click the More link. More links appear if the total number of objects are more than the number of objects defined in Max Items. For example, for URL: emxTree.jsp?DefaultCategory=IssueReferenceDocumentsTreeCategory For example, for command: IssueReferenceDocumentsTreeCategory | URL or command By default, this setting opens the Object Details page. |
| Lookup Input Type | Defines the input control used with the add existing row functionality when the structure browser is in edit mode. | textbox (default) combobox radiobutton listbox listboxmanual |
| Mass Update | Used only for structure browsers in Edit mode. If set to false for the column, the column is not available to the Mass Update feature, but can be edited using the inline editing (one cell at a time). If set to true, the column is used by the Mass Update feature. | true false |
| Max items | Number of Document links, that appear for downloading. | 5 (10) |
| Mouse Over Popup | Enables or disables the mouse over popup DIV that displays the entire contents of the cell. When enabled, the popup shows in both View and Edit mode. | enable disable (default) |
| Nowrap | If the setting is true for any column, then the text in that column will never wrap. The default is false. When wrapping is used, the text is wrapped at a convenient space between words, or in the middle of a text string (for long strings that exceed the column width). | true false |
| Number Format | This setting only affects how numbers are displayed. Numbers being entered or edited do not show any formatting. When true, values are displayed using the format for the thousands and decimal separators based on the user's browser locale. When false, values are displayed as they are stored in the database, without any formatting. This setting only applies to numeric attributes if the format setting is defined as numeric . | true false (default) |
| On Change Handler | The name of the JavaScript function called when the value in the cell is changed. You can provide a semicolon separated list of JavaScript functions. | <JavaScript function name> |
| OnFocus Handler | The name of the JavaScript function called when a cell is selected for edit. You can provide a semicolon separated list of JavaScript functions. | <JavaScript function name> |
| Percentage Format | When set to true , the % symbol shows after the number in view mode. No symbol shows in edit mode. When set to false or the setting is not defined, the % does not show. This setting applies to columns defined as numeric using one of these methods: String attribute with format=numeric Numeric attribute This setting is ignored if added to any other type of column. | true false (default) |
| Popup Modal | If the setting Target Location is set to popup, the window can be configured as modal or non-modal. | true--the popup window is modal false--the popup window is non-modal If the setting is not specified, the window is non-modal. |
| Popup Size | Defines the size of the popup window that opens a list of Documents. The pixel width and height values for these sizes are defined in emxSystem.properties. See Collaboration and Approvals Administration Guide : Properties for Configuring Popup Windows . | Small Medium Large SmallTall MediumTall |
| Preserve Spaces setting | Use to preserve the spaces and tabs for the data in a column. For ProgramHTMLOutput columns that return HTML elements, you can do either of the following tasks: Use the JPO function to add the verbatim class to the immediate parent td. Add the Preserve Spaces setting to the Column. Be sure, however, that there are no spaces between the html elements to prevent any layout issues. | true |
| Printer Friendly | Passes the PrinterFriendly parameter and the entered value to the JSP specified in the column's href URL. JSPs that use the PrinterFriendly parameter show the Printer Friendly tool when the setting is True and hide it when False. If the setting is not included, the tool shows by default. Users can click the tool to get a version of the current page that can be printed with the browser's Print button. You can also specify the PrinterFriendly parameter in the href URL for JSPs that use it. | true (default) false |
| program |  | emxGenericColumns |
| Range Function | Use to specify the name of the method in the JPO specified in the Range Program setting. See the Range Program setting below for more information. | The name of a function in the Range Program JPO, such as: getAssignedRange getClassificationRange getPartUOM |
| Range Program | Use to specify the name of a JPO that contains a method to get the column value ranges (choices). A range program is used only when the Input Type setting is combobox or popup. The corresponding method which runs to get the values of the column should not have any HTML code. | The name of a JPO, such as: SCSBuyerDeskForm TMCPackagesForm ENCPartForm AEFUtilForm |
| *Registered Suite | The application the column belongs to. The system looks for files related to the column in the registered directory for that application, which is specified in emxSystem.properties. Based on the application name, the system passes the following parameters in the href URL: suiteKey emxSuiteDirectory StringResourceFileId | Use the Components setting to define a status column for a structure browser page. You must set the value without spaces, for example, EngineeringCentral or Framework. The value must be the suite name as defined in the key eServiceSuites.DisplayedSuites within emxSystem.properties. If the suite name starts with "eServiceSuite" then you can skip and just assign the remaining text. For example, if the suite name in emxSystem.properties is "eServiceSuite EngineeringCentral ", then the word " EngineeringCentral ", can be assigned as "Registered Suite". In the href URL called when a user clicks the column data, the system passes a parameter called "suiteKey". The value for the parameter is the property name from emxSystem.properties that maps to the setting's value. |
| Relationships | The Drag Types, Drop Types, Relationships, and the Direction settings are all interrelated. You can drop the object of the first type defined in the Drag Types setting on the object of the first type in the Drop Types setting. The relationship between these two objects is the first entry of the Relationships setting. The direction for the relationship is the first entry of the Directions setting. The same interrelationship applies to the second entry and so forth. | relationship_SubVaults, relationship_VaultedDocumentsRev2,relationship_ProjectVaults |
| Reload Function | The name of the method in the JPO specified by the Reload Program setting that executes a cell reload. You also need to specify a value for the Reload Program setting. | <JPO method name> |
| Reload Program | The name of a JPO to invoke when this code in the structure browser is called: emxReloadCell(<columnName>) You also need to specify a value for the Reload Function setting. | <JPO Name> |
| Required | When creating or editing the value in the column, this setting defines if a value must be entered or is optional. If true, a value must be entered; if false, the value is optional. Some apps might include properties that define whether an attribute is required for an object. If so, the true/false value for the column for that attribute should match the true/false value for the property. | true false |
| RMB Menu | Specifies the name of the right-click menu to associate with the component. For a type-specific menu, such as type_Part, defines the type-specific right-click menu for that type. For a table column, specifies the right-click menu for that specific column. | <admin menu name> |
| Root Label | Determines the label for the root node(s). You can use static text, such as Search Results, or a string resource id. When using a string resource id, you can use any valid MQL expression, such as: emxFramework.Common.RootLabel= $<type>$<name>:Root If the expression does not apply to the root object (such as an attribute not associated with that object), the expression is ignored and the default label is used. If this setting is configured for a column not in the freeze pane, it is ignored. | Search Results emxFramework.Common.RootLabel |
| Row Grouping | Enables or disables the column from being selected for row grouping. By default, all columns are enabled for grouping except file columns ( Column Type = File setting) and hard-coded icon columns. | true (default)--column can be used for grouping rows false--column can not be used for grouping roles |
| Row Grouping Range Function | Method in the JPO defined by the Row Grouping Range Program setting. The page will be grouped by the pre-defined row categories even if no rows in the structure browser have that value. |  |
| Row Grouping Range Program | JPO used to provide pre-defined row categories to group the data. |  |
| RowGroupCalculation | Specifies a mathematic function or expression to use to add a calculation to the header row for a group. For more information, see About Calculations for Grouped Rows . | SUM AVE COUNT MAX MIN MEDIAN STDDEV |
| Row Number | If the column is defined with a Group Name setting, this setting defines whether this column displays in the first or second row of the group. Only 2 rows are permitted for a group. Used for merging cells as defined in Merged Cells . | 1 (default) 2 |
| Row Span | If this column is defined with a Group Name setting, defines how many rows each cell in this column will span. If set to 1, no spanning is done. If set to 2, the column value spans 2 rows. You cannot span more than 2 rows. Used for merging cells as defined in Merged Cells . | 1 2 (default) |
| Show Alternate Icon | If the column value to display with the icon is different from the current row's object icon, set this to true. To get the right alternate icon, define the Alternate Type expression with the expression to obtain the object type. For more information, see Column Values Using Alternate OID in href and Select Expression . Not supported for non-object based structure browsers. | true false |
| Show Clear Button | Uses with a textbox input control configured with a chooser. True shows the Clear button to allow the user to erase the value without choosing a new value. | true false |
| Show Counter | Shows or hides the counter. The Counter setting defines the total number of related Items that appear in the counter. | True(true) |
| Show Drop Zone | Shows or hides the drop zone. | True (false) |
| Show Icons | Shows or hides object icons. If the setting is false, then the download link does not show. | True (false) |
| Show Link | Shows or hides the Current State as a link. | True (true) |
| Show Next | Shows or hides the Next State label. | True False (false) (default) |
| Show Previous | Shows or hides the Previous State label. | True (false) |
| Show Table | You must define this setting as true for the tabular view inside the column. | True (false) |
| Show Type Icon | If true, the column value displays with the object type icon as defined in the emxFramework.smallIcon property in emxSystem.properties. The icon displays to the left of the column data. If no property is defined for the type's icon, the system looks for a property defined for the parent type, then grandparent type and so on. If no property is defined for any type in the hierarchy, the system uses the default icon specified in the emxFramework.smallIcon.defaultType property. Not supported for non-object based structure browsers. | true false (default) |
| Sortable | Controls whether users can sort the table based on data in the table column. If a column is sortable, users can click the column heading to sort the table based on that column. | true (default) false |
| Sort Direction | Used only for fields of type combobox and listbox. The sort order for the option list. In edit mode, an integer attribute with range values always lists the values in ascending order, even if this setting is set to descending. Javascript overrides this setting and always uses ascending order. | ascending (default)--Sort a to z or 0 to n. descending--Sort z to a or n to 0. none--The option list is not sorted. |
| Sort Key | Defines the attribute for sorting. | originated--Defines the attribute for sorting. |
| Sort Range Values | Enables or disables the sorting of range values in combobox or listbox controls. When enabled, the list is sorted based on the datatype and using the direction defined by the Sort Direction setting. When disabled, no sorting is done on the list. In a drop-down, range values populated based on an attribute expression will be sorted based on the attribute's datatype. Range values populated using the Range Program and Range Function settings will be sorted alphanumerically. If the Range Program or Range Function returns numeric or date values, the alphanumeric sort will not be appropriate. The program or function should sort the values in the required order, and this setting should be disabled. This setting does not apply to fields or columns configured for attributes associated with a dimension. Dimension ranges are always sorted as alphanumeric in ascending order. This setting cannot be used in custom JSPs that use the editOptionList taglib. For this specific situation, you can use the sortType attribute for that tag. | enable (default disable |
| Sort Type | Determines how the column is sorted. The MQL subclause must also be included. sorttype Sorttype can be used on its own to sort numeric columns ( sorttype = numeric ) Or sorttype can be used in conjunction with the Sort Type setting. Two examples: ( sorttype = other AND Sort Type = integer) ( sorttype = other AND Sort Type = real ) | date integer real AlphaNumericLarger--consider all numeric values as larger than the alphanumeric values AlphaNumericSmaller--consider all numeric values as smaller than the alphanumeric values |
| States | Defines the current state in the color set by the Colors setting. Define this setting along with the Colors setting described in this table. | In Work, Release |
| Style Column | Customizes the style for a column. The value must be a class defined in the dsecUITypeCustom.css. For the example style, the definition in the css file could be: ColumnBackGroundColor { background-color : rgb(252,186, 186); } To define alignment, use one of these values: left-align center-align right-align For numeric fields, the default is left-align. | ColumnBackGroundColor |
| Style Function | The method in the Style Program JPO that defines the style per cell in the column. | The name of a function in the Style Program JPO, such as: getStyleInfo |
| Style Program | The JPO that contains methods to defines the style per cell in the column. | The name of a JPO, such as: AEFUtilStyleJPO |
| Table Columns | Defines the name of columns, that appears in the tabular view. 3 columns appear by default: typeIcon, Type, Name Click typeIcon to open the object details page in a popup Window. | Icon,name,attribute[Title] |
| Target Location | Controls where the page specified in the href parameter appears or is targeted. | content--The page replaces the content frame. popup--Page appears in new window. Set the window modality with the Popup Modal setting. _top--Page replaces the entire body of the browser window. listHidden--A hidden frame within the table frameset. Use this frame for Target Location for any processing in the context of a table. hiddenFrame--The frame called hiddenFrame is part of the top level Navigation window frameset and can be used to submit the Table frame and carry out background processing. This frame is not available for popup windows though so listHidden is a better frame to use. Set this value to any valid frame name that h is available to the table body frame. |
| Type Ahead Mapping | When Type Ahead is configured on the column (by setting the RangeHref to emxFullSearch.jsp or using a predefined Type Ahead Chooser), this setting maps that field to the corresponding indexed field in config.xml. See About Automatic Type Ahead . | NAME LASTNAME,FIRSTNAME,USERNAME |
| Type Ahead Validate | When Type Ahead is configured on the column, this setting determines if the application should restrict the user to selecting one of the suggested values (if true), or if the user can enter any data in the field without Collaboration and Approvals validating that data (if false). | true false (default) |
| Type Icon Function | Specifies the method in the JPO specified in the Type Icon Program setting that retrieves an icon to show in addition to the Alternate Icon or Type Icon (enabled by Show Alternate Icon or Show Type Icon settings). If both the Show Alternate Icon and the Show Type Icon settings are set to false, any value for this setting is ignored. | Method Name |
| Type Icon Program | Defines the program that contains the function specified using the Type Icon Function setting. | JPO Name |
| UOM Expression | Specifies the name of the Unit of Measure to convert from. Required for columns whose values are measurements that can be converted from English units to Metric units or vice versa. The value can be a select clause that returns the value for the Unit of Measure attribute for a specific object or relationship or the actual value. The unit of measure to convert from should be the unit of measure that user entered (the As Entered unit of measure). | For example, for measurement data for RFQ Quotations, the following select clause returns the supplier's unit of measure format. to[Supplier Line Item].attribute[Unit of Measure] |
| Update Function | Specifies a method name in the JPO given in the Update Program setting. | The name of a function in the Update Program JPO, such as: setAssignedBuyerDesk setPackage Access setPartClassification |
| Update Program | Specifies a JPO that contains a method to set the column value when the column is displayed on an Edit mode table and when the user clicks Done. This program is used only when the Field Type setting is program or programHTMLOutput. Using an update program is recommended only when the Field Type is not attribute or basic. | The name of a JPO, such as: SCSBuyerDeskForm TMCPackagesForm ENCPartForm AEFUtilForm |
| Validate | Specifies the method execute for validating any cell. The file containing this method must be specified in the emxAPPNAME.properties file. OR To prevent an incorrect drop operation, apps can configure the Validate setting as needed. The value of this setting can be a Javscript function, which executes when the user drops an object on the drop zone. This function must return a JSON object with the following key-value pair: Key : "status" Value: "success" or "error" Key : "error" Value: "error message" If the value of the status key is success, then the drop operation is processed. If the value of the status key is error, then an alert message appears and the drop operation is not processed. The message string shown in the alert, is the value of the error key. | The name of a method, such as: checkUniqueName OR Javascript function name, such as: isValidDnDOperation |
| Validate Type | Validates any field value with any specific characters defined in emxSystem.properties. | Basic or Restricted--The field values are validated against the value of emxFramework.Javascript.BadChars. Name--The field values are validated against the value of emxFramework.Javascript.NameBadChars. |
| Width | Number of pixels to define the column width. When not defined the width is calculated based on the header text length so that the header text is not truncated. | <column width in pixels> 30 (default) 150 |
| *Required Setting |  |  |

<a id="toolbar-setting"></a>
## Settings for Toolbar Menu Objects

来源：`Toolbar_Setting.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This table describes the available settings for menu objects that represent drop-down toolbar menus. There are no settings for the top-level menu. The name and value for each setting are case sensitive.

| Setting | Description | Accepted Values/Examples |
| --- | --- | --- |
| Access Behavior | This setting works with the results of the Access Expression, Access Mask, or Access Program/Access Function settings. Those settings return a true or false value. When true, the menu command shows in the list and can be selected by the user. When false, this setting defines whether a menu command is hidden from the user (hide; does not display in the menu) or dimmed (disable) so that it cannot be selected. This setting does not work with Role accesses. Commands not available to a user based on role are always hidden, regardless of the value of this setting. | hide (default) disable |
| Access Expression | Controls access to the menu based on a valid expression. The system evaluates the expression at runtime. If the expression evaluates to True and no other access control prevents access, the component is shown. Depending on the type of expression defined, the program may or may not need a valid objectID. Make sure that an objectID is available before configuring this setting for configurable toolbar menus and commands. When you use the setting for commands connected to an app category menu or the Actions menu on the top bar, the system evaluates the expression on the person business object. It does not rely on any objectId to be passed in as a URL parameter. In cases where no objectId is passed, such as a table page originating from an app category menu, the Access Mask setting is ignored. Similarly, the IconMail tree, which does not have an objectId, does not use this setting if defined. | $<attribute[attribute_Weight].value>?> 100 $<attribute[attribute_Originator].value >== owner |
| Access Function | The name of the JPO method to invoke in the JPO specified for the Access Program setting. The Access function gets the input parameter as a HashMap that contains all the request parameters passed into the JSP page. The JPO method must return a Boolean object. If the returned value is true and no other access control prevents access, the component displays. If false, it is hidden. To see a sample JPO method to control access, see Sample JPO for Controlling Access . Use this setting to evaluate access independent of users' roles. | The name of an access check method in the JPO specified in the Access Program setting, such as: emxAccessCheck() |
| Access Mask | Specifies the accesses the user must have for the current business object for the menu to display. When you use the setting for commands in page toolbars, menus, trees, table columns, and form fields, the system evaluates the access mask on a specific business object. This business object is available only when the objectId is passed in as a URL parameter to the JSP. If the user does not have all the specified accesses in the business object's policy for the current state, the system hides the component. If the user has the access and no other access control prevents access, the system displays the component. You can specify multiple accesses by separating the accesses with a comma. | Any set of accesses, separated by a comma. For example: Modify Delete ToConnect ToDisconnect FromConnect FromDisconnect Modify,Delete FromConnect,FromDisconnect |
| Access Program | Controls access to the menu based on the output from a method in the specified JPO program. You must define the program in MQL . This setting requires that you also specify the Access Function setting. If Access Function is not set, the system ignores the Access Program setting. The following input values are required for the program: A list of all of the request parameters in a HashMap Method name as a string JPO Program Name as a string Context The output must be a Boolean. | Name of a JPO defined as a program object, such as: emxAEFCollectionAccess |
| Dynamic Command Function | Defines the method in the JPO specified by the Dynamic Command Program setting that returns a list containing the data structure to build the right-click menu. In general, the list contains dynamic options based on the user context. This setting is only used with the right-click menu component and the toolbar component. | <JPO Method Name> |
| Dynamic Command Program | Defines the JPO invoked from a right-click menu component, or toolbar menu component. | <JPO Name> |
| Hide Mode | A comma-separated list indicating that if the user is accessing the system via that mode, then this menu will be hidden. Value Hide toolbar if access mode is: Show toolbar if access mode is: Desktop Desktop Cloud or Mobile Mobile Mobile Desktop or Cloud Cloud Cloud Desktop or Mobile !Desktop Mobile or Cloud Desktop !Mobile Desktop or Cloud Mobile !Cloud Mobile or Desktop Cloud If this setting is not defined, then the toolbar is always visible as long as the user has access to it contextually. This setting does not apply in these cases: global toolbar menu specified by the tableMenu URL parameter passed to emxIndentedTable.jsp menu for a tree command menu for right mouse button (right-click menu) | Desktop Mobile,Cloud !Mobile |
| Value | Hide toolbar if access mode is: | Show toolbar if access mode is: |
| Desktop | Desktop | Cloud or Mobile |
| Mobile | Mobile | Desktop or Cloud |
| Cloud | Cloud | Desktop or Mobile |
| !Desktop | Mobile or Cloud | Desktop |
| !Mobile | Desktop or Cloud | Mobile |
| !Cloud | Mobile or Desktop | Cloud |
| Image | Specifies the image used for the menu. This is optional if you have defined a label. Required if there is no label. | ${COMMON_DIR}/iconSmallOrganization.gif |
| ImageDnDJPO | Used to add custom image drag-and-drop functionality to the menu for an object type. These settings define the JPO and method that execute custom actions when an image file is dropped onto the target for a context object. The JPO and method must also be developed. For more information, see Java Program Objects . | <JPO Name> |
| ImageDnDMethod | <JPO Method Name> |  |
| Image Manager Toolbar | Defines a default toolbar to use for the Image category for an object type if a specific toolbar is not defined. For Image pages that require access or other settings different from the default, you can define a custom toolbar with a custom image upload command for that page. | <Toolbar Name> APPCompanyImageManagerToolBar |
| Image Upload Command | Defines a default command used for uploading images when a custom command is not required. | <Command Name> APPCompanyUploadImage |
| Maximum Length | Define the greatest number of characters that can be displayed on a drop-down menu label. The Title property of the button is used as the Alt text to display the full label. | Any number of characters, such as 25 When not set, the toolbar displays the entire label. |
| Mode | Used for structure browser only. If defined for a toolbar command, that command is only enabled on the toolbar in the indicated mode (Edit or View). If not defined, the command is enabled in both Edit and View mode. If defined for a toolbar top-level menu, the entire toolbar only shows in the indicated mode and is hidden in the other mode. | edit view |
| Pull Right | Determines whether the drop-down links display at the same level as the menu or pulled right to display the links in the next level. Applies only to second-level drop-down menus. For examples, see About Toolbars . | True (default) False |
| *Registered Suite | Specifies the application the toolbar menu belongs to. The system looks for files related to the menu in the registered directory for that application (specified in emxSystem.properties). Based on the application name, the system passes the following parameters in the href URL: suiteKey SuiteDirectory StringResourceFileId | The value cannot contain any spaces. For example, EngineeringCentral or Framework. The value must be set to the suite name as defined in the key eServiceSuites.DisplayedSuites within emxSystem.properties. If the suite name starts with "eServiceSuite" then this prefix can be skipped and assign the remaining text to the setting. For example, if the suite name in emxSystem.properties is "eServiceSuite EngineeringCentral ", then the word " EngineeringCentral ", can be assigned as "Registered Suite". In the href URL that is called when the menu is clicked, the system passes a parameter called "suiteKey". The value for the parameter is the property name from emxSystem.properties that maps to the setting's value. |
| RMB Menu | Specifies the name of the right-click menu to associate with the component. For a type-specific menu, such as type_Part, defines the type-specific right-click menu for that type. For a table column, specifies the right-click menu for that specific column. | <admin menu name> |
| showAsSubMenu | Displays the commands for the menu as a submenu in the category list for an object that can be expanded and collapsed. If you do not use this setting, the commands within that menu show as individual categories for the object. You can only define one level of submenus for the category menu. | True False (default) |
| *Required |  |  |

<a id="urlparameters"></a>
## URL Parameters

来源：`URLParameters.html`

[返回索引](./ENOVIA_UI_Config_AI_Index.md)

This table lists the parameters that you can specify for configurable JSPs.

| Parameter | Description | Accepted Input Values |
| --- | --- | --- |
| Accepted by JSP |  |  |
| actionMenuName | The name of the menu. | Name of any menu object that represents a menu. The framework installs with a command object called AEFLifecycleMenu that includes a Promote and Demote link. |
| emxLifecycle.jsp |  |  |
| appendColumns | The name of the system table that contains defined columns that you want to include in the structure browser being defined. Any columns added using this parameter are not available to users for Custom Table Views. That is, the user cannot remove or reorder any of the columns added by this parameter. | Table name |
| emxIndentedTable.jsp |  |  |
| appendFields | The name of the web form that contains defined fields that you want to include in the form being defined. |  |
| emxForm.jsp, emxCreate.jsp |  |  |
| AppendParameters | Passes common parameters to the tree commands. To use the parameter, set it to tree and then include the parameters to pass. Parameters added to emxTree.jsp that are not with AppendParameters are passed only to the root tree node and not to commands. You can also use the Tree Scope ID setting for the tree menu object to pass the current tree's object ID to all commands and subtrees. | To make Param1 and Param2 available to the children commands: <a href="../common/emxTree.jsp?objectId=<%=sPartId%>&mode=insert&jsTreeID=<%= jsTreeID %>& AppendParameters=true&Param1=Value1&Param2=Value2 " class="object" target="content"><%=sPartName%></a> To make ProjectId and Bookmark Workspace available to the tree commands: <a href="../common/emxTree.jsp?objectId=<%=sPartId%>&mode=insert&jsTreeID=<%= jsTreeID%>& AppendParameters=true&ProjectId=<%=sProjId%>&Workspace=<%=sWorkspace%> " class="object" target="content"><%=sPartName%></a> To make ProjectId and Bookmark Workspace available only to the root tree node and not to the tree commands: <a href="../common/emxTree.jsp?objectId=<%=sPartId%>&mode=insert&jsTreeID=<%= jsTreeID %>&& ProjectId=<%=sProjId%>&Workspace=<%=sWorkspace%> " class="object" target="content"><%=sPartName%></a> |
| emxTree.jsp |  |  |
| applyURL | Specifies the URL string to be submitted when a user clicks the Apply button after making changes to the structure browser in edit mode. This parameter can also specify custom apply functionality as described in Customizing the Apply Action . | javascript:methodName |
| emxIndentedTable.jsp |  |  |
| autoFilter | Determines whether the filter tool displays in the toolbar, and overrides the Auto Filter setting on a structure browser column. | true (default)--The Filter tool displays in the page toolbar. false--The Filer tool does not display in the page toolbar. |
| emxIndentedTable.jsp |  |  |
| buffer | This parameter overrides the emxFramework.FrezePane.Buffer=true system property defined in emxSystem.properties. The default for this URL property is true : the structure browser starts to cache the data for all rows as the structure browser opens.a If you change the value to false , the structure browser does not automatically buffer the data when the page initially loads. It does, however, buffer the data when the user selects the vertical scroll bar. For large structures, buffer=true can take more time to fetch and render the complete structure. The percentage of buffering displays next to the object count during the buffering process. | true (default) false |
| emxIndentedTable.jsp |  |  |
| calculations | Controls the display of the toolbar button. When true, this icon only displays on the toolbar if the structure browser contains at least one column defined as numeric. | true--Sigma icon shows if the structure browser contains at least 1 numeric column false--Sigma icon does not show on the toolbar (default for structure browser) |
| emxIndentedTable.jsp |  |  |
| CancelButton | Displays the Cancel link in the footer frame. When a user clicks Cancel, the whole browser window gets closed. The Cancel link can only be used for a page that is opened in new window. The label for the Cancel link can be configured using the CancelLabel parameter. If CancelLabel is not passed in, the default label "Cancel" is used. | true--Shows the Cancel link. Use only for pages displayed in popup windows. false (Default)--Does not show the link. |
| emxFormEditDisplay.jsp |  |  |
| CancelLabel | Defines the label for the Cancel link, if you want something other than "Cancel". | Any static text or string resource id. CancelLabel=emxEngineeringCentral.Common.Cancel |
| emxIndentedTable.jsp, emxFormEditDisplay.jsp |  |  |
| cancelProcessJPO | The JPO to use when a user clicks the Cancel or close window button on a form component. | <JPOName>:<MethodName> |
| emxFormEdit.jsp emxIndentedTable.jsp |  |  |
| cancelProcessURL | The JSP called when a user clicks the Cancel button or closes the form page. Specify the JSP name using the macro for the page location, or use the relative path as listed in the examples. | ${SUITE_DIR}/emxCustomCancelProcess.jsp or ..<ApplicationDirectory>/emxCustomCancelProcess.jsp Example: ../engineeringcentral/emxCustomCancelProcess.jsp |
| emxFormEdit.jsp emxIndentedTable.jsp |  |  |
| cellDirection | Defines the relationship direction to traverse. Required when cellRelationship is used. | from to |
| emxGridTable.jsp |  |  |
| cellIdDirection | Defines the relationship direction to traverse. | from to |
| emxGridTable.jsp |  |  |
| cellLabel | Defines the value of the column cell representing the intersection of the expanded row to the column object. The value must be an attribute or selectable. If not defined, the cell is marked with an X to indicate intersection. | Assignee Role Percent+Allocation |
| emxGridTable.jsp |  |  |
| cellLabelType | Defines the type for the cellLabel. | RelAttr the cellLabel is an attribute on the relationship. TypeAttr the cellLabel is an attribute on the business object. RelBasic the cellLabel is a basic selectable on the relationship. Basic the cellLabel is a basic selectable or the business object. |
| emxGridTable.jsp |  |  |
| cellIdRelationship | Defines the rel-to-rel relationship when the cell value is connected to the relationship between the row and column. |  |
| emxGridTable.jsp |  |  |
| cellRangeJPO | Can be used to identify the cell range values in edit mode. This is standard structure browser functionality and the input/output should be based on the structure browser's definition and requirements. | JPO:method |
| emxGridTable.jsp |  |  |
| cellRelationship | Defines the relationship used to traverse the row data to intersect with the column objects. The data from this relationship becomes the columns unless colRelationship is specified. You can provide a comma-separated (no space) list of relationships. | Assigned Tasks |
| emxGridTable.jsp |  |  |
| cellRelationshipStyle | Used to specify the style defined in the CSS for cell coloring and formatting based on the relationship type. If the relationship specified by this parameter matches the cell relationship, then the style defined is used. Specified as: Relationship\|Display\|Style The specified Relationship must be one of the relationships specified by the cellRelationship parameter. Instead of a style, you can replace the cell value with a specified value. You can provide a comma-separated list of multiple value sets to define the style for different possible cell values. The Style can be a pre-defined style in the Indented Table CSS, or the style definition can be provided here. | EBOM\|XX\|Green\|MBOM\|ZZ\|Blue |
| emxGridTable.jsp |  |  |
| cellUpdateJPO | Can be used to modify cell entries in edit mode. | JPO:method |
| emxGridTable.jsp |  |  |
| cellValueStyle | Used to specify the style defined in the CSS for cell coloring and formatting based on the cell value. If the cell value matches the Value in the parameter value, then the indicated style is used. Specified as: Value\|Short Value\|Style For example, in the sample shown, if the cell value is Optional, then the cell is shown in Yellow. Instead of a style, you can replace the cell value with a specified value. You can provide a comma-separated list of multiple value sets to define the style for different possible cell values. The Style can be a pre-defined style in the Indented Table CSS, or the style definition can be provided here. | Standard\|S\|Green\|Optional\|O\|Yellow Standard\|S\| background-color: lightgreen; text-align:center; font-family: Arial; font-size: 11px; font-weight: bold; |
| emxGridTable.jsp |  |  |
| chartType | The type of the chart to be drawn. | BarChart StackBarChart PieChart LineChart |
| emxChart.jsp |  |  |
| chartTitle | The title for the chart. |  |
| emxChart.jsp |  |  |
| colDirection | Defines the relationship direction to traverse for the column objects. Required when colRelationship is used. | from to |
| emxGridTable.jsp |  |  |
| colGroup | Specifies the column group header. The value must be an attribute or selectable. | Project Members |
| emxGridTable.jsp |  |  |
| colGroupType | Defines the label type for the group header (the source of the field). The label can be hard coded or specified as a string resource. | RelAttr the colGroup is an attribute on the relationship. TypeAttr the colGroup is an attribute on the business object. RelBasic the colGroup is a basic selectable on the relationship. Basic the colGroup is a basic selectable or the business object. |
| emxGridTable.jsp |  |  |
| colHyperlinkTreey | Adds a hyperlink to the column object for launching the tree browser for that object. You can specify a command for the object, or the word Default (indicating the object's default category). | Default y Name |
| emxGridTable.jsp |  |  |
| colJPO | Can be used instead of URL parameters to identify column values. The JPO must return a MapList of column objects. | JPO:method |
| emxGridTable.jsp |  |  |
| colLabel | Specifies the label for the column header. By default, the object Name is used. The value must be an attribute or selectable. If you use this parameter, you must also specify the colLabelType parametr. | name revision |
| emxGridTable.jsp |  |  |
| colLabelType | Defines the label type for the column header (the source of the field). Required when colLabel is specified. | RelAttr the colLabel is an attribute on the relationship. TypeAttr the colLabel is an attribute on the business object. RelBasic the colLabel is a basic selectable on the relationship. Basic the colLabel is a basic selectable or the business object. |
| emxGridTable.jsp |  |  |
| colRelationship | Defines the relationship used to traverse the root objects to determine the column objects. You can provide a comma-separated list (no spaces) of relationships. If not specified, the column objects will be based on the cell objects defined by cellRelationship. | Division Member |
| emxGridTable.jsp |  |  |
| colType | Defines the business object types that are valid for column objects. You can provide a comma-separated list (no spaces) of types. This parameters filers the column objects based on types. | Business Unit Business Unit,Department |
| emxGridTable.jsp |  |  |
| CommandName | Use along with the MenuName parameter to define the page that should be displayed in the Content frame of the Navigator page when users log in. The MenuName parameter defines the name of a menu object that represents a submenu and the CommandName parameter defines the name of a command object from which the href URL is to be obtained. Note that the links in the Actions menu submenus cannot be used for display in the content frame. | The name of a command object that is assigned to a menu object that represents an application submenu on on an app category menu. For example: emxNavigator.jsp?MenuName=TMCMyDesk&CommandName=TMCRoutesMyDesk Where TMCMyDesk is the name of the menu object for the Team Central submenu of the My Enovia menu and TMCRoutesMyDesk is the name of the command object for the Routes link. This command object contains an href parameter that points to the JSP for the Routes page. |
| emxNavigator.jsp |  |  |
| compareBy | One or more column names from the system table. The value can be a comma-separated list. | Quantity,Find Number |
| emxStructureCompare.jsp |  |  |
| connectionProgram | Defines the JPO:method to connect objects in a given structure hierarchy. You only need to write and specify a connectionProgram if you need to implement custom logic for connecting objects. For the structure browser, The JPO:method used for operations such as resequence, Copy, Paste, and Cut & Paste to make the needed connection between child and parent objects. This parameter is only used to implement custom logic | emxPart:connectEBOMParts emxDocument:connectFolders |
| emxIndentedTable.jsp emxStructureCompare.jsp |  |  |
| ContentPage | Defines the JSP that should appear in the Content frame (called the Home page in the user interface) when the Navigator page is called. | Any JSP plus parameters: emxNavigator.jsp?ContentPage=../engineeringcentral/emxengchgInboxFrameset.jsp Currently, the URL assigned to the ContentPage parameter can contain only one parameter/value pair as part of the URL. Any additional parameters are ignored. |
| emxNavigator.jsp |  |  |
| crossHighlight | In a structure browser, when the user selects a check box or radio button associated with a 3DLive or 3DPlay graphic, opens that graphic image. If a 3DLive Examine or 3DPlay channel has been defined (within a Powerview), the 3D image opens in that channel, otherwise the 3D image opens in a popup window. If the selectHandler parameter is passed, that Javascript function overrides this parameter. | true |
| emxIndentedTable.jsp |  |  |
| customize | Enables (true) or disables (false) the ability for users to create customized versions of the page. This parameter overrides the emxFramework.UITable.Customization system property, and has no effect on other structure browser pages. If not provided, the value specified for emxFramework.UITable.Customization is used. All access controls defined for the system table are retained in the customized table. If a column does not have a label or alt value defined (and is not a file, checkbox, icon, image, or separator), the column name shows in the user's selection list, however, this name cannot be internationalized. | true false |
| emxIndentedTable.jsp |  |  |
| DefaultCategory | Specifies the category that should be selected when the tree first opens or is first inserted into another tree. By default, the root node is selected, which means the object's Properties page displays. If the tree contains a command that users frequently want to see, you can use this parameter to have it selected instead. Alternatively, you can use the Default Category setting for the tree's menu object. If both are defined, this URL parameter overrides the setting. Note that a tree does not look for default ies defined for its sub-trees (assigned sub-menus). | The name of a command object (tab) that should be selected when the tree first opens. The command object must be assigned to the tree menu object and if it is not, the root node is selected. Defaulty=PMCWBS |
| emxTree.jsp |  |  |
| direction | Structure browser: Direction to be used for expanding the object. When one or more relationship name is passed in, the object will be expanded for the given relationship(s) using the specified direction. Generic Create form: If a relationship is passed, the direction parameter specifies the direction of the relationship from the perspective of the object being created. If from, the relationship would be FROM the passed object TO the newly-created object. If to, the relationship would be FROM the newly-created object TO the passed object. | Structure Browser: to from both (default) Generic Create form: From (default) To |
| emxIndentedTable.jsp, emxCreate.jsp |  |  |
| directionFilter | Used to turn ON or OFF the direction filter shown in the header. By default the parameter is false and the direction filter is hidden. The parameter must be passed explicitly as true to show the filter. | true false (default) |
| emxIndentedTable.jsp |  |  |
| displayView | A comma-separated list of available views (details and thumbnails) for the structured data. The View menu only shows options included in this URL parameter; that is, if you do not include thumbnails in the parameter value, then users cannot select the thumbnail display mode. This URL parameter override the emxFramework.Freezepane.view property in emxSystem.properties. | details details,thumbnails |
| emxIndentedTable.jsp |  |  |
| draw3D | Indicates whether the chart is drawn in 3D or not. | true--Draws the chart in 3D false--Draws the chart in normal mode (default) |
| emxChart.jsp |  |  |
| editable | setting that tells the search component whether to make the type field editable or not. A value of true will make the text box editable. A value of false will make it read-only. If this parameter is not passed into the search component, then the default will be false. The type field will only be set to true if the browser language setting is set to "English". If this value is set to true and the browser language is NOT set to English, then this setting is ignored and reverts to false. | true false |
| any search JSP page |  |  |
| editLink | Use to display the Edit toolbar item on the form page. The Edit link is automatically configured to call the editable version of the current form using the form edit jsp. Use to have the system include a default Edit toolbar item in the View Form header. For structure browser: when true,t he Mode menu and disabled Edit menu (AEFSBEditActions) is included in the toolbar. | true false (default) emxForm.jsp?form+ENCPart&editLink=true |
| emxForm.jsp emxIndentedTable.jsp |  |  |
| editRelationship | Defines a comma-separated list of relationships that can be used in conjunction with the cut and paste edit commands. The user can only cut or paste rows that are connected to the parent object using one of the defined relationships. | relationship_AffectedItems, relationship_EBOM |
| emxIndentedTable.jsp |  |  |
| emxSuiteDirectory | The name of the directory for the application under ematrix. Not explicitly passed: passed by the Registered Suite parameter. | engineeringcentral |
| configurable pages, many standard pages, most custom pages |  |  |
| enableCache | When false (the default) the portal tabs refresh their contents whenever the tab is clicked. If this parameter is set to true, then the contents are cached and not refreshed when the user clicks the tab. | true false (default) |
| emxPortal.jsp |  |  |
| emxExpandFilter | When the structure browser page opens, the value for this parameter determines how many levels to automatically expand the structure. | 1 3 All |
| emxIndentedTable.jsp |  |  |
| ExclusionList | Use to define the types to exclude from the top-level list of types that display when the Type Chooser opens. If neither InclusionList nor ExclusionList are specified, then all types are listed. The chooser displays all types in the top level of the hierarchy, even when Top Level Only is unchecked. | a properties file key If the SuiteKey parameter is passed in, the system looks for the key in the application-specific properties file (for example, emxEngineeringCentral.properties). If the property is not application specific, the system looks in emxSystem.properties. For example, if emxEngineeringCentral.properties contains this property: eServiceEngineeringCentral.Types=type_Part,type_ECR,type_Sketch The parameter to pass would be ExclusionList=eServiceEngineeringCentral.Types a comma delimited list of symbolic names for the types to exclude: ExclusionList=type_Part,type_ ECR |
| emxTypeChooser.jsp |  |  |
| ExclusionList | Means that the type(s) that is passed in will not be displayed in the type chooser. All other types will be displayed and users can select the types they wish to search for from the type chooser. The ExclusionList can either be a property value key or a list of types. | ExclusionList =type_part, type_ECO ExclusionList= emxFramework. GenericSearch.ExclusionList |
| any search JSP page |  |  |
| expandLevelFilter | Enable or disable the Expand filter for the structure browser page. This parameter overrides the expandLevelFilterMenu URL parameter. | true (default false |
| emxIndentedTable.jsp |  |  |
| expandLevelFilterMenu | Specify the menu used to show the Expand level filter. | Administrative menu name AEFFreezePaneExpandLevelFilter (default) |
| emxIndentedTable.jsp |  |  |
| expandProgram | Use to pass the name of the program to use for expanding the object. This parameter value should be assigned to the JPO name and the method name separated by a colon. | <JPO Name:method Name> emxPart:getEBOM emxDocument:getFolders |
| emxIndentedTable.jsp emxStructureCompare.jsp |  |  |
| expandProgramMenu | Can be assigned to an administrative menu or a command name, which contains the definitions of the JPO, method name and label. When command is passed in: Command settings program and function are used for getting the JPO name and method name for expanding the objects. The List Filter will not be shown, as there is only one JPO available to expand. When Menu is passed in: Commands connected to this menu are used for listing the options in the List Filter combo box. The label for each item is obtained from the command's label. The program and function settings on individual commands are used as the JPO method for expanding the objects, upon selecting the options from the List Filter. By default, the first command in the list is used for expanding the objects. The commands connected to the menu honor all the access settings supported by the configurable component (such as Access Mask, Access Expression, and Access Program). | Administrative menu name Administrative command name For example: ENCBOMLists (menu) PMCFolderLists(menu) ENCBOMList (command) |
| emxIndentedTable.jsp |  |  |
| Export | Shows or hides the Export tool on the page toolbar. When users click the tool, the system exports the data to a file in the user's preferred format in their preferred format: CSV, HTML, or Text. Users choose their preferred format using the Preferences tool in the global toolbar. If the user chooses CSV or Text for their export format, the export does not include Icon column type and programHTMLOutput columns. To get the column values exported when the column type is "programHTMLOutput," you must also set "Export= true" for the column. | true (Default)--The Export tool displays, allowing users to export the data. false--The tool does not display. |
| emxIndentedTable.jsp |  |  |
| fieldNameActual | Required for the Type Chooser. Use to specify the field on the form page to populate the type(s) the user selects in the Type Chooser. The system populates the specified field with the actual AEF type names, as stored in the database, so the field must be a hidden field. When calling the Type Chooser using a RangeHref on a configurable Form page, the hidden field is created automatically and has the same name as the RangeHref field. Other pages that need to get the type name selected by the user, for example to perform a search, should get the type name from this field instead of the fieldNameDisplay. | The name of a hidden field on the form page. For example, if the form page contains a field defined as follows: <input type="hidden" name="txtSelectedTypeName" value=""> This parameter should be passed to emxTypeChooser.jsp: fieldNameActual=txtSelectedTy peName |
| emxTypeChooser.jsp |  |  |
| fieldNameDisplay | Required for the Type Chooser. Use to specify the text field on the form page to populate the type(s) the user selected in the Type Chooser and display these types to the user. When calling the Type Chooser using a RangeHref on a configurable Form page, the display field is created automatically and has the same name as the RangeHref field with "Display" appended to it. This field contains the internationalized version of the selected type list. If fieldNameActual is used and the actual value and display value are different, this parameter defines the name of the field where the display value should be returned. | The name of a non-hidden field on the form page. For example, if the form page contains a field defined as follows: <input type="text" name="txtType" value="" onClick="showTypeChooser()"> This parameter should be passed to emxTypeChooser.jsp: fieldNameDisplay=txtType |
| emxTypeChooser.jsp |  |  |
| findMxLink | When true, show the mxLink icon/command button on the toolbar, which opens a search dialog box. The icon only appears on toolbars on a Create or Edit page; if passed to a View page it does not show on the toolbar. Default is false for toolbar. When toolbar is for a form or structure browser, default is true. | true false |
| emx.Form.jsp, emxCreate.jsp, emxIndentedTable.jsp |  |  |
| flatView | Specifies to show the structure browser in flat mode, with no structure. When used, there is no root object and any duplicate row objects are removed. | true false |
| emxGridTable.jsp |  |  |
| form | Specifies the web form administrative object used for presenting the form page. | Name of web form administrative object. emxForm.jsp?form=ENCPart emxCreate.jsp?form=SCSBuyerDesk |
| emxForm.jsp, emxCreate.jsp, emxFormEditDisplay.jsp |  |  |
| formFieldsOnly | By default, all fields in the defined form, dynamic attributes, and custom attributes show on form pages. If you pass formFieldsOnly=true in the href, then only the fields defined in the form object, not any dynamic or custom attributes, show in the form page. | true false (default) |
| emxForm.jsp |  |  |
| formName | Use to specify the name of the form that holds the field specified in fieldNameDisplay. If this parameter is not passed in, the Type Chooser looks for the field name on the first form of the form page. If the field name is not found in the first form, then an error message will be presented. | The name of a form on the form page. For example, if the form page contains a form defined as follows: <form name="searchPage" method="post" action="somejsp.jsp"> This parameter should be passed to emxTypeChooser.jsp: formName=searchPage |
| emxTypeChooser.jsp |  |  |
| frameName | Use to specify the name of the frame that contains the form on the form page. Needed for pages that contain forms in multiple frames. | The name of a frame on the form page. |
| emxTypeChooser.jsp |  |  |
| freezePane | Used to configure which column(s) should be displayed as the freeze pane column in the structure browser. If this parameter is not passed in, the first column in the system table is used as Freeze Pane column. A comma-separated list can be provided, and the columns are displayed in the order listed. If all of the columns are passed, the last column listed for this parameter displays in the scroll pane. | Column name Name Title Name,Title,Description |
| emxIndentedTable.jsp |  |  |
| header | The content of the heading that appears at the top of the page. Generic Create Form: The header displayed in the header frame of the form. The value can be a string resource ID or the label itself. | Any alphanumeric text or a string resource ID. header=Buyer Desk header=emxQuoteCentral.AssignedPackages.AssignedPackages |
| emxIndentedTable.jsp emxChart.jsp emxCreate.jsp |  |  |
| Header | The text to use for the header of the History page. This parameter can either be a string resource ID or a mixture of macros and text or simply text. | $<type> $<name> $<revision> emxFrameworkStringResource.Common.HistoryPageHeading Object History |
| emxHistory.jsp |  |  |
| header | The content of the heading that appears at the top of the portal page. If the structure browser is called by a command in an object's tree (category list), the text specified by this parameter is not displayed. | Any alphanumeric text or a string resource ID. header=SummaryView header=emxSpecificationCentral.SCOSummaryView.SummaryView Text string or string resource, such as: Lifecycle emxFramework.Lifecycle.LifeCyclePageHeading |
| emxIndentedTable.jsp |  |  |
| HelpMarker | Specifies the name of the help marker to call for context-sensitive help. | String The naming convention for help markers "emxhelp" followed by the object or feature and then the action, for example, emxhelproutecreate and emxhelpprojectedit. The marker is all lowercase with no spaces. |
| emxForm.jsp, emxCreate.jsp, emxIndentedTable.jsp, emxPortal.jsp, emxFormEditDisplay.jsp and most standard pages |  |  |
| hideLaunchButton | Used with the advanced structure compare tool, which uses a PowerView window. Channels and tabs in PowerView windows, that often show unrelated data, normally include a launch button that opens that tab in a maximized window. For the advanced structure compare, you may not want your users to open more windows. If true, the launch command is hidden on the toolbar when the page shows in a PowerView (portalMode=true). | true false (default) |
| emxIndentedTable.jsp |  |  |
| hideRootSelection | Shows or hides the check box for the root node when the structure browser is configured for a single root node and the selection URL parameter is set to multiple (ignored if selection is set to single or none or the structure browser includes multiple root nodes). | true false (default) |
| emxIndentedTable.jsp |  |  |
| HistoryMode | Determines whether the entire revision chain or just the current revision history should be displayed. For example, a part may have REV A, Rev B and Rev C. If we are reviewing REV B and the HistoryMode is set to "CurrentRevision", only the history for REV B is presented. If HistoryMode is set to "AllRevisions", then the history for REV A, B, C is presented. | AllRevisions--The page displays the entire revision history of the object. CurrentRevision (default)--The page displays the history for the particular revision that is being reviewed. |
| emxHistory.jsp |  |  |
| InclusionList | Use to define the top-level list of types to display when the Type Chooser opens. Note that these types do not have to be top-level types (types with no parents), they will just be listed in the top level in the chooser. The inclusion list and the exclusion list cannot both be passed as parameters. If they are, an error message is displayed. If neither is specified, all top-level types are listed. | a properties file key If the SuiteKey parameter is passed in, the system looks for the key in the application-specific properties file (for example, emxEngineeringCentral. properties). If the property is not application specific, the system looks in emxSystem.properties. For example, if emxEngineeringCentral.properties contains this property: eServiceEngineeringCentral.Types=type_Part,type_ECR,type_Sketch The parameter to pass would be InclusionList=eServiceEngineeringCentral.Types a comma delimited list of symbolic names for the types to include: InclusionList=type_Part,type_ ECR |
| emxTypeChooser.jsp |  |  |
| InclusionList | Means that the type(s) passed in will override the list of types defined in the properties file. The type or list of types will be displayed in the type chooser and users can select the types they wish to search for. The InclusionList can either be a property value key or a list of types. | InclusionList =type_part, type_ECO InclusionList= emxFramework.GenericSearch.types |
| any search JSP page |  |  |
| inquiry | Used when there is no program parameter passed in to emxIndentedTable.jsp. Specifies the inquiry administrative object that should be used to retrieve the business objects to be included in the table and any inquiries for the table page filter list. emxIndentedTable.jsp use the first inquiry object to display the list in the table when the page is first loaded. If there is only one inquiry, the filter list in the header is not displayed. Currently, to build the filter list, you must use either JPOs or Inquiry objects. You cannot use both. For charts:Assigned to one inquiry administrative object name. The inquiry will be used to get the objects used to draw the chart. | Names of inquiry administrative objects separated by commas. inquiry=SCSBuyerDesk,SCSBuyerDeskAssigned inquiry=ENCAllParts, ENCReleasedParts |
| emxChart.jsp emxIndentedTable.jsp |  |  |
| inquiryLabel | This parameter is used with the inquiry parameter. Specifies the labels for each inquiry in the filter list in the page header. Each label is associated to the corresponding inquiry administrative object name specified in the inquiry parameter. The label may have the actual text or the string resource id for internationalization. | One or more labels (equal to the number of objects specified in the inquiry parameter) for the inquiry administrative object names separated by a comma. inquiryLabel=All,Assigned inquiryLabel= emxEngineeringCentral.Common.All, emxEngineeringCentral.Common.Released |
| emxIndentedTable.jsp |  |  |
| insertNewRow | Adds to the toolbar in edit mode. When passed, the page adds these commands to the toolbar: Create New Create new after Create new before Remove inserted row(s) | true false |
| emxIndentedTable.jsp |  |  |
| jsTreeID | The ID of the existing tree object, which is required to update the tree with "insert" mode. When the tree is constructed for the first time, the "jsTreeID" parameter will not have any valid value. Whenever a category within the tree is clicked, the system passes the current "jsTreeID" to that page and if that page needs to update the tree, it must call emxTree.jsp with the same "jsTreeID" value. | Active NodeId, for example, root_7 or root_7_0_6 node567590204694.5947 |
| emxTree.jsp |  |  |
| jpoAppServerParamList | Allows session data to be passed to a JPO and uses the format: scope:attributeName where scope can be one of these values: application session request and the attribute must be a valid attribute used within the specified scope. The parameter can pass a comma-separated list of scope:attributeName values. The attribute values must be serializable. | application:<attributeName>,session:<attributeName>,request:<attributeName> |
| emxForm.jsp emxIndentedTable.jsp |  |  |
| labelDirection | Used for bar charts, stacked bar charts, and line charts to specify whether to display the x-axis in a horizontal or vertical direction. | horizontal - Draws the x-axis label horizontally (default) vertical - Draws the x-axis label vertically |
| emxChart.jsp |  |  |
| launched | When the Launch button is clicked from a channel tab, launched=true is passed to the new popup page. It indicates that the popup is a result of clicking the Launch button. The behavior of the popup page can be changed from the normal mode pages using this parameter. In normal mode, pages will not have the launched parameter and will default to launched=false . | true false (default) |
| emxForm.jsp |  |  |
| level | The number of hierarchical levels to traverse to fetch object details. The level must be an integer value. | 1 (default) |
| emxStructureCompare.jsp |  |  |
| levelsToDeriveColumnData | Number of levels to traverse to determine column objects. Use when column objects are based on cell values to identify the number of levels used to build the column list. This parameter is not used when colRelationship is specified. | Any integer value |
| emxGridTable.jsp |  |  |
| levelsToDeriveRowData | Number of levels to traverse row data. By default, all levels are expanded. | Any integer value |
| emxGridTable.jsp |  |  |
| lookupJPO | The JPO name and method name to execute the lookup function for a row added to the structure browser (using +). The JPO is invoked when the user clicks Lookup Entry . When passed, the page adds these commands to the toolbar: Add existing Add existing after Add existing before Remove inserted row(s) | JPOName:methodName |
| emxIndentedTable.jsp |  |  |
| massPromoteDemote | Overrides the system property emxFramework.Lifecycle.MassPromoteDemote.Enable for the specific page. The default value for this parameter is the value for the above property. The page must also include the State and Type columns. | true false |
| emxIndentedTable.jsp |  |  |
| massUpdate | Use to turn on or off mass update controls on the editable page. | true (default)--Mass update is available for the page. false--Mass update not is available for the page. |
| emxIndentedTable.jsp |  |  |
| massUpdateTCL | Use if the Range Program setting is defined for any column in a structure browser and you want the column to be available in the mass update tool. If the structure browser does not contain any columns that use a Range Program, you do not need to pass this parameter. | true--Range values for the affected columns will show in the mass update tool. false (default)--Range values for the affected columns will not show in the mass update tool. |
| emxIndentedTable.jsp |  |  |
| matchBasedOn | One or more column names from the table. The value can be a comma-separated list. | Type,Revision |
| emxStructureCompare.jsp |  |  |
| MenuName | Defines which application submenu should be expanded when the Navigator page is called. If you don't specify a submenu, the first menu in the menu will be expanded. Can also be used along with the CommandName parameter to define the page to appear in the Content frame on login. | The name of a menu object that represents an application submenu. For example: emxNavigator.jsp?MenuName=TMCMyDesk |
| emxNavigator.jsp |  |  |
| mode | Specifies whether the form or structure browser page should be view or edit mode. | view (default)--form or structure browser is read only edit--form or structure browseris editable emxForm.jsp?form=ENCPart&mode=edit |
| emxForm.jsp emxIndentedTable.jsp |  |  |
| mode | Controls whether the tree for the object is inserted into the current tree or replaces the current tree. | Insert-Insert the tree for the selected object into the current tree, if there is one. Replace (default) --Replace the current tree, if there is one, with the tree for the selected object. |
| emxTree.jsp |  |  |
| mode | Used when calling the Navigator page from an external link on a Web site, portal page, or email message. Defines whether the Navigator window contains the banner and global toolbar as it normally does when using the applications. | Menu--The Navigator page contains all standard elements. Tree--The Navigator page contains only the tree frame and the content frame. For example: emxNavigator.jsp?mode=Menu&ContentPage=../sourcingcentral/emxBuyerDeskTable.jsp |
| emxNavigator.jsP |  |  |
| multiColumnSort | Enables or disables multiple column sorting on a page. If disabled, the user can still sort by a single column if one is specified in the sortColumnName parameter. When set to false, multiple column sorting is also disabled in any custom tables users' create based on this system table. | true (default) false |
| emxIndentedTable.jsp |  |  |
| nameField | Configures the type of name field on the generic create form. | autoName keyin (default) both |
| emxCreate.jsp |  |  |
| nameSelectable | Defines the expression used for the name column to use for searching and filtering. This parameter is required if the name column is not Name AND will be used for the Find and Filter toolbar commands. To highlight matching rows, the Name column must include these settings: Style Function: getNameStyle Style Program: local:com.matrixone.apps.framework.ui.UITableGrid |  |
| emxGridTable.jsp |  |  |
| objectId | Use to specify the business object for which the properties need to be displayed. Required parameter for structure browser. Generic Create form: If an OID is passed, the object created will be connected to this object using the relationship specified by the relationship parameter. | <OID for the root object in the structure> 2233.5567.2323.4678 For example: emxForm.jsp?objectId=3243.32424.232 |
| emxForm.jsp emxCreate.jsp emxIndentedTable.jsp emxStructureCompare.jsp |  |  |
| objectId | When used with emxTree.jsp, defines the business object ID for which the tree will be constructed. For example, if the object is of type ECR, the tree object of menu type "type_ECR" will be constructed. If the menu "type_ECR" does not exist, the tree will be based on the "Default Tree" configuration. When used with emxNavigator.jsp, defines the business object whose navigation tree should be displayed when the Navigator window is called. | Valid business object ID. 2233.5567.2323.4678 emxForm.jsp?objectId=3243.32424.232 |
| emxTree.jsp emxNavigator.jsp configurable pages, many standard pages, most custom pages |  |  |
| ObserveHidden | Determines whether to display hidden types or not. | true (default)--Hidden types are not included in the chooser's list of types. false--Hidden types are included. |
| emxTypeChooser.jsp |  |  |
| onReset | The name of a JavaScript function that is invoked when a user clicks the Reset button on an editable Structure Browser page. The function is invoked after resettting all changes on the page, but before refreshing the structure. The function must be included in the file defined in emxSystem.properties: eServiceSuiteAPPLICATIONNAME.UIFreezePane.ValidationFile = VALIDATIONFILENAME (either .js or .jsp) For example: eServiceSuiteEngineeringCentral.UIFreezePane.ValidationFile = emxEngineeringCentralFormValidation.jsp | resetEverything |
| emxIndentedTable.jsp |  |  |
| owner | Assigns a default owner to the object being created. If no value is passed, the context user is assigned as the owner. | Design Engineer Buyer |
| emxCreate.jsp |  |  |
| pageSize | Enables pagination for structure browsers in flat mode (no expansions). When this URL parameter is passed, the structure browser initially loads the first set of n rows (for example, 25), and includes a set of pagination tools at the bottom right of the page. Users can select a specific page, or use the back and forward buttons to move through the pages. Users can also click the page button to enable or disable pagination. When pagination is enabled, sort works over all rows, but all other structure browser tools (such as Row Grouping and Find-In) only work on the currently-displayed rows. | integer value |
| emxIndentedTable.jsp |  |  |
| pagination | Specifies the number of rows to show per page. If the rows span more than one page, users can use the Left and Right Arrow buttons or the Page drop-down list to navigate to other pages. If this parameter is not specified, the system uses the value set in the pagination property in emxSystem.properties. When installed, this property is set to 10 rows per page. If 0 is specified, all objects are listed on one page. | Any number. Use 0 if you don't want to paginate. If 0 is specified, the pagination controls do not include the controls for navigation. |
| emxFormEditDisplay.jsp |  |  |
| parallelLoading | Enables Java parallel processing to improve the loading of data Parallel loading of data improves performance and is used whenever this parameter is set to true and there are more than 500 rows of data. | true false (default) |
| emxIndentedTable.jsp |  |  |
| parentOID | Passed to toolbars if there is a valid objectId passed. | Business object ID, for example, 46697.12656.52860.3882 |
| configurable pages, many standard pages, most custom pages |  |  |
| policy | The policy to assign to the type being created. You can use either the original or symbolic policy name, although symbolic names are recommended. | policy_Part policy_DifferentPart |
| emxCreate.jsp |  |  |
| portal | Used when calling the Navigator page from an external link to ensure the Login page does not replace the frame that contains the link. | true--Turns the portal mode on to ensure the Login page and Navigator page have no effect on the page that contains the URL link to the Navigator page. false--Turns the portal mode off. If the parameter isn't specified, the portal mode is turned off. ./Apps/Framework/VERSION/commonUI/emxNavigator.jsp?portal=true |
| emxNavigator.jsp |  |  |
| portal | Specifies the menu administrative object that represents the portal page. | Name of menu administrative object defined to represent a portal page. |
| emxPortal.jsp |  |  |
| portalMode | Every page configured inside the PowerView includes the parameter portalMode=true , so that the page can differentiate between normal display and portal display. When the Launch button is clicked, it launches the currently displayed channel tab into a maximized popup window, passing the parameters launched=true and portalMode=false to the new window. | true false (default) |
| emxForm.jsp |  |  |
| postProcessJPO | The JPO to use when a user clicks the Done button on a form component. | <JPOName>:<MethodName> |
| emxFormEdit.jsp emxIndentedTable.jsp |  |  |
| postProcessURL | The JSP called when a user clicks the Done button on a form page. The configured JSP is called after the edit processing and database update. Specify the JSP name using the macro for the page location, or use the relative path as listed in the examples. | ${SUITE_DIR}/emxCustomPostProcess.jsp or ../<ApplicationDirectory>/emxCustomPostProcess.jsp Example: ../engineeringcentral/emxCustomPostProcess.jsp |
| emxFormEdit.jsp emxIndentedTable.jsp |  |  |
| preFilter | Determines which action types to display when the History page comes up. If the parameter is not passed, the page lists all action types. If the Action Type filter control is shown on the page (determined by the ShowFilterAction parameter), the preFilter actions are listed in the text box and the filter is applied to the history list when the page first opens. The user can use the Action Type filter to change the filter, adding and removing displayed actions as needed. If the Action type filter control is not shown, the user cannot change the preFilter actions. By setting preFilter actions and hiding the Action Type filter, you can hide specific actions that you do not want users to see. | The parameter accepts two types of values: Comma delimited list of action types: preFilter=connect, disconnect, create String Resource ID: preFilter=emxSystem.preFilter.List To specify which string resource properties file to look in to get the value, include the SuiteKey parameter. Possible values for the SuiteKey parameter are: eServiceSuiteTeamCentral, eServiceSuiteEngineeringCentral, eServiceSuiteProgramCentral, or any other suite name. If the SuiteKey is not passed in, the History page looks for the key in the emxSystem.properties file. For example, in the emxSystem.properties file, the value for the emxSystem.preFilter.List might be "connect, create, disconnect". |
| emxHistory.jsp |  |  |
| preProcessJPO | The JPO to use when a user clicks Edit on a form component. | <JPOName>:<MethodName> |
| emxFormEdit.jsp emxIndentedTable.jsp |  |  |
| preProcessURL | The JSP called before displaying an editable table or form. Specify the JSP name using the macro for the page location, or use the relative path as listed in the examples. | ${SUITE_DIR}/emxCustomPreProcess.jsp or ../<ApplicationDirectory>/emxCustomPreProcess.jsp Example:../engineeringcentral/emxCustomPrePreocess.jsp |
| emxFormEdit.jsp emxIndentedTable.jsp |  |  |
| PrinterFriendly | Specifies whether the page should include the Printer Friendly tool. If this parameter is not included, the value is assumed to be True and the page displays the Printer Friendly tool. You can have the system pass this parameter automatically by entering the PrinterFriendly setting for the command object that calls the page. | true (default) false |
| emxForm.jsp emxIndentedTable.jsp |  |  |
| program | Used only when there is no inquiry parameter passed to emxChart.jsp. This parameter is used as an alternative approach to the inquiry object approach. This program approach uses the JPO program object to get the object list to be displayed in the table. This parameter is assigned to one set of values that will form a JPO program name and the method name. The format of the parameter value is: program=<JPO program name>:<JPO method name> The program name and the method name are separated by a colon ":". The parameter value may have one or more sets of values separated by comma ",". The first set of values (JPO name and method name) are used to get the object list when loading first. All programs are associated with the filter in the header. If there is only one program value, the filter in the header will not be shown. | This parameter is assigned to one (or more in view mode) set of values that will form a JPO program name and the method name. The format of the parameter value is: program=<JPO program name>:<JPO method name> The program name and the method name are separated by a colon ":". program=emxTableBuyerDesk:getBuyerDesk,emxTableBuyerDesk:getAssignedBuyerDesk |
| emxChart.jsp, emxFormEditDisplay.jsp |  |  |
| programLabel | Used with the parameter program defined above. Specifies the labels for each program in the filter list in the page header. The value may contain one or more labels (equal to number of program values) separated by a comma ",". Each label is associated to the corresponding JPO object and method name specified in program parameter. The label may have the actual text or the string resource id for internationalization. | programLabel =All,Assigned programLabel = emxEngineeringCentral.Common.All, emxEngineeringCentral.Common.Released |
| emxIndentedTable.jsp |  |  |
| RegisteredDirectory | The directory where the help files are located. | Directory name within ematrix. |
| emxForm.jsp |  |  |
| relationship | Structure Browser: Relationship names to be used for expanding the root object to display the table structure view. The same relationships are used while expanding the structure by clicking on the plus on the nodes. The relationship name passed in can be the symbolic name or the actual relationship name. Using the symbolic names is recommended. A property setting can be used to pass the relationship names. The property must be assigned to one or more relationships to be used for expansion. When one or more relationships is passed in, the program uses the list of passed-in relationship(s) only, to expand the object. If no relationship parameter is passed in, then it assumes all. <all> (default) - The given object will be expanded to get all the connected objects, irrespective of what relationship is used, to be displayed as child/parent objects. Generic Create Form: The name of the relationship to use to connect the object being created to the object passed by the objectId parameter. You can use either the original or symbolic relationship name, although symbolic names are recommended. | <relationship name> <list of comma separated relationship names> <property key assigned to one or more relationships> For example: relationship_EBOM, relationship_PartSpecification relationship_Employee all (default) For Generic Create form, a single relationship can be passed. |
| emxIndentedTable.jsp emxCreate.jsp emxStructureCompare.jsp |  |  |
| relationshipFilter | Used to turn ON or OFF the relationship filter shown in the header. By default the parameter is false and the relationship filter is hidden. The parameter must be passed explicitly as true to show the filter. | true false (default) |
| emxIndentedTable.jsp |  |  |
| relID | Use to specify the relationship used to get the relationship attribute values for relationship fields. | Valid relationship ID. emxForm.jsp?relId=3243.32424.232 |
| emxForm.jsp |  |  |
| ReloadOpener | Determines whether to call a reload() method on the opener/parent page (the form page from which the choose is launched). Reloading the page updates other fields on the form page based on the type(s) selected from the Type Chooser. | true--The reload method is called. false (default)--The reload method is not called. |
| emxTypeChooser.jsp emxCreate.jsp |  |  |
| renderPDF | Controls whether the Render PDF icon is shown in the form toolbar when displaying the form in View mode. If the parameter is not passed in, the default is false and the icon is not shown. | true--The Render PDF icon is shown in the form toolbar. false--The Render PDF icon is not shown in the form toolbar. |
| emxForm.jsp |  |  |
| reportType | The type of report to generate. | complete_summary (default) difference_only Unique_toLeft_Report Unique_toRight_Report Common_Report |
| emxStructureCompare.jsp |  |  |
| resequenceRelationship | Used when the order of child objects is significant. Defines a comma-separated list of relationships that determine if a paste operation is a resequencing. Resequencing means that the child object was moved from one place in the list of child objects to another (under the same parent object). | relationship_AffectedItems, relationship_EBOM |
| emxIndentedTable.jsp |  |  |
| resetForm | Used with the advanced structure compare tool. If true, the page, includes a Reset link. | true false (default) |
| emxForm.jsp |  |  |
| rowBusWhereClause | Defines a where clause to apply during expansion when determining rows. The value can be any valid MQL where clause for the business object. Using this parameter could result in child objects being filtered out if the where clause filters out parent objects. |  |
| emxGridTable.jsp |  |  |
| rowDirection | Defines the relationship direction to expand for the rows in the structure browser table. | from to |
| emxGridTable.jsp |  |  |
| rowExcludeIntermediateRel | Defines the relationship to exclude from the structure browser when the rowRelationship parameter includes intermediate objects. Only use this parameter if there is an intermediate object for every object in the structure (not a common usage). |  |
| emxGridTable.jsp |  |  |
| rowGrouping | When true, the is included on the toolbar, allowing the user to group rows by up to 3 columns. See 对结构化内容中的行进行分组 to see how an end user can use this feature. | true (default) false |
| emxIndentedTable.jsp |  |  |
| rowGroupingColumnNames | When using row grouping, you can provide a comma-separated list of up to 3 column names so that when the page is opened, it automatically groups the page by the values in the specified columns. |  |
| emxIndentedTable.jsp |  |  |
| rowJPO | Can be used instead of URL parameters to identify table rows and dynamic cell values. The specified JPO must return a MapList of table rows. | JPO:method |
| emxGridTable.jsp |  |  |
| rowRelationship | Defines the relationships used for expanding the root objects of rows in the structure browser. You can provide a comma-separated (no space) list of relationships. Required if the structure browser expansion is URL driven instead of JPO driven. |  |
| emxGridTable.jsp |  |  |
| rowRelWhereClause | Defines a where clause to apply against relationships during expansion when determining rows. The value can be any valid MQL where clause for the relationship. |  |
| emxGridTable.jsp |  |  |
| rowType | Defines the business object types valid for expanded row objects. You can provide a comma-separated (no space) list of types. By default, all types are included. |  |
| emxGridTable.jsp |  |  |
| SelectAbstractTypes | Determines whether users can select abstract types. | true--Abstract types can be selected. false (default)--Abstract types cannot be selected but they are displayed in the hierarchical list of types. |
| emxTypeChooser.jsp |  |  |
| selectHandler | The name of a Javascript function to invoke with a user selects/deselects a check box or radio button in a structure browser. If this URL parameter is not defined AND the 3DLive channel exists, the function (FreezePaneregister(strID)) is used. |  |
| emxIndentedTable.jsp |  |  |
| selection | Controls whether the page adds a column of check boxes or radio buttons in the left-most column of the table. When this parameter is set to single or none , the parameter objectCompare is always assumed as false and the object compare icon is not displayed. | multiple--Users can select more than one row in the table. A check box is displayed in the left column of each row. There is no access restriction for the check boxes. If this parameter is passed, it overrides any check box column added to the table administrative object. single--Users can select one row in the table. A radio button is displayed in the left column of each row. none--A selection column is not added to the page. (A check box column can still be displayed by adding the column to the table administrative object.) |
| emxIndentedTable.jsp emxFormEditDisplay.jsp |  |  |
| SelectType | Determines whether the Type Chooser has single select radio buttons or multi-select check boxes. singleselect is the default for the Type Chooser; multiselect is the default for search pages. | multiselect--Users can choose multiple types using check boxes. singleselect--Users can choose only one type using a radio button. |
| emxTypeChooser.jsp any search JSP page |  |  |
| showApply | When set to true, shows the Apply button in Edit mode. When set to false, Edit mode does not have an Apply button. | true (default) false |
| emxIndentedTable.jsp |  |  |
| showClipboard | This menu is enabled by default and adds the to the page toolbar. The icon acts as a pull-down menu: If the user clicks , selected objects are added to the clipboard collection for that user. If the user clicks the arrow, the user can select the New/Add to Collections or Add to Clipboard Collection command. Set the value for this parameter to false to disable this feature. | true (default) false |
| emxIndentedTable.jsp emxForm.jsp |  |  |
| ShowFilterAction | Determines whether to display the action type filter or not. | true (default)--The action type filter displays. false--The action type filter does not display. |
| emxHistory.jsp |  |  |
| ShowFilterTextBox | Determines whether to display the filter text box or not. | true (default)--The filter text box displays. false--The filter text box does not display. |
| emxHistory.jsp |  |  |
| ShowIcons | Determines whether to display types icons in the Type Chooser. | true (default)--Type icons are displayed. false--Type icons are not displayed. This improves performance. |
| emxTypeChooser.jsp |  |  |
| showPageHeader | Enables the page header in the PowerView page. When set to false, the header( including the toolbar) does not display in the PowerView page. | true (default) false |
| emxPortal.jsp |  |  |
| showPageURLIcon | When true, shows in the toolbar. This tool lets users copy the URL to the specific ENOVIA application page. When false, does not show in the toolbar. The default value for this parameter is defined by the emxFramework.Toolbar.ShowPageURLIcon property in emxSystem.properties . | trues false |
| emxFormEditDisplay.jsp emxForm.jsp emxCreate.js emxIndentedTable.jsp emxPortal.jsp |  |  |
| showRMB | Enables or disables the right-click menus on the page. When set to false, all right-click menus for that page are disabled. | true (default) false |
| emxIndentedTable.jsp |  |  |
| showTabHeader | Applies only in Portal mode (when the page is displayed within a PowerView), enables or disables the page header. If false, the header text is not displayed in the PowerView tab. If true, the header text shows in the tab. | true false (default) |
| emxIndentedTable emxForm.jsp |  |  |
| sortColumnName | Specifies the column by which the table should be sorted when the page is first loaded. If no column is specified, the rows are listed in the order they are retrieved from the database. Can be a comma-separated list of up to 3 column names. The page sorts by the first provided column, then by the second, then by the third. Used as the default Sort by settings in the Customize Table View dialog box. Columns defined with sortType=other setting cannot be used with multiColumnSort. | Name of table column. column1,column2,column3 |
| emxIndentedTable.jsp, emxFormEditDisplay.jsp |  |  |
| sortDirection | Defines the sort order for the columns specified in the sortColumnName parameter. If a single value is passed, it applies to all columns, or you can pass a comma-separated list of values matching the number of columns in the sortColumnName parameter. Used as the default sort directions in the Customize Table View dialog box. | ascending (default)--Sort a to z or 0 to n . descending--Sort z to a or n to 0. ascending,descending,ascending |
| emxIndentedTable.jsp |  |  |
| StringResourceFileId | The name of the String Resource file for the application. | emxFrameworkStringResource |
| configurable pages, many standard pages, most custom pages |  |  |
| Style | Determines the cascading style sheet to use for defining the layout and color for headers and footers on the popup dialog. If the style parameter is not specified, the list style is used with the default style sheet. You can use a different style sheet by changing the following property in emxSystem.properties: emxNavigator.UITable.Style.List = styles/emxUIList.css If you specify style = dialog , the default style sheet is used. You can use a different style sheet by changing the following property in emxSystem.properties: emxNavigator.UITable.Style.Dialog = styles/emxUISearch.css If you specify style = PrinterFriendly , the default sheet is used. This is hard-coded into the emxTableReportView.jsp page, which is called to display the printer-friendly page. | list (default)--For pages displayed in the content frame of emxNavigator.jsp. dialog--For pages displayed in popup windows, such as search result pages. PrinterFriendly--This style is used when displaying a page in printer-friendly mode directly from a command link, rather than from the Printer Friendly icon in a list page. |
| emxFormEditDisplay.jsp |  |  |
| subHeader | Only needed when using All Revisions mode. Determines whether to display Version or Revision for the SubHeader, which separates the history events for each revision. | Version--The History page looks in the emxFrameworkStringResource.properties file for value of the emxFramework.History.Version property. Revision--The History page looks in the emxFrameworkStringResource.properties file for value of the emxFramework.History.Revision property. |
| emxHistory.jsp |  |  |
| subHeader | Creates a subHeader below the main header in the header frame. If a structure browser is called by a command in an object's tree (category list), the text specified by this parameter is displayed at the bottom of the window instead of the header area. | The value can be any static text or a string resource id. For example: subHeader= emxEngineeringCentral.Common.BOMLevel subHeader=Bill of Material Level 1 The value can also include macros such as $<type> $<revision>. |
| emxCreate.jsp emxIndentedTable.jsp emxPortal.jsp |  |  |
| submitAction | Determines the action to take after creating the object (in addition to closing the form window): refreshCaller: Reload the calling page. If called from a structure browser, then reloads the structure browser. doNothing: When called from a structure browser, does not refresh (including sorting) the calling page. treeContent: Load the newly-created object's tree in the main content frame. treePopup: Load the newly-created object's tree in a new pop-up window. For emxCreate.jsp, if no value is passed for this parameter, this alert displays to the user: The object Type <type name> Name:<object name> Rev:<revision> is created successfully. | refreshCaller treeContent treePopup |
| emxCreate.jsp emxForm.jsp |  |  |
| SubmitLabel | Defines the label for the Submit link. If not passed, the label shows as "Done". You can pass a static text string or a string resource value (recommended). | Any static text or string resource id. SubmitLabel=emxFramework.GlobalSearch.Select=Select |
| emxIndentedTable.jsp, emxForm.jsp, emxFormEditDisplay.jsp |  |  |
| submitMultipleTimes | When used as part of the advanced structure compare tool, determines if the form is closed when the user clicks Apply , or if it remains open. | true false (default) |
| emxForm.jsp |  |  |
| submitURL | Displays the Submit link in the footer frame. The value can be any valid JSP page, which gets run upon clicking Submit. The label for the Submit link can be configured using the SubmitLabel parameter. If SubmitLabel is not passed in, the default label "Submit" is used. | Any JSP. The path can include any directory macro. submitURL=${SUITE_DIR}/emxBlank.jsp |
| emxIndentedTable.jsp |  |  |
| SuiteKey | Determines which string resource property file to look in for the preFilter property key. This parameter is only used when the preFilter parameter is passed in as a resource ID. For example, SuiteKey=eServiceSuiteEngineeringCentral would be required for the preFilter parameter whose value is in the EngineeringCentral properties file, emxEngineeringCentral.properties. For the Type Chooser: Determines which application's properties file to look in for the key specified in the InclusionList or ExclusionList parameter. If the SuiteKey is not passed in, emxSystem.properties is used. | eServiceSuiteTeamCentral, eServiceSuiteEngineeringCentral, eServiceSuiteProgramCentral, or any other suite name. |
| emxHistory.jsp emxTypeChooser.jsp configurable pages, many standard pages, most custom pages |  |  |
| table | Specifies the system table object to use for presenting this targeted page or the structured view. The table object has all the information needed to display the table, including the columns to present. For a dynamic grid, defines the structure browser table to use. By default, AEFDynamicGrid is used. | Name of table administrative object. For example: table=SCSBuyerDesk table=ENCParts table=AEFDynamicGrid |
| emxIndentedTable.jsp emx.Chart.jsp, emxFormEditDisplay.jsp, emxStructureCompare.jsp |  |  |
| tableHeadClass | Customizes the style for a column header. | Class defined in dsecUIType-Custom.css |
| emxIndentedTable.jsp |  |  |
| tableMenu | Can be assigned to an administrative menu or command name, which will contain the table name definition and label. When command is passed in: Command setting table is used to get the table name for the table definition. The Table Filter is not shown as there is only one table available to view. When menu is passed in: Commands connected to this menu are used for listing the options in the Table Filter combo box. The label for each item is obtained from the command's label. The setting table on individual commands is used as the table definition for displaying the view, upon selecting the options from the Table Filter. By default, the first command is used for the table definition. The commands connected to the menu honor all the access settings supported by the configurable component (such as Access Mask, Access Expression, and Access Program). | Administrative menu name Administrative command name For example: ENCBOMViews (menu) PMCFolderViews(menu) ENCBOMView (command) |
| emxIndentedTable.jsp |  |  |
| TipPage | Specifies whether the page should include the Tip Page tool the specific html or jsp to call when a user clicks the tool. If this setting is not included, the Tip tool is not included on the page. | Name of a custom html or JSP page, including any path. The starting point for the directory reference is the content directory. For example, if you want to call an html file in ematrix/doc/customcentral and the content directory is ematrix/customcentral, you would add this parameter to the jsp: TipPage=../doc/customcentral/tippage.html emxForm.jsp?form=ENCPart&TipPage=../myapplication/showMyTipPage.jsp? |
| emxForm.jsp emxCreate.jsp emxIndentedTable.jsp emxPortal.jsp configurable pages, many standard pages, most custom pages |  |  |
| toolbar | Specifies the menu administrative object that represents the Actions menu, which appears in the page header. Defines the name of the toolbar menu to be shown on the page. For a form, specifies the menu administrative object used in the header frame. For a structure browser, a comma-separated list of UI menu names that add custom filters to the toolbar. Menus specified here are added as a new toolbar row beneath the Action toolbar. | Name of menu administrative object defined to represent the Actions menu. toolbar=SCSBuyerDesktoolbar toolbar=ECEBOMToolbar,ECEBOMFilter |
| emxForm.jsp emxPortal.jsp emxIndentedTable.jsp emxStructureCompare.jsp emxGridTable.jsp |  |  |
| TransactionType | Controls whether the table query is run within an Update transaction or Read transaction. Update transaction can be used whenever the code needs to update the database while fetching the object list. | read (default)--The table query is not run within an Update transaction. update--The table query (inquiry or JPO) will be run within an Update transaction. |
| emxIndentedTable.jsp |  |  |
| treeLabel | Use to override the default label for a tree, which is defined in the "Label" parameter of the tree menu administrative object. When this parameter is available to the emxTree.jsp page, the page uses this value as the Label for tree menu base node. This parameter also overrides any JPO specified for the label in the settings Label Program and Label Function. | emxTree.jsp?treeLabel=custom Part Label&objectId=3434.345.4564.7755 |
| emxTree.jsp |  |  |
| treeMenu | Use to override the standard tree menu for the object's type (or any alternate tree defined by an application). The standard tree menu for an object is named using the symbolic name of the type. For example, the standard tree menu object for quality part plans is type_QualityPartPlan. Using the custom tree menu by overriding the system-defined tree for any object type is NOT RECOMMENDED. | Name of a menu object for a tree: customPartTree |
| emxTree.jsp |  |  |
| triggerValidation | Controls the Validate command on the toolbar. If true or empty, the command shows on the toolbar. If set to false, the trigger validation icon does not display on the toolbar. | true (default) false |
| emxIndentedTable.jsp |  |  |
| type | Structure Browser: Used to pass the type name for filtering the expanded structure when the table is displayed and also when the structure is expanded. The type name passed in can be the symbolic name or the actual type name. Using the symbolic name is recommended. A property setting can be used to pass the type names. The property must be assigned to one or more types to be used for filtering. When one or more types is passed in, the program uses the list of passed-in types to filter the object list. If there is no parameter passed in, then it assumes all. all (default) - This assumes that all the objects expanded will be displayed and no filtering happens. This is an optional parameter. Generic Create Form: A comma-separated list of types that can be created by this form. The type can be the original or symbolic name of the business object, although symbolic names are recommended. | <type name> <list of comma separated type names> <property key assigned to one or more types> For example: type_Part,type_ECO type_Folder all (default) |
| emxIndentedTable.jsp emxCreate.jsp (required) |  |  |
| typeChooser | Specifies whether or not the type chooser will be used for the Type field. If true, the default type chooser, emxTypeChooser,jsp, is used. Only the types defined by the type parameter will be selectable by the type chooser. If you set this parameter to true, you should also set the ReloadOpener parameter to true. | true false (default) |
| emxCreate.jsp |  |  |
| typeFilter | Used to turn ON or OFF the type filter shown in the header. By default the parameter is false and the type filter is hidden. The parameter must be passed explicitly as true to show the filter. | true false (default) |
| emxIndentedTable.jsp |  |  |
| useRowSelectionsAsCols | Indicates that rows selected in an existing table are used as columns on the dynamic table view. In this case, the Actions menu command that calls emxGridTable.jsp requires that rows are selected. If used, the command that calls emxGridTable.jsp must have the Submit=true setting. When used, the cellRelationshipa nd colRelationship parameters are not used to derive the columns, but cellRelationship is still used to intersect with the columns. | true false |
| emxGridTable.jsp |  |  |
| useRowSelectionsAsRows | Indicates that rows selected in an existing table are used as root objects on the dynamic table view. In this case, the Actions menu command that calls emxGridTable.jsp requires that rows are selected. | true false |
| emxGridTable.jsp |  |  |
| useTypeChooser | setting that tells the search component whether to display a type chooser or not for the type field. If this parameter is not passed into the search component, then the default will be true. | true false |
| any search JSP page |  |  |
| vault | The vault where the object being created will be stored. You can use either the original or symbolic value name, although symbolic names are recommended. | Gold vault_Gold |
| emxCreate.jsp |  |  |
| vaultChooser | Specifies whether or not the vault chooser will be used for the Vault field. If true, the default type chooser, emxVaultChooser,jsp, is used. | true false (default) |
| emxCreate.jsp |  |  |
| XAxis | Column name to be used as the x-axis for bar charts or stacked bar charts Column name to be used as the "slice" label for pie charts Column name to be used as the line label for line charts This should be a non-numeric column except for line charts. | Count |
| emxChart.jsp |  |  |
| YAxis | Comma-separated list of numeric column names to be used as the y-axis for bar charts, stacked bar charts, or line charts Column name to be used as the pie data for pie charts | totalCount weight |
| emxChart.jsp |  |  |
