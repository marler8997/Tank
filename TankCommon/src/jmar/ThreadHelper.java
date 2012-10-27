package jmar;

public class ThreadHelper {
	public static void printAllThreads() {
		ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
		ThreadGroup parentGroup;
		while ( ( parentGroup = rootGroup.getParent() ) != null ) {
		    rootGroup = parentGroup;
		}

		System.out.println(String.format("ActiveThreads: %d", rootGroup.activeCount()));
		Thread[] threads = new Thread[ rootGroup.activeCount() ];
		while ( rootGroup.enumerate( threads, true ) == threads.length ) {
		    threads = new Thread[ threads.length * 2 ];
		}
		
	}
}
