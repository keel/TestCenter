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
out.print(JSPOut.out("head0","0","用户管理"));%>
<script src="<%=sPrefix %>/js/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/pagenav.min.js" type="text/javascript"></script>
<script type="text/javascript">
$.sPrefix = "<%=sPrefix %>";$.prefix="<%=prefix %>";
$(function(){
	$("#side_gg a").addClass("sideON");
	//处理请求
	$.validator.dealAjax = {
		bt:$("#submitBT"),
		loading:function(){$("#re").text("请稍候...");},
		ok:function(data){
			if(data != "null"){
				showRE(data);
			}else{$("#re").text("无此用户.");};
		},
		err:function(){
			$("#re").text("用户查询失败!");
		}
	};
	$.validator.addMethod("isMobile", function(value, element) {   
		  var length = value.length;   
		  return this.optional(element) || (length == 11 && /^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1}))+\d{8})$/.test(value));   
		}, "请正确填写您的手机号码"); 
	//开始验证
	$('#u_search').validate({
	    /* 设置验证规则 */
	    rules: {
	    	uName:{
	             rangelength:[1,20]
	         },
	         uCom:{
	             rangelength:[1,20]
	         },
	    	uHand:{
	         	isMobile:true
	         },
	         uMail:{
	             email:true
	         }
	    }
	});
	
});
function showRE(data){
	var re = $.parseJSON(data);
	if(re.length>1){
		var r = "<table width=\"90%\" class=\"table_list\" cellpadding=\"0\" cellspacing=\"1\"><th>用户名</th><th>公司</th><th>手机</th><th>邮箱</th><th>QQ号</th><th>info</th><th>type</th>";
		for(var i=1;i<re.length;i++){
			var ro = $.parseJSON(re[i]);
			r+="<tr><td><a href='<%=prefix %>/user/one?u="+ro.name+"'>"+ro.name+"</a></td><td>"+ro.company+"</td><td>"+ro.phoneNumber+"</td><td>"+ro.email+"</td><td>"+ro.qq+"</td><td>"+ro.info+"</td><td>"+ro.type+" </td></tr>";
			
		}
		r+="</table>";
	}
	$("#re").html(r);
}
function searchU(){
	$("#u_search").submit();
};
</script>
<%out.print(JSPOut.out("main0",new String[]{"0","1"},new String[]{user.getName(),user.getProp("company").toString()})); %>
<jsp:include page="sidenav.jsp" flush="false" > 
  <jsp:param name="lv" value="<%=user.getLevel() %>" /> 
  <jsp:param name="type" value="<%=user.getType() %>" /> 
  <jsp:param name="gg" value='<%=user.getProp("newNews").toString() %>' /> 
  <jsp:param name="tt" value='<%=user.getProp("newTasks").toString() %>' /> 
</jsp:include>

		<div id="mainContent">
<div class="abox">
<div class="aboxTitle"><div>用户管理</div> </div>
<div class="aboxSub" style="text-align: left;">按条件查询：
</div>
<div class="aboxContent" style="padding:20px;">
<form name="u_search" id="u_search" action="user/search" method="post">
<div><label for="uName">用户名:</label><br /> <input type="text" id="uName" name="uName" /></div>
<div><label for="uCom">公司名:</label><br /> <input type="text" id="uCom" name="uCom" /></div>
<div><label for="uHand">手机号码:</label><br /> <input type="text" id="uHand" name="uHand" /></div>
<div><label for="uMail">电子邮箱:</label><br /> <input type="text" id="uMail" name="uMail" /></div>
</form>
<br />
<div><a id="submitBT" href="javascript:searchU();" class="aButton tx_center">搜索用户</a> &amp; <a href="javascript:addU();" class="aButton tx_center">添加用户</a></div>
<br />
</div>
</div>
<div id="re"></div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>