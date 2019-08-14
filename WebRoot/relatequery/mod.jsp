<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>相关问题-修改</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->

  </head>
  
  <body>
    <div style="padding:30px 60px;"">
    	<form id="rqupd-form" method="post">
    		<div style="margin-bottom:20px">
    			<input id="rqupd-relaterequery" name="" value="${relatequery.relatequery}" style="width:100%"  class="easyui-combobox" data-options="label:'相关问题：', disabled:true"/>
    		</div>
    		<div style="margin-bottom:20px">
    			<label></label>
    			<input id="rqupd-remark" name="relatequery.remark" value="${relatequery.remark}" style="width:100%"  class="easyui-textbox" data-options="label:'备注：'"/>
    		</div>
    		<div style="text-align:center;padding:5px 0">
    			<a id="rgupd-edit" class="easyui-linkbutton" data-options="width:80">修改</a>
    		</div>
    	</form>
    </div>
    
    
    <script type="text/javascript">
    	$(function(){
    		
    		// 提交表单
    		$('#rgupd-edit').click(function(){
	    		$('#rqupd-form').form('submit',{
	    			url:'relatequery/modify.action',
	    			queryParams:{
	    				"relatequery.id": '${id}',
	    			},
	    			onSubmit: function(param){
	    			
	    			},
	    			success: function(data){
	    				var data = eval('(' + data + ')');
	    				if(data.response && data.response.success){
	    					$.messager.alert('修改', '修改成功', 'info');
	    				} else{
	    					$.messager.alert('修改', '修改失败。【'+data.response.message+'】', 'warn');
	    				}
	    				
	    				$('#rg-win').window('close');
	    				$('#rg-dg-result').datagrid('reload');
	    			}
	    		});
    		});
    		
    	})
    </script>
  </body>
</html>
