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
//	 setid(userid, ioa);
//	alert(serviceid);
//	return;
	 initPage();
	 btnhide('standard');
	 btnhide('other');
	 
});

function initPage(){
	
	$('#wordCity').dialog('close');
	$("#wordclasssel").combobox({    
	    url:'serviceword.action?type=getStandardWordClass&ioa=' + ioa +'&a='+ Math.random(),    
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px',
	    multiple : false, // 支持多选
		editable:false,
		onChange : function(newVal,oldVal){
		searchByWordclass(newVal);
	}
	});
	$('#standardWord').datagrid({
		title : '词条显示区',
		url : 'serviceword.action',
		pagination : true,
		singleSelect : true,
		striped : true,
		rownumbers : true,
		width : 450,
		height : 380,
		toolbar : '#standardWordTool',	
//		queryParams:{
//			ioa : ioa,
//			type : 'getStandardWord'
//		},
		columns:[[
		          {field:'rn',title:'序号',hidden:true},
		          {field:'wordid',title:'ID',hidden:true},
		          {field:'stdwordid',title:'FID',hidden:true},
		          {field:'word',title:'词条',width:180,editor:'textbox'},
		          {
		        	  field:'type',
		        	  title:'类型',
		        	  editor:{
		        	  		type:'combobox',
		        	  		options:{
		                        valueField:'id',
		                        textField:'text',
		                        data:[{ 
		                        	"id":true, 
		                        	"text":"标准名称" 
		                        	},{ 
		                        	"id":false, 
		                        	"text":"普通词" 
		                        	}],
		                        required:true,
		                        editable:false
		                    }
		          		},
		          		width:130
		          },
		          {field:'wordclass',title:'词类',width:100,hidden:true},
		          {field:'wordclassid',title:'词类ID',hidden:true},
		          {field:'citycode',title:'地市编码',hidden:true},
		          {field:'cityname',title:'地市',hidden:true},
		          {	  
		        	  field:'del',
		        	  title:'删除',
		        	  align : 'center',
		        	  formatter : function(value, row, index) {
						var id = row["wordid"];
						if (null == id){
							return '';
						} else {
							return '<a class="icon-delete btn_a" title="删除" onclick="deleteStandardWord(event,'+id+')"></a>';
						}
		          	}  
		          },
		          {
		        	  field:'city',
		        	  title:'地市属性',
		        	  formatter : function(value, row, index) {
		        	  var id = row["wordid"];
						if (null == id){
							return '';
						} else {
							return '<a class="icon-edit btn_a" title="地市" onclick="viewWordCity(event,'+index+')"></a>';
						}
					}  
		          }
		]],
		onSelect:function(rowIndex,rowData){
			wordid = rowData.wordid;
			$('#otherWord').datagrid({
				url : 'serviceword.action',
				queryParams:{
					ioa : ioa,
					type : 'getOtherWord',
					wordid : wordid,
					wordclass : $("#wordclasssel").combobox('getText')
				}
			});
		},
		onLoadSuccess:function(data){
//			alert(data.rows[0].wordclass);
			curwordclass = data.rows[0].wordclass;
			curwordclassid = data.rows[0].wordclassid;
		}
	});
	
	$('#otherWord').datagrid({
		title : '别名显示区',
		pagination : true,
		singleSelect : true,
		striped : true,
		rownumbers : true,
		width:450,
		height : 380,
		toolbar : '#otherWordTool',
		columns:[[
		          {field:'rn',title:'序号',hidden:true},
		          {field:'wordid',title:'ID',hidden:true},
		          {field:'stdwordid',title:'FID',hidden:true},
		          {field:'word',title:'别名',width:180,editor:'textbox'},
		          {
		        	  field:'type',
		        	  title:'类型',
		        	  editor:{
	        	  		type:'combobox',
	        	  		options:{
	                        valueField:'id',
	                        textField:'text',
	                        data:[{ 
	                        	"id":"全称", 
	                        	"text":"全称" 
	                        	},{ 
	                        	"id":"简称", 
	                        	"text":"简称" 
	                        	},{ 
		                        "id":"代码", 
		                        "text":"代码" 
		                        },{ 
		                        "id":"错词", 
		                        "text":"错词" 
		                        },{ 
		                        "id":"其他别名", 
		                        "text":"其他别名" 
		                        }],
	                        required:true,
	                        editable:false
	                    }
	          		},
	          		width:130
		          },
		          {field:'wordclass',title:'词类',width:100,hidden:true},
		          {field:'wordclassid',title:'词类ID',hidden:true},
		          {field:'citycode',title:'地市编码',hidden:true},
		          {field:'cityname',title:'地市',hidden:true},
		          {	  
		        	  field:'del',
		        	  title:'删除',
		        	  align : 'center',
		        	  formatter : function(value, row, index) {
						var id = row["wordid"];
						if (null == id){
							return '';
						} else {
							return '<a class="icon-delete btn_a" title="删除" onclick="deleteStandardWord(event,'+id+')"></a>';
						}
					}  
		          }
		]],
	});
}

