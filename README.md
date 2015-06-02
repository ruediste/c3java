[![Build Status](https://travis-ci.org/ruediste/c3java.svg?branch=master)](https://travis-ci.org/ruediste/c3java)

C3 Linearization Implementation for Java
==================================
This library implements the [C3 Linearization algorithm](http://en.wikipedia.org/wiki/C3_linearization). It is used to flatten the inheritance graph of a class in the presence of multiple inheritance (interfaces in java). This can be used to determine which type introduced a member (method, property, etc) first in an inheritance graph. 

The superclass and the interfaces of a class are retrieved using reflection. The exact way of ordering the super classes and interfaces can be customized. 

The code has been forked from [lshift](https://bitbucket.org/lshift/java-multimethods/), stripped of the dynamic dispatch code, refactored and cleaned up.

To get the linearized class hierarchy of a type simply use
    
    Iterable<Class<?>> linearization = JavaC3.allSuperclasses(<your type>.class);

C3 and Java
-----------

JavaC3.java is a translation of the dylan example at the end of [this paper](http://www.webcom.com/haahr/dylan/linearization-oopsla96.html),
although its pretty hard to recognise, since it doesn't translate
readily into an imperative language.

To use the algorithm, everything must be a type - classes, interfaces,
and primitive types are all types. We need to generate a list of
super-types for all types in the system. 

### Supertypes for objects

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

### Supertypes for arrays and primitive types
Arrays work exactly as assignability would suggest they do.

Primitive types work through java's notion of a widening conversion.

To build from source
--------------------

This is a maven project. See http://maven.apache.org/


Licensing
---------

see NOTICE.

