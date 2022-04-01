if(!window.rcspc){
    console.log('popuping ini...rcsp='+rcsp);
    window.addEventListener('click',window.rcspc=function(e){
    var w=this,d=w.document,rcsp=w.rcsp,app=w.app,_log=function(e){console.log(e)};
    if(w.frameElement){rcsp=parent.window.rcsp;app=parent.window.app}
    //_ log('wrappedClickFunc 2', e.srcElement.id);
    var curr = e.srcElement;
    _log('popuping...rcsp='+rcsp);
    if(curr!=d.documentElement && curr.nodeName!='INPUT' && curr.nodeName!='BUTTON' && curr.nodeName!='A' && rcsp&0x20 && !curr.noword && !curr.onclick){
        //todo d.activeElement.tagName
        var s = w.getSelection();
        if(s.isCollapsed && s.anchorNode){ // don't bother with user selection
            if(w._NWP) {
                var p=curr; while(p=p.parentElement)
                if(_NWP.indexOf(p)>=0) curr=0;
            }
            if(curr && w._YWPC) {
                var p=curr; o:while(p=p.parentElement){
                    for(var i=0,pc;pc=p.classList[i++];)
                    if(_YWPC.indexOf(pc)>=0) break o;
                    if(p.id && _YWPC.id && _YWPC.indexOf('#'+p.id)>=0) break;
                }
                if(!p) curr=0;
            }
            if(curr) {
                s.modify('extend', 'forward', 'word'); // first attempt
                var an=s.anchorNode;
                //_log(s.anchorNode); _log(s);
                //if(true) return;

                if(s.baseNode != d.body) {// immunize blank area
                    var text=s.toString(); // for word made up of just one character
                    var range = s.getRangeAt(0);

                    var br = range.getBoundingClientRect();
                    var pX = br.left;
                    var pY = br.top;
                    var pW = br.width;
                    var pH = br.height;
                    var cprY = e.clientY;
                    var cprX = e.clientX;

                    debug('tap=', cprX, cprY, pX, pY, pH, pH);
                    var pad=50;
                    if((cprY>pY-pad && cprY<pY+pH+pad && cprX>pX-pad && cprX<pX+pW+pad)){
                        s.collapseToStart();
                        s.modify('extend', 'forward', 'lineboundary');
    
                        if(s.toString().length>=text.length){
                            s.empty();
                            s.addRange(range);

                            s.modify('move', 'backward', 'word'); // now could noway be next line
                            s.modify('extend', 'forward', 'word');

                            var range1 = s.getRangeAt(0);
                            if(range1.endContainer===range.endContainer&&range1.endOffset===range.endOffset){
                                // for word made up of multiple character
                                text=s.toString();
                                br = range1.getBoundingClientRect();
                                pX = br.left;
                                pY = br.top;
                                pW = br.width;
                                pH = br.height;
                            }

                            //网页内部的位置，与缩放无关
                            //_log(rrect);
                            //_log(pX+' ~~ '+pY+' ~~ '+pW+' ~~ '+pH);
                            //_log(cprX+' :: '+cprY);
                            //_log(d.documentElement.scrollLeft+' px:: '+pX);

                            //_log(text); // final output
                            if(app && text.trim().length){
                                app.popupWord(sid.get(), text, 0/*frameAt */, d.documentElement.scrollLeft+pX, d.documentElement.scrollTop+pY, pW, pH);
                                w.popup=1;
                                //s.empty();
                                return true;
                            }
                        }
                    }
                }
            }
            //点击空白关闭点译弹窗
            //...
            s.empty();
        }
    }
    if(w.popup){
        app.popupClose(sid.get());
        w.popup=0;
    }
})}