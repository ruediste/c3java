
package net.lshift.java.dispatch;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.collect.Iterables.all;
import static com.google.common.collect.Iterables.any;
import static java.util.Collections.singletonList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Implement C3 Linearization
 * http://www.webcom.com/haahr/dylan/linearization-oopsla96.html
 * @author david
 */
public class JavaC3
{
    /*
     * This is a translation of the dylan example at the end of the above paper.
     * Its the motivation for the generic list processing library in
     * net.lshift.util.
     */

    public interface DirectSuperclasses
    {
        /**
         * Get the super classes.
         * @param type the type to get the superclasses of.
         */
        public List<Class<?>> directSuperclasses
            (Class<?> type);
    }

    private static class LinearizationKey
    {
        public DirectSuperclasses directSuperclasses;
        public Class<?> type;

        public  LinearizationKey
            (DirectSuperclasses directSuperclasses,
             Class<?> type)
        {
            this.directSuperclasses = directSuperclasses;
            this.type = type;
        }

        public boolean equals(Object o)
        {
            if(o == null) {
                // you wouldn't think this could happen, but there
                // is no way to stop garbage collection from happening
                // on linearizations during a get(), or put(), so we
                // must check this is not null.
                return false;
            }
            else {
                LinearizationKey other = (LinearizationKey)o;
                return type.equals(other.type) &&
                    directSuperclasses.equals(other.directSuperclasses);
            }
        }

        public int hashCode()
        {
            return type.hashCode();
        }
    }

    private static Map<LinearizationKey, Iterable<Class<?>>> linearizations =
        java.util.Collections.synchronizedMap
            (new WeakHashMap<LinearizationKey, Iterable<Class<?>>>());

    /**
     * Thrown when its not possible to linearize
     * all superclasses.
     */
    public static class JavaC3Exception
        extends Error
    {
        private static final long serialVersionUID = 1L;
        private Iterable<Class<?>> partialResult;
        private Iterable<List<Class<?>>> remainingInputs;

        protected JavaC3Exception
            (Iterable<Class<?>> reversedPartialResult,
             Iterable<List<Class<?>>> remainingInputs)
        {
            super("inconsistent precedence");
            this.partialResult = reversedPartialResult;
            this.remainingInputs = remainingInputs;
        }


        /**
         * Gets the value of reversedPartialResult
         * This is really for expert use only. Its
         * the value of reversedPartialResult at the
         * point the linearization failed.
         * @return the value of reversedPartialResult
         */
        public Iterable<Class<?>> getReversedPartialResult()  {
            return this.partialResult;
        }

        /**
         * Gets the value of remainingInputs
         * This is really for expert use only. Its
         * the value of remaining inputs at the point
         * the linearization failed.
         * @return the value of remainingInputs
         */
        public Iterable<List<Class<?>>> getRemainingInputs()  {
            return this.remainingInputs;
        }

        public String toString()
        {
            return "inconsistent precendence: " +
                partialResult +
                " " + remainingInputs;
        }
    }




    protected static Iterable<Class<?>> mergeLists(
         Iterable<Class<?>> partialResult,
         final Iterable<List<Class<?>>> remainingInputs,
         final DirectSuperclasses dsc)
        throws JavaC3Exception
    {
        if(all(remainingInputs, equalTo(Collections.<Class<?>>emptyList()))) {
            return partialResult;
        }

        Optional<Class<?>> next = Optional.absent();
        for(Class<?> c: partialResult) {
            next = Iterables.tryFind(dsc.directSuperclasses(c), isCandidate(remainingInputs));
            if(next.isPresent()) break;
        }

        if(next.isPresent()) {
            List<List<Class<?>>> newRemainingInputs = Lists.newArrayList();
            for(List<Class<?>> input: remainingInputs) {
                newRemainingInputs.add(input.indexOf(next.get()) == 0 ? input.subList(1, input.size()) : input);
            }

            return mergeLists(
                Iterables.concat(singletonList(next.get()), partialResult),
                newRemainingInputs,
                dsc);
        }
        else {
            throw new JavaC3Exception(partialResult, remainingInputs);
        }
    }

    /**
     * To be a candidate for the next place in the linearization, you must
     * be the head of at least one list, and in the tail of none of the lists.
     * @param remainingInputs the lists we are looking for position in.
     * @return true if the class is a candidate for next.
     */
    private static <X> Predicate<X> isCandidate(final Iterable<List<X>> remainingInputs) {
        return new Predicate<X>() {

            Predicate<List<X>> isHead(final X c) {
                return new Predicate<List<X>>() {
                    public boolean apply(List<X> input) {
                        return !input.isEmpty() && c.equals(input.get(0));
                    }
                };
            }

            Predicate<List<X>> tailContains(final X c) {
                return new Predicate<List<X>>() {
                    public boolean apply(List<X> input) {
                        return input.indexOf(c) > 0;
                    }
                };
             }

             public boolean apply(final X c) {
                 return any(remainingInputs, isHead(c)) &&
                       !any(remainingInputs, tailContains(c));
             }
        };
    }

    protected static Iterable<Class<?>> computeClassLinearization
        (Class<?> c,
         final DirectSuperclasses dsc)
        throws JavaC3Exception
    {
        List<Class<?>> cDirectSuperclasses = dsc.directSuperclasses(c);

        Function<Class<?>,List<Class<?>>> cplList =
            new Function<Class<?>,List<Class<?>>>() {
            public List<Class<?>> apply(Class<?> c) {
                return Lists.newArrayList(allSuperclasses(c, dsc));
            }
        };

        return mergeLists(
            Collections.<Class<?>>singletonList(c),
            Iterables.concat(
                 Lists.transform(cDirectSuperclasses, cplList),
                 singletonList(cDirectSuperclasses)),
             dsc);
    }

    public static Iterable<Class<?>> allSuperclasses(Class<?> c)
        throws JavaC3Exception
    {
        return allSuperclasses(c, DefaultDirectSuperclasses.SUPERCLASSES);
    }

    public static Iterable<Class<?>> allSuperclasses(Class<?> c, DirectSuperclasses dsc)
        throws JavaC3Exception
    {
        LinearizationKey key = new LinearizationKey(dsc, c);
        Iterable<Class<?>> linearization = linearizations.get(key);
        if(linearization == null) {
            linearization = computeClassLinearization(c, dsc);
            linearizations.put(key, linearization);
        }

        return linearization;
    }
}
