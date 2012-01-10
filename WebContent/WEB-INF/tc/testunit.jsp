<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,com.k99k.testcenter.*,com.k99k.khunter.*,com.k99k.tools.*" session="false" %>
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
KObject one = (KObject)data.getData("one");
KObject product = (KObject)data.getData("product");
KObject task = (KObject)data.getData("task");
KObject[] cases = (data.getData("cases")==null)?null:(KObject[])data.getData("cases");
ArrayList<HashMap<String,Object>> res = (ArrayList<HashMap<String,Object>>)one.getProp("re");
Boolean ismy = request.getParameter("my")!=null && request.getParameter("my").equals("true");
String myPara = (ismy)?"/my":"";
int userType = user.getType();
int sys = Integer.parseInt(product.getProp("sys").toString());
boolean canSave = (task.getState() == 1 && userType>1 && userType<=4 && user.getName().equals(one.getProp("tester"))) || (userType==99);
out.print(JSPOut.out("head0","0",product.getName()));%>
<link rel="stylesheet" href="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.css" type="text/css" media="screen" />
<script src="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.json-2.3.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload_tc.js" type="text/javascript"></script>
<script type="text/javascript">
<!--
$.sPrefix = "<%=sPrefix %>";$.prefix="<%=prefix %>",$.tid=<%=one.getId() %>;
$.isMy = <%=(ismy)?"true":"false" %>;
function showHtml(s) {
	s=s.replace(/\x20/g,"&nbsp;");
	s=s.replace(/\n/g,"<br />") ;
	s=s.replace( /(http[s]?:\/\/[\w-:\/]*(\.[\w-:\/]*)+)/ig ,"<a href='$1' target='_blank'>$1</a>") ;
	return s;
}
$(function(){
	var tar = ($.isMy) ? "#side_mytask a" : "#side_task a";$(tar).addClass("sideON");
	$("#task_level").val(<%=one.getLevel() %>);
	var p_type = ["免费","短代","点数","下载","进包"];
	$("#task_p_type_v").text(p_type[parseInt($("#task_p_type_v").text())]);
	var net_type = ["单机","联网","WAP"];
	$("#task_p_net_v").text(net_type[parseInt($("#task_p_net_v").text())]);
	var port_type = ["未调通接口","已调通","接口调测中"];
	$("#task_p_acc_v").text(port_type[parseInt($("#task_p_acc_v").text())]);
	var sys = ["kjava","Android","WAP","Brew","Windows mobile","Windows CE","其他"];
	$("#task_p_sys_v").text(sys[parseInt($("#task_p_sys_v").text())]);
	var cState = ["待测","测试中","通过","待反馈","部分通过","暂停","结果确认中"];
	$("#cState").text(cState[parseInt($("#cState").text())]);

	//初始化已测的结果
	var jj = <%=(res==null || res.isEmpty())?"[]":JSON.write(res)%>;
	initRE(jj);
	if(<%=canSave%> && <%=(one.getProp("rank")!=null)%>){
		$("#tu_rank").val(<%=one.getProp("rank")%>);
	}else{
		var cRank = ["暂无","差","较差","一般","较好","优秀"];
		$("#tRank").text(cRank[parseInt($("#tRank").text())]);
	}
	initUpload("<%=user.getName() %>",null,null,null,null,$.prefix+"/upload2");
});
var res=[];
function initRE(j){
	var len = j.length;
	if(len>0){
		for(var i=0;i<len;i++){
			var r=j[i];
			res[r.caseId] = r;
		}
	}
	updateRes();
}
function updateRes(){
	$("#testCases .file_upload").each(function(){
		var cid = this.id.split("_")[1];
		if(res[cid]){
			var rr=res[cid];
			$(this).find(".tus").removeAttr("class").attr("class","tus tu"+rr.re);
			var info=(rr.attach.length<1)?showHtml(rr.info):(showHtml(rr.info)+"<br /><img src='"+$.sPrefix+"/file/"+rr.attach+"' />");
			$(this).find(".re_re").html(info);
		}
	});
}
function ere(i){
	//显示编辑
	$("#execCase").appendTo("#exec_"+i);
	clearExe();
	if(res[i]){
		var r=res[i];$("#tu_re").val(r.re);$("#tu_info").html(r.info);
	}else{
		clearExe();
	}
	$("#cexe").val(i);
}
function clearExe(){
	$("#cexe").val("");$("#tu_re").val(0);$("#tu_info").val("");
}
function exec(){
	//更新res
	var i=$("#cexe").val();
	if(i===""){alert("更新失败!");return;}
	var n={caseId:i,re:$("#tu_re").val(),info:$("#tu_info").val(),attach:$("#upFiles").text()};
	res[i]=n;
	$("#execCase").appendTo("#hide");
	updateRes();
}
function allPass(){
	$("#testCases .file_upload").each(function(){
		var cid = this.id.split("_")[1];
		if(res[cid]){
			res[cid].re=2;
		}else{res[cid]={caseId:cid,re:2,info:"",attach:""};}
	});
	updateRes();
}
function allWait(){
	$("#testCases .file_upload").each(function(){
		var cid = this.id.split("_")[1];
		if(res[cid]){
			res[cid].re=0;
		}else{res[cid]={caseId:cid,re:0,info:"",attach:""};}
	});
	updateRes();
}
function saveRE(){
	//保存res
	abox("保存测试结果","处理中,请稍候……");
	var ff = [];
	$(".files_name").each(function(){
		ff.push(encodeURIComponent($(this).text()));
	});
	var close= "<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a>",err=function(){abox("保存测试结果","<div class='reErr'>保存测试结果失败！ &nbsp;"+close+"</div>");};
	$.post($.prefix+"/tasks/a_exec",{"sys":<%=sys%>,"tu_id":<%=one.getId() %>,"json":$.toJSON(res),"rank":$("#tu_rank").val(),"ff":ff.join(",")},function(data){
		if(data=="ok"){
			abox("保存测试结果","<div class='reOk'>保存测试结果成功！ &nbsp;"+close+"</div>");
		}else{err();}
	}).error(function(){err();});
}
-->
</script>
<%out.print(JSPOut.out("main0","0",user.getName())); %>
<jsp:include page="sidenav.jsp" flush="false" > 
  <jsp:param name="lv" value="<%=user.getLevel() %>" /> 
  <jsp:param name="type" value="<%=user.getType() %>" /> 
  <jsp:param name="gg" value='<%=user.getProp("newNews").toString() %>' /> 
  <jsp:param name="tt" value='<%=user.getProp("newTasks").toString() %>' /> 
