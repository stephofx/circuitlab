/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.net.URL;
import javafx.scene.shape.TriangleMesh;
import java.lang.Exception;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.control.*;
import javafx.geometry.*;

/**
 *
 * 
 */
public class CircuitLab extends Application {
    
    final Group root = new Group();
    
    //final String[] imageNames = {"resistor.stl", "capacitor.stl", ""};
    final TitledPane[] tps = new TitledPane[3];
    
    private static final double CONTROL_MULTIPLIER = 0.1;    
    private static final double SHIFT_MULTIPLIER = 10.0;    
    private static final double MOUSE_SPEED = 0.1;    
    private static final double ROTATION_SPEED = 1.0;    
    private static final double TRACK_SPEED = 0.3;
        
    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;
    final Xform world = new Xform();
    final Xform boxGroup = new Xform();
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    private static final double CAMERA_INITIAL_DISTANCE = -450;
    private static final double CAMERA_INITIAL_X_ANGLE = 70.0;
    private static final double CAMERA_INITIAL_Y_ANGLE = 320.0;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;
    private static final double BOX_LENGTH = 150.0;
    
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
        StlMeshImporter stlImporter = new StlMeshImporter();
        try {
            URL modelUrl = this.getClass().getResource("resistor.stl");
            stlImporter.read(modelUrl);            
        }
        catch (Exception e) {
            // handle exception
        }
        TriangleMesh stlMesh = stlImporter.getImport();
        MeshView resistor = new MeshView(stlMesh);
        resistor.setTranslateX(58); //border length approx 5
        resistor.setTranslateY(65);
        resistor.setTranslateZ(60+5);
        resistor.setRotationAxis(Rotate.X_AXIS);
        resistor.setRotate(90.0);
        //Xform resistorXform = new Xform();
        //resistorXform.setRotate(0,90,0);
        Group tmpGroup = new Group();
        tmpGroup.getChildren().addAll(resistor);
        tmpGroup.setVisible(true);
        
        
        world.getChildren().addAll(tmpGroup);
        final Accordion accordion = new Accordion();
        for(int i = 0; i < 3; i++) { //replace hard code loop limit
            tps[i] = new TitledPane("",null);
        }
        accordion.getPanes().addAll(tps);
        //Scene scene = new Scene(stack, 1024, 768, true);
        //stack.getChildren().add(root);
        //scene.getStylesheets().addAll(this.getClass().getResource("style.css").toExternalForm());
        
        SubScene subScene = new SubScene(root,500,500,true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        
        BorderPane pane = new BorderPane();
        pane.setCenter(subScene);
        Button button = new Button("Reset");
        button.setOnAction(e->{
            //rotateX.setAngle(-20);
            //rotateY.setAngle(-20);
        });
        CheckBox checkBox = new CheckBox("Line");
        checkBox.setOnAction(e->{
           //box.setDrawMode(checkBox.isSelected()?DrawMode.LINE:DrawMode.FILL);
        });
        ToolBar toolBar = new ToolBar(button, checkBox);
        toolBar.setOrientation(Orientation.VERTICAL); 
        pane.setLeft(toolBar);
        pane.setRight(accordion);
        pane.setPrefSize(300,300);
                
        Scene scene = new Scene(pane);        
        
        handleKeyboard(scene, world);
        handleMouse(scene, world);
        
        primaryStage.setTitle("Circuit Lab");
        primaryStage.setScene(scene);
        primaryStage.show();
        //scene.setCamera(camera);
        
    }
    private void buildToolbar() {
        
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

        Xform boxXform = new Xform();

        final Box box = new Box(BOX_LENGTH, BOX_LENGTH, BOX_LENGTH);
        box.setMaterial(grid);
        boxGroup.getChildren().add(box);
        boxGroup.setVisible(true);
        world.getChildren().addAll(boxGroup);
        
    }
    
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