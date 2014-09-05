package br.com.bernardorufino.paint.ui;

import br.com.bernardorufino.paint.ext.ContextAwareController;
import br.com.bernardorufino.paint.ext.Properties;
import br.com.bernardorufino.paint.grapher.FrameBuffer;
import br.com.bernardorufino.paint.grapher.Grapher;
import br.com.bernardorufino.paint.tools.*;
import br.com.bernardorufino.paint.utils.DrawUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.*;

public class WindowController extends ContextAwareController implements Initializable {

    public static final String COORD_STATUS = "%d X %d";
    private static final int RESIZE_FACTOR = 2;
    public static final List<PatternChoice> PATTERN_CHOICES = ImmutableList.of(
            PatternChoice.create("1", "Continuous"),
            PatternChoice.create("1100", "Dotted"),
            PatternChoice.create("1111111000000", "Dashed"),
            PatternChoice.create("111010", "Dash-dot")
    );


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
    @FXML public ToggleGroup mToolToggle;
    @FXML public ColorPicker vForegroundColorPicker;
    @FXML public ChoiceBox<PatternChoice> vPattern;

    private GraphicsContext mGc;
    private Tool mNopTool;
    private FrameBuffer mFb;
    private Grapher mGrapher;
    private List<Tool> mTools;
    private ObservableList<PatternChoice> mPatternChoices;


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
                .build();
        mTools = new ArrayList<>(tools.size());
        tools.forEach((button, tool) -> {
            mTools.add(tool);
            tool.bind(mGrapher, vStatus, RESIZE_FACTOR);
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
        mPatternChoices = FXCollections.observableArrayList(PATTERN_CHOICES);
        vPattern.setItems(mPatternChoices);
        Properties.bind(mGrapher.patternProperty(), p -> p.pattern, vPattern.valueProperty());
        vPattern.getSelectionModel().select(0);
    }

    private Tool getCurrentTool() {
        Toggle toggle = mToolToggle.getSelectedToggle();
        if (toggle == null) return mNopTool;
        return (Tool) toggle.getUserData();
    }

    public void onCleanButtonClick(ActionEvent event) {
        mFb.clean();
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
}
