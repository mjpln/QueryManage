var screenWidth;// 屏宽
var screenHeight;// 屏高

$(function() {
	// 初始化页面样式
	initCss();
	// 验证格式
	validType();
	// 启动验证
	$('.validatebox-text').bind('blur', function() {
		$(this).validatebox('enableValidation').validatebox('validate');
	});
	// 选择商家触发事件
	clickBrand();
});

// 初始化页面样式
function initCss() {
//	screenHeight = $(window).height();
//	screenWidth = $(window).width();
	screenHeight = '100%';
	screenWidth = '100%';
	$("#body").css("width", screenWidth);
	$("#body").css("height", screenHeight);

	// 初始化商家下拉框数据
	$("#combForBrand").combobox({
		url : '../changewordpat.action?type=getBrand',
		valueField : 'id',
		textField : 'text'
	});
	// 初始化商家下拉框数据
	$("#combForBrandsec").combobox({
		url : '../changewordpat.action?type=getBrand',
		valueField : 'id',
		textField : 'text'
	});

	// 替换摘要样式
	$("#combForReplace").combobox({
		mode : 'remote',
		valueField : 'id',
		textField : 'text',
		loader : ComboDataLoader
	});

	// 必选摘要样式
	$("#combForMandatory").combobox({
		mode : 'remote',
		valueField : 'id',
		textField : 'text',
		loader : combForBrandsec
	});
}

// 选择商家触发事件
function clickBrand() {
	// 商家下拉框
	$('#combForBrand').combobox({
		onSelect : function() {
			// 初始化商家下拉框数据
			$("#combForReplace").combobox({
				mode : 'remote',
				valueField : 'id',
				textField : 'text',
				loader : ComboDataLoader
			});
		}
	});
	// 第二个商家下拉框
	$('#combForBrandsec').combobox({
		onSelect : function() {
			// 初始化商家下拉框数据
			$('#combForMandatory').combobox({
				mode : 'remote',
				valueField : 'id',
				textField : 'text',
				loader : combForBrandsec
			});
		}
	});
}

// 验证格式
function validType() {
	$
			.extend(
					$.fn.validatebox.defaults.rules,
					{
						match : {
							validator : function(value, param) {
//								var pattern = /^\[?[A-Za-z0-9\u4e00-\u9fa5]+([|][A-Za-z0-9\u4e00-\u9fa5]+)*\]?(\*\[?[[A-Za-z0-9\u4e00-\u9fa5]+([|][A-Za-z0-9\u4e00-\u9fa5]+)*)*\]?$/;
								var pattern = /^\[?[0-9a-zA-Z\u4e00-\u9fa5]+([|][0-9a-zA-Z\u4e00-\u9fa5]+)*\]?(\*\[?[0-9a-zA-Z\u4e00-\u9fa5]+([|][0-9a-zA-Z\u4e00-\u9fa5]+)*\]?)*$/
								return pattern.test(value);
							},
							message : '词模格式错误'
						},
						matchRequire : {
							validator : function(value, param) {
								var pattern = /^[A-Za-z0-9\u4e00-\u9fa5]+(近类|父类)$/;
								return pattern.test(value);
							},
							message : '格式错误'
						}
					});
}

// Combobox过滤从远程服务器加载数据 param:传递的参数,
function ComboDataLoader(param, success, error) {
	var brand = $("#combForBrand").combobox("getText");
	if (brand == undefined || brand == "" || brand == null) {
		return;
	}
	// 获取combobox输入的值
	var q = param.q;
	if (q == undefined || q == "" || q == null) {
		$.ajax({
			url : "../changewordpat.action?type=search&brand=" + brand,
			type : "post",
			dataType : "json",
			success : function(data) {
				// 执行loader的success回调函数(装载数据)
				success(data);
			},
			// 异常处理
			error : function(xml, text, msg) {
				error.apply(this, arguments);
			}
		});
		return false;
	}
	$.ajax({
		url : "../changewordpat.action?type=searchByText",
		type : "post",
		data : {
			searchTxt : q,
			brand : brand
		},
		dataType : "json",
		success : function(data) {
			// 执行loader的success回调函数(装载数据)
			success(data);
		},
		// 异常处理
		error : function(xml, text, msg) {
			error.apply(this, arguments);
		}
	});
}

