/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package circuitlab;

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
import javafx.scene.image.ImageView;
import java.io.IOException;
import java.util.ArrayList;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.value.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.PickResult;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 *
 * @author Siddhant
 */

/* 
begin from program startup:
face shown at startup = face 1
rotate right one face = face 2
rotate right one face = face 3
rotate right one face = face 4
rotate up one face = face 5
rotate down two faces = face 6
*/

class CircuitMatrix  {
    static MatrixArray[] objArr;
    public CircuitMatrix() {
        objArr = new MatrixArray[6];
        for (int i = 0; i < 6; i++) 
            objArr[i] = new MatrixArray();
    }
    public MatrixArray getMArr(int facenum) {
        return objArr[facenum-1];
    }
}
class MatrixArray {
    static MatrixObject[][] mArr;
    public MatrixArray() {
        mArr = new MatrixObject[5][5];
        for (int r = 0; r < mArr.length; r++)
            for (int c = 0; c < mArr.length; c++)
                mArr[r][c] = new MatrixObject(); 
    }
    public ArrayList<Object> fetchCell(int facenum, fxpoint pt) { //coded in coordinates, approximates border of each grid square
        int row, col;
        ArrayList<Object> al = new ArrayList<Object>();
        if (facenum==1) {
            row = Math.abs((int) ((pt.y - 71.3)/28.1));
            col = Math.abs((int) ((pt.x - 71.3)/28.1));
            System.out.println("facenum=" + facenum + " row=" + row + " col=" + col);
        } else if (facenum==2) {
            row = Math.abs((int) ((pt.y - 71.3)/28.1));
            col = Math.abs((int) ((pt.z - (-71.3))/28.1));
            System.out.println("facenum=" + facenum + " row=" + row + " col=" + col);          
        } else if (facenum==3) {
            row = Math.abs((int) ((pt.y - 71.3)/28.1));
            col = Math.abs((int) ((pt.x - (-71.3))/28.1));
            System.out.println("facenum=" + facenum + " row=" + row + " col=" + col);
        } else if (facenum==4) {
            row = Math.abs((int) ((pt.y - 71.3)/28.1));
            col = Math.abs((int) ((pt.z - 71.3)/28.1));
            System.out.println("facenum=" + facenum + " row=" + row + " col=" + col);
        } else if (facenum==5) {
            row = Math.abs((int) ((pt.z - 71.3)/28.1));
            col = Math.abs((int) ((pt.x - 71.3)/28.1));
            System.out.println("facenum=" + facenum + " row=" + row + " col=" + col);
        } else if (facenum==6) {
            row = Math.abs((int) ((pt.x - (-71.3))/28.1));
            col = Math.abs((int) ((pt.z - 71.3)/28.1));
            System.out.println("facenum=" + facenum + " row=" + row + " col=" + col);
        } else {
            System.out.println("ERROR:");
            row = -1;
            col = -1;
        }
        al.add(mArr[row][col]);
        al.add(row);
        al.add(col);
        return al;
    }
    
}

class MatrixObject {
    //static RectangleContainer r;
    boolean filled;
    String objectText;
    MeshView mv;
    int id; //determines which player the object belongs to.
    
    public MatrixObject() {
      filled = false;
      objectText = "";
      mv = null;
    }
    public MatrixObject(MeshView mv1, String ot) {
        filled = true;
        mv = mv1;
        objectText = ot;
    }
    public void changeObj(MeshView mv1, String ot){
        filled = true;
        mv = mv1;
        objectText = ot;
    }
}

class fxpoint {
    double x, y, z;
    public fxpoint(double x1, double y1, double z1) {
        x=x1;y=y1;z=z1;
    }
}
