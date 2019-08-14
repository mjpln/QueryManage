<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>相关问题-新增</title>
    
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
    	<form id="rqadd-form" method="post">
    		<div style="margin-bottom:20px">
    			<label>相关问题：</label>
    			<input id="rqadd-relaterequery" name="relatequery.relatequerytokbdataid" style="width:80%"  class="easyui-combobox"/>
    		</div>
    		<div style="margin-bottom:20px">
    			<label>备注：&nbsp;&nbsp;</label>
    			<input id="rqadd-remark" name="relatequery.remark" style="width:80%"  class="easyui-textbox"/>
    		</div>
    		<div style="text-align:center;padding:5px 0">
    			<a id="rgadd-save" class="easyui-linkbutton" data-options="width:80">添加</a>
    		</div>
    	</form>
    </div>
    
    
    <script type="text/javascript">
    	$(function(){
    		$('#rqadd-relaterequery').combobox({
    			url:'relatequery/listRelatequeries.action?ioa=${ioa}',
    			valueField:'value',
    			textField:'text',
    			loadFilter: function(data){
    				if (data.response && data.response.success){
						return data.response.body;
					}
    			}
    		});
    		
    		// 提交表单
    		$('#rgadd-save').click(function(){
	    		$("#rqadd-form").form('submit',{
	    			url:'relatequery/add.action',
	    			queryParams:{
	    				"relatequery.kbdataid": '${kbdataid}',
	    				"relatequery.relatequery":$('#rqadd-relaterequery').combobox('getText')
	    			},
	    			onSubmit: function(param){
	    			
	    			},
	    			success: function(data){
	    				var data = eval('(' + data + ')');
	    				if(data.response && data.response.success){
	    					$.messager.alert('新增', '新增成功', 'info');
	    				} else{
	    					$.messager.alert('新增', '新增失败。【'+data.response.message+'】', 'warn');
	    				}
	    				$('#rg-win').window('close');
	    				$('#rg-dg-result').datagrid('reload');
	    			}
	    		});
    		})
    		
    	})
    </script>
  </body>
</html>
