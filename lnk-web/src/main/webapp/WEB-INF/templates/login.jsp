<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html class="no-js">
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
	<title>分布式管理平台</title>
	<meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
	<link rel="stylesheet" href="assets/bootstrap/css/bootstrap.min.css">
	<link rel="stylesheet" href="assets/font-awesome/4.4.0/css/font-awesome.min.css">
	<link rel="stylesheet" href="assets/ionicons/2.0.1/css/ionicons.min.css">
	<link rel="stylesheet" href="assets/dist/css/AdminLTE.min.css">
	<link rel="shortcut icon" href="assets/favicon.ico" type="image/x-icon">
	<style type="text/css">
	.logo {
	    margin: 0 auto;
	    background-image: url('assets/dist/img/logo.png');
	    width: 229px;
	    height: 60px;
	}
	</style>
</head>
    <body class="hold-transition login-page">
	<div class="login-box">
	  <div class="login-logo">
	    <p class="logo"></p>
	    <h1>分布式管理平台</h1>
	  </div>
	  <div class="login-box-body">
	      <div class="form-group has-feedback">
	        <input type="text" class="userName form-control" placeholder="用户名">
	        <span class="glyphicon glyphicon-envelope form-control-feedback"></span>
	      </div>
	      <div class="form-group has-feedback">
	        <input type="password" class="password form-control" placeholder="密码">
	        <span class="glyphicon glyphicon-lock form-control-feedback"></span>
	      </div>
	      <div class="row">
	        <div class="col-xs-4">
	          <button type="button" class="loginbtn btn btn-primary btn-block btn-flat">登录</button>
	        </div>
	      </div>
	  </div>
	</div>
	<footer id="footer" class="site-footer">
       <div style="margin-left: 530px">© Copyright 2015-2016 上海引旅金融信息服务有限公司</div>
	</footer>
	<script src="assets/plugins/jQuery/jQuery-2.1.4.min.js"></script>
	<script src="assets/bootstrap/js/bootstrap.min.js"></script>
	<script src="assets/js/index.js"></script>
</body>
</html>