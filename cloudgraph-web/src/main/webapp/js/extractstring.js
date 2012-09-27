	function parseIdentifier(onclickTxt, tag)
	{
		var idx = onclickTxt.indexOf(tag);
		var subtxt = onclickTxt.substring(idx+10+tag.length);
		idx = subtxt.indexOf("'");
		return subtxt.substring(0,idx);
	}