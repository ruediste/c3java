package net.lshift.java.util;

import java.util.Iterator;

public class FourTuple<T, U, V, W>
    extends ThreeTuple<T, U, V>
{
    private static final long serialVersionUID = 1L;
    public final W fourth;

    public FourTuple(T first, U second, V third, W fourth)
    {
        super(first, second, third);
        this.fourth = fourth;
    }

    @SuppressWarnings("unchecked") FourTuple(Iterator<Object> i)
    {
        super((T)i.next(), (U)i.next(), (V)i.next());
        this.fourth = (W)i.next();
    }

    @Override
    public Object get(int index)
    {
        switch(index) {
        case 0: return first;
        case 1: return second;
        case 2: return third;
        case 3: return fourth;
        default: throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int size()
    {
        return 4;
    }

    public W getFourth()
    {
        return fourth;
    }
}
