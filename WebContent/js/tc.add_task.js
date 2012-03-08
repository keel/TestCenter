var pJSON = {};

function addCompany(){
	return {c:$("#task_company").val()};
}
function aSubmit(){
	$("#add_form").submit();
};
function saveP(){$('#productForm').submit();}
function editP(){
	var e = ($.t<=1)?$('#productFS1'):$('#productFS2');
	e.appendTo($("#task_new"));$('#productFS3').appendTo($("#hide"));
}
function pSelect(pid){
	var pnu = (pid)?"pid="+pid:"p="+encodeURI($("#task_p_search").val());
	$.getJSON($.prefix+"/product/one?"+pnu,function(data){
		if(!data || data==""){alert("产品不存在!请确认产品名称正确.");return;}
		else{
			pJSON = data;
			$("#task_name_v").text(data.name);
			$("#task_p_id_v").text(data._id);
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
	if( $("#chooseCompany")[0].need){$("#c_ok").remove();if(show){$("#chooseCompany").show();}else{$("#chooseCompany").hide().after($("<p id='c_ok'>公司:</p>").append(pJSON.company));}};
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
	case 3:
		if(pJSON.sys!="2"){
			$("#swfBT,.u_ok,#fileupload .aButton").show();$("#taskFS").appendTo("#hide");
		}else{
			$("#urlInput").show();
			$("#urlSet .blueBold").text("").hide();
			$("#taskFS").appendTo("#hide");
		}
		break;
	default:
		break;
	}
}
function urlSet(){
	var v=$("#task_p_url").val();
	if(!v || $.trim(v).length<=0){alert("请正确填写WAP的入口URL地址");return;}
	else{pJSON.url=v;$("#task_p_json_h").html($.toJSON(pJSON));
	$("#task_type_h").val($('input:radio[name=task_type]:checked').val());
	$("#urlInput").hide();$("#urlSet .blueBold").text($("#task_p_url").val()).show();
	$("#taskFS").appendTo("#task_new");
	}
}

function filesSet(){
	//检测是否每个文件都指定了机型组
	var b = true,tmp = [],i=0;
	$("#upFiles").find(".file_upload").each(function(){
		var v = $(this).find(".txtBox"),n = $(this).find(".filename").text(),j={"name":n,"fileName":$(this).find(".newname").text(),"size":$(this).find(".size").text(),"groups":[]};
		if(v.length<=0){b=false;return false;}
		else{
			v.each(function(){
				j.groups.push($(this).text());
			});
			tmp.push(j);
		}
		i++;
	});
	if(!b){alert("请为所有文件都指定机型组!");return;}
	if(i==0){alert("请上传文件并指定机型组!");return;}
	//生成文件json
	if(tmp.length>0){pJSON.files=tmp;$("#task_p_json_h").html($.toJSON(pJSON));}
	if(pJSON.type){$("#task_type_h").val(pJSON.type);}else{$("#task_type_h").val($('input:radio[name=task_type]:checked').val());};
	$("#swfBT,.u_ok,#fileupload .aButton").hide();
	$("#taskFS").appendTo("#task_new");
}
function task_company(){
	$("#task_company_h").val($("#task_company").val());
}
var phTypes = [["C5900","E329","W239","F839","F339","E379","C7500","其他"],
                  ["240x320","320x480","480x800","480x854","960x540","其他"]];
function choosePhType(fu){
	var pt = pJSON.sys;
	$("#fu_"+fu).css("background-color","#FFF");
	if(pt>=0 && pt<=1){
		if($("#phTypes").length<=0){
			var tt = $("<div id='phTypes'></div>");
			for ( var i = 0; i < phTypes[pt].length; i++) {
				$("<input type='checkbox' class='pht' name='pht' id='pht_"+i+"' value='"+phTypes[pt][i]+"' /><label for='pht_"+i+"'>"+phTypes[pt][i]+"</label> ").appendTo(tt);
			}
			tt.append("<br /><a href=\"javascript:phtSet();\" class=\"aButton\">确定<\/a>");
			tt[0].fu = fu;
			tt.appendTo($("#fu_"+fu));
		}else{
			var p = $("#phTypes");p.find(".pht:checked").removeAttr("checked");
			p.appendTo($("#fu_"+fu));p[0].fu = fu;$("#fu_"+fu).find(".sok").remove();
		}
	}
}
function phtSet(){
	var ok = $("<div class='sok'></div>");
	$("#phTypes").find(".pht:checked").each(function(i){
		$("<span class='txtBox'>"+$(this).val()+"</span>").appendTo(ok);
	});
	ok.appendTo($("#fu_"+$("#phTypes")[0].fu));
	$("#phTypes").appendTo($("#hide"));
}