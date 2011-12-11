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
	private final static String NAME = "Spieluhr4";
	private final static String[] notes = new String[] { "C", "D", "E", "F",
			"G", "A", "B" };
	private static final float TEMPO = 0.892F; // TEMPO

	private static final int PADDING_TOP = 220, PADDING_LEFT = 500,
			PADDING_RIGHT = 500, TEXT_PADDING_TOP = 250;

	private static final int STAVE_HEIGHT = 2500, STAVE_WIDTH = 10000,
			KEY_DISTANCE = 108, HOLE_WIDTH = 50, HOLE_HEIGHT = 50,
			STAVES_PER_PAGE = 3, CUT_OFFSET = 100;

	private static final int[] keyMap = new int[] { 81, 79, 77, 76, 74, 72, 71,
			69, 67, 65, 64, 62, 60, 59, 57, 55, 53, 52, 50, 48 };

	public static void main(String[] args) {
		try {
			Sequence sequence = MidiSystem.getSequence(new File("C:/tmp/"
					+ NAME + ".mid"));
			Track[] tracks = sequence.getTracks();
			System.out.println("Tracks: " + tracks.length);
			Track firstTrack = tracks[1];
			long tickLength = sequence.getTickLength();
			int totalWidth = (int) (tickLength * TEMPO);
			int numberOfStaves = (int) Math.ceil(totalWidth
					/ (float) (STAVE_WIDTH - PADDING_LEFT - PADDING_RIGHT));
			int numberOfPages = (int) Math.ceil(numberOfStaves
					/ (float) STAVES_PER_PAGE);
			BufferedImage pages[] = new BufferedImage[numberOfPages];
			for (int page = 0; page < numberOfPages; page++) {
				int staves = STAVES_PER_PAGE;
				if (page == numberOfPages - 1) {
					staves = numberOfStaves % STAVES_PER_PAGE;
					if (staves == 0) {
						staves = STAVES_PER_PAGE;
					}
				}
				pages[page] = new BufferedImage(STAVE_WIDTH, STAVE_HEIGHT
						* staves, BufferedImage.TYPE_BYTE_GRAY);
				Graphics2D g = (Graphics2D) pages[page].getGraphics();
				// Draw background
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, STAVE_WIDTH, STAVE_HEIGHT * staves);
				g.setColor(Color.BLACK);
				for (int stave = 0; stave < staves; stave++) {
					// Draw cut marker
					int offset = stave * STAVE_HEIGHT;
					g.setStroke(new BasicStroke(10));
					g.drawLine(0, offset, STAVE_WIDTH, offset);

					// Draw stave
					FontRenderContext frc = g.getFontRenderContext();
					Font f = new Font("Helvetica", Font.PLAIN, 96);
					if (stave > 0 || page > 0) {
						TextLayout tl = new TextLayout(String.valueOf(stave+page*STAVES_PER_PAGE),
								f, frc);
						tl.draw(g, KEY_DISTANCE, offset
								+ (keyMap.length / 2) * KEY_DISTANCE
								+ TEXT_PADDING_TOP);
						g.setStroke(new BasicStroke(5));
						g.draw(new Ellipse2D.Double(KEY_DISTANCE-50, offset
								+ (keyMap.length / 2) * KEY_DISTANCE
								+ TEXT_PADDING_TOP - 110,
								150, 150));
					}
					float[] dashPattern = { 150, 10 };
					g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
							BasicStroke.JOIN_MITER, 10, dashPattern, 0));
					for (int key = 0; key < keyMap.length; key++) {
						g.drawLine(PADDING_LEFT / 2, offset + key
								* KEY_DISTANCE + PADDING_TOP, STAVE_WIDTH
								- PADDING_RIGHT / 2, offset + key
								* KEY_DISTANCE + PADDING_TOP);
						if (stave == 0 && page == 0) {
							// Draw keys
							TextLayout tl = new TextLayout(notes[key
									% notes.length], f, frc);
							tl.draw(g, KEY_DISTANCE, offset
									+ (keyMap.length - 1 - key) * KEY_DISTANCE
									+ TEXT_PADDING_TOP);
						} else {
							// Draw cut marker
							g.drawLine(PADDING_LEFT + CUT_OFFSET, offset,
									PADDING_LEFT + CUT_OFFSET, offset
											+ STAVE_HEIGHT);
						}
						g.drawLine(STAVE_WIDTH - PADDING_RIGHT + CUT_OFFSET,
								offset,
								STAVE_WIDTH - PADDING_RIGHT + CUT_OFFSET, offset
										+ STAVE_HEIGHT);
					}
				}
				g.setStroke(new BasicStroke(10));
				g.drawLine(0, numberOfStaves * STAVE_HEIGHT, STAVE_WIDTH,
						numberOfStaves * STAVE_HEIGHT);
			}
			// Draw track
			float staveWidth = STAVE_WIDTH - PADDING_LEFT - PADDING_RIGHT;
			for (int i = 0; i < firstTrack.size(); i++) {
				MidiEvent midiEvent = firstTrack.get(i);
				MidiMessage message = midiEvent.getMessage();
				if (message instanceof ShortMessage) {
					ShortMessage shortMessage = (ShortMessage) message;
					if (shortMessage.getCommand() == ShortMessage.NOTE_ON) {
						int key = getKey(shortMessage.getData1());
						if (key >= 0) {
							long tick = midiEvent.getTick();
							int stave = (int) (tick * TEMPO / staveWidth);
							int page = stave / STAVES_PER_PAGE;
							stave -= page * STAVES_PER_PAGE;
							System.out.println("Printin note="
									+ notes[key % notes.length] + "(" + key / 8
									+ ") - on page=" + page + ", stave="
									+ stave + ", tick=" + tick);
							Graphics2D g = (Graphics2D) pages[page]
									.getGraphics();
							g.setColor(Color.BLACK);
							g.fill(new Ellipse2D.Double(tick * TEMPO
									% staveWidth + PADDING_LEFT, stave
									* STAVE_HEIGHT + key * KEY_DISTANCE
									+ PADDING_TOP - HOLE_HEIGHT / 2,
									HOLE_WIDTH, HOLE_HEIGHT));
						}
					}
				}
			}
			for (int page = 0; page < numberOfPages; page++) {
				File outputfile = new File("C:/tmp/" + NAME + "-Page" + page
						+ ".png");
				ImageIO.write(pages[page], "png", outputfile);
			}
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