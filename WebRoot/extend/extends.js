var args = {};
function getAllArgs() {
    var q=location.href.indexOf('?');
    var query = location.href.substring(q+1); // Get query string
    var pairs = query.split("&"); // Break at ampersand
    for (var i = 0; i < pairs.length; i++) {
        var pos = pairs[i].indexOf('='); // Look for "name=value"
        if (pos == -1) continue; // If not found, skip
        var argname = pairs[i].substring(0, pos); // Extract the name
        var value = pairs[i].substring(pos + 1); // Extract the value
        value = decodeURIComponent(value); // Decode it, if needed
        args[argname] = value; // Store as a property
    }
}

function getArgs(strParam) {
    return args[strParam];
}

getAllArgs();

var cityids = getArgs('cityids'); //获取url中的城市id
var brand = getArgs('brand'); //获取品牌
var service = getArgs('service'); //获取当前业务
var serviceid = getArgs('serviceid'); //获取当前业务id
var topic = getArgs('topic'); //获取当前主题
var abstracts = getArgs('abstracts'); //获取摘要
var kbdataid = getArgs('kbdataid');//商家摘要ID
//var question = abstracts.split(">")[1];
var question = getArgs('question');//问题
//console.log(kbdataid);
//console.log(service);
//console.log(abstracts);
$("#ex_abstracts").val(question);
danalyze(1);

//高级分析
function danalyze(etype) {
	var url = '';
	if(etype==1){//继承
		url = "extend/getExtendKbdatas.action";
	}else{//高级分析
		url = "extend/analysis.action";
	}
	$.ajax({
		type : "POST",
		async : false,
		timeout : 120000,
		url : url,
		data : {
			question : $("#ex_abstracts").val(),
			type :'danalyze',
			city : cityids,
			kbdataid : kbdataid
		},
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			var detaildata = data["result"];
			$("#ex_city").val(data["abscity"]);	
			if (data["success"] == true) {
				if(etype==1){//继承
					filldg(detaildata)
				}else{
					detailload(detaildata);
				}
			} 
		}
	});
}

//填充高级分析
function detailload(detaildata) {
	for ( var i = 0; i < detaildata.length; i++) {
		var allsegments = detaildata[i]["allsegments"];
		var creditresults = detaildata[i]["creditresults"];
		var worddata =  word(allsegments);
//		console.log(creditresults);
		filldg(creditresults);
		$("#ex_word").empty();
		$("#ex_word").append(worddata);// 填充word
	}
}

function filldg(data){
	$('#ex_abs_dg').datagrid({
	    loadMsg:'数据正在加载中，请稍后......',
	    emptyMsg:'没有数据',
//	    fitColumns:false,
	    rownumbers:true,
	    nowrap:false,
	    width:920,
	    height:275,
	    data:data,
	    columns:[[
	              {field:'ck',checkbox:true},
	              {field:'kbdataid',title:'摘要id',width:1,hidden:true},
	              {field:'abstracts',title:'共享标准问',width:290},
	              {field:'city',title:'适用地市',width:290},
	              {field:'state',title:'是否共享语义',width:100,align:'center',formatter: function(value, row, index){
	            	    if (value == "否") {
	            	        return '<font color="red">' + value + '</font>';
	            	    }else {
	            	        return '<font color="blue">' + value + '</font>';;
	            	    }
	            	}
	              }
	              ]],
	    toolbar: [{
      		iconCls: 'icon-edit',
      		text: "保存",
      		handler:batchSave
      	},'-',{
      		iconCls: 'icon-delete',
      		text: "批量删除",
      		handler: batchDelete
      	}
//      	,'-',{
//      		iconCls: 'icon-knowledgehit',
//      		text: "批量继承",
//      		handler: batchSave
//      	},'-',{
//      		iconCls: 'icon-add',
//      		text: "新增",
//      		handler: addExtendBtn
//      	}
      	],          
		striped:true,
	})
}

//修改
function exdgUpdate(){
	var row = $('#ex_abs_dg').datagrid("getSelections");
	if(row.length!=1){
		$.messager.alert('提示', '请选择1条记录', 'info');
	}else{
		dialogAjax(row[0]);
		//地市
		$('#inheritCity').combotree({
			url:'extend/getCityTree.action',
			editable:false, 
			multiple:true,
			queryParams:{
				local : row[0].state=='是' ? row[0].city : "",
				city : cityids 		
			}
		});
	}
}

//根据attr6、abstractid获取serviceOrproductinfo
function dialogAjax(row){
		$('#ex_dialog').show().dialog({
		    title: '修改继承',
		    width: 660,
		    height: 400,
		    closed: false,
		    cache: false,
		    modal: true
		});
		$.ajax({
			type : "POST",
//			async : false,
			url : "extend/getServiceInfo.action",
			data : {
				attr6 : kbdataid,
				kbdataid : row.kbdataid
			},
			dataType : "json",
			success : function(data, textStatus, jqXHR) {
				if(data){
					$('#ex_form').form('load',{
						'kbdataid':row.kbdataid,
						'abstracts':row.abstracts,
						'attr15':data.attr15,
						'attr6':data.attr6,
						'attr8':data.attr8,
						'attr9':data.attr9,
						'attr10':data.attr10,
						'attr11':data.attr11,
						'attr12':data.attr12,
						'attr13':data.attr13
						});
				}else{
					$('#ex_form').form('load',{ 'kbdataid':row.kbdataid,abstracts:row.abstracts,'attr15':row.city,'attr6':kbdataid});
				}
			},
		});
		
}

