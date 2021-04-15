package com.useriq.sdk;

import android.support.annotation.NonNull;
import android.view.View;

import java.util.Iterator;

/**
 * @author sudhakar
 * @created 22-Nov-2018
 */
public class ViewTree {

    private final ViewNode root;

    ViewTree(View view) {
        this.root = new ViewNode(view);
    }

    /**
     * preOrderDFSIterator creates PreOrder Depth First iterator
     *
     * @return Iterator
     */
    public Iterable<ViewNode> preOrderDFSIterator() {
        return new Iterable<ViewNode>() {
            @NonNull
            public Iterator<ViewNode> iterator() {
                return new PreOrderDFSIterator();
            }
        };
    }

    private class PreOrderDFSIterator implements Iterator<ViewNode> {
        private ViewNode node = ViewTree.this.root;

        public boolean hasNext() {
            return this.node != null;
        }

        public ViewNode next() {
            ViewNode curr = this.node;
            ViewNode next = curr.getFirstChild();

            if (next == null) {
                next = curr.getRightSibling();

                if (next == null) {
                    ViewNode parent = curr.getParent();

                    while (parent != null && next == null) {
                        next = parent.getRightSibling();
                        if (next == null) {
                            parent = parent.getParent();
                        }
                    }
                }
            }

            this.node = next;

            return curr;
        }
    }
}
