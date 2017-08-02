$(function(){
    	$(".delete").click(function() {
			var systemId = $(this).attr("data-versionId");
			var group = $(this).attr("data-group");
			if (!confirm('确认删除')) {
				return;
			}
			$.ajax({
				type : "GET",
				url : "rpc/group_consumer/delete",
				timeout : 50000,
				dataType : "JSON",
				data : {
					systemId: systemId,
					group: group
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
    	
    		$("#queryForm").validate({
    			rules : {
    				group : "required"
    			},
    			messages : {
    				group : "请输入服务组"
    			},
    			errorPlacement : function(error, element) {
    				$(element).css("border-color", "red");
    				$(error).css("font-size", "14px");
    				$(element).parent().after(error);
    			},
    		});
    		
    		$("#addConsumeForm").validate({
    			rules : {
    				group : "required",
    				systemId : "required",
    			},
    			messages : {
    				appName : "请输入服务组",
    				appDesc : "请输入消费组",
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
    		
    		$('#addConsumerBtn').click(function(){
    			if (!$("#addConsumeForm").valid()) {// 验证是否通过非空等验证
    				return;
    			}
    			$.post("rpc/group_consumer/save",$('#addConsumeForm').serialize(),function(data){
				  var nonpermission = data.nonpermission;
				  if (nonpermission != null && nonpermission != "") {
					  alert(nonpermission);
					  return;
				  }
                	if(data.success){
                    	alert("操作成功");
                     	window.location.href="rpc/group_consumer/query?random="+Math.random();
                 	}else{
                     	alert("操作失败");
                 	}
              	},"json");
    		});   
    })