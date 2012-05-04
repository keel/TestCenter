<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,com.k99k.khunter.*,com.k99k.tools.*" session="false" %>
<%!
static final HashMap<String,String> timeMap = new HashMap<String,String>();
static{
	if(timeMap.isEmpty()){
	timeMap.put("day", "日报表");
	timeMap.put("week", "周报表");
	timeMap.put("month", "月报表");
	timeMap.put("", "时间段查询");
	}
}
 %>
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
String timeFlag = (data.getData("time")==null)?"":data.getData("time").toString();
String timeFlagName = timeMap.get(timeFlag);
long start = 0;
long end = System.currentTimeMillis();
Calendar cal = Calendar.getInstance(); 
if(timeFlag.equals("day")){
	cal.set(Calendar.HOUR_OF_DAY, 0);
	cal.set(Calendar.MINUTE, 0);
	cal.set(Calendar.SECOND, 0);
}else if(timeFlag.equals("week")){
	int day_of_week = cal.get(Calendar.DAY_OF_WEEK) - 2;  
    cal.add(Calendar.DATE, -day_of_week); 
    cal.set(Calendar.HOUR_OF_DAY, 0);
	cal.set(Calendar.MINUTE, 0);
	cal.set(Calendar.SECOND, 0);
}else if(timeFlag.equals("month")){
	cal.set(Calendar.DATE, 1);
	cal.set(Calendar.HOUR_OF_DAY, 0);
	cal.set(Calendar.MINUTE, 0);
	cal.set(Calendar.SECOND, 0);
}
start = cal.getTimeInMillis();
out.print(JSPOut.out("head0","0",timeFlagName));%>
<link rel="stylesheet" href="<%=sPrefix %>/css/default.css" type="text/css" media="screen" />
<script src="<%=sPrefix %>/js/glDatePicker.min.js" type="text/javascript"></script>
<script type="text/javascript">
$.sPrefix = "<%=sPrefix %>";$.prefix="<%=prefix %>";
var start = <%=start %>,end = <%=end %>;
$(function(){
	<%if(!timeFlag.equals("")){%>
	showDefault();
	<%}%>
	$("#tsp").glDatePicker();
	$("#tep").glDatePicker();
});
function search(){
	var s = $("#tsp").val(),e = $("#tep").val();
	if(s && e && s!="" && e!=""){
		start = new Date(s+" 00:00:00").getTime();
		end = new Date(e+" 00:00:00").getTime();
		showDefault();
	}
}
function showDefault(){
	$.getJSON($.prefix+"/ana/period?start="+start+"&end="+end,function(data){
		showSummary(data,$("#summary"));
	});
}
function showTime(time,to){
	var t = new Date(time);
	$(to).text(t.getFullYear()+"-"+(t.getMonth()+1)+"-"+t.getDate()+" "+t.getHours()+":"+t.getMinutes()+":"+t.getSeconds());
}

function showSummary(anaJson,to){
	if(anaJson && anaJson != ''){
		var f = anaJson;
		var tb = "<div id='anaSummary'>";
		tb += "已通过产品:<span>"+f.pass+"</span> 部分通过产品:<span>"+f.pass_part+"</span> 待反馈产品:<span>"+f.need_back+"</span> 测试中产品:<span>"+f.testing+"</span> 已放弃产品:<span>"+f.droped+"</span> 有效产品总数:<span>"+f.sum+"</span><br />";
		tb += "已完成测试任务:<span>"+f.taskDone+"</span> 测试中的任务:<span>"+f.taskTesting+"</span> 待执行任务:<span>"+f.taskWillDo+"</span> 测试单元完成总数:<span>"+f.tuDone+"</span>";
		tb=tb+"</div>";
		$(to).html(tb);
		$("#anaSummary span").addClass("blueBold");
		showTime(start,"#tStart");showTime(end,"#tEnd");
	}
}
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
<div class="aboxTitle"><div><%=timeFlagName %></div> </div>
<div class="aboxSub" style="text-align: left;">开始时间:<input type="text" id="tsp" class="timePick" /> 结束时间: <input id="tep" type="text" class="timePick" /> <a href="javascript:search();" class="aButton">查询</a></div>
<div class="aboxContent" style="padding:20px;">
<div class="bold">统计：[<span class="blueBold" id="tStart"></span> --- <span id="tEnd" class="blueBold"></span>] </div>
<div id="summary" class="inBox"></div>
<br />
<div><a href="javascript:void();" class="aButton">查看测试任务详情</a></div>
<div id="detail"></div>
</div>
<div class="aboxSub2"></div>
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>