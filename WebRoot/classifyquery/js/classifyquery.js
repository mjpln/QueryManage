
$(function(){
	var urlparams = new UrlParams();// 所有url参数
	// kbdataid = decodeURI(urlparams.kbdataid);
	loadClassifyQueryFilters();
	loadClassifyQueryList();
	// 地市树形下拉框
	createCityTree('classifyquery_city', 'edit', false);
	// 模型级别下拉框
	createModelTree('classifyquery_service', 'classifyquery_abstract');
	// 标准问题下拉框
	// createNormalqueryCombobox();
	// 加载相互竞则
	initConstraints();
	
	// searchQuery();
});

function searchQuery(){
	var param = {
		classifyquery : replaceSpace($('#classifyquery_query').combobox('getText')),
		city: $('#classifyquery_city').combotree('getValue'),
		serviceid : $('#classifyquery_service').combotree('getValue'),
		normalquery : replaceSpace($('#classifyquery_abstract').combobox('getText')),
		classified : $('#classifyquery_classified').combobox('getValue'),
		checked : $('#classifyquery_checked').combobox('getValue'),
		checktimeStart : $('#classifyquery_checktime_from').datetimebox('getValue'),
		checktimeEnd : $('#classifyquery_checktime_to').datetimebox('getValue'),
		inserttimeStart : $('#classifyquery_inserttime_from').datetimebox('getValue'),
		inserttimeEnd : $('#classifyquery_inserttime_to').datetimebox('getValue')
	};
	
	if(param.normalquery === '全部'){
		param.normalquery = '';
	}
	
	if(param.classified === '全部'){
		param.normalquery = '';
	}
	
	if(param.checked === '全部'){
		param.normalquery = '';
	}
	
	$("#classifyquery_dg").datagrid('load',param);
}

function clearQuery(){
	$('#classify_form').form('reset');
	$('#classifyquery_service').combotree('clear');
	$('#classifyquery_city').combotree('clear');
	$('#classifyquery_checktime_from').datetimebox('disable');
	$('#classifyquery_checktime_to').datetimebox('disable');
}

function loadClassifyQueryFilters(){
	
}

