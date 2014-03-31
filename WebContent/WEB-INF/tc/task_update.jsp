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
ArrayList<KObject> passfiles = (data.getData("passFileParas")==null)?null:(ArrayList<KObject>)data.getData("passFileParas");
ArrayList<KObject> files = null;

ArrayList<HashMap<String,String>> fee = null;
Object feeobj = data.getData("fee");
if(feeobj!=null){
	fee = (ArrayList<HashMap<String,String>>)feeobj;
}
int userType = user.getType();
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
pJSON.netPort = 0;
pJSON.netType = <% 
String netType = "3";Object gType = pmap.get("gameTypeName");
if(gType.equals("单机游戏")){
	netType = "0";
}else if(gType.equals("联网游戏")){
	netType = "1";
}else if(gType.equals("WAP游戏")){
	netType = "2";
}else{
	netType = "3";
}
out.print(netType+";");
%>
pJSON._id = <%=String.valueOf(pmap.get("gameId"))%>;
pJSON.newp = 2;
pJSON.sys = <% 
String sys = "6";Object os = pmap.get("gameOSName");
if(os.equals("JAVA")){
	sys = "0";
}else if(os.equals("Android")){
	sys = "1";
}else if(netType.equals("2")){
	sys = "2";
}else if(os.equals("BREW")){
	sys = "3";
}else if(os.equals("Windows Mobile")){
	sys = "4";
}else if(os.equals("Windows CE")){
	sys = "5";
}else{
	sys = "6";
}
out.print(sys+";");
%>
pJSON.type = <% 
String type = "0";String pType = pmap.get("payTypeName").toString();Object isPack = pmap.get("isPackage");
if(!StringUtil.toStrNotNull(isPack, "1").equals("0")){
	type = "4";
}else if(pType.indexOf("关卡或道具")>=0){
	type = "1";
}else if(pType.indexOf("下载")>=0){ //下载时按次计费
	type = "3";
}else if(pType.indexOf("免费")>=0){
	type = "0";
}else if(pType.indexOf("包月")>=0){
	type = "5";
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

$.hasFileIndex=0;
sucFn = function(file, serverData){
	var re = serverData;
	swfu.startProg = false;
	var i  =$.hasFileIndex;
	if(swfu.currentFile == "newAdd"){
		i = addFileStartNum + file.index;
	}
	var reShow = "";
	if(re.length>=5){
		reShow="<div class='file_upload' id='fu_"+i+"'><span class='filename'>"+file.name+"</span><span class='newname hide'>"+re+"</span><span class='size hide'>"+file.size+"</span> <span class='u_ok'><span class='greenBold'>上传成功!</span> [ <a href='javascript:delFile(\""+i+"\");'>删除 </a> ][ <a href='javascript:choosePhType2(\""+i+"\");'>选择适配</a> ]<span class=\"files_name\">"+file.name+"</span></span></div>";
	}else{reShow = ("<div class='file_upload file_upload_ERR'>"+file.name+" 上传失败!</div>");}
	$("#uploadInfo").html("");
	if(swfu.currentFile == "newAdd"){
		$("#newPk").append(reShow);
		swfu.end =null;
	}else{
		$("#fileUpload"+i).html(reShow).show();
		swfu.end = function(){
			$("#hide").prepend($("#fileupload"));
		}
	}
};
upFileType = "*.*";
if(pJSON.sys==0){
	upFileType = "*.jar";
}else if(pJSON.sys==1){
	upFileType = "*.apk";
}
<%
int maxFileNum = 0;
if(files != null){
	String lastFileName = files.get(files.size()-1).getProp("fileName").toString();
	maxFileNum = 1+Integer.parseInt(lastFileName.substring(lastFileName.lastIndexOf("_")+1,lastFileName.lastIndexOf("\\.")));
}
%>
maxQueueNum = 1;
addFileStartNum = 0;
queueCheck = function (numFilesSelected, numFilesQueued) {
	if (numFilesQueued > 0 ) {
		if(numFilesQueued > maxQueueNum){
			alert("超过上传包的最大数量！");
		}else{
			this.startUpload();return;
		}
	}
}
initUpload("<%=user.getName() %>",sucFn,upFileType,null,null,null,queueCheck);

swfu.newfile = function(file){
	if(swfu.currentFile == "newAdd"){
		return 	pJSON._id+"_"+(addFileStartNum+file.index)+".apk";
	}
	return swfu.currentFile;
};

feeInfo($("#task_p_fee_v").text(),"#feeInfoTable");
showFileParas();
});
function feeInfo(fee,to){
	if(fee != '' && fee.indexOf('{')>0){
		var f = $.parseJSON(fee);
		if(Object.prototype.toString.apply(f) === '[object Array]'){
			var tb = "<table id='feeList' width='100%' class='table_list' cellpadding='0' cellspacing='1'>";
			tb = tb+"<tr><th>名称</th><th>单价</th><th>功能</th><th>购买路径</th><th>触发条件</th><th>软/硬</th><th>短代</th></tr>";
			$.each(f,function(){
				var tr = "<tr><td>"+this.consumeName+"</td><td>"+this.price+"</td><td>"+this.description+"</td><td>"+this.buyGuide+"</td><td>"+this.trigerCondition+"</td><td>"+this.feeType+"</td><td><a href=\"javascript:abox('短代代码 - "+this.consumeName+"','"+this.smcode+"');\">查看</a></td></tr>";
				tb = tb + tr;
			});
			tb=tb+"</table>";
			$(to).html(tb);
		}
	}
}
function showFileParas(){
	$(".txtBox2").each(function(i){
		var a=$(this).text().split("_");
		if(a.length==2){
			$(this).text(a[1]).addClass("txtBox_"+a[0]);
		}
	});
}
function getApkNum(apkName){
	var s=	apkName.indexOf('_')+1,e=apkName.indexOf('.');
	return parseInt(apkName.substring(s,e));
}
function maxFileNum(){
	var max = 0;
	$("#files").find(".filename").each(function(i){
		var rel = $(this).attr("rel");
		if(rel){
			var fi = getApkNum(rel);
			if(fi>max){max=fi;}
		}
	});
	$("#newPk").find(".newname").each(function(i){
		var fi = getApkNum(this.innerHTML);
		if(fi>max){max=fi;}
	});
	return max;
}
function showUploadBT(cfuId,fileName){
	if(!fileName){
		swfu.currentFile = "newAdd";
		addFileStartNum =maxFileNum()+1; 
		maxQueueNum=50;
		$("#cfu_AddNew").append($("#fileupload"));
	}else{
		swfu.currentFile = fileName;
		maxQueueNum=1;
		$("#cfu_"+cfuId).append($("#fileupload"));
		$.hasFileIndex=cfuId;
	}
}
//-------------------------------------

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
   		<div class="inBoxLine">公司:<span id="task_name_v" class="blueBold"><%=pmap.get("cpName") %></span> 产品ID: <span id="task_p_id_v" class="blueBold"><%=String.valueOf(pmap.get("gameId")) %></span> 
   		产品名称: <span id="task_name_v" class="blueBold"><%=pmap.get("gameName") %></span> 操作系统: <span id="task_p_sys_v" class="blueBold"><%=pmap.get("gameOSName") %></span><span id="task_p_cpid_v" class="hide"><%=pmap.get("venderCode") %></span></div> 
    	<div class="inBoxLine">产品计费类型: <span id="task_p_type_v" class="blueBold"><%=pmap.get("payTypeName") %></span> 联网情况: <span id="task_p_net_v" class="blueBold"><%=pmap.get("gameClassName") %></span> 产品类型: <span id="task_p_gclass_v" class="blueBold"><%=pmap.get("gameTypeName") %></span></div> 
    	<div class="inBoxLine">同步地址: <span id="task_p_synurl_v" class="blueBold"><%=StringUtil.isStringWithLen(pmap.get("synUrl"), 1)?pmap.get("synUrl"):"" %></span><br />WAP入口地址:<span id="task_p_url" class="blueBold"><%=StringUtil.isStringWithLen(pmap.get("wapUrl"), 1)?pmap.get("wapUrl"):"" %></span></div>
    	<div class="inBoxLine">计费点描述: <br /><span id="task_p_fee_v" class="hide"><%=feeInfo %></span><div id="feeInfoTable"></div></div>

    </div>
