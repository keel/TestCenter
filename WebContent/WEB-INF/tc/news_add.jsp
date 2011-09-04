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
out.print(JSPOut.out("head0","0","公告"));%>
<link rel="stylesheet" href="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.css" type="text/css" media="screen" />
<script src="<%=sPrefix %>/fancybox/jquery.fancybox-1.3.4.pack.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/swfupload.min.js" type="text/javascript"></script>
<script src="<%=sPrefix %>/js/jquery.validate.min.js" type="text/javascript"></script>
<script type="text/javascript">
$(function(){
	$("#side_gg a").addClass("sideON");
//处理请求
$.validator.dealAjax = {
	bt:$("#submitBT"),
	ok:function(data){
		if(!isNaN(data)){
			var bt1 = "<a href=\"javascript:window.location='<%=prefix%>/news/"+data+"';\" class=\"aButton\">查看公告</a>";
			abox("公告发表","<div class='reOk'>公告发表成功！ &nbsp;"+bt1+" <a href=\"javascript:window.location =('<%=prefix %>/news');\" class=\"aButton\">返回列表</a></div>");
		}else{abox("公告发表","<div class='reErr'>公告发表失败! &nbsp;<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a></div>");};
	},
	err:function(){
		abox("公告发表","<div class='reErr'>公告发表失败! &nbsp;<a href='javascript:$.fancybox.close();' class=\"aButton\">关闭</a></div>");
	}
};
//开始验证
$('#news_form').validate({
    /* 设置验证规则 */
    rules: {
		news_name: {
            required:true,
            rangelength:[2,50]
        },
        news_text:{
            required:true,
            rangelength:[6,2000]
        }
    }
});
initUpload("<%=user.getName() %>");
});
function aSubmit(){
	$("#news_form").submit();
};
$.sPrefix = "<%=sPrefix %>";$.prefix="<%=prefix %>";
//fileupload
var swfu;
function initUpload(uName){
 swfu= new SWFUpload({
	upload_url : $.prefix+"/upload",
	flash_url : $.sPrefix+"/js/swfupload.swf",
	post_params: {"uName":uName},
	use_query_string:true, 
	button_placeholder_id : "spanSWFUploadButton",
	button_width: 100,
	button_height: 29,
	button_text: "<span class='swfTxt1'>选择上传文件</span>",
	button_text_style: ".swfTxt1 {color:#ffffff;}",
	button_text_left_padding: 12,
	button_text_top_padding: 6,
	button_image_url : $.sPrefix+"/images/swfBT_100x29.png",
	
	prevent_swf_caching:false,
	upload_start_handler : fileQueue,
	file_queue_error_handler : fileQueueError,
	file_dialog_complete_handler : fileDialogComplete,
	upload_progress_handler : uploadProgress,
	upload_error_handler : uploadError,
	upload_success_handler : uploadSuccess,
	upload_complete_handler:uploadComplete,

	file_types : "*.rar;*.zip;*.png;*.gif",  
    file_types_description : "图片文件", 
	file_size_limit : "123000"
	//,debug:true
	
	});
}
function swferr(info){
	$("#uploadInfo").html(info).css("color","red");
}
function swfinfo(info){
	$("#uploadInfo").html(info).css("color","black");
}
function swfok(info){
	$("#upFiles").append(info);
	$("#uploadInfo").html("");
}
function swfreset(){
	swfinfo("图片最大不超过3M,图片格式为jpg,png,gif");
}
function fileQueue(file){
	var f_enc = encodeURIComponent(file.name);
	swfu.addPostParam("f",f_enc);
	//console.log("f_enc:"+f_enc);
	if($("#news_files").val() == ""){
		$("#news_files").val(f_enc);
	}else{
		$("#news_files").val($("#news_files").val()+","+f_enc);
	}
}

