<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.k99k.khunter.*,com.k99k.tools.*" session="false" %>
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
Boolean ismy = request.getParameter("ismy")!=null && request.getParameter("ismy").equals("true");
out.print(JSPOut.out("head0","0","创建新任务"));%>
<link rel="stylesheet" href="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.css" type="text/css" media="screen" />
<link rel="stylesheet" href="<%=sPrefix %>/css/jquery.autocomplete.css" type="text/css" media="screen" />
<script src="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload_tc.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.autocomplete.js" type="text/javascript"></script>
<script type="text/javascript">
$.sPrefix = "<%=sPrefix %>";$.prefix="<%=prefix %>";
$.isMy = <%=(ismy)?"true":"false" %>;
$.userType = <%=user.getType() %>;
$.company = "<%=user.getProp("company") %>";
$(function(){
	var tar = ($.isMy) ? "#side_mytask a" : "#side_task a";
	$(tar).addClass("sideON");
	//是否显示选择公司
	if($.userType>=4){$("#chooseCompany")[0].need = true;gs(true);}
	$(".prev1").hide();
	//任务类型
	$("input[name='task_type']").click(function(){
		var t = $(this).val();$.t = t;
		if(t<=1){$("#productFS2").appendTo($("#hide"));$("#productFS1").appendTo($("#task_new"));}
		else{$("#productFS1").appendTo($("#hide"));$("#productFS2").appendTo($("#task_new"));}
	});
	//产品
	var acc = $("#task_p_acc0").parent();acc.hide();
	$("input[name='task_p_net']").click(function(){if($(this).val()!=0){acc.show();}else{acc.hide();}});
	
	
//处理请求
$.validator.dealAjax = {
	bt:$("#submitBT"),
	loading:function(){abox("创建任务","请稍候...");},
	ok:function(data){
		if(!isNaN(data)){
			var bt1 = "<a href=\"javascript:window.location='<%=prefix%>/tasks/"+data+"';\" class=\"aButton\">查看任务</a>";
			abox("创建任务","<div class='reOk'>创建任务成功！ &nbsp;"+bt1+" <a href=\"javascript:window.location =('<%=prefix %>/tasks');\" class=\"aButton\">返回列表</a></div>");
		}else{abox("创建任务","<div class='reErr'>创建任务失败! &nbsp;<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a></div>");};
	},
	err:function(){
		abox("创建任务","<div class='reErr'>创建任务失败! &nbsp;<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a></div>");
	}
};
$.validator.setDefaults({
	//处理radio选择的错误提示
	errorPlacement : function(label, element){
	if(element.attr("type")=="radio"){
		var n = $("input[name='"+element.attr("name")+"']").last().next();var t=(n && n[0].nodeName.toUpperCase()=="LABEL")?n:n.prev();
	t.after(label);}else{element.after(label);}
	}
});

$.validator.over = function(form){
	switch (form.id) {
	case "productFrom":
		$("#productFrom input,#productFrom select,#productFrom textarea").each(function(i){
			var v = (this.nodeName=="SELECT")?$(this).find("option:selected").text():($(this).attr("type")=="radio")?$("input[name='"+this.name+"']:checked").next().text():$(this).val();
			$("#"+this.name+"_v").html(v);$("#"+this.name+"_h").val($(this).val());
		});
		pJSON.name = $("#task_name").val();
		pJSON.productID = $("#task_p_id").val();
		pJSON.sys = $("#task_p_sys").val();
		pJSON.type = $("#task_p_type").val();
		pJSON.netType = $("input[name='task_p_net']:checked").val();
		pJSON.netPort = (pJSON.netType>0)?$("input[name='task_p_acc']:checked").val():0;
		pJSON.feeInfo = $("#task_p_fee").val();
		pJSON.company = ($("#task_company").val() == "")?$.company:$("#task_company").val();
		next(1);
		$("#productFS1").appendTo($("#hide"));$("#productFS3").appendTo($("#task_new"));$("#chooseType").hide();
		break;
	default:
		break;
	}
}

$('#productFrom').validate({
    rules: {
		task_name: {required:true,rangelength:[1,500]},
		task_p_id: {required:true,number:true},
		task_p_sys: {required:true},
		task_p_type: {required:true},
		task_p_net: {required:true},
		task_p_fee: {required:true,rangelength:[1,1500]}
    }
});
var sucFn = function(file, serverData){
	var re = serverData;
	swfu.startProg = false;
	var i  =($.hasFileIndex) ? ($.hasFileIndex+file.index) :file.index;
	if(re.length>=file.name.length){
	swfok("<div class='file_upload' id='fu_"+i+"'>"+file.name+" <span class='u_ok'><span class='greenBold'>上传成功!</span> [ <a href='javascript:delFile(\""+i+"\");'>删除 </a> ][ <a href='javascript:selectPhone(\""+i+"\");'>选择机型 </a> ]<span class=\"files_name\">"+file.name+"</span></span></div>");
	}else{swfok("<div class='file_upload file_upload_ERR'>"+file.name+" 上传失败!</div>");}
};
initUpload("<%=user.getName() %>",sucFn,"*.apk;*.jar;*.jad;*.zip");
$("#task_company").autocomplete("<%=prefix %>/company/find",
	{cacheLength:20,matchSubset:1,matchContains:1,minChars:2,
	formatItem:function(row){return row[1];},
	onItemSelect:function(li){$("#task_company").val($(li).text());},
	chk:function(v){return (escape(v).indexOf("%u") < 0);}
});
$("#task_p_search").autocomplete("<%=prefix %>/product/find",
	{cacheLength:20,matchSubset:1,matchContains:1,minChars:2,
	formatItem:function(row){return row[1];},
	onItemSelect:function(li){$("#task_p_search").val($(li).text());},
	chk:function(v){if($("#task_company").val().length<2){alert("请先确定[公司],再选择[产品]!");$("#task_p_search").val("");$("#task_company").focus();return false;}else{return (escape(v).indexOf("%u") < 0)};},
	extraParams:addCompany
});
});
//-------------------------------------
var pJSON = {};
//-----------机型选择-------------------
var phoneType = {0:"240x320",1:"320x480",2:"240x400",3:"480x800",4:"480x854",5:"480x960",11:"代表机型",12:"其他"};
var allPData = [];
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
		$.getJSON("<%=prefix %>/phone/json?s="+pJSON.sys,function(data){
			if(data==""){alert("产品操作系统不正确.请返回上一步重设.");return;}
			for(var i=1,j=data.length;i<j;i++){
				var gg = $("<a class=\"aButton phoneCate\" href=\"javascript:showGroup("+data[i].g+");\" id='"+data[i].g+"'>"+phoneType[data[i].g]+"<\/a>");
				$("#phoneCates").after(gg);
			}
			allPData = data;
			$("#phone_fast").keyup(function(e){scPh(e);});
			addP2Group(data);
		});
	}else{clearIn();
		$("#fu_"+i).find(".txtBox").each(function(){
			var a = this.id.split("_");$("#p_"+a[1]+"_"+a[2])[0].io();
		});
	$("#fu_"+i).find(".sok").remove();}
	$("#choosePhone")[0].fu = i;
	$("#choosePhone").appendTo($("#fu_"+i));
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
	if(q==""){resetPh();return;}
	$("#g"+cGroup).hide();
	cGroup = 999;
	var gg=$("#g999");gg.show();
	q = q.toLowerCase();
	for ( var i = 1; i < allPData.length; i++) {
		for ( var j = 0; j < allPData[i].d.length; j++) {
			var e = $("#p_"+i+"_"+j)[0];
			if(allPData[i].d[j].toLowerCase().indexOf(q)>=0){
				e.out();
			}else if(e.state==2){e.reset();}
		}
	}
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
	return p;
}
function resetPh(){
	$("#g999").find(".phone").each(function(i){
		this.reset();
	});
	showGroup(0);
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
	if($("#td_out").find("#g"+i).length>0){
		$("#g"+cGroup).hide();
		$("#g"+i).show();
		cGroup = i;
	}
}
//--------------------
function addCompany(){
	return {c:$("#task_company").val()};
}
function aSubmit(){
	var ff = [];
	$(".files_name").each(function(){
		ff.push(encodeURIComponent($(this).text()));
	});
	//console.log(ff);
	$("#news_files").val(ff.join(","));
	$("#news_form").submit();
};
function saveP(){$('#productFrom').submit();}
function editP(){
	var e = ($.t<=1)?$('#productFS1'):$('#productFS2');
	e.appendTo($("#task_new"));$('#productFS3').appendTo($("#hide"));
}
function pSelect(){
	$.getJSON("<%=prefix %>/product/one?p="+encodeURI($("#task_p_search").val()),function(data){
		if(!data || data==""){alert("产品不存在!请确认产品名称已正确输入.");return;}
		else{
			$("#task_p_json_h").val(data);
			pJSON = data;
			$("#task_name_v").text(data.name);
			$("#task_p_id_v").text(data.productID);
			$("#task_p_sys_v").text($("#task_p_sys > option[value="+data.sys+"]").text());
			$("#task_p_type_v").text($("#task_p_type > option[value="+data.type+"]").text());
			$("#task_p_net_v").text($("label[for='task_p_net"+data.netPort+"']").text());
			$("#task_p_acc_v").text($("label[for='task_p_acc"+data.netPort+"']").text());
			$("#task_p_fee_v").text(data.feeInfo);
			$("#productFS2").appendTo($("#hide"));$("#productFS3").appendTo($("#task_new"));$("#chooseType").hide();
		}
		next(1);
	}).error(function(){alert("查找产品出错!请刷新页面或稍后再试.");});
}
function next(i){
	switch (i) {
	case 1:
		$(".prev1,.next1,#p_e").show();
		gs(false);
		break;
	case 2:
		$(".next1,#p_e").hide();$("#swfBT,.u_ok").show();
		if($("#task_p_sys_v").text()=="WAP"){$("#fileupload").hide();$("#urlSet").show();}else{$("#urlSet").hide();$("#fileupload").show();};
		$("#task_new").append($("#uploadFS"));
		break;

	default:
		break;
	}
}
function gs(show){
	if( $("#chooseCompany")[0].need){$("#c_ok").remove();if(show){$("#chooseCompany").show();}else{$("#chooseCompany").hide().after($("<p id='c_ok'>公司:</p>").append($("#task_company").val()));}};
}
function pre(i){
	switch (i) {
	case 1:
		$(".next1,#p_e,#chooseType").show();gs(true);$(".prev1").hide();
		$("#productFS1,#productFS3").appendTo("#hide");
		break;
	case 2:
		editP();next(1);
		$("#uploadFS").appendTo($("#hide"));
		break;

	default:
		break;
	}
}
function urlSet(){
	
}
function filesSet(){
	$("#swfBT,.u_ok").hide();
	//生成文件json
	$("#taskFS").appendTo("#task_new");
	
}
function task_company(){
	$("#task_company_h").val($("#task_company").val());
}

