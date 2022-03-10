function debug(e){console.log('fatal web d::', e)};

if (typeof Element.prototype.remove != 'function') {  
    Element.prototype.remove = function() {
        this.parentElement.removeChild(this);
    }
    NodeList.prototype.remove = HTMLCollection.prototype.remove = function() {
        for(var i = this.length - 1; i >= 0; i--) {
            if(this[i] && this[i].parentElement) {
                this[i].parentElement.removeChild(this[i]);
            }
        }
    }    
} 

if (typeof Element.prototype.prepend != 'function') {  
    Element.prototype.prepend = function(e) {
        this.insertBefore(e, this.firstChild);
    } 
} 

if (typeof String.prototype.startsWith != 'function') {  
    String.prototype.startsWith = function (prefix){  
        return this.slice(0, prefix.length) === prefix;  
    };  
    window.kit=1;
} 
if (typeof String.prototype.endsWith != 'function') {  
    String.prototype.endsWith = function (suffix){  
        return this.slice(-suffix.length) === suffix;  
    };  
} 
if (typeof Object.assign != 'function') {
    Object.assign = function (target, varArgs) {
        'use strict';
        if (target == null) {
            throw new TypeError('Cannot convert undefined or null to object');
        }
        var to = Object(target);
        for (var index = 1; index < arguments.length; index++) {
            var nextSource = arguments[index];

            if (nextSource != null) {
                for (var nextKey in nextSource) {
                    if (Object.prototype.hasOwnProperty.call(nextSource, nextKey)) {
                        to[nextKey] = nextSource[nextKey];
                    }
                }
            }
        }
        return to;
    };
}