// -*- coding:utf-8 -*-

package kui.lastfm.radio;

class UnKnownParamNameException extends Exception{
    UnKnownParamNameException(String paramName){
	super(String.format("\"%s\" is an unknown parameter name.",
			    paramName));
    }
}
