package net.lshift.java.util;

/**
 * Self adjusting random exponential backoff.
 * This assumes that a suitable backoff time is a function of load on some
 * resource (like processor time, or disk access, for example).
 * @author david
 *
 */
public class RandomExponentialBackoff
{
    private static final double DEFAULT_BACKOFF_FACTOR = Math.sqrt(2.0);
    private static final double DEFAULT_SUCCESS_TIME = 100.0; // 1/10th of a second
    private static final double DEFAULT_DECAY_TIME = 10000.0; // 10 seconds
    
    private final double decayTime;
    private final double backoffFactor;
    private double averageSuccessTime;
    private long lastUpdateTime;
    
    /**
     * Create a new random exponential backoff context.
     * @param decayTime
     * @param backoffFactor
     * @param seedSuccessTime
     */
    public RandomExponentialBackoff
        (double decayTime, 
         double backoffFactor,
         double seedSuccessTime)
    {
        this.decayTime = decayTime;
        this.backoffFactor = backoffFactor;
        this.averageSuccessTime = seedSuccessTime;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    public RandomExponentialBackoff()
    {
        this(DEFAULT_DECAY_TIME, DEFAULT_BACKOFF_FACTOR, DEFAULT_SUCCESS_TIME);
    }
    
    public class Session
    {
        public final long sessionStartTime;
        public long attemptStartTime;
        public double nextWaitFor;
        
        private Session()
        {
            sessionStartTime = System.currentTimeMillis();
            attemptStartTime = sessionStartTime;
            // this assumes a uniform distribution
            nextWaitFor = averageSuccessTime*2;
        }
        
        /**
         * Call after each failed attempt. This makes the current
         * thread sleep for the backoff time.
         * @throws InterruptedException
         */
        public void backoff()
            throws InterruptedException
        {
            Thread.sleep((long)(nextWaitFor*Math.random()));
            attemptStartTime = System.currentTimeMillis();
            nextWaitFor = nextWaitFor*backoffFactor;
        }

        /**
         * Call after the first successful attempt.
         * This updates the initial value for the backoff time for
         * future sessions. Don't call this if the operation doesn't
         * succeed: If the problem is a bug, it will skew your backoff time.
         */
        public void complete()
        {
            // if the first attempt succeeded, I'm not interested in the result
            if(attemptStartTime > sessionStartTime) {
                // the current time is used to calculate the age of this
                // result, 
                long currentTime = System.currentTimeMillis();
                long duration = attemptStartTime - sessionStartTime;

                synchronized(RandomExponentialBackoff.this) {
                    double p = Math.exp((currentTime - lastUpdateTime)/decayTime);
                    averageSuccessTime = p*duration - (1 - p)*averageSuccessTime;
                    lastUpdateTime = attemptStartTime;
                }
            }
        }
    }
    
    /**
     * Get a new session
     * Begin your first attempt immediately after calling this.
     * @return
     */
    public Session newSession()
    {
        return new Session();
    }

    public double getAverageSuccessTime()
    {
        return averageSuccessTime;
    }

    public double getBackoffFactor()
    {
        return backoffFactor;
    }

    public double getDecayTime()
    {
        return decayTime;
    }

    public long getLastUpdateTime()
    {
        return lastUpdateTime;
    }
}
