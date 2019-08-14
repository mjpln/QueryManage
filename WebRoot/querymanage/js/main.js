var kbdataid = "11802590";
var kbcontentid = "13937846";
var kbanswerid = "13943995";
var _abstract = "<test>流程";
var publicscenariosid;
var combitionArr = []; 
var rule0Arr = [];
var rule1Arr = [];
var insertorupdate_combition = 0;
var insertorupdate_rule0 = 0;
var insertorupdate_rule1 = 0;
var insertorupdate_rule2 = 0;
var userid ;
var ioa;
var serviceid;
var citySelect = "";
var customItem = {};// 页面定制配置项 key：定制名，value：值
$(function() {
// getRightClickMenu();
	 var urlparams = new UrlParams();// 所有url参数
	 userid = decodeURI(urlparams.userid);
	 ioa =  decodeURI(urlparams.ioa);
	 createUserInfo(userid,ioa);
});

// 主要初始页面Tab
function initPage(){
	
	
	// 是否显示语义导出按钮 默认不显示
	if(customItem["语义导出"] != null && customItem["语义导出"] == "显示"){
		$('#mm-export').show();
	}
	// 初始化菜单区
	$('#menuarea').contextmenu(function(e){
		e.preventDefault();
	})
	
	// 初始问题库分类Tab
	$('#aa').tabs({
		onSelect:function(title, index){
			var tab = $(this).tabs('getSelected');
			if(title === '问题库分类' && !tab.inited){
				var tab = $(this).tabs('getSelected');
				$(this).tabs('update', {
					tab:tab,
					options:{
						content:'<iframe scrolling="auto" frameborder="0"  src="../classifyquery/classifyquery.html" style="width:100%;height:580px;"></iframe>'
					}
				})
				tab.inited = true;
			}
		}
	});
	
	// 初始业务搜索控件
	var westPanel = $('#qm_layout').layout('panel', 'west');
	westPanel.panel({
		title:'模型名称',
		tools:[{
			iconCls:'icon-search',
			handler:function(){
				if(this.opened){
					this.opened = false;
					westPanel.panel('setTitle', '模型名称');
					$('#qmSearchBox').textbox('destroy');
				}else{
					this.opened = true;
					westPanel.panel('setTitle', '');
					var searchBox = $('<input id="qmSearchBox" type="text"/>');
					$(this).css('margin-right','2px').after(searchBox);
					searchBox.combogrid({
						queryParams : {
							type:'searchservice',
							citySelect:citySelect
						},
						url:'../querymanage.action',
						mode:'remote',
						panelWidth:602,
						width:90,
						height:20,
						idField:'id',
						textField:'text',
						columns:[[
							{field:'text',title:'业务',width:100},
							{field:'textpath',title:'路径',width:500}
						]],
						onSelect:function(record){
							var g = searchBox.combogrid('grid'); // 获取数据表格对象
							var r = g.datagrid('getSelected');   // 获取选择的行
// opentab(r.text, r.id); // 打开tab
// var node = $('#tt').tree('find',r.id);
// $('#tt').tree('expandTo', node.target) // 展开节点
// .tree('select', node.target) // 选择节点
// .tree('scrollTo', node.target) // 滚动到节点
							opentabWithExpandService(r.text, r.id);
							
						}
					}).next('span').find('input').focus();
				}
				
			}
		},
		{
			iconCls:'icon-searchall',
			handler:function(){
				openSearchAllWin();
			}
		},
		{
			iconCls:'icon-reload',
			handler:function(){
				refreshQueryModelTree(citySelect);
			}
		},
		{
			iconCls:'icon-upload',
			handler:function(){
				openWordFileWin();
			}
		}
// {
// iconCls:'icon-update',
// handler:function(){
// $.messager.confirm('更新知识库', '知识库更新可能需要一点时间哦，确认更新吗？', function(r){
// if(r){
// updateKbdata();
// }
// })
// }
// }
		]
	});
	// 是否显示上传业务词按钮
	if(customItem["上传业务词"] != null && customItem["上传业务词"] == "不显示"){
		var options = westPanel.panel("options");
		var newOptions =[];
		$.each(options.tools, function(n, tool){
		  	if(tool.iconCls !='icon-upload'){
		  		newOptions.push(tool);
		  	}
		});
		westPanel.panel({tools:newOptions});
	}
	// 初始化全局搜索窗口
	initSearchAllWin();
	
}
// //更新知识库
// function updateKbdata(){
// $.ajax( {
// type : "POST",
// url : "../extend/updateKbdata.action",
// success : function(data, textStatus, jqXHR) {
// var detaildata = data["result"];
// $.messager.alert('系统提示', detaildata, 'info');
// },
// error : function(jqXHR, textStatus, errorThrown) {
// $.messager.alert('系统提示', '更新知识库请求发送失败！', 'info');
// }
// });
// }

