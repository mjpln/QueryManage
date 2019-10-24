var userid;
var ioa;

var wordclass ;
var word;
var wordid;
var type;
var cuiworditem;
var num;
var swordid;
var curwordclass ;
var curwordclassid ;
var serviceid;

$(function() {
	 var urlparams = new UrlParams();// 所有url参数
	 userid = decodeURI(urlparams.userid);
	 ioa =  decodeURI(urlparams.ioa);
	 serviceid = decodeURI(urlparams.serviceid);
	 initPage();
	 btnhide('serviceWord');
	 
});

function initPage(){
	
	$('#wordCity').dialog('close');
	//新增业务词
	$('#addServiceWord').datagrid({
		title : '业务词显示区',
		url : 'serviceword.action',
		pagination : true,
		singleSelect : true,
		striped : true,
		rownumbers : true,
		width : 900,
		height : 380,
		toolbar : '#addServiceWordTool',	
		queryParams:{
			type : 'getServiceWord'			
		},
		columns:[[
		          {field:'rn',title:'序号',hidden:true},
		          {field:'wordclassid',title:'词类ID',hidden:true},
		          {field:'wordpatid',title:'词模ID',hidden:true},
		          {field:'businessid',title:'商家',width:240},
		          {field:'serviceword',title:'业务词',width:180,editor:'textbox'},
		          {field:'otherword',title:'别名',width:350,editor:'textbox'}
		]]
	});
}

//搜索标准名称
function selServiceWord(){
	var serviceword = $("#serviceword").textbox("getValue");
	$('#addServiceWord').datagrid('load',{
		type : 'getServiceWord',
		serviceword : serviceword
	});
} 


// 显示保存、取消-隐藏增加、修改
function btnShow(flag){
	var btn1 = "#" + flag + "Save";
	var btn2 = "#" + flag + "Cancel";
	var btn3 = "#" + flag + "Add";
	
	$(btn1).show();
	$(btn2).show();
	
	$(btn3).hide();
}

// 显示增加、修改-隐藏保存、取消
function btnhide(flag){
	var btn1 = "#" + flag + "Save";
	var btn2 = "#" + flag + "Cancel";
	var btn3 = "#" + flag + "Add";
	
	$(btn1).hide();
	$(btn2).hide();
	
	$(btn3).show();
}

// 保存按钮
function wordSave(flag){
		$('#addServiceWord').datagrid('endEdit', num);
		var row = $('#addServiceWord').datagrid('getData').rows[num];
		var serviceword = row.serviceword;
		if (null == serviceword || serviceword == ''){
			$.messager.alert('提示', "请填写业务词！", "warning");
			$('#addServiceWord').datagrid('beginEdit', num);
			return;
		}
		
		otherword = row.otherword;
		if (otherword != null || otherword != '' ){
			var otherwords = otherword.split("|");
			for(var i=0;i<otherwords.length;i++){
				if(otherwords[i].length > 4){
					$.messager.alert('提示', "别名长度不能超过4个字！", "warning");
					$('#addServiceWord').datagrid('beginEdit', num);
					return;
				}

			}
		}

		$.ajax({
			type : "POST",
			url : "serviceword.action",
			async : false,
			data:{
			type:"insertServiceWord", 
			serviceword:serviceword,
			otherword:otherword, 
			serviceid:serviceid
		},
		success : function(data, textStatus, jqXHR) {
			if(data.success == true){
				$.messager.alert('提示','新增成功' , "info");
				$('#addServiceWord').datagrid('reload');
				btnhide(flag);	
			}else{
				$.messager.alert('提示',data.msg , "warning");	
			}
			
		},		
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
		});

}

// 取消按钮
function wordCancel(flag){
	var table = "#addServiceWord";
	$(table).datagrid("rejectChanges");
	$(table).datagrid("unselectAll");
	btnhide(flag);
}

//新增业务词
function serviceWordAdd(flag){
	var table = "#addServiceWord";
	$(table).datagrid("insertRow", {
			index : 0,
			row : {}
		});
	num = 0;
	$(table).datagrid("beginEdit", num);
	btnShow(flag);
}