	function getNextNode(n) {
		var a = n.firstChild;
		if (a) {
			return a
		}
		while (n) {
			if ((a = n.nextSibling)) {
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
		for (n = s; n; n = getNextNode(n)) {
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
	function wrapRange(r, n) {
        n = document.createElement("ANNOT");
        n.style="background:#ffaaaa;";
        r = getSelection().getRangeAt(0);
		splitRangeBoundaries(r);
		var f = getTextNodesInRange(r);
		if (f.length == 0) {
			return;
		}
		for (var c = 0, e, d; e = f[c++];) {
			if (e.nodeType == 3) {
				d = n.cloneNode(false);
				e.parentNode.insertBefore(d, e);
				d.appendChild(e);
			}
		}
		r.setStart(f[0], 0);
		var a = f[f.length - 1];
		r.setEnd(a, a.length);
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
    function deserializePosition(serialized, rootNode) {
      rootNode = document.body;
      if(true) return restorePos(serialized, rootNode);
      var parts = serialized.split(":");
      var node = rootNode;
      var nodeIndices = parts[0] ? parts[0].split("/") : [], i = nodeIndices.length, nodeIndex;

      while (i--) {
          nodeIndex = parseInt(nodeIndices[i], 10);
          if (nodeIndex < node.childNodes.length) {
              node = node.childNodes[nodeIndex];
          } else {
              console.log( " has no child with index " + nodeIndex + ", " + i + ", " + node.childNodes.length);
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
            if(n.tagName!=='ANNOT')
			    ++a
		}
		return a
	}
    function storePos(n, o, rootNode) {
        var ret = [], p = n.parentNode;
        while (p && p.tagName=='ANNOT') {
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
        return ret.join("/") + ":" + o;
    }
    function restorePos(str) {
        var rootNode = document.body;
        var parts = str.split(":");
        var node = rootNode;
        var nodeIndices = parts[0] ? parts[0].split("/") : [], i = nodeIndices.length, nodeIndex;

        while (i--) {
            nodeIndex = parseInt(nodeIndices[i], 10);
            var n = node.firstElementChild;
            while (n && n.tagName=='ANNOT') {
                n = n.nextElementSibling;
            }
            while(nodeIndex>0 && n) { //???
                n = n.nextElementSibling;
                if(n && n.tagName!=='ANNOT')
                    nodeIndex--;
            }
            if (!n) {
                console.log(range[0], " has no child with indice " + nodeIndices[i] + ", " + i + ", " + node.childNodes.length, node);
                break;
            }
            node = n;
        }
        if (!node) {
            return;
        }
        console.log(node);
        var o=0,l=parseInt(parts[1], 10);
        for (var t=node; t; t = getNextNode(t)) {
            //if (t == ed) break; //未作边界检查，实际t可能超出初始node范围
            if(t.nodeType == 3) {
                if(o + t.length>l) { // ???
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
        // var rootNode = document.body;
        // return storePos(r.startContainer, r.startOffset, rootNode);
        range = storeRange()
        getSelection().empty();
        getSelection().addRange(makeRange(range[0], range[1]))
    }

    
    function restoreRange() {
        getSelection().empty();
        getSelection().addRange(makeRange(range[0], range[1]))
    }


    function getDocumentRangy(node) {
        if (node.nodeType == 9) {
            return node;
        } else if (typeof node.ownerDocument != undefined) {
            return node.ownerDocument;
        } else if (typeof node.document != undefined) {
            return node.document;
        } else if (node.parentNode) {
            return getDocumentRangy(node.parentNode);
        } else {
            throw module.createError("getDocument: no document found for node");
        }
      }

      var range;
    function storeRange(r) {
        r = getSelection().getRangeAt(0);
        //var rootNode = this.getDocumentRangy(range.startContainer).documentElement;
        var rootNode = document.body;
        return [serializePosition(r.startContainer, r.startOffset, rootNode), serializePosition(r.endContainer, r.endOffset, rootNode)];
    }
    function restore() {
        getSelection().empty();
        getSelection().addRange(makeRange(range[0], range[1]))
    }



      function makeRange(r0, r1) {
        if(r0.length==0||r1.length==0) return null;
        //var doc = document.body;
        var rootNode = document.body;
        //var result = serialized.split(',');
        //todo checksum
        //console.log('1__'+result[0]);
        //console.log('2__'+result[1]);
        var start = deserializePosition(r0, rootNode), end = deserializePosition(r1, rootNode);
        var range = new Range();
        if(start&&end) {
          try{
            range.setStart(start[0], start[1]);
            range.setEnd(end[0], end[1]);
            return range;
          } catch(e){}
        }
       }


    //    getSelection().empty();
    //    getSelection().addRange(makeRange('0/1/4/2:5', '4/1/4/2:24'))

    range = storeRange()

    
    restorePos(range[0]);

    range