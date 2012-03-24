<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,com.k99k.khunter.*,com.k99k.tools.*" session="false" %>
<%
String sPrefix = KFilter.getStaticPrefix();
String prefix = KFilter.getPrefix();
Object o = request.getAttribute("[jspAttr]");
HttpActionMsg data = null;
if(o != null ){
	data = (HttpActionMsg)o;
}else{
	out.print("ERROR:100404");
	return;
}
KObject user = (KObject)data.getData("u");
KObject user_one = (KObject)data.getData("one");
out.print(JSPOut.out("head0","0","编辑用户信息"));%>
<link rel="stylesheet" href="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.css" type="text/css" media="screen" />
<script src="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.validate.min.js" type="text/javascript"></script>
<script type="text/javascript">
$.sPrefix = "<%=sPrefix %>";$.prefix="<%=prefix %>";
$(function(){
	$("#side_gg a").addClass("sideON");
//处理请求
var close = "<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a>";
$.validator.dealAjax = {
	bt:$("#submitBT"),
	ok:function(data){
		if(data=="ok"){abox("编辑用户信息","<div class='reOk'>用户信息更新成功！ &nbsp;"+close+"</div>");};
	},
	err:function(){
		abox("编辑用户信息","<div class='reErr'>用户信息更新失败! &nbsp;"+close+"</div>");
	}
};
$.validator.addMethod("isMobile", function(value, element) {   
	  var length = value.length;   
	  return this.optional(element) || (length == 11 && /^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1}))+\d{8})$/.test(value));   
	}, "请正确填写您的手机号码"); 
//开始验证
$('#user_form').validate({
    /* 设置验证规则 */
    rules: {
		user_pwd: {
            rangelength:[6,20]
        },
        user_pwd2:{
            rangelength:[6,20],
            equalTo: "#user_pwd"
        },
        user_phone:{
        	isMobile:true
        },
        user_email:{
            email:true
        },
        user_qq:{
        	number:true
        }
    }
});

});
function aSubmit(){
	$("#user_form").submit();
};
</script>
<%out.print(JSPOut.out("main0",new String[]{"0","1"},new String[]{user.getName(),user.getProp("company").toString()})); %>
<jsp:include page="sidenav.jsp" flush="false" > 
  <jsp:param name="lv" value="<%=user.getLevel() %>" /> 
  <jsp:param name="type" value='<%=user.getType() %>' /> 
  <jsp:param name="gg" value='<%=user.getProp("newNews").toString() %>' /> 
  <jsp:param name="tt" value='<%=user.getProp("newTasks").toString() %>' /> 
</jsp:include>

		<div id="mainContent">
<div class="abox">
<div class="aboxTitle">编辑用户信息</div>
<div class="aboxContent">
<form action="<%=prefix%>/user/a_u" method="post" id="user_form">
<p>
用户名：<span class="bold blue"><%=user_one.getName()%></span> 公司：<span class="bold blue"><%=user_one.getProp("company")%></span></p>
<p>
密码修改：<span class="gray">(无需修改请留空)</span><br />
<input type="password" id="user_pwd" name="user_pwd" value="" /><br />
再次输入：<br />
<input type="password" id="user_pwd2" name="user_pwd2" value="" /><br />
<br />
手机号码：<br />
<input type="text" id="user_phone" name="user_phone" value="<%=user_one.getProp("phoneNumber")%>" /><br />
电子邮箱：<br />
<input type="text" id="user_email" name="user_email" value="<%=user_one.getProp("email")%>" /><br />
QQ号：<br />
<input type="text" id="user_qq" name="user_qq" value="<%=user_one.getProp("qq")%>" /><br />
<input type="hidden" name="user_id" value="<%=user_one.getId()%>" />

</p>
<p><a href="javascript:aSubmit();" id="submitBT" class="aButton tx_center" style="width:60px;">保存</a><a href="javascript:history.go(-1);" class="aButton tx_center" style="width:60px;">返回</a></p>
</form>
</div>
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>