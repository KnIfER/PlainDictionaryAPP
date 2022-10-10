if(!window.tpshc){
    if(window.shzh==undefined) {
        window.shzh=app.rcsp(sid.get());
    }
    let debug = function(a,b,c,d,e){var t=[a,b,c,d,e];for(var i=5;i>=0;i--){if(t[i]===undefined)t[i]='';else break}console.log("%cTapSch","color:(#FFF);background:#00f;",a,b,c,d,e)}
    debug('popuping ini...shzh='+window.shzh);
    var tRange, tTime;
    // 0=word; 1=paragraph; 
    var granu;
    var bAutoGranu = true;
    var dx, dy;
    window.addEventListener('touchstart',window.tpshts=function(e){
        var w=this;
        if(!w.getSelection().isCollapsed) {
            tRange = w.getSelection().getRangeAt(0);
            var t = e.changedTouches[0];
            dx = t.clientX;
            dy = t.clientY;
            tTime = e.timeStamp;
        } else if(tRange) {
            tRange = 0;
        }
    });
    window.addEventListener('touchend',window.tpshtd=function(e){
        //console.log('popuping ini...shzh=', t.clientX, t.clientY, t.pageX, t.pageY);
        if(tRange && bAutoGranu && granu>0) {
            if(e.timeStamp-tTime>=350) return
            var t = e.changedTouches[0];
            dx=t.clientX-dx;
            dy=t.clientY-dy;
            // make sure that distance between touchstart and touchend smaller than some threshold,
            // e.g. <= 16 px
            if (dx*dx < 80 && dy*dy< 80) {
                var rs=tRange.getClientRects(),rc = rs[0], x=t.clientX, y=t.clientY;
                // debug('\n')
                // debug('边缘触发', x, y, x<=rc.x+50 , x>=rc.x , y>=rc.y , y<=rc.bottom)
                // debug('边缘触发', rc.x, rc.right , rc.y , rc.bottom)
                // debug('\n')
                var pad = 18;
                if(x<=rc.left+pad && x>=rc.left && y>=rc.top && y<=rc.bottom) {
                    return;
                }
                rc = rs[rs.length-1];
                if(x<=rc.right && x>=rc.right-pad && y>=rc.top && y<=rc.bottom) {
                    return;
                }
                debug('将触发点击事件，提前清空选择。', dx*dx, dy*dy)
                w.getSelection().empty();
            }
        }
    });
    window.addEventListener('click',window.tpshc=function(e){
    var w=this,d=w.document,sz=w.shzh,app=w.app;
    if(w.frameElement){sz=parent.window.shzh;app=parent.window.app}
    debug('wrappedClickFunc 2', e);
    var curr = e.srcElement;
    debug('popuping...设置=', sz);
    function pointInRange(r, rg, rc) {
        var x = rc.left;
        var y = rc.top;
        var w = parseInt(rc.width);
        var h = parseInt(rc.height);
        var pY = e.clientY;
        var pX = e.clientX;
        var pad=50;
        return w>0 && y>0
        && (pY>y-pad && pY<y+h+pad && pX>x-pad && pX<x+w+pad);
    }
    if(sz&7 && curr!=d.documentElement && (curr.nodeName!='TEXTAREA'&&curr.nodeName!='INPUT'||curr.readOnly) && curr.nodeName!='BUTTON' && curr.nodeName!='A' && !curr.noword && !curr.onclick){
        //todo d.activeElement.tagName
        var s = w.getSelection();
        if(!(s.isCollapsed && s.anchorNode)) {
            // don't bother with user selection
            return;
        }
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
            var range = s.getRangeAt(0), rg, rc;

            // 获得落点处字符方框
            s.modify('extend', 'backward', 'character'); 
            rg = s.getRangeAt(0);
            rc = rg.getBoundingClientRect();
            if(!(rc.left<=e.x && rc.right>=e.x)) {
                s.empty(); s.addRange(range);
                s.modify('extend', 'forward', 'character'); 
                rg = s.getRangeAt(0);
                rc = rg.getBoundingClientRect();
            }
            if(pointInRange(e, rg, rc)) {
                var tapSel = sz&6;
                debug('fatal 单字='+rg, 'tapSel='+tapSel, 'granu='+granu);
                if(tapSel) {
                    s.empty();s.addRange(rg);
                    var nntd = '_pd_nntd'
                    var sty = d.getElementById(nntd);
                    if(!sty) {
                        sty = d.createElement('STYLE');
                        d.head.appendChild(sty);
                        sty.id = nntd;
                    }
                    //sty.innerText = '._PDB.note{visibility:hidden}'; // skip note texts
                    sty.innerText = '._PDB.note{visibility:hidden}'; // skip note texts
                    s.modify('extend', 'backward', 'paragraphboundary');
                    var tx1 = s.toString();
                    var now = tx1.length;
                    s.collapseToStart();
                    s.modify('extend', 'forward', 'paragraphboundary');
                    
                    if(tapSel==2) granu=0;
                    else {
                        if(bAutoGranu && tRange) {
                            debug('检查包含点', granu)
                            if(tRange.isPointInRange(rg.startContainer, rg.startOffset)) {
                                debug('包含点!!!');
                                if(granu==1) {
                                    granu = 0;
                                }
                            } else {
                                granu = 1;
                            }
                        } else {
                            granu = 1;
                        }
                    }

                    if(granu==0) 
                    {
                        //console.time('probeWord')
                        var sted = app.probeWord(sid.get(), s.toString(), tx1);
                        //console.timeEnd('probeWord')

                        var tst, ted=sted&0xFFFFFFFF;
                        var num = (sted).toString(16);
                        tst = parseInt('0x'+num.slice(0,num.length-8))||0;
                        
                        debug('wrappedClickFunc=', tst, ted, now);
                        
                        if(now>=tst && now<=ted) {
                            s.empty();s.addRange(rg);
                            s.collapseToStart();
                            var r=s.getRangeAt(0);
                            var st=r.startContainer,so=r.startOffset,ed=r.endContainer,eo=r.endOffset;
                            //debug('r='+r, st, so, ed, eo);
                            //debug('movebackward', now-tst);
                            for(var i=0;i<now-tst;i++) {
                                s.modify('extend', 'backward', 'character');
                                r=s.getRangeAt(0);
                                st=r.startContainer;so=r.startOffset;
                            }
                            s.empty();s.addRange(rg);
                            s.collapseToStart();
                            for(var i=0;i<ted-now;i++) {
                                s.modify('extend', 'forward', 'character');
                                r=s.getRangeAt(0);
                                ed=r.endContainer;eo=r.endOffset;
                            // ed=r.startContainer;eo=r.startOffset;
                            }
                            r = new Range();
                            r.setStart(st, so);
                            r.setEnd(ed, eo);
                            s.empty();
                            s.addRange(r);
                        }
                    }
                    
                    sty.innerText=''
                    
                    return
                } else {
                    s.empty(); s.addRange(rg);
                    s.collapseToStart();
                    s.modify('move', 'backward', 'word');
                    s.modify('extend', 'forward', 'word');
                    range = s.getRangeAt(0);
                    if(!(range.comparePoint(rg.startContainer, rg.startOffset)==0
                        && range.comparePoint(rg.endContainer, rg.endOffset)==0)) {
                        s.empty(); s.addRange(rg);
                        s.collapseToStart();
                        s.modify('extend', 'forward', 'word');
                        range = s.getRangeAt(0);
                    }
                    
                    var text = s.toString();
                    if(app && text.trim().length){
                        rc = range.getBoundingClientRect();
                        app.popupWord(sid.get(), text, 0/*frameAt */, d.documentElement.scrollLeft+rc.left, d.documentElement.scrollTop+rc.top, rc.width, rc.height);
                        w.popup=1;
                        //s.empty();
                        return true;
                    }
                }
            }
        }
        //点击空白关闭点译弹窗
        //...
        s.empty();
    }
    if(w.popup){
        app.popupClose(sid.get());
        w.popup=0;
    }
})}