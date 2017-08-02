<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<header class="main-header">
   <script type="text/javascript">
   function logout() {
   		var loginCookie;
   		var arr, reg = new RegExp("(^| )__JRPCSESSIONID=([^;]*)(;|$)");
   		if(arr = document.cookie.match(reg)) {
   			loginCookie = unescape(arr[2]);
   		}
   		if (loginCookie != null) {
   			var exp = new Date();
   			exp.setTime(exp.getTime() - 1);
   			document.cookie = "__JRPCSESSIONID=" + loginCookie + ";expires=" + exp.toGMTString();
   		    alert("delete JID " + document.cookie);
   		}
   	}
   </script>
   <a href="#" class="logo">
     <span class="logo-mini"><b>R</b>PC</span>
     <span class="logo-lg"><b>RPC</b>分布式平台</span>
   </a>
   <nav class="navbar navbar-static-top" role="navigation">
     <a href="#" class="sidebar-toggle" data-toggle="offcanvas" role="button">
       <span class="sr-only">切换</span>
     </a>
     <div class="navbar-custom-menu">
       <ul class="nav navbar-nav">
         <li class="dropdown user user-menu">
           <a href="#" class="dropdown-toggle" data-toggle="dropdown">
             <img src="assets/dist/img/user2-160x160.jpg" class="user-image" alt="大飞哥儿">
             <span class="hidden-xs">大飞哥儿</span>
           </a>
           <ul class="dropdown-menu">
             <li class="user-header">
               <img src="assets/dist/img/user2-160x160.jpg" class="img-circle" alt="大飞哥儿">
               <p>
                 大飞哥儿
                 <small>Member since Nov. 2017</small>
               </p>
             </li>
             <li class="user-footer">
               <div class="pull-right">
                 <a href="logout.cgi" class="btn btn-default btn-flat">退出</a>
               </div>
             </li>
           </ul>
         </li>
         <!-- 
         <li>
           <a href="#" data-toggle="control-sidebar"><i class="fa fa-gears"></i></a>
         </li>
          -->
       </ul>
     </div>
   </nav>
 </header>