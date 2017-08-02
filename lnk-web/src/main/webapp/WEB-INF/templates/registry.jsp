<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
	<title>注册中心</title>
</head>
<body class="hold-transition skin-blue fixed sidebar-mini">
  <div class="content-wrapper">
    <section class="content-header">
      <h1>注册中心<small>服务列表</small></h1>
      <ol class="breadcrumb">
        <li><a href="#"><i class="fa fa-dashboard"></i> 首页</a></li>
        <li class="active">服务列表</li>
      </ol>
    </section>
    <section class="content">
      <div class="row" style="padding-left: 150px;padding-right: 30px;padding-top: 20px;padding-bottom: 20px;">
        <div class="col-lg-7 col-xs-6">
        	<div class="input-group margin">
                <input type="text" class="form-control search-value" placeholder="输入服务ID">
                    <span class="input-group-btn">
                      <button type="button" class="btn btn-info btn-flat search-btn"><i class="fa fa-search"></i></button>
                    </span>
              </div>
        </div>
      </div>
      <div class="row" style="padding-left: 30px;padding-right: 30px;padding-top: 20px;padding-bottom: 20px;">
      	<div class="col-md-13">
          <div class="box box-primary">
            <div class="box-header">
              <h3 class="box-title"><i class="fa fa-th-list"></i>&nbsp;&nbsp;服务列表</h3>
            </div>
            <div class="box-body J_addr_list"></div>
          </div>
        </div>
      </div>
    </section>
  </div>
  <script src="assets/js/registry.js"></script>
</body>
</html>