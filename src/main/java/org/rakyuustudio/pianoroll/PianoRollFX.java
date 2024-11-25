package org.rakyuustudio.pianoroll;

import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.geometry.Orientation;
import javafx.collections.FXCollections;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import static org.rakyuustudio.pianoroll.GridHeader.HEADER_HEIGHT;

import org.rakyuustudio.pianoroll.command.AddNoteCommand;
import org.rakyuustudio.pianoroll.command.Command;
import org.rakyuustudio.pianoroll.command.DeleteNoteCommand;
import org.rakyuustudio.pianoroll.command.MoveNoteCommand;

public class PianoRollFX extends VBox implements PianoRollInterface {
    private static final int KEY_WIDTH = 60;
    private static final int KEY_HEIGHT = 20;
    private static final int MIN_OCTAVE = -1; // C-1
    private static final int MAX_OCTAVE = 10; // C10
    private static final int TOTAL_KEYS = (MAX_OCTAVE - MIN_OCTAVE + 1) * 12;
    private static final int PIXELS_PER_BEAT = 50;
    private static final int BEATS_PER_BAR = 4;
    private int gridSize = PIXELS_PER_BEAT; // 默认值
    private static final int INITIAL_BARS = 32; // 初始小节数
    private static final double NOTE_CORNER_RADIUS = 5.0;
    private static final double LABEL_HIDE_ZOOM_THRESHOLD = 0.5; // 低于此缩放比例时隐藏标签
    private static final Integer[] GRID_DIVISIONS = {4, 8, 16, 32}; // 网格细分等级
    private int currentGridDivision = 4;
    private static final String[] NOTE_NAMES = {  // 添加这个常量
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };
    
    private Canvas pianoCanvas;
    private Canvas gridCanvas;
    private List<Note> notes;
    private Set<Note> selectedNotes;
    private Note resizingNote;
    private double dragStartX;
    private double dragStartY;
    private boolean isDragging = false;
    private boolean isResizing = false;
    private double horizontalZoom = 1.0;
    private double verticalZoom = 1.0;
    private ScrollPane scrollPane;
    private Pane gridPane;
    private double selectionStartX;
    private double selectionStartY;
    private boolean isSelecting = false;
    private boolean snapToGrid = true;
    private int totalBars = INITIAL_BARS; // 当前总小节数
    private GridHeader gridHeader;
    private String defaultLyric = "a";
    private Rectangle selectionRect; // 添加选择框矩形字段

    // 添加最大尺寸限制
    private static final double MAX_CANVAS_WIDTH = 16384;  // JavaFX Canvas的最大宽度
    private static final double MAX_CANVAS_HEIGHT = 16384; // JavaFX Canvas的最大高度
    private static final double MAX_ZOOM = 10.0;          // 最大缩放倍数

    private final Stack<Command> undoStack = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();
    private ScrollPane gridScrollPane;
    