function loadClassifyQueryList(){
	$("#classifyquery_dg").datagrid( {
		height : 450,
		url : "../querymanage.action?type=selectclassifyquery",
		pageSize : 10,
		pagination : true,
		rownumbers : true,
		striped : true,
		nowrap : false,
		fitColumns : true,
		singleSelect : false,
		loadMsg : "数据加载中,请稍后……",
		remoteSort:false,
		columns : [ [ {
			field : 'ck',
			checkbox : true
		}, {
			field : 'id',
			title : 'ID',
			hidden: true
		}, {
			field : 'query',
			title : '客户问题',
			width : 180,
			styler: function(v,r,i){
				return 'font-weight:bold;';
			}
		}, {
			field : 'applyname',
			title : '来源应用渠道',
			width : 150
		}, {
			field : 'channel',
			title : '来源技术渠道',
			width : 150
		}, {
			field : 'city',
			title : '来源地市',
			width : 300
		}, {
			field : 'service',
			title : '模型级别',
			width : 340
		}, {
			field : 'abstract',
			title : '标准问题',
			width : 150
		}, {
			field : 'classified',
			title : '分类状态',
			width : 150,
			align : 'center',
			formatter: function(value){
				return value === 1 ? '分配' : '未分类';
			},
			styler: function(v,r,i){
				if(v === 1){
					return 'font-weight:bold;color:#c00';
				}
			}
		}, {
			field : 'ischecked',
			title : '审核状态',
			width : 150,
			align : 'center',
			formatter: function(value){
				return value === 1 ? '审核' : '未审核';
			},
			styler: function(v,r,i){
				if(v === 1){
					return 'font-weight:bold;color:#c00';
				}
			},
			sortable:true,
			sorter:function(a,b){
				return a > b ? -1 : 1
			}
		}, {
			field : 'checktime',
			title : '审核时间',
			width : 150
		}, {
			field : 'inserttime',
			title : '导入时间',
			width : 150
		}
		] ],
		rowStyler:function(i,r){
			if(r.ischecked === 1){
				return 'background-color:#929292;color:#fff;';
			}
			/*
			 * if(r.classified === 1){ return
			 * 'background-color:#7094cc;color:#fff;'; }
			 */
		},
		loadFilter:function(data){
			var sortedData = {rows:_.sortBy(_.sortBy(_.sortBy(_.sortBy(data.rows, 'query'), 'applyname'), 'channel'), 'city'),total:data.total};
			//console.log('sorted data: \n',JSON.stringify(sortedData));
			return sortedData;
		},
		onLoadSuccess:function(data){
			mergeCells(data);
			initButtons(data.rows);
		},
		onSelect:onCheck,
		onCheck:onCheck,
		onCheckAll:function(rows){
			for(var i = 0; i < rows.length; i++){
				if(rows[i].ischecked === 1){
					$('#classifyquery_dg').datagrid('unselectRow', i);
				}
			}
			var headerChk = $('.datagrid-header-check input[type="checkbox"]')
			if(!headerChk.is(':checked')){
				headerChk.prop('checked', true);
			}
		}
	});
	
	// 添加工具栏
	$("#classifyquery_dg").datagrid('getPager').pagination( {
		showPageList : false,
		buttons : "#btnDiv"
	});
	
	/**
	 * 修改全选功能
	 */
	function onCheck(i, r){
		//debugger;
		if(r.ischecked === 1){
			$("#classifyquery_dg").datagrid('unselectRow', i);
		}
		var rows = $("#classifyquery_dg").datagrid('getRows');
		var disabledLineCnt = 0;
		var selectedLineCnt = $("#classifyquery_dg").datagrid('getSelections').length;
		for(var i = 0; i < rows.length; i++){
			if(rows[i].ischecked === 1){
				disabledLineCnt++;
			}
		}
		var headerChk = $('.datagrid-header-check input[type="checkbox"]')
		headerChk.prop('checked', selectedLineCnt + disabledLineCnt == rows.length);
	}
	
	/**
	 * 合并单元格；初始化工具栏按钮
	 */
	function mergeCells(data){
		var merges = [];
		var tmp = _.groupBy(data.rows, function(ele){
			return ele.query + "@@" + ele.applyname + "@@" + ele.channel + "@@" + ele.city;
		});
		for(var j in tmp){
			for(var i = 0; i < data.rows.length; i++){
				var rowCombineId = data.rows[i].query + "@@" + data.rows[i].applyname + "@@" + data.rows[i].channel + "@@" + data.rows[i].city;
				if(j === rowCombineId){
					merges.push({index:i, field: 'query', rowspan:tmp[j].length});
					merges.push({index:i, field: 'applyname', rowspan:tmp[j].length});
					merges.push({index:i, field: 'channel', rowspan:tmp[j].length});
					merges.push({index:i, field: 'city', rowspan:tmp[j].length});
					break;
				}
			}
		}
		//console.log('merges data:\n', JSON.stringify(merges));
		for(var i=0; i<merges.length; i++){
			$("#classifyquery_dg").datagrid('mergeCells',{
				index: merges[i].index,
				field: merges[i].field,
				rowspan: merges[i].rowspan
			});
		}
	}
	
	/**
	 * 初始化
	 * @return
	 */
	function initButtons(rows){
		var chkBtn = $('#check_btn'),
			delBtn = $('#delete_btn'),
			aucBtn = $('#autoclassify_btn'),
			macBtn = $('#manualclassify_btn');
		
		// 审核按钮、删除按钮、手动分配按钮、批量自动分配按钮：当前也有未审核的数据，使能按钮
		var rtn = _.find(rows, function(ele){
			return ele.ischecked === 0;
		});
		rtn ? chkBtn.linkbutton('enable') : chkBtn.linkbutton('disable');
		rtn ? delBtn.linkbutton('enable') : delBtn.linkbutton('disable');
		rtn ? aucBtn.linkbutton('enable') : aucBtn.linkbutton('disable');
		rtn ? macBtn.linkbutton('enable') : macBtn.linkbutton('disable');
	}

}

