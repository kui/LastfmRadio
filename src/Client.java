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

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.MalformedURLException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

import java.security.MessageDigest;

public class Client {

    public static void main(String[] args) throws Exception{
	Client c = new Client(new File("account"));

	System.out.println("handshaking...");
	c.handshake();
	System.out.println("done");

	System.out.println("adjusting oldies tag radio...");
	c.adjustStation("globaltags", "oldies");
	System.out.println("done");

	System.out.println(c);

	System.out.println("getting tracks...");
	List<Track> tracks = c.getTracks();
	System.out.println("track nums: "+tracks.size());
	System.out.println(tracks);
	System.out.println("done");
    }

    /********************************************************************
                               public methods
     ********************************************************************/

    String userName, password, session, baseUrl, basePath, station;
    boolean handshakingFlag, adjustingFlag;
    public Client(File accountFile) 
	throws FileNotFoundException, IOException{
	HashMap<String,String> a = loadAccountFile(accountFile);
	this.userName = a.get("user");
	this.password = a.get("password");
	this.session = this.baseUrl = this.basePath = null;
	this.handshakingFlag = this.adjustingFlag = false;
    }
    public Client(String userName, String password){
	this.userName = userName;
	this.password = password;
	this.session = this.baseUrl = this.basePath = null;
	this.handshakingFlag = this.adjustingFlag = false;
    }

    static String HANDSHAKE_URL = 
	"http://ws.audioscrobbler.com/radio/handshake.php";
    public void handshake() throws IOException, HandShakeException {

	String paramString = 
	    createParams("version", "1.3.1",
			 "username", userName,
			 "passwordmd5", convertMd5(password));

	String urlString = 
	    String.format("%s?%s",HANDSHAKE_URL,paramString);
	// System.out.println(urlString);

	String body;
	try{
	    body = getHttpBody(urlString);
	}catch(URISyntaxException e){
	    body = null;
	    System.err.println(e.getMessage());System.exit(1);
	}

	// System.out.println(body);
	HashMap<String,String> params = parseParams(body);
	this.session = params.get("session");
	this.baseUrl = params.get("base_url");
	this.basePath = params.get("base_path");
	if(this.session == null || this.baseUrl == null ||
	   this.basePath == null){
	    throw(new HandShakeException(this.session,
					 this.baseUrl,
					 this.basePath,
					 params.get("msg")));
	}
	handshakingFlag = true;
    }

    public void adjustStation(String paramName, String param) 
	throws IOException, AdjustingStationException,
	       UnKnownParamNameException{

	String paramString = 
	    createParams("session", session,
			 "url",createLastfmUri(paramName, param));
	String urlString = 
	    String.format("http://%s%s/adjust.php?%s",
			  baseUrl, basePath, paramString);

	String body;
	try{
	    body = getHttpBody(urlString);
	}catch(URISyntaxException e){
	    body = null;
	    System.err.println(e.getMessage());System.exit(1);
	}

	HashMap<String,String> params = parseParams(body);
	String resp = params.get("response");
	if(resp == null){
	    String m = 
		"no \"response\" field in response body";
	    throw(new AdjustingStationException(m));
	}else if(resp.equals("FAILED")) {
	    String e = params.get("error");
	    if(e == null){
		String m = 
		    "no \"error\" field in response body";
		throw(new AdjustingStationException(m));
	    }else{
		throw(new AdjustingStationException(Integer.parseInt(e)));
	    }
	}

	this.station = params.get("stationname");
	this.adjustingFlag = true;
    }

    public List<Track> getTracks() throws IOException, XMLStreamException{
	return getTracks(true);
    }

    // url example: 
    // http://[base_url][base_path]/xspf.php?sk=[SESSIONID]&discovery=0&desktop=1.5.1
    public List<Track> getTracks(boolean discoveryFlag)
	throws IOException, XMLStreamException {

	String paramString = 
	    createParams("sk", session,
			 "discovery", discoveryFlag ? "1" : "0",
			 "desktop", "1.5.1");
	String urlString = 
	    String.format("http://%s%s/xspf.php?%s",
			  baseUrl, basePath, paramString);

	// System.out.println("get reader");
	Reader r;
	try{
	    r = getHttpStream(urlString);
	}catch(URISyntaxException e){
	    r = null;
	    System.err.println(e.getMessage());System.exit(1);
	}
	
	// System.out.println("parse xspf");
	return parseXSPF(r);
    }

    /********************************************************************
                               privete methods
     ********************************************************************/
    private static HashMap<String,String> loadAccountFile(File f)
	throws FileNotFoundException, IOException{

	StringBuilder body = new StringBuilder(1024);
	FileReader fr = new FileReader(f);
	BufferedReader r = new BufferedReader(fr);
	String line;

	while((line = r.readLine()) != null){
	    body.append(line).append("\r\n");
	}

	return parseParams(body.toString());
    }

