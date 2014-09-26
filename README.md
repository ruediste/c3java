
Multiple Dynamic Dispatch for Java
==================================
Licensing
---------

see NOTICE.

To build from source
--------------------

This is a maven 2 project. See http://maven.apache.org/

Quick start
-----------

Outline:

 - Define an interface for the methods you want to dynamically
 dispatch.

 - Write a class which implements those methods for each type
 signature you want to support.

 - Get dynamic dispatch to create a dispatcher

For example:

    import net.lshift.java.dispatch.DynamicDispatch;

    // define an interface

    public interface NumberPredicate
    {
        public boolean evaluate(Number n);
    }

    // implement it for some argument types

    public class Exact
    {
        public boolean evaluate(Float f)
        {
            return false;
        }

        public boolean evaluate(Double f)
        {
            return false;
        }

        public boolean evaluate(Number n)
        {
            return true;
        }
    }

    // create a dynamic dispatcher

    NumberPredicate exact = (NumberPredicate)DynamicDispatch.proxy(NumberPredicate.class, new Exact());

C3 and Java
-----------

I use the C3 linearization to determine the most specific method.

see
http://www.webcom.com/haahr/dylan/linearization-oopsla96.html. JavaC3.java
is a translation of the dylan example at the end of this paper,
although its pretty hard to recognise, since it doesn't translate
readily into an imperative language.

To use the algorithm, everything must be a type - classes, interfaces,
and primitive types are all types. We need to generate a list of
super-types for all types in the system.  2.1.1 Supertypes for objects

In dylan, you list your superclasses, and its this order that the
linearization uses. In java, you list the interfaces you implement,
and my linearization uses this order, but should you put the super
class at the beginning, or the end of the list?

In java interface is assignable to Object. So for C3 to make sense,
all interfaces which have no super-types have Object as a super-type.

If Object is in the super-type list of a type, it must be the last
thing - otherwise linearization will always be impossible. So the most
obvious thing to do is to include the super-class last in the list of
super-types.

The disadvantage of this is pointed out by collections in java.util:

The various abstract collections implement the corresponding
interface, but the actual implementations don't directly implement the
corresponding interface. eg. AbstractSet implements Set, HashSet
extends AbstractSet, but does not implement Set directly. This is
going to be a common pattern.

If you put the super-class at the end of the list of super-types, this
results in an inconsistent linearization for all of java's built in
collections.

So I ended up doing the following by default:

For any super-class other than Object, the super-class goes first in
the list of super-types. If the super-class is Object, it gets pushed
to the end. This works in an intuitive way in lots of cases.

Supertypes for arrays and primitive types
-----------------------------------------

Arrays work exactly as assignability would suggest they do.

Primitive types work through java's notion of a widening conversion.