</jsp:include>

		<div id="mainContent">
<div class="abox">
<div class="aboxTitle"><div><%=product.getName() %></div> </div>
<div class="aboxSub"><div style="color:#6E747B;float:left;padding-top:7px;"> 
当前状态: <span id="cState" class="blueBold"><%=one.getProp("state") %></span> 
待办人：<span id="operator" class="blueBold"><%=one.getProp("tester") %></span> 
<%if(userType>1){ 
	StringBuilder sb1 = new StringBuilder("优先级：<span id=\"cLevel\" class=\"");
	if(one.getLevel()==0){sb1.append("blueBold\">普通");}else{sb1.append("redBold\">").append(one.getLevel());}
	sb1.append("</span>");out.print(sb1);
}%>
</div>
<%if(userType>3){ 
	String ggid = String.valueOf(one.getId());
%>
<a href="javascript:del(<%=ggid%>);" class="aButton">删除</a>
<%} %>
<a href="<%=prefix+"/tasks"+myPara%>" class="aButton">返回任务列表</a></div>
<div class="aboxContent" style="padding:20px;">
<div>
产品名称：<span class="blueBold"><%=product.getName() %></span> 
公司：<span class="blueBold"><%=product.getProp("company") %></span><br />
</div>
<div class="inBox" id="productFS3">
    <div class="inBoxTitle">产品信息</div> 
    <div class="inBoxContent">
    	<div class="inBoxLine">产品业务平台ID: <span id="task_p_id_v" class="blueBold"><%=product.getProp("productID") %></span> 手机系统: <span id="task_p_sys_v" class="blueBold"><%=product.getProp("sys") %></span> 联网情况: <span id="task_p_net_v" class="blueBold"><%=product.getProp("netType") %></span> 接口调测情况: <span id="task_p_acc_v" class="blueBold"><%=product.getProp("netPort") %></span></div> 
    	<div class="inBoxLine">产品计费类型: <span id="task_p_type_v" class="blueBold"><%=product.getProp("type") %></span> 计费点描述: <br /><span id="task_p_fee_v" class="blueBold"><%=product.getProp("feeInfo") %></span></div> 
    	<%if(sys==2){%>
    	<div class="inBoxLine">测试入口URL: <span id="task_p_url_v" class="blueBold"><%=product.getUrl()%></span></div> 
    	<%} %>
    </div>