//保存继承
function saveExtend(){
    var jsonData = arrayToObject($("#ex_form").serializeArray());
    $('#inheritCityadd').combotree();
    if(Array.isArray(jsonData.attr15)){
    	jsonData.attr15 = arrayToString(jsonData.attr15);
    }
    jsonData.resourcetype = 'querymanage';
    jsonData.type = 'addExtend';
    jsonData.serviceid = serviceid;

    $.ajax({
        type: "POST",
        url: "extend/saveExtend.action",
        data: jsonData,
        success: function(data){
//        	console.log(data);
        	$.messager.alert('提示', data["msg"], 'info');
        	$('#ex_dialog').dialog('close');
        	danalyze();
        }
    });
}


//批量删除
function batchDelete(){
	var ids = "";
	var rows = $('#ex_abs_dg').datagrid("getSelections");
	if(rows.length==0){
		$.messager.alert('系统提示', '请选择再删除', 'info');
	}else{
		for(var i=rows.length-1;i>=0;i--){
			if(rows[i].state=="是"){
				ids = ids+rows[i].kbdataid+",";
			}
			$('#ex_abs_dg').datagrid("deleteRow",i);
		}
//		console.log(ids);
		if(ids.length > 0){
			ids = ids.substr(0,ids.length - 1);
			$.ajax({
		        type: "POST",
		        url: "extend/deleteExtend.action",
		        data : {
					attr6 : kbdataid,
					kbdataid : ids,
					resourcetype:'querymanage',
		            operationtype:'D',
		            resourceid:serviceid
				},
				dataType : "json",
		        success: function(data){
		//        	console.log(data);
		        	$.messager.alert('系统提示', data["msg"], 'info');
		        }
		    });
		}
//		deleteRow
		$('#ex_abs_dg').datagrid("uncheckAll");
//		danalyze();
	}
}

//查看详情
function lookDetail(index){
	var row = $('#ex_abs_dg').datagrid('getRows')[index];
	$('#ex_detail_dialog').show().dialog({
	    title: '继承详情',
	    width: 660,
	    height: 350,
	    closed: false,
	    cache: false,
	    modal: true
	});
	$.ajax({
		type : "POST",
//		async : false,
		url : "extend/getServiceInfo.action",
		data : {
			attr6 : kbdataid,
			kbdataid : row.kbdataid
		},
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if(data){
				$('#ex_detail_form').form('load',{
					'kbdataid':row.kbdataid,
					'abstracts':row.abstracts,
					'attr15':data.attr15,
					'attr6':data.attr6,
					'attr8':data.attr8,
					'attr9':data.attr9,
					'attr10':data.attr10,
					'attr11':data.attr11,
					'attr12':data.attr12,
					'attr13':data.attr13
					});
			}else{
				$('#ex_detail_form').form('load',{ 'kbdataid':row.kbdataid,abstracts:row.abstracts,'attr15':row.city,'attr6':kbdataid});
			}
		},
	});
}

//批量继承
function batchSave(){
	var rows = $('#ex_abs_dg').datagrid("getSelections");
	if(rows.length==0){
		$.messager.alert('提示', '请选择', 'info');
	}else{
		// 批量继承
		var param = {};
		var addindex = 0;
//		console.log(rows)
		for(var i=rows.length-1;i>=0;i--){
			if(rows[i].state=="否"){
				param["listDto[" + addindex + "].id"] = rows[i].kbdataid;
				param["listDto[" + addindex + "].attr6"] = kbdataid;
				
				param["listDto[" + addindex + "].attr8"] = rows[i].attr8;
				param["listDto[" + addindex + "].attr9"] = rows[i].attr9;
				param["listDto[" + addindex + "].attr10"] = rows[i].attr10;
				param["listDto[" + addindex + "].attr11"] = rows[i].attr11;
				param["listDto[" + addindex + "].attr12"] = rows[i].attr12;
				param["listDto[" + addindex + "].attr13"] = rows[i].attr13;
				addindex++;
			}
			$('#ex_abs_dg').datagrid("deleteRow",i);
		}
		param.resourcetype='querymanage';
		param.operationtype='A';
		param.resourceid=serviceid;
		$.ajax({
	        type: "POST",
	        url: "extend/batchSave.action",
	        data : param,
			dataType : "json",
	        success: function(data){
	        	$.messager.alert('提示', data["msg"], 'info',function(){
	        		progress();
	        		danalyze();
	        	});
	        }
	    });
		
	}
}

/**
 * 新增继承
 */
