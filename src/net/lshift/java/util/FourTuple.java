package net.lshift.java.util;

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
    
    public static <T,U,V,W> FourTuple<T,U,V,W> tuple(T a, U b, V c, W d)
    {
        return new FourTuple<T,U,V,W>(a,b,c, d);
    }
}
