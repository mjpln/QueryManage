<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<html>
<head>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>共享语义</title>
	<link type="text/css" rel="stylesheet" href="easyui/jquery-easyui-1.4.1/themes/default/easyui.css" />
	<link type="text/css" rel="stylesheet" href="easyui/jquery-easyui-1.4.1/themes/icon.css" />
	<link type="text/css" rel="stylesheet" href="css/scrollbar.css" />
    <script type="text/javascript" src="script/loading-jsp.js"></script>
</head>
<body>
	<div style="margin: 5px;">	
		<div style="float: left;">
	        <label for="name" style="font-size: 12px">问题:</label>
	        <input type="text" id='ex_abstracts' name="name"  style="width:300px;"/>
	        <label for="city" style="font-size: 12px">地市:</label>
	        <input type="text" id='ex_city' name="city" readonly="readonly" style="width:450px;border-style:none"/>
        </div>
        <div style="float: right;">
            <a class="easyui-linkbutton" onclick="exTestBtn()" data-options="iconCls:'icon-tip'">分词并查看语义</a>
             <a class="easyui-linkbutton" onclick="reportError()" data-options="iconCls:'icon-cancel'">报错</a>
        </div>
        <br style="clear: both;">
    </div>
	<div class="easyui-tabs" id="ex_mytabs">
	    <div title="普通显示" data-options="iconCls:'icon-knowledgehit'" style="padding:5px;">
	    	<div id="ex_word"></div>
	    	<div><table id="ex_abs_dg"></table></div>
	    </div>

	    <div title="添加业务词" data-options="iconCls:'icon-add'" >
	        <table>
  				<tr>
  					<td>
    					<div id="addServiceWord" class="easyui-datagrid" style="padding:5px;"></div>
    				</td>
  				</tr>
  			</table>

			<div id="addServiceWordTool"> 
				<label  style="padding:10px;font-size: 12px">业务词:</label>
				<input id="serviceword" type="text" class="easyui-textbox"  style="width:150px"/>				
				<a href="javascript:void(0)" id="standardWordSel" class="easyui-linkbutton" data-options="iconCls:'icon-search'" plain="false" onclick="selServiceWord()">查询</a>
				<a href="javascript:void(0)" id="serviceWordSave" class="easyui-linkbutton" data-options="iconCls:'icon-save'" plain="false" onclick="wordSave('serviceWord')">保存</a>
				<a href="javascript:void(0)" id="serviceWordCancel" class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" plain="false" onclick="wordCancel('serviceWord')">取消</a>
				<a href="javascript:void(0)" id="serviceWordAdd" class="easyui-linkbutton" data-options="iconCls:'icon-add'" plain="false" onclick="serviceWordAdd('serviceWord')">新增</a>
				<a class="easyui-linkbutton" onclick="updateKbdata()"  data-options="iconCls:'icon-reload'">更新知识库</a>
			</div>
	
<!--   			<div id="otherWordTool"> 
				<input id="wordsel2" type="text" class="easyui-textbox" style="width:150px;height:25px"/>
				<a href="javascript:void(0)" id="otherWordSel" class="easyui-linkbutton" data-options="iconCls:'icon-search'" plain="false" onclick="selOtherWord()">查询</a>
				<div style="text-align: right;float: right">
					<a href="javascript:void(0)" id="otherWordSave" class="easyui-linkbutton" data-options="iconCls:'icon-save'" plain="false" onclick="wordSave('other')">保存</a>
					<a href="javascript:void(0)" id="otherWordCancel" class="easyui-linkbutton" data-options="iconCls:'icon-cancel'" plain="false" onclick="wordCancel('other')">取消</a>
					<a href="javascript:void(0)" id="otherWordAdd" class="easyui-linkbutton" data-options="iconCls:'icon-add'" plain="false" onclick="wordAdd('other')">新增</a>
					<a href="javascript:void(0)" id="otherWordEdit" class="easyui-linkbutton" data-options="iconCls:'icon-edit'" plain="false" onclick="wordEdit('other')">修改</a>
					 <a class="easyui-linkbutton" onclick="updateKbdata()"  data-options="iconCls:'icon-reload'">更新知识库</a>
				</div>
			</div> -->
  
