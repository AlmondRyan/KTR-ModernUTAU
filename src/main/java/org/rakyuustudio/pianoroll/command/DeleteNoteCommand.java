package org.rakyuustudio.pianoroll.command;

import org.rakyuustudio.pianoroll.Note;
import java.util.List;

public class DeleteNoteCommand implements Command {
    private final List<Note> noteList;
    private final Note note;
    
    public DeleteNoteCommand(List<Note> noteList, Note note) {
        this.noteList = noteList;
        this.note = note;
    }
    
    @Override
    public void execute() {
        noteList.remove(note);
    }
    
    @Override
    public void undo() {
        noteList.add(note);
    }
} 