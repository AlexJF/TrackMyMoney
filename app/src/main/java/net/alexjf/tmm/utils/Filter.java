/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class implements a generic filter over different types of collections.
 */
public class Filter<T> {
    private Condition<T> condition;

    public Filter(Condition<T> condition) {
        this.condition = condition;
    }

    public List<T> apply(List<T> initialList) {
        List<T> newList = new LinkedList<T>(initialList);

        applyInPlace(newList);

        return newList;
    }

    public void applyInPlace(List<T> list) {
        for (Iterator<T> i = list.iterator(); i.hasNext(); ) {
            T item = i.next();

            if (!condition.applies(item)) {
                i.remove();
            }
        }
    }

    public interface Condition<T> {
        /**
         * Apply a boolean operation to an item being analyzed by a Filter.
         *
         * @param item The item against which we'll check the condition.
         *
         * @return True if the condition applies and the item should remain on
         * the final collection or False otherwise.
         */
        public boolean applies(T item);
    }
}
