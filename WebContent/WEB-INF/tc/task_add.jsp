<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.k99k.khunter.*,com.k99k.tools.*,java.util.*" session="false" %>
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
HashMap<String,String> pmap = (HashMap<String,String>)data.getData("pmap");
ArrayList<KObject> files = (data.getData("files") == null) ? null : (ArrayList<KObject>)data.getData("files");
ArrayList<HashMap<String,String>> fee = null;
Object feeobj = data.getData("fee");
if(feeobj!=null){
	fee = (ArrayList<HashMap<String,String>>)feeobj;
}
int userType = user.getType();
//String tp = request.getParameter("tp");
Boolean ismy = request.getParameter("ismy")!=null && request.getParameter("ismy").equals("true");
out.print(JSPOut.out("head0","0","创建新测试任务"));%>
<link rel="stylesheet" href="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.css" type="text/css" media="screen" />
<link rel="stylesheet" href="<%=sPrefix %>/css/jquery.autocomplete.css" type="text/css" media="screen" />
<script src="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload_tc.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.json-2.3.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.autocomplete.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/tc.add_task.js" type="text/javascript"></script>
<script type="text/javascript">
<!--
$.sPrefix = "<%=sPrefix %>";$.prefix="<%=prefix %>";
$.isMy = <%=(ismy)?"true":"false" %>;
$.userType = <%=userType %>;
$.company = "<%=user.getProp("company") %>";
$(function(){
	var tar = ($.isMy) ? "#side_mytask a" : "#side_task a";
	$(tar).addClass("sideON");
	
//处理产品信息
pJSON.company = '<%=pmap.get("venderShortName")%>';
pJSON.name = '<%=pmap.get("gameName")%>';
pJSON.oldId = '<%=String.valueOf(pmap.get("oldId"))%>';
pJSON.netPort = 0;
pJSON.netType = <% 
int netType = 3;Object gType = pmap.get("gameClass");
int gameType = StringUtil.isDigits(gType) ? Integer.parseInt(String.valueOf(gType)) : 12;
if(gameType == 11){
	//单机游戏
	netType = 0;
}else if(gameType == 12){
	//联网游戏
	netType = 1;
}else{
	netType = 3;
}
out.print(netType+";");
%>
pJSON._id = <%=String.valueOf(pmap.get("gameId"))%>;
pJSON.newp = 2;
pJSON.sys = <% 
int sys = 6;//Object os = pmap.get("gameOSName");
Object os = pmap.get("gameOS");
int osType = StringUtil.isDigits(os) ? Integer.parseInt(String.valueOf(os)) : 3;
if(osType == 10){
	//JAVA
	sys = 0;
}else if(osType == 3){
	//android
	sys = 1;
}else if(osType == 2){ //接口没有2这个值,按wap处理
	sys = 2;
}else if(osType == 11){
	//brew
	sys = 3;
}else if(osType == 4){
	//mobile
	sys = 4;
}else if(osType == 9){
	//ce 实际为windowns phone
	sys = 5;
}else{
	sys = 6;
}
out.print(sys+";");
%>
pJSON.type = <% 
int type = 0;Object pTypeStr = pmap.get("payType");
Object isPack = pmap.get("isPackage");
int pType = StringUtil.isDigits(pTypeStr) ? Integer.parseInt(String.valueOf(pTypeStr)) : 0;
int isPackage = StringUtil.isDigits(isPack) ? Integer.parseInt(String.valueOf(isPack)) : 0;
if(isPackage == 1){
	type = 4;//进包
}else if(pType ==2){
	type = 1; //单机道具
//}else if(pType.indexOf("下载")>=0){ //下载时按次计费
//	type = 3;
}else if(pType==0){
	type = 0; //单机免费
}else if(pType==3){
	type = 1; //网游道具按次
}else if(pType ==5){
	type = 0; //网游免费
}
out.print(type+";");
%>
pJSON.url = '<%=pmap.get("visitURL")%>';
pJSON.gameClass = '<%=pmap.get("gameClassName")%>';
pJSON.cpID = '<%=pmap.get("venderCode")%>';
pJSON.communityGame = <%=String.valueOf(pmap.get("isSNSGame"))%>;
pJSON.serviceFeeType = '<%=pmap.get("feeTypeName")%>';
pJSON.synUrl = '<%=pmap.get("syncURL")%>';
<%
String feeInfo = "";
if(fee !=null){
	feeInfo = JSON.write(fee);
	out.print("pJSON.feeInfo = '"+feeInfo.replaceAll("'", "\'")+"';");
}else{
	out.print("pJSON.feeInfo = '';");
}
%>

//处理请求
$.validator.dealAjax = {
	bt:$("#submitBT"),
	loading:function(){abox("创建任务","请稍候...");},
	ok:function(data){
		if(!isNaN(data)){
			var bt1 = "<a href=\"javascript:window.location='<%=prefix%>/tasks/"+data+"';\" class=\"aButton\">查看任务</a>",taskUrl=($.isMy)?"/tasks/my":"/tasks";
			abox("创建任务","<div class='reOk'>创建任务成功！ &nbsp;"+bt1+" <a href=\"javascript:window.location =('<%=prefix %>"+taskUrl+"');\" class=\"aButton\">返回列表</a></div>");
		}else{abox("创建任务","<div class='reErr'>创建任务失败! "+data+" &nbsp;<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a></div>");};
	},
	err:function(xhr){
		abox("创建任务","<div class='reErr'>创建任务失败! 错误码:"+xhr.responseText+" &nbsp;<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a></div>");
	}
};
$('#add_form').validate({
    rules: {
		task_info: {required:true}
    }
});

var sucFn = function(file, serverData){
	var re = serverData;
	swfu.startProg = false;
	var i  =($.hasFileIndex) ? ($.hasFileIndex+file.index) :file.index;
	if(re.length>=5){
	swfok("<div class='file_upload' id='fu_"+i+"'><span class='filename'>"+file.name+"</span><span class='newname hide'>"+re+"</span><span class='size hide'>"+file.size+"</span> <span class='u_ok'><span class='greenBold'>上传成功!</span> [ <a href='javascript:delFile(\""+i+"\");'>删除 </a> ][ <a href='javascript:choosePhType2(\""+i+"\");'>选择适配</a> ]<span class=\"files_name\">"+file.name+"</span></span></div>");
	$.hasFileIndex = i;
	}else{swfok("<div class='file_upload file_upload_ERR'>"+file.name+" 上传失败!</div>");}
};
var upFileType = "*.*";
if(pJSON.sys==0){
	upFileType = "*.jar";
}else if(pJSON.sys==1){
	upFileType = "*.apk";
}
initUpload("<%=user.getName() %>",sucFn,upFileType);

<%
int maxFileNum = 1;
if(files != null){
	String lastFileName = files.get(files.size()-1).getProp("fileName").toString();
	int s = lastFileName.lastIndexOf("_")+1;
	int e = lastFileName.lastIndexOf(".");
	maxFileNum = 1+Integer.parseInt(lastFileName.substring(s,e));
}
%>
swfu.newfile = function(file){
	var t = (file.type && file.type.length>2) ? file.type : ".apk";
	return pJSON.oldId+"_"+(file.index+<%=maxFileNum %>)+t;
};


function feeInfo(fee,to){
	if(fee != '' && fee.indexOf('{')>0){
		var f = $.parseJSON(fee);
		if(Object.prototype.toString.apply(f) === '[object Array]'){
			var tb = "<table id='feeList' width='100%' class='table_list' cellpadding='0' cellspacing='1'>";
			tb = tb+"<tr><th>名称</th><th>单价</th><th>功能</th><th>触发条件</th><th>软/硬</th></tr>";
			$.each(f,function(){
				var tr = "<tr><td>"+this.consumeName+"</td><td>"+this.price+"</td><td>"+this.description+"</td><td>"+this.trigerCondition+"</td><td>"+this.feeType+"</td></tr>";
				tb = tb + tr;
			});
			tb=tb+"</table>";
			$(to).html(tb);
		}
	}
}

feeInfo($("#task_p_fee_v").text(),"#feeInfoTable");

});
-->
</script>
<%out.print(JSPOut.out("main0",new String[]{"0","1"},new String[]{user.getName(),user.getProp("company").toString()})); %>
<jsp:include page="sidenav.jsp" flush="false" > 
  <jsp:param name="lv" value="<%=user.getLevel() %>" /> 
  <jsp:param name="type" value="<%=userType %>" /> 
  <jsp:param name="gg" value='<%=user.getProp("newNews").toString() %>' /> 
  <jsp:param name="tt" value='<%=user.getProp("newTasks").toString() %>' /> 
