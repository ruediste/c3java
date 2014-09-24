package net.lshift.java.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Futures
{
    public static class FutureInterruptedException
    extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        public FutureInterruptedException(InterruptedException e)
        {
            super(e);
        }
    }

    public static class FutureExecutionException
    extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        public FutureExecutionException(ExecutionException e)
        {
            super(e);
        }
    }

    public static <T> Transform<Future<T>,T> get()
    {
        return new Transform<Future<T>,T>() {
            public T apply(Future<T> x)
            {
                try {
                    return x.get();
                } catch (InterruptedException e) {
                    throw new FutureInterruptedException(e);
                } catch (ExecutionException e) {
                    throw new FutureExecutionException(e);
                }
            }
        };
    }
}