</div>
<%
if(passfiles != null && !passfiles.isEmpty()){
StringBuilder sb = new StringBuilder();
	sb.append("<div class='inBox' id='files'><div class='inBoxTitle'>已通过实体包</div><div class='inBoxContent'>");
	Iterator<KObject> it = passfiles.iterator();int i = 0;
	while(it.hasNext()){
		KObject f=it.next();
		sb.append(" <div style='background-color:#FFF;padding:5px;' id='cfu_").append(i);
		sb.append("'><a rel='").append(f.getProp("fileName")).append("@").append(f.getId()).append("' href='").append(prefix).append("/gamefile/").append(f.getId()).append("' class=\"filename bold\">").append(f.getName()).append("</a>");
		sb.append(" - <a href='javascript:showUploadBT(")
				.append(i).append(",\"")
				.append(f.getProp("fileName")).append("\");' class='aButton'>更新此实体包</a>");
/* 		if(userType>=3){
			sb.append("<span class=\"u_ok\">[ <a href='javascript:selectPhone(").append(i).append(");'>适配机型</a> ]</span>");
		} */
		sb.append("<div class=\"groups\">");
		
		//显示已通过包
		String[] passFs = f.getProp("passFileParas").toString().split("\\|");
		for(int j=1;j<passFs.length;j++){
			String g = passFs[j];
			sb.append("<span class='txtBox2'>").append(g).append("</span>");
		}
		sb.append("</div><div id='fileUpload").append(i).append("' class='updateFileUpload hide'></div></div>");
		i++;
	}
	sb.append("</div></div>");
	out.print(sb);
}
%>
<div class="inBox" id="uploadFS">
    <div class="inBoxTitle">补充新的适配包 <span class="red bold">注意！此处仅为补充新的适配使用，如果仅需要对已有包更新，请点击上方对应包后面的更新按钮</span></div> 
    <div class="inBoxContent" id="newPk">
		<a href="javascript:showUploadBT(100);" id="addNewPk" class="aButton">增加新适配的实体包</a>
		<div id="cfu_AddNew"></div>
    </div>
