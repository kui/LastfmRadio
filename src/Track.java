// -*- coding:utf-8 -*-

/* The MIT License
   
   Copyright (c) 2011 Keiichiro Ui

   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:
   
   The above copyright notice and this permission notice shall be included in
   all copies or substantial portions of the Software.
   
   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
   THE SOFTWARE.
*/

package kui.lastfm.radio;

import java.util.HashSet;
import java.util.HashMap;

public class Track{

    static public HashSet<String> FIELD_NAMES = new HashSet<String>();
    static {
	FIELD_NAMES.add("creator"); FIELD_NAMES.add("title");
	FIELD_NAMES.add("album"); FIELD_NAMES.add("location");
	FIELD_NAMES.add("duration"); FIELD_NAMES.add("image");
    }

    HashMap<String, String> data;

    public Track() {
	data = new HashMap<String, String>();
    }

    public Track(String artist, String title, String album,
		 String location, String duration, String imgUrl){
	this();
	this.data.put("creator", artist); this.data.put("title", title);
	this.data.put("album", album); this.data.put("location", location);
	this.data.put("duration", duration); this.data.put("image", imgUrl);
    }

    public String put(String key, String val){
	if(!FIELD_NAMES.contains(key)) {
	    return null;
	}
	return data.put(key, val);
    }

    public String get(String key){
	return data.get(key);
    }

    @Override public String toString(){
	return data.toString();
    }

}