// Combobox过滤从远程服务器加载数据 param:传递的参数,
function combForBrandsec(param, success, error) {
	var brand = $("#combForBrandsec").combobox("getText");
	if (brand == undefined || brand == "" || brand == null) {
		return;
	}
	// 获取combobox输入的值
	var q = param.q;
	if (q == undefined || q == "" || q == null) {
		$.ajax({
			url : "../changewordpat.action?type=search&brand=" + brand,
			type : "post",
			dataType : "json",
			success : function(data) {
				// 执行loader的success回调函数(装载数据)
				success(data);
			},
			// 异常处理
			error : function(xml, text, msg) {
				error.apply(this, arguments);
			}
		});
		return false;
	}
	$.ajax({
		url : "../changewordpat.action?type=searchByText",
		type : "post",
		data : {
			searchTxt : q,
			brand : brand
		},
		dataType : "json",
		success : function(data) {
			// 执行loader的success回调函数(装载数据)
			success(data);
		},
		// 异常处理
		error : function(xml, text, msg) {
			error.apply(this, arguments);
		}
	});
}

// 替换目标串
function replace() {
	var kbdataid = $('#combForReplace').combobox("getValue");
	var targetStr = $("#target").textbox("getText");
	var replaceStr = $("#replace").textbox("getText");
	var brand = $("#combForBrand").textbox("getText");

	if (kbdataid == "" || kbdataid == null) {
		$.messager.confirm('警告', '全量替换有风险，还要继续吗？', function(r) {
			if (r) {
				replaceAction();
			}
		});
	} else {
		replaceAction();
	}

	// 请求后台执行replace操作
	function replaceAction() {
		if (isValid()) {
			$.ajax({
				url : "../changewordpat.action",
				type : "post",
				dataType : "json",
				data : {
					type : "replace",
					target : targetStr,
					replace : replaceStr,
					kbdataid : kbdataid,
					brand : brand
				},
				success : function(data) {
					var obj = data;
					if (obj.success == true) {
						$.messager.alert('提示', '替换成功', 'info');
					} else {
						$.messager.alert('提示', '替换失败', 'info');
					}
				}
			});
		}
	}
}

// 验证文本框内容是否有效
function isValid() {
	$('#target').textbox({
		novalidate : false
	});
	$('#replace').textbox({
		novalidate : false
	});
	brand=$('#combForBrand').combobox('getText');
	if(brand==null||brand==undefined||brand==''){
		$('#combForBrand').combobox({
			novalidate : false
		});
	}
	ismatchtar = $('#target').textbox("isValid");
	ismatchaim = $('#replace').textbox('isValid');
	ismatbrand = $('#combForBrand').textbox('isValid');
	if (ismatchtar && ismatchaim && ismatbrand)
		return true;
	else
		return false;
}

// 用于在指定位置添加字符串
function insertStr(soure, start, newStr) {
	return soure.slice(0, start) + newStr + soure.slice(start);
}

// 添加必选词类
function addMastWordClass() {
	$('#requiredWord').textbox({
		novalidate : false
	});
	brand = $('#combForBrandsec').combobox('getText');
	if(brand==undefined||brand==null||brand==''){
		$('#combForBrandsec').combobox({
			novalidate : false
		});
	}
	ismatchtar = $('#requiredWord').textbox("isValid");
	ismatchbrand = $('#combForBrandsec').combobox("isValid");
	var kbdataid = $('#combForMandatory').combobox("getValue");
	if (ismatchtar && ismatchbrand) {
		if (kbdataid == "" || kbdataid == null) {
			$.messager.confirm('警告', '全量替换有风险，还要继续吗？',
					function(r) {
						if (r) {
							commitWordClass();
						}
					});
		} else {
			commitWordClass();
		}
	}
}

// 提交处理必选词
function commitWordClass() {
	replaceStr = $('#requiredWord').textbox('getValue');
	kbdataid = $('#combForMandatory').combobox('getValue');
	brand = $('#combForBrandsec').combobox('getText');
	$.ajax({
		url : "../changewordpat.action",
		type : "post",
		dataType : "json",
		data : {
			type : "addRequireWordClass",
			replace : replaceStr,
			kbdataid : kbdataid,
			brand : brand
		},
		success : function(data) {
			var obj = data;
			if (obj.success == true) {
				$.messager.alert('提示', '必选词【' + replaceStr + '】添加成功', 'info');
			} else {
				$.messager.alert('提示', data.msg, 'info');
			}
		}
	});
}