function fileQueueError(file, errorCode, message) {
	try {
		switch (errorCode) {
		case SWFUpload.QUEUE_ERROR.ZERO_BYTE_FILE:
			swferr("文件大小为空.");
			break;
		case SWFUpload.QUEUE_ERROR.FILE_EXCEEDS_SIZE_LIMIT:
			swferr("文件超过大小限制.");
			break;
		case SWFUpload.QUEUE_ERROR.INVALID_FILETYPE:
			swferr("无效的文件类型.");
			break;
		default:
			swferr("选择文件错误.");
			break;
		}
	} catch (ex) {
		swferr("选择文件错误.");
	}
}
function uploadComplete(file) {
	try {
		if (this.getStats().files_queued === 0) {
			//document.getElementById(this.customSettings.cancelButtonId).disabled = true;
		} else {	
			this.startUpload();
		}
	} catch (ex) {
		this.debug(ex);
	}
}
function fileDialogComplete(numFilesSelected, numFilesQueued) {
	try {
		if (numFilesQueued > 0) {
			this.startUpload();return;
		}
	} catch (ex) {}
}
function uploadProgress(file, bytesLoaded) {
	try {
		if(swfu.startProg){
			var percent = Math.ceil((bytesLoaded / file.size) * 100);
			$("#swfuploadProgress").text(percent);			
		}else{
			swfu.startProg = true;	
			swfinfo("文件上传中......已上传 [<span id='swfuploadProgress'>0</span> ] % , <a href='javascript:swfu.cancelUpload(\""+file.id+"\");'>取消</a>");
		}
	} catch (ex) {}
}
function uploadSuccess(file, serverData) {
	try {
		var re = serverData;
		swfu.startProg = false;
		//console.log(re);
		console.log(file);
		if(re.length>=file.name.length){
		swfok("<div class='file_upload' id='fu_"+file.index+"'>"+file.name+" <span class='greenBold'>上传成功!</span> [ <a href='javascript:delFile(\""+file.index+","+file.name+"\");'>删除 </a> ]</div>");
		}else{swfok("<div class='file_upload file_upload_ERR'>"+f_enc+" 上传失败!</div>");}
	} catch (ex) {}
}
function delFile(fid,fname){
	console.log(fid);
	$("#fu_"+fid).hide();
	var ff = $("#news_files").val().split(",");
	ff.splice(fid,1);
	$("#news_files").val(ff.join(","));
}
function uploadError(file, errorCode, message) {
	try {
		switch (errorCode) {
		case SWFUpload.UPLOAD_ERROR.FILE_CANCELLED:
			swfinfo("上传已取消.");
			break;
		case SWFUpload.UPLOAD_ERROR.UPLOAD_STOPPED:
			swfinfo("上传已停止.");
		case SWFUpload.UPLOAD_ERROR.UPLOAD_LIMIT_EXCEEDED:
			swferr("上传文件超过服务器限制.");
			break;
		default:
			swferr("上传失败:"+message);
			break;
		}
	} catch (ex3) {
	}
};
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
<div class="aboxTitle">新建公告</div>
<div class="aboxContent">
<form action="<%=prefix%>/news/add" method="post" id="news_form">
<p>标题：<br />
<input type="text" name="news_name" style="width:90%;padding:5px;margin:0;" /></p>

<p>内容：<br />
<textarea name="news_text" rows="3" cols="3" style="height:200px;"></textarea></p>
<p>显示级别：
<select name="news_type"><option value="0">所有人</option><option value="1">厂家</option><option value="2">测试员</option><option value="3">组长</option><option value="4">管理员</option></select>
置顶级别(数字最大的在顶部)：
<select name="news_level"><option value="0">无</option><option value="1">1</option><option value="2">2</option><option value="3">3</option></select>
</p>
<input type="hidden" id="news_files" name="news_files" value="" />
</form>

<form name="fileupload" id="fileupload" action="<%=prefix %>/upload" method="post" enctype="multipart/form-data">
	<div id="swfBT">
		<div id="spanSWFUploadButton">载入中...</div> 
		<span id="uploadInfo">图片最大不超过3M,图片格式为jpg,png,gif</span>
	</div>
	<div id="upFiles"></div>
</form>
<p><a href="javascript:aSubmit();" id="submitBT" class="aButton tx_center" style="width:60px;">保存</a><a href="<%=prefix%>/news" class="aButton tx_center" style="width:60px;">返回</a></p>

</div>
</div>
		<div class="clear"></div>
		</div>
<% out.println(JSPOut.out("foot0")); %>