var phoneType_java = ["C5900","E329","W239","F839","F339","E379","C7500","other"];
var phoneType_android = ["240x320","320x480","480x800","480x854","960x800","other"];
function choosePhType(){
	
}
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
<div class="aboxTitle">创建新任务</div>
<div class="aboxContent" id="task_new">
<p id="chooseCompany" class="hide">
<label for="task_company">选择公司：</label><span class="red">*</span><span class="gray">(输入公司名称拼音首字母检索,如:输入jsdx,检索“江苏电信”)</span><br />
<input type="text" id="task_company" name="task_company" style="width:90%;padding:5px;margin:0;" /><br />
</p>
<p id="chooseType">任务类型：<span class="red">*</span><br />
<input type="radio" name="task_type" id="type0" value="0" /><label for="type0">新产品测试</label>
<input type="radio" name="task_type" id="type1" value="1" /><label for="type1">DEMO测试</label>
<input type="radio" name="task_type" id="type2" value="2" /><label for="type2">修正Bug</label>
<input type="radio" name="task_type" id="type3" value="3" /><label for="type3">新机型适配</label>
<input type="radio" name="task_type" id="type4" value="4" /><label for="type4">指定复测</label>
<input type="radio" name="task_type" id="type5" value="5" /><label for="type5">拨测</label>
<input type="radio" name="task_type" id="type6" value="6" /><label for="type6">其他</label>
<br /><span id="task_type_err"></span>
</p>

