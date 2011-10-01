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
KObject one = (KObject)data.getData("one");
KObject product = (KObject)data.getData("product");
ArrayList<KObject> files = (data.getData("files")==null)?null:(ArrayList<KObject>)data.getData("files");
ArrayList<KObject> tus = (data.getData("tus")==null)?null:(ArrayList<KObject>)data.getData("tus");
Boolean ismy = request.getParameter("my")!=null && request.getParameter("my").equals("true");
String myPara = (ismy)?"/my":"";
int state  =one.getState();
out.print(JSPOut.out("head0","0",one.getName()));%>
<link rel="stylesheet" href="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.css" type="text/css" media="screen" />
<script src="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script type="text/javascript">
$.sPrefix = "<%=sPrefix %>";$.prefix="<%=prefix %>";
$.isMy = <%=(ismy)?"true":"false" %>;
function del(id){
	var r=confirm("确认删除此条任务吗？\r\n\r\n["+$(".aboxTitle>div").text()+"]");
	if (r==true){
		$.post("<%=prefix %>/tasks/del", "id="+id ,function(data) {
			if(data=="ok"){alert("删除成功");window.location = "<%=prefix %>/news";};
		});
	}
	return;
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
	
	
	
	
});
//-----------机型选择-------------------
var phoneType = {0:"240x320",1:"320x480",2:"240x400",3:"480x800",4:"480x854",5:"480x960"};
var allPData = [];
var aaData = [],gMap={};
var cGroup = 0;
function selectOK(){
	var ok = $("<div class='sok'></div>");
	$("#td_in").find(".phone1").each(function(i){
		$("<span class='txtBox' id='s"+this.id+"'>"+$(this).text()+"</span>").appendTo(ok);
	});
	ok.appendTo($("#fu_"+$("#choosePhone")[0].fu));
	clearIn();$("#choosePhone").appendTo("#hide");
}
function selectPhone(i){
	$("#fu_"+i).css("background-color","#FFF");
	if(allPData.length == 0){
		//abox("Loading...","请稍侯...");
		$.getJSON("<%=prefix %>/phone/json?s=<%=product.getProp("sys") %>",function(sData){
			if(sData==""){alert("产品操作系统不正确.请返回上一步重设.");return;}
			var data = sData.gg;
			for(var i=1,j=data.length;i<j;i++){
				var gg = $("<a class=\"aButton phoneCate\" href=\"javascript:showGroup("+data[i].g+");\" id='ga"+data[i].g+"'>"+phoneType[data[i].g]+"<\/a>");
				$("#phoneCates").after(gg);
			}
			allPData = data;
			aaData = sData.aa;
			$("#phone_fast").keyup(function(e){scPh(e);});
			addP2Group(data);
			addGG(aaData);
			//aboxClose();
		});
	}else{clearIn();
		$("#fu_"+i).find(".txtBox").each(function(){
			var a = this.id.split("_");$("#p_"+a[1]+"_"+a[2])[0].io();
		});
	$("#fu_"+i).find(".sok").remove();}
	$("#choosePhone")[0].fu = i;
	$("#choosePhone").appendTo($("#fu_"+i));
}
function addGG(aa){
	var i = 0;
	for (p in aa) {
		var gg = $("<a class=\"aButton phoneCate\" href=\"javascript:aaGroup('"+p+"');\">"+p+"<\/a>");
		$("#phoneCates").after(gg);
	}
}
function aaGroup(p){
	$("#g"+cGroup).hide();cGroup = 999;$("#g999").show();
	for(var i=0,n=aaData[p];i<n.length;i++){
		for(var c in gMap){
			if(c == n[i]){
				$(gMap[c])[0].out();
			}
		}
	}
}
function clearIn(){
	$("#td_in").find(".phone1").each(function(i){
		this.io();
	});
}
function scPh(e){
	var k = e.keyCode;
	if(k==38||k==40||k==9||k==13||k==46||(k>8&&k<32)){return;}
	var q = $.trim($("#phone_fast").val());
	if(q==""){showGroup(0);return;}
	$("#g"+cGroup).hide();
	cGroup = 999;$("#g999").show();
	q = q.toLowerCase();
	for(var c in gMap){
		var e = $(gMap[c])[0];
		if(c.toLowerCase().indexOf(q)>=0){
			e.out();
		}else if(e.state==2){e.reset();}
	}
}
function clear999(){
	$("#g999 .phone").each(function(i){
		this.reset();
	});
}
function io(i,j){
	$("#p_"+i+"_"+j)[0].io();
}
function createPh(i,j,c){
	var p = $("<a class=\"phone\" href=\"javascript:io("+i+","+j+");\" id='p_"+i+"_"+j+"'>"+c+"<\/a>");
	p[0].state=1;p[0].i=i;p[0].j=j;p[0].c=c;
	p[0].io = function(){
		if(this.state!=0){$(this).addClass("phone1").appendTo("#td_in");this.state=0;}
		else{$(this).removeClass("phone1").appendTo("#g"+(this.i-1));this.state=1;}
	};
	//如果不在in中则移动到当前group,用于search
	p[0].out = function(){
		if(this.state!=0){$(this).appendTo("#g"+cGroup);this.state=2;}
	};
	p[0].reset = function(){
		if(this.state==0){this.io();}else if(this.state==2){$(this).appendTo("#g"+(this.i-1));}
	};
	gMap[c] = "#p_"+i+"_"+j;
	return p;
}
function createPhg(d){
	var p = $("<a class=\"phone\" href=\"javascript:io("+i+","+j+");\" id='p_"+i+"_"+j+"'>"+c+"<\/a>");
	for(var j = 0,k=data[i].d.length;j<k;j++){
		createPh(i,j,data[i].d[j]).appendTo(gg);
	}
	p.click(function(){
		
	});
	return p;	
}
function addP2Group(data){
	for ( var i = 1; i < data.length; i++) {
		var gg = $("<div id='g"+data[i].g+"'></div>");
		for(var j = 0,k=data[i].d.length;j<k;j++){
			createPh(i,j,data[i].d[j]).appendTo(gg);
		}
		gg.hide().appendTo($("#td_out"));
	}
	showGroup(0);
}
function chooseAll(){
	$("#g"+cGroup).find(".phone").each(function(){var a=this.id.split("_");io(a[1],a[2]);});
}
function showGroup(i){
	clear999();
	if($("#td_out").find("#g"+i).length>0){
		$("#g"+cGroup).hide();
		$("#g"+i).show();
		cGroup = i;
	}
}
//--------------------
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
<div class="aboxTitle"><div>测试任务:<%=one.getName() %></div> </div>
<div class="aboxSub"><div style="color:#6E747B;float:left;padding-top:7px;"> 
当前状态: <span id="cState"><%=one.getProp("state") %></span> 
待办人：<span id="operator" class="blueBold"><%=one.getProp("operator") %></span> 
<%if(Integer.parseInt(user.getType())>1){ 
	StringBuilder sb1 = new StringBuilder("优先级：<span id=\"cLevel\" class=\"");
	if(one.getLevel()==0){sb1.append("blueBold\">普通");}else{sb1.append("redBold\">").append(one.getLevel());}
	sb1.append("</span>");out.print(sb1);
}%>
</div>
<%if(Integer.parseInt(user.getType())>10){ 
	String ggid = String.valueOf(one.getId());
	String edit = prefix+"/tasks/"+ggid+"?edit=true";
%>
<a href="<%=edit%>" class="aButton">编辑</a>
<a href="javascript:del(<%=ggid%>);" class="aButton">删除</a>
<%} %>
<a href="<%=prefix+"/tasks"+myPara%>" class="aButton">返回任务列表</a></div>
<div class="aboxContent" style="padding:20px;">
<div>
产品名称：<span class="blueBold"><%=one.getName() %></span> 
公司：<span class="blueBold"><%=product.getProp("company") %></span><br />
</div>
<div class="inBox" id="productFS3">
    <div class="inBoxTitle">产品信息</div> 
    <div class="inBoxContent">
    	<div class="inBoxLine">产品业务平台ID: <span id="task_p_id_v" class="blueBold"><%=product.getProp("productID") %></span> 手机系统: <span id="task_p_sys_v" class="blueBold"><%=product.getProp("sys") %></span> 联网情况: <span id="task_p_net_v" class="blueBold"><%=product.getProp("netType") %></span> 接口调测情况: <span id="task_p_acc_v" class="blueBold"><%=product.getProp("netPort") %></span></div> 
    	<div class="inBoxLine">产品计费类型: <span id="task_p_type_v" class="blueBold"><%=product.getProp("type") %></span> 计费点描述: <br /><span id="task_p_fee_v" class="blueBold"><%=product.getProp("feeInfo") %></span></div> 
    </div>
