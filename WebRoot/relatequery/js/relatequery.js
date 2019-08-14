var kbdataid;
var serviceid;

$(function() {
	var urlparams = new UrlParams();// 所有url参数
	kbdataid = decodeURI(urlparams.kbdataid);
	serviceid = decodeURI(urlparams.serviceids);
	// 加载相关问题管理列表
	loadRelateQueryList();
});

// 查询问题
function searchQuery() {
	var relatequery = replaceSpace($("#relatequeryselect").textbox("getValue"));
	
	$('#relatequerydatagrid').datagrid('load', {
		type : "selectrelatequery",
		relatequery : relatequery,
		kbdataid:kbdataid
	});
	
}

// 加载问题管理列表
function loadRelateQueryList() {
	$("#relatequerydatagrid").datagrid( {
		height : 350,
		url : "../querymanage.action",
		queryParams : {
			type : "selectrelatequery",
			kbdataid : kbdataid,
			relatequery : ""
		},
		toolbar : "#datagrid_tb",
		pageSize : 10,
		pagination : true,
		rownumbers : true,
		striped : true,
		nowrap : false,
		fitColumns : true,
		singleSelect : false,

		loadMsg : "数据加载中,请稍后……",
		columns : [ [ {
			field : 'ck',
			checkbox : true
		}, {
			field : 'relatequeryid',
			title : '相关问题ID',
			width : 180,
			hidden:true
		}, {
			field : 'relatequery',
			title : '相关问题',
			width : 180
			
		}, 
		{
			field : 'service',
			title : '相关联业务名称',
			width : 180
			
		},{
			field : 'kbdataid',
			title : '摘要ID',
			width : 300,
			hidden:true
		}, {
			field : 'relatequerytokbdataid',
			title : '关联摘要ID',
			width : 300,
			hidden:true
		}, {
			field : 'remark',
			title : '备注',
			width : 150,
			hidden:true
		}

		] ]
	});

	$("#relatequerydatagrid").datagrid('getPager').pagination( {
		showPageList : false,
		buttons : [ {
			text : "新增",
			iconCls : "icon-add",
			handler : function() {
			 createServiceTree('service','kbdata');
			$('#relatequerywin').window('open');
		}
		}, "-", {
			text : "删除相关问题(批量)",
			iconCls : "icon-remove",
			handler : function() {
				deleteRelateQuery();
			}
		}  ]
	});

}





//批量删除客户问题
function deleteRelateQuery(){
	var combition = [];
	var rows = $("#relatequerydatagrid").datagrid("getSelections");
	if (rows.length > 0) {
		for ( var i = 0; i < rows.length; i++) {
			var relatequeryid = rows[i].relatequeryid;
			combition.push(relatequeryid);
		}
	} else {
		$.messager.alert('提示', "请至少选择一行!", "warning");
		return;
	}
	
	if(combition.length>0){
		$.messager.confirm("操作提示", "您确定要删除吗？", function(data) {
			if (data) {
				$.ajax( {
					type : "post",
					async : false,
					url : "../querymanage.action",
					data : {
						type : "deleterelatequery",
						resourcetype:'querymanage',
						operationtype:'D',
						resourceid:serviceid,
						combition : combition.join("@@")
					},
					dataType : "json",
					success : function(data, textStatus, jqXHR) {
						$.messager.alert('信息提示', data["msg"], "info");
						if (data["success"] == true) {
							$("#relatequerydatagrid").datagrid('reload');
						}
					},
					error : function(jqXHR, textStatus, errorThrown) {
//						$.messager.alert('系统异常', "请求数据失败!", "error");
					}
				});
			}
		});
	}
	
}


//新增相关关问题
function addRelateQuery(){
	var cityCode = $("#service").combotree("getValue");
	var relatequerytokbdataid = $("#kbdata").combobox('getValue');
	var relatequery = $("#kbdata").combobox('getText');
	if(relatequerytokbdataid==""|| relatequerytokbdataid==null){
		$.messager.alert('提示','请选择标准问题!', "warning");
		return;
	}
	$.ajax( {
		url : '../querymanage.action',
		type : "post",
		data : {
			type : 'insertrelatequery',
			resourcetype:'querymanage',
			operationtype:'A',
			resourceid:serviceid,
			relatequerytokbdataid:relatequerytokbdataid,
			relatequery:relatequery,
			kbdataid:kbdataid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			
			if (data.success == true) {
				$("#relatequerydatagrid").datagrid("reload");
				$('#relatequerywin').window('close');
				$.messager.alert('提示', data.msg, "info");
			}else{
				$.messager.alert('提示', data.msg, "warning");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
	
	
	
}



//构造树形图
function createServiceTree(fid,id) {
	var fdocid = "#"+fid;
	$(fdocid).combotree({
		url:'../querymanage.action?type=createtree&a='+ Math.random(),
		editable:true,
		onBeforeExpand:function(node,param){ 
			$(fdocid).combotree('tree').tree("options").url = "../querymanage.action?type=createtree&serviceid="+node.id +'&a='+ Math.random();
		}, 
		onClick:function(rec){
			createAbsCombobox(fid,id);
		}
	});
}

//根据业务构造摘要下拉框
function createAbsCombobox(fid,id) {
	var fdocid = "#"+fid;
	var docid = "#"+id
	// 获取树形结构选中的业务
	var serviceid = $(fdocid).combotree("getValue"); 
	$(docid).combobox({    
	    url:'../querymanage.action?type=createabscombobox&serviceid='+serviceid,    
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px'
	    //editable:false 
	});  
}