</div>
</div>

<div id="hide" class="hide">
<!-- 
JSON方式获取该系统的所有机型和分组数据,考虑在服务端使用缓存;
js解析json后生成机型组对象和机型对象,分别进行填充,机型按分组进行隐藏和显示;
机型对象有选中和非选中两个状态,点击后的动作不同;
确定选择时将phone1的对象进行收集,生成json填充到隐藏的textarea中去,同时将选中的对象复制到对应文件后面;
需要有个reset方法来重置初始对象而不需要重新获取服务器数据;
搜索需要实现;

任务，产品为分别两个表单，分别进行验证检测;

真正最后提交的字段包括2个JSON数据字段：产品和机型(testUnit创建用);
 -->
<div class="inBox" id="productFS1">
    <div class="inBoxTitle">产品信息</div> 
    <form action="" id="productFrom">
    <div class="inBoxContent">
    	<div class="inBoxLine"><label for="task_name">产品名称:</label><span class="red">*</span><span class="gray">(注意要与业务管理平台完全一致)</span><br /><input type="text" name="task_name" id="task_name" /></div> 
    	<div class="inBoxLine"><label for="task_p_id">产品ID:</label><span class="red">*</span><span class="gray">(注意要与业务管理平台完全一致)</span><br /><input type="text" name="task_p_id" id="task_p_id" /> </div> 
    	<div class="inBoxLine"><label for="task_p_sys">操作系统:</label><span class="red">*</span><select name="task_p_sys" id="task_p_sys"><option value="0">KJava</option><option value="1">Android</option><option value="2">WAP</option><option value="3">Brew</option><option value="4">Windows mobile</option><option value="5">Windows CE</option><option value="6">其他</option></select></div> 
    	<div class="inBoxLine"><label for="task_p_type">产品类型:</label><span class="red">*</span><select name="task_p_type" id="task_p_type"><option value="0">免费</option><option value="1">短代</option><option value="2">点数</option><option value="3">按次下载</option><option value="4">进游戏包</option></select></div> 
    	<div class="inBoxLine"><label for="task_p_net">联网情况:</label><span class="red">*</span>
    	<input type="radio" name="task_p_net" id="task_p_net0" value="0" /><label for="task_p_net0">单机</label>
    	<input type="radio" name="task_p_net" id="task_p_net1" value="1" /><label for="task_p_net1">网游</label>
    	<input type="radio" name="task_p_net" id="task_p_net2" value="2" /><label for="task_p_net2">WAP</label></div> 
    	<div class="inBoxLine"><label for="task_p_acc">接口调测情况:</label>
    	<input type="radio" name="task_p_acc" id="task_p_acc0" value="0" checked="checked" /><label for="task_p_acc0">未调测</label>
    	<input type="radio" name="task_p_acc" id="task_p_acc1" value="1" /><label for="task_p_acc1">已调通</label>
    	<input type="radio" name="task_p_acc" id="task_p_acc2" value="2" /><label for="task_p_acc2">调测中</label></div> 
    	<div class="inBoxLine"><label for="task_p_fee">计费点描述:</label><span class="gray">(注意要与业务管理平台完全一致)</span><br /><textarea name="task_p_fee" id="task_p_fee" rows="3" cols="3" style="height:100px;"></textarea> </div> 
    	<a href="javascript:saveP();" class="aButton tx_center" style="width:60px;">确定</a> <a href="javascript:pre(1);" class="aButton tx_center prev1">上一步</a>
    </div>
    </form>
