/**
 * 刘飞
 */
$(document).ready(function() {
	$(".select2").select2();
	$(".search-btn").click(function() {
		var systemId = $(".systemId-value").val();
		if (systemId == null || systemId == "") {
			alert("请输入应用名称");
			return;
		}
		$.ajax({
			  type: 'POST',
			  url: "rpc/selector/getting",
			  data: {
				  systemId : systemId
			  },
			  beforeSend : function() {},
			  success: function(data,textStatus,jqXHR) {
				  var systemId = data.systemId;
				  var transportProtocol = data.transportProtocol;
				  $(".exchange").attr("data-systemId", systemId);
				  $(".title").html("应用" + systemId + "当前使用协议为 : " + transportProtocol);
				  if (transportProtocol == "AMQP") {
					  var optHtml = '<option value="AMQP" selected="selected">AMQP</option><option value="WSOCK">WSOCK</option>';
					  $(".exchange").html(optHtml);
				  }
				  if (transportProtocol == "WSOCK") {
					  var optHtml = '<option value="WSOCK" selected="selected">WSOCK</option><option value="AMQP">AMQP</option>';
					  $(".exchange").html(optHtml);
				  }
			  },
			  dataType: "json"
		});
	});
	$(".exchange").on("change", function(){
		var protocol = $(".exchange").val();
		var systemId = $(".exchange").attr("data-systemId");
		if (!confirm("确定要将应用" + systemId + "传输协议切换为[" + protocol + "]吗?")) {
			return;
		}
		$.ajax({
			  type: 'POST',
			  url: "rpc/selector/setting",
			  data: {
				  systemId : systemId,
				  protocol : protocol
			  },
			  beforeSend : function() {},
			  success: function(data, textStatus, jqXHR) {
				  var nonpermission = data.nonpermission;
				  if (nonpermission != null && nonpermission != "") {
					  alert(nonpermission);
					  return;
				  }
				  var systemId = data.systemId;
				  var transportProtocol = data.transportProtocol;
				  $(".title").html("应用" + systemId + "当前使用协议为 : " + transportProtocol);
				  if (transportProtocol == "AMQP") {
					  var optHtml = '<option value="AMQP" selected="selected">AMQP</option><option value="WSOCK">WSOCK</option>';
					  $(".exchange").html(optHtml);
				  }
				  if (transportProtocol == "WSOCK") {
					  var optHtml = '<option value="WSOCK" selected="selected">WSOCK</option><option value="AMQP">AMQP</option>';
					  $(".exchange").html(optHtml);
				  }
			  },
			  dataType: "json"
		});
	});
});

