package org.rakyuustudio.pianoroll.marker;

public class Marker {
    private String name;
    private int position;  // 在时间轴上的位置
    
    public Marker(String name, int position) {
        this.name = name;
        this.position = position;
    }
    
    public String getName() { return name; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
} 