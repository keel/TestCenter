<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.k99k.khunter.*,com.k99k.tools.*" session="false" %>
<%
String sPrefix = KFilter.getStaticPrefix();
String prefix = KFilter.getPrefix();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>游戏测试-登录</title>
<link rel="stylesheet" type="text/css" href="<%=sPrefix %>/css/style.css" />
<script src="<%=sPrefix %>/js/jquery.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.validate.min.js" type="text/javascript"></script>
<script type="text/javascript">
$(function(){
$("#saveLogin").click(function(){
	$("#saveLogin").val(($(":checked").val() == "false")?"true":"false");
});
//处理请求
$.validator.dealAjax = {
	bt:$("#submitBT"),
	ok:function(data){
		if(data=="ok"){window.location = ("<%=prefix %>/news");};
	},
	err:function(){
		alert('登录失败!用户名和密码未通过验证.');
	}
};

//开始验证
$('#consoleLogin').validate({
    /* 设置验证规则 */
    rules: {
        uName: {
            required:true,
            rangelength:[2,15]
        },
        uPwd:{
            required:true,
            pwdCheck:true,
            rangelength:[6,30]
        }
    }
});

});
</script>
</head>
<body>

<div style=" position: relative;width: 380px;margin: 0 auto;padding:150px 0 0 0;">
	<div id="loginTitle" style="width:380px;font-size:120%;padding:10px 0;text-align: center;">
		Test Center
	</div>
<form action="<%=prefix %>/auth/login" method="post" id="consoleLogin">
	<p><label for="uName">用户名:</label><br />
	<input type="text" name="uName" id="uName" style="width:300px;" value="" /></p>
	<p><label for="form_pwd">密 &nbsp;&nbsp;码:</label><br />
	<input type="password" name="uPwd" id="uPwd" style="width:300px;" value="" /></p>
	<p style="float:left;padding-top:10px;"><input type="checkbox" name="saveLogin" value="false" id="saveLogin"/><label for="saveLogin"> 保存登录状态24小时</label></p>
	<p style="float:right;padding:10px 0 0 0;"><input type="submit" id="submitBT" value="登录" style="width:80px;" /></p>
	<div class="clear"></div>
</form>
	<div class="tx_center">
		 &copy; Keel 2011-8 All rights reserved.
	</div>
</div>

</body>
</html>