</jsp:include>
		<div id="mainContent">
<div class="abox">
<div class="aboxTitle">创建新任务</div>
<div class="aboxContent" id="task_new">

<div class="inBox" id="productFS3">
    <div class="inBoxTitle">产品信息</div> 
    <div class="inBoxContent">
   		<div class="inBoxLine">公司:<span id="task_cp_v" class="blueBold"><%=pmap.get("cpName") %></span> 产品ID: <span id="task_p_id_v" class="blueBold"><%=String.valueOf(pmap.get("gameId")) %></span> 
   		产品名称: <span id="task_name_v" class="blueBold"><%=pmap.get("gameName") %></span> 操作系统: <span id="task_p_sys_v" class="blueBold"><%=pmap.get("gameOSName") %></span><span id="task_p_cpid_v" class="hide"><%=pmap.get("venderCode") %></span></div> 
    	<div class="inBoxLine">产品计费类型: <span id="task_p_type_v" class="blueBold"><%=pmap.get("payTypeName") %></span>  联网情况: <span id="task_p_net_v" class="blueBold"><%=pmap.get("gameClassName") %></span> 产品类型: <span id="task_p_gclass_v" class="blueBold"><%=pmap.get("gameTypeName") %></span></div> 
    	<div class="inBoxLine">同步地址:<span id="task_p_synurl_v" class="blueBold"><%=StringUtil.isStringWithLen(pmap.get("synUrl"), 1)?pmap.get("synUrl"):"" %></span><br />WAP入口地址:<span id="task_p_url" class="blueBold"><%=StringUtil.isStringWithLen(pmap.get("wapUrl"), 1)?pmap.get("wapUrl"):"" %></span></div>
    	<div class="inBoxLine">计费点描述: <br /><span id="task_p_fee_v" class="hide"><%=feeInfo %></span><div id="feeInfoTable"></div></div>

    </div>
