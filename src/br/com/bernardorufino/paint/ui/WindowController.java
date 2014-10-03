package br.com.bernardorufino.paint.ui;

import br.com.bernardorufino.paint.ext.*;
import br.com.bernardorufino.paint.ext.Properties;
import br.com.bernardorufino.paint.figures.PersistableFigure;
import br.com.bernardorufino.paint.grapher.FrameBuffer;
import br.com.bernardorufino.paint.grapher.Grapher;
import br.com.bernardorufino.paint.tools.*;
import br.com.bernardorufino.paint.utils.DrawUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class WindowController extends ContextAwareController implements Initializable {

    public static final String COORD_STATUS = "%d X %d";
    private static final int RESIZE_FACTOR = 2;
    public static final List<PatternChoice> PATTERN_CHOICES = ImmutableList.of(
            PatternChoice.create("1", "Continuous"),
            PatternChoice.create("1100", "Dotted"),
            PatternChoice.create("1111111000000", "Dashed"),
            PatternChoice.create("111010", "Dash-dot"));
    public static final List<Pattern2dChoice> PATTERN_2D_CHOICES = ImmutableList.of(
            Pattern2dChoice.create("1", "Flat"),
            Pattern2dChoice.create(
                    "10\n" +
                    "01", "Small Grid"),
            Pattern2dChoice.create(
                    "1100\n" +
                    "1100\n" +
                    "0011\n" +
                    "0011", "Big Grid"),
            Pattern2dChoice.create(
                    "10\n" +
                    "10", "Y Stripes"),
            Pattern2dChoice.create(
                    "11\n" +
                    "00", "X Stripes"),
            Pattern2dChoice.create(
                    "111111\n" +
                    "110011\n" +
                    "100001\n" +
                    "110011\n" +
                    "111111\n", "Circle"),
            Pattern2dChoice.create(
                    "111111\n" +
                    "110011\n" +
                    "100001\n" +
                    "110011\n" +
                    "111111\n", "Circle"));

    private static final Tool NOP_TOOL = new Tool() { /* No-op */ };

    @FXML public Canvas vCanvas;
    @FXML public Label vStatus;
    @FXML public MenuBar vMenu;

    @FXML public ToggleButton vDdaLineTool;
    @FXML public ToggleButton vBresenhamLineTool;
    @FXML public ToggleButton vXorLineTool;
    @FXML public ToggleButton vPointTool;
    @FXML public ToggleButton vFreeTool;
    @FXML public ToggleButton vCircleTool;
    @FXML public ToggleButton vScanLineTool;
    @FXML public ToggleButton vFloodFillTool;
    @FXML public ToggleButton vCircleFill;
    @FXML public ToggleButton vZoomTool;

    @FXML public ToggleGroup mToolToggle;
    @FXML public ColorPicker vForegroundColorPicker;
    @FXML public ChoiceBox<PatternChoice> vPattern;
    @FXML public ChoiceBox<Pattern2dChoice> vPattern2d;
    @FXML public Button vCleanButton;
    @FXML public ToggleButton vSelectButton;

    private GraphicsContext mGc;
    private Tool mNopTool;
    private FrameBuffer mFb;
    private Grapher mGrapher;
    private List<Tool> mTools;
    private List<PersistableFigure> mFigures = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        initializeCanvas();
        vMenu.setUseSystemMenuBar(true);
    }

    private void initializeCanvas() {
        mGc = vCanvas.getGraphicsContext2D();

        // Setup
        mFb = FrameBuffer.empty(mGc, RESIZE_FACTOR);
        mGrapher = new Grapher(mFb);

        // Set proper events
        vCanvas.setOnMouseClicked(e -> getCurrentTool().onMouseClicked(e));
        vCanvas.setOnMouseMoved(e -> getCurrentTool().onMouseMoved(e));
        vCanvas.setOnMousePressed(e -> getCurrentTool().onMousePressed(e));
        vCanvas.setOnMouseDragged(e -> getCurrentTool().onMouseDragged(e));
        vCanvas.setOnMouseReleased(e -> getCurrentTool().onMouseReleased(e));

        // Set toggle values
        Map<ToggleButton, Tool> tools = ImmutableMap.<ToggleButton, Tool>builder()
                .put(vDdaLineTool, new DdaLineTool())
                .put(vBresenhamLineTool, new BresenhamLineTool())
                .put(vXorLineTool, new XorLineTool())
                .put(vCircleTool, new CircleTool())
                .put(vPointTool, new PointTool())
                .put(vFreeTool, new FreeTool())
                .put(vFloodFillTool, new FloodFillTool())
                .put(vScanLineTool, new ScanLineTool())
                .put(vCircleFill, new CircleFillTool())
                .put(vZoomTool, new ZoomTool(this, mFigures))
                .put(vSelectButton, new SelectTool())
                .build();
        mTools = new ArrayList<>(tools.size());
        tools.forEach((button, tool) -> {
            mTools.add(tool);
            tool.bind(mGrapher, vStatus, RESIZE_FACTOR);
            tool.setOnStartUseListener(t -> vCleanButton.setDisable(true));
            tool.setOnFinishUseListener(t -> {
                vCleanButton.setDisable(false);
                mFigures.addAll(t.getPersistableFigures());
            });
            button.setUserData(tool);
        });
        mNopTool = NOP_TOOL.bind(mGrapher, vStatus, RESIZE_FACTOR);

        // Deactivate pattern and color in xor tool
        vForegroundColorPicker.disableProperty().bind(vXorLineTool.selectedProperty());
        vPattern.disableProperty().bind(vXorLineTool.selectedProperty());

        // Color picker stuff
        mTools.forEach((tool) -> {
            mGrapher.colorProperty().bind(vForegroundColorPicker.valueProperty());
        });
        vForegroundColorPicker.setValue(Color.TRANSPARENT); // Trigger change
        vForegroundColorPicker.setValue(Color.BLACK); // Trigger change

        // Pattern choices
        ObservableList<PatternChoice> patternChoices = FXCollections.observableArrayList(PATTERN_CHOICES);
        vPattern.setItems(patternChoices);
        Properties.bind(mGrapher.patternProperty(), p -> p.pattern, vPattern.valueProperty());
        vPattern.getSelectionModel().select(0);

        // Pattern2d choices
        ObservableList<Pattern2dChoice> pattern2dChoices = FXCollections.observableArrayList(PATTERN_2D_CHOICES);
        vPattern2d.setItems(pattern2dChoices);
        Properties.bind(mGrapher.pattern2dProperty(), p -> p.pattern2d, vPattern2d.valueProperty());
        vPattern2d.getSelectionModel().select(0);
    }

    @Override
    protected void onSceneSet(Scene scene) {
        scene.setOnKeyTyped(event -> {
            getCurrentTool().onKeyTyped(event);
        });
    }

    private Tool getCurrentTool() {
        Toggle toggle = mToolToggle.getSelectedToggle();
        if (toggle == null) return mNopTool;
        return (Tool) toggle.getUserData();
    }

    public void onCleanButtonClick(ActionEvent event) {
        mFigures.clear();
        mFb.clean();
    }

    public void onOpenMenuClick(ActionEvent event) {
        File file = new FileChooser().showOpenDialog(getStage());
        if (file != null) {
            try {
                byte[] bytes = Files.toByteArray(file);
                Pack pack = Pack.readOnly(bytes);
                ImmutableList<PersistableFigure> figures = pack.<PersistableFigure>readPersistables();
                resetFigures(figures);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void resetFigures(Iterable<? extends PersistableFigure> figures) {
        mFigures.clear();
        mFb.clean();
        for (PersistableFigure figure : figures) {
            figure.draw(mGrapher);
            mFigures.add(figure);
        }
    }

    public void onSaveMenuClick(ActionEvent event) {
        Pack pack = Pack.writable();
        pack.writePersistables(mFigures);
        byte[] bytes = pack.toByteArray();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("draw.bmd");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Compatible images (bmd)", "*.bmd"));
        fileChooser.setTitle("Save Draw");
        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            try {
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                out.write(bytes);
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SelectTool extends Tool {

        @Override
        public void onMouseClicked(MouseEvent event) {
            Point p = getPosition(event);
            PersistableFigure toDelete = null;
            for (PersistableFigure figure : mFigures) {
                if (figure.isSelected(p)) {
                    toDelete = figure;
                    break;
                }
            }
            mFigures.remove(toDelete);
            resetFigures(new ArrayList<>(mFigures));
        }
    }

    public static class PatternChoice {

        public static PatternChoice create(String pattern, String label) {
            return new PatternChoice(DrawUtils.pattern(pattern), label);
        }

        public BitSet pattern;
        public String label;

        private PatternChoice(BitSet pattern, String label) {
            this.pattern = pattern;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof PatternChoice)) return false;
            PatternChoice that = (PatternChoice) object;
            return pattern.equals(that.pattern);
        }

        @Override
        public int hashCode() {
            return pattern.hashCode();
        }
    }

    public static class Pattern2dChoice {

        public static Pattern2dChoice create(String pattern2d, String label) {
            return new Pattern2dChoice(DrawUtils.pattern2d(pattern2d), label);
        }

        public BitMatrix pattern2d;
        public String label;

        public Pattern2dChoice(BitMatrix pattern2d, String label) {
            this.pattern2d = pattern2d;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pattern2dChoice)) return false;
            Pattern2dChoice that = (Pattern2dChoice) o;
            return !(pattern2d != null ? !pattern2d.equals(that.pattern2d) : that.pattern2d != null);
        }

        @Override
        public int hashCode() {
            return pattern2d != null ? pattern2d.hashCode() : 0;
        }
    }
}
