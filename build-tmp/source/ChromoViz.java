import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ChromoViz extends PApplet {



float[] angles;

ChromIdeogram chr_ideogram;

BedAnnot bed_table;

BedPEAnnot bed_pe_table;

int full_radius;

float spacer_rad = 0.02f;

int num_chr;


//for navigation around canvas:
float zoom  = 1;

//offset for dragging
PVector mouseClick;
PVector prevMousePos;
PVector targetOffset = new PVector(0,0);
PVector offset = new PVector(0,0);
boolean dragging = false;

//angles for rotation
boolean rotating = false;
float rotateXangle = 0;
float rotateYangle = 0;
float rotateZangle = 0;
float x_angle = 0;
float y_angle = 0;



public void setup(){

	chr_ideogram = new ChromIdeogram("hg19.seqs.chr1-22.X.Y.fa.tsv");

	bed_table = new BedAnnot("test.bed");

	bed_pe_table = new BedPEAnnot("test.bedpe");



	size(800,800, OPENGL);
	frameRate(30);
	// noLoop();

}

public void draw(){
	background(255);
	full_radius = PApplet.parseInt(min(width, height) * 0.4f);

	
	// smooth movement of canvas -- move by increments 

    PVector d = new PVector();

    d = PVector.sub(targetOffset, offset);
    d.mult(0.1f);
    offset = PVector.add(offset, d);
 
 	pushMatrix();
    scale(zoom);
    translate(offset.x, offset.y);

    rotateX(rotateXangle + x_angle);
    rotateY(rotateYangle + y_angle);
    // rotateZ(rotateZangle);
	  	

  	bed_table.drawAsInt(full_radius * 0.8f);
  	bed_table.drawAsDot(full_radius * 0.9f);

  	bed_pe_table.drawAsIntPairBezier(full_radius);

  	chr_ideogram.draw(full_radius);
  	popMatrix();

}







// ------ key and mouse events ------

public void keyPressed(){
  

    if (keyCode == UP) zoom += 0.05f;
    if (keyCode == DOWN) zoom -= 0.05f;
    zoom = max(zoom, 0.1f);

    if(key == 'r'){
    	zoom = 1;
		offset = new PVector(0,0);
		targetOffset = offset;
    }

    
}

public void mousePressed() {

	// canvas dragging


	println("click!");
	mouseClick = new PVector(mouseX, mouseY);


}

public void mouseDragged(){

	if(mouseButton==LEFT){
	  	PVector mousePos = new PVector(mouseX, mouseY);
	    targetOffset = PVector.sub(mousePos, mouseClick);
	}
	if(mouseButton==RIGHT){
		x_angle = map(mouseX-mouseClick.x, 0, width, 0, TWO_PI);
		y_angle = map(mouseY-mouseClick.y, 0, width, 0, TWO_PI);
	}

}


public void mouseReleased() {

	if (dragging){
		dragging = false;
	}

	if (rotating){
		rotating = false;
		rotateXangle += x_angle;
		rotateYangle += y_angle;
	}


}

public void mouseEntered(MouseEvent e) {
  loop();
}

public void mouseExited(MouseEvent e) {
  noLoop();
}







//class to store a bed file (bed3)
//methods to draw it in different ways


class BedAnnot{
	
	Table bed_table;

	BedAnnot(String bed_file){

		bed_table = loadTable(bed_file, "header, tsv");
	}


	//draw as colored interval
	public void drawAsInt(float radius){

		float start_angle = 0.0f; 
		float end_angle = 0.0f; 
		String chr_name = "";
		// int startpos = 0;
		// int endpos = 0;

		for (TableRow row : bed_table.rows()){

			chr_name = row.getString("chr");
			// startpos = row.getInt("start");
			// endpos = row.getInt("end");

			// println(chr_name, startpos, endpos);

			start_angle = chr_ideogram.genToPolar(chr_name, row.getInt("start"));
			end_angle = chr_ideogram.genToPolar(chr_name, row.getInt("end"));
			intBand(start_angle, end_angle, radius, width/2, height/2, 40, 180);
		}

	}

	public void drawAsDot(float radius){

		float start_angle = 0.0f; 
		float end_angle = 0.0f; 
		String chr_name = "";
		// int startpos = 0;
		// int endpos = 0;

		for (TableRow row : bed_table.rows()){

			chr_name = row.getString("chr");
			// startpos = row.getInt("start");
			// endpos = row.getInt("end");

			// println(chr_name, startpos, endpos);

			start_angle = chr_ideogram.genToPolar(chr_name, row.getInt("start"));
			end_angle = chr_ideogram.genToPolar(chr_name, row.getInt("end"));
			intMidDot(start_angle, end_angle, radius, width/2, height/2);
		}

	}

	//draw as dot

	//draw as triangle

	//draw as scatter?
}
class BedPEAnnot{
	
	Table bed_pe_table;

	BedPEAnnot(String bed_file){

		bed_pe_table = loadTable(bed_file, "header, tsv");
	}

	public void drawAsIntPairBezier(float radius){

		float start_angle1 = 0.0f; 
		float end_angle1 = 0.0f; 
		String chr_name1 = "";

		float start_angle2 = 0.0f; 
		float end_angle2 = 0.0f; 
		String chr_name2 = "";



		for (TableRow row : bed_pe_table.rows()){

			chr_name1 = row.getString("chr1");
			start_angle1 = chr_ideogram.genToPolar(chr_name1, row.getInt("start1"));
			end_angle1 = chr_ideogram.genToPolar(chr_name1, row.getInt("end1"));

			chr_name2 = row.getString("chr2");
			start_angle2 = chr_ideogram.genToPolar(chr_name2, row.getInt("start2"));
			end_angle2 = chr_ideogram.genToPolar(chr_name2, row.getInt("end2"));


			intPairBezier(start_angle1, end_angle1, start_angle2, end_angle2, radius, width/2, height/2);

		}

	}
}
class ChromIdeogram{


	Table chr_table;
	float tot_length;
	

	ChromIdeogram(String table_name){

		chr_table = loadTable(table_name, "header");

		num_chr = chr_table.getRowCount();

		//take into account the spacers between chromosomes 
		float tot_available_angle = TWO_PI - (num_chr*spacer_rad);

		angles = new float[chr_table.getRowCount()];



		//add columns to table to store info for polar coordinates

		//to store the start angle for polar coordinates of this chr
		chr_table.addColumn("start_angle", Table.FLOAT);
		//to store the end angle for polar coordinates of this chr
		chr_table.addColumn("end_angle", Table.FLOAT);
		//to store int value of chr length
		chr_table.addColumn("chr_length_bp", Table.INT);

		
		tot_length = 0.0f;
		for (TableRow row : chr_table.rows()){
			tot_length += row.getInt("length");
		}

		float start_angle = 0;

		int index = 0;

		for (TableRow row : chr_table.rows()){
			
			int chr_length = row.getInt("length");
			String chr_name = row.getString("chr_name");
			
			float angle = map(chr_length, 0, tot_length, 0.0f, tot_available_angle);

			row.setFloat("start_angle", start_angle);
			row.setFloat("end_angle", start_angle + angle);

			start_angle = start_angle + angle + spacer_rad;
			println(index, chr_name);
			index ++;

		}
	} 

	public void draw(float radius){

		int i = 0;
		for (TableRow row : chr_table.rows()){
			int gray_val = PApplet.parseInt(map(i, 0, num_chr, 0, 255));
			intBand(row.getFloat("start_angle"), row.getFloat("end_angle"), radius, width/2, height/2, 40, gray_val);
			i++;
		}
	}

	public TableRow get_chr_table_row(String chr_name){

		return chr_table.getRow(getChrIndex(chr_name));

	}

	public int getChrIndex(String chr_name){
		if (chr_name.equals( "X")){
			return 22;
		}
		else if (chr_name.equals("Y")){
				return 23;
			
		} else{

			// println(chr_name, int(chr_name));
			return PApplet.parseInt(chr_name) - 1;
		}
	}

	public float genToPolar(String chr_name, int pos){

		// println(chr, pos);
		TableRow chr_ref = chr_table.getRow(getChrIndex(chr_name));

		// find the angle corresponding to the chromosome and position, given the start and end angles defined for that chromosome
		float angle = map(pos, 0, chr_ref.getInt("length"), chr_ref.getFloat("start_angle"), chr_ref.getFloat("end_angle"));
		// println(angle);
		return angle;
	}
}
//methods for drawing shapes given polar coordinates

public void intBand(float start_angle, float end_angle, float radius, float center_x, float center_y, float band_width, float grey_val){

	//if angle is too small, make it the minimum to be seen on screen
	if (end_angle - start_angle < 0.002f){
			start_angle -= 0.001f;
			end_angle += 0.001f;
	}

	pushMatrix();
	translate(center_x, center_y);

	float outside_r = radius + band_width / 2;
	float inside_r = radius - band_width / 2; 

	float middle_angle = start_angle + ((end_angle - start_angle) / 2);

	float int_control_angle = start_angle - ((end_angle - start_angle) / 2);
	float ext_control_angle = end_angle + ((end_angle - start_angle) / 2);
	// println(middle_angle);

	//interior control point 1
	float cp1_x = outside_r * cos(int_control_angle);
	float cp1_y = outside_r * sin(int_control_angle);

	//interior control point 2
	float cp2_x = inside_r * cos(int_control_angle);
	float cp2_y = inside_r * sin(int_control_angle);

	//exterior control point 3
	float cp3_x = inside_r * cos(ext_control_angle);
	float cp3_y = inside_r * sin(ext_control_angle);

	//exterior control point 4
	float cp4_x = outside_r * cos(ext_control_angle);
	float cp4_y = outside_r * sin(ext_control_angle);


	//point 1
	float a = outside_r * cos(start_angle);
	float b = outside_r * sin(start_angle);

	//point 2
	float c = inside_r * cos(start_angle);
	float d = inside_r * sin(start_angle);

	//point 3
	float e = inside_r * cos(middle_angle);
	float f = inside_r * sin(middle_angle);

	//point 4
	float g = inside_r * cos(end_angle);
	float h = inside_r * sin(end_angle);

	//point 5
	float i = outside_r * cos(end_angle);
	float j = outside_r * sin(end_angle);

	//point 6
	float k = outside_r * cos(middle_angle);
	float l = outside_r * sin(middle_angle);

	// println(a, b, c, d, e, f, g, h, i, j, k, l);

	noStroke();
	fill(grey_val);
	beginShape();
		vertex(a,b);
		vertex(c, d);
		curveVertex(cp2_x, cp2_y);
		curveVertex(c,d);
		curveVertex(e,f);
		curveVertex(g,h);
		curveVertex(cp3_x, cp3_y);
		vertex(g,h);
		vertex(i,j);
		curveVertex(cp4_x, cp4_y);
		curveVertex(i,j);
		curveVertex(k,l);
		curveVertex(a,b);
		curveVertex(cp1_x, cp1_y);
	endShape();

	popMatrix();



}

public void intPairBezier(float start_angle1, float end_angle1, float start_angle2, float end_angle2, float radius, float center_x, float center_y){

	pushMatrix();
	translate(center_x, center_y);

	//first interval
	//point 1
	float a = radius * cos(start_angle1);
	float b = radius * sin(start_angle1);

	//point 2
	float c = radius * cos(end_angle1);
	float d = radius * sin(end_angle1);

	//second interval
	//point 3
	float e = radius * cos(start_angle2);
	float f = radius * sin(start_angle2);

	//point 4
	float g = radius * cos(end_angle2);
	float h = radius * sin(end_angle2);



	fill(102,194,165, 180);
  	noStroke();
	beginShape();
		vertex(a,b);
		vertex(c,d);
		// bezierVertex(e/10, d/10, e,f, e,f);
		// vertex(g, h);
		// //push the control point in the opposite side of the curve to make it thicker
		// bezierVertex(0-g/10, 0-b/10, a, b, a, b);

		bezierVertex(0, 0, e,f, e,f);
		vertex(g, h);
		//push the control point in the opposite side of the curve to make it thicker
		bezierVertex(0, 0, a, b, a, b);

	endShape();

	popMatrix();

}

public void intMidDot(float start_angle, float end_angle, float radius, float center_x, float center_y){

	pushMatrix();
	translate(center_x, center_y);

	float middle_angle = start_angle + (end_angle - start_angle)/2;

	//point 1
	float a = x_polToCart(middle_angle,radius);
	float b = y_polToCart(middle_angle,radius);



	fill(252,141,98);
	ellipse(a, b, 10, 10);


	popMatrix();

}


// void intMidDot(float start_angle, float end_angle, float radius, float center_x, float center_y){

// 	pushMatrix();
// 	translate(center_x, center_y);

// 	middle_angle = start_angle + (end_angle - start_angle)/2;

// 	//point 1
// 	float a = x_polToCart(start_angle,radius);
// 	float b = y_polToCart(start_angle,radius);

// 	//point 2
// 	float c = x_polToCart(end_angle, radius);
// 	float d = y_polToCart(end_angle,radius);

// 	fill(252,141,98);
// 	ellipse(x, y, width, height);


// 	popMatrix();

// }

public float x_polToCart(float angle, float radius){

	return radius * cos(angle);
}

public float y_polToCart(float angle, float radius){
	return radius * sin(angle);
}








  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ChromoViz" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