</div>

<div class="inBox" id="productFS3">
    <div class="inBoxTitle">产品信息</div> 
    <div class="inBoxContent">
    	<div class="inBoxLine">产品名称: <span id="task_name_v" class="blueBold"></span> 产品ID: <span id="task_p_id_v" class="blueBold"></span> 操作系统: <span id="task_p_sys_v" class="blueBold"></span></div> 
    	<div class="inBoxLine">产品计费类型: <span id="task_p_type_v" class="blueBold"></span> 联网情况: <span id="task_p_net_v" class="blueBold"></span> 接口调测情况: <span id="task_p_acc_v" class="blueBold"></span></div> 
    	<div class="inBoxLine">计费点描述: <br /><span id="task_p_fee_v" class="blueBold"></span></div>
    	<a href="javascript:editP();" class="aButton tx_center" style="width:60px;" id="p_e">更改</a> 
    	<a href="javascript:next(2);" class="aButton tx_center next1">下一步：上传文件(或设置WAP产品url)</a>
    </div>
</div>

<div class="inBox" id="productFS2">
    <div class="inBoxTitle">产品选择</div> 
    <div class="inBoxContent">
    	<label for="task_p_search">产品名称检索：</label><span class="red">*</span><span class="gray">(输入产品名称拼音首字母检索)</span><br />
    	<input type="text" name="task_p_search" id="task_p_search" style="width:300px;" /><a href="javascript:pSelect();" class="aButton">确定</a>   <a href="javascript:pre(1);" class="aButton tx_center prev1">上一步</a>
    </div>
