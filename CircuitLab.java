package circuitlab;

/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import javafx.application.Application;
import javafx.event.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.shape.Box;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import javafx.scene.shape.TriangleMesh;
import java.lang.Exception;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.control.*;
import javafx.geometry.*;
import javafx.scene.image.ImageView;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.value.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.PickResult;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class CircuitLab extends Application {
    
    final Group root = new Group();
    //Names of pictures and category names for titled pane accordion
    final String[] imageNames = {"wire_singlepic.PNG", "wire_fourwaypic.PNG", "wire_rightjunctionpic.PNG", "batterypic.PNG", "ledpic.PNG","capacitorpic.PNG", "resistorpic.PNG", "transformerpic.PNG"};
    final String[] categories = {"Single Wire (100)", "Four-way Wire (300)", "Right Junction Wire (200)", "Battery (1000)", "LED (2500)", "Capacitor (500)", "Resistor (200)", "Transformer (5000)"};
    final TitledPane[] tps = new TitledPane[imageNames.length];
    static StlMeshImporter stlImporter = new StlMeshImporter();
    
    private static final double CONTROL_MULTIPLIER = 0.1;    
    private static final double SHIFT_MULTIPLIER = 10.0;    
    private static final double MOUSE_SPEED = 0.1;    
    private static final double ROTATION_SPEED = 1.0;    
    private static final double TRACK_SPEED = 0.3;
    
    static int turnCounter = 0;
        
    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;
    final Xform world = new Xform(); //global node
    final static Xform boxGroup = new Xform(); //node of cube
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    private static final double CAMERA_INITIAL_DISTANCE = -450;
    private static final double CAMERA_INITIAL_X_ANGLE = 0;
    private static final double CAMERA_INITIAL_Y_ANGLE = 0;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;
    private static final double BOX_LENGTH = 150.0;
    private static int rotationCount = 0;
    final Accordion accordion = new Accordion(); //handles side accordion for circuit parts
    private static String selectedObj; 
    
    static int facenum;
    static CircuitMatrix cm;
    static Player p1;
    static Player p2;
    static Player[] playerArray;
    
    @Override
    public void start(Stage primaryStage) {    
        buildScene();
        buildCamera();
        buildBox();
        /*BackgroundImage myBI= new BackgroundImage(new Image("http://fortbendlifestylesandhomes.com/wp-content/uploads/2014/03/nithin-parsan-009-767x1024.jpg",1000,150,false,true),
        BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
          BackgroundSize.DEFAULT);
        StackPane stack = new StackPane(root);
        //stack.setId("pane");
        stack.setBackground(new Background(myBI));*/
        
        //StackPane stack = new StackPane(root);         
        
        buildAccordion();
        //accordion.setExpandedPane(tps[0]);
        //Scene scene = new Scene(stack, 1024, 768, true);
        //stack.getChildren().add(root);
        //scene.getStylesheets().addAll(this.getClass().getResource("style.css").toExternalForm());
        Group topPane = new Group();
        Node resultPanel = createLeftOverlay();
        Node playerInfo = createMidOverlay();
        topPane.getChildren().addAll(resultPanel, playerInfo);
        SubScene resultScene = new SubScene(topPane,800,75);
        
        /*Subscene separates 3D nodes from 2D UI Controls */
        SubScene subScene = new SubScene(root,600,600,true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        
        /*Sets up the right border, which adds two buttons for orientation and 
        opening rules. */
        BorderPane pane = new BorderPane();
        pane.setCenter(subScene);
        pane.setTop(resultScene);
        Button button = new Button("Orient");
        button.setOnAction(e->{
            if(cameraXform.rx.getAngle() != 0 && rotationCount == 0) {
                cameraXform.reset();
            } else {
                rotateCamera();
                //RotateTransition rt = rotateReset(boxGroup);
                //rt.play();
            }
            
        });
        Button instrct = new Button("Rules");
        instrct.setOnAction(e->{
            ProcessBuilder pb = new ProcessBuilder("Notepad.exe", "rules.txt");
            try {
                pb.start();
            } catch (IOException ex) {
                Logger.getLogger(CircuitLab.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        });
        ToolBar toolBar = new ToolBar(button, instrct);
        toolBar.setOrientation(Orientation.VERTICAL); 
       
        cm = new CircuitMatrix();
        p1 = new Player(true, 0, 1000, 0);
        p2 = new Player(false, 1, 1000, 0);
        playerArray = new Player[2];
        playerArray[0] = p1;
        playerArray[1] = p2;
        
        pane.setRight(toolBar);
        pane.setLeft(accordion);
        pane.setPrefSize(300,300);
                
        Scene scene = new Scene(pane);        
        
        handleKeyboard(scene, world);
        handleMouse(scene, world);
        
        primaryStage.setTitle("Circuit Lab");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        primaryStage.requestFocus();
        //scene.setCamera(camera);
        
    }
    /*Two methods to rotate the cube in set orientation so that parts can be placed on the cube.
        This rotates the cameraXform.*/
    private void rotateCamera() {
        if(rotationCount < 3) cameraXform.ry.setAngle(cameraXform.ry.getAngle()+90);
        else if(rotationCount == 3) cameraXform.rx.setAngle(cameraXform.rx.getAngle()+90);
        else if(rotationCount == 4) cameraXform.rx.setAngle(cameraXform.rx.getAngle()+180);
        if(rotationCount == 5) rotationCount = 0;
        rotationCount++;
        
    }
    
    /*Rotates the cube to all six sides with set orientation so that circuit parts can be placed,
       with animation. This method turns the boxGroup Xform rather than the cameraXform. */
    private RotateTransition rotateReset(Node node) {
        RotateTransition rotate = new RotateTransition(Duration.seconds(3), node);
        if(rotationCount < 3) {
            rotate.setAxis(Rotate.Y_AXIS);
            rotate.setFromAngle(0);
            rotate.setToAngle(90);
        } else if(rotationCount == 3) {
            rotate.setAxis(Rotate.X_AXIS);
            rotate.setFromAngle(0);
            rotate.setToAngle(90);
        } else {
            rotate.setAxis(Rotate.X_AXIS);
            rotate.setFromAngle(0);
            rotate.setToAngle(180);
        }
        rotate.setInterpolator(Interpolator.LINEAR);
        //rotate.setCycleCount(RotateTransition.INDEFINITE);
        rotationCount++;
        if(rotationCount == 5) rotationCount = 0;
        return rotate;
    }
    
    /*Builds the accordion UI object storing the titlepanes of circuit parts. It
    looks for the opening of a titled pane in order to know it has been selected */
    private void buildAccordion() {
        try {
            for (int i = 0; i < imageNames.length; i++) { //replace hard code loop limit
                ImageView img = new ImageView(new Image(getClass().getResourceAsStream(imageNames[i])));
                img.setPreserveRatio(true);
                img.setFitWidth(200);
                tps[i] = new TitledPane(categories[i],img);
            }
        } catch (Exception e) {
            for (int i = 0; i < 3; i++) { //replace hard code loop limit
               e.printStackTrace();
                tps[i] = new TitledPane("",null);
            }
        }
        accordion.getPanes().addAll(tps);
        accordion.expandedPaneProperty().addListener(new 
            ChangeListener<TitledPane>() {
                public void changed(ObservableValue<? extends TitledPane> ov,
                    TitledPane old_val, TitledPane new_val) {
                        if (new_val != null) {
                            selectedObj = accordion.getExpandedPane().getText();
                            selectedObj = selectedObj.substring(0, selectedObj.indexOf(" ("));
                            String tmp = selectedObj;
                            if(tmp.contains("Wire")) {
                                selectedObj = "wire_";
                                if(tmp.contains("Four-way")) selectedObj += "fourway";
                                else if(tmp.contains("Right Junction")) selectedObj += "rightjunction";
                                else selectedObj += "single";
                            }
                        } else {
                            selectedObj = null;
                            System.out.println("closed");
                        }
              }
        });
    }
    
    
    private void buildScene() {
        System.out.println("buildScene");
        root.getChildren().add(world);
    }
    
    private void buildCamera() {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(180.0);
 
        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
    }
    
    private void buildBox() {
        final String MAP = "http://3.bp.blogspot.com/-xbCBk-wQHqs/TyLMbgjTdzI/AAAAAAAABio/dIJldqxQSA0/s1600/5x5.grid+ref+1.jpeg";
        //final String MAP = "grid.png";
        final PhongMaterial grid = new PhongMaterial();
        grid.setDiffuseMap(new Image(MAP, 1920/2d, 1080/2d, true, true));

        final Box box = new Box(BOX_LENGTH, BOX_LENGTH, BOX_LENGTH);
        box.setMaterial(grid);
        boxListener(box); 
        boxGroup.getChildren().add(box);
        boxGroup.setVisible(true);
        world.getChildren().addAll(boxGroup);
        
    }
    
    /*Creates a overlay for mouse information*/
    Text data, caption, turn;
    HBox hBox = new HBox(10);
    private Node createLeftOverlay() {
        caption = new Text("Node:\nPoint:\nTexture Coord:\nFace:\nDistance:");
        caption.setFont(Font.font("Times New Roman", 10));
        caption.setTextOrigin(VPos.TOP);
        caption.setTextAlignment(TextAlignment.RIGHT);

        data = new Text("-- None --\n\n\n\n");
        data.setFont(Font.font("Times New Roman", 10));
        data.setTextOrigin(VPos.TOP);
        data.setTextAlignment(TextAlignment.LEFT);

        Rectangle rect = new Rectangle(100, 50, null);
        hBox.getChildren().addAll(caption, data);
        return new Group(rect, hBox);   
    }
    private Node createMidOverlay() {
        turn = new Text("Turn: Player " + (turnCounter % 2 + 1) + "\nCurrency: \nVoltage: ");
        turn.setFont(Font.font("Times New Roman", 13));
        turn.setTextOrigin(VPos.TOP);
        data.setTextAlignment(TextAlignment.CENTER);
        Rectangle rect = new Rectangle(100, 50, null);
        rect.setX(400);
        hBox.getChildren().addAll(turn);
        return new Group(rect, hBox); 
        
    }
    
    /*listens for the mouse when it enters the box and exits the box. */
    private void boxListener(Box shape) {
        EventHandler<MouseEvent> moveHandler = (MouseEvent event) -> {
            PickResult res = event.getPickResult();
            setState(res);
            event.consume();
        };

        shape.setOnMouseMoved(moveHandler);
        shape.setOnMouseDragOver(moveHandler);

        shape.setOnMouseEntered((MouseEvent event) -> {
            PickResult res = event.getPickResult();
            if (res == null) {
                System.err.println("Mouse entered has not pickResult");
            }
            setState(res);
        });

        shape.setOnMouseExited((MouseEvent event) -> {
            PickResult res = event.getPickResult();
            if (res == null) {
                System.err.println("Mouse exited has not pickResult");
            }
            setState(res);
            event.consume();
        });
        
        shape.setOnMouseClicked((MouseEvent event) -> {
            PickResult res = event.getPickResult();
            if (res == null) {
                System.err.println("Mouse clicked has no pickResult");
            }
            placeItemOnClick(res);
            setPlayerInfo();
            event.consume();
        });
    }
    /*Sets the data for the overlay mouse information */
    final void setState(PickResult result) {
            
        if (result.getIntersectedNode() == null) {
            data.setText("Scene\n\n"
                    + point3DToString(result.getIntersectedPoint()) + "\n"
                    + point2DToString(result.getIntersectedTexCoord()) + "\n"
                    + result.getIntersectedFace() + "\n"
                    + String.format("%.1f", result.getIntersectedDistance()));
        } else {
            data.setText(result.getIntersectedNode().getId() + "\n"
                    + point3DToString(result.getIntersectedPoint()) + "\n"
                    + point2DToString(result.getIntersectedTexCoord()) + "\n"
                    + result.getIntersectedFace() + "\n"
                    + String.format("%.1f", result.getIntersectedDistance()));
        }
    }
    
    final void setPlayerInfo() {
        turn.setText("Turn: Player " + (turnCounter % 2 + 1) + "\n" + "Currency: " + (playerArray[turnCounter%2].currency) + "\nVoltage: " + 
                (playerArray[turnCounter%2].voltage));
    }
    
    private static String point3DToString(Point3D pt) {
        if (pt == null) {
            return "null";
        }
        return String.format("%.1f; %.1f; %.1f", pt.getX(), pt.getY(), pt.getZ());
    }

    private static String point2DToString(Point2D pt) {
        if (pt == null) {
            return "null";
        }
        return String.format("%.2f; %.2f", pt.getX(), pt.getY());
    }
    
    /*Places a selected circuit part when clicking on the cube. */
    static MeshView prevMV;
    static String prevObjText;
    private static void placeItemOnClick(PickResult res) {
        if(selectedObj == null) System.out.println("No object selected");
        else {
            try {
                URL modelUrl = CircuitLab.class.getResource(selectedObj.toLowerCase() + ".stl");
                stlImporter.read(modelUrl);            
            }
            catch (Exception e) {
                // handle exception
            }
            
            TriangleMesh stlMesh = stlImporter.getImport();
            MeshView placedObj= new MeshView(stlMesh);
            
            String MAP; 
            MAP = (turnCounter%2==0) ? "http://cdn7.bigcommerce.com/s-hfhomm5/images/stencil/1280x1280/products/180/451/Solid_Red_Sized__25214.1507754519.jpg?c=2&imbypass=on" : 
                    "http://www.solidbackgrounds.com/images/2560x1440/2560x1440-brandeis-blue-solid-color-background.jpg";
            final PhongMaterial grid = new PhongMaterial();
            grid.setDiffuseMap(new Image(MAP, 1920/2d, 1080/2d, true, true));
            placedObj.setMaterial(grid);
            placedObj.setTranslateX(res.getIntersectedPoint().getX()); //border length approx 5
            placedObj.setTranslateY(res.getIntersectedPoint().getY());
            placedObj.setTranslateZ(res.getIntersectedPoint().getZ());
            if((int) res.getIntersectedPoint().getY() == 75) {
                placedObj.setRotationAxis(Rotate.X_AXIS);
                placedObj.setRotate(90.0);
                placedObj.setTranslateY(res.getIntersectedPoint().getY()+3.3*2);                                          //KEY FOR FACENUMS IN CIRCUITMATRIX CLASS
                facenum = 5; 
            } else if((int)res.getIntersectedPoint().getY() == -75) {
                placedObj.setRotationAxis(Rotate.X_AXIS);
                placedObj.setRotate(270.0);
                facenum=6;
            } else if((int)res.getIntersectedPoint().getX() == 75) {
                placedObj.setRotationAxis(Rotate.Y_AXIS);
                placedObj.setRotate(270.0);
                facenum=4;
            } else if((int)res.getIntersectedPoint().getX() == -75) {
                placedObj.setRotationAxis(Rotate.Y_AXIS);
                placedObj.setRotate(90.0);
                placedObj.setTranslateX(res.getIntersectedPoint().getX()-3.3*2);
                facenum=2;
            } else if((int)res.getIntersectedPoint().getZ() == 75) {
                placedObj.setRotationAxis(Rotate.Y_AXIS);
                placedObj.setRotate(180.0);
                placedObj.setTranslateZ(res.getIntersectedPoint().getZ()+10);
                facenum = 3;
            } else {
                facenum = 1;
            }
            MatrixArray ma = cm.getMArr(facenum);
            MatrixObject mo = (MatrixObject) ma.fetchCell(facenum, new fxpoint(res.getIntersectedPoint().getX(),res.getIntersectedPoint().getY(),res.getIntersectedPoint().getZ())).get(0);
            int row = (int) ma.fetchCell(facenum, new fxpoint(res.getIntersectedPoint().getX(),res.getIntersectedPoint().getY(),res.getIntersectedPoint().getZ())).get(1);
            int col = (int) ma.fetchCell(facenum, new fxpoint(res.getIntersectedPoint().getX(),res.getIntersectedPoint().getY(),res.getIntersectedPoint().getZ())).get(2);
            if (mo.filled == false) {
                boxGroup.getChildren().addAll(placedObj);
                ma.mArr[row][col].changeObj(placedObj, selectedObj.toLowerCase(), turnCounter % 2);
            } else {
                if(turnCounter % 2 == mo.id) {
                    MeshView prevMV = ma.mArr[row][col].mv;
                    boxGroup.getChildren().remove(prevMV);
                    boxGroup.getChildren().addAll(placedObj);
                    ma.mArr[row][col].changeObj(placedObj, selectedObj.toLowerCase(), turnCounter % 2);
                } else {
                    turnCounter--;
                }
                //ma.mArr[row][col].changeObj(stlMesh, selectedObj.toLowerCase());
            }
            turnCounter++;
            System.out.println(turnCounter);
        }
        
    }
    
    /*Handles the mouse when rotating the box. */
    private void handleMouse(Scene scene, final Node root) {
 
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            }
        });
        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = (mousePosX - mouseOldX); 
                mouseDeltaY = (mousePosY - mouseOldY);

               double modifier = 1.0;

               if (me.isControlDown()) {
                    modifier = CONTROL_MULTIPLIER;
                } 
                if (me.isShiftDown()) {
                    modifier = SHIFT_MULTIPLIER;
                }     
                if (me.isPrimaryButtonDown()) {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() -
                       mouseDeltaX*modifier*ROTATION_SPEED);  // 
                   cameraXform.rx.setAngle(cameraXform.rx.getAngle() +
                       mouseDeltaY*modifier*ROTATION_SPEED);  // -
                }
                else if (me.isSecondaryButtonDown()) {
                    double z = camera.getTranslateZ();
                    double newZ = z + mouseDeltaX*MOUSE_SPEED*modifier;
                    camera.setTranslateZ(newZ);
                }
                else if (me.isMiddleButtonDown()) {
                   cameraXform2.t.setX(cameraXform2.t.getX() + 
                      mouseDeltaX*MOUSE_SPEED*modifier*TRACK_SPEED);  // -
                   cameraXform2.t.setY(cameraXform2.t.getY() + 
                      mouseDeltaY*MOUSE_SPEED*modifier*TRACK_SPEED);  // -
                }
           }
       }); // setOnMouseDragged
   } //handleMouse
    
    private void handleKeyboard(Scene scene, final Node root) {

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
               switch (event.getCode()) {
                   case Z:
                       cameraXform2.t.setX(0.0);
                       cameraXform2.t.setY(0.0);
                       cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
                       cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
                       break;
               } // switch
            } // handle()
        });  // setOnKeyPressed
    }  //  handleKeyboard()

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}