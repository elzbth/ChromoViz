import processing.opengl.*;

float[] angles;

ChromIdeogram chr_ideogram;

BedAnnot bed_table;

BedPEAnnot bed_pe_table;

int full_radius;

float spacer_rad = 0.02;

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



void setup(){

	chr_ideogram = new ChromIdeogram("hg19.seqs.chr1-22.X.Y.fa.tsv");

	bed_table = new BedAnnot("test.bed");

	bed_pe_table = new BedPEAnnot("test.bedpe");



	size(800,800, OPENGL);
	frameRate(30);
	// noLoop();

}

void draw(){
	background(255);
	full_radius = int(min(width, height) * 0.4);

	
	// smooth movement of canvas -- move by increments 

    PVector d = new PVector();

    d = PVector.sub(targetOffset, offset);
    d.mult(0.1);
    offset = PVector.add(offset, d);
 
 	pushMatrix();
    scale(zoom);
    translate(offset.x, offset.y);

    rotateX(rotateXangle + x_angle);
    rotateY(rotateYangle + y_angle);
    // rotateZ(rotateZangle);
	  	

  	bed_table.drawAsInt(full_radius * 0.8);
  	bed_table.drawAsDot(full_radius * 0.9);

  	bed_pe_table.drawAsIntPairBezier(full_radius);

  	chr_ideogram.draw(full_radius);
  	popMatrix();

}







// ------ key and mouse events ------

void keyPressed(){
  

    if (keyCode == UP) zoom += 0.05;
    if (keyCode == DOWN) zoom -= 0.05;
    zoom = max(zoom, 0.1);

    if(key == 'r'){
    	zoom = 1;
		offset = new PVector(0,0);
		targetOffset = offset;
    }

    
}

void mousePressed() {

	// canvas dragging


	println("click!");
	mouseClick = new PVector(mouseX, mouseY);


}

void mouseDragged(){

	if(mouseButton==LEFT){
	  	PVector mousePos = new PVector(mouseX, mouseY);
	    targetOffset = PVector.sub(mousePos, mouseClick);
	}
	if(mouseButton==RIGHT){
		x_angle = map(mouseX-mouseClick.x, 0, width, 0, TWO_PI);
		y_angle = map(mouseY-mouseClick.y, 0, width, 0, TWO_PI);
	}

}


void mouseReleased() {

	if (dragging){
		dragging = false;
	}

	if (rotating){
		rotating = false;
		rotateXangle += x_angle;
		rotateYangle += y_angle;
	}


}

void mouseEntered(MouseEvent e) {
  loop();
}

void mouseExited(MouseEvent e) {
  noLoop();
}