<!--   			<div id="wordCity" class="easyui-dialog" style="padding:10px;display:none;">
  				<br>
  				<span style="margin:10px 10px 10px 0px">词类名称:</span><input id="worclassname" type="text" readonly="readonly" editable="false" class="easyui-textbox" style="width:300px;"/><br/><br/>
    			<span style="margin:10px 10px 10px 0px">词条名称:</span><input id="wordname" type="text" readonly="readonly" editable="false" class="easyui-textbox" style="width:300px;"/><br/><br/>
    			<span style="margin:10px 10px 10px 0px">归属地市:</span><textarea id ="cityname" name="" cols="6" rows="6" readonly="readonly"  style="width:295px;font-size:12px;" ></textarea><br/><br/>
    			<span style="margin:10px 10px 10px 0px">编辑地市:</span><input id="selLocal" class="easyui-combotree" style="width:300px;">
   				<a class="easyui-linkbutton"  plain="false" onclick="update()">更新</a>
  			</div> -->
	    </div>
	</div>
			<!-- 新增 dialog -->
	<div id="ex_add_dialog" style="display: none;">
		<form id="ex_add_form" method="post">
			<div style="margin:10px;">
				问题库业务：<input id="questionservice" name="service" style="width:503px;">
			</div>
		
		    <div style="margin: 10px;">
		       	问题库摘要：<input id="questionkbdata" name="kbdataid" style="width:500px;">
		    </div>
		    <div style="margin: 10px;">
		       	继 承 地 市：<input id="inheritCityadd" name="attr15" style="width:500px;">
		    </div>
		    <div style="margin: 10px;">
		    	业 务 X：<input id="serviceX" name="attr8" class="easyui-textbox" style="width:510px;">
		    </div>
		    <div style="margin: 10px;">
		    	业 务 Y：<input id="serviceY" name="attr9" class="easyui-textbox" style="width:510px;">
		    </div>
		    <div style="margin: 10px;">
		    	业 务 Z：<input id="serviceZ" name="attr10" class="easyui-textbox" style="width:510px;">
		    </div>
		    <div style="margin: 10px;">
		    	业 务 L：<input id="serviceL" name="attr11" class="easyui-textbox" style="width:510px;">
		    </div>
		    <div style="margin: 10px;">
		    	业 务 M：<input id="serviceM" name="attr12" class="easyui-textbox" style="width:510px;">
		    </div>
		    <div style="margin: 10px;">
		    	业 务 N：<input id="serviceN" name="attr13" class="easyui-textbox" style="width:510px;">
		    </div>
		    <div style="margin: 10px 50px;text-align: right;">
		    	<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" onclick="addExtendsave()">继承</a>
		    </div>
		</form>
	</div>
	
	<!-- 修改 dialog -->
 	<div id="ex_dialog" style="display: none;">
		<form id="ex_form" method="post">
		    <div style="margin: 10px;">
		       	问题库摘要：<input name="abstracts" readonly="readonly" style="width:500px;">
		        <input type="hidden" name="kbdataid"/>
		        <input type="hidden" name="attr6"/>
		    </div>
		    <div style="margin: 10px;">
		       	继 承 地 市：<input id="inheritCity" name="attr15" style="width:500px;">
		    </div>
		    <div style="margin: 10px;">
		    	业 务 X：<input name="attr8" class="easyui-textbox" style="width:510px;">
		    </div>
		    <div style="margin: 10px;">
		    	业 务 Y：<input name="attr9" class="easyui-textbox" style="width:510px;">
		    </div>
		    <div style="margin: 10px;">
		    	业 务 Z：<input name="attr10" class="easyui-textbox" style="width:510px;">
		    </div>
		    <div style="margin: 10px;">
		    	业 务 L：<input name="attr11" class="easyui-textbox" style="width:510px;">
		    </div>
		    <div style="margin: 10px;">
		    	业 务 M：<input name="attr12" class="easyui-textbox" style="width:510px;">
		    </div>
		    <div style="margin: 10px;">
		    	业 务 N：<input name="attr13" class="easyui-textbox" style="width:510px;">
		    </div>
		    <div style="margin: 10px 50px;text-align: right;">
		    	<a class="easyui-linkbutton" data-options="iconCls:'icon-save'" onclick="saveExtend()">保存</a>
		    </div>
		</form>
	</div>
	
		<!-- 详情 dialog -->
 	<div id="ex_detail_dialog" style="display: none;">
		<form id="ex_detail_form" method="post">
		    <div style="margin: 10px;">
		       	问题库摘要：<input name="abstracts" readonly="readonly" style="width:500px;">
		        <input type="hidden" name="kbdataid"/>
		        <input type="hidden" name="attr6"/>
		    </div>
		    <div style="margin: 10px;">
		       	继 承 地 市：<input name="attr15" style="width:500px;">
		    </div>
		    <div style="margin: 10px;">
		    	业 务 X：<input name="attr8" class="easyui-textbox" style="width:510px;">
		    </div>
		    <div style="margin: 10px;">
		    	业 务 Y：<input name="attr9" class="easyui-textbox" style="width:510px;">
		    </div>
		    <div style="margin: 10px;">
		    	业 务 Z：<input name="attr10" class="easyui-textbox" style="width:510px;">
		    </div>
		    <div style="margin: 10px;">
		    	业 务 L：<input name="attr11" class="easyui-textbox" style="width:510px;">
		    </div>
		    <div style="margin: 10px;">
		    	业 务 M：<input name="attr12" class="easyui-textbox" style="width:510px;">
		    </div>
		    <div style="margin: 10px;">
		    	业 务 N：<input name="attr13" class="easyui-textbox" style="width:510px;">
		    </div>
		</form>
	</div>
	
	<script type="text/javascript" src="easyui/jquery-1.8.0.min.js"></script>
	<script type="text/javascript" src="easyui/jquery-easyui-1.4.1/jquery.easyui.min.js"></script>
	<script type="text/javascript" src="easyui/jquery-easyui-1.4.1/locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript" src="extend/extends.js"></script>
 	<script type="text/javascript" src="extend/serviceword.js"></script>
	<script type="text/javascript" src="script/json2.js"></script>
	<script type="text/javascript" src="script/common.js"></script>
	<script type="text/javascript" src="script/publicSetup.js"></script>
</body>
</html>