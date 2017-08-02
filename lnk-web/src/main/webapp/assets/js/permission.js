$(function(){
        		$("#add").click(function(){
        			$("#userName").removeAttr("readonly")
        			$('#editPermissionForm')[0].reset();
        		})
                $("#editPermission").click(function(){
                	$('#editPermissionForm')[0].reset();
                	$("#userName").attr("readonly","readonly");
                    $('#userName').val($($(this).parent().siblings()[0]).html());
                    
                    var applyApp =$($(this).parent().siblings()[5]).val();
                    $("#applyApp").val(applyApp);
                    
                    var applyServiceConsumer = $($(this).parent().siblings()[6]).val();
                    $("#applyServiceConsumer").val(applyServiceConsumer);
                    
                    var appManagement = $($(this).parent().siblings()[7]).val();
                    $("#appManagement").val(appManagement);
                    
                    var transportSelectorManagement = $($(this).parent().siblings()[8]).val();
                    $("#transportSelectorManagement").val(transportSelectorManagement);
                    
                });
        		$('#editPermissionForm').validate({
        			debug:true,
            		rules :{
            			userName : {required : true},
            		},
            		messages : {
            			userName : {required : "请输入账户名"},
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
	                }
            	});
                $("#editPermissionBtn").click(function(){
                    if (!$("#editPermissionForm").valid()) {// 验证是否通过非空等验证
    					return;
    				}
                    $("#editPermissionDiv").modal('hide');
                    $.post("rpc/permission/operate",$('#editPermissionForm').serialize(),function(data){
      				  var nonpermission = data.nonpermission;
    				  if (nonpermission != null && nonpermission != "") {
    					  alert(nonpermission);
    					  return;
    				  }
                       if(data.success){
                            alert(data.message);
                            window.location.href="rpc/permission/list?random="+Math.random();
                        }else{
                            alert(data.message);
                        }
                     },"json");
                });
        	});