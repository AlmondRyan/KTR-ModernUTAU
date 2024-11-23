package org.rakyuustudio.pianoroll.marker;

public class SignatureChange {
    private int numerator;
    private int denominator;
    private int position;
    
    public SignatureChange(int numerator, int denominator, int position) {
        this.numerator = numerator;
        this.denominator = denominator;
        this.position = position;
    }
    
    public int getNumerator() { return numerator; }
    public int getDenominator() { return denominator; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
} 