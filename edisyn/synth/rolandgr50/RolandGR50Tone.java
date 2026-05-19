/***
    Copyright 2017-2026 by Sean Luke and contributors
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandgr50;

import edisyn.*;
import edisyn.gui.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.sound.midi.*;

/**
   A patch editor for Roland GR-50 Tones.

   <p>The GR-50 uses LA (Linear Arithmetic) synthesis, identical in structure to the Roland
   D-110. A Tone is composed of up to four Partials (oscillators) and a Common Block, and
   is organized identically to a D-110 Tone: 14 bytes of Common data followed by
   4 × 58 bytes of Partial data = 246 bytes total (TEMP_TONE_LENGTH).

   <p>The GR-50 differs from the D-110 in its outer "Patch" structure: each GR-50 Patch
   contains two Tones per string (1st Tone and 2nd Tone) for up to six strings,
   rather than the D-110's eight independent MIDI parts. The eight tone temp slots
   accessible here (P1–P8) correspond to the 1st and 2nd tones of strings 1–4,
   matching the PG-10 programmer layout.

   <p>Key differences from D-110 Tone:
   <ul>
   <li>WG Pitch Key Follow extended from 0–16 to 0–18 (two additional values)
   <li>Four tone banks: Preset A (a), Preset B (b), Internal/Card (i/c), Rhythm (r)
   <li>P1–P8 slots = string1-1st, string1-2nd, string2-1st, string2-2nd, ..., string4-1st, string4-2nd
   </ul>

   <p>The SysEx format is identical to D-110: Roland manufacturer 0x41, model 0x16,
   DT1 command 0x12, with the same address layout.

   @author Sean Luke
*/

