/**这个不是网页版*/
var w=window, d=document;
var LoadMark, frameAt;
function _log(...e){console.log('fatal web::'+e)};
w.addEventListener('load',function(e){
    //_log('wrappedOnLoadFunc...');
    var ws = d.body.style;
    _log('mdpage loaded dark:'+(w.rcsp&0x40));
    d.body.contentEditable=!1;
    _highlight(null);
    var vi = d.getElementsByTagName('video');
    function f(e){
        //_log('begin fullscreen!!! wrappedFscrFunc');
        var se = e.srcElement;
        //if(se.webkitDisplayingFullscreen&&app) app.onRequestFView(se.videoWidth, se.videoHeight);
        if(app)se.webkitDisplayingFullscreen?app.onRequestFView(se.videoWidth, se.videoHeight):app.onExitFView()
    }
    for(var i=0;i<vi.length;i++){if(!vi[i]._fvwhl){vi[i].addEventListener("webkitfullscreenchange", f, false);vi[i]._fvwhl=1;}}
},false);
function _highlight(keyword){
    var b1=keyword==null;
    if(b1)
        keyword=app.getCurrentPageKey(sid.get());
    if(keyword==null||b1&&keyword.trim().length==0)
        return;
    if(!LoadMark) {
        function cb(){LoadMark=1;highlight(keyword);}
        try{loadJs('mdbr://markloader.js', cb)}catch(e){w.loadJsCb=cb;app.loadJs(sid.get(),'markloader.js');}
    } else highlight(keyword);
}
w.addEventListener('touchstart',function(e){
    //_log('fatal wrappedOnDownFunc');
    if(!w._touchtarget_lck && e.touches.length==1){
        w._touchtarget = e.touches[0].target;
    }
    //_log('fatal wrappedOnDownFunc' +w._touchtarget);
});
function loadJs(url,callback){
    var script=d.createElement('script');
    script.type="text/javascript";
    if(typeof(callback)!="undefined"){
        script.onload=function(){
            callback();
        }
    }
    script.src=url;
    d.body.appendChild(script);
}