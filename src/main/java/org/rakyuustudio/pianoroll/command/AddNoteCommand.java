package org.rakyuustudio.pianoroll.command;

import org.rakyuustudio.pianoroll.Note;
import java.util.List;

public class AddNoteCommand implements Command {
    private final List<Note> noteList;
    private final Note note;
    
    public AddNoteCommand(List<Note> noteList, Note note) {
        this.noteList = noteList;
        this.note = note;
    }
    
    @Override
    public void execute() {
        noteList.add(note);
    }
    
    @Override
    public void undo() {
        noteList.remove(note);
    }
} 