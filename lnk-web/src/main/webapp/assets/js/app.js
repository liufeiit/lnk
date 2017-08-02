/**
 * 刘飞
 */
$(document).ready(function() {
	$(".search-btn").click(function() {
		var appName = $(".search-value").val();
		if (appName == null || appName == "") {
			alert("请输入应用名称");
			return;
		}
		$.ajax({
			  type: 'POST',
			  url: "rpc/app/search/apps",
			  data: {
				  appName : appName
			  },
			  beforeSend : function() {
				  $(".apps-list").slideUp();
			  },
			  success: function(data, textStatus, jqXHR) {
				  // console.log(textStatus + ", " + JSON.stringify(jqXHR));
				  alert("查询到" + data.length + "个节点");
				  var appsHtml = "";
				  for(var i = 0; i < data.length; i++) {
					  var app = data[i];
					  appsHtml += ('<div class="col-lg-3 col-xs-6">' + 
					  				'<div class="small-box bg-green">' + 
					  					'<div class="inner">' + 
						  					'<h3><sup style="font-size: 14px">' + app.appName + '</sup></h3>' + 
						  					'<p>主机:' + app.host + '</p>' + 
						  					'<p>版本:' + app.version + '</p>' + 
					  					'</div>' + 
					  					'<div class="icon">' + 
					  						'<i class="ion">' + app.instance + '</i>' + 
					  					'</div>' + 
					  					'<a href="rpc/management" data-app="' + app.appName + '" data-host="' + app.host + '" data-ins="' + app.instance + '" class="J_app app-status small-box-footer">节点<i class="fa fa-arrow-circle-right"></i></a>' + 
					  				'</div>' + 
					  			 '</div>');
				  }
				  $(".apps-list").html(appsHtml);
				  $(".apps-list").slideDown(1500);
				  
				  $(".app-status").bind("click", function() {
						if (window.localStorage) {
							var J_this = $(this);
							var J_host = J_this.attr("data-host");
							var J_appName = J_this.attr("data-app");
							var J_ins = J_this.attr("data-ins");
							window.localStorage.setItem("management_host", J_host);
							window.localStorage.setItem("management_app", J_appName);
							window.localStorage.setItem("management_ins", J_ins);
				            return;
				        }
						alert("部分功能无法支持，请更换浏览器！");
					});
				  
			  },
			  dataType: "json"
		});
	});
	
});