    public PianoRollFX() {
        notes = new ArrayList<>();
        selectedNotes = new HashSet<>();
        
        HBox toolbar = createToolbar();
        
        // 创建主要内容区域
        VBox mainContent = new VBox(0);
        
        // 创建网格头部
        gridHeader = new GridHeader(BEATS_PER_BAR, PIXELS_PER_BEAT, horizontalZoom, totalBars, 2000);
        
        // 创建内容区域的容器
        HBox contentArea = new HBox(0);
        
        // 创建钢琴键画布和容器
        pianoCanvas = new Canvas(KEY_WIDTH, TOTAL_KEYS * KEY_HEIGHT);
        Pane pianoContainer = new Pane(pianoCanvas);
        pianoContainer.setMinWidth(KEY_WIDTH);
        pianoContainer.setPrefWidth(KEY_WIDTH);
        pianoContainer.setMaxWidth(KEY_WIDTH);
        
        // 创建网格画布和容器
        gridCanvas = new Canvas(2000, TOTAL_KEYS * KEY_HEIGHT);
        gridPane = new Pane(gridCanvas);
        
        // 创建滚动视图
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        
        // 创建包含钢琴键和网格的容器
        HBox scrollContent = new HBox(0);
        scrollContent.getChildren().addAll(pianoContainer, gridPane);
        scrollPane.setContent(scrollContent);
        
        // 在GridHeader下方添加一个HBox来包含钢琴键区域的空白和实际的GridHeader
        HBox headerArea = new HBox(0);
        Region headerSpacer = new Region();
        headerSpacer.setMinWidth(KEY_WIDTH);
        headerSpacer.setPrefWidth(KEY_WIDTH);
        headerSpacer.setMaxWidth(KEY_WIDTH);
        headerArea.getChildren().addAll(headerSpacer, gridHeader);
        
        // 组装布局
        mainContent.getChildren().addAll(headerArea, scrollPane);
        
        // 设置增长属性
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        HBox.setHgrow(gridPane, Priority.ALWAYS);
        
        getChildren().addAll(toolbar, mainContent);
        
        // 设置事件处理和初始化
        setupEventHandlers();
        setupKeyboardShortcuts();
        updateCanvasSize();
        drawPianoKeys();
        drawGrid();
        
        // 监听滚动变化
        scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            // 计算实际的滚动位置
            double scrollableHeight = gridCanvas.getHeight() - scrollPane.getViewportBounds().getHeight();
            double scrollY = newVal.doubleValue() * scrollableHeight;
            
            // 更新钢琴键的位置
            pianoCanvas.setTranslateY(-scrollY);
        });
        
        // 监听视口大小变化
        scrollPane.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            if (newBounds != null) {
                updateCanvasSize();
                drawGrid();
                drawPianoKeys();
            }
        });
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(5);
        toolbar.setPadding(new Insets(5));
        
        // 修改网格细分选择的实现
        ComboBox<Integer> gridDivisionCombo = new ComboBox<>(
            FXCollections.observableArrayList(GRID_DIVISIONS)
        );
        gridDivisionCombo.setValue(currentGridDivision);
        gridDivisionCombo.setTooltip(new Tooltip("网格细分等级"));
        gridDivisionCombo.setOnAction(e -> {
            currentGridDivision = gridDivisionCombo.getValue();
            gridSize = PIXELS_PER_BEAT / currentGridDivision;
            drawGrid();
        });
        
        // 吸附开关
        ToggleButton snapButton = new ToggleButton("吸附");
        snapButton.setSelected(snapToGrid);
        snapButton.setTooltip(new Tooltip("开启/关闭网格吸附"));
        snapButton.setOnAction(e -> snapToGrid = snapButton.isSelected());
        
        // 连音按钮
        Button legatoButton = new Button("连音");
        legatoButton.setTooltip(new Tooltip("将选中的音符连接起来"));
        legatoButton.setOnAction(e -> applyLegato());
        
        // 量化按钮
        Button quantizeButton = new Button("量化");
        quantizeButton.setTooltip(new Tooltip("将选中的音符对齐到网格"));
        quantizeButton.setOnAction(e -> quantizeSelectedNotes());
        
        // 编辑歌词按钮
        Button editLyricButton = new Button("编辑歌词");
        editLyricButton.setTooltip(new Tooltip("编辑选中音符的歌词"));
        editLyricButton.setOnAction(e -> {
            if (!selectedNotes.isEmpty()) {
                showLyricDialog(selectedNotes.iterator().next());
            }
        });
        
        toolbar.getChildren().addAll(
            new Label("细分:"), gridDivisionCombo,
            new Separator(Orientation.VERTICAL),
            snapButton,
            new Separator(Orientation.VERTICAL),
            legatoButton, quantizeButton, editLyricButton
        );
        
        return toolbar;
    }

    private void setupEventHandlers() {
        gridCanvas.setOnMousePressed(this::handleMousePressed);
        gridCanvas.setOnMouseDragged(this::handleMouseDragged);
        gridCanvas.setOnMouseReleased(this::handleMouseReleased);
        gridCanvas.setOnMouseClicked(this::handleMouseClicked);
        
        // 添加键盘事件处理
        gridCanvas.setFocusTraversable(true);
        gridCanvas.setOnKeyPressed(this::handleKeyPressed);
        
        // 添加滚轮缩放事件
        gridCanvas.setOnScroll(e -> {
            if (e.isControlDown()) {
                try {
                    if (e.isShiftDown()) {
                        // 垂直缩放
                        double factor = e.getDeltaY() > 0 ? 1.05 : 0.95;
                        double newVerticalZoom = verticalZoom * factor;
                        
                        // 检查新的缩放值是否在允许范围内
                        if (newVerticalZoom <= MAX_ZOOM && newVerticalZoom >= 0.1) {
                            setZoom(horizontalZoom, newVerticalZoom);
                        }
                    } else {
                        // 水平缩放
                        double factor = e.getDeltaY() > 0 ? 1.05 : 0.95;
                        double newHorizontalZoom = horizontalZoom * factor;
                        
                        // 检查新的缩放值是否在允许范围内
                        if (newHorizontalZoom <= MAX_ZOOM && newHorizontalZoom >= 0.1) {
                            setZoom(newHorizontalZoom, verticalZoom);
                        }
                    }
                    e.consume();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // 添加选择框事件处理
        gridCanvas.setOnMousePressed(e -> {
            if (e.isControlDown()) {
                selectionStartX = e.getX();
                selectionStartY = e.getY();
                
                selectionRect = new Rectangle();
                selectionRect.setStroke(Color.BLUE);
                selectionRect.setFill(Color.TRANSPARENT);
                selectionRect.getStrokeDashArray().addAll(5.0, 5.0);
                selectionRect.setX(selectionStartX);
                selectionRect.setY(selectionStartY);
                
                gridPane.getChildren().add(selectionRect);
                
                if (!e.isShiftDown()) {
                    selectedNotes.clear();
                }
                
                isSelecting = true;
                e.consume();
            }
        });

        gridCanvas.setOnMouseDragged(e -> {
            if (isSelecting && selectionRect != null) {
                double currentX = e.getX();
                double currentY = e.getY();
                
                double x = Math.min(selectionStartX, currentX);
                double y = Math.min(selectionStartY, currentY);
                double width = Math.abs(currentX - selectionStartX);
                double height = Math.abs(currentY - selectionStartY);
                
                selectionRect.setX(x);
                selectionRect.setY(y);
                selectionRect.setWidth(width);
                selectionRect.setHeight(height);
                
                // 更新选中的音符
                updateSelectedNotes(x, y, width, height);
                e.consume();
            }
        });

        gridCanvas.setOnMouseReleased(e -> {
            if (isSelecting) {
                if (selectionRect != null) {
                    gridPane.getChildren().remove(selectionRect);
                    selectionRect = null;
                }
                isSelecting = false;
                e.consume();
            }
        });
    }

    private void handleKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.DELETE) {
            notes.removeAll(selectedNotes);
            selectedNotes.clear();
            drawGrid();
        }
    }

    private void handleMousePressed(MouseEvent e) {
        if (e == null) return;
        
        dragStartX = e.getX();
        dragStartY = e.getY();
        
        Note clickedNote = findNoteAt(e.getX(), e.getY());
        
        if (e.isControlDown()) {
            // Ctrl+点击开始框选
            isSelecting = true;
            selectionStartX = e.getX();
            selectionStartY = e.getY();
            if (!e.isShiftDown()) {
                selectedNotes.clear();
            }
        } else {
            // 普通点击
            if (clickedNote != null) {
                if (!selectedNotes.contains(clickedNote)) {
                    if (!e.isShiftDown()) {
                        selectedNotes.clear();
                    }
                    selectedNotes.add(clickedNote);
                }
                isDragging = true;
            } else {
                selectedNotes.clear();
            }
        }
        
        drawGrid();
    }

    private void handleControlClick(MouseEvent e, Note clickedNote) {
        isSelecting = true;
        selectionStartX = e.getX();
        selectionStartY = e.getY();
        if (!e.isShiftDown() && selectedNotes != null) {
            selectedNotes.clear();
        }
    }

    private void handleNormalClick(MouseEvent e, Note clickedNote) {
        if (clickedNote != null && selectedNotes != null) {
            if (!selectedNotes.contains(clickedNote)) {
                if (!e.isShiftDown()) {
                    selectedNotes.clear();
                }
                selectedNotes.add(clickedNote);
            }
            isDragging = true;
        } else if (selectedNotes != null) {
            selectedNotes.clear();
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        if (isDragging && !selectedNotes.isEmpty()) {
            double deltaX = e.getX() - dragStartX;
            double deltaY = e.getY() - dragStartY;
            
            if (snapToGrid) {
                deltaX = Math.round(deltaX / (gridSize * horizontalZoom)) * (gridSize * horizontalZoom);
            }
            
            for (Note note : selectedNotes) {
                int newStartTime = note.startTime + (int)(deltaX / horizontalZoom);
                int newPitch = TOTAL_KEYS - 1 - (int)((note.pitch * KEY_HEIGHT * verticalZoom + deltaY) / (KEY_HEIGHT * verticalZoom));
                
                executeCommand(new MoveNoteCommand(note, newPitch, newStartTime));
            }
            
            dragStartX = e.getX();
            dragStartY = e.getY();
        }
        
        drawGrid();
    }

    private void handleMouseReleased(MouseEvent e) {
        if (isSelecting) {
            // 处理区域选择
            double selectionEndX = e.getX();
            double selectionEndY = e.getY();
            
            double left = Math.min(selectionStartX, selectionEndX);
            double right = Math.max(selectionStartX, selectionEndX);
            double top = Math.min(selectionStartY, selectionEndY);
            double bottom = Math.max(selectionStartY, selectionEndY);
            
            for (Note note : notes) {
                if (note.startTime >= left && note.startTime <= right &&
                    note.pitch * KEY_HEIGHT >= top && note.pitch * KEY_HEIGHT <= bottom) {
                    selectedNotes.add(note);
                }
            }
        }
        
        isDragging = false;
        isResizing = false;
        isSelecting = false;
        resizingNote = null;
        drawGrid();
    }

    private void handleMouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1 && !isDragging) {
            Note clickedNote = findNoteAt(e.getX(), e.getY());
            if (clickedNote == null) {
                int pitch = TOTAL_KEYS - 1 - (int)(e.getY() / (KEY_HEIGHT * verticalZoom));
                int startTime = (int)(e.getX() / horizontalZoom);
                
                if (snapToGrid) {
                    startTime = (int)(Math.round(startTime / gridSize) * gridSize);
                }
                
                Note newNote = new Note(pitch, startTime, gridSize);
                executeCommand(new AddNoteCommand(notes, newNote));
            }
        }
    }

    private void handlePlayButton() {
        // TODO: 实现MIDI播放功能
    }

    @Override
    public void applyLegato() {
        if (selectedNotes.size() < 2) return;
        
        List<Note> sortedNotes = new ArrayList<>(selectedNotes);
        sortedNotes.sort((a, b) -> Integer.compare(a.startTime, b.startTime));
        
        for (int i = 0; i < sortedNotes.size() - 1; i++) {
            Note currentNote = sortedNotes.get(i);
            Note nextNote = sortedNotes.get(i + 1);
            currentNote.duration = nextNote.startTime - currentNote.startTime;
        }
        
        drawGrid();
    }

    protected void drawGrid() {
        updateCanvasSize();
        
        GraphicsContext gc = gridCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());
        
        // 绘制水平网格线
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        for (double y = 0; y < gridCanvas.getHeight(); y += KEY_HEIGHT * verticalZoom) {
            gc.strokeLine(0, y, gridCanvas.getWidth(), y);
        }
        
        // 绘制垂直网格线，包括细分
        for (int bar = 0; bar < totalBars; bar++) {
            double barStartX = bar * BEATS_PER_BAR * PIXELS_PER_BEAT * horizontalZoom;
            
            // 绘制每个小节内的拍子线和细分线
            for (int beat = 0; beat < BEATS_PER_BAR; beat++) {
                double beatX = barStartX + beat * PIXELS_PER_BEAT * horizontalZoom;
                
                // 绘制主拍线
                gc.setStroke(Color.GRAY);
                gc.setLineWidth(1);
                gc.strokeLine(beatX, 0, beatX, gridCanvas.getHeight());
                
                // 绘制细分线
                gc.setStroke(Color.LIGHTGRAY);
                gc.setLineWidth(0.5);
                for (int div = 1; div < currentGridDivision; div++) {
                    double divX = beatX + (PIXELS_PER_BEAT * horizontalZoom * div) / currentGridDivision;
                    gc.strokeLine(divX, 0, divX, gridCanvas.getHeight());
                }
            }
        }
        
        // 绘制音符
        for (Note note : notes) {
            double x = note.startTime * horizontalZoom;
            double y = (TOTAL_KEYS - 1 - note.pitch) * KEY_HEIGHT * verticalZoom;
            
            if (selectedNotes.contains(note)) {
                gc.setFill(note.isMuted ? Color.GRAY : Color.ORANGE);
                gc.setStroke(note.isMuted ? Color.DARKGRAY : Color.DARKORANGE);
            } else {
                gc.setFill(note.isMuted ? Color.LIGHTGRAY : Color.CORNFLOWERBLUE);
                gc.setStroke(note.isMuted ? Color.GRAY : Color.BLUE);
            }
            
            gc.fillRoundRect(x, y, note.duration * horizontalZoom, 
                           KEY_HEIGHT * verticalZoom, 
                           NOTE_CORNER_RADIUS, NOTE_CORNER_RADIUS);
            gc.strokeRoundRect(x, y, note.duration * horizontalZoom, 
                             KEY_HEIGHT * verticalZoom, 
                             NOTE_CORNER_RADIUS, NOTE_CORNER_RADIUS);
            
            // 绘制音符标签
            if (horizontalZoom >= LABEL_HIDE_ZOOM_THRESHOLD) {
                gc.setFill(Color.BLACK);
                if (note.lyric != null) {
                    gc.fillText(note.lyric, x + 5, y + KEY_HEIGHT * verticalZoom - 5);
                }
                String noteName = getNoteNameForIndex(note.pitch);
                gc.fillText(noteName, x + note.duration * horizontalZoom - 30, 
                          y + KEY_HEIGHT * verticalZoom - 5);
            }
        }
        
        // 制选择框（使用FX的效果）
        if (isSelecting) {
            double width = dragStartX - selectionStartX;
            double height = dragStartY - selectionStartY;
            
            gc.setStroke(Color.rgb(0, 0, 255, 0.5));
            gc.setLineDashes(5);
            gc.setLineWidth(1);
            gc.strokeRect(
                Math.min(selectionStartX, dragStartX),
                Math.min(selectionStartY, dragStartY),
                Math.abs(width),
                Math.abs(height)
            );
            gc.setLineDashes(null);
        }
        
        // 更新网格头部
        gridHeader.draw();
    }

    private void updateCanvasSize() {
        try {
            double totalWidth = totalBars * BEATS_PER_BAR * PIXELS_PER_BEAT * horizontalZoom;
            double totalHeight = TOTAL_KEYS * KEY_HEIGHT * verticalZoom;
            
            // 限制最大尺寸
            totalWidth = Math.min(totalWidth, MAX_CANVAS_WIDTH);
            totalHeight = Math.min(totalHeight, MAX_CANVAS_HEIGHT);
            
            // 更新网格画布大小
            if (gridCanvas != null) {
                gridCanvas.setWidth(Math.max(1000, totalWidth));
                gridCanvas.setHeight(totalHeight);
            }
            
            // 更新钢琴键画布大小
            if (pianoCanvas != null) {
                pianoCanvas.setHeight(totalHeight);
            }
            
            // 更新网格头部大小
            if (gridHeader != null) {
                gridHeader.setWidth(Math.max(1000, totalWidth));
            }
            
            // 确保gridPane的首选大小正确
            if (gridPane != null) {
                gridPane.setPrefWidth(Math.max(1000, totalWidth));
                gridPane.setPrefHeight(totalHeight);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void zoom(double factor) {
        horizontalZoom *= factor;
        verticalZoom *= factor;
        drawGrid();
    }

    @Override
    public void clearNotes() {
        notes.clear();
        selectedNotes.clear();
        drawGrid();
    }

    private void addNote(int pitch, int startTime) {
        notes.add(new Note(pitch, startTime, (int)(100 * horizontalZoom)));
    }

    private boolean isBlackKey(int index) {
        int noteIndex = index % 12;
        return noteIndex == 1 || noteIndex == 3 || noteIndex == 6 || noteIndex == 8 || noteIndex == 10;
    }

    private String getNoteNameForIndex(int index) {
        int noteIndex = index % 12;
        int octave = MIN_OCTAVE + (index / 12);
        return NOTE_NAMES[noteIndex] + octave;
    }

    private Note findNoteAt(double x, double y) {
        if (notes == null) return null;
        
        try {
            int pitch = TOTAL_KEYS - 1 - (int)(y / (KEY_HEIGHT * verticalZoom));
            double time = x / horizontalZoom;
            
            // 使用更精确的碰撞检测
            return notes.stream()
                .filter(n -> n != null &&
                    pitch == n.pitch &&
                    time >= n.startTime &&
                    time <= n.startTime + n.duration)
                .min((n1, n2) -> {
                    // 如果多个音符重叠，选择最接近点击位置的音符
                    double d1 = Math.abs(time - (n1.startTime + n1.duration / 2.0));
                    double d2 = Math.abs(time - (n2.startTime + n2.duration / 2.0));
                    return Double.compare(d1, d2);
                })
                .orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void drawPianoKeys() {
        if (pianoCanvas == null) return;
        
        GraphicsContext gc = pianoCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, pianoCanvas.getWidth(), pianoCanvas.getHeight());
        
        for (int i = 0; i < TOTAL_KEYS; i++) {
            double y = i * KEY_HEIGHT * verticalZoom;
            boolean isBlack = isBlackKey(TOTAL_KEYS - 1 - i);
            
            gc.setFill(isBlack ? Color.BLACK : Color.WHITE);
            gc.setStroke(Color.BLACK);
            gc.fillRect(0, y, KEY_WIDTH, KEY_HEIGHT * verticalZoom);
            gc.strokeRect(0, y, KEY_WIDTH, KEY_HEIGHT * verticalZoom);
            
            gc.setFill(isBlack ? Color.WHITE : Color.BLACK);
            String noteName = getNoteNameForIndex(TOTAL_KEYS - 1 - i);
            gc.fillText(noteName, 5, y + KEY_HEIGHT * verticalZoom - 5);
        }
    }

    // 添加歌词编辑对话框
    private void showLyricDialog(Note note) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("编辑歌词");
        dialog.setHeaderText("输入歌词（支持罗马音自动转换）");

        // 创建对话框内容
        VBox content = new VBox(10);
        TextField lyricField = new TextField(note.lyric);
        ListView<String> candidateList = new ListView<>();
        
        // 添加输入监听器
        lyricField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && !newText.isEmpty()) {
                // 获取罗马音转换结果
                List<String> candidates = convertRomajiToKana(newText);
                candidateList.getItems().setAll(candidates);
            } else {
                candidateList.getItems().clear();
            }
        });
        
        // 双击候选项时选择
        candidateList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = candidateList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    lyricField.setText(selected);
                }
            }
        });
        
        content.getChildren().addAll(
            new Label("歌词:"),
            lyricField,
            new Label("候选:"),
            candidateList
        );
        
        dialog.getDialogPane().setContent(content);
        
        // 添加按钮
        ButtonType confirmButtonType = new ButtonType("确定", ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, cancelButtonType);
        
        // 设置结果转换器
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return lyricField.getText();
            }
            return null;
        });
        
        // 显示对话框并处理结果
        dialog.showAndWait().ifPresent(result -> {
            note.lyric = result;
            drawGrid();
        });
    }

    // 罗马音转换方法（这里需要添加依赖或实现转换逻辑）
    private List<String> convertRomajiToKana(String romaji) {
        return RomajiConverter.convert(romaji);
    }

    // 实现接口方法
    @Override
    public void addNote(int pitch, int startTime, int duration) {
        notes.add(new Note(pitch, startTime, duration));
        drawGrid();
    }
    
    @Override
    public void removeNote(Note note) {
        notes.remove(note);
        selectedNotes.remove(note);
        drawGrid();
    }
    
    @Override
    public List<Note> getNotes() {
        return new ArrayList<>(notes);
    }
    
    @Override
    public void setNotes(List<Note> newNotes) {
        this.notes = new ArrayList<>(newNotes);
        selectedNotes.clear();
        drawGrid();
    }
    
    @Override
    public void setZoom(double newHorizontalZoom, double newVerticalZoom) {
        // 限制缩放范围
        this.horizontalZoom = Math.min(Math.max(0.1, newHorizontalZoom), MAX_ZOOM);
        this.verticalZoom = Math.min(Math.max(0.1, newVerticalZoom), MAX_ZOOM);
        
        // 检查缩放后的画布尺寸是否超出限制
        double totalWidth = totalBars * BEATS_PER_BAR * PIXELS_PER_BEAT * horizontalZoom;
        double totalHeight = TOTAL_KEYS * KEY_HEIGHT * verticalZoom;
        
        if (totalWidth > MAX_CANVAS_WIDTH || totalHeight > MAX_CANVAS_HEIGHT) {
            // 如果超出限制，调整缩放比例
            if (totalWidth > MAX_CANVAS_WIDTH) {
                horizontalZoom = MAX_CANVAS_WIDTH / (totalBars * BEATS_PER_BAR * PIXELS_PER_BEAT);
            }
            if (totalHeight > MAX_CANVAS_HEIGHT) {
                verticalZoom = MAX_CANVAS_HEIGHT / (TOTAL_KEYS * KEY_HEIGHT);
            }
        }
        
        gridHeader.updateZoom(horizontalZoom);
        updateCanvasSize();
        drawGrid();
        drawPianoKeys();
    }
    
    @Override
    public void setSnapToGrid(boolean snap) {
        this.snapToGrid = snap;
    }

    private void checkAndExpandBars(int position) {
        int requiredBars = (position / (BEATS_PER_BAR * PIXELS_PER_BEAT)) + 2;
        if (requiredBars >= totalBars - 1) {
            totalBars += INITIAL_BARS;
            updateCanvasSize();
            gridHeader.updateTotalBars(totalBars);
        }
    }

