<%@ page language="java" contentType="text/html; charset=utf-8" import="java.util.*" pageEncoding="utf-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    
    <title>unknown</title>
    
	<meta http-equiv="X-UA-Compatible" content="IE=8" > 
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>
	<link type="text/css" rel="stylesheet" href="../easyui/jquery-easyui-1.4.1/themes/default/easyui.css" />
	<link type="text/css" rel="stylesheet" href="../easyui/jquery-easyui-1.4.1/themes/icon.css" />
	<script type="text/javascript" src="../easyui/jquery-1.8.0.min.js"></script>
	<script type="text/javascript" src="../easyui/jquery-easyui-1.4.1/jquery.easyui.min.js"></script>
	<script type="text/javascript" src="../easyui/jquery-easyui-1.4.1/locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript" src="../script/json2.js"></script>
	<script type="text/javascript" src="./js/configserviceattr.js"></script>
	<script type="text/javascript" src="../script/publicJqueryAjaxComplete.js"></script>

  </head>
  
  <body >
    <div >
    <span style="margin:10px 10px 10px 0px">词类名称:</span><input id="worclassname" type="text" readonly="readonly" editable="false" class="easyui-textbox" style="width:300px;"/><br/><br/>
    <span style="margin:10px 10px 10px 0px">词条名称:</span><input id="wordname" type="text" readonly="readonly" editable="false" class="easyui-textbox" style="width:300px;"/><br/><br/>
    <span style="margin:10px 10px 10px 0px">归属地市:</span><textarea id ="cityname" name="" cols="6" rows="6" readonly="readonly"  style="width:295px;" ></textarea><br/><br/>
    <span style="margin:10px 10px 10px 0px">编辑地市:</span><input id="selLocal" class="easyui-combotree" style="width:300px">
    <a class="easyui-linkbutton"  plain="false" onclick="update()">更新</a>
  
	</div>
  </body>
</html>
