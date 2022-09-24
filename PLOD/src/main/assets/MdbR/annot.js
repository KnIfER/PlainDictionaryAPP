//(function(){ // based on rangy.js
    var doc = document;
    function log(...e) {console.log(e)}
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
	function wrapRange(r, n, rootNode, nid) {
        if(!n){
            n = doc.createElement("ANNOT");
            n.style="background:#ffaaaa;";
        }
        if(!r) r = getSelection().getRangeAt(0);
        log('wrapping...',r.startContainer, r.startOffset, r.endContainer, r.endOffset);
		splitRangeBoundaries(r);
        log('splitRangeBoundaries...',r.startContainer, r.startOffset, r.endContainer, r.endOffset);
        //getSelection().empty(0);
        //getSelection().addRange(r);
		var f = getTextNodesInRange(r);
		if (f.length == 0) {
			return;
		}
        var nodes = [];
		for (var c = 0, e, d; e = f[c++];) {
			if (e.nodeType == 3) {
                //log(e);
				d = n.cloneNode(false);
				d.bg = n.bg;
                // if(e.parentNode.tagName==='ANNOT' && e.parentNode.bg==d.bg) {
                //     whiterRgb(d, e.parentNode)
                // }
				e.parentNode.insertBefore(d, e);
				d.appendChild(e);
                nodes.push(d);
                d.nid = nid;
			}
		}
		r.setStart(f[0], 0);
		var a = f[f.length - 1];
		r.setEnd(a, a.length);
        debug('fatal web annot::wrapRange::', r);
        if(n.note) {
            var last = nodes[nodes.length-1]
            d = n.cloneNode(false);
            d.innerText = n.note;
            d.className = 'note';
            insertAfter(d, last);
            log('笔记=', d, last)
        }
        return nodes;
	}
    function getNodeIndex(node) {
        var i = 0;
        while( (node = node.previousElementSibling) ) {
            //debug(node);
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
        if(p) {
            for (var t=p; t; t = getNextNode(t)) {
                if (t == n) {
                    break
                }
                if(t.nodeType == 3) {
                    debug('1::', t, t.parentNode, t.nodeType, t.length);
                    o += t.length;
                }
            }
            debug('');
            for (var t=rootNode; t; t = getNextNode(t)) {
                debug('2::', t, t.parentNode, t.nodeType, p, t.length);
                if (t == p) {
                    break
                }
                if(t.nodeType == 3) {
                    o += t.length;
                }
            }
        }
        debug('storeTextPos', o);
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
        debug('sty='+sty);
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

    function annot(type, color, note, rootNode, doc, pos, bid) {
        log('MakeAnnotation::', type, color, note);
        //note = ' (笔记测试) ';
        if(type==undefined) type=0;
        //color = 0xffaaaad0; // todo 长按修改颜色
        if(!doc) doc = document;
        var ann = doc.createElement("ANNOT");
        if(type==0) {
            ann.className = "PLOD_HL";
            ann.setAttribute("style", "background:"+toRgb(color));
        } else {
            ann.className = "PLOD_UL";
            //ann.style = "color:#ffaaaa;text-decoration: underline";
            ann.setAttribute("style", "border-bottom:4px solid "+toRgb(color));
        }
        ann.bg = color;
        ann.note = note;
        if(rootNode==-1) return ann;
        var sel = window.getSelection();
        try {
            var text = sel.toString();
            var range = sel.getRangeAt(0);
            if(!rootNode) rootNode = doc.body;
            var tPos = storeTextPos(range.startContainer, range.startOffset, rootNode);
            var r = store(range, rootNode);
            var nodes = wrapRange(range, ann)
            if(pos==undefined)
                pos = window.currentPos || 0; 
            var nota = {};
            nota.n = r;
            if(type) nota.typ = type;
            if(color) nota.clr = color;
            if(note) nota.note = note;
            var nid = app.annot(sid.get(), text, JSON.stringify(nota), window.entryKey||null, pos, tPos, type, color, note, bid||null);
            for(var i,n;n=nodes[i++];) {
                n.nid = nid;
            }
        } catch (e) { log(e) }
    }

    window.MakeMark=annot;
    window.MakeRange=makeRange;
    window.WrapRange=wrapRange;
//})();