function searchByWordclass(newVal){
	$('#standardWord').datagrid({
		queryParams:{
		wordclass : newVal,
		type : 'getStandardWord',
	},
	});
};

// 删除词条
function deleteStandardWord(event, id) {
	$.messager.confirm('提示', '确定删除该词条吗?', function(r) {
		if (r) {
			$.ajax( {
				url : 'serviceword.action',
				type : "post",
				data : {
					type : 'deleteStandardWord',
					wordid : id,
					resourcetype:'querymanage',
					operationtype:'D',
					resourceid:serviceid
				},
				async : false,
				dataType : "json",
				success : function(data, textStatus, jqXHR) {
					$.messager.alert('提示', data.msg, "info");
					if (data.success == true) {
						$("#standardWord").datagrid("reload");
						$("#otherWord").datagrid("reload");
					}
				},
				error : function(jqXHR, textStatus, errorThrown) {
					$.messager.alert('系统异常', "请求数据失败!", "error");
				}
			});
		}
	});
}

// 查看地市属性
function viewWordCity(event, index){
	if (event.stopPropagation) {// Mozilla and Opera
		event.stopPropagation();
	} else if (window.event) {// IE
		window.event.cancelBubble = true;
	}
	var row = $('#standardWord').datagrid('getData').rows[index];
	wordid = row.wordid;
	wordclass = row.wordclass;
	word = row.word;
	$('#wordCity').show().dialog({
		title: '地市属性',
		modal:true,
	    width: 450,    
	    height: 400,
	    top:50,
	    left:250
	});
	$("#worclassname").textbox('setValue',wordclass);
	$("#wordname").textbox('setValue',word);
	getWordCity(wordid,wordclass);
}