</div>
 
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

<div class="inBox" id="uploadFS">
    <div class="inBoxTitle">游戏实体包上传或URL设置</div> 
    <div class="inBoxContent">
	<form name="fileupload" id="fileupload" action="<%=prefix %>/upload" method="post" enctype="multipart/form-data">
		<div id="swfBT" class="inBoxLine">
			<div id="spanSWFUploadButton">请稍候...</div> 
			<span id="uploadInfo"> &nbsp;文件最大不超过100M,格式限定为apk,jar,jad,zip,按住Ctrl键可多选</span>
		</div>
		<div id="upFiles"></div>
		<br /><a href="javascript:filesSet();" class="aButton">确定</a> <a href="javascript:pre(2);" class="aButton tx_center pre2">上一步</a> 
	</form>
	<div id="urlSet">
		<label for="task_p_url">URL设置：</label><span class="red">*</span><span class="gray">(请输入WAP游戏的入口URL)</span><br />
		<input type="text" name="task_p_url" id="task_p_url" style="width:300px;" />
		<a href="javascript:urlSet();" class="aButton">确定</a> <a href="javascript:pre(2);" class="aButton tx_center pre2">上一步</a>
	</div>
    </div>
</div>

<div id="taskFS">
<form action="<%=prefix%>/tasks/a_a" method="post" id="add_form">
<p><label for="task_info">任务说明：</label><br />
<textarea id="task_info" name="task_info" rows="3" cols="3" style="height:60px;"></textarea></p>
<p>任务优先级：
<select name="task_level"><option value="0">普通</option><option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option></select>
</p>
<p>下一流程处理人：
<select name="task_operator"><option value="0">普通</option><option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option></select>
</p>
<input type="hidden" id="task_p_json_h" class="task_p_json_h" value="" />
<input type="hidden" id="news_files" name="news_files" value="" />
</form>


<p><a href="javascript:aSubmit();" id="submitBT" class="aButton tx_center" style="width:60px;">创建任务</a><a href="<%=prefix%>/news" class="aButton tx_center" style="width:60px;">返回</a></p>

</div>
<!-- end of hide -->
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>