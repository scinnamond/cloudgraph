
    var currentContentDivStyleWidth;
    var currentContentDivStyleHeight;
  
    function doOnLoad()
    {
       window.onresize = doOnResize;
       doOnResize();
    }
    function doOnResize()
    {  
        var frameWidth;
        var frameHeight;
        
        if (self.innerWidth)                                                        
        { 
        	frameWidth = self.innerWidth;                                           
        	frameHeight = self.innerHeight;                                         
        }                                                                           
        else if (document.documentElement && document.documentElement.clientWidth)  
        {                                                                           
        	frameWidth = document.documentElement.clientWidth;                      
        	frameHeight = document.documentElement.clientHeight;                    
        }                                                                           
        else if (document.body)                                                     
        {                                                                           
        	frameWidth = document.body.clientWidth;                                 
        	frameHeight = document.body.clientHeight;                               
        }
        else
            return; // browser can't support script                                                                          
                
        var divWidth = frameWidth - 280;
        var adjDivWidth = divWidth - 10;
        if (adjDivWidth < 300)
            adjDivWidth = 300;

        var divHeight = frameHeight;
        var adjDivHeight = divHeight - 120;
        if (adjDivHeight < 200)
            adjDivHeight = 200;

        currentContentDivStyleWidth = adjDivWidth;
        currentContentDivStyleHeight = adjDivHeight;
        
        var contentDiv = document.all.item('content');
        var contentDivStyle = contentDiv.getAttribute('style');
        contentDivStyle.width = currentContentDivStyleWidth;
        //contentDivStyle.height = currentContentDivStyleHeight;            
    }

