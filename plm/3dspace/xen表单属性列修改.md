sapce-cas服务器下的/webapps/3dspace/webapps/ENOXEngineer/assets/config路径下的xEngineer.conf.json和ColumnsMetas.json文件  
xEngineer.conf.json用于配置表单列的显示   
在defaultAttributes集合中添加  
ps:关系属性需要在ds6x后面加上关系类型（ds6wg:JF_VPMInstance.JF_FNA改成ds6wg:JF_VPMInstance.JF_FNA-Instance）
```json
"defaultAttributes": [
{
"dataIndex": "tree",
"ds6w": "ds6w:label",
"kind": "string",
"dbName": "PLMEntity.V_Name",
"visibleFlag": true,
"customisation": {
"layout": {
"removableFromView": false,
"groupingLocked": true,
"canBeGrouped": false,
"removable": false
}
}
},
{
"dataIndex": "ds6wg:JF_VPMReference.JF_PartType",
"ds6w": "ds6wg:JF_VPMReference.JF_PartType",
"kind": "string",
"dbName": "JF_VPMReference.JF_PartType",
"visibleFlag": true,
"customisation": {
"layout": {
"removableFromView": true,
"removable": true,
"canBeGrouped": true
}
}
},
{
"dataIndex": "ds6wg:EnterpriseExtension.V_PartNumber",
"ds6w": "ds6wg:EnterpriseExtension.V_PartNumber",
"kind": "string",
"dbName": "EnterpriseExtension.V_PartNumber",
"visibleFlag": true,
"customisation": {
"layout": {
"removableFromView": true,
"removable": true,
"canBeGrouped": true
}
}
},
{
"dataIndex": "ds6wg:revision",
"ds6w": "ds6wg:revision",
"kind": "string",
"dbName": "revision",
"visibleFlag": true,
"customisation": {
"layout": {
"removableFromView": true,
"removable": true
}
}
},

{
"dataIndex": "level",
"kind": "string",
"ds6w": "level",
"dbName": "",
"visibleFlag": false,
"customisation": {
"layout": {
"removableFromView": true,
"groupingLocked": true,
"removable": false
}
}
},
{
"dataIndex": "rollup",
"kind": "string",
"ds6w": "rollup",
"dbName": "",
"visibleFlag": true,
"customisation": {
"layout": {
"removableFromView": true,
"groupingLocked": true,
"removable": false
}
}
},
{
"dataIndex": "ds6w:label-Instance",
"ds6w": "ds6w:label",
"ds6wFrom": "rel",
"kind": "string",
"dbName": "PLMInstance.PLM_ExternalID",
"visibleFlag": true,
"customisation": {
"layout": {
"removableFromView": true,
"groupingLocked": true,
"removable": true
}
}
},
{
"dataIndex": "ds6wg:JF_VPMInstance.JF_FNA-Instance",
"ds6w": "ds6wg:JF_VPMInstance.JF_FNA",
"ds6wFrom": "rel",
"kind": "string",
"dbName": "JF_VPMInstance.JF_FNA",
"visibleFlag": true,
"customisation": {
"layout": {
"removableFromView": true,
"groupingLocked": true,
"removable": true
}
}
},
{
"dataIndex": "occurence",
"kind": "string",
"ds6w": "occurence",
"dbName": "",
"ds6wFrom": "rel",
"visibleFlag": false,
"customisation": {
"layout": {
"removableFromView": true,
"groupingLocked": true,
"removable": false
}
}
},
{
"dataIndex": "ds6wg:PLMReference.V_isLastVersion",
"ds6w": "ds6wg:PLMReference.V_isLastVersion",
"dbName": "PLMReference.V_isLastVersion",
"visibleFlag": true,
"kind": "string",
"Type": "boolean",
"customisation": {
"layout": {
"removableFromView": true,
"removable": true,
"canBeGrouped": true
}
}
},
{
"dataIndex": "ds6w:status",
"ds6w": "ds6w:status",
"kind": "string",
"dbName": "current",
"visibleFlag": true,
"customisation": {
"layout": {
"removableFromView": true,
"removable": true,
"canBeGrouped": true
}
}
},
{
"dataIndex": "ds6w:responsible",
"ds6w": "ds6w:responsible",
"kind": "string",
"dbName": "owner",
"visibleFlag": true,
"customisation": {
"layout": {
"removableFromView": true,
"removable": true,
"canBeGrouped": true
}
}
},
{
"dataIndex": "ds6w:reserved",
"ds6w": "ds6w:reserved",
"kind": "string",
"Type": "boolean",
"dbName": "reserved",
"visibleFlag": true,
"customisation": {
"layout": {
"removableFromView": true,
"removable": true,
"canBeGrouped": true
}
}
},
{
"dataIndex": "ds6w:modified",
"ds6w": "ds6w:modified",
"kind": "datetime",
"dbName": "modified",
"visibleFlag": true,
"customisation": {
"layout": {
"removableFromView": true,
"removable": true
}
}
},
{
"dataIndex": "ds6w:type",
"ds6w": "ds6w:type",
"kind": "string",
"dbName": "type",
"visibleFlag": true,
"customisation": {
"layout": {
"removableFromView": true,
"removable": true,
"canBeGrouped": true
}
}
},
{
"dataIndex": "ds6wg:PLMReference.V_versionComment",
"ds6w": "ds6wg:PLMReference.V_versionComment",
"kind": "string",
"dbName": "PLMReference.V_versionComment",
"visibleFlag": true,
"customisation": {
"layout": {
"removableFromView": true,
"removable": true,
"canBeGrouped": true
}
}
},
{
"dataIndex": "ds6w:identifier",
"ds6w": "ds6w:identifier",
"kind": "string",
"dbName": "name",
"visibleFlag": true,
"customisation": {
"layout": {
"removableFromView": true,
"groupingLocked": true,
"removable": true
}
}
},
{
"dataIndex": "Thumbnail",
"kind": "string",
"dbName": "",
"visibleFlag": false,
"customisation": {
"layout": {
"removableFromView": true,
"groupingLocked": true,
"removable": false
}
}
},
{
"dataIndex": "ds6wg:DELFmiContQuantity_Mass.V_ContQuantity",
"ds6w": "ds6wg:DELFmiContQuantity_Mass.V_ContQuantity",
"kind": "string",
"dbName": "DELFmiContQuantity_Mass.V_ContQuantity",
"visibleFlag": false,
"customisation": {
"layout": {
"removableFromView": true,
"removable": false
}
}
},
{
"dataIndex": "ds6wg:DELFmiContQuantity_Volume.V_ContQuantity",
"ds6w": "ds6wg:DELFmiContQuantity_Volume.V_ContQuantity",
"kind": "string",
"dbName": "DELFmiContQuantity_Volume.V_ContQuantity",
"visibleFlag": false,
"customisation": {
"layout": {
"removableFromView": true,
"removable": false
}
}
}
]
```