    private static String createLastfmUri(String paramName, String param)
	throws UnKnownParamNameException{

	String uriFormat = "lastfm://%s/%s";
	String uri;

	if(paramName.equals("personal") || 
	   paramName.equals("neighbours")){

	    String latter = 
		String.format("%s/%s",param, paramName);
	    uri = String.format(uriFormat, "user", latter);

	}else if(paramName.equals("globaltags") ||
		 paramName.equals("artist") ||
		 paramName.equals("group") ){

	    uri = String.format(uriFormat, paramName, param);

	}else if(paramName.equals("recommended")){

	    String latter = 
		String.format("%s/%s/100", param, paramName);
	    uri = String.format(uriFormat, "user", latter);

	}else{
	    throw(new UnKnownParamNameException(paramName));
	}

	return uri;
    }
    
    private static String convertMd5(String p){
	MessageDigest md;
	try{
	    md = MessageDigest.getInstance("MD5");
	}catch(java.security.NoSuchAlgorithmException e){
	    md = null;
	}
	byte[] raw = md.digest(p.getBytes());
	
	StringBuilder md5 = new StringBuilder(32);
	for (int i=0,l=raw.length;i<l;i++) {
	    md5.append(Integer.toHexString(0xff & raw[i]));
	}
	return md5.toString();
    }

    private static String createParams(String ... args){

	if(args.length % 2 != 0){
	    String msg = 
		"invalid argument number. the num must be even.";
	    throw(new IllegalArgumentException(msg));
	}

	StringBuilder paramString = new StringBuilder(200);
	for(int i=0,l=args.length;i<l;i+=2){
	    if(i!=0){ paramString.append("&"); }
	    String key = args[i];
	    String value = args[i+1];
	    paramString.append(key).append("=").append(value);
	}

	return paramString.toString();
    }

    private String getHttpBody(String urlString) 
	throws URISyntaxException, IOException{

	BufferedReader r =
	    new BufferedReader(getHttpStream(urlString));
	String line;
	StringBuilder body = new StringBuilder(1024*1024);
	while((line = r.readLine()) != null){
	    body.append(line).append("\n");
	}
	r.close();

	return body.toString();
    }

    private Reader getHttpStream(String urlString) 
	throws URISyntaxException, IOException {

	URI uri = new URI(urlString);
	URLConnection c;
	try{
	    c = uri.toURL().openConnection();
	}catch(MalformedURLException e){
	    //c = null;
	    throw new URISyntaxException(urlString,
					 e.getMessage());
	}

	InputStreamReader isr;
	try{
	    isr = new InputStreamReader(c.getInputStream(),
					"UTF-8"); 
	}catch(UnsupportedEncodingException e){
	    isr = null;
	    System.err.println(e.getMessage());System.exit(1);
	}

	return isr;
    }

    private static HashMap<String,String> parseParams(String body){

	HashMap<String,String> params =
	    new HashMap<String,String>();

	String[] lines = body.split("\r?\n");
	for(int i=0,l=lines.length;i<l;i++){
	    String[] fields = lines[i].split("=");
	    if(fields.length == 2){
		params.put(fields[0], fields[1]);
	    }
	}

	return params;
    }

    @Override public String toString(){
	StringBuilder sb = new StringBuilder(1024);
	sb.append("[Client userName=").append(userName)
	    .append(", password=").append(password);
	if(handshakingFlag){
	    sb.append(", session=").append(session)
		.append(", baseUrl=").append(baseUrl)
		.append(", basePath=").append(basePath);
	}
	if(adjustingFlag){
	    sb.append(", station=").append(station);
	}
	sb.append("]");
	return sb.toString();
    }

    private List<Track> parseXSPF(Reader r) throws XMLStreamException{
	boolean inTrack = false;
	ArrayList<Track> tracks = new ArrayList<Track>();
	Track t = null;
	XMLInputFactory factory = XMLInputFactory.newInstance();
	XMLStreamReader reader = factory.createXMLStreamReader(r);

	while(reader.hasNext()) {
	    String tagName;
	    switch(reader.getEventType()) {
	    case XMLStreamConstants.START_ELEMENT:
		tagName = reader.getName().getLocalPart();
		if(inTrack && Track.FIELD_NAMES.contains(tagName)) {
		    t.put(tagName, reader.getElementText());
		}else if(!inTrack && tagName.equals("track")) {
		    inTrack = true;
		    t = new Track();
		}
		break;
	    case XMLStreamConstants.END_ELEMENT:
		tagName = reader.getName().getLocalPart();
		if(inTrack && tagName.equals("track")) {
		    inTrack = false;
		    tracks.add(t);
		}
		break;
	    default:
	    }
	    reader.next();
	}
	// System.out.println(tracks);
	return tracks;
    }

}