</div>
<div class="u_ok">
<br /><a href="javascript:filesSet('#task_new');" class="aButton">确定</a> 
</div>
</div>
</div>

<div id="hide" class="hide">
	<form name="fileupload" id="fileupload" action="<%=prefix %>/upload" method="post" enctype="multipart/form-data">
		<div id="swfBT" class="inBoxLine txtBox_1">
			<div id="spanSWFUploadButton">请稍候...</div> 
			<span id="uploadInfo" style="font-size:14px;"> &nbsp;文件最大不超过200M</span>
		</div>
	</form>
<div id="taskFS">
<form action="<%=prefix%>/tasks/a_a" method="post" id="add_form">
<p><label for="task_info">任务说明：<span class="red">请填入需要测试过程中注意的问题,如不适配的android版本，分辨率等，修改后提交请说明具体的修改之处</span></label><br />
<textarea id="task_info" name="task_info" rows="3" cols="3" style="height:60px;"></textarea></p>
<p>下一流程处理人：
<select name="task_operator"><option value="曹雨">曹雨</option></select>
</p>
<input type="hidden" id="is_update" name="is_update" value="true" />
<input type="hidden" id="task_type_h" name="task_type_h" value="<%=pmap.get("task_type") %>" />
<textarea rows="1" cols="1" class="hide" name="task_p_json_h" id="task_p_json_h"></textarea>
</form>
<p><a href="javascript:aSubmit();" id="submitBT" class="aButton tx_center" style="width:100px;">创建更新任务</a> </p>
</div>
<!-- end of hide -->
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>