ColumnsMetas.json用于设置列的编辑模式
```json
{
  "managedColumns": {
    "tree": {
      "options": {
        "editableFlag": true,
        "editionPolicy": "EditionOnDoubleClick",
        "typeRepresentation": "url",
        "hyperlinkTarget": "_self",
        "dataIndex": "tree",
        "alwaysVisibleFlag": "true",
        "pinned": "left",
        "width": "250"
      }
    },
    "level": {
      "options": {
        "dataIndex": "level",
        "editableFlag": false,
        "typeRepresentation": "integer"
      }
    },
    "ds6w:label-Instance": {
      "options": {
        "dataIndex": "ds6w:label-Instance",
        "editableFlag": true
      }
    },
	"ds6wg:JF_VPMInstance.JF_FNA-Instance": {
      "options": {
        "dataIndex": "ds6wg:JF_VPMInstance.JF_FNA-Instance",
		"editableFlag": true
      }
    },
    "ds6wg:revision": {
      "options": {
        "dataIndex": "ds6wg:revision",
        "typeRepresentation": "url",
        "hyperlinkTarget": "_self",
        "editableFlag": false
      }
    },
    "ds6w:modified": {
      "options": {
        "dataIndex": "ds6w:modified",
        "editableFlag": false
      }
    },
    "ds6wg:PLMReference.V_versionComment": {
      "options": {
        "dataIndex": "ds6wg:PLMReference.V_versionComment",
        "editableFlag": false
      }
    },
    "ds6w:created": {
      "options": {
        "dataIndex": "ds6w:created",
        "editableFlag": false
      }
    },
    "occurence": {
      "options": {
        "dataIndex": "occurence",
        "editableFlag": false
      }
    },
    "ds6w:reservedBy": {
      "options": {
        "dataIndex": "ds6w:reservedBy",
        "editableFlag": false
      }
    },
    "ds6w:identifier": {
      "options": {
        "dataIndex": "ds6w:identifier",
        "editableFlag": false
      }
    },
    "ds6w:type": {
      "options": {
        "dataIndex": "ds6w:type",
        "editableFlag": false
      }
    },
    "ds6w:project": {
      "options": {
        "dataIndex": "ds6w:project",
        "editableFlag": false
      }
    },
    "ds6w:responsible": {
      "options": {
        "dataIndex": "ds6w:responsible",
        "typeRepresentation": "url",
        "hyperlinkTarget": "_self",
        "editableFlag": false,
        "className": "xen-owner-icon"
      }
    },
    "rollup": {
      "options": {
        "dataIndex": "rollup",
        "editableFlag": false,
        "groupableFlag": false,
        "typeRepresentation": "url",
        "hyperlinkTarget": "_self"
      }
    },
    "ds6w:status": {
      "options": {
        "dataIndex": "ds6w:status",
        "typeRepresentation": "tag",
        "hyperlinkTarget": "_self",
        "editableFlag": false,
        "minWidth": 50,
        "kind": "tag",
        "className": "lc-maturity-state"
      }
    },
    "ds6wg:EnterpriseExtension.V_PartNumber": {
      "options": {
        "dataIndex": "ds6wg:EnterpriseExtension.V_PartNumber",
        "typeRepresentation": "url",
        "hyperlinkTarget": "_self",
        "editableFlag": false
      }
    },
	"ds6wg:JF_VPMReference.JF_PartType": {
      "options": {
        "dataIndex": "ds6wg:JF_VPMReference.JF_PartType",
        "editableFlag": true
      }
    },
    "ds6w:reserved": {
      "options": {
        "dataIndex": "ds6w:reserved",
        "typeRepresentation": "image",
        "alignment": "center",
        "editableFlag": false
      }
    },
    "ds6w:reserved-Instance": {
      "options": {
        "dataIndex": "ds6w:reserved-Instance",
        "typeRepresentation": "image",
        "alignment": "center"
      }
    },
    "ds6w:cadMaster": {
      "options": {
        "dataIndex": "ds6w:cadMaster",
        "typeRepresentation": "image",
        "alignment": "center",
        "editableFlag": false
      }
    },
    "ds6wg:PLMReference.V_isLastVersion": {
      "options": {
        "dataIndex": "ds6wg:PLMReference.V_isLastVersion",
        "editableFlag": false,
        "typeRepresentation": "boolean"
      }
    },
    "ds6wg:DELFmiContQuantity_Mass.V_ContQuantity": {
      "options": {
        "dataIndex": "ds6wg:DELFmiContQuantity_Mass.V_ContQuantity",
        "typeRepresentation": "url",
        "hyperlinkTarget": "_self",
        "editableFlag": false
      }
    },
    "ds6wg:DELFmiContQuantity_Volume.V_ContQuantity": {
      "options": {
        "dataIndex": "ds6wg:DELFmiContQuantity_Volume.V_ContQuantity",
        "typeRepresentation": "url",
        "hyperlinkTarget": "_self",
        "editableFlag": false
      }
    },
	"ds6wg:PLMReference.V_versionComment": {
		"options": {
        "dataIndex": "ds6wg:PLMReference.V_versionComment",
        "editableFlag": true
      }
	},
    "quantity": {
      "options": {
        "dataIndex": "quantity",
        "typeRepresentation": "url",
        "hyperlinkTarget": "_self",
        "editableFlag": false
      }
    },
    "Alternate": {
      "options": {
        "dataIndex": "Alternate",
        "typeRepresentation": "url",
        "hyperlinkTarget": "_self"
      }
    },
    "Thumbnail": {
      "options": {
        "dataIndex": "thumbnail",
        "typeRepresentation": "image",
        "alignment": "center",
        "width": "80",
        "editableFlag": false
      }
    },
    "ds6w:manufacturable": {
      "options": {
        "dataIndex": "ds6w:manufacturable",
        "dbName": "VPMReference.V_IsManufacturable",
        "editableFlag": "true",
        "editionPolicy": "EditionOnDoubleClick"
      }
    },
    "Deformable": {
      "options": {
        "dataIndex": "Deformable",
        "typeRepresentation": "url",
        "hyperlinkTarget": "_self"
      }
    },
    "ds6w:organizationResponsible": {
      "options": {
        "dataIndex": "ds6w:organizationResponsible",
        "editableFlag": false
      }
    },
    "ds6w:organizationResponsible-Instance": {
      "options": {
        "dataIndex": "ds6w:organizationResponsible-Instance",
        "editableFlag": false
      }
    },
    "ds6w:project-Instance": {
      "options": {
        "dataIndex": "ds6w:project-Instance",
        "editableFlag": false
      }
    },
    "ds6w:kind": {
      "options": {
        "dataIndex": "ds6w:kind",
        "editableFlag": false
      }
    },
    "ds6w:kind-Instance": {
      "options": {
        "dataIndex": "ds6w:kind-Instance",
        "editableFlag": false
      }
    }
  }
}
```

