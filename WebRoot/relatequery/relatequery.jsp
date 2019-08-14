<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" isELIgnored="false"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>相关问题</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">

	<!-- 
	<link rel="stylesheet" type="text/css" href="static/css/themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="static/css/themes/icon.css">
	<link rel="stylesheet" type="text/css" href="static/css/demo.css">
	<script type="text/javascript" src="static/js/jquery.min.js"></script>
	<script type="text/javascript" src="static/js/jquery.easyui.min.js"></script>
	<script type="text/javascript" src="static/js/easyui-lang-zh_CN.js"></script>
	 -->
	 
	<link type="text/css" rel="stylesheet" href="easyui/jquery-easyui-1.4.1/themes/default/easyui.css" />
	<link type="text/css" rel="stylesheet" href="easyui/jquery-easyui-1.4.1/themes/icon.css" />
	<script type="text/javascript" src="easyui/jquery-1.8.0.min.js"></script>
	<script type="text/javascript" src="easyui/jquery-easyui-1.4.1/jquery.easyui.min.js"></script>
	<script type="text/javascript" src="easyui/jquery-easyui-1.4.1/locale/easyui-lang-zh_CN.js"></script>
  </head>
  
<body>
	<div style="margin:20px 0; width:1200px;">
		<form>
			<label>相关问题：</label>
			<input id="sq-f-relatequery" class="easyui-textbox" data-options="width:1000"/>
			<a id="rq-search" class="easyui-linkbutton" style="float:right" data-options="width:80">查询</a>
		</form>
		<table id="rg-dg-result">
		</table>
	</div>
	<div id="rg-win"></div>
	<script type="text/javascript">
	    $('#rg-dg-result').datagrid({
	        url:'relatequery/list.action',
	        queryParams:{
	        	kbdataid: '${kbdataid}',
	        	ioa: '${ioa}'
	        },
	        pagination:true,
	        height:400,
	        width:1200,
	        columns:[[
	        	{field:'ck', checkbox:true},
	        	{field:'id', hidden:true},
	            {field:'relatequery',title:'相关问题',width:600},
	            {field:'remark',title:'备注',width:400}
	        ]],
	        loadFilter: function(data){
				if (data.response){
					return data.response;
				} else {
					return data;
				}
			},
			onSelect:function(index, row){
				if($('#rg-dg-result').datagrid('getSelections').length > 0){
					
				}
			}
	    });
	
		$('#rq-search').click(function(){
			var str = $('#sq-f-relatequery').textbox('getValue')
			$('#rg-dg-result').datagrid('load',{
				kbdataid: '${kbdataid}',
				ioa:'${ioa}',
				relatequeryStr:str
			});
		})
		
		
		 $(function(){
            var pager = $('#rg-dg-result').datagrid('getPager');
            pager.pagination({
                buttons:[{
                    iconCls:'icon-add',
                    text:'添加',
                    handler:function(){
                        $('#rg-win').window({
                        	href:'relatequery/addPage.action?kbdataid=${kbdataid}&ioa=${ioa}',
						    width:600,
						    height:300,
						    title:'新增-相关问题',
						    collapsible:false,
						    collapsible:false,
						    collapsible:false,
						    modal:true
						})
                    }
                },{
                    iconCls:'icon-edit',
                    text:'修改',
                    handler:function(){
                    	var id = $('#rg-dg-result').datagrid('getSelected').id;
                        $('#rg-win').window({
                        	href:'relatequery/modPage.action?id=' + id,
						    width:600,
						    height:300,
						    title:'修改-相关问题',
						    collapsible:false,
						    collapsible:false,
						    collapsible:false,
						    modal:true
						})
                    }
                },{
                    iconCls:'icon-remove',
                    text:'删除',
                    handler:function(){
                        var ids = [];
						var rows = $('#rg-dg-result').datagrid('getSelections');
						for(var i=0; i<rows.length; i++){
							ids.push(rows[i].id);
						}
						$.messager.confirm('确认','确认删除？', function(r){
							$.ajax({
					            url: 'relatequery/delete.action',
					            dataType: 'json',
					            traditional: true,//阻止深度序列化
					            data: { 'ids': ids },
					            success: function (data) {
					                if(data.response && data.response.success){
					                	$.messager.alert('删除', '删除成功', 'info');
					                }else{
					                	$.messager.alert('删除', '删除失败。【'+data.response.message+'】',  'warn');
					                }
				                	$('#rg-dg-result').datagrid('reload');
					            }
					        });
						});
						 
                    }
                }]
            });            
        })
	
		
	</script>
</body>
</html>
