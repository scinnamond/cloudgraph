/* 
* jQuery Context Menu
*/

jQuery.noConflict()

var jquerycontextmenu={
	arrowpath: '/appinv/images/arrow.gif', //full URL or path to arrow image
	contextmenuoffsets: [1, -1], //additional x and y offset from mouse cursor for contextmenus

	builtcontextmenuids: [], //ids of context menus already built (to prevent repeated building of same context menu)

	positionul:function($, $ul, e){
		var istoplevel=$ul.hasClass('jqcontextmenu') //Bool indicating whether $ul is top level context menu DIV
		var docrightedge=$(document).scrollLeft()+$(window).width()-40 //40 is to account for shadows in FF
		var docbottomedge=$(document).scrollTop()+$(window).height()-40
		if (istoplevel){ //if main context menu DIV
//alert("positionul1-toplevel")
			var x=e.pageX+this.contextmenuoffsets[0] //x pos of main context menu UL
			var y=e.pageY+this.contextmenuoffsets[1]
//alert(x)			
//alert(y)			
			x=(x+$ul.data('dimensions').w > docrightedge)? docrightedge-$ul.data('dimensions').w : x //if not enough horizontal room to the ridge of the cursor
			y=(y+$ul.data('dimensions').h > docbottomedge)? docbottomedge-$ul.data('dimensions').h : y
//alert("positionul1A")
		    $ul.css({left:x, top:y})
		}
		else{ //if sub level context menu UL
//alert("positionul1-sublevel")
			var $parentli=$ul.data('$parentliref')
			var parentlioffset=$parentli.offset()
			var x=$ul.data('dimensions').parentliw //x pos of sub UL
			var y=0
//alert(x)			
//alert(y)			
			x=(parentlioffset.left+x+$ul.data('dimensions').w > docrightedge)? x-$ul.data('dimensions').parentliw-$ul.data('dimensions').w : x //if not enough horizontal room to the ridge parent LI
			y=(parentlioffset.top+$ul.data('dimensions').h > docbottomedge)? y-$ul.data('dimensions').h+$ul.data('dimensions').parentlih : y
//alert("positionul1")
		    $ul.css({left:x, top:y})
		}
//alert("positionul2")
	},
	
	showbox:function($, $contextmenu, e){
		$contextmenu.show()
	},

	hidebox:function($, $contextmenu){
		$contextmenu.find('ul').andSelf().hide() //hide context menu plus all of its sub ULs
	},

	buildcontextmenu:function($, $menu){
		$menu.css({display:'block', visibility:'hidden'}).appendTo(document.body)
		$menu.data('dimensions', {w:$menu.outerWidth(), h:$menu.outerHeight()}) //remember main menu's dimensions
		var $lis=$menu.find("ul").parent() //find all LIs within menu with a sub UL
		$lis.each(function(i){
			var $li=$(this).css({zIndex: 1000+i})
			var $subul=$li.find('ul:eq(0)').css({display:'block'}) //set sub UL to "block" so we can get dimensions
			$subul.data('dimensions', {w:$subul.outerWidth(), h:$subul.outerHeight(), parentliw:this.offsetWidth, parentlih:this.offsetHeight})
			$subul.data('$parentliref', $li) //cache parent LI of each sub UL
			$li.data('$subulref', $subul) //cache sub UL of each parent LI
			$li.children("a:eq(0)").append( //add arrow images
				'<img src="'+jquerycontextmenu.arrowpath+'" class="rightarrowclass" style="border:0;" />'
			)
			$li.bind('mouseenter', function(e){ //show sub UL when mouse moves over parent LI
				var $targetul=$(this).data('$subulref')
				if ($targetul.queue().length<=1){ //if 1 or less queued animations
					jquerycontextmenu.positionul($, $targetul, e)
					$targetul.show()
				}
			})
			$li.bind('mouseleave', function(e){ //hide sub UL when mouse moves out of parent LI
				$(this).data('$subulref').hide()
			})
		})
		$menu.find('ul').andSelf().css({display:'none', visibility:'visible'}) //collapse all ULs again
		this.builtcontextmenuids.push($menu.get(0).id) //remember id of context menu that was just built
	},

	tweakcontextmenu:function($, $menu, params){
       
		var domLinks = $menu.find("a").get();
        var link = null; 
        for (var i = 0; i < domLinks.length; i++){ 
            link = domLinks[i]; 
            if (link.href.endsWith("#")) 
		    {
		        //if (link.onclick != null) {
			    //    alert("onclick: " + link.onclick);
			    //    var result = "?";  
	            //    for (var j = 0; j < params.length; j+=2) { 
	            //        var key = params[j];
	            //        var value = params[j+1];
	            //        result += key + "=" + value;
	            //        result += "&";
	            //    }
			    //    var clickContent = link.onclick;
			    //    clickContent.replace("?", result);
			    //    alert("onclick2: " + clickContent);
			    //    link.onclick = clickContent;
		        //}
		    }
		    else
		    {           
			    if (link.search == null || link.search == "")
			    {		      
			        var result = "?";  
	                for (var j = 0; j < params.length; j+=2) { 
	                    var key = params[j];
	                    var value = params[j+1];
	                    if (j > 0)
	                        result += "&";
	                    result += key + "=" + value;
	                }
	                link.search = result;
			    }
			    else
			    {
			        var result2 = link.search;
	                for (var j = 0; j < params.length; j+=2) { 
	                    var key = params[j];
	                    var value = params[j+1];
	                    if (result2.indexOf(key) == -1)
	                    {
	                        result2 += "&" + key + "=" + value;
	                    }
	                    else
	                    {
			                var modSearch = insertParam(result2, key, value);
			                //alert("after2: " + modHref2)
			                result2 = modSearch;
	                    }
	                }
			        link.search = result2;
			    }
		    }		    
        } 				

		//var $links=$menu.find("a") //find all 'a' elements within menu
		//$links.each(function(i){
		//	var $a=$(this)
		//	//alert("onclick: " + $a.attr("onclick"))
		//	if ($a.attr("onclick") == null) { // 
		//	    //alert("URL: " + url)
		//	    $a.attr("href", url);
		//	}
		//})		
		
	},

	init:function($, $target, $contextmenu){
		if (this.builtcontextmenuids.length==0){ //only bind click event to document once
			$(document).bind("click", function(e){
				if (e.button==0){ //hide all context menus (and their sub ULs) when left mouse button is clicked
					jquerycontextmenu.hidebox($, $('.jqcontextmenu'))
				}
			})
		}
		if (jQuery.inArray($contextmenu.get(0).id, this.builtcontextmenuids)==-1) //if this context menu hasn't been built yet
			this.buildcontextmenu($, $contextmenu)
			$(document).bind("click", function(e){
				if (e.button==0){ //hide all context menus (and their sub ULs) when left mouse button is clicked
					jquerycontextmenu.hidebox($, $('.jqcontextmenu'))
				}
			})
		if ($target.parents().filter('ul.jqcontextmenu').length>0) //if $target matches an element within the context menu markup, don't bind oncontextmenu to that element
			return
		$target.bind("click", function(e){
			jquerycontextmenu.hidebox($, $('.jqcontextmenu')) //hide all context menus (and their sub ULs)
			jquerycontextmenu.positionul($, $contextmenu, e)
			jquerycontextmenu.showbox($, $contextmenu, e)
						
			return false
		})
	},

	init2:function($, $target, $contextmenu){
		if (jQuery.inArray($contextmenu.get(0).id, this.builtcontextmenuids)==-1) //if this context menu hasn't been built yet
		{	
			this.buildcontextmenu($, $contextmenu)
		}
		$(document).bind("click", function(e){
			jquerycontextmenu.hidebox($, $('.jqcontextmenu')) //hide all context menus (and their sub ULs)
			return true
		})
		if ($target.parents().filter('ul.jqcontextmenu').length>0) //if $target matches an element within the context menu markup, don't bind oncontextmenu to that element
			return
	},

	show:function($, $contextmenu, event) {
//alert("show1");
		jquerycontextmenu.hidebox($, $('.jqcontextmenu')) //hide all context menus (and their sub ULs)
//alert("show2");
		jquerycontextmenu.positionul($, $contextmenu, event)
//alert("show3");
		jquerycontextmenu.showbox($, $contextmenu, event)
	}
}


