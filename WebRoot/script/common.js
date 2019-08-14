//函数格式 
function trim(str){ //删除左右两端的空格
　　     return str.replace(/(^\s*)|(\s*$)/g, "");
　　 }
　　 function ltrim(str){ //删除左边的空格
　　     return str.replace(/(^\s*)/g,"");
　　 }
　　 function rtrim(str){ //删除右边的空格
　　     return str.replace(/(\s*$)/g,"");
}

 //方法格式
 String.prototype.trim=function(){
　    return this.replace(/(^\s*)|(\s*$)/g, "");
　 }
　 String.prototype.ltrim=function(){
　    return this.replace(/(^\s*)/g,"");
　 }
　 String.prototype.rtrim=function(){
　    return this.replace(/(\s*$)/g,"");
　 }
 
//替换字符串中所有空格
 function replaceSpace(str){
 	str = str.replace(new RegExp(' ','g'),''); 
 	return str;
 }

 //地址栏参数
 function UrlParams() { 
 	var name, value;
 	var str = location.href; // 取得整个地址栏
 	var num = str.indexOf("?");
 	str = str.substr(num + 1); // 取得所有参数
 	var arr = str.split("&"); // 各个参数放到数组里
 	for ( var i = 0; i < arr.length; i++) {
 		num = arr[i].indexOf("=");
 		if (num > 0) {
 			name = arr[i].substring(0, num);
 			value = arr[i].substr(num + 1);
 			this[name] = value;
 		}
 	}
 }
 
 //去重
 function quchong(a){
	    var n = {},r = []; //n为hash表，r为临时数组
	    for(var i = 0; i < a.length; i++){ //遍历当前数组
	        if (!n[a[i]]){ //如果hash表中没有当前项
	            n[a[i]] = true; //存入hash表
	            r.push(a[i]); //把当前数组的当前项push到临时数组里面
	        }
	    }
	    return r;
}
 
 function myKeyDown() {
		var k = window.event.keyCode;
		if (8 == k) {
			event.keyCode = 0;// 取消按键操作
		}
	}

 function str2html(str){
	 return $('<div>').html(str).html();
 }