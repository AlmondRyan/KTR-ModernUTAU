package org.rakyuustudio.pianoroll;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.rakyuustudio.pianoroll.marker.*;
import java.util.*;

public class GridHeader extends Canvas {
    public static final int HEADER_HEIGHT = 60;
    private static final Color BAR_LINE_COLOR = Color.rgb(100, 100, 100, 0.8);
    
    private final int beatsPerBar;
    private final int pixelsPerBeat;
    private double horizontalZoom;
    private int totalBars;
    
    private List<Marker> markers = new ArrayList<>();
    private List<TempoChange> tempoChanges = new ArrayList<>();
    private List<SignatureChange> signatureChanges = new ArrayList<>();
    
    public GridHeader(int beatsPerBar, int pixelsPerBeat, double horizontalZoom, int totalBars, double width) {
        super(width, HEADER_HEIGHT);
        this.beatsPerBar = beatsPerBar;
        this.pixelsPerBeat = pixelsPerBeat;
        this.horizontalZoom = horizontalZoom;
        this.totalBars = totalBars;
        
        setOnMouseClicked(this::handleMouseClick);
        draw();
    }
    
    public void addMarker(Marker marker) {
        markers.add(marker);
        draw();
    }
    
    public void addTempoChange(TempoChange tempoChange) {
        tempoChanges.add(tempoChange);
        draw();
    }
    
    public void addSignatureChange(SignatureChange signatureChange) {
        signatureChanges.add(signatureChange);
        draw();
    }
    
    private void handleMouseClick(javafx.scene.input.MouseEvent e) {
        if (e.isControlDown()) {
            int position = (int)(e.getX() / horizontalZoom);
            String name = "Marker " + (markers.size() + 1);
            addMarker(new Marker(name, position));
        } else if (e.isAltDown()) {
            int position = (int)(e.getX() / horizontalZoom);
            addTempoChange(new TempoChange(120.0, position));
        } else if (e.isShiftDown()) {
            int position = (int)(e.getX() / horizontalZoom);
            addSignatureChange(new SignatureChange(4, 4, position));
        }
    }
    
    public void updateZoom(double newHorizontalZoom) {
        this.horizontalZoom = newHorizontalZoom;
        draw();
    }
    
    public void updateTotalBars(int newTotalBars) {
        this.totalBars = newTotalBars;
        draw();
    }
    
    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        
        gc.setFill(Color.rgb(240, 240, 240));
        gc.fillRect(0, 0, getWidth(), getHeight());
        
        gc.setTextAlign(TextAlignment.LEFT);
        for (int bar = 0; bar <= totalBars; bar++) {
            double x = bar * beatsPerBar * pixelsPerBeat * horizontalZoom;
            
            gc.setStroke(BAR_LINE_COLOR);
            gc.setLineWidth(1);
            gc.strokeLine(x, HEADER_HEIGHT - 20, x, HEADER_HEIGHT);
            
            gc.setFill(Color.BLACK);
            gc.fillText(String.valueOf(bar + 1), x + 5, HEADER_HEIGHT - 25);
        }
        
        gc.setFill(Color.BLUE);
        for (Marker marker : markers) {
            double x = marker.getPosition() * horizontalZoom;
            gc.fillText(marker.getName(), x, 15);
            gc.strokeLine(x, 0, x, 20);
        }
        
        gc.setFill(Color.RED);
        for (TempoChange tempo : tempoChanges) {
            double x = tempo.getPosition() * horizontalZoom;
            gc.fillText(tempo.getTempo() + " BPM", x, 30);
            gc.strokeLine(x, 20, x, 35);
        }
        
        gc.setFill(Color.GREEN);
        for (SignatureChange sig : signatureChanges) {
            double x = sig.getPosition() * horizontalZoom;
            gc.fillText(sig.getNumerator() + "/" + sig.getDenominator(), x, 45);
            gc.strokeLine(x, 35, x, 50);
        }
    }
    
    public static int getHeaderHeight() {
        return HEADER_HEIGHT;
    }
} 