require([ "../require-config" ], function() {
	require([ 'jquery', 'validate' ], function() {
		$.extend($.validator.messages, {
			required: "这是必填字段",
			remote: "请修正此字段",
			email: "请输入有效的电子邮件地址",
			url: "请输入有效的网址",
			date: "请输入有效的日期",
			dateISO: "请输入有效的日期 (YYYY-MM-DD)",
			number: "请输入有效的数字",
			digits: "只能输入数字",
			creditcard: "请输入有效的信用卡号码",
			equalTo: "你的输入不相同",
			extension: "请输入有效的后缀",
			maxlength: $.validator.format("最多可以输入 {0} 个字符"),
			minlength: $.validator.format("最少要输入 {0} 个字符"),
			rangelength: $.validator.format("请输入长度在 {0} 到 {1} 之间的字符串"),
			range: $.validator.format("请输入范围在 {0} 到 {1} 之间的数值"),
			max: $.validator.format("请输入不大于 {0} 的数值"),
			min: $.validator.format("请输入不小于 {0} 的数值")
		});
		
		//添加检验方法
		$.validator.addMethod("isReadonly", function(value, element) {   
			if($(element).attr('readonly')==undefined){
				$(element).rules("add",{digits:true,required:true,maxlength:10});
				return !value=="";
			}else{
				return true;
			}
        }, "");
		$.validator.addMethod("isReadonlyStr", function(value, element) {
			if($(element).attr('readonly')==undefined){
				$(element).rules("add",{required:true,maxlength:10});
				return !value=="";
			}else{
				return true;
			}
		}, "");
		//只能输入－1和正整数
		$.validator.addMethod("isdigits", function(value, element) {
			$(element).rules("remove","digits maxlength");
			if(Number(value)==-1){
				return true;
			}
			if(Number(value)<-1){
				return false;
			}
			$(element).rules("add",{digits:true,maxlength:9});
			return true;
        }, "只能输入－1和正整数");
		//验证数字大小，小于等于传入的参数。该验证提示需要根据情况自己写
		$.validator.addMethod("eqNumber", function(value, element,param) {
			if(isNaN(Number(value))) {
				return true;
			}
			return Number(value) <= Number($('#'+param).val());
        }, "");
		
		$.validator.addMethod("more", function(value, element, param) {
		    return Number(value) > param;
		}, $.validator.format("必须大于{0}"));
	});
});
