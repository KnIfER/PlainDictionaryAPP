package com.fenwjian.sdcardutil;

import com.fenwjian.sdcardutil.BSTree;

    public class BSTNode<T extends Comparable<T>> {
        public T key;                // 关键字(键值)
        BSTNode<T> left;    // 左孩子
        BSTNode<T> right;    // 右孩子
        BSTNode<T> parent;    // 父结点

        public BSTNode(T key, BSTNode<T> parent, BSTNode<T> left, BSTNode<T> right) {
            this.key = key;
            this.parent = parent;
            this.left = left;
            this.right = right;
        }
 
        public T getKey() {
            return key;
        }

        public String toString() {
            return "key:"+key;
        }
    }