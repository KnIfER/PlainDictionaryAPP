console.log('!!!高亮开始'); // 运行于主页面
var w=window,waiting=false;
var MarkLoad,MarkInst;
if(!w.pages) {
    w.pages = [{keys:[],faked:true}]
}
var current, currentIndex=0, pageIndex=0
  , totalMarks, totalPages;
var currentClass = "current";
function findPrvPage() {
    for(var i=pageIndex-1,inf;i>=0;i--) {
        inf=pages[i];
        if(inf.keyz) {
            pageIndex=i; 
            currentIndex=inf.keyz-1;
            //toast(inf.name);
            return true;
        }
    }
    return false;
}
function findNxtPage() {
    waiting=false;
    for(var i=pageIndex+1,inf;i<pages.length;i++) {
        inf=pages[i];
        if(inf.keyz) {
            pageIndex=i;
            currentIndex=0;
            //toast(inf.name);
            return true;
        } else if(inf.waiting) {
            //debug("waiting!!!", inf.name);
            toast("&nbsp;···&nbsp;");
            checkFrame(inf);
            waiting=true;
            return false;
        }
    }
    return false;
}
//exported
function jumpTo(d, desiredOffset, frameAt, HlightIdx, reset, topOffset_frameAt,wait) {
    if (true) { // results.length
        var pg=pageIndex,results=pages[pg].keys;
        if(reset) resetLight(d);
        debug('jumpTo received reset='+reset+' '+frameAt+'->'+HlightIdx+' '+(currentIndex+d)+'/'+(results.length)+' dir='+d);
        var np=currentIndex+d;
        var max=results.length - 1;
        if (currentIndex > max) currentIndex=0;
        if (np < 0 && !findPrvPage()) {return -1;}
        if (np > max && !findNxtPage()) {
            if(!wait && waiting) {
                setTimeout(function(){
                    jumpTo(d,0,frameAt, HlightIdx, reset, topOffset_frameAt, true);
                }, 200);
                return 0;
            }
            return 1;
        }
        if (pg==pageIndex) currentIndex=np;
        else results=pages[pageIndex].keys;
        if(current) removeClass();
        current = results[currentIndex];
        console.log("current=", current, );
        if(current){
            if(w.defP) {
                var page=pages[pageIndex], top=pw_topOffset(current), height=current.offsetHeight
                , defPTop=Math.ceil(defP.scrollTop), defPHeight=defP.offsetHeight
                , b=page.box, boxT=b.offsetTop
                , tH=page.title.offsetHeight, tT=page.title.offsetTop;
                ;
                if(pages.length>=1) {
                    if(page.fold) {
                        unfoldFrame(page);
                    }
                    if(!page.fold) {
                        var d=page.item.contentWindow.document.documentElement
                        , boxH=b.offsetHeight, dst=d.scrollTop;
                        if(page.collapsed) {
                            debug('top::', top, d.scrollTop, d.scrollTop+boxH);
                            if(top<dst || top>=dst+boxH) {
                                d.scrollTo(d.scrollLeft, top);
                                if(page.fakeScroll) {
                                    page.fScrollP.scrollTop=top;
                                }
                            } else if(top+height>dst+boxH) { //贴底
                                var t=top+height-boxH;
                                d.scrollTo(d.scrollLeft, t);
                                if(page.fakeScroll) {
                                    page.fScrollP.scrollTop=t;
                                }
                            }
                            dst = top-d.scrollTop+boxT;
                            if(dst<defPTop) {
                                defP.scrollTop=boxT-tH;
                            } else if(dst>=defPTop+defPHeight) {
                                defP.scrollTop=boxT-defPHeight+boxH+tH;
                            } else if(dst+height>=defPTop+defPHeight) { //贴底
                                defP.scrollTop=dst+height-defPHeight;
                            }
                            defPTop=Math.ceil(defP.scrollTop);
                            if(dst<defPTop || dst>=defPTop+defPHeight) {
                                defP.scrollTop=boxT+dst-height;
                            }
                        } else {
                            top += boxT;
                            if(top<defPTop || top>=defPTop+defPHeight) {
                                defP.scrollTop=top;
                            } else if(top+height>defPTop+defPHeight) { //贴底
                                defP.scrollTop=top+height-defPHeight;
                            } 
                        }
                    }
                }
                if (pg!=pageIndex) {
                    // 切换了
                    if(tT+tH/2<defPTop||tT>=defPTop+defPHeight)
                        toast(page.name, 0, 1800, 0, 0.95);
                    tFrame(page);
                }
            } else {
                var position = topOffset(current);
                app.scrollHighlight(sid.get(), position, d);
            }
            updateIndicator();
            addClass();
            return ''+currentIndex;
        }
    }
    return d;
}
function updateIndicator(){
    if(w.app) {
        var page=pages[pageIndex];
      //  w.app.updateIndicator(sid.get(), page.id, currentIndex, page.keyz, totalMarks);
    }
}
function pw_topOffset(node){
    var top=0;
    while(node){
        top+=node.offsetTop;
        node=node.offsetParent;
    }
    return top;
}
function topOffset(elem){
    var top=0;
    var add=1;
    while(elem && elem!=d.body){
        if(!w.webx)if(elem.style.display=='none' || elem.style.display=='' && d.defaultView.getComputedStyle(elem,null).display=='none'){
            elem.style.display='block';
        }
        if(add){
            top+=elem.offsetTop;
            var tmp = elem.offsetParent;
            if(!tmp) add=0;
            else elem=tmp;
        }
        if(!add) elem=elem.parentNode;
    }
    return !add&&top==0?-1:top;
}
function quenchLight(){
    if(current) removeClass();
}
function resetLight(d){
    if(d==1) {pageIndex=0;currentIndex=-1;}
    else if(d==-1) {pageIndex=pages.length-1;currentIndex=pages[pageIndex].keys.length;}
    quenchLight();
}
function setAsEndLight(){
    pageIndex=pages.length-1;
    currentIndex=pages[pageIndex].keys.length;
    //currentIndex=results.length-1;
}
function setAsStartLight(){
    currentIndex=0;
}
function addClass() {
    current.classList.add(currentClass);
}
function removeClass() {
    current.classList.remove(currentClass);
}
function clearHighlights(){
    if(w.bOnceHighlighted && MarkInst && MarkLoad)
    MarkInst.unmark({
        done: function() {
            //results=[];
            for(var i=0;i<pages.length;i++){
                pages[i].keys=[]
            }
            w.bOnceHighlighted=false;
        }
        ,iframes:true
    });
}
function highlight(keyword){
    var b1=keyword==null;
    if(b1)
        keyword=app.getCurrentPageKey();
    if(keyword==null||b1&&keyword.trim().length==0)
        return;
    console.log('高亮开始，keyword='+keyword);
    if(!MarkLoad) MarkLoad|=w.MarkLoad;
    if(!MarkLoad){
        function cb(){MarkLoad=true; do_highlight(keyword);}
        try{loadJs('mdbr://mark.js', cb)}catch(e){w.loadJsCb=cb;app.loadJs(sid.get(),'mark.js');}
    } else do_highlight(keyword);
}
function do_highlight(keyword){
    if(!MarkInst)
        MarkInst = new Mark(w.defP||document.body);
    w.bOnceHighlighted=false;
    w.pageKey=decodeURIComponent(keyword);
    MarkInst.unmark({
        done: function() {
            if(w.app && !(w.shzh&8)) {
              w.shzh=app.rcsp(sid.get());
            }
            var sz=w.shzh>>4;
            console.log('highlighting...sz=',pageKey,((sz&0x1)!=0),sz&0x4);
            if(sz&0x1)
            MarkInst.markRegExp(new RegExp(pageKey, (sz&0x2)?'m':'im'), {
                done: done_highlight
                ,iframes:true
                ,iframesTimeout:0
            });
            else
            MarkInst.mark(pageKey, {
                separateWordSearch: (sz&0x4)!=0
                ,wildcards:(sz&0x10)?(sz&0x8)?'enabled':'withSpaces':'disabled'
                ,done: done_highlight
                ,caseSensitive:(sz&0x2)!=0
                ,iframes:true
                ,iframesTimeout:0
            });
        }
    });
}
function done_highlight(){
    w.bOnceHighlighted=true;
    w.totalMarks=0;
    for(var i=0;i<pages.length;i++){
        var inf=pages[i];
        if(inf.faked) inf.keys = d.getElementsByTagName("mark");
        else try{
            inf.keys=inf.item.contentWindow.document.getElementsByTagName("mark");
        } catch(e) {
            debug(inf, e);
            inf.keys=[];
        }
        inf.keyz=inf.keys.length;
        totalMarks+=inf.keyz;
    }
    //results = d.getElementsByTagName("mark");
    pageIndex=0;
    currentIndex=-1;
    updateIndicator();
    if(app) app.onHighlightReady(sid.get(), w.frameAt||0, totalMarks);
}

//exported
function MarkFrame(ifr){
    if(w.bOnceHighlighted && ifr) {
        var inf = ifr.dictInfo;
        try{
            var d = ifr.contentWindow.document;
            var inst = new Mark(d.body);
            function dn() {
                inf.keys=d.getElementsByTagName("mark");
                inf.keyz=inf.keys.length;
                totalMarks+=inf.keyz;
                updateIndicator();
            }
            var rcsp=w.rcsp;
            console.log('高亮子页面...'+pageKey+((rcsp&0x1)!=0));
            if(rcsp&0x1)
            inst.markRegExp(new RegExp(pageKey, (rcsp&0x2)?'m':'im'), {
                done: dn
            });
            else
            inst.mark(pageKey, {
                separateWordSearch: (rcsp&0x4)!=0,'wildcards':(rcsp&0x10)?(rcsp&0x8)?'enabled':'withSpaces':'disabled'
                ,done: dn
                ,caseSensitive:(rcsp&0x2)!=0
            });
        } catch(e) {
            debug(inf, ifr);
            inf.keys=[];
            inf.keyz=0;
        }
    }
}