</div>

<div class="inBox" id="uploadFS">
    <div class="inBoxTitle">游戏实体包上传</div> 
    <div class="red">
    注意提交测试前请确认游戏第一帧为最新版爱游戏LOGO[<a href='http://180.96.63.69/Documents/SDK_DownloadGuid.html' target='_blank'>下载</a>],如下图：<br /><img alt="logo" src="/tc/images/egame_logo.jpg">
    </div>
    <div class="inBoxContent">
	<form name="fileupload" id="fileupload" action="<%=prefix %>/upload" method="post" enctype="multipart/form-data">
		<div id="swfBT" class="inBoxLine">
			<div id="spanSWFUploadButton">请稍候...</div> 
			<span id="uploadInfo" style="font-size:14px;"> &nbsp;文件最大不超过200M,按住<span class="purpleBold">Ctrl键</span>可一次选择多个文件上传</span>
		</div>
		<div id="upFiles"></div>
		<br /><a href="javascript:filesSet();" class="aButton">确定</a> 
	</form>
    </div>
</div>

</div>
</div>

<div id="hide" class="hide">
<div id="taskFS">
<form action="<%=prefix%>/tasks/a_a" method="post" id="add_form">
<p><label for="task_info">任务说明：<span class="red">请填入需要测试过程中注意的问题,如不适配的android版本，分辨率等，修改后提交请说明具体的修改之处</span></label><br />
<textarea id="task_info" name="task_info" rows="3" cols="3" style="height:60px;"></textarea></p>
<% if(userType>1){ %>
<p>任务优先级：
<select name="task_level"><option value="0">普通</option><option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option></select>
</p>
<% }%>
<p>下一流程处理人：
<select name="task_operator"><option value="曹雨">曹雨</option></select>
</p>
<input type="hidden" id="task_type_h" name="task_type_h" value="<%=pmap.get("task_type") %>" />
<!-- input type="hidden" id="isUpdate" name="isUpdate" value="<  %=(StringUtil.isDigits(tp)?tp:"0") %>"  -->
<textarea rows="1" cols="1" class="hide" name="task_p_json_h" id="task_p_json_h"></textarea>
</form>
<p><a href="javascript:aSubmit();" id="submitBT" class="aButton tx_center" style="width:60px;">创建任务</a> </p>
</div>
<!-- end of hide -->
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>