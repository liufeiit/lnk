/**
 * 刘飞
 */
$(document).ready(function() {
	$(".select2").select2();
	$(".J_app_group_protocols").click(function() {
		var systemId = $(".systemId-value").val();
		if (systemId == null || systemId == "") {
			alert("请输入应用名称");
			return;
		}
		var serviceGroup = $(".service-group").val();
		if (serviceGroup == null || serviceGroup == "") {
			alert("请选择应用服务组");
			return;
		}
		$.ajax({
			  type: 'POST',
			  url: "rpc/selector/group-getting",
			  data: {
				  systemId : systemId,
				  serviceGroup : serviceGroup
			  },
			  beforeSend : function() {},
			  success: function(data, textStatus, jqXHR) {
				  var systemId = data.systemId;
				  var serviceGroup = data.serviceGroup;
				  var transportProtocol = data.transportProtocol;
				  $(".exchange").attr("data-systemId", systemId);
				  $(".exchange").attr("data-group", serviceGroup);
				  $(".title").html("应用" + systemId + ", 组" + serviceGroup + "当前使用协议为 : " + transportProtocol);
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
	$(".J_app_group_add").click(function() {
		var systemId = null;
        var serviceGroup = null;
        var protocol = null;
		if (systemId == null || systemId == "") {
			systemId = prompt("请输入应用名称", "");
		}
		if (serviceGroup == null || serviceGroup == "") {
			serviceGroup = prompt("请输入该应用组名称", "");
		}
		if (protocol == null || protocol == "") {
			protocol = prompt("请输入该应用服务组协议", "AMQP/WSOCK");
		}
		if ((systemId == null || systemId == "") || (serviceGroup == null || serviceGroup == "") || (protocol == null || protocol == "")) {
			alert("未输入数据！");
			return;
		}
		$.ajax({
			  type: 'POST',
			  url: "rpc/selector/group-add",
			  data: {
				  systemId : systemId,
				  serviceGroup : serviceGroup,
				  protocol : protocol
			  },
			  beforeSend : function() {},
			  success: function(data, textStatus, jqXHR) {
				  var nonpermission = data.nonpermission;
				  if (nonpermission != null && nonpermission != "") {
					  alert(nonpermission);
					  return;
				  }
				  alert("添加成功！");
				  var systemId = data.systemId;
				  $(".exchange").attr("data-systemId", systemId);
				  $(".service-group").attr("data-systemId", systemId);
				  $(".systemId-value").val(systemId);
				  var groupList = data.groupList;
				  var optHtml = "";
				  var selected = '';
				  $(".service-group").val(groupList[0]);
				  for (var i = 0; i < groupList.length; i++) {
					  if (i == 0) {
						  selected = 'selected="selected"';
					  } else {
						  selected = '';
					  }
					  optHtml += ('<option value="' + groupList[i] + '"' + selected + '>' + groupList[i] + '</option>');
					  
				  }
				  $(".service-group").html(optHtml);
			  },
			  dataType: "json"
		});
	});
	$(".J_app_group_clear").click(function() {
		var systemId = $(".systemId-value").val();
		if (systemId == null || systemId == "") {
			alert("请输入应用名称");
			return;
		}
		var serviceGroup = $(".service-group").val();
		if (serviceGroup == null || serviceGroup == "") {
			alert("请选择应用服务组");
			return;
		}
		if (!confirm("确定要将应用" + systemId + ", 组" + serviceGroup + "删除吗?")) {
			return;
		}
		$.ajax({
			  type: 'POST',
			  url: "rpc/selector/group-rm",
			  data: {
				  systemId : systemId,
				  serviceGroup : serviceGroup
			  },
			  beforeSend : function() {},
			  success: function(data, textStatus, jqXHR) {
				  var nonpermission = data.nonpermission;
				  if (nonpermission != null && nonpermission != "") {
					  alert(nonpermission);
					  return;
				  }
				  alert("删除成功！");
				  var systemId = data.systemId;
				  $(".exchange").attr("data-systemId", systemId);
				  $(".service-group").attr("data-systemId", systemId);
				  var groupList = data.groupList;
				  var optHtml = "";
				  var selected = '';
				  $(".service-group").val(groupList[0]);
				  for (var i = 0; i < groupList.length; i++) {
					  if (i == 0) {
						  selected = 'selected="selected"';
					  } else {
						  selected = '';
					  }
					  optHtml += ('<option value="' + groupList[i] + '"' + selected + '>' + groupList[i] + '</option>');
					  
				  }
				  $(".service-group").html(optHtml);
			  },
			  dataType: "json"
		});
	});
	$(".search-btn").click(function() {
		var systemId = $(".systemId-value").val();
		if (systemId == null || systemId == "") {
			alert("请输入应用名称");
			return;
		}
		$.ajax({
			  type: 'POST',
			  url: "rpc/selector/group-list",
			  data: {
				  systemId : systemId
			  },
			  beforeSend : function() {},
			  success: function(data, textStatus, jqXHR) {
				  var systemId = data.systemId;
				  $(".exchange").attr("data-systemId", systemId);
				  $(".service-group").attr("data-systemId", systemId);
				  var groupList = data.groupList;
				  var optHtml = "";
				  var selected = '';
				  $(".service-group").val(groupList[0]);
				  for (var i = 0; i < groupList.length; i++) {
					  if (i == 0) {
						  selected = 'selected="selected"';
					  } else {
						  selected = '';
					  }
					  optHtml += ('<option value="' + groupList[i] + '"' + selected + '>' + groupList[i] + '</option>');
					  
				  }
				  $(".service-group").html(optHtml);
			  },
			  dataType: "json"
		});
	});
	$(".service-group").on("change", function() {
		var selectedGroup = $(".service-group").val();
		var systemId = $(".service-group").attr("data-systemId");
		$.ajax({
			  type: 'POST',
			  url: "rpc/selector/group-getting",
			  data: {
				  systemId : systemId,
				  serviceGroup : selectedGroup
			  },
			  beforeSend : function() {},
			  success: function(data, textStatus, jqXHR) {
				  var systemId = data.systemId;
				  var serviceGroup = data.serviceGroup;
				  var transportProtocol = data.transportProtocol;
				  $(".exchange").attr("data-systemId", systemId);
				  $(".exchange").attr("data-group", serviceGroup);
				  $(".title").html("应用" + systemId + ", 组" + serviceGroup + "当前使用协议为 : " + transportProtocol);
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
	$(".exchange").on("change", function() {
		var protocol = $(".exchange").val();
		var systemId = $(".exchange").attr("data-systemId");
		var serviceGroup = $(".exchange").attr("data-group");
		if (!confirm("确定要将应用" + systemId + ", 组" + serviceGroup + "传输协议切换为[" + protocol + "]吗?")) {
			return;
		}
		$.ajax({
			  type: 'POST',
			  url: "rpc/selector/group-setting",
			  data: {
				  systemId : systemId,
				  serviceGroup : serviceGroup,
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
				  var serviceGroup = data.serviceGroup;
				  var transportProtocol = data.transportProtocol;
				  $(".title").html("应用" + systemId + ", 组" + serviceGroup + "当前使用协议为 : " + transportProtocol);
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

