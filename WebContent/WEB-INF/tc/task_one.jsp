<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,com.k99k.khunter.*,com.k99k.testcenter.*,com.k99k.tools.*" session="false" %>
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
int userType = user.getType();
out.print(JSPOut.out("head0","0",one.getName()));%>
<link rel="stylesheet" href="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.css" type="text/css" media="screen" />
<script src="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.json-2.3.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/tc.choose_phone.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/drag.js" type="text/javascript"></script>
<script type="text/javascript">
<!--
$.sPrefix = "<%=sPrefix %>";$.prefix="<%=prefix %>",$.tid=<%=one.getId() %>,$.sys=<%=product.getProp("sys") %>;
$.isMy = <%=(ismy)?"true":"false" %>;
$.me="<%=user.getName() %>";
$.userType="<%=userType %>";
$.taskUrl=($.isMy)?"/tasks/my":"/tasks";
function del(id){
	var r=confirm("确认删除此条任务吗？\r\n\r\n["+$(".aboxTitle>div").text()+"]");
	if (r==true){
		$.post("<%=prefix %>/tasks/a_d", "id="+id ,function(data) {
			if(data=="ok"){alert("删除成功");window.location = "<%=prefix %>"+$.taskUrl;};
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
	var cState = ["待测","测试中","通过","待反馈","部分通过","暂停","结果确认中","","已反馈"];
	$("#cState").text(cState[parseInt($("#cState").text())]);
	//选择机型
	$(chooseDiv).appendTo("#hide");
	$("#choosePhone").data("url","<%=prefix %>/phone/json?s=<%=product.getProp("sys") %>");

	//处理请求
	$.validator.dealAjax = {
		bt:$("#submitBT"),
		loading:function(){abox("处理任务","请稍候...");},
		ok:function(data){
			if(!isNaN(data)){
				var bt1 = "<a href=\"javascript:window.location='<%=prefix%>/tasks/"+data+"';\" class=\"aButton\">查看任务</a>";
				abox("处理任务","<div class='reOk'>处理任务成功！ &nbsp;"+bt1+" <a href=\"javascript:window.location =('<%=prefix %>"+$.taskUrl+"');\" class=\"aButton\">返回列表</a></div>");
			}else{abox("处理任务","<div class='reErr'>处理任务失败! "+data+" &nbsp;"+close+"</div>");};
		},
		err:function(xhr){
			abox("处理任务","<div class='reErr'>处理任务失败! 错误码:"+xhr.responseText+" &nbsp;"+close+"</div>");
		}
	};

	$('#p_form').validate({
	    rules: {
			task_info: {required:true}
	    }
	});

	selectTU();

	//隐藏按钮
	$("#bt_confirm").hide();
<% //显示summary
if((state==TTask.TASK_STATE_CONFIRM && userType>1) || state==TTask.TASK_STATE_NEED_MOD || state==TTask.TASK_STATE_BACKED){%>
	var data=<%=(StringUtil.isStringWithLen(one.getProp("result"),2))?one.getProp("result").toString():"''"%>;
	if(data != ''){
		showSummary(data);
	}else{$("#fCases").html("未发现测试问题.");}
<%}%>

<% //处理finish选择
if(state==TTask.TASK_STATE_CONFIRM && userType>1){%>
	$("#tu_pass").change(function(){
		var v = $(this).val();
		if(v!=2 && v!=4){
			$("#task_operator").hide();
			var next = "";
			if (v==9){
				next = "<%=product.getProp("company") %>";
			}else if(v==-3){
				var ll = $("#infos").find(".inBoxLine");
				next = (ll.length<=0)?"无":ll.last().text().split(" ")[0];
			}else if(v==-2){
				next = "无";
			}
			$("#task_next").text(next);
		}else{
			$("#task_operator").show();
			$("#task_next").text("");
		}
	});
<%}%>

});
function aSubmit(f){
	if(f=="#p_form"){
		if($("#files").length>0){
			var b = true,tmp = [];
			$("#files .file_upload").each(function(){
				var v = $(this).find(".txtBox"),n = $(this).find(".filename").text(),j={"gFile":n,"fileId":$(this).find(".filename").attr("rel"),"phone":[]};
				if(v.length<=0){b=false;return false;}
				else{
					v.each(function(){
						j.phone.push($(this).text());
					});
					tmp.push(j);
				}
			});
			if(!b){alert("请为所有文件都指定测试机型!");return;}
			//生成文件json
			if(tmp.length>0){$("#task_tu_json_h").html($.toJSON(tmp));}
		}
	}
	$(f).submit();
};
var close= "<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a>";
var cTU = "";
function dropTU(tu){
	var p=tu.offset();
	$("#sendT .st").each(function(i){
		var me=$(this);s=me.offset();
		if(p.top>s.top && p.top<(s.top+me.outerHeight())){
			cTU=this.id;
			return false;
		}		
	});
	if(cTU!=""){
		var id=tu.attr("id"),tx=tu.text();
		tu.remove();
		var n=$("<span class='tu0' id='"+id+"'>"+tx+"</span>");
		tuDrag(n);n.appendTo("#"+cTU+" .tuu");
	}else{alert("拖放错误");}
}
function tuDrag(tar){
	tar.easydrag();
	tar.mousedown(function(){
		cTU=$(this).parent().parent()[0].id;
	});
	tar.ondrop(function(e,el){
		dropTU($(el));
	});
}
//显示分配任务
function selectTU(){
	if($.userType==2){
		var st=$("<div class='file_upload st' id='st_0' style='background-color:#FFF;'><div class='bold'>"+$.me+"</div><div class='tuu'></div></div>");
		$("#sendT").append(st);
		$("#showTUS .tu0").each(function(){
			var un=$(this).attr("rel");
			if(un==$.me){
				$(this).addClass("tume");
			st.find(".tuu").append("<a target='_blank' href='"+$.prefix+"/testUnit/"+this.id.split("_")[1]+"' class='tu0' id='u"+this.id+"'>"+$(this).text()+"</a>");
			}
		});
	}else if($.userType>=3){
	var gUsers = {};
	//获取组员列表,分别将
	$.getJSON($.prefix+"/user/tester",function(data){
		if(!data || data=="" || data.length==0){return;}
		for ( var i = 0; i < data.length; i++) {
			var u = data[i].name;
			var st=$("<div class='file_upload st' id='st_"+i+"' style='background-color:#FFF;'><div class='bold'>"+u+"</div><div class='tuu'></div></div>");
			gUsers[u] = st;
			$("#sendT").append(st);
		}
		$("#showTUS .tu0").each(function(){
			var un=$(this).attr("rel");if(un==$.me){$(this).addClass("tume");};
			gUsers[un].find(".tuu").append("<span class='tu0' id='u"+this.id+"'>"+$(this).text()+"</span>");
		});
		tuDrag($("#sendT .tu0"));
	}).error(function(){alert("获取测试组成员失败!");});
	}
}
function saveTU(){
	var j=[];
	$("#sendT .st").each(function(){
		var t={};t.n=$(this).find(".bold").text();t.us=[];
		$(this).find(".tu0").each(function(){t.us.push(this.id.split("_")[1]);});
		if(t.us.length>0){j.push(t);};
	});
	if(j.length==0){alert("任务已全部执行,无法再分配.");return;}
	else{
		$("#task_tu_json_h").html($.toJSON(j));
		abox("保存任务分配","处理中,请稍候……");
		$.post($.prefix+"/tasks/a_send", $("#s_form").serialize() ,function(data) {
			if(data=="ok"){abox("任务分配","<div class='reOk'>任务分配成功！ &nbsp;"+close+"</div>");}else{abox("任务分配","<div class='reErr'>任务分配保存失败. &nbsp;"+close+"</div>");};
		}).error(function(){abox("任务分配","<div class='reErr'>任务分配保存失败. &nbsp;"+close+"</div>");});
	}
}
function summary(){
	if($("#sendT .tu0").length>0){
		var c=confirm("还有未完成的测试单元,现在就汇总结果吗？");
		if(!c){return;}
	}
	abox("汇总任务问题","处理中,请稍候……");
	$.getJSON($.prefix+"/tasks/a_summary",{tid:$.tid,sys:$.sys},function(data){
		showSummary(data);
		$("#bt_summary,#bt_saveTU,#sendT").hide();
		$("#failCases,#bt_confirm,#finisher").show(10,function(){location.href="#ffcc";$.fancybox.close();});
	}).error(function(){abox("汇总任务问题","<div class='reErr'>汇总任务问题失败! &nbsp;"+close+"</div>");});
}
function showSummary(data){
	if(!data || data=="" || data.length==0){abox("汇总任务问题","处理完成,测试未发现任何问题. &nbsp;"+close);return;}
	for ( var i in data) {
		if(i=="attachs"){
			var attachs = "<div>测试附件:",at=data[i].split(",");
			for(var k=0;k<at.length;k++){
				attachs+="<a href='"+$.prefix+"/file/"+at[k]+"'>"+at[k]+"</a><br />";
			}
			attachs+="</div>";
			$(attachs).appendTo("#fCases");
			continue;
		}
		var c=data[i][0];
		var h=$("<div class='file_upload' style='background-color:#FFF;' id='qc_"+i+"'><div>"+i+". "+c.name
				+"</div><div class='blue' style='font-size:12px;padding:5px;'>"+c.info+"</div></div>");
		for ( var j = 1; j < data[i].length; j++) {
			var q=data[i][j];
			var h2=$("<div><span class='tu"+q.re+"'>"+q.phone+"</span> "+q.info+"</div>");
			h2.appendTo(h);
		}
		h.appendTo("#fCases");
	}
}
function confirmTU(){
	abox("测试结果提交","处理中,请稍候……");
	$.post($.prefix+"/tasks/a_confirm", {tid:$.tid,sys:$.sys,task_operator:$("#task_operator").val()},function(data) {
		var bt1 = "<a href=\"javascript:window.location='"+$.prefix+"/tasks/"+$.tid+"';\" class=\"aButton\">查看任务</a>";
		if(data=="ok"){abox("测试结果提交","<div class='reOk'>测试结果提交成功！ &nbsp;"+bt1+" <a href=\"javascript:window.location =('"+$.prefix+$.taskUrl+"');\" class=\"aButton\">返回列表</a></div>");}
		else{abox("测试结果提交","<div class='reErr'>测试结果提交失败. &nbsp;"+close+"</div>");};
	}).error(function(){abox("测试结果提交","<div class='reErr'>测试结果提交失败. &nbsp;"+close+"</div>");});
}
function finish(){
	abox("确认结果提交","处理中,请稍候……");
	$.post($.prefix+"/tasks/a_finish", $("#f_form").serialize(),function(data) {
		var bt1 = "<a href=\"javascript:window.location='"+$.prefix+"/tasks/"+$.tid+"';\" class=\"aButton\">查看任务</a>";
		if(data=="ok"){abox("确认结果提交","<div class='reOk'>确认结果提交成功！ &nbsp;"+bt1+" <a href=\"javascript:window.location =('"+$.prefix+$.taskUrl+"');\" class=\"aButton\">返回列表</a></div>");}
		else{abox("确认结果提交","<div class='reErr'>确认结果提交失败. &nbsp;"+close+"</div>");};
	}).error(function(){abox("确认结果提交","<div class='reErr'>确认结果提交失败. &nbsp;"+close+"</div>");});
}
function online(){
	abox("上线","处理中,请稍候……");
	$.post($.prefix+"/tasks/a_online", $("#o_form").serialize(),function(data) {
		var bt1 = "<a href=\"javascript:window.location='"+$.prefix+"/tasks/"+$.tid+"';\" class=\"aButton\">查看任务</a>";
		if(data=="ok"){abox("上线","<div class='reOk'>上线成功！ &nbsp;"+bt1+" <a href=\"javascript:window.location =('"+$.prefix+$.taskUrl+"');\" class=\"aButton\">返回列表</a></div>");}
		else{abox("上线","<div class='reErr'>上线失败. &nbsp;"+close+"</div>");};
	}).error(function(){abox("上线","<div class='reErr'>上线失败. &nbsp;"+close+"</div>");});
}
function pre(i){
	if(i==1){
		
	}
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
<div class="aboxTitle"><div>测试任务:<%=one.getName() %></div> </div>
<div class="aboxSub"><div style="color:#6E747B;float:left;padding-top:7px;"> 
当前状态: <span id="cState" class="blueBold"><%=one.getProp("state") %></span> 
待办人：<span id="operator" class="blueBold"><%=one.getProp("operator") %></span> 
<%if(userType>1){ 
	StringBuilder sb1 = new StringBuilder("优先级：<span id=\"cLevel\" class=\"");
	if(one.getLevel()==0){sb1.append("blueBold\">普通");}else{sb1.append("redBold\">").append(one.getLevel());}
	sb1.append("</span>");out.print(sb1);
}%>
</div>
<%if(userType>10){ 
	String ggid = String.valueOf(one.getId());
%>
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
    	<%if(Integer.parseInt(product.getProp("sys").toString())==2){%>
    	<div class="inBoxLine">测试入口URL: <span id="task_p_url_v" class="blueBold"><%=product.getUrl()%></span></div> 
    	<%} %>
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
		sb.append("'><a rel='").append(f.getId()).append("' href='").append(prefix).append("/gamefile/").append(f.getId()).append("' class=\"filename bold\">").append(f.getName()).append("</a>");
		if(userType>=3){
			sb.append("<span class=\"u_ok\">[ <a href='javascript:selectPhone(").append(i).append(");'>适配机型</a> ]</span>");
		}
		sb.append("<div class=\"groups\">");
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
//if(userType>1){
%>
<div class="inBox" id="infos">
    <div class="inBoxTitle">任务流程</div> 
    <div class="inBoxContent">
    	<%
    	Object logO = one.getProp("log");
    	if(logO!=null){
    		ArrayList<HashMap<String,Object>> logs = (ArrayList<HashMap<String,Object>>)logO; 
    		if(!logs.isEmpty()){
    			StringBuilder sb2 = new StringBuilder();
    	    	
    	    	Iterator<HashMap<String,Object>> itl = logs.iterator();
    	    	while(itl.hasNext()){
    	    		HashMap<String,Object> log = itl.next();StringUtil.getFormatDateString("yyyy-MM-dd hh:mm:ss",(Long)log.get("time"));
    	    		sb2.append("<div class='inBoxLine'>").append(log.get("user")).append(" ").append(StringUtil.getFormatDateString("yyyy-MM-dd hh:mm:ss",(Long)log.get("time"))).append(" ");
    	    		if(userType>1){
    	    			sb2.append(log.get("info"));
    	    		}else{
    	    			String s = log.get("info").toString();
    	    			int e = s.indexOf("-");
    	    			if(e>0){
    	    				sb2.append(s.substring(0,e));
    	    			}else{
    	    				sb2.append(s);
    	    			}
    	    		}
    	    		sb2.append("</div>");
    	    	}
    	    	out.print(sb2);
    		}
    	}
    	%>
    </div>
</div>
<% // }
//已创建
if(state==TTask.TASK_STATE_NEW && userType > 3){%>
<div id="appoint">
<form action="<%=prefix%>/tasks/a_p" method="post" id="p_form">
<p><label for="task_info">任务附加说明：</label><br />
<textarea id="task_info" name="task_info" rows="3" cols="3" style="height:60px;"></textarea></p>
<p>优先级调整：
<select name="task_level" id="task_level"><option value="0">普通</option><option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option></select>
</p>
<p>下一流程处理人：
<select name="task_operator"><option value="王朦朦">王朦朦</option><option value="夏丽惠">夏丽惠</option></select>
</p>
<input type="hidden" id="tid" name="tid" value="<%=one.getId()%>" />
<textarea rows="1" cols="1" class="hide" name="task_tu_json_h" id="task_tu_json_h"></textarea>
</form>
<p><a href="javascript:aSubmit('#p_form');" id="submitBT" class="aButton tx_center" style="width:60px;">分配任务</a> <a href="javascript:pre(1);" class="aButton tx_center">退回创建人</a> <a href="<%=prefix+"/tasks"+myPara%>" class="aButton">返回任务列表</a></p>
</div>

<%//转发TestUnit,或在TestUnit完成后汇总
}else if(state==TTask.TASK_STATE_TEST && tus !=null && !tus.isEmpty() && userType >= 2){
	String file = "";int i=0;
	StringBuilder sb = new StringBuilder();
	sb.append("<div class='inBox' id='tus'><div class='inBoxTitle'>测试单元 <span style='font-size:12px;font-weight:normal;'>(<span class='tu0'>待测</span><span class='tu2'>通过</span><span class='tu4'>部分通过</span><span class='tu9'>未通过</span>)</span></div><div class='inBoxContent'><div id='showTUS'>");
	Iterator<KObject> it = tus.iterator();
	while(it.hasNext()){
		KObject tu = it.next();
		if(!tu.getProp("gFile").equals(file)){
			if(!file.equals("")){sb.append("</div></div>");}
			sb.append("<div class='file_upload' style='background-color:#FFF;' id='fu_").append(i);
			sb.append("'><a href='").append(prefix).append("/gamefile/").append(tu.getProp("fileId")).append("' class=\"filename bold\">").append(tu.getProp("gFile")).append("</a><div class=\"groups\">");
			sb.append("<a target='_blank' id='tu_").append(tu.getId()).append("' rel='").append(tu.getProp("tester")).append("' href='").append(prefix).append("/testUnit/").append(tu.getId()).append("' class='tus tu").append(tu.getState()).append("'>").append(tu.getProp("phone")).append("</a>");
			file = (String)tu.getProp("gFile");
			i++;
		}else{
			sb.append("<a target='_blank' id='tu_").append(tu.getId()).append("' rel='").append(tu.getProp("tester")).append("' href='").append(prefix).append("/testUnit/").append(tu.getId()).append("' class='tus tu").append(tu.getState()).append("'>").append(tu.getProp("phone")).append("</a>");
		}
	}
	sb.append("</div></div></div><br /><div id='sendT'><div class='bold' style='padding-bottom:5px;'>待测试单元分配</div></div></div></div>");
	out.print(sb);
%>
<div class="inBox hide" id="failCases">
    <div class="inBoxTitle">测试问题汇总<a name="ffcc"></a> <span style='font-size:12px;font-weight:normal;'>(<span class='tu0'>未测</span><span class='tu2'>通过</span><span class='tu4'>部分通过</span><span class='tu9'>未通过</span>) </span></div> 
    <div class="inBoxContent" id="fCases">
    
    </div>
</div>
<div id="send">
<form action="<%=prefix%>/tasks/a_send" method="post" id="s_form">
<input type="hidden" id="tid" name="tid" value="<%=one.getId()%>" />
<textarea rows="1" cols="1" class="hide" name="task_tu_json_h" id="task_tu_json_h"></textarea>
</form>
<p>
<span id="finisher" class="hide">下一执行人:<select id="task_operator" name="task_operator"><option value="曹雨">曹雨</option></select><br /></span>
<% if(userType > 2){ %>
<a href='javascript:saveTU();' class='aButton tx_center' style='width:60px;' id="bt_saveTU">保存分配</a>
<a href='javascript:summary();' class='aButton tx_center' id="bt_summary">汇总测试结果</a>
<a href='javascript:confirmTU();' class='aButton tx_center' id="bt_confirm">测试结果提交</a>
<%}%>
<a href="<%=prefix+"/tasks"+myPara%>" class="aButton">返回任务列表</a></p></div>
<%//已执行结束,查看TestUnit
}else if(state==TTask.TASK_STATE_CONFIRM && userType>1){
	String file = "";int i=0;
	StringBuilder sb = new StringBuilder();
	sb.append("<div class='inBox' id='tus'><div class='inBoxTitle'>测试单元 <span style='font-size:12px;font-weight:normal;'>(<span class='tu0'>待测</span><span class='tu2'>通过</span><span class='tu4'>部分通过</span><span class='tu9'>未通过</span>)</span></div><div class='inBoxContent'><div id='showTUS'>");
	Iterator<KObject> it = tus.iterator();
	while(it.hasNext()){
		KObject tu = it.next();
		if(!tu.getProp("gFile").equals(file)){
			if(!file.equals("")){sb.append("</div></div>");}
			sb.append("<div class='file_upload' style='background-color:#FFF;' id='fu_").append(i);
			sb.append("'><a href='").append(prefix).append("/gamefile/").append(tu.getProp("fileId")).append("' class=\"filename bold\">").append(tu.getProp("gFile")).append("</a><div class=\"groups\">");
			sb.append("<a target='_blank' id='tu_").append(tu.getId()).append("' rel='").append(tu.getProp("tester")).append("' href='").append(prefix).append("/testUnit/").append(tu.getId()).append("' class='tus tu").append(tu.getState()).append("'>").append(tu.getProp("phone")).append("</a>");
			file = (String)tu.getProp("gFile");
			i++;
		}else{
			sb.append("<a target='_blank' id='tu_").append(tu.getId()).append("' rel='").append(tu.getProp("tester")).append("' href='").append(prefix).append("/testUnit/").append(tu.getId()).append("' class='tus tu").append(tu.getState()).append("'>").append(tu.getProp("phone")).append("</a>");
		}
	}
	sb.append("</div></div></div></div></div>");
	out.print(sb);

%>

<div class="inBox" id="failCases">
    <div class="inBoxTitle">测试问题汇总<a name="ffcc"></a> <span style='font-size:12px;font-weight:normal;'>(<span class='tu0'>未测</span><span class='tu2'>通过</span><span class='tu4'>部分通过</span><span class='tu9'>未通过</span>) </span></div> 
    <div class="inBoxContent" id="fCases">
    
    </div>
</div>
<br />
<div id="finish">
<%//由权限为4的人确认到准备上线状态
int isOnline = StringUtil.isDigits(one.getProp("isOnline"))?Integer.parseInt(one.getProp("isOnline").toString()):0;
if(isOnline==0 && (userType==4 || userType==99)){ %>
<form action="<%=prefix%>/tasks/a_finish" method="post" id="f_form">
<label for="tu_pass">确认测试结果：</label>
<select name="tu_pass" id="tu_pass"><option value="2">通过</option><option value="4">部分通过</option><option value="9">不通过</option><option value="-3">退回到组长</option><option value="-2">放弃</option></select><br />
下一执行人:<select id="task_operator" name="task_operator"><option value="田智龙">田智龙</option></select><span id="task_next"></span><br />
<label for="task_info">附加说明：</label><br />
<textarea id="task_info" name="task_info" rows="3" cols="3" style="height:60px;"></textarea>
<input type="hidden" id="tid" name="tid" value="<%=one.getId()%>" /><br />
</form>
<a href='javascript:finish();' class='aButton tx_center' id="bt_finish">确认结果</a>
<%}//由管理员操作上线
else if(userType==99) { %>
<form action="<%=prefix%>/tasks/a_online" method="post" id="o_form">
<label for="tu_re">确认上线：</label>
<select name="tu_re" id="tu_re"><option value="2">上线</option><option value="4">上线部分通过</option><option value="-3">退回</option><option value="-2">放弃</option></select><br />
<label for="task_info">附加说明：</label><br />
<textarea id="task_info" name="task_info" rows="3" cols="3" style="height:60px;"></textarea>
<input type="hidden" id="tid" name="tid" value="<%=one.getId()%>" /><br />
</form>
<a href='javascript:online();' class='aButton tx_center' id="bt_online">确认已上线</a>
<%} %>
<a href="<%=prefix+"/tasks"+myPara%>" class="aButton">返回任务列表</a></div>

<%//待反馈情况,厂家查看
}else if((state==TTask.TASK_STATE_NEED_MOD && userType>0 && user.getProp("company").equals(one.getProp("company"))) || userType>=3){%>
<div class="inBox" id="failCases">
    <div class="inBoxTitle">测试问题汇总<a name="ffcc"></a> <span style='font-size:12px;font-weight:normal;'>(<span class='tu0'>未测</span><span class='tu2'>通过</span><span class='tu4'>部分通过</span><span class='tu9'>未通过</span>) </span></div> 
    <div class="inBoxContent" id="fCases">
    
    </div>
</div>
<br />
<div id="feedback">
<%if(userType == 1 || userType ==99){ %>
<a href="<%=prefix+"/tasks/add?pid="+one.getProp("PID")+((ismy)?"&ismy=true":"")%>" class="aButton">反馈并发起修改后的测试</a>
<a href="<%=prefix+"/topic/add/company?pid="+one.getProp("PID")+"&tid="+one.getId()%>" class="aButton">对此任务发起回复讨论</a>
<%} %>
<a href="<%=prefix+"/tasks"+myPara%>" class="aButton">返回任务列表</a></div>
<%//厂家或访客查看情况,state<2的情况下
}else {%>

<%}%>
</div>



<div id="hide" class="hide"></div>
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>