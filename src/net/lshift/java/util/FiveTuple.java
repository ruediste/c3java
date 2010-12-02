package net.lshift.java.util;

public class FiveTuple<T, U, V, W, X>
    extends FourTuple<T, U, V, W>
{
    private static final long serialVersionUID = 1L;
    public final X fifth;

    public FiveTuple(T first, U second, V third, W fourth, X fifth)
    {
        super(first, second, third, fourth);
        this.fifth = fifth;
    }
    
    @Override
    public Object get(int index)
    {
        switch(index) {
        case 0: return first;
        case 1: return second;
        case 2: return third;
        case 3: return fourth;
        case 4: return fifth;
        default: throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int size()
    {
        return 5;
    }
    
    public static <T,U,V,W,X> FiveTuple<T,U,V,W,X> tuple(T a, U b, V c, W d, X e)
    {
        return new FiveTuple<T,U,V,W,X>(a,b,c, d, e);
    }
}
