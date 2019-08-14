var serviceid;
var userid;
var ioa;
var cityid;
var kbdataid;
var brand;
var service;
var topic;
var _abstract;
var serviceinfoanswer = "";
var faqanswer = "";
var kbansvaliddateid;
var insertorupdate = 0;
var docname = "docName";
var dnonameDefaultText = "";
//该变量用于判断用户是进行新增还是修改操作，以决定是否进行模板内容select后对信息表模板的赋值操作
var flag = 0;
var customItem = {};//页面显示配置

$(function() {
	var urlparams = new UrlParams(); // 所有url参数
	var urlparams = new UrlParams(); // 所有url参数
	serviceid = decodeURI(urlparams.serviceids);
	userid = decodeURI(urlparams.userid);
	ioa = decodeURI(urlparams.ioa);
	cityid = decodeURI(urlparams.cityids);
	if (cityid == null || cityid == "" || cityid == "null" || cityid == undefined) {
		cityid = "全国";
	}
	kbdataid = decodeURI(urlparams.kbdataids);
	brand = decodeURIComponent(urlparams.brand);
	service = decodeURIComponent(urlparams.service);
	topic = decodeURIComponent(urlparams.topic);
	_abstract = decodeURIComponent(urlparams._abstract);
	initCustom();
	
	// 加载答案列表
	loadFaqList();
	
	//关闭编辑框
	closeEditWindow();
	//加载答案编辑框info
	createInfo();
	//口径文本选择事件
	caliberTextOnSelect();
	
	//初始化原子函数选择框
//	initPrimitiveFunctionSelect();
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

//打开原子函数 win
function openPrimitiveFunctionWin() {
	$('#primitive_function').window({}).window('open');
	$("#primitivefunction").combobox("clear");
	$(".primitive_function_form").css("display","none");
}

//初始化原子函数下拉列表
function initPrimitiveFunctionSelect() {		
	$('#primitivefunction').combobox({ 
		data:[{value:'hxszbj',text:'横向数值比较'},
		      {value:'zxszbj',text:'纵向数值比较'},
		      {value:'lbpx',text:'列表排序'},
		      {value:'szys',text:'数值运算'},
		      {value:'hz',text:'汇总'},
		      {value:'zhcx',text:'组合查询'},
		      {value:'tjl',text:'统计量'},],
		onSelect: function(rec){
			$(".primitive_function_form").css("display","none");
			if(rec) {
				var serviceinfo = $('#serviceinfotemplate').data('serviceinfo');
				var _serviceid = '';
				if (serviceinfo) {
					_serviceid = serviceinfo.serviceid;
				} else {
					_serviceid = $('#serviceinfo').combobox('getValue');
				}
				if (_serviceid == '') {
					$.messager.alert('系统提示', "请选信息表名称或模板内容!", "warning");
					return;
				}
				
				var serviceinfotemplate = $("#serviceinfotemplate").textbox('getValue');
				//信息表模板检查
				if (serviceinfotemplate == null || serviceinfotemplate == "") {
					$.messager.alert('提示', "信息表模板不能为空!", "warning");
					return;
				}
			
	        	var type = rec.value;
	        	switch(type) {
	        		//横向数值比较
	        		case 'hxszbj':
	        			var url = '../faq.action?type=createattrnamecombobox&serviceid=' + _serviceid + '&a=' + Math.random();
	        			createCombobox('hxszbj_alcs', url, false, true);
	        			$("#hxszbj_ysf").combobox({
	        				data:[{value:'>',text:'>'},
	        				      	{value:'=',text:'='},
	        						{value:'<',text:'<'},
	        						{value:'>=',text:'>='},
	        						{value:'<=',text:'<='}]
	        			});
	        			createCombobox('hxszbj_blcs', url, false, true);
	        			$("#hxszbj_mztjsc").textbox('clear');
	        			$("#hxszbj_bmztjsc").textbox('clear');
	        			break;
	        		//纵向数值比较
	        		case 'zxszbj':
	        			var url = '../faq.action?type=createattrnamecombobox&serviceid=' + _serviceid + '&a=' + Math.random();
	        			$("#zxszbj_ysf").combobox({
	        				data:[{value:'>',text:'>'},
	        				      	{value:'=',text:'='},
	        						{value:'<',text:'<'},
	        						{value:'>=',text:'>='},
	        						{value:'<=',text:'<='}]
	        			});
	        			createCombobox('zxszbj_bjcs', url, false, true);
	        			createCombobox('zxszbj_mztjsc', url, true, true);	        			
	        			
	        			$("#zxszbj_bmztjsc").textbox('clear');
	        			$("#zxszbj_bg").attr("checked", false);
	        			break;
	        		//列表排序
	        		case 'lbpx':
	        			var url = '../faq.action?type=createattrnamecombobox&serviceid=' + _serviceid + '&a=' + Math.random();
	        			$("#lbpx_pxfs").combobox({
	        				data:[{value:'正序',text:'正序'},
	        				      	{value:'倒序',text:'倒序'}]
	        			});
	        			createCombobox('lbpx_bjcs', url, false, true);
	        			$("#lbpx_scgs").combobox({
	        				data:[{value:'排序后获取个数',text:'排序后获取个数'}]
	        			});
	        			createCombobox('lbpx_scmb', url, true, true);	        			

	        			$("#lbpx_bg").attr("checked", false);
	        			break;
	        		//数值运算
	        		case 'szys':
	        			var url = '../faq.action?type=createattrnamecombobox&serviceid=' + _serviceid + '&a=' + Math.random();
	        			$("#szys_ysf").combobox({
	        				data:[{value:'+',text:'+'},
	        				      	{value:'-',text:'-'},
	        						{value:'*',text:'*'},
	        						{value:'/',text:'/'}]
	        			});
	        			createCombobox('szys_csa', url, false, true);
	        			$("#szys_csa").combobox({
	        				onSelect: function(rec) {
	        					$("#szys_csb").textbox("setValue",rec.text);
	        				}
	        			});
	        			$("input",$("#szys_csa").next("span")).blur(function(){
	        				var t = $("#szys_csa").combobox("getText");
	        				$("#szys_csb").textbox("setText",t);
	        			})
	        			
	        			createCombobox('szys_bjcs', url, false, true);	        			
	        			$("#szys_scmb").combobox({
	        				data:[{value:'运算结果',text:'运算结果'}]
	        			});
	        			$("#szys_csb").textbox('clear');
	        			break;
	        		//汇总
	        		case 'hz':
	        			var url = '../faq.action?type=createattrnamecombobox&serviceid=' + _serviceid + '&a=' + Math.random();
	        			createCombobox('hz_hzcs', url, false, true);	        			
	        			break;
	        		//组合查询
	        		case 'zhcx':
	        			$("#zhcx_cxgz").textbox('clear');
	        			break;
		        	//统计量
	        		case 'tjl':
	        			var url = '../faq.action?type=createattrnamecombobox&serviceid=' + _serviceid + '&a=' + Math.random();
	        			createCombobox('tjl_tjlcs', url, false, true);	        			
	        			break;
	        	}
	        	
	        	$("#"+type).css("display","");
			}
		}
	});
	
	$("#primitivefunction").combobox("clear");
}

//原子函数生成模板
function build() {
	var type = $("#primitivefunction").combobox('getValue');
	var isOk = $("#" + type).form('validate');
	if(isOk) {
		switch(type) {
			//横向数值比较
			case 'hxszbj':
				var content = '横向数值比较("'+ $("#hxszbj_alcs").combobox('getText') + $("#hxszbj_ysf").combobox('getValue')
								+ $("#hxszbj_blcs").combobox('getText')
								+ '","' + $("#hxszbj_mztjsc").textbox('getValue') + '","'
								+ $("#hxszbj_bmztjsc").textbox('getValue') + '")';
				
				//将函数添加到信息表模板
				var serviceinfotemplate = $("#serviceinfotemplate").textbox('getValue');
				if(serviceinfotemplate.indexOf(";")!=-1) {
					var a = serviceinfotemplate.split(";");
					content = a[0] + ";" + content + ";";
				}
				$("#serviceinfotemplate").textbox('setValue', content);	
				break;
			//纵向数值比较
			case 'zxszbj':
				var zxszbj_mztjsc = $("#zxszbj_mztjsc").combobox('getText');
				var a = zxszbj_mztjsc.split(",");	
				var b = [];
				$.each(a,function(i,n) {
					b.push("<@" + n + ">");
				});
				var bg = $("#zxszbj_bg").is(':checked');
				
				zxszbj_mztjsc = b + "";
				if(bg)
					zxszbj_mztjsc = "TABLE(" + zxszbj_mztjsc + ")";
				
				var content = '纵向数值比较("'+ $("#zxszbj_ysf").combobox('getValue')
								+ $("#zxszbj_bjcs").combobox('getText')
								+ '","' + zxszbj_mztjsc + '","' + $("#zxszbj_bmztjsc").textbox('getValue') + '")';
				
				//将函数添加到信息表模板
				var serviceinfotemplate = $("#serviceinfotemplate").textbox('getValue');
				if(serviceinfotemplate.indexOf(";")!=-1) {
					var a = serviceinfotemplate.split(";");
					content = a[0] + ";" + content + ";";
				}
				$("#serviceinfotemplate").textbox('setValue', content);
				break;
			//列表排序
			case 'lbpx':
				var lbpx_scmb = $("#lbpx_scmb").combobox('getText');
				var a = lbpx_scmb.split(",");	
				var b = [];
				$.each(a,function(i,n) {
					b.push("<@" + n + ">");
				});
				var bg = $("#lbpx_bg").is(':checked');
				
				lbpx_scmb = b + "";
				if(bg)
					lbpx_scmb = "TABLE(" + lbpx_scmb + ")";
				
				var scgs = $("#lbpx_scgs").textbox('getText');
				if(scgs=="排序后获取个数") {
					scgs = "<@" + scgs + ">";
				}			
				
				var content = '列表排序("'+ $("#lbpx_pxfs").combobox('getValue')
								+ '","' + $("#lbpx_bjcs").combobox('getText')
								+ '","' + scgs + '","' + lbpx_scmb + '")';
				
				//将函数添加到信息表模板
				var serviceinfotemplate = $("#serviceinfotemplate").textbox('getValue');
				if(serviceinfotemplate.indexOf(";")!=-1) {
					var a = serviceinfotemplate.split(";");
					content = a[0] + ";" + content + ";";
				}
				$("#serviceinfotemplate").textbox('setValue', content);
				break;
			//数值运算
			case 'szys':
				var content = '数值运算("'+ $("#szys_ysf").combobox('getValue')
								+ '","' + $("#szys_bjcs").combobox('getText')
								+ '","<@' + $("#szys_scmb").textbox('getValue')
								+ '>","<@'+$("#szys_csa").combobox('getText')+'>")';
				
				//将函数添加到信息表模板
				var serviceinfotemplate = $("#serviceinfotemplate").textbox('getValue');
				if(serviceinfotemplate.indexOf(";")!=-1) {
					var a = serviceinfotemplate.split(";");
					content = a[0] + ";" + content + ";";
				}
				$("#serviceinfotemplate").textbox('setValue', content);			
				break;
			//汇总
			case 'hz':
				var content = '汇总("' + $("#hz_hzcs").combobox('getText') +'")';
				
				//将函数添加到信息表模板
				var serviceinfotemplate = $("#serviceinfotemplate").textbox('getValue');
				if(serviceinfotemplate.indexOf(";")!=-1) {
					var a = serviceinfotemplate.split(";");
					content = a[0] + ";" + content + ";";
				}
				$("#serviceinfotemplate").textbox('setValue', content);				
				break;
			//组合查询
			case 'zhcx':
				var content = $("#zhcx_cxgz").textbox('getValue');
				
				var serviceinfo = $("#serviceinfo").combobox('getText');
				var attrname = $("#attrname").combobox('getText');
				//查询("信息表名称","","属性名称|属性值");提示用户("答案文本")
				content = '查询("' + serviceinfo + '","","' + $.trim(content) + '");';
				
				$("#attrvalue").textbox('clear');
				$("#serviceinfotemplate").textbox('setValue', content);
				break;	
			//统计量
			case 'tjl':
				var content = '统计量("' + $("#tjl_tjlcs").combobox('getText') +'")';
				
				//将函数添加到信息表模板
				var serviceinfotemplate = $("#serviceinfotemplate").textbox('getValue');
				if(serviceinfotemplate.indexOf(";")!=-1) {
					var a = serviceinfotemplate.split(";");
					content = a[0] + ";" + content + ";";
				}
				$("#serviceinfotemplate").textbox('setValue', content);				
				break;
		}
		
		btnCancel('primitive_function');
	}
}

//关闭规则编辑框
function closeEditWindow() {
	$('#faqsddwin').panel('close');

}

// 加载答案列表
function loadFaqList() {
	$("#faqdatagrid").datagrid({
		height: 230,
		//width:1020,
		url: "../faq.action",
		queryParams: {
			type: "select",
			kbdataid: kbdataid
		},
		pageSize: 10,
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
					field: 'answer',
					title: '答案',
					width: 500
				}, {
					field: 'channel',
					title: '渠道',
					width: 180

				}, {
					field: 'answertype',
					title: '答案类型',
					width: 180,
					formatter: function(value, row, index) {
						if (value == "9") {
							return "结构化知识";
						} else if (value == "8") {
							return "FAQ";
						} else {
							return "普通文字";
						}
					}

				}, {
					field: 'customertype',
					title: '客户类型',
					width: 180

				}, {
					field: 'userid',
					title: 'userid',
					hidden: true
				}, {
					field: 'robotname',
					title: '实体机器人ID'
				}, {
					field: 'servicetype',
					title: '行业组织应用',
					width: 400
				}, {
					field: 'city',
					title: '地市',
					width: 300,
					hidden: true
				}, {
					field: 'starttime',
					title: '起始时间',
					width: 200
				}, {
					field: 'endtime',
					title: '截止时间',
					width: 200
				}, {
					field: 'kbansvaliddateid',
					title: '答案ID',
					width: 150,
					hidden: true
				}
			]
		]
	});

	if (ioa=='物联网行业->中国电信物联网->多渠道应用'){
		$("#faqdatagrid").datagrid({columns: [
		                          			[{
		                    					field: 'ck',
		                    					checkbox: true
		                    				}, {
		                    					field: 'answer',
		                    					title: '答案',
		                    					width: 500
		                    				}, {
		                    					field: 'channel',
		                    					title: '渠道',
		                    					width: 180

		                    				}, {
		                    					field: 'answertype',
		                    					title: '答案类型',
		                    					width: 180,
		                    					formatter: function(value, row, index) {
		                    						if (value == "9") {
		                    							return "结构化知识";
		                    						} else if (value == "8") {
		                    							return "FAQ";
		                    						} else {
		                    							return "普通文字";
		                    						}
		                    					}

		                    				}, {
		                    					field: 'customertype',
		                    					title: '客户类型',
		                    					width: 180

		                    				}, {
		                    					field: 'userid',
		                    					title: 'userid',
		                    					hidden: true
		                    				}, {
		                    					field: 'robotname',
		                    					title: '实体机器人ID',
		                    					hidden:true
		                    				}, {
		                    					field: 'servicetype',
		                    					title: '行业组织应用',
		                    					width: 400
		                    				}, {
		                    					field: 'city',
		                    					title: '地市',
		                    					width: 300,
		                    					hidden: true
		                    				}, {
		                    					field: 'starttime',
		                    					title: '起始时间',
		                    					width: 200
		                    				}, {
		                    					field: 'endtime',
		                    					title: '截止时间',
		                    					width: 200
		                    				}, {
		                    					field: 'kbansvaliddateid',
		                    					title: '答案ID',
		                    					width: 150,
		                    					hidden: true
		                    				}
		                    			]
		                    		]}); //通过列名获得此列
        $('#robotIDText').hide();
        $('#robotIDBox').hide();
	}
	
	$("#faqdatagrid").datagrid('getPager').pagination({
		showPageList: false,
		buttons: [{
				//			text : "新增",
				iconCls: "icon-add",
				handler: function() {
					flag = 1;
					$('#faqsddwin').panel({
						title: "新增",
						iconCls: 'icon-add'
					});
					$('#faqsddwin').panel('open');

					$('#faqsddwin').panel('expand', true);

					//隐藏除普通文字其他类型选框
					$(".serviceinfo_tr").css("display", "none");
					$("#hitquestion_span").css("display", "none");
					$(".service_tr").css("display", "none");

					//赋值
					$('#customertype').combobox('setValue', "普通客户");
					$('#answertype').combobox('setValue', "0");
					insertorupdate = 0;
					clearForm();
					//        	$('#faqsddwin').panel('onOpen',beforeopenfaqsddwin());
					$('#faqsddwin').panel('onOpen', setCityTreeDefaultValue());


				}
			}, "-", {
				//			text : "删除(批量)",
				iconCls: "icon-delete",
				handler: function() {
					deleteAnswer();
				}
			}, "-", {
				//			text : "修改",
				iconCls: "icon-edit",
				handler: function() {
					flag = 0;
					clearForm();
					fillForm();
				}
			}

		]
	});

}

