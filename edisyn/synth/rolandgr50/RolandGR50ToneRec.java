/***
    Copyright 2017-2026 by Sean Luke and contributors
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandgr50;

import edisyn.*;

/**
   A recognizer for Roland GR-50 Tone SysEx dumps.

   <p>The GR-50 Tone SysEx uses the same format as the D-110:
   <ul>
   <li>Roland manufacturer 0x41
   <li>Model ID 0x16
   <li>DT1 command 0x12
   <li>Address AA = 0x04 (Tone Temp) — 246 data bytes, total = 256 bytes
   <li>Address AA = 0x08 (Tone Memory) — we request 246 data bytes, total = 256 bytes
   </ul>
   <p>Both Tone Temp and Tone Memory responses are 256 bytes total.  The Tone Memory
   stride in the address map is 256 bytes per slot, but we only request 246 bytes
   (the actual tone parameter size); the GR-50 responds with exactly 246 bytes of data.
*/
public class RolandGR50ToneRec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        if (data.length < 10) return false;
        if (data[0] != (byte)0xF0) return false;
        if (data[1] != (byte)0x41) return false;
        if (data[3] != (byte)0x16) return false;
        if (data[4] != (byte)0x12) return false;

        int AA = data[5] & 0xFF;
        // Both Tone Temp (AA=04) and Tone Memory (AA=08) responses are 246 data bytes
        // + 10 bytes overhead = 256 bytes total.
        boolean result = ((AA == 0x04 || AA == 0x08) &&
            data.length == RolandGR50Tone.TEMP_TONE_LENGTH + 10);

        System.out.println("GR-50 Rec: Roland SysEx arrived len=" + data.length +
            " AA=" + String.format("%02X", AA) + " -> recognize=" + result +
            " (expected " + (RolandGR50Tone.TEMP_TONE_LENGTH + 10) + " bytes)");
        return result;
        }
    }