//查询词条地市信息
function getWordCity(wordid,wordclass){
	$.ajax({
		type : "POST",
		url : "worditem.action",
		async : false,
		data:{action:"selectWordCity", wordid:wordid, curwordclass:wordclass},
		success : function(data, textStatus, jqXHR) {
			var cityname = data.cityname;
			$("#cityname").val(cityname);
			//alert(cityname);
			getCityTree('edit,'+cityname);
		},
				
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

//获得地市信息comboxtree
function getCityTree(cityname){
//	var city = "南京市,合肥市,江苏省,北京市";
	$('#selLocal').combotree({
		url:'getCityTreeByLoginInfo.action',
		editable:false, 
		multiple:true,
		queryParams:{
			local : cityname
		}
	}); 
}

// 更新地市信息
function update(){
	var cityCode = $("#selLocal").combotree("getValues");
	var cityName = $('#selLocal').combotree('getText');
	
	var request = {
			action:"updateWordCity",
			wordid:wordid,
			curwordclass:wordclass, 
			citycode:cityCode + '', 
			cityname:cityName,
			resourcetype:'querymanage',
			operationtype:'U',
			resourceid:serviceid
		};
		
		var dataStr = {
			m_request : JSON.stringify(request)
		}
	
		$.post("worditem.action",request,function(data) {
			 if(data.success){ 
				 $("#cityname").val(cityName); 
				 $.messager.alert('提示',data.msg , "info");
			 }else{
				 $.messager.alert('提示',data.msg , "info"); 
			 }
		},"json");		
} 

// 搜索别名
function selOtherWord(){
	var wordsel = $("#wordsel2").textbox("getValue");
	$('#otherWord').datagrid({
		url : 'serviceword.action',
		queryParams:{
			ioa : ioa,
			type : 'selOtherWord',
			word : wordsel,
			wordclass : $("#wordclasssel").combobox('getText')
		}
	});
	$('#standardWord').datagrid('load',{
		ioa : ioa,
		type : 'getStandardWordByOtherWord',
		word : wordsel,
		wordclass : $("#wordclasssel").combobox('getText')
	});
	
}

// 搜索标准名称
function selStandardWord(){
	var wordsel = $("#wordsel").textbox("getValue");
	$('#standardWord').datagrid('load',{
		ioa : ioa,
		type : 'getStandardWord',
		word : wordsel
	});
}

// 显示保存、取消-隐藏增加、修改
function btnShow(flag){
	var btn1 = "#" + flag + "WordSave";
	var btn2 = "#" + flag + "WordCancel";
	var btn3 = "#" + flag + "WordAdd";
	var btn4 = "#" + flag + "WordEdit";
	
	$(btn1).show();
	$(btn2).show();
	
	$(btn3).hide();
	$(btn4).hide();
}

// 显示增加、修改-隐藏保存、取消
function btnhide(flag){
	var btn1 = "#" + flag + "WordSave";
	var btn2 = "#" + flag + "WordCancel";
	var btn3 = "#" + flag + "WordAdd";
	var btn4 = "#" + flag + "WordEdit";
	
	$(btn1).hide();
	$(btn2).hide();
	
	$(btn3).show();
	$(btn4).show();
}

// 新增词条
function wordAdd(flag){
	if ("other" == flag){
		var row = $('#standardWord').datagrid('getSelected');
		if (null == row){
			$.messager.alert('提示', "请选择词条！", "warning");
			return;
		}
		wordid = row.wordid;
		cuiworditem = row.word;
		swordid = row.stdwordid;
//		alert(wordid);
	}
	var table = "#" + flag + "Word";
	$(table).datagrid("insertRow", {
			index : 0,
			row : {}
		});
	num = 0;
	$(table).datagrid("beginEdit", num);
	btnShow(flag);
}

// 保存按钮
function wordSave(flag){
	if ("standard"==flag){
		
//		var rows = $('#standardWord').datagrid('getChanges');
//		var index = $('#standardWord').datagrid("getRowIndex", rows[0]);
		$('#standardWord').datagrid('endEdit', num);
		var row = $('#standardWord').datagrid('getData').rows[num];
		var isstandardword = row.type;
		if (null == isstandardword){
			$.messager.alert('提示', "请选择类型！", "warning");
			$('#standardWord').datagrid('beginEdit', index);
			return;
		}
		
		wordid = row.wordid;
		if (undefined == wordid){
			var worditem = row.word;
			if (worditem.length > 4){
				$.messager.alert('提示', "词条长度不能超过4个字！", "warning");
				return;
			}
//			var curwordclass = "电信业务父类";
//			var curwordclassid = "42500";
			var curwordclasstype = '';
			$.ajax({
				type : "POST",
				url : "worditem.action",
				async : false,
				data:{
				action:"insert", 
				worditem:worditem, 
				curwordclass:curwordclass, 
				curwordclassid:curwordclassid,
				isstandardword:isstandardword,
				curwordclasstype:curwordclasstype,
				resourcetype:'querymanage',
				operationtype:'A',
				resourceid:serviceid
			},
			success : function(data, textStatus, jqXHR) {
				$.messager.alert('提示',data.msg , "info");
				
			},
			
			error : function(jqXHR, textStatus, errorThrown) {
				$.messager.alert('系统异常', "请求数据失败!", "error");
			}
			});
		} else {
			var worditem = row.word;
			if (worditem.length>4){
				$.messager.alert('提示', "词条长度不能超过4个字！", "warning");
				return;
			}
//			var curwordclass = "电信业务父类";
//			var curwordclassid = "42500";
			var oldtype;
			var newtype;
			if (isstandardword){
				oldtype = "标准名称";
			} else {
				oldtype = "普通词";
			}
			if (type){
				newtype = "标准名称";
			} else {
				newtype = "普通词";
			}
			var curwordclasstype = '';
			$.ajax({
				type : "POST",
				url : "worditem.action",
				async : false,
				data:{
				action:"update", 
				newworditem:worditem, 
				oldworditem:word, 
				oldtype:oldtype,
				newtype:newtype,
				wordid:wordid,
				wordclassid:curwordclassid,
				curwordclass:curwordclass, 
				curwordclasstype:curwordclasstype,
				resourcetype:'querymanage',
				operationtype:'U',
				resourceid:serviceid
			},
			success : function(data, textStatus, jqXHR) {
				$.messager.alert('提示',data.msg , "info");
			},
			
			error : function(jqXHR, textStatus, errorThrown) {
				$.messager.alert('系统异常', "请求数据失败!", "error");
			}
			});
		}
		$('#standardWord').datagrid('reload');
		btnhide(flag);
	} else if ("other"==flag){
		$('#otherWord').datagrid('endEdit', num);
		var row = $('#otherWord').datagrid('getData').rows[num];
		var isstandardword = row.type;
		if (null == isstandardword){
			$.messager.alert('提示', "请选择类型！", "warning");
			$('#otherWord').datagrid('beginEdit', num);
			return;
		}
		swordid = row.wordid;
		
		if (undefined == swordid){
			var worditem = row.word;
			if (worditem.length>4){
				$.messager.alert('提示', "别名长度不能超过4个字！", "warning");
				return;
			}
//			var curwordclass = "电信业务父类";
//			var curwordclassid = "42500";
			
			$.ajax({
				type : "POST",
				url : "synonym.action",
				async : false,
				data:{
				action:"insert", 
				wordclassid:curwordclassid,
				synonym:worditem, 
				stdwordid:wordid,
				type:isstandardword,
				curworditem:cuiworditem,
				curwordclass:curwordclass ,
				resourcetype:'querymanage',
				operationtype:'A',
				resourceid:serviceid
			},
			success : function(data, textStatus, jqXHR) {
				$.messager.alert('提示',data.msg , "info");
			},
			
			error : function(jqXHR, textStatus, errorThrown) {
				$.messager.alert('系统异常', "请求数据失败!", "error");
			}
			});
		} else {
			var worditem = row.word;
			if (worditem.length>4){
				$.messager.alert('提示', "别名长度不能超过4个字！", "warning");
				return;
			}
			var stdwordid = row.stdwordid;
//			var curwordclass = "电信业务父类";
//			var curwordclassid = "42500";
			var oldtype = row.type;
			var newtype;
			$.ajax({
				type : "POST",
				url : "synonym.action",
				async : false,
				data:{
				action:"update", 
				oldsynonym:word, 
				newsynonym:worditem, 
				oldtype:oldtype,
				newtype:isstandardword,
				wordid:swordid,
				stdwordid:stdwordid,
				curwordclass:curwordclass,
				resourcetype:'querymanage',
				operationtype:'U',
				resourceid:serviceid
			},
			success : function(data, textStatus, jqXHR) {
				$.messager.alert('提示',data.msg , "info");
			},
			
			error : function(jqXHR, textStatus, errorThrown) {
				$.messager.alert('系统异常', "请求数据失败!", "error");
			}
			});
		}
		$('#otherWord').datagrid('reload');
		btnhide(flag);
	}
}

// 取消按钮
function wordCancel(flag){
	var table = "#" + flag + "Word";
	$(table).datagrid("rejectChanges");
	$(table).datagrid("unselectAll");
	btnhide(flag);
}

// 编辑词条按钮
function wordEdit(flag){
	var table = "#" + flag + "Word";
	var row = $(table).datagrid("getSelected");
	if (null == row){
		$.messager.alert('提示',"请选择一行！", "warning");
	}
	word = row["word"];
	wordid = row["wordid"];
	type = row["type"];
	var index = $(table).datagrid("getRowIndex", row);
	num = index;
	$(table).datagrid("beginEdit", index);
	btnShow(flag);
}