function beforeopenfaqsddwin() {
	var $channels = $("#channel").combobox("getData");
	var channels = new Array();
	for (var i = 0; i < $channels.length; i++) {
		channels[i] = $channels[i].id;
	}
	$("#channel").combobox('setValues', channels);
	return true;
}
//填充编辑框数据
function fillForm() {

	insertorupdate = 1;
	serviceinfoanswer = "";
	faqanswer = "";
	var rows = $("#faqdatagrid").datagrid("getSelections");
	if (rows) {
		if (rows.length == 1) {
			var row = $('#faqdatagrid').datagrid('getSelected');
			kbansvaliddateid = row.kbansvaliddateid;
			var customertype = row.customertype;
			var channel = row.channel;
			var starttime = row.starttime;
			var endtime = row.endtime;
			var answertype = row.answertype;
			var answer = row.answer;
			var city = row.citycode;
			var robotid = row.userid;
			
			$("#customertype").combobox("setValue", customertype);
			$("#channel").combobox("setValues", channel.split('\,'));
			$("#starttime").datebox("setValue", starttime);
			$("#endtime").datebox("setValue", endtime);
			$("#answertype").combobox("setValue", answertype);
			if(city!=null){
				$("#city").combotree("setValues", city.split(','));
			}
			
			$("#robotID").combobox("setValue", robotid);

			if (answertype == "8") {
				$(".serviceinfo_tr").css("display", "none");
				$("#hitquestion_span").css("display", "none");
				$(".service_tr").css("display", "");
				var faqtemplate = "";
				var content = "";
				//					var service = "";
				//					var faq = "";
				if (answer.indexOf(";") != -1) {
					var arry = answer.split(';');
					if (arry.length > 1) {
						faqtemplate = arry[0];
						content = arry[1];
					}
				}

				$('#faqtemplate').textbox('setValue', faqtemplate);
				UM.getEditor('myEditor').setContent(content, false);
			} else if (answertype == "9") {
				$("#hitquestion_span").css("display", "");
				$(".serviceinfo_tr").css("display", "");
				$("#hitquestion_span").css("display", "");
				$(".service_tr").css("display", "none");
				var content = "";
				var serviceinfo = "";
				var attrname = "";
				var attrvalue = "";
				var serviceinfotemplate = "";
				if (answer.indexOf(";") != -1) {
					var arry = answer.split(";");
					if (arry.length > 1) {
						for(var i=0;i<arry.length-1;i++) {
							content += arry[i] + ";";
						}					
						answer = arry[arry.length-1];
						serviceinfotemplate = content;
						var tmp = arry[0].substring(arry[0].indexOf("(") + 1, arry[0].lastIndexOf(")"));
						serviceinfo = tmp.split(",")[0];
						attrname = tmp.split(",")[2];
						serviceinfo = serviceinfo.substring(1, serviceinfo.length - 1);
						attrname = attrname.substring(1, attrname.length - 1);
						if(attrname!="") {
							attrvalue = attrname.split("|")[1];
							attrname = attrname.split("|")[0];
						}
					}
				}

				var serviceinfoData = $("#serviceinfo").combobox("getData");
				var serviceid = "";
				for (var i = 0; i < serviceinfoData.length; i++) {
					if (serviceinfoData[i].text === serviceinfo) {
						serviceid = serviceinfoData[i].id;
					}
				}
				$("#serviceinfo").combobox("select", serviceid);
				if(attrname!="") {
					$('#attrname').combobox('select', attrname);
					//判断是否使用了使用<@****>
					if(!((attrvalue.indexOf('<@')==0)&&(attrvalue.indexOf('>')==attrvalue.length-1))) {
						$('#attrvalue').combobox('select', attrvalue);
					}
				}
				//	 		    setOnComboboxReady('attrvalue', attrvalue);


				$("#serviceinfotemplate").textbox('setValue', serviceinfotemplate);
				$("#serviceinfotemplate").data('serviceinfo', {
					'serviceid': serviceid,
					'service': serviceinfo
				});

				UM.getEditor('myEditor').setContent(str2html(answer), false);
			} else {
				$(".serviceinfo_tr").css("display", "none");
				$("#hitquestion_span").css("display", "none");
				$(".service_tr").css("display", "none");
				UM.getEditor('myEditor').setContent(answer, false);
			}
			
			$('#faqsddwin').panel({
				title: "修改",
				iconCls: 'icon-edit'
			});
			$('#faqsddwin').panel('open');
			$('#faqsddwin').panel('expand', true);

		} else {
			$('#faqsddwin').panel('close');
			$.messager.alert('提示', "请选择一行记录修改!", "warning");
			return;
		}
	} else {
		$('#faqsddwin').panel('close');
		$.messager.alert('提示', "请选择一行记录修改!", "warning");
		return;
	}
}

