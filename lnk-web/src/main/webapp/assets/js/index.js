/**
 * 刘飞
 */
$(document).ready(function() {
	$(".loginbtn").click(function() {
		var userName = $(".userName").val();
		if (userName == null || userName == "") {
			alert("请输入账号");
			return;
		}
		var password = $(".password").val();
		if (password == null || password == "") {
			alert("请输入密码");
			return;
		}
		$.ajax({
			  type: 'POST',
			  url: "login.cgi",
			  data: {
				  userName : userName,
				  password : password
			  },
			  beforeSend : function() {},
			  success: function(data, textStatus, jqXHR) {
				  var status = data.status;
				  var message = data.message;
				  if (status) {
					  window.location.href = "index"
					  return;
				  }
				  $(".userName").val("");
				  $(".password").val("");
				  alert(message);
			  },
			  dataType: "json"
		});
	});
	
});


