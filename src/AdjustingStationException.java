// -*- coding:utf-8 -*-

package kui.lastfm.radio;

class AdjustingStationException extends Exception{
    static String[] MESSAGES = new String[]{
	"There is not enough content to play the station. "+
	"Due to restrictions imposed by the music labels, "+
	"a radio station must have more than 15 tracks; "+
	"each by different artists.",
	"The group does not have enough members to have a radio station.",
	"The artist does not have enough fans to have a radio station.",
	"The station is not available for streaming.",
	"The station is available to subscribers only.",
	"The user does not have enough neighbors to have a radio station.",
	"An unknown error occurred."
    };
    AdjustingStationException(String m){
	super(m);
    }
    AdjustingStationException(int i){
	this(MESSAGES[i-1]);
    }
}
