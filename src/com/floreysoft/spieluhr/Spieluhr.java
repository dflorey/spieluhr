package com.floreysoft.spieluhr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class Spieluhr {
	private final static String[] notes = new String[]{ "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "B", "H" };
	
	public static void main(String[] args) {
		try {
			Sequence sequence = MidiSystem.getSequence(new File("C:/tmp/test.mid"));
			long tickLength = sequence.getTickLength();
			BufferedImage image = new BufferedImage((int) tickLength, 2500, BufferedImage.TYPE_BYTE_GRAY);
			Graphics2D g = (Graphics2D) image.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, (int) tickLength, 2500);
			Track[] tracks = sequence.getTracks();
			Track firstTrack = tracks[0];
			g.setColor(Color.BLACK);
			for ( int i = 0; i < firstTrack.size(); i++ ) {
				MidiEvent midiEvent = firstTrack.get(i);
				MidiMessage message = midiEvent.getMessage();
				if ( message instanceof ShortMessage ) {
					ShortMessage shortMessage = (ShortMessage) message;
					if ( shortMessage.getCommand() == ShortMessage.NOTE_ON ) {
						long tick = midiEvent.getTick();
						int key = shortMessage.getData1();
						System.out.println("Note:"+notes[key%12]+"("+key/12+") - "+tick);
						g.fill(new Ellipse2D.Double(tick, key*20, 15, 15));
					}
				}
			}
		    File outputfile = new File("C:/tmp/image.png");
		    ImageIO.write(image, "png", outputfile);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
