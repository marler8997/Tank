package jmar;

public class JmarMath {
	
	// This function is freakin genius!!
	// It runs extremely fast, I dare you to come up with a faster one:)
    public static int getNearestPowerOfTwo(int n) {
        if (n <= 1) return 1;
        n--;
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
        return n+1;
    }
	
    
    
    
    
    
	public static void main(String[] args) {
	
		
		// Test getNearestPowerOfTwo function
		
		long testStartTime = System.nanoTime();
		
		int test = 0;
		for(int nextPowerOf2 = 1; true; nextPowerOf2 <<= 1) {
			if(nextPowerOf2 < 0) break;
			System.out.println(String.format("Testing: 0x%08x - 0x%08x", test, nextPowerOf2));
			for(; test <= nextPowerOf2; test++) {				
				if(getNearestPowerOfTwo(test) != nextPowerOf2) 
					throw new AssertionError(String.format("Error: getNearestPowerOfTwo(%d) is %d but should be %d", test, getNearestPowerOfTwo(test), nextPowerOf2));
			}
		}
		
		long testEndTime = System.nanoTime();
		System.out.println(String.format("Test Time: %f seconds", ((float)(testEndTime - testStartTime)) / 1000000000f));
	}
}
