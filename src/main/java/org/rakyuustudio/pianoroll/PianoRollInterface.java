package org.rakyuustudio.pianoroll;

import java.util.List;

public interface PianoRollInterface {
    // 基本音符操作
    void addNote(int pitch, int startTime, int duration);
    void removeNote(Note note);
    void clearNotes();
    List<Note> getNotes();
    void setNotes(List<Note> notes);
    
    // 视图控制
    void setZoom(double horizontalZoom, double verticalZoom);
    void setSnapToGrid(boolean snap);
    void setGridDivision(int division);
    double getHorizontalZoom();
    double getVerticalZoom();
    
    // 编辑操作
    void applyLegato();
    void quantizeSelectedNotes();
    void deleteSelectedNotes();
    
    // 选择操作
    List<Note> getSelectedNotes();
    void selectNote(Note note);
    void selectNotes(List<Note> notes);
    void clearSelection();
    
    // 歌词编辑
    void setNoteLyric(Note note, String lyric);
    String getNoteLyric(Note note);
    
    // 视图更新
    void refresh();
} 