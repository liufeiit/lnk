$(function(){
		$("#configForm").validate({
			rules : {
				callbackConcurrent : "required",
				callbackHandlerConcurrent : "required",
				asyncHandlerConcurrent : "required",
				nsHome : "required",
				protocolContentType : "required",
				registry : "required",
			},
			messages : {
				callbackConcurrent : "请输入回调并发数",
				callbackHandlerConcurrent : "请输入回调上下文处理并发数",
				asyncHandlerConcurrent : "请输入异步结果处理并发数",
				nsHome : "请输入配置路径",
				protocolContentType : "请输入通知格式",
				registry : "请输入注册地址",
			},
			errorPlacement: function(error, element) {
                $(element).css("border-color","red");
                $(error).css("font-size","14px");
                $(error).css("font-color","red");
                $(element.parent()).before(error);
            },
            success: function(label) {
                $('[name='+$(label)[0].htmlFor+']').css("border-color","");
            },
            highlight: function(label){
            	$(label).css("border-color","red");
            }
		});
		$('#frm_submit').click(function(){
			if (!$("#configForm").valid()) {// 验证是否通过非空等验证
				return;
			}
			$.post("rpc/app/update_config",$('#configForm').serialize(),function(data){
            	if(data.success){
                	alert("更新成功");
                 	window.location.href="rpc/app/query?random="+Math.random();
             	}else{
                 	alert("更新失败");
             	}
          	},"json");
		});   
	});