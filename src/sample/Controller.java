package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import model.*;
import java.io.*;

public class Controller {

    // gui
    @FXML
    private ScrollPane rootPane;
    @FXML
    private Canvas canvasPane;
    @FXML
    private MenuItem loadMenu;
    @FXML
    private MenuItem saveMenu;

    // graph
    public int width;
    public int height;
    private Graph graph;  // The model (i.e. the graph)
    private Node dragNode;
    private Point2D elasticEndLocation;
    private Edge dragEdge;
    private Point2D dragPoint;
    private GraphicsContext aPen;
    private Node currentSelectionNode; // currentSelectionNode selection node

    @FXML
    public void initialize(){

        /*GraphicsContext gr = canvasPane.getGraphicsContext2D();
        gr.setFill(Color.GREEN);*/

        initSystem();
    }

    private void initSystem(){
        initVariables();
        createMainMenu();
        createEvents();
        canvasPane.requestFocus();
        createDefaultGraph();
    }

    private void initVariables(){
        width = 0;
        height = 0;
        graph = null;
        dragNode = null;
        elasticEndLocation = null;
        dragEdge = null;
        dragPoint = null;
        aPen = null;
        currentSelectionNode = null;
    }

    private void createDefaultGraph(){
        graph = Graph.example();
        aPen = canvasPane.getGraphicsContext2D();
        graph.draw(aPen);
    }

    private void createMainMenu(){
        loadMenu.setAccelerator(KeyCombination.keyCombination("Ctrl+L"));
        saveMenu.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));

        // Set up the event handlers for the File menu
        loadMenu.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                FileChooser chooser = new FileChooser();
                chooser.setInitialDirectory(new File("C:\\"));
                chooser.setTitle("Load Graph");
                File f = chooser.showOpenDialog(null); // primaryStage
                if (f != null) {
                    try {
                        BufferedReader file = new BufferedReader(
                                new FileReader(f.getAbsolutePath()));
                        graph = Graph.loadFrom(file);
                        file.close();
                        update();
                    }
                    catch (Exception ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error !");
                        alert.setHeaderText(null);
                        alert.setContentText("Error Loading Graph From File !");
                        alert.showAndWait();
                    }
                }
            }
        });

        saveMenu.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                FileChooser chooser = new FileChooser();
                chooser.setInitialDirectory(new File("C:\\"));
                chooser.setTitle("Save Graph");
                File f = chooser.showSaveDialog(null); // primaryStage
                if (f != null) {
                    try {
                        PrintWriter file = new PrintWriter(
                                new FileWriter(f.getAbsolutePath()));
                        graph.saveTo(file);
                        file.close();
                    }
                    catch (Exception ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error !");
                        alert.setHeaderText(null);
                        alert.setContentText("Error Saving Graph To File !");
                        alert.showAndWait();
                    }
                }
            }
        });
    }

    private void createEvents(){
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if(width <= rootPane.getWidth()){
                canvasPane.setWidth(rootPane.getWidth());
                width = (int) canvasPane.getWidth();
                update();
                System.out.println("Resize width canvas: " + canvasPane.getWidth());
            }
        });

        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if(height <= rootPane.getHeight()) {
                canvasPane.setHeight(rootPane.getHeight());
                height = (int) canvasPane.getHeight();
                update();
                System.out.println("Resize height canvas: " + canvasPane.getHeight());
            }
        });

        canvasPane.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent ev) {
                dragPoint = new Point2D(ev.getX(), ev.getY());
                currentSelectionNode = graph.nodeAt(ev.getX(), ev.getY());
                if(ev.getButton() == MouseButton.SECONDARY && currentSelectionNode != null){
                    //mainContextMenu.show(canvas, ev.getScreenX(), ev.getScreenY());
                    return;
                }
                else if (ev.getClickCount() == 2) {
                    if (currentSelectionNode == null) {
                        Edge anEdge = graph.edgeAt(ev.getX(), ev.getY());
                        if (anEdge == null)
                            graph.addNode(new Node(ev.getX(), ev.getY()));
                        else
                            anEdge.toggleSelected();
                    }
                    else
                        currentSelectionNode.toggleSelected();
                    update();
                }
                else {
                    if (currentSelectionNode != null) {
                        dragNode = currentSelectionNode;
                        dragEdge = null;
                    }
                    else
                        dragEdge = graph.edgeAt(ev.getX(), ev.getY());
                    dragPoint = new Point2D(ev.getX(), ev.getY());
                }
                //mainContextMenu.hide();
            }

        });

        canvasPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent ev) {
                Node aNode = graph.nodeAt(ev.getX(), ev.getY());
                // Check to see if we have let go on a node
                if ((dragNode != null) && (aNode != null) && (aNode != dragNode))
                    // Change the model, by adding a new Edge
                    graph.addEdge(dragNode, aNode);
                // Update the view, by redrawing the Graph
                dragNode = null; // No need to remember this anymore
                update();
            }
        });

        canvasPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent ev) {
                elasticEndLocation = new Point2D(ev.getX(), ev.getY());
                if (dragNode != null) {
                    if (dragNode.isSelected()) {
                        for (Node n: graph.selectedNodes()) {
                            n.setLocation(n.getLocation().getX()+ev.getX()-dragPoint.getX(),
                                    n.getLocation().getY()+ev.getY()-dragPoint.getY());
                        }
                        dragPoint = new Point2D(ev.getX(), ev.getY());
                    }
                }

                if (dragEdge != null) {
                    if (dragEdge.isSelected()) {
                        dragEdge.getStartNode().setLocation(
                                dragEdge.getStartNode().getLocation().getX() +
                                        ev.getX() - dragPoint.getX(),
                                dragEdge.getStartNode().getLocation().getY() +
                                        ev.getY() - dragPoint.getY());
                        dragEdge.getEndNode().setLocation(
                                dragEdge.getEndNode().getLocation().getX() +
                                        ev.getX() - dragPoint.getX(),
                                dragEdge.getEndNode().getLocation().getY() +
                                        ev.getY() - dragPoint.getY());
                        dragPoint = new Point2D(ev.getX(), ev.getY());
                    }
                }
                update();
            }
        });

        canvasPane.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ev) {
                if (ev.getCode() == KeyCode.DELETE) {
                    // Delete all selected Edges
                    for (Edge e: graph.selectedEdges())
                        graph.deleteEdge(e);
                    // Delete all selected Nodes
                    for (Node n: graph.selectedNodes())
                        graph.deleteNode(n);
                    // Update the view, by redrawing the Graph
                    update();
                }
            }
        });
    }

    private void update() {
        aPen.setFill(Color.WHITE);
        aPen.fillRect(0, 0, width, height);
        graph.draw(aPen);
        // Draw the elastic band
        if (dragNode != null)
            if (!dragNode.isSelected())
                aPen.strokeLine(dragNode.getLocation().getX(),
                        dragNode.getLocation().getY(),
                        elasticEndLocation.getX(),
                        elasticEndLocation.getY());
    }

    public Graph getGraph() { return graph; }

    public void setGraph(Graph g) { graph = g; update(); }

}

// TODO: несколько контекстных меню:
// 1 - щелчок по ребру:
// изменить надпись(вызов диалог. окна)
// 2 - щелчок по узлу:
// изменить надпись внутри(вызов диалог. окна), изменить надпись рядом(вызов диалог. окна), изменить цвет узла
// условие: если выбрано несколько элементов - то не показываем контекстное меню(нет смысла)

// TODO: исключить дублирование линий (возможно)
// TODO: автоизменение размеров - можно смотреть по крайним нодам(возможно сделаю)

