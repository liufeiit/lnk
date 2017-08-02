/**
 * 刘飞
 */
$(document).ready(function() {
	$(".search-btn").click(function() {
		var serviceId = $(".search-value").val();
		if (serviceId == null || serviceId == "") {
			alert("请输入服务ID");
			return;
		}
		$.ajax({
			  type: 'POST',
			  url: "lnk/registry/servers/list",
			  data: {
				  serviceId : serviceId
			  },
			  beforeSend : function() {
				  $(".J_addr_list").html('<div class="overlay"><i class="fa fa-refresh fa-spin"></i></div>');
			  },
			  success: function(data, textStatus, jqXHR) {
				  $(".J_addr_list").html('');
				  // {"2.0.0/0":["10.10.35.8:10039"],"1.0.0/0":["10.10.35.8:10039"]}
				  // alert(JSON.stringify(data));
				  // console.log(JSON.stringify(data));
				  var html = '';
				  for(var key in data) {
					  var vp = key.split("/");
					  var version = vp[0];
					  var protocol = vp[1];
					  var vpHtml = "&nbsp;&nbsp;服务版本号:" + version + "&nbsp;&nbsp;协议编号:" + protocol;
					  html += ('<div class="box"><div class="box-header"><h3 class="box-title"><i class="fa fa-bars"></i>' + vpHtml + '</h3></div><div class="box-body table-responsive no-padding">');
					  var servers = data[key];
					  for(var i = 0; i < servers.length; i++) {
						  var server = servers[i];
						  var optDataHtml = 'data-service-id="' + serviceId + '" data-version="' + version + '" data-protocol="' + protocol + '" data-server="' + server + '"';
						  var optHtml = '<div class="btn-group">' + 
						  	'<button type="button" class="btn btn-default" ' + optDataHtml + '>下线</button>' + 
						  	'<button type="button" class="btn btn-default" ' + optDataHtml + '>降级</button>' + 
						  	'</div>';
						  var serverHtml = '<table class="table table-hover"><tr><th>tcp://' + server + '</th><th>' + optHtml + '</th></tr></table>';
						  html += (serverHtml);
					  }
					  html += ('</div></div>');
				  }
				  $(".J_addr_list").html(html);
				  
			  },
			  dataType: "json"
		});
	});
});