function checkClassifyquery(){
	var ids = [];
	var rows = $("#classifyquery_dg").datagrid("getSelections");
	if (rows.length > 0) {
		for ( var i = 0; i < rows.length; i++) {
			var id = rows[i].id;
			ids.push(id);
		}
	} else {
		$.messager.alert('提示', "请至少选择一行!", "warning");
		return;
	}
	
	if(ids.length>0){
		$.messager.confirm("操作提示", "您确定要审核吗？", function(data) {
			if (data) {
				$.ajax( {
					type : "post",
					async : false,
					url : "../querymanage.action",
					data : {
						type : "checkclassifyquery",
						ids : ids
					},
					dataType : "json",
					traditional: true,
					success : function(data, textStatus, jqXHR) {
						$.messager.alert('信息提示', data.msg, "info");
						if (data.success== true) {
							$("#classifyquery_dg").datagrid('reload');
						}
					},
					error : function(jqXHR, textStatus, errorThrown) {
						$.messager.alert('系统异常', "请求数据失败!", "error");
					}
				});
			}
		});
	}
}

function deleteClassfifyquery(){
	var ids = [];
	var rows = $("#classifyquery_dg").datagrid("getSelections");
	if (rows.length > 0) {
		for ( var i = 0; i < rows.length; i++) {
			var id = rows[i].id;
			ids.push(id);
		}
	} else {
		$.messager.alert('提示', "请至少选择一行!", "warning");
		return;
	}
	
	if(ids.length>0){
		$.messager.confirm("操作提示", "您确定要删除吗？", function(data) {
			if (data) {
				$.ajax( {
					type : "post",
					async : false,
					url : "../querymanage.action",
					data : {
						type : "deleteclassifyquery",
						ids : ids
					},
					dataType : "json",
					traditional: true,
					success : function(data, textStatus, jqXHR) {
						$.messager.alert('信息提示', data.msg, "info");
						if (data.success== true) {
							$("#classifyquery_dg").datagrid('reload');
						}
					},
					error : function(jqXHR, textStatus, errorThrown) {
						$.messager.alert('系统异常', "请求数据失败!", "error");
					}
				});
			}
		});
	}
}

function autoClassify(){
	var ids = [];
	var rows = $("#classifyquery_dg").datagrid("getSelections");
	if (rows.length > 0) {
		for ( var i = 0; i < rows.length; i++) {
			var id = rows[i].id;
			ids.push(id);
		}
	} else {
		$.messager.alert('提示', "请至少选择一行!", "warning");
		return;
	}
	
	if(ids.length>0){
		$.messager.confirm("操作提示", "您确定要自动分配吗？", function(data) {
			if (data) {
				$.ajax( {
					type : "post",
					async : false,
					url : "../querymanage.action",
					data : {
						type : "autoclassifybatch",
						ids : ids
					},
					dataType : "json",
					traditional: true,
					success : function(data, textStatus, jqXHR) {
						$.messager.alert('信息提示', data.msg, "info");
						if (data.success== true) {
							$("#classifyquery_dg").datagrid('reload');
						}
					},
					error : function(jqXHR, textStatus, errorThrown) {
						$.messager.alert('系统异常', "请求数据失败!", "error");
					}
				});
			}
		});
	}
}


function autoClassfiyAll(){
	$.messager.confirm("操作提示", "您确定要全量自动分配吗？", function(data) {
		if (data) {
			$.ajax( {
				type : "post",
				async : false,
				url : "../querymanage.action",
				data : {
					type : "autoclassifyall",
				},
				dataType : "json",
				success : function(data, textStatus, jqXHR) {
					$.messager.alert('信息提示', data.msg, "info");
					if (data.success== true) {
						$("#classifyquery_dg").datagrid('reload');
					}
				},
				error : function(jqXHR, textStatus, errorThrown) {
					$.messager.alert('系统异常', "请求数据失败!", "error");
				}
			});
		}
	});
	
}