function addExtendBtn(){
	$('#ex_add_dialog').show().dialog({
	    title: '新增继承',
	    width: 660,
	    height: 400,
	    closed: false,
	    cache: false,
	    modal: true
	});
			
	// 加载树形下拉	
	$('#questionservice').combotree({    
		url : "extend/createServiceTree.action",
		required: true,
		onBeforeExpand:function(node,param){ 
			$("#questionservice").combotree('tree').tree("options").url = "extend/createServiceTree.action?serviceid="+node.id;
		},
		onClick:function(rec){
			$('#questionkbdata').combobox({    
			    url:'extend/createCombobox.action?serviceid='+rec.id,    
			    valueField:'id',    
			    textField:'text'
			});  
		}
	}); 
		
	// 加载普通下拉
	$('#questionkbdata').combobox({    
	    url:'extend/createCombobox.action',
	    required: true,
	    valueField:'id',    
	    textField:'text'
	}); 
	
	//地市
	$('#inheritCityadd').combotree({
		url:'extend/getCityTree.action',
		editable:false, 
		multiple:true,
		queryParams:{
			local : "",
			city : ""
		}
	});
}

//保存新增继承
function addExtendsave(){
	var jsonData = arrayToObject($("#ex_add_form").serializeArray());
	if(jsonData.service&&jsonData.kbdataid){
		jsonData.attr6 = kbdataid;
	    if(Array.isArray(jsonData.attr15)){
	    	jsonData.attr15 = arrayToString(jsonData.attr15);
	    }
	    jsonData.resourcetype = 'querymanage';
	    jsonData.type = 'addExtend';
	    jsonData.serviceid = serviceid;
//		console.log(jsonData);
		$.ajax({
			type: "POST",
			url: "extend/saveExtend.action",
			data: jsonData,
			success: function(data){
//        	console.log(data);
				$.messager.alert('系统提示', data["msg"], 'info');
				$('#ex_add_dialog').dialog('close');
				danalyze();
			}
		});		
	}else{
		$.messager.alert('系统提示', '红色为必填项', 'info');
	}	
}

//普通显示
function word(allsegments) {
	var word = '';
	if (allsegments.length == 0) {
		word += '<p>分词内容为空！</p>';
	} else {
		for ( var i = 0; i < allsegments.length; i++) {
			word += '<p>' + allsegments[i]["word"] + ' ('
					+ allsegments[i]["wordnum"] + ' words)' + '</p>';
		}
	}
	return word;
}

//更新知识库
function updateKbdata(){
	$.ajax( {
		type : "POST",
		url : "extend/updateKbdata.action",
		success : function(data, textStatus, jqXHR) {
			var detaildata = data["result"];
//			console.log(detaildata);
			$.messager.alert('系统提示', detaildata, 'info');
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统提示', '高级分析的请求发送失败！', 'info');
		}
	});
}


function arrayToObject(array) {
    var o={};
    $.each(array,function(){
        if(o[this.name]){
            if($.isArray(o[this.name])){
                o[this.name].push(this.value);
            }else{
                o[this.name]=[o[this.name],this.value];
            }
        }else{
            o[this.name]=this.value;
        }
    });
    return o;
}

function arrayToString(array){
	var attr15 = '';
	$.each(array,function(){
		attr15 = attr15+this+","
	});
	if(attr15.length > 0){
		attr15 = attr15.substr(0,attr15.length - 1);
	}
	return attr15;
}

function progress(){
	$.messager.progress({
		title:'系统提示',
		msg:'请稍等,数据加载中......'
	});
	setTimeout(function(){
		$.messager.progress('close');
	},5000)
}

function exTestBtn(){
	 $("#ex_mytabs").tabs("select", 0);
	 $('#ex_abs_dg').datagrid('loadData', { total: 0, rows: [] });  
	progress();
	danalyze(2);
}

// 报错功能
function reportError(){
	
	var ex_word = $('#ex_word').html();
	// 没有点击“查看共享语义信息”按钮
	if ("" == ex_word){
		$.messager.alert('系统提示', '请先查看共享语义信息！', 'info');
		return;
	}
	
	var stand_abstract = "";
	var synonymy_abstracts = []; 
	var synonymy_abstract = ""; 
	var total = $('#ex_abs_dg').datagrid('getData').total;
	stand_abstract = $('#ex_abstracts').val();
//	alert(stand_abstract);
	// 有共享标准问
	if (total > 0){
		for (var i = 0;i < total;i++){
			var row = $('#ex_abs_dg').datagrid('getRows')[i];
			synonymy_abstracts.push(row.abstracts);
		}
		synonymy_abstract = synonymy_abstracts.join('|');
	}
	
	$.ajax( {
		type : "POST",
		url : "extend/reportError.action",
		data : {
			stand_abstract : stand_abstract,
			synonymy_abstract : synonymy_abstract,
			city : cityids
		},
		success : function(data, textStatus, jqXHR) {
			var detaildata = data["result"];
//			console.log(detaildata);
			$.messager.alert('系统提示', detaildata, 'info');
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统提示', detaildata, 'info');
		}
	});
	
}