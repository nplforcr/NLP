package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Timing {
	public static String getCurrentTimeStamp() {
	      SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
	      Date now = new Date();
	      String strDate = sdfDate.format(now);
	      return strDate;
	}

}
