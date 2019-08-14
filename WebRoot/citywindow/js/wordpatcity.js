var args = {};
var abstractid ;
var abs;
var wordid;
var serviceid;
var wordpatid;

$(function(){
	 getAllArgs();
	 wordpatid = getArgs('wordpatid');
	 wordpat =getArgs('wordpat');
	 serviceid =getArgs('serviceid');
	 $("#wordpat").textbox('setValue',wordpat);
	 createCityTree('selLocal','edit',true);
	 
	}
)
//查询词模地市信息
function getWordpatCity(wordpatid){
	$.ajax({
		type : "POST",
		url : "../wordpat.action",
		async : false,
		data:{type:"selectWordpatCity",
		wordpatids:wordpatid
	},
		success : function(data, textStatus, jqXHR) {
			var cityname = data.cityname;
			var cityCode = data.cityid;
			$("#cityname").val(cityname);
			$('#selLocal').combotree('setValues', cityCode);
			
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
		url:'../querymanage.action',
		editable:false, 
		multiple:true,
		queryParams:{
		    type:"getcitytree",
			local : cityname
		}
	}); 
}

//创建citytree
function createCityTree(id,flag,isMultiple){
	var id ="#"+id;
	$(id).combotree({
		url : "../querymanage.action",
		editable:false, 
		multiple:isMultiple,
		queryParams:{
		type : "createcitytreebylogininfo",	
		flag: flag
		},
		onLoadSuccess:function(node,data){  
		getWordpatCity(wordpatid);  
    }  
	}); 
}


function update(){
	var cityCode=[]
	      	 cityCode = $("#selLocal").combotree("getValues");
	      	var cityName = $('#selLocal').combotree('getText');
	      	if (cityCode.indexOf("全国") > -1) {
	      		cityCode = ["全国"];
	      		cityName="全国";
	      	}
	      	if(cityCode==null||cityCode==""){
	      		 $.messager.alert('系统提示','请选择地市!' , "info"); 
	      		 return false;
	      	}
	      	var dataStr = {
	      		type : "updateWordpatCity",
	      		wordpatid : wordpatid,
	      		wordpatids : wordpatid,
	      		citycode : cityCode.join('|'),
	      		resourcetype:"wordpat",
	            operationtype:"U",
	            resourceid:serviceid
	      	}
//	$.ajax({
//		type : "POST",
//		url : "../worditem.action",
//		async : false,
////		data:{action:"updateWordCity", wordid:wordid,curwordclass:wordclass, citycode:cityCode, cityname:cityName},
//		data:dataStr,
//		success : function(data, textStatus, jqXHR) {
//			 if(data.success){ 
//				 $("#cityname").val(cityName); 
//				 $.messager.alert('提示',data.msg , "info");
//			 }else{
//				 $.messager.alert('提示',data.msg , "info"); 
//			 }
//			
//		},
//				
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
//		}
//	});
	
		$.post("../wordpat.action",dataStr,function(data) {
			 if(data.success){ 
				 $("#cityname").val(cityName);
				 $("#selLocal").combotree("setValues",cityCode);
				 $.messager.alert('提示',data.msg , "info");
			 }else{
				 $.messager.alert('提示',data.msg , "info"); 
			 }
		},"json");		
		
		
		
		
	
	
    
} 


function getArgs(strParam) {
    return args[strParam];
}

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