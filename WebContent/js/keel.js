/*
 * 获取url中的参数
 * */
function QueryString(fieldName){ 
	var urlString = document.location.search;
	if(urlString != null){
		var typeQu = fieldName+"=";
		var urlEnd = urlString.indexOf(typeQu);
		if(urlEnd != -1){
			var paramsUrl = urlString.substring(urlEnd+typeQu.length);
			var isEnd = paramsUrl.indexOf('&');
			if(isEnd != -1){
				return paramsUrl.substring(0, isEnd);
			}else{
				return paramsUrl;
			}
		}else{
			return null;
		}
	}else{
		return null;
	}
}

/*
 * 将原对象中的内容以html形式替换掉,注意原对象最好是pre或textarea，不然在IE下会无法替换换行
 * 目前实现了换行,空格,链接的转换
 * */
function showHtml(target) {
	var s = $(target).html(),pa = $(target).parent();;
	$(target).remove();
	s=s.replace(/\x20/g,"&nbsp;");
	s=s.replace(/\n/g,"<br />") ;
	s=s.replace( /(http[s]?:\/\/[\w-]*(\.[\w-]*)+)/ig ,"<a href='$1' target='_blank'>$1</a>") ;
	pa.append($("<div>"+s+"</div>"));
}
