// -*- coding:utf-8 -*-

package kui.lastfm.radio;

public class HandShakeException extends Exception{

    HandShakeException(String session, String baseUrl,
		       String basePath, String msg){
	super(createMessage(session,baseUrl,basePath,msg));
    }

    static String createMessage(String session, String baseUrl,
				String basePath, String msg){
	if(session.equals("FAILED")){
	    return String.format("invalid user or password (msg=\"%s\")",
				 msg);
	}else{
	    return String.format("some handshake error (session=%s,"+
				 " base_url=%s, base_path=%s)",
				 session, baseUrl, basePath);
	}
    }
}

