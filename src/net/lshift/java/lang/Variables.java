package net.lshift.java.lang;

public class Variables
{
    public static final Variable<Boolean> TRUE = new Variable<Boolean>() {
        public Boolean get() {
            return true;
        }
    };

    public static final Variable<Boolean> FALSE = new Variable<Boolean>() {
        public Boolean get() {
            return false;
        }
    };
}
