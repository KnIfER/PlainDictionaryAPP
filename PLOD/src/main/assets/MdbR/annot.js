(function(){ // based on rangy.js
    var doc = document;
    var log = function(a,b,c,d,e){var t=[a,b,c,d,e];for(var i=5;i>=0;i--){if(t[i]===undefined)t[i]='';else break}console.log("%c ANNOT ","color:#000;background:#ffaaaa;",t[0],t[1],t[2],t[3],t[4])}
	function getNextNode(n, e) {
        var a = n.firstChild;
        if (a) {
            if(!skipIfNonTex(n)) { // 将这些考虑为没有文本的节点；不要进去。
                return a;
            }
            if(a==e) {
                return null;
            }
        }
		while (n) {
			if (a = n.nextSibling) {
				return a
			}
			n = n.parentNode
		}
	}
	function getNextNodeRaw(n) {
        var a = n.firstChild;
        if (a) return a;
		while (n) {
			if (a = n.nextSibling) {
				return a
			}
			n = n.parentNode
		}
	}
	function getNodesInRange(r) {
        //r=getSelection().getRangeAt(0);
		var b = [];
		var s = r.startContainer;
		var e = r.endContainer;
		var a = r.commonAncestorContainer;
		var n;
		for (n = s.parentNode; n; n = n.parentNode) {
			b.push(n);
			if (n == a) {
				break
			}
		}
		b.reverse();
		for (n = s; n; n = getNextNode(n, e)) {
            if(!n) break
			b.push(n);
			if (n == e) {
				break
			}
		}
		return b
	}
	function getNodeIndex(n) {
		var a = 0;
		while ((n = n.previousSibling)) {
			++a
		}
		return a
	}
	function insertAfter(b, n) {
		var a = n.nextSibling,
			c = n.parentNode;
		if (a) {
				c.insertBefore(b, a)
			} else {
				c.appendChild(b)
			}
		return b
	}
	function splitDataNode(n, a) {
		var b = n.cloneNode(false);
		b.deleteData(0, a);
		n.deleteData(a, n.length - a);
		insertAfter(b, n);
		return b
	}
	function isCharacterDataNode(b) {
		var a = b.nodeType;
		return a == 3 || a == 4 || a == 8
	}
	function splitRangeBoundaries(r) {
		var s = r.startContainer,
			o = r.startOffset,
			e = r.endContainer,
			l = r.endOffset;
		var d = (s === e);
		if (isCharacterDataNode(e) && l > 0 && l < e.length) {
				splitDataNode(e, l)
			}
		if (isCharacterDataNode(s) && o > 0 && o < s.length) {
				s = splitDataNode(s, o);
				if (d) {
					l -= o;
					e = s
				} else {
					if (e == s.parentNode && l >= getNodeIndex(s)) {
						++l
					}
				}
				o = 0
			}
		r.setStart(s, o);
		r.setEnd(e, l)
	}
	function getTextNodesInRange(b) {
		var f = [];
		var a = getNodesInRange(b);
		for (var c = 0, e, d; e = a[c++];) {
			if (e.nodeType == 3) {
				f.push(e);
			}
		}
		return f;
	}

    // attach back link to a refrence row
    function MultiRefLnk(doc, rootNode, backNid, refNid, refRow) {
        if(refRow) {
            function craft(t, p, c) {
                t = doc.createElement(t);
                t.nid = backNid;
                if(c)t.className=c;
                if(p)p.appendChild(t);
                return t;
            }
            if(!refRow.refSelf) {
                refRow.refSelf = true;
                MultiRefLnk(doc, rootNode, refRow.nid, refNid, refRow);
                var b = rootNode._pd_bn;
                if(b) b=b[refNid];
                if(b) {
                    for(var i;i<b.length;i++) {
                        MultiRefLnk(doc, rootNode, b[i], refNid, refRow);
                    }
                }
            }
            var sup = craft('SUP',0, 'RefSup');
            var lnk = craft('A', sup, 'RefBacks');
            lnk.href = '_pd_sup'+backNid;
            refRow.insertBefore(sup, refRow.lastElementChild)
            lnk.addEventListener('click', doc._pd_clks[1], 1);
        } else {
            var b = rootNode._pd_bn;
            if(!b) b=rootNode._pd_bn={}
            if(!b[refNid]) b[refNid]=[]
            b[refNid].push(backNid);
        }
    }

	function wrapRange(r, el, rootNode, doc, nid, neo) {
        var tcn = el.tcn;
        if(!el){
            el = doc.createElement("ANNOT");
            el.style="background:#ffaaaa;";
        }
        if(!r) r = getSelection().getRangeAt(0);
        log('wrapping...',r.startContainer, r.startOffset, r.endContainer, r.endOffset);
		splitRangeBoundaries(r);
        log('splitRangeBoundaries...',r.startContainer, r.startOffset, r.endContainer, r.endOffset);
        //getSelection().empty(0);
        //getSelection().addRange(r);
        if(!doc) doc = document;
        if(!rootNode) rootNode = doc.body;
		var f = getTextNodesInRange(r);
		if (f.length == 0) {
			return;
		}
        //var nodes = [];
        var first=0, last;
		for (var c = 0, e, d; e = f[c++];) {
			if (e.nodeType == 3) {
                //log(e);
				d = el.cloneNode(false);
				d.bg = tcn.bg;
                if(!first) {
                    d.id = '_pd_annot'+nid;
                    first = d;
                }
                last = d;
                // if(e.parentNode.tagName==='ANNOT' && e.parentNode.bg==d.bg) {
                //     whiterRgb(d, e.parentNode)
                // }
				e.parentNode.insertBefore(d, e);
				d.appendChild(e);
                //nodes.push(d);
                d.nid = nid;
			}
		}
        first.end = last;
        log('last=!!!!!!!!!!!!=',tcn.note,first,last);
		r.setStart(f[0], 0);
		var a = f[f.length - 1];
		r.setEnd(a, a.length);
        ///log('fatal web annot::wrapRange::', r);
        if(tcn.note) { // 文本笔记
            var noteType=tcn.ntyp, note=tcn.note;
            //noteType=1;
            d = el.cloneNode(false);
            d.className = 'note';
            d.id = '_pd_note'+nid;
            d.nid = nid;
            insertAfter(d, last);
            function craft(t, p, c) {
                var e=doc.createElement(t);
                e.nid = nid;
                if(c)e.className=c;
                (p||d).appendChild(e);
                return e;
            }
            function bubbleInto(el) {
                var id = '_pd_bsty'
                var sty = doc.getElementById(id);
                if(!sty) {
                    sty = craft('STYLE', doc.head);
                    sty.innerText = '._PDBP{position:relative}._PDBV::after{content:attr(data-tex)}._PDBX{position:absolute;left:95%;bottom:80%;}._PDB{border-radius: 16px;background:#abcdef;color:#fff;white-space:nowrap;padding:0px 8px;margin:0px;font-size:14px;}._PDBF>A{color:white}._PDBF{padding:2px 8px}';
                    sty.id = id;
                    sty.click = function(){
                        log('点击！！！');
                    }
                }
                if(el) {
                    el.style.position = 'relative';
                    el = craft('Annot',el,'_PDB _PDBX note');
                    // if(tcn.bnt) // no text selection
                    //     el.classList.add('_PDB_NT');
                    // if(tcn.bnc) // no click
                    //     el.classList.add('_PDB_NC');
                    //else el // click to edit note
                    el.addEventListener('click', sty.click);
                    if(tcn.bclr) 
                        el.style.background = toRgb(tcn.bclr);
                    return el;
                }
            }
            if(noteType==0) { // 正文
                if(note[0]!='(' && note[1]!='(')
                    note = ' ('+note+') ';
                d.innerText = note;
                if(tcn.bin) {
                    var bel=bubbleInto(d);
                    bel.innerHTML = '&emsp;';
                }
                if(tcn.fclr) 
                    d.style.color = toRgb(tcn.fclr);
                if(tcn.fsz) 
                    d.style.fontSize = (parseInt(tcn.fsz)||100)+'%';
                last = d;
            }
            else if(noteType==1) { // 气泡
                var bel = bubbleInto(last);
                if(tcn.bon) {
                    bel.innerText = note;
                } else {
                    //bel.innerText = '···';
                    bel.classList.add('_PDBV');
                    bel.setAttribute('data-tex', '···');
                }
                if(tcn.fclr) 
                    bel.style.color = toRgb(tcn.fclr);
                if(tcn.fsz) 
                    bel.style.fontSize = (parseInt(tcn.fsz)||100)+'%';
                d.parentNode.removeChild(d);
                last = bel;
            }
            else { // 脚注
                last = d;
                var lnkTo = null;
                if(note.startsWith('_pd_lnk='))
                    lnkTo = parseInt(note.slice(8));
                var id = '_pd_ftsy'
                var sty = doc.getElementById(id);
                if(!sty) {
                    sty = craft('STYLE', doc.head);
                    sty.innerText = '.RefList>LI:focus{background:rgba(5,109,232,.08)}SUP:focus{box-shadow:0 0 0 2px #ffffff, 0 0 0 4px rgb(5 109 232 / 30%)}.RefBack{color:#175199;padding-right:0.25em;font-weight:600;}.RefBack,SUP>A{text-decoration:none}.RefList>LI{counter-reset:_pd_ref}.RefSup{color:#175199;padding-right:0.25em;font-weight:600}.RefBacks:before{counter-increment:_pd_ref;content:counter(_pd_ref, lower-alpha)}._pdn_sup:before{counter-increment:_pdn_sup;content:\'[\'counter(_pdn_sup)\']\'}._PDict_body,body{counter-reset:_pdn_sup}';
                    sty.id = id;
                }
                var clk = doc._pd_clks;
                if(!clk)
                clk = doc._pd_clks = [function(e) {
                    var t = e.target || e.srcElement; 
                    e.preventDefault(); e.stopPropagation();
                    log('  focus', t);
                    t = t.href+'';
                    t = doc._pd_foc = doc.getElementById(t.slice(t.indexOf('#')+1));
                    t.focus();
                    return true;
                }, function(e) {
                    var t = e.target || e.srcElement; 
                    e.preventDefault(); e.stopPropagation();
                    doc.getElementById('_pd_sup'+t.nid).focus();
                    return true;
                }, function(e) {
                    var t = e.target || e.srcElement; 
                    if(doc._pd_foc===t) {
                        doc.getElementById('_pd_sup'+t.nid).focus();
                    }
                    doc._pd_foc = t;
                }]

                if(lnkTo!==null)
                    id = '_pd_ref'+lnkTo;
                else
                    id = '_pd_ref'+nid;
                var has=doc.getElementById(id);
                var num = 0;
                //if(!neo) {
                if(0) {
                    num = rootNode._pd_refs||0;
                    if(!has) {
                        num = rootNode._pd_refs = num+1;
                    }
                } else {
                    if(rootNode._pd_ref) {
                        function reduce(arr, p, st, ed) { // 二分法
                            var len = ed - st;
                            if (len > 1) {
                              len = len >> 1;
                              //log('reduce', st, len, ed);
                              return p > (arr[st + len - 1].tPos||0)
                                        ? reduce(arr, p, st+len, ed)
                                        : reduce(arr, p, st, st+len);
                            } else {
                              return st;
                            }
                        }
                        var lst = rootNode._pd_ref.childNodes;
                        num = (reduce(lst, tcn.tPos, 0, lst.length))||0;
                        log('reduce=', tcn.note, num)
                    }
                }
                var sup = craft('SUP');
                var lnk = craft('A', sup);
                //lnk.innerText = '['+num+']';
                lnk.className = '_pdn_sup';
                lnk.href = '#'+id;
                sup.id = '_pd_sup'+nid;
                lnk.addEventListener('click', clk[0], 1);
                if(tcn.bin) {
                    bubbleInto(0);
                    sup.classList.add('_PDB')
                    sup.classList.add('_PDBF')
                }
                sup.setAttribute('tabindex',0);

                if(!rootNode._pd_ref) {
                    var refP = craft('ANNOT', rootNode, 'note');
                    craft('H2', refP).innerText = '笔记';
                    rootNode._pd_ref = craft('OL', refP, 'RefList');
                }
                if(lnkTo!==null) {
                    MultiRefLnk(doc, rootNode, nid, lnkTo, has);
                }
                else if(!has) {
                    var row = craft('LI', rootNode._pd_ref);
                    row.setAttribute('tabindex',0)
                    row.id = id;
                    row.tPos = tcn.tPos;
                    lnk = craft('A', row, 'RefBack');
                    lnk.innerText = '^';
                    lnk.href = '#'+sup.id;
                    craft('SPAN', row).innerText = note;
                    row.addEventListener('click', clk[2]);
                    lnk.addEventListener('click', clk[1], 1);
                    if(true && num>=0) {
                        var nodes = rootNode._pd_ref.childNodes, n = nodes[num], ibf=0;
                        while(n && n.tPos>tcn.tPos) {
                            ibf = n;
                            n = n.nextElementSibling;
                        }
                        if(ibf && ibf!=row) rootNode._pd_ref.insertBefore(row, ibf);
                    }
                }
            }
            log('笔记=', d, last)
        }
        first.end = last;
        log('last=',tcn.note,first,last);
	}
    function getNodeIndex(node) {
        var i = 0;
        while( (node = node.previousElementSibling) ) {
            //log(node);
            ++i;
        }
        return i;
      }

    function serializePosition(node, offset, rootNode) {
        if(true) return storePos(node, offset, rootNode);
        var pathParts = [], n = node;
        while (n && n != rootNode) {
            pathParts.push(getNodeIndex(n, true));
            n = n.parentNode;
        }
        return pathParts.join("/") + ":" + offset;
    }
    function deserializePosition(serialized, rootNode, ex) {
      if(!rootNode) rootNode = doc.body;
      if(true) return restorePos(serialized, rootNode, ex);
      var parts = serialized.split(":");
      var node = rootNode;
      var nodeIndices = parts[0] ? parts[0].split("/") : [], i = nodeIndices.length, nodeIndex;

      while (i--) {
          nodeIndex = parseInt(nodeIndices[i], 10);
          if (nodeIndex < node.childNodes.length) {
              node = node.childNodes[nodeIndex];
          } else {
              log( " has no child with index " + nodeIndex + ", " + i + ", " + node.childNodes.length);
              return;
          }
      }
      return [node, parseInt(parts[1], 10)]; // node, offset
    }
    
	// function getNextNode(n) {
	// 	var a = n.firstChild;
	// 	if (a) {
	// 		return a
	// 	}
	// 	while (n) {
	// 		if ((a = n.nextSibling)) {
	// 			return a
	// 		}
	// 		n = n.parentNode
	// 	}
	// }
	function getRelIndex(n) {
		var a = 0;
		while ((n = n.previousElementSibling)) {
            if(!skip(n))
			    ++a
		}
		return a
	}
    function storePos(n, o, rootNode) {
        var ret = [], p = n.parentNode;
        while (p && skip(p)) {
            p = p.parentNode;
        }
        // 获得原始非标注父节点
        if(p) {
            for (var t=p; t; t = getNextNode(t)) {
                if (t == n) {
                    break
                }
                if(t.nodeType == 3) {
                    o += t.length;
                }
            }
            while (p && p != rootNode) {
                ret.push(getRelIndex(p, true));
                p = p.parentNode;
            }
        }
        ret = ret.reverse();
        return ret.join("/") + ":" + o;
    }

    function storeTextPos(n, o, rootNode) {
        var p = n.parentNode;
        while (p && skip(p)) {
            p = p.parentNode;
        }
        // 获得原始非标注父节点
        var debug = false;
        if(p) {
            for (var t=p; t; t = getNextNode(t)) {
                if (t == n) {
                    break
                }
                if(t.nodeType == 3) {
                    if(debug) log('1::', t, t.parentNode, t.nodeType, t.length);
                    o += t.length;
                }
            }
            if(debug) log('');
            for (var t=rootNode; t; t = getNextNode(t)) {
                //if(debug)  log('2::', t);
                if (t == p) {
                    break
                }
                if(t.nodeType == 3 && /\S/.test(t.nodeValue)) {
                    o += t.length;
                    if(debug)  log('2::len=', t.length, o, t, "tex="+t.nodeValue);
                }
            }
        }
        log('storeTextPos', o);
        return o;
    }
    function skip(n) {
        if(n.nodeType==1)
            return n.tagName=='ANNOT'||n.tagName=='STYLE'||n.tagName=='LINK'||n.tagName=='SCRIPT'||n.tagName=='MARK'||n.classList.contains('_PDict');
        else return n.nodeType!==3;
    }
    function skipIfNonTex(n) {
        if(n.nodeType==1)
            return (n.tagName=='ANNOT'&&n.classList.contains('note'))||n.tagName=='STYLE'||n.tagName=='LINK'||n.tagName=='SCRIPT'||n.classList.contains('_PDict');
        else return n.nodeType!==3;
    }
    function restorePos(str, rootNode, ex) {
        var parts = str.split(":");
        var node = rootNode;
        var nodeIndices = parts[0] ? parts[0].split("/") : [], i = 0, ln=nodeIndices.length, nodeIndex;

        while (i<ln) {
            nodeIndex = parseInt(nodeIndices[i], 10);
            var n = node.firstElementChild;
            while (n && skip(n)) {
                n = n.nextElementSibling;
            }
            while(nodeIndex>0 && n) { //???
                n = n.nextElementSibling;
                if(n && !skip(n))
                    nodeIndex--;
            }
            if (!n) {
                console.log(str, " has no child with indice " + nodeIndices[i] + ", " + (i+1)+'/'+ln + ", " + node.childNodes.length, node);
                break;
            }
            //log(str, " found " + nodeIndices[i] + ", " + (i)+'/'+ln + ", " + node.childNodes.length, node, n);
            node = n;
            i++;
        }
        if (!node) {
            return;
        }
        var o=0,l=parseInt(parts[1], 10),ln=l;
        if(ex) ln++;
        //log("restorePos at : ", node, l);
        for (var t=node; t; t = getNextNode(t)) {
            //if (t == ed) break; //未作边界检查，实际t可能超出初始node范围
            if(t.nodeType == 3) {
                //log("restorePos : ", t, t.length, o);
                if(o + t.length>=ln) { // ???
                    node = t;
                    l -= o;
                    break;
                }
                o += t.length;
            }
        }

        return [node, l]; // node, offset
    }

    function test(r) {
        // r = getSelection().getRangeAt(0);
        // var rootNode = doc.body;
        // return storePos(r.startContainer, r.startOffset, rootNode);
        range = storeRange()
        getSelection().empty();
        getSelection().addRange(makeRange(range[0], range[1]))
    }

    
    function restoreRange() {
        getSelection().empty();
        getSelection().addRange(makeRange(range[0], range[1]))
    }

    function getdocRangy(node) {
        if (node.nodeType == 9) {
            return node;
        } else if (typeof node.ownerdoc != undefined) {
            return node.ownerdoc;
        } else if (typeof node.doc != undefined) {
            return node.doc;
        } else if (node.parentNode) {
            return getdocRangy(node.parentNode);
        } else {
            throw module.createError("getdoc: no doc found for node");
        }
    }

    var range;
    function storeRange(r) {
        r = getSelection().getRangeAt(0);
        //var rootNode = this.getdocRangy(range.startContainer).docElement;
        var rootNode = doc.body;
        return [serializePosition(r.startContainer, r.startOffset, rootNode), serializePosition(r.endContainer, r.endOffset, rootNode)];
    }
    function store(r, rootNode) {
        var p1 = storePos(r.startContainer, r.startOffset, rootNode);
        var p2 = storePos(r.endContainer, r.endOffset, rootNode);
        var b = p1.length > p2.length, ln = b?p1.length:p2.length, i=0;
        for(;i<ln;i++) {
            if(p1[i]!=p2[i]) break;
        }
        if(i>0) return p1.substring(0, i)+';'+p1.substring(i)+';'+p2.substring(i);
        else return p1+';'+p2;
    }
    function restore() {
        getSelection().empty();
        getSelection().addRange(makeRange(range[0], range[1]))
    }
    
    function makeRange(r0, r1, rootNode, d) {
        doc = d?d:document;
        //if(app.done) return; app.done=1;
        if(r0.length==0||r1.length==0) return null;
        //var doc = doc.body;
        //var result = serialized.split(',');
        //todo checksum
        //log('1__'+result[0]);
        //log('2__'+result[1]);
        var start = deserializePosition(r0, rootNode, 1), end = deserializePosition(r1, rootNode);
        var range = new Range();
        if(start&&end) {
            try{
            range.setStart(start[0], start[1]);
            range.setEnd(end[0], end[1]);
            return range;
            } catch(e){}
        }
    }

    function whiterRgb(n, bf, a){
        if(a==null)a=0.25;
        function mw(ch, p){
            return (0x88*p+ch*(1-p));
        }
        var c = bf._bgr;
        if(c===undefined) c=bf.bg;
        var t = (c>>24)&0xff;
        var r = mw((c>>16)&0xff,a);
        var g = mw((c>>8)&0xff,a);
        var b = mw(c&0xff,a);
        var sty = t?"rgba("+r+","+g+","+b+"/"+t+")":"rgb("+r+","+g+","+b+")";
        n.style.backgroundColor = sty;
        log('sty='+sty);
        n._bgr = (t<<24) | (r<<16) | (g<<8) | (b);
    }

    function toRgb(c){
        if(c===undefined) c=0xffffaaaa;
        var t = (c>>24)&0xff;
        var r = (c>>16)&0xff;
        var g = (c>>8)&0xff;
        var b = c&0xff;
        return t && t!=0xff?"rgba("+r+" "+g+" "+b+" / "+parseInt(t*100.0/256)+"%)":"rgb("+r+","+g+","+b+")";
    }

	function getNidsInRange(det) {
        var rg=getSelection().getRangeAt(0);
        log("NidsInRange::", det, rg);
        if(det) {
            var el=rg.startContainer, e=rg.endContainer, p=el.parentNode;
            while(el) {
                if(p && p.tagName==='ANNOT' && p.nid!==undefined) {
                    return 1;
                }
                if(el.tagName==='ANNOT' && el.nid!==undefined) {
                    return 1;
                }
                if(el==e) break;
                el = getNextNodeRaw(el);
            }
            return 0;
        } else {
            var ret='', f = {}, a = getNodesInRange(rg), p = rg.startContainer.parentNode;
            while(p && p.tagName==='ANNOT') {
                a.push(p);
                p = p.parentNode;
            }
            for (var c = 0, e; e = a[c++];) {
                log("\t\tgetNidsInRange::", e, e.nid);
                if (e.nid!=undefined) {
                    if(!f[e.nid]) {
                        f[e.nid]=1;
                        if(ret.length) ret+=',';
                        ret+=e.nid;
                    }
                }
            }
            return ret;
        }
	}

    function deWrap(ds) {
		for (var e = ds.length - 1, d; e >= 0; e--) {
            d = ds[e];
            if(!d.classList.contains('note')) {
                var c = 0;
                for (var f = d.childNodes.length - 1; f >= 0; f--) {
                    var a = d.childNodes[f];
                    d.parentNode.insertBefore(a, c?c:d);
                    c = a
                }
            }
            d.parentNode.removeChild(d)
        }
	}

    function patchNote(nid, tcn) {
        var el = document.getElementById('_pd_annot'+nid);
        console.log('fatal patchNote::', nid, el, tcn);
        if(el) {
            var e = el.end, nds=[];
            log('patchNote::', nid, el, e);
            while(el) {
                if(el.nid===nid) {
                    nds.push(el);
                    //log('should deWrap::', el);
                }
                if(el==e) break;
                el = getNextNodeRaw(el);
            }
            deWrap(nds);
        }
        el = document.getElementById('_pd_ref'+nid);
        if(el) {
            el.remove();
        }
        if(tcn) {
            var rootNode = document.body, doc = document;
            var row = JSON.parse(tcn);
            var nn = row.n.split(';'), n0=nn[0], n1=nn[1];
            if(nn.length==3) {n0=nn[0]+n1; n1=nn[0]+nn[2]}
            else if(nn.length!=2) return;
            var r = makeRange(n0, n1, rootNode, doc);
            console.log('fatal log annot::renewing::', tcn, r);
            if(r) {
                var el = annot(row, -1, 0, 0);
                wrapRange(r, el, rootNode, doc, nid)
            }
        }
    }

    function annot(tcn, rootNode, doc, pos, bid) {
        log('MakeAnnotation::', tcn);
        if(typeof tcn==='string') tcn = JSON.parse(tcn);
        var type=tcn.typ, color=tcn.clr, note=tcn.note;
        if(type==undefined) type=0;
        if(!doc) doc = document;
        var el = doc.createElement("ANNOT");
        if(type==0) {
            el.className = "PLOD_HL";
            el.setAttribute("style", "background:"+toRgb(color));
        } else {
            el.className = "PLOD_UL";
            //ann.style = "color:#ffaaaa;text-decoration: underline";
            el.setAttribute("style", "border-bottom:4px solid "+toRgb(color));
        }
        el.tcn = tcn;
        if(rootNode==-1) return el;
        var sel = window.getSelection();
        try {
            var nntd = '_pd_nntd'
            var sty = doc.getElementById(nntd);
            if(!sty) {
                sty = doc.createElement('STYLE');
                doc.head.appendChild(sty);
                sty.id = nntd;
            }
            sty.innerText = 'annot.note{visibility:hidden}'; // skip note texts
            var text = sel.toString();
            sty.innerText = '';
            if(text.length==0) return;
            var range = sel.getRangeAt(0);
            if(!rootNode) rootNode = doc.body;
            var tPos = storeTextPos(range.startContainer, range.startOffset, rootNode);
            var r = store(range, rootNode);
            log('tPos='+tPos)
            tcn.n = r;
            tcn.tPos = tPos;
            if(pos==undefined)
                pos = window.currentPos || 0; 
            //var nid = app.annot(sid.get(), text, JSON.stringify(tcn), window.entryKey||null, pos, tPos, type, color, note, bid||null);
            //wrapRange(range, el, rootNode, doc, nid, true)
        } catch (e) { log(e) }
    }

    window.MakeMark=annot;
    window.MakeRange=makeRange;
    window.WrapRange=wrapRange;
    window.NidsInRange=getNidsInRange;
    window.PatchNote=patchNote;
})();