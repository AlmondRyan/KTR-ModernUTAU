package org.rakyuustudio.pianoroll;

public class Note {
    public int pitch;
    public int startTime;
    int duration;
    String lyric;
    boolean isMuted;
    
    public Note(int pitch, int startTime, int duration) {
        this.pitch = pitch;
        this.startTime = startTime;
        this.duration = duration;
        this.lyric = "a";
        this.isMuted = false;
    }
    
    // 用于复制Note的构造函数
    public Note(Note other) {
        this.pitch = other.pitch;
        this.startTime = other.startTime;
        this.duration = other.duration;
        this.lyric = other.lyric;
        this.isMuted = other.isMuted;
    }
} 