// 创建用户信息
function createUserInfo(userid,ioa){
	// 初始化定制配置
	var initCustomItem = function(customItems){
		$.each(customItems,function(n,item){
			var s = item.split("=");
			customItem[s[0]] = s[1];
		});
	};
	
	$.ajax( { 
		url : '../createuser.action',
		type : "post",
		data : {
			type : 'createuserinfo',
			userid : userid,
			ioa : ioa
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if(data.msg){
				if(data.customItem){
					initCustomItem(data.customItem);
				}
				// 加载问题模型树
// loadQueryModelTree();
				 loadCity();
				 // 加载地市列表
				// createCityTree("citylist", false, false);
// cityOnSelect("bb");
// initPage();
			}else{
				$.messager.alert('系统异常', "请求数据失败!", "error");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {

		}
	});
}

// 加载区域树
function loadCity(){
	$("#bb").combotree({
		url:'../querymanage.action?type=getCityTreeByLoginInfo',
		editable:false, 
		multiple:false,
		queryParams:{
			local : 'edit'
		},
		onLoadSuccess : function(node, data) {
			citySelect = data[0].id;
			$("#bb").combotree("setValue",citySelect);
			// 加载问题业务树
			loadQueryModelTree(citySelect);
		},
		onSelect : function(node){
			var city = node.id;
			citySelect = city;
			loadQueryModelTree(citySelect);
		}
	});
}

// 添加city onselect事件
function cityOnSelect(id){ 
	var id ="#"+id;
	$(id).combotree({
		onSelect : function(node){
		var city = node.id;
		citySelect = city;
		loadQueryModelTree(citySelect);
		}
	}); 
}

function initSearchAllWin(){
	$('#searchall_gr').datagrid({
		height:340,
		toolbar:"#searchall_tb",
		rownumbers:true,
		pagination:true,
		pageSize:10,
		columns:[[
		         {field:'service', title:'模型名称', width:150},
		         {field:'', title:'知识类别', hidden:true},
		         {field:'abstractstr', title:'标准问题', width:200, formatter:function(v, r, index){
		        	 var str = v.substr(v.indexOf(">") + 1)
		        	 return $('<div/>').text(str).html();
		         }},
		         {field:'kbdataid',title:'', hidden:true},
		         {field:'answer', title:'答案', width:200},
		         {field:'go', title:'前往', width:100, align:'center', formatter:function(v, r, index){
		        	 var searchtype = $('#searchall_gr').datagrid('options').queryParams.searchtype;
		        	 return '<a href="javascript:void(0)" onclick="go('+index+', \''+searchtype+'\')">GO</a>';
	        	 }},
		        ]]
	})
}

// 全局搜索结果关联页面
function go(index, searchtype){
	var row = $('#searchall_gr').datagrid('getRows')[index];
	var hook = {
				panel1:{'normalquery': row.abstractstr, 'kbdataid': row.kbdataid},
				panel2:{'customerquery':row.customerquery}
			};
	var hook = encodeURI('hook=' + JSON.stringify(hook));
	
	if(searchtype === '业务'){
		opentabWithExpandService(row.service, row.serviceid, hook);
	}else if(searchtype === '标准问'){
		opentabWithExpandService(row.service, row.serviceid, hook);
	}else if(searchtype === '扩展问'){
		opentabWithExpandService(row.service, row.serviceid, hook);
	}else if(searchtype === '答案'){
		hook = {panel1:{'normalquery': row.abstractstr, subPage:{rowIndex:0, pageType:'答案页面'}}};
		hook = encodeURI('hook=' + JSON.stringify(hook));
		opentabWithExpandService(row.service, row.serviceid, hook);
	}else if(searchtype === '咨询'){
		
	}
	
	$('#searchall').window('close');
}



// 打开全局搜索窗口
function openSearchAllWin(){
	// initSearchAllWin();
	$('#searchall').window('center');
	$('#searchall').window('open');
	// 清空条件
	$('#searchall_content').textbox('clear');
	$('#searchtype').combobox('select', '业务');
	// 清空表格内容
	$('#searchall_gr').datagrid('loadData', []);
}

function searchAll(){
	// 清空表格内容
	$('#searchall_gr').datagrid('loadData', []);
	
	var content = $('#searchall_content').textbox('getValue');
	var searchtype = $('#searchtype').combobox('getValue');
	
	$('#searchall_gr').datagrid({
		url:'../querymanage.action',
		queryParams:{
			type:'searchall',
			content:"%"+content+"%",
			searchtype:searchtype
		}
	});
}

// 刷新问题模型树
function refreshQueryModelTree(citySelect, callback){
	$("#tt").tree({
		url:'../querymanage.action?type=refreshservicetree&citySelect='+encodeURI(citySelect),
		onLoadSuccess:callback,
		onBeforeExpand:function(node){
			$('#tt').tree('options').url = "../querymanage.action?type=createservicetree&scenariosid="
				+ node.id + '&a=' + Math.random()+'&citySelect='+encodeURI(citySelect); // 展开时发送请求去加载节点
			
        },
	});
}

// 加载问题模型树
function loadQueryModelTree(citySelect){
	$("#tt").tree({
		url: '../querymanage.action?type=createservicetree&a='+ Math.random()+'&citySelect='+encodeURI(citySelect), // ajax方式
		
// url :'../querymanage.action?type=getservicetree&citySelect='+citySelect, //
// 加载整棵树
// queryParams : {
// type:'getservicetree',
// citySelect:citySelect
// },
// url :'../querymanage.action', // 加载整棵树
// method: 'post',
		animate: true,
		// lines:true,
		cache:false,
		onLoadSuccess:function(node,data){  
			initPage();
			
// $("#tt li:eq(0)").find("div").addClass("tree-node-selected"); //设置第一个节点高亮
// var n = $("#tt").tree("getSelected");
// if(n!=null){
// expandAll();
// $("#tt").tree("select",n.target); //相当于默认点击了一下第一个节点，执行onSelect方法
// }
        
		},
		
		onBeforeExpand:function(node){
// $('#tt').tree('options',{
// queryParams : {
// type:'createservicetree',
// scenariosid:node.id,
// a:Math.random(),
// citySelect:citySelect
// },
// url : "../querymanage.action", //展开时发送请求去加载节点
// method: 'post'
// });
			// $('#tt').tree('options').url =
			// "../querymanage.action?type=createservicetree&serviceid="+node.id+'&a='+
			// Math.random()+'&citySelect='+citySelect; //展开时发送请求去加载节点
			
			
			$('#tt').tree('options').url = "../querymanage.action?type=createservicetree&scenariosid="
				+ node.id + '&a=' + Math.random()+'&citySelect='+encodeURI(citySelect); // 展开时发送请求去加载节点
			
        },
        onClick:function(node){
// // $('#aa').accordion('select','业务摘要对应关系');
// //加载业务树
// createServiceTree();
// publicscenariosid =node.id;
// //加载场景业务摘要对应关系
// loadSceneRelation();
//        
//     
// //加载场景要素
// loadElementName();
// loadWeightCombobox();
// loadElementValue();
// //加载交互规
// loadRule0Combobox();
// loadRule0();
        var sname = node.text;
        if(sname!="安徽电信问题库"){
        var title = "【"+sname+"】";	 
        opentab(title,node.id);	
        }	  
  		} ,
        loadFilter: function (data, parent) {
              for (var i = 0; i < data.length; i++) {
// alert(data[i].text);
// if (data[i].text == "场景名称") {
// $('#tt').tree('expandAll');
// $('#tt').tree('options').url =
// "../interactiveScene.action?type=createinteractivescenetree&scenariosid="+data[i].id+'&a='+
// Math.random(); //展开时发送请求去加载节点
// data[i].state = "open";
// // expand(data[i]);
// expandAll();
// }
              }
              return data;
          },
        onContextMenu: function(e, node){
        	e.preventDefault();
      		// 查找节点
      		$('#tt').tree('select', node.target);
      		// 显示快捷菜单
      		// 根业务不允许修改
			if(node.parentid!=0){
				if(!node.leaf){
					$('#mm').menu('hideItem', $('#mm-remove')[0]);
				}else{
					$('#mm').menu('showItem', $('#mm-remove')[0]);
				}
				if( node.text.indexOf('信息表') > -1 ){
					$('#mm').menu('showItem', $('#mm-openinfo')[0]);
				}else{
					$('#mm').menu('hideItem', $('#mm-openinfo')[0]);
				}
				
				$('#mm').menu('show',{
					left: e.pageX,
					top:  e.pageY
				});
				
// if(node.children){
// $('#mm').menu('disableItem', $('#mm-remove')[0]);
// }else{
// $('#mm').menu('enableItem', $('#mm-remove')[0]);
// }
// if( node.text.indexOf('信息表') > -1 ){
// $('#mm').menu('enableItem', $('#mm-openinfo')[0]);
// }else{
// $('#mm').menu('disableItem', $('#mm-openinfo')[0]);
// }
				
				
			}else if(node.parentid ==0){
				$('#mm2').menu('show',{
					left: e.pageX,
					top:  e.pageY
				});
			}
        }
          
	});
}


function nodeAppendClick(){
	var node = $('#tt').tree('getSelected');
	
	// 添加节点
	$('#nodeadd_preservicename').textbox('setValue', node.text);
	$('#nodeadd_prarentid').val(node.parentid);
	$('#nodeadd_preserviceid').val(node.id);
	$('#nodeadd_servcename').textbox('setValue', '');
	$('#win_nodeadd').window('open');
}
function nodeUpdateClick(){
	var node = $('#tt').tree('getSelected');
	
	$('#nodeupd_servicename').textbox('setValue', node.text);
	$('#nodeadd_prarentid').val(node.parentid);
	$('#nodeupd_serviceid').val(node.id);
	$('#nodeadd_newservicename').textbox('setValue', '');
	$('#win_nodeupd').window('open');
}
function nodeFAQUploadClick(){
	var node = $('#tt').tree('getSelected');
	
	$('#faq_upload').window('open');
	$('#faqnode').textbox('setValue',node.id);
	$('#faqnode').textbox('setText',node.text);
	
}
function nodeRemoveClick(){
	var node = $('#tt').tree('getSelected');
	if(node.children){
		$.messager.alert('警告', '只能删除叶子节点', 'warn');
		return ;
	}
	$.messager.confirm('删除', '删除业务节点后业务下相关内容将一并删除,确认删除【' + node.text + '】吗？', function(r){
		if(r){
			nodeRemove(node.id, node.parentid);
		}
	})
}
// 关联信息表
function openInfoClick(){
	var address=window.location.href;
// thisDLoc = document.location;
    var hostport=document.location.host;
    var domain = document.domain;
// alert(address);
// alert(hostport);
	var node = $('#tt').tree('getSelected');
	var url = "../../KM/serviceattr/serviceattr.jsp?serviceid="+encodeURI(node.id)+"&service="+encodeURI(node.text); 
	
	$.ajax( { 
		url : '../querymanage.action',
		type : "post",
		data : {
			type:"getKMUrl"
		}, 
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if(data.success){
				url = data.url + "/KM/serviceattr/serviceattr.jsp?serviceid="+encodeURI(node.id)+"&service="+encodeURI(node.text);
			}
		}
	});
	
	var content;
	var title = node.text;
	if ($('#tb').tabs('exists', title)){ 
		$('#tb').tabs('close', title);
	}
	content = '<iframe scrolling="auto" frameborder="0"  src="'+url+'" style="width:100%;height:530px;"></iframe>';
	$('#tb').tabs('add',{
		title:title,
		content:content,
		closable:true,
		tools:[{    
			iconCls:'icon-mini-refresh',
			handler:function(){
				var tab = $('#tb').tabs('getSelected');  // 获取选择的面板
				$('#tb').tabs('update', {
					tab: tab,
					options: {
						content:content
					}
				});
			}
		}] 
	});
}
function nodeSave(){
	var data = {
			type:'appendservice',
			resourcetype:'querymanage',
			operationtype:'A',
			resourceid:$('#nodeadd_preserviceid').val(),
			serviceid:$('#nodeadd_preserviceid').val(), // 父节点service id
			service:$('#nodeadd_servicename').textbox('getValue')// 新增节点名称
	};
	if(!data.service || data.service.trim() == ''){
		$.messager.alert('警告', '必须填写业务名');
		return ;
	}
	
	$.ajax( { 
		url : '../querymanage.action',
		type : "post",
		data : data, 
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			winAddClear();
			$('#win_nodeadd').window('close');
			if(data.success){
				$.messager.alert('提示', '保存成功');
				// 刷新业务树
				refreshQueryModelTree(citySelect, function(){
					var node = $('#tt').tree('find', data.serviceid);
					if(node ==null){
						asynExpandTree(data.serviceid);
					}else{
						$('#tt').tree('expandTo', node.target);
						$('#tt').tree('select', node.target);
					}
					// 赋空值，防止业务树异步数据加载成功后重复调用
					$("#tt").tree('options').onLoadSuccess = function(){};
				});
			}else{
				$.messager.alert('警告', data.msg);
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			winAddClear();
			$('#win_nodeadd').window('close');
// $.messager.alert('提示', '数据异常');
		}
	});
}
function nodeEdit(){
	var data = {
			type:'renameservice',
			resourcetype:'querymanage',
			operationtype:'U',
			resourceid:$('#nodeupd_serviceid').val(), 
			parentid:$('#nodeadd_prarentid').val(),
			serviceid:$('#nodeupd_serviceid').val(), 
			service:$('#nodeupd_newservicename').textbox('getValue')
	};
	if(!data.service || data.service.trim() == ''){
		$.messager.alert('警告', '必须填写业务名');
		return ;
	}
	
	$.ajax( { 
		url : '../querymanage.action',
		type : "post",
		data : data,
		async : false,
		dataType : "json",
		success : function(result, textStatus, jqXHR) {
			winUpdClear();
			$('#win_nodeupd').window('close');
			if(result.success){
				$.messager.alert('提示', '保存成功');
				// 刷新业务树
				refreshQueryModelTree(citySelect, function(){
					var node = $('#tt').tree('find', data.serviceid);
					if(node == null){
						asynExpandTree(data.serviceid);
					}else{
						$('#tt').tree('expandTo', node.target);
						$('#tt').tree('select', node.target);
					}
					// 赋空值，防止业务树异步数据加载成功后重复调用
					$("#tt").tree('options').onLoadSuccess = function(){};
				});
			}else{
				$.messager.alert('警告', result.msg);
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			winUpdClear();
			$('#win_nodeupd').window('close');
// $.messager.alert('提示', '数据异常');
		}
	});
}
function nodeRemove(serviceid, parentid){
	var data = {
			type: 'deleteservice',
			resourcetype:'querymanage',
			operationtype:'D',
			resourceid:serviceid, 
			serviceid: serviceid
	}
	$.ajax( { 
		url : '../querymanage.action',
		type : "post",
		data : data,
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if(data.success){
				$.messager.alert('提示', '删除成功');
				// 刷新业务树
				refreshQueryModelTree(citySelect, function(){
					var node = $('#tt').tree('find', parentid);
					if(node ==null){
						asynExpandTree(parentid);
					}else{
						$('#tt').tree('expandTo', node.target);
						$('#tt').tree('select', node.target);
					}
					// 赋空值，防止业务树异步数据加载成功后重复调用
					$("#tt").tree('options').onLoadSuccess = function(){};
				});
				
			}else{
				$.messager.alert('警告', data.msg);
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
// $.messager.alert('提示', '数据异常');
		}
	});
}

function winAddClear(){
	$('#nodeadd_preservcename').textbox('setValue', '');
	$('#nodeadd_servcename').textbox('setValue', '');
	$('#nodeadd_preserviceid').val('');
	$('#nodeadd_prarentid').val('');
}
function winUpdClear(){
	$('#nodeupd_servcename').textbox('setValue', '');
	$('#nodeupd_newservcename').textbox('setValue', '');
	$('#nodeupd_servceid').val('');
	$('#nodeupd_prarentid').val('');
	
}

// 创建citytree
function createCityTree(id,flag,isMultiple){
	var id ="#"+id;
	$(id).combotree({
		url : "../querymanage.action",
		editable:false, 
		multiple:isMultiple,
		queryParams:{
		type : "createcitytreebylogininfo",	
		flag : flag
		}
	}); 
}

function search(){
	$('#menu_select').window('open');
	createScenariosCombobox();
}

function clearName(){
	$("#select_menu_name").combobox('setText',"");
}

function searchByName(){
	var name = $("#select_menu_name").combobox('getText');

	$("#tt").tree({
		url: '../interactiveScene.action?type=createinteractivescenetreebyname&name='+encodeURI(name)+'&a='+ Math.random(),
// url: '../interactiveScene.action',
		method: 'get', 
		animate: true,
		cache:false,
// data : {
// type:'createinteractivescenetreebyname',
// name:name
// },
		onContextMenu: function(e,node){
			e.preventDefault(); 
			$(this).tree('select',node.target);
			$('#mm').menu('show',{
				left: e.pageX,
				top:  e.pageY
			});
		},
		onLoadSuccess:function(node,data){  
			
			$('#menu_select').window('close');
			$("#select_menu_name").combobox('setText',"");
// $("#tt li:eq(0)").find("div").addClass("tree-node-selected"); //设置第一个节点高亮
// var n = $("#tt").tree("getSelected");
// if(n!=null){
// expandAll();
// $("#tt").tree("select",n.target); //相当于默认点击了一下第一个节点，执行onSelect方法
// }
        
		},
		
		onBeforeExpand:function(node){
          $('#tt').tree('options').url = "../interactiveScene.action?type=createinteractivescenetree&scenariosid="+node.id+'&a='+ Math.random(); // 展开时发送请求去加载节点
          },
        onClick:function(node){
        var sname = node.text;
        if(sname!="场景名称"){
        var title = "【"+sname+"】";	 
        opentab(title,node.id)	
        }	  

  		}
          
          
	});
	
}

// 根据角色归属加载不同的右击菜单
function getRightClickMenu(){
	$.ajax( { 
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'getcustomer'
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			var url;
			if (data.customer == "全行业") {
				$("#s").show();
				$("#sm").show();
				$("#r").show();
				$("#rm").show();	
				$("#a").show();
				$("#am").show();
				$("#d").show();
				$("#dm").show();
				$("#u").show();
				$("#um").show();
				$("#sw").show();
			}else{
				$("#a").hide();
				$("#am").hide();
				$("#d").hide();
				$("#dm").hide();
				$("#u").hide();
				$("#um").hide();
// $("#sw").hide();
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
	
	
}


// 构造场景下拉框
function createScenariosCombobox() {
	$("#select_menu_name").combobox({    
	    url:'../interactiveScene.action?type=createscenarioscombobox&a='+ Math.random(),    
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px',
	    multiple : false, // 支持多选
		editable:true,
		onSelect : function(node){
		searchByName();
	}
	});  
}


// 重新加载场景
function reload(){
	loadQueryModelTree();
}

function expandAll(){
	var node = $('#tt').tree('getSelected');
	if (node){
		$('#tt').tree('expandAll', node.target);
	} else {
		$('#tt').tree('expandAll');
	}
}


function opentab(title, id, hook){
	var url = "./querymanage.html?serviceid="+encodeURI(id)+"&userid="+encodeURI(userid)+"&ioa="+encodeURI(ioa)+"&cityselect="+encodeURI(citySelect); 
	if(hook){
		url = url + '&' + hook;
	}
	addTab(title,url);
}

function opentabWithExpandService(title, id, hook){
	opentab("【" + title + "】", id, hook);
	
	var node = $('#tt').tree('find', id);
	if(node == null){
		// 选中第一个地市作为选择地市
		citySelect = $("#bb").combotree("tree").tree('getRoots')[0].id;
		$("#bb").combotree("setValue",citySelect);
		$("#tt").tree('options').onBeforeExpand = function(node){
				$('#tt').tree('options').url = "../querymanage.action?type=createservicetree&scenariosid="
					+ node.id + '&a=' + Math.random()+'&citySelect='+encodeURI(citySelect); // 展开时发送请求去加载节点
				
	        };
		// 异步展开业务树
	    asynExpandTree(id);
	}else{
		$('#tt').tree('expandTo', node.target)  // 展开节点
		.tree('select', node.target)   // 选择节点
		.tree('scrollTo', node.target)  // 滚动到节点
	}
	
}
// 异步展开业务树
function asynExpandTree(serviceid){
	// 根据id递归查询父节点
	$.ajax({
		url:'../querymanage.action',
		type:'post',
		data:{
			type:'findServiceParent',
			serviceid:serviceid
		},
		success:function(data){
			if(data.total){
				// 在业务树上查找存在的最近父节点
				for(var i = 0 ;i<data.total;i++){
					node = $('#tt').tree('find', data["parent"+i]);
					if(node != null){
						appendNode(node,serviceid);
						// 防止重复拼接
						break;
					}
				}
			}
		}
	});
}
// 请求数据补全业务树
function appendNode(node,serviceId){
	
	$.ajax({
		url:'../querymanage.action',
		type:'post',
		data:{
			type:'appendNodeByServiceId',
			serviceid:serviceId,
			parentid:node.id,
			citySelect:citySelect
		},
		success:function(data){
			var childs = $('#tt').tree('getChildren', node.target);
			if(childs != null && childs.length != 0){
				childs.forEach(function (item, index, array) {
					 $('#tt').tree('remove', item.target);
				});
			}
			
			$('#tt').tree('append', {
				parent: node.target,
				data: data
			});
			
			node = $('#tt').tree('find', serviceId);
			$('#tt').tree('expandTo', node.target)  // 展开节点
			.tree('select', node.target)   // 选择节点
			.tree('scrollTo', node.target)  // 滚动到节点
			
		}
	});
	
}

// 打开场景对应选项卡
function addTab(title, url){
	var content;
	if ($('#tb').tabs('exists', title)){ 
		$('#tb').tabs('close', title);
	}
	content = '<iframe scrolling="auto" frameborder="0"  src="'+url+'" style="width:100%;height:580px;"></iframe>';
	$('#tb').tabs('add',{
		title:title,
		content:content,
		closable:true,
		tools:[{    
			iconCls:'icon-mini-refresh',
			handler:function(){
				var tab = $('#tb').tabs('getSelected');  // 获取选择的面板
				$('#tb').tabs('update', {
					tab: tab,
					options: {
						content:content
					}
				});
			}
		}] 
	});
}


// 加载场景关系
function searchRelation(){
	loadSceneRelation();
}

// 打开场景添加编辑框
function append(){
	var t =  $('#tt');
	var node = t.tree('getSelected');
	var text = node.text;
	$('#menu_super').textbox('setValue',text);
	$('#menu_add').window('open');
	$(this).tree('beginEdit',node.target);

	
}

// 打开上传文档操作框
function uploadhtml(){
	var t =  $('#tt');
	var node = t.tree('getSelected');
	var text = node.text;
	$('#menuname').textbox('setValue',text);
	$('#html_upload').window('open');
	
}



// 提交上传文件
function upload() {
	// 得到上传文件的全路径
	var fileName = $('#fileuploadtxt').filebox('getValue');
	// 进行基本校验
	if (fileName == "") {
		$.messager.alert('信息提示', '请选择上传文件!', 'info');
	} else {
		var t =  $('#tt');
		var node = t.tree('getSelected');
		var id = node.id;
		// 对文件格式进行校验
		var d1 = /\.[^\.]+$/.exec(fileName.toLowerCase());
		if (d1 == ".doc") {
			$("#formUpload").form(
					"submit",
					{
						url : "../file/upload?type=html&path=scenariosdoc/"+id,
						success : function(data) {
							var info = $.parseJSON(data);
							var state = info["state"];
							if (state == "success") {
								var name = info["names"][0];
								saveDocname(id,name);
								
							} else {
								$.messager.alert('提示', info["message"]
										+ " 请重新上传!", 'warning');
							}
							$('#fileuploadtxt').filebox('setValue', '');
						}
					});
		} else {
			$.messager.alert('信息提示', '请选择doc格式文件!', 'info');
			$('#fileuploadtxt').filebox('setValue', '');
		}
	}
}

// 保存文档名称
function saveDocname(id,name){

	$.ajax( { 
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'updatedocname',
			scenariosid : id,
			name:name
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$('#html_upload').window('close');
			}
			
// $.messager.alert('信息提示', data.msg, 'info');
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});	
	
}

// 查看文档

function seehtml(){
	var t =  $('#tt');
	var node = t.tree('getSelected');
	var id = node.id;
	$.ajax( { 
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'getdocpath',
			scenariosid : id
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {

			if (data.success == true) {
				
				var text = node.text+"_说明文档";
				var path = data.path;
// alert(path);
				addTab("【"+text+"】", path);
			}else{
// $.messager.alert('信息提示', "查看失败!", 'warning');
				$.messager.alert('提示', data.msg, "info");
			}
			
			
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
	
}

// 添加场景名称节点
function addmenu(){
	var t =  $('#tt');
	var node = t.tree('getSelected');
	var text = node.text;
	var name = replaceSpace($("#menu_name").val());
	var scenariosid = node.id;
	$.ajax( { 
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'addmenu',
			scenariosid : scenariosid,
			name:name
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				t.tree('append', {
					parent: (node?node.target:null),
					data: [{
						id : data.id,
						text: name,
						leaf: true 
					}]
				});
				clearmenu();
				$('#menu_add').window('close');
				t.load(); 
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});	
}

// 清空菜单编辑框
function clearmenu(){ 
	$('#menu_name').textbox('setValue',''); 
}

// 删除场景名称节点
function removeit(){
	var node = $('#tt').tree('getSelected');
	var text = node.text;
	var scenariosid = node.id;
	if(text=="场景名称"){
	  return false; 
	}
	
	$.messager.confirm('提示', '删除场景节点后场景下相关内容将一并删除,确定删除吗?', function(r) {
		if (r) {
			$.ajax( { 
				url : '../interactiveScene.action',
				type : "post",
				data : {
					type : 'deletemenu',
					scenariosid : scenariosid,
					name:text
				},
				async : false,
				dataType : "json",
				success : function(data, textStatus, jqXHR) {
					$.messager.alert('提示', data.msg, "info");
					if (data.success == true) {
						$('#tt').tree('remove', node.target); 
					}
				},
				error : function(jqXHR, textStatus, errorThrown) {
					$.messager.alert('系统异常', "请求数据失败!", "error");
				}
			});
		}
	});
	
	
}

// 构造树形图
function createServiceTree() {
	$("#service").combotree({
		url:'../interactiveScene.action?type=createservicetree&a='+ Math.random(),
		editable:true,
		onBeforeExpand:function(node,param){ 
			$('#service').combotree('tree').tree("options").url = "../interactiveScene.action?type=createservicetree&serviceid="+node.id +'&a='+ Math.random();
		}, 
		onClick:function(rec){
			createCombobox();
		}
	});
}

// 根据业务构造摘要下拉框
function createCombobox() {
	// 获取树形结构选中的业务
	var serviceid = $('#service').combotree("getValue"); 
	$('#kbdata').combobox({    
	    url:'../interactiveScene.action?type=createabstractcombobox&serviceid='+serviceid,    
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px'
	    // editable:false
	});  
}

// 保存保存场景业务对应关系
function saveRelation(){
	var  absid  = $('#kbdata').combobox('getValue');
	var  abs = $('#kbdata').combobox('getText');
	var  sid = $('#service').combotree("getValue");
	var  ser = $('#service').combotree("getText");
	var  uquery = replaceSpace($("#userquery").val());
	$.ajax( { 
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'addrelation',
			scenariosid:publicscenariosid, 
			kbdataid : absid,
			abs:abs,
			serviceid:sid,
			service:ser,
			query:uquery
		},
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#servicekbdatadatagrid").datagrid('load');	
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}


function collapse(node){
	$('#tt').tree('collapse',node.target);
}
function expand(node){
	$('#tt').tree('expand',node.target);
}






// 加载业务摘要对应关系

function loadSceneRelation(){ 
	$("#servicekbdatadatagrid")
			.datagrid(
					{
						title : '业务摘要对应关系显示区',
						url : '../interactiveScene.action',
						width : 1000,
						height : 395,
						toolbar : "#servicekbdatadatagrid_tb",
						pagination : true,
						rownumbers : true,
						queryParams : {
							type : 'selectservicekbdatada',
							kbdataid : $('#kbdata').combobox('getValue'),
							serviceid :$('#service').combotree("getValue"),
							query : replaceSpace($("#userquery").val()),
							scenariosid:publicscenariosid 
						},
						pageSize : 10,
						striped : true,
						singleSelect : true,
						columns : [ [
								{
									field : 'name',
									title : '场景名称',
									width : 180
								},
								{
									field : 'scenerelationid',
									title : '场景关系ID',
									width : 180,
									hidden:true
								},
								{
									field : 'service',
									title : '业务',
									width : 180
								},
								{
									field : 'serviceid',
									title : '业务ID',
									width : 50,
									hidden:true
								},
								{
									field : 'abstract',
									title : '摘要',
									width : 200
								},
								{
									field : 'abstractid',
									title : '摘要ID',
									width : 200,
									hidden:true
								},
								{
									field : 'userquery',
									title : '用户问题',
									width : 350
								},
								{
									field : "delete",
									title : '删除',
									width : 35,
									align : 'center',
									formatter : function(value, row, index) {
										var id = row["scenerelationid"];
										return '<a class="icon-delete btn_a" title="删除" onclick="deleteSceneRelation(event,'+id+')"></a>';
									}
								} ] ],
						onClickRow : function(rowIndex, rowData) {
							$('#elementvaluedatagrid').datagrid('load', {
								type : 'selectword',
								wordclassid : rowData.wordclassid,
								name : $.trim($("#selelementvalue").val())
							});
						}
					});
	$("#servicekbdatadatagrid").datagrid('getPager').pagination( {
		showPageList : false
	});
	
	
}



// 查询问题要素
function searchElementName() {
	$('#elementnamedatagrid').datagrid('load', {
		type : 'selectelementname',
		kbdataid : kbdataid,
		kbcontentid : kbcontentid,
		name : $.trim($("#selelementname").val())
	});
}

// 加载问题要素列表
function loadElementName() {
	$("#elementnamedatagrid")
			.datagrid(
					{
						title : '场景要素显示区',
						url : '../interactiveScene.action',
						width : 550,
						height : 395,
						toolbar : "#elementnamedatagrid_tb",
						pagination : true,
						rownumbers : true,
						queryParams : {
							type : 'selectelementname',
							name : replaceSpace($("#selelementname").val()),
							scenariosid:publicscenariosid
						},
						pageSize : 10,
						striped : true,
						singleSelect : true,
						columns : [ [
								{
									field : 'name',
									title : '场景要素',
									width : 180
								},
								{
									field : 'weight',
									title : '优先级',
									width : 50,
									align : 'center'
								},
								{
									field : 'wordclass',
									title : '对应词类',
									width : 200
								},
								{
									field : 'infotalbepath',
									title : '对应信息表',
									width : 200
								},
								{
									field : 'interpat',
									title : '交互模板',
									width : 200
								},
								{
									field : 'city',
									title : '地市',
									width : 200
								},
								{
									field : 'itemmode',
									title : '填写方式',
									width : 200
								},
								{
									field : 'isshare',
									title : '是否共享',
									width : 200
								},
								{
									field : "delete",
									title : '删除',
									width : 35,
									align : 'center',
									formatter : function(value, row, index) {
										var id = row["id"];
										var weight = row["weight"];
										var name = row["name"];
										return '<a class="icon-delete btn_a" title="删除" onclick="deleteElementName(event,'
												+ id
												+ ','
												+ weight
												+ ',\''
												+ name + '\')"></a>';
									}
								} ] ],
						onClickRow : function(rowIndex, rowData) {
							$('#elementvaluedatagrid').datagrid('load', {
								type : 'selectword',
								wordclassid : rowData.wordclassid,
								name : $.trim($("#selelementvalue").val())
							});
						}
					});
	$("#elementnamedatagrid").datagrid('getPager').pagination( {
		showPageList : false,
		buttons : [ {
			text : "新增",
			iconCls : "icon-add",
			handler : function() {
			$('#elementedit_w').window('open');
			}
		}, "-", {
			text : "修改",
			iconCls : "icon-edit",
			handler : function() {
				editRule0();
			}
		}, "-", {
			text : "删除(批量)",
			iconCls : "icon-remove",
			handler : function() {
				deleteRule0();
			}
		}, "-" ]
	});
}




// 加载问题要素页面的优先级下拉框
function loadWeightCombobox() {
	$.ajax( {
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'getweight',
			scenariosid:publicscenariosid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$('#weightcombobox').combobox( {
				valueField : 'id',
				textField : 'text',
				required : true,
				missingMessage : '对应列不能为空!',
				editable : false,
				data : data.rows,
				onLoadSuccess : function() { // 加载完成后,设置选中第一项
					var val = $(this).combobox("getData");
					if (val.length > 0) {
						for ( var item in val[0]) {
							if (item == "id") {
								$(this).combobox("select", val[0][item]);
							}
						}
					}
				}
			});
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 提交问题要素表单
function submitElementNameForm() {
	$('#elementnameform').form('submit', {
		onSubmit : function() {
			var isValid = $(this).form('enableValidation').form('validate');
			if (isValid) {
				insertElementName();
			}
			return false;
		}
	});
}

// 清空新增问题要素表单
function clearElementNameForm() {
	$("#elementnametextbox").textbox('setValue', "");
	$("#wordclasstextbox").textbox('setValue', "");
	loadWeightCombobox();
}

// 新增问题要素
function insertElementName() {
	var name = $.trim($("#elementnametextbox").val());
	if (name == '') {
		$.messager.alert('提示', "问题要素名称不能为空字符串,请填写问题要素称!", "warning");
		return;
	}
	var wordclass = $.trim($("#wordclasstextbox").val());
// if (wordclass == '') {
// $.messager.alert('提示', "对应词类不能为空字符串,请填写对应词类!", "warning");
// return;
// }
	var weight = $("#weightcombobox").combobox('getText');
	$.ajax( {
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'insertelementname',
			name : name,
			weight : weight,
			wordclass : wordclass,
			scenariosid : publicscenariosid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#elementnamedatagrid").datagrid("reload");
				clearElementNameForm();
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 删除场景关系
function deleteSceneRelation(event, id) {
	alert("dd");
// if (event.stopPropagation) {// Mozilla and Opera
// event.stopPropagation();
// } else if (window.event) {// IE
// window.event.cancelBubble = true;
// }
	$.messager.confirm('提示', '确定删除该对应关系吗?', function(r) {
		if (r) {
			$.ajax( {
				url : '../interactiveScene.action',
				type : "post",
				data : {
					type : 'deletescenerelation',
					scenerelationid : id
				},
				async : false,
				dataType : "json",
				success : function(data, textStatus, jqXHR) {
					$.messager.alert('提示', data.msg, "info");
					if (data.success == true) {
						$("#servicekbdatadatagrid").datagrid("reload");
					}
				},
				error : function(jqXHR, textStatus, errorThrown) {
					$.messager.alert('系统异常', "请求数据失败!", "error");
				}
			});
		}
	});
}

// 删除问题要素
function deleteElementName(event, id, weight, name) {
	if (event.stopPropagation) {// Mozilla and Opera
		event.stopPropagation();
	} else if (window.event) {// IE
		window.event.cancelBubble = true;
	}
	$.messager.confirm('提示', '确定删除该记录吗?', function(r) {
		if (r) {
			$.ajax( {
				url : '../queryelement.action',
				type : "post",
				data : {
					type : 'deleteelementname',
					kbdataid : kbdataid,
					kbcontentid : kbcontentid,
					weight : weight,
					elementnameid : id,
					name : name,
					abs : _abstract
				},
				async : false,
				dataType : "json",
				success : function(data, textStatus, jqXHR) {
					$.messager.alert('提示', data.msg, "info");
					if (data.success == true) {
						$("#elementnamedatagrid").datagrid("reload");
						loadWeightCombobox();
					}
				},
				error : function(jqXHR, textStatus, errorThrown) {
					$.messager.alert('系统异常', "请求数据失败!", "error");
				}
			});
		}
	});
}

// 查询问题要素值
function searchElementValue() {
	var rows = $("#elementnamedatagrid").datagrid('getSelections');
	if (rows.length == 1) {
		$('#elementvaluedatagrid').datagrid('load', {
			type : 'selectword',
			wordclassid : rows[0].wordclassid,
			name : $.trim($("#selelementvalue").val())
		});
	} else {
		$.messager.alert('提示', "请选择一行问题要素!", "info");
	}
}

// 加载问题要素值的列表
function loadElementValue() {
	$("#elementvaluedatagrid")
			.datagrid(
					{
						title : '场景问题要素值显示区',
						url : '../queryelement.action',
						width : 350,
						height : 395,
						toolbar : "#elementvaluedatagrid_tb",
						pagination : true,
						rownumbers : true,
						queryParams : {
							type : 'selectword',
							wordclassid : "",
							name : ""
						},
						pageSize : 10,
						striped : true,
						singleSelect : true,
						columns : [ [
								{
									field : 'word',
									title : '问题要素值',
									width : 250
								},
								{
									field : "delete",
									title : '删除',
									width : 35,
									align : 'center',
									formatter : function(value, row, index) {
										var id = row["wordid"];
										var name = row["word"];
										return '<a class="icon-delete btn_a" title="删除" onclick="deleteElementValue(event,'
												+ id
												+ ',\''
												+ name
												+ '\')"></a>';
									}
								} ] ]
					});
	$("#elementvaluedatagrid").datagrid('getPager').pagination( {
		showPageList : false
	});
}

// 提交问题要素表单
function submitElementValueForm() {
	$('#elementvalueform').form('submit', {
		onSubmit : function() {
			var isValid = $(this).form('enableValidation').form('validate');
			if (isValid) {
				insertElementValue();
			}
			return false;
		}
	});
}

// 清空新增属性值表单
function clearElementValueForm() {
	$("#elementvaluetextbox").textbox('setValue', "");
}

// 新增属性值
function insertElementValue() {
	var name = $.trim($("#elementvaluetextbox").val());
	if (name == '') {
		$.messager.alert('提示', "问题要素值不能为空字符串,请填写问题要素值!", "warning");
		return;
	}
	var row = $('#elementnamedatagrid').datagrid('getSelected');
	if (!row) {
		$.messager.alert('提示', "请选择问题要素列表中的任意一行!", "warning");
		return;
	}
	var wordclassid = row.wordclassid;
	var wordclass = row.wordclass;
	$.ajax( {
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'insertelementvalue',
			name : name,
			wordclassid : wordclassid,
			wordclass : wordclass
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#elementvaluedatagrid").datagrid("reload");
				clearElementValueForm();
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 删除属性值
function deleteElementValue(event, id, name) {
	if (event.stopPropagation) {// Mozilla and Opera
		event.stopPropagation();
	} else if (window.event) {// IE
		window.event.cancelBubble = true;
	}
	var row = $('#elementnamedatagrid').datagrid('getSelected');
	if (!row) {
		$.messager.alert('提示', "请选择问题要素值对应的问题要素!", "warning");
		return;
	}
	var wordclass = row.wordclass;
	var weight = row.weight;
	$.messager.confirm('提示', '确定删除该记录吗?', function(r) {
		if (r) {
			$.ajax( {
				url : '../queryelement.action',
				type : "post",
				data : {
					type : 'deleteelementvalue',
					kbdataid : kbdataid,
					kbcontentid : kbcontentid,
					weight : weight,
					elementvalueid : id,
					name : name,
					wordclass : wordclass
				},
				async : false,
				dataType : "json",
				success : function(data, textStatus, jqXHR) {
					$.messager.alert('提示', data.msg, "info");
					if (data.success == true) {
						$('#elementvaluedatagrid').datagrid('reload');
					}
				},
				error : function(jqXHR, textStatus, errorThrown) {
					$.messager.alert('系统异常', "请求数据失败!", "error");
				}
			});
		}
	});
}

// 加载数据的查询和添加下拉框
function loadCombitionCombobox() {
	for ( var i = 1; i < 11; i++) {
		$("#selelementname" + i).html("");
		$("#sel" + i).hide();
		$("#addelementname" + i).html("");
		$("#add" + i).hide();
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : 'queryelement',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			kbanswerid : kbanswerid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			var info = data.rows;
			$("#kbanswer").val(data.answer);
			combitionArr = [];
			combitionArr.push( {
				field : 'ck',
				checkbox : true
			});
			for ( var i = 0; i < info.length; i++) {
				var name = info[i]["name"];
				var weight = info[i]["weight"];
				var elementvalue = info[i]["elementvalue"];
				$("#selelementname" + weight).html(name + ":");
				$("#sel" + weight).show();
				$("#selelementvalue" + weight).combobox( {
					valueField : 'id',
					textField : 'text',
					editable : false,
					data : elementvalue
				});
				$("#selelementvalue" + weight).combobox("setText", "");
				$("#addelementname" + weight).html(name + ":");
				$("#add" + weight).show();
				$("#addelementvalue" + weight).combobox( {
					valueField : 'id',
					textField : 'text',
					editable : false,
					data : elementvalue
				});
				combitionArr.push( {
					field : "condition" + weight,
					title : name,
					align : 'center'
				});
			}
			combitionArr.push( {
				field : "type",
				title : "答案类型",
				align : 'center',
				formatter : function(value, row, index) {
					if (value == '0') {
						return '普通文本';
					} else if (value == '1') {
						return '知识点映射';
					} else {
						return '普通文本';
					}
				}
			});
			combitionArr.push( {
				field : "status",
				title : "状态    ",
				align : 'center',
				formatter : function(value, row, index) {
					if (value == '0') {
						return '<span style="color:red;">未审核</span>';
					} else if (value == '1') {
						return '<span style="color:blue;">已审核</span>';
					} else {
						return '<span style="color:red;">未审核</span>';
					}
				}
			});
			combitionArr
					.push( {
						field : "returntxt",
						title : "答案内容",
						width : 300,
						formatter : function(value, row, index) {
							if (value != null) {
								return '<div title="' + value + '">' + value
										+ '</div>';
							} else {
								return value;
							}
						}
					});
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
	var typedate = [ {
		"id" : "",
		"text" : ""
	}, {
		"id" : "0",
		"text" : "普通文本"
	}, {
		"id" : "1",
		"text" : "知识点映射"
	} ];
	$("#seltype").combobox( {
		valueField : 'id',
		textField : 'text',
		editable : false,
		panelHeight : 'auto',
		data : typedate
	});
	$("#addtype").combobox( {
		valueField : 'id',
		textField : 'text',
		editable : false,
		panelHeight : 'auto',
		data : typedate
	});
	$("#selstatus").combobox( {
		valueField : 'id',
		textField : 'text',
		editable : false,
		panelHeight : 'auto',
		data : [ {
			"id" : "",
			"text" : ""
		}, {
			"id" : "0",
			"text" : "待审核"
		}, {
			"id" : "1",
			"text" : "已审核"
		} ]
	});
	clearCombitionForm();
}

// 查询数据的信息
function searchCombition() {
	var conditions = [];
	for ( var i = 1; i < 11; i++) {
		var con = $("#selelementvalue" + i).combobox("getText");
		if (con == "(空)") {
			con = "";
		}
		conditions.push(con);
	}
	var returntxttype = $("#seltype").combobox("getValue");
	var status = $("#selstatus").combobox("getValue");
	$('#combitiondatagrid').datagrid('load', {
		type : 'selectcombition',
		kbdataid : kbdataid,
		kbcontentid : kbcontentid,
		conditions : conditions.join("@"),
		returntxttype : returntxttype,
		status : status
	});
}

// 加载数据的列表
function loadCombition() {
	$("#combitiondatagrid").datagrid( {
		url : '../queryelement.action',
		height : 335,
		pagination : true,
		rownumbers : true,
		queryParams : {
			type : 'selectcombition',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : "",
			returntxttype : "",
			status : ""
		},
		pageSize : 10,
		striped : true,
		nowrap : true,
		fitColumns : true,
		singleSelect : false,
		columns : [ combitionArr ]
	});
	$("#combitiondatagrid").datagrid('getPager').pagination( {
		showPageList : false,
		buttons : [ {
			text : "新增",
			iconCls : "icon-add",
			handler : function() {
				clearCombitionForm();
			}
		}, "-", {
			text : "修改",
			iconCls : "icon-edit",
			handler : function() {
				editCombition();
			}
		}, "-", {
			text : "删除(批量)",
			iconCls : "icon-remove",
			handler : function() {
				deleteCombition();
			}
		}, "-", {
			text : "确认(批量)",
			iconCls : "icon-ok",
			handler : function() {
				confirmCombition();
			}
		}, "-", {
			text : "全量删除",
			iconCls : "icon-no",
			handler : function() {
				deleteAllCombition();
			}
		}, "-", {
			text : "全量确认",
			iconCls : "icon-confirmall",
			handler : function() {
				confirmAllCombition();
			}
		}, "-" ]
	});
}

// 保存回复模板
function saveModel() {
	var answer = $.trim($("#kbanswer").val());
	if (answer === null || answer === "") {
		$.messager.alert('提示', "回复模板不能为空!", "warning");
		return;
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : 'savemodel',
			answer : answer,
			kbanswerid : kbanswerid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 修改数据操作，将值放入数据编辑区
function editCombition() {
	var rows = $("#combitiondatagrid").datagrid("getSelections");
	if (rows.length == 1) {
		for ( var i = 1; i < combitionArr.length - 3; i++) {
			var field = combitionArr[i].field;
			var con = rows[0][field];
			var weight = field.replace("condition", "");
			$("#addelementvalue" + weight).combobox("setValue", con);
		}
		$("#addtype").combobox("setValue", rows[0].type.toString());
		$("#addreturntext").val(rows[0].returntxt);
		$("#combitionid").val(rows[0].id);
		insertorupdate_combition = 1;
	} else {
		$.messager.alert('提示', "请选择一行!", "warning");
		return;
	}
}

// 删除(批量)数据
function deleteCombition() {
	var combitionid = [];
	var rows = $("#combitiondatagrid").datagrid("getSelections");
	if (rows.length > 0) {
		for ( var i = 0; i < rows.length; i++) {
			combitionid.push(rows[i].id);
		}
	} else {
		$.messager.alert('提示', "请至少选择一行!", "warning");
		return;
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : 'deletecombition',
			combitionid : combitionid.join(","),
			abs : _abstract
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#combitiondatagrid").datagrid("reload");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 确认(批量)数据，将状态改为已审核
function confirmCombition() {
	var combitionid = [];
	var rows = $("#combitiondatagrid").datagrid("getSelections");
	if (rows.length > 0) {
		for ( var i = 0; i < rows.length; i++) {
			combitionid.push(rows[i].id);
		}
	} else {
		$.messager.alert('提示', "请至少选择一行!", "warning");
		return;
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : 'confirmcombition',
			combitionid : combitionid.join(",")
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#combitiondatagrid").datagrid("reload");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 全量删除数据
function deleteAllCombition() {
	var delconfirm = "<font size=2 style='color:red;'>";
	delconfirm += "确定要全量删除吗?<br/>";
	delconfirm += "该操作将清空所有数据!<br/>";
	delconfirm += "请慎重点击 [确定] 按钮!</font>";
	$.messager.confirm("提示", delconfirm, function(r) {
		if (r) {
			var endconfirm = "<font size=2 style='color:red;'>";
			endconfirm += "最后一次确认是否全量删除<br/>";
			endconfirm += "该操作将清空所有数据!<br/>";
			endconfirm += "请慎重点击 [确定] 按钮!</font>";
			$.messager.confirm("提示", endconfirm, function(y) {
				if (y) {
					$.ajax( {
						url : '../queryelement.action',
						type : "post",
						data : {
							type : 'deleteallcombition',
							kbdataid : kbdataid,
							kbcontentid : kbcontentid
						},
						async : false,
						dataType : "json",
						success : function(data, textStatus, jqXHR) {
							$.messager.alert('提示', data.msg, "info");
							if (data.success == true) {
								$("#combitiondatagrid").datagrid("reload");
							}
						},
						error : function(jqXHR, textStatus, errorThrown) {
							$.messager.alert('系统异常', "请求数据失败!", "error");
						}
					});
				}
			});
		}
	});
}

// 全量确认数据
function confirmAllCombition() {
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : 'confirmallcombition',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#combitiondatagrid").datagrid("reload");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 清空数据表单
function clearCombitionForm() {
	var answer = $("#kbanswer").val();
	$('#combitionform').form('clear');
	$("#kbanswer").val(answer);
	$("#addtype").combobox("setValue", "");
	$("#addreturntext").focus();
	insertorupdate_combition = 0;
}

// 新增数据
function saveCombition() {
	var conditions = [];
	var count = 0;
	for ( var i = 1; i < 11; i++) {
		var cond = $("#addelementvalue" + i).combobox("getText");
		if (cond == "(空)") {
			cond = "";
		}
		conditions.push(cond);
		if (cond === null || cond === "") {
			count++;
		}
	}
	if (count == 10) {
		$.messager.alert('提示', "请至少选择一个问题要素!", "warning");
		return;
	}
	var returntxttype = $("#addtype").combobox("getValue");
	if (returntxttype === null || returntxttype === "") {
		$.messager.alert('提示', "请选择答案类型!", "warning");
		return;
	}
	var returntxt = $.trim($("#addreturntext").val());
	var data = {};
	if (insertorupdate_combition == 0) {
		data = {
			type : 'insertcombition',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : conditions.join("@"),
			returntxttype : returntxttype,
			returntxt : returntxt,
			abs : _abstract
		};
	} else {
		data = {
			type : 'updatecombition',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : conditions.join("@"),
			returntxttype : returntxttype,
			returntxt : returntxt,
			combitionid : $("#combitionid").val()
		};
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : data,
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#combitiondatagrid").datagrid("reload");
				clearCombitionForm();
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 对上传文件进行校验和上传
function uploadCombition() {
	// 得到上传文件的全路径
	var fileName = $('#fileuploadcombition').filebox('getValue');
	// 进行基本校验
	if (fileName == "") {
		$.messager.alert('提示', '请选择上传文件!', 'warning');
	} else {
		// 对文件格式进行校验
		var filetype = /\.[^\.]+$/.exec(fileName.toLowerCase());
		if (filetype == ".xls" || filetype == ".xlsx") {
			// 使表单成为ajax提交
			$("#uploadcombitionform").form(
					"submit",
					{
						url : "../file/upload",
						success : function(data) {
							var info = $.parseJSON(data);
							var state = info["state"];
							if (state == "success") {
								var name = info["names"][0];
								importExcelCombition(name);
							} else {
								$.messager.alert('提示', info["message"]
										+ " 请重新上传!", 'warning');
							}
							$('#fileuploadcombition').filebox('setValue', '');
						}
					});
		} else {
			var alertinfo = "上传的文件格式不正确,请选择<br/>";
			alertinfo += "1997-2003Excel文件(*.xls)<br/>";
			alertinfo += "或者是2007Excel文件(*.xlsx)!";
			$.messager.alert('信息提示', alertinfo, 'warning');
			$('#fileuploadcombition').filebox('setValue', '');
		}
	}
}

// 将Excel文件中的数据导入到数据库中
function importExcelCombition(name) {
	$.ajax( {
		type : "post",
		url : "../queryelement.action",
		data : {
			type : "importxls",
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			filename : name,
			importtype : 0
		},
		async : false,
		dataType : "json",
		timeout : 1800000,
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#combitiondatagrid").datagrid("reload");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 读取数据库并生成Excel2003文件，并提供下载
function exportExcelCombition() {
	$.ajax( {
		type : "post",
		url : "../queryelement.action",
		data : {
			type : 'exportxls',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			exporttype : 0,
			abs : _abstract
		},
		async : false,
		dataType : "json",
		timeout : 180000,
		success : function(data, textStatus, jqXHR) {
			if (data.success == true) {
				location = "../file/download?filename=" + data.path;
			} else {
				$.messager.alert('提示', "下载文件失败!", "warning");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 加载缺失补全规则的查询和添加下拉框
function loadRule0Combobox() {
	for ( var i = 1; i < 11; i++) {
		$("#selrule0name" + i).html("");
		$("#selrule0" + i).hide();
		$("#addrule0name" + i).html("");
		$("#addrule0" + i).hide();
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : 'queryelement',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			kbanswerid : kbanswerid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			var info = data.rows;
			rule0Arr = [];
			rule0Arr.push( {
				field : 'ck',
				checkbox : true
			});
			for ( var i = 0; i < info.length; i++) {
				var name = info[i]["name"];
				var weight = info[i]["weight"];
				var elementvalue = info[i]["elementvalue"];
				$("#selrule0name" + weight).html(name + ":");
				$("#selrule0" + weight).show();
				$("#selrule0value" + weight).combobox( {
					valueField : 'id',
					textField : 'text',
					editable : false,
					data : elementvalue
				});
				$("#selrule0value" + weight).combobox("setText", "");
				$("#addrule0name" + weight).html(name + ":");
				$("#addrule0" + weight).show();
				$("#addrule0value" + weight).combobox( {
					valueField : 'id',
					textField : 'text',
					editable : false,
					data : elementvalue
				});
				rule0Arr.push( {
					field : "condition" + weight,
					title : name,
					align : 'center'
				});
			}
			rule0Arr.push( {
				field : "type",
				title : "规则类型",
				align : 'center',
				formatter : function(value, row, index) {
					if (value == '0') {
						return '缺失补全规则';
					} else if (value == '1') {
						return '问题要素冲突判断规则';
					} else {
						return '其他规则';
					}
				}
			});
			rule0Arr.push( {
				field : "weight",
				title : "规则优先级",
				align : 'center'
			});
			rule0Arr
					.push( {
						field : "response",
						title : "回复内容",
						width : 300,
						formatter : function(value, row, index) {
							if (value != null) {
								value = value.replace(/</g, "&lt;").replace(
										/>/g, "&gt;");
								return "<div title='" + value + "'>" + value
										+ "</div>";
							} else {
								return value;
							}
						}
					});
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
	var typedata = [ {
		"id" : "0",
		"text" : "缺失补全规则"
	}, {
		"id" : "1",
		"text" : "问题要素冲突判断规则"
	}, {
		"id" : "2",
		"text" : "其他规则"
	} ];
	$("#selrule0type").combobox( {
		valueField : 'id',
		textField : 'text',
		editable : false,
		panelHeight : 'auto',
		data : typedata,
		value : "0"
	});
	$("#selrule0type").combobox("disable");
	$("#addrule0type").combobox( {
		valueField : 'id',
		textField : 'text',
		editable : false,
		panelHeight : 'auto',
		data : typedata,
		value : "0"
	});
	$("#addrule0type").combobox("disable");
	clearRule0Form();
}

// 查询缺失补全规则的信息
function searchRule0() {
	var conditions = [];
	for ( var i = 1; i < 11; i++) {
		var con = $("#selrule0value" + i).combobox("getText");
		if (con == "(空)") {
			con = "";
		}
		conditions.push(con);
	}
	var weight = $("#selrule0weight").numberbox("getValue");
	$('#rule0datagrid').datagrid('load', {
		type : 'selectrule',
		kbdataid : kbdataid,
		kbcontentid : kbcontentid,
		conditions : conditions.join("@"),
		ruletype : 0,
		weight : weight
	});
}

// 加载缺失补全规则的列表
function loadRule0() {
	$("#rule0datagrid").datagrid( {
		url : '../queryelement.action',
		height : 335,
		pagination : true,
		rownumbers : true,
		queryParams : {
			type : 'selectrule',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : "",
			ruletype : 0,
			weight : ""
		},
		pageSize : 10,
		striped : true,
		nowrap : true,
		fitColumns : true,
		singleSelect : false,
		columns : [ rule0Arr ]
	});
	$("#rule0datagrid").datagrid('getPager').pagination( {
		showPageList : false,
		buttons : [ {
			text : "新增",
			iconCls : "icon-add",
			handler : function() {
			$('#w').window('open');
				clearRule0Form(); 
			}
		}, "-", {
			text : "修改",
			iconCls : "icon-edit",
			handler : function() {
				editRule0();
			}
		}, "-", {
			text : "删除(批量)",
			iconCls : "icon-remove",
			handler : function() {
				deleteRule0();
			}
		}, "-" ]
	});
}

// 修改缺失补全规则操作，将值放入编辑区
function editRule0() {
	var rows = $("#rule0datagrid").datagrid("getSelections");
	if (rows.length == 1) {
		for ( var i = 1; i < rule0Arr.length - 3; i++) {
			var field = rule0Arr[i].field;
			var con = rows[0][field];
			var weight = field.replace("condition", "");
			$("#addrule0value" + weight).combobox("setValue", con);
		}

		$("#addrule0weight").numberbox("setValue", rows[0].weight);
		$("#addrule0response").val(rows[0].response);
		$("#rule0id").val(rows[0].id);
		insertorupdate_rule0 = 1;
	} else {
		$.messager.alert('提示', "请选择一行!", "warning");
		return;
	}
}

// 删除(批量)缺失补全规则
function deleteRule0() {
	var ruleid = [];
	var rows = $("#rule0datagrid").datagrid("getSelections");
	if (rows.length > 0) {
		for ( var i = 0; i < rows.length; i++) {
			ruleid.push(rows[i].id);
		}
	} else {
		$.messager.alert('提示', "请至少选择一行!", "warning");
		return;
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : 'deleterule',
			ruleid : ruleid.join(","),
			abs : _abstract
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#rule0datagrid").datagrid("reload");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 清空缺失补全规则表单
function clearRule0Form() {
	$('#rule0form').form('clear');
	$("#addrule0type").combobox("setValue", "0");
	$("#addrule0weight").numberbox("setValue", "");
	$("#addrule0response").focus();
	insertorupdate_rule0 = 0;
}

// 新增或修改缺失补全规则
function saveRule0() {
	var conditions = [];
	var count = 0;
	for ( var i = 1; i < 11; i++) {
		var cond = $("#addrule0value" + i).combobox("getText");
		if (cond == "(空)") {
			cond = "";
		}
		conditions.push(cond);
		if (cond === null || cond === "") {
			count++;
		}
	}
	if (count == 10) {
		$.messager.alert('提示', "请至少选择一个问题要素!", "warning");
		return;
	}
	var weight = $("#addrule0weight").numberbox("getValue");
	if (weight === null || weight === "") {
		$.messager.alert('提示', "请输入优先级!", "warning");
		return;
	}
	var response = $.trim($("#addrule0response").val());
	var data = {};
	if (insertorupdate_rule0 == 0) {
		data = {
			type : 'insertrule',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : conditions.join("@"),
			weight : weight,
			ruletype : 0,
			ruleresponse : response,
			abs : _abstract
		};
	} else {
		data = {
			type : "updaterule",
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : conditions.join("@"),
			weight : weight,
			ruletype : 0,
			ruleresponse : response,
			ruleid : $("#rule0id").val()
		};
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : data,
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#rule0datagrid").datagrid("reload");
				clearRule0Form();
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 更新业务规则
function updateRuleNLP() {
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : "updaterulenlp"
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 对上传文件进行校验和上传
function uploadRule0() {
	// 得到上传文件的全路径
	var fileName = $('#fileuploadrule0').filebox('getValue');
	// 进行基本校验
	if (fileName == "") {
		$.messager.alert('提示', '请选择上传文件!', 'warning');
	} else {
		// 对文件格式进行校验
		var filetype = /\.[^\.]+$/.exec(fileName.toLowerCase());
		if (filetype == ".xls" || filetype == ".xlsx") {
			// 使表单成为ajax提交
			$("#uploadrule0form").form(
					"submit",
					{
						url : "../file/upload",
						success : function(data) {
							var info = $.parseJSON(data);
							var state = info["state"];
							if (state == "success") {
								var name = info["names"][0];
								importExcelRule0(name);
							} else {
								$.messager.alert('提示', info["message"]
										+ " 请重新上传!", 'warning');
							}
							$('#fileuploadrule0').filebox('setValue', '');
						}
					});
		} else {
			var alertinfo = "上传的文件格式不正确,请选择<br/>";
			alertinfo += "1997-2003Excel文件(*.xls)<br/>";
			alertinfo += "或者是2007Excel文件(*.xlsx)!";
			$.messager.alert('信息提示', alertinfo, 'warning');
			$('#fileuploadrule0').filebox('setValue', '');
		}
	}
}

// 将Excel文件中的数据导入到数据库中
function importExcelRule0(name) {
	$.ajax( {
		type : "post",
		url : "../queryelement.action",
		data : {
			type : "importxls",
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			filename : name,
			importtype : 1
		},
		async : false,
		dataType : "json",
		timeout : 1800000,
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#rule0datagrid").datagrid("reload");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 读取数据库并生成Excel2003文件，并提供下载
function exportExcelRule0() {
	$.ajax( {
		type : "post",
		url : "../queryelement.action",
		data : {
			type : 'exportxls',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			exporttype : 1,
			abs : _abstract
		},
		async : false,
		dataType : "json",
		timeout : 180000,
		success : function(data, textStatus, jqXHR) {
			if (data.success == true) {
				location = "../file/download?filename=" + data.path;
			} else {
				$.messager.alert('提示', "下载文件失败!", "warning");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 加载问题要素冲突判断规则的查询和添加下拉框
function loadRule1Combobox() {
	for ( var i = 1; i < 11; i++) {
		$("#selrule1name" + i).html("");
		$("#selrule1" + i).hide();
		$("#addrule1name" + i).html("");
		$("#addrule1" + i).hide();
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : 'queryelement',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			kbanswerid : kbanswerid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			var info = data.rows;
			rule1Arr = [];
			rule1Arr.push( {
				field : 'ck',
				checkbox : true
			});
			for ( var i = 0; i < info.length; i++) {
				var name = info[i]["name"];
				var weight = info[i]["weight"];
				$("#selrule1name" + weight).html(name + ":");
				$("#selrule1" + weight).show();
				$("#selrule1value" + weight).textbox("setValue", "");
				$("#addrule1name" + weight).html(name + ":");
				$("#addrule1" + weight).show();
				$("#addrule1value" + weight).textbox("setValue", "");
				rule1Arr.push( {
					field : "condition" + weight,
					title : name,
					align : 'center'
				});
			}
			rule1Arr.push( {
				field : "type",
				title : "规则类型",
				align : 'center',
				formatter : function(value, row, index) {
					if (value == '0') {
						return '缺失补全规则';
					} else if (value == '1') {
						return '问题要素冲突判断规则';
					} else {
						return '其他规则';
					}
				}
			});
			rule1Arr.push( {
				field : "weight",
				title : "规则优先级",
				align : 'center'
			});
			rule1Arr
					.push( {
						field : "response",
						title : "回复内容",
						width : 300,
						formatter : function(value, row, index) {
							if (value != null) {
								value = value.replace(/</g, "&lt;").replace(
										/>/g, "&gt;");
								return "<div title='" + value + "'>" + value
										+ "</div>";
							} else {
								return value;
							}
						}
					});
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
	var typedata = [ {
		"id" : "0",
		"text" : "缺失补全规则"
	}, {
		"id" : "1",
		"text" : "问题要素冲突判断规则"
	}, {
		"id" : "2",
		"text" : "其他规则"
	} ];
	$("#selrule1type").combobox( {
		valueField : 'id',
		textField : 'text',
		editable : false,
		panelHeight : 'auto',
		data : typedata,
		value : "1"
	});
	$("#selrule1type").combobox("disable");
	$("#addrule1type").combobox( {
		valueField : 'id',
		textField : 'text',
		editable : false,
		panelHeight : 'auto',
		data : typedata,
		value : "1"
	});
	$("#addrule1type").combobox("disable");
	clearRule1Form();
}

// 查询问题要素冲突判断规则的信息
function searchRule1() {
	var conditions = [];
	for ( var i = 1; i < 11; i++) {
		var con = $("#selrule1value" + i).textbox("getValue");
		conditions.push(con);
	}
	var weight = $("#selrule1weight").numberbox("getValue");
	$('#rule1datagrid').datagrid('load', {
		type : 'selectrule',
		kbdataid : kbdataid,
		kbcontentid : kbcontentid,
		conditions : conditions.join("@"),
		ruletype : 1,
		weight : weight
	});
}

// 加载问题要素冲突判断规则的列表
function loadRule1() {
	$("#rule1datagrid").datagrid( {
		url : '../queryelement.action',
		height : 335,
		pagination : true,
		rownumbers : true,
		queryParams : {
			type : 'selectrule',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : "",
			ruletype : 1,
			weight : ""
		},
		pageSize : 10,
		striped : true,
		nowrap : true,
		fitColumns : true,
		singleSelect : false,
		columns : [ rule1Arr ]
	});
	$("#rule1datagrid").datagrid('getPager').pagination( {
		showPageList : false,
		buttons : [ {
			text : "新增",
			iconCls : "icon-add",
			handler : function() {
				clearRule1Form();
			}
		}, "-", {
			text : "修改",
			iconCls : "icon-edit",
			handler : function() {
				editRule1();
			}
		}, "-", {
			text : "删除(批量)",
			iconCls : "icon-remove",
			handler : function() {
				deleteRule1();
			}
		}, "-" ]
	});
}

// 修改问题要素冲突判断规则操作，将值放入编辑区
function editRule1() {
	var rows = $("#rule1datagrid").datagrid("getSelections");
	if (rows.length == 1) {
		for ( var i = 1; i < rule1Arr.length - 3; i++) {
			var field = rule1Arr[i].field;
			var con = rows[0][field];
			var weight = field.replace("condition", "");
			$("#addrule1value" + weight).textbox("setValue", con);
		}

		$("#addrule1weight").numberbox("setValue", rows[0].weight);
		$("#addrule1response").val(rows[0].response);
		$("#rule1id").val(rows[0].id);
		insertorupdate_rule1 = 1;
	} else {
		$.messager.alert('提示', "请选择一行!", "warning");
		return;
	}
}

// 删除(批量)问题要素冲突判断规则
function deleteRule1() {
	var ruleid = [];
	var rows = $("#rule1datagrid").datagrid("getSelections");
	if (rows.length > 0) {
		for ( var i = 0; i < rows.length; i++) {
			ruleid.push(rows[i].id);
		}
	} else {
		$.messager.alert('提示', "请至少选择一行!", "warning");
		return;
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : 'deleterule',
			ruleid : ruleid.join(","),
			abs : _abstract
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#rule1datagrid").datagrid("reload");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 清空问题要素冲突判断规则表单
function clearRule1Form() {
	$('#rule1form').form('clear');
	$("#addrule1type").combobox("setValue", "1");
	$("#addrule1weight").numberbox("setValue", "");
	$("#addrule1response").focus();
	insertorupdate_rule1 = 0;
}

// 新增或修改问题要素冲突判断规则
function saveRule1() {
	var conditions = [];
	var count = 0;
	for ( var i = 1; i < 11; i++) {
		var cond = $.trim($("#addrule1value" + i).textbox("getValue"));
		conditions.push(cond);
		if (cond === null || cond === "") {
			count++;
		}
	}
	if (count == 10) {
		$.messager.alert('提示', "请至少填写一个问题要素!", "warning");
		return;
	}
	var weight = $("#addrule1weight").numberbox("getValue");
	if (weight === null || weight === "") {
		$.messager.alert('提示', "请输入优先级!", "warning");
		return;
	}
	var response = $.trim($("#addrule1response").val());
	var data = {};
	if (insertorupdate_rule1 == 0) {
		data = {
			type : 'insertrule',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : conditions.join("@"),
			weight : weight,
			ruletype : 1,
			ruleresponse : response,
			abs : _abstract
		};
	} else {
		data = {
			type : "updaterule",
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : conditions.join("@"),
			weight : weight,
			ruletype : 1,
			ruleresponse : response,
			ruleid : $("#rule1id").val()
		};
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : data,
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#rule1datagrid").datagrid("reload");
				clearRule1Form();
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 对上传文件进行校验和上传
function uploadRule1() {
	// 得到上传文件的全路径
	var fileName = $('#fileuploadrule1').filebox('getValue');
	// 进行基本校验
	if (fileName == "") {
		$.messager.alert('提示', '请选择上传文件!', 'warning');
	} else {
		// 对文件格式进行校验
		var filetype = /\.[^\.]+$/.exec(fileName.toLowerCase());
		if (filetype == ".xls" || filetype == ".xlsx") {
			// 使表单成为ajax提交
			$("#uploadrule1form").form(
					"submit",
					{
						url : "../file/upload",
						success : function(data) {
							var info = $.parseJSON(data);
							var state = info["state"];
							if (state == "success") {
								var name = info["names"][0];
								importExcelRule1(name);
							} else {
								$.messager.alert('提示', info["message"]
										+ " 请重新上传!", 'warning');
							}
							$('#fileuploadrule1').filebox('setValue', '');
						}
					});
		} else {
			var alertinfo = "上传的文件格式不正确,请选择<br/>";
			alertinfo += "1997-2003Excel文件(*.xls)<br/>";
			alertinfo += "或者是2007Excel文件(*.xlsx)!";
			$.messager.alert('信息提示', alertinfo, 'warning');
			$('#fileuploadrule1').filebox('setValue', '');
		}
	}
}

// 将Excel文件中的数据导入到数据库中
function importExcelRule1(name) {
	$.ajax( {
		type : "post",
		url : "../queryelement.action",
		data : {
			type : "importxls",
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			filename : name,
			importtype : 2
		},
		async : false,
		dataType : "json",
		timeout : 1800000,
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#rule1datagrid").datagrid("reload");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 读取数据库并生成Excel2003文件，并提供下载
function exportExcelRule1() {
	$.ajax( {
		type : "post",
		url : "../queryelement.action",
		data : {
			type : 'exportxls',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			exporttype : 2,
			abs : _abstract
		},
		async : false,
		dataType : "json",
		timeout : 180000,
		success : function(data, textStatus, jqXHR) {
			if (data.success == true) {
				location = "../file/download?filename=" + data.path;
			} else {
				$.messager.alert('提示', "下载文件失败!", "warning");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

function exportWordpatAndQuery(flag){
	// 隐藏菜单
	$('#mm').menu('hide');
	$('#mm2').menu('hide');
	// 采用jquery easyui loading css效果
	var ajaxLoading = function(message) { 
		$("<div class=\"datagrid-mask\"></div>").css({display:"block",width:"100%",height:$(window).height()}).appendTo("body");
	    $("<div class=\"datagrid-mask-msg\"></div>").html("<span style='font-size:12px;'>"+message+"</span>").appendTo("body").css({display:"block",left:($(document.body).outerWidth(true) - 190) / 2,top:($(window).height() - 45) / 2});
	};
	ajaxLoading("正在生成文件...");
	var ajaxLoaded = function() {   
		$(".datagrid-mask").remove();   
		$(".datagrid-mask-msg").remove();               
	};
	var node = $('#tt').tree('getSelected');
	$.ajax({
		url: '../querymanage.action',
		type: "post",
		data: {
			type: 'exportwordpatandquery',
			serviceid: node.id,
			flag:flag
		},
		async: false,
		dataType: "json",
		timeout : 180000,
		success: function(data, textStatus, jqXHR) {
			ajaxLoaded();
			if (data.success == true) {
				// 开始下载文件
				$('#down_form').form('submit', {
					url: '../querymanageexport.action',
					queryParams: {
						fileName: data.fileName,
						type:'wordpatexport'
					},
					success: function(data) {
						if (!data && data.trim() !== "") {
							var data = eval('(' + data + ')');
							if (!data.success) {
								$.messager.alert('系统提示', data.msg, "warning");
							}
						}
					},
					onLoadError: function() {
						$.messager.alert('系统提示', '系统内部错误', "warning");
					}
				});
			} else {
				$.messager.alert('系统提示', data.msg, "warning");
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统提示', '系统错误', "warning");
			ajaxLoaded();
		}
	});
}

// 加载其他规则的下拉框
function loadRule2Combobox() {
	var typedata = [ {
// "id" : "0",
// "text" : "缺失补全规则"
// }, {
// "id" : "1",
// "text" : "问题要素冲突判断规则"
// }, {
		"id" : "2",
		"text" : "其他规则"
	}, {
		"id" : "3",
		"text" : "识别规则"
	} ];
	$("#addrule2type").combobox( {
		valueField : 'id',
		textField : 'text',
		editable : false,
		panelHeight : 'auto',
		data : typedata  
	// value : "2"
	});
	// $("#addrule2type").combobox("disable");
	clearRule2Form();
}

// 加载其他规则的列表
function loadRule2() {
	$("#rule2datagrid").datagrid( {
		url : '../queryelement.action',
		height : 335,
		pagination : true,
		rownumbers : true,
		queryParams : {
			type : 'selectrule',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : "",
			ruletype : 2,
			weight : ""
		},
		pageSize : 10,
		striped : true,
		nowrap : true,
		fitColumns : true,
		singleSelect : false,
		columns : [ [ {
			field : 'ck',
			checkbox : true
		}, {
			field : "type",
			title : "规则类型",
			align : 'center',
			formatter : function(value, row, index) {
				if (value == '0') {
					return '缺失补全规则';
				} else if (value == '1') {
					return '问题要素冲突判断规则';
				} else if (value == '2'){
					return '其他规则'; 
				} else {
					return '识别规则';
				}
			}
		}, {
			field : "weight",
			title : "规则优先级",
			align : 'center'
		}, {
			field : "response",
			title : "回复内容",
			width : 300,
			formatter : function(value, row, index) {
				if (value != null) {
					value = value.replace(/</g, "&lt;").replace(/>/g, "&gt;");
					return "<div title='" + value + "'>" + value + "</div>";
				} else {
					return value;
				}
			}
		} ] ]
	});
	$("#rule2datagrid").datagrid('getPager').pagination( {
		showPageList : false,
		buttons : [ {
			text : "新增",
			iconCls : "icon-add",
			handler : function() {
				clearRule2Form();
			}
		}, "-", {
			text : "修改",
			iconCls : "icon-edit",
			handler : function() {
				editRule2();
			}
		}, "-", {
			text : "删除(批量)",
			iconCls : "icon-remove",
			handler : function() {
				deleteRule2();
			}
		}, "-" ]
	});
}

// 修改其他规则操作，将值放入编辑区
function editRule2() {
	var rows = $("#rule2datagrid").datagrid("getSelections");
	if (rows.length == 1) {
		$("#addrule2weight").numberbox("setValue", rows[0].weight);
		var str = rows[0].response;
		var info = [];
		if (str.indexOf("==>") > -1) {
			info = str.split("==>");
			$("#addrule2qianti").textbox("setValue", info[0]);
			$("#addrule2response").val(info[1]);
		} else {
			$("#addrule2qianti").textbox("setValue", "");
			$("#addrule2response").val(str);
		}
		$("#rule2id").val(rows[0].id);
		insertorupdate_rule2 = 1;
	} else {
		$.messager.alert('提示', "请选择一行!", "warning");
		return;
	}
}

// 删除(批量)其他规则
function deleteRule2() {
	var ruleid = [];
	var rows = $("#rule2datagrid").datagrid("getSelections");
	if (rows.length > 0) {
		for ( var i = 0; i < rows.length; i++) {
			ruleid.push(rows[i].id);
		}
	} else {
		$.messager.alert('提示', "请至少选择一行!", "warning");
		return;
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : 'deleterule',
			ruleid : ruleid.join(","),
			abs : _abstract
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#rule2datagrid").datagrid("reload");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 清空其他规则表单
function clearRule2Form() {
	$('#rule2form').form('clear');
	$("#addrule2type").combobox("setValue", "");
	$("#addrule2response").focus();
	insertorupdate_rule2 = 0;
}

// 新增或修改其他规则
function saveRule2() {
	var ruletype = $("#addrule2type").combobox("getValue");
	var qianti = $.trim($("#addrule2qianti").textbox("getValue"));
	if (qianti === null || qianti === "") {
		$.messager.alert('提示', "请填写规则前提!", "warning");
		return;
	}
	var weight = $("#addrule2weight").numberbox("getValue");
	if (weight === null || weight === "") {
		$.messager.alert('提示', "请输入优先级!", "warning");
		return;
	}
	var response = $.trim($("#addrule2response").val());
	var data = {};
	if (insertorupdate_rule2 == 0) {
		data = {
			type : 'insertrule',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : "",
			weight : weight,
			ruletype : ruletype,
			ruleresponse : qianti + "==>" + response,
			abs : _abstract
		};
	} else {
		data = {
			type : "updaterule",
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : "",
			weight : weight,
			ruletype : ruletype,
			ruleresponse : qianti + "==>" + response,
			ruleid : $("#rule2id").val()
		};
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : data,
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#rule2datagrid").datagrid("reload");
				clearRule2Form();
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}



function myKeyDown()
{
var k=window.event.keyCode;
if (8 == k)
{
event.keyCode=0;// 取消按键操作
}
}

function openWordFileWin(){
	$('#wordfileuploadtxt').filebox('clear');
	$('#word_upload').window('open');
}

// 上传词类词条的excel
function uploadWordFile(){
	// 得到上传文件的全路径
	var fileName = $('#wordfileuploadtxt').filebox('getValue');
	// 进行基本校验
	if (fileName == "") {
		$.messager.alert('提示', '请选择上传文件!', 'warning');
	} else {
		// 对文件格式进行校验
		var filetype = /\.[^\.]+$/.exec(fileName.toLowerCase());
		if (filetype == ".xls" || filetype == ".xlsx") {
			// 使表单成为ajax提交
			$("#formUploadWordFile").form(
					"submit",
					{
						url : "../file/upload?path=qatraining/regresstest",
						success : function(data) {
							var info = $.parseJSON(data);
							var state = info["state"];
							if (state == "success") {
								var name = info["names"][0];
								importWordExcel(name);
							} else {
								$.messager.alert('提示', info["message"]
										+ " 请重新上传!", 'warning');
							}
							$('#wordfileuploadtxt').filebox('setValue', '');
						}
					});
		} else {
			var alertinfo = "上传的文件格式不正确,请选择<br/>";
			alertinfo += "1997-2003Excel文件(*.xls)<br/>";
			alertinfo += "或者是2007Excel文件(*.xlsx)!";
			$.messager.alert('信息提示', alertinfo, 'warning');
			$('#wordfileuploadtxt').filebox('setValue', '');
		}
	}
}

// 将Excel文件中的数据导入到数据库中
function importWordExcel(name) {
	$.ajax( {
		type : "post",
		url : "../querymanage.action",
		data : {
			type : "importwordxls",
			filename : name
		},
		async : false,
		dataType : "json",
		timeout : 1800000,
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$('#word_upload').window("close");
				$("#combitiondatagrid").datagrid("reload");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

function uploadFAQFile(){
	var uploadfaqserviceid = $('#faqnode').textbox('getValue');
	var uploadfaqservice = $('#faqnode').textbox('getText');
	// 得到上传文件的全路径
	var fileName = $('#faqfileuploadtxt').filebox('getValue');
	// 进行基本校验
	if (fileName == "") {
		$.messager.alert('提示', '请选择上传文件!', 'warning');
	} else {
		// 对文件格式进行校验
		var filetype = /\.[^\.]+$/.exec(fileName.toLowerCase());
		if (filetype == ".xls" || filetype == ".xlsx") {
			$.messager.progress({
				title:'请稍等',
				msg:'正在上传中...'
				});
			// 使表单成为ajax提交
			$("#formUploadFAQFile").form(
					"submit",
					{
						url : "../file/upload?path=qatraining/regresstest",
						success : function(data) {
							var info = $.parseJSON(data);
							var state = info["state"];
							if (state == "success") {
								var name = info["names"][0];
								importFaqExcel(name,uploadfaqserviceid,uploadfaqservice);
								$.messager.progress('close');
							} else {
								$.messager.alert('提示', info["message"]
										+ " 请重新上传!", 'warning');
							}
							$('#faqfileuploadtxt').filebox('setValue', '');
						}
					});
		} else {
			var alertinfo = "上传的文件格式不正确,请选择<br/>";
			alertinfo += "1997-2003Excel文件(*.xls)<br/>";
			alertinfo += "或者是2007Excel文件(*.xlsx)!";
			$.messager.alert('信息提示', alertinfo, 'warning');
			$('#faqfileuploadtxt').filebox('setValue', '');
		}
	}
}

// 将Excel文件中的数据导入到数据库中
function importFaqExcel(name,uploadfaqserviceid,uploadfaqservice) {
	$.ajax( {
		type : "post",
		url : "../querymanage.action",
		data : {
			type : "importfaqxls",
			filename : name,
			serviceid : uploadfaqserviceid,
			service : uploadfaqservice,
			resourcetype : 'querymanage',
			operationtype : 'A',
			resourceid : uploadfaqserviceid
		},
		async : false,
		dataType : "json",
		timeout : 1800000,
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#faq_upload").window("close");
				opentab("【" + uploadfaqservice + "】",uploadfaqserviceid);
// $("#combitiondatagrid").datagrid("reload");
			}
		}
	});
}
function uploadKBFile(){
	var node = $('#tt').tree('getSelected');
	var serviceid = node.id;
	// 采用jquery easyui loading css效果
	var ajaxLoading = function(message) { 
		$("<div class=\"datagrid-mask\"></div>").css({display:"block",width:"100%",height:$(window).height(), 'z-index':'10000'}).appendTo("body");
	    $("<div class=\"datagrid-mask-msg\"></div>").html("<span style='font-size:12px;'>"+message+"</span>").appendTo("body").css({'z-index':'10000',display:"block",left:($(document.body).outerWidth(true) - 190) / 2,top:($(window).height() - 45) / 2});
	};
	var ajaxLoaded = function() {   
		$(".datagrid-mask").remove();   
		$(".datagrid-mask-msg").remove();               
	};
	

	// 得到上传文件的全路径
	var fileName = $('#kbfileuploadtxt').filebox('getValue');
	// 进行基本校验
	if (fileName == "") {
		$.messager.alert('提示', '请选择上传文件!', 'warning');
	} else {
		// 对文件格式进行校验
		var filetype = /\.[^\.]+$/.exec(fileName.toLowerCase());
		if (filetype == ".xls" || filetype == ".xlsx") {
			ajaxLoading("正在上传中...");
			// 使表单成为ajax提交
			$("#formUploadKBFile").form(
					"submit",
					{
						url : "../file/upload?path=qatraining/regresstest",
						success : function(data) {
							ajaxLoaded();
							var info = $.parseJSON(data);
							var state = info["state"];
							if (state == "success") {
								var name = info["names"][0];
								importKBExcel(name,serviceid);
								$.messager.progress('close');
							} else {
								$.messager.alert('提示', info["message"]
										+ " 请重新上传!", 'warning');
							}
							$('#kbfileuploadtxt').filebox('setValue', '');
						},
						error: function(jqXHR, textStatus, errorThrown) {
							$.messager.alert('系统提示', '系统错误', "warning");
							ajaxLoaded();
						}
					});
		} else {
			var alertinfo = "上传的文件格式不正确,请选择<br/>";
			alertinfo += "1997-2003Excel文件(*.xls)<br/>";
			alertinfo += "或者是2007Excel文件(*.xlsx)!";
			$.messager.alert('信息提示', alertinfo, 'warning');
			$('#kbfileuploadtxt').filebox('setValue', '');
		}
	}
}

// 将Excel文件中的数据导入到数据库中
function importKBExcel(name,serviceid) {
	// 采用jquery easyui loading css效果
	var ajaxLoading = function(message) { 
		$("<div class=\"datagrid-mask\"></div>").css({display:"block",width:"100%",height:$(window).height(), 'z-index':'10000'}).appendTo("body");
	    $("<div class=\"datagrid-mask-msg\"></div>").html("<span style='font-size:12px;'>"+message+"</span>").appendTo("body").css({'z-index':'10000',display:"block",left:($(document.body).outerWidth(true) - 190) / 2,top:($(window).height() - 45) / 2});
	};
	var ajaxLoaded = function() {   
		$(".datagrid-mask").remove();   
		$(".datagrid-mask-msg").remove();               
	};
	
	ajaxLoading("正在处理文件中...");
	$.ajax( {
		type : "post",
		url : "../querymanage.action",
		data : {
			type : "importkb",
			filename : name,
			serviceid : serviceid,
//			resourcetype : 'querymanage',
//			operationtype : 'A',
//			resourceid : serviceid
		},
		async : false,
		dataType : "json",
		timeout : 1800000,
		success : function(data, textStatus, jqXHR) {
			ajaxLoaded();
			if (data.success == true) {
				$("#kb_upload").window("close");
				$.messager.alert('提示', data.msg, "info");
			}else{
				if(data.errorCode && data.errorCode == '1'){
					$.messager.alert('提示', '导入的词模数量超出限制！', "warning");
				}else if(data.errorCode && data.errorCode == '2'){
					$.messager.alert('提示', '导入的客户问数量超出限制！', "warning");
				}else if(data.fileName){
					$.messager.confirm('提示','导入文件未通过检查，是否下载检查报告。',function(r){
						if(r){
							// 开始下载文件
							$('#down_form').form('submit', {
								url: '../querymanageexport.action',
								queryParams: {
									fileName: data.fileName,
									type:'wordpatexport'
								},
								success: function(data) {
									if (!data && data.trim() !== "") {
										var data = eval('(' + data + ')');
										if (!data.success) {
											$.messager.alert('系统提示', data.msg, "warning");
										}
									}
								},
								onLoadError: function() {
									$.messager.alert('系统提示', '系统内部错误', "warning");
								}
							});
						}
					});
					
//					var downloadUrl = '';
//					downloadUrl = '</br>导入文件检查报告：</br>'
//						+'<a href="../querymanageexport.action?type=wordpatexport&fileName='+data.fileName
//						+'" download="'+data.fileName+'" title="下载" >'+data.fileName+'</a>';
//					$.messager.alert('系统提示', downloadUrl, "warning");
				}else{
					$.messager.alert('提示', data.msg, "warning");
				}
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统提示', '系统错误', "warning");
			ajaxLoaded();
		}
	});
}