
package net.lshift.java.util;

import java.util.Collection;

/**
 * Bags are collections which are explicitely not ordered.
 * They are just a matter of equality semantics
 * @see Bags
 * @see EqualsHelper
 */
public interface Bag
    extends Collection
{
    /**
     * Equality
     * two bags are equal if the same items appear the same
     * number of times in each collection.
     */
    public boolean equals(Object o);
}
