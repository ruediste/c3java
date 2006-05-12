/*
     This file is part of the LShift Java Library.

    The LShift Java Library is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    The LShift Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with The LShift Java Library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

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