public class RolandGR50Tone extends Synth
    {
    // GR-50 has the same 256-entry PCM list as D-110 (two banks of 128)
    public static final String[] PCM = new String[] {
        "Bass Drum-1", "Bass Drum-2", "Bass Drum-3", "Snare Drum-1", "Snare Drum-2", "Snare Drum-3", "Snare Drum-4", "Tom Tom-1", "Tom Tom-2", "High Hat",
        "High Hat (Loop)", "Crash Cymbal-1", "Crash Cymbal-2 (Loop)", "Ride Cymbal-1", "Ride Cymbal-2 (Loop)", "Cup", "China Cymbal-1", "China Cymbal-2 (Loop)",
        "Rim Shot", "Hand Clap", "Mute High Conga", "Conga", "Bongo", "Cowbell", "Tambourine", "Agogo", "Claves", "Timbale High", "Timbale Low", "Cabasa",
        "Timpani Attack", "Timpani", "Acoustic Piano High", "Acoustic Piano Low", "Piano Forte Thump", "Organ Percussion", "Trumpet", "Lips", "Trombone",
        "Clarinet", "Flute High", "Flute Low", "Steamer", "Indian Flute", "Breath", "Vibraphone High", "Vibraphone Low", "Marimba", "Xylophone High",
        "Xylophone Low", "Kalimba", "Wind Bell", "Chime Bar", "Hammer", "Guiro", "Chink", "Nails", "Fretless Bass", "Pull Bass", "Slap Bass", "Thump Bass",
        "Acoustic Bass", "Electric Bass", "Gut Guitar", "Steel Guitar", "Dirty Guitar", "Pizzicato", "Harp", "Contrabass", "Cello", "Violin-1", "Violin-2",
        "Koto", "Draw Bars (Loop)", "High Organ (Loop)", "Low Organ (Loop)", "Trumpet (Loop)", "Trombone (Loop)", "Sax-1 (Loop)", "Sax-2 (Loop)", "Reed (Loop)",
        "Slap Bass (Loop)", "Acoustic Bass (Loop)", "Electric Bass-1 (Loop)", "Electric Bass-2 (Loop)", "Gut Guitar (Loop)", "Steel Guitar (Loop)",
        "Electric Guitar (Loop)", "Clav (Loop)", "Cello (Loop)", "Violin (Loop)", "Electric Piano-1 (Loop)", "Electric Piano-2 (Loop)", "Harpsichord-1 (Loop)",
        "Harpsichord-2 (Loop)", "Telephone Bell (Loop)", "Female Voice-1 (Loop)", "Female Voice-2 (Loop)", "Male Voice-1 (Loop)", "Male Voice-2 (Loop)",
        "Spectrum-1 (Loop)", "Spectrum-2 (Loop)", "Spectrum-3 (Loop)", "Spectrum-4 (Loop)", "Spectrum-5 (Loop)", "Spectrum-6 (Loop)", "Spectrum-7 (Loop)",
        "Spectrum-8 (Loop)", "Spectrum-9 (Loop)", "Spectrum-10 (Loop)", "Noise (Loop)", "Shot-1", "Shot-2", "Shot-3", "Shot-4", "Shot-5", "Shot-6",
        "Shot-7", "Shot-8", "Shot-9", "Shot-10", "Shot-11", "Shot-12", "Shot-13", "Shot-14", "Shot-15", "Shot-16", "Shot-17", "Bass Drum-1", "Bass Drum-2",
        "Bass Drum-3", "Snare Drum-1", "Snare Drum-2", "Snare Drum-3", "Snare Drum-4", "Tom Tom-1", "Tom Tom-2", "High Hat", "High Hat (Loop)",
        "Crash Cymbal-1", "Crash Cymbal-2 (Loop)", "Ride Cymbal-1", "Ride Cymbal-2 (Loop)", "Cup", "China Cymbal-1", "China Cymbal-2 (Loop)", "Rim Shot",
        "Hand Clap", "Mute High Conga", "Conga", "Bongo", "Cowbell", "Tambourine", "Agogo", "Claves", "Timbale High", "Timbale Low", "Cabasa", "Loop-1",
        "Loop-2", "Loop-3", "Loop-4", "Loop-5", "Loop-6", "Loop-7", "Loop-8", "Loop-9", "Loop-10", "Loop-11", "Loop-12", "Loop-13", "Loop-14", "Loop-15",
        "Loop-16", "Loop-17", "Loop-18", "Loop-19", "Loop-20", "Loop-21", "Loop-22", "Loop-23", "Loop-24", "Loop-25", "Loop-26", "Loop-27", "Loop-28",
        "Loop-29", "Loop-30", "Loop-31", "Loop-32", "Loop-33", "Loop-34", "Loop-35", "Loop-36", "Loop-37", "Loop-38", "Loop-39", "Loop-40", "Loop-41",
        "Loop-42", "Loop-43", "Loop-44", "Loop-45", "Loop-46", "Loop-47", "Loop-48", "Loop-49", "Loop-50", "Loop-51", "Loop-52", "Loop-53", "Loop-54",
        "Loop-55", "Loop-56", "Loop-57", "Loop-58", "Loop-59", "Loop-60", "Loop-61", "Loop-62", "Loop-63", "Loop-64", "Jam-1", "Jam-2", "Jam-3", "Jam-4",
        "Jam-5", "Jam-6", "Jam-7", "Jam-8", "Jam-9", "Jam-10", "Jam-11", "Jam-12", "Jam-13", "Jam-14", "Jam-15", "Jam-16", "Jam-17", "Jam-18", "Jam-19",
        "Jam-20", "Jam-21", "Jam-22", "Jam-23", "Jam-24", "Jam-25", "Jam-26", "Jam-27", "Jam-28", "Jam-29", "Jam-30", "Jam-31", "Jam-32", "Jam-33", "Jam-34" };

    // GR-50 spec (Table 1) defines four tone groups: 0=a, 1=b, 2=i/c, 3=r
    // There is no separate card bank — inserting a card replaces the internal (i/c) bank.
    public static final String[] TONE_GROUP_SHORT = new String[] { "a", "b", "i", "r" };
    public static final String[] TONE_GROUP = new String[] { "Preset A", "Preset B", "Internal/Card", "Rhythm" };
    public static final String[] WRITEABLE_TONE_GROUP = new String[] { "Internal/Card" };

    // GR-50 extends D-110 key follow from 0-16 to 0-18 (adds values for key follow 3 and 4)
    public static final String[] WG_KEYFOLLOW = new String[] {
        "-1", "-1/2", "-1/4", "0", "1/8", "1/4", "3/8", "1/2", "5/8", "3/4", "7/8", "1", "5/4", "3/2", "2", "S1", "S2", "3", "4"
        };
    public static final String[] TVF_KEYFOLLOW = new String[] { "-1", "-1/2", "-1/4", "0", "1/8", "1/4", "3/8", "1/2", "5/8", "3/4", "7/8", "1", "5/4", "3/2", "2" };
    public static final String[] WG_WAVEFORM = new String[] { "Square", "Sawtooth" };
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };

    // GR-50 uses the same 13 partial structure diagrams as D-110 — reuse the same PNG files from D-110
    public static final ImageIcon[] STRUCTURE_ICONS = new ImageIcon[]
        {
        new ImageIcon(RolandGR50Tone.class.getResource("Structure1.png")),
        new ImageIcon(RolandGR50Tone.class.getResource("Structure2.png")),
        new ImageIcon(RolandGR50Tone.class.getResource("Structure3.png")),
        new ImageIcon(RolandGR50Tone.class.getResource("Structure4.png")),
        new ImageIcon(RolandGR50Tone.class.getResource("Structure5.png")),
        new ImageIcon(RolandGR50Tone.class.getResource("Structure6.png")),
        new ImageIcon(RolandGR50Tone.class.getResource("Structure7.png")),
        new ImageIcon(RolandGR50Tone.class.getResource("Structure8.png")),
        new ImageIcon(RolandGR50Tone.class.getResource("Structure9.png")),
        new ImageIcon(RolandGR50Tone.class.getResource("Structure10.png")),
        new ImageIcon(RolandGR50Tone.class.getResource("Structure11.png")),
        new ImageIcon(RolandGR50Tone.class.getResource("Structure12.png")),
        new ImageIcon(RolandGR50Tone.class.getResource("Structure13.png"))
        };

    // Tone temp slot indices: P1=string1-1st, P2=string1-2nd, P3=string2-1st, ..., P8=string4-2nd
    // Guitar tone-temp slots: 6 strings × 2 tones each = 12 slots (P1–P12).
    // The GR-50 address table confirms slot N maps to 04 (N*246)_in_7bit.
    // Only P1–P8 have a Timbre Temporary entry (03 00 00–03 00 70, stride 0x10).
    // P9–P12 (strings 5–6) and the two Multi-Timbre slots have NO Timbre Temp.
    public static final int PART_1  = 0;
    public static final int PART_2  = 1;
    public static final int PART_3  = 2;
    public static final int PART_4  = 3;
    public static final int PART_5  = 4;
    public static final int PART_6  = 5;
    public static final int PART_7  = 6;
    public static final int PART_8  = 7;
    public static final int PART_9  = 8;   // String 5, 1st Tone — addr 04 0F 30
    public static final int PART_10 = 9;   // String 5, 2nd Tone — addr 04 11 26
    public static final int PART_11 = 10;  // String 6, 1st Tone — addr 04 13 1C
    public static final int PART_12 = 11;  // String 6, 2nd Tone — addr 04 15 12
    // Multi-Timbre MIDI slots (confirmed from GR-50 address table, p.168):
    //   Part 1 Tone Temp = 04 17 08 (= 12×246 in 7-bit)
    //   Part 2 Tone Temp = 04 18 7E (= 13×246 in 7-bit)
    public static final int MULTI_PART_1 = 12;
    public static final int MULTI_PART_2 = 13;

    // Translate our model's bank number to the GR-50's native Tone Group encoding.
    // GR-50 Tone Group: 0=Preset A, 1=Preset B, 2=Rhythm, 3=Internal/Card
    // Our model bank:   0=Preset A, 1=Preset B, 2=Internal/Card, 3=Rhythm
    private static int bankToToneGroup(int bank)
        {
        if (bank == 2) return 3;   // Internal/Card → GR-50 group 3
        if (bank == 3) return 2;   // Rhythm        → GR-50 group 2
        return bank;               // Preset A/B unchanged
        }

    int part = PART_1;
    boolean altLayout = false;

    public static final String ALT_LAYOUT_KEY = "AltLayout";

    // Identical sizes to D-110: TEMP = 246 data bytes, MEMORY = 256 data bytes
    public static final int TEMP_TONE_LENGTH = 246;
    public static final int MEMORY_TONE_LENGTH = 256;
    // Timbre temp slots are 16 bytes each, at addresses 03 00 00, 03 00 10, 03 00 20, ...
    public static final int TEMP_TIMBRE_LENGTH = 16;

    public RolandGR50Tone()
        {
        String m = getLastX(ALT_LAYOUT_KEY, getSynthClassName());
        altLayout = (m == null ? false : Boolean.parseBoolean(m));

        if (allPartialParametersToIndex == null)
            {
            allPartialParametersToIndex = new HashMap();
            for (int i = 0; i < allPartialParameters.length; i++)
                allPartialParametersToIndex.put(allPartialParameters[i], Integer.valueOf(i));

            allCommonParametersToIndex = new HashMap();
            for (int i = 0; i < allCommonParameters.length; i++)
                allCommonParametersToIndex.put(allCommonParameters[i], Integer.valueOf(i));
            }

        if (altLayout)
            {
            JComponent sourcePanel = new SynthPanel(this);
            VBox vbox = new VBox();
            HBox hbox = new HBox();
            hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
            hbox.addLast(addGlobal(Style.COLOR_C()));
            vbox.add(hbox);
            hbox = new HBox();
            hbox.add(addWaveGroup(1, Style.COLOR_A()));
            hbox.addLast(addWaveGroup(2, Style.COLOR_B()));
            vbox.add(hbox);
            hbox = new HBox();
            hbox.add(addWaveGroup(3, Style.COLOR_A()));
            hbox.addLast(addWaveGroup(4, Style.COLOR_B()));
            vbox.add(hbox);
            sourcePanel.add(vbox, BorderLayout.CENTER);
            addTab("Wave Group", sourcePanel);

            sourcePanel = new SynthPanel(this);
            vbox = new VBox();
            hbox = new HBox();
            hbox.add(addPitch(1, Style.COLOR_A()));
            hbox.addLast(addPitch(2, Style.COLOR_B()));
            vbox.add(hbox);
            vbox.add(addPitchEnvelope(1, Style.COLOR_A()));
            vbox.add(addPitchEnvelope(2, Style.COLOR_B()));
            hbox = new HBox();
            hbox.add(addPitch(3, Style.COLOR_A()));
            hbox.addLast(addPitch(4, Style.COLOR_B()));
            vbox.add(hbox);
            vbox.add(addPitchEnvelope(3, Style.COLOR_A()));
            vbox.add(addPitchEnvelope(4, Style.COLOR_B()));
            sourcePanel.add(vbox, BorderLayout.CENTER);
            addTab("Pitch", sourcePanel);

            sourcePanel = new SynthPanel(this);
            vbox = new VBox();
            hbox = new HBox();
            hbox.add(addFilter(1, Style.COLOR_A()));
            hbox.addLast(addFilter(2, Style.COLOR_B()));
            vbox.add(hbox);
            vbox.add(addFilterEnvelope(1, Style.COLOR_A()));
            vbox.add(addFilterEnvelope(2, Style.COLOR_B()));
            hbox = new HBox();
            hbox.add(addFilter(3, Style.COLOR_A()));
            hbox.addLast(addFilter(4, Style.COLOR_B()));
            vbox.add(hbox);
            vbox.add(addFilterEnvelope(3, Style.COLOR_A()));
            vbox.add(addFilterEnvelope(4, Style.COLOR_B()));
            sourcePanel.add(vbox, BorderLayout.CENTER);
            addTab("Filter", sourcePanel);

            sourcePanel = new SynthPanel(this);
            vbox = new VBox();
            hbox = new HBox();
            hbox.add(addAmplifier(1, Style.COLOR_A()));
            hbox.addLast(addAmplifier(2, Style.COLOR_B()));
            vbox.add(hbox);
            vbox.add(addAmplifierEnvelope(1, Style.COLOR_A()));
            vbox.add(addAmplifierEnvelope(2, Style.COLOR_B()));
            hbox = new HBox();
            hbox.add(addAmplifier(3, Style.COLOR_A()));
            hbox.addLast(addAmplifier(4, Style.COLOR_B()));
            vbox.add(hbox);
            vbox.add(addAmplifierEnvelope(3, Style.COLOR_A()));
            vbox.add(addAmplifierEnvelope(4, Style.COLOR_B()));
            sourcePanel.add(vbox, BorderLayout.CENTER);
            addTab("Amplifier", sourcePanel);
            }
        else
            {
            JComponent sourcePanel = new SynthPanel(this);
            VBox vbox = new VBox();
            HBox hbox = new HBox();
            hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
            hbox.add(addGlobal(Style.COLOR_A()));
            hbox.addLast(addWaveGroup(1, Style.COLOR_A()));
            vbox.add(hbox);

            HBox hbox2 = new HBox();
            hbox2.add(addPitch(1, Style.COLOR_A()));
            hbox2.addLast(addFilter(1, Style.COLOR_B()));
            vbox.add(hbox2);
            vbox.add(addPitchEnvelope(1, Style.COLOR_A()));
            vbox.add(addFilterEnvelope(1, Style.COLOR_B()));
            vbox.add(addAmplifier(1, Style.COLOR_C()));
            vbox.add(addAmplifierEnvelope(1, Style.COLOR_C()));

            sourcePanel = new SynthPanel(this);
            sourcePanel.add(vbox, BorderLayout.CENTER);
            ((SynthPanel)sourcePanel).makePasteable("p");
            addTab("Common and Partial 1", sourcePanel);

            for (int i = 2; i < 5; i++)
                {
                sourcePanel = new SynthPanel(this);
                vbox = new VBox();
                vbox.add(addWaveGroup(i, Style.COLOR_A()));

                hbox2 = new HBox();
                hbox2.add(addPitch(i, Style.COLOR_A()));
                hbox2.addLast(addFilter(i, Style.COLOR_B()));
                vbox.add(hbox2);
                vbox.add(addPitchEnvelope(i, Style.COLOR_A()));
                vbox.add(addFilterEnvelope(i, Style.COLOR_B()));
                vbox.add(addAmplifier(i, Style.COLOR_C()));
                vbox.add(addAmplifierEnvelope(i, Style.COLOR_C()));

                sourcePanel = new SynthPanel(this);
                sourcePanel.add(vbox, BorderLayout.CENTER);
                ((SynthPanel)sourcePanel).makePasteable("p");
                addTab("Partial " + i, sourcePanel);
                }
            }

        model.set("name", "Init Tone ");
        model.set("number", 0);
        model.set("bank", 2);           // Internal/Card
        loadDefaults();
        }


    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        addGR50ToneMenu();
        return frame;
        }

    public void addGR50ToneMenu()
        {
        JMenu menu = new JMenu("GR-50");
        menubar.add(menu);

        final JCheckBoxMenuItem altLayoutMenu = new JCheckBoxMenuItem("Alternate Layout");
        altLayoutMenu.setSelected(altLayout);
        altLayoutMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setLastX("" + altLayoutMenu.isSelected(), ALT_LAYOUT_KEY, getSynthClassName(), true);
                }
            });
        menu.add(altLayoutMenu);

        menu.addSeparator();

        ButtonGroup g = new ButtonGroup();
        // All 12 guitar tone-temp slots plus 2 Multi-Timbre MIDI slots.
        // Slots 0–7 (P1–P8) have Timbre Temps; slots 8–11 (P9–P12) do not.
        // Multi-Timbre slots (12–13) use dedicated Tone Temp addresses confirmed
        // by the GR-50 address table: 04 17 08 (Part 1) and 04 18 7E (Part 2).
        int[] slotIndices = new int[]
            { 0, 1, 2, 3, 4, 5, 6, 7, PART_9, PART_10, PART_11, PART_12, MULTI_PART_1, MULTI_PART_2 };
        String[] displayLabels = new String[]
            {
            "String 1, 1st Tone (P1)",  "String 1, 2nd Tone (P2)",
            "String 2, 1st Tone (P3)",  "String 2, 2nd Tone (P4)",
            "String 3, 1st Tone (P5)",  "String 3, 2nd Tone (P6)",
            "String 4, 1st Tone (P7)",  "String 4, 2nd Tone (P8)",
            "String 5, 1st Tone (P9)",  "String 5, 2nd Tone (P10)",
            "String 6, 1st Tone (P11)", "String 6, 2nd Tone (P12)",
            "Multi-Timbre Part 1 (MIDI)", "Multi-Timbre Part 2 (MIDI)"
            };
        for (int i = 0; i < slotIndices.length; i++)
            {
            if (i == 12) menu.addSeparator();  // separator before Multi-Timbre slots
            final int slotPart = slotIndices[i];
            JRadioButtonMenuItem m = new JRadioButtonMenuItem("Edit Tone Slot: " + displayLabels[i]);
            if (i == 0) m.setSelected(true);
            m.addActionListener(new ActionListener()
                {
                public void actionPerformed(ActionEvent e)
                    {
                    part = slotPart;
                    }
                });
            g.add(m);
            menu.add(m);
            }

        menu.addSeparator();

        JMenuItem dumpItem = new JMenuItem("Dump All Internal Tone Names to Console");
        dumpItem.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                dumpAllInternalToneNames();
                }
            });
        menu.add(dumpItem);
        }


    // Sends RQ1 for all 64 Internal/Card tones (bank=2) in sequence.
    // Each response will be received asynchronously and routed through parse(),
    // which prints the address and name to the console.  The final tone received
    // will be loaded into the editor as the current model.
    void dumpAllInternalToneNames()
        {
        System.out.println("GR-50: === Requesting all 64 Internal/Card Tone names ===");
        new Thread(new Runnable()
            {
            public void run()
                {
                for (int n = 0; n < 64; n++)
                    {
                    final int slot = n;
                    byte[] msg = buildMemoryToneRequest(slot);
                    tryToSendSysex(msg);
                    // Wait between requests so the GR-50 has time to respond
                    try { Thread.sleep(300); }
                    catch (InterruptedException ex) { Thread.currentThread().interrupt(); break; }
                    }
                System.out.println("GR-50: === All 64 Internal/Card tone requests sent ===");
                }
            }, "GR-50-InternalDump").start();
        }


    public String getDefaultResourceFileName() { return "RolandGR50Tone.init"; }
    public String getHTMLResourceFileName() { return "RolandGR50Tone.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(writing ? WRITEABLE_TONE_GROUP : TONE_GROUP);
        JTextField number = new SelectedTextField("" + (model.get("number") + 1), 3);
        if (!writing) bank.setSelectedIndex(model.get("bank"));

        while (true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Tone Number" },
                new JComponent[] { bank, number }, title, "<html>Enter Bank and Tone Number.<br>These will be updated in " + slotLabel(part) + ".</html>");

            if (!result) return false;

            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Tone Number must be an integer 1...64");
                continue;
                }
            if (n < 1 || n > 64)
                {
                showSimpleError(title, "The Tone Number must be an integer 1...64");
                continue;
                }

            n--;
            change.set("number", n);
            change.set("bank", writing ? 2 : bank.getSelectedIndex());
            return true;
            }
        }

    static String slotLabel(int part)
        {
        String[] labels = { "String 1 / 1st Tone (P1)", "String 1 / 2nd Tone (P2)",
                            "String 2 / 1st Tone (P3)", "String 2 / 2nd Tone (P4)",
                            "String 3 / 1st Tone (P5)", "String 3 / 2nd Tone (P6)",
                            "String 4 / 1st Tone (P7)", "String 4 / 2nd Tone (P8)" };
        return (part >= 0 && part < labels.length) ? labels[part] : "Part " + (part + 1);
        }


    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);

        JComponent comp;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, "Tone", 9, true);
        hbox2.add(comp);
        vbox.add(hbox2);

        comp = new StringComponent("Tone Name", this, "name", MAXIMUM_NAME_LENGTH, "Name must be up to 10 ASCII characters.")
            {
            public String replace(String val) { return revisePatchName(val); }
            public void update(String key, Model model)
                {
                super.update(key, model);
                updateTitle();
                }
            };
        vbox.add(comp);
        hbox.add(vbox);
        hbox.add(Strut.makeHorizontalStrut(70));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addGlobal(Color color)
        {
        Category category = new Category(this, "Global", color);

        JComponent comp;
        VBox vbox = new VBox();
        HBox hbox = new HBox();

        comp = new LabelledDial("Structure 1-2", this, "structure1and2", color, 0, 12, -1);
        model.removeMetricMinMax("structure1and2");
        hbox.add(comp);

        hbox.add(Strut.makeHorizontalStrut(8));

        comp = new IconDisplay(null, STRUCTURE_ICONS, this, "structure1and2", 106, 80);
        hbox.add(comp);

        hbox.add(Strut.makeHorizontalStrut(8));

        comp = new LabelledDial("Structure 3-4", this, "structure3and4", color, 0, 12, -1);
        model.removeMetricMinMax("structure3and4");
        hbox.add(comp);

        hbox.add(Strut.makeHorizontalStrut(8));

        comp = new IconDisplay(null, STRUCTURE_ICONS, this, "structure3and4", 106, 80);
        hbox.add(comp);
        vbox.add(hbox);
        vbox.add(Strut.makeVerticalStrut(8));

        HBox hbox2 = new HBox();
        comp = new CheckBox("Env No Sustain", this, "envmode");
        ((CheckBox)comp).addToWidth(2);
        hbox2.add(comp);
        comp = new CheckBox("Mute 1", this, "p1mute", true);
        hbox2.add(comp);
        comp = new CheckBox("Mute 2", this, "p2mute", true);
        hbox2.add(comp);
        comp = new CheckBox("Mute 3", this, "p3mute", true);
        hbox2.add(comp);
        comp = new CheckBox("Mute 4", this, "p4mute", true);
        hbox2.add(comp);

        vbox.add(hbox2);
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addWaveGroup(int partial, Color color)
        {
        Category category = new Category(this, "Wavegroup" + (altLayout ? " " + partial : ""), color);
        category.makePasteable("p");

        JComponent comp;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        comp = new Chooser("[S] Synthesizer Waveform", this, "p" + partial + "wgwaveform", WG_WAVEFORM);
        vbox.add(comp);
        comp = new Chooser("[P] PCM Wave", this, "p" + partial + "wgpcmwavenumber", PCM);
        vbox.add(comp);

        HBox hbox2 = new HBox();
        comp = new CheckBox("Pitch Bend", this, "p" + partial + "wgpitchbendersw");
        hbox2.add(comp);
        vbox.add(hbox2);
        hbox.add(vbox);

        comp = new LabelledDial("Pulse Width", this, "p" + partial + "wgpulsewidth", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Pulse Width", this, "p" + partial + "wgpwvelosens", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Velocity Sensitivity");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addPitch(int partial, Color color)
        {
        Category category = new Category(this, "Pitch" + (altLayout ? " " + partial : ""), color);
        category.makePasteable("p");

        JComponent comp;
        HBox hbox = new HBox();

        comp = new LabelledDial("Coarse", this, "p" + partial + "wgpitchcoarse", color, 0, 96)
            {
            public String map(int value) { return NOTES[value % 12] + (value / 12 + 1); }
            };
        hbox.add(comp);

        comp = new LabelledDial("Fine", this, "p" + partial + "wgpitchfine", color, 0, 100, 50);
        hbox.add(comp);

        // GR-50 extends key follow to 0-18 (vs D-110's 0-16)
        comp = new LabelledDial("Keyfollow", this, "p" + partial + "wgpitchkeyfollow", color, 0, 18)
            {
            public String map(int value) { return WG_KEYFOLLOW[value]; }
            };
        hbox.add(comp);

        comp = new LabelledDial("LFO Rate", this, "p" + partial + "plforate", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("LFO Depth", this, "p" + partial + "plfodepth", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("LFO Mod", this, "p" + partial + "plfomodsens", color, 0, 100);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addPitchEnvelope(int partial, Color color)
        {
        Category category = new Category(this, "Pitch Envelope" + (altLayout ? " " + partial : ""), color);
        category.makePasteable("p");

        JComponent comp;
        HBox hbox = new HBox();

        comp = new LabelledDial("Depth", this, "p" + partial + "penvdepth", color, 0, 10);
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "p" + partial + "penvvelosens", color, 0, 3);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);

        comp = new LabelledDial("Time", this, "p" + partial + "penvtimekeyf", color, 0, 4);
        ((LabelledDial)comp).addAdditionalLabel("Key Follow");
        hbox.add(comp);

        comp = new LabelledDial("Level 0", this, "p" + partial + "penvlevel0", color, 0, 100, 50);
        hbox.add(comp);
        comp = new LabelledDial("Time 1", this, "p" + partial + "penvtime1", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Level 1", this, "p" + partial + "penvlevel1", color, 0, 100, 50);
        hbox.add(comp);
        comp = new LabelledDial("Time 2", this, "p" + partial + "penvtime2", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Level 2", this, "p" + partial + "penvlevel2", color, 0, 100, 50);
        hbox.add(comp);
        comp = new LabelledDial("Time 3", this, "p" + partial + "penvtime3", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Sustain Level", this, "p" + partial + "penvsustainlevel", color, 0, 100, 50);
        hbox.add(comp);
        comp = new LabelledDial("Time 4", this, "p" + partial + "penvtime4", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("End Level", this, "p" + partial + "endlevel", color, 0, 100, 50);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(),
            new String[] { null, "p" + partial + "penvtime1", "p" + partial + "penvtime2", "p" + partial + "penvtime3", null, "p" + partial + "penvtime4" },
            new String[] { "p" + partial + "penvlevel0", "p" + partial + "penvlevel1", "p" + partial + "penvlevel2",
                           "p" + partial + "penvsustainlevel", "p" + partial + "penvsustainlevel", "p" + partial + "endlevel" },
            new double[] { 0, 0.2 / 100.0, 0.2 / 100.0, 0.2 / 100.0, 0.2, 0.2 / 100.0 },
            new double[] { 1.0 / 100.0, 1.0 / 100.0, 1.0 / 100.0, 1.0 / 100.0, 1.0 / 100.0, 1.0 / 100.0 });
        ((EnvelopeDisplay)comp).setAxis(1.0 / 100.0 * 50.0);
        hbox.addLast(comp);
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addFilter(int partial, Color color)
        {
        Category category = new Category(this, "Filter" + (altLayout ? " " + partial : ""), color);
        category.makePasteable("p");

        JComponent comp;
        HBox hbox = new HBox();

        comp = new LabelledDial("Cutoff", this, "p" + partial + "tvfcutofffreq", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Resonance", this, "p" + partial + "tvfresonance", color, 0, 30);
        hbox.add(comp);

        comp = new LabelledDial("Keyfollow", this, "p" + partial + "tvfkeyfollow", color, 0, 14)
            {
            public String map(int value) { return TVF_KEYFOLLOW[value]; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Bias Point", this, "p" + partial + "tvfbiaspoint", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value < 64)
                    {
                    if (value == 0) return "<A1";
                    else if (value == 1) return "<A#1";
                    else if (value == 2) return "<B1";
                    else return "<" + NOTES[(value - 3) % 12] + ((value - 3) / 12 + 2);
                    }
                else
                    {
                    value -= 64;
                    if (value == 0) return ">A1";
                    else if (value == 1) return ">A#1";
                    else if (value == 2) return ">B1";
                    else return ">" + NOTES[(value - 3) % 12] + ((value - 3) / 12 + 2);
                    }
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Bias Level", this, "p" + partial + "tvfbiaslevel", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addFilterEnvelope(int partial, Color color)
        {
        Category category = new Category(this, "Filter Envelope" + (altLayout ? " " + partial : ""), color);
        category.makePasteable("p");

        JComponent comp;
        HBox hbox = new HBox();

        comp = new LabelledDial("Depth", this, "p" + partial + "tvfenvdepth", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "p" + partial + "tvfenvvelosens", color, 0, 100);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);

        comp = new LabelledDial("Depth", this, "p" + partial + "tvfenvdepthkeyfollow", color, 0, 4);
        ((LabelledDial)comp).addAdditionalLabel("Key Follow");
        hbox.add(comp);

        comp = new LabelledDial("Time", this, "p" + partial + "tvfenvtimekeyfollow", color, 0, 4);
        ((LabelledDial)comp).addAdditionalLabel("Key Follow");
        hbox.add(comp);

        comp = new LabelledDial("Time 1", this, "p" + partial + "tvfenvtime1", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Level 1", this, "p" + partial + "tvfenvlevel1", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Time 2", this, "p" + partial + "tvfenvtime2", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Level 2", this, "p" + partial + "tvfenvlevel2", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Time 3", this, "p" + partial + "tvfenvtime3", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Level 3", this, "p" + partial + "tvfenvlevel3", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Time 4", this, "p" + partial + "tvfenvtime4", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Sustain Level", this, "p" + partial + "tvfenvsustainlevel", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Time 5", this, "p" + partial + "tvfenvtime5", color, 0, 100);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(),
            new String[] { null, "p" + partial + "tvfenvtime1", "p" + partial + "tvfenvtime2", "p" + partial + "tvfenvtime3", "p" + partial + "tvfenvtime4", null, "p" + partial + "tvfenvtime5" },
            new String[] { null, "p" + partial + "tvfenvlevel1", "p" + partial + "tvfenvlevel2", "p" + partial + "tvfenvlevel3", "p" + partial + "tvfenvsustainlevel", "p" + partial + "tvfenvsustainlevel", null },
            new double[] { 0, 0.2 / 100.0, 0.2 / 100.0, 0.2 / 100.0, 0.2 / 100.0, 0.2, 0.2 / 100.0 },
            new double[] { 0, 1.0 / 100.0, 1.0 / 100.0, 1.0 / 100.0, 1.0 / 100.0, 1.0 / 100.0, 0 });
        hbox.addLast(comp);
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addAmplifier(int partial, Color color)
        {
        Category category = new Category(this, "Amplifier" + (altLayout ? " " + partial : ""), color);
        category.makePasteable("p");

        JComponent comp;
        HBox hbox = new HBox();

        comp = new LabelledDial("Level", this, "p" + partial + "tvalevel", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "p" + partial + "tvavelosens", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);

        comp = new LabelledDial("Bias Point 1", this, "p" + partial + "tvabiaspoint1", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value < 64)
                    {
                    if (value == 0) return "<A1";
                    else if (value == 1) return "<A#1";
                    else if (value == 2) return "<B1";
                    else return "<" + NOTES[(value - 3) % 12] + ((value - 3) / 12 + 2);
                    }
                else
                    {
                    value -= 64;
                    if (value == 0) return ">A1";
                    else if (value == 1) return ">A#1";
                    else if (value == 2) return ">B1";
                    else return ">" + NOTES[(value - 3) % 12] + ((value - 3) / 12 + 2);
                    }
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Bias Level 1", this, "p" + partial + "tvabiaslevel1", color, 0, 12, 12)
            {
            public int getDefaultValue() { return 12; }
            public double getStartAngle() { return 180; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Bias Point 2", this, "p" + partial + "tvabiaspoint2", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value < 64)
                    {
                    if (value == 0) return "<A1";
                    else if (value == 1) return "<A#1";
                    else if (value == 2) return "<B1";
                    else return "<" + NOTES[(value - 3) % 12] + ((value - 3) / 12 + 2);
                    }
                else
                    {
                    value -= 64;
                    if (value == 0) return ">A1";
                    else if (value == 1) return ">A#1";
                    else if (value == 2) return ">B1";
                    else return ">" + NOTES[(value - 3) % 12] + ((value - 3) / 12 + 2);
                    }
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Bias Level 2", this, "p" + partial + "tvabiaslevel2", color, 0, 12, 12)
            {
            public int getDefaultValue() { return 12; }
            public double getStartAngle() { return 180; }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addAmplifierEnvelope(int partial, Color color)
        {
        Category category = new Category(this, "Amplifier Envelope" + (altLayout ? " " + partial : ""), color);
        category.makePasteable("p");

        JComponent comp;
        HBox hbox = new HBox();

        comp = new LabelledDial("Time", this, "p" + partial + "tvaenvtimekeyfollow", color, 0, 4);
        ((LabelledDial)comp).addAdditionalLabel("Key Follow");
        hbox.add(comp);

        comp = new LabelledDial("Time 1", this, "p" + partial + "tvaenvtime1velfollow", color, 0, 4);
        ((LabelledDial)comp).addAdditionalLabel("Velocity Follow");
        hbox.add(comp);

        comp = new LabelledDial("Time 1", this, "p" + partial + "tvaenvtime1", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Level 1", this, "p" + partial + "tvaenvlevel1", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Time 2", this, "p" + partial + "tvaenvtime2", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Level 2", this, "p" + partial + "tvaenvlevel2", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Time 3", this, "p" + partial + "tvaenvtime3", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Level 3", this, "p" + partial + "tvaenvlevel3", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Time 4", this, "p" + partial + "tvaenvtime4", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Sustain Level", this, "p" + partial + "tvaenvsustainlevel", color, 0, 100);
        hbox.add(comp);
        comp = new LabelledDial("Time 5", this, "p" + partial + "tvaenvtime5", color, 0, 100);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(),
            new String[] { null, "p" + partial + "tvaenvtime1", "p" + partial + "tvaenvtime2", "p" + partial + "tvaenvtime3", "p" + partial + "tvaenvtime4", null, "p" + partial + "tvaenvtime5" },
            new String[] { null, "p" + partial + "tvaenvlevel1", "p" + partial + "tvaenvlevel2", "p" + partial + "tvaenvlevel3", "p" + partial + "tvaenvsustainlevel", "p" + partial + "tvaenvsustainlevel", null },
            new double[] { 0, 0.2 / 100.0, 0.2 / 100.0, 0.2 / 100.0, 0.2 / 100.0, 0.2, 0.2 / 100.0 },
            new double[] { 0, 1.0 / 100.0, 1.0 / 100.0, 1.0 / 100.0, 1.0 / 100.0, 1.0 / 100.0, 0 });
        hbox.addLast(comp);
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    // Parameter arrays — identical to D-110 (same LA synthesis chip, same memory layout)
    static HashMap allPartialParametersToIndex = null;
    static final String[] allPartialParameters = new String[]
        {
        "wgpitchcoarse", "wgpitchfine", "wgpitchkeyfollow", "wgpitchbendersw",
        "wgwaveform",           // special: getData writes 2 bytes
        "wgpcmwavenumber",      // special: continue (combined with wgwaveform)
        "wgpulsewidth", "wgpwvelosens",
        "penvdepth", "penvvelosens", "penvtimekeyf",
        "penvtime1", "penvtime2", "penvtime3", "penvtime4",
        "penvlevel0", "penvlevel1", "penvlevel2", "penvsustainlevel", "endlevel",
        "plforate", "plfodepth", "plfomodsens",
        "tvfcutofffreq", "tvfresonance", "tvfkeyfollow", "tvfbiaspoint", "tvfbiaslevel",
        "tvfenvdepth", "tvfenvvelosens", "tvfenvdepthkeyfollow", "tvfenvtimekeyfollow",
        "tvfenvtime1", "tvfenvtime2", "tvfenvtime3", "tvfenvtime4", "tvfenvtime5",
        "tvfenvlevel1", "tvfenvlevel2", "tvfenvlevel3", "tvfenvsustainlevel",
        "tvalevel", "tvavelosens",
        "tvabiaspoint1", "tvabiaslevel1", "tvabiaspoint2", "tvabiaslevel2",
        "tvaenvtimekeyfollow", "tvaenvtime1velfollow",
        "tvaenvtime1", "tvaenvtime2", "tvaenvtime3", "tvaenvtime4", "tvaenvtime5",
        "tvaenvlevel1", "tvaenvlevel2", "tvaenvlevel3", "tvaenvsustainlevel",
        };

    static HashMap allCommonParametersToIndex = null;
    static final String[] allCommonParameters = new String[]
        {
        "name",                 // special: 10 bytes
        "structure1and2",
        "structure3and4",
        "p1mute",               // combined mute byte
        "p2mute",
        "p3mute",
        "p4mute",
        "envmode",
        };


    // Roland GR-50 uses the same device ID range as D-110: 17-32 (IDs start at 17)
    public byte getID()
        {
        try
            {
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 17) return (byte)(b - 1);
            }
        catch (NullPointerException e) { }
        catch (NumberFormatException e) { Synth.handleException(e); }
        return (byte)16;
        }

    public String reviseID(String id)
        {
        try
            {
            int val = Integer.parseInt(id);
            if (val < 17) val = 17;
            if (val > 32) val = 32;
            return "" + val;
            }
        catch (NumberFormatException ex)
            {
            return "" + (getID() + 1);
            }
        }


    public byte produceChecksum(byte[] data) { return produceChecksum(data, 0, data.length); }

    public byte produceChecksum(byte[] data, int start, int end)
        {
        int check = 0;
        for (int i = start; i < end; i++) check += data[i];
        check = check & 0x7F;
        check = 0x80 - check;
        if (check == 0x80) check = 0;
        return (byte)check;
        }


    public byte[] getData(String key)
        {
        if (key.endsWith("mute"))
            {
            return new byte[]
                {
                (byte)(
                    (model.get("p4mute") << 3) |
                    (model.get("p3mute") << 2) |
                    (model.get("p2mute") << 1) |
                    (model.get("p1mute") << 0))
                };
            }
        else if (key.equals("name"))
            {
            byte[] data = new byte[10];
            String name = model.get(key, "Untitled");
            for (int i = 0; i < name.length(); i++)
                data[i] = (byte)(name.charAt(i));
            return data;
            }
        else if (key.endsWith("wgwaveform") || key.endsWith("wgpcmwavenumber"))
            {
            int partial = (int)(key.charAt(1) - '0');
            int wf  = model.get("p" + partial + "wgwaveform", 0);
            int wn  = model.get("p" + partial + "wgpcmwavenumber", 0);
            int bnk = wn / 128;
            int num = wn % 128;
            byte wfbank = (byte)((bnk << 1) | wf);
            byte pcmnum = (byte)(num);
            return new byte[] { wfbank, pcmnum };
            }
        else
            {
            return new byte[] { (byte)model.get(key) };
            }
        }


    public byte[] emit(String key)
        {
        if (key.equals("number") || key.equals("bank")) return new byte[0];

        byte AA = (byte)(0x04);
        int loc = part * TEMP_TONE_LENGTH;
        byte BB = (byte)((loc >>> 7) & 127);
        byte CC = (byte)(loc & 127);

        if (key.endsWith("mute"))
            {
            CC += (byte)0x0C;
            if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
            }
        else if (key.startsWith("p1"))
            {
            CC = (byte)(CC + 0x0E);
            if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
            if (key.endsWith("wgwaveform") || key.endsWith("wgpcmwavenumber"))
                {
                CC += (byte)0x04;
                if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
                }
            else
                {
                int offset = CC + ((Integer)(allPartialParametersToIndex.get(key.substring(2)))).intValue();
                BB += (byte)(offset / 128);
                CC = (byte)(offset % 128);
                }
            }
        else if (key.startsWith("p2"))
            {
            CC = (byte)(CC + 0x48);
            if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
            if (key.endsWith("wgwaveform") || key.endsWith("wgpcmwavenumber"))
                {
                CC += (byte)0x04;
                if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
                }
            else
                {
                int offset = CC + ((Integer)(allPartialParametersToIndex.get(key.substring(2)))).intValue();
                BB += (byte)(offset / 128);
                CC = (byte)(offset % 128);
                }
            }
        else if (key.startsWith("p3"))
            {
            BB = (byte)(BB + 0x01);
            CC = (byte)(CC + 0x02);
            if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
            if (key.endsWith("wgwaveform") || key.endsWith("wgpcmwavenumber"))
                {
                CC += (byte)0x04;
                if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
                }
            else
                {
                int offset = CC + ((Integer)(allPartialParametersToIndex.get(key.substring(2)))).intValue();
                BB += (byte)(offset / 128);
                CC = (byte)(offset % 128);
                }
            }
        else if (key.startsWith("p4"))
            {
            BB = (byte)(BB + 0x01);
            CC = (byte)(CC + 0x3C);
            if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
            if (key.endsWith("wgwaveform") || key.endsWith("wgpcmwavenumber"))
                {
                CC += (byte)0x04;
                if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
                }
            else
                {
                int offset = CC + ((Integer)(allPartialParametersToIndex.get(key.substring(2)))).intValue();
                BB += (byte)(offset / 128);
                CC = (byte)(offset % 128);
                }
            }
        else    // Common parameters
            {
            if (key.equals("name"))
                {
                // address stays at loc+0
                }
            else if (key.equals("envmode"))
                {
                CC = (byte)0x0D;
                }
            else
                {
                CC += (byte)(0x0A - 1);
                CC = (byte)(CC + ((Integer)(allCommonParametersToIndex.get(key))).intValue());
                if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
                }
            }

        byte[] payload = getData(key);

        if (payload.length == 10)
            {
            byte[] data = new byte[20];
            byte[] checkdata = new byte[3 + 10];
            System.arraycopy(new byte[] { AA, BB, CC }, 0, checkdata, 0, 3);
            System.arraycopy(payload, 0, checkdata, 3, payload.length);
            byte checksum = produceChecksum(checkdata);
            data[0] = (byte)0xF0; data[1] = (byte)0x41; data[2] = getID();
            data[3] = (byte)0x16; data[4] = (byte)0x12;
            System.arraycopy(checkdata, 0, data, 5, checkdata.length);
            data[18] = checksum; data[19] = (byte)0xF7;
            return data;
            }
        else if (payload.length == 2)
            {
            byte checksum = produceChecksum(new byte[] { AA, BB, CC, payload[0], payload[1] });
            return new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x16, (byte)0x12, AA, BB, CC, payload[0], payload[1], checksum, (byte)0xF7 };
            }
        else
            {
            byte checksum = produceChecksum(new byte[] { AA, BB, CC, payload[0] });
            return new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x16, (byte)0x12, AA, BB, CC, payload[0], checksum, (byte)0xF7 };
            }
        }


    public boolean getSendsParametersAfterNonMergeParse() { return true; }


    public int parse(byte[] data, boolean fromFile)
        {
        int AA = data[5] & 0xFF;
        int BB = data[6] & 0xFF;
        int CC = data[7] & 0xFF;
        if (AA == 0x08)
            {
            model.set("number", BB / 2);
            model.set("bank", 2);
            }

        int pos = 8;
        String name = "";
        for (int i = 0; i < 10; i++)
            name = name + ((char)data[pos++]);

        // Print a compact summary for console verification
        String bankLabel = (AA == 0x08) ? ("i" + String.format("%02d", BB / 2 + 1))
                                         : ("temp[" + String.format("%02X%02X", BB, CC) + "]");
        System.out.println("GR-50: parse() addr=" + String.format("%02X %02X %02X", AA, BB, CC) +
            " bank=" + bankLabel + " name=[" + name.trim() + "]" +
            " fromFile=" + fromFile);
        model.set("name", name);
        model.set("structure1and2", data[pos++]);
        model.set("structure3and4", data[pos++]);
        model.set("p1mute", (data[pos] >>> 0) & 1);
        model.set("p2mute", (data[pos] >>> 1) & 1);
        model.set("p3mute", (data[pos] >>> 2) & 1);
        model.set("p4mute", (data[pos] >>> 3) & 1);
        pos++;
        model.set("envmode", data[pos++]);

        for (int t = 1; t < 5; t++)
            {
            for (int i = 0; i < allPartialParameters.length; i++)
                {
                if (allPartialParameters[i].equals("-"))
                    {
                    pos++;
                    }
                else if (allPartialParameters[i].endsWith("wgpcmwavenumber"))
                    {
                    model.set("p" + t + "wgpcmwavenumber", data[pos] | ((data[pos - 1] >>> 1) << 7));
                    pos++;
                    }
                else if (allPartialParameters[i].endsWith("wgwaveform"))
                    {
                    model.set("p" + t + "wgwaveform", data[pos++] & 0x01);
                    }
                else
                    {
                    model.set("p" + t + allPartialParameters[i], data[pos++]);
                    }
                }
            }
        revise();
        return PARSE_SUCCEEDED;
        }


    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null) tempModel = getModel();

        byte[] buf = new byte[(toWorkingMemory ? TEMP_TONE_LENGTH : MEMORY_TONE_LENGTH) + 10];

        buf[0] = (byte)0xF0; buf[1] = (byte)0x41; buf[2] = (byte)getID();
        buf[3] = (byte)0x16; buf[4] = (byte)0x12;

        if (toWorkingMemory)
            {
            int loc = part * TEMP_TONE_LENGTH;
            buf[5] = (byte)0x04;
            buf[6] = (byte)((loc >>> 7) & 127);
            buf[7] = (byte)(loc & 127);
            String partLabel = (part == MULTI_PART_1) ? "Multi-Timbre Part 1"
                             : (part == MULTI_PART_2) ? "Multi-Timbre Part 2"
                             : ("Guitar P" + (part + 1));
            System.out.println("GR-50: emit(bulk) → Tone Temp " + partLabel +
                " addr=04 " + String.format("%02X %02X", buf[6] & 0xFF, buf[7] & 0xFF));
            }
        else
            {
            int number = tempModel.get("number", 0);
            buf[5] = (byte)0x08;
            buf[6] = (byte)(number * 2);
            buf[7] = (byte)0x00;
            System.out.println("GR-50: emit(bulk) → Tone Memory I" +
                String.format("%02d", number + 1) +
                " addr=08 " + String.format("%02X 00", buf[6] & 0xFF));
            }

        int pos = 8;
        byte[] d = getData("name");
        System.arraycopy(d, 0, buf, pos, d.length);
        pos += d.length;
        buf[pos++] = getData("structure1and2")[0];
        buf[pos++] = getData("structure3and4")[0];
        buf[pos++] = getData("p1mute")[0];
        buf[pos++] = getData("envmode")[0];

        for (int t = 1; t < 5; t++)
            {
            for (int i = 0; i < allPartialParameters.length; i++)
                {
                if (allPartialParameters[i].equals("-"))
                    {
                    buf[pos++] = 0;
                    }
                else if (allPartialParameters[i].endsWith("wgpcmwavenumber"))
                    {
                    continue;   // already written as part of wgwaveform getData
                    }
                else
                    {
                    d = getData("p" + t + allPartialParameters[i]);
                    for (int j = 0; j < d.length; j++)
                        buf[pos++] = d[j];
                    }
                }
            }

        buf[buf.length - 2] = produceChecksum(buf, 5, buf.length - 2);
        buf[buf.length - 1] = (byte)0xF7;
        return buf;
        }


    public void changePatch(Model tempModel)
        {
        if (tempModel == null) tempModel = getModel();

        int number = tempModel.get("number");
        int bank   = tempModel.get("bank");

        // Only P1–P8 (parts 0–7) have a Timbre Temporary entry (03 00 00–03 00 70).
        // P9–P12 (strings 5–6, parts 8–11) and Multi-Timbre parts (12–13) have none.
        // The GR-50 Tone Group encoding in Timbre Temp differs from our model bank:
        //   GR-50: 0=Preset A, 1=Preset B, 2=Rhythm, 3=Internal/Card
        //   Model: 0=Preset A, 1=Preset B, 2=Internal/Card, 3=Rhythm
        // bankToToneGroup() translates model bank → GR-50 native group.
        if (part < 8)
            {
            byte AA = (byte)(0x03);
            int timbreLoc = part * TEMP_TIMBRE_LENGTH;
            byte BB = (byte)((timbreLoc >>> 7) & 127);
            byte CC = (byte)(timbreLoc & 127);
            int toneGroup = bankToToneGroup(bank);
            byte checksum = produceChecksum(new byte[] { AA, BB, CC, (byte)toneGroup, (byte)number });
            byte[] b = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x16, (byte)0x12,
                AA, BB, CC, (byte)toneGroup, (byte)number, checksum, (byte)0xF7 };
            System.out.println("GR-50: changePatch part=" + part +
                " modelBank=" + bank + " toneGroup=" + toneGroup + " number=" + number);
            tryToSendSysex(b);
            }

        model.set("number", number);
        model.set("bank", bank);
        }

    // For Internal/Card tones (bank=2) we request directly from Tone Memory (08 BB 00)
    // which is reliable.  For Preset A/B/Rhythm tones we fall back to the Timbre Temp
    // mechanism: changePatch() writes the Tone Group/Number into the Timbre Temp slot,
    // and then requestCurrentDump() reads back Tone Temp.  The GR-50 must be in a state
    // where that Tone Temp address is valid (i.e. the Patch using that tone is active).
    //
    // For Multi-Timbre slots (MULTI_PART_1/2): there is no Timbre Temp mechanism.
    // requestCurrentDump() just reads whatever tone is currently loaded in that Part's
    // Tone Temp.  The tone gets there via MIDI Program Change on the Part's MIDI channel.
    public boolean getAlwaysChangesPatchesOnRequestDump() { return true; }

    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null) tempModel = getModel();
        // Multi-Timbre parts have no Timbre Temp — just read what's currently in their Tone Temp.
        if (part >= MULTI_PART_1)
            return requestCurrentDump();
        // Internal/Card tones: read directly from Tone Memory (reliable).
        if (tempModel.get("bank") == 2)
            return buildMemoryToneRequest(tempModel.get("number"));
        // Preset A/B/Rhythm tones: read from Tone Temp after changePatch() loaded it.
        return requestCurrentDump();
        }

    // RQ1 for a specific Internal/Card tone slot from Tone Memory (08 BB 00, 246 bytes).
    private byte[] buildMemoryToneRequest(int number)
        {
        byte AA  = (byte)0x08;
        byte BB  = (byte)(number * 2);      // Tone Memory stride = 256 bytes = 2 × 128
        byte CC  = (byte)0x00;
        byte LSB = (byte)118;               // 246 = 1×128 + 118 = [01 76] in 7-bit
        byte MSB = (byte)1;
        byte checksum = produceChecksum(new byte[] { AA, BB, CC, (byte)0x00, LSB, MSB });
        byte[] msg = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x16, (byte)0x11,
            AA, BB, CC, (byte)0x00, MSB, LSB, checksum, (byte)0xF7 };
        System.out.println("GR-50: Requesting Internal tone " + (number + 1) +
            " from memory addr=08 " + String.format("%02X 00", BB & 0xFF));
        return msg;
        }

    public byte[] requestCurrentDump()
        {
        byte AA = (byte)(0x04);
        int loc = part * TEMP_TONE_LENGTH;
        byte BB = (byte)((loc >>> 7) & 127);
        byte CC = (byte)(loc & 127);

        byte LSB = (byte)118;       // 0x76 — 246 bytes in 7-bit: 1×128 + 118
        byte MSB = (byte)1;

        byte checksum = produceChecksum(new byte[] { AA, BB, CC, (byte)0x00, LSB, MSB });
        byte[] msg = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x16, (byte)0x11,
            AA, BB, CC, (byte)0x00, MSB, LSB, checksum, (byte)0xF7 };
        String partLabel = (part == MULTI_PART_1) ? "Multi-Timbre Part 1"
                         : (part == MULTI_PART_2) ? "Multi-Timbre Part 2"
                         : ("Guitar P" + (part + 1));
        System.out.println("GR-50: RQ1 Tone Temp " + partLabel +
            " addr=04 " + String.format("%02X %02X", BB & 0xFF, CC & 0xFF));
        return msg;
        }


    public static final int MAXIMUM_NAME_LENGTH = 10;

    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);
        if (name.length() > MAXIMUM_NAME_LENGTH)
            name = name.substring(0, MAXIMUM_NAME_LENGTH);
        StringBuffer nameb = new StringBuffer(name);
        for (int i = 0; i < nameb.length(); i++)
            {
            char c = nameb.charAt(i);
            if (c < 32 || c > 127) nameb.setCharAt(i, ' ');
            }
        return super.revisePatchName(nameb.toString());
        }

    public void revise()
        {
        super.revise();
        String nm = model.get("name", "Init");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm)) model.set("name", newnm);
        }

    public static String getSynthName() { return "Roland GR-50 [Tone]"; }
    public String getPatchName(Model model) { return model.get("name", "Untitled  "); }

    public int getPauseAfterSendAllParameters() { return 100; }
    public int getPauseAfterSendOneParameter() { return 25; }
    // Give the GR-50 time to reload the Tone Temp area after a Timbre change
    // 500ms: gives GR-50 time to load a Preset tone into Tone Temp after a Timbre write.
    // This delay only matters for Preset A/B/Rhythm tones (bank 0/1/3); Internal/Card
    // tones (bank 2) are requested directly from Tone Memory and don't use this path.
    public int getPauseAfterChangePatch() { return 500; }

    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        int bank   = model.get("bank");
        number++;
        if (number >= 64)
            {
            number = 0;
            bank++;
            if (bank > 3) bank = 0;
            }
        Model newModel = buildModel();
        newModel.set("number", number);
        newModel.set("bank", bank);
        return newModel;
        }

    public String getPatchLocationName(Model model)
        {
        if (!model.exists("number")) return null;
        return TONE_GROUP_SHORT[model.get("bank")] +
            (model.get("number") + 1 < 10 ? "0" : "") + (model.get("number") + 1);
        }

    public int getBatchDownloadWaitTime() { return 275; }

    public String[] getPatchNumberNames() { return buildIntegerNames(64, 1); }
    public String[] getBankNames() { return TONE_GROUP; }
    public boolean[] getWriteableBanks() { return new boolean[] { false, false, true, false }; }
    public boolean getSupportsPatchWrites() { return true; }
    public int getPatchNameLength() { return MAXIMUM_NAME_LENGTH; }
    public boolean getPatchContainsLocation() { return true; }
    public boolean librarianTested() { return false; }
    }
