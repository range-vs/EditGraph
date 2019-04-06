package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Edge {
    private String label;
    private Node startNode, endNode;
    public static final int WIDTH = 7;
    private boolean selected;
    public boolean isSelected() { return selected;}
    public void setSelected(boolean state) { selected = state;}
    public void toggleSelected() { selected = !selected;}

    public Edge(Node start, Node end) { this("", start, end); }
    public Edge(String aLabel, Node start, Node end) {
        label = aLabel;
        startNode = start;
        endNode = end;
    }
    public String getLabel() { return label; }
    public Node getStartNode() { return startNode; }
    public Node getEndNode() { return endNode; }
    public void setLabel(String newLabel) { label = newLabel; }
    public void setStartNode(Node aNode) { startNode = aNode; }
    public void setEndNode(Node aNode) { endNode = aNode; }
    // Edges look like this: sNode(12,43) --> eNode(67,34)
    public String toString() {
        return(startNode.toString() + " --> " + endNode.toString());
    }
    public Node otherEndFrom(Node aNode) {
        if (startNode == aNode)
            return endNode;
        else
            return startNode;
    }
    public void draw(GraphicsContext aPen) {
        // Draw black line from center of startNode to center of endNode
        if (selected) {
            aPen.setStroke(Color.RED);
            double xDiff = Math.abs(startNode.getLocation().getX() -
                    endNode.getLocation().getX());
            double yDiff = Math.abs(startNode.getLocation().getY() -
                    endNode.getLocation().getY());
            for (int i= -WIDTH/2; i<=WIDTH/2; i++) {
                if (yDiff > xDiff)
                    aPen.strokeLine(startNode.getLocation().getX()+i,
                            startNode.getLocation().getY(),
                            endNode.getLocation().getX()+i,
                            endNode.getLocation().getY());
                else
                    aPen.strokeLine(startNode.getLocation().getX(),
                            startNode.getLocation().getY()+i,
                            endNode.getLocation().getX(),
                            endNode.getLocation().getY()+i);
            }
        }
        else {
            aPen.setStroke(Color.BLACK);
            aPen.strokeLine(startNode.getLocation().getX(),
                    startNode.getLocation().getY(),
                    endNode.getLocation().getX(),
                    endNode.getLocation().getY());
        }
    }

   public void saveTo(PrintWriter aFile) {
        aFile.println(label);
        aFile.println((int)startNode.getLocation().getX());
        aFile.println((int)startNode.getLocation().getY());
        aFile.println((int)endNode.getLocation().getX());
        aFile.println((int)endNode.getLocation().getY());
        aFile.println(selected);
    }
    public static Edge loadFrom(BufferedReader aFile) throws IOException {
        Edge anEdge;
        String aLabel = aFile.readLine();
        Node start = new Node("TEMP");
        Node end = new Node("TEMP");
        start.setLocation(Integer.parseInt(aFile.readLine()),
                Integer.parseInt(aFile.readLine()));
        end.setLocation(Integer.parseInt(aFile.readLine()),
                Integer.parseInt(aFile.readLine()));
        anEdge = new Edge(aLabel, start, end);
        anEdge.setSelected(Boolean.valueOf(aFile.readLine()).booleanValue());
        return anEdge;
    }
}
