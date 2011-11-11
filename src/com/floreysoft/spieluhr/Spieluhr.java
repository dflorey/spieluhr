package com.floreysoft.spieluhr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
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
	private final static String[] notes = new String[] { "C", "D", "E", "F",
			"G", "A", "B" };
	private static final float TEMPO = 0.8F; // TEMPO

	private static final int PADDING_TOP = 250, PADDING_LEFT = 500,
			PADDING_RIGHT = 500, TEXT_PADDING_TOP = 285;

	private static final int STAVE_HEIGHT = 2500, STAVE_WIDTH = 8000,
			KEY_DISTANCE = 105, HOLE_WIDTH = 50, HOLE_HEIGHT = 50;

	private static final int[] keyMap = new int[] { 81, 79, 77, 76, 74, 72, 71,
			69, 67, 65, 64, 62, 60, 59, 57, 55, 53, 52, 50, 48 };

	public static void main(String[] args) {
		try {
			Sequence sequence = MidiSystem.getSequence(new File(
					"C:/tmp/test.mid"));
			long tickLength = sequence.getTickLength();

			int totalWidth = (int) (tickLength * TEMPO);
			int staves = (totalWidth / STAVE_WIDTH) + 1;
			BufferedImage image = new BufferedImage(STAVE_WIDTH, STAVE_HEIGHT
					* staves, BufferedImage.TYPE_BYTE_GRAY);
			Graphics2D g = (Graphics2D) image.getGraphics();
			// Draw background
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, STAVE_WIDTH, STAVE_HEIGHT * staves);
			g.setColor(Color.BLACK);
			for (int i = 0; i < staves; i++) {
				// Draw cut marker
				int offset = i * STAVE_HEIGHT;
				g.setStroke(new BasicStroke(10));
				g.drawLine(0, offset, STAVE_WIDTH, offset);

				// Draw keys and stave
				FontRenderContext frc = g.getFontRenderContext();
				Font f = new Font("Helvetica", Font.PLAIN, 96);
				float[] dashPattern = { 150, 10 };
				g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
						BasicStroke.JOIN_MITER, 10, dashPattern, 0));
				for (int j = 0; j < keyMap.length; j++) {
					g.drawLine(PADDING_LEFT / 2, offset + j * KEY_DISTANCE
							+ PADDING_TOP, STAVE_WIDTH - PADDING_RIGHT / 2,
							offset + j * KEY_DISTANCE + PADDING_TOP);
					TextLayout tl = new TextLayout(notes[j % notes.length], f,
							frc);
					tl.draw(g, KEY_DISTANCE, offset + (keyMap.length - 1 - j)
							* KEY_DISTANCE + TEXT_PADDING_TOP);
				}
			}
			g.setStroke(new BasicStroke(10));
			g.drawLine(0, staves * STAVE_HEIGHT, STAVE_WIDTH, staves
					* STAVE_HEIGHT);
			// Draw track
			float staveWidth = STAVE_WIDTH - PADDING_LEFT - PADDING_RIGHT;
			Track[] tracks = sequence.getTracks();
			System.out.print("Tracks: " + tracks.length);
			Track firstTrack = tracks[1];
			for (int i = 0; i < firstTrack.size(); i++) {
				MidiEvent midiEvent = firstTrack.get(i);
				MidiMessage message = midiEvent.getMessage();
				if (message instanceof ShortMessage) {
					ShortMessage shortMessage = (ShortMessage) message;
					if (shortMessage.getCommand() == ShortMessage.NOTE_ON) {
						int key = getKey(shortMessage.getData1());
						if (key >= 0) {
							long tick = midiEvent.getTick();
							System.out.println("Note:"
									+ notes[key % notes.length] + "(" + key / 8
									+ ") - " + tick);
							int stave = (int) (tick * TEMPO / staveWidth);
							g.fill(new Ellipse2D.Double(tick * TEMPO
									% staveWidth + PADDING_LEFT, stave
									* STAVE_HEIGHT + key * KEY_DISTANCE
									+ PADDING_TOP - HOLE_HEIGHT / 2,
									HOLE_WIDTH, HOLE_HEIGHT));
						}
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

	private static int getKey(int key) {
		for (int i = 0; i < keyMap.length; i++) {
			if (keyMap[i] == key) {
				return i;
			}
		}
		return -1;
	}
}
