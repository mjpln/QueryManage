var returnvalues;
var normalquery;
var serviceid;
var kbdata;
var kbdataid;
var gCitySelect;
var gHookOpts;
var customItem = {};
var showInTab = false;//是否已标签页形式展示
var isClickCustomer = false;//判断是点击的行还是互斥问题
//词模返回值
var returnvalues =[];
$(function() {
	var urlparams = new UrlParams(); // 所有url参数
	normalquery = decodeURI(urlparams.normalquery);
	gCitySelect = decodeURI(urlparams.citycode);
	serviceid = decodeURI(urlparams.serviceid);
	gHookOpts = parseHook(urlparams.hook);
	var userid = decodeURI(urlparams.userid);
	var ioa = decodeURI(urlparams.ioa);
	var customerquery = decodeURI(urlparams.customerquery);
	var wordpatreturn_values = decodeURI(urlparams.returnvalues);
	createUserInfo(userid,ioa);
	if(normalquery == null || normalquery == 'undefined'){
		normalquery = '';
	}
	if(serviceid == null || serviceid == 'undefined'){
		serviceid = '';
	}
	$(".normalquery").html(normalquery);
	
	//initCustom();
	//初始化词模返回值
	getReturnValue(wordpatreturn_values);
	//新增扩展问
	getNormalQuery(customerquery);
	//加载客户问题
	loadCustomerQuerydDatagridList(gHookOpts);
	// 初始化检索Panel
	initFilterPanel(gHookOpts);
	if(serviceid != null && serviceid != ''){
		//添加问题类型onselect 事件
		queryTypeOnSelect();
		//创建selectcitytree
		createCityTree('cityselect', 'edit', false);
		createCityTree('editcustomerquerycity', 'edit', true);
		createCityTree('removequerycityselect', 'edit', false);		
	}

});
//组装词模返回值
function getReturnValue(values){
	
	if(values == null || values == 'undefined'){
		returnvalues = '';
	}
	if(values != null && values != ''){
		var returnvalue = values.split("@@");
		for(var i=0;i<returnvalue.length;i++){
			var returnvalueCon = returnvalue[i].split("#");
			returnvalues.push(returnvalueCon[0]+'="'+returnvalueCon[1]+'"');
		}		
	}		
	
}
//创建用户信息
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
			}else{
				$.messager.alert('系统异常', "请求数据失败!", "error");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {

		}
	});
}
//新增扩展问
function getNormalQuery(customerquery){
	if(customerquery == null || customerquery == 'undefined'){
		customerquery = '';
	}
	$.ajax( { 
		url : '../querymanage.action',
		type : "post",
		data : {
			type : 'getNormalQuery',
			normalquery: normalquery,
			citycode: '全国',
			serviceid:serviceid,
			returnValue:returnvalues.join("&"),
			customerquery:customerquery
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if(data.success){
				kbdata = data.kbdata;
				serviceid = kbdata.serviceid;
				kbdataid = kbdata.kbdataid;
				var oovWord = data.oovWord;
				if (oovWord != null && oovWord != '') {
					//标准问的原分词
					var segmentWord = data.segmentWord;
					getOOVWord(oovWord, normalquery,segmentWord);
				}
			}else{
				$.messager.alert('系统提示', data.msg, 'warning');
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
		}
	});
	
}
//创建citytree
function createCityTree(id, flag, isMultiple) {
	var id = "#" + id;
	$(id).combotree({
		url: "../querymanage.action",
		editable: false,
		multiple: isMultiple,
		queryParams: {
			type: "createcitytreebylogininfo",
			flag: flag
		}
	});
}

function parseHook(hook) {
	var options = {};
	if (hook) {
		try {
			var hookObj = JSON.parse(decodeURI(hook))
				//			var normalquery = options.panel1.normalquery;
				//			var replytype = options.panel1.replytype;
				//			var interacttype = options.panel1.interacttype;
				//			var dosearch = options.panel1.dosearch;
				//			var openSubPage = options.panel1.openSubPage;

		} catch (e) {
			console.error('hook参数不是对象，请检查！')
		}
	}
	return hookObj;
}
//构造业务树形图
function createServiceTree(fid) {
	var fdocid = "#" + fid;
	$(fdocid).combotree({
		url: '../querymanage.action?type=createservicetree&a=' + Math.random(),
		editable: true,
		onBeforeExpand: function(node, param) {
			$(fdocid).combotree('tree').tree("options").url = "../querymanage.action?type=createservicetree&serviceid=" + node.id + '&a=' + Math.random();
		}
	});
}
//初始化检索条件Panel
function initFilterPanel(options) {
	$('#customerquery_panel').panel('open');
	$('#removequery_panel').panel('close');
	$('#datagrid_tb2').panel({
		width: 520,
		title: ' ',
		iconCls: '',
		headerCls: 'filterHeaderCls',
		bodyCls: 'filterBodyCls',
		collapsible: true,
		onCollapse: function() {
			// 重新设置datagrid高度
			$("#customerquerydatagrid").datagrid('resize', {
				height: 455
			});
		},
		onExpand: function() {
			// 重新设置datagrid高度
			$("#customerquerydatagrid").datagrid('resize', {
				height: 350
			});
		}
	}).panel('expand');
}

//创建Combobox
function createCombobox(id, url, multiple, editable, onLoadSuccess) {
	$('#' + id).combobox({
		url: url,
		valueField: 'id',
		textField: 'text',
		panelHeight: '150px',
		multiple: multiple, // 支持多选
		separator: ',', // 多选的时候用“,”分隔
		editable: editable,
		onLoadSuccess: onLoadSuccess
	});
}
//添加问题类型onselect 事件
function queryTypeOnSelect() {
	$("#querytype").combobox({
		editable: false,
		onSelect: function(rec) {
			var selectOption = rec.value;
			if (selectOption == "客户问题") {
				$("#choose_normalquerycombobox_div").show();
				$("#input_normalquerycombobox_div").hide();
				$("#").val("");
				createCityTree('customerquerycity', 'edit', true);
			} else {
				$("#choose_normalquerycombobox_div").hide();
				$("#input_normalquerycombobox_div").show();
				$("#customerquerytextarea").val("");
				createCityTree('customerquerycity', 'edit', true);
			}

		}

	});

}



//加载问题管理列表
function loadCustomerQuerydDatagridList(hookObj) {
	var params;
	if (!hookObj) {
		params = {
				type: "selectcustomerquery",
				serviceid: serviceid,
				kbdataid: kbdataid,
				normalquery: "",
				customerquery: "",
				citycode: "",
				istrain: "",
				understandstatus: ""
		};
	} else {
		params = {
			type: 'selectcustomerquery',
			serviceid: kbdata.serviceid,
			kbdataid: kbdata.kbdataid ,
			normalquery:  '',
			customerquery: hookObj.panel.customerquery ? hookObj.panel1.customerquery : '',
			citycode: '',
			istrain: '',
			understandstatus: ''
		}
	}
	
	$("#customerquerydatagrid")
		.datagrid({
			//						height : 400,
			height: 380,
			width: 520,
			url: "../querymanage.action",
			queryParams:params,
			//						toolbar : "#datagrid_tb2",
			pageSize: 50,
			pagination: true,
			rownumbers: true,
			striped: true,
			nowrap: false,
			fitColumns: true,
			singleSelect: false,

			loadMsg: "数据加载中,请稍后……",
			columns: [
				[{
						field: 'ck',
						checkbox: true
					}, {
						field: 'queryid',
						title: '客户问题ID',
						width: 180,
						hidden: true
					}, {
						field: 'customerquery',
						title: '客户问题',
						width: 400
					}, {
						field: 'cityname',
						title: '来源地市',
						align: 'center',
						width: 200
					}, {
						field: 'citycode',
						title: '来源地市',
						width: 200,
						hidden: true
					}, {
						field: 'normalquery',
						title: '标准问题',
						width: 200,
						hidden: true
					}, {
						field: 'kbdataid',
						title: '标准问ID',
						width: 180,
						hidden: true
					}, {
						field: 'abscity',
						title: '标准问地市',
						width: 180,
						hidden: true
					}, {
						field: 'abs',
						title: '摘要',
						width: 180,
						hidden: true
					}, {
						field: 'topic',
						title: '主题',
						width: 180,
						hidden: true
					}, {
						field: 'service',
						title: '业务',
						width: 180,
						hidden: true
					}, {
						field: 'brand',
						title: '品牌',
						width: 180,
						hidden: true
					}, {
						field: 'cc',
						title: '是否允许匹配<br/>过长问题',
						align: 'center',
						width: 200,
						hidden: true
					}, {
						field: 'status',
						title: '理解<br/>状态',
						align: 'center',
						width: 100,
						formatter: function(value, row, index) {
							if (value == '-2') {
								return '<span style="color:red">无结果</span>';
							} else if (value == '-1') {
								return '<span style="color:blue">不一致</span>';
							} else if (value == '0') {
								return '一致';
							}
						}
					}, {
						field: 'istrain',
						title: '是否<br/>训练',
						align: 'center',
						width: 70,
						formatter: function(value, row, index) {
							if (value == '是') {
								return '<span >是</span>';
							} else if (value == '否') {
								return '<span style="color:red">否</span>';
							}
						}
					}, {
						field: '操作',
						title: '操作',
						align: 'center',
						width: 180,
						formatter: function(value, row, index) {
							return '<!-- 隐藏共享语义 <a href="javascript:void(0)" title="共享语义提示：查看业务词（产品名、套餐名、业务名等）是否录入系统，如果录入，将能极大提高系统的理解精度。（特注，如不能理解业务词以及是否分词成功，请务必联系系统管理员进行理解。）同时查看系统是否有语义共享信息，如果有共享的语义，将省去我们扩展问题的工作。" class="icon-share btn_a" onclick="_openShareWin(event,' + index + ')"></a> --> <a href="javascript:void(0)" title="修改" class="icon-edit btn_a" onclick="openEditCustomerQueryWin2(' + index + ')"></a><a href="javascript:void(0)" title="理解详情" class="icon-search btn_a" onclick="openResultWin(' + index + ')"></a>';
						}
					}

				]
			]
		});

	$("#customerquerydatagrid").datagrid('getPager').pagination({
		showPageList: false,
		buttons: [{
				//			text : "新增",
				tooltip: "新增",
				iconCls: "icon-add",
				handler: function() {
					$('#add_city_div').show();
					$('#add_customerquery_div').show();
					
					
					$('#addquerywindow').window({
						title: '新增客户问题',
						height: 400
					}).window('open');
					
					$("#querytype").combobox('setValue', '客户问题');
					$("#normalqueryinput").val('');
					$("#choose_normalquerycombobox_div").show();
					$("#input_normalquerycombobox_div").hide();
					//创建客户问题citytree
					createCityTree('customerquerycity', 'edit', true);
					//创建标准问下拉框
					$("#normalquerycombobox").combobox('setValue',normalquery);

				}
			},
			"-", {
				//			text : "删除",
				iconCls: "icon-delete",
				handler: function() {
					deleteCustomerQuery();
				}
			}, "-", {
				text: "批量训练",
				iconCls: "icon-wordpat",
				handler: function() {
					//produceWordpat();
					preProduceWordpat();
				}
			}, "-", {
				//			text : "知识库更新",
				title: "知识库更新",
				iconCls: "icon-update",
				handler: function() {
					$.messager.confirm('更新知识库', '知识库更新可能需要一点时间哦，确认更新吗？', function(r) {
						if (r) {
							updateKbdata();
						}
					})
				}
			}, "-", {
				text: "批量理解",
				iconCls: "icon-answer",
				handler: function() {
					understand();
				}
			}

		]
	});

	var pagerOptions = $("#customerquerydatagrid").datagrid('getPager').pagination("options");
	var newButtons =[];
	$.each(pagerOptions.buttons, function(n, botton){
		
		if(customItem["批量训练"] != null && customItem["批量训练"] == "不显示"
			&& botton.text && botton.text =='批量训练'){
			return;
		}
		if(botton == '-' && newButtons[newButtons.length -1] == '-') {
			return;
		}
		newButtons.push(botton);
	});
	$("#customerquerydatagrid").datagrid('getPager').pagination({buttons:newButtons});
}
//共享语义
function openShareWin(event) {
	//	 $.messager.alert('提示', "Waiting for me to do!", "info");
	if (event.stopPropagation) { // Mozilla and Opera
		event.stopPropagation();
	} else if (window.event) { // IE
		window.event.cancelBubble = true;
	}

	var service = kbdata.service;
	var kbdataid = kbdata.kbdataid;
	var cityids = kbdata.abscity;
	var brand = kbdata.brand;
	var topic = kbdata.topic;
	var abstracts = kbdata.abs;
	var ioa = kbdata.ioa;
	var url = encodeURI('../extend/extend.jsp?service=' + service + '&serviceid=' + kbdata.serviceid + '&kbdataid=' + kbdataid + '&cityids=' + cityids + '&brand=' + brand + '&topic=' + topic + '&abstracts=' + abstracts + '&ioa=' + ioa + '&question=' + normalquery);
	if(showInTab){
		addTab('共享语义【' + normalquery + '】',url);
	}else{
		var con = '<iframe style="width:100%;height:98%;border:0;" src="' + url + '"></iframe>';
		$('#sharewin').html(con);
		$('#sharewin').window({
			title: '共享语义【' + normalquery + '】',
			collapsible: false,
			minimizable: false,
			maximizable: false,
			draggable: false,
			resizable: false
		}).window('open');
	}
}
//打开词模编辑页对话框
function openWordpatWin(event) {
	if (event.stopPropagation) { // Mozilla and Opera
		event.stopPropagation();
	} else if (window.event) { // IE
		window.event.cancelBubble = true;
	}

	var service = kbdata.service;
	var kbdataids = kbdata.kbdataid;
	var cityids = kbdata.abscity;
	var brand = kbdata.brand;
	var topic = kbdata.topic;
	var _abstract = kbdata.abs;
	
	var container = "";
	var next_abs_names = "";
	var abs_name = "";
	var pre_abs_name = "";
	var chartaction = "";
	var queryorresponse = "";
	var url = "../wordpat/app.html";
	
	//单独访问词模页面时需要添加userid和ioa用户创建用户信息
	//	url = url + '?userid='+userid+'&ioa='+ioa+'&wordpattype=0&serviceids=' + serviceid + '&cityids=' + cityids + '&brand=' + brand + '&service=' + encodeURI(service) + '&kbdataids=' + kbdataids + '&topic=' + topic + '&_abstract=' + encodeURI(_abstract) + '&container=' + encodeURI(container) + '&next_abs_names=' + encodeURI(next_abs_names) + '&abs_name=' + encodeURI(abs_name) + '&pre_abs_name=' + encodeURI(pre_abs_name) + '&chartaction=' + encodeURI(chartaction) + '&queryorresponse=' + encodeURI(queryorresponse);
	url = url + '?wordpattype=0&serviceids=' + serviceid + '&cityids=' + cityids + '&brand=' + brand + '&service=' + encodeURI(service) + '&kbdataids=' + kbdataids + '&topic=' + topic + '&_abstract=' + encodeURI(_abstract) + '&container=' + encodeURI(container) + '&next_abs_names=' + encodeURI(next_abs_names) + '&abs_name=' + encodeURI(abs_name) + '&pre_abs_name=' + encodeURI(pre_abs_name) + '&chartaction=' + encodeURI(chartaction) + '&queryorresponse=' + encodeURI(queryorresponse);
	if(showInTab){
		addTab('词模详情【' + _abstract.split(">")[1] + '】',url);
	}else{
		var con = '<iframe style="width:100%;height:98%;border:0;" src="' + url + '"></iframe>';
		$('#wordpatwin').html(con);
		$('#wordpatwin').window({
			title: '词模详情【' + _abstract.split(">")[1] + '】',
			collapsible: false,
			minimizable: false,
			maximizable: false,
			draggable: false,
			resizable: false
		}).window('open');
	}
	
}
function openCustomerQuery(){
	$('#customerquery_panel').panel('open');
	$('#removequery_panel').panel('close');
    $("#customerqueryselect").textbox("setValue", "");
    $("#cityselect").combotree("clear");
    $("#understandstatus").combobox("setValue", "");
    $("#istrain").combobox("setValue", "");
    $('#customerquerydatagrid').datagrid('load', {
	   type: "selectcustomerquery",
	   serviceid: serviceid,
	   kbdataid: kbdataid,
	   customerquery: "",
	   citycode: "",
	   istrain: "",
	   understandstatus: ""
	});
}
//打开排除问题
function openRemoveQuery(event) {
	isClickCustomer = true;
	$('#customerquery_panel').panel('close');
	$('#removequery_panel').panel('open');
	$('#removequery_datagrid_tb2').panel({
		width: 520,
		title: ' ',
		iconCls: '',
		headerCls: 'filterHeaderCls',
		bodyCls: 'filterBodyCls',
		collapsible: true,
		onCollapse: function() {
			// 重新设置datagrid高度
			$("#removequerydatagrid").datagrid('resize', {
				height: 455
			});
		},
		onExpand: function() {
			// 重新设置datagrid高度
			$("#removequerydatagrid").datagrid('resize', {
				height: 350
			});
		}
	}).panel('expand');
	// 加载排除问题管理列表
	loadRemoveQuerydDatagridList(gHookOpts);
//	$('#querymanagedatagrid').datagrid('selectRow', index);
}
//加载排除问题管理列表
function loadRemoveQuerydDatagridList(hookObj) {
	var params;
	
	if (!hookObj) {
		params = {
				type: "selectremovequery",
				serviceid: serviceid,
				kbdataid: kbdataid,
				normalquery: "",
				customerquery: "",
				citycode: "",
				istrain: "",
				removequerystatus: ""
		};
	} else {
		params = {
			type: 'selectremovequery',
			serviceid: serviceid,
			kbdataid: kbdataid,
			normalquery:  '',
			customerquery: hookObj.panel1.customerquery ? hookObj.panel1.customerquery : '',
			citycode: '',
			istrain: '',
			removequerystatus: ''
		}
	}
	
	$("#removequerydatagrid").datagrid({
		height: 380,
		width: 520,
		url: "../querymanage.action",
		queryParams:params,
		pageSize: 50,
		pagination: true,
		rownumbers: true,
		striped: true,
		nowrap: false,
		fitColumns: true,
		singleSelect: false,
		loadMsg: "数据加载中,请稍后……",
		columns: [
			[{
				field: 'ck',
				checkbox: true
			}, {
				field: 'queryid',
				title: '客户问题ID',
				width: 180,
				hidden: true
			}, {
				field: 'customerquery',
				title: '排除问题',
				width: 400
			}, {
				field: 'cityname',
				title: '来源地市',
				align: 'center',
				width: 200
			}, {
				field: 'citycode',
				title: '来源地市',
				width: 200,
				hidden: true
			}, {
				field: 'normalquery',
				title: '标准问题',
				width: 200,
				hidden: true
			}, {
				field: 'kbdataid',
				title: '标准问ID',
				width: 180,
				hidden: true
			}, {
				field: 'abscity',
				title: '标准问地市',
				width: 180,
				hidden: true
			}, {
				field: 'abs',
				title: '摘要',
				width: 180,
				hidden: true
			}, {
				field: 'topic',
				title: '主题',
				width: 180,
				hidden: true
			}, {
				field: 'service',
				title: '业务',
				width: 180,
				hidden: true
			}, {
				field: 'brand',
				title: '品牌',
				width: 180,
				hidden: true
			}, {
				field: 'cc',
				title: '是否允许匹配<br/>过长问题',
				align: 'center',
				width: 200,
				hidden: true
			}, {
				field: 'isstrictexclusion',
				title: '严格<br/>排除',
				align: 'center',
				width: 100,
				formatter: function(value, row, index) {
					if (value == '是') {
						return '<span >是</span>';
					} else if (value == '否') {
						return '<span style="color:red">否</span>';
					}
				}
			}, {
				field: 'istrain',
				title: '是否<br/>训练',
				align: 'center',
				width: 70,
				formatter: function(value, row, index) {
					if (value == '是') {
						return '<span >是</span>';
					} else if (value == '否') {
						return '<span style="color:red">否</span>';
					}
				}
			}]
		]
	});

	$("#removequerydatagrid").datagrid('getPager').pagination({
		showPageList: false,
		buttons: [{
			tooltip: "新增",
			iconCls: "icon-add",
			handler: function() {
				$('#add_city_div').show();
				$('#add_customerquery_div').show();
				
				$('#addremovequerywindow').window({
					title: '新增排除问题',
					height: 450
				}).window('open');
				
				$("#removequerytype").combobox('setValue', '排除问题');
				$("#removenormalqueryinput").val('');
				$("#removequerystatus").combobox('setValue','否');
				$("#removequerytextarea").val('');
				$("#choose_remove_normalquerycombobox_div").show();
				$("#input_remove_normalquerycombobox_div").hide();
				//创建客户问题citytree
				createCityTree('removequerycity', 'edit', true);
				//创建标准问下拉框
				$("#removenormalquerycombobox").combobox('setValue',normalquery);
			}
		}, "-", {
			iconCls: "icon-delete",
			handler: function() {
				deleteRemoveCustomerQuery();
			}
		}, "-", {
			text: "批量训练",
			iconCls: "icon-wordpat",
			handler: function() {
				preRemoveProduceWordpat();
			}
		}, "-", {
			title: "知识库更新",
			iconCls: "icon-update",
			handler: function() {
				$.messager.confirm('更新知识库', '知识库更新可能需要一点时间哦，确认更新吗？', function(r) {
					if (r) {
						updateKbdata();
					}
				})
			}
		}, "-", {
			text: "批量理解",
			iconCls: "icon-answer",
			handler: function() {
				removeunderstand();
			}
		}]
	});

	var pagerOptions = $("#removequerydatagrid").datagrid('getPager').pagination("options");
	var newButtons =[];
	$.each(pagerOptions.buttons, function(n, botton){
		if(customItem["批量训练"] != null && customItem["批量训练"] == "不显示"
			&& botton.text && botton.text =='批量训练'){
			return;
		}
		if(botton == '-' && newButtons[newButtons.length -1] == '-') {
			return;
		}
		newButtons.push(botton);
	});
	$("#removequerydatagrid").datagrid('getPager').pagination({buttons:newButtons});
}
function addBeforeCheck() {
	var normalQuery;
	var selectOption = $("#querytype").combobox('getValue');
	var cityCode = $("#customerquerycity").combotree("getValues");
	var customerQuery = $("#customerquerytextarea").val();
	var customerQueryAll = "";
	if (selectOption == "客户问题") {
		normalQuery = $("#normalquerycombobox").combobox('getValue');
		if (normalQuery == "" || normalQuery == null) {
			$.messager.alert('系统提示', '请选择标准问题!', 'info');
			$('#add-dd').dialog('close');
			return false;
		}
		if (cityCode == "" || cityCode == null) {
			$.messager.alert('系统提示', '请选择地市!', 'warning');
			return false;
		}
		if (customerQuery != "" && customerQuery != null) {
			customerQuery = customerQuery.replace(new RegExp("\r\n", 'g'), "\n");
			customerQuery = customerQuery.replace(/^\n+|\n+$/g, "");
			if (customerQuery == "多条以回车分隔") {
				$.messager.alert('系统提示', '请填写客户问题!', 'warning');
				return false;
			}
		} else {
			$.messager.alert('系统提示', '请填写客户问题!', 'warning');
			return false;
		}

	} else { //标准问题
		normalQuery = $("#normalqueryinput").val();
		if (normalQuery) {
			normalQuery = normalQuery.replace(new RegExp("\r\n", 'g'), "\n");
			normalQuery = normalQuery.replace(/^\n+|\n+$/g, "");
			if (normalQuery == "多条以回车分隔") {
				$.messager.alert('系统提示', '请填写标准问题!', 'warning');
				return false;
			}
		} else {
			$.messager.alert('系统提示', '请填写标准问题!', 'warning');
			return false;
		}

		if (customerQuery == "多条以回车分隔") {
			customerQuery = "";
		}

		if (customerQuery != "" && customerQuery != null) {
			if (cityCode == "" || cityCode == null) {
				$.messager.alert('系统提示', '请选择地市!', 'warning');
				return false;
			}
		}
	}

	return true;
}
//获取新词
function getOOVWord(oovWord, normalQuery,segmentWord) {
	$('#addwordwindow').window('open');
	var wordArray = oovWord.split("$_$");
	var wordHtml = '<input type="hidden" id="addwordwindow-query" value=" ' + normalQuery + '"><input type="hidden" id="addwordwindow-segmentWord" value=" ' + segmentWord + '">';
	wordHtml += '<table cellspacing="0" cellpadding="0">';
	wordHtml += '<tr><td style="padding:5px;"><span>选择</span></td><td style="padding:5px;"><span>新词</span></td><td style="padding:5px;"><span>其他别名</span></td><td style="padding:5px;"><span>是否重要</span></td><td style="padding:5px;"><span>是否业务词</span></td></tr>';
	for (var i = 0; i < wordArray.length; i++) {
		if(wordArray[i] != null && wordArray[i] != ''){
		wordHtml += '<tr><td style="padding:5px;"><input type="checkbox" name="wordcheckbox" id="wordcheckbox_' + i + '" value="" /></td>';
		wordHtml += '<td style="padding:5px;"><span><input type="text" name="wordclass_'+i+'" id="wordclass_' + i + '" value="' + wordArray[i] + '" /></span></td>';
		wordHtml += '<td style="padding:5px;"><span><textarea name="" cols="5" rows="2" id="word_'+i+'" style="width: 120px; font-size: 12px;" placeholder="多个别名回车分隔"></textarea></span></td>';		
		wordHtml += '<td style="padding:5px;"><select id="levelcombobox_' + i + '" style="width:100px;"><option value="0">重要</option><option value="1" selected>不重要</option></select></td>';
		wordHtml += '<td style="padding:5px;"><select id="businesscombobox_' + i + '" style="width:100px;"><option value="0">是</option><option value="1" selected>否</option></select></td>';
		wordHtml += '</tr>';
		}
	}
	wordHtml += '</table>';
	$("#addwordtable").html(wordHtml);
}
function addWordAct() {
	//新词重要程度数据
	var wordlevel = [];
	//新词列表
	var combition = [];
	//业务词
	var wordbusiness = [];
	
	var wordlen = $("#addwordtable input[name='wordcheckbox']").length;
	for (var i = 0; i < wordlen; i++) {
		if ($('#wordcheckbox_' + i).is(':checked')) {
			var otherword = $("#word_"+i).val().replace(new RegExp("\r\n", 'g'),"\n");
			otherword = otherword.replace("\n","|")

			wordlevel.push($('#levelcombobox_' + i).val())
			if("0" == $('#businesscombobox_' + i).val()){//判断是否是业务词
				wordbusiness.push($('#wordclass_' + i).val());
			}
			combition.push($('#wordclass_' + i).val()+"# #"+otherword);
			
		}
	}
	
	if (combition.length == 0) {
		$.messager.alert('系统提示', '请至少选择一个新词', "info");
		return;
	}
	// 要判断每一个词是否在问题中出现，如果不出现，需要给出提示，让用户重新添加，
	var query = $("#addwordwindow-query").val();
	var result = true;
	// 将用户添加的新词从问题中去掉，替换成空格，这样形成新的问题2
	var newquery = query;
	for (var i = 0; i < wordlen; i++) {
		if ($('#wordcheckbox_' + i).is(':checked')) {
		  var w = $("#wordclass_"+i).val().toUpperCase();		
		  if (newquery.toUpperCase().indexOf(w) == -1) {
			$.messager.alert('系统提示', '当前分词【' + w + '】在标准问题中不存在或已被用于其他新词中，请选择其他新词', "info");
			return;
		  }
		
		 newquery = newquery.replace(w, " ");
		}
		
   }
	if(!result){
		return;
	}
	//判断业务词是否连续
	if(query.toUpperCase().indexOf(wordbusiness.join('').toUpperCase()) == -1 ){//业务词不连续
		$.messager.alert('系统提示', '选择的业务词【' + wordbusiness.join(',') + '】在标准问题中不连续，只能选择一个作为业务词', "info");
		return;
	}
	
	console.log($("#addwordwindow-segmentWord").val());
	
	$.ajax( { 
		url : '../querymanage.action',
		type : "post",
		data : {
			type : 'addWord',
			serviceid : serviceid ,
			combition : combition.join('#'),
			normalquery : trim(query),
			flag : wordlevel.join('#'),
			businesswords: wordbusiness.join('-'),
			segmentWord:$("#addwordwindow-segmentWord").val(),
			flagScene:true
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if (data.success) {
				$.messager.alert('系统提示', '添加新词生成', "info");
				$('#addwordwindow').window('close');
			} else {
				$.messager.alert('系统提示', data.msg, "info");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
		}
	});
}
function addQueryAct() {
	var selectOption = $("#querytype").combobox('getValue');
	var param, title, content;
	var customerQuery = $("#customerquerytextarea").val();
	param = {
		type: 'findcustomerquery',
		customerquery: customerQuery,
		citySelect: gCitySelect
	};
	title = '在其他业务下有此客户问题';
	if (!addBeforeCheck()) return;

	$.ajax({
		url: '../querymanage.action',
		type: 'POST',
		data: param,
		success: function(result){
			// 问题存在
			if (result && result.length > 0) {
				var tabs = [];
				for (var j = 0; j < result.length; j++) {
					var lis = [];
					var query = (selectOption == "客户问题" ? result[j].customerquery : result[j].normalquery);
					var data = result[j].duplicate;
	
					for (var i = 0; i < data.length; i++) {
						lis.push('<tr><td>' + (i + 1) + '</td><td>' + data[i].servicepath + '</td></tr>');
					}
					tabs.push('<div class="datagrid-body" style="color:blue; padding: 5px;"><strong style="color:black">' + query + '</strong><table><tr><th>No.</th><th>业务路径</th></tr>' + lis.join('') + '</table></div>');
				}
				content = tabs.join('');
	
				$('#add-dd').dialog({
					width: 450,
					title: title,
					modal: true,
					content: content,
					buttons: [{
						text: '确认',
						handler: function() {
							$(this).linkbutton('disable').next().linkbutton('disable');
							addQuery();
						}
					}, {
						text: '取消',
						handler: function() {
							$('#add-dd').dialog('close');
						}
					}]
				});
	
			} else { // 问题不存在
				addQuery();
			}
		},
		dataType: 'json',
		async: false
	})
}
//新增问题
function addQuery(savebtn) {
	var normalQuery;
	var normalQueryAll = "";
	var selectOption = $("#querytype").combobox('getValue');
	var cityCode = $("#customerquerycity").combotree("getValues");
	var customerQuery = $("#customerquerytextarea").val();
	var customerQueryAll = "";
	var multiNormalQuery = false;
	var errorLine = [];
	if (selectOption == "客户问题") {
		normalQuery = $("#normalquerycombobox").combobox('getValue');
		if (normalQuery == "" || normalQuery == null) {
			$.messager.alert('系统提示', '请选择标准问题!', 'info');
			$('#add-dd').dialog('close');
			return;
		}
		if (cityCode == "" || cityCode == null) {
			$.messager.alert('系统提示', '请选择地市!', 'warning');
			return;
		}
		if (customerQuery != "" && customerQuery != null) {
			customerQuery = customerQuery.replace(new RegExp("\r\n", 'g'), "\n");
			customerQuery = customerQuery.replace(/^\n+|\n+$/g, "");
			if (customerQuery == "多条以回车分隔") {
				$.messager.alert('系统提示', '请填写客户问题!', 'warning');
				return;
			}
		} else {
			$.messager.alert('系统提示', '请填写客户问题!', 'warning');
			return;
		}
		// 校验50字
		if(customerQuery != null){
			var customerQueryItem = customerQuery.split('\n');
			var tempList = [];
			for(var i =0 ;i<customerQueryItem.length;i++){
				if(customerQueryItem[i] != null && customerQueryItem[i].length > 50){
//					$.messager.alert('警告','客户问长度不能超过50字');
					errorLine.push(i+1+"");
				}else{
					tempList.push(customerQueryItem[i]);
				}
			}
			if(errorLine.length == customerQueryItem.length){
				$.messager.alert('警告','添加的客户问长度全部超过50字');
				return;
			}
			customerQuery = tempList.join('\n');
		}
		
		
	} else { //标准问题
		multiNormalQuery = true;
		normalQuery = $("#normalqueryinput").val();
		if (normalQuery == "" || normalQuery == null) {
			$.messager.alert('系统提示', '请填写标准问题!', 'warning');
			return;
		}
		if(normalQuery != null){
			var normalQueryItem = normalQuery.split('\n');
			var tempList = [];
			for(var i =0 ;i<normalQueryItem.length;i++){
				if(normalQueryItem[i] != null && normalQueryItem[i].length > 50){
					errorLine.push(i+1+"");
				}else{
					tempList.push(normalQueryItem[i]);
				}
			}
			
			if(errorLine.length == normalQueryItem.length){
				$.messager.alert('警告','添加的标准问长度全部超过50字');
				return;
			}
			normalQuery = tempList.join('\n');
		}
	}
	if (normalQuery != "" && normalQuery != null && multiNormalQuery) {
		normalQuery = normalQuery.replace(new RegExp("\r\n", 'g'), "\n");
		normalQuery = normalQuery.replace(/^\n+|\n+$/g, "");
		var temp = normalQuery.split('\n');
		temp = quchong(temp);
		for (var i = 0; i < temp.length; i++) {
			if (temp[i] !== '' && temp[i] != '\n') {
				normalQueryAll += temp[i] + '\n';
			}
		}
		normalQueryAll = normalQueryAll.substr(0, normalQueryAll.length - 1);
		normalQuery = normalQueryAll;
	}

	if (customerQuery != "" && customerQuery != null) {
		customerQuery = customerQuery.replace(new RegExp("\r\n", 'g'), "\n");
		customerQuery = customerQuery.replace(/^\n+|\n+$/g, "");
		var temp = customerQuery.split('\n');
		temp = quchong(temp);
		for (var i = 0; i < temp.length; i++) {
			if (temp[i] !== '' && temp[i] != '\n') {
				customerQueryAll += temp[i] + '\n';
			}
		}
		customerQueryAll = customerQueryAll.substr(0, customerQueryAll.length - 1);
	}

	var request = {
		type: "addcustomerquery",
		querytype: selectOption,
		normalquery: kbdataid,
		flagScene: true,
		citycode: cityCode,
		customerquery: customerQueryAll,
		serviceid: serviceid,
		returnValue:returnvalues.join('&')
	};
	var dataStr = {
		m_request: JSON.stringify(request),
		resourcetype: 'querymanage',
		operationtype: 'A',
		resourceid: serviceid
	}

	$.ajax({
		url: '../querymanage.action',
		type: 'POST',
		data: dataStr,
		success: function(data){
			if (data.success) {
//				if (selectOption == "标准问题") { //重新加载标准问题下拉框
//					createNormalqueryCombobox();
//				}
				$('#addquerywindow').window('close');
				$("#customerquerydatagrid").datagrid('load');
				if(errorLine.length > 0){
					$.messager.alert('警告','以下行因超出50字限制未导入：第 '+errorLine.join(',')+' 行');
				}else{
					$.messager.alert('系统提示', data.msg, "info");
					// 展示oov分词
					if (selectOption == "标准问题") {
						var oovWord = data.oovWord;
						if (oovWord != null && oovWord != '') {
							//标准问的原分词
							var segmentWord = data.segmentWord;
							getOOVWord(oovWord, normalQuery,segmentWord);
						}
					}
				}
			} else {
				$.messager.alert('系统提示', data.msg, "warning");
			}
			$('#add-dd').dialog('close');
			$(savebtn).linkbutton('enable');
		},
		dataType: 'json',
		async: false
	})
}
//取消按钮事件
function btnCancel(id) {
	$('#' + id).window('close');
}
//打开理解详情窗口
function openResultWin(index) {
	var row = $('#customerquerydatagrid').datagrid('getData').rows[index];
	var queryid = row.queryid;
	var result = row.result;
	var status = row.status;
	var normalquery = row.normalquery;
	var customerquery = row.customerquery;
	var citycode = row.citycode;

	if (null == status) {
		$.messager.alert('系统提示', "请先进行理解!", "warning");
		return;
	}
	$('#understandresultwin').window('open');
	$('#understandresult_div').empty();
	$('#understandresult_div').append(result);
	$('#understandnormalquery_div').empty();
	$('#understandnormalquery_div').append(normalquery);
	$('#understandcustomerquery_div').empty();
	$('#understandcustomerquery_div').append(customerquery);
	$('#understandcitycode_div').empty();
	$('#understandcitycode_div').append(citycode);
	$('#understandreason_div').empty();
	$('#understandreason_div').append(status);
}
//打开客户问题编辑框
function openEditCustomerQueryWin() {
	var rows = $("#customerquerydatagrid").datagrid("getSelections");
	if (rows) {
		if (rows.length == 1) {
			var row = $('#customerquerydatagrid').datagrid('getSelected');
			var queryid = row.queryid;
			var customerquery = row.customerquery;
			var citycode = row.citycode;
			var normalquery = row.normalquery;
			//			clearEditCustomerQueryWin();
			$('#editcustomerquerywin').window('open');
			$("#editcustomerquery").textbox('setValue', customerquery);
			//			$("#editnormalqueryinput2").textbox('setValue', normalquery);
			$("#editnormalqueryinput2").val(normalquery);
			$('#editcustomerquerycity').combotree('setValues', citycode);
		} else {
			$.messager.alert('系统提示', "请选择一行记录编辑!", "warning");
			return;
		}
	} else {
		$.messager.alert('系统提示', "请选择一行记录编辑!", "warning");
		return;
	}

	//	$('#editnormalqueryinput2').textbox('textbox').attr('readonly',true);  
}
//打开客户问题编辑框——行内编辑按钮
function openEditCustomerQueryWin2(index) {

	var row = $('#customerquerydatagrid').datagrid('getData').rows[index];
	var queryid = row.queryid;
	var customerquery = row.customerquery;
	var citycode = row.citycode;
	var normalquery = row.normalquery;
	//			clearEditCustomerQueryWin();
	$('#editcustomerquerywin').window('center').window('open');
	$("#editcustomerquery").textbox('setValue', customerquery);
	//	$("#editnormalqueryinput2").textbox('setValue', normalquery);
	$("#editnormalqueryinput2").val(normalquery);
	$('#editcustomerquerycity').combotree('setValues', citycode);
	//	$('#editnormalqueryinput2').textbox('textbox').attr('readonly',true);
	$('#editrowidx').val(index);

}
//清空客户问题编辑框
function clearEditCustomerQueryWin() {
	$("#editcustomerquery").textbox('setValue', "");
	$("#editnormalqueryinput").textbox('setValue', "");
	$('#editcustomerquerycity').combotree('setValues', "");
	$('#editresponsetype').combobox('clear');
	$('#editinteracttype').combobox('setValue', "");
	$('#editrowidx').val("");
}


//编辑客户问题
function editCustomerQuery() {

	var customerquery = replaceSpace($("#editcustomerquery").textbox("getValue"));
	var citycode = $('#editcustomerquerycity').combotree("getValues");
	if (customerquery != "" && citycode == "") {
		$.messager.alert('系统提示', '请选择地市!', "warning");
		return;
	}
	//	var normalquery = replaceSpace($("#editnormalqueryinput2").textbox("getValue"));
	var normalquery = replaceSpace($("#editnormalqueryinput2").val());
	//var row = $('#customerquerydatagrid').datagrid('getSelected');
	var index = $('#editrowidx').val();
	var row = $('#customerquerydatagrid').datagrid('getData').rows[index];
	var queryid = row.queryid;
	var kbdataid = row.kbdataid;
	var oldcustomerquery = row.customerquery;
	var oldcitycode = row.citycode;
	var service = row.service;
	var oldnormalquery = row.normalquery;

	$.ajax({
		url: '../querymanage.action',
		type: "post",
		data: {
			type: 'updatecustomerquery',
			resourcetype: 'querymanage',
			operationtype: 'U',
			resourceid: serviceid,
			oldcitycode: oldcitycode + "@@",
			citycode: citycode + "@@",
			customerquery: customerquery,
			oldcustomerquery: oldcustomerquery,
			queryid: queryid,
			kbdataid: kbdataid,
			service: service,
			normalquery: normalquery,
			oldnormalquery: oldnormalquery,
			responsetype: "",
			interacttype: ""
		},
		async: false,
		dataType: "json",
		success: function(data, textStatus, jqXHR) {

			if (data.success == true) {
				$("#customerquerydatagrid").datagrid("reload");
				$('#editcustomerquerywin').window('close');
				clearEditCustomerQueryWin();
				$.messager.alert('系统提示', data.msg, "info");
			} else {
				$.messager.alert('系统提示', data.msg, "warning");
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});

}
//批量删除客户问题
function deleteCustomerQuery() {
	var combition = [];
	var rows = $("#customerquerydatagrid").datagrid("getSelections");
	if (rows.length > 0) {
		for (var i = 0; i < rows.length; i++) {
			var queryid = rows[i].queryid;
			if (queryid == null || queryid == "") {
				continue;
			}
			combition.push(queryid + "@#@" + rows[i].customerquery + "@#@" + rows[i].kbdataid);
		}
	} else {
		$.messager.alert('系统提示', "请至少选择一行!", "warning");
		return;
	}

	if (combition.length > 0) {
		$.messager.confirm("操作提示", "您确定要删除吗？", function(data) {
			if (data) {
				$.ajax({
					type: "post",
					async: false,
					url: "../querymanage.action",
					data: {
						type: "deletecustomerquery",
						resourcetype: 'querymanage',
						operationtype: 'D',
						resourceid: serviceid,
						combition: combition.join("@@")
					},
					dataType: "json",
					success: function(data, textStatus, jqXHR) {
						$.messager.alert('系统提示', data["msg"], "info");
						if (data["success"] == true) {
							$("#customerquerydatagrid").datagrid('reload');
						}
					},
					error: function(jqXHR, textStatus, errorThrown) {
						//						$.messager.alert('系统异常', "请求数据失败!", "error");
					}
				});
			}
		});
	}

}
//查询标准问题下客户问题
function searchCustomerQuery(customerQuery) {
	if(kbdataid && customerQuery){
		
	} else {
		var customerQuery = replaceSpace($("#customerqueryselect").textbox("getValue"));
		var istrain = $("#istrain").combobox("getValue");
		var understandstatus = $("#understandstatus").combobox("getValue");
	}
	
	var cityCode = $("#cityselect").combotree("getValue");
	$('#customerquerydatagrid').datagrid('load', {
		type: "selectcustomerquery",
		serviceid: serviceid,
		customerquery: customerQuery,
		citycode: cityCode,
		istrain: istrain,
		understandstatus: understandstatus,
		kbdataid: kbdataid
	});
}
//更新知识库
function updateKbdata() {
	$.ajax({
		type: "POST",
		url: "../extend/updateKbdata.action",
		success: function(data, textStatus, jqXHR) {
			var detaildata = data["result"];
			$.messager.alert('系统提示', detaildata, 'info');
		},
		error: function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统提示', '更新知识库请求发送失败！', 'info');
		}
	});
}
//批量理解
function understand() {
	var rows = $('#customerquerydatagrid').datagrid("getSelections");
	var infos = [];
	if (!rows || rows.length < 1) {
		$.messager.alert('系统提示', "请选择一行进行理解!", "warning");
		return;
	}

	for (var i = 0; i < rows.length; i++) {
		var info = rows[i].queryid + '@-@' + rows[i].normalquery + '@-@' + rows[i].customerquery + '@-@' + rows[i].citycode + '@-@' + rows[i].cityname;
		infos.push(info);
	}
	var understrandinfo = infos.join('xxxnixxx');

	$("#customerquerydatagrid").datagrid('loading');
	$(".datagrid-mask-msg").text('请耐心等待,批量理解中......');
	$.ajax({
		type: "post",
		url: "../querymanage.action",
		data: {
			type: "understand",
			understrandinfo: understrandinfo
		},
		async: true,
		dataType: "json",
		success: function(data, textStatus, jqXHR) {
			$("#customerquerydatagrid").datagrid('reload');
			$.messager.alert('系统提示', data.result, "info");
		},
		error: function(jqXHR, textStatus, errorThrown) {
			$("#customerquerydatagrid").datagrid('reload');
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
};
//提交上传文件
function upload() {
	//	$.messager.alert('提示', "Sorry! Waiting for me to do ", "warning");
	//	 return;
	// 得到上传文件的全路径
	var fileName = $('#fileuploadtxt').filebox('getValue');
	// 进行基本校验
	if (fileName == "") {
		$.messager.alert('系统提示', '请选择上传文件!', 'info');
	} else {
		// 对文件格式进行校验
		var d1 = /\.[^\.]+$/.exec(fileName.toLowerCase());
		if (d1 == ".xls" || d1 == ".xlsx") {
			$("#formUpload").form(
				"submit", {
					url: "../file/upload?path=qatraining/regresstest",
					success: function(data) {
						var info = $.parseJSON(data);
						var state = info["state"];
						if (state == "success") {
							var name = info["names"][0];
							importExcel(name);
						} else {
							$.messager.alert('系统提示', info["message"] + " 请重新上传!", 'warning');
						}
						$('#fileuploadtxt').filebox('setValue', '');
					}
				});
		} else {
			$.messager.alert('系统提示', '请选择.xls或.xlsx格式文件!', 'info');
			$('#fileuploadtxt').filebox('setValue', '');
		}
	}
}
//提交上传文件
function removequery_upload() {
	// 得到上传文件的全路径
	var fileName = $('#removequestion_fileuploadtxt').filebox('getValue');
	// 进行基本校验
	if (fileName == "") {
		$.messager.alert('系统提示', '请选择上传文件!', 'info');
	} else {
		// 对文件格式进行校验
		var d1 = /\.[^\.]+$/.exec(fileName.toLowerCase());
		if (d1 == ".xls" || d1 == ".xlsx") {
			$("#removequery_formUpload").form(
				"submit", {
					url: "../file/upload?path=qatraining/regresstest",
					success: function(data) {
						var info = $.parseJSON(data);
						var state = info["state"];
						if (state == "success") {
							var name = info["names"][0];
							importExcelRemove(name);
						} else {
							$.messager.alert('系统提示', info["message"] + " 请重新上传!", 'warning');
						}
						$('#removequestion_fileuploadtxt').filebox('setValue', '');
					}
				});
		} else {
			$.messager.alert('系统提示', '请选择.xls或.xlsx格式文件!', 'info');
			$('#removequestion_fileuploadtxt').filebox('setValue', '');
		}
	}
}
//将excel文件中的数据导入到数据库中
function importExcelRemove(name) {
	$("#querymanagedatagrid").datagrid('loading');
	$(".datagrid-mask-msg").text('请耐心等待,导入问题中......');

	$("#removequerydatagrid").datagrid('loading');
	$(".datagrid-mask-msg").text('请耐心等待,导入问题中......');

	$.ajax({
		type: "post",
		url: "../querymanage.action",
		data: {
			type: 'importRemoveByScene',
			resourcetype: 'querymanage',
			operationtype: 'A',
			resourceid: serviceid,
			filename: name,
			serviceid: serviceid,
			normalquery:normalquery,
			returnValue:returnvalues.join('&')
		},
		async: false,
		dataType: "json",
		timeout: 180000,
		success: function(data, textStatus, jqXHR) {
			if (data.errorMsg) {
				$.messager.alert('系统提示', '以下行因超出50字限制未导入：第  '+data.errorMsg+' 行', "info");
			} else {
				$.messager.alert('系统提示', data.msg, "info");
			}
			
			if (data.success == true) {
//				createNormalqueryCombobox();
//				_searchNormalQuery();
				loadRemoveQuerydDatagridList();
			}
			$("#removequerydatagrid").datagrid('loaded');
			$("#querymanagedatagrid").datagrid("loaded");
		},
		error: function(jqXHR, textStatus, errorThrown) {
			$("#removequerydatagrid").datagrid('loaded');
			$("#querymanagedatagrid").datagrid("loaded");
		}
	});
}
//导出排除问题
function removequery_exportExcel() {
	var param = {
		normalquery: normalquery,
		responsetype: '',
		interacttype: '',
		serviceid: serviceid
	};

	if (param.normalquery == "全部") {
		param.normalquery = "";
	}
	if (param.responsetype == "全部") {
		param.responsetype = "";
	}
	if (param.interacttype == "全部") {
		param.interacttype = "";
	}

	param.type = 'removequerymanageexport';
	$('#qm_form').form('submit', {
		url: '../querymanageexport.action',
		queryParams: param,
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
//生成词模的预先操作
function preProduceWordpat(type){
	type = type || "";
	if(customItem["训练模式"] != null && customItem["训练模式"] =="显示"){
		var url = '../querymanage.action?type=createproduceWordpatcombobox&a=' + Math.random();
		createCombobox('produceWordpatselect', url, false, true, function() {
			$('#produceWordpatselect').combobox('setValue', '5');
			$('#produceWordpatType').val(type);
			
			$('#produceWordpatwin').window('open');
		});
	}else{
		if(type == "ALL"){
			produceAllWordpat("5");
		}else{
			produceWordpat("5");
		}
	}
}
//将excel文件中的数据导入到数据库中
function importExcel(name) {
	$("#querymanagedatagrid").datagrid('loading');
	$(".datagrid-mask-msg").text('请耐心等待,导入问题中......');

	$("#customerquerydatagrid").datagrid('loading');
	$(".datagrid-mask-msg").text('请耐心等待,导入问题中......');

	$.ajax({
		type: "post",
		url: "../querymanage.action",
		data: { 
			type: 'importByScene',
			resourcetype: 'querymanage',
			operationtype: 'A',
			resourceid: serviceid,
			filename: name,
			serviceid: serviceid,
			normalquery:normalquery,
			returnValue:returnvalues.join('&')
		},
		async: false,
		dataType: "json",
		timeout: 180000,
		success: function(data, textStatus, jqXHR) {
			if(data.errorMsg){
				$.messager.alert('系统提示', '以下行因超出50字限制未导入：第  '+data.errorMsg+' 行', "info");
			}else{
				$.messager.alert('系统提示', data.msg, "info");
			}
			
//			$.messager.alert('系统提示', data.msg, "info");
			if (data.success == true) {
//				createNormalqueryCombobox();
//				_searchNormalQuery();
				loadCustomerQuerydDatagridList();
			}
			$("#customerquerydatagrid").datagrid('loaded');
			$("#querymanagedatagrid").datagrid("loaded");
		},
		error: function(jqXHR, textStatus, errorThrown) {
			//			$.messager.alert('系统异常', "请求数据失败!", "error");
			$("#customerquerydatagrid").datagrid('loaded');
			$("#querymanagedatagrid").datagrid("loaded");
		}
	});
}
function exportExcel() {
	var param = {
		normalquery: normalquery,
		responsetype: '',
		interacttype: '',
		serviceid: serviceid
	};
	

	if (param.normalquery == "全部") {
		param.normalquery = "";
	}
	if (param.responsetype == "全部") {
		param.responsetype = "";
	}
	if (param.interacttype == "全部") {
		param.interacttype = "";
	}
	console.log(param);
	param.type = 'querymanageexport';
	$('#qm_form').form('submit', {
		url: '../querymanageexport.action',
		queryParams: param,
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

//生成词模操作
function doProduceWordpat(){
	var type = $('#produceWordpatType').val();
	var wordpattype = $('#produceWordpatselect').combobox('getValue');
	$('#produceWordpatwin').window('close');
	if(type == "ALL"){
		produceAllWordpat(wordpattype);
	}else{
		produceWordpat(wordpattype);
	}
}

//批量生成词模
function produceWordpat(wordpattype) {
	
	var combition = [];
	var rows = $("#customerquerydatagrid").datagrid("getSelections");
	if (rows.length > 0) {
		for (var i = 0; i < rows.length; i++) {
			var queryid = rows[i].queryid;

			if (queryid == null || queryid == "") {
				continue;
			}
			combition.push(rows[i].citycode + "@#@" + rows[i].customerquery + "@#@" + rows[i].kbdataid + "@#@" + rows[i].queryid +"@#@ ");
		}
	} else {
		$.messager.alert('系统提示', "请至少选择一行数据供系统语义训练!", "warning");
		return;
	}
	$("#customerquerydatagrid").datagrid('loading');
	$(".datagrid-mask-msg").text('请耐心等待,语义训练中......');
	$.ajax({
		url: '../querymanage.action',
		type: "post",
		data: {
			type: 'customerproducewordpat',
			resourcetype: 'querymanage',
			operationtype: 'A',
			resourceid: serviceid,
			combition: combition.join("@@"),
			flag:wordpattype,
			flagScene:true
		},
		async: false,
		dataType: "json",
		success: function(data, textStatus, jqXHR) {
			//			$("#querymanagedatagrid").datagrid('loaded');
			$("#customerquerydatagrid").datagrid('loaded');
			var downloadUrl = '';
			if(data.fileName){
				downloadUrl = '</br>生成报告：</br>'
					+'<a href="../querymanageexport.action?type=wordpatexport&fileName='+data.fileName
					+'" download="'+data.fileName+'" title="下载" >'+data.fileName+'</a>';
			}
//			downloadUrl = '';
			if (data.success == true) {
				$("#querymanagedatagrid").datagrid("reload");
				$("#customerquerydatagrid").datagrid("load");
				$.messager.alert('系统提示', data.msg+downloadUrl, "info");
				var newWord = data.newWord;
				var oovWord = data.OOVWord;
				if(oovWord != null && oovWord != '' && newWord !=null && newWord != ''){
				   loadNewWord(newWord, oovWord,"0",data.OOVWordQuery,data.segmentWord);
				}
				
			} else {
				$.messager.alert('系统提示', data.msg+downloadUrl, "warning");
			}
			
		},
		error: function(jqXHR, textStatus, errorThrown) {
			$("#customerquerydatagrid").datagrid('loaded');
			//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});

}
//查询标准问题下排除问题
function searchRemoveQuery() {

	var customerQuery = replaceSpace($("#removequeryselect").textbox("getValue"));
	var istrain = $("#removequeryistrain").combobox("getValue");
	var removequerystatus = $("#removequerystatusselect").combobox("getValue");
	
	var cityCode = $("#removequerycityselect").combotree("getValue");
	$('#removequerydatagrid').datagrid('load', {
		type: "selectremovequery",
		serviceid: serviceid,
		customerquery: customerQuery,
		citycode: cityCode,
		istrain: istrain,
		removequerystatus: removequerystatus,
		kbdataid: kbdataid
	});
}

//批量训练
function preRemoveProduceWordpat() {
	var combition = [];
	var rows = $("#removequerydatagrid").datagrid("getSelections");
	if (rows.length == 0) {
		$.messager.alert('系统提示', "请至少选择一行数据供系统语义进行排除词模训练!", "warning");
		return;
	}
	removeProduceWordpat("2");
}
//生成词模操作
function doRemoveProduceWordpat(){
	var type = $('#removeProduceWordpatType').val();
	var wordpattype = $('#removeProduceWordpatselect').combobox('getValue');
	$('#removeProduceWordpatwin').window('close');
	removeProduceWordpat(wordpattype);
}
//批量生成词模
function removeProduceWordpat(wordpattype) {
	var combition = [];
	var rows = $("#removequerydatagrid").datagrid("getSelections");
	if (rows.length > 0) {
		for (var i = 0; i < rows.length; i++) {
			var queryid = rows[i].queryid;

			if (queryid == null || queryid == "") {
				continue;
			}
			combition.push(rows[i].citycode + "@#@" + rows[i].customerquery + "@#@" + rows[i].kbdataid + "@#@" + rows[i].queryid + "@#@"+ rows[i].isstrictexclusion);
		}
	} else {
		$.messager.alert('系统提示', "请至少选择一行数据供系统语义训练!", "warning");
		return;
	}
	$("#removequerydatagrid").datagrid('loading');
	$(".datagrid-mask-msg").text('请耐心等待,语义训练中......');
	$.ajax({
		url: '../querymanage.action',
		type: "post",
		data: {
			type: 'removeproducewordpat',
			resourcetype: 'querymanage',
			operationtype: 'A',
			resourceid: serviceid,
			combition: combition.join("@@"),
			flag:wordpattype,
			flagScene:true
		},
		async: false,
		dataType: "json",
		success: function(data, textStatus, jqXHR) {
			$("#removequerydatagrid").datagrid("load");
			var downloadUrl = '';
			if(data.fileName){
				downloadUrl = '</br>生成报告：</br>'
					+'<a href="../querymanageexport.action?type=wordpatexport&fileName='+data.fileName
					+'" download="'+data.fileName+'" title="下载" >'+data.fileName+'</a>';
			}
			if(data.success == true){
				$("#querymanagedatagrid").datagrid("reload");
				$("#removequerydatagrid").datagrid("load");
				$.messager.alert('系统提示', data.msg+downloadUrl, "info");
				var newWord = data.newWord;
				var oovWord = data.OOVWord;
				if(oovWord != null && oovWord != ''){
				   loadNewWord(newWord, oovWord,"1",data.OOVWordQuery,data.segmentWord);
				}
			}else{
				$.messager.alert('系统提示', data.msg+downloadUrl, "warning");				
			}
			
		},
		error: function(jqXHR, textStatus, errorThrown) {
			$("#removequerydatagrid").datagrid('loaded');
		}
	});
}
//新增排除问题
function addRemoveQueryAct() {
	var selectOption = $("#removequerytype").combobox('getValue');
	var param, title, content;
	if (selectOption == "排除问题") {
		var customerQuery = $("#removequerytextarea").val();
		param = {
			type: 'findremovequery',
			customerquery: customerQuery,
			citySelect: gCitySelect
		};
		title = '在其他业务下有此排除问题';
	} else { //标准问题
		var normalQuery = $("#normalqueryinput").val();
		param = {
			type: 'findnormalquery',
			normalquery: normalQuery,
			citySelect: gCitySelect
		};
		title = '在其他业务下有此排除问题';
	}

	if (!addRemoveBeforeCheck()) return;

	$.ajax({
		url: '../querymanage.action',
		type: 'POST',
		data: param,
		success: function(result){
			// 问题存在
			if (result && result.length > 0) {
				var tabs = [];
				for (var j = 0; j < result.length; j++) {
					var lis = [];
					var query = (selectOption == "排除问题" ? result[j].customerquery : result[j].normalquery);
					var data = result[j].duplicate;
	
					for (var i = 0; i < data.length; i++) {
						lis.push('<tr><td>' + (i + 1) + '</td><td>' + data[i].servicepath + '</td></tr>');
					}
					tabs.push('<div class="datagrid-body" style="color:blue; padding: 5px;"><strong style="color:black">' + query + '</strong><table><tr><th>No.</th><th>业务路径</th></tr>' + lis.join('') + '</table></div>');
				}
				content = tabs.join('');
	
				$('#remove_add-dd').dialog({
					width: 450,
					title: title,
					modal: true,
					content: content,
					buttons: [{
						text: '确认',
						handler: function() {
							$(this).linkbutton('disable').next().linkbutton('disable');
							addRemoveQuery();
						}
					}, {
						text: '取消',
						handler: function() {
							$('#remove_add-dd').dialog('close');
						}
					}]
				});
	
			} else { // 问题不存在
				addRemoveQuery();
			}
		},
		dataType: 'json',
		async: false
	});
}
function addRemoveBeforeCheck() {
	var selectOption = $("#removequerytype").combobox('getValue');
	var cityCode = $("#removequerycity").combotree("getValues");
	var customerQuery = $("#removequerytextarea").val();
	var customerQueryAll = "";
	if (selectOption == "排除问题") {
		
		if (cityCode == "" || cityCode == null) {
			$.messager.alert('系统提示', '请选择地市!', 'warning');
			return false;
		}
		if (customerQuery != "" && customerQuery != null) {
			customerQuery = customerQuery.replace(new RegExp("\r\n", 'g'), "\n");
			customerQuery = customerQuery.replace(/^\n+|\n+$/g, "");
			if (customerQuery == "多条以回车分隔") {
				$.messager.alert('系统提示', '请填写排除问题!', 'warning');
				return false;
			}
		} else {
			$.messager.alert('系统提示', '请填写排除问题!', 'warning');
			return false;
		}
	} else { //标准问题
		normalQuery = $("#normalqueryinput").val();
		if (normalQuery) {
			normalQuery = normalQuery.replace(new RegExp("\r\n", 'g'), "\n");
			normalQuery = normalQuery.replace(/^\n+|\n+$/g, "");
			if (normalQuery == "多条以回车分隔") {
				$.messager.alert('系统提示', '请填写标准问题!', 'warning');
				return false;
			}
		} else {
			$.messager.alert('系统提示', '请填写标准问题!', 'warning');
			return false;
		}

		if (customerQuery == "多条以回车分隔") {
			customerQuery = "";
		}

		if (customerQuery != "" && customerQuery != null) {
			if (cityCode == "" || cityCode == null) {
				$.messager.alert('系统提示', '请选择地市!', 'warning');
				return false;
			}
		}
	}
	return true;
}
//新增排除问题
function addRemoveQuery(savebtn) {
	var normalQueryAll = "";
	var selectOption = $("#removequerytype").combobox('getValue');
	var cityCode = $("#removequerycity").combotree("getValues");
	var customerQuery = $("#removequerytextarea").val();
	var customerQueryAll = "";
	var multiNormalQuery = false;
	var errorLine = [];
	if (selectOption == "排除问题") {

		if (cityCode == "" || cityCode == null) {
			$.messager.alert('系统提示', '请选择地市!', 'warning');
			return;
		}
		if (customerQuery != "" && customerQuery != null) {
			customerQuery = customerQuery.replace(new RegExp("\r\n", 'g'), "\n");
			customerQuery = customerQuery.replace(/^\n+|\n+$/g, "");
			if (customerQuery == "多条以回车分隔") {
				$.messager.alert('系统提示', '请填写排除问题!', 'warning');
				return;
			}
		} else {
			$.messager.alert('系统提示', '请填写排除问题!', 'warning');
			return;
		}
		// 校验50字
		if (customerQuery != null) {
			var customerQueryItem = customerQuery.split('\n');
			var tempList = [];
			for (var i = 0 ;i < customerQueryItem.length; i++) {
				if (customerQueryItem[i] != null && customerQueryItem[i].length > 50) {
					errorLine.push(i + 1 + "");
				} else {
					tempList.push(customerQueryItem[i]);
				}
			}
			if (errorLine.length == customerQueryItem.length) {
				$.messager.alert('警告','添加的排除问长度全部超过50字');
				return;
			}
			customerQuery = tempList.join('\n');
		}
	} 

	if (customerQuery != "" && customerQuery != null) {
		customerQuery = customerQuery.replace(new RegExp("\r\n", 'g'), "\n");
		customerQuery = customerQuery.replace(/^\n+|\n+$/g, "");
		var temp = customerQuery.split('\n');
		temp = quchong(temp);
		for (var i = 0; i < temp.length; i++) {
			if (temp[i] !== '' && temp[i] != '\n') {
				customerQueryAll += temp[i] + '\n';
			}
		}
		customerQueryAll = customerQueryAll.substr(0, customerQueryAll.length - 1);
	}

	var removequerystatus = $("#removequerystatus").combobox('getValue');
	var request = {
		type: "addremovequeryscene",
		querytype: selectOption,
		normalquery: kbdataid,
		multinormalquery: multiNormalQuery,
		citycode: cityCode,
		customerquery: customerQueryAll,
		serviceid: serviceid,
		removequerystatus : removequerystatus,
		returnValue: returnvalues.join('&')
	};
	var dataStr = {
		m_request: JSON.stringify(request),
		resourcetype: 'querymanage',
		operationtype: 'A',
		resourceid: serviceid
	}

	$.ajax({
		url: '../querymanage.action',
		type: 'POST',
		data: dataStr,
		success: function(data){
			if (data.success) {
				$('#addremovequerywindow').window('close');
				$("#removequerydatagrid").datagrid('load');
				if(errorLine.length > 0){
					$.messager.alert('警告','以下行因超出50字限制未导入：第 ' + errorLine.join(',') + ' 行');
				}else{
					$.messager.alert('系统提示', data.msg, "info");
				}
			} else {
				$.messager.alert('系统提示', data.msg, "warning");
			}
			$('#remove_add-dd').dialog('close');
			$(savebtn).linkbutton('enable');
		},
		dataType: 'json',
		async: false
	});
}


//展示
function loadNewWord(newWord, oovWord,querytype,oovWordQuery,segmentWord) {
	$('#addotherwordwindow').window('open');
	var wordArray = newWord.split("##");
	
	var wordHtml = '';

	var newwordHtml = '<input type="hidden" id="addotherwordwindow-query" value=" ' + oovWordQuery + '"/><input type="hidden" id="addotherwordwindow-querytype" value=" ' + querytype + '"/>';
	newwordHtml += '<input type="hidden" id="addotherwordwindow-segmentWord" value=" ' + segmentWord + '"/>';
	newwordHtml += '<span style="font-size: 12px; margin-left: 10px">标准词条 ：</span>';
	newwordHtml += '<select  id="newwordselect" class="easyui-combobox" data-options="editable:false" style="width: 100px;" type="text"><option value="">请选择</option>';
	for (var i = 0; i < wordArray.length; i++) {
		if(wordArray != null && wordArray != ''){
			var newWordArray = wordArray[i].split("@@");
			newwordHtml +='<option value="'+newWordArray[0]+'#'+newWordArray[1]+'" >'+newWordArray[0]+'</option>'
		}
	}
	newwordHtml += '</select>';
	$("#addOtherWordDiv").html(newwordHtml);
	// 标准问题中的添加的新词|扩展问中的别名新词1|别名新词2
	wordArray = oovWord.split("$_$");
	wordHtml += '<table cellspacing="0" cellpadding="0">';
	wordHtml += '<tr><td style="padding:5px;"><span>选择</span></td><td style="padding:5px;"><span>新词</span></td><td style="padding:5px;"><span>其他别名</span></td><td style="padding:5px;"><span>是否重要</span></td></tr>';
	for (var i = 0; i < wordArray.length; i++) {
		var content = wordArray[i];
		if(content != null && content !=''){
		wordHtml += '<tr><td style="padding:5px;"><input type="checkbox" name="otherwordcheckbox" id="otherwordcheckbox_' + i + '" value="" /></td>';
		wordHtml += '<td style="padding:5px;"><span><input type="text" name="otherword_'+i+'" id="otherword_' + i + '" value="' + content + '" /></span></td>';
		wordHtml += '<td style="padding:5px;"><span><textarea name="" cols="5" rows="2" id="worditems_'+i+'" style="width: 120px; font-size: 12px;" placeholder="多个别名回车分隔"></textarea></span></td>';		
		wordHtml += '<td style="padding:5px;"><select id="levelcombobox_' + i + '" style="width:100px;"><option value="0">重要</option><option value="1" selected>不重要</option></select></td>';
		wordHtml += '</tr>';
		}
	}
	wordHtml += '</table>';
	$("#addotherwordtable").html(wordHtml);
	
}

//新增别名
function doRemoveNewWord() {
	
	var word = $("#newwordselect").val();
	var customerQuery = $("#addotherwordwindow-query").val();
	//包含新词的扩展问
	var customerArray = customerQuery.split("@@");
	var customerQueryArray = []
	for(var i=0;i<customerArray.length;i++){
		var queryArray = customerArray[i].split("@#@");
		customerQueryArray.push(queryArray[1]);
	}
	//新词
	var combitionArray = [];
	var otherwordlen = $("#addotherwordtable input[name='otherwordcheckbox']").length;
	//是否重要
	var flagArray = [];
	
	for(var i=0;i<otherwordlen;i++){
		if($("#otherwordcheckbox_"+i).is(':checked')){
			var newWord = $("#otherword_"+i).val();
			
			//判断纠正的新词在扩展问是否存在
			var count = 0;
			for(var j=0;j<customerQueryArray.length;j++){
				if(customerQueryArray[j].indexOf(newWord) == -1){
					count++;
				}
			}
			if(count == customerQueryArray.length){
				$.messager.alert('系统提示', '新词【'+newWord+'】在问题中不存在', "info");
				return;
			}

			var combition = "";
			//别名
			var otherword = $("#worditems_"+i).val().replace(new RegExp("\r\n", 'g'),'\n');
			if(word.length == 0){ //词条为空时，新词作为词类填入
				combition += newWord + "# #" + otherword.replace('\n','|');
			}else{
				combition += word + "#"+ newWord + "|" + otherword.replace('\n','|');
			}
			combitionArray.push(combition);
			flagArray.push($("#levelcombobox_"+i).val());
		}
	}

	if (combitionArray.length == 0) {
		$.messager.alert('系统提示', '请至少选择一个新词', "info");
		return;
	}
	$.ajax( { 
		url : '../querymanage.action',
		type : "post",
		data : {
			type : 'addOtherWord',
			combition : combitionArray.join('@@'),
			customerquery : $("#addotherwordwindow-query").val(),
			querytype : $("#addotherwordwindow-querytype").val(),
			flag: flagArray.join('#'),
			segmentWord: $("#addotherwordwindow-segmentWord").val(),
			flagScene:true
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if (data.success) {
				$('#addotherwordwindow').window('close');
				$.messager.alert('系统提示', '新增成功', "info");
			} else {
				$.messager.alert('系统提示', data.msg, "info");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
		}
	});
}
function deleteRemoveCustomerQuery(){
	var combition = [];
	var rows = $("#removequerydatagrid").datagrid("getSelections");
	if (rows.length > 0) {
		for (var i = 0; i < rows.length; i++) {
			var queryid = rows[i].queryid;
			if (queryid == null || queryid == "") {
				continue;
			}
			combition.push(queryid + "@#@" + rows[i].customerquery + "@#@" + rows[i].kbdataid);
		}
	} else {
		$.messager.alert('系统提示', "请至少选择一行!", "warning");
		return;
	}

	if (combition.length > 0) {
		$.messager.confirm("操作提示", "您确定要删除吗？", function(data) {
			if (data) {
				$.ajax({
					type: "post",
					async: false,
					url: "../querymanage.action",
					data: {
						type: "deletecustomerquery",
						resourcetype: 'querymanage',
						operationtype: 'D',
						resourceid: serviceid,
						combition: combition.join("@@")
					},
					dataType: "json",
					success: function(data, textStatus, jqXHR) {
						$.messager.alert('系统提示', data["msg"], "info");
						if (data["success"] == true) {
							$("#removequerydatagrid").datagrid('reload');
						}
					},
					error: function(jqXHR, textStatus, errorThrown) {
						//						$.messager.alert('系统异常', "请求数据失败!", "error");
					}
				});
			}
		});
	}
}

//排除问题-批量理解
function removeunderstand() {
	var rows = $('#removequerydatagrid').datagrid("getSelections");
	var infos = [];
	if (!rows || rows.length < 1) {
		$.messager.alert('系统提示', "请选择一行进行理解!", "warning");
		return;
	}

	for (var i = 0; i < rows.length; i++) {
		var info = rows[i].queryid + '@-@' + rows[i].normalquery + '@-@' + rows[i].customerquery + '@-@' + rows[i].citycode + '@-@' + rows[i].cityname;
		infos.push(info);
	}
	var understrandinfo = infos.join('xxxnixxx');

	$("#removequerydatagrid").datagrid('loading');
	$(".datagrid-mask-msg").text('请耐心等待,批量理解中......');
	$.ajax({
		type: "post",
		url: "../querymanage.action",
		data: {
			type: "understand",
			understrandinfo: understrandinfo
		},
		async: true,
		dataType: "json",
		success: function(data, textStatus, jqXHR) {
			$("#removequerydatagrid").datagrid('reload');
			$.messager.alert('系统提示', data.result, "info");
		},
		error: function(jqXHR, textStatus, errorThrown) {
			$("#customerquerydatagrid").datagrid('reload');
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
};
