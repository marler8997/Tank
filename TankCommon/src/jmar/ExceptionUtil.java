package jmar;

public class ExceptionUtil {
	public static String getStackTraceString(Exception e) {
		StringBuilder builder = new StringBuilder();
		StackTraceElement [] stackTraceElements = e.getStackTrace();
		for(int i = 0; i < stackTraceElements.length; i++) {
			StackTraceElement stackTraceElement = stackTraceElements[i];
			
			String stackLocation = "";
			String fileName = stackTraceElement.getFileName();
			if(fileName != null) {
				stackLocation = String.format("(%s:%d)", fileName, stackTraceElement.getLineNumber());
			}
			
			builder.append(String.format("at %s%s\r\n", stackTraceElement.getClassName(), stackLocation));
		}
		return builder.toString();
	}
}
