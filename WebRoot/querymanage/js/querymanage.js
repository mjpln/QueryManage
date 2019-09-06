var serviceid;
var userid;
var ioa;
var gCitySelect;
var gHookOpts;
var customItem = {};
var showInTab = false;//是否已标签页形式展示
$(function() {
	var urlparams = new UrlParams(); // 所有url参数
	serviceid = decodeURI(urlparams.serviceid);
	userid = decodeURI(urlparams.userid);
	ioa = decodeURI(urlparams.ioa);
	gCitySelect = decodeURI(urlparams.cityselect);
	gHookOpts = parseHook(urlparams.hook);
	initCustom();
	// 加载问题管理列表
	loadQueryManageList(gHookOpts);
	//加载客户问题
	loadCustomerQuerydDatagridList(gHookOpts);
	// 初始化检索Panel
	initFilterPanel(gHookOpts);
	//添加问题类型onselect 事件
	queryTypeOnSelect();
	//创建selectcitytree
	createCityTree('cityselect', 'edit', false);
	createCityTree('editcustomerquerycity', 'edit', true);

	//创建标准问选中事件
	normalQueryOnSelect();
});
function initCustom(){
	//使用父页面的定制化配置
	if(window.parent.customItem){
		customItem = window.parent.customItem;
		return;
	}
	//初始化定制配置
	var initCustomItem = function(customItems){
		$.each(customItems,function(n,item){
			var s = item.split("=");
			customItem[s[0]] = s[1];
			if(customItem["页面布局"] != null && customItem["页面布局"] == "标签展示"){
				showInTab = true;
			}
		});
	};
	$.ajax( { 
		url : '../querymanage.action',
		type : "post",
		data : {
			type : 'configure'
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if(data.customItem){
				initCustomItem(data.customItem);
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
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

//构造树形图
function createInteractiveSceneTree(fid) {
	var fdocid = "#" + fid;
	$(fdocid).combotree({
		url:'../querymanage.action?type=createinteractivescenetree&a='+ Math.random()+"&citySelect="+encodeURI(gCitySelect),
//		url: '../querymanage.action?type=getinteractivescenetree&a=' + Math.random(),
		editable: true,
		onBeforeExpand: function(node, param) {
			$(fdocid).combotree('tree').tree("options").url = "../querymanage.action?type=createinteractivescenetree&scenariosid="+node.id +'&a='+ Math.random()+"&citySelect="+encodeURI(gCitySelect);
		}
	});
}

function searchNormalQuery(normalQuery) {
	if(normalQuery==""||normalQuery==null||normalQuery==undefined){
		normalQuery = replaceSpace($("#normalqueryselect").combobox("getText"));
	}
	var responseType = $("#responsetypeselect").combobox('getValue');
	var interactType = $("#interacttypeselect").combobox('getValue');
	if (normalQuery == "全部") {
		normalQuery = "";
	}
	if (responseType == "全部") {
		responseType = "";
	}
	if (interactType == "全部") {
		interactType = "";
	}

	_searchNormalQuery({
		serviceid: serviceid,
		normalquery: normalQuery,
		responsetype: responseType,
		interacttype: interactType
	});
}

// 查询标准问题
function _searchNormalQuery(params) {
	if (!params) {
		params = {
			type: "selectnormalquery",
			serviceid: serviceid,
			normalquery: '',
			responsetype: '',
			interacttype: ''

		};
	} else {
		params = {
			type: "selectnormalquery",
			serviceid: params.serviceid ? params.serviceid : '',
			normalquery: params.normalquery ? params.normalquery : '',
			responsetype: params.responsetype ? params.responsetype : '',
			interacttype: params.interacttype ? params.interacttype : '',
		}
	}

	$('#querymanagedatagrid').datagrid('load', params);
}



//查询标准问题下客户问题
function searchCustomerQuery(kbdataid, customerQuery) {
	if(kbdataid && customerQuery){
		
	} else {
		var kbdataid;
		var row = $("#querymanagedatagrid").datagrid('getSelected');
		if (row) {
			kbdataid = row.kbdataid;
		} else {
			kbdataid = "";
		}
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

// 初始化检索条件Panel
function initFilterPanel(options) {
	$('#datagrid_tb').panel({
		width: 500,
		title: '检索条件',
		iconCls: 'icon-search',
		headerCls: 'filterHeaderCls',
		bodyCls: 'filterBodyCls',
		collapsible: true,
		onCollapse: function() {
			// 重新设置datagrid高度
			$("#querymanagedatagrid").datagrid('resize', {
				height: 455
			});
		},
		onExpand: function() {
			// 重新设置datagrid高度
			$("#querymanagedatagrid").datagrid('resize', {
				height: 385
			});
		}
	}).panel('expand');

	$('#datagrid_tb2').panel({
		width: 520,
		title: '检索条件',
		iconCls: 'icon-search',
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
	
	// 设置条件
	var normalquery,
		replytype,
		interacttype;
	if (options) {
		if (options.panel1) {
			var normalquery = options.panel1.normalquery;
			normalquery = normalquery.substr(normalquery.indexOf(">") + 1)
			var replytype = options.panel1.replytype;
			var interacttype = options.panel1.interacttype;
			//展开设置的检索条件
			$('#datagrid_tb').panel('expand');
		}
		if (options.panel2) {
			// TODO
		}
	}
	//创建标准问下拉框
	createNormalqueryCombobox(normalquery ? normalquery : '');
	//创建回复类型下拉
	createResponseTypeCombobox(replytype ? replytype : '');
	//创建交互类型下拉
	createInteractTypeCombobox(interacttype ? interacttype : '');
}

// 加载问题管理列表
function loadQueryManageList(hookObj) {
	var params;
	if (!hookObj) {
		params = {
			type: "selectnormalquery",
			serviceid: serviceid,
			normalquery: '',
			responsetype: '',
			interacttype: ''
		};
	} else {
		params = {
			type: "selectnormalquery",
			serviceid: serviceid,
			normalquery: hookObj.panel1.normalquery ? hookObj.panel1.normalquery : '',
			responsetype: hookObj.panel1.responseType ? hookObj.panel1.responseType : '',
			interacttype: hookObj.panel1.interactType ? hookObj.panel1.interactType : ''
		}
	}

	$("#querymanagedatagrid")
		.datagrid({
			//						height : 450,
			height: 385,
			width: 500,
			url: "../querymanage.action",
			queryParams: params,
			//						toolbar : "#datagrid_tb",
			pageSize: 10,
			pagination: true,
			rownumbers: true,
			striped: true,
			nowrap: false,
			fitColumns: true,
			//singleSelect : true,
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
						width: 180,
						hidden: true
					}, {
						field: 'cityname',
						title: '来源地市',
						width: 300,
						hidden: true
					}, {
						field: 'citycode',
						title: '来源地市',
						width: 300,
						hidden: true
					}, {
						field: 'normalquery',
						title: '标准问题',
						width: 300
					}, {
						field: 'responsetype',
						title: '回复<br/>类型',
						width: 120,
						formatter: function(value, row, index) {
							if (value == "" || value == null) {
								return "未知";
							} else {
								return value;
							}
						}
					}, {
						field: 'interacttype',
						title: '交互<br/>类型',
						width: 120,
						formatter: function(value, row, index) {
							if (value == "" || value == null || value=="0") {
								return "未交互";
							} else if(value=="1"){
								return "已交互";
							}else{
								return value;
							}
						}
					}, {
						field: 'wordpatcount',
						title: '语义',
						align: 'center',
						width: 70,
						formatter: function(value, row, index) {
							var val;
							if (value == "0") {
								val = "<div style='color:red;'>" + value + "</div>";
							} else {
								val = "<div style='color:blue;'>" + value + "</div>";
							}
							return val;
						}
					}, {
						field: 'answercount',
						title: '答案',
						align: 'center',
						width: 70,
						formatter: function(value, row, index) {
							var val;
							if (value == "0") {
								val = "<div style='color:red;'>" + value + "</div>";
							} else {
								val = "<div style='color:blue;'>" + value + "</div>";
							}
							return val;
						}
					}, {
						field: 'relatequerycount',
						title: '相关<br/>问',
						align: 'center',
						width: 70,
						formatter: function(value, row, index) {
							var val;
							if (value == "0") {
								val = "<div style='color:red;'>" + value + "</div>";
							} else {
								val = "<div style='color:blue;'>" + value + "</div>";
							}
							return val;
						}
					}, {
						field: 'extendcount',
						title: '共享<br/>语义',
						align: 'center',
						width: 70,
						formatter: function(value, row, index) {
							var val;
							if (value == "0") {
								val = "<div style='color:red;'>" + value + "</div>";
							} else {
								val = "<div style='color:blue;'>" + value + "</div>";
							}
							return val;
						}
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
					},

					{
						field: '操作',
						title: '操作',
						align: 'center',
						width: 250,

						formatter: function(value, row, index) {
							return '<a href="javascript:void(0)" title="共享语义" class="icon-share btn_a" onclick="openShareWin(event,' 
							+ index 
							+ ')"></a><a href="javascript:void(0)" title="交互规则" class="icon-detail btn_a" onclick="openRuleWin(event,' 
							+ index
							+ ')"></a><a href="javascript:void(0)" title="答案知识" class="icon-answerdetail btn_a" onclick="openFaqWin(event,' 
							+ index 
							+ ')"></a><br/><a href="javascript:void(0)" title="语义知识" class="icon-wordpat btn_a" style="margin-left: -24px;" onclick="openWordpatWin(event,' 
							+ index 
							+ ')"></a><!-- 隐藏相关问题 <a href="javascript:void(0)" title="相关问题" class="icon-help btn_a" onclick="openRelateQueryWin(event,' 
							+ index 
							+ ')"></a>--><a href="javascript:void(0)" title="修改" class="icon-edit btn_a" onclick="openEditNormalQueryWin2(' 
							+ index 
							+ ')"></a>';
						}
					}
					//								,
					//								{
					//									field : '修改',
					//									title : '修改',
					//									align : 'center',
					//									width : 90,
					//									formatter : function(value, row, index) {
					//										return '<a href="javascript:void(0)" title="修改" class="icon-edit btn_a" onclick="openEditNormalQueryWin2(' + index + ')"></a>';
					//									}
					//								}
				]
			],
			onClickRow: function(rowIndex, rowData) {
				$("#customerqueryselect").textbox("setValue", "");
				$("#cityselect").combotree("clear");
				$("#understandstatus").combobox("setValue", "");
				$("#istrain").combobox("setValue", "");
				$('#customerquerydatagrid').datagrid('load', {
					type: "selectcustomerquery",
					serviceid: serviceid,
					kbdataid: rowData.kbdataid,
					customerquery: "",
					citycode: "",
					istrain: "",
					understandstatus: ""
				});
			},
			onLoadSuccess:function (data){
				if(hookObj && hookObj.panel1 && hookObj.panel1.subPage){
					var rowidx = hookObj.panel1.subPage.rowIndex;
					var pagetype = hookObj.panel1.subPage.pageType;
					if(pagetype === '答案页面'){
						openFaqWin(null, 0);
					}
				}
			}
		});
	//	$('#querymanagedatagrid').datagrid('hideColumn', 'systemresult');
	//	$('#querymanagedatagrid').datagrid('hideColumn', 'time');
	//	$('#querymanagedatagrid').datagrid('showColumn', 'delete');


	$("#querymanagedatagrid").datagrid('getPager').pagination({
		showPageList: false,
		buttons: [{
				//			text : "新增",
				iconCls: "icon-add",
				handler: function() {
					$('#add_city_div').hide();
					$('#add_customerquery_div').hide();
					$('#addquerywindow').window('vcenter');
					
					$('#addquerywindow').window({
						title: '新增标准问题',
						height: 320
					}).window('center').window('open');
					
					$("#querytype").combobox('setValue', '标准问题');
					$("#normalqueryinput").val('');
					$("#input_normalquerycombobox_div").show();
					$("#choose_normalquerycombobox_div").hide();
					//创建客户问题citytree
					createCityTree('customerquerycity', 'edit', true);
					//创建标准问下拉框
					createNormalqueryCombobox();
				}
			},
			//		"-", {
			////			text : "修改",
			//			iconCls : "icon-edit",
			//			handler : function() {
			//			openEditNormalQueryWin();
			//			}
			//		}
			//		, 
			"-", {
				//			text : "删除",
				iconCls: "icon-delete",
				handler: function() {
					// openDeleteNormalQueryWin();
					deleteNormalquery();
				}
			},
//			"-", {
//				//			text : "迁移",
//				iconCls: "icon-redo",
//				handler: function() {
//					openTransferNormalQueryWin();
//				}
//			}, 
			"-", {
				text: "全量训练",
				iconCls: "icon-wordpat",
				handler: function() {
						//produceAllWordpat();
						preProduceWordpat('ALL');
					}
					//		},"-",{
					//			text : "全量理解",
					//			iconCls : "icon-answer",
					//			handler : function() {
					//			
					//			}
			},
//			"-", {
//				text: "导出词模",
//				iconCls: "icon-download",
//				handler: function() {
//					exportWordpat(1);
//				}
//			}
		]
	});
	
	var pagerOptions = $("#querymanagedatagrid").datagrid('getPager').pagination("options");
	var newButtons =[];
	$.each(pagerOptions.buttons, function(n, botton){
		
		if(customItem["全量训练"] != null && customItem["全量训练"] == "不显示"
			&& botton.text && botton.text =='全量训练'){
			return;
		}
		if(botton == '-' && newButtons[newButtons.length -1] == '-') {
			return;
		}
		newButtons.push(botton);
	});
	$("#querymanagedatagrid").datagrid('getPager').pagination({buttons:newButtons});
	
}


// serviceid,kbdataid, normalquery, customerquery, citycode
//加载问题管理列表
function loadCustomerQuerydDatagridList(hookObj) {
	var params;
	if (!hookObj) {
		params = {
				type: "selectcustomerquery",
				serviceid: serviceid,
				kbdataid: "",
				normalquery: "",
				customerquery: "",
				citycode: "",
				istrain: "",
				understandstatus: ""
		};
	} else {
		params = {
			type: 'selectcustomerquery',
			serviceid: serviceid,
			kbdataid: hookObj.panel1.kbdataid ? hookObj.panel1.kbdataid : '' ,
			normalquery:  '',
			customerquery: hookObj.panel2.customerquery ? hookObj.panel2.customerquery : '',
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
//			queryParams: {
//				type: "selectcustomerquery",
//				serviceid: serviceid,
//				normalquery: "",
//				customerquery: "",
//				citycode: "",
//				responsetype: "",
//				interacttype: "",
//				kbdataid: ""
//			},
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
	//	$('#querymanagedatagrid').datagrid('hideColumn', 'systemresult');
	//	$('#querymanagedatagrid').datagrid('hideColumn', 'time');
	//	$('#querymanagedatagrid').datagrid('showColumn', 'delete');


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
					}).window('center').window('open');
					
					$("#querytype").combobox('setValue', '客户问题');
					$("#normalqueryinput").val('');
					$("#choose_normalquerycombobox_div").show();
					$("#input_normalquerycombobox_div").hide();
					//创建客户问题citytree
					createCityTree('customerquerycity', 'edit', true);
					//创建标准问下拉框
					createNormalqueryCombobox();
					var kbdataid;
					var row = $("#querymanagedatagrid").datagrid('getSelected');
					if (row) {
						$("#normalquerycombobox").combobox('setValue', row.kbdataid);
					}

				}
			},
			//		"-", {
			////			text : "修改",
			//			alTtext:"修改",
			//			iconCls : "icon-edit",
			//			handler : function() {
			//			
			//			openEditCustomerQueryWin();
			//			}
			//		}
			//		, 
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

//打开迁移编辑框
function openTransferNormalQueryWin() {
	var rows = $('#querymanagedatagrid').datagrid('getSelections');
	if (rows && rows.length > 0) {
		$('#transfernormalquerywin').window('open');
		//		createServiceTree("service");
		$('#service').combotree({
			url: '../querymanage.action?type=createservicetree&a='+ Math.random()+'&citySelect='+encodeURI(gCitySelect), // ajax方式
			onBeforeExpand: function(node, param) {
				$('#service').combotree('options').url = "../querymanage.action?type=createservicetree&scenariosid="
					+ node.id + '&a=' + Math.random()+'&citySelect='+encodeURI(gCitySelect); // 展开时发送请求去加载节点
				
				$('#service').combotree('tree').tree('options').url = "../querymanage.action?type=createservicetree&scenariosid="
					+ node.id + '&a=' + Math.random()+'&citySelect='+encodeURI(gCitySelect); // 展开时发送请求去加载节点
			}
		});
		
		
	} else {
		$.messager.alert('系统提示', "请选择需迁移标准问题!", "warning");
		return;
	}

}

//迁移标准问
function transferNormalQuery() {
	var nserviceid = $("#service").combotree("getValue");
	var serviceName = $("#service").combotree("getText");
	if (nserviceid == "" || nserviceid == null || nserviceid == undefined) {
		return;
	}

	var rows = $('#querymanagedatagrid').datagrid('getSelections');
	var kbdataids = [];
	var abses = []
	for (var i = 0; i < rows.length; i++) {
		kbdataids.push(rows[i].kbdataid);
		abses.push("<" + serviceName + ">" + rows[i].normalquery)
	}
	$.ajax({
		url: '../querymanage.action',
		type: "post",
		data: {
			type: 'transfernormalquery',
			resourcetype: 'querymanage',
			operationtype: 'U',
			resourceid: serviceid,
			serviceid: nserviceid,
			kbdataids: kbdataids,
			abses: abses
		},
		traditional: true,
		async: false,
		dataType: "json",
		success: function(data, textStatus, jqXHR) {

			if (data.success == true) {
				$("#querymanagedatagrid").datagrid("reload");
				$("#customerquerydatagrid").datagrid("reload");
				$('#transfernormalquerywin').window('close');
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

// 打开理解详情窗口
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
//打开标准问题编辑框——行内编辑按钮
function openEditNormalQueryWin2(index) {
	var row = $('#querymanagedatagrid').datagrid('getData').rows[index];
	var queryid = row.queryid;
	var customerquery = row.customerquery;
	var citycode = row.citycode;
	var normalquery = row.normalquery;
	var responsetype = row.responsetype;
	var interacttype = row.interacttype;
	if (interacttype == "" || interacttype == null) {
		interacttype = "未交互";
	}
	
	//针对easyui combobox进行text => id转换
	$.each($('#editinteracttype').combobox('getData'),function(n,item){
		if(item.text == interacttype){
			interacttype = item.id;
		}
	});
	//	if(responsetype!=""&& responsetype!=null){
	//		$('#editresponsetype').combobox('setValues', responsetype.split("\,")); 
	//	} 
	$('#editresponsetype').combobox('setValue', responsetype);
	$("#editnormalqueryinput").textbox('setValue', normalquery);
	$('#editinteracttype').combobox('setValue', interacttype);
	$('#normalquery-rowids').val(index);
	$('#editnormalquerywin').window('center').window('open');
}

//打开标准问题编辑框
function openEditNormalQueryWin() {
	$('#editresponsetype').combobox('clear');
	var rows = $("#querymanagedatagrid").datagrid("getSelections");
	if (rows) {
		if (rows.length == 1) {
			var row = $('#querymanagedatagrid').datagrid('getSelected');
			var queryid = row.queryid;
			var customerquery = row.customerquery;
			var citycode = row.citycode;
			var normalquery = row.normalquery;
			var responsetype = row.responsetype;
			var interacttype = row.interacttype;
			if (interacttype == "" || interacttype == null) {
				interacttype = "未交互";
			}
			//			if(responsetype!=""&& responsetype!=null){
			//				$('#editresponsetype').combobox('setValues', responsetype.split("\,")); 
			//			} 
			$('#editresponsetype').combobox('setValue', responsetype);
			$("#editnormalqueryinput").textbox('setValue', normalquery);
			$('#editinteracttype').combobox('setValues', interacttype.split("\,"));
			$('#editnormalquerywin').window('open');
		} else {
			$.messager.alert('系统提示', "请选择一行记录编辑!", "warning");
			return;
		}
	} else {
		$.messager.alert('系统提示', "请选择一行记录编辑!", "warning");
		return;
	}
}

//编辑标准问题
function editNormalQuery() {
	var normalquery = replaceSpace($("#editnormalqueryinput").textbox("getValue"));
	if (normalquery == "") {
		$.messager.alert('系统提示', '请填写标准问题!', "warning");
		return;
	}
	//	var row = $('#querymanagedatagrid').datagrid('getSelected');
	var index = $('#normalquery-rowids').val();
	var row = $('#querymanagedatagrid').datagrid('getData').rows[index];
	var oldnormalquery = row.normalquery;
	var kbdataid = row.kbdataid;
	var service = row.service;
	var citycode = row.abscity;
	var responsetype = $('#editresponsetype').combobox('getValue');
	var interacttype = $('#editinteracttype').combobox('getValue');
	$.ajax({
		url: '../querymanage.action',
		type: "post",
		data: {
			type: 'updatecustomerquery',
			resourcetype: 'querymanage',
			operationtype: 'U',
			resourceid: serviceid,
			kbdataid: kbdataid,
			service: service,
			normalquery: normalquery,
			oldnormalquery: oldnormalquery,
			responsetype: responsetype + "@@",
			interacttype: interacttype + "@@",
			oldcitycode: "",
			citycode: citycode
		},
		async: false,
		dataType: "json",
		success: function(data, textStatus, jqXHR) {

			if (data.success == true) {
				$("#querymanagedatagrid").datagrid("reload");
				$('#editnormalquerywin').window('close');
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
//打开摘要对应选项卡
function addTab(title, url){
	var content;
	if ($('#aa').tabs('exists', title)){ 
		$('#aa').tabs('close', title);
	}
	content = '<iframe style="width:100%;height:500px;border:0;" src="' + url + '"></iframe>';
	$('#aa').tabs('add',{
		title:title,
		content:content,
		height:'500px',
		closable:true,
		tools:[{    
			iconCls:'icon-mini-refresh',
			handler:function(){
				var tab = $('#aa').tabs('getSelected');  // 获取选择的面板
				$('#aa').tabs('update', {
					tab: tab,
					options: {
						content:content
					}
				});
			}
		}] 
	});
}
//打开词模编辑页对话框
function openWordpatWin(event, index) {
	if (event.stopPropagation) { // Mozilla and Opera
		event.stopPropagation();
	} else if (window.event) { // IE
		window.event.cancelBubble = true;
	}
	var row = $('#querymanagedatagrid').datagrid('getData').rows[index];
	var service = row.service;
	var kbdataids = row.kbdataid;
	var cityids = row.abscity;
	var brand = row.brand;
	var topic = row.topic;
	var _abstract = row.abs;
	
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
//	$.ajax({
//		url: '../querymanage.action',
//		type: "post",
//		data: {
//			type: 'getaddress',
//			key: "问题库管理之词模管理"
//		},
//		async: false,
//		dataType: "json",
//		success: function(data, textStatus, jqXHR) {
//			var url = "";
//			if (data.success == true) {
//				url = data.url;
//			} else {
//				$.messager.alert('系统异常', "未配置菜单地址!", "error");
//				return;
//			}
//			
//		},
//		error: function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
//		}
//	});
	
	
}

//打开答案编辑页对话框
function openFaqWin(event, index) {
	var row = $('#querymanagedatagrid').datagrid('getData').rows[index];
	var service = row.service;
	var kbdataids = row.kbdataid;
	var cityids = row.abscity;
	var brand = row.brand;
	var topic = row.topic;
	var _abstract =row.abs;
	_openFaqWin(row);
}

function _openFaqWin(param) {
	var service = param.service;
	var kbdataids = param.kbdataid;
	var cityids = param.abscity;
	var brand = param.brand;
	var topic = param.topic;
	var _abstract =param.abs;
	var url = '../faq/faq.html?wordpattype=1&serviceids=' + encodeURI(serviceid) + '&cityids=' + encodeURI(cityids) + '&brand=' + encodeURI(brand) + '&service=' + encodeURI(service) + '&kbdataids=' + encodeURI(kbdataids) + '&topic=' + encodeURI(topic) + '&_abstract=' + encodeURI(_abstract)+ '&ioa=' + encodeURI(ioa);
	if(showInTab){
		addTab('答案详情【' + _abstract.split(">")[1] + '】',url);
	}else{
		var con = '<iframe style="width:100%;height:98%;border:0;" src="' + url + '"></iframe>';
		$('#faqwin').html(con);
		$('#faqwin').window({
			title: '答案详情【' + _abstract.split(">")[1] + '】',
			collapsible: false,
			minimizable: false,
			maximizable: false,
			draggable: false,
			resizable: false
		}).window('open');
	}
}


//打开相关问题页
function openRelateQueryWin(event, index) {
	var row = $('#querymanagedatagrid').datagrid('getData').rows[index];
	var kbdataid = row.kbdataid;
	var _abstract = row.abs;
	var url = '../relatequery/relatequery.html?&serviceids=' + encodeURI(serviceid) + '&kbdataid=' + encodeURI(kbdataid);
	var con = '<iframe style="width:100%;height:98%;border:0;" src="' + url + '"></iframe>';
	$('#relatequerywin').html(con);
	$('#relatequerywin').window({
		title: '相关问题详情【' + _abstract.split(">")[1] + '】',
		collapsible: false,
		minimizable: false,
		maximizable: false,
		draggable: false,
		resizable: false
	}).window('open');
//	addTab('相关问题详情【' + _abstract.split(">")[1] + '】',url);
}


//打开交互规则页
function openRuleWin(event, index) {

	var row = $('#querymanagedatagrid').datagrid('getData').rows[index];
	var kbdataid = row.kbdataid;
	var _abstract =row.abs;
	$.ajax({
		url: '../querymanage.action',
		type: "post",
		data: {
			type: 'findscenarios',
			kbdataid: kbdataid,
			key: "问题库管理之交互规则"
		},
		async: false,
		dataType: "json",
		success: function(data, textStatus, jqXHR) {
			if (data.success == true) {
				var scenariosid = data.scenariosid;
				var name = data.name;
				var url = data.url;
				url = url + '?scenariosid=' + encodeURI(scenariosid) + '&name=' + encodeURI(name) + '&ioa=' + encodeURI(ioa) + '&userid=' + encodeURI(userid);
				if(showInTab){
					addTab('交互规则【' + _abstract.split(">")[1] + '】',url);
				}else{
					var con = '<iframe style="width:100%;height:98%;border:0;" src="' + url + '"></iframe>';
					$('#rulewin').html(con);
					$('#rulewin').window('open');
				}
			} else {
				$('#kbdataid').val(kbdataid);
				$('#rowindex').val(index);
				$("#normalquerytoscenarioswin").window('open');
				createInteractiveSceneTree("scenariosname");
				//$.messager.alert('提示', "当前标准问题未配置场景,如需配置请到场景配置页配置!", "warning");
				return;
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}


//共享语义
function openShareWin(event, index) {
	//	 $.messager.alert('提示', "Waiting for me to do!", "info");
	if (event.stopPropagation) { // Mozilla and Opera
		event.stopPropagation();
	} else if (window.event) { // IE
		window.event.cancelBubble = true;
	}
	var row = $('#querymanagedatagrid').datagrid('getData').rows[index];
	var service = row.service;
	var kbdataid = row.kbdataid;
	var cityids = row.abscity;
	var brand = row.brand;
	var topic = row.topic;
	var abstracts = row.abs;
	var url = encodeURI('../extend/extend.jsp?service=' + service + '&serviceid=' + serviceid + '&kbdataid=' + kbdataid + '&cityids=' + cityids + '&brand=' + brand + '&topic=' + topic + '&abstracts=' + abstracts + '&ioa=' + ioa + '&question=' + row.normalquery);
	if(showInTab){
		addTab('共享语义【' + row.normalquery + '】',url);
	}else{
		var con = '<iframe style="width:100%;height:98%;border:0;" src="' + url + '"></iframe>';
		$('#sharewin').html(con);
		$('#sharewin').window({
			title: '共享语义【' + row.normalquery + '】',
			collapsible: false,
			minimizable: false,
			maximizable: false,
			draggable: false,
			resizable: false
		}).window('open');
	}
}


//客户问共享语义
function _openShareWin(event, index) {
	if (event.stopPropagation) { // Mozilla and Opera
		event.stopPropagation();
	} else if (window.event) { // IE
		window.event.cancelBubble = true;
	}
	var row = $('#customerquerydatagrid').datagrid('getData').rows[index];
	var service = row.service;
	var kbdataid = row.kbdataid;
	var cityids = row.abscity;
	var brand = row.brand;
	var topic = row.topic;
	var abstracts = row.abs;
	var url = encodeURI('../extend/extend.jsp?service=' + service + '&serviceid=' + serviceid + '&kbdataid=' + kbdataid + '&cityids=' + cityids + '&brand=' + brand + '&topic=' + topic + '&abstracts=' + abstracts + '&ioa=' + ioa + '&question=' + row.customerquery);
	var con = '<iframe style="width:100%;height:98%;border:0;" src="' + url + '"></iframe>';
	$('#sharewin').html(con);
	$('#sharewin').window({
		title: '共享语义【' + row.customerquery + '】',
		collapsible: false,
		minimizable: false,
		maximizable: false,
		draggable: false,
		resizable: false
	}).window('open');
//	addTab('共享语义【' + row.customerquery + '】',url);
}


//保存标准问场景对应关系
function saveScenarios() {
	var kbdataid = $('#kbdataid').val(),
		scenariosid = $('#scenariosname').combotree('getValue'),
		rowindex = $('#rowindex').val();

	if (!kbdataid || kbdataid == '') {
		return;
	}
	if (!rowindex || rowindex == '') {
		return;
	}

	if (!scenariosid || scenariosid == '') {
		$.messager.alert('系统提示', "请选择场景!", "warning");
	}

	$.ajax({
		url: '../querymanage.action',
		type: "post",
		data: {
			type: 'insertscenarios2kbdata',
			resourcetype: 'querymanage',
			operationtype: 'U',
			resourceid: serviceid,
			kbdataid: kbdataid,
			scenariosid: scenariosid
		},
		async: false,
		dataType: "json",
		success: function(data, textStatus, jqXHR) {
			if (!data.success) {
				$.messager.alert('系统异常', "标准问场景配置出错，请尝试重新配置！", "error");
				$("#normalquerytoscenarioswin").window('close');
			} else {
				$.messager.alert('系统提示', "标准问场景配置成功！", "info", function() {
					$("#normalquerytoscenarioswin").window('close');
					openRuleWin(null, rowindex);
				});
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			//			$.messager.alert('系统异常', "请求数据失败!", "error");
			$("#normalquerytoscenarioswin").window('close');
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
			combition.push(rows[i].citycode + "@#@" + rows[i].customerquery + "@#@" + rows[i].kbdataid + "@#@" + rows[i].queryid);
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
			type: 'producewordpat',
			resourcetype: 'querymanage',
			operationtype: 'A',
			resourceid: serviceid,
			combition: combition.join("@@"),
			flag:wordpattype
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

//全量生成词模
function produceAllWordpat(wordpattype) {
	wordpattype = wordpattype || "5";//默认为自学习词模
	$.messager.confirm("操作提示", "全量语义训练需要让您等待一会会哦，确定训练吗？", function(data) {
		if (data) {
			$("#querymanagedatagrid").datagrid('loading');
			$("#customerquerydatagrid").datagrid('loading');
			$(".datagrid-mask-msg").text('请耐心等待,语义训练中......');
			$.ajax({
				url: '../querymanage.action',
				type: "post",
				data: {
					type: 'produceallwordpat',
					resourcetype: 'querymanage',
					operationtype: 'A',
					resourceid: serviceid,
					serviceid: serviceid,
					flag:wordpattype
				},
				async: false,
				dataType: "json",
				success: function(data, textStatus, jqXHR) {
					$("#querymanagedatagrid").datagrid('loaded');
					$("#customerquerydatagrid").datagrid('loaded');
					var downloadUrl = '';
					if(data.fileName){
						downloadUrl = '</br>生成报告：</br>'
							+'<a href="../querymanageexport.action?type=wordpatexport&fileName='+data.fileName
							+'" download="'+data.fileName+'" title="下载" >'+data.fileName+'</a>';
					}
					
					if (data.success == true) {
						$("#querymanagedatagrid").datagrid("load");
						$("#customerquerydatagrid").datagrid("load");
						$.messager.alert('系统提示', data.msg+downloadUrl, "info");
					} else {
						$.messager.alert('系统提示', data.msg+downloadUrl, "warning");
					}
				},
				error: function(jqXHR, textStatus, errorThrown) {
					$("#querymanagedatagrid").datagrid("loaded");
					//					$.messager.alert('系统异常', "请求数据失败!", "error");
				}
			});
		}
	});
}



//取消按钮事件
function btnCancel(id) {
	$('#' + id).window('close');
}

//打开删除标准问题对话框
function openDeleteNormalQueryWin() {
	createNormalqueryCombobox();
	$('#deletenormalquerywin').window('open');
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
//批量删除标准问题
function deleteNormalquery() {
	// var kbdataid  = $('#deletenormalquery').combobox('getValues'); 
	var kbdataid = [];
	var rows = $("#querymanagedatagrid").datagrid("getSelections");
	if (!rows || rows.length < 1) {
		$.messager.alert('系统提示', "请选择一行记录删除!", "warning");
		return;
	}

	for (var i = 0; i < rows.length; i++) {
		kbdataid.push(rows[i].kbdataid);
	}

	$.messager.confirm("系统提示", "删除标准问题将会级联删除其下面所有内容，您确定要删除吗？", function(data) {
		if (data) {
			$.ajax({
				type: "post",
				async: false,
				url: "../querymanage.action",
				data: {
					type: "deletenormalquery",
					resourcetype: 'querymanage',
					operationtype: 'D',
					resourceid: serviceid,
					kbdataid: kbdataid.join(',') + "@@"
				},
				dataType: "json",
				success: function(data, textStatus, jqXHR) {
					$.messager.alert('系统提示', data["msg"], "info");
					if (data["success"] == true) {
						$("#querymanagedatagrid").datagrid("reload");
						$("#customerquerydatagrid").datagrid("reload");
						createNormalqueryCombobox();
						//$('#deletenormalquerywin').window('close');
					}
				},
				error: function(jqXHR, textStatus, errorThrown) {
					//					$.messager.alert('系统异常', "请求数据失败!", "error");
				}
			});
		}
	});
}


// 删除回归问题
function deleteregress(event, index) {
	if (event.stopPropagation) { // Mozilla and Opera
		event.stopPropagation();
	} else if (window.event) { // IE
		window.event.cancelBubble = true;
	}
	$("#querymanagedatagrid").datagrid('selectRow', index);
	var row = $("#querymanagedatagrid").datagrid('getSelected');
	$.messager.confirm("系统提示", "您确定要删除吗？", function(data) {
		if (data) {
			$.ajax({
				type: "post",
				async: false,
				url: "../regresstest.action",
				data: {
					type: "delete",
					question: row.question,
					extendquestion: row.extendquestion
				},
				dataType: "json",
				success: function(data, textStatus, jqXHR) {
					$.messager.alert('系统提示', data["msg"], "info");
					if (data["success"] == true) {
						$("#querymanagedatagrid").datagrid('reload');
					}
				},
				error: function(jqXHR, textStatus, errorThrown) {
					$.messager.alert('系统异常', "请求数据失败!", "error");
				}
			});
		}
	});
}


//添加问题类型onselect 事件
function normalQueryOnSelect() {
	$("#normalqueryselect").combobox({
		onSelect: function(res) {
			var normalQuery = res.text;
			searchNormalQuery(normalQuery);
		}
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
				createNormalqueryCombobox();
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



//创建标准问下拉框
function createNormalqueryCombobox(defaultVal) {
	var url = '../querymanage.action?type=createnormalquerycombobox&flag=select&serviceid=' + serviceid + '&a=' + Math.random();
	createCombobox('normalqueryselect', url, false, true, function() {
		$('#normalqueryselect').combobox('setValue', defaultVal);
	});
	url = '../querymanage.action?type=createnormalquerycombobox&serviceid=' + serviceid + '&a=' + Math.random();
	createCombobox('normalquerycombobox', url, false, true);
	url = '../querymanage.action?type=createnormalquerycombobox&serviceid=' + serviceid + '&a=' + Math.random();
	createCombobox('deletenormalquery', url, true, false);

}

//创建回复类型下拉框
function createResponseTypeCombobox(defaultVal) {
	var url = '../querymanage.action?type=createresponsetypecombobox&flag=select&a=' + Math.random();
	createCombobox('responsetypeselect', url, false, true, function() {
		$('#responsetypeselect').combobox('setValue', defaultVal);
	});
	url = '../querymanage.action?type=createresponsetypecombobox&a=' + Math.random();
	createCombobox('editresponsetype', url, false, false);
}
//创建交互类型下拉框
function createInteractTypeCombobox(defaultVal) {
	var url = '../querymanage.action?type=createinteracttypecombobox&flag=select&a=' + Math.random();
	createCombobox('interacttypeselect', url, false, false, function() {
		$('#interacttypeselect').combobox('setValue', defaultVal);
	});
	url = '../querymanage.action?type=createinteracttypecombobox&a=' + Math.random();
	createCombobox('editinteracttype', url, false, false);
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

function addQueryAct() {
	var selectOption = $("#querytype").combobox('getValue');
	var param, title, content;
	if (selectOption == "客户问题") {
		var customerQuery = $("#customerquerytextarea").val();
		param = {
			type: 'findcustomerquery',
			customerquery: customerQuery,
			citySelect: gCitySelect
		};
		title = '在其他业务下有此客户问题';
	} else { //标准问题
		var normalQuery = $("#normalqueryinput").val();
		param = {
			type: 'findnormalquery',
			normalquery: normalQuery,
			citySelect: gCitySelect
		};
		title = '在其他业务下有此标准问题';
	}

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
//	
//	$.post('../querymanage.action', param, function(result) {
//		// 问题存在
//		if (result && result.length > 0) {
//			var tabs = [];
//			for (var j = 0; j < result.length; j++) {
//				var lis = [];
//				var query = (selectOption == "客户问题" ? result[j].customerquery : result[j].normalquery);
//				var data = result[j].duplicate;
//
//				for (var i = 0; i < data.length; i++) {
//					lis.push('<tr><td>' + (i + 1) + '</td><td>' + data[i].servicepath + '</td></tr>');
//				}
//				tabs.push('<div class="datagrid-body" style="color:blue; padding: 5px;"><strong style="color:black">' + query + '</strong><table><tr><th>No.</th><th>业务路径</th></tr>' + lis.join('') + '</table></div>');
//			}
//			content = tabs.join('');
//
//			$('#add-dd').dialog({
//				width: 450,
//				title: title,
//				modal: true,
//				content: content,
//				buttons: [{
//					text: '确认',
//					handler: function() {
//						$(this).linkbutton('disable').next().linkbutton('disable');
//						addQuery();
//					}
//				}, {
//					text: '取消',
//					handler: function() {
//						$('#add-dd').dialog('close');
//					}
//				}]
//			});
//
//		} else { // 问题不存在
//			addQuery();
//		}
//	}, 'json')
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
		
		//		 if(customerQuery == "多条以回车分隔"){
		//			 customerQuery="";
		//		 }
		//	     
		//	     if(customerQuery!=""&&customerQuery!=null){
		//	    	 if(cityCode==""||cityCode==null){
		//	 			$.messager.alert('系统提示', '请选择地市!', 'warning'); 
		//	 			 return;
		//	 		}
		//	     }
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
		type: "addquery",
		querytype: selectOption,
		normalquery: normalQuery,
		multinormalquery: multiNormalQuery,
		citycode: cityCode,
		customerquery: customerQueryAll,
		serviceid: serviceid
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
				if (selectOption == "标准问题") { //重新加载标准问题下拉框
					createNormalqueryCombobox();
				}
				$('#addquerywindow').window('close');
				$("#querymanagedatagrid").datagrid('load');
				$("#customerquerydatagrid").datagrid('load');
				if(errorLine.length > 0){
					$.messager.alert('警告','以下行因超出50字限制未导入：第 '+errorLine.join(',')+' 行');
				}else{
					$.messager.alert('系统提示', data.msg, "info");
					// 展示oov分词
					if (selectOption == "标准问题") {
						var oovWord = data.oovWord;
						if (oovWord != null && oovWord != '') {
							getOOVWord(oovWord, normalQuery);
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
	
//	
//	$.post("../querymanage.action", dataStr, function(data) {
//		if (data.success) {
//			if (selectOption == "标准问题") { //重新加载标准问题下拉框
//				createNormalqueryCombobox();
//			}
//			$('#addquerywindow').window('close');
//			$("#querymanagedatagrid").datagrid('load');
//			$("#customerquerydatagrid").datagrid('load');
//			$.messager.alert('系统提示', data.msg, "info");
//		} else {
//			$.messager.alert('系统提示', data.msg, "warning");
//		}
//		$('#add-dd').dialog('close');
//		$(savebtn).linkbutton('enable');
//	}, "json");
}

// 查询回归测试后的分析结果
function searchtestquery() {
	$('#querymanagedatagrid').datagrid('load', {
		type: "selectqueryresult",
		starttime: $("#starttime").datebox('getValue'),
		endtime: $("#endtime").datebox('getValue'),
		question: $.trim($("#questionselect").textbox("getValue"))
	});
	$('#querymanagedatagrid').datagrid('showColumn', 'systemresult');
	$('#querymanagedatagrid').datagrid('showColumn', 'time');
	$('#querymanagedatagrid').datagrid('hideColumn', 'delete');
}

// 提交上传文件
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

// 将excel文件中的数据导入到数据库中
function importExcel(name) {
	$("#querymanagedatagrid").datagrid('loading');
	$(".datagrid-mask-msg").text('请耐心等待,导入问题中......');

	$("#customerquerydatagrid").datagrid('loading');
	$(".datagrid-mask-msg").text('请耐心等待,导入问题中......');

	$.ajax({
		type: "post",
		url: "../querymanage.action",
		data: {
			type: 'import',
			resourcetype: 'querymanage',
			operationtype: 'A',
			resourceid: serviceid,
			filename: name,
			serviceid: serviceid
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
				createNormalqueryCombobox();
				_searchNormalQuery();
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


//导出标准问题
/*
function exportFile(){
	$.messager.confirm("操作提示", "确定导出标准问题吗?", function(data) {
		if (data) {
			var cvalue = getCookieDecode("user.cookie");
			var s = cvalue.split(',')[5];
			var request={
					sqlid:"select_normalquery",
	                paras : [$("#starttime").datebox('getValue')+" 00:00:00", $("#endtime").datebox('getValue')+" 23:59:59"],
	                rows:1000
				};
	            hef ="filename=regressquery.xls&request="+JSON.stringify(request);
				location.href='/KM/file/export?'+hef;
		}
	});
}
*/
function exportExcel() {
	var param = {
		normalquery: replaceSpace($("#normalqueryselect").combobox("getText")),
		responsetype: $("#responsetypeselect").combobox('getValue'),
		interacttype: $("#interacttypeselect").combobox('getValue'),
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

function exportWordpat(flag){
	//采用jquery easyui loading css效果   
	var ajaxLoading = function(message) { 
		$("<div class=\"datagrid-mask\"></div>").css({display:"block",width:"100%",height:$(window).height()}).appendTo("body");
	    $("<div class=\"datagrid-mask-msg\"></div>").html("<span style='font-size:12px;'>"+message+"</span>").appendTo("body").css({display:"block",left:($(document.body).outerWidth(true) - 190) / 2,top:($(window).height() - 45) / 2});
	};
	ajaxLoading("正在生成文件...");
	var ajaxLoaded = function() {   
		$(".datagrid-mask").remove();   
		$(".datagrid-mask-msg").remove();               
	};
	$.ajax({
		url: '../querymanage.action',
		type: "post",
		data: {
			type: 'createwordpatexport',
			serviceid: serviceid,
			flag:flag
		},
		async: false,
		dataType: "json",
		success: function(data, textStatus, jqXHR) {
			ajaxLoaded();
			if (data.success == true) {
				//开始下载文件
				$('#qm_form').form('submit', {
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
function exportCustomerQuery(flag){
	//采用jquery easyui loading css效果   
	var ajaxLoading = function(message) { 
		$("<div class=\"datagrid-mask\"></div>").css({display:"block",width:"100%",height:$(window).height()}).appendTo("body");
	    $("<div class=\"datagrid-mask-msg\"></div>").html("<span style='font-size:12px;'>"+message+"</span>").appendTo("body").css({display:"block",left:($(document.body).outerWidth(true) - 190) / 2,top:($(window).height() - 45) / 2});
	};
	ajaxLoading("正在生成文件...");
	var ajaxLoaded = function() {   
		$(".datagrid-mask").remove();   
		$(".datagrid-mask-msg").remove();               
	};
	$.ajax({
		url: '../querymanage.action',
		type: "post",
		data: {
			type: 'createqueryexport',
			serviceid: serviceid,
			flag:flag
		},
		async: false,
		dataType: "json",
		success: function(data, textStatus, jqXHR) {
			ajaxLoaded();
			if (data.success == true) {
				//开始下载文件
				$('#qm_form').form('submit', {
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
// 开始进行回归测试
function begintest() {
	$("#wrongcount").textbox("setValue", "");
	$("#testtime").textbox("setValue", "");
	$("#querymanagedatagrid").datagrid('loading');
	$(".datagrid-mask-msg").text('初始化......');
	$.ajax({
		type: "post",
		async: true,
		url: "../regresstest.action",
		data: {
			type: "findtest"
		},
		dataType: "json",
		success: function(data, textStatus, jqXHR) {
			if (data["flag"] == true) {
				var servicetype = data["servicetype"];
				lookprogress();
				regresstest(servicetype);
			} else {
				$("#querymanagedatagrid").datagrid('loaded');
				$.messager.alert('信息提示', '回归测试正在使用,请等待其他人使用完毕!', "info");
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			$("#querymanagedatagrid").datagrid('loaded');
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 进行回归测试
function regresstest(servicetype) {
	$.ajax({
		type: "post",
		url: "../regresstest.action",
		data: {
			type: "regresstest",
			servicetype: servicetype
		},
		async: true,
		dataType: "json",
		success: function(data, textStatus, jqXHR) {

		},
		error: function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 查看进度
function lookprogress() {
	var timer = setInterval(function() {
		$.ajax({
			type: "post",
			async: true,
			url: "../regresstest.action",
			data: {
				type: "progress"
			},
			timeout: 1800000,
			dataType: "json",
			success: function(data, textStatus, jqXHR) {
				var p = parseInt(data["status"]);
				if (p == 0) {
					$(".datagrid-mask-msg").text('初始化......');
				} else if (p == 1) {
					var testcount = data["testcount"];
					var totalcount = data["totalcount"];
					var rate = testcount + "/" + totalcount;
					$(".datagrid-mask-msg").text('正在进行回归测试中,已进行：' + rate);
				} else if (p == 2) {
					$(".datagrid-mask-msg").text('分析完毕,保存中......');
				} else if (p == 3) {
					clearInterval(timer);
					$("#querymanagedatagrid").datagrid('loaded');
					$.messager.alert('信息提示', "回归测试成功!", "info");
					$("#wrongcount").textbox("setValue", data["wrongcount"]);
					$("#testtime").textbox("setValue", data["testtime"]);
					searchtestquery();
				} else if (p == 4) {
					clearInterval(timer);
					$("#querymanagedatagrid").datagrid('loaded');
					$.messager.alert('信息提示', "回归测试失败!", "warning");
					$("#wrongcount").textbox("setValue", data["wrongcount"]);
					$("#testtime").textbox("setValue", data["testtime"]);
					searchtestquery();
				} else if (p == 5) {
					clearInterval(timer);
					$("#querymanagedatagrid").datagrid('loaded');
					$.messager.alert('信息提示', "回归问题为空!", "warning");
					$("#wrongcount").textbox("setValue", data["wrongcount"]);
					$("#testtime").textbox("setValue", data["testtime"]);
					searchtestquery();
				} else {
					$("#querymanagedatagrid").datagrid('loaded');
					searchtestquery();
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				$.messager.alert('系统异常', "请求数据失败!", "error");
			}
		});
	}, 2000);
}

function getRowIndex(target) {
	var tr = $(target).closest('tr.datagrid-row');
	return parseInt(tr.attr('datagrid-row-index'));
}

// 批量理解
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

//报错功能
function reportError() {


	var stand_abstract = "";
	var synonymy_abstract = "";
	var cityids = "";
	var reason = "";
	var understandresult = "";
	stand_abstract = $('#understandnormalquery_div').text();
	synonymy_abstract = $('#understandcustomerquery_div').text();
	cityids = $('#understandcitycode_div').text();
	reason = $('#understandreason_div').text();
	understandresult = $('#understandresult_div').html();

	$.ajax({
		type: "POST",
		url: "../extend/reportError.action",
		data: {
			stand_abstract: stand_abstract,
			synonymy_abstract: synonymy_abstract,
			city: cityids,
			reason: reason,
			understandresult: understandresult
		},
		success: function(data, textStatus, jqXHR) {
			var detaildata = data["result"];
			//			console.log(detaildata);
			$.messager.alert('系统提示', detaildata, 'info');
		},
		error: function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统提示', detaildata, 'info');
		}
	});

}

//获取新词
function getOOVWord(oovWord, normalQuery) {
	$('#addwordwindow').window('open');
	var wordArray = oovWord.split("$_$");
	var wordHtml = '<input type="hidden" id="addwordwindow-query" value=" ' + normalQuery + '">';
	wordHtml += '<table cellspacing="0" cellpadding="0">';
	wordHtml += '<tr><td style="padding:5px;"><span>选择</span></td><td style="padding:5px;"><span>分词</span></td><td style="padding:5px;"><span>是否重要</span></td><td style="padding:5px;"><span>是否业务词</span></td></tr>';
	for (var i = 0; i < wordArray.length; i++) {
		wordHtml += '<tr><td style="padding:5px;"><input type="checkbox" name="wordcheckbox" id="wordcheckbox_' + i + '" value="' + wordArray[i] + '" /></td><td style="padding:5px;"><span>' + wordArray[i] + '</span></td><td style="padding:5px;"><select id="levelcombobox_' + i + '" style="width:100px;"><option value="0">重要</option><option value="1" selected>不重要</option></select></td>';
		wordHtml += '<td style="padding:5px;"><select id="businesscombobox_' + i + '" style="width:100px;"><option value="0">是</option><option value="1" selected>否</option></select></td>';
		wordHtml += '</tr>';
	}
	wordHtml += '</table>';
	$("#addwordtable").html(wordHtml);
}

function addWordAct() {
	var word = [];
	var wordlevel = [];
	var wordbusiness = [];
	//业务词
	var businesswords = "";
	var wordlen = $("#addwordtable input[name='wordcheckbox']").length;
	for (var i = 0; i < wordlen; i++) {
		if ($('#wordcheckbox_' + i).is(':checked')) {
			word.push($('#wordcheckbox_' + i).val());
			wordlevel.push($('#levelcombobox_' + i).val());
			if("0" == $('#businesscombobox_' + i).val()){//判断是否是业务词
				wordbusiness.push($('#wordcheckbox_' + i).val());
			}
			
		}
	}
	
	if (word.length == 0) {
		$.messager.alert('系统提示', '请至少选择一个新词', "info");
		return;
	}
	// 要判断每一个词是否在问题中出现，如果不出现，需要给出提示，让用户重新添加，
	var query = $("#addwordwindow-query").val();
	var result = true;
	$("#addwordtable input[name='wordcheckbox']:checked").each(function () {
		var w = $(this).val();
		if (query.indexOf(w) == -1) {
			$.messager.alert('系统提示', '当前分词【' + w + '】在标准问题中不存在，请选择其他分词', "info");
			result = false;
		}
   });
	if(!result){
		return;
	}
	//判断业务词是否连续
	if(query.indexOf(wordbusiness.join('')) == -1 ){//业务词不连续
		$.messager.alert('系统提示', '选择的业务词【' + wordbusiness.join('，') + '】在标准问题中不连续，只能选择一个作为业务词', "info");
		return;
	}
	
	// 将用户添加的新词从问题中去掉，替换成空格，这样形成新的问题2
	var newquery = query;
	$("#addwordtable input[name='wordcheckbox']:checked").each(function () {
		var w = $(this).val();
		newquery = newquery.replace(w, " ");
  });
	$.ajax( { 
		url : '../querymanage.action',
		type : "post",
		data : {
			type : 'addWord',
			serviceid : serviceid ,
			combition : word.join('#'),
			normalquery : trim(query),
			newnormalquery: newquery,
			flag : wordlevel.join('#'),
			businesswords: wordbusiness.join('-')
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if (data.success) {
				$('#addwordwindow').window('close');
			} else {
				$.messager.alert('系统提示', data.msg, "info");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
		}
	});
}
