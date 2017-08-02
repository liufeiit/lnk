/**
 * 刘飞
 */

function syntaxHighlight(json) {
	if (typeof json != 'string') {
		json = JSON.stringify(json, undefined, 2);
	}
	json = json.replace(/&/g, '&').replace(/</g, '<').replace(/>/g, '>');
	return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g,
					function(match) {
						var cls = 'number';
						if (/^"/.test(match)) {
							if (/:$/.test(match)) {
								cls = 'key';
							} else {
								cls = 'string';
							}
						} else if (/true|false/.test(match)) {
							cls = 'boolean';
						} else if (/null/.test(match)) {
							cls = 'null';
						}
						return '<span class="' + cls + '">' + match + '</span>';
					});
}

$(document).ready(function() {
	$(".J_app").click(function() {
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
	
	$(".J_app_clear").click(function() {
		if (window.localStorage) {
			window.localStorage.setItem("management_host", "");
			window.localStorage.setItem("management_app", "");
			window.localStorage.setItem("management_ins", "");
			alert("清理成功！");
            return;
        }
		alert("部分功能无法支持，请更换浏览器！");
	});
	
	$(".J_app_show").click(function() {
		if (window.localStorage) {
	        var J_host = window.localStorage.getItem('management_host');
	        var J_appName = window.localStorage.getItem('management_app');
	        var J_ins = window.localStorage.getItem('management_ins');
	        alert(J_host + ":" + J_appName + "#" + J_ins);
            return;
        }
		alert("部分功能无法支持，请更换浏览器！");
	});
	
	$(".J_management").click(function() {
        var J_host = window.localStorage.getItem('management_host');
        var J_appName = window.localStorage.getItem('management_app');
        var J_ins = window.localStorage.getItem('management_ins');
        var i = 0;
		while ((J_host == null || J_host == "") && i < 3) {
			J_host = prompt("请输入应用节点所在IP地址", "");
			if (J_host != null && J_host != "") {
				window.localStorage.setItem("management_host", J_host);
				break;
			}
			i++;
		}
		i = 0;
		while ((J_appName == null || J_appName == "") && i < 3) {
			J_appName = prompt("请输入该应用名称", "");
			if (J_appName != null && J_appName != "") {
				window.localStorage.setItem("management_app", J_appName);
				break;
			}
			i++;
		}
		i = 0;
		while ((J_ins == null || J_ins == "") && i < 3) {
			J_ins = prompt("请输入该应用实例在该机器的节点数", "");
			if (J_ins != null && J_ins != "") {
				window.localStorage.setItem("management_ins", J_ins);
				break;
			}
			i++;
		}
		if ((J_host == null || J_host == "") || (J_appName == null || J_appName == "") || (J_ins == null || J_ins == "")) {
			alert("未设置应用节点数据！");
			return;
		}
		var J_tip = $(this).attr("data-tip");
		if (J_tip != null && J_tip != "") {
			if (!confirm(J_tip)) {
				return;
			}
		}
		var J_ref = $(this).attr("data-ref");
		var J_URI = "rpc/management/" + J_ref;
		$.ajax({
			  type: 'POST',
			  url: J_URI,
			  data: {
				  ip : J_host,
				  systemId : J_appName,
				  instance : J_ins
			  },
			  beforeSend : function() {
				  $(".J_M_data").html('<div class="overlay"><i class="fa fa-refresh fa-spin"></i></div>');
			  },
			  success: function(data, textStatus, jqXHR) {
				  var nonpermission = data.nonpermission;
				  if (nonpermission != null && nonpermission != "") {
					  $(".J_M_data").html('');
					  alert(nonpermission);
					  return;
				  }
				  $(".J_M_data").html('');
				  var ip = data.ip;
				  var systemId = data.systemId;
				  var instance = data.instance;
				  $(".J_app_node").html(ip + ":" + systemId + "#" + instance);
				  var J_data = data.data;
				  if (J_ref == "os") {
					  $(".J_M_data").html(syntaxHighlight(JSON.stringify(J_data)));
					  return;
				  }
				  if (J_ref == "module-status") {
					  $(".J_M_data").html(syntaxHighlight(J_data));
					  return;
				  }
				  if (J_ref == "dump") {
					  $(".J_M_data").html(J_data);
					  return;
				  }
				  if (J_ref == "kill") {
					  $(".J_M_data").html("执行结果：" + J_data);
					  return;
				  }
				  var command = J_data.command;
				  var cmd = "";
				  for (var i = 0; i < command.length; i++) {
					  cmd += (command[i] + " ");
				  }
				  var exitValue = J_data.exitValue;
				  var err = J_data.err;
				  var out = J_data.out;
				  var html = "";
				  html += ("<div>执行指令：" + cmd + "</div>");
				  html += ("<div>指令执行结果：" + (exitValue == 0 ? "成功" : "失败") + ", exitValue = " + exitValue + "</div>");
				  if (err != null && err != "") {
					  html += ("<div>执行失败结果输出：" + err + "</div>");
				  }
				  if (out != null && out != "") {
					  html += ("<div>执行成功结果输出：" + out + "</div>");
				  }
				  $(".J_M_data").html(html);
			  },
			  dataType: "json"
		});
	});
	
});