function insertParam(search, key, value)
{
    key = escape(key); value = escape(value);

    var s = search;
    var kvp = key+"="+value;

    var r = new RegExp("(&|\\?)"+key+"=[^\&]*");

    s = s.replace(r,"$1"+kvp);

    if(!RegExp.$1) {s += (s.length>0 ? '&' : '?') + kvp;};

    return s;
}


jQuery.fn.addcontextmenu=function(contextmenuid){
	var $=jQuery
	return this.each(function(){ //return jQuery obj
		var $target=$(this)
			jquerycontextmenu.init($, $target, $('#'+contextmenuid))
	})
};

jQuery.fn.initcontextmenu=function(contextmenuid){
	var $=jQuery
	var $target=$(this)
//alert("initcontextmenu");
	jquerycontextmenu.init2($, $target, $('#'+contextmenuid))
};

jQuery.fn.showcontextmenu=function(contextmenuid, ev, params){
	var $=jQuery
    var e = $.event.fix(ev); // fixes up event making it IE happy, e.g. pageX, pageY work
//alert(e.type)
//alert(e.pageX)
	var $target=$(this)
	var $contextmenu=$('#'+contextmenuid)
//alert($contextmenu)
//alert($contextmenu.html())
	jquerycontextmenu.tweakcontextmenu($, $contextmenu, params)
	jquerycontextmenu.show($, $contextmenu, e)
};

String.prototype.startsWith = function(str)
{return (this.match("^"+str)==str)}

String.prototype.endsWith = function(str)
{return (this.match(str+"$")==str)}

//Usage: $(elementselector).addcontextmenu('id_of_context_menu_on_page')