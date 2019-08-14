var args = {};
var wordclass ;
var word;
var wordid;

$(function(){
	 getAllArgs();
	 wordclass = getArgs('wordclass');
	 word =getArgs('worditem');
	 wordid =getArgs('wordid');
	 $("#worclassname").textbox('setValue',wordclass);
	 $("#wordname").textbox('setValue',word);
	 getWordCity(wordid,wordclass);
	 
	}
)
//查询词条地市信息
function getWordCity(wordid,wordclass){
	$.ajax({
		type : "POST",
		url : "../worditem.action",
		async : false,
		data:{action:"selectWordCity", wordid:wordid, curwordclass:wordclass},
		success : function(data, textStatus, jqXHR) {
			var cityname = data.cityname;
			$("#cityname").val(cityname);
			//alert(cityname);
			getCityTree(cityname);
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
		url:'../getCityTree.action',
		editable:false, 
		multiple:true,
		queryParams:{
			local : cityname
		}
	}); 
}

function update(){
	var cityCode = $("#selLocal").combotree("getValues");
	var cityName = $('#selLocal').combotree('getText');
//	alert(cityCode);
//	alert(cityName);
	
	var request = {
			action:"updateWordCity",
			wordid:wordid,
			curwordclass:wordclass, 
			citycode:cityCode, 
			cityname:cityName
		};
		
		var dataStr = {
			m_request : JSON.stringify(request)
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
	
		$.post("../worditem.action",dataStr,function(data) {
			 if(data.success){ 
				 $("#cityname").val(cityName); 
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