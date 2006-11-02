package net.lshift.java.util;

/**
 * Do some work.
 * @author david
 *
 * @param <P>
 * @param <R>
 * @param <E>
 */
public interface Operation<R>
{
    public R apply()
        throws Exception;
}
