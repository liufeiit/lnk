$(function(){
		var id = 0;
		var addId = 0;
		var delId = 0;
		$('#addServiceGroupButton').click(function(){
			var html = 
			'<div class="row" style="margin-top:10px;margin-bottom:20px;" id="'+addId+'">'+
				'<div class="col-md-5">'+
					'<div class="input-group">'+
						'<span class="input-group-addon">应用服务组: 名称</span>'+
						'<input type="text" id="addServiceGroupName'+addId+'" name="addServiceGroupName" class="form-control" placeholder="应用服务组: 名称"  maxlength="88">'+
					'</div>'+
				'</div>'+
				'<div class="col-md-5">'+
					'<div class="input-group">'+
						'<span class="input-group-addon">应用服务组: 备注</span>'+
						'<input type="text" id="addServiceGroupDesc'+addId+'" name="addServiceGroupDesc" class="form-control" placeholder="应用服务: 备注"  maxlength="8">'+
					'</div>'+
				'</div>'+
			'</div>'
			id = addId;
			delId = id;
		$('#addArea').prepend(html);
			addId = addId+1;
		});
		$('#delServiceGroupButton').click(function(){
			$("#"+delId).remove();
			delId = delId -1;
		});
		
		$("#informationForm").validate({
			rules : {
				serviceGroupName : "required",
				serviceGroupDesc : "required",
				addServiceGroupName : "required",
				addServiceGroupDesc : "required"
			},
			messages : {
				serviceGroupName : "请输入应用服务组: 名称",
				serviceGroupDesc : "请输入应用服务组: 备注",
				addServiceGroupName : "请输入应用服务组: 名称",
				addServiceGroupDesc : "请输入应用服务组: 备注"
			},
			errorPlacement: function(error, element) {
                $(element).css("border-color","red");
                $(error).css("font-size","14px");
                $(error).css("font-color","red");
                $(element.parent()).before(error);
            },
            success: function(span) {
                $('[name='+$(span)[0].htmlFor+']').css("border-color","");
            },
            highlight: function(span){
            	$(span).css("border-color","red");
            }
		});
		
		
		$('#frm_submit').click(function(){
			if (!$("#groupForm").valid()) {// 验证是否通过非空等验证
				return;
			}
			for(var i = 0 ; i <= delId ; i++){
				var addServiceGroupName = $("#addServiceGroupName"+i).val();
				var addServiceGroupDesc = $("#addServiceGroupDesc"+i).val();
				if(addServiceGroupDesc == "" || addServiceGroupDesc == ""){
					alert("填写新增group");
					return;
				}
			}
			$.post("rpc/app/update_group",$('#groupForm').serialize(),function(data){
				  var nonpermission = data.nonpermission;
				  if (nonpermission != null && nonpermission != "") {
					  alert(nonpermission);
					  return;
				  }
            	if(data.success){
                	alert("更新成功");
                 	window.location.href="rpc/app/query?random="+Math.random();
             	}else{
                 	alert("更新失败");
                 	location.reload();
             	}
          	},"json");
		});   
		
		$(".delete").click(function() {
			var id = $(this).attr("data-versionId");
			if (!confirm('确认删除')) {
				return;
			}
			$.ajax({
				type : "GET",
				url : "rpc/app/delete_group",
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
	});