</div>

<%
if(files != null && !files.isEmpty()){
StringBuilder sb = new StringBuilder();
	sb.append("<div class='inBox' id='files'><div class='inBoxTitle'>游戏实体包</div><div class='inBoxContent'>");
	Iterator<KObject> it = files.iterator();int i = 0;
	while(it.hasNext()){
		KObject f=it.next();
		sb.append(" <div class='file_upload' style='background-color:#FFF;' id='fu_").append(i);
		sb.append("'><a href='").append(prefix).append("/gamefile/").append(f.getId()).append("' class=\"filename bold\">").append(f.getName()).append("</a><span class=\"u_ok\"> [ <a href='javascript:selectPhone(").append(i).append(");'>适配机型</a> ]</span><div class=\"groups\">");
		ArrayList<String> gps = (ArrayList<String>)f.getProp("groups");
		Iterator<String> itr = gps.iterator();
		while(itr.hasNext()){
			String g = itr.next();
			sb.append("<span class='txtBox2'>").append(g).append("</span>");
		}
		sb.append("</div></div>");
		i++;
	}
	sb.append("</div></div>");
	out.print(sb);
}
%>
<div class="inBox" id="infos">
    <div class="inBoxTitle">任务流程及说明</div> 
    <div class="inBoxContent">
    	<%
    	Object logO = one.getProp("log");
    	if(logO!=null){
    		ArrayList<HashMap<String,Object>> logs = (ArrayList<HashMap<String,Object>>)one.getProp("log"); 
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

<% 
//已创建
if(state==0){%>
<div id="appoint">
<form action="<%=prefix%>/tasks/a_p" method="post" id="p_form">
<p><label for="task_info">任务附加说明：</label><br />
<textarea id="task_info" name="task_info" rows="3" cols="3" style="height:60px;"></textarea></p>
<p>优先级调整：
<select name="task_level" id="task_level"><option value="0">普通</option><option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option></select>
</p>
<p>下一流程处理人：
<select name="task_operator"><option value="曹雨">曹雨</option></select>
</p>
<textarea rows="1" cols="1" class="hide" name="task_tu_json_h" id="task_tu_json_h"></textarea>
</form>
<p><a href="javascript:aSubmit();" id="submitBT" class="aButton tx_center" style="width:60px;">分配任务</a> <a href="javascript:pre(3);" class="aButton tx_center">退回创建人</a> <a href="<%=prefix+"/tasks"+myPara%>" class="aButton">返回任务列表</a></p>
</div>

<%}else if(state==1){%>

<%}else if(state==2){%>

<%}else if(state==3){%>

<%}else if(state==4){%>

<%}%>
</div>
<div id="hide" class="hide">


<div id="choosePhone" class="inBox" style="width:95%;">
	<div style="padding:10px;">
	<div id="selectedPhones">
		<div class="inBoxTitle">已选中机型：<span class="gray normal">(点击删除)</span>	</div>
		<div class="inBoxContent" style="border-bottom: 1px dotted #aaa;background-color:#FFF;">
			<table width="100%">
			<tr><td id="td_in"></td>
			<td style="width:90px;"><a class="aButton" href="javascript:selectOK();" style="width:70px;text-align:center;">确定所选</a></td></tr>
			</table>
			
		</div>
	</div>
	<div id="phones">
		<div id="phoneCates" class="inBoxTitle">备选机型组：<span class="gray normal">(点击组名选择分组,点击机型名或全选进行选择,搜索框可在<span class="black bold">该类系统所有机型</span>中筛选)</span></div><span class="aButton phoneCate"><label for="phone_fast">搜索:</label><input style="padding:3px 5px;margin:0;width:100px;" type="text" name="phone_fast" id="phone_fast" /></span>
		<div class="inBoxContent" style="border-bottom:1px dotted #aaa;border-top:1px dotted #aaa;background-color:#FFF;">
			<table width="100%">
			<tr><td id="td_out"><div id="g999"></div></td>
			<td style="width:60px;"><a class="aButton" href="javascript:chooseAll();">全选</a></td></tr>
			</table>
		</div>
	</div>
	</div>
</div>
</div>

</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>