$(function(){
	var flag = "";
		$(".delete").click(function() {
			var id = $(this).attr("data-versionId");
			if (!confirm('确认删除')) {
				return;
			}
			$.ajax({
				type : "GET",
				url : "rpc/app/delete_app",
				timeout : 50000,
				dataType : "JSON",
				data : {
					id: id
				},
				success : function(data) {
				  var nonpermission = data.nonpermission;
				  if (nonpermission != null && nonpermission != "") {
					  alert(nonpermission);
					  return;
				  }
					if (data.success) {
						alert("删除成功!!!");
						window.location.reload();
					} else {
						alert("删除失败!!!");
					}
				},
				failure : function(data) {
					alert("删除失败!!!");
				}
			});
		});
		
		
		$("#addApplicationForm").validate({
			rules : {
				appName : "required",
				appDesc : "required",
				insNum : "required",
				callbackConcurrent : "required",
				callbackHandlerConcurrent : "required",
				asyncHandlerConcurrent : "required",
				nsHome : "required",
				contentType : "required",
				registry : "required"
			},
			messages : {
				appName : "请输入应用名称",
				appDesc : "请输入应用备注",
				insNum : "请输入应用实例个数",
				callbackConcurrent : "请输入回调并发数",
				callbackHandlerConcurrent : "请输入回调上下文处理并发数",
				asyncHandlerConcurrent : "请输入异步结果处理并发数",
				nsHome : "请输入配置路径",
				contentType : "请输入通知格式",
				registry : "请输入注册地址",
			},
			errorPlacement: function(error, element) {
                $(element).css("border-color","red");
                $(error).css("font-size","14px");
                $(element).before(error);
            },
            success: function(label) {
                $('[name='+$(label)[0].htmlFor+']').css("border-color","");
            },
            highlight: function(label){
            	$(label).css("border-color","red");
            },
		});
		$('#add').click(function(){
   		 	flag = "save";
   		 	$('#appName').val(""); 
   		 	$('#appDesc').val("");
   		 	$('#insNum').val("4");
   		 	$('#callbackConcurrent').val("30");
   		 	$('#callbackHandlerConcurrent').val("30");
   		 	$('#asyncHandlerConcurrent').val("30"); 
		 	$('#nsHome').val("${user.home}/.ns_config");
		 	$('#contentType').val("application/json-jackson-smile");
		 	$('#registry').val("zk.jr.17usoft.com:2181");
   	 	});
		$("#editApp").click(function(){
        	flag = "update";
        	$('#mid').val($($(this).parent().siblings()[0]).html());
            $('#appName').val($($(this).parent().siblings()[1]).html());
            $('#appDesc').val($($(this).parent().siblings()[2]).html());
   		 	$('#insNum').val($($(this).parent().siblings()[3]).html());
   		 	$('#callbackConcurrent').val($($(this).parent().siblings()[4]).html());
   		 	$('#callbackHandlerConcurrent').val($($(this).parent().siblings()[5]).html());
   		 	$('#asyncHandlerConcurrent').val($($(this).parent().siblings()[6]).html());
   		 	$('#nsHome').val($($(this).parent().siblings()[7]).html());
		 	$('#contentType').val($($(this).parent().siblings()[8]).html());
		 	$('#registry').val($($(this).parent().siblings()[9]).html());
        });
		$('#addApplicationBtn').click(function(){
			if (!$("#addApplicationForm").valid()) {// 验证是否通过非空等验证
				return;
			}
			$.post("rpc/app/"+flag,$('#addApplicationForm').serialize(),function(data){
				  var nonpermission = data.nonpermission;
				  if (nonpermission != null && nonpermission != "") {
					  alert(nonpermission);
					  return;
				  }
            	if(data.success){
                	alert("操作成功");
                 	window.location.href="rpc/app/query?random="+Math.random();
             	}else{
                 	alert("操作失败");
             	}
          	},"json");
		});   
	})