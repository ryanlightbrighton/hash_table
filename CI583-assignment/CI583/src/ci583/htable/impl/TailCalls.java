package ci583.htable.impl;

// from here: https://www.youtube.com/watch?v=4tEi86h8-TM&feature=youtu.be&t=32m30s
// more related here; https://stackoverflow.com/questions/43937160/designing-tail-recursion-using-java-8
// implementation: https://blog.knoldus.com/tail-recursion-in-java-8/

public class TailCalls {
    public static <T> TailCall<T> call(final TailCall<T> nextcall) {
        return nextcall;
    }

    public static <T> TailCall<T> done(final T value) {
        return new TailCall<T>() {
            @Override
            public boolean isComplete() {
                return true;
            }

            @Override
            public T result() {
                return value;
            }

            @Override
            public TailCall<T> apply() {
                throw new Error("not implemented!!!");
            }
        };
    }
    
    /*// test function
    
    public static TailCall<Integer> adder(int n) {
        if (n == 100) {
        	Thread.dumpStack(); // check Trace to make sure one call to function
            return TailCalls.done(n);
        }

        return  TailCalls.call (()-> adder(n+1)); // lambda function - note ++n would crash this
    }
    
    // test normal rec function
 
    
    static int adderNorm(int n) {
    	if (n == 100) {
    		Thread.dumpStack();
    		return n;
    	} else {
    		return adderNorm(++n);
    	}
    }
    
    // check
    
    public static void main(String[] args) {
    	System.out.println(adder(1).get());
    	
    	System.out.println(adderNorm(1));
    }*/
}
