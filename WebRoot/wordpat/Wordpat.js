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

// 获取创建复选框  
function createCheckbox(cbBoxLabel, cbName, cbId, cbInputValue) {
    return new Ext.form.field.Checkbox({
        id: "checkboxCity" + cbInputValue,
        boxLabel: cbBoxLabel,
        inputValue: cbInputValue,
        checked:true
    });
}

getAllArgs();

var cityids = getArgs('cityids'); //获取url中的城市id
var citys = getArgs('citys'); //获取url中的城市名
var brand = getArgs('brand'); //获取品牌
var service = getArgs('service'); //获取当前业务
var topic = getArgs('topic'); //获取当前主题
var _abstract = getArgs('_abstract'); //获取摘要
var kbdataids = getArgs('kbdataids');//摘要ID
var serviceids = getArgs('serviceids');//业务ID

var wordpattype = getArgs('wordpattype');

var container = getArgs('container');

//流程图页面传入参数
var abs_name =getArgs('abs_name');
var pre_abs_name=getArgs('pre_abs_name');
var next_abs_names =getArgs('next_abs_names');
var chartaction=getArgs('chartaction');
var queryorresponse = getArgs('queryorresponse');


var result;
var loaded = false;//是否已经加载
//修改前的值
var editstatus = "insert"; //默认为“插入记录状态”
var oldwordpat; //修改前的词模
var oldcity; //修改前的地市
var oldautosendswitch; //修改前的自动发送开关
var commonwordpatid ;

var userid = getArgs('userid') || "";
var ioa = getArgs('ioa') || "";

if(userid != "" && ioa != ""){
	createUserInfo(userid,ioa);
}
// 创建用户信息
function createUserInfo(userid,ioa){
	$.ajax( { 
		url : '../createuser.action',
		type : "post",
		data : {
			type : 'createuserinfo',
			userid:userid,
			ioa:ioa
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if(data.msg){
			}else{
				$.messager.alert('系统异常', "请求数据失败!", "error");
				//停止加载页面
				if (window.stop) {
					window.stop();
				}else{ 
					document.execCommand("Stop"); 
				}
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {

		}
	});
}

//打开迁移编辑框
function openTransferWordpatWin() {
	//获取当前记录
    var selRecords = Ext.getCmp("gridpanelQueryResult").getSelectionModel().getSelection();
    var len = selRecords.length;
    if (len == 0) {
        Ext.MessageBox.alert("提示消息", "您未选中行");
        return false;
    }
    $('#transferwordpatwin').window({
		onOpen:function(){
			$('#transferwordpatwin').width(454.8);
		}
	});
	$('#transferwordpatwin').window('open');
	
	
	//		createServiceTree("service");
	$('#service').combotree({
		url: '../querymanage.action?type=createservicetree&a='+ Math.random(), // ajax方式
		onBeforeExpand: function(node, param) {
			$('#service').combotree('options').url = "../querymanage.action?type=createservicetree&scenariosid="
				+ node.id + '&a=' + Math.random(); // 展开时发送请求去加载节点
			
			$('#service').combotree('tree').tree('options').url = "../querymanage.action?type=createservicetree&scenariosid="
				+ node.id + '&a=' + Math.random(); // 展开时发送请求去加载节点
		}, 
		onClick:function(rec){
			createAbsCombobox();
		}
	});

}

//根据业务构造摘要下拉框
function createAbsCombobox() {
	// 获取树形结构选中的业务
	var serviceid = $('#service').combotree("getValue"); 
	$('#kbdata').combobox({    
	    url:'../querymanage.action?type=createabscombobox&serviceid='+serviceid,    
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px'
	    //editable:false 
	});  
}
function transferWordpat(){
	//获取当前记录
    var selRecords = Ext.getCmp("gridpanelQueryResult").getSelectionModel().getSelection();
    var len = selRecords.length;
    if (len == 0) {
        Ext.MessageBox.alert("提示消息", "您未选中行");
        return false;
    }
    var serviceid = $('#service').combotree("getValue"); 
    if(serviceid == null || serviceid == ""){
    	Ext.MessageBox.alert("提示消息", "您未选中业务");
    	return false;
    }
    
    // 获取树形结构选中的业务
	var kbdataid = $('#kbdata').combobox("getValue"); 
    if(kbdataid == null || kbdataid == ""){
    	Ext.MessageBox.alert("提示消息", "您未选中标准问");
    	return false;
    }
    var wordpatid = selRecords[0].raw.wordpatid;
    $.ajax({
    	url:'../wordpat.action',
    	type:'post',
    	dataType:'json',
    	data:{
    		'action':'transferWordpat',
    		'wordpatids' : wordpatid,
    		'kbdataid' : kbdataid,
    		'operationtype':'U',
    		'resourceid':serviceids,
    		'param':JSON.stringify({
    			'service':serviceid
    		})
    	},
    	success:function(data){
    		if(data.success){
    			$('#transferwordpatwin').window('close');
    			Ext.MessageBox.alert("提示消息", "迁移成功！");
    			
    			var gridpanelQueryResult = Ext.getCmp('gridpanelQueryResult');
                var ToolBar = Ext.getCmp("ToolBar");
                var page = ToolBar.store.currentPage;
                if(gridpanelQueryResult.store.data.length<=1){
                    page = page-1;
                    gridpanelQueryResult.store.loadPage(page);
                    if(page==0){
                        gridpanelQueryResult.store.loadPage(1);
                    }
                }else{
                    gridpanelQueryResult.store.loadPage(page);
                }
    		}else{
    			Ext.MessageBox.alert("提示消息",data.checkInfo);
    		}
    	},
    	error:function(){
    		Ext.MessageBox.alert('系统异常', '请求数据失败！');
    	}
    });
}
//取消按钮事件
function btnCancel(id) {
	$('#' + id).window('close');
}
/*
 city="上海";
 cityids="284";
 brand ="IPTV";
 service ="IPTV测试";
 topic="使用";
 _abstract ="<IPTV测试>使用";
 wordpattype=0;

*/

/*测试用！
cityids = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20";
brand = "神州行";
service = "12593资费";
topic = "比较";
_abstract = "12593、17951与直拨资费对比";
wordpattype = 0;
//*var args = {};
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

// 获取创建复选框  
function createCheckbox(cbBoxLabel, cbName, cbId, cbInputValue) {
    return new Ext.form.field.Checkbox({
        id: "checkboxCity" + cbInputValue,
        boxLabel: cbBoxLabel,
        inputValue: cbInputValue
    });
}

getAllArgs();

var cityids = getArgs('cityids'); //获取url中的城市id
var citys = getArgs('citys'); //获取url中的城市名
var brand = getArgs('brand'); //获取品牌
var service = getArgs('service'); //获取当前业务
var topic = getArgs('topic'); //获取当前主题
var _abstract = getArgs('_abstract'); //获取摘要

var wordpattype = getArgs('wordpattype');
var result;
var loaded = false;//是否已经加载
//修改前的值
var editstatus = "insert"; //默认为“插入记录状态”
var oldwordpat; //修改前的词模
var oldcity; //修改前的地市
var oldautosendswitch; //修改前的自动发送开关

/*测试用！
cityids = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20";
brand = "神州行";
service = "12593资费";
topic = "比较";
_abstract = "12593、17951与直拨资费对比";
wordpattype = 0;
//*/


