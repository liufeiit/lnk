<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
	<%
	   String path = request.getContextPath();
	   String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
	%>
	<base href="<%=basePath%>"/>
	<meta charset="UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
	<meta name="keywords" content="" />
	<meta name="description" content="" />
	<link rel="stylesheet" href="assets/bootstrap/css/bootstrap.min.css">
	<link rel="stylesheet" href="assets/font-awesome/4.4.0/css/font-awesome.min.css">
	<link rel="stylesheet" href="assets/ionicons/2.0.1/css/ionicons.min.css">
	<link rel="stylesheet" href="assets/dist/css/AdminLTE.min.css">
	<link rel="stylesheet" href="assets/dist/css/skins/_all-skins.min.css">
	<link rel="stylesheet" href="assets/plugins/iCheck/flat/blue.css">
	<link rel="stylesheet" href="assets/plugins/morris/morris.css">
	<link rel="stylesheet" href="assets/plugins/jvectormap/jquery-jvectormap-1.2.2.css">
	<link rel="stylesheet" href="assets/plugins/datepicker/datepicker3.css">
	<link rel="stylesheet" href="assets/plugins/daterangepicker/daterangepicker-bs3.css">
	<link rel="stylesheet" href="assets/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
  	<link rel="stylesheet" href="assets/plugins/select2/select2.min.css">
	<!--[if lt IE 9]>
	<script src="assets/html5shiv/3.7.3/html5shiv.min.js"></script>
	<script src="assets/respond/1.4.2/respond.min.js"></script>
	<![endif]-->
	<title>分布式平台-<sitemesh:write property="title"/></title>
	<link rel="shortcut icon" href="assets/favicon.ico" type="image/x-icon">
	
	<script src="assets/plugins/jQuery/jQuery-2.1.4.min.js"></script>
	<script src="assets/plugins/jQueryUI/jquery-ui.min.js"></script>
	<script>
	 $.widget.bridge('uibutton', $.ui.button);
	</script>
	<script src="assets/bootstrap/js/bootstrap.min.js"></script>
	<script src="assets/plugins/select2/select2.full.min.js"></script>
	<script src="assets/ajax/libs/raphael/2.1.0/raphael-min.js"></script>
	<script src="assets/plugins/morris/morris.min.js"></script>
	<script src="assets/plugins/sparkline/jquery.sparkline.min.js"></script>
	<script src="assets/plugins/jvectormap/jquery-jvectormap-1.2.2.min.js"></script>
	<script src="assets/plugins/jvectormap/jquery-jvectormap-world-mill-en.js"></script>
	<script src="assets/plugins/knob/jquery.knob.js"></script>
	<script src="assets/ajax/libs/moment.js/2.10.2/moment.min.js"></script>
	<script src="assets/plugins/daterangepicker/daterangepicker.js"></script>
	<script src="assets/plugins/datepicker/bootstrap-datepicker.js"></script>
	<script src="assets/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.all.min.js"></script>
	<script src="assets/plugins/slimScroll/jquery.slimscroll.min.js"></script>
	<script src="assets/plugins/fastclick/fastclick.js"></script>
	<script src="assets/dist/js/app.min.js"></script>
	<script src="assets/dist/js/pages/dashboard.js"></script>
	<script src="assets/dist/js/demo.js"></script>
	<script src="assets/jquery.validate.min.js"></script>
	<!-- <script src="assets/commonMessages.js"></script> -->
	
	<sitemesh:write property="head"/>
</head>
<body class="hold-transition skin-purple fixed sidebar-mini" >
	<div class="wrapper">
		<jsp:include page="../control/header.jsp"/>
		<jsp:include page="../control/sidebar.jsp"/>
		<sitemesh:write property="body"/>
		<!-- <jsp:include page="../control/rights.jsp"/> -->
		<jsp:include page="../control/control-sidebar.jsp"/>
		<div class="control-sidebar-bg"></div>
	</div>
</body>
<script src="assets/js/menu_storage.js"></script>
</html>