</div>
<%if(userType>1){%>
<div class="inBox" id="infos">
    <div class="inBoxTitle">任务流程及说明</div> 
    <div class="inBoxContent">
    	<%
    	Object logO = task.getProp("log");
    	if(logO!=null){
    		ArrayList<HashMap<String,Object>> logs = (ArrayList<HashMap<String,Object>>)logO; 
    		if(!logs.isEmpty()){
    			StringBuilder sb2 = new StringBuilder();
    	    	Iterator<HashMap<String,Object>> itl = logs.iterator();
    	    	while(itl.hasNext()){
    	    		HashMap<String,Object> log = itl.next();StringUtil.getFormatDateString("yyyy-MM-dd hh:mm:ss",(Long)log.get("time"));
    	    		sb2.append("<div class='inBoxLine'>").append(log.get("user")).append(" ").append(StringUtil.getFormatDateString("yyyy-MM-dd hh:mm:ss",(Long)log.get("time"))).append(" ");
    	    		sb2.append(log.get("info")).append("</div>");
    	    	}
    	    	out.print(sb2);
    		}
    	}
    	%>
    </div>
</div>
<%} 
StringBuilder sb = new StringBuilder();
sb.append("<div class='inBox' id='files'><div class='inBoxTitle'>测试单元 <span style='font-size:12px;font-weight:normal;'>(<span class='tu0'>未测</span><span class='tu2'>通过</span><span class='tu3'>待反馈</span><span class='tu4'>部分通过</span><span class='tu9'>未通过</span>)</span></div><div class='inBoxContent'><div class='file_upload' style='background-color:#FFF;'><span class='tu");
sb.append(one.getState()).append("'>").append(one.getProp("phone")).append("</span> ");
Object o_f = one.getProp("fileId");
if(sys!=2 && StringUtil.isDigits(o_f)){
	long fileId = Long.parseLong(o_f.toString());
	if(fileId>0){
 		sb.append("文件下载: <a href='").append(prefix).append("/gamefile/").append(fileId).append("' class=\"filename\">").append(one.getProp("gFile")).append("</a>");
	}
}else{
	sb.append("WAP入口地址：").append(product.getUrl());
}
sb.append("</div></div></div>");
out.print(sb);
%>
<div class="inBox" id="testCases">
    <div class="inBoxTitle">测试项 <span style='font-size:12px;font-weight:normal;'>(<span class='tu0'>待测</span><span class='tu2'>通过</span><span class='tu4'>部分通过</span><span class='tu9'>未通过</span>)  <%if(canSave){%><span><a href="javascript:allPass();" class="aButton">全部预设为通过</a> <a href="javascript:allWait();" class="aButton">全部预设为未测</a></span><%}%></span></div> 
    <div class="inBoxContent">
    <%
    StringBuilder s = new StringBuilder();
    for(int j=0;j<cases.length;j++){
    	if(cases[j]==null){continue;}
    KObject ca = cases[j]; 
    int i = (Integer)ca.getProp("caseId");
    s.append("<div id='exec_").append(i).append("' class='file_upload' style='background-color:#FFF;'><div><span class='tu0 tus'>").append(ca.getProp("caseId"));
    s.append(". ").append(ca.getName()).append(" </span> ");
    if(canSave){
    s.append("<a href='javascript:ere(").append(i).append(");' class='aButton' style='font-size:12px;'>填报结果</a>");
    }
    s.append("</div><div class='blue' style='font-size:12px;padding:5px;'>要求:");
    s.append(ca.getProp("info")).append("</div><div class='re_re'></div></div>\r\n");
    }
    if(canSave){
    	s.append("<br />总体评价： <select name='tu_rank' id='tu_rank'><option value='3'>一般</option><option value='4'>较好</option><option value='5'>优秀</option><option value='2'>较差</option><option value='1'>差</option></select>");
    }else{
    	s.append("<br />总体评价：<span id='tRank'>");
    	if(one.getProp("rank") != null){
    		s.append(one.getProp("rank"));
    	}else{s.append(0);}
    	s.append("</span>");
    }
    out.print(s);%>
    <br /><br />
    <%
Object off = one.getProp("attachs");
if(StringUtil.isStringWithLen(off,2)){
	sb = new StringBuilder();
	String[] fileList = off.toString().split(",");
	sb.append("<div class='bold' style='padding-top:10px;'>文件列表</div>");
	for(int i = 0;i<fileList.length;i++){
		String f=fileList[i];
		sb.append("<div class='file_upload'><a href='").append(prefix).append("/file/").append(f).append("'>").append(f).append("</a></div>");
	}
	out.print(sb);
}
%>
    <div>测试附件(如问题截图等):<span id="swfBT"><span id="spanSWFUploadButton">请稍候...</span><span id="uploadInfo"></span></span><div id="upFiles"></div></div>
    </div>
</div>

<br />
<%if(canSave){%>

<a href="javascript:saveRE();" class="aButton">保存测试结果</a> <%} %>
<a href="<%=prefix+"/tasks"+myPara%>" class="aButton">返回任务列表</a></div>
<div id="hide" class="hide">
<div class="inBoxLine" id="execCase">
	<input type="hidden" value="" name="cexe" id="cexe" />
  	测试结果:<select name="tu_re" id="tu_re"><option value="0">未测</option><option value="2">通过</option><option value="4">部分通过</option><option value="9">不通过</option></select><br />
  	测试结果说明:<br /><textarea style="height:60px;" rows="3" cols="3" id="tu_info" name="tu_info"></textarea>
  	<br /><a href="javascript:exec();" class="aButton">  确定  </a>
</div> 
</div>
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>