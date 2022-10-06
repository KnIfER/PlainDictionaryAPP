if(!window.tpshc){
    if(window.shzh==undefined) {
        window.shzh=app.rcsp(sid.get());
    }
    console.log('popuping ini...shzh='+window.shzh);
    window.addEventListener('click',window.tpshc=function(e){
    var w=this,d=w.document,sz=w.shzh,app=w.app;
    if(w.frameElement){sz=parent.window.shzh;app=parent.window.app}
    console.log('wrappedClickFunc 2', e);
    var curr = e.srcElement;
    console.log('popuping...设置=', sz);
    if(sz&7 && curr!=d.documentElement && (curr.nodeName!='TEXTAREA'&&curr.nodeName!='INPUT'||curr.readOnly) && curr.nodeName!='BUTTON' && curr.nodeName!='A' && !curr.noword && !curr.onclick){
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
                var range = s.getRangeAt(0), rg1, rg2, rg;
                // 定位点击的文本框
                s.modify('extend', 'backward', 'character'); 
                rg1 = s.getRangeAt(0);
                s.empty(); s.addRange(range);
                s.modify('extend', 'forward', 'character'); 
                rg2 = s.getRangeAt(0);
                var rc1 = rg1.getBoundingClientRect(), rc2 = rg2.getBoundingClientRect();
                if(rc2.left<=e.x && rc2.right>=e.x/*  && rc2.top<=e.y && rc2.bottom>=e.y */) {
                    s.empty(); s.addRange(rg2);
                    rg = rg2;
                } else {
                    rg = rg1;
                }
                var tapSel = shzh&6;
                console.log('fatal 单字='+rg, tapSel);
                if(tapSel) {
                    if(rg) {
                        console.log('popuping...单字='+rg, tapSel);
                        s.empty();s.addRange(rg);
                        if(1) {
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
    
                            if(tapSel == 2) {
                                //console.time('probeWord')
                                var sted = app.probeWord(sid.get(), s.toString(), tx1);
                                //console.timeEnd('probeWord')
        
                                var tst, ted=sted&0xFFFFFFFF;
                                var num = (sted).toString(16);
                                tst = parseInt('0x'+num.slice(0,num.length-8))||0;
                                
                                console.log('wrappedClickFunc=', tst, ted, now);
                                
                                if(now>=tst && now<=ted) {
                                    s.empty();s.addRange(rg);
                                    s.collapseToStart();
                                    var r=s.getRangeAt(0);
                                    var st=r.startContainer,so=r.startOffset,ed=r.endContainer,eo=r.endOffset;
                                    //console.log('r='+r, st, so, ed, eo);
                                    //console.log('movebackward', now-tst);
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
    
                        }
                        return
                    }
                } else {
                    s.modify('extend', 'forward', 'word'); // first attempt
                    range = s.getRangeAt(0);
                }

                //if(rc1.left<=e.x && rc1.right>=e.x/*  && rc1.top<=e.y && rc1.bottom>=e.y */) {
                //    rg = rg1;
                //} 



               // if(true) return;
                    // var rg = new Range();
                    // rg.setStart(st, so);
                    // rg.setEnd(ed, eo);
                    // s.empty();
                    // s.addRange(rg);

                var an=s.anchorNode;
                //_log(s.anchorNode); _log(s);

                if(s.baseNode != d.body) {// immunize blank area
                    var text=s.toString(); // for word made up of just one character
                    var range = s.getRangeAt(0);

                    var br = range.getBoundingClientRect();
                    var pX = br.left;
                    var pY = br.top;
                    var pW = parseInt(br.width);
                    var pH = parseInt(br.height);
                    var cprY = e.clientY;
                    var cprX = e.clientX;

                    //debug('tap=', pW, pH, parseInt(cprX), parseInt(cprY), parseInt(pX)+'~'+parseInt(pX+pW), parseInt(pY)+'~'+parseInt(pY+pH));
                    var pad=50;
                    if(pW>0 && pY>0 
                        && (cprY>pY-pad && cprY<pY+pH+pad && cprX>pX-pad && cprX<pX+pW+pad)){
                        s.collapseToStart();
                        
                        // if(1) {
                        //     // s.modify('move', 'backward', 'sentence');
                        //     // s.modify('extend', 'forward', 'paragraph');

                        //     s.modify('extend', 'backward', 'paragraphboundary');
                        //     var rg = s.getRangeAt(0);
                        //     var st=rg.startContainer, so=rg.startOffset;
                        //     s.collapseToStart();
                        //     s.modify('extend', 'forward', 'paragraphboundary');
                        //     rg = s.getRangeAt(0);
                        //     var ed=rg.endContainer, eo=rg.endOffset;

                        //     debug('st,so,ed, eo', st,so,ed, eo);
                        //     rg = new Range();
                        //     rg.setStart(st, so);
                        //     rg.setEnd(ed, eo);

                        //     s.empty();
                        //     s.addRange(rg);

                        //     return;
                        // }

                        s.modify('extend', 'forward', 'lineboundary');
                        if(s.toString().length>=text.length)
                        {
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