// 对上传文件进行校验和上传
function upload() {
	// 得到上传文件的全路径
	var fileName = $('#classifyquery_file').filebox('getValue');
	// 进行基本校验
	if (fileName == "") {
		$.messager.alert('提示', '请选择上传文件!', 'warning');
	} else {
		// 对文件格式进行校验
		var filetype = /\.[^\.]+$/.exec(fileName.toLowerCase());
		if (filetype == ".xls" || filetype == ".xlsx") {
			loading();
			// 使表单成为ajax提交
			$("#ssformUpload").form(
					"submit",
					{
						url : "../file/upload?path=classifyquery",
						success : function(data) {
							var info = $.parseJSON(data);
							var state = info["state"];
							if (state == "success") {
								var name = info["names"][0];
								importXls(name);
							} else {
								loadEnd();
								$.messager.alert('提示', info.message
										+ " 请重新上传!", 'warning');
							}
							$('#classifyquery_file').filebox('setValue', '');
						}
					});
		} else {
			var alertinfo = "上传的文件格式不正确,请选择<br/>";
			alertinfo += "1997-2003Excel文件(*.xls)<br/>";
			alertinfo += "或者是2007Excel文件(*.xlsx)!";
			$.messager.alert('信息提示', alertinfo, 'warning');
			$('#classifyquery_file').filebox('setValue', '');
		}
	}
}

