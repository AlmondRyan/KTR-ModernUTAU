package org.rakyuustudio.pianoroll.marker;

public class TempoChange {
    private double tempo;
    private int position;
    
    public TempoChange(double tempo, int position) {
        this.tempo = tempo;
        this.position = position;
    }
    
    public double getTempo() { return tempo; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
} 