//    @Override
    public void initialize() {
        // 确保所有组件都被正确初始化
        notes = new ArrayList<>();
        selectedNotes = new HashSet<>();
        
        initializeComponents();
        setupEventHandlers();
        updateCanvasSize();
        drawPianoKeys();
        drawGrid();
    }

    private void initializeComponents() {
        // 初始化所有UI组件
        if (pianoCanvas == null) {
            pianoCanvas = new Canvas(KEY_WIDTH, TOTAL_KEYS * KEY_HEIGHT);
        }
        
        if (gridCanvas == null) {
            gridCanvas = new Canvas(2000, TOTAL_KEYS * KEY_HEIGHT);
        }
        
        if (gridPane == null) {
            gridPane = new Pane(gridCanvas);
        }
        
        if (gridHeader == null) {
            gridHeader = new GridHeader(BEATS_PER_BAR, PIXELS_PER_BEAT, horizontalZoom, totalBars, 2000);
        }
    }

    @Override
    public void refresh() {
        if (gridCanvas != null) {
            drawGrid();
        }
        if (pianoCanvas != null) {
            drawPianoKeys();
        }
        if (gridHeader != null) {
            gridHeader.draw();
        }
    }

    @Override
    public List<Note> getSelectedNotes() {
        return new ArrayList<>(selectedNotes != null ? selectedNotes : new HashSet<>());
    }

    @Override
    public void selectNote(Note note) {
        if (selectedNotes != null && note != null) {
            selectedNotes.add(note);
            refresh();
        }
    }

    @Override
    public void selectNotes(List<Note> notes) {
        if (selectedNotes != null && notes != null) {
            selectedNotes.addAll(notes);
            refresh();
        }
    }

    @Override
    public void clearSelection() {
        if (selectedNotes != null) {
            selectedNotes.clear();
            refresh();
        }
    }

    @Override
    public void setNoteLyric(Note note, String lyric) {
        if (note != null) {
            note.lyric = lyric;
            refresh();
        }
    }

    @Override
    public String getNoteLyric(Note note) {
        return note != null ? note.lyric : "";
    }

    @Override
    public void setGridDivision(int division) {
        this.currentGridDivision = division;
        this.gridSize = PIXELS_PER_BEAT / division;
        drawGrid();
    }

    @Override
    public double getHorizontalZoom() {
        return horizontalZoom;
    }

    @Override
    public double getVerticalZoom() {
        return verticalZoom;
    }

    @Override
    public void deleteSelectedNotes() {
        if (!selectedNotes.isEmpty()) {
            for (Note note : new ArrayList<>(selectedNotes)) {
                executeCommand(new DeleteNoteCommand(notes, note));
            }
            selectedNotes.clear();
        }
    }

    @Override
    public void quantizeSelectedNotes() {
        for (Note note : selectedNotes) {
            note.startTime = Math.round(note.startTime / gridSize) * gridSize;
            note.duration = Math.round(note.duration / gridSize) * gridSize;
        }
        drawGrid();
    }

    private void updateSelectedNotes(double x, double y, double width, double height) {
        try {
            double right = x + width;
            double bottom = y + height;
            
            // 计算实际的音符位置范围
            int startPitch = TOTAL_KEYS - 1 - (int)((bottom) / (KEY_HEIGHT * verticalZoom));
            int endPitch = TOTAL_KEYS - 1 - (int)((y) / (KEY_HEIGHT * verticalZoom));
            int startTime = (int)(x / horizontalZoom);
            int endTime = (int)(right / horizontalZoom);
            
            // 检查每个音符是否在选择范围内
            for (Note note : notes) {
                int noteEndTime = note.startTime + note.duration;
                
                // 检查音符是否与选择区域相交
                if (note.pitch >= Math.min(startPitch, endPitch) && 
                    note.pitch <= Math.max(startPitch, endPitch) &&
                    noteEndTime >= startTime && 
                    note.startTime <= endTime) {
                    selectedNotes.add(note);
                }
            }
            
            drawGrid();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupKeyboardShortcuts() {
        setOnKeyPressed(event -> {
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case Z:
                        if (event.isShiftDown()) {
                            redo();
                        } else {
                            undo();
                        }
                        break;
                    case Y:
                        redo();
                        break;
                    case Q:
                        quantizeSelectedNotes();
                        break;
                    case L:
                        applyLegato();
                        break;
                    case D:
                        duplicateSelectedNotes();
                        break;
                    case M:
                        if (event.isAltDown()) {
                            unmuteSelectedNotes();
                        } else {
                            muteSelectedNotes();
                        }
                        break;
                }
            } else {
                switch (event.getCode()) {
                    case DELETE:
                        deleteSelectedNotes();
                        break;
                    case PAGE_UP:
                        scrollToTop();
                        break;
                    case PAGE_DOWN:
                        scrollToBottom();
                        break;
                    case LEFT:
                        scrollLeft();
                        break;
                    case RIGHT:
                        scrollRight();
                        break;
                    case NUMPAD0:
                        muteSelectedNotes();
                        break;
                    case NUMPAD1:
                        unmuteSelectedNotes();
                        break;
                }
            }
            event.consume();
        });
    }
    
    private void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
            drawGrid();
        }
    }
    
    private void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
            drawGrid();
        }
    }
    
    private void duplicateSelectedNotes() {
        List<Note> newNotes = new ArrayList<>();
        for (Note note : selectedNotes) {
            Note newNote = new Note(note);
            newNote.startTime += note.duration; // 将复制的音符放在原音符后面
            newNotes.add(newNote);
        }
        for (Note note : newNotes) {
            executeCommand(new AddNoteCommand(notes, note));
        }
        selectedNotes.clear();
        selectedNotes.addAll(newNotes);
    }
    
    private void muteSelectedNotes() {
        for (Note note : selectedNotes) {
            note.isMuted = true;
        }
        drawGrid();
    }
    
    private void unmuteSelectedNotes() {
        for (Note note : selectedNotes) {
            note.isMuted = false;
        }
        drawGrid();
    }

    // 添加滚动方法
    private void scrollToTop() {
        if (gridScrollPane != null) {
            gridScrollPane.setVvalue(0);
        }
    }
    
    private void scrollToBottom() {
        if (gridScrollPane != null) {
            gridScrollPane.setVvalue(1);
        }
    }
    
    private void scrollLeft() {
        if (gridScrollPane != null) {
            double newValue = gridScrollPane.getHvalue() - 0.1;
            gridScrollPane.setHvalue(Math.max(0, newValue));
        }
    }
    
    private void scrollRight() {
        if (gridScrollPane != null) {
            double newValue = gridScrollPane.getHvalue() + 0.1;
            gridScrollPane.setHvalue(Math.min(1, newValue));
        }
    }
    
    private void executeCommand(Command command) {
        if (command != null) {
            command.execute();
            undoStack.push(command);
            redoStack.clear();
            drawGrid();
        }
    }
} 