function importXls(name) {
	$.ajax( {
		type : "post",
		url : "../querymanage.action",
		data : {
			type : 'classifyqueryimport',
			filename : name
		},
		async : false,
		dataType : "json",
		timeout : 180000,
		success : function(data, textStatus, jqXHR) {
			loadEnd();
			if (data.success == true) {
				$.messager.alert('提示', data.msg);
				$("#classifyquery_dg").datagrid('reload');
			} else {
				var msg = '<div style="height:250px;overflow:auto;">' + data.msg.join("<br>") + '</div>'
				$.messager.alert('提示', msg);
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			loadEnd();
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

function exportExcel(checked){
	var param = {
			classifyquery : replaceSpace($('#classifyquery_query').combobox('getText')),
			city: $('#classifyquery_city').combotree('getValue'),
			serviceid : $('#classifyquery_service').combotree('getValue'),
			normalquery : replaceSpace($('#classifyquery_abstract').combobox('getText')),
			classified : $('#classifyquery_classified').combobox('getValue'),
			checked : checked ? "1" : "0",
			checktimeStart : $('#classifyquery_checktime_from').datetimebox('getValue'),
			checktimeEnd : $('#classifyquery_checktime_to').datetimebox('getValue'),
			inserttimeStart : $('#classifyquery_inserttime_from').datetimebox('getValue'),
			inserttimeEnd : $('#classifyquery_inserttime_to').datetimebox('getValue')
		};
		
		if(param.normalquery === '全部'){
			param.normalquery = '';
		}
		
		if(param.classified === '全部'){
			param.normalquery = '';
		}
		
		if(param.checked === '全部'){
			param.normalquery = '';
		}
		
		param.type = 'classifyqueryexport';
		$('#classify_form').form('submit', {
			url : '../classifyquery.action',
			//ajax:false,
			queryParams : param,
			success:function(data){
				var data = eval('(' + data + ')');
				//var data = JSON.parseJSON(data);
				if(!data.success){
					$.messager.alert('提示', data.msg, "warning");
				}
			},
			onLoadError:function(){
				$.messager.alert('提示', '系统内部错误', "warning");
			}
		});
}

function loading(){   
	$("<div class=\"datagrid-mask\"></div>").css({'z-index':9999,display:"block",width:"100%",height:$(window).height()}).appendTo("body");   
	$("<div class=\"datagrid-mask-msg\"></div>").html("正在导入数据...").appendTo("body").css({'z-index':9999,display:"block",left:($(document.body).outerWidth(true) - 190) / 2,top:($(window).height() - 45) / 2});   
 }   

function loadEnd(){   
	 $(".datagrid-mask").remove();   
	 $(".datagrid-mask-msg").remove();			   
}

// 创建模型级别下拉框
function createModelTree(cid, sid){
	$('#'+cid).combotree({
		url : '../querymanage.action?type=createservicetree&a=' + Math.random(),
		editable:false, 
		multiple:false,
		onClick: function(node){
			// 联动标准问下拉框
			createNormalqueryCombobox(node.id, sid);
		},
		onBeforeExpand:function(node){
			$('#'+cid).combotree('tree').tree("options").url = "../querymanage.action?type=createservicetree&serviceid="+node.id+'&a='+ Math.random(); // 展开时发送请求去加载节点
		},
	}); 
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

// 创建标准问下拉框
function createNormalqueryCombobox(serviceid, sid){
	var url = '../querymanage.action?type=createnormalquerycombobox&flag=select&serviceid='+serviceid+'&a='+ Math.random();   
	createCombobox(sid,url,false,true);	
	
}

// 创建Combobox
function createCombobox(id,url,multiple,editable) {
	$('#'+id).combobox({	
		url:url,
		valueField:'id',	
		textField:'text',
		panelHeight:'150px',
		multiple : multiple, // 支持多选
		separator : ',' ,// 多选的时候用“,”分隔
		editable:editable
	});
}

// 控件约束初始化
function initConstraints(){
	// 审核状态
	$('#classifyquery_checked').combobox({
		valueField: 'id',
		textField: 'text',
		data: [{
					text: '全部',
					id: '',
					selected:true
				},{
					text: '未审核',
					id: '0'
				},{
					text: '审核',
					id: '1'
				}],
		onChange:function(newVal, oldVal){
			if(newVal == '0'){
				$('#classifyquery_checktime_from').datetimebox('disable');
				$('#classifyquery_checktime_to').datetimebox('disable');
			}else{
				$('#classifyquery_checktime_from').datetimebox('enable');
				$('#classifyquery_checktime_to').datetimebox('enable');
			}
		}
	});
	// 分类状态
	$('#classifyquery_classified').combobox({
		valueField: 'id',
		textField: 'text',
		data: [{
					text: '全部',
					id: '',
					selected:true
				},{
					text: '未分配',
					id: '0'
				},{
					text: '分配',
					id: '1'
				}]
	});
}

function addClassifyquery(){
	var ids = [];
	var rows = $("#classifyquery_dg").datagrid("getSelections");
	if (rows.length > 0) {
		for ( var i = 0; i < rows.length; i++) {
			var id = rows[i].id;
			ids.push(id);
		}
	} else {
		$.messager.alert('提示', "请至少选择一行!", "warning");
		return;
	}
	
	var data = {
		 ids:ids,
		 serviceid : $('#service').combotree('getValue'),
		 service : $('#service').combotree('getText'),
		 kbdataid : $('#kbdata').combobox('getValue'),
		 abs : $('#kbdata').combobox('getText')
	};
	
	if(data.serviceid == ""|| data.serviceid == null){
		$.messager.alert('提示','请选业务模型!', "warning");
		return;
	}
	
	if(data.kbdataid == ""|| data.kbdataid == null){
		$.messager.alert('提示','请选择标准问题!', "warning");
		return;
	}
	$.ajax( {
		url : '../querymanage.action?type=insertclassifyquery',
		type : "post",
		data : data,
		async : false,
		dataType : "json",
		traditional: true,
		success : function(data, textStatus, jqXHR) {
			
			if (data.success == true) {
				$("#classifyquery_dg").datagrid("reload");
				$('#classifyquery_win').window('close');
				$.messager.alert('提示', data.msg, "info");
			}else{
				$.messager.alert('提示', data.msg, "warning");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	})
}
function openNewWin(){
	if($('#classifyquery_dg').datagrid('getSelections').length < 1){
		$.messager.alert('提示', "请至少选择一行!", "warning");
		return;
	}
	
	createModelTree('service', 'kbdata');
	$('#classifyquery_win').window({onClose:function(){
		$('#service').combotree('setValue', '');
		$('#kbdata').combobox('setValue', '');
	}}).window('open');
}
function closeWin(){
	$('#classifyquery_win').window('close');
}
