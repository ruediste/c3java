
package net.lshift.java.util;

import java.lang.ref.Reference;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

public class Collections
{
    public static class Procedures
    {
        public static Transform<Object,Object> IDENTITY =
            new Transform<Object,Object>() {

            @Override
            public Object apply(Object x)
            {
                return x;
            }

        };

        @SuppressWarnings("unchecked")
        public static <V> Transform<V,V> identity()
        {
            return (Transform<V,V>)IDENTITY;
        }


        public static final Predicate<? extends Iterator<?>> HAS_NEXT =
            new Predicate<Iterator<?>>() {
            public Boolean apply(Iterator<?> i) {
                return i.hasNext();
            }
        };

        // I _could_ return something not a singleton here
        // to avoid a warning, and implement equals in a
        // sensible way, but I'd rather suppress warnings
        @SuppressWarnings("unchecked")
        public static <E> Predicate<Iterator<E>> hasNext()
        {
            return (Predicate<Iterator<E>>)HAS_NEXT;
        }

        public interface NextTransform<T>
        extends Transform<Iterator<T>,T> { }

        public static final NextTransform<Object> NEXT =
            new NextTransform<Object>() {
            public Object apply(Iterator<Object> i) {
                return i.next();
            }
        };

        // I _could_ return something not a singleton here
        // to avoid a warning, and implement equals in a
        // sensible way, but I'd rather suppress warnings
        @SuppressWarnings("unchecked")
        public static <T> NextTransform<T> next()
        {
            return (NextTransform<T>)NEXT;
        }

        public static <K,V> Transform<K,V> get(final Map<K,V> map)
        {
            return new Transform<K,V>() {
                public V apply(K k) {
                    return map.get(k);
                }
            };
        }

        public static <E> Transform<Iterable<E>,Iterator<E>> iterator()
        {
            return new Transform<Iterable<E>,Iterator<E>>() {
                public Iterator<E> apply(Iterable<E> c) {
                    return c.iterator();
                }
            };
        }

        public static final Transform<Collection<?>, Integer> SIZE =
            new Transform<Collection<?>,Integer>() {
            public Integer apply(Collection<?> c) {
                return c == null ? 0 : c.size();
            }
        };

        public static final Comparator<Collection<?>> SIZE_INCREASING =
            new Comparator<Collection<?>>() {

            @Override
            public int compare(Collection<?> a, Collection<?> b)
            {
                return a.size() - b.size();
            }

        };

        public static final Comparator<Collection<?>> SIZE_DECREASING =
            new Comparator<Collection<?>>() {

            @Override
            public int compare(Collection<?> a, Collection<?> b)
            {
                return b.size() - a.size();
            }

        };

        // I _could_ return something not a singleton here
        // to avoid a warning, and implement equals in a
        // sensible way, but I'd rather suppress warnings
        @SuppressWarnings("unchecked")
        public static <C extends Collection<?>> Transform<C,Integer> size()
        {
            return (Transform<C, Integer>)SIZE;
        }

        public static final Predicate<Collection<?>> IS_EMPTY =
            new Predicate<Collection<?>>() {
            public Boolean apply(Collection<?> x)
            {
                return x.isEmpty();
            }
        };

        // I _could_ return something not a singleton here
        // to avoid a warning, and implement equals in a
        // sensible way, but I'd rather suppress warnings
        @SuppressWarnings("unchecked")
        public static <C extends Collection<?>> Predicate<C> isEmpty()
        {
            return (Predicate<C>)IS_EMPTY;
        }

        public static <E> Predicate<E> contains(final Collection<E> c)
        {
            return new Predicate<E>() {

                public Boolean apply(E x)
                {
                    return c.contains(x);
                }

            };
        }

        public static <E> Predicate<E> contains(final Collection<E> ... cn)
        {
            return new Predicate<E>() {

                public Boolean apply(E x)
                {
                    for(Collection<E> c: cn)
                        if(c.contains(x))
                            return true;
                    return false;
                }
            };
        }

        public static Predicate<?> NOT_NULL = new Predicate<Object>() {
            @Override
            public Boolean apply(Object x) {
                return x != null;
            }
        };

        @SuppressWarnings("unchecked")
        public static <E> Predicate<E> notNull()
        {
            return (Predicate<E>) NOT_NULL;
        }

        public static <E> Transform<Reference<E>, E> dereference()
        {
            return new Transform<Reference<E>, E>() {
                @Override
                public E apply(Reference<E> x) {
                    return x.get();
                }
            };

        }

        public static Procedure<Iterator<?>> remove()
        {
            return new Procedure<Iterator<?>>() {
                public void apply(Iterator<?> iterator) {
                    iterator.remove();
                }
            };
        }
    }
}
