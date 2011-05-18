// -*- coding:utf-8 -*-
// main class

package kui.lastfm.radio;

import java.util.List;

import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.MalformedURLException;

import javax.xml.stream.XMLStreamException;

public class Player {

    public static void main(String[] args) throws Exception{
	String profileFileName = "account";
	Player p = new Player(profileFileName);
	p.init("globaltags", "oldies");
	p.play();
    }

    // 

    private Client client;

    public Player(String fileName) throws IOException, FileNotFoundException{
	client = new Client(new File(fileName));
    }

    public void init(String paramName, String param)
	throws IOException, HandShakeException,
	       AdjustingStationException, UnKnownParamNameException {

	System.out.println("shaking hand ...");
	client.handshake();

	System.out.println("adjusting station ...");
	client.adjustStation(paramName, param);
    }
    
    public void play()
	throws IOException, MalformedURLException, URISyntaxException, 
	       XMLStreamException{

	boolean loopFlag = true;
	while(loopFlag){
	    System.out.println("getting tracks ...");
	    List<Track> tracks = client.getTracks();
	    for(Track t : tracks) {
		printTrack(t);
		playTrack(t.get("location"));
	    }
	    loopFlag = false;
	}

    }

    public void printTrack(Track t){
	System.out.printf("%s\t- %s\t(%s)\n",
			  t.get("title"),
			  t.get("album"),
			  t.get("creator"));
	// System.out.println(t);
    }

    private final int BUFFER_SIZE = 1024*16;
    public void playTrack(String mp3Location)
	throws MalformedURLException, URISyntaxException, IOException, 
	       XMLStreamException{

	URI uri = new URI(mp3Location);
	URLConnection c = uri.toURL().openConnection();
	BufferedInputStream bis =
	    new BufferedInputStream(c.getInputStream());
	byte[] buffer = new byte[BUFFER_SIZE];
	BufferedOutputStream o = 
	    new BufferedOutputStream(new FileOutputStream("hoge.mp3"));
	long offset = 0L;
	int size;
	while((size = bis.read(buffer)) >= 0){
	    o.write(buffer);
	    offset += size;
	    System.out.print(offset);
	    System.out.print("\r");
	    System.out.flush();
	}
	bis.close();
	o.close();
    }
}