function setOnComboboxReady(id, value) {
	$('#' + id).combobox({
		onLoadSuccess: function() {
			$(this).combobox('setValue', value);
		}
	})
}

//清空编辑框内容
function clearForm() {
	$("#channel").combobox("clear");
	//	beforeopenfaqsddwin();
	$("#starttime").datebox("clear");
	$("#endtime").datebox("clear");
	$("#serviceinfo").combobox("clear");
	$("#faq").combobox("clear");
	$("#knoname").combobox("clear");
	$("#attrname").combobox("clear");
	$("#attrvalue").combobox("clear");
	$("#service").combotree("clear");
	$('#faqtemplate').textbox('clear');
	clearServiceinfotemplate();
	$("#city").combotree("clear");
	$("#robotID").combobox("clear");
	//    $('#customertype').combobox('setValue',"普通客户"); 
	//    $('#answertype').combobox('setValue',"0"); 
	serviceinfoanswer = "";
	faqanswer = "";


	UM.getEditor('myEditor').setContent("", false);

}

//批量删除答案
function deleteAnswer() {

	var combition = [];
	var rows = $("#faqdatagrid").datagrid("getSelections");
	if (rows.length > 0) {
		for (var i = 0; i < rows.length; i++) {
			kbansvaliddateid = rows[i].kbansvaliddateid;
			combition.push(kbansvaliddateid);
		}
	} else {
		$.messager.alert('提示', "请至少选择一行!", "warning");
		return;
	}

	if (combition.length > 0) {
		$.messager.confirm("操作提示", "您确定要删除吗？", function(data) {
			if (data) {
				$.ajax({
					type: "post",
					async: false,
					url: "../faq.action",
					data: {
						type: "delete",
						resourcetype: 'querymanage',
						operationtype: 'D',
						resourceid: serviceid,
						combition: combition.join("@@")

					},
					dataType: "json",
					success: function(data, textStatus, jqXHR) {
						$.messager.alert('信息提示', data["msg"], "info");
						if (data["success"] == true) {
							$("#faqdatagrid").datagrid('reload');
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


//加载答案编辑框info
function createInfo() {
	//创建客户类型下拉框
	var url = '../faq.action?type=createcustomertypecombobox&a=' + Math.random();
	createCombobox('customertype', url, false, false);

	//创建渠道下拉框
	var url = '../faq.action?type=createchannelcombobox&a=' + Math.random();
	createCombobox('channel', url, true, false, channelOnLoadSuccess);

	//创建答案类型下拉框
	var url = '../faq.action?type=createanswertypecombobox&a=' + Math.random();
	createCombobox('answertype', url, false, false);
	answertypeOnSelect();

	//创建地市下拉框
	createCityTree('city', 'edit', true);

	//创建回复口径类型下拉框
	var url = '../faq.action?type=createReplyCaliberTypeCombobox&a=' + Math.random();
	createCombobox('replyCaliberType', url, false, false);
	replyCaliberTypeOnSelect();

	//构造树形图
	createServiceTree("service", "faq");
	faqOnSelect();

	//加载信息表名称
	var url = '../faq.action?type=createserviceinfocombobox&a=' + Math.random();
	createServiecInfoCombobox('serviceinfo', url, false, true);
	//knoNameOnSelect();

	//加载所有模板属性为docName的模板内容
	createAttrLabelCombobox('', '', '');
	
	//加载robot ID 
	var url = '../faq.action?type=createrobotidcombobox&city=' + encodeURI(cityid) +'&a=' + Math.random();
	createCombobox('robotID', url, false, false)
	
	//是否显示插入其他形式答案
	if(customItem["插入其他形式答案"] != null && customItem["插入其他形式答案"] == "是"){
		$("#addOtherResponse_btn").show();
	}else{
		$("#addOtherResponse_btn").hide();
	}
}

//创建Combobox
function createCombobox(id, url, multiple, editable, defaultText) {
	$('#' + id).combobox({
		url: url,
		valueField: 'id',
		textField: 'text',
		panelHeight: '150px',
		multiple: multiple, // 支持多选
		separator: ',', // 多选的时候用“,”分隔
		editable: editable,
		onLoadSuccess: function() {
			// 传入的第五参数是函数代表自定义函数
			if (typeof defaultText === 'function') {
				defaultText();
				return;
			}

			if (defaultText) {
				var defaultValue = "";
				var data = $(this).combobox('getData');
				for (var i = 0; i < data.length; i++) {
					if (defaultText === data[i].text) {
						defaultValue = data[i].id;
					}
				}
				$(this).combobox('select', defaultValue);
			}
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
		},
		onLoadSuccess: function(node, data) {
			setCityTreeDefaultValue();
		}
	});
}

//构造信息表名称
function createServiecInfoCombobox(id, url, multiple, editable) {
	$('#' + id).combobox({
		url: url,
		valueField: 'id',
		textField: 'text',
		panelHeight: '150px',
		multiple: multiple, // 支持多选
		separator: ',', // 多选的时候用“,”分隔
		editable: editable,
		onSelect: function(rec) {
			var _serviceid = rec.id;
			if (_serviceid == "" || _serviceid == null) {
				$.messager.alert('提示', "请选择信息表名称!", "warning");
				return;
			}
			//selectSemanticsKeyword(_serviceid);

			//加载属性名称下拉数据
			createAttrCombobox(_serviceid);

			// 选择时，重置信息表模板
			var serviceinfo = rec.text;
			var content = '查询("' + serviceinfo + '","","");';
			$("#serviceinfotemplate").textbox('setValue', content);
			$("#serviceinfotemplate").data('serviceinfo', {
				'serviceid': _serviceid,
				'service': serviceinfo
			}); // 绑定当前信息表到信息表模板对象上

			$('#attrvalue').combobox('setValue', '');
		},
		onChange: function(newVal, oldVal) {
			if (!newVal || newVal.trim() === '') {
				// 选择时，清空信息表模板
				clearServiceinfotemplate();
				$('#attrname').combobox('loadData', []).combobox('setValue', '');
				$('#attrvalue').combobox('setValue', '');
				createAttrLabelCombobox('', '', '');
			}
		}
	});
}

function clearServiceinfotemplate() {
	$("#serviceinfotemplate").textbox('setValue', '').data('serviceinfo', null);
}

//加载模板路径下拉数据
function createAttrCombobox(serviceid) {
	var _url = '../faq.action?type=createattrnamecombobox&serviceid=' + serviceid + '&a=' + Math.random();
	$('#attrname').combobox({
		url: _url,
		valueField: 'id',
		textField: 'text',
		onSelect: function(rec) {
			if (rec.text != '') {
				// 选择时，清空信息表模板
				clearServiceinfotemplate();
				$('#attrvalue').textbox('setValue', '');
				createAttrLabelCombobox(serviceid, rec.text, rec.id);
				
				serviceinfo = $("#serviceinfo").combobox('getText');
				//查询("信息表名称","","属性名称|属性值");提示用户("答案文本")
				var content = '查询("' + serviceinfo + '","","' + rec.text + '|' + '<@' + rec.text + '>' + '");';
				$("#serviceinfotemplate").textbox('setValue', content);
				$("#serviceinfotemplate").data('serviceinfo', {
					'serviceid': serviceid,
					'service': serviceinfo
				}); // 绑定当前信息表到信息表模板对象上

				var answer = UM.getEditor('myEditor').getContentTxt();
				serviceinfoanswer = content + answer;
			}

		}
	})
}

//加载模板内容数据
function createAttrLabelCombobox(serviceid, attrname, colnum) {
	var city = "all";
	if (cityid.indexOf("全国") == -1) {
		city = cityid;
	}
	//加载属性标签内容下拉数据
	var _url = '../faq.action?type=createattrvaluescombobox&serviceid=' + serviceid + '&colnum=' + colnum + '&city=' + city + '&a=' + Math.random();
	$('#attrvalue').combobox({
		url: _url,
		valueField: 'id',
		textField: 'text',
		groupField: 'service',
		//	    onLoadSuccess:function(){
		//			$(this).combobox('setValue', value);
		//		},
		onSelect: function(rec) {
			var serviceinfo, serviceid;
			if (rec.attrname) { // 判断属性内容是选择得到，还是全部加载得到
				serviceinfo = rec.service;
				attrname = rec.attrname;
				serviceid = rec.serviceid;
			} else {
				serviceinfo = $("#serviceinfo").combobox('getText');
				serviceid = $('#serviceinfo').combobox('getValue');
			}
			var attrvalue = rec.text;
			//查询("信息表名称","","属性名称|属性值");提示用户("答案文本")
			var content = '查询("' + serviceinfo + '","","' + attrname + '|' + attrvalue + '");';
			if(flag!=0) {
				$("#serviceinfotemplate").textbox('setValue', content);
			} else {
				if($("#attrvalue").combobox('getValue')=="") {
					$("#serviceinfotemplate").textbox('setValue', content);
				}
				flag=1;
			}
			$("#serviceinfotemplate").data('serviceinfo', {
				'serviceid': serviceid,
				'service': serviceinfo
			}); // 绑定当前信息表到信息表模板对象上

			var answer = UM.getEditor('myEditor').getContentTxt();
			serviceinfoanswer = content + answer;
		}
	})
}



////查询信息表下列对应语义关键字
//function selectSemanticsKeyword(_serviceid){
//	$.ajax( {
//		url : '../faq.action',
//		type : "post",
//		data : {
//			type : "selectsemanticskeyword",
//			serviceid:_serviceid
//		},
//		async : false,
//		dataType : "json",
//		success : function(data, textStatus, jqXHR) {
//			if (data.success == true) {
//				var keyword =data.name;
//				if(keyword!=null&&keyword!=""){
//					docname = keyword;
//				}else{
//					docname = "docname";
//				}
//				
//				//加载知识名称下拉数据
//				var _url = '../faq.action?type=createknonamecombobox&serviceid='+_serviceid+'&a='+ Math.random(); 
//				createCombobox("knoname",_url,false, true, dnonameDefaultText);
//				dnonameDefaultText = "";
//				//加载属性名称下拉数据
//				var _url2 = '../faq.action?type=createattrnamecombobox&serviceid='+_serviceid+'&a='+Math.random();
//				createCombobox('attrname',_url2,false,true);
//			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
//		}
//	});
//}

function channelOnLoadSuccess() {
	// 显示复选框
	$('#selectAllChannel').show().click(function() {
		var checkON = $(this).is(":checked");
		if (checkON) {
			var data = $('#channel').combobox('getData');
			var values = [];
			data.forEach(function(v) {
				values.push(v.id)
			});
			$('#channel').combobox('setValues', values);
		} else {
			$('#channel').combobox('setValues', []);
		}
	})
}

//对应FAQ onselcet事件
function faqOnSelect() {
	$("#faq").combobox({
		onSelect: function(res) {
			var _service = $("#service").combotree("getText");
			var _id = res.id;
			var _text = res.text
			var _flag = "";
			if (_service.indexOf("对内") != -1) {
				_flag = "对内";
			} else if (_service.indexOf("对外") != -1) {
				_flag = "对外";
			}
			//FAQ("FAQ内容","对内、对外标记")
			var content = 'FAQ("' + _text + '","' + _flag + '","' + _id + '")';
			UM.getEditor('myEditor').setContent('<@FAQ内容>', false);
			$('#faqtemplate').textbox('setValue', content);
			faqanswer = content;

		}
	});
}


//知识名称 onselcet事件
function knoNameOnSelect() {
	$("#knoname").combobox({
		onSelect: function(res) {
			var serviceinfo = $("#serviceinfo").combobox('getText');
			var kname = res.text;
			//查询("信息表名称","","标准名称|docname");提示用户("答案文本")
			var content = '查询("' + serviceinfo + '","","' + docname + '|' + kname + '");';
			$("#serviceinfotemplate").textbox('setValue', content);
			var answer = UM.getEditor('myEditor').getContentTxt();
			serviceinfoanswer = content + answer;
			//docname = "docName";
		}
	});
}

//答案类型onselcet事件
function answertypeOnSelect() {
	$("#answertype").combobox({
		onSelect: function(rec) {
			UM.getEditor('myEditor').setEnabled();
			var newPtion = rec.id;
			if (newPtion == "8") {
				$(".serviceinfo_tr").css("display", "none");
				$("#hitquestion_span").css("display", "none");
				$(".reply_caliber").css("display", "none");
				$(".service_tr").css("display", "");
				UM.getEditor('myEditor').setContent(faqanswer, false);
			} else if (newPtion == "9") {
				$(".serviceinfo_tr").css("display", "");
				$("#hitquestion_span").css("display", "");
				$(".service_tr").css("display", "none");
				$(".reply_caliber").css("display", "none");
				UM.getEditor('myEditor').setContent(serviceinfoanswer, false);
			} else {
				$("#replyCaliberType").combobox("setValue", "");
				$("#caliberText").combobox("setValue", "");
				$(".serviceinfo_tr").css("display", "none");
				$("#hitquestion_span").css("display", "none");
				$(".service_tr").css("display", "none");
				$(".reply_caliber").css("display", "");
			}
		}
	});


}

//回复口径类型onselcet事件
function replyCaliberTypeOnSelect() {
	$("#replyCaliberType").combobox({
		onSelect: function(rec) {
			UM.getEditor('myEditor').setContent("", false);
			var caliberTypeId = rec.id;
			//编码防止中文乱码
			caliberTypeId = encodeURIComponent(caliberTypeId);
			var caliberTypeName = rec.text;
			//转人工不可编辑
			if (caliberTypeName.indexOf("转人工") >= 0) {
				UM.getEditor('myEditor').setDisabled();
			} else {
				UM.getEditor('myEditor').setEnabled();
			}
			//创建口径文本下拉框
			var url = '../faq.action?type=createCaliberTextCombobox&caliberTypeId=' + caliberTypeId + '&a=' + Math.random();
			createCombobox('caliberText', url, false, false);
			//caliberTextOnSelect();
		}
	});
}

//回复口径文本onselcet事件
function caliberTextOnSelect() {
	$("#caliberText").combobox({
		onSelect: function(rec) {
			var caliberText = rec.text;
			var caliberTypeName = $("#replyCaliberType").combobox("getText");
			//转人工不可编辑
			if (caliberTypeName.indexOf("转人工") >= 0) {
				UM.getEditor('myEditor').setContent(caliberText, false);
			} else { //非转人工光标处插入文本值
				UM.getEditor('myEditor').focus();
				UM.getEditor('myEditor').execCommand('inserthtml', caliberText);
			}
		}
	});
}

//地市下拉框初始值
function setCityTreeDefaultValue() {
	var url = '../faq.action?type=createcitycombobox&kbdataid=' + kbdataid + '&a=' + Math.random();
	$.ajax({
		url: url,
		type: 'get',
		dataType: 'json',
		success: function(data, status, xhr) {
			if (data.length > 0) {
				//				var t = $('#city').combotree('tree');
				//				for(var i=0; i<data.length; i++){
				//					var node = t.tree('find', data[i].id);
				//					t.tree('check', node.target);
				//				}
				var values = [];
				data.forEach(function(value) {
					values.push(value.id);
				})
				$('#city').combotree('setValues', values);
			}
		},
		error: function(xhr, status, errorThrown) {

		}
	});
}

/*
//构造树形图
function createServiceTree(fid,id) {
	var fdocid = "#"+fid;
	$(fdocid).combotree({
		url:'../querymanage.action?type=createtree&a='+ Math.random(),
		onBeforeExpand:function(node,param){ 
			$(fdocid).combotree('tree').tree("options").url = "../querymanage.action?type=createtree&serviceid="+node.id +'&a='+ Math.random();
		}, 
		onClick:function(rec){
			createAbsCombobox(fid,id);
			
		}
	});
}
*/

//根据业务构造摘要下拉框
function createAbsCombobox(fid, id) {
	var fdocid = "#" + fid;
	var docid = "#" + id
		// 获取树形结构选中的业务
	var _serviceid = $(fdocid).combotree("getValue");
	$(docid).combobox({
		url: '../querymanage.action?type=createabscombobox&serviceid=' + _serviceid,
		valueField: 'id',
		textField: 'text',
		panelHeight: '150px'

	});
}

//打开模板列 win
function openTemplateColumnWin() {
	var serviceinfo = $('#serviceinfotemplate').data('serviceinfo');
	var _serviceid = '';
	if (serviceinfo) {
		_serviceid = serviceinfo.serviceid;
	} else {
		_serviceid = $('#serviceinfo').combobox('getValue');
	}
	if (_serviceid == '') {
		$.messager.alert('系统提示', "请选信息表名称或模板内容!", "warning");
		return;
	}
//	var url = '../faq.action?type=createtemplatecolumncombobox&serviceid=' + _serviceid + '&a=' + Math.random();
	var url = '../faq.action?type=createattrnamecombobox&serviceid=' + _serviceid + '&a=' + Math.random();
	createCombobox('templatecolumn', url, false, true);
	$('#add_templatecolumn').window({}).window('open');
	$("#templatecolumn").combobox("clear");
}


//插入模板列
function addTemplateColumn() {
	var templatecolumn = $("#templatecolumn").combobox('getText');
	if (templatecolumn == "" || templatecolumn == null) {
		return;
	}
	var content = UM.getEditor('myEditor').getContentTxt();
	var response = $.trim(content);
	response = response + "<@" + templatecolumn + ">";
	response = $('<div>').text(response).html();
	UM.getEditor('myEditor').setContent(response, false);
	$('#add_templatecolumn').window('close');
	serviceinfoanswer = response;
}

//取消按钮事件
function btnCancel(id) {
	$('#' + id).window('close');
}

//保存
function save() {
	var customertype = $("#customertype").combobox('getText');
	var starttime = $('#starttime').datebox('getValue');
	var endtime = $('#endtime').datebox('getValue');
	var answertype = $("#answertype").combobox('getValue');
	var channel = $("#channel").combobox('getValues');
	var serviceinfotemplate = $("#serviceinfotemplate").textbox('getValue');
	var faqtemplate = $('#faqtemplate').textbox('getValue');
	var city = $('#city').combotree('getValues');
	if(city == null || city.length <= 0){
		city=['全国'];
	}
	var robotid = $('#robotID').combobox('getValue');
	
	var answer;
	var operationtype;
	if (customertype == "" || customertype == null) {
		$.messager.alert('提示', "请选择客户类型!", "warning");
		return;
	}
	if (channel == "" || channel == null) {
		$.messager.alert('提示', "请选择渠道!", "warning");
		return;
	}
	if (answertype == "8") { //FAQ
		answer = UM.getEditor('myEditor').getContentTxt();
		if (answer == "" || answer == null) {
			$.messager.alert('提示', "请输入答案内容!", "warning");
			return;
		}
	}
	if (answertype == "9") { //结构化知识
		answer = UM.getEditor('myEditor').getContentTxt();
		if (serviceinfotemplate == null || serviceinfotemplate == "") {
			if (answer == "" || answer == null) {
				$.messager.alert('提示', "请输入答案内容!", "warning");
				return;
			}
		}
	} else { //普通文本
		answer = UM.getEditor('myEditor').getContentTxt();
		if (answer == "" || answer == null) {
			$.messager.alert('提示', "请输入答案内容!", "warning");
			return;
		} else {
			answer = UM.getEditor('myEditor').getContent();
		}
	}
	//FAQ模板检查
	if (answertype == "8") {
		if (faqtemplate == null || faqtemplate == "") {
			$.messager.alert('提示', "FAQ模板不能为空!", "warning");
			return;
		}
		answer = faqtemplate + ";" + answer;
	}
	//信息表模板检查
	if (answertype == "9") {
		if (serviceinfotemplate == null || serviceinfotemplate == "") {
			$.messager.alert('提示', "信息表模板不能为空!", "warning");
			return;
		}
		answer = serviceinfotemplate + answer;
	}
	var data = {};
	if (insertorupdate == 0) {
		type = "insert";
		operationtype = "A";
	} else {
		var rows = $("#faqdatagrid").datagrid("getSelections");
		if (rows) {
			if (rows.length == 1) {
				var row = $('#faqdatagrid').datagrid('getSelected');
				kbansvaliddateid = row.kbansvaliddateid;
			} else {
				$.messager.alert('提示', "请选择一行记录修改!", "warning");
				return;
			}
		} else {
			$.messager.alert('提示', "请选择一行记录修改!", "warning");
			return;
		}
		type = "update";
		operationtype = "U";
	}


	$.ajax({
		url: '../faq.action',
		type: "post",
		data: {
			type: type,
			resourcetype: 'querymanage',
			operationtype: operationtype,
			resourceid: serviceid,
			customertype: customertype,
			starttime: starttime,
			endtime: endtime,
			answertype: answertype,
			channel: channel + "@@",
			answer: answer,
			service: service,
			brand: brand,
//			kbansvaliddateid: (!kbansvaliddateid?null:kbansvaliddateid.toFixed(1)), // 保留小数点后 1位数
			kbansvaliddateid : kbansvaliddateid,
			kbdataid: kbdataid,
			city: city + "@@",
			robotid : robotid
		},
		async: false,
		dataType: "json",
		success: function(data, textStatus, jqXHR) {
			if (data.success == true) {
				if (type == "update") {
					$("#faqdatagrid").datagrid("reload");
				} else {
					$("#faqdatagrid").datagrid("load");
				}

				$('#faqsddwin').panel('close');
				$.messager.alert('提示', data.msg, "info");
			} else {
				$.messager.alert('提示', data.msg, "warn");
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});



}



//构造树形图
function createServiceTree(fid, id) {
	var fdocid = "#" + fid;
	$(fdocid).combotree({
		url: '../querymanage.action?type=createservicetree&a='+ Math.random()+'&citySelect='+encodeURI(cityid), // ajax方式
		//		url:'../querymanage.action?type=createtree&a='+ Math.random(),
//		url: '../querymanage.action?type=getservicetree&citySelect=' + cityid,
		editable: true,
		//		onBeforeExpand:function(node,param){ 
		//			$(fdocid).combotree('tree').tree("options").url = "../querymanage.action?type=createtree&serviceid="+node.id +'&a='+ Math.random();
		//		}, 
		onClick: function(rec) {
			createAbsCombobox(fid, id);
		},
		onBeforeExpand:function(node){
			$(this).tree('options').url = "../querymanage.action?type=createservicetree&scenariosid="
				+ node.id + '&a=' + Math.random()+'&citySelect='+encodeURI(cityid); // 展开时发送请求去加载节点
        },
	});
}

//根据业务构造摘要下拉框
function createAbsCombobox(fid, id) {
	var fdocid = "#" + fid;
	var docid = "#" + id
		// 获取树形结构选中的业务
	var serviceid = $(fdocid).combotree("getValue");
	$(docid).combobox({
		url: '../querymanage.action?type=createabscombobox&serviceid=' + serviceid,
		valueField: 'id',
		textField: 'text',
		panelHeight: '150px'
			//editable:false 
	});
}

//打开插入话术窗口
function openAddExtraText() {
	$('#beforetext').textbox('setValue', '');
	$('#aftertext').textbox('setValue', '');
	$('#addextratextwin').window({}).window('open');
}

// 保存FAQ话术
function addExtraText4FAQ() {
	var beforetext = $('#beforetext').textbox('getValue');
	var aftertext = $('#aftertext').textbox('getValue');
	var faqtemplate = $('#faqtemplate').textbox('getValue');
	
	var content = "";
	if (faqtemplate) {
		content = beforetext + "&lt;@FAQ内容&gt;" + aftertext;
	}
	// 回填到答案内容中
	UM.getEditor('myEditor').setContent(content, false);

	//关闭窗口
	cancelAddExtraText4FAQ();
}

function cancelAddExtraText4FAQ() {
	$('#beforetext').textbox('setValue', '');
	$('#aftertext').textbox('setValue', '');
	$('#addextratextwin').window('close');
}

//插入其他形式答案
function addOtherResponse(){
	for ( var i = 1; i < 11; i++) {
		$("#otherResponse0name" + i).html("");
		$("#otherResponse0" + i).hide();
		$(".btd-display" + i).css("display","none");
	}
	 $.ajax({
			url : '../faq.action',
			type : "post",
			data : {
				type : 'getResConfig'
			},
			async : false,
			dataType : "json",
			success : function(data, textStatus, jqXHR) {
				var info = data.rows;
				for ( var i = 0; i < info.length; i++) {
					var name = info[i]["key"];
					var weight = info[i]["weight"];
					var words = info[i]["words"];
					$(".btd-display" + weight).css("display","");
					$("#otherResponse0name" + weight).html(name);
					$("#otherResponse0" + weight).show();
					var type = info[i]["value"];
					if ('自定义' == type){
						$("#otherResponse0value" + weight).textbox({
							multiline: true,
							width: 200,
							height:100
						});
						$("#otherResponse0value" + weight).textbox('clear');
					} else {
						$("#otherResponse0value" + weight).combobox({
							width: 200,
							valueField: 'id',    
					        textField: 'text',
					        data : words
						});
						$("#otherResponse0value" + weight).combobox('clear');
					}
				}
			},
			error : function(jqXHR, textStatus, errorThrown) {
				$.messager.alert('系统异常', "请求数据失败!", "error");
			}
		});
	$('#otherResponse_add').window('open');
}

function addToResponse(){
	var conditions = [];
	for ( var i = 1; i < 11; i++) {
		if ($("#otherResponse0name" + i).html() == "" ){
			break;
		}
		var con = $("#otherResponse0value" + i).textbox('getText');
		if (null == con){
			con = $("#otherResponse0value" + i).combobox('getValue');
		}
		if ('' == con){
			continue;
		}
		conditions.push('SET("' + $("#otherResponse0name" + i).html() + '","' + con + '")');
	}
	var cond = conditions.join(';');
	
	if (UM.getEditor('myEditor').getContent().indexOf('###;')!=-1){
		UM.getEditor('myEditor').setContent(cond + ';###;' + UM.getEditor('myEditor').getContent().split(';###;')[1], false);
	}else{
		UM.getEditor('myEditor').setContent(cond + ';###;' + UM.getEditor('myEditor').getContent(), false);
	}
	$('#otherResponse_add').window('close');
}