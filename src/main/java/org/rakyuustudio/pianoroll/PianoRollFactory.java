package org.rakyuustudio.pianoroll;

public class PianoRollFactory {
    public static PianoRollInterface createPianoRoll() {
        return new PianoRollFX();
    }
} 